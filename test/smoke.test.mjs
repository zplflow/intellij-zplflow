import { describe, it } from 'node:test';
import * as assert from 'node:assert/strict';
import * as fs from 'node:fs';

describe('intellij-zplflow scaffold', () => {
  it('has build.gradle.kts and plugin.xml', () => {
    assert.ok(fs.existsSync('build.gradle.kts'));
    assert.ok(fs.existsSync('src/main/resources/META-INF/plugin.xml'));
  });

  it('targets IntelliJ Platform sinceBuild 262+ for LspIntegrationProvider', () => {
    const gradle = fs.readFileSync('build.gradle.kts', 'utf-8');
    assert.ok(gradle.includes('sinceBuild = "262"'));
  });
});
