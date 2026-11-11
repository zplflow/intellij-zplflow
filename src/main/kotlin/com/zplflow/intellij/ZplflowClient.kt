package com.zplflow.intellij

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.Duration

/**
 * Options describing a single preview render request.
 */
data class PreviewRequestOptions(
    val zpl: String,
    val dpi: Int = 203,
    val widthMm: Double = 101.6,
    val heightMm: Double = 152.4,
    val page: Int = 0
)

/**
 * Data returned by the zplflow preview endpoint.
 */
data class PreviewResponseData(
    val zpl: String,
    val pngBase64: String,
    val pageCount: Int,
    val currentPage: Int,
    val fromCache: Boolean,
    val rateLimitRemaining: Int? = null
)

/**
 * Thrown when the zplflow API answers with HTTP 429 Too Many Requests.
 */
class RateLimitException(message: String, val retryAfterSeconds: Int) : RuntimeException(message)

/**
 * Thin HTTP client for the zplflow preview API.
 *
 * Renders are cached in a bounded, access-ordered (LRU) map keyed by the
 * SHA-256 digest of the render options, and HTTP 429 responses are surfaced as
 * [RateLimitException] carrying the server provided Retry-After delay.
 */
class ZplflowClient(private val maxCacheEntries: Int = 50) {
    private val httpClient: HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build()

    private val cache = object : LinkedHashMap<String, PreviewResponseData>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, PreviewResponseData>?): Boolean {
            return size > maxCacheEntries
        }
    }

    @Synchronized
    fun clearCache() {
        cache.clear()
    }

    /**
     * Computes the SHA-256 based cache key for the given options.
     */
    fun computeCacheKey(opts: PreviewRequestOptions): String {
        val raw = "${opts.zpl}|${opts.dpi}|${opts.widthMm}|${opts.heightMm}|${opts.page}"
        val digest = MessageDigest.getInstance("SHA-256").digest(raw.toByteArray(StandardCharsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    /**
     * Renders a ZPL document to a base64 encoded PNG, using the cache when possible.
     */
    fun renderPreview(baseUrl: String, apiKey: String, opts: PreviewRequestOptions): PreviewResponseData {
        require(apiKey.isNotBlank()) { "Missing ZplFlow API key. Configure it in Settings > Tools > ZplFlow." }

        val key = computeCacheKey(opts)
        synchronized(this) {
            cache[key]?.let { return it.copy(fromCache = true) }
        }

        val cleanBase = baseUrl.trimEnd('/')
        val escapedZpl = opts.zpl
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\r", "\\r")
            .replace("\n", "\\n")

        val bodyJson = """{"zpl":"$escapedZpl","steps":[],"page":${opts.page},"dpi":${opts.dpi},"width_mm":${opts.widthMm},"height_mm":${opts.heightMm}}"""

        val req = HttpRequest.newBuilder()
            .uri(URI.create("$cleanBase/v1/pipelines/preview"))
            .timeout(Duration.ofSeconds(15))
            .header("Authorization", "Bearer ${apiKey.trim()}")
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(bodyJson, StandardCharsets.UTF_8))
            .build()

        val resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))

        if (resp.statusCode() == 429) {
            val retryAfter = resp.headers().firstValue("Retry-After").orElse("5").toIntOrNull() ?: 5
            throw RateLimitException("Rate limit exceeded. Retry in $retryAfter seconds.", retryAfter)
        }

        if (resp.statusCode() !in 200..299) {
            throw RuntimeException("Preview request failed (HTTP ${resp.statusCode()}): ${resp.body()}")
        }

        val body = resp.body()
        val pngBase64 = Regex(""""png_base64"\s*:\s*"([^"]+)"""").find(body)?.groupValues?.get(1) ?: ""
        val pageCount = Regex(""""page_count"\s*:\s*(\d+)""").find(body)?.groupValues?.get(1)?.toIntOrNull() ?: 1
        val currentPage = Regex(""""current_page"\s*:\s*(\d+)""").find(body)?.groupValues?.get(1)?.toIntOrNull() ?: 0
        val remaining = resp.headers().firstValue("X-RateLimit-Remaining").orElse(null)?.toIntOrNull()

        val result = PreviewResponseData(
            zpl = opts.zpl,
            pngBase64 = pngBase64,
            pageCount = pageCount,
            currentPage = currentPage,
            fromCache = false,
            rateLimitRemaining = remaining
        )

        synchronized(this) {
            cache[key] = result
        }
        return result
    }
}
