package com.ziondev.qtui.settings

import com.intellij.openapi.options.BoundConfigurable
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel

class QtUiCompilerConfigurable : BoundConfigurable("Qt UI Compiler") {
    
    private val settings = QtUiCompilerSettings.getInstance()

    override fun createPanel() = panel {
        row {
            checkBox("Enable auto-compilation")
                .bindSelected(settings.state::autoCompileEnabled)
        }
        
        group("Environment Settings") {
            row("Virtual Environment Path:") {
                textField()
                    .bindText(settings.state::virtualEnvironmentPath)
                    .comment("Default: .venv")
            }
            row("Custom UIC Path:") {
                textField()
                    .bindText(settings.state::uicPath)
                    .comment("Optional custom path to uic executable")
            }
            row {
                checkBox("Interpret VENV and UIC paths as relative to project root")
                    .bindSelected(settings.state::useRelativePaths)
            }
        }
        
        group("Compilation Settings") {
            row("UI Files Filter:") {
                textField()
                    .bindText(settings.state::uiFilePattern)
                    .comment("Glob pattern (e.g., **/ui/*.ui), default: *.ui")
            }
            row("Output Path/Pattern:") {
                textField()
                    .bindText(settings.state::outputPath)
                    .comment("$1 = original filename, default: same folder as .ui file")
            }
        }
    }
}