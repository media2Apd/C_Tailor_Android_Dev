package com.cuso.mobile.view.home.profile_settings.all_settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.cuso.mobile.ui.theme.BorderGray
import com.cuso.mobile.ui.theme.Primary
import com.cuso.mobile.ui.theme.TextSecondary
import com.cuso.mobile.ui.theme.background_light_purple
import com.cuso.mobile.ui.theme.mutedText
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.SearchFilterBar
import com.cuso.mobile.view.composable.StatusBadge
import com.cuso.mobile.view.composable.StatusBadgeVariant
import com.cuso.mobile.view.composable.TitleBar

private val PrimaryBlue = Color(0xFF3B3BF9)
private val TextDark = Color(0xFF111827)
private val CardBorderColor = Color(0xFFE5E7EB)

data class ModuleSettingItem(
    val id: String,
    val title: String,
    val description: String,
    val icon: Int,
    val tags: List<String>,
    val isConfigured: Boolean = true,
    val onConfigure: () -> Unit = {}
)

@Composable
fun ModuleSettingsScreen(
    onClose: () -> Unit,
    onNavigateToModule: (String) -> Unit = {},
    onConfigureHome: () -> Unit = { onNavigateToModule("home") },
    onConfigureSales: () -> Unit = { onNavigateToModule("sales") },
    onConfigureMarketing: () -> Unit = { onNavigateToModule("marketing") },
    onConfigureFinance: () -> Unit = { onNavigateToModule("finance") },
    onConfigureInventory: () -> Unit = { onNavigateToModule("inventory") },
    onConfigureLogistics: () -> Unit = { onNavigateToModule("logistics") },
    onConfigureServices: () -> Unit = { onNavigateToModule("services") },
    onConfigureHR: () -> Unit = { onNavigateToModule("hr") },
    onConfigureIT: () -> Unit = { onNavigateToModule("it") },
    onConfigureLegal: () -> Unit = { onNavigateToModule("legal") },
    onConfigureSecurity: () -> Unit = { onNavigateToModule("security") },
    onConfigureReports: () -> Unit = { onNavigateToModule("reports") }
) {
    var searchQuery by remember { mutableStateOf("") }

    val moduleList = remember(
        onConfigureHome,
        onConfigureSales,
        onConfigureMarketing,
        onConfigureFinance,
        onConfigureInventory,
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
                onConfigure = onConfigureInventory
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
        // --- Top Bar ---
        TitleBar(title = "Module Settings", onClose = onClose)
        HorizontalDivider(color = Color(0xFFF3F4F6))

        // --- Search Bar ---
        SearchFilterBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            placeholder = "Search Module Settings...",
            accentColor = PrimaryBlue,
            borderColor = BorderGray,
            textSecondaryColor = TextSecondary,
            onFilterClick = { }
        )

        // --- Module Cards List ---
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
private fun ModuleSettingCard(module: ModuleSettingItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardBorderColor, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(whiteBg)
            .clickable { module.onConfigure() }
            .padding(16.dp)
    ) {
        // Header (Icon, Title, Status Badge)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Outer Squircle Container
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(background_light_purple, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    // Inner Circle Container
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(whiteBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = module.icon),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(Modifier.width(12.dp))

                Text(
                    text = module.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark
                )
            }

            // Status Badge (CONFIGURED / SETUP REQUIRED)
            StatusBadge(
                text = if (module.isConfigured) "CONFIGURED" else "SETUP REQUIRED",
                variant = if (module.isConfigured) StatusBadgeVariant.SUCCESS else StatusBadgeVariant.WARNING,
            )
        }

        Spacer(Modifier.height(10.dp))

        // Description
        Text(
            text = module.description,
            fontSize = 12.5.sp,
            color = mutedText,
            lineHeight = 17.sp
        )

        Spacer(Modifier.height(12.dp))

        // Tags List
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            module.tags.forEach { tag ->
                StatusBadge(
                    text = tag,
                    variant = StatusBadgeVariant.DEFAULT
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // Action Link
        Text(
            text = "Configure ${module.title} →",
            fontSize = 15.sp,
            color = Primary
        )
    }
}