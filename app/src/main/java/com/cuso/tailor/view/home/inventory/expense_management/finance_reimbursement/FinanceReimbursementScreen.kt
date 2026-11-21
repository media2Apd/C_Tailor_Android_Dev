@file:Suppress("unused", "UNUSED_PARAMETER", "DEPRECATION", "AssignedValueIsNeverRead")

package com.cuso.tailor.view.home.inventory.expense_management.finance_reimbursement

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.hr.ExpenseClaimDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.view.composable.CirculerProgressIndicatorSmall
import com.cuso.tailor.view.composable.DataCard
import com.cuso.tailor.view.composable.DatePickerField
import com.cuso.tailor.view.composable.FormLabel
import com.cuso.tailor.view.composable.FormTextArea
import com.cuso.tailor.view.composable.FormTextField
import com.cuso.tailor.view.composable.PaginatedDataList
import com.cuso.tailor.view.composable.SearchFilterBar
import com.cuso.tailor.view.composable.StatusBadge
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.viewmodel.HrViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FinanceReimbursementScreen(
    viewModel: HrViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
    onViewDetail: (ExpenseClaimDto) -> Unit = {},
    onProcessPayClick: (ExpenseClaimDto) -> Unit = {}
) {
    val tokens = LocalAppTokens.current

    val claimsPagingState by viewModel.expenseClaimsPagingState.collectAsStateWithLifecycle()
    val claimsError by viewModel.expenseClaimsError.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.fetchReimbursementClaims()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val filteredClaims = remember(claimsPagingState.items, searchQuery) {
        if (searchQuery.isBlank()) {
            claimsPagingState.items
        } else {
            claimsPagingState.items.filter { claim ->
                claim.title?.contains(searchQuery, ignoreCase = true) == true ||
                        claim.claimNumber?.contains(searchQuery, ignoreCase = true) == true ||
                        (claim.employee?.fullName?.contains(searchQuery, ignoreCase = true) == true)
            }
        }
    }

    val displayPagingState = remember(claimsPagingState, filteredClaims) {
        claimsPagingState.copy(items = filteredClaims)
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TitleBar(
                title = "Finance Reimbursement",
                onClose = onClose
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SearchFilterBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Search types...",
                showFilterIcon = true,
                filterCount = 0,
                onFilterClick = { },
                accentColor = Primary,
                borderColor = sectionBorder,
                textSecondaryColor = iconMuted,
                height = tokens.fieldHeight
            )

            if (!displayPagingState.isInitialLoading && displayPagingState.items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(tokens.screenPadding),
                    contentAlignment = Alignment.Center
                ) {
                    // Display API error message if request failed, otherwise show empty state
                    Text(
                        text = claimsError?.takeIf { it.isNotBlank() } ?: "No claims to reimburse",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (!claimsError.isNullOrBlank()) redText else mutedText,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                PaginatedDataList(
                    state = displayPagingState,
                    onLoadMore = { viewModel.loadMoreExpenseClaims() },
                    modifier = Modifier.fillMaxSize()
                ) { claim ->
                    ReimbursementItemCard(
                        claim = claim,
                        onViewDetail = { onViewDetail(claim) },
                        onProcessPay = { onProcessPayClick(claim) }
                    )
                }
            }
        }
    }
}

@Composable
fun ReimbursementItemCard(
    claim: ExpenseClaimDto,
    onViewDetail: () -> Unit,
    onProcessPay: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }

    val managerStep = claim.workflow.find { it.stage.contains("Manager", ignoreCase = true) }

    // Read manager status from workflow and finance status directly from data.data.status
    val rawManagerStatus = managerStep?.status.orEmpty()
    val managerStatusText = rawManagerStatus.replace("_", " ").ifBlank { "—" }

    val rawFinanceStatus = claim.status.orEmpty()
    val financeStatusText = rawFinanceStatus.replace("_", " ").ifBlank { "—" }

    val isAlreadySettled = rawFinanceStatus.equals("Paid", ignoreCase = true) ||
            rawFinanceStatus.equals("Rejected", ignoreCase = true)
    val isPayEligible = !isAlreadySettled && (
            rawFinanceStatus.equals("Approved", ignoreCase = true) ||
                    rawFinanceStatus.equals("Payment_Pending", ignoreCase = true)
            )

    // Resolve badge colors dynamically from API status values
    val isManagerApproved = rawManagerStatus.equals("Approved", ignoreCase = true) ||
            rawManagerStatus.equals("Completed", ignoreCase = true)
    val isManagerRejected = rawManagerStatus.equals("Rejected", ignoreCase = true)

    val isFinanceCompleted = rawFinanceStatus.equals("Paid", ignoreCase = true) ||
            rawFinanceStatus.equals("Completed", ignoreCase = true) ||
            rawFinanceStatus.equals("Approved", ignoreCase = true)
    val isFinanceRejected = rawFinanceStatus.equals("Rejected", ignoreCase = true)

    val claimNumberText = claim.claimNumber.orEmpty().ifBlank { "—" }
    val employeeDisplayName = claim.employee?.fullName.orEmpty().ifBlank { "—" }
    val categoryDisplayText = claim.category?.name?.takeIf { it.isNotBlank() }
        ?.let { "Category: $it" }
        ?: "Category: —"

    DataCard(
        item = claim,
        onClick = { onViewDetail() },
        showDivider = false,
        headerContent = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = claimNumberText,
                    fontSize = tokens.caption,
                    color = headerGrey
                )
                Text(
                    text = currencyFormatter.format(claim.totalAmount),
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Primary
                )
            }
        },
        title = employeeDisplayName,
        titleFontWeight = FontWeight.Medium,
        titleColor = TextPrimary,
        subtitle = categoryDisplayText,
        content = {
            Column(modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Manager Status", fontSize = 10.sp, color = headerGrey)
                        Spacer(Modifier.height(2.dp))
                        StatusBadge(
                            text = managerStatusText,
                            bgColor = when {
                                isManagerApproved -> greenBg
                                isManagerRejected -> redBg
                                else -> activity_purple_bg
                            },
                            textColor = when {
                                isManagerApproved -> greentext
                                isManagerRejected -> redText
                                else -> blueText
                            },
                            showDot = false,
                            cornerRadius = 4.dp
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("Finance Status", fontSize = 10.sp, color = headerGrey)
                        Spacer(Modifier.height(2.dp))
                        StatusBadge(
                            text = financeStatusText,
                            bgColor = when {
                                isFinanceCompleted -> greenBg
                                isFinanceRejected -> redBg
                                else -> activity_purple_bg
                            },
                            textColor = when {
                                isFinanceCompleted -> greentext
                                isFinanceRejected -> redText
                                else -> blueText
                            },
                            showDot = false,
                            cornerRadius = 4.dp
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "View Details",
                        fontSize = tokens.caption,
                        color = headerGrey,
                        modifier = Modifier.clickable { onViewDetail() }
                    )

                    if (isPayEligible) {
                        Spacer(Modifier.width(14.dp))
                        Button(
                            onClick = onProcessPay,
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Process Pay", fontSize = tokens.label, fontWeight = FontWeight.Medium, color = whiteBg)
                        }
                    }
                }
            }
        }
    )
}

// Standalone screen for processing expense claim payment
@Composable
fun ProcessPaymentScreen(
    claim: ExpenseClaimDto,
    onClose: () -> Unit = {},
    onPaymentSuccess: () -> Unit = {},
    viewModel: HrViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val scrollState = rememberScrollState()

    var selectedMode by remember { mutableStateOf("Bank") }
    var refNo by remember { mutableStateOf("") }
    var paymentDate by remember { mutableStateOf(SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())) }
    var notes by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }

    Scaffold(
        containerColor = whiteBg,
        topBar = {
            Column(modifier = Modifier.fillMaxWidth().background(whiteBg)) {
                TitleBar(
                    title = "Process Payment",
                    onClose = onClose
                )
                HorizontalDivider(color = title_border, thickness = 1.dp)
            }
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = whiteBg,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = tokens.screenPadding, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onClose,
                        modifier = Modifier
                            .weight(1f)
                            .height(tokens.buttonHeight),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                        border = BorderStroke(1.dp, sectionBorder)
                    ) {
                        Text("Cancel", fontSize = tokens.bodySmall, color = TextSecondary)
                    }

                    Button(
                        onClick = {
                            if (!isSubmitting) {
                                isSubmitting = true

                                // Map selected UI payment mode to API enum value
                                val apiMode = when (selectedMode) {
                                    "Bank" -> "Bank_Transfer"
                                    else -> selectedMode
                                }

                                // Convert selected UI date to API format (yyyy-MM-dd)
                                val formattedApiDate = formatPaymentDateToApi(paymentDate)

                                val paymentPayload = com.cuso.tailor.model.hr.ExpenseClaimPaymentDetails(
                                    mode = apiMode,
                                    referenceNo = refNo.trim(),
                                    paymentDate = formattedApiDate,
                                    notes = notes.trim()
                                )

                                viewModel.updateExpenseClaimStatus(
                                    claimId = claim.id,
                                    status = "Paid",
                                    payment = paymentPayload,
                                    onSuccess = { successMsg ->
                                        isSubmitting = false
                                        DynamicIslandManager.showSuccess(successMsg)
                                        viewModel.fetchReimbursementClaims()
                                        onPaymentSuccess()
                                    },
                                    onError = { errorMsg ->
                                        isSubmitting = false
                                        DynamicIslandManager.showError(errorMsg)
                                    }
                                )
                            }
                        },
                        enabled = !isSubmitting,
                        modifier = Modifier
                            .weight(1f)
                            .height(tokens.buttonHeight),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary, disabledContainerColor = disabled)
                    ) {
                        if (isSubmitting) {
                            CirculerProgressIndicatorSmall()
                        } else {
                            Text(
                                "Mark as Paid",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = whiteBg
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(tokens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Amount & Payee Summary Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = PanelBg),
                    border = BorderStroke(1.dp, sectionBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Total Amount", fontSize = tokens.label, color = headerGrey)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = currencyFormatter.format(claim.totalAmount),
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = Primary
                        )
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = PanelBg),
                    border = BorderStroke(1.dp, sectionBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Payee", fontSize = tokens.label, color = headerGrey)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = claim.employee?.fullName.orEmpty().ifBlank { "—" },
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                }
            }

            // Payment Mode Selector Tabs
            Column {
                FormLabel("Payment Mode")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf("Bank", "Cash", "UPI").forEach { mode ->
                        val isSelected = selectedMode.equals(mode, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) Primary else sectionBorder,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .background(whiteBg)
                                .clickable { selectedMode = mode },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = mode,
                                fontSize = tokens.bodySmall,
                                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                                color = if (isSelected) Primary else TextSecondary
                            )
                        }
                    }
                }
            }

            // Payment Reference No
            Column {
                FormLabel("Payment Reference No.")
                FormTextField(
                    value = refNo,
                    onValueChange = { refNo = it },
                    placeholder = "Enter TXN reference..."
                )
            }

            // Payment Date
            Column {
                FormLabel("Payment Date")
                DatePickerField(
                    value = paymentDate,
                    onDateSelected = { selectedDate ->
                        paymentDate = selectedDate
                    }
                )
            }

            // Notes
            Column {
                FormLabel("Notes")
                FormTextArea(
                    value = notes,
                    onValueChange = { notes = it },
                    placeholder = "Reconciliation notes for accounts audit team...",
                    minLines = 3,
                    maxLines = 4
                )
            }
        }
    }
}
private fun formatPaymentDateToApi(rawDate: String): String {
    val inputFormats = listOf("dd-MM-yyyy", "yyyy-MM-dd", "dd/MM/yyyy", "MMM dd, yyyy")
    val apiFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    for (pattern in inputFormats) {
        try {
            val parser = SimpleDateFormat(pattern, Locale.getDefault()).apply { isLenient = false }
            val parsed = parser.parse(rawDate.trim())
            if (parsed != null) return apiFormat.format(parsed)
        } catch (_: Exception) {
        }
    }
    return apiFormat.format(Date())
}