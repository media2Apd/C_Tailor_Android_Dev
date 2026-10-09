@file:Suppress(
    "UNUSED_VALUE",
    "SpellCheckingInspection",
    "GrazieInspection",
    "AssignedValueIsNeverRead",
    "unused_variable",
    "unused_parameter",
    "UnusedMaterial3ScaffoldPaddingParameter",
    "VariableNeverRead"
)

package com.cuso.tailor.view.home.sales.sales_order

import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.database.entities.SelectedGarment
import com.cuso.tailor.model.inventory.TaxGroupDto
import com.cuso.tailor.model.sales.OrderOverviewItem
import com.cuso.tailor.model.sales.OrderReviewData
import com.cuso.tailor.model.settings.DesignItem
import com.cuso.tailor.model.settings.GarmentItem
import com.cuso.tailor.model.settings.GarmentStyleItem
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.*
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

private val LocalTealBadgeColor = Color(0xFF0D9488)

data class AdditionalCraftItem(
    val id: String = UUID.randomUUID().toString(),
    var title: String = "",
    var metricUnitLabel: String = "",
    var metricValue: String = "",
    var amount: Double = 0.0,
    var gstRateLabel: String = ""
) {
    val gstAmount: Double get() = amount * 0.05
    val totalWithGst: Double get() = amount + gstAmount
}

sealed interface OrderItemEntry {
    val id: String
    var isItemExpanded: Boolean
}

data class DynamicGarmentItem(
    override val id: String = UUID.randomUUID().toString(),
    override var isItemExpanded: Boolean = true,
    var config: ConfiguredOrderItem = ConfiguredOrderItem(),
    var referenceImageUri: Uri? = null,
    var sketchUri: Uri? = null
) : OrderItemEntry

data class DynamicFabricItem(
    override val id: String = UUID.randomUUID().toString(),
    override var isItemExpanded: Boolean = false,
    var itemId: String = "",
    var selection: String = "",
    var quantity: String = "1",
    var unit: String = "Meters (m)",
    var warehouseId: String = "",
    var warehouseName: String = "",
    var binId: String = "",
    var binName: String = "",
    var deliveryDate: String = "",
    var instructions: String = "",
    var stockCount: Double = 0.0,
    var isOutOfStock: Boolean = false
) : OrderItemEntry

data class DynamicAccessoryItem(
    override val id: String = UUID.randomUUID().toString(),
    override var isItemExpanded: Boolean = false,
    var itemId: String = "",
    var selection: String = "",
    var quantity: String = "1",
    var unit: String = "Pieces (Pcs)",
    var warehouseId: String = "",
    var warehouseName: String = "",
    var binId: String = "",
    var binName: String = "",
    var deliveryDate: String = "",
    var instructions: String = "",
    var stockCount: Double = 0.0,
    var isOutOfStock: Boolean = false
) : OrderItemEntry

data class ConfiguredOrderItem(
    val id: String = UUID.randomUUID().toString(),
    var garmentCategory: String = "",
    var garmentType: String = "",
    var quantity: Int = 1,
    var productionTemplateName: String = "",
    var fabricSource: String = "Store Provided",
    var fabricSelection: String = "",
    var designPreset: String = "",
    var colorAccent: String = "",
    var sizeStandard: String = "",
    var stitchingType: String = "",
    var assignedTailor: String = "",
    var deliveryDate: String = "",
    var specialInstructions: String = "",
    var segmentId: String = "",
    var garmentId: String = "",
    var garmentCategoryId: String = "",
    var designId: String = "",
    var productionTemplateId: String = "",
    var stitchingSacCode: String = "",
    var isCustomizationEnabled: Boolean = true,
    var selectedWorkPricingIds: List<String> = emptyList(),
    var selectedTechnique: String = "",
    var sketchFileName: String = "",
    var selectedPlacements: List<String> = listOf("Front", "Back", "Neckline", "Sleeves"),
    var selectedMaterials: List<String> = listOf("Silk Thread", "Zari", "Stones", "Pearls"),
    var primaryColor: String = "Antique Gold",
    var secondaryColor: String = "Crimson Maroon",
    var coverage: String = "Medium",
    var workTarget: String = "Custom Area",
    var areaDescription: String = "Neckline floral motif and 2.5-inch sleeve borders",
    var estArea: String = "120 sq. in",
    var borderLength: String = "2.2 meters",
    var countDetail: String = "65 pearls",
    var customizationInstructions: String = "",
    var isDesignPreviewApproved: Boolean = true,
    var productionInstructions: String = "",

    var stitchingPrice: Double = 0.0,
    var stitchingGstRate: Double = 5.0,
    var fabricRate: Double = 0.0,
    var fabricMeters: Double = 2.5,
    var fabricGstRate: Double = 5.0,
    var customWorkPrices: Map<String, Double> = emptyMap(),
    var customWorkGstRates: Map<String, Double> = emptyMap(),
    var addlWorkPrice: Double = 0.0,
    var gstPrice: Double = 0.0,
    var discountPrice: Double = 0.0,
    var trialRequired: Boolean = false
) {
    val effectiveFabricPrice: Double
        get() = if (fabricSource.equals("Customer Provided", ignoreCase = true)) 0.0 else fabricRate * fabricMeters

    val totalItemPrice: Double
        get() = ((stitchingPrice + effectiveFabricPrice + addlWorkPrice - discountPrice + gstPrice) * quantity).coerceAtLeast(0.0)
}

private fun validateRequiredOrderFields(
    fullName: String,
    customerType: String,
    selectedBranchId: String,
    orderDate: String,
    orderType: String,
    salespersonId: String,
    salespersonName: String,
    dynamicOrderItems: List<OrderItemEntry>,
    paymentType: String,
    advanceAmount: String
): String? {
    if (fullName.trim().isBlank()) return "Please select or enter customer name"
    if (customerType.trim().isBlank()) return "Please select customer type"
    if (selectedBranchId.trim().isBlank()) return "Please select branch"

    if (orderDate.trim().isBlank()) return "Please select order date"
    if (orderType.trim().isBlank()) return "Please select order type"
    if (salespersonId.trim().isBlank() || salespersonName == "Select Salesperson") return "Please select a salesperson"

    if (dynamicOrderItems.isEmpty()) return "Please add at least one item to the order"

    dynamicOrderItems.forEachIndexed { index, item ->
        val itemNum = index + 1
        when (item) {
            is DynamicGarmentItem -> {
                val cfg = item.config
                if (cfg.garmentType.isBlank() || cfg.garmentType == "Select Garment Type") {
                    return "Please select Garment Type for Item #$itemNum"
                }
                if (cfg.garmentCategory.isBlank() || cfg.garmentCategory == "Select Category") {
                    return "Please select Category for Item #$itemNum"
                }
                if (cfg.quantity <= 0) {
                    return "Please enter a valid Quantity for Item #$itemNum"
                }
                if (cfg.fabricSource.isBlank() || cfg.fabricSource == "-") {
                    return "Please select Fabric Source for Item #$itemNum"
                }
                if (cfg.fabricSelection.isBlank()) {
                    return if (cfg.fabricSource.equals("Customer Provided", ignoreCase = true)) {
                        "Please enter customer fabric details for Item #$itemNum"
                    } else {
                        "Please select store fabric for Item #$itemNum"
                    }
                }
                if (cfg.productionTemplateId.isBlank()) {
                    return "Please select Production Workflow Template for Item #$itemNum"
                }
                if (cfg.deliveryDate.isBlank()) {
                    return "Please select Item Delivery Date for Item #$itemNum"
                }
            }
            is DynamicFabricItem -> {
                if (item.selection.isBlank() || item.selection == "Select Fabric") {
                    return "Please select Fabric for Item #$itemNum"
                }
                if (item.quantity.trim().isBlank() || (item.quantity.toDoubleOrNull() ?: 0.0) <= 0.0) {
                    return "Please enter a valid Quantity for Item #$itemNum"
                }
                if (item.unit.isBlank()) {
                    return "Please select Unit for Item #$itemNum"
                }
                if (item.warehouseId.isBlank()) {
                    return "Please pick Warehouse for Item #$itemNum"
                }
                if (item.binId.isBlank()) {
                    return "Please pick Bin for Item #$itemNum"
                }
            }
            is DynamicAccessoryItem -> {
                if (item.selection.isBlank() || item.selection == "Select Accessory") {
                    return "Please select Item Selection for Item #$itemNum"
                }
                if (item.quantity.trim().isBlank() || (item.quantity.toDoubleOrNull() ?: 0.0) <= 0.0) {
                    return "Please enter a valid Quantity for Item #$itemNum"
                }
                if (item.unit.isBlank()) {
                    return "Please select Unit for Item #$itemNum"
                }
                if (item.warehouseId.isBlank()) {
                    return "Please pick Warehouse for Item #$itemNum"
                }
                if (item.binId.isBlank()) {
                    return "Please pick Bin for Item #$itemNum"
                }
            }
        }
    }

    if (paymentType.isBlank() || paymentType == "-") return "Please select payment type"
    if (paymentType == "Advance") {
        if (advanceAmount.trim().isBlank() || advanceAmount == "-") return "Please enter advance amount required"
    }

    return null
}

private fun formatIsoDateForDisplay(isoString: String?): String {
    if (isoString.isNullOrBlank()) return ""
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        val date = parser.parse(isoString)
        if (date != null) {
            SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(date)
        } else isoString
    } catch (_: Exception) {
        try {
            val parserShort = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = parserShort.parse(isoString)
            if (date != null) SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(date) else isoString
        } catch (_: Exception) {
            isoString
        }
    }
}

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun CreateOrderScreen(
    initialData: OrderReviewData? = null,
    onBack: () -> Unit = {},
    onCancel: () -> Unit = {},
    onAddNewCustomer: () -> Unit = {},
    orderOverviewViewModel: OrderOverviewViewModel = hiltViewModel(),
    overviewViewModel: SalesOrderViewModel = hiltViewModel(),
    orderViewViewModel: OrderViewViewModel = hiltViewModel(),
    onNextStep: (OrderReviewData) -> Unit = {},
    salesViewModel: SalesViewModel = hiltViewModel(),
    branchViewModel: BranchViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    customerViewModel: CustomerViewModel = hiltViewModel(),
    inventoryViewModel: InventoryViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val scrollState = rememberScrollState()
    val isEditMode = !initialData?.orderId.isNullOrBlank()

    // Saved draft fallback
    val savedDraft = remember {
        if (initialData == null) OrderFlowStore.clear()
        OrderFlowStore.draft
    }

    val todayFormatted = remember {
        SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(Date())
    }

    // State Variables
    var orderIdText by rememberSaveable { mutableStateOf(initialData?.orderId ?: savedDraft?.orderId ?: "") }
    var orderDate by rememberSaveable {
        mutableStateOf(initialData?.orderDate.orEmpty().ifBlank { savedDraft?.orderDate ?: todayFormatted })
    }
    var expectedDeliveryDate by rememberSaveable {
        mutableStateOf(initialData?.deliveryDate.orEmpty().ifBlank { savedDraft?.expectedDeliveryDate ?: "" })
    }
    var customerId by rememberSaveable { mutableStateOf(initialData?.customerId?.takeIf { it.isNotBlank() } ?: savedDraft?.customerId ?: "") }
    var phone by rememberSaveable { mutableStateOf(initialData?.phone?.takeIf { it.isNotBlank() } ?: savedDraft?.phone ?: "") }
    var fullName by rememberSaveable { mutableStateOf(initialData?.fullName?.takeIf { it.isNotBlank() } ?: savedDraft?.customerName ?: "") }
    var customerType by rememberSaveable {
        mutableStateOf(initialData?.dressFor.orEmpty().ifBlank { savedDraft?.customerType ?: "Individual" })
    }
    var selectedBranchId by rememberSaveable {
        mutableStateOf(initialData?.branchId?.takeIf { it.isNotBlank() } ?: savedDraft?.branchId ?: "")
    }
    var address by rememberSaveable { mutableStateOf(initialData?.address?.takeIf { it.isNotBlank() } ?: savedDraft?.address ?: "") }
    var emailAddress by rememberSaveable { mutableStateOf(savedDraft?.email ?: "") }
    var customerCode by rememberSaveable { mutableStateOf(savedDraft?.customerCode ?: "") }
    var customerAddressJson by rememberSaveable { mutableStateOf(savedDraft?.customerAddressJson ?: "") }
    var selectedBranchName by rememberSaveable { mutableStateOf(savedDraft?.branchName ?: "") }
    var salespersonId by rememberSaveable { mutableStateOf(savedDraft?.salespersonId ?: "") }
    var salespersonName by rememberSaveable { mutableStateOf(savedDraft?.salespersonName ?: "") }
    var gender by rememberSaveable { mutableStateOf(initialData?.gender ?: "") }
    var dressFor by rememberSaveable { mutableStateOf(initialData?.dressFor ?: "") }
    var source by rememberSaveable { mutableStateOf(initialData?.source ?: "Direct") }
    var countryCode by rememberSaveable { mutableStateOf(initialData?.countryCode ?: "+91") }
    var orderType by rememberSaveable { mutableStateOf(savedDraft?.orderType ?: "New Stitching") }
    val priority by rememberSaveable { mutableStateOf("Medium") }

    var dynamicOrderItems by remember {
        mutableStateOf(
            if (savedDraft != null && savedDraft.items.isNotEmpty()) {
                savedDraft.items
            } else if (!initialData?.garments.isNullOrEmpty()) {
                initialData.garments.map { g ->
                    DynamicGarmentItem(
                        isItemExpanded = true,
                        config = ConfiguredOrderItem(
                            garmentType = g.categoryName.ifBlank { g.category },
                            garmentCategory = g.categoryName.ifBlank { g.category },
                            quantity = g.quantity,
                            stitchingPrice = g.price,
                            fabricSource = g.fabricSource.ifBlank { "Customer Provided" },
                            fabricSelection = g.fabricType,
                            colorAccent = g.colorTone,
                            designPreset = g.pattern,
                            trialRequired = g.trialRequired
                        )
                    )
                }
            } else {
                listOf<OrderItemEntry>(
                    DynamicGarmentItem(
                        isItemExpanded = true,
                        config = ConfiguredOrderItem(
                            fabricSource = "-",
                            isCustomizationEnabled = true,
                            selectedWorkPricingIds = emptyList()
                        )
                    )
                )
            }
        )
    }

    var discountText by rememberSaveable {
        mutableStateOf(
            (savedDraft?.pricing?.discount ?: initialData?.discount ?: 0.0)
                .takeIf { it > 0.0 }
                ?.let { if (it % 1.0 == 0.0) it.toLong().toString() else it.toString() }
                ?: ""
        )
    }
    var paymentType by rememberSaveable { mutableStateOf(savedDraft?.paymentType ?: "-") }
    var advanceAmount by rememberSaveable {
        mutableStateOf(
            if ((initialData?.paidSoFar ?: 0.0) > 0.0) {
                initialData!!.paidSoFar.toInt().toString()
            } else {
                savedDraft?.advanceAmount ?: "-"
            }
        )
    }

    var isPrefilled by rememberSaveable { mutableStateOf(savedDraft != null) }
    var existingImageUrls by remember { mutableStateOf(initialData?.existingImageUrls ?: emptyList()) }

    // ── NEW: collect the overview state ──
    val orderOverviewState by orderOverviewViewModel.overviewState.collectAsStateWithLifecycle()

    // Trigger the API call once on edit mode
    LaunchedEffect(initialData?.orderId) {
        val targetOrderId = initialData?.orderId
        if (isEditMode && !isPrefilled && targetOrderId.isNotBlank()) {
            orderOverviewViewModel.fetchSalesOverview(targetOrderId)
        }
    }
//
//    LaunchedEffect(orderViewState) {
//        val state = orderViewState
//    }

    // Prefill form when data arrives

    LaunchedEffect(orderOverviewState) {
        val state = orderOverviewState

        if (!isEditMode || isPrefilled || state !is OrderOverviewState.Success) {
            return@LaunchedEffect
        }

        val data = state.data

        /* ───────── 1. ORDER INFO ───────── */

        orderIdText = data.orderCode
            ?.takeIf { it.isNotBlank() }
            ?: data.orderNumberLegacy
                ?.takeIf { it.isNotBlank() }
                    ?: orderIdText

        orderDate = formatIsoDateForDisplay(data.orderDate)
            .ifBlank { orderDate }

        expectedDeliveryDate = formatIsoDateForDisplay(data.dueDate)

        orderType = data.orderType
            ?.takeIf { it.isNotBlank() }
            ?: orderType


        /* ───────── 2. CUSTOMER DETAILS ───────── */

        val snap = data.customerSnapshot
        val cust = data.customerId

        fullName = snap?.name
            ?.takeIf { it.isNotBlank() }
            ?: cust?.fullName
                ?.takeIf { it.isNotBlank() }
                    ?: fullName

        phone = snap?.phone
            ?.takeIf { it.isNotBlank() }
            ?: cust?.mobileNumber
                ?.takeIf { it.isNotBlank() }
                    ?: phone

        emailAddress = snap?.email
            ?.takeIf { it.isNotBlank() }
            ?: cust?.email
                ?.takeIf { it.isNotBlank() }
                    ?: emailAddress

        customerCode = snap?.customerCode
            ?.takeIf { it.isNotBlank() }
            ?: cust?.customerCode
                ?.takeIf { it.isNotBlank() }
                    ?: customerCode

        customerId = cust?._id ?: customerId

        val addr = snap?.billingAddress ?: cust?.billingAddress

        if (addr != null) {
            address = listOfNotNull(
                addr.flatNo,
                addr.street,
                addr.areaZone,
                addr.city,
                addr.subdivisionName,
                addr.pincode
            )
                .filter { it.isNotBlank() }
                .joinToString(", ")

            customerAddressJson = Gson().toJson(addr)
        }


        /* ───────── 3. BRANCH ───────── */

        data.branchId?.let { branch ->
            selectedBranchId = branch._id
            selectedBranchName = branch.name
        }


        /* ───────── 4. SALESPERSON ───────── */

        data.salespersonId?.let { salesperson ->
            salespersonId = salesperson.id.orEmpty()

            salespersonName = salesperson.firstName
                ?.takeIf { it.isNotBlank() }
                ?: salespersonName
        }


        /* ───────── 5. PAYMENT / DISCOUNT ───────── */

        paymentType = data.paymentType
            ?.takeIf { it.isNotBlank() }
            ?: paymentType

        val paid = data.advanceAmountPaid ?: 0.0

        advanceAmount = if (paid > 0.0) {
            paid.toInt().toString()
        } else {
            "-"
        }

        val disc = data.totalDiscount ?: 0.0

        discountText = if (disc > 0.0) {
            if (disc % 1.0 == 0.0) {
                disc.toLong().toString()
            } else {
                disc.toString()
            }
        } else {
            ""
        }


        /* ───────── 6. LINE ITEMS ───────── */

        val allLines = data.items

        val garmentLines = allLines.filter {
            it.lineType.equals(
                "Custom_Garment",
                ignoreCase = true
            )
        }

        val addonLines = allLines.filter {
            it.lineType.equals(
                "Garment_Addon",
                ignoreCase = true
            )
        }

        // Group addons by their parent garment line id
        val addonsByParent: Map<String?, List<OrderOverviewItem>> =
            addonLines.groupBy { item ->
                item.parentLineId
            }


        if (garmentLines.isNotEmpty()) {

            dynamicOrderItems = garmentLines.map { gItem ->

                val cfg = gItem.customGarment

                val lineAddons = addonsByParent[gItem._id].orEmpty()


                /* ───────── WORK PRICING ───────── */

                val selectedWorkIds = lineAddons
                    .mapNotNull { addon ->
                        addon.addonWork
                            ?.workPricingId
                            ?.id
                    }
                    .filter { id ->
                        id.isNotBlank()
                    }


                val workPrices: Map<String, Double> = lineAddons
                    .mapNotNull { addon ->

                        val wpId = addon.addonWork
                            ?.workPricingId
                            ?.id
                            ?: return@mapNotNull null

                        val amount = addon.unitPrice
                            ?: addon.addonWork
                                ?.workPricingId
                                ?.basePrice
                            ?: 0.0

                        wpId to amount
                    }
                    .toMap()


                val workGstRates: Map<String, Double> = lineAddons
                    .mapNotNull { addon ->

                        val wpId = addon.addonWork
                            ?.workPricingId
                            ?.id
                            ?: return@mapNotNull null

                        wpId to (
                                addon.taxGroupId?.totalRate
                                    ?: 5.0
                                )
                    }
                    .toMap()


                /* ───────── FABRIC SOURCE ───────── */

                val resolvedFabricSource = when {

                    cfg?.fabricSource.equals(
                        "Store_Provided",
                        ignoreCase = true
                    ) -> {
                        "Store Provided"
                    }

                    cfg?.fabricSource.equals(
                        "Customer_Provided",
                        ignoreCase = true
                    ) -> {
                        "Customer Provided"
                    }

                    !cfg?.fabricSource.isNullOrBlank() -> {
                        cfg?.fabricSource.orEmpty()
                    }

                    else -> {
                        "Store Provided"
                    }
                }


                /*
                 * Fabric pricing list was removed because
                 * fabricPricingList is not available in this scope.
                 *
                 * The order API already provides fabricNotes,
                 * so keep the selected fabric name directly.
                 */
                val fabricSelection = cfg?.fabricNotes.orEmpty()

                val fabricRate = 0.0


                /*
                 * Production template list was removed because
                 * productionTemplates is not available in this scope.
                 *
                 * Keep the productionTemplateId received from
                 * the order API.
                 */
                val productionTemplateId =
                    gItem.productionTemplateId.orEmpty()

                val productionTemplateName = ""


                /* ───────── BUILD DYNAMIC GARMENT ───────── */

                DynamicGarmentItem(

                    id = gItem._id.ifBlank {
                        UUID.randomUUID().toString()
                    },

                    isItemExpanded = false,

                    config = ConfiguredOrderItem(

                        id = gItem._id.ifBlank {
                            UUID.randomUUID().toString()
                        },

                        garmentType = cfg?.garmentName.orEmpty(),

                        garmentCategory = cfg
                            ?.categoryDisplayName
                            .orEmpty(),

                        garmentId = cfg
                            ?.garmentId
                            ?.id
                            .orEmpty(),

                        garmentCategoryId = cfg
                            ?.garmentCategoryId
                            ?.id
                            .orEmpty(),

                        segmentId = cfg
                            ?.segmentId
                            ?.id
                            .orEmpty(),

                        designId = cfg
                            ?.designId
                            ?.id
                            .orEmpty(),

                        designPreset = cfg
                            ?.designName
                            .orEmpty(),

                        productionTemplateId =
                            productionTemplateId,

                        productionTemplateName =
                            productionTemplateName,

                        stitchingSacCode =
                            gItem.sacCode.orEmpty(),

                        colorAccent =
                            cfg?.colorAccent.orEmpty(),

                        sizeStandard =
                            cfg?.sizeStandard.orEmpty(),

                        stitchingType =
                            cfg?.stitchingType.orEmpty(),

                        quantity =
                            gItem.quantity.coerceAtLeast(1),

                        stitchingPrice =
                            gItem.stitchingCharge,

                        stitchingGstRate =
                            gItem.taxGroupId?.totalRate ?: 5.0,

                        fabricSource =
                            resolvedFabricSource,

                        fabricSelection =
                            fabricSelection,

                        fabricRate =
                            fabricRate,

                        fabricGstRate =
                            gItem.taxGroupId?.totalRate ?: 5.0,

                        selectedWorkPricingIds =
                            selectedWorkIds,

                        customWorkPrices =
                            workPrices,

                        customWorkGstRates =
                            workGstRates,

                        specialInstructions =
                            cfg?.specialInstructions.orEmpty(),

                        deliveryDate =
                            formatIsoDateForDisplay(data.dueDate),

                        isCustomizationEnabled =
                            selectedWorkIds.isNotEmpty(),

                        trialRequired =
                            gItem.trialRequired
                    )
                )
            }


            /* ───────── DELIVERY DATE FALLBACK ───────── */

            if (expectedDeliveryDate.isBlank()) {

                expectedDeliveryDate = dynamicOrderItems
                    .filterIsInstance<DynamicGarmentItem>()
                    .firstOrNull {
                        it.config.deliveryDate.isNotBlank()
                    }
                    ?.config
                    ?.deliveryDate
                    .orEmpty()
            }
        }


        /* ───────── 7. ATTACHMENTS ───────── */

        val urls = data.attachments
            ?.mapNotNull { attachment ->
                attachment.url
            }
            .orEmpty()

        if (urls.isNotEmpty()) {
            existingImageUrls = urls
        }


        /* ───────── 8. MARK PREFILL DONE ───────── */

        isPrefilled = true
    }


    val fabricItemList by inventoryViewModel.fabricItemList.collectAsStateWithLifecycle()
    val accessoryItemList by inventoryViewModel.accessoryItemList.collectAsStateWithLifecycle()
    val warehouseDropdownItems by inventoryViewModel.warehouseDropdown.collectAsStateWithLifecycle()
    val binDropdownItems by inventoryViewModel.binDropdown.collectAsStateWithLifecycle()
    val productionTemplates by inventoryViewModel.productionTemplates.collectAsStateWithLifecycle()

    var showDiscardDialog by remember { mutableStateOf(false) }

    BackHandler {
        showDiscardDialog = true
    }

    if (showDiscardDialog) {
        DiscardOrderConfirmDialog(
            onConfirmDiscard = {
                showDiscardDialog = false
                OrderFlowStore.clear()
                onCancel()
            },
            onDismiss = { showDiscardDialog = false }
        )
    }

    val garmentStyles by settingsViewModel.garmentStyles.collectAsStateWithLifecycle()
    val workPricingList by settingsViewModel.workPricingList.collectAsStateWithLifecycle()
    val garmentsList by settingsViewModel.garments.collectAsStateWithLifecycle()
    val fabricPricingList by settingsViewModel.fabricPricingList.collectAsStateWithLifecycle()
    val designsList by settingsViewModel.designs.collectAsStateWithLifecycle()
    val taxGroups by inventoryViewModel.taxGroups.collectAsStateWithLifecycle()

    val workInfoMap = remember(workPricingList) {
        workPricingList.associate { it.id to (it.workType to it.basePrice) }
    }
    val fabricPriceMap = remember(fabricPricingList) {
        fabricPricingList.associate { it.name to it.sellingPrice }
    }
    val workMetaMap = remember(workPricingList) {
        workPricingList.associate { it.id to WorkMeta(it.workType, it.sacCode.orEmpty(), it.basePrice) }
    }
    val taxGroupIdByRate = remember(taxGroups) {
        taxGroups.associate { it.totalRate to it.id }
    }

    val staffList by salesViewModel.staffList.collectAsStateWithLifecycle()
    val salespersonNames = remember(staffList) { staffList.map { it.firstName } }
    val salespersonNameToId = remember(staffList) { staffList.associate { it.firstName to it.id } }

    val customerUiState by customerViewModel.uiState.collectAsStateWithLifecycle()
    val customerSearchResults = (customerUiState as? CustomerUiState.Success)?.customers ?: emptyList()

    val branchUiState by branchViewModel.uiState.collectAsStateWithLifecycle()
    val branches = (branchUiState as? BranchUiState.Success)?.branches ?: emptyList()

    LaunchedEffect(Unit) {
        salesViewModel.fetchStaff()
        branchViewModel.loadBranches()
        settingsViewModel.fetchWorkPricing(status = "Active")
        settingsViewModel.fetchGarments()
        settingsViewModel.fetchSegments()
        settingsViewModel.fetchFabricPricing()
        settingsViewModel.fetchDesigns()
        inventoryViewModel.fetchTaxGroups()
        inventoryViewModel.loadWarehouseDropdown()
        inventoryViewModel.fetchFabricList()
        inventoryViewModel.fetchAccessoryList()
        inventoryViewModel.fetchProductionTemplates()
    }

    val branchNames = remember(branches) {
        branches.map { branch ->
            branch.name?.takeIf { it.isNotBlank() } ?: branch.branchId ?: "Branch"
        }
    }

    val branchNameToIdMap = remember(branches) {
        branches.associate { branch ->
            val displayName = branch.name?.takeIf { it.isNotBlank() } ?: branch.branchId ?: "Branch"
            displayName to branch.id
        }
    }

    LaunchedEffect(branches) {
        if (selectedBranchId.isNotBlank() && selectedBranchName.isBlank()) {
            val matching = branches.firstOrNull { it.id == selectedBranchId }
            if (matching != null) {
                selectedBranchName = matching.name?.takeIf { it.isNotBlank() } ?: matching.branchId.orEmpty()
            }
        } else if (selectedBranchId.isBlank() && branches.isNotEmpty()) {
            val first = branches.first()
            selectedBranchId = first.id
            selectedBranchName = first.name?.takeIf { it.isNotBlank() } ?: first.branchId.orEmpty()
        }
    }

    var showSelectDesignDialog by remember { mutableStateOf(false) }
    var designTargetItemId by remember { mutableStateOf("") }
    var selectedPresetDesign by remember {
        mutableStateOf(
            DesignItem(
                id = "6ab4cbab9a1fbdafed06a436",
                name = "V-Neck Basic",
                designType = "Front Neck",
                code = "V-NECK-001",
                status = "Active"
            )
        )
    }

    val totalDiscount = discountText.toDoubleOrNull() ?: 0.0

    var selectedImages by remember {
        mutableStateOf<List<Uri>>(initialData?.designImages ?: emptyList())
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            selectedImages = (selectedImages + uris).distinct().take(5)
        }
    }

    val recordedVoiceNoteUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var expandedSection by rememberSaveable { mutableStateOf("customer") }

    LaunchedEffect(expandedSection) {
        if (expandedSection == "pricing") {
            inventoryViewModel.fetchTaxGroups()
        }
    }

    var customerTypeExpanded by remember { mutableStateOf(false) }
    var branchExpanded by remember { mutableStateOf(false) }
    var orderTypeExpanded by remember { mutableStateOf(false) }
    var paymentTypeExpanded by remember { mutableStateOf(false) }
    var salespersonExpanded by remember { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TitleBar(
                title = if (isEditMode) "Edit Order" else "Create Order",
                onClose = { showDiscardDialog = true }
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(scrollState)
                    .padding(bottom = 90.dp)
            ) {
                // 1. CUSTOMER DETAILS
                AccordionSection(
                    title = "1. Customer Details",
                    expanded = expandedSection == "customer",
                    onHeaderClick = { expandedSection = if (expandedSection == "customer") "" else "customer" }
                ) {
                    var customerDropdownExpanded by remember { mutableStateOf(false) }
                    var selectedCustomerLabel by rememberSaveable { mutableStateOf(fullName) }

                    val customerOptions = remember(customerSearchResults) {
                        customerSearchResults.map { "${it.name} (${it.mobile ?: "No phone"})" }
                    }

                    val customerMap = remember(customerSearchResults) {
                        customerSearchResults.associateBy { "${it.name} (${it.mobile ?: "No phone"})" }
                    }

                    FormDropdown(
                        label = "Customer Search & Select",
                        value = selectedCustomerLabel.ifEmpty { "Select Customer" },
                        expanded = customerDropdownExpanded,
                        onExpandChange = { customerDropdownExpanded = it },
                        options = customerOptions,
                        onOptionSelected = { selectedLabel ->
                            selectedCustomerLabel = selectedLabel
                            val customer = customerMap[selectedLabel]
                            if (customer != null) {
                                customerId = customer.id
                                customerCode = customer.customerCode.orEmpty()
                                customerAddressJson = Gson().toJson(customer.address)
                                fullName = customer.name
                                phone = customer.mobile ?: ""
                                emailAddress = customer.email.orEmpty()
                                customerType = customer.customerType?.replaceFirstChar { it.uppercase() } ?: "Individual"
                                gender = customer.gender.orEmpty()

                                val resolvedAddress = listOfNotNull(
                                    customer.address?.addressLine?.takeIf { it.isNotBlank() },
                                    customer.address?.area?.takeIf { it.isNotBlank() },
                                    customer.address?.city?.takeIf { it.isNotBlank() },
                                    customer.address?.pincode?.takeIf { it.isNotBlank() }
                                ).joinToString(", ")

                                if (resolvedAddress.isNotBlank()) {
                                    address = resolvedAddress
                                }
                            }
                        },
                        isRequired = true
                    )

                    Spacer(Modifier.height(14.dp))

                    FormLabel("Customer Name", isRequired = true)
                    FormTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        placeholder = "Enter customer name"
                    )

                    Spacer(Modifier.height(14.dp))

                    FormLabel("Email Address")
                    FormTextField(
                        value = emailAddress,
                        onValueChange = { emailAddress = it },
                        placeholder = "Enter email address"
                    )

                    Spacer(Modifier.height(14.dp))

                    FormDropdown(
                        label = "Customer Type",
                        value = customerType.ifEmpty { "Individual" },
                        expanded = customerTypeExpanded,
                        onExpandChange = { customerTypeExpanded = it },
                        options = listOf("Individual", "Corporate"),
                        onOptionSelected = { customerType = it },
                        isRequired = true
                    )

                    Spacer(Modifier.height(14.dp))

                    FormDropdown(
                        label = "Branch",
                        value = selectedBranchName.ifEmpty { "Select Branch" },
                        expanded = branchExpanded,
                        onExpandChange = { branchExpanded = it },
                        options = branchNames,
                        onOptionSelected = { name ->
                            selectedBranchName = name
                            selectedBranchId = branchNameToIdMap[name] ?: ""
                        },
                        isRequired = true
                    )

                    Spacer(Modifier.height(14.dp))

                    FormLabel("Delivery / Billing Address")
                    FormTextArea(
                        value = address,
                        onValueChange = { address = it },
                        placeholder = "Enter full delivery address..."
                    )
                }

                // 2. ORDER INFORMATION
                AccordionSection(
                    title = "2. Order Information",
                    expanded = expandedSection == "order_info",
                    onHeaderClick = { expandedSection = if (expandedSection == "order_info") "" else "order_info" }
                ) {
                    FormLabel("Order ID")
                    FormTextField(
                        value = orderIdText,
                        onValueChange = { orderIdText = it },
                        placeholder = "e.g. ORD-2026-0001 (auto-generated if empty)"
                    )

                    Spacer(Modifier.height(14.dp))

                    FormLabel("Order Date", isRequired = true)
                    DatePickerField(
                        value = orderDate,
                        onDateSelected = { orderDate = it }
                    )

                    Spacer(Modifier.height(14.dp))

                    FormDropdown(
                        label = "Order Type",
                        value = orderType.ifEmpty { "New Stitching" },
                        expanded = orderTypeExpanded,
                        onExpandChange = { orderTypeExpanded = it },
                        options = listOf("New Stitching", "Alteration", "Repair", "Retail Sale", "Mixed"),
                        onOptionSelected = { orderType = it },
                        isRequired = true
                    )

                    Spacer(Modifier.height(14.dp))

                    FormDropdown(
                        label = "Salesperson",
                        value = salespersonName.ifEmpty { "Select Salesperson" },
                        expanded = salespersonExpanded,
                        onExpandChange = { salespersonExpanded = it },
                        options = salespersonNames.ifEmpty { listOf("Loading staff...") },
                        onOptionSelected = { name ->
                            val id = salespersonNameToId[name]
                            if (id != null) {
                                salespersonName = name
                                salespersonId = id
                            }
                        },
                        isRequired = true
                    )
                }

                // 3. ORDER ITEMS
                AccordionSection(
                    title = "3. Order Items",
                    expanded = expandedSection == "order_items",
                    onHeaderClick = { expandedSection = if (expandedSection == "order_items") "" else "order_items" }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = primary_light
                        ) {
                            Text(
                                text = if (dynamicOrderItems.size > 1) "Multi-item Order (${dynamicOrderItems.size})" else "Single-item Order",
                                color = Primary,
                                fontSize = tokens.label,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    dynamicOrderItems.forEachIndexed { index, item ->
                        val itemNumber = index + 1

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 14.dp),
                            shape = RoundedCornerShape(tokens.cardCornerRadius),
                            colors = CardDefaults.cardColors(containerColor = whiteBg),
                            border = BorderStroke(1.dp, BorderGray)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                dynamicOrderItems = dynamicOrderItems.map {
                                                    if (it.id == item.id) {
                                                        when (it) {
                                                            is DynamicGarmentItem -> it.copy(isItemExpanded = !it.isItemExpanded)
                                                            is DynamicFabricItem -> it.copy(isItemExpanded = !it.isItemExpanded)
                                                            is DynamicAccessoryItem -> it.copy(isItemExpanded = !it.isItemExpanded)
                                                        }
                                                    } else it
                                                }
                                            },
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = when (item) {
                                                is DynamicGarmentItem -> Primary
                                                is DynamicFabricItem -> LocalTealBadgeColor
                                                is DynamicAccessoryItem -> darkGreenBg
                                            }
                                        ) {
                                            Text(
                                                text = "ITEM #$itemNumber",
                                                color = whiteBg,
                                                fontSize = tokens.label,
                                                fontWeight = FontWeight.Medium,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                        Text(
                                            text = when (item) {
                                                is DynamicGarmentItem -> "Garment Configuration"
                                                is DynamicFabricItem -> "Fabric Configuration"
                                                is DynamicAccessoryItem -> "Inventory Items"
                                            },
                                            fontSize = tokens.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = TextPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        IconButton(
                                            onClick = {
                                                dynamicOrderItems = dynamicOrderItems.filterNot { it.id == item.id }
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DeleteOutline,
                                                contentDescription = "Delete Item",
                                                tint = redText,
                                                modifier = Modifier.size(tokens.iconSize)
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                dynamicOrderItems = dynamicOrderItems.map {
                                                    if (it.id == item.id) {
                                                        when (it) {
                                                            is DynamicGarmentItem -> it.copy(isItemExpanded = !it.isItemExpanded)
                                                            is DynamicFabricItem -> it.copy(isItemExpanded = !it.isItemExpanded)
                                                            is DynamicAccessoryItem -> it.copy(isItemExpanded = !it.isItemExpanded)
                                                        }
                                                    } else it
                                                }
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (item.isItemExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                                contentDescription = null,
                                                tint = TextSecondary
                                            )
                                        }
                                    }
                                }

                                AnimatedVisibility(visible = item.isItemExpanded) {
                                    when (item) {
                                        is DynamicGarmentItem -> {
                                            val currentGarment = item.config

                                            Column(modifier = Modifier.padding(top = 14.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    var typeOpen by remember { mutableStateOf(false) }
                                                    var catOpen by remember { mutableStateOf(false) }

                                                    val garmentTypeOptions: List<String> = remember(garmentsList) {
                                                        garmentsList.map { it.displayName?.takeIf { d -> d.isNotBlank() } ?: it.name }
                                                    }

                                                    val selectedGarmentItem: GarmentItem? = remember(garmentsList, currentGarment.garmentType) {
                                                        garmentsList.find {
                                                            (it.displayName?.takeIf { d -> d.isNotBlank() } ?: it.name) == currentGarment.garmentType
                                                        }
                                                    }

                                                    val availableCategoryStyles: List<GarmentStyleItem> = remember(garmentStyles, selectedGarmentItem) {
                                                        if (selectedGarmentItem != null) {
                                                            garmentStyles.filter { it.garment?.id == selectedGarmentItem.id }
                                                        } else {
                                                            emptyList()
                                                        }
                                                    }

                                                    val categoryOptions: List<String> = remember(availableCategoryStyles) {
                                                        availableCategoryStyles.map { it.displayName?.takeIf { d -> d.isNotBlank() } ?: it.name }
                                                    }

                                                    Column(Modifier.weight(1.3f)) {
                                                        val typeDisplayValue = currentGarment.garmentType.ifEmpty { "Select Garment Type" }
                                                        val displayedTypeOptions =
                                                            garmentTypeOptions.ifEmpty {
                                                                listOf("Loading garments...")
                                                            }

                                                        FormDropdown(
                                                            label = "Garment Type",
                                                            value = typeDisplayValue,
                                                            expanded = typeOpen,
                                                            onExpandChange = { typeOpen = it },
                                                            options = displayedTypeOptions,
                                                            onOptionSelected = { selectedTypeName ->
                                                                val chosenGarment = garmentsList.find {
                                                                    (it.displayName?.takeIf { d -> d.isNotBlank() } ?: it.name) == selectedTypeName
                                                                }
                                                                if (chosenGarment != null) {
                                                                    settingsViewModel.fetchGarmentStyles(segmentId = null, garmentId = chosenGarment.id)

                                                                    val updated = currentGarment.copy(
                                                                        garmentType = selectedTypeName,
                                                                        garmentCategory = "Select Category",
                                                                        garmentCategoryId = "",
                                                                        garmentId = chosenGarment.id,
                                                                        segmentId = chosenGarment.applicableSegments.firstOrNull()?.id.orEmpty(),
                                                                        productionTemplateId = chosenGarment.productionTemplateId.orEmpty(),
                                                                        stitchingSacCode = chosenGarment.sacCode.orEmpty(),
                                                                        stitchingPrice = chosenGarment.baseStitchingCharge
                                                                    )
                                                                    dynamicOrderItems = dynamicOrderItems.map { orderItem ->
                                                                        if (orderItem.id == item.id) item.copy(config = updated) else orderItem
                                                                    }
                                                                }
                                                            },
                                                            isRequired = true
                                                        )
                                                    }

                                                    Column(Modifier.weight(1.3f)) {
                                                        val categoryDisplayValue = when {
                                                            selectedGarmentItem == null -> "Select garment type first"
                                                            currentGarment.garmentCategory.isNotBlank() && currentGarment.garmentCategory != "Select Category" -> currentGarment.garmentCategory
                                                            else -> "Select Category"
                                                        }

                                                        val displayedCategoryOptions = when {
                                                            selectedGarmentItem == null -> listOf("Select garment type first")
                                                            categoryOptions.isEmpty() -> listOf("No categories available")
                                                            else -> categoryOptions
                                                        }

                                                        FormDropdown(
                                                            label = "Category",
                                                            value = categoryDisplayValue,
                                                            expanded = catOpen,
                                                            onExpandChange = {
                                                                if (selectedGarmentItem != null) catOpen = it
                                                            },
                                                            options = displayedCategoryOptions,
                                                            onOptionSelected = { selectedCategoryName ->
                                                                if (selectedGarmentItem != null) {
                                                                    val chosenCategory = availableCategoryStyles.find {
                                                                        (it.displayName?.takeIf { d -> d.isNotBlank() } ?: it.name) == selectedCategoryName
                                                                    }
                                                                    val effectiveStitching = if ((chosenCategory?.stitchingCharge ?: 0.0) > 0.0) {
                                                                        chosenCategory!!.stitchingCharge
                                                                    } else {
                                                                        currentGarment.stitchingPrice
                                                                    }

                                                                    val updated = currentGarment.copy(
                                                                        garmentCategory = selectedCategoryName,
                                                                        garmentCategoryId = chosenCategory?.id.orEmpty(),
                                                                        segmentId = chosenCategory?.segment?.id ?: currentGarment.segmentId,
                                                                        stitchingPrice = effectiveStitching
                                                                    )
                                                                    dynamicOrderItems = dynamicOrderItems.map { orderItem ->
                                                                        if (orderItem.id == item.id) item.copy(config = updated) else orderItem
                                                                    }
                                                                }
                                                            },
                                                            isRequired = true
                                                        )
                                                    }

                                                    Column(Modifier.weight(0.7f)) {
                                                        FormLabel("Qty", isRequired = true)
                                                        FormTextField(
                                                            value = if (currentGarment.quantity == 0) "" else currentGarment.quantity.toString(),
                                                            onValueChange = { input ->
                                                                val filtered = input.filter { it.isDigit() }
                                                                val parsed = filtered.toIntOrNull() ?: 1
                                                                val updated = currentGarment.copy(quantity = parsed)
                                                                dynamicOrderItems = dynamicOrderItems.map { orderItem ->
                                                                    if (orderItem.id == item.id) item.copy(config = updated) else orderItem
                                                                }
                                                            },
                                                            placeholder = "1",
                                                            keyboardType = KeyboardType.Number
                                                        )
                                                    }
                                                }

                                                Spacer(Modifier.height(12.dp))

                                                var fabSourceOpen by remember { mutableStateOf(false) }
                                                FormDropdown(
                                                    label = "Fabric Source",
                                                    value = currentGarment.fabricSource.ifEmpty { "Store Provided" },
                                                    expanded = fabSourceOpen,
                                                    onExpandChange = { fabSourceOpen = it },
                                                    options = listOf("Store Provided", "Customer Provided"),
                                                    onOptionSelected = { selected ->
                                                        if (selected == "Store Provided") {
                                                            settingsViewModel.fetchFabricPricing()
                                                        }
                                                        val updated = currentGarment.copy(
                                                            fabricSource = selected,
                                                            fabricSelection = if (selected == "Customer Provided") "" else currentGarment.fabricSelection,
                                                            fabricRate = if (selected == "Customer Provided") 0.0 else currentGarment.fabricRate
                                                        )
                                                        dynamicOrderItems = dynamicOrderItems.map {
                                                            if (it.id == item.id) item.copy(config = updated) else it
                                                        }
                                                    },
                                                    isRequired = true
                                                )

                                                Spacer(Modifier.height(12.dp))

                                                if (currentGarment.fabricSource.equals("Customer Provided", ignoreCase = true)) {
                                                    FormLabel("Customer Provided Fabric Details", isRequired = true)
                                                    FormTextField(
                                                        value = currentGarment.fabricSelection,
                                                        onValueChange = { typedDetails ->
                                                            val updated = currentGarment.copy(fabricSelection = typedDetails)
                                                            dynamicOrderItems = dynamicOrderItems.map {
                                                                if (it.id == item.id) item.copy(config = updated) else it
                                                            }
                                                        },
                                                        placeholder = "Enter fabric details (e.g., Raw silk, Navy blue)"
                                                    )
                                                } else {
                                                    // API response mattum: fabric-list?fabric=true
                                                    val garmentFabricOptions = remember(fabricItemList) {
                                                        fabricItemList.map { it.name }
                                                    }

                                                    var fabSelectOpen by remember { mutableStateOf(false) }
                                                    FormDropdown(
                                                        label = "Store Provided",
                                                        value = currentGarment.fabricSelection.ifEmpty { "Select Fabric" },
                                                        expanded = fabSelectOpen,
                                                        onExpandChange = { fabSelectOpen = it },
                                                        options = garmentFabricOptions.ifEmpty { listOf("Loading fabrics...") },
                                                        onOptionSelected = { selected ->
                                                            val selectedFabric = fabricItemList.find { it.name == selected }
                                                            if (selectedFabric != null) {
                                                                val updated = currentGarment.copy(
                                                                    fabricSelection = selectedFabric.name,
                                                                    fabricRate = selectedFabric.sellingPrice
                                                                )
                                                                dynamicOrderItems = dynamicOrderItems.map {
                                                                    if (it.id == item.id) item.copy(config = updated) else it
                                                                }
                                                            }
                                                        },
                                                        isRequired = true
                                                    )
                                                }

                                                Spacer(Modifier.height(12.dp))

                                                var stitchTypeOpen by remember { mutableStateOf(false) }
                                                FormDropdown(
                                                    label = "Stitching Type",
                                                    value = currentGarment.stitchingType,
                                                    expanded = stitchTypeOpen,
                                                    onExpandChange = { stitchTypeOpen = it },
                                                    options = listOf("Normal Machine", "Hand Finished", "Double Seam"),
                                                    onOptionSelected = { selected ->
                                                        val updated = currentGarment.copy(stitchingType = selected)
                                                        dynamicOrderItems = dynamicOrderItems.map {
                                                            if (it.id == item.id) item.copy(config = updated) else it
                                                        }
                                                    }
                                                )

                                                Spacer(Modifier.height(12.dp))

                                                var templateOpen by remember { mutableStateOf(false) }
                                                val templateOptions = remember(productionTemplates) {
                                                    productionTemplates.map { it.name }
                                                }
                                                val templateMap = remember(productionTemplates) {
                                                    productionTemplates.associateBy { it.name }
                                                }

                                                val currentTemplateDisplay = when {
                                                    currentGarment.productionTemplateName.isNotBlank() -> currentGarment.productionTemplateName
                                                    currentGarment.productionTemplateId.isNotBlank() -> {
                                                        productionTemplates.find { it.id == currentGarment.productionTemplateId }?.name ?: "Select Workflow Template"
                                                    }
                                                    else -> "Select Workflow Template"
                                                }

                                                FormDropdown(
                                                    label = "Production Workflow Template",
                                                    isRequired = true,
                                                    value = currentTemplateDisplay,
                                                    expanded = templateOpen,
                                                    onExpandChange = { templateOpen = it },
                                                    options = if (templateOptions.isEmpty()) listOf("Loading templates...") else templateOptions,
                                                    onOptionSelected = { chosenName ->
                                                        val selectedTmpl = templateMap[chosenName]
                                                        val updated = currentGarment.copy(
                                                            productionTemplateId = selectedTmpl?.id.orEmpty(),
                                                            productionTemplateName = chosenName
                                                        )
                                                        dynamicOrderItems = dynamicOrderItems.map { orderItem ->
                                                            if (orderItem.id == item.id) item.copy(config = updated) else orderItem
                                                        }
                                                    }
                                                )

                                                Spacer(Modifier.height(12.dp))
                                                val shownDesign = designsList.find { it.id == currentGarment.designId }
                                                    ?: selectedPresetDesign
                                                FormLabel("Design/ Style Preset")
                                                Surface(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .border(BorderStroke(1.dp, BorderGray), RoundedCornerShape(10.dp))
                                                        .clickable {
                                                            settingsViewModel.fetchDesigns()
                                                            designTargetItemId = item.id
                                                            showSelectDesignDialog = true
                                                        },
                                                    shape = RoundedCornerShape(10.dp),
                                                    color = whiteBg
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(10.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                        ) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(38.dp)
                                                                    .clip(RoundedCornerShape(8.dp))
                                                                    .background(grey_border),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                val imgUrl = shownDesign.imageUrl
                                                                if (!imgUrl.isNullOrBlank()) {
                                                                    AsyncImage(
                                                                        model = imgUrl,
                                                                        contentDescription = shownDesign.name,
                                                                        contentScale = ContentScale.Crop,
                                                                        modifier = Modifier.fillMaxSize()
                                                                    )
                                                                } else {
                                                                    Icon(
                                                                        imageVector = Icons.Default.Checkroom,
                                                                        contentDescription = null,
                                                                        tint = TextSecondary,
                                                                        modifier = Modifier.size(tokens.iconSize)
                                                                    )
                                                                }
                                                            }
                                                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                                Text(
                                                                    text = shownDesign.name,
                                                                    fontSize = tokens.bodyMedium,
                                                                    fontWeight = FontWeight.Medium,
                                                                    color = TextPrimary
                                                                )
                                                                Row(
                                                                    verticalAlignment = Alignment.CenterVertically,
                                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                                ) {
                                                                    Text(text = shownDesign.designType, fontSize = tokens.caption, color = TextSecondary)
                                                                    Text(text = "•", fontSize = tokens.caption, color = greentext)
                                                                    Text(text = shownDesign.status, fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = greentext)
                                                                }
                                                            }
                                                        }
                                                        Icon(Icons.Default.KeyboardArrowDown, null, tint = TextSecondary, modifier = Modifier.size(tokens.iconSize))
                                                    }
                                                }

                                                Spacer(Modifier.height(12.dp))

                                                var itemDeliveryDate by rememberSaveable { mutableStateOf(currentGarment.deliveryDate.ifBlank { todayFormatted }) }

                                                FormLabel("Item Delivery Date", isRequired = true)

                                                DatePickerField(
                                                    value = itemDeliveryDate,
                                                    onDateSelected = { selectedDate ->
                                                        itemDeliveryDate = selectedDate
                                                        expectedDeliveryDate = selectedDate
                                                        val updated = currentGarment.copy(deliveryDate = selectedDate)
                                                        dynamicOrderItems = dynamicOrderItems.map { orderItem ->
                                                            if (orderItem.id == item.id) item.copy(config = updated) else orderItem
                                                        }
                                                    }
                                                )

                                                Spacer(Modifier.height(12.dp))

                                                FormLabel("Garment Special Instructions")
                                                FormTextArea(
                                                    value = currentGarment.specialInstructions,
                                                    onValueChange = { newText ->
                                                        val updated = currentGarment.copy(specialInstructions = newText)
                                                        dynamicOrderItems = dynamicOrderItems.map {
                                                            if (it.id == item.id) item.copy(config = updated) else it
                                                        }
                                                    },
                                                    placeholder = "Specific pocket layout, double-stitch specifications, pattern matches, etc."
                                                )

                                                Spacer(Modifier.height(14.dp))

                                                Surface(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(14.dp),
                                                    color = whiteBg,
                                                    border = BorderStroke(1.dp, BorderGray)
                                                ) {
                                                    Column(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(14.dp),
                                                        verticalArrangement = Arrangement.spacedBy(14.dp)
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Row(
                                                                modifier = Modifier.weight(1f),
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                            ) {
                                                                Box(Modifier.size(8.dp).clip(CircleShape).background(Primary))
                                                                Text(
                                                                    text = "Customization & Embellishment",
                                                                    fontSize = tokens.bodyMedium,
                                                                    fontWeight = FontWeight.Medium,
                                                                    color = TextPrimary,
                                                                    maxLines = 1,
                                                                    overflow = TextOverflow.Ellipsis
                                                                )
                                                            }

                                                            Surface(
                                                                shape = RoundedCornerShape(20.dp),
                                                                color = grey_border,
                                                                border = BorderStroke(1.dp, sectionBorder)
                                                            ) {
                                                                Row(modifier = Modifier.padding(2.dp)) {
                                                                    Box(
                                                                        modifier = Modifier
                                                                            .width(42.dp)
                                                                            .height(28.dp)
                                                                            .clip(RoundedCornerShape(16.dp))
                                                                            .background(if (!currentGarment.isCustomizationEnabled) whiteBg else Color.Transparent)
                                                                            .clickable {
                                                                                val updatedConfig = currentGarment.copy(isCustomizationEnabled = false)
                                                                                dynamicOrderItems = dynamicOrderItems.map {
                                                                                    if (it.id == item.id) item.copy(config = updatedConfig) else it
                                                                                }
                                                                            },
                                                                        contentAlignment = Alignment.Center
                                                                    ) {
                                                                        Text(
                                                                            text = "No",
                                                                            fontSize = tokens.bodySmall,
                                                                            fontWeight = if (!currentGarment.isCustomizationEnabled) FontWeight.Medium else FontWeight.Normal,
                                                                            color = if (!currentGarment.isCustomizationEnabled) TextPrimary else TextSecondary
                                                                        )
                                                                    }

                                                                    Box(
                                                                        modifier = Modifier
                                                                            .width(42.dp)
                                                                            .height(28.dp)
                                                                            .clip(RoundedCornerShape(16.dp))
                                                                            .background(if (currentGarment.isCustomizationEnabled) Primary else Color.Transparent)
                                                                            .clickable {
                                                                                val updatedConfig = currentGarment.copy(isCustomizationEnabled = true)
                                                                                dynamicOrderItems = dynamicOrderItems.map {
                                                                                    if (it.id == item.id) item.copy(config = updatedConfig) else it
                                                                                }
                                                                            },
                                                                        contentAlignment = Alignment.Center
                                                                    ) {
                                                                        Text(
                                                                            text = "Yes",
                                                                            fontSize = tokens.bodySmall,
                                                                            fontWeight = FontWeight.Medium,
                                                                            color = if (currentGarment.isCustomizationEnabled) whiteBg else TextSecondary
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                        }

                                                        AnimatedVisibility(
                                                            visible = currentGarment.isCustomizationEnabled,
                                                            enter = expandVertically() + fadeIn(),
                                                            exit = shrinkVertically() + fadeOut()
                                                        ) {
                                                            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                                    Text(
                                                                        text = "WORK TYPE (MULTI-SELECT)",
                                                                        fontSize = tokens.label,
                                                                        fontWeight = FontWeight.Medium,
                                                                        color = TextSecondary
                                                                    )

                                                                    if (workPricingList.isEmpty()) {
                                                                        Text("Loading work types...", fontSize = tokens.caption, color = TextSecondary)
                                                                    } else {
                                                                        @OptIn(ExperimentalLayoutApi::class)
                                                                        FlowRow(
                                                                            modifier = Modifier.fillMaxWidth(),
                                                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                                                        ) {
                                                                            workPricingList.forEach { workPricingItem ->
                                                                                val isSelected = currentGarment.selectedWorkPricingIds.contains(workPricingItem.id)

                                                                                Surface(
                                                                                    shape = RoundedCornerShape(16.dp),
                                                                                    color = if (isSelected) primary_light else whiteBg,
                                                                                    border = BorderStroke(1.dp, if (isSelected) Primary else BorderGray),
                                                                                    modifier = Modifier
                                                                                        .clip(RoundedCornerShape(16.dp))
                                                                                        .clickable {
                                                                                            val currentIds = currentGarment.selectedWorkPricingIds
                                                                                            val updatedIds = if (isSelected) {
                                                                                                currentIds - workPricingItem.id
                                                                                            } else {
                                                                                                currentIds + workPricingItem.id
                                                                                            }

                                                                                            val updatedConfig = currentGarment.copy(selectedWorkPricingIds = updatedIds)
                                                                                            dynamicOrderItems = dynamicOrderItems.map {
                                                                                                if (it.id == item.id) item.copy(config = updatedConfig) else it
                                                                                            }
                                                                                        }
                                                                                ) {
                                                                                    Text(
                                                                                        text = if (isSelected) "✓ ${workPricingItem.workType}" else workPricingItem.workType,
                                                                                        fontSize = tokens.caption,
                                                                                        fontWeight = FontWeight.Medium,
                                                                                        color = if (isSelected) Primary else TextPrimary,
                                                                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                                                                    )
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        is DynamicFabricItem -> {
                                            Column(
                                                modifier = Modifier.padding(top = 14.dp),
                                                verticalArrangement = Arrangement.spacedBy(14.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                ) {
                                                    val fabricOptions = remember(fabricItemList) {
                                                        fabricItemList.map { inv ->
                                                            if (inv.sku.isNotBlank()) "${inv.name} (${inv.sku})" else inv.name
                                                        }
                                                    }
                                                    val fabricMap = remember(fabricItemList) {
                                                        fabricItemList.associateBy { inv ->
                                                            if (inv.sku.isNotBlank()) "${inv.name} (${inv.sku})" else inv.name
                                                        }
                                                    }

                                                    var fabSelectOpen by remember { mutableStateOf(false) }

                                                    Column(modifier = Modifier.weight(1.4f)) {
                                                        Row(Modifier.fillMaxWidth()) {
                                                            if (item.selection.isNotBlank()) {
                                                                if (item.isOutOfStock) {
                                                                    Surface(
                                                                        shape = RoundedCornerShape(4.dp),
                                                                        color = redText.copy(alpha = 0.1f),
                                                                        border = BorderStroke(1.dp, redText.copy(alpha = 0.3f))
                                                                    ) {
                                                                        Text(
                                                                            text = "Out of Stock",
                                                                            color = redText,
                                                                            fontSize = tokens.label,
                                                                            fontWeight = FontWeight.Medium,
                                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                                        )
                                                                    }
                                                                } else {
                                                                    Surface(
                                                                        shape = RoundedCornerShape(4.dp),
                                                                        color = activity_green_bg,
                                                                        border = BorderStroke(1.dp, greenBg)
                                                                    ) {
                                                                        Text(
                                                                            text = "In Stock: ${item.stockCount.toInt()} Meters",
                                                                            color = darkGreenBg,
                                                                            fontSize = tokens.label,
                                                                            fontWeight = FontWeight.Medium,
                                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                        }
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            FormLabel("Fabric Selection", isRequired = true)
                                                        }

                                                        Spacer(Modifier.height(4.dp))

                                                        FormDropdown(
                                                            label = "",
                                                            value = item.selection.ifBlank { "Select Fabric" },
                                                            expanded = fabSelectOpen,
                                                            onExpandChange = { fabSelectOpen = it },
                                                            options = fabricOptions.ifEmpty { listOf("Loading fabrics...") },
                                                            onOptionSelected = { chosenDisplay ->
                                                                val selectedInv = fabricMap[chosenDisplay]
                                                                val isOut = (selectedInv?.currentStock ?: 0.0) <= 0.0
                                                                val updated = item.copy(
                                                                    itemId = selectedInv?.id ?: selectedInv?._id.orEmpty(),
                                                                    selection = chosenDisplay,
                                                                    stockCount = selectedInv?.currentStock ?: 0.0,
                                                                    isOutOfStock = isOut,
                                                                    unit = if (!selectedInv?.unit.isNullOrBlank()) "${selectedInv.unit} (m)" else item.unit
                                                                )
                                                                dynamicOrderItems = dynamicOrderItems.map {
                                                                    if (it.id == item.id) updated else it
                                                                }
                                                            },
                                                            isRequired = true
                                                        )
                                                    }

                                                    Column(modifier = Modifier.weight(1f)) {
                                                        FormLabel("Quantity", isRequired = true)
                                                        FormTextField(
                                                            value = item.quantity,
                                                            onValueChange = { qty ->
                                                                val updated = item.copy(quantity = qty)
                                                                dynamicOrderItems = dynamicOrderItems.map {
                                                                    if (it.id == item.id) updated else it
                                                                }
                                                            },
                                                            placeholder = "1"
                                                        )
                                                    }

                                                    var unitOpen by remember { mutableStateOf(false) }
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        FormDropdown(
                                                            label = "Unit",
                                                            value = item.unit.ifBlank { "Meters (m)" },
                                                            expanded = unitOpen,
                                                            onExpandChange = { unitOpen = it },
                                                            options = listOf("Meters (m)", "Yards (yd)", "Pieces (pcs)"),
                                                            onOptionSelected = { selectedUnit ->
                                                                val updated = item.copy(unit = selectedUnit)
                                                                dynamicOrderItems = dynamicOrderItems.map {
                                                                    if (it.id == item.id) updated else it
                                                                }
                                                            },
                                                            isRequired = true
                                                        )
                                                    }
                                                }

                                                if (item.selection.isNotBlank()) {
                                                    var whOpen by remember { mutableStateOf(false) }
                                                    var binOpen by remember { mutableStateOf(false) }

                                                    val whOptions: List<String> = remember(warehouseDropdownItems) {
                                                        warehouseDropdownItems.map { it.label }
                                                    }
                                                    val whMap = remember(warehouseDropdownItems) {
                                                        warehouseDropdownItems.associateBy { it.label }
                                                    }

                                                    val binOptions: List<String> = remember(binDropdownItems) {
                                                        binDropdownItems.map { it.name }
                                                    }
                                                    val binMap = remember(binDropdownItems) {
                                                        binDropdownItems.associateBy { it.name }
                                                    }

                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                    ) {
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            val whDisplayValue = item.warehouseName.ifBlank { "Select Warehouse" }
                                                            FormDropdown(
                                                                label = "Pick Warehouse",
                                                                isRequired = true,
                                                                value = whDisplayValue,
                                                                expanded = whOpen,
                                                                onExpandChange = { whOpen = it },
                                                                options = whOptions.ifEmpty { listOf("Loading warehouses...") },
                                                                onOptionSelected = { chosenWhName ->
                                                                    val wh = whMap[chosenWhName]
                                                                    val whId = wh?.value.orEmpty()
                                                                    if (whId.isNotBlank()) {
                                                                        inventoryViewModel.loadBinDropdown(whId)
                                                                    }
                                                                    val updated = item.copy(
                                                                        warehouseId = whId,
                                                                        warehouseName = chosenWhName,
                                                                        binId = "",
                                                                        binName = ""
                                                                    )
                                                                    dynamicOrderItems = dynamicOrderItems.map { if (it.id == item.id) updated else it }
                                                                }
                                                            )
                                                        }

                                                        Column(modifier = Modifier.weight(1f)) {
                                                            val binPlaceholder = if (item.warehouseId.isBlank()) "Select warehouse first" else "Select Bin"
                                                            val binDisplayValue = item.binName.ifBlank { binPlaceholder }

                                                            FormDropdown(
                                                                label = "Pick Bin",
                                                                isRequired = true,
                                                                value = binDisplayValue,
                                                                expanded = binOpen,
                                                                onExpandChange = {
                                                                    if (item.warehouseId.isNotBlank()) binOpen = it
                                                                },
                                                                options = if (item.warehouseId.isBlank()) {
                                                                    listOf("Select warehouse first")
                                                                } else binOptions.ifEmpty {
                                                                    listOf("No bins available")
                                                                },
                                                                onOptionSelected = { chosenBinName ->
                                                                    val bin = binMap[chosenBinName]
                                                                    val updated = item.copy(
                                                                        binId = bin?.name.orEmpty(),
                                                                        binName = chosenBinName
                                                                    )
                                                                    dynamicOrderItems = dynamicOrderItems.map { if (it.id == item.id) updated else it }
                                                                }
                                                            )
                                                        }
                                                    }
                                                }

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        FormLabel("Item Delivery Date")
                                                        DatePickerField(
                                                            value = item.deliveryDate,
                                                            onDateSelected = { selectedDate ->
                                                                val updated = item.copy(deliveryDate = selectedDate)
                                                                dynamicOrderItems = dynamicOrderItems.map {
                                                                    if (it.id == item.id) updated else it
                                                                }
                                                            }
                                                        )
                                                    }

                                                    Column(modifier = Modifier.weight(1f)) {
                                                        FormLabel("Fabric Instructions")
                                                        FormTextField(
                                                            value = item.instructions,
                                                            onValueChange = { text ->
                                                                val updated = item.copy(instructions = text)
                                                                dynamicOrderItems = dynamicOrderItems.map {
                                                                    if (it.id == item.id) updated else it
                                                                }
                                                            },
                                                            placeholder = "Cut continuous meters from roll. Ensure edge selvedge is clean..."
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        is DynamicAccessoryItem -> {
                                            Column(
                                                modifier = Modifier.padding(top = 14.dp),
                                                verticalArrangement = Arrangement.spacedBy(14.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                ) {
                                                    val accessoryOptions = remember(accessoryItemList) {
                                                        accessoryItemList.map { inv ->
                                                            if (inv.sku.isNotBlank()) "${inv.name} (${inv.sku})" else inv.name
                                                        }
                                                    }
                                                    val accessoryMap = remember(accessoryItemList) {
                                                        accessoryItemList.associateBy { inv ->
                                                            if (inv.sku.isNotBlank()) "${inv.name} (${inv.sku})" else inv.name
                                                        }
                                                    }

                                                    var accSelectOpen by remember { mutableStateOf(false) }

                                                    Column(modifier = Modifier.weight(1.4f)) {
                                                        Row(Modifier.fillMaxWidth()) {
                                                            if (item.selection.isNotBlank()) {
                                                                Surface(
                                                                    shape = RoundedCornerShape(4.dp),
                                                                    color = activity_green_bg,
                                                                    border = BorderStroke(1.dp, greenBg)
                                                                ) {
                                                                    Text(
                                                                        text = "In Stock: ${item.stockCount.toInt()} Pieces",
                                                                        color = darkGreenBg,
                                                                        fontSize = tokens.label,
                                                                        fontWeight = FontWeight.Medium,
                                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                                    )
                                                                }
                                                            }
                                                        }
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            FormLabel("Item Selection", isRequired = true)
                                                        }

                                                        Spacer(Modifier.height(4.dp))

                                                        FormDropdown(
                                                            label = "",
                                                            value = item.selection.ifBlank { "Select Accessory" },
                                                            expanded = accSelectOpen,
                                                            onExpandChange = { accSelectOpen = it },
                                                            options = accessoryOptions.ifEmpty { listOf("Loading accessories...") },
                                                            onOptionSelected = { chosenDisplay ->
                                                                val selectedInv = accessoryMap[chosenDisplay]
                                                                val isOut = (selectedInv?.currentStock ?: 0.0) <= 0.0
                                                                val updated = item.copy(
                                                                    itemId = selectedInv?.id ?: selectedInv?._id.orEmpty(),
                                                                    selection = chosenDisplay,
                                                                    stockCount = selectedInv?.currentStock ?: 0.0,
                                                                    isOutOfStock = isOut,
                                                                    unit = if (!selectedInv?.unit.isNullOrBlank()) "${selectedInv.unit} (Pcs)" else item.unit
                                                                )
                                                                dynamicOrderItems = dynamicOrderItems.map { orderItem ->
                                                                    if (orderItem.id == item.id) updated else orderItem
                                                                }
                                                            },
                                                            isRequired = true
                                                        )
                                                    }

                                                    Column(modifier = Modifier.weight(1f)) {
                                                        FormLabel("Quantity", isRequired = true)
                                                        FormTextField(
                                                            value = item.quantity,
                                                            onValueChange = { qty ->
                                                                val updated = item.copy(quantity = qty)
                                                                dynamicOrderItems = dynamicOrderItems.map {
                                                                    if (it.id == item.id) updated else it
                                                                }
                                                            },
                                                            placeholder = "1",
                                                            keyboardType = KeyboardType.Number
                                                        )
                                                    }

                                                    var accUnitOpen by remember { mutableStateOf(false) }
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        FormDropdown(
                                                            label = "Unit",
                                                            isRequired = true,
                                                            value = item.unit.ifBlank { "Pieces (Pcs)" },
                                                            expanded = accUnitOpen,
                                                            onExpandChange = { accUnitOpen = it },
                                                            options = listOf("Pieces (Pcs)", "Spool", "Packet", "Set", "Meter (m)"),
                                                            onOptionSelected = { selectedUnit ->
                                                                val updated = item.copy(unit = selectedUnit)
                                                                dynamicOrderItems = dynamicOrderItems.map {
                                                                    if (it.id == item.id) updated else it
                                                                }
                                                            }
                                                        )
                                                    }
                                                }

                                                if (item.selection.isNotBlank()) {
                                                    var whOpen by remember { mutableStateOf(false) }
                                                    var binOpen by remember { mutableStateOf(false) }

                                                    val whOptions: List<String> = remember(warehouseDropdownItems) {
                                                        warehouseDropdownItems.map { w ->
                                                            val codeStr = w.value
                                                            if (codeStr.isNotBlank()) "${w.label} ($codeStr)" else w.label
                                                        }
                                                    }
                                                    val whMap = remember(warehouseDropdownItems) {
                                                        warehouseDropdownItems.associateBy { w ->
                                                            val codeStr = w.value
                                                            if (codeStr.isNotBlank()) "${w.label} ($codeStr)" else w.label
                                                        }
                                                    }

                                                    val binOptions: List<String> = remember(binDropdownItems) {
                                                        binDropdownItems.map { b ->
                                                            val codeStr = b.code
                                                            if (codeStr.isNotBlank()) "${b.name} ($codeStr)" else b.name
                                                        }
                                                    }
                                                    val binMap = remember(binDropdownItems) {
                                                        binDropdownItems.associateBy { b ->
                                                            val codeStr = b.code
                                                            if (codeStr.isNotBlank()) "${b.name} ($codeStr)" else b.name
                                                        }
                                                    }

                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                    ) {
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            val whDisplayValue = item.warehouseName.ifBlank { "Select Warehouse" }
                                                            FormDropdown(
                                                                label = "Pick Warehouse",
                                                                isRequired = true,
                                                                value = whDisplayValue,
                                                                expanded = whOpen,
                                                                onExpandChange = { whOpen = it },
                                                                options = whOptions.ifEmpty { listOf("Loading warehouses...") },
                                                                onOptionSelected = { chosenWhName ->
                                                                    val wh = whMap[chosenWhName]
                                                                    val whId = wh?.value.orEmpty()
                                                                    if (whId.isNotBlank()) {
                                                                        inventoryViewModel.loadBinDropdown(whId)
                                                                    }
                                                                    val updated = item.copy(
                                                                        warehouseId = whId,
                                                                        warehouseName = chosenWhName,
                                                                        binId = "",
                                                                        binName = ""
                                                                    )
                                                                    dynamicOrderItems = dynamicOrderItems.map { if (it.id == item.id) updated else it }
                                                                }
                                                            )
                                                        }

                                                        Column(modifier = Modifier.weight(1f)) {
                                                            val binPlaceholder = if (item.warehouseId.isBlank()) "Select warehouse first" else "Select Bin"
                                                            val binDisplayValue = item.binName.ifBlank { binPlaceholder }

                                                            FormDropdown(
                                                                label = "Pick Bin",
                                                                isRequired = true,
                                                                value = binDisplayValue,
                                                                expanded = binOpen,
                                                                onExpandChange = {
                                                                    if (item.warehouseId.isNotBlank()) binOpen = it
                                                                },
                                                                options = if (item.warehouseId.isBlank()) {
                                                                    listOf("Select warehouse first")
                                                                } else binOptions.ifEmpty {
                                                                    listOf("No bins available")
                                                                },
                                                                onOptionSelected = { chosenBinName ->
                                                                    val bin = binMap[chosenBinName]
                                                                    val updated = item.copy(
                                                                        binId = bin?.id.orEmpty(),
                                                                        binName = chosenBinName
                                                                    )
                                                                    dynamicOrderItems = dynamicOrderItems.map { if (it.id == item.id) updated else it }
                                                                }
                                                            )
                                                        }
                                                    }
                                                }

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        FormLabel("Item Delivery Date")
                                                        DatePickerField(
                                                            value = item.deliveryDate,
                                                            onDateSelected = { selectedDate ->
                                                                val updated = item.copy(deliveryDate = selectedDate)
                                                                dynamicOrderItems = dynamicOrderItems.map {
                                                                    if (it.id == item.id) updated else it
                                                                }
                                                            }
                                                        )
                                                    }

                                                    Column(modifier = Modifier.weight(1f)) {
                                                        FormLabel("Accessory Instructions")
                                                        FormTextField(
                                                            value = item.instructions,
                                                            onValueChange = { text ->
                                                                val updated = item.copy(instructions = text)
                                                                dynamicOrderItems = dynamicOrderItems.map {
                                                                    if (it.id == item.id) updated else it
                                                                }
                                                            },
                                                            placeholder = "Matching thread for fabric roll #B-402 for collar..."
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ADD NEW ITEM BUTTON & POPUP MENU
                    var showAddItemMenu by remember { mutableStateOf(false) }

                    Box(modifier = Modifier.fillMaxWidth()) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(tokens.buttonHeight)
                                .border(
                                    BorderStroke(1.5.dp, Primary.copy(alpha = 0.5f)),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { showAddItemMenu = true },
                            shape = RoundedCornerShape(12.dp),
                            color = light_blue
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.Add, null, tint = Primary, modifier = Modifier.size(tokens.iconSize))
                                Spacer(Modifier.width(6.dp))
                                Text("Add New Item", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium, color = Primary)
                                Spacer(Modifier.width(6.dp))
                                Icon(Icons.Default.KeyboardArrowDown, null, tint = Primary, modifier = Modifier.size(tokens.iconSize))
                            }
                        }

                        DropdownMenu(
                            expanded = showAddItemMenu,
                            onDismissRequest = { showAddItemMenu = false },
                            modifier = Modifier
                                .background(whiteBg)
                                .border(BorderStroke(1.dp, BorderGray), RoundedCornerShape(14.dp)),
                            shape = RoundedCornerShape(14.dp),
                            shadowElevation = 8.dp
                        ) {
                            DropdownMenuItem(
                                text = { Text("Add Garment", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium, color = TextPrimary) },
                                leadingIcon = {
                                    Icon(Icons.Default.Checkroom, null, tint = Primary, modifier = Modifier.size(tokens.iconSize))
                                },
                                onClick = {
                                    showAddItemMenu = false
                                    dynamicOrderItems = dynamicOrderItems + DynamicGarmentItem(isItemExpanded = true)
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Add Fabric", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium, color = TextPrimary) },
                                leadingIcon = {
                                    Icon(Icons.Default.Texture, null, tint = LocalTealBadgeColor, modifier = Modifier.size(tokens.iconSize))
                                },
                                onClick = {
                                    showAddItemMenu = false
                                    dynamicOrderItems = dynamicOrderItems + DynamicFabricItem(isItemExpanded = true)
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Add Stitching", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium, color = TextPrimary) },
                                leadingIcon = {
                                    Icon(Icons.Default.ContentCut, null, tint = darkGreenBg, modifier = Modifier.size(tokens.iconSize))
                                },
                                onClick = {
                                    showAddItemMenu = false
                                    dynamicOrderItems = dynamicOrderItems + DynamicAccessoryItem(isItemExpanded = true)
                                }
                            )
                        }
                    }
                }

                // 4. PRICING & CHARGES
                AccordionSection(
                    title = "4. Pricing & Charges",
                    expanded = expandedSection == "pricing",
                    onHeaderClick = { expandedSection = if (expandedSection == "pricing") "" else "pricing" }
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(tokens.cardCornerRadius),
                        colors = CardDefaults.cardColors(containerColor = whiteBg),
                        border = BorderStroke(1.dp, BorderGray)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(primary_light),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "4",
                                            fontSize = tokens.bodySmall,
                                            color = Primary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    Text(
                                        text = "Pricing & Charges",
                                        fontSize = tokens.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimary
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = activity_green_bg,
                                    border = BorderStroke(1.dp, greenBg)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(darkGreenBg)
                                        )
                                        Text(
                                            text = "Live Tax Engine",
                                            fontSize = tokens.label,
                                            fontWeight = FontWeight.Medium,
                                            color = activity_green
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(14.dp))

                            var dynamicSubtotal = 0.0
                            var dynamicTaxTotal = 0.0

                            dynamicOrderItems.forEachIndexed { index, orderItem ->
                                val itemIndexNumber = index + 1

                                when (orderItem) {
                                    is DynamicGarmentItem -> {
                                        val garmentConfig = orderItem.config

                                        val stitchingRate = garmentConfig.stitchingPrice
                                        val stitchingQty = garmentConfig.quantity
                                        val stitchingLineSubtotal = stitchingRate * stitchingQty
                                        val stitchingGstAmount = stitchingLineSubtotal * (garmentConfig.stitchingGstRate / 100.0)

                                        val isStoreFabric = garmentConfig.fabricSource.equals("Store Provided", ignoreCase = true)
                                        val fabricUnitPrice = if (isStoreFabric) garmentConfig.fabricRate else 0.0
                                        val fabricMeters = if (isStoreFabric) garmentConfig.fabricMeters else 0.0
                                        val fabricLineSubtotal = fabricUnitPrice * fabricMeters
                                        val fabricGstAmount = fabricLineSubtotal * (garmentConfig.fabricGstRate / 100.0)

                                        val selectedWorksList = remember(garmentConfig.selectedWorkPricingIds, workPricingList) {
                                            workPricingList.filter { garmentConfig.selectedWorkPricingIds.contains(it.id) }
                                        }
                                        val addlWorkTotal = selectedWorksList.sumOf { garmentConfig.customWorkPrices[it.id] ?: it.basePrice }
                                        val addlWorkGstAmount = selectedWorksList.sumOf {
                                            val price = garmentConfig.customWorkPrices[it.id] ?: it.basePrice
                                            val gstRate = garmentConfig.customWorkGstRates[it.id] ?: 5.0
                                            price * (gstRate / 100.0)
                                        }

                                        val lineTotalBeforeTax = stitchingLineSubtotal + fabricLineSubtotal + addlWorkTotal
                                        val lineTotalTax = stitchingGstAmount + fabricGstAmount + addlWorkGstAmount
                                        val lineGrandTotal = lineTotalBeforeTax + lineTotalTax

                                        dynamicSubtotal += lineTotalBeforeTax
                                        dynamicTaxTotal += lineTotalTax

                                        var isCardExpanded by rememberSaveable(orderItem.id) { mutableStateOf(true) }

                                        val rawGarmentName = garmentConfig.garmentCategory.ifBlank { garmentConfig.garmentType }
                                        val displayGarmentTitle = if (!tokens.isTablet && rawGarmentName.length > 15) {
                                            "${rawGarmentName.take(15)}..."
                                        } else {
                                            rawGarmentName
                                        }

                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 12.dp),
                                            shape = RoundedCornerShape(tokens.cardCornerRadius),
                                            color = whiteBg,
                                            border = BorderStroke(1.dp, BorderGray)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(12.dp),
                                                verticalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(
                                                        modifier = Modifier.weight(1f, fill = false),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        Text(
                                                            text = "#$itemIndexNumber",
                                                            fontSize = tokens.bodySmall,
                                                            color = TextSecondary,
                                                            fontWeight = FontWeight.Medium
                                                        )

                                                        Text(
                                                            text = displayGarmentTitle.ifBlank { "Garment" },
                                                            color = TextPrimary,
                                                            fontSize = tokens.bodySmall,
                                                            fontWeight = FontWeight.Medium,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )

                                                        QuantityStepper(
                                                            quantity = garmentConfig.quantity,
                                                            onMinus = {
                                                                if (garmentConfig.quantity > 1) {
                                                                    val updated = garmentConfig.copy(quantity = garmentConfig.quantity - 1)
                                                                    dynamicOrderItems = dynamicOrderItems.map {
                                                                        if (it.id == orderItem.id) orderItem.copy(config = updated) else it
                                                                    }
                                                                }
                                                            },
                                                            onPlus = {
                                                                val updated = garmentConfig.copy(quantity = garmentConfig.quantity + 1)
                                                                dynamicOrderItems = dynamicOrderItems.map {
                                                                    if (it.id == orderItem.id) orderItem.copy(config = updated) else it
                                                                }
                                                            }
                                                        )
                                                    }

                                                    Row(
                                                        modifier = Modifier.clickable { isCardExpanded = !isCardExpanded },
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        Text(
                                                            text = "₹${String.format(Locale.US, "%.2f", lineGrandTotal)}",
                                                            fontSize = tokens.bodyLarge,
                                                            fontWeight = FontWeight.Medium,
                                                            color = Primary,
                                                            maxLines = 1
                                                        )
                                                        Icon(
                                                            imageVector = if (isCardExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                                            contentDescription = null,
                                                            tint = Primary,
                                                            modifier = Modifier.size(tokens.iconSize)
                                                        )
                                                    }
                                                }

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    QuickPriceBox("Stitching", "₹${stitchingLineSubtotal.toInt()}", Modifier.weight(1f))
                                                    QuickPriceBox("Fabric", "₹${fabricLineSubtotal.toInt()}", Modifier.weight(1f))
                                                    QuickPriceBox(
                                                        label = "Addl. Work",
                                                        value = "₹${addlWorkTotal.toInt()}",
                                                        modifier = Modifier.weight(1.1f),
                                                        bg = primary_light,
                                                        tint = Primary
                                                    )
                                                    QuickPriceBox(
                                                        label = "GST",
                                                        value = "₹${lineTotalTax.toInt()}",
                                                        modifier = Modifier.weight(0.9f),
                                                        tint = darkGreenBg
                                                    )
                                                }

                                                AnimatedVisibility(visible = isCardExpanded) {
                                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                                        Surface(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            shape = RoundedCornerShape(8.dp),
                                                            color = badgeGrey
                                                        ) {
                                                            Row(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .padding(horizontal = 8.dp, vertical = 7.dp),
                                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                Text(
                                                                    text = "Item Breakdown",
                                                                    fontSize = tokens.caption,
                                                                    fontWeight = FontWeight.Medium,
                                                                    color = TextPrimary
                                                                )
                                                                Surface(
                                                                    shape = RoundedCornerShape(4.dp),
                                                                    color = primary_light
                                                                ) {
                                                                    Text(
                                                                        text = "Active Source of Truth",
                                                                        fontSize = tokens.label,
                                                                        color = Primary,
                                                                        fontWeight = FontWeight.Medium,
                                                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                                    )
                                                                }
                                                            }
                                                        }

                                                        if (isStoreFabric) {
                                                            EditableBreakdownItemCard(
                                                                tag = "Fabric",
                                                                title = garmentConfig.fabricSelection.ifBlank { "Store Fabric" },
                                                                qtyLabel = "Qty (Meters)",
                                                                qtyValue = if (garmentConfig.fabricMeters == 0.0) "" else garmentConfig.fabricMeters.toString(),
                                                                onQtyChange = { input ->
                                                                    val parsedMeters = input.toDoubleOrNull() ?: 0.0
                                                                    val updated = garmentConfig.copy(fabricMeters = parsedMeters)
                                                                    dynamicOrderItems = dynamicOrderItems.map { if (it.id == orderItem.id) orderItem.copy(config = updated) else it }
                                                                },
                                                                rateLabel = "Rate (₹/m)",
                                                                rateValue = if (garmentConfig.fabricRate == 0.0) "" else garmentConfig.fabricRate.toInt().toString(),
                                                                onRateChange = { input ->
                                                                    val parsedRate = input.toDoubleOrNull() ?: 0.0
                                                                    val updated = garmentConfig.copy(fabricRate = parsedRate)
                                                                    dynamicOrderItems = dynamicOrderItems.map { if (it.id == orderItem.id) orderItem.copy(config = updated) else it }
                                                                },
                                                                selectedGstRate = garmentConfig.fabricGstRate,
                                                                taxGroups = taxGroups,
                                                                onGstRateChange = { parsedGst ->
                                                                    val updated = garmentConfig.copy(fabricGstRate = parsedGst)
                                                                    dynamicOrderItems = dynamicOrderItems.map { if (it.id == orderItem.id) orderItem.copy(config = updated) else it }
                                                                },
                                                                onDropdownOpen = { inventoryViewModel.fetchTaxGroups() },
                                                                gstComputed = "₹${fabricGstAmount.toInt()}",
                                                                lineTotal = "₹${(fabricLineSubtotal + fabricGstAmount).toInt()}",
                                                                onDelete = {
                                                                    val updated = garmentConfig.copy(fabricSource = "Customer Provided", fabricRate = 0.0)
                                                                    dynamicOrderItems = dynamicOrderItems.map { if (it.id == orderItem.id) orderItem.copy(config = updated) else it }
                                                                }
                                                            )
                                                        }

                                                        EditableBreakdownItemCard(
                                                            tag = "Stitching",
                                                            title = "${garmentConfig.garmentType.ifBlank { "Garment" }} Custom Stitching",
                                                            qtyLabel = "Qty (Pcs)",
                                                            qtyValue = garmentConfig.quantity.toString(),
                                                            onQtyChange = { input ->
                                                                val parsedQty = input.filter { it.isDigit() }.toIntOrNull() ?: 1
                                                                val updated = garmentConfig.copy(quantity = parsedQty)
                                                                dynamicOrderItems = dynamicOrderItems.map { if (it.id == orderItem.id) orderItem.copy(config = updated) else it }
                                                            },
                                                            rateLabel = "Rate (₹/pc)",
                                                            rateValue = if (garmentConfig.stitchingPrice == 0.0) "" else garmentConfig.stitchingPrice.toInt().toString(),
                                                            onRateChange = { input ->
                                                                val parsedPrice = input.toDoubleOrNull() ?: 0.0
                                                                val updated = garmentConfig.copy(stitchingPrice = parsedPrice)
                                                                dynamicOrderItems = dynamicOrderItems.map { if (it.id == orderItem.id) orderItem.copy(config = updated) else it }
                                                            },
                                                            selectedGstRate = garmentConfig.stitchingGstRate,
                                                            taxGroups = taxGroups,
                                                            onGstRateChange = { parsedGst ->
                                                                val updated = garmentConfig.copy(stitchingGstRate = parsedGst)
                                                                dynamicOrderItems = dynamicOrderItems.map { if (it.id == orderItem.id) orderItem.copy(config = updated) else it }
                                                            },
                                                            onDropdownOpen = { inventoryViewModel.fetchTaxGroups() },
                                                            gstComputed = "₹${stitchingGstAmount.toInt()}",
                                                            lineTotal = "₹${(stitchingLineSubtotal + stitchingGstAmount).toInt()}",
                                                            onDelete = {
                                                                val updated = garmentConfig.copy(stitchingPrice = 0.0)
                                                                dynamicOrderItems = dynamicOrderItems.map { if (it.id == orderItem.id) orderItem.copy(config = updated) else it }
                                                            }
                                                        )

                                                        if (selectedWorksList.isNotEmpty()) {
                                                            Surface(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                shape = RoundedCornerShape(12.dp),
                                                                color = modelBg,
                                                                border = BorderStroke(1.dp, sectionBorder)
                                                            ) {
                                                                Column(
                                                                    modifier = Modifier.padding(10.dp),
                                                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                                                ) {
                                                                    Row(
                                                                        modifier = Modifier.fillMaxWidth(),
                                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                                        verticalAlignment = Alignment.CenterVertically
                                                                    ) {
                                                                        Text(
                                                                            text = "Additional Work (${selectedWorksList.size})",
                                                                            fontSize = tokens.caption,
                                                                            fontWeight = FontWeight.Medium,
                                                                            color = TextPrimary
                                                                        )
                                                                    }

                                                                    selectedWorksList.forEach { workItem ->
                                                                        val currentAmount = garmentConfig.customWorkPrices[workItem.id] ?: workItem.basePrice
                                                                        val currentGstRate = garmentConfig.customWorkGstRates[workItem.id] ?: 5.0
                                                                        val workGst = currentAmount * (currentGstRate / 100.0)

                                                                        EditableCraftLineItemCard(
                                                                            title = workItem.workType,
                                                                            unitLabel = "Unit",
                                                                            unitValue = "1 piece",
                                                                            amount = if (currentAmount == 0.0) "" else currentAmount.toInt().toString(),
                                                                            onAmountChange = { input ->
                                                                                val parsedAmount = input.toDoubleOrNull() ?: 0.0
                                                                                val updatedMap = garmentConfig.customWorkPrices + (workItem.id to parsedAmount)
                                                                                val updated = garmentConfig.copy(customWorkPrices = updatedMap)
                                                                                dynamicOrderItems = dynamicOrderItems.map { if (it.id == orderItem.id) orderItem.copy(config = updated) else it }
                                                                            },
                                                                            selectedGstRate = currentGstRate,
                                                                            taxGroups = taxGroups,
                                                                            onGstRateChange = { parsedGst ->
                                                                                val updatedGstMap = garmentConfig.customWorkGstRates + (workItem.id to parsedGst)
                                                                                val updated = garmentConfig.copy(customWorkGstRates = updatedGstMap)
                                                                                dynamicOrderItems = dynamicOrderItems.map { if (it.id == orderItem.id) orderItem.copy(config = updated) else it }
                                                                            },
                                                                            onDropdownOpen = { inventoryViewModel.fetchTaxGroups() },
                                                                            gstComputed = "₹${workGst.toInt()}",
                                                                            total = "₹${(currentAmount + workGst).toInt()}",
                                                                            onDelete = {
                                                                                val updatedIds = garmentConfig.selectedWorkPricingIds - workItem.id
                                                                                val updated = garmentConfig.copy(selectedWorkPricingIds = updatedIds)
                                                                                dynamicOrderItems = dynamicOrderItems.map { if (it.id == orderItem.id) orderItem.copy(config = updated) else it }
                                                                            }
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    is DynamicFabricItem -> {
                                        val fabricItem = fabricPricingList.find { it.name == orderItem.selection }
                                        val unitPrice = fabricItem?.sellingPrice ?: 0.0
                                        val qty = orderItem.quantity.toDoubleOrNull() ?: 1.0
                                        val lineSubtotal = unitPrice * qty
                                        val gstAmount = lineSubtotal * 0.05
                                        val lineTotal = lineSubtotal + gstAmount

                                        dynamicSubtotal += lineSubtotal
                                        dynamicTaxTotal += gstAmount

                                        CollapsedPricingItemCardExact(
                                            number = "#$itemIndexNumber",
                                            title = orderItem.selection.ifBlank { "Fabric Material" },
                                            quantity = qty.toInt().coerceAtLeast(1),
                                            containerColor = whiteBg,
                                            borderColor = BorderGray,
                                            onMinus = {
                                                if (qty > 1.0) {
                                                    val updated = orderItem.copy(quantity = "${(qty - 1).toInt()}")
                                                    dynamicOrderItems = dynamicOrderItems.map { if (it.id == orderItem.id) updated else it }
                                                }
                                            },
                                            onPlus = {
                                                val updated = orderItem.copy(quantity = "${(qty + 1).toInt()}")
                                                dynamicOrderItems = dynamicOrderItems.map { if (it.id == orderItem.id) updated else it }
                                            },
                                            price = "₹${String.format(Locale.US, "%.2f", lineTotal)}",
                                            stitching = "₹0",
                                            fabric = "₹${lineSubtotal.toInt()}",
                                            addlWork = null,
                                            gst = "₹${gstAmount.toInt()}",
                                            onDelete = {
                                                dynamicOrderItems = dynamicOrderItems.filterNot { it.id == orderItem.id }
                                            }
                                        )
                                        Spacer(Modifier.height(10.dp))
                                    }

                                    is DynamicAccessoryItem -> {
                                        val unitPrice = 150.0
                                        val qty = orderItem.quantity.toIntOrNull() ?: 1
                                        val lineSubtotal = unitPrice * qty
                                        val gstAmount = lineSubtotal * 0.05
                                        val lineTotal = lineSubtotal + gstAmount

                                        dynamicSubtotal += lineSubtotal
                                        dynamicTaxTotal += gstAmount

                                        CollapsedPricingItemCardExact(
                                            number = "#$itemIndexNumber",
                                            title = orderItem.selection.ifBlank { "Accessory Item" },
                                            quantity = qty,
                                            containerColor = whiteBg,
                                            borderColor = BorderGray,
                                            onMinus = {
                                                if (qty > 1) {
                                                    val updated = orderItem.copy(quantity = "${qty - 1}")
                                                    dynamicOrderItems = dynamicOrderItems.map { if (it.id == orderItem.id) updated else it }
                                                }
                                            },
                                            onPlus = {
                                                val updated = orderItem.copy(quantity = "${qty + 1}")
                                                dynamicOrderItems = dynamicOrderItems.map { if (it.id == orderItem.id) updated else it }
                                            },
                                            price = "₹${String.format(Locale.US, "%.2f", lineTotal)}",
                                            stitching = "₹${lineSubtotal.toInt()}",
                                            fabric = "₹0",
                                            addlWork = null,
                                            gst = "₹${gstAmount.toInt()}",
                                            onDelete = {
                                                dynamicOrderItems = dynamicOrderItems.filterNot { it.id == orderItem.id }
                                            }
                                        )
                                        Spacer(Modifier.height(10.dp))
                                    }
                                }
                            }

                            Spacer(Modifier.height(14.dp))
                            Text(
                                text = "Currency: INR (₹) • GST: Standard live tax engine applied",
                                fontSize = tokens.label,
                                color = TextSecondary
                            )

                            Spacer(Modifier.height(10.dp))
                            HorizontalDivider(color = BorderGray)
                            Spacer(Modifier.height(10.dp))
                            FormLabel("Total Discount (₹)")
                            FormTextField(
                                value = discountText,
                                onValueChange = { discountText = it.filter { c -> c.isDigit() || c == '.' } },
                                placeholder = "0",
                                keyboardType = KeyboardType.Number
                            )
                            Spacer(Modifier.height(10.dp))

                            val grandTotalCalculated = (dynamicSubtotal - totalDiscount + dynamicTaxTotal).coerceAtLeast(0.0)

                            SummaryRow("Subtotal", "₹${String.format(Locale.US, "%.2f", dynamicSubtotal)}")
                            Spacer(Modifier.height(6.dp))
                            SummaryRow("Total Discount", "-₹${String.format(Locale.US, "%.2f", totalDiscount)}", valueColor = redText)
                            Spacer(Modifier.height(6.dp))
                            SummaryRow("Total Tax (GST)", "+₹${String.format(Locale.US, "%.2f", dynamicTaxTotal)}")
                            Spacer(Modifier.height(6.dp))
                            SummaryRow("Delivery Charges", "₹0.00")

                            Spacer(Modifier.height(12.dp))
                            HorizontalDivider(color = BorderGray)
                            Spacer(Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "GRAND TOTAL",
                                    fontSize = tokens.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "₹${String.format(Locale.US, "%.2f", grandTotalCalculated)}",
                                    fontSize = tokens.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = Primary
                                )
                            }
                        }
                    }
                }

                // 5. PAYMENT & BILLING PREFERENCE
                AccordionSection(
                    title = "5. Payment & Billing Preference",
                    expanded = expandedSection == "payment",
                    onHeaderClick = { expandedSection = if (expandedSection == "payment") "" else "payment" }
                ) {
                    FormDropdown(
                        label = "Payment Type",
                        value = paymentType.ifEmpty { "" },
                        expanded = paymentTypeExpanded,
                        onExpandChange = { paymentTypeExpanded = it },
                        options = listOf("Advance", "Without_Payment"),
                        onOptionSelected = { paymentType = it },
                        isRequired = true
                    )
                    if (paymentType == "Advance") {
                        Spacer(Modifier.height(14.dp))

                        FormLabel("Advance Amount Required", isRequired = true)
                        FormTextField(
                            value = advanceAmount,
                            onValueChange = { advanceAmount = it },
                            placeholder = "Enter Payment"
                        )
                    }
                }

                // 6. ATTACHMENTS & IMAGES
                AccordionSection(
                    title = "6. Attachments & Images",
                    expanded = expandedSection == "attachments",
                    onHeaderClick = { expandedSection = if (expandedSection == "attachments") "" else "attachments" }
                ) {
                    ImageUploadSection(
                        selectedImages = selectedImages,
                        onBrowseClick = { imagePickerLauncher.launch("image/*") },
                        onRemoveImage = { uriToRemove ->
                            selectedImages = selectedImages.filter { it != uriToRemove }
                        },
                        maxFiles = 5,
                        title = "Order Attachments",
                        subtitle = "Upload customer design sketches, reference photos, or fabric sample images"
                    )
                }
            }

            // Floating Next Button
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 24.dp)
            ) {
                TrailingFabButton(
                    action = TrailingFabAction.Next(
                        label = "Next",
                        onClick = {
                            val validationError = validateRequiredOrderFields(
                                fullName = fullName,
                                customerType = customerType,
                                selectedBranchId = selectedBranchId,
                                orderDate = orderDate,
                                orderType = orderType,
                                salespersonId = salespersonId,
                                salespersonName = salespersonName,
                                dynamicOrderItems = dynamicOrderItems,
                                paymentType = paymentType,
                                advanceAmount = advanceAmount
                            )

                            if (validationError != null) {
                                DynamicIslandManager.showError(validationError)
                                return@Next
                            }

                            val itemsForDraft: List<OrderItemEntry> = dynamicOrderItems.map { entry ->
                                if (entry is DynamicGarmentItem && entry.config.designId.isBlank()) {
                                    entry.copy(
                                        config = entry.config.copy(
                                            designId = selectedPresetDesign.id,
                                            designPreset = entry.config.designPreset.ifBlank { selectedPresetDesign.name }
                                        )
                                    )
                                } else entry
                            }

                            val pricing = computeOrderPricing(
                                items = itemsForDraft,
                                workInfo = workInfoMap,
                                fabricPrices = fabricPriceMap,
                                discount = totalDiscount
                            )

                            OrderFlowStore.previewPayment = OrderFlowStore.previewPayment?.copy(
                                paymentAmountReceived = advanceAmount.trim().toDoubleOrNull()
                                    ?.toInt()?.toString() ?: "0"
                            )

                            OrderFlowStore.draft = OrderDraftSnapshot(
                                customerId = customerId,
                                customerCode = customerCode,
                                customerName = fullName.trim(),
                                phone = phone.trim(),
                                countryCode = countryCode,
                                email = emailAddress.trim(),
                                customerType = customerType,
                                customerAddressJson = customerAddressJson,
                                branchId = selectedBranchId,
                                branchName = selectedBranchName,
                                salespersonId = salespersonId,
                                salespersonName = salespersonName,
                                address = address.trim(),
                                orderId = orderIdText.trim(),
                                orderDate = orderDate.trim(),
                                orderType = orderType,
                                expectedDeliveryDate = expectedDeliveryDate.trim(),
                                paymentType = paymentType,
                                advanceAmount = advanceAmount.trim(),
                                items = itemsForDraft,
                                pricing = pricing,
                                taxGroupIdByRate = taxGroupIdByRate,
                                workMeta = workMetaMap
                            )

                            val reviewData = OrderReviewData(
                                leadId = initialData?.leadId,
                                orderId = orderIdText.trim(),
                                editOrderId = if (isEditMode) initialData?.orderId else null,
                                customerId = customerId.ifBlank { initialData?.customerId ?: "" },
                                branchId = selectedBranchId.ifBlank { initialData?.branchId ?: "" },
                                fullName = fullName.trim(),
                                countryCode = countryCode.ifBlank { "+91" },
                                phone = phone.trim(),
                                gender = gender.trim(),
                                dressFor = customerType.ifBlank { dressFor.trim() },
                                address = address.trim(),
                                source = source.trim(),
                                orderDate = orderDate.trim(),
                                trialDate = initialData?.trialDate.orEmpty(),
                                deliveryDate = expectedDeliveryDate.trim(),
                                discount = totalDiscount,
                                paidSoFar = advanceAmount.trim().toDoubleOrNull() ?: 0.0,
                                designImages = selectedImages,
                                existingImageUrls = existingImageUrls,
                                voiceNoteUri = recordedVoiceNoteUris.firstOrNull(),
                                garments = itemsForDraft.mapIndexedNotNull { entryIndex, entry ->
                                    if (entry is DynamicGarmentItem) {
                                        val item = entry.config
                                        val line = pricing.lines.getOrNull(entryIndex)
                                        val priceBeforeTax = if (line != null) {
                                            line.stitching + line.fabric + line.additionalWork
                                        } else {
                                            item.totalItemPrice
                                        }

                                        SelectedGarment(
                                            category = item.garmentType.trim(),
                                            categoryName = item.garmentCategory.trim()
                                                .ifBlank { item.garmentType.trim() },
                                            categoryId = item.garmentCategoryId.ifBlank { item.garmentType.trim() },
                                            quantity = item.quantity,
                                            price = priceBeforeTax,
                                            priority = priority.trim(),
                                            trialRequired = item.trialRequired,
                                            fabricSource = item.fabricSource.trim(),
                                            fabricType = item.fabricSelection.trim(),
                                            colorTone = item.colorAccent.trim(),
                                            pattern = item.designPreset.trim(),
                                            models = emptyList()
                                        )
                                    } else null
                                }
                            )

                            onNextStep(reviewData)
                        }
                    )
                )
            }

            if (showSelectDesignDialog) {
                val targetId = designTargetItemId.ifBlank {
                    dynamicOrderItems.firstOrNull { it is DynamicGarmentItem }?.id.orEmpty()
                }
                val targetGarment = dynamicOrderItems.firstOrNull { it.id == targetId } as? DynamicGarmentItem

                SelectDynamicDesignDialog(
                    designs = designsList,
                    currentSelectedId = targetGarment?.config?.designId?.ifBlank { selectedPresetDesign.id }
                        ?: selectedPresetDesign.id,
                    onDismiss = { showSelectDesignDialog = false },
                    onDesignSelected = { design ->
                        dynamicOrderItems = dynamicOrderItems.map { entry ->
                            if (entry.id == targetId && entry is DynamicGarmentItem) {
                                entry.copy(
                                    config = entry.config.copy(
                                        designPreset = design.name,
                                        designId = design.id
                                    )
                                )
                            } else entry
                        }
                        showSelectDesignDialog = false
                    }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// DIALOG & HELPER COMPOSABLES
// ─────────────────────────────────────────────────────────────

@Composable
fun DiscardOrderConfirmDialog(
    onConfirmDiscard: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Quit Order Creation?", fontWeight = FontWeight.Bold) },
        text = { Text("Would you like to quit from Create Order? All unsaved data in this order will be cleared.") },
        confirmButton = {
            Button(
                onClick = onConfirmDiscard,
                colors = ButtonDefaults.buttonColors(containerColor = redText)
            ) {
                Text("Discard", color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun SelectDynamicDesignDialog(
    designs: List<DesignItem>,
    currentSelectedId: String,
    onDismiss: () -> Unit,
    onDesignSelected: (DesignItem) -> Unit
) {
    val tokens = LocalAppTokens.current
    var searchQuery by remember { mutableStateOf("") }
    var tempSelectedId by remember { mutableStateOf(currentSelectedId) }
    var selectedCategoryFilter by remember { mutableStateOf("All") }

    val categoryCounts = remember(designs) {
        val counts = mutableMapOf<String, Int>()
        designs.forEach { design ->
            val type = design.designType.trim()
            if (type.isNotBlank()) {
                counts[type] = (counts[type] ?: 0) + 1
            }
        }
        counts
    }

    val filterChips = remember(designs, categoryCounts) {
        listOf("All" to designs.size) + categoryCounts.map { it.key to it.value }
    }

    val filteredDesigns = remember(designs, searchQuery, selectedCategoryFilter) {
        designs.filter { design ->
            val matchesCategory = if (selectedCategoryFilter == "All") {
                true
            } else {
                design.designType.equals(selectedCategoryFilter, ignoreCase = true)
            }

            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else {
                design.name.contains(searchQuery, ignoreCase = true) ||
                        design.designType.contains(searchQuery, ignoreCase = true) ||
                        design.code.contains(searchQuery, ignoreCase = true)
            }

            matchesCategory && matchesSearch
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.90f)
                .clip(RoundedCornerShape(tokens.cardCornerRadius)),
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Select Design",
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                        Text(
                            text = "Choose from the saved designs in Design Setup.",
                            fontSize = tokens.caption,
                            color = TextSecondary
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = iconMuted,
                            modifier = Modifier.size(tokens.iconSize)
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = "Search designs by name, type, or code...",
                            fontSize = tokens.bodySmall,
                            color = mutedText
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = iconMuted,
                            modifier = Modifier.size(tokens.iconSize)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(tokens.fieldHeight),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = sectionBorder,
                        focusedContainerColor = whiteBg,
                        unfocusedContainerColor = whiteBg
                    ),
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                )

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    filterChips.forEach { (categoryName, count) ->
                        val isSelected = selectedCategoryFilter == categoryName
                        val labelText = "$categoryName ($count)"

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) primary_light else Color.Transparent,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) Primary.copy(alpha = 0.5f) else Color.Transparent
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedCategoryFilter = categoryName }
                        ) {
                            Text(
                                text = labelText,
                                fontSize = tokens.caption,
                                fontWeight = FontWeight.Medium,
                                color = if (isSelected) Primary else TextSecondary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                if (filteredDesigns.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No designs found.",
                            fontSize = tokens.bodySmall,
                            color = TextSecondary
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredDesigns, key = { it.id }) { design ->
                            val isSelected = tempSelectedId == design.id

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { tempSelectedId = design.id },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) primary_light.copy(alpha = 0.25f) else whiteBg,
                                border = BorderStroke(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) Primary else BorderGray
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(grey_border),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val designImg = design.imageUrl
                                        if (!designImg.isNullOrBlank()) {
                                            AsyncImage(
                                                model = designImg,
                                                contentDescription = design.name,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.Checkroom,
                                                contentDescription = null,
                                                tint = if (isSelected) Primary else TextSecondary,
                                                modifier = Modifier.size(tokens.iconSize)
                                            )
                                        }
                                    }

                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = design.name,
                                                fontSize = tokens.bodyMedium,
                                                fontWeight = FontWeight.Medium,
                                                color = TextPrimary
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(darkGreenBg)
                                            )
                                            Text(
                                                text = design.status.ifBlank { "Active" },
                                                fontSize = tokens.label,
                                                color = TextSecondary
                                            )
                                        }

                                        Text(
                                            text = "${design.designType} • ${design.code}",
                                            fontSize = tokens.caption,
                                            color = TextSecondary
                                        )

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = if (isSelected) "Selected" else "Click to Select",
                                                fontSize = tokens.label,
                                                fontWeight = FontWeight.Medium,
                                                color = if (isSelected) Primary else TextSecondary
                                            )
                                            Text(
                                                text = "• View Details",
                                                fontSize = tokens.label,
                                                color = TextSecondary
                                            )
                                        }
                                    }

                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(22.dp)
                                                .clip(CircleShape)
                                                .background(Primary),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = whiteBg,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(22.dp)
                                                .clip(CircleShape)
                                                .border(1.5.dp, iconMuted, CircleShape)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = sectionBorder)
                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(tokens.buttonHeight),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, sectionBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                    ) {
                        Text(
                            text = "Cancel",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Button(
                        onClick = {
                            val selected = designs.firstOrNull { it.id == tempSelectedId }
                            if (selected != null) onDesignSelected(selected)
                        },
                        modifier = Modifier
                            .weight(1.4f)
                            .height(tokens.buttonHeight),
                        enabled = tempSelectedId.isNotBlank(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary,
                            disabledContainerColor = Primary.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = whiteBg,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Select Design",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = whiteBg
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuantityStepper(
    quantity: Int,
    onMinus: () -> Unit,
    onPlus: () -> Unit
) {
    val tokens = LocalAppTokens.current
    Surface(
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, iconMuted),
        color = whiteBg
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "-",
                fontSize = tokens.bodySmall,
                color = TextSecondary,
                modifier = Modifier.clickable { onMinus() }
            )
            Text(
                text = quantity.toString(),
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Text(
                text = "+",
                fontSize = tokens.bodySmall,
                color = TextSecondary,
                modifier = Modifier.clickable { onPlus() }
            )
        }
    }
}

@Composable
private fun EditableBreakdownItemCard(
    tag: String,
    title: String,
    qtyLabel: String,
    qtyValue: String,
    onQtyChange: (String) -> Unit,
    rateLabel: String,
    rateValue: String,
    onRateChange: (String) -> Unit,
    selectedGstRate: Double,
    taxGroups: List<TaxGroupDto>,
    onGstRateChange: (Double) -> Unit,
    onDropdownOpen: () -> Unit = {},
    gstComputed: String,
    lineTotal: String,
    onDelete: () -> Unit
) {
    val tokens = LocalAppTokens.current
    var gstDropdownOpen by remember { mutableStateOf(false) }

    val displayGstLabel = remember(selectedGstRate, taxGroups) {
        val matched = taxGroups.find { it.totalRate == selectedGstRate }
        if (matched != null && matched.name.isNotBlank()) {
            "${matched.name} (${matched.totalRate.toInt()}%)"
        } else {
            "${selectedGstRate.toInt()}% GST"
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = whiteBg,
        border = BorderStroke(1.dp, sectionBorder)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = primary_light
                    ) {
                        Text(
                            text = tag,
                            fontSize = tokens.label,
                            fontWeight = FontWeight.Medium,
                            color = Primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = title,
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = redText,
                        modifier = Modifier.size(tokens.iconSize)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Column(Modifier.weight(1f)) {
                    Text(qtyLabel, fontSize = tokens.label, color = TextSecondary)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp),
                        shape = RoundedCornerShape(6.dp),
                        color = badgeGrey,
                        border = BorderStroke(1.dp, sectionBorder)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            BasicTextField(
                                value = qtyValue,
                                onValueChange = onQtyChange,
                                textStyle = LocalTextStyle.current.copy(
                                    fontSize = tokens.caption,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                ),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }
                    }
                }

                Column(Modifier.weight(1f)) {
                    Text(rateLabel, fontSize = tokens.label, color = TextSecondary)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp),
                        shape = RoundedCornerShape(6.dp),
                        color = badgeGrey,
                        border = BorderStroke(1.dp, sectionBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("₹ ", fontSize = tokens.caption, color = TextSecondary)
                            BasicTextField(
                                value = rateValue,
                                onValueChange = onRateChange,
                                textStyle = LocalTextStyle.current.copy(
                                    fontSize = tokens.caption,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                ),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }
                    }
                }

                Column(Modifier.weight(1.1f)) {
                    Text("GST Rate", fontSize = tokens.label, color = TextSecondary)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(34.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
                                    onDropdownOpen()
                                    gstDropdownOpen = true
                                },
                            shape = RoundedCornerShape(6.dp),
                            color = whiteBg,
                            border = BorderStroke(1.dp, iconMuted)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = displayGstLabel,
                                    fontSize = tokens.caption,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(tokens.iconSize)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = gstDropdownOpen,
                            onDismissRequest = { gstDropdownOpen = false },
                            modifier = Modifier.background(whiteBg)
                        ) {
                            if (taxGroups.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("Loading tax rates...", fontSize = tokens.bodySmall, color = TextSecondary) },
                                    onClick = { gstDropdownOpen = false }
                                )
                            } else {
                                taxGroups.forEach { tg ->
                                    val itemLabel = if (tg.name.isNotBlank()) "${tg.name} (${tg.totalRate.toInt()}%)" else "${tg.totalRate.toInt()}% GST"
                                    DropdownMenuItem(
                                        text = { Text(itemLabel, fontSize = tokens.bodySmall, color = TextPrimary) },
                                        onClick = {
                                            onGstRateChange(tg.totalRate)
                                            gstDropdownOpen = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "GST computed: $gstComputed",
                    fontSize = tokens.label,
                    color = TextSecondary
                )
                Text(
                    text = "LINE TOTAL: $lineTotal",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
            }
        }
    }
}

@Composable
private fun EditableCraftLineItemCard(
    title: String,
    unitLabel: String,
    unitValue: String,
    amount: String,
    onAmountChange: (String) -> Unit,
    selectedGstRate: Double,
    taxGroups: List<TaxGroupDto>,
    onGstRateChange: (Double) -> Unit,
    onDropdownOpen: () -> Unit = {},
    gstComputed: String,
    total: String,
    onDelete: () -> Unit
) {
    val tokens = LocalAppTokens.current
    var gstDropdownOpen by remember { mutableStateOf(false) }

    val displayGstLabel = remember(selectedGstRate, taxGroups) {
        val matched = taxGroups.find { it.totalRate == selectedGstRate }
        if (matched != null && matched.name.isNotBlank()) {
            "${matched.name} (${matched.totalRate.toInt()}%)"
        } else {
            "${selectedGstRate.toInt()}% GST"
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = whiteBg,
        border = BorderStroke(1.dp, sectionBorder)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(Primary)
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = grey_border
                    ) {
                        Text(
                            text = title,
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(22.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = redText,
                        modifier = Modifier.size(tokens.iconSize)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Column(Modifier.weight(1f)) {
                    Text(unitLabel, fontSize = tokens.label, color = TextSecondary)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(30.dp),
                        shape = RoundedCornerShape(4.dp),
                        color = badgeGrey,
                        border = BorderStroke(1.dp, sectionBorder)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(unitValue, fontSize = tokens.label, color = TextPrimary)
                        }
                    }
                }

                Column(Modifier.weight(1f)) {
                    Text("Amount (₹)", fontSize = tokens.label, color = TextSecondary)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(30.dp),
                        shape = RoundedCornerShape(4.dp),
                        color = badgeGrey,
                        border = BorderStroke(1.dp, sectionBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("₹ ", fontSize = tokens.label, color = TextSecondary)
                            BasicTextField(
                                value = amount,
                                onValueChange = onAmountChange,
                                textStyle = LocalTextStyle.current.copy(
                                    fontSize = tokens.label,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                ),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }
                    }
                }

                Column(Modifier.weight(1.1f)) {
                    Text("GST %", fontSize = tokens.label, color = TextSecondary)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(30.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .clickable {
                                    onDropdownOpen()
                                    gstDropdownOpen = true
                                },
                            shape = RoundedCornerShape(4.dp),
                            color = whiteBg,
                            border = BorderStroke(1.dp, iconMuted)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = displayGstLabel,
                                    fontSize = tokens.label,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(tokens.iconSize)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = gstDropdownOpen,
                            onDismissRequest = { gstDropdownOpen = false },
                            modifier = Modifier.background(whiteBg)
                        ) {
                            if (taxGroups.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("Loading tax rates...", fontSize = tokens.bodySmall, color = TextSecondary) },
                                    onClick = { gstDropdownOpen = false }
                                )
                            } else {
                                taxGroups.forEach { tg ->
                                    val itemLabel = if (tg.name.isNotBlank()) "${tg.name} (${tg.totalRate.toInt()}%)" else "${tg.totalRate.toInt()}% GST"
                                    DropdownMenuItem(
                                        text = { Text(itemLabel, fontSize = tokens.bodySmall, color = TextPrimary) },
                                        onClick = {
                                            onGstRateChange(tg.totalRate)
                                            gstDropdownOpen = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("GST: $gstComputed", fontSize = tokens.label, color = TextSecondary)
                Text(
                    text = "Total: $total",
                    fontSize = tokens.label,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
            }
        }
    }
}

@Composable
private fun CollapsedPricingItemCardExact(
    number: String,
    title: String,
    quantity: Int,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    price: String,
    stitching: String,
    fabric: String,
    addlWork: String? = null,
    discount: String? = null,
    gst: String,
    containerColor: Color = whiteBg,
    borderColor: Color = BorderGray,
    onDelete: () -> Unit
) {
    val tokens = LocalAppTokens.current

    val displayTitle = if (!tokens.isTablet && title.length > 15) {
        "${title.take(15)}..."
    } else {
        title
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = number,
                        fontSize = tokens.caption,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = displayTitle,
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    QuantityStepper(
                        quantity = quantity,
                        onMinus = onMinus,
                        onPlus = onPlus
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = price,
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary,
                        maxLines = 1
                    )
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete",
                            tint = redText,
                            modifier = Modifier.size(tokens.iconSize)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                QuickPriceBox("Stitching", stitching, Modifier.weight(1f))
                QuickPriceBox("Fabric", fabric, Modifier.weight(1f))
                if (addlWork != null) {
                    QuickPriceBox("Addl. Work", addlWork, Modifier.weight(1f))
                }
                if (discount != null) {
                    QuickPriceBox("Disc.", discount, Modifier.weight(1f), tint = redText)
                }
                QuickPriceBox("GST", gst, Modifier.weight(1f), tint = darkGreenBg)
            }
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    valueColor: Color = TextPrimary
) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = tokens.bodySmall, color = TextSecondary)
        Text(text = value, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = valueColor)
    }
}

@Composable
private fun QuickPriceBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    bg: Color = whiteBg,
    tint: Color = TextPrimary
) {
    val tokens = LocalAppTokens.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = bg,
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = tokens.label, color = TextSecondary)
            Text(value, fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = tint)
        }
    }
}