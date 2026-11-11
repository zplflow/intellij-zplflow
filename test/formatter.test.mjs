import { describe, it } from 'node:test';
import * as assert from 'node:assert/strict';
import * as fs from 'node:fs';

describe('ZPL Formatter and Indentation', () => {
  it('defines ZplFormatter with command tokenization and field formatting', () => {
    const src = fs.readFileSync('src/main/kotlin/com/zplflow/intellij/ZplFormatter.kt', 'utf-8');
    assert.ok(src.includes('object ZplFormatter'));
    assert.ok(src.includes('fun format(text: String, indent: String = "  "): String'));
    assert.ok(src.includes('^XA'));
    assert.ok(src.includes('^XZ'));
    assert.ok(src.includes('^FS'));
  });

  it('defines ZplEnterHandler for auto-indentation on Enter inside ^XA...^XZ', () => {
    const src = fs.readFileSync('src/main/kotlin/com/zplflow/intellij/ZplEnterHandler.kt', 'utf-8');
    assert.ok(src.includes('class ZplEnterHandler : EnterHandlerDelegateAdapter'));
    assert.ok(src.includes('lastIndexOf("^XA")'));
    assert.ok(src.includes('lastIndexOf("^XZ")'));
  });

  it('defines ZplDocumentFormattingService and ReformatZplAction', () => {
    const serviceSrc = fs.readFileSync('src/main/kotlin/com/zplflow/intellij/ZplDocumentFormattingService.kt', 'utf-8');
    const actionSrc = fs.readFileSync('src/main/kotlin/com/zplflow/intellij/ReformatZplAction.kt', 'utf-8');
    assert.ok(serviceSrc.includes('class ZplDocumentFormattingService : AsyncDocumentFormattingService'));
    assert.ok(actionSrc.includes('class ReformatZplAction : AnAction'));
  });

  it('wires formatting and enter handler in plugin.xml', () => {
    const xml = fs.readFileSync('src/main/resources/META-INF/plugin.xml', 'utf-8');
    assert.ok(xml.includes('implementation="com.zplflow.intellij.ZplDocumentFormattingService"'));
    assert.ok(xml.includes('implementation="com.zplflow.intellij.ZplEnterHandler"'));
    assert.ok(xml.includes('class="com.zplflow.intellij.ReformatZplAction"'));
  });
});
