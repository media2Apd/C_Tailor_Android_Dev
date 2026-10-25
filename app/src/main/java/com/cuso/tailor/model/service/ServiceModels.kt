@file:Suppress("unused", "AssignedValueIsNeverRead")

package com.cuso.tailor.model.service

import com.google.gson.JsonElement
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName

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










data class ServiceOrderListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: List<ServiceOrderItemDto>?,
    @SerializedName("pagination") val pagination: ServiceOrderPagination?
)

data class ServiceOrderPagination(
    @SerializedName("total") val total: Int,
    @SerializedName("page") val page: Int,
    @SerializedName("limit") val limit: Int,
    @SerializedName("totalPages") val totalPages: Int,
    @SerializedName("hasNextPage") val hasNextPage: Boolean? = false,
    @SerializedName("hasPreviousPage") val hasPreviousPage: Boolean? = false
)

data class ServiceOrderItemDto(
    @SerializedName("_id") val id: String,
    @SerializedName("serviceOrderCode") val serviceOrderCode: String? = null,
    @SerializedName("orderCode") val orderCode: String? = null,
    @SerializedName("orderDate") val orderDate: String? = null,
    @SerializedName("dueDate") val dueDate: String? = null,
    @SerializedName("deliveryDate") val deliveryDate: String? = null,
    @SerializedName("priority") val priority: String? = "Medium",
    @SerializedName("orderType") val orderType: String? = null,
    @SerializedName("serviceCategory") val serviceCategory: String? = null,
    @SerializedName("garmentSource") val garmentSource: String? = null,

    // Directly maps to API: "customer": { "name": "...", "phone": "..." }
    @SerializedName("customer") val customer: ServiceOrderCustomer? = null,

    // Fallbacks for older endpoints
    @SerializedName("customerSnapshot") val customerSnapshot: ServiceOrderCustomerSnapshot? = null,
    @SerializedName("customerId") val customerId: ServiceOrderCustomerInfo? = null,

    // Directly maps to API: "production": { ... }
    @SerializedName("production") val production: ServiceOrderProduction? = null,

    @SerializedName("primaryCategory") val primaryCategory: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("grandTotal") val grandTotal: Double? = 0.0,
    @SerializedName("advanceAmountPaid") val advanceAmountPaid: Double? = 0.0,
    @SerializedName("balanceAmount") val balanceAmount: Double? = 0.0,
    @SerializedName("items") val items: List<ServiceOrderGarmentItem>? = null,
    @SerializedName("createdAt") val createdAt: String? = null
)

data class ServiceOrderCustomer(
    @SerializedName("name") val name: String?,
    @SerializedName("phone") val phone: String?
)

data class ServiceOrderProduction(
    @SerializedName("totalJobCards") val totalJobCards: Int? = 0,
    @SerializedName("jobCardCodes") val jobCardCodes: List<String>? = emptyList(),
    @SerializedName("totalPieces") val totalPieces: Int? = 0,
    @SerializedName("completedPieces") val completedPieces: Int? = 0,
    @SerializedName("overallStatus") val overallStatus: String? = null,
    @SerializedName("currentStages") val currentStages: List<String>? = emptyList()
)

data class ServiceOrderCustomerSnapshot(
    @SerializedName("name") val name: String?,
    @SerializedName("phone") val phone: String?,
    @SerializedName("email") val email: String?
)

data class ServiceOrderCustomerInfo(
    @SerializedName("_id") val id: String?,
    @SerializedName("fullName") val fullName: String?,
    @SerializedName("mobileNumber") val mobileNumber: String?,
    @SerializedName("email") val email: String?
)

data class ServiceOrderGarmentItem(
    @SerializedName("_id") val id: String?,
    @SerializedName("garmentName") val garmentName: String?,
    @SerializedName("garmentDescription") val garmentDescription: String?,
    @SerializedName("category") val category: String?,
    @SerializedName("quantity") val quantity: Int?,
    @SerializedName("unitPrice") val unitPrice: Double?,
    @SerializedName("status") val status: String?
)

data class ServiceOrderDetailResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: ServiceOrderDetailData? = null
)

data class ServiceOrderDetailData(
    @SerializedName("orderHeader") val orderHeader: ServiceOrderHeader? = null,
    @SerializedName("totalJobCardsCount") val totalJobCardsCount: Int = 0,
    @SerializedName("jobCards") val jobCards: List<ServiceJobCardDetail> = emptyList()
)

data class ServiceOrderHeader(
    @SerializedName("_id") val id: String,
    @SerializedName("orderCode") val orderCode: String? = null,
    @SerializedName("orderType") val orderType: String? = null,
    @SerializedName("serviceCategory") val serviceCategory: String? = null,
    @SerializedName("garmentSource") val garmentSource: String? = null,
    @SerializedName("orderDate") val orderDate: String? = null,
    @SerializedName("dueDate") val dueDate: String? = null,
    @SerializedName("priority") val priority: String? = "Medium",
    @SerializedName("status") val status: String? = "Pending",
    @SerializedName("customer") val customer: ServiceCustomerHeader? = null,
    @SerializedName("payment") val payment: ServicePaymentHeader? = null
)

data class ServiceCustomerHeader(
    @SerializedName("name") val name: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("customerCode") val customerCode: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("customerLevel") val customerLevel: String? = null,
    @SerializedName("customerId") val customerId: String? = null,
    @SerializedName("billingAddress") val billingAddress: ServiceBillingAddress? = null
)

data class ServiceBillingAddress(
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

data class ServicePaymentHeader(
    @SerializedName("paymentStatus") val paymentStatus: String? = "Unpaid",
    @SerializedName("paymentMode") val paymentMode: String? = "Cash",
    @SerializedName("grandTotal") val grandTotal: Double = 0.0,
    @SerializedName("advanceAmountPaid") val advanceAmountPaid: Double = 0.0,
    @SerializedName("balanceAmount") val balanceAmount: Double = 0.0,
    @SerializedName("currency") val currency: String? = "INR"
)

data class ServiceJobCardDetail(
    @SerializedName("_id") val id: String,
    @SerializedName("jobCardCode") val jobCardCode: String? = null,
    @SerializedName("status") val status: String? = "Pending",
    @SerializedName("priority") val priority: String? = "Medium",
    @SerializedName("currentStageName") val currentStageName: String? = null,
    @SerializedName("trackingType") val trackingType: String? = null,
    @SerializedName("pieceIndex") val pieceIndex: Int? = null,
    @SerializedName("serialCode") val serialCode: String? = null,
    @SerializedName("totalQuantity") val totalQuantity: Int = 1,
    @SerializedName("completedQuantity") val completedQuantity: Int = 0,
    @SerializedName("defectiveQuantity") val defectiveQuantity: Int = 0,
    @SerializedName("garment") val garment: ServiceJobGarment? = null,
    @SerializedName("stages") val stages: List<ServiceJobStage> = emptyList(),
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("fabricWastage")
    val fabricWastage: ServiceFabricWastage? = null,

    @SerializedName("hasMaterialRequisition")
    val hasMaterialRequisition: Boolean = false,

    @SerializedName("materialRequisitionCount")
    val materialRequisitionCount: Int = 0,

    @SerializedName("materialsIssued")
    val materialsIssued: List<JsonElement> = emptyList()
)

data class ServiceFabricWastage(
    @SerializedName("totalIssuedMeters")
    val totalIssuedMeters: Double = 0.0,

    @SerializedName("actualGarmentMeters")
    val actualGarmentMeters: Double = 0.0,

    @SerializedName("wastedMeters")
    val wastedMeters: Double = 0.0,

    @SerializedName("scrapType")
    val scrapType: String? = null,

    @SerializedName("notes")
    val notes: String? = null,

    @SerializedName("loggedBy")
    val loggedBy: JsonElement? = null,

    @SerializedName("loggedAt")
    val loggedAt: String? = null
)

data class ServiceJobGarment(
    @SerializedName("name") val name: String? = null,
    @SerializedName("stitchingType") val stitchingType: String? = null,
    @SerializedName("fabricSource") val fabricSource: String? = null,
    @SerializedName("fabricNotes") val fabricNotes: String? = null,
    @SerializedName("specialInstructions") val specialInstructions: String? = null,
    @SerializedName("colorAccent") val colorAccent: String? = null,
    @SerializedName("sizeStandard") val sizeStandard: String? = null,
    @SerializedName("category") val category: ServiceGarmentCategoryRef? = null,
    @SerializedName("design") val design: ServiceGarmentDesignRef? = null,
    @SerializedName("measurements") val measurements: List<ServiceJobMeasurementItem>? = emptyList(),
    @SerializedName("addons") val addons: List<ServiceJobAddon>? = emptyList()
)

data class ServiceJobAddon(
    @SerializedName("addonName") val addonName: String? = null,
    @SerializedName("workType") val workType: String? = null,
    @SerializedName("specialInstructions") val specialInstructions: String? = null
)

data class ServiceJobMeasurementItem(
    @SerializedName("fieldId") val fieldId: ServiceMeasurementFieldRef? = null,
    @SerializedName("fieldName") val fieldName: String? = null,
    @SerializedName("entries") val entries: List<ServiceMeasurementEntry>? = emptyList(),
    @SerializedName("fitAllowance") val fitAllowance: Double? = 0.0
)

data class ServiceMeasurementFieldRef(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("displayName") val displayName: String? = null,
    @SerializedName("unit") val unit: String? = "inch"
)

data class ServiceMeasurementEntry(
    @SerializedName("subLabel") val subLabel: String? = null,
    @SerializedName("value") val value: Double? = 0.0
)

data class ServiceJobStage(
    @SerializedName("_id") val id: String,
    @SerializedName("stageId") val stageId: ServiceStageMetaRef? = null,
    @SerializedName("stageName") val stageName: String? = null,
    @SerializedName("workType") val workType: String? = null,
    @SerializedName("estimatedMinutes") val estimatedMinutes: Int? = 0,
    @SerializedName("displayOrder") val displayOrder: Int = 0,
    @SerializedName("status") val status: String? = "Pending",
    @SerializedName("allocations") val allocations: List<ServiceStageAllocation> = emptyList(),
    @SerializedName("stageCompletedQuantity") val stageCompletedQuantity: Int = 0,
    @SerializedName("stageDefectiveQuantity") val stageDefectiveQuantity: Int = 0,
    @SerializedName("startedAt") val startedAt: String? = null,
    @SerializedName("completedAt") val completedAt: String? = null,
    @SerializedName("timeSpentMinutes") val timeSpentMinutes: Int? = 0,
    @SerializedName("reworkNotes") val reworkNotes: String? = null
)

@JsonAdapter(StageMetaRefAdapter::class)
data class ServiceStageMetaRef(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("code") val code: String? = null,
    @SerializedName("workType") val workType: String? = null
)

data class ServiceGarmentCategoryRef(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("displayName") val displayName: String? = null,
    @SerializedName("imageUrl") val imageUrl: String? = null
)

data class ServiceGarmentDesignRef(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("code") val code: String? = null,
    @SerializedName("designType") val designType: String? = null,
    @SerializedName("imageUrl") val imageUrl: String? = null,
    @SerializedName("description") val description: String? = null
)
//
//data class ServiceJobStage(
//    @SerializedName("_id") val id: String,
//    @SerializedName("stageName") val stageName: String? = null,
//    @SerializedName("workType") val workType: String? = null,
//    @SerializedName("displayOrder") val displayOrder: Int = 0,
//    @SerializedName("status") val status: String? = "Pending",
//    @SerializedName("allocations") val allocations: List<ServiceStageAllocation> = emptyList(),
//    @SerializedName("stageCompletedQuantity") val stageCompletedQuantity: Int = 0,
//    @SerializedName("startedAt") val startedAt: String? = null,
//    @SerializedName("completedAt") val completedAt: String? = null,
//    @SerializedName("reworkNotes") val reworkNotes: String? = null
//)

data class ServiceStageAllocation(
    @SerializedName("_id") val id: String,
    @SerializedName("workerId") val workerId: ServiceWorkerRef? = null,
    @SerializedName("allocatedQuantity") val allocatedQuantity: Int = 0,
    @SerializedName("completedQuantity") val completedQuantity: Int = 0,
    @SerializedName("status") val status: String? = "Pending",
    @SerializedName("assignedAt") val assignedAt: String? = null,
    @SerializedName("completedAt") val completedAt: String? = null
)

@JsonAdapter(WorkerRefAdapter::class)
data class ServiceWorkerRef(
    @SerializedName("_id") val id: String,
    @SerializedName("firstName") val firstName: String? = null,
    @SerializedName("lastName") val lastName: String? = null,
    @SerializedName("employeeCode") val employeeCode: String? = null,
    @SerializedName("memberId") val memberId: String? = null
)

data class SpecialistWorkloadResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: List<SpecialistWorkloadDto>? = emptyList()
)

data class SpecialistWorkloadDto(
    @SerializedName("_id") val id: String,
    @SerializedName("firstName") val firstName: String? = null,
    @SerializedName("lastName") val lastName: String? = null,
    @SerializedName("fullName") val fullName: String? = null,
    @SerializedName("roleName") val roleName: String? = null,
    @SerializedName("empCode") val empCode: String? = null,
    @SerializedName("activeStagesCount") val activeStagesCount: Int = 0,
    @SerializedName("totalPieces") val totalPieces: Int = 0,
    @SerializedName("workloadPercentage") val workloadPercentage: Int = 0
)

data class JobCardViewOneResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: JobCardViewOneData? = null
)

data class JobCardViewOneData(
    @SerializedName("_id") val id: String,
    @SerializedName("jobCardCode") val jobCardCode: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("priority") val priority: String? = "Medium",
    @SerializedName("currentStageName") val currentStageName: String? = null,
    // Added currentStageStatus from API
    @SerializedName("currentStageStatus") val currentStageStatus: String? = null,
    @SerializedName("totalQuantity") val totalQuantity: Int = 1,
    @SerializedName("completedQuantity") val completedQuantity: Int = 0,
    @SerializedName("orderHeader") val orderHeader: ServiceOrderHeader? = null,
    // Added orderSummary as returned in the API
    @SerializedName("orderSummary") val orderSummary: ServiceOrderHeader? = null,
    @SerializedName("customerSnapshot") val customerSnapshot: ServiceOrderCustomerSnapshot? = null,
    @SerializedName("garment") val garment: ServiceJobGarment? = null,
    @SerializedName("stages") val stages: List<ServiceJobStage> = emptyList(),
    @SerializedName("activityLogs") val activityLogs: List<JobCardActivityLog>? = emptyList(),
    @SerializedName("materialsIssued") val materialsIssued: List<JsonElement>? = emptyList()
)

data class JobCardActivityLog(
    @SerializedName("action") val action: String? = null,
    @SerializedName("timestamp") val timestamp: String? = null,
    @SerializedName("isCompleted") val isCompleted: Boolean = true
)

//REWORK
/**
 * Stage Progress Update Request Payload
 */
data class UpdateStageProgressRequest(
    @SerializedName("stageId")
    val stageId: String,
    @SerializedName("status")
    val status: String, // e.g., "Rework", "Completed", etc.
    @SerializedName("notes")
    val notes: String = ""
)

/**
 * Stage Progress Update Response
 */
data class UpdateStageProgressResponse(
    @SerializedName("success")
    val success: Boolean? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: JobCardViewOneData? = null
)


// Request payload for allocating workers across job card stages
data class AllocateWorkersStageItem(
    @SerializedName("stageId")
    val stageId: String,
    @SerializedName("workerId")
    val workerId: String,
    @SerializedName("allocatedQuantity")
    val allocatedQuantity: Int = 1,
    @SerializedName("estimatedMinutes")
    val estimatedMinutes: Int = 0
)

data class AllocateWorkersRequest(
    @SerializedName("assignments")
    val assignments: List<AllocateWorkersStageItem>,
    @SerializedName("forceReassign")
    val forceReassign: Boolean = false,
    @SerializedName("replaceExisting")
    val replaceExisting: Boolean = true
)

// Response model for allocating workers across job card stages
data class AllocateWorkersResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String?,
    @SerializedName("data")
    val data: JobCardViewOneData? = null
)

// Data class matching the exact Mark Complete & Update Stage payload
data class UpdateStageProgressRequestComplete(
    val stageId: String,
    val status: String,
    val completedQuantity: Int? = null,
    val defectiveQuantity: Int? = null,
    val completedAt: String? = null,
    val startedAt: String? = null,
    val holdReason: String? = null,
    val notes: String? = null
)

// Data class matching the exact Report Issue payload
data class LogIssueRequest(
    val issueType: String,
    val description: String,
    val affectedQuantity: Int = 1
)

// Response data class for logging issues
data class LogIssueResponse(
    val success: Boolean,
    val message: String?,
    val data: Any? = null
)