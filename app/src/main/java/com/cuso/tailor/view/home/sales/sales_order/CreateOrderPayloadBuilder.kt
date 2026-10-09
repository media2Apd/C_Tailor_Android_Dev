package com.cuso.tailor.view.home.sales.sales_order

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

// Line types used by the backend. Verify the fabric and accessory values with the API owner.
private const val LINE_TYPE_GARMENT = "Custom_Garment"
private const val LINE_TYPE_ADDON = "Garment_Addon"
private const val LINE_TYPE_FABRIC = "Garment_Material"
private const val LINE_TYPE_ACCESSORY = "Retail_Product"

data class CreateOrderBuildResult(
    val fields: Map<String, String>,
    val errors: List<String>
)

// ─────────────────────────────────────────────────────────────
// Small helpers
// ─────────────────────────────────────────────────────────────

/** Writes whole numbers without decimals (2000 instead of 2000.0). */
private fun JsonObject.addNumber(name: String, value: Double) {
    if (value % 1.0 == 0.0) addProperty(name, value.toLong()) else addProperty(name, value)
}

private fun numberText(value: Double): String {
    return if (value % 1.0 == 0.0) value.toLong().toString() else String.format(Locale.US, "%.2f", value)
}

/** "New Stitching" -> "New_Stitching" */
private fun toApiEnum(text: String): String = text.trim().replace(Regex("\\s+"), "_")

private val inputDateFormats = listOf("dd MMM yyyy", "dd/MM/yyyy", "dd-MM-yyyy", "yyyy-MM-dd")

private fun parseDateOrNull(text: String): Date? {
    if (text.isBlank()) return null
    for (pattern in inputDateFormats) {
        try {
            val format = SimpleDateFormat(pattern, Locale.ENGLISH)
            format.isLenient = false
            return format.parse(text.trim())
        } catch (e: Exception) {
            // try next pattern
        }
    }
    return null
}

/** Converts a screen date into the API format: 2026-10-08T00:00:00.000Z */
private fun toIsoDate(date: Date): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date) + "T00:00:00.000Z"
}

private fun measurementsJson(snapshot: MeasurementSnapshot?): JsonArray {
    val array = JsonArray()
    snapshot?.fields?.filter { it.value > 0f }?.forEach { field ->
        val entry = JsonObject().apply {
            addProperty("subLabel", field.unit)
            // toString() avoids float noise such as 12.100000381
            addNumber("value", field.value.toString().toDouble())
        }
        array.add(JsonObject().apply {
            addProperty("fieldId", field.fieldId)
            addProperty("fieldName", field.name.ifBlank { field.label })
            add("entries", JsonArray().apply { add(entry) })
            addProperty("fitAllowance", 0)
        })
    }
    return array
}

private fun addressJson(raw: String): JsonObject {
    return try {
        val element = JsonParser.parseString(raw)
        if (element.isJsonObject) element.asJsonObject else JsonObject()
    } catch (e: Exception) {
        JsonObject()
    }
}

// ─────────────────────────────────────────────────────────────
// Main builder
// ─────────────────────────────────────────────────────────────

/**
 * Builds the form-data fields for the create order API from the saved order flow.
 * IDs stay as IDs, text stays as text, items and customerSnapshot are JSON strings.
 * errors is not empty when a mandatory ID is missing, so the caller can block the request.
 */
fun buildCreateOrderFields(
    draft: OrderDraftSnapshot,
    measurements: Map<String, MeasurementSnapshot>,
    salespersonId: String,
    paymentTermId: String,
    paymentMode: String,
    advanceAmountPaid: Double,
    isFullPayment: Boolean,
    orderNotes: String
): CreateOrderBuildResult {
    val errors = mutableListOf<String>()
    val gson: Gson = GsonBuilder().disableHtmlEscaping().create()

    fun need(value: String, label: String) {
        if (value.isBlank()) errors += "$label is missing"
    }

    need(draft.branchId, "Branch")
    need(draft.customerId, "Customer")
    need(salespersonId, "Salesperson")
    need(paymentTermId, "Payment term")
    if (draft.items.isEmpty()) errors += "No items in the order"

    // ── Items ──
    val itemsArray = JsonArray()

    draft.items.forEachIndexed { index, entry ->
        val number = index + 1
        val line = draft.pricing.lines.getOrNull(index)

        when (entry) {
            is DynamicGarmentItem -> {
                val cfg = entry.config
                val tempId = "temp_${System.currentTimeMillis()}_${UUID.randomUUID().toString().replace("-", "").take(5)}"

                val categoryName = cfg.garmentCategory.takeIf { it.isNotBlank() && it != "Select Category" }
                    ?: cfg.garmentType.ifBlank { "Garment" }
                val stitchingTaxGroupId = draft.taxGroupIdByRate[cfg.stitchingGstRate].orEmpty()

                need(cfg.garmentId, "Item #$number garment")
                need(cfg.segmentId, "Item #$number segment")
                need(cfg.garmentCategoryId, "Item #$number category")
//                need(cfg.productionTemplateId, "Item #$number production template")
                need(stitchingTaxGroupId, "Item #$number tax group")

                val snapshot = measurements[entry.id] ?: measurements[cfg.garmentCategoryId]
                if (snapshot == null) errors += "Item #$number measurement is missing"

                val isStoreFabric = cfg.fabricSource.equals("Store Provided", ignoreCase = true)

                val customGarment = JsonObject().apply {
                    addProperty("segmentId", cfg.segmentId)
                    addProperty("garmentId", cfg.garmentId)
                    addProperty("garmentCategoryId", cfg.garmentCategoryId)
                    addProperty("designId", cfg.designId)
                    addProperty("stitchingType", cfg.stitchingType)
                    addProperty("fabricSource", if (isStoreFabric) "Store_Provided" else "Customer_Provided")
                    // For customer provided fabric the typed fabric details are the notes
                    addProperty("fabricNotes", cfg.fabricSelection)
                    addProperty("specialInstructions", cfg.specialInstructions)
                    add("measurements", measurementsJson(snapshot))
                }

                itemsArray.add(JsonObject().apply {
                    addProperty("_id", tempId)
                    addProperty("lineType", LINE_TYPE_GARMENT)
                    addProperty("productionTemplateId", cfg.productionTemplateId)
                    addProperty("itemDescription", "$categoryName Stitching")
                    addProperty("isTaxable", true)
                    addProperty("taxGroupId", stitchingTaxGroupId)
                    addProperty("sacCode", cfg.stitchingSacCode)
                    add("customGarment", customGarment)
                    addProperty("quantity", cfg.quantity)
                    addProperty("unit", "Pieces")
                    addNumber("unitPrice", cfg.stitchingPrice)
                    addNumber("discountAmount", 0.0)
                    addNumber("lineTotal", cfg.stitchingPrice * cfg.quantity)
                })

                // Store provided fabric becomes its own child line
//                if (isStoreFabric && cfg.fabricRate * cfg.fabricMeters > 0.0) {
//                    val fabricTaxGroupId = draft.taxGroupIdByRate[cfg.fabricGstRate].orEmpty()
//                    need(fabricTaxGroupId, "Item #$number fabric tax group")
//
//                    itemsArray.add(JsonObject().apply {
//                        addProperty("lineType", LINE_TYPE_FABRIC)
//                        addProperty("parentLineId", tempId)
//                        addProperty("itemDescription", cfg.fabricSelection)
//                        addProperty("isTaxable", true)
//                        addProperty("taxGroupId", fabricTaxGroupId)
//                        addNumber("quantity", cfg.fabricMeters)
//                        addProperty("unit", "Meter")
//                        addNumber("unitPrice", cfg.fabricRate)
//                        addNumber("discountAmount", 0.0)
//                        addNumber("lineTotal", cfg.fabricRate * cfg.fabricMeters)
//                    })
//                }

                // Additional work becomes addon child lines
                cfg.selectedWorkPricingIds.forEach { workId ->
                    val meta = draft.workMeta[workId] ?: return@forEach
                    val price = cfg.customWorkPrices[workId] ?: meta.basePrice
                    val rate = cfg.customWorkGstRates[workId] ?: 5.0
                    val workTaxGroupId = draft.taxGroupIdByRate[rate].orEmpty()
                    need(workTaxGroupId, "Item #$number ${meta.workType} tax group")

                    itemsArray.add(JsonObject().apply {
                        addProperty("lineType", LINE_TYPE_ADDON)
                        addProperty("parentLineId", tempId)
                        addProperty("itemDescription", meta.workType)
                        addProperty("isTaxable", true)
                        addProperty("taxGroupId", workTaxGroupId)
                        addProperty("sacCode", meta.sacCode)
                        add("addonWork", JsonObject().apply {
                            addProperty("workPricingId", workId)
                            addProperty("workType", meta.workType)
                            addProperty("specialInstructions", "")
                        })
                        addProperty("quantity", 1)
                        addProperty("unit", "Piece")
                        addNumber("unitPrice", price)
                        addNumber("discountAmount", 0.0)
                        addNumber("lineTotal", price)
                    })
                }
            }

            is DynamicFabricItem -> {
                val qty = line?.quantity ?: 1
                val lineSubtotal = line?.fabric ?: 0.0
                val taxGroupId = draft.taxGroupIdByRate[5.0].orEmpty()
                need(taxGroupId, "Item #$number tax group")

                itemsArray.add(JsonObject().apply {
                    addProperty("lineType", LINE_TYPE_FABRIC)
                    if (entry.itemId.isNotBlank()) {
                        addProperty("itemId", entry.itemId)
                    }
                    addProperty("itemDescription", entry.selection)
                    addProperty("isTaxable", true)
                    addProperty("taxGroupId", taxGroupId)
                    addProperty("quantity", qty)
                    addProperty("unit", entry.unit.ifBlank { "Meters (m)" })
                    addNumber("unitPrice", if (qty > 0) lineSubtotal / qty else 0.0)
                    addNumber("discountAmount", 0.0)
                    addNumber("lineTotal", lineSubtotal)

                    // Warehouse & Bin payload
                    if (entry.warehouseId.isNotBlank()) {
                        addProperty("warehouseId", entry.warehouseId)
                    }
                    if (entry.binId.isNotBlank()) {
                        addProperty("binId", entry.binId)
                    }
                })
            }

            is DynamicAccessoryItem -> {
                val qty = line?.quantity ?: 1
                val lineSubtotal = line?.stitching ?: 0.0
                val taxGroupId = draft.taxGroupIdByRate[5.0].orEmpty()
                need(taxGroupId, "Item #$number tax group")

                itemsArray.add(JsonObject().apply {
                    addProperty("lineType", LINE_TYPE_ACCESSORY)
                    if (entry.itemId.isNotBlank()) {
                        addProperty("itemId", entry.itemId)
                    }
                    addProperty("itemDescription", entry.selection)
                    addProperty("isTaxable", true)
                    addProperty("taxGroupId", taxGroupId)
                    addProperty("quantity", qty)
                    addProperty("unit", entry.unit.ifBlank { "Pieces (Pcs)" })
                    addNumber("unitPrice", if (qty > 0) lineSubtotal / qty else 0.0)
                    addNumber("discountAmount", 0.0)
                    addNumber("lineTotal", lineSubtotal)

                    // Warehouse & Bin payload
                    if (entry.warehouseId.isNotBlank()) {
                        addProperty("warehouseId", entry.warehouseId)
                    }
                    if (entry.binId.isNotBlank()) {
                        addProperty("binId", entry.binId)
                    }
                })
            }
        }
    }

    // ── Customer snapshot ──
    val phoneDigits = draft.phone.filter { it.isDigit() }
    val codeDigits = draft.countryCode.filter { it.isDigit() }
    val fullPhone = if (phoneDigits.length > 10 || codeDigits.isBlank()) phoneDigits else codeDigits + phoneDigits

    val billingAddress = addressJson(draft.customerAddressJson)
    val customerSnapshot = JsonObject().apply {
        addProperty("customerCode", draft.customerCode)
        addProperty("name", draft.customerName)
        addProperty("phone", fullPhone)
        addProperty("email", draft.email)
        addProperty("customerType", draft.customerType)
        add("billingAddress", billingAddress)
        add("shippingAddress", billingAddress.deepCopy())
    }

    // ── Dates ──
    val orderDate = parseDateOrNull(draft.orderDate) ?: Date()
    val itemDates = draft.items.mapNotNull { entry ->
        when (entry) {
            is DynamicGarmentItem -> parseDateOrNull(entry.config.deliveryDate)
            is DynamicFabricItem -> parseDateOrNull(entry.deliveryDate)
            is DynamicAccessoryItem -> parseDateOrNull(entry.deliveryDate)
        }
    } + listOfNotNull(parseDateOrNull(draft.expectedDeliveryDate))
    // Due date is the latest delivery date of all items
    val dueDate = itemDates.maxOrNull() ?: orderDate

    // ── Final form fields (same order as the API sample) ──
    val advanceRequired = draft.advanceAmount.toDoubleOrNull() ?: 0.0
    // The screen default "-" means nothing was chosen, so fall back to Advance
    val rawPaymentType = draft.paymentType.takeIf { it.isNotBlank() && it != "-" } ?: "Advance"
    val paymentTypeValue = if (isFullPayment) "Full_Payment" else toApiEnum(rawPaymentType)

    val fields = linkedMapOf<String, String>()
    fields["branchId"] = draft.branchId
    fields["customerId"] = draft.customerId
    fields["paymentTermId"] = paymentTermId
    fields["salespersonId"] = salespersonId
    fields["orderDate"] = toIsoDate(orderDate)
    fields["dueDate"] = toIsoDate(dueDate)
    fields["orderType"] = toApiEnum(draft.orderType.ifBlank { "New Stitching" })
    fields["priority"] = "Medium"
    fields["orderNotes"] = orderNotes
    fields["paymentMode"] = paymentMode
    fields["paymentType"] = paymentTypeValue
    fields["advanceAmountRequired"] = numberText(advanceRequired)
    fields["advanceAmountPaid"] = numberText(advanceAmountPaid)
    fields["deliveryMethod"] = "Store_Pickup"
    fields["deliveryCharge"] = "0"
    fields["totalDiscount"] = numberText(draft.pricing.discount)
    fields["billingNotes"] = ""
    fields["financeClearanceRequired"] = "true"
    fields["currency"] = "INR"
    fields["status"] = "Pending_Approval"
    fields["customerSnapshot"] = gson.toJson(customerSnapshot)
    fields["items"] = gson.toJson(itemsArray)
    fields["subtotal"] = numberText(draft.pricing.subtotal)
    fields["grandTotal"] = numberText(draft.pricing.grandTotal)

    return CreateOrderBuildResult(fields = fields, errors = errors)
}

/** Converts the form fields into Retrofit @PartMap text parts. */
fun Map<String, String>.toTextParts(): Map<String, RequestBody> {
    val mediaType = "text/plain".toMediaTypeOrNull()
    return mapValues { it.value.toRequestBody(mediaType) }
}