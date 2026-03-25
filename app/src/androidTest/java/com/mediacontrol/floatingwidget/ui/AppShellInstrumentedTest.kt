package com.mediacontrol.floatingwidget.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mediacontrol.floatingwidget.model.AppPreferences
import com.mediacontrol.floatingwidget.ui.theme.MediaFloatTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppShellInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun appShell_opensOnLandingAndHidesDebugByDefault() {
        composeRule.setContent {
            MediaFloatTheme {
                AppShell(appPreferences = AppPreferences(debugToolsEnabled = false))
            }
        }

        composeRule.onNodeWithText("Keep previous, play / pause, and next within reach while you stay in other apps.").assertIsDisplayed()
        composeRule.onAllNodesWithTag("section-landing").assertCountEquals(1)
        composeRule.onAllNodesWithText("Debug").assertCountEquals(0)
    }

    @Test
    fun appShell_showsDebugSectionWhenPreferenceIsEnabled() {
        composeRule.setContent {
            MediaFloatTheme {
                AppShell(appPreferences = AppPreferences(debugToolsEnabled = true))
            }
        }

        composeRule.onAllNodesWithTag("section-debug").assertCountEquals(1)
        composeRule.onNodeWithTag("section-debug").performClick()
        composeRule.onNodeWithText("Debug controls").assertIsDisplayed()
    }

    @Test
    fun appShell_revealsDebugAfterEnablingItFromSettings() {
        val debugToolsEnabled = mutableStateOf(false)

        composeRule.setContent {
            MediaFloatTheme {
                AppShell(
                    appPreferences = AppPreferences(debugToolsEnabled = debugToolsEnabled.value),
                    onSetDebugToolsEnabled = { debugToolsEnabled.value = it }
                )
            }
        }

        composeRule.onAllNodesWithTag("section-debug").assertCountEquals(0)
        composeRule.runOnIdle {
            debugToolsEnabled.value = true
        }
        composeRule.onAllNodesWithTag("section-debug").assertCountEquals(1)
        composeRule.runOnIdle {
            assertTrue(debugToolsEnabled.value)
        }
    }
}
