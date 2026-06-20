package com.cuso.mobile.database.dao

import androidx.room.*
import com.cuso.mobile.database.entities.TokensEntity

@Dao
interface TokensDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTokens(tokens: TokensEntity)

    @Query("SELECT * FROM tokens ORDER BY id DESC LIMIT 1")
    suspend fun getTokens(): TokensEntity?

    @Query("DELETE FROM tokens")
    suspend fun clearTokens()
}