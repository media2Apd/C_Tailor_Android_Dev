package com.cuso.mobile.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cuso.mobile.database.entities.SelectedGarment
import kotlinx.coroutines.flow.Flow

@Dao
interface SelectedGarmentDao {

    @Query("SELECT * FROM selected_garments WHERE orderSessionId = :sessionId")
    fun getGarmentsForSession(sessionId: String): Flow<List<SelectedGarment>>

    @Query("SELECT * FROM selected_garments WHERE categoryId = :categoryId LIMIT 1")
    fun getGarmentByCategoryId(categoryId: String): Flow<SelectedGarment?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGarment(garment: SelectedGarment)

    @Query("DELETE FROM selected_garments WHERE id = :garmentId")
    suspend fun deleteGarmentById(garmentId: String)

    @Query("DELETE FROM selected_garments WHERE categoryId = :categoryId")
    suspend fun deleteGarmentByCategoryId(categoryId: String)

    @Query("DELETE FROM selected_garments WHERE orderSessionId = :sessionId")
    suspend fun clearSession(sessionId: String)
}