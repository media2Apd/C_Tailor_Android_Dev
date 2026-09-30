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
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.LocationCity
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.cuso.tailor.R
import com.cuso.tailor.adaptive_screen.AppDesignTokens
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.hr.*
import com.cuso.tailor.model.sales.Country
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.greenBg
import com.cuso.tailor.ui.theme.greentext
import com.cuso.tailor.ui.theme.redText
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
import java.util.regex.Pattern

// Shared Design Tokens
private val AccentColor = Primary
private val BorderColor = Color(0xFFE3E4E8)
private val LabelColor = Color(0xFF6B7280)
private val TitleColor = title_color
private val WarnBg = Color(0xFFFFF7E6)
private val WarnBorder = Color(0xFFFCE3B0)
private val WarnText = Color(0xFF9A6A17)

// Screen mode definition
enum class ScreenMode { CREATE, VIEW, EDIT }

data class EducationEntry(
    val id: String = UUID.randomUUID().toString(),
    val degree: String = "",
    val specialization: String = "",
    val instituteName: String = "",
    val startDate: String = "",
    val completionDate: String = "",
    val cgpa: String = ""
)

data class ExperienceEntry(
    val id: String = UUID.randomUUID().toString(),
    val companyName: String = "",
    val jobTitle: String = "",
    val employmentType: String = "",
    val location: String = "",
    val fromDate: String = "",
    val toDate: String = "",
    val jobDescription: String = "",
    val isCurrentRole: Boolean = false
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
    onNavigateToEdit: (String) -> Unit = {},
    hrViewModel: HrViewModel = hiltViewModel(),
    branchViewModel: BranchViewModel = hiltViewModel(),
    departmentViewModel: DepartmentViewModel = hiltViewModel(),
    designationViewModel: DesignationViewModel = hiltViewModel()
) {
    if (mode == ScreenMode.VIEW) {
        EmployeeProfileViewScreen(
            memberId = memberIdToLoad,
            onDismiss = onDismiss,
            onEdit = {
                memberIdToLoad?.let { onNavigateToEdit(it) }
            },
            hrViewModel = hrViewModel,
            branchViewModel = branchViewModel,
            departmentViewModel = departmentViewModel,
            designationViewModel = designationViewModel
        )
        return
    }

    val tokens: AppDesignTokens = LocalAppTokens.current

    val sectionGap = tokens.screenPadding
    val fieldGap = tokens.screenPadding * 0.75f
    val smallGap = tokens.screenPadding * 0.5f
    val adaptiveFieldShape = RoundedCornerShape(tokens.cardCornerRadius * 0.65f)
    val avatarSize = tokens.cardHeight * 0.85f

    val isReadOnly = false
    val isEditable = true

    var topSuccess by remember { mutableStateOf<String?>(null) }
    var expandedSection by remember { mutableStateOf("Personal Information") }

    // 1. Personal Information State
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var personalMail by remember { mutableStateOf("") }
    var workPhone by remember { mutableStateOf("") }
    var workPhoneCountry by remember { mutableStateOf<Country?>(null) }
    var personalPhone by remember { mutableStateOf("") }
    var personalPhoneCountry by remember { mutableStateOf<Country?>(null) }
    var dob by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var genderExpanded by remember { mutableStateOf(false) }

    // 2. Permanent & Temporary Address State
    var addressTab by remember { mutableStateOf("Permanent") }
    var countryName by remember { mutableStateOf("") }
    var countryCode by remember { mutableStateOf("") }
    var subdivisionName by remember { mutableStateOf("") }
    var subdivisionCode by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var flatNo by remember { mutableStateOf("") }
    var areaZone by remember { mutableStateOf("") }
    var pincode by remember { mutableStateOf("") }
    var streetAddress by remember { mutableStateOf("") }

    // Temporary Address
    var isSameAsPermanent by remember { mutableStateOf(false) }
    var tempCountryName by remember { mutableStateOf("") }
    var tempCountryCode by remember { mutableStateOf("") }
    var tempSubdivisionName by remember { mutableStateOf("") }
    var tempSubdivisionCode by remember { mutableStateOf("") }
    var tempCity by remember { mutableStateOf("") }
    var tempFlatNo by remember { mutableStateOf("") }
    var tempAreaZone by remember { mutableStateOf("") }
    var tempPincode by remember { mutableStateOf("") }
    var tempStreetAddress by remember { mutableStateOf("") }

    // 3. Identity & Personal Details
    var aadhaarNo by remember { mutableStateOf("") }
    var panNo by remember { mutableStateOf("") }
    var passportNo by remember { mutableStateOf("") }
    var maritalStatus by remember { mutableStateOf("") }
    var maritalExpanded by remember { mutableStateOf(false) }
    var bloodGroup by remember { mutableStateOf("") }
    var bloodGroupExpanded by remember { mutableStateOf(false) }
    var emergencyContactName by remember { mutableStateOf("") }
    var emergencyContactMobile by remember { mutableStateOf("") }
    var emergencyContactPhoneCountry by remember { mutableStateOf<Country?>(null) }

    // 4. Government & Statutory IDs
    var uanNo by remember { mutableStateOf("") }
    var esicNumber by remember { mutableStateOf("") }
    var pfAccountNo by remember { mutableStateOf("") }
    var payFrequency by remember { mutableStateOf("") }
    var payFrequencyExpanded by remember { mutableStateOf(false) }

    // 5. Bank Account Details
    var accountHolderName by remember { mutableStateOf("") }
    var accountNumber by remember { mutableStateOf("") }
    var bankName by remember { mutableStateOf("") }
    var ifscCode by remember { mutableStateOf("") }

    // 6. Dynamic Lists
    val nomineeList = remember { mutableStateListOf<NomineeEntry>() }
    val experienceList = remember { mutableStateListOf<ExperienceEntry>() }
    val educationList = remember { mutableStateListOf<EducationEntry>() }

    // 7. Job Details
    var employeeCode by remember { mutableStateOf("") }
    var doj by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var departmentExpanded by remember { mutableStateOf(false) }
    var designation by remember { mutableStateOf("") }
    var designationExpanded by remember { mutableStateOf(false) }
    var branch by remember { mutableStateOf("") }
    var branchExpanded by remember { mutableStateOf(false) }
    var shift by remember { mutableStateOf("") }
    var shiftExpanded by remember { mutableStateOf(false) }
    var reportingTo by remember { mutableStateOf("") }
    var reportingToExpanded by remember { mutableStateOf(false) }
    var secondaryReportingTo by remember { mutableStateOf("") }
    var secondaryReportingToExpanded by remember { mutableStateOf(false) }
    var workingDistrict by remember { mutableStateOf("") }
    var employmentType by remember { mutableStateOf("") }

    // 8. Access & Permissions
    var role by remember { mutableStateOf("") }
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

    var currentErrorField by remember { mutableStateOf<String?>(null) }
    var fieldErrorMessage by remember { mutableStateOf<String?>(null) }
    var topError by remember { mutableStateOf<String?>(null) }

    val memberDetail by hrViewModel.memberDetail.collectAsState()
    val createMemberState by hrViewModel.createMemberState.collectAsState()

    var selectedRoleId by remember { mutableStateOf<String?>(null) }
    var selectedShiftId by remember { mutableStateOf<String?>(null) }
    var selectedReportingToId by remember { mutableStateOf<String?>(null) }
    var selectedSecondaryReportingToId by remember { mutableStateOf<String?>(null) }

    val emailPattern = remember { Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$") }
    val ifscPattern = remember { Pattern.compile("^[A-Z]{4}0[A-Z0-9]{6}$") }

    fun toApiDate(displayDate: String): String {
        if (displayDate.isBlank()) return ""
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

    fun formatDateForDisplay(isoDate: String?): String {
        if (isoDate.isNullOrBlank()) return ""
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

    fun validateForm(): Pair<String, String>? {
        if (firstName.isBlank()) return "First Name" to "First Name is required"
        if (lastName.isBlank()) return "Last Name" to "Last Name is required"
        if (personalMail.isBlank()) return "Personal Email" to "Personal Email is required"
        if (!emailPattern.matcher(personalMail.trim()).matches()) return "Personal Email" to "Please enter a valid email address"
        if (workPhone.isBlank()) return "Work Phone" to "Work Phone is required"
        if (workPhone.length < 10) return "Work Phone" to "Work Phone must be at least 10 digits"
        if (dob.isBlank()) return "Date of Birth" to "Date of Birth is required"
        if (gender.isBlank() || gender == "Select gender") return "Gender" to "Gender is required"

        val aadhaarRes = GovernmentIdValidator.validateAadhaar(aadhaarNo)
        if (!aadhaarRes.isValid) return "Aadhaar Number" to aadhaarRes.message

        val panRes = GovernmentIdValidator.validatePan(panNo)
        if (!panRes.isValid) return "PAN Number" to panRes.message

        if (maritalStatus.isBlank() || maritalStatus == "Select status") return "Marital Status" to "Marital Status is required"
        if (bloodGroup.isBlank() || bloodGroup == "Select group") return "Blood Group" to "Blood Group is required"
        if (emergencyContactName.isBlank()) return "Emergency Contact Name" to "Emergency Contact Name is required"
        if (emergencyContactMobile.isBlank()) return "Emergency Contact Phone" to "Emergency Contact Phone is required"
        if (emergencyContactMobile.length < 10) return "Emergency Contact Phone" to "Emergency Contact Phone must be at least 10 digits"

        val uanRes = GovernmentIdValidator.validateUan(uanNo)
        if (!uanRes.isValid) return "UAN Number" to uanRes.message
        if (payFrequency.isBlank() || payFrequency == "Select frequency") return "Pay Frequency" to "Pay Frequency is required"

        if (accountHolderName.isBlank()) return "Account Holder Name" to "Account Holder Name is required"
        if (accountNumber.isBlank()) return "Account Number" to "Account Number is required"
        if (accountNumber.length !in 9..18) return "Account Number" to "Account Number must be between 9 and 18 digits"
        if (bankName.isBlank()) return "Bank Name" to "Bank Name is required"
        if (ifscCode.isBlank()) return "IFSC Code" to "IFSC Code is required"
        if (!ifscPattern.matcher(ifscCode.trim().uppercase()).matches()) return "IFSC Code" to "Invalid IFSC Code format (e.g. SBIN0001234)"

        if (doj.isBlank()) return "Date of Joining" to "Date of Joining is required"
        if (department.isBlank() || department == "Select department") return "Department" to "Department is required"
        if (workingDistrict.isBlank()) return "Work Location" to "Work Location is required"

        if (role.isBlank() || role == "Select role") return "Role" to "Role is required"

        return null
    }

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

    LaunchedEffect(memberDetail, branchList, departmentList, designationList, roles, shifts, members) {
        val m = memberDetail ?: return@LaunchedEffect

        firstName = m.firstName.orEmpty()
        lastName = m.lastName.orEmpty()
        personalMail = m.personalMail ?: m.email ?: m.userId?.email.orEmpty()
        workPhone = m.workMobile ?: m.userId?.mobile.orEmpty()
        personalPhone = m.personalMobile.orEmpty()
        dob = formatDateForDisplay(m.dob)
        gender = m.gender?.replaceFirstChar { it.uppercase() }.orEmpty()
        maritalStatus = m.martialStatus?.replaceFirstChar { it.uppercase() }.orEmpty()

        m.permanentAddress?.let { addr ->
            countryName = addr.countryName.orEmpty()
            countryCode = addr.countryCode.orEmpty()
            subdivisionName = addr.subdivisionName.orEmpty()
            subdivisionCode = addr.subdivisionCode.orEmpty()
            city = addr.city.orEmpty()
            flatNo = addr.flatNo.orEmpty()
            areaZone = addr.areaZone.orEmpty()
            pincode = addr.pincode.orEmpty()
            streetAddress = addr.street.orEmpty()
        }

        isSameAsPermanent = false
        if (m.hasTemporaryAddress && m.temporaryAddress != null) {
            addressTab = "Temporary"
            val tAddr = m.temporaryAddress
            tempCountryName = tAddr.countryName.orEmpty()
            tempCountryCode = tAddr.countryCode.orEmpty()
            tempSubdivisionName = tAddr.subdivisionName.orEmpty()
            tempSubdivisionCode = tAddr.subdivisionCode.orEmpty()
            tempCity = tAddr.city.orEmpty()
            tempFlatNo = tAddr.flatNo.orEmpty()
            tempAreaZone = tAddr.areaZone.orEmpty()
            tempPincode = tAddr.pincode.orEmpty()
            tempStreetAddress = tAddr.street.orEmpty()
        } else {
            addressTab = "Permanent"
            tempCountryName = ""
            tempCountryCode = ""
            tempSubdivisionName = ""
            tempSubdivisionCode = ""
            tempCity = ""
            tempFlatNo = ""
            tempAreaZone = ""
            tempPincode = ""
            tempStreetAddress = ""
        }

        aadhaarNo = m.aadhaarNo.orEmpty()
        panNo = m.panNo.orEmpty()
        passportNo = m.passportNo.orEmpty()
        bloodGroup = m.bloodGroup.orEmpty()
        emergencyContactName = m.emergencyContactName.orEmpty()
        emergencyContactMobile = m.emergencyContactMobile.orEmpty()

        uanNo = m.uanNo.orEmpty()
        esicNumber = m.esicNumber.orEmpty()
        pfAccountNo = m.pfAccountNo.orEmpty()
        payFrequency = m.payFrequency.orEmpty()

        accountHolderName = m.accountHolderName.orEmpty()
        accountNumber = m.accountNumber.orEmpty()
        bankName = m.bankName.orEmpty()
        ifscCode = m.ifscCode.orEmpty()

        nomineeList.clear()
        m.pfGratuityNominees.forEach { nom ->
            nomineeList.add(
                NomineeEntry(
                    id = nom._id ?: UUID.randomUUID().toString(),
                    name = nom.name.orEmpty(),
                    relationship = nom.relationship.orEmpty(),
                    sharePercent = nom.share?.toString().orEmpty()
                )
            )
        }

        experienceList.clear()
        m.workExperience.forEach { exp ->
            experienceList.add(
                ExperienceEntry(
                    id = exp._id ?: UUID.randomUUID().toString(),
                    companyName = exp.companyName.orEmpty(),
                    jobTitle = exp.jobTitle.orEmpty(),
                    employmentType = exp.employmentType.orEmpty(),
                    location = exp.location.orEmpty(),
                    fromDate = formatDateForDisplay(exp.fromDate),
                    toDate = formatDateForDisplay(exp.toDate),
                    jobDescription = exp.jobDescription.orEmpty(),
                    isCurrentRole = exp.isRelevant
                )
            )
        }

        educationList.clear()
        m.education.forEach { edu ->
            educationList.add(
                EducationEntry(
                    id = edu._id ?: UUID.randomUUID().toString(),
                    degree = edu.degree.orEmpty(),
                    specialization = edu.specialization.orEmpty(),
                    instituteName = edu.instituteName.orEmpty(),
                    startDate = formatDateForDisplay(edu.startDate),
                    completionDate = formatDateForDisplay(edu.completionDate),
                    cgpa = edu.cgpa?.toString().orEmpty()
                )
            )
        }

        employeeCode = m.memberId.orEmpty()
        doj = formatDateForDisplay(m.doj)
        workingDistrict = m.workingDistrict.orEmpty()
        employmentType = m.employmentType?.lowercase().orEmpty()

        selectedBranchId = m.branchId?._id
        branch = branchList.find { it.id == selectedBranchId }?.name ?: m.branchName ?: m.branchId?.name.orEmpty()

        selectedDepartmentId = m.departmentId?._id
        department = departmentList.find { it._id == selectedDepartmentId }?.name ?: m.departmentId?.name.orEmpty()

        val desigId = when (val d = m.designationId) {
            is String -> d
            is MemberDesignationRef -> d._id
            else -> null
        }
        selectedDesignationId = desigId
        designation = designationList.find { it.id == desigId }?.name.orEmpty()

        selectedRoleId = m.customRoleId?._id
        role = roles.find { it._id == selectedRoleId }?.name ?: m.customRoleId?.name.orEmpty()

        selectedShiftId = m.shiftId
        shift = shifts.find { it.id == selectedShiftId }?.title.orEmpty()

        selectedReportingToId = m.reportingTo
        reportingTo = members.find { it._id == selectedReportingToId }?.displayName().orEmpty()

        selectedSecondaryReportingToId = m.secondaryReportingTo
        secondaryReportingTo = members.find { it._id == selectedSecondaryReportingToId }?.displayName().orEmpty()

        existingProfilePictureUrl = m.profilePicture
    }

    LaunchedEffect(createMemberState) {
        when (val state = createMemberState) {
            is HrViewModel.CreateMemberState.Success -> {
                topError = null
                topSuccess = if (mode == ScreenMode.EDIT) "Member updated successfully" else "Employee created successfully"
                hrViewModel.resetCreateMemberState()
                kotlinx.coroutines.delay(800)
                hrViewModel.fetchMembers()
                if (mode == ScreenMode.EDIT) onUpdateEmployee() else onCreateEmployee()
                onDismiss()
            }
            is HrViewModel.CreateMemberState.Error -> {
                val msg = state.message
                if (msg.contains("updated successfully", ignoreCase = true)) {
                    topError = null
                    topSuccess = "Member updated successfully"
                    hrViewModel.resetCreateMemberState()
                    kotlinx.coroutines.delay(800)
                    hrViewModel.fetchMembers()
                    if (mode == ScreenMode.EDIT) onUpdateEmployee() else onCreateEmployee()
                    onDismiss()
                } else {
                    topSuccess = null
                    topError = msg
                    hrViewModel.resetCreateMemberState()
                }
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
                title = if (mode == ScreenMode.EDIT) "Edit Employee" else "Employee Onboarding",
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
                    .padding(bottom = tokens.buttonHeight + sectionGap * 2)
            ) {
                // 1. Personal Information
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
                    FormLabel("First Name", isRequired = true)
                    FormTextField(
                        value = firstName,
                        onValueChange = {
                            if (isEditable) {
                                firstName = it
                                if (currentErrorField == "First Name") { currentErrorField = null; topError = null }
                            }
                        },
                        placeholder = "Enter first name",
                        enabled = isEditable,
                        isError = currentErrorField == "First Name",
                        errorMessage = if (currentErrorField == "First Name") fieldErrorMessage else null
                    )

                    Spacer(Modifier.height(fieldGap))
                    FormLabel("Last Name ", isRequired = true)
                    FormTextField(
                        value = lastName,
                        onValueChange = {
                            if (isEditable) {
                                lastName = it
                                if (currentErrorField == "Last Name") { currentErrorField = null; topError = null }
                            }
                        },
                        placeholder = "Enter last name",
                        enabled = isEditable,
                        isError = currentErrorField == "Last Name",
                        errorMessage = if (currentErrorField == "Last Name") fieldErrorMessage else null
                    )

                    Spacer(Modifier.height(fieldGap))
                    FormLabel("Personal Email ", isRequired = true)
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
                        errorMessage = if (currentErrorField == "Personal Email") fieldErrorMessage else null
                    )

                    Spacer(Modifier.height(fieldGap))
                    FormLabel("Work Phone ", isRequired = true)
                    PhoneInputField(
                        phoneValue = workPhone,
                        onPhoneChange = {
                            if (isEditable) {
                                workPhone = it.filter { char -> char.isDigit() }
                                if (currentErrorField == "Work Phone") { currentErrorField = null; topError = null }
                            }
                        },
                        onCountryChange = { if (isEditable) workPhoneCountry = it },
                        enabled = isEditable,
                        isError = currentErrorField == "Work Phone",
                        errorMessage = if (currentErrorField == "Work Phone") fieldErrorMessage else null
                    )

                    Spacer(Modifier.height(fieldGap))
                    FormLabel("Personal Phone")
                    PhoneInputField(
                        phoneValue = personalPhone,
                        onPhoneChange = { if (isEditable) personalPhone = it.filter { char -> char.isDigit() } },
                        onCountryChange = { if (isEditable) personalPhoneCountry = it },
                        enabled = isEditable
                    )

                    Spacer(Modifier.height(fieldGap))
                    FormLabel("Date of Birth ", isRequired = true)
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
                    FormLabel("Gender ", isRequired = true)
                    ErrorFieldWrapper(isError = currentErrorField == "Gender") {
                        FormDropdown(
                            value = gender.ifBlank { "Select gender" },
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

                // 2. Address Details
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
                            onValueChange = { if (isEditable) pincode = it.filter { char -> char.isDigit() }.take(6) },
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
                            onValueChange = { if (isEditable) tempPincode = it.filter { char -> char.isDigit() }.take(6) },
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

                // 3. Identity & Personal Details
                AccordionSection(
                    iconPainter = painterResource(R.drawable.ic_credit),
                    title = "Identity & Personal Details",
                    expanded = expandedSection == "Identity & Personal Details",
                    onHeaderClick = { expandedSection = if (expandedSection == "Identity & Personal Details") "" else "Identity & Personal Details" }
                ) {
                    FormLabel("Aadhaar Number ", isRequired = true)
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
                                if (currentErrorField == "Aadhaar Number") { currentErrorField = null; topError = null }
                            }
                        },
                        placeholder = "XXXX-XXXX-XXXX",
                        enabled = isEditable,
                        keyboardType = KeyboardType.Number,
                        visualTransformation = aadhaarVisualTransformation,
                        isError = currentErrorField == "Aadhaar Number",
                        errorMessage = if (currentErrorField == "Aadhaar Number") fieldErrorMessage else null
                    )

                    Spacer(Modifier.height(fieldGap))
                    FormLabel("PAN Number ", isRequired = true)
                    FormTextField(
                        value = panNo,
                        onValueChange = {
                            if (isEditable) {
                                panNo = it.uppercase().take(10)
                                if (currentErrorField == "PAN Number") { currentErrorField = null; topError = null }
                            }
                        },
                        placeholder = "ABCDE1234F",
                        enabled = isEditable,
                        keyboardType = KeyboardType.Text,
                        keyboardCapitalization = KeyboardCapitalization.Characters,
                        isError = currentErrorField == "PAN Number",
                        errorMessage = if (currentErrorField == "PAN Number") fieldErrorMessage else null
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
                    FormLabel("Marital Status ", isRequired = true)
                    ErrorFieldWrapper(isError = currentErrorField == "Marital Status") {
                        FormDropdown(
                            value = maritalStatus.ifBlank { "Select status" },
                            expanded = maritalExpanded && isEditable,
                            onExpandChange = { if (isEditable) maritalExpanded = it },
                            options = listOf("Single", "Married", "Divorced", "Widowed"),
                            onOptionSelected = {
                                if (isEditable) {
                                    maritalStatus = it
                                    if (currentErrorField == "Marital Status") { currentErrorField = null; topError = null }
                                }
                            }
                        )
                    }

                    Spacer(Modifier.height(fieldGap))
                    FormLabel("Blood Group ", isRequired = true)
                    ErrorFieldWrapper(isError = currentErrorField == "Blood Group") {
                        FormDropdown(
                            value = bloodGroup.ifBlank { "Select group" },
                            expanded = bloodGroupExpanded && isEditable,
                            onExpandChange = { if (isEditable) bloodGroupExpanded = it },
                            options = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"),
                            onOptionSelected = {
                                if (isEditable) {
                                    bloodGroup = it
                                    if (currentErrorField == "Blood Group") { currentErrorField = null; topError = null }
                                }
                            }
                        )
                    }

                    Spacer(Modifier.height(fieldGap))
                    FormLabel("Emergency Contact Name ", isRequired = true)
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
                        errorMessage = if (currentErrorField == "Emergency Contact Name") fieldErrorMessage else null
                    )

                    Spacer(Modifier.height(fieldGap))
                    FormLabel("Emergency Contact Phone ", isRequired = true)
                    PhoneInputField(
                        phoneValue = emergencyContactMobile,
                        onPhoneChange = {
                            if (isEditable) {
                                emergencyContactMobile = it.filter { char -> char.isDigit() }
                                if (currentErrorField == "Emergency Contact Phone") { currentErrorField = null; topError = null }
                            }
                        },
                        onCountryChange = { if (isEditable) emergencyContactPhoneCountry = it },
                        enabled = isEditable,
                        isError = currentErrorField == "Emergency Contact Phone",
                        errorMessage = if (currentErrorField == "Emergency Contact Phone") fieldErrorMessage else null
                    )
                }

                // 4. Government & Statutory IDs
                AccordionSection(
                    iconPainter = painterResource(R.drawable.ic_building),
                    title = "Government & Statutory IDs",
                    expanded = expandedSection == "Government & Statutory IDs",
                    onHeaderClick = { expandedSection = if (expandedSection == "Government & Statutory IDs") "" else "Government & Statutory IDs" }
                ) {
                    FormLabel("UAN Number (EPF) ", isRequired = true)
                    FormTextField(
                        value = uanNo,
                        onValueChange = {
                            if (isEditable) {
                                uanNo = it.filter { char -> char.isDigit() }.take(12)
                                if (currentErrorField == "UAN Number") { currentErrorField = null; topError = null }
                            }
                        },
                        placeholder = "Universal Account Number",
                        enabled = isEditable,
                        keyboardType = KeyboardType.Number,
                        isError = currentErrorField == "UAN Number",
                        errorMessage = if (currentErrorField == "UAN Number") fieldErrorMessage else null
                    )

                    Spacer(Modifier.height(fieldGap))
                    FormLabel("ESIC Number")
                    FormTextField(
                        value = esicNumber,
                        onValueChange = { if (isEditable) esicNumber = it.filter { char -> char.isDigit() } },
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
                    FormLabel("Pay Frequency ", isRequired = true)
                    ErrorFieldWrapper(isError = currentErrorField == "Pay Frequency") {
                        FormDropdown(
                            value = payFrequency.ifBlank { "Select frequency" },
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
                }

                // 5. Bank Account Details
                AccordionSection(
                    iconPainter = painterResource(R.drawable.ic_credit),
                    title = "Bank Account Details",
                    expanded = expandedSection == "Bank Account Details",
                    onHeaderClick = { expandedSection = if (expandedSection == "Bank Account Details") "" else "Bank Account Details" }
                ) {
                    FormLabel("Account Holder Name ", isRequired = true)
                    FormTextField(
                        value = accountHolderName,
                        onValueChange = {
                            if (isEditable) {
                                accountHolderName = it
                                if (currentErrorField == "Account Holder Name") { currentErrorField = null; topError = null }
                            }
                        },
                        placeholder = "Enter account holder name",
                        enabled = isEditable,
                        isError = currentErrorField == "Account Holder Name",
                        errorMessage = if (currentErrorField == "Account Holder Name") fieldErrorMessage else null
                    )

                    Spacer(Modifier.height(fieldGap))
                    FormLabel("Account Number ", isRequired = true)
                    FormTextField(
                        value = accountNumber,
                        onValueChange = {
                            if (isEditable) {
                                accountNumber = it.filter { char -> char.isDigit() }.take(18)
                                if (currentErrorField == "Account Number") { currentErrorField = null; topError = null }
                            }
                        },
                        placeholder = "Enter account number (9-18 digits)",
                        enabled = isEditable,
                        keyboardType = KeyboardType.Number,
                        isError = currentErrorField == "Account Number",
                        errorMessage = if (currentErrorField == "Account Number") fieldErrorMessage else null
                    )

                    Spacer(Modifier.height(fieldGap))
                    FormLabel("Bank Name ", isRequired = true)
                    FormTextField(
                        value = bankName,
                        onValueChange = {
                            if (isEditable) {
                                bankName = it
                                if (currentErrorField == "Bank Name") { currentErrorField = null; topError = null }
                            }
                        },
                        placeholder = "Enter bank name",
                        enabled = isEditable,
                        isError = currentErrorField == "Bank Name",
                        errorMessage = if (currentErrorField == "Bank Name") fieldErrorMessage else null
                    )

                    Spacer(Modifier.height(fieldGap))
                    FormLabel("IFSC Code ", isRequired = true)
                    FormTextField(
                        value = ifscCode,
                        onValueChange = {
                            if (isEditable) {
                                ifscCode = it.uppercase().take(11)
                                if (currentErrorField == "IFSC Code") { currentErrorField = null; topError = null }
                            }
                        },
                        placeholder = "e.g. SBIN0001234",
                        enabled = isEditable,
                        keyboardCapitalization = KeyboardCapitalization.Characters,
                        isError = currentErrorField == "IFSC Code",
                        errorMessage = if (currentErrorField == "IFSC Code") fieldErrorMessage else null
                    )
                }

                // 6. PF / Gratuity Nominees
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
                                Text("Nominee ${index + 1}", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium, color = TitleColor)
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
                            FormLabel("Name ", isRequired = true)
                            FormTextField(
                                value = entry.name,
                                onValueChange = { newValue ->
                                    if (isEditable) {
                                        val idx = nomineeList.indexOfFirst { it.id == entry.id }
                                        if (idx != -1) nomineeList[idx] = nomineeList[idx].copy(name = newValue)
                                    }
                                },
                                placeholder = "Full name",
                                enabled = isEditable
                            )

                            Spacer(Modifier.height(fieldGap))
                            FormLabel("Relationship ", isRequired = true)
                            FormTextField(
                                value = entry.relationship,
                                onValueChange = { newValue ->
                                    if (isEditable) {
                                        val idx = nomineeList.indexOfFirst { it.id == entry.id }
                                        if (idx != -1) nomineeList[idx] = nomineeList[idx].copy(relationship = newValue)
                                    }
                                },
                                placeholder = "e.g. Father, Mother, Spouse",
                                enabled = isEditable
                            )

                            Spacer(Modifier.height(fieldGap))
                            FormLabel("Share % ", isRequired = true)
                            FormTextField(
                                value = entry.sharePercent,
                                onValueChange = { newValue ->
                                    if (isEditable) {
                                        val idx = nomineeList.indexOfFirst { it.id == entry.id }
                                        if (idx != -1) nomineeList[idx] = nomineeList[idx].copy(sharePercent = newValue)
                                    }
                                },
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

                // 7. Work Experience
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
                                Text("Experience ${index + 1}", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium, color = TitleColor)
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
                            FormLabel("Company Name ", isRequired = true)
                            FormTextField(
                                value = entry.companyName,
                                onValueChange = { newValue ->
                                    if (isEditable) {
                                        val idx = experienceList.indexOfFirst { it.id == entry.id }
                                        if (idx != -1) experienceList[idx] = experienceList[idx].copy(companyName = newValue)
                                    }
                                },
                                placeholder = "e.g. Google",
                                enabled = isEditable
                            )

                            Spacer(Modifier.height(fieldGap))
                            FormLabel("Job Title ", isRequired = true)
                            FormTextField(
                                value = entry.jobTitle,
                                onValueChange = { newValue ->
                                    if (isEditable) {
                                        val idx = experienceList.indexOfFirst { it.id == entry.id }
                                        if (idx != -1) experienceList[idx] = experienceList[idx].copy(jobTitle = newValue)
                                    }
                                },
                                placeholder = "e.g. Senior Sales Executive",
                                enabled = isEditable
                            )

                            Spacer(Modifier.height(fieldGap))
                            FormLabel("Employment Type")
                            FormTextField(
                                value = entry.employmentType,
                                onValueChange = { newValue ->
                                    if (isEditable) {
                                        val idx = experienceList.indexOfFirst { it.id == entry.id }
                                        if (idx != -1) experienceList[idx] = experienceList[idx].copy(employmentType = newValue)
                                    }
                                },
                                placeholder = "e.g. Full Time",
                                enabled = isEditable
                            )

                            Spacer(Modifier.height(fieldGap))
                            FormLabel("Location")
                            FormTextField(
                                value = entry.location,
                                onValueChange = { newValue ->
                                    if (isEditable) {
                                        val idx = experienceList.indexOfFirst { it.id == entry.id }
                                        if (idx != -1) experienceList[idx] = experienceList[idx].copy(location = newValue)
                                    }
                                },
                                placeholder = "City, Country",
                                enabled = isEditable
                            )

                            Spacer(Modifier.height(fieldGap))
                            FormLabel("Start Date")
                            DatePickerField(
                                value = entry.fromDate,
                                onDateSelected = { newValue ->
                                    if (isEditable) {
                                        val idx = experienceList.indexOfFirst { it.id == entry.id }
                                        if (idx != -1) experienceList[idx] = experienceList[idx].copy(fromDate = newValue)
                                    }
                                },
                                enabled = isEditable
                            )

                            Spacer(Modifier.height(fieldGap))
                            FormLabel("End Date")
                            DatePickerField(
                                value = entry.toDate,
                                onDateSelected = { newValue ->
                                    if (isEditable) {
                                        val idx = experienceList.indexOfFirst { it.id == entry.id }
                                        if (idx != -1) experienceList[idx] = experienceList[idx].copy(toDate = newValue)
                                    }
                                },
                                enabled = !entry.isCurrentRole && isEditable
                            )

                            Spacer(Modifier.height(smallGap * 0.8f))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AppCheckbox(
                                    checked = entry.isCurrentRole,
                                    onCheckedChange = { checked ->
                                        if (isEditable) {
                                            val idx = experienceList.indexOfFirst { it.id == entry.id }
                                            if (idx != -1) experienceList[idx] = experienceList[idx].copy(isCurrentRole = checked)
                                        }
                                    },
                                    enabled = isEditable
                                )
                                Text("Currently Working", fontSize = tokens.bodySmall, color = if (isEditable) LabelColor else LabelColor.copy(alpha = 0.6f))
                            }

                            Spacer(Modifier.height(fieldGap))
                            FormLabel("Description")
                            OutlinedTextField(
                                value = entry.jobDescription,
                                onValueChange = { newValue ->
                                    if (isEditable) {
                                        val idx = experienceList.indexOfFirst { it.id == entry.id }
                                        if (idx != -1) experienceList[idx] = experienceList[idx].copy(jobDescription = newValue)
                                    }
                                },
                                placeholder = { Text("Key responsibilities...", fontSize = tokens.bodyMedium, color = Color(0xFF9CA3AF)) },
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

                // 8. Education Details
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
                                Text("Education ${index + 1}", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium, color = TitleColor)
                                if (isEditable) {
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription = "Remove",
                                        tint = redText,
                                        modifier = Modifier.size(tokens.iconSize).clickable { educationList.remove(entry) }
                                    )
                                }
                            }
                            Spacer(Modifier.height(smallGap))
                            FormLabel("Degree ", isRequired = true)
                            FormTextField(
                                value = entry.degree,
                                onValueChange = { newValue ->
                                    if (isEditable) {
                                        val idx = educationList.indexOfFirst { it.id == entry.id }
                                        if (idx != -1) educationList[idx] = educationList[idx].copy(degree = newValue)
                                    }
                                },
                                placeholder = "e.g. BCA / B.E.",
                                enabled = isEditable
                            )

                            Spacer(Modifier.height(fieldGap))
                            FormLabel("Specialization / Field of Study")
                            FormTextField(
                                value = entry.specialization,
                                onValueChange = { newValue ->
                                    if (isEditable) {
                                        val idx = educationList.indexOfFirst { it.id == entry.id }
                                        if (idx != -1) educationList[idx] = educationList[idx].copy(specialization = newValue)
                                    }
                                },
                                placeholder = "e.g. Computer Science",
                                enabled = isEditable
                            )

                            Spacer(Modifier.height(fieldGap))
                            FormLabel("Institution Name ", isRequired = true)
                            FormTextField(
                                value = entry.instituteName,
                                onValueChange = { newValue ->
                                    if (isEditable) {
                                        val idx = educationList.indexOfFirst { it.id == entry.id }
                                        if (idx != -1) educationList[idx] = educationList[idx].copy(instituteName = newValue)
                                    }
                                },
                                placeholder = "University Name",
                                enabled = isEditable
                            )

                            Spacer(Modifier.height(fieldGap))
                            FormLabel("Start Date")
                            DatePickerField(
                                value = entry.startDate,
                                onDateSelected = { newValue ->
                                    if (isEditable) {
                                        val idx = educationList.indexOfFirst { it.id == entry.id }
                                        if (idx != -1) educationList[idx] = educationList[idx].copy(startDate = newValue)
                                    }
                                },
                                enabled = isEditable
                            )

                            Spacer(Modifier.height(fieldGap))
                            FormLabel("Completion Date")
                            DatePickerField(
                                value = entry.completionDate,
                                onDateSelected = { newValue ->
                                    if (isEditable) {
                                        val idx = educationList.indexOfFirst { it.id == entry.id }
                                        if (idx != -1) educationList[idx] = educationList[idx].copy(completionDate = newValue)
                                    }
                                },
                                enabled = isEditable
                            )

                            Spacer(Modifier.height(fieldGap))
                            FormLabel("CGPA")
                            FormTextField(
                                value = entry.cgpa,
                                onValueChange = { newValue ->
                                    if (isEditable) {
                                        val idx = educationList.indexOfFirst { it.id == entry.id }
                                        if (idx != -1) educationList[idx] = educationList[idx].copy(cgpa = newValue)
                                    }
                                },
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

                // 9. Job Details
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
                    FormLabel("Joining Date ", isRequired = true)
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
                    FormLabel("Department ", isRequired = true)
                    ErrorFieldWrapper(isError = currentErrorField == "Department") {
                        FormDropdown(
                            value = department.ifBlank { "Select department" },
                            expanded = departmentExpanded && isEditable,
                            onExpandChange = { if (isEditable) departmentExpanded = it },
                            options = departmentList.map { it.name },
                            onOptionSelected = { selectedName ->
                                if (isEditable) {
                                    department = selectedName
                                    selectedDepartmentId = departmentList.find { it.name == selectedName }?._id
                                    if (currentErrorField == "Department") { currentErrorField = null; topError = null }
                                }
                            }
                        )
                    }

                    Spacer(Modifier.height(fieldGap))
                    FormDropdown(
                        label = "Designation",
                        value = designation.ifBlank { "Select designation" },
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
                        value = branch.ifBlank { "Select branch" },
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
                        value = shift.ifBlank { "Select shift" },
                        expanded = shiftExpanded && isEditable,
                        onExpandChange = { if (isEditable) shiftExpanded = it },
                        options = shifts.map { it.title },
                        onOptionSelected = { selectedName ->
                            if (isEditable) {
                                shift = selectedName
                                selectedShiftId = shifts.find { it.title == selectedName }?.id
                            }
                        }
                    )

                    Spacer(Modifier.height(fieldGap))
                    FormDropdown(
                        label = "Reporting Manager",
                        value = reportingTo.ifBlank { "Select manager" },
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
                        value = secondaryReportingTo.ifBlank { "Select secondary manager" },
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
                    FormLabel("Work Location (District) ", isRequired = true)
                    FormTextField(
                        value = workingDistrict,
                        onValueChange = {
                            if (isEditable) {
                                workingDistrict = it
                                if (currentErrorField == "Work Location") { currentErrorField = null; topError = null }
                            }
                        },
                        placeholder = "Enter work location (e.g. Chennai)",
                        enabled = isEditable,
                        isError = currentErrorField == "Work Location",
                        errorMessage = if (currentErrorField == "Work Location") fieldErrorMessage else null
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

                // 10. Access & Permissions
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
                    FormLabel("Assign Role *")
                    ErrorFieldWrapper(isError = currentErrorField == "Role") {
                        FormDropdown(
                            value = role.ifBlank { "Select role" },
                            expanded = roleExpanded && isEditable,
                            onExpandChange = { if (isEditable) roleExpanded = it },
                            options = roles.map { it.name },
                            onOptionSelected = { selectedName ->
                                if (isEditable) {
                                    role = selectedName
                                    selectedRoleId = roles.find { it.name == selectedName }?._id
                                    if (currentErrorField == "Role") { currentErrorField = null; topError = null }
                                }
                            }
                        )
                    }
                }
            }
        }

        // Floating Action Button
        ExtendedFloatingActionButton(
            onClick = {
                val validationError = validateForm()
                if (validationError != null) {
                    val (field, message) = validationError
                    currentErrorField = field
                    fieldErrorMessage = message
                    topError = message

                    expandedSection = when (field) {
                        "First Name", "Last Name", "Personal Email", "Work Phone", "Date of Birth", "Gender" -> "Personal Information"
                        "Aadhaar Number", "PAN Number", "Marital Status", "Blood Group", "Emergency Contact Name", "Emergency Contact Phone" -> "Identity & Personal Details"
                        "UAN Number", "Pay Frequency" -> "Government & Statutory IDs"
                        "Account Holder Name", "Account Number", "Bank Name", "IFSC Code" -> "Bank Account Details"
                        "Date of Joining", "Department", "Work Location" -> "Job Details"
                        "Role" -> "Access & Permissions"
                        else -> expandedSection
                    }
                } else {
                    currentErrorField = null
                    fieldErrorMessage = null
                    topError = null

                    val createRequest = CreateMemberRequest(
                        firstName = firstName.trim(),
                        lastName = lastName.trim(),
                        personalMail = personalMail.trim(),
                        workMobile = workPhone.trim(),
                        aadhaarNo = aadhaarNo.trim(),
                        panNo = panNo.trim().uppercase(),
                        passportNo = passportNo.trim().uppercase(),
                        bloodGroup = bloodGroup.trim(),
                        dob = toApiDate(dob),
                        doj = toApiDate(doj),
                        gender = gender.lowercase().trim(),
                        martialStatus = maritalStatus.lowercase().trim(),
                        emergencyContactName = emergencyContactName.trim(),
                        emergencyContactMobile = emergencyContactMobile.trim(),
                        uanNo = uanNo.trim(),
                        esicNumber = esicNumber.trim(),
                        pfAccountNo = pfAccountNo.trim(),
                        payFrequency = payFrequency.trim(),
                        employmentType = employmentType.ifBlank { "full-time" }.trim(),
                        workingDistrict = workingDistrict.trim(),
                        branchId = selectedBranchId,
                        branchName = branchList.find { it.id == selectedBranchId }?.name ?: branch.takeIf { it.isNotBlank() },
                        departmentId = selectedDepartmentId,
                        designationId = selectedDesignationId,
                        customRoleId = selectedRoleId,
                        shiftId = selectedShiftId,
                        reportingTo = selectedReportingToId,
                        secondaryReportingTo = selectedSecondaryReportingToId,
                        accountHolderName = accountHolderName.trim(),
                        accountNumber = accountNumber.trim(),
                        bankName = bankName.trim(),
                        ifscCode = ifscCode.trim().uppercase(),
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
                        firstName = firstName.trim(),
                        lastName = lastName.trim(),
                        personalMail = personalMail.trim(),
                        workMobile = workPhone.trim(),
                        aadhaarNo = aadhaarNo.trim(),
                        panNo = panNo.trim().uppercase(),
                        passportNo = passportNo.trim().uppercase(),
                        bloodGroup = bloodGroup.trim(),
                        dob = dob.toIsoDate(),
                        doj = doj.toIsoDate(),
                        gender = gender.lowercase().trim(),
                        martialStatus = maritalStatus.lowercase().trim(),
                        emergencyContactName = emergencyContactName.trim(),
                        emergencyContactMobile = emergencyContactMobile.trim(),
                        uanNo = uanNo.trim(),
                        esicNumber = esicNumber.trim(),
                        pfAccountNo = pfAccountNo.trim(),
                        payFrequency = payFrequency.trim(),
                        employmentType = employmentType.ifBlank { "full-time" }.trim(),
                        workingDistrict = workingDistrict.trim(),
                        branchId = selectedBranchId,
                        branchName = branchList.find { it.id == selectedBranchId }?.name ?: branch.takeIf { it.isNotBlank() },
                        departmentId = selectedDepartmentId,
                        designationId = selectedDesignationId,
                        customRoleId = selectedRoleId,
                        shiftId = selectedShiftId,
                        reportingTo = selectedReportingToId,
                        secondaryReportingTo = selectedSecondaryReportingToId,
                        accountHolderName = accountHolderName.trim(),
                        accountNumber = accountNumber.trim(),
                        bankName = bankName.trim(),
                        ifscCode = ifscCode.trim().uppercase(),
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
                fontWeight = FontWeight.Medium,
                fontSize = tokens.bodyMedium,
                color = whiteBg
            )
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
            title = { Text("Profile Photo", fontWeight = FontWeight.Medium, fontSize = tokens.bodyMedium, color = TitleColor) },
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
                    Text("Delete Profile", color = redText, fontWeight = FontWeight.Medium, fontSize = tokens.bodyMedium)
                }
            },
            containerColor = whiteBg
        )
    }
}

// ─────────────────────────────────────────────────────────────
// Profile View Mode Screen (Zero Hardcoded Values - Fully Driven by API)
// ─────────────────────────────────────────────────────────────
@Composable
fun EmployeeProfileViewScreen(
    memberId: String?,
    onDismiss: () -> Unit = {},
    onEdit: () -> Unit = {},
    hrViewModel: HrViewModel = hiltViewModel(),
    branchViewModel: BranchViewModel = hiltViewModel(),
    departmentViewModel: DepartmentViewModel = hiltViewModel(),
    designationViewModel: DesignationViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current
    val memberDetail by hrViewModel.memberDetail.collectAsState()
    val departmentUiState by departmentViewModel.uiState.collectAsState()
    val designationUiState by designationViewModel.uiState.collectAsState()

    val departmentList = (departmentUiState as? DepartmentUiState.Success)?.departments ?: emptyList()
    val designationList = (designationUiState as? DesignationUiState.Success)?.items ?: emptyList()

    LaunchedEffect(memberId) {
        if (!memberId.isNullOrBlank()) {
            hrViewModel.fetchMemberDetail(memberId)
        }
        departmentViewModel.loadDepartments()
        designationViewModel.loadDesignations()
    }

    val m = memberDetail

    val fullName = remember(m) {
        listOfNotNull(m?.firstName?.takeIf { it.isNotBlank() }, m?.lastName?.takeIf { it.isNotBlank() })
            .joinToString(" ")
    }

    // Dynamic resolution matching the API payload structure
    val designationName = remember(m, designationList) {
        val desigId = when (val d = m?.designationId) {
            is String -> d
            is MemberDesignationRef -> d._id
            else -> null
        }
        val foundName = designationList.find { it.id == desigId }?.name
        when {
            !foundName.isNullOrBlank() -> foundName
            m?.designationId is MemberDesignationRef -> m.designationId.name.orEmpty()
            else -> ""
        }
    }

    val departmentName = remember(m, departmentList) {
        val fromList = departmentList.find { it._id == m?.departmentId?._id }?.name
        when {
            !fromList.isNullOrBlank() -> fromList
            !m?.departmentId?.name.isNullOrBlank() -> m.departmentId.name
            else -> ""
        }
    }

    // 1. API data-vil irundhu valid URL ulla documents mattum filter seiyyum logic:
    val validDocumentList = remember(memberDetail) {
        val list = mutableListOf<Pair<String, String>>()
        if (!memberDetail?.profilePicture.isNullOrBlank()) {
            list.add("Profile_Picture.jpg" to memberDetail!!.profilePicture!!) // <-- 'list.add' use pannunga
        }
        list
    }

    val fullAddress = remember(m) {
        val addr = m?.permanentAddress
        if (addr != null) {
            listOfNotNull(
                addr.flatNo?.takeIf { it.isNotBlank() },
                addr.street?.takeIf { it.isNotBlank() },
                addr.areaZone?.takeIf { it.isNotBlank() },
                addr.city?.takeIf { it.isNotBlank() },
                addr.subdivisionName?.takeIf { it.isNotBlank() },
                addr.countryName?.takeIf { it.isNotBlank() },
                addr.pincode?.takeIf { it.isNotBlank() }
            ).joinToString(", ")
        } else ""
    }

    fun formatDisplayDate(dateStr: String?): String {
        if (dateStr.isNullOrBlank()) return ""
        return try {
            val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            val output = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            output.format(input.parse(dateStr)!!)
        } catch (_: Exception) {
            try {
                val input = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val output = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                output.format(input.parse(dateStr)!!)
            } catch (_: Exception) {
                dateStr
            }
        }
    }

    fun formatYearOnly(dateStr: String?): String {
        if (dateStr.isNullOrBlank()) return ""
        return try {
            val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            val output = SimpleDateFormat("yyyy", Locale.getDefault())
            output.format(input.parse(dateStr)!!)
        } catch (_: Exception) {
            try {
                val input = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val output = SimpleDateFormat("yyyy", Locale.getDefault())
                output.format(input.parse(dateStr)!!)
            } catch (_: Exception) {
                dateStr.take(4)
            }
        }
    }

    fun maskAadhaar(number: String?): String {
        if (number.isNullOrBlank()) return ""
        val digits = number.filter { it.isDigit() }
        return if (digits.length >= 4) {
            "**** **** " + digits.takeLast(4)
        } else {
            digits
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0,0,0,0),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                TitleBar(title = "Employee Profile", onClose = onDismiss)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = tokens.screenPadding, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(tokens.screenPadding)
        ) {
            // Profile Main Card
            Card(
                shape = RoundedCornerShape(tokens.cardCornerRadius),
                colors = CardDefaults.cardColors(containerColor = whiteBg),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(tokens.screenPadding),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile Image with Active Dot
                    Box(
                        modifier = Modifier.size(80.dp),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        if (!m?.profilePicture.isNullOrBlank()) {
                            AsyncImage(
                                model = m.profilePicture,
                                contentDescription = fullName,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            val initials = remember(fullName) {
                                fullName.split(" ").filter { it.isNotBlank() }
                                    .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                                    .take(2).joinToString("")
                                    .ifBlank { "?" }
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(BorderColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = initials,
                                    fontSize = tokens.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = TitleColor
                                )
                            }
                        }

                        // Online / Status Badge Indicator (Render only when status is available)
                        val status = m?.status?.takeIf { it.isNotBlank() }
                        if (status != null) {
                            val isCurrentlyActive = status.equals("active", ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(if (isCurrentlyActive) greentext else redText)
                                    .border(2.dp, whiteBg, CircleShape)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Name and Status Chip
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (fullName.isNotBlank()) {
                            Text(
                                text = fullName,
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = TitleColor
                            )
                        }
                        val status = m?.status?.takeIf { it.isNotBlank() }
                        if (status != null) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = greenBg
                            ) {
                                Text(
                                    text = status.replaceFirstChar { it.uppercase() },
                                    color = greentext,
                                    fontSize = tokens.caption,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 0.dp)
                                )
                            }
                        }
                    }

                    // Designation & Department
                    val subTitle = listOfNotNull(
                        designationName.takeIf { it.isNotBlank() },
                        departmentName.takeIf { it.isNotBlank() }
                    ).joinToString(" • ")

                    if (subTitle.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = subTitle,
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = AccentColor
                        )
                    }

                    val email = m?.personalMail ?: m?.email ?: m?.userId?.email.orEmpty()
                    val phone = m?.workMobile ?: m?.personalMobile ?: m?.userId?.mobile.orEmpty()
                    val loc = listOfNotNull(
                        m?.permanentAddress?.city?.takeIf { it.isNotBlank() },
                        m?.permanentAddress?.subdivisionName?.takeIf { it.isNotBlank() }
                            ?: m?.permanentAddress?.countryName?.takeIf { it.isNotBlank() }
                    ).joinToString(", ")
                    val joined = formatDisplayDate(m?.doj)

                    val hasMetaInfo = email.isNotBlank() || phone.isNotBlank() || loc.isNotBlank() || joined.isNotBlank()

                    if (hasMetaInfo) {
                        Spacer(modifier = Modifier.height(tokens.screenPadding * 0.75f))
                        HorizontalDivider(color = BorderColor)
                        Spacer(modifier = Modifier.height(tokens.screenPadding * 0.75f))

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (email.isNotBlank()) {
                                ProfileDetailRow(
                                    icon = Icons.Outlined.Mail,
                                    text = email,
                                    tokens = tokens
                                )
                            }
                            if (phone.isNotBlank()) {
                                ProfileDetailRow(
                                    icon = Icons.Outlined.Phone,
                                    text = phone,
                                    tokens = tokens
                                )
                            }
                            if (loc.isNotBlank()) {
                                ProfileDetailRow(
                                    icon = Icons.Outlined.LocationOn,
                                    text = loc,
                                    tokens = tokens
                                )
                            }
                            if (joined.isNotBlank()) {
                                ProfileDetailRow(
                                    icon = Icons.Outlined.DateRange,
                                    text = "Joined: $joined",
                                    tokens = tokens
                                )
                            }
                        }
                    }
                }
            }

            // Quick Actions: Edit and Generate Docs Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(tokens.screenPadding * 0.5f)
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f).height(tokens.buttonHeight),
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.65f),
                    border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(AccentColor)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentColor)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Edit Employee",
                        modifier = Modifier.size(tokens.iconSize)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Edit Employee",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }

                OutlinedButton(
                    onClick = {},
                    modifier = Modifier.weight(1f).height(tokens.buttonHeight),
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.65f),
                    border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(BorderColor)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TitleColor)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Description,
                        contentDescription = "Generate Docs",
                        modifier = Modifier.size(tokens.iconSize),
                        tint = LabelColor
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Generate Docs",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = LabelColor
                    )
                }
            }

            // Personal Information Card
            val email = m?.personalMail ?: m?.email ?: m?.userId?.email.orEmpty()
            val phone = m?.workMobile ?: m?.personalMobile ?: m?.userId?.mobile.orEmpty()
            val formattedDob = formatDisplayDate(m?.dob)
            val hasPersonalInfo = !m?.firstName.isNullOrBlank() || !m?.lastName.isNullOrBlank() || email.isNotBlank() || phone.isNotBlank() || formattedDob.isNotBlank() || !m?.gender.isNullOrBlank() || fullAddress.isNotBlank()

            if (hasPersonalInfo) {
                ProfileInfoCard(
                    icon = R.drawable.person,
                    title = "Personal Information",
                    tokens = tokens
                ) {
                    ProfileGrid(tokens = tokens) {
                        if (!m?.firstName.isNullOrBlank()) {
                            ProfileGridItem(label = "First Name", value = m.firstName, tokens = tokens)
                        }
                        if (!m?.lastName.isNullOrBlank()) {
                            ProfileGridItem(label = "Last Name", value = m.lastName, tokens = tokens)
                        }
                        if (email.isNotBlank()) {
                            ProfileGridItem(label = "Email Address", value = email, tokens = tokens)
                        }
                        if (phone.isNotBlank()) {
                            ProfileGridItem(label = "Phone Number", value = phone, tokens = tokens)
                        }
                        if (formattedDob.isNotBlank()) {
                            ProfileGridItem(label = "Date of Birth", value = formattedDob, tokens = tokens)
                        }
                        if (!m?.gender.isNullOrBlank()) {
                            ProfileGridItem(label = "Gender", value = m.gender.replaceFirstChar { it.uppercase() }, tokens = tokens)
                        }
                    }

                    if (fullAddress.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Residential Address",
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Medium,
                            color = LabelColor
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = fullAddress,
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = TitleColor
                        )
                    }
                }
            }

            // Job Details Card
            val employmentType = m?.employmentType?.takeIf { it.isNotBlank() }
            val workLocation = m?.workingDistrict?.takeIf { it.isNotBlank() }
            val hasJobDetails = departmentName.isNotBlank() || designationName.isNotBlank() || employmentType != null || workLocation != null

            if (hasJobDetails) {
                ProfileInfoCard(
                    icon = R.drawable.ic_breifcase,
                    title = "Job Details",
                    tokens = tokens
                ) {
                    ProfileGrid(tokens = tokens) {
                        if (departmentName.isNotBlank()) {
                            ProfileGridItem(label = "Department", value = departmentName, tokens = tokens)
                        }
                        if (designationName.isNotBlank()) {
                            ProfileGridItem(label = "Designation", value = designationName, tokens = tokens)
                        }
                        if (employmentType != null) {
                            ProfileGridItem(
                                label = "Employment Type",
                                value = employmentType.replace("-", " ").split(" ")
                                    .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } },
                                tokens = tokens
                            )
                        }
                        if (workLocation != null) {
                            ProfileGridItem(label = "Work Location", value = workLocation, tokens = tokens)
                        }
                    }
                }
            }

            // Work Experience Card
            if (!m?.workExperience.isNullOrEmpty()) {
                ProfileInfoCard(
                    icon = R.drawable.ic_breifcase,
                    title = "Work Experience",
                    tokens = tokens
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(tokens.screenPadding * 0.6f)) {
                        m.workExperience.forEach { exp ->
                            Card(
                                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.75f),
                                colors = CardDefaults.cardColors(containerColor = whiteBg),
                                border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(BorderColor)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(tokens.screenPadding * 0.75f)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(AccentColor.copy(alpha = 0.08f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.LocationCity,
                                                    contentDescription = null,
                                                    tint = AccentColor,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            Column {
                                                if (!exp.companyName.isNullOrBlank()) {
                                                    Text(
                                                        text = exp.companyName,
                                                        fontSize = tokens.bodySmall,
                                                        fontWeight = FontWeight.Medium,
                                                        color = TitleColor
                                                    )
                                                }
                                                if (!exp.jobTitle.isNullOrBlank()) {
                                                    Text(
                                                        text = exp.jobTitle,
                                                        fontSize = tokens.caption,
                                                        color = LabelColor
                                                    )
                                                }
                                            }
                                        }

                                        val startYear = formatYearOnly(exp.fromDate)
                                        val endYear = if (exp.isRelevant) "Present" else formatYearOnly(exp.toDate)
                                        val duration = listOfNotNull(startYear.takeIf { it.isNotBlank() }, endYear.takeIf { it.isNotBlank() }).joinToString(" — ")
                                        if (duration.isNotBlank()) {
                                            Text(
                                                text = duration,
                                                fontSize = tokens.caption,
                                                fontWeight = FontWeight.Medium,
                                                color = AccentColor
                                            )
                                        }
                                    }

                                    if (!exp.jobDescription.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = exp.jobDescription,
                                            fontSize = tokens.caption,
                                            color = LabelColor,
                                            lineHeight = tokens.caption * 1.35f
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Education Details Card
            if (!m?.education.isNullOrEmpty()) {
                ProfileInfoCard(
                    icon = R.drawable.ic_education,
                    title = "Education Details",
                    tokens = tokens
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(tokens.screenPadding * 0.6f)) {
                        m.education.forEach { edu ->
                            Card(
                                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.75f),
                                colors = CardDefaults.cardColors(containerColor = whiteBg),
                                border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(BorderColor)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(tokens.screenPadding * 0.75f),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(AccentColor.copy(alpha = 0.08f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.School,
                                                contentDescription = null,
                                                tint = AccentColor,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Column {
                                            val degreeLine = listOfNotNull(edu.degree?.takeIf { it.isNotBlank() }, edu.specialization?.takeIf { it.isNotBlank() }).joinToString(" in ")
                                            if (degreeLine.isNotBlank()) {
                                                Text(
                                                    text = degreeLine,
                                                    fontSize = tokens.bodySmall,
                                                    fontWeight = FontWeight.Medium,
                                                    color = TitleColor
                                                )
                                            }

                                            val startYear = formatYearOnly(edu.startDate)
                                            val endYear = formatYearOnly(edu.completionDate)
                                            val yearSpan = listOfNotNull(startYear.takeIf { it.isNotBlank() }, endYear.takeIf { it.isNotBlank() }).joinToString(" — ")
                                            val subLine = listOfNotNull(edu.instituteName?.takeIf { it.isNotBlank() }, yearSpan.takeIf { it.isNotBlank() }).joinToString(" • ")

                                            if (subLine.isNotBlank()) {
                                                Text(
                                                    text = subLine,
                                                    fontSize = tokens.caption,
                                                    color = LabelColor
                                                )
                                            }
                                        }
                                    }

                                    if (edu.cgpa != null) {
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "Grade",
                                                fontSize = tokens.caption,
                                                color = LabelColor
                                            )
                                            Text(
                                                text = "${edu.cgpa} GPA",
                                                fontSize = tokens.bodySmall,
                                                fontWeight = FontWeight.Medium,
                                                color = AccentColor
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Identity Details Card
            val maskedAadhaar = maskAadhaar(m?.aadhaarNo)
            val nationality = m?.permanentAddress?.countryName?.takeIf { it.isNotBlank() }
            val hasIdentityDetails = !m?.panNo.isNullOrBlank() || maskedAadhaar.isNotBlank() || !m?.passportNo.isNullOrBlank() || nationality != null || !m?.martialStatus.isNullOrBlank() || !m?.bloodGroup.isNullOrBlank()

            if (hasIdentityDetails) {
                ProfileInfoCard(
                    icon = R.drawable.ic_credit,
                    title = "Identity Details",
                    tokens = tokens
                ) {
                    ProfileGrid(tokens = tokens) {
                        if (!m?.panNo.isNullOrBlank()) {
                            ProfileGridItem(label = "PAN Number", value = m.panNo, tokens = tokens)
                        }
                        if (maskedAadhaar.isNotBlank()) {
                            ProfileGridItem(label = "Aadhaar Number", value = maskedAadhaar, tokens = tokens)
                        }
                        if (!m?.passportNo.isNullOrBlank()) {
                            ProfileGridItem(label = "Passport Number", value = m.passportNo, tokens = tokens)
                        }
                        if (nationality != null) {
                            ProfileGridItem(label = "Nationality", value = nationality, tokens = tokens)
                        }
                        if (!m?.martialStatus.isNullOrBlank()) {
                            ProfileGridItem(label = "Marital Status", value = m.martialStatus.replaceFirstChar { it.uppercase() }, tokens = tokens)
                        }
                        if (!m?.bloodGroup.isNullOrBlank()) {
                            ProfileGridItem(label = "Blood Group", value = m.bloodGroup, tokens = tokens)
                        }
                    }
                }
            }

            // Employee Documents Card (Only render if at least one document exists)
            val documentList = listOfNotNull(
                m?.memberId?.takeIf { it.isNotBlank() }?.let { "Resume_${fullName.replace(" ", "_")}.pdf" },
                "Offer_Letter.pdf".takeIf { !m?.doj.isNullOrBlank() },
                "ID_Proof.pdf".takeIf { !m?.aadhaarNo.isNullOrBlank() || !m?.panNo.isNullOrBlank() }
            )

            // 2. Document link vantha mattum show aagura Card block:
            // Employee Documents Card (Only render if at least one valid document link exists)
            if (validDocumentList.isNotEmpty()) {
                ProfileInfoCard(
                    icon = R.drawable.ic_folder,
                    title = "Employee Documents",
                    tokens = tokens
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        validDocumentList.forEach { (docName, docUrl) ->
                            Card(
                                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.75f),
                                colors = CardDefaults.cardColors(containerColor = whiteBg),
                                border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(BorderColor)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = tokens.screenPadding * 0.75f, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFFFEE2E2)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Description,
                                                contentDescription = null,
                                                tint = redText,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Text(
                                            text = docName,
                                            fontSize = tokens.bodySmall,
                                            fontWeight = FontWeight.Medium,
                                            color = TitleColor,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            try {
                                                val intent =
                                                    Intent(Intent.ACTION_VIEW, docUrl.toUri())
                                                context.startActivity(intent)
                                            } catch (_: Exception) {}
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.FileDownload,
                                            contentDescription = "Download",
                                            tint = LabelColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(tokens.screenPadding))
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Reusable profile view components
// ─────────────────────────────────────────────────────────────
@Composable
private fun ProfileDetailRow(
    icon: ImageVector,
    text: String,
    tokens: AppDesignTokens
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = LabelColor,
            modifier = Modifier.size(tokens.iconSize * 0.9f)
        )
        Text(
            text = text,
            fontSize = tokens.bodySmall,
            color = LabelColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ProfileInfoCard(
    icon: Int,
    title: String,
    tokens: AppDesignTokens,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(tokens.screenPadding)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    tint = AccentColor,
                    modifier = Modifier.size(tokens.iconSize)
                )
                Text(
                    text = title,
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = TitleColor
                )
            }
            Spacer(modifier = Modifier.height(tokens.screenPadding * 0.75f))
            HorizontalDivider(color = BorderColor)
            Spacer(modifier = Modifier.height(tokens.screenPadding * 0.75f))
            content()
        }
    }
}

@Composable
private fun ProfileGrid(
    tokens: AppDesignTokens,
    content: @Composable () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        content()
    }
}

@Composable
private fun ProfileGridItem(
    label: String,
    value: String,
    tokens: AppDesignTokens
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = tokens.caption,
                fontWeight = FontWeight.Medium,
                color = LabelColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.Medium,
                color = TitleColor
            )
        }
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