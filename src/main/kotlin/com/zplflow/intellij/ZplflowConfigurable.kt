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
    private val renderModeCombo = JComboBox(arrayOf("onSave", "onType"))
    private val debounceMsField = JTextField(10)
    private val lspEnabledBox = JCheckBox("Enable ZPL Language Server (LSP)")
    private val lspPathField = JTextField(30)

    private var myPanel: JPanel? = null

    override fun getId(): String = "com.zplflow.intellij.ZplflowConfigurable"
    override fun getDisplayName(): String = "ZplFlow"

    override fun createComponent(): JComponent {
        val panel = FormBuilder.createFormBuilder()
            .addLabeledComponent("API Key:", apiKeyField)
            .addTooltip("Your ZplFlow API key (e.g. zpl_live_xxx) with pipelines:apply scope.")
            .addLabeledComponent("API URL:", apiUrlField)
            .addTooltip("Base URL for ZplFlow API (defaults to https://api.zplflow.com).")
            .addLabeledComponent("Default DPI:", dpiCombo)
            .addLabeledComponent("Label Width (mm):", widthMmField)
            .addLabeledComponent("Label Height (mm):", heightMmField)
            .addLabeledComponent("Preview Mode:", renderModeCombo)
            .addTooltip("onSave (renders when saving) or onType (debounced real-time rendering).")
            .addLabeledComponent("Debounce Delay (ms):", debounceMsField)
            .addTooltip("Delay in ms before triggering real-time rendering when in onType mode.")
            .addComponent(lspEnabledBox)
            .addLabeledComponent("Custom LSP Binary Path:", lspPathField)
            .addTooltip("Leave empty to use bundled ZplFlow LSP binary.")
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
                renderModeCombo.selectedItem != s.renderMode ||
                debounceMsField.text.toIntOrNull() != s.debounceMs ||
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
        s.renderMode = renderModeCombo.selectedItem as? String ?: "onSave"
        s.debounceMs = debounceMsField.text.toIntOrNull() ?: 450
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
        renderModeCombo.selectedItem = s.renderMode
        debounceMsField.text = s.debounceMs.toString()
        lspEnabledBox.isSelected = s.lspEnabled
        lspPathField.text = s.lspPath
    }

    override fun disposeUIResources() {
        myPanel = null
    }
}
