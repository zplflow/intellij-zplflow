package com.zplflow.intellij

import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

object ZplFileType : LanguageFileType(ZplLanguage) {
    override fun getName(): String = "ZPL File"
    override fun getDescription(): String = "Zebra Programming Language thermal label file"
    override fun getDefaultExtension(): String = "zpl"
    override fun getIcon(): Icon? = null
}
