@file:Suppress("unused", "AssignedValueIsNeverRead", "DEPRECATION")

package com.cuso.tailor.view.home.inventory.expense_management.expense_type

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
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
import com.cuso.tailor.model.hr.ExpenseTypeDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.HrViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ExpenseTypeScreen(
    viewModel: HrViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
    onExpenseTypeClick: (ExpenseTypeDto) -> Unit = {},
    onAddNewExpenseType: () -> Unit = {},
    onEditExpenseType: (String) -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val expenseTypes by viewModel.expenseTypes.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingExpenseTypes.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.fetchExpenseTypes()
    }

    val filteredTypes = remember(expenseTypes, searchQuery) {
        if (searchQuery.isBlank()) {
            expenseTypes
        } else {
            expenseTypes.filter { type ->
                (type.name?.contains(searchQuery, ignoreCase = true) == true) ||
                        (type.description?.contains(searchQuery, ignoreCase = true) == true)
            }
        }
    }
    var itemToDelete by remember { mutableStateOf<ExpenseTypeDto?>(null) }
    itemToDelete?.let { item ->
        DeleteModel(
            title = "Delete Expense Type",
            message = "You're going to delete the \"${item.name ?: "Expense Type"}\". Are you sure?",
            onDismiss = { itemToDelete = null },
            onDelete = {
                viewModel.deleteExpenseType(item.id) {
                    viewModel.fetchExpenseTypes()
                }
                itemToDelete = null
            }
        )
    }

    FabScaffold(
        modifier = Modifier.fillMaxSize(),
        fab = FabConfig(
            label = "Add Expense Type",
            icon = Icons.Default.Add,
            onClick = onAddNewExpenseType
        )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                TitleBar(
                    title = "Exp Type List",
                    onClose = onClose
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                SearchFilterBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search types...",
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
                } else if (filteredTypes.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No expense types found",
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = mutedText,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        itemsIndexed(filteredTypes, key = { _, item -> item.id }) { index, item ->
                            ExpenseTypeItemCard(
                                index = index + 1,
                                item = item,
                                onClick = { onExpenseTypeClick(item) },
                                onEdit = { onEditExpenseType(item.id) },
                                onDelete = {
                                    itemToDelete = item
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

//  Clean, Reusable and Production-Ready using ActionDropdownMenu & DataCard
@Composable
fun ExpenseTypeItemCard(
    index: Int,
    item: ExpenseTypeDto,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val tokens = LocalAppTokens.current

    val codeText = "EXP-${index.toString().padStart(3, '0')}"
    val statusText = item.status?.ifBlank { "Active" } ?: "Active"
    val (badgeBg, badgeColor) = if (statusText.equals("Active", ignoreCase = true)) {
        greenBg to greentext
    } else {
        grey_border to TextSecondary
    }

    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }
    val formattedLimit = currencyFormatter.format(item.defaultLimit)

    val menuActions = remember(item.id) {
        listOf(
            MenuAction(
                label = "Edit",
                tint = Primary,
                textColor = TextPrimary,
                onClick = onEdit
            ),
            MenuAction(
                label = "Delete",
                tint = redText,
                textColor = redText,
                onClick = onDelete
            )
        )
    }

    DataCard(
        item = item,
        onClick = { onClick() },
        showDivider = false,
        headerContent = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = codeText,
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Normal,
                    color = headerGrey
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusBadge(
                        text = statusText,
                        bgColor = badgeBg,
                        textColor = badgeColor
                    )

                    Spacer(Modifier.width(6.dp))

                    ActionDropdownMenu(
                        actions = menuActions,
                        icon = Icons.Default.MoreVert
                    )
                }
            }
        },
        title = item.name ?: "Unnamed Type",
        titleFontWeight = FontWeight.Medium,
        titleColor = TextPrimary,
        content = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Default Limit: ",
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Normal,
                    color = TextSecondary
                )
                Text(
                    text = formattedLimit,
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Medium,
                    color = Primary
                )
            }
        }
    )
}