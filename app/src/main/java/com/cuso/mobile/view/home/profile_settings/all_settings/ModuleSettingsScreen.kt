@file:Suppress("unused", "SpellCheckingInspection", "UNUSED_PARAMETER")

package com.cuso.mobile.view.home.profile_settings.all_settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.mobile.R
import com.cuso.mobile.ui.theme.*
import com.cuso.mobile.view.composable.SearchFilterBar
import com.cuso.mobile.view.composable.TitleBar

private val PrimaryBlue = Color(0xFF3B3BF9)

// Sub-Item Model for Module Settings
data class ModuleSubItem(
    val title: String,
    val onClick: () -> Unit
)

// Module Setting Item Model
data class ModuleSettingItem(
    val id: String,
    val title: String,
    val description: String,
    val icon: Int,
    val tags: List<String> = emptyList(),
    val isConfigured: Boolean = true,
    val subItems: List<ModuleSubItem> = emptyList(),
    val onConfigure: () -> Unit = {}
)

@Composable
fun ModuleSettingsScreen(
    onClose: () -> Unit,
    onNavigateToModule: (String) -> Unit = {},
    onConfigureHome: () -> Unit = { onNavigateToModule("home") },
    onConfigureSales: () -> Unit = { onNavigateToModule("sales_garment_type") },
    onConfigureSalesPricing: () -> Unit = { onNavigateToModule("sales_garment_pricing_setup") },
    onConfigureMarketing: () -> Unit = {},
    onConfigureFinance: () -> Unit = { onNavigateToModule("finance_chart_of_accounts") },
    onConfigureInventory: () -> Unit = { onNavigateToModule("inventory_allocation_rules") },
    // Inventory 7 Sub-items Navigation
    onNavigateAllocationRules: () -> Unit = { onNavigateToModule("inventory_allocation_rules") },
    onNavigatePdfTemplates: () -> Unit = { onNavigateToModule("inventory_pdf_templates") },
    onNavigateLocationStructure: () -> Unit = { onNavigateToModule("inventory_location_structure") },
    onNavigateFloorOverview: () -> Unit = { onNavigateToModule("inventory_floor_overview") },
    onNavigateSectionOverview: () -> Unit = { onNavigateToModule("inventory_section_overview") },
    onNavigateRackOverview: () -> Unit = { onNavigateToModule("inventory_rack_overview") },
    onNavigateBinOverview: () -> Unit = { onNavigateToModule("inventory_bin_overview") },
    onConfigureLogistics: () -> Unit = { onNavigateToModule("logistics_delivery") },
    onConfigureServices: () -> Unit = { onNavigateToModule("services_service_status") },
    onConfigureHR: () -> Unit = { onNavigateToModule("hr_all_employees") },
    onConfigureIT: () -> Unit = {},
    onConfigureLegal: () -> Unit = {},
    onConfigureSecurity: () -> Unit = {},
    onConfigureReports: () -> Unit = { onNavigateToModule("reports_sales") }
) {
    var searchQuery by remember { mutableStateOf("") }

    val moduleList = remember(
        onConfigureHome,
        onConfigureSales,
        onConfigureSalesPricing,
        onConfigureMarketing,
        onConfigureFinance,
        onConfigureInventory,
        onNavigateAllocationRules,
        onNavigatePdfTemplates,
        onNavigateLocationStructure,
        onNavigateFloorOverview,
        onNavigateSectionOverview,
        onNavigateRackOverview,
        onNavigateBinOverview,
        onConfigureLogistics,
        onConfigureServices,
        onConfigureHR,
        onConfigureIT,
        onConfigureLegal,
        onConfigureSecurity,
        onConfigureReports
    ) {
        listOf(
            ModuleSettingItem(
                id = "home",
                title = "Home",
                description = "Configure dashboard, widgets, and role-based home experience.",
                icon = R.drawable.home,
                tags = listOf("Dashboard", "Widgets", "Shortcuts"),
                isConfigured = true,
                onConfigure = onConfigureHome
            ),
            ModuleSettingItem(
                id = "sales",
                title = "Sales",
                description = "Manage leads, quotes, orders, and sales pipeline workflows.",
                icon = R.drawable.sales,
                tags = listOf("Orders", "Pricing", "Approvals"),
                isConfigured = true,
                subItems = listOf(
                    ModuleSubItem(title = "Garment", onClick = onConfigureSales),
                    ModuleSubItem(title = "Garment Pricing", onClick = onConfigureSalesPricing)
                ),
                onConfigure = onConfigureSales
            ),
            ModuleSettingItem(
                id = "marketing",
                title = "Marketing",
                description = "Manage campaigns, email automation, and audience engagement.",
                icon = R.drawable.marketing,
                tags = listOf("Campaigns", "Lead Sources", "Communication"),
                isConfigured = true,
                onConfigure = onConfigureMarketing
            ),
            ModuleSettingItem(
                id = "finance",
                title = "Finance",
                description = "Manage billing, payments, accounting and financial workflows.",
                icon = R.drawable.finance,
                tags = listOf("Billing", "Payments", "Accounting"),
                isConfigured = true,
                onConfigure = onConfigureFinance
            ),
            ModuleSettingItem(
                id = "inventory",
                title = "Inventory",
                description = "Manage stock levels, warehouses, and procurement workflows.",
                icon = R.drawable.inventory,
                tags = listOf("Stock", "Warehouse", "Procurement"),
                isConfigured = true,
                subItems = listOf(
                    ModuleSubItem(title = "Allocation Rules", onClick = onNavigateAllocationRules),
                    ModuleSubItem(title = "PDF Templates", onClick = onNavigatePdfTemplates),
                    ModuleSubItem(title = "Location Structure", onClick = onNavigateLocationStructure),
                    ModuleSubItem(title = "Floor Overview", onClick = onNavigateFloorOverview),
                    ModuleSubItem(title = "Section Overview", onClick = onNavigateSectionOverview),
                    ModuleSubItem(title = "Rack Overview", onClick = onNavigateRackOverview),
                    ModuleSubItem(title = "Bin Overview", onClick = onNavigateBinOverview)
                ),
                onConfigure = onNavigateAllocationRules
            ),
            ModuleSettingItem(
                id = "logistics",
                title = "Logistics",
                description = "Manage shipping, fleet tracking, and delivery operations.",
                icon = R.drawable.logistics,
                tags = listOf("Delivery", "Dispatch", "Shipment"),
                isConfigured = true,
                onConfigure = onConfigureLogistics
            ),
            ModuleSettingItem(
                id = "services",
                title = "Services",
                description = "Configure ticketing, SLAs, knowledge base, and field service.",
                icon = R.drawable.services,
                tags = listOf("Service Orders", "Workflow", "Status Rules"),
                isConfigured = true,
                onConfigure = onConfigureServices
            ),
            ModuleSettingItem(
                id = "hr",
                title = "Human Resources",
                description = "Manage employees, attendance, leave, and payroll processing.",
                icon = R.drawable.hr,
                tags = listOf("Employees", "Attendance", "Payroll"),
                isConfigured = true,
                onConfigure = onConfigureHR
            ),
            ModuleSettingItem(
                id = "it",
                title = "IT",
                description = "Manage IT assets, helpdesk, network, and infrastructure.",
                icon = R.drawable.it,
                tags = listOf("Assets", "Access", "System Rules"),
                isConfigured = true,
                onConfigure = onConfigureIT
            ),
            ModuleSettingItem(
                id = "legal",
                title = "Legal",
                description = "Manage contracts, compliance, and legal document workflows.",
                icon = R.drawable.legal,
                tags = listOf("Contracts", "Approvals", "Documents"),
                isConfigured = false,
                onConfigure = onConfigureLegal
            ),
            ModuleSettingItem(
                id = "security",
                title = "Security",
                description = "Configure access control, audit logs, and security policies.",
                icon = R.drawable.security,
                tags = listOf("Access", "Authentication", "Policies"),
                isConfigured = true,
                onConfigure = onConfigureSecurity
            ),
            ModuleSettingItem(
                id = "reports",
                title = "Reports",
                description = "Build dashboards, schedule reports, and configure analytics.",
                icon = R.drawable.reports,
                tags = listOf("Reports", "Visibility", "Exports"),
                isConfigured = true,
                onConfigure = onConfigureReports
            )
        )
    }

    val filteredModules = remember(searchQuery, moduleList) {
        if (searchQuery.isBlank()) moduleList
        else moduleList.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                    it.description.contains(searchQuery, ignoreCase = true) ||
                    it.tags.any { tag -> tag.contains(searchQuery, ignoreCase = true) }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        TitleBar(title = "Module Settings", onClose = onClose)
        HorizontalDivider(color = light_grey)

        SearchFilterBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            placeholder = "Search Module Settings...",
            accentColor = PrimaryBlue,
            borderColor = BorderGray,
            textSecondaryColor = TextSecondary,
            onFilterClick = { }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(filteredModules, key = { it.id }) { module ->
                ModuleSettingCard(module = module)
            }
        }
    }
}

@Composable
fun ModuleSettingCard(module: ModuleSettingItem) {
    var isExpanded by remember { mutableStateOf(module.subItems.isNotEmpty()) }

    val statusBg = if (module.isConfigured) Color(0xFFE6F7ED) else Color(0xFFFEF3C7)
    val statusTextColor = if (module.isConfigured) Color(0xFF10B981) else Color(0xFFD97706)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (module.subItems.isNotEmpty()) {
                            isExpanded = !isExpanded
                        } else {
                            module.onConfigure()
                        }
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(background_light_purple),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = module.icon),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(Modifier.width(12.dp))

                Text(
                    text = module.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = title_color,
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .background(statusBg, RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (module.isConfigured) "CONFIGURED" else "SETUP REQUIRED",
                        color = statusTextColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.4.sp
                    )
                }

                Spacer(Modifier.width(6.dp))

                if (module.subItems.isNotEmpty()) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = module.description,
                fontSize = 13.sp,
                color = close_color,
                lineHeight = 18.sp
            )

            AnimatedVisibility(
                visible = isExpanded && module.subItems.isNotEmpty(),
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    module.subItems.forEach { subItem ->
                        HorizontalDivider(
                            color = Color(0xFFF1F5F9),
                            thickness = 1.dp
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { subItem.onClick() }
                                .padding(vertical = 14.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = subItem.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = title_color
                            )

                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}