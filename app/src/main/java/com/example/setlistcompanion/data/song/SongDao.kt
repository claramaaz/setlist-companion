package com.example.setlistcompanion.data.song

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

// The operations the app needs on the "songs" table.
// Reads return a Flow (the UI is told about every change).
// Writes are suspend functions (they run in a coroutine, never on the UI thread).
@Dao
interface SongDao {

    @Query("SELECT * FROM songs ORDER BY title COLLATE NOCASE")
    fun observeAll(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE id = :id")
    suspend fun getById(id: Long): SongEntity?

    // Inserts when id = 0 (new song), updates when the id already exists.
    @Upsert
    suspend fun upsert(song: SongEntity)

    @Delete
    suspend fun delete(song: SongEntity)
}
