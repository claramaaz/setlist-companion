package com.example.setlistcompanion.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.setlistcompanion.data.setlist.SetlistDao
import com.example.setlistcompanion.data.setlist.SetlistEntity
import com.example.setlistcompanion.data.setlist.SetlistSongCrossRef
import com.example.setlistcompanion.data.song.SongDao
import com.example.setlistcompanion.data.song.SongEntity

// The Room database: lists every table and gives access to the DAOs.
// If you change an entity, increase "version" (during development the old data is wiped,
// see fallbackToDestructiveMigration in AppContainer).
@Database(
    entities = [SongEntity::class, SetlistEntity::class, SetlistSongCrossRef::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun setlistDao(): SetlistDao
}
