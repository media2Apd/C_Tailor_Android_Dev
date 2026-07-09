package com.cuso.mobile.view.home.sales.pricing_and_quotation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.mobile.model.PricingCategoryItem
import com.cuso.mobile.model.PricingStatValue
import com.cuso.mobile.view.home.reusablecomposables.FabConfig
import com.cuso.mobile.view.home.reusablecomposables.FabScaffold
import com.cuso.mobile.viewmodel.PricingQuotationUiState
import com.cuso.mobile.viewmodel.PricingQuotationViewModel

private val Primary = Color(0xFF3B3BF9)
private val PrimaryLight = Color(0xFFEEF0FF)
private val TextMuted = Color(0xFF9CA3AF)
private val TextDark = Color(0xFF111827)
private val Success = Color(0xFF22C55E)
private val CardBorder = Color(0xFFF0F0F0)
private val StripBg = Color(0xFFF9FAFB)

@Composable
fun PricingQuotationScreen(
    onClose: () -> Unit,
    onCategoryClick: (PricingCategoryItem) -> Unit = {},
    onAddNewPricing: () -> Unit = {}   // ✅ NEW — FAB click callback
) {
    val viewModel: PricingQuotationViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // ✅ NEW — wraps everything so the FAB floats above the scrollable content
    FabScaffold(
        fab = FabConfig(
            label = "Add New Pricing",
            icon = Icons.Default.Add,
            onClick = onAddNewPricing,
            bottomPadding = 24.dp
        ),
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F7))
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Pricing & Quotation", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close",
                    tint = TextDark,
                    modifier = Modifier.size(22.dp).clickable { onClose() }
                )
            }
            HorizontalDivider(color = CardBorder)

            // Breadcrumb
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Sales", fontSize = 13.sp, color = TextMuted)
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp)
                )
                Text("Pricing & Quotations", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Primary)
            }
            HorizontalDivider(color = CardBorder)

            when (val state = uiState) {
                is PricingQuotationUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Primary)
                    }
                }
                is PricingQuotationUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Warning, null, tint = Color.Red, modifier = Modifier.size(40.dp))
                            Spacer(Modifier.height(8.dp))
                            Text(state.message, color = Color.Red, fontSize = 13.sp)
                            Spacer(Modifier.height(12.dp))
                            Button(onClick = { viewModel.loadPricingQuotation() }) { Text("Retry") }
                        }
                    }
                }
                is PricingQuotationUiState.Success -> {
                    PricingQuotationContent(
                        stats = state.data.stats,
                        categories = state.data.categories,
                        onCategoryClick = onCategoryClick
                    )
                }
            }
        }
    }
}

@Composable
private fun PricingQuotationContent(
    stats: com.cuso.mobile.model.PricingStats,
    categories: List<PricingCategoryItem>,
    onCategoryClick: (PricingCategoryItem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Stats grid - 2x2
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(label = "Active Quotations", stat = stats.activeQuotations, modifier = Modifier.weight(1f))
            StatCard(label = "Avg. Quote Value", stat = stats.avgQuoteValue, modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(label = "Approval Rate", stat = stats.approvalRate, modifier = Modifier.weight(1f))
            StatCard(label = "This Month", stat = stats.thisMonth, modifier = Modifier.weight(1f))
        }

        Spacer(Modifier.height(20.dp))

        // Pricing category list
        categories.forEach { category ->
            PricingCategoryCard(category = category, onClick = { onCategoryClick(category) })
            Spacer(Modifier.height(12.dp))
        }

        // ✅ NEW — extra bottom space so last card isn't hidden behind the floating FAB
        Spacer(Modifier.height(90.dp))
    }
}

@Composable
private fun StatCard(label: String, stat: PricingStatValue, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(label, fontSize = 12.sp, color = TextMuted)
            Spacer(Modifier.height(6.dp))
            Text(stat.value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.ArrowUpward,
                    contentDescription = null,
                    tint = Success,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(Modifier.width(2.dp))
                Text(
                    "+${stat.changePercent.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() }}% ${stat.changeLabel}",
                    fontSize = 11.sp,
                    color = Success
                )
            }
        }
    }
}

@Composable
private fun PricingCategoryCard(category: PricingCategoryItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(PrimaryLight, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.BarChart,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(category.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                    Text(category.subtitle, fontSize = 12.sp, color = TextMuted)
                }
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Base price strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StripBg)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Base Price Range", fontSize = 12.sp, color = TextMuted)
                Text(
                    "₹${category.basePriceMin.toInt()}–₹${category.basePriceMax.toInt()}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark
                )
            }
        }
    }
}