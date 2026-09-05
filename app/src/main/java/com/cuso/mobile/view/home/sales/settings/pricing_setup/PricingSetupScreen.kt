@file:Suppress("DEPRECATION", "unused", "unusedVariable", "AssignedValueIsNeverRead")

package com.cuso.mobile.view.home.sales.settings.pricing_setup

import android.R
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.model.settings.GarmentStyleItem
import com.cuso.mobile.model.settings.WorkPricingItem
import com.cuso.mobile.ui.theme.*
import com.cuso.mobile.view.composable.*
import com.cuso.mobile.view.home.sales.settings.garment.ToggleSegmentStatusDialog
import com.cuso.mobile.viewmodel.SettingsViewModel

enum class PricingTab {
    GARMENT_PRICING,
    FABRIC_PRICING,
    WORK_PRICING
}

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

@Composable
fun PricingSetupScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
    onAddGarmentPricing: () -> Unit = {},
    onAddFabricPricing: () -> Unit = {},
    onAddWorkPricing: () -> Unit = {},
    onEditGarmentPricing: (String) -> Unit = {},
    onEditWorkPricing: (String) -> Unit = {}
) {
    val tokens = LocalAppTokens.current

    val segments by viewModel.segments.collectAsStateWithLifecycle()
    val garmentStyles by viewModel.garmentStyles.collectAsStateWithLifecycle()
    val workPricingList by viewModel.workPricingList.collectAsStateWithLifecycle()

    val isLoadingStyles by viewModel.isLoadingStyles.collectAsStateWithLifecycle()
    val isLoadingWorkPricing by viewModel.isLoadingWorkPricing.collectAsStateWithLifecycle()

    var selectedMainTab by remember { mutableStateOf(PricingTab.GARMENT_PRICING) }
    var selectedSubTabIndex by remember { mutableIntStateOf(0) }
    val safeSegmentIndex = if (selectedSubTabIndex in segments.indices) selectedSubTabIndex else 0

    LaunchedEffect(Unit) {
        viewModel.fetchSegments()
    }

    LaunchedEffect(selectedMainTab, safeSegmentIndex, segments) {
        if (segments.isNotEmpty()) {
            val segmentId = segments[safeSegmentIndex].id
            when (selectedMainTab) {
                PricingTab.GARMENT_PRICING -> viewModel.fetchGarmentStyles(segmentId = segmentId, garmentId = null)
                PricingTab.WORK_PRICING -> viewModel.fetchWorkPricing(segmentId = segmentId, status = "Active")
                PricingTab.FABRIC_PRICING -> {}
            }
        }
    }

    val fabConfig = remember(selectedMainTab) {
        when (selectedMainTab) {
            PricingTab.GARMENT_PRICING -> null
            PricingTab.FABRIC_PRICING -> FabConfig(label = "Add New", icon = Icons.Default.Add, onClick = onAddFabricPricing)
            PricingTab.WORK_PRICING -> FabConfig(label = "Add New", icon = Icons.Default.Add, onClick = onAddWorkPricing)
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = { TitleBar(title = "Pricing Setup", onClose = onClose) }
    ) { padding ->
        FabScaffold(
            fab = fabConfig,
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            // Root Column MUST fill max size to provide finite constraints to its children
            Column(modifier = Modifier.fillMaxSize()) {

                // Main Category Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = tokens.screenPadding, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PricingTab.entries.forEach { tab ->
                        val isSelected = selectedMainTab == tab
                        val title = tab.name.replace("_", " ").lowercase().split(" ")
                            .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }

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

                // Segment Tabs
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
                            Tab(
                                selected = safeSegmentIndex == index,
                                onClick = { selectedSubTabIndex = index },
                                text = {
                                    Text(
                                        text = segment.name,
                                        fontSize = 13.sp,
                                        fontWeight = if (safeSegmentIndex == index) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (safeSegmentIndex == index) Primary else TextSecondary
                                    )
                                }
                            )
                        }
                    }
                }

                // The weight(1f) here is critical. It tells the Box to take up ONLY
                // the remaining space, providing a finite height to the LazyColumn inside.
                Box(modifier = Modifier.weight(1f)) {
                    when (selectedMainTab) {
                        PricingTab.GARMENT_PRICING -> {
                            GarmentPricingListContent(
                                styles = garmentStyles,
                                isLoading = isLoadingStyles,
                                onEdit = onEditGarmentPricing
                            )
                        }
                        PricingTab.FABRIC_PRICING -> FabricPricingListContent()
                        PricingTab.WORK_PRICING -> {
                            WorkPricingListContent(
                                items = workPricingList,
                                isLoading = isLoadingWorkPricing,
                                onEdit = onEditWorkPricing,
                                onToggleStatus = { item -> viewModel.changeWorkPricingStatus(item) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GarmentPricingListContent(
    styles: List<GarmentStyleItem>,
    isLoading: Boolean,
    onEdit: (String) -> Unit
) {
    val tokens = LocalAppTokens.current
    val groupedStyles = remember(styles) {
        styles.groupBy { it.garment?.displayName ?: it.garment?.name ?: "Other" }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // --- Header Area ---
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = tokens.screenPadding, vertical = 14.dp)) {
            Text(text = "Garment Pricing", fontSize = 16.sp, color = title_color, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(2.dp))
            Text(text = "Set the base making price for each garment style and variant.", fontSize = 12.sp, color = close_color)
        }

        if (isLoading) {
            Box(modifier = Modifier.weight(1f)) {
                ListSkeleton()
            }
        } else if (groupedStyles.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No garment styles found.", color = iconMuted)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // FIX: Use grouped iteration to flatten the list
                groupedStyles.forEach { (garmentName, variants) ->
                    item(key = garmentName) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = tokens.screenPadding, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = garmentName, fontSize = 15.sp, color = title_color, fontWeight = FontWeight.Bold)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { onEdit(garmentName) }
                            ) {
                                Text(text = "Edit Pricing", fontSize = 12.sp, color = Primary, fontWeight = FontWeight.Medium)
                                Spacer(Modifier.width(4.dp))
                                Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = Primary, modifier = Modifier.size(13.dp))
                            }
                        }
                    }

                    items(variants) { style ->
                        val isActive = style.status.equals("Active", ignoreCase = true)
                        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = tokens.screenPadding)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = style.displayName ?: style.name, fontSize = 13.sp, color = iconMuted)
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(text = "₹${style.stitchingCharge.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = title_color)
                                    if (isActive) {
                                        Box(
                                            modifier = Modifier.background(Color(0xFFE6F7ED), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(text = "ACTIVE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                                        }
                                    }
                                }
                            }
                            HorizontalDivider(color = title_border, thickness = 0.8.dp)
                        }
                    }

                    item { Spacer(Modifier.height(12.dp)) }
                }
            }
        }
    }
}

@Composable
private fun FabricPricingListContent() {
    val tokens = LocalAppTokens.current
    val fabricItems = remember {
        listOf(
            FabricPriceItem("Premium Cotton", "Cotton", "White", "FAB-001", "Meter", "₹450", null, true),
            FabricPriceItem("Linen Premium", "Linen", "Blue", "FAB-002", "Meter", "₹650", "₹700 ↗", true)
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // --- Header Area ---
        Column(modifier = Modifier.padding(horizontal = tokens.screenPadding, vertical = 14.dp)) {
            Text(text = "Fabric Pricing", fontSize = 16.sp, color = title_color, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(2.dp))
            Text(text = "Set the selling price of each fabric per meter.", fontSize = 12.sp, color = close_color)
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(fabricItems) { item ->
                Column(modifier = Modifier.padding(horizontal = tokens.screenPadding, vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = item.name, fontSize = 15.sp, color = title_color)
                        Box(
                            modifier = Modifier
                                .background(if (item.isActive) Color(0xFFE6F7ED) else Color(0xFFF1F5F9), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (item.isActive) "ACTIVE" else "INACTIVE",
                                fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (item.isActive) Color(0xFF10B981) else Color(0xFF64748B)
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider(color = title_border, thickness = 0.8.dp)
                }
            }
        }
    }
}

@Composable
private fun WorkPricingListContent(
    items: List<WorkPricingItem>,
    isLoading: Boolean,
    onEdit: (String) -> Unit,
    onToggleStatus: (WorkPricingItem) -> Unit
) {
    val tokens = LocalAppTokens.current

    // State to manage the confirmation dialog
    var itemToToggle by remember { mutableStateOf<WorkPricingItem?>(null) }

    // Dialog logic
    itemToToggle?.let { item ->
        val isActive = item.status.equals("Active", ignoreCase = true)
        ToggleSegmentStatusDialog(
            isActivating = !isActive, // If currently active, we are deactivating (isActivating = false)
            segmentName = item.workType,
            entityLabel = "Work Pricing",
            onDismiss = { itemToToggle = null },
            onConfirm = {
                onToggleStatus(item)
                itemToToggle = null
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = tokens.screenPadding, vertical = 14.dp)) {
            Text(text = "Work Pricing", fontSize = 16.sp, color = title_color, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(2.dp))
            Text(text = "Manage base prices for different types of workmanship.", fontSize = 12.sp, color = close_color)
        }

        if (isLoading) {
            Box(modifier = Modifier.weight(1f)) { ListSkeleton() }
        } else if (items.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(text = "No work pricing found.", color = iconMuted, fontSize = 14.sp)
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f), contentPadding = PaddingValues(bottom = 100.dp)) {
                items(items) { item ->
                    val isActive = item.status.equals("Active", ignoreCase = true)
                    var menuExpanded by remember { mutableStateOf(false) }

                    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = tokens.screenPadding, vertical = 8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = item.workType, fontSize = 15.sp, color = title_color, fontWeight = FontWeight.Medium)

                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                // Status Badge
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (isActive) Color(0xFFE6F7ED) else Color(0xFFFEF2F2),
                                            RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = item.status.uppercase(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isActive) Color(0xFF10B981) else Color(0xFFEF4444)
                                    )
                                }

                                // 3-Dot Action Menu
                                Box {
                                    IconButton(onClick = { menuExpanded = true }, modifier = Modifier.size(24.dp)) {
                                        Icon(Icons.Default.MoreVert, null, tint = iconMuted)
                                    }
                                    DropdownMenu(
                                        expanded = menuExpanded,
                                        onDismissRequest = { menuExpanded = false },
                                        modifier = Modifier.background(whiteBg)
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Edit Pricing", color = title_color, fontSize = 14.sp) },
                                            leadingIcon = { Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp)) },
                                            onClick = {
                                                menuExpanded = false
                                                onEdit(item.id)
                                            }
                                        )

                                        // Inactivate / Activate Button
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = if (isActive) "Inactivate" else "Activate",
                                                    color = if (isActive) Color.Red else Color(0xFF10B981),
                                                    fontSize = 14.sp
                                                )
                                            },
                                            onClick = {
                                                menuExpanded = false
                                                // Instead of calling API directly, show dialog first
                                                itemToToggle = item
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Segment: ${item.segmentId?.name ?: "N/A"}", fontSize = 12.sp, color = close_color)
                            Text(text = "₹${item.basePrice.toInt()}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = title_color)
                        }
                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider(color = title_border, thickness = 0.8.dp)
                    }
                }
            }
        }
    }
}