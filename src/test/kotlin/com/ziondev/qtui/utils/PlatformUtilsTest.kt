package com.ziondev.qtui.utils

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledOnOs
import org.junit.jupiter.api.condition.OS
import java.io.File

class PlatformUtilsTest {

    @Test
    fun `isWindows, isMacOS, and isLinux are mutually consistent`() {
        // At most one should be true (could be none if running on an exotic OS)
        val trueCount = listOf(PlatformUtils.isWindows, PlatformUtils.isMacOS, PlatformUtils.isLinux).count { it }
        assertThat(trueCount).isLessThanOrEqualTo(1)
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    fun `on Windows scriptsDir is Scripts`() {
        assertThat(PlatformUtils.scriptsDir).isEqualTo("Scripts")
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    fun `on Windows pathEnvVar is Path`() {
        assertThat(PlatformUtils.pathEnvVar).isEqualTo("Path")
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    fun `on Windows executableExtension is exe`() {
        assertThat(PlatformUtils.executableExtension).isEqualTo(".exe")
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    fun `on Windows whichCommand is where`() {
        assertThat(PlatformUtils.whichCommand).isEqualTo("where")
    }

    @Test
    @EnabledOnOs(OS.LINUX, OS.MAC)
    fun `on Unix scriptsDir is bin`() {
        assertThat(PlatformUtils.scriptsDir).isEqualTo("bin")
    }

    @Test
    @EnabledOnOs(OS.LINUX, OS.MAC)
    fun `on Unix pathEnvVar is PATH`() {
        assertThat(PlatformUtils.pathEnvVar).isEqualTo("PATH")
    }

    @Test
    @EnabledOnOs(OS.LINUX, OS.MAC)
    fun `on Unix executableExtension is empty`() {
        assertThat(PlatformUtils.executableExtension).isEmpty()
    }

    @Test
    @EnabledOnOs(OS.LINUX, OS.MAC)
    fun `on Unix whichCommand is which`() {
        assertThat(PlatformUtils.whichCommand).isEqualTo("which")
    }

    @Test
    fun `getPyQt6Paths returns valid paths for venv`() {
        val venvPath = "/test/venv"
        val paths = PlatformUtils.getPyQt6Paths(venvPath)

        assertThat(paths).isNotEmpty
        assertThat(paths).allMatch { it.contains(venvPath) }
        assertThat(paths).allMatch { it.contains("pyuic6") }
    }

    @Test
    fun `getPySide6Paths returns valid paths for venv`() {
        val venvPath = "/test/venv"
        val paths = PlatformUtils.getPySide6Paths(venvPath)

        assertThat(paths).isNotEmpty
        assertThat(paths).allMatch { it.contains(venvPath) }
        assertThat(paths).allMatch { it.contains("pyside6-uic") }
    }

    @Test
    fun `systemExecutables contains expected executables`() {
        assertThat(PlatformUtils.systemExecutables).containsExactly("pyuic6", "pyside6-uic")
    }

    @Test
    fun `getPyQt6Paths uses correct separator for venv path`() {
        val venvPath = "C:${File.separator}Users${File.separator}test${File.separator}.venv"
        val paths = PlatformUtils.getPyQt6Paths(venvPath)

        paths.forEach { path ->
            assertThat(path).startsWith(venvPath)
        }
    }
}
