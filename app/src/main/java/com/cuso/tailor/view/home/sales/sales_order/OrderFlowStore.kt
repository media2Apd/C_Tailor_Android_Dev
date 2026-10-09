package com.cuso.tailor.view.home.sales.sales_order

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.Locale

// ─────────────────────────────────────────────────────────────
// Snapshot models shared across the whole order flow
// (Create Order -> Measurement Entry -> Order Preview)
// ─────────────────────────────────────────────────────────────

/** One priced line shown in the preview (garment, fabric or accessory). */
data class OrderPricingLine(
    val itemNumber: Int,
    val type: String,
    val title: String,
    val quantity: Int,
    val stitching: Double,
    val fabric: Double,
    val additionalWork: Double,
    val gst: Double,
    val total: Double,
    val details: List<Pair<String, String>>
)

data class OrderPricingResult(
    val lines: List<OrderPricingLine> = emptyList(),
    val subtotal: Double = 0.0,
    val discount: Double = 0.0,
    val tax: Double = 0.0,
    val grandTotal: Double = 0.0
)

/** Static info of an additional work type, needed for the addon lines of the payload. */
data class WorkMeta(
    val workType: String,
    val sacCode: String,
    val basePrice: Double
)

/** Everything entered in the Create Order screen. */
data class OrderDraftSnapshot(
    val customerId: String,
    val customerCode: String,
    val customerName: String,
    val phone: String,
    val countryCode: String,
    val email: String,
    val customerType: String,
    val customerAddressJson: String,
    val branchId: String,
    val branchName: String,
    val salespersonId: String = "",
    val salespersonName: String = "",
    val address: String,
    val orderId: String,
    val orderDate: String,
    val orderType: String,
    val expectedDeliveryDate: String,
    val paymentType: String,
    val advanceAmount: String,
    val items: List<OrderItemEntry>,
    val pricing: OrderPricingResult,
    // tax group id lookup: total GST rate -> tax group id
    val taxGroupIdByRate: Map<Double, String>,
    // work pricing id -> work info
    val workMeta: Map<String, WorkMeta>
)
/** One measurement field with its entered value (label already resolved from the API). */
data class MeasurementFieldValue(
    val fieldId: String,
    val name: String,
    val label: String,
    val value: Float,
    val unit: String,
    val group: String
)

/** Everything entered in the Measurement Entry screen for one garment. */
data class MeasurementSnapshot(
    val key: String,
    val garmentType: String,
    val garmentCategory: String,
    val profileName: String,
    val linkedPreviousMeasurement: String,
    val measurementDate: String,
    val takenBy: String,
    val unit: String,
    val fitType: String,
    val specialInstructions: String,
    val fields: List<MeasurementFieldValue>
) {
    val fieldValues: Map<String, Float>
        get() = fields.associate { it.fieldId to it.value }
}

/**
 * In-memory holder for the current order flow.
 * It lives as long as the app process, so data survives back and forward
 * navigation between the three screens. Call clear() when a new order
 * starts, is discarded, or has been submitted successfully.
 */
data class PreviewPaymentState(
    val collectedBy: String = "Store Associate A",
    val isFullAdvance: Boolean = false,
    val paymentAmountReceived: String = "",
    val orderNotes: String = ""
)

object OrderFlowStore {
    var draft: OrderDraftSnapshot? by mutableStateOf(null)
    val measurements = mutableStateMapOf<String, MeasurementSnapshot>()
    var previewPayment: PreviewPaymentState? by mutableStateOf(null)

    fun clear() {
        draft = null
        measurements.clear()
        previewPayment = null
    }
}

// ─────────────────────────────────────────────────────────────
// Pure helpers
// ─────────────────────────────────────────────────────────────

fun formatMeasurementValue(value: Float): String {
    return if (value % 1f == 0f) {
        value.toInt().toString()
    } else {
        String.format(Locale.US, "%.2f", value).trimEnd('0').trimEnd('.')
    }
}

/**
 * Computes line level and order level pricing from the dynamic items.
 * The formulas are the same as the Pricing & Charges section.
 *
 * workInfo    : work pricing id -> (work type name, base price)
 * fabricPrices: fabric name -> selling price
 */
fun computeOrderPricing(
    items: List<OrderItemEntry>,
    workInfo: Map<String, Pair<String, Double>>,
    fabricPrices: Map<String, Double>,
    discount: Double
): OrderPricingResult {
    val lines = mutableListOf<OrderPricingLine>()
    var subtotal = 0.0
    var tax = 0.0

    items.forEachIndexed { index, entry ->
        val number = index + 1

        when (entry) {
            is DynamicGarmentItem -> {
                val cfg = entry.config

                val stitchingSubtotal = cfg.stitchingPrice * cfg.quantity
                val stitchingGst = stitchingSubtotal * (cfg.stitchingGstRate / 100.0)

                val isStoreFabric = cfg.fabricSource.equals("Store Provided", ignoreCase = true)
                val fabricSubtotal = if (isStoreFabric) cfg.fabricRate * cfg.fabricMeters else 0.0
                val fabricGst = fabricSubtotal * (cfg.fabricGstRate / 100.0)

                val selectedWorks = cfg.selectedWorkPricingIds.mapNotNull { id ->
                    workInfo[id]?.let { info -> id to info }
                }
                val workTotal = selectedWorks.sumOf { (id, info) ->
                    cfg.customWorkPrices[id] ?: info.second
                }
                val workGst = selectedWorks.sumOf { (id, info) ->
                    val price = cfg.customWorkPrices[id] ?: info.second
                    val rate = cfg.customWorkGstRates[id] ?: 5.0
                    price * (rate / 100.0)
                }

                val before = stitchingSubtotal + fabricSubtotal + workTotal
                val gst = stitchingGst + fabricGst + workGst

                subtotal += before
                tax += gst

                val category = cfg.garmentCategory.takeIf { it.isNotBlank() && it != "Select Category" }
                val title = category ?: cfg.garmentType.ifBlank { "Garment" }

                val details = mutableListOf<Pair<String, String>>()
                if (cfg.garmentType.isNotBlank()) details += "Garment Type" to cfg.garmentType
                if (category != null) details += "Category" to category
                if (cfg.fabricSource.isNotBlank() && cfg.fabricSource != "-") {
                    details += "Fabric Source" to cfg.fabricSource
                }
                if (cfg.fabricSelection.isNotBlank()) {
                    val meters = if (isStoreFabric) " (${formatMeasurementValue(cfg.fabricMeters.toFloat())} m)" else ""
                    details += "Fabric" to (cfg.fabricSelection + meters)
                }
                if (cfg.stitchingType.isNotBlank()) details += "Stitching Type" to cfg.stitchingType
                if (cfg.designPreset.isNotBlank()) details += "Design Preset" to cfg.designPreset
                if (cfg.deliveryDate.isNotBlank()) details += "Delivery Date" to cfg.deliveryDate
                if (selectedWorks.isNotEmpty()) {
                    details += "Additional Work" to selectedWorks.joinToString(", ") { it.second.first }
                }
                if (cfg.specialInstructions.isNotBlank()) details += "Instructions" to cfg.specialInstructions

                lines += OrderPricingLine(
                    itemNumber = number,
                    type = "Garment",
                    title = title,
                    quantity = cfg.quantity,
                    stitching = stitchingSubtotal,
                    fabric = fabricSubtotal,
                    additionalWork = workTotal,
                    gst = gst,
                    total = before + gst,
                    details = details
                )
            }

            is DynamicFabricItem -> {
                val unitPrice = fabricPrices[entry.selection] ?: 0.0
                val qty = entry.quantity.toDoubleOrNull() ?: 1.0
                val lineSubtotal = unitPrice * qty
                val gst = lineSubtotal * 0.05

                subtotal += lineSubtotal
                tax += gst

                val details = mutableListOf<Pair<String, String>>()
                if (entry.unit.isNotBlank()) details += "Unit" to entry.unit
                if (entry.deliveryDate.isNotBlank()) details += "Delivery Date" to entry.deliveryDate
                if (entry.instructions.isNotBlank()) details += "Instructions" to entry.instructions

                lines += OrderPricingLine(
                    itemNumber = number,
                    type = "Fabric",
                    title = entry.selection.ifBlank { "Fabric Material" },
                    quantity = qty.toInt().coerceAtLeast(1),
                    stitching = 0.0,
                    fabric = lineSubtotal,
                    additionalWork = 0.0,
                    gst = gst,
                    total = lineSubtotal + gst,
                    details = details
                )
            }

            is DynamicAccessoryItem -> {
                val unitPrice = 150.0
                val qty = entry.quantity.toIntOrNull() ?: 1
                val lineSubtotal = unitPrice * qty
                val gst = lineSubtotal * 0.05

                subtotal += lineSubtotal
                tax += gst

                val details = mutableListOf<Pair<String, String>>()
                if (entry.unit.isNotBlank()) details += "Unit" to entry.unit
                if (entry.deliveryDate.isNotBlank()) details += "Delivery Date" to entry.deliveryDate
                if (entry.instructions.isNotBlank()) details += "Instructions" to entry.instructions

                lines += OrderPricingLine(
                    itemNumber = number,
                    type = "Accessory",
                    title = entry.selection.ifBlank { "Accessory Item" },
                    quantity = qty,
                    stitching = lineSubtotal,
                    fabric = 0.0,
                    additionalWork = 0.0,
                    gst = gst,
                    total = lineSubtotal + gst,
                    details = details
                )
            }
        }
    }

    return OrderPricingResult(
        lines = lines,
        subtotal = subtotal,
        discount = discount,
        tax = tax,
        grandTotal = (subtotal - discount + tax).coerceAtLeast(0.0)
    )
}