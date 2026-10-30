package com.cuso.tailor.view.home.logistics.courier_partners

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.logistics.CourierPartnerItemDto
import com.cuso.tailor.ui.theme.PanelBg
import com.cuso.tailor.ui.theme.Primary_background
import com.cuso.tailor.ui.theme.greenBg
import com.cuso.tailor.ui.theme.greentext
import com.cuso.tailor.ui.theme.headerGrey
import com.cuso.tailor.ui.theme.redBg
import com.cuso.tailor.ui.theme.redText
import com.cuso.tailor.ui.theme.sectionBorder
import com.cuso.tailor.ui.theme.title_color
import com.cuso.tailor.view.composable.ActionDropdownMenu
import com.cuso.tailor.view.composable.DataCard
import com.cuso.tailor.view.composable.FabConfig
import com.cuso.tailor.view.composable.FabScaffold
import com.cuso.tailor.view.composable.ListSkeleton
import com.cuso.tailor.view.composable.MenuAction
import com.cuso.tailor.view.composable.SearchFilterBar
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.viewmodel.LogisticsViewModel

@Composable
fun CourierPartnerListScreen(
    viewModel: LogisticsViewModel,
    onBack: () -> Unit,
    onNewShipmentClick: () -> Unit,
    onViewCourierDetail: (String) -> Unit = {}
){
    val tokens = LocalAppTokens.current

    // Observe courier partner state directly from existing LogisticsViewModel
    val courierPartners by viewModel.courierPartnersList.collectAsState()
    val isLoading by viewModel.isLoadingCouriers.collectAsState()

    // Local search state handled directly in Compose
    var searchQuery by remember { mutableStateOf("") }

    // Fetch courier partners on launch using existing function
    LaunchedEffect(Unit) {
        viewModel.fetchCourierPartners()
    }

    // Filter couriers locally based on search text without modifying the ViewModel
    val filteredCouriers = remember(courierPartners, searchQuery) {
        val cleanQuery = searchQuery.trim().lowercase()
        if (cleanQuery.isEmpty()) {
            courierPartners
        } else {
            courierPartners.filter { item ->
                item.courierName.lowercase().contains(cleanQuery) ||
                        item.transportMode.lowercase().contains(cleanQuery) ||
                        (item.regionType?.lowercase()?.contains(cleanQuery) == true)
            }
        }
    }

    // Floating action button (+ New Shipment)
    val fabConfig = remember(onNewShipmentClick) {
        FabConfig(
            label = "New Shipment",
            icon = Icons.Default.Add,
            onClick = onNewShipmentClick
        )
    }

    FabScaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary_background),
        fab = fabConfig,
        fabVisible = true
    ) {
        Scaffold(
            containerColor = Primary_background,
            topBar = {
                TitleBar(
                    title = "All Courier",
                    onClose = onBack
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Search & Filter Bar
                SearchFilterBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search SO number, customer...",
                    showFilterIcon = true,
                    height = 44.dp
                )

                Spacer(modifier = Modifier.height(8.dp))

                when {
                    isLoading && courierPartners.isEmpty() -> {
                        ListSkeleton()
                    }

                    filteredCouriers.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(tokens.screenPadding),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No records found",
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = headerGrey
                            )
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(
                                items = filteredCouriers,
                                key = { it.id }
                            ) { item ->
                                CourierPartnerCard(
                                    item = item,
                                    formatDateHelper = { rawDate -> viewModel.formatDeliveryDate(rawDate) },
                                    onViewCourierDetail = onViewCourierDetail
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Courier Partner Card component matching the screenshot layout.
 */
@Composable
private fun CourierPartnerCard(
    item: CourierPartnerItemDto,
    formatDateHelper: (String) -> String,
    onViewCourierDetail: (String) -> Unit = {}
) {
    val tokens = LocalAppTokens.current

    val isActive = item.status.equals("Active", ignoreCase = true)

    val formattedDate = remember(item.createdAt) {
        if (!item.createdAt.isNullOrBlank()) formatDateHelper(item.createdAt)
        else "26 Feb 2026"
    }

    val serviceabilityText = remember(item.serviceability, item.regionType) {
        if (item.serviceability?.coverageType.equals("DOMESTIC", ignoreCase = true)) {
            "All India"
        } else {
            item.regionType ?: "All India"
        }
    }

    val actions = remember(item.id, onViewCourierDetail) {
        listOf(
            MenuAction(
                label = "View Details",
                onClick = { if (item.id.isNotBlank()) onViewCourierDetail(item.id) }
            )
        )
    }

    DataCard(
        item = item,
        onClick = { if (item.id.isNotBlank()) onViewCourierDetail(item.id) },
//        actions = actions,
        showDivider = false,
        content = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header Row: Courier Name, Active/Inactive Pill & 3-dot Menu
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = item.courierName,
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = title_color
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Status Badge Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isActive) greenBg else redBg)
                                .padding(horizontal = 10.dp, vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isActive) "Active" else "Inactive",
                                fontSize = tokens.label,
                                fontWeight = FontWeight.Medium,
                                color = if (isActive) greentext else redText
                            )
                        }

                        // 3-dot Dropdown Menu
                        ActionDropdownMenu(
                            actions = actions,
                            icon = Icons.Default.MoreVert
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Middle Box: Serviceability & Created Date Columns
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(PanelBg)
                        .border(1.dp, sectionBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Serviceability Column
                    Column {
                        Text(
                            text = "Serviceability",
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Normal,
                            color = headerGrey
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = serviceabilityText,
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = title_color
                        )
                    }

                    // Created Date Column
                    Column {
                        Text(
                            text = "Created Date",
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Normal,
                            color = headerGrey
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = formattedDate,
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = title_color
                        )
                    }
                }
            }
        }
    )
}