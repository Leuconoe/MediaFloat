package com.mediacontrol.floatingwidget.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mediacontrol.floatingwidget.model.AppPreferences
import com.mediacontrol.floatingwidget.ui.theme.MediaFloatTheme
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

        composeRule.onNodeWithText("Overlay landing").assertIsDisplayed()
        composeRule.onNodeWithText("What MediaFloat does").assertIsDisplayed()
        composeRule.onAllNodesWithText("Debug").assertCountEquals(0)
    }

    @Test
    fun appShell_showsDebugSectionWhenPreferenceIsEnabled() {
        composeRule.setContent {
            MediaFloatTheme {
                AppShell(appPreferences = AppPreferences(debugToolsEnabled = true))
            }
        }

        composeRule.onAllNodesWithText("Debug").assertCountEquals(1)
        composeRule.onNodeWithText("Overlay landing").assertIsDisplayed()
    }
}
