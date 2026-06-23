package com.cuso.mobile.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cuso.mobile.database.entities.LeadEntity
import kotlinx.coroutines.flow.Flow


    // database/dao/LeadDao.kt
    @Dao
    interface LeadDao {
        @Query("SELECT * FROM leads ORDER BY createdAt DESC")
        fun getAll(): Flow<List<LeadEntity>>

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun upsert(lead: LeadEntity)

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun upsertAll(leads: List<LeadEntity>)

        @Query("DELETE FROM leads")
        suspend fun clearAll()

        @Query("SELECT * FROM leads WHERE id = :id")
        suspend fun getById(id: String): LeadEntity?

        @Query("DELETE FROM leads WHERE id = :id")
        suspend fun deleteById(id: String)
    }
