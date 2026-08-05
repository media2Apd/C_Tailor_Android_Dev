package com.cuso.mobile.view.home.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.cuso.mobile.view.home.reusablecomposables.DataCardField
import com.cuso.mobile.view.home.reusablecomposables.FabConfig
import com.cuso.mobile.view.home.reusablecomposables.FabScaffold
import com.cuso.mobile.view.home.reusablecomposables.MenuAction
import com.cuso.mobile.view.home.reusablecomposables.SearchFilterBar

// ── Design tokens ──
private val AccentColor = Color(0xFF4F39F6)
private val TitleColor = Color(0xFF111827)
private val LabelColor = Color(0xFF6B7280)
private val MutedColor = Color(0xFF9CA3AF)
private val BorderColor = Color(0xFFE3E4E8)
private val CountBadgeBg = Color(0xFFEDE9FE)
private val CountBadgeText = Color(0xFF6D28D9)

// ── Static sample data model ──
private data class ItemGroupStatic(
    val id: String,
    val name: String,
    val attributes: String,     // "Color, Size"
    val itemCount: Int,
    val date: String
)

@Composable
fun AllItemGroupScreen(
    onDismiss: () -> Unit = {},
    onAddItemGroup: () -> Unit = {},
    onView: (String) -> Unit = {},
    onEdit: (String) -> Unit = {},
    onDelete: (String) -> Unit = {},
    onBreadCrumbClick: () -> Unit ={}

) {
    var searchQuery by remember { mutableStateOf("") }

    // ── Static sample list (matches image 1) ──
    val itemGroups = remember {
        List(8) { index ->
            ItemGroupStatic(
                id = "ig_$index",
                name = "Cotton Twil",
                attributes = "Color, Size",
                itemCount = 3,
                date = "20 Jul 2026"
            )
        }
    }

    FabScaffold(
        modifier = Modifier.fillMaxSize(),
        fab = FabConfig(
            label = "Create item group",
            icon = Icons.Default.Add,
            onClick = onAddItemGroup
        )
    ) {
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
                TitleBar("Create customer", onClose = onDismiss)

            }

            // ── Breadcrumb ──
            ScreenBreadcrumb(listOf("Inventory", "All Item Group"), onClick = {onBreadCrumbClick()})

            // ── Search + Filter ──
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
                SearchFilterBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search Item Group...",
                    accentColor = AccentColor,
                    borderColor = BorderColor,
                    textSecondaryColor = MutedColor,
                    onFilterClick = { /* static — no-op */ }
                )
            }

            HorizontalDivider(color = BorderColor)

            // ── Item Group list (reusing shared DataCard) ──
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 8.dp, horizontal = 0.dp)
            ) {
                items(itemGroups, key = { it.id }) { group ->
                    DataCard(
                        item = group,
                        title = group.name,
                        titleFontSize = 15.sp,
                        subtitle = group.attributes,
                        topBadgeText = "${group.itemCount} Items",
                        topBadgeTextColor = CountBadgeText,
                        topBadgeBgColor = CountBadgeBg,
                        topBadgeInline = true,
                        footerAsRows = false,
                        footerFields = listOf(
                            DataCardField(
                                label = "",
                                text = group.date,
                                labelColor = MutedColor,
                                textColor = MutedColor
                            )
                        ),
                        actions = listOf(
                            MenuAction("View", Icons.Filled.Visibility, onClick = { onView(group.id) }),
                            MenuAction("Edit", Icons.Filled.Edit, onClick = { onEdit(group.id) }),
                            MenuAction(
                                "Delete", Icons.Filled.Delete,
                                tint = Color(0xFFDC2626), textColor = Color(0xFFDC2626),
                                onClick = { onDelete(group.id) }
                            )
                        ),
                        onClick = { onView(group.id) }
                    )
                }
            }
        }
    }
}