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
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.R
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.permission.PaywallModuleConfig
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.SearchFilterBar
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.view.home.paywall.AccessRestrictedDialog
import com.cuso.tailor.view.home.sales.settings.design.DesignSetupScreen
import com.cuso.tailor.view.home.search.SearchablePageItem
import com.cuso.tailor.view.home.search.extractSearchablePages
import com.cuso.tailor.viewmodel.PermissionViewModel

// Sub-Item Model supporting optional nested children
data class ModuleSubItem(
    val title: String,
    val permissionModule: String? = null,
    val permissionFeature: String? = null,
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
    onPaywallRequired: (PaywallModuleConfig) -> Unit = { onNavigateToModule("module_upgrade_paywall") },
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
    val myPermissions by permissionViewModel.myPermissions.collectAsStateWithLifecycle()
    val modulePermissions by permissionViewModel.modulePermissions.collectAsStateWithLifecycle()
    var restrictedPermissionLabel by remember { mutableStateOf<String?>(null) }

    fun checkAndNavigate(
        module: String,
        feature: String,
        permissionLabel: String = "$module.$feature",
        action: () -> Unit
    ) {
        when {
            // 1. Highest priority: check plan subscription first and open Upgrade Paywall
            !permissionViewModel.isPlanFeatureSubscribed(module, feature) -> {
                onPaywallRequired(permissionViewModel.getPaywallConfigForModule(module))
            }
            // 2. Plan is subscribed: check user role view permission and show AccessRestrictedDialog
            !permissionViewModel.canViewFeature(module, feature) -> {
                restrictedPermissionLabel = permissionLabel
            }
            else -> {
                action()
            }
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
                onConfigure = { checkAndNavigate("home", "organizationProfile", "Home", onConfigureHome) }
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
                    ModuleSubItem(
                        title = "Garment",
                        permissionModule = "sales",
                        permissionFeature = "garmentMeasurement",
                        onClick = { checkAndNavigate("sales", "garmentMeasurement", "Sales - Garment", onConfigureSales) }
                    ),
                    ModuleSubItem(
                        title = "Garment Pricing",
                        permissionModule = "sales",
                        permissionFeature = "pricingGarment",
                        onClick = { checkAndNavigate("sales", "pricingGarment", "Sales - Garment Pricing", onConfigureSalesPricing) }
                    ),
                    ModuleSubItem(
                        title = "Measurement List",
                        permissionModule = "sales",
                        permissionFeature = "measurementList",
                        onClick = { checkAndNavigate("sales", "measurementList", "Sales - Measurement List", onNavigateMeasurementList) }
                    ),
                    ModuleSubItem(
                        title = "Design Setup",
                        permissionModule = "sales",
                        permissionFeature = "designSetup",
                        onClick = {
                            checkAndNavigate("sales", "designSetup", "Sales - Design Setup") {
                                isDesignSetupOpen = true
                            }
                        }
                    )
                ),
                onConfigure = { checkAndNavigate("sales", "designSetup", "Sales", onConfigureSales) }
            ),

            // 3. Marketing
            ModuleSettingItem(
                id = "marketing",
                title = "Marketing",
                description = "Manage campaigns, email automation, and audience engagement.",
                icon = R.drawable.marketing,
                tags = listOf("Campaigns", "Lead Sources", "Communication"),
                isConfigured = true,
                onConfigure = { checkAndNavigate("marketing", "dashboard", "Marketing", onConfigureMarketing) }
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
                    ModuleSubItem(
                        title = "GST Settings",
                        permissionModule = "finance",
                        permissionFeature = "finTaxRegistration",
                        onClick = { checkAndNavigate("finance", "finTaxRegistration", "Finance - GST Settings", onNavigateGstSettings) }
                    ),
                    ModuleSubItem(
                        title = "Tax Rates & Rules",
                        permissionModule = "finance",
                        permissionFeature = "finTaxRatesRules",
                        onClick = { checkAndNavigate("finance", "finTaxRatesRules", "Finance - Tax Rates & Rules", onNavigateTaxRates) }
                    )
                ),
                onConfigure = { checkAndNavigate("finance", "finChartOfAccounts", "Finance", onConfigureFinance) }
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
                            ModuleSubItem(title = "Warehouse", permissionModule = "inventory", permissionFeature = "warehouseStructure", onClick = { checkAndNavigate("inventory", "warehouseStructure", "Inventory - Warehouse", onNavigateLocationStructure) }),
                            ModuleSubItem(title = "Floor", permissionModule = "inventory", permissionFeature = "warehouseStructure", onClick = { checkAndNavigate("inventory", "warehouseStructure", "Inventory - Floor", onNavigateFloorOverview) }),
                            ModuleSubItem(title = "Section", permissionModule = "inventory", permissionFeature = "warehouseStructure", onClick = { checkAndNavigate("inventory", "warehouseStructure", "Inventory - Section", onNavigateSectionOverview) }),
                            ModuleSubItem(title = "Rack", permissionModule = "inventory", permissionFeature = "warehouseStructure", onClick = { checkAndNavigate("inventory", "warehouseStructure", "Inventory - Rack", onNavigateRackOverview) }),
                            ModuleSubItem(title = "Bin", permissionModule = "inventory", permissionFeature = "warehouseStructure", onClick = { checkAndNavigate("inventory", "warehouseStructure", "Inventory - Bin", onNavigateBinOverview) })
                        )
                    ),
                    ModuleSubItem(
                        title = "Auto Reorder Settings",
                        permissionModule = "inventory",
                        permissionFeature = "autoReorderSettings",
                        onClick = { checkAndNavigate("inventory", "autoReorderSettings", "Inventory - Auto Reorder Settings", onNavigateAutoReorder) }
                    ),
                    ModuleSubItem(
                        title = "Customization",
                        children = listOf(
                            ModuleSubItem(title = "PDF Templates", permissionModule = "inventory", permissionFeature = "pdftemp", onClick = { checkAndNavigate("inventory", "pdftemp", "Inventory - PDF Templates", onNavigatePdfTemplates) })
                        )
                    )
                ),
                onConfigure = { checkAndNavigate("inventory", "warehouseStructure", "Inventory", onNavigateLocationStructure) }
            ),

            // 6. Logistics
            ModuleSettingItem(
                id = "logistics",
                title = "Logistics",
                description = "Manage shipping, fleet tracking, and delivery operations.",
                icon = R.drawable.logistics,
                tags = listOf("Delivery", "Dispatch", "Shipment"),
                isConfigured = true,
                onConfigure = { checkAndNavigate("logistics", "delivery", "Logistics", onConfigureLogistics) }
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
                        permissionModule = "services",
                        permissionFeature = "serviceTemplates",
                        onClick = { checkAndNavigate("services", "serviceTemplates", "Services - Service Templates", onNavigateServiceTemplates) }
                    ),
                    ModuleSubItem(
                        title = "Workflow Stages",
                        permissionModule = "services",
                        permissionFeature = "workflowStages",
                        onClick = { checkAndNavigate("services", "workflowStages", "Services - Workflow Stages", onNavigateProductionStages) }
                    )
                ),
                onConfigure = { checkAndNavigate("services", "dashboard", "Services", onConfigureServices) }
            ),

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
                        permissionModule = "hr",
                        permissionFeature = "leaveTypes",
                        onClick = {
                            checkAndNavigate("hr", "leaveTypes", "HR - Leave Types", onNavigateLeaveTypes)
                        }
                    ),
                    ModuleSubItem(
                        title = "Leave Policies",
                        permissionModule = "hr",
                        permissionFeature = "leavePolicies",
                        onClick = {
                            checkAndNavigate("hr", "leavePolicies", "HR - Leave Policies", onNavigateLeavePolicies)
                        }
                    ),
                    ModuleSubItem(
                        title = "Piece Rate Setup",
                        permissionModule = "hr",
                        permissionFeature = "pieceRate",
                        onClick = {
                            checkAndNavigate("hr", "pieceRate", "HR - Piece Rate Setup", onNavigatePieceRateSetup)
                        }
                    )
                ),
                onConfigure = { checkAndNavigate("hr", "employeeManagement", "Human Resources", onConfigureHR) }
            ),

            // 9. IT
            ModuleSettingItem(
                id = "it",
                title = "IT",
                description = "Manage IT assets, helpdesk, network, and infrastructure.",
                icon = R.drawable.it,
                tags = listOf("Assets", "Access", "System Rules"),
                isConfigured = true,
                onConfigure = { checkAndNavigate("it", "deviceManagement", "IT", onConfigureIT) }
            ),

            // 10. Legal
            ModuleSettingItem(
                id = "legal",
                title = "Legal",
                description = "Manage contracts, compliance, and legal document workflows.",
                icon = R.drawable.legal,
                tags = listOf("Contracts", "Approvals", "Documents"),
                isConfigured = true,
                onConfigure = { checkAndNavigate("legal", "dashboard", "Legal", onConfigureLegal) }
            ),

            // 11. Security
            ModuleSettingItem(
                id = "security",
                title = "Security",
                description = "Configure access control, audit logs, and security policies.",
                icon = R.drawable.security,
                tags = listOf("Access", "Authentication", "Policies"),
                isConfigured = true,
                onConfigure = { checkAndNavigate("security", "dashboard", "Security", onConfigureSecurity) }
            ),

            // 12. Reports
            ModuleSettingItem(
                id = "reports",
                title = "Reports",
                description = "Build dashboards, schedule reports, and configure analytics.",
                icon = R.drawable.reports,
                tags = listOf("Reports", "Visibility", "Exports"),
                isConfigured = true,
                onConfigure = { checkAndNavigate("reports", "orderMasterRegister", "Reports", onConfigureReports) }
            )
        )
    }

    fun filterVisibleSubItems(items: List<ModuleSubItem>): List<ModuleSubItem> {
        return items.mapNotNull { item ->
            val mod = item.permissionModule
            val feat = item.permissionFeature
            if (mod != null && feat != null && permissionViewModel.isFeatureHidden(mod, feat)) {
                return@mapNotNull null
            }
            if (item.children.isNotEmpty()) {
                val visibleChildren = filterVisibleSubItems(item.children)
                if (visibleChildren.isEmpty()) null else item.copy(children = visibleChildren)
            } else {
                item
            }
        }
    }

    // Recursively filter sub-items by search query so matching nested settings stay visible
    fun filterSubItemsByQuery(items: List<ModuleSubItem>, query: String): List<ModuleSubItem> {
        return items.mapNotNull { item ->
            val titleMatches = item.title.contains(query, ignoreCase = true)
            if (item.children.isNotEmpty()) {
                val matchingChildren = if (titleMatches) item.children else filterSubItemsByQuery(item.children, query)
                if (titleMatches || matchingChildren.isNotEmpty()) {
                    item.copy(children = matchingChildren)
                } else {
                    null
                }
            } else {
                if (titleMatches) item else null
            }
        }
    }

    val filteredModules = remember(searchQuery, moduleList, myPermissions) {
        val query = searchQuery.trim()
        moduleList
            .filter { module -> !permissionViewModel.isModuleHidden(module.id) }
            .mapNotNull { module ->
                val visibleSubItems = if (module.subItems.isEmpty()) {
                    emptyList()
                } else {
                    filterVisibleSubItems(module.subItems)
                }

                if (query.isBlank()) {
                    module.copy(subItems = visibleSubItems)
                } else {
                    val moduleMatches = module.title.contains(query, ignoreCase = true) ||
                            module.description.contains(query, ignoreCase = true) ||
                            module.tags.any { tag -> tag.contains(query, ignoreCase = true) }
                    val matchingSubItems = filterSubItemsByQuery(visibleSubItems, query)

                    when {
                        moduleMatches -> module.copy(subItems = visibleSubItems)
                        matchingSubItems.isNotEmpty() -> module.copy(subItems = matchingSubItems)
                        else -> null
                    }
                }
            }
    }

    // Extract all app pages so searching here also surfaces any screen across the entire app
    val allAppPages = remember(permissionViewModel, myPermissions, modulePermissions) {
        extractSearchablePages(permissionViewModel)
    }

    val matchingAppPages = remember(searchQuery, allAppPages) {
        val query = searchQuery.trim().lowercase()
        if (query.isEmpty()) {
            emptyList()
        } else {
            val queryWords = query.split(Regex("\\s+")).filter { it.isNotBlank() }
            allAppPages.filter { page ->
                queryWords.all { word -> page.searchTerms.contains(word) }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                placeholder = "Search modules or any app page...",
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
                if (searchQuery.isNotBlank() && filteredModules.isNotEmpty()) {
                    item(key = "header_module_settings") {
                        Text(
                            text = "MODULE SETTINGS",
                            fontSize = tokens.label,
                            fontWeight = FontWeight.SemiBold,
                            color = mutedText
                        )
                    }
                }

                items(filteredModules, key = { it.id }) { module ->
                    ModuleSettingCard(
                        module = module,
                        forceExpand = searchQuery.isNotBlank() && module.subItems.isNotEmpty()
                    )
                }

                if (matchingAppPages.isNotEmpty()) {
                    item(key = "header_app_pages") {
                        Text(
                            text = "ALL APP PAGES (${matchingAppPages.size})",
                            fontSize = tokens.label,
                            fontWeight = FontWeight.SemiBold,
                            color = mutedText,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    items(matchingAppPages, key = { "page_${it.id}" }) { page ->
                        ModuleSearchPageRow(
                            page = page,
                            onClick = { onNavigateToModule(page.routeKey) }
                        )
                    }
                }
            }
        }

        restrictedPermissionLabel?.let { permissionLabel ->
            AccessRestrictedDialog(
                onRequestAccess = { restrictedPermissionLabel = null },
                onGoBack = { restrictedPermissionLabel = null },
                onDismiss = { restrictedPermissionLabel = null },
                requiredPermission = permissionLabel
            )
        }
    }
}

@Composable
fun ModuleSettingCard(
    module: ModuleSettingItem,
    forceExpand: Boolean = false
) {
    val tokens = LocalAppTokens.current
    // Enable only modules that have dropdown sub-items
    val isEnabled = module.subItems.isNotEmpty()
    var isExpanded by remember { mutableStateOf(false) }
    val showExpanded = isEnabled && (forceExpand || isExpanded)

    val statusBg = when {
        !isEnabled -> whiteBg
        module.isConfigured -> greenBg
        else -> yellowBg
    }
    val statusTextColor = when {
        !isEnabled -> TextSecondary
        module.isConfigured -> darkGreenBg
        else -> yellowText
    }
    val statusLabel = when {
        !isEnabled -> "Disabled"
        module.isConfigured -> "Configured"
        else -> "Setup Required"
    }

    Card(
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = if (isEnabled) whiteBg else light_grey
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        ),
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (isEnabled) 1f else 0.55f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(tokens.screenPadding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = isEnabled) {
                        isExpanded = !isExpanded
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
                        .padding(horizontal = 8.dp, vertical = 0.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = statusLabel,
                        color = statusTextColor,
                        fontSize = tokens.label,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (isEnabled) {
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        imageVector = if (showExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
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
                visible = showExpanded && module.subItems.isNotEmpty(),
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
                        SubItemRowRenderer(
                            subItem = subItem,
                            forceExpand = forceExpand
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SubItemRowRenderer(
    subItem: ModuleSubItem,
    forceExpand: Boolean = false
) {
    val tokens = LocalAppTokens.current
    val hasChildren = subItem.children.isNotEmpty()
    var isChildGroupExpanded by remember { mutableStateOf(false) }
    val showChildExpanded = forceExpand || isChildGroupExpanded

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
                    imageVector = if (showChildExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(tokens.iconSize)
                )
            }

            AnimatedVisibility(
                visible = showChildExpanded,
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

@Composable
private fun ModuleSearchPageRow(
    page: SearchablePageItem,
    onClick: () -> Unit
) {
    val tokens = LocalAppTokens.current

    Card(
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.85f),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        border = BorderStroke(1.dp, sectionBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = tokens.screenPadding, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(tokens.buttonHeight * 0.85f)
                    .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.45f))
                    .background(background_light_purple),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Layers,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(tokens.iconSize * 0.9f)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = page.title,
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = title_color,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = page.breadcrumb,
                    fontSize = tokens.caption,
                    color = mutedText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.Default.NorthEast,
                contentDescription = "Open page",
                tint = mutedText,
                modifier = Modifier.size(tokens.iconSize * 0.85f)
            )
        }
    }
}