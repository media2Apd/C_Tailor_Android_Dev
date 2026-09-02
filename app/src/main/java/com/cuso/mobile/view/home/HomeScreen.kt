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
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.cuso.mobile.R
import com.cuso.mobile.adaptive_screen.AppDesignTokens
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.adaptive_screen.getAdaptiveTokens
import com.cuso.mobile.database.entities.SalesStatusEntity
import com.cuso.mobile.model.sales.DashboardStatDto
import com.cuso.mobile.model.sales.OperationItem
import com.cuso.mobile.model.login_forgotPassword_resetPassword.Organization
import com.cuso.mobile.model.login_forgotPassword_resetPassword.Settings
import com.cuso.mobile.model.login_forgotPassword_resetPassword.Subscription
import com.cuso.mobile.model.login_forgotPassword_resetPassword.User
import com.cuso.mobile.model.sales.CategoryItem
import com.cuso.mobile.model.sales.CustomerItem
import com.cuso.mobile.model.sales.StaffDto
import com.cuso.mobile.ui.theme.Primary
import com.cuso.mobile.ui.theme.Primary_background
import com.cuso.mobile.ui.theme.TextSecondary
import com.cuso.mobile.ui.theme.blackTitle
import com.cuso.mobile.ui.theme.light_blue_border
import com.cuso.mobile.ui.theme.modelBg
import com.cuso.mobile.ui.theme.modelBorder
import com.cuso.mobile.ui.theme.mutedText
import com.cuso.mobile.ui.theme.redText
import com.cuso.mobile.ui.theme.statLogoBg
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.CirculerProgressIndicatorReuse
import com.cuso.mobile.view.composable.DashboardSkeleton
import com.cuso.mobile.view.composable.DynamicIslandSuccess
import com.cuso.mobile.view.composable.ExitAppDialog
import com.cuso.mobile.view.composable.FilterOption
import com.cuso.mobile.view.composable.FilterSection
import com.cuso.mobile.view.composable.FilterSectionType
import com.cuso.mobile.view.composable.blurScrim
import com.cuso.mobile.view.home.finance.account_payable.purchase_invoices.PurchaseInvoiceItem
import com.cuso.mobile.view.home.finance.account_payable.suppliers.SupplierRow
import com.cuso.mobile.view.home.hr.employees.ScreenMode
import com.cuso.mobile.view.home.inventory.procurement.orders.LowStockItem
import com.cuso.mobile.view.home.sales.sales_order.OrderReviewData
import com.cuso.mobile.view.home.sales.sales_order.toOrderReviewData
import com.cuso.mobile.view.home.sidebar.FullSideBar
import com.cuso.mobile.view.home.sidebar.ModulesPanel
import com.cuso.mobile.view.home.sidebar.QuickAccessPanel
import com.cuso.mobile.view.home.sidebar.SalesSideBar
import com.cuso.mobile.view.home.sidebar.SidebarConfig
import com.cuso.mobile.view.home.sidebar.buildNavigationKey
import com.cuso.mobile.viewmodel.Authenticate
import com.cuso.mobile.viewmodel.CustomerDeleteState
import com.cuso.mobile.viewmodel.CustomerViewModel
import com.cuso.mobile.viewmodel.DashboardUiState
import com.cuso.mobile.viewmodel.DashboardViewModel
import com.cuso.mobile.viewmodel.HomeViewModel
import com.cuso.mobile.viewmodel.HrViewModel
import com.cuso.mobile.viewmodel.OrderOverviewState
import com.cuso.mobile.viewmodel.OrderOverviewViewModel
import com.cuso.mobile.viewmodel.ProfileViewModel
import com.cuso.mobile.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch
import java.time.LocalTime

val LeadPrimary = Primary
val LeadPrimarySoft = light_blue_border
val LeadmutedText = mutedText

@SuppressLint("UnrememberedGetBackStackEntry", "SuspiciousIndentation")
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun HomeScreen(navController: NavHostController, widthSizeClass: WindowWidthSizeClass) {
    val viewModel: HomeViewModel = hiltViewModel()
    val authViewModel: Authenticate = hiltViewModel()
    val hrViewModel: HrViewModel = hiltViewModel()
    val customerViewModel: CustomerViewModel = hiltViewModel()
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val profileViewModel: ProfileViewModel = hiltViewModel()
    val orderOverviewViewModel: OrderOverviewViewModel = hiltViewModel()

    val authTokens by authViewModel.tokens.collectAsStateWithLifecycle()
    val token: String = authTokens?.accessToken.orEmpty()
    val isLoggedOut: Boolean by viewModel.isLoggedOut.collectAsStateWithLifecycle(initialValue = false)
    val screenStack = remember { mutableStateListOf("home") }
    val currentScreen: String = screenStack.last()
    val tokens = getAdaptiveTokens(widthSizeClass)

    fun navigateTo(screen: String) {
        if (screenStack.lastOrNull() != screen) screenStack.add(screen)
    }

    var lastBackPressTime by remember { mutableLongStateOf(0L) }

    fun goBack() {
        val now = System.currentTimeMillis()
        if (now - lastBackPressTime < 300) return // ignore duplicate back within 300ms
        lastBackPressTime = now
        if (screenStack.size > 1) screenStack.removeAt(screenStack.lastIndex)
    }
    fun resetToHome() {
        screenStack.clear()
        screenStack.add("home")
    }

    var previousStackSize by remember { mutableIntStateOf(screenStack.size) }
    val isForwardNavigation = screenStack.size >= previousStackSize
    LaunchedEffect(screenStack.size) {
        previousStackSize = screenStack.size
    }

    var isDrawerOpen by remember { mutableStateOf(false) }
    var sidebarBlur by remember { mutableStateOf(0.dp) }
    var pendingOrderReviewData by remember { mutableStateOf<OrderReviewData?>(null) }
    var orderFlowOrigin by remember { mutableStateOf<String?>(null) }

    val customerUiState by customerViewModel.uiState.collectAsStateWithLifecycle()
    var selectedCustomer by remember { mutableStateOf<CustomerItem?>(null) }
    var selectedOrderId by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val activity = context as ComponentActivity

    // Finance State
    var selectedLedgerAccountId by remember { mutableStateOf<String?>(null) }
    var selectedLedgerAccountName by remember { mutableStateOf("Ledger") }
    var selectedPaymentModeId by remember { mutableStateOf<String?>(null) }
    var selectedInvoiceId by remember { mutableStateOf<String?>(null) }
    var selectedPurchaseInvoice by remember { mutableStateOf<PurchaseInvoiceItem?>(null) }
    var selectedSupplier by remember { mutableStateOf<SupplierRow?>(null) }

    // Inventory State
    var selectedInventoryItemId by remember { mutableStateOf<String?>(null) }
    var selectedLowStockItem by remember { mutableStateOf<LowStockItem?>(null) }
    var selectedItemGroupId by remember { mutableStateOf<String?>(null) }

    // HR State
    var employeeScreenMode by remember { mutableStateOf(ScreenMode.CREATE) }
    var selectedEmployeeId by remember { mutableStateOf<String?>(null) }
    var selectedAttendanceId by remember { mutableStateOf<String?>(null) }

    // Services State
    var selectedFeedbackId by remember { mutableStateOf<String?>(null) }

    // Dashboard recent-customer navigation state
    var selectedRecentCustomerId by remember { mutableStateOf<String?>(null) }

    // Sales State
    var isSalesSettingsMode by remember { mutableStateOf(false) }
    var selectedManagementOrderId by remember { mutableStateOf<String?>(null) }
    var editOrderId by remember { mutableStateOf<String?>(null) }
    var editingPricingId by remember { mutableStateOf<String?>(null) }
    var quotationScreenMode by remember { mutableStateOf("create") }

    // Panels and Feedback State
    var showModulesPanel by remember { mutableStateOf(false) }
    var modulesPanelInitialExpanded by remember { mutableStateOf<String?>(null) }
    var showQuickAccessPanel by remember { mutableStateOf(false) }
    var quickAccessBlur by remember { mutableStateOf(0.dp) }
    var comingSoonMessage by remember { mutableStateOf<String?>(null) }
    var showExitDialog by remember { mutableStateOf(false) }

    val deleteState by customerViewModel.deleteState.collectAsState()
    val editOverviewState by orderOverviewViewModel.overviewState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

//    val implementedRoutes = remember {
//        setOf(
//            // ── Home & Settings ──
//            "home",
//            "settings",
//            "profile-settings",
//            "home_organization_profile",
//            "home_branch_management",
//            "home_department_teams",
//            "home_designation",
////            "home_role_management",
////            "home_warehouse_management",
////            "home_opening_balance",
//
//            // ── Sales ──
//            "sales_lead",
//            "create_lead",
//            "view_lead",
//            "edit_lead",
//            "sales_customers",
//            "create_customer",
//            "view_customer",
//            "edit_customer",
//            "view_customer_recent",
//            "sales_measurements",
//            "sales_sales_orders",
//            "create_order",
//            "order_overview",
//            "create_order_review",
//            "sales_orders",
//            "order_management_overview",
//            "sales_pricing_overview",
//            "create_garment_pricing",
//            "garment_pricing_list",
//            "sales_pricing_quotation",
//            "create_quotation",
////            "sales_payment_and_billing",
////            "payment_detail",
//            "sales_settings",
//            "sales_garment_type",
//
//            // ── Finance ──
//            "finance_sales_invoices",
//            "finance_invoice_detail",
////            "finance_purchase_invoices",
////            "finance_purchase_invoice_detail",
//            "finance_customers",
////            "finance_suppliers",
////            "finance_supplier_detail",
//            "finance_expenses",
//            "finance_chart_of_accounts",
//            "finance_journal_screen",
//            "finance_trial_balance",
//            "finance_ledger",
////            "finance_payments_received",
////            "payment_detail_screen",
////            "finance_payments_mode",
////            "payment_mode_detail",
//
//            // ── Inventory ──
//            "inventory_items",
//            "inventory_create_item",
//            "inventory_item_detail",
////            "inventory_low_stock_alerts",
////            "inventory_create_purchase_order",
////            "inventory_item_groups",
////            "inventory_create_item_group",
//
//            // ── HR ──
//            "hr_all_employees",
//            "hr_employee_onboarding",
////            "hr_attendance",
////            "hr_attendance_detail",
//
//            // ── Logistics ──
////            "logistics_delivery",
////            "delivery_detail",
////            "logistics_order_tracking",
////            "tracking_overview",
//
//            // ── Services ──
////            "services_customer_feedback",
////            "feedback_detail",
////            "services_alteration_management",
////            "create_alteration",
////            "services_service_request",
////            "create_request",
////            "review_services",
//
//            // ── Reports ──
////            "reports_sales",
////            "reports_inventory",
////            "reports_inventory_stock_summary",
////            "reports_inventory_low_stock",
////            "reports_inventory_warehouse_report",
////            "reports_inventory_purchase_report",
////            "reports_inventory_dead_stock",
////            "reports_finance",
////            "reports_finance_profit_and_loss_report"
//        )
//    }
    val implementedRoutes = remember {
        setOf(
            // ── Home & Settings ──
            "home",
            "settings",
            "profile-settings",
            "settings_overview",
            "module_settings",
            "home_organization_profile",
            "home_branch_management",
            "home_department_teams",
            "home_designation",
            "home_role_management",
            "home_warehouse_management",
            "home_opening_balance",
            "sales_garment_type",
            "sales_garment_pricing_setup",
            "sales_add_garment_pricing",
            "sales_add_fabric_pricing",
            "sales_add_work_pricing",

            // ── Sales ──
            "sales_lead",
            "create_lead",
            "view_lead",
            "edit_lead",
            "sales_customers",
            "create_customer",
            "view_customer",
            "edit_customer",
            "view_customer_recent",
            "sales_measurements",
            "sales_sales_orders",
            "create_order",
            "order_overview",
            "create_order_review",
            "sales_orders",
            "order_management_overview",
            "sales_pricing_overview",
            "create_garment_pricing",
            "garment_pricing_list",
            "sales_pricing_quotation",
            "create_quotation",
            "measurement_entry",
            "order_preview",
            "sales_payment_and_billing",
            "payment_detail",
            "sales_settings",
            "sales_garment_type",

            // ── Finance ──
            "finance_sales_invoices",
            "finance_invoice_detail",
            "finance_purchase_invoices",
            "finance_purchase_invoice_detail",
            "finance_customers",
            "finance_suppliers",
            "finance_supplier_detail",
            "finance_expenses",
            "finance_chart_of_accounts",
            "finance_journal_screen",
            "finance_trial_balance",
            "finance_ledger",
            "finance_payments_received",
            "payment_detail_screen",
            "finance_payments_mode",
            "payment_mode_detail",

            // ── Inventory ──
            "inventory_items",
            "inventory_create_item",
            "inventory_item_detail",
            "inventory_low_stock_alerts",
            "inventory_create_purchase_order",
            "inventory_item_groups",
            "inventory_create_item_group",

            // ── HR ──
            "hr_all_employees",
            "hr_employee_onboarding",
            "hr_attendance",
            "hr_attendance_detail",

            // ── Logistics ──
            "logistics_delivery",
            "delivery_detail",
            "logistics_order_tracking",
            "tracking_overview",

            // ── Services ──
            "services_service_status",
            "services_delay_rework",
            "services_service_delivery",
            "service_status_detail",
            "services_service_orders",
            "services_service_order",
            "service_order_overview",
            "services_customer_feedback",
            "feedback_detail",
            "services_alteration_management",
            "create_alteration",
            "services_service_request",
            "create_request",
            "review_services",

            // ── Reports ──
            "reports_sales",
            "reports_inventory",
            "reports_inventory_stock_summary",
            "reports_inventory_low_stock",
            "reports_inventory_warehouse_report",
            "reports_inventory_purchase_report",
            "reports_inventory_dead_stock",
            "reports_finance",
            "reports_finance_profit_and_loss_report"
        )
    }

    fun safeNavigate(route: String) {
        val navKey = normalizeRoute(route)
        if (navKey in implementedRoutes) {
            isSalesSettingsMode = false
            navigateTo(navKey)
        } else {
            comingSoonMessage = "Coming Soon, Stay tuned !"
        }
    }

    val homeBackStackEntry = remember { navController.getBackStackEntry("home") }
    val pendingOrderIdFlow = homeBackStackEntry.savedStateHandle.getStateFlow<String?>("pendingOrderId", null)
    val pendingOrderId by pendingOrderIdFlow.collectAsStateWithLifecycle()

    LaunchedEffect(pendingOrderId) {
        val id = pendingOrderId
        if (id != null) {
            selectedOrderId = id
            navigateTo("order_overview")
            homeBackStackEntry.savedStateHandle["pendingOrderId"] = null
        }
    }

    LaunchedEffect(editOrderId) {
        editOrderId?.let { orderOverviewViewModel.fetchSalesOverview(it) }
    }

    LaunchedEffect(Unit) {
        profileViewModel.loadOrganization("")
    }

    LaunchedEffect(token) {
        if (token.isNotEmpty()) {
            profileViewModel.loadOrganization(token)
        }
    }

    LaunchedEffect(editOverviewState) {
        if (editOrderId == null) return@LaunchedEffect
        when (val s = editOverviewState) {
            is OrderOverviewState.Success -> {
                pendingOrderReviewData = s.data.toOrderReviewData()
                navigateTo("create_order")
                editOrderId = null
            }
            is OrderOverviewState.Error -> {
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
            currentScreen == "home_designation" ||
            currentScreen == "home_role_management" ||
            currentScreen == "home_warehouse_management" ||
            currentScreen == "home_opening_balance"

    val showSalesPanel = isSalesSettingsMode

    // ── 1. MODULARIZED BACK HANDLER ──
    HomeBackHandler(
        screenStackSize = screenStack.size,
        currentScreen = currentScreen,
        isDrawerOpen = isDrawerOpen,
        showModulesPanel = showModulesPanel,
        showQuickAccessPanel = showQuickAccessPanel,
        onCloseDrawer = { isDrawerOpen = false },
        onCloseModulesPanel = { showModulesPanel = false },
        onCloseQuickAccessPanel = { showQuickAccessPanel = false },
        onSetSalesSettingsMode = { isSalesSettingsMode = it },
        onClearStateForScreen = { scr ->
            when (scr) {
                "create_order_review", "create_order" -> pendingOrderReviewData = null
                "finance_invoice_detail" -> selectedInvoiceId = null
                "finance_ledger" -> selectedLedgerAccountId = null
                "finance_supplier_detail" -> selectedSupplier = null
                "inventory_item_detail" -> selectedInventoryItemId = null
                "inventory_create_purchase_order" -> selectedLowStockItem = null
                "inventory_create_item_group" -> selectedItemGroupId = null
                "hr_employee_onboarding" -> selectedEmployeeId = null
                "hr_attendance_detail" -> selectedAttendanceId = null
                "feedback_detail" -> selectedFeedbackId = null
                "view_customer", "edit_customer" -> selectedCustomer = null
                "view_customer_recent" -> selectedRecentCustomerId = null
                "order_overview", "service_order_overview" -> selectedOrderId = null
                "order_management_overview" -> selectedManagementOrderId = null
                "payment_mode_detail" -> selectedPaymentModeId = null
            }
        },
        onGoBack = { goBack() }
    )

    // At the root "home" screen with no drawer or panel open, back gesture
    // shows an exit confirmation instead of doing nothing
    BackHandler(
        enabled = currentScreen == "home" &&
                screenStack.size <= 1 &&
                !isDrawerOpen &&
                !showModulesPanel &&
                !showQuickAccessPanel
    ) {
        showExitDialog = true
    }

    ExitAppDialog(
        show = showExitDialog,
        onDismiss = { showExitDialog = false }
    )

    Box(modifier = Modifier.fillMaxSize()) {
        CompositionLocalProvider(LocalAppTokens provides tokens) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = Primary_background,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                topBar = {
                    TopBar(
                        onProfileClick = { navigateTo("profile-settings") }
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
                                "home" -> {
                                    isSalesSettingsMode = false
                                    resetToHome()
                                    isDrawerOpen = false
                                }
                                "settings" -> {
                                    navigateTo("settings")
                                    isDrawerOpen = false
                                }
                                "home_organization_profile",
                                "home_branch_management",
                                "home_department_teams",
                                "home_designation",
                                "home_role_management",
                                "home_warehouse_management",
                                "home_opening_balance" -> {
                                    navigateTo(route)
                                    isDrawerOpen = false
                                }
                                "sales_garment_type" -> {
                                    navigateTo("sales_garment_type")
                                    isDrawerOpen = false
                                }
                                else -> {
                                    safeNavigate(route)
                                    isDrawerOpen = false
                                }
                            }
                        },
                        onModulesClick = {
                            modulesPanelInitialExpanded = menuForScreen(currentScreen)
                            showModulesPanel = true
                        },
                        onAddClick = { showQuickAccessPanel = true },
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
                        .blurScrim(sidebarBlur)
                ) {
                    AnimatedContent(
                        targetState = currentScreen,
                        modifier = Modifier.fillMaxSize(),
                        transitionSpec = {
                            val slideSpec = tween<androidx.compose.ui.unit.IntOffset>(
                                durationMillis = 320,
                                easing = FastOutSlowInEasing
                            )
                            val fadeInSpec = tween<Float>(
                                durationMillis = 220,
                                easing = LinearOutSlowInEasing
                            )
                            val fadeOutSpec = tween<Float>(
                                durationMillis = 180,
                                easing = FastOutLinearInEasing
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
                        // ── 2. MODULARIZED SCREEN ROUTER ──
                        HomeScreenRouter(
                            screen = screen,
                            navController = navController,
                            widthSizeClass = widthSizeClass,
                            token = token,
                            hrViewModel = hrViewModel,
                            customerViewModel = customerViewModel,
                            settingsViewModel = settingsViewModel,
                            authViewModel = authViewModel,
                            onNavigate = { navigateTo(it) },
                            onSafeNavigate = { safeNavigate(it) },
                            onGoBack = { goBack() },
                            onOpenDrawer = { isDrawerOpen = true },
                            onOpenModulesPanel = {
                                modulesPanelInitialExpanded = it
                                showModulesPanel = true
                            },
                            onShowComingSoon = { comingSoonMessage = it },
                            onSalesSettingsModeChange = { isSalesSettingsMode = it },
                            selectedCustomer = selectedCustomer,
                            onCustomerSelected = { selectedCustomer = it },
                            selectedOrderId = selectedOrderId,
                            onOrderIdSelected = { selectedOrderId = it },
                            editOrderId = editOrderId,
                            onEditOrderIdChange = { editOrderId = it },
                            selectedManagementOrderId = selectedManagementOrderId,
                            onManagementOrderIdSelected = { selectedManagementOrderId = it },
                            selectedLedgerAccountId = selectedLedgerAccountId,
                            selectedLedgerAccountName = selectedLedgerAccountName,
                            onLedgerAccountSelected = { id, name ->
                                selectedLedgerAccountId = id
                                selectedLedgerAccountName = name
                            },
                            selectedSupplier = selectedSupplier,
                            onSupplierSelected = { selectedSupplier = it },
                            selectedPaymentModeId = selectedPaymentModeId,
                            onPaymentModeSelected = { selectedPaymentModeId = it },
                            selectedInvoiceId = selectedInvoiceId,
                            onInvoiceSelected = { selectedInvoiceId = it },
                            selectedPurchaseInvoice = selectedPurchaseInvoice,
                            onPurchaseInvoiceSelected = { selectedPurchaseInvoice = it },
                            selectedInventoryItemId = selectedInventoryItemId,
                            onInventoryItemIdSelected = { selectedInventoryItemId = it },
                            selectedLowStockItem = selectedLowStockItem,
                            onLowStockItemSelected = { selectedLowStockItem = it },
                            selectedItemGroupId = selectedItemGroupId,
                            onItemGroupIdSelected = { selectedItemGroupId = it },
                            employeeScreenMode = employeeScreenMode,
                            onEmployeeScreenModeChange = { employeeScreenMode = it },
                            selectedEmployeeId = selectedEmployeeId,
                            onEmployeeIdSelected = { selectedEmployeeId = it },
                            selectedAttendanceId = selectedAttendanceId,
                            onAttendanceIdSelected = { selectedAttendanceId = it },
                            selectedFeedbackId = selectedFeedbackId,
                            onFeedbackIdSelected = { selectedFeedbackId = it },
                            selectedRecentCustomerId = selectedRecentCustomerId,
                            onRecentCustomerIdSelected = { selectedRecentCustomerId = it },
                            editingPricingId = editingPricingId,
                            onEditingPricingIdChange = { editingPricingId = it },
                            quotationScreenMode = quotationScreenMode,
                            onQuotationScreenModeChange = { quotationScreenMode = it },
                            pendingOrderReviewData = pendingOrderReviewData,
                            onPendingOrderReviewDataChange = { pendingOrderReviewData = it },
                            onOrderFlowOriginChange = { orderFlowOrigin = it },
                            onOrderSavedSuccessfully = { savedOrderId ->
                                screenStack.removeAll {
                                    it in setOf(
                                        "sales_lead", "create_lead", "view_lead", "edit_lead",
                                        "create_order", "create_order_review", "sales_sales_orders"
                                    )
                                }
                                if (savedOrderId != null) {
                                    selectedOrderId = savedOrderId
                                    navigateTo("order_overview")
                                } else {
                                    navigateTo("sales_sales_orders")
                                }
                            }
                        )
                    }
                }
            }
        }

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
                showModulesPanel = false
                safeNavigate(rawNavKey)
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
                    "quick_add" -> { }
                    else -> {
                        isSalesSettingsMode = false
                        navigateTo(route)
                    }
                }
            },
            onBlurScrimChange = { radius, _ -> quickAccessBlur = radius }
        )

        DynamicIslandSuccess(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 100.dp),
            message = comingSoonMessage,
            onDismiss = { comingSoonMessage = null }
        )
    }
}

// ─────────────────────────────────────────────────────────────
// TopBar & BottomBar UI implementations remain below
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
    val authViewModel: Authenticate = hiltViewModel(LocalContext.current as ComponentActivity)
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.height(tokens.buttonHeight * 1.25f)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.cuso_tailor_logo),
                    contentDescription = "Logo",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(tokens.buttonHeight * 2f)
                )
            }

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
                                .background(redText)
                                .border(1.5.dp, Color(0xFFF5F5FA), CircleShape)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(tokens.screenPadding * 0.9f))

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
    onAddClick: () -> Unit = {},
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
                defaultSelectedMenu = "Home"
            )
        }

        val bottomBarShape = RoundedCornerShape(
            topStart = tokens.cardCornerRadius * 1.6f,
            topEnd = tokens.cardCornerRadius * 1.6f,
            bottomStart = 0.dp,
            bottomEnd = 0.dp
        )

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
                        elevation = 12.dp,
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
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BottomNavItem(
                            icon = R.drawable.home,
                            label = "Home",
                            isSelected = currentScreen == "home",
                            selectedColor = Primary,
                            onClick = { onMenuItemClick("home") }
                        )

                        BottomNavItem(
                            icon = R.drawable.orders,
                            label = "Orders",
                            isSelected = currentScreen == "sales_sales_orders",
                            selectedColor = Primary,
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
                            selectedColor = Primary,
                            onClick = { onMenuItemClick("reports_sales") }
                        )

                        BottomNavItem(
                            icon = R.drawable.modules,
                            label = "Modules",
                            isSelected = currentScreen == "modules",
                            selectedColor = Primary,
                            onClick = { onModulesClick() }
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-22).dp)
                    .size(tokens.buttonHeight * 1.45f)
                    .shadow(10.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Primary)
                    .clickable { onAddClick() },
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

@Composable
fun BottomNavItem(
    icon: Int,
    label: String,
    isSelected: Boolean,
    selectedColor: Color = Primary,
    onClick: () -> Unit
) {
    val tokens = LocalAppTokens.current

    val animatedColor by animateColorAsState(
        targetValue = if (isSelected) selectedColor else Color(0xFF9CA3AF),
        animationSpec = tween(durationMillis = 300),
        label = "BottomNavItemColor"
    )

    val animatedScale by animateFloatAsState(
        targetValue = if (isSelected) 1.12f else 1.0f,
        animationSpec = tween(durationMillis = 300),
        label = "BottomNavItemScale"
    )

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() }
            .padding(
                horizontal = tokens.screenPadding * 0.5f,
                vertical = tokens.screenPadding * 0.25f
            )
            .scale(animatedScale),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = icon),
            contentDescription = label,
            colorFilter = ColorFilter.tint(animatedColor),
            modifier = Modifier.size(tokens.iconSize * 1.45f)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            color = animatedColor,
            fontSize = tokens.caption,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

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

data class ActivityCardItem(
    val icon: Painter,
    val iconBg: Color,
    val iconTint: Color,
    val title: String,
    val timeAgo: String,
    val amount: String? = null
)

private data class RecentCustomer(
    val id: String = "",
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
            id = op.customerId,
            name = prettyName.ifBlank { op.customer },
            role = op.type,
            initials = initials,
            avatarColor = CustomerAvatarPalette[index % CustomerAvatarPalette.size]
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreenContent(
    navController: NavHostController,
    widthSizeClass: WindowWidthSizeClass,
    onNavigate: (String) -> Unit = {},
    onCustomerClick: (String) -> Unit = {}
) {
    val designTokens = getAdaptiveTokens(widthSizeClass)
    val baseDensity = LocalDensity.current
    CompositionLocalProvider(
        LocalDensity provides Density(density = baseDensity.density, fontScale = 1f)
    ) {
        HomeScreenContentBody(
            navController = navController,
            onNavigate = onNavigate,
            onCustomerClick = onCustomerClick,
            designTokens = designTokens
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun HomeScreenContentBody(
    navController: NavHostController,
    onNavigate: (String) -> Unit,
    onCustomerClick: (String) -> Unit,
    designTokens: AppDesignTokens
) {
    val dashboardViewModel: DashboardViewModel = hiltViewModel()
    val uiState by dashboardViewModel.uiState.collectAsStateWithLifecycle()

    val authViewModel: Authenticate = hiltViewModel()
    val userEntity by authViewModel.user.collectAsStateWithLifecycle()
    val adminName = userEntity?.firstName?.takeIf { it.isNotBlank() } ?: "Admin"

    val quickModules = remember {
        listOf(
            QuickModule("Contacts", R.drawable.ic_contact),
            QuickModule("Leads", R.drawable.ic_lead),
            QuickModule("Deals", R.drawable.ic_speaker),
            QuickModule("Tickets", R.drawable.ic_ticket),
            QuickModule("Email", R.drawable.ic_mail),
            QuickModule("Calendar", R.drawable.ic_calendar),
            QuickModule("Orders", R.drawable.box),
            QuickModule("Reports", R.drawable.ic_report),
            QuickModule("Customer feedback", R.drawable.ic_document),
            QuickModule("Quotation Screen", R.drawable.ic_file)
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
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color.Red,
                        modifier = Modifier.size(designTokens.iconSize * 2.2f)
                    )
                    Spacer(Modifier.height(designTokens.screenPadding * 0.5f))
                    Text(
                        "Failed to load dashboard",
                        color = Color.Red,
                        fontWeight = FontWeight.Bold,
                        fontSize = designTokens.bodyMedium
                    )
                    Text(
                        "Something went wrong, Please Try again after sometime",
                        color = Color.Gray,
                        fontSize = designTokens.bodySmall
                    )
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
                contentPadding = PaddingValues(
                    horizontal = designTokens.screenPadding,
                    vertical = designTokens.screenPadding
                ),
                verticalArrangement = Arrangement.spacedBy(designTokens.screenPadding * 1.25f)
            ) {
                item {
                    GreetingCard(
                        userName = adminName,
                        newLeadsCount = newLeadsCount,
                        onNavigate = onNavigate
                    )
                }
                item { StatsGrid(stats, designTokens) }
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
                            onCustomerClick = onCustomerClick
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

@Composable
private fun StatsGrid(stats: List<DashboardStat>, tokens: AppDesignTokens) {
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
            .background(Color(0xFFF8F9FF), RoundedCornerShape(tokens.cardCornerRadius * 1.6f))
            .border(1.dp, Color(0xFFE8EAF4), RoundedCornerShape(tokens.cardCornerRadius * 1.6f))
            .padding(tokens.cardPadding * 0.7f)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
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
        Spacer(Modifier.height(tokens.screenPadding * 0.7f))
        Text(
            stat.value,
            fontSize = tokens.h2,
            color = if (stat.label == "Revenue" || stat.label == "Pending") Color(0xFF2F27CE) else Color(0xFF0B1C30),
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(tokens.screenPadding * 0.25f))
        when (stat.trendUp) {
            true -> TrendRow(icon = Icons.Default.ArrowUpward, text = stat.trendText, color = Color(0xFF16A34A), tokens = tokens)
            false -> TrendRow(icon = Icons.Default.ArrowDownward, text = stat.trendText, color = redText, tokens = tokens)
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
                                "Orders"->"sales_sales_orders"
                                "Reports"->"sales_reports"
                                "Customer feedback"->""
                                "Quotation Screen"->""
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
                        Icon(
                            painterResource(module.icon),
                            contentDescription = module.label,
                            tint = Color(0xFF2F27CE),
                            modifier = Modifier.size(tokens.iconSize * 1.3f)
                        )
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
        title.contains("Revenue", true) -> Triple(R.drawable.revenue, Color(0xFFEDE9FE), statLogoBg)
        title.contains("Order", true) -> Triple(R.drawable.cart, Color(0xFFEDE9FE), statLogoBg)
        title.contains("Measurement", true) -> Triple(R.drawable.customer, Color(0xFFEDE9FE), statLogoBg)
        title.contains("Pending", true) || title.contains("Payment", true) -> Triple(R.drawable.pending, Color(0xFFEDE9FE), statLogoBg)
        else -> Triple(R.drawable.cart, Color(0xFFEDE9FE), Color(0xFF7C3AED))
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

@Composable
private fun RecentActivitySection(onNavigate: (String) -> Unit) {
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
                color = Primary,
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
                            painter = activity.icon,
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

@Composable
private fun RecentCustomersSection(
    customers: List<RecentCustomer>,
    onCustomerClick: (String) -> Unit
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
                        ) { onCustomerClick(customer.id) }
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

fun normalizeRoute(rawKey: String): String {
    return when (rawKey) {
        "sales_lead_management" -> "sales_lead"
        "sales_customer" -> "sales_customers"
        "sales_measurements" -> "sales_measurements"
        "sales_sales_&_orders", "sales_sales_and_orders" -> "sales_sales_orders"
        "sales_order_management" -> "sales_orders"
        "sales_pricing_overview" -> "sales_pricing_overview"
        "sales_quotation" -> "sales_pricing_quotation"

        "finance_sales_invoices" -> "finance_sales_invoices"
        "finance_customers" -> "finance_customers"
        "finance_payments_received" -> "finance_payments_received"
        "finance_suppliers" -> "finance_suppliers"
        "finance_expenses" -> "finance_expenses"
        "finance_chart_of_accounts" -> "finance_chart_of_accounts"
        "finance_journal_entries" -> "finance_journal_screen"
        "finance_trial_balance" -> "finance_trial_balance"

        "inventory_all_items" -> "inventory_items"
        "inventory_item_groups" -> "inventory_item_groups"
        "inventory_orders",
        "inventory_procurement_orders",
        "inventory_low_stock_alerts",
        "inventory_alerts_&_reorder" -> "inventory_low_stock_alerts"

        "logistics_delivery" -> "logistics_delivery"
        "logistics_order_tracking" -> "logistics_order_tracking"

        "services_service_status" -> "services_service_status"
        "services_delay_rework" -> "services_delay_rework"
        "services_service_delivery" -> "services_service_delivery"
        "services_customer_feedback" -> "services_customer_feedback"
        "services_alteration_management" -> "services_alteration_management"
        "services_service_request" -> "services_service_request"
        "services_service_orders", "services_service_order" -> "services_service_orders"
        "service_order_overview" -> "service_order_overview"

        "hr_employees" -> "hr_all_employees"

        "reports_sales" -> "reports_sales"
        "reports_marketing" -> "reports_marketing"
        "reports_inventory", "reports_reports_inventory" -> "reports_inventory"
        "reports_finance", "reports_reports_finance" -> "reports_finance"
        "reports_human_resource" -> "reports_human_resource"
        "reports_logistics" -> "reports_logistics"
        "reports_it" -> "reports_it"
        "reports_legal" -> "reports_legal"

        else -> rawKey
    }
}

fun menuForScreen(screen: String): String = when {
    screen == "home" || screen == "settings" || screen == "profile-settings" || screen == "settings_overview" || screen == "module_settings" || screen.startsWith("home_") -> "Home"
    screen.startsWith("sales_") || screen in setOf(
        "create_lead", "view_lead", "edit_lead",
        "create_order", "order_overview", "create_order_review",
        "view_customer", "edit_customer",
        "create_quotation", "create_garment_pricing", "garment_pricing_list",
        "order_management_overview"
    ) -> "Sales"
    screen.startsWith("services_") || screen in setOf(
        "service_status_detail",
        "service_order_overview",
        "review_services",
        "feedback_detail",
        "create_alteration",
        "create_request"
    ) -> "Services"
    screen.startsWith("finance_") -> "Finance"
    screen.startsWith("inventory_") -> "Inventory"
    screen.startsWith("hr_") -> "HR"
    screen.startsWith("logistics_") || screen == "tracking_overview" -> "Logistics"
    screen.startsWith("reports_") -> "Reports"
    else -> "Home"
}

fun formatLeadDate(raw: String?): String {
    if (raw.isNullOrBlank()) return "—"
    return try {
        val datePart = raw.take(10)
        val parts = datePart.split("-")
        if (parts.size == 3) {
            val year = parts[0]
            val month = parts[1]
            val day = parts[2]
            "$day-$month-$year"
        } else {
            raw
        }
    } catch (_: Exception) {
        raw
    }
}

fun String.toIsoDate(): String {
    if (this.isBlank()) return ""
    return try {
        val parts = this.trim().split("-")
        if (parts.size == 3) {
            val day = parts[0]
            val month = parts[1]
            val year = parts[2]
            "$year-$month-${day}T00:00:00.000Z"
        } else {
            ""
        }
    } catch (_: Exception) {
        ""
    }
}