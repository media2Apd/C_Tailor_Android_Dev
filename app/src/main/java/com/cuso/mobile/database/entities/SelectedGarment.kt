// File: app/src/main/java/com/cuso/mobile/database/entities/SelectedGarment.kt
// ⚠️ REPLACE THE ENTIRE EXISTING FILE WITH THIS — this is the missing piece
// causing all the "Unresolved reference 'measurements'" compile errors.

package com.cuso.mobile.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.UUID

class StringListConverter {
    @TypeConverter
    fun fromList(list: List<String>): String = list.joinToString(",")

    @TypeConverter
    fun toList(data: String): List<String> =
        if (data.isBlank()) emptyList() else data.split(",")
}

// Plain serializable measurement row (kept here so Room/Gson can use it
// without the entity layer depending on the UI layer's MeasurementField)
data class GarmentMeasurement(
    val id: String = "",
    val label: String = "",
    val value: String = "",
    val unit: String = "inch"
)

class MeasurementListConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromMeasurementList(list: List<GarmentMeasurement>): String = gson.toJson(list)

    @TypeConverter
    fun toMeasurementList(data: String): List<GarmentMeasurement> {
        if (data.isBlank()) return emptyList()
        val type = object : TypeToken<List<GarmentMeasurement>>() {}.type
        return try {
            gson.fromJson<List<GarmentMeasurement>>(data, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}

@Entity(tableName = "selected_garments")
@TypeConverters(StringListConverter::class, MeasurementListConverter::class)
data class SelectedGarment(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val orderSessionId: String = "draft_order",
    val category: String = "",
    val categoryId: String,
    val categoryName: String = "",
    val quantity: Int = 1,
    val price: Double = 0.0,
    val priority: String = "Low",
    val trialRequired: Boolean = false,
    val fabricSource: String = "In-House",
    val fabricType: String = "",
    val colorTone: String = "",
    val pattern: String = "Solid",
    val models: List<String> = emptyList(),
    val measurements: List<GarmentMeasurement> = emptyList()
)