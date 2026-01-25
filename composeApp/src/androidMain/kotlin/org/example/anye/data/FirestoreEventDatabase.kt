package org.example.anye.data

import android.content.Context
import androidx.room.Room

object FirebaseEventDatabase {
    @Volatile
    private var INSTANCE: FirebaseEventRoomDatabase? = null

    fun getDatabase(context: Context): FirebaseEventRoomDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                FirebaseEventRoomDatabase::class.java,
                "firebase_events.db"
            ).build()
            INSTANCE = instance
            instance
        }
    }
}