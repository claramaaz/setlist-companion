package com.example.setlistcompanion.ui.setlists

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
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
import com.example.setlistcompanion.data.setlist.SetlistEntity
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

// The list of all setlists, with a "+" button that opens the "New setlist" dialog.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetlistListScreen(
    onSetlistClick: (Long) -> Unit,
    viewModel: SetlistsViewModel = viewModel(factory = SetlistsViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Setlists") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "New setlist")
            }
        }
    ) { padding ->
        when (val state = uiState) {
            SetlistsUiState.Loading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is SetlistsUiState.Content -> {
                if (state.setlists.isEmpty()) {
                    Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                        Text("No setlists yet. Tap + to create one.")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.setlists, key = { it.id }) { setlist ->
                            SetlistCard(setlist = setlist, onClick = { onSetlistClick(setlist.id) })
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        NewSetlistDialog(
            onDismiss = { showDialog = false },
            onCreate = { name, date, venue ->
                viewModel.createSetlist(name, date, venue)
                showDialog = false
            }
        )
    }
}

@Composable
private fun SetlistCard(setlist: SetlistEntity, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text(setlist.name, style = MaterialTheme.typography.titleMedium)
            val details = setlistDetails(setlist)
            if (details.isNotEmpty()) {
                Text(details, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

// rememberSaveable keeps what was typed when the phone rotates.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewSetlistDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, eventDate: Long?, venue: String) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var venue by rememberSaveable { mutableStateOf("") }
    var eventDate by rememberSaveable { mutableStateOf<Long?>(null) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New setlist") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = venue,
                    onValueChange = { venue = it },
                    label = { Text("Venue (optional)") },
                    singleLine = true
                )
                OutlinedButton(onClick = { showDatePicker = true }) {
                    Text(eventDate?.let { formatDate(it) } ?: "Pick a date (optional)")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onCreate(name, eventDate, venue) },
                enabled = name.isNotBlank()
            ) { Text("Create") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = eventDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    eventDate = datePickerState.selectedDateMillis
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

// "Nov 13, 2026 · The Basement", or only the parts that were filled in.
internal fun setlistDetails(setlist: SetlistEntity): String =
    listOfNotNull(setlist.eventDate?.let { formatDate(it) }, setlist.venue.ifBlank { null })
        .joinToString(" · ")

// The date picker returns midnight UTC, so it is formatted in UTC to show the day that was picked.
private fun formatDate(millis: Long): String =
    Instant.ofEpochMilli(millis)
        .atZone(ZoneOffset.UTC)
        .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
