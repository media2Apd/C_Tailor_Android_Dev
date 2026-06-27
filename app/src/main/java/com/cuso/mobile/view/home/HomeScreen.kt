package com.cuso.mobile.view.home

import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

import androidx.compose.foundation.horizontalScroll
import com.cuso.mobile.model.Organization
import com.cuso.mobile.model.Settings
import com.cuso.mobile.model.Subscription
import com.cuso.mobile.model.User
import com.cuso.mobile.view.sales.CreateOrderScreen
import com.cuso.mobile.view.sales.SalesOrderScreen


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

@Composable
fun HomeScreen(navController: NavController) {
    val viewModel: HomeViewModel = hiltViewModel()
    val isLoggedOut: Boolean by viewModel.isLoggedOut.collectAsStateWithLifecycle(initialValue = false)
    var currentScreen by remember { mutableStateOf("home") }
    var isDrawerOpen by remember { mutableStateOf(false) }

    // ✅ Track if we're in Sales Settings mode
    var isSalesSettingsMode by remember { mutableStateOf(false) }

    LaunchedEffect(isLoggedOut) {
        if (isLoggedOut) {
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    // ✅ Determine which panel to show
    val showHomePanel = currentScreen == "settings" ||
            currentScreen == "home_organization_profile" ||
            currentScreen == "home_branch_management" ||
            currentScreen == "home_department_teams" ||
            currentScreen == "home_designation"

    val showSalesPanel = isSalesSettingsMode

    Column(
        Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        TopNavBar(
            navController = navController,
            isSettingsOpen = showHomePanel || showSalesPanel,
            currentScreen = currentScreen,
            isDrawerOpen = isDrawerOpen,
            onDrawerToggle = { isDrawerOpen = !isDrawerOpen },
            onDrawerClose = { isDrawerOpen = false },
            onSettingsClick = {
                if (currentScreen == "sales_lead" || currentScreen == "create_lead" ||
                    currentScreen == "view_lead" || currentScreen == "edit_lead") {
                    // ✅ Enter Sales Settings mode
                    isSalesSettingsMode = true
                    currentScreen = "sales_settings"
                } else {
                    if (showHomePanel || showSalesPanel) {
                        // ✅ Close settings - go back to home
                        isSalesSettingsMode = false
                        currentScreen = "home"
                    } else {
                        // ✅ Open settings
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
                    // ✅ Fix: catch both possible route keys
                    route == "sales_sales_orders" || route == "sales_sales_&_orders" -> {
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
                        if (showHomePanel) {
                            currentScreen = "settings"
                        } else {
                            currentScreen = "home"
                        }
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
                        } catch (e: Exception) {
                            when {
                                route.startsWith("sales_") -> {
                                    currentScreen = route
                                }
                            }
                        }
                        isDrawerOpen = false
                    }
                }
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

        when (currentScreen) {
            // ✅ Settings Screen (your existing SettingsScreen)
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
                onMenuClick = { isDrawerOpen = true},
                onBack = { currentScreen = "settings" }
            )
            "home_designation" -> DesignationScreen(
                navController = navController,
                onMenuClick = { isDrawerOpen = true },
                onBack = { currentScreen = "settings" }
            )
            // ✅ Sales Settings Screen (your existing SalesSettingsScreen)
            "sales_settings" -> SalesSettingsScreen(
                navController = navController,
                onClose = {
                    isSalesSettingsMode = false
                    currentScreen = "sales_lead"
                },
                onMenuClick = { isDrawerOpen = true }
            )
            // ✅ Garment Type Screen (your existing GarmentTypeContent)
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
            "sales_customers" -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Customers Screen", fontSize = 18.sp, color = Color.Gray)
                }
            }
            "sales_measurements" -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Measurements Screen", fontSize = 18.sp, color = Color.Gray)
                }
            }
            "sales_sales_orders" -> {
                SalesOrderScreen(
                    navController = navController,
                    onCreateOrder = { currentScreen = "create_order" },
                    onBack = { currentScreen = "home" }
                )
            }

            "create_order" -> {
                CreateOrderScreen(
                    onBack = { currentScreen = "sales_sales_orders" },
                    onCancel = { currentScreen = "sales_sales_orders" },
                    onNextStep = { /* TODO */ }
                )
            }
            "sales_orders" -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Orders Screen", fontSize = 18.sp, color = Color.Gray)
                }
            }
            else -> { }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// TopNavBar
// ─────────────────────────────────────────────────────────────


@Composable
fun TopNavBar(
    navController: NavController,
    isSettingsOpen: Boolean = false,
    currentScreen: String = "home",
    isDrawerOpen: Boolean = false,
    onDrawerToggle: () -> Unit = {},
    onDrawerClose: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onMenuItemClick: (String) -> Unit = {},
    onLogout: () -> Unit,
    showHomePanel: Boolean = false,
    showSalesPanel: Boolean = false,
    isSalesSettingsMode: Boolean = false
) {
    val authViewModel: Authenticate = hiltViewModel()
    val userEntity by authViewModel.user.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }

    val user: User? = userEntity?.let {
        User(
            firstName = it.firstName,
            lastName = it.lastName,
            email = it.email,
            profilePicture = it.profilePicture.orEmpty(),
            organizationId = Organization(
                _id = it.organizationId?:"",
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

    Box(modifier = Modifier
        .fillMaxWidth()
        .wrapContentHeight()) {

        // ✅ Show SalesNavBar when settings is open OR sales panel mode
        if (isSettingsOpen || showSalesPanel) {
            // 📊 Sales Nav Bar - Only Home & Sales enabled
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
            // 🏠 Full Nav Bar - All menus enabled
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

        // ── TOP APP BAR ──
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(start = 30.dp, end = 30.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // ── Menu Button ──
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(lightGray, RoundedCornerShape(8.dp))
                        .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                        .clickable { onDrawerToggle() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Menu,
                        contentDescription = "Menu",
                        modifier = Modifier.size(22.dp),
                        tint = Color.Black
                    )
                }

                // ── Search Bar ──
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                        .border(0.5.dp, Color.LightGray, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                        singleLine = true,
                        decorationBox = { inner ->
                            Box(
                                Modifier.fillMaxSize(),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        "Search anything",
                                        color = Color.Gray,
                                        fontSize = 14.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                inner()
                            }
                        }
                    )
                }

                // ── Add Button ──
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(lightGray, RoundedCornerShape(8.dp))
                        .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add",
                        modifier = Modifier.size(24.dp),
                        tint = Color.Black
                    )
                }

                // ── Notifications ──
                IconButton(onClick = {}, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        modifier = Modifier.size(30.dp),
                        tint = Color.DarkGray
                    )
                }

                // ── Calendar ──
                IconButton(onClick = {}, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = "Calendar",
                        modifier = Modifier.size(30.dp),
                        tint = Color.DarkGray
                    )
                }

                // ── Settings/Close Button ──
                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isSettingsOpen) Icons.Default.Close else Icons.Filled.Settings,
                        contentDescription = if (isSettingsOpen) "Close Settings" else "Settings",
                        modifier = Modifier.size(30.dp),
                        tint = if (isSettingsOpen) Color.Red else Color.DarkGray
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = Color(0xFFF2F2F2))
        }
    }
}
// ─────────────────────────────────────────────────────────────
// HomeScreenContent
// ─────────────────────────────────────────────────────────────
@Composable
fun HomeScreenContent() {
    val leadItems = listOf(
        LeadItem("New Enquiry", 10f),
        LeadItem("Quoted", 7f),
        LeadItem("Follow-up", 5f),
        LeadItem("Converted", 12f),
        LeadItem("Last Enquiry", 3f)
    )
    val operationControls = listOf(ControlItem("Customer"), ControlItem("Type"), ControlItem("Measurements"), ControlItem("Priority"))
    val barColors = listOf(Color(0xFF6C63FF), Color(0xFF3B82F6), Color(0xFFF59E0B), Color(0xFF10B981), Color(0xFFEF4444))
    val labels = leadItems.map { it.header }
    val values = leadItems.map { it.value }

    val bottomAxisFormatter = CartesianValueFormatter { value, _, _ -> labels.getOrNull(value.toInt()) ?: "" }
    val modelProducer = remember { CartesianChartModelProducer() }
    LaunchedEffect(leadItems) { modelProducer.runTransaction { columnSeries { series(values) } } }

    val columnComponents = barColors.map { color ->
        rememberLineComponent(color = color, thickness = 24.dp, shape = VicoShape.rounded(allPercent = 20))
    }
    val columnProvider = remember(columnComponents) {
        object : ColumnCartesianLayer.ColumnProvider {
            override fun getColumn(entry: ColumnCartesianLayerModel.Entry, seriesIndex: Int, extraStore: ExtraStore): LineComponent =
                columnComponents[entry.x.toInt().mod(columnComponents.size)]
            override fun getWidestSeriesColumn(seriesIndex: Int, extraStore: ExtraStore): LineComponent = columnComponents[0]
        }
    }
    val markerLabel = rememberTextComponent(
        color = Color.White,
        background = rememberShapeComponent(color = Color(0xFF1E293B), shape = VicoShape.rounded(allPercent = 8)),
        padding = Dimensions(8f, 4f, 8f, 4f)
    )
    val marker = rememberDefaultCartesianMarker(
        label = markerLabel,
        valueFormatter = { _, targets ->
            targets.joinToString { t -> "${labels.getOrNull(t.x.toInt()) ?: ""}: ${values.getOrNull(t.x.toInt())?.toInt() ?: 0}" }
        }
    )
    val legendScrollState     = rememberLazyListState()
    val operationsScrollState = rememberLazyListState()

    Column(Modifier
        .fillMaxSize()
        .background(lightGray)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(lightGray),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    StatCard(Modifier.weight(1f), Color(0xFFDCFCE7), Icons.Filled.Money,        Color(0xFF16A34A), "Total Revenue",     "₹0")
                    StatCard(Modifier.weight(1f), Color(0xFFDBEAFE), Icons.Filled.ShoppingCart,  Color(0xFF2563EB), "Active Orders",     "0")
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    StatCard(Modifier.weight(1f), Color(0xFFDBEAFE), Icons.Filled.LinearScale,   Color(0xFF9333EA), "Measurements",      "0")
                    StatCard(Modifier.weight(1f), Color(0xFFDCFCE7), Icons.Filled.Money,         Color(0xFF16A34A), "Pending Payments",  "₹0")
                }
            }

            // Collection Efficiency
            item {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .padding(16.dp)) {
                    Column {
                        Text("Collection Efficiency", color = Color.Gray, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text("0%", color = Color.Black, fontSize = 50.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                        Row(Modifier.fillMaxWidth()) {
                            Column(Modifier.weight(1f)) {
                                Text("Total Invoiced", color = Color(0xFF6366F1), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text("₹0", modifier = Modifier.align(Alignment.End), fontSize = 16.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                            Column(Modifier.weight(1f)) {
                                Text("Payments Received", color = Color(0xFF22C55E), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text("₹0", modifier = Modifier.align(Alignment.End), fontSize = 16.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Box(Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFE0E0FC), RoundedCornerShape(12.dp))) { Text(" ") }
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier
                                .size(10.dp)
                                .background(Color(0xFFF97316), CircleShape))
                            Spacer(Modifier.width(8.dp))
                            Text("Pending Collection :", fontSize = 16.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Text("₹0", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text("Target 0%", color = Color.LightGray, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }

            // Lead Management Chart
            item {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .padding(16.dp)) {
                    Column(Modifier.fillMaxWidth()) {
                        Text("Lead Management", color = Color.Gray, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(16.dp))
                        CartesianChartHost(
                            chart = rememberCartesianChart(
                                rememberColumnCartesianLayer(columnProvider = columnProvider),
                                startAxis  = rememberStartAxis(guideline = null),
                                bottomAxis = rememberBottomAxis(valueFormatter = bottomAxisFormatter, guideline = null),
                                marker = marker
                            ),
                            modelProducer = modelProducer,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            leadItems.forEachIndexed { index, item ->
                                val color = barColors[index % barColors.size]
                                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(Modifier
                                        .size(12.dp)
                                        .background(color, CircleShape))
                                    Spacer(Modifier.height(4.dp))
                                    Text(text = item.header, color = Color.DarkGray, fontSize = 14.sp, maxLines = 1, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                    Spacer(Modifier.height(4.dp))
                                    Text(text = item.value.toInt().toString(), fontWeight = FontWeight.Bold, color = color, fontSize = 14.sp)
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        HorizontalScrollbar(state = legendScrollState, modifier = Modifier.padding(horizontal = 8.dp))
                    }
                }
            }

            // Invoicing vs Collection
            item {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp)
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .padding(16.dp)) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text("Invoicing vs. Collection", fontSize = 20.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2D3748))
                            Spacer(Modifier.weight(1f))
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End) {
                                Box(Modifier
                                    .size(10.dp)
                                    .background(Color(0xFF6C63FF), CircleShape))
                                Spacer(Modifier.width(6.dp))
                                Text("Invoiced", fontSize = 12.sp, color = Color.Black, maxLines = 1)
                                Spacer(Modifier.width(12.dp))
                                Box(Modifier
                                    .size(10.dp)
                                    .background(Color(0xFF34C759), CircleShape))
                                Spacer(Modifier.width(6.dp))
                                Text("Collected", fontSize = 12.sp, color = Color.Black, maxLines = 1)
                            }
                        }
                        Box(modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth())
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF5F5F5), RoundedCornerShape(16.dp))
                                .padding(horizontal = 20.dp, vertical = 18.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier
                                    .size(12.dp)
                                    .background(Color(0xFF6C63FF), CircleShape))
                                Spacer(Modifier.width(12.dp))
                                Text("Total 7d: ₹0", fontSize = 18.sp, color = Color(0xFF4A5568))
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier
                                    .size(12.dp)
                                    .background(Color(0xFF34C759), CircleShape))
                                Spacer(Modifier.width(12.dp))
                                Text("Collected: ₹0", fontSize = 18.sp, color = Color(0xFF34C759))
                            }
                        }
                    }
                }
            }

            // Operations Control
            item {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .padding(16.dp)) {
                    Column(Modifier.fillMaxWidth()) {
                        Text("Operations Control", fontSize = 25.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(16.dp))
                        LazyRow(state = operationsScrollState, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            items(operationControls) { item ->
                                Column(
                                    modifier = Modifier
                                        .width(200.dp)
                                        .background(Color(0xFFF8FAFC))
                                        .padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(text = item.controls, color = Color.DarkGray, maxLines = 1, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        HorizontalScrollbar(state = operationsScrollState, modifier = Modifier.padding(horizontal = 8.dp))
                    }
                }
            }

            // Order Status Distribution
            item {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(450.dp)
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .padding(16.dp)) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text("Order Status Distribution", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Spacer(modifier = Modifier.height(50.dp))
                        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(modifier = Modifier
                                .size(30.dp)
                                .background(Color(0xFF3F3CCF), CircleShape))
                            Spacer(modifier = Modifier.height(30.dp))
                            Text("0", fontSize = 64.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                            Text("TOTAL", fontSize = 28.sp, color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                StatusLegend(color = Color(0xFF3F3CCF), text = "In Progress (0)\n(0%)")
                                StatusLegend(color = Color(0xFF3FA66B), text = "Completed (0)\n(0%)")
                            }
                            StatusLegend(color = Color(0xFFD1D5DB), text = "Scheduled (0)\n(0%)")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// CreateLeadScreen
// ─────────────────────────────────────────────────────────────
@Composable
fun CreateLeadScreen(onBack: () -> Unit) {
    var leadSource       by remember { mutableStateOf("") }
    var enquiryDate      by remember { mutableStateOf("22-06-2026") }
    var leadOwner        by remember { mutableStateOf("Nithish Kumar - NIT-001") }
    var leadStatus       by remember { mutableStateOf("New Enquiry") }
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
    var appointmentRequired by remember { mutableStateOf(true) }
    var appointmentDate  by remember { mutableStateOf("") }
    var appointmentTime  by remember { mutableStateOf("01:04 PM") }
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
    val garmentCategories  by salesViewModel.garmentCategories.collectAsStateWithLifecycle()  // ✅

    val staffDisplayList   = staffList.map { "${it.firstName} ${it.lastName} - ${it.memberId}" }
    val staffIdMap         = staffList.associate { "${it.firstName} ${it.lastName} - ${it.memberId}" to it.id }
    val selectedStaffLabel = staffIdMap.entries.firstOrNull { it.value == assignedStaff }?.key ?: ""
    val statusOptions      = salesStatuses.map { it.name }
    val statusIdMap        = salesStatuses.associate { it.name to it.id }
    val garmentIdMap       = garmentCategories.associate { it.categoryId.categoryName to it.id }  // ✅
    val garmentOptions     = garmentCategories.map { it.categoryId.categoryName }                  // ✅


    LaunchedEffect(Unit) {
        salesViewModel.fetchStaff()
        salesViewModel.fetchGarmentCategories()  // ✅
    }

    LaunchedEffect(leadState) {
        if (leadState is SaleState.Success) {
            salesViewModel.resetLeadState()
            salesViewModel.fetchSalesData()
            onBack()
        }
    }

    Scaffold(
        bottomBar = {
            Column {
                HorizontalDivider(color = Color(0xFFF0F0F0))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("All required fields are filled", fontSize = 12.sp, color = Color(0xFF6B7280))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = onBack,
                            shape  = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFD1D5DB))
                        ) { Text("Cancel", color = Color(0xFF374151)) }
                        Button(
                            onClick = {
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
                                        gender = gender,        // ✅ This will now be sent
                                        dob = dob.toIsoDate()   // ✅ This will now be sent
                                    ),
                                    contact = LeadContact(
                                        address = address,      // ✅ This will now be sent
                                        area = areaZone,        // ✅ This will now be sent
                                        city = city,            // ✅ This will now be sent
                                        preferredContactMethod = preferredContact  // ✅ This will now be sent
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
                            },
                            enabled = leadState !is SaleState.Loading,
                            colors  = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B3BF9)),
                            shape   = RoundedCornerShape(8.dp)
                        ) {
                            if (leadState is SaleState.Loading) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Text("Save Lead", color = Color.White)
                            }
                        }
                    }
                }
                if (leadState is SaleState.Error) {
                    Text((leadState as SaleState.Error).message, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F7))
            .padding(padding)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", modifier = Modifier.clickable { onBack() }, tint = Color(0xFF111827))
                    Text("Create Lead", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                }
                Box(modifier = Modifier
                    .border(1.dp, Color(0xFF3B3BF9), RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)) {
                    Text("New Enquiry", fontSize = 12.sp, color = Color(0xFF3B3BF9))
                }
            }
            HorizontalDivider(color = Color(0xFFF0F0F0))

            LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                item {
                    FormCard {
                        SectionHeader(icon = Icons.Default.Description, title = "Lead Information", subtitle = "Basic details about this lead")
                        FormDropdown("Lead Source", leadSource.ifEmpty { "Select an option" }, leadSourceExpanded, { leadSourceExpanded = it }, leadSourceOptions, { leadSource = it })
                        Spacer(Modifier.height(14.dp))
                        FormLabel("Enquiry Date")
                        DatePickerField(value = enquiryDate.ifEmpty { "Select Date" }, onDateSelected = { enquiryDate = it })
                        Spacer(Modifier.height(14.dp))
                        FormDropdown("Lead Owner", leadOwner.ifEmpty { "Select an option" }, leadOwnerExpanded, { leadOwnerExpanded = it }, listOf("Nithish Kumar - NIT-001"), { leadOwner = it })
                        Spacer(Modifier.height(14.dp))
                        FormDropdown("Lead Status", leadStatus.ifEmpty { "Select an option" }, leadStatusExpanded, { leadStatusExpanded = it }, statusOptions, { leadStatus = it })
                    }
                }
                item {
                    FormCard {
                        SectionHeader(icon = Icons.Default.Person, title = "Customer Identity", subtitle = "Who is this lead for?")
                        Spacer(Modifier.padding(top = 10.dp))
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
                                        tint = if (isSelected) Color.Black else Color(0xFF6B7280)
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
                        FormLabel(if (customerType == "Corporate") "Company Name" else "Full Name")
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
                    FormCard {
                        SectionHeader(icon = Icons.Default.LocationOn, title = "Location & Communication", subtitle = "Contact details and preferences")
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
                    FormCard {
                        SectionHeader(icon = Icons.AutoMirrored.Filled.Assignment, title = "Enquiry Details", subtitle = "What are they looking for?")
                        FormDropdown("Enquiry Type", enquiryType.ifEmpty { "Select an option" }, enquiryTypeExpanded, { enquiryTypeExpanded = it }, enquiryTypeOptions, { enquiryType = it })
                        Spacer(Modifier.height(14.dp))
                        FormLabel("Estimated Quantity")
                        FormTextField(value = estimatedQuantity, onValueChange = { estimatedQuantity = it }, keyboardType = KeyboardType.Number)
                        Spacer(Modifier.height(14.dp))
                        FormLabel("Garment Category")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(garmentOptions) { option ->  // ✅ from API
                                val isSelected = garmentCategory == option
                                Box(
                                    modifier = Modifier
                                        .border(
                                            1.dp,
                                            if (isSelected) Color(0xFF3B3BF9) else Color(0xFFE5E7EB),
                                            RoundedCornerShape(50.dp)
                                        )
                                        .background(
                                            if (isSelected) Color(0xFFEEEEFE) else Color.White,
                                            RoundedCornerShape(50.dp)
                                        )
                                        .clickable {
                                            garmentCategory =
                                                if (garmentCategory == option) "" else option
                                        }
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text(option, fontSize = 13.sp, color = if (isSelected) Color(0xFF3B3BF9) else Color(0xFF374151), fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal)
                                }
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                        FormLabel("Budget Range")
                        Slider(value = budgetRange, onValueChange = { budgetRange = it }, valueRange = 1000f..250000f, modifier = Modifier.fillMaxWidth(), colors = SliderDefaults.colors(thumbColor = Color(0xFF3B3BF9), activeTrackColor = Color(0xFF3B3BF9), inactiveTrackColor = Color(0xFFE5E7EB)))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("₹1000", fontSize = 12.sp, color = Color(0xFF6B7280))
                            Text("₹${formatIndianNumber(budgetRange.toInt())}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3B3BF9))
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
                    FormCard {
                        SectionHeader(icon = Icons.Default.CalendarMonth, title = "Appointment & Follow-Up", subtitle = "Schedule interactions")
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Appointment Required?", fontSize = 14.sp, color = Color(0xFF374151))
                            Switch(checked = appointmentRequired, onCheckedChange = { appointmentRequired = it }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF3B3BF9)))
                        }
                        if (appointmentRequired) {
                            Spacer(Modifier.height(14.dp))
                            FormLabel("Appointment Date")
                            DatePickerField(value = appointmentDate.ifEmpty { "Select Date" }, onDateSelected = { appointmentDate = it })
                            Spacer(Modifier.height(14.dp))
                            FormLabel("Appointment Time")
                            TimePickerField(
                                value = appointmentTime,
                                onTimeSelected = { appointmentTime = it }
                            )
                            FormDropdown("Assigned Staff", selectedStaffLabel.ifEmpty { if (isLoadingStaff) "Loading staff..." else "Select an option" }, assignedStaffExpanded && !isLoadingStaff, { assignedStaffExpanded = it }, staffDisplayList, { label -> assignedStaff = staffIdMap[label] ?: "" })
                            Spacer(Modifier.height(14.dp))
                            FormLabel("Follow-up Date", isRequired = true)
                            DatePickerField(value = followUpDate.ifEmpty { "Select Date" }, onDateSelected = { followUpDate = it })
                            Spacer(Modifier.height(14.dp))
                            FormDropdown("Priority", priority.ifEmpty { "Select an option" }, priorityExpanded, { priorityExpanded = it }, priorityOptions, { priority = it }, isRequired = true)
                        }
                        Spacer(Modifier.height(14.dp))
                    }
                }
                item {
                    FormCard {
                        SectionHeader(icon = Icons.Default.Description, title = "Notes & References", subtitle = "Additional information and attachments")
                        FormLabel("Internal Notes")
                        OutlinedTextField(value = internalNotes, onValueChange = { internalNotes = it }, modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp), shape = RoundedCornerShape(8.dp), colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color(0xFFE5E7EB), focusedBorderColor = Color(0xFF3B3BF9), unfocusedContainerColor = Color.White, focusedContainerColor = Color.White))
                        Spacer(Modifier.height(14.dp))
                        FormLabel("Customer Notes")
                        OutlinedTextField(value = customerNotes, onValueChange = { customerNotes = it }, modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp), shape = RoundedCornerShape(8.dp), colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color(0xFFE5E7EB), focusedBorderColor = Color(0xFF3B3BF9), unfocusedContainerColor = Color.White, focusedContainerColor = Color.White))
                    }
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

    // State declarations
    val leads by salesViewModel.tableLeads.collectAsStateWithLifecycle()
    val isLoading by salesViewModel.isLoadingTableLeads.collectAsStateWithLifecycle()
    val tableError by salesViewModel.tableError.collectAsStateWithLifecycle()
    val deleteState by salesViewModel.deleteState.collectAsStateWithLifecycle()
    val updateState by salesViewModel.updateState.collectAsStateWithLifecycle()

    var actionMenuLeadId by remember { mutableStateOf<String?>(null) }
    var leadToDelete by remember { mutableStateOf<LeadTableItem?>(null) }
    var isLoadingEdit by remember { mutableStateOf(false) }
    var isLoadingView by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }

    // Scroll states
    val horizontalScrollState = rememberScrollState()

    // ✅ Fetch table data when screen opens
    LaunchedEffect(Unit) {
        salesViewModel.fetchTableLeads()
        salesViewModel.fetchStaff()
        salesViewModel.fetchGarmentCategories()
    }

    // ✅ Handle delete success with loading state - FIXED
    LaunchedEffect(deleteState) {
        // Capture the current state in a local variable to enable smart casting
        val currentState = deleteState
        when (currentState) {
            is SaleState.Loading -> {
                isDeleting = true
            }
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
            else -> {
                isDeleting = false
            }
        }
    }

    // ✅ Handle update success
    LaunchedEffect(updateState) {
        if (updateState is SaleState.Success) {
            salesViewModel.fetchTableLeads()
            salesViewModel.resetUpdateState()
        }
    }

    var filterExpanded by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("All Leads") }
    var isListView by remember { mutableStateOf(true) }
    var selectedIds by remember { mutableStateOf(setOf<String>()) }
    val filterOptions = listOf("All Leads", "New Enquiry", "Quoted", "Follow-up", "Converted")

    val itemsPerPage = 10
    var currentPage by remember { mutableIntStateOf(1) }

    val filteredLeads = if (selectedFilter == "All Leads") leads
    else leads.filter {
        val statusName = when (it.status) {
            is String -> it.status
            is Map<*, *> -> (it.status["name"] as? String) ?: ""
            else -> ""
        }
        statusName.contains(selectedFilter, ignoreCase = true)
    }

    val totalPages = maxOf(1, if (filteredLeads.isNotEmpty()) (filteredLeads.size + itemsPerPage - 1) / itemsPerPage else 1)
    val pagedLeads = if (filteredLeads.isNotEmpty()) {
        filteredLeads.drop((currentPage - 1) * itemsPerPage).take(itemsPerPage)
    } else {
        emptyList()
    }

    // Define column widths
    val checkboxWidth = 40.dp
    val customerWidth = 150.dp
    val enquiryWidth = 130.dp
    val garmentsWidth = 120.dp
    val qtyWidth = 80.dp
    val budgetWidth = 160.dp
    val dateWidth = 140.dp
    val sourceWidth = 120.dp
    val statusWidth = 140.dp
    val actionWidth = 80.dp

    Column(
        Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F7))
            .padding(16.dp)
    ) {
        // ── TOP HEADER BAR ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(10.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // LEFT SIDE: Filter Icon + "All Leads"
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Filter Icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFFF3F3F5), RoundedCornerShape(8.dp))
                        .clickable { filterExpanded = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.FilterAlt,
                        contentDescription = "Filter",
                        tint = Color(0xFF374151),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // "All Leads" with dropdown
                Box {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { filterExpanded = true }
                    ) {
                        Text(
                            text = selectedFilter,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowDown,
                            contentDescription = null,
                            tint = Color(0xFF111827),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = filterExpanded,
                        onDismissRequest = { filterExpanded = false },
                        containerColor = Color.White,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        filterOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option, color = Color(0xFF374151)) },
                                onClick = {
                                    selectedFilter = option
                                    currentPage = 1
                                    filterExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // RIGHT SIDE: List/Grid (tablet only) + Create Lead
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // List/Grid Toggle - Shows only on tablet (width > 600dp)
                androidx.compose.ui.platform.LocalConfiguration.current.run {
                    val isTablet = screenWidthDp > 600
                    if (isTablet) {
                        Row(
                            modifier = Modifier
                                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
                                .clip(RoundedCornerShape(8.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(if (isListView) Color(0xFFEEEEFE) else Color.White)
                                    .clickable { isListView = true }
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.List,
                                    contentDescription = "List View",
                                    tint = if (isListView) Color(0xFF3B3BF9) else Color(0xFF9CA3AF),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .background(if (!isListView) Color(0xFFEEEEFE) else Color.White)
                                    .clickable { isListView = false }
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GridView,
                                    contentDescription = "Grid View",
                                    tint = if (!isListView) Color(0xFF3B3BF9) else Color(0xFF9CA3AF),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                // Create Lead Button
                Button(
                    onClick = onCreateLead,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F27CE)),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text(
                        "Create Lead",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // ── Table container ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White, RoundedCornerShape(12.dp))
            ) {
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
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
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color.Red,
                                    modifier = Modifier.size(48.dp)
                                )
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
                    leads.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                Modifier.padding(40.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "No Leads Yet",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF111827)
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    "Start by creating your first lead",
                                    fontSize = 14.sp,
                                    color = Color(0xFF9CA3AF)
                                )
                                Spacer(Modifier.height(20.dp))
                                Button(
                                    onClick = onCreateLead,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F27CE)),
                                    shape = RoundedCornerShape(5.dp),
                                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                                ) {
                                    Text("Create Lead", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                    else -> {
                        // ── Table with horizontal scroll ──
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .horizontalScroll(horizontalScrollState)
                        ) {
                            // ── Table Header ──
                            Row(
                                modifier = Modifier
                                    .background(Color(0xFFF1F1F1))
                                    .padding(horizontal = 12.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.width(checkboxWidth)) {
                                    Checkbox(
                                        checked = pagedLeads.isNotEmpty() && pagedLeads.all { it.id in selectedIds },
                                        onCheckedChange = { checked ->
                                            selectedIds = if (checked) selectedIds + pagedLeads.map { it.id }
                                            else selectedIds - pagedLeads.map { it.id }.toSet()
                                        },
                                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF3B3BF9)),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Text("Customer",     modifier = Modifier.width(customerWidth), fontSize = 14.sp,fontWeight = FontWeight.Bold, color = Color.Black)
                                Text("Enquiry Type", modifier = Modifier.width(enquiryWidth),  fontSize = 14.sp,fontWeight = FontWeight.Bold, color = Color.Black)
                                Text("Garments",     modifier = Modifier.width(garmentsWidth), fontSize = 14.sp,fontWeight = FontWeight.Bold, color = Color.Black)
                                Text("Qty",          modifier = Modifier.width(qtyWidth),      fontSize = 14.sp,fontWeight = FontWeight.Bold, color = Color.Black)
                                Text("Budget Range", modifier = Modifier.width(budgetWidth),   fontSize = 14.sp,fontWeight = FontWeight.Bold, color = Color.Black)
                                Text("Required Date",modifier = Modifier.width(dateWidth),     fontSize = 14.sp,fontWeight = FontWeight.Bold, color = Color.Black)
                                Text("Source",       modifier = Modifier.width(sourceWidth),   fontSize = 14.sp,fontWeight = FontWeight.Bold, color = Color.Black)
                                Text("Status",       modifier = Modifier.width(statusWidth),   fontSize = 14.sp,fontWeight = FontWeight.Bold, color = Color.Black)
                                Text("Action",       modifier = Modifier.width(actionWidth),   fontSize = 14.sp,fontWeight = FontWeight.Bold, color = Color.Black)
                            }

                            HorizontalDivider(color = Color(0xFFF0F0F0))

                            // ── Table Body ──
                            Column {
                                pagedLeads.forEach { lead ->
                                    val isChecked = lead.id in selectedIds
                                    val statusName = when (lead.status) {
                                        is String -> lead.status
                                        is Map<*, *> -> (lead.status["name"] as? String) ?: ""
                                        else -> ""
                                    }

                                    val garmentName = run {
                                        val garment = lead.garmentCategory?.firstOrNull()
                                        if (garment == null) {
                                            "—"
                                        } else {
                                            when (garment) {
                                                is Map<*, *> -> {
                                                    val categoryId = garment["categoryId"] as? Map<*, *>
                                                    categoryId?.get("categoryName") as? String ?: "—"
                                                }
                                                is String -> {
                                                    lead.occasion?.takeIf { it.isNotBlank() } ?: "—"
                                                }
                                                else -> "—"
                                            }
                                        }
                                    }

                                    Row(
                                        modifier = Modifier
                                            .background(if (isChecked) Color(0xFFF5F5FF) else Color.White)
                                            .padding(horizontal = 12.dp, vertical = 16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(modifier = Modifier.width(checkboxWidth)) {
                                            Checkbox(
                                                checked = isChecked,
                                                onCheckedChange = { checked ->
                                                    selectedIds = if (checked) selectedIds + lead.id else selectedIds - lead.id
                                                },
                                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF3B3BF9)),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Text(lead.person.name, modifier = Modifier.width(customerWidth), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF111827), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(lead.enquiryType, modifier = Modifier.width(enquiryWidth), fontSize = 13.sp, color = Color(0xFF3B3BF9), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(garmentName, modifier = Modifier.width(garmentsWidth), fontSize = 13.sp, color = Color(0xFF374151), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(if (lead.estimatedQuantity == 0) "—" else lead.estimatedQuantity.toString(), modifier = Modifier.width(qtyWidth), fontSize = 13.sp, color = Color(0xFF374151))
                                        Text("₹${formatIndianNumber(lead.budgetRange.min)} - ₹${formatIndianNumber(lead.budgetRange.max)}", modifier = Modifier.width(budgetWidth), fontSize = 13.sp, color = Color(0xFF374151), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(formatLeadDate(lead.requiredDate ?: ""), modifier = Modifier.width(dateWidth), fontSize = 13.sp, color = Color(0xFF374151))
                                        Text(lead.source.ifEmpty { "—" }, modifier = Modifier.width(sourceWidth), fontSize = 13.sp, color = Color(0xFF374151), maxLines = 1, overflow = TextOverflow.Ellipsis)

                                        Box(modifier = Modifier.width(statusWidth)) {
                                            val (badgeText, badgeColor) = when {
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
                                            Box(
                                                modifier = Modifier
                                                    .border(
                                                        1.dp,
                                                        badgeColor,
                                                        RoundedCornerShape(20.dp)
                                                    )
                                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                                            ) {
                                                Text(badgeText, fontSize = 11.sp, color = badgeColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            }
                                        }

                                        // ✅ Action Menu with Delete
                                        Box(modifier = Modifier.width(actionWidth), contentAlignment = Alignment.Center) {
                                            Box {
                                                Icon(
                                                    imageVector = Icons.Default.MoreVert,
                                                    contentDescription = "More",
                                                    tint = Color(0xFF9CA3AF),
                                                    modifier = Modifier
                                                        .size(20.dp)
                                                        .clickable { actionMenuLeadId = lead.id }
                                                )
                                                DropdownMenu(
                                                    expanded = actionMenuLeadId == lead.id,
                                                    onDismissRequest = { actionMenuLeadId = null },
                                                    containerColor = Color.White,
                                                    shape = RoundedCornerShape(10.dp)
                                                ) {
                                                    // View
                                                    DropdownMenuItem(
                                                        text = {
                                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                                if (isLoadingView) {
                                                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                                                } else {
                                                                    Text("View", color = Color(0xFF374151))
                                                                }
                                                            }
                                                        },
                                                        enabled = !isLoadingView,
                                                        onClick = {
                                                            actionMenuLeadId = null
                                                            isLoadingView = true
                                                            salesViewModel.fetchLeadDetails(lead.id) { success ->
                                                                isLoadingView = false
                                                                if (success) {
                                                                    onViewLead()
                                                                } else {
                                                                    Toast.makeText(context, "Failed to load lead details", Toast.LENGTH_SHORT).show()
                                                                    val leadEntity = lead.toLeadEntity()
                                                                    salesViewModel.selectLead(leadEntity)
                                                                    onViewLead()
                                                                }
                                                            }
                                                        }
                                                    )

                                                    // Edit
                                                    DropdownMenuItem(
                                                        text = {
                                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                                if (isLoadingEdit) {
                                                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                                                } else {
                                                                    Text("Edit", color = Color(0xFF374151))
                                                                }
                                                            }
                                                        },
                                                        enabled = !isLoadingEdit,
                                                        onClick = {
                                                            actionMenuLeadId = null
                                                            isLoadingEdit = true
                                                            salesViewModel.fetchLeadDetails(lead.id) { success ->
                                                                isLoadingEdit = false
                                                                if (success) {
                                                                    onEditLead()
                                                                } else {
                                                                    Toast.makeText(context, "Failed to load lead details for editing", Toast.LENGTH_SHORT).show()
                                                                    val leadEntity = lead.toLeadEntity()
                                                                    salesViewModel.selectLead(leadEntity)
                                                                    onEditLead()
                                                                }
                                                            }
                                                        }
                                                    )


                                                    // ✅ Delete - with loading state
                                                    DropdownMenuItem(
                                                        text = {
                                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                                if (isDeleting) {
                                                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                                                } else {
                                                                    Text("Delete", color = Color(0xFF374151))
                                                                }
                                                            }
                                                        },
                                                        enabled = !isDeleting,
                                                        onClick = {
                                                            actionMenuLeadId = null
                                                            leadToDelete = lead
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    HorizontalDivider(color = Color(0xFFF5F5F5))
                                }
                            }
                        }

                        // ── Pagination Footer ──
                        HorizontalDivider(color = Color(0xFFF0F0F0))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Showing ${if (filteredLeads.isEmpty()) 0 else (currentPage - 1) * itemsPerPage + 1} - ${minOf(currentPage * itemsPerPage, filteredLeads.size)} of ${filteredLeads.size}",
                                fontSize = 13.sp,
                                color = Color(0xFF6B7280)
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Settings, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("10 per page", fontSize = 13.sp, color = Color(0xFF6B7280))
                                }
                                Spacer(Modifier.width(8.dp))
                                IconButton(
                                    onClick = { if (currentPage > 1) currentPage-- },
                                    enabled = currentPage > 1,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.ChevronLeft, contentDescription = "Previous", tint = if (currentPage > 1) Color(0xFF374151) else Color(0xFFD1D5DB))
                                }
                                Text("$currentPage - $totalPages", fontSize = 13.sp, color = Color(0xFF374151))
                                IconButton(
                                    onClick = { if (currentPage < totalPages) currentPage++ },
                                    enabled = currentPage < totalPages,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.ChevronRight, contentDescription = "Next", tint = if (currentPage < totalPages) Color(0xFF374151) else Color(0xFFD1D5DB))
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── Delete Confirmation Dialog ──
        if (leadToDelete != null) {
            AlertDialog(
                onDismissRequest = { leadToDelete = null },
                containerColor = Color.White,
                shape = RoundedCornerShape(12.dp),
                title = {
                    Text(
                        "Delete Lead",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                },
                text = {
                    Text(
                        "Are you sure you want to delete this lead? This action cannot be undone.",
                        color = Color(0xFF6B7280)
                    )
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
    }
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

    // Show loading state
    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F7)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(Modifier.height(8.dp))
                Text("Loading lead details...", color = Color.Gray, fontSize = 14.sp)
            }
        }
        return
    }

    // Show error state
    if (error != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F7)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color.Red,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Error loading lead",
                    color = Color.Red,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    error ?: "Unknown error",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { onBack() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B3BF9)),
                    shape = RoundedCornerShape(8.dp)
                ) {
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

    // ✅ Map garment IDs to names using garmentCategories
    val garmentNames = if (l.garments.isNotBlank() && garmentCategories.isNotEmpty()) {
        val ids = l.garments.split(",").filter { it.isNotBlank() }
        ids.mapNotNull { id ->
            garmentCategories.find { it.id == id }?.categoryId?.categoryName
        }
    } else if (l.garments.isNotBlank() && garmentCategories.isEmpty()) {
        // Fallback: show IDs if categories not loaded yet
        l.garments.split(",").filter { it.isNotBlank() }
    } else {
        emptyList()
    }

    Scaffold(
        bottomBar = {
            Column {
                HorizontalDivider(color = Color(0xFFF0F0F0))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onBack,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFD1D5DB))
                    ) {
                        Text("Back", color = Color(0xFF374151))
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            salesViewModel.fetchLeadDetails(l.id) { success ->
                                if (success) {
                                    onEditLead()
                                } else {
                                    Toast.makeText(context, "Failed to refresh lead data", Toast.LENGTH_SHORT).show()
                                    onEditLead()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B3BF9)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Edit Lead", color = Color.White)
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F7))
                .padding(padding)
        ) {
            // ── Top Bar ──
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier.clickable { onBack() },
                        tint = Color(0xFF111827)
                    )
                    Text(
                        "View Lead",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                }
                Box(
                    modifier = Modifier
                        .border(1.dp, Color(0xFF3B3BF9), RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        l.status.ifEmpty { "—" },
                        fontSize = 12.sp,
                        color = Color(0xFF3B3BF9)
                    )
                }
            }
            HorizontalDivider(color = Color(0xFFF0F0F0))

            // ── Form Fields (Read-Only) ──
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── Lead Information ──
                item {
                    FormCard {
                        SectionHeader(
                            Icons.Default.Description,
                            "Lead Information",
                            "Basic details about this lead"
                        )
                        Spacer(Modifier.height(8.dp))
                        ViewFieldValue("Lead Source", l.source.ifEmpty { "—" })
                        ViewFieldValue("Enquiry Date", formatLeadDate(l.enquiryDate))
                        ViewFieldValue("Lead Owner", "Nithish Kumar - NIT-001")
                        ViewFieldValue("Lead Status", l.status.ifEmpty { "—" })
                    }
                }

                // ── Customer Identity ──
                item {
                    FormCard {
                        SectionHeader(
                            Icons.Default.Person,
                            "Customer Identity",
                            "Who is this lead for?"
                        )
                        Spacer(Modifier.height(8.dp))

                        // Customer Type - Individual/Corporate toggle (read-only)
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
                                        tint = if (isSelected) Color.Black else Color(0xFF6B7280)
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

                        Spacer(Modifier.height(12.dp))
                        ViewFieldValue("Full Name", l.fullName.ifEmpty { "—" })
                        ViewFieldValue("Phone", l.phone.ifEmpty { "—" })
                        ViewFieldValue("Email", l.email.ifEmpty { "—" })
                        ViewFieldValue("Gender", l.gender.ifEmpty { "—" })
                        ViewFieldValue("Date of Birth", formatLeadDate(l.dob))
                    }
                }

                // ── Location & Communication ──
                item {
                    FormCard {
                        SectionHeader(
                            Icons.Default.LocationOn,
                            "Location & Communication",
                            "Contact details and preferences"
                        )
                        Spacer(Modifier.height(8.dp))
                        ViewFieldValue("Address", l.address.ifEmpty { "—" })
                        ViewFieldValue("Area / Zone", l.area.ifEmpty { "—" })
                        ViewFieldValue("City", l.city.ifEmpty { "—" })
                        ViewFieldValue("Preferred Contact Method", l.preferredContactMethod.ifEmpty { "—" })
                    }
                }

                // ── Enquiry Details ──
                item {
                    FormCard {
                        SectionHeader(
                            Icons.AutoMirrored.Filled.Assignment,
                            "Enquiry Details",
                            "What are they looking for?"
                        )
                        Spacer(Modifier.height(8.dp))
                        ViewFieldValue("Enquiry Type", l.enquiryType.ifEmpty { "—" })
                        ViewFieldValue("Estimated Quantity", if (l.estimatedQuantity == 0) "—" else l.estimatedQuantity.toString())

                        // ── Garment Category ── ✅ Shows names, not IDs
                        Column(modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)) {
                            Text(
                                "Garment Category",
                                fontSize = 12.sp,
                                color = Color(0xFF9CA3AF),
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(Modifier.height(4.dp))
                            if (garmentNames.isNotEmpty()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    garmentNames.forEach { garment ->
                                        Box(
                                            modifier = Modifier
                                                .border(
                                                    1.dp,
                                                    Color(0xFF3B3BF9),
                                                    RoundedCornerShape(50.dp)
                                                )
                                                .background(
                                                    Color(0xFFEEEEFE),
                                                    RoundedCornerShape(50.dp)
                                                )
                                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Default.Check,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(14.dp),
                                                    tint = Color(0xFF3B3BF9)
                                                )
                                                Spacer(Modifier.width(4.dp))
                                                Text(
                                                    garment,
                                                    fontSize = 13.sp,
                                                    color = Color(0xFF3B3BF9),
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                Text("—", fontSize = 14.sp, color = Color(0xFF111827))
                            }
                        }

                        // ── Budget Range ──
                        Column(modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)) {
                            Text(
                                "Budget Range",
                                fontSize = 12.sp,
                                color = Color(0xFF9CA3AF),
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "₹${formatIndianNumber(l.budgetMin)}  ₹${formatIndianNumber(l.budgetMax)}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF3B3BF9)
                            )
                            Spacer(Modifier.height(4.dp))
                            Slider(
                                value = l.budgetMin.toFloat(),
                                onValueChange = {},
                                valueRange = 1000f..250000f,
                                modifier = Modifier.fillMaxWidth(),
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFF3B3BF9),
                                    activeTrackColor = Color(0xFF3B3BF9),
                                    inactiveTrackColor = Color(0xFFE5E7EB)
                                ),
                                enabled = false
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("₹1000", fontSize = 12.sp, color = Color(0xFF6B7280))
                                Text("₹250000", fontSize = 12.sp, color = Color(0xFF6B7280))
                            }
                        }

                        ViewFieldValue("Required Date", formatLeadDate(l.requiredDate))
                        ViewFieldValue("Occasion", l.occasion.ifEmpty { "—" })
                    }
                }

                // ── Appointment & Follow-Up ──
                item {
                    FormCard {
                        SectionHeader(
                            Icons.Default.CalendarMonth,
                            "Appointment & Follow-Up",
                            "Schedule interactions"
                        )
                        Spacer(Modifier.height(8.dp))

                        // Appointment Required - Switch (read-only)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Appointment Required?", fontSize = 14.sp, color = Color(0xFF374151))
                            Switch(
                                checked = l.appointmentRequired,
                                onCheckedChange = {},
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF3B3BF9)
                                ),
                                enabled = false
                            )
                        }

                        if (l.appointmentRequired) {
                            Spacer(Modifier.height(12.dp))
                            ViewFieldValue("Appointment Date", formatLeadDate(l.appointmentDate))
                            ViewFieldValue("Appointment Time", l.appointmentTime.ifEmpty { "--:--" })
                            ViewFieldValue("Assigned Staff", "Select an option")
                            ViewFieldValue("Follow-up Date", formatLeadDate(l.followUpDate))
                            ViewFieldValue("Priority", l.priority.ifEmpty { "Select an option" })
                        }
                    }
                }

                // ── Notes & References ──
                item {
                    FormCard {
                        SectionHeader(
                            Icons.Default.Description,
                            "Notes & References",
                            "Additional information and attachments"
                        )
                        Spacer(Modifier.height(8.dp))
                        ViewFieldValue("Internal Notes", l.internalNotes.ifEmpty { "—" })
                        ViewFieldValue("Customer Notes", l.customerNotes.ifEmpty { "—" })
                    }
                }
            }
        }
    }
}


// ─────────────────────────────────────────────────────────────
// Helper Functions
// ─────────────────────────────────────────────────────────────

// ── Custom ViewFieldValue Component for Read-Only Display ──
@Composable
fun ViewFieldValue(label: String, value: String) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 6.dp)) {
        Text(
            label,
            fontSize = 12.sp,
            color = Color(0xFF9CA3AF),
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

    // Show loading state while fetching lead details
    if (isLoadingLead) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F7)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
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

    // Only fetch staff and categories if they are empty
    LaunchedEffect(Unit) {
        if (staffList.isEmpty()) {
            salesViewModel.fetchStaff()
        }
        if (garmentCategories.isEmpty()) {
            salesViewModel.fetchGarmentCategories()
        }
    }

    // ── State declarations ──
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

    // ✅ Multi-select garment categories
    var selectedGarmentCategories by remember { mutableStateOf<List<String>>(emptyList()) }
    var showGarmentError by remember { mutableStateOf(false) }

    // ── Dropdown expanded states ──
    var leadSourceExpanded by remember { mutableStateOf(false) }
    var leadStatusExpanded by remember { mutableStateOf(false) }
    var genderExpanded by remember { mutableStateOf(false) }
    var preferredContactExpanded by remember { mutableStateOf(false) }
    var enquiryTypeExpanded by remember { mutableStateOf(false) }
    var assignedStaffExpanded by remember { mutableStateOf(false) }
    var priorityExpanded by remember { mutableStateOf(false) }

    // ── Options ──
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

    // ✅ Initialize selected garment categories from lead data
    LaunchedEffect(l.garments, garmentCategories) {
        if (garmentCategories.isNotEmpty() && l.garments.isNotBlank()) {
            val ids = l.garments.split(",").filter { it.isNotBlank() }
            val names = ids.mapNotNull { id ->
                garmentCategories.find { it.id == id }?.categoryId?.categoryName
            }
            if (names.isNotEmpty()) {
                selectedGarmentCategories = names
            }
        }
    }

    // Set default garment category if empty and categories are loaded
    LaunchedEffect(garmentCategories) {
//        if (garmentCategories.isNotEmpty() && selectedGarmentCategories.isEmpty() && l.garments.isBlank()) {
//            // Only set default if no garments are selected and none exist in the data
//            // Don't auto-select - let user choose
//        }
    }

    // ✅ Handle update success/failure
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

    // In EditLeadScreen.kt - Update validateAndUpdate function

    fun validateAndUpdate() {
        // Check if at least one garment is selected
        if (selectedGarmentCategories.isEmpty()) {
            showGarmentError = true
            return
        }

        // Get the IDs for all selected garments
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
            person = LeadPerson(
                name = fullName,
                phone = phone,
                email = email,
                gender = gender,           // ✅ This will now be sent
                dob = dob.toIsoDate()      // ✅ This will now be sent
            ),
            contact = LeadContact(
                address = address,         // ✅ This will now be sent
                area = areaZone,           // ✅ This will now be sent
                city = city,               // ✅ This will now be sent
                preferredContactMethod = preferredContact  // ✅ This will now be sent
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
                // ✅ Ensure both notes are always sent
                if (internalNotes.isBlank() && customerNotes.isBlank()) {
                    add(LeadNote("-", "internal"))
                }
            },
            occasion = l.occasion
        )

        salesViewModel.updateLeadById(l.id, request)
    }

    // ✅ Get current state for error display
    val currentUpdateState = updateState

    Scaffold(
        bottomBar = {
            Column {
                HorizontalDivider(color = Color(0xFFF0F0F0))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 10.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Edit and save your changes", fontSize = 12.sp, color = Color(0xFF6B7280))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = onBack,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFD1D5DB))
                        ) { Text("Cancel", color = Color(0xFF374151)) }

                        Button(
                            onClick = { validateAndUpdate() },
                            enabled = currentUpdateState !is SaleState.Loading && selectedGarmentCategories.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B3BF9)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            if (currentUpdateState is SaleState.Loading) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Text("Update Lead", color = Color.White)
                            }
                        }
                    }
                }
                if (showGarmentError) {
                    Text(
                        "Please select at least one garment category",
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
                if (currentUpdateState is SaleState.Error) {
                    Text(
                        currentUpdateState.message,
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F7))
                .padding(padding)
        ) {
            // ── Top Bar ──
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier.clickable { onBack() },
                        tint = Color(0xFF111827)
                    )
                    Text("Edit Lead", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                }
                Box(
                    modifier = Modifier
                        .border(1.dp, Color(0xFF3B3BF9), RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(leadStatus, fontSize = 12.sp, color = Color(0xFF3B3BF9))
                }
            }
            HorizontalDivider(color = Color(0xFFF0F0F0))

            // ── Form Fields ──
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── Lead Information ──
                item {
                    FormCard {
                        SectionHeader(
                            Icons.Default.Description,
                            "Lead Information",
                            "Basic details about this lead"
                        )
                        FormDropdown(
                            "Lead Source",
                            leadSource.ifEmpty { "Select an option" },
                            leadSourceExpanded,
                            { leadSourceExpanded = it },
                            leadSourceOptions,
                            { leadSource = it }
                        )
                        Spacer(Modifier.height(14.dp))
                        FormLabel("Enquiry Date")
                        DatePickerField(
                            value = enquiryDate.ifEmpty { "Select Date" },
                            onDateSelected = { enquiryDate = it }
                        )
                        Spacer(Modifier.height(14.dp))
                        FormDropdown(
                            "Lead Status",
                            leadStatus.ifEmpty { "Select an option" },
                            leadStatusExpanded,
                            { leadStatusExpanded = it },
                            statusOptions,
                            { leadStatus = it }
                        )
                    }
                }

                // ── Customer Identity ──
                item {
                    FormCard {
                        SectionHeader(
                            Icons.Default.Person,
                            "Customer Identity",
                            "Who is this lead for?"
                        )
                        Spacer(Modifier.padding(top = 10.dp))
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
                                        tint = if (isSelected) Color.Black else Color(0xFF6B7280)
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
                        FormLabel("Full Name")
                        FormTextField(value = fullName, onValueChange = { fullName = it })
                        Spacer(Modifier.height(14.dp))
                        PhoneInputField(
                            phoneValue = phone,
                            onPhoneChange = { phone = it },
                            onCountryChange = {}
                        )
                        Spacer(Modifier.height(14.dp))
                        FormLabel("Email")
                        FormTextField(value = email, onValueChange = { email = it })
                        Spacer(Modifier.height(14.dp))
                        FormDropdown(
                            "Gender",
                            gender.ifEmpty { "Select an option" },
                            genderExpanded,
                            { genderExpanded = it },
                            genderOptions,
                            { gender = it }
                        )
                        Spacer(Modifier.height(14.dp))
                        FormLabel("Date of Birth")
                        DatePickerField(
                            value = dob.ifEmpty { "Select Date" },
                            onDateSelected = { dob = it }
                        )
                    }
                }

                // ── Location & Communication ──
                item {
                    FormCard {
                        SectionHeader(
                            Icons.Default.LocationOn,
                            "Location & Communication",
                            "Contact details"
                        )
                        FormLabel("Address")
                        FormTextField(value = address, onValueChange = { address = it })
                        Spacer(Modifier.height(14.dp))
                        FormLabel("Area / Zone")
                        FormTextField(value = areaZone, onValueChange = { areaZone = it })
                        Spacer(Modifier.height(14.dp))
                        FormLabel("City")
                        FormTextField(value = city, onValueChange = { city = it })
                        Spacer(Modifier.height(14.dp))
                        FormDropdown(
                            "Preferred Contact Method",
                            preferredContact.ifEmpty { "Select an option" },
                            preferredContactExpanded,
                            { preferredContactExpanded = it },
                            preferredContactOptions,
                            { preferredContact = it }
                        )
                    }
                }

                // ── Enquiry Details ──
                item {
                    FormCard {
                        SectionHeader(
                            Icons.AutoMirrored.Filled.Assignment,
                            "Enquiry Details",
                            "What are they looking for?"
                        )
                        FormDropdown(
                            "Enquiry Type",
                            enquiryType.ifEmpty { "Select an option" },
                            enquiryTypeExpanded,
                            { enquiryTypeExpanded = it },
                            enquiryTypeOptions,
                            { enquiryType = it }
                        )
                        Spacer(Modifier.height(14.dp))
                        FormLabel("Estimated Quantity")
                        FormTextField(
                            value = estimatedQuantity,
                            onValueChange = { estimatedQuantity = it },
                            keyboardType = KeyboardType.Number
                        )
                        Spacer(Modifier.height(14.dp))

                        // ── Garment Category Section (Multi-Select) ──
                        Column {
                            Row {
                                Text(
                                    text = "Garment Categories",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (showGarmentError) Color.Red else Color.Gray
                                )
                                Text(
                                    text = " *",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Red
                                )
                                Text(
                                    text = " (Select one or more)",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
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
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                        Text("Loading categories...", fontSize = 14.sp, color = Color(0xFF6B7280))
                                    }
                                }
                            } else {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(garmentOptions) { option ->
                                        val isSelected = selectedGarmentCategories.contains(option)
                                        Box(
                                            modifier = Modifier
                                                .border(
                                                    1.dp,
                                                    if (isSelected) Color(0xFF3B3BF9) else Color(
                                                        0xFFE5E7EB
                                                    ),
                                                    RoundedCornerShape(50.dp)
                                                )
                                                .background(
                                                    if (isSelected) Color(0xFFEEEEFE) else Color.White,
                                                    RoundedCornerShape(50.dp)
                                                )
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
                                                    Icon(
                                                        Icons.Default.Check,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(14.dp),
                                                        tint = Color(0xFF3B3BF9)
                                                    )
                                                    Spacer(Modifier.width(4.dp))
                                                }
                                                Text(
                                                    option,
                                                    fontSize = 13.sp,
                                                    color = if (isSelected) Color(0xFF3B3BF9) else Color(0xFF374151),
                                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                                )
                                            }
                                        }
                                    }
                                }
                                // Show selected count
                                if (selectedGarmentCategories.isNotEmpty()) {
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "Selected: ${selectedGarmentCategories.joinToString(", ")}",
                                        fontSize = 12.sp,
                                        color = Color(0xFF6B7280)
                                    )
                                }
                            }

                            if (showGarmentError) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Please select at least one garment category",
                                    fontSize = 12.sp,
                                    color = Color.Red
                                )
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
                                thumbColor = Color(0xFF3B3BF9),
                                activeTrackColor = Color(0xFF3B3BF9),
                                inactiveTrackColor = Color(0xFFE5E7EB)
                            )
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("₹1000", fontSize = 12.sp, color = Color(0xFF6B7280))
                            Text(
                                "₹${formatIndianNumber(budgetRange.toInt())}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF3B3BF9)
                            )
                            Text("₹250000", fontSize = 12.sp, color = Color(0xFF6B7280))
                        }
                        Spacer(Modifier.height(14.dp))
                        FormLabel("Required Date")
                        DatePickerField(
                            value = requiredDate.ifEmpty { "Select Date" },
                            onDateSelected = { requiredDate = it }
                        )
                    }
                }

                // ── Appointment & Follow-Up ──
                item {
                    FormCard {
                        SectionHeader(
                            Icons.Default.CalendarMonth,
                            "Appointment & Follow-Up",
                            "Schedule interactions"
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Appointment Required?", fontSize = 14.sp, color = Color(0xFF374151))
                            Switch(
                                checked = appointmentRequired,
                                onCheckedChange = { appointmentRequired = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF3B3BF9)
                                )
                            )
                        }
                        if (appointmentRequired) {
                            Spacer(Modifier.height(14.dp))
                            FormLabel("Appointment Date")
                            DatePickerField(
                                value = appointmentDate.ifEmpty { "Select Date" },
                                onDateSelected = { appointmentDate = it }
                            )
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
                                Icon(
                                    Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            FormDropdown(
                                "Assigned Staff",
                                selectedStaffLabel.ifEmpty { if (isLoadingStaff) "Loading..." else "Select an option" },
                                assignedStaffExpanded && !isLoadingStaff,
                                { assignedStaffExpanded = it },
                                staffDisplayList,
                                { label -> assignedStaff = staffIdMap[label] ?: "" }
                            )
                            Spacer(Modifier.height(14.dp))
                            FormLabel("Follow-up Date")
                            DatePickerField(
                                value = followUpDate.ifEmpty { "Select Date" },
                                onDateSelected = { followUpDate = it }
                            )
                            Spacer(Modifier.height(14.dp))
                            FormDropdown(
                                "Priority",
                                priority.ifEmpty { "Select an option" },
                                priorityExpanded,
                                { priorityExpanded = it },
                                priorityOptions,
                                { priority = it }
                            )
                        }
                        Spacer(Modifier.height(14.dp))
                    }
                }

                // ── Notes & References ──
                item {
                    FormCard {
                        SectionHeader(
                            Icons.Default.Description,
                            "Notes & References",
                            "Additional information"
                        )
                        FormLabel("Internal Notes")
                        OutlinedTextField(
                            value = internalNotes,
                            onValueChange = { internalNotes = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color(0xFFE5E7EB),
                                focusedBorderColor = Color(0xFF3B3BF9),
                                unfocusedContainerColor = Color.White,
                                focusedContainerColor = Color.White
                            )
                        )
                        Spacer(Modifier.height(14.dp))
                        FormLabel("Customer Notes")
                        OutlinedTextField(
                            value = customerNotes,
                            onValueChange = { customerNotes = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color(0xFFE5E7EB),
                                focusedBorderColor = Color(0xFF3B3BF9),
                                unfocusedContainerColor = Color.White,
                                focusedContainerColor = Color.White
                            )
                        )
                    }
                }
            }
        }
    }
}


// "2026-03-31T00:00:00.000Z" or "2026-03-31" → "31-03-2026"
fun formatLeadDate(raw: String): String {
    if (raw.isBlank()) return "—"
    return try {
        val datePart = raw.take(10)          // "2026-03-31"
        val parts = datePart.split("-")      // [2026, 03, 31]
        if (parts.size == 3) "${parts[2]}-${parts[1]}-${parts[0]}" else raw
    } catch (e: Exception) { raw }
}

// 250000 → "2,50,000"  (Indian numbering)
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

    // Parse initial time
    var selectedHour by remember(value) {
        mutableIntStateOf(
            if (value.isNotEmpty()) {
                try {
                    val hourStr = value.substringBefore(":").trim()
                    val hour = hourStr.toInt()
                    if (value.contains("PM", ignoreCase = true) && hour != 12) hour + 12
                    else if (value.contains("AM", ignoreCase = true) && hour == 12) 0
                    else hour
                } catch (e: Exception) { 10 }
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
                } catch (e: Exception) { 53 }
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

    // Display row
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

    // Custom Time Picker Dialog
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
                        contentColor = Color(0xFF3B3BF9)
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
    // Generate hour options (1-12)
    val hourOptions = (1..12).toList()
    // Generate minute options (00-59)
    val minuteOptions = (0..59).map { String.format("%02d", it) }

    // Current display values
    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    val displayMinute = String.format("%02d", minute)

    // Scroll states for hour and minute pickers
    val hourScrollState = rememberLazyListState(
        initialFirstVisibleItemIndex = hourOptions.indexOf(displayHour).coerceAtLeast(0)
    )
    val minuteScrollState = rememberLazyListState(
        initialFirstVisibleItemIndex = minuteOptions.indexOf(displayMinute).coerceAtLeast(0)
    )

    // Detect which item is centered
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

    // Update selected values when center changes
    LaunchedEffect(hourCenterIndex) {
        if (hourCenterIndex in hourOptions.indices) {
            val newHour = hourOptions[hourCenterIndex]
            // Convert display hour back to 24-hour format
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
        // ── Hour Picker ──
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Hour",
                fontSize = 12.sp,
                color = Color(0xFF9CA3AF),
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(4.dp))

            // Highlighted center background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                // Center highlight
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .align(Alignment.Center)
                        .background(Color(0xFFF0F0FF), RoundedCornerShape(8.dp))
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
                                color = if (isSelected) Color(0xFF3B3BF9) else Color(0xFF6B7280)
                            )
                        }
                    }
                }
            }
        }

        // ── Colon Separator ──
        Text(
            ":",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF111827),
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        // ── Minute Picker ──
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Minute",
                fontSize = 12.sp,
                color = Color(0xFF9CA3AF),
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                // Center highlight
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .align(Alignment.Center)
                        .background(Color(0xFFF0F0FF), RoundedCornerShape(8.dp))
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
                                color = if (isSelected) Color(0xFF3B3BF9) else Color(0xFF6B7280)
                            )
                        }
                    }
                }
            }
        }

        // ── AM/PM Toggle ──
        Column(
            modifier = Modifier
                .weight(0.8f)
                .padding(start = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "AM/PM",
                fontSize = 12.sp,
                color = Color(0xFF9CA3AF),
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
                // AM Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isAm) Color(0xFF3B3BF9) else Color.Transparent)
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

                // PM Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (!isAm) Color(0xFF3B3BF9) else Color.Transparent)
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
// ── Table cell helpers ──
@Composable
fun LeadTableCell(text: String, modifier: Modifier, bold: Boolean = false, color: Color = Color(0xFF374151)) {
    Text(
        text = text,
        modifier = modifier,
        fontSize = 13.sp,
        fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal,
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}


@Composable
fun SectionHeader(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(modifier = Modifier
            .size(40.dp)
            .background(Color(0xFFF3F4F6), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
            Icon(imageVector = icon, contentDescription = null, tint = Color(0xFF374151), modifier = Modifier.size(20.dp))
        }
        Column {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
            Text(subtitle, fontSize = 13.sp, color = Color(0xFF9CA3AF))
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
    Row { Text(text = text, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.Gray) }
    Spacer(Modifier.height(6.dp))
}

@Composable
fun FormTextField(value: String, onValueChange: (String) -> Unit, keyboardType: KeyboardType = KeyboardType.Text) {
    BasicTextField(
        value = value, onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 14.dp),
        singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
    )
}

@Composable
fun FormDateField(value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(value, fontSize = 14.sp, color = if (value == "Select Date") Color(0xFF9CA3AF) else Color(0xFF374151))
        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(value: String, onDateSelected: (String) -> Unit) {
    var showPicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    FormDateField(value = value, onClick = { showPicker = true })
    if (showPicker) {
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val calendar = java.util.Calendar.getInstance().apply { timeInMillis = millis }
                        val day   = calendar.get(java.util.Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
                        val month = (calendar.get(java.util.Calendar.MONTH) + 1).toString().padStart(2, '0')
                        val year  = calendar.get(java.util.Calendar.YEAR).toString()
                        onDateSelected("$day-$month-$year")
                    }
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showPicker = false }) { Text("Cancel") } },
            colors = DatePickerDefaults.colors(containerColor = Color.White)
        ) {
            DatePicker(state = datePickerState, colors = DatePickerDefaults.colors(selectedDayContainerColor = Color(0xFF3B3BF9), todayDateBorderColor = Color(0xFF3B3BF9), todayContentColor = Color(0xFF3B3BF9)))
        }
    }
}

@Composable
fun FormDropdown(label: String, value: String, expanded: Boolean, onExpandChange: (Boolean) -> Unit, options: List<String>, onOptionSelected: (String) -> Unit, isRequired: Boolean = false) {
    FormLabel(label, isRequired)
    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp))
                .clickable { onExpandChange(true) }
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(value, fontSize = 14.sp, color = if (value == "Select an option") Color(0xFF9CA3AF) else Color(0xFF374151))
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.Gray)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { onExpandChange(false) }, containerColor = Color.White, shape = RoundedCornerShape(10.dp)) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option, color = Color(0xFF374151)) }, onClick = { onOptionSelected(option); onExpandChange(false) })
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
    } catch (e: Exception) { "" }
}

//// Add this helper at the bottom of the file
//fun mapLeadStatusToApiCode(displayStatus: String): String {
//    return when (displayStatus) {
//        "New Enquiry"        -> "NEW"
//        "Quoted"             -> "QUOTED"
//        "Follow-up Pending"  -> "FOLLOW_UP"
//        "Converted to Order" -> "CONVERTED"
//        "Lost Enquiry"       -> "LOST"
//        else                 -> displayStatus
//    }
//}