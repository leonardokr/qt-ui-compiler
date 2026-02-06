package com.ziondev.qtui.utils

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.ziondev.qtui.QtUiCompilerBundle

/**
 * Helper object for displaying notifications in a consistent manner.
 * Centralizes notification logic to avoid code duplication.
 */
object NotificationHelper {

    private const val NOTIFICATION_GROUP_ID = "Qt UI Compiler"

    /** Default expiration time for notifications in milliseconds. */
    private const val NOTIFICATION_EXPIRE_MS = 3000L

    /**
     * Shows a notification with the given content and type.
     * The notification will automatically expire after the configured time.
     *
     * @param project The project context for the notification.
     * @param content The notification message content.
     * @param type The type of notification (INFO, WARNING, ERROR).
     */
    fun show(project: Project, content: String, type: NotificationType) {
        val group = NotificationGroupManager.getInstance().getNotificationGroup(NOTIFICATION_GROUP_ID)
        val notification = group?.createNotification(
            QtUiCompilerBundle.message("notification.title"),
            content,
            type
        )

        notification?.notify(project)

        // Auto-expire the notification after the configured time
        notification?.let {
            com.intellij.util.Alarm().addRequest({
                it.expire()
            }, NOTIFICATION_EXPIRE_MS)
        }
    }

    /**
     * Shows an information notification.
     */
    fun info(project: Project, content: String) {
        show(project, content, NotificationType.INFORMATION)
    }

    /**
     * Shows a warning notification.
     */
    fun warning(project: Project, content: String) {
        show(project, content, NotificationType.WARNING)
    }

    /**
     * Shows an error notification.
     */
    fun error(project: Project, content: String) {
        show(project, content, NotificationType.ERROR)
    }

    /**
     * Shows a success notification for compiled files.
     */
    fun compilationSuccess(project: Project, inputFileName: String, outputFileName: String) {
        info(project, QtUiCompilerBundle.message("notification.success.compiled", inputFileName, outputFileName))
    }

    /**
     * Shows an error notification for failed compilation.
     */
    fun compilationFailed(project: Project, fileName: String, exitCode: Int) {
        error(project, QtUiCompilerBundle.message("notification.error.compilation.failed", fileName, exitCode))
    }

    /**
     * Shows an error notification when UIC executable is not found.
     */
    fun uicNotFound(project: Project, venvPath: String, uicPath: String) {
        error(project, QtUiCompilerBundle.message("notification.error.uic.not.found", venvPath, uicPath))
    }

    /**
     * Shows a warning notification when the file is not a UI file.
     */
    fun notUiFile(project: Project, fileName: String) {
        warning(project, QtUiCompilerBundle.message("notification.error.not.ui.file", fileName))
    }
}
