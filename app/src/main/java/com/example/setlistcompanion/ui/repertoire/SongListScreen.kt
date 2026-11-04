package com.example.setlistcompanion.ui.repertoire

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.setlistcompanion.data.song.SongEntity
import com.example.setlistcompanion.data.song.SongStatus

// The list of all songs, with filter and sort chips and a "+" button.
// It only RENDERS the state and REPORTS events: the ViewModel decides what changes.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongListScreen(
    onAddSong: () -> Unit,
    onSongClick: (Long) -> Unit,
    viewModel: RepertoireViewModel = viewModel(factory = RepertoireViewModel.Factory)
) {
    // Reads the StateFlow; stops collecting when the screen is not visible.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Repertoire") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddSong) {
                Icon(Icons.Filled.Add, contentDescription = "Add song")
            }
        }
    ) { padding ->
        // The sealed interface makes "when" cover every possible state.
        when (val state = uiState) {
            RepertoireUiState.Loading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is RepertoireUiState.Content -> {
                SongListContent(
                    state = state,
                    onFilterSelected = viewModel::onFilterSelected,
                    onSortSelected = viewModel::onSortSelected,
                    onSongClick = onSongClick,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

@Composable
private fun SongListContent(
    state: RepertoireUiState.Content,
    onFilterSelected: (SongStatus?) -> Unit,
    onSortSelected: (SongSort) -> Unit,
    onSongClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxSize()) {

        // Filter row: All / To learn / Learning / Ready
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = state.filter == null,
                onClick = { onFilterSelected(null) },
                label = { Text("All") }
            )
            SongStatus.entries.forEach { status ->
                FilterChip(
                    selected = state.filter == status,
                    onClick = { onFilterSelected(status) },
                    label = { Text(status.label) }
                )
            }
        }

        // Sort row: Title / Difficulty
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Sort by", style = MaterialTheme.typography.labelLarge)
            SongSort.entries.forEach { sort ->
                FilterChip(
                    selected = state.sort == sort,
                    onClick = { onSortSelected(sort) },
                    label = { Text(sort.label) }
                )
            }
        }

        if (state.songs.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(if (state.filter == null) "No songs yet. Tap + to add one." else "No songs with this status.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // key = song.id lets Compose keep each row when the list is filtered or sorted.
                items(state.songs, key = { it.id }) { song ->
                    SongCard(song = song, onClick = { onSongClick(song.id) })
                }
            }
        }
    }
}

@Composable
private fun SongCard(song: SongEntity, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(song.title, style = MaterialTheme.typography.titleMedium)
                val artist = if (song.artist.isBlank()) "" else "${song.artist} · "
                Text(
                    "${artist}Key ${song.musicalKey} · Difficulty ${song.difficulty}/5",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            AssistChip(onClick = onClick, label = { Text(song.status.label) })
        }
    }
}
