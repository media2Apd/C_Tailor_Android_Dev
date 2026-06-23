package com.cuso.mobile.view.home

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.cuso.mobile.R
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Rect
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

// ── Data classes ──
data class LeadItem(
    val header: String,
    val value: Float
)

data class LeadListItem(
    val id: String,
    val customerName: String,
    val enquiryType: String,
    val garments: String,
    val qty: String,
    val budgetRange: String,
    val requiredDate: String,
    val source: String,
    val status: String
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

    LaunchedEffect(isLoggedOut) {
        if (isLoggedOut) {
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        TopNavBar(
            navController = navController,
            isSettingsOpen = currentScreen == "settings",
            onSettingsClick = {
                currentScreen = if (currentScreen == "settings") "home" else "settings"
            },
            onMenuItemClick = { screen -> currentScreen = screen },
            onLogout = {
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            }
        )

        when (currentScreen) {
            "settings"    -> SettingsScreen(navController)
            "home"        -> HomeScreenContent()
            "sales_lead"  -> LeadScreenContent(
                onCreateLead = { currentScreen = "create_lead" },
                onViewLead   = { currentScreen = "view_lead" },
                onEditLead   = { currentScreen = "edit_lead" }
            )
            "create_lead" -> CreateLeadScreen(
                onBack = { currentScreen = "sales_lead" }
            )
            "view_lead"   -> ViewLeadScreen(
                onBack     = { currentScreen = "sales_lead" },
                onEditLead = { currentScreen = "edit_lead" }
            )
            "edit_lead"   -> EditLeadScreen(
                onBack = { currentScreen = "sales_lead" }
            )
            "sales"       -> { }
            "marketing"   -> { }
            else          -> { }
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
    onSettingsClick: () -> Unit = {},
    onMenuItemClick: (String) -> Unit = {},
    onLogout: () -> Unit
) {
    val salesViewModel: SalesViewModel = hiltViewModel()
    val authViewModel: Authenticate = hiltViewModel()
    var isDrawerOpen by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedMenu by remember { mutableStateOf("Home") }

    val user by authViewModel.user.collectAsStateWithLifecycle()
    val fetchState by salesViewModel.fetchState.collectAsStateWithLifecycle()
    var menuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(fetchState) {
        when (fetchState) {
            is SaleState.Error -> { /* Toast or Snackbar if needed */ }
            else -> Unit
        }
    }

    val menuItems = listOf(
        Pair(R.drawable.home, "Home"),
        Pair(R.drawable.sales, "Sales"),
        Pair(R.drawable.marketing, "Marketing"),
        Pair(R.drawable.finance, "Finance"),
        Pair(R.drawable.inventory, "Inventory"),
        Pair(R.drawable.logistics, "Logistics"),
        Pair(R.drawable.services, "Services"),
        Pair(R.drawable.hr, "HR"),
        Pair(R.drawable.it, "IT"),
        Pair(R.drawable.legal, "Legal"),
        Pair(R.drawable.security, "Security"),
        Pair(R.drawable.reports, "Reports"),
    )

    val salesCategories     = listOf("Lead Management","Customer","Measurements","Sales & Orders","Order Management","Pricing & Quotations","Targets vs Achievements","Salesperson Analytics")
    val marketingCategories = listOf("Website","Campaigns","Leads & Audience","Engagement","Growth","Pages","Budget","Team")
    val financeCategories   = listOf("Accounts Receivable","Accounts Payable","Expenses","Finance Core")
    val inventoryCategories = listOf("Items","Procurement","Payables")
    val logisticsCategories = listOf("Delivery","Returns")
    val servicesCategories  = listOf("Service Request","Alteration Management","Return","Damaged Goods","Customer Feedback")
    val hrCategories        = listOf("Employees")
    val itCategories        = listOf("Integrations")
    val legalCategories     = listOf("Legal Management")
    val securityCategories  = listOf("Access Control","Auth & Verification","Monitoring & Audit")
    val reportsCategories   = listOf("Sales Reports","Finance Reports")

    val salesSubItems     = mapOf("Lead Management" to listOf("Lead"),"Customer" to listOf("Customers"),"Measurements" to listOf("Measurements"),"Sales & Orders" to listOf("Sales & Orders"),"Order Management" to listOf("Orders"),"Pricing & Quotations" to listOf("Overview","Pricing & Quotations"),"Targets vs Achievements" to listOf("Targets vs Achievements"),"Salesperson Analytics" to listOf("Salesperson Analytics"))
    val marketingSubItems = mapOf("Campaigns" to listOf("Campaigns","Promotions","Marketing & Calendar"),"Leads & Audience" to listOf("Lead Generation","Customer Segmentation"),"Engagement" to listOf("Customer Engagement","WhatsApp","Social Media","Review & Feedback"),"Growth" to listOf("Referral Program","Influencer"),"Pages" to listOf("Landing Page"),"Budget" to listOf("Marketing Budget"),"Team" to listOf("Marketing Tasks","Team Management"))
    val financeSubItems   = mapOf("Accounts Receivable" to listOf("Customers","Sales Invoices","Payments Received"),"Accounts Payable" to listOf("Suppliers","Purchase Invoices","Payments Mode"),"Expenses" to listOf("Expenses"),"Finance Core" to listOf("Chart of Accounts","Journal Entries","Trial Balance"))
    val inventorySubItems = mapOf("Items" to listOf("All Items","Item Groups"),"Procurement" to listOf("Suppliers","Requisitions","Orders","Goods Receipt"),"Payables" to listOf("Invoices","Payments","Credits"))
    val logisticsSubItems = mapOf("Delivery" to listOf("Delivery"),"Returns" to listOf("Returns"))
    val servicesSubItems  = mapOf("Service Request" to listOf("Service Request"),"Alteration Management" to listOf("Alteration Management"),"Return" to listOf("Return"),"Damaged Goods" to listOf("Damaged Goods"),"Customer Feedback" to listOf("Customer Feedback"))
    val hrSubItems        = mapOf("Employees" to listOf("All Employees"))
    val itSubItems        = mapOf("Integrations" to listOf("API Integration"))
    val legalSubItems     = mapOf("Legal Management" to listOf("Legal Documents"))
    val securitySubItems  = mapOf("Access Control" to listOf("User Accounts","Roles & Permissions"),"Auth & Verification" to listOf("Multi Factor (MFA)","SSO Settings"),"Monitoring & Audit" to listOf("Login Logs","Activity Logs"))
    val reportsSubItems   = mapOf("Sales Reports" to listOf("Sales Reports"),"Finance Reports" to listOf("Finance Reports"))

    var expandedCategory by remember { mutableStateOf<String?>(null) }
    var selectedSubItem  by remember { mutableStateOf<String?>(null) }

    val panelMenus = setOf("Sales","Marketing","Finance","Inventory","Logistics","Services","HR","IT","Legal","Security","Reports")
    val isPanelMode = selectedMenu in panelMenus

    val activeCategories: List<String> = when (selectedMenu) {
        "Sales"     -> salesCategories
        "Marketing" -> marketingCategories
        "Finance"   -> financeCategories
        "Inventory" -> inventoryCategories
        "Logistics" -> logisticsCategories
        "Services"  -> servicesCategories
        "HR"        -> hrCategories
        "IT"        -> itCategories
        "Legal"     -> legalCategories
        "Security"  -> securityCategories
        "Reports"   -> reportsCategories
        else        -> emptyList()
    }

    val activeSubItems: Map<String, List<String>> = when (selectedMenu) {
        "Sales"     -> salesSubItems
        "Marketing" -> marketingSubItems
        "Finance"   -> financeSubItems
        "Inventory" -> inventorySubItems
        "Logistics" -> logisticsSubItems
        "Services"  -> servicesSubItems
        "HR"        -> hrSubItems
        "IT"        -> itSubItems
        "Legal"     -> legalSubItems
        "Security"  -> securitySubItems
        "Reports"   -> reportsSubItems
        else        -> emptyMap()
    }

    fun handleMenuClick(label: String) {
        selectedMenu = label
        expandedCategory = null

        if (label in panelMenus) {
            if (label == "Sales") salesViewModel.fetchSalesData()
            isDrawerOpen = true

            val firstCategory = when (label) {
                "Sales"     -> salesCategories.firstOrNull()
                "Marketing" -> marketingCategories.firstOrNull()
                "Finance"   -> financeCategories.firstOrNull()
                "Inventory" -> inventoryCategories.firstOrNull()
                "Logistics" -> logisticsCategories.firstOrNull()
                "Services"  -> servicesCategories.firstOrNull()
                "HR"        -> hrCategories.firstOrNull()
                "IT"        -> itCategories.firstOrNull()
                "Legal"     -> legalCategories.firstOrNull()
                "Security"  -> securityCategories.firstOrNull()
                "Reports"   -> reportsCategories.firstOrNull()
                else        -> null
            }
            val firstSubItemsMap = when (label) {
                "Sales"     -> salesSubItems
                "Marketing" -> marketingSubItems
                "Finance"   -> financeSubItems
                "Inventory" -> inventorySubItems
                "Logistics" -> logisticsSubItems
                "Services"  -> servicesSubItems
                "HR"        -> hrSubItems
                "IT"        -> itSubItems
                "Legal"     -> legalSubItems
                "Security"  -> securitySubItems
                "Reports"   -> reportsSubItems
                else        -> emptyMap()
            }
            val firstSubItem = firstCategory?.let { firstSubItemsMap[it]?.firstOrNull() }

            if (firstCategory != null && firstSubItem != null) {
                expandedCategory = firstCategory
                selectedSubItem  = "$firstCategory::$firstSubItem"
                isDrawerOpen     = false
                onMenuItemClick("${label.lowercase()}_${firstSubItem.lowercase().replace(" ", "_")}")
            }
        } else {
            isDrawerOpen = false
            onMenuItemClick(label.lowercase())
        }
    }

    Box(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {

        if (isDrawerOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .clickable { isDrawerOpen = false }
                    .zIndex(1f)
            )
        }

        AnimatedVisibility(
            visible = isDrawerOpen,
            enter = slideInHorizontally(initialOffsetX = { -it }),
            exit  = slideOutHorizontally(targetOffsetX = { -it }),
            modifier = Modifier.zIndex(2f)
        ) {
            Row(modifier = Modifier.fillMaxHeight().width(IntrinsicSize.Max)) {

                // ── LEFT SIDEBAR ──
                Column(
                    modifier = Modifier
                        .width(if (isPanelMode) 86.dp else 280.dp)
                        .fillMaxHeight()
                        .background(Color.White)
                        .border(0.5.dp, Color(0xFFE0E0E0), RoundedCornerShape(0.dp))
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 20.dp),
                        contentAlignment = if (isPanelMode) Alignment.Center else Alignment.CenterStart
                    ) {
                        if (isPanelMode) {
                            Image(
                                painter = painterResource(id = R.drawable.cuso_logo),
                                contentDescription = "Logo",
                                modifier = Modifier.size(48.dp),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Image(painter = painterResource(R.drawable.logo), contentDescription = null)
                        }
                    }

                    HorizontalDivider(color = Color(0xFFF0F0F0))
                    Spacer(Modifier.height(8.dp))

                    Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                        menuItems.forEach { (icon, label) ->
                            val isSelected = selectedMenu == label
                            if (isPanelMode) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                        .clickable { handleMenuClick(label) }
                                        .padding(vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) Color(0xFFE3E0FB) else Color.Transparent),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            painter = painterResource(id = icon),
                                            contentDescription = label,
                                            tint = if (isSelected) Color(0xFF4338CA) else Color(0xFF6B7280),
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Text(
                                        text = label,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (isSelected) Color(0xFF4338CA) else Color(0xFF6B7280),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            } else {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 4.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) Color(0xFF3B3BF9) else Color.Transparent)
                                        .clickable { handleMenuClick(label) }
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = icon),
                                        contentDescription = label,
                                        tint = if (isSelected) Color.White else Color.Gray,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Text(
                                        text = label,
                                        fontSize = 15.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else Color.Black
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = Color(0xFFF0F0F0))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = if (isPanelMode) 14.dp else 16.dp)
                            .clickable { menuExpanded = true },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = if (isPanelMode) Arrangement.Center else Arrangement.spacedBy(12.dp)
                    ) {
                        if (!user?.profilePicture.isNullOrBlank()) {
                            AsyncImage(
                                model = user!!.profilePicture,
                                contentDescription = "Profile picture",
                                modifier = Modifier.size(if (isPanelMode) 38.dp else 42.dp).clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            val initials = buildString {
                                user?.firstName?.firstOrNull()?.let { append(it.uppercaseChar()) }
                                user?.lastName?.firstOrNull()?.let { append(it.uppercaseChar()) }
                            }
                            Box(
                                modifier = Modifier
                                    .size(if (isPanelMode) 38.dp else 42.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF3B3BF9)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(initials, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                        if (!isPanelMode) {
                            Column {
                                Text(
                                    "${user?.firstName.orEmpty()} ${user?.lastName.orEmpty()}".trim().ifBlank { "—" },
                                    fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black
                                )
                                Text(
                                    user?.email.orEmpty(),
                                    fontSize = 12.sp, color = Color.Gray,
                                    maxLines = 1, overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        offset = DpOffset(x = 10.dp, y = (-90).dp),
                        shape = RoundedCornerShape(8.dp),
                        containerColor = Color.White,
                        tonalElevation = 8.dp,
                        shadowElevation = 12.dp
                    ) {
                        DropdownMenuItem(
                            text = { Text("Logout", color = Color.Red) },
                            onClick = {
                                menuExpanded = false
                                authViewModel.logout { onLogout() }
                            }
                        )
                    }
                }

                // ── RIGHT ACCORDION PANEL ──
                if (isPanelMode) {
                    Column(
                        modifier = Modifier
                            .width(280.dp)
                            .fillMaxHeight()
                            .background(Color.White)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 16.dp, top = 22.dp, bottom = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(selectedMenu, fontSize = 19.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color(0xFF374151),
                                modifier = Modifier.size(20.dp).clickable { isDrawerOpen = false }
                            )
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            activeCategories.forEach { category ->
                                val isExpanded = expandedCategory == category
                                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isExpanded) Color(0xFFE9E7FC) else Color.Transparent)
                                            .clickable { expandedCategory = if (isExpanded) null else category }
                                            .padding(horizontal = 14.dp, vertical = 13.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = category,
                                            fontSize = 14.5.sp,
                                            fontWeight = if (isExpanded) FontWeight.SemiBold else FontWeight.Medium,
                                            color = if (isExpanded) Color(0xFF4338CA) else Color(0xFF374151)
                                        )
                                        Icon(
                                            imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                            contentDescription = null,
                                            tint = if (isExpanded) Color(0xFF4338CA) else Color(0xFF9CA3AF)
                                        )
                                    }

                                    if (isExpanded) {
                                        val subItems = activeSubItems[category].orEmpty()
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(start = 12.dp, end = 14.dp, bottom = 12.dp)
                                        ) {
                                            subItems.forEachIndexed { index, subItem ->
                                                val isLast = index == subItems.lastIndex
                                                val itemKey = "$category::$subItem"
                                                val isSubSelected = selectedSubItem == itemKey

                                                Box(modifier = Modifier.fillMaxWidth()) {
                                                    Canvas(modifier = Modifier.matchParentSize()) {
                                                        val strokeColor = Color(0xFFD1D5DB)
                                                        val lStrokeWidth = 1.dp.toPx()
                                                        val continueStrokeWidth = 0.5.dp.toPx()
                                                        val cornerRadius = 12.dp.toPx()
                                                        val lineX = 8.dp.toPx()
                                                        val curveTopY = 0f
                                                        val curveBottomY = 24.dp.toPx()
                                                        val horizontalEndX = lineX + 16.dp.toPx()

                                                        val path = Path().apply {
                                                            moveTo(lineX, curveTopY)
                                                            lineTo(lineX, curveBottomY - cornerRadius)
                                                            arcTo(
                                                                rect = Rect(left = lineX, top = curveBottomY - 2 * cornerRadius, right = lineX + 2 * cornerRadius, bottom = curveBottomY),
                                                                startAngleDegrees = 180f, sweepAngleDegrees = -90f, forceMoveTo = false
                                                            )
                                                            lineTo(horizontalEndX, curveBottomY)
                                                        }
                                                        drawPath(path = path, color = strokeColor, style = Stroke(width = lStrokeWidth))
                                                        if (!isLast) {
                                                            drawLine(color = strokeColor, start = Offset(lineX, curveBottomY), end = Offset(lineX, size.height), strokeWidth = continueStrokeWidth)
                                                        }
                                                    }
                                                    Text(
                                                        text = subItem,
                                                        fontSize = 13.sp,
                                                        fontWeight = if (isSubSelected) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (isSubSelected) Color(0xFF3B3BF9) else Color(0xFF424662),
                                                        modifier = Modifier
                                                            .padding(start = 26.dp)
                                                            .clickable {
                                                                selectedSubItem = itemKey
                                                                onMenuItemClick("${selectedMenu.lowercase()}_${subItem.lowercase().replace(" ", "_")}")
                                                                isDrawerOpen = false
                                                            }
                                                            .align(Alignment.CenterStart)
                                                            .padding(top = 12.dp, bottom = 6.dp)
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
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(lightGray, RoundedCornerShape(8.dp))
                        .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                        .clickable { isDrawerOpen = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu", modifier = Modifier.size(22.dp), tint = Color.Black)
                }

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                        .border(0.5.dp, Color.LightGray, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(24.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                        singleLine = true,
                        decorationBox = { inner ->
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.CenterStart) {
                                if (searchQuery.isEmpty()) {
                                    Text("Search anything", color = Color.Gray, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                                inner()
                            }
                        }
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(lightGray, RoundedCornerShape(8.dp))
                        .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(24.dp), tint = Color.Black)
                }

                IconButton(onClick = {}, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Notifications, contentDescription = "Notifications", modifier = Modifier.size(30.dp), tint = Color.DarkGray)
                }
                IconButton(onClick = {}, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = "Calendar", modifier = Modifier.size(30.dp), tint = Color.DarkGray)
                }
                IconButton(onClick = onSettingsClick, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = if (isSettingsOpen) Icons.Default.Close else Icons.Filled.Settings,
                        contentDescription = if (isSettingsOpen) "Close" else "Settings",
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
// LeadScreenContent
// ─────────────────────────────────────────────────────────────
// In LeadScreenContent.kt - Updated version
// ─────────────────────────────────────────────────────────────
// LeadScreenContent
// ─────────────────────────────────────────────────────────────
// ─────────────────────────────────────────────────────────────
// LeadScreenContent
// ─────────────────────────────────────────────────────────────
@Composable
fun LeadScreenContent(
    onCreateLead: () -> Unit = {},
    onViewLead:   () -> Unit = {},
    onEditLead:   () -> Unit = {}
) {
    val salesViewModel: SalesViewModel = hiltViewModel()
    val context = LocalContext.current

    // ✅ Use tableLeads from API directly - no Room database
    val leads by salesViewModel.tableLeads.collectAsStateWithLifecycle()
    val isLoading by salesViewModel.isLoadingTableLeads.collectAsStateWithLifecycle()
    val tableError by salesViewModel.tableError.collectAsStateWithLifecycle()

    val salesStatuses by salesViewModel.salesStatuses.collectAsStateWithLifecycle()
    val deleteState by salesViewModel.deleteState.collectAsStateWithLifecycle()
    val updateState by salesViewModel.updateState.collectAsStateWithLifecycle()

    // track which lead's action menu is open
    var actionMenuLeadId by remember { mutableStateOf<String?>(null) }

    // delete confirmation dialog
    var leadToDelete by remember { mutableStateOf<LeadTableItem?>(null) }

    // Loading state for edit/view
    var isLoadingEdit by remember { mutableStateOf(false) }
    var isLoadingView by remember { mutableStateOf(false) }

    // ✅ Fetch table data when screen opens
    LaunchedEffect(Unit) {
        salesViewModel.fetchTableLeads()
        // Pre-fetch data needed for edit
        salesViewModel.fetchStaff()
        salesViewModel.fetchGarmentCategories()
    }

    // handle delete success
    LaunchedEffect(deleteState) {
        if (deleteState is SaleState.Success) {
            salesViewModel.resetDeleteState()
            salesViewModel.fetchTableLeads() // ✅ Refresh table
        }
    }

    // Refresh leads when update is successful
    LaunchedEffect(updateState) {
        if (updateState is SaleState.Success) {
            salesViewModel.fetchTableLeads() // ✅ Refresh table
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

    // ✅ Filter leads from API data
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

    Column(
        Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F7))
            .padding(16.dp)
    ) {
        // ── Header bar ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(10.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: filter icon + dropdown title
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFFF3F3F5), RoundedCornerShape(8.dp))
                        .size(40.dp)
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Filled.FilterAlt, contentDescription = "Filter", tint = Color(0xFF374151), modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(12.dp))
                Box {
                    Row(modifier = Modifier.clickable { filterExpanded = true }, verticalAlignment = Alignment.CenterVertically) {
                        Text(text = selectedFilter, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Spacer(Modifier.width(4.dp))
                        Icon(imageVector = Icons.Filled.KeyboardArrowDown, contentDescription = "Expand", tint = Color(0xFF111827))
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
                                onClick = { selectedFilter = option; currentPage = 1; filterExpanded = false }
                            )
                        }
                    }
                }
            }

            // Right: list/grid toggle + create button
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                        Icon(imageVector = Icons.AutoMirrored.Filled.List, contentDescription = "List View", tint = if (isListView) Color(0xFF3B3BF9) else Color(0xFF9CA3AF), modifier = Modifier.size(20.dp))
                    }
                    Box(
                        modifier = Modifier
                            .background(if (!isListView) Color(0xFFEEEEFE) else Color.White)
                            .clickable { isListView = false }
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.GridView, contentDescription = "Grid View", tint = if (!isListView) Color(0xFF3B3BF9) else Color(0xFF9CA3AF), modifier = Modifier.size(20.dp))
                    }
                }
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

        Spacer(Modifier.height(12.dp))

        // ✅ Show loading state
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.White, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(8.dp))
                    Text("Loading leads...", color = Color.Gray)
                }
            }
        }
        // ✅ Show error state
        else if (tableError != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.White, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red, modifier = Modifier.size(48.dp))
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
        // ✅ Show empty state
        else if (leads.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.White, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(Modifier.padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No Leads Yet", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                    Spacer(Modifier.height(6.dp))
                    Text("Start by creating your first lead", fontSize = 14.sp, color = Color(0xFF9CA3AF))
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
        // ✅ Show table with data
        else {
            // ── Table view ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.White, RoundedCornerShape(12.dp))
            ) {
                // Delete confirmation dialog
                if (leadToDelete != null) {
                    AlertDialog(
                        onDismissRequest = { leadToDelete = null },
                        containerColor = Color.White,
                        shape = RoundedCornerShape(12.dp),
                        title = { Text("Delete Lead", fontWeight = FontWeight.Bold, color = Color(0xFF111827)) },
                        text = { Text("Are you sure you want to delete this lead? This action cannot be undone.", color = Color(0xFF6B7280)) },
                        confirmButton = {
                            Button(
                                onClick = {
                                    salesViewModel.deleteLead(leadToDelete!!.id)
                                    leadToDelete = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                shape = RoundedCornerShape(8.dp)
                            ) { Text("Delete", color = Color.White) }
                        },
                        dismissButton = {
                            OutlinedButton(
                                onClick = { leadToDelete = null },
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFFD1D5DB))
                            ) { Text("Cancel", color = Color(0xFF374151)) }
                        }
                    )
                }

                // Table header row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF9FAFB), RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                        .padding(horizontal = 12.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = pagedLeads.isNotEmpty() && pagedLeads.all { it.id in selectedIds },
                        onCheckedChange = { checked ->
                            selectedIds = if (checked) selectedIds + pagedLeads.map { it.id }
                            else selectedIds - pagedLeads.map { it.id }.toSet()
                        },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF3B3BF9)),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    LeadTableHeaderCell("Customer", Modifier.weight(1.6f))
                    LeadTableHeaderCell("Enquiry Type", Modifier.weight(1.4f))
                    LeadTableHeaderCell("Garments", Modifier.weight(1f))
                    LeadTableHeaderCell("Qty", Modifier.weight(0.6f))
                    LeadTableHeaderCell("Budget Range", Modifier.weight(1.6f))
                    LeadTableHeaderCell("Required Date", Modifier.weight(1.3f))
                    LeadTableHeaderCell("Source", Modifier.weight(1f))
                    LeadTableHeaderCell("Status", Modifier.weight(1.4f))
                    LeadTableHeaderCell("Action", Modifier.weight(0.7f), isAction = true)
                }

                HorizontalDivider(color = Color(0xFFF0F0F0))

                // Table rows
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(pagedLeads, key = { it.id }) { lead ->
                        val isChecked = lead.id in selectedIds
                        // Extract status name from API response
                        val statusName = when (lead.status) {
                            is String -> lead.status
                            is Map<*, *> -> (lead.status["name"] as? String) ?: ""
                            else -> ""
                        }

                        // ✅ Extract garment name - show "—" if empty or null
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

                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (isChecked) Color(0xFFF5F5FF) else Color.White)
                                    .padding(horizontal = 12.dp, vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { checked ->
                                        selectedIds = if (checked) selectedIds + lead.id else selectedIds - lead.id
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF3B3BF9)),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                LeadTableCell(lead.person.name, Modifier.weight(1.6f), bold = true, color = Color(0xFF111827))
                                LeadTableCell(lead.enquiryType, Modifier.weight(1.4f), color = Color(0xFF3B3BF9))
                                LeadTableCell(garmentName, Modifier.weight(1f))
                                LeadTableCell(
                                    if (lead.estimatedQuantity == 0) "—" else lead.estimatedQuantity.toString(),
                                    Modifier.weight(0.6f)
                                )
                                LeadTableCell(
                                    "₹${formatIndianNumber(lead.budgetRange.min)} - ₹${formatIndianNumber(lead.budgetRange.max)}",
                                    Modifier.weight(1.6f)
                                )
                                LeadTableCell(formatLeadDate(lead.requiredDate ?: ""), Modifier.weight(1.3f))
                                LeadTableCell(lead.source.ifEmpty { "—" }, Modifier.weight(1f))

                                // Status badge
                                Box(modifier = Modifier.weight(1.4f)) {
                                    val (badgeText, badgeColor) = when {
                                        statusName.contains("Convert", ignoreCase = true)
                                                || statusName.equals("CONVERTED", ignoreCase = true)
                                                || statusName.equals("converted_to_order", ignoreCase = true)
                                            -> "Converted to Order" to Color(0xFF34C759)

                                        statusName.contains("New", ignoreCase = true)
                                                || statusName.equals("NEW", ignoreCase = true)
                                                || statusName.equals("new_enquiry", ignoreCase = true)
                                            -> "New Enquiry" to Color(0xFF3B3BF9)

                                        statusName.contains("Quot", ignoreCase = true)
                                                || statusName.equals("QUOTED", ignoreCase = true)
                                            -> "Quoted" to Color(0xFFF59E0B)

                                        statusName.contains("Follow", ignoreCase = true)
                                                || statusName.equals("FOLLOW_UP", ignoreCase = true)
                                                || statusName.contains("Pending", ignoreCase = true)
                                            -> "Follow-up" to Color(0xFFEF4444)

                                        statusName.contains("Lost", ignoreCase = true)
                                            -> "Lost" to Color(0xFF6B7280)

                                        else -> statusName to Color(0xFF9CA3AF)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .border(1.dp, badgeColor, RoundedCornerShape(20.dp))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(badgeText, fontSize = 11.sp, color = badgeColor, maxLines = 1)
                                    }
                                }

                                // ✅ Action menu with View, Edit, Delete
                                Box(modifier = Modifier.weight(0.7f), contentAlignment = Alignment.Center) {
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
                                            // ✅ View - Only ONE API call (getViewOne)
                                            DropdownMenuItem(
                                                text = {
                                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                        if (isLoadingView) {
                                                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                                        } else {
                                                            Icon(Icons.Default.Visibility, contentDescription = null, tint = Color(0xFF3B3BF9), modifier = Modifier.size(16.dp))
                                                            Text("View", color = Color(0xFF374151))
                                                        }
                                                    }
                                                },
                                                enabled = !isLoadingView,
                                                onClick = {
                                                    actionMenuLeadId = null
                                                    isLoadingView = true
                                                    // ✅ Only ONE API call - getViewOne
                                                    salesViewModel.fetchLeadDetails(lead.id) { success ->
                                                        isLoadingView = false
                                                        if (success) {
                                                            onViewLead()
                                                        } else {
                                                            Toast.makeText(context, "Failed to load lead details", Toast.LENGTH_SHORT).show()
                                                            // Fallback: use existing data
                                                            val leadEntity = lead.toLeadEntity()
                                                            salesViewModel.selectLead(leadEntity)
                                                            onViewLead()
                                                        }
                                                    }
                                                }
                                            )

                                            // ✅ Edit - Only ONE API call (getViewOne)
                                            DropdownMenuItem(
                                                text = {
                                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                        if (isLoadingEdit) {
                                                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                                        } else {
                                                            Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                                                            Text("Edit", color = Color(0xFF374151))
                                                        }
                                                    }
                                                },
                                                enabled = !isLoadingEdit,
                                                onClick = {
                                                    actionMenuLeadId = null
                                                    isLoadingEdit = true
                                                    // ✅ Only ONE API call - getViewOne
                                                    salesViewModel.fetchLeadDetails(lead.id) { success ->
                                                        isLoadingEdit = false
                                                        if (success) {
                                                            onEditLead()
                                                        } else {
                                                            Toast.makeText(context, "Failed to load lead details for editing", Toast.LENGTH_SHORT).show()
                                                            // Fallback: use existing data
                                                            val leadEntity = lead.toLeadEntity()
                                                            salesViewModel.selectLead(leadEntity)
                                                            onEditLead()
                                                        }
                                                    }
                                                }
                                            )

                                            HorizontalDivider(color = Color(0xFFF0F0F0))

                                            // Delete
                                            DropdownMenuItem(
                                                text = {
                                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                                        Text("Delete", color = Color(0xFFEF4444))
                                                    }
                                                },
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

                // ── Pagination footer ──
                HorizontalDivider(color = Color(0xFFF0F0F0))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Showing ${if (filteredLeads.isEmpty()) 0 else (currentPage - 1) * itemsPerPage + 1} - ${minOf(currentPage * itemsPerPage, filteredLeads.size)} of ${filteredLeads.size}",
                        fontSize = 13.sp,
                        color = Color(0xFF6B7280)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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

    // ✅ No duplicate fetchLeadDetails here - data already loaded

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
                            // Fetch fresh data before editing
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
        // ... rest of ViewLeadScreen UI remains the same
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F7))
                .padding(padding)
        ) {
            // ... all the UI remains the same
        }
    }
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
        if (garmentCategories.isNotEmpty() && selectedGarmentCategories.isEmpty() && l.garments.isBlank()) {
            // Only set default if no garments are selected and none exist in the data
            // Don't auto-select - let user choose
        }
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
                        .padding(horizontal = 16.dp, vertical = 12.dp),
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
                                                    if (isSelected) Color(0xFF3B3BF9) else Color(0xFFE5E7EB),
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
@Composable
fun ViewField(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text(label, fontSize = 12.sp, color = Color(0xFF9CA3AF), fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(2.dp))
        Text(value, fontSize = 14.sp, color = Color(0xFF111827), fontWeight = FontWeight.Medium)
    }
    HorizontalDivider(color = Color(0xFFF5F5F5), modifier = Modifier.padding(top = 6.dp))
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

// ── Table cell helpers ──
@Composable
fun LeadTableHeaderCell(text: String, modifier: Modifier, isAction: Boolean = false) {
    Text(
        text = text,
        modifier = modifier,
        fontSize = 12.sp,
        fontWeight = if (isAction) FontWeight.Bold else FontWeight.Normal,
        color = if (isAction) Color(0xFF111827) else Color(0xFF6B7280),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

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

    Column(Modifier.fillMaxSize().background(lightGray)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(lightGray),
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
                Box(modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(12.dp)).padding(16.dp)) {
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
                        Box(Modifier.fillMaxWidth().background(Color(0xFFE0E0FC), RoundedCornerShape(12.dp))) { Text(" ") }
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(10.dp).background(Color(0xFFF97316), CircleShape))
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
                Box(modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(12.dp)).padding(16.dp)) {
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
                            modifier = Modifier.fillMaxWidth().height(220.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            leadItems.forEachIndexed { index, item ->
                                val color = barColors[index % barColors.size]
                                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(Modifier.size(12.dp).background(color, CircleShape))
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
                Box(modifier = Modifier.fillMaxWidth().height(420.dp).background(Color.White, RoundedCornerShape(16.dp)).padding(16.dp)) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text("Invoicing vs. Collection", fontSize = 20.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2D3748))
                            Spacer(Modifier.weight(1f))
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End) {
                                Box(Modifier.size(10.dp).background(Color(0xFF6C63FF), CircleShape))
                                Spacer(Modifier.width(6.dp))
                                Text("Invoiced", fontSize = 12.sp, color = Color.Black, maxLines = 1)
                                Spacer(Modifier.width(12.dp))
                                Box(Modifier.size(10.dp).background(Color(0xFF34C759), CircleShape))
                                Spacer(Modifier.width(6.dp))
                                Text("Collected", fontSize = 12.sp, color = Color.Black, maxLines = 1)
                            }
                        }
                        Box(modifier = Modifier.weight(1f).fillMaxWidth())
                        Row(
                            modifier = Modifier.fillMaxWidth().background(Color(0xFFF5F5F5), RoundedCornerShape(16.dp)).padding(horizontal = 20.dp, vertical = 18.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(12.dp).background(Color(0xFF6C63FF), CircleShape))
                                Spacer(Modifier.width(12.dp))
                                Text("Total 7d: ₹0", fontSize = 18.sp, color = Color(0xFF4A5568))
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(12.dp).background(Color(0xFF34C759), CircleShape))
                                Spacer(Modifier.width(12.dp))
                                Text("Collected: ₹0", fontSize = 18.sp, color = Color(0xFF34C759))
                            }
                        }
                    }
                }
            }

            // Operations Control
            item {
                Box(modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(12.dp)).padding(16.dp)) {
                    Column(Modifier.fillMaxWidth()) {
                        Text("Operations Control", fontSize = 25.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(16.dp))
                        LazyRow(state = operationsScrollState, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            items(operationControls) { item ->
                                Column(
                                    modifier = Modifier.width(200.dp).background(Color(0xFFF8FAFC)).padding(20.dp),
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
                Box(modifier = Modifier.fillMaxWidth().height(450.dp).background(Color.White, RoundedCornerShape(12.dp)).padding(16.dp)) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text("Order Status Distribution", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Spacer(modifier = Modifier.height(50.dp))
                        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(modifier = Modifier.size(30.dp).background(Color(0xFF3F3CCF), CircleShape))
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
                    modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 16.dp, vertical = 12.dp),
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
        Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F7)).padding(padding)) {
            Row(
                modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", modifier = Modifier.clickable { onBack() }, tint = Color(0xFF111827))
                    Text("Create Lead", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                }
                Box(modifier = Modifier.border(1.dp, Color(0xFF3B3BF9), RoundedCornerShape(6.dp)).padding(horizontal = 10.dp, vertical = 4.dp)) {
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
                        Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp)).padding(4.dp)) {
                            listOf("Individual", "Corporate").forEach { type ->
                                val isSelected = customerType == type
                                Row(
                                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) Color.White else Color.Transparent)
                                        .clickable { customerType = type }.padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(if (type == "Individual") Icons.Default.Person else Icons.Default.Business, type, modifier = Modifier.size(16.dp), tint = if (isSelected) Color.Black else Color(0xFF6B7280))
                                    Spacer(Modifier.width(6.dp))
                                    Text(type, fontSize = 14.sp, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal, color = if (isSelected) Color.Black else Color(0xFF6B7280))
                                }
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                        FormLabel("Full Name")
                        FormTextField(value = fullName, onValueChange = { fullName = it })
                        Spacer(Modifier.height(14.dp))
                        PhoneInputField(phoneValue = phone, onPhoneChange = { phone = it }, onCountryChange = { selectedIso = it.iso })
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
                                        .border(1.dp, if (isSelected) Color(0xFF3B3BF9) else Color(0xFFE5E7EB), RoundedCornerShape(50.dp))
                                        .background(if (isSelected) Color(0xFFEEEEFE) else Color.White, RoundedCornerShape(50.dp))
                                        .clickable { garmentCategory = if (garmentCategory == option) "" else option }
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
                            Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(appointmentTime, color = Color(0xFF374151), fontSize = 14.sp)
                                Icon(Icons.Default.Schedule, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                            }
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
                        OutlinedTextField(value = internalNotes, onValueChange = { internalNotes = it }, modifier = Modifier.fillMaxWidth().height(100.dp), shape = RoundedCornerShape(8.dp), colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color(0xFFE5E7EB), focusedBorderColor = Color(0xFF3B3BF9), unfocusedContainerColor = Color.White, focusedContainerColor = Color.White))
                        Spacer(Modifier.height(14.dp))
                        FormLabel("Customer Notes")
                        OutlinedTextField(value = customerNotes, onValueChange = { customerNotes = it }, modifier = Modifier.fillMaxWidth().height(100.dp), shape = RoundedCornerShape(8.dp), colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color(0xFFE5E7EB), focusedBorderColor = Color(0xFF3B3BF9), unfocusedContainerColor = Color.White, focusedContainerColor = Color.White))
                    }
                }
            }
        }
    }
}
// ─────────────────────────────────────────────────────────────
// Reusable Components
// ─────────────────────────────────────────────────────────────
@Composable
fun SectionHeader(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(modifier = Modifier.size(40.dp).background(Color(0xFFF3F4F6), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
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
    Column(modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(12.dp)).padding(16.dp), content = content)
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
        modifier = Modifier.fillMaxWidth().background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 14.dp),
        singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
    )
}

@Composable
fun FormDateField(value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp)).clickable { onClick() }.padding(horizontal = 12.dp, vertical = 14.dp),
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
            modifier = Modifier.fillMaxWidth().background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp)).clickable { onExpandChange(true) }.padding(horizontal = 12.dp, vertical = 14.dp),
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
    Box(modifier = modifier.background(Color.White, RoundedCornerShape(12.dp)).padding(16.dp)) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(48.dp).background(iconBg, RoundedCornerShape(12.dp)).clip(RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(26.dp))
                }
                Spacer(Modifier.weight(1f))
                Text(text = "0%", modifier = Modifier.background(Color(0xFFDCFCE7), RoundedCornerShape(18.dp)).padding(horizontal = 12.dp, vertical = 6.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF16A34A))
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
        Box(modifier = modifier.fillMaxWidth().height(height))
        return
    }

    val viewportSize              = (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset).toFloat()
    val averageItemSize           = visibleItemsInfo.sumOf { it.size }.toFloat() / visibleItemsInfo.size
    val estimatedTotalContentSize = averageItemSize * totalItems
    val thumbSizeFraction         = (viewportSize / estimatedTotalContentSize).coerceIn(0.1f, 1f)
    val scrolledPixels            = state.firstVisibleItemIndex * averageItemSize + state.firstVisibleItemScrollOffset
    val maxScrollPixels           = (estimatedTotalContentSize - viewportSize).coerceAtLeast(1f)
    val scrollFraction            = (scrolledPixels / maxScrollPixels).coerceIn(0f, 1f)

    BoxWithConstraints(modifier = modifier.fillMaxWidth().height(height).background(trackColor, RoundedCornerShape(height / 2))) {
        val trackWidth  = this@BoxWithConstraints.maxWidth
        val thumbWidth  = trackWidth * thumbSizeFraction
        val thumbOffset = (trackWidth - thumbWidth) * scrollFraction
        Box(modifier = Modifier.offset(x = thumbOffset).width(thumbWidth).height(height).background(thumbColor, RoundedCornerShape(height / 2)))
    }
}

@Composable
fun StatusLegend(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(18.dp).background(color = color, shape = CircleShape))
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

// Add this helper at the bottom of the file
fun mapLeadStatusToApiCode(displayStatus: String): String {
    return when (displayStatus) {
        "New Enquiry"        -> "NEW"
        "Quoted"             -> "QUOTED"
        "Follow-up Pending"  -> "FOLLOW_UP"
        "Converted to Order" -> "CONVERTED"
        "Lost Enquiry"       -> "LOST"
        else                 -> displayStatus
    }
}