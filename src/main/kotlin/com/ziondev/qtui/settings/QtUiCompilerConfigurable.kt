package com.ziondev.qtui.settings

import com.intellij.openapi.options.BoundConfigurable
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import com.ziondev.qtui.QtUiCompilerBundle

class QtUiCompilerConfigurable : BoundConfigurable(QtUiCompilerBundle.message("settings.displayName")) {
    
    private val settings = QtUiCompilerSettings.getInstance()

    override fun createPanel() = panel {
        row {
            checkBox(QtUiCompilerBundle.message("settings.autoCompile"))
                .bindSelected(settings.state::autoCompileEnabled)
        }
        
        group(QtUiCompilerBundle.message("settings.group.environment")) {
            row(QtUiCompilerBundle.message("settings.venvPath")) {
                textField()
                    .bindText(settings.state::virtualEnvironmentPath)
                    .comment(QtUiCompilerBundle.message("settings.venvPath.comment"))
            }
            row(QtUiCompilerBundle.message("settings.uicPath")) {
                textField()
                    .bindText(settings.state::uicPath)
                    .comment(QtUiCompilerBundle.message("settings.uicPath.comment"))
            }
            row {
                checkBox(QtUiCompilerBundle.message("settings.useRelativePaths"))
                    .bindSelected(settings.state::useRelativePaths)
            }
        }
        
        group(QtUiCompilerBundle.message("settings.group.compilation")) {
            row(QtUiCompilerBundle.message("settings.uiFilePattern")) {
                textField()
                    .bindText(settings.state::uiFilePattern)
                    .comment(QtUiCompilerBundle.message("settings.uiFilePattern.comment"))
            }
            row(QtUiCompilerBundle.message("settings.outputPath")) {
                textField()
                    .bindText(settings.state::outputPath)
                    .comment(QtUiCompilerBundle.message("settings.outputPath.comment"))
            }
        }
    }
}