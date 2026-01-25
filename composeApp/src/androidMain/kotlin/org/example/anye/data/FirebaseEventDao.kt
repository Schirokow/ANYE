package org.example.anye.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FirebaseEventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) // Fügt einen Event hinzu oder ersetzt ihn bei ID-Konflikt
    suspend fun insertEvent(event: FirebaseEvent)

    @Query("DELETE FROM events WHERE id = :id")
    suspend fun deleteEvent(id: String)

    @Query("SELECT * FROM events")
    fun getAllEvents(): Flow<List<FirebaseEvent>>

    @Query("SELECT EXISTS (SELECT 1 FROM events WHERE id = :id)")
    suspend fun isFavorite(id: String): Boolean

    @Query("DELETE FROM events")
    suspend fun deleteAllEvents()
}