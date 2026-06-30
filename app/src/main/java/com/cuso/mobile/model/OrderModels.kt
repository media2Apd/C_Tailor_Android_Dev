package com.cuso.mobile.model

import com.google.gson.annotations.SerializedName

// ─────────────────────────────────────────────────────────────
// API Response Models
// ─────────────────────────────────────────────────────────────

data class OrderApiResponse(
    @SerializedName("_id")
    val id: String,
    @SerializedName("orderNumber")
    val orderNumber: String,
    @SerializedName("customerId")
    val customerId: CustomerApiResponse?,
    @SerializedName("garments")
    val garments: List<GarmentApiResponse>,
    @SerializedName("totalAmount")
    val totalAmount: Int?,
    @SerializedName("totalPaid")
    val totalPaid: Int?,
    @SerializedName("balanceAmount")
    val balanceAmount: Int?,
    @SerializedName("paymentStatus")
    val paymentStatus: String?,
    @SerializedName("orderDate")
    val orderDate: String?,
    @SerializedName("deliveryDate")
    val deliveryDate: String?,
    @SerializedName("status")
    val status: String?,
    @SerializedName("source")
    val source: String?,
    @SerializedName("createdAt")
    val createdAt: String?,
    @SerializedName("updatedAt")
    val updatedAt: String?
)

data class CustomerApiResponse(
    @SerializedName("_id")
    val id: String?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("mobile")
    val mobile: String?,
    @SerializedName("email")
    val email: String?
)

data class GarmentApiResponse(
    @SerializedName("category")
    val category: String,
    @SerializedName("categoryName")
    val categoryName: String,
    @SerializedName("models")
    val models: List<String>,
    @SerializedName("quantity")
    val quantity: Int,
    @SerializedName("price")
    val price: Double?,
    @SerializedName("total")
    val total: Double?
)

// ─────────────────────────────────────────────────────────────
// Response Wrappers
// ─────────────────────────────────────────────────────────────

data class OrderResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("data")
    val data: List<OrderApiResponse>,
    @SerializedName("total")
    val total: Int,
    @SerializedName("page")
    val page: Int,
    @SerializedName("totalPages")
    val totalPages: Int
)

data class OrderDetailResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("data")
    val data: OrderApiResponse?,
    @SerializedName("message")
    val message: String? = null
)

// ─────────────────────────────────────────────────────────────
// UI Models (Mapped from API)
// ─────────────────────────────────────────────────────────────

data class OrderItem(
    val id: String,
    val orderNumber: String,
    val customerId: Customer?,
    val garments: List<Garment>,
    val totalAmount: Int?,
    val totalPaid: Int?,
    val balanceAmount: Int?,
    val paymentStatus: String?,
    val orderDate: Long?,
    val deliveryDate: Long?,
    val status: String?,
    val source: String?
)

data class Customer(
    val id: String?,
    val name: String?,
    val mobile: String?,
    val email: String?
)

data class Garment(
    val category: String,
    val categoryName: String,
    val models: List<String>,
    val quantity: Int,
    val price: Double?,
    val total: Double?
)

// ─────────────────────────────────────────────────────────────
// Request Models
// ─────────────────────────────────────────────────────────────

//data class CreateOrderRequest(
//    @SerializedName("customerId")
//    val customerId: String,
//    @SerializedName("garments")
//    val garments: List<CreateGarmentRequest>,
//    @SerializedName("totalAmount")
//    val totalAmount: Int,
//    @SerializedName("totalPaid")
//    val totalPaid: Int? = null,
//    @SerializedName("orderDate")
//    val orderDate: String,
//    @SerializedName("deliveryDate")
//    val deliveryDate: String? = null,
//    @SerializedName("source")
//    val source: String? = null,
//    @SerializedName("notes")
//    val notes: String? = null
//)

data class CreateGarmentRequest(
    @SerializedName("category")
    val category: String,
    @SerializedName("models")
    val models: List<String>,
    @SerializedName("quantity")
    val quantity: Int,
    @SerializedName("price")
    val price: Double,
    @SerializedName("total")
    val total: Double
)

data class UpdateOrderRequest(
    @SerializedName("customerId")
    val customerId: String? = null,
    @SerializedName("garments")
    val garments: List<CreateGarmentRequest>? = null,
    @SerializedName("totalAmount")
    val totalAmount: Int? = null,
    @SerializedName("totalPaid")
    val totalPaid: Int? = null,
    @SerializedName("deliveryDate")
    val deliveryDate: String? = null,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("source")
    val source: String? = null,
    @SerializedName("notes")
    val notes: String? = null
)

// ─────────────────────────────────────────────────────────────
// Mappers
// ─────────────────────────────────────────────────────────────

fun OrderApiResponse.toOrderItem() = OrderItem(
    id            = id,
    orderNumber   = orderNumber,
    customerId    = customerId?.toCustomer(),
    garments      = garments.map { it.toGarment() },
    totalAmount   = totalAmount,
    totalPaid     = totalPaid,
    balanceAmount = balanceAmount,
    paymentStatus = paymentStatus,
    orderDate     = orderDate?.toEpochMillis(),
    deliveryDate  = deliveryDate?.toEpochMillis(),
    status        = status,
    source        = source
)

fun CustomerApiResponse.toCustomer() = Customer(
    id     = id,
    name   = name,
    mobile = mobile,
    email  = email
)

fun GarmentApiResponse.toGarment() = Garment(
    category     = category,
    categoryName = categoryName,
    models       = models,
    quantity     = quantity,
    price        = price,
    total        = total
)

// ISO-8601 date string  →  epoch millis (null if blank / unparseable)
private fun String.toEpochMillis(): Long? {
    if (isBlank()) return null
    val formats = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd"
    )
    for (fmt in formats) {
        runCatching {
            java.text.SimpleDateFormat(fmt, java.util.Locale.getDefault())
                .apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }
                .parse(this)?.time
        }.getOrNull()?.let { return it }
    }
    return null
}



data class CreateOrderRequest(
    @SerializedName("customerId")
    val customerId: String,
    @SerializedName("branch")
    val branch: String? = null,
    @SerializedName("wearerType")
    val wearerType: String? = null,
    @SerializedName("garments")
    val garments: List<CreateGarmentRequestForCreateOrder>,   // ✅ changed type here
    @SerializedName("summaryAdditionalCharges")
    val summaryAdditionalCharges: List<ChargeRequest> = emptyList(),
    @SerializedName("discount")
    val discount: Double = 0.0,
    @SerializedName("totalAmount")
    val totalAmount: Double,
    @SerializedName("totalPaid")
    val totalPaid: Double? = null,
    @SerializedName("paymentStatus")
    val paymentStatus: String? = null,
    @SerializedName("source")
    val source: String? = null,
    @SerializedName("orderDate")
    val orderDate: String,
    @SerializedName("trialDate")
    val trialDate: String? = null,
    @SerializedName("deliveryDate")
    val deliveryDate: String? = null,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("notes")
    val notes: String? = null
)

data class CreateGarmentRequestForCreateOrder(
    @SerializedName("category")
    val category: String,
    @SerializedName("categoryName")
    val categoryName: String? = null,
    @SerializedName("models")
    val models: List<String> = emptyList(),
    @SerializedName("quantity")
    val quantity: Int,
    @SerializedName("price")
    val price: Double,
    @SerializedName("total")
    val total: Double,
    @SerializedName("additionalCharges")
    val additionalCharges: List<ChargeRequest> = emptyList()
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