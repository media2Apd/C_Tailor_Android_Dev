@file:Suppress("UNUSED_PARAMETER",
    "UNUSED",
    "RedundantSuppression",
    "unused_variable",
    "AssignedValueIsNeverRead", "VariableNeverRead"
)

package com.cuso.mobile.view.home.finance.finance_core.trial_balance

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.mobile.model.finance.LedgerItem
import com.cuso.mobile.ui.theme.BluePrimary
import com.cuso.mobile.ui.theme.BorderGray
import com.cuso.mobile.ui.theme.TextPrimary
import com.cuso.mobile.ui.theme.TextSecondary
import com.cuso.mobile.ui.theme.blackTitle
import com.cuso.mobile.ui.theme.title_color
import com.cuso.mobile.ui.theme.title_font
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.ScreenBreadcrumb
import com.cuso.mobile.view.composable.DataCard
import com.cuso.mobile.view.composable.DataCardField
import com.cuso.mobile.view.composable.ListSkeleton
import com.cuso.mobile.view.composable.SearchFilterBar
import com.cuso.mobile.view.home.finance.account_receivable.customers.formatDate
import com.cuso.mobile.viewmodel.FinanceViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone



private fun formatLedgerAmount(value: Double): String {
    val nf = NumberFormat.getNumberInstance(Locale.Builder().setLanguage("en").setRegion("IN").build())
    nf.maximumFractionDigits = 0
    return nf.format(value)
}

private fun formatLedgerDate(iso: String): String {
    return try {
        val inFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val outFmt = SimpleDateFormat("dd MM,yyyy", Locale.ENGLISH)
        val date = inFmt.parse(iso.substringBefore(".")) ?: return iso
        outFmt.format(date)
    } catch (_: Exception) {
        iso
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerScreen(
    accountId: String,
    accountName: String = "Ledger",
    onClose: () -> Unit = {},
    onBreadcrumbClick: () -> Unit = {},   //   NEW
    financeViewModel: FinanceViewModel = hiltViewModel()
) {
    val items by financeViewModel.ledgerList.collectAsStateWithLifecycle()
    val isLoading by financeViewModel.isLoadingLedger.collectAsStateWithLifecycle()
    val errorMessage by financeViewModel.ledgerError.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(accountId) {
        financeViewModel.fetchLedger(accountId)
    }

    val filteredItems = remember(items, searchQuery) {
        if (searchQuery.isBlank()) items
        else items.filter {
            (it.reference?.contains(searchQuery, ignoreCase = true) == true) ||
                    (it.journalNumber?.contains(searchQuery, ignoreCase = true) == true) ||
                    it.account.contains(searchQuery, ignoreCase = true)
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
                .background(whiteBg)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Ledger", fontSize = title_font, fontWeight = FontWeight.Bold, color = title_color)
            Icon(
                Icons.Default.Close,
                contentDescription = "Close",
                tint = TextPrimary,
                modifier = Modifier.size(22.dp).clickable { onClose() }
            )
        }

        Column {
            // ── Breadcrumb ──
            ScreenBreadcrumb(
                segments = listOf("Finance", "Ledger"),
                onClick = onBreadcrumbClick
            )

            SearchFilterBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Search Ledgers...",
                accentColor = BluePrimary,
                borderColor = BorderGray,
                textSecondaryColor = TextSecondary,
                onFilterClick = {  }
            )
        }

        when {
            isLoading -> {
                ListSkeleton()
            }
            errorMessage != null -> {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Text(text = errorMessage ?: "Something went wrong", color = Color.Red)
                }
            }
            filteredItems.isEmpty() -> {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Text("No ledger entries found", color = TextSecondary)
                }
            }
            else -> {
                LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    items(filteredItems, key = { it.id }) { entry ->
                        DataCard(
                            item = entry,
                            title = entry.reference ?: "-",
                            titleColor = TextPrimary,
                            subtitle = "${entry.journalNumber ?: "-"} • ${if (entry.credit > 0) "Credit" else "Debit"}",
                            footerAsRows = true, //   each field renders as a full-width SpaceBetween row, same as TrialBalanceScreen
                            footerFields = listOf(
                                DataCardField(
                                    label = "Date",
                                    text = formatDate(entry.date),
                                    textColor = blackTitle,
                                    labelColor = TextSecondary
                                ),
                                DataCardField(
                                    label = "Debit",
                                    text = if (entry.debit > 0) formatLedgerAmount(entry.debit) else "-",
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

@Composable
private fun LedgerCard(entry: LedgerItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Text(
            text = formatLedgerDate(entry.date),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "${entry.journalNumber ?: "-"} • ${if (entry.credit > 0) "Credit" else "Debit"}",
            fontSize = 12.sp,
            color = TextSecondary
        )

        Spacer(Modifier.height(10.dp))

        LedgerRow(label = "Description", value = entry.reference ?: "-")
        LedgerRow(label = "Debit", value = if (entry.debit > 0) formatLedgerAmount(entry.debit) else "-")
        LedgerRow(label = "Credit", value = if (entry.credit > 0) formatLedgerAmount(entry.credit) else "-")
        LedgerRow(label = "Balance", value = formatLedgerAmount(entry.balance))
    }
}

@Composable
private fun LedgerRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = TextSecondary)
        Text(value, fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
    }
}