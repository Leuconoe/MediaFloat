package com.mediacontrol.floatingwidget.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mediacontrol.floatingwidget.AutomationEntryActivity
import com.mediacontrol.floatingwidget.BuildConfig
import com.mediacontrol.floatingwidget.debug.DebugLogEntry
import com.mediacontrol.floatingwidget.debug.DebugLogLevel
import com.mediacontrol.floatingwidget.model.AppPreferences
import com.mediacontrol.floatingwidget.model.CapabilityGrantState
import com.mediacontrol.floatingwidget.model.CapabilityState
import com.mediacontrol.floatingwidget.model.MediaCommand
import com.mediacontrol.floatingwidget.model.MediaSessionErrorReason
import com.mediacontrol.floatingwidget.model.MediaSessionLimitReason
import com.mediacontrol.floatingwidget.model.MediaSessionState
import com.mediacontrol.floatingwidget.model.NotificationPosture
import com.mediacontrol.floatingwidget.model.OverlayRuntimeState
import com.mediacontrol.floatingwidget.model.OverlayUnavailableReason
import com.mediacontrol.floatingwidget.model.PlaybackStatus
import com.mediacontrol.floatingwidget.model.WidgetAnchor
import com.mediacontrol.floatingwidget.model.WidgetButton
import com.mediacontrol.floatingwidget.model.WidgetConfig
import com.mediacontrol.floatingwidget.model.WidgetLayout
import com.mediacontrol.floatingwidget.model.WidgetPosition
import com.mediacontrol.floatingwidget.model.WidgetSizePreset
import com.mediacontrol.floatingwidget.model.supports
import com.mediacontrol.floatingwidget.runtime.RuntimeStatusFormatter
import com.mediacontrol.floatingwidget.state.DebugLogScreenState
import com.mediacontrol.floatingwidget.state.MediaSummaryState
import com.mediacontrol.floatingwidget.state.WidgetConfigScreenState
import com.mediacontrol.floatingwidget.ui.theme.MediaFloatTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

private const val PREVIEW_SCALE = 0.72f
private val SectionCardShape = RoundedCornerShape(28.dp)
private val PanelShape = RoundedCornerShape(26.dp)
private val LogTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, HH:mm:ss")

internal enum class AppSection(
    val title: String,
    val shortTitle: String,
    val description: String
) {
    Landing(
        title = "Overlay landing",
        shortTitle = "Landing",
        description = "Check what MediaFloat needs, confirm the device is ready, and start or stop the overlay without digging through deeper tools."
    ),
    Settings(
        title = "Widget settings",
        shortTitle = "Settings",
        description = "Configure the horizontal overlay family, tune the supported button set, and preview the live shell before you launch it."
    ),
    Debug(
        title = "Debug console",
        shortTitle = "Debug",
        description = "Inspect runtime and media readiness, send real transport commands through the dispatcher, and review recent logs in-app."
    ),
    Support(
        title = "Support and about",
        shortTitle = "Support",
        description = "See setup guidance, version information, product constraints, and the app's lightweight v1 license notices."
    )
}

private fun AppSection.selectorTag(): String = "section-${name.lowercase()}"

private fun AppSection.headerTag(): String = "screen-header-${name.lowercase()}"

internal fun appSections(debugToolsEnabled: Boolean): List<AppSection> {
    return buildList {
        add(AppSection.Landing)
        add(AppSection.Settings)
        add(AppSection.Support)
        if (debugToolsEnabled) {
            add(AppSection.Debug)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppShell(
    appPreferences: AppPreferences = AppPreferences(),
    capabilityState: CapabilityState = previewCapabilityState(),
    runtimeState: OverlayRuntimeState = previewRuntimeState(),
    mediaSummaryState: MediaSummaryState = previewMediaSummaryState(),
    widgetConfigState: WidgetConfigScreenState = previewWidgetConfigState(),
    debugLogState: DebugLogScreenState = previewDebugLogState(),
    onSetDebugToolsEnabled: (Boolean) -> Unit = {},
    onSetVisibleButtons: (Set<WidgetButton>) -> Unit = {},
    onSetSizePreset: (WidgetSizePreset) -> Unit = {},
    onSetPersistentOverlayEnabled: (Boolean) -> Unit = {},
    onStartOverlay: () -> Unit = {},
    onStopOverlay: () -> Unit = {},
    onDispatchPrevious: () -> Unit = {},
    onDispatchPlayPause: () -> Unit = {},
    onDispatchNext: () -> Unit = {},
    onClearLogs: () -> Unit = {},
    onOpenOverlaySettings: () -> Unit = {},
    onOpenNotificationListenerSettings: () -> Unit = {},
    onOpenNotificationSettings: () -> Unit = {},
    automationLaunchAction: String = AutomationEntryActivity.ACTION_SHOW_OVERLAY,
    modifier: Modifier = Modifier
) {
    var selectedSection by rememberSaveable { mutableStateOf(AppSection.Landing) }
    val sections = appSections(debugToolsEnabled = appPreferences.debugToolsEnabled)

    LaunchedEffect(sections, selectedSection) {
        if (selectedSection !in sections) {
            selectedSection = AppSection.Landing
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(text = "MediaFloat")
                        Text(
                            text = selectedSection.shortTitle,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val wideLayout = maxWidth >= 920.dp

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                if (wideLayout) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp, vertical = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        SectionRail(
                            sections = sections,
                            selectedSection = selectedSection,
                            onSectionSelected = { selectedSection = it },
                            modifier = Modifier.width(248.dp)
                        )
                        SectionContent(
                            selectedSection = selectedSection,
                            appPreferences = appPreferences,
                            capabilityState = capabilityState,
                            runtimeState = runtimeState,
                            mediaSummaryState = mediaSummaryState,
                            widgetConfigState = widgetConfigState,
                            debugLogState = debugLogState,
                            onSetDebugToolsEnabled = onSetDebugToolsEnabled,
                            onSetVisibleButtons = onSetVisibleButtons,
                            onSetSizePreset = onSetSizePreset,
                            onSetPersistentOverlayEnabled = onSetPersistentOverlayEnabled,
                            onStartOverlay = onStartOverlay,
                            onStopOverlay = onStopOverlay,
                            onDispatchPrevious = onDispatchPrevious,
                            onDispatchPlayPause = onDispatchPlayPause,
                            onDispatchNext = onDispatchNext,
                            onClearLogs = onClearLogs,
                            onOpenOverlaySettings = onOpenOverlaySettings,
                            onOpenNotificationListenerSettings = onOpenNotificationListenerSettings,
                            onOpenNotificationSettings = onOpenNotificationSettings,
                            automationLaunchAction = automationLaunchAction,
                            wideLayout = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CompactSectionSelector(
                            sections = sections,
                            selectedSection = selectedSection,
                            onSectionSelected = { selectedSection = it }
                        )
                        SectionContent(
                            selectedSection = selectedSection,
                            appPreferences = appPreferences,
                            capabilityState = capabilityState,
                            runtimeState = runtimeState,
                            mediaSummaryState = mediaSummaryState,
                            widgetConfigState = widgetConfigState,
                            debugLogState = debugLogState,
                            onSetDebugToolsEnabled = onSetDebugToolsEnabled,
                            onSetVisibleButtons = onSetVisibleButtons,
                            onSetSizePreset = onSetSizePreset,
                            onSetPersistentOverlayEnabled = onSetPersistentOverlayEnabled,
                            onStartOverlay = onStartOverlay,
                            onStopOverlay = onStopOverlay,
                            onDispatchPrevious = onDispatchPrevious,
                            onDispatchPlayPause = onDispatchPlayPause,
                            onDispatchNext = onDispatchNext,
                            onClearLogs = onClearLogs,
                            onOpenOverlaySettings = onOpenOverlaySettings,
                            onOpenNotificationListenerSettings = onOpenNotificationListenerSettings,
                            onOpenNotificationSettings = onOpenNotificationSettings,
                            automationLaunchAction = automationLaunchAction,
                            wideLayout = false,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionRail(
    sections: List<AppSection>,
    selectedSection: AppSection,
    onSectionSelected: (AppSection) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ScreenHeaderCard(
            title = "Control center",
            detail = "Start from the landing surface to recover readiness quickly, then move into settings or support. Debug stays hidden until it is enabled from Settings.",
            compact = false
        )
        sections.forEach { section ->
            SectionCard(
                title = section.shortTitle,
                detail = section.description,
                selected = section == selectedSection,
                compact = false,
                modifier = Modifier.testTag(section.selectorTag()),
                onClick = { onSectionSelected(section) }
            )
        }
    }
}

@Composable
private fun CompactSectionSelector(
    sections: List<AppSection>,
    selectedSection: AppSection,
    onSectionSelected: (AppSection) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        sections.forEach { section ->
            CompactSectionButton(
                title = section.shortTitle,
                selected = section == selectedSection,
                modifier = Modifier.testTag(section.selectorTag()),
                onClick = { onSectionSelected(section) }
            )
        }
    }
}

@Composable
private fun RowScope.CompactSectionButton(
    title: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = modifier
            .weight(1f)
            .clip(RoundedCornerShape(20.dp))
            .background(containerColor)
            .border(
                width = 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SectionCard(
    title: String,
    detail: String,
    selected: Boolean,
    compact: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = SectionCardShape,
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = contentColor
            )
            if (!compact) {
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor.copy(alpha = 0.86f)
                )
            }
        }
    }
}

@Composable
private fun SectionContent(
    selectedSection: AppSection,
    appPreferences: AppPreferences,
    capabilityState: CapabilityState,
    runtimeState: OverlayRuntimeState,
    mediaSummaryState: MediaSummaryState,
    widgetConfigState: WidgetConfigScreenState,
    debugLogState: DebugLogScreenState,
    onSetDebugToolsEnabled: (Boolean) -> Unit,
    onSetVisibleButtons: (Set<WidgetButton>) -> Unit,
    onSetSizePreset: (WidgetSizePreset) -> Unit,
    onSetPersistentOverlayEnabled: (Boolean) -> Unit,
    onStartOverlay: () -> Unit,
    onStopOverlay: () -> Unit,
    onDispatchPrevious: () -> Unit,
    onDispatchPlayPause: () -> Unit,
    onDispatchNext: () -> Unit,
    onClearLogs: () -> Unit,
    onOpenOverlaySettings: () -> Unit,
    onOpenNotificationListenerSettings: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
    automationLaunchAction: String,
    wideLayout: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 980.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (selectedSection) {
                AppSection.Landing -> LandingScreen(
                    capabilityState = capabilityState,
                    runtimeState = runtimeState,
                    mediaSummaryState = mediaSummaryState,
                    widgetConfigState = widgetConfigState,
                    onStartOverlay = onStartOverlay,
                    onStopOverlay = onStopOverlay,
                    onOpenOverlaySettings = onOpenOverlaySettings,
                    onOpenNotificationListenerSettings = onOpenNotificationListenerSettings,
                    onOpenNotificationSettings = onOpenNotificationSettings,
                    wideLayout = wideLayout
                )

                AppSection.Settings -> SettingsScreen(
                    appPreferences = appPreferences,
                    capabilityState = capabilityState,
                    runtimeState = runtimeState,
                    mediaSummaryState = mediaSummaryState,
                    widgetConfigState = widgetConfigState,
                    onSetDebugToolsEnabled = onSetDebugToolsEnabled,
                    onSetVisibleButtons = onSetVisibleButtons,
                    onSetSizePreset = onSetSizePreset,
                    onSetPersistentOverlayEnabled = onSetPersistentOverlayEnabled,
                    onStartOverlay = onStartOverlay,
                    onStopOverlay = onStopOverlay,
                    onDispatchPrevious = onDispatchPrevious,
                    onDispatchPlayPause = onDispatchPlayPause,
                    onDispatchNext = onDispatchNext,
                    onOpenOverlaySettings = onOpenOverlaySettings,
                    onOpenNotificationListenerSettings = onOpenNotificationListenerSettings,
                    onOpenNotificationSettings = onOpenNotificationSettings,
                    wideLayout = wideLayout
                )

                AppSection.Debug -> DebugScreen(
                    capabilityState = capabilityState,
                    runtimeState = runtimeState,
                    mediaSummaryState = mediaSummaryState,
                    debugLogState = debugLogState,
                    onStartOverlay = onStartOverlay,
                    onStopOverlay = onStopOverlay,
                    onDispatchPrevious = onDispatchPrevious,
                    onDispatchPlayPause = onDispatchPlayPause,
                    onDispatchNext = onDispatchNext,
                    onClearLogs = onClearLogs,
                    onOpenOverlaySettings = onOpenOverlaySettings,
                    onOpenNotificationListenerSettings = onOpenNotificationListenerSettings,
                    onOpenNotificationSettings = onOpenNotificationSettings,
                    wideLayout = wideLayout
                )

                AppSection.Support -> SupportScreen(
                    capabilityState = capabilityState,
                    widgetConfigState = widgetConfigState,
                    debugLogState = debugLogState,
                    onOpenOverlaySettings = onOpenOverlaySettings,
                    onOpenNotificationListenerSettings = onOpenNotificationListenerSettings,
                    onOpenNotificationSettings = onOpenNotificationSettings,
                    automationLaunchAction = automationLaunchAction,
                    wideLayout = wideLayout
                )
            }
        }
    }
}

@Composable
private fun LandingScreen(
    capabilityState: CapabilityState,
    runtimeState: OverlayRuntimeState,
    mediaSummaryState: MediaSummaryState,
    widgetConfigState: WidgetConfigScreenState,
    onStartOverlay: () -> Unit,
    onStopOverlay: () -> Unit,
    onOpenOverlaySettings: () -> Unit,
    onOpenNotificationListenerSettings: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
    wideLayout: Boolean
) {
    val readinessProblems = capabilityState.unavailableReasons()

    ScreenHeaderCard(
        title = AppSection.Landing.title,
        detail = AppSection.Landing.description,
        modifier = Modifier.testTag(AppSection.Landing.headerTag()),
        compact = !wideLayout
    )

    if (wideLayout) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LandingOverviewCard(
                    runtimeState = runtimeState,
                    widgetConfigState = widgetConfigState,
                    readinessProblems = readinessProblems
                )
                RuntimeStatusCard(
                    capabilityState = capabilityState,
                    runtimeState = runtimeState,
                    heading = "Current readiness",
                    supportingLine = landingSupportingLine(
                        runtimeState = runtimeState,
                        readinessProblems = readinessProblems
                    )
                )
                LandingRecoveryCard(
                    readinessProblems = readinessProblems,
                    onOpenOverlaySettings = onOpenOverlaySettings,
                    onOpenNotificationListenerSettings = onOpenNotificationListenerSettings,
                    onOpenNotificationSettings = onOpenNotificationSettings,
                    compact = false
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DebugControlsCard(
                    readyForStart = capabilityState.isReadyForPersistentOverlay(),
                    runtimeState = runtimeState,
                    mediaSummaryState = mediaSummaryState,
                    onStartOverlay = onStartOverlay,
                    onStopOverlay = onStopOverlay,
                    onDispatchPrevious = {},
                    onDispatchPlayPause = {},
                    onDispatchNext = {},
                    title = "Overlay controls",
                    detail = "Use the saved widget setup to start or stop the floating bar right away. If Android blocks startup, recover the missing access from this page and try again.",
                    showTransportControls = false
                )
                MediaStatusCard(mediaSummaryState = mediaSummaryState)
            }
        }
    } else {
        LandingOverviewCard(
            runtimeState = runtimeState,
            widgetConfigState = widgetConfigState,
            readinessProblems = readinessProblems
        )
        RuntimeStatusCard(
            capabilityState = capabilityState,
            runtimeState = runtimeState,
            heading = "Current readiness",
            supportingLine = landingSupportingLine(
                runtimeState = runtimeState,
                readinessProblems = readinessProblems
            )
        )
        DebugControlsCard(
            readyForStart = capabilityState.isReadyForPersistentOverlay(),
            runtimeState = runtimeState,
            mediaSummaryState = mediaSummaryState,
            onStartOverlay = onStartOverlay,
            onStopOverlay = onStopOverlay,
            onDispatchPrevious = {},
            onDispatchPlayPause = {},
            onDispatchNext = {},
            title = "Overlay controls",
            detail = "Start or stop the floating bar here. If Android blocks startup, use the recovery shortcuts below and return to this screen.",
            showTransportControls = false
        )
        LandingRecoveryCard(
            readinessProblems = readinessProblems,
            onOpenOverlaySettings = onOpenOverlaySettings,
            onOpenNotificationListenerSettings = onOpenNotificationListenerSettings,
            onOpenNotificationSettings = onOpenNotificationSettings,
            compact = true
        )
        MediaStatusCard(mediaSummaryState = mediaSummaryState)
    }
}

@Composable
private fun SettingsScreen(
    appPreferences: AppPreferences,
    capabilityState: CapabilityState,
    runtimeState: OverlayRuntimeState,
    mediaSummaryState: MediaSummaryState,
    widgetConfigState: WidgetConfigScreenState,
    onSetDebugToolsEnabled: (Boolean) -> Unit,
    onSetVisibleButtons: (Set<WidgetButton>) -> Unit,
    onSetSizePreset: (WidgetSizePreset) -> Unit,
    onSetPersistentOverlayEnabled: (Boolean) -> Unit,
    onStartOverlay: () -> Unit,
    onStopOverlay: () -> Unit,
    onDispatchPrevious: () -> Unit,
    onDispatchPlayPause: () -> Unit,
    onDispatchNext: () -> Unit,
    onOpenOverlaySettings: () -> Unit,
    onOpenNotificationListenerSettings: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
    wideLayout: Boolean
) {
    ScreenHeaderCard(
        title = AppSection.Settings.title,
        detail = AppSection.Settings.description,
        modifier = Modifier.testTag(AppSection.Settings.headerTag()),
        compact = !wideLayout
    )

    if (wideLayout) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            WidgetPreviewEditorCard(
                config = widgetConfigState.config,
                position = widgetConfigState.position,
                mediaState = mediaSummaryState.mediaState,
                modifier = Modifier.weight(1.08f)
            )
            Column(
                modifier = Modifier.weight(0.92f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ButtonSetEditorCard(
                    config = widgetConfigState.config,
                    onSetVisibleButtons = onSetVisibleButtons
                )
                SizePresetCard(
                    selectedPreset = widgetConfigState.config.sizePreset,
                    onSetSizePreset = onSetSizePreset
                )
                WidgetBehaviorCard(
                    config = widgetConfigState.config,
                    position = widgetConfigState.position,
                    onSetPersistentOverlayEnabled = onSetPersistentOverlayEnabled
                )
                DeveloperOptionsCard(
                    debugToolsEnabled = appPreferences.debugToolsEnabled,
                    onSetDebugToolsEnabled = onSetDebugToolsEnabled
                )
            }
        }
    } else {
        WidgetPreviewEditorCard(
            config = widgetConfigState.config,
            position = widgetConfigState.position,
            mediaState = mediaSummaryState.mediaState
        )
        ButtonSetEditorCard(
            config = widgetConfigState.config,
            onSetVisibleButtons = onSetVisibleButtons
        )
        SizePresetCard(
            selectedPreset = widgetConfigState.config.sizePreset,
            onSetSizePreset = onSetSizePreset
        )
        WidgetBehaviorCard(
            config = widgetConfigState.config,
            position = widgetConfigState.position,
            onSetPersistentOverlayEnabled = onSetPersistentOverlayEnabled
        )
        DeveloperOptionsCard(
            debugToolsEnabled = appPreferences.debugToolsEnabled,
            onSetDebugToolsEnabled = onSetDebugToolsEnabled
        )
    }

    RuntimeStatusCard(
        capabilityState = capabilityState,
        runtimeState = runtimeState,
        heading = "Runtime readiness",
        supportingLine = runtimeSupportingLine(runtimeState)
    )
    DebugControlsCard(
        readyForStart = capabilityState.isReadyForPersistentOverlay(),
        runtimeState = runtimeState,
        mediaSummaryState = mediaSummaryState,
        onStartOverlay = onStartOverlay,
        onStopOverlay = onStopOverlay,
        onDispatchPrevious = onDispatchPrevious,
        onDispatchPlayPause = onDispatchPlayPause,
        onDispatchNext = onDispatchNext,
        title = "Overlay controls",
        detail = "Start or stop the floating overlay here while you tune settings, without jumping over to the debug console.",
        showTransportControls = false
    )
    ReadinessActionsCard(
        readinessProblems = capabilityState.unavailableReasons(),
        onOpenOverlaySettings = onOpenOverlaySettings,
        onOpenNotificationListenerSettings = onOpenNotificationListenerSettings,
        onOpenNotificationSettings = onOpenNotificationSettings,
        compact = !wideLayout
    )
}

@Composable
private fun DebugScreen(
    capabilityState: CapabilityState,
    runtimeState: OverlayRuntimeState,
    mediaSummaryState: MediaSummaryState,
    debugLogState: DebugLogScreenState,
    onStartOverlay: () -> Unit,
    onStopOverlay: () -> Unit,
    onDispatchPrevious: () -> Unit,
    onDispatchPlayPause: () -> Unit,
    onDispatchNext: () -> Unit,
    onClearLogs: () -> Unit,
    onOpenOverlaySettings: () -> Unit,
    onOpenNotificationListenerSettings: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
    wideLayout: Boolean
) {
    ScreenHeaderCard(
        title = AppSection.Debug.title,
        detail = AppSection.Debug.description,
        modifier = Modifier.testTag(AppSection.Debug.headerTag()),
        compact = !wideLayout
    )

    if (wideLayout) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier.weight(0.9f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                RuntimeStatusCard(
                    capabilityState = capabilityState,
                    runtimeState = runtimeState,
                    heading = "Runtime status",
                    supportingLine = runtimeSupportingLine(runtimeState)
                )
                MediaStatusCard(mediaSummaryState = mediaSummaryState)
                DebugControlsCard(
                    readyForStart = capabilityState.isReadyForPersistentOverlay(),
                    runtimeState = runtimeState,
                    mediaSummaryState = mediaSummaryState,
                    onStartOverlay = onStartOverlay,
                    onStopOverlay = onStopOverlay,
                    onDispatchPrevious = onDispatchPrevious,
                    onDispatchPlayPause = onDispatchPlayPause,
                    onDispatchNext = onDispatchNext
                )
                ReadinessActionsCard(
                    readinessProblems = capabilityState.unavailableReasons(),
                    onOpenOverlaySettings = onOpenOverlaySettings,
                    onOpenNotificationListenerSettings = onOpenNotificationListenerSettings,
                    onOpenNotificationSettings = onOpenNotificationSettings,
                    compact = false
                )
            }
            DebugLogViewerCard(
                debugLogState = debugLogState,
                onClearLogs = onClearLogs,
                modifier = Modifier.weight(1.1f)
            )
        }
    } else {
        RuntimeStatusCard(
            capabilityState = capabilityState,
            runtimeState = runtimeState,
            heading = "Runtime status",
            supportingLine = runtimeSupportingLine(runtimeState)
        )
        MediaStatusCard(mediaSummaryState = mediaSummaryState)
        DebugControlsCard(
            readyForStart = capabilityState.isReadyForPersistentOverlay(),
            runtimeState = runtimeState,
            mediaSummaryState = mediaSummaryState,
            onStartOverlay = onStartOverlay,
            onStopOverlay = onStopOverlay,
            onDispatchPrevious = onDispatchPrevious,
            onDispatchPlayPause = onDispatchPlayPause,
            onDispatchNext = onDispatchNext
        )
        ReadinessActionsCard(
            readinessProblems = capabilityState.unavailableReasons(),
            onOpenOverlaySettings = onOpenOverlaySettings,
            onOpenNotificationListenerSettings = onOpenNotificationListenerSettings,
            onOpenNotificationSettings = onOpenNotificationSettings,
            compact = true
        )
        DebugLogViewerCard(
            debugLogState = debugLogState,
            onClearLogs = onClearLogs
        )
    }
}

@Composable
private fun SupportScreen(
    capabilityState: CapabilityState,
    widgetConfigState: WidgetConfigScreenState,
    debugLogState: DebugLogScreenState,
    onOpenOverlaySettings: () -> Unit,
    onOpenNotificationListenerSettings: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
    automationLaunchAction: String,
    wideLayout: Boolean
) {
    ScreenHeaderCard(
        title = AppSection.Support.title,
        detail = AppSection.Support.description,
        modifier = Modifier.testTag(AppSection.Support.headerTag()),
        compact = !wideLayout
    )

    if (wideLayout) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SupportActionsCard(
                    readinessProblems = capabilityState.unavailableReasons(),
                    onOpenOverlaySettings = onOpenOverlaySettings,
                    onOpenNotificationListenerSettings = onOpenNotificationListenerSettings,
                    onOpenNotificationSettings = onOpenNotificationSettings,
                    compact = false
                )
                AboutCard(
                    widgetConfigState = widgetConfigState,
                    automationLaunchAction = automationLaunchAction
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ProductConstraintsCard()
                LicenseCard(retentionLimit = debugLogState.retentionLimit)
            }
        }
    } else {
        SupportActionsCard(
            readinessProblems = capabilityState.unavailableReasons(),
            onOpenOverlaySettings = onOpenOverlaySettings,
            onOpenNotificationListenerSettings = onOpenNotificationListenerSettings,
            onOpenNotificationSettings = onOpenNotificationSettings,
            compact = true
        )
        AboutCard(
            widgetConfigState = widgetConfigState,
            automationLaunchAction = automationLaunchAction
        )
        ProductConstraintsCard()
        LicenseCard(retentionLimit = debugLogState.retentionLimit)
    }
}

@Composable
private fun ScreenHeaderCard(
    title: String,
    detail: String,
    compact: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = SectionCardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.secondaryContainer,
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(if (compact) 18.dp else 22.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = if (compact) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun WidgetPreviewEditorCard(
    config: WidgetConfig,
    position: WidgetPosition,
    mediaState: MediaSessionState,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = PanelShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Live widget preview",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "This constrained stage mirrors the real horizontal overlay family: supported button sets only, preset sizing only, and a fixed right-side drag handle.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            WidgetPreviewStage(
                config = config,
                position = position,
                mediaState = mediaState
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricPill(label = "Size", value = config.sizePreset.displayTitle())
                MetricPill(label = "Buttons", value = config.layout.summaryLabel())
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricPill(label = "Saved edge", value = position.anchor.displayLabel())
                MetricPill(label = "Offset", value = "${position.xOffsetDp}dp x ${position.yOffsetDp}dp")
            }
        }
    }
}

@Composable
private fun WidgetPreviewStage(
    config: WidgetConfig,
    position: WidgetPosition,
    mediaState: MediaSessionState,
    modifier: Modifier = Modifier
) {
    val scaledWidth = (config.sizePreset.widthDp * PREVIEW_SCALE).roundToInt().dp
    val scaledHeight = (config.sizePreset.heightDp * PREVIEW_SCALE).roundToInt().dp
    val scaledTop = (position.yOffsetDp * PREVIEW_SCALE * 0.58f).roundToInt().coerceIn(24, 180).dp
    val overlayAlignment = if (position.anchor == WidgetAnchor.Start) Alignment.TopStart else Alignment.TopEnd

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(320.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.secondaryContainer
                    )
                )
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(32.dp)
            )
            .padding(18.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                .padding(18.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(overlayAlignment)
                    .padding(top = scaledTop)
                    .width(scaledWidth)
                    .height(scaledHeight)
            ) {
                PreviewOverlayBar(
                    config = config,
                    mediaState = mediaState,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun PreviewOverlayBar(
    config: WidgetConfig,
    mediaState: MediaSessionState,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.94f))
            .padding(start = 10.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        config.layout.orderedButtons.forEach { button ->
            PreviewOverlayButton(
                button = button,
                mediaState = mediaState,
                modifier = Modifier.weight(1f)
            )
        }
        Box(
            modifier = Modifier
                .width(22.dp)
                .height(28.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.tertiaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "|||",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun PreviewOverlayButton(
    button: WidgetButton,
    mediaState: MediaSessionState,
    modifier: Modifier = Modifier
) {
    val command = button.toMediaCommand()
    val enabled = mediaState.supports(command)
    val label = when (button) {
        WidgetButton.Previous -> "Prev"
        WidgetButton.PlayPause -> if ((mediaState as? MediaSessionState.Active)?.playbackStatus == PlaybackStatus.Playing) "Pause" else "Play"
        WidgetButton.Next -> "Next"
    }

    Box(
        modifier = modifier
            .heightIn(min = 28.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(
                if (enabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
            )
            .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (enabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

@Composable
private fun ButtonSetEditorCard(
    config: WidgetConfig,
    onSetVisibleButtons: (Set<WidgetButton>) -> Unit,
    modifier: Modifier = Modifier
) {
    val hasPrevious = WidgetButton.Previous in config.layout.visibleButtons
    val hasNext = WidgetButton.Next in config.layout.visibleButtons

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = PanelShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Visible buttons",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Play / pause stays in the center of every supported layout. Add previous and next only when you want them in the live bar.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            ToggleOptionCard(
                title = "Previous",
                detail = "Shows the previous-track command on the left edge.",
                selected = hasPrevious,
                enabled = true,
                onClick = {
                    onSetVisibleButtons(buildVisibleButtons(includePrevious = !hasPrevious, includeNext = hasNext))
                }
            )
            ToggleOptionCard(
                title = "Play / pause",
                detail = "Always included so the overlay keeps a consistent supported layout.",
                selected = true,
                enabled = false,
                onClick = {}
            )
            ToggleOptionCard(
                title = "Next",
                detail = "Shows the next-track command before the drag handle.",
                selected = hasNext,
                enabled = true,
                onClick = {
                    onSetVisibleButtons(buildVisibleButtons(includePrevious = hasPrevious, includeNext = !hasNext))
                }
            )
        }
    }
}

@Composable
private fun ToggleOptionCard(
    title: String,
    detail: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = when {
        !enabled -> MaterialTheme.colorScheme.surfaceVariant
        selected -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surface
    }
    val contentColor = when {
        !enabled -> MaterialTheme.colorScheme.onSurfaceVariant
        selected -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = contentColor
                )
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.84f)
                )
            }
            Text(
                text = when {
                    !enabled -> "Fixed"
                    selected -> "On"
                    else -> "Off"
                },
                style = MaterialTheme.typography.labelLarge,
                color = contentColor
            )
        }
    }
}

@Composable
private fun SizePresetCard(
    selectedPreset: WidgetSizePreset,
    onSetSizePreset: (WidgetSizePreset) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = PanelShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Size preset",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Switch between the supported overlay shells instead of freeform resizing. The preview updates immediately so the live bar stays predictable.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            WidgetSizePreset.entries.forEach { preset ->
                ToggleOptionCard(
                    title = preset.displayTitle(),
                    detail = "${preset.widthDp}dp wide x ${preset.heightDp}dp tall",
                    selected = preset == selectedPreset,
                    enabled = true,
                    onClick = { onSetSizePreset(preset) }
                )
            }
        }
    }
}

@Composable
private fun LandingOverviewCard(
    runtimeState: OverlayRuntimeState,
    widgetConfigState: WidgetConfigScreenState,
    readinessProblems: List<OverlayUnavailableReason>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = PanelShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "What MediaFloat does",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "MediaFloat keeps previous, play / pause, and next in a small movable overlay so transport controls stay close while you use other apps.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricPill(
                    label = "Runtime",
                    value = runtimeState.landingStatusLabel(),
                    modifier = Modifier.weight(1f)
                )
                MetricPill(
                    label = "Buttons",
                    value = widgetConfigState.config.layout.orderedButtons.size.toString(),
                    modifier = Modifier.weight(1f)
                )
                MetricPill(
                    label = "Size",
                    value = widgetConfigState.config.sizePreset.displayTitle(),
                    modifier = Modifier.weight(1f)
                )
            }
            Text(
                text = when (runtimeState) {
                    OverlayRuntimeState.Ready -> "System access is aligned, so you can start the overlay from this page."
                    is OverlayRuntimeState.Showing -> "The overlay is already live. Stop it here or move into Settings to adjust the saved shell."
                    is OverlayRuntimeState.Suspended -> "The runtime paused after startup. Recover the missing access and try the overlay again."
                    is OverlayRuntimeState.Unavailable -> "A required system condition is missing. Use the recovery shortcuts below to unblock startup."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LandingRecoveryCard(
    readinessProblems: List<OverlayUnavailableReason>,
    onOpenOverlaySettings: () -> Unit,
    onOpenNotificationListenerSettings: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
    compact: Boolean,
    modifier: Modifier = Modifier
) {
    val actions = readinessProblems.toRecoveryActions(
        onOpenOverlaySettings = onOpenOverlaySettings,
        onOpenNotificationListenerSettings = onOpenNotificationListenerSettings,
        onOpenNotificationSettings = onOpenNotificationSettings
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = PanelShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Recovery shortcuts",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Text(
                text = if (actions.isEmpty()) {
                    "No blocked system access is detected right now. If Android changes something later, the Settings and Support sections still keep the full recovery links."
                } else {
                    "Open the missing system access page, return here, and try the overlay again. Only the currently relevant recovery routes are shown."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            readinessProblems.forEach { reason ->
                Text(
                    text = reason.toDisplayLine(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
            if (actions.isNotEmpty()) {
                if (compact || actions.size == 1) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        actions.forEach { action ->
                            OutlinedButton(
                                onClick = action.onClick,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = action.label)
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        actions.forEach { action ->
                            OutlinedButton(
                                onClick = action.onClick,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = action.label)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DeveloperOptionsCard(
    debugToolsEnabled: Boolean,
    onSetDebugToolsEnabled: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = PanelShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Developer tools",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Keep the Debug section hidden unless you need runtime inspection, transport dispatches, or recent log visibility. This setting is saved on the device.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Show Debug section",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (debugToolsEnabled) {
                            "Debug is visible in the section selector."
                        } else {
                            "Debug stays out of the main shell until you turn it on."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = debugToolsEnabled,
                    onCheckedChange = onSetDebugToolsEnabled
                )
            }
        }
    }
}

@Composable
private fun WidgetBehaviorCard(
    config: WidgetConfig,
    position: WidgetPosition,
    onSetPersistentOverlayEnabled: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = PanelShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Behavior",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Persistent overlay",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Keep the runtime preference aligned with the saved widget configuration.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = config.persistentOverlayEnabled,
                    onCheckedChange = onSetPersistentOverlayEnabled
                )
            }
            HorizontalDivider()
            PropertyLine(label = "Current edge", value = position.anchor.displayLabel())
            PropertyLine(label = "Horizontal offset", value = "${position.xOffsetDp}dp")
            PropertyLine(label = "Vertical offset", value = "${position.yOffsetDp}dp")
            Text(
                text = "The overlay itself still owns drag positioning. This screen previews the saved shape without turning into a freeform editor.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RuntimeStatusCard(
    capabilityState: CapabilityState,
    runtimeState: OverlayRuntimeState,
    heading: String,
    supportingLine: String,
    modifier: Modifier = Modifier
) {
    val runtimeSummary = RuntimeStatusFormatter.format(
        capabilityState = capabilityState,
        runtimeState = runtimeState
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = PanelShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = heading,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = runtimeSummary.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = runtimeSummary.detail,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = supportingLine,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.84f)
            )
            runtimeSummary.capabilityLines.forEach { line ->
                Text(
                    text = line,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun ReadinessActionsCard(
    readinessProblems: List<OverlayUnavailableReason>,
    onOpenOverlaySettings: () -> Unit,
    onOpenNotificationListenerSettings: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
    compact: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = PanelShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "System access",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Text(
                text = if (readinessProblems.isEmpty()) {
                    "Everything needed for the persistent overlay runtime is currently ready."
                } else {
                    "Use these system links to unblock the runtime when a permission or notification posture changes."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            readinessProblems.forEach { reason ->
                Text(
                    text = reason.toDisplayLine(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
            if (compact) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onOpenOverlaySettings, modifier = Modifier.fillMaxWidth()) {
                        Text(text = "Overlay access")
                    }
                    OutlinedButton(onClick = onOpenNotificationListenerSettings, modifier = Modifier.fillMaxWidth()) {
                        Text(text = "Listener access")
                    }
                    OutlinedButton(onClick = onOpenNotificationSettings, modifier = Modifier.fillMaxWidth()) {
                        Text(text = "Notifications")
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(onClick = onOpenOverlaySettings, modifier = Modifier.weight(1f)) {
                        Text(text = "Overlay access")
                    }
                    OutlinedButton(onClick = onOpenNotificationListenerSettings, modifier = Modifier.weight(1f)) {
                        Text(text = "Listener access")
                    }
                    OutlinedButton(onClick = onOpenNotificationSettings, modifier = Modifier.weight(1f)) {
                        Text(text = "Notifications")
                    }
                }
            }
        }
    }
}

@Composable
private fun MediaStatusCard(
    mediaSummaryState: MediaSummaryState,
    modifier: Modifier = Modifier
) {
    val title = mediaSummaryState.mediaState.title()
    val detail = mediaSummaryState.mediaState.detail()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = PanelShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Media readiness",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            mediaSummaryState.mediaState.supportingLines().forEach { line ->
                Text(
                    text = line,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Text(
                text = "Previous ${mediaSummaryState.previousEnabled.readinessLabel()}  Play / pause ${mediaSummaryState.playPauseEnabled.readinessLabel()}  Next ${mediaSummaryState.nextEnabled.readinessLabel()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun DebugControlsCard(
    readyForStart: Boolean,
    runtimeState: OverlayRuntimeState,
    mediaSummaryState: MediaSummaryState,
    onStartOverlay: () -> Unit,
    onStopOverlay: () -> Unit,
    onDispatchPrevious: () -> Unit,
    onDispatchPlayPause: () -> Unit,
    onDispatchNext: () -> Unit,
    title: String = "Debug controls",
    detail: String = "These buttons travel through the same app services and dispatcher path as the runtime, so they double as a practical test surface when overlay touch input is unreliable.",
    showTransportControls: Boolean = true,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = PanelShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onStartOverlay,
                    enabled = readyForStart,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Start overlay")
                }
                OutlinedButton(
                    onClick = onStopOverlay,
                    enabled = runtimeState is OverlayRuntimeState.Showing,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Stop overlay")
                }
            }
            if (showTransportControls) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDispatchPrevious,
                        enabled = mediaSummaryState.previousEnabled,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Prev")
                    }
                    Button(
                        onClick = onDispatchPlayPause,
                        enabled = mediaSummaryState.playPauseEnabled,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Play / pause")
                    }
                    OutlinedButton(
                        onClick = onDispatchNext,
                        enabled = mediaSummaryState.nextEnabled,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Next")
                    }
                }
            }
        }
    }
}

@Composable
private fun DebugLogViewerCard(
    debugLogState: DebugLogScreenState,
    onClearLogs: () -> Unit,
    modifier: Modifier = Modifier
) {
    val entries = debugLogState.entries.takeLast(60).asReversed()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = PanelShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Recent debug log",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Showing ${entries.size} of ${debugLogState.entries.size} entries. Retention limit ${debugLogState.retentionLimit}.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                OutlinedButton(onClick = onClearLogs) {
                    Text(text = "Clear")
                }
            }

            if (entries.isEmpty()) {
                Text(
                    text = "No log entries yet. Runtime transitions, media dispatches, widget saves, and debug actions will appear here.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                entries.forEachIndexed { index, entry ->
                    if (index > 0) {
                        HorizontalDivider()
                    }
                    LogEntryRow(entry = entry)
                }
            }
        }
    }
}

@Composable
private fun LogEntryRow(entry: DebugLogEntry, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(entry.level.badgeColor())
                .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Text(
                text = entry.level.name.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = entry.level.badgeContentColor()
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "${entry.tag}  ${entry.timestampLabel()}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = entry.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            entry.details?.takeIf { it.isNotBlank() }?.let { details ->
                Text(
                    text = details,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SupportActionsCard(
    readinessProblems: List<OverlayUnavailableReason>,
    onOpenOverlaySettings: () -> Unit,
    onOpenNotificationListenerSettings: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
    compact: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = PanelShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Setup help",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "If the overlay does not appear, confirm display-over-apps access, notification listener access, and visible app notifications. If you enable Debug from Settings, the debug console can still send real media commands while you verify the runtime.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (readinessProblems.isNotEmpty()) {
                readinessProblems.forEach { problem ->
                    Text(
                        text = problem.toDisplayLine(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (compact) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onOpenOverlaySettings, modifier = Modifier.fillMaxWidth()) {
                        Text(text = "Open overlay access")
                    }
                    OutlinedButton(onClick = onOpenNotificationListenerSettings, modifier = Modifier.fillMaxWidth()) {
                        Text(text = "Open listener access")
                    }
                    OutlinedButton(onClick = onOpenNotificationSettings, modifier = Modifier.fillMaxWidth()) {
                        Text(text = "Open notifications")
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(onClick = onOpenOverlaySettings, modifier = Modifier.weight(1f)) {
                        Text(text = "Overlay access")
                    }
                    OutlinedButton(onClick = onOpenNotificationListenerSettings, modifier = Modifier.weight(1f)) {
                        Text(text = "Listener access")
                    }
                    OutlinedButton(onClick = onOpenNotificationSettings, modifier = Modifier.weight(1f)) {
                        Text(text = "Notifications")
                    }
                }
            }
        }
    }
}

@Composable
private fun AboutCard(
    widgetConfigState: WidgetConfigScreenState,
    automationLaunchAction: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = PanelShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "App info",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            PropertyLine(
                label = "Version",
                value = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                supportingColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
            )
            PropertyLine(
                label = "Package",
                value = BuildConfig.APPLICATION_ID,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                supportingColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
            )
            PropertyLine(
                label = "Automation action",
                value = automationLaunchAction,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                supportingColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
            )
            PropertyLine(
                label = "Saved widget",
                value = "${widgetConfigState.config.sizePreset.displayTitle()} / ${widgetConfigState.config.layout.summaryLabel()}",
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                supportingColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
            )
            Text(
                text = "This release focuses on a single horizontal overlay family so configuration stays deliberate, testable, and consistent with the WindowManager runtime.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun ProductConstraintsCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = PanelShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Product constraints",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "- Horizontal overlay family only\n- Right-side drag handle only\n- Size presets instead of freeform resizing\n- Button sets limited to supported transport layouts",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "Those constraints keep the overlay predictable across phones and tablets while the optional debug console exercises the same real runtime path.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.84f)
            )
        }
    }
}

@Composable
private fun LicenseCard(
    retentionLimit: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = PanelShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Licenses",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Reasonable v1 notice summary for the libraries visible in this single-module app:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "AndroidX Activity Compose, Lifecycle Runtime Compose, Compose UI, and Material3 are distributed under Apache License 2.0.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Google Material Components are distributed under Apache License 2.0.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Kotlin standard library components are distributed under Apache License 2.0.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Android platform media and overlay APIs are used through the Android SDK terms rather than bundled third-party source.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Current in-app debug log retention: $retentionLimit entries.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MetricPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun PropertyLine(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    color: Color? = null,
    supportingColor: Color? = null
) {
    val resolvedColor = color ?: MaterialTheme.colorScheme.onSurface
    val resolvedSupportingColor = supportingColor ?: MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = resolvedSupportingColor
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = resolvedColor
        )
    }
}

private data class RecoveryAction(
    val label: String,
    val onClick: () -> Unit
)

private fun buildVisibleButtons(includePrevious: Boolean, includeNext: Boolean): Set<WidgetButton> {
    return buildSet {
        if (includePrevious) {
            add(WidgetButton.Previous)
        }
        add(WidgetButton.PlayPause)
        if (includeNext) {
            add(WidgetButton.Next)
        }
    }
}

private fun runtimeSupportingLine(runtimeState: OverlayRuntimeState): String {
    return when (runtimeState) {
        OverlayRuntimeState.Ready -> "The foreground service can be started from the landing screen, Settings, or the automation entry activity."
        is OverlayRuntimeState.Showing -> {
            "Live layout ${runtimeState.layout.summaryLabel()} on the ${runtimeState.position.anchor.displayLabel().lowercase()} edge."
        }
        is OverlayRuntimeState.Suspended -> "The runtime is paused and waiting for recovery."
        is OverlayRuntimeState.Unavailable -> "Resolve the blocking capability, then start the overlay again."
    }
}

internal fun landingSupportingLine(
    runtimeState: OverlayRuntimeState,
    readinessProblems: List<OverlayUnavailableReason>
): String {
    return when {
        readinessProblems.isEmpty() && runtimeState is OverlayRuntimeState.Showing -> {
            "The overlay is already visible. Stop it here or head into Settings to change the saved shell."
        }
        readinessProblems.isEmpty() -> {
            "Everything required for startup is aligned. You can launch the overlay from this screen or through automation."
        }
        else -> {
            "Recover the missing system access below, come back here, and try the overlay again."
        }
    }
}

internal fun OverlayRuntimeState.landingStatusLabel(): String {
    return when (this) {
        OverlayRuntimeState.Ready -> "Ready"
        is OverlayRuntimeState.Showing -> "Showing"
        is OverlayRuntimeState.Suspended -> "Paused"
        is OverlayRuntimeState.Unavailable -> "Blocked"
    }
}

private fun List<OverlayUnavailableReason>.toRecoveryActions(
    onOpenOverlaySettings: () -> Unit,
    onOpenNotificationListenerSettings: () -> Unit,
    onOpenNotificationSettings: () -> Unit
): List<RecoveryAction> {
    return buildList {
        if (OverlayUnavailableReason.MissingOverlayAccess in this@toRecoveryActions) {
            add(RecoveryAction(label = "Open overlay access", onClick = onOpenOverlaySettings))
        }
        if (OverlayUnavailableReason.MissingNotificationListenerAccess in this@toRecoveryActions) {
            add(RecoveryAction(label = "Open listener access", onClick = onOpenNotificationListenerSettings))
        }
        if (OverlayUnavailableReason.NotificationPostureBlocked in this@toRecoveryActions) {
            add(RecoveryAction(label = "Open notifications", onClick = onOpenNotificationSettings))
        }
    }
}

private fun OverlayUnavailableReason.toDisplayLine(): String {
    return when (this) {
        OverlayUnavailableReason.MissingOverlayAccess -> "- Grant display over other apps access"
        OverlayUnavailableReason.MissingNotificationListenerAccess -> "- Grant notification listener access"
        OverlayUnavailableReason.NotificationPostureBlocked -> "- Restore app notification visibility"
        OverlayUnavailableReason.ServiceStartNotAllowed -> "- Check foreground service eligibility"
        OverlayUnavailableReason.UnsupportedDeviceCondition -> "- Device conditions are unsupported for this runtime"
        OverlayUnavailableReason.UnknownRuntimeFailure -> "- Resolve the last runtime failure"
    }
}

private fun WidgetLayout.summaryLabel(): String {
    return orderedButtons.joinToString(separator = " + ") { it.displayLabel() }
}

private fun WidgetButton.displayLabel(): String {
    return when (this) {
        WidgetButton.Previous -> "Previous"
        WidgetButton.PlayPause -> "Play / pause"
        WidgetButton.Next -> "Next"
    }
}

private fun WidgetSizePreset.displayTitle(): String {
    return when (this) {
        WidgetSizePreset.Compact -> "Compact"
        WidgetSizePreset.Standard -> "Standard"
        WidgetSizePreset.Large -> "Large"
    }
}

private fun WidgetAnchor.displayLabel(): String {
    return when (this) {
        WidgetAnchor.Start -> "Left"
        WidgetAnchor.End -> "Right"
    }
}

private fun WidgetButton.toMediaCommand(): MediaCommand {
    return when (this) {
        WidgetButton.Previous -> MediaCommand.Previous
        WidgetButton.PlayPause -> MediaCommand.TogglePlayPause
        WidgetButton.Next -> MediaCommand.Next
    }
}

private fun Boolean.readinessLabel(): String {
    return if (this) "ready" else "blocked"
}

private fun MediaSessionState.title(): String {
    return when (this) {
        MediaSessionState.Discovering -> "Looking for an active session"
        MediaSessionState.Unavailable -> "No active media session"
        is MediaSessionState.Active -> "Active media session"
        is MediaSessionState.Limited -> "Limited media session"
        is MediaSessionState.Error -> "Media session error"
    }
}

private fun MediaSessionState.detail(): String {
    return when (this) {
        MediaSessionState.Discovering -> "The repository is tracking notification listener state and searching for the best controller."
        MediaSessionState.Unavailable -> "Open a media app and start playback so transport controls become available."
        is MediaSessionState.Active -> "Session ${sessionId.take(36)} is available and can update transport support live as playback changes."
        is MediaSessionState.Limited -> "A session is present, but one or more transport controls are currently unavailable."
        is MediaSessionState.Error -> "The repository reported an error while reading media session state."
    }
}

private fun MediaSessionState.supportingLines(): List<String> {
    return when (this) {
        MediaSessionState.Discovering -> emptyList()
        MediaSessionState.Unavailable -> emptyList()
        is MediaSessionState.Active -> listOf(
            "Playback: ${playbackStatus.displayLabel()}",
            "Supported: ${supportedActions.joinToString { it.displayLabel() }}"
        )
        is MediaSessionState.Limited -> listOf(
            "Limit: ${reason.displayLabel()}",
            "Supported: ${supportedActions.joinToString { it.displayLabel() }}"
        )
        is MediaSessionState.Error -> listOf(
            "Reason: ${reason.displayLabel()}"
        )
    }
}

private fun MediaCommand.displayLabel(): String {
    return when (this) {
        MediaCommand.Previous -> "Previous"
        MediaCommand.TogglePlayPause -> "Play / pause"
        MediaCommand.Next -> "Next"
    }
}

private fun PlaybackStatus.displayLabel(): String {
    return when (this) {
        PlaybackStatus.Playing -> "Playing"
        PlaybackStatus.Paused -> "Paused"
        PlaybackStatus.Buffering -> "Buffering"
        PlaybackStatus.Stopped -> "Stopped"
        PlaybackStatus.Unknown -> "Unknown"
    }
}

private fun MediaSessionLimitReason.displayLabel(): String {
    return when (this) {
        MediaSessionLimitReason.MissingTransportControls -> "Missing transport controls"
        MediaSessionLimitReason.PlaybackStateUnknown -> "Playback state unknown"
        MediaSessionLimitReason.SessionChanging -> "Session changing"
    }
}

private fun MediaSessionErrorReason.displayLabel(): String {
    return when (this) {
        MediaSessionErrorReason.PermissionRevoked -> "Permission revoked"
        MediaSessionErrorReason.PlatformFailure -> "Platform failure"
        MediaSessionErrorReason.Unknown -> "Unknown"
    }
}

@Composable
private fun DebugLogLevel.badgeColor(): androidx.compose.ui.graphics.Color {
    return when (this) {
        DebugLogLevel.Debug -> MaterialTheme.colorScheme.surfaceVariant
        DebugLogLevel.Info -> MaterialTheme.colorScheme.secondaryContainer
        DebugLogLevel.Warning -> MaterialTheme.colorScheme.tertiaryContainer
        DebugLogLevel.Error -> MaterialTheme.colorScheme.errorContainer
    }
}

@Composable
private fun DebugLogLevel.badgeContentColor(): androidx.compose.ui.graphics.Color {
    return when (this) {
        DebugLogLevel.Debug -> MaterialTheme.colorScheme.onSurfaceVariant
        DebugLogLevel.Info -> MaterialTheme.colorScheme.onSecondaryContainer
        DebugLogLevel.Warning -> MaterialTheme.colorScheme.onTertiaryContainer
        DebugLogLevel.Error -> MaterialTheme.colorScheme.onErrorContainer
    }
}

private fun DebugLogEntry.timestampLabel(): String {
    return Instant.ofEpochMilli(timestampEpochMillis)
        .atZone(ZoneId.systemDefault())
        .format(LogTimeFormatter)
}

@Preview(showBackground = true, widthDp = 412, heightDp = 960)
@Composable
private fun AppShellPhonePreview() {
            MediaFloatTheme {
        AppShell()
    }
}

@Preview(showBackground = true, widthDp = 1180, heightDp = 900)
@Composable
private fun AppShellTabletPreview() {
            MediaFloatTheme {
        AppShell()
    }
}

private fun previewCapabilityState(): CapabilityState {
    return CapabilityState(
        overlayAccess = CapabilityGrantState.Missing,
        notificationListenerAccess = CapabilityGrantState.Granted,
        notificationPosture = NotificationPosture.Visible,
        serviceStartReadiness = CapabilityGrantState.Granted
    )
}

private fun previewRuntimeState(): OverlayRuntimeState {
    return OverlayRuntimeState.Showing(
        position = WidgetPosition(anchor = WidgetAnchor.End, xOffsetDp = 24, yOffsetDp = 156),
        layout = WidgetLayout.Default,
        mediaState = MediaSessionState.Active(
            sessionId = "preview-session",
            supportedActions = setOf(MediaCommand.Previous, MediaCommand.TogglePlayPause, MediaCommand.Next),
            playbackStatus = PlaybackStatus.Playing
        )
    )
}

private fun previewMediaSummaryState(): MediaSummaryState {
    return MediaSummaryState(
        mediaState = MediaSessionState.Active(
            sessionId = "preview-session",
            supportedActions = setOf(MediaCommand.Previous, MediaCommand.TogglePlayPause, MediaCommand.Next),
            playbackStatus = PlaybackStatus.Playing
        ),
        previousEnabled = true,
        playPauseEnabled = true,
        nextEnabled = true
    )
}

private fun previewWidgetConfigState(): WidgetConfigScreenState {
    return WidgetConfigScreenState(
        config = WidgetConfig(
            layout = WidgetLayout(visibleButtons = setOf(WidgetButton.Previous, WidgetButton.PlayPause, WidgetButton.Next)),
            sizePreset = WidgetSizePreset.Standard,
            persistentOverlayEnabled = true
        ),
        position = WidgetPosition(anchor = WidgetAnchor.End, xOffsetDp = 24, yOffsetDp = 156)
    )
}

private fun previewDebugLogState(): DebugLogScreenState {
    return DebugLogScreenState(
        retentionLimit = 200,
        entries = listOf(
            DebugLogEntry(
                timestampEpochMillis = 1_711_000_000_000,
                level = DebugLogLevel.Info,
                tag = "OverlayRuntime",
                message = "Overlay showing",
                details = "buttons=Previous, PlayPause, Next size=Standard"
            ),
            DebugLogEntry(
                timestampEpochMillis = 1_711_000_030_000,
                level = DebugLogLevel.Debug,
                tag = "AppDebugActions",
                message = "In-app media debug action",
                details = "command=TogglePlayPause dispatched=true"
            )
        )
    )
}
