import { describe, it } from 'node:test';
import * as assert from 'node:assert/strict';
import * as fs from 'node:fs';

describe('IntelliJ ZPL LSP integration', () => {
  it('defines ZplLspServerResolver with multi-platform detection, env override, and candidate search', () => {
    const src = fs.readFileSync(
      'src/main/kotlin/com/zplflow/intellij/lsp/ZplLspServerResolver.kt',
      'utf-8'
    );
    assert.ok(src.includes('object ZplLspServerResolver'));
    assert.ok(src.includes('getPlatformInfo()'));
    assert.ok(src.includes('"windows"'));
    assert.ok(src.includes('"darwin"'));
    assert.ok(src.includes('"linux"'));
    assert.ok(src.includes('"arm64"'));
    assert.ok(src.includes('"amd64"'));
    assert.ok(src.includes('ZPLFLOW_LSP_PATH'));
    assert.ok(src.includes('findInPath(genericBinaryName)'));
    assert.ok(src.includes('.zplflow/bin'));
  });

  it('implements ZplLspIntegrationProvider and ZplLspClientDescriptor without PluginManager internal API', () => {
    const src = fs.readFileSync(
      'src/main/kotlin/com/zplflow/intellij/lsp/ZplLspServerSupportProvider.kt',
      'utf-8'
    );
    assert.ok(src.includes('class ZplLspIntegrationProvider : LspIntegrationProvider'));
    assert.ok(src.includes('clientStarter: LspIntegrationProvider.LspClientStarter'));
    assert.ok(src.includes('class ZplLspClientDescriptor(project: Project) : ProjectWideLspClientDescriptor'));
    assert.ok(src.includes('clientStarter.ensureClientStarted(ZplLspClientDescriptor(project))'));
    assert.ok(src.includes('resolvePluginDir()'));
    assert.ok(src.includes('protectionDomain?.codeSource?.location'));
    assert.ok(src.includes('GeneralCommandLine(binary)'));
    assert.ok(src.includes('ZplLspServerResolver.resolve'));
    assert.ok(!src.includes('PluginManager'));
    assert.ok(!src.includes('LspServerSupportProvider'));
    assert.ok(!src.includes('ProjectWideLspServerDescriptor'));
  });

  it('configures plugin.xml and zplflow-lsp.xml for JetBrains LSP integrationProvider extension point', () => {
    const pluginXml = fs.readFileSync('src/main/resources/META-INF/plugin.xml', 'utf-8');
    const lspXml = fs.readFileSync('src/main/resources/META-INF/zplflow-lsp.xml', 'utf-8');

    assert.ok(pluginXml.includes('<depends optional="true" config-file="zplflow-lsp.xml">com.intellij.modules.lsp</depends>'));
    assert.ok(lspXml.includes('defaultExtensionNs="com.intellij.platform.lsp"'));
    assert.ok(lspXml.includes('<integrationProvider'));
    assert.ok(lspXml.includes('implementation="com.zplflow.intellij.lsp.ZplLspIntegrationProvider"'));
    assert.ok(!lspXml.includes('serverSupportProvider'));
  });
});
