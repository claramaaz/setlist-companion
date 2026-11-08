package com.example.setlistcompanion.ui.setlists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.setlistcompanion.SetlistApp
import com.example.setlistcompanion.data.setlist.SetlistEntity
import com.example.setlistcompanion.data.setlist.SetlistRepository
import com.example.setlistcompanion.data.song.SongEntity
import com.example.setlistcompanion.data.song.SongRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface SetlistDetailUiState {
    data object Loading : SetlistDetailUiState
    data class Content(
        val setlist: SetlistEntity,
        val songs: List<SongEntity>,          // in setlist order
        val availableSongs: List<SongEntity>  // repertoire songs not in this setlist yet
    ) : SetlistDetailUiState
}

// Owns the state of one setlist: its songs, the picker, and reordering.
class SetlistDetailViewModel(
    private val setlistId: Long,
    private val setlistRepository: SetlistRepository,
    songRepository: SongRepository
) : ViewModel() {

    // combine = rebuild the state whenever the setlist, its songs OR the repertoire change.
    val uiState: StateFlow<SetlistDetailUiState> =
        combine(
            setlistRepository.observeSetlist(setlistId),
            setlistRepository.observeSongs(setlistId),
            songRepository.songs
        ) { setlist, songs, allSongs ->
            if (setlist == null) {
                SetlistDetailUiState.Loading
            } else {
                val usedIds = songs.map { it.id }.toSet()
                SetlistDetailUiState.Content(
                    setlist = setlist,
                    songs = songs,
                    availableSongs = allSongs.filter { it.id !in usedIds }
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SetlistDetailUiState.Loading
        )

    fun addSong(songId: Long) {
        viewModelScope.launch { setlistRepository.addSong(setlistId, songId) }
    }

    fun removeSong(songId: Long) {
        viewModelScope.launch { setlistRepository.removeSong(setlistId, songId) }
    }

    fun moveUp(songId: Long) = move(songId, -1)

    fun moveDown(songId: Long) = move(songId, +1)

    // Computes the new order, then saves it. We do not touch the UI here:
    // the write makes the ORDER BY position query emit again, and the list updates by itself.
    private fun move(songId: Long, offset: Int) {
        val state = uiState.value as? SetlistDetailUiState.Content ?: return
        val ids = state.songs.map { it.id }.toMutableList()
        val from = ids.indexOf(songId)
        val to = from + offset
        if (from == -1 || to !in ids.indices) return

        ids.removeAt(from)
        ids.add(to, songId)
        viewModelScope.launch { setlistRepository.reorder(setlistId, ids) }
    }

    companion object {
        // A function, because this ViewModel also needs to know which setlist it shows.
        fun factory(setlistId: Long): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as SetlistApp
                SetlistDetailViewModel(
                    setlistId,
                    app.container.setlistRepository,
                    app.container.songRepository
                )
            }
        }
    }
}
