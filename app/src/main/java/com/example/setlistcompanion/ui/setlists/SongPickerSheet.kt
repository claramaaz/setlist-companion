package com.example.setlistcompanion.ui.setlists

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.setlistcompanion.data.song.SongEntity

// Bottom sheet listing the repertoire songs that are not in the setlist yet.
// Tapping a song adds it; it then disappears from this list because the state updates.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongPickerSheet(
    songs: List<SongEntity>,
    onSongPicked: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Text(
            "Add songs",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        if (songs.isEmpty()) {
            Text(
                "No more songs to add. New songs are created in the Repertoire tab.",
                modifier = Modifier.padding(16.dp)
            )
        } else {
            LazyColumn {
                items(songs, key = { it.id }) { song ->
                    ListItem(
                        headlineContent = { Text(song.title) },
                        supportingContent = {
                            val artist = if (song.artist.isBlank()) "" else "${song.artist} · "
                            Text("${artist}Key ${song.musicalKey}")
                        },
                        modifier = Modifier.fillMaxWidth().clickable { onSongPicked(song.id) }
                    )
                }
            }
        }
    }
}
