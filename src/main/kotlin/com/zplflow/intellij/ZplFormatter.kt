package com.zplflow.intellij

object ZplFormatter {
    fun format(text: String, indent: String = "  "): String {
        if (text.isBlank()) return text

        val tokens = ArrayList<String>()
        var pos = 0
        val len = text.length

        while (pos < len) {
            val ch = text[pos]
            if (ch == '^' || ch == '~') {
                val cmdStart = pos
                val cmdPrefix = ch
                pos++

                // Read command code (letters / digits, up to 2-3 chars or known commands)
                val codeBuilder = StringBuilder()
                while (pos < len && (text[pos].isLetter() || text[pos].isDigit()) && codeBuilder.length < 2) {
                    codeBuilder.append(text[pos])
                    pos++
                }
                val cmdCode = codeBuilder.toString().uppercase()
                val fullCmd = "$cmdPrefix$cmdCode"

                // Commands with free text until ^FS or ^XZ: ^FD (Field Data), ^FX (Comment), ^HV, ~DG
                if (fullCmd == "^FD" || fullCmd == "^FX" || fullCmd == "^HV") {
                    val endFs = text.indexOf("^FS", pos)
                    val endXz = text.indexOf("^XZ", pos)
                    val endIdx = when {
                        endFs != -1 && endXz != -1 -> minOf(endFs, endXz)
                        endFs != -1 -> endFs
                        else -> endXz
                    }
                    if (endIdx != -1) {
                        tokens.add(text.substring(cmdStart, endIdx).trim())
                        pos = endIdx
                    } else {
                        tokens.add(text.substring(cmdStart).trim())
                        pos = len
                    }
                } else {
                    // Read parameters until next ^ or ~ or newline
                    val paramStart = pos
                    while (pos < len && text[pos] != '^' && text[pos] != '~' && text[pos] != '\n' && text[pos] != '\r') {
                        pos++
                    }
                    val paramStr = text.substring(paramStart, pos).trim()
                    tokens.add("$cmdPrefix$cmdCode$paramStr")
                }
            } else {
                pos++
            }
        }

        if (tokens.isEmpty()) return text

        val result = ArrayList<String>()
        var inLabel = false
        val currentField = ArrayList<String>()

        fun flushField() {
            if (currentField.isNotEmpty()) {
                val prefix = if (inLabel) indent else ""
                result.add(prefix + currentField.joinToString(""))
                currentField.clear()
            }
        }

        for (token in tokens) {
            val t = token.trim()
            if (t.isEmpty()) continue

            when {
                t.startsWith("^XA") -> {
                    flushField()
                    inLabel = true
                    result.add("^XA")
                }
                t.startsWith("^XZ") -> {
                    flushField()
                    result.add("^XZ")
                    inLabel = false
                }
                t.startsWith("^FO") || t.startsWith("^FT") || t.startsWith("^FN") -> {
                    flushField()
                    currentField.add(t)
                }
                t.startsWith("^FS") -> {
                    currentField.add("^FS")
                    flushField()
                }
                else -> {
                    if (currentField.isNotEmpty()) {
                        currentField.add(t)
                    } else {
                        val prefix = if (inLabel) indent else ""
                        result.add(prefix + t)
                    }
                }
            }
        }
        flushField()

        return result.joinToString("\n")
    }
}
