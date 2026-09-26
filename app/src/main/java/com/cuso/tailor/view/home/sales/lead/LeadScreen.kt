@file:Suppress(
    "UNUSED_VALUE",
    "unused_variable",
    "SpellCheckingInspection",
    "GrazieInspection",
    "AssignedValueIsNeverRead",
    "VariableNeverRead",
    "unused"
)

package com.cuso.tailor.view.home.sales.lead

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.R
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.sales.CreateLeadFormRequest
import com.cuso.tailor.model.sales.GarmentCategoryDto
import com.cuso.tailor.model.sales.LeadTableItem
import com.cuso.tailor.model.sales.toLeadEntity
import com.cuso.tailor.model.settings.GarmentItem
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.view.home.formatIndianNumber
import com.cuso.tailor.view.home.formatLeadDate
import com.cuso.tailor.view.home.sales.sales_order.OrderReviewData
import com.cuso.tailor.view.home.toIsoDate
import com.cuso.tailor.viewmodel.BranchUiState
import com.cuso.tailor.viewmodel.BranchViewModel
import com.cuso.tailor.viewmodel.SaleState
import com.cuso.tailor.viewmodel.SalesViewModel
import com.cuso.tailor.viewmodel.SettingsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.collections.filter

enum class LeadFormMode {
    CREATE, VIEW, EDIT
}

data class LeadGarmentItemRow(
    val templateId: String = "",
    val templateName: String = "",
    val categoryId: String = "",
    val categoryName: String = "",
    val quantity: String = "1",
    val availableCategories: List<GarmentCategoryDto> = emptyList(),
    val isLoadingCategories: Boolean = false
)

@Composable
fun LeadFormTopBar(
    title: String,
    badgeText: String,
    badgeColor: Color = Primary,
    onClose: () -> Unit,
    isConverted: Boolean = false,
    isViewMode: Boolean = false,
    onConvertToOpportunity: () -> Unit = {}
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
        Text(title, fontSize = tokens.h1, fontWeight = FontWeight.Bold, color = title_color)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
        ) {
            // Conversion pill or button is displayed strictly in VIEW mode only
            if (isViewMode) {
                if (isConverted) {
                    // Display green status pill when lead is already converted
                    Surface(
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 1.5f),
                        color = greenBg,
                        border = BorderStroke(1.dp, darkGreenBg.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(
                                horizontal = tokens.screenPadding * 0.75f,
                                vertical = tokens.screenPadding * 0.375f
                            ),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = darkGreenBg,
                                modifier = Modifier.size(tokens.iconSize * 0.75f)
                            )
                            Text(
                                text = "Converted to Opportunity",
                                fontSize = tokens.caption,
                                fontWeight = FontWeight.SemiBold,
                                color = darkGreenBg
                            )
                        }
                    }
                } else {
                    // Display clickable convert button when not yet converted
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 1.5f))
                            .background(Color(0xFF3730A3))
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { onConvertToOpportunity() }
                            .padding(
                                horizontal = tokens.screenPadding * 0.75f,
                                vertical = tokens.screenPadding * 0.375f
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = null,
                            tint = whiteBg,
                            modifier = Modifier.size(tokens.iconSize * 0.8f)
                        )
                        Text(
                            text = "Convert to Opportunity",
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.SemiBold,
                            color = whiteBg
                        )
                    }
                }
            }

            Icon(
                Icons.Default.Close,
                contentDescription = "Close",
                tint = close_color,
                modifier = Modifier
                    .size(tokens.iconSize * 1.1f)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onClose() }
            )
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
                .background(primary_light, RoundedCornerShape(tokens.cardCornerRadius * 0.7f))
                .padding(horizontal = tokens.screenPadding * 0.85f, vertical = tokens.screenPadding * 0.75f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(tokens.iconSize)
            )
            Spacer(Modifier.width(tokens.extraPadding))
            Text(
                text = text,
                fontSize = tokens.bodySmall,
                color = TextLog,
                modifier = Modifier.weight(1f)
            )
            Icon(
                Icons.Default.Close,
                contentDescription = "Dismiss",
                tint = mutedText,
                modifier = Modifier
                    .size(tokens.iconSize * 0.9f)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { visible = false }
            )
        }
    }
}

@SuppressLint("AutoboxingStateCreation")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeadFormScreen(
    mode: LeadFormMode,
    onBack: () -> Unit,
    onEditRequested: () -> Unit = {},
    onConvertToOrder: (OrderReviewData) -> Unit = {},
    onConvertToOpportunity: () -> Unit = {},
    salesViewModel: SalesViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    branchViewModel: BranchViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current

    val isCreate = mode == LeadFormMode.CREATE
    val isView = mode == LeadFormMode.VIEW
    val isEdit = mode == LeadFormMode.EDIT
    val isEditable = !isView

    val selectedLead by salesViewModel.selectedLead.collectAsStateWithLifecycle()
    val l = if (isCreate) null else selectedLead

    val leadState by salesViewModel.leadState.collectAsStateWithLifecycle()
    val updateState by salesViewModel.updateState.collectAsStateWithLifecycle()

    val staffList by salesViewModel.staffList.collectAsStateWithLifecycle()
    val salesStatuses by salesViewModel.salesStatuses.collectAsStateWithLifecycle()
    val allGarments by settingsViewModel.garments.collectAsStateWithLifecycle()
    val garmentCategories by salesViewModel.garmentCategories.collectAsStateWithLifecycle()
    val branchState by branchViewModel.uiState.collectAsStateWithLifecycle()

    val apiLeadSources by salesViewModel.leadSources.collectAsStateWithLifecycle()
    val apiGenderOptions by salesViewModel.genderOptions.collectAsStateWithLifecycle()
    val apiContactOptions by salesViewModel.preferredContactOptions.collectAsStateWithLifecycle()
    val apiEnquiryOptions by salesViewModel.enquiryTypeOptions.collectAsStateWithLifecycle()
    val apiPriorityOptions by salesViewModel.priorityOptions.collectAsStateWithLifecycle()
    val leadsList by salesViewModel.tableLeads.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        branchViewModel.loadBranches()
        salesViewModel.fetchStaff()
        settingsViewModel.fetchGarments()
        salesViewModel.fetchGarmentCategories()
        salesViewModel.fetchSalesData()
    }

    val branches = (branchState as? BranchUiState.Success)?.branches ?: emptyList()

    val activeGarments: List<GarmentItem> = remember(allGarments) {
        allGarments.filter { it.status.equals("Active", ignoreCase = true) }
    }
    val garmentTemplateOptions = remember(activeGarments) {
        activeGarments.map { it.displayName?.takeIf { n -> n.isNotBlank() } ?: it.name }
    }

    val leadSourceOptions = remember(apiLeadSources, leadsList) {
        (apiLeadSources + leadsList.map { it.effectiveSource }.filter { it.isNotBlank() && it != "—" })
            .distinct()
            .ifEmpty { listOf("No Lead Source Found") }
    }
    val genderOptions = remember(apiGenderOptions) {
        apiGenderOptions.ifEmpty { listOf("Male", "Female", "Other") }
    }
    val preferredContactOptions = remember(apiContactOptions) {
        apiContactOptions.ifEmpty { listOf("Whatsapp", "Call", "Email", "SMS") }
    }
    val enquiryClassificationOptions = remember(apiEnquiryOptions, leadsList) {
        (apiEnquiryOptions + leadsList.mapNotNull { it.enquiryType?.takeIf { e -> e.isNotBlank() } })
            .distinct()
            .ifEmpty { listOf("New Order", "Alteration", "Repair", "Custom Tailoring") }
    }
    val priorityOptions = remember(apiPriorityOptions) {
        apiPriorityOptions.ifEmpty { listOf("Low", "Medium", "High", "Urgent") }
    }
    val fabricSourceOptions = listOf("Customer_Provided", "Store_Provided", "Other")
    val lifecycleStatusOptions = listOf("Active", "Inactive", "Archived")
    val appointmentStatusOptions = listOf("None", "Scheduled", "Confirmed", "Completed", "Cancelled")

    val staffDisplayList = remember(staffList) {
        staffList.map { "${it.firstName} ${it.lastName} - ${it.memberId}" }
    }
    val staffIdMap = remember(staffList) {
        staffList.associate { "${it.firstName} ${it.lastName} - ${it.memberId}" to it.id }
    }
    val statusOptions = remember(salesStatuses) {
        salesStatuses.map { it.name }.ifEmpty { listOf("New", "Scheduled", "In_Progress", "Contacted", "Qualified", "Not_Qualified", "Lost", "Junk") }
    }

    // Lead information form states
    var leadSource by remember { mutableStateOf(l?.source ?: "-") }
    var enquiryDate by remember { mutableStateOf(l?.let { formatLeadDate(it.enquiryDate) } ?: "-") }
    var leadOwner by remember { mutableStateOf(l?.leadOwner ?: "") }
    var pipelineStatus by remember { mutableStateOf(l?.status ?: "New") }
    var lifecycleStatus by remember { mutableStateOf("Active") }

    var customerType by remember { mutableStateOf(l?.customerType?.replaceFirstChar { c -> c.uppercase() } ?: "-") }
    var fullName by remember { mutableStateOf(l?.fullName ?: "") }
    var mobileNumber by remember { mutableStateOf(l?.phone ?: "") }
    var emailAddress by remember { mutableStateOf(l?.email ?: "") }
    var gender by remember { mutableStateOf(l?.gender ?: "") }
    var dateOfBirth by remember { mutableStateOf(l?.let { formatLeadDate(it.dob) } ?: "") }
    var selectedIso by remember { mutableStateOf("-") }

    var flatDoorNo by remember { mutableStateOf("") }
    var streetLandmark by remember { mutableStateOf(l?.address ?: "") }
    var areaZone by remember { mutableStateOf(l?.area ?: "") }
    var city by remember { mutableStateOf(l?.city ?: "") }
    var stateProvince by remember { mutableStateOf("") }
    var pincode by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("-") }
    var preferredContact by remember { mutableStateOf(l?.preferredContactMethod ?: "-") }

    var enquiryClassification by remember { mutableStateOf(l?.enquiryType ?: "-") }
    var requiredCompletionDate by remember { mutableStateOf(l?.let { formatLeadDate(it.requiredDate) } ?: "") }
    var minBudget by remember { mutableStateOf(l?.budgetMin?.toString() ?: "1500") }
    var maxBudget by remember { mutableStateOf(l?.budgetMax?.toString() ?: "50000") }

    var garmentSpecs by remember {
        mutableStateOf(listOf(LeadGarmentItemRow(quantity = l?.estimatedQuantity?.takeIf { it > 0 }?.toString() ?: "-")))
    }

    var fabricProvided by remember { mutableStateOf(false) }
    var fabricSource by remember { mutableStateOf("") }
    var fabricNotes by remember { mutableStateOf("") }

    var appointmentRequired by remember { mutableStateOf(l?.appointmentRequired ?: false) }
    var assignedStaff by remember { mutableStateOf(l?.assignedStaff ?: "") }
    var priorityLevel by remember { mutableStateOf(l?.priority ?: "-") }
    var nextFollowUpDate by remember { mutableStateOf(l?.let { formatLeadDate(it.followUpDate) } ?: "") }
    var appointmentDate by remember { mutableStateOf(l?.let { formatLeadDate(it.appointmentDate) } ?: "") }
    var appointmentTime by remember { mutableStateOf(l?.appointmentTime ?: "-") }
    var appointmentStatus by remember { mutableStateOf("-") }

    var internalNotes by remember { mutableStateOf(l?.internalNotes ?: "") }
    var customerNotes by remember { mutableStateOf(l?.customerNotes ?: "") }

    var leadSourceExpanded by remember { mutableStateOf(false) }
    var leadOwnerExpanded by remember { mutableStateOf(false) }
    var pipelineStatusExpanded by remember { mutableStateOf(false) }
    var lifecycleStatusExpanded by remember { mutableStateOf(false) }
    var genderExpanded by remember { mutableStateOf(false) }
    var preferredContactExpanded by remember { mutableStateOf(false) }
    var enquiryClassificationExpanded by remember { mutableStateOf(false) }
    var fabricSourceExpanded by remember { mutableStateOf(false) }
    var assignedStaffExpanded by remember { mutableStateOf(false) }
    var priorityLevelExpanded by remember { mutableStateOf(false) }
    var appointmentStatusExpanded by remember { mutableStateOf(false) }

    var expandedSection by remember { mutableStateOf("-") }

    var validationError by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorField by remember { mutableStateOf<String?>(null) }

    val leadOwnerLabel = staffIdMap.entries.firstOrNull { it.value == leadOwner }?.key ?: ""
    val assignedStaffLabel = staffIdMap.entries.firstOrNull { it.value == assignedStaff }?.key ?: ""

    val customerTypeTabs = remember {
        listOf(
            TabItem(label = "Individual", icon = Icons.Default.Person),
            TabItem(label = "Corporate", icon = Icons.Default.Business)
        )
    }

    // Determine conversion state by matching lead ID against leadsList
    val matchingLeadItem = remember(l?.id, leadsList) {
        leadsList.find { it.id == l?.id }
    }
    val isLeadConverted = matchingLeadItem?.isConverted == true ||
            l?.status?.contains("Convert", ignoreCase = true) == true

    fun submitLeadForm() {
        if (!isEditable) return

        if (fullName.isBlank()) {
            errorField = "fullName"
            validationError = "Full Name is required"
            expandedSection = "customer"
            return
        }
        if (mobileNumber.isBlank()) {
            errorField = "mobileNumber"
            validationError = "Mobile Number is required"
            expandedSection = "customer"
            return
        }
        if (enquiryDate.isBlank()) {
            errorField = "enquiryDate"
            validationError = "Enquiry Date is required"
            expandedSection = "lead_info"
            return
        }
        if (requiredCompletionDate.isBlank()) {
            errorField = "requiredCompletionDate"
            validationError = "Required Completion Date is required"
            expandedSection = "enquiry"
            return
        }

        fun safeIsoDate(dateStr: String): String {
            return if (dateStr.isNotBlank()) dateStr.toIsoDate().ifBlank { dateStr } else ""
        }

        val activeBranchId = (branchViewModel.uiState.value as? BranchUiState.Success)
            ?.branches?.firstOrNull()?.id

        val parsedMinBudget = minBudget.toIntOrNull() ?: 0
        val parsedMaxBudget = maxBudget.toIntOrNull() ?: 0

        val garmentsPayload = garmentSpecs.mapNotNull { row ->
            if (row.templateId.isNotBlank() && row.categoryId.isNotBlank()) {
                com.cuso.tailor.model.sales.LeadGarmentSpecificationPayload(
                    garmentId = row.templateId,
                    garmentCategoryId = row.categoryId,
                    quantity = row.quantity.toIntOrNull() ?: 1
                )
            } else null
        }

        val addressPayload = com.cuso.tailor.model.sales.LeadAddressDto(
            flatNo = flatDoorNo,
            street = streetLandmark,
            areaZone = areaZone,
            city = city,
            subdivisionName = stateProvince,
            pincode = pincode
        )

        val request = CreateLeadFormRequest(
            branchId = activeBranchId,
            leadSource = leadSource,
            enquiryDate = safeIsoDate(enquiryDate),
            leadOwner = leadOwner,
            leadStatus = pipelineStatus,
            customerType = customerType,
            fullName = fullName,
            mobileNumber = mobileNumber,
            email = emailAddress.takeIf { it.isNotBlank() },
            gender = gender.takeIf { it.isNotBlank() },
            dateOfBirth = safeIsoDate(dateOfBirth).takeIf { it.isNotBlank() },
            preferredContactMethod = preferredContact,
            enquiryType = enquiryClassification,
            requiredDate = safeIsoDate(requiredCompletionDate),
            address = addressPayload,
            garmentSpecifications = garmentsPayload,
            budgetMin = parsedMinBudget,
            budgetMax = parsedMaxBudget,
            isFabricProvided = fabricProvided,
            fabricSource = fabricSource.takeIf { fabricProvided && it.isNotBlank() },
            fabricNotes = fabricNotes.takeIf { fabricProvided && it.isNotBlank() },
            isAppointmentRequired = appointmentRequired,
            appointmentDate = if (appointmentRequired) safeIsoDate(appointmentDate) else null,
            appointmentTime = if (appointmentRequired) appointmentTime.takeIf { it.isNotBlank() } else null,
            appointmentStatus = if (appointmentRequired) appointmentStatus else "None",
            assignedStaffId = assignedStaff.takeIf { it.isNotBlank() },
            followUpDate = safeIsoDate(nextFollowUpDate).takeIf { it.isNotBlank() },
            priorityLevel = priorityLevel,
            internalNotes = internalNotes.takeIf { it.isNotBlank() },
            customerNotes = customerNotes.takeIf { it.isNotBlank() },
            status = lifecycleStatus
        )

        if (isCreate) {
            salesViewModel.createLead(request)
        } else if (isEdit && l != null) {
            salesViewModel.updateLeadById(l.id, request)
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
                delay(1000)
                salesViewModel.resetUpdateState()
                onBack()
            }
            is SaleState.Error -> {
                validationError = "Update failed: ${state.message}"
                salesViewModel.resetUpdateState()
            }
            else -> Unit
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0)
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(modifier = Modifier.fillMaxSize()) {
                LeadFormTopBar(
                    title = if (isCreate) "Create Lead" else if (isEdit) "Edit Lead" else "View Lead",
                    badgeText = if (isLeadConverted) "Converted" else pipelineStatus,
                    onClose = onBack,
                    isConverted = isLeadConverted,
                    isViewMode = isView, // Controls visibility so conversion options appear only in VIEW mode
                    onConvertToOpportunity = onConvertToOpportunity
                )
                HorizontalDivider(color = title_border)

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    if (isEditable) {
                        item {
                            Spacer(Modifier.height(tokens.screenPadding * 0.75f))
                            Box(modifier = Modifier.padding(horizontal = tokens.screenPadding)) {
                                LeadInfoBanner("Complete all mandatory fields marked with an asterisk (*).")
                            }
                        }
                    }

                    // Section 1: Lead Information
                    item {
                        AccordionSection(
                            iconPainter = painterResource(R.drawable.ic_person),
                            title = "Lead Information",
                            expanded = expandedSection == "lead_info",
                            onHeaderClick = {
                                expandedSection = if (expandedSection == "lead_info") "" else "lead_info"
                            }
                        ) {
                            Text("Core branch, inquiry, and ownership details", fontSize = tokens.caption, color = headerGrey)
                            Spacer(Modifier.height(14.dp))

                            FormDropdown(
                                label = "Lead Source",
                                value = leadSource.ifEmpty { "Select an option" },
                                expanded = leadSourceExpanded && isEditable,
                                onExpandChange = { if (isEditable) leadSourceExpanded = it },
                                options = leadSourceOptions,
                                onOptionSelected = { leadSource = it },
                                enabled = isEditable
                            )

                            Spacer(Modifier.height(14.dp))
                            FormLabel("Enquiry Date", isRequired = true)
                            DatePickerField(
                                value = enquiryDate,
                                onDateSelected = { if (isEditable) enquiryDate = it },
                                isError = errorField == "enquiryDate",
                                enabled = isEditable
                            )

                            Spacer(Modifier.height(14.dp))
                            FormDropdown(
                                label = "Lead Owner / Assigned Rep",
                                value = leadOwnerLabel.ifEmpty { "Select an option" },
                                expanded = leadOwnerExpanded && isEditable,
                                onExpandChange = { if (isEditable) leadOwnerExpanded = it },
                                options = staffDisplayList,
                                onOptionSelected = { label -> leadOwner = staffIdMap[label] ?: "" },
                                enabled = isEditable
                            )

                            Spacer(Modifier.height(14.dp))
                            FormDropdown(
                                label = "Lead Pipeline Status",
                                value = pipelineStatus.ifEmpty { "Select an option" },
                                expanded = pipelineStatusExpanded && isEditable,
                                onExpandChange = { if (isEditable) pipelineStatusExpanded = it },
                                options = statusOptions,
                                onOptionSelected = { pipelineStatus = it },
                                enabled = isEditable
                            )

                            Spacer(Modifier.height(14.dp))
                            FormDropdown(
                                label = "Lifecycle Status",
                                value = lifecycleStatus,
                                expanded = lifecycleStatusExpanded && isEditable,
                                onExpandChange = { if (isEditable) lifecycleStatusExpanded = it },
                                options = lifecycleStatusOptions,
                                onOptionSelected = { lifecycleStatus = it },
                                enabled = isEditable
                            )
                        }
                    }

                    // Section 2: Customer Identity
                    item {
                        AccordionSection(
                            iconPainter = painterResource(R.drawable.ic_date_of_birth),
                            title = "Customer Identity",
                            expanded = expandedSection == "customer",
                            onHeaderClick = {
                                expandedSection = if (expandedSection == "customer") "" else "customer"
                            }
                        ) {
                            Text("Customer personal identification and communication parameters", fontSize = tokens.caption, color = headerGrey)
                            Spacer(Modifier.height(14.dp))

                            SettingsTabs(
                                tabs = customerTypeTabs,
                                selectedIndex = if (customerType.equals("Corporate", ignoreCase = true)) 1 else 0,
                                onTabSelected = { index ->
                                    if (isEditable) {
                                        customerType = if (index == 0) "Individual" else "Corporate"
                                    }
                                },
                                containerColor = whiteBg,
                                selectedBackgroundColor = primary_light,
                                selectedTextColor = Primary,
                                unselectedTextColor = TextSecondary,
                                selectedIconColor = Primary,
                                unselectedIconColor = TextSecondary,
                                borderColor = grey_border,
                                cornerRadius = tokens.cardCornerRadius * 0.8f,
                                selectedCornerRadius = tokens.cardCornerRadius * 0.65f
                            )

                            Spacer(Modifier.height(14.dp))
                            FormLabel("Full Name", isRequired = true)
                            FormTextField(
                                value = fullName,
                                onValueChange = { fullName = it },
                                placeholder = "e.g. John Doe",
                                isError = errorField == "fullName",
                                enabled = isEditable
                            )

                            Spacer(Modifier.height(14.dp))
                            FormLabel("Mobile Number", isRequired = true)
                            PhoneInputField(
                                phoneValue = mobileNumber,
                                onPhoneChange = { mobileNumber = it },
                                onCountryChange = { selectedIso = it.iso },
                                isError = errorField == "mobileNumber",
                                enabled = isEditable
                            )

                            Spacer(Modifier.height(14.dp))
                            FormLabel("Email Address")
                            FormTextField(
                                value = emailAddress,
                                onValueChange = { emailAddress = it },
                                placeholder = "client@example.com",
                                enabled = isEditable
                            )

                            if (customerType.equals("Individual", ignoreCase = true)) {
                                Spacer(Modifier.height(14.dp))
                                FormDropdown(
                                    label = "Gender",
                                    value = gender.ifEmpty { "Select an option" },
                                    expanded = genderExpanded && isEditable,
                                    onExpandChange = { if (isEditable) genderExpanded = it },
                                    options = genderOptions,
                                    onOptionSelected = { gender = it },
                                    enabled = isEditable
                                )

                                Spacer(Modifier.height(14.dp))
                                FormLabel("Date of Birth")
                                DatePickerField(
                                    value = dateOfBirth,
                                    onDateSelected = { if (isEditable) dateOfBirth = it },
                                    enabled = isEditable
                                )
                            }
                        }
                    }

                    // Section 3: Location & Communication
                    item {
                        AccordionSection(
                            iconPainter = painterResource(R.drawable.ic_location),
                            title = "Location & Communication",
                            expanded = expandedSection == "location",
                            onHeaderClick = {
                                expandedSection = if (expandedSection == "location") "" else "location"
                            }
                        ) {
                            Text("Delivery address snapshot & contact preferences", fontSize = tokens.caption, color = headerGrey)
                            Spacer(Modifier.height(14.dp))

                            FormLabel("Flat / Door / Building No")
                            FormTextField(value = flatDoorNo, onValueChange = { flatDoorNo = it }, placeholder = "e.g. Flat 4B, Emerald Tower", enabled = isEditable)

                            Spacer(Modifier.height(14.dp))
                            FormLabel("Street Name / Landmark")
                            FormTextField(value = streetLandmark, onValueChange = { streetLandmark = it }, placeholder = "e.g. MG Road, Near Metro Pillar 120", enabled = isEditable)

                            Spacer(Modifier.height(14.dp))
                            FormLabel("Area / Zone")
                            FormTextField(value = areaZone, onValueChange = { areaZone = it }, placeholder = "e.g. Indiranagar Zone 2", enabled = isEditable)

                            Spacer(Modifier.height(14.dp))
                            FormLabel("City")
                            FormTextField(value = city, onValueChange = { city = it }, placeholder = "e.g. Bengaluru", enabled = isEditable)

                            Spacer(Modifier.height(14.dp))
                            FormLabel("State / Province")
                            FormTextField(value = stateProvince, onValueChange = { stateProvince = it }, placeholder = "e.g. Karnataka", enabled = isEditable)

                            Spacer(Modifier.height(14.dp))
                            FormLabel("Pincode / Postal Code")
                            FormTextField(value = pincode, onValueChange = { pincode = it }, placeholder = "e.g. 560038", keyboardType = KeyboardType.Number, enabled = isEditable)

                            Spacer(Modifier.height(14.dp))
                            FormLabel("Country")
                            FormTextField(value = country, onValueChange = { country = it }, enabled = isEditable)

                            Spacer(Modifier.height(14.dp))
                            FormDropdown(
                                label = "Preferred Contact Method",
                                value = preferredContact,
                                expanded = preferredContactExpanded && isEditable,
                                onExpandChange = { if (isEditable) preferredContactExpanded = it },
                                options = preferredContactOptions,
                                onOptionSelected = { preferredContact = it },
                                enabled = isEditable
                            )
                        }
                    }

                    // Section 4: Enquiry Details
                    item {
                        AccordionSection(
                            iconPainter = painterResource(R.drawable.ic_message),
                            title = "Enquiry Details",
                            expanded = expandedSection == "enquiry",
                            onHeaderClick = {
                                expandedSection = if (expandedSection == "enquiry") "" else "enquiry"
                            }
                        ) {
                            Text("Order classification, requirements, quantities, and budget", fontSize = tokens.caption, color = headerGrey)
                            Spacer(Modifier.height(14.dp))

                            FormDropdown(
                                label = "Enquiry Classification",
                                value = enquiryClassification,
                                expanded = enquiryClassificationExpanded && isEditable,
                                onExpandChange = { if (isEditable) enquiryClassificationExpanded = it },
                                options = enquiryClassificationOptions,
                                onOptionSelected = { enquiryClassification = it },
                                enabled = isEditable
                            )

                            Spacer(Modifier.height(14.dp))
                            FormLabel("Required Completion Date", isRequired = true)
                            DatePickerField(
                                value = requiredCompletionDate,
                                onDateSelected = { if (isEditable) requiredCompletionDate = it },
                                isError = errorField == "requiredCompletionDate",
                                enabled = isEditable
                            )

                            Spacer(Modifier.height(tokens.extraPadding))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Garment Specifications *",
                                        fontSize = tokens.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = "Specify garment templates, categories, and expected quantities",
                                        fontSize = tokens.caption,
                                        color = headerGrey
                                    )
                                }
                                if (isEditable) {
                                    Spacer(Modifier.width(8.dp))
                                    TextButton(
                                        onClick = { garmentSpecs = garmentSpecs + LeadGarmentItemRow() },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Add",
                                            tint = Primary,
                                            modifier = Modifier.size(tokens.iconSize * 0.8f)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = "Add Garment",
                                            fontSize = tokens.caption,
                                            color = Primary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(tokens.extraPadding * 0.6f))

                            garmentSpecs.forEachIndexed { index, specRow ->
                                var templateExpanded by remember { mutableStateOf(false) }
                                var categoryExpanded by remember { mutableStateOf(false) }

                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = whiteBg),
                                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                                    border = BorderStroke(1.dp, BorderGray)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Garment Item #${index + 1}", fontSize = tokens.caption, fontWeight = FontWeight.Bold, color = TextPrimary)
                                            if (isEditable && garmentSpecs.size > 1) {
                                                IconButton(
                                                    onClick = { garmentSpecs = garmentSpecs.filterIndexed { i, _ -> i != index } },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(Icons.Default.Close, contentDescription = "Delete", tint = redText, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }

                                        Spacer(Modifier.height(8.dp))
                                        FormDropdown(
                                            label = "GARMENT TEMPLATE *",
                                            value = specRow.templateName.ifEmpty { "Select Garment" },
                                            expanded = templateExpanded && isEditable,
                                            onExpandChange = { if (isEditable) templateExpanded = it },
                                            options = garmentTemplateOptions,
                                            enabled = isEditable,
                                            onOptionSelected = { selectedTemplateName ->
                                                val selectedGarmentId = activeGarments.find {
                                                    (it.displayName?.takeIf { n -> n.isNotBlank() } ?: it.name) == selectedTemplateName
                                                }?.id ?: ""

                                                garmentSpecs = garmentSpecs.mapIndexed { i, row ->
                                                    if (i == index) row.copy(
                                                        templateId = selectedGarmentId,
                                                        templateName = selectedTemplateName,
                                                        categoryId = "",
                                                        categoryName = "",
                                                        isLoadingCategories = true
                                                    ) else row
                                                }

                                                salesViewModel.fetchGarmentCategoriesByGarmentId(selectedGarmentId) { categories ->
                                                    garmentSpecs = garmentSpecs.mapIndexed { i, row ->
                                                        if (i == index) row.copy(
                                                            availableCategories = categories,
                                                            isLoadingCategories = false
                                                        ) else row
                                                    }
                                                }
                                            }
                                        )

                                        Spacer(Modifier.height(8.dp))
                                        FormDropdown(
                                            label = "GARMENT CATEGORY / CUT PROFILE",
                                            value = when {
                                                specRow.isLoadingCategories -> "Loading categories..."
                                                specRow.categoryName.isNotBlank() -> specRow.categoryName
                                                specRow.templateId.isBlank() -> "Select Garment First"
                                                else -> "Select Category"
                                            },
                                            expanded = categoryExpanded && !specRow.isLoadingCategories && specRow.templateId.isNotBlank() && isEditable,
                                            onExpandChange = { if (isEditable) categoryExpanded = it },
                                            options = specRow.availableCategories.map { it.displayName }.filter { it.isNotBlank() },
                                            enabled = isEditable,
                                            onOptionSelected = { selectedCategoryName ->
                                                val selectedCatId = specRow.availableCategories.find {
                                                    it.displayName == selectedCategoryName
                                                }?.id ?: ""

                                                garmentSpecs = garmentSpecs.mapIndexed { i, row ->
                                                    if (i == index) row.copy(
                                                        categoryId = selectedCatId,
                                                        categoryName = selectedCategoryName
                                                    ) else row
                                                }
                                            }
                                        )

                                        Spacer(Modifier.height(8.dp))
                                        FormLabel("QUANTITY *")
                                        FormTextField(
                                            value = specRow.quantity,
                                            onValueChange = { qty ->
                                                if (isEditable) {
                                                    garmentSpecs = garmentSpecs.mapIndexed { i, row ->
                                                        if (i == index) row.copy(quantity = qty) else row
                                                    }
                                                }
                                            },
                                            keyboardType = KeyboardType.Number,
                                            enabled = isEditable
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(20.dp))
                            Text("Customer Expected Budget Range (INR)", fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Spacer(Modifier.height(8.dp))

                            FormLabel("MINIMUM BUDGET (₹)")
                            FormTextField(
                                value = minBudget,
                                onValueChange = { minBudget = it },
                                placeholder = "e.g. 1500",
                                keyboardType = KeyboardType.Number,
                                enabled = isEditable
                            )

                            Spacer(Modifier.height(14.dp))
                            FormLabel("MAXIMUM BUDGET (₹)")
                            FormTextField(
                                value = maxBudget,
                                onValueChange = { maxBudget = it },
                                placeholder = "e.g. 50000",
                                keyboardType = KeyboardType.Number,
                                enabled = isEditable
                            )
                        }
                    }

                    // Section 5: Fabric Information
                    item {
                        AccordionSection(
                            iconPainter = painterResource(R.drawable.ic_file),
                            title = "Fabric Information",
                            expanded = expandedSection == "fabric",
                            onHeaderClick = {
                                expandedSection = if (expandedSection == "fabric") "" else "fabric"
                            },
                            showArrow = false,
                            trailing = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Fabric Provided?", fontSize = tokens.caption, color = headerGrey)
                                    Spacer(Modifier.width(8.dp))
                                    MiniSwitch(
                                        checked = fabricProvided,
                                        onCheckedChange = { if (isEditable) fabricProvided = it },
                                        enabled = isEditable
                                    )
                                }
                            }
                        ) {
                            Text("Customer-provided or store-procured fabric details", fontSize = tokens.caption, color = headerGrey)
                            Spacer(Modifier.height(14.dp))

                            FormDropdown(
                                label = "Fabric Source",
                                value = fabricSource.ifEmpty { "Select an option" },
                                expanded = fabricSourceExpanded && isEditable,
                                onExpandChange = { if (isEditable) fabricSourceExpanded = it },
                                options = fabricSourceOptions,
                                onOptionSelected = { fabricSource = it },
                                enabled = isEditable
                            )

                            Spacer(Modifier.height(14.dp))
                            FormLabel("Fabric Notes & Material Details")
                            FormTextArea(
                                value = fabricNotes,
                                onValueChange = { fabricNotes = it },
                                enabled = isEditable
                            )
                        }
                    }

                    // Section 6: Appointment & Staff Assignment
                    item {
                        AccordionSection(
                            iconPainter = painterResource(R.drawable.ic_calendar),
                            title = "Appointment & Staff Assignment",
                            expanded = expandedSection == "appointment",
                            onHeaderClick = {
                                expandedSection = if (expandedSection == "appointment") "" else "appointment"
                            },
                            showArrow = false,
                            trailing = {
                                MiniSwitch(
                                    checked = appointmentRequired,
                                    onCheckedChange = { if (isEditable) appointmentRequired = it },
                                    enabled = isEditable
                                )
                            }
                        ) {
                            Text("Assigned Staff, measurement schedule, and follow-up priority", fontSize = tokens.caption, color = headerGrey)
                            Spacer(Modifier.height(14.dp))

                            FormDropdown(
                                label = "Assigned Staff",
                                value = assignedStaffLabel.ifEmpty { "Select an option" },
                                expanded = assignedStaffExpanded && isEditable,
                                onExpandChange = { if (isEditable) assignedStaffExpanded = it },
                                options = staffDisplayList,
                                onOptionSelected = { label -> assignedStaff = staffIdMap[label] ?: "" },
                                enabled = isEditable
                            )

                            Spacer(Modifier.height(14.dp))
                            FormDropdown(
                                label = "Priority Level",
                                value = priorityLevel,
                                expanded = priorityLevelExpanded && isEditable,
                                onExpandChange = { if (isEditable) priorityLevelExpanded = it },
                                options = priorityOptions,
                                onOptionSelected = { priorityLevel = it },
                                enabled = isEditable
                            )

                            Spacer(Modifier.height(14.dp))
                            FormLabel("Next Follow-up Date")
                            DatePickerField(
                                value = nextFollowUpDate,
                                onDateSelected = { if (isEditable) nextFollowUpDate = it },
                                enabled = isEditable
                            )

                            if (appointmentRequired) {
                                Spacer(Modifier.height(14.dp))
                                FormLabel("Appointment Date")
                                DatePickerField(
                                    value = appointmentDate,
                                    onDateSelected = { if (isEditable) appointmentDate = it },
                                    enabled = isEditable
                                )

                                Spacer(Modifier.height(14.dp))
                                FormLabel("Appointment Time")
                                TimePickerField(
                                    value = appointmentTime,
                                    onTimeSelected = { if (isEditable) appointmentTime = it },
                                    enabled = isEditable
                                )

                                Spacer(Modifier.height(14.dp))
                                FormDropdown(
                                    label = "Appointment Status",
                                    value = appointmentStatus,
                                    expanded = appointmentStatusExpanded && isEditable,
                                    onExpandChange = { if (isEditable) appointmentStatusExpanded = it },
                                    options = appointmentStatusOptions,
                                    onOptionSelected = { appointmentStatus = it },
                                    enabled = isEditable
                                )
                            }
                        }
                    }

                    // Section 7: Notes & References
                    item {
                        AccordionSection(
                            iconPainter = painterResource(R.drawable.ic_file),
                            title = "Notes & References",
                            expanded = expandedSection == "notes",
                            onHeaderClick = {
                                expandedSection = if (expandedSection == "notes") "" else "notes"
                            }
                        ) {
                            Text("Internal tailor memos, customer notes, and reference files", fontSize = tokens.caption, color = headerGrey)
                            Spacer(Modifier.height(14.dp))

                            FormLabel("Internal Staff Notes (Private)")
                            FormTextArea(
                                value = internalNotes,
                                onValueChange = { internalNotes = it },
                                enabled = isEditable
                            )

                            Spacer(Modifier.height(14.dp))
                            FormLabel("Customer Facing Notes")
                            FormTextArea(
                                value = customerNotes,
                                onValueChange = { customerNotes = it },
                                enabled = isEditable
                            )
                        }
                    }

                    item {
                        Spacer(Modifier.height(tokens.extraPadding))
                    }
                }
            }

            // Fixed bottom action bar using reusable StepNavigationFab
            StepNavigationFab(
                showBack = true,
                backLabel = if (isView) "Close" else "Cancel",
                showBackArrow = false,
                onBack = onBack,
                trailingAction = if (isView) {
                    TrailingFabAction.Edit(
                        label = "Edit",
                        onClick = onEditRequested
                    )
                } else {
                    TrailingFabAction.Update(
                        label = if (isCreate) "Create Lead" else "Save Changes",
                        isLoading = leadState is SaleState.Loading || updateState is SaleState.Loading,
                        onClick = { submitLeadForm() }
                    )
                },
                showTrailingArrow = false,
                modifier = Modifier.align(Alignment.BottomCenter)
            )

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
        }
    }
}

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
    val staffList by salesViewModel.staffList.collectAsStateWithLifecycle()

    var actionMenuLeadId by remember { mutableStateOf<String?>(null) }
    var leadToDelete by remember { mutableStateOf<LeadTableItem?>(null) }
    var isLoadingEdit by remember { mutableStateOf(false) }
    var isLoadingView by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }

    val filterDrawerState = rememberFilterDrawerState()
    var searchQuery by remember { mutableStateOf("") }

    var filterSections by remember {
        mutableStateOf(getDefaultLeadFilterSections())
    }

    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(salesStatuses, leads) {
        val dynamicSources = leads.map { it.effectiveSource }.filter { it.isNotBlank() && it != "—" }.distinct()
        val apiStatusNames = salesStatuses.map { it.name }.filter { it.isNotBlank() }

        filterSections = filterSections.map { section ->
            when (section.title) {
                "Lead Status", "Status" -> {
                    val existingSelected = section.options.filter { it.isSelected }.map { it.label }.toSet()
                    val allLabels = (section.options.map { it.label } + apiStatusNames).distinct()
                    section.copy(
                        options = allLabels.map { label ->
                            FilterOption(
                                id = label.lowercase().replace(" ", "_"),
                                label = label,
                                isSelected = label in existingSelected
                            )
                        }
                    )
                }
                "Source" -> {
                    val existingSelected = section.options.filter { it.isSelected }.map { it.label }.toSet()
                    val allLabels = (section.options.map { it.label } + dynamicSources).distinct()
                    section.copy(
                        options = allLabels.map { label ->
                            FilterOption(id = label.lowercase().replace(" ", "_"), label = label, isSelected = label in existingSelected)
                        }
                    )
                }
                else -> section
            }
        }
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

    val activeFilterCount by remember(filterSections) {
        derivedStateOf {
            filterSections.sumOf { section ->
                section.options.count { it.isSelected }
            }
        }
    }

    // Display all leads in the table matching filters
    val filteredLeads by remember(leads, searchQuery, filterSections) {
        derivedStateOf {
            leads.filter { lead ->
                val personName = lead.name
                val enquiryType = lead.enquiryType ?: ""
                val matchesSearch = searchQuery.isBlank() ||
                        personName.contains(searchQuery, ignoreCase = true) ||
                        enquiryType.contains(searchQuery, ignoreCase = true)

                val statusName = lead.effectiveStatus
                val selectedStatusLabels = filterSections.find { it.title == "Lead Status" || it.title == "Status" }
                    ?.options?.filter { it.isSelected }?.map { it.label } ?: emptyList()
                val matchesStatus = selectedStatusLabels.isEmpty() ||
                        selectedStatusLabels.any { it.equals(statusName, ignoreCase = true) }

                val selectedSourceLabels = filterSections.find { it.title == "Source" }
                    ?.options?.filter { it.isSelected }?.map { it.label } ?: emptyList()
                val matchesSource = selectedSourceLabels.isEmpty() ||
                        selectedSourceLabels.any { it.equals(lead.effectiveSource, ignoreCase = true) }

                val selectedPriorityLabels = filterSections.find { it.title == "Priority" }
                    ?.options?.filter { it.isSelected }?.map { it.label } ?: emptyList()
                val matchesPriority = selectedPriorityLabels.isEmpty() ||
                        selectedPriorityLabels.any { it.equals(lead.priorityLevel, ignoreCase = true) }

                val selectedEnquiryLabels = filterSections.find { it.title == "Enquiry Type" }
                    ?.options?.filter { it.isSelected }?.map { it.label } ?: emptyList()
                val matchesEnquiry = selectedEnquiryLabels.isEmpty() ||
                        selectedEnquiryLabels.any { it.equals(enquiryType, ignoreCase = true) }

                matchesSearch && matchesStatus && matchesSource && matchesPriority && matchesEnquiry
            }
        }
    }

    val listState = rememberLazyListState()
    val isLoadingMore by salesViewModel.isLoadingMore.collectAsStateWithLifecycle()
    val canLoadMore by salesViewModel.canLoadMore.collectAsStateWithLifecycle()

    LaunchedEffect(listState) {
        snapshotFlow {
            val info = listState.layoutInfo
            val total = info.totalItemsCount
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            total > 0 && lastVisible >= total - 2
        }
            .distinctUntilChanged()
            .collect { nearEnd ->
                if (nearEnd && canLoadMore && !isLoadingMore && !isLoading) {
                    salesViewModel.loadMoreLeads()
                }
            }
    }

    fun resolveStatusBadge(lead: LeadTableItem): Pair<String, Pair<Color, Color>> {
        val statusName = lead.effectiveStatus
        return when {
            statusName.contains("Convert", ignoreCase = true) ->
                "Converted to Opportunity" to (greenBg to darkGreenBg)
            statusName.contains("New", ignoreCase = true) || statusName.equals("NEW", ignoreCase = true) ->
                "New Enquiry" to (primary_light to Primary)
            statusName.contains("Quot", ignoreCase = true) || statusName.equals("QUOTED", ignoreCase = true) ->
                "Quoted" to (yellowBg to yellowText)
            statusName.contains("Follow", ignoreCase = true) || statusName.contains("Pending", ignoreCase = true) ->
                "Follow-up" to (redBg to redText)
            statusName.contains("Lost", ignoreCase = true) ->
                "Lost" to (grey_border to headerGrey)
            statusName.contains("Qualified", ignoreCase = true) ->
                "Qualified" to (greenBg to darkGreenBg)
            else -> (if (statusName.isBlank()) "—" else statusName) to (grey_border to mutedText)
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
                }
                HorizontalDivider(color = title_border)

                Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        SearchFilterBar(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            placeholder = "Search Leads...",
                            filterCount = activeFilterCount,
                            accentColor = BluePrimary,
                            borderColor = BorderGray,
                            textSecondaryColor = TextSecondary,
                            onFilterClick = { filterDrawerState.open() }
                        )

                        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                            when {
                                isLoading && leads.isEmpty() -> {
                                    ListSkeleton()
                                }
                                tableError != null && leads.isEmpty() -> {
                                    AppErrorState(
                                        title = "Failed to load Leads",
                                        message = "Something went wrong. Please check your connection and try again.",
                                        onRetry = { salesViewModel.fetchTableLeads() }
                                    )
                                }
                                filteredLeads.isEmpty() -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(whiteBg, RoundedCornerShape(tokens.cardCornerRadius)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(tokens.screenPadding * 2.5f),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            val hasFilters = filterSections.any { section -> section.options.any { it.isSelected } }
                                            Text(
                                                text = if (searchQuery.isNotBlank() || hasFilters) "No matching leads found" else "No Leads Yet",
                                                fontSize = tokens.h2,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                            Spacer(Modifier.height(6.dp))
                                            Text(
                                                text = if (searchQuery.isNotBlank() || hasFilters) "Try adjusting your search or filter" else "Start by creating your first lead",
                                                fontSize = tokens.bodyMedium,
                                                color = mutedText
                                            )
                                            Spacer(Modifier.height(20.dp))
                                            Button(
                                                onClick = onCreateLead,
                                                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                                                contentPadding = PaddingValues(
                                                    horizontal = tokens.screenPadding,
                                                    vertical = tokens.screenPadding * 0.6f
                                                )
                                            ) {
                                                Text(
                                                    text = "Create Lead",
                                                    fontSize = tokens.bodyMedium,
                                                    color = whiteBg,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                    }
                                }
                                else -> {
                                    Column(modifier = Modifier.fillMaxSize()) {
                                        LazyColumn(
                                            state = listState,
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxWidth()
                                        ) {
                                            items(filteredLeads, key = { it.id }) { lead ->
                                                val (badgeText, badgeColors) = resolveStatusBadge(lead)
                                                val (bgColor, textColor) = badgeColors

                                                DataCard(
                                                    item = lead,
                                                    dateText = "Order ID: ${lead.id.takeLast(6).uppercase()}",
                                                    showDateIcon = false,
                                                    topBadgeText = badgeText,
                                                    topBadgeTextColor = textColor,
                                                    topBadgeBgColor = bgColor,
                                                    title = lead.name,
                                                    subtitle = "${formatLeadDate(lead.requiredDate?.ifBlank { "—" })} • ${lead.garmentName} • Qty ${lead.effectiveQuantity}",
                                                    footerFields = listOf(
                                                        DataCardField(
                                                            icon = Icons.Default.AttachMoney,
                                                            iconTint = Primary,
                                                            iconBackgroundColor = primary_light,
                                                            iconCircleSize = 24.dp,
                                                            text = "₹${formatIndianNumber(lead.minBudget)} - ₹${formatIndianNumber(lead.maxBudget)}",
                                                            textColor = TextLog
                                                        )
                                                    ),
                                                    actions = listOf(
                                                        MenuAction("View", Icons.Default.Visibility, enabled = !isLoadingView) { onViewClicked(lead) },
                                                        MenuAction("Edit", Icons.Default.Edit, enabled = !isLoadingEdit) { onEditClicked(lead) },
                                                        MenuAction("Delete", Icons.Default.Delete, tint = redText, textColor = redText, enabled = !isDeleting) { leadToDelete = lead }
                                                    )
                                                )
                                            }

                                            if (isLoadingMore) {
                                                item {
                                                    ThreeDotLoading()
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    FilterDrawer(
                        state = filterDrawerState,
                        title = "Filters",
                        sections = filterSections,
                        onApply = { updatedSections -> filterSections = updatedSections },
                        onClearAll = {
                            filterSections = filterSections.map { section ->
                                section.copy(options = section.options.map { option -> option.copy(isSelected = false) })
                            }
                        }
                    )
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
}

@Composable
fun MiniSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val tokens = LocalAppTokens.current
    Box(
        modifier = modifier
            .width(30.dp)
            .height(18.dp)
            .clip(RoundedCornerShape(50))
            .background(if (checked) Primary else grey_border)
            .border(
                width = 1.dp,
                color = if (checked) Primary else BorderGray,
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
        Text(label, fontSize = tokens.caption, color = mutedText, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(2.dp))
        if (value != null) {
            Text(value, fontSize = tokens.bodyMedium, color = TextPrimary, fontWeight = FontWeight.Normal)
        }
    }
    HorizontalDivider(color = grey_border, modifier = Modifier.padding(top = 4.dp))
}

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
            thumbColor = Primary,
            activeTrackColor = Primary,
            inactiveTrackColor = grey_border
        ),
        thumb = {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .background(whiteBg, CircleShape)
                    .border(3.dp, Primary, CircleShape)
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
                        .background(Primary)
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
        Text("₹${formatIndianNumber(min)}", fontSize = tokens.caption, color = headerGrey)
        Text("₹${formatIndianNumber(currentValue)}", fontSize = tokens.caption, fontWeight = FontWeight.Bold, color = Primary)
        Text("₹${formatIndianNumber(max)}", fontSize = tokens.caption, color = headerGrey)
    }
}