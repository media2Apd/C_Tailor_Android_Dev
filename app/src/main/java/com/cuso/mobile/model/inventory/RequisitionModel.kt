package com.cuso.mobile.model.inventory

import com.google.gson.annotations.SerializedName

//// ── Generic Requisition Response Wrappers ──
//
//data class RequisitionListResponse(
//    @SerializedName("success") val success: Boolean,
//    @SerializedName("data") val data: List<PurchaseRequisition>,
//    @SerializedName("pagination") val pagination: Any? = null,
//    @SerializedName("message") val message: String? = null
//)
//
//data class RequisitionSingleResponse(
//    @SerializedName("success") val success: Boolean,
//    @SerializedName("data") val data: PurchaseRequisition?,
//    @SerializedName("message") val message: String? = null
//)
//
//// ── Purchase Requisition Core Model ──
//
//data class PurchaseRequisition(
//    @SerializedName("_id") val id: String? = null,
//    @SerializedName("organizationId") val organizationId: String? = null,
//    @SerializedName("prNumber") val prNumber: String? = null,
//    @SerializedName("supplierId") val supplierId: String? = null,
//    @SerializedName("warehouseId") val warehouseId: Any? = null,
//    @SerializedName("department") val department: String? = null,
//    @SerializedName("priority") val priority: String? = "Normal",
//    @SerializedName("requiredByDate") val requiredByDate: String? = null,
//    @SerializedName("budgetCode") val budgetCode: String? = null,
//    @SerializedName("internalReference") val internalReference: String? = null,
//    @SerializedName("justification") val justification: String? = null,
//    @SerializedName("items") val items: List<RequisitionItem> = emptyList(),
//    @SerializedName("estimatedSubtotal") val estimatedSubtotal: Double? = 0.0,
//    @SerializedName("estimatedTax") val estimatedTax: Double? = 0.0,
//    @SerializedName("estimatedTotal") val estimatedTotal: Double? = 0.0,
//    @SerializedName("discount") val discount: Double? = 0.0,
//    @SerializedName("shippingCost") val shippingCost: Double? = 0.0,
//    @SerializedName("currentApprovalLevel") val currentApprovalLevel: Int? = 0,
//    @SerializedName("approvalStatus") val approvalStatus: String? = "Draft",
//    @SerializedName("approvalTrail") val approvalTrail: List<RequisitionApprovalTrail> = emptyList(),
//    @SerializedName("linkedPOIds") val linkedPOIds: List<String> = emptyList(),
//    @SerializedName("conversionStatus") val conversionStatus: String? = "Not Converted",
//    @SerializedName("requestedBy") val requestedBy: Any? = null,
//    @SerializedName("createdBy") val createdBy: String? = null,
//    @SerializedName("isDeleted") val isDeleted: Boolean = false,
//    @SerializedName("createdAt") val createdAt: String? = null,
//    @SerializedName("updatedAt") val updatedAt: String? = null,
//    @SerializedName("nextApprovalStage") val nextApprovalStage: String? = null,
//    @SerializedName("nextApprovalRequiredDesignation") val nextApprovalRequiredDesignation: String? = null,
//    @SerializedName("fullApprovalHierarchy") val fullApprovalHierarchy: List<String> = emptyList()
//) {
//    val warehouseDisplayName: String
//        get() = when (warehouseId) {
//            is Map<*, *> -> warehouseId["name"]?.toString() ?: "Warehouse"
//            else -> "Warehouse"
//        }
//}
//
//// ── Requisition Item Model ──
//
//data class RequisitionItem(
//    @SerializedName("_id") val id: String? = null,
//    @SerializedName("itemId") val itemId: Any? = null,
//    @SerializedName("qty") val qty: Double = 0.0,
//    @SerializedName("convertedQty") val convertedQty: Double = 0.0,
//    @SerializedName("unit") val unit: String? = "PCS",
//    @SerializedName("type") val type: String? = "Goods",
//    @SerializedName("rate") val rate: Double = 0.0,
//    @SerializedName("taxPercent") val taxPercent: Double = 0.0,
//    @SerializedName("subtotal") val subtotal: Double = 0.0,
//    @SerializedName("taxAmount") val taxAmount: Double = 0.0,
//    @SerializedName("total") val total: Double = 0.0,
//    @SerializedName("status") val status: String? = "Pending"
//) {
//    val itemDisplayName: String
//        get() = when (itemId) {
//            is Map<*, *> -> itemId["name"]?.toString() ?: "Item"
//            else -> "Item"
//        }
//
//    val itemSku: String
//        get() = when (itemId) {
//            is Map<*, *> -> itemId["sku"]?.toString() ?: ""
//            else -> ""
//        }
//}
//
//// ── Approval Trail Model ──
//
//data class RequisitionApprovalTrail(
//    @SerializedName("_id") val id: String? = null,
//    @SerializedName("stage") val stage: String? = null,
//    @SerializedName("approverId") val approverId: Any? = null,
//    @SerializedName("status") val status: String? = null,
//    @SerializedName("remarks") val remarks: String? = null,
//    @SerializedName("actionedAt") val actionedAt: String? = null
//)
//
//// ── Request Payloads ──
//
//data class CreateRequisitionRequest(
//    @SerializedName("supplierId") val supplierId: String,
//    @SerializedName("warehouseId") val warehouseId: String,
//    @SerializedName("department") val department: String,
//    @SerializedName("priority") val priority: String,
//    @SerializedName("requiredByDate") val requiredByDate: String,
//    @SerializedName("justification") val justification: String,
//    @SerializedName("items") val items: List<RequisitionItem>,
//    @SerializedName("estimatedSubtotal") val estimatedSubtotal: Double,
//    @SerializedName("estimatedTax") val estimatedTax: Double,
//    @SerializedName("estimatedTotal") val estimatedTotal: Double,
//    @SerializedName("budgetCode") val budgetCode: String? = null,
//    @SerializedName("internalReference") val internalReference: String? = null
//)
//
//data class RequisitionApprovalActionRequest(
//    @SerializedName("status") val status: String,
//    @SerializedName("remarks") val remarks: String? = null
//)
//
////Add Comments
//// 1. Request Body Model
//data class AddRequisitionCommentRequest(
//    val message: String
//)
//
//// 2. Comment Models (RequisitionResponse-ல் சேர்க்க)
//data class RequisitionComment(
//    val _id: String? = null,
//    val text: String? = null,
//    val createdAt: String? = null,
//    val commentedBy: RequisitionCommentAuthor? = null
//)

data class RequisitionCommentAuthor(
    val _id: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val profilePicture: String? = null
) {
    val fullName: String
        get() = listOfNotNull(firstName, lastName).joinToString(" ").ifBlank { "User" }

    val initials: String
        get() {
            val f = firstName?.firstOrNull()?.uppercaseChar()?.toString() ?: ""
            val l = lastName?.firstOrNull()?.uppercaseChar()?.toString() ?: ""
            return (f + l).ifBlank { "U" }
        }
}
//
//data class RequisitionListResponse(
//    @SerializedName("success") val success: Boolean,
//    @SerializedName("data") val data: List<PurchaseRequisition>,
//    @SerializedName("pagination") val pagination: Any? = null,
//    @SerializedName("message") val message: String? = null
//)
//
//data class RequisitionSingleResponse(
//    @SerializedName("success") val success: Boolean,
//    @SerializedName("data") val data: PurchaseRequisition?,
//    @SerializedName("message") val message: String? = null
//)
//
//data class PurchaseRequisition(
//    @SerializedName("_id") val id: String? = null,
//    @SerializedName("organizationId") val organizationId: String? = null,
//    @SerializedName("prNumber") val prNumber: String? = null,
//    @SerializedName("supplierId") val supplierId: String? = null,
//    @SerializedName("warehouseId") val warehouseId: Any? = null,
//    @SerializedName("department") val department: String? = null,
//    @SerializedName("priority") val priority: String? = "Normal",
//    @SerializedName("requiredByDate") val requiredByDate: String? = null,
//    @SerializedName("budgetCode") val budgetCode: String? = null,
//    @SerializedName("internalReference") val internalReference: String? = null,
//    @SerializedName("justification") val justification: String? = null,
//    @SerializedName("items") val items: List<RequisitionItem> = emptyList(),
//    @SerializedName("comments") val comments: List<RequisitionComment> = emptyList(),
//    @SerializedName("estimatedSubtotal") val estimatedSubtotal: Double? = 0.0,
//    @SerializedName("estimatedTax") val estimatedTax: Double? = 0.0,
//    @SerializedName("estimatedTotal") val estimatedTotal: Double? = 0.0,
//    @SerializedName("discount") val discount: Double? = 0.0,
//    @SerializedName("shippingCost") val shippingCost: Double? = 0.0,
//    @SerializedName("currentApprovalLevel") val currentApprovalLevel: Int? = 0,
//    @SerializedName("approvalStatus") val approvalStatus: String? = "Draft",
//    @SerializedName("approvalTrail") val approvalTrail: List<RequisitionApprovalTrail> = emptyList(),
//    @SerializedName("linkedPOIds") val linkedPOIds: List<String> = emptyList(),
//    @SerializedName("conversionStatus") val conversionStatus: String? = "Not Converted",
//    @SerializedName("requestedBy") val requestedBy: Any? = null,
//    @SerializedName("createdBy") val createdBy: String? = null,
//    @SerializedName("isDeleted") val isDeleted: Boolean = false,
//    @SerializedName("createdAt") val createdAt: String? = null,
//    @SerializedName("updatedAt") val updatedAt: String? = null,
//    @SerializedName("nextApprovalStage") val nextApprovalStage: String? = null,
//    @SerializedName("nextApprovalRequiredDesignation") val nextApprovalRequiredDesignation: String? = null,
//    @SerializedName("fullApprovalHierarchy") val fullApprovalHierarchy: List<String> = emptyList()
//) {
//    val warehouseDisplayName: String
//        get() = when (warehouseId) {
//            is Map<*, *> -> warehouseId["name"]?.toString() ?: "Warehouse"
//            else -> "Warehouse"
//        }
//}
//
//data class RequisitionItem(
//    @SerializedName("_id") val id: String? = null,
//    @SerializedName("itemId") val itemId: Any? = null,
//    @SerializedName("qty") val qty: Double = 0.0,
//    @SerializedName("convertedQty") val convertedQty: Double = 0.0,
//    @SerializedName("unit") val unit: String? = "PCS",
//    @SerializedName("type") val type: String? = "Goods",
//    @SerializedName("rate") val rate: Double = 0.0,
//    @SerializedName("taxPercent") val taxPercent: Double = 0.0,
//    @SerializedName("subtotal") val subtotal: Double = 0.0,
//    @SerializedName("taxAmount") val taxAmount: Double = 0.0,
//    @SerializedName("total") val total: Double = 0.0,
//    @SerializedName("status") val status: String? = "Pending"
//) {
//    val itemDisplayName: String
//        get() = when (itemId) {
//            is Map<*, *> -> itemId["name"]?.toString() ?: "Item"
//            else -> "Item"
//        }
//
//    val itemSku: String
//        get() = when (itemId) {
//            is Map<*, *> -> itemId["sku"]?.toString() ?: ""
//            else -> ""
//        }
//}
//
//data class RequisitionApprovalTrail(
//    @SerializedName("_id") val id: String? = null,
//    @SerializedName("stage") val stage: String? = null,
//    @SerializedName("approverId") val approverId: Any? = null,
//    @SerializedName("status") val status: String? = null,
//    @SerializedName("remarks") val remarks: String? = null,
//    @SerializedName("actionedAt") val actionedAt: String? = null
//)
//
//data class CreateRequisitionRequest(
//    @SerializedName("supplierId") val supplierId: String,
//    @SerializedName("warehouseId") val warehouseId: String,
//    @SerializedName("department") val department: String,
//    @SerializedName("priority") val priority: String,
//    @SerializedName("requiredByDate") val requiredByDate: String,
//    @SerializedName("justification") val justification: String,
//    @SerializedName("items") val items: List<RequisitionItem>,
//    @SerializedName("estimatedSubtotal") val estimatedSubtotal: Double,
//    @SerializedName("estimatedTax") val estimatedTax: Double,
//    @SerializedName("estimatedTotal") val estimatedTotal: Double,
//    @SerializedName("budgetCode") val budgetCode: String? = null,
//    @SerializedName("internalReference") val internalReference: String? = null
//)
//
//data class RequisitionApprovalActionRequest(
//    @SerializedName("status") val status: String,
//    @SerializedName("remarks") val remarks: String? = null
//)
//
//data class AddRequisitionCommentRequest(
//    @SerializedName("comment") val comment: String? = null,
//    @SerializedName("message") val message: String? = null
//)

//data class RequisitionComment(
//    @SerializedName("_id") val id: String? = null,
//    @SerializedName("comment") val comment: String? = null,
//    @SerializedName("message") val message: String? = null,
//    @SerializedName("createdAt") val createdAt: String? = null,
//    @SerializedName("commentedBy") val commentedBy: Any? = null
//) {
//    val content: String
//        get() = comment ?: message ?: ""
//
//    val authorName: String
//        get() = when (commentedBy) {
//            is Map<*, *> -> {
//                val first = commentedBy["firstName"]?.toString() ?: ""
//                val last = commentedBy["lastName"]?.toString() ?: ""
//                val name = commentedBy["name"]?.toString() ?: ""
//                listOf(first, last).filter { it.isNotBlank() }.joinToString(" ")
//                    .ifBlank { name.ifBlank { "User" } }
//            }
//            is String -> commentedBy
//            else -> "User"
//        }
//
//    val authorInitial: String
//        get() = authorName.firstOrNull()?.uppercaseChar()?.toString() ?: "U"
//}

//
//data class AddRequisitionCommentRequest(
//    @SerializedName("comment") val comment: String? = null,
//    @SerializedName("message") val message: String? = null,
//    @SerializedName("text") val text: String? = null
//)
//
//data class RequisitionComment(
//    @SerializedName("_id") val id: String? = null,
//    @SerializedName("text") val text: String? = null,
//    @SerializedName("comment") val comment: String? = null,
//    @SerializedName("message") val message: String? = null,
//    @SerializedName("createdAt") val createdAt: String? = null,
//    @SerializedName("commentedBy") val commentedBy: RequisitionCommentAuthor? = null
//) {
//    val content: String
//        get() = text ?: comment ?: message ?: ""
//
//    val authorName: String
//        get() = commentedBy?.fullName ?: "User"
//
//    val profilePictureUrl: String?
//        get() = commentedBy?.profilePicture
//
//    val authorInitial: String
//        get() = commentedBy?.initials ?: "U"
//}
//
//data class RequisitionCommentAuthor(
//    @SerializedName("_id") val id: String? = null,
//    @SerializedName("firstName") val firstName: String? = null,
//    @SerializedName("lastName") val lastName: String? = null,
//    @SerializedName("profilePicture") val profilePicture: String? = null
//) {
//    val fullName: String
//        get() = listOfNotNull(firstName, lastName)
//            .filter { it.isNotBlank() }
//            .joinToString(" ")
//            .ifBlank { "User" }
//
//    val initials: String
//        get() {
//            val f = firstName?.firstOrNull()?.uppercaseChar()?.toString() ?: ""
//            val l = lastName?.firstOrNull()?.uppercaseChar()?.toString() ?: ""
//            return (f + l).ifBlank { "U" }
//        }
//}

















data class RequisitionListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<PurchaseRequisition> = emptyList(),
    @SerializedName("pagination") val pagination: Any? = null,
    @SerializedName("message") val message: String? = null
)

data class RequisitionSingleResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: PurchaseRequisition?,
    @SerializedName("message") val message: String? = null
)

data class CommentActionResponse(
    @SerializedName("success") val success: Boolean? = true,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: Any? = null
)

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
    @SerializedName("comments") val comments: List<RequisitionComment> = emptyList(),
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

data class RequisitionApprovalTrail(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("stage") val stage: String? = null,
    @SerializedName("approverId") val approverId: Any? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("remarks") val remarks: String? = null,
    @SerializedName("actionedAt") val actionedAt: String? = null
)

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

data class AddRequisitionCommentRequest(
    @SerializedName("text") val text: String? = null,
    @SerializedName("comment") val comment: String? = null,
    @SerializedName("message") val message: String? = null
)

data class RequisitionComment(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("text") val text: String? = null,
    @SerializedName("comment") val comment: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("commentedBy") val commentedBy: Any? = null
) {
    val content: String
        get() = text ?: comment ?: message ?: ""

    val authorName: String
        get() = when (commentedBy) {
            is Map<*, *> -> {
                val first = commentedBy["firstName"]?.toString() ?: ""
                val last = commentedBy["lastName"]?.toString() ?: ""
                val name = commentedBy["name"]?.toString() ?: ""
                listOf(first, last).filter { it.isNotBlank() }.joinToString(" ")
                    .ifBlank { name.ifBlank { "User" } }
            }
            is String -> "User"
            else -> "User"
        }

    val profilePictureUrl: String?
        get() = when (commentedBy) {
            is Map<*, *> -> commentedBy["profilePicture"]?.toString()
            else -> null
        }

    val authorInitial: String
        get() = authorName.firstOrNull()?.uppercaseChar()?.toString() ?: "U"
}