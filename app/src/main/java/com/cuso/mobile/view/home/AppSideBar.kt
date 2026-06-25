// com/cuso/mobile/view/home/AppSidebar.kt

package com.cuso.mobile.view.home

import com.cuso.mobile.model.User
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.cuso.mobile.R

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
// Sidebar Configuration
// ─────────────────────────────────────────────────────────────

object SidebarConfig {

    // 🏠 FULL MENU ITEMS - Home has NO categories (like the image)
    fun getFullMenuItems(): List<MenuItem> {
        return listOf(
            MenuItem(
                R.drawable.home,
                "Home",
                isPanel = false,  // ✅ No panel mode for Home - just a simple menu item
                categories = emptyList(),
                subItems = emptyMap()
            ),
            MenuItem(
                R.drawable.sales,
                "Sales",
                isPanel = true,
                categories = listOf(
                    "Lead Management",
                    "Customer",
                    "Measurements",
                    "Sales & Orders",
                    "Order Management",
                    "Pricing & Quotations",
                    "Targets vs Achievements",
                    "Salesperson Analytics"
                ),
                subItems = mapOf(
                    "Lead Management" to listOf("Lead"),
                    "Customer" to listOf("Customers"),
                    "Measurements" to listOf("Measurements"),
                    "Sales & Orders" to listOf("Sales & Orders"),
                    "Order Management" to listOf("Orders"),
                    "Pricing & Quotations" to listOf("Overview", "Pricing & Quotations"),
                    "Targets vs Achievements" to listOf("Targets vs Achievements"),
                    "Salesperson Analytics" to listOf("Salesperson Analytics")
                )
            ),
            MenuItem(
                R.drawable.marketing,
                "Marketing",
                isPanel = true,
                categories = listOf("Website", "Campaigns", "Leads & Audience", "Engagement", "Growth", "Pages", "Budget", "Team"),
                subItems = mapOf(
                    "Campaigns" to listOf("Campaigns", "Promotions", "Marketing & Calendar"),
                    "Leads & Audience" to listOf("Lead Generation", "Customer Segmentation"),
                    "Engagement" to listOf("Customer Engagement", "WhatsApp", "Social Media", "Review & Feedback"),
                    "Growth" to listOf("Referral Program", "Influencer"),
                    "Pages" to listOf("Landing Page"),
                    "Budget" to listOf("Marketing Budget"),
                    "Team" to listOf("Marketing Tasks", "Team Management")
                )
            ),
            MenuItem(
                R.drawable.finance,
                "Finance",
                isPanel = true,
                categories = listOf("Accounts Receivable", "Accounts Payable", "Expenses", "Finance Core"),
                subItems = mapOf(
                    "Accounts Receivable" to listOf("Customers", "Sales Invoices", "Payments Received"),
                    "Accounts Payable" to listOf("Suppliers", "Purchase Invoices", "Payments Mode"),
                    "Expenses" to listOf("Expenses"),
                    "Finance Core" to listOf("Chart of Accounts", "Journal Entries", "Trial Balance")
                )
            ),
            MenuItem(
                R.drawable.inventory,
                "Inventory",
                isPanel = true,
                categories = listOf("Items", "Procurement", "Payables"),
                subItems = mapOf(
                    "Items" to listOf("All Items", "Item Groups"),
                    "Procurement" to listOf("Suppliers", "Requisitions", "Orders", "Goods Receipt"),
                    "Payables" to listOf("Invoices", "Payments", "Credits")
                )
            ),
            MenuItem(
                R.drawable.logistics,
                "Logistics",
                isPanel = true,
                categories = listOf("Delivery", "Returns"),
                subItems = mapOf(
                    "Delivery" to listOf("Delivery"),
                    "Returns" to listOf("Returns")
                )
            ),
            MenuItem(
                R.drawable.services,
                "Services",
                isPanel = true,
                categories = listOf("Service Request", "Alteration Management", "Return", "Damaged Goods", "Customer Feedback"),
                subItems = mapOf(
                    "Service Request" to listOf("Service Request"),
                    "Alteration Management" to listOf("Alteration Management"),
                    "Return" to listOf("Return"),
                    "Damaged Goods" to listOf("Damaged Goods"),
                    "Customer Feedback" to listOf("Customer Feedback")
                )
            ),
            MenuItem(
                R.drawable.hr,
                "HR",
                isPanel = true,
                categories = listOf("Employees"),
                subItems = mapOf("Employees" to listOf("All Employees"))
            ),
            MenuItem(
                R.drawable.it,
                "IT",
                isPanel = true,
                categories = listOf("Integrations"),
                subItems = mapOf("Integrations" to listOf("API Integration"))
            ),
            MenuItem(
                R.drawable.legal,
                "Legal",
                isPanel = true,
                categories = listOf("Legal Management"),
                subItems = mapOf("Legal Management" to listOf("Legal Documents"))
            ),
            MenuItem(
                R.drawable.security,
                "Security",
                isPanel = true,
                categories = listOf("Access Control", "Auth & Verification", "Monitoring & Audit"),
                subItems = mapOf(
                    "Access Control" to listOf("User Accounts", "Roles & Permissions"),
                    "Auth & Verification" to listOf("Multi Factor (MFA)", "SSO Settings"),
                    "Monitoring & Audit" to listOf("Login Logs", "Activity Logs")
                )
            ),
            MenuItem(
                R.drawable.reports,
                "Reports",
                isPanel = true,
                categories = listOf("Sales Reports", "Finance Reports"),
                subItems = mapOf(
                    "Sales Reports" to listOf("Sales Reports"),
                    "Finance Reports" to listOf("Finance Reports")
                )
            )
        )
    }

    // 📊 SALES ONLY MENU ITEMS - Only Home & Sales enabled
    fun getSalesMenuItems(): List<MenuItem> {
        return listOf(
            MenuItem(
                R.drawable.home,
                "Home",
                enabled = true,
                isPanel = true,
                categories = listOf(
                    "Organization Profile",
                    "Branch Management",
                    "Department & Teams",
                    "Designation"
                ),
                subItems = mapOf(
                    "Organization Profile" to listOf("Organization Profile"),
                    "Branch Management" to listOf("Branch Management"),
                    "Department & Teams" to listOf("Department & Teams"),
                    "Designation" to listOf("Designation")
                )
            ),
            MenuItem(
                R.drawable.sales,
                "Sales",
                enabled = true,
                isPanel = true,
                categories = listOf("Garment Type"),
                subItems = mapOf(
                    "Garment Type" to listOf("Garment Type")
                )
            ),
            MenuItem(
                R.drawable.marketing,
                "Marketing",
                enabled = false,
                isPanel = true,
                categories = emptyList(),
                subItems = emptyMap()
            ),
            MenuItem(
                R.drawable.finance,
                "Finance",
                enabled = false,
                isPanel = true,
                categories = emptyList(),
                subItems = emptyMap()
            ),
            MenuItem(
                R.drawable.inventory,
                "Inventory",
                enabled = false,
                isPanel = true,
                categories = emptyList(),
                subItems = emptyMap()
            ),
            MenuItem(
                R.drawable.logistics,
                "Logistics",
                enabled = false,
                isPanel = true,
                categories = emptyList(),
                subItems = emptyMap()
            ),
            MenuItem(
                R.drawable.services,
                "Services",
                enabled = false,
                isPanel = true,
                categories = emptyList(),
                subItems = emptyMap()
            ),
            MenuItem(
                R.drawable.hr,
                "HR",
                enabled = false,
                isPanel = true,
                categories = emptyList(),
                subItems = emptyMap()
            ),
            MenuItem(
                R.drawable.it,
                "IT",
                enabled = false,
                isPanel = true,
                categories = emptyList(),
                subItems = emptyMap()
            ),
            MenuItem(
                R.drawable.legal,
                "Legal",
                enabled = false,
                isPanel = true,
                categories = emptyList(),
                subItems = emptyMap()
            ),
            MenuItem(
                R.drawable.security,
                "Security",
                enabled = false,
                isPanel = true,
                categories = emptyList(),
                subItems = emptyMap()
            ),
            MenuItem(
                R.drawable.reports,
                "Reports",
                enabled = false,
                isPanel = true,
                categories = emptyList(),
                subItems = emptyMap()
            )
        )
    }
}

// ─────────────────────────────────────────────────────────────
// 🏠 FULL NAV BAR - All menus enabled
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
    val menuItems = SidebarConfig.getFullMenuItems()

    AppSidebarContent(
        isOpen = isOpen,
        onClose = onClose,
        onMenuItemClick = onMenuItemClick,
        onLogout = onLogout,
        user = user,
        menuItems = menuItems,
        defaultSelectedMenu = defaultSelectedMenu,
        isSalesMode = false
    )
}

// ─────────────────────────────────────────────────────────────
// 📊 SALES NAV BAR - Only Home & Sales enabled
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
    val menuItems = SidebarConfig.getSalesMenuItems()

    AppSidebarContent(
        isOpen = isOpen,
        onClose = onClose,
        onMenuItemClick = onMenuItemClick,
        onLogout = onLogout,
        user = user,
        menuItems = menuItems,
        defaultSelectedMenu = defaultSelectedMenu,
        isSalesMode = true
    )
}

// ─────────────────────────────────────────────────────────────
// 🧩 Reusable Sidebar Content
// ─────────────────────────────────────────────────────────────

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

    // ✅ Determine if panel mode is active
    // For FullNavBar: Only show panel for menus with isPanel = true (Sales, Marketing, etc.)
    // For SalesNavBar: Always show panel mode
    val isPanelMode = if (isSalesMode) {
        true  // Sales mode always shows panel
    } else {
        // Full mode: Only show panel if selected menu has categories
        val selectedMenuItem = menuItems.find { it.label == selectedMenu }
        selectedMenuItem?.isPanel == true && selectedMenuItem.categories.isNotEmpty()
    }

    val activeMenuItem = menuItems.find { it.label == selectedMenu }
    val activeCategories = activeMenuItem?.categories ?: emptyList()
    val activeSubItems = activeMenuItem?.subItems ?: emptyMap()

    fun handleMenuClick(label: String) {
        val menuItem = menuItems.find { it.label == label }
        if (menuItem?.enabled == false) return

        selectedMenu = label
        expandedCategory = null
        burgerMenuExpanded = false

        // ✅ Check if this menu should show a panel
        val shouldShowPanel = if (isSalesMode) {
            true  // Sales mode always shows panel
        } else {
            // Full mode: Only show panel if menu has categories
            menuItem?.isPanel == true && menuItem.categories.isNotEmpty()
        }

        if (shouldShowPanel) {
            val firstCategory = menuItem?.categories?.firstOrNull()
            val firstSubItem = firstCategory?.let { menuItem.subItems[it]?.firstOrNull() }
            if (firstCategory != null && firstSubItem != null) {
                expandedCategory = firstCategory
                selectedSubItem = "$firstCategory::$firstSubItem"

                val navigationKey = if (label == "Home") {
                    when (firstSubItem) {
                        "Organization Profile" -> "home_organization_profile"
                        "Branch Management" -> "home_branch_management"
                        "Department & Teams" -> "home_department_teams"
                        "Designation" -> "home_designation"
                        else -> "home_${firstSubItem.lowercase().replace(" ", "_")}"
                    }
                } else {
                    "${label.lowercase()}_${firstSubItem.lowercase().replace(" ", "_")}"
                }
                onMenuItemClick(navigationKey)
            }
        } else {
            // Simple navigation (like Home in FullNavBar)
            onMenuItemClick(label.lowercase())
        }
    }

    fun handleCategoryClick(category: String) {
        expandedCategory = if (expandedCategory == category) null else category
    }

    fun handleSubItemClick(category: String, subItem: String) {
        selectedSubItem = "$category::$subItem"

        val navigationKey = when {
            selectedMenu == "Home" -> {
                when (subItem) {
                    "Organization Profile" -> "home_organization_profile"
                    "Branch Management" -> "home_branch_management"
                    "Department & Teams" -> "home_department_teams"
                    "Designation" -> "home_designation"
                    else -> "home_${subItem.lowercase().replace(" ", "_")}"
                }
            }
            else -> {
                "${selectedMenu.lowercase()}_${subItem.lowercase().replace(" ", "_")}"
            }
        }

        onMenuItemClick(navigationKey)
        onClose()
    }

    if (isOpen) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
                .clickable { onClose() }
                .zIndex(1f)
        )
    }

    AnimatedVisibility(
        visible = isOpen,
        enter = slideInHorizontally(initialOffsetX = { -it }),
        exit = slideOutHorizontally(targetOffsetX = { -it }),
        modifier = Modifier.zIndex(2f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .width(IntrinsicSize.Max)
        ) {
            // ─── Icon Rail ───
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

            // ─── Accordion Panel (only shown when panel mode is active) ───
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
                    isSalesMode = isSalesMode,
                    onCategoryClick = { handleCategoryClick(it) },
                    onSubItemClick = { category, subItem -> handleSubItemClick(category, subItem) },
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
        // ─── Logo ───
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

        // ─── Menu Items ───
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            menuItems.forEach { item ->
                val isSelected = selectedMenu == item.label

                if (isPanelMode) {
                    // Panel Mode (Icons only - like the image)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                            .clickable(enabled = item.enabled) {
                                onMenuItemClick(item.label)
                            }
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) Color(0xFFE3E0FB)
                                    else if (!item.enabled) Color(0xFFF5F5F5)
                                    else Color.Transparent
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = item.icon),
                                contentDescription = item.label,
                                tint = when {
                                    !item.enabled -> Color(0xFFD1D5DB)
                                    isSelected -> Color(0xFF4338CA)
                                    else -> Color(0xFF6B7280)
                                },
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Text(
                            text = item.label,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (!item.enabled) Color(0xFFD1D5DB)
                            else if (isSelected) Color(0xFF4338CA)
                            else Color(0xFF6B7280),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    // Expanded Mode (Text + Icon - like the image when Home is clicked)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 2.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) Color(0xFF3B3BF9) else Color.Transparent)
                            .clickable(enabled = item.enabled) {
                                onMenuItemClick(item.label)
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = item.icon),
                            contentDescription = item.label,
                            tint = when {
                                !item.enabled -> Color(0xFFD1D5DB)
                                isSelected -> Color.White
                                else -> Color(0xFF6B7280)
                            },
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = item.label,
                            fontSize = 15.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = when {
                                !item.enabled -> Color(0xFFD1D5DB)
                                isSelected -> Color.White
                                else -> Color(0xFF111827)
                            }
                        )
                    }
                }
            }
        }

        HorizontalDivider(color = Color(0xFFF0F0F0))

        // ─── User Profile ───
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
    isSalesMode: Boolean,
    onCategoryClick: (String) -> Unit,
    onSubItemClick: (String, String) -> Unit,
    onClose: () -> Unit
) {
    Column(modifier = modifier) {
        // ─── Header ───
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
                            .clickable {
                                onCategoryClick("Settings")
                            }
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

        // ─── Categories ───
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            activeCategories.forEach { category ->
                val isExpanded = expandedCategory == category
                val isSettingsCategory = category == "Settings"
                val hasSubItems = activeSubItems[category]?.isNotEmpty() == true

                val shouldShowCategory = if (isSettingsCategory) {
                    burgerMenuExpanded
                } else {
                    true
                }

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
                                .clickable {
                                    if (isHomeMenu) {
                                        // 🏠 Direct navigation for Home menu items
                                        val navigationKey = when (category) {
                                            "Organization Profile" -> "home_organization_profile"
                                            "Branch Management" -> "home_branch_management"
                                            "Department & Teams" -> "home_department_teams"
                                            "Designation" -> "home_designation"
                                            else -> "home_${category.lowercase().replace(" ", "_")}"
                                        }
                                        onSubItemClick(category, category)
                                        onClose()
                                    } else {
                                        // 📂 For other menus, toggle expansion if has sub-items
                                        if (hasSubItems) {
                                            onCategoryClick(category)
                                        } else {
                                            val navKey = "${selectedMenu.lowercase()}_${category.lowercase().replace(" ", "_")}"
                                            onSubItemClick(category, category)
                                            onClose()
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

                            // 🔽 Show arrow only for non-Home menus with sub-items
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

                        // 📋 Show sub-items only for non-Home menus when expanded
                        if (isExpanded && !isHomeMenu) {
                            val subItems = activeSubItems[category].orEmpty()
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 12.dp, end = 14.dp, bottom = 12.dp)
                            ) {
                                subItems.forEachIndexed { index, subItem ->
                                    val isLast = index == subItems.lastIndex
                                    val isSubSelected = selectedSubItem == "$category::$subItem"

                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        Canvas(modifier = Modifier.matchParentSize()) {
                                            val strokeColor = Color(0xFFD1D5DB)
                                            val lineX = 8.dp.toPx()
                                            val curveBottomY = 24.dp.toPx()
                                            val cornerRadius = 12.dp.toPx()
                                            val horizontalEndX = lineX + 16.dp.toPx()

                                            val path = Path().apply {
                                                moveTo(lineX, 0f)
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
                                                style = Stroke(width = 1.dp.toPx())
                                            )
                                            if (!isLast) {
                                                drawLine(
                                                    color = strokeColor,
                                                    start = Offset(lineX, curveBottomY),
                                                    end = Offset(lineX, size.height),
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
                                                .clickable { onSubItemClick(category, subItem) }
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