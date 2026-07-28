package com.cuso.mobile.view.home.sidebar

import android.content.Context
import com.cuso.mobile.model.User
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.cuso.mobile.R
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import com.cuso.mobile.ui.theme.BorderGray
import com.cuso.mobile.ui.theme.modelGray

// ─────────────────────────────────────────────────────────────
// Data Classes for Menu Configuration
// ─────────────────────────────────────────────────────────────

data class MenuItem(
    val icon: Int,
    val label: String,
    val enabled: Boolean = true,
    val isPanel: Boolean = false,
    val categories: List<String> = emptyList(),
    val subItems: Map<String, List<String>> = emptyMap()
)

// ─────────────────────────────────────────────────────────────
// Navigation Key Builder  (top-level — visible to all composables)
// ─────────────────────────────────────────────────────────────

fun buildNavigationKey(menu: String, subItem: String): String {
    if (menu == "Home") {
        return when (subItem) {
            "Organization Profile" -> "home_organization_profile"
            "Branch Management"    -> "home_branch_management"
            "Department & Teams"   -> "home_department_teams"
            "Designation"          -> "home_designation"
            else -> "home_${subItem.lowercase().replace(" ", "_").replace("&", "and")}"
        }
    }
    val menuKey    = menu.lowercase().replace(" ", "_").replace("&", "and")
    val subItemKey = subItem.lowercase().replace(" ", "_").replace("&", "and")
    return "${menuKey}_${subItemKey}"
}

// ─────────────────────────────────────────────────────────────
// Sidebar Configuration
// ─────────────────────────────────────────────────────────────

object SidebarConfig {

    fun getFullMenuItems(): List<MenuItem> {
        return listOf(
            MenuItem(
                R.drawable.home, "Home",
                isPanel = false,
                categories = emptyList(),
                subItems = emptyMap()
            ),
            MenuItem(
                R.drawable.sales, "Sales",
                isPanel = true,
                categories = listOf(
                    "Lead Management", "Customer", "Measurements",
                    "Sales & Orders", "Order Management", "Pricing & Quotes"
                ),
                subItems = mapOf(
                    "Pricing & Quotes"         to listOf("Pricing Overview","Quotation")
                )
            ),
            MenuItem(
                R.drawable.marketing, "Marketing",
                isPanel = true,
                categories = listOf("Website", "Campaigns", "Leads & Audience", "Engagement", "Growth", "Pages", "Budget", "Team"),
                subItems = mapOf(
                    "Campaigns"       to listOf("Campaigns", "Promotions", "Marketing & Calendar"),
                    "Leads & Audience" to listOf("Lead Generation", "Customer Segmentation"),
                    "Engagement"      to listOf("Customer Engagement", "WhatsApp", "Social Media", "Review & Feedback"),
                    "Growth"          to listOf("Referral Program", "Influencer"),
                    "Team"            to listOf("Marketing Tasks", "Team Management")
                )
            ),
            MenuItem(
                R.drawable.finance, "Finance",
                isPanel = true,
                categories = listOf(
                    "Accounts Receivable", "Accounts Payable", "Expenses",
                    "Finance Core"
                ),
                subItems = mapOf(
                    "Accounts Receivable" to listOf("Sales Invoices", "Customers", "Payments Received"),
                    "Accounts Payable"    to listOf("Suppliers", "Purchase Invoices", "Payments Mode"),
                    "Finance Core"        to listOf("Chart of Accounts","Journal Entries", "Trial Balance")
                )
            ),
            MenuItem(
                R.drawable.inventory, "Inventory",
                isPanel = true,
                categories = listOf("Items", "Procurement", "Payables"),
                subItems = mapOf(
                    "Items"       to listOf("All Items", "Item Groups"),
                    "Procurement" to listOf("Suppliers", "Requisitions", "Orders", "Goods Receipt"),
                    "Payables"    to listOf("Invoices", "Payments", "Credits")
                )
            ),
            MenuItem(
                R.drawable.logistics, "Logistics",
                isPanel = true,
                categories = listOf("Delivery", "Returns", "Order Tracking")
            ),
            MenuItem(
                R.drawable.services, "Services",
                isPanel = true,
                categories = listOf("Service Request", "Alteration Management", "Return", "Damaged Goods", "Customer Feedback")
            ),
            MenuItem(R.drawable.hr, "HR", isPanel = true,
                categories = listOf("Employees")
            ),
            MenuItem(R.drawable.it, "IT", isPanel = true,
                categories = listOf("Integrations")
            ),
            MenuItem(R.drawable.legal, "Legal", isPanel = true,
                categories = listOf("Legal Management")
            ),
            MenuItem(
                R.drawable.security, "Security",
                isPanel = true,
                categories = listOf("Access Control", "Auth & Verification", "Monitoring & Audit"),
                subItems = mapOf(
                    "Access Control"      to listOf("User Accounts", "Roles & Permissions"),
                    "Auth & Verification" to listOf("Multi Factor (MFA)", "SSO Settings"),
                    "Monitoring & Audit"  to listOf("Login Logs", "Activity Logs")
                )
            ),
            MenuItem(
                R.drawable.reports, "Reports",
                isPanel = true,
                categories = listOf("Sales Reports", "Finance Reports")
            )
        )
    }

    fun getSalesMenuItems(): List<MenuItem> {
        return listOf(
            MenuItem(
                R.drawable.home, "Home",
                enabled = true,
                isPanel = true,
                categories = listOf("Organization Profile", "Branch Management", "Department & Teams", "Designation"),
                subItems = mapOf(
                    "Organization Profile" to listOf("Organization Profile"),
                    "Branch Management"    to listOf("Branch Management"),
                    "Department & Teams"   to listOf("Department & Teams"),
                    "Designation"          to listOf("Designation")
                )
            ),
            MenuItem(
                R.drawable.sales, "Sales",
                enabled = true,
                isPanel = true,
                categories = listOf("Garment Type"),
                subItems = mapOf("Garment Type" to listOf("Garment Type"))
            ),
            MenuItem(R.drawable.marketing, "Marketing", enabled = false, isPanel = true),
            MenuItem(R.drawable.finance,   "Finance",   enabled = false, isPanel = true),
            MenuItem(R.drawable.inventory, "Inventory", enabled = false, isPanel = true),
            MenuItem(R.drawable.logistics, "Logistics", enabled = false, isPanel = true),
            MenuItem(R.drawable.services,  "Services",  enabled = false, isPanel = true),
            MenuItem(R.drawable.hr,        "HR",        enabled = false, isPanel = true),
            MenuItem(R.drawable.it,        "IT",        enabled = false, isPanel = true),
            MenuItem(R.drawable.legal,     "Legal",     enabled = false, isPanel = true),
            MenuItem(R.drawable.security,  "Security",  enabled = false, isPanel = true),
            MenuItem(R.drawable.reports,   "Reports",   enabled = false, isPanel = true)
        )
    }
}

// ─────────────────────────────────────────────────────────────
// 🏠 FULL NAV BAR
// ─────────────────────────────────────────────────────────────

@Composable
fun FullSideBar(
    isOpen: Boolean,
    onClose: () -> Unit,
    onMenuItemClick: (String) -> Unit,
    onLogout: () -> Unit,
    user: User? = null,
    defaultSelectedMenu: String = "Home"
) {
    AppSidebarContent(
        isOpen = isOpen,
        onClose = onClose,
        onMenuItemClick = onMenuItemClick,
        onLogout = onLogout,
        user = user,
        menuItems = SidebarConfig.getFullMenuItems(),
        defaultSelectedMenu = defaultSelectedMenu,
        isSalesMode = false
    )
}

// ─────────────────────────────────────────────────────────────
// 📊 SALES NAV BAR
// ─────────────────────────────────────────────────────────────

@Composable
fun SalesSideBar(
    isOpen: Boolean,
    onClose: () -> Unit,
    onMenuItemClick: (String) -> Unit,
    onLogout: () -> Unit,
    user: User? = null,
    defaultSelectedMenu: String = "Sales"
) {
    AppSidebarContent(
        isOpen = isOpen,
        onClose = onClose,
        onMenuItemClick = onMenuItemClick,
        onLogout = onLogout,
        user = user,
        menuItems = SidebarConfig.getSalesMenuItems(),
        defaultSelectedMenu = defaultSelectedMenu,
        isSalesMode = true
    )
}

// ─────────────────────────────────────────────────────────────
// 🧩 Reusable Sidebar Content
// ─────────────────────────────────────────────────────────────
@Suppress("UNUSED_PARAMETER")

@Composable
private fun AppSidebarContent(
    isOpen: Boolean,
    onClose: () -> Unit,
    onMenuItemClick: (String) -> Unit,
    onLogout: () -> Unit,
    user: User?,
    menuItems: List<MenuItem>,
    defaultSelectedMenu: String,
    isSalesMode: Boolean
) {
    var selectedMenu by remember { mutableStateOf(defaultSelectedMenu) }
    var expandedCategory by remember { mutableStateOf<String?>(null) }
    var selectedSubItem by remember { mutableStateOf<String?>(null) }
    var menuExpanded by remember { mutableStateOf(false) }
    var burgerMenuExpanded by remember { mutableStateOf(false) }

    val selectedMenuItem = menuItems.find { it.label == selectedMenu }
    val isPanelMode = selectedMenuItem?.isPanel == true && selectedMenuItem.categories.isNotEmpty()

    val activeCategories = selectedMenuItem?.categories ?: emptyList()
    val activeSubItems   = selectedMenuItem?.subItems   ?: emptyMap()

    val context = LocalContext.current

    // ── handleMenuClick ───────────────────────────────────────
    fun handleMenuClick(label: String) {
        val menuItem = menuItems.find { it.label == label }
        if (menuItem?.enabled == false) return

        ModuleUsageTracker.recordUsage(context, label)
        selectedMenu = label
        expandedCategory = null
        burgerMenuExpanded = false

        val hasPanel = menuItem?.isPanel == true && menuItem.categories.isNotEmpty()
        if (hasPanel) {
            val firstCategory = menuItem.categories.firstOrNull()
            val firstSubItem  = firstCategory?.let { menuItem.subItems[it]?.firstOrNull() }
            if (firstCategory != null && firstSubItem != null) {
                expandedCategory = firstCategory
                selectedSubItem  = "$firstCategory::$firstSubItem"
                onMenuItemClick(buildNavigationKey(label, firstSubItem))
            }
        } else {
            onMenuItemClick(label.lowercase())
        }
    }

    // ── handleCategoryClick ───────────────────────────────────
    fun handleCategoryClick(category: String) {
        expandedCategory = if (expandedCategory == category) null else category
    }

    // ── handleSubItemClick ────────────────────────────────────
    fun handleSubItemClick(category: String, subItem: String) {
        selectedSubItem = "$category::$subItem"
        onMenuItemClick(buildNavigationKey(selectedMenu, subItem))
        onClose()
    }

    // ── Scrim ─────────────────────────────────────────────────
    if (isOpen) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
                .clickable { onClose() }
                .zIndex(1f)
        )
    }

    // ── Drawer ────────────────────────────────────────────────
    AnimatedVisibility(
        visible = isOpen,
        enter = slideInVertically(initialOffsetY = { fullHeight -> fullHeight }),
        exit  = slideOutVertically(targetOffsetY = { fullHeight -> fullHeight }),
        modifier = Modifier.zIndex(2f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .width(IntrinsicSize.Max)
        ) {
            SidebarIconRail(
                modifier = Modifier
                    .width(if (isPanelMode) 86.dp else 280.dp)
                    .fillMaxHeight()
                    .background(Color.White)
                    .border(0.5.dp, Color(0xFFE0E0E0)),
                menuItems = menuItems,
                selectedMenu = selectedMenu,
                isPanelMode = isPanelMode,
                user = user,
                menuExpanded = menuExpanded,
                onMenuExpandedChange = { menuExpanded = it },
                onMenuItemClick = { handleMenuClick(it) },
                onLogout = onLogout
            )

            if (isPanelMode) {
                SidebarAccordionPanel(
                    modifier = Modifier
                        .width(220.dp)
                        .fillMaxHeight()
                        .background(Color.White),
                    selectedMenu = selectedMenu,
                    activeCategories = activeCategories,
                    activeSubItems = activeSubItems,
                    expandedCategory = expandedCategory,
                    selectedSubItem = selectedSubItem,
                    burgerMenuExpanded = burgerMenuExpanded,
                    isHomeMenu = selectedMenu == "Home",
                    onCategoryClick = { handleCategoryClick(it) },
                    onSubItemClick  = { category, subItem -> handleSubItemClick(category, subItem) },
                    onClose = onClose
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Sidebar Icon Rail
// ─────────────────────────────────────────────────────────────

@Composable
private fun SidebarIconRail(
    modifier: Modifier = Modifier,
    menuItems: List<MenuItem>,
    selectedMenu: String,
    isPanelMode: Boolean,
    user: User?,
    menuExpanded: Boolean,
    onMenuExpandedChange: (Boolean) -> Unit,
    onMenuItemClick: (String) -> Unit,
    onLogout: () -> Unit
) {
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            contentAlignment = if (isPanelMode) Alignment.Center else Alignment.CenterStart
        ) {
            if (isPanelMode) {
                Icon(
                    painter = painterResource(id = R.drawable.cuso_logo),
                    contentDescription = "Logo",
                    modifier = Modifier.size(48.dp),
                    tint = Color.Unspecified
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.logo),
                    contentDescription = "Logo",
                    tint = Color.Unspecified
                )
            }
        }

        HorizontalDivider(color = Color(0xFFF0F0F0))
        Spacer(Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            menuItems.forEach { item ->
                val isSelected = selectedMenu == item.label

                if (isPanelMode) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                            .clickable(enabled = item.enabled) { onMenuItemClick(item.label) }
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    when {
                                        isSelected    -> Color(0xFFE3E0FB)
                                        !item.enabled -> Color(0xFFF5F5F5)
                                        else          -> Color.Transparent
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = item.icon),
                                contentDescription = item.label,
                                tint = when {
                                    !item.enabled -> Color(0xFFD1D5DB)
                                    isSelected    -> Color(0xFF4338CA)
                                    else          -> Color(0xFF6B7280)
                                },
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Text(
                            text = item.label,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = when {
                                !item.enabled -> Color(0xFFD1D5DB)
                                isSelected    -> Color(0xFF4338CA)
                                else          -> Color(0xFF6B7280)
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 2.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) Color(0xFF3B3BF9) else Color.Transparent)
                            .clickable(enabled = item.enabled) { onMenuItemClick(item.label) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = item.icon),
                            contentDescription = item.label,
                            tint = when {
                                !item.enabled -> Color(0xFFD1D5DB)
                                isSelected    -> Color.White
                                else          -> Color(0xFF6B7280)
                            },
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = item.label,
                            fontSize = 15.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = when {
                                !item.enabled -> Color(0xFFD1D5DB)
                                isSelected    -> Color.White
                                else          -> Color(0xFF111827)
                            }
                        )
                    }
                }
            }
        }

        HorizontalDivider(color = Color(0xFFF0F0F0))

        SidebarUserProfile(
            user = user,
            isPanelMode = isPanelMode,
            menuExpanded = menuExpanded,
            onMenuExpandedChange = onMenuExpandedChange,
            onLogout = onLogout
        )
    }
}

// ─────────────────────────────────────────────────────────────
// User Profile + Logout Dropdown
// ─────────────────────────────────────────────────────────────

@Composable
fun SidebarUserProfile(
    user: User?,
    isPanelMode: Boolean,
    menuExpanded: Boolean,
    onMenuExpandedChange: (Boolean) -> Unit,
    onLogout: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = if (isPanelMode) 14.dp else 16.dp)
            .clickable { onMenuExpandedChange(true) },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (isPanelMode) Arrangement.Center else Arrangement.spacedBy(12.dp)
    ) {
        val profilePicture = user?.profilePicture
        if (!profilePicture.isNullOrBlank()) {
            AsyncImage(
                model = profilePicture,
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
                Text(
                    text = initials,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }

        if (!isPanelMode) {
            Column {
                Text(
                    text = "${user?.firstName.orEmpty()} ${user?.lastName.orEmpty()}".trim().ifBlank { "—" },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = user?.email.orEmpty(),
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }

    DropdownMenu(
        expanded = menuExpanded,
        onDismissRequest = { onMenuExpandedChange(false) },
        offset = DpOffset(x = 10.dp, y = (-90).dp),
        shape = RoundedCornerShape(8.dp),
        containerColor = Color.White,
        tonalElevation = 8.dp,
        shadowElevation = 12.dp
    ) {
        DropdownMenuItem(
            text = { Text("Logout", color = Color.Red) },
            onClick = {
                onMenuExpandedChange(false)
                onLogout()
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────
// Sidebar Accordion Panel
// ─────────────────────────────────────────────────────────────

@Composable
private fun SidebarAccordionPanel(
    modifier: Modifier = Modifier,
    selectedMenu: String,
    activeCategories: List<String>,
    activeSubItems: Map<String, List<String>>,
    expandedCategory: String?,
    selectedSubItem: String?,
    burgerMenuExpanded: Boolean,
    isHomeMenu: Boolean,
    onCategoryClick: (String) -> Unit,
    onSubItemClick: (String, String) -> Unit,
    onClose: () -> Unit
) {
    Column(modifier = modifier) {
        // ── Header ────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 16.dp, top = 22.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isHomeMenu && activeCategories.isNotEmpty()) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = Color(0xFF374151),
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { onCategoryClick("Settings") }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }
                Text(
                    text = selectedMenu,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )
            }
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close sidebar",
                tint = Color(0xFF374151),
                modifier = Modifier
                    .size(20.dp)
                    .clickable { onClose() }
            )
        }

        // ── Categories ────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            activeCategories.forEach { category ->
                val isExpanded        = expandedCategory == category
                val isSettingsCategory = category == "Settings"
                val hasSubItems       = activeSubItems[category]?.isNotEmpty() == true

                val shouldShowCategory = !isSettingsCategory || burgerMenuExpanded

                if (shouldShowCategory) {
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
                                .border(
                                    width = if (isExpanded) 1.5.dp else 0.dp,
                                    color = if (isExpanded) Color(0xFF4338CA) else Color.Transparent,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    if (isHomeMenu) {
                                        onSubItemClick(category, category)
                                    } else {
                                        val subItemsForCategory = activeSubItems[category].orEmpty()
                                        val isSingleSameNamed = subItemsForCategory.size == 1 &&
                                                subItemsForCategory[0] == category

                                        if (hasSubItems && !isSingleSameNamed) {
                                            onCategoryClick(category)
                                        } else {
                                            onSubItemClick(category, subItemsForCategory.firstOrNull() ?: category)
                                        }
                                    }
                                }
                                .padding(horizontal = 14.dp, vertical = 13.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = category,
                                fontSize = 14.sp,
                                fontWeight = if (isExpanded) FontWeight.SemiBold else FontWeight.Medium,
                                color = if (isExpanded) Color(0xFF4338CA) else Color(0xFF374151)
                            )

                            if (hasSubItems && !isHomeMenu) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isSettingsCategory && !isExpanded) {
                                        Text(
                                            text = "Click to expand",
                                            fontSize = 10.sp,
                                            color = Color(0xFF9CA3AF),
                                            modifier = Modifier.padding(end = 8.dp)
                                        )
                                    }
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp
                                        else Icons.Filled.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = if (isExpanded) Color(0xFF4338CA) else Color(0xFF9CA3AF)
                                    )
                                }
                            }
                        }

                        // Sub-items (non-Home, expanded)
                        if (isExpanded && !isHomeMenu) {
                            val subItems = activeSubItems[category].orEmpty()
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 12.dp, end = 14.dp, bottom = 12.dp)
                            ) {
                                subItems.forEachIndexed { index, subItem ->
                                    val isLast      = index == subItems.lastIndex
                                    val isSubSelected = selectedSubItem == "$category::$subItem"

                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        Canvas(modifier = Modifier.matchParentSize()) {
                                            val strokeColor   = Color(0xFFD1D5DB)
                                            val lineX         = 8.dp.toPx()
                                            val curveBottomY  = 24.dp.toPx()
                                            val cornerRadius  = 12.dp.toPx()
                                            val horizontalEndX = lineX + 16.dp.toPx()

                                            val path = Path().apply {
                                                moveTo(lineX, 0f)
                                                lineTo(lineX, curveBottomY - cornerRadius)
                                                arcTo(
                                                    rect = Rect(
                                                        left   = lineX,
                                                        top    = curveBottomY - 2 * cornerRadius,
                                                        right  = lineX + 2 * cornerRadius,
                                                        bottom = curveBottomY
                                                    ),
                                                    startAngleDegrees = 180f,
                                                    sweepAngleDegrees = -90f,
                                                    forceMoveTo = false
                                                )
                                                lineTo(horizontalEndX, curveBottomY)
                                            }
                                            drawPath(path, strokeColor, style = Stroke(width = 1.dp.toPx()))
                                            if (!isLast) {
                                                drawLine(
                                                    color       = strokeColor,
                                                    start       = Offset(lineX, curveBottomY),
                                                    end         = Offset(lineX, size.height),
                                                    strokeWidth = 0.5.dp.toPx()
                                                )
                                            }
                                        }

                                        Text(
                                            text = subItem,
                                            fontSize = 13.sp,
                                            fontWeight = if (isSubSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSubSelected) Color(0xFF3B3BF9) else Color(0xFF424662),
                                            modifier = Modifier
                                                .align(Alignment.CenterStart)
                                                .padding(start = 26.dp, top = 12.dp, bottom = 6.dp)
                                                .background(
                                                    color = if (isSubSelected) Color(0xFFEDEBFF) else Color.Transparent,
                                                    shape = RoundedCornerShape(6.dp)
                                                )
                                                .border(
                                                    width = if (isSubSelected) 1.dp else 0.dp,
                                                    color = if (isSubSelected) Color(0xFF3B3BF9) else Color.Transparent,
                                                    shape = RoundedCornerShape(6.dp)
                                                )
                                                .clickable { onSubItemClick(category, subItem) }
                                                .padding(horizontal = if (isSubSelected) 8.dp else 0.dp, vertical = 2.dp)
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


// ── Module descriptions (shown under each module title) ──
private val moduleDescriptions = mapOf(
    "Sales" to "Manage leads, customers, orders & quotes",
    "Marketing" to "Campaigns, promotions & customer outreach",
    "Finance" to "Invoices, payments, expenses & accounting",
    "Inventory" to "Manage stock, products & warehouses",
    "Logistics" to "Shipments, delivery & transportation",
    "Services" to "Service requests, jobs & maintenance",
    "HR" to "Employees, attendance & payroll",
    "IT" to "IT assets, support & system management",
    "Legal" to "Contracts, documents & compliance",
    "Security" to "Access control & activity monitoring",
    "Reports" to "Sales & finance reporting"
)

private val moduleAccentColors = mapOf(
    "Sales"      to Color(0xFF6C4FF6),
    "Inventory"  to Color(0xFF10B981),
    "Finance"    to Color(0xFFF59E0B),
    "Marketing"  to Color(0xFFEC4899),
    "Logistics"  to Color(0xFF0EA5E9),
    "Services"   to Color(0xFF8B5CF6),
    "HR"         to Color(0xFFEF4444),
    "IT"         to Color(0xFF6366F1),
    "Legal"      to Color(0xFF64748B),
    "Security"   to Color(0xFF14B8A6),
    "Reports"    to Color(0xFFF97316)
)
private val fallbackAccentColor = Color(0xFF6B7280)

private data class FrequentModule(
    val label: String,
    val icon: Int,
    val bg: Color,
    val tint: Color = Color.White
)

private fun buildFrequentlyUsed(context: Context, menuItems: List<MenuItem>): List<FrequentModule> {
    val candidateLabels = menuItems.map { it.label }
    val recentlyUsed = ModuleUsageTracker.getRecentlyUsed(context, candidateLabels, limit = 3)

    val labelsToShow = recentlyUsed.ifEmpty {
        candidateLabels.take(3)
    }

    return labelsToShow.mapNotNull { label ->
        val menuItem = menuItems.find { it.label == label } ?: return@mapNotNull null
        FrequentModule(
            label = menuItem.label,
            icon = menuItem.icon,
            bg = moduleAccentColors[menuItem.label] ?: fallbackAccentColor
        )
    }
}
private const val HALF_FRACTION = 0.55f
private const val FULL_FRACTION = 0.96f

// ─────────────────────────────────────────────────────────────
// 🏠 FULL MODULES PANEL
// ─────────────────────────────────────────────────────────────

@Composable
fun ModulesPanel(
    isOpen: Boolean,
    onClose: () -> Unit,
    initialExpandedModule: String? = null,
    initialExpandedCategory: String? = null,
    initialActiveSubItem: String? = null,   // ✅ NEW — current screen's actual sub-item
    onModuleCategoryClick: (menu: String, category: String) -> Unit
) {
    ModulesPanelContent(
        isOpen = isOpen,
        onClose = onClose,
        onModuleCategoryClick = onModuleCategoryClick,
        menuItems = SidebarConfig.getFullMenuItems().filter { it.label != "Home" },
        showFrequentlyUsed = true,
        initialExpandedModule = initialExpandedModule,
        initialExpandedCategory = initialExpandedCategory,
        initialActiveSubItem = initialActiveSubItem   // ✅ NEW
    )
}

// ─────────────────────────────────────────────────────────────
// 🧩 Reusable Modules Panel Content
// ─────────────────────────────────────────────────────────────
@Suppress("SameParameterValue")
@Composable
private fun ModulesPanelContent(
    isOpen: Boolean,
    onClose: () -> Unit,
    onModuleCategoryClick: (menu: String, category: String) -> Unit,
    menuItems: List<MenuItem>,
    showFrequentlyUsed: Boolean,
    initialExpandedModule: String? = null,
    initialExpandedCategory: String? = null,
    initialActiveSubItem: String? = null   // ✅ NEW
) {
    val density = LocalDensity.current
    val context = LocalContext.current
    val heightFraction = remember { Animatable(HALF_FRACTION) }
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var expandedModule by remember { mutableStateOf(menuItems.firstOrNull()?.label) }
    var activeCategory by remember { mutableStateOf<String?>(null) }
    var activeSubItem by remember { mutableStateOf<String?>(null) }

    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val filteredModules = if (searchQuery.isBlank()) {
        menuItems
    } else {
        menuItems.filter { it.label.contains(searchQuery, ignoreCase = true) }
    }
    val candidateLabels = remember(menuItems) { menuItems.map { it.label } }

    val hasUsageHistory by remember(menuItems) {
        derivedStateOf {
            ModuleUsageTracker.getRecentlyUsed(context, candidateLabels, limit = 3).isNotEmpty()
        }
    }
    val frequentlyUsed by remember(menuItems) {
        derivedStateOf { buildFrequentlyUsed(context, menuItems).take(3) }
    }

    // state — add this near your other `var` declarations, alongside expandedModule
    var expandedSubCategory by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(isOpen, initialExpandedModule, initialExpandedCategory, initialActiveSubItem) {
        if (isOpen) {
            heightFraction.snapTo(HALF_FRACTION)
            searchQuery = ""
            val target = initialExpandedModule ?: menuItems.firstOrNull()?.label
            expandedModule = target
            activeCategory = initialExpandedCategory   // ✅ always synced from current screen every time panel opens
            activeSubItem = initialActiveSubItem

            if (target != null) {
                val idx = filteredModules.indexOfFirst { it.label == target }
                if (idx >= 0) {
                    val headerOffset =
                        (if (showFrequentlyUsed) 1 else 0) + 1
                    scope.launch {
                        listState.animateScrollToItem(
                            (headerOffset + idx).coerceAtLeast(0)
                        )
                    }
                }
            }
        }
    }

    val cornerRadius: Dp = lerp(
        24.dp, 0.dp,
        ((heightFraction.value - HALF_FRACTION) / (FULL_FRACTION - HALF_FRACTION)).coerceIn(0f, 1f)
    )

    // ── Scrim — smooth fade ──
    AnimatedVisibility(
        visible = isOpen,
        enter = fadeIn(animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)),
        exit = fadeOut(animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)),
        modifier = Modifier.zIndex(10f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.35f))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onClose() }
        )
    }

    // ── Panel — smooth slide up/down ──
    AnimatedVisibility(
        visible = isOpen,
        enter = slideInVertically(
            initialOffsetY = { fullHeight -> fullHeight },
            animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
        ),
        exit = slideOutVertically(
            targetOffsetY = { fullHeight -> fullHeight },
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
        ),
        modifier = Modifier
            .fillMaxSize()
            .zIndex(11f)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val maxHeightPx = with(density) { maxHeight.toPx() }
            val panelHeight = maxHeight * heightFraction.value

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()

                    .height(panelHeight),
                color = Color.White,
                shape = RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius),
                shadowElevation = 16.dp
            ) {
                Column(modifier = Modifier.fillMaxSize()
                    .background(Color(0XFFFAFAFB))) {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = {
                                        scope.launch {
                                            val target = if (heightFraction.value >= FULL_FRACTION - 0.05f)
                                                HALF_FRACTION else FULL_FRACTION
                                            heightFraction.animateTo(target, tween(250))
                                        }
                                    }
                                )
                            }
                            .pointerInput(Unit) {
                                detectVerticalDragGestures(
                                    onDragEnd = {
                                        scope.launch {
                                            val target = if (heightFraction.value > (HALF_FRACTION + FULL_FRACTION) / 2)
                                                FULL_FRACTION else HALF_FRACTION
                                            heightFraction.animateTo(target, tween(250))
                                        }
                                    },
                                    onVerticalDrag = { change, dragAmount ->
                                        change.consume()
                                        scope.launch {
                                            val deltaFraction = -dragAmount / maxHeightPx
                                            val newValue = (heightFraction.value + deltaFraction)
                                                .coerceIn(HALF_FRACTION - 0.05f, FULL_FRACTION)
                                            heightFraction.snapTo(newValue)
                                        }
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color(0xFFD1D5DB))
                        )
                    }

                    // ── Header ──
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Modules", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                        Icon(
                            imageVector = Icons.Default.Close,
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

                    Spacer(Modifier.height(12.dp))

                    // ── Search ──
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .height(46.dp)
                            .background(Color(0xFFF3F4F6), RoundedCornerShape(10.dp))
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            textStyle = TextStyle(fontSize = 14.sp, color = Color(0xFF374151)),
                            cursorBrush = SolidColor(Color(0xFF3B3BF9)),
                            decorationBox = { inner ->
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (searchQuery.isEmpty()) {
                                        Text("Search modules...", fontSize = 14.sp, color = Color(0xFF9CA3AF))
                                    }
                                    inner()
                                }
                            }
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp)
                    ) {
                        if (searchQuery.isBlank() && showFrequentlyUsed) {
                            item {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        if (hasUsageHistory) "FREQUENTLY USED" else "EXPLORE",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF9CA3AF)
                                    )
                                    if (!hasUsageHistory) {
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            "Modules you haven't tried yet",
                                            fontSize = 11.sp,
                                            color = Color(0xFFC1C5CC)
                                        )
                                    }
                                    Spacer(Modifier.height(10.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        frequentlyUsed.forEach { fm ->
                                            Column(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .background(Color.White, RoundedCornerShape(14.dp))
                                                    .border1(Color(0xFFF0F0F0))
                                                    .clickable(
                                                        indication = null,
                                                        interactionSource = remember { MutableInteractionSource() }
                                                    ) {
                                                        ModuleUsageTracker.recordUsage(context, fm.label)
                                                        val menu = menuItems.find { it.label == fm.label }
                                                        val firstCat = menu?.categories?.firstOrNull()
                                                        if (menu != null && firstCat != null) {
                                                            onModuleCategoryClick(menu.label, firstCat)
                                                        }
                                                    }
                                                    .padding(vertical = 16.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(44.dp)
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .background(fm.bg),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        painter = painterResource(id = fm.icon),
                                                        contentDescription = fm.label,
                                                        tint = fm.tint,
                                                        modifier = Modifier.size(22.dp)
                                                    )
                                                }
                                                Spacer(Modifier.height(8.dp))
                                                Text(fm.label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF111827))
                                            }
                                        }
                                    }
                                    Spacer(Modifier.height(24.dp))
                                }
                            }
                        }

                        item {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    "ALL MODULES",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF9CA3AF)
                                )
                                Spacer(Modifier.height(10.dp))
                            }
                        }

                        items(filteredModules) { module ->
                            val isExpanded = expandedModule == module.label

                            val arrowRotation by animateFloatAsState(
                                targetValue = if (isExpanded) 180f else 0f,
                                animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
                                label = "arrowRotation_${module.label}"
                            )

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                                    .shadow(
                                        elevation = if (isExpanded) 1.dp else 1.dp,
                                        shape = RoundedCornerShape(24.dp),
                                        clip = false
                                    )
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(Color.White)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(
                                            indication = null,
                                            interactionSource = remember { MutableInteractionSource() }
                                        ) {
                                            expandedModule = if (isExpanded) null else module.label
                                        }
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFFF3F4F6)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            painter = painterResource(id = module.icon),
                                            contentDescription = module.label,
                                            tint = Color(0xFF111827),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(module.label, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF111827))
                                    }
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = Color(0xFF9CA3AF),
                                        modifier = Modifier
                                            .size(20.dp)
                                            .rotate(arrowRotation)
                                    )
                                }

                                AnimatedVisibility(
                                    visible = isExpanded && module.categories.isNotEmpty(),
                                    enter = expandVertically(
                                        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                                    ) + fadeIn(
                                        animationSpec = tween(durationMillis = 250, delayMillis = 50)
                                    ),
                                    exit = shrinkVertically(
                                        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)
                                    ) + fadeOut(
                                        animationSpec = tween(durationMillis = 150)
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 48.dp, end = 16.dp, bottom = 16.dp)
                                    ) {
                                        module.categories.forEach { category ->
                                            val categorySubItems = module.subItems[category].orEmpty()
                                            val hasSubItems = categorySubItems.isNotEmpty()
                                            val categoryKey = "${module.label}::$category"
                                            val isDropdownOpen = expandedSubCategory == categoryKey
                                            val isCategoryActive = category == activeCategory

                                            val subArrowRotation by animateFloatAsState(
                                                targetValue = if (isDropdownOpen) 180f else 0f,
                                                animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
                                                label = "subArrowRotation_$categoryKey"
                                            )

                                            // ── Category header row ──
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 3.dp)
                                                    .background(
                                                        color = Color.Transparent,
                                                        shape = RoundedCornerShape(8.dp)
                                                    )
                                                    .clickable(
                                                        indication = null,
                                                        interactionSource = remember { MutableInteractionSource() }
                                                    ) {
                                                        if (hasSubItems) {
                                                            // ✅ CHANGED — has sub-items: text click ONLY toggles dropdown, no navigation
                                                            expandedSubCategory = if (isDropdownOpen) null else categoryKey
                                                        } else {
                                                            // ✅ unchanged — no sub-items: text click navigates directly
                                                            ModuleUsageTracker.recordUsage(context, module.label)
                                                            activeCategory = category
                                                            activeSubItem = null
                                                            onModuleCategoryClick(module.label, category)
                                                        }
                                                    }
                                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = category,
                                                    fontSize = 16.sp,
                                                    fontWeight = if (isDropdownOpen || isCategoryActive) FontWeight.Bold else FontWeight.SemiBold,
                                                    color = Color(0xFF4B5563)
                                                )

                                                if (hasSubItems) {
                                                    Icon(
                                                        imageVector = Icons.Default.KeyboardArrowUp,
                                                        contentDescription = if (isDropdownOpen) "Collapse" else "Expand",
                                                        tint = if (isDropdownOpen || isCategoryActive) Color(0xFF4338CA) else Color(0xFF9CA3AF),
                                                        modifier = Modifier
                                                            .size(18.dp)
                                                            .rotate(180f - subArrowRotation)
                                                        // ❌ removed separate .clickable on the icon — whole row (including icon) now
                                                        // triggers the same toggle behavior since the parent Row handles the click.
                                                        // Keeping a duplicate click handler on the icon was redundant once the row logic branches correctly.
                                                    )
                                                }
                                            }

                                            // ── Sub items ──
                                            AnimatedVisibility(
                                                visible = isDropdownOpen && hasSubItems,
                                                enter = expandVertically(animationSpec = tween(250, easing = FastOutSlowInEasing)) +
                                                        fadeIn(animationSpec = tween(200, delayMillis = 50)),
                                                exit = shrinkVertically(animationSpec = tween(200, easing = FastOutSlowInEasing)) +
                                                        fadeOut(animationSpec = tween(150))
                                            ) {
                                                Column(modifier = Modifier.padding(start = 14.dp, top = 2.dp, bottom = 4.dp)) {
                                                    categorySubItems.forEach { subItem ->
                                                        val isSubActive = activeCategory == category && activeSubItem == subItem

                                                        Text(
                                                            "-  $subItem",
                                                            fontSize = 16.sp,
                                                            fontWeight = if (isSubActive) FontWeight.SemiBold else FontWeight.Normal,
                                                            color = if (isSubActive) Color(0xFF4338CA) else Color(0xFF6B7280),
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .background(
                                                                    color = if (isSubActive) Color(0xFFEDEBFF) else Color.Transparent,
                                                                    shape = RoundedCornerShape(6.dp)
                                                                )
                                                                .clickable(
                                                                    indication = null,
                                                                    interactionSource = remember { MutableInteractionSource() }
                                                                ) {
                                                                    ModuleUsageTracker.recordUsage(context, module.label)
                                                                    activeCategory = category
                                                                    activeSubItem = subItem
                                                                    onModuleCategoryClick(module.label, subItem)
                                                                }
                                                                .padding(horizontal = 8.dp, vertical = 8.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        item { Spacer(Modifier.height(100.dp)) }
                    }
                }
            }
        }
    }
}

// small helper for a subtle 1dp rounded border
private fun Modifier.border1(color: Color): Modifier =
    this.border(1.dp, color, RoundedCornerShape(14.dp))