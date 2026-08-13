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
    "KotlinConstantConditions",
    "VariableNeverRead"
)

package com.cuso.mobile.view.home

import android.annotation.SuppressLint
import android.os.Build
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
import com.cuso.mobile.view.composable.FilterOption
import com.cuso.mobile.view.composable.FilterSection
import com.cuso.mobile.view.composable.FilterSectionType
import com.cuso.mobile.R
import com.cuso.mobile.view.home.sidebar.ModulesPanel
import com.cuso.mobile.view.home.sidebar.SidebarConfig
import com.cuso.mobile.view.home.sidebar.buildNavigationKey
//import com.cuso.mobile.model.ActiveOrderItem
import com.cuso.mobile.model.sales.CustomerItem
import com.cuso.mobile.model.DashboardStatDto
import com.cuso.mobile.model.OperationItem
import com.cuso.mobile.view.home.sales.customer.CustomerScreen
import com.cuso.mobile.view.home.sales.measurements.MeasurementsScreen
import com.cuso.mobile.viewmodel.CustomerViewModel
import com.cuso.mobile.viewmodel.DashboardUiState
import com.cuso.mobile.viewmodel.DashboardViewModel
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.cuso.mobile.view.home.inventory.InventoryViewOne
import com.cuso.mobile.ui.theme.PrimaryBorder
import com.cuso.mobile.ui.theme.Primary_background
import com.cuso.mobile.ui.theme.TextSecondary
import com.cuso.mobile.ui.theme.blackTitle
import com.cuso.mobile.ui.theme.modelBg
import com.cuso.mobile.ui.theme.modelBorder
import com.cuso.mobile.ui.theme.statLogoBg
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.CirculerProgressIndicatorReuse
import com.cuso.mobile.view.composable.DynamicIslandSuccess
import com.cuso.mobile.view.home.finance.AllPaymentScreen
import com.cuso.mobile.view.home.finance.AllSuppliersScreen
import com.cuso.mobile.view.home.finance.ChartOfAccountScreen
import com.cuso.mobile.view.home.finance.ExpensesScreen
import com.cuso.mobile.view.home.finance.FinanceCustomerScreen
import com.cuso.mobile.view.home.finance.FinanceInvoiceScreen
import com.cuso.mobile.view.home.finance.LedgerScreen
import com.cuso.mobile.view.home.finance.ManualJournalEntryScreen
import com.cuso.mobile.view.home.finance.PaymentDetailScreenAR
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
import com.cuso.mobile.view.composable.DashboardSkeleton
import com.cuso.mobile.view.composable.blurScrim
import com.cuso.mobile.view.home.sales.customer.CustomerDetailScreen
import com.cuso.mobile.view.home.sales.ordermanagement.OrderManagementScreen
import com.cuso.mobile.view.home.sales.pricing.PricingScreen
import com.cuso.mobile.view.home.sales.quotation.CreateQuotationScreen
import com.cuso.mobile.view.home.sales.quotation.QuotationScreen
import com.cuso.mobile.view.home.sales.sales_order.toOrderReviewData
import com.cuso.mobile.viewmodel.CustomerDeleteState
import com.cuso.mobile.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch
import com.cuso.mobile.view.home.sales.lead.LeadScreenContent
import com.cuso.mobile.view.home.sales.lead.LeadFormScreen
import com.cuso.mobile.view.home.sales.lead.LeadFormMode
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
import com.cuso.mobile.view.home.logistics.TrackingOverviewScreen
import kotlin.collections.isNotEmpty
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.ui.draw.scale
import com.cuso.mobile.adaptive_screen.AppDesignTokens
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.adaptive_screen.getAdaptiveTokens
import com.cuso.mobile.view.home.branch.RoleSettingsScreen
import com.cuso.mobile.view.home.finance.AllPaymentListScreen
import com.cuso.mobile.view.home.finance.PaymentDetailScreenAP
import com.cuso.mobile.view.home.finance.PurchaseInvoiceItem
import com.cuso.mobile.view.home.finance.PurchaseInvoiceScreen
import com.cuso.mobile.view.home.hr.AttendanceDetailScreen
import com.cuso.mobile.view.home.hr.AttendanceScreen
import com.cuso.mobile.view.home.opening_balance.OpeningBalancesScreen
import com.cuso.mobile.view.home.reports.DeadStockReportScreen
import com.cuso.mobile.view.home.reports.FinanceReportPage
import com.cuso.mobile.view.home.reports.InventoryReportPage
import com.cuso.mobile.view.home.reports.LowStockScreen
import com.cuso.mobile.view.home.reports.ProfitAndLossReportScreen
import com.cuso.mobile.view.home.reports.PurchaseReportScreen
import com.cuso.mobile.view.home.reports.StockSummaryScreen
import com.cuso.mobile.view.home.reports.WarehouseReportScreen
import com.cuso.mobile.view.home.sales.payment_listing.PaymentInformationScreen
import com.cuso.mobile.view.home.sales.payment_listing.PaymentListingScreen
import com.cuso.mobile.view.home.sidebar.QuickAccessPanel
import com.cuso.mobile.view.home.warehouse.WarehouseSettingsScreen
import java.time.LocalTime

// ── Design tokens (Primary color used everywhere for icons / accents) ──
val LeadPrimary = Color(0xFF3B3BF9)
val LeadPrimarySoft = Color(0xFFEEEEFE)
val LeadmutedText = Color(0xFF9CA3AF)
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Suppress("UnusedMaterial3ScaffoldPaddingParameter","UNUSED_PARAMETER")
@Composable
fun HomeScreen(navController: NavHostController, widthSizeClass: WindowWidthSizeClass) {
    val viewModel: HomeViewModel = hiltViewModel()
    val authViewModel: Authenticate = hiltViewModel()
    val hrViewModel: HrViewModel = hiltViewModel()



    val token: String = authViewModel.tokens.value?.accessToken ?: ""
    val isLoggedOut: Boolean by viewModel.isLoggedOut.collectAsStateWithLifecycle(initialValue = false)
    val screenStack = remember { mutableStateListOf("home") }
    val currentScreen: String = screenStack.last()
    val tokens = getAdaptiveTokens(widthSizeClass)


    //   NEW — animation direction tracking (push vs pop)
    var previousStackSize by remember { mutableIntStateOf(screenStack.size) }
    val isForwardNavigation = screenStack.size >= previousStackSize
    LaunchedEffect(screenStack.size) {
        previousStackSize = screenStack.size
    }


    val profileViewModel: ProfileViewModel = hiltViewModel()

    var isDrawerOpen by remember { mutableStateOf(false) }
    var sidebarBlur by remember { mutableStateOf(0.dp) }
    var pendingOrderReviewData by remember { mutableStateOf<OrderReviewData?>(null) }
    val customerViewModel: CustomerViewModel = hiltViewModel()

    val customerUiState by customerViewModel.uiState.collectAsStateWithLifecycle()
    var selectedCustomer by remember { mutableStateOf<CustomerItem?>(null) }
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    var selectedOrderId by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val activity = context as ComponentActivity

    //   NEW — Finance > Trial Balance > Ledger flow
    var selectedLedgerAccountId by remember { mutableStateOf<String?>(null) }
    var selectedLedgerAccountName by remember { mutableStateOf("Ledger") }


    //delete
    val deleteState by customerViewModel.deleteState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var selectedManagementOrderId by remember { mutableStateOf<String?>(null) }
    // ── NEW — Finance > Accounts Payable > Payment Mode detail flow ──
    var selectedPaymentModeId by remember { mutableStateOf<String?>(null) }

    //   NEW — Finance > Accounts Receivable > Sales Invoices flow
//   NEW — Finance > Accounts Receivable > Sales Invoices flow
    var selectedInvoiceId by remember { mutableStateOf<String?>(null) }

    //   NEW — Finance > Accounts Payable > Purchase Invoices flow (static data)
    var selectedPurchaseInvoice by remember { mutableStateOf<PurchaseInvoiceItem?>(null) }
    //   NEW — Inventory > Item Detail flow
    var selectedInventoryItemId by remember { mutableStateOf<String?>(null) }

    var selectedLowStockItem by remember { mutableStateOf<LowStockItem?>(null) }
    //   NEW — HR > Employee Onboarding flow (Create / View / Edit)
    var employeeScreenMode by remember { mutableStateOf(com.cuso.mobile.view.home.hr.ScreenMode.CREATE) }
    var selectedEmployeeId by remember { mutableStateOf<String?>(null) }

//   NEW — HR > Attendance > Attendance Detail flow
    var selectedAttendanceId by remember { mutableStateOf<String?>(null) }

    var isSalesSettingsMode by remember { mutableStateOf(false) }

    var showModulesPanel by remember { mutableStateOf(false) }
    var modulesPanelInitialExpanded by remember { mutableStateOf<String?>(null) }

//   NEW — Quick Access bottom sheet (+ button)
    var showQuickAccessPanel by remember { mutableStateOf(false) }
    var quickAccessBlur by remember { mutableStateOf(0.dp) }

    //dynamicisland state
    var comingSoonMessage by remember { mutableStateOf<String?>(null) }


    val orderOverviewViewModel: com.cuso.mobile.viewmodel.OrderOverviewViewModel = hiltViewModel()
    val editOverviewState by orderOverviewViewModel.overviewState.collectAsStateWithLifecycle()
    var editOrderId by remember { mutableStateOf<String?>(null) }
    var editingPricingId by remember { mutableStateOf<String?>(null) }

    var quotationScreenMode by remember { mutableStateOf("create") }

    var selectedSupplier by remember { mutableStateOf<SupplierRow?>(null) }

    //   NEW — Services > Customer Feedback flow
    var selectedFeedbackId by remember { mutableStateOf<String?>(null) }

    //   NEW — Inventory > Item Groups flow
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
//            currentScreen == "home_role_management"||
//            currentScreen == "home_warehouse_management"||
//            currentScreen == "home_opening_balance"


    val showSalesPanel = isSalesSettingsMode

    //   NEW — System back button handling
    BackHandler(enabled = isDrawerOpen || showModulesPanel || screenStack.size > 1) {
        when {
            // Priority 1: close overlays first
            showQuickAccessPanel -> showQuickAccessPanel = false
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
            currentScreen == "finance_payments_mode" -> {
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
            currentScreen == "hr_attendance_detail" -> {
                selectedAttendanceId = null
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
            currentScreen == "payment_mode_detail" -> {
                selectedPaymentModeId = null
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
        CompositionLocalProvider(LocalAppTokens provides tokens) {


            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = Primary_background,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
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
                                    resetToHome()
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
                                "finance_payments_mode" ->{
                                    isSalesSettingsMode = false
                                    navigateTo("finance_payments_mode")
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

                                "finance_purchase_invoices", "finance_accounts_payable" -> {
                                    isSalesSettingsMode = false
                                    navigateTo("finance_purchase_invoices")
                                    isDrawerOpen = false
                                }

                                "home" -> {
                                    isSalesSettingsMode = false
                                    resetToHome()
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

                                "hr_attendance" -> {
                                    isSalesSettingsMode = false
                                    navigateTo("hr_attendance")
                                    isDrawerOpen = false
                                }

                                "logistics_delivery" -> {
                                    isSalesSettingsMode = false
                                    navigateTo("logistics_delivery")
                                    isDrawerOpen = false
                                }

                                "reports_sales", "reports_finance_reports" -> {
                                    isSalesSettingsMode = false
                                    navigateTo("reports_sales")
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
                        onAddClick = {
                            showQuickAccessPanel = true
                        },
                        onLogout = {
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        showHomePanel = showHomePanel,
                        showSalesPanel = showSalesPanel,
                        isSalesSettingsMode = isSalesSettingsMode,
                        onBlurScrimChange = { radius, _ -> sidebarBlur = radius }
                    )
                }

            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Transparent)
                        .padding(innerPadding)
                        .blurScrim(sidebarBlur)   //   UPDATED: blurBehindSheet -> blurScrim
                ) {
                    AnimatedContent(
                        targetState = currentScreen,
                        modifier = Modifier.fillMaxSize(),
                        transitionSpec = {
                            // Industry-style push/pop transition: incoming screen eases in with
                            // FastOutSlowInEasing (Material's standard "emphasized" curve) while the
                            // outgoing screen fades a touch faster so there's never a blank frame
                            // between the two — both animations run on the SAME AnimatedContent,
                            // so nothing else should add its own slide/fade on top of this one.
                            val slideSpec = tween<androidx.compose.ui.unit.IntOffset>(
                                durationMillis = 320,
                                easing = androidx.compose.animation.core.FastOutSlowInEasing
                            )
                            val fadeInSpec = tween<Float>(
                                durationMillis = 220,
                                easing = androidx.compose.animation.core.LinearOutSlowInEasing
                            )
                            val fadeOutSpec = tween<Float>(
                                durationMillis = 180,
                                easing = androidx.compose.animation.core.FastOutLinearInEasing
                            )
                            if (isForwardNavigation) {
                                (slideInHorizontally(
                                    animationSpec = slideSpec,
                                    initialOffsetX = { fullWidth -> fullWidth }
                                ) + fadeIn(fadeInSpec)) togetherWith
                                        (slideOutHorizontally(
                                            animationSpec = slideSpec,
                                            targetOffsetX = { fullWidth -> -fullWidth / 5 }
                                        ) + fadeOut(fadeOutSpec))
                            } else {
                                (slideInHorizontally(
                                    animationSpec = slideSpec,
                                    initialOffsetX = { fullWidth -> -fullWidth / 5 }
                                ) + fadeIn(fadeInSpec)) togetherWith
                                        (slideOutHorizontally(
                                            animationSpec = slideSpec,
                                            targetOffsetX = { fullWidth -> fullWidth }
                                        ) + fadeOut(fadeOutSpec))
                            }.using(
                                androidx.compose.animation.SizeTransform(clip = false)
                            )
                        },
                        label = "screen_transition"
                    ) { screen ->
                        when (screen) {
                            "settings" -> SettingsScreen(
                                navController = navController,
                                onMenuClick = { isDrawerOpen = true },
                                onBack = { goBack() }
                            )

                            "home_organization_profile" -> SettingsScreen(
                                navController = navController,
                                onMenuClick = { isDrawerOpen = true },
                                onBack = { goBack() }

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
                                widthSizeClass = widthSizeClass,
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

                            "create_lead" -> LeadFormScreen(
                                mode = LeadFormMode.CREATE,
                                onBack = { goBack() }
                            )

                            "view_lead" -> LeadFormScreen(
                                mode = LeadFormMode.VIEW,
                                onBack = { goBack() },
                                onEditRequested = { navigateTo("edit_lead") }
                            )

                            "edit_lead" -> LeadFormScreen(
                                mode = LeadFormMode.EDIT,
                                onBack = { goBack() },
                                onConvertToOrder = { orderReviewData ->
                                    pendingOrderReviewData = orderReviewData
                                    navigateTo("create_order")
                                }
                            )
                            "sales_sales_orders" -> SalesOrderScreen(
                                navController = navController,
                                onMenuClick = { isDrawerOpen = true },
                                onBack = { goBack() },
                                onCreateOrder = {
                                    pendingOrderReviewData = null
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

                            "create_order" -> {
                                CreateOrderScreen(
                                    initialData = pendingOrderReviewData,
                                    onBack = {
                                        pendingOrderReviewData = null
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

                            "order_overview" -> {
                                selectedOrderId?.let { id ->
                                    OrderOverviewScreen(
                                        orderId = id,
                                        onClose = {
                                            goBack()
                                        },
                                        onEditOrder = { reviewData ->
                                            pendingOrderReviewData = reviewData
                                            navigateTo("create_order")
                                        },
                                        onCreateNew = {
                                            pendingOrderReviewData = null
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

                            "finance_payments_mode" -> {
                                AllPaymentListScreen(
                                    onClose = { goBack() },
                                    onPaymentClick = { paymentId ->
                                        selectedPaymentModeId = paymentId
                                        navigateTo("payment_mode_detail")
                                    }
                                )
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
                                        selectedLowStockItem = null
                                        goBack()
                                        goBack()
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
                                },
                                hrViewModel = hrViewModel,
                                onBreadCrumbClick = {
                                    modulesPanelInitialExpanded = "HR"
                                    showModulesPanel = true
                                }
                            )
                            "hr_attendance" -> AttendanceScreen(
                                onClose = { goBack() },
                                onBreadCrumbClick = {
                                    modulesPanelInitialExpanded = "HR"
                                    showModulesPanel = true
                                },
                                onRecordClick = { recordId ->
                                    selectedAttendanceId = recordId
                                    navigateTo("hr_attendance_detail")
                                }
                            )
                            "hr_attendance_detail" -> {
                                AttendanceDetailScreen(
                                    onClose = {
                                        selectedAttendanceId = null
                                        goBack()
                                    },
                                    onBreadCrumbClick = {
                                        modulesPanelInitialExpanded = "HR"
                                        showModulesPanel = true
                                    },
                                    onHistoryClick = { date ->
                                        // future: navigate to a specific history date's detail if needed
                                    }
                                )
                            }

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
                                    val itemDetailViewModel: com.cuso.mobile.viewmodel.InventoryViewModel =
                                        hiltViewModel()
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
                                onClose = { goBack() },
                                onCreateNewAlteration = { navigateTo("create_alteration") },
                                onBreadcrumbClick = {
                                    modulesPanelInitialExpanded = "Services"
                                    showModulesPanel = true
                                },
                                onViewClick = {}
                            )

                            "create_alteration" -> CreateAlterationManagementScreen(
                                onClose = { goBack() },
                            )

                            "services_service_request" -> ServiceRequestScreen(
                                onClose = {},
                                onBreadcrumbClick = {},
                                onCreateNewRequest = { navigateTo("create_request") },
                                onViewClick = { navigateTo("review_services") }
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
                                onAddNewPricing = {
                                    editingPricingId = null; navigateTo("create_garment_pricing")
                                },
                                onCardClick = { pricingId ->
                                    editingPricingId = pricingId; navigateTo("create_garment_pricing")
                                },
                                onBreadCrumbClick = {
                                    modulesPanelInitialExpanded = "Sales"
                                    showModulesPanel = true
                                }
                            )
                            "sales_payment_and_billing" -> PaymentListingScreen(
                                navController = navController,
                                widthSizeClass = calculateWindowSizeClass(activity).widthSizeClass,
                                onBack = { isSalesSettingsMode = false; goBack() },
                                onBreadCrumbClick = {
                                    modulesPanelInitialExpanded = "Sales"
                                    showModulesPanel = true
                                },
                                onPaymentClick = { navigateTo("payment_detail") }
                            )
                            "payment_detail" -> PaymentInformationScreen(
                                onClose = { goBack() }
                            )

                            "garment_pricing_list" -> com.cuso.mobile.view.home.sales.pricing.GarmentPricingListScreen(
                                onBack = { goBack() },
                                onAddNewPricing = {
                                    editingPricingId = null; navigateTo("create_garment_pricing")
                                },
                                onCardClick = { pricingId ->
                                    editingPricingId = pricingId; navigateTo("create_garment_pricing")
                                }
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
                                onSearch = customerViewModel::onSearch,
                                onTypeFilterChange = customerViewModel::onTypeFilterChange,
                                onPageChange = customerViewModel::onPageChange,
                                onItemsPerPageChange = customerViewModel::onItemsPerPageChange,

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

                            "payment_detail_screen" -> PaymentDetailScreenAR(
                                onClose = { goBack() }
                            )
                            "payment_mode_detail" -> PaymentDetailScreenAP(
                                onClose = {
                                    selectedPaymentModeId = null
                                    goBack()
                                }
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
                            "finance_purchase_invoices" -> PurchaseInvoiceScreen(
                                onClose = { goBack() },
                                onInvoiceClick = { invoice ->
                                    selectedPurchaseInvoice = invoice
                                    navigateTo("finance_purchase_invoice_detail")
                                },
                                onBreadCrumbClick = {
                                    modulesPanelInitialExpanded = "Finance"
                                    showModulesPanel = true
                                }
                            )

                            "finance_purchase_invoice_detail" -> {
                                selectedPurchaseInvoice?.let { invoice ->
                                    com.cuso.mobile.view.home.finance.PurchaseInvoiceDetailScreen(
                                        invoiceId = invoice.id,
                                        onClose = {
                                            goBack()
                                        }
                                    )
                                } ?: run { goBack() }
                            }


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
                                            goBack()
                                        },
                                        onUpdateSuccess = {
                                            customerViewModel.refresh()
                                            goBack()
                                        },
                                        onRequestEdit = { navigateTo("edit_customer") }
                                    )
                                } else {
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
                                            goBack()
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
                                onHelpSupport = { navigateTo("") },
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

                            "reports_sales" -> SalesOrderReportsScreen(
                                onClose = { goBack() },
                                onBreadCrumbClick = {
                                    modulesPanelInitialExpanded = "Reports"
                                    showModulesPanel = true
                                }
                            )
                            "home_role_management" -> RoleSettingsScreen(
                                navController = navController,
                                onMenuClick = { isDrawerOpen = true },
                                onBack = { goBack() }
                            )
                            "home_opening_balance" -> OpeningBalancesScreen(
                                navController = navController,
                                onBack = { goBack() }
                            )
                            "home_warehouse_management" -> WarehouseSettingsScreen(
                                navController = navController,
                                onMenuClick = { isDrawerOpen = true },
                                onBack = { goBack() }
                            )

                            "reports_inventory" -> InventoryReportPage(
                                onClose = { goBack() },
                                onBreadCrumbClick = {
                                    modulesPanelInitialExpanded = "Reports"
                                    showModulesPanel = true
                                },
                                onReportClick = { route ->
                                    val implementedInventoryReports = setOf(
                                        "reports_inventory_stock_summary",
                                        "reports_inventory_low_stock",
                                        "reports_inventory_warehouse_report",
                                        "reports_inventory_purchase_report",
                                        "reports_inventory_dead_stock"
                                    )
                                    if (route in implementedInventoryReports) {
                                        navigateTo(route)
                                    } else {
                                        comingSoonMessage = "Coming Soon, Stay tuned !"
                                    }
                                }
                            )

                            "reports_inventory_stock_summary" -> StockSummaryScreen(
                                onClose = { goBack() }
                            )

                            "reports_inventory_low_stock" -> LowStockScreen(
                                onClose = { goBack() }
                            )

                            "reports_inventory_warehouse_report" -> WarehouseReportScreen(
                                onClose = { goBack() }
                            )

                            "reports_inventory_purchase_report" -> PurchaseReportScreen(
                                onClose = { goBack() }
                            )

                            "reports_inventory_dead_stock" -> DeadStockReportScreen(
                                onClose = { goBack() }
                            )

                            "reports_finance" -> FinanceReportPage(
                                onClose = { goBack() },
                                onBreadCrumbClick = {
                                    modulesPanelInitialExpanded = "Reports"
                                    showModulesPanel = true
                                },
                                onReportClick = { route ->
                                    val implementedFinanceReports = setOf(
                                        "reports_finance_profit_and_loss_report"

                                    )
                                    if (route in implementedFinanceReports) {
                                        navigateTo(route)
                                    } else {
                                        comingSoonMessage = "Coming Soon, Stay tuned !"
                                    }
                                }
                            )
                            "reports_finance_profit_and_loss_report" -> ProfitAndLossReportScreen(
                                onClose = { goBack() }
                            )


                            else -> {}
                        }
                    }
                }
            }
        }

        //   ModulesPanel remains the same
        ModulesPanel(
            isOpen = showModulesPanel,
            onClose = { showModulesPanel = false },
            initialExpandedModule = modulesPanelInitialExpanded,
            onModuleCategoryClick = { menu, category ->
                val menuItem = SidebarConfig.getFullMenuItems().find { it.label == menu }
                val firstSubItem = menuItem?.subItems?.get(category)?.firstOrNull()

                val rawNavKey = if (firstSubItem != null) {
                    buildNavigationKey(menu, firstSubItem)
                } else {
                    buildNavigationKey(menu, category)
                }
                val navKey = normalizeRoute(rawNavKey)

                val implementedRoutes = setOf(
                    // Sales
                    "sales_lead", "sales_customers", "sales_measurements", "sales_sales_orders",
                    "sales_orders", "sales_pricing_overview", "sales_pricing_quotation",
                    "sales_payment_and_billing",

                    // Finance
                    "finance_sales_invoices", "finance_purchase_invoices", "finance_customers",
                    "finance_payments_received",
                    "finance_suppliers",
                    "finance_expenses", "finance_chart_of_accounts",
                    "finance_journal_screen", "finance_trial_balance",
                    "finance_payments_mode",

                    // Inventory
                    "inventory_items",
                    "inventory_item_groups", "inventory_low_stock_alerts",

                    // Logistics
                    "logistics_delivery", "logistics_order_tracking",

                    // Services
                    "services_customer_feedback", "services_alteration_management", "services_service_request",

                    // HR
                    "hr_all_employees",
                    "hr_attendance",

                    // Reports
                    "reports_sales",
                    "reports_inventory",
                    "reports_finance"

                )

                isSalesSettingsMode = false
                showModulesPanel = false

                if (navKey in implementedRoutes) {
                    navigateTo(navKey)
                } else {
                    comingSoonMessage = "Coming Soon, Stay tuned !"
                }
            }
        )

        QuickAccessPanel(
            isOpen = showQuickAccessPanel,
            onClose = { showQuickAccessPanel = false },
            onItemClick = { route ->
                when (route) {
                    "create_order" -> {
                        isSalesSettingsMode = false
                        pendingOrderReviewData = null
                        navigateTo("create_order")
                    }
                    "create_customer" -> {
                        isSalesSettingsMode = false
                        navigateTo("sales_customers")
                        navigateTo("create_customer")
                    }
                    "quick_add" -> {
                    }
                    else -> {
                        isSalesSettingsMode = false
                        navigateTo(route)
                    }
                }
            },
            onBlurScrimChange = { radius, _ -> quickAccessBlur = radius }
        )

        DynamicIslandSuccess(
            modifier = Modifier.align(Alignment.TopCenter),
            message = comingSoonMessage,
            onDismiss = { comingSoonMessage = null }
        )
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
    val tokens = LocalAppTokens.current

    // EmployeeOnboardingScreen.kt la:
    val authViewModel: Authenticate = hiltViewModel(
        LocalContext.current as ComponentActivity
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
        color = whiteBg,
        shadowElevation = 1.dp,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = tokens.screenPadding, vertical = 0.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // ── Left: Logo ──
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.height(tokens.buttonHeight * 1.25f)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.cuso_tailor_logo),
                    contentDescription = "Logo",
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .size(tokens.buttonHeight * 2f)
                )
            }

            // ── Right: Search + Notification + Profile ──
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color(0xFF4B5563),
                    modifier = Modifier
                        .size(tokens.iconSize * 1.3f)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { onSearchClick() }
                )

                Spacer(modifier = Modifier.width(tokens.screenPadding * 0.9f))

                Box {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = Color(0xFF4B5563),
                        modifier = Modifier
                            .size(tokens.iconSize * 1.3f)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { onNotificationClick() }
                    )
                    if (hasNotification) {
                        Box(
                            modifier = Modifier
                                .size(tokens.iconSize * 0.5f)
                                .align(Alignment.TopEnd)
                                .offset(x = 2.dp, y = (-1).dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEF4444))
                                .border(1.5.dp, Color(0xFFF5F5FA), CircleShape)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(tokens.screenPadding * 0.9f))

                // ── Profile picture / initials ──
                val profilePicture = user?.profilePicture
                val avatarSize = if (isPanelMode) tokens.buttonHeight * 0.86f else tokens.buttonHeight * 0.95f

                if (!profilePicture.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(profilePicture)
                            .crossfade(true)
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
                            color = whiteBg,
                            fontWeight = FontWeight.Bold,
                            fontSize = tokens.bodySmall
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
    onModulesClick: () -> Unit = {},
    onAddClick: () -> Unit = {},              //   NEW
    onLogout: () -> Unit = {},
    showHomePanel: Boolean = false,
    showSalesPanel: Boolean = false,
    isSalesSettingsMode: Boolean = false,
    onBlurScrimChange: (radius: Dp, scrim: Float) -> Unit = { _, _ -> }
) {
    val tokens = LocalAppTokens.current
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

    Box {
        if (isSettingsOpen || showSalesPanel) {
            SalesSideBar(
                isOpen = isDrawerOpen,
                onClose = onDrawerClose,
                onMenuItemClick = { route ->
                    onMenuItemClick(route)
                    onDrawerClose()
                },
                onLogout = onLogout,
                onBlurScrimChange = onBlurScrimChange,
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
                onBlurScrimChange = onBlurScrimChange,
                user = user,
                defaultSelectedMenu = if (showHomePanel) "Home" else "Home"
            )
        }

        // ── TOP-ONLY ROUNDED SHAPE (Bottom corners strictly 0.dp) ──
        val bottomBarShape = RoundedCornerShape(
            topStart = tokens.cardCornerRadius * 1.6f,
            topEnd = tokens.cardCornerRadius * 1.6f,
            bottomStart = 0.dp,
            bottomEnd = 0.dp
        )

        // ── BOTTOM NAV BAR WRAPPER (Transparent background so no white box bleeds) ──
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Transparent)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 12.dp,          // 80dp -> 12dp, romba diffuse aagama sharp ah theriyum
                        shape = bottomBarShape,
                        clip = false,
                        ambientColor = Color.Black.copy(alpha = 0.45f),
                        spotColor = Color.Black.copy(alpha = 0.55f)
                    )

                    .clip(bottomBarShape),
                color = whiteBg,
                shape = bottomBarShape,
                tonalElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .height(tokens.buttonHeight * 1.7f)
                        .padding(horizontal = tokens.screenPadding * 1.25f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // ✅ Left half — Home + Orders, evenly spaced within its own weight
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
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
                    }

                    Spacer(modifier = Modifier.width(tokens.buttonHeight * 1.45f))


                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BottomNavItem(
                            icon = R.drawable.reports,
                            label = "Reports",
                            isSelected = currentScreen == "reports_sales",
                            selectedColor = Color(0xFF6C4FF6),
                            onClick = { onMenuItemClick("reports_sales") }
                        )

                        BottomNavItem(
                            icon = R.drawable.modules,
                            label = "Modules",
                            isSelected = currentScreen == "modules",
                            selectedColor = Color(0xFF6C4FF6),
                            onClick = {
                                Log.d("BOTTOM_BAR", "Modules button clicked")
                                onModulesClick()
                            }
                        )
                    }
                }
            }

            // Floating center "+" button — opens Quick Access sheet
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-22).dp)
                    .size(tokens.buttonHeight * 1.45f)
                    .shadow(10.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color(0xFF6C4FF6))
                    .clickable {
                        onAddClick()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create New",
                    tint = whiteBg,
                    modifier = Modifier.size(tokens.iconSize * 1.9f)
                )
            }
        }
    }
}

// Ensure these are imported at the top of your file
// import androidx.compose.animation.animateColorAsState
// import androidx.compose.animation.core.animateFloatAsState
// import androidx.compose.animation.core.tween
// import androidx.compose.ui.draw.scale

@Composable
fun BottomNavItem(
    icon: Int,
    label: String,
    isSelected: Boolean,
    selectedColor: Color = Color(0xFF6C4FF6),
    onClick: () -> Unit
) {
    val tokens = LocalAppTokens.current

    // 1. Animate the color transition smoothly (300ms duration)
    val animatedColor by animateColorAsState(
        targetValue = if (isSelected) selectedColor else Color(0xFF9CA3AF),
        animationSpec = tween(durationMillis = 300),
        label = "BottomNavItemColor"
    )

    // 2. Optional: Animate scale to make the selected item slightly larger
    val animatedScale by animateFloatAsState(
        targetValue = if (isSelected) 1.12f else 1.0f,
        animationSpec = tween(durationMillis = 300),
        label = "BottomNavItemScale"
    )

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                indication = null, // Removed default ripple to keep the focus on the animation
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() }
            .padding(
                horizontal = tokens.screenPadding * 0.5f,
                vertical = tokens.screenPadding * 0.25f
            )
            .scale(animatedScale), // Apply the animated scale
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = icon),
            contentDescription = label,
            // Use the animatedColor for the icon tint
            colorFilter = ColorFilter.tint(animatedColor),
            modifier = Modifier.size(tokens.iconSize * 1.45f)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            // Use the animatedColor for the text
            color = animatedColor,
            fontSize = tokens.caption,
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
    val icon: Int
)


// ── Data model ──
data class ActivityCardItem(
    val icon: Painter,
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
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreenContent(
    navController: NavHostController,
    widthSizeClass: WindowWidthSizeClass,
    onNavigate: (String) -> Unit = {}
) {
    val designTokens = getAdaptiveTokens(widthSizeClass)

    val baseDensity = LocalDensity.current
    CompositionLocalProvider(
        LocalDensity provides Density(density = baseDensity.density, fontScale = 1f)
    ) {
        HomeScreenContentBody(navController = navController, onNavigate = onNavigate, designTokens = designTokens )
    }
}
@RequiresApi(Build.VERSION_CODES.O)
@Suppress("UNUSED_PARAMETER")

@Composable
private fun HomeScreenContentBody(
    navController: NavHostController,
    onNavigate: (String) -> Unit,
    designTokens: AppDesignTokens
) {
    val dashboardViewModel: DashboardViewModel = hiltViewModel()
    val uiState by dashboardViewModel.uiState.collectAsStateWithLifecycle()

    val authViewModel: Authenticate = hiltViewModel()
    val userEntity by authViewModel.user.collectAsStateWithLifecycle()
    val adminName = userEntity?.firstName?.takeIf { it.isNotBlank() } ?: "Admin"

    // Quick Modules — static shortcuts (not part of this API response)
    val quickModules = remember {
        listOf(
            QuickModule("Contacts", R.drawable.ic_contact),
            QuickModule("Leads", R.drawable.ic_lead),
            QuickModule("Deals", R.drawable.ic_speaker),
            QuickModule("Tickets", R.drawable.ic_ticket),
            QuickModule("Email", R.drawable.ic_contact),
            QuickModule("Calendar", R.drawable.ic_contact),
            QuickModule("Orders", R.drawable.ic_contact),
            QuickModule("Reports", R.drawable.ic_contact),
            QuickModule("Customer feedback", R.drawable.ic_contact),
            QuickModule("Quotation Screen", R.drawable.ic_contact)
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
                    .background(Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red, modifier = Modifier.size(designTokens.iconSize * 2.2f))
                    Spacer(Modifier.height(designTokens.screenPadding * 0.5f))
                    Text("Failed to load dashboard", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = designTokens.bodyMedium)
                    Text("Something went wrong, Please Try again after sometime", color = Color.Gray, fontSize = designTokens.bodySmall)
                    Spacer(Modifier.height(designTokens.screenPadding * 0.75f))
                    Button(onClick = { dashboardViewModel.loadDashboard() }) {
                        Text("Retry")
                    }
                }
            }
        }

        is DashboardUiState.Success -> {
            val data = state.data
            val stats = remember(data) { mapApiStatsToUi(data.stats) }
            val customers = remember(data) { mapOperationsToCustomers(data.operations) }
            val newLeadsCount = remember(data) {
                data.leadChart.find { it.name.equals("New Enquiry", ignoreCase = true) }?.count ?: 0
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent),
                contentPadding = PaddingValues(horizontal = designTokens.screenPadding, vertical = designTokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(designTokens.screenPadding * 1.25f)
            ) {
                item {
                    GreetingCard(
                        userName = adminName,
                        newLeadsCount = newLeadsCount,
                        onNavigate = onNavigate
                    )
                }
                item { StatsGrid(stats,designTokens) }
                item {
                    QuickModulesSection(
                        modules = quickModules,
                        onNavigate = onNavigate
                    )
                }
                item {
                    RecentActivitySection(onNavigate = onNavigate)

                }
                if (customers.isNotEmpty()) {
                    item {
                        RecentCustomersSection(
                            customers = customers,
                            onNavigate = onNavigate
                        )
                    }
                }
                item { Spacer(Modifier.height(designTokens.screenPadding * 0.5f)) }
            }
        }
        else -> {
            CirculerProgressIndicatorReuse()
        }
    }
}

// ── Greeting card — solid purple banner ──
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun GreetingCard(
    userName: String,
    newLeadsCount: Int,
    onNavigate: (String) -> Unit
) {
    val tokens = LocalAppTokens.current

    val greeting = remember {
        val hour = LocalTime.now().hour
        when (hour) {
            in 5..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(tokens.cardHeight)
            .clip(RoundedCornerShape(tokens.cardCornerRadius * 1.6f))
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
                .size(tokens.iconSize * 5f)
                .offset(x = -(1).dp, y = 25.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(tokens.screenPadding),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "$greeting, $userName",
                color = whiteBg,
                fontSize = tokens.h1,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(tokens.screenPadding * 0.375f))
            Text(
                "You have $newLeadsCount new leads to review today.",
                color = Color(0xFFF8F7FF).copy(alpha = 0.85f),
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

// ── Stats grid — 2x2 cards, driven by API `stats` array ──
@Composable
private fun StatsGrid(stats: List<DashboardStat>,tokens: AppDesignTokens) {
    Column(verticalArrangement = Arrangement.spacedBy(tokens.screenPadding * 0.75f)) {
        stats.chunked(tokens.gridColumns).forEach { rowStats ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(tokens.screenPadding * 0.75f)
            ) {
                rowStats.forEach { stat ->
                    DashboardStatCard(
                        stat = stat,
                        tokens = tokens,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowStats.size < tokens.gridColumns) {
                    repeat(tokens.gridColumns - rowStats.size) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardStatCard(stat: DashboardStat, tokens: AppDesignTokens, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(Color(0xFFf8f9ff), RoundedCornerShape(tokens.cardCornerRadius * 1.6f))
            .border(1.dp, Color(0xFFe8eaf4), RoundedCornerShape(tokens.cardCornerRadius * 1.6f))
            .padding(tokens.cardPadding * 0.7f)
    ) {
        Row(Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start) {
            Image(
                painter = painterResource(id = stat.icon),
                contentDescription = null,
                colorFilter = ColorFilter.tint(stat.iconTint),
                modifier = Modifier.size(tokens.iconSize)
            )
            Spacer(Modifier.width(tokens.screenPadding * 0.3f))

            Text(
                stat.label,
                fontSize = tokens.bodySmall,
                color = statLogoBg,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(Modifier.height(tokens.screenPadding * 0.6f))
        Spacer(Modifier.height(tokens.screenPadding * 0.1f))

        Text(stat.value, fontSize = tokens.h2, color = if(stat.label=="Revenue"||stat.label=="Pending"){Color(0xFF2F27CE)}else{Color(0xFF0B1C30)}, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(tokens.screenPadding * 0.25f))
        when (stat.trendUp) {
            true -> TrendRow(icon = Icons.Default.ArrowUpward, text = stat.trendText, color = Color(0xFF16A34A), tokens = tokens)
            false -> TrendRow(icon = Icons.Default.ArrowDownward, text = stat.trendText, color = Color(0xFFEF4444), tokens = tokens)
            null -> Text(stat.trendText, fontSize = tokens.label, color = Color(0xFF9CA3AF))
        }
    }
}

@Composable
private fun TrendRow(icon: ImageVector, text: String, color: Color, tokens: AppDesignTokens) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(tokens.iconSize * 0.65f))
        Spacer(Modifier.width(2.dp))
        Text(text, fontSize = tokens.caption, color = color, fontWeight = FontWeight.SemiBold)
    }
}

// ──  — static shortcuts row ──
@Composable
private fun QuickModulesSection(
    modules: List<QuickModule>,
    onNavigate: (String) -> Unit
) {
    val tokens = LocalAppTokens.current
    Column {
        Text("Quick Modules", fontSize = tokens.h2, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
        Spacer(Modifier.height(tokens.screenPadding * 0.75f))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(tokens.screenPadding)) {
            items(modules) { module ->
                Column(
                    modifier = Modifier
                        .width(tokens.buttonHeight * 1.45f)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            val route = when (module.label) {
                                "Contacts" -> "sales_customers"
                                "Leads" -> "sales_lead"
                                "Deals" -> "sales_sales_orders"
                                "Tickets" -> null
                                "Email" -> null
                                "Calendar" -> null
                                "Orders" -> null
                                "Reports" -> null
                                "Customer feedback" -> null
                                "Sales invoice" -> null
                                "Quotation Screen" -> null
                                else -> null
                            }
                            route?.let { onNavigate(it) }
                        },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(tokens.buttonHeight * 1.27f)
                            .clip(RoundedCornerShape(tokens.cardCornerRadius))
                            .background(Color(0xFFDCE9FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon( painterResource(module.icon), contentDescription = module.label, tint = Color(0xFF2F27CE), modifier = Modifier.size(tokens.iconSize * 1.3f))
                    }
                    Spacer(Modifier.height(tokens.screenPadding * 0.375f))
                    Text(
                        module.label,
                        fontSize = tokens.caption,
                        color = blackTitle,
                        fontWeight = FontWeight.SemiBold,
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


// ── Recent Activity ──
@Composable
private fun RecentActivitySection(
    onNavigate: (String) -> Unit
) {
    val tokens = LocalAppTokens.current
    val activities = listOf(
        ActivityCardItem(
            icon = painterResource(R.drawable.ic_handshake),
            iconBg = Color(0xFFE4E7FF),
            iconTint = Color(0xFF4F46E5),
            title = "Sarah Chen closed the Global Logistics deal.",
            timeAgo = "2 hours ago",
            amount = "+$4,200"
        ),
        ActivityCardItem(
            icon = painterResource(R.drawable.person),
            iconBg = Color(0xFFE4E7FF),
            iconTint = Color(0xFF4F46E5),
            title = "New lead assigned: Alex Riviera from TechNova.",
            timeAgo = "5 hours ago",
            amount = null
        )
    )

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Recent Activity", fontSize = tokens.h2, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
            Text(
                text = "View All",
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF6C4FF6),
                modifier = Modifier.clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onNavigate("sales_sales_orders") }
            )
        }

        Spacer(Modifier.height(tokens.screenPadding * 0.875f))

        Column(verticalArrangement = Arrangement.spacedBy(tokens.screenPadding * 0.75f)) {
            activities.forEach { activity ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(modelBg, RoundedCornerShape(tokens.cardCornerRadius))
                        .border(1.dp, modelBorder, RoundedCornerShape(tokens.cardCornerRadius))
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { onNavigate("sales_sales_orders") }
                        .padding(tokens.cardPadding * 0.7f),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(tokens.buttonHeight)
                            .background(activity.iconBg, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter =  activity.icon,
                            contentDescription = null,
                            tint = activity.iconTint,
                            modifier = Modifier.size(tokens.iconSize)
                        )
                    }
                    Spacer(Modifier.width(tokens.screenPadding * 0.75f))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            activity.title,
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF111827),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = (tokens.bodyLarge.value * 1.25f).sp
                        )
                        Spacer(Modifier.height(tokens.screenPadding * 0.375f))
                        Text(activity.timeAgo, fontSize = tokens.caption, color = TextSecondary)
                    }
                    if (activity.amount != null) {
                        Spacer(Modifier.width(tokens.screenPadding * 0.5f))
                        Text(activity.amount, fontSize = tokens.bodyMedium, fontWeight = FontWeight.SemiBold, color = Color(0xFF16A34A))
                    }
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
    val tokens = LocalAppTokens.current
    Column {
        Text("Recent Customers", fontSize = tokens.h2, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
        Spacer(Modifier.height(tokens.screenPadding * 0.75f))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(modelBg, RoundedCornerShape(tokens.cardCornerRadius))
                .border(1.dp, modelBorder, RoundedCornerShape(tokens.cardCornerRadius))
        ) {
            customers.forEachIndexed { index, customer ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { onNavigate("sales_customers") }
                        .padding(
                            horizontal = tokens.cardPadding * 0.7f,
                            vertical = tokens.cardPadding * 0.6f
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(tokens.buttonHeight)
                            .clip(CircleShape)
                            .background(customer.avatarColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(customer.initials, color = customer.avatarColor, fontWeight = FontWeight.Bold, fontSize = tokens.bodySmall)
                    }
                    Spacer(Modifier.width(tokens.screenPadding * 0.75f))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(customer.name, fontSize = tokens.bodyMedium, fontWeight = FontWeight.SemiBold, color = Color(0xFF111827))
                        Text(customer.role, fontSize = tokens.caption, color = Color(0xFF9CA3AF))
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(tokens.iconSize * 1.4f))
                }

                if (index != customers.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = tokens.cardPadding * 0.7f, end = tokens.cardPadding * 0.7f),
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

fun formatLeadDate(raw: String?): String {
    if (raw.isNullOrBlank()) return "—"
    return try {
        val datePart = raw.take(10)
        val parts = datePart.split("-")
        if (parts.size == 3) {
            val year = parts[0]
            val month = parts[1].toIntOrNull()
            val day = parts[2]

            val monthNames = listOf(
                "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
            )
            val monthName = month?.let { if (it in 1..12) monthNames[it - 1] else null }

            if (monthName != null) "$day $monthName $year" else raw
        } else raw
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
    val tokens = LocalAppTokens.current
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
            .background(whiteBg, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
            .border(1.dp, PrimaryBorder, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
            .clickable { showPicker = true }
            .padding(
                horizontal = tokens.cardPadding * 0.6f,
                vertical = tokens.screenPadding * 0.375f
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = value.ifEmpty { "Select Time" },
            fontSize = tokens.bodyMedium,
            color = if (value.isEmpty()) Color(0xFF9CA3AF) else Color(0xFF374151)
        )
        Icon(
            Icons.Default.Schedule,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(tokens.iconSize)
        )
    }

    if (showPicker) {
        AlertDialog(
            onDismissRequest = { showPicker = false },
            containerColor = whiteBg,
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            title = {
                Text(
                    "Appointment Time",
                    fontSize = tokens.h2,
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
    val tokens = LocalAppTokens.current
    val itemHeight = tokens.fieldHeight
    val wheelHeight = itemHeight * 4.5f
    val rowHeight = itemHeight * 6.3f

    val hourOptions = (1..12).toList()
    val minuteOptions = (0..59).map { String.format("%02d", it) }

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

    LaunchedEffect(minuteCenterIndex) {
        if (minuteCenterIndex in 0 until totalMinuteItems) {
            onMinuteChange(minuteOptions[minuteCenterIndex % minuteOptions.size].toInt())
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(rowHeight)
            .padding(vertical = tokens.screenPadding * 0.5f),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Hour", fontSize = tokens.caption, color = LeadmutedText, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(tokens.screenPadding * 0.25f))

            Box(modifier = Modifier
                .fillMaxWidth()
                .height(wheelHeight)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight)
                        .align(Alignment.Center)
                        .background(
                            LeadPrimarySoft,
                            RoundedCornerShape(tokens.cardCornerRadius * 0.5f)
                        )
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
                    items(totalHourItems) { i ->
                        val h = hourOptions[i % hourOptions.size]
                        val isSelected = i == hourCenterIndex
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(itemHeight)
                                .padding(horizontal = tokens.screenPadding * 0.5f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = String.format("%02d", h),
                                fontSize = if (isSelected) tokens.h1 else tokens.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) LeadPrimary else Color(0xFF6B7280)
                            )
                        }
                    }
                }
            }
        }

        Text(":", fontSize = tokens.h1, fontWeight = FontWeight.Bold, color = Color(0xFF111827), modifier = Modifier.padding(horizontal = tokens.screenPadding * 0.25f))

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Minute", fontSize = tokens.caption, color = LeadmutedText, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(tokens.screenPadding * 0.25f))

            Box(modifier = Modifier
                .fillMaxWidth()
                .height(wheelHeight)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight)
                        .align(Alignment.Center)
                        .background(
                            LeadPrimarySoft,
                            RoundedCornerShape(tokens.cardCornerRadius * 0.5f)
                        )
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
                    items(totalMinuteItems) { i ->
                        val m = minuteOptions[i % minuteOptions.size]
                        val isSelected = i == minuteCenterIndex
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(itemHeight)
                                .padding(horizontal = tokens.screenPadding * 0.5f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = m,
                                fontSize = if (isSelected) tokens.h1 else tokens.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) LeadPrimary else Color(0xFF6B7280)
                            )
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(0.8f)
                .padding(start = tokens.screenPadding * 0.5f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("AM/PM", fontSize = tokens.caption, color = LeadmutedText, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(tokens.screenPadding * 0.25f))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(wheelHeight)
                    .padding(vertical = tokens.screenPadding * 1.25f),
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight)
                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                        .background(if (isAm) LeadPrimary else Color.Transparent)
                        .clickable { onAmPmChange(true) }
                        .padding(tokens.screenPadding * 0.5f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("AM", fontSize = tokens.bodyMedium, fontWeight = if (isAm) FontWeight.Bold else FontWeight.Normal, color = if (isAm) whiteBg else Color(0xFF6B7280))
                }

                Spacer(Modifier.height(tokens.screenPadding * 0.5f))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight)
                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                        .background(if (!isAm) LeadPrimary else Color.Transparent)
                        .clickable { onAmPmChange(false) }
                        .padding(tokens.screenPadding * 0.5f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("PM", fontSize = tokens.bodyMedium, fontWeight = if (!isAm) FontWeight.Bold else FontWeight.Normal, color = if (!isAm) whiteBg else Color(0xFF6B7280))
                }
            }
        }
    }
}

@Composable
fun FormLabel(text: String?, isRequired: Boolean = false) {
    val tokens = LocalAppTokens.current
    Row {
        Text(
            text ?: "",
            fontSize = tokens.bodySmall,
            fontWeight = FontWeight.Medium,
            color = Color.Gray
        )
        if (isRequired) {
            Text(
                text = " *",
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.Medium,
                color = Color.Red
            )
        }
    }
    Spacer(Modifier.height(tokens.screenPadding * 0.375f))
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
    enabled: Boolean = true,
    // ── NEW: needed for PAN (uppercase) / Aadhaar (spaced grouping) ──
    keyboardCapitalization: androidx.compose.ui.text.input.KeyboardCapitalization =
        androidx.compose.ui.text.input.KeyboardCapitalization.None,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation =
        androidx.compose.ui.text.input.VisualTransformation.None
) {
    val tokens = LocalAppTokens.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(tokens.fieldHeight)
            .background(
                if (enabled) whiteBg else Color(0xFFF3F4F6),
                RoundedCornerShape(tokens.cardCornerRadius * 0.5f)
            )
            .border(
                1.dp,
                if (isError) Color(0xFFEF4444) else Color(0xFFE5E7EB),
                RoundedCornerShape(tokens.cardCornerRadius * 0.5f)
            )
            .padding(horizontal = tokens.cardPadding * 0.6f),
        contentAlignment = Alignment.CenterStart
    ) {
        if (value.isEmpty() && placeholder.isNotEmpty()) {
            Text(placeholder, fontSize = tokens.bodyMedium, color = Color(0xFF9CA3AF))
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                capitalization = keyboardCapitalization
            ),
            visualTransformation = visualTransformation,
            textStyle = TextStyle(
                fontSize = tokens.bodyMedium,
                color = if (enabled) Color(0xFF374151) else Color(0xFF6B7280)
            )
        )
    }
    if (isError && !errorMessage.isNullOrBlank()) {
        Text(
            errorMessage,
            fontSize = tokens.label,
            color = Color(0xFFEF4444),
            modifier = Modifier.padding(top = tokens.screenPadding * 0.25f, start = tokens.screenPadding * 0.25f)
        )
    }
}


@Suppress("UNUSED_PARAMETER")
@Composable
fun FormDropdown(
    label: String? = null,
    value: String,
    expanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    isRequired: Boolean = false,
    enabled: Boolean = true,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    val tokens = LocalAppTokens.current
    if (!label.isNullOrEmpty()) {
        FormLabel(label, isRequired)
    } else {
        Spacer(Modifier.height(tokens.screenPadding * 0.375f))
    }

    val density = LocalDensity.current
    var triggerWidthPx by remember { mutableIntStateOf(0) }

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates -> triggerWidthPx = coordinates.size.width }
                .height(tokens.fieldHeight)
                .background(
                    if (enabled) whiteBg else Color(0xFFF3F4F6),
                    RoundedCornerShape(tokens.cardCornerRadius * 0.5f)
                )
                .border(
                    1.dp,
                    if (isError) Color(0xFFEF4444) else Color(0xFFE5E7EB),
                    RoundedCornerShape(tokens.cardCornerRadius * 0.5f)
                )
                .clickable(enabled = enabled) { onExpandChange(true) }
                .padding(horizontal = tokens.cardPadding * 0.6f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                value,
                fontSize = tokens.bodySmall,
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
                containerColor = whiteBg,
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                modifier = Modifier
                    .width(with(density) { triggerWidthPx.toDp() })
                    .heightIn(max = tokens.fieldHeight * 4.5f)
            ) {
                options.forEach { option ->
                    Text(
                        text = option,
                        fontSize = tokens.bodyMedium,
                        color = Color(0xFF374151),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onOptionSelected(option)
                                onExpandChange(false)
                            }
                            .padding(
                                horizontal = tokens.cardPadding * 0.6f,
                                vertical = tokens.screenPadding * 0.5f
                            )
                    )
                }
            }
        }
    }
}

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
        "inventory_orders",
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
        "reports_sales"             -> "reports_sales"
        "reports_marketing"         -> "reports_marketing"
        "reports_inventory"         -> "reports_inventory"
        "reports_reports_inventory" -> "reports_inventory" // Handling prefix from ModulesPanel
        "reports_finance"           -> "reports_finance"
        "reports_reports_finance"   -> "reports_finance" // Handling prefix from ModulesPanel
        "reports_human_resource"    -> "reports_human_resource"
        "reports_logistics"         -> "reports_logistics"
        "reports_it"                -> "reports_it"
        "reports_legal"             -> "reports_legal"



        else -> rawKey
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
    screen.startsWith("reports_") -> "Reports"

    else -> "Home"
}

fun String.toIsoDate(): String {
    if (this.isEmpty() || this == " ") return ""
    return try {
        val parts = this.split("-")
        if (parts.size == 3) "${parts[2]}-${parts[1]}-${parts[0]}T00:00:00.000Z" else ""
    } catch (_: Exception) { "" }
}