@file:Suppress("unused", "SpellCheckingInspection")
package com.cuso.tailor.view.home.finance.funds.bankstatement

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLocale
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.finance.BankStatementLineDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.utils.PagingUiState
import com.cuso.tailor.utils.formatCurrency
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

    val statementsPagingState by viewModel.bankStatementsPagingState.collectAsState()
    val bankStatementError by viewModel.bankStatementError.collectAsState()

    var searchQuery by remember { mutableStateOf("") }

    // 1. Fetch statements immediately on screen launch
    LaunchedEffect(Unit) {
        viewModel.fetchBankStatements(page = 1, limit = 10)
    }

    // 2. Flatten transaction lines while preserving PagingUiState flags
    val flattenedPagingState = remember(statementsPagingState) {
        val flattened = statementsPagingState.items.flatMap { statement ->
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
        PagingUiState(
            items = flattened,
            isInitialLoading = statementsPagingState.isInitialLoading,
            isLoadingMore = statementsPagingState.isLoadingMore,
            canLoadMore = statementsPagingState.canLoadMore
        )
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
                bankStatementError != null && flattenedPagingState.items.isEmpty() && !flattenedPagingState.isInitialLoading -> {
                    AppErrorState(
                        title = "Failed to load bank statements",
                        message = bankStatementError?.ifBlank { "Something went wrong. Please check your connection and try again." }
                            ?: "Something went wrong. Please check your connection and try again.",
                        onRetry = {
                            viewModel.fetchBankStatements(page = 1, search = searchQuery.trim().ifBlank { null })
                        }
                    )
                }

                !flattenedPagingState.isInitialLoading && flattenedPagingState.items.isEmpty() -> {
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
                    PaginatedDataList(
                        state = flattenedPagingState,
                        onLoadMore = { viewModel.loadMoreBankStatements() },
                        modifier = Modifier.fillMaxSize()
                    ) { item ->
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
                                    text = "${formatCurrency()}${
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
                                    text = "${formatCurrency()}${
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
                                    text = "${formatCurrency()}${
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