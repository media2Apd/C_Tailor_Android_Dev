@file:Suppress("unused")

package com.cuso.tailor.view.home.finance.account_payable.suppliers


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.cuso.tailor.ui.theme.grey_border
import com.cuso.tailor.ui.theme.light_grey
import com.cuso.tailor.ui.theme.redText
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.view.composable.DataCard
import com.cuso.tailor.view.composable.DataCardField
import com.cuso.tailor.view.composable.MenuAction
import com.cuso.tailor.view.composable.SearchFilterBar

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



        // ── Search + filter row ──

        SearchFilterBar(
            onQueryChange = {searchQuery = it},
            query = searchQuery
        )
        HorizontalDivider(color = grey_border)

        // ── List, using shared DataCard ──
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(dummySuppliers) { supplier ->
                DataCard(
                    item = supplier,
                    title = supplier.name,
                    subtitle = "Code: ${supplier.code}  •  ${supplier.phone}",
                    topBadgeText = if (supplier.isActive) "Active" else "Inactive",
                    topBadgeTextColor = if (supplier.isActive) Color(0xFF16A34A) else Color(0xFF6B7280),
                    topBadgeBgColor = if (supplier.isActive) Color(0xFFDCFCE7) else light_grey,
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
                        MenuAction(label = "Delete", textColor = redText, onClick = { })
                    ),
                    onClick = { onSupplierClick(it) }
                )
            }
        }
    }
}