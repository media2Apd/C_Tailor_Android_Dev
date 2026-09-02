@file:Suppress("DEPRECATION","unused","unusedVariable","AssignedValueIsNeverRead")

package com.cuso.mobile.view.home.sales.settings.pricing_setup

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.*
import com.cuso.mobile.view.composable.FabConfig
import com.cuso.mobile.view.composable.FabScaffold
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.viewmodel.SettingsViewModel

enum class PricingTab {
    GARMENT_PRICING,
    LABOUR_PRICING,
    WORK_PRICING
}

data class GarmentVariantPrice(
    val name: String,
    val price: String,
    val isActive: Boolean
)

data class GarmentStylePricingGroup(
    val garmentName: String,
    val variants: List<GarmentVariantPrice>
)

data class FabricPriceItem(
    val name: String,
    val material: String,
    val color: String,
    val sku: String,
    val unit: String,
    val currentPrice: String,
    val newPrice: String? = null,
    val isActive: Boolean
)

data class WorkPriceItem(
    val title: String,
    val garmentDetails: String,
    val price: String,
    val isActive: Boolean
)

@Composable
fun PricingSetupScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
    onAddGarmentPricing: () -> Unit = {},
    onAddFabricPricing: () -> Unit = {},
    onAddWorkPricing: () -> Unit = {},
    onEditGarmentPricing: (String) -> Unit = {}
) {
    val tokens = LocalAppTokens.current

    val segments by viewModel.segments.collectAsState()
//    val isLoadingSegments by viewModel.isLoadingSegments.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchSegments()
    }

    var selectedMainTab by remember { mutableStateOf(PricingTab.GARMENT_PRICING) }
    var selectedSubTabIndex by remember { mutableIntStateOf(0) }
    val safeSegmentIndex = if (selectedSubTabIndex in segments.indices) selectedSubTabIndex else 0

    val fabConfig = remember(selectedMainTab) {
        when (selectedMainTab) {
            PricingTab.GARMENT_PRICING -> FabConfig(
                label = "Add New",
                icon = Icons.Default.Add,
                onClick = onAddGarmentPricing
            )
            PricingTab.LABOUR_PRICING -> FabConfig(
                label = "Add New",
                icon = Icons.Default.Add,
                onClick = onAddFabricPricing
            )
            PricingTab.WORK_PRICING -> FabConfig(
                label = "Add New",
                icon = Icons.Default.Add,
                onClick = onAddWorkPricing
            )
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TitleBar(
                title = "Pricing Setup",
                onClose = onClose
            )
        }
    ) { padding ->
        FabScaffold(
            fab = fabConfig,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // 1. Top Main Pill Tabs (Garment / Labour / Work)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = tokens.screenPadding, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        PricingTab.GARMENT_PRICING to "Garment Pricing",
                        PricingTab.LABOUR_PRICING to "Labour Pricing",
                        PricingTab.WORK_PRICING to "Work Pricing"
                    ).forEach { (tab, title) ->
                        val isSelected = selectedMainTab == tab
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) primary_light else whiteBg,
                            border = BorderStroke(1.dp, if (isSelected) Primary else sectionBorder),
                            modifier = Modifier.clickable { selectedMainTab = tab }
                        ) {
                            Text(
                                text = title,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) Primary else TextSecondary,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                // 2. Dynamic Segment Sub-Tabs (From API)
                if (segments.isNotEmpty()) {
                    ScrollableTabRow(
                        selectedTabIndex = safeSegmentIndex,
                        edgePadding = tokens.screenPadding,
                        containerColor = whiteBg,
                        divider = { HorizontalDivider(color = title_border, thickness = 1.dp) },
                        indicator = { tabPositions ->
                            if (safeSegmentIndex < tabPositions.size) {
                                TabRowDefaults.SecondaryIndicator(
                                    Modifier.tabIndicatorOffset(tabPositions[safeSegmentIndex]),
                                    color = Primary,
                                    height = 2.5.dp
                                )
                            }
                        }
                    ) {
                        segments.forEachIndexed { index, segment ->
                            val isSelected = safeSegmentIndex == index
                            Tab(
                                selected = isSelected,
                                onClick = { selectedSubTabIndex = index },
                                text = {
                                    Text(
                                        text = segment.name,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (isSelected) Primary else TextSecondary
                                    )
                                }
                            )
                        }
                    }
                }

                // 3. Tab Content Display
                when (selectedMainTab) {
                    PricingTab.GARMENT_PRICING -> {
                        GarmentPricingListContent(
                            onEdit = onEditGarmentPricing
                        )
                    }
                    PricingTab.LABOUR_PRICING -> {
                        FabricPricingListContent()
                    }
                    PricingTab.WORK_PRICING -> {
                        WorkPricingListContent()
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// 1. Garment Pricing List Content
// ─────────────────────────────────────────────────────────────
@Composable
private fun GarmentPricingListContent(
    onEdit: (String) -> Unit
) {
    val tokens = LocalAppTokens.current

    val garmentGroups = remember {
        listOf(
            GarmentStylePricingGroup(
                garmentName = "Men's Shirt",
                variants = listOf(
                    GarmentVariantPrice("General", "—", false),
                    GarmentVariantPrice("Half Sleeve", "₹1,200", true),
                    GarmentVariantPrice("Full Sleeve", "₹1,500", true)
                )
            ),
            GarmentStylePricingGroup(
                garmentName = "Trouser",
                variants = listOf(
                    GarmentVariantPrice("General", "—", false),
                    GarmentVariantPrice("Regular", "₹800", true),
                    GarmentVariantPrice("Formal", "₹1,050", true)
                )
            ),
            GarmentStylePricingGroup(
                garmentName = "Kurta",
                variants = listOf(
                    GarmentVariantPrice("General", "—", false),
                    GarmentVariantPrice("Regular", "₹900", false),
                    GarmentVariantPrice("Designer", "₹1,400", true)
                )
            ),
            GarmentStylePricingGroup(
                garmentName = "Blazer",
                variants = listOf(
                    GarmentVariantPrice("General", "—", false),
                    GarmentVariantPrice("Single Breasted", "₹2,500", true),
                    GarmentVariantPrice("Double Breasted", "₹3,000", true)
                )
            )
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = tokens.screenPadding, vertical = 14.dp)
            ) {
                Text(
                    text = "Garment Styles & Variants",
                    fontSize = 16.sp,
                    color = title_color
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Set the base making price for each garment style and variant.",
                    fontSize = 12.sp,
                    color = close_color
                )
            }
        }

        items(garmentGroups) { group ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = tokens.screenPadding, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = group.garmentName,
                        fontSize = 15.sp,
                        color = title_color
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onEdit(group.garmentName) }
                    ) {
                        Text(
                            text = "Edit Pricing",
                            fontSize = 12.sp,
                            color = Primary,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Primary,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }

                Spacer(Modifier.height(6.dp))

                group.variants.forEach { variant ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = variant.name,
                            fontSize = 13.sp,
                            color = iconMuted
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = variant.price,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = title_color
                            )
                            if (variant.isActive) {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFE6F7ED), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "ACTIVE",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF10B981)
                                    )
                                }
                            } else if (variant.price != "—") {
                                Text("—", fontSize = 13.sp, color = iconMuted)
                            }
                        }
                    }
                    HorizontalDivider(color = title_border, thickness = 0.8.dp)
                }
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// 2. Fabric / Labour Pricing List Content
// ─────────────────────────────────────────────────────────────
@Composable
private fun FabricPricingListContent() {
    val tokens = LocalAppTokens.current

    val fabricItems = remember {
        listOf(
            FabricPriceItem("Premium Cotton", "Cotton", "White", "FAB-001", "Meter", "₹450", null, true),
            FabricPriceItem("Linen Premium", "Linen", "Blue", "FAB-002", "Meter", "₹650", "₹700 ↗", true),
            FabricPriceItem("Silk", "Silk", "Maroon", "FAB-003", "Meter", "₹1,200", null, true),
            FabricPriceItem("Georgette", "Synthetic", "Pink", "FAB-004", "Meter", "₹680", null, true),
            FabricPriceItem("Velvet", "Luxury", "Red", "FAB-005", "Meter", "₹1,500", null, false)
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = tokens.screenPadding, vertical = 14.dp)
            ) {
                Text(
                    text = "Fabric Pricing",
                    fontSize = 16.sp,
                    color = title_color
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Set the selling price of each fabric per meter.",
                    fontSize = 12.sp,
                    color = close_color
                )
            }
        }

        items(fabricItems) { item ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = tokens.screenPadding, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.name,
                        fontSize = 15.sp,
                        color = title_color
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    if (item.isActive) Color(0xFFE6F7ED) else Color(0xFFF1F5F9),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (item.isActive) "ACTIVE" else "INACTIVE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (item.isActive) Color(0xFF10B981) else Color(0xFF64748B)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = iconMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${item.material} • ${item.color} • ${item.sku}",
                            fontSize = 12.sp,
                            color = close_color
                        )
                        Text(
                            text = "Unit: ${item.unit}",
                            fontSize = 11.sp,
                            color = iconMuted
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Current: ${item.currentPrice}",
                            fontSize = 13.sp,
                            color = title_color
                        )
                        Text(
                            text = if (item.newPrice != null) "New: ${item.newPrice}" else "No scheduled changes",
                            fontSize = 11.sp,
                            color = if (item.newPrice != null) Primary else iconMuted,
                            fontWeight = if (item.newPrice != null) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))
                HorizontalDivider(color = title_border, thickness = 0.8.dp)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// 3. Work Pricing List Content
// ─────────────────────────────────────────────────────────────
@Composable
private fun WorkPricingListContent() {
    val tokens = LocalAppTokens.current

    val workItems = remember {
        listOf(
            WorkPriceItem("Aari Work", "Men's Shirt · Half Sleeve", "₹600", true),
            WorkPriceItem("Aari Work", "Women's Blouse · Full Sleeve", "₹1000", true),
            WorkPriceItem("Aari Work", "Men's Shirt · Half Sleeve", "₹600", true),
            WorkPriceItem("Embroidery", "Men's Shirt · Half Sleeve", "₹600", true),
            WorkPriceItem("Stone Work", "Women's Blouse · Full Sleeve", "₹400", true),
            WorkPriceItem("Embroidery", "Lehenga · Standard", "₹600", true)
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = tokens.screenPadding, vertical = 14.dp)
            ) {
                Text(
                    text = "Pricing Setup",
                    fontSize = 16.sp,
                    color = title_color
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Configure garment, fabric and work pricing used to calculate the selling price.",
                    fontSize = 12.sp,
                    color = close_color
                )
            }
        }

        items(workItems) { item ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = tokens.screenPadding, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.title,
                        fontSize = 15.sp,
                        color = title_color
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFE6F7ED), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 0.dp)
                        ) {
                            Text(
                                text = "ACTIVE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = iconMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.garmentDetails,
                        fontSize = 12.sp,
                        color = close_color
                    )
                    Text(
                        text = item.price,
                        fontSize = 15.sp,
                        color = title_color
                    )
                }

                Spacer(Modifier.height(10.dp))
                HorizontalDivider(color = title_border, thickness = 0.8.dp)
            }
        }
    }
}