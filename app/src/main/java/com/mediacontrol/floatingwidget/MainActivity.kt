package sw2.io.mediafloat

import android.content.Context
import android.content.Intent
import android.content.ActivityNotFoundException
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import sw2.io.mediafloat.debug.DebugActions
import sw2.io.mediafloat.model.AppPreferences
import sw2.io.mediafloat.model.CapabilityGrantState
import sw2.io.mediafloat.model.CapabilityState
import sw2.io.mediafloat.model.MediaSessionState
import sw2.io.mediafloat.model.NotificationPosture
import sw2.io.mediafloat.model.OverlayRuntimeState
import sw2.io.mediafloat.model.OverlayUnavailableReason
import sw2.io.mediafloat.model.WidgetButton
import sw2.io.mediafloat.model.WidgetConfig
import sw2.io.mediafloat.model.WidgetPosition
import sw2.io.mediafloat.model.WidgetSizePreset
import sw2.io.mediafloat.model.WidgetThemePreset
import sw2.io.mediafloat.model.WidgetWidthStyle
import sw2.io.mediafloat.model.DragHandlePlacement
import sw2.io.mediafloat.runtime.OverlayRuntimeCoordinator
import sw2.io.mediafloat.state.AppPreferencesStateHolder
import sw2.io.mediafloat.state.DebugLogScreenState
import sw2.io.mediafloat.state.DebugLogStateHolder
import sw2.io.mediafloat.state.MediaSummaryState
import sw2.io.mediafloat.state.MediaSummaryStateHolder
import sw2.io.mediafloat.state.RuntimeSummaryState
import sw2.io.mediafloat.state.RuntimeSummaryStateHolder
import sw2.io.mediafloat.state.StateListener
import sw2.io.mediafloat.state.WidgetConfigScreenState
import sw2.io.mediafloat.state.WidgetConfigStateHolder
import sw2.io.mediafloat.ui.AppShell
import sw2.io.mediafloat.ui.theme.MediaFloatTheme

class MainActivity : AppCompatActivity() {

    private val appServices by lazy { MediaControlAppServices.from(this) }
    private val runtimeCoordinator: OverlayRuntimeCoordinator by lazy { appServices.runtimeCoordinator }
    private val appPreferencesStateHolder: AppPreferencesStateHolder by lazy { appServices.appPreferencesStateHolder }
    private val runtimeSummaryStateHolder: RuntimeSummaryStateHolder by lazy { appServices.runtimeSummaryStateHolder }
    private val widgetConfigStateHolder: WidgetConfigStateHolder by lazy { appServices.widgetConfigStateHolder }
    private val mediaSummaryStateHolder: MediaSummaryStateHolder by lazy { appServices.mediaSummaryStateHolder }
    private val debugLogStateHolder: DebugLogStateHolder by lazy { appServices.debugLogStateHolder }
    private val debugActions: DebugActions by lazy { appServices.debugActions }
    private val appPreferencesStateListener = StateListener<AppPreferences> { nextState ->
        appPreferences = nextState
    }
    private val runtimeStateListener = StateListener<RuntimeSummaryState> { summaryState ->
        applySummaryState(summaryState)
    }
    private val widgetConfigStateListener = StateListener<WidgetConfigScreenState> { nextState ->
        widgetConfigState = nextState
    }
    private val mediaSummaryStateListener = StateListener<MediaSummaryState> { nextState ->
        mediaSummaryState = nextState
    }
    private val debugLogStateListener = StateListener<DebugLogScreenState> { nextState ->
        debugLogState = nextState
    }

    private var appPreferences by mutableStateOf(defaultAppPreferences())
    private var capabilityState by mutableStateOf(defaultCapabilityState())
    private var runtimeState by mutableStateOf<OverlayRuntimeState>(
        OverlayRuntimeState.Unavailable(OverlayUnavailableReason.UnknownRuntimeFailure)
    )
    private var widgetConfigState by mutableStateOf(defaultWidgetConfigState())
    private var mediaSummaryState by mutableStateOf(defaultMediaSummaryState())
    private var debugLogState by mutableStateOf(defaultDebugLogState())

    override fun onCreate(savedInstanceState: Bundle?) {
        AppLocaleManager.apply(appServices.appPreferencesRepository.currentState().appLanguage)
        super.onCreate(savedInstanceState)
        appPreferences = appPreferencesStateHolder.currentState()
        applySummaryState(runtimeSummaryStateHolder.currentState())
        widgetConfigState = widgetConfigStateHolder.currentState()
        mediaSummaryState = mediaSummaryStateHolder.currentState()
        debugLogState = debugLogStateHolder.currentState()

        enableEdgeToEdge()
        setContent {
            MediaFloatTheme {
                AppShell(
                    appPreferences = appPreferences,
                    capabilityState = capabilityState,
                    runtimeState = runtimeState,
                    mediaSummaryState = mediaSummaryState,
                    widgetConfigState = widgetConfigState,
                    debugLogState = debugLogState,
                    onSetDebugToolsEnabled = ::setDebugToolsEnabled,
                    onSetAppLanguage = ::setAppLanguage,
                    onSetVisibleButtons = ::setVisibleButtons,
                    onSetSizePreset = ::setSizePreset,
                    onSetWidthStyle = ::setWidthStyle,
                    onSetThemePreset = ::setThemePreset,
                    onSetOpacity = ::setOpacity,
                    onSetDragHandlePlacement = ::setDragHandlePlacement,
                    onSetPersistentOverlayEnabled = ::setPersistentOverlayEnabled,
                    onSetLowQualityThumbnailFallbackEnabled = ::setLowQualityThumbnailFallbackEnabled,
                    onStartOverlay = ::startOverlay,
                    onStopOverlay = ::stopOverlay,
                    onDispatchPrevious = ::dispatchPrevious,
                    onDispatchPlayPause = ::dispatchPlayPause,
                    onDispatchNext = ::dispatchNext,
                    onClearLogs = ::clearLogs,
                    onOpenOverlaySettings = {
                        openSystemSettings(runtimeCoordinator::overlaySettingsIntent)
                    },
                    onOpenNotificationListenerSettings = {
                        openSystemSettings(runtimeCoordinator::notificationListenerSettingsIntent)
                    },
                    onOpenNotificationSettings = {
                        openSystemSettings(runtimeCoordinator::notificationSettingsIntent)
                    },
                    automationLaunchAction = AutomationEntryActivity.ACTION_SHOW_OVERLAY
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        appPreferencesStateHolder.addListener(appPreferencesStateListener)
        runtimeSummaryStateHolder.addListener(runtimeStateListener)
        widgetConfigStateHolder.addListener(widgetConfigStateListener)
        mediaSummaryStateHolder.addListener(mediaSummaryStateListener)
        debugLogStateHolder.addListener(debugLogStateListener)
        mediaSummaryStateHolder.start()
        runtimeSummaryStateHolder.refresh()
    }

    override fun onResume() {
        super.onResume()
        runtimeSummaryStateHolder.refresh()
    }

    override fun onStop() {
        debugLogStateHolder.removeListener(debugLogStateListener)
        mediaSummaryStateHolder.stop()
        mediaSummaryStateHolder.removeListener(mediaSummaryStateListener)
        widgetConfigStateHolder.removeListener(widgetConfigStateListener)
        runtimeSummaryStateHolder.removeListener(runtimeStateListener)
        appPreferencesStateHolder.removeListener(appPreferencesStateListener)
        super.onStop()
    }

    private fun setVisibleButtons(buttons: Set<WidgetButton>) {
        widgetConfigStateHolder.setVisibleButtons(buttons)
    }

    private fun setDebugToolsEnabled(enabled: Boolean) {
        appPreferencesStateHolder.setDebugToolsEnabled(enabled)
    }

    private fun setAppLanguage(appLanguage: sw2.io.mediafloat.model.AppLanguage) {
        AppLocaleManager.apply(appLanguage)
        appPreferencesStateHolder.setAppLanguage(appLanguage)
    }

    private fun setSizePreset(sizePreset: WidgetSizePreset) {
        widgetConfigStateHolder.setSizePreset(sizePreset)
    }

    private fun setWidthStyle(widthStyle: WidgetWidthStyle) {
        widgetConfigStateHolder.setWidthStyle(widthStyle)
    }

    private fun setThemePreset(themePreset: WidgetThemePreset) {
        widgetConfigStateHolder.setThemePreset(themePreset)
    }

    private fun setOpacity(opacity: Float) {
        widgetConfigStateHolder.setOpacity(opacity)
    }

    private fun setDragHandlePlacement(placement: DragHandlePlacement) {
        widgetConfigStateHolder.setDragHandlePlacement(placement)
    }

    private fun setPersistentOverlayEnabled(enabled: Boolean) {
        widgetConfigStateHolder.setPersistentOverlayEnabled(enabled)
    }

    private fun setLowQualityThumbnailFallbackEnabled(enabled: Boolean) {
        widgetConfigStateHolder.setLowQualityThumbnailFallbackEnabled(enabled)
    }

    private fun startOverlay() {
        val started = debugActions.startOverlay()
        runtimeSummaryStateHolder.refresh()

        if (!started) {
            when {
                capabilityState.overlayAccess != CapabilityGrantState.Granted -> {
                    startActivity(runtimeCoordinator.overlaySettingsIntent())
                }

                capabilityState.notificationListenerAccess != CapabilityGrantState.Granted -> {
                    startActivity(runtimeCoordinator.notificationListenerSettingsIntent())
                }

                capabilityState.notificationPosture != NotificationPosture.Visible -> {
                    startActivity(runtimeCoordinator.notificationSettingsIntent())
                }
            }
        }
    }

    private fun stopOverlay() {
        debugActions.stopOverlay()
        runtimeSummaryStateHolder.refresh()
    }

    private fun dispatchPrevious() {
        debugActions.dispatchPrevious()
    }

    private fun dispatchPlayPause() {
        debugActions.dispatchPlayPause()
    }

    private fun dispatchNext() {
        debugActions.dispatchNext()
    }

    private fun clearLogs() {
        debugActions.clearLogs()
    }

    private fun openSystemSettings(intentProvider: () -> Intent) {
        val primaryIntent = intentProvider()
        try {
            startActivity(primaryIntent)
        } catch (error: ActivityNotFoundException) {
            Log.w(TAG, "Primary settings intent unavailable; falling back to app details", error)
            openAppDetailsSettings()
        } catch (error: Exception) {
            Log.e(TAG, "Failed to open system settings", error)
            openAppDetailsSettings()
        }
    }

    private fun openAppDetailsSettings() {
        runCatching {
            startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    .setData(Uri.parse("package:$packageName"))
            )
        }.onFailure { error ->
            Log.e(TAG, "Failed to open app details settings fallback", error)
        }
    }

    private fun applySummaryState(summaryState: RuntimeSummaryState) {
        capabilityState = summaryState.capabilityState
        runtimeState = summaryState.runtimeState
    }

    private fun defaultCapabilityState(): CapabilityState {
        return CapabilityState(
            overlayAccess = CapabilityGrantState.Missing,
            notificationListenerAccess = CapabilityGrantState.Missing,
            notificationPosture = NotificationPosture.PermissionRequired,
            serviceStartReadiness = CapabilityGrantState.Granted
        )
    }

    private fun defaultAppPreferences(): AppPreferences {
        return AppPreferences()
    }

    private fun defaultWidgetConfigState(): WidgetConfigScreenState {
        return WidgetConfigScreenState(
            config = WidgetConfig(),
            position = WidgetPosition()
        )
    }

    private fun defaultMediaSummaryState(): MediaSummaryState {
        return MediaSummaryState(
            mediaState = MediaSessionState.Unavailable,
            previousEnabled = false,
            playPauseEnabled = false,
            nextEnabled = false
        )
    }

    private fun defaultDebugLogState(): DebugLogScreenState {
        return DebugLogScreenState(
            retentionLimit = 200,
            entries = emptyList()
        )
    }

    companion object {
        private const val TAG = "MainActivity"

        fun launchIntent(context: Context): Intent {
            return Intent(context, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
    }
}
