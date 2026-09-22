@file:Suppress("unused", "SpellCheckingInspection")

package com.cuso.tailor.view.home.inventory.billing

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import androidx.compose.ui.unit.sp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.AppCheckbox
import com.cuso.tailor.view.composable.FabConfig
import com.cuso.tailor.view.composable.FabScaffold
import com.cuso.tailor.view.composable.SearchFilterBar
import com.cuso.tailor.view.composable.TitleBar

data class BillItem(
    val id: String,
    val initial: String,
    val vendorName: String,
    val invoiceNumber: String,
    val date: String,
    val status: String,
    val approvalStatus: String,
    val tag: String,
    val amount: Double,
    val isSelected: Boolean = false
)

@Composable
fun AllBillsScreen(
    onClose: () -> Unit = {},
    onCreateBillClick: () -> Unit = {},
    onBillDetailsClick: (String) -> Unit = {},
    onFilterClick: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    var searchQuery by remember { mutableStateOf("") }

    var billsList by remember {
        mutableStateOf(
            listOf(
                BillItem("1", "DH", "Dhana", "INV-2023-001", "01-10-2023", "Completed", "Approved", "BULK", 120.0),
                BillItem("2", "DH", "Dhana", "INV-2023-001", "01-10-2023", "Completed", "Approved", "BULK", 120.0),
                BillItem("3", "DH", "Dhana", "INV-2023-001", "01-10-2023", "Completed", "Approved", "BULK", 120.0),
                BillItem("4", "DH", "Dhana", "INV-2023-001", "01-10-2023", "Completed", "Approved", "BULK", 120.0),
                BillItem("5", "DH", "Dhana", "INV-2023-001", "01-10-2023", "Completed", "Approved", "BULK", 120.0)
            )
        )
    }

    val filteredList = remember(searchQuery, billsList) {
        if (searchQuery.isBlank()) {
            billsList
        } else {
            billsList.filter {
                it.vendorName.contains(searchQuery, ignoreCase = true) ||
                        it.invoiceNumber.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            Surface(modifier = Modifier.fillMaxWidth(), color = whiteBg) {
                TitleBar(title = "All Bills", onClose = onClose)
            }
        },
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        FabScaffold(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            fab = FabConfig(
                label = "Create Bill",
                icon = Icons.Default.Add,
                onClick = onCreateBillClick,
                endPadding = 16.dp,
                bottomPadding = 24.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SearchFilterBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        placeholder = "Search Customers...",
                        onFilterClick = onFilterClick
                    )
                }

                HorizontalDivider(color = dividerColor, thickness = 1.dp)

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(
                        items = filteredList,
                        key = { it.id }
                    ) { bill ->
                        BillRowCard(
                            bill = bill,
                            onCheckedChange = { checked ->
                                billsList = billsList.map {
                                    if (it.id == bill.id) it.copy(isSelected = checked) else it
                                }
                            },
                            onViewDetailsClick = { onBillDetailsClick(bill.id) }
                        )
                        HorizontalDivider(color = dividerColor, thickness = 1.dp)
                    }
                }
            }
        }
    }
}

@Composable
fun BillRowCard(
    bill: BillItem,
    onCheckedChange: (Boolean) -> Unit,
    onViewDetailsClick: () -> Unit
) {
    val tokens = LocalAppTokens.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(whiteBg)
            .clickable { onViewDetailsClick() }
            .padding(horizontal = tokens.screenPadding, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppCheckbox(
                checked = bill.isSelected,
                onCheckedChange = onCheckedChange
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(background_light_purple)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = bill.tag,
                    fontSize = tokens.label,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            IconButton(onClick = {}, modifier = Modifier.size(24.dp)) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = null,
                    tint = close_color,
                    modifier = Modifier.size(tokens.iconSize)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(background_light_purple),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = bill.initial,
                    fontSize = tokens.bodyMedium,
                    color = Primary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = bill.vendorName,
                    fontSize = tokens.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = bill.invoiceNumber,
                    fontSize = tokens.bodySmall,
                    color = close_color
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Date: ${bill.date}",
                        fontSize = tokens.caption,
                        color = close_color
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "•", fontSize = tokens.caption, color = close_color)
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(greenBg)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(greentext)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = bill.status,
                                fontSize = tokens.label,
                                fontWeight = FontWeight.Medium,
                                color = greentext
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(light_blue)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = bill.approvalStatus,
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(color = grey_border, thickness = 2.dp)
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "INVOICE AMOUNT",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = close_color,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "₹${"%.0f".format(bill.amount)}",
                    fontSize = tokens.bodyMedium,
                    color = Primary
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onViewDetailsClick() }
            ) {
                Text(
                    text = "View Details",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
    Spacer(Modifier.height(tokens.screenPadding))
}