package com.example.setlistcompanion.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.setlistcompanion.ui.repertoire.SongEditScreen
import com.example.setlistcompanion.ui.repertoire.SongListScreen
import com.example.setlistcompanion.ui.setlists.SetlistDetailScreen
import com.example.setlistcompanion.ui.setlists.SetlistListScreen

// Bottom bar + navigation between the two features.
// Each feature replaces ONLY its own placeholder composable(...) below.
@Composable
fun SetlistCompanionApp() {
    val navController = rememberNavController()
    val tabs = listOf(Screen.Repertoire, Screen.Setlists)

    Scaffold(
        bottomBar = {
            NavigationBar {
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route
                tabs.forEach { screen ->
                    NavigationBarItem(
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Repertoire.route,
            // Only the bottom padding: each screen's own top bar handles the top.
            // consumeWindowInsets stops the inner screens from adding the bottom space twice.
            modifier = Modifier
                .padding(bottom = innerPadding.calculateBottomPadding())
                .consumeWindowInsets(PaddingValues(bottom = innerPadding.calculateBottomPadding()))
        ) {
            // ---------- Feature 1: Repertoire (Clara) ----------
            composable(Screen.Repertoire.route) {
                SongListScreen(
                    onAddSong = { navController.navigate("song_edit/-1") },
                    onSongClick = { id -> navController.navigate("song_edit/$id") }
                )
            }
            // "{songId}" is an argument in the route; -1 means "new song".
            composable(
                route = "song_edit/{songId}",
                arguments = listOf(navArgument("songId") { type = NavType.LongType })
            ) { entry ->
                val songId = entry.arguments?.getLong("songId") ?: -1L
                SongEditScreen(songId = songId, onDone = { navController.popBackStack() })
            }

            // ---------- Feature 2: Setlists (Mia) ----------
            composable(Screen.Setlists.route) {
                SetlistListScreen(onSetlistClick = { id -> navController.navigate("setlist_detail/$id") })
            }
            composable(
                route = "setlist_detail/{setlistId}",
                arguments = listOf(navArgument("setlistId") { type = NavType.LongType })
            ) { entry ->
                val setlistId = entry.arguments?.getLong("setlistId") ?: -1L
                SetlistDetailScreen(setlistId = setlistId, onBack = { navController.popBackStack() })
            }
        }
    }
}
