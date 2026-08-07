@file:Suppress(
    "UNUSED_VALUE",
    "UNUSED_PARAMETER",
    "unused",
    "UNCHECKED_CAST",
    "DEPRECATION",
    "AssignedValueIsNeverRead",
    "GrazieInspection",
    "SpellCheckingInspection",
    "unusedvariable",
    "AssignedVariableIsNeverRead",
    "UNUSED_VARIABLE",
    "KotlinConstantConditions",
    "VariableNeverRead"
)

package com.cuso.mobile.view.home

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.cuso.mobile.model.UpdateOrganizationRequest
import com.cuso.mobile.model.UpdateOrganizationSettings
import com.cuso.mobile.ui.theme.Primary
import com.cuso.mobile.ui.theme.Primary_background
import com.cuso.mobile.ui.theme.TextSecondary
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.CirculerProgressIndicatorSmall
import com.cuso.mobile.view.composable.CountryAndStatePicker
import com.cuso.mobile.view.composable.DynamicIslandError
import com.cuso.mobile.view.composable.DynamicIslandSuccess
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.view.composable.SettingsTabs
import com.cuso.mobile.view.composable.TabItem
import com.cuso.mobile.view.organization.OrgLabel
import com.cuso.mobile.view.organization.OrgOptions
import com.cuso.mobile.view.organization.OrgOptions.companySizes
import com.cuso.mobile.viewmodel.Authenticate
import com.cuso.mobile.viewmodel.ProfileUiState
import com.cuso.mobile.viewmodel.ProfileViewModel
import com.cuso.mobile.viewmodel.UpdateOrgUiState
import kotlin.math.roundToInt
import java.io.File

// ─────────────────────────────────────────────────────────────
// Design tokens
// ─────────────────────────────────────────────────────────────
private object OrgTheme {
    val PrimaryLight = Color(0xFFEEF0FF)
    val TextPrimary = Color(0xFF111827)
    val TextSecondary = Color(0xFF6B7280)
    val mutedText = Color(0xFF9CA3AF)
    val Border = Color(0xFFE5E7EB)
    val Divider = Color(0xFFF0F0F0)
    val PageBg = Color(0xFFF5F5F5)
    val InputBg = whiteBg
}

@Suppress("UNUSED_PARAMETER")
@Composable
fun SettingsScreen(
    navController: NavController,
    onMenuClick: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    // ── Define tabs with icons ──
    val tabs = listOf(
        TabItem(
            label = "Profile",
            icon = Icons.Outlined.Badge
        ),
        TabItem(
            label = "Localization",
            icon = Icons.Outlined.Business
        )
    )

    val authViewModel: Authenticate = hiltViewModel()
    val tokensEntity by authViewModel.tokens.collectAsStateWithLifecycle()
    val token = tokensEntity?.accessToken ?: ""

    // ── Dynamic Island States ──
    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary_background)
    ) {
        // ── Header ──
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                TitleBar("Organization Setup", onClose = onBack)
            }
        }

        // ── Reusable Tabs ──
        SettingsTabs(
            tabs = tabs,
            selectedIndex = selectedTab,
            onTabSelected = { selectedTab = it },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            containerColor = whiteBg,
            selectedBackgroundColor = Color(0xFFEEF0FF),
            selectedTextColor = Primary,
            unselectedTextColor = TextSecondary,
            selectedIconColor = Primary,
            unselectedIconColor = TextSecondary,
            borderColor = Color(0xFFE5E7EB),
            cornerRadius = 12.dp,
            selectedCornerRadius = 10.dp
        )

        when (selectedTab) {
            0 -> ProfileTab(
                token = token,
                onSuccess = { message -> successMessage = message },
                onError = { message -> errorMessage = message }
            )
            1 -> LocalizationTab(
                token = token,
                onSuccess = { message -> successMessage = message },
                onError = { message -> errorMessage = message }
            )
        }
    }

    // ── Dynamic Island Overlays ──
    Box(modifier = Modifier.fillMaxSize()) {
        DynamicIslandSuccess(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(10f),
            message = successMessage,
            onDismiss = { successMessage = null }
        )

        DynamicIslandError(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(10f),
            message = errorMessage,
            onDismiss = { errorMessage = null }
        )
    }
}

// ─────────────────────────────────────────────────────────────
// Section header
// ─────────────────────────────────────────────────────────────
@Composable
private fun SectionHeader(
    title: String,
    subtitle: String,
    isEditing: Boolean,
    onEditClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(whiteBg)
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = OrgTheme.TextPrimary)
            Spacer(Modifier.height(2.dp))
            Text(subtitle, fontSize = 13.sp, color = TextSecondary)
        }
        if (!isEditing) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onEditClick() }
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = Primary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text("Edit", color = Primary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Read-only field row
// ─────────────────────────────────────────────────────────────
@Composable
fun OrgInfoRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = OrgTheme.mutedText)
        Text(text = value.ifEmpty { "-" }, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = OrgTheme.TextPrimary)
    }
}

// ─────────────────────────────────────────────────────────────
// Editable field - Uses FormTextField from Home page
// ─────────────────────────────────────────────────────────────
@Composable
fun EditableOrgInfoRow(
    label: String,
    value: String,
    isMultiline: Boolean = false,
    onValueChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (label.isNotEmpty()) {
            FormLabel(label)
        }
        FormTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = if (label.isNotEmpty()) "Enter $label" else "Enter value"
        )
    }
}

// ─────────────────────────────────────────────────────────────
// Logo upload (view mode)
// ─────────────────────────────────────────────────────────────
@Composable
private fun LogoDisplayView(pictureUrl: String?) {
    Box(
        modifier = Modifier
            .size(96.dp)
            .border(1.5.dp, OrgTheme.Border, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (!pictureUrl.isNullOrBlank()) {
            AsyncImage(
                model = pictureUrl,
                contentDescription = "Organization Logo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Business, contentDescription = null, tint = OrgTheme.mutedText, modifier = Modifier.size(32.dp))
                Spacer(Modifier.height(4.dp))
                Text("No Logo", color = OrgTheme.mutedText, fontSize = 12.sp)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Logo upload (edit mode)
// ─────────────────────────────────────────────────────────────
@Composable
private fun LogoDisplayEdit(
    pictureUrl: String?,
    selectedImageUri: Uri?,
    onClick: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .border(1.5.dp, Primary.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                .clip(RoundedCornerShape(10.dp))
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            when {
                selectedImageUri != null -> {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "Organization Logo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                !pictureUrl.isNullOrBlank() -> {
                    AsyncImage(
                        model = pictureUrl,
                        contentDescription = "Organization Logo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                else -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.height(2.dp))
                        Text("Upload Logo", color = Primary, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text("Organization Logo", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = OrgTheme.TextPrimary)
            Spacer(Modifier.height(2.dp))
            Text(
                "Recommended size: 200*200px, PNG or JPG",
                fontSize = 12.sp,
                color = OrgTheme.TextSecondary
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Fixed bottom footer
// ─────────────────────────────────────────────────────────────
@Composable
private fun EditFooter(
    isSaving: Boolean,
    onCancel: () -> Unit,
    onSave: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(whiteBg)
            .border(width = 1.dp, color = OrgTheme.Divider)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onCancel,
            enabled = !isSaving,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = OrgTheme.TextSecondary),
            modifier = Modifier.weight(1f).height(46.dp)
        ) {
            Text("Cancel", fontSize = 15.sp)
        }
        Button(
            onClick = onSave,
            enabled = !isSaving,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
            modifier = Modifier.weight(2f).height(46.dp)
        ) {
            if (isSaving) {
                CirculerProgressIndicatorSmall()
            } else {
                Text("Save Changes", color = whiteBg, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Helper: Convert picked Uri -> File
// ─────────────────────────────────────────────────────────────
private fun uriToFile(context: Context, uri: Uri): File? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val tempFile = File.createTempFile("org_logo_", ".jpg", context.cacheDir)
        tempFile.outputStream().use { output -> inputStream.copyTo(output) }
        inputStream.close()
        tempFile
    } catch (_: Exception) {
        null
    }
}

// ─────────────────────────────────────────────────────────────
// ProfileTab
// ─────────────────────────────────────────────────────────────
@Composable
fun ProfileTab(
    token: String,
    onSuccess: (String) -> Unit = {},
    onError: (String) -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isEditing by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()
    val updateState by viewModel.updateState.collectAsState()

    var orgName by remember { mutableStateOf("") }
    var orgType by remember { mutableStateOf("") }
    var businessType by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
        }
    }

    LaunchedEffect(token) {
        if (token.isNotEmpty()) viewModel.loadOrganization(token)
    }

    LaunchedEffect(uiState) {
        if (uiState is ProfileUiState.Success) {
            val org = (uiState as ProfileUiState.Success).data.organization
            orgName = org.name
            orgType = org.orgType
            businessType = org.businessType
            email = org.email
            mobile = org.mobile
        }
    }

    LaunchedEffect(updateState) {
        when (val state = updateState) {
            is UpdateOrgUiState.Success -> {
                isEditing = false
                selectedImageUri = null
                viewModel.resetUpdateState()
                onSuccess(state.message)
            }
            is UpdateOrgUiState.Error -> {
                viewModel.resetUpdateState()
                onError(state.message)
            }
            else -> Unit
        }
    }

    val isSaving = updateState is UpdateOrgUiState.Loading

    when (val state = uiState) {
        is ProfileUiState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CirculerProgressIndicatorSmall()
            }
        }

        is ProfileUiState.Error -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(state.message, color = Color.Red)
            }
        }

        is ProfileUiState.Success -> {
            val org = state.data.organization
            val stats = state.data.stats

            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f)) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Primary_background),
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(whiteBg)
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                SectionHeader(
                                    title = "Organization Information",
                                    subtitle = "Core details about your organization",
                                    isEditing = isEditing,
                                    onEditClick = { isEditing = true }
                                )
                            }
                            Spacer(Modifier.height(10.dp))

                            Column(
                                Modifier.fillMaxWidth()
                                    .padding(horizontal = 12.dp)
                                    .background(Primary_background)
                            ) {
                                if (isEditing) {
                                    LogoDisplayEdit(
                                        pictureUrl = org.organizationPicture,
                                        selectedImageUri = selectedImageUri,
                                        onClick = {
                                            imagePickerLauncher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        }
                                    )
                                    Spacer(Modifier.height(20.dp))

                                    EditableOrgInfoRow("Organization Name", orgName) {
                                        orgName = it
                                    }
                                    Spacer(Modifier.height(16.dp))

                                    // ── Organization Type - Using FormDropdown with OrgOptions.orgTypes ──
                                    OrgLabel("Organization Type")
                                    var orgTypeExpanded by remember { mutableStateOf(false) }
                                    FormDropdown(
                                        value = orgType,
                                        expanded = orgTypeExpanded,
                                        onExpandChange = { orgTypeExpanded = it },
                                        options = OrgOptions.orgTypes,
                                        onOptionSelected = { orgType = it },
                                        isRequired = true
                                    )
                                    Spacer(Modifier.height(16.dp))

                                    // ── Business Type - Using FormDropdown with OrgOptions.businessTypes ──
                                    OrgLabel("Business Type")
                                    var businessTypeExpanded by remember { mutableStateOf(false) }
                                    FormDropdown(
                                        value = businessType,
                                        expanded = businessTypeExpanded,
                                        onExpandChange = { businessTypeExpanded = it },
                                        options = OrgOptions.businessTypes,
                                        onOptionSelected = { businessType = it },
                                        isRequired = true
                                    )

                                    Spacer(Modifier.height(24.dp))
                                } else {
                                    LogoDisplayView(org.organizationPicture)
                                    Spacer(Modifier.height(20.dp))

                                    val rows = listOf(
                                        "Organization Name" to org.name,
                                        "Organization Type" to org.orgType,
                                        "Business Type" to org.businessType,
                                        "Business ID" to org.businessId,
                                        "Email" to org.email,
                                        "Mobile" to org.mobile,
                                        "Plan " to (org.plan?.name ?: "plan not found"),
                                        "Status" to org.status
                                    )
                                    rows.forEachIndexed { index, (label, value) ->
                                        OrgInfoRow(label, value)
                                        Spacer(Modifier.height(14.dp))
                                        if (index != rows.lastIndex) {
                                            HorizontalDivider(color = OrgTheme.Divider)

                                        }
                                    }


                                }

                            }
                        }
                        item {
                            Column(
                                Modifier.fillMaxWidth()
                            ) {
                                if (stats != null) {
                                    Spacer(Modifier.height(8.dp))
                                    HorizontalDivider(color = OrgTheme.Divider)

                                    val plan = state.data.organization.plan
                                    Row(
                                        Modifier.fillMaxWidth()
                                            .background(whiteBg)
                                            .padding(horizontal = 20.dp, vertical = 10.dp)
                                    ) {
                                        Text(
                                            "Subscription Usage",
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = OrgTheme.TextPrimary
                                        )
                                    }
                                    Spacer(Modifier.height(16.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        SubscriptionRing(
                                            modifier = Modifier.weight(1f),
                                            label = "Orders",
                                            used = 0,
                                            limit = plan?.orderLimit ?: 100
                                        )
                                        SubscriptionRing(
                                            modifier = Modifier.weight(1f),
                                            label = "Employees",
                                            used = org.activeMembers,
                                            limit = plan?.employeeLimit
                                                ?: org.totalMembers.coerceAtLeast(1)
                                        )
                                        SubscriptionRing(
                                            modifier = Modifier.weight(1f),
                                            label = "Branches",
                                            used = stats.totalBranches,
                                            limit = plan?.branchLimit
                                                ?: stats.totalBranches.coerceAtLeast(1)
                                        )
                                    }
                                    Spacer(Modifier.height(35.dp))
                                } else {
                                    Spacer(Modifier.height(8.dp))
                                }
                            }
                        }
                    }
                }

                if (isEditing) {
                    EditFooter(
                        isSaving = isSaving,
                        onCancel = {
                            isEditing = false
                            selectedImageUri = null
                            viewModel.resetUpdateState()
                        },
                        onSave = {
                            val logoFile = selectedImageUri?.let { uriToFile(context, it) }
                            val request = UpdateOrganizationRequest(
                                name = orgName,
                                orgType = orgType,
                                businessType = businessType,
                                email = email,
                                mobile = mobile
                            )
                            viewModel.updateOrganization(token, request, logoFile)
                        }
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// LocalizationTab
// ─────────────────────────────────────────────────────────────
@Composable
fun LocalizationTab(
    token: String,
    onSuccess: (String) -> Unit = {},
    onError: (String) -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    var isEditing by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()
    val updateState by viewModel.updateState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var country by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var postalCode by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var timezone by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf("") }
    var language by remember { mutableStateOf("") }
    var companySize by rememberSaveable { mutableStateOf("") }
    var portalName by remember { mutableStateOf("") }

    LaunchedEffect(token) {
        if (token.isNotEmpty()) viewModel.loadOrganization(token)
    }

    LaunchedEffect(uiState) {
        if (uiState is ProfileUiState.Success) {
            val settings = (uiState as ProfileUiState.Success).data.organization.settings
            country = settings.country
            state = settings.state
            city = settings.city
            postalCode = settings.pincode
            address = settings.address
            timezone = settings.timezone
            currency = settings.currency
            language = settings.language
            portalName = settings.portalName

            val org = (uiState as ProfileUiState.Success).data.organization
            val totalMembers = org.totalMembers
            companySize = when {
                totalMembers <= 10 -> "1-10 employees"
                totalMembers <= 50 -> "11-50 employees"
                totalMembers <= 200 -> "51-200 employees"
                totalMembers <= 500 -> "201-500 employees"
                else -> "500+ employees"
            }
        }
    }

    LaunchedEffect(updateState) {
        when (val state = updateState) {
            is UpdateOrgUiState.Success -> {
                isEditing = false
                viewModel.resetUpdateState()
                onSuccess(state.message)
            }
            is UpdateOrgUiState.Error -> {
                viewModel.resetUpdateState()
                onError(state.message)
            }
            else -> Unit
        }
    }

    val isLoading = uiState is ProfileUiState.Loading
    val isSaving = updateState is UpdateOrgUiState.Loading
    val org = (uiState as? ProfileUiState.Success)?.data?.organization
    val settings = org?.settings
    val errorMessage = (uiState as? ProfileUiState.Error)?.message

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Primary_background)
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(whiteBg)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        SectionHeader(
                            title = "Localization Settings",
                            subtitle = "Regional preferences for your organization",
                            isEditing = isEditing,
                            onEditClick = { isEditing = true }
                        )
                    }
                    Spacer(Modifier.height(18.dp))

                    Column(
                        Modifier.fillMaxWidth()
                            .padding(horizontal = 12.dp)
                            .background(Primary_background)
                    ) {
                        when {
                            isLoading -> {
                                Box(
                                    Modifier.fillMaxWidth().padding(vertical = 24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CirculerProgressIndicatorSmall()
                                }
                            }

                            errorMessage != null -> {
                                Text(
                                    errorMessage,
                                    color = Color.Red,
                                    modifier = Modifier.padding(vertical = 16.dp)
                                )
                            }

                            settings != null -> {
                                if (isEditing) {
                                    CountryAndStatePicker(
                                        selectedCountry = country,
                                        selectedState = state,
                                        onCountryChange = { country = it },
                                        onStateChange = { state = it }
                                    )
                                    Spacer(Modifier.height(16.dp))

                                    EditableOrgInfoRow("City", city) { city = it }
                                    Spacer(Modifier.height(16.dp))

                                    EditableOrgInfoRow("Postal Code", postalCode) {
                                        postalCode = it
                                    }
                                    Spacer(Modifier.height(16.dp))

                                    EditableOrgInfoRow(
                                        "Address",
                                        address,
                                        isMultiline = true
                                    ) { address = it }
                                    Spacer(Modifier.height(16.dp))

                                    // ── Timezone - Using FormDropdown with OrgOptions.timezones ──
                                    OrgLabel("Timezone")
                                    var timezoneExpanded by remember { mutableStateOf(false) }
                                    FormDropdown(
                                        value = timezone,
                                        expanded = timezoneExpanded,
                                        onExpandChange = { timezoneExpanded = it },
                                        options = OrgOptions.timezones,
                                        onOptionSelected = { timezone = it }
                                    )
                                    Spacer(Modifier.height(16.dp))

                                    // ── Currency - Using FormDropdown with OrgOptions.currencies ──
                                    OrgLabel("Default Currency")
                                    var currencyExpanded by remember { mutableStateOf(false) }
                                    FormDropdown(
                                        value = currency,
                                        expanded = currencyExpanded,
                                        onExpandChange = { currencyExpanded = it },
                                        options = OrgOptions.currencies,
                                        onOptionSelected = { currency = it }
                                    )
                                    Spacer(Modifier.height(16.dp))

                                    // ── Language - Using FormDropdown with OrgOptions.languages ──
                                    OrgLabel("Language")
                                    var languageExpanded by remember { mutableStateOf(false) }
                                    FormDropdown(
                                        value = language,
                                        expanded = languageExpanded,
                                        onExpandChange = { languageExpanded = it },
                                        options = OrgOptions.languages,
                                        onOptionSelected = { language = it }
                                    )
                                    Spacer(Modifier.height(16.dp))

                                    EditableOrgInfoRow("Portal Name", portalName) {
                                        portalName = it
                                    }
                                    Spacer(Modifier.height(16.dp))

                                    // ── Company Size - Using FormDropdown with OrgOptions.companySizes ──
                                    OrgLabel("Company Size")
                                    var companySizeExpanded by remember { mutableStateOf(false) }
                                    FormDropdown(
                                        value = companySize,
                                        expanded = companySizeExpanded,
                                        onExpandChange = { companySizeExpanded = it },
                                        options = companySizes,
                                        onOptionSelected = { companySize = it }
                                    )
                                    Spacer(Modifier.height(24.dp))
                                } else {
                                    val rows = listOf(
                                        "Country" to settings.country,
                                        "State" to settings.state,
                                        "City" to settings.city,
                                        "Postal Code" to settings.pincode,
                                        "Address" to settings.address,
                                        "Timezone" to settings.timezone,
                                        "Default Currency" to settings.currency,
                                        "Language" to settings.language,
                                        "Portal Name" to settings.portalName,
                                        "Company Size" to settings.companySize
                                    )
                                    rows.forEachIndexed { index, (label, value) ->
                                        OrgInfoRow(label, value)
                                        Spacer(Modifier.height(14.dp))
                                        if (index != rows.lastIndex) {
                                            HorizontalDivider(color = OrgTheme.Divider)
                                            Spacer(Modifier.height(14.dp))
                                        }
                                    }
                                    Spacer(Modifier.height(8.dp))
                                }
                            }

                            else -> {
                                Text(
                                    "No data available",
                                    color = Color.Gray,
                                    modifier = Modifier.padding(vertical = 16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (isEditing) {
            EditFooter(
                isSaving = isSaving,
                onCancel = {
                    isEditing = false
                    viewModel.resetUpdateState()
                },
                onSave = {
                    val request = UpdateOrganizationRequest(
                        settings = UpdateOrganizationSettings(
                            country = country,
                            state = state,
                            city = city,
                            pincode = postalCode,
                            address = address,
                            timezone = timezone,
                            currency = currency,
                            language = language,
                            portalName = portalName,
                            companySize = companySize
                        )
                    )
                    viewModel.updateOrganization(token, request)
                }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Subscription Ring
// ─────────────────────────────────────────────────────────────
@Composable
fun SubscriptionRing(
    modifier: Modifier = Modifier,
    label: String,
    used: Int,
    limit: Int
) {
    val pct = if (limit > 0) (used.toFloat() / limit) else 0f
    val remaining = (limit - used).coerceAtLeast(0)
    val ringColor = when {
        pct >= 1f    -> Color(0xFFE24B4A)
        pct >= 0.75f -> Color(0xFFEDA100)
        else         -> Color(0xFF2A78D6)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(90.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val stroke = 10.dp.toPx()
                val radius = (size.minDimension - stroke) / 2
                val topLeft = Offset(stroke / 2, stroke / 2)
                val arcSize = Size(radius * 2, radius * 2)
                drawArc(
                    color = Color(0xFFE1E0D9),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
                if (pct > 0f) {
                    drawArc(
                        color = ringColor,
                        startAngle = -90f,
                        sweepAngle = 360f * pct.coerceAtMost(1f),
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                }
            }
            Text(
                text = "${(pct * 100).roundToInt()}%",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = OrgTheme.TextPrimary
            )
        }
        Text(text = label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = OrgTheme.TextPrimary)
        Text(text = "$used / $limit", fontSize = 12.sp, color = OrgTheme.TextSecondary)
        Text(text = "$remaining remaining", fontSize = 12.sp, color = OrgTheme.mutedText)
    }
}