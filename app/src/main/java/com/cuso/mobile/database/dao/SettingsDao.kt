package com.cuso.mobile.database.dao

import androidx.room.*
import com.cuso.mobile.database.entities.SettingsEntity
import com.cuso.mobile.database.entities.WorkingDayEntity

@Dao
interface SettingsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: SettingsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkingDays(days: List<WorkingDayEntity>)

    @Query("SELECT * FROM settings LIMIT 1")
    suspend fun getSettings(): SettingsEntity?

    @Query("SELECT day FROM working_days WHERE orgId = :orgId")
    suspend fun getWorkingDays(orgId: String): List<String>

    @Query("DELETE FROM settings")
    suspend fun clearSettings()

    @Query("DELETE FROM working_days")
    suspend fun clearWorkingDays()
}