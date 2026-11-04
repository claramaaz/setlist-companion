package com.example.setlistcompanion.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector

// The two tabs of the bottom bar: one per feature.
sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Repertoire : Screen("repertoire", "Repertoire", Icons.Filled.Home)
    data object Setlists : Screen("setlists", "Setlists", Icons.AutoMirrored.Filled.List)
}
