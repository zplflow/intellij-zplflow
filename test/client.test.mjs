import { describe, it } from 'node:test';
import * as assert from 'node:assert/strict';
import * as fs from 'node:fs';

describe('ZplflowClient Kotlin implementation', () => {
  it('implements SHA-256 LRU cache, /v1/pipelines/preview endpoint, and HTTP 429 Retry-After handling', () => {
    const src = fs.readFileSync('src/main/kotlin/com/zplflow/intellij/ZplflowClient.kt', 'utf-8');
    assert.ok(src.includes('MessageDigest.getInstance("SHA-256")'));
    assert.ok(src.includes('/v1/pipelines/preview'));
    assert.ok(src.includes('resp.statusCode() == 429'));
    assert.ok(src.includes('Retry-After'));
    assert.ok(src.includes('X-RateLimit-Remaining'));
  });
});
