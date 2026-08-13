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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.cuso.mobile.model.sales.CustomerItem
import com.cuso.mobile.ui.theme.BluePrimary
import com.cuso.mobile.ui.theme.BorderGray
import com.cuso.mobile.ui.theme.TextSecondary
import com.cuso.mobile.view.composable.DynamicIslandError
import com.cuso.mobile.view.composable.DynamicIslandSuccess
import com.cuso.mobile.view.composable.ScreenBreadcrumb
import com.cuso.mobile.view.composable.DataCard
import com.cuso.mobile.view.composable.DataCardField
import com.cuso.mobile.view.composable.DataCardImage
import com.cuso.mobile.view.composable.FabScaffold
import com.cuso.mobile.view.composable.ListSkeleton
import com.cuso.mobile.view.composable.MenuAction
import com.cuso.mobile.view.composable.SearchFilterBar
import com.cuso.mobile.viewmodel.CustomerCreateState
import com.cuso.mobile.viewmodel.CustomerDeleteState
import com.cuso.mobile.viewmodel.CustomerUiState
import com.cuso.mobile.viewmodel.CustomerViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import com.cuso.mobile.R
import com.cuso.mobile.ui.theme.blackTitle
import com.cuso.mobile.view.composable.DeleteModel
import com.cuso.mobile.view.composable.TitleBar

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

// ─────────────────────────────────────────────────────────────
// Screen
// ─────────────────────────────────────────────────────────────
@Suppress("UNUSED_PARAMETER")
@Composable
fun CustomerScreen(
    navController: NavController,
    customerState: CustomerUiState,
    onSearch: (String) -> Unit = {},
    onTypeFilterChange: (String) -> Unit = {},
    onPageChange: (Int) -> Unit = {},
    onItemsPerPageChange: (Int) -> Unit = {},
    onClose: () -> Unit = {},
    onCreateCustomer: () -> Unit = {},
    onView: (CustomerItem) -> Unit = {},
    onEdit: (CustomerItem) -> Unit = {},
    onDelete: (CustomerItem) -> Unit = {},
    onBreadCrumbClick: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }

    var searchQuery by remember { mutableStateOf("") }
    var customerPendingDelete by remember { mutableStateOf<CustomerItem?>(null) }

    val customerViewModel: CustomerViewModel = hiltViewModel()
    val uiState by customerViewModel.uiState.collectAsStateWithLifecycle()
    val currentPage by customerViewModel.currentPageFlow.collectAsStateWithLifecycle()
    val pageSize by customerViewModel.pageSizeFlow.collectAsStateWithLifecycle()

    val deleteState by customerViewModel.deleteState.collectAsStateWithLifecycle()
    val createState by customerViewModel.createState.collectAsStateWithLifecycle()

    var showCreateSuccess by remember { mutableStateOf(false) }
    var showDeleteSuccess by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(customerState) {
        if (customerState is CustomerUiState.Error) {
            errorMessage = customerState.message
        }
    }

    LaunchedEffect(deleteState) {
        when (val state = deleteState) {
            is CustomerDeleteState.Success -> {
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

    val isLoading = customerState is CustomerUiState.Loading
    val customers = (customerState as? CustomerUiState.Success)?.customers ?: emptyList()
    val total = (customerState as? CustomerUiState.Success)?.total ?: 0

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
                // ── Top Bar ──────────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TitleBar("Customers", onClose = onClose)
                }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        ScreenBreadcrumb(segments = listOf("Sales", "Customers"), onClick = { onBreadCrumbClick() })

                        SearchFilterBar(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                            placeholder = "Search Customers...",
                            accentColor = BluePrimary,
                            borderColor = BorderGray,
                            textSecondaryColor = TextSecondary,
                            onFilterClick = { }
                        )
                    }


                HorizontalDivider(color = Color(0xFFF0F0F0))

                // ── Content ──────────────────────────────────────────
                when {
                    isLoading -> {
                        ListSkeleton()
                    }

                    customerState is CustomerUiState.Error -> {
                        Box(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Warning, null, tint = Color.Red, modifier = Modifier.size(48.dp))
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Something went wrong, Please try again after sometime",
                                    color = Color.Red,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 32.dp)
                                )
                            }
                        }
                    }

                    customerState is CustomerUiState.Success -> {
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
                            Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .verticalScroll(rememberScrollState())
                                ) {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        customers.forEach { customer ->
                                            val (badgeText, badgeColor) = when (customer.type?.lowercase()) {
                                                "business" -> "Business" to Color(0xFFD97706)
                                                "regular" -> "Regular" to Color(0xFF16A34A)
                                                else -> "Individual" to Color(0xFF3B3BF9)
                                            }

                                            DataCard(
                                                item = customer,
                                                image = DataCardImage(
                                                    painter=painterResource(R.drawable.ic_person),
                                                    size = 30.dp,
                                                    tint = blackTitle,
                                                    backgroundColor = Color.Transparent
                                                ),
                                                topBadgeText = badgeText,
                                                topBadgeTextColor = badgeColor,
                                                topBadgeBgColor = badgeColor.copy(alpha = 0.14f),
                                                topBadgeInline = true,
                                                title = customer.name,
                                                subtitle = "Order ID : not found"
                                                    .takeIf { it.isNotBlank() }
                                                    ?: "Order ID : not found",
                                                footerAsRows = true,
                                                footerFields = listOf(
                                                    DataCardField(
                                                        label = "Email",
                                                        text = customer.email?.ifBlank { "—" } ?: "—",
                                                        asRow = true),
                                                    DataCardField(
                                                        label = "Mobile",
                                                        text = customer.mobile?.ifBlank { "—" } ?: "—",
                                                        asRow = true),
                                                    DataCardField(
                                                        label = "Gender",
                                                        text = customer.gender?.ifBlank { "—" } ?: "—",
                                                        asRow = true),
                                                    DataCardField(
                                                        label = "Location",
                                                        text = customer.location.ifBlank { "—" },
                                                        asRow = true)
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
                                    }
                                }

                            }
                        }
                    }
                }
            }

            // ── Dynamic Island Notifications ──────────────────────────
            if (showCreateSuccess) {
                DynamicIslandSuccess(
                    message = "Customer Created Successfully",
                    onDismiss = { showCreateSuccess = false },
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }

            if (showDeleteSuccess) {
                DynamicIslandSuccess(
                    message = "Customer Deleted Successfully",
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
            onDismiss = {
                customerPendingDelete = null
            },
            onDelete = {
                onDelete(customer)
                showDeleteSuccess = true
                customerPendingDelete = null
            }
        )
    }
}