@file:Suppress("UNUSED_VALUE", "ASSIGNED_VALUE_IS_NEVER_READ", "UNUSED_PARAMETER")

package com.cuso.mobile.view.home.finance.finance_core.trial_balance

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.mobile.model.finance.TrialBalanceItem
import com.cuso.mobile.ui.theme.title_border
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.ListSkeleton
import com.cuso.mobile.view.composable.SearchFilterBar
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.viewmodel.FinanceViewModel
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

private val TextPrimary = Color(0xFF111827)
private val TextSecondary = Color(0xFF9A9AA8)
private val BorderGray = Color(0xFFE8E8ED)
private val BluePrimary = Color(0xFF3A2FCB)
private val RedText = Color(0xFFDC2626)
private val RedBg = Color(0xFFFDECEC)
private val GreenText = Color(0xFF16A34A)
private val PanelBg = Color(0xFFF7F7FA)

private fun formatAmount(value: Double): String {
    val nf = NumberFormat.getNumberInstance(Locale.Builder().setLanguage("en").setRegion("IN").build())
    nf.maximumFractionDigits = 0
    return nf.format(value)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrialBalanceScreen(
    onClose: () -> Unit = {},
    onAccountClick: (accountId: String, accountName: String) -> Unit = { _, _ -> },
    onBreadcrumbClick: () -> Unit = {},
    financeViewModel: FinanceViewModel = hiltViewModel()
) {
    val items by financeViewModel.trialBalanceList.collectAsStateWithLifecycle()
    val isLoading by financeViewModel.isLoadingTrialBalance.collectAsStateWithLifecycle()
    val errorMessage by financeViewModel.trialBalanceError.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var debouncedSearchQuery by remember { mutableStateOf("") }

    val listState = rememberLazyListState()

    // Fetch on initial screen entry
    LaunchedEffect(Unit) {
        financeViewModel.fetchTrialBalance()
    }

    // Debounce search query filtering
    LaunchedEffect(searchQuery) {
        delay(300)
        debouncedSearchQuery = searchQuery.trim()
    }

    // Filter trial balance list based on debounced search query
    val filteredItems = remember(items, debouncedSearchQuery) {
        if (debouncedSearchQuery.isBlank()) items
        else items.filter {
            it.account.contains(debouncedSearchQuery, ignoreCase = true) ||
                    it.code.contains(debouncedSearchQuery, ignoreCase = true) ||
                    it.accountType.contains(debouncedSearchQuery, ignoreCase = true)
        }
    }

    // Calculate report totals
    val totalDebit = remember(items) { items.sumOf { it.debit } }
    val totalCredit = remember(items) { items.sumOf { it.credit } }
    val imbalance = remember(totalDebit, totalCredit) { abs(totalDebit - totalCredit) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        // ── Title bar ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TitleBar("Trial Balance", onClose = onClose)
        }

        Column {


            // ── Search & Filter bar ──
            SearchFilterBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Search Trial balance...",
                accentColor = BluePrimary,
                borderColor = BorderGray,
                textSecondaryColor = TextSecondary,
                onFilterClick = { }
            )
        }
        HorizontalDivider(color = title_border)

        when {
            // Loading skeleton state
            isLoading && items.isEmpty() -> {
                ListSkeleton()
            }

            // Error state with retry option
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
                            tint = RedText,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = errorMessage ?: "Something went wrong",
                            color = RedText,
                            fontSize = 14.sp
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { financeViewModel.fetchTrialBalance() },
                            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Retry", color = whiteBg, fontSize = 14.sp)
                        }
                    }
                }
            }

            // Empty state
            filteredItems.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No accounts found", color = TextSecondary, fontSize = 14.sp)
                }
            }

            // Trial balance list & total summary
            else -> {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    items(filteredItems, key = { it.accountId }) { item ->
                        TrialBalanceItemCard(
                            item = item,
                            onClick = { onAccountClick(item.accountId, item.account) }
                        )
                    }

                    // ── Total Summary Card ──
                    item {
                        Column(modifier = Modifier.background(PanelBg)) {
                            Spacer(modifier = Modifier.padding(top = 10.dp))
                            TotalSummaryCard(
                                totalDebit = totalDebit,
                                totalCredit = totalCredit,
                                imbalance = imbalance
                            )
                        }
                    }

                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }
    }
}

@Composable
private fun TrialBalanceItemCard(
    item: TrialBalanceItem,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(whiteBg)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Side: Account Name + Code
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.account,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1F2937)
                )
                Text(
                    text = "Code: ${item.code}",
                    fontSize = 13.sp,
                    color = Color(0xFF9CA3AF)
                )
            }

            // Right Side: Debit, Credit, BAL aligned to the right
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Debit Row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Debit: ",
                        fontSize = 13.sp,
                        color = Color(0xFF9CA3AF)
                    )
                    Text(
                        text = formatAmount(item.debit),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1F2937)
                    )
                }

                // Credit Row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Credit: ",
                        fontSize = 13.sp,
                        color = Color(0xFF9CA3AF)
                    )
                    Text(
                        text = formatAmount(item.credit),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1F2937)
                    )
                }

                // Balance Row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "BAL: ",
                        fontSize = 13.sp,
                        color = Color(0xFF9CA3AF)
                    )
                    Text(
                        text = "${formatAmount(item.balanceAbs)} ${item.balanceLabel}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1F2937)
                    )
                }
            }
        }
        HorizontalDivider(color = BorderGray, thickness = 1.dp)
    }
}

@Composable
private fun TotalSummaryCard(
    totalDebit: Double,
    totalCredit: Double,
    imbalance: Double
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("TOTAL SUMMARY", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(BluePrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.BarChart,
                    contentDescription = null,
                    tint = whiteBg,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        HorizontalDivider(color = BorderGray, modifier = Modifier.padding(vertical = 12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Total Debit", fontSize = 12.sp, color = TextSecondary)
                Text(formatAmount(totalDebit), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = RedText)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Total Credit", fontSize = 12.sp, color = TextSecondary)
                Text(formatAmount(totalCredit), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GreenText)
            }
        }

        // Show imbalance indicator if debits and credits do not balance
        if (imbalance != 0.0) {
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(RedBg)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = RedText,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Imbalance", color = RedText, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
                Text(formatAmount(imbalance), color = RedText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}