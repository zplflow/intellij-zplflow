package com.zplflow.intellij.highlight

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import javax.swing.Icon

class ZplColorSettingsPage : ColorSettingsPage {
    companion object {
        private val DESCRIPTORS = arrayOf(
            AttributesDescriptor("Command Prefix (^ or ~)", ZplSyntaxHighlighter.PREFIX),
            AttributesDescriptor("Label Bounds (^XA, ^XZ)", ZplSyntaxHighlighter.LABEL_BOUND),
            AttributesDescriptor("Commands (^FO, ^BY, ^A0, etc.)", ZplSyntaxHighlighter.COMMAND),
            AttributesDescriptor("Field Delimiter (^FS)", ZplSyntaxHighlighter.FIELD_DELIMITER),
            AttributesDescriptor("Field Data (^FD content)", ZplSyntaxHighlighter.FIELD_DATA),
            AttributesDescriptor("Numbers", ZplSyntaxHighlighter.NUMBER),
            AttributesDescriptor("Commas", ZplSyntaxHighlighter.COMMA),
            AttributesDescriptor("Parameters", ZplSyntaxHighlighter.PARAM_TEXT),
            AttributesDescriptor("Comments (^FX)", ZplSyntaxHighlighter.COMMENT)
        )
    }

    override fun getIcon(): Icon? = null
    override fun getHighlighter(): SyntaxHighlighter = ZplSyntaxHighlighter()
    override fun getDemoText(): String = """
^XA
^FX Shipping Label Sample
^PW800
^LL1200
^FO50,50^A0N,40,40^FDZplFlow Logistics^FS
^FO50,120^GB700,2,2^FS
^FO50,150^BCN,100,Y,N,N^FD1234567890^FS
^FO50,300^A0N,30,30^FDPackage 1 of 1^FS
^XZ
    """.trimIndent()

    override fun getAdditionalHighlightingTagToDescriptorMap(): Map<String, TextAttributesKey>? = null
    override fun getAttributeDescriptors(): Array<AttributesDescriptor> = DESCRIPTORS
    override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY
    override fun getDisplayName(): String = "ZPL"
}
