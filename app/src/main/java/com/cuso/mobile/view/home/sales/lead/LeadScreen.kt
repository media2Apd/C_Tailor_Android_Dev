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
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.cuso.mobile.ui.theme.blackTitle
import com.cuso.mobile.ui.theme.close_color
import com.cuso.mobile.ui.theme.title_color
import com.cuso.mobile.ui.theme.title_font
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.CirculerProgressIndicatorReuse
import com.cuso.mobile.view.composable.CirculerProgressIndicatorSmall
import com.cuso.mobile.view.composable.DatePickerField
import com.cuso.mobile.view.composable.DynamicIslandError
import com.cuso.mobile.view.composable.DynamicIslandSuccess
import com.cuso.mobile.view.composable.FieldValidator
import com.cuso.mobile.view.composable.PhoneInputField
import com.cuso.mobile.view.composable.ScreenBreadcrumb
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.view.composable.ValidationField
import com.cuso.mobile.view.home.FormDropdown
import com.cuso.mobile.view.home.FormLabel
import com.cuso.mobile.view.home.FormTextField
import com.cuso.mobile.view.home.LeadPrimary
import com.cuso.mobile.view.home.LeadPrimarySoft
import com.cuso.mobile.view.home.LeadmutedText
import com.cuso.mobile.view.home.TimePickerField
import com.cuso.mobile.view.home.buildFilterSections
import com.cuso.mobile.view.home.formatIndianNumber
import com.cuso.mobile.view.home.formatLeadDate
import com.cuso.mobile.view.home.reusablecomposables.DataCard
import com.cuso.mobile.view.home.reusablecomposables.DataCardField
import com.cuso.mobile.view.home.reusablecomposables.FabConfig
import com.cuso.mobile.view.home.reusablecomposables.FabScaffold
import com.cuso.mobile.view.home.reusablecomposables.FilterDrawer
import com.cuso.mobile.view.home.reusablecomposables.FilterSection
import com.cuso.mobile.view.home.reusablecomposables.ListSkeleton
import com.cuso.mobile.view.home.reusablecomposables.MenuAction
import com.cuso.mobile.view.home.reusablecomposables.SearchFilterBar
import com.cuso.mobile.view.home.reusablecomposables.StepNavigationFab
import com.cuso.mobile.view.home.reusablecomposables.TrailingFabAction
import com.cuso.mobile.view.home.reusablecomposables.rememberFilterDrawerState
import com.cuso.mobile.view.home.toIsoDate
import com.cuso.mobile.viewmodel.ConvertOrderState
import com.cuso.mobile.viewmodel.SaleState
import com.cuso.mobile.viewmodel.SalesViewModel
import kotlin.collections.get
import kotlin.text.ifEmpty

// ─────────────────────────────────────────────────────────────
// Reusable "Lead Form" UI kit
// ─────────────────────────────────────────────────────────────

@Composable
fun LeadFormTopBar(
    title: String,
    badgeText: String,
    badgeColor: Color = LeadPrimary,
    onClose: () -> Unit,
    isConverted: Boolean = true,
    onConvertToOrder: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(whiteBg)
            .padding(horizontal = 20.dp, vertical = 16.dp),
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
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(badgeText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = badgeColor)
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
                        .padding(horizontal = 12.dp, vertical = 6.dp),
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
                        fontSize = 12.sp,
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
    Dialog(onDismissRequest = { if (!isLoading) onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = whiteBg,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "Convert Lead to Order",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    leadName,
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280)
                )
                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss, enabled = !isLoading) {
                        Text("Cancel", color = Color(0xFF6B7280))
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
                            Text("Convert", color = whiteBg)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LeadInfoBanner(text: String) {
    var visible by remember { mutableStateOf(true) }
    AnimatedVisibility(visible = visible, enter = fadeIn(), exit = fadeOut()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(LeadPrimarySoft, RoundedCornerShape(10.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Info, contentDescription = null, tint = LeadPrimary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            Text(text, fontSize = 13.sp, color = Color(0xFF374151), modifier = Modifier.weight(1f))
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

@Suppress("UNUSED_PARAMETER")
@Composable
fun LeadAccordionSection(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    expanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    trailing: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val chevronRotation by animateFloatAsState(if (expanded) 180f else 0f, label = "lead_chevron")
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onExpandChange(!expanded) }
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF111827))
            }
            Spacer(Modifier.width(8.dp))
            if (trailing != null) {
                trailing()
            } else {
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = Primary,
                    modifier = Modifier
                        .size(25.dp)
                        .rotate(chevronRotation)
                )
            }
        }
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp)
            ) {
                content()
            }
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
// CreateLeadScreen
// ─────────────────────────────────────────────────────────────
@SuppressLint("AutoboxingStateCreation")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateLeadScreen(
    onBack: () -> Unit,
    onBreadCrumbClick: () -> Unit ={}
) {
    var leadSource       by remember { mutableStateOf("") }
    var enquiryDate      by remember { mutableStateOf("") }
    var leadOwner        by remember { mutableStateOf("") }
    var leadStatus       by remember { mutableStateOf("") }
    var customerType     by remember { mutableStateOf("Individual") }
    var fullName         by remember { mutableStateOf("") }
    var email            by remember { mutableStateOf("") }
    var gender           by remember { mutableStateOf("") }
    var dob              by remember { mutableStateOf("") }
    var address          by remember { mutableStateOf("") }
    var areaZone         by remember { mutableStateOf("") }
    var city             by remember { mutableStateOf("") }
    var preferredContact by remember { mutableStateOf("") }
    var enquiryType      by remember { mutableStateOf("") }
    var estimatedQuantity by remember { mutableStateOf("") }
    var selectedGarmentCategories by remember { mutableStateOf<List<String>>(emptyList()) }
    var budgetRange      by remember { mutableFloatStateOf(1000f) }
    var requiredDate     by remember { mutableStateOf("") }
    var occasion         by remember { mutableStateOf("") }
    var appointmentRequired by remember { mutableStateOf(false) }
    var appointmentDate  by remember { mutableStateOf("") }
    var appointmentTime  by remember { mutableStateOf("") }
    var assignedStaff    by remember { mutableStateOf("") }
    var followUpDate     by remember { mutableStateOf("") }
    var priority         by remember { mutableStateOf("") }
    var internalNotes    by remember { mutableStateOf("") }
    var customerNotes    by remember { mutableStateOf("") }
    var phone            by remember { mutableStateOf("") }
    var selectedIso      by remember { mutableStateOf("IN") }

    var leadSourceExpanded       by remember { mutableStateOf(false) }
    var leadOwnerExpanded        by remember { mutableStateOf(false) }
    var leadStatusExpanded       by remember { mutableStateOf(false) }
    var genderExpanded           by remember { mutableStateOf(false) }
    var preferredContactExpanded by remember { mutableStateOf(false) }
    var enquiryTypeExpanded      by remember { mutableStateOf(false) }
    var assignedStaffExpanded    by remember { mutableStateOf(false) }
    var priorityExpanded         by remember { mutableStateOf(false) }

    var expandedSection by remember { mutableStateOf("lead_info") }

    var errorField by remember { mutableStateOf<String?>(null) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val leadSectionFieldMap = remember {
        mapOf(
            "lead_info" to listOf("leadSource", "enquiryDate", "leadOwner", "leadStatus"),
            "customer" to listOf("fullName", "phone", "email"),
            "location" to emptyList(),
            "enquiry" to listOf("enquiryType", "estimatedQuantity", "garmentCategory", "requiredDate"),
            "appointment" to listOf("appointmentDate", "appointmentTime", "assignedStaff", "followUpDate", "priority")
        )
    }
    val leadSourceOptions       = listOf("Walk-in", "Instagram", "Facebook Ads", "Website")
    val genderOptions           = listOf("Male", "Female", "Other")
    val preferredContactOptions = listOf("WhatsApp", "Call")
    val enquiryTypeOptions      = listOf("New Order", "Bulk Order")
    val priorityOptions         = listOf("Low", "Medium", "High")

    val salesViewModel: SalesViewModel = hiltViewModel()
    val leadState          by salesViewModel.leadState.collectAsStateWithLifecycle()
    val staffList          by salesViewModel.staffList.collectAsStateWithLifecycle()
    val isLoadingStaff     by salesViewModel.isLoadingStaff.collectAsStateWithLifecycle()
    val salesStatuses      by salesViewModel.salesStatuses.collectAsStateWithLifecycle()
    val garmentCategories  by salesViewModel.garmentCategories.collectAsStateWithLifecycle()

    val staffDisplayList   = staffList.map { "${it.firstName} ${it.lastName} - ${it.memberId}" }
    val staffIdMap         = staffList.associate { "${it.firstName} ${it.lastName} - ${it.memberId}" to it.id }
    val selectedStaffLabel = staffIdMap.entries.firstOrNull { it.value == leadOwner }?.key ?: "Select an option"

    val statusOptions      = salesStatuses.map { it.name }
    val statusIdMap        = salesStatuses.associate { it.name to it.id }
    val garmentIdMap       = garmentCategories.associate { it.categoryId.categoryName to it.id }
    val garmentOptions     = garmentCategories.map { it.categoryId.categoryName }

    fun clearAllFields() {
        leadSource = ""; enquiryDate = ""; leadStatus = ""; customerType = "Individual"
        fullName = ""; email = ""; gender = ""; dob = ""; address = ""; areaZone = ""; city = ""
        preferredContact = ""; enquiryType = ""; estimatedQuantity = ""; selectedGarmentCategories = emptyList()
        budgetRange = 1000f; requiredDate = ""; occasion = ""; appointmentRequired = false
        appointmentDate = ""; appointmentTime = ""; assignedStaff = ""; followUpDate = ""
        priority = ""; internalNotes = ""; customerNotes = ""; phone = ""
        leadOwner = ""
    }

    fun submitLead() {
        val baseFields = buildList {
            add(ValidationField("leadSource", leadSource, "Lead Source is required"))
            add(ValidationField("enquiryDate", enquiryDate, "Enquiry Date is required"))
            add(ValidationField("leadOwner", leadOwner, "Lead Owner is required"))
            add(ValidationField("leadStatus", leadStatus, "Lead Status is required"))
            add(ValidationField("fullName", fullName, "Full Name is required"))
            add(ValidationField("phone", phone, "Mobile Number is required"))
            add(ValidationField("email", email, "Email is required"))
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

        val result = FieldValidator.validate(baseFields + appointmentFields)
        if (result != null) {
            errorField = result.fieldKey
            validationError = result.message
            expandedSection = FieldValidator.resolveSection(result.fieldKey, leadSectionFieldMap) ?: expandedSection
            return
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
        val request = CreateLeadFormRequest(
            customerType = customerType.lowercase(),
            enquiryType = enquiryType,
            estimatedQuantity = estimatedQuantity.toIntOrNull() ?: 0,
            budgetRange = BudgetRange(min = budgetRange.toInt(), max = 250000),
            garments = selectedGarmentCategories.mapNotNull { garmentIdMap[it] },
            enquiryDate = enquiryDate.toIsoDate(),
            requiredDate = requiredDate.toIsoDate(),
            source = leadSource,
            leadOwner = leadOwner,
            person = LeadPerson(
                name = fullName,
                phone = phone,
                email = email,
                gender = gender,
                dob = dob.toIsoDate()
            ),
            contact = LeadContact(
                address = address,
                area = areaZone,
                city = city,
                preferredContactMethod = preferredContact
            ),
            appointment = LeadAppointment(
                isRequired = appointmentRequired,
                date = if (appointmentRequired) appointmentDate.toIsoDate() else null,
                time = if (appointmentRequired) appointmentTime.takeIf { it.isNotBlank() } else null,
                assignedStaff = assignedStaff.takeIf { it.isNotBlank() },
                priority = if (appointmentRequired) priority.takeIf { it.isNotBlank() } else null,
                followUpDate = if (appointmentRequired) followUpDate.toIsoDate() else null
            ),
            status = statusIdMap[leadStatus] ?: "",
            statusName = leadStatus,
            notes = buildList {
                if (internalNotes.isNotBlank()) add(LeadNote(internalNotes, "internal"))
                if (customerNotes.isNotBlank()) add(LeadNote(customerNotes, "customer"))
            }
        )
        salesViewModel.createLead(request)
    }

    LaunchedEffect(Unit) {
        salesViewModel.fetchStaff()
        salesViewModel.fetchGarmentCategories()
    }

    LaunchedEffect(leadState) {
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

    Box(modifier = Modifier.fillMaxSize()) {

        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0)
        ) { padding ->
            FabScaffold(
                fab = FabConfig(
                    label = "Create Lead",
                    icon = Icons.AutoMirrored.Filled.ArrowForward,
                    onClick = { submitLead() },
                    bottomPadding = 50.dp
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    LeadFormTopBar(
                        title = "Create Lead",
                        badgeText = leadStatus.ifEmpty { "New Enquiry" },
                        onClose = onBack
                    )
                    HorizontalDivider(color = Color(0xFFF0F0F0))

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            LeadInfoBanner("Fill the details below to create a new lead.")
                        }

                        item {
                            LeadAccordionSection(
                                icon = Icons.Default.Description,
                                title = "Lead Information",
                                subtitle = "",
                                expanded = expandedSection == "lead_info",
                                onExpandChange = {
                                    expandedSection =
                                        if (expandedSection == "lead_info") "" else "lead_info"
                                }
                            ) {
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
                                    value = enquiryDate.ifEmpty { "Select Date" },
                                    onDateSelected = { enquiryDate = it },
                                    isError = errorField == "enquiryDate")
                                Spacer(Modifier.height(14.dp))

                                FormDropdown(
                                    "Lead Owner",
                                    selectedStaffLabel.ifEmpty {
                                        if (isLoadingStaff) "Loading staff..." else "Select an option"
                                    },
                                    leadOwnerExpanded,
                                    { leadOwnerExpanded = it },
                                    staffDisplayList,
                                    { label ->
                                        leadOwner = staffIdMap[label] ?: ""
                                    },
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

                        item {
                            LeadAccordionSection(
                                icon = Icons.Default.Person,
                                title = "Customer Identity",
                                subtitle = "Who is this lead for?",
                                expanded = expandedSection == "customer",
                                onExpandChange = {
                                    expandedSection =
                                        if (expandedSection == "customer") "" else "customer"
                                }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp))
                                        .padding(4.dp)
                                ) {
                                    listOf("Individual", "Corporate").forEach { type ->
                                        val isSelected = customerType == type
                                        Row(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isSelected) whiteBg else Color.Transparent)
                                                .clickable { customerType = type }
                                                .padding(vertical = 8.dp),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                if (type == "Individual") Icons.Default.Person else Icons.Default.Business,
                                                type,
                                                modifier = Modifier.size(16.dp),
                                                tint = if (isSelected) LeadPrimary else Color(0xFF6B7280)
                                            )
                                            Spacer(Modifier.width(6.dp))
                                            Text(
                                                type,
                                                fontSize = 14.sp,
                                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                                color = if (isSelected) blackTitle else Color(0xFF6B7280)
                                            )
                                        }
                                    }
                                }
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
                                PhoneInputField(
                                    phoneValue = phone,
                                    onPhoneChange = { phone = it },
                                    onCountryChange = { selectedIso = it.iso },
                                    isError = errorField == "phone",
                                    errorMessage = if (errorField == "phone") "Mobile Number is required" else null
                                )
                                Spacer(Modifier.height(14.dp))
                                FormLabel("Email")
                                FormTextField(value = email,
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
                                    DatePickerField(
                                        value = dob.ifEmpty { "Select Date" },
                                        onDateSelected = { dob = it })
                                }
                            }
                        }

                        item {
                            LeadAccordionSection(
                                icon = Icons.Default.LocationOn,
                                title = "Location & Communication",
                                subtitle = "Contact details and preferences",
                                expanded = expandedSection == "location",
                                onExpandChange = {
                                    expandedSection =
                                        if (expandedSection == "location") "" else "location"
                                }
                            ) {
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
                                    { preferredContact = it }
                                )
                            }
                        }

                        item {
                            LeadAccordionSection(
                                icon = Icons.AutoMirrored.Filled.Assignment,
                                title = "Enquiry Details",
                                subtitle = "What are they looking for?",
                                expanded = expandedSection == "enquiry",
                                onExpandChange = {
                                    expandedSection =
                                        if (expandedSection == "enquiry") "" else "enquiry"
                                }
                            ) {
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
                                FormLabel("Garment Category")
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(garmentOptions) { option ->
                                        val isSelected = selectedGarmentCategories.contains(option)
                                        Box(
                                            modifier = Modifier
                                                .border(
                                                    1.dp,
                                                    if (isSelected) Primary else Color(0xFFE5E7EB),
                                                    RoundedCornerShape(50.dp)
                                                )
                                                .background(
                                                    if (isSelected) LeadPrimarySoft else whiteBg,
                                                    RoundedCornerShape(50.dp)
                                                )
                                                .clickable {
                                                    selectedGarmentCategories =
                                                        if (isSelected) {
                                                            selectedGarmentCategories.filter { it != option }
                                                        } else {
                                                            selectedGarmentCategories + option
                                                        }
                                                }
                                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                if (isSelected) {
                                                    Icon(
                                                        Icons.Default.Check,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(14.dp),
                                                        tint = LeadPrimary
                                                    )
                                                    Spacer(Modifier.width(4.dp))
                                                }
                                                Text(
                                                    option,
                                                    fontSize = 13.sp,
                                                    color = if (isSelected) LeadPrimary else Color(0xFF374151),
                                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                                )
                                            }
                                        }
                                    }
                                }
                                Spacer(Modifier.height(14.dp))

                                FormLabel("Budget Range")
                                BudgetRangeSlider(
                                    value = budgetRange,
                                    onValueChange = { budgetRange = it })
                                Spacer(Modifier.height(4.dp))
                                BudgetRangeLabels(currentValue = budgetRange.toInt())

                                Spacer(Modifier.height(14.dp))
                                FormLabel("Required Date")
                                DatePickerField(
                                    value = requiredDate.ifEmpty { "Select Date" },
                                    onDateSelected = { requiredDate = it })
                                Spacer(Modifier.height(14.dp))
                                FormLabel("Occasion")
                                FormTextField(value = occasion, onValueChange = { occasion = it })
                            }
                        }

                        item {
                            LeadAccordionSection(
                                icon = Icons.Default.CalendarMonth,
                                title = "Appointment & Follow-Up",
                                subtitle = "Schedule interactions",
                                expanded = expandedSection == "appointment",
                                onExpandChange = {
                                    expandedSection =
                                        if (expandedSection == "appointment") "" else "appointment"
                                },
                                trailing = {
                                    MiniSwitch(
                                        checked = appointmentRequired,
                                        onCheckedChange = {
                                            appointmentRequired = it
                                            if (it) expandedSection = "appointment"
                                        }
                                    )
                                }
                            ) {
                                if (appointmentRequired) {
                                    FormLabel("Appointment Date")
                                    DatePickerField(
                                        value = appointmentDate.ifEmpty { "Select Date" },
                                        onDateSelected = { appointmentDate = it })
                                    Spacer(Modifier.height(14.dp))
                                    FormLabel("Appointment Time")
                                    TimePickerField(
                                        value = appointmentTime,
                                        onTimeSelected = { appointmentTime = it }
                                    )
                                    Spacer(Modifier.height(14.dp))

                                    FormDropdown(
                                        "Assigned Staff",
                                        selectedStaffLabel.ifEmpty { if (isLoadingStaff) "Loading staff..." else "Select an option" },
                                        assignedStaffExpanded && !isLoadingStaff,
                                        { assignedStaffExpanded = it },
                                        staffDisplayList,
                                        { label -> assignedStaff = staffIdMap[label] ?: "" },
                                        isError = errorField == "assignedStaff",
                                        errorMessage = if (errorField == "assignedStaff") "Assigned Staff is required" else null
                                    )
                                    Spacer(Modifier.height(14.dp))
                                    FormLabel("Follow-up Date", isRequired = true)
                                    DatePickerField(
                                        value = followUpDate.ifEmpty { "Select Date" },
                                        onDateSelected = { followUpDate = it })
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
                                } else {
                                    Text(
                                        "No appointment scheduled.",
                                        fontSize = 13.sp,
                                        color = LeadmutedText
                                    )
                                }
                            }
                        }

                        item {
                            LeadAccordionSection(
                                icon = Icons.Default.Description,
                                title = "Notes & References",
                                subtitle = "Additional information and attachments",
                                expanded = expandedSection == "notes",
                                onExpandChange = {
                                    expandedSection =
                                        if (expandedSection == "notes") "" else "notes"
                                }
                            ) {
                                FormLabel("Internal Notes")
                                OutlinedTextField(
                                    value = internalNotes,
                                    onValueChange = { internalNotes = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedBorderColor = Color(0xFFE5E7EB),
                                        focusedBorderColor = LeadPrimary,
                                        unfocusedContainerColor = whiteBg,
                                        focusedContainerColor = whiteBg
                                    )
                                )
                                Spacer(Modifier.height(14.dp))
                                FormLabel("Customer Notes")
                                OutlinedTextField(
                                    value = customerNotes,
                                    onValueChange = { customerNotes = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedBorderColor = Color(0xFFE5E7EB),
                                        focusedBorderColor = LeadPrimary,
                                        unfocusedContainerColor = whiteBg,
                                        focusedContainerColor = whiteBg
                                    )
                                )
                            }
                        }

                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() }
                                    ) { clearAllFields() }
                                    .padding(horizontal = 15.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = Primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "Clear All",
                                    fontSize = 13.sp,
                                    color = Primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        item { Spacer(Modifier.height(90.dp)) }
                    }
                }
            }
            DynamicIslandError(
                modifier = Modifier.align(Alignment.TopCenter),
                message = validationError,
                onDismiss = { validationError = null }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// LeadScreenContent
// ─────────────────────────────────────────────────────────────
@Composable
fun LeadScreenContent(
    onCreateLead: () -> Unit = {},
    onViewLead: () -> Unit = {},
    onEditLead: () -> Unit = {},
    onClose: () -> Unit = {},
    onBreadCrumbClick: () -> Unit = {}
) {
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
//                salesViewModel.fetchLeadDetails(l.id) { success ->
//                    if (!success) errorMessage = "Failed to refresh lead details"
//                }
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
        val matchesSearch = searchQuery.isBlank() ||
                lead.person.name.contains(searchQuery, ignoreCase = true) ||
                lead.enquiryType.contains(searchQuery, ignoreCase = true)

        val statusName = when (lead.status) {
            is String -> lead.status
            is Map<*, *> -> (lead.status["name"] as? String) ?: ""
            else -> ""
        }
        val selectedStatusLabels = filterSections.find { it.title == "Status" }
            ?.options?.filter { it.isSelected }?.map { it.label } ?: emptyList()
        val matchesStatus = selectedStatusLabels.isEmpty() ||
                selectedStatusLabels.any { it.equals(statusName, ignoreCase = true) }

        val selectedSourceLabels = filterSections.find { it.title == "Source" }
            ?.options?.filter { it.isSelected }?.map { it.label } ?: emptyList()
        val matchesSource = selectedSourceLabels.isEmpty() ||
                selectedSourceLabels.any { it.equals(lead.source, ignoreCase = true) }

        val garmentName = getGarmentName(lead)
        val selectedGarmentLabels = filterSections.find { it.title == "Garments" }
            ?.options?.filter { it.isSelected }?.map { it.label } ?: emptyList()
        val matchesGarments = selectedGarmentLabels.isEmpty() ||
                selectedGarmentLabels.any { it.equals(garmentName, ignoreCase = true) }

        val minAmountFilter = filterSections.find { it.title == "Amount Range" }?.minAmount?.toIntOrNull()
        val maxAmountFilter = filterSections.find { it.title == "Amount Range" }?.maxAmount?.toIntOrNull()
        val matchesAmount = (minAmountFilter == null || lead.budgetRange.max >= minAmountFilter) &&
                (maxAmountFilter == null || lead.budgetRange.min <= maxAmountFilter)

        val selectedPriority = filterSections.find { it.title == "Priority" }
            ?.options
            ?.find { it.isSelected }
            ?.id

        val matchesPriority = selectedPriority == null || run {
            val priority = lead.appointment?.priority?.lowercase() ?: ""
            when (selectedPriority) {
                "high" -> priority.contains("high")
                "medium" -> priority.contains("medium")
                "low" -> priority.contains("low")
                else -> true
            }
        }

        val selectedStaffIds = filterSections.find { it.title == "Sales Person" }
            ?.options?.filter { it.isSelected }?.map { it.id } ?: emptyList()
        val matchesSalesPerson = selectedStaffIds.isEmpty() ||
                selectedStaffIds.contains(lead.appointment?.assignedStaff)

        matchesSearch && matchesStatus && matchesSource && matchesGarments && matchesPriority && matchesAmount && matchesSalesPerson
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()

                ) {
                    TitleBar("Lead Management",
                        onClose= onClose)
                    Spacer(Modifier.height(8.dp))
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    ScreenBreadcrumb(listOf("Sales","Lead Management"), onClick = { onBreadCrumbClick() })

                    SearchFilterBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        placeholder = "Search Leads...",
                        accentColor = BluePrimary,
                        borderColor = BorderGray,
                        textSecondaryColor = TextSecondary,
                        onFilterClick = { filterDrawerState.open() }
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    when {
                        isLoading -> {
                            ListSkeleton()
                        }
                        tableError != null -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(whiteBg, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Warning, null, tint = Color.Red, modifier = Modifier.size(48.dp))
                                    Spacer(Modifier.height(8.dp))
                                    Text("Error loading leads", color = Color.Red, fontWeight = FontWeight.Bold)
                                    Text( "Something went wrong, Please try again after sometime", color = Color.Gray, fontSize = 14.sp)
                                    Spacer(Modifier.height(12.dp))
                                    Button(onClick = { salesViewModel.fetchTableLeads() }) {
                                        Text("Retry")
                                    }
                                }
                            }
                        }
                        filteredLeads.isEmpty() -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(whiteBg, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(Modifier.padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    val hasFilters = filterSections.any { section -> section.options.any { it.isSelected } }
                                    Text(
                                        if (searchQuery.isNotBlank() || hasFilters)
                                            "No matching leads found"
                                        else "No Leads Yet",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF111827)
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        if (searchQuery.isNotBlank() || hasFilters)
                                            "Try adjusting your search or filter"
                                        else "Start by creating your first lead",
                                        fontSize = 14.sp,
                                        color = Color(0xFF9CA3AF)
                                    )
                                    Spacer(Modifier.height(20.dp))
                                    Button(
                                        onClick = onCreateLead,
                                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                                    ) {
                                        Text("Create Lead", color = whiteBg, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    }
                                }
                            }
                        }
                        else -> {
                            Column(modifier = Modifier.fillMaxSize()) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .verticalScroll(rememberScrollState())
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(0.dp)
                                    ) {
                                        filteredLeads.forEach { lead ->
                                            val (badgeText, badgeColor) = resolveStatusBadge(lead)
                                            DataCard(
                                                item = lead,
                                                dateText = "Order ID: ${lead.convertedOrderId}",   // or lead.leadNumber if you have one
                                                showDateIcon = false,

                                                topBadgeText = badgeText,
                                                topBadgeTextColor = badgeColor,
                                                topBadgeBgColor = badgeColor.copy(alpha = 0.14f),

                                                title = lead.person.name.ifEmpty { "—" },
                                                subtitle = "${formatLeadDate(lead.requiredDate?.ifEmpty { "—" })} • ${getGarmentName(lead)} • Qty ${if (lead.estimatedQuantity == 0) "—" else lead.estimatedQuantity.toString()}",

                                                footerFields = listOf(
                                                    DataCardField(
                                                        icon = Icons.Default.AttachMoney,        // ✅ core icon, no extra dependency needed
                                                        iconTint = Color(0xFF6366F1),
                                                        iconBackgroundColor = Color(0xFFEEF2FF),
                                                        iconCircleSize = 24.dp,
                                                        text = "₹${formatIndianNumber(lead.budgetRange.min)} - ₹${formatIndianNumber(lead.budgetRange.max)}",
                                                        textColor = Color(0xFF374151)
                                                    )
                                                ),

                                                actions = listOf(
                                                    MenuAction("View", Icons.Default.Visibility, enabled = !isLoadingView) { onViewClicked(lead) },
                                                    MenuAction("Edit", Icons.Default.Edit, enabled = !isLoadingEdit) { onEditClicked(lead) },
                                                    MenuAction(
                                                        "Delete", Icons.Default.Delete,
                                                        tint = Color(0xFFF44336), textColor = Color(0xFFF44336),
                                                        enabled = !isDeleting
                                                    ) { leadToDelete = lead }   // ✅ existing state var, not orderToDelete
                                                )
                                            )
                                        }
                                    }
                                }

//                                PaginationFooter(
//                                    currentPage = currentPage,
//                                    pageSize = pageSize,
//                                    totalItems = totalLeads,
//                                    onPageChange = { salesViewModel.onPageChange(it) },
//                                    onItemsPerPageChange = { salesViewModel.onItemsPerPageChange(it) }
//                                )
                            }
                        }
                    }
                }
            }
        }

        // Top notification banners
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
        AlertDialog(
            onDismissRequest = { leadToDelete = null },
            containerColor = whiteBg,
            shape = RoundedCornerShape(12.dp),
            title = {
                Text("Delete Lead", fontWeight = FontWeight.Bold, color = Color(0xFF111827))
            },
            text = {
                Text("Are you sure you want to delete this lead? This action cannot be undone.", color = Color(0xFF6B7280))
            },
            confirmButton = {
                Button(
                    onClick = {
                        salesViewModel.deleteLead(leadToDelete!!.id)
                        leadToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Delete", color = whiteBg)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { leadToDelete = null },
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFD1D5DB))
                ) {
                    Text("Cancel", color = Color(0xFF374151))
                }
            }
        )
    }

    FilterDrawer(
        state = filterDrawerState,
        title = "Filters",
        sections = filterSections,
        onApply = { updatedSections ->
            applyFilters(updatedSections)
        },
        onClearAll = {
            filterSections = filterSections.map { section ->
                section.copy(
                    options = section.options.map { option ->
                        option.copy(isSelected = false)
                    }
                )
            }
        }
    )
}

// ─────────────────────────────────────────────────────────────
// ViewLeadScreen
// ─────────────────────────────────────────────────────────────
@Composable
fun ViewLeadScreen(
    onBack: () -> Unit,
    onEditLead: () -> Unit
) {
    val salesViewModel: SalesViewModel = hiltViewModel()

    val lead by salesViewModel.selectedLead.collectAsStateWithLifecycle()
    val isLoading by salesViewModel.isLoadingLeadDetails.collectAsStateWithLifecycle()
    val error by salesViewModel.leadDetailsError.collectAsStateWithLifecycle()
    val garmentCategories by salesViewModel.garmentCategories.collectAsStateWithLifecycle()
    val staffList by salesViewModel.staffList.collectAsStateWithLifecycle()

    val staffDisplayMap = staffList.associate { it.id to "${it.firstName} ${it.lastName} - ${it.memberId}" }

    LaunchedEffect(Unit) {
        if (staffList.isEmpty()) salesViewModel.fetchStaff()
    }

    var sectionLeadInfo    by remember { mutableStateOf(true) }
    var sectionCustomer    by remember { mutableStateOf(false) }
    var sectionLocation    by remember { mutableStateOf(false) }
    var sectionEnquiry     by remember { mutableStateOf(false) }
    var sectionAppointment by remember { mutableStateOf(false) }
    var sectionNotes       by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CirculerProgressIndicatorReuse()
                Spacer(Modifier.height(8.dp))
                Text("Loading lead details...", color = Color.Gray, fontSize = 14.sp)
            }
        }
        return
    }

    if (error != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red, modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(8.dp))
                Text("Error loading lead", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(error ?: "Unknown error", color = Color.Gray, fontSize = 14.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 32.dp))
                Spacer(Modifier.height(12.dp))
                Button(onClick = { onBack() }, colors = ButtonDefaults.buttonColors(containerColor = LeadPrimary), shape = RoundedCornerShape(8.dp)) {
                    Text("Go Back", color = whiteBg)
                }
            }
        }
        return
    }

    if (lead == null) {
        LaunchedEffect(Unit) {
            onBack()
        }
        return
    }

    val l = lead!!

    var appointmentRequired by remember(l.id) { mutableStateOf(l.appointmentRequired) }

    val leadOwnerDisplay = remember(l.leadOwner, staffDisplayMap) {
        val ownerId = l.leadOwner
        if (ownerId.isBlank()) {
            "—"
        } else {
            staffDisplayMap[ownerId] ?: ownerId
        }
    }

    val assignedStaffDisplay = remember(l.assignedStaff, staffDisplayMap) {
        val staffId = l.assignedStaff
        if (staffId.isNullOrBlank()) {
            "—"
        } else {
            staffDisplayMap[staffId] ?: staffId
        }
    }

    val garmentNames = if (l.garments.isNotBlank() && garmentCategories.isNotEmpty()) {
        val ids = l.garments.split(",").filter { it.isNotBlank() }
        ids.mapNotNull { id -> garmentCategories.find { it.id == id }?.categoryId?.categoryName }
    } else if (l.garments.isNotBlank() && garmentCategories.isEmpty()) {
        l.garments.split(",").filter { it.isNotBlank() }
    } else emptyList()

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0)
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                LeadFormTopBar(
                    title = "View Lead",
                    badgeText = l.status.ifEmpty { "—" },
                    onClose = onBack
                )
                HorizontalDivider(color = Color(0xFFF0F0F0))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 90.dp)
                ) {
                    item {
                        LeadAccordionSection(
                            icon = Icons.Default.Description,
                            title = "Lead Information",
                            subtitle = "Basic details about this lead",
                            expanded = sectionLeadInfo,
                            onExpandChange = { sectionLeadInfo = it }
                        ) {
                            ViewFieldValue("Lead Source", l.source.ifEmpty { "—" })
                            ViewFieldValue("Enquiry Date", formatLeadDate(l.enquiryDate))
                            ViewFieldValue("Lead Owner", leadOwnerDisplay)
                            ViewFieldValue("Lead Status", l.status.ifEmpty { "—" })
                        }
                    }

                    item {
                        LeadAccordionSection(
                            icon = Icons.Default.Person,
                            title = "Customer Identity",
                            subtitle = "Who is this lead for?",
                            expanded = sectionCustomer,
                            onExpandChange = { sectionCustomer = it }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp))
                                    .padding(4.dp)
                            ) {
                                listOf("Individual", "Corporate").forEach { type ->
                                    val isSelected = l.customerType.equals(type, ignoreCase = true)
                                    Row(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSelected) whiteBg else Color.Transparent)
                                            .padding(vertical = 8.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            if (type == "Individual") Icons.Default.Person else Icons.Default.Business,
                                            type,
                                            modifier = Modifier.size(16.dp),
                                            tint = if (isSelected) LeadPrimary else Color(0xFF6B7280)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(type, fontSize = 14.sp, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal, color = if (isSelected) blackTitle else Color(0xFF6B7280))
                                    }
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            ViewFieldValue("Full Name", l.fullName.ifEmpty { "—" })
                            ViewFieldValue("Phone", l.phone.ifEmpty { "—" })
                            ViewFieldValue("Email", l.email.ifEmpty { "—" })
                            if (l.customerType.equals("Individual", ignoreCase = true)) {
                                ViewFieldValue("Gender", l.gender.ifEmpty { "—" })
                                ViewFieldValue("Date of Birth", formatLeadDate(l.dob))
                            }
                        }
                    }

                    item {
                        LeadAccordionSection(
                            icon = Icons.Default.LocationOn,
                            title = "Location & Communication",
                            subtitle = "Contact details and preferences",
                            expanded = sectionLocation,
                            onExpandChange = { sectionLocation = it }
                        ) {
                            ViewFieldValue("Address", l.address.ifEmpty { "—" })
                            ViewFieldValue("Area / Zone", l.area.ifEmpty { "—" })
                            ViewFieldValue("City", l.city.ifEmpty { "—" })
                            ViewFieldValue("Preferred Contact Method", l.preferredContactMethod.ifEmpty { "—" })
                        }
                    }

                    item {
                        LeadAccordionSection(
                            icon = Icons.AutoMirrored.Filled.Assignment,
                            title = "Enquiry Details",
                            subtitle = "What are they looking for?",
                            expanded = sectionEnquiry,
                            onExpandChange = { sectionEnquiry = it }
                        ) {
                            ViewFieldValue("Enquiry Type", l.enquiryType.ifEmpty { "—" })
                            ViewFieldValue("Estimated Quantity", if (l.estimatedQuantity == 0) "—" else l.estimatedQuantity.toString())

                            Column(modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)) {
                                Text("Garment Category", fontSize = 12.sp, color = LeadmutedText, fontWeight = FontWeight.Medium)
                                Spacer(Modifier.height(4.dp))
                                if (garmentNames.isNotEmpty()) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        garmentNames.forEach { garment ->
                                            Box(
                                                modifier = Modifier
                                                    .border(
                                                        1.dp,
                                                        LeadPrimary,
                                                        RoundedCornerShape(50.dp)
                                                    )
                                                    .background(
                                                        LeadPrimarySoft,
                                                        RoundedCornerShape(50.dp)
                                                    )
                                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp), tint = LeadPrimary)
                                                    Spacer(Modifier.width(4.dp))
                                                    Text(garment, fontSize = 13.sp, color = LeadPrimary, fontWeight = FontWeight.SemiBold)
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    Text("—", fontSize = 14.sp, color = Color(0xFF111827))
                                }
                            }

                            Column(modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)) {

                                Text("Budget Range", fontSize = 12.sp, color = LeadmutedText, fontWeight = FontWeight.Medium)
                                Spacer(Modifier.height(4.dp))
                                Text("₹${formatIndianNumber(l.budgetMin)}  ₹${formatIndianNumber(l.budgetMax)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = LeadPrimary)
                                Spacer(Modifier.height(4.dp))
                                BudgetRangeSlider(value = l.budgetMin.toFloat(), onValueChange = {}, enabled = false)
                                Spacer(Modifier.height(4.dp))
                                BudgetRangeLabels(currentValue = l.budgetMin)
                            }

                            ViewFieldValue("Required Date", formatLeadDate(l.requiredDate))
                            ViewFieldValue("Occasion", l.occasion.ifEmpty { "—" })
                        }
                    }

                    item {
                        LeadAccordionSection(
                            icon = Icons.Default.CalendarMonth,
                            title = "Appointment & Follow-Up",
                            subtitle = "Schedule interactions",
                            expanded = sectionAppointment,
                            onExpandChange = { sectionAppointment = it },
                            trailing = {
                                MiniSwitch(
                                    checked = appointmentRequired,
                                    onCheckedChange = { },
                                    enabled = false
                                )
                            }
                        ) {
                            if (appointmentRequired) {
                                ViewFieldValue("Appointment Date", formatLeadDate(l.appointmentDate))
                                ViewFieldValue("Appointment Time", l.appointmentTime?.ifEmpty { "--:--" })
                                ViewFieldValue("Assigned Staff", assignedStaffDisplay)
                                ViewFieldValue("Follow-up Date", formatLeadDate(l.followUpDate))
                                ViewFieldValue("Priority", l.priority?.ifEmpty { "Select an option" })
                            } else {
                                Text("No appointment scheduled.", fontSize = 13.sp, color = LeadmutedText)
                            }
                        }
                    }

                    item {
                        LeadAccordionSection(
                            icon = Icons.Default.Description,
                            title = "Notes & References",
                            subtitle = "Additional information and attachments",
                            expanded = sectionNotes,
                            onExpandChange = { sectionNotes = it }
                        ) {
                            ViewFieldValue("Internal Notes", l.internalNotes.ifEmpty { "—" })
                            ViewFieldValue("Customer Notes", l.customerNotes.ifEmpty { "—" })
                        }
                    }

                    item { Spacer(Modifier.height(8.dp)) }
                }
            }
        }

        DynamicIslandError(
            modifier = Modifier.align(Alignment.TopCenter),
            message = errorMessage,
            onDismiss = { errorMessage = null }
        )

        StepNavigationFab(
            showBack = true,
            onBack = onBack,
            backLabel = "Back",
            trailingAction = TrailingFabAction.Edit(
                label = "Edit Lead",
                onClick = {
                    salesViewModel.fetchLeadDetails(l.id) { success ->
                        if (!success) {
                            errorMessage = "Failed to refresh lead data"
                        }
                        onEditLead()
                    }
                }
            )
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
    Box(
        modifier = modifier
            .width(30.dp)
            .height(18.dp)
            .clip(RoundedCornerShape(50))
            .background(
                if (checked) Primary else Color(0xFFE5E7EB)
            )
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
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 6.dp)) {
        Text(
            label,
            fontSize = 12.sp,
            color = LeadmutedText,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(2.dp))
        if (value != null) {
            Text(
                value,
                fontSize = 14.sp,
                color = Color(0xFF111827),
                fontWeight = FontWeight.Normal
            )
        }
    }
    HorizontalDivider(
        color = Color(0xFFF5F5F5),
        modifier = Modifier.padding(top = 4.dp)
    )
}

// ─────────────────────────────────────────────────────────────
// EditLeadScreen
// ─────────────────────────────────────────────────────────────

@Composable
fun EditLeadScreen(onBack: () -> Unit) {
    val salesViewModel: SalesViewModel = hiltViewModel()

    var showConvertDialog by remember { mutableStateOf(false) }
    var isConverted by remember { mutableStateOf(false) }

    val convertOrderState by salesViewModel.convertOrderState.collectAsStateWithLifecycle()

    val lead by salesViewModel.selectedLead.collectAsStateWithLifecycle()
    val updateState by salesViewModel.updateState.collectAsStateWithLifecycle()
    val staffList by salesViewModel.staffList.collectAsStateWithLifecycle()
    val isLoadingStaff by salesViewModel.isLoadingStaff.collectAsStateWithLifecycle()
    val salesStatuses by salesViewModel.salesStatuses.collectAsStateWithLifecycle()
    val garmentCategories by salesViewModel.garmentCategories.collectAsStateWithLifecycle()
    val isLoadingLead by salesViewModel.isLoadingLeadDetails.collectAsStateWithLifecycle()

    var validationError by remember { mutableStateOf<String?>(null) }
    var convertSuccessMessage by remember { mutableStateOf<String?>(null) }

    if (isLoadingLead) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CirculerProgressIndicatorReuse()
                Spacer(Modifier.height(8.dp))
                Text("Loading lead data...", color = Color.Gray, fontSize = 14.sp)
            }
        }
        return
    }

    if (lead == null) {
        LaunchedEffect(Unit) {
            onBack()
        }
        return
    }

    val l = lead!!

    LaunchedEffect(Unit) {
        if (staffList.isEmpty()) salesViewModel.fetchStaff()
        if (garmentCategories.isEmpty()) salesViewModel.fetchGarmentCategories()
        if (salesStatuses.isEmpty()) salesViewModel.fetchSalesData()
    }

    LaunchedEffect(convertOrderState) {
        when (val state = convertOrderState) {
            is ConvertOrderState.Success -> {
                showConvertDialog = false
                convertSuccessMessage = state.data.message.ifBlank { "Order created successfully" }
                salesViewModel.resetConvertOrderState()
            }
            is ConvertOrderState.Error -> {
                validationError = "Conversion failed: ${state.message}"
                salesViewModel.resetConvertOrderState()
                salesViewModel.fetchLeadDetails(l.id) { }
            }
            else -> Unit
        }
    }

    var leadSource by remember { mutableStateOf(l.source) }
    var enquiryDate by remember { mutableStateOf(formatLeadDate(l.enquiryDate)) }
    var leadOwner by remember { mutableStateOf(l.leadOwner) }
    var leadStatus by remember { mutableStateOf(l.status) }
    var customerType by remember { mutableStateOf(l.customerType.replaceFirstChar { it.uppercase() }) }
    var fullName by remember { mutableStateOf(l.fullName) }
    var email by remember { mutableStateOf(l.email) }
    var gender by remember { mutableStateOf(l.gender) }
    var dob by remember { mutableStateOf(formatLeadDate(l.dob)) }
    var address by remember { mutableStateOf(l.address) }
    var areaZone by remember { mutableStateOf(l.area) }
    var city by remember { mutableStateOf(l.city) }
    var preferredContact by remember { mutableStateOf(l.preferredContactMethod) }
    var enquiryType by remember { mutableStateOf(l.enquiryType) }
    var estimatedQuantity by remember { mutableStateOf(if (l.estimatedQuantity == 0) "" else l.estimatedQuantity.toString()) }
    var budgetRange by remember { mutableFloatStateOf(l.budgetMin.toFloat()) }
    var requiredDate by remember { mutableStateOf(formatLeadDate(l.requiredDate)) }
    var appointmentRequired by remember { mutableStateOf(l.appointmentRequired) }
    var appointmentDate by remember { mutableStateOf(formatLeadDate(l.appointmentDate)) }
    var appointmentTime by remember { mutableStateOf(l.appointmentTime) }
    var assignedStaff by remember { mutableStateOf(l.assignedStaff) }
    var followUpDate by remember { mutableStateOf(formatLeadDate(l.followUpDate)) }
    var priority by remember { mutableStateOf(l.priority) }
    var internalNotes by remember { mutableStateOf(l.internalNotes) }
    var customerNotes by remember { mutableStateOf(l.customerNotes) }
    var phone by remember { mutableStateOf(l.phone) }

    var selectedGarmentCategories by remember { mutableStateOf<List<String>>(emptyList()) }
    var showGarmentError by remember { mutableStateOf(false) }

    var leadSourceExpanded by remember { mutableStateOf(false) }
    var leadOwnerExpanded by remember { mutableStateOf(false) }
    var leadStatusExpanded by remember { mutableStateOf(false) }
    var genderExpanded by remember { mutableStateOf(false) }
    var preferredContactExpanded by remember { mutableStateOf(false) }
    var enquiryTypeExpanded by remember { mutableStateOf(false) }
    var assignedStaffExpanded by remember { mutableStateOf(false) }
    var priorityExpanded by remember { mutableStateOf(false) }

    var sectionLeadInfo by remember { mutableStateOf(true) }
    var sectionCustomer by remember { mutableStateOf(false) }
    var sectionLocation by remember { mutableStateOf(false) }
    var sectionEnquiry by remember { mutableStateOf(false) }
    var sectionAppointment by remember { mutableStateOf(false) }
    var sectionNotes by remember { mutableStateOf(false) }

    val leadSourceOptions = listOf("Walk-in", "Instagram", "Facebook Ads", "Website")
    val genderOptions = listOf("Male", "Female", "Other")
    val preferredContactOptions = listOf("WhatsApp", "Call")
    val enquiryTypeOptions = listOf("New Order", "Bulk Order")
    val priorityOptions = listOf("Low", "Medium", "High")

    val statusOptions = salesStatuses.map { it.name }
    val statusIdMap = salesStatuses.associate { it.name to it.id }
    val staffDisplayList = staffList.map { "${it.firstName} ${it.lastName} - ${it.memberId}" }
    val staffIdMap = staffList.associate { "${it.firstName} ${it.lastName} - ${it.memberId}" to it.id }
    val selectedStaffLabel = staffIdMap.entries.firstOrNull { it.value == assignedStaff }?.key ?: ""
    val garmentIdMap = garmentCategories.associate { it.categoryId.categoryName to it.id }
    val garmentOptions = garmentCategories.map { it.categoryId.categoryName }

    LaunchedEffect(l.garments, garmentCategories) {
        if (garmentCategories.isNotEmpty() && l.garments.isNotBlank()) {
            val ids = l.garments.split(",").filter { it.isNotBlank() }
            val names = ids.mapNotNull { id -> garmentCategories.find { it.id == id }?.categoryId?.categoryName }
            if (names.isNotEmpty()) selectedGarmentCategories = names
        }
    }
    LaunchedEffect(l.status) {
        isConverted = l.status.equals("Converted to order", ignoreCase = true)
        leadStatus = l.status
    }

    LaunchedEffect(updateState) {
        when (val state = updateState) {
            is SaleState.Success<*> -> {
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

    fun validateAndUpdate() {
        val error = validateLeadFields(
            leadSource = leadSource, enquiryDate = enquiryDate, leadOwner = leadOwner,
            leadStatus = leadStatus, customerType = customerType, fullName = fullName,
            phone = phone, email = email,
            gender = gender, dob = dob, preferredContact = preferredContact,
            enquiryType = enquiryType, estimatedQuantity = estimatedQuantity,
            garmentCategory = selectedGarmentCategories.joinToString(","),
            requiredDate = requiredDate,
            appointmentRequired = appointmentRequired, appointmentDate = appointmentDate,
            appointmentTime = appointmentTime, assignedStaff = assignedStaff,
            followUpDate = followUpDate, priority = priority
        )
        if (error != null) {
            validationError = error
            return
        }

        if (selectedGarmentCategories.isEmpty()) {
            showGarmentError = true
            return
        }
        val garmentIds = selectedGarmentCategories.mapNotNull { garmentIdMap[it] }
        if (garmentIds.isEmpty()) {
            showGarmentError = true
            return
        }
        showGarmentError = false

        val request = CreateLeadFormRequest(
            customerType = customerType.lowercase(),
            enquiryType = enquiryType,
            estimatedQuantity = estimatedQuantity.toIntOrNull() ?: 0,
            budgetRange = BudgetRange(min = budgetRange.toInt(), max = 250000),
            garments = garmentIds,
            enquiryDate = enquiryDate.toIsoDate(),
            requiredDate = requiredDate.toIsoDate(),
            source = leadSource,
            person = LeadPerson(name = fullName, phone = phone, email = email, gender = gender, dob = dob.toIsoDate()),
            contact = LeadContact(address = address, area = areaZone, city = city, preferredContactMethod = preferredContact),
            appointment = LeadAppointment(
                isRequired = appointmentRequired,
                date = if (appointmentRequired) appointmentDate.toIsoDate() else null,
                time = if (appointmentRequired) appointmentTime?.takeIf { it.isNotBlank() } else null,
                assignedStaff = assignedStaff?.takeIf { it.isNotBlank() },
                priority = if (appointmentRequired) priority?.takeIf { it.isNotBlank() } else null,
                followUpDate = if (appointmentRequired) followUpDate.toIsoDate() else null
            ),
            status = statusIdMap[leadStatus] ?: "",
            statusName = leadStatus,
            notes = buildList {
                if (internalNotes.isNotBlank()) add(LeadNote(internalNotes, "internal"))
                if (customerNotes.isNotBlank()) add(LeadNote(customerNotes, "customer"))
                if (internalNotes.isBlank() && customerNotes.isBlank()) add(LeadNote("-", "internal"))
            },
            occasion = l.occasion
        )

        salesViewModel.updateLeadById(l.id, request)
    }

    val currentUpdateState = updateState

    Box(modifier = Modifier.fillMaxSize()) {

        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0)
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
//                    .padding(padding)
            ) {
                LeadFormTopBar(
                    title = "Edit Lead",
                    badgeText = leadStatus.ifEmpty { "—" },
                    onClose = onBack,
                    isConverted = isConverted,
                    onConvertToOrder = { showConvertDialog = true }
                )
                HorizontalDivider(color = Color(0xFFF0F0F0))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(0.dp, bottom = 90.dp),
                ) {
                    item {
                        LeadInfoBanner("Edit the details below and save your changes.")
                    }

                    item {
                        LeadAccordionSection(
                            icon = Icons.Default.Description,
                            title = "Lead Information",
                            subtitle = "Basic details about this lead",
                            expanded = sectionLeadInfo,
                            onExpandChange = { sectionLeadInfo = it }
                        ) {
                            FormDropdown(
                                "Lead Source",
                                leadSource.ifEmpty { "Select an option" },
                                leadSourceExpanded,
                                { leadSourceExpanded = it },
                                leadSourceOptions,
                                { leadSource = it },
                                isRequired = true
                            )
                            Spacer(Modifier.height(14.dp))
                            FormLabel("Enquiry Date", isRequired = true)
                            DatePickerField(
                                value = enquiryDate.ifEmpty { "Select Date" },
                                onDateSelected = { enquiryDate = it })
                            Spacer(Modifier.height(14.dp))

                            val selectedLeadOwnerLabel =
                                staffIdMap.entries.firstOrNull { it.value == leadOwner }?.key ?: ""
                            FormDropdown(
                                "Lead Owner",
                                selectedLeadOwnerLabel.ifEmpty { if (isLoadingStaff) "Loading staff..." else "Select an option" },
                                leadOwnerExpanded,
                                { leadOwnerExpanded = it },
                                staffDisplayList,
                                { label -> leadOwner = staffIdMap[label] ?: "" },
                                isRequired = true
                            )

                            Spacer(Modifier.height(14.dp))
                            FormDropdown(
                                "Lead Status",
                                leadStatus.ifEmpty { "Select an option" },
                                leadStatusExpanded,
                                { leadStatusExpanded = it },
                                statusOptions,
                                { leadStatus = it },
                                isRequired = true
                            )
                        }
                    }

                    item {
                        LeadAccordionSection(
                            icon = Icons.Default.Person,
                            title = "Customer Identity",
                            subtitle = "Who is this lead for?",
                            expanded = sectionCustomer,
                            onExpandChange = { sectionCustomer = it }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp))
                                    .padding(4.dp)
                            ) {
                                listOf("Individual", "Corporate").forEach { type ->
                                    val isSelected = customerType == type
                                    Row(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSelected) whiteBg else Color.Transparent)
                                            .clickable { customerType = type }
                                            .padding(vertical = 8.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            if (type == "Individual") Icons.Default.Person else Icons.Default.Business,
                                            type,
                                            modifier = Modifier.size(16.dp),
                                            tint = if (isSelected) LeadPrimary else Color(0xFF6B7280)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            type,
                                            fontSize = 14.sp,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                            color = if (isSelected) blackTitle else Color(0xFF6B7280)
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(14.dp))
                            FormLabel("Full Name", isRequired = true)
                            FormTextField(value = fullName, onValueChange = { fullName = it })
                            Spacer(Modifier.height(14.dp))
                            FormLabel("Full Name", isRequired = true)

                            PhoneInputField(
                                phoneValue = phone,
                                onPhoneChange = { phone = it },
                                onCountryChange = {},
                            )
                            Spacer(Modifier.height(14.dp))
                            FormLabel("Email")
                            FormTextField(value = email, onValueChange = { email = it })
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
                                DatePickerField(
                                    value = dob.ifEmpty { "Select Date" },
                                    onDateSelected = { dob = it }
                                )
                            }
                        }
                    }

                    item {
                        LeadAccordionSection(
                            icon = Icons.Default.LocationOn,
                            title = "Location & Communication",
                            subtitle = "Contact details and preferences",
                            expanded = sectionLocation,
                            onExpandChange = { sectionLocation = it }
                        ) {
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
                                { preferredContact = it }
                            )
                        }
                    }

                    item {
                        LeadAccordionSection(
                            icon = Icons.AutoMirrored.Filled.Assignment,
                            title = "Enquiry Details",
                            subtitle = "What are they looking for?",
                            expanded = sectionEnquiry,
                            onExpandChange = { sectionEnquiry = it }
                        ) {
                            FormDropdown(
                                "Enquiry Type",
                                enquiryType.ifEmpty { "Select an option" },
                                enquiryTypeExpanded,
                                { enquiryTypeExpanded = it },
                                enquiryTypeOptions,
                                { enquiryType = it })
                            Spacer(Modifier.height(14.dp))
                            FormLabel("Estimated Quantity")
                            FormTextField(
                                value = estimatedQuantity,
                                onValueChange = { estimatedQuantity = it },
                                keyboardType = KeyboardType.Number
                            )
                            Spacer(Modifier.height(14.dp))

                            Column {
                                Row {
                                    Text(
                                        "Garment Categories",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (showGarmentError) Color.Red else Color.Gray
                                    )
                                    Text(
                                        " *",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.Red
                                    )
                                    Text(
                                        " (Select one or more)",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                                Spacer(Modifier.height(6.dp))

                                if (garmentCategories.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp))
                                            .padding(14.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            CirculerProgressIndicatorReuse()
                                            Text(
                                                "Loading categories...",
                                                fontSize = 14.sp,
                                                color = Color(0xFF6B7280)
                                            )
                                        }
                                    }
                                } else {
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        items(garmentOptions) { option ->
                                            val isSelected =
                                                selectedGarmentCategories.contains(option)
                                            Box(
                                                modifier = Modifier
                                                    .border(
                                                        1.dp,
                                                        if (isSelected) LeadPrimary else Color(0xFFE5E7EB),
                                                        RoundedCornerShape(50.dp)
                                                    )
                                                    .background(
                                                        if (isSelected) LeadPrimarySoft else whiteBg,
                                                        RoundedCornerShape(50.dp)
                                                    )
                                                    .clickable {
                                                        selectedGarmentCategories =
                                                            if (isSelected) {
                                                                selectedGarmentCategories.filter { it != option }
                                                            } else {
                                                                selectedGarmentCategories + option
                                                            }
                                                        showGarmentError = false
                                                    }
                                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    if (isSelected) {
                                                        Icon(
                                                            Icons.Default.Check,
                                                            contentDescription = null,
                                                            modifier = Modifier.size(14.dp),
                                                            tint = LeadPrimary
                                                        )
                                                        Spacer(Modifier.width(4.dp))
                                                    }
                                                    Text(
                                                        option,
                                                        fontSize = 13.sp,
                                                        color = if (isSelected) LeadPrimary else Color(0xFF374151),
                                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    if (selectedGarmentCategories.isNotEmpty()) {
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            "Selected: ${selectedGarmentCategories.joinToString(", ")}",
                                            fontSize = 12.sp,
                                            color = Color(0xFF6B7280)
                                        )
                                    }
                                }

                                if (showGarmentError) {
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "Please select at least one garment category",
                                        fontSize = 12.sp,
                                        color = Color.Red
                                    )
                                }
                            }

                            Spacer(Modifier.height(14.dp))
                            FormLabel("Budget Range")
                            BudgetRangeSlider(
                                value = budgetRange,
                                onValueChange = { budgetRange = it })
                            Spacer(Modifier.height(4.dp))
                            BudgetRangeLabels(currentValue = budgetRange.toInt())

                            Spacer(Modifier.height(14.dp))
                            FormLabel("Required Date")
                            DatePickerField(
                                value = requiredDate.ifEmpty { "Select Date" },
                                onDateSelected = { requiredDate = it })
                        }
                    }

                    item {
                        LeadAccordionSection(
                            icon = Icons.Default.CalendarMonth,
                            title = "Appointment & Follow-Up",
                            subtitle = "Schedule interactions",
                            expanded = sectionAppointment && appointmentRequired,
                            onExpandChange = { sectionAppointment = it },
                            trailing = {
                                MiniSwitch(
                                    checked = appointmentRequired,
                                    onCheckedChange = {
                                        appointmentRequired = it
                                        sectionAppointment = it
                                    }
                                )
                            }
                        ) {
                            if (appointmentRequired) {
                                FormLabel("Appointment Date")
                                DatePickerField(
                                    value = appointmentDate.ifEmpty { "Select Date" },
                                    onDateSelected = { appointmentDate = it })
                                Spacer(Modifier.height(14.dp))
                                FormLabel("Appointment Time")
                                TimePickerField(
                                    value = appointmentTime.orEmpty(),
                                    onTimeSelected = { appointmentTime = it }
                                )
                                FormDropdown(
                                    "Assigned Staff",
                                    selectedStaffLabel.ifEmpty { if (isLoadingStaff) "Loading..." else "Select an option" },
                                    assignedStaffExpanded && !isLoadingStaff,
                                    { assignedStaffExpanded = it },
                                    staffDisplayList,
                                    { label -> assignedStaff = staffIdMap[label] ?: "" })
                                Spacer(Modifier.height(14.dp))
                                FormLabel("Follow-up Date")
                                DatePickerField(
                                    value = followUpDate.ifEmpty { "Select Date" },
                                    onDateSelected = { followUpDate = it })
                                Spacer(Modifier.height(14.dp))
                                FormDropdown(
                                    "Priority",
                                    priority?.ifEmpty { "Select an option" } ?: "Select an option",
                                    priorityExpanded,
                                    { priorityExpanded = it },
                                    priorityOptions,
                                    { priority = it }
                                )
                            } else {
                                Text(
                                    "No appointment scheduled.",
                                    fontSize = 13.sp,
                                    color = LeadmutedText
                                )
                            }
                        }
                    }

                    item {
                        LeadAccordionSection(
                            icon = Icons.Default.Description,
                            title = "Notes & References",
                            subtitle = "Additional information",
                            expanded = sectionNotes,
                            onExpandChange = { sectionNotes = it }
                        ) {
                            FormLabel("Internal Notes")
                            OutlinedTextField(
                                value = internalNotes,
                                onValueChange = { internalNotes = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color(0xFFE5E7EB),
                                    focusedBorderColor = LeadPrimary,
                                    unfocusedContainerColor = whiteBg,
                                    focusedContainerColor = whiteBg
                                )
                            )
                            Spacer(Modifier.height(14.dp))
                            FormLabel("Customer Notes")
                            OutlinedTextField(
                                value = customerNotes,
                                onValueChange = { customerNotes = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color(0xFFE5E7EB),
                                    focusedBorderColor = LeadPrimary,
                                    unfocusedContainerColor = whiteBg,
                                    focusedContainerColor = whiteBg
                                )
                            )
                        }
                    }

                    item { Spacer(Modifier.height(8.dp)) }
                }
            }

            DynamicIslandSuccess(
                modifier = Modifier.align(Alignment.TopCenter),
                message = convertSuccessMessage,
                onDismiss = { convertSuccessMessage = null }
            )

            DynamicIslandError(
                modifier = Modifier.align(Alignment.TopCenter),
                message = validationError,
                onDismiss = { validationError = null }
            )
        }
        if (showConvertDialog) {
            ConvertToOrderDialog(
                leadName = fullName,
                onDismiss = { showConvertDialog = false },
                onConfirm = {
                    salesViewModel.convertLeadToOrder(l.id)
                }
            )
        }
        StepNavigationFab(
            showBack = true,
            onBack = onBack,
            backLabel = "Cancel",
            trailingAction = TrailingFabAction.Update(
                isLoading = currentUpdateState is SaleState.Loading,
                label = "Update Lead",
                enabled = currentUpdateState !is SaleState.Loading && selectedGarmentCategories.isNotEmpty(),
                onClick = { validateAndUpdate() }
            )
        )
    }
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
            inactiveTrackColor = Color(0xFFE5E7EB)
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
                    .background(Color(0xFFE5E7EB))
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
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("₹${formatIndianNumber(min)}", fontSize = 12.sp, color = Color(0xFF6B7280))
        Text("₹${formatIndianNumber(currentValue)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LeadPrimary)
        Text("₹${formatIndianNumber(max)}", fontSize = 12.sp, color = Color(0xFF6B7280))
    }
}