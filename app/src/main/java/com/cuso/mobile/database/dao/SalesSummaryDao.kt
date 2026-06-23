package com.cuso.mobile.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.cuso.mobile.database.entities.SalesSummaryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SalesSummaryDao {
    @Query("SELECT * FROM sales_summary WHERE id = 1")
    fun getSummary(): Flow<SalesSummaryEntity?>

    @Upsert
    suspend fun upsert(summary: SalesSummaryEntity)

    @Query("DELETE FROM sales_summary")
    suspend fun clear()
}