package com.cuso.tailor.model.sales

import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

data class OrderDetailResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("data")
    val data: OrderApiResponse?,
    @SerializedName("message")
    val message: String? = null
)

data class CreateOrderRequest(
    val leadId: String? = null,
    @SerializedName("customer")
    val customer: CustomerRequest,              //   object, not customerId string
    @SerializedName("branch")
    val branch: String,                          //   was missing entirely
    @SerializedName("wearerType")
    val wearerType: String? = null,
    @SerializedName("source")
    val source: String? = null,
    @SerializedName("orderType")
    val orderType: String? = null,                //   was missing entirely
    @SerializedName("garments")
    val garments: List<CreateGarmentRequestForCreateOrder>,
    @SerializedName("paymentDetails")
    val paymentDetails: PaymentDetailsRequest,    //   nested object, not flat fields
    @SerializedName("orderDate")
    val orderDate: String,
    @SerializedName("trialDate")
    val trialDate: String? = null,
    @SerializedName("deliveryDate")
    val deliveryDate: String? = null,
    @SerializedName("totalAmount")
    val totalAmount: Double,
    @SerializedName("status")
    val status: String? = null
)
data class CustomerRequest(
    @SerializedName("name")
    val name: String,
    @SerializedName("mobile")
    val mobile: String,
    @SerializedName("address")
    val address: String? = null,
    @SerializedName("gender")
    val gender: String? = null,
    @SerializedName("email")
    val email: String? = null
)
data class CreateGarmentRequestForCreateOrder(
    @SerializedName("category")
    val category: String,
    @SerializedName("categoryName")
    val categoryName: String? = null,
    @SerializedName("models")
    val models: List<GarmentModelRequest> = emptyList(),   //   objects, not plain strings
    @SerializedName("measurements")
    val measurements: Map<String, MeasurementValueRequest>? = null,
    @SerializedName("quantity")
    val quantity: Int,
    @SerializedName("clothType")
    val clothType: String? = null,
    @SerializedName("priority")
    val priority: String? = null,
    @SerializedName("trialRequired")
    val trialRequired: Boolean = false,
    @SerializedName("fabricDetails")
    val fabricDetails: FabricDetailsRequest? = null,
    @SerializedName("stitchingCharge")
    val stitchingCharge: String? = null,
    @SerializedName("price")
    val price: Double = 0.0,
    @SerializedName("total")
    val total: Double = 0.0,
    @SerializedName("additionalCharges")
    val additionalCharges: List<ChargeRequest> = emptyList()
)

data class GarmentModelRequest(
    @SerializedName("modelName")
    val modelName: String,
    @SerializedName("pieceRate")
    val pieceRate: Double? = null,
    @SerializedName("modelIcon")
    val modelIcon: String? = null,
    @SerializedName("_id")
    val id: String? = null
)

data class MeasurementValueRequest(
    @SerializedName("value")
    val value: List<String>,
    @SerializedName("inputType")
    val inputType: String,
    @SerializedName("unit")
    val unit: String
)

data class FabricDetailsRequest(
    @SerializedName("fabricSource")
    val fabricSource: String? = null,
    @SerializedName("fabricType")
    val fabricType: String? = null,
    @SerializedName("color")
    val color: String? = null,
    @SerializedName("pattern")
    val pattern: String? = null
)

data class PaymentDetailsRequest(
    @SerializedName("notes")
    val notes: String? = null,
    @SerializedName("discount")
    val discount: Double = 0.0,
    @SerializedName("summaryAdditionalCharges")
    val summaryAdditionalCharges: List<ChargeRequest> = emptyList(),
    @SerializedName("paymentAmount")
    val paymentAmount: Double = 0.0
)
data class ChargeRequest(
    @SerializedName("name")
    val name: String,
    @SerializedName("amount")
    val amount: Double
)

data class CreateOrderResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("data")
    val data: OrderApiResponse?,
    @SerializedName("message")
    val message: String? = null
)

// ─────────────────────────────────────────────────────────────
// API Response Models
// ─────────────────────────────────────────────────────────────

data class OrderResponse(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("data")
    val data: List<OrderApiResponse> = emptyList(),

    @SerializedName("pagination")
    val pagination: PaginationInfo? = null,

    // Fallbacks for legacy endpoints
    @SerializedName("total")
    private val _total: Int? = null,
    @SerializedName("page")
    private val _page: Int? = null,
    @SerializedName("totalPages")
    private val _totalPages: Int? = null
) {
    val total: Int
        get() = pagination?.total ?: _total ?: data.size

    val page: Int
        get() = pagination?.page ?: _page ?: 1

    val totalPages: Int
        get() = pagination?.totalPages ?: _totalPages ?: 1
}

data class OrderApiResponse(
    @SerializedName("_id")
    val id: String = "",

    @SerializedName("orderCode")
    val orderCode: String? = null,

    @SerializedName("orderNumber")
    val orderNumber: String? = null,

    @SerializedName("customer")
    val customer: CustomerApiResponse? = null,

    @SerializedName("customerId")
    val customerLegacy: CustomerApiResponse? = null,

    @SerializedName("branch")
    val branch: BranchApiResponse? = null,

    @SerializedName("items")
    val items: List<OrderItemDetailResponse>? = null,

    @SerializedName("garments")
    val garmentsLegacy: List<GarmentApiResponse>? = null,

    @SerializedName("grandTotal")
    val grandTotal: Double? = null,

    @SerializedName("totalAmount")
    val totalAmountLegacy: Double? = null,

    @SerializedName("advancePaid")
    val advancePaid: Double? = null,

    @SerializedName("totalPaid")
    val totalPaidLegacy: Double? = null,

    @SerializedName("balanceDue")
    val balanceDue: Double? = null,

    @SerializedName("balanceAmount")
    val balanceAmountLegacy: Double? = null,

    @SerializedName("paymentStatus")
    val paymentStatus: String? = null,

    @SerializedName("orderDate")
    val orderDate: String? = null,

    @SerializedName("dueDate")
    val dueDate: String? = null,

    @SerializedName("deliveryDate")
    val deliveryDate: String? = null,

    @SerializedName("status")
    val status: String? = null,

    @SerializedName("priority")
    val priority: String? = null,

    @SerializedName("source")
    val source: String? = null,

    @SerializedName("createdAt")
    val createdAt: String? = null,

    @SerializedName("updatedAt")
    val updatedAt: String? = null
)

data class CustomerApiResponse(
    @SerializedName("_id")
    val _id: String? = null,

    @SerializedName("id")
    val id: String? = null,

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("phone")
    val phone: String? = null,

    @SerializedName("mobile")
    val mobile: String? = null,

    @SerializedName("code")
    val code: String? = null,

    @SerializedName("email")
    val email: String? = null
)

data class BranchApiResponse(
    @SerializedName("id")
    val id: String? = null,

    @SerializedName("name")
    val name: String? = null
)

data class OrderItemDetailResponse(
    @SerializedName("_id")
    val id: String? = null,

    @SerializedName("itemType")
    val itemType: String? = null,

    @SerializedName("garment")
    val garment: NamedEntity? = null,

    @SerializedName("category")
    val category: NamedEntity? = null,

    @SerializedName("segment")
    val segment: NamedEntity? = null,

    @SerializedName("quantity")
    val quantity: Double? = 1.0,

    @SerializedName("lineTotal")
    val lineTotal: Double? = 0.0
)

data class NamedEntity(
    @SerializedName("id")
    val id: String? = null,

    @SerializedName("name")
    val name: String? = null
)

// Legacy Garment Model
data class GarmentApiResponse(
    @SerializedName("category")
    val category: String? = null,
    @SerializedName("categoryName")
    val categoryName: String? = null,
    @SerializedName("models")
    val models: List<String?>? = null,
    @SerializedName("quantity")
    val quantity: Double? = 1.0,
    @SerializedName("price")
    val price: Double? = null,
    @SerializedName("total")
    val total: Double? = null
)

// ─────────────────────────────────────────────────────────────
// Safe UI Models
// ─────────────────────────────────────────────────────────────

data class OrderItem(
    val id: String,
    val orderNumber: String,
    val customerId: Customer?,
    val garments: List<Garment>,
    val totalAmount: Double,
    val totalPaid: Double,
    val balanceAmount: Double,
    val paymentStatus: String?,
    val orderDate: Long?,
    val deliveryDate: Long?,
    val status: String?,
    val source: String?,
    val branchName: String?
)

data class Customer(
    val id: String?,
    val name: String?,
    val mobile: String?,
    val email: String?,
    val code: String?
)

data class Garment(
    val category: String?,
    val categoryName: String,
    val models: List<String>,
    val quantity: Double,
    val price: Double?,
    val total: Double?
)

// ─────────────────────────────────────────────────────────────
// Null-Safe Mappers
// ─────────────────────────────────────────────────────────────

fun OrderApiResponse.toOrderItem(): OrderItem {
    // Resolve order code
    val resolvedNumber = orderCode?.takeIf { it.isNotBlank() }
        ?: orderNumber?.takeIf { it.isNotBlank() }
        ?: id.takeLast(6).uppercase()

    // Resolve customer
    val activeCustomer = customer ?: customerLegacy

    // Resolve garments from new "items" array or legacy "garments"
    val mappedGarments: List<Garment> = when {
        !items.isNullOrEmpty() -> {
            items.map { item ->
                Garment(
                    category = item.category?.id,
                    categoryName = item.category?.name?.takeIf { it.isNotBlank() }
                        ?: item.garment?.name?.takeIf { it.isNotBlank() }
                        ?: "Custom Garment",
                    models = emptyList(),
                    quantity = item.quantity ?: 1.0,
                    price = null,
                    total = item.lineTotal
                )
            }
        }
        !garmentsLegacy.isNullOrEmpty() -> {
            garmentsLegacy.map { it.toGarment() }
        }
        else -> emptyList()
    }

    val finalTotal = grandTotal ?: totalAmountLegacy ?: 0.0
    val finalPaid = advancePaid ?: totalPaidLegacy ?: 0.0
    val finalBalance = balanceDue ?: balanceAmountLegacy ?: (finalTotal - finalPaid).coerceAtLeast(0.0)

    val finalPaymentStatus = paymentStatus ?: when {
        finalBalance <= 0.0 -> "Paid"
        finalPaid > 0.0 -> "Partial"
        else -> "Unpaid"
    }

    return OrderItem(
        id = id,
        orderNumber = resolvedNumber,
        customerId = activeCustomer?.toCustomer(),
        garments = mappedGarments,
        totalAmount = finalTotal,
        totalPaid = finalPaid,
        balanceAmount = finalBalance,
        paymentStatus = finalPaymentStatus,
        orderDate = (orderDate ?: createdAt)?.toEpochMillis(),
        deliveryDate = (dueDate ?: deliveryDate)?.toEpochMillis(),
        status = status ?: "Pending",
        source = source ?: priority,
        branchName = branch?.name
    )
}

fun CustomerApiResponse.toCustomer() = Customer(
    id = id ?: _id,
    name = name,
    mobile = phone ?: mobile,
    email = email,
    code = code
)

fun GarmentApiResponse.toGarment() = Garment(
    category = category,
    categoryName = categoryName ?: "Garment",
    models = models.orEmpty().filterNotNull(),
    quantity = quantity ?: 1.0,
    price = price,
    total = total
)

private fun String.toEpochMillis(): Long? {
    if (isBlank()) return null
    val formats = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd"
    )
    for (fmt in formats) {
        runCatching {
            SimpleDateFormat(fmt, Locale.getDefault())
                .apply { timeZone = TimeZone.getTimeZone("UTC") }
                .parse(this)?.time
        }.getOrNull()?.let { return it }
    }
    return null
}