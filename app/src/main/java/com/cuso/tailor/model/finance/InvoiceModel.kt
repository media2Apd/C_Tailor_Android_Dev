package com.cuso.tailor.model.finance

import com.cuso.tailor.model.sales.PaginationInfo
import com.google.gson.annotations.SerializedName

// ─────────────────────────────────────────────────────────────
// Invoice — View All API Response
// ─────────────────────────────────────────────────────────────

data class InvoiceListResponse(
    val success: Boolean = false,
    val pagination: PaginationInfo? = null,
    val data: List<InvoiceItem> = emptyList()
)

data class InvoiceItem(
    @SerializedName("_id")
    val id: String = "",
    val organizationId: String? = null,
    val branchId: String? = null,
    val invoiceNumber: String = "",
    @com.google.gson.annotations.JsonAdapter(SalesOrderRefDeserializer::class)
    val salesOrderId: SalesOrderRef? = null,
    @com.google.gson.annotations.JsonAdapter(InvoiceCustomerRefDeserializer::class)
    val customerId: InvoiceCustomerRef? = null,
    val customer: CustomerInfo? = null,
    val invoiceDate: String = "",
    val dueDate: String? = null,

    @SerializedName("lines", alternate = ["items"])
    val lines: List<InvoiceLineItem>? = emptyList(),

    val subtotal: Double = 0.0,

    @SerializedName("totalTax", alternate = ["taxAmount"])
    val taxAmount: Double = 0.0,

    @SerializedName("grandTotal", alternate = ["totalAmount"])
    val totalAmount: Double = 0.0,

    @SerializedName("amountPaid", alternate = ["paidAmount"])
    val paidAmount: Double = 0.0,

    @SerializedName("balanceDue", alternate = ["balanceAmount"])
    val balanceAmount: Double = 0.0,

    @SerializedName("totalDiscount", alternate = ["discountAmount"])
    val discountAmount: Double = 0.0,

    val status: String = "",
    val createdAt: String? = null,
    val updatedAt: String? = null,
    @SerializedName("__v")
    val version: Int? = null
) {
    val displayCustomerName: String
        get() = customer?.name ?: customerId?.name ?: customerId?.id ?: "Walk-in Customer"
}

// ─────────────────────────────────────────────────────────────
// Custom Deserializers
// ─────────────────────────────────────────────────────────────

data class SalesOrderRef(
    @SerializedName("_id") val id: String? = null,
    val orderNumber: String? = null,
    val status: String? = null
)

class SalesOrderRefDeserializer : com.google.gson.JsonDeserializer<SalesOrderRef?> {
    override fun deserialize(
        json: com.google.gson.JsonElement?,
        typeOfT: java.lang.reflect.Type?,
        context: com.google.gson.JsonDeserializationContext?
    ): SalesOrderRef? {
        if (json == null || json.isJsonNull) return null
        return when {
            json.isJsonObject -> {
                val obj = json.asJsonObject
                SalesOrderRef(
                    id = obj.get("_id")?.asString,
                    orderNumber = obj.get("orderNumber")?.asString,
                    status = obj.get("status")?.asString
                )
            }
            json.isJsonPrimitive -> SalesOrderRef(id = json.asString)
            else -> null
        }
    }
}

data class InvoiceCustomerRef(
    @SerializedName("_id") val id: String? = null,
    val name: String? = null,
    val mobile: String? = null,
    val email: String? = null
)
data class InvoiceLineItem(
    @SerializedName("_id")
    val id: String? = null,
    @SerializedName("itemDescription", alternate = ["description"])
    val description: String? = null,
    val hsnSku: String? = null,
    val quantity: Int = 1,
    @SerializedName("rate", alternate = ["unitPrice"])
    val unitPrice: Double = 0.0,
    @SerializedName("discountPercent", alternate = ["discount"])
    val discount: String? = null,
    @SerializedName("totalTax", alternate = ["tax"])
    val tax: Double = 0.0,
    @SerializedName("lineTotal", alternate = ["total"])
    val total: Double = 0.0
)

// ─────────────────────────────────────────────────────────────
// Invoice View One Response
// ─────────────────────────────────────────────────────────────

data class InvoiceViewOneResponse(
    val success: Boolean = false,
    val data: InvoiceViewOneData? = null
)

data class AddressSnapshot(
    val flatNo: String? = null,
    val street: String? = null,
    val city: String? = null,
    val state: String? = null,
    val stateCode: String? = null,
    val country: String? = null,
    val pincode: String? = null
) {
    val fullAddress: String
        get() = listOfNotNull(flatNo, street, city, state, pincode).filter { it.isNotBlank() }.joinToString(", ")
}

data class CustomerInfo(
    @SerializedName("_id") val id: String? = null,
    val name: String? = null,
    val billingAddress: String? = null,
    val shippingAddress: String? = null,
    val phone: String? = null,
    val email: String? = null
)

data class BankDetails(
    val bankName: String? = null,
    val accountNo: String? = null,
    val ifscSwift: String? = null
)

data class JournalData(
    @SerializedName("_id")
    val id: String,
    val organizationId: String,
    val branchId: String,
    val entryNumber: String,
    val entryDate: String,
    val reference: String,
    val referenceType: String,
    val referenceId: String,
    val notes: String,
    val isManual: Boolean,
    val lines: List<JournalLine> = emptyList(),
    val status: String,
    val createdBy: String,
    val createdAt: String,
    val updatedAt: String,
    @SerializedName("__v")
    val version: Int? = null
)

// Reads the first non-null primitive value among the given keys.
// Avoids the crash that JsonNull.asString causes.
private fun com.google.gson.JsonObject.optString(vararg keys: String): String? {
    for (key in keys) {
        val element = get(key)
        if (element != null && element.isJsonPrimitive) return element.asString
    }
    return null
}

class InvoiceCustomerRefDeserializer : com.google.gson.JsonDeserializer<InvoiceCustomerRef?> {
    override fun deserialize(
        json: com.google.gson.JsonElement?,
        typeOfT: java.lang.reflect.Type?,
        context: com.google.gson.JsonDeserializationContext?
    ): InvoiceCustomerRef? {
        if (json == null || json.isJsonNull) return null
        return when {
            json.isJsonObject -> {
                val obj = json.asJsonObject
                InvoiceCustomerRef(
                    id = obj.optString("_id"),
                    // The view-one API sends fullName and mobileNumber,
                    // the list API sends name and mobile
                    name = obj.optString("fullName", "name"),
                    mobile = obj.optString("mobileNumber", "mobile"),
                    email = obj.optString("email")
                )
            }
            json.isJsonPrimitive -> InvoiceCustomerRef(id = json.asString)
            else -> null
        }
    }
}

// Customer details frozen on the invoice at creation time
data class InvoiceCustomerSnapshot(
    val customerCode: String? = null,
    val name: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val billingAddress: AddressSnapshot? = null,
    val shippingAddress: AddressSnapshot? = null,
    val taxRegistrationNumber: String? = null
)

data class AuthorizedSignatory(
    val name: String? = null,
    val designation: String? = null,
    val signatureUrl: String? = null,
    val signedAt: String? = null
)

data class CompanyDetails(
    val name: String? = null,
    val address: String? = null,
    val email: String? = null,
    val phone: String? = null,
    @SerializedName("gstin", alternate = ["gstNumber", "taxRegistrationNumber"])
    val gstNumber: String? = null,
    val logoUrl: String? = null
)

data class InvoiceItemDetail(
    @SerializedName("_id")
    val id: String = "",
    val itemName: String? = null,
    @SerializedName("itemDescription", alternate = ["description"])
    val description: String? = null,
    val hsnCode: String? = null,
    val sacCode: String? = null,
    val hsnSku: String? = null,
    val unit: String? = null,
    val quantity: Int = 1,
    @SerializedName("rate", alternate = ["unitPrice"])
    val unitPrice: Double = 0.0,
    // The API sends a number (0), Gson converts it to a String here
    @SerializedName("discountPercent", alternate = ["discount"])
    val discount: String? = null,
    @SerializedName("totalTax", alternate = ["tax"])
    val tax: Double = 0.0,
    @SerializedName("lineTotal", alternate = ["total"])
    val total: Double = 0.0
)

data class InvoiceViewOneData(
    @SerializedName("_id")
    val id: String = "",
    val organizationId: String? = null,
    val branchId: String? = null,
    val invoiceNumber: String = "",
    @com.google.gson.annotations.JsonAdapter(SalesOrderRefDeserializer::class)
    val salesOrderId: SalesOrderRef? = null,
    @com.google.gson.annotations.JsonAdapter(InvoiceCustomerRefDeserializer::class)
    val customerId: InvoiceCustomerRef? = null,
    val customer: CustomerInfo? = null,
    val customerSnapshot: InvoiceCustomerSnapshot? = null,
    val authorizedSignatory: AuthorizedSignatory? = null,
    val invoiceDate: String = "",
    val dueDate: String? = null,

    @SerializedName("lines", alternate = ["items"])
    val items: List<InvoiceItemDetail>? = emptyList(),

    val subtotal: Double = 0.0,

    @SerializedName("totalTax", alternate = ["taxAmount"])
    val taxAmount: Double = 0.0,

    @SerializedName("totalDiscount", alternate = ["discountAmount"])
    val discountAmount: Double = 0.0,

    val shippingAmount: Double = 0.0,

    @SerializedName("grandTotal", alternate = ["totalAmount"])
    val totalAmount: Double = 0.0,

    @SerializedName("amountPaid", alternate = ["paidAmount"])
    val paidAmount: Double = 0.0,

    @SerializedName("balanceDue", alternate = ["balanceAmount"])
    val balanceAmount: Double = 0.0,

    val paymentMethod: String? = null,
    val status: String = "",
    val createdAt: String? = null,
    val updatedAt: String? = null,
    @SerializedName("__v")
    val version: Int? = null,
    val journal: JournalData? = null,

    @SerializedName("companySnapshot", alternate = ["organization"])
    val organization: CompanyDetails? = null,

    @SerializedName("shippingAddressSnapshot")
    val shippingAddressSnapshot: AddressSnapshot? = null,

    val bankDetails: BankDetails? = null,
    val termsAndConditions: String? = null
)

data class VoidInvoiceRequest(
    @SerializedName("reason")
    val reason: String
)

data class VoidInvoiceResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: InvoiceViewOneData? = null
)

// Line item payload structure matching Postman screenshot
data class CreateInvoiceLineItemRequest(
    @SerializedName("lineType")
    val lineType: String = "Product",
    @SerializedName("itemId")
    val itemId: String,
    @SerializedName("itemName")
    val itemName: String,
    @SerializedName("itemDescription")
    val itemDescription: String,
    @SerializedName("unit")
    val unit: String = "Pieces",
    @SerializedName("quantity")
    val quantity: Double,
    @SerializedName("rate")
    val rate: Double,
    @SerializedName("discountPercent")
    val discountPercent: Double = 0.0,
    @SerializedName("taxGroupId")
    val taxGroupId: String,
    @SerializedName("incomeAccountId")
    val incomeAccountId: String
)

// API response model
data class CreateInvoiceApiResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: InvoiceViewOneData? = null
)

// Response model for Send Invoice API
data class SendInvoiceApiResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: InvoiceViewOneData? = null
)