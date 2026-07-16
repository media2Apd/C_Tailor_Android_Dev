@file:Suppress("UNUSED_VALUE", "ASSIGNED_VALUE_IS_NEVER_READ")

package com.cuso.mobile.view.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.cuso.mobile.model.UpdateOrganizationRequest
import com.cuso.mobile.model.UpdateOrganizationSettings
import com.cuso.mobile.view.composable.CountryAndStatePicker
import com.cuso.mobile.view.organization.OrgLabel
import com.cuso.mobile.view.organization.OrgOptions
import com.cuso.mobile.view.organization.OrgOptions.companySizes
import com.cuso.mobile.view.organization.OrganizationDropdown
import com.cuso.mobile.viewmodel.Authenticate
import com.cuso.mobile.viewmodel.ProfileUiState
import com.cuso.mobile.viewmodel.ProfileViewModel
import com.cuso.mobile.viewmodel.UpdateOrgUiState
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.ui.platform.LocalContext
import com.cuso.mobile.view.composable.CirculerProgressIndicatorReuse
//import okhttp3.MediaType.Companion.toMediaTypeOrNull
//import okhttp3.MultipartBody
//import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

@Suppress("UNUSED_PARAMETER")
@Composable
fun SettingsScreen(
     navController: NavController,
     onMenuClick: () -> Unit = {},
    onBack: () -> Unit = {}   // ✅ NEW
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        Pair(Icons.Outlined.Badge, "Profile"),
        Pair(Icons.Outlined.Business, "Localization")
    )

    val authViewModel: Authenticate = hiltViewModel()
    val tokensEntity by authViewModel.tokens.collectAsStateWithLifecycle()
    val token = tokensEntity?.accessToken ?: ""

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // ── FIXED TOP HEADER (matches Branch/Department/Designation pattern) ──
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier
                            .size(22.dp)
                            .clickable { onBack() },
                        tint = Color(0xFF111827)
                    )
                    Text(
                        "Organization Settings",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF111827)
                    )
                }
            }
        }

        // ── Breadcrumb ──
        Column(
            modifier = Modifier
                .background(Color(0xFFF8F9FF))
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Settings", fontSize = 13.sp, color = Color(0xFF9CA3AF))
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color(0xFF9CA3AF),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    "Organization Settings",
                    fontSize = 13.sp,
                    color = Color(0xFF3B3BF9),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // ── Tabs ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFEEEEEE), RoundedCornerShape(12.dp)),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            tabs.forEachIndexed { index, (icon, label) ->
                val isSelected = selectedTab == index
                Row(
                    modifier = Modifier
                        .wrapContentWidth(Alignment.CenterHorizontally)
                        .background(
                            if (isSelected) Color.White else Color.Transparent,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { selectedTab = index }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isSelected) Color.Black else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = label,
                        fontSize = 16.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) Color.Black else Color.Gray
                    )
                }
            }
        }

        when (selectedTab) {
            0 -> ProfileTab(token = token)
            1 -> LocalizationTab(token = token)
        }
    }
}


// 1. Uri -> File (cache-la copy pannanum, since multipart-ku real file path venum)
// REPLACE uriToBase64 with:

//
//// 2. File -> MultipartBody.Part
//private fun fileToMultipart(file: File, partName: String = "organizationPicture"): MultipartBody.Part {
//    val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
//    return MultipartBody.Part.createFormData(partName, file.name, requestBody)
//}

// ─────────────────────────────────────────────────────────────
// Helper: Convert picked Uri -> Base64 string
// ─────────────────────────────────────────────────────────────
// REPLACE uriToBase64 with:
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

@Composable
fun ProfileTab(
    token: String,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isEditing by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()
    val updateState by viewModel.updateState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Form states for editing
    var orgName by remember { mutableStateOf("") }
    var orgType by remember { mutableStateOf("") }
    var businessType by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }

    // 👇 Logo state - just holds the picked image for local preview until Save is clicked
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // 👇 Image picker launcher - ONLY stores the uri, does NOT upload immediately
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

    // Handle update result
    LaunchedEffect(updateState) {
        when (val state = updateState) {
            is UpdateOrgUiState.Success -> {
                isEditing = false
                selectedImageUri = null   // clear local preview - real URL now comes from server
                viewModel.resetUpdateState()
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(state.message)
                }
            }
            is UpdateOrgUiState.Error -> {
                viewModel.resetUpdateState()
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(state.message)
                }
            }
            else -> Unit
        }
    }

    val isSaving = updateState is UpdateOrgUiState.Loading

    when (val state = uiState) {
        is ProfileUiState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CirculerProgressIndicatorReuse()
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

            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                // ── Header with Edit/Save buttons ──
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "Organization Information",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.Black
                                        )
                                        Text(
                                            "Core details about your organization",
                                            fontSize = 13.sp,
                                            color = Color.Gray
                                        )
                                    }
                                    if (isEditing) {
                                        Row {
                                            TextButton(
                                                onClick = {
                                                    isEditing = false
                                                    selectedImageUri = null
                                                    viewModel.resetUpdateState()
                                                },
                                                enabled = !isSaving
                                            ) {
                                                Text("Cancel", fontSize = 14.sp, color = Color.Gray)
                                            }
                                            Button(
                                                onClick = {
                                                    val logoFile = selectedImageUri?.let { uriToFile(context, it) }
                                                    val request = UpdateOrganizationRequest(
                                                        name = orgName,
                                                        orgType = orgType,
                                                        businessType = businessType,
                                                        email = email,
                                                        mobile = mobile
                                                    )
                                                    viewModel.updateOrganization(token, request, logoFile)
                                                },
                                                enabled = !isSaving,
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Color(0xFF3B3BF9)
                                                ),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                if (isSaving) {
                                                    CirculerProgressIndicatorReuse()
                                                } else {
                                                    Text("Save", color = Color.White, fontSize = 14.sp)
                                                }
                                            }
                                        }
                                    } else {
                                        OutlinedButton(
                                            onClick = { isEditing = true },
                                            shape = RoundedCornerShape(8.dp),
                                            border = ButtonDefaults.outlinedButtonBorder(enabled = true),
                                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Edit, contentDescription = "Edit",
                                                modifier = Modifier.size(14.dp), tint = Color.Black
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Text("Edit", color = Color.Black, fontSize = 14.sp)
                                        }
                                    }
                                }

                                Spacer(Modifier.height(16.dp))

                                // ── Logo (clickable in edit mode - just picks image, upload happens on Save) ──
                                Box(
                                    modifier = Modifier
                                        .size(130.dp)
                                        .border(1.5.dp, Color.LightGray, RoundedCornerShape(10.dp))
                                        .clickable(enabled = isEditing) {
                                            imagePickerLauncher.launch(
                                                PickVisualMediaRequest(
                                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                                )
                                            )
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    when {
                                        selectedImageUri != null -> {
                                            // Local preview of newly picked image (not yet saved)
                                            AsyncImage(
                                                model = selectedImageUri,
                                                contentDescription = "Organization Logo",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                        !org.organizationPicture.isNullOrBlank() -> {
                                            AsyncImage(
                                                model = org.organizationPicture,
                                                contentDescription = "Organization Logo",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                        else -> {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.CameraAlt,
                                                    contentDescription = null,
                                                    tint = Color.LightGray,
                                                    modifier = Modifier.size(28.dp)
                                                )
                                                Text("Upload Logo", color = Color.LightGray, fontSize = 13.sp)
                                            }
                                        }
                                    }

                                    // Camera badge overlay - only in edit mode
                                    if (isEditing) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .padding(6.dp)
                                                .size(28.dp)
                                                .background(Color(0xFF3B3BF9), RoundedCornerShape(50)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.CameraAlt,
                                                contentDescription = "Change logo",
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(Modifier.height(16.dp))
                                HorizontalDivider(color = Color(0xFFF0F0F0))
                                Spacer(Modifier.height(16.dp))

                                // ── Editable Fields ──
                                if (isEditing) {
                                    EditableOrgInfoRow("Organization Name", orgName) { orgName = it }
                                    Spacer(Modifier.height(16.dp))
                                    HorizontalDivider(color = Color(0xFFF0F0F0))
                                    Spacer(Modifier.height(16.dp))

                                    OrgLabel("Organization Type")
                                    OrganizationDropdown(OrgOptions.orgTypes, orgType) { orgType = it }
                                    Spacer(Modifier.height(16.dp))
                                    HorizontalDivider(color = Color(0xFFF0F0F0))
                                    Spacer(Modifier.height(16.dp))

                                    OrgLabel("Business Type")
                                    OrganizationDropdown(OrgOptions.businessTypes, businessType) { businessType = it }
                                    Spacer(Modifier.height(16.dp))
                                    HorizontalDivider(color = Color(0xFFF0F0F0))
                                    Spacer(Modifier.height(16.dp))

                                    OrgInfoRow("Business ID", org.businessId )
                                    Spacer(Modifier.height(16.dp))
                                    HorizontalDivider(color = Color(0xFFF0F0F0))
                                    Spacer(Modifier.height(16.dp))

                                    EditableOrgInfoRow("Email", email) { email = it }
                                    Spacer(Modifier.height(16.dp))
                                    HorizontalDivider(color = Color(0xFFF0F0F0))
                                    Spacer(Modifier.height(16.dp))

                                    EditableOrgInfoRow("Mobile", mobile) { mobile = it }
                                    Spacer(Modifier.height(16.dp))
                                    HorizontalDivider(color = Color(0xFFF0F0F0))
                                    Spacer(Modifier.height(16.dp))

                                    OrgInfoRow("Plan Status", org.subscription.status)
                                    OrgInfoRow("Status", org.status)
                                } else {
                                    val rows = listOf(
                                        "Organization Name" to org.name,
                                        "Organization Type" to (org.orgType ),
                                        "Business Type" to (org.businessType ),
                                        "Business ID" to (org.businessId ),
                                        "Email" to (org.email ),
                                        "Mobile" to (org.mobile ),
                                        "Plan Status" to org.subscription.status,
                                        "Status" to org.status
                                    )

                                    rows.forEachIndexed { index, (label, value) ->
                                        OrgInfoRow(label, value)
                                        if (index != rows.lastIndex) {
                                            Spacer(Modifier.height(16.dp))
                                            HorizontalDivider(color = Color(0xFFF0F0F0))
                                            Spacer(Modifier.height(16.dp))
                                        } else {
                                            Spacer(Modifier.height(16.dp))
                                        }
                                    }
                                }

                                // ── Subscription Usage ──
                                if (stats != null) {
                                    val plan = state.data.organization.plan

                                    Text(
                                        "Subscription Usage",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.Black
                                    )
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
                                            limit = plan?.employeeLimit ?: org.totalMembers.coerceAtLeast(1)
                                        )
                                        SubscriptionRing(
                                            modifier = Modifier.weight(1f),
                                            label = "Branches",
                                            used = stats.totalBranches,
                                            limit = plan?.branchLimit ?: stats.totalBranches.coerceAtLeast(1)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Snackbar
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// LocalizationTab - With Edit Mode & Save
// ─────────────────────────────────────────────────────────────
@Composable
fun LocalizationTab(
    token: String,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    var isEditing by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()
    val updateState by viewModel.updateState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Form states for editing
    var country by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var postalCode by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var timezone by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf("") }
    var language by remember { mutableStateOf("") }
    var companySize by rememberSaveable { mutableStateOf("") }
    var portalName by remember { mutableStateOf("") } // Added portal name state

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

            // Set company size based on total members
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

    // Handle update result
    LaunchedEffect(updateState) {
        when (val state = updateState) {
            is UpdateOrgUiState.Success -> {
                isEditing = false
                viewModel.resetUpdateState()
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(state.message)
                }
            }
            is UpdateOrgUiState.Error -> {
                viewModel.resetUpdateState()
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(state.message)
                }
            }
            else -> Unit
        }
    }

    val isLoading = uiState is ProfileUiState.Loading
    val isSaving = updateState is UpdateOrgUiState.Loading
    val org = (uiState as? ProfileUiState.Success)?.data?.organization
    val settings = org?.settings
    val errorMessage = (uiState as? ProfileUiState.Error)?.message

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Localization & Address Card ──
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Localization & Address",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.Black
                                )
                                Text(
                                    "Location and regional settings",
                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )
                            }
                            if (isEditing) {
                                Row {
                                    TextButton(
                                        onClick = {
                                            isEditing = false
                                            viewModel.resetUpdateState()
                                        },
                                        enabled = !isSaving
                                    ) {
                                        Text("Cancel", fontSize = 14.sp, color = Color.Gray)
                                    }
                                    Button(
                                        onClick = {
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
                                                    companySize=companySize// Include portal name in update
                                                )
                                            )
                                            viewModel.updateOrganization(token, request)
                                        },
                                        enabled = !isSaving,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF3B3BF9)
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        if (isSaving) {
                                            CirculerProgressIndicatorReuse()

                                        } else {
                                            Text("Save", color = Color.White, fontSize = 14.sp)
                                        }
                                    }
                                }
                            } else {
                                OutlinedButton(
                                    onClick = { isEditing = true },
                                    shape = RoundedCornerShape(8.dp),
                                    border = ButtonDefaults.outlinedButtonBorder(enabled = true),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Edit, contentDescription = "Edit",
                                        modifier = Modifier.size(14.dp), tint = Color.Black
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text("Edit", color = Color.Black, fontSize = 14.sp)
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(color = Color(0xFFF0F0F0))
                        Spacer(Modifier.height(16.dp))

                        when {
                            isLoading -> {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CirculerProgressIndicatorReuse()
                                }
                            }
                            errorMessage != null -> {
                                Text(errorMessage, color = Color.Red, modifier = Modifier.padding(vertical = 16.dp))
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
                                    HorizontalDivider(color = Color(0xFFF0F0F0))
                                    Spacer(Modifier.height(16.dp))

                                    EditableOrgInfoRow("City", city) { city = it }
                                    Spacer(Modifier.height(16.dp))
                                    HorizontalDivider(color = Color(0xFFF0F0F0))
                                    Spacer(Modifier.height(16.dp))

                                    EditableOrgInfoRow("Postal Code", postalCode) { postalCode = it }
                                    Spacer(Modifier.height(16.dp))
                                    HorizontalDivider(color = Color(0xFFF0F0F0))
                                    Spacer(Modifier.height(16.dp))

                                    EditableOrgInfoRow("Address", address, isMultiline = true) { address = it }
                                    Spacer(Modifier.height(16.dp))
                                    HorizontalDivider(color = Color(0xFFF0F0F0))
                                    Spacer(Modifier.height(16.dp))

                                    OrgLabel("Timezone")
                                    OrganizationDropdown(OrgOptions.timezones, timezone) { timezone = it }
                                    Spacer(Modifier.height(16.dp))
                                    HorizontalDivider(color = Color(0xFFF0F0F0))
                                    Spacer(Modifier.height(16.dp))

                                    OrgLabel("Currency")
                                    OrganizationDropdown(OrgOptions.currencies, currency) { currency = it }
                                    Spacer(Modifier.height(16.dp))
                                    HorizontalDivider(color = Color(0xFFF0F0F0))
                                    Spacer(Modifier.height(16.dp))

                                    OrgLabel("Language")
                                    OrganizationDropdown(OrgOptions.languages, language) { language = it }
                                    Spacer(Modifier.height(16.dp))
                                    HorizontalDivider(color = Color(0xFFF0F0F0))
                                    Spacer(Modifier.height(16.dp))

                                    // Add Portal Name edit field
                                    OrgLabel("Portal Name")
                                    EditableOrgInfoRow("", portalName) { portalName = it }
                                    Spacer(Modifier.height(16.dp))
                                    HorizontalDivider(color = Color(0xFFF0F0F0))
                                    Spacer(Modifier.height(16.dp))

                                    // Add Company Size edit field
                                    OrgLabel("Company Size")
                                    OrganizationDropdown(companySizes, companySize) { companySize = it }
                                } else {
                                    val rows = listOf(
                                        "Country" to settings.country,
                                        "State" to settings.state,
                                        "City" to settings.city,
                                        "Postal Code" to settings.pincode,
                                        "Address" to settings.address,
                                        "Timezone" to settings.timezone,
                                        "Currency" to settings.currency,
                                        "Language" to settings.language,
                                        "Portal Name" to settings.portalName // Add portal name to display
                                    )
                                    rows.forEachIndexed { index, (label, value) ->
                                        OrgInfoRow(label, value )
                                        if (index != rows.lastIndex) {
                                            Spacer(Modifier.height(16.dp))
                                            HorizontalDivider(color = Color(0xFFF0F0F0))
                                            Spacer(Modifier.height(16.dp))
                                        }
                                    }
                                }
                            }
                            else -> {
                                Text("No data available", color = Color.Gray, modifier = Modifier.padding(vertical = 16.dp))
                            }
                        }
                    }
                }
            }

            // ── Regional Settings Card ──
            // This card is now redundant since we show all fields in the first card,
            // but I'll keep it for backward compatibility
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Regional Settings",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )
                        Spacer(Modifier.height(4.dp))
                        Text("Timezone, currency and language", fontSize = 13.sp, color = Color.Gray)

                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(color = Color(0xFFF0F0F0))
                        Spacer(Modifier.height(16.dp))

                        when {
                            isLoading -> {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CirculerProgressIndicatorReuse()
                                }
                            }
                            settings != null -> {
                                if (!isEditing) {
                                    // Show regional settings in a condensed format
                                    val rows = listOf(
                                        "Timezone" to settings.timezone,
                                        "Currency" to settings.currency,
                                        "Language" to settings.language
                                    )
                                    rows.forEachIndexed { index, (label, value) ->
                                        OrgInfoRow(label, value )
                                        if (index != rows.lastIndex) {
                                            Spacer(Modifier.height(16.dp))
                                            HorizontalDivider(color = Color(0xFFF0F0F0))
                                            Spacer(Modifier.height(16.dp))
                                        }
                                    }
                                }
                            }
                            else -> {
                                Text("No data available", color = Color.Gray, modifier = Modifier.padding(vertical = 16.dp))
                            }
                        }
                    }
                }
            }

            // ── Portal Info Card ──
            // This card is now redundant since we show these fields in the first card,
            // but I'll keep it for backward compatibility
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Portal Information",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )
                        Spacer(Modifier.height(4.dp))
                        Text("Portal and company details", fontSize = 13.sp, color = Color.Gray)

                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(color = Color(0xFFF0F0F0))
                        Spacer(Modifier.height(16.dp))

                        when {
                            isLoading -> {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CirculerProgressIndicatorReuse()
                                }
                            }
                            org != null && settings != null -> {
                                if (!isEditing) {

                                    OrgInfoRow("Portal Name", settings.portalName )
                                    Spacer(Modifier.height(16.dp))
                                    HorizontalDivider(color = Color(0xFFF0F0F0))
                                    Spacer(Modifier.height(16.dp))
                                    OrgInfoRow("Company Size", settings.companySize)
                                }
                            }
                            else -> {
                                Text("No data available", color = Color.Gray, modifier = Modifier.padding(vertical = 16.dp))
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }

        // Snackbar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}
// ─────────────────────────────────────────────────────────────
// Editable Org Info Row
// ─────────────────────────────────────────────────────────────
@Composable
fun EditableOrgInfoRow(
    label: String,
    value: String,
    isMultiline: Boolean = false,
    onValueChange: (String) -> Unit

) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 12.dp),
            singleLine = !isMultiline,
            maxLines = if (isMultiline) 3 else 1,
            textStyle = androidx.compose.ui.text.TextStyle(
                fontSize = 14.sp,
                color = Color.Black
            ),
            cursorBrush = SolidColor(Color(0xFF3B3BF9))
        )
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
                color = Color(0xFF111827)
            )
        }
        Text(text = label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF111827))
        Text(text = "$used / $limit", fontSize = 12.sp, color = Color(0xFF6B7280))
        Text(text = "$remaining remaining", fontSize = 12.sp, color = Color(0xFF9CA3AF))
    }
}

// ─────────────────────────────────────────────────────────────
// Org Info Row
// ─────────────────────────────────────────────────────────────
@Composable
fun OrgInfoRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}