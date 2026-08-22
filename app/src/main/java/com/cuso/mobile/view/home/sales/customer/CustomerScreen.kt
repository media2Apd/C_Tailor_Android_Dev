@file:Suppress(
    "UNUSED_VALUE",
    "unused_variable",
    "SpellCheckingInspection",
    "GrazieInspection",
    "AssignedValueIsNeverRead",
    "VariableNeverRead",
    "unused"
)
package com.cuso.mobile.view.home.sales.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.cuso.mobile.R
import com.cuso.mobile.model.sales.CustomerItem
import com.cuso.mobile.ui.theme.BluePrimary
import com.cuso.mobile.ui.theme.BorderGray
import com.cuso.mobile.ui.theme.TextSecondary
import com.cuso.mobile.ui.theme.blackTitle
import com.cuso.mobile.view.composable.CirculerProgressIndicatorSmall
import com.cuso.mobile.view.composable.DataCard
import com.cuso.mobile.view.composable.DataCardField
import com.cuso.mobile.view.composable.DataCardImage
import com.cuso.mobile.view.composable.DeleteModel
import com.cuso.mobile.view.composable.DynamicIslandError
import com.cuso.mobile.view.composable.DynamicIslandSuccess
import com.cuso.mobile.view.composable.FabScaffold
import com.cuso.mobile.view.composable.ListSkeleton
import com.cuso.mobile.view.composable.MenuAction
import com.cuso.mobile.view.composable.ScreenBreadcrumb
import com.cuso.mobile.view.composable.SearchFilterBar
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.viewmodel.CustomerCreateState
import com.cuso.mobile.viewmodel.CustomerDeleteState
import com.cuso.mobile.viewmodel.CustomerUiState
import com.cuso.mobile.viewmodel.CustomerViewModel
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
            } catch (_: Exception) {
            }
        }
        parsedDate?.let {
            val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
            outputFormat.format(it)
        } ?: "—"
    } catch (_: Exception) {
        "—"
    }
}

// -------------------------------------------------------------
// Customer Screen with Infinite Scroll
// -------------------------------------------------------------
@Suppress("UNUSED_PARAMETER")
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
    val listState = rememberLazyListState()

    val uiState by customerViewModel.uiState.collectAsStateWithLifecycle()
    val isLoadingMore by customerViewModel.isLoadingMore.collectAsStateWithLifecycle()
    val canLoadMore by customerViewModel.canLoadMore.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    var searchQuery by remember { mutableStateOf("") }
    var customerPendingDelete by remember { mutableStateOf<CustomerItem?>(null) }

    val deleteState by customerViewModel.deleteState.collectAsStateWithLifecycle()
    val createState by customerViewModel.createState.collectAsStateWithLifecycle()

    var showCreateSuccess by remember { mutableStateOf(false) }
    var showDeleteSuccess by remember { mutableStateOf(false) }
    var deleteSuccessMessage by remember { mutableStateOf("Customer Deleted Successfully") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Infinite scroll listener
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

    // Debounced search
    LaunchedEffect(searchQuery) {
        delay(400)
        customerViewModel.onSearch(searchQuery)
    }

    LaunchedEffect(uiState) {
        if (uiState is CustomerUiState.Error) {
            errorMessage = (uiState as CustomerUiState.Error).message
        }
    }

    LaunchedEffect(deleteState) {
        when (val state = deleteState) {
            is CustomerDeleteState.Success -> {
                deleteSuccessMessage = state.message?.takeIf { it.isNotBlank() }
                    ?: "Customer Deleted Successfully"
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

    LaunchedEffect(createState) {
        when (val state = createState) {
            is CustomerCreateState.Success -> {
                showCreateSuccess = true
                customerViewModel.resetCreateState()
            }
            is CustomerCreateState.Error -> {
                errorMessage = state.message
                customerViewModel.resetCreateState()
            }
            else -> {}
        }
    }

    val isLoading = uiState is CustomerUiState.Loading
    val customers = (uiState as? CustomerUiState.Success)?.customers ?: emptyList()

    FabScaffold(
        fab = null,
        snackbarHostState = snackbarHostState
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
            ) {
                // Top Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TitleBar("Customers", onClose = onClose)
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    ScreenBreadcrumb(segments = listOf("Sales", "Customers"), onClick = { onBreadCrumbClick() })

                    SearchFilterBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        placeholder = "Search Customers...",
                        accentColor = BluePrimary,
                        borderColor = BorderGray,
                        textSecondaryColor = TextSecondary,
                        onFilterClick = { }
                    )
                }

                HorizontalDivider(color = Color(0xFFF0F0F0))

                // Content View
                when {
                    isLoading -> {
                        ListSkeleton()
                    }

                    uiState is CustomerUiState.Error -> {
                        Box(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Warning, null, tint = Color.Red, modifier = Modifier.size(48.dp))
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "Something went wrong, Please try again after sometime",
                                    color = Color.Red,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 32.dp)
                                )
                            }
                        }
                    }

                    uiState is CustomerUiState.Success -> {
                        if (customers.isEmpty()) {
                            Box(
                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.People, null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
                                    Spacer(Modifier.height(8.dp))
                                    Text("No customers found", color = Color.Gray, fontSize = 15.sp)
                                }
                            }
                        } else {
                            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                LazyColumn(
                                    state = listState,
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(bottom = 90.dp)
                                ) {
                                    items(customers, key = { it.id }) { customer ->
                                        val (badgeText, badgeColor) = when (customer.type?.lowercase()) {
                                            "business" -> "Business" to Color(0xFFD97706)
                                            "regular" -> "Regular" to Color(0xFF16A34A)
                                            else -> "Individual" to Color(0xFF3B3BF9)
                                        }

                                        DataCard(
                                            item = customer,
                                            image = DataCardImage(
                                                painter = painterResource(R.drawable.ic_person),
                                                size = 30.dp,
                                                tint = blackTitle,
                                                backgroundColor = Color.Transparent
                                            ),
                                            topBadgeText = badgeText,
                                            topBadgeTextColor = badgeColor,
                                            topBadgeBgColor = badgeColor.copy(alpha = 0.14f),
                                            topBadgeInline = true,
                                            title = customer.name,
                                            subtitle = "Order ID : not found",
                                            footerAsRows = true,
                                            footerFields = listOf(
                                                DataCardField(label = "Email", text = customer.email?.ifBlank { "—" } ?: "—", asRow = true),
                                                DataCardField(label = "Mobile", text = customer.mobile?.ifBlank { "—" } ?: "—", asRow = true),
                                                DataCardField(label = "Gender", text = customer.gender?.ifBlank { "—" } ?: "—", asRow = true),
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
                                                MenuAction("Delete", Icons.Default.Delete, tint = Color(0xFFF44336), textColor = Color(0xFFF44336)) {
                                                    customerPendingDelete = customer
                                                }
                                            )
                                        )
                                    }

                                    if (isLoadingMore) {
                                        item {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(16.dp),
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

            // Dynamic Island Notifications
            if (showCreateSuccess) {
                DynamicIslandSuccess(
                    message = "Customer Created Successfully",
                    onDismiss = { showCreateSuccess = false },
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }

            if (showDeleteSuccess) {
                DynamicIslandSuccess(
                    message = deleteSuccessMessage,
                    onDismiss = { showDeleteSuccess = false },
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }

            if (errorMessage != null) {
                DynamicIslandError(
                    message = errorMessage ?: "An error occurred",
                    onDismiss = { errorMessage = null },
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
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