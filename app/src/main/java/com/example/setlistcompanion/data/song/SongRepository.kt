package com.example.setlistcompanion.data.song

import kotlinx.coroutines.flow.Flow

// The one place the ViewModels ask for song data.
// Direction: ViewModel -> Repository -> DAO -> Room.
// The ViewModel never knows that the songs come from Room.
class SongRepository(private val dao: SongDao) {

    // Also used by the Setlists feature to pick songs.
    val songs: Flow<List<SongEntity>> = dao.observeAll()

    suspend fun getSong(id: Long): SongEntity? = dao.getById(id)

    suspend fun save(song: SongEntity) = dao.upsert(song)

    suspend fun delete(song: SongEntity) = dao.delete(song)
}
