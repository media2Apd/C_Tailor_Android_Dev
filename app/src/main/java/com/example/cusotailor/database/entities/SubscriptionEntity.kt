package com.example.cusotailor.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subscription")
data class SubscriptionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val orgId: String,        // Foreign key to OrganizationEntity
    val startDate: String,
    val endDate: String,
    val status: String,
    val memberLimit: Int,
    // featuresEnabled - separate table
)