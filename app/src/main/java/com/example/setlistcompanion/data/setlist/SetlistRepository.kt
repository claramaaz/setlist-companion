package com.example.setlistcompanion.data.setlist

import com.example.setlistcompanion.data.song.SongEntity
import kotlinx.coroutines.flow.Flow

// The one place the Setlist ViewModels ask for setlist data.
// Direction: ViewModel -> Repository -> DAO -> Room.
class SetlistRepository(private val dao: SetlistDao) {

    val setlists: Flow<List<SetlistEntity>> = dao.observeSetlists()

    fun observeSetlist(id: Long): Flow<SetlistEntity?> = dao.observeSetlist(id)

    fun observeSongs(setlistId: Long): Flow<List<SongEntity>> = dao.observeSongsInSetlist(setlistId)

    suspend fun createSetlist(setlist: SetlistEntity): Long = dao.insertSetlist(setlist)

    suspend fun addSong(setlistId: Long, songId: Long) = dao.addSongAtEnd(setlistId, songId)

    suspend fun removeSong(setlistId: Long, songId: Long) = dao.removeSong(setlistId, songId)

    suspend fun reorder(setlistId: Long, orderedSongIds: List<Long>) =
        dao.updatePositions(setlistId, orderedSongIds)
}
