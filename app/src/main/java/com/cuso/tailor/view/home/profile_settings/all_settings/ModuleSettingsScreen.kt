@file:Suppress("unused", "SpellCheckingInspection", "UNUSED_PARAMETER", "AssignedValueIsNeverRead")

package com.cuso.tailor.view.home.profile_settings.all_settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.cuso.tailor.R
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.SearchFilterBar
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.view.home.sales.settings.design.DesignSetupScreen
import com.cuso.tailor.viewmodel.PermissionViewModel

// Sub-Item Model supporting optional nested children
data class ModuleSubItem(
    val title: String,
    val onClick: (() -> Unit)? = null,
    val children: List<ModuleSubItem> = emptyList()
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
    onNavigateLeaveTypes: () -> Unit = { onNavigateToModule("hr_leave_types") },
    onNavigateLeavePolicies: () -> Unit = { onNavigateToModule("hr_leave_policies") },
    onNavigatePieceRateSetup: () -> Unit = { onNavigateToModule("hr_piece_rate_setup") },
    onConfigureSales: () -> Unit = { onNavigateToModule("sales_garment_type") },
    onConfigureSalesPricing: () -> Unit = { onNavigateToModule("sales_garment_pricing_setup") },
    onNavigateProductionStages: () -> Unit = { onNavigateToModule("services_production_stages") },
    onNavigateMeasurementList: () -> Unit = { onNavigateToModule("sales_measurement_list") },
    onNavigateDesignSetup: () -> Unit = { onNavigateToModule("sales_design_setup") },
    onConfigureMarketing: () -> Unit = {},
    onConfigureFinance: () -> Unit = { onNavigateToModule("finance_chart_of_accounts") },
    onConfigureInventory: () -> Unit = { onNavigateToModule("inventory_location_structure") },
    onNavigateWarehouse: () -> Unit = { onNavigateToModule("inventory_location_structure") },
    onNavigateFloorOverview: () -> Unit = { onNavigateToModule("inventory_floor_overview") },
    onNavigateSectionOverview: () -> Unit = { onNavigateToModule("inventory_section_overview") },
    onNavigateRackOverview: () -> Unit = { onNavigateToModule("inventory_rack_overview") },
    onNavigateBinOverview: () -> Unit = { onNavigateToModule("inventory_bin_overview") },
    onNavigateAutoReorder: () -> Unit = { onNavigateToModule("inventory_auto_reorder_settings") },
    onNavigatePdfTemplates: () -> Unit = { onNavigateToModule("inventory_pdf_templates") },
    onNavigateAllocationRules: () -> Unit = { onNavigateToModule("inventory_allocation_rules") },
    onNavigateLocationStructure: () -> Unit = { onNavigateToModule("inventory_location_structure") },
    onNavigateGstSettings: () -> Unit = { onNavigateToModule("finance_gst_settings") },
    onNavigateTaxRates: () -> Unit = { onNavigateToModule("finance_tax_rates") },
    onConfigureLogistics: () -> Unit = { onNavigateToModule("logistics_delivery") },
    onConfigureServices: () -> Unit = { onNavigateToModule("services_service_status") },
    onNavigateServiceTemplates: () -> Unit = { onNavigateToModule("services_service_templates") },
    onConfigureHR: () -> Unit = { onNavigateToModule("hr_all_employees") },
    onConfigureIT: () -> Unit = {},
    onConfigureLegal: () -> Unit = {},
    onConfigureSecurity: () -> Unit = {},
    onConfigureReports: () -> Unit = { onNavigateToModule("reports_sales") }
) {
    val tokens = LocalAppTokens.current
    var searchQuery by remember { mutableStateOf("") }
    val permissionViewModel: PermissionViewModel = hiltViewModel()

    fun checkAndNavigate(module: String, feature: String, action: () -> Unit) {
        if (permissionViewModel.hasFeatureAccess(module, feature)) {
            action()
        } else {
            onNavigateToModule("module_upgrade_paywall")
        }
    }
    // State to toggle the full Design Setup screen overlay
    var isDesignSetupOpen by remember { mutableStateOf(false) }

    if (isDesignSetupOpen) {
        DesignSetupScreen(
            onClose = { isDesignSetupOpen = false }
        )
        return
    }

    val moduleList = remember(
        onConfigureHome,
        onConfigureSales,
        onConfigureSalesPricing,
        onNavigateMeasurementList,
        onNavigateProductionStages,
        onNavigateDesignSetup,
        onConfigureMarketing,
        onConfigureFinance,
        onConfigureInventory,
        onNavigateWarehouse,
        onNavigateFloorOverview,
        onNavigateSectionOverview,
        onNavigateRackOverview,
        onNavigateBinOverview,
        onNavigateAutoReorder,
        onNavigatePdfTemplates,
        onNavigateTaxRates,
        onNavigateGstSettings,
        onConfigureLogistics,
        onConfigureServices,
        onConfigureHR,
        onNavigatePieceRateSetup,
        onConfigureIT,
        onConfigureLegal,
        onConfigureSecurity,
        onConfigureReports
    ) {
        listOf(
            // 1. Home
            ModuleSettingItem(
                id = "home",
                title = "Home",
                description = "Configure dashboard, widgets, and role-based home experience.",
                icon = R.drawable.home,
                tags = listOf("Dashboard", "Widgets", "Shortcuts"),
                isConfigured = true,
                onConfigure = { checkAndNavigate("home", "organizationProfile", onConfigureHome) }
            ),

            // 2. Sales
            ModuleSettingItem(
                id = "sales",
                title = "Sales",
                description = "Manage leads, quotes, orders, and sales pipeline workflows.",
                icon = R.drawable.sales,
                tags = listOf("Orders", "Pricing", "Approvals"),
                isConfigured = true,
                subItems = listOf(
                    ModuleSubItem(title = "Garment", onClick = { checkAndNavigate("sales", "garmentMeasurement", onConfigureSales) }),
                    ModuleSubItem(title = "Garment Pricing", onClick = { checkAndNavigate("sales", "pricingGarment", onConfigureSalesPricing) }),
                    ModuleSubItem(title = "Measurement List", onClick = { checkAndNavigate("sales", "measurementList", onNavigateMeasurementList) }),
                    ModuleSubItem(
                        title = "Design Setup",
                        onClick = {
                            checkAndNavigate("sales", "designSetup") {
                                onNavigateDesignSetup()
                                isDesignSetupOpen = true
                            }
                        }
                    )
                ),
                onConfigure = { checkAndNavigate("sales", "designSetup", onConfigureSales) }
            ),

            // 3. Marketing
            ModuleSettingItem(
                id = "marketing",
                title = "Marketing",
                description = "Manage campaigns, email automation, and audience engagement.",
                icon = R.drawable.marketing,
                tags = listOf("Campaigns", "Lead Sources", "Communication"),
                isConfigured = true,
                onConfigure = { checkAndNavigate("marketing", "dashboard", onConfigureMarketing) }
            ),

            // 4. Finance
            ModuleSettingItem(
                id = "finance",
                title = "Finance",
                description = "Manage billing, payments, accounting and financial workflows.",
                icon = R.drawable.finance,
                tags = listOf("Billing", "Payments", "Accounting"),
                isConfigured = true,
                subItems = listOf(
                    ModuleSubItem(title = "GST Settings", onClick = { checkAndNavigate("finance", "finTaxRegistration", onNavigateGstSettings) }),
                    ModuleSubItem(title = "Tax Rates & Rules", onClick = { checkAndNavigate("finance", "finTaxRatesRules", onNavigateTaxRates) })
                ),
                onConfigure = { checkAndNavigate("finance", "finChartOfAccounts", onConfigureFinance) }
            ),

            // 5. Inventory
            ModuleSettingItem(
                id = "inventory",
                title = "Inventory",
                description = "Manage stock levels, warehouses, and procurement workflows.",
                icon = R.drawable.inventory,
                tags = listOf("Stock", "Warehouse", "Procurement"),
                isConfigured = true,
                subItems = listOf(
                    ModuleSubItem(
                        title = "Warehouse Structure",
                        children = listOf(
                            ModuleSubItem(title = "Warehouse", onClick = { checkAndNavigate("inventory", "warehouseStructure", onNavigateLocationStructure) }),
                            ModuleSubItem(title = "Floor", onClick = { checkAndNavigate("inventory", "warehouseStructure", onNavigateFloorOverview) }),
                            ModuleSubItem(title = "Section", onClick = { checkAndNavigate("inventory", "warehouseStructure", onNavigateSectionOverview) }),
                            ModuleSubItem(title = "Rack", onClick = { checkAndNavigate("inventory", "warehouseStructure", onNavigateRackOverview) }),
                            ModuleSubItem(title = "Bin", onClick = { checkAndNavigate("inventory", "warehouseStructure", onNavigateBinOverview) })
                        )
                    ),
                    ModuleSubItem(
                        title = "Auto Reorder Settings",
                        onClick = { checkAndNavigate("inventory", "autoReorderSettings", onNavigateAutoReorder) }
                    ),
                    ModuleSubItem(
                        title = "Customization",
                        children = listOf(
                            ModuleSubItem(title = "PDF Templates", onClick = { checkAndNavigate("inventory", "pdftemp", onNavigatePdfTemplates) })
                        )
                    )
                ),
                onConfigure = { checkAndNavigate("inventory", "warehouseStructure", onNavigateLocationStructure) }
            ),

            // 6. Logistics
            ModuleSettingItem(
                id = "logistics",
                title = "Logistics",
                description = "Manage shipping, fleet tracking, and delivery operations.",
                icon = R.drawable.logistics,
                tags = listOf("Delivery", "Dispatch", "Shipment"),
                isConfigured = true,
                onConfigure = { checkAndNavigate("logistics", "delivery", onConfigureLogistics) }
            ),

            // 7. Services
            ModuleSettingItem(
                id = "services",
                title = "Services",
                description = "Configure ticketing, SLAs, knowledge base, and field service.",
                icon = R.drawable.services,
                tags = listOf("Service Orders", "Workflow", "Status Rules"),
                isConfigured = true,
                subItems = listOf(
                    ModuleSubItem(
                        title = "Service Templates",
                        onClick = { checkAndNavigate("services", "serviceTemplates", onNavigateServiceTemplates) }
                    ),
                    // ── ADD WORKFLOW STAGES SUB-ITEM HERE ──
                    // Services sub-items
                    ModuleSubItem(
                        title = "Workflow Stages",
                        // Use the real feature key returned by the permissions API
                        onClick = { checkAndNavigate("services", "workflowStages", onNavigateProductionStages) }
                    ),
                ),
                onConfigure = { checkAndNavigate("services", "dashboard", onConfigureServices) }
            ),

            // 8. Human Resources
            // 8. Human Resources
            ModuleSettingItem(
                id = "hr",
                title = "Human Resources",
                description = "Manage employees, attendance, leave, and payroll processing.",
                icon = R.drawable.hr,
                tags = listOf("Employees", "Attendance", "Payroll", "Leave"),
                isConfigured = true,
                subItems = listOf(
                    ModuleSubItem(
                        title = "Leave Types",
                        onClick = {
                            checkAndNavigate("hr", "leaveTypes", onNavigateLeaveTypes)
                        }
                    ),
                    ModuleSubItem(
                        title = "Leave Policies",
                        onClick = {
                            checkAndNavigate("hr", "leavePolicies", onNavigateLeavePolicies)
                        }
                    ),
                    ModuleSubItem(
                        title = "Piece Rate Setup",
                        onClick = {
                            // Match the exact permission key ("pieceRate") returned by /api/permissions/me
                            checkAndNavigate("hr", "pieceRate", onNavigatePieceRateSetup)
                        }
                    )
                ),
                onConfigure = { checkAndNavigate("hr", "employeeManagement", onConfigureHR) }
            ),

            // 9. IT
            ModuleSettingItem(
                id = "it",
                title = "IT",
                description = "Manage IT assets, helpdesk, network, and infrastructure.",
                icon = R.drawable.it,
                tags = listOf("Assets", "Access", "System Rules"),
                isConfigured = true,
                onConfigure = { checkAndNavigate("it", "deviceManagement", onConfigureIT) }
            ),

            // 10. Legal
            ModuleSettingItem(
                id = "legal",
                title = "Legal",
                description = "Manage contracts, compliance, and legal document workflows.",
                icon = R.drawable.legal,
                tags = listOf("Contracts", "Approvals", "Documents"),
                isConfigured = true,
                onConfigure = { checkAndNavigate("legal", "dashboard", onConfigureLegal) }
            ),

            // 11. Security
            ModuleSettingItem(
                id = "security",
                title = "Security",
                description = "Configure access control, audit logs, and security policies.",
                icon = R.drawable.security,
                tags = listOf("Access", "Authentication", "Policies"),
                isConfigured = true,
                onConfigure = { checkAndNavigate("security", "dashboard", onConfigureSecurity) }
            ),

            // 12. Reports
            ModuleSettingItem(
                id = "reports",
                title = "Reports",
                description = "Build dashboards, schedule reports, and configure analytics.",
                icon = R.drawable.reports,
                tags = listOf("Reports", "Visibility", "Exports"),
                isConfigured = true,
                onConfigure = { checkAndNavigate("reports", "orderMasterRegister", onConfigureReports) }
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
            .background(Primary_background)
    ) {
        TitleBar(title = "Module Settings", onClose = onClose)
        HorizontalDivider(color = title_border)

        SearchFilterBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            placeholder = "Search Module Settings...",
            accentColor = Primary,
            borderColor = BorderGray,
            textSecondaryColor = TextSecondary,
            onFilterClick = { }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = tokens.screenPadding, vertical = 12.dp),
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
    val tokens = LocalAppTokens.current
    var isExpanded by remember { mutableStateOf(false) }

    val statusBg = if (module.isConfigured) greenBg else yellowBg
    val statusTextColor = if (module.isConfigured) darkGreenBg else yellowText

    Card(
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        border = BorderStroke(1.dp, sectionBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(tokens.screenPadding)
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
                        .size(tokens.fieldHeight)
                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.45f))
                        .background(background_light_purple),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = module.icon),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(tokens.iconSize)
                    )
                }

                Spacer(Modifier.width(12.dp))

                Text(
                    text = module.title,
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = title_color,
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .background(statusBg, RoundedCornerShape(20.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (module.isConfigured) "CONFIGURED" else "SETUP REQUIRED",
                        color = statusTextColor,
                        fontSize = tokens.label,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(Modifier.width(6.dp))

                if (module.subItems.isNotEmpty()) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = headerGrey,
                        modifier = Modifier.size(tokens.iconSize)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = module.description,
                fontSize = tokens.caption,
                color = close_color,
                fontWeight = FontWeight.Normal
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
                            color = grey_border,
                            thickness = 1.dp
                        )
                        SubItemRowRenderer(subItem = subItem)
                    }
                }
            }
        }
    }
}

@Composable
private fun SubItemRowRenderer(subItem: ModuleSubItem) {
    val tokens = LocalAppTokens.current
    val hasChildren = subItem.children.isNotEmpty()
    var isChildGroupExpanded by remember { mutableStateOf(false) }

    if (hasChildren) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isChildGroupExpanded = !isChildGroupExpanded }
                    .padding(vertical = 12.dp, horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = subItem.title,
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Primary
                )

                Icon(
                    imageVector = if (isChildGroupExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(tokens.iconSize)
                )
            }

            AnimatedVisibility(
                visible = isChildGroupExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, bottom = 4.dp)
                ) {
                    subItem.children.forEach { child ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { child.onClick?.invoke() }
                                .padding(vertical = 10.dp, horizontal = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "•  ${child.title}",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Normal,
                                color = title_color
                            )

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = iconMuted,
                                modifier = Modifier.size(tokens.iconSize * 0.9f)
                            )
                        }
                    }
                }
            }
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { subItem.onClick?.invoke() }
                .padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = subItem.title,
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.Medium,
                color = title_color
            )

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = iconMuted,
                modifier = Modifier.size(tokens.iconSize)
            )
        }
    }
}