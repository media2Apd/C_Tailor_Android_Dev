@file:Suppress("AssignedValueIsNeverRead")
package com.cuso.tailor.view.home.finance.funds.bankAccounts

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.finance.BankAccountItemDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.FinanceViewModel
import java.util.Locale

@Composable
fun BankAccountsListScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToDetail: (String) -> Unit
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current

    val accounts by viewModel.bankAccounts.collectAsState()
    val isLoading by viewModel.isLoadingBankAccounts.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    var accountToArchive by remember { mutableStateOf<BankAccountItemDto?>(null) }

    LaunchedEffect(Unit) {
        viewModel.fetchBankAccounts()
    }

    // Reusable Archive Confirmation Dialog
    if (accountToArchive != null) {
        ArchiveBankAccountDialog(
            account = accountToArchive!!,
            viewModel = viewModel,
            onDismiss = { accountToArchive = null },
            onArchiveSuccess = {
                accountToArchive = null
                viewModel.fetchBankAccounts()
            }
        )
    }

    FabScaffold(
        modifier = Modifier.fillMaxSize(),
        fab = FabConfig(
            label = "Add Bank Account",
            icon = Icons.Default.Add,
            onClick = onNavigateToCreate
        )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                Row(modifier = Modifier.fillMaxWidth()) {
                    TitleBar(title = "Bank Accounts", onClose = onBack)
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Search and Filter Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(tokens.fieldHeight)
                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.6f))
                            .background(whiteBg)
                            .border(1.dp, grey_border, RoundedCornerShape(tokens.cardCornerRadius * 0.6f))
                            .padding(horizontal = tokens.cardPadding * 0.6f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = null,
                                tint = mutedText,
                                modifier = Modifier.size(tokens.iconSize)
                            )
                            Spacer(Modifier.width(tokens.extraPadding * 0.8f))
                            FormTextField(
                                value = searchQuery,
                                onValueChange = {
                                    searchQuery = it
                                    viewModel.fetchBankAccounts(it)
                                },
                                placeholder = "Search SO number, customer...",
                                containerColor = Color.Transparent,
                                borderColor = Color.Transparent,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(tokens.fieldHeight)
                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.6f))
                            .background(whiteBg)
                            .border(1.dp, grey_border, RoundedCornerShape(tokens.cardCornerRadius * 0.6f))
                            .clickable { },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.FilterList,
                            contentDescription = "Filter",
                            tint = TitleColor,
                            modifier = Modifier.size(tokens.iconSize)
                        )
                    }
                }

                if (isLoading && accounts.isEmpty()) {
                    ListSkeleton()
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(accounts, key = { it.id }) { item ->
                            val isActive = item.status.equals("Active", ignoreCase = true)
                            val maskedNum = item.accountNumberMasked.ifBlank {
                                if (item.accountNumber.length > 4) "****" + item.accountNumber.takeLast(4) else item.accountNumber
                            }

                            // Reusing generic DataCard component
                            DataCard(
                                item = item,
                                title = item.accountDisplayName.ifBlank { item.bankName },
                                titleColor = if (item.isDefault) Primary else TitleColor,
                                titleFontWeight = if (item.isDefault) FontWeight.SemiBold else FontWeight.Medium,
                                subtitle = item.bankName,
                                topBadgeText = if (isActive) "Active" else "Inactive",
                                topBadgeBgColor = if (isActive) greenBg else redBg,
                                topBadgeTextColor = if (isActive) greentext else redText,
                                topBadgeInline = true,
                                showActionsInHeader = false,
                                actions = listOf(
                                    MenuAction("View Details", onClick = { onNavigateToDetail(item.id) }),
                                    MenuAction("Edit Account", onClick = { onNavigateToDetail(item.id) }),
                                    MenuAction(
                                        label = "Set as Default",
                                        enabled = !item.isDefault,
                                        textColor = if (item.isDefault) mutedText else TitleColor,
                                        onClick = {
                                            viewModel.setBankAccountAsDefault(item.id) { msg ->
                                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    ),
                                    MenuAction("Archive Account", textColor = redText, onClick = { accountToArchive = item })
                                ),
                                onClick = { onNavigateToDetail(item.id) },
                                content = {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        // Account Number Row
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "ACCOUNT NUMBER",
                                                fontSize = tokens.label,
                                                fontWeight = FontWeight.Medium,
                                                color = mutedText
                                            )
                                            Text(
                                                text = maskedNum,
                                                fontSize = tokens.bodySmall,
                                                fontWeight = FontWeight.Medium,
                                                color = TitleColor
                                            )
                                        }

                                        Spacer(Modifier.height(tokens.extraPadding * 0.4f))

                                        // Last Synced Status Line
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Outlined.Sync,
                                                contentDescription = null,
                                                tint = mutedText,
                                                modifier = Modifier.size(tokens.iconSize * 0.75f)
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Text(
                                                text = "Last synced recently",
                                                fontSize = tokens.label,
                                                color = mutedText
                                            )
                                        }

                                        Spacer(Modifier.height(tokens.extraPadding * 0.8f))

                                        // Balance & Type Container
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                                .background(Primary_background)
                                                .padding(tokens.cardPadding * 0.6f)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text(
                                                        text = "CURRENT BALANCE",
                                                        fontSize = tokens.label,
                                                        color = mutedText
                                                    )
                                                    Spacer(Modifier.height(tokens.extraPadding * 0.2f))
                                                    Text(
                                                        text = "₹${String.format(Locale.US, "%,.2f", item.chartOfAccountId?.openingBalance ?: 0.0)}",
                                                        fontSize = tokens.bodyMedium,
                                                        fontWeight = FontWeight.Medium,
                                                        color = TitleColor
                                                    )
                                                }

                                                Column(horizontalAlignment = Alignment.End) {
                                                    Text(
                                                        text = "TYPE",
                                                        fontSize = tokens.label,
                                                        color = mutedText
                                                    )
                                                    Spacer(Modifier.height(tokens.extraPadding * 0.2f))
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(primary_light)
                                                            .border(1.dp, light_blue_border, RoundedCornerShape(6.dp))
                                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(
                                                            text = item.accountType,
                                                            fontSize = tokens.label,
                                                            color = Primary,
                                                            fontWeight = FontWeight.Medium
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Reusable Confirmation Dialog for Archiving Bank Accounts
 */
@Composable
fun ArchiveBankAccountDialog(
    account: BankAccountItemDto,
    viewModel: FinanceViewModel,
    onDismiss: () -> Unit,
    onArchiveSuccess: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current
    val isSubmitting by viewModel.isSubmitting.collectAsState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            colors = CardDefaults.cardColors(containerColor = whiteBg),
            border = BorderStroke(1.dp, BorderGray)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(tokens.cardPadding)
            ) {
                Text(
                    text = "Archive Bank Account",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = TitleColor
                )

                Spacer(Modifier.height(tokens.extraPadding * 0.8f))

                Text(
                    text = "Are you sure you want to archive this account? Accounts with posted general ledger entries cannot be purged.",
                    fontSize = tokens.bodySmall,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )

                Spacer(Modifier.height(tokens.extraPadding * 1.5f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.height(tokens.buttonHeight * 0.9f),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                        border = BorderStroke(1.dp, BorderGray),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = whiteBg)
                    ) {
                        Text(
                            text = "Cancel",
                            color = TitleColor,
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(Modifier.width(tokens.extraPadding * 0.8f))

                    Button(
                        onClick = {
                            viewModel.archiveBankAccount(account.id) { successMsg ->
                                Toast.makeText(context, successMsg, Toast.LENGTH_SHORT).show()
                                onArchiveSuccess()
                            }
                        },
                        enabled = !isSubmitting,
                        modifier = Modifier.height(tokens.buttonHeight * 0.9f),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                        colors = ButtonDefaults.buttonColors(containerColor = redText)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(tokens.iconSize)
                            )
                        } else {
                            Text(
                                text = "Archive",
                                color = Color.White,
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}