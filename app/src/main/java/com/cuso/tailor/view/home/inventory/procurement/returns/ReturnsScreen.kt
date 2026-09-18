@file:Suppress("unused", "SameParameterValue")

package com.cuso.tailor.view.home.inventory.procurement.returns

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.tailor.adaptive_screen.AppDesignTokens
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*

// ─────────────────────────────────────────────────────────────
// DATA MODELS
// ─────────────────────────────────────────────────────────────

data class ReturnItemModel(
    val id: String,
    val rmaCode: String = "RMA-00124",
    val orderNo: String = "648770",
    val customerName: String = "Hameed",
    val date: String = "01-10-2023",
    val returned: String = "No",
    val approvalStatus: String = "Approved",
    val receiveStatus: String = "In Transit",
    val refundStatus: String = "In Transit"
)

// ─────────────────────────────────────────────────────────────
// SCREEN COMPOSABLE
// ─────────────────────────────────────────────────────────────

@Composable
fun AllReturnsScreen(
    onClose: () -> Unit,
    onEditRma: () -> Unit = {},
    onPdfExport: () -> Unit = {},
    onReturnItemClick: (ReturnItemModel) -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedItemIds by remember { mutableStateOf(setOf<String>()) }

    // Mock Sample Data
    val returnItems = remember {
        listOf(
            ReturnItemModel(id = "1"),
            ReturnItemModel(id = "2"),
            ReturnItemModel(id = "3"),
            ReturnItemModel(id = "4"),
            ReturnItemModel(id = "5")
        )
    }

    // Pre-select first item as shown in the mockup
    LaunchedEffect(Unit) {
        selectedItemIds = setOf("1")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary_background)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                TitleBar(
                    title = "All Returns",
                    onClose = onClose
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Search and Filter Header
                SearchFilterBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search Customers...",
                    showFilterIcon = true,
                    onFilterClick = { },
                    height = tokens.fieldHeight * 1.1f
                )

                HorizontalDivider(color = grey_border.copy(alpha = 0.6f), thickness = 1.dp)

                // RMA Main Group Header (RMA-1001)
                RmaGroupHeaderSection(
                    rmaGroupCode = "RMA-1001",
                    status = "Approved",
                    raisedDate = "12 Feb 2026",
                    department = "Production",
                    tokens = tokens,
                    onEditClick = onEditRma,
                    onPdfClick = onPdfExport,
                    onMoreClick = { }
                )

                HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 1.dp)

                // Returns List
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(returnItems, key = { _, item -> item.id }) { _, item ->
                        val isChecked = selectedItemIds.contains(item.id)
                        ReturnCardItem(
                            item = item,
                            isChecked = isChecked,
                            tokens = tokens,
                            onCheckedChange = { checked ->
                                selectedItemIds = if (checked) {
                                    selectedItemIds + item.id
                                } else {
                                    selectedItemIds - item.id
                                }
                            },
                            onClick = { onReturnItemClick(item) }
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// RMA GROUP HEADER
// ─────────────────────────────────────────────────────────────

@Composable
private fun RmaGroupHeaderSection(
    rmaGroupCode: String,
    status: String,
    raisedDate: String,
    department: String,
    tokens: AppDesignTokens,
    onEditClick: () -> Unit,
    onPdfClick: () -> Unit,
    onMoreClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(whiteBg)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = rmaGroupCode,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = title_color
            )

            Spacer(Modifier.width(8.dp))

            StatusDotBadge(text = status)

            Spacer(Modifier.weight(1f))

            // Action: Edit
            OutlinedActionButton(
                label = "Edit",
                icon = Icons.Default.Edit,
                onClick = onEditClick
            )

            Spacer(Modifier.width(8.dp))

            // Action: PDF
            OutlinedActionButton(
                label = "PDF",
                onClick = onPdfClick
            )

            Spacer(Modifier.width(4.dp))

            IconButton(
                onClick = onMoreClick,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Options",
                    tint = mutedText,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Subtext: Raised on date | Department
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(color = mutedText)) {
                    append("Raised on ")
                }
                withStyle(style = SpanStyle(color = title_color, fontWeight = FontWeight.SemiBold)) {
                    append(raisedDate)
                }
                withStyle(style = SpanStyle(color = grey_border)) {
                    append("   |   ")
                }
                withStyle(style = SpanStyle(color = mutedText)) {
                    append("Department: ")
                }
                withStyle(style = SpanStyle(color = title_color, fontWeight = FontWeight.SemiBold)) {
                    append(department)
                }
            },
            fontSize = 12.sp
        )
    }
}

// ─────────────────────────────────────────────────────────────
// RETURN CARD ITEM
// ─────────────────────────────────────────────────────────────

@Composable
private fun ReturnCardItem(
    item: ReturnItemModel,
    isChecked: Boolean,
    tokens: AppDesignTokens,
    onCheckedChange: (Boolean) -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Header Row: Checkbox, Code, Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppCheckbox(
                    checked = isChecked,
                    onCheckedChange = onCheckedChange
                )

                Spacer(Modifier.width(10.dp))

                Text(
                    text = item.rmaCode,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = title_color
                )

                Spacer(Modifier.weight(1f))

                StatusDotBadge(text = item.approvalStatus)
            }

            Spacer(Modifier.height(14.dp))

            // Details Grid
            Row(modifier = Modifier.fillMaxWidth()) {
                // Column 1
                Column(modifier = Modifier.weight(1f)) {
                    LabelValueItem(label = "ORDER NO", value = item.orderNo)
                    Spacer(Modifier.height(10.dp))
                    LabelValueItem(label = "DATE", value = item.date)
                }

                // Column 2
                Column(modifier = Modifier.weight(1f)) {
                    LabelValueItem(label = "CUSTOMER NAME", value = item.customerName)
                    Spacer(Modifier.height(10.dp))
                    LabelValueItem(label = "RETURNED", value = item.returned)
                }
            }

            Spacer(Modifier.height(14.dp))

            // Bottom Status Strip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFFAFAFA))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Receive Status
                    Column {
                        Text(
                            text = "Receive Status",
                            fontSize = 10.sp,
                            color = mutedText
                        )
                        Spacer(Modifier.height(4.dp))
                        TransitBadge(
                            text = item.receiveStatus,
                            textColor = Color(0xFFD97706),
                            bgColor = Color(0xFFFEF3C7),
                            borderColor = Color(0xFFFDE68A)
                        )
                    }

                    // Refund Status
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Refund Status",
                            fontSize = 10.sp,
                            color = mutedText
                        )
                        Spacer(Modifier.height(4.dp))
                        TransitBadge(
                            text = item.refundStatus,
                            textColor = Color(0xFF2563EB),
                            bgColor = Color(0xFFEFF6FF),
                            borderColor = Color(0xFFDBEAFE)
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// REUSABLE MINI COMPONENTS
// ─────────────────────────────────────────────────────────────

@Composable
private fun LabelValueItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = mutedText,
            letterSpacing = 0.5.sp
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            color = title_color
        )
    }
}

@Composable
private fun StatusDotBadge(
    text: String,
    bgColor: Color = Color(0xFFE8F8F0),
    textColor: Color = Color(0xFF10B981),
    dotColor: Color = Color(0xFF10B981)
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(RoundedCornerShape(50))
                    .background(dotColor)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = text,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
        }
    }
}

@Composable
private fun TransitBadge(
    text: String,
    textColor: Color,
    bgColor: Color,
    borderColor: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .border(width = 0.8.dp, color = borderColor, shape = RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

@Composable
private fun OutlinedActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .border(width = 1.dp, color = grey_border, shape = RoundedCornerShape(4.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = title_color,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(Modifier.width(4.dp))
            }
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = title_color
            )
        }
    }
}