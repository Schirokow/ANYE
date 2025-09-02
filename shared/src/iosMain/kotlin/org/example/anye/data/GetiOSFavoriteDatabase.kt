package org.example.anye.data

import androidx.room.Room
import platform.Foundation.NSHomeDirectory
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

fun getDatabase(): FavoriteDatabase {
    val dbFile = NSHomeDirectory() + "/favorites"
    return Room.databaseBuilder<FavoriteDatabase>(
        name = dbFile,
//        factory = { PostDatabase::class.instantiateImpl() }
    )
        .setDriver(BundledSQLiteDriver())
        .build()
}