// app/src/main/java/com/cuso/mobile/model/CustomerSearchModels.kt

package com.cuso.mobile.model.sales

import com.google.gson.annotations.SerializedName

data class CustomerSearchResponse(
    val success: Boolean,
    val customer: CustomerResult?,
    val orders: List<CustomerOrder> = emptyList()
)

data class CustomerResult(
    @SerializedName("_id") val id: String,
    val name: String,
    val mobile: String,
    val type: String = "",
    val status: String = "",
    val address: CustomerAddress? = null
)

data class CustomerAddress(
    val addressLine: String = "",
    val area: String? = null,
    val city: String = "",
    val pincode: String = ""
)

data class CustomerOrder(
    @SerializedName("_id") val id: String,
    val orderNumber: String,
    val status: String,
    val orderDate: String? = null,   //   backend may omit this field
    val garments: List<CustomerGarment> = emptyList(),
    val totalAmount: Double? = null,
    val wearerType: String = "",
    val source: String = "",
    val stylingNotes: String = ""
)

data class CustomerGarment(
    @SerializedName("_id") val id: String,
    val category: String = "",
    val categoryName: String = "",
    val quantity: Int = 1,
    val models: List<String> = emptyList(),
    val priority: String = "",
    val trialRequired: Boolean = false,
    val clothType: String = "",
    val fabricDetails: CustomerFabricDetails? = null,
    val measurementSnapshot: Map<String, MeasurementValue>? = null
)

data class CustomerFabricDetails(
    val fabricSource: String = "",
    val fabricType: String = "",
    val color: String = "",
    val pattern: String = ""
)

data class MeasurementValue(
    val value: List<String> = emptyList(),
    val inputType: String = "",
    val unit: String = ""
)