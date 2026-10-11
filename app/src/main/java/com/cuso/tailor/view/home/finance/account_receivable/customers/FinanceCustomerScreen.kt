@file:Suppress("UNUSED_PARAMETER", "UNUSED", "RedundantSuppression", "unused")

package com.cuso.tailor.view.home.finance.account_receivable.customers

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.sales.CustomerItemV2
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.view.home.formatIndianNumber
import com.cuso.tailor.viewmodel.FinanceViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import com.cuso.tailor.R
import com.cuso.tailor.model.finance.CreateCustomerPayload
import com.cuso.tailor.model.finance.CustomerAddressPayload
import com.cuso.tailor.model.finance.CustomerCustomFieldsPayload
import com.cuso.tailor.model.settings.BranchItem
import com.cuso.tailor.viewmodel.BranchUiState
import com.cuso.tailor.viewmodel.BranchViewModel
import com.cuso.tailor.viewmodel.CreateCustomerState

// ─────────────────────────────────────────────────────────────
// FinanceCustomerScreen — List with Draggable FabScaffold
// ─────────────────────────────────────────────────────────────
@Composable
fun FinanceCustomerScreen(
    onClose: () -> Unit,
    onCustomerClick: (String) -> Unit,
    onCustomerEdit: (String) -> Unit = onCustomerClick,
    onAddCustomer: () -> Unit = {},
    onBreadCrumbClick: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val viewModel: FinanceViewModel = hiltViewModel()

    val customerListResponse by viewModel.financeCustomerList.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingFinanceCustomers.collectAsStateWithLifecycle()
    val isLoadingMore by viewModel.isLoadingMoreFinanceCustomers.collectAsStateWithLifecycle()
    val canLoadMore by viewModel.canLoadMoreFinanceCustomers.collectAsStateWithLifecycle()
    val error by viewModel.financeCustomerError.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Initial data fetch
    LaunchedEffect(Unit) {
        viewModel.fetchCustomerForFinance()
    }

    // Infinite scroll detection: triggers loadMore near end of list
    LaunchedEffect(listState) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 0 && lastVisibleIndex >= totalItems - 2
        }
            .distinctUntilChanged()
            .collect { nearBottom ->
                if (nearBottom && canLoadMore && !isLoadingMore && !isLoading) {
                    viewModel.loadMoreFinanceCustomers()
                }
            }
    }

    val allCustomers = customerListResponse?.data.orEmpty()

    val filteredCustomers = remember(allCustomers, searchQuery) {
        if (searchQuery.isBlank()) {
            allCustomers
        } else {
            allCustomers.filter { customer ->
                customer.name.contains(searchQuery, ignoreCase = true) ||
                        customer.mobile.contains(searchQuery, ignoreCase = true) ||
                        (customer.customerCode?.contains(searchQuery, ignoreCase = true) == true)
            }
        }
    }

    // Screen wrapped with Draggable FabScaffold
    FabScaffold(
        modifier = Modifier.fillMaxSize(),
        fab = FabConfig(
            label = "Add Customer",
            icon = Icons.Default.Add,
            onClick = { onAddCustomer() },
            alignment = Alignment.BottomEnd,
            endPadding = 16.dp,
            bottomPadding = 24.dp,
            draggable = true
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
            // --- HEADER SECTION ---
            Column(modifier = Modifier.fillMaxWidth()) {
                TitleBar(title = "All Customers", onClose = onClose)

                SearchFilterBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search by Name, Code or Mobile...",
                    accentColor = BluePrimary,
                    borderColor = BorderGray,
                    textSecondaryColor = TextSecondary,
                    onFilterClick = { }
                )

                HorizontalDivider(color = title_border)
            }

            // --- CONTENT SECTION ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when {
                    isLoading -> {
                        ListSkeleton()
                    }

                    error != null -> {
                        AppErrorState(
                            title = "Failed to load finance customers",
                            message = error ?: "Something went wrong. Please check your connection and try again.",
                            onRetry = { viewModel.fetchCustomerForFinance() }
                        )
                    }

                    filteredCustomers.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = tokens.screenPadding),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.PersonOff,
                                    contentDescription = null,
                                    tint = mutedText,
                                    modifier = Modifier.size(tokens.imageDp)
                                )
                                Spacer(Modifier.height(tokens.extraPadding))
                                Text(
                                    text = if (searchQuery.isNotBlank()) "No matching customers found" else "No customers yet",
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Normal,
                                    color = mutedText
                                )
                            }
                        }
                    }

                    else -> {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                top = tokens.extraPadding,
                                bottom = 80.dp
                            )
                        ) {
                            items(
                                items = filteredCustomers,
                                key = { it._id }
                            ) { customer ->
                                CustomerCardItem(
                                    customer = customer,
                                    onClick = { onCustomerClick(customer._id) },
                                    onEdit = { onCustomerEdit(customer._id) }
                                )
                            }

                            if (isLoadingMore) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(tokens.extraPadding),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CirculerProgressIndicatorSmall()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Standalone Create Customer Screen (Full Page)
// ─────────────────────────────────────────────────────────────
@Composable
fun CreateCustomerScreen(
    onClose: () -> Unit,
    onCustomerCreated: () -> Unit = onClose,
    viewModel: FinanceViewModel = hiltViewModel(),
    branchViewModel: BranchViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    val createCustomerState by viewModel.createCustomerState.collectAsStateWithLifecycle()

    // ── Fetch Branches from API ──
    LaunchedEffect(Unit) {
        branchViewModel.loadBranches()
    }

    val branchUiState by branchViewModel.uiState.collectAsStateWithLifecycle()
    val branches = remember(branchUiState) {
        (branchUiState as? BranchUiState.Success)?.branches.orEmpty()
    }

    // Selected Branch State
    var selectedBranch by remember { mutableStateOf<BranchItem?>(null) }
    var branchExpanded by remember { mutableStateOf(false) }

    // Pre-select first branch or default branch once branches are loaded
    LaunchedEffect(branches) {
        if (selectedBranch == null && branches.isNotEmpty()) {
            selectedBranch = branches.find { it.isMainBranch } ?: branches.first()
        }
    }

    // Tab 1: General & Demographics Form States
    var fullName by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }
    var emailAddress by remember { mutableStateOf("") }
    var customerType by remember { mutableStateOf("") }
    var customerTypeExpanded by remember { mutableStateOf(false) }
    var gender by remember { mutableStateOf("Male") }
    var genderExpanded by remember { mutableStateOf(false) }
    var dateOfBirth by remember { mutableStateOf("") }
    var preferredContactMethod by remember { mutableStateOf("") }
    var contactMethodExpanded by remember { mutableStateOf(false) }
    var preferredLanguage by remember { mutableStateOf("") }
    var languageExpanded by remember { mutableStateOf(false) }

    // Tab 2: Credit & Taxation Form States
    var customerClassification by remember { mutableStateOf("") }
    var classificationExpanded by remember { mutableStateOf(false) }
    var creditLimit by remember { mutableStateOf("") }
    var creditPeriodDays by remember { mutableStateOf("") }
    var taxIdType by remember { mutableStateOf("") }
    var taxIdTypeExpanded by remember { mutableStateOf(false) }
    var taxRegistrationNumber by remember { mutableStateOf("") }
    var accountStatus by remember { mutableStateOf("") }
    var accountStatusExpanded by remember { mutableStateOf(false) }

    // Tab 3: Billing & Shipping Form States
    var flatBuildingNo by remember { mutableStateOf("") }
    var streetAddress by remember { mutableStateOf("") }
    var areaZone by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var stateProvince by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var pincode by remember { mutableStateOf("") }
    var shippingSameAsBilling by remember { mutableStateOf(true) }

    // Tab 4: Internal Notes Form States
    var customerAccountingNotes by remember { mutableStateOf("") }

    val tabs = listOf(
        "General & Demographics" to R.drawable.ic_person,
        "Credit & Taxation" to R.drawable.ic_credit,
        "Billing & Shipping" to R.drawable.ic_location,
        "Internal Notes" to R.drawable.ic_document
    )

    // Handle Customer Creation Response
    LaunchedEffect(createCustomerState) {
        when (createCustomerState) {
            is CreateCustomerState.Success -> {
                viewModel.resetCreateCustomerState()
                onCustomerCreated()
            }
            else -> Unit
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary_background)
    ) {
        // Top Bar
        TitleBar(
            title = "New Customer",
            onClose = onClose
        )
        HorizontalDivider(color = dividerColor)
        Spacer(Modifier.padding(top = 10.dp))

        // Horizontal Tabs Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(whiteBg)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = tokens.screenPadding, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tabs.forEachIndexed { index, tab ->
                val isSelected = selectedTabIndex == index
                Surface(
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                    color = if (isSelected) Primary else Color.Transparent,
                    border = if (isSelected) null else BorderStroke(1.dp, BorderGray),
                    modifier = Modifier.clickable { selectedTabIndex = index }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            painter = painterResource(tab.second),
                            contentDescription = tab.first,
                            tint = if (isSelected) whiteBg else TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = tab.first,
                            color = if (isSelected) whiteBg else title_color,
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
        HorizontalDivider(color = dividerColor)

        // Scrollable Form Content Body
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
            ) {
                when (selectedTabIndex) {
                    // --- TAB 1: General & Demographics ---
                    0 -> {
                        FormSectionCard(title = "BASIC DEMOGRAPHICS") {
                            FormLabel(text = "Full Name", isRequired = true)
                            FormTextField(
                                value = fullName,
                                onValueChange = { fullName = it },
                                placeholder = "e.g. Rajesh Kumar"
                            )

                            Spacer(Modifier.height(tokens.extraPadding))
                            FormLabel(text = "Mobile Number", isRequired = true)
                            FormTextField(
                                value = mobileNumber,
                                onValueChange = { mobileNumber = it },
                                placeholder = "+91 9876543210",
                                keyboardType = KeyboardType.Phone
                            )

                            Spacer(Modifier.height(tokens.extraPadding))
                            FormLabel(text = "Email Address")
                            FormTextField(
                                value = emailAddress,
                                onValueChange = { emailAddress = it },
                                placeholder = "customer@example.com",
                                keyboardType = KeyboardType.Email
                            )

                            Spacer(Modifier.height(tokens.extraPadding))
                            FormDropdown(
                                label = "Customer Type",
                                value = customerType,
                                expanded = customerTypeExpanded,
                                onExpandChange = { customerTypeExpanded = it },
                                options = listOf("Individual", "Corporate", "Business"),
                                onOptionSelected = { customerType = it }
                            )

                            Spacer(Modifier.height(tokens.extraPadding))
                            FormDropdown(
                                label = "Gender",
                                value = gender,
                                expanded = genderExpanded,
                                onExpandChange = { genderExpanded = it },
                                options = listOf("Male", "Female", "Other"),
                                onOptionSelected = { gender = it }
                            )

                            Spacer(Modifier.height(tokens.extraPadding))
                            FormLabel(text = "Date of Birth")
                            FormTextField(
                                value = dateOfBirth,
                                onValueChange = { dateOfBirth = it },
                                placeholder = "YYYY-MM-DD"
                            )

                            Spacer(Modifier.height(tokens.extraPadding))
                            FormDropdown(
                                label = "Preferred Contact Method",
                                value = preferredContactMethod,
                                expanded = contactMethodExpanded,
                                onExpandChange = { contactMethodExpanded = it },
                                options = listOf("Whatsapp", "Call", "SMS", "Email"),
                                onOptionSelected = { preferredContactMethod = it }
                            )

                            Spacer(Modifier.height(tokens.extraPadding))
                            FormDropdown(
                                label = "Preferred Language",
                                value = preferredLanguage,
                                expanded = languageExpanded,
                                onExpandChange = { languageExpanded = it },
                                options = listOf("English", "Tamil", "Hindi"),
                                onOptionSelected = { preferredLanguage = it }
                            )

                            Spacer(Modifier.height(tokens.extraPadding))
                            // ── Live Branch Selection from API ──
                            FormDropdown(
                                label = "Assigned Branch",
                                value = selectedBranch?.name ?: if (branchUiState is BranchUiState.Loading) "Loading branches..." else "Select Branch",
                                expanded = branchExpanded,
                                onExpandChange = { branchExpanded = it },
                                options = branches.mapNotNull { it.name },
                                onOptionSelected = { branchName ->
                                    selectedBranch = branches.find { it.name == branchName }
                                }
                            )
                        }
                    }

                    // --- TAB 2: Credit & Taxation ---
                    1 -> {
                        FormSectionCard(title = "B2B CREDIT & TAX CONFIGURATION") {
                            FormDropdown(
                                label = "Customer Classification",
                                value = customerClassification,
                                expanded = classificationExpanded,
                                onExpandChange = { classificationExpanded = it },
                                options = listOf("Regular", "VIP", "Premium"),
                                onOptionSelected = { customerClassification = it }
                            )

                            Spacer(Modifier.height(tokens.extraPadding))
                            FormLabel(text = "Credit Limit (₹)")
                            FormTextField(
                                value = creditLimit,
                                onValueChange = { creditLimit = it },
                                placeholder = "0",
                                keyboardType = KeyboardType.Number
                            )

                            Spacer(Modifier.height(tokens.extraPadding))
                            FormLabel(text = "Credit Period (Days)")
                            FormTextField(
                                value = creditPeriodDays,
                                onValueChange = { creditPeriodDays = it },
                                placeholder = "0",
                                keyboardType = KeyboardType.Number
                            )

                            Spacer(Modifier.height(tokens.extraPadding))
                            FormDropdown(
                                label = "Tax ID Type",
                                value = taxIdType,
                                expanded = taxIdTypeExpanded,
                                onExpandChange = { taxIdTypeExpanded = it },
                                options = listOf("GSTIN", "PAN", "Other"),
                                onOptionSelected = { taxIdType = it }
                            )

                            Spacer(Modifier.height(tokens.extraPadding))
                            FormLabel(text = "Tax / Registration Number")
                            FormTextField(
                                value = taxRegistrationNumber,
                                onValueChange = { taxRegistrationNumber = it },
                                placeholder = "E.G. 33AAAAA0000A1Z5"
                            )

                            Spacer(Modifier.height(tokens.extraPadding))
                            FormDropdown(
                                label = "Account Status",
                                value = accountStatus,
                                expanded = accountStatusExpanded,
                                onExpandChange = { accountStatusExpanded = it },
                                options = listOf("Active", "Inactive", "Blocked"),
                                onOptionSelected = { accountStatus = it }
                            )
                        }
                    }

                    // --- TAB 3: Billing & Shipping ---
                    2 -> {
                        FormSectionCard(title = "BILLING ADDRESS") {
                            FormLabel(text = "Flat / Building / Door No")
                            FormTextField(
                                value = flatBuildingNo,
                                onValueChange = { flatBuildingNo = it },
                                placeholder = "Floor 4, Block B"
                            )

                            Spacer(Modifier.height(tokens.extraPadding))
                            FormLabel(text = "Street Address")
                            FormTextField(
                                value = streetAddress,
                                onValueChange = { streetAddress = it },
                                placeholder = "Gandhi Road"
                            )

                            Spacer(Modifier.height(tokens.extraPadding))
                            FormLabel(text = "Area / Zone")
                            FormTextField(
                                value = areaZone,
                                onValueChange = { areaZone = it },
                                placeholder = "Anna Nagar"
                            )

                            Spacer(Modifier.height(tokens.extraPadding))
                            FormLabel(text = "City", isRequired = true)
                            FormTextField(
                                value = city,
                                onValueChange = { city = it },
                                placeholder = "Chennai"
                            )

                            Spacer(Modifier.height(tokens.extraPadding))
                            FormLabel(text = "State / Province")
                            FormTextField(
                                value = stateProvince,
                                onValueChange = { stateProvince = it },
                                placeholder = "Tamil Nadu"
                            )

                            Spacer(Modifier.height(tokens.extraPadding))
                            FormLabel(text = "Country")
                            FormTextField(
                                value = country,
                                onValueChange = { country = it },
                                placeholder = "India"
                            )

                            Spacer(Modifier.height(tokens.extraPadding))
                            FormLabel(text = "Pincode")
                            FormTextField(
                                value = pincode,
                                onValueChange = { pincode = it },
                                placeholder = "600040",
                                keyboardType = KeyboardType.Number
                            )

                            Spacer(Modifier.height(tokens.extraPadding))
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = shippingSameAsBilling,
                                    onCheckedChange = { shippingSameAsBilling = it },
                                    colors = CheckboxDefaults.colors(checkedColor = Primary)
                                )
                                Text(
                                    text = "Shipping Address matches Billing Address",
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = title_color
                                )
                            }
                        }
                    }

                    // --- TAB 4: Internal Notes ---
                    3 -> {
                        FormSectionCard(title = "INTERNAL LEDGER REMARKS") {
                            FormLabel(text = "Customer Accounting Notes")
                            FormTextArea(
                                value = customerAccountingNotes,
                                onValueChange = { customerAccountingNotes = it },
                                placeholder = "Enter credit history observations, tax audit notes, or special client instructions...",
                                minLines = 4,
                                maxLines = 6
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = dividerColor)

        // Bottom Action Bar: Cancel & Save Customer Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(whiteBg)
                .padding(horizontal = tokens.screenPadding, vertical = 12.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onClose,
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                border = BorderStroke(1.dp, BorderGray),
                modifier = Modifier.height(tokens.buttonHeight)
            ) {
                Text(
                    text = "Cancel",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = title_color
                )
            }

            Spacer(Modifier.width(12.dp))

            // Save Customer Button
            Button(
                onClick = {
                    val addressPayload = CustomerAddressPayload(
                        flatNo = flatBuildingNo.trim(),
                        street = streetAddress.trim(),
                        areaZone = areaZone.trim(),
                        city = city.trim(),
                        subdivisionName = stateProvince.trim(),
                        countryName = country.trim().ifBlank { "India" },
                        pincode = pincode.trim()
                    )

                    val payload = CreateCustomerPayload(
                        fullName = fullName.trim(),
                        mobileNumber = mobileNumber.trim(),
                        email = emailAddress.trim().ifBlank { null },
                        customerType = customerType.ifBlank { "Individual" },
                        gender = gender.ifBlank { "Male" },
                        dateOfBirth = dateOfBirth.trim().ifBlank { null },
                        preferredContactMethod = preferredContactMethod.ifBlank { "Whatsapp" },
                        preferredLanguage = preferredLanguage.ifBlank { "English" },
                        customerLevel = customerClassification.ifBlank { "Regular" },
                        creditLimit = creditLimit.toDoubleOrNull() ?: 0.0,
                        creditPeriodDays = creditPeriodDays.toIntOrNull() ?: 0,
                        // ── Dynamic Branch ID from Selected Branch ──
                        branchId = selectedBranch?.id ?: branches.firstOrNull()?.id,
                        status = accountStatus.ifBlank { "Active" },
                        taxId = taxRegistrationNumber.trim().ifBlank { null },
                        taxIdType = taxIdType.ifBlank { "GSTIN" },
                        sameAsBillingAddress = shippingSameAsBilling,
                        billingAddress = addressPayload,
                        shippingAddress = if (shippingSameAsBilling) addressPayload else addressPayload,
                        customFields = CustomerCustomFieldsPayload(
                            notes = customerAccountingNotes.trim().ifBlank { null }
                        )
                    )

                    viewModel.createCustomer(payload)
                },
                enabled = fullName.isNotBlank() && mobileNumber.isNotBlank() && createCustomerState !is CreateCustomerState.Loading,
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                modifier = Modifier.height(tokens.buttonHeight)
            ) {
                if (createCustomerState is CreateCustomerState.Loading) {
                    CirculerProgressIndicatorSmall()
                } else {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = whiteBg,
                        modifier = Modifier.size(tokens.iconSize)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Save Customer",
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
// Form Container Card Helper
// ─────────────────────────────────────────────────────────────
@Composable
private fun FormSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val tokens = LocalAppTokens.current
    Card(
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(tokens.screenPadding)
        ) {
            Text(
                text = title,
                fontSize = tokens.caption,
                fontWeight = FontWeight.Medium,
                color = Primary,
                letterSpacing = 0.5.sp
            )
            Spacer(Modifier.height(tokens.extraPadding))
            HorizontalDivider(color = grey_border)
            Spacer(Modifier.height(tokens.extraPadding))
            content()
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Customer Card Item
// ─────────────────────────────────────────────────────────────
@Composable
private fun CustomerCardItem(
    customer: CustomerItemV2,
    onClick: () -> Unit,
    onEdit: () -> Unit
) {
    val statusSafe = customer.status?.ifBlank { "Active" } ?: "Active"
    val (badgeText, badgeColors) = statusColorsOfCustomer(statusSafe)
    val (badgeTextColor, badgeBgColor) = badgeColors

    val safeType = customer.type.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase() else it.toString()
    }
    val codeAndType = listOfNotNull(customer.customerCode, safeType).joinToString(" • ")
    val address = customer.displayAddress

    DataCard(
        item = customer,
        topBadgeText = badgeText,
        topBadgeTextColor = badgeTextColor,
        topBadgeBgColor = badgeBgColor,
        topBadgeInline = true,
        title = customer.name,
        footerFields = listOf(
            DataCardField(
                icon = Icons.Default.Phone,
                text = customer.mobile
            ),
            DataCardField(
                text = "$codeAndType | $address"
            ),
            DataCardField(
                text = "Outstanding: ₹${formatIndianNumber(customer.outstanding ?: 0.0)}"
            )
        ),
        actions = listOf(
            MenuAction("View", Icons.Default.Visibility) { onClick() },
            MenuAction("Edit", Icons.Default.Edit) { onEdit() }
        ),
        onClick = { onClick() }
    )
}

// ─────────────────────────────────────────────────────────────
// Status Badge Color Mapping using colors.kt
// ─────────────────────────────────────────────────────────────
private fun statusColorsOfCustomer(status: String?): Pair<String, Pair<Color, Color>> = when (status?.lowercase()) {
    "active" -> "Active" to (greentext to greenBg)
    "blocked" -> "Blocked" to (redText to redBg)
    "inactive" -> "Inactive" to (TextSecondary to lightGray)
    else -> (status?.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } ?: "Active") to (TextSecondary to lightGray)
}