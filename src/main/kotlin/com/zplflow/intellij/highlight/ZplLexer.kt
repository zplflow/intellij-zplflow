package com.zplflow.intellij.highlight

import com.intellij.lexer.LexerBase
import com.intellij.psi.tree.IElementType

class ZplLexer : LexerBase() {
    private var buffer: CharSequence = ""
    private var bufferStart: Int = 0
    private var bufferEnd: Int = 0
    private var tokenStart: Int = 0
    private var tokenEnd: Int = 0
    private var tokenType: IElementType? = null
    private var state: Int = 0 // 0 = NORMAL, 1 = IN_FIELD_DATA, 2 = IN_COMMENT

    override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
        this.buffer = buffer
        this.bufferStart = startOffset
        this.bufferEnd = endOffset
        this.tokenStart = startOffset
        this.tokenEnd = startOffset
        this.state = initialState
        advance()
    }

    override fun getState(): Int = state
    override fun getTokenType(): IElementType? = tokenType
    override fun getTokenStart(): Int = tokenStart
    override fun getTokenEnd(): Int = tokenEnd
    override fun getBufferSequence(): CharSequence = buffer
    override fun getBufferEnd(): Int = bufferEnd

    override fun advance() {
        if (tokenEnd >= bufferEnd) {
            tokenType = null
            tokenStart = bufferEnd
            return
        }

        tokenStart = tokenEnd
        var pos = tokenStart

        // 1. In Comment mode (^FX...)
        if (state == 2) {
            while (pos < bufferEnd && buffer[pos] != '^' && buffer[pos] != '~' && buffer[pos] != '\n' && buffer[pos] != '\r') {
                pos++
            }
            if (pos == tokenStart) {
                state = 0
            } else {
                tokenEnd = pos
                tokenType = ZplTypes.COMMENT
                if (pos < bufferEnd && (buffer[pos] == '\n' || buffer[pos] == '\r')) {
                    state = 0
                }
                return
            }
        }

        // 2. In Field Data mode (^FD... or ^FV...)
        if (state == 1) {
            while (pos < bufferEnd && buffer[pos] != '^' && buffer[pos] != '~') {
                pos++
            }
            if (pos == tokenStart) {
                state = 0
            } else {
                tokenEnd = pos
                tokenType = ZplTypes.FIELD_DATA
                state = 0
                return
            }
        }

        val curr = buffer[pos]

        // 3. Whitespace
        if (curr.isWhitespace()) {
            while (pos < bufferEnd && buffer[pos].isWhitespace()) {
                pos++
            }
            tokenEnd = pos
            tokenType = ZplTypes.WHITESPACE
            return
        }

        // 4. Command prefix ^ or ~
        if (curr == '^' || curr == '~') {
            if (pos + 1 >= bufferEnd) {
                tokenEnd = pos + 1
                tokenType = ZplTypes.PREFIX
                return
            }

            val nextChar = buffer[pos + 1]
            if (!nextChar.isLetter()) {
                tokenEnd = pos + 1
                tokenType = ZplTypes.PREFIX
                return
            }

            val remainingLen = bufferEnd - (pos + 1)
            val sub = if (remainingLen >= 2) {
                buffer.substring(pos + 1, pos + 3).uppercase()
            } else {
                buffer.substring(pos + 1, bufferEnd).uppercase()
            }

            when {
                sub == "XA" || sub == "XZ" -> {
                    tokenEnd = pos + 1 + 2
                    tokenType = ZplTypes.LABEL_BOUND
                    return
                }
                sub == "FS" -> {
                    tokenEnd = pos + 1 + 2
                    tokenType = ZplTypes.FIELD_DELIMITER
                    return
                }
                sub == "FX" -> {
                    tokenEnd = pos + 1 + 2
                    tokenType = ZplTypes.COMMAND
                    state = 2 // enter comment
                    return
                }
                sub == "FD" || sub == "FV" -> {
                    tokenEnd = pos + 1 + 2
                    tokenType = ZplTypes.COMMAND
                    state = 1 // enter field data
                    return
                }
                sub.startsWith("A") -> {
                    var end = pos + 2 // ^A
                    if (end < bufferEnd && (buffer[end].isLetterOrDigit() || buffer[end] == '@')) {
                        end++
                    }
                    if (end < bufferEnd && buffer[end].uppercaseChar() in listOf('N', 'R', 'I', 'B')) {
                        end++
                    }
                    tokenEnd = end
                    tokenType = ZplTypes.COMMAND
                    return
                }
                sub.length == 2 && sub[0].isLetter() && (sub[1].isLetterOrDigit()) -> {
                    tokenEnd = pos + 1 + 2
                    tokenType = ZplTypes.COMMAND
                    return
                }
                else -> {
                    tokenEnd = pos + 2
                    tokenType = ZplTypes.COMMAND
                    return
                }
            }
        }

        // 5. Comma delimiter
        if (curr == ',') {
            tokenEnd = pos + 1
            tokenType = ZplTypes.COMMA
            return
        }

        // 6. Numbers
        if (curr.isDigit() || (curr == '-' && pos + 1 < bufferEnd && buffer[pos + 1].isDigit())) {
            pos++
            while (pos < bufferEnd && buffer[pos].isDigit()) {
                pos++
            }
            if (pos < bufferEnd && buffer[pos] == '.' && pos + 1 < bufferEnd && buffer[pos + 1].isDigit()) {
                pos++
                while (pos < bufferEnd && buffer[pos].isDigit()) {
                    pos++
                }
            }
            tokenEnd = pos
            tokenType = ZplTypes.NUMBER
            return
        }

        // 7. General parameter text / identifier
        while (pos < bufferEnd && buffer[pos] != ',' && buffer[pos] != '^' && buffer[pos] != '~' && !buffer[pos].isWhitespace()) {
            pos++
        }
        tokenEnd = pos
        tokenType = ZplTypes.PARAM_TEXT
    }
}
