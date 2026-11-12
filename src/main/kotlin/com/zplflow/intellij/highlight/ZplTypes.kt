package com.zplflow.intellij.highlight

import com.intellij.psi.tree.IElementType
import com.zplflow.intellij.ZplLanguage

class ZplTokenType(debugName: String) : IElementType(debugName, ZplLanguage)

object ZplTypes {
    @JvmField val PREFIX = ZplTokenType("ZPL_PREFIX")
    @JvmField val LABEL_BOUND = ZplTokenType("ZPL_LABEL_BOUND")
    @JvmField val FIELD_DELIMITER = ZplTokenType("ZPL_FIELD_DELIMITER")
    @JvmField val COMMAND = ZplTokenType("ZPL_COMMAND")
    @JvmField val NUMBER = ZplTokenType("ZPL_NUMBER")
    @JvmField val COMMA = ZplTokenType("ZPL_COMMA")
    @JvmField val FIELD_DATA = ZplTokenType("ZPL_FIELD_DATA")
    @JvmField val PARAM_TEXT = ZplTokenType("ZPL_PARAM_TEXT")
    @JvmField val COMMENT = ZplTokenType("ZPL_COMMENT")
    @JvmField val WHITESPACE = ZplTokenType("ZPL_WHITESPACE")
}
