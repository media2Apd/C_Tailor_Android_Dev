@file:Suppress("unused", "SpellCheckingInspection")
package com.cuso.tailor.view.home.finance.funds.bankstatement

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items // Resolves 'items' & 'count' parameter error
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.finance.BankStatementLineDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.view.composable.ThreeDotLoading
import com.cuso.tailor.viewmodel.FinanceViewModel

@SuppressLint("DefaultLocale")
@Composable
fun BankStatementListScreen(
    viewModel: FinanceViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onNavigateToImport: () -> Unit = {},
    onNavigateToDetail: (String) -> Unit = {}
) {
    val tokens = LocalAppTokens.current

    val bankStatements by viewModel.bankStatements.collectAsState()
    val isLoading by viewModel.isLoadingStatements.collectAsState()
    val isLoadingMore by viewModel.isLoadingMoreStatements.collectAsState()
    val canLoadMore by viewModel.canLoadMoreStatements.collectAsState()
    val bankStatementError by viewModel.bankStatementError.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // 1. Fetch statements immediately on screen launch
    LaunchedEffect(Unit) {
        viewModel.fetchBankStatements(page = 1, limit = 10)
    }

    // 2. Detect infinite scroll
    LaunchedEffect(listState) {
        snapshotFlow {
            val totalItems = listState.layoutInfo.totalItemsCount
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 0 && lastVisibleItem >= totalItems - 2
        }.collect { shouldLoadMore ->
            if (shouldLoadMore && canLoadMore && !isLoading && !isLoadingMore) {
                viewModel.loadMoreBankStatements()
            }
        }
    }

    // 3. Flatten transaction lines
    val flattenedTransactions = remember(bankStatements) {
        bankStatements.flatMap { statement ->
            statement.lines.map { line ->
                FlattenedBankTxItem(
                    statementId = statement.id,
                    statementNumber = statement.statementNumber,
                    bankName = statement.bankAccountId?.bankName ?: "",
                    currency = statement.bankAccountId?.currency ?: "",
                    line = line
                )
            }
        }
    }

    FabScaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary_background),
        fab = FabConfig(
            label = "Import Transaction",
            icon = Icons.Default.FileDownload,
            onClick = onNavigateToImport
        )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TitleBar(
                title = "Bank Statements",
                onClose = onBack
            )

            // Search Bar
            SearchFilterBar(
                query = searchQuery,
                onQueryChange = {
                    searchQuery = it
                    viewModel.fetchBankStatements(page = 1, search = it.trim().ifBlank { null })
                },
                placeholder = "Search SO number, customer...",
                showFilterIcon = true,
                onFilterClick = { }
            )

            when {
                isLoading && flattenedTransactions.isEmpty() -> {
                    ListSkeleton()
                }

                bankStatementError != null && flattenedTransactions.isEmpty() -> {
                    AppErrorState(
                        title = "Failed to load bank statements",
                        message = bankStatementError?.ifBlank { "Something went wrong. Please check your connection and try again." }
                            ?: "Something went wrong. Please check your connection and try again.",
                        onRetry = {
                            viewModel.fetchBankStatements(page = 1, search = searchQuery.trim().ifBlank { null })
                        }
                    )
                }

                flattenedTransactions.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(tokens.screenPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isNotBlank()) "No matching bank transactions found" else "No bank transactions found. Tap Import Transaction to add.",
                            fontSize = tokens.bodySmall,
                            color = mutedText
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            items = flattenedTransactions,
                            key = { "${it.statementId}_${it.line.id}" }
                        ) { item: FlattenedBankTxItem ->
                            val line = item.line

                            val (statusText, statusTextColor, statusBgColor) = when (line.reconciliationStatus.lowercase()) {
                                "reconciled" -> Triple("Matched", greentext, Color(0xFFDCFCE7))
                                "partially_reconciled", "partial" -> Triple(
                                    "Partial",
                                    orangeText,
                                    Color(0xFFFEF3C7)
                                )
                                else -> Triple("Unmatched", redText, Color(0xFFFDE7E7))
                            }

                            DataCard(
                                item = item,
                                modifier = Modifier.fillMaxWidth(),
                                title = line.description.ifBlank { "Bank Transaction" },
                                dateText = line.transactionDate.take(10),
                                code = line.referenceNumber.ifBlank { null },
                                topBadgeText = statusText,
                                topBadgeTextColor = statusTextColor,
                                topBadgeBgColor = statusBgColor,
                                topBadgeDotColor = statusTextColor,
                                footerAsRows = true,
                                onClick = {
                                    if (item.statementId.isNotBlank()) {
                                        onNavigateToDetail(item.statementId)
                                    }
                                },
                                footerFields = listOf(
                                    DataCardField(
                                        label = "Debit",
                                        text = "${currencySymbol()}${
                                            String.format(
                                                LocalLocale.current.platformLocale,
                                                "%,.2f",
                                                line.withdrawal
                                            )
                                        }",
                                        textColor = greentext,
                                        asRow = true
                                    ),
                                    DataCardField(
                                        label = "Credit",
                                        text = "${currencySymbol()}${
                                            String.format(
                                                LocalLocale.current.platformLocale,
                                                "%,.2f",
                                                line.deposit
                                            )
                                        }",
                                        textColor = redText,
                                        asRow = true
                                    ),
                                    DataCardField(
                                        label = "Run Balance",
                                        text = "${currencySymbol()}${
                                            String.format(
                                                LocalLocale.current.platformLocale,
                                                "%,.2f",
                                                line.runningBalance ?: 0.0
                                            )
                                        }",
                                        textColor = TextPrimary,
                                        asRow = true
                                    )
                                ),
                                actions = listOf(
                                    MenuAction(
                                        label = "View Details",
                                        onClick = {
                                            if (item.statementId.isNotBlank()) {
                                                onNavigateToDetail(item.statementId)
                                            }
                                        }
                                    )
                                )
                            )
                        }

                        if (isLoadingMore) {
                            item {
                                ThreeDotLoading(
                                    modifier = Modifier.padding(vertical = 16.dp)
                                )
                            }
                        }

                        item {
                            Spacer(Modifier.height(80.dp))
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// UI DATA HOLDER WITH STATEMENT ID
// -------------------------------------------------------------
data class FlattenedBankTxItem(
    val statementId: String,
    val statementNumber: String,
    val bankName: String,
    val currency: String,
    val line: BankStatementLineDto
)