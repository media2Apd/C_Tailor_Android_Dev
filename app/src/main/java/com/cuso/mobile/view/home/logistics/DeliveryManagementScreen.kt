package com.cuso.mobile.view.home.logistics

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.mobile.view.composable.ScreenBreadcrumb
import com.cuso.mobile.view.home.reusablecomposables.DataCard
import com.cuso.mobile.view.home.reusablecomposables.DataCardField
import com.cuso.mobile.view.home.reusablecomposables.MenuAction
import com.cuso.mobile.view.home.reusablecomposables.SearchFilterBar

// ── Design tokens ──
private val AccentColor = Color(0xFF4F39F6)
private val TitleColor = Color(0xFF111827)
private val LabelColor = Color(0xFF6B7280)
private val MutedColor = Color(0xFF9CA3AF)
private val BorderColor = Color(0xFFE3E4E8)

private val InTransitBg = Color(0xFFEDE9FE)
private val InTransitText = Color(0xFF6D28D9)
private val ReadyBg = Color(0xFFDCFCE7)
private val ReadyText = Color(0xFF16A34A)

// ── Static sample data model ──
private data class DeliveryStatic(
    val id: String,
    val recipientName: String,
    val deliveryId: String,
    val customer: String,
    val deliveryLocation: String,
    val deliveryType: String,
    val date: String,
    val status: String   // "In Transit" | "Ready"
)

@Composable
fun DeliveryManagementScreen(
    onDismiss: () -> Unit = {},
    onView: (String) -> Unit = {},
    onEdit: (String) -> Unit = {},
    onDelete: (String) -> Unit = {},

) {
    var searchQuery by remember { mutableStateOf("") }

    // ── Static sample list (matches image 1) ──
    val deliveries = remember {
        listOf(
            DeliveryStatic("d0", "Raji", "001", "8778239060", "Chennai", "Delivery Location", "25 Feb 2026", "In Transit"),
            DeliveryStatic("d1", "Raji", "001", "8778239060", "Chennai", "Delivery Type", "25 Feb 2026", "Ready"),
            DeliveryStatic("d2", "Raji", "001", "8778239060", "Chennai", "Delivery Type", "25 Feb 2026", "Ready"),
            DeliveryStatic("d3", "Raji", "001", "8778239060", "Chennai", "Delivery Type", "25 Feb 2026", "Ready")
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // ── Header ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Delivery management", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TitleColor)
            Icon(
                Icons.Filled.Close,
                contentDescription = "Close",
                tint = LabelColor,
                modifier = Modifier.clickable(onClick = onDismiss)
            )
        }

        // ── Breadcrumb ──
        ScreenBreadcrumb(listOf("Logistics", "Delivery Management"), onClick = {})

        // ── Search + Filter ──
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
            SearchFilterBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Search Delivery...",
                accentColor = AccentColor,
                borderColor = BorderColor,
                textSecondaryColor = MutedColor,
                onFilterClick = { /* static — no-op */ }
            )
        }

        HorizontalDivider(color = BorderColor)

        // ── Delivery list (reusing shared DataCard) ──
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(deliveries, key = { it.id }) { delivery ->
                val isInTransit = delivery.status == "In Transit"
                DataCard(
                    item = delivery,
                    title = "${delivery.recipientName} • Delivery Id",
                    titleFontSize = 14.sp,
                    subtitle = delivery.deliveryId,
                    topBadgeText = delivery.status,
                    topBadgeTextColor = if (isInTransit) InTransitText else ReadyText,
                    topBadgeBgColor = if (isInTransit) InTransitBg else ReadyBg,
                    topBadgeInline = false,
                    footerAsRows = true,
                    footerFields = listOf(
                        DataCardField(
                            label = "Customer",
                            text = delivery.customer,
                            labelColor = MutedColor,
                            textColor = TitleColor
                        ),
                        DataCardField(
                            label = delivery.deliveryType,
                            text = delivery.deliveryLocation,
                            labelColor = MutedColor,
                            textColor = TitleColor
                        ),
                        DataCardField(
                            label = "Delivery Date",
                            text = delivery.date,
                            labelColor = MutedColor,
                            textColor = TitleColor
                        )
                    ),
                    actions = listOf(
                        MenuAction("View", Icons.Filled.Visibility, onClick = { onView(delivery.id) }),
                        MenuAction("Edit", Icons.Filled.Edit, onClick = { onEdit(delivery.id) }),
                        MenuAction(
                            "Delete", Icons.Filled.Delete,
                            tint = Color(0xFFDC2626), textColor = Color(0xFFDC2626),
                            onClick = { onDelete(delivery.id) }
                        )
                    ),
                    onClick = { onView(delivery.id) }
                )
            }
        }
    }
}