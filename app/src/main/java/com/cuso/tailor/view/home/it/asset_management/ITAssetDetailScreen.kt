@file:Suppress("AssignedValueIsNeverRead", "unused")
package com.cuso.tailor.view.home.it.asset_management

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.it.AssetDetailDto
import com.cuso.tailor.model.it.AssignAssetRequest
import com.cuso.tailor.model.it.OrgMemberDto
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.Primary_background
import com.cuso.tailor.ui.theme.TextPrimary
import com.cuso.tailor.ui.theme.TextSecondary
import com.cuso.tailor.ui.theme.disabled
import com.cuso.tailor.ui.theme.greentext
import com.cuso.tailor.ui.theme.light_blue
import com.cuso.tailor.ui.theme.light_grey
import com.cuso.tailor.ui.theme.mutedText
import com.cuso.tailor.ui.theme.primary_light
import com.cuso.tailor.ui.theme.redBg
import com.cuso.tailor.ui.theme.redText
import com.cuso.tailor.ui.theme.sectionBorder
import com.cuso.tailor.ui.theme.title_color
import com.cuso.tailor.ui.theme.whiteBg
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.view.composable.CirculerProgressIndicatorSmall
import com.cuso.tailor.view.composable.FormDropdown
import com.cuso.tailor.view.composable.ListSkeleton
import com.cuso.tailor.view.composable.StatusBadge
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.viewmodel.ItViewModel

@Composable
fun ItAssetDetailScreen(
    assetId: String,
    viewModel: ItViewModel,
    onNavigateBack: () -> Unit,
    onNavigateDevice: ((String) -> Unit)? = null
) {
    val tokens = LocalAppTokens.current
    val asset by viewModel.assetDetail.collectAsState()
    val isLoading by viewModel.isLoadingAssetDetail.collectAsState()

    var showDisposalDialog by remember { mutableStateOf(false) }
    var showAssignDialog by remember { mutableStateOf(false) }

    LaunchedEffect(assetId) {
        viewModel.fetchAssetDetail(assetId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary_background)
    ) {
        TitleBar(
            title = "Asset Details",
            onClose = onNavigateBack
        )

        if (isLoading && asset == null) {
            ListSkeleton()
        } else if (asset != null) {
            val item = asset!!

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = tokens.screenPadding, vertical = tokens.screenPadding / 2),
                verticalArrangement = Arrangement.spacedBy(tokens.screenPadding)
            ) {
                // Top Action Card
                TopAssetCard(
                    asset = item,
                    onEditClick = { /* Handled in edit flow */ },
                    onDisposalClick = { showDisposalDialog = true },
                    onViewDeviceClick = {
                        item.linkedDeviceId?.let { devId ->
                            onNavigateDevice?.invoke(devId)
                        }
                    }
                )

                // Disposal Alert banner if asset is marked
                if (item.isMarkedForDisposal) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(tokens.cardCornerRadius))
                            .background(redBg)
                            .border(1.dp, redText, RoundedCornerShape(tokens.cardCornerRadius))
                            .padding(tokens.screenPadding),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Disposal Warning",
                            tint = redText,
                            modifier = Modifier.size(tokens.iconSize)
                        )
                        Spacer(Modifier.width(tokens.screenPadding / 2))
                        Text(
                            text = "Asset marked for disposal. Reason: ${item.disposalReason ?: "Decommissioning in progress"}",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = redText
                        )
                    }
                }

                // Asset Information Card
                DetailSectionCard(
                    title = "Asset Information",
                    icon = Icons.Default.Info
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(tokens.screenPadding * 0.75f)) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            DetailGridField(modifier = Modifier.weight(1f), label = "Asset Name", value = item.assetName)
                            DetailGridField(modifier = Modifier.weight(1f), label = "Asset ID", value = item.assetId, isBadge = true)
                        }
                        Row(modifier = Modifier.fillMaxWidth()) {
                            DetailGridField(modifier = Modifier.weight(1f), label = "Type", value = item.type)
                            DetailGridField(modifier = Modifier.weight(1f), label = "Category", value = item.category ?: "--")
                        }
                        Row(modifier = Modifier.fillMaxWidth()) {
                            DetailGridField(modifier = Modifier.weight(1f), label = "Manufacturer", value = item.manufacturer ?: "--")
                            DetailGridField(modifier = Modifier.weight(1f), label = "Model", value = item.model ?: "--")
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Status", fontSize = tokens.bodySmall, color = mutedText)
                            Text(text = item.status, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = greentext)
                        }
                    }
                }

                // Purchase Information Card
                DetailSectionCard(
                    title = "Purchase Information",
                    icon = Icons.Default.Info
                ) {
                    val purchase = item.purchaseInfo
                    val currencySym = if (purchase?.currency == "USD") "$" else "₹"
                    val costFormatted = purchase?.cost?.let { "$currencySym${it.toInt()}" } ?: "--"

                    Column(verticalArrangement = Arrangement.spacedBy(tokens.screenPadding * 0.75f)) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            DetailGridField(
                                modifier = Modifier.weight(1f),
                                label = "Purchase Date",
                                value = purchase?.purchaseDate?.take(10) ?: "--",
                                showDot = true
                            )
                            DetailGridField(modifier = Modifier.weight(1f), label = "Cost", value = costFormatted)
                        }
                        Row(modifier = Modifier.fillMaxWidth()) {
                            DetailGridField(modifier = Modifier.weight(1f), label = "Vendor", value = purchase?.vendor ?: "--")
                            DetailGridField(
                                modifier = Modifier.weight(1f),
                                label = "Warranty",
                                value = purchase?.warrantyUntil?.let { "Valid until ${it.take(10)}" } ?: "--"
                            )
                        }
                    }
                }

                // Current Assignee Card
                DetailSectionCard(title = "Current Assignee") {
                    val assignee = item.currentAssignee
                    val initials = assignee?.name?.take(2)?.uppercase() ?: "--"

                    Column(verticalArrangement = Arrangement.spacedBy(tokens.screenPadding)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(tokens.screenPadding)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(tokens.imageDp * 0.8f)
                                    .clip(CircleShape)
                                    .background(primary_light),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = initials,
                                    fontSize = tokens.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = Primary
                                )
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(tokens.screenPadding / 6)) {
                                Text(
                                    text = assignee?.name ?: "Unassigned",
                                    fontSize = tokens.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "${assignee?.empId ?: ""} • ${assignee?.department ?: ""}",
                                    fontSize = tokens.caption,
                                    color = mutedText
                                )
                                Text(
                                    text = "Assigned on: ${assignee?.assignedOn?.take(10) ?: "--"}",
                                    fontSize = tokens.caption,
                                    color = mutedText
                                )
                            }
                        }

                        Button(
                            onClick = { showAssignDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            shape = RoundedCornerShape(tokens.cardCornerRadius),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(tokens.buttonHeight)
                        ) {
                            Text(
                                text = "Manage Assignment",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = whiteBg
                            )
                        }
                    }
                }

                // Current Location Card
                DetailSectionCard(
                    title = "Current Location",
                    icon = Icons.Default.Place
                ) {
                    val location = item.location
                    Column(verticalArrangement = Arrangement.spacedBy(tokens.screenPadding * 0.75f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Branch", fontSize = tokens.bodySmall, color = mutedText)
                            Text(text = location?.branchName ?: "--", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary)
                        }
                        HorizontalDivider(thickness = 1.dp, color = sectionBorder)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Department", fontSize = tokens.bodySmall, color = mutedText)
                            Text(text = location?.department ?: "--", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary)
                        }
                        HorizontalDivider(thickness = 1.dp, color = sectionBorder)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Location Details", fontSize = tokens.bodySmall, color = mutedText)
                            Text(text = location?.locationDetails ?: "--", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary)
                        }
                    }
                }

                Spacer(Modifier.height(tokens.screenPadding * 2))
            }
        }
    }

    if (showDisposalDialog) {
        DisposalDialog(
            onDismiss = { showDisposalDialog = false },
            onConfirm = { reason ->
                viewModel.markAssetForDisposal(
                    assetId = assetId,
                    reason = reason,
                    onSuccess = { showDisposalDialog = false },
                    onError = { }
                )
            }
        )
    }

    val orgMembers by viewModel.orgMembers.collectAsState()

    LaunchedEffect(showAssignDialog) {
        if (showAssignDialog) {
            viewModel.fetchUsersForAssignment()
        }
    }

    var isAssigning by remember { mutableStateOf(false) }

    if (showAssignDialog) {
        AssignDialog(
            members = orgMembers,
            initialAssignee = asset?.currentAssignee?.name ?: "",
            initialEmpId = asset?.currentAssignee?.empId ?: "",
            initialDepartment = asset?.location?.department ?: "",
            initialBranch = asset?.location?.branchName ?: "",
            initialUserId = asset?.currentAssignee?.userId,
            isLoading = isAssigning,
            onDismiss = {
                if (!isAssigning) showAssignDialog = false
            },
            onConfirm = { req ->
                isAssigning = true
                viewModel.assignAsset(
                    assetId = assetId,
                    request = req,
                    onSuccess = { message ->
                        isAssigning = false
                        showAssignDialog = false
                        DynamicIslandManager.showSuccess(message)
                    },
                    onError = { errorMsg ->
                        isAssigning = false
                        DynamicIslandManager.showError(errorMsg)
                    }
                )
            }
        )
    }
}

@Composable
private fun TopAssetCard(
    asset: AssetDetailDto,
    onEditClick: () -> Unit,
    onDisposalClick: () -> Unit,
    onViewDeviceClick: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val (statusBg, statusText) = getAssetStatusColors(asset.status)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(tokens.cardCornerRadius))
            .background(whiteBg)
            .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius))
            .padding(tokens.screenPadding)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(tokens.screenPadding * 0.75f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = asset.assetName,
                    fontSize = tokens.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                StatusBadge(
                    text = asset.status,
                    bgColor = statusBg,
                    textColor = statusText,
                    showDot = true
                )
            }

            Text(
                text = "${asset.type} · ${asset.assetId} · ${asset.location?.branchName ?: ""}",
                fontSize = tokens.bodySmall,
                color = mutedText
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(tokens.screenPadding / 2)
            ) {
//                OutlinedActionButton(
//                    modifier = Modifier.weight(1f),
//                    label = "Edit",
//                    icon = Icons.Default.Edit,
//                    onClick = onEditClick
//                )
                OutlinedActionButton(
                    modifier = Modifier.weight(1f),
                    label = "Mark for Disposal",
                    icon = Icons.Default.DeleteOutline,
                    onClick = onDisposalClick
                )
            }

//            Button(
//                onClick = onViewDeviceClick,
//                colors = ButtonDefaults.buttonColors(containerColor = Primary),
//                shape = RoundedCornerShape(tokens.cardCornerRadius),
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(tokens.buttonHeight)
//            ) {
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.spacedBy(tokens.screenPadding / 3)
//                ) {
//                    Text(
//                        text = "View Device",
//                        fontSize = tokens.bodySmall,
//                        fontWeight = FontWeight.Medium,
//                        color = whiteBg
//                    )
//                    Icon(
//                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
//                        contentDescription = null,
//                        tint = whiteBg,
//                        modifier = Modifier.size(tokens.iconSize)
//                    )
//                }
//            }
        }
    }
}

@Composable
private fun OutlinedActionButton(
    modifier: Modifier = Modifier,
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val tokens = LocalAppTokens.current

    Row(
        modifier = modifier
            .height(tokens.buttonHeight)
            .clip(RoundedCornerShape(tokens.cardCornerRadius))
            .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius))
            .background(whiteBg)
            .clickable { onClick() }
            .padding(horizontal = tokens.screenPadding / 2),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = title_color,
            modifier = Modifier.size(tokens.iconSize)
        )
        Spacer(Modifier.width(tokens.screenPadding / 4))
        Text(
            text = label,
            fontSize = tokens.bodySmall,
            fontWeight = FontWeight.Medium,
            color = title_color
        )
    }
}

@Composable
private fun DetailSectionCard(
    title: String,
    icon: ImageVector? = null,
    content: @Composable () -> Unit
) {
    val tokens = LocalAppTokens.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(tokens.cardCornerRadius))
            .background(whiteBg)
            .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius))
            .padding(tokens.screenPadding)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(tokens.screenPadding)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(tokens.screenPadding / 3)
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(tokens.iconSize)
                    )
                }
                Text(
                    text = title,
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Primary
                )
            }
            content()
        }
    }
}

@Composable
private fun DetailGridField(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    isBadge: Boolean = false,
    showDot: Boolean = false
) {
    val tokens = LocalAppTokens.current

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(tokens.screenPadding / 6)) {
        Text(
            text = label,
            fontSize = tokens.caption,
            color = mutedText
        )
        if (isBadge) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(tokens.cardCornerRadius / 2))
                    .background(light_blue)
                    .padding(horizontal = tokens.screenPadding / 3, vertical = tokens.screenPadding / 8)
            ) {
                Text(
                    text = value,
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = Primary
                )
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (showDot) {
                    Box(
                        modifier = Modifier
                            .size(tokens.iconSize / 3)
                            .clip(CircleShape)
                            .background(greentext)
                    )
                    Spacer(Modifier.width(tokens.screenPadding / 4))
                }
                Text(
                    text = value,
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
            }
        }
    }
}

@Composable
private fun DisposalDialog(
    onDismiss: () -> Unit,
    onConfirm: (reason: String) -> Unit
) {
    val tokens = LocalAppTokens.current
    var reason by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg
        ) {
            Column(
                modifier = Modifier.padding(tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(tokens.screenPadding)
            ) {
                Text(
                    text = "Mark Asset for Disposal",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = title_color
                )
                Text(
                    text = "Enter disposal reason to decommission this asset.",
                    fontSize = tokens.bodySmall,
                    color = TextSecondary
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(tokens.fieldHeight)
                        .clip(RoundedCornerShape(tokens.cardCornerRadius))
                        .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius))
                        .padding(horizontal = tokens.screenPadding / 2),
                    contentAlignment = Alignment.CenterStart
                ) {
                    BasicTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle = TextStyle(fontSize = tokens.bodySmall, color = TextPrimary),
                        cursorBrush = SolidColor(Primary),
                        decorationBox = { inner ->
                            if (reason.isEmpty()) {
                                Text("Reason for disposal", fontSize = tokens.bodySmall, color = mutedText)
                            }
                            inner()
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(tokens.screenPadding / 2)
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = light_grey),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", fontSize = tokens.bodySmall, color = title_color)
                    }
                    Button(
                        onClick = { onConfirm(reason) },
                        colors = ButtonDefaults.buttonColors(containerColor = redText),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Confirm", fontSize = tokens.bodySmall, color = whiteBg)
                    }
                }
            }
        }
    }
}

@Composable
private fun AssignDialog(
    members: List<OrgMemberDto>,
    initialAssignee: String,
    initialEmpId: String,
    initialDepartment: String,
    initialBranch: String,
    initialUserId: String? = null,
    isLoading: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (AssignAssetRequest) -> Unit
) {
    val tokens = LocalAppTokens.current

    // Initialize dropdown text and selected fields
    val unassignedLabel = "Unassigned"
    val initialSelection = remember(initialAssignee, initialEmpId) {
        if (initialAssignee.isNotBlank() && initialAssignee != "Unassigned") {
            if (initialEmpId.isNotBlank()) "$initialAssignee [$initialEmpId]" else initialAssignee
        } else {
            "Select User / Employee"
        }
    }

    var selectedDropdownText by remember { mutableStateOf(initialSelection) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    var assigneeName by remember { mutableStateOf(initialAssignee.ifBlank { "Unassigned" }) }
    var empId by remember { mutableStateOf(initialEmpId) }
    var department by remember { mutableStateOf(initialDepartment) }
    var branchName by remember { mutableStateOf(initialBranch) }
    var selectedUserId by remember { mutableStateOf(initialUserId) }

    val dropdownOptions = remember(members) {
        listOf(unassignedLabel) + members.map { it.dropdownLabel }
    }

    Dialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = !isLoading,
            dismissOnClickOutside = !isLoading
        )
    ) {
        Surface(
            shape = RoundedCornerShape(tokens.cardCornerRadius * 1.2f),
            color = whiteBg,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(tokens.screenPadding * 0.75f)
            ) {
                // Header with title and close icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Manage Asset Assignment",
                        fontSize = tokens.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = title_color
                    )
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = mutedText,
                        modifier = Modifier
                            .size(tokens.iconSize * 1.1f)
                            .clickable(enabled = !isLoading) { onDismiss() }
                    )
                }

                HorizontalDivider(color = sectionBorder, thickness = 1.dp)

                // Select User / Employee Dropdown
                Column(verticalArrangement = Arrangement.spacedBy(tokens.screenPadding * 0.25f)) {
                    FormDropdown(
                        label = "Select User / Employee *",
                        value = selectedDropdownText,
                        expanded = dropdownExpanded && !isLoading,
                        onExpandChange = { if (!isLoading) dropdownExpanded = it },
                        options = dropdownOptions,
                        isRequired = false,
                        enabled = !isLoading,
                        onOptionSelected = { chosen ->
                            selectedDropdownText = chosen
                            if (chosen == unassignedLabel) {
                                assigneeName = "Unassigned"
                                empId = ""
                                department = ""
                                branchName = ""
                                selectedUserId = null
                            } else {
                                val matched = members.find { it.dropdownLabel == chosen }
                                matched?.let { m ->
                                    assigneeName = m.fullName
                                    empId = m.memberId ?: m.employeeCode.orEmpty()
                                    department = m.departmentId?.name.orEmpty()
                                    branchName = m.branchId?.name.orEmpty()
                                    selectedUserId = m.userId?.id ?: m.id
                                }
                            }
                        }
                    )

                    // Helper caption note
                    Text(
                        text = "Select an employee to auto-fill details, or select 'Unassigned' to detach.",
                        fontSize = tokens.caption,
                        color = mutedText
                    )
                }

                // Summary info preview card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.8f))
                        .background(Primary_background)
                        .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius * 0.8f))
                        .padding(tokens.screenPadding * 0.9f)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(tokens.screenPadding * 0.6f)) {
                        AssigneeInfoRow(label = "Assignee Name:", value = assigneeName)
                        AssigneeInfoRow(label = "Employee ID:", value = empId.ifEmpty { "—" })
                        AssigneeInfoRow(label = "Department:", value = department.ifEmpty { "—" })
                        AssigneeInfoRow(label = "Branch:", value = branchName.ifEmpty { "—" })
                    }
                }

                HorizontalDivider(color = sectionBorder, thickness = 1.dp)

                // Bottom actions (Cancel & Confirm Assignment)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(tokens.screenPadding * 0.5f)
                ) {
                    Button(
                        onClick = onDismiss,
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = whiteBg,
                            disabledContainerColor = light_grey
                        ),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, sectionBorder),
                        modifier = Modifier
                            .weight(1f)
                            .height(tokens.buttonHeight)
                    ) {
                        Text(
                            text = "Cancel",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = title_color
                        )
                    }

                    Button(
                        onClick = {
                            onConfirm(
                                AssignAssetRequest(
                                    assigneeName = assigneeName.trim(),
                                    branchName = branchName.trim().ifBlank { null },
                                    department = department.trim().ifBlank { null },
                                    empId = empId.trim().ifBlank { null },
                                    userId = selectedUserId
                                )
                            )
                        },
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary,
                            disabledContainerColor = disabled
                        ),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(tokens.buttonHeight)
                    ) {
                        if (isLoading) {
                            CirculerProgressIndicatorSmall()
                        } else {
                            Text(
                                text = "Confirm",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = whiteBg
                            )
                        }
                    }
                }
            }
        }
    }
}
@Composable
private fun AssigneeInfoRow(
    label: String,
    value: String
) {
    val tokens = LocalAppTokens.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = tokens.bodySmall,
            color = mutedText
        )
        Text(
            text = value,
            fontSize = tokens.bodySmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
    }
}
