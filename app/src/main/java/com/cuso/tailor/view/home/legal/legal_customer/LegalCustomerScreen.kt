@file:Suppress("unused")

package com.cuso.tailor.view.home.legal.legal_customer

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.legal.LegalCustomerItemDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.LegalViewModel
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs

private fun parseIsoDate(rawDate: String?): Date? {
    if (rawDate.isNullOrBlank()) return null
    val formats = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd"
    )
    for (format in formats) {
        try {
            val parser = SimpleDateFormat(format, Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val date = parser.parse(rawDate)
            if (date != null) return date
        } catch (_: Exception) {}
    }
    return null
}

private fun formatDisplayDate(rawDate: String?): String {
    val date = parseIsoDate(rawDate) ?: return "-"
    return SimpleDateFormat("dd MMM yyyy", Locale.US).format(date)
}

private fun calculateRemainingDays(rawDate: String?): String {
    val target = parseIsoDate(rawDate) ?: return ""
    val diffMillis = target.time - System.currentTimeMillis()
    val diffDays = TimeUnit.MILLISECONDS.toDays(diffMillis)

    return when {
        diffDays > 0 -> "(${diffDays}d remaining )"
        diffDays == 0L -> "(Expires today)"
        else -> "(${abs(diffDays)}d expired )"
    }
}

private fun getLegalCustomerFilterSections(): List<FilterSection> = listOf(
    FilterSection(
        title = "Status",
        options = listOf(
            FilterOption("Pending", "Pending"),
            FilterOption("Active", "Active"),
            FilterOption("Expired", "Expired")
        ),
        isMultiSelect = true
    ),
    FilterSection(
        title = "Consent Type",
        options = listOf(
            FilterOption("Signed", "Signed"),
            FilterOption("Pending", "Pending"),
            FilterOption("Declined", "Declined")
        ),
        isMultiSelect = true
    )
)

@Composable
fun LegalCustomerListScreen(
    viewModel: LegalViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onAddCustomerRecord: () -> Unit = {},
    onRecordClick: (LegalCustomerItemDto) -> Unit = {},
    onEditRecord: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val tokens = LocalAppTokens.current

    LaunchedEffect(Unit) {
        viewModel.fetchLegalCustomers()
    }

    val records by viewModel.legalCustomers.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingCustomers.collectAsStateWithLifecycle()
    val errorMessage by viewModel.customersError.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    val filterDrawerState = rememberFilterDrawerState()
    var filterSections by remember { mutableStateOf(getLegalCustomerFilterSections()) }

    var recordToDelete by remember { mutableStateOf<LegalCustomerItemDto?>(null) }

    val activeFilterCount = remember(filterSections) {
        filterSections.sumOf { it.options.count { opt -> opt.isSelected } }
    }

    val filteredRecords = remember(records, searchQuery, filterSections) {
        val selectedStatuses = filterSections.find { it.title == "Status" }?.options?.filter { it.isSelected }?.map { it.id }.orEmpty()
        val selectedConsents = filterSections.find { it.title == "Consent Type" }?.options?.filter { it.isSelected }?.map { it.id }.orEmpty()

        records.filter { item ->
            val customerName = item.customerId?.name.orEmpty()
            val matchesSearch = searchQuery.isBlank() ||
                    customerName.contains(searchQuery, ignoreCase = true) ||
                    item.legalCustomerNumber?.contains(searchQuery, ignoreCase = true) == true ||
                    item.customerCode?.contains(searchQuery, ignoreCase = true) == true

            val matchesStatus = selectedStatuses.isEmpty() || selectedStatuses.any { it.equals(item.status, ignoreCase = true) }
            val matchesConsent = selectedConsents.isEmpty() || selectedConsents.any { it.equals(item.consentType, ignoreCase = true) }

            matchesSearch && matchesStatus && matchesConsent
        }
    }

    val fabConfig = remember(onAddCustomerRecord) {
        FabConfig(
            label = "Add Customer Record",
            icon = Icons.Default.Add,
            onClick = onAddCustomerRecord,
            alignment = Alignment.BottomEnd,
            endPadding = 16.dp,
            bottomPadding = 80.dp,
            draggable = true
        )
    }

    FabScaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent),
        fab = fabConfig,
        fabVisible = true
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TitleBar(
                    title = "Customer Legal",
                    onClose = onBack
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (isLoading && records.isEmpty()) {
                    ListSkeleton()
                } else if (!errorMessage.isNullOrBlank() && records.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = errorMessage ?: "Failed to load records",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Normal,
                                color = redText
                            )
                            Spacer(Modifier.height(tokens.extraPadding))
                            Button(
                                onClick = { viewModel.fetchLegalCustomers() },
                                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f)
                            ) {
                                Text(
                                    text = "Retry",
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = whiteBg
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 100.dp)
                    ) {
                        item {
                            SearchFilterBar(
                                query = searchQuery,
                                onQueryChange = {
                                    searchQuery = it
                                    viewModel.fetchLegalCustomers(search = it)
                                },
                                placeholder = "Search SO number, customer...",
                                filterCount = activeFilterCount,
                                onFilterClick = { filterDrawerState.open() },
                                accentColor = Primary,
                                borderColor = sectionBorder,
                                textSecondaryColor = mutedText
                            )
                        }

                        items(filteredRecords, key = { it.id }) { recordItem ->
                            val customerDisplayName = recordItem.customerId?.name ?: recordItem.customerCode ?: "Customer Record"
                            val statusText = recordItem.status ?: "Pending"
                            val recordIdText = recordItem.legalCustomerNumber ?: recordItem.customerCode ?: "-"
                            val typeText = recordItem.legalMatterType ?: "-"
                            val countryBranchText = listOfNotNull(recordItem.countryName, recordItem.branchId?.name).filter { it.isNotBlank() }.joinToString(" / ")
                            val termsText = recordItem.termsType ?: "Standard"
                            val consentText = recordItem.consentType ?: "Signed"
                            val disputesCount = if (recordItem.disputeReference.isNullOrBlank()) "0" else "1"
                            val riskText = recordItem.riskLevel ?: "Low"
                            val expiryDateFormatted = formatDisplayDate(recordItem.expiryDate)
                            val remainingFormatted = calculateRemainingDays(recordItem.expiryDate)

                            DataCard(
                                item = recordItem,
                                title = customerDisplayName,
                                titleFontWeight = FontWeight.Medium,
                                titleColor = TextPrimary,
                                topBadgeText = statusText,
                                topBadgeTextColor = orangeText,
                                topBadgeBgColor = orangeBg,
                                topBadgeShowDot = false,
                                topBadgeInline = true,
                                actions = listOf(
                                    MenuAction(
                                        label = "Edit",
                                        onClick = { onEditRecord(recordItem.id) }
                                    ),
                                    MenuAction(
                                        label = "View Details",
                                        onClick = { onRecordClick(recordItem) }
                                    ),
                                    MenuAction(
                                        label = "Delete",
                                        textColor = redText,
                                        onClick = { recordToDelete = recordItem }
                                    )
                                ),
                                showActionsInHeader = false,
                                showDivider = false,
                                onClick = { onRecordClick(recordItem) },
                                content = {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Record ID",
                                                fontSize = tokens.bodySmall,
                                                fontWeight = FontWeight.Normal,
                                                color = mutedText
                                            )
                                            Text(
                                                text = recordIdText,
                                                fontSize = tokens.bodySmall,
                                                fontWeight = FontWeight.Medium,
                                                fontFamily = FontFamily.Monospace,
                                                color = TextPrimary
                                            )
                                        }

                                        Spacer(Modifier.height(10.dp))

                                        // 6-Grid Stats Box
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                                .background(Primary_background)
                                                .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                                .padding(vertical = 10.dp, horizontal = 12.dp)
                                        ) {
                                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                                        Text("Type", fontSize = tokens.caption, color = mutedText)
                                                        Spacer(Modifier.height(2.dp))
                                                        Text(typeText, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                    }
                                                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                                        Text("Country", fontSize = tokens.caption, color = mutedText)
                                                        Spacer(Modifier.height(2.dp))
                                                        Text(countryBranchText.ifBlank { "-" }, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                    }
                                                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                                        Text("Terms", fontSize = tokens.caption, color = mutedText)
                                                        Spacer(Modifier.height(2.dp))
                                                        Text(termsText, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                    }
                                                }

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                                        Text("Consent", fontSize = tokens.caption, color = mutedText)
                                                        Spacer(Modifier.height(2.dp))
                                                        Text(consentText, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = greentext, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                    }
                                                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                                        Text("Disputes", fontSize = tokens.caption, color = mutedText)
                                                        Spacer(Modifier.height(2.dp))
                                                        Text(disputesCount, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                    }
                                                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                                        Text("Risk", fontSize = tokens.caption, color = mutedText)
                                                        Spacer(Modifier.height(2.dp))
                                                        Text(riskText, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(Modifier.height(12.dp))

                                        Text(
                                            text = buildAnnotatedString {
                                                withStyle(SpanStyle(color = mutedText, fontWeight = FontWeight.Normal)) {
                                                    append("Expiry :")
                                                }
                                                withStyle(SpanStyle(color = TextPrimary, fontWeight = FontWeight.Medium)) {
                                                    append(expiryDateFormatted)
                                                }
                                                if (remainingFormatted.isNotBlank()) {
                                                    append(" ")
                                                    withStyle(SpanStyle(color = redText, fontWeight = FontWeight.Medium)) {
                                                        append(remainingFormatted)
                                                    }
                                                }
                                            },
                                            fontSize = tokens.bodySmall
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        FilterDrawer(
            state = filterDrawerState,
            title = "Filter Records",
            sections = filterSections,
            onApply = { updatedSections -> filterSections = updatedSections },
            onClearAll = { filterSections = getLegalCustomerFilterSections() }
        )

        recordToDelete?.let { targetRecord ->
            DeleteModel(
                title = "Delete Customer Record",
                message = "You're going to delete \"${targetRecord.customerId?.name ?: targetRecord.legalCustomerNumber}\". Are you sure?",
                checkboxText = "I confirm this deletion",
                cancelText = "Cancel",
                confirmText = "Delete",
                onDismiss = { recordToDelete = null },
                onDelete = {
                    viewModel.deleteLegalCustomer(
                        recordId = targetRecord.id,
                        onSuccess = { msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            recordToDelete = null
                        },
                        onError = { err ->
                            Toast.makeText(context, err ?: "Failed to delete record", Toast.LENGTH_SHORT).show()
                            recordToDelete = null
                        }
                    )
                }
            )
        }
    }
}