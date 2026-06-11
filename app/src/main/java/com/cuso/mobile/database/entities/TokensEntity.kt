package com.cuso.mobile.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tokens")
data class TokensEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: String,       // Foreign key to UserEntity
    val accessToken: String,
    val refreshToken: String,
    val csrfToken: String,
    val sessionLoginToken: String,
    val orgToken: String
)