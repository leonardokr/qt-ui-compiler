package com.ziondev.qtui.utils

import java.io.File
import java.nio.file.FileSystems
import java.nio.file.InvalidPathException
import java.nio.file.Paths
import java.util.regex.PatternSyntaxException

/**
 * Utility object for validating plugin settings.
 * Provides validation methods with descriptive error messages.
 */
object SettingsValidator {

    /**
     * Validation result containing success status and optional error message.
     */
    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null
    ) {
        companion object {
            fun valid() = ValidationResult(true)
            fun invalid(message: String) = ValidationResult(false, message)
        }
    }

    /**
     * Validates a virtual environment path.
     * Checks if the path is a valid directory path format.
     */
    fun validateVenvPath(path: String, projectPath: String?, useRelative: Boolean): ValidationResult {
        if (path.isBlank()) {
            return ValidationResult.valid() // Empty is allowed, will use default
        }

        return validateDirectoryPath(path, projectPath, useRelative, "Virtual environment")
    }

    /**
     * Validates a custom UIC executable path.
     * Checks if the path points to an existing executable file.
     */
    fun validateUicPath(path: String, projectPath: String?, useRelative: Boolean): ValidationResult {
        if (path.isBlank()) {
            return ValidationResult.valid() // Empty is allowed, will auto-detect
        }

        val absolutePath = resolvePath(path, projectPath, useRelative)

        if (!isValidPath(absolutePath)) {
            return ValidationResult.invalid("Invalid path format")
        }

        val file = File(absolutePath)
        if (!file.exists()) {
            return ValidationResult.invalid("File does not exist: $absolutePath")
        }

        if (!file.isFile) {
            return ValidationResult.invalid("Path is not a file: $absolutePath")
        }

        if (!file.canExecute() && !PlatformUtils.isWindows) {
            return ValidationResult.invalid("File is not executable: $absolutePath")
        }

        return ValidationResult.valid()
    }

    /**
     * Validates a UI file pattern (glob or regex).
     */
    fun validateUiFilePattern(pattern: String): ValidationResult {
        if (pattern.isBlank()) {
            return ValidationResult.valid() // Empty is allowed, will use default
        }

        val globPattern = if (pattern.startsWith("glob:") || pattern.startsWith("regex:")) {
            pattern
        } else {
            "glob:$pattern"
        }

        return try {
            FileSystems.getDefault().getPathMatcher(globPattern)
            ValidationResult.valid()
        } catch (e: PatternSyntaxException) {
            ValidationResult.invalid("Invalid pattern syntax: ${e.message}")
        } catch (e: IllegalArgumentException) {
            ValidationResult.invalid("Invalid pattern: ${e.message}")
        }
    }

    /**
     * Validates an output path pattern.
     * Checks for valid path format and pattern syntax.
     */
    fun validateOutputPath(path: String): ValidationResult {
        if (path.isBlank()) {
            return ValidationResult.valid() // Empty is allowed, will use default
        }

        // Check for invalid characters in path (except for $1 placeholder)
        val pathWithoutPlaceholder = path.replace("\$1", "filename")

        if (!isValidPath(pathWithoutPlaceholder)) {
            return ValidationResult.invalid("Invalid path format")
        }

        return ValidationResult.valid()
    }

    private fun validateDirectoryPath(
        path: String,
        projectPath: String?,
        useRelative: Boolean,
        fieldName: String
    ): ValidationResult {
        val absolutePath = resolvePath(path, projectPath, useRelative)

        if (!isValidPath(absolutePath)) {
            return ValidationResult.invalid("Invalid path format for $fieldName")
        }

        // For relative paths without a project context, we can't fully validate
        if (useRelative && projectPath == null) {
            return ValidationResult.valid()
        }

        return ValidationResult.valid()
    }

    private fun resolvePath(path: String, projectPath: String?, useRelative: Boolean): String {
        return if (useRelative && projectPath != null) {
            try {
                Paths.get(projectPath, path).toAbsolutePath().toString()
            } catch (e: InvalidPathException) {
                path
            }
        } else {
            path
        }
    }

    private fun isValidPath(path: String): Boolean {
        return try {
            Paths.get(path)
            true
        } catch (e: InvalidPathException) {
            false
        }
    }
}
