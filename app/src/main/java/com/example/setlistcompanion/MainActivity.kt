package com.example.setlistcompanion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.setlistcompanion.ui.navigation.SetlistCompanionApp
import com.example.setlistcompanion.ui.theme.SetlistCompanionTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SetlistCompanionTheme {
                SetlistCompanionApp()
            }
        }
    }
}
