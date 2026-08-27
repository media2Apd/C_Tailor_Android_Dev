@file:Suppress(
    "UNUSED_VALUE",
    "unused_variable",
    "SpellCheckingInspection",
    "GrazieInspection",
    "AssignedValueIsNeverRead",
    "VariableNeverRead",
    "unused"
)
package com.cuso.mobile.view.home.sales.lead

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.cuso.mobile.R
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.database.entities.SelectedGarment
import com.cuso.mobile.model.sales.BudgetRange
import com.cuso.mobile.model.sales.CreateLeadFormRequest
import com.cuso.mobile.model.sales.LeadAppointment
import com.cuso.mobile.model.sales.LeadContact
import com.cuso.mobile.model.sales.LeadNote
import com.cuso.mobile.model.sales.LeadPerson
import com.cuso.mobile.model.sales.LeadTableItem
import com.cuso.mobile.model.sales.toLeadEntity
import com.cuso.mobile.ui.theme.BluePrimary
import com.cuso.mobile.ui.theme.BorderGray
import com.cuso.mobile.ui.theme.Primary
import com.cuso.mobile.ui.theme.TextSecondary
import com.cuso.mobile.ui.theme.close_color
import com.cuso.mobile.ui.theme.grey_border
import com.cuso.mobile.ui.theme.primary_light
import com.cuso.mobile.ui.theme.title_border
import com.cuso.mobile.ui.theme.title_color
import com.cuso.mobile.ui.theme.title_font
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.AccordionSection
import com.cuso.mobile.view.composable.CirculerProgressIndicatorReuse
import com.cuso.mobile.view.composable.SettingsTabs
import com.cuso.mobile.view.composable.TabItem
import com.cuso.mobile.view.composable.CirculerProgressIndicatorSmall
import com.cuso.mobile.view.composable.DatePickerField
import com.cuso.mobile.view.composable.DynamicIslandError
import com.cuso.mobile.view.composable.DynamicIslandSuccess
import com.cuso.mobile.view.composable.FieldValidator
import com.cuso.mobile.view.composable.PhoneInputField
import com.cuso.mobile.view.composable.ScreenBreadcrumb
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.view.composable.ValidationField
import com.cuso.mobile.view.composable.FormDropdown
import com.cuso.mobile.view.composable.FormLabel
import com.cuso.mobile.view.composable.FormTextField
import com.cuso.mobile.view.home.LeadPrimary
import com.cuso.mobile.view.home.LeadPrimarySoft
import com.cuso.mobile.view.home.LeadmutedText
import com.cuso.mobile.view.home.buildFilterSections
import com.cuso.mobile.view.home.formatIndianNumber
import com.cuso.mobile.view.home.formatLeadDate
import com.cuso.mobile.view.composable.DataCard
import com.cuso.mobile.view.composable.DataCardField
import com.cuso.mobile.view.composable.DeleteModel
import com.cuso.mobile.view.composable.FabConfig
import com.cuso.mobile.view.composable.FabScaffold
import com.cuso.mobile.view.composable.FilterDrawer
import com.cuso.mobile.view.composable.FilterSection
import com.cuso.mobile.view.composable.ListSkeleton
import com.cuso.mobile.view.composable.MenuAction
import com.cuso.mobile.view.composable.SearchFilterBar
import com.cuso.mobile.view.composable.StepNavigationFab
import com.cuso.mobile.view.composable.TimePickerField
import com.cuso.mobile.view.composable.TrailingFabAction
import com.cuso.mobile.view.composable.rememberFilterDrawerState
import com.cuso.mobile.view.home.inventory.procurement.orders.FormTextArea
import com.cuso.mobile.view.home.toIsoDate
import com.cuso.mobile.view.home.sales.sales_order.OrderReviewData
import com.cuso.mobile.viewmodel.SaleState
import com.cuso.mobile.viewmodel.SalesViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.collections.get
import kotlin.text.ifEmpty

// ─────────────────────────────────────────────────────────────
// Reusable "Lead Form" UI kit
// All font sizes / screen-level paddings now come from
// LocalAppTokens.current (AppDesignTokens) instead of hardcoded sp/dp,
// so this whole module scales across compact / medium / expanded windows.
// ─────────────────────────────────────────────────────────────

/**
 * Single source of truth for what the lead form screen is doing.
 * CREATE -> blank form, "Create Lead" button, POST call
 * VIEW   -> read-only fields, "Edit Lead" trailing FAB, no API write
 * EDIT   -> editable fields (prefilled), "Update Lead" trailing FAB, PATCH call
 */
enum class LeadFormMode {
    CREATE, VIEW, EDIT
}

@Composable
fun LeadFormTopBar(
    title: String,
    badgeText: String,
    badgeColor: Color = LeadPrimary,
    onClose: () -> Unit,
    isConverted: Boolean = true,
    onConvertToOrder: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(whiteBg)
            .padding(horizontal = tokens.screenPadding, vertical = tokens.screenPadding * 0.8f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, fontSize = title_font, fontWeight = FontWeight.Bold, color = title_color)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {

            if (isConverted) {
                if (badgeText.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .background(badgeColor.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                            .padding(
                                horizontal = tokens.screenPadding * 0.75f,
                                vertical = tokens.screenPadding * 0.375f
                            )
                    ) {
                        Text(badgeText, fontSize = tokens.caption, fontWeight = FontWeight.SemiBold, color = badgeColor)
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .background(LeadPrimary, RoundedCornerShape(20.dp))
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { onConvertToOrder() }
                        .padding(
                            horizontal = tokens.screenPadding * 0.75f,
                            vertical = tokens.screenPadding * 0.375f
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.SwapHoriz,
                        contentDescription = null,
                        tint = whiteBg,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        "Convert to Order",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.SemiBold,
                        color = whiteBg
                    )
                }
            }

            Icon(
                Icons.Default.Close,
                contentDescription = "Close",
                tint = close_color,
                modifier = Modifier
                    .size(20.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onClose() }
            )
        }
    }
}

@Composable
fun ConvertToOrderDialog(
    leadName: String,
    isLoading: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val tokens = LocalAppTokens.current
    Dialog(onDismissRequest = { if (!isLoading) onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = whiteBg,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(tokens.screenPadding)) {
                Text(
                    "Convert Lead to Order",
                    fontSize = tokens.h2,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    leadName,
                    fontSize = tokens.bodyMedium,
                    color = Color(0xFF6B7280)
                )
                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss, enabled = !isLoading) {
                        Text("Cancel", fontSize = tokens.bodyMedium, color = Color(0xFF6B7280))
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = onConfirm,
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = LeadPrimary)
                    ) {
                        if (isLoading) {
                            CirculerProgressIndicatorSmall()
                        } else {
                            Text("Convert", fontSize = tokens.bodyMedium, color = whiteBg)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LeadInfoBanner(text: String) {
    val tokens = LocalAppTokens.current
    var visible by remember { mutableStateOf(true) }
    AnimatedVisibility(visible = visible, enter = fadeIn(), exit = fadeOut()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(LeadPrimarySoft, RoundedCornerShape(10.dp))
                .padding(horizontal = tokens.screenPadding * 0.85f, vertical = tokens.screenPadding * 0.75f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Info, contentDescription = null, tint = LeadPrimary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            Text(text, fontSize = tokens.bodySmall, color = Color(0xFF374151), modifier = Modifier.weight(1f))
            Icon(
                Icons.Default.Close,
                contentDescription = "Dismiss",
                tint = Color(0xFF9CA3AF),
                modifier = Modifier
                    .size(16.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { visible = false }
            )
        }
    }
}

private fun validateLeadFields(
    leadSource: String,
    enquiryDate: String,
    leadOwner: String,
    leadStatus: String,
    customerType: String,
    fullName: String,
    phone: String,
    email: String,
    gender: String,
    dob: String,
    preferredContact: String,
    enquiryType: String,
    estimatedQuantity: String,
    garmentCategory: String,
    requiredDate: String,
    appointmentRequired: Boolean,
    appointmentDate: String,
    appointmentTime: String?,
    assignedStaff: String?,
    followUpDate: String,
    priority: String?
): String? {
    val missing = mutableListOf<String>()

    if (leadSource.isBlank()) missing += "Lead Source"
    if (enquiryDate.isBlank()) missing += "Enquiry Date"
    if (leadOwner.isBlank()) missing += "Lead Owner"
    if (leadStatus.isBlank()) missing += "Lead Status"
    if (fullName.isBlank()) missing += "Full Name"
    if (phone.isBlank()) missing += "Mobile Number"
    if (email.isBlank()) missing += "Email"
    if (preferredContact.isBlank()) missing += "Preferred Contact Method"
    if (enquiryType.isBlank()) missing += "Enquiry Type"
    if (estimatedQuantity.isBlank()) missing += "Estimated Quantity"
    if (garmentCategory.isBlank()) missing += "Garment Category"
    if (requiredDate.isBlank()) missing += "Required Date"

    if (appointmentRequired) {
        if (appointmentDate.isBlank()) missing += "Appointment Date"
        if (appointmentTime.isNullOrBlank()) missing += "Appointment Time"
        if (assignedStaff.isNullOrBlank()) missing += "Assigned Staff"
        if (followUpDate.isBlank()) missing += "Follow-up Date"
        if (priority.isNullOrBlank()) missing += "Priority"
    }

    if (missing.isEmpty()) return null
    return "Missing: ${missing.first()}" + if (missing.size > 1) " (+${missing.size - 1} more)" else ""
}

// ─────────────────────────────────────────────────────────────
//   Lead → Order local mapping (no API call).
//   Takes whatever is currently filled in on the form and turns
//   it directly into an OrderReviewData that CreateOrderScreen
//   already knows how to prefill itself from.
// ─────────────────────────────────────────────────────────────
private fun buildOrderReviewDataFromLead(
    leadId: String,
    fullName: String,
    phone: String,
    gender: String,
    address: String,
    leadSource: String,
    requiredDate: String,
    appointmentDate: String,
    priority: String?,
    appointmentRequired: Boolean,
    selectedGarmentCategories: List<String>,
    garmentIdMap: Map<String, String>
): OrderReviewData {
    val garments = selectedGarmentCategories.map { categoryName ->
        val categoryId = garmentIdMap[categoryName] ?: ""
        SelectedGarment(
            category = categoryId,
            categoryName = categoryName,
            categoryId = categoryId,
            quantity = 1,
            price = 0.0,
            priority = priority.orEmpty(),
            trialRequired = appointmentRequired,
            fabricSource = "In-House",
            fabricType = "",
            colorTone = "",
            pattern = "Solid",
            models = emptyList()
        )
    }

    return OrderReviewData(
        leadId = leadId,
        orderId = null,
        customerId = "",
        branchId = "",
        fullName = fullName,
        countryCode = "+91",
        phone = phone,
        gender = gender.ifBlank { "Male" },
        dressFor = "",
        address = address,
        garments = garments,
        orderDate = "",
        source = leadSource,
        trialDate = appointmentDate,
        deliveryDate = requiredDate,
        discount = 0.0,
        paidSoFar = 0.0,
        designImages = emptyList(),
        existingImageUrls = emptyList(),
        voiceNoteUri = null
    )
}

// ─────────────────────────────────────────────────────────────
// LeadFormScreen — single screen for Create / View / Edit
// ─────────────────────────────────────────────────────────────
@SuppressLint("AutoboxingStateCreation")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeadFormScreen(
    mode: LeadFormMode,
    onBack: () -> Unit,
    onEditRequested: () -> Unit = {},          // used only in VIEW mode ("Edit Lead" FAB)
    onConvertToOrder: (OrderReviewData) -> Unit = {}  // used only in EDIT mode
) {
    val tokens = LocalAppTokens.current
    val salesViewModel: SalesViewModel = hiltViewModel()

    val isCreate = mode == LeadFormMode.CREATE
    val isView = mode == LeadFormMode.VIEW
    val isEdit = mode == LeadFormMode.EDIT

    // ---- Source lead (null for CREATE, populated for VIEW/EDIT) ----
    val selectedLead by salesViewModel.selectedLead.collectAsStateWithLifecycle()
    val isLoadingLead by salesViewModel.isLoadingLeadDetails.collectAsStateWithLifecycle()
    val leadDetailsError by salesViewModel.leadDetailsError.collectAsStateWithLifecycle()
    val l = if (isCreate) null else selectedLead

    val leadState by salesViewModel.leadState.collectAsStateWithLifecycle()
    val updateState by salesViewModel.updateState.collectAsStateWithLifecycle()

    val staffList by salesViewModel.staffList.collectAsStateWithLifecycle()
    val isLoadingStaff by salesViewModel.isLoadingStaff.collectAsStateWithLifecycle()
    val salesStatuses by salesViewModel.salesStatuses.collectAsStateWithLifecycle()
    val garmentCategories by salesViewModel.garmentCategories.collectAsStateWithLifecycle()

    // 1. Initial Garments list
    val initialGarmentNames = remember(l?.garments, garmentCategories) {
        if (!l?.garments.isNullOrBlank() && garmentCategories.isNotEmpty()) {
            val ids = l.garments.split(",").filter { it.isNotBlank() }
            ids.mapNotNull { id -> garmentCategories.find { it.id == id }?.categoryId?.categoryName }
        } else {
            emptyList()
        }
    }

    LaunchedEffect(Unit) {
        if (staffList.isEmpty()) salesViewModel.fetchStaff()
        if (garmentCategories.isEmpty()) salesViewModel.fetchGarmentCategories()
        if (isCreate && salesStatuses.isEmpty()) salesViewModel.fetchSalesData()
        if (!isCreate && salesStatuses.isEmpty()) salesViewModel.fetchSalesData()
    }

    // VIEW / EDIT need the lead to already be loaded (LeadScreenContent calls
    // fetchLeadDetails before navigating in). If it's missing, bail out.
    if (!isCreate && l == null && !isLoadingLead && leadDetailsError == null) {
        LaunchedEffect(Unit) { onBack() }
        return
    }

    // Only show full screen loader on initial entry when lead data is not yet in memory
    if (!isCreate && l == null && isLoadingLead) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CirculerProgressIndicatorReuse()
                Spacer(Modifier.height(8.dp))
                Text("Loading lead data...", fontSize = tokens.bodyMedium, color = Color.Gray)
            }
        }
        return
    }

    if (!isCreate && leadDetailsError != null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red, modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(8.dp))
                Text("Error loading lead", fontSize = tokens.h2, color = Color.Red, fontWeight = FontWeight.Bold)
                Text(
                    leadDetailsError ?: "Unknown error",
                    fontSize = tokens.bodyMedium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = tokens.screenPadding * 2f)
                )
                Spacer(Modifier.height(12.dp))
                Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = LeadPrimary), shape = RoundedCornerShape(8.dp)) {
                    Text("Go Back", fontSize = tokens.bodyMedium, color = whiteBg)
                }
            }
        }
        return
    }

    // ---- Field state, seeded from `l` when present, blank for CREATE ----
    var leadSource        by remember { mutableStateOf(l?.source ?: "") }
    var enquiryDate       by remember { mutableStateOf(l?.let { formatLeadDate(it.enquiryDate) } ?: "") }
    var leadOwner         by remember { mutableStateOf(l?.leadOwner ?: "") }
    var leadStatus        by remember { mutableStateOf(l?.status ?: "") }
    var customerType      by remember { mutableStateOf(l?.customerType?.replaceFirstChar { c -> c.uppercase() } ?: "Individual") }
    var fullName          by remember { mutableStateOf(l?.fullName ?: "") }
    var email             by remember { mutableStateOf(l?.email ?: "") }
    var gender            by remember { mutableStateOf(l?.gender ?: "") }
    var dob               by remember { mutableStateOf(l?.let { formatLeadDate(it.dob) } ?: "") }
    var address           by remember { mutableStateOf(l?.address ?: "") }
    var areaZone          by remember { mutableStateOf(l?.area ?: "") }
    var city              by remember { mutableStateOf(l?.city ?: "") }
    var preferredContact  by remember { mutableStateOf(l?.preferredContactMethod ?: "") }
    var enquiryType       by remember { mutableStateOf(l?.enquiryType ?: "") }
    var estimatedQuantity by remember { mutableStateOf(l?.let { if (it.estimatedQuantity == 0) "" else it.estimatedQuantity.toString() } ?: "") }
    var budgetRange       by remember { mutableFloatStateOf(l?.budgetMin?.toFloat() ?: 1000f) }
    var requiredDate      by remember { mutableStateOf(l?.let { formatLeadDate(it.requiredDate) } ?: "") }
    var occasion          by remember { mutableStateOf(l?.occasion ?: "") }
    var appointmentRequired by remember(l?.id) { mutableStateOf(l?.appointmentRequired ?: false) }
    var appointmentDate   by remember { mutableStateOf(l?.let { formatLeadDate(it.appointmentDate) } ?: "") }
    var appointmentTime   by remember { mutableStateOf(l?.appointmentTime ?: "") }
    var assignedStaff     by remember { mutableStateOf(l?.assignedStaff ?: "") }
    var followUpDate      by remember { mutableStateOf(l?.let { formatLeadDate(it.followUpDate) } ?: "") }
    var priority          by remember { mutableStateOf(l?.priority ?: "") }
    var internalNotes     by remember { mutableStateOf(l?.internalNotes ?: "") }
    var customerNotes     by remember { mutableStateOf(l?.customerNotes ?: "") }
    var phone             by remember { mutableStateOf(l?.phone ?: "") }
    var selectedIso       by remember { mutableStateOf("IN") }

    var selectedGarmentCategories by remember { mutableStateOf<List<String>>(emptyList()) }
    var showGarmentError by remember { mutableStateOf(false) }

    var leadSourceExpanded       by remember { mutableStateOf(false) }
    var leadOwnerExpanded        by remember { mutableStateOf(false) }
    var leadStatusExpanded       by remember { mutableStateOf(false) }
    var genderExpanded           by remember { mutableStateOf(false) }
    var preferredContactExpanded by remember { mutableStateOf(false) }
    var enquiryTypeExpanded      by remember { mutableStateOf(false) }
    var assignedStaffExpanded    by remember { mutableStateOf(false) }
    var priorityExpanded         by remember { mutableStateOf(false) }

    // VIEW defaults to Lead Info expanded, everything else collapsed (matches old ViewLeadScreen / EditLeadScreen)
    var expandedSection by remember { mutableStateOf("lead_info") }

    var errorField by remember { mutableStateOf<String?>(null) }
    var validationError by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var showConvertDialog by remember { mutableStateOf(false) }

    val isConvertedStatus = remember(leadStatus) {
        leadStatus.equals("Converted to order", ignoreCase = true)
    }

    val leadSectionFieldMap = remember {
        mapOf(
            "lead_info" to listOf("leadSource", "enquiryDate", "leadOwner", "leadStatus"),
            "customer" to listOf("fullName", "phone", "email"),
            "location" to listOf("preferredContact"),
            "enquiry" to listOf("enquiryType", "estimatedQuantity", "garmentCategory", "requiredDate"),
            "appointment" to listOf("appointmentDate", "appointmentTime", "assignedStaff", "followUpDate", "priority")
        )
    }
    val leadSourceOptions       = listOf("Walk-in", "Instagram", "Facebook Ads", "Website")
    val genderOptions           = listOf("Male", "Female", "Other")
    val customerTypeTabs = remember {
        listOf(
            TabItem(label = "Individual", icon = Icons.Default.Person),
            TabItem(label = "Corporate", icon = Icons.Default.Business)
        )
    }
    val preferredContactOptions = listOf("WhatsApp", "Call")
    val enquiryTypeOptions      = listOf("New Order", "Bulk Order")
    val priorityOptions         = listOf("Low", "Medium", "High")

    val staffDisplayList   = staffList.map { "${it.firstName} ${it.lastName} - ${it.memberId}" }
    val staffIdMap         = staffList.associate { "${it.firstName} ${it.lastName} - ${it.memberId}" to it.id }
    val selectedStaffLabel = staffIdMap.entries.firstOrNull { it.value == (if (isEdit) assignedStaff else leadOwner) }?.key
        ?: (if (isCreate) "" else "")
    val leadOwnerLabel     = staffIdMap.entries.firstOrNull { it.value == leadOwner }?.key ?: ""
    val assignedStaffLabel = staffIdMap.entries.firstOrNull { it.value == assignedStaff }?.key ?: ""

    val statusOptions      = salesStatuses.map { it.name }
    val statusIdMap        = salesStatuses.associate { it.name to it.id }
    val garmentIdMap       = garmentCategories.associate { it.categoryId.categoryName to it.id }
    val garmentOptions     = garmentCategories.map { it.categoryId.categoryName }

    // Prefill garment chips from the lead once categories are loaded (VIEW + EDIT).
    LaunchedEffect(l?.garments, garmentCategories) {
        if (!isCreate && garmentCategories.isNotEmpty() && !l?.garments.isNullOrBlank()) {
            val ids = l.garments.split(",").filter { it.isNotBlank() }
            val names = ids.mapNotNull { id -> garmentCategories.find { it.id == id }?.categoryId?.categoryName }
            if (names.isNotEmpty()) selectedGarmentCategories = names
        }
    }
    val isFormDirty = remember(
        l, leadSource, enquiryDate, leadOwner, leadStatus, customerType,
        fullName, email, gender, dob, address, areaZone, city, preferredContact,
        enquiryType, estimatedQuantity, budgetRange, requiredDate, occasion,
        appointmentRequired, appointmentDate, appointmentTime, assignedStaff,
        followUpDate, priority, internalNotes, customerNotes, phone,
        selectedGarmentCategories, initialGarmentNames
    ) {
        l != null
    }

    fun clearAllFields() {
        leadSource = ""; enquiryDate = ""; leadStatus = ""; customerType = "Individual"
        fullName = ""; email = ""; gender = ""; dob = ""; address = ""; areaZone = ""; city = ""
        preferredContact = ""; enquiryType = ""; estimatedQuantity = ""; selectedGarmentCategories = emptyList()
        budgetRange = 1000f; requiredDate = ""; occasion = ""; appointmentRequired = false
        appointmentDate = ""; appointmentTime = ""; assignedStaff = ""; followUpDate = ""
        priority = ""; internalNotes = ""; customerNotes = ""; phone = ""
        leadOwner = ""
    }

    fun buildRequest(): CreateLeadFormRequest {
        /**
         * Helper function to safely convert date strings to ISO format.
         * If the [toIsoDate] utility returns an empty string (which can happen if the
         * input is already in ISO format or the format is unrecognized), it returns
         * the original [dateStr] to prevent sending empty values to the API.
         */
        fun safeIsoDate(dateStr: String): String {
            return if (dateStr.isNotBlank()) {
                val converted = dateStr.toIsoDate()
                // If conversion results in an empty string, fallback to the original value
                converted.ifBlank { dateStr }
            } else {
                ""
            }
        }

        return CreateLeadFormRequest(
            customerType = customerType.lowercase(),
            enquiryType = enquiryType,
            estimatedQuantity = estimatedQuantity.toIntOrNull() ?: 0,
            budgetRange = BudgetRange(min = budgetRange.toInt(), max = 250000),
            // Map selected category display names back to their respective database IDs
            garments = selectedGarmentCategories.mapNotNull { garmentIdMap[it] },

            // Safely convert primary lead dates
            enquiryDate = safeIsoDate(enquiryDate),
            requiredDate = safeIsoDate(requiredDate),

            source = leadSource,
            leadOwner = leadOwner,
            person = LeadPerson(
                name = fullName,
                phone = phone,
                email = email,
                gender = gender,
                // Safely convert date of birth
                dob = safeIsoDate(dob)
            ),
            contact = LeadContact(
                address = address,
                area = areaZone,
                city = city,
                preferredContactMethod = preferredContact
            ),
            appointment = LeadAppointment(
                isRequired = appointmentRequired,
                // Safely convert appointment related dates if required
                date = if (appointmentRequired) safeIsoDate(appointmentDate) else null,
                time = if (appointmentRequired) appointmentTime.takeIf { it.isNotBlank() } else null,
                assignedStaff = assignedStaff.takeIf { it.isNotBlank() },
                priority = if (appointmentRequired) priority.takeIf { it.isNotBlank() } else null,
                followUpDate = if (appointmentRequired) safeIsoDate(followUpDate) else null
            ),
            status = statusIdMap[leadStatus] ?: "",
            statusName = leadStatus,
            notes = buildList {
                if (internalNotes.isNotBlank()) add(LeadNote(internalNotes, "internal"))
                if (customerNotes.isNotBlank()) add(LeadNote(customerNotes, "customer"))
                // During edit mode, if both notes are blank, provide a default placeholder
                if (isEdit && internalNotes.isBlank() && customerNotes.isBlank()) {
                    add(LeadNote("-", "internal"))
                }
            },
            occasion = if (isEdit) l?.occasion ?: occasion else occasion
        )
    }
    // ---- Mode-aware submit: CREATE -> createLead, EDIT -> updateLeadById ----
    fun submitLead() {
        val baseFields = buildList {
            add(ValidationField("leadSource", leadSource, "Lead Source is required"))
            add(ValidationField("enquiryDate", enquiryDate, "Enquiry Date is required"))
            add(ValidationField("leadOwner", leadOwner, "Lead Owner is required"))
            add(ValidationField("leadStatus", leadStatus, "Lead Status is required"))
            add(ValidationField("fullName", fullName, "Full Name is required"))
            add(ValidationField("phone", phone, "Mobile Number is required"))
            add(ValidationField("email", email, "Email is required"))
            add(ValidationField("preferredContact", preferredContact, "Preferred Contact Method is required"))
            add(ValidationField("enquiryType", enquiryType, "Enquiry Type is required"))
            add(ValidationField("estimatedQuantity", estimatedQuantity, "Estimated Quantity is required"))
            add(ValidationField("garmentCategory", selectedGarmentCategories.joinToString(","), "Garment Category is required"))
            add(ValidationField("requiredDate", requiredDate, "Required Date is required"))
        }
        val appointmentFields = if (appointmentRequired) {
            listOf(
                ValidationField("appointmentDate", appointmentDate, "Appointment Date is required"),
                ValidationField("appointmentTime", appointmentTime, "Appointment Time is required"),
                ValidationField("assignedStaff", assignedStaff, "Assigned Staff is required"),
                ValidationField("followUpDate", followUpDate, "Follow-up Date is required"),
                ValidationField("priority", priority, "Priority is required")
            )
        } else emptyList()

        if (isCreate) {
            val result = FieldValidator.validate(baseFields + appointmentFields)
            if (result != null) {
                errorField = result.fieldKey
                validationError = result.message
                expandedSection = FieldValidator.resolveSection(result.fieldKey, leadSectionFieldMap) ?: expandedSection
                return
            }
        }
        errorField = null

        val error = validateLeadFields(
            leadSource = leadSource, enquiryDate = enquiryDate, leadOwner = leadOwner,
            leadStatus = leadStatus, customerType = customerType, fullName = fullName,
            phone = phone, email = email,
            gender = gender, dob = dob, preferredContact = preferredContact,
            enquiryType = enquiryType, estimatedQuantity = estimatedQuantity,
            garmentCategory = selectedGarmentCategories.joinToString(","), requiredDate = requiredDate,
            appointmentRequired = appointmentRequired, appointmentDate = appointmentDate,
            appointmentTime = appointmentTime, assignedStaff = assignedStaff,
            followUpDate = followUpDate, priority = priority
        )
        if (error != null) {
            validationError = error
            return
        }

        if (isEdit && selectedGarmentCategories.isEmpty()) {
            showGarmentError = true
            return
        }
        showGarmentError = false

        val request = buildRequest()
        if (isCreate) {
            salesViewModel.createLead(request)
        } else if (isEdit) {
            salesViewModel.updateLeadById(l!!.id, request)
        }
    }

    LaunchedEffect(leadState) {
        if (!isCreate) return@LaunchedEffect
        when (val state = leadState) {
            is SaleState.Success<*> -> {
                salesViewModel.fetchSalesData()
                onBack()
            }
            is SaleState.Error -> {
                validationError = state.message
            }
            else -> Unit
        }
    }


    LaunchedEffect(updateState) {
        if (!isEdit) return@LaunchedEffect
        when (val state = updateState) {
            is SaleState.Success<*> -> {
                successMessage = "Lead updated successfully"
                salesViewModel.fetchTableLeads()

                delay(1200)

                // Reset update state first so it doesn't re-trigger
                salesViewModel.resetUpdateState()

                // Refresh the lead details for the View screen
                l?.id?.let { leadId ->
                    salesViewModel.fetchLeadDetails(leadId) { /* Refresh completed */ }
                }

                onBack()
            }
            is SaleState.Error -> {
                validationError = "Update failed: ${state.message}"
                salesViewModel.resetUpdateState()
            }
            else -> Unit
        }
    }

    // Synchronize form fields whenever the selected lead data updates
    LaunchedEffect(l) {
        if (l != null) {
            leadSource = l.source
            enquiryDate = formatLeadDate(l.enquiryDate)
            leadOwner = l.leadOwner
            leadStatus = l.status
            customerType = l.customerType.replaceFirstChar { c -> c.uppercase() }
            fullName = l.fullName
            email = l.email
            gender = l.gender
            dob = formatLeadDate(l.dob)
            address = l.address
            areaZone = l.area
            city = l.city
            preferredContact = l.preferredContactMethod
            enquiryType = l.enquiryType.orEmpty()
            estimatedQuantity = if (l.estimatedQuantity == 0) "" else l.estimatedQuantity.toString()
            budgetRange = l.budgetMin.toFloat()
            requiredDate = formatLeadDate(l.requiredDate)
            occasion = l.occasion
            appointmentRequired = l.appointmentRequired
            appointmentDate = formatLeadDate(l.appointmentDate)
            appointmentTime = l.appointmentTime.orEmpty()
            assignedStaff = l.assignedStaff.orEmpty()
            followUpDate = formatLeadDate(l.followUpDate)
            priority = l.priority.orEmpty()
            internalNotes = l.internalNotes
            customerNotes = l.customerNotes
            phone = l.phone
        }
    }

    // ---- Mode-aware chrome text ----
    val screenTitle = when (mode) {
        LeadFormMode.CREATE -> "Create Lead"
        LeadFormMode.VIEW -> "View Lead"
        LeadFormMode.EDIT -> "Edit Lead"
    }
    val badgeFallback = if (isCreate) "New Enquiry" else "—"

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0)
        ) { padding ->
            val screenContent: @Composable () -> Unit = {
                Column(modifier = Modifier.fillMaxSize()) {
                    LeadFormTopBar(
                        title = screenTitle,
                        badgeText = leadStatus.ifEmpty { badgeFallback },
                        onClose = onBack,
                        isConverted = if (isEdit) isConvertedStatus else true,
                        onConvertToOrder = { showConvertDialog = true }
                    )
                    HorizontalDivider(color = title_border)

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 90.dp)
                    ) {
                        if (isCreate) {
                            item {
                                Spacer(Modifier.height(tokens.screenPadding * 0.75f))
                                Box(modifier = Modifier.padding(horizontal = tokens.screenPadding)) {
                                    LeadInfoBanner("Fill the details below to create a new lead.")
                                }
                            }
                        } else if (isEdit) {
                            item {
                                Box(modifier = Modifier.padding(horizontal = tokens.screenPadding)) {
                                    LeadInfoBanner("Edit the details below and save your changes.")
                                }
                            }
                        }

                        item {
                            AccordionSection(
                                iconPainter = painterResource(R.drawable.ic_person),
                                title = "Lead Information",
                                expanded = expandedSection == "lead_info",
                                onHeaderClick = {
                                    expandedSection = if (expandedSection == "lead_info") "" else "lead_info"
                                }
                            ) {
                                if (isView) {
                                    ViewFieldValue("Lead Source", leadSource.ifEmpty { "—" })
                                    ViewFieldValue("Enquiry Date", enquiryDate.ifEmpty { "—" })
                                    ViewFieldValue("Lead Owner", leadOwnerLabel.ifEmpty { "—" })
                                    ViewFieldValue("Lead Status", leadStatus.ifEmpty { "—" })
                                } else {
                                    FormDropdown(
                                        "Lead Source", leadSource.ifEmpty { "Select an option" },
                                        leadSourceExpanded, { leadSourceExpanded = it },
                                        leadSourceOptions, { leadSource = it },
                                        isRequired = true,
                                        isError = errorField == "leadSource",
                                        errorMessage = if (errorField == "leadSource") "Lead Source is required" else null
                                    )
                                    Spacer(Modifier.height(14.dp))
                                    FormLabel("Enquiry Date", isRequired = true)
                                    DatePickerField(
                                        value = enquiryDate,
                                        onDateSelected = { enquiryDate = it },
                                        isError = errorField == "enquiryDate"
                                    )
                                    Spacer(Modifier.height(14.dp))
                                    FormDropdown(
                                        "Lead Owner",
                                        leadOwnerLabel.ifEmpty { if (isLoadingStaff) "Loading staff..." else "Select an option" },
                                        leadOwnerExpanded,
                                        { leadOwnerExpanded = it },
                                        staffDisplayList,
                                        { label -> leadOwner = staffIdMap[label] ?: "" },
                                        isRequired = true,
                                        isError = errorField == "leadOwner",
                                        errorMessage = if (errorField == "leadOwner") "Lead Owner is required" else null
                                    )
                                    Spacer(Modifier.height(14.dp))
                                    FormDropdown(
                                        "Lead Status",
                                        leadStatus.ifEmpty { "Select an option" },
                                        leadStatusExpanded,
                                        { leadStatusExpanded = it },
                                        statusOptions,
                                        { leadStatus = it },
                                        isRequired = true,
                                        isError = errorField == "leadStatus",
                                        errorMessage = if (errorField == "leadStatus") "Lead Status is required" else null
                                    )
                                }
                            }
                        }

                        item {
                            AccordionSection(
                                iconPainter = painterResource(R.drawable.ic_date_of_birth),
                                title = "Customer Identity",
                                expanded = expandedSection == "customer",
                                onHeaderClick = {
                                    expandedSection = if (expandedSection == "customer") "" else "customer"
                                }
                            ) {
                                if (isView) {
                                    SettingsTabs(
                                        tabs = customerTypeTabs,
                                        selectedIndex = if (customerType.equals("Corporate", ignoreCase = true)) 1 else 0,
                                        onTabSelected = {},
                                        containerColor = whiteBg,
                                        selectedBackgroundColor = Color(0xFFEEF0FF),
                                        selectedTextColor = Primary,
                                        unselectedTextColor = TextSecondary,
                                        selectedIconColor = Primary,
                                        unselectedIconColor = TextSecondary,
                                        borderColor = grey_border,
                                        cornerRadius = 12.dp,
                                        selectedCornerRadius = 10.dp
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    ViewFieldValue(
                                        if (customerType == "Corporate") "Company Name" else "Full Name",
                                        fullName.ifEmpty { "—" }
                                    )
                                    ViewFieldValue("Phone", phone.ifEmpty { "—" })
                                    ViewFieldValue("Email", email.ifEmpty { "—" })
                                    if (customerType.equals("Individual", ignoreCase = true)) {
                                        ViewFieldValue("Gender", gender.ifEmpty { "—" })
                                        ViewFieldValue("Date of Birth", dob.ifEmpty { "" })
                                    }
                                } else {
                                    SettingsTabs(
                                        tabs = customerTypeTabs,
                                        selectedIndex = if (customerType.equals("Corporate", ignoreCase = true)) 1 else 0,
                                        onTabSelected = { index ->
                                            customerType = if (index == 0) "Individual" else "Corporate"
                                        },
                                        containerColor = whiteBg,
                                        selectedBackgroundColor = Color(0xFFEEF0FF),
                                        selectedTextColor = Primary,
                                        unselectedTextColor = TextSecondary,
                                        selectedIconColor = Primary,
                                        unselectedIconColor = TextSecondary,
                                        borderColor = grey_border,
                                        cornerRadius = 12.dp,
                                        selectedCornerRadius = 10.dp
                                    )
                                    Spacer(Modifier.height(14.dp))
                                    FormLabel(
                                        if (customerType == "Corporate") "Company Name" else "Full Name",
                                        isRequired = true
                                    )
                                    FormTextField(
                                        value = fullName,
                                        onValueChange = { fullName = it },
                                        isError = errorField == "fullName",
                                        errorMessage = if (errorField == "fullName") "Full Name is required" else null
                                    )
                                    Spacer(Modifier.height(14.dp))
                                    FormLabel("Mobile Number", isRequired = true)
                                    PhoneInputField(
                                        phoneValue = phone,
                                        onPhoneChange = { phone = it },
                                        onCountryChange = { selectedIso = it.iso },
                                        isError = errorField == "phone",
                                        errorMessage = if (errorField == "phone") "Mobile Number is required" else null
                                    )
                                    Spacer(Modifier.height(14.dp))
                                    FormLabel("Email")
                                    FormTextField(
                                        value = email,
                                        onValueChange = { email = it },
                                        isError = errorField == "email",
                                        errorMessage = if (errorField == "email") "Email id is required" else null
                                    )
                                    if (customerType == "Individual") {
                                        Spacer(Modifier.height(14.dp))
                                        FormDropdown(
                                            "Gender",
                                            gender.ifEmpty { "Select an option" },
                                            genderExpanded,
                                            { genderExpanded = it },
                                            genderOptions,
                                            { gender = it }
                                        )
                                        Spacer(Modifier.height(14.dp))
                                        FormLabel("Date of Birth")
                                        DatePickerField(value = dob, onDateSelected = { dob = it })
                                    }
                                }
                            }
                        }

                        item {
                            AccordionSection(
                                iconPainter = painterResource(R.drawable.ic_location),
                                title = "Location & Communication",
                                expanded = expandedSection == "location",
                                onHeaderClick = {
                                    expandedSection = if (expandedSection == "location") "" else "location"
                                }
                            ) {
                                if (isView) {
                                    ViewFieldValue("Address", address.ifEmpty { "—" })
                                    ViewFieldValue("Area / Zone", areaZone.ifEmpty { "—" })
                                    ViewFieldValue("City", city.ifEmpty { "—" })
                                    ViewFieldValue("Preferred Contact Method", preferredContact.ifEmpty { "—" })
                                } else {
                                    FormLabel("Address")
                                    FormTextField(value = address, onValueChange = { address = it })
                                    Spacer(Modifier.height(14.dp))
                                    FormLabel("Area / Zone")
                                    FormTextField(value = areaZone, onValueChange = { areaZone = it })
                                    Spacer(Modifier.height(14.dp))
                                    FormLabel("City")
                                    FormTextField(value = city, onValueChange = { city = it })
                                    Spacer(Modifier.height(14.dp))
                                    FormDropdown(
                                        "Preferred Contact Method",
                                        preferredContact.ifEmpty { "Select an option" },
                                        preferredContactExpanded,
                                        { preferredContactExpanded = it },
                                        preferredContactOptions,
                                        { preferredContact = it },
                                        isRequired = true,

                                    )
                                }
                            }
                        }

                        item {
                            AccordionSection(
                                iconPainter = painterResource(R.drawable.ic_message),
                                title = "Enquiry Details",
                                expanded = expandedSection == "enquiry",
                                onHeaderClick = {
                                    expandedSection = if (expandedSection == "enquiry") "" else "enquiry"
                                }
                            ) {
                                if (isView) {
                                    ViewFieldValue("Enquiry Type", enquiryType.ifEmpty { "—" })
                                    ViewFieldValue("Estimated Quantity", estimatedQuantity.ifEmpty { "—" })

                                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                                        Text("Garment Category", fontSize = tokens.caption, color = LeadmutedText, fontWeight = FontWeight.Medium)
                                        Spacer(Modifier.height(4.dp))
                                        if (selectedGarmentCategories.isNotEmpty()) {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                selectedGarmentCategories.forEach { garment ->
                                                    Box(
                                                        modifier = Modifier
                                                            .border(1.dp, LeadPrimary, RoundedCornerShape(50.dp))
                                                            .background(LeadPrimarySoft, RoundedCornerShape(50.dp))
                                                            .padding(
                                                                horizontal = tokens.screenPadding,
                                                                vertical = tokens.screenPadding * 0.5f
                                                            )
                                                    ) {
                                                        Text(garment, fontSize = tokens.bodySmall, color = LeadPrimary, fontWeight = FontWeight.SemiBold)
                                                    }
                                                }
                                            }
                                        } else {
                                            Text("—", fontSize = tokens.bodyMedium, color = Color(0xFF111827))
                                        }
                                    }

                                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                                        Text("Budget Range", fontSize = tokens.caption, color = LeadmutedText, fontWeight = FontWeight.Medium)
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            "₹${formatIndianNumber(budgetRange.toInt())}",
                                            fontSize = tokens.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = LeadPrimary
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        BudgetRangeSlider(value = budgetRange, onValueChange = {}, enabled = false)
                                        Spacer(Modifier.height(4.dp))
                                        BudgetRangeLabels(currentValue = budgetRange.toInt())
                                    }

                                    ViewFieldValue("Required Date", requiredDate.ifEmpty { "—" })
                                    ViewFieldValue("Occasion", occasion.ifEmpty { "—" })
                                } else {
                                    FormDropdown(
                                        "Enquiry Type",
                                        enquiryType.ifEmpty { "Select an option" },
                                        enquiryTypeExpanded,
                                        { enquiryTypeExpanded = it },
                                        enquiryTypeOptions,
                                        { enquiryType = it },
                                        isError = errorField == "enquiryType",
                                        errorMessage = if (errorField == "enquiryType") "Enquiry Type is required" else null
                                    )
                                    Spacer(Modifier.height(14.dp))
                                    FormLabel("Estimated Quantity")
                                    FormTextField(
                                        value = estimatedQuantity,
                                        onValueChange = { estimatedQuantity = it },
                                        keyboardType = KeyboardType.Number,
                                        isError = errorField == "estimatedQuantity",
                                        errorMessage = if (errorField == "estimatedQuantity") "Estimated Quantity is required" else null
                                    )
                                    Spacer(Modifier.height(14.dp))

                                    Column {
                                        Row {
                                            Text(
                                                "Garment Categories",
                                                fontSize = tokens.bodySmall,
                                                fontWeight = FontWeight.Medium,
                                                color = if (showGarmentError) Color.Red else Color.Gray
                                            )
                                            Text(" *", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = Color.Red)
                                            Text(" (Select one or more)", fontSize = tokens.label, color = Color.Gray)
                                        }
                                        Spacer(Modifier.height(6.dp))

                                        if (garmentCategories.isEmpty()) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp))
                                                    .padding(tokens.screenPadding * 0.85f),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    CirculerProgressIndicatorSmall()
                                                    Text("Loading categories...", fontSize = tokens.bodyMedium, color = Color(0xFF6B7280))
                                                }
                                            }
                                        } else {
                                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                                items(garmentOptions) { option ->
                                                    val isSelected = selectedGarmentCategories.contains(option)
                                                    Box(
                                                        modifier = Modifier
                                                            .border(
                                                                1.dp,
                                                                if (isSelected) LeadPrimary else grey_border,
                                                                RoundedCornerShape(50.dp)
                                                            )
                                                            .background(
                                                                if (isSelected) LeadPrimarySoft else whiteBg,
                                                                RoundedCornerShape(50.dp)
                                                            )
                                                            .clickable {
                                                                selectedGarmentCategories = if (isSelected) {
                                                                    selectedGarmentCategories.filter { it != option }
                                                                } else {
                                                                    selectedGarmentCategories + option
                                                                }
                                                                showGarmentError = false
                                                            }
                                                            .padding(
                                                                horizontal = tokens.screenPadding,
                                                                vertical = tokens.screenPadding * 0.5f
                                                            )
                                                    ) {
                                                        Text(
                                                            option,
                                                            fontSize = tokens.bodySmall,
                                                            color = if (isSelected) LeadPrimary else Color(0xFF374151),
                                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                                        )
                                                    }
                                                }
                                            }
                                            if (selectedGarmentCategories.isNotEmpty()) {
                                                Spacer(Modifier.height(4.dp))
                                                Text(
                                                    "Selected: ${selectedGarmentCategories.joinToString(", ")}",
                                                    fontSize = tokens.caption,
                                                    color = Color(0xFF6B7280)
                                                )
                                            }
                                        }

                                        if (showGarmentError) {
                                            Spacer(Modifier.height(4.dp))
                                            Text("Please select at least one garment category", fontSize = tokens.caption, color = Color.Red)
                                        }
                                    }

                                    Spacer(Modifier.height(14.dp))
                                    FormLabel("Budget Range")
                                    BudgetRangeSlider(value = budgetRange, onValueChange = { budgetRange = it })
                                    Spacer(Modifier.height(4.dp))
                                    BudgetRangeLabels(currentValue = budgetRange.toInt())

                                    Spacer(Modifier.height(14.dp))
                                    FormLabel("Required Date")
                                    DatePickerField(value = requiredDate, onDateSelected = { requiredDate = it })
                                    Spacer(Modifier.height(14.dp))
                                    FormLabel("Occasion")
                                    FormTextField(value = occasion, onValueChange = { occasion = it })
                                }
                            }
                        }

                        item {
                            AccordionSection(
                                iconPainter = painterResource(R.drawable.ic_calendar),
                                iconTint = Primary,
                                title = "Appointment & Follow-Up",
                                expanded = if (isView) expandedSection == "appointment" else (expandedSection == "appointment" && appointmentRequired),
                                onHeaderClick = {
                                    expandedSection = if (expandedSection == "appointment") "" else "appointment"
                                },
                                showArrow = false,
                                trailing = {
                                    MiniSwitch(
                                        checked = appointmentRequired,
                                        onCheckedChange = {
                                            if (!isView) {
                                                appointmentRequired = it
                                                if (it) expandedSection = "appointment"
                                            }
                                        },
                                        enabled = !isView
                                    )
                                }
                            ) {
                                if (appointmentRequired) {
                                    if (isView) {
                                        ViewFieldValue("Appointment Date", appointmentDate.ifEmpty { "—" })
                                        ViewFieldValue("Appointment Time", appointmentTime.ifEmpty { "--:--" })
                                        ViewFieldValue("Assigned Staff", assignedStaffLabel.ifEmpty { "—" })
                                        ViewFieldValue("Follow-up Date", followUpDate.ifEmpty { "—" })
                                        ViewFieldValue("Priority", priority.ifEmpty { "—" })
                                    } else {
                                        FormLabel("Appointment Date")
                                        DatePickerField(value = appointmentDate,
                                            onDateSelected = {
                                                appointmentDate = it
                                                if (errorField == "appointmentDate") errorField = null
                                            },
                                            isError = errorField == "appointmentDate"
                                        )
                                        Spacer(Modifier.height(14.dp))
                                        FormLabel("Appointment Time")
                                        TimePickerField(value = appointmentTime, onTimeSelected = { appointmentTime = it })
                                        Spacer(Modifier.height(14.dp))
                                        FormDropdown(
                                            "Assigned Staff",
                                            assignedStaffLabel.ifEmpty { if (isLoadingStaff) "Loading staff..." else "Select an option" },
                                            assignedStaffExpanded && !isLoadingStaff,
                                            { assignedStaffExpanded = it },
                                            staffDisplayList,
                                            { label -> assignedStaff = staffIdMap[label] ?: "" },
                                            isError = errorField == "assignedStaff",
                                            errorMessage = if (errorField == "assignedStaff") "Assigned Staff is required" else null
                                        )
                                        Spacer(Modifier.height(14.dp))
                                        FormLabel("Follow-up Date", isRequired = true)
                                        DatePickerField(value = followUpDate, onDateSelected = { followUpDate = it })
                                        Spacer(Modifier.height(14.dp))
                                        FormDropdown(
                                            "Priority",
                                            priority.ifEmpty { "Select an option" },
                                            priorityExpanded,
                                            { priorityExpanded = it },
                                            priorityOptions,
                                            { priority = it },
                                            isRequired = true,
                                            isError = errorField == "priority",
                                            errorMessage = if (errorField == "priority") "Priority is required" else null
                                        )
                                    }
                                } else {
                                    Text("No appointment scheduled.", fontSize = tokens.bodySmall, color = LeadmutedText)
                                }
                            }
                        }

                        item {
                            AccordionSection(
                                iconPainter = painterResource(R.drawable.ic_file),
                                title = "Notes & References",
                                expanded = expandedSection == "notes",
                                onHeaderClick = {
                                    expandedSection = if (expandedSection == "notes") "" else "notes"
                                }
                            ) {
                                if (isView) {
                                    ViewFieldValue("Internal Notes", internalNotes.ifEmpty { "—" })
                                    ViewFieldValue("Customer Notes", customerNotes.ifEmpty { "—" })
                                } else {
                                    FormLabel("Internal Notes")
                                    FormTextArea(
                                        value = internalNotes,
                                        onValueChange = {internalNotes = it}
                                    )
                                    Spacer(Modifier.height(14.dp))
                                    FormLabel("Customer Notes")
                                    FormTextArea(
                                        value = customerNotes,
                                        onValueChange = {customerNotes = it}
                                    )
                                }
                            }
                        }

                        if (isCreate) {
                            item {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clickable(
                                            indication = null,
                                            interactionSource = remember { MutableInteractionSource() }
                                        ) { clearAllFields() }
                                        .padding(horizontal = tokens.screenPadding * 0.9f, vertical = 8.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Clear All", fontSize = tokens.bodySmall, color = Primary, fontWeight = FontWeight.Medium)
                                }
                            }
                        }

                        item { Spacer(Modifier.height(8.dp)) }
                    }
                }
            }

            when (mode) {
                LeadFormMode.CREATE -> {
                    FabScaffold(
                        fab = FabConfig(
                            label = "Create Lead",
                            icon = Icons.AutoMirrored.Filled.ArrowForward,
                            onClick = { submitLead() },
                            bottomPadding = 50.dp
                        ),
                        modifier = Modifier.fillMaxSize().padding(padding)
                    ) {
                        screenContent()
                    }
                }
                else -> {
                    Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                        screenContent()
                    }
                }
            }
        }

        if (!isCreate) {
            StepNavigationFab(
                showBack = true,
                onBack = onBack,
                showBackArrow = false,
                backLabel = if (isEdit) "Cancel" else "Back",
                trailingAction = if (isEdit) {
                    TrailingFabAction.Update(
                        isLoading = updateState is SaleState.Loading,
                        label = "Update Lead",
                        enabled = updateState !is SaleState.Loading && selectedGarmentCategories.isNotEmpty(),
                        onClick = { submitLead() }
                    )
                } else {
                    TrailingFabAction.Edit(
                        label = "Edit Lead",
                        onClick = {
                            salesViewModel.fetchLeadDetails(l!!.id) { success ->
                                if (!success) {
                                    validationError = "Failed to refresh lead data"
                                }
                                onEditRequested()
                            }
                        }
                    )
                }
            )
        }

        DynamicIslandError(
            modifier = Modifier.align(Alignment.TopCenter),
            message = validationError,
            onDismiss = { validationError = null }
        )
        DynamicIslandSuccess(
            modifier = Modifier.align(Alignment.TopCenter),
            message = successMessage,
            onDismiss = { successMessage = null }
        )

        DynamicIslandError(
            modifier = Modifier.align(Alignment.TopCenter),
            message = validationError,
            onDismiss = { validationError = null }
        )
    }

    if (isEdit && showConvertDialog) {
        ConvertToOrderDialog(
            leadName = fullName,
            onDismiss = { showConvertDialog = false },
            onConfirm = {
                showConvertDialog = false
                onConvertToOrder(
                    buildOrderReviewDataFromLead(
                        leadId = l!!.id,
                        fullName = fullName,
                        phone = phone,
                        gender = gender,
                        address = address,
                        leadSource = leadSource,
                        requiredDate = requiredDate,
                        appointmentDate = appointmentDate,
                        priority = priority,
                        appointmentRequired = appointmentRequired,
                        selectedGarmentCategories = selectedGarmentCategories,
                        garmentIdMap = garmentIdMap
                    )
                )
            }
        )
    }
}



// ─────────────────────────────────────────────────────────────
// LeadScreenContent — unchanged (list screen, unrelated to form merge)
// ─────────────────────────────────────────────────────────────
@Composable
fun LeadScreenContent(
    onCreateLead: () -> Unit = {},
    onViewLead: () -> Unit = {},
    onEditLead: () -> Unit = {},
    onClose: () -> Unit = {},
    onBreadCrumbClick: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val salesViewModel: SalesViewModel = hiltViewModel()

    val leads by salesViewModel.tableLeads.collectAsStateWithLifecycle()
    val isLoading by salesViewModel.isLoadingTableLeads.collectAsStateWithLifecycle()
    val tableError by salesViewModel.tableError.collectAsStateWithLifecycle()
    val deleteState by salesViewModel.deleteState.collectAsStateWithLifecycle()
    val updateState by salesViewModel.updateState.collectAsStateWithLifecycle()
    val leadState by salesViewModel.leadState.collectAsStateWithLifecycle()

    val salesStatuses by salesViewModel.salesStatuses.collectAsStateWithLifecycle()
    val garmentCategories by salesViewModel.garmentCategories.collectAsStateWithLifecycle()
    val staffList by salesViewModel.staffList.collectAsStateWithLifecycle()

    var actionMenuLeadId by remember { mutableStateOf<String?>(null) }
    var leadToDelete by remember { mutableStateOf<LeadTableItem?>(null) }
    var isLoadingEdit by remember { mutableStateOf(false) }
    var isLoadingView by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }

    val filterDrawerState = rememberFilterDrawerState()
    var searchQuery by remember { mutableStateOf("") }

    val currentPage by salesViewModel.currentPage.collectAsStateWithLifecycle()
    val pageSize by salesViewModel.pageSize.collectAsStateWithLifecycle()
    val totalLeads by salesViewModel.totalLeads.collectAsStateWithLifecycle()

    var filterSections by remember {
        mutableStateOf(buildFilterSections(emptyList(), emptyList(), emptyList(), emptyList(), emptyList()))
    }

    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(salesStatuses, garmentCategories, staffList, leads) {
        val dynamicSources = leads.map { it.source }.filter { it.isNotBlank() }.distinct().sorted()
        filterSections = buildFilterSections(filterSections, salesStatuses, garmentCategories, staffList, dynamicSources)
    }

    LaunchedEffect(Unit) {
        salesViewModel.fetchTableLeads()
        salesViewModel.fetchStaff()
        salesViewModel.fetchGarmentCategories()
        salesViewModel.fetchSalesData()
    }

    LaunchedEffect(leadState) {
        when (val state = leadState) {
            is SaleState.Success<*> -> {
                successMessage = "Lead Created Successfully"
                salesViewModel.resetLeadState()
                salesViewModel.fetchSalesData()
                salesViewModel.fetchTableLeads()
            }
            is SaleState.Error -> {
                errorMessage = state.message
                salesViewModel.resetLeadState()
            }
            else -> Unit
        }
    }

    LaunchedEffect(deleteState) {
        when (val currentState = deleteState) {
            is SaleState.Loading -> { isDeleting = true }
            is SaleState.Success<*> -> {
                isDeleting = false
                successMessage = "Lead Deleted Successfully"
                salesViewModel.resetDeleteState()
                salesViewModel.fetchTableLeads()
            }
            is SaleState.Error -> {
                isDeleting = false
                errorMessage = "Failed to delete lead: ${currentState.message}"
                salesViewModel.resetDeleteState()
            }
            else -> { isDeleting = false }
        }
    }

    LaunchedEffect(updateState) {
        when (val state = updateState) {
            is SaleState.Success<*> -> {
                successMessage = "Lead Updated Successfully"
                salesViewModel.fetchTableLeads()
                salesViewModel.resetUpdateState()
            }
            is SaleState.Error -> {
                errorMessage = "Failed to update lead: ${state.message}"
                salesViewModel.resetUpdateState()
            }
            else -> Unit
        }
    }

    fun applyFilters(sections: List<FilterSection>) {
        filterSections = sections
    }

    fun getGarmentName(lead: LeadTableItem): String {
        val garment = lead.garmentCategory?.firstOrNull()
        return if (garment == null) "—"
        else when (garment) {
            is Map<*, *> -> {
                val categoryId = garment["categoryId"] as? Map<*, *>
                categoryId?.get("categoryName") as? String ?: "—"
            }
            is String -> lead.occasion?.takeIf { it.isNotBlank() } ?: "—"
            else -> "—"
        }
    }

    val filteredLeads = leads.filter { lead ->
        // 1. Safe Search Match (null-safety உடன்)
        val personName = lead.person?.name.orEmpty()
        val enquiryType = lead.enquiryType.orEmpty()
        val matchesSearch = searchQuery.isBlank() ||
                personName.contains(searchQuery, ignoreCase = true) ||
                enquiryType.contains(searchQuery, ignoreCase = true)

        // 2. Status Match
        val statusName = when (val status = lead.status) {
            is String -> status
            is Map<*, *> -> (status["name"] as? String) ?: ""
            else -> ""
        }
        val selectedStatusLabels = filterSections.find { it.title == "Status" }
            ?.options?.filter { it.isSelected }?.map { it.label } ?: emptyList()
        val matchesStatus = selectedStatusLabels.isEmpty() ||
                selectedStatusLabels.any { it.equals(statusName, ignoreCase = true) }

        // 3. Source Match
        val selectedSourceLabels = filterSections.find { it.title == "Source" }
            ?.options?.filter { it.isSelected }?.map { it.label } ?: emptyList()
        val matchesSource = selectedSourceLabels.isEmpty() ||
                selectedSourceLabels.any { it.equals(lead.source.orEmpty(), ignoreCase = true) }

        // 4. Garments Match
        val garmentName = getGarmentName(lead)
        val selectedGarmentLabels = filterSections.find { it.title == "Garments" }
            ?.options?.filter { it.isSelected }?.map { it.label } ?: emptyList()
        val matchesGarments = selectedGarmentLabels.isEmpty() ||
                selectedGarmentLabels.any { it.equals(garmentName, ignoreCase = true) }

        // 5. Amount Range Match (null-safe budget check)
        val minAmountFilter = filterSections.find { it.title == "Amount Range" }?.minAmount?.toIntOrNull()
        val maxAmountFilter = filterSections.find { it.title == "Amount Range" }?.maxAmount?.toIntOrNull()
        val leadMinBudget = lead.budgetRange?.min ?: 0
        val leadMaxBudget = lead.budgetRange?.max ?: Int.MAX_VALUE
        val matchesAmount = (minAmountFilter == null || leadMaxBudget >= minAmountFilter) &&
                (maxAmountFilter == null || leadMinBudget <= maxAmountFilter)

        // 6. Priority Match
        val selectedPriority = filterSections.find { it.title == "Priority" }
            ?.options
            ?.find { it.isSelected }
            ?.id

        val matchesPriority = selectedPriority == null || run {
            val priority = lead.appointment?.priority?.lowercase() ?: ""
            when (selectedPriority.lowercase()) {
                "high" -> priority.contains("high")
                "medium" -> priority.contains("medium")
                "low" -> priority.contains("low")
                else -> true
            }
        }

        // 7. Sales Person Match
        val selectedStaffIds = filterSections.find { it.title == "Sales Person" }
            ?.options?.filter { it.isSelected }?.map { it.id } ?: emptyList()
        val assignedStaffId = lead.appointment?.assignedStaff
        val matchesSalesPerson = selectedStaffIds.isEmpty() ||
                (assignedStaffId != null && selectedStaffIds.contains(assignedStaffId))

        matchesSearch && matchesStatus && matchesSource && matchesGarments && matchesPriority && matchesAmount && matchesSalesPerson
    }

    val listState = rememberLazyListState()
    val isLoadingMore by salesViewModel.isLoadingMore.collectAsStateWithLifecycle()
    val canLoadMore by salesViewModel.canLoadMore.collectAsStateWithLifecycle()

    LaunchedEffect(listState) {
        snapshotFlow {
            val info = listState.layoutInfo
            val total = info.totalItemsCount
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            total > 0 && lastVisible >= total - 3
        }
            .distinctUntilChanged()
            .collect { nearEnd ->
                if (nearEnd && canLoadMore && !isLoadingMore) {
                    salesViewModel.loadMoreLeads()
                }
            }
    }

    fun resolveStatusBadge(lead: LeadTableItem): Pair<String, Color> {
        val statusName = when (lead.status) {
            is String -> lead.status
            is Map<*, *> -> (lead.status["name"] as? String) ?: ""
            else -> ""
        }
        return when {
            statusName.contains("Convert", ignoreCase = true) || statusName.equals("CONVERTED", ignoreCase = true) || statusName.equals("converted_to_order", ignoreCase = true) ->
                "Converted to Order" to Color(0xFF34C759)
            statusName.contains("New", ignoreCase = true) || statusName.equals("NEW", ignoreCase = true) || statusName.equals("new_enquiry", ignoreCase = true) ->
                "New Enquiry" to Color(0xFF3B3BF9)
            statusName.contains("Quot", ignoreCase = true) || statusName.equals("QUOTED", ignoreCase = true) ->
                "Quoted" to Color(0xFFF59E0B)
            statusName.contains("Follow", ignoreCase = true) || statusName.equals("FOLLOW_UP", ignoreCase = true) || statusName.contains("Pending", ignoreCase = true) ->
                "Follow-up" to Color(0xFFEF4444)
            statusName.contains("Lost", ignoreCase = true) ->
                "Lost" to Color(0xFF6B7280)
            else -> statusName to Color(0xFF9CA3AF)
        }
    }

    fun onViewClicked(lead: LeadTableItem) {
        actionMenuLeadId = null
        isLoadingView = true
        salesViewModel.fetchLeadDetails(lead.id) { success ->
            isLoadingView = false
            if (success) onViewLead()
            else {
                errorMessage = "Failed to load lead details"
                salesViewModel.selectLead(lead.toLeadEntity())
                onViewLead()
            }
        }
    }

    fun onEditClicked(lead: LeadTableItem) {
        actionMenuLeadId = null
        isLoadingEdit = true
        salesViewModel.fetchLeadDetails(lead.id) { success ->
            isLoadingEdit = false
            if (success) onEditLead()
            else {
                errorMessage = "Failed to load lead details for editing"
                salesViewModel.selectLead(lead.toLeadEntity())
                onEditLead()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        FabScaffold(
            fab = FabConfig(
                label = "Create Lead",
                icon = Icons.Default.Add,
                onClick = onCreateLead,
                bottomPadding = 50.dp
            ),
            modifier = Modifier.fillMaxSize()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    TitleBar("Lead Management", onClose = onClose)
                    Spacer(Modifier.height(8.dp))
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    ScreenBreadcrumb(listOf("Sales", "Lead Management"), onClick = { onBreadCrumbClick() })

                    SearchFilterBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        placeholder = "Search Leads...",
                        accentColor = BluePrimary,
                        borderColor = BorderGray,
                        textSecondaryColor = TextSecondary,
                        onFilterClick = { filterDrawerState.open() }
                    )
                }
                HorizontalDivider(color = title_border)

                Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    when {
                        isLoading -> {
                            ListSkeleton()
                        }
                        tableError != null -> {
                            Box(
                                modifier = Modifier.fillMaxSize().background(whiteBg, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Warning, null, tint = Color.Red, modifier = Modifier.size(48.dp))
                                    Spacer(Modifier.height(8.dp))
                                    Text("Error loading leads", fontSize = tokens.bodyMedium, color = Color.Red, fontWeight = FontWeight.Bold)
                                    Text("Something went wrong, Please try again after sometime", fontSize = tokens.bodyMedium, color = Color.Gray)
                                    Spacer(Modifier.height(12.dp))
                                    Button(onClick = { salesViewModel.fetchTableLeads() }) {
                                        Text("Retry", fontSize = tokens.bodyMedium)
                                    }
                                }
                            }
                        }
                        filteredLeads.isEmpty() -> {
                            Box(
                                modifier = Modifier.fillMaxSize().background(whiteBg, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(Modifier.padding(tokens.screenPadding * 2.5f), horizontalAlignment = Alignment.CenterHorizontally) {
                                    val hasFilters = filterSections.any { section -> section.options.any { it.isSelected } }
                                    Text(
                                        if (searchQuery.isNotBlank() || hasFilters) "No matching leads found" else "No Leads Yet",
                                        fontSize = tokens.h2,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF111827)
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        if (searchQuery.isNotBlank() || hasFilters) "Try adjusting your search or filter" else "Start by creating your first lead",
                                        fontSize = tokens.bodyMedium,
                                        color = Color(0xFF9CA3AF)
                                    )
                                    Spacer(Modifier.height(20.dp))
                                    Button(
                                        onClick = onCreateLead,
                                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(
                                            horizontal = tokens.screenPadding,
                                            vertical = tokens.screenPadding * 0.6f
                                        )
                                    ) {
                                        Text("Create Lead", fontSize = tokens.bodyMedium, color = whiteBg, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                        else -> {
                            Column(modifier = Modifier.fillMaxSize()) {
                                LazyColumn(
                                    state = listState,
                                    modifier = Modifier.weight(1f).fillMaxWidth()
                                ) {
                                    items(filteredLeads, key = { it.id }) { lead ->
                                        val (badgeText, badgeColor) = resolveStatusBadge(lead)
                                        DataCard(
                                            item = lead,
                                            dateText = "Order ID: order id not found",
                                            showDateIcon = false,
                                            topBadgeText = badgeText,
                                            topBadgeTextColor = badgeColor,
                                            topBadgeBgColor = badgeColor.copy(alpha = 0.14f),
                                            title = lead.person.name.ifEmpty { "—" },
                                            subtitle = "${formatLeadDate(lead.requiredDate?.ifEmpty { "—" })} • ${getGarmentName(lead)} • Qty ${if (lead.estimatedQuantity == 0) "—" else lead.estimatedQuantity.toString()}",
                                            footerFields = listOf(
                                                DataCardField(
                                                    icon = Icons.Default.AttachMoney,
                                                    iconTint = Color(0xFF6366F1),
                                                    iconBackgroundColor = primary_light,
                                                    iconCircleSize = 24.dp,
                                                    text = "₹${formatIndianNumber(lead.budgetRange.min)} - ₹${formatIndianNumber(lead.budgetRange.max)}",
                                                    textColor = Color(0xFF374151)
                                                )
                                            ),
                                            actions = listOf(
                                                MenuAction("View", Icons.Default.Visibility, enabled = !isLoadingView) { onViewClicked(lead) },
                                                MenuAction("Edit", Icons.Default.Edit, enabled = !isLoadingEdit) { onEditClicked(lead) },
                                                MenuAction("Delete", Icons.Default.Delete, tint = Color(0xFFF44336), textColor = Color(0xFFF44336), enabled = !isDeleting) { leadToDelete = lead }
                                            )
                                        )
                                    }

                                    if (isLoadingMore) {
                                        item {
                                            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                                CirculerProgressIndicatorSmall()
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

        DynamicIslandSuccess(
            modifier = Modifier.align(Alignment.TopCenter),
            message = successMessage,
            onDismiss = { successMessage = null }
        )

        DynamicIslandError(
            modifier = Modifier.align(Alignment.TopCenter),
            message = errorMessage,
            onDismiss = { errorMessage = null }
        )
    }

    if (leadToDelete != null) {
        DeleteModel(
            title = "Delete Lead",
            message = "Are you sure you want to delete this lead? This action cannot be undone.",
            onDismiss = { leadToDelete = null },
            onDelete = {
                salesViewModel.deleteLead(leadToDelete!!.id)
                leadToDelete = null
            }
        )
    }

    FilterDrawer(
        state = filterDrawerState,
        title = "Filters",
        sections = filterSections,
        onApply = { updatedSections -> applyFilters(updatedSections) },
        onClearAll = {
            filterSections = filterSections.map { section ->
                section.copy(options = section.options.map { option -> option.copy(isSelected = false) })
            }
        }
    )
}

@Composable
fun MiniSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier
            .width(30.dp)
            .height(18.dp)
            .clip(RoundedCornerShape(50))
            .background(if (checked) Primary else grey_border)
            .border(
                width = 1.dp,
                color = if (checked) Primary else Color(0xFFD1D5DB),
                shape = RoundedCornerShape(50)
            )
            .alpha(if (enabled) 1f else 0.5f)
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
    ) {
        Box(
            modifier = Modifier
                .padding(2.dp)
                .size(14.dp)
                .offset(x = if (checked) 12.dp else 0.dp)
                .clip(CircleShape)
                .background(whiteBg)
        )
    }
}

@Composable
fun ViewFieldValue(label: String, value: String?) {
    val tokens = LocalAppTokens.current
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text(label, fontSize = tokens.caption, color = LeadmutedText, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(2.dp))
        if (value != null) {
            Text(value, fontSize = tokens.bodyMedium, color = Color(0xFF111827), fontWeight = FontWeight.Normal)
        }
    }
    HorizontalDivider(color = Color(0xFFF5F5F5), modifier = Modifier.padding(top = 4.dp))
}

// ─────────────────────────────────────────────────────────────
// Reusable Budget Range Slider
// ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetRangeSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 1000f..250000f,
    enabled: Boolean = true
) {
    Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = valueRange,
        enabled = enabled,
        modifier = modifier.fillMaxWidth(),
        colors = SliderDefaults.colors(
            thumbColor = LeadPrimary,
            activeTrackColor = LeadPrimary,
            inactiveTrackColor = grey_border
        ),
        thumb = {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .background(whiteBg, CircleShape)
                    .border(3.dp, LeadPrimary, CircleShape)
            )
        },
        track = { sliderState ->
            val fraction = (sliderState.value - sliderState.valueRange.start) /
                    (sliderState.valueRange.endInclusive - sliderState.valueRange.start)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(grey_border)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .background(LeadPrimary)
                )
            }
        }
    )
}

@Composable
fun BudgetRangeLabels(
    currentValue: Int,
    min: Int = 1000,
    max: Int = 250000
) {
    val tokens = LocalAppTokens.current
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("₹${formatIndianNumber(min)}", fontSize = tokens.caption, color = Color(0xFF6B7280))
        Text("₹${formatIndianNumber(currentValue)}", fontSize = tokens.caption, fontWeight = FontWeight.Bold, color = LeadPrimary)
        Text("₹${formatIndianNumber(max)}", fontSize = tokens.caption, color = Color(0xFF6B7280))
    }
}