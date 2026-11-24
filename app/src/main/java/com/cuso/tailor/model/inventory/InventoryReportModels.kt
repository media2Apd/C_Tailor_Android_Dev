@file:Suppress("unused")

package com.cuso.tailor.model.inventory

import com.cuso.tailor.model.report.ReportPaginationDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// =============================================================================
// 1. STOCK SUMMARY REPORT
// =============================================================================

@Serializable
data class StockSummaryReportResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: List<StockSummaryItemDtoReport> = emptyList(),
    val pagination: ReportPaginationDto? = null,
    val summary: StockSummaryDto? = null
)

@Serializable
data class StockSummaryItemDtoReport(
    @SerialName("_id")
    val id: String = "",
    val name: String = "",
    val sku: String = "",
    val barcode: String = "",
    val category: String = "",
    val unit: String = "",
    val isFabric: Boolean = false,
    val onHandQuantity: Double = 0.0,
    val reservedQuantity: Double = 0.0,
    val availableQuantity: Double = 0.0,
    val costPrice: Double = 0.0,
    val sellingPrice: Double = 0.0,
    val stockValuation: Double = 0.0,
    val statusBadge: String = "",
    val reorderLevel: Double = 0.0
)

@Serializable
data class StockSummaryDto(
    val totalSkus: Int = 0,
    val totalOnHandUnits: Double = 0.0,
    val totalReservedUnits: Double = 0.0,
    val totalStockValuation: Double = 0.0,
    val lowStockCount: Int = 0,
    val outOfStockCount: Int = 0
)

// =============================================================================
// 2. LOW STOCK & REORDER ACTION REPORT
// =============================================================================

@Serializable
data class LowStockReorderReportResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: List<LowStockReorderItemDto> = emptyList(),
    val pagination: ReportPaginationDto? = null,
    val summary: LowStockReorderSummaryDto? = null
)

@Serializable
data class LowStockReorderItemDto(
    @SerialName("_id")
    val id: String = "",
    val name: String = "",
    val sku: String = "",
    val unit: String = "",
    val availableQuantity: Double = 0.0,
    val onHandQuantity: Double = 0.0,
    val reorderLevel: Double = 0.0,
    val suggestedOrderQty: Double = 0.0,
    val preferredSupplier: String = "",
    val automationRule: String = "",
    val estimatedCost: Double = 0.0,
    val statusBadge: String = ""
)

@Serializable
data class LowStockReorderSummaryDto(
    val totalLowStockItems: Int = 0,
    val outOfStockItems: Int = 0,
    val autoRulesCoveredCount: Int = 0,
    val pendingApprovalCount: Int = 0,
    val totalShortageUnits: Double = 0.0,
    val totalEstimatedCost: Double = 0.0,
    val isAutomationEnabled: Boolean = false,
    val checkFrequency: String = "",
    val poConsolidationMode: String = ""
)

// =============================================================================
// 3. STOCK MOVEMENT REPORT
// =============================================================================

@Serializable
data class StockMovementReportResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: List<StockMovementItemDto> = emptyList(),
    val pagination: ReportPaginationDto? = null,
    val summary: StockMovementSummaryDto? = null
)

@Serializable
data class StockMovementItemDto(
    @SerialName("_id")
    val id: String = "",
    val itemName: String = "",
    val sku: String = "",
    val unit: String = "",
    val movementType: String = "",
    val quantity: Double = 0.0,
    val isPositive: Boolean = false,
    val warehouse: String = "",
    val bin: String = "",
    val reason: String = "",
    val handledBy: String = "",
    val createdAt: String? = null
)

@Serializable
data class StockMovementSummaryDto(
    val totalMovementsCount: Int = 0,
    val totalInwardQty: Double = 0.0,
    val totalOutwardQty: Double = 0.0,
    val adjustmentsCount: Int = 0
)

// =============================================================================
// 4. MATERIAL CONSUMPTION REPORT
// =============================================================================

@Serializable
data class MaterialConsumptionReportResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: List<MaterialConsumptionItemDto> = emptyList(),
    val pagination: ReportPaginationDto? = null,
    val summary: MaterialConsumptionSummaryDto? = null
)

@Serializable
data class MaterialConsumptionItemDto(
    @SerialName("_id")
    val id: String = "",
    val itemName: String = "",
    val sku: String = "",
    val unit: String = "",
    val consumedQuantity: Double = 0.0,
    val valuation: Double = 0.0,
    val materialClassification: String = "",
    val warehouse: String = "",
    val handledBy: String = "",
    val usageStream: String = "",
    val createdAt: String? = null
)

@Serializable
data class MaterialConsumptionSummaryDto(
    val totalConsumptionEvents: Int = 0,
    val totalUnitsConsumed: Double = 0.0,
    val totalConsumptionValuation: Double = 0.0,
    val fabricEventsCount: Int = 0
)

// =============================================================================
// 5. INVENTORY VALUATION REPORT
// =============================================================================

@Serializable
data class InventoryValuationReportResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: List<InventoryValuationItemDto> = emptyList(),
    val pagination: ReportPaginationDto? = null,
    val summary: InventoryValuationSummaryDto? = null
)

@Serializable
data class InventoryValuationItemDto(
    @SerialName("_id")
    val id: String = "",
    val name: String = "",
    val sku: String = "",
    val category: String = "",
    val unit: String = "",
    val currentStock: Double = 0.0,
    val costPrice: Double = 0.0,
    val sellingPrice: Double = 0.0,
    val totalCostValue: Double = 0.0,
    val totalRetailValue: Double = 0.0,
    val potentialProfit: Double = 0.0,
    val marginPercent: String = "0%"
)

@Serializable
data class InventoryValuationSummaryDto(
    val totalTrackedItems: Int = 0,
    val totalStockUnits: Double = 0.0,
    val totalAssetCostValue: Double = 0.0,
    val totalRetailValue: Double = 0.0,
    val totalPotentialProfit: Double = 0.0,
    val overallMargin: String = "0%"
)