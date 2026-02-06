package com.ziondev.qtui.settings

import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.project.ProjectManager
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.Row
import com.ziondev.qtui.QtUiCompilerBundle
import com.ziondev.qtui.services.UiCompilerService
import com.ziondev.qtui.utils.SettingsValidator
import javax.swing.JTextField

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
                    .validationOnInput { textField ->
                        val result = SettingsValidator.validateVenvPath(
                            textField.text,
                            getCurrentProjectPath(),
                            settings.state.useRelativePaths
                        )
                        if (!result.isValid) error(result.errorMessage ?: "Invalid path") else null
                    }
                    .comment(QtUiCompilerBundle.message("settings.venvPath.comment"))
            }
            row(QtUiCompilerBundle.message("settings.uicPath")) {
                textField()
                    .bindText(settings.state::uicPath)
                    .validationOnInput { textField ->
                        val result = SettingsValidator.validateUicPath(
                            textField.text,
                            getCurrentProjectPath(),
                            settings.state.useRelativePaths
                        )
                        if (!result.isValid) error(result.errorMessage ?: "Invalid path") else null
                    }
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
                    .validationOnInput { textField ->
                        val result = SettingsValidator.validateUiFilePattern(textField.text)
                        if (!result.isValid) error(result.errorMessage ?: "Invalid pattern") else null
                    }
                    .comment(QtUiCompilerBundle.message("settings.uiFilePattern.comment"))
            }
            row(QtUiCompilerBundle.message("settings.outputPath")) {
                textField()
                    .bindText(settings.state::outputPath)
                    .validationOnInput { textField ->
                        val result = SettingsValidator.validateOutputPath(textField.text)
                        if (!result.isValid) error(result.errorMessage ?: "Invalid path") else null
                    }
                    .comment(QtUiCompilerBundle.message("settings.outputPath.comment"))
            }
        }
    }

    override fun apply() {
        super.apply()
        // Invalidate UIC executable cache when settings change
        invalidateUicCache()
    }

    private fun getCurrentProjectPath(): String? {
        val openProjects = ProjectManager.getInstance().openProjects
        return openProjects.firstOrNull()?.basePath
    }

    private fun invalidateUicCache() {
        val openProjects = ProjectManager.getInstance().openProjects
        for (project in openProjects) {
            try {
                UiCompilerService.getInstance(project).invalidateCache()
            } catch (_: Exception) {
                // Project might not have the service initialized yet
            }
        }
    }
}
