@file:Suppress(
    "UNUSED_VALUE",
    "AssignedValueIsNeverRead",
    "unused",
    "NAME_SHADOWING",
    "GrazieInspection",
    "SpellCheckingInspection",
    "VariableNeverRead"
)

package com.cuso.mobile.view.home.hr.employees

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
import androidx.compose.material.icons.filled.Close
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
import com.cuso.mobile.R
import com.cuso.mobile.adaptive_screen.AppDesignTokens
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.model.Country
import com.cuso.mobile.model.hr.AddressRequest
import com.cuso.mobile.model.hr.CreateMemberRequest
import com.cuso.mobile.model.hr.EducationRequestItem
import com.cuso.mobile.model.hr.UpdateMemberRequest
import com.cuso.mobile.model.hr.WorkExperienceRequestItem
import com.cuso.mobile.model.hr.displayName
import com.cuso.mobile.ui.theme.Primary
import com.cuso.mobile.ui.theme.title_color
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.*
import com.cuso.mobile.view.home.toIsoDate
import com.cuso.mobile.viewmodel.*
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
    val instituteName: String = "",
    val degree: String = "",
    val specialization: String = "",
    val completionDate: String = " "
)

data class ExperienceEntry(
    val id: String = UUID.randomUUID().toString(),
    val companyName: String = "",
    val jobTitle: String = "",
    val fromDate: String = " ",
    val toDate: String = " ",
    val jobDescription: String = "",
    val isCurrentRole: Boolean = false
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
    var expandedSection by remember { mutableStateOf("Basic Information") }

    // ── Basic Information state ──
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var workEmail by remember { mutableStateOf("") }
    var personalEmail by remember { mutableStateOf("") }

    var workPhone by remember { mutableStateOf("") }
    var workPhoneCountry by remember { mutableStateOf<Country?>(null) }

    var personalPhone by remember { mutableStateOf("") }
    var personalPhoneCountry by remember { mutableStateOf<Country?>(null) }

    var dob by remember { mutableStateOf(" ") }
    var gender by remember { mutableStateOf("Select Gender") }
    var genderExpanded by remember { mutableStateOf(false) }
    var maritalStatus by remember { mutableStateOf("Select Marital Status") }
    var maritalExpanded by remember { mutableStateOf(false) }

    // ── Address state (Permanent) ──
    var addressTab by remember { mutableStateOf("Permanent") }
    var country by remember { mutableStateOf("Select country") }
    var state by remember { mutableStateOf("Select state") }
    var city by remember { mutableStateOf("") }
    var postalCode by remember { mutableStateOf("") }
    var streetAddress by remember { mutableStateOf("") }

    // ── Address state (Temporary) ──
    var isSameAsPermanent by remember { mutableStateOf(false) }
    var tempCountry by remember { mutableStateOf("Select country") }
    var tempState by remember { mutableStateOf("Select state") }
    var tempCity by remember { mutableStateOf("") }
    var tempPostalCode by remember { mutableStateOf("") }
    var tempStreetAddress by remember { mutableStateOf("") }

    // ── Government IDs ──
    var pan by remember { mutableStateOf("") }
    var aadhaar by remember { mutableStateOf("") }
    var uan by remember { mutableStateOf("") }

    // ── Education & Experience Lists ──
    val educationList = remember { mutableStateListOf<EducationEntry>() }
    val experienceList = remember { mutableStateListOf<ExperienceEntry>() }

    // ── Work Info ──
    var memberId by remember { mutableStateOf("") }
    var employeeCode by remember { mutableStateOf("") }
    var doj by remember { mutableStateOf(" ") }
    var branch by remember { mutableStateOf("Select Branch") }
    var branchExpanded by remember { mutableStateOf(false) }
    var department by remember { mutableStateOf("Select Department") }
    var departmentExpanded by remember { mutableStateOf(false) }
    var designation by remember { mutableStateOf("Select Designation") }
    var designationExpanded by remember { mutableStateOf(false) }
    var role by remember { mutableStateOf("Select Role") }
    var roleExpanded by remember { mutableStateOf(false) }
    var shift by remember { mutableStateOf("Select Shift") }
    var shiftExpanded by remember { mutableStateOf(false) }
    var employmentType by remember { mutableStateOf("Select Employment Type") }
    var employmentTypeExpanded by remember { mutableStateOf(false) }
    var workLocation by remember { mutableStateOf("") }
    var reportingTo by remember { mutableStateOf("Select Reporting To") }
    var reportingToExpanded by remember { mutableStateOf(false) }
    var secondaryReportingTo by remember { mutableStateOf("Select Secondary Reporting To") }
    var secondaryReportingToExpanded by remember { mutableStateOf(false) }

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

    // Validation state
    var currentErrorField by remember { mutableStateOf<String?>(null) }
    var topError by remember { mutableStateOf<String?>(null) }

    val uploadPictureState by hrViewModel.uploadPictureState.collectAsState()
    val deletePictureState by hrViewModel.deletePictureState.collectAsState()
    val memberDetail by hrViewModel.memberDetail.collectAsState()

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

    LaunchedEffect(uploadPictureState) {
        when (val state = uploadPictureState) {
            is HrViewModel.UploadPictureState.Success -> {
                existingProfilePictureUrl = state.pictureUrl
                profileImageUri = null
                topSuccess = "Profile Uploaded Successfully"

                authViewModel.updateUserProfilePictureIfCurrentUser(
                    targetUserId = memberDetail?._id,
                    newUrl = state.pictureUrl
                )
                hrViewModel.resetUploadPictureState()
            }
            is HrViewModel.UploadPictureState.Error -> {
                topError = state.message
                hrViewModel.resetUploadPictureState()
            }
            else -> Unit
        }
    }

    LaunchedEffect(deletePictureState) {
        when (val state = deletePictureState) {
            is HrViewModel.DeletePictureState.Success -> {
                existingProfilePictureUrl = null
                profileImageUri = null
                topSuccess = "Profile Deleted Successfully"

                authViewModel.updateUserProfilePictureIfCurrentUser(
                    targetUserId = memberDetail?._id,
                    newUrl = null
                )
                hrViewModel.resetDeletePictureState()
            }
            is HrViewModel.DeletePictureState.Error -> {
                topError = state.message
                hrViewModel.resetDeletePictureState()
            }
            else -> Unit
        }
    }

    var selectedRoleId by remember { mutableStateOf<String?>(null) }
    var selectedShiftId by remember { mutableStateOf<String?>(null) }
    var selectedReportingToId by remember { mutableStateOf<String?>(null) }
    var selectedSecondaryReportingToId by remember { mutableStateOf<String?>(null) }

    val createMemberState by hrViewModel.createMemberState.collectAsState()

    var panError by remember { mutableStateOf<String?>(null) }
    var aadhaarError by remember { mutableStateOf<String?>(null) }
    var uanError by remember { mutableStateOf<String?>(null) }

    var isPanValid by remember { mutableStateOf(false) }
    var isAadhaarValid by remember { mutableStateOf(false) }
    var isUanValid by remember { mutableStateOf(false) }

    val memberDetailError by hrViewModel.memberDetailError.collectAsState()

    LaunchedEffect(memberDetailError) {
        if (memberDetailError != null) {
            topError = memberDetailError
        }
    }

    fun validatePanNumber(value: String): Boolean {
        if (value.isBlank()) {
            panError = null
            isPanValid = false
            return true
        }
        val result = GovernmentIdValidator.validatePan(value)
        panError = if (result.isValid) null else result.message
        isPanValid = result.isValid
        return result.isValid
    }

    fun validateAadhaarNumber(value: String): Boolean {
        if (value.isBlank()) {
            aadhaarError = null
            isAadhaarValid = false
            return true
        }
        val result = GovernmentIdValidator.validateAadhaar(value)
        aadhaarError = if (result.isValid) null else result.message
        isAadhaarValid = result.isValid
        return result.isValid
    }

    fun validateUanNumber(value: String): Boolean {
        if (value.isBlank()) {
            uanError = null
            isUanValid = false
            return true
        }
        val result = GovernmentIdValidator.validateUan(value)
        uanError = if (result.isValid) null else result.message
        isUanValid = result.isValid
        return result.isValid
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
            workPhone.isBlank() -> "Work Phone"
            personalPhone.isBlank() -> "Personal Phone"
            dob.isBlank() || dob == " " -> "Date of Birth"
            gender.isBlank() || gender == "Select Gender" -> "Gender"
            doj.isBlank() || doj == " " -> "Date of Joining"
            department == "Select Department" -> "Department"
            role == "Select Role" -> "Role"
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

    LaunchedEffect(
        memberDetail,
        branchList,
        departmentList,
        designationList,
        roles,
        shifts,
        members
    ) {
        val m = memberDetail ?: return@LaunchedEffect

        firstName = m.firstName.orEmpty()
        lastName = m.lastName.orEmpty()

        // Map from direct fields or fallback to nested userId
        workEmail = m.email ?: m.userId?.email.orEmpty()
        personalEmail = m.email ?: m.userId?.email.orEmpty()
        workPhone = m.workMobile ?: m.userId?.mobile.orEmpty()
        personalPhone = m.personalMobile ?: m.userId?.mobile.orEmpty()

        dob = m.dob?.let { formatDateForDisplay(it) } ?: " "
        gender = m.gender?.replaceFirstChar { it.uppercase() } ?: "Select Gender"
        maritalStatus = m.martialStatus?.replaceFirstChar { it.uppercase() } ?: "Select Marital Status"

        // Permanent Address
        m.permanentAddress?.let { addr ->
            country = addr.country.orEmpty().ifBlank { "Select country" }
            state = addr.state.orEmpty().ifBlank { "Select state" }
            city = addr.city.orEmpty()
            postalCode = addr.postalCode.orEmpty()
            streetAddress = addr.street.orEmpty()
        }

        // Temporary Address
        if (m.hasTemporaryAddress) {
            m.temporaryAddress?.let { addr ->
                tempCountry = addr.country.orEmpty().ifBlank { "Select country" }
                tempState = addr.state.orEmpty().ifBlank { "Select state" }
                tempCity = addr.city.orEmpty()
                tempPostalCode = addr.postalCode.orEmpty()
                tempStreetAddress = addr.street.orEmpty()
            }
        }

        // Education
        educationList.clear()
        m.education.forEach { edu ->
            educationList.add(
                EducationEntry(
                    instituteName = edu.instituteName.orEmpty(),
                    degree = edu.degree.orEmpty(),
                    specialization = edu.specialization.orEmpty(),
                    completionDate = edu.completionDate?.let { formatDateForDisplay(it) } ?: " "
                )
            )
        }

        // Experience
        experienceList.clear()
        m.workExperience.forEach { exp ->
            experienceList.add(
                ExperienceEntry(
                    companyName = exp.companyName.orEmpty(),
                    jobTitle = exp.jobTitle.orEmpty(),
                    fromDate = exp.fromDate?.let { formatDateForDisplay(it) } ?: " ",
                    toDate = exp.toDate?.let { formatDateForDisplay(it) } ?: " ",
                    jobDescription = exp.jobDescription.orEmpty(),
                    isCurrentRole = exp.isRelevant
                )
            )
        }

        // Work Info
        memberId = m.memberId.orEmpty()
        doj = m.doj?.let { formatDateForDisplay(it) } ?: " "
        workLocation = m.workingDistrict.orEmpty()
        employmentType = m.employmentType?.replaceFirstChar { it.uppercase() } ?: "Select Employment Type"

        selectedBranchId = m.branchId?._id
        branch = branchList.find { it.id == selectedBranchId }?.name ?: m.branchId?.name ?: "Select Branch"

        selectedDepartmentId = m.departmentId?._id
        department = departmentList.find { it._id == selectedDepartmentId }?.name ?: m.departmentId?.name ?: "Select Department"

        selectedDesignationId = m.designationId
        designation = designationList.find { it.id == selectedDesignationId }?.name ?: "Select Designation"

        selectedRoleId = m.customRoleId?._id
        role = roles.find { it._id == selectedRoleId }?.name ?: m.customRoleId?.name ?: "Select Role"

        selectedShiftId = m.shiftId
        shift = shifts.find { it._id == selectedShiftId }?.name ?: "Select Shift"

        selectedReportingToId = m.reportingTo
        reportingTo = members.find { it._id == selectedReportingToId }?.displayName() ?: "Select Reporting To"

        selectedSecondaryReportingToId = m.secondaryReportingTo
        secondaryReportingTo = members.find { it._id == selectedSecondaryReportingToId }?.displayName() ?: "Select Secondary Reporting To"

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

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
            // ── Header ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(whiteBg)
                    .padding(horizontal = sectionGap, vertical = smallGap + tinyGap),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (mode) {
                        ScreenMode.VIEW -> "View Employee"
                        ScreenMode.EDIT -> "Edit Employee"
                        ScreenMode.CREATE -> "Employee Onboarding"
                    },
                    fontSize = tokens.h1,
                    fontWeight = FontWeight.Bold,
                    color = TitleColor
                )
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Close",
                    tint = LabelColor,
                    modifier = Modifier
                        .size(tokens.iconSize)
                        .clickable {
                            hrViewModel.clearMemberDetail()
                            onDismiss()
                        }
                )
            }
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
                // ── Basic Information ──
                AccordionSection(
                    iconPainter = painterResource(R.drawable.person),
                    title = "Basic Information",
                    expanded = expandedSection == "Basic Information",
                    onHeaderClick = { expandedSection = if (expandedSection == "Basic Information") "" else "Basic Information" }
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
                    FormLabel("First Name")
                    FormTextField(
                        value = firstName,
                        onValueChange = {
                            if (isEditable) {
                                firstName = it
                                if (currentErrorField == "First Name") { currentErrorField = null; topError = null }
                            }
                        },
                        placeholder = "Enter Your First Name",
                        enabled = isEditable,
                        isError = currentErrorField == "First Name",
                        errorMessage = if (currentErrorField == "First Name") "First name is required" else null
                    )

                    Spacer(Modifier.height(fieldGap))
                    FormLabel("Last Name")
                    FormTextField(
                        value = lastName,
                        onValueChange = {
                            if (isEditable) {
                                lastName = it
                                if (currentErrorField == "Last Name") { currentErrorField = null; topError = null }
                            }
                        },
                        placeholder = "Enter Your Last Name",
                        enabled = isEditable,
                        isError = currentErrorField == "Last Name",
                        errorMessage = if (currentErrorField == "Last Name") "Last name is required" else null
                    )
                    Spacer(Modifier.height(fieldGap))
                    FormLabel("Work Email")
                    FormTextField(
                        value = workEmail,
                        onValueChange = { if (isEditable) workEmail = it },
                        placeholder = "Enter Your Work Email",
                        enabled = isEditable,
                        keyboardType = KeyboardType.Email
                    )

                    Spacer(Modifier.height(fieldGap))
                    FormLabel("Personal Email")
                    FormTextField(
                        value = personalEmail,
                        onValueChange = { if (isEditable) personalEmail = it },
                        placeholder = "Enter Your Personal Email",
                        enabled = isEditable,
                        keyboardType = KeyboardType.Email
                    )

                    Spacer(Modifier.height(fieldGap))
                    FormLabel("Work Phone")
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
                        onPhoneChange = {
                            if (isEditable) {
                                personalPhone = it
                                if (currentErrorField == "Personal Phone") { currentErrorField = null; topError = null }
                            }
                        },
                        onCountryChange = { if (isEditable) personalPhoneCountry = it },
                        enabled = isEditable,
                        isError = currentErrorField == "Personal Phone",
                        errorMessage = if (currentErrorField == "Personal Phone") "Personal phone is required" else null
                    )
                    Spacer(Modifier.height(fieldGap))
                    FormLabel("Date of Birth")
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
                    FormLabel("Gender")
                    ErrorFieldWrapper(isError = currentErrorField == "Gender") {
                        FormDropdown(
                            label = "Gender",
                            value = gender,
                            expanded = genderExpanded && isEditable,
                            onExpandChange = { if (isEditable) genderExpanded = it },
                            options = listOf("Male", "Female", "Other"),
                            onOptionSelected = {
                                if (isEditable) {
                                    gender = it
                                    if (currentErrorField == "Gender") { currentErrorField = null; topError = null }
                                }
                            }
                        )
                    }

                    Spacer(Modifier.height(fieldGap))
                    FormDropdown(
                        label = "Marital Status",
                        value = maritalStatus,
                        expanded = maritalExpanded && isEditable,
                        onExpandChange = { if (isEditable) maritalExpanded = it },
                        options = listOf("Single", "Married", "Divorced", "Widowed"),
                        onOptionSelected = { if (isEditable) maritalStatus = it }
                    )
                }

                // ── Address ──
                AccordionSection(
                    iconPainter = painterResource(R.drawable.ic_location),
                    title = "Address",
                    expanded = expandedSection == "Address",
                    onHeaderClick = { expandedSection = if (expandedSection == "Address") "" else "Address" }
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
                            selectedCountry = country,
                            selectedState = state,
                            enabled = isEditable,
                            onCountryChange = { if (isEditable) country = it },
                            onStateChange = { if (isEditable) state = it }
                        )
                        Spacer(Modifier.height(fieldGap))
                        FormLabel("City")
                        FormTextField(
                            value = city,
                            onValueChange = { if (isEditable) city = it },
                            placeholder = "Enter Your City",
                            enabled = isEditable
                        )
                        Spacer(Modifier.height(fieldGap))
                        FormLabel("Postal Code")
                        FormTextField(
                            value = postalCode,
                            onValueChange = { if (isEditable) postalCode = it },
                            placeholder = "Enter postal code",
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
                                        tempCountry = country
                                        tempState = state
                                        tempCity = city
                                        tempPostalCode = postalCode
                                        tempStreetAddress = streetAddress
                                    }
                                }
                        ) {
                            Checkbox(
                                checked = isSameAsPermanent,
                                onCheckedChange = { checked ->
                                    if (isEditable) {
                                        isSameAsPermanent = checked
                                        if (checked) {
                                            tempCountry = country
                                            tempState = state
                                            tempCity = city
                                            tempPostalCode = postalCode
                                            tempStreetAddress = streetAddress
                                        }
                                    }
                                },
                                enabled = isEditable,
                                colors = CheckboxDefaults.colors(checkedColor = AccentColor)
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
                            selectedCountry = tempCountry,
                            selectedState = tempState,
                            enabled = isEditable,
                            onCountryChange = { if (isEditable) tempCountry = it },
                            onStateChange = { if (isEditable) tempState = it }
                        )
                        Spacer(Modifier.height(fieldGap))
                        FormLabel("City")
                        FormTextField(
                            value = tempCity,
                            onValueChange = { if (isEditable) tempCity = it },
                            placeholder = "Enter Your City",
                            enabled = isEditable
                        )
                        Spacer(Modifier.height(fieldGap))
                        FormLabel("Postal Code")
                        FormTextField(
                            value = tempPostalCode,
                            onValueChange = { if (isEditable) tempPostalCode = it },
                            placeholder = "Enter postal code",
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

                // ── Government IDs ──
                AccordionSection(
                    iconPainter = painterResource(R.drawable.ic_credit),
                    title = "Government IDs",
                    expanded = expandedSection == "Government IDs",
                    onHeaderClick = { expandedSection = if (expandedSection == "Government IDs") "" else "Government IDs" }
                ) {
                    FormLabel("PAN Number")
                    Column {
                        FormTextField(
                            value = pan,
                            onValueChange = {
                                if (isEditable) {
                                    val newValue = it.uppercase().take(10)
                                    pan = newValue
                                    if (newValue.length >= 10) {
                                        validatePanNumber(newValue)
                                    } else {
                                        panError = null
                                    }
                                }
                            },
                            placeholder = "Enter PAN Number (e.g., ABCDE1234F)",
                            enabled = isEditable,
                            isError = panError != null,
                            errorMessage = null,
                            keyboardType = KeyboardType.Text,
                            keyboardCapitalization = KeyboardCapitalization.Characters
                        )
                        if (panError != null) {
                            Text(
                                text = panError!!,
                                color = Color(0xFFDC2626),
                                fontSize = tokens.caption,
                                modifier = Modifier.padding(top = tinyGap)
                            )
                        }
                        if (pan.isNotEmpty() && panError == null && pan.length == 10) {
                            Text(
                                text = "✓ Valid PAN number",
                                color = Color(0xFF059669),
                                fontSize = tokens.caption,
                                modifier = Modifier.padding(top = tinyGap)
                            )
                        }
                    }

                    Spacer(Modifier.height(fieldGap))
                    FormLabel("Aadhaar Number")
                    Column {
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
                                        val spacesBefore =
                                            formatted.substring(0, o).count { it == ' ' }
                                        return (o - spacesBefore).coerceIn(0, trimmed.length)
                                    }
                                }
                                TransformedText(AnnotatedString(formatted), offsetMapping)
                            }
                        }
                        FormTextField(
                            value = aadhaar,
                            onValueChange = {
                                if (isEditable) {
                                    val newValue = it.filter { char -> char.isDigit() }.take(12)
                                    aadhaar = newValue
                                    if (newValue.length >= 12) {
                                        validateAadhaarNumber(newValue)
                                    } else {
                                        aadhaarError = null
                                    }
                                }
                            },
                            placeholder = "Enter 12-digit Aadhaar Number",
                            enabled = isEditable,
                            isError = aadhaarError != null,
                            errorMessage = null,
                            keyboardType = KeyboardType.Number,
                            visualTransformation = aadhaarVisualTransformation
                        )
                        if (aadhaarError != null) {
                            Text(
                                text = aadhaarError!!,
                                color = Color(0xFFDC2626),
                                fontSize = tokens.caption,
                                modifier = Modifier.padding(top = tinyGap)
                            )
                        }
                        if (aadhaar.isNotEmpty() && aadhaarError == null && aadhaar.length == 12) {
                            Text(
                                text = "✓ Valid Aadhaar number",
                                color = Color(0xFF059669),
                                fontSize = tokens.caption,
                                modifier = Modifier.padding(top = tinyGap)
                            )
                        }
                    }

                    Spacer(Modifier.height(fieldGap))
                    FormLabel("UAN Number")
                    Column {
                        FormTextField(
                            value = uan,
                            onValueChange = {
                                if (isEditable) {
                                    val newValue = it.filter { char -> char.isDigit() }.take(12)
                                    uan = newValue
                                    if (newValue.length >= 12) {
                                        validateUanNumber(newValue)
                                    } else {
                                        uanError = null
                                    }
                                }
                            },
                            placeholder = "Enter 12-digit UAN Number",
                            enabled = isEditable,
                            isError = uanError != null,
                            errorMessage = null,
                            keyboardType = KeyboardType.Number
                        )
                        if (uanError != null) {
                            Text(
                                text = uanError!!,
                                color = Color(0xFFDC2626),
                                fontSize = tokens.caption,
                                modifier = Modifier.padding(top = tinyGap)
                            )
                        }
                        if (uan.isNotEmpty() && uanError == null && uan.length == 12) {
                            Text(
                                text = "✓ Valid UAN number",
                                color = Color(0xFF059669),
                                fontSize = tokens.caption,
                                modifier = Modifier.padding(top = tinyGap)
                            )
                        }
                    }

                    Spacer(Modifier.height(smallGap + tinyGap))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(WarnBg, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                            .border(1.dp, WarnBorder, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                            .padding(horizontal = smallGap, vertical = smallGap * 0.8f)
                    ) {
                        Text(
                            "These IDs are sensitive information and will be stored securely.",
                            fontSize = tokens.caption,
                            color = WarnText
                        )
                    }
                }

                // ── Education ──
                AccordionSection(
                    iconPainter = painterResource(R.drawable.ic_education),
                    title = "Education",
                    expanded = expandedSection == "Education",
                    onHeaderClick = { expandedSection = if (expandedSection == "Education") "" else "Education" }
                ) {
                    if (educationList.isEmpty()) {
                        Text(
                            "No education added",
                            fontSize = tokens.bodySmall,
                            color = LabelColor,
                            modifier = Modifier.padding(vertical = smallGap)
                        )
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
                            FormLabel("Institute Name")
                            FormTextField(
                                value = entry.instituteName,
                                onValueChange = { if (isEditable) educationList[educationList.indexOf(entry)] = entry.copy(instituteName = it) },
                                placeholder = "Enter Institute Name",
                                enabled = isEditable
                            )

                            Spacer(Modifier.height(fieldGap))
                            FormLabel("Degree/Diploma")
                            FormTextField(
                                value = entry.degree,
                                onValueChange = { if (isEditable) educationList[educationList.indexOf(entry)] = entry.copy(degree = it) },
                                placeholder = "Enter Degree/Diploma",
                                enabled = isEditable
                            )

                            Spacer(Modifier.height(fieldGap))
                            FormLabel("Specialization")
                            FormTextField(
                                value = entry.specialization,
                                onValueChange = { if (isEditable) educationList[educationList.indexOf(entry)] = entry.copy(specialization = it) },
                                placeholder = "Enter Specialization",
                                enabled = isEditable
                            )

                            Spacer(Modifier.height(fieldGap))
                            FormLabel("Completion Date")
                            DatePickerField(
                                value = entry.completionDate,
                                onDateSelected = { if (isEditable) educationList[educationList.indexOf(entry)] = entry.copy(completionDate = it) },
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

                // ── Experience ──
                AccordionSection(
                    icon = Icons.Outlined.Work,
                    title = "Experience",
                    expanded = expandedSection == "Experience",
                    onHeaderClick = { expandedSection = if (expandedSection == "Experience") "" else "Experience" }
                ) {
                    if (experienceList.isEmpty()) {
                        Text(
                            "No experience added",
                            fontSize = tokens.bodySmall,
                            color = LabelColor,
                            modifier = Modifier.padding(vertical = smallGap)
                        )
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
                            FormLabel("Company Name")
                            FormTextField(
                                value = entry.companyName,
                                onValueChange = { if (isEditable) experienceList[experienceList.indexOf(entry)] = entry.copy(companyName = it) },
                                placeholder = "Enter Company Name",
                                enabled = isEditable
                            )

                            Spacer(Modifier.height(fieldGap))
                            FormLabel("Job Title")
                            FormTextField(
                                value = entry.jobTitle,
                                onValueChange = { if (isEditable) experienceList[experienceList.indexOf(entry)] = entry.copy(jobTitle = it) },
                                placeholder = "Enter Job Title",
                                enabled = isEditable
                            )

                            Spacer(Modifier.height(fieldGap))
                            FormLabel("From Date")
                            DatePickerField(
                                value = entry.fromDate,
                                onDateSelected = { if (isEditable) experienceList[experienceList.indexOf(entry)] = entry.copy(fromDate = it) },
                                enabled = isEditable
                            )

                            Spacer(Modifier.height(fieldGap))
                            FormLabel("To Date")
                            DatePickerField(
                                value = entry.toDate,
                                onDateSelected = { if (isEditable) experienceList[experienceList.indexOf(entry)] = entry.copy(toDate = it) },
                                enabled = !entry.isCurrentRole && isEditable
                            )

                            Spacer(Modifier.height(fieldGap))
                            FormLabel("Job Description")
                            OutlinedTextField(
                                value = entry.jobDescription,
                                onValueChange = { if (isEditable) experienceList[experienceList.indexOf(entry)] = entry.copy(jobDescription = it) },
                                placeholder = { Text("Enter Job Description", fontSize = tokens.bodyMedium, color = Color(0xFF9CA3AF)) },
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

                            Spacer(Modifier.height(smallGap * 0.8f))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = entry.isCurrentRole,
                                    onCheckedChange = { checked ->
                                        if (isEditable) experienceList[experienceList.indexOf(entry)] = entry.copy(isCurrentRole = checked)
                                    },
                                    enabled = isEditable,
                                    colors = CheckboxDefaults.colors(checkedColor = AccentColor)
                                )
                                Text("This experience is relevant to current role", fontSize = tokens.bodySmall, color = if (isEditable) LabelColor else LabelColor.copy(alpha = 0.6f))
                            }

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

                // ── Work Info ──
                AccordionSection(
                    iconPainter = painterResource(R.drawable.ic_building),
                    title = "Work Info",
                    expanded = expandedSection == "Work Info",
                    onHeaderClick = { expandedSection = if (expandedSection == "Work Info") "" else "Work Info" }
                ) {
                    FormLabel("Member ID")
                    FormTextField(
                        value = memberId,
                        onValueChange = { if (isEditable) memberId = it },
                        placeholder = "Enter Member ID",
                        enabled = isEditable
                    )

                    Spacer(Modifier.height(fieldGap))
                    FormLabel("Employee Code")
                    FormTextField(
                        value = employeeCode,
                        onValueChange = { if (isEditable) employeeCode = it },
                        placeholder = "Enter Employee Code",
                        enabled = isEditable
                    )

                    Spacer(Modifier.height(fieldGap))
                    FormLabel("Date of Joining")
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
                        label = "Department",
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
                        label = "Role",
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
                        label = "Employment Type",
                        value = employmentType,
                        expanded = employmentTypeExpanded && isEditable,
                        onExpandChange = { if (isEditable) employmentTypeExpanded = it },
                        options = listOf("Full-time", "Part-time", "Contract"),
                        onOptionSelected = { if (isEditable) employmentType = it }
                    )

                    Spacer(Modifier.height(fieldGap))
                    FormLabel("Work Location")
                    FormTextField(
                        value = workLocation,
                        onValueChange = { if (isEditable) workLocation = it },
                        placeholder = "Enter Work Location",
                        enabled = isEditable
                    )

                    Spacer(Modifier.height(fieldGap))
                    FormDropdown(
                        label = "Reporting To",
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
                        label = "Secondary Reporting To",
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
                }
            }
        }

        // ── Floating Action Button (Only in Create/Edit mode) ──
        if (mode != ScreenMode.VIEW) {
            ExtendedFloatingActionButton(
                onClick = {
                    var hasGovIdError = false

                    if (pan.isNotBlank()) {
                        val panResult = GovernmentIdValidator.validatePan(pan)
                        if (!panResult.isValid) {
                            currentErrorField = "PAN"
                            topError = panResult.message
                            expandedSection = "Government IDs"
                            hasGovIdError = true
                        }
                    }

                    if (!hasGovIdError && aadhaar.isNotBlank()) {
                        val aadhaarResult = GovernmentIdValidator.validateAadhaar(aadhaar)
                        if (!aadhaarResult.isValid) {
                            currentErrorField = "Aadhaar"
                            topError = aadhaarResult.message
                            expandedSection = "Government IDs"
                            hasGovIdError = true
                        }
                    }

                    if (!hasGovIdError && uan.isNotBlank()) {
                        val uanResult = GovernmentIdValidator.validateUan(uan)
                        if (!uanResult.isValid) {
                            currentErrorField = "UAN"
                            topError = uanResult.message
                            expandedSection = "Government IDs"
                            hasGovIdError = true
                        }
                    }

                    if (!hasGovIdError) {
                        val missingField = findFirstMissingField()
                        if (missingField != null) {
                            currentErrorField = missingField
                            topError = "$missingField is required"
                            expandedSection = when (missingField) {
                                "First Name", "Last Name", "Work Phone", "Personal Phone", "Date of Birth", "Gender" -> "Basic Information"
                                "Date of Joining", "Department", "Role" -> "Work Info"
                                else -> expandedSection
                            }
                        } else {
                            currentErrorField = null
                            topError = null

                            val createRequest = CreateMemberRequest(
                                firstName = firstName,
                                lastName = lastName,
                                email = workEmail,
                                personalEmail = personalEmail,
                                personalMobile = personalPhone,
                                workMobile = workPhone,
                                dob = toApiDate(dob),
                                gender = gender.lowercase(),
                                martialStatus = maritalStatus.lowercase(),
                                doj = toApiDate(doj),
                                branchId = selectedBranchId,
                                departmentId = selectedDepartmentId,
                                designationId = selectedDesignationId,
                                customRoleId = selectedRoleId,
                                shiftId = selectedShiftId,
                                workingDistrict = workLocation,
                                employmentType = employmentType.lowercase().replace(" ", "-"),
                                reportingTo = selectedReportingToId,
                                secondaryReportingTo = selectedSecondaryReportingToId,
                                permanentAddress = AddressRequest(country, state, city, streetAddress, postalCode),
                                hasTemporaryAddress = addressTab == "Temporary",
                                temporaryAddress = if (addressTab == "Temporary")
                                    AddressRequest(tempCountry, tempState, tempCity, tempStreetAddress, tempPostalCode)
                                else null,
                                education = educationList.map {
                                    EducationRequestItem(it.instituteName, it.degree, it.specialization, toApiDate(it.completionDate))
                                },
                                workExperience = experienceList.map {
                                    WorkExperienceRequestItem(it.companyName, it.jobTitle, toApiDate(it.fromDate), it.toDate, it.jobDescription, it.isCurrentRole)
                                }
                            )
                            val updateRequest = UpdateMemberRequest(
                                firstName = firstName,
                                lastName = lastName,
                                personalEmail = personalEmail,
                                personalMobile = personalPhone,
                                workMobile = workPhone,
                                dob = dob.toIsoDate(),
                                gender = gender.lowercase(),
                                martialStatus = maritalStatus,
                                doj = doj.toIsoDate(),
                                branchId = selectedBranchId,
                                departmentId = selectedDepartmentId,
                                designationId = selectedDesignationId,
                                customRoleId = selectedRoleId,
                                shiftId = selectedShiftId,
                                workingDistrict = workLocation,
                                employmentType = employmentType.lowercase(),
                                reportingTo = selectedReportingToId,
                                secondaryReportingTo = selectedSecondaryReportingToId,
                                permanentAddress = AddressRequest(country, state, city, streetAddress, postalCode),
                                hasTemporaryAddress = addressTab == "Temporary",
                                temporaryAddress = if (addressTab == "Temporary")
                                    AddressRequest(tempCountry, tempState, tempCity, tempStreetAddress, tempPostalCode)
                                else null,
                                education = educationList.map {
                                    EducationRequestItem(it.instituteName, it.degree, it.specialization, toApiDate(it.completionDate))
                                },
                                workExperience = experienceList.map {
                                    WorkExperienceRequestItem(it.companyName, it.jobTitle, toApiDate(it.fromDate), it.toDate, it.jobDescription, it.isCurrentRole)
                                }
                            )

                            if (mode == ScreenMode.EDIT && memberIdToLoad != null) {
                                hrViewModel.updateMember(memberIdToLoad, updateRequest)
                            } else {
                                hrViewModel.createMember(createRequest)
                            }
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