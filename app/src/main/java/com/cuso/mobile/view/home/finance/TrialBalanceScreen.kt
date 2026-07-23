@file:Suppress("UNUSED_VALUE", "ASSIGNED_VALUE_IS_NEVER_READ","UNUSED_PARAMETER")
package com.cuso.mobile.view.home.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Close
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
import com.cuso.mobile.view.composable.ScreenBreadcrumb
import com.cuso.mobile.view.home.reusablecomposables.DataCard
import com.cuso.mobile.view.home.reusablecomposables.DataCardField
import com.cuso.mobile.view.home.reusablecomposables.ListSkeleton
import com.cuso.mobile.view.home.reusablecomposables.SearchFilterBar
import com.cuso.mobile.viewmodel.FinanceViewModel
import java.text.NumberFormat
import java.util.Locale

private val TextPrimary = Color(0xFF111827)
private val TextSecondary = Color(0xFF9A9AA8)
private val BorderGray = Color(0xFFE8E8ED)
private val BluePrimary = Color(0xFF3A2FCB)
private val RedText = Color(0xFFDC2626)
private val RedBg = Color(0xFFFDECEC)
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
    onBreadcrumbClick: () -> Unit = {},   // ✅ NEW
    financeViewModel: FinanceViewModel = hiltViewModel()
) {
    val items by financeViewModel.trialBalanceList.collectAsStateWithLifecycle()
    val isLoading by financeViewModel.isLoadingTrialBalance.collectAsStateWithLifecycle()
    val errorMessage by financeViewModel.trialBalanceError.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        financeViewModel.fetchTrialBalance()
    }

    val filteredItems = remember(items, searchQuery) {
        if (searchQuery.isBlank()) items
        else items.filter {
            it.account.contains(searchQuery, ignoreCase = true) ||
                    it.code.contains(searchQuery, ignoreCase = true)
        }
    }

    val totalDebit = remember(items) { items.sumOf { it.debit } }
    val totalCredit = remember(items) { items.sumOf { it.credit } }
    val imbalance = remember(totalDebit, totalCredit) { kotlin.math.abs(totalDebit - totalCredit) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // ── Title bar ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Trial Balance", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Icon(
                Icons.Default.Close,
                contentDescription = "Close",
                tint = TextPrimary,
                modifier = Modifier.size(22.dp).clickable { onClose() }
            )
        }

        Column(Modifier.background(PanelBg)) {
            // ── Breadcrumb ──
            ScreenBreadcrumb(
                segments = listOf("Finance", "Trial Balance"),
                onClick = onBreadcrumbClick
            )

            // ── Search + Filter ──
            SearchFilterBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Search Trial balance...",
                accentColor = BluePrimary,
                borderColor = BorderGray,
                textSecondaryColor = TextSecondary,
                onFilterClick = { /* open your filter drawer here */ },
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
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
                    Text("No accounts found", color = TextSecondary)
                }
            }
            else -> {
                LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    items(filteredItems, key = { it.accountId }) { item ->
                        // ✅ Reusing the shared DataCard component.
                        // Debit / Credit / BAL passed as footerFields with asRow=true
                        // so DataCard renders each as a SpaceBetween row → label on
                        // the left, value on the right — all three values land on the
                        // exact same right-edge column, same as the header/title column.
                        Box(modifier = Modifier.clickable { onAccountClick(item.accountId, item.account) }) {

                            DataCard(
                                item = item,
                                title = item.account,
                                trailingText = "Code: ${item.code}",
                                titleColor = TextPrimary,
                                titleFontSize = 16.sp,
                                footerAsRows = true, // every field below renders as a full-width row
                                footerFields = listOf(
                                    DataCardField(
                                        label = "Debit",
                                        text = formatAmount(item.debit),
                                        textColor = Color.Black,      // ✅ value in black
                                        labelColor = TextSecondary
                                    ),
                                    DataCardField(
                                        label = "Credit",
                                        text = formatAmount(item.credit),
                                        textColor = Color.Black,      // ✅ value in black
                                        labelColor = TextSecondary
                                    ),
                                    DataCardField(
                                        label = "BAL",
                                        text = "${formatAmount(item.balanceAbs)} ${item.balanceLabel}",
                                        textColor = Color.Black,      // ✅ value in black
                                        labelColor = TextSecondary
                                    )
                                )
                            )
                        }
                    }

                    // ── Total Summary card ──
                    item {
                        Column(
                            Modifier.background(PanelBg)
                        ) {
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
private fun TotalSummaryCard(totalDebit: Double, totalCredit: Double, imbalance: Double) {

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
                Icon(Icons.Default.BarChart, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
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
                Text(formatAmount(totalCredit), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Green)
            }
        }

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
                    Icon(Icons.Default.Warning, contentDescription = null, tint = RedText, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Imbalance", color = RedText, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
                Text(formatAmount(imbalance), color = RedText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}