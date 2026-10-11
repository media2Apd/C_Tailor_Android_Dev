@file:Suppress(
    "UNUSED_PARAMETER",
    "unused",
    "UNCHECKED_CAST",
    "DEPRECATION",
    "AssignedValueIsNeverRead",
    "GrazieInspection",
    "SpellCheckingInspection",
    "unusedvariable",
    "SameParameterValue"
)

package com.cuso.tailor.view.home.finance.account_payable.suppliers

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.R
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.finance.SupplierOverviewData
import com.cuso.tailor.model.inventory.PurchaseOrder
import com.cuso.tailor.model.inventory.SupplierLedgerContainer
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.SettingsTabs
import com.cuso.tailor.view.composable.TabItem
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.view.home.formatIndianNumber
import com.cuso.tailor.viewmodel.FinanceViewModel
import com.cuso.tailor.viewmodel.InventoryViewModel
import androidx.core.net.toUri

@Composable
fun FinanceSupplierDetailScreen(
    supplier: SupplierRow,
    supplierId: String = "",
    onClose: () -> Unit = {},
    onEditSupplier: () -> Unit = {},
    onNewOrder: () -> Unit = {},
    onBreadcrumbClick: () -> Unit = {},
    viewModel: FinanceViewModel = hiltViewModel(),
    inventoryViewModel: InventoryViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    // ── Overview Data ──
    val supplierOverviewData by viewModel.supplierOverview.collectAsStateWithLifecycle()
    val isLoadingOverview by viewModel.isLoadingSupplierOverview.collectAsStateWithLifecycle()

    // ── Ledger Data ──
    val supplierLedgerData by inventoryViewModel.supplierLedger.collectAsStateWithLifecycle()
    val isLoadingLedger by inventoryViewModel.isLoadingLedger.collectAsStateWithLifecycle()

    // ── Transactions (Purchase Orders List for this Supplier) ──
    val purchaseOrdersList by inventoryViewModel.purchaseOrdersList.collectAsStateWithLifecycle()
    val isLoadingPOs by inventoryViewModel.isLoadingPurchaseOrders.collectAsStateWithLifecycle()

    val effectiveSupplierId = remember(supplierId, supplier.id) {
        supplierId.ifBlank { supplier.id }
    }

    // Trigger Overview API on screen launch
    LaunchedEffect(effectiveSupplierId) {
        if (effectiveSupplierId.isNotBlank()) {
            viewModel.fetchSupplierOverview(effectiveSupplierId)
        }
    }

    // Trigger API calls dynamically when switching tabs
    LaunchedEffect(selectedTabIndex, effectiveSupplierId) {
        if (effectiveSupplierId.isNotBlank()) {
            when (selectedTabIndex) {
                1 -> {
                    // Fetch Purchase Orders for this supplier
                    inventoryViewModel.fetchAllPurchaseOrders(supplierId = effectiveSupplierId)
                }
                2 -> {
                    // Fetch Supplier Ledger
                    inventoryViewModel.fetchSupplierLedger(effectiveSupplierId)
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearSupplierOverview()
        }
    }

    val tabs = remember {
        listOf(
            TabItem(label = "Overview"),
            TabItem(label = "Transactions"),
            TabItem(label = "Ledger"),
            TabItem(label = "Statements")
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        TitleBar(
            title = "Supplier Details",
            onClose = onClose
        )
        HorizontalDivider(color = dividerColor)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(tokens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
        ) {
            SupplierTopCard(
                supplier = supplier,
                overview = supplierOverviewData,
                onEdit = onEditSupplier,
                onNewOrder = onNewOrder
            )

            SettingsTabs(
                tabs = tabs,
                selectedIndex = selectedTabIndex,
                onTabSelected = { selectedTabIndex = it },
                height = tokens.fieldHeight,
                containerColor = whiteBg,
                selectedBackgroundColor = primary_light,
                selectedTextColor = Primary,
                unselectedTextColor = TextSecondary,
                borderColor = BorderGray,
                cornerRadius = tokens.cardCornerRadius * 0.5f,
                selectedCornerRadius = tokens.cardCornerRadius * 0.4f
            )

            when (selectedTabIndex) {
                0 -> {
                    if (isLoadingOverview && supplierOverviewData == null) {
                        SupplierOverviewSkeleton()
                    } else {
                        SupplierOverviewTab(
                            supplier = supplier,
                            overview = supplierOverviewData
                        )
                    }
                }
                1 -> {
                    if (isLoadingPOs && purchaseOrdersList.isEmpty()) {
                        SupplierOverviewSkeleton()
                    } else {
                        SupplierTransactionsTab(
                            orders = purchaseOrdersList
                        )
                    }
                }
                2 -> {
                    if (isLoadingLedger && supplierLedgerData == null) {
                        SupplierOverviewSkeleton()
                    } else {
                        SupplierLedgerTab(
                            ledgerData = supplierLedgerData,
                            overview = supplierOverviewData
                        )
                    }
                }
                3 -> SupplierStatementsTab()
            }

            Spacer(Modifier.height(tokens.screenPadding * 2))
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Non-Lazy Placeholder for scroll container safe loading
// ─────────────────────────────────────────────────────────────
@Composable
private fun SupplierOverviewSkeleton() {
    val tokens = LocalAppTokens.current
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
    ) {
        Card(
            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
            colors = CardDefaults.cardColors(containerColor = whiteBg),
            border = BorderStroke(1.dp, BorderGray),
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize().padding(tokens.screenPadding)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(lightGray)
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                colors = CardDefaults.cardColors(containerColor = whiteBg),
                border = BorderStroke(1.dp, BorderGray),
                modifier = Modifier.weight(1f).height(80.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                    Box(modifier = Modifier.fillMaxWidth(0.7f).height(16.dp).clip(RoundedCornerShape(4.dp)).background(lightGray))
                }
            }
            Card(
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                colors = CardDefaults.cardColors(containerColor = whiteBg),
                border = BorderStroke(1.dp, BorderGray),
                modifier = Modifier.weight(1f).height(80.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                    Box(modifier = Modifier.fillMaxWidth(0.7f).height(16.dp).clip(RoundedCornerShape(4.dp)).background(lightGray))
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Supplier Top Card
// ─────────────────────────────────────────────────────────────
@Composable
private fun SupplierTopCard(
    supplier: SupplierRow,
    overview: SupplierOverviewData?,
    onEdit: () -> Unit,
    onNewOrder: () -> Unit
) {
    val tokens = LocalAppTokens.current

    val name = overview?.name?.ifBlank { null } ?: supplier.name.ifBlank { "-" }
    val city = overview?.billingAddress?.city?.ifBlank { null } ?: supplier.city.ifBlank { "-" }
    val isSupplierActive = (overview?.status ?: "active").equals("active", ignoreCase = true)

    Card(
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        border = BorderStroke(1.dp, BorderGray),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(tokens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
        ) {
            Text(
                text = name,
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = title_color
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSupplierActive) greenBg else redBg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isSupplierActive) greentext else redText)
                        )
                        Text(
                            text = if (isSupplierActive) "Active Supplier" else "Inactive",
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Medium,
                            color = if (isSupplierActive) greentext else redText
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = lightGray
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_location),
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(tokens.iconSize * 0.7f)
                        )
                        Text(
                            text = city,
                            fontSize = tokens.caption,
                            color = TextSecondary
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.45f),
                    border = BorderStroke(1.dp, BorderGray),
                    modifier = Modifier
                        .weight(1f)
                        .height(tokens.buttonHeight)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = title_color,
                        modifier = Modifier.size(tokens.iconSize * 0.85f)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Edit Details",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = title_color
                    )
                }

                Button(
                    onClick = onNewOrder,
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.45f),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    modifier = Modifier
                        .weight(1f)
                        .height(tokens.buttonHeight)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = whiteBg,
                        modifier = Modifier.size(tokens.iconSize * 0.85f)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "New Order",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = whiteBg
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// 1. OVERVIEW TAB
// ─────────────────────────────────────────────────────────────
@Composable
private fun SupplierOverviewTab(
    supplier: SupplierRow,
    overview: SupplierOverviewData?
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current

    val financial = overview?.financialSummary
    val purchase = overview?.purchaseActivity
    val payment = overview?.payment
    val docs = overview?.documents

    val outstandingPayable = financial?.outstandingPayable ?: 0.0
    val advanceGiven = financial?.advanceGiven ?: 0.0
    val totalPurchaseValue = financial?.totalPurchaseValue ?: 0.0

    // Helper to open document URL in browser / external downloader
    fun openDocument(url: String?) {
        if (!url.isNullOrBlank()) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                context.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "Cannot open document link", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
        // --- 1. Total Payable Card ---
        Card(
            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
            colors = CardDefaults.cardColors(containerColor = whiteBg),
            border = BorderStroke(1.dp, BorderGray),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(tokens.screenPadding)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TOTAL PAYABLE",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(light_blue, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AccountBalanceWallet,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(tokens.iconSize)
                        )
                    }
                }

                Spacer(Modifier.height(6.dp))

                Text(
                    text = "₹${formatIndianNumber(outstandingPayable)}.00",
                    fontSize = tokens.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = title_color
                )

                Spacer(Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = null,
                        tint = greentext,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Total Purchases: ₹${formatIndianNumber(totalPurchaseValue)}",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Medium,
                        color = greentext
                    )
                }
            }
        }

        // --- 2. Advance Given & PO Metrics ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                colors = CardDefaults.cardColors(containerColor = whiteBg),
                border = BorderStroke(1.dp, BorderGray),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ADVANCE GIVEN",
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .background(light_blue, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AccountBalanceWallet,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "₹${formatIndianNumber(advanceGiven)}.00",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (advanceGiven > 0) greentext else title_color
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Credit Limit: ₹${formatIndianNumber(payment?.creditLimit ?: 0)}",
                        fontSize = tokens.caption,
                        color = mutedText
                    )
                }
            }

            Card(
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                colors = CardDefaults.cardColors(containerColor = whiteBg),
                border = BorderStroke(1.dp, BorderGray),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PURCHASE ORDERS",
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .background(light_blue, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "${purchase?.totalPO ?: 0} Total POs",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = title_color
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "${purchase?.openPO ?: 0} Open • ${purchase?.completed ?: 0} Settled",
                        fontSize = tokens.caption,
                        color = mutedText
                    )
                }
            }
        }

        PurchaseVolumeTrendChartCard()
        PointOfContactCard(overview = overview)

        // --- 3. Compliance Documents Section ---
        Text(
            text = "COMPLIANCE DOCUMENTS",
            fontSize = tokens.caption,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary,
            letterSpacing = 0.5.sp
        )

        // GST Certificate
        ComplianceDocCard(
            title = "GST Certificate",
            fileUrl = docs?.gstCertificate?.fileUrl,
            onDownload = { openDocument(docs?.gstCertificate?.fileUrl) }
        )

        // Company Registration
        ComplianceDocCard(
            title = "Company Registration",
            fileUrl = docs?.companyRegistration?.fileUrl,
            onDownload = { openDocument(docs?.companyRegistration?.fileUrl) }
        )

        // Bank Proof
        ComplianceDocCard(
            title = "Bank Proof",
            fileUrl = docs?.bankProof?.fileUrl,
            onDownload = { openDocument(docs?.bankProof?.fileUrl) }
        )

        // Agreements
        ComplianceDocCard(
            title = "Agreements",
            fileUrl = docs?.agreements?.fileUrl,
            onDownload = { openDocument(docs?.agreements?.fileUrl) }
        )
    }
}

// ─────────────────────────────────────────────────────────────
// Dynamic Compliance Document Item Card
// ─────────────────────────────────────────────────────────────
@Composable
private fun ComplianceDocCard(
    title: String,
    fileUrl: String?,
    onDownload: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val isAvailable = !fileUrl.isNullOrBlank()

    Card(
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.45f),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        border = BorderStroke(1.dp, BorderGray),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isAvailable) { onDownload() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = tokens.screenPadding, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // PDF Document Icon (Always Red icon like your image)
                Icon(
                    imageVector = Icons.Outlined.PictureAsPdf,
                    contentDescription = null,
                    tint = redText,
                    modifier = Modifier.size(tokens.iconSize * 1.1f)
                )

                Text(
                    text = title,
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = title_color
                )
            }

            if (isAvailable) {
                // Download button if URL exists
                IconButton(
                    onClick = onDownload,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Download Document",
                        tint = Primary,
                        modifier = Modifier.size(tokens.iconSize)
                    )
                }
            } else {
                // "Missing" text if URL is absent
                Text(
                    text = "Missing",
                    fontSize = tokens.caption,
                    color = mutedText,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Point Of Contact Card
// ─────────────────────────────────────────────────────────────
@Composable
private fun PointOfContactCard(overview: SupplierOverviewData?) {
    val tokens = LocalAppTokens.current

    val contact = overview?.contact
    val contactName = contact?.contactName?.ifBlank { null } ?: "Not Assigned"
    val email = contact?.email?.ifBlank { null } ?: "No Email Provided"
    val phone = contact?.phone?.ifBlank { null } ?: "No Phone Provided"
    val supplierType = overview?.type?.ifBlank { "Corporate Supplier" } ?: "Corporate Supplier"

    Card(
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        border = BorderStroke(1.dp, BorderGray),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(tokens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .shadow(elevation = 2.dp, shape = RoundedCornerShape(14.dp))
                    .background(whiteBg, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(tokens.iconSize * 1.2f)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Main Point of Contact",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = Primary
                )
                Text(
                    text = supplierType,
                    fontSize = tokens.caption,
                    color = headerGrey
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ContactInfoRow(
                    icon = Icons.Outlined.Badge,
                    text = contactName,
                    textColor = title_color,
                    fontWeight = FontWeight.SemiBold
                )
                ContactInfoRow(
                    icon = Icons.Outlined.Mail,
                    text = email,
                    textColor = Primary
                )
                ContactInfoRow(
                    icon = painterResource(R.drawable.ic_phone),
                    text = phone,
                    textColor = textSubdued
                )
            }

            OutlinedButton(
                onClick = { },
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                border = BorderStroke(1.dp, sectionBorder),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = badgeGrey),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(tokens.buttonHeight)
            ) {
                Text(
                    text = "Contact Supplier",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = title_color
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Purchase Volume Trend Chart
// ─────────────────────────────────────────────────────────────
@Composable
private fun PurchaseVolumeTrendChartCard() {
    val tokens = LocalAppTokens.current
    val months = listOf("May", "Jun", "Jul", "Aug", "Sep", "Oct")
    val heights = listOf(0.33f, 0.62f, 1.0f, 0.45f, 0.77f, 0.87f)
    val peakIndex = heights.indices.maxByOrNull { heights[it] } ?: 0

    Card(
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        border = BorderStroke(1.dp, BorderGray),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(tokens.screenPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Purchase Volume Trend",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = Primary
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = grey_border
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "Last 6 Months",
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Medium,
                            color = textSubdued
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = headerGrey,
                            modifier = Modifier.size(tokens.iconSize * 0.8f)
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                months.forEachIndexed { index, month ->
                    val isPeak = index == peakIndex

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.55f)
                                    .fillMaxHeight(heights[index])
                                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                    .background(if (isPeak) Primary else grey_border)
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = month,
                            fontSize = tokens.caption,
                            fontWeight = if (isPeak) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isPeak) Primary else iconMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ContactInfoRow(
    icon: Any,
    text: String,
    textColor: Color,
    fontWeight: FontWeight = FontWeight.Normal
) {
    val tokens = LocalAppTokens.current

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(grey_border, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            when (icon) {
                is ImageVector -> {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = headerGrey,
                        modifier = Modifier.size(tokens.iconSize * 0.8f)
                    )
                }
                is androidx.compose.ui.graphics.painter.Painter -> {
                    Icon(
                        painter = icon,
                        contentDescription = null,
                        tint = headerGrey,
                        modifier = Modifier.size(tokens.iconSize * 0.8f)
                    )
                }
            }
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = text,
            fontSize = tokens.bodySmall,
            fontWeight = fontWeight,
            color = textColor
        )
    }
}

@Composable
private fun SupplierOrderListItem(
    code: String,
    title: String,
    status: String,
    dateInfo: String,
    quantity: String,
    amount: String
) {
    val tokens = LocalAppTokens.current
    val isCompleted = status.equals("Completed", ignoreCase = true)

    Card(
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        border = BorderStroke(1.dp, BorderGray),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(tokens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(light_blue, RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Description,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(tokens.iconSize)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "$code - $title",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = title_color
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isCompleted) greenBg else light_blue
                ) {
                    Text(
                        text = status,
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Medium,
                        color = if (isCompleted) greentext else Primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = dateInfo,
                fontSize = tokens.caption,
                color = mutedText
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = quantity,
                    fontSize = tokens.caption,
                    color = TextSecondary
                )
                Text(
                    text = amount,
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = title_color
                )
            }
        }
    }
}

@Composable
private fun ComplianceDocCard(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color
) {
    val tokens = LocalAppTokens.current

    Card(
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        border = BorderStroke(1.dp, BorderGray),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = tokens.screenPadding, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(iconBg, RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(tokens.iconSize)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    text = title,
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = title_color
                )
            }

            IconButton(onClick = { }, modifier = Modifier.size(28.dp)) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "Download",
                    tint = TextSecondary,
                    modifier = Modifier.size(tokens.iconSize * 0.9f)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// 2. TRANSACTIONS TAB (Real API-bound Purchase Orders)
// ─────────────────────────────────────────────────────────────
@Composable
private fun SupplierTransactionsTab(
    orders: List<PurchaseOrder>
) {
    val tokens = LocalAppTokens.current

    Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
        // Last Transaction Summary Banner
        val lastOrder = orders.firstOrNull()
        Surface(
            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.45f),
            color = light_blue,
            border = BorderStroke(1.dp, light_blue_border),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(tokens.iconSize)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = if (lastOrder != null) {
                        "Last Order: ${lastOrder.poNumber ?: "PO"} (${lastOrder.poDate?.take(10) ?: "Recent"}) - ₹${formatIndianNumber(lastOrder.grandTotal)}"
                    } else {
                        "No transaction records available for this supplier"
                    },
                    fontSize = tokens.bodySmall,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TRANSACTION RECORDS",
                fontSize = tokens.caption,
                fontWeight = FontWeight.SemiBold,
                color = Primary,
                letterSpacing = 0.5.sp
            )
            Text(
                text = "${orders.size} Total",
                fontSize = tokens.caption,
                color = mutedText
            )
        }

        if (orders.isEmpty()) {
            Card(
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                colors = CardDefaults.cardColors(containerColor = whiteBg),
                border = BorderStroke(1.dp, BorderGray),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No purchase orders found for this supplier",
                        fontSize = tokens.bodySmall,
                        color = mutedText
                    )
                }
            }
        } else {
            orders.forEach { po ->
                TransactionRecordCardItem(
                    date = po.poDate?.take(10) ?: (po.createdAt?.take(10) ?: "Recent"),
                    poNumber = po.poNumber ?: "PO",
                    type = po.poType.ifBlank { "Standard" },
                    description = if (po.items.isNotEmpty()) "${po.items.size} item(s) ordered" else "Purchase Order",
                    debit = "₹${formatIndianNumber(po.grandTotal)}.00",
                    credit = if (po.paymentStatus.equals("Paid", ignoreCase = true)) "₹${formatIndianNumber(po.grandTotal)}.00" else "-",
                    balance = po.paymentStatus.ifBlank { "Unpaid" }
                )
            }
        }
    }
}

@Composable
private fun TransactionRecordCardItem(
    date: String,
    poNumber: String,
    type: String,
    description: String,
    debit: String,
    credit: String,
    balance: String
) {
    val tokens = LocalAppTokens.current

    Card(
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        border = BorderStroke(1.dp, BorderGray),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(tokens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = date,
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = title_color
                    )
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = lightGray
                    ) {
                        Text(
                            text = poNumber,
                            fontSize = 11.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = light_blue
                ) {
                    Text(
                        text = type,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }
            }

            Text(
                text = description,
                fontSize = tokens.bodySmall,
                color = TextSecondary
            )

            Spacer(Modifier.height(4.dp))
            HorizontalDivider(color = grey_border.copy(alpha = 0.6f))
            Spacer(Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Total (Debit)", fontSize = tokens.caption, color = mutedText)
                    Text(debit, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = title_color)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Paid (Credit)", fontSize = tokens.caption, color = mutedText)
                    Text(credit, fontSize = tokens.bodySmall, color = title_color)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Status", fontSize = tokens.caption, color = mutedText)
                    Text(balance, fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = Primary)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// 3. LEDGER TAB
// ─────────────────────────────────────────────────────────────
@Composable
private fun SupplierLedgerTab(
    ledgerData: SupplierLedgerContainer?,
    overview: SupplierOverviewData?
) {
    val tokens = LocalAppTokens.current
    val ledgerList = ledgerData?.ledger.orEmpty()

    val totalPurchases = ledgerList.filter { it.type.contains("Purchase", ignoreCase = true) || it.type.contains("Invoice", ignoreCase = true) }.sumOf { it.debit }
    val totalPaid = ledgerList.filter { it.type.contains("Payment", ignoreCase = true) }.sumOf { it.credit }
    val openingBalance = overview?.payment?.openingBalance ?: (ledgerList.firstOrNull()?.balance ?: 0.0)
    val closingDue = overview?.financialSummary?.outstandingPayable ?: (ledgerList.lastOrNull()?.balance ?: 0.0)

    Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.spacedBy(10.dp)
//        ) {
//            LedgerKpiCard(
//                modifier = Modifier.weight(1f),
//                title = "OPENING BAL",
//                amount = "₹${formatIndianNumber(openingBalance)}",
//                subtitle = "Initial forwarded balance",
//                icon = Icons.Outlined.Schedule
//            )
//            LedgerKpiCard(
//                modifier = Modifier.weight(1f),
//                title = "PURCHASES",
//                amount = "₹${formatIndianNumber(if (totalPurchases > 0) totalPurchases else overview?.financialSummary?.totalPurchaseValue ?: 0.0)}",
//                subtitle = "Invoices (+)",
//                icon = Icons.Default.Add
//            )
//        }
//
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.spacedBy(10.dp)
//        ) {
//            LedgerKpiCard(
//                modifier = Modifier.weight(1f),
//                title = "PAID",
//                amount = "₹${formatIndianNumber(totalPaid)}",
//                subtitle = "Disbursed (-)",
//                icon = Icons.Default.Check,
//                iconTint = greentext
//            )
//            LedgerKpiCard(
//                modifier = Modifier.weight(1f),
//                title = "• CLOSING DUE",
//                amount = "₹${formatIndianNumber(closingDue)}",
//                subtitle = "Net owed balance",
//                icon = Icons.Default.CalendarMonth,
//                isHighlighted = true
//            )
//        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Ledger Records",
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = title_color
            )
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = lightGray
            ) {
                Text(
                    text = ledgerList.size.toString(),
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        if (ledgerList.isEmpty()) {
            Card(
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                colors = CardDefaults.cardColors(containerColor = whiteBg),
                border = BorderStroke(1.dp, BorderGray),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No ledger entries found for this supplier",
                        fontSize = tokens.bodySmall,
                        color = mutedText
                    )
                }
            }
        } else {
            ledgerList.forEach { entry ->
                LedgerRecordCardItem(
                    date = entry.date.take(10).ifBlank { "Recent" },
                    status = "Posted",
                    reference = entry.refNo.ifBlank { "—" },
                    type = entry.type.ifBlank { "General" },
                    debit = if (entry.debit > 0) "₹${formatIndianNumber(entry.debit)}" else "—",
                    credit = if (entry.credit > 0) "₹${formatIndianNumber(entry.credit)}" else "—",
                    runningBal = "₹${formatIndianNumber(entry.balance)}",
                    description = entry.description.ifBlank { "Supplier transaction" }
                )
            }
        }
    }
}

@Composable
private fun LedgerRecordCardItem(
    date: String,
    status: String,
    reference: String,
    type: String,
    debit: String,
    credit: String,
    runningBal: String,
    description: String
) {
    val tokens = LocalAppTokens.current

    Card(
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        border = BorderStroke(1.dp, BorderGray),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(tokens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = date,
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = title_color
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = greenBg
                    ) {
                        Text(
                            text = status,
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Medium,
                            color = greentext,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                    IconButton(onClick = { }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.MoreVert, null, tint = mutedText)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("REFERENCE", fontSize = tokens.caption, color = mutedText)
                    Text(reference, fontSize = tokens.bodySmall, color = title_color)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("TRANSACTION TYPE", fontSize = tokens.caption, color = mutedText)
                    Text(type, fontSize = tokens.bodySmall, color = title_color)
                }
            }

            Surface(
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                color = light_blue,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("DEBIT (₹)", fontSize = tokens.caption, color = mutedText)
                        Text(debit, fontSize = tokens.bodySmall, color = title_color)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("CREDIT (₹)", fontSize = tokens.caption, color = mutedText)
                        Text(credit, fontSize = tokens.bodySmall, color = title_color)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("RUNNING BAL (₹)", fontSize = tokens.caption, color = mutedText)
                        Text(runningBal, fontSize = tokens.bodySmall, color = title_color)
                    }
                }
            }

            Column {
                Text("Description", fontSize = tokens.caption, color = mutedText)
                Spacer(Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                    color = lightGray.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = description,
                        fontSize = tokens.caption,
                        color = TextSecondary,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// 4. STATEMENTS TAB
// ─────────────────────────────────────────────────────────────
@Composable
private fun SupplierStatementsTab() {
    val tokens = LocalAppTokens.current

    Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LedgerKpiCard(
                modifier = Modifier.weight(1f),
                title = "TOTAL STATEMENTS",
                amount = "₹24,80,000",
                subtitle = "As of 01 Apr 2026"
            )
            LedgerKpiCard(
                modifier = Modifier.weight(1f),
                title = "CURRENT PERIOD BILLS",
                amount = "₹72,40,000",
                subtitle = "8 Invoices (+)"
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LedgerKpiCard(
                modifier = Modifier.weight(1f),
                title = "CURRENT PERIOD PAYMENTS",
                amount = "₹48,20,000",
                subtitle = "5 Disbursed (-)"
            )
            LedgerKpiCard(
                modifier = Modifier.weight(1f),
                title = "• CURRENT NET OUTSTANDING",
                amount = "₹49,00,000",
                subtitle = "Reconciled with GL",
                isHighlighted = true
            )
        }

        StatementRecordCardItem(
            stmtCode = "STMT-2026-003",
            status = "Generated",
            opening = "₹32,30,000",
            generatedOn = "01 Oct 2026",
            totalBills = "₹23,60,000",
            payments = "₹15,70,000",
            closingBal = "₹49,00,000",
            period = "01 Sep 2026 - 30 Sep 2026"
        )
    }
}

@Composable
private fun StatementRecordCardItem(
    stmtCode: String,
    status: String,
    opening: String,
    generatedOn: String,
    totalBills: String,
    payments: String,
    closingBal: String,
    period: String
) {
    val tokens = LocalAppTokens.current

    Card(
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        border = BorderStroke(1.dp, BorderGray),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(tokens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stmtCode,
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Primary
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = greenBg
                    ) {
                        Text(
                            text = status,
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Medium,
                            color = greentext,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                    IconButton(onClick = { }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.MoreVert, null, tint = mutedText)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("OPENING (₹)", fontSize = tokens.caption, color = mutedText)
                    Text(opening, fontSize = tokens.bodySmall, color = title_color)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("GENERATED ON", fontSize = tokens.caption, color = mutedText)
                    Text(generatedOn, fontSize = tokens.bodySmall, color = title_color)
                }
            }

            Surface(
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                color = light_blue,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("TOTAL BILLS (₹)", fontSize = tokens.caption, color = mutedText)
                        Text(totalBills, fontSize = tokens.bodySmall, color = title_color)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("PAYMENTS (₹)", fontSize = tokens.caption, color = mutedText)
                        Text(payments, fontSize = tokens.bodySmall, color = title_color)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("CLOSING BAL (₹)", fontSize = tokens.caption, color = mutedText)
                        Text(closingBal, fontSize = tokens.bodySmall, color = title_color)
                    }
                }
            }

            Column {
                Text("PERIOD", fontSize = tokens.caption, color = mutedText)
                Spacer(Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                    color = lightGray.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = period,
                        fontSize = tokens.caption,
                        color = TextSecondary,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun LedgerKpiCard(
    modifier: Modifier = Modifier,
    title: String,
    amount: String,
    subtitle: String,
    icon: ImageVector? = null,
    iconTint: Color = TextSecondary,
    isHighlighted: Boolean = false
) {
    val tokens = LocalAppTokens.current

    Card(
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        border = BorderStroke(
            width = 1.dp,
            color = if (isHighlighted) Primary else BorderGray
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Medium,
                    color = if (isHighlighted) Primary else TextSecondary
                )
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isHighlighted) Primary else iconTint,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = amount,
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (isHighlighted) Primary else title_color
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = tokens.caption,
                color = mutedText
            )
        }
    }
}