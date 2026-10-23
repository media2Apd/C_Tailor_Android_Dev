@file:Suppress("unused", "UNUSED_PARAMETER", "DEPRECATION", "AssignedValueIsNeverRead")

package com.cuso.tailor.view.home.inventory.expense_management.finance_reimbursement

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
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.cuso.tailor.view.composable.CirculerProgressIndicatorSmall
import com.cuso.tailor.view.composable.DataCard
import com.cuso.tailor.view.composable.FormLabel
import com.cuso.tailor.view.composable.FormTextArea
import com.cuso.tailor.view.composable.FormTextField
import com.cuso.tailor.view.composable.ListSkeleton
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

    val expenseClaims by viewModel.expenseClaims.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingExpenseClaims.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.fetchReimbursementClaims()
    }

    val filteredClaims = remember(expenseClaims, searchQuery) {
        if (searchQuery.isBlank()) {
            expenseClaims
        } else {
            expenseClaims.filter { claim ->
                claim.title?.contains(searchQuery, ignoreCase = true) == true ||
                        claim.claimNumber?.contains(searchQuery, ignoreCase = true) == true ||
                        (claim.employee?.fullName?.contains(searchQuery, ignoreCase = true) == true)
            }
        }
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
            Modifier
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

            if (isLoading) {
                ListSkeleton()
            } else if (filteredClaims.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No claims to reimburse",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = mutedText,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(filteredClaims, key = { it.id }) { claim ->
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
    val financeStep = claim.workflow.find { it.stage.contains("Finance", ignoreCase = true) || it.stage.contains("Payment", ignoreCase = true) }

    val managerStatus = managerStep?.status ?: ""
    val financeStatus = financeStep?.status ?: claim.status ?: ""

    val isPayEligible = claim.status.equals("Approved", ignoreCase = true) || financeStatus.contains("Pending", ignoreCase = true)

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
                    text = claim.claimNumber.orEmpty().ifBlank { "CLM-${claim.id.takeLast(3).uppercase()}" },
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
        title = claim.employee?.fullName ?: "Employee Name",
        titleFontWeight = FontWeight.Medium,
        titleColor = TextPrimary,
        subtitle = "Category: ${claim.category?.name ?: "Software"}",
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
                            text = managerStatus,
                            bgColor = greenBg,
                            textColor = greentext,
                            showDot = false,
                            cornerRadius = 4.dp
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("Finance Status", fontSize = 10.sp, color = headerGrey)
                        Spacer(Modifier.height(2.dp))
                        val isPaid = financeStatus.equals("Paid", ignoreCase = true) || financeStatus.equals("Completed", ignoreCase = true)
                        val isRejected = financeStatus.equals("Rejected", ignoreCase = true)
                        StatusBadge(
                            text = if (isPaid) "Approved" else if (isRejected) "Rejected" else "Pending",
                            bgColor = if (isPaid) greenBg else if (isRejected) redBg else activity_purple_bg,
                            textColor = if (isPaid) greentext else if (isRejected) redText else blueText,
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

// 🟢 NEW STANDALONE SCREEN (Replaces Dialog)
@OptIn(ExperimentalMaterial3Api::class)
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
    var refNo by remember { mutableStateOf("TXN-${System.currentTimeMillis().toString().takeLast(8)}") }
    var paymentDate by remember { mutableStateOf(SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())) }
    var notes by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }

    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        paymentDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(it))
                    }
                    showDatePicker = false
                }) {
                    Text("OK", color = Primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

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

                                // Mode format map (e.g., Bank -> Bank_Transfer)
                                val apiMode = when (selectedMode) {
                                    "Bank" -> "Bank_Transfer"
                                    else -> selectedMode
                                }

                                // UI Date (MMM dd, yyyy) to API Date (yyyy-MM-dd)
                                val formattedApiDate = try {
                                    val parsed = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).parse(paymentDate)
                                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(parsed ?: Date())
                                } catch (_: Exception) {
                                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                                }

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
                                    onSuccess = {
                                        isSubmitting = false
                                        onPaymentSuccess()
                                    },
                                    onError = {
                                        isSubmitting = false
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
                            text = claim.employee?.fullName ?: "Employee",
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
                Box(modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true }) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(tokens.fieldHeight)
                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                            .border(1.dp, grey_border, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                            .background(whiteBg)
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(paymentDate, fontSize = tokens.bodySmall, color = TextPrimary)
                        Icon(Icons.Outlined.CalendarToday, null, tint = headerGrey, modifier = Modifier.size(16.dp))
                    }
                }
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