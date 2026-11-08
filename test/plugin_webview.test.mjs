import { describe, it } from 'node:test';
import * as assert from 'node:assert/strict';
import * as fs from 'node:fs';

describe('JetBrains JCEF ToolWindow and plugin.xml', () => {
  it('wires ZplFileType and ZplPreviewToolWindowFactory in plugin.xml', () => {
    const xml = fs.readFileSync('src/main/resources/META-INF/plugin.xml', 'utf-8');
    const html = fs.readFileSync('src/main/resources/webview/preview.html', 'utf-8');
    const factory = fs.readFileSync(
      'src/main/kotlin/com/zplflow/intellij/ZplPreviewToolWindowFactory.kt',
      'utf-8'
    );

    assert.ok(xml.includes('implementationClass="com.zplflow.intellij.ZplFileType"'));
    assert.ok(xml.includes('extensions="zpl;prn;label"'));
    assert.ok(xml.includes('factoryClass="com.zplflow.intellij.ZplPreviewToolWindowFactory"'));
    assert.ok(html.includes('window.updateZplPreview'));
    assert.ok(factory.includes('JBCefBrowser'));
  });
});
