package com.zplflow.intellij

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction

class ReformatZplAction : AnAction("Format ZPL Label", "Format and indent ZPL label commands", null) {
    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val doc = editor.document
        val formatted = ZplFormatter.format(doc.text)
        if (formatted != doc.text) {
            WriteCommandAction.runWriteCommandAction(e.project) {
                doc.setText(formatted)
            }
        }
    }

    override fun update(e: AnActionEvent) {
        val psi = e.getData(CommonDataKeys.PSI_FILE)
        val isZpl = if (psi != null) {
            val name = psi.name.lowercase()
            psi.fileType == ZplFileType || name.endsWith(".zpl") || name.endsWith(".prn") || name.endsWith(".label")
        } else {
            false
        }
        e.presentation.isEnabledAndVisible = isZpl
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}
