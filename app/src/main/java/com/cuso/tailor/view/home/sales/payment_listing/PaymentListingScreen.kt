@file:Suppress("unused", "unusedVariable")
@file:OptIn(ExperimentalStdlibApi::class)

package com.cuso.tailor.view.home.sales.payment_listing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.adaptive_screen.getAdaptiveTokens
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.view.home.formatIndianNumber
import com.cuso.tailor.viewmodel.BranchUiState
import com.cuso.tailor.viewmodel.BranchViewModel
import com.cuso.tailor.viewmodel.SalesViewModel
import java.text.SimpleDateFormat
import java.util.Locale

// -------------------------------------------------------------
// Payments & Billing Filters Builder Function
// -------------------------------------------------------------
private fun buildPaymentFilterSections(branchOptions: List<FilterOption> = emptyList()): List<FilterSection> {
    return listOf(
        FilterSection(
            title = "Payment Status",
            type = FilterSectionType.CHECKBOX_LIST,
            isMultiSelect = true,
            options = listOf(
                FilterOption("paid", "Paid"),
                FilterOption("partially_paid", "Partially Paid"),
                FilterOption("pending", "Pending"),
                FilterOption("overdue", "Overdue"),
                FilterOption("cancelled", "Cancelled")
            )
        ),
        FilterSection(
            title = "Payment Mode",
            type = FilterSectionType.CHECKBOX_LIST,
            isMultiSelect = true,
            options = listOf(
                FilterOption("cash", "Cash"),
                FilterOption("upi", "UPI"),
                FilterOption("card", "Credit / Debit Card"),
                FilterOption("bank_transfer", "Bank Transfer"),
                FilterOption("cheque", "Cheque")
            )
        ),
        FilterSection(
            title = "Branch",
            type = FilterSectionType.CHECKBOX_LIST,
            isMultiSelect = true,
            options = branchOptions.ifEmpty {
                listOf(FilterOption("default_branch", "Nexus Boutique - Main Branch"))
            }
        )
    )
}

@Composable
fun PaymentListingScreen(
    navController: NavController,
    widthSizeClass: WindowWidthSizeClass,
    onBack: () -> Unit = {},
    onBreadCrumbClick: () -> Unit = {},
    onPaymentClick: (String) -> Unit = {},
    viewModel: SalesViewModel = hiltViewModel(),
    branchViewModel: BranchViewModel = hiltViewModel()
) {
    val tokens = getAdaptiveTokens(widthSizeClass)

    val payments by viewModel.billingPaymentsList.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingBillingPayments.collectAsStateWithLifecycle()
    val isLoadingMore by viewModel.isLoadingMoreBillingPayments.collectAsStateWithLifecycle()
    val canLoadMore by viewModel.canLoadMoreBillingPayments.collectAsStateWithLifecycle()
    val errorMessage by viewModel.billingPaymentsError.collectAsStateWithLifecycle()

    // Fetch branches from BranchViewModel API
    val branchState by branchViewModel.uiState.collectAsStateWithLifecycle()
    val branches = (branchState as? BranchUiState.Success)?.branches ?: emptyList()

    val listState = rememberLazyListState()
    var searchQuery by remember { mutableStateOf("") }

    // Filter drawer state
    val filterDrawerState = rememberFilterDrawerState()
    var filterSections: List<FilterSection> by remember {
        mutableStateOf(buildPaymentFilterSections())
    }

    // Populate branches dynamically from API
    LaunchedEffect(branches) {
        if (branches.isNotEmpty()) {
            filterSections = filterSections.map { section: FilterSection ->
                if (section.title.equals("Branch", ignoreCase = true)) {
                    val previouslySelected: Set<String> = section.options
                        .filter { it.isSelected }
                        .map { it.id }
                        .toSet()

                    val dynamicBranchOptions = branches.map { b ->
                        val label = b.name?.takeIf { it.isNotBlank() } ?: "Main Branch"
                        FilterOption(
                            id = b.id,
                            label = label,
                            isSelected = b.id in previouslySelected
                        )
                    }
                    section.copy(options = dynamicBranchOptions)
                } else {
                    section
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.fetchBillingPayments(page = 1)
        branchViewModel.loadBranches()
    }

    // Infinite scrolling
    LaunchedEffect(listState) {
        snapshotFlow {
            val total = listState.layoutInfo.totalItemsCount
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            total > 0 && lastVisible >= total - 3
        }.collect { nearEnd ->
            if (nearEnd && canLoadMore && !isLoadingMore && !isLoading) {
                viewModel.loadMoreBillingPayments()
            }
        }
    }

    // Active filter badge count
    val activeFilterCount by remember(filterSections) {
        derivedStateOf {
            filterSections.sumOf { section: FilterSection ->
                section.options.count { it.isSelected }
            }
        }
    }

    // Filter payments locally by selected status, payment mode, and branch
    val filteredPayments by remember(payments, filterSections) {
        derivedStateOf {
            val selectedStatuses: List<String> = filterSections
                .find { it.title.equals("Payment Status", ignoreCase = true) }
                ?.options
                ?.filter { it.isSelected }
                ?.map { it.label.lowercase().replace(" ", "_") }
                ?: emptyList()

            val selectedModes: List<String> = filterSections
                .find { it.title.equals("Payment Mode", ignoreCase = true) }
                ?.options
                ?.filter { it.isSelected }
                ?.map { it.label.lowercase() }
                ?: emptyList()

            val selectedBranchIds: List<String> = filterSections
                .find { it.title.equals("Branch", ignoreCase = true) }
                ?.options
                ?.filter { it.isSelected }
                ?.map { it.id }
                ?: emptyList()

            payments.filter { item ->
                val status = item.billing?.paymentStatus?.lowercase() ?: ""
                val mode = item.billing?.paymentMode?.lowercase() ?: ""
                val itemBranchId = item.branch?.id ?: ""

                val matchesStatus = selectedStatuses.isEmpty() ||
                        selectedStatuses.any { status.contains(it) || it.contains(status) }

                val matchesMode = selectedModes.isEmpty() ||
                        selectedModes.any { mode.contains(it) || it.contains(mode) }

                val matchesBranch = selectedBranchIds.isEmpty() ||
                        (itemBranchId.isNotBlank() && itemBranchId in selectedBranchIds)

                matchesStatus && matchesMode && matchesBranch
            }
        }
    }

    CompositionLocalProvider(LocalAppTokens provides tokens) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
            // Header
            Row(Modifier.fillMaxWidth()) {
                TitleBar(title = "All Payments", onClose = onBack)
            }

            HorizontalDivider(color = title_border)

            // Content Area containing Search bar, List, and FilterDrawer below TitleBar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    SearchFilterBar(
                        query = searchQuery,
                        onQueryChange = {
                            searchQuery = it
                            viewModel.onBillingSearchQueryChanged(it)
                        },
                        placeholder = "Search Payment...",
                        filterCount = activeFilterCount,
                        accentColor = BluePrimary,
                        borderColor = BorderGray,
                        textSecondaryColor = mutedText,
                        onFilterClick = { filterDrawerState.open() }
                    )

                    when {
                        isLoading && payments.isEmpty() -> {
                            ListSkeleton()
                        }

                        errorMessage != null && payments.isEmpty() -> {
                            AppErrorState(
                                title = "Failed to load payments",
                                message = errorMessage ?: "Something went wrong",
                                onRetry = { viewModel.fetchBillingPayments(page = 1) }
                            )
                        }

                        filteredPayments.isEmpty() -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No payments found",
                                    fontSize = tokens.bodyMedium,
                                    fontWeight = FontWeight.Normal,
                                    color = mutedText
                                )
                            }
                        }

                        else -> {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxWidth().weight(1f)
                            ) {
                                items(filteredPayments, key = { it.id }) { item ->
                                    val customerName = item.customer?.name?.takeIf { it.isNotBlank() } ?: "Valued Customer"
                                    val billing = item.billing
                                    val balance = billing?.balanceDue ?: 0.0

                                    val paymentStatus = billing?.paymentStatus?.replace("_", " ") ?: "Pending"
                                    val (badgeBg, badgeText) = when (billing?.paymentStatus?.lowercase()) {
                                        "paid", "completed" -> greenBg to greentext
                                        "partially_paid", "partial" -> yellowBg to yellowText
                                        else -> redBg to redText
                                    }

                                    DataCard(
                                        item = item,
                                        code = item.orderCode,
                                        dateText = formatDisplayDate(item.orderDate),
                                        title = customerName,
                                        titleFontWeight = FontWeight.Medium,
                                        titleColor = TextPrimary,
                                        subtitle = "Mode: ${billing?.paymentMode ?: "-"}",
                                        topBadgeText = paymentStatus,
                                        topBadgeBgColor = badgeBg,
                                        topBadgeTextColor = badgeText,
                                        topBadgeShowDot = true,
                                        topBadgeCornerRadius = 20.dp,
                                        showChevron = false,
                                        content = {
                                            Column(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                                                HorizontalDivider(
                                                    color = dividerColor,
                                                    thickness = 1.dp,
                                                    modifier = Modifier.padding(bottom = 8.dp)
                                                )

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    // Amount Paid
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = "Amount Paid",
                                                            color = mutedText,
                                                            fontSize = tokens.caption,
                                                            fontWeight = FontWeight.Normal
                                                        )
                                                        Spacer(Modifier.height(2.dp))
                                                        Text(
                                                            text = "${currencySymbol()}${formatIndianNumber(billing?.paidAmount ?: 0.0)}",
                                                            color = TextPrimary,
                                                            fontSize = tokens.bodyMedium,
                                                            fontWeight = FontWeight.Medium
                                                        )
                                                        Spacer(Modifier.height(2.dp))
                                                        Text(
                                                            text = billing?.paymentType?.replace("_", " ") ?: "Advance",
                                                            color = close_color,
                                                            fontSize = tokens.caption,
                                                            fontWeight = FontWeight.Normal
                                                        )
                                                    }

                                                    // Balance Due
                                                    Column(
                                                        modifier = Modifier.weight(1f),
                                                        horizontalAlignment = Alignment.End
                                                    ) {
                                                        Text(
                                                            text = "Balance Due",
                                                            color = mutedText,
                                                            fontSize = tokens.caption,
                                                            fontWeight = FontWeight.Normal
                                                        )
                                                        Spacer(Modifier.height(2.dp))
                                                        Text(
                                                            text = "${currencySymbol()}${formatIndianNumber(balance)}",
                                                            color = if (balance <= 0.0) greentext else redText,
                                                            fontSize = tokens.bodyMedium,
                                                            fontWeight = FontWeight.Medium
                                                        )
                                                        Spacer(Modifier.height(2.dp))
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(6.dp)
                                                                    .clip(CircleShape)
                                                                    .background(if (balance <= 0.0) greentext else yellowText)
                                                            )
                                                            Spacer(Modifier.width(4.dp))
                                                            Text(
                                                                text = if (balance <= 0.0) "Cleared" else "Due",
                                                                color = close_color,
                                                                fontSize = tokens.caption,
                                                                fontWeight = FontWeight.Normal
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        },
                                        onClick = { onPaymentClick(item.id) }
                                    )
                                }

                                if (isLoadingMore) {
                                    item {
                                        ThreeDotLoading()
                                    }
                                }

                                item { Spacer(Modifier.height(80.dp)) }
                            }
                        }
                    }
                }

                // Slide-over Filter Drawer matching screenshot
                FilterDrawer(
                    state = filterDrawerState,
                    title = "Filter Payments",
                    sections = filterSections,
                    onApply = { updatedSections: List<FilterSection> ->
                        filterSections = updatedSections
                    },
                    onClearAll = {
                        filterSections = filterSections.map { section: FilterSection ->
                            section.copy(
                                options = section.options.map { option: FilterOption ->
                                    option.copy(isSelected = false)
                                }
                            )
                        }
                    }
                )
            }
        }
    }
}

private fun formatDisplayDate(raw: String?): String {
    if (raw.isNullOrBlank()) return "—"
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val date = parser.parse(raw.take(10)) ?: return raw.take(10)
        SimpleDateFormat("dd MMM yyyy", Locale.US).format(date)
    } catch (_: Exception) {
        raw.take(10)
    }
}