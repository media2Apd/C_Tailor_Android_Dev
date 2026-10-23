@file:Suppress("unused", "UNUSED_PARAMETER","DEPRECATION", "AssignedValueIsNeverRead")


package com.cuso.tailor.view.home.inventory.expense_management.new_claims

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
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
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.HrViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun NewClaimsScreen(
    viewModel: HrViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
    onClaimClick: (ExpenseClaimDto) -> Unit = {},
    onEditClaimClick: (ExpenseClaimDto) -> Unit = {},
    onNavigateToSubmitClaim: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current

    val expenseClaims by viewModel.expenseClaims.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingExpenseClaims.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }

    // State for managing deletion modal
    var claimToDelete by remember { mutableStateOf<ExpenseClaimDto?>(null) }

    LaunchedEffect(Unit) {
        viewModel.fetchExpenseClaims(page = 1, limit = 10, status = null, search = null)
    }

    val filteredClaims = remember(expenseClaims, searchQuery) {
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

    // Delete Confirmation Dialog
    claimToDelete?.let { claim ->
        DeleteModel(
            title = "Delete Expense Claim",
            message = "Are you sure you want to delete the expense claim \"${claim.title ?: "this claim"}\"? This action cannot be undone.",
            checkboxText = "I confirm this deletion",
            cancelText = "No, Keep It",
            confirmText = "Yes, Delete!",
            onDismiss = { claimToDelete = null },
            onDelete = {
                viewModel.deleteExpenseClaim(
                    claimId = claim.id,
                    onSuccess = { msg ->
                        DynamicIslandManager.showSuccess(msg)
                        claimToDelete = null
                    },
                    onError = { err ->
                        DynamicIslandManager.showError(err)
                        claimToDelete = null
                    }
                )
            }
        )
    }

    FabScaffold(
        modifier = Modifier.fillMaxSize(),
        fab = FabConfig(
            label = "Add Expense",
            icon = Icons.Default.Add,
            onClick = onNavigateToSubmitClaim
        )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                TitleBar(
                    title = "New Claims",
                    onClose = onClose
                )
            }
        ) { paddingValues ->
            Column(
                Modifier
                    .fillMaxSize()
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
                } else if (filteredClaims.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No claims found",
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
                        items(filteredClaims, key = { it.id }) { claim ->
                            NewClaimItemCard(
                                claim = claim,
                                onClick = { onClaimClick(claim) },
                                onEditClick = { onEditClaimClick(claim) },
                                onDeleteClick = { claimToDelete = claim }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NewClaimItemCard(
    claim: ExpenseClaimDto,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val tokens = LocalAppTokens.current

    val (badgeBg, badgeText) = getClaimStatusPalette(claim.status)
    val displayStatus = formatClaimStatus(claim.status)
    val displayDate = formatClaimDate(claim.expenseDate ?: claim.createdAt)
    val formattedAmount = formatClaimCurrency(claim.totalAmount)

    DataCard(
        item = claim,
        onClick = { onClick() },
        showDivider = false,
        actions = listOf(
            MenuAction(
                label = "View",
                icon = Icons.Outlined.Visibility,
                tint = Primary,
                textColor = TextPrimary,
                onClick = onClick
            ),
            MenuAction(
                label = "Edit",
                icon = Icons.Outlined.Edit,
                tint = Primary,
                textColor = TextPrimary,
                onClick = onEditClick
            ),
            MenuAction(
                label = "Delete",
                icon = Icons.Outlined.Delete,
                tint = redText,
                textColor = redText,
                onClick = onDeleteClick
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
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Normal,
                    color = headerGrey
                )

                StatusBadge(
                    text = displayStatus,
                    bgColor = badgeBg,
                    textColor = badgeText
                )
            }
        },
        title = (claim.title ?: "Untitled Claim").uppercase(),
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

private fun getClaimStatusPalette(status: String?): Pair<Color, Color> {
    val safeStatus = status.orEmpty()
    return when {
        safeStatus.contains("approved", ignoreCase = true) -> greenBg to greentext
        safeStatus.contains("rejected", ignoreCase = true) -> redBg to redText
        safeStatus.contains("pending", ignoreCase = true) -> activity_purple_bg to blueText
        else -> grey_border to TextSecondary
    }
}

private fun formatClaimStatus(status: String?): String {
    val safeStatus = status.orEmpty()
    return when {
        safeStatus.contains("approved", ignoreCase = true) -> "Approved"
        safeStatus.contains("rejected", ignoreCase = true) -> "Rejected"
        safeStatus.contains("pending", ignoreCase = true) -> "Pending"
        else -> safeStatus.replace("_", " ")
    }
}

private fun formatClaimCurrency(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    return formatter.format(amount)
}

private fun formatClaimDate(isoDateString: String?): String {
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