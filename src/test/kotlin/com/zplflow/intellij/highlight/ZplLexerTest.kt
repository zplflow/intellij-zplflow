package com.zplflow.intellij.highlight

import kotlin.test.Test
import kotlin.test.assertEquals

class ZplLexerTest {

    private fun tokenize(text: String): List<Pair<String, String>> {
        val lexer = ZplLexer()
        lexer.start(text, 0, text.length, 0)
        val tokens = mutableListOf<Pair<String, String>>()
        while (lexer.tokenType != null) {
            val tokenText = text.substring(lexer.tokenStart, lexer.tokenEnd)
            tokens.add(tokenText to lexer.tokenType.toString())
            lexer.advance()
        }
        return tokens
    }

    @Test
    fun testLabelBounds() {
        val tokens = tokenize("^XA\n^XZ")
        assertEquals(
            listOf(
                "^XA" to "ZPL_LABEL_BOUND",
                "\n" to "ZPL_WHITESPACE",
                "^XZ" to "ZPL_LABEL_BOUND"
            ),
            tokens
        )
    }

    @Test
    fun testCommandAndNumericParameters() {
        val tokens = tokenize("^FO50,50")
        assertEquals(
            listOf(
                "^FO" to "ZPL_COMMAND",
                "50" to "ZPL_NUMBER",
                "," to "ZPL_COMMA",
                "50" to "ZPL_NUMBER"
            ),
            tokens
        )
    }

    @Test
    fun testFontAndFieldData() {
        val tokens = tokenize("^A0N,40,40^FDHello World^FS")
        assertEquals(
            listOf(
                "^A0N" to "ZPL_COMMAND",
                "," to "ZPL_COMMA",
                "40" to "ZPL_NUMBER",
                "," to "ZPL_COMMA",
                "40" to "ZPL_NUMBER",
                "^FD" to "ZPL_COMMAND",
                "Hello World" to "ZPL_FIELD_DATA",
                "^FS" to "ZPL_FIELD_DELIMITER"
            ),
            tokens
        )
    }

    @Test
    fun testBarcodeAndOrientation() {
        val tokens = tokenize("^BY2,3,50^BCN,100,Y,N,N")
        assertEquals(
            listOf(
                "^BY" to "ZPL_COMMAND",
                "2" to "ZPL_NUMBER",
                "," to "ZPL_COMMA",
                "3" to "ZPL_NUMBER",
                "," to "ZPL_COMMA",
                "50" to "ZPL_NUMBER",
                "^BC" to "ZPL_COMMAND",
                "N" to "ZPL_PARAM_TEXT",
                "," to "ZPL_COMMA",
                "100" to "ZPL_NUMBER",
                "," to "ZPL_COMMA",
                "Y" to "ZPL_PARAM_TEXT",
                "," to "ZPL_COMMA",
                "N" to "ZPL_PARAM_TEXT",
                "," to "ZPL_COMMA",
                "N" to "ZPL_PARAM_TEXT"
            ),
            tokens
        )
    }

    @Test
    fun testComments() {
        val tokens = tokenize("^FX Shipping Label Sample\n^PW800")
        assertEquals(
            listOf(
                "^FX" to "ZPL_COMMAND",
                " Shipping Label Sample" to "ZPL_COMMENT",
                "\n" to "ZPL_WHITESPACE",
                "^PW" to "ZPL_COMMAND",
                "800" to "ZPL_NUMBER"
            ),
            tokens
        )
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val t = ZplLexerTest()
            t.testLabelBounds()
            t.testCommandAndNumericParameters()
            t.testFontAndFieldData()
            t.testBarcodeAndOrientation()
            t.testComments()
            println("ALL ZPL LEXER TESTS PASSED SUCCESSFULLY!")
        }
    }
}
