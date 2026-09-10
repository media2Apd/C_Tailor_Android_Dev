@file:Suppress("unused")

package com.cuso.mobile.model.inventory

import com.google.gson.annotations.SerializedName

// =============================================================================
// 1. BASE & LIST RESPONSE MODELS
// =============================================================================

data class WarehouseResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: WarehouseItem? = null
)

data class WarehouseListResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("data") val data: List<WarehouseItem> = emptyList()
)

data class WarehouseDropdownResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("data") val data: List<WarehouseDropdownItem> = emptyList()
)

data class WarehouseMessageResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String = ""
)

// =============================================================================
// 2. CORE WAREHOUSE DTO & NESTED STRUCTURES
// =============================================================================

data class WarehouseItem(
    @SerializedName("_id") val id: String = "",
    @SerializedName("organizationId") val organizationId: String = "",
    @SerializedName("branchId") val branchId: String? = null,
    @SerializedName("name") val name: String = "",
    @SerializedName("code") val code: String = "",
    @SerializedName("type") val type: String = "main", // "main" | "showroom" | "transit"
    @SerializedName("description") val description: String? = null,
    @SerializedName("contactPerson") val contactPerson: String = "",
    @SerializedName("contactPhone") val contactPhone: String = "",
    @SerializedName("isDefault") val isDefault: Boolean = false,
    @SerializedName("status") val status: String = "active", // "active" | "inactive"
    @SerializedName("address") val address: WarehouseAddress = WarehouseAddress(),
    @SerializedName("capacitySummary") val capacitySummary: CapacitySummary = CapacitySummary(),
    @SerializedName("hierarchyCounts") val hierarchyCounts: HierarchyCounts = HierarchyCounts(),
    @SerializedName("createdBy") val createdBy: String? = null,
    @SerializedName("updatedBy") val updatedBy: String? = null,
    @SerializedName("isDeleted") val isDeleted: Boolean = false,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null,
    @SerializedName("__v") val version: Int = 0
)

data class WarehouseAddress(
    @SerializedName("address") val address: String = "",
    @SerializedName("city") val city: String = "",
    @SerializedName("state") val state: String = "",
    @SerializedName("country") val country: String = "India",
    @SerializedName("pincode") val pincode: String = ""
)

data class CapacitySummary(
    @SerializedName("totalFloorAreaSqft") val totalFloorAreaSqft: Double = 0.0,
    @SerializedName("defaultTemperatureZone") val defaultTemperatureZone: String = "normal"
)

data class HierarchyCounts(
    @SerializedName("floors") val floors: Int = 0,
    @SerializedName("sections") val sections: Int = 0,
    @SerializedName("racks") val racks: Int = 0,
    @SerializedName("bins") val bins: Int = 0
)

data class WarehouseDropdownItem(
    @SerializedName("value") val value: String = "",
    @SerializedName("label") val label: String = "",
    @SerializedName("isDefault") val isDefault: Boolean = false
)

// =============================================================================
// 3. REQUEST BODY MODELS
// =============================================================================

data class CreateWarehouseRequest(
    @SerializedName("branchId") val branchId: String? = null,
    @SerializedName("name") val name: String,
    @SerializedName("code") val code: String,
    @SerializedName("type") val type: String = "main",
    @SerializedName("description") val description: String? = null,
    @SerializedName("contactPerson") val contactPerson: String,
    @SerializedName("contactPhone") val contactPhone: String,
    @SerializedName("isDefault") val isDefault: Boolean = false,
    @SerializedName("address") val address: WarehouseAddress,
    @SerializedName("capacitySummary") val capacitySummary: CapacitySummary = CapacitySummary()
)

data class UpdateWarehouseRequest(
    @SerializedName("branchId") val branchId: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("code") val code: String? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("contactPerson") val contactPerson: String? = null,
    @SerializedName("contactPhone") val contactPhone: String? = null,
    @SerializedName("isDefault") val isDefault: Boolean? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("address") val address: WarehouseAddress? = null,
    @SerializedName("capacitySummary") val capacitySummary: CapacitySummary? = null
)