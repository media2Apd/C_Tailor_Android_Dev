@file:Suppress("unused", "AssignedValueIsNeverRead")

package com.cuso.tailor.view.home.legal.legal_contract

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.hr.MemberItem
import com.cuso.tailor.model.legal.LegalContractDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.CirculerProgressIndicatorReuse
import com.cuso.tailor.view.composable.DatePickerField
import com.cuso.tailor.view.composable.FormDropdown
import com.cuso.tailor.view.composable.FormLabel
import com.cuso.tailor.view.composable.FormTextArea
import com.cuso.tailor.view.composable.FormTextField
import com.cuso.tailor.view.composable.TabItem
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.view.home.legal.detail_template.*
import com.cuso.tailor.viewmodel.HrViewModel
import com.cuso.tailor.viewmodel.LegalViewModel

// ─────────────────────────────────────────────────────────────
// Badge color helpers
// ─────────────────────────────────────────────────────────────

private fun statusBadge(label: String): DetailBadge = when (label.lowercase()) {
    "expired" -> DetailBadge(label, redText, redBg)
    "expiring soon", "renewal notice" -> DetailBadge(label, yellowText, yellowBg)
    else -> DetailBadge(label, greentext, greenBg)
}

private fun riskBadge(level: String, text: String = level): DetailBadge = when (level.lowercase()) {
    "high", "critical" -> DetailBadge(text, redText, redBg)
    "medium" -> DetailBadge(text, yellowText, yellowBg)
    else -> DetailBadge(text, greentext, greenBg)
}

// ─────────────────────────────────────────────────────────────
// Screen Entry Point
// ─────────────────────────────────────────────────────────────

@Composable
fun ContractOverviewScreen(
    contractId: String,
    onClose: () -> Unit = {},
    viewModel: LegalViewModel = hiltViewModel(),
    hrViewModel: HrViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current

    val contractDto by viewModel.contractDetail.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingContractDetail.collectAsStateWithLifecycle()
    val errorMessage by viewModel.contractDetailError.collectAsStateWithLifecycle()

    val rawMemberList: List<MemberItem> by hrViewModel.members.collectAsStateWithLifecycle()

    val ownerOptions: List<Pair<String, String>> = remember(rawMemberList) {
        rawMemberList.map { member ->
            val memberId = member._id
            val fullName = listOfNotNull(member.firstName, member.lastName)
                .filter { it.isNotBlank() }
                .joinToString(" ")
            val displayName = fullName.ifBlank { "Member" }
            Pair(memberId, displayName)
        }
    }

    LaunchedEffect(contractId) {
        viewModel.loadContractDetail(contractId)
        hrViewModel.fetchMembers(page = 1, limit = 100)
    }

    val contract = remember(contractDto, contractId) {
        contractDto?.takeIf { it.id == contractId }?.toContractDetailUi()
    }

    // Modal state for Renew Contract Dialog
    var showRenewDialog by remember { mutableStateOf(false) }

    // Modal state for Confirmation of Status Transitions (Start Review, Submit, Approve, Reject, etc.)
    var pendingStatusAction by remember { mutableStateOf<RenewalActionConfig?>(null) }

    // 1. Propose Renewal Dialog
    if (showRenewDialog && contract != null) {
        RenewContractDialog(
            currentEndDate = contract.expiry,
            initialOwnerId = contractDto?.renewalOwner?.id ?: contractDto?.internalBusinessOwner?.id,
            ownerOptions = ownerOptions,
            onDismiss = { showRenewDialog = false },
            onSubmit = { start: String, end: String, value: String, ownerId: String, terms: String ->
                showRenewDialog = false
                viewModel.renewContract(
                    contractId = contractId,
                    proposedStartDate = start,
                    proposedEndDate = end,
                    renewalValue = value,
                    renewalOwnerId = ownerId,
                    renewalTerms = terms,
                    onSuccess = {
                        Toast.makeText(context, "Renewal initiated successfully", Toast.LENGTH_SHORT).show()
                        viewModel.loadContractDetail(contractId)
                    },
                    onError = { err ->
                        Toast.makeText(context, err ?: "Failed to renew contract", Toast.LENGTH_LONG).show()
                    }
                )
            }
        )
    }

    // 2. Status Action Confirmation Dialog
    pendingStatusAction?.let { action ->
        ContractStatusActionDialog(
            action = action,
            currentStatus = contractDto?.renewalStatus ?: "",
            onDismiss = { pendingStatusAction = null },
            onConfirm = { comments ->
                val targetStatus = action.status
                pendingStatusAction = null
                viewModel.updateContractRenewalStatus(
                    contractId = contractId,
                    status = targetStatus,
                    approvalComments = comments,
                    onSuccess = {
                        Toast.makeText(context, "Renewal moved to ${formatRenewalLabel(targetStatus)}", Toast.LENGTH_SHORT).show()
                        viewModel.loadContractDetail(contractId)
                    },
                    onError = { err ->
                        Toast.makeText(context, err ?: "Failed to update renewal status", Toast.LENGTH_LONG).show()
                    }
                )
            }
        )
    }

    if (contract != null) {
        ContractOverviewContent(
            contract = contract,
            contractDto = contractDto,
            onClose = onClose,
            onRenewClick = { showRenewDialog = true },
            onStatusActionClick = { action -> pendingStatusAction = action }
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Primary_background)
        ) {
            TitleBar(title = "Contract Overview", onClose = onClose)

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (errorMessage != null && !isLoading) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = errorMessage ?: "Contract not found",
                            fontSize = tokens.bodySmall,
                            color = redText
                        )
                        Spacer(Modifier.height(tokens.extraPadding))
                        Button(
                            onClick = { viewModel.loadContractDetail(contractId) },
                            modifier = Modifier.height(tokens.buttonHeight),
                            shape = RoundedCornerShape(tokens.cardCornerRadius),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary)
                        ) {
                            Text(
                                text = "Retry",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = whiteBg
                            )
                        }
                    }
                } else {
                    CirculerProgressIndicatorReuse()
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Content with Reusable Detail Screen Template
// ─────────────────────────────────────────────────────────────

@Composable
private fun ContractOverviewContent(
    contract: ContractDetailUi,
    contractDto: LegalContractDto?,
    onClose: () -> Unit,
    onRenewClick: () -> Unit,
    onStatusActionClick: (RenewalActionConfig) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    val tabs = remember {
        listOf(
            TabItem("Overview"),
            TabItem("Parties"),
            TabItem("Terms"),
            TabItem("Documents"),
            TabItem("Renewal"),
            TabItem("History")
        )
    }

    val status = statusBadge(contract.statusLabel)
    val risk = riskBadge(contract.riskLabel)

    DetailScreenTemplate(
        title = "Contract Overview",
        onClose = onClose,
        header = DetailHeaderData(
            icon = Icons.Default.Description,
            badges = listOf(
                status,
                riskBadge(contract.riskLabel, "${contract.riskLabel} Risk")
            ),
            title = contract.title,
            metaItems = listOf(
                contract.code,
                contract.type,
                contract.location,
                "Owner: ${contract.owner}"
            ),
            highlightStats = listOf(
                DetailStat("Time Left", contract.timeLeft),
                DetailStat("Expiry Date", contract.expiry)
            ),
            primaryAction = DetailAction("Renew Contract", Icons.Default.Autorenew, onClick = onRenewClick),
            secondaryActions = listOf(
                DetailAction("Edit", Icons.Default.Edit),
                DetailAction("Download", Icons.Default.Download),
                DetailAction("Share", Icons.Default.Share),
                DetailAction("More", Icons.Default.MoreVert)
            )
        ),
        summaryStats = listOf(
            DetailStat("Status", contract.statusLabel, status.textColor, showDot = true),
            DetailStat("Risk", contract.riskLabel, risk.textColor, showDot = true),
            DetailStat("Expiry", contract.expiry),
            DetailStat("Owner", contract.owner),
            DetailStat("Party", contract.partyName)
        ),
        tabs = tabs,
        selectedTabIndex = selectedTab,
        onTabSelected = { selectedTab = it },
        banner = contract.bannerMessage?.let {
            DetailBannerData(
                title = "Renewal Attention",
                message = it,
                actionLabel = "Review Renewal",
                onAction = { selectedTab = 4 }
            )
        }
    ) { index ->
        when (index) {
            0 -> OverviewTab(contract)
            1 -> PartiesTab(contract)
            2 -> TermsTab(contract)
            3 -> DocumentsTab(contract)
            4 -> RenewalTab(
                c = contract,
                contractDto = contractDto,
                onStartRenewalClick = onRenewClick,
                onStatusActionClick = onStatusActionClick
            )
            else -> HistoryTab(contract)
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Tab Implementations
// ─────────────────────────────────────────────────────────────

@Composable
private fun OverviewTab(c: ContractDetailUi) {
    val tokens = LocalAppTokens.current
    val shape = RoundedCornerShape(tokens.cardCornerRadius)
    val status = statusBadge(c.statusLabel)

    DetailSectionCard(
        title = "Contract Information",
        subtitle = "Key details and lifecycle information for this agreement."
    ) {
        Spacer(Modifier.height(tokens.extraPadding))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(status.bgColor, shape)
                .border(1.dp, status.textColor.copy(alpha = 0.3f), shape)
                .padding(horizontal = 12.dp, vertical = tokens.extraPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Lifecycle Status", fontSize = tokens.bodySmall, color = status.textColor)
            DetailPill(status)
        }

        Spacer(Modifier.height(tokens.extraPadding))

        Row(horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
            DetailStatBox(DetailStat("Start Date", c.startDate), Modifier.weight(1f))
            DetailStatBox(DetailStat("Expiry Date", c.expiry), Modifier.weight(1f))
        }
    }

    DetailSectionCard(title = "Agreement Details") {
        DetailInfoRow("Contract ID", c.code)
        DetailInfoRow("Contract Type", c.type)
        DetailInfoRow("Owner", c.owner)
        DetailInfoRow("Country / Region", c.location, showDivider = false)
    }

    DetailSectionCard(title = "Contract Health") {
        DetailBadgeRow("Status", statusBadge(c.statusLabel))
        DetailBadgeRow("Risk Level", riskBadge(c.riskLabel))
        DetailInfoRow("Days Remaining", c.daysRemaining, showDivider = false)
    }

    DetailSectionCard(title = "Related Records") {
        Spacer(Modifier.height(tokens.extraPadding))
        Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
            DetailLinkRow(Icons.Default.Apartment, c.partyName, "Primary Party • ${c.partyType}")
            DetailLinkRow(Icons.Default.LocationOn, c.location, "Country / Region")
            DetailLinkRow(Icons.Default.Folder, "Contract Documents", "${c.documents.size} Files Linked")
        }
    }
}

@Composable
private fun PartiesTab(c: ContractDetailUi) {
    c.parties.forEach { party ->
        DetailSectionCard(
            title = party.heading,
            titleDotColor = if (party.isPrimary) Primary else headerGrey,
            trailing = {
                DetailPill(DetailBadge(party.tag, TextSecondary, badgeGrey, showDot = false))
            }
        ) {
            DetailStackedField("Party Name", party.name, Modifier.fillMaxWidth(), showDivider = true)
            Row(Modifier.fillMaxWidth()) {
                DetailStackedField("Party Type", party.type, Modifier.weight(1f))
                DetailStackedField("Country", party.country, Modifier.weight(1f))
            }
            HorizontalDivider(color = light_grey, thickness = 1.dp)
            DetailStackedField("Designated Role", party.role, Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun TermsTab(c: ContractDetailUi) {
    DetailSectionCard(
        title = "Contract Terms",
        subtitle = "Key governing conditions & clauses",
        trailing = {
            DetailPill(DetailBadge(c.statusLabel, statusBadge(c.statusLabel).textColor, statusBadge(c.statusLabel).bgColor, showDot = false))
        }
    ) {
        c.terms.forEachIndexed { i, (label, value) ->
            DetailStackedField(
                label = label,
                value = value,
                modifier = Modifier.fillMaxWidth(),
                showDivider = i != c.terms.lastIndex
            )
        }
    }
}

@Composable
private fun DocumentsTab(c: ContractDetailUi) {
    val tokens = LocalAppTokens.current
    val uriHandler = LocalUriHandler.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = "Contract Documents",
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "${c.documents.size} files",
                fontSize = tokens.caption,
                color = mutedText
            )
        }

        OutlinedButton(
            onClick = {},
            modifier = Modifier.height(tokens.buttonHeight),
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            border = BorderStroke(1.dp, light_blue_border)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(tokens.iconSize)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = "Upload",
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.Medium,
                color = Primary
            )
        }
    }

    if (c.documents.isEmpty()) {
        Text(
            text = "No documents uploaded",
            fontSize = tokens.bodySmall,
            color = mutedText
        )
    }

    c.documents.forEach { doc ->
        val openDocument = { if (doc.url.isNotBlank()) uriHandler.openUri(doc.url) }

        DetailDocumentCard(
            icon = Icons.Default.Description,
            name = doc.name,
            uploadedText = doc.category,
            typeLabel = doc.extension.uppercase().ifBlank { "FILE" },
            tags = listOf("Version: v${doc.version}"),
            onReview = openDocument,
            onView = openDocument
        )
    }
}

// ─────────────────────────────────────────────────────────────
// Dynamic Renewal Tab matching the React Component State Machine
// ─────────────────────────────────────────────────────────────

@Composable
private fun RenewalTab(
    c: ContractDetailUi,
    contractDto: LegalContractDto?,
    onStartRenewalClick: () -> Unit,
    onStatusActionClick: (RenewalActionConfig) -> Unit
) {
    val tokens = LocalAppTokens.current
    val shape = RoundedCornerShape(tokens.cardCornerRadius)

    val currentStatus = contractDto?.renewalStatus ?: "Renewal_Notice"
    val hasProposal = !contractDto?.proposedStartDate.isNullOrBlank()
    val nextStep = c.nextStepInfo

    DetailSectionCard(
        title = "Renewal Management",
        trailing = {
            Text(text = c.renewalCycle, fontSize = tokens.caption, color = mutedText)
        }
    ) {
        Row(Modifier.fillMaxWidth()) {
            DetailStackedField("Current Expiry", c.expiry, Modifier.weight(1f))
            DetailStackedField("Renewal Status", formatRenewalLabel(currentStatus), Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth()) {
            DetailStackedField("Notice Period", c.noticePeriod, Modifier.weight(1f))
            DetailStackedField("Renewal Owner", c.renewalOwner, Modifier.weight(1f))
        }

        // Display Proposed Term and Value if proposal has been submitted
        if (hasProposal) {
            HorizontalDivider(color = light_grey, thickness = 1.dp)
            Row(Modifier.fillMaxWidth()) {
                val proposedRange = "${contractDto.proposedStartDate.take(10)} to ${contractDto.proposedEndDate?.take(10) ?: "—"}"
                DetailStackedField("Proposed Term", proposedRange, Modifier.weight(1f))
                DetailStackedField("Renewal Value", contractDto.renewalValue.toString(), Modifier.weight(1f))
            }
            if (!contractDto.approvalComments.isNullOrBlank()) {
                DetailStackedField("Approval Comments", contractDto.approvalComments, Modifier.fillMaxWidth())
            }
        }

        HorizontalDivider(color = light_grey, thickness = 1.dp)

        Spacer(Modifier.height(tokens.extraPadding))
        Text(text = "NEXT ACTION", fontSize = tokens.label, color = mutedText)
        Spacer(Modifier.height(6.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(Primary_background, shape)
                .border(1.dp, title_border, shape)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(tokens.iconSize)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = nextStep.text,
                fontSize = tokens.bodySmall,
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            Text(text = c.nextActionTag, fontSize = tokens.caption, color = mutedText)
        }

        Spacer(Modifier.height(tokens.extraPadding))

        // State Machine Driven Action Buttons
        if (nextStep.isStartRenewal || nextStep.actions.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
            ) {
                if (nextStep.isStartRenewal) {
                    Button(
                        onClick = onStartRenewalClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(tokens.buttonHeight),
                        shape = shape,
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Text(
                            text = "Start Renewal",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = whiteBg
                        )
                    }
                } else {
                    nextStep.actions.forEach { action ->
                        if (action.isSecondary) {
                            OutlinedButton(
                                onClick = { onStatusActionClick(action) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(tokens.buttonHeight),
                                shape = shape,
                                border = BorderStroke(1.dp, if (action.status == "Rejected") redText else BorderGray)
                            ) {
                                Text(
                                    text = action.label,
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = if (action.status == "Rejected") redText else TextPrimary
                                )
                            }
                        } else {
                            val buttonColor = when (action.status) {
                                "Approved" -> Color(0xFF15803D)
                                "In_Review" -> Color(0xFFD97706)
                                else -> Primary
                            }
                            Button(
                                onClick = { onStatusActionClick(action) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(tokens.buttonHeight),
                                shape = shape,
                                colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
                            ) {
                                Text(
                                    text = action.label,
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = whiteBg
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    DetailSectionCard(title = "Renewal Timeline") {
        Spacer(Modifier.height(tokens.extraPadding))
        DetailStepTimeline(c.steps)
    }
}

@Composable
private fun HistoryTab(c: ContractDetailUi) {
    val tokens = LocalAppTokens.current

    DetailSectionCard(title = "Contract Activity") {
        Spacer(Modifier.height(tokens.screenPadding))
        if (c.activities.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No activity recorded",
                    fontSize = tokens.bodySmall,
                    color = mutedText
                )
            }
        } else {
            DetailActivityTimeline(items = c.activities)
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Propose Renewal Dialog
// ─────────────────────────────────────────────────────────────

@Composable
fun RenewContractDialog(
    currentEndDate: String,
    initialOwnerId: String? = null,
    ownerOptions: List<Pair<String, String>>,
    onDismiss: () -> Unit,
    onSubmit: (String, String, String, String, String) -> Unit
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current

    var proposedStartDate by remember { mutableStateOf("") }
    var proposedEndDate by remember { mutableStateOf("") }
    var renewalValue by remember { mutableStateOf("") }
    var selectedOwnerId by remember { mutableStateOf(initialOwnerId ?: ownerOptions.firstOrNull()?.first.orEmpty()) }
    var isOwnerDropdownExpanded by remember { mutableStateOf(false) }
    var renewalTerms by remember { mutableStateOf("") }

    val selectedOwnerName = ownerOptions.find { it.first == selectedOwnerId }?.second ?: "Select Owner..."

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = Primary_background,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(tokens.screenPadding)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Renew Contract",
                        fontSize = tokens.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = mutedText,
                            modifier = Modifier.size(tokens.iconSize)
                        )
                    }
                }

                val infoText = buildAnnotatedString {
                    append("The current term ends on ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = TextPrimary)) {
                        append(currentEndDate.ifBlank { "N/A" })
                    }
                    append(". The renewal has to start after this date.")
                }

                Text(
                    text = infoText,
                    fontSize = tokens.bodySmall,
                    color = TextSecondary,
                    lineHeight = tokens.bodySmall * 1.4f
                )

                Spacer(Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        FormLabel(text = "Proposed Start Date", isRequired = true)
                        DatePickerField(
                            value = proposedStartDate,
                            onDateSelected = { proposedStartDate = it }
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        FormLabel(text = "Proposed End Date", isRequired = true)
                        DatePickerField(
                            value = proposedEndDate,
                            onDateSelected = { proposedEndDate = it }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        FormLabel(text = "Renewal Value")
                        FormTextField(
                            value = renewalValue,
                            onValueChange = { renewalValue = it },
                            placeholder = "0.00",
                            keyboardType = KeyboardType.Number
                        )
                    }

                    Column(modifier = Modifier.weight(1.2f)) {
                        FormDropdown(
                            label = "Renewal Owner",
                            value = selectedOwnerName,
                            expanded = isOwnerDropdownExpanded,
                            onExpandChange = { isOwnerDropdownExpanded = it },
                            options = ownerOptions.map { it.second },
                            onOptionSelected = { selectedName ->
                                selectedOwnerId = ownerOptions.find { it.second == selectedName }?.first.orEmpty()
                            }
                        )
                    }
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    FormLabel(text = "Renewal Terms")
                    FormTextArea(
                        value = renewalTerms,
                        onValueChange = { renewalTerms = it },
                        placeholder = "Changes to pricing, scope or conditions for the new term",
                        minLines = 3,
                        maxLines = 5
                    )
                }

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                        border = BorderStroke(1.dp, BorderGray),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                        modifier = Modifier.height(tokens.buttonHeight)
                    ) {
                        Text(
                            text = "Cancel",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(Modifier.width(10.dp))

                    Button(
                        onClick = {
                            val startDate = proposedStartDate.trim()
                            val endDate = proposedEndDate.trim()

                            if (startDate.isBlank() || endDate.isBlank()) {
                                Toast.makeText(context, "Proposed start and end dates are required", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            onSubmit(
                                startDate,
                                endDate,
                                renewalValue.trim(),
                                selectedOwnerId,
                                renewalTerms.trim()
                            )
                        },
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        modifier = Modifier.height(tokens.buttonHeight)
                    ) {
                        Text(
                            text = "Start Renewal",
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

// ─────────────────────────────────────────────────────────────
// Confirmation Modal matching the React ConfirmModal
// ─────────────────────────────────────────────────────────────

@Composable
fun ContractStatusActionDialog(
    action: RenewalActionConfig,
    currentStatus: String,
    onDismiss: () -> Unit,
    onConfirm: (approvalComments: String) -> Unit
) {
    val tokens = LocalAppTokens.current
    var comments by remember { mutableStateOf("") }

    val isReject = action.status == "Rejected"
    val accentColor = if (isReject) redText else Color(0xFFD97706)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .background(accentColor.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PriorityHigh,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Text(
                            text = action.label,
                            fontSize = tokens.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = mutedText,
                            modifier = Modifier.size(tokens.iconSize)
                        )
                    }
                }

                // Description
                val fromLabel = formatRenewalLabel(currentStatus)
                val toLabel = formatRenewalLabel(action.status)
                val message = buildAnnotatedString {
                    append("Move this renewal from ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold, color = TextPrimary)) {
                        append(fromLabel)
                    }
                    append(" to ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold, color = TextPrimary)) {
                        append(toLabel)
                    }
                    append("?")
                }

                Text(
                    text = message,
                    fontSize = tokens.bodyMedium,
                    color = TextSecondary
                )

                // Optional comments textarea for approval/rejection actions
                if (action.isApproval) {
                    FormTextArea(
                        value = comments,
                        onValueChange = { comments = it },
                        placeholder = "Comments (optional)",
                        minLines = 3,
                        maxLines = 4
                    )
                }

                Spacer(Modifier.height(4.dp))

                // Bottom Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                        border = BorderStroke(1.dp, BorderGray),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                        modifier = Modifier.height(tokens.buttonHeight)
                    ) {
                        Text(
                            text = "Cancel",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(Modifier.width(10.dp))

                    Button(
                        onClick = { onConfirm(comments) },
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isReject) redText else accentColor
                        ),
                        modifier = Modifier.height(tokens.buttonHeight)
                    ) {
                        Text(
                            text = action.label,
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