package com.ziondev.qtui.listeners

import com.ziondev.qtui.services.UiCompilerService
import com.ziondev.qtui.settings.QtUiCompilerSettings
import com.ziondev.qtui.utils.UiFileDetector
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent

class UiFileChangeListener(private val project: Project) : BulkFileListener {
    
    private val logger = Logger.getInstance(UiFileChangeListener::class.java)
    
    override fun after(events: MutableList<out VFileEvent>) {
        val settings = QtUiCompilerSettings.getInstance()
        if (!settings.state.autoCompileEnabled) return

        if (project.isDisposed) return
        
        for (event in events) {
            val file = event.file ?: continue
            
            if (!UiFileDetector.isUiFile(file, project)) continue

            val projectDir = project.guessProjectDir()
            val belongsToProject = projectDir?.let { VfsUtil.isAncestor(it, file, false) } ?: false
            
            if (!belongsToProject) continue

            val compilerService = UiCompilerService.getInstance(project)
            logger.info("Triggering auto-compilation for UI file: ${file.path}")
            compilerService.compileUiFile(file)
        }
    }
}