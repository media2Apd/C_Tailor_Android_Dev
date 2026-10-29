@file:Suppress("unused", "SpellCheckingInspection")
package com.cuso.tailor.view.home.finance.funds.bankstatement

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.cuso.tailor.viewmodel.FinanceViewModel

@SuppressLint("DefaultLocale")
@Composable
fun BankStatementListScreen(
    viewModel: FinanceViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onNavigateToImport: () -> Unit = {},
    onNavigateToDetail: (String) -> Unit = {} // ✅ Detail screen-க்கு ID அனுப்பும் callback
) {
    val tokens = LocalAppTokens.current

    val bankStatements by viewModel.bankStatements.collectAsState()
    val isLoading by viewModel.isLoadingStatements.collectAsState()
    val isLoadingMore by viewModel.isLoadingMoreStatements.collectAsState()
    val canLoadMore by viewModel.canLoadMoreStatements.collectAsState()

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
            lastVisibleItem >= totalItems - 2
        }.collect { shouldLoadMore ->
            if (shouldLoadMore && canLoadMore && !isLoading && !isLoadingMore) {
                viewModel.loadMoreBankStatements()
            }
        }
    }

    // 3. Flatten transaction lines - statement ID சரியாக map செய்யப்பட்டுள்ளது
    val flattenedTransactions = remember(bankStatements) {
        bankStatements.flatMap { statement ->
            statement.lines.map { line ->
                FlattenedBankTxItem(
                    statementId = statement.id, // ✅ Parent statement ID
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
                    viewModel.fetchBankStatements(page = 1, search = it)
                },
                placeholder = "Search SO number, customer...",
                showFilterIcon = true,
                onFilterClick = { }
            )

            if (isLoading && flattenedTransactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(tokens.screenPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primary, modifier = Modifier.size(28.dp))
                }
            } else if (flattenedTransactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(tokens.screenPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No bank transactions found. Tap Import Transaction to add.",
                        fontSize = tokens.bodySmall,
                        color = mutedText
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(flattenedTransactions) { _, item ->
                        val line = item.line

                        val (statusText, statusTextColor, statusBgColor) = when (line.reconciliationStatus.lowercase()) {
                            "reconciled" -> Triple("Matched", greentext, Color(0xFFDCFCE7))
                            "partially_reconciled", "partial" -> Triple("Partial", orangeText, Color(0xFFFEF3C7))
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
                            // ✅ Card-ஐ கிளிக் செய்தால் ID null இல்லாமல் navigate ஆகும்
                            onClick = {
                                if (item.statementId.isNotBlank()) {
                                    onNavigateToDetail(item.statementId)
                                }
                            },
                            footerFields = listOf(
                                DataCardField(
                                    label = "Debit",
                                    text = "${currencySymbol()}${String.format(LocalLocale.current.platformLocale, "%,.2f", line.withdrawal)}",
                                    textColor = greentext,
                                    asRow = true
                                ),
                                DataCardField(
                                    label = "Credit",
                                    text = "${currencySymbol()}${String.format(LocalLocale.current.platformLocale, "%,.2f", line.deposit)}",
                                    textColor = redText,
                                    asRow = true
                                ),
                                DataCardField(
                                    label = "Run Balance",
                                    text = "${currencySymbol()}${String.format(LocalLocale.current.platformLocale, "%,.2f", line.runningBalance ?: 0.0)}",
                                    textColor = TextPrimary,
                                    asRow = true
                                )
                            ),
                            actions = listOf(
                                MenuAction(
                                    label = "View Details",
                                    onClick = {
                                        // ✅ Menu Action வழியாகவும் Detail-க்கு செல்லும்
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
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Primary, modifier = Modifier.size(20.dp))
                            }
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

// -------------------------------------------------------------
// UI DATA HOLDER WITH STATEMENT ID
// -------------------------------------------------------------
data class FlattenedBankTxItem(
    val statementId: String, // ✅ Added statementId
    val statementNumber: String,
    val bankName: String,
    val currency: String,
    val line: BankStatementLineDto
)