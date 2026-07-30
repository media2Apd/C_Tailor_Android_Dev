@file:Suppress(
    "UNUSED_VALUE",
    "UNUSED_PARAMETER",
    "unused",
    "UNCHECKED_CAST",
    "DEPRECATION",
    "AssignedValueIsNeverRead",
    "GrazieInspection",
    "SpellCheckingInspection",
    "unusedvariable",
    "AssignedVariableIsNeverRead",
    "UNUSED_VARIABLE",
    "KotlinConstantConditions", "VariableNeverRead"
)

package com.cuso.mobile.view.home

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.cuso.mobile.viewmodel.HomeViewModel
import com.cuso.mobile.viewmodel.Authenticate
import androidx.compose.material3.DropdownMenu
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Density
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.cuso.mobile.database.entities.SalesStatusEntity
import com.cuso.mobile.model.sales.CategoryItem
import com.cuso.mobile.model.Organization
import com.cuso.mobile.model.Settings
import com.cuso.mobile.model.sales.StaffDto
import com.cuso.mobile.model.Subscription
import com.cuso.mobile.model.User
import com.cuso.mobile.view.home.branch.BranchSettingsScreen
import com.cuso.mobile.view.home.department.DepartmentSettingsScreen
import com.cuso.mobile.view.home.designation.DesignationScreen
import com.cuso.mobile.view.home.sales.GarmentTypeContent
import com.cuso.mobile.view.home.sales.SalesSettingsScreen
import com.cuso.mobile.view.home.sidebar.FullSideBar
import com.cuso.mobile.view.home.sidebar.SalesSideBar
import com.cuso.mobile.view.home.sales.sales_order.CreateOrderScreen
import com.cuso.mobile.view.home.sales.sales_order.SalesOrderScreen
import com.cuso.mobile.view.home.sales.sales_order.CreateOrderNextStep
import com.cuso.mobile.view.home.sales.sales_order.OrderReviewData
import com.cuso.mobile.view.home.reusablecomposables.FilterOption
import com.cuso.mobile.view.home.reusablecomposables.FilterSection
import com.cuso.mobile.view.home.reusablecomposables.FilterSectionType
import com.cuso.mobile.R
import com.cuso.mobile.view.home.sidebar.ModulesPanel
import com.cuso.mobile.view.home.sidebar.SidebarConfig
import com.cuso.mobile.view.home.sidebar.buildNavigationKey
// ─────────────────────────────────────────────────────────────
// HomeScreenContent
// ─────────────────────────────────────────────────────────────
import com.cuso.mobile.model.ActiveOrderItem
import com.cuso.mobile.model.sales.CustomerItem
import com.cuso.mobile.model.DashboardStatDto
import com.cuso.mobile.model.OperationItem
import com.cuso.mobile.view.home.sales.customer.CustomerScreen
import com.cuso.mobile.view.home.sales.measurements.MeasurementsScreen
import com.cuso.mobile.viewmodel.CustomerViewModel
import com.cuso.mobile.viewmodel.DashboardUiState
import com.cuso.mobile.viewmodel.DashboardViewModel
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.ColorFilter
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.cuso.mobile.view.home.inventory.InventoryViewOne
import com.cuso.mobile.ui.theme.PrimaryBorder
import com.cuso.mobile.ui.theme.modelBg
import com.cuso.mobile.ui.theme.modelBorder
import com.cuso.mobile.ui.theme.statLogoBg
import com.cuso.mobile.view.composable.CirculerProgressIndicatorReuse
import com.cuso.mobile.view.home.finance.AllPaymentScreen
import com.cuso.mobile.view.home.finance.AllSuppliersScreen
import com.cuso.mobile.view.home.finance.ChartOfAccountScreen
import com.cuso.mobile.view.home.finance.ExpensesScreen
import com.cuso.mobile.view.home.finance.FinanceCustomerScreen
import com.cuso.mobile.view.home.finance.FinanceInvoiceScreen
import com.cuso.mobile.view.home.finance.LedgerScreen
import com.cuso.mobile.view.home.finance.ManualJournalEntryScreen
import com.cuso.mobile.view.home.finance.PaymentDetailScreen
import com.cuso.mobile.view.home.finance.SupplierDetailScreen
import com.cuso.mobile.view.home.finance.SupplierRow
import com.cuso.mobile.view.home.finance.TrialBalanceScreen
import com.cuso.mobile.view.home.hr.AllEmployeesScreen
import com.cuso.mobile.view.home.hr.EmployeeOnboardingScreen
import com.cuso.mobile.view.home.inventory.AdjustmentType
import com.cuso.mobile.view.home.inventory.AllItemGroupScreen
import com.cuso.mobile.view.home.inventory.CreateItemGroupScreen
import com.cuso.mobile.view.home.inventory.CreateItemScreen
import com.cuso.mobile.view.home.inventory.CreatePurchaseOrderScreen
import com.cuso.mobile.view.home.inventory.InventoryScreen
import com.cuso.mobile.view.home.inventory.LowStockAlertsScreen
import com.cuso.mobile.view.home.inventory.LowStockItem
import com.cuso.mobile.view.home.logistics.DeliveryDetailScreen
import com.cuso.mobile.view.home.logistics.DeliveryManagementScreen
import com.cuso.mobile.view.home.logistics.OrderTrackingScreen
import com.cuso.mobile.view.home.profile.ProfileSettingsScreen
import com.cuso.mobile.view.home.reports.SalesOrderReportsScreen
import com.cuso.mobile.view.home.reusablecomposables.DashboardSkeleton
import com.cuso.mobile.view.home.sales.customer.CustomerDetailScreen
import com.cuso.mobile.view.home.sales.ordermanagement.OrderManagementScreen
import com.cuso.mobile.view.home.sales.pricing.PricingScreen
import com.cuso.mobile.view.home.sales.quotation.CreateQuotationScreen
import com.cuso.mobile.view.home.sales.quotation.QuotationScreen
import com.cuso.mobile.view.home.sales.sales_order.toOrderReviewData
import com.cuso.mobile.viewmodel.CustomerDeleteState
import com.cuso.mobile.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch
// Add these imports for the Lead screens
import com.cuso.mobile.view.home.sales.lead.LeadScreenContent
import com.cuso.mobile.view.home.sales.lead.CreateLeadScreen
import com.cuso.mobile.view.home.sales.lead.ViewLeadScreen
import com.cuso.mobile.view.home.sales.lead.EditLeadScreen
import com.cuso.mobile.view.home.sales.ordermanagement.OrderDetailScreen
import com.cuso.mobile.view.home.sales.sales_order.OrderOverviewScreen
import com.cuso.mobile.view.home.services.AlterationManagementScreen
import com.cuso.mobile.view.home.services.CreateAlterationManagementScreen
import com.cuso.mobile.view.home.services.CreateServiceRequest
import com.cuso.mobile.view.home.services.CustomerFeedbackScreen
import com.cuso.mobile.view.home.services.FeedbackDetailScreen
import com.cuso.mobile.view.home.services.OrderDetails
import com.cuso.mobile.view.home.services.ServiceDetails
import com.cuso.mobile.view.home.services.ServiceOrderDetailsScreen
import com.cuso.mobile.view.home.services.ServiceRequestScreen
import com.cuso.mobile.viewmodel.HrViewModel
import com.cuso.mobile.viewmodel.ProfileViewModel
import com.example.tracking.TrackingOverviewScreen

// ── Design tokens (Primary color used everywhere for icons / accents) ──
val LeadPrimary = Color(0xFF3B3BF9)
val LeadPrimarySoft = Color(0xFFEEEEFE)
val LeadTextMuted = Color(0xFF9CA3AF)
//
//// ── Data classes ──
//
//data class LeadItem(
//    val header: String,
//    val value: Float
//)
//
//data class ControlItem(
//    val controls: String
//)

// ─────────────────────────────────────────────────────────────
// HomeScreen
// ─────────────────────────────────────────────────────────────

@Suppress("UnusedMaterial3ScaffoldPaddingParameter","UNUSED_PARAMETER")
@Composable
fun HomeScreen(navController: NavHostController) {
    val viewModel: HomeViewModel = hiltViewModel()
    val authViewModel: Authenticate = hiltViewModel()
    val hrViewModel: HrViewModel = hiltViewModel()



    val token: String = authViewModel.tokens.value?.accessToken ?: ""
    val isLoggedOut: Boolean by viewModel.isLoggedOut.collectAsStateWithLifecycle(initialValue = false)
    val screenStack = remember { mutableStateListOf("home") }
    val currentScreen: String = screenStack.last()


    val profileViewModel: ProfileViewModel = hiltViewModel()

    var isDrawerOpen by remember { mutableStateOf(false) }
    var pendingOrderReviewData by remember { mutableStateOf<OrderReviewData?>(null) }
    val customerViewModel: CustomerViewModel = hiltViewModel()
    val customerUiState by customerViewModel.uiState.collectAsStateWithLifecycle()
    var selectedCustomer by remember { mutableStateOf<CustomerItem?>(null) }
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    var selectedOrderId by remember { mutableStateOf<String?>(null) }   // ✅ ADD THIS LINE
    val context = LocalContext.current

    // ✅ NEW — Finance > Trial Balance > Ledger flow
    var selectedLedgerAccountId by remember { mutableStateOf<String?>(null) }
    var selectedLedgerAccountName by remember { mutableStateOf("Ledger") }


    //delete
    val deleteState by customerViewModel.deleteState.collectAsState()   // ✅ this needs to exist
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var selectedManagementOrderId by remember { mutableStateOf<String?>(null) }

    // ✅ NEW — Finance > Accounts Receivable > Sales Invoices flow
    // ✅ NEW — Finance > Accounts Receivable > Sales Invoices flow
    var selectedInvoiceId by remember { mutableStateOf<String?>(null) }

    // ✅ NEW — Inventory > Item Detail flow
    var selectedInventoryItemId by remember { mutableStateOf<String?>(null) }

    var selectedLowStockItem by remember { mutableStateOf<LowStockItem?>(null) }
    // ✅ NEW — HR > Employee Onboarding flow (Create / View / Edit)
    var employeeScreenMode by remember { mutableStateOf(com.cuso.mobile.view.home.hr.ScreenMode.CREATE) }
    var selectedEmployeeId by remember { mutableStateOf<String?>(null) }

    var isSalesSettingsMode by remember { mutableStateOf(false) }

    var showModulesPanel by remember { mutableStateOf(false) }   // ✅ NEW
    var modulesPanelInitialExpanded by remember { mutableStateOf<String?>(null) }   // ✅ NEW — set by breadcrumb clicks
    val orderOverviewViewModel: com.cuso.mobile.viewmodel.OrderOverviewViewModel = hiltViewModel()
    val editOverviewState by orderOverviewViewModel.overviewState.collectAsStateWithLifecycle()
    var editOrderId by remember { mutableStateOf<String?>(null) }
    var editingPricingId by remember { mutableStateOf<String?>(null) }   // ✅ ADD THIS — fixes "Unresolved reference"

    var quotationScreenMode by remember { mutableStateOf("create") }

    var selectedSupplier by remember { mutableStateOf<SupplierRow?>(null) }

    // ✅ NEW — Services > Customer Feedback flow
    var selectedFeedbackId by remember { mutableStateOf<String?>(null) }

    // ✅ NEW — Inventory > Item Groups flow
    var selectedItemGroupId by remember { mutableStateOf<String?>(null) }

    fun navigateTo(screen: String) {
        if (screenStack.lastOrNull() != screen) screenStack.add(screen)
    }

    // Pop the stack — automatically returns to whatever screen was visited last
    fun goBack() {
        if (screenStack.size > 1) screenStack.removeAt(screenStack.lastIndex)
    }

    // Used only for bottom-nav tab taps (Home/Orders/Reports) — resets stack to a fresh root
    fun resetToHome() {
        screenStack.clear()
        screenStack.add("home")
    }

    LaunchedEffect(editOrderId) {
        editOrderId?.let { orderOverviewViewModel.fetchSalesOverview(it) }
    }
    LaunchedEffect(Unit) {
        profileViewModel.loadOrganization("")
    }
    LaunchedEffect(token) {
        if (token.isNotEmpty()) {
            Log.d("ORG", "Calling loadOrganization from Home")
            profileViewModel.loadOrganization(token)
        }
    }
    LaunchedEffect(profileViewModel.uiState.collectAsState().value) {
        Log.d("ORG_STATE", profileViewModel.uiState.value.toString())
    }

    LaunchedEffect(editOverviewState) {
        // ✅ Only act on this state if an Edit flow is actually in progress.
        // Prevents accidental navigation when the OrderOverviewScreen (View flow)
        // updates the same shared ViewModel instance's state.
        if (editOrderId == null) return@LaunchedEffect

        when (val s = editOverviewState) {
            is com.cuso.mobile.viewmodel.OrderOverviewState.Success -> {
                pendingOrderReviewData = s.data.toOrderReviewData()
                navigateTo("create_order")
                editOrderId = null
            }
            is com.cuso.mobile.viewmodel.OrderOverviewState.Error -> {
                Toast.makeText(context, "Failed to load order for editing", Toast.LENGTH_SHORT).show()
                editOrderId = null
            }
            else -> Unit
        }
    }
    LaunchedEffect(isLoggedOut) {
        if (isLoggedOut) {
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        }
    }
    LaunchedEffect(currentScreen) {
        if (currentScreen == "sales_customers") {
            customerViewModel.loadCustomers()
        }
    }
    LaunchedEffect(deleteState) {
        when (val state = deleteState) {
            is CustomerDeleteState.Success -> {
                coroutineScope.launch { snackbarHostState.showSnackbar("Customer deleted successfully") }
                customerViewModel.resetDeleteState()
            }
            is CustomerDeleteState.Error -> {
                coroutineScope.launch { snackbarHostState.showSnackbar(state.message) }
                customerViewModel.resetDeleteState()
            }
            else -> {}
        }
    }



    val showHomePanel = currentScreen == "settings" ||
            currentScreen == "home_organization_profile" ||
            currentScreen == "home_branch_management" ||
            currentScreen == "home_department_teams" ||
            currentScreen == "home_designation"

    val showSalesPanel = isSalesSettingsMode

    // ✅ NEW — System back button handling
    BackHandler(enabled = isDrawerOpen || showModulesPanel || screenStack.size > 1) {
        when {
            // Priority 1: close overlays first
            showModulesPanel -> showModulesPanel = false
            isDrawerOpen -> isDrawerOpen = false

            // Priority 2: clear any screen-specific selection state before popping back
            currentScreen == "sales_garment_type" -> {
                isSalesSettingsMode = true
                goBack()
            }
            currentScreen == "sales_settings" -> {
                isSalesSettingsMode = false
                goBack()
            }
            currentScreen == "sales_lead" -> {
                isSalesSettingsMode = false
                goBack()
            }
            currentScreen == "sales_pricing_quotation" -> {
                isSalesSettingsMode = false
                goBack()
            }
            currentScreen == "create_order_review" -> {
                pendingOrderReviewData = null
                goBack()
            }
            currentScreen == "finance_invoice_detail" -> {
                selectedInvoiceId = null
                goBack()
            }
            currentScreen == "finance_ledger" -> {
                selectedLedgerAccountId = null
                goBack()
            }
            currentScreen == "finance_supplier_detail" -> {
                selectedSupplier = null
                goBack()
            }
            currentScreen == "inventory_item_detail" -> {
                selectedInventoryItemId = null
                goBack()
            }

            currentScreen == "inventory_create_purchase_order" -> {
                selectedLowStockItem = null
                goBack()
            }
            currentScreen == "inventory_create_item_group" -> {
                selectedItemGroupId = null
                goBack()
            }
            currentScreen == "hr_employee_onboarding" -> {
                selectedEmployeeId = null
                goBack()
            }
            currentScreen == "feedback_detail" -> {
                selectedFeedbackId = null
                goBack()
            }
            currentScreen == "view_customer" || currentScreen == "edit_customer" -> {
                selectedCustomer = null
                goBack()
            }
            currentScreen == "order_overview" -> {
                selectedOrderId = null
                goBack()
            }
            currentScreen == "order_management_overview" -> {
                selectedManagementOrderId = null
                goBack()
            }
            currentScreen == "payment_detail_screen" -> {
                goBack()
            }
            currentScreen == "create_order" -> {
                pendingOrderReviewData = null
                goBack()
            }

            // Default — just pop the stack, lands on whatever was visited before
            else -> goBack()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0),
            topBar = {
                TopBar(
                    onProfileClick = {
                        navigateTo("profile-settings")
                    }
                )
            },
            bottomBar = {
                BottomBar(
                    navController = navController,
                    isSettingsOpen = showHomePanel || showSalesPanel,
                    currentScreen = currentScreen,
                    isDrawerOpen = isDrawerOpen,
                    onDrawerToggle = { isDrawerOpen = !isDrawerOpen },
                    onDrawerClose = { isDrawerOpen = false },
                    onSettingsClick = {
                        if (
                            currentScreen == "sales_lead" ||
                            currentScreen == "create_lead" ||
                            currentScreen == "view_lead" ||
                            currentScreen == "edit_lead"
                        ) {
                            isSalesSettingsMode = true
                            navigateTo("sales_settings")
                        } else {
                            if (showHomePanel || showSalesPanel) {
                                isSalesSettingsMode = false
                                resetToHome()   // closing the settings panel — treat as returning to a fresh home, not "back one step"
                            } else {
                                navigateTo("settings")
                            }
                        }
                    },
                    onMenuItemClick = { route ->
                        when (route) {
                            "home_organization_profile" -> {
                                navigateTo("home_organization_profile")
                                isDrawerOpen = false
                            }

                            "home_branch_management" -> {
                                navigateTo("home_branch_management")
                                isDrawerOpen = false
                            }

                            "home_department_teams" -> {
                                navigateTo("home_department_teams")
                                isDrawerOpen = false
                            }

                            "home_designation" -> {
                                navigateTo("home_designation")
                                isDrawerOpen = false
                            }

                            "sales_lead" -> {
                                isSalesSettingsMode = false
                                navigateTo("sales_lead")
                                isDrawerOpen = false
                            }

                            "sales_customers" -> {
                                isSalesSettingsMode = false
                                navigateTo("sales_customers")
                                isDrawerOpen = false
                            }
                            "finance_expenses" -> {
                                isSalesSettingsMode = false
                                navigateTo("finance_expenses")
                                isDrawerOpen = false
                            }

                            "sales_measurements" -> {
                                isSalesSettingsMode = false
                                navigateTo("sales_measurements")
                                isDrawerOpen = false
                            }

                            "sales_sales_orders", "sales_sales_&_orders" -> {
                                isSalesSettingsMode = false
                                navigateTo("sales_sales_orders")
                                isDrawerOpen = false
                            }

                            "sales_orders" -> {
                                isSalesSettingsMode = false
                                navigateTo("sales_orders")
                                isDrawerOpen = false
                            }

                            "sales_garment_type" -> {
                                navigateTo("sales_garment_type")
                                isDrawerOpen = false
                            }

                            "sales_pricing_quotation", "sales_pricing_and_quotations",
                            "sales_pricing_&_quotations" -> {
                                isSalesSettingsMode = false
                                navigateTo("sales_pricing_quotation")
                                isDrawerOpen = false
                            }
                            "finance_journal_entry", "finance_journal_entries" -> {
                                isSalesSettingsMode = false
                                navigateTo("finance_journal_screen")
                                isDrawerOpen = false
                            }

                            "finance_chart_of_accounts" -> {
                                isSalesSettingsMode = false
                                navigateTo("finance_chart_of_accounts")
                                isDrawerOpen = false
                            }

                            "finance_sales_invoices", "finance_accounts_receivable" -> {
                                isSalesSettingsMode = false
                                navigateTo("finance_sales_invoices")
                                isDrawerOpen = false
                            }

                            "home" -> {
                                isSalesSettingsMode = false
                                if (showHomePanel) navigateTo("settings") else resetToHome()
                                isDrawerOpen = false
                            }

                            "settings" -> {
                                navigateTo("settings")
                                isDrawerOpen = false
                            }
                            "finance_trial_balance" -> {
                                isSalesSettingsMode = false
                                navigateTo("finance_trial_balance")
                                isDrawerOpen = false
                            }

                            "inventory_items", "inventory_all_items" -> {
                                isSalesSettingsMode = false
                                navigateTo("inventory_items")
                                isDrawerOpen = false
                            }

                            "inventory_item_groups" -> {
                                isSalesSettingsMode = false
                                navigateTo("inventory_item_groups")
                                isDrawerOpen = false
                            }
                            "inventory_orders", "inventory_procurement_orders" -> {
                                isSalesSettingsMode = false
                                navigateTo("inventory_low_stock_alerts")
                                isDrawerOpen = false
                            }
                            "hr_all_employees" -> {
                                isSalesSettingsMode = false
                                navigateTo("hr_all_employees")
                                isDrawerOpen = false
                            }
                            "logistics_delivery" -> {
                                isSalesSettingsMode = false
                                navigateTo("logistics_delivery")
                                isDrawerOpen = false
                            }
                            "reports_sales_reports", "reports_finance_reports" -> {
                                isSalesSettingsMode = false
                                navigateTo("reports_sales_reports")
                                isDrawerOpen = false
                            }
                            "reports" -> {
                                isSalesSettingsMode = false
                                navigateTo("reports_sales_reports")
                                isDrawerOpen = false
                            }
                            else -> {
                                Log.d("NAV_DEBUG", "Unhandled route: $route")
                                try {
                                    navController.navigate(route)
                                } catch (_: Exception) {
                                    if (route.startsWith("sales_")) {
                                        navigateTo(route)
                                    }
                                }
                                isDrawerOpen = false
                            }
                        }
                    },
                    onModulesClick = {
                        modulesPanelInitialExpanded = menuForScreen(currentScreen)
                        showModulesPanel = true
                    },
                    onLogout = {
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    showHomePanel = showHomePanel,
                    showSalesPanel = showSalesPanel,
                    isSalesSettingsMode = isSalesSettingsMode
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(innerPadding)
            ) {
                when (currentScreen) {
                    "settings" -> SettingsScreen(
                        navController = navController,
                        onMenuClick = { isDrawerOpen = true },
                        onBack = { goBack() }
                    )
                    "home_organization_profile" -> SettingsScreen(
                        navController = navController,
                        onMenuClick = { isDrawerOpen = true }
                    )
                    "home_branch_management" -> BranchSettingsScreen(
                        navController = navController,
                        onMenuClick = { isDrawerOpen = true },
                        onBack = { goBack() }
                    )
                    "home_department_teams" -> DepartmentSettingsScreen(
                        navController = navController,
                        onMenuClick = { isDrawerOpen = true },
                        onBack = { goBack() }
                    )
                    "home_designation" -> DesignationScreen(
                        navController = navController,
                        onMenuClick = { isDrawerOpen = true },
                        onBack = { goBack() }
                    )

                    "sales_settings" -> SalesSettingsScreen(
                        navController = navController,
                        onClose = {
                            isSalesSettingsMode = false
                            goBack()
                        },
                        onMenuClick = { isDrawerOpen = true }
                    )
                    "sales_garment_type" -> GarmentTypeContent(
                        onClose = {
                            isSalesSettingsMode = true
                            goBack()
                        },
                        onMenuClick = { isDrawerOpen = true }
                    )
                    "home" -> HomeScreenContent(
                        navController = navController,
                        onNavigate = { route ->
                            when (route) {
                                "sales_lead" -> {
                                    isSalesSettingsMode = false
                                    navigateTo("sales_lead")
                                }
                                "sales_customers" -> {
                                    isSalesSettingsMode = false
                                    navigateTo("sales_customers")
                                }
                                "sales_sales_orders" -> {
                                    isSalesSettingsMode = false
                                    navigateTo("sales_sales_orders")
                                }
                                "sales_measurements" -> {
                                    isSalesSettingsMode = false
                                    navigateTo("sales_measurements")
                                }
                                "sales_pricing_quotation",
                                "sales_pricing_and_quotations",
                                "sales_pricing_&_quotations" -> {
                                    isSalesSettingsMode = false
                                    navigateTo("sales_pricing_quotation")
                                }
                                "services_customer_feedback", "customer_feedback" -> {
                                    isSalesSettingsMode = false
                                    navigateTo("services_customer_feedback")
                                    isDrawerOpen = false
                                }
                                else -> {
                                    Log.d("NAV_DEBUG", "Unhandled home navigation: $route")
                                }
                            }
                        }
                    )
                    "sales_lead" -> LeadScreenContent(
                        onCreateLead = { navigateTo("create_lead") },
                        onViewLead = { navigateTo("view_lead") },
                        onEditLead = { navigateTo("edit_lead") },
                        onClose = {
                            isSalesSettingsMode = false
                            goBack()
                        },
                        onBreadCrumbClick = {
                            modulesPanelInitialExpanded = "Sales"
                            showModulesPanel = true
                        }
                    )
                    "create_lead" -> CreateLeadScreen(
                        onBack = { goBack() }
                    )
                    "view_lead" -> ViewLeadScreen(
                        onBack = { goBack() },
                        onEditLead = { navigateTo("edit_lead") }
                    )
                    "edit_lead" -> EditLeadScreen(
                        onBack = { goBack() }
                    )
                    "create_order" -> {
                        CreateOrderScreen(
                            initialData = pendingOrderReviewData,
                            onBack = {
                                pendingOrderReviewData = null      // 🔑 CRITICAL — X/close um clear pannanum, cancel mattum pothaathu
                                goBack()
                            },
                            onCancel = {
                                pendingOrderReviewData = null
                                goBack()
                            },
                            onNextStep = { orderReviewData ->
                                pendingOrderReviewData = orderReviewData
                                navigateTo("create_order_review")
                            }
                        )
                    }
                    "sales_sales_orders" -> SalesOrderScreen(
                        navController = navController,
                        onMenuClick = { isDrawerOpen = true },
                        onBack = { goBack() },
                        onCreateOrder = {
                            pendingOrderReviewData = null      // 🔑 CRITICAL — kill stale edit data
                            navigateTo("create_order")
                        },
                        onViewOrder = { orderId ->
                            selectedOrderId = orderId
                            navigateTo("order_overview")
                        },
                        onEditOrder = { orderId ->
                            editOrderId = orderId
                        },
                        onBreadCrumbClick = {
                            modulesPanelInitialExpanded = "Sales"
                            showModulesPanel = true
                        }
                    )
                    "order_overview" -> {
                        selectedOrderId?.let { id ->
                            OrderOverviewScreen(
                                orderId = id,
                                onClose = {
                                    selectedOrderId = null
                                    goBack()
                                },
                                onEditOrder = { reviewData ->
                                    pendingOrderReviewData = reviewData
                                    selectedOrderId = null
                                    navigateTo("create_order")
                                },
                                onCreateNew = {
                                    pendingOrderReviewData = null
                                    selectedOrderId = null
                                    navigateTo("create_order")
                                }
                            )
                        } ?: run { goBack() }
                    }
                    "sales_orders" -> OrderManagementScreen(
                        navController = navController,
                        onMenuClick = { isDrawerOpen = true },
                        onBack = { goBack() },
                        onViewOrder = { orderId ->
                            selectedManagementOrderId = orderId
                            navigateTo("order_management_overview")
                        },
                        onBreadCrumbClick = {
                            modulesPanelInitialExpanded = "Sales"
                            showModulesPanel = true
                        }
                    )
                    "order_management_overview" -> {
                        selectedManagementOrderId?.let { id ->
                            OrderDetailScreen(
                                orderId = id,
                                onClose = {
                                    selectedManagementOrderId = null
                                    goBack()
                                },
                                onEditOrder = {
                                    editOrderId = id
                                }
                            )
                        } ?: run { goBack() }
                    }
                    "finance_trial_balance" -> TrialBalanceScreen(
                        onClose = { goBack() },
                        onAccountClick = { accountId, accountName ->
                            selectedLedgerAccountId = accountId
                            selectedLedgerAccountName = accountName
                            navigateTo("finance_ledger")
                        },
                        onBreadcrumbClick = {
                            modulesPanelInitialExpanded = "Finance"
                            showModulesPanel = true
                        }
                    )

                    "finance_ledger" -> {
                        selectedLedgerAccountId?.let { id ->
                            LedgerScreen(
                                accountId = id,
                                accountName = selectedLedgerAccountName,
                                onClose = {
                                    selectedLedgerAccountId = null
                                    goBack()
                                },
                                onBreadcrumbClick = {
                                    modulesPanelInitialExpanded = "Finance"
                                    showModulesPanel = true
                                }
                            )
                        } ?: run { goBack() }
                    }

                    "finance_chart_of_accounts" -> ChartOfAccountScreen(
                        onClose = { goBack() },
                        onBreadcrumbClick = {
                            modulesPanelInitialExpanded = "Finance"
                            showModulesPanel = true
                        }
                    )
                    "finance_suppliers" -> AllSuppliersScreen(
                        onClose = { goBack() },
                        onBreadcrumbClick = {
                            modulesPanelInitialExpanded = "Finance"
                            showModulesPanel = true
                        },
                        onSupplierClick = { supplier ->
                            selectedSupplier = supplier
                            navigateTo("finance_supplier_detail")
                        }
                    )
                    "finance_supplier_detail" -> {
                        selectedSupplier?.let { supplier ->
                            SupplierDetailScreen(
                                supplier = supplier,
                                onClose = {
                                    selectedSupplier = null
                                    goBack()
                                },
                                onBreadcrumbClick = {
                                    modulesPanelInitialExpanded = "Finance"
                                    showModulesPanel = true
                                }
                            )
                        } ?: run { goBack() }
                    }
                    "inventory_items" -> InventoryScreen(
                        onClose = { goBack() },
                        onAddItem = { navigateTo("inventory_create_item") },
                        onViewItem = { item ->
                            selectedInventoryItemId = item._id
                            navigateTo("inventory_item_detail")
                        },
                        onEditItem = { navigateTo("inventory_create_item") },
                        onBreadCrumbClick = {
                            modulesPanelInitialExpanded = "Inventory"
                            showModulesPanel = true
                        }
                    )

                    // ADD right after the "inventory_item_detail" block ends

                    "inventory_low_stock_alerts" -> LowStockAlertsScreen(
                        onClose = { goBack() },
                        onReorderClick = { item ->
                            selectedLowStockItem = item
                            navigateTo("inventory_create_purchase_order")
                        },
                        onCreateNewItem = { navigateTo("inventory_create_purchase_order") },
                        onBreadcrumbClick = {
                            modulesPanelInitialExpanded = "Inventory"
                            showModulesPanel = true
                        }
                    )

                    "inventory_create_purchase_order" -> {
                        CreatePurchaseOrderScreen(
                            onClose = {
                                selectedLowStockItem = null
                                goBack()
                            },
                            onCancel = {
                                selectedLowStockItem = null
                                goBack()
                            },
                            onCreateOrder = {
                                // TODO: wire to actual PO creation API via a ViewModel
                                selectedLowStockItem = null
                                goBack()
                                goBack()   // pops both create_purchase_order and low_stock_alerts → lands on inventory_items
                            }
                        )
                    }

                    "hr_all_employees" -> AllEmployeesScreen(
                        onDismiss = { goBack() },
                        onAddEmployee = {
                            employeeScreenMode = com.cuso.mobile.view.home.hr.ScreenMode.CREATE
                            selectedEmployeeId = null
                            navigateTo("hr_employee_onboarding")
                        },
                        onView = { employee ->
                            employeeScreenMode = com.cuso.mobile.view.home.hr.ScreenMode.VIEW
                            selectedEmployeeId = employee._id
                            navigateTo("hr_employee_onboarding")
                        },
                        onEdit = { employee ->
                            employeeScreenMode = com.cuso.mobile.view.home.hr.ScreenMode.EDIT
                            selectedEmployeeId = employee._id
                            navigateTo("hr_employee_onboarding")
                        },
                        onDelete = { employee ->
                            // TODO: call delete API via a HR/Employee ViewModel
                        },
                        hrViewModel = hrViewModel,
                        onBreadCrumbClick = {
                            modulesPanelInitialExpanded = "HR"
                            showModulesPanel = true
                        }
                    )
                    "logistics_order_tracking" -> OrderTrackingScreen(
                        onClose = { goBack() },
                        onViewOrder = { order ->
                            selectedOrderId = order.id
                            navigateTo("tracking_overview")
                        },
                        onBreadCrumbClick = {
                            modulesPanelInitialExpanded = "Logistics"
                            showModulesPanel = true
                        }
                    )
                    "tracking_overview" -> TrackingOverviewScreen(
                        onClose = { goBack() }
                    )

                    "hr_employee_onboarding" -> EmployeeOnboardingScreen(
                        mode = employeeScreenMode,
                        memberIdToLoad = selectedEmployeeId,
                        onDismiss = {
                            selectedEmployeeId = null
                            goBack()
                        },
                        onCreateEmployee = {
                            selectedEmployeeId = null
                            goBack()
                        },
                        onUpdateEmployee = {
                            selectedEmployeeId = null
                            goBack()
                        },
                        hrViewModel = hrViewModel
                    )

                    "inventory_create_item" -> CreateItemScreen(
                        onDismiss = { goBack() },
                        onItemCreated = { goBack() }
                    )

                    "inventory_item_detail" -> {
                        selectedInventoryItemId?.let { id ->
                            val itemDetailViewModel: com.cuso.mobile.viewmodel.InventoryViewModel = hiltViewModel()
                            val selectedItem by itemDetailViewModel.selectedItem.collectAsStateWithLifecycle()
                            val isLoadingDetail by itemDetailViewModel.isLoadingItemDetail.collectAsStateWithLifecycle()
                            val detailError by itemDetailViewModel.itemDetailError.collectAsStateWithLifecycle()

                            LaunchedEffect(id) {
                                itemDetailViewModel.fetchInventoryItemDetail(id)
                            }

                            InventoryViewOne(
                                item = selectedItem,
                                isLoading = isLoadingDetail,
                                errorMessage = detailError,
                                onDismiss = {
                                    selectedInventoryItemId = null
                                    goBack()
                                },
                                onAdjustStock = { },
                                onAdjustStockSubmit = { type, quantity, reason, notes ->
                                    val apiType = when (type) {
                                        AdjustmentType.INCREASE -> "increase"
                                        AdjustmentType.DECREASE -> "decrease"
                                        AdjustmentType.SET_EXACT -> "set"
                                    }
                                    itemDetailViewModel.adjustStock(
                                        itemId = id,
                                        adjustmentType = apiType,
                                        quantity = quantity,
                                        reason = reason,
                                        notes = notes
                                    )
                                },
                                onWarehouseTransfer = { },
                                onReorderStock = { },
                                onMarkInactive = { },
                                onEdit = { },
                                onShare = { }
                            )
                        } ?: run { goBack() }
                    }

                    "services_customer_feedback" -> CustomerFeedbackScreen(
                        onDismiss = { goBack() },
                        onView = { feedbackId ->
                            selectedFeedbackId = feedbackId
                            navigateTo("feedback_detail")
                        },
                        onEdit = { feedbackId -> },
                        onDelete = { feedbackId -> },
                        onBreadCrumbClick = {
                            modulesPanelInitialExpanded = "Services"
                            showModulesPanel = true
                        }
                    )
                    "services_alteration_management" -> AlterationManagementScreen(
                        onClose = {goBack()},
                        onCreateNewAlteration = {navigateTo("create_alteration")},
                        onBreadcrumbClick = {
                            modulesPanelInitialExpanded = "Services"
                            showModulesPanel=true
                        },
                        onViewClick = {}
                    )

                    "create_alteration" -> CreateAlterationManagementScreen (
                        onClose = {goBack()},
                    )

                    "services_service_request" -> ServiceRequestScreen(
                        onClose = {},
                        onBreadcrumbClick = {},
                        onCreateNewRequest = {navigateTo("create_request")},
                        onViewClick = {navigateTo("review_services")}
                    )
                    "create_request" -> CreateServiceRequest()

                    "review_services" -> ServiceOrderDetailsScreen(
                        service = ServiceDetails(
                            serviceRef = "SR-1045",
                            reviewStatus = "Pending Review",
                            service = "Bespoke Alteration",
                            requestDate = "Oct 24, 2025",
                            priority = "High",
                            serviceCategory = "Suit Fitting & Adjustments",
                            preferredCompletionDate = "Nov 15, 2023",
                            serviceType = "Internal Production Refit",
                            customerName = "Jonathan Sterling",
                            phoneNumber = "+1 (555) 123-4567",
                            emailAddress = "j.sterling@executive.com",
                            shippingAddress = "452 Premium Way, Floor 12\nManhattan, NY 10001"
                        ),
                        order = OrderDetails(
                            orderId = "#ORD-8829-23",
                            status = "Completed",
                            garmentItem = "Custom Charcoal 3-Piece Wool Suit",
                            orderDate = "Sep 12, 2023",
                            deliveryDate = "Oct 15, 2023",
                            issueDescription = "The sleeves are approximately 2 inches too long...",
                            internalNotes = "Check fabric elasticity before cutting...",
                            attachmentCount = 3
                        ),
                        onBack = { goBack() },
                        onViewFullOrderHistory = { navigateTo("order_history") }
                    )

                    "feedback_detail" -> FeedbackDetailScreen(
                        onDismiss = {
                            selectedFeedbackId = null
                            goBack()
                        }
                    )
                    "sales_pricing_quotation" -> QuotationScreen(
                        onClose = { isSalesSettingsMode = false; goBack() },
                        onAddNe = {
                            editingPricingId = null
                            quotationScreenMode = "create"
                            navigateTo("create_quotation")
                        },
                        onView = { id ->
                            editingPricingId = id
                            quotationScreenMode = "view"
                            navigateTo("create_quotation")
                        },
                        onEdit = { id ->
                            editingPricingId = id
                            quotationScreenMode = "edit"
                            navigateTo("create_quotation")
                        },
                        onBreadCrumbClick = {
                            modulesPanelInitialExpanded = "Sales"
                            showModulesPanel = true
                        }
                    )

                    "create_quotation" -> CreateQuotationScreen(
                        quotationId = editingPricingId,
                        mode = quotationScreenMode,
                        onClose = { goBack() },
                        onSave = { goBack() },
                        token = token
                    )
                    "inventory_item_groups" -> AllItemGroupScreen(
                        onDismiss = { goBack() },
                        onAddItemGroup = { navigateTo("inventory_create_item_group") },
                        onView = { groupId ->
                            selectedItemGroupId = groupId
                        },
                        onEdit = { groupId ->
                            selectedItemGroupId = groupId
                            navigateTo("inventory_create_item_group")
                        },
                        onDelete = { groupId -> },
                        onBreadCrumbClick = {
                            modulesPanelInitialExpanded = "Inventory"
                            showModulesPanel = true
                        }
                    )

                    "inventory_create_item_group" -> CreateItemGroupScreen(
                        onDismiss = {
                            selectedItemGroupId = null
                            goBack()
                        },
                        onSave = {
                            selectedItemGroupId = null
                            goBack()
                        }
                    )
                    "sales_pricing_overview" -> PricingScreen(
                        onClose = { isSalesSettingsMode = false; goBack() },
                        onAddNewPricing = { editingPricingId = null; navigateTo("create_garment_pricing") },
                        onCardClick = { pricingId -> editingPricingId = pricingId; navigateTo("create_garment_pricing") },
                        onBreadCrumbClick = {
                            modulesPanelInitialExpanded = "Sales"
                            showModulesPanel = true
                        }
                    )

                    "garment_pricing_list" -> com.cuso.mobile.view.home.sales.pricing.GarmentPricingListScreen(
                        onBack = { goBack() },
                        onAddNewPricing = { editingPricingId = null; navigateTo("create_garment_pricing") },
                        onCardClick = { pricingId -> editingPricingId = pricingId; navigateTo("create_garment_pricing") }
                    )

                    "create_garment_pricing" -> com.cuso.mobile.view.home.sales.pricing.AddGarmentPricingScreen(
                        pricingId = editingPricingId,
                        onClose = {
                            editingPricingId = null
                            goBack()
                        },
                        onSave = {
                            editingPricingId = null
                            goBack()
                        }
                    )
                    "sales_customers" -> CustomerScreen(
                        navController = navController,
                        customerState = customerUiState,
                        onSearch = customerViewModel::onSearch,                          // ✅ ADD
                        onTypeFilterChange = customerViewModel::onTypeFilterChange,      // ✅ ADD
                        onPageChange = customerViewModel::onPageChange,                  // ✅ ADD — idhu illama Next/Prev API hit aagathu
                        onItemsPerPageChange = customerViewModel::onItemsPerPageChange,  // ✅ ADD — idhu illama 20/50 dropdown API hit aagathu

                        onClose = { goBack() },
                        onCreateCustomer = { navigateTo("create_customer") },
                        onView = { customer ->
                            selectedCustomer = customer
                            navigateTo("view_customer")
                        },
                        onEdit = { customer ->
                            selectedCustomer = customer
                            navigateTo("edit_customer")
                        },
                        onDelete = { customer -> customerViewModel.deleteCustomer(customer.id) },
                        onBreadCrumbClick = {
                            modulesPanelInitialExpanded = "Sales"
                            showModulesPanel = true
                        }
                    )
                    "finance_customers" -> FinanceCustomerScreen(
                        onClose = { goBack() },
                        onCustomerEdit = {},
                        onCustomerClick = {},
                        onBreadCrumbClick = {
                            modulesPanelInitialExpanded = "Finance"
                            showModulesPanel = true
                        }
                    )
                    "finance_expenses" -> ExpensesScreen(
                        onClose = { goBack() },
                        onBreadCrumbClick = {
                            modulesPanelInitialExpanded = "Finance"
                            showModulesPanel = true
                        }
                    )
                    "finance_journal_screen" -> ManualJournalEntryScreen(
                        onClose = { goBack() }
                    )
                    "finance_payments_received" -> AllPaymentScreen(
                        onViewPayment = { navigateTo("payment_detail_screen") },
                        onBreadCrumbClick = {
                            modulesPanelInitialExpanded = "Finance"
                            showModulesPanel = true
                        }
                    )
                    "payment_detail_screen" -> PaymentDetailScreen(
                        onClose = { goBack() }
                    )

                    "finance_sales_invoices" -> FinanceInvoiceScreen(
                        onClose = { goBack() },
                        onInvoiceClick = { invoice ->
                            selectedInvoiceId = invoice.id
                            navigateTo("finance_invoice_detail")
                        },
                        onBreadCrumbClick = {
                            modulesPanelInitialExpanded = "Finance"
                            showModulesPanel = true
                        }
                    )

                    "finance_invoice_detail" -> {
                        selectedInvoiceId?.let { id ->
                            com.cuso.mobile.view.home.finance.InvoiceDetailScreen(
                                invoiceId = id,
                                onClose = {
                                    selectedInvoiceId = null
                                    goBack()
                                },
                                token = token
                            )
                        } ?: run { goBack() }
                    }
                    "logistics_delivery" -> DeliveryManagementScreen(
                        onDismiss = { goBack() },
                        onView = { navigateTo("delivery_detail") },
                        onBreadCrumbClick = {
                            modulesPanelInitialExpanded = "Logistics"
                            showModulesPanel = true
                        }
                    )
                    "delivery_detail" -> DeliveryDetailScreen(
                        onDismiss = { goBack() }
                    )

                    "view_customer", "edit_customer" -> {
                        val customer = selectedCustomer
                        if (customer != null && customer.id.isNotBlank()) {
                            CustomerDetailScreen(
                                navController = navController,
                                customerId = customer.id,
                                startInEditMode = currentScreen == "edit_customer",
                                onClose = {
                                    selectedCustomer = null
                                    goBack()
                                },
                                onUpdateSuccess = {
                                    customerViewModel.refresh()
                                    selectedCustomer = null
                                    goBack()
                                },
                                onRequestEdit = { navigateTo("edit_customer") }
                            )
                        } else {
                            LaunchedEffect(Unit) {
                                Toast.makeText(context, "Unable to load customer details", Toast.LENGTH_SHORT).show()
                            }
                            goBack()
                        }
                    }

                    "sales_measurements" -> MeasurementsScreen(
                        navController = navController,
                        onBack = { goBack() },
                        onCreateOrder = { navigateTo("create_order") },
                        onBreadCrumbClick = {
                            modulesPanelInitialExpanded = "Sales"
                            showModulesPanel = true
                        }
                    )
                    "create_order_review" -> {
                        pendingOrderReviewData?.let { data ->
                            CreateOrderNextStep(
                                orderData = data,
                                onBack = { goBack() },
                                onSaveOrder = {
                                    pendingOrderReviewData = null
                                    goBack()
                                    goBack()   // pops both create_order_review and create_order → lands on sales_sales_orders
                                }
                            )
                        } ?: run { goBack() }
                    }
                    "profile-settings" -> ProfileSettingsScreen(
                        onClose = { goBack() },
                        onOrganizationSetup = { navigateTo("home_organization_profile") },
                        onBranchManagement = { navigateTo("home_branch_management") },
                        onDepartment = { navigateTo("home_department_teams") },
                        onDesignation = { navigateTo("home_designation") },
                        onLogout = {
                            settingsViewModel.logout {
                                authViewModel.logout {
                                    navController.navigate("login") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            }
                        }
                    )

                    "reports_sales_reports" -> SalesOrderReportsScreen(
                        onClose = { goBack() },
                        onBreadCrumbClick = {
                            modulesPanelInitialExpanded = "Reports"
                            showModulesPanel = true
                        }
                    )
                    else -> { }
                }
            }
        }

        // ✅ NEW — Modules bottom-sheet panel, overlays Scaffold+bottom nav, slides up from bottom
        // ✅ NEW — Modules bottom-sheet panel, overlays Scaffold+bottom nav, slides up from bottom
        ModulesPanel(
            isOpen = showModulesPanel,
            onClose = { showModulesPanel = false },
            initialExpandedModule = modulesPanelInitialExpanded,   // ✅ NEW
            onModuleCategoryClick = { menu, category ->
                val menuItem = SidebarConfig.getFullMenuItems().find { it.label == menu }
                val firstSubItem = menuItem?.subItems?.get(category)?.firstOrNull()

                val rawNavKey = if (firstSubItem != null) {
                    buildNavigationKey(menu, firstSubItem)
                } else {
                    buildNavigationKey(menu, category)
                }
                val navKey = normalizeRoute(rawNavKey)

                // AFTER
                val implementedRoutes = setOf(
                    "sales_lead", "sales_customers", "sales_measurements", "sales_sales_orders",
                    "sales_orders", "sales_pricing_overview", "sales_pricing_quotation",
                    "finance_sales_invoices", "finance_customers", "finance_payments_received",
                    "finance_suppliers", "finance_expenses", "finance_chart_of_accounts",
                    "finance_journal_screen", "finance_trial_balance",
                    "inventory_items", "inventory_item_groups",
                    "inventory_low_stock_alerts",
                    "logistics_delivery", "logistics_order_tracking",
                    "services_customer_feedback","services_alteration_management","services_service_request", "hr_all_employees", "reports_sales_reports"
                )

                isSalesSettingsMode = false
                showModulesPanel = false

                if (navKey in implementedRoutes) {
                    navigateTo(navKey)
                } else {
                    Toast.makeText(context, "Coming soon", Toast.LENGTH_SHORT).show()
                }
            }        )
    }
}
// ─────────────────────────────────────────────────────────────
// TopNavBar
// ─────────────────────────────────────────────────────────────

@SuppressLint("ContextCastToActivity")
@Composable
fun TopBar(
    isPanelMode: Boolean = false,
    hasNotification: Boolean = true,
    onSearchClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    // EmployeeOnboardingScreen.kt la:
    val authViewModel: Authenticate = hiltViewModel(
        LocalContext.current as ComponentActivity   // ✅ இதை சேருங்க
    )
    val userEntity by authViewModel.user.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        Log.d("VM_DEBUG", "TopBar authViewModel hashcode: ${authViewModel.hashCode()}")
    }

    val user: User? = userEntity?.let {
        User(
            firstName = it.firstName,
            lastName = it.lastName,
            email = it.email,
            profilePicture = it.profilePicture.orEmpty(),
            organizationId = Organization(
                _id = it.organizationId,
                businessId = "",
                name = "",
                industry = "",
                orgType = "",
                organizationPicture = null,
                organizationPictureId = null,
                domains = emptyList(),
                email = "",
                mobile = "",
                orgSetupComplete = false,
                totalMembers = 0,
                activeMembers = 0,
                segments = emptyList(),
                branches = emptyList(),
                isTaxId = false,
                status = "",
                createdAt = "",
                updatedAt = "",
                slug = "",
                __v = 0,
                defaultBranch = "",
                ownerId = "",
                ownerMemberId = "",
                businessType = "",
                taxId = "",
                isInternalOrganization = false,
                subscription = Subscription(
                    memberLimit = 0,
                    featuresEnabled = emptyList()
                ),
                settings = Settings(
                    country = "",
                    state = "",
                    portalName = "",
                    termsAccepted = false,
                    marketingEmails = false,
                    workingDays = emptyList(),
                    timezone = "",
                    currency = "",
                    language = "",
                    address = "",
                    city = "",
                    pincode = ""
                )
            ),
            role = it.role
        )
    }
    LaunchedEffect(userEntity) {
        Log.d("TOPBAR", "user=${userEntity?.firstName}")
        Log.d("TOPBAR", "profile=${userEntity?.profilePicture}")
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 1.dp,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 0.dp),   // ✅ vertical padding 0
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // ── Left: Logo ──
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.height(56.dp)   // ✅ fixed height so logo doesn't push row down
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.cuso_tailor_logo),
                    contentDescription = "Logo",
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .size(90.dp)   // ✅ slightly smaller so it fits within the fixed height
                )
            }

            // ── Right: Search + Notification + Profile ──
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color(0xFF4B5563),
                    modifier = Modifier
                        .size(24.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { onSearchClick() }
                )

                Spacer(modifier = Modifier.width(18.dp))

                Box {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = Color(0xFF4B5563),
                        modifier = Modifier
                            .size(24.dp)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { onNotificationClick() }
                    )
                    if (hasNotification) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .align(Alignment.TopEnd)
                                .offset(x = 2.dp, y = (-1).dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEF4444))
                                .border(1.5.dp, Color(0xFFF5F5FA), CircleShape)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(18.dp))

                // ── Profile picture / initials ──
                val profilePicture = user?.profilePicture
                val avatarSize = if (isPanelMode) 38.dp else 42.dp

                if (!profilePicture.isNullOrBlank()) {
                    // TopBar.kt - AsyncImage உள்ள இடத்தில்
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(profilePicture)
                            .crossfade(true)
                            // 👇 இந்த இரண்டு வரிகள் மிக முக்கியம்
                            .memoryCachePolicy(CachePolicy.DISABLED)
                            .diskCachePolicy(CachePolicy.DISABLED)
                            .build(),
                        contentDescription = "Profile picture",
                        modifier = Modifier
                            .size(avatarSize)
                            .clip(CircleShape)
                            .clickable { onProfileClick() },
                        contentScale = ContentScale.Crop
                    )
                } else {
                    val initials = buildString {
                        user?.firstName?.firstOrNull()?.let { append(it.uppercaseChar()) }
                        user?.lastName?.firstOrNull()?.let { append(it.uppercaseChar()) }
                    }
                    Box(
                        modifier = Modifier
                            .size(avatarSize)
                            .clip(CircleShape)
                            .background(Color(0xFF3B3BF9))
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { onProfileClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
@Suppress("UNUSED_PARAMETER")

@Composable
fun BottomBar(
    navController: NavController,
    isSettingsOpen: Boolean = false,
    currentScreen: String = "home",
    isDrawerOpen: Boolean = false,
    onDrawerToggle: () -> Unit = {},
    onDrawerClose: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onMenuItemClick: (String) -> Unit = {},
    onModulesClick: () -> Unit = {},   // ✅ NEW
    onLogout: () -> Unit,
    showHomePanel: Boolean = false,
    showSalesPanel: Boolean = false,
    isSalesSettingsMode: Boolean = false
) {
    val authViewModel: Authenticate = hiltViewModel()
    val userEntity by authViewModel.user.collectAsStateWithLifecycle()

    val user: User? = userEntity?.let {
        User(
            firstName = it.firstName,
            lastName = it.lastName,
            email = it.email,
            profilePicture = it.profilePicture.orEmpty(),
            organizationId = Organization(
                _id = it.organizationId,
                businessId = "",
                name = "",
                industry = "",
                orgType = "",
                organizationPicture = null,
                organizationPictureId = null,
                domains = emptyList(),
                email = "",
                mobile = "",
                orgSetupComplete = false,
                totalMembers = 0,
                activeMembers = 0,
                segments = emptyList(),
                branches = emptyList(),
                isTaxId = false,
                status = "",
                createdAt = "",
                updatedAt = "",
                slug = "",
                __v = 0,
                defaultBranch = "",
                ownerId = "",
                ownerMemberId = "",
                businessType = "",
                taxId = "",
                isInternalOrganization = false,
                subscription = Subscription(
                    memberLimit = 0,
                    featuresEnabled = emptyList()
                ),
                settings = Settings(
                    country = "",
                    state = "",
                    portalName = "",
                    termsAccepted = false,
                    marketingEmails = false,
                    workingDays = emptyList(),
                    timezone = "",
                    currency = "",
                    language = "",
                    address = "",
                    city = "",
                    pincode = ""
                )
            ),
            role = it.role
        )
    }

    Box(modifier = Modifier) {

        if (isSettingsOpen || showSalesPanel) {
            SalesSideBar(
                isOpen = isDrawerOpen,
                onClose = onDrawerClose,
                onMenuItemClick = { route ->
                    onMenuItemClick(route)
                    onDrawerClose()
                },
                onLogout = onLogout,
                user = user,
                defaultSelectedMenu = if (currentScreen == "sales_settings" || currentScreen == "sales_garment_type") "Sales" else "Home"
            )
        } else {
            FullSideBar(
                isOpen = isDrawerOpen,
                onClose = onDrawerClose,
                onMenuItemClick = { route ->
                    onMenuItemClick(route)
                    onDrawerClose()
                },
                onLogout = onLogout,
                user = user,
                defaultSelectedMenu = if (showHomePanel) "Home" else "Home"
            )
        }

        // ── BOTTOM NAV BAR ──
        Box(
            modifier = Modifier
                    .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()   // ✅ NEW — keeps rounded corners clear of the system nav bar
                .background(Color.Transparent)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 12.dp,
                tonalElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    BottomNavItem(
                        icon = R.drawable.home,
                        label = "Home",
                        isSelected = currentScreen == "home",
                        selectedColor = Color(0xFF6C4FF6),
                        onClick = { onMenuItemClick("home") }
                    )

                    BottomNavItem(
                        icon = R.drawable.orders,
                        label = "Orders",
                        isSelected = currentScreen == "sales_sales_orders",
                        selectedColor = Color(0xFF6C4FF6),
                        onClick = { onMenuItemClick("sales_sales_orders") }
                    )

                    Spacer(modifier = Modifier.width(64.dp))

                    BottomNavItem(
                        icon = R.drawable.reports,
                        label = "Reports",
                        isSelected = currentScreen == "reports",
                        selectedColor = Color(0xFF6C4FF6),
                        onClick = { onMenuItemClick("reports") }
                    )

                    BottomNavItem(
                        icon = R.drawable.modules,
                        label = "Modules",
                        isSelected = currentScreen == "modules",
                        selectedColor = Color(0xFF6C4FF6),
                        onClick = { onModulesClick() }   // ✅ CHANGED — was onMenuItemClick("modules")
                    )
                }
            }

            // Floating center "+" button
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-24).dp)
                    .size(72.dp)
                    .shadow(10.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color(0xFF6C4FF6)),
//                    .clickable { onDrawerToggle() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}

@Composable
fun BottomNavItem(
    icon: Int,                          // ✅ CHANGED — drawable resource id, not ImageVector
    label: String,
    isSelected: Boolean,
    selectedColor: Color = Color(0xFF6C4FF6),
    onClick: () -> Unit
) {
    val color = if (isSelected) selectedColor else Color(0xFF9CA3AF)
    Column(
        modifier = Modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = icon),
            contentDescription = label,
            colorFilter = ColorFilter.tint(color),   // ✅ tints the PNG like an Icon would
            modifier = Modifier.size(30.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = color,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

// ─────────────────────────────────────────────────────────────
// UI-side data models (built FROM api)
// ─────────────────────────────────────────────────────────────
private data class DashboardStat(
    val label: String,
    val value: String,
    val icon: Int,
    val iconBg: Color,
    val iconTint: Color,
    val trendText: String,
    val trendUp: Boolean?,
    val isHighlighted: Boolean = false

)

private data class QuickModule(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

private data class ActivityItem(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val iconBg: Color,
    val iconTint: Color,
    val title: String,
    val timeAgo: String,
    val amount: String? = null
)

private data class RecentCustomer(
    val name: String,
    val role: String,
    val initials: String,
    val avatarColor: Color
)





private fun mapActiveOrdersToActivity(orders: List<ActiveOrderItem>): List<ActivityItem> {
    return orders.take(4).map { order ->
        val statusLabel = order.status.replaceFirstChar { it.uppercase() }
        ActivityItem(
            icon = Icons.Default.ShoppingCart,
            iconBg = Color(0xFFDCFCE7),
            iconTint = Color(0xFF16A34A),
            title = "${order.customer} — Order ${order.orderNumber} ($statusLabel)",
            timeAgo = "",
            amount = if (order.amount > 0) "₹${formatIndianNumber(order.amount.toInt())}" else null
        )
    }
}

private val CustomerAvatarPalette = listOf(
    Color(0xFFF59E0B), Color(0xFF3B82F6), Color(0xFFEC4899), Color(0xFF10B981), Color(0xFF6366F1)
)

private fun mapOperationsToCustomers(ops: List<OperationItem>): List<RecentCustomer> {
    return ops.distinctBy { it.customer }.take(5).mapIndexed { index, op ->
        val nameParts = op.customer.trim().split(" ").filter { it.isNotBlank() }
        val initials = nameParts.take(2).joinToString("") { it.first().uppercase() }.ifBlank { "?" }
        val prettyName = nameParts.joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { it.uppercase() }
        }
        RecentCustomer(
            name = prettyName.ifBlank { op.customer },
            role = op.type,
            initials = initials,
            avatarColor = CustomerAvatarPalette[index % CustomerAvatarPalette.size]
        )
    }
}

// ─────────────────────────────────────────────────────────────
// HomeScreenContent — fetches real dashboard data + fixed design
// ─────────────────────────────────────────────────────────────
// ─────────────────────────────────────────────────────────────
// HomeScreenContent — fetches real dashboard data + fixed design
// ─────────────────────────────────────────────────────────────
@Composable
fun HomeScreenContent(
    navController: NavHostController,
    onNavigate: (String) -> Unit = {}
) {
    // ✅ Locks fontScale to 1f so the layout looks identical across every
    // device regardless of the system "font size" accessibility setting.
    val baseDensity = LocalDensity.current
    CompositionLocalProvider(
        LocalDensity provides Density(density = baseDensity.density, fontScale = 1f)
    ) {
        HomeScreenContentBody(navController = navController, onNavigate = onNavigate)
    }
}
@Suppress("UNUSED_PARAMETER")

@Composable
private fun HomeScreenContentBody(
    navController: NavHostController,
    onNavigate: (String) -> Unit
) {
    val dashboardViewModel: DashboardViewModel = hiltViewModel()
    val uiState by dashboardViewModel.uiState.collectAsStateWithLifecycle()

    val authViewModel: Authenticate = hiltViewModel()
    val userEntity by authViewModel.user.collectAsStateWithLifecycle()
    val adminName = userEntity?.firstName?.takeIf { it.isNotBlank() } ?: "Admin"

    // Quick Modules — static shortcuts (not part of this API response)
    val quickModules = remember {
        listOf(
            QuickModule("Contacts", Icons.Default.Person),
            QuickModule("Leads", Icons.AutoMirrored.Filled.TrendingUp),
            QuickModule("Deals", Icons.Default.Sell),
            QuickModule("Tickets", Icons.Default.Description),
            QuickModule("Email", Icons.Default.Email),
            QuickModule("Calendar", Icons.Default.CalendarMonth)
        )
    }

    when (val state = uiState) {
        is DashboardUiState.Loading -> {
            DashboardSkeleton()
        }

        is DashboardUiState.Error -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFfafafb)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red, modifier = Modifier.size(40.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("Failed to load dashboard", color = Color.Red, fontWeight = FontWeight.Bold)
                    Text(state.message, color = Color.Gray, fontSize = 13.sp)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { dashboardViewModel.loadDashboard() }) {
                        Text("Retry")
                    }
                }
            }
        }

        is DashboardUiState.Success -> {
            val data = state.data
            val stats = remember(data) { mapApiStatsToUi(data.stats) }
            val activities = remember(data) { mapActiveOrdersToActivity(data.activeOrders) }
            val customers = remember(data) { mapOperationsToCustomers(data.operations) }
            val newLeadsCount = remember(data) {
                data.leadChart.find { it.name.equals("New Enquiry", ignoreCase = true) }?.count ?: 0
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    GreetingCard(
                        userName = adminName,
                        newLeadsCount = newLeadsCount,
                        onNavigate = onNavigate
                    )
                }
                item { StatsGrid(stats) }
                item {
                    QuickModulesSection(
                        modules = quickModules,
                        onNavigate = onNavigate
                    )
                }
                if (activities.isNotEmpty()) {
                    item {
                        RecentActivitySection(
                            activities = activities,
                            onNavigate = onNavigate
                        )
                    }
                }
                if (customers.isNotEmpty()) {
                    item {
                        RecentCustomersSection(
                            customers = customers,
                            onNavigate = onNavigate
                        )
                    }
                }
                item { Spacer(Modifier.height(8.dp)) }
            }
            //TEST CRASH
//            Button(
//                onClick = {
//                    throw RuntimeException("Test Crash — Crashlytics Verification")
//                }
//            ) {
//                Text("Test Crash")
//            }
        }
        else -> {
            CirculerProgressIndicatorReuse()
        }
    }
}

// ── Greeting card — solid purple banner ──
@Composable
private fun GreetingCard(
    userName: String,
    newLeadsCount: Int,
    onNavigate: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)                       // fixed height — icon size affect pannathu
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF2F27CE))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onNavigate("sales_lead") }
    ) {
        // Watermark trend icon — mostly visible, edge-la konjam matum crop
        Icon(
            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
            contentDescription = null,
            tint = Color(0xFFF8F7FF).copy(alpha = 0.25f),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(90.dp)
                .offset(x = -(1).dp, y = 25.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(20.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Good Morning, $userName",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "You have $newLeadsCount new leads to review today.",
                color = Color(0xFFF8F7FF).copy(alpha = 0.85f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

// ── Stats grid — 2x2 cards, driven by API `stats` array ──
@Composable
private fun StatsGrid(stats: List<DashboardStat>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        stats.chunked(2).forEach { rowStats ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowStats.forEach { stat -> DashboardStatCard(
                    stat = stat, modifier = Modifier.weight(1f)) }
                if (rowStats.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun DashboardStatCard(stat: DashboardStat, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(Color(0xFFf8f9ff), RoundedCornerShape(20.dp))
            .border(1.dp,Color(0xFFe8eaf4),RoundedCornerShape(20.dp))
//            .shadow(elevation = 2.dp)
            .padding(14.dp)
    ) {
        Row(Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start) {
//            Box(
//                modifier = Modifier
//                    .size(34.dp)
//                    .clip(RoundedCornerShape(10.dp))
//                    .background(stat.iconBg),
//                contentAlignment = Alignment.Center
//            ) {
                Image(
                    painter = painterResource(id = stat.icon),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(stat.iconTint),   // ✅ tints the PNG same as Icon would
                    modifier = Modifier.size(18.dp)
                )
//            }
            Spacer(Modifier.width(5.dp))

            Text(
                stat.label,
                fontSize = 13.sp,
                color = statLogoBg,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(Modifier.height(10.dp))
        Spacer(Modifier.height(2.dp))

        Text(stat.value, fontSize = 20.sp, color = if(stat.label=="Revenue"||stat.label=="Pending"){Color(0xFF2F27CE)}else{Color(0xFF0B1C30)}, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        when (stat.trendUp) {
            true -> TrendRow(icon = Icons.Default.ArrowUpward, text = stat.trendText, color = Color(0xFF16A34A))
            false -> TrendRow(icon = Icons.Default.ArrowDownward, text = stat.trendText, color = Color(0xFFEF4444))
            null -> Text(stat.trendText, fontSize = 11.sp, color = Color(0xFF9CA3AF))
        }
    }
}

@Composable
private fun TrendRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(12.dp))
        Spacer(Modifier.width(2.dp))
        Text(text, fontSize = 12.sp, color = color, fontWeight = FontWeight.SemiBold)
    }
}

// ──  — static shortcuts row ──
@Composable
private fun QuickModulesSection(
    modules: List<QuickModule>,
    onNavigate: (String) -> Unit
) {
    Column {
        Text("Quick Modules", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
        Spacer(Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(modules) { module ->
                Column(
                    modifier = Modifier
                        .width(64.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            val route = when (module.label) {
                                "Contacts" -> "sales_customers"
                                "Leads" -> "sales_lead"
                                "Deals" -> "sales_sales_orders"
                                "Tickets" -> null   // no screen mapped yet
                                "Email" -> null     // no screen mapped yet
                                "Calendar" -> null  // no screen mapped yet
                                else -> null
                            }
                            route?.let { onNavigate(it) }
                        },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFDCE9FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(module.icon, contentDescription = module.label, tint = Color(0xFF2F27CE), modifier = Modifier.size(24.dp))
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        module.label,
                        fontSize = 11.sp,
                        color = Color(0xFF6B7280),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

private fun statVisualsFor(title: String): Triple<Int, Color, Color> {
    return when {
        title.contains("Revenue", true) ->
            Triple(R.drawable.revenue, Color(0xFFEDE9FE), statLogoBg)
        title.contains("Order", true) ->
            Triple(R.drawable.cart, Color(0xFFEDE9FE), statLogoBg)
        title.contains("Measurement", true) ->
            Triple(R.drawable.customer, Color(0xFFEDE9FE), statLogoBg)
        title.contains("Pending", true) || title.contains("Payment", true) ->
            Triple(R.drawable.pending, Color(0xFFEDE9FE), statLogoBg)
        else ->
            Triple(R.drawable.cart, Color(0xFFEDE9FE), Color(0xFF7C3AED))
    }
}

// ✅ NEW — short display label for stat cards
private fun shortStatLabel(title: String): String {
    return when {
        title.contains("Revenue", true) -> "Revenue"
        title.contains("Order", true) -> "Orders"
        title.contains("Measurement", true) -> "Customers"
        title.contains("Pending", true) || title.contains("Payment", true) -> "Pending"
        else -> title
    }
}

private fun mapApiStatsToUi(stats: List<DashboardStatDto>): List<DashboardStat> {
    return stats.map { stat ->
        val (icon, iconBg, iconTint) = statVisualsFor(stat.title)
        val trendUp: Boolean? = when (stat.color.lowercase()) {
            "green" -> true
            "red" -> false
            else -> null
        }
        val valueText = if (stat.type == "currency") {
            "₹${formatIndianNumber(stat.value)}"
        } else {
            formatIndianNumber(stat.value.toInt())
        }
        val trendText = "${stat.change.toInt()}%"
        DashboardStat(shortStatLabel(stat.title), valueText, icon, iconBg, iconTint, trendText, trendUp)
    }
}

// ── Recent Activity — driven by API `activeOrders` ──
@Composable
private fun RecentActivitySection(
    activities: List<ActivityItem>,
    onNavigate: (String) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth()
            ,
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Recent Activity", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
            Row(Modifier.fillMaxWidth()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onNavigate("sales_sales_orders") },
                horizontalArrangement = Arrangement.End) {
                Text(
                    "View All",
                    fontSize = 13.sp,
                    color = Color(0xFF7C3AED),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier

                )
                Icon(imageVector = Icons.Default.ChevronRight, "right")
            }
        }
        Spacer(Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(modelBg, RoundedCornerShape(14.dp))
                .border(1.dp, modelBorder,RoundedCornerShape((14.dp)))
        ) {
            activities.forEachIndexed { index, activity ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { onNavigate("sales_sales_orders") }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(activity.iconBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(activity.icon, contentDescription = null, tint = activity.iconTint, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            activity.title,
                            fontSize = 13.sp,
                            color = Color(0xFF111827),
                            fontWeight = FontWeight.Medium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (activity.timeAgo.isNotBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Text(activity.timeAgo, fontSize = 11.sp, color = Color(0xFF9CA3AF))
                        }
                    }
                    if (activity.amount != null) {
                        Spacer(Modifier.width(8.dp))
                        Text(activity.amount, fontSize = 13.sp, color = Color(0xFF16A34A), fontWeight = FontWeight.Bold)
                    }
                }

                // 👇 divider between rows, not after the last one
                if (index != activities.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 14.dp, end = 14.dp),
                        thickness = 1.dp,
                        color = modelBorder
                    )
                }
            }
        }
    }
}

// ── Recent Customers — driven by API `operations` ──
@Composable
private fun RecentCustomersSection(
    customers: List<RecentCustomer>,
    onNavigate: (String) -> Unit
) {
    Column {
        Text("Recent Customers", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
        Spacer(Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(modelBg, RoundedCornerShape(14.dp))
                .border(1.dp, modelBorder,RoundedCornerShape((14.dp)))// 👈 single outer card
        ) {
            customers.forEachIndexed { index, customer ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { onNavigate("sales_customers") }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(customer.avatarColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(customer.initials, color = customer.avatarColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(customer.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF111827))
                        Text(customer.role, fontSize = 12.sp, color = Color(0xFF9CA3AF))
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(25.dp))
                }

                // 👇 divider between rows, not after the last one
                if (index != customers.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 14.dp, end = 14.dp),
                        thickness = 1.dp,
                        color = modelBorder
                    )
                }
            }
        }
    }
}



// ─────────────────────────────────────────────────────────────
// buildFilterSections
// ─────────────────────────────────────────────────────────────
fun buildFilterSections(
    previous: List<FilterSection>,
    statuses: List<SalesStatusEntity>,
    garments: List<CategoryItem>,
    staff: List<StaffDto>,
    dynamicSources: List<String>
): List<FilterSection> {
    fun preserveSelection(title: String, options: List<FilterOption>): List<FilterOption> {
        val prevSelectedIds = previous.find { it.title == title }
            ?.options?.filter { it.isSelected }?.map { it.id }?.toSet() ?: emptySet()
        return options.map { it.copy(isSelected = it.id in prevSelectedIds) }
    }

    return listOf(
        FilterSection(
            title = "Date Range", icon = Icons.Filled.CalendarMonth,
            type = FilterSectionType.CHIP_GRID, isMultiSelect = false,
            options = preserveSelection("Date Range", listOf(
                FilterOption("today", "Today"),
                FilterOption("week", "This Week"),
                FilterOption("month", "This Month"),
                FilterOption("custom", "Custom")
            ))
        ),
        FilterSection(
            title = "Status", icon = Icons.Filled.Sell,
            type = FilterSectionType.CHECKBOX_LIST, isMultiSelect = true,
            options = preserveSelection(
                "Status",
                statuses.map { FilterOption(id = it.id, label = it.name) }
            )
        ),
        FilterSection(
            title = "Source", icon = Icons.Filled.Campaign,
            type = FilterSectionType.CHIP_ROW, isMultiSelect = true,
            options = preserveSelection(
                "Source",
                dynamicSources.map { FilterOption(id = it, label = it) }
            )
        ),
        FilterSection(
            title = "Garments", icon = Icons.Filled.Checkroom,
            type = FilterSectionType.CHIP_ROW_MORE, isMultiSelect = true,
            options = preserveSelection(
                "Garments",
                garments.map { FilterOption(id = it.id, label = it.categoryId.categoryName) }
            )
        ),
        FilterSection(
            title = "Amount Range", icon = Icons.Filled.CurrencyRupee,
            type = FilterSectionType.AMOUNT_RANGE,
            minAmount = previous.find { it.title == "Amount Range" }?.minAmount ?: "",
            maxAmount = previous.find { it.title == "Amount Range" }?.maxAmount ?: "",
            options = emptyList()
        ),
        FilterSection(
            title = "Sales Person", icon = Icons.Filled.People,
            type = FilterSectionType.CHECKBOX_LIST, isMultiSelect = true,
            options = preserveSelection(
                "Sales Person",
                staff.map { FilterOption(id = it.id, label = "${it.firstName} ${it.lastName}") }
            )
        ),
        FilterSection(
            title = "Location", icon = Icons.Filled.LocationOn,
            type = FilterSectionType.DROPDOWN,
            options = emptyList()
        ),
        FilterSection(
            title = "Priority", icon = Icons.Filled.Flag,
            type = FilterSectionType.PRIORITY_DOTS, isMultiSelect = false,
            options = preserveSelection("Priority", listOf(
                FilterOption("high", "High"),
                FilterOption("medium", "Medium"),
                FilterOption("low", "Low")
            ))
        )
    )
}



// ─────────────────────────────────────────────────────────────
// LeadCard
// ─────────────────────────────────────────────────────────────
//

fun formatLeadDate(raw: String?): String {
    if (raw.isNullOrBlank()) return "—"
    return try {
        val datePart = raw.take(10)
        val parts = datePart.split("-")
        if (parts.size == 3) "${parts[2]}-${parts[1]}-${parts[0]}" else raw
    } catch (_: Exception) { raw }
}

fun formatIndianNumber(number: Number): String {
    val value = number.toLong()

    if (value <= 0) return "0"

    val s = value.toString()

    if (s.length <= 3) return s

    val last3 = s.takeLast(3)
    val rest = s.dropLast(3)
    val grouped = rest.reversed().chunked(2).joinToString(",").reversed()

    return "$grouped,$last3"
}


@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerField(
    value: String,
    onTimeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showPicker by remember { mutableStateOf(false) }

    var selectedHour by remember(value) {
        mutableIntStateOf(
            if (value.isNotEmpty()) {
                try {
                    val hourStr = value.substringBefore(":").trim()
                    val hour = hourStr.toInt()
                    if (value.contains("PM", ignoreCase = true) && hour != 12) hour + 12
                    else if (value.contains("AM", ignoreCase = true) && hour == 12) 0
                    else hour
                } catch (_: Exception) { 10 }
            } else 10
        )
    }

    var selectedMinute by remember(value) {
        mutableIntStateOf(
            if (value.isNotEmpty()) {
                try {
                    val parts = value.split(":")
                    if (parts.size >= 2) {
                        parts[1].take(2).toInt()
                    } else 0
                } catch (_: Exception) { 53 }
            } else 53
        )
    }

    var isAm by remember(value) {
        mutableStateOf(
            if (value.isNotEmpty()) {
                !value.contains("PM", ignoreCase = true)
            } else true
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(8.dp))
            .border(1.dp, PrimaryBorder, RoundedCornerShape(8.dp))
            .clickable { showPicker = true }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = value.ifEmpty { "Select Time" },
            fontSize = 14.sp,
            color = if (value.isEmpty()) Color(0xFF9CA3AF) else Color(0xFF374151)
        )
        Icon(
            Icons.Default.Schedule,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(18.dp)
        )
    }

    if (showPicker) {
        AlertDialog(
            onDismissRequest = { showPicker = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    "Appointment Time",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
            },
            text = {
                CustomTimePicker(
                    hour = selectedHour,
                    minute = selectedMinute,
                    isAm = isAm,
                    onHourChange = { selectedHour = it },
                    onMinuteChange = { selectedMinute = it },
                    onAmPmChange = { isAm = it }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val displayHour = when {
                            selectedHour == 0 -> 12
                            selectedHour > 12 -> selectedHour - 12
                            else -> selectedHour
                        }
                        val amPm = if (isAm) "AM" else "PM"
                        val formattedTime = String.format("%02d:%02d %s", displayHour, selectedMinute, amPm)
                        onTimeSelected(formattedTime)
                        showPicker = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = LeadPrimary
                    )
                ) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showPicker = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFF6B7280)
                    )
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun CustomTimePicker(
    hour: Int,
    minute: Int,
    isAm: Boolean,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit,
    onAmPmChange: (Boolean) -> Unit
) {
    val hourOptions = (1..12).toList()          // 12 values
    val minuteOptions = (0..59).map { String.format("%02d", it) }  // 60 values

    // ✅ NEW — repeat each list many times so scrolling in either
    // direction never hits a hard edge; feels like infinite/circular scroll.
    val hourRepeatCount = 1000
    val minuteRepeatCount = 1000
    val totalHourItems = hourOptions.size * hourRepeatCount
    val totalMinuteItems = minuteOptions.size * minuteRepeatCount

    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    val displayMinute = String.format("%02d", minute)

    // ✅ NEW — start near the middle of the repeated list, not at index 0,
    // so the user has "room" to scroll up or down before ever hitting an edge.
    val initialHourIndex = remember {
        (hourRepeatCount / 2) * hourOptions.size + hourOptions.indexOf(displayHour).coerceAtLeast(0)
    }
    val initialMinuteIndex = remember {
        (minuteRepeatCount / 2) * minuteOptions.size + minuteOptions.indexOf(displayMinute).coerceAtLeast(0)
    }

    val hourScrollState = rememberLazyListState(initialFirstVisibleItemIndex = initialHourIndex)
    val minuteScrollState = rememberLazyListState(initialFirstVisibleItemIndex = initialMinuteIndex)

    val hourCenterIndex by remember {
        derivedStateOf {
            val layoutInfo = hourScrollState.layoutInfo
            val center = layoutInfo.viewportEndOffset / 2
            val visibleItems = layoutInfo.visibleItemsInfo
            val closest = visibleItems.minByOrNull {
                val itemCenter = (it.offset + it.size / 2)
                kotlin.math.abs(itemCenter - center)
            }
            closest?.index ?: 0
        }
    }

    val minuteCenterIndex by remember {
        derivedStateOf {
            val layoutInfo = minuteScrollState.layoutInfo
            val center = layoutInfo.viewportEndOffset / 2
            val visibleItems = layoutInfo.visibleItemsInfo
            val closest = visibleItems.minByOrNull {
                val itemCenter = (it.offset + it.size / 2)
                kotlin.math.abs(itemCenter - center)
            }
            closest?.index ?: 0
        }
    }

    // ✅ NEW — map the repeated-list index back to the real 1-12 hour value
    LaunchedEffect(hourCenterIndex) {
        if (hourCenterIndex in 0 until totalHourItems) {
            val newHour = hourOptions[hourCenterIndex % hourOptions.size]
            val hour24 = when {
                newHour == 12 && isAm -> 0
                newHour == 12 && !isAm -> 12
                !isAm -> newHour + 12
                else -> newHour
            }
            onHourChange(hour24)
        }
    }

    // ✅ NEW — map the repeated-list index back to the real 00-59 minute value
    LaunchedEffect(minuteCenterIndex) {
        if (minuteCenterIndex in 0 until totalMinuteItems) {
            onMinuteChange(minuteOptions[minuteCenterIndex % minuteOptions.size].toInt())
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Hour", fontSize = 12.sp, color = LeadTextMuted, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(4.dp))

            Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .align(Alignment.Center)
                        .background(LeadPrimarySoft, RoundedCornerShape(8.dp))
                )

                LazyColumn(
                    state = hourScrollState,
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    flingBehavior = androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior(
                        lazyListState = hourScrollState
                    )
                ) {
                    // ✅ NEW — items(count) over the repeated total, value derived via modulo
                    items(totalHourItems) { i ->
                        val h = hourOptions[i % hourOptions.size]
                        val isSelected = i == hourCenterIndex
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = String.format("%02d", h),
                                fontSize = if (isSelected) 24.sp else 18.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) LeadPrimary else Color(0xFF6B7280)
                            )
                        }
                    }
                }
            }
        }

        Text(":", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827), modifier = Modifier.padding(horizontal = 4.dp))

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Minute", fontSize = 12.sp, color = LeadTextMuted, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(4.dp))

            Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .align(Alignment.Center)
                        .background(LeadPrimarySoft, RoundedCornerShape(8.dp))
                )

                LazyColumn(
                    state = minuteScrollState,
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    flingBehavior = androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior(
                        lazyListState = minuteScrollState
                    )
                ) {
                    // ✅ NEW — items(count) over the repeated total, value derived via modulo
                    items(totalMinuteItems) { i ->
                        val m = minuteOptions[i % minuteOptions.size]
                        val isSelected = i == minuteCenterIndex
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = m,
                                fontSize = if (isSelected) 24.sp else 18.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) LeadPrimary else Color(0xFF6B7280)
                            )
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier.weight(0.8f).padding(start = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("AM/PM", fontSize = 12.sp, color = LeadTextMuted, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(4.dp))

            Column(
                modifier = Modifier.fillMaxWidth().height(200.dp).padding(vertical = 20.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isAm) LeadPrimary else Color.Transparent)
                        .clickable { onAmPmChange(true) }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("AM", fontSize = 16.sp, fontWeight = if (isAm) FontWeight.Bold else FontWeight.Normal, color = if (isAm) Color.White else Color(0xFF6B7280))
                }

                Spacer(Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (!isAm) LeadPrimary else Color.Transparent)
                        .clickable { onAmPmChange(false) }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("PM", fontSize = 16.sp, fontWeight = if (!isAm) FontWeight.Bold else FontWeight.Normal, color = if (!isAm) Color.White else Color(0xFF6B7280))
                }
            }
        }
    }
}




@Composable
fun FormLabel(text: String?, isRequired: Boolean = false) {  // ✅ Made nullable
    Row {
        Text(
            text = text ?: "",  // ✅ Null-safe
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray
        )
        if (isRequired) {
            Text(
                text = " *",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Red
            )
        }
    }
    Spacer(Modifier.height(6.dp))
}

@Suppress("UNUSED_PARAMETER")
@Composable
fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    placeholder: String = "",
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true            // ✅ NEW
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(
                if (enabled) Color.White else Color(0xFFF3F4F6),   // ✅ greyed bg when disabled
                RoundedCornerShape(8.dp)
            )
            .border(
                1.dp,
                if (isError) Color(0xFFEF4444) else Color(0xFFE5E7EB),
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        if (value.isEmpty() && placeholder.isNotEmpty()) {
            Text(placeholder, fontSize = 14.sp, color = Color(0xFF9CA3AF))
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,          // ✅ actually blocks focus/typing now
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            textStyle = TextStyle(
                fontSize = 14.sp,
                color = if (enabled) Color(0xFF374151) else Color(0xFF6B7280)
            )
        )
    }
    if (isError && !errorMessage.isNullOrBlank()) {
        Text(
            errorMessage,
            fontSize = 11.sp,
            color = Color(0xFFEF4444),
            modifier = Modifier.padding(top = 4.dp, start = 4.dp)
        )
    }
}
@Composable
fun FormDateField(value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(Color.White, RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        BasicTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            textStyle = TextStyle(
                fontSize = 14.sp,
                color = Color(0xFF374151)
            ),
            modifier = Modifier.weight(1f),
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty()) {
                        Text(
                            text = "dd-mm-yyyy",
                            fontSize = 14.sp,
                            color = Color(0xFF9CA3AF)
                        )
                    }
                    innerTextField()
                }
            }
        )
        Icon(
            Icons.Default.CalendarMonth,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(18.dp)
        )
    }
}



@Suppress("UNUSED_PARAMETER")
@Composable
fun FormDropdown(
    label: String? = null,  // ✅ Made nullable
    value: String,
    expanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    isRequired: Boolean = false,
    enabled: Boolean = true,
    isError: Boolean = false,          // ✅ NEW
    errorMessage: String? = null
) {
    // ✅ Null-safe label
    if (!label.isNullOrEmpty()) {
        FormLabel(label, isRequired)
    } else {
        // If no label, still add spacing for consistency
        Spacer(Modifier.height(6.dp))
    }

    val density = LocalDensity.current
    var triggerWidthPx by remember { mutableIntStateOf(0) }

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates -> triggerWidthPx = coordinates.size.width }
                .height(40.dp)
                .background(
                    if (enabled) Color.White else Color(0xFFF3F4F6),
                    RoundedCornerShape(8.dp)
                )
                .border(
                    1.dp,
                    if (isError) Color(0xFFEF4444) else Color(0xFFE5E7EB),   // ✅ red border on error
                    RoundedCornerShape(8.dp)
                )                .clickable(enabled = enabled) { onExpandChange(true) }
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                value,
                fontSize = 12.sp,
                color = when {
                    !enabled -> Color(0xFF9CA3AF)
                    value == "Select an option" -> Color(0xFF9CA3AF)
                    else -> Color(0xFF374151)
                }
            )
            Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = if (enabled) Color.Gray else Color(0xFFD1D5DB)
            )
        }
        if (enabled) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { onExpandChange(false) },
                containerColor = Color.White,
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                    .width(with(density) { triggerWidthPx.toDp() })
                    .heightIn(max = 180.dp)
            ) {
                options.forEach { option ->
                    Text(
                        text = option,
                        fontSize = 14.sp,
                        color = Color(0xFF374151),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onOptionSelected(option)
                                onExpandChange(false)
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

//@SuppressLint("FrequentlyChangingValue","UNUSED_PARAMETER")
//@Composable
//fun HorizontalScrollbar(state: LazyListState, modifier: Modifier = Modifier, trackColor: Color = Color(0xFFE5E7EB), thumbColor: Color = Color.Gray, height: androidx.compose.ui.unit.Dp = 4.dp) {
//    val layoutInfo       = state.layoutInfo
//    val visibleItemsInfo = layoutInfo.visibleItemsInfo
//    val totalItems       = layoutInfo.totalItemsCount
//    val canScroll        = state.canScrollForward || state.canScrollBackward
//
//    if (totalItems == 0 || visibleItemsInfo.isEmpty() || !canScroll) {
//        Box(modifier = modifier
//            .fillMaxWidth()
//            .height(height))
//        return
//    }
//
//    val viewportSize              = (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset).toFloat()
//    val averageItemSize           = visibleItemsInfo.sumOf { it.size }.toFloat() / visibleItemsInfo.size
//    val estimatedTotalContentSize = averageItemSize * totalItems
//    val thumbSizeFraction         = (viewportSize / estimatedTotalContentSize).coerceIn(0.1f, 1f)
//    val scrolledPixels            = state.firstVisibleItemIndex * averageItemSize + state.firstVisibleItemScrollOffset
//    val maxScrollPixels           = (estimatedTotalContentSize - viewportSize).coerceAtLeast(1f)
//    val scrollFraction            = (scrolledPixels / maxScrollPixels).coerceIn(0f, 1f)
//
//    BoxWithConstraints(modifier = modifier
//        .fillMaxWidth()
//        .height(height)
//        .background(trackColor, RoundedCornerShape(height / 2))) {
//        val trackWidth  = this@BoxWithConstraints.maxWidth
//        val thumbWidth  = trackWidth * thumbSizeFraction
//        val thumbOffset = (trackWidth - thumbWidth) * scrollFraction
//        Box(modifier = Modifier
//            .offset(x = thumbOffset)
//            .width(thumbWidth)
//            .height(height)
//            .background(thumbColor, RoundedCornerShape(height / 2)))
//    }
//}
// ─────────────────────────────────────────────────────────────
// Route alias normalizer — single source of truth for all
// route-name variations that buildNavigationKey() or menu
// clicks might produce. Add new aliases here ONLY.
// ─────────────────────────────────────────────────────────────
fun normalizeRoute(rawKey: String): String {
    return when (rawKey) {
        // Sales
        "sales_lead_management"        -> "sales_lead"
        "sales_customer"               -> "sales_customers"
        "sales_measurements"           -> "sales_measurements"
        "sales_sales_&_orders",
        "sales_sales_and_orders"       -> "sales_sales_orders"
        "sales_order_management"       -> "sales_orders"
        "sales_pricing_overview"       -> "sales_pricing_overview"
        "sales_quotation"              -> "sales_pricing_quotation"

        // Finance
        "finance_sales_invoices"       -> "finance_sales_invoices"
        "finance_customers"            -> "finance_customers"
        "finance_payments_received"    -> "finance_payments_received"
        "finance_suppliers"            -> "finance_suppliers"
        "finance_expenses"             -> "finance_expenses"
        "finance_chart_of_accounts"    -> "finance_chart_of_accounts"
        "finance_journal_entries"      -> "finance_journal_screen"
        "finance_trial_balance"        -> "finance_trial_balance"

        // Inventory
        "inventory_all_items"          -> "inventory_items"
        "inventory_item_groups"        -> "inventory_item_groups"
        "inventory_orders",              // ✅ Procurement > Orders sub-item click
        "inventory_procurement_orders",
        "inventory_low_stock_alerts",
        "inventory_alerts_&_reorder"   -> "inventory_low_stock_alerts"

        // Logistics
        "logistics_delivery"           -> "logistics_delivery"
        "logistics_order_tracking"     -> "logistics_order_tracking"

        // Services
        "services_customer_feedback"   -> "services_customer_feedback"
        "services_alteration_management" -> "services_alteration_management"
        "services_service_request" -> "services_service_request"

        // HR
        "hr_employees"                 -> "hr_all_employees"

        // Reports
        "reports_sales_reports"        -> "reports_sales_reports"

        else -> rawKey   // no mapping found — passes through, will hit `else -> {}` and just close panel silently
    }
}
fun menuForScreen(screen: String): String = when {
    screen == "home" || screen == "settings" || screen == "profile-settings" ||
            screen.startsWith("home_") -> "Home"

    screen.startsWith("sales_") || screen in setOf(
        "create_lead", "view_lead", "edit_lead",
        "create_order", "order_overview", "create_order_review",
        "view_customer", "edit_customer",
        "create_quotation", "create_garment_pricing", "garment_pricing_list",
        "order_management_overview"
    ) -> "Sales"

    screen.startsWith("finance_") -> "Finance"
    screen.startsWith("inventory_") -> "Inventory"
    screen.startsWith("hr_") -> "HR"
    screen.startsWith("logistics_") || screen == "tracking_overview" -> "Logistics"

    else -> "Home"
}
fun String.toIsoDate(): String {
    if (this.isEmpty() || this == "Select Date") return ""
    return try {
        val parts = this.split("-")
        if (parts.size == 3) "${parts[2]}-${parts[1]}-${parts[0]}T00:00:00.000Z" else ""
    } catch (_: Exception) { "" }
}