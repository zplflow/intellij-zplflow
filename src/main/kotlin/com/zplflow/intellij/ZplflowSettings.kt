package com.zplflow.intellij

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@Service(Service.Level.APP)
@State(
    name = "com.zplflow.intellij.ZplflowSettings",
    storages = [Storage("zplflow.xml")]
)
class ZplflowSettings : PersistentStateComponent<ZplflowSettings.State> {
    data class State(
        var apiKey: String = "",
        var apiUrl: String = "https://api.zplflow.io",
        var dpi: Int = 203,
        var widthMm: Double = 101.6,
        var heightMm: Double = 152.4,
        var renderMode: String = "onSave",
        var debounceMs: Int = 450,
        var lspEnabled: Boolean = true,
        var lspPath: String = ""
    )

    private var myState = State()

    override fun getState(): State = myState

    override fun loadState(state: State) {
        myState = state
    }

    companion object {
        fun getInstance(): ZplflowSettings =
            ApplicationManager.getApplication().getService(ZplflowSettings::class.java)
    }
}
