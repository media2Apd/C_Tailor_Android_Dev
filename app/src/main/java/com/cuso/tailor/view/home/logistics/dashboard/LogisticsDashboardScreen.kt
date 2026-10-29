@file:Suppress("unused")

package com.cuso.tailor.view.home.logistics.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.logistics.*
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.DatePickerField
import com.cuso.tailor.view.composable.ListSkeleton
import com.cuso.tailor.viewmodel.LogisticsViewModel

@Composable
fun LogisticsDashboardScreen(
    onNavigateBack: () -> Unit,
    onViewAllShipments: () -> Unit = {},
    onViewAllCodOrders: () -> Unit = {},
    viewModel: LogisticsViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val dashboardData by viewModel.dashboardData.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val trendRange by viewModel.trendRange.collectAsStateWithLifecycle()

    // Date Picker States (UI Format: DD-MM-YYYY)
    var fromDateUi by remember { mutableStateOf("") }
    var toDateUi by remember { mutableStateOf("") }

    // Dropdown state for trend range
    var trendDropdownExpanded by remember { mutableStateOf(false) }

    val trendOptions = remember {
        listOf(
            "Monthly" to "monthly",
            "Daily" to "daily",
            "Weekly" to "weekly"
        )
    }

    Scaffold(
        containerColor = Primary_background
    ) { innerPadding ->
        if (isLoading && dashboardData == null) {
            ListSkeleton()
        } else if (!errorMessage.isNullOrBlank() && dashboardData == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = errorMessage.orEmpty(),
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = redText
                    )
                    Spacer(modifier = Modifier.height(tokens.extraPadding))
                    Button(
                        onClick = {
                            viewModel.fetchDashboardMetrics(
                                startDate = convertUiDateToApiDate(fromDateUi),
                                endDate = convertUiDateToApiDate(toDateUi),
                                trendRange = trendRange
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.7f)
                    ) {
                        Text("Retry", fontSize = tokens.bodySmall, color = whiteBg)
                    }
                }
            }
        } else {
            dashboardData?.let { data ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(vertical = tokens.extraPadding),
                    verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                ) {
                    item{
                        Row(
                            Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Logistics Dashboard",
                                fontSize = tokens.h1,
                                color = title_color,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    // Date Pickers Row (From Date to To Date)
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = tokens.screenPadding),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // From Date Picker
                            Box(modifier = Modifier.weight(1f)) {
                                DatePickerField(
                                    value = fromDateUi,
                                    onDateSelected = { selectedDate ->
                                        fromDateUi = selectedDate
                                        val apiStartDate = convertUiDateToApiDate(selectedDate)
                                        val apiEndDate = convertUiDateToApiDate(toDateUi)
                                        viewModel.fetchDashboardMetrics(
                                            startDate = apiStartDate,
                                            endDate = apiEndDate,
                                            trendRange = trendRange
                                        )
                                    }
                                )
                            }

                            Text(
                                text = "to",
                                fontSize = tokens.caption,
                                fontWeight = FontWeight.Normal,
                                color = mutedText
                            )

                            // To Date Picker
                            Box(modifier = Modifier.weight(1f)) {
                                DatePickerField(
                                    value = toDateUi,
                                    onDateSelected = { selectedDate ->
                                        toDateUi = selectedDate
                                        val apiStartDate = convertUiDateToApiDate(fromDateUi)
                                        val apiEndDate = convertUiDateToApiDate(selectedDate)
                                        viewModel.fetchDashboardMetrics(
                                            startDate = apiStartDate,
                                            endDate = apiEndDate,
                                            trendRange = trendRange
                                        )
                                    }
                                )
                            }
                        }
                    }

                    // Key Metrics Section: 7 Cards in a 2-Column Grid
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = tokens.screenPadding),
                            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                        ) {
                            Text(
                                text = "Key Metrics",
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = Primary
                            )

                            data.kpiCards?.let { kpis ->
                                // Row 1: Total Orders & Delivered Orders
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                                ) {
                                    KpiCard(
                                        title = "Total Orders",
                                        value = kpis.totalOrders?.value?.toString() ?: "0",
                                        trend = kpis.totalOrders?.comparison.orEmpty(),
                                        icon = Icons.Outlined.Inventory2,
                                        iconColor = chartIndigo,
                                        iconBg = purpleBg,
                                        modifier = Modifier.weight(1f)
                                    )
                                    KpiCard(
                                        title = "Delivered Orders",
                                        value = kpis.deliveredOrders?.value?.toString() ?: "0",
                                        trend = kpis.deliveredOrders?.comparison.orEmpty(),
                                        icon = Icons.Outlined.CheckCircle,
                                        iconColor = chartEmerald,
                                        iconBg = greenBg,
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                // Row 2: In Transit & Pending Pickup
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                                ) {
                                    KpiCard(
                                        title = "In Transit",
                                        value = kpis.inTransit?.value?.toString() ?: "0",
                                        trend = kpis.inTransit?.comparison.orEmpty(),
                                        icon = Icons.Outlined.LocalShipping,
                                        iconColor = chartCyan,
                                        iconBg = cyanBg,
                                        modifier = Modifier.weight(1f)
                                    )
                                    KpiCard(
                                        title = "Pending Pickup",
                                        value = kpis.pendingPickup?.value?.toString() ?: "0",
                                        trend = kpis.pendingPickup?.comparison.orEmpty(),
                                        icon = Icons.Default.AccessTime,
                                        iconColor = chartAmber,
                                        iconBg = yellowBg,
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                // Row 3: Return Orders & COD Collections
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                                ) {
                                    KpiCard(
                                        title = "Return Orders",
                                        value = kpis.returnOrders?.value?.toString() ?: "0",
                                        trend = kpis.returnOrders?.comparison.orEmpty(),
                                        icon = Icons.Default.Replay,
                                        iconColor = chartRose,
                                        iconBg = redBg,
                                        modifier = Modifier.weight(1f)
                                    )
                                    KpiCard(
                                        title = "COD Collections",
                                        value = kpis.codCollections?.value ?: "₹0",
                                        trend = kpis.codCollections?.comparison.orEmpty(),
                                        icon = Icons.Default.CurrencyRupee,
                                        iconColor = chartEmerald,
                                        iconBg = greenBg,
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                // Row 4: New Customer Sign-ups
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                                ) {
                                    KpiCard(
                                        title = "New Sign-ups",
                                        value = kpis.newCustomerSignups?.value?.toString() ?: "0",
                                        trend = kpis.newCustomerSignups?.comparison.orEmpty(),
                                        icon = Icons.Outlined.PersonAdd,
                                        iconColor = chartViolet,
                                        iconBg = purpleBg,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    // Shipments & Deliveries Trend Card
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = tokens.screenPadding),
                            shape = RoundedCornerShape(tokens.cardCornerRadius),
                            colors = CardDefaults.cardColors(containerColor = whiteBg),
                            border = BorderStroke(1.dp, sectionBorder)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(tokens.screenPadding),
                                verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "Shipments & Deliveries Trend",
                                            fontSize = tokens.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = TextPrimary
                                        )
                                        Icon(
                                            imageVector = Icons.Outlined.Info,
                                            contentDescription = null,
                                            tint = mutedText,
                                            modifier = Modifier.size(tokens.iconSize * 0.75f)
                                        )
                                    }

                                    // Trend Range Dropdown Matching Reference Style
                                    Box {
                                        val currentLabel = trendOptions.find { it.second == trendRange }?.first ?: "Monthly"

                                        Surface(
                                            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                                            color = if (trendDropdownExpanded) primary_light else badgeGrey,
                                            border = BorderStroke(1.dp, if (trendDropdownExpanded) Primary else BorderGray),
                                            modifier = Modifier.clickable { trendDropdownExpanded = !trendDropdownExpanded }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = currentLabel,
                                                    fontSize = tokens.caption,
                                                    fontWeight = FontWeight.Medium,
                                                    color = if (trendDropdownExpanded) Primary else TextPrimary
                                                )
                                                Icon(
                                                    imageVector = if (trendDropdownExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                                    contentDescription = null,
                                                    tint = if (trendDropdownExpanded) Primary else TextSecondary,
                                                    modifier = Modifier.size(tokens.iconSize * 0.75f)
                                                )
                                            }
                                        }

                                        DropdownMenu(
                                            expanded = trendDropdownExpanded,
                                            onDismissRequest = { trendDropdownExpanded = false },
                                            offset = DpOffset(0.dp, 4.dp),
                                            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.7f),
                                            modifier = Modifier
                                                .width(110.dp)
                                                .background(whiteBg)
                                                .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius * 0.7f))
                                        ) {
                                            trendOptions.forEach { (label, valueKey) ->
                                                val isSelected = valueKey == trendRange

                                                DropdownMenuItem(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .background(if (isSelected) primary_light else Color.Transparent),
                                                    text = {
                                                        Text(
                                                            text = label,
                                                            fontSize = tokens.bodySmall,
                                                            color = if (isSelected) Primary else TextPrimary,
                                                            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                                                        )
                                                    },
                                                    onClick = {
                                                        trendDropdownExpanded = false
                                                        viewModel.fetchDashboardMetrics(
                                                            startDate = convertUiDateToApiDate(fromDateUi),
                                                            endDate = convertUiDateToApiDate(toDateUi),
                                                            trendRange = valueKey
                                                        )
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    LegendIndicator(label = "Shipments", color = chartIndigo)
                                    LegendIndicator(label = "Deliveries", color = chartEmerald)
                                    LegendIndicator(label = "Returns", color = chartRose)
                                }

                                TrendLineChart(
                                    trends = data.shipmentsAndDeliveriesTrend,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(130.dp)
                                )
                            }
                        }
                    }

                    // Delivery Performance Targets Section
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = tokens.screenPadding),
                            shape = RoundedCornerShape(tokens.cardCornerRadius),
                            colors = CardDefaults.cardColors(containerColor = whiteBg),
                            border = BorderStroke(1.dp, sectionBorder)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(tokens.screenPadding),
                                verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "Delivery Performance",
                                        fontSize = tokens.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimary
                                    )
                                    Icon(
                                        imageVector = Icons.Outlined.Info,
                                        contentDescription = null,
                                        tint = mutedText,
                                        modifier = Modifier.size(tokens.iconSize * 0.75f)
                                    )
                                }

                                data.deliveryPerformance?.let { perf ->
                                    PerformanceProgressRow(
                                        title = "On-Time Delivery",
                                        target = perf.onTimeDeliveryRate?.target.orEmpty(),
                                        actual = perf.onTimeDeliveryRate?.actual.orEmpty(),
                                        status = perf.onTimeDeliveryRate?.status.orEmpty(),
                                        progress = parsePercentToFloat(perf.onTimeDeliveryRate?.actual),
                                        barColor = chartBlue
                                    )
                                    PerformanceProgressRow(
                                        title = "Delivery Success Rate",
                                        target = perf.deliverySuccessRate?.target.orEmpty(),
                                        actual = perf.deliverySuccessRate?.actual.orEmpty(),
                                        status = perf.deliverySuccessRate?.status.orEmpty(),
                                        progress = parsePercentToFloat(perf.deliverySuccessRate?.actual),
                                        barColor = chartBlue
                                    )
                                    PerformanceProgressRow(
                                        title = "Return Rate",
                                        target = perf.returnRate?.target.orEmpty(),
                                        actual = perf.returnRate?.actual.orEmpty(),
                                        status = perf.returnRate?.status.orEmpty(),
                                        progress = (parsePercentToFloat(perf.returnRate?.actual) * 10f).coerceIn(0f, 1f),
                                        barColor = chartBlue
                                    )
                                    PerformanceProgressRow(
                                        title = "Avg Delivery Time",
                                        target = perf.avgDeliveryTime?.target.orEmpty(),
                                        actual = perf.avgDeliveryTime?.actual.orEmpty(),
                                        status = perf.avgDeliveryTime?.status.orEmpty(),
                                        progress = 0.57f,
                                        barColor = chartBlue
                                    )
                                    PerformanceProgressRow(
                                        title = "COD Collection Rate",
                                        target = perf.codCollectionRate?.target.orEmpty(),
                                        actual = perf.codCollectionRate?.actual.orEmpty(),
                                        status = perf.codCollectionRate?.status.orEmpty(),
                                        progress = parsePercentToFloat(perf.codCollectionRate?.actual),
                                        barColor = chartBlue
                                    )
                                }
                            }
                        }
                    }

                    // Shipment Status Donut Chart Section
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = tokens.screenPadding),
                            shape = RoundedCornerShape(tokens.cardCornerRadius),
                            colors = CardDefaults.cardColors(containerColor = whiteBg),
                            border = BorderStroke(1.dp, sectionBorder)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(tokens.screenPadding),
                                verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "Shipment Status",
                                        fontSize = tokens.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimary
                                    )
                                    Icon(
                                        imageVector = Icons.Outlined.Info,
                                        contentDescription = null,
                                        tint = mutedText,
                                        modifier = Modifier.size(tokens.iconSize * 0.75f)
                                    )
                                }

                                data.shipmentStatusBreakdown?.let { status ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Box(
                                            modifier = Modifier.size(110.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            DonutStatusChart(
                                                delivered = status.delivered?.count ?: 0,
                                                pending = status.pendingPickup?.count ?: 0,
                                                inTransit = status.inTransit?.count ?: 0,
                                                delayed = status.delayed?.count ?: 0,
                                                cancelled = status.cancelled?.count ?: 0,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    text = status.totalShipments.toString(),
                                                    fontSize = tokens.bodyMedium,
                                                    fontWeight = FontWeight.Medium,
                                                    color = TextPrimary
                                                )
                                                Text(
                                                    text = "Total",
                                                    fontSize = tokens.label,
                                                    fontWeight = FontWeight.Normal,
                                                    color = mutedText
                                                )
                                            }
                                        }

                                        Column(
                                            modifier = Modifier.weight(1f).padding(start = 16.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            StatusBreakdownRow(
                                                title = "Delivered",
                                                count = status.delivered?.count ?: 0,
                                                percent = status.delivered?.percent ?: "0%",
                                                color = chartEmerald
                                            )
                                            StatusBreakdownRow(
                                                title = "In Transit",
                                                count = status.inTransit?.count ?: 0,
                                                percent = status.inTransit?.percent ?: "0%",
                                                color = chartAmber
                                            )
                                            StatusBreakdownRow(
                                                title = "Pending Pickup",
                                                count = status.pendingPickup?.count ?: 0,
                                                percent = status.pendingPickup?.percent ?: "0%",
                                                color = chartBlue
                                            )
                                            StatusBreakdownRow(
                                                title = "Delayed",
                                                count = status.delayed?.count ?: 0,
                                                percent = status.delayed?.percent ?: "0%",
                                                color = chartRose
                                            )
                                            StatusBreakdownRow(
                                                title = "Cancelled",
                                                count = status.cancelled?.count ?: 0,
                                                percent = status.cancelled?.percent ?: "0%",
                                                color = iconMuted
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Shipments by Courier Partner Section
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = tokens.screenPadding),
                            shape = RoundedCornerShape(tokens.cardCornerRadius),
                            colors = CardDefaults.cardColors(containerColor = whiteBg),
                            border = BorderStroke(1.dp, sectionBorder)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(tokens.screenPadding),
                                verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "Shipments by Courier",
                                            fontSize = tokens.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = TextPrimary
                                        )
                                        Icon(
                                            imageVector = Icons.Outlined.Info,
                                            contentDescription = null,
                                            tint = mutedText,
                                            modifier = Modifier.size(tokens.iconSize * 0.75f)
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                                        color = badgeGrey,
                                        border = BorderStroke(1.dp, BorderGray)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = "By Shipment",
                                                fontSize = tokens.caption,
                                                fontWeight = FontWeight.Medium,
                                                color = TextSecondary
                                            )
                                            Icon(
                                                imageVector = Icons.Default.KeyboardArrowDown,
                                                contentDescription = null,
                                                tint = TextSecondary,
                                                modifier = Modifier.size(tokens.iconSize * 0.75f)
                                            )
                                        }
                                    }
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    data.shipmentsByCourierPartner.forEach { courier ->
                                        CourierProgressRow(
                                            courierName = courier.courierName,
                                            count = courier.count,
                                            percentage = courier.percentage
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Recent Shipments Table Section
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = tokens.screenPadding),
                            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.5f)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "Recent Shipments",
                                        fontSize = tokens.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimary
                                    )
                                    Icon(
                                        imageVector = Icons.Outlined.Info,
                                        contentDescription = null,
                                        tint = mutedText,
                                        modifier = Modifier.size(tokens.iconSize * 0.75f)
                                    )
                                }
                                Text(
                                    text = "View all",
                                    fontSize = tokens.caption,
                                    fontWeight = FontWeight.Medium,
                                    color = BluePrimary,
                                    modifier = Modifier.clickable { onViewAllShipments() }
                                )
                            }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(tokens.cardCornerRadius),
                                colors = CardDefaults.cardColors(containerColor = whiteBg),
                                border = BorderStroke(1.dp, sectionBorder)
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(badgeGrey)
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Shipment / Order",
                                            fontSize = tokens.label,
                                            fontWeight = FontWeight.Medium,
                                            color = headerGrey,
                                            modifier = Modifier.weight(1.3f)
                                        )
                                        Text(
                                            text = "Customer & Courier",
                                            fontSize = tokens.label,
                                            fontWeight = FontWeight.Medium,
                                            color = headerGrey,
                                            modifier = Modifier.weight(1.4f)
                                        )
                                        Text(
                                            text = "Status",
                                            fontSize = tokens.label,
                                            fontWeight = FontWeight.Medium,
                                            color = headerGrey,
                                            modifier = Modifier.weight(1.0f)
                                        )
                                    }

                                    data.recentShipments.forEachIndexed { index, shipment ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1.3f)) {
                                                Text(
                                                    text = shipment.shipmentId,
                                                    fontSize = tokens.caption,
                                                    fontWeight = FontWeight.Medium,
                                                    color = TextPrimary
                                                )
                                                Text(
                                                    text = shipment.orderCode,
                                                    fontSize = tokens.label,
                                                    fontWeight = FontWeight.Normal,
                                                    color = mutedText
                                                )
                                            }

                                            Column(modifier = Modifier.weight(1.4f)) {
                                                Text(
                                                    text = shipment.customer,
                                                    fontSize = tokens.caption,
                                                    fontWeight = FontWeight.Medium,
                                                    color = TextPrimary,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = shipment.courierPartner,
                                                    fontSize = tokens.label,
                                                    fontWeight = FontWeight.Normal,
                                                    color = mutedText,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }

                                            Box(modifier = Modifier.weight(1.0f)) {
                                                ShipmentStatusPill(status = shipment.status)
                                            }
                                        }

                                        if (index < data.recentShipments.lastIndex) {
                                            HorizontalDivider(color = dividerColor, thickness = 1.dp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // COD Orders Summary Section
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = tokens.screenPadding),
                            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.5f)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "COD Orders Summary",
                                        fontSize = tokens.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimary
                                    )
                                    Icon(
                                        imageVector = Icons.Outlined.Info,
                                        contentDescription = null,
                                        tint = mutedText,
                                        modifier = Modifier.size(tokens.iconSize * 0.75f)
                                    )
                                }
                                Text(
                                    text = "View all",
                                    fontSize = tokens.caption,
                                    fontWeight = FontWeight.Medium,
                                    color = BluePrimary,
                                    modifier = Modifier.clickable { onViewAllCodOrders() }
                                )
                            }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(tokens.cardCornerRadius),
                                colors = CardDefaults.cardColors(containerColor = whiteBg),
                                border = BorderStroke(1.dp, sectionBorder)
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(badgeGrey)
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Order / Customer",
                                            fontSize = tokens.label,
                                            fontWeight = FontWeight.Medium,
                                            color = headerGrey,
                                            modifier = Modifier.weight(1.4f)
                                        )
                                        Text(
                                            text = "Order Value",
                                            fontSize = tokens.label,
                                            fontWeight = FontWeight.Medium,
                                            color = headerGrey,
                                            modifier = Modifier.weight(1.1f)
                                        )
                                        Text(
                                            text = "Collected",
                                            fontSize = tokens.label,
                                            fontWeight = FontWeight.Medium,
                                            color = headerGrey,
                                            modifier = Modifier.weight(1.1f)
                                        )
                                    }

                                    data.codOrdersSummary.forEachIndexed { index, order ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1.4f)) {
                                                Text(
                                                    text = order.orderCode,
                                                    fontSize = tokens.caption,
                                                    fontWeight = FontWeight.Medium,
                                                    color = TextPrimary
                                                )
                                                Text(
                                                    text = order.customer,
                                                    fontSize = tokens.label,
                                                    fontWeight = FontWeight.Normal,
                                                    color = mutedText
                                                )
                                            }

                                            Text(
                                                text = "₹${order.amount.toInt()}",
                                                fontSize = tokens.caption,
                                                fontWeight = FontWeight.Medium,
                                                color = TextPrimary,
                                                modifier = Modifier.weight(1.1f)
                                            )

                                            Row(
                                                modifier = Modifier.weight(1.1f),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(
                                                    text = "₹${order.collected.toInt()}",
                                                    fontSize = tokens.caption,
                                                    fontWeight = FontWeight.Medium,
                                                    color = if (order.collected > 0) chartEmerald else headerGrey
                                                )
                                                if (order.status.equals("Collected", ignoreCase = true)) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(6.dp)
                                                            .background(darkGreenBg, CircleShape)
                                                    )
                                                }
                                            }
                                        }

                                        if (index < data.codOrdersSummary.lastIndex) {
                                            HorizontalDivider(color = dividerColor, thickness = 1.dp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(tokens.extraPadding))
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Sub-Composables
// ─────────────────────────────────────────────────────────────

@Composable
private fun KpiCard(
    title: String,
    value: String,
    trend: String,
    icon: ImageVector,
    iconColor: Color,
    iconBg: Color,
    modifier: Modifier = Modifier
) {
    val tokens = LocalAppTokens.current
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.7f),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        border = BorderStroke(1.dp, sectionBorder)
    ) {
        Column(
            modifier = Modifier.padding(tokens.extraPadding),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Normal,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .background(iconBg, RoundedCornerShape(tokens.cardCornerRadius * 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(tokens.iconSize * 0.75f)
                    )
                }
            }

            Text(
                text = value,
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (trend.startsWith("-")) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                        contentDescription = null,
                        tint = if (trend.startsWith("-")) chartRose else chartEmerald,
                        modifier = Modifier.size(tokens.iconSize * 0.65f)
                    )
                    Text(
                        text = trend.substringBefore(" "),
                        fontSize = tokens.label,
                        fontWeight = FontWeight.Medium,
                        color = if (trend.startsWith("-")) chartRose else chartEmerald
                    )
                }

                Canvas(modifier = Modifier.size(width = 36.dp, height = 14.dp)) {
                    val path = Path().apply {
                        moveTo(0f, size.height * 0.7f)
                        quadraticTo(size.width * 0.4f, size.height * 0.2f, size.width * 0.7f, size.height * 0.6f)
                        lineTo(size.width, size.height * 0.1f)
                    }
                    drawPath(
                        path = path,
                        color = iconColor,
                        style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }
        }
    }
}

@Composable
private fun LegendIndicator(label: String, color: Color) {
    val tokens = LocalAppTokens.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(color, CircleShape)
        )
        Text(
            text = label,
            fontSize = tokens.caption,
            fontWeight = FontWeight.Normal,
            color = TextSecondary
        )
    }
}

@Composable
private fun TrendLineChart(
    trends: List<ShipmentDeliveryTrendItem>,
    modifier: Modifier = Modifier
) {
    val tokens = LocalAppTokens.current
    if (trends.isEmpty()) return

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            val maxVal = maxOf(
                trends.maxOfOrNull { it.shipments } ?: 1,
                trends.maxOfOrNull { it.deliveries } ?: 1,
                trends.maxOfOrNull { it.returns } ?: 1
            ).coerceAtLeast(1).toFloat()

            val stepX = if (trends.size > 1) size.width / (trends.size - 1) else size.width

            fun getPointY(value: Int): Float {
                val ratio = value / (maxVal * 1.25f)
                return size.height - (ratio * size.height)
            }

            fun drawTrendPath(color: Color, values: List<Int>) {
                val path = Path()
                values.forEachIndexed { index, value ->
                    val x = index * stepX
                    val y = getPointY(value)
                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(path = path, color = color, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
                values.forEachIndexed { index, value ->
                    drawCircle(color = color, radius = 3.dp.toPx(), center = Offset(index * stepX, getPointY(value)))
                }
            }

            drawTrendPath(chartIndigo, trends.map { it.shipments })
            drawTrendPath(chartEmerald, trends.map { it.deliveries })
            drawTrendPath(chartRose, trends.map { it.returns })
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            trends.forEach { item ->
                Text(
                    text = item.month,
                    fontSize = tokens.label,
                    fontWeight = FontWeight.Normal,
                    color = mutedText
                )
            }
        }
    }
}

@Composable
private fun PerformanceProgressRow(
    title: String,
    target: String,
    actual: String,
    status: String,
    progress: Float,
    barColor: Color
) {
    val tokens = LocalAppTokens.current
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = tokens.caption,
                fontWeight = FontWeight.Normal,
                color = TextSecondary
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "$target target",
                    fontSize = tokens.label,
                    fontWeight = FontWeight.Normal,
                    color = mutedText
                )
                Text(
                    text = actual,
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                if (status.isNotBlank()) {
                    val (badgeBg, badgeText) = when (status) {
                        "Good", "On Track" -> greenBg to greentext
                        "Needs Attention" -> yellowBg to yellowText
                        else -> redBg to redText
                    }
                    Surface(
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                        color = badgeBg
                    ) {
                        Text(
                            text = status,
                            fontSize = tokens.label,
                            fontWeight = FontWeight.Medium,
                            color = badgeText,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                }
            }
        }

        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = barColor,
            trackColor = light_grey
        )
    }
}

@Composable
private fun DonutStatusChart(
    delivered: Int,
    pending: Int,
    inTransit: Int,
    delayed: Int,
    cancelled: Int,
    modifier: Modifier = Modifier
) {
    val total = (delivered + pending + inTransit + delayed + cancelled).coerceAtLeast(1).toFloat()

    Canvas(modifier = modifier) {
        val strokeWidth = 10.dp.toPx()
        val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
        val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

        var startAngle = -90f

        fun drawSlice(count: Int, color: Color) {
            if (count <= 0) return
            val sweepAngle = (count / total) * 360f
            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle - 2f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            startAngle += sweepAngle
        }

        drawSlice(delivered, chartEmerald)
        drawSlice(inTransit, chartAmber)
        drawSlice(pending, chartBlue)
        drawSlice(delayed, chartRose)
        drawSlice(cancelled, iconMuted)
    }
}

@Composable
private fun StatusBreakdownRow(
    title: String,
    count: Int,
    percent: String,
    color: Color
) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(color, CircleShape)
            )
            Text(
                text = title,
                fontSize = tokens.caption,
                fontWeight = FontWeight.Normal,
                color = TextSecondary
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = count.toString(),
                fontSize = tokens.caption,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Text(
                text = "($percent)",
                fontSize = tokens.label,
                fontWeight = FontWeight.Normal,
                color = mutedText
            )
        }
    }
}

@Composable
private fun CourierProgressRow(
    courierName: String,
    count: Int,
    percentage: String
) {
    val tokens = LocalAppTokens.current
    val progress = (percentage.replace("%", "").toFloatOrNull() ?: 0f) / 100f

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = courierName,
                fontSize = tokens.caption,
                fontWeight = FontWeight.Normal,
                color = TextPrimary
            )
            Text(
                text = "$count ($percentage)",
                fontSize = tokens.caption,
                fontWeight = FontWeight.Medium,
                color = TextSecondary
            )
        }
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = chartIndigo,
            trackColor = light_grey
        )
    }
}

@Composable
private fun ShipmentStatusPill(status: String) {
    val tokens = LocalAppTokens.current

    val (bgColor, textColor) = when (status.lowercase()) {
        "delivered" -> greenBg to greentext
        "in transit" -> yellowBg to yellowText
        "pending pickup" -> primary_light to BluePrimary
        "delayed" -> redBg to redText
        else -> badgeGrey to headerGrey
    }

    Surface(
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
        color = bgColor
    ) {
        Text(
            text = status,
            fontSize = tokens.label,
            fontWeight = FontWeight.Medium,
            color = textColor,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

private fun parsePercentToFloat(raw: String?): Float {
    if (raw.isNullOrBlank()) return 0f
    val clean = raw.replace("%", "").trim().toFloatOrNull() ?: 0f
    return (clean / 100f).coerceIn(0f, 1f)
}

/**
 * Converts user-selected DD-MM-YYYY format into YYYY-MM-DD expected by the API
 */
private fun convertUiDateToApiDate(uiDate: String): String {
    if (uiDate.isBlank()) return ""
    return try {
        val parts = uiDate.split("-")
        if (parts.size == 3 && parts[0].length == 2 && parts[2].length == 4) {
            "${parts[2]}-${parts[1]}-${parts[0]}"
        } else {
            uiDate
        }
    } catch (_: Exception) {
        uiDate
    }
}