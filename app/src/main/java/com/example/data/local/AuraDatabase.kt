package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.entities.FavoriteEntity
import com.example.data.local.entities.PlayHistoryEntity
import com.example.data.local.entities.PlaylistEntity
import com.example.data.local.entities.PlaylistSongCrossRef

@Database(
    entities = [
        PlaylistEntity::class,
        PlaylistSongCrossRef::class,
        FavoriteEntity::class,
        PlayHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AuraDatabase : RoomDatabase() {

    abstract fun auraDao(): AuraDao

    companion object {
        @Volatile
        private var INSTANCE: AuraDatabase? = null

        fun getInstance(context: Context): AuraDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AuraDatabase::class.java,
                    "aura_music.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
