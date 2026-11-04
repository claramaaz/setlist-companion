package com.example.setlistcompanion.data.setlist

import androidx.room.Entity
import androidx.room.PrimaryKey

// One row of the "setlists" table.
@Entity(tableName = "setlists")
data class SetlistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val eventDate: Long? = null,  // epoch millis, optional
    val venue: String = ""
)
