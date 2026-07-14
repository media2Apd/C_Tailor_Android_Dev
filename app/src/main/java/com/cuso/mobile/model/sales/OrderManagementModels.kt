package com.cuso.mobile.model.sales

import com.google.gson.annotations.SerializedName

data class OrderManagementResponse(
    val success: Boolean,
    val data: List<OrderManagementItem>,
    val total: Int
)

data class OrderManagementItem(
    @SerializedName("_id") val id: String,
    val orderNumber: String,
    val totalAmount: Double? = null,      // சில entries-ல missing (SO-00027 பாருங்க)
    val customerName: String? = null,     // சில entries-ல missing (SO-00016 பாருங்க)
    val mobile: String? = null,
    val garments: String = "",            // already formatted string: "Shirt (40), Pant (21)"
    val totalGarments: Int = 0,
    val stageStatus: String = "",
    val orderStatus: String = "",
    val paymentStatus: String = "",
    val totalPaid: Double = 0.0,
    val balanceAmount: Double = 0.0,
    val orderDate: String = "",           // ISO string: "2026-07-01T00:00:00.000Z"
    val deliveryDate: String = ""
)


//order view models

data class OrderViewResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("data")
    val data: OrderViewData?
)

data class OrderViewData(
    @SerializedName("order")
    val order: OrderViewDetail,
    @SerializedName("items")
    val items: List<OrderViewGarmentItem>,
    @SerializedName("stages")
    val stages: List<OrderViewStageGroup>,
    @SerializedName("payments")
    val payments: List<OrderViewPayment>? = null,
    @SerializedName("delivery")
    val delivery: OrderViewDelivery? = null
)

data class OrderViewDetail(
    @SerializedName("_id")
    val id: String,
    @SerializedName("organizationId")
    val organizationId: String? = null,
    @SerializedName("orderNumber")
    val orderNumber: String,
    @SerializedName("customerId")
    val customerId: OrderViewCustomer,
    @SerializedName("branch")
    val branch: OrderViewBranch? = null,
    @SerializedName("totalPaid")
    val totalPaid: Double = 0.0,
    @SerializedName("balanceAmount")
    val balanceAmount: Double = 0.0,
    @SerializedName("paymentStatus")
    val paymentStatus: String = "",
    @SerializedName("source")
    val source: String? = null,
    @SerializedName("wearerType")
    val wearerType: String? = null,
    @SerializedName("orderDate")
    val orderDate: String? = null,
    @SerializedName("trialDate")
    val trialDate: String? = null,
    @SerializedName("deliveryDate")
    val deliveryDate: String? = null,
    @SerializedName("status")
    val status: String = "",
    @SerializedName("discount")
    val discount: Double = 0.0,
    @SerializedName("totalAmount")
    val totalAmount: Double = 0.0
)

data class OrderViewCustomer(
    @SerializedName("_id")
    val id: String? = null,
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("mobile")
    val mobile: String? = null,
    @SerializedName("address")
    val address: OrderViewCustomerAddress? = null
)

data class OrderViewCustomerAddress(
    @SerializedName("addressLine")
    val addressLine: String = "",
    @SerializedName("city")
    val city: String = "",
    @SerializedName("pincode")
    val pincode: String = ""
)

data class OrderViewBranch(
    @SerializedName("_id")
    val id: String? = null,
    @SerializedName("name")
    val name: String? = null
)

data class OrderViewGarmentItem(
    @SerializedName("_id")
    val id: String,
    @SerializedName("salesOrderId")
    val salesOrderId: String? = null,
    @SerializedName("categoryName")
    val categoryName: String = "",
    @SerializedName("quantity")
    val quantity: Int = 0,
    @SerializedName("fabricDetails")
    val fabricDetails: OrderViewFabricDetails? = null,
    @SerializedName("stitchingCharge")
    val stitchingCharge: Double = 0.0,
    @SerializedName("priority")
    val priority: String = "",
    @SerializedName("trialRequired")
    val trialRequired: Boolean = false,
    @SerializedName("measurementSnapshot")
    val measurementSnapshot: Map<String, MeasurementValues>? = null  // Added this field
)

// Add this new data class for measurement values
data class MeasurementValues(
    @SerializedName("inputType")
    val inputType: String? = null,
    @SerializedName("unit")
    val unit: String? = null,
    @SerializedName("value")
    val value: List<String> = emptyList()
)

data class OrderViewFabricDetails(
    @SerializedName("fabricSource")
    val fabricSource: String = "",
    @SerializedName("fabricType")
    val fabricType: String = "",
    @SerializedName("color")
    val color: String = "",
    @SerializedName("pattern")
    val pattern: String = ""
)

data class OrderViewStageGroup(
    @SerializedName("_id")
    val id: String,
    @SerializedName("garmentItemId")
    val garmentItemId: String,
    @SerializedName("stages")
    val stages: List<OrderViewStage>,
    @SerializedName("status")
    val status: String = ""
)

data class OrderViewAssignedWorker(
    @SerializedName("_id")
    val id: String,
    @SerializedName("role")
    val role: String? = null,
    @SerializedName("firstName")
    val firstName: String? = null,
    @SerializedName("lastName")
    val lastName: String? = null
)

// Update OrderViewStage
data class OrderViewStage(
    @SerializedName("_id")
    val id: String,
    @SerializedName("stageName")
    val stageName: String,
    @SerializedName("assignedTo")
    val assignedTo: List<OrderViewAssignedWorker> = emptyList(),
    @SerializedName("assignedQuantity")
    val assignedQuantity: Int = 0,
    @SerializedName("completedQuantity")
    val completedQuantity: Int = 0,
    @SerializedName("failedQuantity")
    val failedQuantity: Int = 0,
    @SerializedName("status")
    val status: String = "",
    @SerializedName("completedAt")
val completedAt: String? = null
)

data class OrderViewPayment(
    @SerializedName("_id")
    val id: String? = null,
    @SerializedName("amount")
    val amount: Double? = null,
    @SerializedName("method")
    val method: String? = null,
    @SerializedName("date")
    val date: String? = null
)

data class OrderViewDelivery(
    @SerializedName("_id")
    val id: String? = null,
    @SerializedName("status")
    val status: String? = null
)