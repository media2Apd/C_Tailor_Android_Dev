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

package com.cuso.mobile.view.home.profile_settings

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.zIndex
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.cuso.mobile.adaptive_screen.AppDesignTokens
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.model.UpdateOrganizationRequest
import com.cuso.mobile.model.UpdateOrganizationSettings
import com.cuso.mobile.ui.theme.Primary
import com.cuso.mobile.ui.theme.Primary_background
import com.cuso.mobile.ui.theme.TextSecondary
import com.cuso.mobile.ui.theme.grey_border
import com.cuso.mobile.ui.theme.mutedText
import com.cuso.mobile.ui.theme.statLogoBg
import com.cuso.mobile.ui.theme.title_border
import com.cuso.mobile.ui.theme.title_color
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.CirculerProgressIndicatorSmall
import com.cuso.mobile.view.composable.CountryAndStatePicker
import com.cuso.mobile.view.composable.DynamicIslandError
import com.cuso.mobile.view.composable.DynamicIslandSuccess
import com.cuso.mobile.view.composable.FormDropdown
import com.cuso.mobile.view.composable.FormTextField
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.view.composable.SettingsTabs
import com.cuso.mobile.view.composable.StepNavigationFab
import com.cuso.mobile.view.composable.TabItem
import com.cuso.mobile.view.composable.TrailingFabAction
import com.cuso.mobile.view.composable.dashedBorder
import com.cuso.mobile.view.organization.OrgOptions
import com.cuso.mobile.view.organization.OrgOptions.companySizes
import com.cuso.mobile.viewmodel.Authenticate
import com.cuso.mobile.viewmodel.ProfileUiState
import com.cuso.mobile.viewmodel.ProfileViewModel
import com.cuso.mobile.viewmodel.UpdateOrgUiState
import kotlin.math.roundToInt
import java.io.File

// ─────────────────────────────────────────────────────────────
// Design tokens (static colors only — sizes now come from
// LocalAppTokens.current so the screen adapts across
// phone / foldable / tablet / expanded widths)
// ─────────────────────────────────────────────────────────────
private object OrgTheme {
    val PrimaryLight = Color(0xFFEEF0FF)
    val TextPrimary = Color(0xFF111827)
    val TextSecondary = Color(0xFF6B7280)
    val mutedText = Color(0xFF9CA3AF)
    val Border = grey_border
    val Divider = title_border
    val PageBg = Color(0xFFF5F5F5)
    val InputBg = whiteBg
}

// Max readable width for form content on large screens (tablet / expanded).
// Keeps long forms from stretching edge-to-edge like a phone sheet.
private val ADAPTIVE_CONTENT_MAX_WIDTH = 640.dp

// ─────────────────────────────────────────────────────────────
// Reusable wrapper: centers content and caps its width on
// tablet/expanded layouts, stays full-bleed on phones.
// ─────────────────────────────────────────────────────────────
@Composable
private fun AdaptiveWidthContainer(
    tokens: AppDesignTokens,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .then(
                    if (tokens.isTablet) Modifier.widthIn(max = ADAPTIVE_CONTENT_MAX_WIDTH) else Modifier
                ),
            content = content
        )
    }
}

@Suppress("UNUSED_PARAMETER")
@Composable
fun SettingsScreen(
    navController: NavController,
    onMenuClick: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tokens = LocalAppTokens.current

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

    // NOTE: root container background changed from an opaque Primary_background
    // to Color.Transparent. This matches the CustomerDetailScreen pattern so the
    // bottom StepNavigationFab (Cancel / Save Changes) renders with a transparent
    // background instead of sitting on top of an opaque page color.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
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

        // ── Reusable Tabs (centered + width-capped on tablet) ──
        AdaptiveWidthContainer(tokens = tokens) {
            SettingsTabs(
                tabs = tabs,
                selectedIndex = selectedTab,
                onTabSelected = { selectedTab = it },
                modifier = Modifier.padding(
                    horizontal = tokens.screenPadding,
                    vertical = tokens.extraPadding
                ),
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
        }

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
    val tokens = LocalAppTokens.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(whiteBg)
            .padding( vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = tokens.bodyMedium, fontWeight = FontWeight.SemiBold, color = OrgTheme.TextPrimary)
            Spacer(Modifier.height(2.dp))
            Text(subtitle, fontSize = tokens.bodySmall, color = TextSecondary)
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
                    modifier = Modifier.size(tokens.iconSize * 0.8f)
                )
                Spacer(Modifier.width(4.dp))
                Text("Edit", color = Primary, fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Read-only field row
// ─────────────────────────────────────────────────────────────
@Composable
fun OrgInfoRow(label: String, value: String) {
    val tokens = LocalAppTokens.current
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = label, fontSize = tokens.bodyMedium, color = mutedText)
        Text(text = value.ifEmpty { "-" }, fontSize = tokens.bodyMedium, color = title_color)
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
    val tokens = LocalAppTokens.current
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        if (label.isNotEmpty()) {
            Text(text = label, fontSize = tokens.bodyMedium, color = mutedText)

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
    val tokens = LocalAppTokens.current
    val logoSize = if (tokens.isTablet) 120.dp else 96.dp
    val radius = tokens.cardCornerRadius * 0.65f


    Box(
        modifier = Modifier
            .size(logoSize)
            .dashedBorder(
                color = statLogoBg,
                strokeWidth = 1.5.dp,
                dashLength = 6.dp,
                gapLength = 4.dp,
                cornerRadius = radius
            )            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.65f)),
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
                Icon(Icons.Default.Business, contentDescription = null, tint = OrgTheme.mutedText, modifier = Modifier.size(tokens.iconSize * 1.6f))
                Spacer(Modifier.height(4.dp))
                Text("No Logo", color = OrgTheme.mutedText, fontSize = tokens.label)
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
    val tokens = LocalAppTokens.current
    val logoSize = if (tokens.isTablet) 120.dp else 96.dp
    val radius = tokens.cardCornerRadius * 0.65f


    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(logoSize)
                .dashedBorder(
                    color = statLogoBg,
                    strokeWidth = 1.5.dp,
                    dashLength = 6.dp,
                    gapLength = 4.dp,
                    cornerRadius = radius
                )
                .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.65f))
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
                            modifier = Modifier.size(tokens.iconSize)
                        )
                        Spacer(Modifier.height(2.dp))
                        Text("Upload Logo", color = Primary, fontSize = tokens.label, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text("Organization Logo", fontSize = tokens.bodyMedium, fontWeight = FontWeight.SemiBold, color = OrgTheme.TextPrimary)
            Spacer(Modifier.height(2.dp))
            Text(
                "Recommended size: 200*200px, PNG or JPG",
                fontSize = tokens.label,
                color = OrgTheme.TextSecondary
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Bottom footer (Cancel / Save Changes)
//
// UPDATED: now accepts an external `modifier` so callers can place it
// as a floating overlay (e.g. Modifier.align(Alignment.BottomCenter)
// inside a Box) instead of it being a regular in-flow item inside a
// Column. This is the same pattern used by StepNavigationFab on the
// CustomerDetailScreen, and it is what keeps the background transparent
// instead of appearing on top of an opaque page background.
// ─────────────────────────────────────────────────────────────
@Composable
private fun EditFooter(
    modifier: Modifier = Modifier,
    isSaving: Boolean,
    onCancel: () -> Unit,
    onSave: () -> Unit
) {
    val tokens = LocalAppTokens.current

    // Using Row instead of Box to avoid the internal fillMaxSize() inside StepNavigationFab
    // from taking up the whole screen and hiding the content.
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(horizontal = tokens.screenPadding, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        StepNavigationFab(
            showBack = true,
            onBack = onCancel,
            trailingAction = TrailingFabAction.Next(
                label = "Save Changes",
                enabled = !isSaving,
                onClick = onSave
            ),
            backLabel = "Cancel",
            backEnabled = !isSaving,
            showBackArrow = false,
            showTrailingArrow = false
        )
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
    val tokens = LocalAppTokens.current

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

            // UPDATED: outer wrapper changed from Column { Box(weight) ; EditFooter } to a
            // single Box. This lets EditFooter be placed as a floating, bottom-aligned
            // overlay on top of the LazyColumn (same as StepNavigationFab in
            // CustomerDetailScreen) instead of being pushed into the normal layout flow
            // on top of an opaque background.
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Primary_background),
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    // Reserve space at the bottom so the last item is not hidden behind
                    // the floating footer when it is visible.
                    contentPadding = if (isEditing) PaddingValues(bottom = 96.dp) else PaddingValues(0.dp)
                ) {
                    item {
                        AdaptiveWidthContainer(tokens = tokens) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(whiteBg)
                                    .padding(horizontal = tokens.screenPadding, vertical = 8.dp)
                            ) {
                                SectionHeader(
                                    title = "Organization Information",
                                    subtitle = "Core details about your organization",
                                    isEditing = isEditing,
                                    onEditClick = { isEditing = true }
                                )
                            }
                        }
                        Spacer(Modifier.height(10.dp))

                        AdaptiveWidthContainer(tokens = tokens) {
                            Column(
                                Modifier.fillMaxWidth()
                                    .padding(horizontal = tokens.screenPadding)
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
                                    OrgLabelLocal("Organization Type")
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
                                    OrgLabelLocal("Business Type")
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
                    }
                    item {
                        AdaptiveWidthContainer(tokens = tokens) {
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
                                            .padding(horizontal = tokens.screenPadding + 4.dp, vertical = 10.dp)
                                    ) {
                                        Text(
                                            "Subscription Usage",
                                            fontSize = tokens.h2,
                                            fontWeight = FontWeight.SemiBold,
                                            color = OrgTheme.TextPrimary
                                        )
                                    }
                                    Spacer(Modifier.height(16.dp))

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = tokens.extraPadding),
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

                // Floating footer overlay — transparent background, bottom-aligned,
                // matches StepNavigationFab placement in CustomerDetailScreen.
                if (isEditing) {
                    EditFooter(
                        modifier = Modifier.align(Alignment.BottomCenter),
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
@Composable
fun OrgLabelLocal(text: String){
    val tokens = LocalAppTokens.current
    Text(text = text, fontSize = tokens.bodyMedium, color = mutedText)

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
    val tokens = LocalAppTokens.current

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

    // UPDATED: outer wrapper changed from Column { Box(weight) ; EditFooter } to a
    // single Box so EditFooter can float as a bottom-aligned, transparent overlay
    // instead of sitting in the normal layout flow on top of an opaque background.
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Primary_background),
            horizontalAlignment = Alignment.CenterHorizontally,
            // Reserve space at the bottom so the last item is not hidden behind
            // the floating footer when it is visible.
            contentPadding = if (isEditing) PaddingValues(bottom = 96.dp) else PaddingValues(0.dp)
        ) {
            item {
                AdaptiveWidthContainer(tokens = tokens) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(whiteBg)
                            .padding(horizontal = tokens.screenPadding, vertical = 8.dp)
                    ) {
                        SectionHeader(
                            title = "Localization Settings",
                            subtitle = "Regional preferences for your organization",
                            isEditing = isEditing,
                            onEditClick = { isEditing = true }
                        )
                    }
                }
                Spacer(Modifier.height(18.dp))

                AdaptiveWidthContainer(tokens = tokens) {
                    Column(
                        Modifier.fillMaxWidth()
                            .padding(horizontal = tokens.screenPadding)
                            .background(Color.Transparent)
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
                                    fontSize = tokens.bodyMedium,
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
                                    OrgLabelLocal("Timezone")
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
                                    OrgLabelLocal("Default Currency")
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
                                    OrgLabelLocal("Language")
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
                                    OrgLabelLocal("Company Size")
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
                                    fontSize = tokens.bodyMedium,
                                    modifier = Modifier.padding(vertical = 16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Floating footer overlay — transparent background, bottom-aligned,
        // matches StepNavigationFab placement in CustomerDetailScreen.
        if (isEditing) {
            EditFooter(
                modifier = Modifier.align(Alignment.BottomCenter),
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
    val tokens = LocalAppTokens.current
    val ringDiameter = if (tokens.isTablet) 110.dp else 90.dp

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
            modifier = Modifier.size(ringDiameter)
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
                fontSize = tokens.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = OrgTheme.TextPrimary
            )
        }
        Text(text = label, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = OrgTheme.TextPrimary)
        Text(text = "$used / $limit", fontSize = tokens.caption, color = OrgTheme.TextSecondary)
        Text(text = "$remaining remaining", fontSize = tokens.caption, color = OrgTheme.mutedText)
    }
}