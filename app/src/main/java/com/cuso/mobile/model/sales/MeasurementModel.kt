package com.cuso.mobile.model.sales

import com.google.gson.annotations.SerializedName



// ── Raw API response: GET /api/measurements (or similar) ──
data class MeasurementsResponse(
    val success: Boolean,
    @SerializedName("data")
    val customersLastOrders: List<CustomerLastOrder>
)

// ── Single customer's last order/measurement record ──
data class CustomerLastOrder(
    @SerializedName("_id")
    val id: String,
    val garments: List<String>,
    val customerName: String,
    val customerId: String,
    val type: String,           // "individual" | "corporate"
    val contact: String,
    val lastUpdated: String,    // ISO date string
    val pendingPayment: Int,
    val totalSpend: Int
)

data class MeasurementsData(
    val totalAssigned: Int,
    val active: Int,
    val inactive: Int,
    val availableSlots: Int?,
    val categories: List<OrgGarmentCategory>
)

// ── Garment Category ──
data class OrgGarmentCategory(
    @SerializedName("_id")
    val id: String,
    val organizationId: String,
    val categoryId: CategoryDetailMeasurement,
    val isActive: Boolean,
    val createdAt: String,
    val updatedAt: String
)

data class CategoryDetailMeasurement(
    @SerializedName("_id")
    val id: String,
    val categoryName: String,
    val measurements: List<MeasurementField>,
    val models: List<Model>
)

data class MeasurementField(
    val fieldName: String,
    val unit: String,
    val inputType: String,
    val inputCount: Int,
    val options: List<String>,
    val isCommonField: Boolean,
    val commonFieldId: String?,
    @SerializedName("_id")
    val id: String
)

data class Model(
    val modelName: String,
    val pieceRate: Int,
    val modelIcon: String,
    @SerializedName("_id")
    val id: String
)


data class MeasurementItem(
    val id: String,
    val customerName: String,
    val contact: String,
    val type: String,       // "Individual" | "Corporate"
    val garments: String,   // "Shirt / Pant"
    val pending: String,    // "₹0"
    val totalSpend: String, // "₹0"
    val lastUpdated: String // "01-07-2026"
)
