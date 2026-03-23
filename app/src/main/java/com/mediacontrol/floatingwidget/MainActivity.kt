package com.mediacontrol.floatingwidget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.mediacontrol.floatingwidget.ui.AppShell
import com.mediacontrol.floatingwidget.ui.theme.MediaControlFloatingWidgetTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MediaControlFloatingWidgetTheme {
                AppShell()
            }
        }
    }
}
