package com.zplflow.intellij

import com.intellij.formatting.service.AsyncDocumentFormattingService
import com.intellij.formatting.service.AsyncFormattingRequest
import com.intellij.formatting.service.FormattingService
import com.intellij.openapi.util.NlsSafe
import com.intellij.psi.PsiFile

class ZplDocumentFormattingService : AsyncDocumentFormattingService() {
    override fun getFeatures(): Set<FormattingService.Feature> = setOf(
        FormattingService.Feature.FORMAT_FRAGMENTS
    )

    override fun canFormat(file: PsiFile): Boolean {
        val name = file.name.lowercase()
        return file.fileType == ZplFileType || name.endsWith(".zpl") || name.endsWith(".prn") || name.endsWith(".label")
    }

    override fun createFormattingTask(request: AsyncFormattingRequest): AsyncDocumentFormattingService.FormattingTask? {
        val originalText = request.documentText
        val formatted = ZplFormatter.format(originalText)
        return object : AsyncDocumentFormattingService.FormattingTask {
            override fun run() {
                request.onTextReady(formatted)
            }

            override fun cancel(): Boolean = true
        }
    }

    override fun getNotificationGroupId(): String = "ZplFlow"

    override fun getName(): @NlsSafe String = "ZplFlow Formatter"
}
