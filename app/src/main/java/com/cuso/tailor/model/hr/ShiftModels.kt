package com.cuso.tailor.model.hr

import com.google.gson.annotations.SerializedName

data class ShiftListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<ShiftDto>?
)

data class ShiftDto(
    @SerializedName("_id") val id: String,
    @SerializedName("name") val name: String?,
    @SerializedName("shiftId") val shiftId: String?,
    @SerializedName("startTime") val startTime: String?,
    @SerializedName("endTime") val endTime: String?,
    @SerializedName("breakDuration") val breakDuration: Int? = null,
    @SerializedName("shiftType") val shiftType: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("status") val status: Boolean = true,
    @SerializedName("isDefault") val isDefault: Boolean = false,
    @SerializedName("customWorkingDays") val customWorkingDays: List<String> = emptyList()
)

// UI Display Model
data class ShiftItem(
    val id: String,
    val title: String,
    val department: String,      // shiftId or description e.g. "SHIFT-001" or "General"
    val startTime: String,
    val endTime: String,
    val breakDuration: String,
    val shiftType: String,
    val isActive: Boolean = true
)

data class CreateShiftRequest(
    @SerializedName("name") val name: String,
    @SerializedName("shiftId") val shiftId: String,
    @SerializedName("startTime") val startTime: String,         // 24-hr format "HH:mm"
    @SerializedName("endTime") val endTime: String,             // 24-hr format "HH:mm"
    @SerializedName("breakDuration") val breakDuration: Int,    // in minutes e.g. 60
    @SerializedName("shiftType") val shiftType: String,         // "Fixed" or "Rotational"
    @SerializedName("customWorkingDays") val customWorkingDays: List<String>, // e.g. ["Mon", "Wed"]
    @SerializedName("status") val status: Boolean = true
)

data class CreateShiftResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: Any?
)


// View-one response model
data class ShiftDetailResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: ShiftDetailData?
)

data class ShiftDetailData(
    @SerializedName("_id") val id: String,
    @SerializedName("name") val name: String?,
    @SerializedName("shiftId") val shiftId: String?,
    @SerializedName("startTime") val startTime: String?,
    @SerializedName("endTime") val endTime: String?,
    @SerializedName("breakDuration") val breakDuration: Int? = 0,
    @SerializedName("shiftType") val shiftType: String? = "Fixed",
    @SerializedName("status") val status: Boolean = true,
    @SerializedName("customWorkingDays") val customWorkingDays: List<String> = emptyList()
)

// Update request & response models
data class UpdateShiftRequest(
    @SerializedName("name") val name: String,
    @SerializedName("shiftId") val shiftId: String,
    @SerializedName("startTime") val startTime: String,
    @SerializedName("endTime") val endTime: String,
    @SerializedName("breakDuration") val breakDuration: Int,
    @SerializedName("shiftType") val shiftType: String,
    @SerializedName("customWorkingDays") val customWorkingDays: List<String>,
    @SerializedName("status") val status: Boolean = true
)

data class UpdateShiftResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: Any?
)

// Generic response for delete operation
data class GenericApiResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?
)