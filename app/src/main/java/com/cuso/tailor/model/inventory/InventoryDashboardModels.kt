package com.cuso.tailor.model.inventory

import com.google.gson.annotations.SerializedName

// 1. Dashboard Summary
data class DashboardSummaryResponse(
    val success: Boolean,
    val message: String?,
    val data: DashboardSummaryData?
)

data class DashboardSummaryData(
    val totalProducts: SummaryMetricItem?,
    val inventoryValue: SummaryMetricItem?,
    val lowStockItems: SummaryMetricItem?,
    val outOfStock: SummaryMetricItem?,
    val pendingPOs: SummaryMetricItem?,
    val inventoryAccuracy: SummaryMetricItem?
)

data class SummaryMetricItem(
    val value: Double?,
    val formattedValue: Any?,
    val changePercent: Double?,
    val trend: String?,
    val sparkline: List<Double> = emptyList(),
    val displayValue: String? = null
)

// 2. Stock Health
data class StockHealthResponse(
    val success: Boolean,
    val message: String?,
    val data: StockHealthData?
)

data class StockHealthData(
    val total: Int = 0,
    val segments: List<StockHealthSegment> = emptyList()
)

data class StockHealthSegment(
    val key: String,
    val label: String,
    val count: Int,
    val percent: Double,
    val color: String
)

// 3. Stock Trends
data class StockTrendsResponse(
    val success: Boolean,
    val message: String?,
    val data: StockTrendsData?
)

data class StockTrendsData(
    val year: Int = 2026,
    val points: List<StockTrendPoint> = emptyList()
)

data class StockTrendPoint(
    val month: String,
    val incomingStock: Double = 0.0,
    val outgoingStock: Double = 0.0,
    val inventoryValue: Double = 0.0
)

// 4. Category Breakdown
data class CategoryBreakdownResponse(
    val success: Boolean,
    val message: String?,
    val data: List<CategoryBreakdownItem> = emptyList()
)

data class CategoryBreakdownItem(
    val id: String,
    val name: String,
    val count: Int,
    val percent: Double
)

// 5. Fast Moving Products
data class FastMovingResponse(
    val success: Boolean,
    val message: String?,
    val data: List<FastMovingProductItem> = emptyList()
)

data class FastMovingProductItem(
    val id: String,
    val name: String,
    val sku: String,
    val image: String?,
    val currentStock: String,
    val monthlyUsage: String,
    val trendPercent: Double?,
    val trendDirection: String?
)

// 6. Warehouse Health
data class WarehouseHealthResponse(
    val success: Boolean,
    val message: String?,
    val data: List<WarehouseHealthItem> = emptyList()
)

data class WarehouseHealthItem(
    val id: String,
    val name: String,
    val location: String,
    val percent: Double,
    val status: String,
    val color: String
)

// 7. Slow Moving / Dead Stock
data class SlowMovingResponse(
    val success: Boolean,
    val message: String?,
    val data: List<SlowMovingProductItem> = emptyList()
)

data class SlowMovingProductItem(
    val id: String,
    val name: String,
    val sku: String,
    val image: String?,
    val daysIdle: String,
    val stockValue: String,
    val recommendation: String,
    val badgeType: String
)