package com.cuso.mobile.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sales_summary")
data class SalesSummaryEntity(
    @PrimaryKey
    val id: Int = 1,
    @ColumnInfo(name = "total_assigned") val totalAssigned: Int,
    @ColumnInfo(name = "active") val active: Int,
    @ColumnInfo(name = "inactive") val inactive: Int,
    @ColumnInfo(name = "available_slots") val availableSlots: Int?
)
