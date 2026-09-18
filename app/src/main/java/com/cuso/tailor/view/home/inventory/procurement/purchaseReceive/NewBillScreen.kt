@file:Suppress("SpellCheckingInspection", "unused")

package com.cuso.tailor.view.home.inventory.procurement.purchaseReceive

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.FormDropdown
import com.cuso.tailor.view.composable.FormLabel
import com.cuso.tailor.view.composable.FormTextField
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.viewmodel.InventoryViewModel

@Composable
fun NewBillScreen(
    viewModel: InventoryViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
    onCancel: () -> Unit = onClose,
    onSaveDraft: () -> Unit = {},
    onSave: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val billData by viewModel.generatedBill.collectAsStateWithLifecycle()

    var vendorExpanded by remember { mutableStateOf(false) }
    var selectedVendor by remember { mutableStateOf("Acme Industrial Supplies Pvt Ltd") }

    var billNumberExpanded by remember { mutableStateOf(false) }
    var selectedBillNumber by remember { mutableStateOf("Bank Transfer (NEFT/RTGS)") }

    var orderNumber by remember { mutableStateOf("PO-2026-0011") }
    var dueDate by remember { mutableStateOf("2026-10-07") }
    var discountInput by remember { mutableStateOf("0") }

    LaunchedEffect(billData) {
        billData?.let { bill ->
            selectedVendor = bill.supplierId?.name ?: selectedVendor
            orderNumber = bill.poId?.poNumber ?: orderNumber
            dueDate = bill.dueDate.take(10)
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TitleBar(
                title = "New Bill",
                onClose = onClose
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = tokens.screenPadding, vertical = 8.dp)
        ) {
            Text(
                text = "GENERAL DETAILS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = close_color
            )

            Spacer(Modifier.height(12.dp))

            FormLabel("Vendor Name")
            FormDropdown(
                value = selectedVendor,
                expanded = vendorExpanded,
                onExpandChange = { vendorExpanded = it },
                options = listOf(selectedVendor),
                onOptionSelected = { selectedVendor = it }
            )

            Spacer(Modifier.height(14.dp))

            FormLabel("Bill Number")
            FormDropdown(
                value = billData?.billNumber ?: selectedBillNumber,
                expanded = billNumberExpanded,
                onExpandChange = { billNumberExpanded = it },
                options = listOf(billData?.billNumber ?: selectedBillNumber, "Bank Transfer (NEFT/RTGS)"),
                onOptionSelected = { selectedBillNumber = it }
            )

            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    FormLabel("Order Number")
                    FormTextField(value = orderNumber, onValueChange = { orderNumber = it })
                }
                Column(modifier = Modifier.weight(1f)) {
                    FormLabel("Due Date")
                    FormTextField(value = dueDate, onValueChange = { dueDate = it })
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Items List ──
            val items = billData?.items ?: emptyList()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ITEMS LIST (${items.size.coerceAtLeast(1)})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Account: Product",
                    fontSize = 11.sp,
                    color = mutedText
                )
            }

            Spacer(Modifier.height(12.dp))

            if (items.isNotEmpty()) {
                items.forEach { item ->
                    BillItemRow(
                        title = item.itemId?.name ?: "Product",
                        quantity = "${item.qty}",
                        rate = "₹${item.rate}",
                        amount = "₹${item.total}"
                    )
                    Spacer(Modifier.height(14.dp))
                }
            } else {
                BillItemRow(
                    title = "Classic Cotton Shirt",
                    quantity = "100",
                    rate = "₹250.00",
                    amount = "₹29,500"
                )
            }

            Spacer(Modifier.height(20.dp))
            HorizontalDivider(color = dividerColor, thickness = 1.dp)
            Spacer(Modifier.height(16.dp))

            // ── Order Summary ──
            Text(
                text = "Order Summary",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(Modifier.height(12.dp))

            SummaryRow(label = "Subtotal", value = "₹${billData?.subtotal ?: 25000.0}")
            Spacer(Modifier.height(8.dp))
            SummaryRow(label = "Tax Total", value = "₹${billData?.taxTotal ?: 4500.0}")
            Spacer(Modifier.height(8.dp))

            FormLabel("Discount")
            FormTextField(value = discountInput, onValueChange = { discountInput = it })

            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Grand Total", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("₹${billData?.grandTotal ?: 29500.0}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }

            Spacer(Modifier.height(28.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancel", color = TextPrimary, fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onSaveDraft,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Save Draft", color = TextPrimary, fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(10.dp))

            Button(
                onClick = onSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text("Save Bill", color = whiteBg, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── Missing Composables Implemented Below ──

@Composable
fun BillItemRow(
    title: String,
    quantity: String,
    rate: String,
    amount: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Primary, CircleShape)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }

            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color(0xFFF1F5F9)
            ) {
                Text(
                    text = "Product",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = "QUANTITY", fontSize = 10.sp, color = iconMuted)
                Text(text = quantity, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "RATE", fontSize = 10.sp, color = iconMuted)
                Text(text = rate, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(text = "AMOUNT", fontSize = 10.sp, color = iconMuted)
                Text(text = amount, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
            }
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = TextSecondary)
        Text(text = value, fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
    }
}