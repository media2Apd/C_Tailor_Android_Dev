package com.cuso.tailor.model.inventory

import com.google.gson.annotations.SerializedName

// =============================================================================
// APPROVAL LIST (VIEW-ALL) MODELS
// =============================================================================

data class ApprovalListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("pagination") val pagination: InventoryPagination?,
    @SerializedName("data") val data: List<ApprovalListItemDto> = emptyList()
)

data class ApprovalListItemDto(
    @SerializedName("_id") val id: String,
    @SerializedName("requestId") val requestId: String,
    @SerializedName("date") val date: String,
    @SerializedName("supplier") val supplier: String,
    @SerializedName("itemCount") val itemCount: Int,
    @SerializedName("isActive") val isActive: Boolean,
    @SerializedName("totalAmount") val totalAmount: Double,
    @SerializedName("requestedBy") val requestedBy: String,
    @SerializedName("status") val status: String,
    @SerializedName("lastUpdated") val lastUpdated: String
)

// =============================================================================
// APPROVAL DETAIL (VIEW-ONE) MODELS
// =============================================================================

data class ApprovalDetailResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: ApprovalDetailData? = null
)

data class ApprovalDetailData(
    @SerializedName("_id") val id: String,
    @SerializedName("organizationId") val organizationId: String?,
    @SerializedName("prNumber") val prNumber: String,
    @SerializedName("supplierId") val supplierId: String?,
    @SerializedName("warehouseId") val warehouseId: ApprovalWarehouseDto?,
    @SerializedName("department") val department: String?,
    @SerializedName("priority") val priority: String?,
    @SerializedName("requiredByDate") val requiredByDate: String?,
    @SerializedName("budgetCode") val budgetCode: String?,
    @SerializedName("justification") val justification: String?,
    @SerializedName("items") val items: List<ApprovalItemDoc> = emptyList(),
    @SerializedName("estimatedSubtotal") val estimatedSubtotal: Double,
    @SerializedName("estimatedTax") val estimatedTax: Double,
    @SerializedName("estimatedTotal") val estimatedTotal: Double,
    @SerializedName("discount") val discount: Double,
    @SerializedName("shippingCost") val shippingCost: Double,
    @SerializedName("approvalStatus") val approvalStatus: String,
    @SerializedName("linkedPOIds") val linkedPOIds: List<String> = emptyList(),
    @SerializedName("conversionStatus") val conversionStatus: String?,
    @SerializedName("internalReference") val internalReference: String?,
    @SerializedName("requestedBy") val requestedBy: ApprovalUserSummary?,
    @SerializedName("createdBy") val createdBy: String?,
    @SerializedName("isDeleted") val isDeleted: Boolean = false,
    @SerializedName("comments") val comments: List<ApprovalCommentItem> = emptyList(),
    @SerializedName("attachments") val attachments: List<String> = emptyList(),
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("updatedAt") val updatedAt: String?,
    @SerializedName("updatedBy") val updatedBy: String?
)

data class ApprovalWarehouseDto(
    @SerializedName("_id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("code") val code: String?,
    @SerializedName("address") val address: WarehouseAddressSummary?
)

data class WarehouseAddressSummary(
    @SerializedName("state") val state: String?,
    @SerializedName("country") val country: String?
)

data class ApprovalUserSummary(
    @SerializedName("_id") val id: String,
    @SerializedName("firstName") val firstName: String,
    @SerializedName("lastName") val lastName: String
) {
    val fullName: String get() = "$firstName $lastName".trim()
}

data class ApprovalItemDoc(
    @SerializedName("_id") val id: String,
    @SerializedName("itemId") val itemId: ApprovalItemProductSummary?,
    @SerializedName("qty") val qty: Double,
    @SerializedName("convertedQty") val convertedQty: Double,
    @SerializedName("unit") val unit: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("rate") val rate: Double,
    @SerializedName("taxPercent") val taxPercent: Double,
    @SerializedName("subtotal") val subtotal: Double,
    @SerializedName("taxAmount") val taxAmount: Double,
    @SerializedName("total") val total: Double,
    @SerializedName("status") val status: String
)

data class ApprovalItemProductSummary(
    @SerializedName("_id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("sku") val sku: String,
    @SerializedName("unit") val unit: String?,
    @SerializedName("marginPercent") val marginPercent: Double = 0.0,
    @SerializedName("currentStockValue") val currentStockValue: Double = 0.0
)

data class ApprovalCommentItem(
    @SerializedName("_id") val id: String,
    @SerializedName("commentedBy") val commentedBy: ApprovalCommentAuthor?,
    @SerializedName("text") val text: String,
    @SerializedName("createdAt") val createdAt: String
)

data class ApprovalCommentAuthor(
    @SerializedName("_id") val id: String,
    @SerializedName("firstName") val firstName: String,
    @SerializedName("lastName") val lastName: String,
    @SerializedName("profilePicture") val profilePicture: String? = null
) {
    val fullName: String get() = "$firstName $lastName".trim()
    val initials: String
        get() = "${firstName.firstOrNull()?.uppercase() ?: ""}${lastName.firstOrNull()?.uppercase() ?: ""}".ifBlank { "U" }
}

data class AddApprovalCommentRequest(
    @SerializedName("text") val text: String
)