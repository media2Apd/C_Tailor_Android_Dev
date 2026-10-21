package com.cuso.tailor.model.settings

import com.google.gson.annotations.SerializedName

/**
 * Supported document types for fetching document templates
 */
object DocumentType {
    const val BILL = "bill"
    const val PAYMENT_RECEIPT = "paymentReceipt"
    const val PURCHASE_RECEIVE = "purchaseReceive"
    const val SALES_ORDER = "salesOrder"
    const val PACKING_SLIP = "packingSlip"
    const val DELIVERY_SLIP = "deliverySlip"
    const val CREDIT_NOTE = "creditNote"
    const val RETURN_RECEIPT = "returnReceipt"
}

data class DocumentTemplateResponse(
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("data")
    val data: List<DocumentTemplateDto> = emptyList(),
    @SerializedName("message")
    val message: String? = null
)

data class DocumentTemplateDto(
    @SerializedName("_id")
    val id: String = "",
    @SerializedName("organizationId")
    val organizationId: String = "",
    @SerializedName("templateKey")
    val templateKey: String = "",
    @SerializedName("name")
    val name: String = "",
    @SerializedName("applicableTo")
    val applicableTo: List<String> = emptyList(),
    @SerializedName("thumbnailUrl")
    val thumbnailUrl: String = "",
    @SerializedName("designConfig")
    val designConfig: DocumentDesignConfig? = null,
    @SerializedName("isDefault")
    val isDefault: Boolean = false,
    @SerializedName("isActive")
    val isActive: Boolean = true,
    @SerializedName("createdAt")
    val createdAt: String? = null,
    @SerializedName("updatedAt")
    val updatedAt: String? = null
)

data class DocumentDesignConfig(
    @SerializedName("primaryColor")
    val primaryColor: String = "#2563eb",
    @SerializedName("accentColor")
    val accentColor: String = "#0f172a",
    @SerializedName("fontFamily")
    val fontFamily: String = "Inter",
    @SerializedName("showCompanyLogo")
    val showCompanyLogo: Boolean = true,
    @SerializedName("showSignature")
    val showSignature: Boolean = true
)

/**
 * Request payload to set default document template
 */
data class SetDefaultTemplateRequest(
    @SerializedName("docType")
    val docType: String,
    @SerializedName("templateTheme")
    val templateTheme: String // "standard" or "modern"
)

/**
 * API Response when default template is set
 */
data class SetDefaultTemplateResponse(
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: DocumentTemplateDto? = null
)