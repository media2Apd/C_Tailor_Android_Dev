@file:Suppress("unused", "AssignedValueIsNeverRead")

package com.cuso.tailor.model.service

import com.google.gson.annotations.SerializedName

// ── Generic Response Wrappers ──

//data class ServiceRequestListResponse(
//    @SerializedName("success") val success: Boolean,
//    @SerializedName("data") val data: List<ServiceRequestData> = emptyList(),
//    @SerializedName("pagination") val pagination: ServicePaginationData? = null,
//    @SerializedName("message") val message: String? = null
//)

data class ServiceRequestDetailResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: ServiceRequestData? = null
)

data class ServicePaginationData(
    @SerializedName("total") val total: Int = 0,
    @SerializedName("page") val page: Int = 1,
    @SerializedName("limit") val limit: Int = 10,
    @SerializedName("totalPages") val totalPages: Int = 1,
    @SerializedName("hasNextPage") val hasNextPage: Boolean = false,
    @SerializedName("hasPreviousPage") val hasPreviousPage: Boolean = false
)

// ── Service Request Entity ──

data class ServiceRequestData(
    @SerializedName("_id") val id: String,
    @SerializedName("organizationId") val organizationId: String? = null,
    @SerializedName("branchId") val branchId: String? = null,
    @SerializedName("serviceRequestCode") val serviceRequestCode: String,
    @SerializedName("primaryCategory") val primaryCategory: String = "Alteration",
    @SerializedName("customerId") val customerId: ServiceCustomerRef? = null,
    @SerializedName("originalSalesOrderId") val originalSalesOrderId: String? = null,
    @SerializedName("items") val items: List<ServiceRequestItem> = emptyList(),
    @SerializedName("attachments") val attachments: List<String> = emptyList(),
    @SerializedName("priceIncludesTax") val priceIncludesTax: Boolean = false,
    @SerializedName("taxType") val taxType: String = "Standard",
    @SerializedName("placeOfSupply") val placeOfSupply: String? = null,
    @SerializedName("subtotal") val subtotal: Double = 0.0,
    @SerializedName("totalTax") val totalTax: Double = 0.0,
    @SerializedName("grandTotal") val grandTotal: Double = 0.0,
    @SerializedName("advanceAmountPaid") val advanceAmountPaid: Double = 0.0,
    @SerializedName("balanceAmount") val balanceAmount: Double = 0.0,
    @SerializedName("paymentStatus") val paymentStatus: String = "Unpaid",
    @SerializedName("invoiceId") val invoiceId: String? = null,
    @SerializedName("paymentMode") val paymentMode: String? = null,
    @SerializedName("approvalStatus") val approvalStatus: String = "Not_Required",
    @SerializedName("status") val status: String = "Draft",
    @SerializedName("priority") val priority: String = "Medium",
    @SerializedName("deliveryDate") val deliveryDate: String? = null,
    @SerializedName("createdBy") val createdBy: ServiceUserRef? = null,
    @SerializedName("updatedBy") val updatedBy: ServiceUserRef? = null,
    @SerializedName("receivedDate") val receivedDate: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
)

data class ServiceCustomerRef(
    @SerializedName("_id") val id: String,
    @SerializedName("customerCode") val customerCode: String? = null,
    @SerializedName("fullName") val fullName: String,
    @SerializedName("mobileNumber") val mobileNumber: String,
    @SerializedName("email") val email: String? = null
)

data class ServiceUserRef(
    @SerializedName("_id") val id: String,
    @SerializedName("firstName") val firstName: String? = null,
    @SerializedName("lastName") val lastName: String? = null
)

data class ServiceRequestItem(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("originalOrderItemId") val originalOrderItemId: String? = null,
    @SerializedName("category") val category: String = "Alteration",
    @SerializedName("garmentDescription") val garmentDescription: String = "",
    @SerializedName("segmentName") val segmentName: String? = null,
    @SerializedName("garmentName") val garmentName: String? = null,
    @SerializedName("garmentCategoryName") val garmentCategoryName: String? = null,
    @SerializedName("quantity") val quantity: Int = 1,
    @SerializedName("alterations") val alterations: List<AlterationDetail> = emptyList(),
    @SerializedName("issueSummary") val issueSummary: String = "",
    @SerializedName("internalNotes") val internalNotes: String? = null,
    @SerializedName("measurementId") val measurementId: String? = null,
    @SerializedName("isChargeable") val isChargeable: Boolean = true,
    @SerializedName("serviceCharge") val serviceCharge: Double = 0.0,
    @SerializedName("sacCode") val sacCode: String? = "998812",
    @SerializedName("taxGroupId") val taxGroupId: ServiceTaxGroupRef? = null,
    @SerializedName("taxableAmount") val taxableAmount: Double = 0.0,
    @SerializedName("taxBreakdown") val taxBreakdown: List<ServiceTaxBreakdown> = emptyList(),
    @SerializedName("taxAmount") val taxAmount: Double = 0.0,
    @SerializedName("lineTotal") val lineTotal: Double = 0.0,
    @SerializedName("incomeAccountId") val incomeAccountId: ServiceIncomeAccountRef? = null,
    @SerializedName("productionTemplateId") val productionTemplateId: String? = null,
    @SerializedName("jobCardId") val jobCardId: ServiceJobCardRef? = null,
    @SerializedName("itemDeliveryDate") val itemDeliveryDate: String? = null,
    @SerializedName("itemStatus") val itemStatus: String = "Received_In_Workshop"
)

data class AlterationDetail(
    @SerializedName("targetArea") val targetArea: String,
    @SerializedName("action") val action: String,
    @SerializedName("value") val value: Double = 0.0,
    @SerializedName("unit") val unit: String = "inch",
    @SerializedName("notes") val notes: String = ""
)
//
//data class ServiceTaxGroupRef(
//    @SerializedName("_id") val id: String,
//    @SerializedName("name") val name: String,
//    @SerializedName("totalRate") val totalRate: Double,
//    @SerializedName("isCompound") val isCompound: Boolean = false
//)

data class ServiceTaxBreakdown(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("taxRateId") val taxRateId: String? = null,
    @SerializedName("name") val name: String,
    @SerializedName("rate") val rate: Double,
    @SerializedName("amount") val amount: Double
)

data class ServiceIncomeAccountRef(
    @SerializedName("_id") val id: String,
    @SerializedName("accountName") val accountName: String,
    @SerializedName("accountCode") val accountCode: String
)

//data class ServiceJobCardRef(
//    @SerializedName("_id") val id: String,
//    @SerializedName("jobCardCode") val jobCardCode: String,
//    @SerializedName("totalQuantity") val totalQuantity: Int = 1,
//    @SerializedName("status") val status: String,
//    @SerializedName("currentStageName") val currentStageName: String? = null
//)

// ── Request Payloads ──
//
//data class CreateServiceRequestPayload(
//    @SerializedName("customerId") val customerId: String,
//    @SerializedName("primaryCategory") val primaryCategory: String = "Alteration",
//    @SerializedName("originalSalesOrderId") val originalSalesOrderId: String? = null,
//    @SerializedName("priority") val priority: String = "Medium",
//    @SerializedName("deliveryDate") val deliveryDate: String? = null,
//    @SerializedName("items") val items: List<CreateServiceRequestItemPayload>,
//    @SerializedName("advanceAmountPaid") val advanceAmountPaid: Double = 0.0,
//    @SerializedName("paymentMode") val paymentMode: String = "UPI",
//    @SerializedName("attachments") val attachments: List<String> = emptyList()
//)
//
//data class CreateServiceRequestItemPayload(
//    @SerializedName("category") val category: String = "Alteration",
//    @SerializedName("garmentDescription") val garmentDescription: String,
//    @SerializedName("quantity") val quantity: Int = 1,
//    @SerializedName("serviceCharge") val serviceCharge: Double = 0.0,
//    @SerializedName("issueSummary") val issueSummary: String = "",
//    @SerializedName("internalNotes") val internalNotes: String = "",
//    @SerializedName("alterations") val alterations: List<AlterationDetail> = emptyList()
//)
//
//data class UpdateServiceStatusPayload(
//    @SerializedName("status") val status: String
//)


































//
//data class ServiceRequestListResponse(
//    @SerializedName("success") val success: Boolean,
//    @SerializedName("message") val message: String?,
//    @SerializedName("data") val data: List<ServiceRequestListItem>?,
//    @SerializedName("pagination") val pagination: ServiceRequestPagination?
//)
//
//data class ServiceRequestListItem(
//    @SerializedName("_id") val id: String,
//    @SerializedName("organizationId") val organizationId: String?,
//    @SerializedName("branchId") val branchId: String?,
//    @SerializedName("garmentSource") val garmentSource: String?,
//    @SerializedName("serviceRequestCode") val serviceRequestCode: String?,
//    @SerializedName("primaryCategory") val primaryCategory: String?,
//    @SerializedName("customerId") val customerId: ServiceCustomerDetails?,
//    @SerializedName("originalSalesOrderId") val originalSalesOrderId: String?,
//    @SerializedName("customerSnapshot") val customerSnapshot: ServiceCustomerSnapshotFull?,
//    @SerializedName("items") val items: List<ServiceGarmentItem>?,
//    @SerializedName("attachments") val attachments: List<ServiceAttachment>?,
//    @SerializedName("priceIncludesTax") val priceIncludesTax: Boolean?,
//    @SerializedName("taxType") val taxType: String?,
//    @SerializedName("placeOfSupply") val placeOfSupply: String?,
//    @SerializedName("subtotal") val subtotal: Double?,
//    @SerializedName("totalTax") val totalTax: Double?,
//    @SerializedName("grandTotal") val grandTotal: Double?,
//    @SerializedName("advanceAmountPaid") val advanceAmountPaid: Double?,
//    @SerializedName("balanceAmount") val balanceAmount: Double?,
//    @SerializedName("paymentStatus") val paymentStatus: String?,
//    @SerializedName("invoiceId") val invoiceId: String?,
//    @SerializedName("paymentMode") val paymentMode: String?,
//    @SerializedName("refundStatus") val refundStatus: String?,
//    @SerializedName("totalRefundAmount") val totalRefundAmount: Double?,
//    @SerializedName("refundPaymentMode") val refundPaymentMode: String?,
//    @SerializedName("approvalStatus") val approvalStatus: String?,
//    @SerializedName("rejectionReason") val rejectionReason: String?,
//    @SerializedName("internalNotes") val internalNotes: String?,
//    @SerializedName("status") val status: String?,
//    @SerializedName("priority") val priority: String?,
//    @SerializedName("preferredDate") val preferredDate: String?,
//    @SerializedName("deliveryDate") val deliveryDate: String?,
//    @SerializedName("receivedDate") val receivedDate: String?,
//    @SerializedName("createdBy") val createdBy: ServiceStaffUser?,
//    @SerializedName("updatedBy") val updatedBy: ServiceStaffUser?,
//    @SerializedName("createdAt") val createdAt: String?,
//    @SerializedName("updatedAt") val updatedAt: String?
//)
//
//data class ServiceCustomerDetails(
//    @SerializedName("_id") val id: String?,
//    @SerializedName("customerCode") val customerCode: String?,
//    @SerializedName("fullName") val fullName: String?,
//    @SerializedName("mobileNumber") val mobileNumber: String?,
//    @SerializedName("email") val email: String?,
//    @SerializedName("customerType") val customerType: String?,
//    @SerializedName("billingAddress") val billingAddress: ServiceAddress?
//)
//
//data class ServiceCustomerSnapshotFull(
//    @SerializedName("customerCode") val customerCode: String?,
//    @SerializedName("name") val name: String?,
//    @SerializedName("email") val email: String?,
//    @SerializedName("phone") val phone: String?,
//    @SerializedName("billingAddress") val billingAddress: ServiceAddress?
//)
//
//data class ServiceAddress(
//    @SerializedName("flatNo") val flatNo: String?,
//    @SerializedName("street") val street: String?,
//    @SerializedName("areaZone") val areaZone: String?,
//    @SerializedName("city") val city: String?,
//    @SerializedName("subdivisionCode") val subdivisionCode: String?,
//    @SerializedName("subdivisionName") val subdivisionName: String?,
//    @SerializedName("countryCode") val countryCode: String?,
//    @SerializedName("countryName") val countryName: String?,
//    @SerializedName("pincode") val pincode: String?
//)
//
//data class ServiceGarmentItem(
//    @SerializedName("_id") val id: String,
//    @SerializedName("originalOrderItemId") val originalOrderItemId: String?,
//    @SerializedName("category") val category: String?,
//    @SerializedName("garmentDescription") val garmentDescription: String?,
//    @SerializedName("segmentName") val segmentName: String?,
//    @SerializedName("garmentName") val garmentName: String?,
//    @SerializedName("garmentCategoryName") val garmentCategoryName: String?,
//    @SerializedName("quantity") val quantity: Int?,
//    @SerializedName("specialRequirements") val specialRequirements: List<String>?,
//    @SerializedName("repairs") val repairs: List<ServiceRepairAction>?,
//    @SerializedName("alterations") val alterations: List<ServiceAlterationAction>?,
//    @SerializedName("issueSummary") val issueSummary: String?,
//    @SerializedName("internalNotes") val internalNotes: String?,
//    @SerializedName("measurementId") val measurementId: String?,
//    @SerializedName("isChargeable") val isChargeable: Boolean?,
//    @SerializedName("serviceCharge") val serviceCharge: Double?,
//    @SerializedName("sacCode") val sacCode: String?,
//    @SerializedName("taxableAmount") val taxableAmount: Double?,
//    @SerializedName("taxAmount") val taxAmount: Double?,
//    @SerializedName("lineTotal") val lineTotal: Double?,
//    @SerializedName("incomeAccountId") val incomeAccountId: ServiceAccountRef?,
//    @SerializedName("productionTemplateId") val productionTemplateId: ServiceTemplateRef?,
//    @SerializedName("jobCardId") val jobCardId: ServiceJobCardRef?,
//    @SerializedName("productionState") val productionState: String?,
//    @SerializedName("currentProductionStageName") val currentProductionStageName: String?,
//    @SerializedName("itemDeliveryDate") val itemDeliveryDate: String?,
//    @SerializedName("itemStatus") val itemStatus: String?
//)
//
//data class ServiceRepairAction(
//    @SerializedName("repairArea") val repairArea: String?,
//    @SerializedName("action") val action: String?,
//    @SerializedName("notes") val notes: String?
//)
//
//data class ServiceAlterationAction(
//    @SerializedName("targetArea") val targetArea: String?,
//    @SerializedName("action") val action: String?,
//    @SerializedName("currentValue") val currentValue: Double?,
//    @SerializedName("targetValue") val targetValue: Double?,
//    @SerializedName("value") val value: Double?,
//    @SerializedName("unit") val unit: String?,
//    @SerializedName("notes") val notes: String?
//)
//
//data class ServiceAccountRef(
//    @SerializedName("_id") val id: String?,
//    @SerializedName("accountName") val accountName: String?,
//    @SerializedName("accountCode") val accountCode: String?
//)
//
//data class ServiceTemplateRef(
//    @SerializedName("_id") val id: String?,
//    @SerializedName("name") val name: String?,
//    @SerializedName("code") val code: String?
//)
//
//data class ServiceJobCardRef(
//    @SerializedName("_id") val id: String?,
//    @SerializedName("jobCardCode") val jobCardCode: String?,
//    @SerializedName("totalQuantity") val totalQuantity: Int?,
//    @SerializedName("status") val status: String?,
//    @SerializedName("currentStageName") val currentStageName: String?
//)
//
//data class ServiceAttachment(
//    @SerializedName("url") val url: String?,
//    @SerializedName("publicId") val publicId: String?,
//    @SerializedName("originalName") val originalName: String?,
//    @SerializedName("uploadedAt") val uploadedAt: String?
//)
//
//data class ServiceStaffUser(
//    @SerializedName("_id") val id: String?,
//    @SerializedName("firstName") val firstName: String?,
//    @SerializedName("lastName") val lastName: String?
//)
//
//data class ServiceRequestPagination(
//    @SerializedName("total") val total: Int,
//    @SerializedName("page") val page: Int,
//    @SerializedName("limit") val limit: Int,
//    @SerializedName("totalPages") val totalPages: Int,
//    @SerializedName("hasNextPage") val hasNextPage: Boolean,
//    @SerializedName("hasPreviousPage") val hasPreviousPage: Boolean
//)









//
//
//
//// ── Root API Responses ──
//
//data class ServiceRequestListResponse(
//    @SerializedName("success") val success: Boolean,
//    @SerializedName("message") val message: String? = null,
//    @SerializedName("data") val data: List<ServiceRequestListItem>? = emptyList(),
//    @SerializedName("pagination") val pagination: ServiceRequestPagination? = null
//)
//
//data class SingleServiceRequestResponse(
//    @SerializedName("success") val success: Boolean,
//    @SerializedName("message") val message: String? = null,
//    @SerializedName("data") val data: ServiceRequestListItem? = null
//)
//
//data class ServiceRequestPagination(
//    @SerializedName("total") val total: Int = 0,
//    @SerializedName("page") val page: Int = 1,
//    @SerializedName("limit") val limit: Int = 10,
//    @SerializedName("totalPages") val totalPages: Int = 1,
//    @SerializedName("hasNextPage") val hasNextPage: Boolean = false,
//    @SerializedName("hasPreviousPage") val hasPreviousPage: Boolean = false
//)
//
//// ── Core Service Request Entity ──
//
//data class ServiceRequestListItem(
//    @SerializedName("_id") val id: String,
//    @SerializedName("organizationId") val organizationId: String? = null,
//    @SerializedName("branchId") val branchId: String? = null,
//    @SerializedName("garmentSource") val garmentSource: String? = null,
//    @SerializedName("serviceRequestCode") val serviceRequestCode: String? = null,
//    @SerializedName("primaryCategory") val primaryCategory: String? = null,
//    @SerializedName("customerId") val customerId: ServiceCustomerDetails? = null,
//    @SerializedName("originalSalesOrderId") val originalSalesOrderId: String? = null,
//    @SerializedName("customerSnapshot") val customerSnapshot: ServiceCustomerSnapshotFull? = null,
//    @SerializedName("items") val items: List<ServiceGarmentItem>? = emptyList(),
//    @SerializedName("attachments") val attachments: List<ServiceAttachment>? = emptyList(),
//    @SerializedName("priceIncludesTax") val priceIncludesTax: Boolean? = false,
//    @SerializedName("taxType") val taxType: String? = null,
//    @SerializedName("placeOfSupply") val placeOfSupply: String? = null,
//    @SerializedName("subtotal") val subtotal: Double? = 0.0,
//    @SerializedName("totalTax") val totalTax: Double? = 0.0,
//    @SerializedName("grandTotal") val grandTotal: Double? = 0.0,
//    @SerializedName("advanceAmountPaid") val advanceAmountPaid: Double? = 0.0,
//    @SerializedName("balanceAmount") val balanceAmount: Double? = 0.0,
//    @SerializedName("paymentStatus") val paymentStatus: String? = null,
//    @SerializedName("invoiceId") val invoiceId: String? = null,
//    @SerializedName("paymentMode") val paymentMode: String? = null,
//    @SerializedName("refundStatus") val refundStatus: String? = null,
//    @SerializedName("totalRefundAmount") val totalRefundAmount: Double? = 0.0,
//    @SerializedName("refundPaymentMode") val refundPaymentMode: String? = null,
//    @SerializedName("approvalStatus") val approvalStatus: String? = null,
//    @SerializedName("rejectionReason") val rejectionReason: String? = null,
//    @SerializedName("internalNotes") val internalNotes: String? = null,
//    @SerializedName("status") val status: String? = null,
//    @SerializedName("priority") val priority: String? = null,
//    @SerializedName("preferredDate") val preferredDate: String? = null,
//    @SerializedName("deliveryDate") val deliveryDate: String? = null,
//    @SerializedName("deliveredAt") val deliveredAt: String? = null,
//    @SerializedName("receivedDate") val receivedDate: String? = null,
//    @SerializedName("createdBy") val createdBy: ServiceStaffUser? = null,
//    @SerializedName("updatedBy") val updatedBy: ServiceStaffUser? = null,
//    @SerializedName("createdAt") val createdAt: String? = null,
//    @SerializedName("updatedAt") val updatedAt: String? = null,
//    @SerializedName("__v") val version: Int? = null
//)
//
//// ── Customer Entities ──
//
//data class ServiceCustomerDetails(
//    @SerializedName("_id") val id: String?,
//    @SerializedName("customerCode") val customerCode: String? = null,
//    @SerializedName("fullName") val fullName: String? = null,
//    @SerializedName("mobileNumber") val mobileNumber: String? = null,
//    @SerializedName("email") val email: String? = null,
//    @SerializedName("customerType") val customerType: String? = null,
//    @SerializedName("billingAddress") val billingAddress: ServiceAddress? = null
//)
//
//data class ServiceCustomerSnapshotFull(
//    @SerializedName("customerCode") val customerCode: String? = null,
//    @SerializedName("name") val name: String? = null,
//    @SerializedName("email") val email: String? = null,
//    @SerializedName("phone") val phone: String? = null,
//    @SerializedName("billingAddress") val billingAddress: ServiceAddress? = null
//)
//
//data class ServiceAddress(
//    @SerializedName("flatNo") val flatNo: String? = null,
//    @SerializedName("street") val street: String? = null,
//    @SerializedName("areaZone") val areaZone: String? = null,
//    @SerializedName("city") val city: String? = null,
//    @SerializedName("subdivisionCode") val subdivisionCode: String? = null,
//    @SerializedName("subdivisionName") val subdivisionName: String? = null,
//    @SerializedName("countryCode") val countryCode: String? = null,
//    @SerializedName("countryName") val countryName: String? = null,
//    @SerializedName("pincode") val pincode: String? = null
//)
//
//// ── Item Details ──
//
//data class ServiceGarmentItem(
//    @SerializedName("_id") val id: String,
//    @SerializedName("originalOrderItemId") val originalOrderItemId: String? = null,
//    @SerializedName("category") val category: String? = null,
//    @SerializedName("garmentDescription") val garmentDescription: String? = null,
//    @SerializedName("segmentName") val segmentName: String? = null,
//    @SerializedName("garmentName") val garmentName: String? = null,
//    @SerializedName("garmentCategoryName") val garmentCategoryName: String? = null,
//    @SerializedName("quantity") val quantity: Int? = 1,
//    @SerializedName("specialRequirements") val specialRequirements: List<String>? = emptyList(),
//    @SerializedName("repairs") val repairs: List<ServiceRepairAction>? = emptyList(),
//    @SerializedName("alterations") val alterations: List<ServiceAlterationAction>? = emptyList(),
//    @SerializedName("issueSummary") val issueSummary: String? = null,
//    @SerializedName("internalNotes") val internalNotes: String? = null,
//    @SerializedName("measurementId") val measurementId: String? = null,
//    @SerializedName("isChargeable") val isChargeable: Boolean? = false,
//    @SerializedName("serviceCharge") val serviceCharge: Double? = 0.0,
//    @SerializedName("sacCode") val sacCode: String? = null,
//    @SerializedName("taxGroupId") val taxGroupId: ServiceTaxGroupRef? = null,
//    @SerializedName("taxableAmount") val taxableAmount: Double? = 0.0,
//    @SerializedName("taxBreakdown") val taxBreakdown: List<ServiceTaxBreakdownItem>? = emptyList(),
//    @SerializedName("taxAmount") val taxAmount: Double? = 0.0,
//    @SerializedName("lineTotal") val lineTotal: Double? = 0.0,
//    @SerializedName("incomeAccountId") val incomeAccountId: ServiceAccountRef? = null,
//    @SerializedName("productionTemplateId") val productionTemplateId: ServiceTemplateRef? = null,
//    @SerializedName("jobCardId") val jobCardId: ServiceJobCardRef? = null,
//    @SerializedName("productionState") val productionState: String? = null,
//    @SerializedName("currentProductionStageName") val currentProductionStageName: String? = null,
//    @SerializedName("itemDeliveryDate") val itemDeliveryDate: String? = null,
//    @SerializedName("itemStatus") val itemStatus: String? = null
//)
//
//data class ServiceAlterationAction(
//    @SerializedName("targetArea") val targetArea: String? = null,
//    @SerializedName("action") val action: String? = null,
//    @SerializedName("currentValue") val currentValue: Double? = null,
//    @SerializedName("targetValue") val targetValue: Double? = null,
//    @SerializedName("value") val value: Double? = null,
//    @SerializedName("unit") val unit: String? = null,
//    @SerializedName("notes") val notes: String? = null
//)
//
//data class ServiceRepairAction(
//    @SerializedName("repairArea") val repairArea: String? = null,
//    @SerializedName("action") val action: String? = null,
//    @SerializedName("notes") val notes: String? = null
//)
//
//data class ServiceTaxGroupRef(
//    @SerializedName("_id") val id: String,
//    @SerializedName("name") val name: String,
//    @SerializedName("totalRate") val totalRate: Double,
//    @SerializedName("isCompound") val isCompound: Boolean = false
//)
//
//data class ServiceTaxBreakdownItem(
//    @SerializedName("_id") val id: String? = null,
//    @SerializedName("taxRateId") val taxRateId: String? = null,
//    @SerializedName("name") val name: String? = null,
//    @SerializedName("rate") val rate: Double? = 0.0,
//    @SerializedName("taxAccountId") val taxAccountId: String? = null,
//    @SerializedName("amount") val amount: Double? = 0.0
//)
//
//data class ServiceAccountRef(
//    @SerializedName("_id") val id: String?,
//    @SerializedName("accountName") val accountName: String?,
//    @SerializedName("accountCode") val accountCode: String?
//)
//
//data class ServiceTemplateRef(
//    @SerializedName("_id") val id: String?,
//    @SerializedName("name") val name: String?,
//    @SerializedName("code") val code: String?
//)
//
//data class ServiceJobCardRef(
//    @SerializedName("_id") val id: String?,
//    @SerializedName("jobCardCode") val jobCardCode: String?,
//    @SerializedName("totalQuantity") val totalQuantity: Int? = 1,
//    @SerializedName("status") val status: String? = null,
//    @SerializedName("currentStageName") val currentStageName: String? = null
//)
//
//data class ServiceAttachment(
//    @SerializedName("url") val url: String?,
//    @SerializedName("publicId") val publicId: String? = null,
//    @SerializedName("originalName") val originalName: String? = null,
//    @SerializedName("uploadedAt") val uploadedAt: String? = null
//)
//
//data class ServiceStaffUser(
//    @SerializedName("_id") val id: String?,
//    @SerializedName("firstName") val firstName: String? = null,
//    @SerializedName("lastName") val lastName: String? = null
//)











// ── Dropdown Data Models ──

data class SalesOrderDropdownResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<SalesOrderDropdownItem>? = emptyList(),
    @SerializedName("message") val message: String? = null
)

data class SalesOrderDropdownItem(
    @SerializedName("_id") val id: String,
    @SerializedName("orderCode") val orderCode: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("orderDate") val orderDate: String? = null,
    @SerializedName("dueDate") val dueDate: String? = null,
    @SerializedName("customer") val customer: SalesOrderCustomerRef? = null
)

data class SalesOrderCustomerRef(
    @SerializedName("fullName") val fullName: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("email") val email: String? = null
)

// ── Root API Responses ──

data class ServiceRequestListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: List<ServiceRequestListItem>? = emptyList(),
    @SerializedName("pagination") val pagination: ServiceRequestPagination? = null
)

data class SingleServiceRequestResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: ServiceRequestListItem? = null
)

data class ServiceRequestPagination(
    @SerializedName("total") val total: Int = 0,
    @SerializedName("page") val page: Int = 1,
    @SerializedName("limit") val limit: Int = 10,
    @SerializedName("totalPages") val totalPages: Int = 1,
    @SerializedName("hasNextPage") val hasNextPage: Boolean = false,
    @SerializedName("hasPreviousPage") val hasPreviousPage: Boolean = false
)

// ── Core Service Request Entity ──

typealias ServiceRequestDetailData = ServiceRequestListItem

data class ServiceRequestListItem(
    @SerializedName("_id") val id: String,
    @SerializedName("organizationId") val organizationId: String? = null,
    @SerializedName("branchId") val branchId: String? = null,
    @SerializedName("garmentSource") val garmentSource: String? = null,
    @SerializedName("serviceRequestCode") val serviceRequestCode: String? = null,
    @SerializedName("primaryCategory") val primaryCategory: String? = null,
    @SerializedName("customerId") val customerId: ServiceCustomerDetails? = null,
    @SerializedName("originalSalesOrderId") val originalSalesOrderId: String? = null,
    @SerializedName("customerSnapshot") val customerSnapshot: ServiceCustomerSnapshotFull? = null,
    @SerializedName("items") val items: List<ServiceGarmentItem>? = emptyList(),
    @SerializedName("attachments") val attachments: List<ServiceAttachment>? = emptyList(),
    @SerializedName("priceIncludesTax") val priceIncludesTax: Boolean? = false,
    @SerializedName("taxType") val taxType: String? = null,
    @SerializedName("placeOfSupply") val placeOfSupply: String? = null,
    @SerializedName("subtotal") val subtotal: Double? = 0.0,
    @SerializedName("totalTax") val totalTax: Double? = 0.0,
    @SerializedName("grandTotal") val grandTotal: Double? = 0.0,
    @SerializedName("advanceAmountPaid") val advanceAmountPaid: Double? = 0.0,
    @SerializedName("balanceAmount") val balanceAmount: Double? = 0.0,
    @SerializedName("paymentStatus") val paymentStatus: String? = null,
    @SerializedName("invoiceId") val invoiceId: String? = null,
    @SerializedName("paymentMode") val paymentMode: String? = null,
    @SerializedName("refundStatus") val refundStatus: String? = null,
    @SerializedName("totalRefundAmount") val totalRefundAmount: Double? = 0.0,
    @SerializedName("refundPaymentMode") val refundPaymentMode: String? = null,
    @SerializedName("approvalStatus") val approvalStatus: String? = null,
    @SerializedName("rejectionReason") val rejectionReason: String? = null,
    @SerializedName("internalNotes") val internalNotes: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("priority") val priority: String? = null,
    @SerializedName("preferredDate") val preferredDate: String? = null,
    @SerializedName("deliveryDate") val deliveryDate: String? = null,
    @SerializedName("deliveredAt") val deliveredAt: String? = null,
    @SerializedName("receivedDate") val receivedDate: String? = null,
    @SerializedName("createdBy") val createdBy: ServiceStaffUser? = null,
    @SerializedName("updatedBy") val updatedBy: ServiceStaffUser? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null,
    @SerializedName("__v") val version: Int? = null
)

// ── Customer Entities ──

data class ServiceCustomerDetails(
    @SerializedName("_id") val id: String?,
    @SerializedName("customerCode") val customerCode: String? = null,
    @SerializedName("fullName") val fullName: String? = null,
    @SerializedName("mobileNumber") val mobileNumber: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("customerType") val customerType: String? = null,
    @SerializedName("billingAddress") val billingAddress: ServiceAddress? = null
)

data class ServiceCustomerSnapshotFull(
    @SerializedName("customerCode") val customerCode: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("billingAddress") val billingAddress: ServiceAddress? = null
)

data class ServiceAddress(
    @SerializedName("flatNo") val flatNo: String? = null,
    @SerializedName("street") val street: String? = null,
    @SerializedName("areaZone") val areaZone: String? = null,
    @SerializedName("city") val city: String? = null,
    @SerializedName("subdivisionCode") val subdivisionCode: String? = null,
    @SerializedName("subdivisionName") val subdivisionName: String? = null,
    @SerializedName("countryCode") val countryCode: String? = null,
    @SerializedName("countryName") val countryName: String? = null,
    @SerializedName("pincode") val pincode: String? = null
)

// ── Item Details ──

data class ServiceGarmentItem(
    @SerializedName("_id") val id: String,
    @SerializedName("originalOrderItemId") val originalOrderItemId: String? = null,
    @SerializedName("category") val category: String? = null,
    @SerializedName("garmentDescription") val garmentDescription: String? = null,
    @SerializedName("segmentName") val segmentName: String? = null,
    @SerializedName("garmentName") val garmentName: String? = null,
    @SerializedName("garmentCategoryName") val garmentCategoryName: String? = null,
    @SerializedName("quantity") val quantity: Int? = 1,
    @SerializedName("specialRequirements") val specialRequirements: List<String>? = emptyList(),
    @SerializedName("repairs") val repairs: List<ServiceRepairAction>? = emptyList(),
    @SerializedName("alterations") val alterations: List<ServiceAlterationAction>? = emptyList(),
    @SerializedName("issueSummary") val issueSummary: String? = null,
    @SerializedName("internalNotes") val internalNotes: String? = null,
    @SerializedName("measurementId") val measurementId: String? = null,
    @SerializedName("isChargeable") val isChargeable: Boolean? = false,
    @SerializedName("serviceCharge") val serviceCharge: Double? = 0.0,
    @SerializedName("sacCode") val sacCode: String? = null,
    @SerializedName("taxGroupId") val taxGroupId: ServiceTaxGroupRef? = null,
    @SerializedName("taxableAmount") val taxableAmount: Double? = 0.0,
    @SerializedName("taxBreakdown") val taxBreakdown: List<ServiceTaxBreakdownItem>? = emptyList(),
    @SerializedName("taxAmount") val taxAmount: Double? = 0.0,
    @SerializedName("lineTotal") val lineTotal: Double? = 0.0,
    @SerializedName("incomeAccountId") val incomeAccountId: ServiceAccountRef? = null,
    @SerializedName("productionTemplateId") val productionTemplateId: ServiceTemplateRef? = null,
    @SerializedName("jobCardId") val jobCardId: ServiceJobCardRef? = null,
    @SerializedName("productionState") val productionState: String? = null,
    @SerializedName("currentProductionStageName") val currentProductionStageName: String? = null,
    @SerializedName("itemDeliveryDate") val itemDeliveryDate: String? = null,
    @SerializedName("itemStatus") val itemStatus: String? = null
)

data class ServiceAlterationAction(
    @SerializedName("targetArea") val targetArea: String? = null,
    @SerializedName("action") val action: String? = null,
    @SerializedName("currentValue") val currentValue: Double? = null,
    @SerializedName("targetValue") val targetValue: Double? = null,
    @SerializedName("value") val value: Double? = null,
    @SerializedName("unit") val unit: String? = null,
    @SerializedName("notes") val notes: String? = null
)

data class ServiceRepairAction(
    @SerializedName("repairArea") val repairArea: String? = null,
    @SerializedName("action") val action: String? = null,
    @SerializedName("notes") val notes: String? = null
)

data class ServiceTaxGroupRef(
    @SerializedName("_id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("totalRate") val totalRate: Double,
    @SerializedName("isCompound") val isCompound: Boolean = false
)

data class ServiceTaxBreakdownItem(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("taxRateId") val taxRateId: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("rate") val rate: Double? = 0.0,
    @SerializedName("taxAccountId") val taxAccountId: String? = null,
    @SerializedName("amount") val amount: Double? = 0.0
)

data class ServiceAccountRef(
    @SerializedName("_id") val id: String?,
    @SerializedName("accountName") val accountName: String?,
    @SerializedName("accountCode") val accountCode: String?
)

data class ServiceTemplateRef(
    @SerializedName("_id") val id: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("code") val code: String?
)

data class ServiceJobCardRef(
    @SerializedName("_id") val id: String?,
    @SerializedName("jobCardCode") val jobCardCode: String?,
    @SerializedName("totalQuantity") val totalQuantity: Int? = 1,
    @SerializedName("status") val status: String? = null,
    @SerializedName("currentStageName") val currentStageName: String? = null
)

data class ServiceAttachment(
    @SerializedName("url") val url: String?,
    @SerializedName("publicId") val publicId: String? = null,
    @SerializedName("originalName") val originalName: String? = null,
    @SerializedName("uploadedAt") val uploadedAt: String? = null
)

data class ServiceStaffUser(
    @SerializedName("_id") val id: String?,
    @SerializedName("firstName") val firstName: String? = null,
    @SerializedName("lastName") val lastName: String? = null
)

// ── Request Payloads ──

data class CreateServiceRequestPayload(
    @SerializedName("customerId") val customerId: String,
    @SerializedName("primaryCategory") val primaryCategory: String = "Alteration",
    @SerializedName("originalSalesOrderId") val originalSalesOrderId: String? = null,
    @SerializedName("priority") val priority: String = "Medium",
    @SerializedName("deliveryDate") val deliveryDate: String? = null,
    @SerializedName("items") val items: List<CreateServiceRequestItemPayload>,
    @SerializedName("advanceAmountPaid") val advanceAmountPaid: Double = 0.0,
    @SerializedName("paymentMode") val paymentMode: String = "UPI",
    @SerializedName("attachments") val attachments: List<String> = emptyList()
)

data class CreateServiceRequestItemPayload(
    @SerializedName("category") val category: String = "Alteration",
    @SerializedName("garmentDescription") val garmentDescription: String,
    @SerializedName("quantity") val quantity: Int = 1,
    @SerializedName("serviceCharge") val serviceCharge: Double = 0.0,
    @SerializedName("issueSummary") val issueSummary: String = "",
    @SerializedName("internalNotes") val internalNotes: String = "",
    @SerializedName("alterations") val alterations: List<ServiceAlterationAction> = emptyList()
)

data class UpdateServiceStatusPayload(
    @SerializedName("status") val status: String
)

// ── Extension Mappers ──

fun ServiceStaffUser.toServiceUserRef(): ServiceUserRef {
    return ServiceUserRef(
        id = id ?: "",
        firstName = firstName,
        lastName = lastName
    )
}

fun ServiceCustomerDetails.toServiceCustomerRef(): ServiceCustomerRef {
    return ServiceCustomerRef(
        id = id ?: "",
        customerCode = customerCode,
        fullName = fullName ?: "",
        mobileNumber = mobileNumber ?: "",
        email = email
    )
}

fun ServiceGarmentItem.toServiceRequestItem(): ServiceRequestItem {
    return ServiceRequestItem(
        id = id,
        originalOrderItemId = originalOrderItemId,
        category = category ?: "Alteration",
        garmentDescription = garmentDescription ?: garmentName ?: "",
        segmentName = segmentName,
        garmentName = garmentName,
        garmentCategoryName = garmentCategoryName,
        quantity = quantity ?: 1,
        alterations = alterations?.map {
            AlterationDetail(
                targetArea = it.targetArea ?: "",
                action = it.action ?: "",
                value = it.value ?: it.targetValue ?: it.currentValue ?: 0.0,
                unit = it.unit ?: "inch",
                notes = it.notes ?: ""
            )
        } ?: emptyList(),
        issueSummary = issueSummary ?: "",
        internalNotes = internalNotes,
        measurementId = measurementId,
        isChargeable = isChargeable ?: true,
        serviceCharge = serviceCharge ?: 0.0,
        sacCode = sacCode ?: "998812",
        taxGroupId = taxGroupId,
        taxableAmount = taxableAmount ?: 0.0,
        taxBreakdown = taxBreakdown?.map {
            ServiceTaxBreakdown(
                id = it.id,
                taxRateId = it.taxRateId,
                name = it.name ?: "",
                rate = it.rate ?: 0.0,
                amount = it.amount ?: 0.0
            )
        } ?: emptyList(),
        taxAmount = taxAmount ?: 0.0,
        lineTotal = lineTotal ?: 0.0,
        incomeAccountId = incomeAccountId?.let {
            ServiceIncomeAccountRef(
                id = it.id ?: "",
                accountName = it.accountName ?: "",
                accountCode = it.accountCode ?: ""
            )
        },
        productionTemplateId = productionTemplateId?.id,
        jobCardId = jobCardId,
        itemDeliveryDate = itemDeliveryDate,
        itemStatus = itemStatus ?: "Received_In_Workshop"
    )
}

fun ServiceRequestListItem.toServiceRequestData(): ServiceRequestData {
    return ServiceRequestData(
        id = id,
        organizationId = organizationId,
        branchId = branchId,
        serviceRequestCode = serviceRequestCode ?: "",
        primaryCategory = primaryCategory ?: "Alteration",
        customerId = customerId?.toServiceCustomerRef() ?: customerSnapshot?.let {
            ServiceCustomerRef(
                id = "",
                customerCode = it.customerCode,
                fullName = it.name ?: "",
                mobileNumber = it.phone ?: "",
                email = it.email
            )
        },
        originalSalesOrderId = originalSalesOrderId,
        items = items?.map { it.toServiceRequestItem() } ?: emptyList(),
        attachments = attachments?.mapNotNull { it.url } ?: emptyList(),
        priceIncludesTax = priceIncludesTax ?: false,
        taxType = taxType ?: "Standard",
        placeOfSupply = placeOfSupply,
        subtotal = subtotal ?: 0.0,
        totalTax = totalTax ?: 0.0,
        grandTotal = grandTotal ?: 0.0,
        advanceAmountPaid = advanceAmountPaid ?: 0.0,
        balanceAmount = balanceAmount ?: 0.0,
        paymentStatus = paymentStatus ?: "Unpaid",
        invoiceId = invoiceId,
        paymentMode = paymentMode,
        approvalStatus = approvalStatus ?: "Not_Required",
        status = status ?: "Draft",
        priority = priority ?: "Medium",
        deliveryDate = deliveryDate,
        createdBy = createdBy?.toServiceUserRef(),
        updatedBy = updatedBy?.toServiceUserRef(),
        receivedDate = receivedDate,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
