package com.cuso.mobile.view.home.services

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.mobile.ui.theme.modelGray

import com.cuso.mobile.view.composable.ScreenBreadcrumb
import com.cuso.mobile.view.home.reusablecomposables.DataCard
import com.cuso.mobile.view.home.reusablecomposables.DataCardField
import com.cuso.mobile.view.home.reusablecomposables.FabConfig
import com.cuso.mobile.view.home.reusablecomposables.FabScaffold
import com.cuso.mobile.view.home.reusablecomposables.SearchFilterBar

// ─────────────────────────────────────────────
// Data model (static/dummy)
// ─────────────────────────────────────────────
enum class AlterationStatus { PENDING, IN_PROGRESS, COMPLETED }

data class AlterationItem(
    val customerName: String,
    val orderId: String,
    val garmentType: String,
    val alterationType: String,
    val assignedTailor: String,
    val dueDate: String,
    val status: AlterationStatus
)

val dummyAlterationItems = List(4) {
    AlterationItem(
        customerName = "Rahul Mehta",
        orderId = "ALT-2201",
        garmentType = "Blazer",
        alterationType = "Sleeve Shortening",
        assignedTailor = "Suresh Kumar",
        dueDate = "Aug 2, 2026",
        status = AlterationStatus.PENDING
    )
}

// ─────────────────────────────────────────────
// AlterationCard — same DataCard pattern as LowStockAlertCard
// ─────────────────────────────────────────────
@Composable
fun AlterationCard(
    item: AlterationItem,
    onViewClick: () -> Unit
) {
    val (badgeText, badgeBg, badgeText2Color) = when (item.status) {
        AlterationStatus.PENDING -> Triple("Pending", Color(0xFFFDE7E7), Color(0xFFE53935))
        AlterationStatus.IN_PROGRESS -> Triple("In Progress", Color(0xFFFFF4DE), Color(0xFFF59E0B))
        AlterationStatus.COMPLETED -> Triple("Completed", Color(0xFFE7F8EE), Color(0xFF16A34A))
    }

    DataCard(
        item = item,
        title = item.customerName,
        subtitle = "Order: ${item.orderId} · ${item.garmentType}",
        topBadgeText = badgeText,
        topBadgeTextColor = badgeText2Color,
        topBadgeBgColor = badgeBg,
        topBadgeInline = true,
        footerAsRows = true,
        footerFields = listOf(
            DataCardField(
                label = "Alteration",
                text = item.alterationType
            ),
            DataCardField(
                label = "Tailor",
                text = item.assignedTailor
            ),
            DataCardField(
                label = "Due Date",
                text = item.dueDate
            )
        ),
        content = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = onViewClick,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF3D3DFF))
                ) {
                    Text("View →", fontSize = 13.sp)
                }
            }
        }
    )
}

// ─────────────────────────────────────────────
// AlterationManagementScreen — full screen
// ─────────────────────────────────────────────
@Composable
fun AlterationManagementScreen(
    onClose: () -> Unit,
    onViewClick: (AlterationItem) -> Unit,
    onCreateNewAlteration: () -> Unit,
    onBreadcrumbClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text("Alteration Management", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = onClose) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }
            }
        }
    ) { padding ->
        FabScaffold(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            fab = FabConfig(
                label = "New Alteration",
                icon = Icons.Default.Add,
                onClick = onCreateNewAlteration,
                endPadding = 16.dp,
                bottomPadding = 16.dp,
                draggable = true
            )
        ) {
            Column(modifier = Modifier.fillMaxSize().background(modelGray)) {
                ScreenBreadcrumb(
                    segments = listOf("Services", "Alteration Management"),
                    onClick = onBreadcrumbClick
                )
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    SearchFilterBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        placeholder = "Search Alterations...",
                        onFilterClick = { /* TODO: handle filter click */ },
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                    )
                }
                LazyColumn {
                    items(dummyAlterationItems) { item ->
                        AlterationCard(
                            item = item,
                            onViewClick = { onViewClick(item) }
                        )
                    }
                    item { Spacer(Modifier.height(80.dp)) } // FAB clearance
                }
            }
        }
    }
}