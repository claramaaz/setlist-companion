package com.example.setlistcompanion.ui.repertoire

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.setlistcompanion.data.song.SongEntity
import com.example.setlistcompanion.data.song.SongStatus
import kotlin.math.roundToInt

// Add a new song (songId = -1) or edit an existing one.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongEditScreen(
    songId: Long,
    onDone: () -> Unit,
    viewModel: RepertoireViewModel = viewModel(factory = RepertoireViewModel.Factory)
) {
    val isNew = songId == -1L

    // Temporary input typed by the user: rememberSaveable keeps it across rotation.
    // It is NOT the database: the song only becomes durable when Save calls the ViewModel.
    var title by rememberSaveable { mutableStateOf("") }
    var artist by rememberSaveable { mutableStateOf("") }
    var musicalKey by rememberSaveable { mutableStateOf("C") }
    var difficulty by rememberSaveable { mutableIntStateOf(3) }
    var status by rememberSaveable { mutableStateOf(SongStatus.TO_LEARN) }
    var notes by rememberSaveable { mutableStateOf("") }
    var loaded by rememberSaveable { mutableStateOf(false) }
    var triedToSave by rememberSaveable { mutableStateOf(false) }
    var saving by rememberSaveable { mutableStateOf(false) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    // When editing: load the song from the database once and fill the fields.
    LaunchedEffect(songId) {
        if (!isNew && !loaded) {
            viewModel.getSong(songId)?.let { song ->
                title = song.title
                artist = song.artist
                musicalKey = song.musicalKey
                difficulty = song.difficulty
                status = song.status
                notes = song.notes
            }
            loaded = true
        }
    }

    fun currentSong() = SongEntity(
        id = if (isNew) 0 else songId,
        title = title.trim(),
        artist = artist.trim(),
        musicalKey = musicalKey.trim(),
        difficulty = difficulty,
        status = status,
        notes = notes.trim()
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isNew) "New song" else "Edit song") },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!isNew) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete song")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val titleError = triedToSave && title.isBlank()
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title *") },
                isError = titleError,
                supportingText = { if (titleError) Text("The title is required") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = artist,
                onValueChange = { artist = it },
                label = { Text("Artist") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = musicalKey,
                onValueChange = { musicalKey = it },
                label = { Text("Key (e.g. G, Em, Bb)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Text("Difficulty: $difficulty / 5", style = MaterialTheme.typography.labelLarge)
            Slider(
                value = difficulty.toFloat(),
                onValueChange = { difficulty = it.roundToInt() },
                valueRange = 1f..5f,
                steps = 3   // 3 stops between 1 and 5 -> 1, 2, 3, 4, 5
            )

            Text("Status", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SongStatus.entries.forEach { option ->
                    FilterChip(
                        selected = status == option,
                        onClick = { status = option },
                        label = { Text(option.label) }
                    )
                }
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    triedToSave = true
                    if (title.isNotBlank()) {
                        saving = true   // prevents saving the same new song twice
                        viewModel.save(currentSong(), onDone = onDone)
                    }
                },
                enabled = !saving,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save") }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete this song?") },
            text = { Text("It will also be removed from every setlist.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.delete(currentSong(), onDone = onDone)
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }
}
