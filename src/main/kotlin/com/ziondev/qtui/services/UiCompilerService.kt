package com.ziondev.qtui.services

import com.ziondev.qtui.settings.QtUiCompilerSettings
import com.ziondev.qtui.utils.NotificationHelper
import com.ziondev.qtui.utils.PlatformUtils
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.ProcessHandlerFactory
import com.intellij.execution.process.ProcessListener
import com.intellij.execution.process.ProcessEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import java.io.File
import java.io.IOException
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicReference

@Service(Service.Level.PROJECT)
class UiCompilerService(private val project: Project) {

    private val logger = Logger.getInstance(UiCompilerService::class.java)

    /**
     * Cache for the UIC executable path.
     * Invalidated when settings change or when the cached executable no longer exists.
     */
    private val cachedUicExecutable = AtomicReference<CachedExecutable?>(null)

    private data class CachedExecutable(
        val path: String,
        val settingsHash: Int
    )

    companion object {
        /** Default timeout for UIC process execution in seconds. */
        private const val DEFAULT_PROCESS_TIMEOUT_SECONDS = 30L

        /** Timeout for checking if executable exists in PATH. */
        private const val EXECUTABLE_CHECK_TIMEOUT_SECONDS = 1L

        fun getInstance(project: Project): UiCompilerService {
            return project.getService(UiCompilerService::class.java)
        }
    }

    fun compileUiFile(uiFile: VirtualFile) {
        ApplicationManager.getApplication().executeOnPooledThread {
            executeCompilation(uiFile)
        }
    }

    private fun executeCompilation(uiFile: VirtualFile) {
        val projectPath = project.basePath
        if (projectPath == null) {
            logger.warn("Cannot compile UI file: project base path is null")
            return
        }

        val settings = QtUiCompilerSettings.getInstance()
        val settingsState = settings.state

        val uicExecutable = findUicExecutable(projectPath, settingsState)
        if (uicExecutable == null) {
            NotificationHelper.uicNotFound(
                project,
                settingsState.virtualEnvironmentPath,
                settingsState.uicPath
            )
            return
        }

        val outputFile = determineOutputFile(uiFile, settingsState, projectPath)
        createOutputDirectoryIfNeeded(outputFile)

        try {
            val commandLine = buildCommandLine(uicExecutable, uiFile, outputFile, projectPath, settingsState)
            executeProcess(commandLine, uiFile, outputFile)
        } catch (e: IOException) {
            logger.error("IO error while compiling UI file: ${uiFile.name}", e)
            NotificationHelper.error(project, "IO error: ${e.message}")
        } catch (e: SecurityException) {
            logger.error("Security error while compiling UI file: ${uiFile.name}", e)
            NotificationHelper.error(project, "Security error: ${e.message}")
        }
    }

    private fun buildCommandLine(
        uicExecutable: String,
        uiFile: VirtualFile,
        outputFile: File,
        projectPath: String,
        settings: QtUiCompilerSettings.State
    ): GeneralCommandLine {
        val commandLine = GeneralCommandLine()
            .withExePath(uicExecutable)
            .withParameters("-o", outputFile.absolutePath, uiFile.path)
            .withWorkDirectory(projectPath)

        if (settings.virtualEnvironmentPath.isNotEmpty()) {
            configureVirtualEnvironment(commandLine, settings, projectPath)
        }

        return commandLine
    }

    private fun configureVirtualEnvironment(
        commandLine: GeneralCommandLine,
        settings: QtUiCompilerSettings.State,
        projectPath: String
    ) {
        val venvPath = resolvePath(settings.virtualEnvironmentPath, projectPath, settings.useRelativePaths)

        commandLine.withEnvironment("VIRTUAL_ENV", venvPath)

        val currentPath = System.getenv(PlatformUtils.pathEnvVar) ?: ""
        val venvBin = Paths.get(venvPath, PlatformUtils.scriptsDir).toString()
        commandLine.withEnvironment(PlatformUtils.pathEnvVar, "$venvBin${File.pathSeparator}$currentPath")
    }

    private fun executeProcess(commandLine: GeneralCommandLine, uiFile: VirtualFile, outputFile: File) {
        val processHandler = ProcessHandlerFactory.getInstance()
            .createColoredProcessHandler(commandLine)

        processHandler.addProcessListener(object : ProcessListener {
            override fun processTerminated(event: ProcessEvent) {
                handleProcessTermination(event, uiFile, outputFile)
            }

            override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
                logger.info("UIC output: ${event.text}")
            }
        })

        processHandler.startNotify()

        // Wait for process with timeout to prevent hanging
        val finished = processHandler.waitFor(DEFAULT_PROCESS_TIMEOUT_SECONDS * 1000)
        if (!finished) {
            processHandler.destroyProcess()
            logger.error("UIC process timed out after $DEFAULT_PROCESS_TIMEOUT_SECONDS seconds for file: ${uiFile.name}")
            NotificationHelper.error(project, "Compilation timed out after $DEFAULT_PROCESS_TIMEOUT_SECONDS seconds")
        }
    }

    private fun handleProcessTermination(event: ProcessEvent, uiFile: VirtualFile, outputFile: File) {
        if (event.exitCode == 0) {
            NotificationHelper.compilationSuccess(project, uiFile.name, outputFile.name)
            refreshOutputFile(outputFile)
        } else {
            NotificationHelper.compilationFailed(project, uiFile.name, event.exitCode)
        }
    }

    /**
     * Refreshes the output file in the IDE.
     * Uses a short delay to ensure the file system has finished writing.
     */
    private fun refreshOutputFile(outputFile: File) {
        ApplicationManager.getApplication().invokeLater {
            // Small delay to ensure file is fully written
            Thread.sleep(100)
            LocalFileSystem.getInstance().refreshAndFindFileByIoFile(outputFile)
        }
    }

    private fun createOutputDirectoryIfNeeded(outputFile: File) {
        outputFile.parentFile?.let { parentDir ->
            if (!parentDir.exists()) {
                if (!parentDir.mkdirs()) {
                    logger.warn("Failed to create output directory: ${parentDir.absolutePath}")
                }
            }
        }
    }

    // ============================================================
    // UIC Executable Resolution
    // ============================================================

    private fun findUicExecutable(projectPath: String, settings: QtUiCompilerSettings.State): String? {
        // Check cache first
        val cached = cachedUicExecutable.get()
        val currentSettingsHash = computeSettingsHash(settings, projectPath)

        if (cached != null && cached.settingsHash == currentSettingsHash) {
            // Verify cached executable still exists
            if (File(cached.path).exists() || isSystemCommand(cached.path)) {
                return cached.path
            }
        }

        // Find executable
        val executable = resolveUicExecutable(projectPath, settings)

        // Update cache
        if (executable != null) {
            cachedUicExecutable.set(CachedExecutable(executable, currentSettingsHash))
        } else {
            cachedUicExecutable.set(null)
        }

        return executable
    }

    private fun computeSettingsHash(settings: QtUiCompilerSettings.State, projectPath: String): Int {
        return listOf(
            settings.virtualEnvironmentPath,
            settings.uicPath,
            settings.useRelativePaths.toString(),
            projectPath
        ).hashCode()
    }

    private fun isSystemCommand(path: String): Boolean {
        return !path.contains(File.separator) && !path.contains("/")
    }

    private fun resolveUicExecutable(projectPath: String, settings: QtUiCompilerSettings.State): String? {
        // 1. Try custom UIC path if specified
        if (settings.uicPath.isNotEmpty()) {
            val customPath = resolvePath(settings.uicPath, projectPath, settings.useRelativePaths)
            if (File(customPath).exists()) {
                return customPath
            }
        }

        // 2. Try virtual environment paths
        val venvPath = resolvePath(settings.virtualEnvironmentPath, projectPath, settings.useRelativePaths)

        val venvExecutable = findExecutableInVenv(venvPath)
        if (venvExecutable != null) {
            return venvExecutable
        }

        // 3. Try system PATH
        return findExecutableInSystemPath()
    }

    private fun findExecutableInVenv(venvPath: String): String? {
        val pyqt6Paths = PlatformUtils.getPyQt6Paths(venvPath)
        val pyside6Paths = PlatformUtils.getPySide6Paths(venvPath)

        return (pyqt6Paths + pyside6Paths).firstOrNull { File(it).exists() }
    }

    private fun findExecutableInSystemPath(): String? {
        return PlatformUtils.systemExecutables.firstOrNull { isExecutableInPath(it) }
    }

    private fun isExecutableInPath(executable: String): Boolean {
        val checkCommand = listOf(PlatformUtils.whichCommand, executable)

        return try {
            val process = ProcessBuilder(checkCommand).start()
            val finished = process.waitFor(EXECUTABLE_CHECK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            if (!finished) {
                process.destroyForcibly()
                false
            } else {
                process.exitValue() == 0
            }
        } catch (e: IOException) {
            logger.debug("IO error checking executable '$executable' in PATH: ${e.message}")
            false
        } catch (e: InterruptedException) {
            logger.debug("Interrupted while checking executable '$executable' in PATH")
            Thread.currentThread().interrupt()
            false
        } catch (e: SecurityException) {
            logger.debug("Security error checking executable '$executable' in PATH: ${e.message}")
            false
        }
    }

    // ============================================================
    // Output File Resolution
    // ============================================================

    private fun determineOutputFile(
        uiFile: VirtualFile,
        settings: QtUiCompilerSettings.State,
        projectPath: String
    ): File {
        val baseName = uiFile.nameWithoutExtension

        return if (settings.outputPath.isNotEmpty()) {
            resolveCustomOutputPath(settings.outputPath, baseName, projectPath)
        } else {
            getDefaultOutputFile(uiFile, baseName)
        }
    }

    /**
     * Resolves custom output path with pattern support.
     * Supports:
     * - $1 placeholder for base filename
     * - Directory paths (ending with / or \)
     * - Direct file paths
     */
    private fun resolveCustomOutputPath(outputPattern: String, baseName: String, projectPath: String): File {
        val resolvedPath = when {
            // Pattern with $1 placeholder
            outputPattern.contains("$") -> outputPattern.replace("\$1", baseName)

            // Directory path
            isDirectoryPath(outputPattern) -> Paths.get(outputPattern, "$baseName.py").toString()

            // File path without .py extension
            !outputPattern.lowercase().endsWith(".py") -> Paths.get(outputPattern, "$baseName.py").toString()

            // Direct file path with .py extension
            else -> outputPattern
        }

        return makeAbsolute(File(resolvedPath), projectPath)
    }

    private fun isDirectoryPath(path: String): Boolean {
        return path.endsWith("/") || path.endsWith("\\") || File(path).isDirectory
    }

    private fun getDefaultOutputFile(uiFile: VirtualFile, baseName: String): File {
        val uiFilePath = Paths.get(uiFile.path)
        return File(uiFilePath.parent.toString(), "$baseName.py")
    }

    private fun makeAbsolute(file: File, projectPath: String): File {
        return if (file.isAbsolute) file else File(projectPath, file.path)
    }

    // ============================================================
    // Path Utilities
    // ============================================================

    private fun resolvePath(path: String, projectPath: String, useRelative: Boolean): String {
        return if (useRelative) {
            Paths.get(projectPath, path).toAbsolutePath().toString()
        } else {
            path
        }
    }

    /**
     * Invalidates the cached UIC executable.
     * Call this when settings change.
     */
    fun invalidateCache() {
        cachedUicExecutable.set(null)
    }
}
