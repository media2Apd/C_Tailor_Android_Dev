@file:Suppress(
    "UNUSED_VALUE",
    "AssignedValueIsNeverRead",
    "unused",
    "NAME_SHADOWING",
    "GrazieInspection",
    "SpellCheckingInspection", "VariableNeverRead"
)
package com.cuso.mobile.view.home.hr


import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.cuso.mobile.view.home.FormDropdown
import com.cuso.mobile.view.home.FormLabel
import com.cuso.mobile.view.home.FormTextField
import com.cuso.mobile.viewmodel.BranchViewModel
import com.cuso.mobile.viewmodel.DepartmentViewModel
import com.cuso.mobile.viewmodel.DesignationViewModel
import com.cuso.mobile.viewmodel.HrViewModel
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.cuso.mobile.model.hr.displayName
import com.cuso.mobile.view.composable.CountryAndStatePicker
import com.cuso.mobile.view.composable.DatePickerField
import com.cuso.mobile.viewmodel.BranchUiState
import com.cuso.mobile.viewmodel.DepartmentUiState
import com.cuso.mobile.viewmodel.DesignationUiState
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.cuso.mobile.model.Country
import com.cuso.mobile.model.hr.AddressRequest
import com.cuso.mobile.model.hr.CreateMemberRequest
import com.cuso.mobile.model.hr.EducationRequestItem
import com.cuso.mobile.model.hr.UpdateMemberRequest
import com.cuso.mobile.model.hr.WorkExperienceRequestItem
import com.cuso.mobile.view.composable.DynamicIslandError
import com.cuso.mobile.view.composable.DynamicIslandSuccess
import com.cuso.mobile.view.composable.ErrorFieldWrapper
import com.cuso.mobile.view.composable.PhoneInputField
import com.cuso.mobile.view.home.reusablecomposables.GovernmentIdValidator
import com.cuso.mobile.view.home.toIsoDate
import com.cuso.mobile.viewmodel.Authenticate
import androidx.activity.ComponentActivity
import com.cuso.mobile.view.composable.CirculerProgressIndicatorSmall
import com.yalantis.ucrop.UCrop
import java.io.File


// ── Design tokens (match screenshot) ──
private val AccentColor = Color(0xFF4F39F6)
private val BorderColor = Color(0xFFE3E4E8)
private val LabelColor = Color(0xFF6B7280)
private val TitleColor = Color(0xFF111827)
private val WarnBg = Color(0xFFFFF7E6)
private val WarnBorder = Color(0xFFFCE3B0)
private val WarnText = Color(0xFF9A6A17)
private val FieldShape = RoundedCornerShape(10.dp)

// ── Screen mode ──
enum class ScreenMode { CREATE, VIEW, EDIT }

// education / experience data model

data class EducationEntry(
    val id: String = java.util.UUID.randomUUID().toString(),
    val instituteName: String = "",
    val degree: String = "",
    val specialization: String = "",
    val completionDate: String = "Select Date"
)

data class ExperienceEntry(
    val id: String = java.util.UUID.randomUUID().toString(),
    val companyName: String = "",
    val jobTitle: String = "",
    val fromDate: String = "Select Date",
    val toDate: String = "Select Date",
    val jobDescription: String = "",
    val isCurrentRole: Boolean = false
)
@SuppressLint("ContextCastToActivity")
@Suppress("UNUSED_VARIABLE", "UNUSED_PARAMETER", "NAME_SHADOWING")

@Composable
fun EmployeeOnboardingScreen(
    mode: ScreenMode = ScreenMode.CREATE,
    memberIdToLoad: String? = null,          // pass the _id when VIEW/EDIT
    onDismiss: () -> Unit = {},
    onCreateEmployee: () -> Unit = {},
    onUpdateEmployee: () -> Unit = {},
    hrViewModel: HrViewModel = hiltViewModel(),
    branchViewModel: BranchViewModel = hiltViewModel(),
    departmentViewModel: DepartmentViewModel = hiltViewModel(),
    designationViewModel: DesignationViewModel = hiltViewModel()
) {
    // TopBar.kt la um SAME MAADHIRI:
    val authViewModel: Authenticate = hiltViewModel(
        LocalContext.current as ComponentActivity
    )
    DisposableEffect(Unit) {
        Log.d("LIFECYCLE_DEBUG", "EmployeeOnboardingScreen ENTERED composition")
        onDispose {
            Log.d("LIFECYCLE_DEBUG", "EmployeeOnboardingScreen LEFT composition")
        }
    }
    val isReadOnly = mode == ScreenMode.VIEW

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

    var dob by remember { mutableStateOf("Select Date") }
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
    var tempCountry by remember { mutableStateOf("Select country") }
    var tempState by remember { mutableStateOf("Select state") }
    var tempCity by remember { mutableStateOf("") }
    var tempPostalCode by remember { mutableStateOf("") }
    var tempStreetAddress by remember { mutableStateOf("") }

    // ── Government IDs ──
    var pan by remember { mutableStateOf("") }
    var aadhaar by remember { mutableStateOf("") }
    var uan by remember { mutableStateOf("") }

    // ── Education (dynamic list) ──
    val educationList = remember { mutableStateListOf<EducationEntry>() }

    // ── Experience (dynamic list) ──
    val experienceList = remember { mutableStateListOf<ExperienceEntry>() }

    // ── Work Info (static, single set of fields) ──
    var memberId by remember { mutableStateOf("") }
    var employeeCode by remember { mutableStateOf("") }
    var doj by remember { mutableStateOf("Select Date") }
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

    // ── Validation state ──
    var currentErrorField by remember { mutableStateOf<String?>(null) }
    var topError by remember { mutableStateOf<String?>(null) }

    val uploadPictureState by hrViewModel.uploadPictureState.collectAsState()

    val deletePictureState by hrViewModel.deletePictureState.collectAsState()
    val memberDetail by hrViewModel.memberDetail.collectAsState()   // ✅ idha idhku mேலே kondu vaanga

//    val currentUserId = authViewModel.user.value?.id
//    val memberUserId = memberDetail?.userId?._id
//
//    if (!memberUserId.isNullOrBlank() && memberUserId == currentUserId) {
//        authViewModel.updateUserProfilePictureIfCurrentUser(state.pictureUrl)
//    }
    val cropLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val resultUri = result.data?.let { UCrop.getOutput(it) }
            resultUri?.let { uri ->
                profileImageUri = uri

                if (mode == ScreenMode.EDIT && memberIdToLoad != null) {
                    val file = uriToFile(context, uri)
                    hrViewModel.uploadProfilePicture(memberIdToLoad, file)
                }
            }
        } else if (result.resultCode == UCrop.RESULT_ERROR) {
            val cropError = result.data?.let { UCrop.getError(it) }
            Log.e("CROP_ERROR", "Error: $cropError")
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
                setCompressionFormat(android.graphics.Bitmap.CompressFormat.JPEG)
                setToolbarColor(android.graphics.Color.parseColor("#4F39F6"))
                setToolbarWidgetColor(android.graphics.Color.WHITE)
            }

            val uCropIntent = UCrop.of(sourceUri, destinationUri)
                .withAspectRatio(1f, 1f)
                .withMaxResultSize(1000, 1000)
                .withOptions(options)
                .getIntent(context)

            cropLauncher.launch(uCropIntent) // கிராப் ஸ்கிரீன் ஓபன் ஆகும்
        }
    }
    LaunchedEffect(uploadPictureState) {
        when (val state = uploadPictureState) {
            is HrViewModel.UploadPictureState.Success -> {
                existingProfilePictureUrl = state.pictureUrl
                profileImageUri = null
                topSuccess = "Profile Uploaded Successfully"

                val targetId = memberDetail?._id
                val currentId = authViewModel.user
                Log.d("PROFILE_PIC_DEBUG_UPLOAD", "target=$targetId current=$currentId")

                // ✅ decision முழுசும் ViewModel எடுக்கும் — screen வெறும் target id தான் தர வேண்டும்
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
//    val memberDetail by hrViewModel.memberDetail.collectAsState()

    // Add these state variables in your EmployeeOnboardingScreen
// ── Government IDs validation states ──
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

    // ── Validation functions ──
    fun validatePanNumber(value: String): Boolean {
        if (value.isBlank()) {
            panError = null
            isPanValid = false
            return true  // Empty is valid (optional field)
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
            return true  // Empty is valid (optional field)
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
            return true  // Empty is valid (optional field)
        }
        val result = GovernmentIdValidator.validateUan(value)
        uanError = if (result.isValid) null else result.message
        isUanValid = result.isValid
        return result.isValid
    }

    // ── date helpers ──
    // ADJUST the display pattern below to match whatever DatePickerField actually returns/expects
    // ── date helpers ──
    fun toApiDate(displayDate: String): String {
        if (displayDate.isBlank() || displayDate == "Select Date") return ""
        return try {
            // Try multiple formats
            val formats = listOf(
                "dd-MM-yyyy",
                "dd MMM yyy",
                "dd/MM/yyyy",
                "yyyy-MM-dd"
            )

            for (format in formats) {
                try {
                    val input = java.text.SimpleDateFormat(format, java.util.Locale.getDefault())
                    val output = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                    return output.format(input.parse(displayDate)!!)
                } catch (_: Exception) {
                    // Try next format
                }
            }
            // If all formats fail, return as-is (might already be yyyy-MM-dd)
            displayDate
        } catch (e: Exception) {
            displayDate
        }
    }

    fun formatDateForDisplay(isoDate: String): String {
        if (isoDate.isBlank()) return "Select Date"
        return try {
            val input = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
            val output = java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault())   // ✅ changed
            output.format(input.parse(isoDate)!!)
        } catch (e: Exception) {
            try {
                val input = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                val output = java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault())   // ✅ changed
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
            dob.isBlank() || dob == "Select Date" -> "Date of Birth"
            gender.isBlank() || gender == "Select Gender" -> "Gender"
            doj.isBlank() || doj == "Select Date" -> "Date of Joining"
            department == "Select Department" -> "Department"
            role == "Select Role" -> "Role"
            else -> null
        }
    }

    // ── Initial load: dropdown lists + (if VIEW/EDIT) member detail ──
    LaunchedEffect(Unit) {
        hrViewModel.fetchMembers()
        hrViewModel.fetchRoles()
        hrViewModel.fetchShifts()
        branchViewModel.loadBranches()
        departmentViewModel.loadDepartments()
        designationViewModel.loadDesignations()
    }

// ✅ NEW — separate effect, keyed on memberIdToLoad so it re-fires
// every time a DIFFERENT employee is opened, and clears stale data first
    LaunchedEffect(memberIdToLoad) {
        hrViewModel.clearMemberDetail()   // wipe old employee's data immediately
        if (mode != ScreenMode.CREATE && memberIdToLoad != null) {
            hrViewModel.fetchMemberDetail(memberIdToLoad)
        }
    }

    // ── Prefill once detail is available (runs once per memberIdToLoad) ──
    var prefilled by remember { mutableStateOf(false) }

    LaunchedEffect(
        memberDetail,
        branchList,
        departmentList,
        designationList,
        roles,
        shifts,
        members
    ) {
        Log.d("PREFILL_DEBUG", "Effect fired. memberIdToLoad=$memberIdToLoad, memberDetail=$memberDetail")
        val m = memberDetail ?: run {
            Log.d("PREFILL_DEBUG", "memberDetail is NULL — skipping prefill")
            return@LaunchedEffect
        }
        Log.d("PREFILL_DEBUG", "Prefilling with firstName=${m.firstName}")
//        val m = memberDetail ?: return@LaunchedEffect

        // Basic Information
        firstName = m.firstName.orEmpty()
        lastName = m.lastName.orEmpty()
        workPhone = m.workMobile.orEmpty()
        personalPhone = m.personalMobile.orEmpty()

        dob = m.dob?.let { formatDateForDisplay(it) } ?: "Select Date"
        gender = m.gender?.replaceFirstChar { it.uppercase() } ?: "Select Gender"
        maritalStatus =
            m.martialStatus?.replaceFirstChar { it.uppercase() } ?: "Select Marital Status"

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
                    completionDate = edu.completionDate?.let {
                        formatDateForDisplay(it)
                    } ?: "Select Date"
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
                    fromDate = exp.fromDate?.let {
                        formatDateForDisplay(it)
                    } ?: "Select Date",

                    jobDescription = exp.jobDescription.orEmpty(),
                    isCurrentRole = exp.isRelevant
                )
            )
        }

        // Work Info
        memberId = m.memberId.orEmpty()
        doj = m.doj?.let { formatDateForDisplay(it) } ?: "Select Date"
        workLocation = m.workingDistrict.orEmpty()
        employmentType =
            m.employmentType?.replaceFirstChar { it.uppercase() } ?: "Select Employment Type"

        // Branch
        selectedBranchId = m.branchId?._id
        branch = branchList.find { it.id == selectedBranchId }?.name
            ?: m.branchId?.name
                    ?: "Select Branch"

        // Department
        selectedDepartmentId = m.departmentId?._id
        department = departmentList.find { it._id == selectedDepartmentId }?.name
            ?: m.departmentId?.name
                    ?: "Select Department"

        // Designation
        selectedDesignationId = m.designationId
        designation = designationList.find { it.id == selectedDesignationId }?.name
            ?: "Select Designation"

        // Role
        selectedRoleId = m.customRoleId?._id
        role = roles.find { it._id == selectedRoleId }?.name
            ?: m.customRoleId?.name
                    ?: "Select Role"

        // Shift
        selectedShiftId = m.shiftId
        shift = shifts.find { it._id == selectedShiftId }?.name
            ?: "Select Shift"

        // Reporting To
        selectedReportingToId = m.reportingTo
        reportingTo = members.find { it._id == selectedReportingToId }?.displayName()
            ?: "Select Reporting To"

        // Secondary Reporting To
        selectedSecondaryReportingToId = m.secondaryReportingTo
        secondaryReportingTo =
            members.find { it._id == selectedSecondaryReportingToId }?.displayName()
                ?: "Select Secondary Reporting To"

        existingProfilePictureUrl = m.profilePicture
    }
//    LaunchedEffect(memberIdToLoad) {
//        // New target member -> allow prefill to run again
//        prefilled = false
//    }


    // ── React to create/update result ──
    LaunchedEffect(createMemberState) {
        when (val state = createMemberState) {
            is HrViewModel.CreateMemberState.Success -> {
                hrViewModel.resetCreateMemberState()

                topSuccess = if (mode == ScreenMode.EDIT)
                    "Employee updated successfully"      // ✅ NEW
                else
                    "Employee created successfully"
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
                .background(Color.White)
        ) {
            // ── Header ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (mode) {
                        ScreenMode.VIEW -> "View Employee"
                        ScreenMode.EDIT -> "Edit Employee"
                        ScreenMode.CREATE -> "Employee Onboarding"
                    },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TitleColor
                )
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Close",
                    tint = LabelColor,
                    modifier = Modifier.clickable {
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
                    .padding(bottom = if (mode == ScreenMode.VIEW) 20.dp else 90.dp)
            ) {
                // ── Basic Information ──
                AccordionSection(
                    icon = Icons.Filled.Person,
                    title = "Basic Information",
                    expanded = expandedSection == "Basic Information",
                    onHeaderClick = { expandedSection = if (expandedSection == "Basic Information") "" else "Basic Information" }
                ) {
                    // Avatar with center upload icon / tap-to-manage
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .clip(CircleShape)
                                .background(AccentColor)
                                .clickable(enabled = !isReadOnly) {
                                    if (profileImageUri != null || !existingProfilePictureUrl.isNullOrBlank()) {
                                        showProfileOptionsDialog = true
                                    } else {
                                        imagePickerLauncher.launch("image/*")
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                isUploading -> {
                                    CirculerProgressIndicatorSmall()
                                }
                                profileImageUri != null -> {
                                    AsyncImage(
                                        model = profileImageUri,
                                        contentDescription = "Profile Photo",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.size(88.dp).clip(CircleShape)
                                    )
                                }
                                !existingProfilePictureUrl.isNullOrBlank() -> {
                                    AsyncImage(
                                        model = existingProfilePictureUrl,
                                        contentDescription = "Profile Photo",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.size(88.dp).clip(CircleShape)
                                    )
                                }
                                initials != "?" -> {
                                    Text(initials, color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                                }
                                else -> {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = "Upload Photo",
                                        tint = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                    FormLabel("First Name")
                    FormTextField(
                        value = firstName,
                        onValueChange = {
                            if (!isReadOnly) {
                                firstName = it
                                if (currentErrorField == "First Name") { currentErrorField = null; topError = null }
                            }
                        },
                        placeholder = "Enter Your First Name",
                        isError = currentErrorField == "First Name",
                        errorMessage = if (currentErrorField == "First Name") "First name is required" else null
                    )

                    Spacer(Modifier.height(16.dp))
                    FormLabel("Last Name")
                    FormTextField(
                        value = lastName,
                        onValueChange = {
                            if (!isReadOnly) {
                                lastName = it
                                if (currentErrorField == "Last Name") { currentErrorField = null; topError = null }
                            }
                        },
                        placeholder = "Enter Your Last Name",
                        isError = currentErrorField == "Last Name",
                        errorMessage = if (currentErrorField == "Last Name") "Last name is required" else null
                    )
                    Spacer(Modifier.height(16.dp))
                    FormLabel("Work Email")
                    FormTextField(
                        value = workEmail,
                        onValueChange = { if (!isReadOnly) workEmail = it },
                        placeholder = "Enter Your Work Email",
                        keyboardType = KeyboardType.Email
                    )

                    Spacer(Modifier.height(16.dp))
                    FormLabel("Personal Email")
                    FormTextField(
                        value = personalEmail,
                        onValueChange = { if (!isReadOnly) personalEmail = it },
                        placeholder = "Enter Your Personal Email",
                        keyboardType = KeyboardType.Email
                    )

                    Spacer(Modifier.height(16.dp))
                    FormLabel("Work Phone")
                    PhoneInputField(
                        phoneValue = workPhone,
                        onPhoneChange = {
                            if (!isReadOnly) {
                                workPhone = it
                                if (currentErrorField == "Work Phone") { currentErrorField = null; topError = null }
                            }
                        },
                        onCountryChange = { workPhoneCountry = it },
                        isError = currentErrorField == "Work Phone",
                        errorMessage = if (currentErrorField == "Work Phone") "Work phone is required" else null
                    )
                    Spacer(Modifier.height(16.dp))
                    FormLabel("Personal Phone")
                    PhoneInputField(
                        phoneValue = personalPhone,
                        onPhoneChange = {
                            if (!isReadOnly) {
                                personalPhone = it
                                if (currentErrorField == "Personal Phone") { currentErrorField = null; topError = null }
                            }
                        },
                        onCountryChange = { personalPhoneCountry = it },
                        isError = currentErrorField == "Personal Phone",
                        errorMessage = if (currentErrorField == "Personal Phone") "Personal phone is required" else null
                    )
                    Spacer(Modifier.height(16.dp))
                    FormLabel("Date of Birth")
                    DatePickerField(
                        value = dob,
                        onDateSelected = {
                            if (!isReadOnly) {
                                dob = it
                                if (currentErrorField == "Date of Birth") { currentErrorField = null; topError = null }
                            }
                        },
                        isError = currentErrorField == "Date of Birth"
                    )
                    Spacer(Modifier.height(16.dp))
                    ErrorFieldWrapper(isError = currentErrorField == "Gender") {
                        FormDropdown(
                            label = "Gender",
                            value = gender,
                            expanded = genderExpanded && !isReadOnly,
                            onExpandChange = { if (!isReadOnly) genderExpanded = it },
                            options = listOf("Male", "Female", "Other"),
                            onOptionSelected = {
                                if (!isReadOnly) {
                                    gender = it
                                    if (currentErrorField == "Gender") { currentErrorField = null; topError = null }
                                }
                            }
                        )
                    }

                    Spacer(Modifier.height(16.dp))
                    FormDropdown(
                        label = "Marital Status",
                        value = maritalStatus,
                        expanded = maritalExpanded && !isReadOnly,
                        onExpandChange = { if (!isReadOnly) maritalExpanded = it },
                        options = listOf("Single", "Married", "Divorced", "Widowed"),
                        onOptionSelected = { if (!isReadOnly) maritalStatus = it }
                    )
                }

                // ── Address ──
                AccordionSection(
                    icon = Icons.Filled.LocationOn,
                    title = "Address",
                    expanded = expandedSection == "Address",
                    onHeaderClick = { expandedSection = if (expandedSection == "Address") "" else "Address" }
                ) {
                    AddressTypeToggle(
                        selected = addressTab,
                        onSelect = { if (!isReadOnly) addressTab = it }
                    )

                    Spacer(Modifier.height(16.dp))
                    if (addressTab == "Permanent") {
                        CountryAndStatePicker(
                            selectedCountry = country,
                            selectedState = state,
                            onCountryChange = { if (!isReadOnly) country = it },
                            onStateChange = { if (!isReadOnly) state = it }
                        )
                        Spacer(Modifier.height(16.dp))
                        FormLabel("City")
                        FormTextField(value = city, onValueChange = { if (!isReadOnly) city = it }, placeholder = "Enter Your City")
                        Spacer(Modifier.height(16.dp))
                        FormLabel("Postal Code")
                        FormTextField(value = postalCode, onValueChange = { if (!isReadOnly) postalCode = it }, placeholder = "Enter postal code", keyboardType = KeyboardType.Number)
                        Spacer(Modifier.height(16.dp))
                        FormLabel("Street Address")
                        FormTextField(value = streetAddress, onValueChange = { if (!isReadOnly) streetAddress = it }, placeholder = "Enter street address")
                    } else {
                        CountryAndStatePicker(
                            selectedCountry = tempCountry,
                            selectedState = tempState,
                            onCountryChange = { if (!isReadOnly) tempCountry = it },
                            onStateChange = { if (!isReadOnly) tempState = it }
                        )
                        Spacer(Modifier.height(16.dp))
                        FormLabel("City")
                        FormTextField(value = tempCity, onValueChange = { if (!isReadOnly) tempCity = it }, placeholder = "Enter Your City")
                        Spacer(Modifier.height(16.dp))
                        FormLabel("Postal Code")
                        FormTextField(value = tempPostalCode, onValueChange = { if (!isReadOnly) tempPostalCode = it }, placeholder = "Enter postal code", keyboardType = KeyboardType.Number)
                        Spacer(Modifier.height(16.dp))
                        FormLabel("Street Address")
                        FormTextField(value = tempStreetAddress, onValueChange = { if (!isReadOnly) tempStreetAddress = it }, placeholder = "Enter street address")
                    }
                }

                // ── Government IDs ──
                // ── Government IDs ──
                AccordionSection(
                    icon = Icons.Filled.Badge,
                    title = "Government IDs",
                    expanded = expandedSection == "Government IDs",
                    onHeaderClick = { expandedSection = if (expandedSection == "Government IDs") "" else "Government IDs" }
                ) {
                    // ── PAN Number ──
                    FormLabel("PAN Number")
                    Column {
                        OutlinedTextField(
                            value = pan,
                            onValueChange = {
                                if (!isReadOnly) {
                                    val newValue = it.uppercase().take(10)
                                    pan = newValue
                                    if (newValue.length >= 10) {
                                        validatePanNumber(newValue)
                                    } else {
                                        panError = null
                                    }
                                }
                            },
                            placeholder = { Text("Enter PAN Number (e.g., ABCDE1234F)", color = Color(0xFF9CA3AF)) },
                            shape = FieldShape,
                            enabled = !isReadOnly,
                            isError = panError != null,
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = if (panError != null) Color(0xFFDC2626) else BorderColor,
                                focusedBorderColor = if (panError != null) Color(0xFFDC2626) else AccentColor,
                                errorBorderColor = Color(0xFFDC2626)
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                capitalization = KeyboardCapitalization.Characters,
                                keyboardType = KeyboardType.Text
                            )
                        )
                        if (panError != null) {
                            Text(
                                text = panError!!,
                                color = Color(0xFFDC2626),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        if (pan.isNotEmpty() && panError == null && pan.length == 10) {
                            Text(
                                text = "✓ Valid PAN number",
                                color = Color(0xFF059669),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        if (pan.isNotEmpty() && pan.length < 10) {
                            Text(
                                text = "PAN must be 10 characters (${pan.length}/10)",
                                color = Color(0xFFD97706),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // ── Aadhaar Number ──
                    FormLabel("Aadhaar Number")
                    Column {
                        OutlinedTextField(
                            value = aadhaar,
                            onValueChange = {
                                if (!isReadOnly) {
                                    val newValue = it.filter { char -> char.isDigit() }.take(12)
                                    aadhaar = newValue
                                    if (newValue.length >= 12) {
                                        validateAadhaarNumber(newValue)
                                    } else {
                                        aadhaarError = null
                                    }
                                }
                            },
                            placeholder = { Text("Enter 12-digit Aadhaar Number", color = Color(0xFF9CA3AF)) },
                            shape = FieldShape,
                            enabled = !isReadOnly,
                            isError = aadhaarError != null,
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = if (aadhaarError != null) Color(0xFFDC2626) else BorderColor,
                                focusedBorderColor = if (aadhaarError != null) Color(0xFFDC2626) else AccentColor,
                                errorBorderColor = Color(0xFFDC2626)
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            ),
                            visualTransformation = remember {
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
                        )
                        if (aadhaarError != null) {
                            Text(
                                text = aadhaarError!!,
                                color = Color(0xFFDC2626),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        if (aadhaar.isNotEmpty() && aadhaarError == null && aadhaar.length == 12) {
                            Text(
                                text = "✓ Valid Aadhaar number",
                                color = Color(0xFF059669),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        if (aadhaar.isNotEmpty() && aadhaar.length < 12) {
                            Text(
                                text = "Aadhaar must be 12 digits (${aadhaar.length}/12)",
                                color = Color(0xFFD97706),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // ── UAN Number ──
                    FormLabel("UAN Number")
                    Column {
                        OutlinedTextField(
                            value = uan,
                            onValueChange = {
                                if (!isReadOnly) {
                                    val newValue = it.filter { char -> char.isDigit() }.take(12)
                                    uan = newValue
                                    if (newValue.length >= 12) {
                                        validateUanNumber(newValue)
                                    } else {
                                        uanError = null
                                    }
                                }
                            },
                            placeholder = { Text("Enter 12-digit UAN Number", color = Color(0xFF9CA3AF)) },
                            shape = FieldShape,
                            enabled = !isReadOnly,
                            isError = uanError != null,
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = if (uanError != null) Color(0xFFDC2626) else BorderColor,
                                focusedBorderColor = if (uanError != null) Color(0xFFDC2626) else AccentColor,
                                errorBorderColor = Color(0xFFDC2626)
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            )
                        )
                        if (uanError != null) {
                            Text(
                                text = uanError!!,
                                color = Color(0xFFDC2626),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        if (uan.isNotEmpty() && uanError == null && uan.length == 12) {
                            Text(
                                text = "✓ Valid UAN number",
                                color = Color(0xFF059669),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        if (uan.isNotEmpty() && uan.length < 12) {
                            Text(
                                text = "UAN must be 12 digits (${uan.length}/12)",
                                color = Color(0xFFD97706),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(WarnBg, RoundedCornerShape(8.dp))
                            .border(1.dp, WarnBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Text(
                            "These IDs are sensitive information and will be stored securely.",
                            fontSize = 12.sp,
                            color = WarnText
                        )
                    }
                }

                // ── Education ──
                AccordionSection(
                    icon = Icons.Filled.School,
                    title = "Education",
                    expanded = expandedSection == "Education",
                    onHeaderClick = { expandedSection = if (expandedSection == "Education") "" else "Education" }
                ) {
                    if (educationList.isEmpty()) {
                        Text(
                            "No education added",
                            fontSize = 13.sp,
                            color = LabelColor,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        educationList.forEachIndexed { index, entry ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Education ${index + 1}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TitleColor)
                                if (!isReadOnly) {
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription = "Remove",
                                        tint = Color(0xFFDC2626),
                                        modifier = Modifier.size(18.dp).clickable { educationList.remove(entry) }
                                    )
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            FormLabel("Institute Name")
                            FormTextField(
                                value = entry.instituteName,
                                onValueChange = { if (!isReadOnly) educationList[educationList.indexOf(entry)] = entry.copy(instituteName = it) },
                                placeholder = "Enter Institute Name"
                            )

                            Spacer(Modifier.height(16.dp))
                            FormLabel("Degree/Diploma")
                            FormTextField(
                                value = entry.degree,
                                onValueChange = { if (!isReadOnly) educationList[educationList.indexOf(entry)] = entry.copy(degree = it) },
                                placeholder = "Enter Degree/Diploma"
                            )

                            Spacer(Modifier.height(16.dp))
                            FormLabel("Specialization")
                            FormTextField(
                                value = entry.specialization,
                                onValueChange = { if (!isReadOnly) educationList[educationList.indexOf(entry)] = entry.copy(specialization = it) },
                                placeholder = "Enter Specialization"
                            )

                            Spacer(Modifier.height(16.dp))
                            FormLabel("Completion Date")
                            DatePickerField(
                                value = entry.completionDate,
                                onDateSelected = { if (!isReadOnly) educationList[educationList.indexOf(entry)] = entry.copy(completionDate = it) }
                            )

                            if (index != educationList.lastIndex) {
                                Spacer(Modifier.height(16.dp))
                                HorizontalDivider(color = BorderColor)
                                Spacer(Modifier.height(16.dp))
                            }
                        }
                    }

                    if (!isReadOnly) {
                        Spacer(Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, AccentColor, RoundedCornerShape(8.dp))
                                .clickable { educationList.add(EducationEntry()) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("+ Add Education", color = AccentColor, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        }
                    }
                }

                // ── Experience ──
                AccordionSection(
                    icon = Icons.Filled.Work,
                    title = "Experience",
                    expanded = expandedSection == "Experience",
                    onHeaderClick = { expandedSection = if (expandedSection == "Experience") "" else "Experience" }
                ) {
                    if (experienceList.isEmpty()) {
                        Text(
                            "No experience added",
                            fontSize = 13.sp,
                            color = LabelColor,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        experienceList.forEachIndexed { index, entry ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Experience ${index + 1}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TitleColor)
                                if (!isReadOnly) {
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription = "Remove",
                                        tint = Color(0xFFDC2626),
                                        modifier = Modifier.size(18.dp).clickable { experienceList.remove(entry) }
                                    )
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            FormLabel("Company Name")
                            FormTextField(
                                value = entry.companyName,
                                onValueChange = { if (!isReadOnly) experienceList[experienceList.indexOf(entry)] = entry.copy(companyName = it) },
                                placeholder = "Enter Company Name"
                            )

                            Spacer(Modifier.height(16.dp))
                            FormLabel("Job Title")
                            FormTextField(
                                value = entry.jobTitle,
                                onValueChange = { if (!isReadOnly) experienceList[experienceList.indexOf(entry)] = entry.copy(jobTitle = it) },
                                placeholder = "Enter Job Title"
                            )

                            Spacer(Modifier.height(16.dp))
                            FormLabel("From Date")
                            DatePickerField(
                                value = entry.fromDate,
                                onDateSelected = { if (!isReadOnly) experienceList[experienceList.indexOf(entry)] = entry.copy(fromDate = it) }
                            )

                            Spacer(Modifier.height(16.dp))
                            FormLabel("To Date")
                            DatePickerField(
                                value = entry.toDate,
                                onDateSelected = { if (!isReadOnly) experienceList[experienceList.indexOf(entry)] = entry.copy(toDate = it) },
                                enabled = !entry.isCurrentRole && !isReadOnly
                            )

                            Spacer(Modifier.height(16.dp))
                            FormLabel("Job Description")
                            OutlinedTextField(
                                value = entry.jobDescription,
                                onValueChange = { if (!isReadOnly) experienceList[experienceList.indexOf(entry)] = entry.copy(jobDescription = it) },
                                placeholder = { Text("Enter Job Description", color = Color(0xFF9CA3AF)) },
                                shape = FieldShape,
                                enabled = !isReadOnly,
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = BorderColor,
                                    focusedBorderColor = AccentColor
                                ),
                                modifier = Modifier.fillMaxWidth().height(100.dp)
                            )

                            Spacer(Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = entry.isCurrentRole,
                                    onCheckedChange = { checked ->
                                        if (!isReadOnly) experienceList[experienceList.indexOf(entry)] = entry.copy(isCurrentRole = checked)
                                    },
                                    enabled = !isReadOnly,
                                    colors = CheckboxDefaults.colors(checkedColor = AccentColor)
                                )
                                Text("This experience is relevant to current role", fontSize = 13.sp, color = LabelColor)
                            }

                            if (index != experienceList.lastIndex) {
                                Spacer(Modifier.height(16.dp))
                                HorizontalDivider(color = BorderColor)
                                Spacer(Modifier.height(16.dp))
                            }
                        }
                    }

                    if (!isReadOnly) {
                        Spacer(Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, AccentColor, RoundedCornerShape(8.dp))
                                .clickable { experienceList.add(ExperienceEntry()) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("+ Add Experience", color = AccentColor, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        }
                    }
                }

                // ── Work Info ──
                AccordionSection(
                    icon = Icons.Filled.AccountBalance,
                    title = "Work Info",
                    expanded = expandedSection == "Work Info",
                    onHeaderClick = { expandedSection = if (expandedSection == "Work Info") "" else "Work Info" }
                ) {
                    FormLabel("Member ID")
                    FormTextField(value = memberId, onValueChange = { if (!isReadOnly) memberId = it }, placeholder = "Enter Member ID")

                    Spacer(Modifier.height(16.dp))
                    FormLabel("Employee Code")
                    FormTextField(value = employeeCode, onValueChange = { if (!isReadOnly) employeeCode = it }, placeholder = "Enter Employee Code")

                    Spacer(Modifier.height(16.dp))
                    FormLabel("Date of Joining")
                    DatePickerField(
                        value = doj,
                        onDateSelected = {
                            if (!isReadOnly) {
                                doj = it
                                if (currentErrorField == "Date of Joining") { currentErrorField = null; topError = null }
                            }
                        },
                        isError = currentErrorField == "Date of Joining"
                    )
                    Spacer(Modifier.height(16.dp))
                    FormDropdown(
                        label = "Branch",
                        value = branch,
                        expanded = branchExpanded && !isReadOnly,
                        onExpandChange = { if (!isReadOnly) branchExpanded = it },
                        options = branchList.mapNotNull { it.name },
                        onOptionSelected = { selectedName ->
                            if (!isReadOnly) {
                                branch = selectedName
                                selectedBranchId = branchList.find { it.name == selectedName }?.id
                            }
                        }
                    )

                    Spacer(Modifier.height(16.dp))
                    FormDropdown(
                        label = "Department",
                        value = department,
                        expanded = departmentExpanded && !isReadOnly,
                        onExpandChange = { if (!isReadOnly) departmentExpanded = it },
                        options = departmentList.map { it.name },
                        onOptionSelected = { selectedName ->
                            if (!isReadOnly) {
                                department = selectedName
                                selectedDepartmentId = departmentList.find { it.name == selectedName }?._id
                                if (currentErrorField == "Department") { currentErrorField = null; topError = null }
                            }
                        },
                        isError = currentErrorField == "Department",
                        errorMessage = if (currentErrorField == "Department") "Department is required" else null
                    )

                    Spacer(Modifier.height(16.dp))
                    FormDropdown(
                        label = "Designation",
                        value = designation,
                        expanded = designationExpanded && !isReadOnly,
                        onExpandChange = { if (!isReadOnly) designationExpanded = it },
                        options = designationList.map { it.name },
                        onOptionSelected = { selectedName ->
                            if (!isReadOnly) {
                                designation = selectedName
                                selectedDesignationId = designationList.find { it.name == selectedName }?.id
                            }
                        }
                    )

                    Spacer(Modifier.height(16.dp))
                    FormDropdown(
                        label = "Role",
                        value = role,
                        expanded = roleExpanded && !isReadOnly,
                        onExpandChange = { if (!isReadOnly) roleExpanded = it },
                        options = roles.map { it.name },
                        onOptionSelected = { selectedName ->
                            if (!isReadOnly) {
                                role = selectedName
                                selectedRoleId = roles.find { it.name == selectedName }?._id
                                if (currentErrorField == "Role") { currentErrorField = null; topError = null }
                            }
                        },
                        isError = currentErrorField == "Role",
                        errorMessage = if (currentErrorField == "Role") "Role is required" else null
                    )

                    Spacer(Modifier.height(16.dp))
                    FormDropdown(
                        label = "Shift",
                        value = shift,
                        expanded = shiftExpanded && !isReadOnly,
                        onExpandChange = { if (!isReadOnly) shiftExpanded = it },
                        options = shifts.map { it.name },
                        onOptionSelected = { selectedName ->
                            if (!isReadOnly) {
                                shift = selectedName
                                selectedShiftId = shifts.find { it.name == selectedName }?._id
                            }
                        }
                    )
                    Spacer(Modifier.height(16.dp))
                    FormDropdown(
                        label = "Employment Type",
                        value = employmentType,
                        expanded = employmentTypeExpanded && !isReadOnly,
                        onExpandChange = { if (!isReadOnly) employmentTypeExpanded = it },
                        options = listOf("Full-time", "Part-time", "Contract"),
                        onOptionSelected = { if (!isReadOnly) employmentType = it }
                    )

                    Spacer(Modifier.height(16.dp))
                    FormLabel("Work Location")
                    FormTextField(value = workLocation, onValueChange = { if (!isReadOnly) workLocation = it }, placeholder = "Enter Work Location")

                    Spacer(Modifier.height(16.dp))
                    FormDropdown(
                        label = "Reporting To",
                        value = reportingTo,
                        expanded = reportingToExpanded && !isReadOnly,
                        onExpandChange = { if (!isReadOnly) reportingToExpanded = it },
                        options = members.map { it.displayName() },
                        onOptionSelected = { selectedName ->
                            if (!isReadOnly) {
                                reportingTo = selectedName
                                selectedReportingToId = members.find { it.displayName() == selectedName }?._id
                            }
                        }
                    )
                    Spacer(Modifier.height(16.dp))
                    FormDropdown(
                        label = "Secondary Reporting To",
                        value = secondaryReportingTo,
                        expanded = secondaryReportingToExpanded && !isReadOnly,
                        onExpandChange = { if (!isReadOnly) secondaryReportingToExpanded = it },
                        options = members.map { it.displayName() },
                        onOptionSelected = { selectedName ->
                            if (!isReadOnly) {
                                secondaryReportingTo = selectedName
                                selectedSecondaryReportingToId = members.find { it.displayName() == selectedName }?._id
                            }
                        }
                    )
                }
            }
        }

        // ── Fixed bottom button — hidden entirely in VIEW mode ──
        if (mode != ScreenMode.VIEW) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Button(
                    onClick = {
                        // ── Step 1: Validate Government IDs first ──
                        var hasGovIdError = false

                        // Validate PAN if not empty
                        if (pan.isNotBlank()) {
                            val panResult = GovernmentIdValidator.validatePan(pan)
                            if (!panResult.isValid) {
                                currentErrorField = "PAN"
                                topError = panResult.message
                                expandedSection = "Government IDs"
                                hasGovIdError = true
                            }
                        }

                        // Validate Aadhaar if not empty
                        if (!hasGovIdError && aadhaar.isNotBlank()) {
                            val aadhaarResult = GovernmentIdValidator.validateAadhaar(aadhaar)
                            if (!aadhaarResult.isValid) {
                                currentErrorField = "Aadhaar"
                                topError = aadhaarResult.message
                                expandedSection = "Government IDs"
                                hasGovIdError = true
                            }
                        }

                        // Validate UAN if not empty
                        if (!hasGovIdError && uan.isNotBlank()) {
                            val uanResult = GovernmentIdValidator.validateUan(uan)
                            if (!uanResult.isValid) {
                                currentErrorField = "UAN"
                                topError = uanResult.message
                                expandedSection = "Government IDs"
                                hasGovIdError = true
                            }
                        }

                        // ── Step 2: Check required fields ──
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
                                // ── Step 3: All validations passed - submit ──
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
                                        WorkExperienceRequestItem(it.companyName, it.jobTitle, toApiDate(it.fromDate), it.jobDescription, it.isCurrentRole)
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
                                        WorkExperienceRequestItem(it.companyName, it.jobTitle, toApiDate(it.fromDate), it.jobDescription, it.isCurrentRole)
                                    }
                                )


                                val imageFile = profileImageUri?.let { uriToFile(context, it) }

                                if (mode == ScreenMode.EDIT && memberIdToLoad != null) {
                                    hrViewModel.updateMember(memberIdToLoad, updateRequest)
                                } else {
                                    hrViewModel.createMember(createRequest)
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentColor),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text(
                        text = if (mode == ScreenMode.EDIT) "Save Changes" else "Create Employee",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                }
            }
        }

        DynamicIslandError(
            modifier = Modifier.align(Alignment.TopCenter),
            message = topError,
            onDismiss = { topError = null }
        )

        DynamicIslandSuccess(                                    // ✅ NEW
            modifier = Modifier.align(Alignment.TopCenter),
            message = topSuccess,
            onDismiss = { topSuccess = null }
        )
    }

    if (showProfileOptionsDialog && !isReadOnly) {
        AlertDialog(
            onDismissRequest = { showProfileOptionsDialog = false },
            title = { Text("Profile Photo", fontWeight = FontWeight.SemiBold, color = TitleColor) },
            text = { Text("Choose an action for your profile photo", fontSize = 13.sp, color = LabelColor) },
            confirmButton = {
                TextButton(onClick = {
                    showProfileOptionsDialog = false
                    imagePickerLauncher.launch("image/*")
                }) {
                    Text("Upload New", color = AccentColor, fontWeight = FontWeight.Medium)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showProfileOptionsDialog = false
                    if (memberIdToLoad != null) {
                        hrViewModel.deleteProfilePicture(memberIdToLoad)   // ✅ backend call
                    } else {
                        // CREATE mode la member illa, local ah mattum clear pannunga
                        profileImageUri = null
                        existingProfilePictureUrl = null
                    }
                }) {
                    Text("Delete Profile", color = Color(0xFFDC2626), fontWeight = FontWeight.Medium)
                }
            },
            containerColor = Color.White
        )
    }
}

fun uriToFile(context: android.content.Context, uri: Uri): java.io.File {
    val inputStream = context.contentResolver.openInputStream(uri)
    val file = java.io.File(context.cacheDir, "profile_${System.currentTimeMillis()}.jpg")
    inputStream?.use { input ->
        file.outputStream().use { output -> input.copyTo(output) }
    }
    return file
}

// ── Accordion section (same pattern as CreateItemScreen.kt) ──
@Composable
private fun AccordionSection(
    icon: ImageVector,
    title: String,
    expanded: Boolean,
    onHeaderClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (expanded) Color(0xFFF7F7FA) else Color.White)
                .clickable { onHeaderClick() }
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = AccentColor, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TitleColor)
            }
            Icon(if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, null, tint = LabelColor)
        }
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                content()
            }
        }
        HorizontalDivider(color = BorderColor)
    }
}

// ── Permanent / Temporary segmented toggle (Address section) ──
@Composable
private fun AddressTypeToggle(selected: String, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderColor, FieldShape)
            .padding(4.dp)
    ) {
        listOf("Permanent", "Temporary").forEach { option ->
            val isSelected = selected == option
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(if (isSelected) AccentColor else Color.Transparent, RoundedCornerShape(8.dp))
                    .clickable { onSelect(option) }
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "$option Address",
                    color = if (isSelected) Color.White else LabelColor,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp
                )
            }
        }
    }
}