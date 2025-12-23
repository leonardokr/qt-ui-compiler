package com.ziondev.qtui.utils

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.ziondev.qtui.settings.QtUiCompilerSettings
import java.nio.file.FileSystems
import java.nio.file.PathMatcher
import java.nio.file.Paths

object UiFileDetector {

    fun isUiFile(file: VirtualFile?, project: Project? = null): Boolean {
        if (file == null || file.isDirectory) return false

        val settings = QtUiCompilerSettings.getInstance()
        val rawPattern = settings.state.uiFilePattern.trim()

        val extension = file.extension?.lowercase()?.trim()
        val isExtensionUi = extension == "ui"
        
        if (rawPattern.isBlank() || rawPattern == "*.ui") {
            return isExtensionUi
        }

        return try {
            val pattern = rawPattern
            val globPattern = if (pattern.startsWith("glob:") || pattern.startsWith("regex:")) {
                pattern
            } else {
                "glob:$pattern"
            }
            val matcher: PathMatcher = FileSystems.getDefault().getPathMatcher(globPattern)

            val fileName = file.name
            val projectDir = project?.guessProjectDir()
            
            val fileNamePath = Paths.get(fileName)
            if (matcher.matches(fileNamePath)) return true
            
            val relativePath = if (projectDir != null) {
                VfsUtil.getRelativePath(file, projectDir)
            } else {
                null
            }
            
            if (relativePath != null && matcher.matches(Paths.get(relativePath))) return true
            
            val fullPath = file.path
            if (matcher.matches(Paths.get(fullPath))) return true

            val relativePathUnix = relativePath?.replace('\\', '/')
            if (relativePathUnix != null && relativePathUnix != relativePath) {
                if (matcher.matches(Paths.get(relativePathUnix))) return true
            }
            
            val fullPathUnix = fullPath.replace('\\', '/')
            if (fullPathUnix != fullPath) {
                if (matcher.matches(Paths.get(fullPathUnix))) return true
            }

            false
        } catch (e: Exception) {
            isExtensionUi
        }
    }
}
