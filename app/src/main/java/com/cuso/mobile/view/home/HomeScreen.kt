package com.cuso.mobile.view.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Rect


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
            .padding(top = 50.dp)
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
            "settings" -> SettingsScreen(navController)
            "home" -> HomeScreenContent()
            "sales" -> { }
            "marketing" -> { }
            else -> { }
        }
    }
}
@Composable
fun TopNavBar(
    navController: NavController,
    isSettingsOpen: Boolean = false,
    onSettingsClick: () -> Unit = {},
    onMenuItemClick: (String) -> Unit = {},
    onLogout: () -> Unit
) {
    val authViewModel: Authenticate = hiltViewModel()
    var isDrawerOpen by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedMenu by remember { mutableStateOf("Home") }

    val user by authViewModel.user.collectAsStateWithLifecycle()
    var menuExpanded by remember { mutableStateOf(false) }

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

    // ───── Top-level categories shown as accordion rows, per menu ─────
    val salesCategories = listOf(
        "Lead Management",
        "Customer",
        "Measurements",
        "Sales & Orders",
        "Order Management",
        "Pricing & Quotations",
        "Targets vs Achievements",
        "Salesperson Analytics"
    )
    val marketingCategories = listOf(
        "Website",
        "Campaigns",
        "Leads & Audience",
        "Engagement",
        "Growth",
        "Pages",
        "Budget",
        "Team"
    )
    val financeCategories = listOf(
        "Accounts Receivable",
        "Accounts Payable",
        "Expenses",
        "Finance Core"
    )
    val inventoryCategories = listOf(
        "Items",
        "Procurement",
        "Payables"
    )
    val logisticsCategories = listOf(
        "Delivery",
        "Returns"
    )
    val servicesCategories = listOf(
        "Service Request",
        "Alteration Management",
        "Return",
        "Damaged Goods",
        "Customer Feedback"
    )
    val hrCategories = listOf(
        "Employees"
    )
    val itCategories = listOf(
        "Integrations"
    )
    val legalCategories = listOf(
        "Legal Management"
    )
    val securityCategories = listOf(
        "Access Control",
        "Auth & Verification",
        "Monitoring & Audit"
    )
    val reportsCategories = listOf(
        "Sales Reports",
        "Finance Reports"
    )

    // ───── Sub-items revealed when a category row is expanded ─────
    val salesSubItems = mapOf(
        "Lead Management" to listOf("Lead"),
        "Customer" to listOf("Customers"),
        "Measurements" to listOf("Measurements"),
        "Sales & Orders" to listOf("Sales & Orders"),
        "Order Management" to listOf("Orders"),
        "Pricing & Quotations" to listOf("Overview", "Pricing & Quotations"),
        "Targets vs Achievements" to listOf("Targets vs Achievements"),
        "Salesperson Analytics" to listOf("Salesperson Analytics")
    )
    val marketingSubItems = mapOf(
        "Campaigns" to listOf("Campaigns", "Promotions", "Marketing & Calendar"),
        "Leads & Audience" to listOf("Lead Generation", "Customer Segmentation"),
        "Engagement" to listOf("Customer Engagement", "WhatsApp", "Social Media", "Review & Feedback"),
        "Growth" to listOf("Referral Program", "Influencer"),
        "Pages" to listOf("Landing Page"),
        "Budget" to listOf("Marketing Budget"),
        "Team" to listOf("Marketing Tasks", "Team Management")
    )
    val financeSubItems = mapOf(
        "Accounts Receivable" to listOf("Customers", "Sales Invoices", "Payments Received"),
        "Accounts Payable" to listOf("Suppliers", "Purchase Invoices", "Payments Mode"),
        "Expenses" to listOf("Expenses"),
        "Finance Core" to listOf("Chart of Accounts", "Journal Entries", "Trial Balance")
    )
    val inventorySubItems = mapOf(
        "Items" to listOf("All Items", "Item Groups"),
        "Procurement" to listOf("Suppliers", "Requisitions", "Orders", "Goods Receipt"),
        "Payables" to listOf("Invoices", "Payments", "Credits")
    )
    val logisticsSubItems = mapOf(
        "Delivery" to listOf("Delivery"),
        "Returns" to listOf("Returns")
    )
    val servicesSubItems = mapOf(
        "Service Request" to listOf("Service Request"),
        "Alteration Management" to listOf("Alteration Management"),
        "Return" to listOf("Return"),
        "Damaged Goods" to listOf("Damaged Goods"),
        "Customer Feedback" to listOf("Customer Feedback")
    )
    val hrSubItems = mapOf(
        "Employees" to listOf("All Employees")
    )
    val itSubItems = mapOf(
        "Integrations" to listOf("API Integration")
    )
    val legalSubItems = mapOf(
        "Legal Management" to listOf("Legal Documents")
    )
    val securitySubItems = mapOf(
        "Access Control" to listOf("User Accounts", "Roles & Permissions"),
        "Auth & Verification" to listOf("Multi Factor (MFA)", "SSO Settings"),
        "Monitoring & Audit" to listOf("Login Logs", "Activity Logs")
    )
    val reportsSubItems = mapOf(
        "Sales Reports" to listOf("Sales Reports"),
        "Finance Reports" to listOf("Finance Reports")
    )

    // Tracks which single category row is expanded (null = none open)
    var expandedCategory by remember { mutableStateOf<String?>(null) }

    // Tracks which sub-item is currently selected, keyed by "category::subItem"
    // so only one sub-item is ever highlighted at a time.
    var selectedSubItem by remember { mutableStateOf<String?>(null) }

    // Menus that have a right-hand accordion panel (everything except Home)
    val panelMenus = setOf(
        "Sales", "Marketing", "Finance", "Inventory", "Logistics",
        "Services", "HR", "IT", "Legal", "Security", "Reports"
    )
    val isPanelMode = selectedMenu in panelMenus

    // Resolve the categories + sub-items for whichever menu is currently selected
    val activeCategories: List<String> = when (selectedMenu) {
        "Sales" -> salesCategories
        "Marketing" -> marketingCategories
        "Finance" -> financeCategories
        "Inventory" -> inventoryCategories
        "Logistics" -> logisticsCategories
        "Services" -> servicesCategories
        "HR" -> hrCategories
        "IT" -> itCategories
        "Legal" -> legalCategories
        "Security" -> securityCategories
        "Reports" -> reportsCategories
        else -> emptyList()
    }
    val activeSubItems: Map<String, List<String>> = when (selectedMenu) {
        "Sales" -> salesSubItems
        "Marketing" -> marketingSubItems
        "Finance" -> financeSubItems
        "Inventory" -> inventorySubItems
        "Logistics" -> logisticsSubItems
        "Services" -> servicesSubItems
        "HR" -> hrSubItems
        "IT" -> itSubItems
        "Legal" -> legalSubItems
        "Security" -> securitySubItems
        "Reports" -> reportsSubItems
        else -> emptyMap()
    }

    Box(modifier = Modifier
        .fillMaxWidth()
        .wrapContentHeight()) {

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
            exit = slideOutHorizontally(targetOffsetX = { -it }),
            modifier = Modifier.zIndex(2f)
        ) {

            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(IntrinsicSize.Max)
            ) {

                // ───────────────── LEFT SIDEBAR ─────────────────
                // Narrow icon-rail when a right panel is open (Sales, Finance, ...),
                // wide labeled sidebar with full profile when it isn't (Home).
                Column(
                    modifier = Modifier
                        .width(if (isPanelMode) 86.dp else 280.dp)
                        .fillMaxHeight()
                        .background(Color.White)
                        .border(0.5.dp, Color(0xFFE0E0E0), RoundedCornerShape(0.dp))
                ) {

                    // ✅ HEADER — brand logo
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 20.dp),
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

                    // ───── MENU ITEMS ─────
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        menuItems.forEach { (icon, label) ->
                            val isSelected = selectedMenu == label

                            if (isPanelMode) {
                                // Narrow rail: icon on top, label underneath.
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                        .clickable {
                                            selectedMenu = label
                                            expandedCategory = null
                                            isDrawerOpen = label in panelMenus
                                            onMenuItemClick(label.lowercase())
                                        }
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
                                // Wide rail: icon + label side by side, like the original Home view.
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 4.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) Color(0xFF3B3BF9) else Color.Transparent)
                                        .clickable {
                                            selectedMenu = label
                                            expandedCategory = null
                                            isDrawerOpen = label in panelMenus
                                            onMenuItemClick(label.lowercase())
                                        }
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

                    // ───── USER PROFILE ─────
                    // Narrow rail: avatar only. Wide rail: avatar + name + email, like the original Home view.
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
                                modifier = Modifier
                                    .size(if (isPanelMode) 38.dp else 42.dp)
                                    .clip(CircleShape),
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
                                    fontSize = 12.sp, color = Color.Gray
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
                                authViewModel.logout {
                                    onLogout()
                                }
                            }
                        )
                    }
                }

                // ───────────────── RIGHT ACCORDION PANEL (shared by every menu) ─────────────────
                if (isPanelMode) {
                    Column(
                        modifier = Modifier
                            .width(280.dp)
                            .fillMaxHeight()
                            .background(Color.White)
                    ) {
                        // ── Panel header: section title + close (X) button ──
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 20.dp, end = 16.dp, top = 22.dp, bottom = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = selectedMenu,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1F2937)
                            )
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color(0xFF374151),
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable { isDrawerOpen = false }
                            )
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp)
                                .verticalScroll(rememberScrollState())
                        ) {

                            // ───── ACCORDION CATEGORY LIST ─────
                            activeCategories.forEach { category ->
                                val isExpanded = expandedCategory == category

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isExpanded) Color(0xFFE9E7FC) else Color.Transparent)
                                            .clickable {
                                                expandedCategory =
                                                    if (isExpanded) null else category
                                            }
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
                                            contentDescription = if (isExpanded) "Collapse $category" else "Expand $category",
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

                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                ) {
                                                    // Tree connector: rounded "L" curve (like the CSS
                                                    // border-l + border-b + rounded-bl-xl), plus a
                                                    // continuing straight line down to the next item
                                                    // unless this is the last sub-item.
                                                    Canvas(
                                                        modifier = Modifier
                                                            .matchParentSize()
                                                    ) {
                                                        val strokeColor = Color(0xFFD1D5DB)
                                                        val lStrokeWidth = 1.dp.toPx()
                                                        val continueStrokeWidth = 0.5.dp.toPx()
                                                        val cornerRadius = 12.dp.toPx()
                                                        val lineX = 8.dp.toPx()
                                                        val curveTopY = 0f
                                                        val curveBottomY = 24.dp.toPx()
                                                        val horizontalEndX = lineX + 16.dp.toPx()

                                                        // rounded L: vertical segment down, then curve
                                                        // into a horizontal segment (rounded bottom-left,
                                                        // 12px radius — matches Tailwind's rounded-bl-xl)
                                                        val path = Path().apply {
                                                            moveTo(lineX, curveTopY)
                                                            lineTo(lineX, curveBottomY - cornerRadius)
                                                            arcTo(
                                                                rect = Rect(
                                                                    left = lineX,
                                                                    top = curveBottomY - 2 * cornerRadius,
                                                                    right = lineX + 2 * cornerRadius,
                                                                    bottom = curveBottomY
                                                                ),
                                                                startAngleDegrees = 180f,
                                                                sweepAngleDegrees = -90f,
                                                                forceMoveTo = false
                                                            )
                                                            lineTo(horizontalEndX, curveBottomY)
                                                        }
                                                        drawPath(
                                                            path = path,
                                                            color = strokeColor,
                                                            style = Stroke(width = lStrokeWidth)
                                                        )

                                                        // continuing vertical trunk line down to the next
                                                        // sub-item — thinner (0.5px), only if not last
                                                        if (!isLast) {
                                                            drawLine(
                                                                color = strokeColor,
                                                                start = Offset(lineX, curveBottomY),
                                                                end = Offset(lineX, size.height),
                                                                strokeWidth = continueStrokeWidth
                                                            )
                                                        }
                                                    }

                                                    Text(
                                                        text = subItem,
                                                        fontSize = 13.sp,
                                                        fontWeight = if (isSubSelected) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (isSubSelected) Color(0xFF3B3BF9) else Color(0xFF424662),
                                                        modifier = Modifier
                                                            .padding(start = 26.dp)
                                                            .clickable { selectedSubItem = itemKey }
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
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
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
@Composable
fun HomeScreenContent() {

    // ✅ Single source of truth — update values here to change both chart and list
    val leadItems = listOf(
        LeadItem("New Enquiry", 10f),
        LeadItem("Quoted", 7f),
        LeadItem("Follow-up", 5f),
        LeadItem("Converted", 12f),
        LeadItem("Last Enquiry", 3f)
    )

    val operationControls = listOf(
        ControlItem("Customer"),
        ControlItem("Type"),
        ControlItem("Measurements"),
        ControlItem("Priority")
    )

    val barColors = listOf(
        Color(0xFF6C63FF),
        Color(0xFF3B82F6),
        Color(0xFFF59E0B),
        Color(0xFF10B981),
        Color(0xFFEF4444)
    )

    val labels = leadItems.map { it.header }
    val values = leadItems.map { it.value }

    // ✅ Correct Vico 2.0 stable: CartesianValueFormatter
    val bottomAxisFormatter = CartesianValueFormatter { value, _, _ ->
        labels.getOrNull(value.toInt()) ?: ""
    }

    val modelProducer = remember { CartesianChartModelProducer() }
    LaunchedEffect(leadItems) {
        modelProducer.runTransaction {
            columnSeries { series(values) }
        }
    }

    // ✅ Build one LineComponent per color (each bar gets its own component)
    val columnComponents = barColors.map { color ->
        rememberLineComponent(
            color = color,
            thickness = 24.dp,
            shape = VicoShape.rounded(allPercent = 20)
        )
    }

    // ✅ Custom ColumnProvider: picks color by bar (x) index instead of series index
    // This is what makes every single bar a different color.
    val columnProvider = remember(columnComponents) {
        object : ColumnCartesianLayer.ColumnProvider {
            override fun getColumn(
                entry: ColumnCartesianLayerModel.Entry,
                seriesIndex: Int,
                extraStore: ExtraStore
            ): LineComponent {
                val index = entry.x.toInt().mod(columnComponents.size)
                return columnComponents[index]
            }

            override fun getWidestSeriesColumn(
                seriesIndex: Int,
                extraStore: ExtraStore
            ): LineComponent = columnComponents[0]
        }
    }

    // ✅ Correct Vico 2.0 stable marker
    val markerLabel = rememberTextComponent(
        color = Color.White,
        background = rememberShapeComponent(
            color = Color(0xFF1E293B),
            shape = VicoShape.rounded(allPercent = 8)
        ),
        padding = Dimensions(8f, 4f, 8f, 4f)
    )

    val marker = rememberDefaultCartesianMarker(
        label = markerLabel,
        valueFormatter = { _, targets ->
            targets.joinToString { target ->
                val index = target.x.toInt()
                val label = labels.getOrNull(index) ?: ""
                val value = values.getOrNull(index)?.toInt() ?: 0
                "$label: $value"
            }
        }
    )

    // ✅ Scroll states for the two LazyRows that need a scrollbar
    val legendScrollState = rememberLazyListState()
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

            // ── Row 1: Total Revenue + Active Orders ──
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        iconBg = Color(0xFFDCFCE7),
                        icon = Icons.Filled.Money,
                        iconTint = Color(0xFF16A34A),
                        title = "Total Revenue",
                        value = "₹0"
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        iconBg = Color(0xFFDBEAFE),
                        icon = Icons.Filled.ShoppingCart,
                        iconTint = Color(0xFF2563EB),
                        title = "Active Orders",
                        value = "0"
                    )
                }
            }

            // ── Row 2: Measurements + Pending Payments ──
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        iconBg = Color(0xFFDBEAFE),
                        icon = Icons.Filled.LinearScale,
                        iconTint = Color(0xFF9333EA),
                        title = "Measurements",
                        value = "0"
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        iconBg = Color(0xFFDCFCE7),
                        icon = Icons.Filled.Money,
                        iconTint = Color(0xFF16A34A),
                        title = "Pending Payments",
                        value = "₹0"
                    )
                }
            }

            // ── Collection Efficiency ──
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
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
                            .background(Color(0xFFE0E0FC), RoundedCornerShape(12.dp))) {
                            Text(" ")
                        }
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

            // ── Lead Management Chart ──
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Column(Modifier.fillMaxWidth()) {
                        Text("Lead Management", color = Color.Gray, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(16.dp))

                        // ✅ Colorful bar chart with hover tooltip, no dashed gridlines
                        CartesianChartHost(
                            chart = rememberCartesianChart(
                                rememberColumnCartesianLayer(
                                    columnProvider = columnProvider
                                ),
                                startAxis = rememberStartAxis(
                                    guideline = null // ❌ removes dashed horizontal gridlines
                                ),
                                bottomAxis = rememberBottomAxis(
                                    valueFormatter = bottomAxisFormatter,
                                    guideline = null // ❌ removes dashed vertical gridlines
                                ),
                                marker = marker
                            ),
                            modelProducer = modelProducer,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                        )

                        Spacer(Modifier.height(16.dp))

                        // ✅ Color-coded legend synced with chart
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            leadItems.forEachIndexed { index, item ->
                                val color = barColors[index % barColors.size]

                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(Modifier
                                        .size(12.dp)
                                        .background(color, CircleShape))
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = item.header,
                                        color = Color.DarkGray,
                                        fontSize = 14.sp,
                                        maxLines = 1,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = item.value.toInt().toString(),
                                        fontWeight = FontWeight.Bold,
                                        color = color,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                        HorizontalScrollbar(
                            state = legendScrollState,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            }
            // ── Invoicing vs Collection ──
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp)
                        .background(Color.White, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text("Invoicing vs. Collection", fontSize = 20.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2D3748))
                            Spacer(Modifier.weight(1f))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier
                                    .size(10.dp)
                                    .background(Color(0xFF6C63FF), CircleShape))
                                Spacer(Modifier.width(6.dp))
                                Text("Invoiced", fontSize = 14.sp,color=Color.Black)
                                Spacer(Modifier.width(16.dp))
                                Box(Modifier
                                    .size(10.dp)
                                    .background(Color(0xFF34C759), CircleShape))
                                Spacer(Modifier.width(6.dp))
                                Text("Collected", fontSize = 14.sp,color=Color.Black)
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

            // ── Operations Control ──
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Column(Modifier.fillMaxWidth()) {
                        Text("Operations Control", fontSize = 25.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(16.dp))
                        LazyRow(
                            state = operationsScrollState,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
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
                        HorizontalScrollbar(
                            state = operationsScrollState,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            }

            // ── Order Status Distribution ──
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(450.dp)
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
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

// ✅ Reusable stat card to eliminate repetition
@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    iconBg: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    value: String
) {
    Box(
        modifier = modifier
            .height(200.dp)
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(iconBg, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(30.dp))
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text = "0%",
                    modifier = Modifier
                        .background(Color(0xFFDCFCE7), RoundedCornerShape(18.dp))
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF16A34A)
                )
            }
            Spacer(modifier = Modifier.height(40.dp))
            Text(text = title, color = Color.DarkGray, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        }
    }
}

// ✅ Lightweight custom horizontal scrollbar for a LazyRow.
// Shows a thumb whose width reflects how many items are visible vs total,
// and whose offset reflects current scroll position.
@Composable
fun HorizontalScrollbar(
    state: LazyListState,
    modifier: Modifier = Modifier,
    trackColor: Color = Color(0xFFE5E7EB),
    thumbColor: Color = Color.Gray,
    height: androidx.compose.ui.unit.Dp = 4.dp
) {
    val layoutInfo = state.layoutInfo
    val visibleItemsInfo = layoutInfo.visibleItemsInfo
    val totalItems = layoutInfo.totalItemsCount

    // ✅ Pixel-based check — catches partial overflow that item-count
    // comparisons miss (visibleItemsInfo includes partially-clipped items).
    val canScroll = state.canScrollForward || state.canScrollBackward

    if (totalItems == 0 || visibleItemsInfo.isEmpty() || !canScroll) {
        Box(modifier = modifier
            .fillMaxWidth()
            .height(height))
        return
    }

    val viewportSize = (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset).toFloat()
    val averageItemSize = visibleItemsInfo.sumOf { it.size }.toFloat() / visibleItemsInfo.size
    val estimatedTotalContentSize = averageItemSize * totalItems
    val thumbSizeFraction = (viewportSize / estimatedTotalContentSize).coerceIn(0.1f, 1f)

    val scrolledPixels = state.firstVisibleItemIndex * averageItemSize + state.firstVisibleItemScrollOffset
    val maxScrollPixels = (estimatedTotalContentSize - viewportSize).coerceAtLeast(1f)
    val scrollFraction = (scrolledPixels / maxScrollPixels).coerceIn(0f, 1f)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(trackColor, RoundedCornerShape(height / 2))
    ) {
        val trackWidth = this@BoxWithConstraints.maxWidth
        val thumbWidth = trackWidth * thumbSizeFraction
        val thumbOffset = (trackWidth - thumbWidth) * scrollFraction

        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .width(thumbWidth)
                .height(height)
                .background(thumbColor, RoundedCornerShape(height / 2))
        )
    }
}
data class LeadItem(
    val header: String,
    val value: Float          // ✅ Float so chart and list share same data
)

data class ControlItem(
    val controls: String
)

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