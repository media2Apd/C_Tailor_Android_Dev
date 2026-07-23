@file:Suppress("UNUSED_PARAMETER",
    "UNUSED",
    "RedundantSuppression",
    "unused_variable",
    "AssignedValueIsNeverRead", "VariableNeverRead"
)
package com.cuso.mobile.view.home.finance

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
    import androidx.compose.material.icons.filled.Add
    import androidx.compose.material.icons.filled.Close
    import androidx.compose.material.icons.filled.Delete
    import androidx.compose.material.icons.filled.Edit
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
    import com.cuso.mobile.model.finance.ChartOfAccountItem
    import com.cuso.mobile.view.home.reusablecomposables.DataCard
    import com.cuso.mobile.view.home.reusablecomposables.MenuAction
    import com.cuso.mobile.viewmodel.FinanceViewModel
    import com.cuso.mobile.model.finance.indentLevel
    import com.cuso.mobile.view.home.FormDropdown
    import com.cuso.mobile.view.home.FormLabel
    import com.cuso.mobile.view.home.FormTextField
    import com.cuso.mobile.view.home.reusablecomposables.FabConfig
    import com.cuso.mobile.view.home.reusablecomposables.FabScaffold
    import com.cuso.mobile.viewmodel.CreateAccountState
    import kotlinx.coroutines.launch
    import androidx.compose.material.icons.filled.Visibility
    import com.cuso.mobile.ui.theme.BluePrimary
    import com.cuso.mobile.ui.theme.BorderGray
    import com.cuso.mobile.ui.theme.TextSecondary
    import com.cuso.mobile.view.composable.DynamicIslandError
    import com.cuso.mobile.view.composable.FieldValidator
    import com.cuso.mobile.view.composable.ScreenBreadcrumb
    import com.cuso.mobile.view.composable.ValidationField
    import com.cuso.mobile.view.home.reusablecomposables.ListSkeleton
    import com.cuso.mobile.view.home.reusablecomposables.SearchFilterBar
    import com.cuso.mobile.viewmodel.DeleteAccountState
    import com.cuso.mobile.viewmodel.UpdateAccountState


    private val GreenBg = Color(0xFFE3F7EA)
    private val GreenText = Color(0xFF1FA751)
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
        onBreadcrumbClick: () -> Unit = {},   // ✅ NEW

    )  {
        val accounts by financeViewModel.chartOfAccounts.collectAsStateWithLifecycle()
        val isLoading by financeViewModel.isLoadingChartOfAccounts.collectAsStateWithLifecycle()
        val errorMessage by financeViewModel.chartOfAccountsError.collectAsStateWithLifecycle() // RENAMED from "error" — that name shadows Kotlin's built-in error() function

        var searchQuery by remember { mutableStateOf("") }
        var showAddAccount by remember { mutableStateOf(false) } // NEW — same pattern as showAddExpense

// ✅ NEW — drives View / Edit reuse of AddAccountScreen
        var screenMode by remember { mutableStateOf(AccountScreenMode.CREATE) }
        var selectedAccount by remember { mutableStateOf<ChartOfAccountItem?>(null) }

        val snackbarHostState = remember { SnackbarHostState() }
        val coroutineScope = rememberCoroutineScope()

// ✅ NEW — delete confirmation
        var accountPendingDelete by remember { mutableStateOf<ChartOfAccountItem?>(null) }
        val deleteAccountState by financeViewModel.deleteAccountState.collectAsStateWithLifecycle()



        // Which account ids are currently expanded (showing their direct children)
        var expandedIds by remember { mutableStateOf(setOf<String>()) }

        // Fetch once when screen is first shown
        LaunchedEffect(Unit) {
            financeViewModel.fetchChartOfAccounts()
        }

        // ── Build parent → children map once per accounts list change ──
        // key = parent account's _id (null key = root/main accounts like Assets, Liabilities...)
        val childrenOf = remember(accounts) {
            accounts.groupBy { it.parentAccount?._id }
        }
        fun hasChildren(id: String): Boolean = !childrenOf[id].isNullOrEmpty()

        val isSearching = searchQuery.isNotBlank()

        // ── Rows actually shown in the list ──
        // - Not searching: accordion tree, starting from root accounts, only
        //   expanding a branch when its id is in expandedIds.
        // - Searching: flat filtered list (hierarchy doesn't make sense while
        //   filtering, so we just show every match with no expand/collapse).
        val visibleAccounts = remember(accounts, childrenOf, expandedIds, searchQuery) {
            if (isSearching) {
                accounts.filter {
                    it.accountName.contains(searchQuery, ignoreCase = true) ||
                            it.accountCode.contains(searchQuery, ignoreCase = true)
                }
            } else {
                buildVisibleTree(childrenOf[null].orEmpty(), childrenOf, expandedIds)
            }
        }
        LaunchedEffect(deleteAccountState) {
            when (val state = deleteAccountState) {
                is DeleteAccountState.Success -> {
                    coroutineScope.launch { snackbarHostState.showSnackbar(state.message) }
                    financeViewModel.resetDeleteAccountState()
                }
                is DeleteAccountState.Error -> {
                    coroutineScope.launch { snackbarHostState.showSnackbar(state.message) }
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
                onSaved = { message ->                          // ✅ CREATE success
                    showAddAccount = false
                    selectedAccount = null
                    screenMode = AccountScreenMode.CREATE
                    financeViewModel.fetchChartOfAccounts()      // refresh list after add
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(message)  // ✅ shows on ChartOfAccountScreen
                    }
                },
                onUpdate = { _, _, _, _, _ ->
                    // AddAccountScreen already called financeViewModel.updateChartOfAccount()
                    // and refreshed the list on success — here we just close + show feedback
                    showAddAccount = false
                    selectedAccount = null
                    screenMode = AccountScreenMode.CREATE
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Account updated successfully")
                    }
                }
            )
            return
        }
        Box(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                        .background(Color.White),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Chart Of Account",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF111827),
                        modifier = Modifier
                            .size(22.dp)
                            .clickable { onClose() }
                    )
                }
                Column(
                    Modifier.background(Color(0xFFF8F9FF))

                ) {
                    // ── Breadcrumb ──
                    ScreenBreadcrumb(
                        segments = listOf("Finance", "Chart of Accounts"),
                        onClick = onBreadcrumbClick
                    )

                    SearchFilterBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        placeholder = "Search account...",
                        accentColor = BluePrimary,
                        borderColor = BorderGray,
                        textSecondaryColor = TextSecondary,
                        onFilterClick = { /* TODO: open filter drawer */ }
                    )
                }

                // ── Content ──
                when {
                    isLoading -> {
                        ListSkeleton()
                    }

                    errorMessage != null -> {
                        Box(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = errorMessage ?: "Something went wrong", color = Color.Red)
                        }
                    }

                    visibleAccounts.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No accounts found", color = TextSecondary)
                        }
                    }

                    else -> {
                        FabScaffold(
                            modifier = Modifier.weight(1f),
                            fab = FabConfig(
                                label = "Add Account",
                                icon = Icons.Default.Add,
                                onClick = { showAddAccount = true }
                            )
                        ) {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(visibleAccounts, key = { it._id }) { account ->
                                    val (badgeBg, badgeFg) = statusBadgeColors(account.status)
                                    val expandable = !isSearching && hasChildren(account._id)
                                    val isExpanded = expandedIds.contains(account._id)
                                    val depth = account.indentLevel()

                                    val (titleColor, titleSize) = when (depth) {
                                        0 -> Color(0xFF111827) to 18.sp
                                        1 -> Color(0xFF4B5563) to 16.sp
                                        else -> Color(0xFF7C8592) to 15.sp
                                    }

                                    DataCard(
                                        item = account,
                                        modifier = Modifier.animateItem(),
                                        title = account.accountName,
                                        titleColor = titleColor,
                                        titleFontSize = titleSize,
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
                                            // ✅ isEditable = false na Edit option kaamikadhu (show aaga vendam)
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
                                            // ✅ System accounts can't be deleted — Delete action omitted entirely
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
            // ✅ NEW — Snackbar host, floats over the screen
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )
        }
        // ✅ NEW — Delete confirmation dialog
        accountPendingDelete?.let { account ->
            AlertDialog(
                onDismissRequest = { accountPendingDelete = null },
                title = { Text("Delete Account") },
                text = {
                    Text("Are you sure you want to delete \"${account.accountName}\"? This action cannot be undone.")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            financeViewModel.deleteChartOfAccount(account._id)
                            accountPendingDelete = null
                        }
                    ) {
                        Text("Delete", color = Color(0xFFDC2626))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { accountPendingDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }

    /**
     * Flattens the account tree into the list actually rendered by the LazyColumn:
     * each node, followed immediately by its children (recursively) only if that
     * node's id is present in [expandedIds]. Collapsed branches contribute nothing
     * beyond their own row.
     */
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

    /**
     * Real API sample only returns "Active" for status — there's no Posted/Pending
     * in this endpoint's response. If your backend has a separate posting-status
     * field, swap it in here. For now: "Active" (and "Posted") render green,
     * everything else (e.g. "Inactive", "Pending") renders orange — matching the
     * two badge colors shown in the design.
     */
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
        onSaved: (String) -> Unit = {},                                             // CREATE success
        onUpdate: (String, String, String?, String?, String) -> Unit = { _, _, _, _, _ -> }
        // ↑ onUpdate params: accountName, accountType, description, parentAccountId, accountId
    ) {
        val createAccountState by financeViewModel.createAccountState.collectAsStateWithLifecycle()
        val accounts by financeViewModel.chartOfAccounts.collectAsStateWithLifecycle() // for Parent Account options

        val isViewMode = mode == AccountScreenMode.VIEW
        val isEditMode = mode == AccountScreenMode.EDIT
        val fieldsEnabled = !isViewMode   // disabled only in VIEW mode; CREATE/EDIT stay editable

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

        // ── Dynamic Island error state ──
        var showError by remember { mutableStateOf(false) }
        var errorMessage2 by remember { mutableStateOf("") }
        var errorField by remember { mutableStateOf<String?>(null) }

// ✅ moved up — must be declared BEFORE isSaving references it
        val updateAccountState by financeViewModel.updateAccountState.collectAsStateWithLifecycle()

        val isSaving = createAccountState is CreateAccountState.Loading ||
                updateAccountState is UpdateAccountState.Loading
        val isFormValid = accountName.isNotBlank() && accountType.isNotBlank() &&
                (!isSubAccount || selectedParent != null)

        LaunchedEffect(createAccountState) {
            when (val state = createAccountState) {
                is CreateAccountState.Success -> {
                    onSaved(state.message)
                    financeViewModel.resetCreateAccountState()
                }
                else -> Unit
            }
        }

        LaunchedEffect(updateAccountState) {
            when (val state = updateAccountState) {
                is UpdateAccountState.Success -> {
                    onUpdate(accountName, accountType, description.ifBlank { null },
                        if (isSubAccount) selectedParent?._id else null,
                        existingAccount?._id.orEmpty())
                    financeViewModel.resetUpdateAccountState()
                }
                else -> Unit
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                // ── Title bar ──
                Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (mode) {
                        AccountScreenMode.VIEW -> "View Account"
                        AccountScreenMode.EDIT -> "Edit Account"
                        AccountScreenMode.CREATE -> "Create Account"
                    },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color(0xFF111827),
                    modifier = Modifier
                        .size(22.dp)
                        .clickable(enabled = !isSaving) { onClose() }
                )
            }
            HorizontalDivider(color = BorderGray)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                // ── Account Type ──
                FormDropdown(
                    label = "Account Type",
                    value = accountType.ifBlank { "Select an option" },
                    expanded = typeExpanded,
                    onExpandChange = { typeExpanded = it },
                    options = accountTypeOptions,
                    onOptionSelected = { newType ->
                        if (newType != accountType) {
                            accountType = newType
                            isSubAccount = false      // reset checkbox
                            selectedParent = null     // clear stale parent
                        }
                        errorField = null
                    },
                    enabled = fieldsEnabled,            // ✅ disabled in VIEW mode
                    isRequired = true,
                    isError = errorField == "accountType",
                    errorMessage = if (errorField == "accountType") "Account type is required" else null
                )
                Spacer(Modifier.height(14.dp))

                FormLabel("Account Name", isRequired = true)
                // ── Account Name (placeholder only, no label — matches design) ──
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

                // ── Make this a sub-account ──
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
                        colors = CheckboxDefaults.colors(checkedColor = BluePrimary)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Make this a sub-account",
                        fontSize = 14.sp,
                        color = if (subAccountEnabled) Color(0xFF374151) else Color(0xFFB0B4BB)
                    )
                }

// ── Parent Account (only shown once Account Type is picked AND it's a sub-account) ──
                // ── Parent Account (only shown once Account Type is picked AND it's a sub-account) ──
                if (subAccountEnabled && isSubAccount) {
                    Spacer(Modifier.height(10.dp))

                    // "Account Name (CODE)" — built straight from the API response fields
                    // ALL accounts of the SAME accountType are valid parents (root + sub-accounts),
// sorted by accountCode ascending — matches the API response order in the design
                    val eligibleParentAccounts = remember(accounts, accountType) {
                        accounts
                            .filter { it.accountType == accountType }
                            .sortedBy { it.accountCode.toIntOrNull() ?: Int.MAX_VALUE }
                    }

                    // "CODE - Account Name" — straight from the API response fields
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
                        enabled = fieldsEnabled
                    )
                }
                Spacer(Modifier.height(14.dp))

                // ── Description (placeholder only, multiline, no label — matches design) ──
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .border(1.dp, BorderGray, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    androidx.compose.foundation.text.BasicTextField(
                        value = description,
                        onValueChange = { if (fieldsEnabled) description = it },
                        enabled = fieldsEnabled,
                        modifier = Modifier.fillMaxSize(),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 14.sp,
                            color = if (fieldsEnabled) Color(0xFF374151) else Color(0xFF9CA3AF)
                        ),
                        decorationBox = { inner ->
                            if (description.isEmpty()) {
                                Text("Description", fontSize = 14.sp, color = TextSecondary)
                            }
                            inner()
                        }
                    )
                }

                if (createAccountState is CreateAccountState.Error) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = (createAccountState as CreateAccountState.Error).message,
                        color = Color.Red,
                        fontSize = 13.sp
                    )
                }
                if (updateAccountState is UpdateAccountState.Error) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = (updateAccountState as UpdateAccountState.Error).message,
                        color = Color.Red,
                        fontSize = 13.sp
                    )
                }
            }

            // ── Bottom action bar ──
            HorizontalDivider(color = BorderGray)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onClose,
                    enabled = !isSaving,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(if (isViewMode) "Close" else "Cancel")
                }

                if (!isViewMode) {
                    Spacer(Modifier.width(12.dp))
                    Button(
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
                                return@Button
                            }
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
                                // ✅ CREATE
                                financeViewModel.createChartOfAccount(
                                    accountName = accountName,
                                    accountType = accountType,
                                    description = description.ifBlank { null },
                                    parentAccount = if (isSubAccount) selectedParent?._id else null
                                )
                            }
                        },
                        enabled = !isSaving,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(if (isEditMode) "Save Changes" else "Save Account", color = Color.White)
                        }
                    }
                }
            }
            }   // ← closes inner Column

            // ── Dynamic Island error toast ──
            DynamicIslandError(
                modifier = Modifier.align(Alignment.TopCenter),
                message = if (showError) errorMessage2 else null,
                onDismiss = { showError = false }
            )
        }   // ← closes root Box
    }

