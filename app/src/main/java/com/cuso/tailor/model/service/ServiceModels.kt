package com.cuso.tailor.model.service

import com.google.gson.annotations.SerializedName

// ── Generic Response Wrappers ──

data class ServiceRequestListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<ServiceRequestData> = emptyList(),
    @SerializedName("pagination") val pagination: ServicePaginationData? = null,
    @SerializedName("message") val message: String? = null
)

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

data class ServiceTaxGroupRef(
    @SerializedName("_id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("totalRate") val totalRate: Double,
    @SerializedName("isCompound") val isCompound: Boolean = false
)

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

data class ServiceJobCardRef(
    @SerializedName("_id") val id: String,
    @SerializedName("jobCardCode") val jobCardCode: String,
    @SerializedName("totalQuantity") val totalQuantity: Int = 1,
    @SerializedName("status") val status: String,
    @SerializedName("currentStageName") val currentStageName: String? = null
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
    @SerializedName("alterations") val alterations: List<AlterationDetail> = emptyList()
)

data class UpdateServiceStatusPayload(
    @SerializedName("status") val status: String
)