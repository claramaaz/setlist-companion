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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface SetlistsUiState {
    data object Loading : SetlistsUiState
    data class Content(val setlists: List<SetlistEntity>) : SetlistsUiState
}

// Owns the state of the list of setlists.
class SetlistsViewModel(private val repository: SetlistRepository) : ViewModel() {

    val uiState: StateFlow<SetlistsUiState> =
        repository.setlists
            .map<List<SetlistEntity>, SetlistsUiState> { SetlistsUiState.Content(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = SetlistsUiState.Loading
            )

    fun createSetlist(name: String, eventDate: Long?, venue: String) {
        viewModelScope.launch {
            repository.createSetlist(
                SetlistEntity(name = name.trim(), eventDate = eventDate, venue = venue.trim())
            )
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as SetlistApp
                SetlistsViewModel(app.container.setlistRepository)
            }
        }
    }
}
