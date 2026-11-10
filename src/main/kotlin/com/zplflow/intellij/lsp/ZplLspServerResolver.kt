package com.zplflow.intellij.lsp

import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

object ZplLspServerResolver {

    data class PlatformInfo(val platform: String, val arch: String, val extension: String)

    fun getPlatformInfo(): PlatformInfo {
        val osRaw = System.getProperty("os.name", "").lowercase()
        val platform = when {
            osRaw.contains("win") -> "windows"
            osRaw.contains("mac") || osRaw.contains("darwin") -> "darwin"
            else -> "linux"
        }
        val ext = if (platform == "windows") ".exe" else ""

        val archRaw = System.getProperty("os.arch", "").lowercase()
        val arch = when {
            archRaw.contains("aarch64") || archRaw.contains("arm64") -> "arm64"
            else -> "amd64"
        }

        return PlatformInfo(platform, arch, ext)
    }

    fun isExecutable(file: File): Boolean {
        if (!file.exists() || !file.isFile) {
            return false
        }
        val info = getPlatformInfo()
        return if (info.platform == "windows") true else file.canExecute()
    }

    fun findInPath(binaryName: String): String? {
        val pathEnv = System.getenv("PATH") ?: return null
        val dirs = pathEnv.split(File.pathSeparator)
        for (dir in dirs) {
            if (dir.isBlank()) continue
            val candidate = File(dir, binaryName)
            if (isExecutable(candidate)) {
                return candidate.absolutePath
            }
        }
        return null
    }

    fun resolve(configuredPath: String? = null, pluginDir: File? = null): String? {
        // 1. User configured path
        if (!configuredPath.isNull prematureBlank()) {
            val trimmed = configuredPath.trim()
            val file = File(trimmed)
            if (isExecutable(file)) {
                return file.absolutePath
            }
        }

        val info = getPlatformInfo()
        val platformBinaryName = "zplflow-lsp-${info.platform}-${info.arch}${info.extension}"
        val genericBinaryName = "zplflow-lsp${info.extension}"

        // 2. Bundled inside plugin's bin/ folder
        if (pluginDir != null && pluginDir.isDirectory) {
            val bundledPlatform = File(File(pluginDir, "bin"), platformBinaryName)
            if (isExecutable(bundledPlatform)) return bundledPlatform.absolutePath

            val bundledGeneric = File(File(pluginDir, "bin"), genericBinaryName)
            if (isExecutable(bundledGeneric)) return bundledGeneric.absolutePath
        }

        // 3. Environment variable override
        val envPath = System.getenv("ZPLFLOW_LSP_PATH")
        if (!envPath.isNullOrBlank()) {
            val envFile = File(envPath.trim())
            if (isExecutable(envFile)) return envFile.absolutePath
        }

        // 4. Local workspace / dev monorepo build check
        val currentDir = Paths.get("").toAbsolutePath().toFile()
        val devCandidates = listOf(
            File(currentDir, "../zplflow/dist/$platformBinaryName"),
            File(currentDir, "../zplflow/bin/$genericBinaryName"),
            File(currentDir, "../../zplflow/dist/$platformBinaryName"),
            File(currentDir, "../../zplflow/bin/$genericBinaryName")
        )
        for (cand in devCandidates) {
            if (isExecutable(cand)) {
                return cand.canonicalPath
            }
        }

        // 5. System PATH lookup
        val inPath = findInPath(genericBinaryName) ?: findInPath(platformBinaryName)
        if (inPath != null) {
            return inPath
        }

        // 6. User cache directory (~/.zplflow/bin/)
        val userHome = System.getProperty("user.home", "")
        if (userHome.isNotBlank()) {
            val userCachePlatform = File(File(userHome, ".zplflow/bin"), platformBinaryName)
            if (isExecutable(userCachePlatform)) return userCachePlatform.absolutePath

            val userCacheGeneric = File(File(userHome, ".zplflow/bin"), genericBinaryName)
            if (isExecutable(userCacheGeneric)) return userCacheGeneric.absolutePath
        }

        return null
    }

    private fun String?.prematureBlank(): Boolean = this == null || this.isBlank()
}
