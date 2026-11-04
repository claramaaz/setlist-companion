package com.example.setlistcompanion.data.setlist

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.example.setlistcompanion.data.song.SongEntity

// Join table: which song is in which setlist, and at which position.
// "position" is what keeps the order after the app restarts.
@Entity(
    tableName = "setlist_songs",
    primaryKeys = ["setlistId", "songId"],
    foreignKeys = [
        ForeignKey(
            entity = SetlistEntity::class,
            parentColumns = ["id"],
            childColumns = ["setlistId"],
            onDelete = ForeignKey.CASCADE   // delete a setlist -> its rows here are deleted
        ),
        ForeignKey(
            entity = SongEntity::class,
            parentColumns = ["id"],
            childColumns = ["songId"],
            onDelete = ForeignKey.CASCADE   // delete a song -> it leaves every setlist
        )
    ],
    indices = [Index("songId")]
)
data class SetlistSongCrossRef(
    val setlistId: Long,
    val songId: Long,
    val position: Int
)
