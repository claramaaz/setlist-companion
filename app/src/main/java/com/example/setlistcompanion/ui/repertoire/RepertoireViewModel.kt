package com.example.setlistcompanion.ui.repertoire

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.setlistcompanion.SetlistApp
import com.example.setlistcompanion.data.song.SongEntity
import com.example.setlistcompanion.data.song.SongRepository
import com.example.setlistcompanion.data.song.SongStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SongSort(val label: String) { TITLE("Title"), DIFFICULTY("Difficulty") }

// One explicit state for the song list screen: it shows exactly one of these at a time.
sealed interface RepertoireUiState {
    data object Loading : RepertoireUiState
    data class Content(
        val songs: List<SongEntity>,
        val filter: SongStatus?,   // null = show all songs
        val sort: SongSort
    ) : RepertoireUiState
}

// Owns the state of the Repertoire screens and coordinates the work.
// The screens only render the state and report events (clicks) to this class.
class RepertoireViewModel(private val repository: SongRepository) : ViewModel() {

    // Private + mutable: only the ViewModel can change the filter and the sort.
    private val filter = MutableStateFlow<SongStatus?>(null)
    private val sort = MutableStateFlow(SongSort.TITLE)

    // Public + read-only: the screen observes it but cannot change it.
    // combine = rebuild the state whenever the songs, the filter OR the sort change.
    val uiState: StateFlow<RepertoireUiState> =
        combine(repository.songs, filter, sort) { songs, currentFilter, currentSort ->
            val filtered = if (currentFilter == null) songs else songs.filter { it.status == currentFilter }
            val sorted = when (currentSort) {
                SongSort.TITLE -> filtered.sortedBy { it.title.lowercase() }
                SongSort.DIFFICULTY -> filtered.sortedBy { it.difficulty }
            }
            RepertoireUiState.Content(songs = sorted, filter = currentFilter, sort = currentSort)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = RepertoireUiState.Loading
        )

    // ----- Events coming from the screens -----

    fun onFilterSelected(status: SongStatus?) { filter.value = status }

    fun onSortSelected(newSort: SongSort) { sort.value = newSort }

    suspend fun getSong(id: Long): SongEntity? = repository.getSong(id)

    // onDone runs AFTER the write is finished, so the edit screen closes only once
    // the song is really saved (closing first would clear this ViewModel and cancel the coroutine).
    fun save(song: SongEntity, onDone: () -> Unit) {
        viewModelScope.launch {
            repository.save(song)
            onDone()
        }
    }

    fun delete(song: SongEntity, onDone: () -> Unit) {
        viewModelScope.launch {
            repository.delete(song)
            onDone()
        }
    }

    companion object {
        // Tells viewModel(factory = ...) how to build this ViewModel with its repository,
        // taken from the AppContainer created in SetlistApp.
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as SetlistApp
                RepertoireViewModel(app.container.songRepository)
            }
        }
    }
}
