package com.ziondev.qtui.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@State(
    name = "QtUiCompilerSettings",
    storages = [Storage("QtUiCompilerSettings.xml")]
)
class QtUiCompilerSettings : PersistentStateComponent<QtUiCompilerSettings.State> {
    
    data class State(
        var virtualEnvironmentPath: String = ".venv",
        var uicPath: String = "",
        var uiFilePattern: String = "",
        var outputPath: String = "",
        var autoCompileEnabled: Boolean = true,
        var useRelativePaths: Boolean = true
    )
    
    private var myState = State()
    
    override fun getState(): State = myState
    
    override fun loadState(state: State) {
        myState = state
    }
    
    companion object {
        fun getInstance(): QtUiCompilerSettings {
            return ApplicationManager.getApplication().getService(QtUiCompilerSettings::class.java)
        }
    }
}
