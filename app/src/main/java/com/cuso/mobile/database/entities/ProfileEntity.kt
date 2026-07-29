package com.cuso.mobile.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

// entity
@Entity(tableName = "profile")
data class ProfileEntity(
    @PrimaryKey val userId: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val profilePicture: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
)