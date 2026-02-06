package com.ziondev.qtui.utils

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.ziondev.qtui.settings.QtUiCompilerSettings
import java.nio.file.FileSystems
import java.nio.file.InvalidPathException
import java.nio.file.PathMatcher
import java.nio.file.Paths
import java.util.regex.PatternSyntaxException

/**
 * Utility object for detecting Qt UI files.
 * Supports custom glob/regex patterns for flexible file matching.
 */
object UiFileDetector {

    private val logger = Logger.getInstance(UiFileDetector::class.java)

    private const val DEFAULT_EXTENSION = "ui"

    /**
     * Determines if the given file is a Qt UI file based on extension or custom pattern.
     *
     * @param file The file to check.
     * @param project Optional project context for relative path resolution.
     * @return true if the file matches UI file criteria.
     */
    fun isUiFile(file: VirtualFile?, project: Project? = null): Boolean {
        if (file == null || file.isDirectory) return false

        val settings = QtUiCompilerSettings.getInstance()
        val rawPattern = settings.state.uiFilePattern.trim()

        // Fast path: use extension check for default or empty pattern
        if (rawPattern.isBlank() || rawPattern == "*.ui") {
            return hasUiExtension(file)
        }

        return matchesCustomPattern(file, rawPattern, project)
    }

    private fun hasUiExtension(file: VirtualFile): Boolean {
        return file.extension?.lowercase() == DEFAULT_EXTENSION
    }

    /**
     * Matches file against a custom glob or regex pattern.
     * Falls back to extension check on pattern errors.
     */
    private fun matchesCustomPattern(file: VirtualFile, rawPattern: String, project: Project?): Boolean {
        val matcher = createPathMatcher(rawPattern)
        if (matcher == null) {
            // Pattern was invalid, fall back to extension check
            return hasUiExtension(file)
        }

        return tryMatchPaths(file, matcher, project)
    }

    /**
     * Creates a PathMatcher from the pattern.
     * Returns null if the pattern is invalid.
     */
    private fun createPathMatcher(pattern: String): PathMatcher? {
        val globPattern = if (pattern.startsWith("glob:") || pattern.startsWith("regex:")) {
            pattern
        } else {
            "glob:$pattern"
        }

        return try {
            FileSystems.getDefault().getPathMatcher(globPattern)
        } catch (e: PatternSyntaxException) {
            logger.warn("Invalid file pattern syntax: '$pattern'. Error: ${e.message}")
            null
        } catch (e: IllegalArgumentException) {
            logger.warn("Invalid file pattern: '$pattern'. Error: ${e.message}")
            null
        }
    }

    /**
     * Attempts to match the file against the pattern using various path representations.
     * This handles cross-platform path separator differences.
     */
    private fun tryMatchPaths(file: VirtualFile, matcher: PathMatcher, project: Project?): Boolean {
        // Try matching filename only
        if (matchPath(matcher, file.name)) return true

        // Try relative path from project
        val relativePath = getRelativePath(file, project)
        if (relativePath != null) {
            if (matchPath(matcher, relativePath)) return true

            // Try with Unix-style separators (for cross-platform patterns)
            val unixPath = relativePath.replace('\\', '/')
            if (unixPath != relativePath && matchPath(matcher, unixPath)) return true
        }

        // Try full path as last resort
        if (matchPath(matcher, file.path)) return true

        // Try full path with Unix separators
        val unixFullPath = file.path.replace('\\', '/')
        if (unixFullPath != file.path && matchPath(matcher, unixFullPath)) return true

        return false
    }

    private fun getRelativePath(file: VirtualFile, project: Project?): String? {
        val projectDir = project?.guessProjectDir() ?: return null
        return VfsUtil.getRelativePath(file, projectDir)
    }

    /**
     * Safely matches a path string against the matcher.
     * Returns false on invalid paths rather than throwing.
     */
    private fun matchPath(matcher: PathMatcher, pathString: String): Boolean {
        return try {
            matcher.matches(Paths.get(pathString))
        } catch (e: InvalidPathException) {
            logger.debug("Invalid path for matching: '$pathString'")
            false
        }
    }
}
