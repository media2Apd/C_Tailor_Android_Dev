@file:Suppress("unused", "unusedVariable", "SpellCheckingInspection", "UNUSED_PARAMETER", "AssignedValueIsNeverRead")

package com.cuso.tailor.view.home.inventory.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.R
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.settings.NotificationChannelsConfig
import com.cuso.tailor.model.settings.SafetyBufferConfig
import com.cuso.tailor.model.settings.SaveAutoReorderSettingsRequest
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.view.home.sales.lead.MiniSwitch
import com.cuso.tailor.viewmodel.HrViewModel
import com.cuso.tailor.viewmodel.InventoryViewModel
import com.cuso.tailor.viewmodel.SettingsViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AutoReorderSettingsScreen(
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    inventoryViewModel: InventoryViewModel = hiltViewModel(),
    hrViewModel: HrViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
    onSaved: () -> Unit = {} // Callback used to reload the parent screen after a successful save
) {
    val tokens = LocalAppTokens.current

    val suppliers by inventoryViewModel.supplierDropdown.collectAsStateWithLifecycle()
    val warehouses by inventoryViewModel.warehouseDropdown.collectAsStateWithLifecycle()
    val rolesFromApi by hrViewModel.roles.collectAsStateWithLifecycle()
    val isLoadingSettings by settingsViewModel.isLoadingSettings.collectAsStateWithLifecycle()
    val isSavingSettings by settingsViewModel.isSavingAutoReorderSettings.collectAsStateWithLifecycle()

    // --- State variables ---
    var autoReorderEngineEnabled by remember { mutableStateOf(true) }
    var checkFrequency by remember { mutableStateOf("") }
    var lastRunAtTimestamp by remember { mutableStateOf<String?>(null) }
    var lastCheckedText by remember { mutableStateOf("") }
    var isFrequencyDropdownExpanded by remember { mutableStateOf(false) }

    var consolidationType by remember { mutableStateOf("") }
    var appendToExistingDraftPO by remember { mutableStateOf(false) }
    var staticLookAheadEnabled by remember { mutableStateOf(false) }
    var lookAheadThresholdPercent by remember { mutableIntStateOf(15) }

    var salesVelocityEnabled by remember { mutableStateOf(false) }
    var salesDepletionDays by remember { mutableStateOf("") }
    var salesLookbackDays by remember { mutableStateOf("") }

    var selectedSupplierId by remember { mutableStateOf("") }
    var selectedSupplierName by remember { mutableStateOf("") }
    var isSupplierDropdownExpanded by remember { mutableStateOf(false) }

    var selectedWarehouseId by remember { mutableStateOf("") }
    var selectedWarehouseName by remember { mutableStateOf("") }
    var isWarehouseDropdownExpanded by remember { mutableStateOf(false) }

    var minOrderQty by remember { mutableStateOf("") }
    var maxOrderQty by remember { mutableStateOf("") }

    var autoCreatePO by remember { mutableStateOf(true) }
    var autoApprovePO by remember { mutableStateOf(false) }
    var sendNotificationToPurchasing by remember { mutableStateOf(true) }

    var emailNotifications by remember { mutableStateOf(true) }
    var inAppNotifications by remember { mutableStateOf(true) }
    var smsAlerts by remember { mutableStateOf(false) }

    // Dynamic selected role IDs from API
    val selectedRoleIds = remember { mutableStateListOf<String>() }

    var selectedPriorityRule by remember { mutableStateOf("") }

    var safetyBufferEnabled by remember { mutableStateOf(false) }
    var safetyBufferQuantity by remember { mutableStateOf("") }

    // Inline validation message shown above the bottom action buttons
    var validationError by remember { mutableStateOf<String?>(null) }

    val serverSettings by settingsViewModel.autoReorderSettings.collectAsStateWithLifecycle()

    // 1. Fetch initial dropdowns, roles, and settings
    LaunchedEffect(Unit) {
        inventoryViewModel.fetchSupplierDropdown()
        inventoryViewModel.loadWarehouseDropdown()
        hrViewModel.fetchRoles()
        settingsViewModel.fetchAutoReorderSettings()
    }

    // 2. Automatically update all states whenever serverSettings changes (on initial load or after a successful save + re-fetch)
    LaunchedEffect(serverSettings) {
        serverSettings?.let { settings ->
            // Any fresh server data invalidates an old validation message
            validationError = null

            // Prefill Cadence
            autoReorderEngineEnabled = settings.enabled
            checkFrequency = settings.checkFrequency.ifBlank { "Every 1 Minute" }
            lastRunAtTimestamp = settings.lastRunAt
            settings.lastRunAt?.let { lastCheckedText = it.replace("T", ", ").take(21) }

            // Prefill Consolidation
            consolidationType = settings.poConsolidationMode.ifBlank { "CONSOLIDATED" }
            appendToExistingDraftPO = settings.appendToExistingDraftPO
            staticLookAheadEnabled = settings.enableLookAheadWindow
            lookAheadThresholdPercent = settings.lookAheadThresholdPercent

            // Prefill Sales Velocity
            salesVelocityEnabled = settings.enableSalesVelocity
            salesDepletionDays = settings.velocityLookAheadDays.toString()
            salesLookbackDays = settings.salesHistoryDays.toString()

            // Prefill Defaults
            settings.defaultSupplierId?.let {
                selectedSupplierId = it.id
                selectedSupplierName = it.name
            }
            settings.defaultWarehouseId?.let {
                selectedWarehouseId = it.id
                selectedWarehouseName = it.name
            }
            minOrderQty = settings.minOrderQty.toString()
            maxOrderQty = settings.maxOrderQty.toString()

            // Prefill PO Lifecycle
            autoCreatePO = settings.autoCreatePO
            autoApprovePO = settings.autoApprovePO
            sendNotificationToPurchasing = settings.notifyPurchasingTeam

            // Prefill Notification channels
            settings.notificationChannels?.let {
                emailNotifications = it.email
                inAppNotifications = it.inApp
                smsAlerts = it.sms
            }

            // Prefill Roles
            selectedRoleIds.clear()
            selectedRoleIds.addAll(settings.recipientRoleIds)

            // Prefill Priority
            selectedPriorityRule = settings.reorderPriorityRule.ifBlank { "Product Priority" }

            // Prefill Safety Buffer
            settings.safetyBuffer?.let {
                safetyBufferEnabled = it.enabled
                safetyBufferQuantity = it.quantity.toString()
            }
        }
    }

    // Validates the form and returns an error message, or null when everything is valid
    fun validateForm(): String? {
        if (selectedSupplierId.isBlank()) return "Please select a default supplier."
        if (selectedWarehouseId.isBlank()) return "Please select a default warehouse."

        val minQty = minOrderQty.toIntOrNull()
        val maxQty = maxOrderQty.toIntOrNull()
        if (minQty == null || minQty <= 0) return "Min order quantity must be greater than 0."
        if (maxQty == null || maxQty < minQty) {
            return "Max order quantity must be greater than or equal to min order quantity."
        }

        if (salesVelocityEnabled) {
            val depletionDays = salesDepletionDays.toIntOrNull()
            val lookbackDays = salesLookbackDays.toIntOrNull()
            if (depletionDays == null || depletionDays <= 0) {
                return "Sales velocity: stock depletion days must be greater than 0."
            }
            if (lookbackDays == null || lookbackDays <= 0) {
                return "Sales velocity: sales history days must be greater than 0."
            }
        }

        if (safetyBufferEnabled) {
            val bufferQty = safetyBufferQuantity.toIntOrNull()
            if (bufferQty == null || bufferQty <= 0) {
                return "Safety buffer quantity must be greater than 0."
            }
        }
        return null
    }

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(whiteBg)
            ) {
                TitleBar(
                    title = "Auto Reorder Settings",
                    onClose = onClose
                )
                HorizontalDivider(color = title_border, thickness = 1.dp)
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(whiteBg)
            ) {
                HorizontalDivider(color = title_border, thickness = 1.dp)

                // Inline validation message
                validationError?.let { message ->
                    Text(
                        text = message,
                        fontSize = 12.sp,
                        color = Color(0xFFDC2626),
                        lineHeight = 16.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 10.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onClose,
                        enabled = !isSavingSettings,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0xFFF8FAFC),
                            contentColor = TextPrimary
                        )
                    ) {
                        Text(
                            text = "Cancel Changes",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Button(
                        onClick = {
                            // Block the save call when the form is invalid
                            val error = validateForm()
                            if (error != null) {
                                validationError = error
                                return@Button
                            }
                            validationError = null

                            val payload = SaveAutoReorderSettingsRequest(
                                enabled = autoReorderEngineEnabled,
                                checkFrequency = checkFrequency,
                                lastRunAt = lastRunAtTimestamp,
                                poConsolidationMode = consolidationType,
                                appendToExistingDraftPO = appendToExistingDraftPO,
                                enableLookAheadWindow = staticLookAheadEnabled,
                                lookAheadThresholdPercent = lookAheadThresholdPercent,
                                enableSalesVelocity = salesVelocityEnabled,
                                velocityLookAheadDays = salesDepletionDays.toIntOrNull() ?: 7,
                                salesHistoryDays = salesLookbackDays.toIntOrNull() ?: 30,
                                defaultSupplierId = selectedSupplierId,
                                defaultWarehouseId = selectedWarehouseId,
                                minOrderQty = minOrderQty.toIntOrNull() ?: 20,
                                maxOrderQty = maxOrderQty.toIntOrNull() ?: 200,
                                autoCreatePO = autoCreatePO,
                                autoApprovePO = autoApprovePO,
                                notifyPurchasingTeam = sendNotificationToPurchasing,
                                notificationChannels = NotificationChannelsConfig(
                                    email = emailNotifications,
                                    inApp = inAppNotifications,
                                    sms = smsAlerts
                                ),
                                recipientRoleIds = selectedRoleIds.toList(),
                                reorderPriorityRule = selectedPriorityRule,
                                safetyBuffer = SafetyBufferConfig(
                                    enabled = safetyBufferEnabled,
                                    quantity = safetyBufferQuantity.toIntOrNull() ?: 0
                                )
                            )

                            // The ViewModel saves, shows the message, and re-fetches the settings.
                            // onSuccess runs only after the fresh data has been loaded from the server.
                            settingsViewModel.saveAutoReorderSettings(
                                request = payload,
                                onSuccess = {
                                    // 1. Refresh rules in inventoryViewModel
                                    inventoryViewModel.fetchAutoReorderRules()
                                    // 2. Notify parent
                                    onSaved()
                                }
                            )
                        },
                        enabled = !isSavingSettings,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary,
                            disabledContainerColor = disabled,
                            contentColor = Color.White
                        )
                    ) {
                        if (isSavingSettings) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Save All Settings",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        // Show the full-screen loader only on the very first load.
        // During the post-save re-fetch the form stays visible, so there is no flicker
        // and the scroll position is preserved.
        if (isLoadingSettings && serverSettings == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── 1. Automation Cadence ──
                item {
                    SettingsCardContainer(
                        icon = R.drawable.ic_thunder,
                        title = "Automation Cadence"
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Enable Auto Reorder Engine",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = "Automatically scans stock and runs replenishment calculations",
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    lineHeight = 16.sp
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            MiniSwitch(
                                checked = autoReorderEngineEnabled,
                                onCheckedChange = { autoReorderEngineEnabled = it }
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 14.dp),
                            color = Color(0xFFF1F5F9),
                            thickness = 1.dp
                        )

                        FormLabel("Inventory Check Frequency")
                        Spacer(Modifier.height(4.dp))
                        FormDropdown(
                            value = checkFrequency,
                            expanded = isFrequencyDropdownExpanded,
                            onExpandChange = { isFrequencyDropdownExpanded = it },
                            options = listOf(
                                "Every 1 Minute",
                                "Every 1 Hour",
                                "Every 6 Hours",
                                "Every 12 Hours",
                                "Daily",
                                "Weekly"
                            ),
                            onOptionSelected = { checkFrequency = it }
                        )

                        Spacer(Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Schedule,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Last checked: $lastCheckedText",
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                }

                // ── 2. Logistics & PO Consolidation ──
                item {
                    SettingsCardContainer(
                        icon = R.drawable.ic_delivery,
                        title = "Logistics & PO Consolidation",
                        subtitle = "Eliminate fragmented deliveries and optimize container capacity by batching supplier orders."
                    ) {
                        // Option 1: Consolidated Batching
                        val isConsolidated = consolidationType == "CONSOLIDATED"
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .border(
                                    width = if (isConsolidated) 1.5.dp else 1.dp,
                                    color = if (isConsolidated) Primary else Color(0xFFE2E8F0),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .background(if (isConsolidated) Color(0xFFF8FAFC) else Color.White)
                                .clickable { consolidationType = "CONSOLIDATED" }
                                .padding(14.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            AppRadioButton(
                                selected = isConsolidated,
                                onClick = { consolidationType = "CONSOLIDATED" }
                            )
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Consolidated Batching",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Primary.copy(alpha = 0.1f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Recommended",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Primary
                                        )
                                    }
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "Combines items from the same supplier & warehouse into a single PO to save shipping and container costs.",
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        // Option 2: Individual Orders
                        val isIndividual = consolidationType == "INDIVIDUAL"
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .border(
                                    width = if (isIndividual) 1.5.dp else 1.dp,
                                    color = if (isIndividual) Primary else Color(0xFFE2E8F0),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .background(if (isIndividual) Color(0xFFF8FAFC) else Color.White)
                                .clickable { consolidationType = "INDIVIDUAL" }
                                .padding(14.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            AppRadioButton(
                                selected = isIndividual,
                                onClick = { consolidationType = "INDIVIDUAL" }
                            )
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Individual Orders",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "Generates an isolated Purchase Order for each triggered item immediately.",
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        RowToggleItem(
                            title = "Append to Existing Draft POs",
                            subtitle = "Add new items into open Draft PO for same supplier",
                            checked = appendToExistingDraftPO,
                            onCheckedChange = { appendToExistingDraftPO = it }
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = Color(0xFFF1F5F9),
                            thickness = 1.dp
                        )

                        // Static Look-Ahead Co-Ordering Container
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Toggle Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Static Look-Ahead Co-Ordering",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = "Bundle other items from the same supplier that are within a % safety margin of their threshold.",
                                        fontSize = 11.sp,
                                        color = TextSecondary,
                                        lineHeight = 15.sp
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                MiniSwitch(
                                    checked = staticLookAheadEnabled,
                                    onCheckedChange = { staticLookAheadEnabled = it }
                                )
                            }

                            // Expandable Threshold Proximity Margin input (shown only when toggle is enabled)
                            if (staticLookAheadEnabled) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    color = Color(0xFFF1F5F9),
                                    thickness = 1.dp
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Threshold Proximity Margin:",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimary
                                    )

                                    Box(modifier = Modifier.width(110.dp)) {
                                        FormTextField(
                                            value = lookAheadThresholdPercent.toString(),
                                            onValueChange = { input ->
                                                // Keep only digits and clamp the percentage to 0..100
                                                lookAheadThresholdPercent =
                                                    (input.filter { it.isDigit() }.take(3).toIntOrNull() ?: 0)
                                                        .coerceIn(0, 100)
                                            },
                                            placeholder = "",
                                            trailingText = "%",
                                            keyboardType = KeyboardType.Number
                                        )
                                    }
                                }
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = Color(0xFFF1F5F9),
                            thickness = 1.dp
                        )

                        // Sales Velocity Co-Ordering Container
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                                .background(Color.White)
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Primary.copy(alpha = 0.08f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                        contentDescription = null,
                                        tint = Primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Sales Velocity Co-Ordering (Fast-Moving Items)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = "Dynamically bundles high-selling products from the same supplier if their current stock will deplete within X days based on sales run-rate.",
                                        fontSize = 11.sp,
                                        color = TextSecondary,
                                        lineHeight = 15.sp
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                                MiniSwitch(
                                    checked = salesVelocityEnabled,
                                    onCheckedChange = { salesVelocityEnabled = it }
                                )
                            }

                            if (salesVelocityEnabled) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 14.dp),
                                    color = Color(0xFFF1F5F9),
                                    thickness = 1.dp
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        FormLabel("Bundle if stock lasts less than:")
                                        Spacer(Modifier.height(4.dp))
                                        FormTextField(
                                            value = salesDepletionDays,
                                            onValueChange = {
                                                salesDepletionDays =
                                                    it.filter { ch -> ch.isDigit() }
                                            },
                                            placeholder = "7",
                                            trailingText = "Days",
                                            keyboardType = KeyboardType.Number
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text = "E.g., 7 days means fast-moving items depleting in a week will be bundled.",
                                            fontSize = 10.sp,
                                            color = TextSecondary,
                                            lineHeight = 14.sp
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        FormLabel("Calculate run-rate from past:")
                                        Spacer(Modifier.height(4.dp))
                                        FormTextField(
                                            value = salesLookbackDays,
                                            onValueChange = {
                                                salesLookbackDays = it.filter { ch -> ch.isDigit() }
                                            },
                                            placeholder = "30",
                                            trailingText = "Days of sales",
                                            keyboardType = KeyboardType.Number
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text = "Historical sales window to compute average daily consumption.",
                                            fontSize = 10.sp,
                                            color = TextSecondary,
                                            lineHeight = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ── 3. Reorder Configuration (Defaults) ──
                item {
                    SettingsCardContainer(
                        icon = R.drawable.services,
                        title = "Reorder Configuration (Defaults)"
                    ) {
                        FormLabel("Default Supplier")
                        Spacer(Modifier.height(4.dp))
                        val supplierOptions = suppliers.map { it.label }
                        FormDropdown(
                            value = selectedSupplierName,
                            expanded = isSupplierDropdownExpanded,
                            onExpandChange = { isSupplierDropdownExpanded = it },
                            options = supplierOptions.ifEmpty { listOf(selectedSupplierName) },
                            onOptionSelected = { chosen ->
                                selectedSupplierName = chosen
                                selectedSupplierId =
                                    suppliers.find { it.label == chosen }?.value.orEmpty()
                            }
                        )

                        Spacer(Modifier.height(14.dp))

                        FormLabel("Default Warehouse")
                        Spacer(Modifier.height(4.dp))
                        val warehouseOptions = warehouses.map { it.label }
                        FormDropdown(
                            value = selectedWarehouseName,
                            expanded = isWarehouseDropdownExpanded,
                            onExpandChange = { isWarehouseDropdownExpanded = it },
                            options = warehouseOptions.ifEmpty { listOf(selectedWarehouseName) },
                            onOptionSelected = { chosen ->
                                selectedWarehouseName = chosen
                                selectedWarehouseId =
                                    warehouses.find { it.label == chosen }?.value.orEmpty()
                            }
                        )

                        Spacer(Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                FormLabel("Min Order Qty")
                                Spacer(Modifier.height(4.dp))
                                FormTextField(
                                    value = minOrderQty,
                                    onValueChange = {
                                        minOrderQty = it.filter { ch -> ch.isDigit() }
                                    },
                                    placeholder = "20",
                                    keyboardType = KeyboardType.Number
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                FormLabel("Max Order Qty")
                                Spacer(Modifier.height(4.dp))
                                FormTextField(
                                    value = maxOrderQty,
                                    onValueChange = {
                                        maxOrderQty = it.filter { ch -> ch.isDigit() }
                                    },
                                    placeholder = "200",
                                    keyboardType = KeyboardType.Number
                                )
                            }
                        }
                    }
                }

                // ── 4. Purchase Order Lifecycle ──
                item {
                    SettingsCardContainer(
                        icon = R.drawable.orders,
                        title = "Purchase Order Lifecycle"
                    ) {
                        RowToggleItem(
                            title = "Auto Create Purchase Orders",
                            subtitle = "Generate Draft POs when thresholds breached",
                            checked = autoCreatePO,
                            onCheckedChange = { autoCreatePO = it }
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = Color(0xFFF1F5F9),
                            thickness = 1.dp
                        )

                        RowToggleItem(
                            title = "Auto Approve Purchase Orders",
                            subtitle = "Bypass approval hierarchy directly as Approved",
                            checked = autoApprovePO,
                            onCheckedChange = { autoApprovePO = it }
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = Color(0xFFF1F5F9),
                            thickness = 1.dp
                        )

                        RowToggleItem(
                            title = "Send Notification to Purchasing Team",
                            subtitle = "Alert assigned members when POs are spawned",
                            checked = sendNotificationToPurchasing,
                            onCheckedChange = { sendNotificationToPurchasing = it }
                        )
                    }
                }

                // ── 5. Notification Channels & Recipients ──
                item {
                    SettingsCardContainer(
                        icon = R.drawable.ic_notification,
                        title = "Notification Channels & Recipients"
                    ) {
                        RowToggleItem(
                            title = "Email Notifications",
                            subtitle = "Send purchase order summaries directly to inbox",
                            checked = emailNotifications,
                            onCheckedChange = { emailNotifications = it }
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = Color(0xFFF1F5F9),
                            thickness = 1.dp
                        )

                        RowToggleItem(
                            title = "In-App Notifications",
                            subtitle = "Display alerts on dashboard bell icon",
                            checked = inAppNotifications,
                            onCheckedChange = { inAppNotifications = it }
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = Color(0xFFF1F5F9),
                            thickness = 1.dp
                        )

                        RowToggleItem(
                            title = "SMS Alerts",
                            subtitle = "Send urgent text alerts to registered mobile numbers",
                            checked = smsAlerts,
                            onCheckedChange = { smsAlerts = it }
                        )

                        Spacer(Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Notify Recipient Roles",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Primary.copy(alpha = 0.08f))
                                    .padding(horizontal = 8.dp, vertical = 0.dp)
                            ) {
                                Text(
                                    text = "Owner always notified",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = Primary
                                )
                            }
                        }

                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Select additional custom roles who should receive auto-reorder alerts.",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )

                        Spacer(Modifier.height(12.dp))

                        // Roles flow/pill selection from API roles
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rolesFromApi.forEach { role ->
                                val isSelected = role._id in selectedRoleIds
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) Primary.copy(alpha = 0.4f) else Color(
                                                0xFFE2E8F0
                                            ),
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .background(if (isSelected) Primary.copy(alpha = 0.08f) else Color.White)
                                        .clickable {
                                            if (isSelected) {
                                                selectedRoleIds.remove(role._id)
                                            } else {
                                                selectedRoleIds.add(role._id)
                                            }
                                        }
                                        .padding(horizontal = 12.dp, vertical = 0.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected) Primary else Color(
                                                    0xFF94A3B8
                                                )
                                            )
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = role.name,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                                        color = if (isSelected) Primary else TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                // ── 6. Reorder Priority Rules ──
                item {
                    SettingsCardContainer(
                        icon = R.drawable.ic_document,
                        title = "Reorder Priority Rules",
                        subtitle = "Determine which dimension takes precedence during supplier capacity constraints."
                    ) {
                        val priorityOptions =
                            listOf("Supplier Priority", "Warehouse Priority", "Product Priority")

                        priorityOptions.forEach { opt ->
                            val isSelected = selectedPriorityRule == opt
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) Primary else Color(0xFFE2E8F0),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .background(if (isSelected) Primary.copy(alpha = 0.03f) else Color.White)
                                    .clickable { selectedPriorityRule = opt }
                                    .padding(horizontal = 12.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AppRadioButton(
                                    selected = isSelected,
                                    onClick = { selectedPriorityRule = opt }
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = opt,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = TextPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Outlined.Info,
                                    contentDescription = null,
                                    tint = if (isSelected) Primary else Color(0xFF94A3B8),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // ── 7. Safety Buffer Settings ──
                item {
                    SettingsCardContainer(
                        icon = R.drawable.security,
                        title = "Safety Buffer Settings"
                    ) {
                        RowToggleItem(
                            title = "Enable Safety Buffer",
                            subtitle = "The engine injects additional safety cushion units into generated orders to insulate against supply delays.",
                            checked = safetyBufferEnabled,
                            onCheckedChange = { safetyBufferEnabled = it }
                        )

                        Spacer(Modifier.height(16.dp))

                        FormLabel("Safety Buffer Quantity")
                        Spacer(Modifier.height(4.dp))
                        FormTextField(
                            value = safetyBufferQuantity,
                            onValueChange = {
                                safetyBufferQuantity = it.filter { ch -> ch.isDigit() }
                            },
                            placeholder = "0",
                            trailingText = "Units",
                            keyboardType = KeyboardType.Number
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// REUSABLE SUB-COMPONENTS
// ─────────────────────────────────────────────────────────────

@Composable
fun SettingsCardContainer(
    icon: Int,
    title: String,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = icon),
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
            if (subtitle != null) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )
            }
            Spacer(Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun RowToggleItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = TextSecondary,
                lineHeight = 15.sp
            )
        }
        Spacer(Modifier.width(12.dp))
        MiniSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}