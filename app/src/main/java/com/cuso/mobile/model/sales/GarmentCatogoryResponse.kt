package com.cuso.mobile.model.sales

// Garment Categories Response
data class GarmentCategoriesResponse(
    val success: Boolean,
    val data: GarmentCategoriesData
)

data class GarmentCategoriesData(
    val totalAssigned: Int,
    val active: Int,
    val inactive: Int,
    val availableSlots: Int?,
    val categories: List<CategoryItem>
)