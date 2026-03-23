package com.mediacontrol.floatingwidget.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mediacontrol.floatingwidget.model.CapabilityGrantState
import com.mediacontrol.floatingwidget.model.CapabilityState
import com.mediacontrol.floatingwidget.model.NotificationPosture
import com.mediacontrol.floatingwidget.model.OverlayRuntimeState
import com.mediacontrol.floatingwidget.runtime.RuntimeStatusFormatter
import com.mediacontrol.floatingwidget.ui.theme.MediaControlFloatingWidgetTheme

data class PlaceholderSection(
    val title: String,
    val detail: String
)

fun defaultPlaceholderSections(): List<PlaceholderSection> = listOf(
    PlaceholderSection(
        title = "Overlay runtime",
        detail = "Reserved for the future foreground service and WindowManager host."
    ),
    PlaceholderSection(
        title = "Media controls",
        detail = "Reserved for media session discovery and transport commands."
    ),
    PlaceholderSection(
        title = "Settings and support",
        detail = "Reserved for Compose settings, app info, licenses, and debug logs."
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppShell(
    capabilityState: CapabilityState = previewCapabilityState(),
    runtimeState: OverlayRuntimeState = previewRuntimeState(),
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Media Control Floating Widget")
                }
            )
        }
    ) { innerPadding ->
        PlaceholderHomeScreen(
            sections = defaultPlaceholderSections(),
            capabilityState = capabilityState,
            runtimeState = runtimeState,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
fun PlaceholderHomeScreen(
    sections: List<PlaceholderSection>,
    capabilityState: CapabilityState,
    runtimeState: OverlayRuntimeState,
    modifier: Modifier = Modifier
) {
    val runtimeSummary = RuntimeStatusFormatter.format(
        capabilityState = capabilityState,
        runtimeState = runtimeState
    )

    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Bootstrap shell",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "This baseline app is ready for the service-owned overlay runtime, media session integration, and Compose settings screens planned next.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            RuntimeStatusCard(runtimeSummary = runtimeSummary)
            WidgetPreviewCard()
            sections.forEach { section ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = section.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = section.detail,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RuntimeStatusCard(
    runtimeSummary: com.mediacontrol.floatingwidget.runtime.RuntimeStatusSummary,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
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
private fun WidgetPreviewCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Future widget preview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PreviewButtonSlot(label = "Prev")
                PreviewButtonSlot(label = "Play")
                PreviewButtonSlot(label = "Next")
                Box(
                    modifier = Modifier
                        .height(28.dp)
                        .width(28.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.tertiaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "::",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
            Text(
                text = "The right-side handle and media actions are visual only in this bootstrap.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun PreviewButtonSlot(label: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(40.dp)
            .width(64.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AppShellPreview() {
    MediaControlFloatingWidgetTheme {
        AppShell()
    }
}

private fun previewCapabilityState(): CapabilityState {
    return CapabilityState(
        overlayAccess = CapabilityGrantState.Missing,
        notificationListenerAccess = CapabilityGrantState.Missing,
        notificationPosture = NotificationPosture.PermissionRequired,
        serviceStartReadiness = CapabilityGrantState.Granted
    )
}

private fun previewRuntimeState(): OverlayRuntimeState {
    return OverlayRuntimeState.Unavailable(
        reason = com.mediacontrol.floatingwidget.model.OverlayUnavailableReason.MissingOverlayAccess
    )
}
