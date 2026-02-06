package com.ziondev.qtui.actions

import com.ziondev.qtui.services.UiCompilerService
import com.ziondev.qtui.utils.NotificationHelper
import com.ziondev.qtui.utils.UiFileDetector
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.Project

class CompileUiFileAction : AnAction() {

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project: Project = e.project ?: return
        val virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE)
            ?: e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)?.firstOrNull()
            ?: return

        if (UiFileDetector.isUiFile(virtualFile, project)) {
            UiCompilerService.getInstance(project).compileUiFile(virtualFile)
        } else {
            NotificationHelper.notUiFile(project, virtualFile.name)
        }
    }

    override fun update(e: AnActionEvent) {
        val project = e.project
        val virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE)
            ?: e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)?.firstOrNull()

        e.presentation.isEnabled = UiFileDetector.isUiFile(virtualFile, project)
        e.presentation.isVisible = true
    }
}
