package org.example.anye.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.example.anye.data.Favorite

@Dao
interface FavoriteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) // Fügt einen Favoriten hinzu oder ersetzt ihn bei ID-Konflikt
    suspend fun insertFavorite(favorite: Favorite)

    @Query("DELETE FROM favorites WHERE eventId = :eventId")
    suspend fun deleteFavorite(eventId: String)

    @Query("SELECT * FROM favorites")
    fun getAllFavorites(): Flow<List<Favorite>>

    @Query("SELECT EXISTS (SELECT 1 FROM favorites WHERE eventId = :eventId)")
    suspend fun isFavorite(eventId: String): Boolean

    @Query("DELETE FROM favorites")
    suspend fun deleteAllFavorites()
}