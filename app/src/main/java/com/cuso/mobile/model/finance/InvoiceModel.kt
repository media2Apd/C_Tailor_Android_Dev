package com.cuso.mobile.model.finance

import com.cuso.mobile.model.sales.PaginationInfo
import com.google.gson.annotations.SerializedName

// ─────────────────────────────────────────────────────────────
// Invoice — View All API Response
// GET /api/finance/invoices/view-all
// ─────────────────────────────────────────────────────────────

data class InvoiceListResponse(
    val success: Boolean,
    val data: InvoiceListData
)

data class InvoiceListData(
    val data: List<InvoiceItem>,
    val pagination: PaginationInfo? = null
)

data class InvoiceItem(
    @SerializedName("_id")
    val id: String,
    val organizationId: String,
    val branchId: String? = null,
    val invoiceNumber: String,
    val salesOrderId: String? = null,
    @com.google.gson.annotations.JsonAdapter(InvoiceCustomerRefDeserializer::class)
    val customerId: InvoiceCustomerRef? = null,   // ← was String?
    val customer: CustomerInfo? = null,
    val invoiceDate: String,
    val dueDate: String? = null,
    val items: List<InvoiceLineItem> = emptyList(),
    val subtotal: Double = 0.0,
    val taxAmount: Double = 0.0,
    val totalAmount: Double = 0.0,
    val paidAmount: Double = 0.0,
    val balanceAmount: Double = 0.0,
    val status: String,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    @SerializedName("__v")
    val version: Int? = null
) {
    val displayCustomerName: String
        get() = customer?.name ?: customerId?.name ?: customerId?.id ?: "Walk-in Customer"
}
data class InvoiceCustomerRef(
    @SerializedName("_id") val id: String? = null,
    val name: String? = null,
    val mobile: String? = null,
    val email: String? = null
)

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
                    id = obj.get("_id")?.asString,
                    name = obj.get("name")?.asString,
                    mobile = obj.get("mobile")?.asString,
                    email = obj.get("email")?.asString
                )
            }
            json.isJsonPrimitive -> InvoiceCustomerRef(id = json.asString)
            else -> null
        }
    }
}
data class InvoiceLineItem(
    @SerializedName("_id")
    val id: String? = null,
    val description: String,
    val hsnSku: String? = null,       // 🆕
    val quantity: Int,
    val unitPrice: Double,
    val discount: String? = null,     // 🆕
    val tax: Double = 0.0,
    val total: Double
)



// ─────────────────────────────────────────────────────────────
// UI State
// ─────────────────────────────────────────────────────────────

sealed class InvoiceUiState {
    object Loading : InvoiceUiState()
    data class Success(val invoices: List<InvoiceItem>, val pagination: PaginationInfo?) : InvoiceUiState()
    data class Error(val message: String) : InvoiceUiState()
}

// ─────────────────────────────────────────────────────────────
// Invoice View One Response
// GET /api/finance/invoices/{id}
// ─────────────────────────────────────────────────────────────

data class InvoiceViewOneResponse(
    val success: Boolean,
    val data: InvoiceViewOneData
)

data class InvoiceViewOneData(
    @SerializedName("_id")
    val id: String,
    val organizationId: String,
    val branchId: String,
    val invoiceNumber: String,
    val salesOrderId: String? = null,
    val customerId: InvoiceCustomerRef? = null,   // ← was String?
    val customer: CustomerInfo? = null,           // 🆕
    val invoiceDate: String,
    val dueDate: String? = null,
    val items: List<InvoiceItemDetail>,
    val subtotal: Double,
    val taxAmount: Double,
    val discountAmount: Double = 0.0,             // 🆕
    val shippingAmount: Double = 0.0,              // 🆕
    val totalAmount: Double,
    val paidAmount: Double,
    val balanceAmount: Double,
    val paymentMethod: String? = null,             // 🆕
    val status: String,
    val createdAt: String,
    val updatedAt: String,
    @SerializedName("__v")
    val version: Int? = null,
    val journal: JournalData? = null,
    val organization: CompanyDetails? = null,      // 🆕 company/branch info from backend
    val bankDetails: BankDetails? = null,          // 🆕
    val termsAndConditions: String? = null         // 🆕
)

data class InvoiceItemDetail(
    val description: String,
    val hsnSku: String? = null,        // 🆕
    val quantity: Int,
    val unitPrice: Double,
    val discount: String? = null,      // 🆕
    val tax: Double,
    val total: Double,
    @SerializedName("_id")
    val id: String
)

// ─────────────────────────────────────────────────────────────
// 🆕 Supporting models — adjust field names to match real backend keys
// once the API team confirms the exact response shape.
// ─────────────────────────────────────────────────────────────

data class CustomerInfo(
    @SerializedName("_id") val id: String? = null,
    val name: String? = null,
    val billingAddress: String? = null,
    val shippingAddress: String? = null,
    val phone: String? = null,
    val email: String? = null
)

data class CompanyDetails(
    val name: String? = null,
    val address: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val gstNumber: String? = null,
    val logoUrl: String? = null        // used for the WebView invoice logo
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
    val lines: List<JournalLine>,
    val status: String,
    val createdBy: String,
    val createdAt: String,
    val updatedAt: String,
    @SerializedName("__v")
    val version: Int? = null
)



//data class AccountInfo(
//    @SerializedName("_id")
//    val id: String,
//    val accountName: String,
//    val accountCode: String
//)