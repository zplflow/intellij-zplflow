package com.zplflow.intellij

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileDocumentManagerListener
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.jcef.JBCefApp
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.util.Alarm
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefLoadHandlerAdapter
import java.awt.BorderLayout
import javax.swing.JLabel
import javax.swing.JPanel

class ZplPreviewToolWindowFactory : ToolWindowFactory, DumbAware {
    private val client = ZplflowClient()

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = JPanel(BorderLayout())
        if (!JBCefApp.isSupported()) {
            panel.add(JLabel("JCEF is required for zplflow Live Preview."), BorderLayout.CENTER)
            val content = ContentFactory.getInstance().createContent(panel, "", false)
            toolWindow.contentManager.addContent(content)
            return
        }

        val browser = JBCefBrowser()
        val html = ZplPreviewToolWindowFactory::class.java
            .getResource("/webview/preview.html")
            ?.readText() ?: "<html><body>Missing preview.html</body></html>"
        browser.loadHTML(html)
        panel.add(browser.component, BorderLayout.CENTER)

        val content = ContentFactory.getInstance().createContent(panel, "", false)
        toolWindow.contentManager.addContent(content)

        fun isZplFile(vFile: VirtualFile?): Boolean {
            if (vFile == null) return false
            val ext = vFile.extension?.lowercase() ?: ""
            return ext == "zpl" || ext == "prn" || ext == "label" || vFile.fileType == ZplFileType
        }

        fun doRender(zplText: String) {
            if (zplText.isBlank()) return
            val settings = ZplflowSettings.getInstance().state
            val apiKey = settings.apiKey.ifBlank {
                System.getenv("ZPLFLOW_API_KEY") ?: System.getenv("ZPL_API_KEY") ?: ""
            }

            ApplicationManager.getApplication().executeOnPooledThread {
                try {
                    val res = client.renderPreview(
                        settings.apiUrl,
                        apiKey,
                        PreviewRequestOptions(
                            zpl = zplText,
                            dpi = settings.dpi,
                            widthMm = settings.widthMm,
                            heightMm = settings.heightMm
                        )
                    )
                    val quotaVal = res.rateLimitRemaining?.toString() ?: "null"
                    val js = """window.updateZplPreview({"pngBase64":"${res.pngBase64}","dpi":${settings.dpi},"currentPage":${res.currentPage},"pageCount":${res.pageCount},"fromCache":${res.fromCache},"rateLimitRemaining":$quotaVal});"""
                    browser.cefBrowser.executeJavaScript(js, "", 0)
                } catch (e: Exception) {
                    val safeMsg = (e.message ?: "Preview failed")
                        .replace("\\", "\\\\")
                        .replace("\"", "\\\"")
                        .replace("\n", " ")
                    val js = """window.updateZplPreview({"error":"$safeMsg"});"""
                    browser.cefBrowser.executeJavaScript(js, "", 0)
                }
            }
        }

        fun renderActiveEditor() {
            ApplicationManager.getApplication().invokeLater {
                val editor = FileEditorManager.getInstance(project).selectedTextEditor ?: return@invokeLater
                val vFile = FileDocumentManager.getInstance().getFile(editor.document)
                if (isZplFile(vFile)) {
                    doRender(editor.document.text)
                }
            }
        }

        // 1. Initial render when webview finishes loading
        browser.jbCefClient.addLoadHandler(object : CefLoadHandlerAdapter() {
            override fun onLoadEnd(browser: CefBrowser?, frame: CefFrame?, httpStatusCode: Int) {
                if (frame?.isMain == true) {
                    renderActiveEditor()
                }
            }
        }, browser.cefBrowser)

        // 2. Active editor switch listener
        project.messageBus.connect(toolWindow.disposable).subscribe(
            FileEditorManagerListener.FILE_EDITOR_MANAGER,
            object : FileEditorManagerListener {
                override fun selectionChanged(event: FileEditorManagerEvent) {
                    val vFile = event.newFile
                    if (isZplFile(vFile)) {
                        renderActiveEditor()
                    }
                }
            }
        )

        // 3. Save listener (runs on onSave)
        project.messageBus.connect(toolWindow.disposable).subscribe(
            FileDocumentManagerListener.TOPIC,
            object : FileDocumentManagerListener {
                override fun beforeDocumentSaving(document: Document) {
                    val vFile = FileDocumentManager.getInstance().getFile(document) ?: return
                    if (isZplFile(vFile)) {
                        doRender(document.text)
                    }
                }
            }
        )

        // 4. Real-time typing listener (runs on onType with debounce)
        val alarm = Alarm(Alarm.ThreadToUse.POOLED_THREAD, toolWindow.disposable)
        EditorFactory.getInstance().eventMulticaster.addDocumentListener(object : DocumentListener {
            override fun documentChanged(event: DocumentEvent) {
                val settings = ZplflowSettings.getInstance().state
                if (settings.renderMode != "onType") return

                val vFile = FileDocumentManager.getInstance().getFile(event.document) ?: return
                if (!isZplFile(vFile)) return

                alarm.cancelAllRequests()
                alarm.addRequest({
                    doRender(event.document.text)
                }, settings.debounceMs)
            }
        }, toolWindow.disposable)

        // 5. Title bar action: Refresh
        val refreshAction = object : DumbAwareAction("Refresh Preview", "Re-render the current ZPL label", null) {
            override fun actionPerformed(e: AnActionEvent) {
                renderActiveEditor()
            }
        }
        toolWindow.setTitleActions(listOf(refreshAction))
    }
}
