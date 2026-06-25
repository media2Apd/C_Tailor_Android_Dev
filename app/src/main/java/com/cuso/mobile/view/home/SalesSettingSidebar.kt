//package com.cuso.mobile.view.home
//
//import android.widget.Toast
//import androidx.compose.animation.AnimatedVisibility
//import androidx.compose.animation.slideInHorizontally
//import androidx.compose.animation.slideOutHorizontally
//import androidx.compose.foundation.Canvas
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Close
//import androidx.compose.material.icons.filled.KeyboardArrowDown
//import androidx.compose.material.icons.filled.KeyboardArrowUp
//import androidx.compose.material.icons.filled.Menu
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.geometry.Rect
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.Path
//import androidx.compose.ui.graphics.drawscope.Stroke
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.text.style.TextOverflow
//import androidx.compose.ui.unit.DpOffset
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.compose.ui.zIndex
//import androidx.hilt.navigation.compose.hiltViewModel
//import androidx.lifecycle.compose.collectAsStateWithLifecycle
//import androidx.navigation.NavController
//import coil.compose.AsyncImage
//import com.cuso.mobile.R
//import com.cuso.mobile.model.Organization
//import com.cuso.mobile.model.Settings
//import com.cuso.mobile.model.Subscription
//import com.cuso.mobile.model.User
//import com.cuso.mobile.ui.theme.lightGray
//import com.cuso.mobile.viewmodel.Authenticate
//
//// ─────────────────────────────────────────────────────────────
//// Data Classes for Menu Configuration
//// ─────────────────────────────────────────────────────────────
//
////data class MenuItem(
////    val icon: Int,
////    val label: String,
////    val isPanel: Boolean = false,
////    val categories: List<String> = emptyList(),
////    val subItems: Map<String, List<String>> = emptyMap()
////)
////
////data class SidebarState(
////    val isOpen: Boolean = false,
////    val selectedMenu: String = "Home",
////    val expandedCategory: String? = null,
////    val selectedSubItem: String? = null,
////    val isPanelMode: Boolean = false
////)
//
//// ─────────────────────────────────────────────────────────────
//// SalesSettingsSidebar - Custom sidebar for Sales Settings
//// ─────────────────────────────────────────────────────────────
//@Composable
//fun SalesSettingsSidebar(
//    modifier: Modifier = Modifier,
//    isOpen: Boolean,
//    onClose: () -> Unit,
//    onMenuItemClick: (String) -> Unit,
//    onLogout: () -> Unit,
//    user: User? = null
//) {
//    // Only two menu items: Home and Sales
//    val menuItems = listOf(
//        MenuItem(R.drawable.home, "Home"),
//        MenuItem(
//            R.drawable.sales,
//            "Sales",
//            isPanel = true,
//            categories = listOf("Garment Type"),
//            subItems = mapOf("Garment Type" to listOf("Garment Type"))
//        )
//    )
//
//    var selectedMenu by remember { mutableStateOf("Sales") } // Default to Sales
//    var expandedCategory by remember { mutableStateOf<String?>("Garment Type") }
//    var selectedSubItem by remember { mutableStateOf<String?>("Garment Type::Garment Type") }
//    var menuExpanded by remember { mutableStateOf(false) }
//
//    val isPanelMode = selectedMenu == "Sales"
//    val activeMenuItem = menuItems.find { it.label == selectedMenu }
//    val activeCategories = activeMenuItem?.categories ?: emptyList()
//    val activeSubItems = activeMenuItem?.subItems ?: emptyMap()
//
//    fun handleMenuClick(label: String) {
//        selectedMenu = label
//        if (label == "Sales") {
//            expandedCategory = "Garment Type"
//            selectedSubItem = "Garment Type::Garment Type"
//            onMenuItemClick("sales_garment_type")
//        } else {
//            expandedCategory = null
//            selectedSubItem = null
//            onMenuItemClick(label.lowercase())
//        }
//    }
//
//    // ── Dim overlay ─────────────────────────────────────────
//    if (isOpen) {
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .background(Color.Black.copy(alpha = 0.3f))
//                .clickable { onClose() }
//                .zIndex(1f)
//        )
//    }
//
//    // ── Sidebar panels ──────────────────────────────────────
//    AnimatedVisibility(
//        visible = isOpen,
//        enter = slideInHorizontally(initialOffsetX = { -it }),
//        exit = slideOutHorizontally(targetOffsetX = { -it }),
//        modifier = modifier.zIndex(2f)
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxHeight()
//                .width(IntrinsicSize.Max)
//        ) {
//            // ── Left icon rail ───────────────────────────────
//            SalesSettingsIconRail(
//                modifier = Modifier
//                    .width(if (isPanelMode) 86.dp else 280.dp)
//                    .fillMaxHeight()
//                    .background(Color.White)
//                    .border(0.5.dp, Color(0xFFE0E0E0)),
//                menuItems = menuItems,
//                selectedMenu = selectedMenu,
//                isPanelMode = isPanelMode,
//                user = user,
//                menuExpanded = menuExpanded,
//                onMenuExpandedChange = { menuExpanded = it },
//                onMenuItemClick = { handleMenuClick(it) },
//                onLogout = onLogout
//            )
//
//            // ── Right accordion panel ────────────────────────
//            if (isPanelMode) {
//                SalesSettingsAccordionPanel(
//                    modifier = Modifier
//                        .width(220.dp)
//                        .fillMaxHeight()
//                        .background(Color.White),
//                    selectedMenu = selectedMenu,
//                    activeCategories = activeCategories,
//                    activeSubItems = activeSubItems,
//                    expandedCategory = expandedCategory,
//                    selectedSubItem = selectedSubItem,
//                    onCategoryClick = {
//                        expandedCategory = if (expandedCategory == it) null else it
//                    },
//                    onSubItemClick = { category, subItem ->
//                        selectedSubItem = "$category::$subItem"
//                        onMenuItemClick("sales_garment_type")
//                        onClose()
//                    },
//                    onClose = onClose
//                )
//            }
//        }
//    }
//}
//
//// ─────────────────────────────────────────────────────────────
//// Sales Settings Icon Rail
//// ─────────────────────────────────────────────────────────────
//@Composable
//private fun SalesSettingsIconRail(
//    modifier: Modifier = Modifier,
//    menuItems: List<MenuItem>,
//    selectedMenu: String,
//    isPanelMode: Boolean,
//    user: User?,
//    menuExpanded: Boolean,
//    onMenuExpandedChange: (Boolean) -> Unit,
//    onMenuItemClick: (String) -> Unit,
//    onLogout: () -> Unit
//) {
//    Column(modifier = modifier) {
//
//        // Logo
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 16.dp, vertical = 20.dp),
//            contentAlignment = if (isPanelMode) Alignment.Center else Alignment.CenterStart
//        ) {
//            if (isPanelMode) {
//                Icon(
//                    painter = painterResource(id = R.drawable.cuso_logo),
//                    contentDescription = "Logo",
//                    modifier = Modifier.size(48.dp),
//                    tint = Color.Unspecified
//                )
//            } else {
//                Icon(
//                    painter = painterResource(R.drawable.logo),
//                    contentDescription = "Logo",
//                    tint = Color.Unspecified
//                )
//            }
//        }
//
//        HorizontalDivider(color = Color(0xFFF0F0F0))
//        Spacer(Modifier.height(8.dp))
//
//        // Menu list - Only Home and Sales
//        Column(
//            modifier = Modifier
//                .weight(1f)
//                .verticalScroll(rememberScrollState())
//        ) {
//            menuItems.forEach { item ->
//                val isSelected = selectedMenu == item.label
//
//                if (isPanelMode) {
//                    // Compact icon + label
//                    Column(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(horizontal = 8.dp, vertical = 3.dp)
//                            .clickable { onMenuItemClick(item.label) }
//                            .padding(vertical = 8.dp),
//                        horizontalAlignment = Alignment.CenterHorizontally,
//                        verticalArrangement = Arrangement.spacedBy(4.dp)
//                    ) {
//                        Box(
//                            modifier = Modifier
//                                .size(38.dp)
//                                .clip(RoundedCornerShape(12.dp))
//                                .background(if (isSelected) Color(0xFFE3E0FB) else Color.Transparent),
//                            contentAlignment = Alignment.Center
//                        ) {
//                            Icon(
//                                painter = painterResource(id = item.icon),
//                                contentDescription = item.label,
//                                tint = if (isSelected) Color(0xFF4338CA) else Color(0xFF6B7280),
//                                modifier = Modifier.size(22.dp)
//                            )
//                        }
//                        Text(
//                            text = item.label,
//                            fontSize = 10.sp,
//                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
//                            color = if (isSelected) Color(0xFF4338CA) else Color(0xFF6B7280),
//                            maxLines = 1,
//                            overflow = TextOverflow.Ellipsis,
//                            textAlign = TextAlign.Center
//                        )
//                    }
//                } else {
//                    // Full width row
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(horizontal = 12.dp, vertical = 4.dp)
//                            .clip(RoundedCornerShape(10.dp))
//                            .background(if (isSelected) Color(0xFF3B3BF9) else Color.Transparent)
//                            .clickable { onMenuItemClick(item.label) }
//                            .padding(horizontal = 16.dp, vertical = 14.dp),
//                        verticalAlignment = Alignment.CenterVertically,
//                        horizontalArrangement = Arrangement.spacedBy(14.dp)
//                    ) {
//                        Icon(
//                            painter = painterResource(id = item.icon),
//                            contentDescription = item.label,
//                            tint = if (isSelected) Color.White else Color.Gray,
//                            modifier = Modifier.size(22.dp)
//                        )
//                        Text(
//                            text = item.label,
//                            fontSize = 15.sp,
//                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
//                            color = if (isSelected) Color.White else Color.Black
//                        )
//                    }
//                }
//            }
//        }
//
//        HorizontalDivider(color = Color(0xFFF0F0F0))
//
//        // User profile + logout
//        SidebarUserProfile(
//            user = user,
//            isPanelMode = isPanelMode,
//            menuExpanded = menuExpanded,
//            onMenuExpandedChange = onMenuExpandedChange,
//            onLogout = onLogout
//        )
//    }
//}
//
//// ─────────────────────────────────────────────────────────────
//// Sales Settings Accordion Panel
//// ─────────────────────────────────────────────────────────────
//@Composable
//private fun SalesSettingsAccordionPanel(
//    modifier: Modifier = Modifier,
//    selectedMenu: String,
//    activeCategories: List<String>,
//    activeSubItems: Map<String, List<String>>,
//    expandedCategory: String?,
//    selectedSubItem: String?,
//    onCategoryClick: (String) -> Unit,
//    onSubItemClick: (String, String) -> Unit,
//    onClose: () -> Unit
//) {
//    Column(modifier = modifier) {
//
//        // Header — title + close
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(start = 20.dp, end = 16.dp, top = 22.dp, bottom = 14.dp),
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            Text(
//                text = selectedMenu,
//                fontSize = 19.sp,
//                fontWeight = FontWeight.Bold,
//                color = Color(0xFF1F2937)
//            )
//            Icon(
//                imageVector = Icons.Default.Close,
//                contentDescription = "Close sidebar",
//                tint = Color(0xFF374151),
//                modifier = Modifier
//                    .size(20.dp)
//                    .clickable { onClose() }
//            )
//        }
//
//        // Accordion list - Only Garment Type
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(horizontal = 12.dp)
//                .verticalScroll(rememberScrollState())
//        ) {
//            activeCategories.forEach { category ->
//                val isExpanded = expandedCategory == category
//
//                Column(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(vertical = 4.dp)
//                ) {
//                    // Category row - Garment Type
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .clip(RoundedCornerShape(10.dp))
//                            .background(if (isExpanded) Color(0xFFE9E7FC) else Color.Transparent)
//                            .clickable { onCategoryClick(category) }
//                            .padding(horizontal = 14.dp, vertical = 13.dp),
//                        verticalAlignment = Alignment.CenterVertically,
//                        horizontalArrangement = Arrangement.SpaceBetween
//                    ) {
//                        Text(
//                            text = category,
//                            fontSize = 14.sp,
//                            fontWeight = if (isExpanded) FontWeight.SemiBold else FontWeight.Medium,
//                            color = if (isExpanded) Color(0xFF4338CA) else Color(0xFF374151)
//                        )
//                        Icon(
//                            imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp
//                            else Icons.Filled.KeyboardArrowDown,
//                            contentDescription = null,
//                            tint = if (isExpanded) Color(0xFF4338CA) else Color(0xFF9CA3AF)
//                        )
//                    }
//
//                    // Sub-items - Only Garment Type
//                    if (isExpanded) {
//                        val subItems = activeSubItems[category].orEmpty()
//                        Column(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(start = 12.dp, end = 14.dp, bottom = 12.dp)
//                        ) {
//                            subItems.forEachIndexed { index, subItem ->
//                                val isLast = index == subItems.lastIndex
//                                val isSubSelected = selectedSubItem == "$category::$subItem"
//
//                                Box(modifier = Modifier.fillMaxWidth()) {
//                                    // Tree connector line
//                                    Canvas(modifier = Modifier.matchParentSize()) {
//                                        val strokeColor = Color(0xFFD1D5DB)
//                                        val lineX = 8.dp.toPx()
//                                        val curveBottomY = 24.dp.toPx()
//                                        val cornerRadius = 12.dp.toPx()
//                                        val horizontalEndX = lineX + 16.dp.toPx()
//
//                                        val path = Path().apply {
//                                            moveTo(lineX, 0f)
//                                            lineTo(lineX, curveBottomY - cornerRadius)
//                                            arcTo(
//                                                rect = Rect(
//                                                    left = lineX,
//                                                    top = curveBottomY - 2 * cornerRadius,
//                                                    right = lineX + 2 * cornerRadius,
//                                                    bottom = curveBottomY
//                                                ),
//                                                startAngleDegrees = 180f,
//                                                sweepAngleDegrees = -90f,
//                                                forceMoveTo = false
//                                            )
//                                            lineTo(horizontalEndX, curveBottomY)
//                                        }
//                                        drawPath(
//                                            path = path,
//                                            color = strokeColor,
//                                            style = Stroke(width = 1.dp.toPx())
//                                        )
//                                        if (!isLast) {
//                                            drawLine(
//                                                color = strokeColor,
//                                                start = Offset(lineX, curveBottomY),
//                                                end = Offset(lineX, size.height),
//                                                strokeWidth = 0.5.dp.toPx()
//                                            )
//                                        }
//                                    }
//
//                                    // Sub-item label - Garment Type
//                                    Text(
//                                        text = subItem,
//                                        fontSize = 13.sp,
//                                        fontWeight = if (isSubSelected) FontWeight.Bold else FontWeight.Normal,
//                                        color = if (isSubSelected) Color(0xFF3B3BF9) else Color(0xFF424662),
//                                        modifier = Modifier
//                                            .align(Alignment.CenterStart)
//                                            .padding(start = 26.dp, top = 12.dp, bottom = 6.dp)
//                                            .clickable { onSubItemClick(category, subItem) }
//                                    )
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
//
//// ─────────────────────────────────────────────────────────────
//// User Profile + Logout Dropdown (Reusable)
//// ─────────────────────────────────────────────────────────────
//@Composable
//fun SidebarUserProfile(
//    user: User?,
//    isPanelMode: Boolean,
//    menuExpanded: Boolean,
//    onMenuExpandedChange: (Boolean) -> Unit,
//    onLogout: () -> Unit
//) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(horizontal = 16.dp, vertical = if (isPanelMode) 14.dp else 16.dp)
//            .clickable { onMenuExpandedChange(true) },
//        verticalAlignment = Alignment.CenterVertically,
//        horizontalArrangement = if (isPanelMode) Arrangement.Center else Arrangement.spacedBy(12.dp)
//    ) {
//        if (!user?.profilePicture.isNullOrBlank()) {
//            AsyncImage(
//                model = user!!.profilePicture,
//                contentDescription = "Profile picture",
//                modifier = Modifier
//                    .size(if (isPanelMode) 38.dp else 42.dp)
//                    .clip(CircleShape),
//                contentScale = ContentScale.Crop
//            )
//        } else {
//            val initials = buildString {
//                user?.firstName?.firstOrNull()?.let { append(it.uppercaseChar()) }
//                user?.lastName?.firstOrNull()?.let { append(it.uppercaseChar()) }
//            }
//            Box(
//                modifier = Modifier
//                    .size(if (isPanelMode) 38.dp else 42.dp)
//                    .clip(CircleShape)
//                    .background(Color(0xFF3B3BF9)),
//                contentAlignment = Alignment.Center
//            ) {
//                Text(
//                    text = initials,
//                    color = Color.White,
//                    fontWeight = FontWeight.Bold,
//                    fontSize = 13.sp
//                )
//            }
//        }
//
//        if (!isPanelMode) {
//            Column {
//                Text(
//                    text = "${user?.firstName.orEmpty()} ${user?.lastName.orEmpty()}".trim().ifBlank { "—" },
//                    fontSize = 14.sp,
//                    fontWeight = FontWeight.Bold,
//                    color = Color.Black
//                )
//                Text(
//                    text = user?.email.orEmpty(),
//                    fontSize = 12.sp,
//                    color = Color.Gray,
//                    maxLines = 1,
//                    overflow = TextOverflow.Ellipsis
//                )
//            }
//        }
//    }
//
//    DropdownMenu(
//        expanded = menuExpanded,
//        onDismissRequest = { onMenuExpandedChange(false) },
//        offset = DpOffset(x = 10.dp, y = (-90).dp),
//        shape = RoundedCornerShape(8.dp),
//        containerColor = Color.White,
//        tonalElevation = 8.dp,
//        shadowElevation = 12.dp
//    ) {
//        DropdownMenuItem(
//            text = { Text("Logout", color = Color.Red) },
//            onClick = {
//                onMenuExpandedChange(false)
//                onLogout()
//            }
//        )
//    }
//}
//
//// ─────────────────────────────────────────────────────────────
//// Sales Settings Screen
//// ─────────────────────────────────────────────────────────────
//@Composable
//fun SalesSettingsScreen(navController: NavController) {
//    val authViewModel: Authenticate = hiltViewModel()
//    val userEntity by authViewModel.user.collectAsStateWithLifecycle()
//
//    // Map UserEntity → User
//    val user: User? = userEntity?.let {
//        User(
//            firstName = it.firstName.orEmpty(),
//            lastName = it.lastName.orEmpty(),
//            email = it.email.orEmpty(),
//            profilePicture = it.profilePicture.orEmpty(),
//            organizationId = Organization(
//                _id = it.organizationId,
//                businessId = "",
//                name = "",
//                industry = "",
//                orgType = "",
//                organizationPicture = null,
//                organizationPictureId = null,
//                domains = emptyList(),
//                email = "",
//                mobile = "",
//                orgSetupComplete = false,
//                totalMembers = 0,
//                activeMembers = 0,
//                segments = emptyList(),
//                branches = emptyList(),
//                isTaxId = false,
//                status = "",
//                createdAt = "",
//                updatedAt = "",
//                slug = "",
//                __v = 0,
//                defaultBranch = "",
//                ownerId = "",
//                ownerMemberId = "",
//                businessType = "",
//                taxId = "",
//                isInternalOrganization = false,
//                subscription = Subscription(
//                    memberLimit = 0,
//                    featuresEnabled = emptyList()
//                ),
//                settings = Settings(
//                    country = "",
//                    state = "",
//                    portalName = "",
//                    termsAccepted = false,
//                    marketingEmails = false,
//                    workingDays = emptyList(),
//                    timezone = "",
//                    currency = "",
//                    language = "",
//                    address = "",
//                    city = "",
//                    pincode = ""
//                )
//            ),
//            role = it.role.orEmpty()
//        )
//    }
//
//    var isDrawerOpen by remember { mutableStateOf(false) }
//    var currentScreen by remember { mutableStateOf("garment_type") }
//
//    // Handle sidebar menu clicks
//    fun handleSidebarClick(route: String) {
//        when {
//            route == "home" -> {
//                navController.popBackStack()
//            }
//            route == "sales_garment_type" -> {
//                currentScreen = "garment_type"
//            }
//        }
//    }
//
//    Box(modifier = Modifier.fillMaxSize()) {
//        // ── Custom Sidebar ──
//        SalesSettingsSidebar(
//            isOpen = isDrawerOpen,
//            onClose = { isDrawerOpen = false },
//            onMenuItemClick = { route ->
//                handleSidebarClick(route)
//                isDrawerOpen = false
//            },
//            onLogout = {
//                // Handle logout
//                navController.navigate("login") {
//                    popUpTo(0) { inclusive = true }
//                }
//            },
//            user = user
//        )
//
//        // ── Main Content ──
//        Column(
//            Modifier
//                .fillMaxSize()
//                .background(Color.White)
//        ) {
//            // Custom Top Bar with menu button
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .background(Color.White)
//                    .padding(start = 30.dp, end = 30.dp, top = 8.dp, bottom = 8.dp),
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.spacedBy(18.dp)
//            ) {
//                // Menu Button - Opens custom sidebar
//                Box(
//                    modifier = Modifier
//                        .size(40.dp)
//                        .background(lightGray, RoundedCornerShape(8.dp))
//                        .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
//                        .clickable { isDrawerOpen = true },
//                    contentAlignment = Alignment.Center
//                ) {
//                    Icon(
//                        Icons.Default.Menu,
//                        contentDescription = "Menu",
//                        modifier = Modifier.size(22.dp),
//                        tint = Color.Black
//                    )
//                }
//
//                Spacer(modifier = Modifier.weight(1f))
//
//                // Close button
//                IconButton(
//                    onClick = { navController.popBackStack() }
//                ) {
//                    Icon(
//                        Icons.Default.Close,
//                        contentDescription = "Close",
//                        tint = Color.DarkGray
//                    )
//                }
//            }
//
//            HorizontalDivider(color = Color(0xFFF2F2F2))
//
//            // Content based on current screen
//            when (currentScreen) {
//                "garment_type" -> GarmentTypeScreen()
//                else -> GarmentTypeScreen()
//            }
//        }
//    }
//}
//
//// ─────────────────────────────────────────────────────────────
//// Garment Type Screen
//// ─────────────────────────────────────────────────────────────
//@Composable
//fun GarmentTypeScreen() {
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(24.dp),
//        verticalArrangement = Arrangement.Top
//    ) {
//        Text(
//            text = "Garment Type Settings",
//            fontSize = 24.sp,
//            fontWeight = FontWeight.Bold,
//            color = Color(0xFF111827)
//        )
//        Spacer(Modifier.height(8.dp))
//        Text(
//            text = "Manage your garment categories and types",
//            fontSize = 14.sp,
//            color = Color(0xFF6B7280)
//        )
//        Spacer(Modifier.height(24.dp))
//
//        // Garment type content - Shows Pants and Shirts like in your first image
//        Card(
//            modifier = Modifier.fillMaxWidth(),
//            shape = RoundedCornerShape(12.dp),
//            colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB))
//        ) {
//            Column(
//                modifier = Modifier.padding(16.dp)
//            ) {
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    GarmentTypeChip("Pants")
//                    GarmentTypeChip("Shirts")
//                    GarmentTypeChip("Suits")
//                }
//                Spacer(Modifier.height(16.dp))
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    GarmentTypeChip("T-Shirts")
//                    GarmentTypeChip("Jeans")
//                    GarmentTypeChip("Jackets")
//                }
//                Spacer(Modifier.height(16.dp))
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    GarmentTypeChip("Dresses")
//                    GarmentTypeChip("Skirts")
//                    GarmentTypeChip("Blazers")
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun GarmentTypeChip(label: String) {
//    Box(
//        modifier = Modifier
//            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(50.dp))
//            .background(Color.White, RoundedCornerShape(50.dp))
//            .padding(horizontal = 16.dp, vertical = 8.dp)
//    ) {
//        Text(
//            label,
//            fontSize = 14.sp,
//            color = Color(0xFF374151)
//        )
//    }
//}