package com.example.cusotailor.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val orgId: String,        // Foreign key to OrganizationEntity
    val country: String,
    val state: String,
    val portalName: String,
    val termsAccepted: Boolean,
    val marketingEmails: Boolean,
    val companySize: String,
    val timezone: String,
    val currency: String,
    val language: String,
    val address: String,
    val city: String,
    val pincode: String,
    // workingDays - separate table
)