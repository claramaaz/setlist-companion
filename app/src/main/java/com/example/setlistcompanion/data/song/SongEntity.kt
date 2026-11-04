package com.example.setlistcompanion.data.song

import androidx.room.Entity
import androidx.room.PrimaryKey

// Room saves an enum as its name ("TO_LEARN", "LEARNING", "READY"), so no TypeConverter is needed.
enum class SongStatus(val label: String) {
    TO_LEARN("To learn"),
    LEARNING("Learning"),
    READY("Ready")
}

// One row of the "songs" table.
@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val artist: String = "",
    val musicalKey: String,       // e.g. "G", "Em", "Bb"
    val difficulty: Int,          // 1..5
    val status: SongStatus,
    val durationSec: Int = 0,
    val notes: String = ""
)
