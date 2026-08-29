@file:Suppress(
    "UNUSED_PARAMETER",
    "UNUSED",
    "RedundantSuppression",
    "unused_variable",
    "AssignedValueIsNeverRead",
    "VariableNeverRead"
)

package com.cuso.mobile.view.home.finance.finance_core.trial_balance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.mobile.ui.theme.BluePrimary
import com.cuso.mobile.ui.theme.BorderGray
import com.cuso.mobile.ui.theme.TextPrimary
import com.cuso.mobile.ui.theme.TextSecondary
import com.cuso.mobile.ui.theme.blackTitle
import com.cuso.mobile.ui.theme.title_border
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.DataCard
import com.cuso.mobile.view.composable.DataCardField
import com.cuso.mobile.view.composable.ListSkeleton
import com.cuso.mobile.view.composable.SearchFilterBar
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.view.home.finance.account_receivable.customers.formatDate
import com.cuso.mobile.viewmodel.FinanceViewModel
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.Locale

private fun formatLedgerAmount(value: Double): String {
    val nf = NumberFormat.getNumberInstance(Locale.Builder().setLanguage("en").setRegion("IN").build())
    nf.maximumFractionDigits = 0
    return nf.format(value)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerScreen(
    accountId: String,
    accountName: String = "Ledger",
    onClose: () -> Unit = {},
    onBreadcrumbClick: () -> Unit = {},
    financeViewModel: FinanceViewModel = hiltViewModel()
) {
    val items by financeViewModel.ledgerList.collectAsStateWithLifecycle()
    val isLoading by financeViewModel.isLoadingLedger.collectAsStateWithLifecycle()
    val errorMessage by financeViewModel.ledgerError.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var debouncedSearchQuery by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Fetch ledger details for selected account
    LaunchedEffect(accountId) {
        financeViewModel.fetchLedger(accountId)
    }

    // Debounce search query
    LaunchedEffect(searchQuery) {
        delay(300)
        debouncedSearchQuery = searchQuery.trim()
    }

    val filteredItems = remember(items, debouncedSearchQuery) {
        if (debouncedSearchQuery.isBlank()) items
        else items.filter {
            (it.reference?.contains(debouncedSearchQuery, ignoreCase = true) == true) ||
                    (it.journalNumber?.contains(debouncedSearchQuery, ignoreCase = true) == true) ||
                    (it.account?.contains(debouncedSearchQuery, ignoreCase = true) == true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        // ── Title bar ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            TitleBar("Ledger", onClose = onClose)
        }

        Column {


            SearchFilterBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Search Ledgers...",
                accentColor = BluePrimary,
                borderColor = BorderGray,
                textSecondaryColor = TextSecondary,
                onFilterClick = { }
            )
        }
        HorizontalDivider(color = title_border)

        when {
            isLoading && items.isEmpty() -> {
                ListSkeleton()
            }
            errorMessage != null && items.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color.Red,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = errorMessage ?: "Something went wrong",
                            color = Color.Red,
                            fontSize = 14.sp
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { financeViewModel.fetchLedger(accountId) },
                            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Retry", color = whiteBg, fontSize = 14.sp)
                        }
                    }
                }
            }
            filteredItems.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No ledger entries found", color = TextSecondary, fontSize = 14.sp)
                }
            }
            else -> {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    items(filteredItems, key = { it.id }) { entry ->
                        DataCard(
                            item = entry,
                            title = entry.date?.let{formatDate(it)}?.ifBlank { entry.journalNumber }
                                ?: "-",
                            titleColor = TextPrimary,
                            subtitle = "${entry.code ?: "-"} • ${if (entry.credit > 0) "Credit" else "Debit"}",
                            footerAsRows = true,
                            footerFields = listOf(
                                DataCardField(
                                    label = "Description",
                                    text = "Not found",
                                    textColor = blackTitle,
                                    labelColor = TextSecondary
                                ),
                                DataCardField(
                                    label = "Debit",
                                    text = formatLedgerAmount (entry.debit) ,
                                    textColor = blackTitle,
                                    labelColor = TextSecondary
                                ),
                                DataCardField(
                                    label = "Credit",
                                    text = if (entry.credit > 0) formatLedgerAmount(entry.credit) else "-",
                                    textColor = blackTitle,
                                    labelColor = TextSecondary
                                ),
                                DataCardField(
                                    label = "Balance",
                                    text = formatLedgerAmount(entry.balance),
                                    textColor = blackTitle,
                                    labelColor = TextSecondary
                                )
                            )
                        )
                    }
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }
    }
}