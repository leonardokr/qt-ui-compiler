package com.ziondev.qtui.utils

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class SettingsValidatorTest {

    @Nested
    inner class VenvPathValidation {

        @Test
        fun `empty path is valid`() {
            val result = SettingsValidator.validateVenvPath("", null, false)
            assertThat(result.isValid).isTrue()
        }

        @Test
        fun `blank path is valid`() {
            val result = SettingsValidator.validateVenvPath("   ", null, false)
            assertThat(result.isValid).isTrue()
        }

        @Test
        fun `valid relative path format is valid`() {
            val result = SettingsValidator.validateVenvPath(".venv", null, true)
            assertThat(result.isValid).isTrue()
        }

        @Test
        fun `valid absolute path format is valid`(@TempDir tempDir: Path) {
            val result = SettingsValidator.validateVenvPath(tempDir.toString(), null, false)
            assertThat(result.isValid).isTrue()
        }
    }

    @Nested
    inner class UicPathValidation {

        @Test
        fun `empty path is valid`() {
            val result = SettingsValidator.validateUicPath("", null, false)
            assertThat(result.isValid).isTrue()
        }

        @Test
        fun `blank path is valid`() {
            val result = SettingsValidator.validateUicPath("   ", null, false)
            assertThat(result.isValid).isTrue()
        }

        @Test
        fun `non-existent file is invalid`() {
            val result = SettingsValidator.validateUicPath("/non/existent/path/pyuic6", null, false)
            assertThat(result.isValid).isFalse()
            assertThat(result.errorMessage).contains("does not exist")
        }

        @Test
        fun `existing file is valid`(@TempDir tempDir: Path) {
            val testFile = File(tempDir.toFile(), "test-exec")
            testFile.createNewFile()
            testFile.setExecutable(true)

            val result = SettingsValidator.validateUicPath(testFile.absolutePath, null, false)
            assertThat(result.isValid).isTrue()
        }

        @Test
        fun `directory path is invalid`(@TempDir tempDir: Path) {
            val result = SettingsValidator.validateUicPath(tempDir.toString(), null, false)
            assertThat(result.isValid).isFalse()
            assertThat(result.errorMessage).contains("not a file")
        }
    }

    @Nested
    inner class UiFilePatternValidation {

        @Test
        fun `empty pattern is valid`() {
            val result = SettingsValidator.validateUiFilePattern("")
            assertThat(result.isValid).isTrue()
        }

        @Test
        fun `simple glob pattern is valid`() {
            val result = SettingsValidator.validateUiFilePattern("*.ui")
            assertThat(result.isValid).isTrue()
        }

        @Test
        fun `complex glob pattern is valid`() {
            val result = SettingsValidator.validateUiFilePattern("**/ui/*.ui")
            assertThat(result.isValid).isTrue()
        }

        @Test
        fun `explicit glob prefix is valid`() {
            val result = SettingsValidator.validateUiFilePattern("glob:*.ui")
            assertThat(result.isValid).isTrue()
        }

        @Test
        fun `regex pattern is valid`() {
            val result = SettingsValidator.validateUiFilePattern("regex:.*\\.ui$")
            assertThat(result.isValid).isTrue()
        }

        @Test
        fun `invalid regex pattern is invalid`() {
            val result = SettingsValidator.validateUiFilePattern("regex:[invalid")
            assertThat(result.isValid).isFalse()
            assertThat(result.errorMessage).contains("Invalid")
        }
    }

    @Nested
    inner class OutputPathValidation {

        @Test
        fun `empty path is valid`() {
            val result = SettingsValidator.validateOutputPath("")
            assertThat(result.isValid).isTrue()
        }

        @Test
        fun `simple directory path is valid`() {
            val result = SettingsValidator.validateOutputPath("output/")
            assertThat(result.isValid).isTrue()
        }

        @Test
        fun `path with placeholder is valid`() {
            val result = SettingsValidator.validateOutputPath("generated/ui_\$1.py")
            assertThat(result.isValid).isTrue()
        }

        @Test
        fun `absolute path is valid`() {
            val result = SettingsValidator.validateOutputPath("/absolute/path/to/output")
            assertThat(result.isValid).isTrue()
        }
    }

    @Nested
    inner class ValidationResultTest {

        @Test
        fun `valid result has no error message`() {
            val result = SettingsValidator.ValidationResult.valid()
            assertThat(result.isValid).isTrue()
            assertThat(result.errorMessage).isNull()
        }

        @Test
        fun `invalid result has error message`() {
            val result = SettingsValidator.ValidationResult.invalid("Test error")
            assertThat(result.isValid).isFalse()
            assertThat(result.errorMessage).isEqualTo("Test error")
        }
    }
}
