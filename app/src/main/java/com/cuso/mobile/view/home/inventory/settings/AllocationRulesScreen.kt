package com.cuso.mobile.view.home.inventory.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.*
import com.cuso.mobile.view.composable.*

// Data Models
data class AllocationRuleItem(
    val id: String,
    val title: String,
    val type: String,
    val status: String,
    val allocatedChannels: String,
    val totalAllocation: Int,
    val hasWarning: Boolean = false
)

data class ChannelAllocationEntry(
    val channelNumber: String,
    var channel: String = "Meesho",
    var allocationType: String = "Percentage",
    var value: String = "50%"
)

// -------------------------------------------------------------
// Screen 1: Allocation Rules Overview List
// -------------------------------------------------------------
@Composable
fun AllocationRulesScreen(
    onClose: () -> Unit,
    onAddNewClick: () -> Unit
) {
    val tokens = LocalAppTokens.current
    var searchQuery by remember { mutableStateOf("") }

    val rules = remember {
        listOf(
            AllocationRuleItem("1", "Holiday Campaign Split", "Percentage", "ACTIVE", "Meesho, Flipkart, Amazon", 100),
            AllocationRuleItem("2", "Q3 Revenue Allocation", "Equal", "ACTIVE", "Shopify, Amazon, Wholesale", 100),
            AllocationRuleItem("3", "Regional Marketing Budget", "Percentage", "DRAFT", "Google, Facebook", 85, true),
            AllocationRuleItem("4", "Product Launch Distribution", "Weighted", "ACTIVE", "Direct, Retail, Affiliate", 100),
            AllocationRuleItem("5", "Customer Acquisition Split", "Percentage", "AT RISK", "TikTok, Instagram, YouTube", 95, true),
            AllocationRuleItem("6", "Brand Awareness Fund", "Equal", "PAUSED", "Offline, Digital", 100),
            AllocationRuleItem("7", "Fulfillment Priority Split", "Fixed", "ACTIVE", "B2B, B2C, Express", 100)
        )
    }

    FabScaffold(
        modifier = Modifier.fillMaxSize().background(Color.Transparent),
        fab = FabConfig(
            label = "Add Rule",
            icon = Icons.Default.Add,
            onClick = onAddNewClick
        )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TitleBar(title = "Allocation Rules", onClose = onClose)

            SearchFilterBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Search Customers...",
                showFilterIcon = true,
                onFilterClick = {}
            )

            // Header banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = tokens.screenPadding, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Garment Styles & Variants",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF111827)
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Configure inventory, pricing rules, and priority channels across e-commerce stores.",
                        fontSize = tokens.caption,
                        color = Color(0xFF6B7280)
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(rules) { item ->
                    AllocationRuleCard(item = item)
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                }
            }
        }
    }
}

@Composable
fun AllocationRuleCard(item: AllocationRuleItem) {
    val tokens = LocalAppTokens.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = tokens.screenPadding, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.title,
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF111827)
            )
            Icon(
                imageVector = Icons.Default.MoreHoriz,
                contentDescription = null,
                tint = Color(0xFF9CA3AF),
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val (typeBg, typeColor) = when (item.type) {
                "Percentage" -> Color(0xFFEFF6FF) to Color(0xFF3B82F6)
                "Equal" -> Color(0xFFF0FDF4) to Color(0xFF16A34A)
                "Weighted" -> Color(0xFFFAF5FF) to Color(0xFF9333EA)
                else -> Color(0xFFFEF2F2) to Color(0xFFDC2626)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(typeBg)
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            ) {
                Text(item.type, fontSize = tokens.caption, color = typeColor, fontWeight = FontWeight.Medium)
            }

            val (statusBg, statusColor) = when (item.status) {
                "ACTIVE" -> Color(0xFFDCFCE7) to Color(0xFF16A34A)
                "DRAFT" -> Color(0xFFF1F5F9) to Color(0xFF64748B)
                "AT RISK" -> Color(0xFFFEE2E2) to Color(0xFFDC2626)
                else -> Color(0xFFFEF3C7) to Color(0xFFD97706)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(statusBg)
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            ) {
                Text(item.status, fontSize = tokens.caption, color = statusColor, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text("ALLOCATED CHANNELS", fontSize = 10.sp, color = Color(0xFF9CA3AF), fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(3.dp))
                Text(item.allocatedChannels, fontSize = tokens.bodySmall, color = Color(0xFF4B5563))
            }

            Column(horizontalAlignment = Alignment.End) {
                Text("TOTAL ALLOC.", fontSize = 10.sp, color = Color(0xFF9CA3AF), fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (item.hasWarning) {
                        Icon(
                            imageVector = Icons.Default.WarningAmber,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(Modifier.width(3.dp))
                    }
                    Text(
                        text = "${item.totalAllocation}%",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (item.hasWarning) Color(0xFFD97706) else Primary
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Screen 2: Create Allocation Form
// -------------------------------------------------------------
@Composable
fun CreateAllocationScreen(
    onClose: () -> Unit,
    onSaveRule: () -> Unit
) {
    val tokens = LocalAppTokens.current
    var ruleName by remember { mutableStateOf("") }
    var selectedRuleType by remember { mutableStateOf("Percentage") }

    val channelOptions = listOf("Meesho", "Flipkart", "Amazon", "Shopify")
    val allocationTypeOptions = listOf("Percentage", "Fixed", "Equal")

    val channelEntries = remember {
        mutableStateListOf(
            ChannelAllocationEntry("01", "Meesho", "Percentage", "50%"),
            ChannelAllocationEntry("02", "Meesho", "Percentage", "30%"),
            ChannelAllocationEntry("03", "Meesho", "Percentage", "20%")
        )
    }

    val channelDropdownExpanded = remember { mutableStateMapOf<Int, Boolean>() }
    val allocDropdownExpanded = remember { mutableStateMapOf<Int, Boolean>() }

    Box(modifier = Modifier.fillMaxSize().background(Color.Transparent)) {
        Column(modifier = Modifier.fillMaxSize()) {
            TitleBar(title = "Create Allocation", onClose = onClose)

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                item {
                    Text("General Information", fontSize = tokens.bodyMedium, fontWeight = FontWeight.SemiBold, color = Color(0xFF111827))
                    Spacer(Modifier.height(10.dp))

                    FormLabel(text = "Rule Name", isRequired = false)
                    FormTextField(
                        value = ruleName,
                        onValueChange = { ruleName = it },
                        placeholder = "Bedroom Combo Set"
                    )

                    Spacer(Modifier.height(10.dp))

                    FormLabel(text = "Rule Type", isRequired = false)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(tokens.fieldHeight)
                            .background(Color(0xFFF8FAFC), RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                            .border(1.dp, grey_border, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("Percentage", "Equal", "Fixed").forEach { type ->
                            val isSelected = selectedRuleType == type
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
                                    .background(if (isSelected) Color(0xFFEEF2FF) else Color.Transparent)
                                    .clickable { selectedRuleType = type },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = type,
                                    fontSize = tokens.bodySmall,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isSelected) Primary else Color(0xFF64748B)
                                )
                            }
                        }
                    }
                }

                item {
                    Text("Channel Allocation", fontSize = tokens.bodyMedium, fontWeight = FontWeight.SemiBold, color = Color(0xFF111827))
                }

                items(channelEntries.size) { index ->
                    val entry = channelEntries[index]
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Primary))
                                Spacer(Modifier.width(6.dp))
                                Text("Channel ${entry.channelNumber}", fontSize = tokens.bodyMedium, fontWeight = FontWeight.SemiBold, color = Color(0xFF111827))
                            }
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFFEE2E2))
                                    .clickable { channelEntries.removeAt(index) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        FormDropdown(
                            label = "Channel",
                            value = entry.channel,
                            expanded = channelDropdownExpanded[index] ?: false,
                            onExpandChange = { channelDropdownExpanded[index] = it },
                            options = channelOptions,
                            onOptionSelected = { entry.channel = it }
                        )

                        Spacer(Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                FormDropdown(
                                    label = "Allocation Type",
                                    value = entry.allocationType,
                                    expanded = allocDropdownExpanded[index] ?: false,
                                    onExpandChange = { allocDropdownExpanded[index] = it },
                                    options = allocationTypeOptions,
                                    onOptionSelected = { entry.allocationType = it }
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                Column {
                                    FormLabel(text = "Value")
                                    FormTextField(
                                        value = entry.value,
                                        onValueChange = { entry.value = it },
                                        placeholder = "50%"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom Action using StepNavigationFab
        StepNavigationFab(
            showBack = true,
            backLabel = "Cancel",
            onBack = onClose,
            showBackArrow = false,
            showTrailingArrow = false,
            trailingAction = TrailingFabAction.Update(
                label = "Save Rule",
                onClick = onSaveRule
            ),
            backWidthFraction = 0.35f,
            trailingWidthFraction = 0.55f
        )
    }
}