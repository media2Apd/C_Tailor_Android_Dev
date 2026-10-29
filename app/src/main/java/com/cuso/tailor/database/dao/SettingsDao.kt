package com.cuso.tailor.database.dao

import androidx.room.*
import com.cuso.tailor.database.entities.SettingsEntity
import com.cuso.tailor.database.entities.WorkingDayEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: SettingsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkingDays(days: List<WorkingDayEntity>)

    // Keep existing suspend function if needed
    @Query("SELECT * FROM settings LIMIT 1")
    suspend fun getSettings(): SettingsEntity?

    // Add this reactive Flow to observe DB changes automatically
    @Query("SELECT * FROM settings LIMIT 1")
    fun getSettingsFlow(): Flow<SettingsEntity?>

    @Query("SELECT day FROM working_days WHERE orgId = :orgId")
    suspend fun getWorkingDays(orgId: String): List<String>

    @Query("DELETE FROM settings")
    suspend fun clearSettings()

    @Query("DELETE FROM working_days")
    suspend fun clearWorkingDays()
}