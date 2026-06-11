package com.cuso.mobile.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val profilePicture: String?,
    val role: String,
    val memberId: String,
    val organizationId:String
)