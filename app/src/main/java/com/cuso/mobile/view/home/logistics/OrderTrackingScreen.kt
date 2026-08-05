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
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.ScreenBreadcrumb
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.view.home.reusablecomposables.DataCard
import com.cuso.mobile.view.home.reusablecomposables.MenuAction
import com.cuso.mobile.view.home.reusablecomposables.SearchFilterBar

// ── Static dummy row model — swap with your real order/tracking model later ──
data class OrderTrackingItem(
    val id: String,
    val date: String,
    val location: String,
    val updatedBy: String,
    val status: String
)

private val dummyOrders = List(11) { index ->
    OrderTrackingItem(
        id = "order_$index",
        date = "Oct 19 2026",
        location = "Chennai",
        updatedBy = "Admin",
        status = "Processed"
    )
}

@Composable
fun OrderTrackingScreen(
    onClose: () -> Unit = {},
    onViewOrder: (OrderTrackingItem) -> Unit = {},
    onBreadCrumbClick: () -> Unit ={}

) {
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            // ── Top bar: title + close ──
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TitleBar("Order Tracking", onClose = onClose)

            }

            // ── Breadcrumb ──
            ScreenBreadcrumb(listOf("Logistics", "Order Tracking"), onClick = {onBreadCrumbClick()})

            // ── Search + filter row ──
            SearchFilterBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Search Order...",
                onFilterClick = { /* open filter drawer */ },
                modifier = Modifier
                    .background(Color(0xFFF9FAFB))
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            )

            HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 1.dp)

            // ── List of orders, built with the shared DataCard ──
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(dummyOrders) { order ->
                    DataCard(
                        item = order,
                        title = order.date,
                        titleFontSize = 15.sp,
                        titleFontWeight = FontWeight.SemiBold,
                        titleColor = Color(0xFF111827),
                        subtitle = "Location: ${order.location}    Updated By: ${order.updatedBy}",

                        // "Processed" pill — shown inline, next to the title row (matches design)
                        topBadgeText = order.status,
                        topBadgeInline = true,
                        topBadgeTextColor = Color(0xFF4338CA),
                        topBadgeBgColor = Color(0xFFE0E1FB),

                        actions = listOf(
                            MenuAction(
                                label = "View",
                                icon = Icons.Default.Visibility,
                                onClick = { onViewOrder(order) }
                            ),
                            MenuAction(
                                label = "Edit",
                                icon = Icons.Default.Edit,
                                onClick = { }
                            ),
                            MenuAction(
                                label = "Delete",
                                icon = Icons.Default.Delete,
                                tint = Color(0xFFDC2626),
                                textColor = Color(0xFFDC2626),
                                onClick = {  }
                            )
                        )
                    )
                }
            }
        }
    }
}