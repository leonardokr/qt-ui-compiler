package com.ziondev.qtui.utils

import java.io.File

/**
 * Utility object for platform-specific operations and constants.
 * Centralizes OS detection and platform-dependent path configurations.
 */
object PlatformUtils {

    private val osName: String = System.getProperty("os.name").lowercase()

    /**
     * Whether the current OS is Windows.
     */
    val isWindows: Boolean = osName.contains("win")

    /**
     * Whether the current OS is macOS.
     */
    val isMacOS: Boolean = osName.contains("mac")

    /**
     * Whether the current OS is Linux.
     */
    val isLinux: Boolean = osName.contains("nux") || osName.contains("nix")

    /**
     * The scripts directory name for virtual environments.
     * "Scripts" on Windows, "bin" on Unix-like systems.
     */
    val scriptsDir: String = if (isWindows) "Scripts" else "bin"

    /**
     * The PATH environment variable name.
     * "Path" on Windows, "PATH" on Unix-like systems.
     */
    val pathEnvVar: String = if (isWindows) "Path" else "PATH"

    /**
     * The executable file extension.
     * ".exe" on Windows, empty on Unix-like systems.
     */
    val executableExtension: String = if (isWindows) ".exe" else ""

    /**
     * Known paths to search for PyQt6's pyuic6 executable within a virtual environment.
     */
    fun getPyQt6Paths(venvPath: String): List<String> = listOf(
        "$venvPath${File.separator}$scriptsDir${File.separator}pyuic6$executableExtension",
        "$venvPath${File.separator}bin${File.separator}pyuic6",
        "$venvPath${File.separator}Lib${File.separator}site-packages${File.separator}PyQt6${File.separator}pyuic6$executableExtension"
    )

    /**
     * Known paths to search for PySide6's pyside6-uic executable within a virtual environment.
     */
    fun getPySide6Paths(venvPath: String): List<String> = listOf(
        "$venvPath${File.separator}$scriptsDir${File.separator}pyside6-uic$executableExtension",
        "$venvPath${File.separator}bin${File.separator}pyside6-uic",
        "$venvPath${File.separator}Lib${File.separator}site-packages${File.separator}PySide6${File.separator}pyside6-uic$executableExtension"
    )

    /**
     * System executables to search in PATH when venv executables are not found.
     */
    val systemExecutables: List<String> = listOf("pyuic6", "pyside6-uic")

    /**
     * The command to check if an executable exists in PATH.
     * "where" on Windows, "which" on Unix-like systems.
     */
    val whichCommand: String = if (isWindows) "where" else "which"
}
