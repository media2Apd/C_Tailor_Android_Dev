@file:Suppress("unused")

package com.cuso.mobile.view.home.inventory.procurement.suppliers

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.cuso.mobile.adaptive_screen.AppDesignTokens
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.model.inventory.SupplierDto
import com.cuso.mobile.model.inventory.SupplierFinancialSummary
import com.cuso.mobile.model.inventory.SupplierLedgerEntry
import com.cuso.mobile.model.inventory.SupplierPendingItem
import com.cuso.mobile.model.inventory.SupplierPurchaseActivity
import com.cuso.mobile.ui.theme.*
import com.cuso.mobile.view.composable.*
import com.cuso.mobile.view.home.inventory.settings.SectionHeader
import com.cuso.mobile.viewmodel.InventoryViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import com.cuso.mobile.R

@Composable
fun SupplierDetailScreen(
    supplierId: String,
    onClose: () -> Unit,
    onEditSupplier: (SupplierDto) -> Unit = {},
    onBreadcrumbClick: () -> Unit = {},
    viewModel: InventoryViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val selectedSupplier by viewModel.selectedSupplier.collectAsState()
    val isLoadingDetail by viewModel.isLoadingDetail.collectAsState()
    val ledgerData by viewModel.supplierLedger.collectAsState()
    val isLoadingLedger by viewModel.isLoadingLedger.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Overview, 1: Purchase Orders, 2: Transactions

    val supplierTabs = remember {
        listOf("Overview", "Purchase Orders", "Transactions")
    }

    LaunchedEffect(supplierId) {
        if (supplierId.isNotBlank()) {
            viewModel.fetchSupplierDetail(supplierId)
            viewModel.fetchSupplierLedger(supplierId)
        }
    }

    Scaffold(
        topBar = {
            TitleBar(
                title = "Supplier",
                onClose = onClose
            )
        },
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.Transparent)
        ) {
            if (isLoadingDetail && selectedSupplier == null) {
                ListSkeleton()
            } else if (selectedSupplier != null) {
                val supplier = selectedSupplier!!
                Spacer(Modifier.height(20.dp))

                // ── 1. Header Profile Snapshot ──
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(whiteBg)
                        .padding(horizontal = tokens.screenPadding)
                ) {
                    Spacer(Modifier.height(8.dp))
                    SupplierHeaderProfile(
                        supplier = supplier,
                        tokens = tokens,
                        onEditClick = { onEditSupplier(supplier) }
                    )
                    Spacer(Modifier.height(14.dp))
                }
                Spacer(Modifier.height(20.dp))

                // ── 2. Underline Tab Row ──
                AppUnderlineTabRow(
                    tabs = supplierTabs,
                    selectedIndex = selectedTab,
                    onTabSelected = { selectedTab = it },
                    isScrollable = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // ── 3. Tab Content ──
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    when (selectedTab) {
                        0 -> SupplierOverviewTab(supplier = supplier, tokens = tokens)
                        1 -> SupplierPurchaseOrdersTab(supplier = supplier, tokens = tokens)
                        2 -> SupplierTransactionsTab(
                            ledgerEntries = ledgerData?.ledger ?: emptyList(),
                            isLoading = isLoadingLedger,
                            tokens = tokens
                        )
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Supplier details not available",
                        color = mutedText,
                        fontSize = tokens.bodyMedium
                    )
                }
            }
        }
    }
}

// =============================================================================
// HEADER PROFILE (100% API DATA)
// =============================================================================

@Composable
private fun SupplierHeaderProfile(
    supplier: SupplierDto,
    tokens: AppDesignTokens,
    onEditClick: () -> Unit
) {
    val contact = supplier.contact
    val createdYear = remember(supplier.createdAt) {
        supplier.createdAt?.take(4) ?: ""
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(lightGray),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_person),
                    contentDescription = null,
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = supplier.name.ifBlank { "—" },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF0F172A)
                )

                Spacer(Modifier.height(4.dp))

                OutlinedButton(
                    onClick = onEditClick,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier.height(26.dp)
                ) {
                    Text(
                        text = "Edit Supplier",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF334155)
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // Contact Phone
        contact?.phone?.takeIf { it.isNotBlank() }?.let { phone ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                Icon(Icons.Default.Phone, null, tint = Color(0xFF64748B), modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(10.dp))
                Text(phone, fontSize = 13.sp, color = Color(0xFF334155))
            }
        }

        // Contact Email
        contact?.email?.takeIf { it.isNotBlank() }?.let { email ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                Icon(Icons.Default.Email, null, tint = Color(0xFF64748B), modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(10.dp))
                Text(email, fontSize = 13.sp, color = Color(0xFF334155))
            }
        }

        // Created Date
        if (createdYear.isNotBlank()) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                Icon(Icons.Default.DateRange, null, tint = Color(0xFF64748B), modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(10.dp))
                Text("Supplier since $createdYear", fontSize = 13.sp, color = Color(0xFF64748B))
            }
        }
    }
}

// =============================================================================
// TAB 1: OVERVIEW (100% API DATA)
// =============================================================================

@Composable
private fun SupplierOverviewTab(
    supplier: SupplierDto,
    tokens: AppDesignTokens
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val fin = supplier.financialSummary
    val purchase = supplier.purchaseActivity
    val billing = supplier.address?.billing
    val shipping = supplier.address?.shipping

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding( vertical = 18.dp)
    ) {
        Column(
            Modifier.fillMaxWidth()
                .background(whiteBg)
                .padding(horizontal = tokens.screenPadding, vertical = 10.dp)
        ) {
            // ── 1. Supplier Information ──

            SectionHeader("Supplier Information")
            Spacer(Modifier.height(12.dp))
            InfoDividerRow("Type", supplier.type.ifBlank { "—" })
            InfoDividerRow("Contact", supplier.contact?.contactName?.ifBlank { "—" } ?: "—")
            InfoDividerRow("Phone", supplier.contact?.phone?.ifBlank { "—" } ?: "—")
            InfoDividerRow("Email", supplier.contact?.email?.ifBlank { "—" } ?: "—")
            InfoDividerRow("GST", supplier.tax?.gstNumber?.ifBlank { "—" } ?: "—", isBold = true)
            InfoDividerRow(
                "Payment Terms",
                supplier.payment?.let { "Net ${it.paymentTerm} Days" } ?: "—")
            InfoDividerRow(
                "Payment Mode",
                supplier.payment?.preferredPaymentMethod?.ifBlank { "—" } ?: "—")
        }
            SectionGap()
        Column(
            Modifier.fillMaxWidth()
                .background(whiteBg)
                .padding(horizontal = tokens.screenPadding, vertical = 10.dp)
        ) {
            // ── 2. Financial Summary ──

            SectionHeader("Financial Summary")

            Spacer(Modifier.height(12.dp))
            FinancialSummarySection(fin = fin)
        }
            SectionGap()
        Column(
            Modifier.fillMaxWidth()
                .background(whiteBg)
                .padding(horizontal = tokens.screenPadding, vertical = 10.dp)
        ) {
            // ── 3. Purchase Activity ──

            SectionHeader("Purchase Activity")

            Spacer(Modifier.height(14.dp))
            PurchaseActivitySection(purchase = purchase)
        }
            SectionGap()
        Column(
            Modifier.fillMaxWidth()
                .background(whiteBg)
                .padding(horizontal = tokens.screenPadding, vertical = 10.dp)

        ) {
            // ── 4. Pending items to receive ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pending items to receive",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "View All",
                    fontSize = 13.sp,
                    color = Primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { }
                )
            }
            Spacer(Modifier.height(10.dp))

            HorizontalDivider(color =grey_border )
            PendingItemsSection(pendingItems = supplier.pendingItems)
        }
            SectionGap()
        Column(
            Modifier.fillMaxWidth()
                .background(whiteBg)
                .padding(horizontal = tokens.screenPadding, vertical = 10.dp)

        ) {
            // ── 5. Notes ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Notes",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "+ Add Note",
                    fontSize = 13.sp,
                    color = Primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { }
                )
            }
            Spacer(Modifier.height(10.dp))

            HorizontalDivider(color =grey_border )
            Spacer(Modifier.height(10.dp))


            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = supplier.notes?.takeIf { it.isNotBlank() }
                        ?: "No notes available for this supplier.",
                    fontSize = 13.sp,
                    fontStyle = if (supplier.notes.isNullOrBlank()) FontStyle.Normal else FontStyle.Italic,
                    color = Color(0xFF475569),
                    lineHeight = 20.sp
                )
            }
        }
            SectionGap()
        Column(
            Modifier.fillMaxWidth()
                .background(whiteBg)
                .padding(horizontal = tokens.screenPadding, vertical = 10.dp)

        ) {
            // ── 6. Billing Address ──
            val billingText = billing?.fullAddressText?.takeIf { it.isNotBlank() }
                ?: "No billing address provided"
            AddressSection(
                title = "BILLING ADDRESS",
                addressText = billingText,
                onCopy = {
                    if (billing?.fullAddressText?.isNotBlank() == true) {
                        clipboardManager.setText(AnnotatedString(billing.fullAddressText))
                        Toast.makeText(context, "Billing address copied", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
            SectionGap()
        Column(
            Modifier.fillMaxWidth()
                .background(whiteBg)
                .padding(horizontal = tokens.screenPadding, vertical = 10.dp)

        ) {
            // ── 7. Shipping Address ──
            val shippingText = shipping?.fullAddressText?.takeIf { it.isNotBlank() }
                ?: "No shipping address provided"
            AddressSection(
                title = "SHIPPING ADDRESS",
                addressText = shippingText,
                onCopy = {
                    if (shipping?.fullAddressText?.isNotBlank() == true) {
                        clipboardManager.setText(AnnotatedString(shipping.fullAddressText))
                        Toast.makeText(context, "Shipping address copied", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            )
        }
            Spacer(Modifier.height(28.dp))
    }
}


// =============================================================================
// TAB 2: PURCHASE ORDERS TAB (100% API DATA)
// =============================================================================

@Composable
private fun SupplierPurchaseOrdersTab(
    supplier: SupplierDto,
    tokens: AppDesignTokens
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = tokens.screenPadding, vertical = 18.dp)
    ) {
        val purchase = supplier.purchaseActivity
        PurchaseActivitySection(purchase = purchase)
        SectionGap()
        Text(
            text = "Pending items to receive",
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF0F172A)
        )
        Spacer(Modifier.height(12.dp))
        PendingItemsSection(pendingItems = supplier.pendingItems)
    }
}

// =============================================================================
// TAB 3: TRANSACTIONS / LEDGER TAB (100% API DATA)
// =============================================================================

@Composable
private fun SupplierTransactionsTab(
    ledgerEntries: List<SupplierLedgerEntry>,
    isLoading: Boolean,
    tokens: AppDesignTokens
) {
    if (isLoading && ledgerEntries.isEmpty()) {
        ListSkeleton()
    } else if (ledgerEntries.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
            Text("No transactions or ledger records found", color = mutedText, fontSize = tokens.bodyMedium)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = tokens.screenPadding, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(ledgerEntries) { entry ->
                SupplierTransactionCard(entry = entry, tokens = tokens)
            }
        }
    }
}

// =============================================================================
// REUSABLE SUB-COMPONENTS
// =============================================================================

@Composable
private fun SectionGap() {
    Spacer(Modifier.height(20.dp))
}

@Composable
private fun InfoDividerRow(
    label: String,
    value: String,
    isBold: Boolean = false
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, fontSize = 14.sp, color = Color(0xFF64748B))
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
                color = Color(0xFF0F172A)
            )
        }
        HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
    }
}

@Composable
private fun FinancialSummarySection(
    fin: SupplierFinancialSummary?
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        FinancialItem("Outstanding Payable", formatCurrency(fin?.outstandingPayable ?: 0.0))
        Spacer(Modifier.height(14.dp))
        FinancialItem("Advance Given", formatCurrency(fin?.advanceGiven ?: 0.0))
        Spacer(Modifier.height(14.dp))
        FinancialItem("Total Purchase Value", formatCurrency(fin?.totalPurchaseValue ?: 0.0))
        Spacer(Modifier.height(14.dp))
        FinancialItem("Last Payment Date", formatDateString(fin?.lastPaymentDate) ?: "—")
    }
}

@Composable
private fun FinancialItem(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, fontSize = 13.sp, color = Color(0xFF64748B))
        Spacer(Modifier.height(3.dp))
        Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
    }
}

@Composable
private fun PurchaseActivitySection(
    purchase: SupplierPurchaseActivity?
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ActivityStatBox("Total PO", (purchase?.totalPO ?: 0).toString(), Modifier.weight(1f))
            ActivityStatBox("Open PO", (purchase?.openPO ?: 0).toString(), Modifier.weight(1f))
            ActivityStatBox("Partial", (purchase?.partial ?: 0).toString(), Modifier.weight(1f))
            ActivityStatBox("Completed", (purchase?.completed ?: 0).toString(), Modifier.weight(1f))
        }

        Spacer(Modifier.height(16.dp))
        HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
        Spacer(Modifier.height(12.dp))

        val lastPo = purchase?.lastPO
        if (lastPo != null && lastPo.poNumber.isNotBlank()) {
            Text(
                text = "Last PO:    ${lastPo.poNumber}    ${formatDateString(lastPo.poDate) ?: ""}    ${formatCurrency(lastPo.grandTotal)}",
                fontSize = 13.sp,
                color = Color(0xFF64748B),
                fontWeight = FontWeight.Medium
            )
        } else {
            Text(
                text = "No recent purchase orders",
                fontSize = 13.sp,
                color = Color(0xFF94A3B8)
            )
        }
    }
}

@Composable
private fun ActivityStatBox(label: String, value: String, modifier: Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
        Spacer(Modifier.height(2.dp))
        Text(text = label, fontSize = 12.sp, color = Color(0xFF64748B))
    }
}

@Composable
private fun PendingItemsSection(
    pendingItems: List<SupplierPendingItem>
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("PO NO", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8), modifier = Modifier.weight(1f))
            Text("ITEM", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8), modifier = Modifier.weight(2f))
            Text("QTY PENDING", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8), modifier = Modifier.weight(1.1f))
        }

        HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

        if (pendingItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No pending items", fontSize = 13.sp, color = Color(0xFF94A3B8))
            }
        } else {
            pendingItems.forEach { item ->
                PendingItemRowLine(item.poNo, item.itemName, "", "${item.qtyPending.toInt()} Pcs")
            }
        }
    }
}

@Composable
private fun PendingItemRowLine(poNo: String, title: String, subtitle: String, qty: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = poNo, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Primary, modifier = Modifier.weight(1f))
            Column(modifier = Modifier.weight(2f)) {
                Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF0F172A))
                if (subtitle.isNotBlank()) {
                    Text(text = subtitle, fontSize = 11.sp, color = Color(0xFF94A3B8))
                }
            }
            Text(text = qty, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF0F172A), modifier = Modifier.weight(1.1f))
        }
        Spacer(Modifier.height(10.dp))
        HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
    }
}

@Composable
private fun AddressSection(
    title: String,
    addressText: String,
    onCopy: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A), letterSpacing = 0.5.sp)
            Text(text = "Copy", fontSize = 12.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Medium, modifier = Modifier.clickable { onCopy() })
        }
        Spacer(Modifier.height(10.dp))
        HorizontalDivider(color = grey_border)
        Spacer(Modifier.height(10.dp))
        Text(
            text = addressText,
            fontSize = 13.sp,
            color = Color(0xFF334155),
            lineHeight = 19.sp
        )
    }
}

// ─────────────────────────────────────────────────────────────
// TRANSACTIONS / LEDGER CARD (100% API DATA)
// ─────────────────────────────────────────────────────────────

@Composable
private fun SupplierTransactionCard(
    entry: SupplierLedgerEntry,
    tokens: AppDesignTokens
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(primary_light)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(text = entry.refNo.ifBlank { "—" }, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Primary)
                }

                Spacer(Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(badgeGrey)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(text = entry.type.ifBlank { "TRANSACTION" }.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textSubdued)
                }

                Spacer(Modifier.weight(1f))

                Icon(Icons.Default.MoreVert, contentDescription = null, tint = mutedText, modifier = Modifier.size(tokens.iconSize * 0.9f))
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = entry.description.ifBlank { "—" },
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = title_color
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "Date: ${formatDateString(entry.date) ?: "—"}",
                fontSize = tokens.caption,
                color = mutedText
            )

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(Modifier.height(10.dp))

            // 3-Column Metrics (Debit, Credit, Balance)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("DEBIT", fontSize = tokens.caption, color = mutedText)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = if (entry.debit > 0) formatCurrency(entry.debit) else "-",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (entry.debit > 0) redText else title_color
                    )
                }

                Column {
                    Text("CREDIT", fontSize = tokens.caption, color = mutedText)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = if (entry.credit > 0) formatCurrency(entry.credit) else "-",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = greentext
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("BALANCE", fontSize = tokens.caption, color = mutedText)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = formatCurrency(entry.balance),
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = title_color
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// HELPERS
// ─────────────────────────────────────────────────────────────

private fun formatCurrency(value: Double): String {
    val format = NumberFormat.getNumberInstance(Locale.Builder().setLanguage("en").setRegion("IN").build())
    format.maximumFractionDigits = 2
    format.minimumFractionDigits = 2
    return "₹${format.format(value)}"
}

private fun formatDateString(isoDate: String?): String? {
    if (isoDate.isNullOrBlank()) return null
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = parser.parse(isoDate) ?: return isoDate.take(10)
        val formatter = SimpleDateFormat("dd/MMM/yyyy", Locale.US)
        formatter.format(date)
    } catch (_: Exception) {
        isoDate.take(10)
    }
}