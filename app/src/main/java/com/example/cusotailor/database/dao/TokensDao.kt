package com.example.cusotailor.database.dao

import androidx.room.*
import com.example.cusotailor.database.entities.TokensEntity

@Dao
interface TokensDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTokens(tokens: TokensEntity)

    @Query("SELECT * FROM tokens LIMIT 1")
    suspend fun getTokens(): TokensEntity?

    @Query("DELETE FROM tokens")
    suspend fun clearTokens()
}