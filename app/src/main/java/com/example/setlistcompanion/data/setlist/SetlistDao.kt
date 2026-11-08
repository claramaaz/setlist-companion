package com.example.setlistcompanion.data.setlist

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.setlistcompanion.data.song.SongEntity
import kotlinx.coroutines.flow.Flow

// The operations the app needs on "setlists" and "setlist_songs".
// Reads return a Flow, so Room emits a new list after every write.
// It is an abstract class (not an interface) so the @Transaction functions can have a body.
@Dao
abstract class SetlistDao {

    @Query("SELECT * FROM setlists ORDER BY name COLLATE NOCASE")
    abstract fun observeSetlists(): Flow<List<SetlistEntity>>

    @Query("SELECT * FROM setlists WHERE id = :id")
    abstract fun observeSetlist(id: Long): Flow<SetlistEntity?>

    // JOIN: keep the songs that belong to this setlist.
    // ORDER BY position: this is what shows the songs in the saved order.
    @Query(
        """
        SELECT songs.* FROM songs
        INNER JOIN setlist_songs ON songs.id = setlist_songs.songId
        WHERE setlist_songs.setlistId = :setlistId
        ORDER BY setlist_songs.position
        """
    )
    abstract fun observeSongsInSetlist(setlistId: Long): Flow<List<SongEntity>>

    @Insert
    abstract suspend fun insertSetlist(setlist: SetlistEntity): Long

    // IGNORE: if the song is already in this setlist, nothing happens.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun addSong(crossRef: SetlistSongCrossRef)

    @Query("DELETE FROM setlist_songs WHERE setlistId = :setlistId AND songId = :songId")
    abstract suspend fun removeSong(setlistId: Long, songId: Long)

    // -1 when the setlist is empty, so the first song gets position 0.
    @Query("SELECT COALESCE(MAX(position), -1) FROM setlist_songs WHERE setlistId = :setlistId")
    abstract suspend fun lastPosition(setlistId: Long): Int

    @Query("UPDATE setlist_songs SET position = :position WHERE setlistId = :setlistId AND songId = :songId")
    abstract suspend fun updatePosition(setlistId: Long, songId: Long, position: Int)

    // Reading the last position and inserting must happen as one step.
    @Transaction
    open suspend fun addSongAtEnd(setlistId: Long, songId: Long) {
        addSong(SetlistSongCrossRef(setlistId, songId, lastPosition(setlistId) + 1))
    }

    // Saves the whole new order at once: either every position changes, or none does.
    @Transaction
    open suspend fun updatePositions(setlistId: Long, orderedSongIds: List<Long>) {
        orderedSongIds.forEachIndexed { index, songId ->
            updatePosition(setlistId, songId, index)
        }
    }
}
