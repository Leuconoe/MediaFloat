package com.mediacontrol.floatingwidget

import android.app.Activity
import android.os.Bundle
import android.util.Log

class AutomationEntryActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val started = MediaControlAppServices.from(this).debugActions.startOverlay()

        if (!started) {
            Log.w(TAG, "Automation launch fell back to MainActivity because readiness is blocked")
            startActivity(MainActivity.launchIntent(this))
        } else {
            Log.d(TAG, "Automation launch requested overlay runtime")
        }

        finish()
    }

    companion object {
        const val ACTION_SHOW_OVERLAY = "com.mediacontrol.floatingwidget.action.SHOW_OVERLAY"
        private const val TAG = "AutomationEntry"
    }
}
