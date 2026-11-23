package com.zplflow.intellij.lsp

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspIntegrationProvider
import com.intellij.platform.lsp.api.ProjectWideLspClientDescriptor
import com.zplflow.intellij.ZplFileType
import com.zplflow.intellij.ZplflowSettings
import java.io.File

class ZplLspIntegrationProvider : LspIntegrationProvider {
    override fun fileOpened(
        project: Project,
        file: VirtualFile,
        clientStarter: LspIntegrationProvider.LspClientStarter
    ) {
        if (isZplFile(file)) {
            val settings = ZplflowSettings.getInstance()
            if (settings.state.lspEnabled) {
                clientStarter.ensureClientStarted(ZplLspClientDescriptor(project))
            }
        }
    }

    companion object {
        fun isZplFile(file: VirtualFile): Boolean {
            val ext = file.extension?.lowercase()
            return ext == "zpl" || ext == "prn" || ext == "label" || file.fileType == ZplFileType
        }

        fun resolvePluginDir(): File? {
            val location = ZplLspIntegrationProvider::class.java.protectionDomain?.codeSource?.location
                ?: return null
            val codeFile = runCatching { File(location.toURI()) }.getOrNull() ?: return null
            val candidates = listOfNotNull(
                codeFile.parentFile?.parentFile,
                codeFile.parentFile,
                codeFile
            )
            return candidates.firstOrNull { File(it, "bin").isDirectory } ?: codeFile.parentFile?.parentFile
        }
    }
}

class ZplLspClientDescriptor(project: Project) : ProjectWideLspClientDescriptor(project, "zplflow-lsp") {
    override fun isSupportedFile(file: VirtualFile): Boolean =
        ZplLspIntegrationProvider.isZplFile(file)

    override fun createCommandLine(): GeneralCommandLine {
        val settings = ZplflowSettings.getInstance()
        val pluginDir = ZplLspIntegrationProvider.resolvePluginDir()
        val binary = ZplLspServerResolver.resolve(settings.state.lspPath, pluginDir)
            ?: throw IllegalStateException(
                "zplflow-lsp binary not found. Please install zplflow-lsp or configure its path in Settings."
            )
        return GeneralCommandLine(binary)
    }
}
