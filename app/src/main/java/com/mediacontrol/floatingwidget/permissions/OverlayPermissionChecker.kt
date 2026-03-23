package com.mediacontrol.floatingwidget.permissions

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

class OverlayPermissionChecker(
    private val context: Context
) {
    fun isGranted(): Boolean = Settings.canDrawOverlays(context)

    fun createSettingsIntent(): Intent {
        return Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
}
