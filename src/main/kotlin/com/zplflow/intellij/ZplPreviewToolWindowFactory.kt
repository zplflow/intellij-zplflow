package com.zplflow.intellij

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
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
import com.intellij.ui.jcef.JBCefBrowserBase
import com.intellij.ui.jcef.JBCefJSQuery
import com.intellij.util.Alarm
import org.cef.CefSettings
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefDisplayHandlerAdapter
import org.cef.handler.CefLoadHandlerAdapter
import java.awt.BorderLayout
import javax.swing.JLabel
import javax.swing.JPanel

class ZplPreviewToolWindowFactory : ToolWindowFactory, DumbAware {
    companion object {
        private val LOG = Logger.getInstance(ZplPreviewToolWindowFactory::class.java)
    }

    private val client = ZplflowClient()

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        LOG.info("ZplFlow: Initializing Preview Tool Window...")

        val panel = JPanel(BorderLayout())
        if (!JBCefApp.isSupported()) {
            LOG.warn("ZplFlow: JCEF is not supported in this runtime environment.")
            panel.add(JLabel("JCEF is required for ZplFlow Live Preview."), BorderLayout.CENTER)
            val content = ContentFactory.getInstance().createContent(panel, "", false)
            toolWindow.contentManager.addContent(content)
            return
        }

        val settings = ZplflowSettings.getInstance().state
        var currentWidthMm = settings.widthMm
        var currentHeightMm = settings.heightMm
        var currentDpi = settings.dpi
        var currentPage = 0

        val browser = JBCefBrowser()
        val html = ZplPreviewToolWindowFactory::class.java
            .getResource("/webview/preview.html")
            ?.readText() ?: "<html><body>Missing preview.html</body></html>"
        browser.loadHTML(html)
        panel.add(browser.component, BorderLayout.CENTER)

        var lastActionPayload = ""
        var lastActionTime = 0L

        fun doRender(zplText: String) {
            if (zplText.isBlank()) {
                LOG.info("ZplFlow: doRender skipped (text is blank)")
                return
            }
            val apiKey = settings.apiKey.ifBlank {
                System.getenv("ZPLFLOW_API_KEY") ?: System.getenv("ZPL_API_KEY") ?: ""
            }

            LOG.info("ZplFlow: starting preview render (url=${settings.apiUrl}, dpi=$currentDpi, W=${currentWidthMm}mm, H=${currentHeightMm}mm, page=$currentPage, apiKeyConfigured=${apiKey.isNotBlank()}, zplLen=${zplText.length})")

            ApplicationManager.getApplication().executeOnPooledThread {
                try {
                    val res = client.renderPreview(
                        settings.apiUrl,
                        apiKey,
                        PreviewRequestOptions(
                            zpl = zplText,
                            dpi = currentDpi,
                            widthMm = currentWidthMm,
                            heightMm = currentHeightMm,
                            page = currentPage
                        )
                    )
                    LOG.info("ZplFlow: preview rendered successfully (fromCache=${res.fromCache}, remainingQuota=${res.rateLimitRemaining}, pages=${res.pageCount}, page=${res.currentPage})")
                    val quotaVal = res.rateLimitRemaining?.toString() ?: "null"
                    val js = """(function() {
                        var p = {
                            "pngBase64":"${res.pngBase64}",
                            "dpi":$currentDpi,
                            "widthMm":$currentWidthMm,
                            "heightMm":$currentHeightMm,
                            "currentPage":${res.currentPage},
                            "pageCount":${res.pageCount},
                            "fromCache":${res.fromCache},
                            "rateLimitRemaining":$quotaVal
                        };
                        if (window.updateZplPreview) { window.updateZplPreview(p); } else { window.__pendingPayload = p; }
                    })();"""
                    browser.cefBrowser.executeJavaScript(js, "", 0)
                } catch (e: Exception) {
                    LOG.warn("ZplFlow: preview render error: ${e.message}", e)
                    val safeMsg = (e.message ?: "Preview failed")
                        .replace("\\", "\\\\")
                        .replace("\"", "\\\"")
                        .replace("\n", " ")
                    val js = """(function() {
                        var p = {"error":"$safeMsg"};
                        if (window.updateZplPreview) { window.updateZplPreview(p); } else { window.__pendingPayload = p; }
                    })();"""
                    browser.cefBrowser.executeJavaScript(js, "", 0)
                }
            }
        }

        fun isZplFile(vFile: VirtualFile?, sampleText: String? = null): Boolean {
            if (vFile == null && sampleText == null) return false
            val ext = vFile?.extension?.lowercase() ?: ""
            if (ext in listOf("zpl", "prn", "label")) return true
            if (vFile?.fileType == ZplFileType || vFile?.fileType?.name == "ZPL File") return true
            if (sampleText != null && sampleText.trim().startsWith("^XA")) return true
            return false
        }

        fun renderActiveEditor() {
            ApplicationManager.getApplication().invokeLater {
                val editor = project.getService(FileEditorManager::class.java)?.selectedTextEditor
                if (editor == null) {
                    LOG.info("ZplFlow: renderActiveEditor - no selectedTextEditor found")
                    return@invokeLater
                }
                val doc = editor.document
                val vFile = FileDocumentManager.getInstance().getFile(doc)
                val text = ApplicationManager.getApplication().runReadAction<String> { doc.text }
                if (isZplFile(vFile, text)) {
                    LOG.info("ZplFlow: active file matched ZPL: ${vFile?.name ?: "untitled"}")
                    doRender(text)
                } else {
                    LOG.info("ZplFlow: active file is not ZPL: ${vFile?.name ?: "untitled"} (ext=${vFile?.extension})")
                }
            }
        }

        fun handleWebviewAction(json: String) {
            val now = System.currentTimeMillis()
            if (json == lastActionPayload && (now - lastActionTime) < 150) {
                return
            }
            lastActionPayload = json
            lastActionTime = now

            try {
                if (json.contains("\"changeDimensions\"")) {
                    val wMatch = Regex(""""widthMm"\s*:\s*([0-9.]+)""").find(json)
                    val hMatch = Regex(""""heightMm"\s*:\s*([0-9.]+)""").find(json)
                    val dpiMatch = Regex(""""dpi"\s*:\s*(\d+)""").find(json)

                    if (wMatch != null) currentWidthMm = wMatch.groupValues[1].toDoubleOrNull() ?: currentWidthMm
                    if (hMatch != null) currentHeightMm = hMatch.groupValues[1].toDoubleOrNull() ?: currentHeightMm
                    if (dpiMatch != null) currentDpi = dpiMatch.groupValues[1].toIntOrNull() ?: currentDpi

                    settings.widthMm = currentWidthMm
                    settings.heightMm = currentHeightMm
                    settings.dpi = currentDpi

                    LOG.info("ZplFlow: Updated dimensions from UI toolbar: W=${currentWidthMm}mm, H=${currentHeightMm}mm, DPI=$currentDpi")
                    renderActiveEditor()
                } else if (json.contains("\"changePage\"")) {
                    val pageMatch = Regex(""""page"\s*:\s*(\d+)""").find(json)
                    if (pageMatch != null) {
                        currentPage = pageMatch.groupValues[1].toIntOrNull() ?: 0
                        LOG.info("ZplFlow: Changed page to $currentPage")
                        renderActiveEditor()
                    }
                }
            } catch (e: Exception) {
                LOG.warn("ZplFlow: Error parsing webview action: ${e.message}", e)
            }
        }

        // Bridge 1: Console message bridge
        browser.jbCefClient.addDisplayHandler(object : CefDisplayHandlerAdapter() {
            override fun onConsoleMessage(
                b: CefBrowser?,
                level: CefSettings.LogSeverity?,
                message: String?,
                source: String?,
                line: Int
            ): Boolean {
                if (message != null && message.startsWith("ZPLFLOW_ACTION:")) {
                    handleWebviewAction(message.removePrefix("ZPLFLOW_ACTION:"))
                    return true
                }
                LOG.info("ZplFlow WebView: [$level] $message ($source:$line)")
                return false
            }
        }, browser.cefBrowser)

        // Bridge 2: JBCefJSQuery (if supported by environment)
        try {
            val jsQuery = JBCefJSQuery.create(browser as JBCefBrowserBase)
            jsQuery.addHandler { json ->
                handleWebviewAction(json)
                JBCefJSQuery.Response(null)
            }
        } catch (e: Throwable) {
            LOG.info("ZplFlow: using console bridge for webview actions (${e.message})")
        }

        val content = ContentFactory.getInstance().createContent(panel, "", false)
        toolWindow.contentManager.addContent(content)

        // 1. Initial render when webview finishes loading
        browser.jbCefClient.addLoadHandler(object : CefLoadHandlerAdapter() {
            override fun onLoadEnd(b: CefBrowser?, frame: CefFrame?, httpStatusCode: Int) {
                if (frame?.isMain == true) {
                    LOG.info("ZplFlow: JCEF onLoadEnd triggered, rendering active editor")
                    val initJs = """(function() {
                        if (window.updateZplPreview) {
                            window.updateZplPreview({
                                "dpi": $currentDpi,
                                "widthMm": $currentWidthMm,
                                "heightMm": $currentHeightMm
                            });
                        }
                    })();"""
                    browser.cefBrowser.executeJavaScript(initJs, "", 0)
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
                    LOG.info("ZplFlow: editor selection changed to ${vFile?.name}")
                    if (isZplFile(vFile)) {
                        currentPage = 0
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
                    val text = ApplicationManager.getApplication().runReadAction<String> { document.text }
                    if (isZplFile(vFile, text)) {
                        LOG.info("ZplFlow: beforeDocumentSaving triggered for ${vFile.name}")
                        doRender(text)
                    }
                }
            }
        )

        // 4. Real-time typing listener (runs on onType with debounce)
        val alarm = Alarm(Alarm.ThreadToUse.POOLED_THREAD, toolWindow.disposable)
        EditorFactory.getInstance().eventMulticaster.addDocumentListener(object : DocumentListener {
            override fun documentChanged(event: DocumentEvent) {
                if (settings.renderMode != "onType") return

                val doc = event.document
                val vFile = FileDocumentManager.getInstance().getFile(doc)
                val text = ApplicationManager.getApplication().runReadAction<String> { doc.text }
                if (!isZplFile(vFile, text)) return

                alarm.cancelAllRequests()
                alarm.addRequest({
                    LOG.info("ZplFlow: onType debounced trigger (${settings.debounceMs}ms) for ${vFile?.name}")
                    doRender(text)
                }, settings.debounceMs)
            }
        }, toolWindow.disposable)

        // 5. Title bar action: Refresh
        val refreshAction = object : DumbAwareAction("Refresh Preview", "Re-render the current ZPL label", null) {
            override fun actionPerformed(e: AnActionEvent) {
                LOG.info("ZplFlow: manual Refresh Preview clicked")
                renderActiveEditor()
            }
        }
        toolWindow.setTitleActions(listOf(refreshAction))

        // Trigger immediate check in case editor is already open
        renderActiveEditor()
    }
}
