package com.zplflow.intellij

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileDocumentManagerListener
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.jcef.JBCefApp
import com.intellij.ui.jcef.JBCefBrowser
import javax.swing.JLabel
import javax.swing.JPanel
import java.awt.BorderLayout

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

        project.messageBus.connect(toolWindow.disposable).subscribe(
            FileDocumentManagerListener.TOPIC,
            object : FileDocumentManagerListener {
                override fun beforeDocumentSaving(document: Document) {
                    val vFile = FileDocumentManager.getInstance().getFile(document) ?: return
                    val ext = vFile.extension?.lowercase() ?: return
                    if (ext != "zpl" && ext != "prn" && ext != "label") return

                    val zplText = document.text
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
                            val js = """window.updateZplPreview({"pngBase64":"${res.pngBase64}","dpi":${settings.dpi},"currentPage":${res.currentPage},"pageCount":${res.pageCount},"fromCache":${res.fromCache}});"""
                            browser.cefBrowser.executeJavaScript(js, browser.cefBrowser.url, 0)
                        } catch (e: Exception) {
                            val safeMsg = (e.message ?: "Preview failed").replace("\"", "\\\"")
                            val js = """window.updateZplPreview({"error":"$safeMsg"});"""
                            browser.cefBrowser.executeJavaScript(js, browser.cefBrowser.url, 0)
                        }
                    }
                }
            }
        )
    }
}
