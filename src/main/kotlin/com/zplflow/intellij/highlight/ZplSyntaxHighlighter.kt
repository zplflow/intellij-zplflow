package com.zplflow.intellij.highlight

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.tree.IElementType

class ZplSyntaxHighlighter : SyntaxHighlighterBase() {
    companion object {
        val PREFIX = createTextAttributesKey("ZPL_PREFIX", DefaultLanguageHighlighterColors.OPERATION_SIGN)
        val LABEL_BOUND = createTextAttributesKey("ZPL_LABEL_BOUND", DefaultLanguageHighlighterColors.KEYWORD)
        val FIELD_DELIMITER = createTextAttributesKey("ZPL_FIELD_DELIMITER", DefaultLanguageHighlighterColors.KEYWORD)
        val COMMAND = createTextAttributesKey("ZPL_COMMAND", DefaultLanguageHighlighterColors.FUNCTION_DECLARATION)
        val NUMBER = createTextAttributesKey("ZPL_NUMBER", DefaultLanguageHighlighterColors.NUMBER)
        val COMMA = createTextAttributesKey("ZPL_COMMA", DefaultLanguageHighlighterColors.COMMA)
        val FIELD_DATA = createTextAttributesKey("ZPL_FIELD_DATA", DefaultLanguageHighlighterColors.STRING)
        val PARAM_TEXT = createTextAttributesKey("ZPL_PARAM_TEXT", DefaultLanguageHighlighterColors.PARAMETER)
        val COMMENT = createTextAttributesKey("ZPL_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT)

        private val PREFIX_KEYS = arrayOf(PREFIX)
        private val LABEL_BOUND_KEYS = arrayOf(LABEL_BOUND)
        private val FIELD_DELIMITER_KEYS = arrayOf(FIELD_DELIMITER)
        private val COMMAND_KEYS = arrayOf(COMMAND)
        private val NUMBER_KEYS = arrayOf(NUMBER)
        private val COMMA_KEYS = arrayOf(COMMA)
        private val FIELD_DATA_KEYS = arrayOf(FIELD_DATA)
        private val PARAM_TEXT_KEYS = arrayOf(PARAM_TEXT)
        private val COMMENT_KEYS = arrayOf(COMMENT)
        private val EMPTY_KEYS = emptyArray<TextAttributesKey>()
    }

    override fun getHighlightingLexer(): Lexer = ZplLexer()

    override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> = when (tokenType) {
        ZplTypes.PREFIX -> PREFIX_KEYS
        ZplTypes.LABEL_BOUND -> LABEL_BOUND_KEYS
        ZplTypes.FIELD_DELIMITER -> FIELD_DELIMITER_KEYS
        ZplTypes.COMMAND -> COMMAND_KEYS
        ZplTypes.NUMBER -> NUMBER_KEYS
        ZplTypes.COMMA -> COMMA_KEYS
        ZplTypes.FIELD_DATA -> FIELD_DATA_KEYS
        ZplTypes.PARAM_TEXT -> PARAM_TEXT_KEYS
        ZplTypes.COMMENT -> COMMENT_KEYS
        else -> EMPTY_KEYS
    }
}
