package com.cuso.mobile.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sales_status")
data class SalesStatusEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "code") val code: String,
    @ColumnInfo(name = "color") val color: String,
    @ColumnInfo(name = "order") val order: Int,
    @ColumnInfo(name = "conversion_status") val conversionStatus: Boolean,
    @ColumnInfo(name = "is_default") val isDefault: Boolean,
    @ColumnInfo(name = "organization_id") val organizationId: String?,
    @ColumnInfo(name = "version") val version: Int,
    @ColumnInfo(name = "created_at") val createdAt: String,
    @ColumnInfo(name = "updated_at") val updatedAt: String
)
