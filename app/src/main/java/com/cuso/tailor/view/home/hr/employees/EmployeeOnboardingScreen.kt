@file:Suppress(
    "UNUSED_VALUE",
    "AssignedValueIsNeverRead",
    "unused",
    "unusedVariable",
    "NAME_SHADOWING",
    "GrazieInspection",
    "SpellCheckingInspection",
    "VariableNeverRead"
)

package com.cuso.tailor.view.home.hr.employees

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.cuso.tailor.R
import com.cuso.tailor.adaptive_screen.AppDesignTokens
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.sales.Country
import com.cuso.tailor.model.hr.*
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.title_color
import com.cuso.tailor.ui.theme.whiteBg
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.view.home.toIsoDate
import com.cuso.tailor.viewmodel.*
import com.yalantis.ucrop.UCrop
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID

// ── Design tokens ──
private val AccentColor = Primary
private val BorderColor = Color(0xFFE3E4E8)
private val LabelColor = Color(0xFF6B7280)
private val TitleColor = title_color
private val WarnBg = Color(0xFFFFF7E6)
private val WarnBorder = Color(0xFFFCE3B0)
private val WarnText = Color(0xFF9A6A17)

// ── Screen mode ──
enum class ScreenMode { CREATE, VIEW, EDIT }

data class EducationEntry(
    val id: String = UUID.randomUUID().toString(),
    val degree: String = "",
    val specialization: String = "",
    val instituteName: String = "",
    val startDate: String = " ",
    val completionDate: String = " ",
    val cgpa: String = ""
)

data class ExperienceEntry(
    val id: String = UUID.randomUUID().toString(),
    val companyName: String = "",
    val jobTitle: String = "",
    val employmentType: String = "Full Time",
    val location: String = "",
    val fromDate: String = " ",
    val toDate: String = " ",
    val jobDescription: String = "",
    val isCurrentRole: Boolean = true
)

data class NomineeEntry(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val relationship: String = "",
    val sharePercent: String = ""
)

@SuppressLint("ContextCastToActivity")
@Composable
fun EmployeeOnboardingScreen(
    mode: ScreenMode = ScreenMode.CREATE,
    memberIdToLoad: String? = null,
    onDismiss: () -> Unit = {},
    onCreateEmployee: () -> Unit = {},
    onUpdateEmployee: () -> Unit = {},
    hrViewModel: HrViewModel = hiltViewModel(),
    branchViewModel: BranchViewModel = hiltViewModel(),
    departmentViewModel: DepartmentViewModel = hiltViewModel(),
    designationViewModel: DesignationViewModel = hiltViewModel()
) {
    val tokens: AppDesignTokens = LocalAppTokens.current

    val sectionGap = tokens.screenPadding
    val fieldGap = tokens.screenPadding * 0.75f
    val smallGap = tokens.screenPadding * 0.5f
    val tinyGap = tokens.screenPadding * 0.3f
    val adaptiveFieldShape = RoundedCornerShape(tokens.cardCornerRadius * 0.65f)
    val avatarSize = tokens.cardHeight * 0.85f

    val authViewModel: Authenticate = hiltViewModel(
        LocalContext.current as ComponentActivity
    )

    val isReadOnly = mode == ScreenMode.VIEW
    val isEditable = !isReadOnly

    var topSuccess by remember { mutableStateOf<String?>(null) }
    var expandedSection by remember { mutableStateOf("Personal Information") }

    // ── 1. Personal Information State ──
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var personalMail by remember { mutableStateOf("") }
    var workPhone by remember { mutableStateOf("") }
    var workPhoneCountry by remember { mutableStateOf<Country?>(null) }
    var personalPhone by remember { mutableStateOf("") }
    var personalPhoneCountry by remember { mutableStateOf<Country?>(null) }
    var dob by remember { mutableStateOf(" ") }
    var gender by remember { mutableStateOf("Select gender") }
    var genderExpanded by remember { mutableStateOf(false) }

    // ── 2. Permanent & Temporary Address State ──
    var addressTab by remember { mutableStateOf("Permanent") }
    var countryName by remember { mutableStateOf("India") }
    var countryCode by remember { mutableStateOf("IN") }
    var subdivisionName by remember { mutableStateOf("Tamil Nadu") }
    var subdivisionCode by remember { mutableStateOf("TN") }
    var city by remember { mutableStateOf("") }
    var flatNo by remember { mutableStateOf("") }
    var areaZone by remember { mutableStateOf("") }
    var pincode by remember { mutableStateOf("") }
    var streetAddress by remember { mutableStateOf("") }

    // Temporary Address
    var isSameAsPermanent by remember { mutableStateOf(false) }
    var tempCountryName by remember { mutableStateOf("India") }
    var tempCountryCode by remember { mutableStateOf("IN") }
    var tempSubdivisionName by remember { mutableStateOf("Tamil Nadu") }
    var tempSubdivisionCode by remember { mutableStateOf("TN") }
    var tempCity by remember { mutableStateOf("") }
    var tempFlatNo by remember { mutableStateOf("") }
    var tempAreaZone by remember { mutableStateOf("") }
    var tempPincode by remember { mutableStateOf("") }
    var tempStreetAddress by remember { mutableStateOf("") }

    // ── 3. Identity & Personal Details ──
    var aadhaarNo by remember { mutableStateOf("") }
    var panNo by remember { mutableStateOf("") }
    var passportNo by remember { mutableStateOf("") }
    var maritalStatus by remember { mutableStateOf("Select status") }
    var maritalExpanded by remember { mutableStateOf(false) }
    var bloodGroup by remember { mutableStateOf("Select group") }
    var bloodGroupExpanded by remember { mutableStateOf(false) }
    var emergencyContactName by remember { mutableStateOf("") }
    var emergencyContactMobile by remember { mutableStateOf("") }
    var emergencyContactPhoneCountry by remember { mutableStateOf<Country?>(null) }

    // ── 4. Government & Statutory IDs ──
    var uanNo by remember { mutableStateOf("") }
    var esicNumber by remember { mutableStateOf("") }
    var pfAccountNo by remember { mutableStateOf("") }
    var payFrequency by remember { mutableStateOf("Monthly") }
    var payFrequencyExpanded by remember { mutableStateOf(false) }

    // ── 5. Bank Account Details (Present in payload) ──
    var accountHolderName by remember { mutableStateOf("") }
    var accountNumber by remember { mutableStateOf("") }
    var bankName by remember { mutableStateOf("") }
    var ifscCode by remember { mutableStateOf("") }

    // ── 6. PF / Gratuity Nominees ──
    val nomineeList = remember { mutableStateListOf<NomineeEntry>() }

    // ── 7. Work Experience ──
    val experienceList = remember { mutableStateListOf<ExperienceEntry>() }

    // ── 8. Education Details ──
    val educationList = remember { mutableStateListOf<EducationEntry>() }

    // ── 9. Job Details ──
    var employeeCode by remember { mutableStateOf("") }
    var doj by remember { mutableStateOf(" ") }
    var department by remember { mutableStateOf("Select department") }
    var departmentExpanded by remember { mutableStateOf(false) }
    var designation by remember { mutableStateOf("Select designation") }
    var designationExpanded by remember { mutableStateOf(false) }
    var branch by remember { mutableStateOf("Select branch") }
    var branchExpanded by remember { mutableStateOf(false) }
    var shift by remember { mutableStateOf("Select shift") }
    var shiftExpanded by remember { mutableStateOf(false) }
    var reportingTo by remember { mutableStateOf("Search manager...") }
    var reportingToExpanded by remember { mutableStateOf(false) }
    var secondaryReportingTo by remember { mutableStateOf("Search manager...") }
    var secondaryReportingToExpanded by remember { mutableStateOf(false) }
    var workingDistrict by remember { mutableStateOf("") }
    var employmentType by remember { mutableStateOf("full-time") }

    // ── 10. Access & Permissions ──
    var role by remember { mutableStateOf("Select role") }
    var roleExpanded by remember { mutableStateOf(false) }

    val initials = remember(firstName, lastName) {
        "${firstName.firstOrNull()?.uppercaseChar() ?: ' '}${lastName.firstOrNull()?.uppercaseChar() ?: ' '}"
            .trim().ifBlank { "?" }
    }

    val branchUiState by branchViewModel.uiState.collectAsState()
    val branchList = (branchUiState as? BranchUiState.Success)?.branches ?: emptyList()
    var selectedBranchId by remember { mutableStateOf<String?>(null) }

    val departmentUiState by departmentViewModel.uiState.collectAsState()
    val departmentList = (departmentUiState as? DepartmentUiState.Success)?.departments ?: emptyList()
    var selectedDepartmentId by remember { mutableStateOf<String?>(null) }

    val designationUiState by designationViewModel.uiState.collectAsState()
    val designationList = (designationUiState as? DesignationUiState.Success)?.items ?: emptyList()
    var selectedDesignationId by remember { mutableStateOf<String?>(null) }

    val roles by hrViewModel.roles.collectAsState()
    val shifts by hrViewModel.shifts.collectAsState()
    val members by hrViewModel.members.collectAsState()

    var isUploading by remember { mutableStateOf(false) }
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    var existingProfilePictureUrl by remember { mutableStateOf<String?>(null) }
    var showProfileOptionsDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Error states
    var currentErrorField by remember { mutableStateOf<String?>(null) }
    var topError by remember { mutableStateOf<String?>(null) }

    var panError by remember { mutableStateOf<String?>(null) }
    var aadhaarError by remember { mutableStateOf<String?>(null) }
    var uanError by remember { mutableStateOf<String?>(null) }

    val uploadPictureState by hrViewModel.uploadPictureState.collectAsState()
    val deletePictureState by hrViewModel.deletePictureState.collectAsState()
    val memberDetail by hrViewModel.memberDetail.collectAsState()
    val createMemberState by hrViewModel.createMemberState.collectAsState()
    val memberDetailError by hrViewModel.memberDetailError.collectAsState()

    var selectedRoleId by remember { mutableStateOf<String?>(null) }
    var selectedShiftId by remember { mutableStateOf<String?>(null) }
    var selectedReportingToId by remember { mutableStateOf<String?>(null) }
    var selectedSecondaryReportingToId by remember { mutableStateOf<String?>(null) }

    val cropLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val resultUri = result.data?.let { UCrop.getOutput(it) }
            resultUri?.let { uri ->
                profileImageUri = uri
                if (mode == ScreenMode.EDIT && memberIdToLoad != null) {
                    val file = uriToFile(context, uri)
                    hrViewModel.uploadProfilePicture(memberIdToLoad, file)
                }
            }
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { sourceUri ->
            val destinationFileName = "cropped_profile_${System.currentTimeMillis()}.jpg"
            val destinationUri = Uri.fromFile(File(context.cacheDir, destinationFileName))

            val options = UCrop.Options().apply {
                setCircleDimmedLayer(true)
                setShowCropGrid(false)
                setCompressionFormat(Bitmap.CompressFormat.JPEG)
                setToolbarColor("#4F39F6".toColorInt())
                setToolbarWidgetColor(android.graphics.Color.WHITE)
            }

            val uCropIntent = UCrop.of(sourceUri, destinationUri)
                .withAspectRatio(1f, 1f)
                .withMaxResultSize(1000, 1000)
                .withOptions(options)
                .getIntent(context)

            cropLauncher.launch(uCropIntent)
        }
    }

    fun toApiDate(displayDate: String): String {
        if (displayDate.isBlank() || displayDate == " ") return ""
        return try {
            val formats = listOf("dd-MM-yyyy", "dd MMM yyy", "dd/MM/yyyy", "yyyy-MM-dd")
            for (format in formats) {
                try {
                    val input = SimpleDateFormat(format, Locale.getDefault())
                    val output = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    return output.format(input.parse(displayDate)!!)
                } catch (_: Exception) {}
            }
            displayDate
        } catch (e: Exception) {
            displayDate
        }
    }

    fun formatDateForDisplay(isoDate: String): String {
        if (isoDate.isBlank()) return " "
        return try {
            val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            val output = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            output.format(input.parse(isoDate)!!)
        } catch (e: Exception) {
            try {
                val input = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val output = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                output.format(input.parse(isoDate)!!)
            } catch (e2: Exception) {
                isoDate
            }
        }
    }

    fun findFirstMissingField(): String? {
        return when {
            firstName.isBlank() -> "First Name"
            lastName.isBlank() -> "Last Name"
            personalMail.isBlank() -> "Personal Email"
            workPhone.isBlank() -> "Work Phone"
            dob.isBlank() || dob == " " -> "Date of Birth"
            gender.isBlank() || gender == "Select gender" -> "Gender"
            bloodGroup.isBlank() || bloodGroup == "Select group" -> "Blood Group"
            emergencyContactName.isBlank() -> "Emergency Contact Name"
            emergencyContactMobile.isBlank() -> "Emergency Contact Phone"
            payFrequency.isBlank() || payFrequency == "Select frequency" -> "Pay Frequency"
            doj.isBlank() || doj == " " -> "Date of Joining"
            department == "Select department" -> "Department"
            role == "Select role" -> "Role"
            else -> null
        }
    }

    // Initial load
    LaunchedEffect(Unit) {
        hrViewModel.fetchMembers()
        hrViewModel.fetchRoles()
        hrViewModel.fetchShifts()
        branchViewModel.loadBranches()
        departmentViewModel.loadDepartments()
        designationViewModel.loadDesignations()
    }

    LaunchedEffect(memberIdToLoad) {
        hrViewModel.clearMemberDetail()
        if (mode != ScreenMode.CREATE && memberIdToLoad != null) {
            hrViewModel.fetchMemberDetail(memberIdToLoad)
        }
    }

    LaunchedEffect(memberDetail) {
        val m = memberDetail ?: return@LaunchedEffect
        firstName = m.firstName.orEmpty()
        lastName = m.lastName.orEmpty()
        personalMail = m.email ?: m.userId?.email.orEmpty()
        workPhone = m.workMobile ?: m.userId?.mobile.orEmpty()
        dob = m.dob?.let { formatDateForDisplay(it) } ?: " "
        gender = m.gender?.replaceFirstChar { it.uppercase() } ?: "Select gender"
        maritalStatus = m.martialStatus?.replaceFirstChar { it.uppercase() } ?: "Select status"

        m.permanentAddress?.let { addr ->
            countryName = addr.countryName.orEmpty().ifBlank { "India" }
            countryCode = addr.countryCode.orEmpty().ifBlank { "IN" }
            subdivisionName = addr.subdivisionName.orEmpty().ifBlank { "Tamil Nadu" }
            subdivisionCode = addr.subdivisionCode.orEmpty().ifBlank { "TN" }
            city = addr.city.orEmpty()
            flatNo = addr.flatNo.orEmpty()
            areaZone = addr.areaZone.orEmpty()
            pincode = addr.pincode.orEmpty()
            streetAddress = addr.street.orEmpty()
        }

        m.temporaryAddress?.let { addr ->
            tempCountryName = addr.countryName.orEmpty().ifBlank { "India" }
            tempCountryCode = addr.countryCode.orEmpty().ifBlank { "IN" }
            tempSubdivisionName = addr.subdivisionName.orEmpty().ifBlank { "Tamil Nadu" }
            tempSubdivisionCode = addr.subdivisionCode.orEmpty().ifBlank { "TN" }
            tempCity = addr.city.orEmpty()
            tempFlatNo = addr.flatNo.orEmpty()
            tempAreaZone = addr.areaZone.orEmpty()
            tempPincode = addr.pincode.orEmpty()
            tempStreetAddress = addr.street.orEmpty()
        }

        employeeCode = m.memberId.orEmpty()
        doj = m.doj?.let { formatDateForDisplay(it) } ?: " "
        workingDistrict = m.workingDistrict.orEmpty()
        employmentType = m.employmentType?.lowercase() ?: "full-time"

        selectedBranchId = m.branchId?._id
        branch = branchList.find { it.id == selectedBranchId }?.name ?: m.branchId?.name ?: "Select branch"

        selectedDepartmentId = m.departmentId?._id
        department = departmentList.find { it._id == selectedDepartmentId }?.name ?: m.departmentId?.name ?: "Select department"

        selectedDesignationId = m.designationId?._id
        designation = designationList.find { it.id == selectedDesignationId }?.name ?: m.designationId?.name ?: "Select designation"

        selectedRoleId = m.customRoleId?._id
        role = roles.find { it._id == selectedRoleId }?.name ?: m.customRoleId?.name ?: "Select role"

        selectedShiftId = m.shiftId
        shift = shifts.find { it._id == selectedShiftId }?.name ?: "Select shift"

        selectedReportingToId = m.reportingTo
        reportingTo = members.find { it._id == selectedReportingToId }?.displayName() ?: "Search manager..."

        selectedSecondaryReportingToId = m.secondaryReportingTo
        secondaryReportingTo = members.find { it._id == selectedSecondaryReportingToId }?.displayName() ?: "Search manager..."

        existingProfilePictureUrl = m.profilePicture
    }

    LaunchedEffect(createMemberState) {
        when (val state = createMemberState) {
            is HrViewModel.CreateMemberState.Success -> {
                hrViewModel.resetCreateMemberState()
                topSuccess = if (mode == ScreenMode.EDIT) "Employee updated successfully" else "Employee created successfully"
                if (mode == ScreenMode.EDIT) onUpdateEmployee() else onCreateEmployee()
            }
            is HrViewModel.CreateMemberState.Error -> {
                topError = state.message
                hrViewModel.resetCreateMemberState()
            }
            else -> Unit
        }
    }

    val totalAllocatedPercent = remember(nomineeList.toList()) {
        nomineeList.sumOf { it.sharePercent.toDoubleOrNull() ?: 0.0 }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
            TitleBar(
                title = when (mode) {
                    ScreenMode.VIEW -> "View Employee"
                    ScreenMode.EDIT -> "Edit Employee"
                    ScreenMode.CREATE -> "Employee Onboarding"
                },
                onClose = {
                    hrViewModel.clearMemberDetail()
                    hrViewModel.fetchMembers()
                    onDismiss()
                }
            )

            HorizontalDivider(color = BorderColor)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(
                        bottom = if (mode == ScreenMode.VIEW) sectionGap
                        else tokens.buttonHeight + sectionGap * 2
                    )
            ) {
                // ── 1. Personal Information ──
                AccordionSection(
                    iconPainter = painterResource(R.drawable.person),
                    title = "Personal Information",
                    expanded = expandedSection == "Personal Information",
                    onHeaderClick = { expandedSection = if (expandedSection == "Personal Information") "" else "Personal Information" }
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        EditableAvatar(
                            imageUri = profileImageUri,
                            imageUrl = existingProfilePictureUrl,
                            initials = initials,
                            isUploading = isUploading,
                            isReadOnly = isReadOnly,
                            avatarSize = avatarSize,
                            backgroundColor = Color.Gray,
                            onClick = {
                                if (isEditable) {
                                    if (profileImageUri != null || !existingProfilePictureUrl.isNullOrBlank()) {
                                        showProfileOptionsDialog = true
                                    } else {
                                        imagePickerLauncher.launch("image/*")
                                    }
                                }
                            }
                        )
                    }

                    Spacer(Modifier.height(sectionGap))
                    FormLabel("First Name *")
                    FormTextField(
                        value = firstName,
                        onValueChange = {
                            if (isEditable) {
                                firstName = it
                                if (currentErrorField == "First Name") { currentErrorField = null; topError = null }
                            }
                        },
                        placeholder = "e.g. John",
                        enabled = isEditable,
                        isError = currentErrorField == "First Name",
                        errorMessage = if (currentErrorField == "First Name") "First name is required" else null
                    )

                    Spacer(Modifier.height(fieldGap))
                    FormLabel("Last Name *")
                    FormTextField(
                        value = lastName,
                        onValueChange = {
                            if (isEditable) {
                                lastName = it
                                if (currentErrorField == "Last Name") { currentErrorField = null; topError = null }
                            }
                        },
                        placeholder = "e.g. Doe",
                        enabled = isEditable,
                        isError = currentErrorField == "Last Name",
                        errorMessage = if (currentErrorField == "Last Name") "Last name is required" else null
                    )

                    Spacer(Modifier.height(fieldGap))
                    FormLabel("Personal Email *")
                    FormTextField(
                        value = personalMail,
                        onValueChange = {
                            if (isEditable) {
                                personalMail = it
                                if (currentErrorField == "Personal Email") { currentErrorField = null; topError = null }
                            }
                        },
                        placeholder = "john.doe@company.com",
                        enabled = isEditable,
                        keyboardType = KeyboardType.Email,
                        isError = currentErrorField == "Personal Email",
                        errorMessage = if (currentErrorField == "Personal Email") "Personal Email is required" else null
                    )

                    Spacer(Modifier.height(fieldGap))
                    FormLabel("Work Phone *")
                    PhoneInputField(
                        phoneValue = workPhone,
                        onPhoneChange = {
                            if (isEditable) {
                                workPhone = it
                                if (currentErrorField == "Work Phone") { currentErrorField = null; topError = null }
                            }
                        },
                        onCountryChange = { if (isEditable) workPhoneCountry = it },
                        enabled = isEditable,
                        isError = currentErrorField == "Work Phone",
                        errorMessage = if (currentErrorField == "Work Phone") "Work phone is required" else null
                    )

                    Spacer(Modifier.height(fieldGap))
                    FormLabel("Personal Phone")
                    PhoneInputField(
                        phoneValue = personalPhone,
                        onPhoneChange = { if (isEditable) personalPhone = it },
                        onCountryChange = { if (isEditable) personalPhoneCountry = it },
                        enabled = isEditable
                    )

                    Spacer(Modifier.height(fieldGap))
                    FormLabel("Date of Birth *")
                    DatePickerField(
                        value = dob,
                        enabled = isEditable,
                        onDateSelected = {
                            if (isEditable) {
                                dob = it
                                if (currentErrorField == "Date of Birth") { currentErrorField = null; topError = null }
                            }
                        },
                        isError = currentErrorField == "Date of Birth"
                    )

                    Spacer(Modifier.height(fieldGap))
                    FormLabel("Gender *")
                    ErrorFieldWrapper(isError = currentErrorField == "Gender") {
                        FormDropdown(
                            value = gender,
                            expanded = genderExpanded && isEditable,
                            onExpandChange = { if (isEditable) genderExpanded = it },
                            options = listOf("Male", "Female", "Others"),
                            onOptionSelected = {
                                if (isEditable) {
                                    gender = it
                                    if (currentErrorField == "Gender") { currentErrorField = null; topError = null }
                                }
                            }
                        )
                    }
                }

                // ── 2. Permanent & Temporary Address ──
                AccordionSection(
                    iconPainter = painterResource(R.drawable.ic_location),
                    title = "Address Details",
                    expanded = expandedSection == "Address Details",
                    onHeaderClick = { expandedSection = if (expandedSection == "Address Details") "" else "Address Details" }
                ) {
                    SettingsTabs(
                        tabs = listOf(
                            TabItem(label = "Permanent"),
                            TabItem(label = "Temporary")
                        ),
                        selectedIndex = if (addressTab == "Permanent") 0 else 1,
                        onTabSelected = { index ->
                            addressTab = if (index == 0) "Permanent" else "Temporary"
                        }
                    )

                    Spacer(Modifier.height(fieldGap))
                    if (addressTab == "Permanent") {
                        CountryAndStatePicker(
                            selectedCountry = countryName,
                            selectedState = subdivisionName,
                            enabled = isEditable,
                            onCountryChange = {
                                if (isEditable) {
                                    countryName = it
                                    countryCode = if (it.equals("India", ignoreCase = true)) "IN" else it.take(2).uppercase()
                                }
                            },
                            onStateChange = {
                                if (isEditable) {
                                    subdivisionName = it
                                    subdivisionCode = if (it.contains("Tamil", ignoreCase = true)) "TN" else it.take(2).uppercase()
                                }
                            }
                        )

                        Spacer(Modifier.height(fieldGap))
                        FormLabel("City")
                        FormTextField(
                            value = city,
                            onValueChange = { if (isEditable) city = it },
                            placeholder = "Enter city",
                            enabled = isEditable
                        )

                        Spacer(Modifier.height(fieldGap))
                        FormLabel("Flat / Door No")
                        FormTextField(
                            value = flatNo,
                            onValueChange = { if (isEditable) flatNo = it },
                            placeholder = "Enter flat / door no.",
                            enabled = isEditable
                        )

                        Spacer(Modifier.height(fieldGap))
                        FormLabel("Area / Zone")
                        FormTextField(
                            value = areaZone,
                            onValueChange = { if (isEditable) areaZone = it },
                            placeholder = "Enter area / zone",
                            enabled = isEditable
                        )

                        Spacer(Modifier.height(fieldGap))
                        FormLabel("Pincode")
                        FormTextField(
                            value = pincode,
                            onValueChange = { if (isEditable) pincode = it },
                            placeholder = "Enter pincode",
                            keyboardType = KeyboardType.Number,
                            enabled = isEditable
                        )

                        Spacer(Modifier.height(fieldGap))
                        FormLabel("Street Address")
                        FormTextField(
                            value = streetAddress,
                            onValueChange = { if (isEditable) streetAddress = it },
                            placeholder = "Enter street address",
                            enabled = isEditable
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = isEditable) {
                                    val next = !isSameAsPermanent
                                    isSameAsPermanent = next
                                    if (next) {
                                        tempCountryName = countryName
                                        tempCountryCode = countryCode
                                        tempSubdivisionName = subdivisionName
                                        tempSubdivisionCode = subdivisionCode
                                        tempCity = city
                                        tempFlatNo = flatNo
                                        tempAreaZone = areaZone
                                        tempPincode = pincode
                                        tempStreetAddress = streetAddress
                                    }
                                }
                        ) {
                            AppCheckbox(
                                checked = isSameAsPermanent,
                                onCheckedChange = { checked ->
                                    if (isEditable) {
                                        isSameAsPermanent = checked
                                        if (checked) {
                                            tempCountryName = countryName
                                            tempCountryCode = countryCode
                                            tempSubdivisionName = subdivisionName
                                            tempSubdivisionCode = subdivisionCode
                                            tempCity = city
                                            tempFlatNo = flatNo
                                            tempAreaZone = areaZone
                                            tempPincode = pincode
                                            tempStreetAddress = streetAddress
                                        }
                                    }
                                },
                                enabled = isEditable
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Same as Permanent Address",
                                fontSize = tokens.bodySmall,
                                color = if (isEditable) TitleColor else TitleColor.copy(alpha = 0.6f)
                            )
                        }

                        Spacer(Modifier.height(fieldGap))
                        CountryAndStatePicker(
                            selectedCountry = tempCountryName,
                            selectedState = tempSubdivisionName,
                            enabled = isEditable,
                            onCountryChange = {
                                if (isEditable) {
                                    tempCountryName = it
                                    tempCountryCode = if (it.equals("India", ignoreCase = true)) "IN" else it.take(2).uppercase()
                                }
                            },
                            onStateChange = {
                                if (isEditable) {
                                    tempSubdivisionName = it
                                    tempSubdivisionCode = if (it.contains("Tamil", ignoreCase = true)) "TN" else it.take(2).uppercase()
                                }
                            }
                        )

                        Spacer(Modifier.height(fieldGap))
                        FormLabel("City")
                        FormTextField(
                            value = tempCity,
                            onValueChange = { if (isEditable) tempCity = it },
                            placeholder = "Enter city",
                            enabled = isEditable
                        )

                        Spacer(Modifier.height(fieldGap))
                        FormLabel("Flat / Door No")
                        FormTextField(
                            value = tempFlatNo,
                            onValueChange = { if (isEditable) tempFlatNo = it },
                            placeholder = "Enter flat / door no.",
                            enabled = isEditable
                        )

                        Spacer(Modifier.height(fieldGap))
                        FormLabel("Area / Zone")
                        FormTextField(
                            value = tempAreaZone,
                            onValueChange = { if (isEditable) tempAreaZone = it },
                            placeholder = "Enter area / zone",
                            enabled = isEditable
                        )

                        Spacer(Modifier.height(fieldGap))
                        FormLabel("Pincode")
                        FormTextField(
                            value = tempPincode,
                            onValueChange = { if (isEditable) tempPincode = it },
                            placeholder = "Enter pincode",
                            keyboardType = KeyboardType.Number,
                            enabled = isEditable
                        )

                        Spacer(Modifier.height(fieldGap))
                        FormLabel("Street Address")
                        FormTextField(
                            value = tempStreetAddress,
                            onValueChange = { if (isEditable) tempStreetAddress = it },
                            placeholder = "Enter street address",
                            enabled = isEditable
                        )
                    }
                }

                // ── 3. Identity & Personal Details ──
                AccordionSection(
                    iconPainter = painterResource(R.drawable.ic_credit),
                    title = "Identity & Personal Details",
                    expanded = expandedSection == "Identity & Personal Details",
                    onHeaderClick = { expandedSection = if (expandedSection == "Identity & Personal Details") "" else "Identity & Personal Details" }
                ) {
                    FormLabel("Aadhaar Number")
                    val aadhaarVisualTransformation = remember {
                        VisualTransformation { text ->
                            val trimmed = text.text.take(12)
                            val formatted = trimmed.chunked(4).joinToString(" ")
                            val offsetMapping = object : OffsetMapping {
                                override fun originalToTransformed(offset: Int): Int {
                                    val o = offset.coerceIn(0, trimmed.length)
                                    val spacesBefore = (o - 1).coerceAtLeast(0) / 4
                                    return (o + spacesBefore).coerceIn(0, formatted.length)
                                }

                                override fun transformedToOriginal(offset: Int): Int {
                                    val o = offset.coerceIn(0, formatted.length)
                                    val spacesBefore = formatted.substring(0, o).count { it == ' ' }
                                    return (o - spacesBefore).coerceIn(0, trimmed.length)
                                }
                            }
                            TransformedText(AnnotatedString(formatted), offsetMapping)
                        }
                    }
                    FormTextField(
                        value = aadhaarNo,
                        onValueChange = {
                            if (isEditable) {
                                aadhaarNo = it.filter { char -> char.isDigit() }.take(12)
                            }
                        },
                        placeholder = "XXXX-XXXX-XXXX",
                        enabled = isEditable,
                        keyboardType = KeyboardType.Number,
                        visualTransformation = aadhaarVisualTransformation
                    )

                    Spacer(Modifier.height(fieldGap))
                    FormLabel("PAN Number")
                    FormTextField(
                        value = panNo,
                        onValueChange = {
                            if (isEditable) {
                                panNo = it.uppercase().take(10)
                            }
                        },
                        placeholder = "ABCDE1234F",
                        enabled = isEditable,
                        keyboardType = KeyboardType.Text,
                        keyboardCapitalization = KeyboardCapitalization.Characters
                    )

                    Spacer(Modifier.height(fieldGap))
                    FormLabel("Passport Number")
                    FormTextField(
                        value = passportNo,
                        onValueChange = { if (isEditable) passportNo = it.uppercase() },
                        placeholder = "e.g. A1234567",
                        enabled = isEditable
                    )

                    Spacer(Modifier.height(fieldGap))
                    FormDropdown(
                        label = "Marital Status",
                        value = maritalStatus,
                        expanded = maritalExpanded && isEditable,
                        onExpandChange = { if (isEditable) maritalExpanded = it },
                        options = listOf("Single", "Married", "Divorced", "Widowed"),
                        onOptionSelected = { if (isEditable) maritalStatus = it }
                    )

                    Spacer(Modifier.height(fieldGap))
                    FormDropdown(
                        label = "Blood Group *",
                        value = bloodGroup,
                        expanded = bloodGroupExpanded && isEditable,
                        onExpandChange = { if (isEditable) bloodGroupExpanded = it },
                        options = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"),
                        onOptionSelected = {
                            if (isEditable) {
                                bloodGroup = it
                                if (currentErrorField == "Blood Group") { currentErrorField = null; topError = null }
                            }
                        },
                        isError = currentErrorField == "Blood Group",
                        errorMessage = if (currentErrorField == "Blood Group") "Blood Group is required" else null
                    )

                    Spacer(Modifier.height(fieldGap))
                    FormLabel("Emergency Contact Name *")
                    FormTextField(
                        value = emergencyContactName,
                        onValueChange = {
                            if (isEditable) {
                                emergencyContactName = it
                                if (currentErrorField == "Emergency Contact Name") { currentErrorField = null; topError = null }
                            }
                        },
                        placeholder = "Full name",
                        enabled = isEditable,
                        isError = currentErrorField == "Emergency Contact Name",
                        errorMessage = if (currentErrorField == "Emergency Contact Name") "Emergency Contact Name is required" else null
                    )

                    Spacer(Modifier.height(fieldGap))
                    FormLabel("Emergency Contact Phone *")
                    PhoneInputField(
                        phoneValue = emergencyContactMobile,
                        onPhoneChange = {
                            if (isEditable) {
                                emergencyContactMobile = it
                                if (currentErrorField == "Emergency Contact Phone") { currentErrorField = null; topError = null }
                            }
                        },
                        onCountryChange = { if (isEditable) emergencyContactPhoneCountry = it },
                        enabled = isEditable,
                        isError = currentErrorField == "Emergency Contact Phone",
                        errorMessage = if (currentErrorField == "Emergency Contact Phone") "Emergency Contact Phone is required" else null
                    )
                }

                // ── 4. Government & Statutory IDs ──
                AccordionSection(
                    iconPainter = painterResource(R.drawable.ic_building),
                    title = "Government & Statutory IDs",
                    expanded = expandedSection == "Government & Statutory IDs",
                    onHeaderClick = { expandedSection = if (expandedSection == "Government & Statutory IDs") "" else "Government & Statutory IDs" }
                ) {
                    FormLabel("UAN Number (EPF)")
                    FormTextField(
                        value = uanNo,
                        onValueChange = {
                            if (isEditable) {
                                uanNo = it.filter { char -> char.isDigit() }.take(12)
                            }
                        },
                        placeholder = "Universal Account Number",
                        enabled = isEditable,
                        keyboardType = KeyboardType.Number
                    )

                    Spacer(Modifier.height(fieldGap))
                    FormLabel("ESIC Number")
                    FormTextField(
                        value = esicNumber,
                        onValueChange = { if (isEditable) esicNumber = it },
                        placeholder = "Member Number",
                        enabled = isEditable,
                        keyboardType = KeyboardType.Number
                    )

                    Spacer(Modifier.height(fieldGap))
                    FormLabel("PF Account Number")
                    FormTextField(
                        value = pfAccountNo,
                        onValueChange = { if (isEditable) pfAccountNo = it },
                        placeholder = "Org specific no. (e.g. TN/CHN/0000000/000/0000000)",
                        enabled = isEditable
                    )

                    Spacer(Modifier.height(fieldGap))
                    FormDropdown(
                        label = "Pay Frequency *",
                        value = payFrequency,
                        expanded = payFrequencyExpanded && isEditable,
                        onExpandChange = { if (isEditable) payFrequencyExpanded = it },
                        options = listOf("Monthly", "Bi-weekly", "Weekly", "Daily"),
                        onOptionSelected = {
                            if (isEditable) {
                                payFrequency = it
                                if (currentErrorField == "Pay Frequency") { currentErrorField = null; topError = null }
                            }
                        }
                    )
                }

                // ── 5. Bank Account Details (Added to prevent hardcoding) ──
                AccordionSection(
                    iconPainter = painterResource(R.drawable.ic_credit),
                    title = "Bank Account Details",
                    expanded = expandedSection == "Bank Account Details",
                    onHeaderClick = { expandedSection = if (expandedSection == "Bank Account Details") "" else "Bank Account Details" }
                ) {
                    FormLabel("Account Holder Name")
                    FormTextField(
                        value = accountHolderName,
                        onValueChange = { if (isEditable) accountHolderName = it },
                        placeholder = "Enter account holder name",
                        enabled = isEditable
                    )

                    Spacer(Modifier.height(fieldGap))
                    FormLabel("Account Number")
                    FormTextField(
                        value = accountNumber,
                        onValueChange = { if (isEditable) accountNumber = it },
                        placeholder = "Enter account number",
                        enabled = isEditable,
                        keyboardType = KeyboardType.Number
                    )

                    Spacer(Modifier.height(fieldGap))
                    FormLabel("Bank Name")
                    FormTextField(
                        value = bankName,
                        onValueChange = { if (isEditable) bankName = it },
                        placeholder = "Enter bank name",
                        enabled = isEditable
                    )

                    Spacer(Modifier.height(fieldGap))
                    FormLabel("IFSC Code")
                    FormTextField(
                        value = ifscCode,
                        onValueChange = { if (isEditable) ifscCode = it.uppercase() },
                        placeholder = "Enter IFSC code",
                        enabled = isEditable,
                        keyboardCapitalization = KeyboardCapitalization.Characters
                    )
                }

                // ── 6. PF / Gratuity Nominees ──
                AccordionSection(
                    iconPainter = painterResource(R.drawable.person),
                    title = "PF / Gratuity Nominees",
                    expanded = expandedSection == "PF / Gratuity Nominees",
                    onHeaderClick = { expandedSection = if (expandedSection == "PF / Gratuity Nominees") "" else "PF / Gratuity Nominees" }
                ) {
                    if (nomineeList.isEmpty()) {
                        Text("No nominees added yet", fontSize = tokens.bodySmall, color = LabelColor, modifier = Modifier.padding(vertical = smallGap))
                    } else {
                        nomineeList.forEachIndexed { index, entry ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Nominee ${index + 1}", fontSize = tokens.bodyMedium, fontWeight = FontWeight.SemiBold, color = TitleColor)
                                if (isEditable) {
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription = "Remove",
                                        tint = Color(0xFFDC2626),
                                        modifier = Modifier.size(tokens.iconSize).clickable { nomineeList.remove(entry) }
                                    )
                                }
                            }
                            Spacer(Modifier.height(smallGap))
                            FormLabel("Name *")
                            FormTextField(
                                value = entry.name,
                                onValueChange = { if (isEditable) nomineeList[nomineeList.indexOf(entry)] = entry.copy(name = it) },
                                placeholder = "Full name",
                                enabled = isEditable
                            )

                            Spacer(Modifier.height(fieldGap))
                            FormLabel("Relationship *")
                            FormTextField(
                                value = entry.relationship,
                                onValueChange = { if (isEditable) nomineeList[nomineeList.indexOf(entry)] = entry.copy(relationship = it) },
                                placeholder = "e.g. Father, Mother, Spouse",
                                enabled = isEditable
                            )

                            Spacer(Modifier.height(fieldGap))
                            FormLabel("Share % *")
                            FormTextField(
                                value = entry.sharePercent,
                                onValueChange = { if (isEditable) nomineeList[nomineeList.indexOf(entry)] = entry.copy(sharePercent = it) },
                                placeholder = "e.g. 100",
                                keyboardType = KeyboardType.Number,
                                enabled = isEditable
                            )

                            if (index != nomineeList.lastIndex) {
                                Spacer(Modifier.height(fieldGap))
                                HorizontalDivider(color = BorderColor)
                                Spacer(Modifier.height(fieldGap))
                            }
                        }

                        Spacer(Modifier.height(smallGap))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(WarnBg, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                .border(1.dp, WarnBorder, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                .padding(horizontal = smallGap, vertical = smallGap * 0.8f)
                        ) {
                            Text(
                                "Allocated: ${totalAllocatedPercent.toInt()}% (Pending: ${(100 - totalAllocatedPercent).coerceAtLeast(0.0).toInt()}%)",
                                fontSize = tokens.caption,
                                color = WarnText,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    if (isEditable) {
                        Spacer(Modifier.height(fieldGap))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, AccentColor, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                .clickable { nomineeList.add(NomineeEntry()) }
                                .padding(vertical = smallGap),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("+ Add Nominee", color = AccentColor, fontWeight = FontWeight.Medium, fontSize = tokens.bodyMedium)
                        }
                    }
                }

                // ── 7. Work Experience ──
                AccordionSection(
                    icon = Icons.Outlined.Work,
                    title = "Work Experience",
                    expanded = expandedSection == "Work Experience",
                    onHeaderClick = { expandedSection = if (expandedSection == "Work Experience") "" else "Work Experience" }
                ) {
                    if (experienceList.isEmpty()) {
                        Text("No work experience added yet", fontSize = tokens.bodySmall, color = LabelColor, modifier = Modifier.padding(vertical = smallGap))
                    } else {
                        experienceList.forEachIndexed { index, entry ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Experience ${index + 1}", fontSize = tokens.bodyMedium, fontWeight = FontWeight.SemiBold, color = TitleColor)
                                if (isEditable) {
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription = "Remove",
                                        tint = Color(0xFFDC2626),
                                        modifier = Modifier.size(tokens.iconSize).clickable { experienceList.remove(entry) }
                                    )
                                }
                            }
                            Spacer(Modifier.height(smallGap))
                            FormLabel("Company Name *")
                            FormTextField(
                                value = entry.companyName,
                                onValueChange = { if (isEditable) experienceList[experienceList.indexOf(entry)] = entry.copy(companyName = it) },
                                placeholder = "e.g. Google",
                                enabled = isEditable
                            )

                            Spacer(Modifier.height(fieldGap))
                            FormLabel("Job Title *")
                            FormTextField(
                                value = entry.jobTitle,
                                onValueChange = { if (isEditable) experienceList[experienceList.indexOf(entry)] = entry.copy(jobTitle = it) },
                                placeholder = "e.g. Senior Sales Executive",
                                enabled = isEditable
                            )

                            Spacer(Modifier.height(fieldGap))
                            FormLabel("Employment Type *")
                            FormTextField(
                                value = entry.employmentType,
                                onValueChange = { if (isEditable) experienceList[experienceList.indexOf(entry)] = entry.copy(employmentType = it) },
                                placeholder = "e.g. Full Time",
                                enabled = isEditable
                            )

                            Spacer(Modifier.height(fieldGap))
                            FormLabel("Location *")
                            FormTextField(
                                value = entry.location,
                                onValueChange = { if (isEditable) experienceList[experienceList.indexOf(entry)] = entry.copy(location = it) },
                                placeholder = "City, Country",
                                enabled = isEditable
                            )

                            Spacer(Modifier.height(fieldGap))
                            FormLabel("Start Date *")
                            DatePickerField(
                                value = entry.fromDate,
                                onDateSelected = { if (isEditable) experienceList[experienceList.indexOf(entry)] = entry.copy(fromDate = it) },
                                enabled = isEditable
                            )

                            Spacer(Modifier.height(fieldGap))
                            FormLabel("End Date")
                            DatePickerField(
                                value = entry.toDate,
                                onDateSelected = { if (isEditable) experienceList[experienceList.indexOf(entry)] = entry.copy(toDate = it) },
                                enabled = !entry.isCurrentRole && isEditable
                            )

                            Spacer(Modifier.height(smallGap * 0.8f))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AppCheckbox(
                                    checked = entry.isCurrentRole,
                                    onCheckedChange = { checked ->
                                        if (isEditable) experienceList[experienceList.indexOf(entry)] = entry.copy(isCurrentRole = checked)
                                    },
                                    enabled = isEditable
                                )
                                Text("Currently Working", fontSize = tokens.bodySmall, color = if (isEditable) LabelColor else LabelColor.copy(alpha = 0.6f))
                            }

                            Spacer(Modifier.height(fieldGap))
                            FormLabel("Description")
                            OutlinedTextField(
                                value = entry.jobDescription,
                                onValueChange = { if (isEditable) experienceList[experienceList.indexOf(entry)] = entry.copy(jobDescription = it) },
                                placeholder = { Text("Key responsibilities and achievements...", fontSize = tokens.bodyMedium, color = Color(0xFF9CA3AF)) },
                                textStyle = TextStyle(fontSize = tokens.bodyMedium),
                                shape = adaptiveFieldShape,
                                enabled = isEditable,
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = BorderColor,
                                    focusedBorderColor = AccentColor,
                                    disabledBorderColor = BorderColor.copy(alpha = 0.5f),
                                    disabledTextColor = TitleColor.copy(alpha = 0.8f)
                                ),
                                modifier = Modifier.fillMaxWidth().height(tokens.fieldHeight * 2.2f)
                            )

                            if (index != experienceList.lastIndex) {
                                Spacer(Modifier.height(fieldGap))
                                HorizontalDivider(color = BorderColor)
                                Spacer(Modifier.height(fieldGap))
                            }
                        }
                    }

                    if (isEditable) {
                        Spacer(Modifier.height(fieldGap))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, AccentColor, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                .clickable { experienceList.add(ExperienceEntry()) }
                                .padding(vertical = smallGap),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("+ Add Experience", color = AccentColor, fontWeight = FontWeight.Medium, fontSize = tokens.bodyMedium)
                        }
                    }
                }

                // ── 8. Education Details ──
                AccordionSection(
                    iconPainter = painterResource(R.drawable.ic_education),
                    title = "Education Details",
                    expanded = expandedSection == "Education Details",
                    onHeaderClick = { expandedSection = if (expandedSection == "Education Details") "" else "Education Details" }
                ) {
                    if (educationList.isEmpty()) {
                        Text("No education details added yet", fontSize = tokens.bodySmall, color = LabelColor, modifier = Modifier.padding(vertical = smallGap))
                    } else {
                        educationList.forEachIndexed { index, entry ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Education ${index + 1}", fontSize = tokens.bodyMedium, fontWeight = FontWeight.SemiBold, color = TitleColor)
                                if (isEditable) {
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription = "Remove",
                                        tint = Color(0xFFDC2626),
                                        modifier = Modifier.size(tokens.iconSize).clickable { educationList.remove(entry) }
                                    )
                                }
                            }
                            Spacer(Modifier.height(smallGap))
                            FormLabel("Degree *")
                            FormTextField(
                                value = entry.degree,
                                onValueChange = { if (isEditable) educationList[educationList.indexOf(entry)] = entry.copy(degree = it) },
                                placeholder = "e.g. BCA / B.E.",
                                enabled = isEditable
                            )

                            Spacer(Modifier.height(fieldGap))
                            FormLabel("Specialization / Field of Study")
                            FormTextField(
                                value = entry.specialization,
                                onValueChange = { if (isEditable) educationList[educationList.indexOf(entry)] = entry.copy(specialization = it) },
                                placeholder = "e.g. Computer Science",
                                enabled = isEditable
                            )

                            Spacer(Modifier.height(fieldGap))
                            FormLabel("Institution Name *")
                            FormTextField(
                                value = entry.instituteName,
                                onValueChange = { if (isEditable) educationList[educationList.indexOf(entry)] = entry.copy(instituteName = it) },
                                placeholder = "e.g. Dummy University",
                                enabled = isEditable
                            )

                            Spacer(Modifier.height(fieldGap))
                            FormLabel("Start Date *")
                            DatePickerField(
                                value = entry.startDate,
                                onDateSelected = { if (isEditable) educationList[educationList.indexOf(entry)] = entry.copy(startDate = it) },
                                enabled = isEditable
                            )

                            Spacer(Modifier.height(fieldGap))
                            FormLabel("Completion Date")
                            DatePickerField(
                                value = entry.completionDate,
                                onDateSelected = { if (isEditable) educationList[educationList.indexOf(entry)] = entry.copy(completionDate = it) },
                                enabled = isEditable
                            )

                            Spacer(Modifier.height(fieldGap))
                            FormLabel("CGPA")
                            FormTextField(
                                value = entry.cgpa,
                                onValueChange = { if (isEditable) educationList[educationList.indexOf(entry)] = entry.copy(cgpa = it) },
                                placeholder = "e.g. 9.0",
                                enabled = isEditable
                            )

                            if (index != educationList.lastIndex) {
                                Spacer(Modifier.height(fieldGap))
                                HorizontalDivider(color = BorderColor)
                                Spacer(Modifier.height(fieldGap))
                            }
                        }
                    }

                    if (isEditable) {
                        Spacer(Modifier.height(fieldGap))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, AccentColor, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                .clickable { educationList.add(EducationEntry()) }
                                .padding(vertical = smallGap),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("+ Add Education", color = AccentColor, fontWeight = FontWeight.Medium, fontSize = tokens.bodyMedium)
                        }
                    }
                }

                // ── 9. Job Details ──
                AccordionSection(
                    iconPainter = painterResource(R.drawable.ic_building),
                    title = "Job Details",
                    expanded = expandedSection == "Job Details",
                    onHeaderClick = { expandedSection = if (expandedSection == "Job Details") "" else "Job Details" }
                ) {
                    FormLabel("Employee Code")
                    FormTextField(
                        value = employeeCode,
                        onValueChange = { if (isEditable) employeeCode = it },
                        placeholder = "Auto-generated if left blank",
                        enabled = isEditable
                    )

                    Spacer(Modifier.height(fieldGap))
                    FormLabel("Joining Date *")
                    DatePickerField(
                        value = doj,
                        enabled = isEditable,
                        onDateSelected = {
                            if (isEditable) {
                                doj = it
                                if (currentErrorField == "Date of Joining") { currentErrorField = null; topError = null }
                            }
                        },
                        isError = currentErrorField == "Date of Joining"
                    )

                    Spacer(Modifier.height(fieldGap))
                    FormDropdown(
                        label = "Department *",
                        value = department,
                        expanded = departmentExpanded && isEditable,
                        onExpandChange = { if (isEditable) departmentExpanded = it },
                        options = departmentList.map { it.name },
                        onOptionSelected = { selectedName ->
                            if (isEditable) {
                                department = selectedName
                                selectedDepartmentId = departmentList.find { it.name == selectedName }?._id
                                if (currentErrorField == "Department") { currentErrorField = null; topError = null }
                            }
                        },
                        isError = currentErrorField == "Department",
                        errorMessage = if (currentErrorField == "Department") "Department is required" else null
                    )

                    Spacer(Modifier.height(fieldGap))
                    FormDropdown(
                        label = "Designation",
                        value = designation,
                        expanded = designationExpanded && isEditable,
                        onExpandChange = { if (isEditable) designationExpanded = it },
                        options = designationList.map { it.name },
                        onOptionSelected = { selectedName ->
                            if (isEditable) {
                                designation = selectedName
                                selectedDesignationId = designationList.find { it.name == selectedName }?.id
                            }
                        }
                    )

                    Spacer(Modifier.height(fieldGap))
                    FormDropdown(
                        label = "Branch",
                        value = branch,
                        expanded = branchExpanded && isEditable,
                        onExpandChange = { if (isEditable) branchExpanded = it },
                        options = branchList.mapNotNull { it.name },
                        onOptionSelected = { selectedName ->
                            if (isEditable) {
                                branch = selectedName
                                selectedBranchId = branchList.find { it.name == selectedName }?.id
                            }
                        }
                    )

                    Spacer(Modifier.height(fieldGap))
                    FormDropdown(
                        label = "Shift",
                        value = shift,
                        expanded = shiftExpanded && isEditable,
                        onExpandChange = { if (isEditable) shiftExpanded = it },
                        options = shifts.map { it.name },
                        onOptionSelected = { selectedName ->
                            if (isEditable) {
                                shift = selectedName
                                selectedShiftId = shifts.find { it.name == selectedName }?._id
                            }
                        }
                    )

                    Spacer(Modifier.height(fieldGap))
                    FormDropdown(
                        label = "Reporting Manager",
                        value = reportingTo,
                        expanded = reportingToExpanded && isEditable,
                        onExpandChange = { if (isEditable) reportingToExpanded = it },
                        options = members.map { it.displayName() },
                        onOptionSelected = { selectedName ->
                            if (isEditable) {
                                reportingTo = selectedName
                                selectedReportingToId = members.find { it.displayName() == selectedName }?._id
                            }
                        }
                    )

                    Spacer(Modifier.height(fieldGap))
                    FormDropdown(
                        label = "Secondary Manager",
                        value = secondaryReportingTo,
                        expanded = secondaryReportingToExpanded && isEditable,
                        onExpandChange = { if (isEditable) secondaryReportingToExpanded = it },
                        options = members.map { it.displayName() },
                        onOptionSelected = { selectedName ->
                            if (isEditable) {
                                secondaryReportingTo = selectedName
                                selectedSecondaryReportingToId = members.find { it.displayName() == selectedName }?._id
                            }
                        }
                    )

                    Spacer(Modifier.height(fieldGap))
                    FormLabel("Work Location")
                    FormTextField(
                        value = workingDistrict,
                        onValueChange = { if (isEditable) workingDistrict = it },
                        placeholder = "Enter work location (e.g. Chennai)",
                        enabled = isEditable
                    )

                    Spacer(Modifier.height(fieldGap))
                    FormLabel("Employment Type")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf("full-time", "part-time", "contract", "volunteer").forEach { type ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable(enabled = isEditable) {
                                    employmentType = type
                                }
                            ) {
                                AppRadioButton(
                                    selected = employmentType == type,
                                    onClick = { if (isEditable) employmentType = type },
                                    enabled = isEditable,
                                )
                                Text(
                                    type.replaceFirstChar { it.uppercase() },
                                    fontSize = tokens.caption,
                                    color = TitleColor
                                )
                            }
                        }
                    }
                }

                // ── 10. Access & Permissions ──
                AccordionSection(
                    iconPainter = painterResource(R.drawable.person),
                    title = "Access & Permissions",
                    expanded = expandedSection == "Access & Permissions",
                    onHeaderClick = { expandedSection = if (expandedSection == "Access & Permissions") "" else "Access & Permissions" }
                ) {
                    Text(
                        "Assign a role that determines what this employee can see and do.",
                        fontSize = tokens.caption,
                        color = LabelColor
                    )
                    Spacer(Modifier.height(fieldGap))
                    FormDropdown(
                        label = "Assign Role *",
                        value = role,
                        expanded = roleExpanded && isEditable,
                        onExpandChange = { if (isEditable) roleExpanded = it },
                        options = roles.map { it.name },
                        onOptionSelected = { selectedName ->
                            if (isEditable) {
                                role = selectedName
                                selectedRoleId = roles.find { it.name == selectedName }?._id
                                if (currentErrorField == "Role") { currentErrorField = null; topError = null }
                            }
                        },
                        isError = currentErrorField == "Role",
                        errorMessage = if (currentErrorField == "Role") "Role is required" else null
                    )
                }
            }
        }

        // ── Floating Action Button (Submit) ──
        if (mode != ScreenMode.VIEW) {
            ExtendedFloatingActionButton(
                onClick = {
                    val missingField = findFirstMissingField()
                    if (missingField != null) {
                        currentErrorField = missingField
                        topError = "$missingField is required"
                        expandedSection = when (missingField) {
                            "First Name", "Last Name", "Personal Email", "Work Phone", "Date of Birth", "Gender" -> "Personal Information"
                            "Blood Group", "Emergency Contact Name", "Emergency Contact Phone" -> "Identity & Personal Details"
                            "Pay Frequency" -> "Government & Statutory IDs"
                            "Date of Joining", "Department" -> "Job Details"
                            "Role" -> "Access & Permissions"
                            else -> expandedSection
                        }
                    } else {
                        currentErrorField = null
                        topError = null

                        // Create Request Object matching Exact Payload
                        val createRequest = CreateMemberRequest(
                            firstName = firstName,
                            lastName = lastName,
                            personalMail = personalMail,
                            workMobile = workPhone,
                            aadhaarNo = aadhaarNo,
                            panNo = panNo,
                            passportNo = passportNo,
                            bloodGroup = bloodGroup,
                            dob = toApiDate(dob),
                            doj = toApiDate(doj),
                            gender = gender.lowercase(),
                            martialStatus = maritalStatus.lowercase(),
                            emergencyContactName = emergencyContactName,
                            emergencyContactMobile = emergencyContactMobile,
                            uanNo = uanNo,
                            esicNumber = esicNumber,
                            pfAccountNo = pfAccountNo,
                            payFrequency = payFrequency,
                            employmentType = employmentType,
                            workingDistrict = workingDistrict,
                            branchId = selectedBranchId,
                            branchName = branchList.find { it.id == selectedBranchId }?.name ?: branch.takeIf { it != "Select branch" },
                            departmentId = selectedDepartmentId,
                            designationId = selectedDesignationId,
                            customRoleId = selectedRoleId,
                            shiftId = selectedShiftId,
                            reportingTo = selectedReportingToId,
                            secondaryReportingTo = selectedSecondaryReportingToId,
                            accountHolderName = accountHolderName,
                            accountNumber = accountNumber,
                            bankName = bankName,
                            ifscCode = ifscCode,
                            permanentAddress = AddressRequest(
                                flatNo = flatNo,
                                street = streetAddress,
                                areaZone = areaZone,
                                city = city,
                                pincode = pincode,
                                countryCode = countryCode,
                                countryName = countryName,
                                subdivisionCode = subdivisionCode,
                                subdivisionName = subdivisionName
                            ),
                            hasTemporaryAddress = addressTab == "Temporary",
                            temporaryAddress = if (addressTab == "Temporary") {
                                AddressRequest(
                                    flatNo = tempFlatNo,
                                    street = tempStreetAddress,
                                    areaZone = tempAreaZone,
                                    city = tempCity,
                                    pincode = tempPincode,
                                    countryCode = tempCountryCode,
                                    countryName = tempCountryName,
                                    subdivisionCode = tempSubdivisionCode,
                                    subdivisionName = tempSubdivisionName
                                )
                            } else null,
                            education = educationList.map {
                                EducationRequestItem(
                                    instituteName = it.instituteName,
                                    degree = it.degree,
                                    specialization = it.specialization,
                                    startDate = toApiDate(it.startDate),
                                    completionDate = toApiDate(it.completionDate),
                                    cgpa = it.cgpa
                                )
                            },
                            workExperience = experienceList.map {
                                WorkExperienceRequestItem(
                                    companyName = it.companyName,
                                    jobTitle = it.jobTitle,
                                    employmentType = it.employmentType,
                                    location = it.location,
                                    fromDate = toApiDate(it.fromDate),
                                    toDate = toApiDate(it.toDate),
                                    jobDescription = it.jobDescription,
                                    isRelevant = it.isCurrentRole
                                )
                            },
                            pfGratuityNominees = nomineeList.map {
                                NomineeRequestItem(
                                    name = it.name,
                                    relationship = it.relationship,
                                    share = it.sharePercent
                                )
                            }
                        )

                        val updateRequest = UpdateMemberRequest(
                            firstName = firstName,
                            lastName = lastName,
                            personalMail = personalMail,
                            workMobile = workPhone,
                            aadhaarNo = aadhaarNo,
                            panNo = panNo,
                            passportNo = passportNo,
                            bloodGroup = bloodGroup,
                            dob = dob.toIsoDate(),
                            doj = doj.toIsoDate(),
                            gender = gender.lowercase(),
                            martialStatus = maritalStatus.lowercase(),
                            emergencyContactName = emergencyContactName,
                            emergencyContactMobile = emergencyContactMobile,
                            uanNo = uanNo,
                            esicNumber = esicNumber,
                            pfAccountNo = pfAccountNo,
                            payFrequency = payFrequency,
                            employmentType = employmentType,
                            workingDistrict = workingDistrict,
                            branchId = selectedBranchId,
                            branchName = branchList.find { it.id == selectedBranchId }?.name ?: branch.takeIf { it != "Select branch" },
                            departmentId = selectedDepartmentId,
                            designationId = selectedDesignationId,
                            customRoleId = selectedRoleId,
                            shiftId = selectedShiftId,
                            reportingTo = selectedReportingToId,
                            secondaryReportingTo = selectedSecondaryReportingToId,
                            accountHolderName = accountHolderName,
                            accountNumber = accountNumber,
                            bankName = bankName,
                            ifscCode = ifscCode,
                            permanentAddress = AddressRequest(
                                flatNo = flatNo,
                                street = streetAddress,
                                areaZone = areaZone,
                                city = city,
                                pincode = pincode,
                                countryCode = countryCode,
                                countryName = countryName,
                                subdivisionCode = subdivisionCode,
                                subdivisionName = subdivisionName
                            ),
                            hasTemporaryAddress = addressTab == "Temporary",
                            temporaryAddress = if (addressTab == "Temporary") {
                                AddressRequest(
                                    flatNo = tempFlatNo,
                                    street = tempStreetAddress,
                                    areaZone = tempAreaZone,
                                    city = tempCity,
                                    pincode = tempPincode,
                                    countryCode = tempCountryCode,
                                    countryName = tempCountryName,
                                    subdivisionCode = tempSubdivisionCode,
                                    subdivisionName = tempSubdivisionName
                                )
                            } else null,
                            education = educationList.map {
                                EducationRequestItem(
                                    instituteName = it.instituteName,
                                    degree = it.degree,
                                    specialization = it.specialization,
                                    startDate = toApiDate(it.startDate),
                                    completionDate = toApiDate(it.completionDate),
                                    cgpa = it.cgpa
                                )
                            },
                            workExperience = experienceList.map {
                                WorkExperienceRequestItem(
                                    companyName = it.companyName,
                                    jobTitle = it.jobTitle,
                                    employmentType = it.employmentType,
                                    location = it.location,
                                    fromDate = toApiDate(it.fromDate),
                                    toDate = toApiDate(it.toDate),
                                    jobDescription = it.jobDescription,
                                    isRelevant = it.isCurrentRole
                                )
                            },
                            pfGratuityNominees = nomineeList.map {
                                NomineeRequestItem(
                                    name = it.name,
                                    relationship = it.relationship,
                                    share = it.sharePercent
                                )
                            }
                        )

                        if (mode == ScreenMode.EDIT && memberIdToLoad != null) {
                            hrViewModel.updateMember(memberIdToLoad, updateRequest)
                        } else {
                            hrViewModel.createMember(createRequest)
                        }
                    }
                },
                containerColor = Primary,
                contentColor = whiteBg,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.65f),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(
                        start = sectionGap,
                        end = sectionGap,
                        bottom = sectionGap + 10.dp
                    )
                    .height(tokens.buttonHeight)
            ) {
                Text(
                    text = if (mode == ScreenMode.EDIT) "Save Changes" else "Create Employee",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = tokens.bodyLarge,
                    color = whiteBg
                )
            }
        }

        DynamicIslandError(
            modifier = Modifier.align(Alignment.TopCenter),
            message = topError,
            onDismiss = { topError = null }
        )

        DynamicIslandSuccess(
            modifier = Modifier.align(Alignment.TopCenter),
            message = topSuccess,
            onDismiss = { topSuccess = null }
        )
    }

    if (showProfileOptionsDialog && isEditable) {
        AlertDialog(
            onDismissRequest = { showProfileOptionsDialog = false },
            title = { Text("Profile Photo", fontWeight = FontWeight.SemiBold, fontSize = tokens.h2, color = TitleColor) },
            text = { Text("Choose an action for your profile photo", fontSize = tokens.bodySmall, color = LabelColor) },
            confirmButton = {
                TextButton(onClick = {
                    showProfileOptionsDialog = false
                    imagePickerLauncher.launch("image/*")
                }) {
                    Text("Upload New", color = AccentColor, fontWeight = FontWeight.Medium, fontSize = tokens.bodyMedium)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showProfileOptionsDialog = false
                    if (memberIdToLoad != null) {
                        hrViewModel.deleteProfilePicture(memberIdToLoad)
                    } else {
                        profileImageUri = null
                        existingProfilePictureUrl = null
                    }
                }) {
                    Text("Delete Profile", color = Color(0xFFDC2626), fontWeight = FontWeight.Medium, fontSize = tokens.bodyMedium)
                }
            },
            containerColor = whiteBg
        )
    }
}

fun uriToFile(context: Context, uri: Uri): File {
    val inputStream = context.contentResolver.openInputStream(uri)
    val file = File(context.cacheDir, "profile_${System.currentTimeMillis()}.jpg")
    inputStream?.use { input ->
        file.outputStream().use { output -> input.copyTo(output) }
    }
    return file
}