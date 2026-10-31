@file:Suppress("unused")
package com.cuso.tailor.model.logistics

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

/**
 * Top-level API Response model for returns.
 */
data class ReturnsResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("pagination")
    val pagination: PaginationData,
    @SerializedName("data")
    val data: List<ReturnItem>
)

data class ReturnItem(
    @SerializedName("_id")
    val id: String,
    @SerializedName("organizationId")
    val organizationId: String,
    @SerializedName("returnId")
    val returnId: String,
    @SerializedName("orderId")
    val orderId: ReturnOrderSnapshot?,
    @SerializedName("orderCode")
    val orderCode: String,
    @SerializedName("customer")
    val customer: ReturnCustomer?,
    @SerializedName("returnType")
    val returnType: String,
    @SerializedName("serviceCategory")
    val serviceCategory: String?,
    @SerializedName("priority")
    val priority: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("issueDescription")
    val issueDescription: String?,
    @SerializedName("preferredResolutionNotes")
    val preferredResolutionNotes: String?,
    @SerializedName("internalNotes")
    val internalNotes: String?,
    // CHANGED: server sends objects {url, publicId, name, _id}, not plain strings
    @SerializedName("attachments")
    val attachments: List<ReturnAttachmentDto>? = emptyList(),
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("updatedAt")
    val updatedAt: String
) {
    /** Just the image URLs, safe to use in UI */
    val attachmentUrls: List<String>
        get() = attachments.orEmpty().mapNotNull { it.url }
}

data class ReturnOrderSnapshot(
    @SerializedName("_id")
    val id: String,
    @SerializedName("orderCode")
    val orderCode: String,
    @SerializedName("customerSnapshot")
    val customerSnapshot: ReturnCustomerSnapshot?,
    @SerializedName("grandTotal")
    val grandTotal: Double,
    @SerializedName("status")
    val status: String
)

data class ReturnCustomerSnapshot(
    @SerializedName("customerCode")
    val customerCode: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("email")
    val email: String?,
    @SerializedName("phone")
    val phone: String?,
    @SerializedName("billingAddress")
    val billingAddress: ReturnAddress?,
    @SerializedName("shippingAddress")
    val shippingAddress: ReturnAddress?,
    @SerializedName("taxId")
    val taxId: String?
)

data class ReturnCustomer(
    @SerializedName("name")
    val name: String,
    @SerializedName("phone")
    val phone: String?,
    @SerializedName("email")
    val email: String?,
    @SerializedName("address")
    val address: String?
)

data class ReturnAddress(
    @SerializedName("flatNo")
    val flatNo: String?,
    @SerializedName("street")
    val street: String?,
    @SerializedName("areaZone")
    val areaZone: String?,
    @SerializedName("city")
    val city: String?,
    @SerializedName("subdivisionCode")
    val subdivisionCode: String?,
    @SerializedName("subdivisionName")
    val subdivisionName: String?,
    @SerializedName("countryCode")
    val countryCode: String?,
    @SerializedName("countryName")
    val countryName: String?,
    @SerializedName("pincode")
    val pincode: String?
)

/**
 * API response for Returns list.
 */
data class ReturnsListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("pagination") val pagination: ReturnPaginationDto?,
    @SerializedName("data") val data: List<ReturnItemDto> = emptyList(),
    @SerializedName("message") val message: String? = null
)

/**
 * Single Return detail response from /api/logistics/returns/view-one/:id
 */
data class ReturnDetailResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: ReturnItemDto?,
    @SerializedName("message") val message: String? = null
)


data class ReturnPaginationDto(
    @SerializedName("page") val page: Int,
    @SerializedName("limit") val limit: Int,
    @SerializedName("total") val total: Int,
    @SerializedName("totalPages") val totalPages: Int
)

data class ReturnItemDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("organizationId") val organizationId: String? = null,
    @SerializedName("returnId") val returnId: String = "",
    // Handles orderId as either an object or a plain string ID
    @SerializedName("orderId") val orderIdRaw: JsonElement? = null,
    // Handles garmentSummary as an escaped JSON string or a JSON object
    @SerializedName("garmentSummary") val garmentSummaryRaw: JsonElement? = null,
    @SerializedName("preferredCompletionDate") val preferredCompletionDate: String? = null,
    @SerializedName("orderCode") val orderCode: String = "",
    @SerializedName("customer") val customer: ReturnCustomerDto? = null,
    @SerializedName("returnType") val returnType: String = "",
    @SerializedName("serviceCategory") val serviceCategory: String? = null,
    @SerializedName("priority") val priority: String = "Medium",
    @SerializedName("status") val status: String = "",
    @SerializedName("issueDescription") val issueDescription: String? = null,
    @SerializedName("preferredResolutionNotes") val preferredResolutionNotes: String? = null,
    @SerializedName("internalNotes") val internalNotes: String? = null,
    @SerializedName("attachments") val attachments: List<ReturnAttachmentDto>? = emptyList(),
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
) {
    /** Safe accessor for raw Order ID hex */
    val orderIdString: String
        get() = when {
            orderIdRaw == null || orderIdRaw.isJsonNull -> ""
            orderIdRaw.isJsonPrimitive -> orderIdRaw.asString
            orderIdRaw.isJsonObject && orderIdRaw.asJsonObject.has("_id") ->
                orderIdRaw.asJsonObject.get("_id").asString
            else -> ""
        }

    /** Safe accessor for order snapshot */
    val orderSnapshot: ReturnOrderSnapshotDto?
        get() = try {
            if (orderIdRaw != null && orderIdRaw.isJsonObject) {
                Gson().fromJson(orderIdRaw, ReturnOrderSnapshotDto::class.java)
            } else null
        } catch (_: Exception) { null }

    val orderId: ReturnOrderSnapshotDto?
        get() = orderSnapshot

    /** Safe accessor for GarmentSummaryDto whether API sends JSON String or JSON Object */
    val garmentSummary: GarmentSummaryDto?
        get() = try {
            when {
                garmentSummaryRaw == null || garmentSummaryRaw.isJsonNull -> null
                garmentSummaryRaw.isJsonObject ->
                    Gson().fromJson(garmentSummaryRaw, GarmentSummaryDto::class.java)
                garmentSummaryRaw.isJsonPrimitive && garmentSummaryRaw.asJsonPrimitive.isString -> {
                    val rawStr = garmentSummaryRaw.asString
                    if (rawStr.isNotBlank() && rawStr.startsWith("{")) {
                        Gson().fromJson(rawStr, GarmentSummaryDto::class.java)
                    } else null
                }
                else -> null
            }
        } catch (_: Exception) { null }

    val attachmentUrls: List<String>
        get() = attachments.orEmpty().mapNotNull { it.url }
}

data class ReturnOrderSnapshotDto(
    @SerializedName("_id") val id: String,
    @SerializedName("orderCode") val orderCode: String,
    @SerializedName("customerSnapshot") val customerSnapshot: ReturnCustomerSnapshotDto?,
    @SerializedName("grandTotal") val grandTotal: Double?,
    @SerializedName("status") val status: String?
)

data class ReturnCustomerSnapshotDto(
    @SerializedName("customerCode") val customerCode: String?,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String?,
    @SerializedName("phone") val phone: String?,
    @SerializedName("billingAddress") val billingAddress: ReturnAddressDto?,
    @SerializedName("shippingAddress") val shippingAddress: ReturnAddressDto?,
    @SerializedName("taxId") val taxId: String?
)

data class ReturnCustomerDto(
    @SerializedName("name") val name: String,
    @SerializedName("phone") val phone: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("address") val address: String?
)

data class ReturnAddressDto(
    @SerializedName("flatNo") val flatNo: String?,
    @SerializedName("street") val street: String?,
    @SerializedName("areaZone") val areaZone: String?,
    @SerializedName("city") val city: String?,
    @SerializedName("subdivisionCode") val subdivisionCode: String?,
    @SerializedName("subdivisionName") val subdivisionName: String?,
    @SerializedName("countryCode") val countryCode: String?,
    @SerializedName("countryName") val countryName: String?,
    @SerializedName("pincode") val pincode: String?
)

/**
 * Payload for updating return status.
 */
data class UpdateReturnStatusRequest(
    @SerializedName("status") val status: String,
    @SerializedName("internalNotes") val internalNotes: String? = null,
    @SerializedName("notes") val notes: String? = null
)

data class GarmentSummaryDto(
    @SerializedName("itemName") val itemName: String? = null,
    @SerializedName("orderedDate") val orderedDate: String? = null,
    @SerializedName("deliveredDate") val deliveredDate: String? = null
)

data class ReturnAttachmentDto(
    @SerializedName("_id") val id: String? = null,   // ADDED
    @SerializedName("url") val url: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("publicId") val publicId: String? = null
)

/**
 * Top-level response for fetching delivered orders available for return creation.
 */
data class DeliveredOrdersResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<DeliveredOrderDto> = emptyList(),
    @SerializedName("total") val total: Int? = null
)

/**
 * Delivered order entity matching API response.
 */
data class DeliveredOrderDto(
    @SerializedName("_id") val id: String,
    @SerializedName("orderCode") val orderCode: String,
    @SerializedName("orderDate") val orderDate: String?,
    @SerializedName("dueDate") val dueDate: String?,
    @SerializedName("deliveredAt") val deliveredAt: String?,
    @SerializedName("deliveryNumber") val deliveryNumber: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("customerSnapshot") val customerSnapshot: OrderCustomerSnapshotDto?,
    @SerializedName("customerId") val customerId: OrderCustomerSnapshotDto?,
    @SerializedName("garments") val garments: List<OrderGarmentItemDto> = emptyList(),
    @SerializedName("items") val items: List<OrderGarmentItemDto> = emptyList(),
    @SerializedName("courierPartner") val courierPartner: String? = null
)

/**
 * Customer snapshot data embedded in the order.
 */
data class OrderCustomerSnapshotDto(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("customerCode") val customerCode: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("fullName") val fullName: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("mobileNumber") val mobileNumber: String? = null,
    @SerializedName("billingAddress") val billingAddress: CustomerAddressDto? = null,
    @SerializedName("shippingAddress") val shippingAddress: CustomerAddressDto? = null
)

/**
 * Address representation for formatting full address text.
 */
data class CustomerAddressDto(
    @SerializedName("flatNo") val flatNo: String? = null,
    @SerializedName("street") val street: String? = null,
    @SerializedName("areaZone") val areaZone: String? = null,
    @SerializedName("city") val city: String? = null,
    @SerializedName("subdivisionName") val subdivisionName: String? = null,
    @SerializedName("countryName") val countryName: String? = null,
    @SerializedName("pincode") val pincode: String? = null
) {
    fun toDisplayText(): String {
        return listOfNotNull(flatNo, street, areaZone, city, subdivisionName, pincode)
            .filter { it.isNotBlank() }
            .joinToString(", ")
    }
}

/**
 * Item & Garment representation for primary return garment selection.
 */
data class OrderGarmentItemDto(
    @SerializedName("_id") val id: String,
    @SerializedName("lineType") val lineType: String?,
    @SerializedName("itemDescription") val itemDescription: String?,
    @SerializedName("customGarment") val customGarment: CustomGarmentDetailDto? = null
) {
    fun getDisplayName(): String {
        return customGarment?.categoryDisplayName
            ?: customGarment?.garmentName
            ?: itemDescription
            ?: "Custom Garment"
    }
}

data class CustomGarmentDetailDto(
    @SerializedName("garmentName") val garmentName: String? = null,
    @SerializedName("categoryDisplayName") val categoryDisplayName: String? = null,
    @SerializedName("fabricNotes") val fabricNotes: String? = null,
    @SerializedName("stitchingType") val stitchingType: String? = null
)

/**
 * Customer payload object for creating a return request.
 */
data class CreateReturnCustomerPayload(
    val name: String,
    val phone: String,
    val email: String,
    val address: String
)

/**
 * Garment summary payload object.
 */
data class CreateReturnGarmentSummaryPayload(
    val itemName: String,
    val orderedDate: String,
    val deliveredDate: String
)