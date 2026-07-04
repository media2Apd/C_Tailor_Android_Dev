package com.cuso.mobile.view.home

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.cuso.mobile.ui.theme.lightGray
import com.cuso.mobile.viewmodel.HomeViewModel
import com.cuso.mobile.viewmodel.Authenticate
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.core.common.Dimensions
import com.patrykandpatrick.vico.core.common.shape.Shape as VicoShape
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.common.component.LineComponent
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import com.cuso.mobile.model.BudgetRange
import com.cuso.mobile.model.CreateLeadFormRequest
import com.cuso.mobile.model.LeadAppointment
import com.cuso.mobile.model.LeadContact
import com.cuso.mobile.model.LeadNote
import com.cuso.mobile.model.LeadPerson
import com.cuso.mobile.model.LeadTableItem
import com.cuso.mobile.model.toLeadEntity
import com.cuso.mobile.view.composable.PhoneInputField
import com.cuso.mobile.viewmodel.SaleState
import com.cuso.mobile.viewmodel.SalesViewModel

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.wear.compose.material.Colors
import coil.compose.AsyncImage
import com.cuso.mobile.database.entities.SalesStatusEntity
import com.cuso.mobile.model.CategoryItem
import com.cuso.mobile.model.Organization
import com.cuso.mobile.model.Settings
import com.cuso.mobile.model.StaffDto
import com.cuso.mobile.model.Subscription
import com.cuso.mobile.model.User
import com.cuso.mobile.ui.theme.Primary
import com.cuso.mobile.view.home.branch.BranchSettingsScreen
import com.cuso.mobile.view.home.department.DepartmentSettingsScreen
import com.cuso.mobile.view.home.designation.DesignationScreen
import com.cuso.mobile.view.home.sales.GarmentTypeContent
import com.cuso.mobile.view.home.sales.SalesSettingsScreen
import com.cuso.mobile.view.home.sidebar.FullSideBar
import com.cuso.mobile.view.home.sidebar.SalesSideBar
import com.cuso.mobile.view.home.sales.CreateOrderScreen
import com.cuso.mobile.view.home.sales.SalesOrderScreen
import com.cuso.mobile.view.home.sales.CreateOrderNextStep
import com.cuso.mobile.view.home.sales.OrderReviewData
import com.cuso.mobile.view.home.reusablecomposables.ActionDropdownMenu
import com.cuso.mobile.view.home.reusablecomposables.DataCard
import com.cuso.mobile.view.home.reusablecomposables.DataCardBadge
import com.cuso.mobile.view.home.reusablecomposables.DataCardField
import com.cuso.mobile.view.home.reusablecomposables.FilterDrawer
import com.cuso.mobile.view.home.reusablecomposables.FilterOption
import com.cuso.mobile.view.home.reusablecomposables.MenuAction
import com.cuso.mobile.view.home.reusablecomposables.rememberFilterDrawerState
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
import com.cuso.mobile.model.CustomerItem
import com.cuso.mobile.model.DashboardStatDto
import com.cuso.mobile.model.OperationItem
import com.cuso.mobile.view.home.sales.CustomerScreen
import com.cuso.mobile.view.home.sales.MeasurementsScreen
import com.cuso.mobile.viewmodel.CustomerViewModel
import com.cuso.mobile.viewmodel.DashboardUiState
import com.cuso.mobile.viewmodel.DashboardViewModel

// ── Design tokens (Primary color used everywhere for icons / accents) ──
val LeadPrimary = Color(0xFF3B3BF9)
val LeadPrimarySoft = Color(0xFFEEEEFE)
val LeadTextMuted = Color(0xFF9CA3AF)

// ── Data classes ──

data class LeadItem(
    val header: String,
    val value: Float
)

data class ControlItem(
    val controls: String
)

// ─────────────────────────────────────────────────────────────
// HomeScreen
// ─────────────────────────────────────────────────────────────
@Suppress("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(navController: NavController) {
    val viewModel: HomeViewModel = hiltViewModel()
    val isLoggedOut: Boolean by viewModel.isLoggedOut.collectAsStateWithLifecycle(initialValue = false)
    var currentScreen by remember { mutableStateOf("home") }
    var isDrawerOpen by remember { mutableStateOf(false) }
    var pendingOrderReviewData by remember { mutableStateOf<OrderReviewData?>(null) }
    val customerViewModel: CustomerViewModel = hiltViewModel()
    val customerUiState by customerViewModel.uiState.collectAsStateWithLifecycle()
    var selectedCustomer by remember { mutableStateOf<CustomerItem?>(null) }

    var isSalesSettingsMode by remember { mutableStateOf(false) }
    var showModulesPanel by remember { mutableStateOf(false) }   // ✅ NEW

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

    val showHomePanel = currentScreen == "settings" ||
            currentScreen == "home_organization_profile" ||
            currentScreen == "home_branch_management" ||
            currentScreen == "home_department_teams" ||
            currentScreen == "home_designation"

    val showSalesPanel = isSalesSettingsMode
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {   // ✅ NEW — wraps Scaffold so panel can overlay everything
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0),
            topBar = {
                TopBar(
                    onProfileClick={
                        currentScreen="profile-settings"
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
                            currentScreen = "sales_settings"
                        } else {
                            if (showHomePanel || showSalesPanel) {
                                isSalesSettingsMode = false
                                currentScreen = "home"
                            } else {
                                currentScreen = "settings"
                            }
                        }
                    },
                    onMenuItemClick = { route ->
                        when {
                            route == "home_organization_profile" -> {
                                currentScreen = "home_organization_profile"
                                isDrawerOpen = false
                            }
                            route == "home_branch_management" -> {
                                currentScreen = "home_branch_management"
                                isDrawerOpen = false
                            }
                            route == "home_department_teams" -> {
                                currentScreen = "home_department_teams"
                                isDrawerOpen = false
                            }
                            route == "home_designation" -> {
                                currentScreen = "home_designation"
                                isDrawerOpen = false
                            }
                            route == "sales_lead" -> {
                                isSalesSettingsMode = false
                                currentScreen = "sales_lead"
                                isDrawerOpen = false
                            }
                            route == "sales_customers" -> {
                                isSalesSettingsMode = false
                                currentScreen = "sales_customers"
                                isDrawerOpen = false
                            }
                            route == "sales_measurements" -> {
                                isSalesSettingsMode = false
                                currentScreen = "sales_measurements"
                                isDrawerOpen = false
                            }
                            route == "sales_sales_orders" ||
                                    route == "sales_sales_&_orders" -> {
                                isSalesSettingsMode = false
                                currentScreen = "sales_sales_orders"
                                isDrawerOpen = false
                            }
                            route == "sales_orders" -> {
                                isSalesSettingsMode = false
                                currentScreen = "sales_orders"
                                isDrawerOpen = false
                            }
                            route == "sales_garment_type" -> {
                                currentScreen = "sales_garment_type"
                                isDrawerOpen = false
                            }
                            route == "home" -> {
                                isSalesSettingsMode = false
                                currentScreen =
                                    if (showHomePanel) "settings" else "home"
                                isDrawerOpen = false
                            }
                            route == "settings" -> {
                                currentScreen = "settings"
                                isDrawerOpen = false
                            }
                            else -> {
                                android.util.Log.d("NAV_DEBUG", "Unhandled route: $route")
                                try {
                                    navController.navigate(route)
                                } catch (_: Exception) {
                                    if (route.startsWith("sales_")) {
                                        currentScreen = route
                                    }
                                }
                                isDrawerOpen = false
                            }
                        }
                    },
                    onModulesClick = { showModulesPanel = true },   // ✅ NEW
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
                        onMenuClick = { isDrawerOpen = true }
                    )
                    "home_organization_profile" -> SettingsScreen(
                        navController = navController,
                        onMenuClick = { isDrawerOpen = true }
                    )
                    "home_branch_management" -> BranchSettingsScreen(
                        navController = navController,
                        onMenuClick = { isDrawerOpen = true },
                        onBack = { currentScreen = "settings" }
                    )
                    "home_department_teams" -> DepartmentSettingsScreen(
                        navController = navController,
                        onMenuClick = { isDrawerOpen = true },
                        onBack = { currentScreen = "settings" }
                    )
                    "home_designation" -> DesignationScreen(
                        navController = navController,
                        onMenuClick = { isDrawerOpen = true },
                        onBack = { currentScreen = "settings" }
                    )
                    "sales_settings" -> SalesSettingsScreen(
                        navController = navController,
                        onClose = {
                            isSalesSettingsMode = false
                            currentScreen = "sales_lead"
                        },
                        onMenuClick = { isDrawerOpen = true }
                    )
                    "sales_garment_type" -> GarmentTypeContent(
                        onClose = {
                            currentScreen = "sales_settings"
                            isSalesSettingsMode = true
                        },
                        onMenuClick = { isDrawerOpen = true }
                    )
                    "home" -> HomeScreenContent()
                    "sales_lead" -> LeadScreenContent(
                        onCreateLead = { currentScreen = "create_lead" },
                        onViewLead = { currentScreen = "view_lead" },
                        onEditLead = { currentScreen = "edit_lead" }
                    )
                    "create_lead" -> CreateLeadScreen(
                        onBack = { currentScreen = "sales_lead" }
                    )
                    "view_lead" -> ViewLeadScreen(
                        onBack = { currentScreen = "sales_lead" },
                        onEditLead = { currentScreen = "edit_lead" }
                    )
                    "edit_lead" -> EditLeadScreen(
                        onBack = { currentScreen = "sales_lead" }
                    )
                    "create_order" -> {
                        CreateOrderScreen(
                            onBack = { currentScreen = "sales_sales_orders" },
                            onCancel = { currentScreen = "sales_sales_orders" },
                            onNextStep = { orderReviewData ->
                                pendingOrderReviewData = orderReviewData
                                currentScreen = "create_order_review"
                            }
                        )
                    }
                    "sales_sales_orders" -> SalesOrderScreen(
                        navController = navController,
                        onMenuClick = { isDrawerOpen = true },
                        onBack = { currentScreen = "sales_lead" },
                        onCreateOrder = { currentScreen = "create_order" }
                    )
                    "sales_orders" -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Orders Screen", fontSize = 18.sp, color = Color.Gray)
                        }
                    }
                    "sales_customers" -> CustomerScreen(
                        navController = navController,
                        customerState = customerUiState,
//                        onSearch = { query -> customerViewModel.search(query) },
//                        onTypeFilterChange = { type -> customerViewModel.filterByType(type) },
//                        onPageChange = { page -> customerViewModel.setPage(page) },
//                        onItemsPerPageChange = { count -> customerViewModel.setItemsPerPage(count) },
                        onBack = { currentScreen = "sales_lead" },
                        onCreateCustomer = { currentScreen = "create_customer" },
                        onView = { customer ->
                            selectedCustomer = customer
                            currentScreen = "view_customer"
                        },
                        onEdit = { customer ->
                            selectedCustomer = customer
                            currentScreen = "edit_customer"
                        },
//                        onDelete = { customer -> customerViewModel.deleteCustomer(customer.id) }
                    )

                    "sales_measurements" -> MeasurementsScreen(
                        navController = navController,
                        onBack = { currentScreen = "sales_lead" },
                        onCreateOrder = { currentScreen = "create_order" }
                    )
                    "create_order_review" -> {
                        pendingOrderReviewData?.let { data ->
                            CreateOrderNextStep(
                                orderData = data,
                                onBack = { currentScreen = "create_order" },
                                onSaveOrder = {
                                    pendingOrderReviewData = null
                                    currentScreen = "sales_sales_orders"
                                }
                            )
                        } ?: run {
                            currentScreen = "create_order"
                        }
                    }
                    "profile-settings" -> ProfileSettingsScreen(
                        onClose = { currentScreen = "home" },
                        onOrganizationSetup = { currentScreen = "home_organization_profile" },
                        onBranchManagement = { currentScreen = "home_branch_management" },
                        onDepartment = { currentScreen = "home_department_teams" },
                        onDesignation = { currentScreen = "home_designation" },
                        onLogout = {
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
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
            onModuleCategoryClick = { menu, category ->
                // category-ல first subItem-ஐ எடுத்து, sidebar mாதிரி அதே navigation key build பண்றோம்
                val menuItem = SidebarConfig.getFullMenuItems().find { it.label == menu }
                val firstSubItem = menuItem?.subItems?.get(category)?.firstOrNull()

                val navKey = if (firstSubItem != null) {
                    buildNavigationKey(menu, firstSubItem)
                } else {
                    buildNavigationKey(menu, category)
                }

                currentScreen = navKey
                showModulesPanel = false
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────
// TopNavBar
// ─────────────────────────────────────────────────────────────

@Composable
fun TopBar(
    isPanelMode: Boolean = false,
    hasNotification: Boolean = true,
    onSearchClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
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

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 1.dp,   // ✅ real elevation shadow
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // ── Left: Logo + Title ──
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.cuso_logo),
                    contentDescription = "Logo",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(15.dp))
                Text(
                    text = "Tailor",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
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
                    AsyncImage(
                        model = profilePicture,
                        contentDescription = "Profile picture",
                        modifier = Modifier
                            .size(avatarSize)
                            .clip(CircleShape)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { onProfileClick() },
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
                .background(Color.Transparent)

        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .border(
                        width = 1.dp,
                        color = Color(0xFFE5E7EB),
                        shape = RoundedCornerShape(24.dp)
                    ),
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
                        icon = Icons.Default.Home,
                        label = "Home",
                        isSelected = currentScreen == "home",
                        selectedColor = Color(0xFF6C4FF6),
                        onClick = { onMenuItemClick("home") }
                    )

                    BottomNavItem(
                        icon = Icons.Default.Inventory2,
                        label = "Orders",
                        isSelected = currentScreen == "orders",
                        selectedColor = Color(0xFF6C4FF6),
                        onClick = { onMenuItemClick("orders") }
                    )

                    Spacer(modifier = Modifier.width(64.dp))

                    BottomNavItem(
                        icon = Icons.Default.BarChart,
                        label = "Reports",
                        isSelected = currentScreen == "reports",
                        selectedColor = Color(0xFF6C4FF6),
                        onClick = { onMenuItemClick("reports") }
                    )

                    BottomNavItem(
                        icon = Icons.Default.GridView,
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
                    .background(Color(0xFF6C4FF6))
                    .clickable { onDrawerToggle() },
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
    icon: ImageVector,
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
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
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
// UI-side data models (built FROM api dtos, not hardcoded)
// ─────────────────────────────────────────────────────────────
private data class DashboardStat(
    val label: String,
    val value: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val iconBg: Color,
    val iconTint: Color,
    val trendText: String,
    val trendUp: Boolean?
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

// ─────────────────────────────────────────────────────────────
// Mappers — API dto -> UI model
// ─────────────────────────────────────────────────────────────
private fun statVisualsFor(title: String): Triple<androidx.compose.ui.graphics.vector.ImageVector, Color, Color> {
    return when {
        title.contains("Revenue", true) ->
            Triple(Icons.Default.AccountBalanceWallet, Color(0xFFEDE9FE), Color(0xFF7C3AED))
        title.contains("Order", true) ->
            Triple(Icons.Default.ShoppingCart, Color(0xFFEDE9FE), Color(0xFF7C3AED))
        title.contains("Measurement", true) ->
            Triple(Icons.Default.Straighten, Color(0xFFEDE9FE), Color(0xFF7C3AED))
        title.contains("Pending", true) || title.contains("Payment", true) ->
            Triple(Icons.Default.Description, Color(0xFFEDE9FE), Color(0xFF7C3AED))
        else ->
            Triple(Icons.Default.Info, Color(0xFFEDE9FE), Color(0xFF7C3AED))
    }
}

private fun formatCompactNumber(value: Double): String {
    return when {
        value >= 100000 -> String.format("%.1fL", value / 100000)
        value >= 1000 -> String.format("%.1fk", value / 1000)
        else -> value.toInt().toString()
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
            "₹ ${formatCompactNumber(stat.value)}"
        } else {
            stat.value.toInt().toString()
        }
        val trendText = "${stat.change.toInt()}%"
        DashboardStat(stat.title, valueText, icon, iconBg, iconTint, trendText, trendUp)
    }
}

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
@Composable
fun HomeScreenContent() {
    // ✅ Locks fontScale to 1f so the layout looks identical across every
    // device regardless of the system "font size" accessibility setting.
    val baseDensity = LocalDensity.current
    CompositionLocalProvider(
        LocalDensity provides Density(density = baseDensity.density, fontScale = 1f)
    ) {
        HomeScreenContentBody()
    }
}

@Composable
private fun HomeScreenContentBody() {
    val dashboardViewModel: DashboardViewModel = hiltViewModel()
    val uiState by dashboardViewModel.uiState.collectAsStateWithLifecycle()

    val authViewModel: Authenticate = hiltViewModel()
    val userEntity by authViewModel.user.collectAsStateWithLifecycle()
    val adminName = userEntity?.firstName?.takeIf { it.isNotBlank() } ?: "Admin"

    // Quick Modules — static shortcuts (not part of this API response)
    val quickModules = remember {
        listOf(
            QuickModule("Contacts", Icons.Default.Person),
            QuickModule("Leads", Icons.Default.TrendingUp),
            QuickModule("Deals", Icons.Default.Sell),
            QuickModule("Tickets", Icons.Default.Description),
            QuickModule("Email", Icons.Default.Email),
            QuickModule("Calendar", Icons.Default.CalendarMonth)
        )
    }

    when (val state = uiState) {
        is DashboardUiState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF7F7FB)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color(0xFF7C3AED))
                    Spacer(Modifier.height(8.dp))
                    Text("Loading dashboard...", color = Color.Gray, fontSize = 14.sp)
                }
            }
        }

        is DashboardUiState.Error -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF7F7FB)),
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
                    .background(Color(0xFFF7F7FB)),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item { GreetingCard(userName = adminName, newLeadsCount = newLeadsCount) }
                item { StatsGrid(stats) }
                item { QuickModulesSection(quickModules) }
                if (activities.isNotEmpty()) {
                    item { RecentActivitySection(activities) }
                }
                if (customers.isNotEmpty()) {
                    item { RecentCustomersSection(customers) }
                }
                item { Spacer(Modifier.height(8.dp)) }
            }
        }
        else -> {
            CircularProgressIndicator()
        }
    }


}

// ── Greeting card — purple gradient banner ──
@Composable
private fun GreetingCard(userName: String, newLeadsCount: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(colors = listOf(Color(0xFF6C4FF6), Color(0xFF9333EA))))
            .padding(20.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
        }
        Column {
            Text("Good Morning, $userName", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(
                "You have $newLeadsCount new leads to review today",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 13.sp,
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
                rowStats.forEach { stat -> DashboardStatCard(stat = stat, modifier = Modifier.weight(1f)) }
                if (rowStats.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun DashboardStatCard(stat: DashboardStat, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(Color.White, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(stat.iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(stat.icon, contentDescription = null, tint = stat.iconTint, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.height(10.dp))
        Text(stat.label, fontSize = 13.sp, color = Color(0xFF6B7280), fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(2.dp))
        Text(stat.value, fontSize = 19.sp, color = Color(0xFF111827), fontWeight = FontWeight.Bold)
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

// ── Quick Modules — static shortcuts row ──
@Composable
private fun QuickModulesSection(modules: List<QuickModule>) {
    Column {
        Text("Quick Modules", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
        Spacer(Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(modules) { module ->
                Column(modifier = Modifier.width(64.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFEDE9FE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(module.icon, contentDescription = module.label, tint = Color(0xFF7C3AED), modifier = Modifier.size(24.dp))
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

// ── Recent Activity — driven by API `activeOrders` ──
@Composable
private fun RecentActivitySection(activities: List<ActivityItem>) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Recent Activity", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
            Text("View All", fontSize = 13.sp, color = Color(0xFF7C3AED), fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            activities.forEach { activity ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(14.dp))
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
            }
        }
    }
}

// ── Recent Customers — driven by API `operations` ──
@Composable
private fun RecentCustomersSection(customers: List<RecentCustomer>) {
    Column {
        Text("Recent Customers", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
        Spacer(Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            customers.forEach { customer ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                        .clickable { /* TODO: navigate to customer detail */ },
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
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Reusable "Lead Form" UI kit
// ─────────────────────────────────────────────────────────────

@Composable
fun LeadFormTopBar(
    title: String,
    badgeText: String,
    badgeColor: Color = LeadPrimary,
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (badgeText.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .background(badgeColor.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(badgeText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = badgeColor)
                }
            }
            Icon(
                Icons.Default.Close,
                contentDescription = "Close",
                tint = Color(0xFF111827),
                modifier = Modifier
                    .size(22.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onClose() }
            )
        }
    }
}

@Composable
fun LeadInfoBanner(text: String) {
    var visible by remember { mutableStateOf(true) }
    AnimatedVisibility(visible = visible, enter = fadeIn(), exit = fadeOut()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(LeadPrimarySoft, RoundedCornerShape(10.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Info, contentDescription = null, tint = LeadPrimary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            Text(text, fontSize = 13.sp, color = Color(0xFF374151), modifier = Modifier.weight(1f))
            Icon(
                Icons.Default.Close,
                contentDescription = "Dismiss",
                tint = Color(0xFF9CA3AF),
                modifier = Modifier
                    .size(16.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { visible = false }
            )
        }
    }
}

@Composable
fun LeadAccordionSection(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    expanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    trailing: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val chevronRotation by animateFloatAsState(if (expanded) 180f else 0f, label = "lead_chevron")
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onExpandChange(!expanded) }
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(LeadPrimarySoft, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF111827))
            }
            Spacer(Modifier.width(8.dp))
            if (trailing != null) {
                trailing()
            } else {
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = Primary,
                    modifier = Modifier
                        .size(25.dp)
                        .rotate(chevronRotation)
                )
            }
        }
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun LeadBottomBar(
    leftLabel: String,
    leftIcon: androidx.compose.ui.graphics.vector.ImageVector,
    onLeftClick: () -> Unit,
    rightLabel: String,
    rightIcon: androidx.compose.ui.graphics.vector.ImageVector?,
    onRightClick: () -> Unit,
    rightEnabled: Boolean = true,
    rightLoading: Boolean = false
) {
    Column {
        HorizontalDivider(color = Color(0xFFF0F0F0))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onLeftClick() }
                    .padding(vertical = 6.dp, horizontal = 4.dp)
            ) {
                Icon(leftIcon, contentDescription = null, tint = Color(0xFF6B7280), modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(leftLabel, fontSize = 13.sp, color = Color(0xFF6B7280), fontWeight = FontWeight.Medium)
            }
            Button(
                onClick = onRightClick,
                enabled = rightEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = LeadPrimary,
                    disabledContainerColor = LeadPrimary.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 22.dp, vertical = 12.dp)
            ) {
                if (rightLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text(rightLabel, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    if (rightIcon != null) {
                        Spacer(Modifier.width(6.dp))
                        Icon(rightIcon, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// CreateLeadScreen
// ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateLeadScreen(onBack: () -> Unit) {
    var leadSource       by remember { mutableStateOf("") }
    var enquiryDate      by remember { mutableStateOf("") }
    var leadOwner        by remember { mutableStateOf("") }
    var leadStatus       by remember { mutableStateOf("") }
    var customerType     by remember { mutableStateOf("Individual") }
    var fullName         by remember { mutableStateOf("") }
    var email            by remember { mutableStateOf("") }
    var gender           by remember { mutableStateOf("") }
    var dob              by remember { mutableStateOf("") }
    var address          by remember { mutableStateOf("") }
    var areaZone         by remember { mutableStateOf("") }
    var city             by remember { mutableStateOf("") }
    var preferredContact by remember { mutableStateOf("") }
    var enquiryType      by remember { mutableStateOf("") }
    var estimatedQuantity by remember { mutableStateOf("") }
    var garmentCategory  by remember { mutableStateOf("") }
    var budgetRange      by remember { mutableStateOf(1000f) }
    var requiredDate     by remember { mutableStateOf("") }
    var occasion         by remember { mutableStateOf("") }
    var appointmentRequired by remember { mutableStateOf(false) }
    var appointmentDate  by remember { mutableStateOf("") }
    var appointmentTime  by remember { mutableStateOf("") }
    var assignedStaff    by remember { mutableStateOf("") }
    var followUpDate     by remember { mutableStateOf("") }
    var priority         by remember { mutableStateOf("") }
    var internalNotes    by remember { mutableStateOf("") }
    var customerNotes    by remember { mutableStateOf("") }
    var phone            by remember { mutableStateOf("") }
    var selectedIso      by remember { mutableStateOf("IN") }

    var leadSourceExpanded       by remember { mutableStateOf(false) }
    var leadOwnerExpanded        by remember { mutableStateOf(false) }
    var leadStatusExpanded       by remember { mutableStateOf(false) }
    var genderExpanded           by remember { mutableStateOf(false) }
    var preferredContactExpanded by remember { mutableStateOf(false) }
    var enquiryTypeExpanded      by remember { mutableStateOf(false) }
    var assignedStaffExpanded    by remember { mutableStateOf(false) }
    var priorityExpanded         by remember { mutableStateOf(false) }

    var expandedSection by remember { mutableStateOf("lead_info") }

    val leadSourceOptions       = listOf("Walk-in", "Instagram", "Facebook Ads", "Website")
    val genderOptions           = listOf("Male", "Female", "Other")
    val preferredContactOptions = listOf("WhatsApp", "Call")
    val enquiryTypeOptions      = listOf("New Order", "Bulk Order")
    val priorityOptions         = listOf("Low", "Medium", "High")

    val salesViewModel: SalesViewModel = hiltViewModel()
    val leadState          by salesViewModel.leadState.collectAsStateWithLifecycle()
    val staffList          by salesViewModel.staffList.collectAsStateWithLifecycle()
    val isLoadingStaff     by salesViewModel.isLoadingStaff.collectAsStateWithLifecycle()
    val salesStatuses      by salesViewModel.salesStatuses.collectAsStateWithLifecycle()
    val garmentCategories  by salesViewModel.garmentCategories.collectAsStateWithLifecycle()

    val staffDisplayList   = staffList.map { "${it.firstName} ${it.lastName} - ${it.memberId}" }
    val staffIdMap         = staffList.associate { "${it.firstName} ${it.lastName} - ${it.memberId}" to it.id }
    val selectedStaffLabel = staffIdMap.entries.firstOrNull { it.value == assignedStaff }?.key ?: ""
    val statusOptions      = salesStatuses.map { it.name }
    val statusIdMap        = salesStatuses.associate { it.name to it.id }
    val garmentIdMap       = garmentCategories.associate { it.categoryId.categoryName to it.id }
    val garmentOptions     = garmentCategories.map { it.categoryId.categoryName }

    fun clearAllFields() {
        leadSource = ""; enquiryDate = ""; leadStatus = ""; customerType = "Individual"
        fullName = ""; email = ""; gender = ""; dob = ""; address = ""; areaZone = ""; city = ""
        preferredContact = ""; enquiryType = ""; estimatedQuantity = ""; garmentCategory = ""
        budgetRange = 1000f; requiredDate = ""; occasion = ""; appointmentRequired = false
        appointmentDate = ""; appointmentTime = ""; assignedStaff = ""; followUpDate = ""
        priority = ""; internalNotes = ""; customerNotes = ""; phone = ""
    }

    fun submitLead() {
        val request = CreateLeadFormRequest(
            customerType = customerType.lowercase(),
            enquiryType = enquiryType,
            estimatedQuantity = estimatedQuantity.toIntOrNull() ?: 0,
            budgetRange = BudgetRange(min = budgetRange.toInt(), max = 250000),
            garments = listOfNotNull(garmentIdMap[garmentCategory]?.takeIf {
                garmentCategory.isNotBlank()
            }),
            enquiryDate = enquiryDate.toIsoDate(),
            requiredDate = requiredDate.toIsoDate(),
            source = leadSource,
            person = LeadPerson(
                name = fullName,
                phone = phone,
                email = email,
                gender = gender,
                dob = dob.toIsoDate()
            ),
            contact = LeadContact(
                address = address,
                area = areaZone,
                city = city,
                preferredContactMethod = preferredContact
            ),
            appointment = LeadAppointment(
                isRequired = appointmentRequired,
                date = appointmentDate.toIsoDate(),
                time = appointmentTime,
                assignedStaff = assignedStaff,
                priority = priority,
                followUpDate = followUpDate.toIsoDate()
            ),
            status = statusIdMap[leadStatus] ?: "",
            statusName = leadStatus,
            notes = buildList {
                if (internalNotes.isNotBlank()) add(LeadNote(internalNotes, "internal"))
                if (customerNotes.isNotBlank()) add(LeadNote(customerNotes, "customer"))
            }
        )
        salesViewModel.createLead(request)
    }

    LaunchedEffect(Unit) {
        salesViewModel.fetchStaff()
        salesViewModel.fetchGarmentCategories()
    }

    LaunchedEffect(leadState) {
        if (leadState is SaleState.Success) {
            salesViewModel.resetLeadState()
            salesViewModel.fetchSalesData()
            onBack()
        }
    }

    Scaffold(
        containerColor = Color(0xFFF5F5F7),
        contentWindowInsets = WindowInsets(0)
        ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                LeadFormTopBar(
                    title = "Create Lead",
                    badgeText = leadStatus.ifEmpty { "New Enquiry" },
                    onClose = onBack
                )
                HorizontalDivider(color = Color(0xFFF0F0F0))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        LeadInfoBanner("Fill the details below to create a new lead.")
                    }

                    item {
                        LeadAccordionSection(
                            icon = Icons.Default.Description,
                            title = "Lead Information",
                            subtitle = "",
                            expanded = expandedSection == "lead_info",
                            onExpandChange = { expandedSection = if (expandedSection == "lead_info") "" else "lead_info" }
                        ) {
                            FormDropdown("Lead Source", leadSource.ifEmpty { "Select an option" }, leadSourceExpanded, { leadSourceExpanded = it }, leadSourceOptions, { leadSource = it }, isRequired = true)
                            Spacer(Modifier.height(14.dp))
                            FormLabel("Enquiry Date", isRequired = true)
                            DatePickerField(value = enquiryDate.ifEmpty { "Select Date" }, onDateSelected = { enquiryDate = it })
                            Spacer(Modifier.height(14.dp))
                            FormDropdown("Lead Owner", leadOwner.ifEmpty { "Select an option" }, leadOwnerExpanded, { leadOwnerExpanded = it }, listOf("Nithish Kumar - NIT-001"), { leadOwner = it }, isRequired = true)
                            Spacer(Modifier.height(14.dp))
                            FormDropdown("Lead Status", leadStatus.ifEmpty { "Select an option" }, leadStatusExpanded, { leadStatusExpanded = it }, statusOptions, { leadStatus = it }, isRequired = true)
                        }
                    }

                    item {
                        LeadAccordionSection(
                            icon = Icons.Default.Person,
                            title = "Customer Identity",
                            subtitle = "Who is this lead for?",
                            expanded = expandedSection == "customer",
                            onExpandChange = { expandedSection = if (expandedSection == "customer") "" else "customer" }
                        ) {
                            Row(modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp))
                                .padding(4.dp)) {
                                listOf("Individual", "Corporate").forEach { type ->
                                    val isSelected = customerType == type
                                    Row(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSelected) Color.White else Color.Transparent)
                                            .clickable { customerType = type }
                                            .padding(vertical = 8.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            if (type == "Individual") Icons.Default.Person else Icons.Default.Business,
                                            type,
                                            modifier = Modifier.size(16.dp),
                                            tint = if (isSelected) LeadPrimary else Color(0xFF6B7280)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            type,
                                            fontSize = 14.sp,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                            color = if (isSelected) Color.Black else Color(0xFF6B7280)
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(14.dp))
                            FormLabel(if (customerType == "Corporate") "Company Name" else "Full Name", isRequired = true)
                            FormTextField(value = fullName, onValueChange = { fullName = it })
                            Spacer(Modifier.height(14.dp))
                            PhoneInputField(phoneValue = phone, onPhoneChange = { phone = it }, onCountryChange = { selectedIso = it.iso })
                            Spacer(Modifier.height(14.dp))
                            FormLabel("Email")
                            FormTextField(value = email, onValueChange = { email = it })
                            if (customerType == "Individual") {
                                Spacer(Modifier.height(14.dp))
                                FormDropdown("Gender", gender.ifEmpty { "Select an option" }, genderExpanded, { genderExpanded = it }, genderOptions, { gender = it })
                                Spacer(Modifier.height(14.dp))
                                FormLabel("Date of Birth")
                                DatePickerField(value = dob.ifEmpty { "Select Date" }, onDateSelected = { dob = it })
                            }
                        }
                    }

                    item {
                        LeadAccordionSection(
                            icon = Icons.Default.LocationOn,
                            title = "Location & Communication",
                            subtitle = "Contact details and preferences",
                            expanded = expandedSection == "location",
                            onExpandChange = { expandedSection = if (expandedSection == "location") "" else "location" }
                        ) {
                            FormLabel("Address")
                            FormTextField(value = address, onValueChange = { address = it })
                            Spacer(Modifier.height(14.dp))
                            FormLabel("Area / Zone")
                            FormTextField(value = areaZone, onValueChange = { areaZone = it })
                            Spacer(Modifier.height(14.dp))
                            FormLabel("City")
                            FormTextField(value = city, onValueChange = { city = it })
                            Spacer(Modifier.height(14.dp))
                            FormDropdown("Preferred Contact Method", preferredContact.ifEmpty { "Select an option" }, preferredContactExpanded, { preferredContactExpanded = it }, preferredContactOptions, { preferredContact = it })
                        }
                    }

                    item {
                        LeadAccordionSection(
                            icon = Icons.AutoMirrored.Filled.Assignment,
                            title = "Enquiry Details",
                            subtitle = "What are they looking for?",
                            expanded = expandedSection == "enquiry",
                            onExpandChange = { expandedSection = if (expandedSection == "enquiry") "" else "enquiry" }
                        ) {
                            FormDropdown("Enquiry Type", enquiryType.ifEmpty { "Select an option" }, enquiryTypeExpanded, { enquiryTypeExpanded = it }, enquiryTypeOptions, { enquiryType = it })
                            Spacer(Modifier.height(14.dp))
                            FormLabel("Estimated Quantity")
                            FormTextField(value = estimatedQuantity, onValueChange = { estimatedQuantity = it }, keyboardType = KeyboardType.Number)
                            Spacer(Modifier.height(14.dp))
                            FormLabel("Garment Category")
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(garmentOptions) { option ->
                                    val isSelected = garmentCategory == option
                                    Box(
                                        modifier = Modifier
                                            .border(1.dp, if (isSelected) Primary else Color(0xFFE5E7EB), RoundedCornerShape(10.dp))
                                            .background(if (isSelected) LeadPrimarySoft else Color.White, RoundedCornerShape(50.dp))
                                            .clickable { garmentCategory = if (garmentCategory == option) "" else option }
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        Text(option, fontSize = 13.sp, color = if (isSelected) LeadPrimary else Color(0xFF374151), fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal)
                                    }
                                }
                            }
                            Spacer(Modifier.height(14.dp))
                            FormLabel("Budget Range")
                            Slider(
                                value = budgetRange,
                                onValueChange = { budgetRange = it },
                                valueRange = 1000f..250000f,
                                modifier = Modifier.fillMaxWidth(),
                                colors = SliderDefaults.colors(
                                    thumbColor = LeadPrimary,
                                    activeTrackColor = LeadPrimary,
                                    inactiveTrackColor = Color(0xFFE5E7EB)
                                ),
                                thumb = {
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .background(Color.White, CircleShape)
                                            .border(3.dp, LeadPrimary, CircleShape)
                                    )
                                },
                                track = { sliderState ->
                                    val fraction = (sliderState.value - sliderState.valueRange.start) /
                                            (sliderState.valueRange.endInclusive - sliderState.valueRange.start)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(50))
                                            .background(Color(0xFFE5E7EB))
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(fraction)
                                                .fillMaxHeight()
                                                .clip(RoundedCornerShape(50))
                                                .background(LeadPrimary)
                                        )
                                    }
                                }
                            )
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("₹1000", fontSize = 12.sp, color = Color(0xFF6B7280))
                                Text("₹${formatIndianNumber(budgetRange.toInt())}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LeadPrimary)
                                Text("₹250000", fontSize = 12.sp, color = Color(0xFF6B7280))
                            }
                            Spacer(Modifier.height(14.dp))
                            FormLabel("Required Date")
                            DatePickerField(value = requiredDate.ifEmpty { "Select Date" }, onDateSelected = { requiredDate = it })
                            Spacer(Modifier.height(14.dp))
                            FormLabel("Occasion")
                            FormTextField(value = occasion, onValueChange = { occasion = it })
                        }
                    }

                    item {
                        LeadAccordionSection(
                            icon = Icons.Default.CalendarMonth,
                            title = "Appointment & Follow-Up",
                            subtitle = "Schedule interactions",
                            expanded = expandedSection == "appointment",
                            onExpandChange = { expandedSection = if (expandedSection == "appointment") "" else "appointment" },
                            trailing = {
                                MiniSwitch(
                                    checked = appointmentRequired,
                                    onCheckedChange = {
                                        appointmentRequired = it
                                        if (it) expandedSection = "appointment"
                                    }
                                )
                            }
                        ) {
                            if (appointmentRequired) {
                                FormLabel("Appointment Date")
                                DatePickerField(value = appointmentDate.ifEmpty { "Select Date" }, onDateSelected = { appointmentDate = it })
                                Spacer(Modifier.height(14.dp))
                                FormLabel("Appointment Time")
                                TimePickerField(value = appointmentTime, onTimeSelected = { appointmentTime = it })
                                FormDropdown("Assigned Staff", selectedStaffLabel.ifEmpty { if (isLoadingStaff) "Loading staff..." else "Select an option" }, assignedStaffExpanded && !isLoadingStaff, { assignedStaffExpanded = it }, staffDisplayList, { label -> assignedStaff = staffIdMap[label] ?: "" })
                                Spacer(Modifier.height(14.dp))
                                FormLabel("Follow-up Date", isRequired = true)
                                DatePickerField(value = followUpDate.ifEmpty { "Select Date" }, onDateSelected = { followUpDate = it })
                                Spacer(Modifier.height(14.dp))
                                FormDropdown("Priority", priority.ifEmpty { "Select an option" }, priorityExpanded, { priorityExpanded = it }, priorityOptions, { priority = it }, isRequired = true)
                            } else {
                                Text("No appointment scheduled.", fontSize = 13.sp, color = LeadTextMuted)
                            }
                        }
                    }

                    item {
                        LeadAccordionSection(
                            icon = Icons.Default.Description,
                            title = "Notes & References",
                            subtitle = "Additional information and attachments",
                            expanded = expandedSection == "notes",
                            onExpandChange = { expandedSection = if (expandedSection == "notes") "" else "notes" }
                        ) {
                            FormLabel("Internal Notes")
                            OutlinedTextField(value = internalNotes, onValueChange = { internalNotes = it }, modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp), shape = RoundedCornerShape(8.dp), colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color(0xFFE5E7EB), focusedBorderColor = LeadPrimary, unfocusedContainerColor = Color.White, focusedContainerColor = Color.White))
                            Spacer(Modifier.height(14.dp))
                            FormLabel("Customer Notes")
                            OutlinedTextField(value = customerNotes, onValueChange = { customerNotes = it }, modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp), shape = RoundedCornerShape(8.dp), colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color(0xFFE5E7EB), focusedBorderColor = LeadPrimary, unfocusedContainerColor = Color.White, focusedContainerColor = Color.White))
                        }
                    }

                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) { clearAllFields() }
                                .padding(horizontal = 15.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Clear All", fontSize = 13.sp, color = Primary, fontWeight = FontWeight.Medium)
                        }
                    }

                    if (leadState is SaleState.Error) {
                        item {
                            Text(
                                (leadState as SaleState.Error).message,
                                color = Color.Red,
                                fontSize = 12.sp
                            )
                        }
                    }

                    item { Spacer(Modifier.height(90.dp)) }
                }
            }

            Button(
                onClick = { submitLead() },
                enabled = leadState !is SaleState.Loading,
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 10.dp, bottom = 50.dp)
            ) {
                if (leadState is SaleState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Create Lead", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// LeadScreenContent
// ─────────────────────────────────────────────────────────────
@Composable
fun LeadScreenContent(
    onCreateLead: () -> Unit = {},
    onViewLead: () -> Unit = {},
    onEditLead: () -> Unit = {}
) {
    val salesViewModel: SalesViewModel = hiltViewModel()
    val context = LocalContext.current

    val leads by salesViewModel.tableLeads.collectAsStateWithLifecycle()
    val isLoading by salesViewModel.isLoadingTableLeads.collectAsStateWithLifecycle()
    val tableError by salesViewModel.tableError.collectAsStateWithLifecycle()
    val deleteState by salesViewModel.deleteState.collectAsStateWithLifecycle()
    val updateState by salesViewModel.updateState.collectAsStateWithLifecycle()

    val salesStatuses by salesViewModel.salesStatuses.collectAsStateWithLifecycle()
    val garmentCategories by salesViewModel.garmentCategories.collectAsStateWithLifecycle()
    val staffList by salesViewModel.staffList.collectAsStateWithLifecycle()

    var actionMenuLeadId by remember { mutableStateOf<String?>(null) }
    var leadToDelete by remember { mutableStateOf<LeadTableItem?>(null) }
    var isLoadingEdit by remember { mutableStateOf(false) }
    var isLoadingView by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }

    val filterDrawerState = rememberFilterDrawerState()
    var searchQuery by remember { mutableStateOf("") }
    var currentPage by remember { mutableIntStateOf(1) }
    val itemsPerPage = 10

    var filterSections by remember {
        mutableStateOf(buildFilterSections(emptyList(), emptyList(), emptyList(), emptyList(), emptyList()))
    }

    LaunchedEffect(salesStatuses, garmentCategories, staffList, leads) {
        val dynamicSources = leads.map { it.source }.filter { it.isNotBlank() }.distinct().sorted()
        filterSections = buildFilterSections(filterSections, salesStatuses, garmentCategories, staffList, dynamicSources)
    }

    LaunchedEffect(Unit) {
        salesViewModel.fetchTableLeads()
        salesViewModel.fetchStaff()
        salesViewModel.fetchGarmentCategories()
        salesViewModel.fetchSalesData()
    }

    LaunchedEffect(deleteState) {
        when (val currentState = deleteState) {
            is SaleState.Loading -> { isDeleting = true }
            is SaleState.Success -> {
                isDeleting = false
                salesViewModel.resetDeleteState()
                salesViewModel.fetchTableLeads()
                Toast.makeText(context, "Lead deleted successfully!", Toast.LENGTH_SHORT).show()
            }
            is SaleState.Error -> {
                isDeleting = false
                Toast.makeText(context, "Failed to delete lead: ${currentState.message}", Toast.LENGTH_LONG).show()
                salesViewModel.resetDeleteState()
            }
            else -> { isDeleting = false }
        }
    }

    LaunchedEffect(updateState) {
        if (updateState is SaleState.Success) {
            salesViewModel.fetchTableLeads()
            salesViewModel.resetUpdateState()
        }
    }

    fun applyFilters(sections: List<FilterSection>) {
        filterSections = sections
        currentPage = 1
    }

    fun getGarmentName(lead: LeadTableItem): String {
        val garment = lead.garmentCategory?.firstOrNull()
        return if (garment == null) "—"
        else when (garment) {
            is Map<*, *> -> {
                val categoryId = garment["categoryId"] as? Map<*, *>
                categoryId?.get("categoryName") as? String ?: "—"
            }
            is String -> lead.occasion?.takeIf { it.isNotBlank() } ?: "—"
            else -> "—"
        }
    }

    val filteredLeads = leads.filter { lead ->
        val matchesSearch = searchQuery.isBlank() ||
                lead.person.name.contains(searchQuery, ignoreCase = true) ||
                lead.enquiryType.contains(searchQuery, ignoreCase = true)

        val statusName = when (lead.status) {
            is String -> lead.status
            is Map<*, *> -> (lead.status["name"] as? String) ?: ""
            else -> ""
        }
        val selectedStatusLabels = filterSections.find { it.title == "Status" }
            ?.options?.filter { it.isSelected }?.map { it.label } ?: emptyList()
        val matchesStatus = selectedStatusLabels.isEmpty() ||
                selectedStatusLabels.any { it.equals(statusName, ignoreCase = true) }

        val selectedSourceLabels = filterSections.find { it.title == "Source" }
            ?.options?.filter { it.isSelected }?.map { it.label } ?: emptyList()
        val matchesSource = selectedSourceLabels.isEmpty() ||
                selectedSourceLabels.any { it.equals(lead.source, ignoreCase = true) }

        val garmentName = getGarmentName(lead)
        val selectedGarmentLabels = filterSections.find { it.title == "Garments" }
            ?.options?.filter { it.isSelected }?.map { it.label } ?: emptyList()
        val matchesGarments = selectedGarmentLabels.isEmpty() ||
                selectedGarmentLabels.any { it.equals(garmentName, ignoreCase = true) }

        val minAmountFilter = filterSections.find { it.title == "Amount Range" }?.minAmount?.toIntOrNull()
        val maxAmountFilter = filterSections.find { it.title == "Amount Range" }?.maxAmount?.toIntOrNull()
        val matchesAmount = (minAmountFilter == null || lead.budgetRange.max >= minAmountFilter) &&
                (maxAmountFilter == null || lead.budgetRange.min <= maxAmountFilter)

        val selectedPriority = filterSections.find { it.title == "Priority" }
            ?.options
            ?.find { it.isSelected }
            ?.id

        val matchesPriority = selectedPriority == null || run {
            val priority = lead.appointment?.priority?.lowercase() ?: ""
            when (selectedPriority) {
                "high" -> priority.contains("high")
                "medium" -> priority.contains("medium")
                "low" -> priority.contains("low")
                else -> true
            }
        }

        val selectedStaffIds = filterSections.find { it.title == "Sales Person" }
            ?.options?.filter { it.isSelected }?.map { it.id } ?: emptyList()
        val matchesSalesPerson = selectedStaffIds.isEmpty() ||
                selectedStaffIds.contains(lead.appointment?.assignedStaff)

        matchesSearch && matchesStatus && matchesSource && matchesGarments && matchesPriority && matchesAmount && matchesSalesPerson
    }

    val totalPages = maxOf(1, if (filteredLeads.isNotEmpty()) (filteredLeads.size + itemsPerPage - 1) / itemsPerPage else 1)
    val pagedLeads = if (filteredLeads.isNotEmpty()) {
        filteredLeads.drop((currentPage - 1) * itemsPerPage).take(itemsPerPage)
    } else emptyList()

    fun resolveStatusBadge(lead: LeadTableItem): Pair<String, Color> {
        val statusName = when (lead.status) {
            is String -> lead.status
            is Map<*, *> -> (lead.status["name"] as? String) ?: ""
            else -> ""
        }
        return when {
            statusName.contains("Convert", ignoreCase = true) || statusName.equals("CONVERTED", ignoreCase = true) || statusName.equals("converted_to_order", ignoreCase = true) ->
                "Converted to Order" to Color(0xFF34C759)
            statusName.contains("New", ignoreCase = true) || statusName.equals("NEW", ignoreCase = true) || statusName.equals("new_enquiry", ignoreCase = true) ->
                "New Enquiry" to Color(0xFF3B3BF9)
            statusName.contains("Quot", ignoreCase = true) || statusName.equals("QUOTED", ignoreCase = true) ->
                "Quoted" to Color(0xFFF59E0B)
            statusName.contains("Follow", ignoreCase = true) || statusName.equals("FOLLOW_UP", ignoreCase = true) || statusName.contains("Pending", ignoreCase = true) ->
                "Follow-up" to Color(0xFFEF4444)
            statusName.contains("Lost", ignoreCase = true) ->
                "Lost" to Color(0xFF6B7280)
            else -> statusName to Color(0xFF9CA3AF)
        }
    }

    fun onViewClicked(lead: LeadTableItem) {
        actionMenuLeadId = null
        isLoadingView = true
        salesViewModel.fetchLeadDetails(lead.id) { success ->
            isLoadingView = false
            if (success) onViewLead()
            else {
                Toast.makeText(context, "Failed to load lead details", Toast.LENGTH_SHORT).show()
                salesViewModel.selectLead(lead.toLeadEntity())
                onViewLead()
            }
        }
    }

    fun onEditClicked(lead: LeadTableItem) {
        actionMenuLeadId = null
        isLoadingEdit = true
        salesViewModel.fetchLeadDetails(lead.id) { success ->
            isLoadingEdit = false
            if (success) onEditLead()
            else {
                Toast.makeText(context, "Failed to load lead details for editing", Toast.LENGTH_SHORT).show()
                salesViewModel.selectLead(lead.toLeadEntity())
                onEditLead()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F7))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Lead Management", fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF111827))
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Black, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.height(8.dp))
            }

            Column(
                modifier = Modifier
                    .background(Color(0xFFF8F9FF))
                    .fillMaxWidth()
                    .padding(vertical = 10.dp, horizontal = 16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Sales", fontSize = 13.sp, color = Color(0xFF9CA3AF))
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(16.dp))
                    Text("Lead Management", fontSize = 13.sp, color = Color(0xFF3B3BF9), fontWeight = FontWeight.SemiBold)
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .background(Color.White, RoundedCornerShape(10.dp))
                            .border(1.dp, Color(0xFFE2E8F0), shape = RoundedCornerShape(10.dp))
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = {
                                searchQuery = it
                                currentPage = 1
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            textStyle = TextStyle(fontSize = 14.sp, color = Color(0xFF374151)),
                            decorationBox = { innerTextField ->
                                if (searchQuery.isEmpty()) {
                                    Text("Search leads...", fontSize = 14.sp, color = Color.Black)
                                }
                                innerTextField()
                            }
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color.White, RoundedCornerShape(10.dp))
                            .border(1.dp, Color(0xFFE2E8F0), shape = RoundedCornerShape(10.dp))
                            .clickable { filterDrawerState.open() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.FilterList, "Filter", tint = Color.Black, modifier = Modifier.size(20.dp))
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator()
                                Spacer(Modifier.height(8.dp))
                                Text("Loading leads...", color = Color.Gray)
                            }
                        }
                    }
                    tableError != null -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Warning, null, tint = Color.Red, modifier = Modifier.size(48.dp))
                                Spacer(Modifier.height(8.dp))
                                Text("Error loading leads", color = Color.Red, fontWeight = FontWeight.Bold)
                                Text(tableError ?: "Unknown error", color = Color.Gray, fontSize = 14.sp)
                                Spacer(Modifier.height(12.dp))
                                Button(onClick = { salesViewModel.fetchTableLeads() }) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                    filteredLeads.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(Modifier.padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                val hasFilters = filterSections.any { section -> section.options.any { it.isSelected } }
                                Text(
                                    if (searchQuery.isNotBlank() || hasFilters)
                                        "No matching leads found"
                                    else "No Leads Yet",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF111827)
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    if (searchQuery.isNotBlank() || hasFilters)
                                        "Try adjusting your search or filter"
                                    else "Start by creating your first lead",
                                    fontSize = 14.sp,
                                    color = Color(0xFF9CA3AF)
                                )
                                Spacer(Modifier.height(20.dp))
                                Button(
                                    onClick = onCreateLead,
                                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                                ) {
                                    Text("Create Lead", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                    else -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(0.dp)
                                ) {
                                    pagedLeads.forEach { lead ->
                                        val (badgeText, badgeColor) = resolveStatusBadge(lead)
                                        DataCard(
                                            item = lead,
                                            dateText = formatLeadDate(lead.requiredDate?.takeIf { it.isNotBlank() } ?: lead.enquiryDate),
                                            badge = DataCardBadge(text = badgeText, color = badgeColor),
                                            title = lead.person.name.ifEmpty { "—" },
                                            subtitle = "${lead.enquiryType.ifEmpty { "—" }} • ${getGarmentName(lead)} • Qty ${if (lead.estimatedQuantity == 0) "—" else lead.estimatedQuantity.toString()}",
                                            footerFields = listOf(
                                                DataCardField(
                                                    text = "₹${formatIndianNumber(lead.budgetRange.min)} - ₹${formatIndianNumber(lead.budgetRange.max)}"
                                                )
                                            ),
                                            actions = listOf(
                                                MenuAction("View", Icons.Default.Visibility, enabled = !isLoadingView) { onViewClicked(lead) },
                                                MenuAction("Edit", Icons.Default.Edit, enabled = !isLoadingEdit) { onEditClicked(lead) },
                                                MenuAction(
                                                    "Delete", Icons.Default.Delete,
                                                    tint = Color(0xFFF44336), textColor = Color(0xFFF44336),
                                                    enabled = !isDeleting
                                                ) { leadToDelete = lead }
                                            )
                                        )
                                    }
                                }
                            }

                            // ── Pagination Footer ──
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White, RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                            ) {
                                Column {
                                    HorizontalDivider(color = Color(0xFFF0F0F0))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            "Showing ${if (filteredLeads.isEmpty()) 0 else (currentPage - 1) * itemsPerPage + 1} - ${minOf(currentPage * itemsPerPage, filteredLeads.size)} of ${filteredLeads.size}",
                                            fontSize = 13.sp,
                                            color = Color(0xFF6B7280)
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            IconButton(
                                                onClick = { if (currentPage > 1) currentPage-- },
                                                enabled = currentPage > 1,
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.ChevronLeft,
                                                    "Previous",
                                                    tint = if (currentPage > 1) Color(0xFF374151) else Color(0xFFD1D5DB)
                                                )
                                            }
                                            Text("$currentPage - $totalPages", fontSize = 13.sp, color = Color(0xFF374151))
                                            IconButton(
                                                onClick = { if (currentPage < totalPages) currentPage++ },
                                                enabled = currentPage < totalPages,
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.ChevronRight,
                                                    "Next",
                                                    tint = if (currentPage < totalPages) Color(0xFF374151) else Color(0xFFD1D5DB)
                                                )
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

        Button(
            onClick = onCreateLead,
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 10.dp, bottom = 50.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Create Lead", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(Modifier.width(6.dp))
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
    }

    if (leadToDelete != null) {
        AlertDialog(
            onDismissRequest = { leadToDelete = null },
            containerColor = Color.White,
            shape = RoundedCornerShape(12.dp),
            title = {
                Text("Delete Lead", fontWeight = FontWeight.Bold, color = Color(0xFF111827))
            },
            text = {
                Text("Are you sure you want to delete this lead? This action cannot be undone.", color = Color(0xFF6B7280))
            },
            confirmButton = {
                Button(
                    onClick = {
                        salesViewModel.deleteLead(leadToDelete!!.id)
                        leadToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { leadToDelete = null },
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFD1D5DB))
                ) {
                    Text("Cancel", color = Color(0xFF374151))
                }
            }
        )
    }

    FilterDrawer(
        state = filterDrawerState,
        title = "Filters",
        sections = filterSections,
        onApply = { updatedSections ->
            applyFilters(updatedSections)
        },
        onClearAll = {
            filterSections = filterSections.map { section ->
                section.copy(
                    options = section.options.map { option ->
                        option.copy(isSelected = false)
                    }
                )
            }
            currentPage = 1
        }
    )
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
// ViewLeadScreen
// ─────────────────────────────────────────────────────────────
@Composable
fun ViewLeadScreen(
    onBack: () -> Unit,
    onEditLead: () -> Unit
) {
    val salesViewModel: SalesViewModel = hiltViewModel()
    val context = LocalContext.current

    val lead by salesViewModel.selectedLead.collectAsStateWithLifecycle()
    val isLoading by salesViewModel.isLoadingLeadDetails.collectAsStateWithLifecycle()
    val error by salesViewModel.leadDetailsError.collectAsStateWithLifecycle()
    val garmentCategories by salesViewModel.garmentCategories.collectAsStateWithLifecycle()

    var sectionLeadInfo    by remember { mutableStateOf(true) }
    var sectionCustomer    by remember { mutableStateOf(false) }
    var sectionLocation    by remember { mutableStateOf(false) }
    var sectionEnquiry     by remember { mutableStateOf(false) }
    var sectionAppointment by remember { mutableStateOf(false) }
    var sectionNotes       by remember { mutableStateOf(false) }

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F7)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = LeadPrimary)
                Spacer(Modifier.height(8.dp))
                Text("Loading lead details...", color = Color.Gray, fontSize = 14.sp)
            }
        }
        return
    }

    if (error != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F7)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red, modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(8.dp))
                Text("Error loading lead", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(error ?: "Unknown error", color = Color.Gray, fontSize = 14.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 32.dp))
                Spacer(Modifier.height(12.dp))
                Button(onClick = { onBack() }, colors = ButtonDefaults.buttonColors(containerColor = LeadPrimary), shape = RoundedCornerShape(8.dp)) {
                    Text("Go Back", color = Color.White)
                }
            }
        }
        return
    }

    if (lead == null) {
        LaunchedEffect(Unit) {
            Toast.makeText(context, "Lead data not found", Toast.LENGTH_SHORT).show()
            onBack()
        }
        return
    }

    val l = lead!!

    var appointmentRequired by remember(l.id) { mutableStateOf(l.appointmentRequired) }

    val garmentNames = if (l.garments.isNotBlank() && garmentCategories.isNotEmpty()) {
        val ids = l.garments.split(",").filter { it.isNotBlank() }
        ids.mapNotNull { id -> garmentCategories.find { it.id == id }?.categoryId?.categoryName }
    } else if (l.garments.isNotBlank() && garmentCategories.isEmpty()) {
        l.garments.split(",").filter { it.isNotBlank() }
    } else emptyList()

    Scaffold(
        containerColor = Color(0xFFF5F5F7),
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            LeadBottomBar(
                leftLabel = "Back",
                leftIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onLeftClick = onBack,
                rightLabel = "Edit Lead",
                rightIcon = Icons.Default.Edit,
                onRightClick = {
                    salesViewModel.fetchLeadDetails(l.id) { success ->
                        if (!success) {
                            Toast.makeText(context, "Failed to refresh lead data", Toast.LENGTH_SHORT).show()
                        }
                        onEditLead()
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LeadFormTopBar(
                title = "View Lead",
                badgeText = l.status.ifEmpty { "—" },
                onClose = onBack
            )
            HorizontalDivider(color = Color(0xFFF0F0F0))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
            ) {
                item {
                    LeadAccordionSection(
                        icon = Icons.Default.Description,
                        title = "Lead Information",
                        subtitle = "Basic details about this lead",
                        expanded = sectionLeadInfo,
                        onExpandChange = { sectionLeadInfo = it }
                    ) {
                        ViewFieldValue("Lead Source", l.source.ifEmpty { "—" })
                        ViewFieldValue("Enquiry Date", formatLeadDate(l.enquiryDate))
                        ViewFieldValue("Lead Owner", "Nithish Kumar - NIT-001")
                        ViewFieldValue("Lead Status", l.status.ifEmpty { "—" })
                    }
                }

                item {
                    LeadAccordionSection(
                        icon = Icons.Default.Person,
                        title = "Customer Identity",
                        subtitle = "Who is this lead for?",
                        expanded = sectionCustomer,
                        onExpandChange = { sectionCustomer = it }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp))
                                .padding(4.dp)
                        ) {
                            listOf("Individual", "Corporate").forEach { type ->
                                val isSelected = l.customerType.equals(type, ignoreCase = true)
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) Color.White else Color.Transparent)
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        if (type == "Individual") Icons.Default.Person else Icons.Default.Business,
                                        type,
                                        modifier = Modifier.size(16.dp),
                                        tint = if (isSelected) LeadPrimary else Color(0xFF6B7280)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(type, fontSize = 14.sp, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal, color = if (isSelected) Color.Black else Color(0xFF6B7280))
                                }
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        ViewFieldValue("Full Name", l.fullName.ifEmpty { "—" })
                        ViewFieldValue("Phone", l.phone.ifEmpty { "—" })
                        ViewFieldValue("Email", l.email.ifEmpty { "—" })
                        ViewFieldValue("Gender", l.gender.ifEmpty { "—" })
                        ViewFieldValue("Date of Birth", formatLeadDate(l.dob))
                    }
                }

                item {
                    LeadAccordionSection(
                        icon = Icons.Default.LocationOn,
                        title = "Location & Communication",
                        subtitle = "Contact details and preferences",
                        expanded = sectionLocation,
                        onExpandChange = { sectionLocation = it }
                    ) {
                        ViewFieldValue("Address", l.address.ifEmpty { "—" })
                        ViewFieldValue("Area / Zone", l.area.ifEmpty { "—" })
                        ViewFieldValue("City", l.city.ifEmpty { "—" })
                        ViewFieldValue("Preferred Contact Method", l.preferredContactMethod.ifEmpty { "—" })
                    }
                }

                item {
                    LeadAccordionSection(
                        icon = Icons.AutoMirrored.Filled.Assignment,
                        title = "Enquiry Details",
                        subtitle = "What are they looking for?",
                        expanded = sectionEnquiry,
                        onExpandChange = { sectionEnquiry = it }
                    ) {
                        ViewFieldValue("Enquiry Type", l.enquiryType.ifEmpty { "—" })
                        ViewFieldValue("Estimated Quantity", if (l.estimatedQuantity == 0) "—" else l.estimatedQuantity.toString())

                        Column(modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)) {
                            Text("Garment Category", fontSize = 12.sp, color = LeadTextMuted, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(4.dp))
                            if (garmentNames.isNotEmpty()) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    garmentNames.forEach { garment ->
                                        Box(
                                            modifier = Modifier
                                                .border(1.dp, LeadPrimary, RoundedCornerShape(50.dp))
                                                .background(LeadPrimarySoft, RoundedCornerShape(50.dp))
                                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp), tint = LeadPrimary)
                                                Spacer(Modifier.width(4.dp))
                                                Text(garment, fontSize = 13.sp, color = LeadPrimary, fontWeight = FontWeight.SemiBold)
                                            }
                                        }
                                    }
                                }
                            } else {
                                Text("—", fontSize = 14.sp, color = Color(0xFF111827))
                            }
                        }

                        Column(modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)) {
                            Text("Budget Range", fontSize = 12.sp, color = LeadTextMuted, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(4.dp))
                            Text("₹${formatIndianNumber(l.budgetMin)}  ₹${formatIndianNumber(l.budgetMax)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = LeadPrimary)
                            Spacer(Modifier.height(4.dp))
                            Slider(
                                value = l.budgetMin.toFloat(),
                                onValueChange = {},
                                valueRange = 1000f..250000f,
                                modifier = Modifier.fillMaxWidth(),
                                colors = SliderDefaults.colors(thumbColor = LeadPrimary, activeTrackColor = LeadPrimary, inactiveTrackColor = Color(0xFFE5E7EB)),
                                enabled = false
                            )
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("₹1000", fontSize = 12.sp, color = Color(0xFF6B7280))
                                Text("₹250000", fontSize = 12.sp, color = Color(0xFF6B7280))
                            }
                        }

                        ViewFieldValue("Required Date", formatLeadDate(l.requiredDate))
                        ViewFieldValue("Occasion", l.occasion.ifEmpty { "—" })
                    }
                }

                item {
                    LeadAccordionSection(
                        icon = Icons.Default.CalendarMonth,
                        title = "Appointment & Follow-Up",
                        subtitle = "Schedule interactions",
                        expanded = sectionAppointment,
                        onExpandChange = { sectionAppointment = it },
                        trailing = {
                            MiniSwitch(
                                checked = appointmentRequired,
                                onCheckedChange = { appointmentRequired = it }
                            )
                        }
                    ) {
                        if (appointmentRequired) {
                            ViewFieldValue("Appointment Date", formatLeadDate(l.appointmentDate))
                            ViewFieldValue("Appointment Time", l.appointmentTime.ifEmpty { "--:--" })
                            ViewFieldValue("Assigned Staff", "Select an option")
                            ViewFieldValue("Follow-up Date", formatLeadDate(l.followUpDate))
                            ViewFieldValue("Priority", l.priority.ifEmpty { "Select an option" })
                        } else {
                            Text("No appointment scheduled.", fontSize = 13.sp, color = LeadTextMuted)
                        }
                    }
                }

                item {
                    LeadAccordionSection(
                        icon = Icons.Default.Description,
                        title = "Notes & References",
                        subtitle = "Additional information and attachments",
                        expanded = sectionNotes,
                        onExpandChange = { sectionNotes = it }
                    ) {
                        ViewFieldValue("Internal Notes", l.internalNotes.ifEmpty { "—" })
                        ViewFieldValue("Customer Notes", l.customerNotes.ifEmpty { "—" })
                    }
                }

                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    }
}

@Composable
fun MiniSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(30.dp)
            .height(18.dp)
            .clip(RoundedCornerShape(50))
            .background(
                if (checked) Primary else Color(0xFFE5E7EB)
            )
            .border(
                width = 1.dp,
                color = if (checked) Primary else Color(0xFFD1D5DB),
                shape = RoundedCornerShape(50)
            )
            .clickable { onCheckedChange(!checked) }
    ) {
        Box(
            modifier = Modifier
                .padding(2.dp)
                .size(14.dp)
                .offset(x = if (checked) 12.dp else 0.dp)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}

@Composable
fun ViewFieldValue(label: String, value: String) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 6.dp)) {
        Text(
            label,
            fontSize = 12.sp,
            color = LeadTextMuted,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(2.dp))
        Text(
            value,
            fontSize = 14.sp,
            color = Color(0xFF111827),
            fontWeight = FontWeight.Normal
        )
    }
    HorizontalDivider(
        color = Color(0xFFF5F5F5),
        modifier = Modifier.padding(top = 4.dp)
    )
}

// ─────────────────────────────────────────────────────────────
// EditLeadScreen
// ─────────────────────────────────────────────────────────────
@Composable
fun EditLeadScreen(onBack: () -> Unit) {
    val salesViewModel: SalesViewModel = hiltViewModel()
    val context = LocalContext.current

    val lead by salesViewModel.selectedLead.collectAsStateWithLifecycle()
    val updateState by salesViewModel.updateState.collectAsStateWithLifecycle()
    val staffList by salesViewModel.staffList.collectAsStateWithLifecycle()
    val isLoadingStaff by salesViewModel.isLoadingStaff.collectAsStateWithLifecycle()
    val salesStatuses by salesViewModel.salesStatuses.collectAsStateWithLifecycle()
    val garmentCategories by salesViewModel.garmentCategories.collectAsStateWithLifecycle()
    val isLoadingLead by salesViewModel.isLoadingLeadDetails.collectAsStateWithLifecycle()

    if (isLoadingLead) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F7)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = LeadPrimary)
                Spacer(Modifier.height(8.dp))
                Text("Loading lead data...", color = Color.Gray, fontSize = 14.sp)
            }
        }
        return
    }

    if (lead == null) {
        LaunchedEffect(Unit) {
            Toast.makeText(context, "Lead data not found", Toast.LENGTH_SHORT).show()
            onBack()
        }
        return
    }

    val l = lead!!

    LaunchedEffect(Unit) {
        if (staffList.isEmpty()) salesViewModel.fetchStaff()
        if (garmentCategories.isEmpty()) salesViewModel.fetchGarmentCategories()
        if (salesStatuses.isEmpty()) salesViewModel.fetchSalesData()
    }

    var leadSource by remember { mutableStateOf(l.source) }
    var enquiryDate by remember { mutableStateOf(formatLeadDate(l.enquiryDate)) }
    var leadStatus by remember { mutableStateOf(l.status) }
    var customerType by remember { mutableStateOf(l.customerType.replaceFirstChar { it.uppercase() }) }
    var fullName by remember { mutableStateOf(l.fullName) }
    var email by remember { mutableStateOf(l.email) }
    var gender by remember { mutableStateOf(l.gender) }
    var dob by remember { mutableStateOf(formatLeadDate(l.dob)) }
    var address by remember { mutableStateOf(l.address) }
    var areaZone by remember { mutableStateOf(l.area) }
    var city by remember { mutableStateOf(l.city) }
    var preferredContact by remember { mutableStateOf(l.preferredContactMethod) }
    var enquiryType by remember { mutableStateOf(l.enquiryType) }
    var estimatedQuantity by remember { mutableStateOf(if (l.estimatedQuantity == 0) "" else l.estimatedQuantity.toString()) }
    var budgetRange by remember { mutableFloatStateOf(l.budgetMin.toFloat()) }
    var requiredDate by remember { mutableStateOf(formatLeadDate(l.requiredDate)) }
    var appointmentRequired by remember { mutableStateOf(l.appointmentRequired) }
    var appointmentDate by remember { mutableStateOf(formatLeadDate(l.appointmentDate)) }
    var appointmentTime by remember { mutableStateOf(l.appointmentTime) }
    var assignedStaff by remember { mutableStateOf(l.assignedStaff) }
    var followUpDate by remember { mutableStateOf(formatLeadDate(l.followUpDate)) }
    var priority by remember { mutableStateOf(l.priority) }
    var internalNotes by remember { mutableStateOf(l.internalNotes) }
    var customerNotes by remember { mutableStateOf(l.customerNotes) }
    var phone by remember { mutableStateOf(l.phone) }

    var selectedGarmentCategories by remember { mutableStateOf<List<String>>(emptyList()) }
    var showGarmentError by remember { mutableStateOf(false) }

    var leadSourceExpanded by remember { mutableStateOf(false) }
    var leadStatusExpanded by remember { mutableStateOf(false) }
    var genderExpanded by remember { mutableStateOf(false) }
    var preferredContactExpanded by remember { mutableStateOf(false) }
    var enquiryTypeExpanded by remember { mutableStateOf(false) }
    var assignedStaffExpanded by remember { mutableStateOf(false) }
    var priorityExpanded by remember { mutableStateOf(false) }

    var sectionLeadInfo    by remember { mutableStateOf(true) }
    var sectionCustomer    by remember { mutableStateOf(false) }
    var sectionLocation    by remember { mutableStateOf(false) }
    var sectionEnquiry     by remember { mutableStateOf(false) }
    var sectionAppointment by remember { mutableStateOf(false) }
    var sectionNotes       by remember { mutableStateOf(false) }

    val leadSourceOptions = listOf("Walk-in", "Instagram", "Facebook Ads", "Website")
    val genderOptions = listOf("Male", "Female", "Other")
    val preferredContactOptions = listOf("WhatsApp", "Call")
    val enquiryTypeOptions = listOf("New Order", "Bulk Order")
    val priorityOptions = listOf("Low", "Medium", "High")

    val statusOptions = salesStatuses.map { it.name }
    val statusIdMap = salesStatuses.associate { it.name to it.id }
    val staffDisplayList = staffList.map { "${it.firstName} ${it.lastName} - ${it.memberId}" }
    val staffIdMap = staffList.associate { "${it.firstName} ${it.lastName} - ${it.memberId}" to it.id }
    val selectedStaffLabel = staffIdMap.entries.firstOrNull { it.value == assignedStaff }?.key ?: ""
    val garmentIdMap = garmentCategories.associate { it.categoryId.categoryName to it.id }
    val garmentOptions = garmentCategories.map { it.categoryId.categoryName }

    LaunchedEffect(l.garments, garmentCategories) {
        if (garmentCategories.isNotEmpty() && l.garments.isNotBlank()) {
            val ids = l.garments.split(",").filter { it.isNotBlank() }
            val names = ids.mapNotNull { id -> garmentCategories.find { it.id == id }?.categoryId?.categoryName }
            if (names.isNotEmpty()) selectedGarmentCategories = names
        }
    }

    LaunchedEffect(updateState) {
        when (val state = updateState) {
            is SaleState.Success -> {
                salesViewModel.resetUpdateState()
                Toast.makeText(context, "Lead updated successfully!", Toast.LENGTH_SHORT).show()
                onBack()
            }
            is SaleState.Error -> {
                Toast.makeText(context, "Update failed: ${state.message}", Toast.LENGTH_LONG).show()
                salesViewModel.resetUpdateState()
            }
            else -> Unit
        }
    }

    fun validateAndUpdate() {
        if (selectedGarmentCategories.isEmpty()) {
            showGarmentError = true
            return
        }
        val garmentIds = selectedGarmentCategories.mapNotNull { garmentIdMap[it] }
        if (garmentIds.isEmpty()) {
            showGarmentError = true
            return
        }
        showGarmentError = false

        val request = CreateLeadFormRequest(
            customerType = customerType.lowercase(),
            enquiryType = enquiryType,
            estimatedQuantity = estimatedQuantity.toIntOrNull() ?: 0,
            budgetRange = BudgetRange(min = budgetRange.toInt(), max = 250000),
            garments = garmentIds,
            enquiryDate = enquiryDate.toIsoDate(),
            requiredDate = requiredDate.toIsoDate(),
            source = leadSource,
            person = LeadPerson(name = fullName, phone = phone, email = email, gender = gender, dob = dob.toIsoDate()),
            contact = LeadContact(address = address, area = areaZone, city = city, preferredContactMethod = preferredContact),
            appointment = LeadAppointment(
                isRequired = appointmentRequired,
                date = appointmentDate.toIsoDate(),
                time = appointmentTime,
                assignedStaff = assignedStaff,
                priority = priority,
                followUpDate = followUpDate.toIsoDate()
            ),
            status = statusIdMap[leadStatus] ?: "",
            statusName = leadStatus,
            notes = buildList {
                if (internalNotes.isNotBlank()) add(LeadNote(internalNotes, "internal"))
                if (customerNotes.isNotBlank()) add(LeadNote(customerNotes, "customer"))
                if (internalNotes.isBlank() && customerNotes.isBlank()) add(LeadNote("-", "internal"))
            },
            occasion = l.occasion
        )

        salesViewModel.updateLeadById(l.id, request)
    }

    val currentUpdateState = updateState

    Scaffold(
        containerColor = Color(0xFFF5F5F7),
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            Column {
                LeadBottomBar(
                    leftLabel = "Cancel",
                    leftIcon = Icons.Default.Close,
                    onLeftClick = onBack,
                    rightLabel = if (currentUpdateState is SaleState.Loading) "" else "Update Lead",
                    rightIcon = Icons.Default.Check,
                    rightEnabled = currentUpdateState !is SaleState.Loading && selectedGarmentCategories.isNotEmpty(),
                    rightLoading = currentUpdateState is SaleState.Loading,
                    onRightClick = { validateAndUpdate() }
                )
                if (showGarmentError) {
                    Text("Please select at least one garment category", color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
                }
                if (currentUpdateState is SaleState.Error) {
                    Text(currentUpdateState.message, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LeadFormTopBar(
                title = "Edit Lead",
                badgeText = leadStatus.ifEmpty { "—" },
                onClose = onBack
            )
            HorizontalDivider(color = Color(0xFFF0F0F0))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    LeadInfoBanner("Edit the details below and save your changes.")
                }

                item {
                    LeadAccordionSection(
                        icon = Icons.Default.Description,
                        title = "Lead Information",
                        subtitle = "Basic details about this lead",
                        expanded = sectionLeadInfo,
                        onExpandChange = { sectionLeadInfo = it }
                    ) {
                        FormDropdown("Lead Source", leadSource.ifEmpty { "Select an option" }, leadSourceExpanded, { leadSourceExpanded = it }, leadSourceOptions, { leadSource = it }, isRequired = true)
                        Spacer(Modifier.height(14.dp))
                        FormLabel("Enquiry Date", isRequired = true)
                        DatePickerField(value = enquiryDate.ifEmpty { "Select Date" }, onDateSelected = { enquiryDate = it })
                        Spacer(Modifier.height(14.dp))
                        FormDropdown("Lead Status", leadStatus.ifEmpty { "Select an option" }, leadStatusExpanded, { leadStatusExpanded = it }, statusOptions, { leadStatus = it }, isRequired = true)
                    }
                }

                item {
                    LeadAccordionSection(
                        icon = Icons.Default.Person,
                        title = "Customer Identity",
                        subtitle = "Who is this lead for?",
                        expanded = sectionCustomer,
                        onExpandChange = { sectionCustomer = it }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp))
                                .padding(4.dp)
                        ) {
                            listOf("Individual", "Corporate").forEach { type ->
                                val isSelected = customerType == type
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) Color.White else Color.Transparent)
                                        .clickable { customerType = type }
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        if (type == "Individual") Icons.Default.Person else Icons.Default.Business,
                                        type,
                                        modifier = Modifier.size(16.dp),
                                        tint = if (isSelected) LeadPrimary else Color(0xFF6B7280)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(type, fontSize = 14.sp, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal, color = if (isSelected) Color.Black else Color(0xFF6B7280))
                                }
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                        FormLabel("Full Name", isRequired = true)
                        FormTextField(value = fullName, onValueChange = { fullName = it })
                        Spacer(Modifier.height(14.dp))
                        PhoneInputField(phoneValue = phone, onPhoneChange = { phone = it }, onCountryChange = {})
                        Spacer(Modifier.height(14.dp))
                        FormLabel("Email")
                        FormTextField(value = email, onValueChange = { email = it })
                        Spacer(Modifier.height(14.dp))
                        FormDropdown("Gender", gender.ifEmpty { "Select an option" }, genderExpanded, { genderExpanded = it }, genderOptions, { gender = it })
                        Spacer(Modifier.height(14.dp))
                        FormLabel("Date of Birth")
                        DatePickerField(value = dob.ifEmpty { "Select Date" }, onDateSelected = { dob = it })
                    }
                }

                item {
                    LeadAccordionSection(
                        icon = Icons.Default.LocationOn,
                        title = "Location & Communication",
                        subtitle = "Contact details and preferences",
                        expanded = sectionLocation,
                        onExpandChange = { sectionLocation = it }
                    ) {
                        FormLabel("Address")
                        FormTextField(value = address, onValueChange = { address = it })
                        Spacer(Modifier.height(14.dp))
                        FormLabel("Area / Zone")
                        FormTextField(value = areaZone, onValueChange = { areaZone = it })
                        Spacer(Modifier.height(14.dp))
                        FormLabel("City")
                        FormTextField(value = city, onValueChange = { city = it })
                        Spacer(Modifier.height(14.dp))
                        FormDropdown("Preferred Contact Method", preferredContact.ifEmpty { "Select an option" }, preferredContactExpanded, { preferredContactExpanded = it }, preferredContactOptions, { preferredContact = it })
                    }
                }

                item {
                    LeadAccordionSection(
                        icon = Icons.AutoMirrored.Filled.Assignment,
                        title = "Enquiry Details",
                        subtitle = "What are they looking for?",
                        expanded = sectionEnquiry,
                        onExpandChange = { sectionEnquiry = it }
                    ) {
                        FormDropdown("Enquiry Type", enquiryType.ifEmpty { "Select an option" }, enquiryTypeExpanded, { enquiryTypeExpanded = it }, enquiryTypeOptions, { enquiryType = it })
                        Spacer(Modifier.height(14.dp))
                        FormLabel("Estimated Quantity")
                        FormTextField(value = estimatedQuantity, onValueChange = { estimatedQuantity = it }, keyboardType = KeyboardType.Number)
                        Spacer(Modifier.height(14.dp))

                        Column {
                            Row {
                                Text("Garment Categories", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = if (showGarmentError) Color.Red else Color.Gray)
                                Text(" *", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.Red)
                                Text(" (Select one or more)", fontSize = 11.sp, color = Color.Gray)
                            }
                            Spacer(Modifier.height(6.dp))

                            if (garmentCategories.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp))
                                        .padding(14.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = LeadPrimary)
                                        Text("Loading categories...", fontSize = 14.sp, color = Color(0xFF6B7280))
                                    }
                                }
                            } else {
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                    items(garmentOptions) { option ->
                                        val isSelected = selectedGarmentCategories.contains(option)
                                        Box(
                                            modifier = Modifier
                                                .border(1.dp, if (isSelected) LeadPrimary else Color(0xFFE5E7EB), RoundedCornerShape(50.dp))
                                                .background(if (isSelected) LeadPrimarySoft else Color.White, RoundedCornerShape(50.dp))
                                                .clickable {
                                                    selectedGarmentCategories = if (isSelected) {
                                                        selectedGarmentCategories.filter { it != option }
                                                    } else {
                                                        selectedGarmentCategories + option
                                                    }
                                                    showGarmentError = false
                                                }
                                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                if (isSelected) {
                                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp), tint = LeadPrimary)
                                                    Spacer(Modifier.width(4.dp))
                                                }
                                                Text(option, fontSize = 13.sp, color = if (isSelected) LeadPrimary else Color(0xFF374151), fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal)
                                            }
                                        }
                                    }
                                }
                                if (selectedGarmentCategories.isNotEmpty()) {
                                    Spacer(Modifier.height(4.dp))
                                    Text("Selected: ${selectedGarmentCategories.joinToString(", ")}", fontSize = 12.sp, color = Color(0xFF6B7280))
                                }
                            }

                            if (showGarmentError) {
                                Spacer(Modifier.height(4.dp))
                                Text("Please select at least one garment category", fontSize = 12.sp, color = Color.Red)
                            }
                        }

                        Spacer(Modifier.height(14.dp))
                        FormLabel("Budget Range")
                        Slider(value = budgetRange, onValueChange = { budgetRange = it }, valueRange = 1000f..250000f, modifier = Modifier.fillMaxWidth(), colors = SliderDefaults.colors(thumbColor = LeadPrimary, activeTrackColor = LeadPrimary, inactiveTrackColor = Color(0xFFE5E7EB)))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("₹1000", fontSize = 12.sp, color = Color(0xFF6B7280))
                            Text("₹${formatIndianNumber(budgetRange.toInt())}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LeadPrimary)
                            Text("₹250000", fontSize = 12.sp, color = Color(0xFF6B7280))
                        }
                        Spacer(Modifier.height(14.dp))
                        FormLabel("Required Date")
                        DatePickerField(value = requiredDate.ifEmpty { "Select Date" }, onDateSelected = { requiredDate = it })
                    }
                }

                item {
                    LeadAccordionSection(
                        icon = Icons.Default.CalendarMonth,
                        title = "Appointment & Follow-Up",
                        subtitle = "Schedule interactions",
                        expanded = sectionAppointment && appointmentRequired,
                        onExpandChange = { sectionAppointment = it },
                        trailing = {
                            Switch(
                                checked = appointmentRequired,
                                onCheckedChange = {
                                    appointmentRequired = it
                                    sectionAppointment = it
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Primary)
                            )
                        }
                    ) {
                        if (appointmentRequired) {
                            FormLabel("Appointment Date")
                            DatePickerField(value = appointmentDate.ifEmpty { "Select Date" }, onDateSelected = { appointmentDate = it })
                            Spacer(Modifier.height(14.dp))
                            FormLabel("Appointment Time")
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(appointmentTime, color = Color(0xFF374151), fontSize = 14.sp)
                                Icon(Icons.Default.Schedule, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                            }
                            FormDropdown("Assigned Staff", selectedStaffLabel.ifEmpty { if (isLoadingStaff) "Loading..." else "Select an option" }, assignedStaffExpanded && !isLoadingStaff, { assignedStaffExpanded = it }, staffDisplayList, { label -> assignedStaff = staffIdMap[label] ?: "" })
                            Spacer(Modifier.height(14.dp))
                            FormLabel("Follow-up Date")
                            DatePickerField(value = followUpDate.ifEmpty { "Select Date" }, onDateSelected = { followUpDate = it })
                            Spacer(Modifier.height(14.dp))
                            FormDropdown("Priority", priority.ifEmpty { "Select an option" }, priorityExpanded, { priorityExpanded = it }, priorityOptions, { priority = it })
                        } else {
                            Text("No appointment scheduled.", fontSize = 13.sp, color = LeadTextMuted)
                        }
                    }
                }

                item {
                    LeadAccordionSection(
                        icon = Icons.Default.Description,
                        title = "Notes & References",
                        subtitle = "Additional information",
                        expanded = sectionNotes,
                        onExpandChange = { sectionNotes = it }
                    ) {
                        FormLabel("Internal Notes")
                        OutlinedTextField(value = internalNotes, onValueChange = { internalNotes = it }, modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp), shape = RoundedCornerShape(8.dp), colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color(0xFFE5E7EB), focusedBorderColor = LeadPrimary, unfocusedContainerColor = Color.White, focusedContainerColor = Color.White))
                        Spacer(Modifier.height(14.dp))
                        FormLabel("Customer Notes")
                        OutlinedTextField(value = customerNotes, onValueChange = { customerNotes = it }, modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp), shape = RoundedCornerShape(8.dp), colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color(0xFFE5E7EB), focusedBorderColor = LeadPrimary, unfocusedContainerColor = Color.White, focusedContainerColor = Color.White))
                    }
                }

                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// LeadCard
// ─────────────────────────────────────────────────────────────
@Composable
fun LeadCard(
    lead: LeadTableItem,
    badgeText: String,
    badgeColor: Color,
    garmentName: String,
    isLoadingView: Boolean,
    isLoadingEdit: Boolean,
    isDeleting: Boolean,
    onViewClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = LeadTextMuted, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(formatLeadDate(lead.requiredDate?.takeIf { it.isNotBlank() } ?: lead.enquiryDate), fontSize = 12.sp, color = Color(0xFF6B7280))
            }
            Box(modifier = Modifier
                .background(badgeColor.copy(alpha = 0.14f), RoundedCornerShape(20.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp)) {
                Text(badgeText, fontSize = 11.sp, color = badgeColor, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                lead.person.name.ifEmpty { "—" },
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            ActionDropdownMenu(
                icon = Icons.Default.MoreVert,
                actions = listOf(
                    MenuAction("View", Icons.Default.Visibility, enabled = !isLoadingView) { onViewClick() },
                    MenuAction("Edit", Icons.Default.Edit, enabled = !isLoadingEdit) { onEditClick() },
                    MenuAction("Delete", Icons.Default.Delete, tint = Color(0xFFF44336), textColor = Color(0xFFF44336), enabled = !isDeleting) { onDeleteClick() }
                )
            )
        }

        Spacer(Modifier.height(4.dp))

        Text(
            "${lead.enquiryType.ifEmpty { "—" }} • $garmentName • Qty ${if (lead.estimatedQuantity == 0) "—" else lead.estimatedQuantity.toString()}",
            fontSize = 13.sp,
            color = Color(0xFF6B7280),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = LeadTextMuted, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(
                "₹${formatIndianNumber(lead.budgetRange.min)} - ₹${formatIndianNumber(lead.budgetRange.max)}",
                fontSize = 13.sp,
                color = Color(0xFF374151),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

fun formatLeadDate(raw: String): String {
    if (raw.isBlank()) return "—"
    return try {
        val datePart = raw.take(10)
        val parts = datePart.split("-")
        if (parts.size == 3) "${parts[2]}-${parts[1]}-${parts[0]}" else raw
    } catch (_: Exception) { raw }
}

fun formatIndianNumber(number: Int): String {
    if (number <= 0) return "0"
    val s = number.toString()
    if (s.length <= 3) return s
    val last3 = s.takeLast(3)
    val rest = s.dropLast(3)
    val grouped = rest.reversed().chunked(2).joinToString(",").reversed()
    return "$grouped,$last3"
}


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
            .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp))
            .clickable { showPicker = true }
            .padding(horizontal = 12.dp, vertical = 14.dp),
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

@Composable
fun CustomTimePicker(
    hour: Int,
    minute: Int,
    isAm: Boolean,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit,
    onAmPmChange: (Boolean) -> Unit
) {
    val hourOptions = (1..12).toList()
    val minuteOptions = (0..59).map { String.format("%02d", it) }

    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    val displayMinute = String.format("%02d", minute)

    val hourScrollState = rememberLazyListState(
        initialFirstVisibleItemIndex = hourOptions.indexOf(displayHour).coerceAtLeast(0)
    )
    val minuteScrollState = rememberLazyListState(
        initialFirstVisibleItemIndex = minuteOptions.indexOf(displayMinute).coerceAtLeast(0)
    )

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
        if (hourCenterIndex in hourOptions.indices) {
            val newHour = hourOptions[hourCenterIndex]
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
        if (minuteCenterIndex in minuteOptions.indices) {
            onMinuteChange(minuteOptions[minuteCenterIndex].toInt())
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
            Text(
                "Hour",
                fontSize = 12.sp,
                color = LeadTextMuted,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
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
                    items(hourOptions) { h ->
                        val isSelected = h == hourOptions[hourCenterIndex]
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

        Text(
            ":",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF111827),
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Minute",
                fontSize = 12.sp,
                color = LeadTextMuted,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
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
                    items(minuteOptions) { m ->
                        val isSelected = m == minuteOptions[minuteCenterIndex]
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
            modifier = Modifier
                .weight(0.8f)
                .padding(start = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "AM/PM",
                fontSize = 12.sp,
                color = LeadTextMuted,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(4.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(vertical = 20.dp),
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
                    Text(
                        "AM",
                        fontSize = 16.sp,
                        fontWeight = if (isAm) FontWeight.Bold else FontWeight.Normal,
                        color = if (isAm) Color.White else Color(0xFF6B7280)
                    )
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
                    Text(
                        "PM",
                        fontSize = 16.sp,
                        fontWeight = if (!isAm) FontWeight.Bold else FontWeight.Normal,
                        color = if (!isAm) Color.White else Color(0xFF6B7280)
                    )
                }
            }
        }
    }
}


@Composable
fun SectionHeader(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(modifier = Modifier
            .size(40.dp)
            .background(LeadPrimarySoft, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
            Icon(imageVector = icon, contentDescription = null, tint = LeadPrimary, modifier = Modifier.size(20.dp))
        }
        Column {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
            Text(subtitle, fontSize = 13.sp, color = LeadTextMuted)
        }
    }
}

@Composable
fun FormCard(content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .background(Color.White, RoundedCornerShape(12.dp))
        .padding(16.dp), content = content)
}

@Composable
fun FormLabel(text: String, isRequired: Boolean = false) {
    Row {
        Text(text = text, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
        if (isRequired) {
            Text(text = " *", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.Red)
        }
    }
    Spacer(Modifier.height(6.dp))
}
@Composable
fun FormTextField(value: String, onValueChange: (String) -> Unit, keyboardType: KeyboardType = KeyboardType.Text) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(Color.White, RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            textStyle = TextStyle(fontSize = 14.sp, color = Color(0xFF374151))
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
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(value, fontSize = 14.sp, color = if (value == "Select Date") Color(0xFF9CA3AF) else Color(0xFF374151))
        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
    }
}

private data class DatePickerPalette(
    val surface: Color,
    val text: Color,
    val subtext: Color,
    val accent: Color,
    val divider: Color,
    val accentText: Color
)

private val LightDatePalette = DatePickerPalette(
    surface = Color.White,
    text = Color.Black,
    subtext = Color(0xFF666666),
    accent = LeadPrimary,
    divider = Color(0xFFE0E0E0),
    accentText = Color.White
)

private val DarkDatePalette = DatePickerPalette(
    surface = Color(0xFF1E1E2E),
    text = Color.White,
    subtext = Color(0xFFAAAAAA),
    accent = Color(0xFF7C7CFF),
    divider = Color(0xFF3A3A4A),
    accentText = Color.Black
)

private val DatePickerMonthNames = listOf(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December"
)

@Composable
fun DatePickerField(value: String, onDateSelected: (String) -> Unit) {
    var showPicker by remember { mutableStateOf(false) }
    FormDateField(value = value, onClick = { showPicker = true })

    if (showPicker) {
        val palette = if (isSystemInDarkTheme()) DarkDatePalette else LightDatePalette
        CustomDatePickerDialog(
            palette = palette,
            initialDate = value,
            onDismiss = { showPicker = false },
            onConfirm = { day, month, year ->
                onDateSelected(
                    String.format(java.util.Locale.US, "%02d-%02d-%04d", day, month, year)
                )
                showPicker = false
            }
        )
    }
}

@Composable
private fun CustomDatePickerDialog(
    palette: DatePickerPalette,
    initialDate: String,
    onDismiss: () -> Unit,
    onConfirm: (day: Int, month: Int, year: Int) -> Unit
) {
    val today = remember { java.util.Calendar.getInstance() }
    var displayMonth by remember { mutableIntStateOf(today.get(java.util.Calendar.MONTH)) }
    var displayYear by remember { mutableIntStateOf(today.get(java.util.Calendar.YEAR)) }
    var selectedDay by remember { mutableStateOf(today.get(java.util.Calendar.DAY_OF_MONTH)) }
    var isManualEntry by remember { mutableStateOf(false) }
    var manualText by remember { mutableStateOf(initialDate) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.width(320.dp),
            shape = RoundedCornerShape(24.dp),
            color = palette.surface
        ) {
            Column(modifier = Modifier.padding(20.dp)) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Select date", color = palette.text, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    IconButton(
                        onClick = { isManualEntry = !isManualEntry },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (isManualEntry) Icons.Default.CalendarMonth else Icons.Default.Edit,
                            contentDescription = "Toggle input mode",
                            tint = palette.accent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                if (isManualEntry) {
                    OutlinedTextField(
                        value = manualText,
                        onValueChange = { manualText = it },
                        label = { Text("dd-mm-yyyy", color = palette.subtext) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = palette.text,
                            unfocusedTextColor = palette.text,
                            focusedBorderColor = palette.accent,
                            unfocusedBorderColor = palette.divider,
                            cursorColor = palette.accent,
                            focusedLabelColor = palette.accent,
                            unfocusedLabelColor = palette.subtext
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    DatePickerCalendarGrid(
                        palette = palette,
                        displayMonth = displayMonth,
                        displayYear = displayYear,
                        selectedDay = selectedDay,
                        onDaySelected = { selectedDay = it },
                        onPrevMonth = {
                            if (displayMonth == 0) { displayMonth = 11; displayYear-- }
                            else displayMonth--
                        },
                        onNextMonth = {
                            if (displayMonth == 11) { displayMonth = 0; displayYear++ }
                            else displayMonth++
                        }
                    )
                }

                Spacer(Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = palette.accent)
                    }
                    Spacer(Modifier.width(4.dp))
                    TextButton(onClick = {
                        if (isManualEntry) {
                            parseManualDatePicked(manualText)?.let { (d, m, y) -> onConfirm(d, m, y) }
                        } else {
                            onConfirm(selectedDay, displayMonth + 1, displayYear)
                        }
                    }) {
                        Text("OK", color = palette.accent)
                    }
                }
            }
        }
    }
}

@Composable
private fun DatePickerCalendarGrid(
    palette: DatePickerPalette,
    displayMonth: Int,
    displayYear: Int,
    selectedDay: Int,
    onDaySelected: (Int) -> Unit,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${DatePickerMonthNames[displayMonth]} $displayYear",
            color = palette.text,
            fontWeight = FontWeight.Medium
        )
        Row {
            IconButton(onClick = onPrevMonth) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous month", tint = palette.text)
            }
            IconButton(onClick = onNextMonth) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Next month", tint = palette.text)
            }
        }
    }

    Spacer(Modifier.height(8.dp))

    Row(modifier = Modifier.fillMaxWidth()) {
        listOf("S", "M", "T", "W", "T", "F", "S").forEach { d ->
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text(d, color = palette.subtext, fontWeight = FontWeight.Medium)
            }
        }
    }

    Spacer(Modifier.height(4.dp))

    val calendar = remember(displayMonth, displayYear) {
        java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.YEAR, displayYear)
            set(java.util.Calendar.MONTH, displayMonth)
            set(java.util.Calendar.DAY_OF_MONTH, 1)
        }
    }
    val firstDayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK) - 1
    val daysInMonth = calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)

    val cells = remember(displayMonth, displayYear) {
        buildList {
            repeat(firstDayOfWeek) { add(null) }
            for (d in 1..daysInMonth) add(d)
        }
    }

    cells.chunked(7).forEach { week ->
        Row(modifier = Modifier.fillMaxWidth()) {
            week.forEach { day ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .padding(2.dp)
                        .then(
                            if (day != null && day == selectedDay)
                                Modifier.background(palette.accent, RoundedCornerShape(50))
                            else Modifier
                        )
                        .clickable(enabled = day != null) { day?.let(onDaySelected) },
                    contentAlignment = Alignment.Center
                ) {
                    if (day != null) {
                        Text(
                            text = day.toString(),
                            color = if (day == selectedDay) palette.accentText else palette.text
                        )
                    }
                }
            }
            repeat(7 - week.size) {
                Box(modifier = Modifier.weight(1f).aspectRatio(1f))
            }
        }
    }
}

private fun parseManualDatePicked(text: String): Triple<Int, Int, Int>? {
    val parts = text.split("-")
    if (parts.size != 3) return null
    return try {
        val day = parts[0].trim().toInt()
        val month = parts[1].trim().toInt()
        val year = parts[2].trim().toInt()
        if (month !in 1..12 || day !in 1..31) return null
        Triple(day, month, year)
    } catch (e: NumberFormatException) {
        null
    }
}

@Composable
fun FormDropdown(label: String, value: String, expanded: Boolean, onExpandChange: (Boolean) -> Unit, options: List<String>, onOptionSelected: (String) -> Unit, isRequired: Boolean = false) {
    FormLabel(label, isRequired)

    val density = androidx.compose.ui.platform.LocalDensity.current
    var triggerWidthPx by remember { mutableStateOf(0) }

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates -> triggerWidthPx = coordinates.size.width }
                .height(40.dp)
                .background(Color.White, RoundedCornerShape(8.dp))
                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
                .clickable { onExpandChange(true) }
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(value, fontSize = 12.sp, color = if (value == "Select an option") Color(0xFF9CA3AF) else Color(0xFF374151))
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.Gray)
        }
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
                DropdownMenuItem(
                    text = { Text(option, fontSize = 14.sp, color = Color(0xFF374151)) },
                    onClick = { onOptionSelected(option); onExpandChange(false) },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    modifier = Modifier.heightIn(min = 36.dp)
                )
            }
        }
    }
}
@Composable
fun StatCard(modifier: Modifier = Modifier, iconBg: Color, icon: androidx.compose.ui.graphics.vector.ImageVector, iconTint: Color, title: String, value: String) {
    Box(modifier = modifier
        .background(Color.White, RoundedCornerShape(12.dp))
        .padding(16.dp)) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier
                    .size(48.dp)
                    .background(iconBg, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(26.dp))
                }
                Spacer(Modifier.weight(1f))
                Text(text = "0%", modifier = Modifier
                    .background(Color(0xFFDCFCE7), RoundedCornerShape(18.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF16A34A))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = title, color = Color.DarkGray, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        }
    }
}

@Composable
fun HorizontalScrollbar(state: LazyListState, modifier: Modifier = Modifier, trackColor: Color = Color(0xFFE5E7EB), thumbColor: Color = Color.Gray, height: androidx.compose.ui.unit.Dp = 4.dp) {
    val layoutInfo       = state.layoutInfo
    val visibleItemsInfo = layoutInfo.visibleItemsInfo
    val totalItems       = layoutInfo.totalItemsCount
    val canScroll        = state.canScrollForward || state.canScrollBackward

    if (totalItems == 0 || visibleItemsInfo.isEmpty() || !canScroll) {
        Box(modifier = modifier
            .fillMaxWidth()
            .height(height))
        return
    }

    val viewportSize              = (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset).toFloat()
    val averageItemSize           = visibleItemsInfo.sumOf { it.size }.toFloat() / visibleItemsInfo.size
    val estimatedTotalContentSize = averageItemSize * totalItems
    val thumbSizeFraction         = (viewportSize / estimatedTotalContentSize).coerceIn(0.1f, 1f)
    val scrolledPixels            = state.firstVisibleItemIndex * averageItemSize + state.firstVisibleItemScrollOffset
    val maxScrollPixels           = (estimatedTotalContentSize - viewportSize).coerceAtLeast(1f)
    val scrollFraction            = (scrolledPixels / maxScrollPixels).coerceIn(0f, 1f)

    BoxWithConstraints(modifier = modifier
        .fillMaxWidth()
        .height(height)
        .background(trackColor, RoundedCornerShape(height / 2))) {
        val trackWidth  = this@BoxWithConstraints.maxWidth
        val thumbWidth  = trackWidth * thumbSizeFraction
        val thumbOffset = (trackWidth - thumbWidth) * scrollFraction
        Box(modifier = Modifier
            .offset(x = thumbOffset)
            .width(thumbWidth)
            .height(height)
            .background(thumbColor, RoundedCornerShape(height / 2)))
    }
}

@Composable
fun StatusLegend(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier
            .size(18.dp)
            .background(color = color, shape = CircleShape))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = text, fontSize = 18.sp, color = Color.Black)
    }
}

fun String.toIsoDate(): String {
    if (this.isEmpty() || this == "Select Date") return ""
    return try {
        val parts = this.split("-")
        if (parts.size == 3) "${parts[2]}-${parts[1]}-${parts[0]}T00:00:00.000Z" else ""
    } catch (_: Exception) { "" }
}