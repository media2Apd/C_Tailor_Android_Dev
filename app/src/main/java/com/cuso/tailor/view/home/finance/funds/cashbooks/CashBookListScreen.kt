@file:Suppress("unused","AssignedValueIsNeverRead")

package com.cuso.tailor.view.home.finance.funds.cashbooks

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.PointOfSale
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.finance.OpenCashShiftRequest
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.BranchUiState
import com.cuso.tailor.viewmodel.BranchViewModel
import com.cuso.tailor.viewmodel.FinanceViewModel
import com.cuso.tailor.viewmodel.OpenCashShiftState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun CashBookListScreen(
    viewModel: FinanceViewModel,
    branchViewModel: BranchViewModel,
    onBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit
) {
    val tokens = LocalAppTokens.current
    val shifts by viewModel.cashShifts.collectAsState()
    val isLoading by viewModel.isLoadingCashShifts.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showOpenShiftDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.fetchCashShifts()
    }

    if (showOpenShiftDialog) {
        OpenCashDrawerShiftDialog(
            viewModel = viewModel,
            branchViewModel = branchViewModel,
            onDismiss = { showOpenShiftDialog = false },
            onShiftOpenedSuccessfully = {
                showOpenShiftDialog = false
                viewModel.fetchCashShifts()
            }
        )
    }

    FabScaffold(
        modifier = Modifier.fillMaxSize(),
        fab = FabConfig(
            label = "Open New Shift",
            icon = Icons.Default.Add,
            onClick = { showOpenShiftDialog = true }
        )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                Row(modifier = Modifier.fillMaxWidth()) {
                    TitleBar(title = "Cash Books", onClose = onBack)
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Search and Filter Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(tokens.fieldHeight)
                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.6f))
                            .background(whiteBg)
                            .border(1.dp, grey_border, RoundedCornerShape(tokens.cardCornerRadius * 0.6f))
                            .padding(horizontal = tokens.cardPadding * 0.6f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = null,
                                tint = mutedText,
                                modifier = Modifier.size(tokens.iconSize)
                            )
                            Spacer(Modifier.width(tokens.extraPadding * 0.8f))
                            FormTextField(
                                value = searchQuery,
                                onValueChange = {
                                    searchQuery = it
                                    viewModel.fetchCashShifts(it)
                                },
                                placeholder = "Search shift number, cashier...",
                                containerColor = Color.Transparent,
                                borderColor = Color.Transparent,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(tokens.fieldHeight)
                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.6f))
                            .background(whiteBg)
                            .border(1.dp, grey_border, RoundedCornerShape(tokens.cardCornerRadius * 0.6f))
                            .clickable { },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.FilterList,
                            contentDescription = "Filter",
                            tint = TitleColor,
                            modifier = Modifier.size(tokens.iconSize)
                        )
                    }
                }

                if (isLoading && shifts.isEmpty()) {
                    ListSkeleton()
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = tokens.buttonHeight * 1.5f)
                    ) {
                        items(shifts, key = { it.id }) { item ->
                            val cashierName = item.cashierId?.fullName ?: "Cashier"
                            val formattedDate = formatIsoDate(item.openingTime)
                            val isClosed = item.status.equals("Closed", ignoreCase = true)

                            val badgeBg = if (isClosed) light_grey else greenBg
                            val badgeText = if (isClosed) headerGrey else greentext

                            DataCard(
                                item = item,
                                title = item.shiftNumber,
                                titleFontWeight = FontWeight.Medium,
                                topBadgeText = if (isClosed) "Closed" else "Open",
                                topBadgeBgColor = badgeBg,
                                topBadgeTextColor = badgeText,
                                topBadgeInline = true,
                                actions = listOf(
                                    MenuAction("View Details", onClick = { onNavigateToDetail(item.id) })
                                ),
                                content = {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        // Row 1: Inflow & Outflow
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Debit (Inflow)", fontSize = tokens.bodySmall, color = mutedText)
                                            Text(
                                                text = "₹${String.format(Locale.US, "%,.2f", item.totalCashCollected)}",
                                                fontSize = tokens.bodySmall,
                                                color = greentext,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }

                                        Spacer(Modifier.height(tokens.extraPadding * 0.4f))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Credit (Outflow)", fontSize = tokens.bodySmall, color = mutedText)
                                            Text(
                                                text = "₹${String.format(Locale.US, "%,.2f", item.totalCashWithdrawn)}",
                                                fontSize = tokens.bodySmall,
                                                color = redText,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }

                                        Spacer(Modifier.height(tokens.extraPadding * 0.4f))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Balance", fontSize = tokens.bodySmall, color = mutedText)
                                            Text(
                                                text = "₹${String.format(Locale.US, "%,.2f", item.actualClosingBalance)}",
                                                fontSize = tokens.bodySmall,
                                                color = TitleColor,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }

                                        HorizontalDivider(
                                            modifier = Modifier.padding(vertical = tokens.extraPadding * 0.8f),
                                            color = dividerColor
                                        )

                                        // Row 3: Meta Footers (Date, Voucher Type, Cashier)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text("Date", fontSize = tokens.label, color = mutedText)
                                                Spacer(Modifier.height(tokens.extraPadding * 0.2f))
                                                Text(formattedDate, fontSize = tokens.caption, color = TitleColor)
                                            }

                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("Voucher Type", fontSize = tokens.label, color = mutedText)
                                                Spacer(Modifier.height(tokens.extraPadding * 0.2f))
                                                Text("Shift Drawer", fontSize = tokens.caption, color = TitleColor)
                                            }

                                            Column(horizontalAlignment = Alignment.End) {
                                                Text("Cashier", fontSize = tokens.label, color = mutedText)
                                                Spacer(Modifier.height(tokens.extraPadding * 0.2f))
                                                Text(cashierName, fontSize = tokens.caption, color = TitleColor)
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatIsoDate(iso: String?): String {
    if (iso.isNullOrBlank()) return "—"
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        val formatter = SimpleDateFormat("dd MMM, yyyy", Locale.US)
        parser.parse(iso)?.let { formatter.format(it) } ?: iso.take(10)
    } catch (_: Exception) {
        iso.take(10)
    }
}

@Composable
fun OpenCashDrawerShiftDialog(
    viewModel: FinanceViewModel,
    branchViewModel: BranchViewModel,
    onDismiss: () -> Unit,
    onShiftOpenedSuccessfully: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current

    // Fetch branches and payment_source chart of accounts on launch
    LaunchedEffect(Unit) {
        branchViewModel.loadBranches()
        viewModel.fetchChartOfAccountsDropdown("payment_source")
    }

    // Branch selection state
    val branchUiState by branchViewModel.uiState.collectAsState()
    val branchesList = remember(branchUiState) {
        (branchUiState as? BranchUiState.Success)?.branches.orEmpty()
    }
    val branchOptions = remember(branchesList) {
        val names = branchesList.mapNotNull { it.name }
        names.ifEmpty { listOf("Select Branch") }
    }
    var selectedBranchName by remember { mutableStateOf("Select Branch") }
    var selectedBranchId by remember { mutableStateOf<String?>(null) }
    var isBranchDropdownOpen by remember { mutableStateOf(false) }

    // Cash Ledger Account selection state (context = "payment_source")
    val accountsList by viewModel.accountDropdownList.collectAsState()
    val accountOptions = remember(accountsList) {
        val names = accountsList.mapNotNull { it.accountName.ifBlank { null } }
        names.ifEmpty { listOf("Select Cash Account") }
    }
    var selectedAccountName by remember { mutableStateOf("Select Cash Account") }
    var selectedAccountId by remember { mutableStateOf<String?>(null) }
    var isAccountDropdownOpen by remember { mutableStateOf(false) }

    var openingFloat by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val openShiftState by viewModel.openCashShiftState.collectAsState()
    val isSubmitting = openShiftState is OpenCashShiftState.Loading

    // Handle errors via Toast
    LaunchedEffect(openShiftState) {
        if (openShiftState is OpenCashShiftState.Error) {
            Toast.makeText(context, (openShiftState as OpenCashShiftState.Error).message, Toast.LENGTH_LONG).show()
            viewModel.resetOpenCashShiftState()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            colors = CardDefaults.cardColors(containerColor = whiteBg),
            border = BorderStroke(1.dp, BorderGray)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(tokens.cardPadding)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PointOfSale,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(tokens.iconSize * 1.2f)
                        )
                        Text(
                            text = "Open Cash Drawer Shift",
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = TitleColor
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Close",
                            tint = close_color,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                HorizontalDivider(color = dividerColor)

                // 1. Branch Dropdown Field
                FormDropdown(
                    label = "Branch Jurisdiction",
                    isRequired = true,
                    value = selectedBranchName,
                    expanded = isBranchDropdownOpen,
                    onExpandChange = { isBranchDropdownOpen = it },
                    options = branchOptions,
                    onOptionSelected = { chosenBranch ->
                        val chosenObj = branchesList.find { it.name == chosenBranch }
                        selectedBranchName = chosenBranch
                        selectedBranchId = chosenObj?.id
                    }
                )

                // 2. Cash Ledger Account Dropdown Field (context = payment_source)
                FormDropdown(
                    label = "Cash Ledger Account",
                    isRequired = true,
                    value = selectedAccountName,
                    expanded = isAccountDropdownOpen,
                    onExpandChange = { isAccountDropdownOpen = it },
                    options = accountOptions,
                    onOptionSelected = { chosenAccName ->
                        val chosenObj = accountsList.find { it.accountName == chosenAccName }
                        selectedAccountName = chosenAccName
                        selectedAccountId = chosenObj?.id
                    }
                )

                // 3. Opening Balance (Float) Input
                Column {
                    FormLabel(text = "Opening Float / Balance (INR)", isRequired = true)
                    FormTextField(
                        value = openingFloat,
                        onValueChange = { openingFloat = it },
                        placeholder = "e.g., 12",
                        keyboardType = KeyboardType.Decimal,
                        containerColor = badgeGrey,
                        textColor = TitleColor
                    )
                }

                // 4. Shift Notes / Remarks
                Column {
                    FormLabel(text = "Opening Notes / Remarks", isRequired = false)
                    FormTextArea(
                        value = notes,
                        onValueChange = { notes = it },
                        placeholder = "e.g., Starting float verified by cashier...",
                        minLines = 3,
                        maxLines = 4,
                        borderColor = grey_border,
                        textColor = TitleColor
                    )
                }

                Spacer(Modifier.height(tokens.extraPadding * 0.5f))
                HorizontalDivider(color = dividerColor)

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.height(tokens.buttonHeight),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                        border = BorderStroke(1.dp, BorderGray),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = whiteBg)
                    ) {
                        Text(
                            text = "Cancel",
                            color = TitleColor,
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(Modifier.width(tokens.extraPadding))

                    Button(
                        onClick = {
                            val branchId = selectedBranchId
                            val accountId = selectedAccountId
                            val amount = openingFloat.toDoubleOrNull()

                            if (branchId.isNullOrBlank()) {
                                Toast.makeText(context, "Please select a branch", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (accountId.isNullOrBlank()) {
                                Toast.makeText(context, "Please select a cash ledger account", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (amount == null || amount < 0.0) {
                                Toast.makeText(context, "Please enter a valid opening float amount", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            // Formats current timestamp to ISO 8601 UTC
                            val currentIsoOpeningTime = SimpleDateFormat(
                                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                                Locale.US
                            ).apply {
                                timeZone = TimeZone.getTimeZone("UTC")
                            }.format(Date())

                            val request = OpenCashShiftRequest(
                                branchId = branchId,
                                cashAccountId = accountId,
                                openingBalance = amount,
                                openingTime = currentIsoOpeningTime,
                                notes = notes.trim()
                            )

                            viewModel.openCashDrawerShift(request) { successMsg ->
                                Toast.makeText(context, successMsg, Toast.LENGTH_SHORT).show()
                                viewModel.resetOpenCashShiftState()
                                onShiftOpenedSuccessfully()
                            }
                        },
                        enabled = !isSubmitting,
                        modifier = Modifier.height(tokens.buttonHeight),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(tokens.iconSize)
                            )
                        } else {
                            Text(
                                text = "Start Shift",
                                color = Color.White,
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}