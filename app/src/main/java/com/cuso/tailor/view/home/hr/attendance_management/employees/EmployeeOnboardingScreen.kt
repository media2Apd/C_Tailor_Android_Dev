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

package com.cuso.tailor.view.home.hr.attendance_management.employees

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.LocationCity
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.cuso.tailor.adaptive_screen.AppDesignTokens
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.hr.*
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.*
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.delay
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID
import java.util.regex.Pattern

// Screen mode definition
enum class ScreenMode { CREATE, VIEW, EDIT }

// ---------------------------------------------------------------------------
// Static Dropdown Options matching Web Reference
// ---------------------------------------------------------------------------

private val BLOOD_GROUPS = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
private val PAY_FREQUENCIES = listOf("Monthly", "Weekly", "Biweekly", "Daily")
private val MEMBER_EMPLOYMENT_TYPES = listOf("full-time", "part-time", "contract", "volunteer")
private val WORK_EXP_EMPLOYMENT_TYPES = listOf("Full Time", "Part Time", "Contract", "Intern", "Temporary")
private val GENDER_OPTIONS = listOf("Male", "Female", "Other")
private val MARITAL_OPTIONS = listOf("Single", "Married", "Divorced", "Widowed")
private val NOMINEE_RELATIONSHIPS = listOf("Father", "Mother", "Spouse", "Son", "Daughter", "Brother", "Sister", "Other")

// ---------------------------------------------------------------------------
// Dynamic Model Item Entries
// ---------------------------------------------------------------------------

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
    val isRelevant: Boolean = false
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
            onEdit = { memberIdToLoad?.let { onNavigateToEdit(it) } },
            hrViewModel = hrViewModel,
            branchViewModel = branchViewModel,
            departmentViewModel = departmentViewModel,
            designationViewModel = designationViewModel
        )
        return
    }

    val tokens: AppDesignTokens = LocalAppTokens.current
    val context = LocalContext.current

    val isEditMode = mode == ScreenMode.EDIT

    // -------------------------------------------------------------------------
    // Form States
    // -------------------------------------------------------------------------

    // 1. Personal Information
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var personalMail by remember { mutableStateOf("") }
    var workMobile by remember { mutableStateOf("") }
    var personalMobile by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var genderExpanded by remember { mutableStateOf(false) }

    // 2. Permanent Address
    var countryCode by remember { mutableStateOf("") }
    var countryName by remember { mutableStateOf("") }
    var subdivisionCode by remember { mutableStateOf("") }
    var subdivisionName by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var flatNo by remember { mutableStateOf("") }
    var areaZone by remember { mutableStateOf("") }
    var pincode by remember { mutableStateOf("") }
    var streetAddress by remember { mutableStateOf("") }

    // 3. Temporary Address
    var hasTemporaryAddress by remember { mutableStateOf(false) } // true = Same as permanent
    var tempCountryCode by remember { mutableStateOf("") }
    var tempCountryName by remember { mutableStateOf("") }
    var tempSubdivisionCode by remember { mutableStateOf("") }
    var tempSubdivisionName by remember { mutableStateOf("") }
    var tempCity by remember { mutableStateOf("") }
    var tempFlatNo by remember { mutableStateOf("") }
    var tempAreaZone by remember { mutableStateOf("") }
    var tempPincode by remember { mutableStateOf("") }
    var tempStreetAddress by remember { mutableStateOf("") }

    // 4. Identity & Personal Details
    var aadhaarNo by remember { mutableStateOf("") }
    var panNo by remember { mutableStateOf("") }
    var passportNo by remember { mutableStateOf("") }
    var martialStatus by remember { mutableStateOf("") }
    var maritalExpanded by remember { mutableStateOf(false) }
    var bloodGroup by remember { mutableStateOf("") }
    var bloodGroupExpanded by remember { mutableStateOf(false) }
    var emergencyContactName by remember { mutableStateOf("") }
    var emergencyContactMobile by remember { mutableStateOf("") }

    // 5. Government & Statutory IDs
    var uanNo by remember { mutableStateOf("") }
    var esicNumber by remember { mutableStateOf("") }
    var pfAccountNo by remember { mutableStateOf("") }
    var payFrequency by remember { mutableStateOf("") }
    var payFrequencyExpanded by remember { mutableStateOf(false) }

    // 6. Bank Account Details
    var accountHolderName by remember { mutableStateOf("") }
    var accountNumber by remember { mutableStateOf("") }
    var ifscCode by remember { mutableStateOf("") }
    var bankName by remember { mutableStateOf("") }
    var branchName by remember { mutableStateOf("") }

    // 7. Dynamic Lists
    val pfGratuityNominees = remember { mutableStateListOf<NomineeEntry>() }
    val workExperience = remember { mutableStateListOf<ExperienceEntry>() }
    val education = remember { mutableStateListOf<EducationEntry>() }

    // 8. Job Details & Role Scope
    var employeeCode by remember { mutableStateOf("") }
    var doj by remember { mutableStateOf("") }
    var departmentId by remember { mutableStateOf("") }
    var departmentExpanded by remember { mutableStateOf(false) }
    var designationId by remember { mutableStateOf("") }
    var designationExpanded by remember { mutableStateOf(false) }
    var branchId by remember { mutableStateOf("") }
    var branchExpanded by remember { mutableStateOf(false) }
    val allowedBranches = remember { mutableStateListOf<String>() }
    var shiftId by remember { mutableStateOf("") }
    var shiftExpanded by remember { mutableStateOf(false) }
    var reportingTo by remember { mutableStateOf("") }
    var reportingToExpanded by remember { mutableStateOf(false) }
    var secondaryReportingTo by remember { mutableStateOf("") }
    var secondaryReportingToExpanded by remember { mutableStateOf(false) }
    var workingDistrict by remember { mutableStateOf("") }
    var employmentType by remember { mutableStateOf("full-time") }

    // 9. Access & Permissions
    var customRoleId by remember { mutableStateOf("") }
    var roleExpanded by remember { mutableStateOf(false) }
    var roleScope by remember { mutableStateOf("") }

    // Profile Picture Handling
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    var existingProfilePictureUrl by remember { mutableStateOf<String?>(null) }
    var profileImageFile by remember { mutableStateOf<File?>(null) }

    // Validation State Tracking
    val errors = remember { mutableStateMapOf<String, String>() }

    // Dropdown Data Observables
    val branchUiState by branchViewModel.uiState.collectAsState()
    val branches = (branchUiState as? BranchUiState.Success)?.branches ?: emptyList()

    val departmentUiState by departmentViewModel.uiState.collectAsState()
    val departments = (departmentUiState as? DepartmentUiState.Success)?.departments ?: emptyList()

    val designationUiState by designationViewModel.uiState.collectAsState()
    val designations = (designationUiState as? DesignationUiState.Success)?.items ?: emptyList()

    val roles by hrViewModel.roles.collectAsState()
    val shifts by hrViewModel.shifts.collectAsState()
    val members by hrViewModel.members.collectAsState()

    val roleDetails by hrViewModel.roleDetails.collectAsState()
    val memberDetail by hrViewModel.memberDetail.collectAsState()
    val createMemberState by hrViewModel.createMemberState.collectAsState()

    // Sync roleScope whenever roleDetails updates
    LaunchedEffect(roleDetails) {
        roleDetails?.accessScope?.let { scope ->
            roleScope = scope
        }
    }

    val cropLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val resultUri = result.data?.let { UCrop.getOutput(it) }
            resultUri?.let { uri ->
                profileImageUri = uri
                val file = uriToFile(context, uri)
                profileImageFile = file
                // Call API to upload image immediately in edit mode
                if (isEditMode && memberIdToLoad != null) {
                    hrViewModel.uploadProfilePicture(memberIdToLoad, file)
                    // Fetch updated details from API
                    hrViewModel.fetchMemberDetail(memberIdToLoad)
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
                setToolbarColor(Primary.hashCode())
                setToolbarWidgetColor(android.graphics.Color.WHITE)
            }

            val uCropIntent = UCrop.of(sourceUri, destinationUri)
                .withAspectRatio(1f, 1f)
                .withMaxResultSize(800, 800)
                .withOptions(options)
                .getIntent(context)

            cropLauncher.launch(uCropIntent)
        }
    }

    // Load initial dropdowns
    LaunchedEffect(Unit) {
        hrViewModel.fetchMembers()
        hrViewModel.fetchRoles()
        hrViewModel.fetchShifts()
        branchViewModel.loadBranches()
        departmentViewModel.loadDepartments()
        designationViewModel.loadDesignations()
    }

    // Load existing member if editing
    LaunchedEffect(memberIdToLoad) {
        if (isEditMode && !memberIdToLoad.isNullOrBlank()) {
            hrViewModel.fetchMemberDetail(memberIdToLoad)
        }
    }

    // Hydrate form state from fetched member details
    LaunchedEffect(memberDetail) {
        val m = memberDetail ?: return@LaunchedEffect

        firstName = m.firstName.orEmpty()
        lastName = m.lastName.orEmpty()
        personalMail = m.personalMail ?: m.email.orEmpty()
        workMobile = m.workMobile ?: m.userId?.mobile.orEmpty()
        personalMobile = m.personalMobile.orEmpty()
        dob = formatDateForDisplay(m.dob)
        gender = m.gender?.replaceFirstChar { it.uppercase() }.orEmpty()

        m.permanentAddress?.let { addr ->
            countryCode = addr.countryCode.orEmpty()
            countryName = addr.countryName.orEmpty()
            subdivisionCode = addr.subdivisionCode.orEmpty()
            subdivisionName = addr.subdivisionName.orEmpty()
            city = addr.city.orEmpty()
            flatNo = addr.flatNo.orEmpty()
            areaZone = addr.areaZone.orEmpty()
            pincode = addr.pincode.orEmpty()
            streetAddress = addr.street.orEmpty()
        }

        hasTemporaryAddress = m.hasTemporaryAddress
        if (!m.hasTemporaryAddress && m.temporaryAddress != null) {
            val tAddr = m.temporaryAddress
            tempCountryCode = tAddr.countryCode.orEmpty()
            tempCountryName = tAddr.countryName.orEmpty()
            tempSubdivisionCode = tAddr.subdivisionCode.orEmpty()
            tempSubdivisionName = tAddr.subdivisionName.orEmpty()
            tempCity = tAddr.city.orEmpty()
            tempFlatNo = tAddr.flatNo.orEmpty()
            tempAreaZone = tAddr.areaZone.orEmpty()
            tempPincode = tAddr.pincode.orEmpty()
            tempStreetAddress = tAddr.street.orEmpty()
        }

        aadhaarNo = m.aadhaarNo.orEmpty()
        panNo = m.panNo.orEmpty()
        passportNo = m.passportNo.orEmpty()
        martialStatus = m.martialStatus?.replaceFirstChar { it.uppercase() }.orEmpty()
        bloodGroup = m.bloodGroup.orEmpty()
        emergencyContactName = m.emergencyContactName.orEmpty()
        emergencyContactMobile = m.emergencyContactMobile.orEmpty()

        uanNo = m.uanNo.orEmpty()
        esicNumber = m.esicNumber.orEmpty()
        pfAccountNo = m.pfAccountNo.orEmpty()
        payFrequency = m.payFrequency?.replaceFirstChar { it.uppercase() }.orEmpty()

        accountHolderName = m.accountHolderName.orEmpty()
        accountNumber = m.accountNumber.orEmpty()
        ifscCode = m.ifscCode.orEmpty()
        bankName = m.bankName.orEmpty()
        branchName = m.branchName.orEmpty()

        pfGratuityNominees.clear()
        m.pfGratuityNominees.forEach { nom ->
            pfGratuityNominees.add(
                NomineeEntry(
                    id = nom._id ?: UUID.randomUUID().toString(),
                    name = nom.name.orEmpty(),
                    relationship = nom.relationship?.replaceFirstChar { it.uppercase() }.orEmpty(),
                    sharePercent = nom.share?.toString().orEmpty()
                )
            )
        }

        workExperience.clear()
        m.workExperience.forEach { exp ->
            workExperience.add(
                ExperienceEntry(
                    id = exp._id ?: UUID.randomUUID().toString(),
                    companyName = exp.companyName.orEmpty(),
                    jobTitle = exp.jobTitle.orEmpty(),
                    employmentType = exp.employmentType.orEmpty(),
                    location = exp.location.orEmpty(),
                    fromDate = formatDateForDisplay(exp.fromDate),
                    toDate = formatDateForDisplay(exp.toDate),
                    jobDescription = exp.jobDescription.orEmpty(),
                    isRelevant = exp.isRelevant
                )
            )
        }

        education.clear()
        m.education.forEach { edu ->
            education.add(
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

        employeeCode = m.memberId ?: m.employeeCode.orEmpty()
        doj = formatDateForDisplay(m.doj)
        departmentId = m.departmentId?._id.orEmpty()
        designationId = m.designationId?._id.orEmpty()
        branchId = m.branchId?._id.orEmpty()

        shiftId = m.shiftId.orEmpty()
        reportingTo = m.reportingTo.orEmpty()
        secondaryReportingTo = m.secondaryReportingTo.orEmpty()
        workingDistrict = m.workingDistrict.orEmpty()
        employmentType = m.employmentType?.lowercase() ?: "full-time"

        val roleId = m.customRoleId?._id ?: m.role.orEmpty()
        customRoleId = roleId
        if (roleId.isNotBlank()) {
            hrViewModel.fetchRoleById(roleId)
        }

        existingProfilePictureUrl = m.profilePicture
    }

    // Success & Error feedback
// Success & Error feedback
    LaunchedEffect(createMemberState) {
        when (val state = createMemberState) {
            is HrViewModel.CreateMemberState.Success -> {
                DynamicIslandManager.showSuccess(
                    if (isEditMode) "Employee updated successfully" else "Employee created successfully"
                )
                hrViewModel.fetchMembers()
                delay(500)
                hrViewModel.resetCreateMemberState()

                // Go back call
                if (isEditMode) {
                    onUpdateEmployee()
                } else {
                    onCreateEmployee()
                }
            }
            is HrViewModel.CreateMemberState.Error -> {
                DynamicIslandManager.showError(state.message)
                hrViewModel.resetCreateMemberState()
            }
            else -> Unit
        }
    }

    val totalNomineeShare = remember(pfGratuityNominees.toList()) {
        pfGratuityNominees.sumOf { it.sharePercent.toDoubleOrNull() ?: 0.0 }
    }

    // Validation according to React Onboarding Logic
    fun validate(): Boolean {
        errors.clear()

        if (firstName.trim().isBlank()) errors["firstName"] = "Required"
        if (lastName.trim().isBlank()) errors["lastName"] = "Required"
        if (workMobile.trim().isBlank()) errors["workMobile"] = "Required"
        if (dob.trim().isBlank()) errors["dob"] = "Required"
        if (gender.trim().isBlank() || gender == "Select gender") errors["gender"] = "Required"
        if (emergencyContactName.trim().isBlank()) errors["emergencyContactName"] = "Required"
        if (bloodGroup.trim().isBlank() || bloodGroup == "Select group") errors["bloodGroup"] = "Required"

        if (panNo.isNotBlank() && !Pattern.compile("^[A-Z]{5}[0-9]{4}[A-Z]$").matcher(panNo.trim()).matches()) {
            errors["panNo"] = "Invalid PAN format (e.g. ABCDE1234F)"
        }

        if (aadhaarNo.isNotBlank() && !Pattern.compile("^\\d{12}$").matcher(aadhaarNo.trim()).matches()) {
            errors["aadhaarNo"] = "Invalid Aadhaar (12 digits)"
        }

        if (payFrequency.trim().isBlank() || payFrequency == "Select frequency") errors["payFrequency"] = "Required"
        if (accountHolderName.trim().isBlank()) errors["accountHolderName"] = "Required"
        if (accountNumber.trim().isBlank()) errors["accountNumber"] = "Required"
        if (ifscCode.trim().isBlank()) errors["ifscCode"] = "Required"
        if (bankName.trim().isBlank()) errors["bankName"] = "Required"
        if (branchName.trim().isBlank()) errors["branchName"] = "Required"

        if (pfGratuityNominees.isNotEmpty()) {
            pfGratuityNominees.forEachIndexed { i, n ->
                if (n.name.trim().isBlank()) errors["nominee_${i}_name"] = "Required"
                if (n.relationship.trim().isBlank() || n.relationship == "Select an option") errors["nominee_${i}_relationship"] = "Required"
                if (n.sharePercent.trim().isBlank()) errors["nominee_${i}_share"] = "Required"
            }
            if (totalNomineeShare != 100.0) {
                errors["pfGratuityNominees"] = "Nominee share must total 100%"
            }
        }

        workExperience.forEachIndexed { i, exp ->
            if (exp.companyName.trim().isBlank()) errors["exp_${i}_companyName"] = "Required"
            if (exp.jobTitle.trim().isBlank()) errors["exp_${i}_jobTitle"] = "Required"
            if (exp.employmentType.trim().isBlank() || exp.employmentType == "Select type") errors["exp_${i}_employmentType"] = "Required"
            if (exp.location.trim().isBlank()) errors["exp_${i}_location"] = "Required"
            if (exp.fromDate.trim().isBlank()) errors["exp_${i}_fromDate"] = "Required"
        }

        education.forEachIndexed { i, edu ->
            if (edu.degree.trim().isBlank()) errors["edu_${i}_degree"] = "Required"
            if (edu.instituteName.trim().isBlank()) errors["edu_${i}_instituteName"] = "Required"
            if (edu.startDate.trim().isBlank()) errors["edu_${i}_startDate"] = "Required"
        }

        if (departmentId.isBlank()) errors["departmentId"] = "Required"
        if (doj.trim().isBlank()) errors["doj"] = "Required"
        if (customRoleId.isBlank()) errors["customRoleId"] = "Required"

        if (roleScope.equals("regional", ignoreCase = true) && allowedBranches.isEmpty()) {
            errors["allowedBranches"] = "Please select at least one branch"
        }

        if (errors.isNotEmpty()) {
            DynamicIslandManager.showError("Please check the highlighted fields")
            return false
        }
        return true
    }

    // Submit handler matching React cleanObject and payload
    fun handleSubmit() {
        if (!validate()) return

        val submitBranchId = if (roleScope.equals("regional", ignoreCase = true)) null else branchId.ifBlank { null }

        val permanentAddressPayload = AddressRequest(
            flatNo = flatNo.trim().ifBlank { null },
            street = streetAddress.trim().ifBlank { null },
            areaZone = areaZone.trim().ifBlank { null },
            city = city.trim().ifBlank { null },
            subdivisionCode = subdivisionCode.trim().ifBlank { null },
            subdivisionName = subdivisionName.trim().ifBlank { null },
            countryCode = countryCode.trim().ifBlank { null },
            countryName = countryName.trim().ifBlank { null },
            pincode = pincode.trim().ifBlank { null }
        )

        val temporaryAddressPayload = if (hasTemporaryAddress) {
            null
        } else {
            AddressRequest(
                flatNo = tempFlatNo.trim().ifBlank { null },
                street = tempStreetAddress.trim().ifBlank { null },
                areaZone = tempAreaZone.trim().ifBlank { null },
                city = tempCity.trim().ifBlank { null },
                subdivisionCode = tempSubdivisionCode.trim().ifBlank { null },
                subdivisionName = tempSubdivisionName.trim().ifBlank { null },
                countryCode = tempCountryCode.trim().ifBlank { null },
                countryName = tempCountryName.trim().ifBlank { null },
                pincode = tempPincode.trim().ifBlank { null }
            )
        }

        val educationPayload = education.map {
            EducationRequestItem(
                degree = it.degree.trim(),
                specialization = it.specialization.trim(),
                instituteName = it.instituteName.trim(),
                startDate = toApiDate(it.startDate),
                completionDate = toApiDate(it.completionDate).ifBlank { null },
                cgpa = it.cgpa.trim().ifBlank { null }
            )
        }

        val workExpPayload = workExperience.map {
            WorkExperienceRequestItem(
                companyName = it.companyName.trim(),
                jobTitle = it.jobTitle.trim(),
                employmentType = it.employmentType.trim(),
                location = it.location.trim(),
                fromDate = toApiDate(it.fromDate),
                toDate = if (it.isRelevant) null else toApiDate(it.toDate).ifBlank { null },
                jobDescription = it.jobDescription.trim(),
                isRelevant = it.isRelevant
            )
        }

        val nomineesPayload = pfGratuityNominees.map {
            NomineeRequestItem(
                name = it.name.trim(),
                relationship = it.relationship.trim(),
                share = it.sharePercent.trim()
            )
        }

        if (isEditMode && memberIdToLoad != null) {
            val updateRequest = UpdateMemberRequest(
                firstName = firstName.trim(),
                lastName = lastName.trim(),
                personalMail = personalMail.trim(),
                workMobile = workMobile.trim(),
                aadhaarNo = aadhaarNo.trim(),
                panNo = panNo.trim().uppercase(),
                passportNo = passportNo.trim().uppercase(),
                bloodGroup = bloodGroup.trim(),
                dob = toApiDate(dob),
                doj = toApiDate(doj),
                gender = gender.lowercase().trim(),
                martialStatus = martialStatus.lowercase().trim(),
                emergencyContactName = emergencyContactName.trim(),
                emergencyContactMobile = emergencyContactMobile.trim(),
                uanNo = uanNo.trim(),
                esicNumber = esicNumber.trim(),
                pfAccountNo = pfAccountNo.trim(),
                payFrequency = payFrequency.trim(),
                employmentType = employmentType,
                workingDistrict = workingDistrict.trim(),
                branchId = submitBranchId,
                branchName = branchName.trim().ifBlank { null },
                departmentId = departmentId.ifBlank { null },
                designationId = designationId.ifBlank { null },
                customRoleId = customRoleId.ifBlank { null },
                shiftId = shiftId.ifBlank { null },
                reportingTo = reportingTo.ifBlank { null },
                secondaryReportingTo = secondaryReportingTo.ifBlank { null },
                accountHolderName = accountHolderName.trim(),
                accountNumber = accountNumber.trim(),
                bankName = bankName.trim(),
                ifscCode = ifscCode.trim().uppercase(),
                permanentAddress = permanentAddressPayload,
                hasTemporaryAddress = hasTemporaryAddress,
                temporaryAddress = temporaryAddressPayload,
                education = educationPayload,
                workExperience = workExpPayload,
                pfGratuityNominees = nomineesPayload
            )
            hrViewModel.updateMember(memberIdToLoad, updateRequest)
        } else {
            val createRequest = CreateMemberRequest(
                firstName = firstName.trim(),
                lastName = lastName.trim(),
                personalMail = personalMail.trim(),
                workMobile = workMobile.trim(),
                aadhaarNo = aadhaarNo.trim(),
                panNo = panNo.trim().uppercase(),
                passportNo = passportNo.trim().uppercase(),
                bloodGroup = bloodGroup.trim(),
                dob = toApiDate(dob),
                doj = toApiDate(doj),
                gender = gender.lowercase().trim(),
                martialStatus = martialStatus.lowercase().trim(),
                emergencyContactName = emergencyContactName.trim(),
                emergencyContactMobile = emergencyContactMobile.trim(),
                uanNo = uanNo.trim(),
                esicNumber = esicNumber.trim(),
                pfAccountNo = pfAccountNo.trim(),
                payFrequency = payFrequency.trim(),
                employmentType = employmentType,
                workingDistrict = workingDistrict.trim(),
                branchId = submitBranchId,
                branchName = branchName.trim().ifBlank { null },
                departmentId = departmentId.ifBlank { null },
                designationId = designationId.ifBlank { null },
                customRoleId = customRoleId.ifBlank { null },
                shiftId = shiftId.ifBlank { null },
                reportingTo = reportingTo.ifBlank { null },
                secondaryReportingTo = secondaryReportingTo.ifBlank { null },
                accountHolderName = accountHolderName.trim(),
                accountNumber = accountNumber.trim(),
                bankName = bankName.trim(),
                ifscCode = ifscCode.trim().uppercase(),
                permanentAddress = permanentAddressPayload,
                hasTemporaryAddress = hasTemporaryAddress,
                temporaryAddress = temporaryAddressPayload,
                education = educationPayload,
                workExperience = workExpPayload,
                pfGratuityNominees = nomineesPayload
            )
            hrViewModel.createMember(createRequest)
        }
    }

    Scaffold(
        containerColor = Primary_background,
        topBar = {
            TitleBar(
                title = if (isEditMode) "Edit Employee" else "Employee Onboarding",
                onClose = onDismiss
            )
        },
        bottomBar = {
            Surface(
                color = whiteBg,
                border = BorderStroke(1.dp, sectionBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = tokens.screenPadding, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(tokens.cardCornerRadius),
                        border = BorderStroke(1.dp, sectionBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                        modifier = Modifier
                            .weight(1f)
                            .height(tokens.buttonHeight)
                    ) {
                        Text("Cancel", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium)
                    }

                    Button(
                        onClick = { handleSubmit() },
                        shape = RoundedCornerShape(tokens.cardCornerRadius),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        modifier = Modifier
                            .weight(1f)
                            .height(tokens.buttonHeight)
                    ) {
                        Text(
                            text = if (isEditMode) "Save Changes" else "Save Employee",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = whiteBg
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(tokens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(tokens.screenPadding)
        ) {
            // ===============================================================
            // 1. PERSONAL INFORMATION
            // ===============================================================
            FormSectionCard(
                title = "Personal Information",
                description = "Basic details and contact information of the employee."
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(RoundedCornerShape(tokens.cardCornerRadius))
                                .background(light_grey)
                                .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius)),
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                profileImageUri != null -> {
                                    AsyncImage(
                                        model = profileImageUri,
                                        contentDescription = "Profile",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                !existingProfilePictureUrl.isNullOrBlank() -> {
                                    AsyncImage(
                                        model = existingProfilePictureUrl,
                                        contentDescription = "Profile",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                else -> {
                                    Icon(
                                        imageVector = Icons.Outlined.Person,
                                        contentDescription = null,
                                        tint = mutedText,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }

                            if (profileImageUri != null || !existingProfilePictureUrl.isNullOrBlank()) {
                                IconButton(
                                    onClick = {
                                        // Call API to delete profile picture if in edit mode
                                        if (isEditMode && memberIdToLoad != null) {
                                            hrViewModel.deleteProfilePicture(memberIdToLoad)
                                            hrViewModel.fetchMemberDetail(memberIdToLoad)
                                        }
                                        profileImageUri = null
                                        existingProfilePictureUrl = null
                                        profileImageFile = null
                                    },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(24.dp)
                                        .background(redBg, CircleShape)
                                ) {
                                    Icon(Icons.Filled.Close, contentDescription = "Remove", tint = redText, modifier = Modifier.size(14.dp))
                                }
                            }
                        }

                        OutlinedButton(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            shape = RoundedCornerShape(tokens.cardCornerRadius),
                            border = BorderStroke(1.dp, Primary),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary),
                            modifier = Modifier.height(tokens.fieldHeight)
                        ) {
                            Icon(Icons.Outlined.Upload, contentDescription = null, modifier = Modifier.size(tokens.iconSize))
                            Spacer(Modifier.width(6.dp))
                            Text("Upload Photo", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                Spacer(Modifier.height(tokens.screenPadding))

                FormLabel("First Name", isRequired = true)
                FormTextField(
                    value = firstName,
                    onValueChange = {
                        firstName = it
                        errors.remove("firstName")
                    },
                    placeholder = "e.g. John",
                    isError = errors.containsKey("firstName"),
                    errorMessage = errors["firstName"]
                )

                Spacer(Modifier.height(tokens.screenPadding * 0.5f))

                FormLabel("Last Name", isRequired = true)
                FormTextField(
                    value = lastName,
                    onValueChange = {
                        lastName = it
                        errors.remove("lastName")
                    },
                    placeholder = "e.g. Doe",
                    isError = errors.containsKey("lastName"),
                    errorMessage = errors["lastName"]
                )

                Spacer(Modifier.height(tokens.screenPadding * 0.5f))

                FormLabel("Personal Email")
                FormTextField(
                    value = personalMail,
                    onValueChange = { personalMail = it },
                    placeholder = "john.doe@company.com",
                    keyboardType = KeyboardType.Email
                )

                Spacer(Modifier.height(tokens.screenPadding * 0.5f))

                FormLabel("Work Phone", isRequired = true)
                PhoneInputField(
                    phoneValue = workMobile,
                    onPhoneChange = {
                        workMobile = it
                        errors.remove("workMobile")
                    },
                    onCountryChange = { /* country handling if needed */ },
                    isError = errors.containsKey("workMobile"),
                    errorMessage = errors["workMobile"]
                )

                Spacer(Modifier.height(tokens.screenPadding * 0.5f))

                FormLabel("Personal Phone")
                PhoneInputField(
                    phoneValue = personalMobile,
                    onPhoneChange = { personalMobile = it },
                    onCountryChange = { /* country handling if needed */ }
                )

                Spacer(Modifier.height(tokens.screenPadding * 0.5f))

                FormLabel("Date of Birth", isRequired = true)
                DatePickerField(
                    value = dob,
                    onDateSelected = {
                        dob = it
                        errors.remove("dob")
                    },
                    isError = errors.containsKey("dob")
                )
                if (errors.containsKey("dob")) {
                    Text(errors["dob"].orEmpty(), color = redText, fontSize = tokens.label)
                }

                Spacer(Modifier.height(tokens.screenPadding * 0.5f))

                FormDropdown(
                    label = "Gender",
                    value = gender.ifBlank { "Select gender" },
                    expanded = genderExpanded,
                    onExpandChange = { genderExpanded = it },
                    options = GENDER_OPTIONS,
                    onOptionSelected = {
                        gender = it
                        errors.remove("gender")
                    },
                    isRequired = true,
                    isError = errors.containsKey("gender"),
                    errorMessage = errors["gender"]
                )

                // ── Permanent Address ──
                Spacer(Modifier.height(tokens.screenPadding))
                HorizontalDivider(color = sectionBorder)
                Spacer(Modifier.height(tokens.screenPadding * 0.5f))

                Text("Permanent Address", fontSize = tokens.bodyMedium, fontWeight = FontWeight.SemiBold, color = title_color)
                Spacer(Modifier.height(tokens.screenPadding * 0.5f))

                // Replaced manual Country/State text fields with CountryAndStatePicker
                CountryAndStatePicker(
                    selectedCountry = countryName,
                    selectedState = subdivisionName,
                    onCountryChange = { country ->
                        countryName = country
                        countryCode = if (country.equals("India", ignoreCase = true)) "IN" else country.take(2).uppercase()
                    },
                    onStateChange = { state ->
                        subdivisionName = state
                        subdivisionCode = state.take(2).uppercase()
                    }
                )

                Spacer(Modifier.height(tokens.screenPadding * 0.5f))

                FormLabel("City")
                FormTextField(
                    value = city,
                    onValueChange = { city = it },
                    placeholder = "Select city"
                )

                Spacer(Modifier.height(tokens.screenPadding * 0.5f))

                FormLabel("Flat / Door No")
                FormTextField(
                    value = flatNo,
                    onValueChange = { flatNo = it },
                    placeholder = "Enter flat / door no."
                )

                Spacer(Modifier.height(tokens.screenPadding * 0.5f))

                FormLabel("Area / Zone")
                FormTextField(
                    value = areaZone,
                    onValueChange = { areaZone = it },
                    placeholder = "Enter area / zone"
                )

                Spacer(Modifier.height(tokens.screenPadding * 0.5f))

                FormLabel("Pincode")
                FormTextField(
                    value = pincode,
                    onValueChange = { pincode = it.filter { char -> char.isDigit() } },
                    placeholder = "Enter pincode",
                    keyboardType = KeyboardType.Number
                )

                Spacer(Modifier.height(tokens.screenPadding * 0.5f))

                FormLabel("Street Address")
                FormTextArea(
                    value = streetAddress,
                    onValueChange = { streetAddress = it },
                    placeholder = "Street address",
                    minLines = 2,
                    maxLines = 3
                )

                // ── Temporary Address ──
                Spacer(Modifier.height(tokens.screenPadding))
                HorizontalDivider(color = sectionBorder)
                Spacer(Modifier.height(tokens.screenPadding * 0.5f))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { hasTemporaryAddress = !hasTemporaryAddress }
                ) {
                    AppCheckbox(
                        checked = hasTemporaryAddress,
                        onCheckedChange = { hasTemporaryAddress = it }
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Temporary address same as permanent address",
                        fontSize = tokens.bodySmall,
                        color = TextSecondary
                    )
                }

                if (!hasTemporaryAddress) {
                    Spacer(Modifier.height(tokens.screenPadding * 0.5f))

                    // Replaced manual Country/State text fields with CountryAndStatePicker
                    CountryAndStatePicker(
                        selectedCountry = tempCountryName,
                        selectedState = tempSubdivisionName,
                        onCountryChange = { country ->
                            tempCountryName = country
                            tempCountryCode = if (country.equals("India", ignoreCase = true)) "IN" else country.take(2).uppercase()
                        },
                        onStateChange = { state ->
                            tempSubdivisionName = state
                            tempSubdivisionCode = state.take(2).uppercase()
                        }
                    )

                    Spacer(Modifier.height(tokens.screenPadding * 0.5f))

                    FormLabel("City")
                    FormTextField(
                        value = tempCity,
                        onValueChange = { tempCity = it },
                        placeholder = "Select city"
                    )

                    Spacer(Modifier.height(tokens.screenPadding * 0.5f))

                    FormLabel("Flat / Door No")
                    FormTextField(
                        value = tempFlatNo,
                        onValueChange = { tempFlatNo = it },
                        placeholder = "Enter flat / door no."
                    )

                    Spacer(Modifier.height(tokens.screenPadding * 0.5f))

                    FormLabel("Area / Zone")
                    FormTextField(
                        value = tempAreaZone,
                        onValueChange = { tempAreaZone = it },
                        placeholder = "Enter area / zone"
                    )

                    Spacer(Modifier.height(tokens.screenPadding * 0.5f))

                    FormLabel("Pincode")
                    FormTextField(
                        value = tempPincode,
                        onValueChange = { tempPincode = it.filter { char -> char.isDigit() } },
                        placeholder = "Enter pincode",
                        keyboardType = KeyboardType.Number
                    )

                    Spacer(Modifier.height(tokens.screenPadding * 0.5f))

                    FormLabel("Street Address")
                    FormTextArea(
                        value = tempStreetAddress,
                        onValueChange = { tempStreetAddress = it },
                        placeholder = "Street address",
                        minLines = 2,
                        maxLines = 3
                    )
                }
            }

            // ===============================================================
            // 2. IDENTITY & PERSONAL DETAILS
            // ===============================================================
            FormSectionCard(
                title = "Identity & Personal Details",
                description = "Government identification and emergency contacts."
            ) {
                FormLabel("Aadhaar Number")
                FormTextField(
                    value = aadhaarNo,
                    onValueChange = {
                        aadhaarNo = it.filter { char -> char.isDigit() }.take(12)
                        errors.remove("aadhaarNo")
                    },
                    placeholder = "XXXX-XXXX-XXXX",
                    keyboardType = KeyboardType.Number,
                    isError = errors.containsKey("aadhaarNo"),
                    errorMessage = errors["aadhaarNo"]
                )

                Spacer(Modifier.height(tokens.screenPadding * 0.5f))

                FormLabel("PAN Number")
                FormTextField(
                    value = panNo,
                    onValueChange = {
                        panNo = it.uppercase().take(10)
                        errors.remove("panNo")
                    },
                    placeholder = "ABCDE1234F",
                    keyboardCapitalization = KeyboardCapitalization.Characters,
                    isError = errors.containsKey("panNo"),
                    errorMessage = errors["panNo"]
                )

                Spacer(Modifier.height(tokens.screenPadding * 0.5f))

                FormLabel("Passport Number")
                FormTextField(
                    value = passportNo,
                    onValueChange = { passportNo = it.uppercase() },
                    placeholder = "e.g. A1234567"
                )

                Spacer(Modifier.height(tokens.screenPadding * 0.5f))

                FormDropdown(
                    label = "Marital Status",
                    value = martialStatus.ifBlank { "Select status" },
                    expanded = maritalExpanded,
                    onExpandChange = { maritalExpanded = it },
                    options = MARITAL_OPTIONS,
                    onOptionSelected = { martialStatus = it }
                )

                Spacer(Modifier.height(tokens.screenPadding * 0.5f))

                FormDropdown(
                    label = "Blood Group",
                    value = bloodGroup.ifBlank { "Select group" },
                    expanded = bloodGroupExpanded,
                    onExpandChange = { bloodGroupExpanded = it },
                    options = BLOOD_GROUPS,
                    onOptionSelected = {
                        bloodGroup = it
                        errors.remove("bloodGroup")
                    },
                    isRequired = true,
                    isError = errors.containsKey("bloodGroup"),
                    errorMessage = errors["bloodGroup"]
                )

                Spacer(Modifier.height(tokens.screenPadding * 0.5f))

                FormLabel("Emergency Contact Name", isRequired = true)
                FormTextField(
                    value = emergencyContactName,
                    onValueChange = {
                        emergencyContactName = it
                        errors.remove("emergencyContactName")
                    },
                    placeholder = "Full name",
                    isError = errors.containsKey("emergencyContactName"),
                    errorMessage = errors["emergencyContactName"]
                )

                Spacer(Modifier.height(tokens.screenPadding * 0.5f))

                FormLabel("Emergency Contact Phone")
                PhoneInputField(
                    phoneValue = emergencyContactMobile,
                    onPhoneChange = { emergencyContactMobile = it },
                    onCountryChange = { /* country handling if needed */ }
                )
            }

            // ===============================================================
            // 3. GOVERNMENT & STATUTORY IDS
            // ===============================================================
            FormSectionCard(
                title = "Government & Statutory IDs",
                description = "PF, ESIC and payment frequencies."
            ) {
                FormLabel("UAN Number (EPF)")
                FormTextField(
                    value = uanNo,
                    onValueChange = { uanNo = it.filter { char -> char.isDigit() }.take(12) },
                    placeholder = "Universal Account Number",
                    keyboardType = KeyboardType.Number
                )

                Spacer(Modifier.height(tokens.screenPadding * 0.5f))

                FormLabel("ESIC Number")
                FormTextField(
                    value = esicNumber,
                    onValueChange = { esicNumber = it.filter { char -> char.isDigit() } },
                    placeholder = "Member Number",
                    keyboardType = KeyboardType.Number
                )

                Spacer(Modifier.height(tokens.screenPadding * 0.5f))

                FormLabel("PF Account Number")
                FormTextField(
                    value = pfAccountNo,
                    onValueChange = { pfAccountNo = it },
                    placeholder = "Org specific no."
                )

                Spacer(Modifier.height(tokens.screenPadding * 0.5f))

                FormDropdown(
                    label = "Pay Frequency",
                    value = payFrequency.ifBlank { "Select frequency" },
                    expanded = payFrequencyExpanded,
                    onExpandChange = { payFrequencyExpanded = it },
                    options = PAY_FREQUENCIES,
                    onOptionSelected = {
                        payFrequency = it
                        errors.remove("payFrequency")
                    },
                    isRequired = true,
                    isError = errors.containsKey("payFrequency"),
                    errorMessage = errors["payFrequency"]
                )
            }

            // ===============================================================
            // 4. BANK ACCOUNT DETAILS
            // ===============================================================
            FormSectionCard(
                title = "Bank Account Details",
                description = "Bank information for payroll processing."
            ) {
                FormLabel("Account Holder Name", isRequired = true)
                FormTextField(
                    value = accountHolderName,
                    onValueChange = {
                        accountHolderName = it
                        errors.remove("accountHolderName")
                    },
                    placeholder = "As per bank",
                    isError = errors.containsKey("accountHolderName"),
                    errorMessage = errors["accountHolderName"]
                )

                Spacer(Modifier.height(tokens.screenPadding * 0.5f))

                FormLabel("Account Number", isRequired = true)
                FormTextField(
                    value = accountNumber,
                    onValueChange = {
                        accountNumber = it.filter { char -> char.isDigit() }
                        errors.remove("accountNumber")
                    },
                    placeholder = "Bank account no.",
                    keyboardType = KeyboardType.Number,
                    isError = errors.containsKey("accountNumber"),
                    errorMessage = errors["accountNumber"]
                )

                Spacer(Modifier.height(tokens.screenPadding * 0.5f))

                FormLabel("IFSC Code", isRequired = true)
                FormTextField(
                    value = ifscCode,
                    onValueChange = {
                        ifscCode = it.uppercase()
                        errors.remove("ifscCode")
                    },
                    placeholder = "e.g. SBIN0001234",
                    keyboardCapitalization = KeyboardCapitalization.Characters,
                    isError = errors.containsKey("ifscCode"),
                    errorMessage = errors["ifscCode"]
                )

                Spacer(Modifier.height(tokens.screenPadding * 0.5f))

                FormLabel("Bank Name", isRequired = true)
                FormTextField(
                    value = bankName,
                    onValueChange = {
                        bankName = it
                        errors.remove("bankName")
                    },
                    placeholder = "e.g. HDFC Bank",
                    isError = errors.containsKey("bankName"),
                    errorMessage = errors["bankName"]
                )

                Spacer(Modifier.height(tokens.screenPadding * 0.5f))

                FormLabel("Branch Name", isRequired = true)
                FormTextField(
                    value = branchName,
                    onValueChange = {
                        branchName = it
                        errors.remove("branchName")
                    },
                    placeholder = "e.g. Main Branch",
                    isError = errors.containsKey("branchName"),
                    errorMessage = errors["branchName"]
                )
            }

            // ===============================================================
            // 5. PF / GRATUITY NOMINEES
            // ===============================================================
            FormSectionCard(
                title = "PF / Gratuity Nominees",
                action = {
                    OutlinedButton(
                        onClick = { pfGratuityNominees.add(NomineeEntry()) },
                        shape = RoundedCornerShape(tokens.cardCornerRadius),
                        border = BorderStroke(1.dp, Primary),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary),
                        modifier = Modifier.height(tokens.fieldHeight)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(tokens.iconSize))
                        Spacer(Modifier.width(4.dp))
                        Text("Add Nominee", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium)
                    }
                }
            ) {
                if (pfGratuityNominees.isEmpty()) {
                    Text(
                        text = "No nominees added yet",
                        fontSize = tokens.bodySmall,
                        color = mutedText,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    pfGratuityNominees.forEachIndexed { index, nominee ->
                        Card(
                            shape = RoundedCornerShape(tokens.cardCornerRadius),
                            colors = CardDefaults.cardColors(containerColor = Primary_background),
                            border = BorderStroke(1.dp, sectionBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Nominee ${index + 1}",
                                        fontSize = tokens.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = title_color
                                    )
                                    IconButton(
                                        onClick = { pfGratuityNominees.removeAt(index) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = redText, modifier = Modifier.size(18.dp))
                                    }
                                }

                                Spacer(Modifier.height(8.dp))

                                FormLabel("Name", isRequired = true)
                                FormTextField(
                                    value = nominee.name,
                                    onValueChange = {
                                        pfGratuityNominees[index] = nominee.copy(name = it)
                                        errors.remove("nominee_${index}_name")
                                    },
                                    placeholder = "Full name",
                                    isError = errors.containsKey("nominee_${index}_name"),
                                    errorMessage = errors["nominee_${index}_name"]
                                )

                                Spacer(Modifier.height(8.dp))

                                var relationshipExpanded by remember { mutableStateOf(false) }
                                FormDropdown(
                                    label = "Relationship",
                                    value = nominee.relationship.ifBlank { "Select an option" },
                                    expanded = relationshipExpanded,
                                    onExpandChange = { relationshipExpanded = it },
                                    options = NOMINEE_RELATIONSHIPS,
                                    onOptionSelected = {
                                        pfGratuityNominees[index] = nominee.copy(relationship = it)
                                        errors.remove("nominee_${index}_relationship")
                                    },
                                    isRequired = true,
                                    isError = errors.containsKey("nominee_${index}_relationship"),
                                    errorMessage = errors["nominee_${index}_relationship"]
                                )

                                Spacer(Modifier.height(8.dp))

                                FormLabel("Share %", isRequired = true)
                                FormTextField(
                                    value = nominee.sharePercent,
                                    onValueChange = {
                                        pfGratuityNominees[index] = nominee.copy(sharePercent = it.filter { c -> c.isDigit() })
                                        errors.remove("nominee_${index}_share")
                                    },
                                    placeholder = "e.g. 100",
                                    keyboardType = KeyboardType.Number,
                                    isError = errors.containsKey("nominee_${index}_share"),
                                    errorMessage = errors["nominee_${index}_share"]
                                )
                            }
                        }
                    }

                    Surface(
                        color = if (totalNomineeShare == 100.0) greenBg else yellowBg,
                        shape = RoundedCornerShape(tokens.cardCornerRadius),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Allocated: ${totalNomineeShare.toInt()}%" +
                                    if (totalNomineeShare != 100.0) " (Pending: ${(100.0 - totalNomineeShare).coerceAtLeast(0.0).toInt()}%)" else "",
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Medium,
                            color = if (totalNomineeShare == 100.0) greentext else yellowText,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }

                    if (errors.containsKey("pfGratuityNominees")) {
                        Text(
                            text = errors["pfGratuityNominees"].orEmpty(),
                            color = redText,
                            fontSize = tokens.label,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // ===============================================================
            // 6. WORK EXPERIENCE
            // ===============================================================
            FormSectionCard(
                title = "Work Experience",
                action = {
                    OutlinedButton(
                        onClick = { workExperience.add(ExperienceEntry()) },
                        shape = RoundedCornerShape(tokens.cardCornerRadius),
                        border = BorderStroke(1.dp, Primary),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary),
                        modifier = Modifier.height(tokens.fieldHeight)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(tokens.iconSize))
                        Spacer(Modifier.width(4.dp))
                        Text("Add Experience", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium)
                    }
                }
            ) {
                if (workExperience.isEmpty()) {
                    Text(
                        text = "No work experience added yet",
                        fontSize = tokens.bodySmall,
                        color = mutedText,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    workExperience.forEachIndexed { index, exp ->
                        Card(
                            shape = RoundedCornerShape(tokens.cardCornerRadius),
                            colors = CardDefaults.cardColors(containerColor = Primary_background),
                            border = BorderStroke(1.dp, sectionBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Experience ${index + 1}",
                                        fontSize = tokens.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = title_color
                                    )
                                    IconButton(
                                        onClick = { workExperience.removeAt(index) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = redText, modifier = Modifier.size(18.dp))
                                    }
                                }

                                Spacer(Modifier.height(8.dp))

                                FormLabel("Company Name", isRequired = true)
                                FormTextField(
                                    value = exp.companyName,
                                    onValueChange = {
                                        workExperience[index] = exp.copy(companyName = it)
                                        errors.remove("exp_${index}_companyName")
                                    },
                                    placeholder = "e.g. Google",
                                    isError = errors.containsKey("exp_${index}_companyName"),
                                    errorMessage = errors["exp_${index}_companyName"]
                                )

                                Spacer(Modifier.height(8.dp))

                                FormLabel("Job Title", isRequired = true)
                                FormTextField(
                                    value = exp.jobTitle,
                                    onValueChange = {
                                        workExperience[index] = exp.copy(jobTitle = it)
                                        errors.remove("exp_${index}_jobTitle")
                                    },
                                    placeholder = "e.g. Senior Sales Executive",
                                    isError = errors.containsKey("exp_${index}_jobTitle"),
                                    errorMessage = errors["exp_${index}_jobTitle"]
                                )

                                Spacer(Modifier.height(8.dp))

                                var expTypeExpanded by remember { mutableStateOf(false) }
                                FormDropdown(
                                    label = "Employment Type",
                                    value = exp.employmentType.ifBlank { "Select type" },
                                    expanded = expTypeExpanded,
                                    onExpandChange = { expTypeExpanded = it },
                                    options = WORK_EXP_EMPLOYMENT_TYPES,
                                    onOptionSelected = {
                                        workExperience[index] = exp.copy(employmentType = it)
                                        errors.remove("exp_${index}_employmentType")
                                    },
                                    isRequired = true,
                                    isError = errors.containsKey("exp_${index}_employmentType"),
                                    errorMessage = errors["exp_${index}_employmentType"]
                                )

                                Spacer(Modifier.height(8.dp))

                                FormLabel("Location", isRequired = true)
                                FormTextField(
                                    value = exp.location,
                                    onValueChange = {
                                        workExperience[index] = exp.copy(location = it)
                                        errors.remove("exp_${index}_location")
                                    },
                                    placeholder = "City, Country",
                                    isError = errors.containsKey("exp_${index}_location"),
                                    errorMessage = errors["exp_${index}_location"]
                                )

                                Spacer(Modifier.height(8.dp))

                                FormLabel("Start Date", isRequired = true)
                                DatePickerField(
                                    value = exp.fromDate,
                                    onDateSelected = {
                                        workExperience[index] = exp.copy(fromDate = it)
                                        errors.remove("exp_${index}_fromDate")
                                    },
                                    isError = errors.containsKey("exp_${index}_fromDate")
                                )

                                Spacer(Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    FormLabel("End Date")
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable {
                                            workExperience[index] = exp.copy(isRelevant = !exp.isRelevant)
                                        }
                                    ) {
                                        AppCheckbox(
                                            checked = exp.isRelevant,
                                            onCheckedChange = { checked ->
                                                workExperience[index] = exp.copy(isRelevant = checked)
                                            }
                                        )
                                        Text("Currently Working", fontSize = tokens.caption, color = TextSecondary)
                                    }
                                }

                                DatePickerField(
                                    value = exp.toDate,
                                    onDateSelected = { workExperience[index] = exp.copy(toDate = it) },
                                    enabled = !exp.isRelevant
                                )

                                Spacer(Modifier.height(8.dp))

                                FormLabel("Description")
                                FormTextArea(
                                    value = exp.jobDescription,
                                    onValueChange = { workExperience[index] = exp.copy(jobDescription = it) },
                                    placeholder = "Key responsibilities and achievements...",
                                    minLines = 3,
                                    maxLines = 4
                                )
                            }
                        }
                    }
                }
            }

            // ===============================================================
            // 7. EDUCATION DETAILS
            // ===============================================================
            FormSectionCard(
                title = "Education Details",
                action = {
                    OutlinedButton(
                        onClick = { education.add(EducationEntry()) },
                        shape = RoundedCornerShape(tokens.cardCornerRadius),
                        border = BorderStroke(1.dp, Primary),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary),
                        modifier = Modifier.height(tokens.fieldHeight)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(tokens.iconSize))
                        Spacer(Modifier.width(4.dp))
                        Text("Add Education", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium)
                    }
                }
            ) {
                if (education.isEmpty()) {
                    Text(
                        text = "No education details added yet",
                        fontSize = tokens.bodySmall,
                        color = mutedText,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    education.forEachIndexed { index, edu ->
                        Card(
                            shape = RoundedCornerShape(tokens.cardCornerRadius),
                            colors = CardDefaults.cardColors(containerColor = Primary_background),
                            border = BorderStroke(1.dp, sectionBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Education ${index + 1}",
                                        fontSize = tokens.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = title_color
                                    )
                                    IconButton(
                                        onClick = { education.removeAt(index) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = redText, modifier = Modifier.size(18.dp))
                                    }
                                }

                                Spacer(Modifier.height(8.dp))

                                FormLabel("Degree", isRequired = true)
                                FormTextField(
                                    value = edu.degree,
                                    onValueChange = {
                                        education[index] = edu.copy(degree = it)
                                        errors.remove("edu_${index}_degree")
                                    },
                                    placeholder = "e.g. Bachelor of Science",
                                    isError = errors.containsKey("edu_${index}_degree"),
                                    errorMessage = errors["edu_${index}_degree"]
                                )

                                Spacer(Modifier.height(8.dp))

                                FormLabel("Field of Study")
                                FormTextField(
                                    value = edu.specialization,
                                    onValueChange = { education[index] = edu.copy(specialization = it) },
                                    placeholder = "e.g. Computer Science"
                                )

                                Spacer(Modifier.height(8.dp))

                                FormLabel("Institution Name", isRequired = true)
                                FormTextField(
                                    value = edu.instituteName,
                                    onValueChange = {
                                        education[index] = edu.copy(instituteName = it)
                                        errors.remove("edu_${index}_instituteName")
                                    },
                                    placeholder = "e.g. Stanford University",
                                    isError = errors.containsKey("edu_${index}_instituteName"),
                                    errorMessage = errors["edu_${index}_instituteName"]
                                )

                                Spacer(Modifier.height(8.dp))

                                FormLabel("Start Date", isRequired = true)
                                DatePickerField(
                                    value = edu.startDate,
                                    onDateSelected = {
                                        education[index] = edu.copy(startDate = it)
                                        errors.remove("edu_${index}_startDate")
                                    },
                                    isError = errors.containsKey("edu_${index}_startDate")
                                )

                                Spacer(Modifier.height(8.dp))

                                FormLabel("Completion Date")
                                DatePickerField(
                                    value = edu.completionDate,
                                    onDateSelected = { education[index] = edu.copy(completionDate = it) }
                                )

                                Spacer(Modifier.height(8.dp))

                                FormLabel("CGPA")
                                FormTextField(
                                    value = edu.cgpa,
                                    onValueChange = { education[index] = edu.copy(cgpa = it) },
                                    placeholder = "e.g. 8.5",
                                    keyboardType = KeyboardType.Decimal
                                )
                            }
                        }
                    }
                }
            }

            // ===============================================================
            // 8. JOB DETAILS & REGIONAL BRANCH MULTI-SELECT
            // ===============================================================
            FormSectionCard(
                title = "Job Details",
                description = "Organizational hierarchy, reporting managers and shift."
            ) {
                FormLabel("Employee Code")
                FormTextField(
                    value = employeeCode,
                    onValueChange = { employeeCode = it },
                    placeholder = "Auto-generated if left blank"
                )

                Spacer(Modifier.height(tokens.screenPadding * 0.5f))

                FormLabel("Joining Date", isRequired = true)
                DatePickerField(
                    value = doj,
                    onDateSelected = {
                        doj = it
                        errors.remove("doj")
                    },
                    isError = errors.containsKey("doj")
                )

                Spacer(Modifier.height(tokens.screenPadding * 0.5f))

                val selectedDeptName = departments.find { it._id == departmentId }?.name.orEmpty()
                FormDropdown(
                    label = "Department",
                    value = selectedDeptName.ifBlank { "Select department" },
                    expanded = departmentExpanded,
                    onExpandChange = { departmentExpanded = it },
                    options = departments.map { it.name },
                    onOptionSelected = { name ->
                        departmentId = departments.find { it.name == name }?._id.orEmpty()
                        errors.remove("departmentId")
                    },
                    isRequired = true,
                    isError = errors.containsKey("departmentId"),
                    errorMessage = errors["departmentId"]
                )

                Spacer(Modifier.height(tokens.screenPadding * 0.5f))

                val selectedDesigName = designations.find { it.id == designationId }?.name.orEmpty()
                FormDropdown(
                    label = "Designation",
                    value = selectedDesigName.ifBlank { "Select designation" },
                    expanded = designationExpanded,
                    onExpandChange = { designationExpanded = it },
                    options = designations.map { it.name },
                    onOptionSelected = { name ->
                        designationId = designations.find { it.name == name }?.id.orEmpty()
                    }
                )

                Spacer(Modifier.height(tokens.screenPadding * 0.5f))

                // ── BRANCH CONFIGURATION DRIVEN BY ROLE SCOPE ──
                val isRegionalRole = roleScope.equals("regional", ignoreCase = true)
                FormLabel(
                    text = if (isRegionalRole) "Allowed Branches (Multi-Select)" else "Branch",
                    isRequired = isRegionalRole
                )

                when {
                    customRoleId.isBlank() -> {
                        Surface(
                            color = light_grey,
                            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                            border = BorderStroke(1.dp, sectionBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(tokens.fieldHeight)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 12.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = "Please select a role first to configure branch access",
                                    fontSize = tokens.bodySmall,
                                    color = mutedText
                                )
                            }
                        }
                    }

                    isRegionalRole -> {
                        Surface(
                            color = Primary_background,
                            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                            border = BorderStroke(1.dp, if (errors.containsKey("allowedBranches")) redText else sectionBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                branches.forEach { b ->
                                    val isChecked = allowedBranches.contains(b.id)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                if (isChecked) {
                                                    allowedBranches.remove(b.id)
                                                } else {
                                                    b.id.let { allowedBranches.add(it) }
                                                }
                                                branchId = ""
                                                errors.remove("allowedBranches")
                                            }
                                    ) {
                                        AppCheckbox(
                                            checked = isChecked,
                                            onCheckedChange = { checked ->
                                                if (checked) {
                                                    b.id.let { allowedBranches.add(it) }
                                                } else {
                                                    allowedBranches.remove(b.id)
                                                }
                                                branchId = ""
                                                errors.remove("allowedBranches")
                                            }
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = b.name.orEmpty(),
                                            fontSize = tokens.bodySmall,
                                            fontWeight = FontWeight.Medium,
                                            color = TextPrimary
                                        )
                                    }
                                }
                            }
                        }
                        if (errors.containsKey("allowedBranches")) {
                            Text(
                                text = errors["allowedBranches"].orEmpty(),
                                color = redText,
                                fontSize = tokens.label,
                                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                            )
                        }
                    }

                    else -> {
                        val selectedBranchName = branches.find { it.id == branchId }?.name.orEmpty()
                        FormDropdown(
                            value = selectedBranchName.ifBlank { "Select branch" },
                            expanded = branchExpanded,
                            onExpandChange = { branchExpanded = it },
                            options = branches.mapNotNull { it.name },
                            onOptionSelected = { name ->
                                branchId = branches.find { it.name == name }?.id.orEmpty()
                                allowedBranches.clear()
                            }
                        )
                    }
                }

                Spacer(Modifier.height(tokens.screenPadding * 0.5f))

                val selectedShiftName = shifts.find { it.id == shiftId }?.title.orEmpty()
                FormDropdown(
                    label = "Shift",
                    value = selectedShiftName.ifBlank { "Select shift" },
                    expanded = shiftExpanded,
                    onExpandChange = { shiftExpanded = it },
                    options = shifts.map { it.title },
                    onOptionSelected = { title ->
                        shiftId = shifts.find { it.title == title }?.id.orEmpty()
                    }
                )

                Spacer(Modifier.height(tokens.screenPadding * 0.5f))

                val reportingManagerName = members.find { it._id == reportingTo }?.displayName().orEmpty()
                FormDropdown(
                    label = "Reporting Manager",
                    value = reportingManagerName.ifBlank { "Search manager..." },
                    expanded = reportingToExpanded,
                    onExpandChange = { reportingToExpanded = it },
                    options = members.map { it.displayName() },
                    onOptionSelected = { name ->
                        reportingTo = members.find { it.displayName() == name }?._id.orEmpty()
                    }
                )

                Spacer(Modifier.height(tokens.screenPadding * 0.5f))

                val secManagerName = members.find { it._id == secondaryReportingTo }?.displayName().orEmpty()
                FormDropdown(
                    label = "Secondary Manager",
                    value = secManagerName.ifBlank { "Search manager..." },
                    expanded = secondaryReportingToExpanded,
                    onExpandChange = { secondaryReportingToExpanded = it },
                    options = members.map { it.displayName() },
                    onOptionSelected = { name ->
                        secondaryReportingTo = members.find { it.displayName() == name }?._id.orEmpty()
                    }
                )

                Spacer(Modifier.height(tokens.screenPadding * 0.5f))

                FormLabel("Work Location (District)")
                FormTextField(
                    value = workingDistrict,
                    onValueChange = { workingDistrict = it },
                    placeholder = "Enter work location"
                )

                Spacer(Modifier.height(tokens.screenPadding * 0.5f))

                FormLabel("Employment Type")
                Spacer(Modifier.height(6.dp))

                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MEMBER_EMPLOYMENT_TYPES.forEach { type ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { employmentType = type }
                        ) {
                            AppRadioButton(
                                selected = employmentType == type,
                                onClick = { employmentType = type },
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = type.replace("-", " ")
                                    .split(" ")
                                    .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } },
                                fontSize = tokens.caption,
                                color = TextPrimary,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // ===============================================================
            // 9. ACCESS & PERMISSIONS
            // ===============================================================
            FormSectionCard(
                title = "Access & Permissions",
                description = "Assign a role that determines what this employee can see and do."
            ) {
                val selectedRoleName = roles.find { it._id == customRoleId }?.name.orEmpty()
                FormDropdown(
                    label = "Assign Role",
                    value = selectedRoleName.ifBlank { "Select role" },
                    expanded = roleExpanded,
                    onExpandChange = { roleExpanded = it },
                    options = roles.map { it.name },
                    onOptionSelected = { name ->
                        val roleObj = roles.find { it.name == name }
                        val newRoleId = roleObj?._id.orEmpty()
                        customRoleId = newRoleId

                        if (newRoleId.isNotBlank()) {
                            hrViewModel.fetchRoleById(newRoleId)
                        }

                        // Reset branch states to match React logic
                        if (roleScope.equals("regional", ignoreCase = true)) {
                            branchId = ""
                        } else {
                            allowedBranches.clear()
                        }
                        errors.remove("customRoleId")
                    },
                    isRequired = true,
                    isError = errors.containsKey("customRoleId"),
                    errorMessage = errors["customRoleId"]
                )

                Spacer(Modifier.height(tokens.screenPadding * 0.5f))

                Text(
                    text = "Employee portal access and login can be enabled for this employee after the profile is created.",
                    fontSize = tokens.caption,
                    color = mutedText
                )
            }

            Spacer(Modifier.height(tokens.screenPadding))
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Complete Employee Profile Overview (View Mode)
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

    val memberDetail by hrViewModel.memberDetail.collectAsStateWithLifecycle()
    val isLoading by hrViewModel.isLoadingMemberDetail.collectAsStateWithLifecycle()
    val errorMessage by hrViewModel.memberDetailError.collectAsStateWithLifecycle()

    val departmentUiState by departmentViewModel.uiState.collectAsStateWithLifecycle()
    val designationUiState by designationViewModel.uiState.collectAsStateWithLifecycle()

    val departmentList = (departmentUiState as? DepartmentUiState.Success)?.departments ?: emptyList()
    val designationList = (designationUiState as? DesignationUiState.Success)?.items ?: emptyList()

    // Fetches complete employee profile including profilePicture URL from API
    LaunchedEffect(memberId) {
        if (!memberId.isNullOrBlank()) {
            hrViewModel.fetchMemberDetail(memberId)
        }
        departmentViewModel.loadDepartments()
        designationViewModel.loadDesignations()
    }

    DisposableEffect(Unit) {
        onDispose {
            hrViewModel.clearMemberDetail()
        }
    }

    val m = memberDetail

    Scaffold(
        contentWindowInsets = WindowInsets(0,0,0,0),
        containerColor = Color.Transparent,
        topBar = {
            TitleBar(
                title = "Employee Profile",
                onClose = onDismiss
            )
        }
    ) { innerPadding ->
        when {
            isLoading && m == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primary)
                }
            }

            errorMessage != null && m == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(tokens.screenPadding)
                    ) {
                        Text(
                            text = "Failed to load employee profile",
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = redText
                        )
                        Text(
                            text = errorMessage.orEmpty(),
                            fontSize = tokens.bodySmall,
                            color = mutedText
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = {
                                if (!memberId.isNullOrBlank()) {
                                    hrViewModel.fetchMemberDetail(memberId)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Primary)
                        ) {
                            Text("Retry", color = whiteBg)
                        }
                    }
                }
            }

            m != null -> {
                val fullName = listOfNotNull(
                    m.firstName?.takeIf { it.isNotBlank() },
                    m.lastName?.takeIf { it.isNotBlank() }
                ).joinToString(" ").ifBlank { "Employee" }

                val designationName = remember(m, designationList) {
                    when {
                        !m.designationId?.name.isNullOrBlank() -> m.designationId.name
                        else -> designationList.find { it.id == m.designationId?._id }?.name.orEmpty()
                    }
                }

                val departmentName = remember(m, departmentList) {
                    when {
                        !m.departmentId?.name.isNullOrBlank() -> m.departmentId.name
                        else -> departmentList.find { it._id == m.departmentId?._id }?.name.orEmpty()
                    }
                }

                val branchName = m.branchId?.name.orEmpty().ifBlank { m.branchName.orEmpty() }

                val fullPermanentAddress = remember(m) {
                    val addr = m.permanentAddress
                    if (addr != null) {
                        listOfNotNull(
                            addr.flatNo?.takeIf { it.isNotBlank() },
                            addr.street?.takeIf { it.isNotBlank() },
                            addr.areaZone?.takeIf { it.isNotBlank() },
                            addr.city?.takeIf { it.isNotBlank() },
                            (addr.subdivisionName?.takeIf { it.isNotBlank() } ?: addr.state?.takeIf { it.isNotBlank() }),
                            (addr.countryName?.takeIf { it.isNotBlank() } ?: addr.country?.takeIf { it.isNotBlank() }),
                            (addr.pincode?.takeIf { it.isNotBlank() } ?: addr.postalCode?.takeIf { it.isNotBlank() })
                        ).joinToString(", ")
                    } else ""
                }

                val fullTemporaryAddress = remember(m) {
                    val addr = m.temporaryAddress
                    if (!m.hasTemporaryAddress && addr != null) {
                        listOfNotNull(
                            addr.flatNo?.takeIf { it.isNotBlank() },
                            addr.street?.takeIf { it.isNotBlank() },
                            addr.areaZone?.takeIf { it.isNotBlank() },
                            addr.city?.takeIf { it.isNotBlank() },
                            (addr.subdivisionName?.takeIf { it.isNotBlank() } ?: addr.state?.takeIf { it.isNotBlank() }),
                            (addr.countryName?.takeIf { it.isNotBlank() } ?: addr.country?.takeIf { it.isNotBlank() }),
                            (addr.pincode?.takeIf { it.isNotBlank() } ?: addr.postalCode?.takeIf { it.isNotBlank() })
                        ).joinToString(", ")
                    } else ""
                }

                val email = m.personalMail ?: m.email ?: m.userId?.email.orEmpty()
                val phone = m.workMobile ?: m.personalMobile ?: m.userId?.mobile.orEmpty()
                val empCode = m.employeeCode ?: m.memberId.orEmpty()
                val joined = formatDisplayDate(m.doj ?: m.joinedAt)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(tokens.screenPadding),
                    verticalArrangement = Arrangement.spacedBy(tokens.screenPadding)
                ) {
                    // 1. Profile Header Card
                    Surface(
                        shape = RoundedCornerShape(tokens.cardCornerRadius),
                        color = whiteBg,
                        border = BorderStroke(1.dp, sectionBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(tokens.screenPadding),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Avatar Box: Loads profile picture from API response (m.profilePicture)
                            Box(
                                modifier = Modifier.size(88.dp),
                                contentAlignment = Alignment.BottomEnd
                            ) {
                                if (!m.profilePicture.isNullOrBlank()) {
                                    AsyncImage(
                                        model = m.profilePicture,
                                        contentDescription = fullName,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    val initials = fullName.split(" ")
                                        .filter { it.isNotBlank() }
                                        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                                        .take(2).joinToString("")
                                        .ifBlank { "?" }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                            .background(grey_border),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = initials,
                                            fontSize = tokens.bodyLarge,
                                            fontWeight = FontWeight.Medium,
                                            color = title_color
                                        )
                                    }
                                }

                                val status = m.status?.takeIf { it.isNotBlank() }
                                if (status != null) {
                                    val isCurrentlyActive = status.equals("active", ignoreCase = true)
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(if (isCurrentlyActive) greentext else redText)
                                            .border(2.dp, whiteBg, CircleShape)
                                    )
                                }
                            }

                            Spacer(Modifier.height(10.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = fullName,
                                    fontSize = tokens.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = title_color
                                )

                                val status = m.status?.takeIf { it.isNotBlank() }
                                if (status != null) {
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = greenBg
                                    ) {
                                        Text(
                                            text = status.replaceFirstChar { it.uppercase() },
                                            color = greentext,
                                            fontSize = tokens.label,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            val subTitle = listOfNotNull(
                                designationName.takeIf { it.isNotBlank() },
                                departmentName.takeIf { it.isNotBlank() }
                            ).joinToString(" • ")

                            if (subTitle.isNotBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = subTitle,
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = Primary
                                )
                            }

                            Spacer(Modifier.height(tokens.screenPadding * 0.75f))
                            HorizontalDivider(color = sectionBorder)
                            Spacer(Modifier.height(tokens.screenPadding * 0.75f))

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (empCode.isNotBlank()) {
                                    ProfileDetailRow(icon = Icons.Outlined.Badge, text = "Employee ID: $empCode", tokens = tokens)
                                }
                                if (email.isNotBlank()) {
                                    ProfileDetailRow(icon = Icons.Outlined.Mail, text = email, tokens = tokens)
                                }
                                if (phone.isNotBlank()) {
                                    ProfileDetailRow(icon = Icons.Outlined.Phone, text = phone, tokens = tokens)
                                }
                                if (joined.isNotBlank()) {
                                    ProfileDetailRow(icon = Icons.Outlined.DateRange, text = "Joined: $joined", tokens = tokens)
                                }
                            }
                        }
                    }

                    // 2. Action Buttons (Edit and Generate Docs)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(tokens.screenPadding * 0.5f)
                    ) {
                        OutlinedButton(
                            onClick = onEdit,
                            modifier = Modifier
                                .weight(1f)
                                .height(tokens.buttonHeight),
                            shape = RoundedCornerShape(tokens.cardCornerRadius),
                            border = BorderStroke(1.dp, Primary),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary)
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
                            onClick = {
                                DynamicIslandManager.showSuccess("Document generation queued")
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(tokens.buttonHeight),
                            shape = RoundedCornerShape(tokens.cardCornerRadius),
                            border = BorderStroke(1.dp, sectionBorder),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = title_color)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Description,
                                contentDescription = "Generate Docs",
                                modifier = Modifier.size(tokens.iconSize),
                                tint = mutedText
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Generate Docs",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = TextSecondary
                            )
                        }
                    }

                    // 3. Personal Information Card
                    ProfileInfoCard(
                        icon = Icons.Outlined.Person,
                        title = "Personal Information",
                        tokens = tokens
                    ) {
                        ProfileGrid(tokens = tokens) {
                            ProfileGridItem(label = "First Name", value = m.firstName.orEmpty().ifBlank { "—" }, tokens = tokens)
                            ProfileGridItem(label = "Last Name", value = m.lastName.orEmpty().ifBlank { "—" }, tokens = tokens)
                            ProfileGridItem(label = "Date of Birth", value = formatDisplayDate(m.dob).ifBlank { "—" }, tokens = tokens)
                            ProfileGridItem(label = "Gender", value = m.gender?.replaceFirstChar { it.uppercase() } ?: "—", tokens = tokens)
                            ProfileGridItem(label = "Marital Status", value = m.martialStatus?.replaceFirstChar { it.uppercase() } ?: "—", tokens = tokens)
                            ProfileGridItem(label = "Blood Group", value = m.bloodGroup.orEmpty().ifBlank { "—" }, tokens = tokens)
                            ProfileGridItem(label = "Emergency Contact Name", value = m.emergencyContactName.orEmpty().ifBlank { "—" }, tokens = tokens)
                            ProfileGridItem(label = "Emergency Contact Phone", value = m.emergencyContactMobile.orEmpty().ifBlank { "—" }, tokens = tokens)
                        }

                        if (fullPermanentAddress.isNotBlank()) {
                            Spacer(Modifier.height(10.dp))
                            Text("Permanent Address", fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = mutedText)
                            Spacer(Modifier.height(2.dp))
                            Text(fullPermanentAddress, fontSize = tokens.bodySmall, color = title_color)
                        }

                        if (!m.hasTemporaryAddress && fullTemporaryAddress.isNotBlank()) {
                            Spacer(Modifier.height(10.dp))
                            Text("Temporary Address", fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = mutedText)
                            Spacer(Modifier.height(2.dp))
                            Text(fullTemporaryAddress, fontSize = tokens.bodySmall, color = title_color)
                        }
                    }

                    // 4. Identity & Statutory IDs Card
                    ProfileInfoCard(
                        icon = Icons.Outlined.VerifiedUser,
                        title = "Identity & Statutory IDs",
                        tokens = tokens
                    ) {
                        ProfileGrid(tokens = tokens) {
                            ProfileGridItem(label = "Aadhaar Number", value = m.aadhaarNo.orEmpty().ifBlank { "—" }, tokens = tokens)
                            ProfileGridItem(label = "PAN Number", value = m.panNo.orEmpty().ifBlank { "—" }, tokens = tokens)
                            ProfileGridItem(label = "Passport Number", value = m.passportNo.orEmpty().ifBlank { "—" }, tokens = tokens)
                            ProfileGridItem(label = "UAN Number (EPF)", value = m.uanNo.orEmpty().ifBlank { "—" }, tokens = tokens)
                            ProfileGridItem(label = "ESIC Number", value = m.esicNumber.orEmpty().ifBlank { "—" }, tokens = tokens)
                            ProfileGridItem(label = "PF Account Number", value = m.pfAccountNo.orEmpty().ifBlank { "—" }, tokens = tokens)
                            ProfileGridItem(label = "Pay Frequency", value = m.payFrequency?.replaceFirstChar { it.uppercase() } ?: "—", tokens = tokens)
                        }
                    }

                    // 5. Bank Account Details Card
                    ProfileInfoCard(
                        icon = Icons.Outlined.AccountBalance,
                        title = "Bank Account Details",
                        tokens = tokens
                    ) {
                        ProfileGrid(tokens = tokens) {
                            ProfileGridItem(label = "Account Holder Name", value = m.accountHolderName.orEmpty().ifBlank { "—" }, tokens = tokens)
                            ProfileGridItem(label = "Account Number", value = m.accountNumber.orEmpty().ifBlank { "—" }, tokens = tokens)
                            ProfileGridItem(label = "IFSC Code", value = m.ifscCode.orEmpty().ifBlank { "—" }, tokens = tokens)
                            ProfileGridItem(label = "Bank Name", value = m.bankName.orEmpty().ifBlank { "—" }, tokens = tokens)
                            ProfileGridItem(label = "Branch Name", value = m.branchName.orEmpty().ifBlank { "—" }, tokens = tokens)
                        }
                    }

                    // 6. Job Details Card
                    ProfileInfoCard(
                        icon = Icons.Outlined.Work,
                        title = "Job Details",
                        tokens = tokens
                    ) {
                        ProfileGrid(tokens = tokens) {
                            ProfileGridItem(label = "Branch", value = branchName.ifBlank { "—" }, tokens = tokens)
                            ProfileGridItem(label = "Department", value = departmentName.ifBlank { "—" }, tokens = tokens)
                            ProfileGridItem(label = "Designation", value = designationName.ifBlank { "—" }, tokens = tokens)
                            ProfileGridItem(label = "Role", value = m.customRoleId?.name ?: m.role?.replaceFirstChar { it.uppercase() } ?: "—", tokens = tokens)
                            ProfileGridItem(label = "Employment Type", value = m.employmentType?.replace("-", " ")?.split(" ")?.joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } } ?: "—", tokens = tokens)
                            ProfileGridItem(label = "Work Location (District)", value = m.workingDistrict.orEmpty().ifBlank { "—" }, tokens = tokens)
                        }
                    }

                    // 7. PF / Gratuity Nominees Card
                    if (m.pfGratuityNominees.isNotEmpty()) {
                        ProfileInfoCard(
                            icon = Icons.Outlined.People,
                            title = "PF / Gratuity Nominees",
                            tokens = tokens
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                m.pfGratuityNominees.forEachIndexed { idx, nom ->
                                    Card(
                                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.75f),
                                        colors = CardDefaults.cardColors(containerColor = whiteBg),
                                        border = BorderStroke(1.dp, sectionBorder),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = nom.name.orEmpty().ifBlank { "Nominee ${idx + 1}" },
                                                    fontSize = tokens.bodySmall,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = title_color
                                                )
                                                Text(
                                                    text = "Relationship: ${nom.relationship.orEmpty().ifBlank { "—" }}",
                                                    fontSize = tokens.caption,
                                                    color = mutedText
                                                )
                                            }
                                            Surface(
                                                color = primary_light,
                                                shape = RoundedCornerShape(20.dp)
                                            ) {
                                                Text(
                                                    text = "${nom.share ?: 0}%",
                                                    fontSize = tokens.bodySmall,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Primary,
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 8. Work Experience Card
                    if (m.workExperience.isNotEmpty()) {
                        ProfileInfoCard(
                            icon = Icons.Outlined.Work,
                            title = "Work Experience",
                            tokens = tokens
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(tokens.screenPadding * 0.6f)) {
                                m.workExperience.forEach { exp ->
                                    Card(
                                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.75f),
                                        colors = CardDefaults.cardColors(containerColor = whiteBg),
                                        border = BorderStroke(1.dp, sectionBorder),
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
                                                            .background(primary_light),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Outlined.LocationCity,
                                                            contentDescription = null,
                                                            tint = Primary,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                    }
                                                    Column {
                                                        Text(
                                                            text = exp.companyName.orEmpty(),
                                                            fontSize = tokens.bodySmall,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = title_color
                                                        )
                                                        Text(
                                                            text = exp.jobTitle.orEmpty(),
                                                            fontSize = tokens.caption,
                                                            color = mutedText
                                                        )
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
                                                        color = Primary
                                                    )
                                                }
                                            }

                                            if (!exp.jobDescription.isNullOrBlank()) {
                                                Spacer(Modifier.height(8.dp))
                                                Text(
                                                    text = exp.jobDescription,
                                                    fontSize = tokens.caption,
                                                    color = TextSecondary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 9. Education Details Card
                    if (m.education.isNotEmpty()) {
                        ProfileInfoCard(
                            icon = Icons.Outlined.School,
                            title = "Education Details",
                            tokens = tokens
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(tokens.screenPadding * 0.6f)) {
                                m.education.forEach { edu ->
                                    Card(
                                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.75f),
                                        colors = CardDefaults.cardColors(containerColor = whiteBg),
                                        border = BorderStroke(1.dp, sectionBorder),
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
                                                        .background(primary_light),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Outlined.School,
                                                        contentDescription = null,
                                                        tint = Primary,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                                Column {
                                                    val degreeLine = listOfNotNull(edu.degree?.takeIf { it.isNotBlank() }, edu.specialization?.takeIf { it.isNotBlank() }).joinToString(" in ")
                                                    if (degreeLine.isNotBlank()) {
                                                        Text(
                                                            text = degreeLine,
                                                            fontSize = tokens.bodySmall,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = title_color
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
                                                            color = mutedText
                                                        )
                                                    }
                                                }
                                            }

                                            if (edu.cgpa != null) {
                                                Column(horizontalAlignment = Alignment.End) {
                                                    Text("Grade", fontSize = tokens.caption, color = mutedText)
                                                    Text("${edu.cgpa} GPA", fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = Primary)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(tokens.screenPadding))
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Reusable Components & Helper Views
// ---------------------------------------------------------------------------

@Composable
fun FormSectionCard(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    action: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val tokens = LocalAppTokens.current

    Surface(
        color = whiteBg,
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        border = BorderStroke(1.dp, sectionBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(tokens.screenPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = title_color
                    )
                    if (!description.isNullOrBlank()) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = description,
                            fontSize = tokens.caption,
                            color = mutedText
                        )
                    }
                }
                action?.invoke()
            }

            Spacer(Modifier.height(tokens.screenPadding))
            content()
        }
    }
}

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
            tint = mutedText,
            modifier = Modifier.size(tokens.iconSize * 0.9f)
        )
        Text(
            text = text,
            fontSize = tokens.bodySmall,
            color = TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ProfileInfoCard(
    icon: ImageVector,
    title: String,
    tokens: AppDesignTokens,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        color = whiteBg,
        border = BorderStroke(1.dp, sectionBorder),
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
                    imageVector = icon,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(tokens.iconSize)
                )
                Text(
                    text = title,
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = title_color
                )
            }
            Spacer(Modifier.height(tokens.screenPadding * 0.75f))
            HorizontalDivider(color = sectionBorder)
            Spacer(Modifier.height(tokens.screenPadding * 0.75f))
            content()
        }
    }
}

@Composable
private fun ProfileGrid(
    tokens: AppDesignTokens,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                color = mutedText
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.Normal,
                color = title_color
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Format Utilities
// ---------------------------------------------------------------------------

private fun toApiDate(displayDate: String): String {
    if (displayDate.isBlank()) return ""
    return try {
        val formats = listOf("dd-MM-yyyy", "dd MMM yyyy", "dd/MM/yyyy", "yyyy-MM-dd")
        for (format in formats) {
            try {
                val input = SimpleDateFormat(format, Locale.getDefault())
                val output = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                return output.format(input.parse(displayDate)!!)
            } catch (_: Exception) {}
        }
        displayDate
    } catch (_: Exception) {
        displayDate
    }
}

private fun formatDateForDisplay(isoDate: String?): String {
    if (isoDate.isNullOrBlank()) return ""
    return try {
        val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val output = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        output.format(input.parse(isoDate)!!)
    } catch (_: Exception) {
        try {
            val input = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val output = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            output.format(input.parse(isoDate)!!)
        } catch (_: Exception) {
            isoDate
        }
    }
}

private fun formatDisplayDate(dateStr: String?): String {
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

private fun formatYearOnly(dateStr: String?): String {
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

fun uriToFile(context: Context, uri: Uri): File {
    val inputStream = context.contentResolver.openInputStream(uri)
    val file = File(context.cacheDir, "profile_${System.currentTimeMillis()}.jpg")
    inputStream?.use { input ->
        file.outputStream().use { output -> input.copyTo(output) }
    }
    return file
}