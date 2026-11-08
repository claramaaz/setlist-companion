package com.example.setlistcompanion

import android.content.Context
import androidx.room.Room
import com.example.setlistcompanion.data.local.AppDatabase
import com.example.setlistcompanion.data.setlist.SetlistRepository
import com.example.setlistcompanion.data.song.SongRepository

// Manual dependency injection: builds the database once.
// Each feature adds its repository here (one line each), e.g.
//   val songRepository = SongRepository(database.songDao())
class AppContainer(context: Context) {

    val database: AppDatabase = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "setlist_companion.db"
    )
        .fallbackToDestructiveMigration(dropAllTables = true) // dev only: wipes data when version changes
        .build()

    // Feature 1: Repertoire
    val songRepository = SongRepository(database.songDao())

    // Feature 2: Setlists
    val setlistRepository = SetlistRepository(database.setlistDao())
}
