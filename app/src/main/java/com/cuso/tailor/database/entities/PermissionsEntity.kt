package com.cuso.tailor.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_permissions")
data class UserPermissionEntity(
    @PrimaryKey
    val id: Int = 1,
    val isOwner: Boolean,
    val visiblePermissionsJson: String,
    val rawPermissionsJson: String = "{}", // Stores Map<String, Map<String, ActionPermission>>
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "plan_modules")
data class PlanModuleEntity(
    @PrimaryKey
    val id: Int = 1,
    val modulesJson: String, // Serialized JSON of ModulePermissionsData
    val updatedAt: Long = System.currentTimeMillis()
)