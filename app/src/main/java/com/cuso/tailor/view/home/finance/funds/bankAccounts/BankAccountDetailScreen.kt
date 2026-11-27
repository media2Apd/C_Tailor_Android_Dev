package com.cuso.tailor.view.home.finance.funds.bankAccounts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import android.widget.Toast
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.finance.BankAccountItemDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.utils.formatCurrency
import com.cuso.tailor.view.composable.ListSkeleton
import com.cuso.tailor.view.composable.StatusBadge
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.viewmodel.FinanceViewModel
import java.util.Locale

@Composable
fun BankAccountDetailScreen(
    accountId: String,
    viewModel: FinanceViewModel,
    onBack: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current
    val account by viewModel.selectedBankAccount.collectAsState()
    val isLoading by viewModel.isLoadingBankAccounts.collectAsState()

    var selectedTab by remember { mutableStateOf("Overview") }
    var isSyncTriggered by remember { mutableStateOf(false) }
    val tabs = listOf("Overview", "Transactions", "Reconciliation")

    // Hit view-one API and trigger full skeleton reload
    fun reloadBankAccount() {
        isSyncTriggered = true
        viewModel.fetchBankAccountById(accountId)
    }

    LaunchedEffect(accountId) {
        viewModel.fetchBankAccountById(accountId)
    }

    // Show confirmation toast once view-one reload finishes
    LaunchedEffect(isLoading) {
        if (!isLoading && isSyncTriggered) {
            isSyncTriggered = false
            Toast.makeText(
                context,
                "Bank account synced successfully!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Row(modifier = Modifier.fillMaxWidth()) {
                TitleBar(
                    title = account?.accountDisplayName ?: "Bank Details",
                    onClose = onBack
                )
            }
        }
    ) { padding ->
        if (isLoading || account == null) {
            ListSkeleton()
        } else {
            val item: BankAccountItemDto = account!!

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding),
                verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
            ) {
                // Segmented Tabs Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                        .background(badgeGrey)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    tabs.forEach { tab ->
                        val isSelected = selectedTab == tab
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
                                .background(if (isSelected) whiteBg else Color.Transparent)
                                .clickable { selectedTab = tab }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tab,
                                fontSize = tokens.bodySmall,
                                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                                color = if (isSelected) Primary else TextSecondary
                            )
                        }
                    }
                }

                when (selectedTab) {
                    "Overview" -> {
                        // =============================================================
                        // Card 1: Main Current Balance
                        // =============================================================
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    colors = CardDefaults.cardColors(containerColor = whiteBg),
                    border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(PrimaryBorder))
                ) {
                    Column(
                        modifier = Modifier.padding(tokens.cardPadding * 0.75f),
                        verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.4f)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CURRENT BALANCE",
                                fontSize = tokens.label,
                                color = mutedText,
                                fontWeight = FontWeight.Medium
                            )
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(primary_light, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AccountBalance,
                                    contentDescription = null,
                                    tint = Primary,
                                    modifier = Modifier.size(tokens.iconSize)
                                )
                            }
                        }

                        Text(
                            text = "${formatCurrency()}${String.format(Locale.US, "%,.2f", item.chartOfAccountId?.openingBalance ?: 0.0)}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Medium,
                            color = TitleColor
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ShowChart,
                                contentDescription = null,
                                tint = greentext,
                                modifier = Modifier.size(tokens.iconSize * 0.8f)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "+12.5% from last month",
                                fontSize = tokens.label,
                                color = greentext
                            )
                        }
                    }
                }

                // =============================================================
                // Row 2: Cleared & Uncleared Balances
                // =============================================================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                ) {
                    MetricBox(
                        title = "CLEARED BALANCE",
                        amount = "${formatCurrency()}${String.format(Locale.US, "%,.2f", item.chartOfAccountId?.openingBalance ?: 0.0)}",
                        caption = "Reconciled: Recently",
                        icon = Icons.Outlined.CheckCircle,
                        iconTint = greentext,
                        modifier = Modifier.weight(1f)
                    )

                    MetricBox(
                        title = "UNCLEARED",
                        amount = "${formatCurrency()}0.00",
                        caption = "0 pending trans.",
                        icon = Icons.Outlined.History,
                        iconTint = yellowText,
                        modifier = Modifier.weight(1f)
                    )
                }

                // =============================================================
                // Card 3: Account Summary
                // =============================================================
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    colors = CardDefaults.cardColors(containerColor = whiteBg),
                    border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(PrimaryBorder))
                ) {
                    Column(
                        modifier = Modifier.padding(tokens.cardPadding * 0.75f),
                        verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                    ) {
                        Text(
                            text = "Account Summary",
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = TitleColor
                        )

                        DetailRow("INSTITUTION", item.bankName)
                        DetailRow("ACCOUNT NUMBER", item.accountNumberMasked.ifBlank { item.accountNumber })
                        DetailRow("ROUTING / IFSC", item.routingIdentifierValue.orEmpty())
                        DetailRow("BRANCH", item.bankBranchName.orEmpty())
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("STATUS", fontSize = tokens.caption, color = mutedText)
                            StatusBadge(
                                text = item.status,
                                bgColor = if (item.status == "Active") greenBg else redBg,
                                textColor = if (item.status == "Active") greentext else redText
                            )
                        }
                    }
                }

                // =============================================================
                // Card 4: Direct Bank Sync
                // =============================================================
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    colors = CardDefaults.cardColors(containerColor = whiteBg),
                    border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(PrimaryBorder))
                ) {
                    Column(
                        modifier = Modifier.padding(tokens.cardPadding * 0.75f),
                        verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Direct Bank Sync",
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = TitleColor
                            )
                            Text(
                                text = "CONNECTED",
                                fontSize = tokens.label,
                                color = greentext,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        DetailRow("Method:", "Plaid Secure Link")
                        DetailRow("Last Synced:", "Today")

                        OutlinedButton(
                            onClick = { reloadBankAccount() },
                            enabled = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(tokens.buttonHeight),
                            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                            border = ButtonDefaults.outlinedButtonBorder.copy(brush = SolidColor(BorderGray))
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Sync,
                                contentDescription = null,
                                tint = TitleColor,
                                modifier = Modifier.size(tokens.iconSize)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (isSyncTriggered) "Syncing..." else "Sync Now",
                                color = TitleColor,
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // =============================================================
                // Card 5: Account Tools
                // =============================================================
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    colors = CardDefaults.cardColors(containerColor = whiteBg),
                    border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(PrimaryBorder))
                ) {
                    Column(
                        modifier = Modifier.padding(tokens.cardPadding * 0.75f),
                        verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                    ) {
                        Text(
                            text = "Account Tools",
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = TitleColor
                        )

                        ToolItem(
                            title = "Statement Reconciliation",
                            icon = Icons.AutoMirrored.Outlined.ReceiptLong,
                            onClick = { selectedTab = "Reconciliation" }
                        )
                        HorizontalDivider(color = dividerColor)
                        ToolItem(
                            title = "Recurring Payments",
                            icon = Icons.Outlined.DateRange,
                            onClick = { selectedTab = "Transactions" }
                        )
                        HorizontalDivider(color = dividerColor)
                        ToolItem(
                            title = "Access Permissions",
                            icon = Icons.Outlined.Security,
                            onClick = { }
                        )
                    }
                }
                    }

                    // =============================================================
                    // Tab 2: Transactions
                    // =============================================================
                    "Transactions" -> {
                        BankTransactionsTab(
                            accountName = item.accountDisplayName,
                            onRefresh = { reloadBankAccount() }
                        )
                    }

                    // =============================================================
                    // Tab 3: Reconciliation
                    // =============================================================
                    "Reconciliation" -> {
                        BankReconciliationTab(
                            accountName = item.accountDisplayName,
                            clearedBalance = item.chartOfAccountId?.openingBalance ?: 0.0,
                            isSyncing = isSyncTriggered,
                            onRunReconciliation = { reloadBankAccount() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricBox(
    title: String,
    amount: String,
    caption: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    val tokens = LocalAppTokens.current
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.7f),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(PrimaryBorder))
    ) {
        Column(
            modifier = Modifier.padding(tokens.cardPadding * 0.65f),
            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.4f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = tokens.label,
                    color = mutedText
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(tokens.iconSize * 0.85f)
                )
            }
            Text(
                text = amount,
                fontSize = tokens.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = TitleColor
            )
            Text(
                text = caption,
                fontSize = tokens.label,
                color = mutedText
            )
        }
    }
}

@Composable
private fun ToolItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(primary_light, RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(tokens.iconSize * 0.9f)
                )
            }
            Text(
                text = title,
                fontSize = tokens.bodySmall,
                color = TitleColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String?) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = tokens.caption, color = mutedText)
        Text(
            text = value ?: "",
            fontSize = tokens.bodySmall,
            fontWeight = FontWeight.Medium,
            color = TitleColor
        )
    }
}

// =============================================================
// Transactions Tab (matches existing BankAccountDetailScreen card style)
// =============================================================
@Composable
private fun BankTransactionsTab(
    accountName: String,
    onRefresh: () -> Unit
) {
    val tokens = LocalAppTokens.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(PrimaryBorder))
    ) {
        Column(
            modifier = Modifier.padding(tokens.cardPadding * 0.75f),
            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Live Bank Transactions",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = TitleColor
                )
                Text(
                    text = "Refresh",
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Medium,
                    color = Primary,
                    modifier = Modifier.clickable { onRefresh() }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.History,
                    contentDescription = null,
                    tint = mutedText,
                    modifier = Modifier.size(tokens.iconSize * 1.3f)
                )
                Text(
                    text = "No recent activity recorded",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = TitleColor
                )
                Text(
                    text = "Transactions posted to $accountName will appear here dynamically.",
                    fontSize = tokens.caption,
                    color = mutedText,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// =============================================================
// Reconciliation Tab (hits viewModel.fetchBankAccountById on click)
// =============================================================
@Composable
private fun BankReconciliationTab(
    accountName: String,
    clearedBalance: Double,
    isSyncing: Boolean,
    onRunReconciliation: () -> Unit
) {
    val tokens = LocalAppTokens.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(PrimaryBorder))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(tokens.cardPadding * 0.75f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(primary_light, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ReceiptLong,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(tokens.iconSize * 1.1f)
                )
            }

            Text(
                text = "Statement Reconciliation",
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = TitleColor
            )

            Text(
                text = "Live double-entry reconciliation between bank feeds and General Ledger for $accountName.",
                fontSize = tokens.caption,
                color = mutedText,
                textAlign = TextAlign.Center
            )

            DetailRow(
                label = "CLEARED BALANCE",
                value = "${formatCurrency()}${String.format(Locale.US, "%,.2f", clearedBalance)}"
            )

            OutlinedButton(
                onClick = onRunReconciliation,
                enabled = !isSyncing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(tokens.buttonHeight),
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = SolidColor(BorderGray))
            ) {
                Icon(
                    imageVector = Icons.Outlined.Sync,
                    contentDescription = null,
                    tint = TitleColor,
                    modifier = Modifier.size(tokens.iconSize)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = if (isSyncing) "Syncing..." else "Run Reconciliation",
                    color = TitleColor,
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}