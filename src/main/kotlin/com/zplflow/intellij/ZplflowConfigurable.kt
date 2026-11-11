package com.zplflow.intellij

import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.util.ui.FormBuilder
import javax.swing.*

class ZplflowConfigurable : SearchableConfigurable {
    private val apiKeyField = JPasswordField(30)
    private val apiUrlField = JTextField(30)
    private val dpiCombo = JComboBox(arrayOf(152, 203, 300, 600))
    private val widthMmField = JTextField(10)
    private val heightMmField = JTextField(10)
    private val lspEnabledBox = JCheckBox("Enable ZPL Language Server (LSP)")
    private val lspPathField = JTextField(30)

    private var myPanel: JPanel? = null

    override fun getId(): String = "com.zplflow.intellij.ZplflowConfigurable"
    override fun getDisplayName(): String = "zplflow"

    override fun createComponent(): JComponent {
        val panel = FormBuilder.createFormBuilder()
            .addLabeledComponent("API Key:", apiKeyField)
            .addTooltip("Your zplflow API key (e.g. zpl_live_xxx) with pipelines:apply scope.")
            .addLabeledComponent("API URL:", apiUrlField)
            .addTooltip("Base URL for zplflow API (defaults to https://api.zplflow.com).")
            .addLabeledComponent("Default DPI:", dpiCombo)
            .addLabeledComponent("Label Width (mm):", widthMmField)
            .addLabeledComponent("Label Height (mm):", heightMmField)
            .addComponent(lspEnabledBox)
            .addLabeledComponent("Custom LSP Binary Path:", lspPathField)
            .addTooltip("Leave empty to use bundled zplflow-lsp binary.")
            .addComponentFillVertically(JPanel(), 0)
            .panel

        myPanel = panel
        reset()
        return panel
    }

    override fun isModified(): Boolean {
        val s = ZplflowSettings.getInstance().state
        return String(apiKeyField.password) != s.apiKey ||
                apiUrlField.text != s.apiUrl ||
                dpiCombo.selectedItem != s.dpi ||
                widthMmField.text.toDoubleOrNull() != s.widthMm ||
                heightMmField.text.toDoubleOrNull() != s.heightMm ||
                lspEnabledBox.isSelected != s.lspEnabled ||
                lspPathField.text != s.lspPath
    }

    override fun apply() {
        val s = ZplflowSettings.getInstance().state
        s.apiKey = String(apiKeyField.password).trim()
        s.apiUrl = apiUrlField.text.trim().ifEmpty { "https://api.zplflow.com" }
        s.dpi = dpiCombo.selectedItem as? Int ?: 203
        s.widthMm = widthMmField.text.toDoubleOrNull() ?: 101.6
        s.heightMm = heightMmField.text.toDoubleOrNull() ?: 152.4
        s.lspEnabled = lspEnabledBox.isSelected
        s.lspPath = lspPathField.text.trim()
    }

    override fun reset() {
        val s = ZplflowSettings.getInstance().state
        apiKeyField.text = s.apiKey
        apiUrlField.text = s.apiUrl
        dpiCombo.selectedItem = s.dpi
        widthMmField.text = s.widthMm.toString()
        heightMmField.text = s.heightMm.toString()
        lspEnabledBox.isSelected = s.lspEnabled
        lspPathField.text = s.lspPath
    }

    override fun disposeUIResources() {
        myPanel = null
    }
}
