package com.zplflow.intellij.lsp

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspServerSupportProvider
import com.intellij.platform.lsp.api.ProjectWideLspServerDescriptor
import com.zplflow.intellij.ZplFileType
import com.zplflow.intellij.ZplflowSettings

class ZplLspServerSupportProvider : LspServerSupportProvider {
    override fun fileOpened(
        project: Project,
        file: VirtualFile,
        serverStarter: LspServerSupportProvider.LspServerStarter
    ) {
        if (isZplFile(file)) {
            val settings = ZplflowSettings.getInstance()
            if (settings.state.lspEnabled) {
                serverStarter.ensureServerStarted(ZplLspServerDescriptor(project))
            }
        }
    }

    companion object {
        fun isZplFile(file: VirtualFile): Boolean {
            val ext = file.extension?.lowercase()
            return ext == "zpl" || ext == "prn" || ext == "label" || file.fileType == ZplFileType
        }
    }
}

class ZplLspServerDescriptor(project: Project) : ProjectWideLspServerDescriptor(project, "zplflow-lsp") {
    override fun isSupportedFile(file: VirtualFile): Boolean =
        ZplLspServerSupportProvider.isZplFile(file)

    override fun createCommandLine(): GeneralCommandLine {
        val settings = ZplflowSettings.getInstance()
        val pluginDescriptor = PluginManagerCore.getPlugin(PluginId.getId("com.zplflow"))
        val pluginDir = pluginDescriptor?.pluginPath?.toFile()
        val binary = ZplLspServerResolver.resolve(settings.state.lspPath, pluginDir)
            ?: throw IllegalStateException(
                "zplflow-lsp binary not found. Please install zplflow-lsp or configure its path in Settings."
            )
        return GeneralCommandLine(binary)
    }
}
