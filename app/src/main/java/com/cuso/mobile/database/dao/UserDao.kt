package com.cuso.mobile.database.dao

import androidx.room.*
import com.cuso.mobile.database.entities.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT * FROM user LIMIT 1")
    suspend fun getUser(): UserEntity?

    @Query("SELECT * FROM user LIMIT 1")
    fun getUserFlow(): Flow<UserEntity?>

    @Query("DELETE FROM user")
    suspend fun clearUser()

    @Update
    suspend fun updateUser(user: UserEntity)
}