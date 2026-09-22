package com.cuso.tailor.model.inventory

data class SafetyStockResponse(
    val success: Boolean,
    val pagination: SafetyStockPaginationDto? = null,
    val data: List<SafetyStockItemDto> = emptyList()
)

data class SafetyStockPaginationDto(
    val total: Int = 0,
    val page: Int = 1,
    val limit: Int = 10,
    val totalPages: Int = 1
)

data class SafetyStockItemDto(
    val itemId: String = "",
    val name: String = "",
    val sku: String = "",
    val variantLabel: String? = null,
    val unit: String? = "Meters",
    val warehouseId: String? = null,
    val warehouseName: String? = null,
    val available: Double = 0.0,
    val safetyStock: Double = 0.0,
    val reorderLevel: Double = 0.0,
    val health: String = "Safe",
    val healthPercent: Double = 100.0
)