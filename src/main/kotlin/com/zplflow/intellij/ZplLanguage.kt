package com.zplflow.intellij

import com.intellij.lang.Language

object ZplLanguage : Language("ZPL") {
    private fun readResolve(): Any = ZplLanguage
    override fun getDisplayName(): String = "ZPL (Zebra Programming Language)"
}
