package com.mediacontrol.floatingwidget

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log

class AutomationEntryActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val started = MediaControlAppServices.from(this).debugActions.startOverlay()

        if (!started) {
            Log.w(TAG, "Automation launch fell back to MainActivity because readiness is blocked")
            startActivity(
                Intent(this, MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    .putExtra(EXTRA_OPEN_READINESS, true)
            )
        } else {
            Log.d(TAG, "Automation launch requested overlay runtime")
        }

        finish()
    }

    companion object {
        const val ACTION_SHOW_OVERLAY = "com.mediacontrol.floatingwidget.action.SHOW_OVERLAY"
        const val EXTRA_OPEN_READINESS = "open_readiness"
        private const val TAG = "AutomationEntry"
    }
}
