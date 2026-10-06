@file:Suppress("unused")

package com.cuso.tailor.view.home.logistics.delivery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.DataCard
import com.cuso.tailor.view.composable.DataCardField
import com.cuso.tailor.view.composable.MenuAction
import com.cuso.tailor.view.composable.SearchFilterBar
import com.cuso.tailor.view.composable.TitleBar

// Delivery data model
private data class DeliveryStatic(
    val id: String,
    val recipientName: String,
    val deliveryId: String,
    val customer: String,
    val deliveryLocation: String,
    val deliveryType: String,
    val date: String,
    val status: String
)

@Composable
fun DeliveryManagementScreen(
    onDismiss: () -> Unit = {},
    onView: (String) -> Unit = {},
    onEdit: (String) -> Unit = {},
    onDelete: (String) -> Unit = {},
    onBreadCrumbClick: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }

    // Delivery sample items
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
            .background(Color.Transparent)
    ) {
        // Screen top title bar
        TitleBar(
            title = "Delivery Management",
            onClose = onDismiss
        )

        // Search and filter container
        SearchFilterBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            placeholder = "Search Delivery...",
            onFilterClick = { /* Handle filter */ }
        )

        // Delivery records list using adaptive tokens
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(deliveries, key = { it.id }) { delivery ->
                val isInTransit = delivery.status == "In Transit"

                DataCard(
                    item = delivery,
                    // Title and delivery ID displayed together in the top row
                    title = if (delivery.deliveryId.isNotBlank()) "${delivery.recipientName}  #${delivery.deliveryId}" else delivery.recipientName,
                    titleColor = title_color,
                    titleFontWeight = FontWeight.SemiBold,
                    topBadgeText = delivery.status,
                    // Colors mapped from project theme
                    topBadgeTextColor = if (isInTransit) Primary else greentext,
                    topBadgeBgColor = if (isInTransit) activity_purple_bg else greenBg,
                    topBadgeShowDot = false,
                    // Places Title, StatusBadge, and Actions on the same 1st row
                    topBadgeInline = true,
                    showActionsInHeader = false,
                    showHeaderDivider = true,
                    footerAsRows = true,
                    footerFields = listOf(
                        DataCardField(
                            label = "Customer",
                            text = delivery.customer,
                            labelColor = mutedText,
                            textColor = TextLog,
                            valueFontWeight = FontWeight.Medium
                        ),
                        DataCardField(
                            label = delivery.deliveryType,
                            text = delivery.deliveryLocation,
                            labelColor = mutedText,
                            textColor = TextLog,
                            valueFontWeight = FontWeight.Medium
                        ),
                        DataCardField(
                            label = "Delivery Date",
                            text = delivery.date,
                            labelColor = mutedText,
                            textColor = TextLog,
                            valueFontWeight = FontWeight.Medium
                        )
                    ),
                    actions = listOf(
                        MenuAction(
                            label = "View",
                            tint = mutedText,
                            textColor = TextPrimary,
                            onClick = { onView(delivery.id) }
                        ),
                        MenuAction(
                            label = "Edit",
                            tint = mutedText,
                            textColor = TextPrimary,
                            onClick = { onEdit(delivery.id) }
                        ),
                        MenuAction(
                            label = "Delete",
                            tint = redText,
                            textColor = redText,
                            onClick = { onDelete(delivery.id) }
                        )
                    ),
                    onClick = { onView(delivery.id) }
                )
            }
        }
    }
}