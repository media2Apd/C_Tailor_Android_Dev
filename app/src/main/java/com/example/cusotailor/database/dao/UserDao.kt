package com.example.cusotailor.database.dao

import androidx.room.*
import com.example.cusotailor.database.entities.UserEntity

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT * FROM user LIMIT 1")
    suspend fun getUser(): UserEntity?

    @Query("DELETE FROM user")
    suspend fun clearUser()
}