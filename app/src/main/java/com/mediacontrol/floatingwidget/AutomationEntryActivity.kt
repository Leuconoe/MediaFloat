package sw2.io.mediafloat

import android.app.Activity
import android.os.Bundle
import android.util.Log

class AutomationEntryActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        AppLocaleManager.apply(MediaControlAppServices.from(this).appPreferencesRepository.currentState().appLanguage)
        super.onCreate(savedInstanceState)

        handleAutomationAction(intent?.action)
        finish()
    }

    override fun onNewIntent(intent: android.content.Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleAutomationAction(intent?.action)
        finish()
    }

    private fun handleAutomationAction(action: String?) {
        val services = MediaControlAppServices.from(this)
        val debugActions = services.debugActions

        when (action) {
            ACTION_STOP_OVERLAY -> {
                debugActions.stopOverlay()
                Log.d(TAG, "Automation shortcut requested overlay stop")
            }

            ACTION_TOGGLE_OVERLAY -> {
                val wasShowing = services.runtimeCoordinator.runtimeState() is sw2.io.mediafloat.model.OverlayRuntimeState.Showing
                val started = debugActions.toggleOverlay()

                if (!wasShowing && !started) {
                    Log.w(TAG, "Automation toggle fell back to MainActivity because readiness is blocked")
                    startActivity(MainActivity.launchIntent(this))
                } else {
                    Log.d(TAG, "Automation shortcut toggled overlay runtime")
                }
            }

            else -> {
                val started = debugActions.startOverlay()

                if (!started) {
                    Log.w(TAG, "Automation launch fell back to MainActivity because readiness is blocked")
                    startActivity(MainActivity.launchIntent(this))
                } else {
                    Log.d(TAG, "Automation launch requested overlay runtime")
                }
            }
        }
    }

    companion object {
        const val ACTION_SHOW_OVERLAY = "sw2.io.mediafloat.action.SHOW_OVERLAY"
        const val ACTION_STOP_OVERLAY = "sw2.io.mediafloat.action.STOP_OVERLAY"
        const val ACTION_TOGGLE_OVERLAY = "sw2.io.mediafloat.action.TOGGLE_OVERLAY"
        private const val TAG = "AutomationEntry"
    }
}
