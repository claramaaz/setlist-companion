package com.example.setlistcompanion.ui.setlists

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.setlistcompanion.data.song.SongEntity

// One setlist: its songs in order, with up / down / remove on each row and "+" to add songs.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetlistDetailScreen(
    setlistId: Long,
    onBack: () -> Unit,
    viewModel: SetlistDetailViewModel = viewModel(factory = SetlistDetailViewModel.factory(setlistId))
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showPicker by rememberSaveable { mutableStateOf(false) }
    val content = uiState as? SetlistDetailUiState.Content

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(content?.setlist?.name ?: "")
                        val details = content?.setlist?.let { setlistDetails(it) }.orEmpty()
                        if (details.isNotEmpty()) {
                            Text(details, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showPicker = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add songs")
            }
        }
    ) { padding ->
        when (val state = uiState) {
            SetlistDetailUiState.Loading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is SetlistDetailUiState.Content -> {
                if (state.songs.isEmpty()) {
                    Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                        Text("No songs yet. Tap + to add some.")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // key = song.id lets Compose move the row instead of rebuilding it.
                        itemsIndexed(state.songs, key = { _, song -> song.id }) { index, song ->
                            SetlistSongRow(
                                number = index + 1,
                                song = song,
                                canMoveUp = index > 0,
                                canMoveDown = index < state.songs.lastIndex,
                                onMoveUp = { viewModel.moveUp(song.id) },
                                onMoveDown = { viewModel.moveDown(song.id) },
                                onRemove = { viewModel.removeSong(song.id) },
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                }
            }
        }
    }

    if (showPicker && content != null) {
        SongPickerSheet(
            songs = content.availableSongs,
            onSongPicked = viewModel::addSong,
            onDismiss = { showPicker = false }
        )
    }
}

@Composable
private fun SetlistSongRow(
    number: Int,
    song: SongEntity,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(start = 16.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("$number", style = MaterialTheme.typography.titleMedium)
            Column(Modifier.weight(1f)) {
                Text(song.title, style = MaterialTheme.typography.titleMedium)
                val artist = if (song.artist.isBlank()) "" else "${song.artist} · "
                Text("${artist}Key ${song.musicalKey}", style = MaterialTheme.typography.bodySmall)
            }
            Row {
                IconButton(onClick = onMoveUp, enabled = canMoveUp) {
                    Icon(Icons.Filled.ArrowUpward, contentDescription = "Move ${song.title} up")
                }
                IconButton(onClick = onMoveDown, enabled = canMoveDown) {
                    Icon(Icons.Filled.ArrowDownward, contentDescription = "Move ${song.title} down")
                }
                IconButton(onClick = onRemove) {
                    Icon(Icons.Filled.Close, contentDescription = "Remove ${song.title}")
                }
            }
        }
    }
}
