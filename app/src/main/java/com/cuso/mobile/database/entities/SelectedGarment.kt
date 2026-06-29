// File: app/src/main/java/com/cuso/mobile/database/entities/SelectedGarment.kt

package com.cuso.mobile.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.util.UUID

class StringListConverter {
    @TypeConverter
    fun fromList(list: List<String>): String = list.joinToString(",")

    @TypeConverter
    fun toList(data: String): List<String> =
        if (data.isBlank()) emptyList() else data.split(",")
}

@Entity(tableName = "selected_garments")
@TypeConverters(StringListConverter::class)
data class SelectedGarment(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val orderSessionId: String = "draft_order",
    val category: String = "",
    val categoryName: String = "",
    val quantity: Int = 1,
    val price: Double = 0.0,
    val priority: String = "Low",
    val trialRequired: Boolean = false,
    val fabricSource: String = "In-House",
    val fabricType: String = "",
    val colorTone: String = "",
    val pattern: String = "Solid",
    val models: List<String> = emptyList()
)