package sw2.io.mediafloat.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
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
import androidx.compose.foundation.selection.toggleable
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
import androidx.compose.material3.Slider
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import sw2.io.mediafloat.AutomationEntryActivity
import sw2.io.mediafloat.BuildConfig
import sw2.io.mediafloat.R
import sw2.io.mediafloat.debug.DebugLogEntry
import sw2.io.mediafloat.debug.DebugLogLevel
import sw2.io.mediafloat.model.AppLanguage
import sw2.io.mediafloat.model.AppPreferences
import sw2.io.mediafloat.model.CapabilityGrantState
import sw2.io.mediafloat.model.CapabilityState
import sw2.io.mediafloat.model.MediaCommand
import sw2.io.mediafloat.model.MediaArtwork
import sw2.io.mediafloat.model.MediaArtworkSource
import sw2.io.mediafloat.model.MediaSessionErrorReason
import sw2.io.mediafloat.model.MediaSessionLimitReason
import sw2.io.mediafloat.model.MediaSessionState
import sw2.io.mediafloat.model.NotificationPosture
import sw2.io.mediafloat.model.OverlayRuntimeState
import sw2.io.mediafloat.model.OverlayUnavailableReason
import sw2.io.mediafloat.model.PlaybackStatus
import sw2.io.mediafloat.model.WidgetAnchor
import sw2.io.mediafloat.model.WidgetButton
import sw2.io.mediafloat.model.WidgetConfig
import sw2.io.mediafloat.model.DragHandlePlacement
import sw2.io.mediafloat.model.WidgetLayout
import sw2.io.mediafloat.model.WidgetPosition
import sw2.io.mediafloat.model.WidgetSizePreset
import sw2.io.mediafloat.model.WidgetThemePreset
import sw2.io.mediafloat.model.WidgetWidthStyle
import sw2.io.mediafloat.model.currentTitle
import sw2.io.mediafloat.model.overlayAppearance
import sw2.io.mediafloat.model.supports
import sw2.io.mediafloat.overlay.bodyWidthDp
import sw2.io.mediafloat.overlay.resolveOverlayThumbnailPresentation
import sw2.io.mediafloat.runtime.RuntimeStatusFormatter
import sw2.io.mediafloat.state.DebugLogScreenState
import sw2.io.mediafloat.state.MediaSummaryState
import sw2.io.mediafloat.state.WidgetConfigScreenState
import sw2.io.mediafloat.ui.theme.MediaFloatTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt
import androidx.annotation.StringRes

private const val PREVIEW_SCALE = 0.72f
private val SectionCardShape = RoundedCornerShape(28.dp)
private val PanelShape = RoundedCornerShape(26.dp)
private val LogTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, HH:mm:ss")

private fun previewScaledDp(value: Int): Dp = (value * PREVIEW_SCALE).roundToInt().dp

internal enum class AppSection(
    @StringRes val titleRes: Int,
    @StringRes val shortTitleRes: Int,
    @StringRes val descriptionRes: Int
) {
    Landing(
        titleRes = R.string.section_landing_title,
        shortTitleRes = R.string.section_landing_short,
        descriptionRes = R.string.section_landing_description
    ),
    Settings(
        titleRes = R.string.section_settings_title,
        shortTitleRes = R.string.section_settings_short,
        descriptionRes = R.string.section_settings_description
    ),
    Advanced(
        titleRes = R.string.section_advanced_title,
        shortTitleRes = R.string.section_advanced_short,
        descriptionRes = R.string.section_advanced_description
    ),
    Debug(
        titleRes = R.string.section_debug_title,
        shortTitleRes = R.string.section_debug_short,
        descriptionRes = R.string.section_debug_description
    ),
    Support(
        titleRes = R.string.section_support_title,
        shortTitleRes = R.string.section_support_short,
        descriptionRes = R.string.section_support_description
    )
}

private fun AppSection.selectorTag(): String = "section-${name.lowercase()}"

private fun AppSection.headerTag(): String = "screen-header-${name.lowercase()}"

internal fun appSections(debugToolsEnabled: Boolean): List<AppSection> {
    return buildList {
        add(AppSection.Landing)
        add(AppSection.Settings)
        add(AppSection.Advanced)
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
    onSetAppLanguage: (AppLanguage) -> Unit = {},
    onSetVisibleButtons: (Set<WidgetButton>) -> Unit = {},
    onSetSizePreset: (WidgetSizePreset) -> Unit = {},
    onSetWidthStyle: (WidgetWidthStyle) -> Unit = {},
    onSetThemePreset: (WidgetThemePreset) -> Unit = {},
    onSetOpacity: (Float) -> Unit = {},
    onSetDragHandlePlacement: (DragHandlePlacement) -> Unit = {},
    onSetHorizontalOffsetPreset: (Int) -> Unit = {},
    onSetPersistentOverlayEnabled: (Boolean) -> Unit = {},
    onSetLowQualityThumbnailFallbackEnabled: (Boolean) -> Unit = {},
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
                        Text(text = stringResource(id = R.string.app_name))
                        Text(
                            text = stringResource(id = selectedSection.shortTitleRes),
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
                            onSetAppLanguage = onSetAppLanguage,
                            onSetVisibleButtons = onSetVisibleButtons,
                            onSetSizePreset = onSetSizePreset,
                            onSetWidthStyle = onSetWidthStyle,
                            onSetThemePreset = onSetThemePreset,
                            onSetOpacity = onSetOpacity,
                            onSetDragHandlePlacement = onSetDragHandlePlacement,
                            onSetHorizontalOffsetPreset = onSetHorizontalOffsetPreset,
                            onSetPersistentOverlayEnabled = onSetPersistentOverlayEnabled,
                            onSetLowQualityThumbnailFallbackEnabled = onSetLowQualityThumbnailFallbackEnabled,
                            onStartOverlay = onStartOverlay,
                            onStopOverlay = onStopOverlay,
                            onDispatchPrevious = onDispatchPrevious,
                            onDispatchPlayPause = onDispatchPlayPause,
                            onDispatchNext = onDispatchNext,
                            onClearLogs = onClearLogs,
                            onOpenOverlaySettings = onOpenOverlaySettings,
                            onOpenNotificationListenerSettings = onOpenNotificationListenerSettings,
                            onOpenNotificationSettings = onOpenNotificationSettings,
                            onOpenSupport = { selectedSection = AppSection.Support },
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
                            onSetAppLanguage = onSetAppLanguage,
                            onSetVisibleButtons = onSetVisibleButtons,
                            onSetSizePreset = onSetSizePreset,
                            onSetWidthStyle = onSetWidthStyle,
                            onSetThemePreset = onSetThemePreset,
                            onSetOpacity = onSetOpacity,
                            onSetDragHandlePlacement = onSetDragHandlePlacement,
                            onSetHorizontalOffsetPreset = onSetHorizontalOffsetPreset,
                            onSetPersistentOverlayEnabled = onSetPersistentOverlayEnabled,
                            onSetLowQualityThumbnailFallbackEnabled = onSetLowQualityThumbnailFallbackEnabled,
                            onStartOverlay = onStartOverlay,
                            onStopOverlay = onStopOverlay,
                            onDispatchPrevious = onDispatchPrevious,
                            onDispatchPlayPause = onDispatchPlayPause,
                            onDispatchNext = onDispatchNext,
                            onClearLogs = onClearLogs,
                            onOpenOverlaySettings = onOpenOverlaySettings,
                            onOpenNotificationListenerSettings = onOpenNotificationListenerSettings,
                            onOpenNotificationSettings = onOpenNotificationSettings,
                            onOpenSupport = { selectedSection = AppSection.Support },
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
    val context = LocalContext.current

    Column(
        modifier = modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        sections.forEach { section ->
            SectionCard(
                title = context.getString(section.shortTitleRes),
                selected = section == selectedSection,
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
    val context = LocalContext.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        sections.forEach { section ->
            CompactSectionButton(
                title = context.getString(section.shortTitleRes),
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

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = SectionCardShape,
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = contentColor
            )
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
    onSetAppLanguage: (AppLanguage) -> Unit,
    onSetVisibleButtons: (Set<WidgetButton>) -> Unit,
    onSetSizePreset: (WidgetSizePreset) -> Unit,
    onSetWidthStyle: (WidgetWidthStyle) -> Unit,
    onSetThemePreset: (WidgetThemePreset) -> Unit,
    onSetOpacity: (Float) -> Unit,
    onSetDragHandlePlacement: (DragHandlePlacement) -> Unit,
    onSetHorizontalOffsetPreset: (Int) -> Unit,
    onSetPersistentOverlayEnabled: (Boolean) -> Unit,
    onSetLowQualityThumbnailFallbackEnabled: (Boolean) -> Unit,
    onStartOverlay: () -> Unit,
    onStopOverlay: () -> Unit,
    onDispatchPrevious: () -> Unit,
    onDispatchPlayPause: () -> Unit,
    onDispatchNext: () -> Unit,
    onClearLogs: () -> Unit,
    onOpenOverlaySettings: () -> Unit,
    onOpenNotificationListenerSettings: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
    onOpenSupport: () -> Unit,
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
                    onOpenSupport = onOpenSupport,
                    wideLayout = wideLayout
                )

                AppSection.Settings -> SettingsScreen(
                    capabilityState = capabilityState,
                    runtimeState = runtimeState,
                    mediaSummaryState = mediaSummaryState,
                    widgetConfigState = widgetConfigState,
                    onSetVisibleButtons = onSetVisibleButtons,
                    onSetSizePreset = onSetSizePreset,
                    onSetWidthStyle = onSetWidthStyle,
                    onSetOpacity = onSetOpacity,
                    onSetDragHandlePlacement = onSetDragHandlePlacement,
                    onSetHorizontalOffsetPreset = onSetHorizontalOffsetPreset,
                    onSetLowQualityThumbnailFallbackEnabled = onSetLowQualityThumbnailFallbackEnabled,
                    onStartOverlay = onStartOverlay,
                    onStopOverlay = onStopOverlay,
                    wideLayout = wideLayout
                )

                AppSection.Advanced -> AdvancedSettingsScreen(
                    appPreferences = appPreferences,
                    widgetConfigState = widgetConfigState,
                    onSetDebugToolsEnabled = onSetDebugToolsEnabled,
                    onSetAppLanguage = onSetAppLanguage,
                    onSetThemePreset = onSetThemePreset,
                    onSetPersistentOverlayEnabled = onSetPersistentOverlayEnabled,
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
                    runtimeState = runtimeState,
                    mediaSummaryState = mediaSummaryState,
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
    onOpenSupport: () -> Unit,
    wideLayout: Boolean
) {
    val readinessProblems = capabilityState.unavailableReasons()

    LandingMainCard(
        runtimeState = runtimeState,
        widgetConfigState = widgetConfigState,
        readinessProblems = readinessProblems
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
        title = stringResource(id = R.string.overlay_controls_title),
        detail = stringResource(id = R.string.overlay_controls_landing_detail),
        showTransportControls = false
    )
    LandingSetupStatusCard(
        readinessProblems = readinessProblems,
        capabilityState = capabilityState,
        onOpenSupport = onOpenSupport
    )
}

@Composable
private fun SettingsScreen(
    capabilityState: CapabilityState,
    runtimeState: OverlayRuntimeState,
    mediaSummaryState: MediaSummaryState,
    widgetConfigState: WidgetConfigScreenState,
    onSetVisibleButtons: (Set<WidgetButton>) -> Unit,
    onSetSizePreset: (WidgetSizePreset) -> Unit,
    onSetWidthStyle: (WidgetWidthStyle) -> Unit,
    onSetOpacity: (Float) -> Unit,
    onSetDragHandlePlacement: (DragHandlePlacement) -> Unit,
    onSetHorizontalOffsetPreset: (Int) -> Unit,
    onSetLowQualityThumbnailFallbackEnabled: (Boolean) -> Unit,
    onStartOverlay: () -> Unit,
    onStopOverlay: () -> Unit,
    wideLayout: Boolean
) {
    if (wideLayout) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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
                    title = stringResource(id = R.string.overlay_controls_title),
                    detail = stringResource(id = R.string.overlay_controls_settings_detail),
                    showTransportControls = false
                )
                ButtonSetEditorCard(
                    config = widgetConfigState.config,
                    onSetVisibleButtons = onSetVisibleButtons
                )
                SizePresetCard(
                    selectedPreset = widgetConfigState.config.sizePreset,
                    selectedWidthStyle = widgetConfigState.config.widthStyle,
                    onSetSizePreset = onSetSizePreset,
                    onSetWidthStyle = onSetWidthStyle
                )
                WidgetOpacityCard(
                    opacity = widgetConfigState.config.opacity,
                    onSetOpacity = onSetOpacity
                )
                SidebarPlacementCard(
                    selectedPlacement = widgetConfigState.config.layout.dragHandlePlacement,
                    onSetDragHandlePlacement = onSetDragHandlePlacement
                )
                // OffsetPresetCard temporarily disabled
                ThumbnailToggleCard(
                    config = widgetConfigState.config,
                    onSetEnabled = onSetLowQualityThumbnailFallbackEnabled
                )
            }
        }
    } else {
        DebugControlsCard(
            readyForStart = capabilityState.isReadyForPersistentOverlay(),
            runtimeState = runtimeState,
            mediaSummaryState = mediaSummaryState,
            onStartOverlay = onStartOverlay,
            onStopOverlay = onStopOverlay,
            onDispatchPrevious = {},
            onDispatchPlayPause = {},
            onDispatchNext = {},
            title = stringResource(id = R.string.overlay_controls_title),
            detail = stringResource(id = R.string.overlay_controls_settings_detail),
            showTransportControls = false
        )
        ButtonSetEditorCard(
            config = widgetConfigState.config,
            onSetVisibleButtons = onSetVisibleButtons
        )
        SizePresetCard(
            selectedPreset = widgetConfigState.config.sizePreset,
            selectedWidthStyle = widgetConfigState.config.widthStyle,
            onSetSizePreset = onSetSizePreset,
            onSetWidthStyle = onSetWidthStyle
        )
        WidgetOpacityCard(
            opacity = widgetConfigState.config.opacity,
            onSetOpacity = onSetOpacity
        )
        SidebarPlacementCard(
            selectedPlacement = widgetConfigState.config.layout.dragHandlePlacement,
            onSetDragHandlePlacement = onSetDragHandlePlacement
        )
        // OffsetPresetCard temporarily disabled
        ThumbnailToggleCard(
            config = widgetConfigState.config,
            onSetEnabled = onSetLowQualityThumbnailFallbackEnabled
        )
    }
}

@Composable
private fun AdvancedSettingsScreen(
    appPreferences: AppPreferences,
    widgetConfigState: WidgetConfigScreenState,
    onSetDebugToolsEnabled: (Boolean) -> Unit,
    onSetAppLanguage: (AppLanguage) -> Unit,
    onSetThemePreset: (WidgetThemePreset) -> Unit,
    onSetPersistentOverlayEnabled: (Boolean) -> Unit,
    wideLayout: Boolean
) {
    if (wideLayout) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ThemePresetCard(
                    selectedPreset = widgetConfigState.config.themePreset,
                    onSetThemePreset = onSetThemePreset
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AppLanguageCard(
                    selectedLanguage = appPreferences.appLanguage,
                    onSetAppLanguage = onSetAppLanguage
                )
                WidgetBehaviorCard(
                    config = widgetConfigState.config,
                    onSetPersistentOverlayEnabled = onSetPersistentOverlayEnabled
                )
                DeveloperOptionsCard(
                    debugToolsEnabled = appPreferences.debugToolsEnabled,
                    onSetDebugToolsEnabled = onSetDebugToolsEnabled
                )
            }
        }
    } else {
        ThemePresetCard(
            selectedPreset = widgetConfigState.config.themePreset,
            onSetThemePreset = onSetThemePreset
        )
        AppLanguageCard(
            selectedLanguage = appPreferences.appLanguage,
            onSetAppLanguage = onSetAppLanguage
        )
        WidgetBehaviorCard(
            config = widgetConfigState.config,
            onSetPersistentOverlayEnabled = onSetPersistentOverlayEnabled
        )
        DeveloperOptionsCard(
            debugToolsEnabled = appPreferences.debugToolsEnabled,
            onSetDebugToolsEnabled = onSetDebugToolsEnabled
        )
    }
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
                    heading = stringResource(id = R.string.runtime_status_title),
                    supportingLine = runtimeSupportingLine(LocalContext.current, runtimeState)
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
            heading = stringResource(id = R.string.runtime_status_title),
            supportingLine = runtimeSupportingLine(LocalContext.current, runtimeState)
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
    runtimeState: OverlayRuntimeState,
    mediaSummaryState: MediaSummaryState,
    debugLogState: DebugLogScreenState,
    onOpenOverlaySettings: () -> Unit,
    onOpenNotificationListenerSettings: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
    automationLaunchAction: String,
    wideLayout: Boolean
) {
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
                    automationLaunchAction = automationLaunchAction
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SupportStatusSummaryCard(
                    capabilityState = capabilityState,
                    runtimeState = runtimeState,
                    mediaSummaryState = mediaSummaryState
                )
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
            automationLaunchAction = automationLaunchAction
        )
        SupportStatusSummaryCard(
            capabilityState = capabilityState,
            runtimeState = runtimeState,
            mediaSummaryState = mediaSummaryState
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
                text = stringResource(id = R.string.widget_preview_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(id = R.string.widget_preview_detail),
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
                MetricPill(label = stringResource(id = R.string.metric_size), value = config.sizePreset.displayTitle(LocalContext.current))
                MetricPill(label = stringResource(id = R.string.metric_buttons), value = config.layout.summaryLabel(LocalContext.current))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricPill(label = stringResource(id = R.string.metric_saved_edge), value = position.anchor.displayLabel(LocalContext.current))
                MetricPill(label = stringResource(id = R.string.metric_offset), value = stringResource(id = R.string.offset_value, position.xOffsetDp, position.yOffsetDp))
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
    val sizing = config.overlayAppearance().sizing
    val hasTitle = mediaState.currentTitle() != null
    val thumbnailPresentation = resolveOverlayThumbnailPresentation(
        mediaState = mediaState,
        sizing = sizing,
        allowLowQualityFallback = config.allowLowQualityThumbnailFallback
    )
    val overlayHeightDp = sizing.containerHeightDp + if (hasTitle) {
        sizing.titleStripMinHeightDp + sizing.titleStripSpacingDp
    } else {
        0
    }
    val scaledWidth = previewScaledDp(sizing.bodyWidthDp(hasThumbnail = thumbnailPresentation != null))
    val scaledHeight = previewScaledDp(overlayHeightDp)
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
    val sizing = config.overlayAppearance().sizing
    val title = mediaState.currentTitle()
    val thumbnailPresentation = resolveOverlayThumbnailPresentation(
        mediaState = mediaState,
        sizing = sizing,
        allowLowQualityFallback = config.allowLowQualityThumbnailFallback
    )

    Column(
        modifier = modifier.width(previewScaledDp(sizing.bodyWidthDp(hasThumbnail = thumbnailPresentation != null))),
        verticalArrangement = Arrangement.spacedBy(
            if (title == null) 0.dp else previewScaledDp(sizing.titleStripSpacingDp)
        )
    ) {
        if (title != null) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .width(previewScaledDp(sizing.titleStripWidthDp))
                        .heightIn(min = previewScaledDp(sizing.titleStripMinHeightDp))
                        .clip(RoundedCornerShape(previewScaledDp(sizing.titleStripCornerRadiusDp)))
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .padding(horizontal = previewScaledDp(sizing.titleStripHorizontalPaddingDp), vertical = 4.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .basicMarquee(iterations = Int.MAX_VALUE),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Clip
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(previewScaledDp(sizing.itemSpacingDp)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (thumbnailPresentation != null) {
                PreviewOverlayThumbnail(
                    source = thumbnailPresentation.artwork.source,
                    sizing = sizing
                )
            }
            Row(
                modifier = Modifier
                    .width(previewScaledDp(sizing.containerWidthDp))
                    .height(previewScaledDp(sizing.containerHeightDp))
                    .clip(RoundedCornerShape(previewScaledDp(sizing.containerCornerRadiusDp)))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.94f))
                    .padding(
                        start = previewScaledDp(sizing.containerStartPaddingDp),
                        end = previewScaledDp(sizing.containerEndPaddingDp),
                        top = previewScaledDp(sizing.containerVerticalPaddingDp),
                        bottom = previewScaledDp(sizing.containerVerticalPaddingDp)
                    ),
                horizontalArrangement = Arrangement.spacedBy(previewScaledDp(sizing.itemSpacingDp)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (config.layout.dragHandlePlacement == DragHandlePlacement.Left) {
                    PreviewDragHandle(sizing = sizing)
                }
                config.layout.orderedButtons.forEach { button ->
                    PreviewOverlayButton(
                        button = button,
                        mediaState = mediaState,
                        sizing = sizing
                    )
                }
                if (config.layout.dragHandlePlacement == DragHandlePlacement.Right) {
                    PreviewDragHandle(sizing = sizing)
                }
            }
        }
    }
}

@Composable
private fun PreviewOverlayThumbnail(
    source: MediaArtworkSource,
    sizing: sw2.io.mediafloat.model.WidgetOverlaySizing,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(previewScaledDp(sizing.thumbnailSizeDp))
            .clip(RoundedCornerShape(previewScaledDp(sizing.thumbnailCornerRadiusDp)))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.secondary,
                        MaterialTheme.colorScheme.tertiary,
                        MaterialTheme.colorScheme.secondaryContainer
                    )
                )
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(previewScaledDp(sizing.thumbnailCornerRadiusDp))
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = when (source) {
                MediaArtworkSource.MetadataDisplayIconUri,
                MediaArtworkSource.MetadataArtUri,
                MediaArtworkSource.MetadataAlbumArtUri -> "URI"
                MediaArtworkSource.MetadataDisplayIconBitmap,
                MediaArtworkSource.MetadataArtBitmap,
                MediaArtworkSource.MetadataAlbumArtBitmap -> "ART"
                MediaArtworkSource.NotificationPicture,
                MediaArtworkSource.NotificationLargeIconBig,
                MediaArtworkSource.NotificationExtraLargeIcon,
                MediaArtworkSource.NotificationLargeIcon -> "NOTI"
            },
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondary,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

@Composable
private fun PreviewOverlayButton(
    button: WidgetButton,
    mediaState: MediaSessionState,
    sizing: sw2.io.mediafloat.model.WidgetOverlaySizing,
    modifier: Modifier = Modifier
) {
    val command = button.toMediaCommand()
    val enabled = mediaState.supports(command)
    val label = when (button) {
        WidgetButton.Previous -> stringResource(id = R.string.preview_button_previous)
        WidgetButton.PlayPause -> if ((mediaState as? MediaSessionState.Active)?.playbackStatus == PlaybackStatus.Playing) {
            stringResource(id = R.string.preview_button_pause)
        } else {
            stringResource(id = R.string.preview_button_play)
        }
        WidgetButton.Next -> stringResource(id = R.string.preview_button_next)
    }

    Box(
        modifier = modifier
            .width(previewScaledDp(sizing.buttonWidthDp))
            .height(previewScaledDp(sizing.buttonHeightDp))
            .clip(RoundedCornerShape(previewScaledDp(sizing.buttonHeightDp / 2)))
            .background(
                if (enabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
            )
            .padding(horizontal = previewScaledDp(8), vertical = previewScaledDp(6)),
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
private fun PreviewDragHandle(
    sizing: sw2.io.mediafloat.model.WidgetOverlaySizing,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(previewScaledDp(sizing.handleWidthDp))
            .height(previewScaledDp(sizing.handleHeightDp))
            .clip(RoundedCornerShape(previewScaledDp(sizing.handleCornerRadiusDp)))
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
                text = stringResource(id = R.string.visible_buttons_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SelectionPill(
                    text = stringResource(id = R.string.media_previous),
                    selected = hasPrevious,
                    enabled = true,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onSetVisibleButtons(buildVisibleButtons(includePrevious = !hasPrevious, includeNext = hasNext))
                    }
                )
                SelectionPill(
                    text = stringResource(id = R.string.media_play_pause),
                    selected = true,
                    enabled = false,
                    modifier = Modifier.weight(1f),
                    onClick = {}
                )
                SelectionPill(
                    text = stringResource(id = R.string.media_next),
                    selected = hasNext,
                    enabled = true,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onSetVisibleButtons(buildVisibleButtons(includePrevious = hasPrevious, includeNext = !hasNext))
                    }
                )
            }
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
                if (detail.isNotBlank()) {
                    Text(
                        text = detail,
                        style = MaterialTheme.typography.bodySmall,
                        color = contentColor.copy(alpha = 0.84f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SizePresetCard(
    selectedPreset: WidgetSizePreset,
    selectedWidthStyle: WidgetWidthStyle,
    onSetSizePreset: (WidgetSizePreset) -> Unit,
    onSetWidthStyle: (WidgetWidthStyle) -> Unit,
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
                text = stringResource(id = R.string.size_preset_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                WidgetSizePreset.entries.forEach { preset ->
                    SelectionPill(
                        text = preset.displayTitle(LocalContext.current),
                        selected = preset == selectedPreset,
                        enabled = true,
                        modifier = Modifier.weight(1f),
                        onClick = { onSetSizePreset(preset) }
                    )
                }
            }
            Text(
                text = stringResource(id = R.string.button_width_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                WidgetWidthStyle.entries.forEach { style ->
                    SelectionPill(
                        text = style.displayTitle(LocalContext.current),
                        selected = style == selectedWidthStyle,
                        enabled = true,
                        modifier = Modifier.weight(1f),
                        onClick = { onSetWidthStyle(style) }
                    )
                }
            }
        }
    }
}

@Composable
private fun WidthStyleCard(
    selectedStyle: WidgetWidthStyle,
    onSetWidthStyle: (WidgetWidthStyle) -> Unit,
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
                text = stringResource(id = R.string.button_width_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(id = R.string.button_width_detail),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            WidgetWidthStyle.entries.forEach { style ->
                ToggleOptionCard(
                    title = style.displayTitle(LocalContext.current),
                    detail = when (style) {
                        WidgetWidthStyle.Regular -> stringResource(id = R.string.button_width_regular_detail)
                        WidgetWidthStyle.Wide -> stringResource(id = R.string.button_width_wide_detail)
                    },
                    selected = style == selectedStyle,
                    enabled = true,
                    onClick = { onSetWidthStyle(style) }
                )
            }
        }
    }
}

@Composable
private fun WidgetOpacityCard(
    opacity: Float,
    onSetOpacity: (Float) -> Unit,
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
                text = stringResource(id = R.string.widget_opacity_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(id = R.string.widget_opacity_value, (opacity * 100).roundToInt()),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Slider(
                value = opacity,
                onValueChange = onSetOpacity,
                valueRange = 0.35f..1f
            )
        }
    }
}

@Composable
private fun SelectionPill(
    text: String,
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

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(containerColor)
            .border(
                width = 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ThemePresetCard(
    selectedPreset: WidgetThemePreset,
    onSetThemePreset: (WidgetThemePreset) -> Unit,
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
                text = stringResource(id = R.string.widget_theme_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(id = R.string.widget_theme_detail),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            WidgetThemePreset.entries.forEach { preset ->
                ToggleOptionCard(
                    title = preset.displayTitle(LocalContext.current),
                    detail = preset.description(LocalContext.current),
                    selected = preset == selectedPreset,
                    enabled = true,
                    onClick = { onSetThemePreset(preset) }
                )
            }
        }
    }
}

@Composable
private fun SidebarPlacementCard(
    selectedPlacement: DragHandlePlacement,
    onSetDragHandlePlacement: (DragHandlePlacement) -> Unit,
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
                text = stringResource(id = R.string.sidebar_side_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DragHandlePlacement.entries.forEach { placement ->
                    SelectionPill(
                        text = placement.displayTitle(LocalContext.current),
                        selected = placement == selectedPlacement,
                        enabled = true,
                        modifier = Modifier.weight(1f),
                        onClick = { onSetDragHandlePlacement(placement) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ActualOverlayNoticeCard(
    appLanguage: AppLanguage,
    config: WidgetConfig,
    position: WidgetPosition,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

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
                text = stringResource(id = R.string.actual_widget_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(id = R.string.actual_widget_detail),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricPill(label = stringResource(id = R.string.metric_buttons), value = config.layout.summaryLabel(context), modifier = Modifier.weight(1f))
                MetricPill(label = stringResource(id = R.string.metric_size), value = config.sizePreset.displayTitle(context), modifier = Modifier.weight(1f))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricPill(label = stringResource(id = R.string.metric_width), value = config.widthStyle.displayTitle(context), modifier = Modifier.weight(1f))
                MetricPill(label = stringResource(id = R.string.metric_theme), value = config.themePreset.displayTitle(context), modifier = Modifier.weight(1f))
            }
            MetricPill(
                label = stringResource(id = R.string.metric_language),
                value = appLanguage.displayLabel(context),
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = stringResource(
                    id = R.string.actual_widget_saved_position,
                    position.anchor.displayLabel(context),
                    position.xOffsetDp,
                    position.yOffsetDp
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LandingMainCard(
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(id = R.string.main_intro_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(id = R.string.main_intro_detail),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricPill(
                    label = stringResource(id = R.string.metric_buttons),
                    value = widgetConfigState.config.layout.summaryLabel(LocalContext.current),
                    modifier = Modifier.weight(1f)
                )
                MetricPill(
                    label = stringResource(id = R.string.metric_size),
                    value = widgetConfigState.config.sizePreset.displayTitle(LocalContext.current),
                    modifier = Modifier.weight(1f)
                )
                MetricPill(
                    label = stringResource(id = R.string.metric_runtime),
                    value = runtimeState.landingStatusLabel(LocalContext.current),
                    modifier = Modifier.weight(1f)
                )
            }
            if (readinessProblems.isNotEmpty()) {
                Text(
                    text = stringResource(id = R.string.main_intro_blocked_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun LandingSetupStatusCard(
    readinessProblems: List<OverlayUnavailableReason>,
    capabilityState: CapabilityState,
    onOpenSupport: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ready = capabilityState.isReadyForPersistentOverlay()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = PanelShape,
        colors = CardDefaults.cardColors(
            containerColor = if (ready) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(id = R.string.main_status_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (ready) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onTertiaryContainer
            )
            Text(
                text = if (ready) stringResource(id = R.string.main_status_ready) else stringResource(id = R.string.main_status_blocked),
                style = MaterialTheme.typography.bodyMedium,
                color = if (ready) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onTertiaryContainer
            )
            if (readinessProblems.isNotEmpty()) {
                OutlinedButton(onClick = onOpenSupport) {
                    Text(text = stringResource(id = R.string.main_status_open_support))
                }
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
                text = stringResource(id = R.string.landing_overview_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(id = R.string.landing_overview_detail),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricPill(
                    label = stringResource(id = R.string.metric_runtime),
                    value = runtimeState.landingStatusLabel(LocalContext.current),
                    modifier = Modifier.weight(1f)
                )
                MetricPill(
                    label = stringResource(id = R.string.metric_buttons),
                    value = widgetConfigState.config.layout.orderedButtons.size.toString(),
                    modifier = Modifier.weight(1f)
                )
                MetricPill(
                    label = stringResource(id = R.string.metric_size),
                    value = widgetConfigState.config.sizePreset.displayTitle(LocalContext.current),
                    modifier = Modifier.weight(1f)
                )
            }
            Text(
                text = runtimeState.landingOverviewLine(LocalContext.current),
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
        context = LocalContext.current,
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
                text = stringResource(id = R.string.recovery_shortcuts_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Text(
                text = if (actions.isEmpty()) {
                    stringResource(id = R.string.recovery_shortcuts_ready_detail)
                } else {
                    stringResource(id = R.string.recovery_shortcuts_blocked_detail)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            readinessProblems.forEach { reason ->
                Text(
                    text = reason.toDisplayLine(LocalContext.current),
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
                text = stringResource(id = R.string.developer_tools_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(id = R.string.developer_tools_detail),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("debug-tools-toggle-row")
                    .toggleable(
                        value = debugToolsEnabled,
                        role = Role.Switch,
                        onValueChange = onSetDebugToolsEnabled
                    ),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.developer_tools_toggle_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                }
                Switch(
                    checked = debugToolsEnabled,
                    onCheckedChange = null,
                    modifier = Modifier.testTag("debug-tools-toggle")
                )
            }
        }
    }
}

@Composable
private fun WidgetBehaviorCard(
    config: WidgetConfig,
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
                text = stringResource(id = R.string.behavior_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.persistent_overlay_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = stringResource(id = R.string.persistent_overlay_detail),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = config.persistentOverlayEnabled,
                    onCheckedChange = onSetPersistentOverlayEnabled
                )
            }
        }
    }
}

@Composable
private fun OffsetPresetCard(
    currentOffsetDp: Int,
    onSetOffsetDp: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val presets = listOf(12, 24, 40)

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
                text = stringResource(id = R.string.edge_offset_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(id = R.string.edge_offset_detail),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                presets.forEach { offsetDp ->
                    SelectionPill(
                        text = when (offsetDp) {
                            12 -> stringResource(id = R.string.edge_offset_near)
                            24 -> stringResource(id = R.string.edge_offset_default)
                            else -> stringResource(id = R.string.edge_offset_far)
                        },
                        selected = currentOffsetDp == offsetDp,
                        enabled = true,
                        modifier = Modifier.weight(1f),
                        onClick = { onSetOffsetDp(offsetDp) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ThumbnailToggleCard(
    config: WidgetConfig,
    onSetEnabled: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = PanelShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .testTag("thumbnail-toggle-row"),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.low_quality_thumbnail_fallback_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = stringResource(id = R.string.low_quality_thumbnail_fallback_detail),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = config.allowLowQualityThumbnailFallback,
                onCheckedChange = onSetEnabled,
                modifier = Modifier.testTag("thumbnail-toggle")
            )
        }
    }
}

@Composable
private fun AppLanguageCard(
    selectedLanguage: AppLanguage,
    onSetAppLanguage: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

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
                text = stringResource(id = R.string.app_language_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(id = R.string.app_language_detail),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            AppLanguage.entries.forEach { appLanguage ->
                ToggleOptionCard(
                    title = appLanguage.displayLabel(context),
                    detail = "",
                    selected = appLanguage == selectedLanguage,
                    enabled = true,
                    onClick = { onSetAppLanguage(appLanguage) }
                )
            }
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
    val context = LocalContext.current
    val runtimeSummary = RuntimeStatusFormatter.format(
        context = context,
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
                text = stringResource(id = R.string.system_access_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Text(
                text = if (readinessProblems.isEmpty()) {
                    stringResource(id = R.string.system_access_ready_detail)
                } else {
                    stringResource(id = R.string.system_access_blocked_detail)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            readinessProblems.forEach { reason ->
                Text(
                    text = reason.toDisplayLine(LocalContext.current),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
            if (compact) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onOpenOverlaySettings, modifier = Modifier.fillMaxWidth()) {
                        Text(text = stringResource(id = R.string.action_overlay_access))
                    }
                    OutlinedButton(onClick = onOpenNotificationListenerSettings, modifier = Modifier.fillMaxWidth()) {
                        Text(text = stringResource(id = R.string.action_listener_access))
                    }
                    OutlinedButton(onClick = onOpenNotificationSettings, modifier = Modifier.fillMaxWidth()) {
                        Text(text = stringResource(id = R.string.action_notifications))
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(onClick = onOpenOverlaySettings, modifier = Modifier.weight(1f)) {
                        Text(text = stringResource(id = R.string.action_overlay_access))
                    }
                    OutlinedButton(onClick = onOpenNotificationListenerSettings, modifier = Modifier.weight(1f)) {
                        Text(text = stringResource(id = R.string.action_listener_access))
                    }
                    OutlinedButton(onClick = onOpenNotificationSettings, modifier = Modifier.weight(1f)) {
                        Text(text = stringResource(id = R.string.action_notifications))
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
    val context = LocalContext.current
    val title = mediaSummaryState.mediaState.title(context)
    val detail = mediaSummaryState.mediaState.detail(context)

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
                text = stringResource(id = R.string.media_readiness_title),
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
            mediaSummaryState.mediaState.supportingLines(context).forEach { line ->
                Text(
                    text = line,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Text(
                text = stringResource(
                    id = R.string.media_readiness_actions,
                    mediaSummaryState.previousEnabled.readinessLabel(LocalContext.current),
                    mediaSummaryState.playPauseEnabled.readinessLabel(LocalContext.current),
                    mediaSummaryState.nextEnabled.readinessLabel(LocalContext.current)
                ),
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
    title: String = "",
    detail: String = "",
    showTransportControls: Boolean = true,
    modifier: Modifier = Modifier
) {
    val resolvedTitle = title.ifBlank { androidx.compose.ui.res.stringResource(id = R.string.debug_controls_title) }
    val resolvedDetail = detail.ifBlank { androidx.compose.ui.res.stringResource(id = R.string.debug_controls_detail) }

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
                text = resolvedTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = resolvedDetail,
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
                    Text(text = stringResource(id = R.string.action_start_overlay))
                }
                OutlinedButton(
                    onClick = onStopOverlay,
                    enabled = runtimeState is OverlayRuntimeState.Showing,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = stringResource(id = R.string.action_stop_overlay))
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
                        Text(text = stringResource(id = R.string.media_prev_short))
                    }
                    Button(
                        onClick = onDispatchPlayPause,
                        enabled = mediaSummaryState.playPauseEnabled,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = stringResource(id = R.string.media_play_pause))
                    }
                    OutlinedButton(
                        onClick = onDispatchNext,
                        enabled = mediaSummaryState.nextEnabled,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = stringResource(id = R.string.media_next))
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
                        text = stringResource(id = R.string.recent_debug_log_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = stringResource(id = R.string.recent_debug_log_detail, entries.size, debugLogState.entries.size, debugLogState.retentionLimit),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                OutlinedButton(onClick = onClearLogs) {
                    Text(text = stringResource(id = R.string.action_clear))
                }
            }

            if (entries.isEmpty()) {
                Text(
                    text = stringResource(id = R.string.recent_debug_log_empty),
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
                text = stringResource(id = R.string.setup_help_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(id = R.string.setup_help_detail),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (readinessProblems.isNotEmpty()) {
                readinessProblems.forEach { problem ->
                    Text(
                        text = problem.toDisplayLine(LocalContext.current),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (compact) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onOpenOverlaySettings, modifier = Modifier.fillMaxWidth()) {
                        Text(text = stringResource(id = R.string.action_open_overlay_access))
                    }
                    OutlinedButton(onClick = onOpenNotificationListenerSettings, modifier = Modifier.fillMaxWidth()) {
                        Text(text = stringResource(id = R.string.action_open_listener_access))
                    }
                    OutlinedButton(onClick = onOpenNotificationSettings, modifier = Modifier.fillMaxWidth()) {
                        Text(text = stringResource(id = R.string.action_open_notifications))
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(onClick = onOpenOverlaySettings, modifier = Modifier.weight(1f)) {
                        Text(text = stringResource(id = R.string.action_overlay_access))
                    }
                    OutlinedButton(onClick = onOpenNotificationListenerSettings, modifier = Modifier.weight(1f)) {
                        Text(text = stringResource(id = R.string.action_listener_access))
                    }
                    OutlinedButton(onClick = onOpenNotificationSettings, modifier = Modifier.weight(1f)) {
                        Text(text = stringResource(id = R.string.action_notifications))
                    }
                }
            }
        }
    }
}

@Composable
private fun SupportStatusSummaryCard(
    capabilityState: CapabilityState,
    runtimeState: OverlayRuntimeState,
    mediaSummaryState: MediaSummaryState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val runtimeSummary = RuntimeStatusFormatter.format(context, capabilityState, runtimeState)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = PanelShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(id = R.string.support_status_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            PropertyLine(
                label = stringResource(id = R.string.current_readiness_title),
                value = runtimeSummary.title,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                supportingColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
            PropertyLine(
                label = stringResource(id = R.string.media_readiness_title),
                value = mediaSummaryState.mediaState.title(context),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                supportingColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun AboutCard(
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
                text = stringResource(id = R.string.app_info_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            PropertyLine(
                label = stringResource(id = R.string.about_version_label),
                value = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                supportingColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
            )
            PropertyLine(
                label = stringResource(id = R.string.about_package_label),
                value = BuildConfig.APPLICATION_ID,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                supportingColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
            )
            PropertyLine(
                label = stringResource(id = R.string.about_automation_label),
                value = automationLaunchAction,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                supportingColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
            )
            PropertyLine(
                label = stringResource(id = R.string.about_activity_label),
                value = AutomationEntryActivity::class.java.name,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                supportingColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
            )
            Text(
                text = stringResource(id = R.string.app_info_footer),
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
                text = stringResource(id = R.string.product_constraints_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = stringResource(id = R.string.product_constraints_list),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = stringResource(id = R.string.product_constraints_footer),
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
                text = stringResource(id = R.string.licenses_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(id = R.string.licenses_intro),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(id = R.string.licenses_androidx),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(id = R.string.licenses_material),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(id = R.string.licenses_kotlin),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(id = R.string.licenses_platform),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(id = R.string.licenses_log_retention, retentionLimit),
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

private fun runtimeSupportingLine(context: Context, runtimeState: OverlayRuntimeState): String {
    return when (runtimeState) {
        OverlayRuntimeState.Ready -> context.getString(R.string.runtime_supporting_ready)
        is OverlayRuntimeState.Showing -> {
            context.getString(
                R.string.runtime_supporting_showing,
                runtimeState.layout.summaryLabel(context),
                runtimeState.position.anchor.displayLabel(context).lowercase()
            )
        }
        is OverlayRuntimeState.Suspended -> context.getString(R.string.runtime_supporting_suspended)
        is OverlayRuntimeState.Unavailable -> context.getString(R.string.runtime_supporting_unavailable)
    }
}

internal fun landingSupportingLine(
    context: Context,
    runtimeState: OverlayRuntimeState,
    readinessProblems: List<OverlayUnavailableReason>
): String {
    return when {
        readinessProblems.isEmpty() && runtimeState is OverlayRuntimeState.Showing -> {
            context.getString(R.string.landing_supporting_showing)
        }
        readinessProblems.isEmpty() -> {
            context.getString(R.string.landing_supporting_ready)
        }
        else -> {
            context.getString(R.string.landing_supporting_blocked)
        }
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

internal fun OverlayRuntimeState.landingStatusLabel(context: Context): String {
    return when (this) {
        OverlayRuntimeState.Ready -> context.getString(R.string.state_ready)
        is OverlayRuntimeState.Showing -> context.getString(R.string.state_showing)
        is OverlayRuntimeState.Suspended -> context.getString(R.string.state_paused)
        is OverlayRuntimeState.Unavailable -> context.getString(R.string.state_blocked)
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

private fun OverlayRuntimeState.landingOverviewLine(context: Context): String {
    return when (this) {
        OverlayRuntimeState.Ready -> context.getString(R.string.landing_overview_ready)
        is OverlayRuntimeState.Showing -> context.getString(R.string.landing_overview_showing)
        is OverlayRuntimeState.Suspended -> context.getString(R.string.landing_overview_suspended)
        is OverlayRuntimeState.Unavailable -> context.getString(R.string.landing_overview_unavailable)
    }
}

private fun List<OverlayUnavailableReason>.toRecoveryActions(
    context: Context,
    onOpenOverlaySettings: () -> Unit,
    onOpenNotificationListenerSettings: () -> Unit,
    onOpenNotificationSettings: () -> Unit
): List<RecoveryAction> {
    return buildList {
        if (OverlayUnavailableReason.MissingOverlayAccess in this@toRecoveryActions) {
            add(RecoveryAction(label = context.getString(R.string.action_open_overlay_access), onClick = onOpenOverlaySettings))
        }
        if (OverlayUnavailableReason.MissingNotificationListenerAccess in this@toRecoveryActions) {
            add(RecoveryAction(label = context.getString(R.string.action_open_listener_access), onClick = onOpenNotificationListenerSettings))
        }
        if (OverlayUnavailableReason.NotificationPostureBlocked in this@toRecoveryActions) {
            add(RecoveryAction(label = context.getString(R.string.action_open_notifications), onClick = onOpenNotificationSettings))
        }
    }
}

private fun OverlayUnavailableReason.toDisplayLine(context: Context): String {
    return when (this) {
        OverlayUnavailableReason.MissingOverlayAccess -> context.getString(R.string.recovery_line_overlay_access)
        OverlayUnavailableReason.MissingNotificationListenerAccess -> context.getString(R.string.recovery_line_listener_access)
        OverlayUnavailableReason.NotificationPostureBlocked -> context.getString(R.string.recovery_line_notifications)
        OverlayUnavailableReason.ServiceStartNotAllowed -> context.getString(R.string.recovery_line_service_start)
        OverlayUnavailableReason.UnsupportedDeviceCondition -> context.getString(R.string.recovery_line_device_condition)
        OverlayUnavailableReason.UnknownRuntimeFailure -> context.getString(R.string.recovery_line_runtime_failure)
    }
}

private fun WidgetLayout.summaryLabel(context: Context): String {
    return orderedButtons.joinToString(separator = " + ") { it.displayLabel(context) }
}

private fun WidgetButton.displayLabel(context: Context): String {
    return when (this) {
        WidgetButton.Previous -> context.getString(R.string.media_previous)
        WidgetButton.PlayPause -> context.getString(R.string.media_play_pause)
        WidgetButton.Next -> context.getString(R.string.media_next)
    }
}

private fun WidgetSizePreset.displayTitle(context: Context): String {
    return when (this) {
        WidgetSizePreset.Compact -> context.getString(R.string.size_preset_compact)
        WidgetSizePreset.Standard -> context.getString(R.string.size_preset_standard)
        WidgetSizePreset.Large -> context.getString(R.string.size_preset_large)
    }
}

private fun WidgetWidthStyle.displayTitle(context: Context): String {
    return when (this) {
        WidgetWidthStyle.Regular -> context.getString(R.string.button_width_regular)
        WidgetWidthStyle.Wide -> context.getString(R.string.button_width_wide)
    }
}

private fun DragHandlePlacement.displayTitle(context: Context): String {
    return when (this) {
        DragHandlePlacement.Left -> context.getString(R.string.sidebar_left)
        DragHandlePlacement.Right -> context.getString(R.string.sidebar_right)
    }
}

private fun WidgetThemePreset.displayTitle(context: Context): String {
    return when (this) {
        WidgetThemePreset.Light -> context.getString(R.string.theme_light)
        WidgetThemePreset.Dark -> context.getString(R.string.theme_dark)
        WidgetThemePreset.DarkBlue -> context.getString(R.string.theme_dark_blue)
        WidgetThemePreset.MediumYellow -> context.getString(R.string.theme_medium_yellow)
        WidgetThemePreset.Pink -> context.getString(R.string.theme_pink)
    }
}

private fun WidgetThemePreset.description(context: Context): String {
    return when (this) {
        WidgetThemePreset.Light -> context.getString(R.string.theme_light_detail)
        WidgetThemePreset.Dark -> context.getString(R.string.theme_dark_detail)
        WidgetThemePreset.DarkBlue -> context.getString(R.string.theme_dark_blue_detail)
        WidgetThemePreset.MediumYellow -> context.getString(R.string.theme_medium_yellow_detail)
        WidgetThemePreset.Pink -> context.getString(R.string.theme_pink_detail)
    }
}

private fun WidgetAnchor.displayLabel(context: Context): String {
    return when (this) {
        WidgetAnchor.Start -> context.getString(R.string.edge_left)
        WidgetAnchor.End -> context.getString(R.string.edge_right)
    }
}

private fun WidgetButton.toMediaCommand(): MediaCommand {
    return when (this) {
        WidgetButton.Previous -> MediaCommand.Previous
        WidgetButton.PlayPause -> MediaCommand.TogglePlayPause
        WidgetButton.Next -> MediaCommand.Next
    }
}

private fun AppLanguage.displayLabel(context: Context): String {
    return when (this) {
        AppLanguage.SystemDefault -> context.getString(R.string.language_system_default)
        AppLanguage.English -> context.getString(R.string.language_english)
        AppLanguage.Korean -> context.getString(R.string.language_korean)
        AppLanguage.Chinese -> context.getString(R.string.language_chinese)
        AppLanguage.Japanese -> context.getString(R.string.language_japanese)
        AppLanguage.Spanish -> context.getString(R.string.language_spanish)
        AppLanguage.French -> context.getString(R.string.language_french)
    }
}

private fun AppLanguage.supportingLabel(context: Context): String {
    return if (this == AppLanguage.SystemDefault) {
        context.getString(R.string.language_system_default_detail)
    } else {
        context.getString(R.string.language_applies_immediately)
    }
}

private fun Boolean.readinessLabel(context: Context): String {
    return if (this) context.getString(R.string.state_ready) else context.getString(R.string.state_blocked)
}

private fun MediaSessionState.title(context: Context): String {
    return when (this) {
        MediaSessionState.Discovering -> context.getString(R.string.media_state_discovering_title)
        MediaSessionState.Unavailable -> context.getString(R.string.media_state_unavailable_title)
        is MediaSessionState.Active -> context.getString(R.string.media_state_active_title)
        is MediaSessionState.Limited -> context.getString(R.string.media_state_limited_title)
        is MediaSessionState.Error -> context.getString(R.string.media_state_error_title)
    }
}

private fun MediaSessionState.detail(context: Context): String {
    return when (this) {
        MediaSessionState.Discovering -> context.getString(R.string.media_state_discovering_detail)
        MediaSessionState.Unavailable -> context.getString(R.string.media_state_unavailable_detail)
        is MediaSessionState.Active -> context.getString(R.string.media_state_active_detail, sessionId.take(36))
        is MediaSessionState.Limited -> context.getString(R.string.media_state_limited_detail)
        is MediaSessionState.Error -> context.getString(R.string.media_state_error_detail)
    }
}

private fun MediaSessionState.supportingLines(context: Context): List<String> {
    return when (this) {
        MediaSessionState.Discovering -> emptyList()
        MediaSessionState.Unavailable -> emptyList()
        is MediaSessionState.Active -> listOf(
            context.getString(R.string.media_state_playback_line, playbackStatus.displayLabel(context)),
            context.getString(R.string.media_state_supported_line, supportedActions.joinToString { it.displayLabel(context) })
        )
        is MediaSessionState.Limited -> listOf(
            context.getString(R.string.media_state_limit_line, reason.displayLabel(context)),
            context.getString(R.string.media_state_supported_line, supportedActions.joinToString { it.displayLabel(context) })
        )
        is MediaSessionState.Error -> listOf(
            context.getString(R.string.media_state_reason_line, reason.displayLabel(context))
        )
    }
}

private fun MediaCommand.displayLabel(context: Context): String {
    return when (this) {
        MediaCommand.Previous -> context.getString(R.string.media_previous)
        MediaCommand.TogglePlayPause -> context.getString(R.string.media_play_pause)
        MediaCommand.Next -> context.getString(R.string.media_next)
    }
}

private fun PlaybackStatus.displayLabel(context: Context): String {
    return when (this) {
        PlaybackStatus.Playing -> context.getString(R.string.playback_playing)
        PlaybackStatus.Paused -> context.getString(R.string.playback_paused)
        PlaybackStatus.Buffering -> context.getString(R.string.playback_buffering)
        PlaybackStatus.Stopped -> context.getString(R.string.playback_stopped)
        PlaybackStatus.Unknown -> context.getString(R.string.playback_unknown)
    }
}

private fun MediaSessionLimitReason.displayLabel(context: Context): String {
    return when (this) {
        MediaSessionLimitReason.MissingTransportControls -> context.getString(R.string.media_limit_missing_transport)
        MediaSessionLimitReason.PlaybackStateUnknown -> context.getString(R.string.media_limit_playback_unknown)
        MediaSessionLimitReason.SessionChanging -> context.getString(R.string.media_limit_session_changing)
    }
}

private fun MediaSessionErrorReason.displayLabel(context: Context): String {
    return when (this) {
        MediaSessionErrorReason.PermissionRevoked -> context.getString(R.string.media_error_permission_revoked)
        MediaSessionErrorReason.PlatformFailure -> context.getString(R.string.media_error_platform_failure)
        MediaSessionErrorReason.Unknown -> context.getString(R.string.media_error_unknown)
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
            title = "Velvet City Lights After Midnight Remix",
            artworkCandidates = listOf(
                MediaArtwork.UriSource(
                    source = MediaArtworkSource.MetadataArtUri,
                    uri = "content://preview/artwork/cover",
                    widthPx = 640,
                    heightPx = 640
                )
            ),
            supportedActions = setOf(MediaCommand.Previous, MediaCommand.TogglePlayPause, MediaCommand.Next),
            playbackStatus = PlaybackStatus.Playing
        )
    )
}

private fun previewMediaSummaryState(): MediaSummaryState {
    return MediaSummaryState(
        mediaState = MediaSessionState.Active(
            sessionId = "preview-session",
            title = "Velvet City Lights After Midnight Remix",
            artworkCandidates = listOf(
                MediaArtwork.UriSource(
                    source = MediaArtworkSource.MetadataArtUri,
                    uri = "content://preview/artwork/cover",
                    widthPx = 640,
                    heightPx = 640
                )
            ),
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
            persistentOverlayEnabled = true,
            allowLowQualityThumbnailFallback = false
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
