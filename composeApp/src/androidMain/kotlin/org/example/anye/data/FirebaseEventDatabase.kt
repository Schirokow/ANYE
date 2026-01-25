package org.example.anye.data

import androidx.room.Database
import androidx.room.RoomDatabase


@Database(entities = [FirebaseEvent::class], version = 1, exportSchema = false)
abstract class FirebaseEventRoomDatabase : RoomDatabase() {
    abstract fun firebaseEventDao(): FirebaseEventDao
}

