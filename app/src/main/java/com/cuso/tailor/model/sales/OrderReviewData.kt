@file:Suppress("unused")

package com.cuso.tailor.model.sales

import android.net.Uri
import com.cuso.tailor.database.entities.SelectedGarment

/**
 * Shared state container preserved throughout the entire 3-step creation flow.
 */
data class OrderReviewData(
    val leadId: String? = null,
    val orderId: String? = null,
    val editOrderId: String? = null,
    val customerId: String = "",
    val customerCode: String = "",
    val branchId: String = "",
    val fullName: String = "",
    val countryCode: String = "+91",
    val phone: String = "",
    val email: String = "",
    val gender: String = "",
    val dressFor: String = "Individual",
    val customerType: String = "Individual",
    val address: String = "",
    val flatNo: String = "",
    val street: String = "",
    val areaZone: String = "",
    val city: String = "",
    val subdivisionCode: String = "TN",
    val subdivisionName: String = "Tamil Nadu",
    val countryCodeIso: String = "IN",
    val countryName: String = "India",
    val pincode: String = "",
    val source: String = "Direct",
    val orderDate: String = "",
    val trialDate: String = "",
    val deliveryDate: String = "",
    val priority: String = "Medium",
    val orderType: String = "New_Stitching",
    val orderNotes: String = "",
    val paymentMode: String = "Cash",
    val paymentType: String = "Advance",
    val paymentTermId: String = "",
    val salespersonId: String = "",
    val advanceAmountRequired: Double = 0.0,
    val advanceAmountPaid: Double = 0.0,
    val deliveryMethod: String = "Store_Pickup",
    val deliveryCharge: Double = 0.0,
    val discount: Double = 0.0,
    val paidSoFar: Double = 0.0,
    val billingNotes: String = "",
    val financeClearanceRequired: Boolean = true,
    val currency: String = "INR",
    val status: String = "Pending_Approval",
    val subtotal: Double = 0.0,
    val grandTotal: Double = 0.0,
    val dynamicItems: List<OrderItemEntry> = emptyList(),
    val garments: List<SelectedGarment> = emptyList(),
    val measurements: Map<String, Float> = emptyMap(),
    val measurementFieldMeta: Map<String, CategoryMeasurementFieldData> = emptyMap(),
    val designImages: List<Uri> = emptyList(),
    val existingImageUrls: List<String> = emptyList(),
    val voiceNoteUri: Uri? = null,

    // Step 1 & Step 2 details
    val segmentId: String = "",
    val garmentId: String = "",
    val garmentCategoryId: String = "",
    val designPresetId: String = "",
    val selectedWorkPricingIds: List<String> = emptyList()
)

// DTOs for JSON string fields in the multipart payload
data class CustomerSnapshotPayload(
    val customerCode: String,
    val name: String,
    val phone: String,
    val email: String,
    val customerType: String,
    val billingAddress: AddressSnapshotPayload,
    val shippingAddress: AddressSnapshotPayload
)

data class AddressSnapshotPayload(
    val flatNo: String = "",
    val street: String = "",
    val areaZone: String = "",
    val city: String = "",
    val subdivisionCode: String = "",
    val subdivisionName: String = "",
    val countryCode: String = "",
    val countryName: String = "",
    val pincode: String = ""
)

data class OrderItemPayload(
    val _id: String,
    val lineType: String,
    val productionTemplateId: String? = null,
    val parentLineId: String? = null,
    val itemDescription: String,
    val isTaxable: Boolean = true,
    val taxGroupId: String? = null,
    val sacCode: String? = null,
    val customGarment: CustomGarmentPayload? = null,
    val addonWork: AddonWorkPayload? = null,
    val quantity: Int,
    val unit: String,
    val unitPrice: Double,
    val discountAmount: Double = 0.0,
    val lineTotal: Double
)
//data class CustomGarmentPayload(
//    @SerializedName("segmentId") val segmentId: String,
//    @SerializedName("segmentName") val segmentName: String,
//    @SerializedName("garmentId") val garmentId: String,
//    @SerializedName("garmentName") val garmentName: String,
//    @SerializedName("garmentCategoryId") val garmentCategoryId: String,
//    @SerializedName("categoryDisplayName") val categoryDisplayName: String,
//    @SerializedName("designId") val designId: String = "",          // "" when no design is selected
//    @SerializedName("designName") val designName: String = "",
//    @SerializedName("stitchingType") val stitchingType: String = "Normal Machine",
//    @SerializedName("fabricSource") val fabricSource: String = "Customer_Provided",
//    @SerializedName("fabricNotes") val fabricNotes: String = "",
//    @SerializedName("specialInstructions") val specialInstructions: String = ""
//)
//
//data class AddonWorkPayload(
//    @SerializedName("workPricingId") val workPricingId: String,
//    @SerializedName("workType") val workType: String,
//    @SerializedName("specialInstructions") val specialInstructions: String = ""
//)

data class MeasurementEntryPayload(
    val fieldId: String,
    val fieldName: String,
    val entries: List<MeasurementSubEntryPayload>,
    val fitAllowance: Int = 0
)

data class MeasurementSubEntryPayload(
    val subLabel: String,
    val value: Float
)
//
//data class AddonWorkPayload(
//    @SerializedName("workPricingId") val workPricingId: String,
//    @SerializedName("workType") val workType: String,
//    @SerializedName("specialInstructions") val specialInstructions: String = ""
//)

/**
 * One line in the order (garment, addon work, fabric, etc.).
 * Maps directly to OrderItemPayload when the order is submitted.
 */
data class OrderItemEntry(
    val id: String = "",
    val lineType: String = "Custom_Garment",   // e.g. Custom_Garment, Addon_Work
    val parentLineId: String? = null,
    val itemDescription: String = "",
    val quantity: Int = 1,
    val unit: String = "pcs",
    val unitPrice: Double = 0.0,
    val discountAmount: Double = 0.0,
    val isTaxable: Boolean = true,
    val taxGroupId: String? = null,
    val sacCode: String? = null,
    val productionTemplateId: String? = null,
    val customGarment: CustomGarmentPayload? = null,
    val addonWork: AddonWorkPayload? = null
) {
    val lineTotal: Double
        get() = (unitPrice * quantity) - discountAmount
}

/**
 * Describes one measurement field for a garment category
 * (e.g. "Chest" with sub-labels "Front" and "Back").
 */
data class CategoryMeasurementFieldData(
    val fieldId: String = "",
    val fieldName: String = "",
    val subLabels: List<String> = emptyList(),
    val unit: String = "in",
    val fitAllowance: Int = 0
)
