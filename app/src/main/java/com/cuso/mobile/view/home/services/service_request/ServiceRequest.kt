package com.cuso.mobile.view.home.services.service_request


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.cuso.mobile.ui.theme.Primary
import com.cuso.mobile.ui.theme.whiteBg

import com.cuso.mobile.view.composable.ScreenBreadcrumb
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.view.composable.DataCard
import com.cuso.mobile.view.composable.DataCardField
import com.cuso.mobile.view.composable.FabConfig
import com.cuso.mobile.view.composable.FabScaffold
import com.cuso.mobile.view.composable.MenuAction
import com.cuso.mobile.view.composable.SearchFilterBar

// ─────────────────────────────────────────────
// Model + Status enum
// (previously had TWO enums — ServiceStatus and AlterationStatus —
//  mixed together by mistake. Kept just one here.)
// ─────────────────────────────────────────────

enum class ServiceStatus { PENDING, IN_PROGRESS, COMPLETED }
enum class Priority { HIGH, MEDIUM, LOW }

data class ServicesItem(
    val requestNo: String,        // e.g. "SR-1025"  -> card title
    val orderId: String,          // e.g. "ORD-4401" -> eyebrow text
    val customerName: String,
    val garmentType: String,
    val alterationType: String,
    val assignedTailor: String,
    val dueDate: String,
    val createdDate: String,      // e.g. "28 Feb 2026" -> shown in subtitle
    val priority: Priority,
    val status: ServiceStatus
)

val dummyServiceItems = List(4) {
    ServicesItem(
        requestNo = "SR-1025",
        orderId = "ORD-4401",
        customerName = "Amit Verma",
        garmentType = "Linen shirt",
        alterationType = "Alteration",
        assignedTailor = "Suresh Kumar",
        dueDate = "Aug 2, 2026",
        createdDate = "28 Feb 2026",
        priority = Priority.HIGH,
        status = ServiceStatus.IN_PROGRESS
    )
}

// ─────────────────────────────────────────────
// ServiceRequestCard — matches "All Request" design
// ─────────────────────────────────────────────
@Composable
fun ServiceRequestCard(
    item: ServicesItem,
    onViewClick: () -> Unit,
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {}
) {
    val (statusBadgeText, statusBadgeBg, statusBadgeTextColor) = when (item.status) {
        ServiceStatus.PENDING -> Triple("Pending", Color(0xFFFDE7E7), Color(0xFFE53935))
        ServiceStatus.IN_PROGRESS -> Triple("In Progress", Color(0xFFEDEBFF), Primary)
        ServiceStatus.COMPLETED -> Triple("Completed", Color(0xFFE7F8EE), Color(0xFF16A34A))
    }

    val (priorityText, priorityBg, priorityTextColor) = when (item.priority) {
        Priority.HIGH -> Triple("High", Color(0xFFFDE7E7), Color(0xFFE53935))
        Priority.MEDIUM -> Triple("Medium", Color(0xFFFFF4DE), Color(0xFFF59E0B))
        Priority.LOW -> Triple("Low", Color(0xFFE7F8EE), Color(0xFF16A34A))
    }

    DataCard(
        item = item,
        eyebrowText = "Order ID: ${item.orderId}",
        title = item.requestNo,
        subtitle = "Type: ${item.alterationType} · Date: ${item.createdDate}",
        topBadgeText = statusBadgeText,
        topBadgeTextColor = statusBadgeTextColor,
        topBadgeBgColor = statusBadgeBg,
        topBadgeInline = true,
        footerAsRows = true,
        footerFields = listOf(
            DataCardField(
                label = "Customer name",
                text = item.customerName
            ),
            DataCardField(
                label = "Garment name",
                text = item.garmentType
            ),
            DataCardField(
                label = "Priority",
                text = priorityText,
                valueBadge = true,
                valueBadgeBgColor = priorityBg,
                valueBadgeTextColor = priorityTextColor
            )
        ),
        actions = listOf(
            MenuAction(label = "View", onClick = onViewClick),
            MenuAction(label = "Edit", onClick = onEditClick),
            MenuAction(label = "Delete", textColor = Color(0xFFE53935), onClick = onDeleteClick)
        ),
        content = null // "View →" button removed — not present in this design
    )
}

// ─────────────────────────────────────────────
// ServiceRequestScreen — full screen ("All Request")
// ─────────────────────────────────────────────

@Composable
fun ServiceRequestScreen(
    onClose: () -> Unit,
    onViewClick: (ServicesItem) -> Unit,
    onCreateNewRequest: () -> Unit,
    onBreadcrumbClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = whiteBg
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        TitleBar("All Request", onClose = onClose)

                    }
                }
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = Color.Transparent
    ) { padding ->
        FabScaffold(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            fab = FabConfig(
                label = "Create Request",
                icon = Icons.Default.Add,
                onClick = onCreateNewRequest,
                endPadding = 16.dp,
                bottomPadding = 16.dp,
                draggable = true
            )
        ) {
            Column(modifier = Modifier.fillMaxSize().background(Color.Transparent)) {
                ScreenBreadcrumb(
                    segments = listOf("Services", "Service Request"),
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
                        placeholder = "Search Request...",
                        onFilterClick = {  }
                    )
                }
                LazyColumn {
                    items(dummyServiceItems) { item ->
                        ServiceRequestCard(
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