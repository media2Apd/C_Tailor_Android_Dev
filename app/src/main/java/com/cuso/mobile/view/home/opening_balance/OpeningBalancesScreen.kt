@file:Suppress(
    "UNUSED_PARAMETER",
    "unused",
    "UNCHECKED_CAST",
    "DEPRECATION",
    "AssignedValueIsNeverRead",
    "GrazieInspection",
    "SpellCheckingInspection",
    "unusedvariable"
)
package com.cuso.mobile.view.home.opening_balance

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.BluePrimary
import com.cuso.mobile.ui.theme.mutedText
import com.cuso.mobile.ui.theme.title_color
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.DataCard
import com.cuso.mobile.view.composable.DatePickerField
import com.cuso.mobile.view.composable.SearchFilterBar
import com.cuso.mobile.view.composable.SheetValue
import com.cuso.mobile.view.composable.SmoothBottomSheet
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.view.composable.blurScrim
import com.cuso.mobile.view.composable.FormDropdown
import com.cuso.mobile.view.composable.FormLabel
import com.cuso.mobile.view.composable.FormTextField

// ─────────────────────────────────────────────────────────────
// Local data models for this screen
// ─────────────────────────────────────────────────────────────

data class OpeningBalanceAccount(
    val id: String,
    val name: String,
    val code: String,
    val debit: String = "",
    val credit: String = ""
)

data class OpeningBalanceCategory(
    val id: String,
    val name: String,
    val code: String,
    val accounts: List<OpeningBalanceAccount>
)

data class AccountCategoryFormData(
    val categoryType: String = "",
    val categoryName: String = "",
    val code: String = ""
)

private fun sampleCategories(): List<OpeningBalanceCategory> = listOf(
    OpeningBalanceCategory("cat_ar", "Accounts Receivable", "1001", emptyList()),
    OpeningBalanceCategory("cat_ap", "Accounts Payable", "1001", emptyList()),
    OpeningBalanceCategory("cat_asset", "Asset", "1001", emptyList()),
    OpeningBalanceCategory(
        "cat_expense", "Expense", "1001",
        listOf(OpeningBalanceAccount("acc_bank_fees", "Bank Fees and Charges", "1001"))
    ),
    OpeningBalanceCategory("cat_bank", "Bank", "1001", emptyList()),
    OpeningBalanceCategory("cat_liability", "Liability", "1001", emptyList())
)

@Composable
fun OpeningBalancesScreen(
    navController: NavController,
    onBack: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current

    var categories by remember { mutableStateOf(sampleCategories()) }
    // Tracks which category is currently open in the detail view. Null means
    // the top-level list screen is shown.
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var migrationDate by remember { mutableStateOf("Select date") }

    // Tracks which category the "Add Account Category" sheet is being opened for
    var addCategoryTargetId by remember { mutableStateOf<String?>(null) }
    var addSheetState by remember { mutableStateOf(SheetValue.Hidden) }
    var addSheetBlur by remember { mutableStateOf(0.dp) }

    val filteredCategories = categories.filter { c ->
        searchQuery.isBlank() || c.name.contains(searchQuery, ignoreCase = true)
    }

    val selectedCategory = categories.firstOrNull { it.id == selectedCategoryId }

    Scaffold(
        topBar = {
            TitleBar(
                "Opening Balances",
                // Close button steps back from the detail view to the list first,
                // and only exits the screen when already on the list.
                onClose = { if (selectedCategory != null) selectedCategoryId = null else onBack() }
            )
        },
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .blurScrim(addSheetBlur)
        ) {
            // Import action row - shared by both the list and detail views
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = tokens.screenPadding, vertical = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { }
                ) {
                    Icon(Icons.Default.Upload, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(tokens.iconSize * 0.7f))
                    Spacer(Modifier.width(4.dp))
                    Text("Import", color = BluePrimary, fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium)
                }
            }

            if (selectedCategory != null) {
                OpeningBalanceCategoryDetailContent(
                    category = selectedCategory,
                    onAccountChange = { updatedAccount ->
                        categories = categories.map { c ->
                            if (c.id == selectedCategory.id) {
                                c.copy(accounts = c.accounts.map { if (it.id == updatedAccount.id) updatedAccount else it })
                            } else c
                        }
                    },
                    onAddAccountCategory = {
                        addCategoryTargetId = selectedCategory.id
                        addSheetState = SheetValue.Expanded
                    }
                )
            } else {
                SearchFilterBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search Opening Balances...",
                    accentColor = BluePrimary
                )

                Spacer(Modifier.height(16.dp))

                // Migration Details section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = tokens.screenPadding)
                ) {
                    Text("Migration Details", fontSize = tokens.bodyMedium, color = mutedText)
                    Spacer(Modifier.height(8.dp))
                    DatePickerField(
                        value = migrationDate,
                        onDateSelected = { migrationDate = it }
                    )
                }

                Spacer(Modifier.height(12.dp))

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(filteredCategories) { category ->
                        OpeningBalanceCategoryCard(
                            category = category,
                            onDebitChange = { newDebit ->
                                categories = categories.map { c ->
                                    if (c.id == category.id) {
                                        val existing = c.accounts.firstOrNull()
                                        val updated = existing?.copy(debit = newDebit)
                                            ?: OpeningBalanceAccount(c.id, c.name, c.code, debit = newDebit)
                                        c.copy(accounts = listOf(updated) + c.accounts.drop(1))
                                    } else c
                                }
                            },
                            onCreditChange = { newCredit ->
                                categories = categories.map { c ->
                                    if (c.id == category.id) {
                                        val existing = c.accounts.firstOrNull()
                                        val updated = existing?.copy(credit = newCredit)
                                            ?: OpeningBalanceAccount(c.id, c.name, c.code, credit = newCredit)
                                        c.copy(accounts = listOf(updated) + c.accounts.drop(1))
                                    } else c
                                }
                            },
                            onClick = { selectedCategoryId = category.id }
                        )
                    }
                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }

        // Add Account Category BottomSheet - reachable from the detail view only
        SmoothBottomSheet(
            state = addSheetState,
            onStateChange = { newState ->
                addSheetState = newState
                if (newState == SheetValue.Hidden) {
                    addCategoryTargetId = null
                    addSheetBlur = 0.dp
                }
            },
            peekHeight = 340.dp,
            topInset = 66.dp,
            sheetBackgroundColor = whiteBg,
            collapsedCornerRadius = 24.dp,
            dragCloseEnabled = true,
            scrollableContent = true,
            onDismissRequest = {
                addSheetState = SheetValue.Hidden
                addCategoryTargetId = null
                addSheetBlur = 0.dp
            },
            onBlurScrimChange = { r, _ -> addSheetBlur = r }
        ) {
            AddAccountCategorySheetContent(
                onDismiss = {
                    addSheetState = SheetValue.Hidden
                    addCategoryTargetId = null
                    addSheetBlur = 0.dp
                },
                onSubmit = { formData ->
                    val targetId = addCategoryTargetId
                    if (targetId != null) {
                        categories = categories.map { c ->
                            if (c.id == targetId) {
                                c.copy(
                                    accounts = c.accounts + OpeningBalanceAccount(
                                        id = "acc_${System.currentTimeMillis()}",
                                        name = formData.categoryName,
                                        code = formData.code.ifBlank { c.code }
                                    )
                                )
                            } else c
                        }
                    }
                    addSheetState = SheetValue.Hidden
                    addCategoryTargetId = null
                    addSheetBlur = 0.dp
                }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// List Card (top-level list screen) - flat row with Debit/Credit fields,
// clicking anywhere on the header row navigates into the detail view
// ─────────────────────────────────────────────────────────────

@Composable
private fun OpeningBalanceCategoryCard(
    category: OpeningBalanceCategory,
    onDebitChange: (String) -> Unit,
    onCreditChange: (String) -> Unit,
    onClick: () -> Unit
) {
    val tokens = LocalAppTokens.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = tokens.screenPadding, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(category.name, fontSize = tokens.bodyMedium, fontWeight = FontWeight.SemiBold, color = title_color)
            Row(verticalAlignment = Alignment.CenterVertically) {
                CodeBadge(category.code)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = mutedText)
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Debit", fontSize = tokens.caption, color = mutedText)
                Spacer(Modifier.height(6.dp))
                CurrencyField(
                    value = category.accounts.firstOrNull()?.debit.orEmpty(),
                    onValueChange = onDebitChange
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Credit", fontSize = tokens.caption, color = mutedText)
                Spacer(Modifier.height(6.dp))
                CurrencyField(
                    value = category.accounts.firstOrNull()?.credit.orEmpty(),
                    onValueChange = onCreditChange
                )
            }
        }

        Spacer(Modifier.height(4.dp))
        HorizontalDivider(color = Color(0xFFF0F0F0))
    }
}

// ─────────────────────────────────────────────────────────────
// Detail view content (shown when a category card is tapped) - single
// category accordion header, its account rows, add-account link and
// the totals summary box
// ─────────────────────────────────────────────────────────────

@Composable
private fun OpeningBalanceCategoryDetailContent(
    category: OpeningBalanceCategory,
    onAccountChange: (OpeningBalanceAccount) -> Unit,
    onAddAccountCategory: () -> Unit
) {
    val tokens = LocalAppTokens.current

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = tokens.screenPadding)
                .padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(category.name, fontSize = tokens.bodyMedium, fontWeight = FontWeight.SemiBold, color = title_color)
            Icon(Icons.Default.KeyboardArrowUp, contentDescription = null, tint = mutedText)
        }
        HorizontalDivider(color = Color(0xFFF0F0F0))

        // Category with no sub-accounts yet still shows a single editable
        // debit/credit row representing the category itself
        val accounts = if (category.accounts.isEmpty()) {
            listOf(OpeningBalanceAccount(category.id, category.name, category.code))
        } else category.accounts

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(accounts) { account ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = tokens.screenPadding, vertical = 12.dp)
                ) {
                    OpeningBalanceAccountRow(account = account, onChange = onAccountChange)
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = tokens.screenPadding)
                ) {
                    Text(
                        "+ Add Account Category",
                        color = BluePrimary,
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .align(Alignment.End)
                            .clickable { onAddAccountCategory() }
                    )

                    Spacer(Modifier.height(16.dp))

                    val totalDebit = category.accounts.sumOf { it.debit.toDoubleOrNull() ?: 0.0 }
                    val totalCredit = category.accounts.sumOf { it.credit.toDoubleOrNull() ?: 0.0 }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFEEF0FF), RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Debit", fontSize = tokens.bodySmall, color = title_color)
                            Text(String.format("%.2f", totalDebit), fontSize = tokens.bodySmall, color = title_color)
                        }
                        Spacer(Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total credit", fontSize = tokens.bodySmall, color = title_color)
                            Text(String.format("%.2f", totalCredit), fontSize = tokens.bodySmall, color = title_color)
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Single account row: name, code badge, debit/credit fields
// ─────────────────────────────────────────────────────────────

@Composable
private fun OpeningBalanceAccountRow(
    account: OpeningBalanceAccount,
    onChange: (OpeningBalanceAccount) -> Unit
) {
    val tokens = LocalAppTokens.current

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(account.name, fontSize = tokens.bodyMedium, color = title_color)
            Row(verticalAlignment = Alignment.CenterVertically) {
                CodeBadge(account.code)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = mutedText)
            }
        }

        Spacer(Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Debit", fontSize = tokens.caption, color = mutedText)
                Spacer(Modifier.height(6.dp))
                CurrencyField(
                    value = account.debit,
                    onValueChange = { onChange(account.copy(debit = it)) }
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Credit", fontSize = tokens.caption, color = mutedText)
                Spacer(Modifier.height(6.dp))
                CurrencyField(
                    value = account.credit,
                    onValueChange = { onChange(account.copy(credit = it)) }
                )
            }
        }
    }
}

@Composable
private fun CurrencyField(value: String, onValueChange: (String) -> Unit) {
    val tokens = LocalAppTokens.current
    val interactionSource = remember { MutableInteractionSource() }

    val colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = BluePrimary,
        unfocusedBorderColor = Color(0xFFE3E4E8)
    )

    BasicTextField(
        value = value,
        onValueChange = { new -> if (new.all { it.isDigit() || it == '.' }) onValueChange(new) },
        modifier = Modifier.fillMaxWidth().height(48.dp),
        singleLine = true,
        textStyle = androidx.compose.ui.text.TextStyle(
            fontSize = tokens.bodySmall,
            color = title_color
        ),
        interactionSource = interactionSource,
        cursorBrush = SolidColor(BluePrimary)
    ) { innerTextField ->
        OutlinedTextFieldDefaults.DecorationBox(
            value = value,
            innerTextField = innerTextField,
            enabled = true,
            singleLine = true,
            visualTransformation = VisualTransformation.None,
            interactionSource = interactionSource,
            placeholder = { Text("\u20B90.00", color = Color(0xFFB0B3BD), fontSize = tokens.bodySmall) },
            colors = colors,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
            container = {
                OutlinedTextFieldDefaults.Container(
                    enabled = true,
                    isError = false,
                    interactionSource = interactionSource,
                    colors = colors,
                    shape = RoundedCornerShape(10.dp)
                )
            }
        )
    }
}

@Composable
private fun CodeBadge(code: String) {
    val tokens = LocalAppTokens.current
    Box(
        modifier = Modifier
            .background(Color(0xFFF3F4F6), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(code, fontSize = tokens.caption, color = mutedText)
    }
}

// ─────────────────────────────────────────────────────────────
// Add Account Category bottom sheet content
// ─────────────────────────────────────────────────────────────

@Composable
private fun AddAccountCategorySheetContent(
    onDismiss: () -> Unit,
    onSubmit: (AccountCategoryFormData) -> Unit
) {
    val tokens = LocalAppTokens.current

    var categoryType by remember { mutableStateOf("") }
    var categoryName by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var typeExpanded by remember { mutableStateOf(false) }

    val categoryTypeOptions = listOf("Account Receivable", "Account Payable", "Asset", "Expense", "Bank", "Liability")

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = tokens.screenPadding).padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            "Add Account Category",
            fontSize = tokens.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = title_color,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Column {
            FormLabel("Category Type", isRequired = true)
            FormDropdown(
                label = "Category Type",
                value = categoryType.ifBlank { "Select category type" },
                expanded = typeExpanded,
                onExpandChange = { typeExpanded = it },
                options = categoryTypeOptions,
                onOptionSelected = { categoryType = it }
            )
        }

        Column {
            FormLabel("Category Name", isRequired = true)
            FormTextField(value = categoryName, onValueChange = { categoryName = it }, placeholder = "Enter category name")
        }

        Column {
            FormLabel("Code")
            FormTextField(value = code, onValueChange = { code = it }, placeholder = "Enter code")
        }

        Spacer(Modifier.height(20.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f).height(tokens.buttonHeight),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color(0xFFD1D5DB))
            ) {
                Text("Cancel", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = Color(0xFF374151))
            }
            Button(
                onClick = {
                    if (categoryName.isBlank()) return@Button
                    onSubmit(AccountCategoryFormData(categoryType, categoryName, code))
                },
                modifier = Modifier.weight(1f).height(tokens.buttonHeight),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                Text("Add", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = whiteBg)
            }
        }
    }
}