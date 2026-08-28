@file:Suppress(
    "UNUSED_PARAMETER",
    "UNUSED",
    "RedundantSuppression",
    "unused_variable",
    "AssignedValueIsNeverRead",
    "VariableNeverRead"
)

package com.cuso.mobile.view.home.finance.finance_core.chart_of_accounts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.model.finance.ChartOfAccountItem
import com.cuso.mobile.model.finance.indentLevel
import com.cuso.mobile.ui.theme.BluePrimary
import com.cuso.mobile.ui.theme.BorderGray
import com.cuso.mobile.ui.theme.Primary
import com.cuso.mobile.ui.theme.TextSecondary
import com.cuso.mobile.ui.theme.greenBg
import com.cuso.mobile.ui.theme.greentext
import com.cuso.mobile.ui.theme.title_border
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.DataCard
import com.cuso.mobile.view.composable.DeleteModel
import com.cuso.mobile.view.composable.DynamicIslandError
import com.cuso.mobile.view.composable.DynamicIslandSuccess
import com.cuso.mobile.view.composable.FabConfig
import com.cuso.mobile.view.composable.FabScaffold
import com.cuso.mobile.view.composable.FieldValidator
import com.cuso.mobile.view.composable.FormDropdown
import com.cuso.mobile.view.composable.FormLabel
import com.cuso.mobile.view.composable.FormTextField
import com.cuso.mobile.view.composable.ListSkeleton
import com.cuso.mobile.view.composable.MenuAction
import com.cuso.mobile.view.composable.ScreenBreadcrumb
import com.cuso.mobile.view.composable.SearchFilterBar
import com.cuso.mobile.view.composable.StepNavigationFab
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.view.composable.TrailingFabAction
import com.cuso.mobile.view.composable.ValidationField
import com.cuso.mobile.viewmodel.CreateAccountState
import com.cuso.mobile.viewmodel.DeleteAccountState
import com.cuso.mobile.viewmodel.FinanceViewModel
import com.cuso.mobile.viewmodel.UpdateAccountState
import kotlinx.coroutines.delay

private val GreenBg = greenBg
private val GreenText = greentext
private val OrangeBg = Color(0xFFFDEFE0)
private val OrangeText = Color(0xFFE08A2C)
private val accountTypeOptions = listOf("Asset", "Liability", "Equity", "Income", "Expense")

enum class AccountScreenMode { CREATE, VIEW, EDIT }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartOfAccountScreen(
    onClose: () -> Unit = {},
    onEditAccount: (ChartOfAccountItem) -> Unit = {},
    onDeleteAccount: (ChartOfAccountItem) -> Unit = {},
    financeViewModel: FinanceViewModel = hiltViewModel(),
    onBreadcrumbClick: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current

    val accounts by financeViewModel.chartOfAccounts.collectAsStateWithLifecycle()
    val isLoading by financeViewModel.isLoadingChartOfAccounts.collectAsStateWithLifecycle()
    val errorMessage by financeViewModel.chartOfAccountsError.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var debouncedSearchQuery by remember { mutableStateOf("") }
    var showAddAccount by remember { mutableStateOf(false) }

    var screenMode by remember { mutableStateOf(AccountScreenMode.CREATE) }
    var selectedAccount by remember { mutableStateOf<ChartOfAccountItem?>(null) }

    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorToastMessage by remember { mutableStateOf<String?>(null) }

    var accountPendingDelete by remember { mutableStateOf<ChartOfAccountItem?>(null) }
    val deleteAccountState by financeViewModel.deleteAccountState.collectAsStateWithLifecycle()

    var expandedIds by remember { mutableStateOf(setOf<String>()) }
    val listState = rememberLazyListState()

    // Fetch initial accounts
    LaunchedEffect(Unit) {
        financeViewModel.fetchChartOfAccounts()
    }

    // Debounce local search filtering
    LaunchedEffect(searchQuery) {
        delay(300)
        debouncedSearchQuery = searchQuery.trim()
    }

    val childrenOf = remember(accounts) {
        accounts.groupBy { it.parentAccount?._id }
    }
    fun hasChildren(id: String): Boolean = !childrenOf[id].isNullOrEmpty()

    val isSearching = debouncedSearchQuery.isNotBlank()

    val visibleAccounts = remember(accounts, childrenOf, expandedIds, debouncedSearchQuery) {
        if (isSearching) {
            accounts.filter {
                it.accountName.contains(debouncedSearchQuery, ignoreCase = true) ||
                        it.accountCode.contains(debouncedSearchQuery, ignoreCase = true)
            }
        } else {
            buildVisibleTree(childrenOf[null].orEmpty(), childrenOf, expandedIds)
        }
    }

    LaunchedEffect(deleteAccountState) {
        when (val state = deleteAccountState) {
            is DeleteAccountState.Success -> {
                successMessage = state.message.takeIf { it.isNotBlank() }
                    ?: "Account deleted successfully"
                financeViewModel.resetDeleteAccountState()
            }
            is DeleteAccountState.Error -> {
                errorToastMessage = state.message.takeIf { it.isNotBlank() }
                    ?: "Failed to delete account"
                financeViewModel.resetDeleteAccountState()
            }
            else -> Unit
        }
    }

    if (showAddAccount) {
        AddAccountScreen(
            financeViewModel = financeViewModel,
            mode = screenMode,
            existingAccount = selectedAccount,
            onClose = {
                showAddAccount = false
                selectedAccount = null
                screenMode = AccountScreenMode.CREATE
            },
            onSaved = { message ->
                showAddAccount = false
                selectedAccount = null
                screenMode = AccountScreenMode.CREATE
                financeViewModel.fetchChartOfAccounts()
                successMessage = message.takeIf { it.isNotBlank() } ?: "Account created successfully"
            },
            onUpdate = { _, _, _, _, _, message ->
                showAddAccount = false
                selectedAccount = null
                screenMode = AccountScreenMode.CREATE
                successMessage = message.takeIf { it.isNotBlank() } ?: "Account updated successfully"
            }
        )
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TitleBar("Chart of Accounts", onClose = onClose)
            }

            Column {
                ScreenBreadcrumb(
                    segments = listOf("Finance", "Chart of Accounts"),
                    onClick = onBreadcrumbClick
                )

                SearchFilterBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search account...",
                    accentColor = BluePrimary,
                    borderColor = BorderGray,
                    textSecondaryColor = TextSecondary,
                    onFilterClick = { }
                )
            }
            HorizontalDivider(color = title_border)

            when {
                isLoading && accounts.isEmpty() -> {
                    ListSkeleton()
                }

                errorMessage != null && accounts.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Warning,
                                null,
                                tint = Color.Red,
                                modifier = Modifier.size(tokens.iconSize * 2.4f)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Something went wrong, Please try again later",
                                color = Color.Red,
                                fontSize = tokens.bodyMedium
                            )
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = { financeViewModel.fetchChartOfAccounts() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B3BF9)),
                                shape = RoundedCornerShape(tokens.cardCornerRadius / 2)
                            ) {
                                Text("Retry", color = whiteBg, fontSize = tokens.bodyMedium)
                            }
                        }
                    }
                }

                else -> {
                    // Wrap with FabScaffold so the FAB remains visible even when the account list is empty
                    FabScaffold(
                        modifier = Modifier.weight(1f),
                        fab = FabConfig(
                            label = "Add Account",
                            icon = Icons.Default.Add,
                            onClick = { showAddAccount = true }
                        )
                    ) {
                        if (visibleAccounts.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No accounts found", color = TextSecondary, fontSize = tokens.bodyMedium)
                            }
                        } else {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(visibleAccounts, key = { it._id }) { account ->
                                    val (badgeBg, badgeFg) = statusBadgeColors(account.status)
                                    val expandable = !isSearching && hasChildren(account._id)
                                    val isExpanded = expandedIds.contains(account._id)
                                    val depth = account.indentLevel()

                                    val (titleColor, titleSize) = when (depth) {
                                        0 -> Color(0xFF111827) to tokens.h2
                                        1 -> Color(0xFF4B5563) to tokens.bodyLarge
                                        else -> Color(0xFF7C8592) to tokens.bodyMedium
                                    }

                                    DataCard(
                                        item = account,
                                        modifier = Modifier.animateItem(),
                                        title = account.accountName,
                                        titleColor = titleColor,
                                        subtitle = "Code: ${account.accountCode}   Type: ${account.accountType}   Sub: ${account.category ?: "-"}",
                                        topBadgeText = account.status,
                                        topBadgeTextColor = badgeFg,
                                        topBadgeBgColor = badgeBg,
                                        topBadgeInline = true,
                                        showChevron = expandable,
                                        chevronExpanded = isExpanded,
                                        onChevronClick = {
                                            expandedIds = if (isExpanded) {
                                                expandedIds - account._id
                                            } else {
                                                expandedIds + account._id
                                            }
                                        },
                                        actions = buildList {
                                            add(
                                                MenuAction(
                                                    label = "View",
                                                    icon = Icons.Default.Visibility,
                                                    onClick = {
                                                        selectedAccount = account
                                                        screenMode = AccountScreenMode.VIEW
                                                        showAddAccount = true
                                                    }
                                                )
                                            )
                                            if (account.isEditable) {
                                                add(
                                                    MenuAction(
                                                        label = "Edit",
                                                        icon = Icons.Default.Edit,
                                                        onClick = {
                                                            selectedAccount = account
                                                            screenMode = AccountScreenMode.EDIT
                                                            showAddAccount = true
                                                            onEditAccount(account)
                                                        }
                                                    )
                                                )
                                            }
                                            if (!account.isSystemAccount) {
                                                add(
                                                    MenuAction(
                                                        label = "Delete",
                                                        icon = Icons.Default.Delete,
                                                        tint = Color(0xFFDC2626),
                                                        textColor = Color(0xFFDC2626),
                                                        onClick = { accountPendingDelete = account }
                                                    )
                                                )
                                            }
                                        }
                                    )
                                }
                                item { Spacer(modifier = Modifier.height(80.dp)) }
                            }
                        }
                    }
                }
            }
        }

        DynamicIslandSuccess(
            modifier = Modifier.align(Alignment.TopCenter),
            message = successMessage,
            onDismiss = { successMessage = null }
        )
        DynamicIslandError(
            modifier = Modifier.align(Alignment.TopCenter),
            message = errorToastMessage,
            onDismiss = { errorToastMessage = null }
        )
    }

    accountPendingDelete?.let { account ->
        DeleteModel(
            title = "Delete Account",
            message = "Are you sure you want to delete \"${account.accountName}\"? This action cannot be undone.",
            onDismiss = {
                accountPendingDelete = null
            },
            onDelete = {
                financeViewModel.deleteChartOfAccount(account._id)
                accountPendingDelete = null
            }
        )
    }
}

private fun buildVisibleTree(
    nodes: List<ChartOfAccountItem>,
    childrenOf: Map<String?, List<ChartOfAccountItem>>,
    expandedIds: Set<String>
): List<ChartOfAccountItem> {
    val result = mutableListOf<ChartOfAccountItem>()
    for (node in nodes) {
        result.add(node)
        if (expandedIds.contains(node._id)) {
            val children = childrenOf[node._id].orEmpty()
            result.addAll(buildVisibleTree(children, childrenOf, expandedIds))
        }
    }
    return result
}

private fun statusBadgeColors(status: String): Pair<Color, Color> {
    val isPositive = status.equals("Active", ignoreCase = true) ||
            status.equals("Posted", ignoreCase = true)
    return if (isPositive) GreenBg to GreenText else OrangeBg to OrangeText
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAccountScreen(
    financeViewModel: FinanceViewModel,
    mode: AccountScreenMode = AccountScreenMode.CREATE,
    existingAccount: ChartOfAccountItem? = null,
    onClose: () -> Unit = {},
    onSaved: (String) -> Unit = {},
    onUpdate: (String, String, String?, String?, String, String) -> Unit = { _, _, _, _, _, _ -> }
) {
    val tokens = LocalAppTokens.current

    val createAccountState by financeViewModel.createAccountState.collectAsStateWithLifecycle()
    val accounts by financeViewModel.chartOfAccounts.collectAsStateWithLifecycle()

    var currentMode by remember(existingAccount?._id) { mutableStateOf(mode) }
    val isViewMode = currentMode == AccountScreenMode.VIEW
    val isEditMode = currentMode == AccountScreenMode.EDIT
    val fieldsEnabled = !isViewMode

    var accountType by remember { mutableStateOf(existingAccount?.accountType.orEmpty()) }
    var typeExpanded by remember { mutableStateOf(false) }

    var accountName by remember { mutableStateOf(existingAccount?.accountName.orEmpty()) }

    var isSubAccount by remember { mutableStateOf(existingAccount?.parentAccount != null) }
    var selectedParent by remember { mutableStateOf<ChartOfAccountItem?>(null) }
    var parentExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(accounts, existingAccount) {
        val parentId = existingAccount?.parentAccount?._id
        if (parentId != null && selectedParent == null) {
            selectedParent = accounts.find { it._id == parentId }
        }
    }

    var description by remember { mutableStateOf(existingAccount?.description.orEmpty()) }
    val subAccountEnabled = accountType.isNotBlank() && fieldsEnabled

    var showError by remember { mutableStateOf(false) }
    var errorMessage2 by remember { mutableStateOf("") }
    var errorField by remember { mutableStateOf<String?>(null) }

    val updateAccountState by financeViewModel.updateAccountState.collectAsStateWithLifecycle()

    val isSaving = createAccountState is CreateAccountState.Loading ||
            updateAccountState is UpdateAccountState.Loading

    LaunchedEffect(createAccountState) {
        when (val state = createAccountState) {
            is CreateAccountState.Success -> {
                onSaved(state.message.takeIf { it.isNotBlank() } ?: "Account created successfully")
                financeViewModel.resetCreateAccountState()
            }
            is CreateAccountState.Error -> {
                errorMessage2 = state.message.takeIf { it.isNotBlank() } ?: "Failed to create account"
                showError = true
                financeViewModel.resetCreateAccountState()
            }
            else -> Unit
        }
    }

    LaunchedEffect(updateAccountState) {
        when (val state = updateAccountState) {
            is UpdateAccountState.Success -> {
                onUpdate(
                    accountName, accountType, description.ifBlank { null },
                    if (isSubAccount) selectedParent?._id else null,
                    existingAccount?._id.orEmpty(),
                    state.message.takeIf { it.isNotBlank() } ?: "Account updated successfully"
                )
                financeViewModel.resetUpdateAccountState()
            }
            is UpdateAccountState.Error -> {
                errorMessage2 = state.message.takeIf { it.isNotBlank() } ?: "Failed to update account"
                showError = true
                financeViewModel.resetUpdateAccountState()
            }
            else -> Unit
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
            TitleBar(
                title = when (currentMode) {
                    AccountScreenMode.VIEW -> "View Account"
                    AccountScreenMode.EDIT -> "Edit Account"
                    AccountScreenMode.CREATE -> "Create Account"
                },
                onClose = {
                    if (!isSaving) onClose()
                }
            )
            HorizontalDivider(color = BorderGray)
            HorizontalDivider(color = BorderGray)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = tokens.screenPadding, vertical = tokens.screenPadding)
                    .padding(bottom = 70.dp)
            ) {
                FormDropdown(
                    label = "Account Type",
                    value = accountType.ifBlank { "Select an option" },
                    expanded = typeExpanded,
                    onExpandChange = { typeExpanded = it },
                    options = accountTypeOptions,
                    onOptionSelected = { newType ->
                        if (newType != accountType) {
                            accountType = newType
                            isSubAccount = false
                            selectedParent = null
                        }
                        errorField = null
                    },
                    enabled = fieldsEnabled,
                    isRequired = true,
                    isError = errorField == "accountType",
                    errorMessage = if (errorField == "accountType") "Account type is required" else null
                )
                Spacer(Modifier.height(14.dp))

                FormLabel("Account Name", isRequired = true)
                FormTextField(
                    value = accountName,
                    onValueChange = {
                        if (fieldsEnabled) {
                            accountName = it
                            errorField = null
                        }
                    },
                    placeholder = "Account Name",
                    enabled = fieldsEnabled,
                    isError = errorField == "accountName",
                    errorMessage = if (errorField == "accountName") "Account name is required" else null
                )
                Spacer(Modifier.height(14.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(enabled = subAccountEnabled) {
                        isSubAccount = !isSubAccount
                        if (!isSubAccount) selectedParent = null
                    }
                ) {
                    Checkbox(
                        checked = isSubAccount,
                        onCheckedChange = {
                            isSubAccount = it
                            if (!it) selectedParent = null
                        },
                        enabled = subAccountEnabled,
                        colors = CheckboxDefaults.colors(
                            checkedColor = Primary,
                            checkmarkColor = whiteBg
                        )
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Make this a sub-account",
                        fontSize = tokens.bodyMedium,
                        color = if (subAccountEnabled) Color(0xFF374151) else Color(0xFFB0B4BB)
                    )
                }

                if (subAccountEnabled && isSubAccount) {
                    Spacer(Modifier.height(10.dp))

                    val eligibleParentAccounts = remember(accounts, accountType) {
                        accounts
                            .filter { it.accountType == accountType }
                            .sortedBy { it.accountCode.toIntOrNull() ?: Int.MAX_VALUE }
                    }

                    val parentDisplayOptions = remember(eligibleParentAccounts) {
                        eligibleParentAccounts.map { "${it.accountCode} - ${it.accountName}" }
                    }

                    FormDropdown(
                        label = "Parent Account",
                        value = selectedParent?.let { "${it.accountCode} - ${it.accountName}" } ?: "Select an option",
                        expanded = parentExpanded,
                        onExpandChange = { parentExpanded = it },
                        options = parentDisplayOptions,
                        onOptionSelected = { display ->
                            val code = display.substringBefore(" - ")
                            selectedParent = eligibleParentAccounts.find { it.accountCode == code }
                        },
                        enabled = true
                    )
                }
                Spacer(Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .background(whiteBg, RoundedCornerShape(tokens.cardCornerRadius / 2))
                        .border(1.dp, BorderGray, RoundedCornerShape(tokens.cardCornerRadius / 2))
                        .padding(12.dp)
                ) {
                    BasicTextField(
                        value = description,
                        onValueChange = { if (fieldsEnabled) description = it },
                        enabled = fieldsEnabled,
                        modifier = Modifier.fillMaxSize(),
                        textStyle = TextStyle(
                            fontSize = tokens.bodyMedium,
                            color = if (fieldsEnabled) Color(0xFF374151) else Color(0xFF9CA3AF)
                        ),
                        decorationBox = { inner ->
                            if (description.isEmpty()) {
                                Text("Description", fontSize = tokens.bodyMedium, color = TextSecondary)
                            }
                            inner()
                        }
                    )
                }
            }
        }

        StepNavigationFab(
            showBack = true,
            onBack = onClose,
            backLabel = if (isViewMode) "Close" else "Cancel",
            backEnabled = !isSaving,
            showBackArrow = false,
            showTrailingArrow = false,
            trailingAction = if (isViewMode) {
                TrailingFabAction.Edit(
                    label = "Edit",
                    enabled = true,
                    onClick = { currentMode = AccountScreenMode.EDIT }
                )
            } else {
                TrailingFabAction.Update(
                    isLoading = isSaving,
                    label = if (isEditMode) "Update Changes" else "Create Account",
                    enabled = !isSaving,
                    onClick = {
                        val fields = listOf(
                            ValidationField(
                                "accountType",
                                accountType,
                                "Account type is required"
                            ),
                            ValidationField("accountName", accountName, "Account name is required")
                        )
                        val result = FieldValidator.validate(fields)
                        if (result != null) {
                            errorField = result.fieldKey
                            errorMessage2 = result.message
                            showError = true
                        } else {
                            errorField = null

                            if (isEditMode) {
                                financeViewModel.updateChartOfAccount(
                                    id = existingAccount?._id.orEmpty(),
                                    accountName = accountName,
                                    accountType = accountType,
                                    description = description.ifBlank { null },
                                    parentAccount = if (isSubAccount) selectedParent?._id else null
                                )
                            } else {
                                financeViewModel.createChartOfAccount(
                                    accountName = accountName,
                                    accountType = accountType,
                                    description = description.ifBlank { null },
                                    parentAccount = if (isSubAccount) selectedParent?._id else null
                                )
                            }
                        }
                    }
                )
            }
        )

        DynamicIslandError(
            modifier = Modifier.align(Alignment.TopCenter),
            message = if (showError) errorMessage2 else null,
            onDismiss = { showError = false }
        )
    }
}