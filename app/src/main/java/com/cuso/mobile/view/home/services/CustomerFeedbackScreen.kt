package com.cuso.mobile.view.home.services

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.mobile.view.composable.ScreenBreadcrumb
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.view.composable.DataCard
import com.cuso.mobile.view.composable.DataCardField
import com.cuso.mobile.view.composable.MenuAction
import com.cuso.mobile.view.composable.SearchFilterBar

// ── Design tokens ──
private val AccentColor = Color(0xFF4F39F6)
private val TitleColor = Color(0xFF111827)
private val MutedColor = Color(0xFF9CA3AF)
private val BorderColor = Color(0xFFE3E4E8)
private val CategoryBg = Color(0xFFEDE9FE)
private val CategoryText = Color(0xFF6D28D9)

// ── Static sample data model (matches screenshot fields) ──
private data class FeedbackStatic(
    val id: String,
    val feedbackId: String,
    val organizationName: String,
    val category: String,
    val orderId: String,
    val date: String
)

@Composable
fun CustomerFeedbackScreen(
    onDismiss: () -> Unit = {},
    onView: (String) -> Unit = {},
    onEdit: (String) -> Unit = {},
    onDelete: (String) -> Unit = {},
    onBreadCrumbClick: () -> Unit ={}

) {
    var searchQuery by remember { mutableStateOf("") }

    // ── Static sample list (matches image 2) ──
    val feedbackList = remember {
        List(6) { index ->
            FeedbackStatic(
                id = "fb_$index",
                feedbackId = "FB-1024",
                organizationName = "Meena Textiles",
                category = "Customer Service",
                orderId = "ORD-8821",
                date = "28 Feb 2026"
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        // ── Header ──
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TitleBar("Lead Management", onClose= onDismiss)
        }

        // ── Breadcrumb ──
        ScreenBreadcrumb(listOf("Services", "Customer Feedback"), onClick = {onBreadCrumbClick()})

        // ── Search + Filter ──
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
            SearchFilterBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Search Feedback...",
                accentColor = AccentColor,
                borderColor = BorderColor,
                textSecondaryColor = MutedColor,
                onFilterClick = { /* static — no-op */ }
            )
        }

        HorizontalDivider(color = BorderColor)

        // ── Feedback list (reusing shared DataCard) ──
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(feedbackList, key = { it.id }) { feedback ->
                DataCard(
                    item = feedback,
                    title = "${feedback.feedbackId} • Feedback ID",
                    subtitle = feedback.organizationName,
                    topBadgeText = feedback.category,
                    topBadgeTextColor = CategoryText,
                    topBadgeBgColor = CategoryBg,
                    topBadgeInline = true,
                    footerAsRows = true,
                    footerFields = listOf(
                        DataCardField(
                            label = "Order ID",
                            text = feedback.orderId,
                            labelColor = MutedColor,
                            textColor = TitleColor
                        ),
                        DataCardField(
                            label = "Date",
                            text = feedback.date,
                            labelColor = MutedColor,
                            textColor = TitleColor
                        )
                    ),
                    actions = listOf(
                        MenuAction("View", Icons.Filled.Visibility, onClick = { onView(feedback.id) }),
                        MenuAction("Edit", Icons.Filled.Edit, onClick = { onEdit(feedback.id) }),
                        MenuAction(
                            "Delete", Icons.Filled.Delete,
                            tint = Color(0xFFDC2626), textColor = Color(0xFFDC2626),
                            onClick = { onDelete(feedback.id) }
                        )
                    ),
                    onClick = { onView(feedback.id) }
                )
            }
        }
    }
}