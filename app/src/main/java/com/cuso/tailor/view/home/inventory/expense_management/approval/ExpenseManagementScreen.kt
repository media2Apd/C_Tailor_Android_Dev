@file:Suppress("unused", "UNUSED_PARAMETER", "DEPRECATION")

package com.cuso.tailor.view.home.inventory.expense_management.approval

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.hr.ExpenseClaimDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.DataCard
import com.cuso.tailor.view.composable.ListSkeleton
import com.cuso.tailor.view.composable.MenuAction
import com.cuso.tailor.view.composable.SearchFilterBar
import com.cuso.tailor.view.composable.StatusBadge
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.viewmodel.HrViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ExpenseManagementScreen(
    viewModel: HrViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
    onViewClaimDetail: (String) -> Unit = {}
) {
    val tokens = LocalAppTokens.current

    val expenseClaims by viewModel.expenseClaims.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingExpenseClaims.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.fetchExpenseClaims()
    }

    val filteredExpenses = remember(expenseClaims, searchQuery) {
        if (searchQuery.isBlank()) {
            expenseClaims
        } else {
            expenseClaims.filter { claim ->
                claim.title?.contains(searchQuery, ignoreCase = true) == true ||
                        claim.claimNumber?.contains(searchQuery, ignoreCase = true) == true ||
                        (claim.category?.name?.contains(searchQuery, ignoreCase = true) == true) ||
                        (claim.employee?.fullName?.contains(searchQuery, ignoreCase = true) == true)
            }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TitleBar(
                title = "Expense Management",
                onClose = onClose
            )
        }
    ) { paddingValues ->
        Column(
            Modifier.fillMaxSize()
                .padding(paddingValues)
        ) {
            SearchFilterBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Search expenses...",
                showFilterIcon = true,
                filterCount = 0,
                onFilterClick = { },
                accentColor = Primary,
                borderColor = sectionBorder,
                textSecondaryColor = iconMuted,
                height = tokens.fieldHeight
            )
            if (isLoading) {
                ListSkeleton()
            } else if (filteredExpenses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No expenses found",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = mutedText,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredExpenses, key = { it.id }) { claim ->
                        ExpenseItemCard(
                            claim = claim,
                            onViewClick = {
                                if (claim.id.isNotBlank()) {
                                    onViewClaimDetail(claim.id)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExpenseItemCard(
    claim: ExpenseClaimDto,
    onViewClick: () -> Unit
) {
    val tokens = LocalAppTokens.current

    val (badgeBg, badgeText) = getStatusColorPalette(claim.status)
    val displayStatus = formatExpenseStatus(claim.status)
    val displayDate = formatExpenseDate(claim.expenseDate ?: claim.createdAt)
    val formattedAmount = formatCurrency(claim.totalAmount)

    DataCard(
        item = claim,
        onClick = { onViewClick() },
        showDivider = false,
        actions = listOf(
            MenuAction(
                label = "View",
                icon = Icons.Outlined.Visibility,
                tint = Primary,
                textColor = TextPrimary,
                onClick = onViewClick
            )
        ),
        headerContent = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = claim.claimNumber.orEmpty().ifBlank { "EXP-${claim.id.takeLast(4).uppercase()}" },
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Normal,
                    color = headerGrey
                )

                StatusBadge(
                    text = displayStatus,
                    bgColor = badgeBg,
                    textColor = badgeText,
                    showDot = false,
                    cornerRadius = tokens.cardCornerRadius * 0.4f
                )
            }
        },
        title = claim.title ?: "Untitled Claim",
        titleFontWeight = FontWeight.Medium,
        titleColor = TextPrimary,
        content = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Category: ${claim.category?.name ?: "N/A"}",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Normal,
                        color = TextSecondary
                    )
                    Text(
                        text = "Employee: ${claim.employee?.fullName ?: "N/A"}",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Normal,
                        color = TextSecondary
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = formattedAmount,
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Primary
                    )
                    Text(
                        text = displayDate,
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Normal,
                        color = headerGrey
                    )
                }
            }
        }
    )
}

private fun getStatusColorPalette(status: String?): Pair<Color, Color> {
    val safeStatus = status.orEmpty()
    return when {
        safeStatus.contains("approved", ignoreCase = true) -> greenBg to greentext
        safeStatus.contains("rejected", ignoreCase = true) -> redBg to redText
        safeStatus.contains("pending", ignoreCase = true) -> activity_purple_bg to blueText
        else -> grey_border to TextSecondary
    }
}

private fun formatExpenseStatus(status: String?): String {
    val safeStatus = status.orEmpty()
    return when {
        safeStatus.contains("approved", ignoreCase = true) -> "Approved"
        safeStatus.contains("rejected", ignoreCase = true) -> "Rejected"
        safeStatus.contains("pending", ignoreCase = true) -> "Pending"
        else -> safeStatus.replace("_", " ")
    }
}

private fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    return formatter.format(amount)
}

private fun formatExpenseDate(isoDateString: String?): String {
    if (isoDateString.isNullOrBlank()) return ""
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val date = inputFormat.parse(isoDateString)
        val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        if (date != null) outputFormat.format(date) else ""
    } catch (_: Exception) {
        isoDateString.take(10)
    }
}