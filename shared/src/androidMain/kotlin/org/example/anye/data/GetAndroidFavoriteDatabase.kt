package org.example.anye.data

import android.content.Context
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

object GetAndroidFavoriteDatabase {
    fun getDatabase(context: Context): FavoriteDatabase {
        val dbFile = context.getDatabasePath("favorites")
        return Room.databaseBuilder<FavoriteDatabase>(
            context = context.applicationContext,
            name = dbFile.absolutePath
        )
            .setDriver(BundledSQLiteDriver())
            .build()
    }
}