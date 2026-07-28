package com.cuso.mobile.view.home.inventory

// ─────────────────────────────────────────────
// Imports
// ─────────────────────────────────────────────
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.mobile.ui.theme.modelGray

// ── Your existing reusables ──
import com.cuso.mobile.view.composable.ScreenBreadcrumb
import com.cuso.mobile.view.home.reusablecomposables.DataCard
import com.cuso.mobile.view.home.reusablecomposables.DataCardField
import com.cuso.mobile.view.home.reusablecomposables.FabConfig
import com.cuso.mobile.view.home.reusablecomposables.FabScaffold
import com.cuso.mobile.view.home.reusablecomposables.SearchFilterBar

// ─────────────────────────────────────────────
// Data model (static/dummy)
// ─────────────────────────────────────────────
data class LowStockItem(
    val name: String,
    val sku: String,
    val variant: String,
    val warehouse: String,
    val availableQty: String,
    val reorderLevel: String,
    val utilizationPercent: Int
)

val dummyLowStockItems = List(4) {
    LowStockItem(
        name = "Linen Shirt Fabric",
        sku = "60756",
        variant = "Blue",
        warehouse = "Factory",
        availableQty = "12 m",
        reorderLevel = "100 m",
        utilizationPercent = 12
    )
}

// ─────────────────────────────────────────────
// Reusable gauge (shared with Create Purchase Order screen too)
// ─────────────────────────────────────────────
@Composable
fun StockUtilizationGauge(
    percentage: Int,
    modifier: Modifier = Modifier,
    barColor: Color = Color(0xFFE53935)
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(Color(0xFFEDEDED))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction = (percentage / 100f).coerceIn(0f, 1f))
                .fillMaxHeight()
                .clip(RoundedCornerShape(3.dp))
                .background(barColor)
        )
    }
}

// ─────────────────────────────────────────────
// LowStockAlertCard — DataCard for the info block,
// gauge + reorder row attached right below it
// ─────────────────────────────────────────────
@Composable
fun LowStockAlertCard(
    item: LowStockItem,
    onReorderClick: () -> Unit
) {
    DataCard(
        item = item,
        title = item.name,
        subtitle = "SKU: ${item.sku} · Variant: ${item.variant}",
        topBadgeText = "Critical",
        topBadgeTextColor = Color(0xFFE53935),
        topBadgeBgColor = Color(0xFFFDE7E7),
        topBadgeInline = true,
        footerAsRows = true,
        footerFields = listOf(
            DataCardField(
                label = "Warehouse",
                text = item.warehouse
            ),
            DataCardField(
                label = "Available",
                text = item.availableQty,
                textColor = Color(0xFFE53935)
            )
        ),
        content = {
            Column(modifier = Modifier.fillMaxWidth()) {
                StockUtilizationGauge(percentage = item.utilizationPercent)
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Reorder Level: ${item.reorderLevel}",
                        fontSize = 12.sp,
                        color = Color(0xFF8A8A99)
                    )
                    OutlinedButton(
                        onClick = onReorderClick,
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF3D3DFF))
                    ) {
                        Text("Reorder →", fontSize = 13.sp)
                    }
                }
            }
        }
    )
}
// ─────────────────────────────────────────────
// LowStockAlertsScreen — full screen
// ─────────────────────────────────────────────
@Composable
fun LowStockAlertsScreen(
    onClose: () -> Unit,
    onReorderClick: (LowStockItem) -> Unit,
    onCreateNewItem: () -> Unit,
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
                        Text("All Orders", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = onClose) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }
            }
        }
        // ❌ floatingActionButton slot removed — FabScaffold handles it now
    ) { padding ->
        FabScaffold(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            fab = FabConfig(
                label = "Create New Item",
                icon = Icons.Default.Add,
                onClick = onCreateNewItem,
                endPadding = 16.dp,
                bottomPadding = 16.dp,
                draggable = true
            )
        ) {
            Column(modifier = Modifier.fillMaxSize().background(modelGray)) {
                ScreenBreadcrumb(
                    segments = listOf("Inventory", "Alerts & Reorder", "Low Stock Alerts"),
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
                        placeholder = "Search Stock Items...",
                        onFilterClick = { /* TODO: handle filter click */ },
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                    )
                }
                LazyColumn {
                    items(dummyLowStockItems) { item ->
                        LowStockAlertCard(
                            item = item,
                            onReorderClick = { onReorderClick(item) }
                        )
                    }
                    item { Spacer(Modifier.height(80.dp)) } // FAB clearance
                }
            }
        }
    }
}