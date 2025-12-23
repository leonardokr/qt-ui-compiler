package com.ziondev.qtui.services

import com.ziondev.qtui.settings.QtUiCompilerSettings
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessHandlerFactory
import com.intellij.execution.process.ProcessListener
import com.intellij.execution.process.ProcessEvent
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import java.io.File
import java.nio.file.Paths
import java.util.concurrent.TimeUnit

@Service(Service.Level.PROJECT)
class UiCompilerService(private val project: Project) {
    
    private val logger = Logger.getInstance(UiCompilerService::class.java)
    
    fun compileUiFile(uiFile: VirtualFile) {
        ApplicationManager.getApplication().executeOnPooledThread {
            try {
                val settings = QtUiCompilerSettings.getInstance()
                val projectPath = project.basePath ?: return@executeOnPooledThread
                
                val uicExecutable = findUicExecutable(projectPath, settings.state)
                if (uicExecutable == null) {
                    val message = "UIC executable not found. Please check your settings.\n" +
                                 "Looking in: ${settings.state.virtualEnvironmentPath}\n" +
                                 "Custom path: ${settings.state.uicPath}"
                    showNotification(message, NotificationType.ERROR)
                    return@executeOnPooledThread
                }
                
                val outputFile = determineOutputFile(uiFile, settings.state, projectPath)
                
                outputFile.parentFile?.let {
                    if (!it.exists()) {
                        it.mkdirs()
                    }
                }
                
                val commandLine = GeneralCommandLine()
                    .withExePath(uicExecutable)
                    .withParameters("-o", outputFile.absolutePath, uiFile.path)
                    .withWorkDirectory(projectPath)
                
                val settingsState = settings.state
                if (settingsState.virtualEnvironmentPath.isNotEmpty()) {
                    val venvPath = if (settingsState.useRelativePaths) {
                        Paths.get(projectPath, settingsState.virtualEnvironmentPath).toAbsolutePath().toString()
                    } else {
                        settingsState.virtualEnvironmentPath
                    }
                    commandLine.withEnvironment("VIRTUAL_ENV", venvPath)
                    
                    val isWindows = System.getProperty("os.name").lowercase().contains("win")
                    val pathVar = if (isWindows) "Path" else "PATH"
                    val currentPath = System.getenv(pathVar) ?: ""
                    val binDir = if (isWindows) "Scripts" else "bin"
                    val venvBin = Paths.get(venvPath, binDir).toString()
                    commandLine.withEnvironment(pathVar, "$venvBin${File.pathSeparator}$currentPath")
                }
                
                val processHandler = ProcessHandlerFactory.getInstance()
                    .createColoredProcessHandler(commandLine)
                
                processHandler.addProcessListener(object : ProcessListener {
                    override fun processTerminated(event: ProcessEvent) {
                        if (event.exitCode == 0) {
                            showNotification("Successfully compiled ${uiFile.name} to ${outputFile.name}", NotificationType.INFORMATION)
                            
                            ApplicationManager.getApplication().invokeLater {
                                LocalFileSystem.getInstance().refreshAndFindFileByIoFile(outputFile)
                            }
                        } else {
                            showNotification("Failed to compile ${uiFile.name}. Exit code: ${event.exitCode}. Check logs for details.", NotificationType.ERROR)
                        }
                    }
                    
                    override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
                        logger.info("UIC output: ${event.text}")
                    }
                })
                
                processHandler.startNotify()
                
            } catch (e: Exception) {
                logger.error("Error compiling UI file: ${uiFile.name}", e)
            }
        }
    }
    
    private fun findUicExecutable(projectPath: String, settings: QtUiCompilerSettings.State): String? {
        val venvPath = if (settings.useRelativePaths) {
            Paths.get(projectPath, settings.virtualEnvironmentPath)
        } else {
            Paths.get(settings.virtualEnvironmentPath)
        }
        
        if (settings.uicPath.isNotEmpty()) {
            val customUicPath = if (settings.useRelativePaths) {
                Paths.get(projectPath, settings.uicPath)
            } else {
                Paths.get(settings.uicPath)
            }
            
            if (customUicPath.toFile().exists()) {
                return customUicPath.toString()
            }
        }
        
        val pyqt6Paths = listOf(
            venvPath.resolve("Scripts/pyuic6.exe"),
            venvPath.resolve("bin/pyuic6"),
            venvPath.resolve("Lib/site-packages/PyQt6/pyuic6.exe")
        )
        
        val pyside6Paths = listOf(
            venvPath.resolve("Scripts/pyside6-uic.exe"),
            venvPath.resolve("bin/pyside6-uic"),
            venvPath.resolve("Lib/site-packages/PySide6/pyside6-uic.exe")
        )
        
        val pyqt6Executable = pyqt6Paths.firstOrNull { it.toFile().exists() }
        if (pyqt6Executable != null) return pyqt6Executable.toString()
        
        val pyside6Executable = pyside6Paths.firstOrNull { it.toFile().exists() }
        if (pyside6Executable != null) return pyside6Executable.toString()
        
        val systemPaths = listOf("pyuic6", "pyside6-uic")
        for (cmd in systemPaths) {
            if (isExecutableInPath(cmd)) {
                return cmd
            }
        }
        
        return null
    }

    private fun isExecutableInPath(executable: String): Boolean {
        val os = System.getProperty("os.name").lowercase()
        val checkCommand = if (os.contains("win")) {
            listOf("where", executable)
        } else {
            listOf("which", executable)
        }

        return try {
            val process = ProcessBuilder(checkCommand).start()
            process.waitFor(1, TimeUnit.SECONDS)
            process.exitValue() == 0
        } catch (e: Exception) {
            false
        }
    }
    
    private fun determineOutputFile(uiFile: VirtualFile, settings: QtUiCompilerSettings.State, projectPath: String): File {
        val fileName = uiFile.nameWithoutExtension
        
        if (settings.outputPath.isNotEmpty()) {
            val outputPattern = settings.outputPath
            val outputFileName = if (outputPattern.contains("$")) {
                outputPattern.replace("\$1", fileName)
            } else {
                val path = Paths.get(outputPattern)
                if (outputPattern.endsWith("/") || outputPattern.endsWith("\\") || File(outputPattern).isDirectory) {
                    path.resolve("${fileName}.py").toString()
                } else {
                    if (!outputPattern.lowercase().endsWith(".py")) {
                        path.resolve("${fileName}.py").toString()
                    } else {
                        outputPattern
                    }
                }
            }
            
            val file = File(outputFileName)
            return if (file.isAbsolute) {
                file
            } else {
                File(projectPath, outputFileName)
            }
        } else {
            val uiFilePath = Paths.get(uiFile.path)
            return File(uiFilePath.parent.toString(), "${fileName}.py")
        }
    }
    
    private fun showNotification(content: String, type: NotificationType) {
        val group = NotificationGroupManager.getInstance().getNotificationGroup("Qt UI Compiler")
        group?.createNotification("Qt UI Compiler", content, type)?.notify(project)
    }

    companion object {
        fun getInstance(project: Project): UiCompilerService {
            return project.getService(UiCompilerService::class.java)
        }
    }
}