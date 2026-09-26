@file:Suppress(
    "UNUSED_VALUE",
    "unused_variable",
    "SpellCheckingInspection",
    "GrazieInspection",
    "AssignedValueIsNeverRead",
    "VariableNeverRead",
    "unused"
)
package com.cuso.tailor.view.home.sales.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.cuso.tailor.R
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.sales.CustomerItem
import com.cuso.tailor.ui.theme.BluePrimary
import com.cuso.tailor.ui.theme.BorderGray
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.TextPrimary
import com.cuso.tailor.ui.theme.TextSecondary
import com.cuso.tailor.ui.theme.blackTitle
import com.cuso.tailor.ui.theme.darkGreenBg
import com.cuso.tailor.ui.theme.light_grey
import com.cuso.tailor.ui.theme.mutedText
import com.cuso.tailor.ui.theme.redText
import com.cuso.tailor.ui.theme.title_border
import com.cuso.tailor.ui.theme.whiteBg
import com.cuso.tailor.ui.theme.yellowText
import com.cuso.tailor.view.composable.AppErrorState
import com.cuso.tailor.view.composable.DataCard
import com.cuso.tailor.view.composable.DataCardField
import com.cuso.tailor.view.composable.DataCardImage
import com.cuso.tailor.view.composable.DeleteModel
import com.cuso.tailor.view.composable.DynamicIslandError
import com.cuso.tailor.view.composable.DynamicIslandSuccess
import com.cuso.tailor.view.composable.FabConfig
import com.cuso.tailor.view.composable.FabScaffold
import com.cuso.tailor.view.composable.FilterDrawer
import com.cuso.tailor.view.composable.FilterOption
import com.cuso.tailor.view.composable.FilterSection
import com.cuso.tailor.view.composable.ListSkeleton
import com.cuso.tailor.view.composable.MenuAction
import com.cuso.tailor.view.composable.SearchFilterBar
import com.cuso.tailor.view.composable.ThreeDotLoading
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.view.composable.rememberFilterDrawerState
import com.cuso.tailor.viewmodel.CustomerDeleteState
import com.cuso.tailor.viewmodel.CustomerUiState
import com.cuso.tailor.viewmodel.CustomerViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

fun String?.toDisplayDate(): String {
    if (this.isNullOrBlank()) return "—"
    return try {
        val inputFormats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd"
        )
        var parsedDate: java.util.Date? = null
        for (pattern in inputFormats) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.ENGLISH)
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                parsedDate = sdf.parse(this)
                if (parsedDate != null) break
            } catch (_: Exception) {}
        }
        parsedDate?.let {
            val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
            outputFormat.format(it)
        } ?: "—"
    } catch (_: Exception) {
        "—"
    }
}

private fun getDefaultCustomerFilterSections(): List<FilterSection> = listOf(
    FilterSection(
        title = "Customer Type",
        options = listOf(
            FilterOption("individual", "Individual"),
            FilterOption("corporate", "Corporate")
        ),
        isMultiSelect = true
    ),
    FilterSection(
        title = "Gender",
        options = listOf(
            FilterOption("male", "Male"),
            FilterOption("female", "Female"),
            FilterOption("other", "Other")
        ),
        isMultiSelect = true
    )
)

// -------------------------------------------------------------
// Customer Screen with Infinite Scroll and Filter Drawer
// -------------------------------------------------------------
@Composable
fun CustomerScreen(
    navController: NavController,
    customerViewModel: CustomerViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
    onCreateCustomer: () -> Unit = {},
    onView: (CustomerItem) -> Unit = {},
    onEdit: (CustomerItem) -> Unit = {},
    onDelete: (CustomerItem) -> Unit = {},
    onBreadCrumbClick: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val listState = rememberLazyListState()

    val uiState by customerViewModel.uiState.collectAsStateWithLifecycle()
    val isLoadingMore by customerViewModel.isLoadingMore.collectAsStateWithLifecycle()
    val canLoadMore by customerViewModel.canLoadMore.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    var searchQuery by remember { mutableStateOf("") }
    var customerPendingDelete by remember { mutableStateOf<CustomerItem?>(null) }

    val filterDrawerState = rememberFilterDrawerState()
    var filterSections by remember { mutableStateOf(getDefaultCustomerFilterSections()) }

    val deleteState by customerViewModel.deleteState.collectAsStateWithLifecycle()
    val createState by customerViewModel.createState.collectAsStateWithLifecycle()

    var showCreateSuccess by remember { mutableStateOf(false) }
    var showDeleteSuccess by remember { mutableStateOf(false) }
    var deleteSuccessMessage by remember { mutableStateOf("Customer Deleted Successfully") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(listState) {
        snapshotFlow {
            val info = listState.layoutInfo
            val total = info.totalItemsCount
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            total > 0 && lastVisible >= total - 3
        }
            .distinctUntilChanged()
            .collect { nearEnd ->
                if (nearEnd && canLoadMore && !isLoadingMore) {
                    customerViewModel.loadMoreCustomers()
                }
            }
    }

    LaunchedEffect(searchQuery) {
        delay(400)
        customerViewModel.onSearch(searchQuery)
    }

    LaunchedEffect(deleteState) {
        when (val state = deleteState) {
            is CustomerDeleteState.Success -> {
                deleteSuccessMessage = state.message?.takeIf { it.isNotBlank() } ?: "Customer Deleted Successfully"
                showDeleteSuccess = true
                customerViewModel.resetDeleteState()
            }
            is CustomerDeleteState.Error -> {
                errorMessage = state.message
                customerViewModel.resetDeleteState()
            }
            else -> {}
        }
    }

    // Active filter counter for the badge on the filter button
    val activeFilterCount by remember(filterSections) {
        derivedStateOf {
            filterSections.sumOf { section ->
                section.options.count { it.isSelected }
            }
        }
    }

    val isLoading = uiState is CustomerUiState.Loading
    val customers = (uiState as? CustomerUiState.Success)?.customers ?: emptyList()

    // Filter customers locally by selected Type and Gender options
    val filteredCustomers by remember(customers, filterSections) {
        derivedStateOf {
            val selectedTypes = filterSections.find { it.title == "Customer Type" }
                ?.options?.filter { it.isSelected }?.map { it.label } ?: emptyList()
            val selectedGenders = filterSections.find { it.title == "Gender" }
                ?.options?.filter { it.isSelected }?.map { it.label } ?: emptyList()

            customers.filter { customer ->
                val matchesType = selectedTypes.isEmpty() ||
                        selectedTypes.any { it.equals(customer.type, ignoreCase = true) }

                val matchesGender = selectedGenders.isEmpty() ||
                        selectedGenders.any { it.equals(customer.gender, ignoreCase = true) }

                matchesType && matchesGender
            }
        }
    }

    FabScaffold(
        fab = FabConfig(
            label = "Create Customer",
            icon = Icons.Default.Add,
            onClick = onCreateCustomer,
            bottomPadding = 50.dp
        ),
        snackbarHostState = snackbarHostState
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
            ) {
                // Fixed top TitleBar: Never obscured by FilterDrawer
                TitleBar("Customers", onClose = onClose)

                HorizontalDivider(color = title_border)

                // Container hosting Content and slide-in Filter Drawer below TitleBar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        SearchFilterBar(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            placeholder = "Search Customers...",
                            filterCount = activeFilterCount,
                            accentColor = BluePrimary,
                            borderColor = BorderGray,
                            textSecondaryColor = TextSecondary,
                            onFilterClick = { filterDrawerState.open() }
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            when {
                                isLoading && filteredCustomers.isEmpty() -> {
                                    ListSkeleton()
                                }
                                uiState is CustomerUiState.Error && filteredCustomers.isEmpty() -> {
                                    AppErrorState(
                                        title = "Failed to load Customers",
                                        message = "Something went wrong. Please check your connection and try again.",
                                        onRetry = { customerViewModel.refresh() }
                                    )
                                }
                                filteredCustomers.isEmpty() -> {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(tokens.screenPadding * 2f),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            val hasFilters = filterSections.any { s -> s.options.any { it.isSelected } }
                                            Icon(
                                                Icons.Default.People,
                                                contentDescription = null,
                                                tint = mutedText,
                                                modifier = Modifier.size(48.dp)
                                            )
                                            Spacer(Modifier.height(8.dp))
                                            Text(
                                                text = if (searchQuery.isNotBlank() || hasFilters) "No matching customers found" else "No customers found",
                                                color = TextPrimary,
                                                fontSize = tokens.h2,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                text = if (searchQuery.isNotBlank() || hasFilters) "Try adjusting your search query or filters." else "Get started by adding your first customer.",
                                                color = mutedText,
                                                fontSize = tokens.bodyMedium
                                            )
                                            if (!hasFilters && searchQuery.isBlank()) {
                                                Spacer(Modifier.height(16.dp))
                                                Button(
                                                    onClick = onCreateCustomer,
                                                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f)
                                                ) {
                                                    Text("Create Customer", color = whiteBg, fontSize = tokens.bodyMedium)
                                                }
                                            }
                                        }
                                    }
                                }
                                else -> {
                                    LazyColumn(
                                        state = listState,
                                        modifier = Modifier
                                            .fillMaxSize(),
                                        contentPadding = PaddingValues(bottom = 90.dp)
                                    ) {
                                        items(filteredCustomers, key = { it.id }) { customer ->
                                            val (badgeText, badgeColor) = when (customer.type?.lowercase()) {
                                                "business", "corporate" -> "Corporate" to yellowText
                                                "regular" -> "Regular" to darkGreenBg
                                                else -> customer.displayType to Primary
                                            }

                                            val imageUrl = customer.profilePicture?.url
                                            val hasProfilePic = !imageUrl.isNullOrBlank()

                                            DataCard(
                                                item = customer,
                                                onClick = { onView(customer) },
                                                image = if (hasProfilePic) {
                                                    DataCardImage(
                                                        url = imageUrl,
                                                        size = 30.dp,
                                                        shape = CircleShape,
                                                        backgroundColor = Color.Transparent
                                                    )
                                                } else {
                                                    DataCardImage(
                                                        painter = painterResource(R.drawable.ic_person),
                                                        size = 30.dp,
                                                        shape = CircleShape,
                                                        tint = blackTitle,
                                                        backgroundColor = light_grey
                                                    )
                                                },
                                                topBadgeText = badgeText,
                                                topBadgeTextColor = badgeColor,
                                                topBadgeBgColor = badgeColor.copy(alpha = 0.14f),
                                                topBadgeInline = true,
                                                title = customer.name,
                                                subtitle = customer.customerCode?.takeIf { it.isNotBlank() } ?: "—",
                                                footerAsRows = true,
                                                footerFields = listOf(
                                                    DataCardField(
                                                        label = "Email",
                                                        text = customer.email?.takeIf { it.isNotBlank() } ?: "—",
                                                        asRow = true
                                                    ),
                                                    DataCardField(
                                                        label = "Mobile",
                                                        text = customer.mobile?.takeIf { it.isNotBlank() } ?: "—",
                                                        asRow = true
                                                    ),
                                                    DataCardField(
                                                        label = "Gender",
                                                        text = customer.gender?.takeIf { it.isNotBlank() } ?: "—",
                                                        asRow = true
                                                    ),
                                                    DataCardField(
                                                        label = "Location",
                                                        text = customer.location.ifBlank { "—" }.let { loc ->
                                                            if (loc.length > 30) "${loc.take(30)}..." else loc
                                                        },
                                                        asRow = true
                                                    )
                                                ),
                                                actions = listOf(
                                                    MenuAction("View", Icons.Default.Visibility) { onView(customer) },
                                                    MenuAction("Edit", Icons.Default.Edit) { onEdit(customer) },
                                                    MenuAction("Delete", Icons.Default.Delete, tint = redText, textColor = redText) {
                                                        customerPendingDelete = customer
                                                    }
                                                )
                                            )
                                        }

                                        if (isLoadingMore) {
                                            item {
                                                ThreeDotLoading()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // FilterDrawer overlay rendered strictly below TitleBar
                    FilterDrawer(
                        state = filterDrawerState,
                        title = "Filter Customers",
                        sections = filterSections,
                        onApply = { updatedSections -> filterSections = updatedSections },
                        onClearAll = {
                            filterSections = filterSections.map { section ->
                                section.copy(options = section.options.map { option -> option.copy(isSelected = false) })
                            }
                        }
                    )
                }
            }

            DynamicIslandSuccess(
                message = when {
                    showCreateSuccess -> "Customer Created Successfully"
                    showDeleteSuccess -> deleteSuccessMessage
                    else -> null
                },
                onDismiss = {
                    showCreateSuccess = false
                    showDeleteSuccess = false
                },
                modifier = Modifier.align(Alignment.TopCenter)
            )

            DynamicIslandError(
                message = errorMessage,
                onDismiss = { errorMessage = null },
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }

    customerPendingDelete?.let { customer ->
        DeleteModel(
            title = "Delete Customer",
            message = "Are you sure you want to delete \"${customer.name}\"? This action cannot be undone.",
            onDismiss = { customerPendingDelete = null },
            onDelete = {
                onDelete(customer)
                customerPendingDelete = null
            }
        )
    }
}