package com.cuso.mobile.view.home.inventory

// ─────────────────────────────────────────────
// Imports
// ─────────────────────────────────────────────
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.DataCard
import com.cuso.mobile.view.composable.DataCardField
import com.cuso.mobile.view.composable.DataCardProgressBar

// ── Your existing reusables ──
import com.cuso.mobile.view.composable.ScreenBreadcrumb
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.view.composable.FabConfig
import com.cuso.mobile.view.composable.FabScaffold
import com.cuso.mobile.view.composable.SearchFilterBar

// ─────────────────────────────────────────────
// Data model (static/dummy)
// ─────────────────────────────────────────────
// Data model for a low-stock item
data class LowStockItem(
    val name: String,
    val sku: String,
    val variant: String,
    val warehouse: String,
    val availableQty: String,
    val utilizationPercent: Float, // 0f..1f, used for the red progress bar fill
    val reorderLevel: String
)
val dummyLowStockItems = List(4) {
    LowStockItem(
        name = "Linen Shirt Fabric",
        sku = "60756",
        variant = "Blue",
        warehouse = "Factory",
        availableQty = "12 m",
        reorderLevel = "100 m",
        utilizationPercent = 12f
    )
}

// ─────────────────────────────────────────────
// Reusable gauge (shared with Create Purchase Order screen too)
// ─────────────────────────────────────────────

// ── Simple horizontal gauge/progress bar: red fill on a light gray track ──
@Composable
fun StockUtilizationGauge(percentage: Float) {
    val clamped = percentage.coerceIn(0f, 1f)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .background(Color(0xFFEDEDF2), RoundedCornerShape(50)) // light gray track
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(clamped)
                .background(Color(0xFFE53935), RoundedCornerShape(50)) // red fill
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
    // Outer white rounded + elevated container — gives the card its
    // shadow and rounded corners, matching the design image.
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        DataCard(
            item = item,
            // showDivider = false so DataCard's own bottom divider doesn't
            // cut across the rounded corners of our outer Card above.
            showDivider = false,

            // ── Title + subtitle ──
            title = item.name,
            subtitle = "SKU: ${item.sku} · Variant: ${item.variant}",

            // ── "Critical" pill badge, top-right, inline with the title ──
            topBadgeText = "Critical",
            topBadgeTextColor = Color(0xFFE53935),
            topBadgeBgColor = Color(0xFFFDE7E7),
            // Dot color matched to the badge background so the dot blends
            // in and effectively disappears — image shows a plain pill,
            // no visible dot.
            topBadgeDotColor = Color(0xFFFDE7E7),
            topBadgeInline = true,

            // ── Warehouse / Available rows ──
            footerAsRows = true,
            footerFields = listOf(
                DataCardField(
                    label = "Warehouse",
                    text = item.warehouse,
                    valueFontWeight = FontWeight.SemiBold
                ),
                DataCardField(
                    label = "Available",
                    text = item.availableQty,
                    textColor = Color(0xFFE53935),
                    valueFontWeight = FontWeight.SemiBold
                )
            ),

            // ── Gauge + Reorder row, reusing DataCardProgressBar ──
            content = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    DataCardProgressBar(
                        progress = item.utilizationPercent,
                        progressColor = Color(0xFFE53935),
                        trackColor = Color(0xFFEDEDF2)
                    )
                    Spacer(Modifier.height(14.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Reorder Level: ${item.reorderLevel}",
                            fontSize = 12.sp,
                            color = Color(0xFF9B9BA5)
                        )
                        OutlinedButton(
                            onClick = onReorderClick,
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF3D3DFF)),
                            border = BorderStroke(1.dp, Color(0xFF3D3DFF)),
                            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp)
                        ) {
                            Text("Reorder →", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        )
    }
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
                color = whiteBg
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        TitleBar("All Orders", onClose = onClose)

                    }
                }
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = Color.Transparent
        //  floatingActionButton slot removed — FabScaffold handles it now
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
            Column(modifier = Modifier.fillMaxSize().background(Color.Transparent)) {
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
                        onFilterClick = {  },
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