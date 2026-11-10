import { describe, it } from 'node:test';
import * as assert from 'node:assert/strict';
import * as fs from 'node:fs';

describe('ZplLanguage, ZplFileType, and ZplflowSettings', () => {
  it('defines ZplLanguage, ZplFileType, and ZplflowSettings with defaults', () => {
    const lang = fs.readFileSync('src/main/kotlin/com/zplflow/intellij/ZplLanguage.kt', 'utf-8');
    const ft = fs.readFileSync('src/main/kotlin/com/zplflow/intellij/ZplFileType.kt', 'utf-8');
    const settings = fs.readFileSync('src/main/kotlin/com/zplflow/intellij/ZplflowSettings.kt', 'utf-8');

    assert.ok(lang.includes('object ZplLanguage : Language("ZPL")'));
    assert.ok(ft.includes('override fun getDefaultExtension(): String = "zpl"'));
    assert.ok(settings.includes('var apiUrl: String = "https://api.zplflow.com"'));
    assert.ok(settings.includes('var dpi: Int = 203'));
    assert.ok(settings.includes('var widthMm: Double = 101.6'));
    assert.ok(settings.includes('var heightMm: Double = 152.4'));
    assert.ok(settings.includes('var lspEnabled: Boolean = true'));
    assert.ok(settings.includes('var lspPath: String = ""'));
  });
});
