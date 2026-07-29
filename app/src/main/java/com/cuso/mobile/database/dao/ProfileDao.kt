package com.cuso.mobile.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.cuso.mobile.database.entities.ProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profile WHERE userId = :userId LIMIT 1")
    fun observeProfile(userId: String): Flow<ProfileEntity?>

    @Upsert
    suspend fun upsert(profile: ProfileEntity)

    @Query("UPDATE profile SET profilePicture = :url, updatedAt = :ts WHERE userId = :userId")
    suspend fun updateProfilePicture(userId: String, url: String?, ts: Long = System.currentTimeMillis())
}