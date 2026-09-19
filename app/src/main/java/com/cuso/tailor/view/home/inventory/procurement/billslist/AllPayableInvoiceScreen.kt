package com.cuso.tailor.view.home.inventory.procurement.billslist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cuso.tailor.adaptive_screen.AppDesignTokens
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*

data class PayableInvoiceItemModel(
    val id: String,
    val billNo: String = "12345#",
    val vendorName: String = "Sri Lakshmi Textiles",
    val status: String = "Completed",
    val date: String = "06 Feb 2026",
    val dueDate: String = "28 Jul 2026",
    val balanceDueDate: String = "Feb 1, 2026",
    val totalAmount: String = "₹12,500"
)

@Composable
fun AllPayableInvoicesScreen(
    onClose: () -> Unit,
    onCreateOrder: () -> Unit = {},
    onInvoiceClick: (PayableInvoiceItemModel) -> Unit = {},
    onOptionsClick: (PayableInvoiceItemModel) -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedItemIds by remember { mutableStateOf(setOf<String>()) }

    val invoiceList = remember {
        listOf(
            PayableInvoiceItemModel(id = "1"),
            PayableInvoiceItemModel(id = "2"),
            PayableInvoiceItemModel(id = "3"),
            PayableInvoiceItemModel(id = "4"),
            PayableInvoiceItemModel(id = "5")
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary_background)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                Column(modifier = Modifier.fillMaxWidth().background(whiteBg)) {
                    TitleBar("All Invoice", onClose)
                    HorizontalDivider(color = title_border)
                }
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = onCreateOrder,
                    containerColor = Primary,
                    contentColor = whiteBg,
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                    modifier = Modifier.height(tokens.buttonHeight)
                ) {
                    Text(
                        text = "Create Order",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = whiteBg
                    )
                    Spacer(Modifier.width(tokens.extraPadding * 0.4f))
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create",
                        tint = whiteBg,
                        modifier = Modifier.size(tokens.iconSize)
                    )
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Search & Filter
                SearchFilterBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search Customers...",
                    showFilterIcon = true,
                    onFilterClick = { },
                    height = tokens.fieldHeight * 1.1f
                )

                HorizontalDivider(color = grey_border.copy(alpha = 0.5f), thickness = 2.dp)

                // Invoices List
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = tokens.extraPadding * 0.6f),
                    verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f)
                ) {
                    itemsIndexed(invoiceList, key = { _, item -> item.id }) { _, item ->
                        val isChecked = selectedItemIds.contains(item.id)
                        PayableInvoiceCardItem(
                            item = item,
                            isChecked = isChecked,
                            tokens = tokens,
                            onCheckedChange = { checked ->
                                selectedItemIds = if (checked) selectedItemIds + item.id else selectedItemIds - item.id
                            },
                            onClick = { onInvoiceClick(item) },
                            onOptionsClick = { onOptionsClick(item) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PayableInvoiceCardItem(
    item: PayableInvoiceItemModel,
    isChecked: Boolean,
    tokens: AppDesignTokens,
    onCheckedChange: (Boolean) -> Unit,
    onClick: () -> Unit,
    onOptionsClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding * 1.2f)
        ) {
            // Row 1: Checkbox + Bill No Badge + Code + Status Badge + Options
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppCheckbox(checked = isChecked, onCheckedChange = onCheckedChange)
                Spacer(Modifier.width(tokens.extraPadding * 0.8f))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.35f))
                        .background(activity_purple_bg)
                        .padding(horizontal = tokens.extraPadding * 0.8f, vertical = 3.dp)
                ) {
                    Text("BILL NO", fontSize = tokens.label, fontWeight = FontWeight.Bold, color = activity_purple)
                }

                Spacer(Modifier.width(tokens.extraPadding * 0.8f))
                Text(item.billNo, fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = title_color)

                Spacer(Modifier.weight(1f))

                // Completed Status Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 2f))
                        .background(greenBg)
                        .padding(horizontal = tokens.extraPadding * 0.8f, vertical = 3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(tokens.iconSize * 0.3f)
                                .clip(CircleShape)
                                .background(darkGreenBg)
                        )
                        Spacer(Modifier.width(tokens.extraPadding * 0.4f))
                        Text(item.status, fontSize = tokens.label, fontWeight = FontWeight.Medium, color = darkGreenBg)
                    }
                }

                Spacer(Modifier.width(tokens.extraPadding * 0.4f))

                IconButton(onClick = onOptionsClick, modifier = Modifier.size(tokens.iconSize * 1.3f)) {
                    Icon(Icons.Default.MoreVert, contentDescription = null, tint = mutedText, modifier = Modifier.size(tokens.iconSize))
                }
            }

            Spacer(Modifier.height(tokens.extraPadding * 0.8f))

            // Row 2: Vendor Name
            Text(item.vendorName, fontSize = tokens.bodyMedium, fontWeight = FontWeight.Bold, color = title_color)

            Spacer(Modifier.height(tokens.extraPadding * 1.2f))

            // Row 3: Grid (Date, Due Date, Balance Due)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Date", fontSize = tokens.label, color = mutedText)
                    Spacer(Modifier.height(2.dp))
                    Text(item.date, fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = title_color)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Due Date", fontSize = tokens.label, color = mutedText)
                    Spacer(Modifier.height(2.dp))
                    Text(item.dueDate, fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = title_color)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Balance Due", fontSize = tokens.label, color = mutedText)
                    Spacer(Modifier.height(2.dp))
                    Text(item.balanceDueDate, fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = title_color)
                }
            }

            Spacer(Modifier.height(tokens.extraPadding * 1.2f))
            HorizontalDivider(color = grey_border.copy(alpha = 0.5f), thickness = 0.8.dp)
            Spacer(Modifier.height(tokens.extraPadding * 1.2f))

            // Row 4: Total Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Total Amount", fontSize = tokens.caption, color = mutedText)
                Text(item.totalAmount, fontSize = tokens.bodyLarge, fontWeight = FontWeight.Bold, color = Primary)
            }
        }
    }
}