package com.cuso.mobile.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.cuso.mobile.database.entities.SalesStatusEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SalesStatusDao {
    @Query("SELECT * FROM sales_status ORDER BY `order` ASC")
    fun getAll(): Flow<List<SalesStatusEntity>>

    @Upsert
    suspend fun upsertAll(items: List<SalesStatusEntity>)

    @Query("DELETE FROM sales_status")
    suspend fun clearAll()
}