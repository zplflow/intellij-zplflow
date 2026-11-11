package com.zplflow.intellij

import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegate
import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegateAdapter
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiFile

class ZplEnterHandler : EnterHandlerDelegateAdapter() {
    override fun preprocessEnter(
        file: PsiFile,
        editor: Editor,
        caretOffset: Ref<Int>,
        caretAdvance: Ref<Int>,
        dataContext: DataContext,
        originalHandler: EditorActionHandler?
    ): EnterHandlerDelegate.Result {
        val name = file.name.lowercase()
        if (file.fileType != ZplFileType && !name.endsWith(".zpl") && !name.endsWith(".prn") && !name.endsWith(".label")) {
            return EnterHandlerDelegate.Result.Continue
        }

        val doc = editor.document
        val offset = editor.caretModel.offset
        val text = doc.text
        if (offset < 0 || offset > text.length) return EnterHandlerDelegate.Result.Continue

        val before = text.substring(0, offset)
        val lastXa = before.lastIndexOf("^XA")
        val lastXz = before.lastIndexOf("^XZ")

        if (lastXa > lastXz) {
            val lineNum = doc.getLineNumber(offset)
            val lineStart = doc.getLineStartOffset(lineNum)
            val currentLine = text.substring(lineStart, offset)
            val indent = if (currentLine.trim().startsWith("^XA")) {
                "  "
            } else {
                val leadingWs = currentLine.takeWhile { it == ' ' || it == '\t' }
                if (leadingWs.isEmpty()) "  " else leadingWs
            }

            doc.insertString(offset, "\n$indent")
            editor.caretModel.moveToOffset(offset + 1 + indent.length)
            return EnterHandlerDelegate.Result.Stop
        }

        return EnterHandlerDelegate.Result.Continue
    }
}
