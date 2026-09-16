package com.cuso.mobile.model.inventory

import com.google.gson.annotations.SerializedName

// ── Generic Requisition Response Wrappers ──

data class RequisitionListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<PurchaseRequisition>,
    @SerializedName("pagination") val pagination: Any? = null,
    @SerializedName("message") val message: String? = null
)

data class RequisitionSingleResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: PurchaseRequisition?,
    @SerializedName("message") val message: String? = null
)

// ── Purchase Requisition Core Model ──

data class PurchaseRequisition(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("organizationId") val organizationId: String? = null,
    @SerializedName("prNumber") val prNumber: String? = null,
    @SerializedName("supplierId") val supplierId: String? = null,
    @SerializedName("warehouseId") val warehouseId: Any? = null,
    @SerializedName("department") val department: String? = null,
    @SerializedName("priority") val priority: String? = "Normal",
    @SerializedName("requiredByDate") val requiredByDate: String? = null,
    @SerializedName("budgetCode") val budgetCode: String? = null,
    @SerializedName("internalReference") val internalReference: String? = null,
    @SerializedName("justification") val justification: String? = null,
    @SerializedName("items") val items: List<RequisitionItem> = emptyList(),
    @SerializedName("estimatedSubtotal") val estimatedSubtotal: Double? = 0.0,
    @SerializedName("estimatedTax") val estimatedTax: Double? = 0.0,
    @SerializedName("estimatedTotal") val estimatedTotal: Double? = 0.0,
    @SerializedName("discount") val discount: Double? = 0.0,
    @SerializedName("shippingCost") val shippingCost: Double? = 0.0,
    @SerializedName("currentApprovalLevel") val currentApprovalLevel: Int? = 0,
    @SerializedName("approvalStatus") val approvalStatus: String? = "Draft",
    @SerializedName("approvalTrail") val approvalTrail: List<RequisitionApprovalTrail> = emptyList(),
    @SerializedName("linkedPOIds") val linkedPOIds: List<String> = emptyList(),
    @SerializedName("conversionStatus") val conversionStatus: String? = "Not Converted",
    @SerializedName("requestedBy") val requestedBy: Any? = null,
    @SerializedName("createdBy") val createdBy: String? = null,
    @SerializedName("isDeleted") val isDeleted: Boolean = false,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null,
    @SerializedName("nextApprovalStage") val nextApprovalStage: String? = null,
    @SerializedName("nextApprovalRequiredDesignation") val nextApprovalRequiredDesignation: String? = null,
    @SerializedName("fullApprovalHierarchy") val fullApprovalHierarchy: List<String> = emptyList()
) {
    val warehouseDisplayName: String
        get() = when (warehouseId) {
            is Map<*, *> -> warehouseId["name"]?.toString() ?: "Warehouse"
            else -> "Warehouse"
        }
}

// ── Requisition Item Model ──

data class RequisitionItem(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("itemId") val itemId: Any? = null,
    @SerializedName("qty") val qty: Double = 0.0,
    @SerializedName("convertedQty") val convertedQty: Double = 0.0,
    @SerializedName("unit") val unit: String? = "PCS",
    @SerializedName("type") val type: String? = "Goods",
    @SerializedName("rate") val rate: Double = 0.0,
    @SerializedName("taxPercent") val taxPercent: Double = 0.0,
    @SerializedName("subtotal") val subtotal: Double = 0.0,
    @SerializedName("taxAmount") val taxAmount: Double = 0.0,
    @SerializedName("total") val total: Double = 0.0,
    @SerializedName("status") val status: String? = "Pending"
) {
    val itemDisplayName: String
        get() = when (itemId) {
            is Map<*, *> -> itemId["name"]?.toString() ?: "Item"
            else -> "Item"
        }

    val itemSku: String
        get() = when (itemId) {
            is Map<*, *> -> itemId["sku"]?.toString() ?: ""
            else -> ""
        }
}

// ── Approval Trail Model ──

data class RequisitionApprovalTrail(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("stage") val stage: String? = null,
    @SerializedName("approverId") val approverId: Any? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("remarks") val remarks: String? = null,
    @SerializedName("actionedAt") val actionedAt: String? = null
)

// ── Request Payloads ──

data class CreateRequisitionRequest(
    @SerializedName("supplierId") val supplierId: String,
    @SerializedName("warehouseId") val warehouseId: String,
    @SerializedName("department") val department: String,
    @SerializedName("priority") val priority: String,
    @SerializedName("requiredByDate") val requiredByDate: String,
    @SerializedName("justification") val justification: String,
    @SerializedName("items") val items: List<RequisitionItem>,
    @SerializedName("estimatedSubtotal") val estimatedSubtotal: Double,
    @SerializedName("estimatedTax") val estimatedTax: Double,
    @SerializedName("estimatedTotal") val estimatedTotal: Double,
    @SerializedName("budgetCode") val budgetCode: String? = null,
    @SerializedName("internalReference") val internalReference: String? = null
)

data class RequisitionApprovalActionRequest(
    @SerializedName("status") val status: String,
    @SerializedName("remarks") val remarks: String? = null
)