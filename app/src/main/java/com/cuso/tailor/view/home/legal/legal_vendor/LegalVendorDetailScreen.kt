package com.cuso.tailor.view.home.legal.legal_vendor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.legal.LegalVendorDocumentItem
import com.cuso.tailor.model.legal.LegalVendorItem
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.DataCardProgressBar
import com.cuso.tailor.view.composable.ListSkeleton
import com.cuso.tailor.view.composable.TabItem
import com.cuso.tailor.view.home.inventory.approval.formatIsoDate
import com.cuso.tailor.view.home.legal.detail_template.*
import com.cuso.tailor.viewmodel.LegalViewModel

// ─────────────────────────────────────────────────────────────
// LEGAL STATUS CONFIG (mirrors React STATUS_LABEL / STATUS_ACTIONS for vendor)
// ─────────────────────────────────────────────────────────────

// Distance from the top of the screen to where the dropdown card appears.
// Increase or decrease this value to move the dropdown right under the "Legal Action" button.
private val VendorMenuTopOffset: Dp = 220.dp

// Colors used by the confirmation dialog (yellow = normal, red = danger)
private val VendorWarningYellow = Color(0xFFEAB308)
private val VendorDangerRed = Color(0xFFEF4444)

/**
 * One option shown inside the "Legal Action" dropdown.
 * [targetStatus] is the exact enum value sent to the backend, e.g. {"status":"Active"}.
 */
data class VendorStatusAction(
    val label: String,
    val targetStatus: String,
    val isDanger: Boolean = false
)

/**
 * Converts any raw status text ("inactive", "InActive", "In Active"...)
 * into the exact backend enum value used by the web app: Pending, Active, InActive, Expired.
 */
private fun canonicalVendorStatus(raw: String): String {
    return when (raw.trim().lowercase().replace(" ", "")) {
        "pending" -> "Pending"
        "active" -> "Active"
        "inactive" -> "InActive"
        "expired" -> "Expired"
        else -> raw.trim()
    }
}

/**
 * Human readable label for a status (same as React STATUS_LABEL).
 */
private fun vendorStatusLabel(status: String): String {
    return when (canonicalVendorStatus(status)) {
        "Pending" -> "Pending"
        "Active" -> "Active"
        "InActive" -> "Inactive"
        "Expired" -> "Expired"
        else -> status
    }
}

/**
 * Next steps offered under "Legal Action" for the current status (same as React STATUS_ACTIONS).
 * Returns an empty list when no action is available, so the button is hidden.
 */
private fun getVendorStatusActions(status: String): List<VendorStatusAction> {
    return when (canonicalVendorStatus(status)) {
        "Pending" -> listOf(
            VendorStatusAction(label = "Mark Active", targetStatus = "Active")
        )
        "Active" -> listOf(
            // Only InActive is treated as a danger action (red dialog), same as React
            VendorStatusAction(label = "Mark Inactive", targetStatus = "InActive", isDanger = true),
            VendorStatusAction(label = "Mark Expired", targetStatus = "Expired")
        )
        "InActive" -> listOf(
            VendorStatusAction(label = "Reactivate", targetStatus = "Active")
        )
        "Expired" -> listOf(
            VendorStatusAction(label = "Reactivate", targetStatus = "Active")
        )
        else -> emptyList()
    }
}

@Composable
fun LegalVendorDetailScreen(
    vendorId: String,
    viewModel: LegalViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit
) {
    val tokens = LocalAppTokens.current
//    val context = LocalContext.current
    val vendorDetail by viewModel.vendorDetail.collectAsState()
    val isLoading by viewModel.isLoadingVendorDetail.collectAsState()
    val errorMessage by viewModel.vendorDetailError.collectAsState()

    // NOTE: this flow must exist in LegalViewModel (see the notes after the code)
    val isUpdatingStatus by viewModel.isUpdatingVendorStatus.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }

    // Controls whether the "Legal Action" dropdown card is visible
    var showActionMenu by remember { mutableStateOf(false) }

    // Holds the action chosen from the dropdown while waiting for confirmation
    var pendingStatusAction by remember { mutableStateOf<VendorStatusAction?>(null) }

    LaunchedEffect(vendorId) {
        viewModel.fetchLegalVendorDetail(vendorId)
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearLegalVendorDetail()
        }
    }

    if (isLoading) {
        ListSkeleton()
    } else if (!errorMessage.isNullOrBlank()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(whiteBg)
                .padding(tokens.screenPadding),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = errorMessage ?: "Failed to load vendor details",
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.Medium,
                color = redText
            )
        }
    } else if (vendorDetail != null) {
        val vendor = vendorDetail!!
        val isVendorActive = vendor.status.equals("Active", ignoreCase = true)
        val dynamicComplianceScore = calculateComplianceScore(vendor)

        // Calls the status API with the exact enum value (payload: {"status": newStatus}).
        // The dialog stays open while saving and is closed by onDone (success or error).
        fun changeStatus(newStatus: String, onDone: () -> Unit) {
            if (isUpdatingStatus) return
            viewModel.updateLegalVendorStatus(
                vendorId = vendor.id,
                status = newStatus,
                onSuccess = {
                    // Reload the record so the status badge and available actions refresh
                    viewModel.fetchLegalVendorDetail(vendor.id)
                    onDone()
                },
                onError = {
                    onDone()
                }
            )
        }

        // Available status actions for the current status (same as React STATUS_ACTIONS)
        val statusActions = remember(vendor.status) {
            getVendorStatusActions(vendor.status)
        }

        // "Legal Action" button: shown only when the current status has actions.
        // Clicking it opens the dropdown card (rendered below the template).
        val primaryAction: DetailAction? = if (statusActions.isNotEmpty()) {
            DetailAction(
                label = "Legal Action",
                icon = Icons.Default.Add,
                onClick = { showActionMenu = true }
            )
        } else null

        val headerData = DetailHeaderData(
            icon = Icons.Default.LocalShipping,
            badges = listOf(
                DetailBadge(
                    // Show the readable label (e.g. "Inactive") like the web app
                    text = vendorStatusLabel(vendor.status).ifBlank { "N/A" },
                    textColor = if (isVendorActive) greentext else orangeText,
                    bgColor = if (isVendorActive) greenBg else orangeBg,
                    showDot = true
                )
            ),
            title = vendor.vendor.ifBlank { "N/A" },
            metaItems = listOfNotNull(
                vendor.legalVendorNumber.ifBlank { null },
                vendor.supplierType.ifBlank { null },
                vendor.displayCountry.ifBlank { null }
            ),
            secondaryActions = listOf(
                DetailAction(
                    label = "Edit",
                    icon = Icons.Default.Edit,
                    onClick = { onNavigateToEdit(vendor.id) }
                ),
                DetailAction(
                    label = "Download",
                    icon = Icons.Default.Download,
                    onClick = {}
                ),
                DetailAction(
                    label = "Share",
                    icon = Icons.Default.Share,
                    onClick = {}
                )
            ),
            primaryAction = primaryAction
        )

        val summaryStats = listOf(
            DetailStat(
                label = "STATUS",
                value = vendorStatusLabel(vendor.status).ifBlank { "N/A" },
                valueColor = if (isVendorActive) greentext else orangeText,
                showDot = true
            ),
            DetailStat(
                label = "RISK LEVEL",
                value = vendor.riskLevel.ifBlank { "N/A" },
                valueColor = when (vendor.riskLevel.lowercase()) {
                    "high" -> redText
                    "medium" -> yellowText
                    else -> greentext
                },
                showDot = true
            ),
            DetailStat(
                label = "COMPLIANCE",
                value = "$dynamicComplianceScore %",
                valueColor = TextPrimary
            ),
            DetailStat(
                label = "DISPUTES",
                value = "0",
                valueColor = TextPrimary
            ),
            DetailStat(
                label = "CONTRACT EXPIRY",
                value = formatIsoDate(vendor.endDate),
                valueColor = Primary
            )
        )

        val tabs = listOf(
            TabItem( "Overview"),
            TabItem( "Contracts & Terms"),
            TabItem( "Documents & Compliance"),
            TabItem( "Disputes & Notices"),
            TabItem( "History")
        )

        // Root box so the dropdown and dialog can be drawn on top of the screen
        Box(modifier = Modifier.fillMaxSize()) {

            DetailScreenTemplate(
                title = "Vender & Suppliers",
                onClose = onNavigateBack,
                header = headerData,
                summaryStats = summaryStats,
                tabs = tabs,
                selectedTabIndex = selectedTabIndex,
                onTabSelected = { selectedTabIndex = it }
            ) { activeIndex ->
                when (activeIndex) {
                    0 -> VendorOverviewTab(vendor)
                    1 -> VendorContractsTab(vendor)
                    2 -> VendorDocumentsTab(vendor)
                    3 -> VendorDisputesTab()
                    4 -> VendorHistoryTab(vendor)
                }
            }

            // Step 1: floating dropdown card with the available status actions
            if (showActionMenu && statusActions.isNotEmpty()) {
                VendorActionMenu(
                    actions = statusActions,
                    onDismiss = { showActionMenu = false },
                    onSelect = { action ->
                        showActionMenu = false
                        pendingStatusAction = action // opens the confirmation dialog
                    }
                )
            }

            // Step 2: confirmation dialog shown after an action is chosen
            pendingStatusAction?.let { action ->
                VendorStatusConfirmDialog(
                    action = action,
                    currentStatusLabel = vendorStatusLabel(vendor.status),
                    isSaving = isUpdatingStatus,
                    onConfirm = {
                        changeStatus(action.targetStatus) { pendingStatusAction = null }
                    },
                    onDismiss = { pendingStatusAction = null }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// LEGAL ACTION DROPDOWN (white floating card, like the web menu)
// ─────────────────────────────────────────────────────────────

@Composable
private fun VendorActionMenu(
    actions: List<VendorStatusAction>,
    onDismiss: () -> Unit,
    onSelect: (VendorStatusAction) -> Unit
) {
    val tokens = LocalAppTokens.current

    // Transparent full-screen layer: tapping outside the card closes the menu
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            )
    ) {
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = VendorMenuTopOffset, end = 16.dp)
                .width(208.dp),
            shape = RoundedCornerShape(8.dp),
            color = whiteBg,
            shadowElevation = 8.dp,
            border = BorderStroke(1.dp, Color(0xFFF3F4F6))
        ) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                actions.forEach { action ->
                    Text(
                        text = action.label,
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Normal,
                        // Same text color as the web menu (gray-700)
                        color = Color(0xFF374151),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(action) }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// STATUS CONFIRMATION DIALOG (same layout as React ConfirmModal)
// ─────────────────────────────────────────────────────────────

@Composable
private fun VendorStatusConfirmDialog(
    action: VendorStatusAction,
    currentStatusLabel: String,
    isSaving: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val tokens = LocalAppTokens.current

    // Yellow for normal actions, red for the danger action (Mark Inactive)
    val accentColor = if (action.isDanger) VendorDangerRed else VendorWarningYellow

    Dialog(onDismissRequest = { if (!isSaving) onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = whiteBg
        ) {
            Column(modifier = Modifier.padding(20.dp)) {

                // Header row: warning icon + title on the left, close (X) on the right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ErrorOutline,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = action.label,
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier
                            .size(20.dp)
                            .clickable(enabled = !isSaving) { onDismiss() }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Message: "Change the status from <current> to <target>?" with highlighted values
                Text(
                    text = buildAnnotatedString {
                        append("Change the status from ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Medium, color = TextPrimary)) {
                            append(currentStatusLabel)
                        }
                        append(" to ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Medium, color = TextPrimary)) {
                            append(vendorStatusLabel(action.targetStatus))
                        }
                        append("?")
                    },
                    fontSize = tokens.bodySmall,
                    color = Color(0xFF4B5563)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Buttons: Cancel (outlined) and confirm (yellow / red), aligned to the right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isSaving,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFD1D5DB))
                    ) {
                        Text(
                            text = "Cancel",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF374151)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = onConfirm,
                        enabled = !isSaving,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accentColor,
                            disabledContainerColor = accentColor.copy(alpha = 0.6f)
                        )
                    ) {
                        Text(
                            text = if (isSaving) "Saving..." else action.label,
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Tab 0: Overview (Image 3) - 100% Dynamic
// ─────────────────────────────────────────────────────────────

@Composable
private fun VendorOverviewTab(vendor: LegalVendorItem) {
    val tokens = LocalAppTokens.current

    val agreementsScore = if (vendor.vendorAgreement.isNotEmpty()) 1.0f else 0.0f
    val slaScore = if (vendor.slaDocuments.isNotEmpty()) 1.0f else 0.0f
    val supportingScore = if (vendor.supportingDocuments.isNotEmpty()) 1.0f else 0.0f
    val totalScore = calculateComplianceScore(vendor) / 100f

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
    ) {
        // Vendor Legal Profile Card
        DetailCard {
            Column(modifier = Modifier.padding(tokens.screenPadding)) {
                Text(
                    text = "Vendor Legal Profile",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Primary
                )
                Spacer(modifier = Modifier.height(tokens.extraPadding))
                HorizontalDivider(color = light_grey, thickness = 1.dp)
                Spacer(modifier = Modifier.height(tokens.extraPadding))

                DetailInfoRow(label = "Vendor ID", value = vendor.legalVendorNumber.ifBlank { "N/A" })
                DetailInfoRow(label = "Vendor Name", value = vendor.vendor.ifBlank { "N/A" })
                DetailInfoRow(label = "Category", value = vendor.supplierType.ifBlank { "N/A" })
                DetailInfoRow(label = "Region / Jurisdiction", value = vendor.displayCountry.ifBlank { "N/A" })
                DetailInfoRow(
                    label = "Contract Reference",
                    value = vendor.contractReference?.contractName ?: vendor.agreementType.ifBlank { "N/A" },
                    valueColor = Primary
                )

                DetailBadgeRow(
                    label = "Status",
                    badge = DetailBadge(
                        text = vendorStatusLabel(vendor.status).ifBlank { "N/A" },
                        textColor = if (vendor.status.equals("Active", ignoreCase = true)) greentext else orangeText,
                        bgColor = if (vendor.status.equals("Active", ignoreCase = true)) greenBg else orangeBg,
                        showDot = true
                    )
                )

                DetailBadgeRow(
                    label = "Risk Level",
                    badge = DetailBadge(
                        text = "${vendor.riskLevel.ifBlank { "N/A" }} Risk",
                        textColor = Primary,
                        bgColor = primary_light,
                        showDot = true
                    ),
                    showDivider = false
                )
            }
        }

        // Compliance Track Card
        DetailCard {
            Column(modifier = Modifier.padding(tokens.screenPadding)) {
                Text(
                    text = "Compliance Track",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Primary
                )
                Spacer(modifier = Modifier.height(tokens.extraPadding))
                HorizontalDivider(color = light_grey, thickness = 1.dp)
                Spacer(modifier = Modifier.height(tokens.extraPadding))

                ComplianceProgressBarItem(
                    label = "Vendor Agreements",
                    progress = agreementsScore,
                    percentText = "${(agreementsScore * 100).toInt()}%",
                    barColor = complete_button_bg
                )
                ComplianceProgressBarItem(
                    label = "SLA Verification",
                    progress = slaScore,
                    percentText = "${(slaScore * 100).toInt()}%",
                    barColor = orangeText
                )
                ComplianceProgressBarItem(
                    label = "Supporting Checks",
                    progress = supportingScore,
                    percentText = "${(supportingScore * 100).toInt()}%",
                    barColor = complete_button_bg
                )
                ComplianceProgressBarItem(
                    label = "Overall Status Score",
                    progress = totalScore,
                    percentText = "${(totalScore * 100).toInt()}%",
                    barColor = Primary
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Tab 1: Contracts & Terms (Image 2) - 100% Dynamic
// ─────────────────────────────────────────────────────────────

@Composable
private fun VendorContractsTab(vendor: LegalVendorItem) {
    val tokens = LocalAppTokens.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
    ) {
        // Vendor & Compliance Summary Card
        DetailCard {
            Column(modifier = Modifier.padding(tokens.screenPadding)) {
                Text(
                    text = "Vendor & Compliance Summary",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Primary
                )
                Spacer(modifier = Modifier.height(tokens.extraPadding))
                HorizontalDivider(color = light_grey, thickness = 1.dp)
                Spacer(modifier = Modifier.height(tokens.extraPadding))

                Row(modifier = Modifier.fillMaxWidth()) {
                    DetailStackedField(
                        label = "Vendor Name",
                        value = vendor.vendor.ifBlank { "N/A" },
                        modifier = Modifier.weight(1f)
                    )
                    DetailStackedField(
                        label = "Legal Entity",
                        value = vendor.legalEntity.ifBlank { "N/A" },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth()) {
                    DetailStackedField(
                        label = "Contract Type",
                        value = vendor.supplierType.ifBlank { "N/A" },
                        modifier = Modifier.weight(1f)
                    )
                    DetailStackedField(
                        label = "Risk Profile",
                        value = vendor.riskLevel.ifBlank { "N/A" },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth()) {
                    DetailStackedField(
                        label = "Payment Terms",
                        value = vendor.paymentTerms.ifBlank { "N/A" },
                        modifier = Modifier.weight(1f)
                    )
                    DetailStackedField(
                        label = "Start Date",
                        value = formatIsoDate(vendor.startDate),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Primary Contact & Compliance Card
        DetailCard {
            Column(modifier = Modifier.padding(tokens.screenPadding)) {
                Text(
                    text = "Primary Contact & Compliance",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Primary
                )
                Spacer(modifier = Modifier.height(tokens.extraPadding))
                HorizontalDivider(color = light_grey, thickness = 1.dp)
                Spacer(modifier = Modifier.height(tokens.extraPadding))

                Row(modifier = Modifier.fillMaxWidth()) {
                    DetailStackedField(
                        label = "Primary Contact",
                        value = vendor.procurementOwner?.fullName?.ifBlank { "N/A" } ?: "N/A",
                        modifier = Modifier.weight(1f)
                    )
                    DetailStackedField(
                        label = "Legal Owner",
                        value = vendor.legalOwner?.fullName?.ifBlank { "N/A" } ?: "N/A",
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth()) {
                    DetailStackedField(
                        label = "End Date",
                        value = formatIsoDate(vendor.endDate),
                        modifier = Modifier.weight(1f)
                    )
                    DetailStackedField(
                        label = "Renewal Date",
                        value = formatIsoDate(vendor.renewalDate),
                        modifier = Modifier.weight(1f)
                    )
                }

                DetailBadgeRow(
                    label = "Compliance Status",
                    badge = DetailBadge(
                        text = vendorStatusLabel(vendor.status).ifBlank { "N/A" },
                        textColor = if (vendor.status.equals("Active", ignoreCase = true)) greentext else orangeText,
                        bgColor = if (vendor.status.equals("Active", ignoreCase = true)) greenBg else orangeBg,
                        showDot = true
                    ),
                    showDivider = false
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Tab 2: Documents & Compliance (Image 4) - 100% Dynamic List
// ─────────────────────────────────────────────────────────────

@Composable
private fun VendorDocumentsTab(vendor: LegalVendorItem) {
    val tokens = LocalAppTokens.current

    // Combine all uploaded document collections directly from the API response
    val allDocuments: List<LegalVendorDocumentItem> = remember(vendor) {
        vendor.vendorAgreement + vendor.slaDocuments + vendor.supportingDocuments
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Vendor Legal Documents",
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = title_color
            )

            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(tokens.cardCornerRadius),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Upload,
                    contentDescription = null,
                    tint = whiteBg,
                    modifier = Modifier.size(tokens.iconSize * 0.8f)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Upload",
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Medium,
                    color = whiteBg
                )
            }
        }

        DetailCard {
            Column(modifier = Modifier.padding(tokens.screenPadding)) {
                if (allDocuments.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = tokens.extraPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No documents uploaded for this vendor",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = mutedText
                        )
                    }
                } else {
                    // Dynamic Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DOCUMENT",
                            fontSize = tokens.label,
                            color = mutedText,
                            modifier = Modifier.weight(1.5f)
                        )
                        Text(
                            text = "TYPE",
                            fontSize = tokens.label,
                            color = mutedText,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "STATUS",
                            fontSize = tokens.label,
                            color = mutedText,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "DATE",
                            fontSize = tokens.label,
                            color = mutedText,
                            modifier = Modifier.weight(0.8f)
                        )
                    }

                    HorizontalDivider(color = light_grey, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(tokens.extraPadding))

                    // Dynamic List Rendering from API
                    allDocuments.forEachIndexed { index, doc ->
                        val fileName = doc.publicId.substringAfterLast("/").ifBlank { doc.documentType }
                        val fileType = doc.documentType.ifBlank { "File" }
                        val fileStatus = if (doc.documentUrl.isNotBlank()) "Active" else "Pending"
                        val fileDate = formatIsoDate(doc.uploadedAt)

                        DocumentRowItem(
                            title = fileName,
                            type = fileType,
                            statusText = fileStatus,
                            statusBg = if (fileStatus == "Active") greenBg else yellowBg,
                            statusTextColor = if (fileStatus == "Active") greentext else yellowText,
                            date = fileDate
                        )

                        if (index < allDocuments.lastIndex) {
                            Spacer(modifier = Modifier.height(tokens.extraPadding))
                            HorizontalDivider(color = light_grey, thickness = 1.dp)
                            Spacer(modifier = Modifier.height(tokens.extraPadding))
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Tab 3: Disputes & Notices (Image 5)
// ─────────────────────────────────────────────────────────────

@Composable
private fun VendorDisputesTab() {
    val tokens = LocalAppTokens.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Disputes & Legal Notices",
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Primary
            )

//            Button(
//                onClick = {},
//                shape = RoundedCornerShape(tokens.cardCornerRadius),
//                colors = ButtonDefaults.buttonColors(containerColor = Primary),
//                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
//            ) {
//                Icon(
//                    imageVector = Icons.Default.Add,
//                    contentDescription = null,
//                    tint = whiteBg,
//                    modifier = Modifier.size(tokens.iconSize * 0.8f)
//                )
//                Spacer(modifier = Modifier.width(4.dp))
//                Text(
//                    text = "Add Notice",
//                    fontSize = tokens.caption,
//                    fontWeight = FontWeight.Medium,
//                    color = whiteBg
//                )
//            }
        }

        DetailCard {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(tokens.screenPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(Primary_background, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = iconMuted,
                        modifier = Modifier.size(tokens.iconSize * 1.2f)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No active disputes or notices for this vendor.",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Tab 4: History (Image 6) - 100% Dynamic Timeline from API
// ─────────────────────────────────────────────────────────────

@Composable
private fun VendorHistoryTab(vendor: LegalVendorItem) {
    val tokens = LocalAppTokens.current

    DetailCard {
        Column(modifier = Modifier.padding(tokens.screenPadding)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Vendor Legal Activity",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = title_color
                )

                OutlinedButton(
                    onClick = {},
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    border = BorderStroke(1.dp, BorderGray),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                        tint = headerGrey,
                        modifier = Modifier.size(tokens.iconSize * 0.8f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Filter Activity",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Medium,
                        color = headerGrey
                    )
                }
            }

            Spacer(modifier = Modifier.height(tokens.extraPadding))
            HorizontalDivider(color = light_grey, thickness = 1.dp)
            Spacer(modifier = Modifier.height(tokens.extraPadding))

            if (vendor.activity.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = tokens.extraPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No activity recorded for this vendor",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = mutedText
                    )
                }
            } else {
                vendor.activity.forEachIndexed { index, act ->
                    val dotColor = when {
                        act.action.contains("Created", ignoreCase = true) -> complete_button_bg
                        act.action.contains("Updated", ignoreCase = true) -> yellowText
                        act.action.contains("Status", ignoreCase = true) -> Primary
                        else -> iconMuted
                    }

                    VendorActivityRow(
                        dotColor = dotColor,
                        title = act.action.replace("_", " "),
                        timestamp = formatIsoDate(act.performedAt),
                        description = act.description.ifBlank { "Activity logged" },
                        performedBy = if (act.performedBy.isNotBlank()) "Performed by ID: ${act.performedBy}" else "System"
                    )

                    if (index < vendor.activity.lastIndex) {
                        Spacer(modifier = Modifier.height(tokens.extraPadding))
                        HorizontalDivider(color = light_grey, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(tokens.extraPadding))
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Dynamic Sub-components
// ─────────────────────────────────────────────────────────────

@Composable
private fun VendorActivityRow(
    dotColor: Color,
    title: String,
    timestamp: String,
    description: String,
    performedBy: String
) {
    val tokens = LocalAppTokens.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(10.dp)
                .background(dotColor, CircleShape)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Text(
                text = timestamp,
                fontSize = tokens.caption,
                color = mutedText
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                fontSize = tokens.bodySmall,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = performedBy,
                fontSize = tokens.caption,
                color = mutedText
            )
        }
    }
}

@Composable
private fun ComplianceProgressBarItem(
    label: String,
    progress: Float,
    percentText: String,
    barColor: Color
) {
    val tokens = LocalAppTokens.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Text(
                text = percentText,
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.Medium,
                color = TextSecondary
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        DataCardProgressBar(
            progress = progress,
            progressColor = barColor,
            trackColor = light_grey,
            height = 6.dp
        )
    }
}

@Composable
private fun DocumentRowItem(
    title: String,
    type: String,
    statusText: String,
    statusBg: Color,
    statusTextColor: Color,
    date: String
) {
    val tokens = LocalAppTokens.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1.5f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(primary_light, RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(tokens.iconSize * 0.8f)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Text(
            text = type,
            fontSize = tokens.caption,
            color = TextSecondary,
            modifier = Modifier.weight(1f)
        )

        Box(modifier = Modifier.weight(1f)) {
            DetailPill(
                badge = DetailBadge(
                    text = statusText,
                    textColor = statusTextColor,
                    bgColor = statusBg,
                    showDot = false
                )
            )
        }

        Text(
            text = date,
            fontSize = tokens.caption,
            color = headerGrey,
            modifier = Modifier.weight(0.8f)
        )
    }
}