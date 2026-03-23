package com.mediacontrol.floatingwidget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.mediacontrol.floatingwidget.debug.DebugActions
import com.mediacontrol.floatingwidget.model.AppPreferences
import com.mediacontrol.floatingwidget.model.CapabilityGrantState
import com.mediacontrol.floatingwidget.model.CapabilityState
import com.mediacontrol.floatingwidget.model.MediaSessionState
import com.mediacontrol.floatingwidget.model.NotificationPosture
import com.mediacontrol.floatingwidget.model.OverlayRuntimeState
import com.mediacontrol.floatingwidget.model.OverlayUnavailableReason
import com.mediacontrol.floatingwidget.model.WidgetButton
import com.mediacontrol.floatingwidget.model.WidgetConfig
import com.mediacontrol.floatingwidget.model.WidgetPosition
import com.mediacontrol.floatingwidget.model.WidgetSizePreset
import com.mediacontrol.floatingwidget.model.WidgetThemePreset
import com.mediacontrol.floatingwidget.model.WidgetWidthStyle
import com.mediacontrol.floatingwidget.runtime.OverlayRuntimeCoordinator
import com.mediacontrol.floatingwidget.state.AppPreferencesStateHolder
import com.mediacontrol.floatingwidget.state.DebugLogScreenState
import com.mediacontrol.floatingwidget.state.DebugLogStateHolder
import com.mediacontrol.floatingwidget.state.MediaSummaryState
import com.mediacontrol.floatingwidget.state.MediaSummaryStateHolder
import com.mediacontrol.floatingwidget.state.RuntimeSummaryState
import com.mediacontrol.floatingwidget.state.RuntimeSummaryStateHolder
import com.mediacontrol.floatingwidget.state.StateListener
import com.mediacontrol.floatingwidget.state.WidgetConfigScreenState
import com.mediacontrol.floatingwidget.state.WidgetConfigStateHolder
import com.mediacontrol.floatingwidget.ui.AppShell
import com.mediacontrol.floatingwidget.ui.theme.MediaFloatTheme

class MainActivity : ComponentActivity() {

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
                    onSetVisibleButtons = ::setVisibleButtons,
                    onSetSizePreset = ::setSizePreset,
                    onSetWidthStyle = ::setWidthStyle,
                    onSetThemePreset = ::setThemePreset,
                    onSetPersistentOverlayEnabled = ::setPersistentOverlayEnabled,
                    onStartOverlay = ::startOverlay,
                    onStopOverlay = ::stopOverlay,
                    onDispatchPrevious = ::dispatchPrevious,
                    onDispatchPlayPause = ::dispatchPlayPause,
                    onDispatchNext = ::dispatchNext,
                    onClearLogs = ::clearLogs,
                    onOpenOverlaySettings = {
                        startActivity(runtimeCoordinator.overlaySettingsIntent())
                    },
                    onOpenNotificationListenerSettings = {
                        startActivity(runtimeCoordinator.notificationListenerSettingsIntent())
                    },
                    onOpenNotificationSettings = {
                        startActivity(runtimeCoordinator.notificationSettingsIntent())
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

    private fun setSizePreset(sizePreset: WidgetSizePreset) {
        widgetConfigStateHolder.setSizePreset(sizePreset)
    }

    private fun setWidthStyle(widthStyle: WidgetWidthStyle) {
        widgetConfigStateHolder.setWidthStyle(widthStyle)
    }

    private fun setThemePreset(themePreset: WidgetThemePreset) {
        widgetConfigStateHolder.setThemePreset(themePreset)
    }

    private fun setPersistentOverlayEnabled(enabled: Boolean) {
        widgetConfigStateHolder.setPersistentOverlayEnabled(enabled)
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
}
