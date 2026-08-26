package com.cuso.mobile.view.home.finance


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
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

// ── Static dummy model, matches the screenshot's row data ──
 data class SupplierRow(
    val name: String,
    val code: String,
    val phone: String,
    val city: String,
    val outstanding: String,
    val lastBill: String,
    val isActive: Boolean
)

private val dummySuppliers = List(6) {
    SupplierRow(
        name = "Raji",
        code = "001",
        phone = "8778239060",
        city = "Chennai",
        outstanding = "₹10,000",
        lastBill = "₹450",
        isActive = true
    )
}

@Composable
 fun AllSuppliersScreen(
    onClose: () -> Unit = {},
    onBreadCrumbClick: () -> Unit = {},
    onSupplierClick: (SupplierRow) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        // ── Top bar ──
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TitleBar("All Suppliers", onClose = onClose)

        }

        // ── Breadcrumb ──
        ScreenBreadcrumb(
            segments = listOf("Finance", "All Suppliers"),
            onClick = onBreadCrumbClick
        )

        // ── Search + filter row ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .background(Color(0xFFF3F4F6), RoundedCornerShape(10.dp))
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                BasicTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, color = Color(0xFF374151)),
                    decorationBox = { inner ->
                        if (searchQuery.isEmpty()) {
                            Text("Search Customers...", fontSize = 14.sp, color = Color(0xFF9CA3AF))
                        }
                        inner()
                    }
                )
            }
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(Color(0xFFF3F4F6), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = Color(0xFF374151), modifier = Modifier.size(18.dp))
            }
        }

        // ── List, using shared DataCard ──
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(dummySuppliers) { supplier ->
                DataCard(
                    item = supplier,
                    title = supplier.name,
                    subtitle = "Code: ${supplier.code}  •  ${supplier.phone}",
                    topBadgeText = if (supplier.isActive) "Active" else "Inactive",
                    topBadgeTextColor = if (supplier.isActive) Color(0xFF16A34A) else Color(0xFF6B7280),
                    topBadgeBgColor = if (supplier.isActive) Color(0xFFDCFCE7) else Color(0xFFF3F4F6),
                    topBadgeInline = true,
                    footerFields = listOf(
                        DataCardField(
                            text = "${supplier.city}  •  ${supplier.outstanding}",
                            textColor = Color(0xFF6B7280)
                        ),
                        DataCardField(
                            text = "Last Bill: ${supplier.lastBill}",
                            textColor = Color(0xFF9CA3AF)
                        )
                    ),
                    actions = listOf(
                        MenuAction(label = "View", onClick = { onSupplierClick(supplier) }),
                        MenuAction(label = "Edit", onClick = { }),
                        MenuAction(label = "Delete", textColor = Color(0xFFEF4444), onClick = { })
                    ),
                    onClick = { onSupplierClick(it) }
                )
            }
        }
    }
}