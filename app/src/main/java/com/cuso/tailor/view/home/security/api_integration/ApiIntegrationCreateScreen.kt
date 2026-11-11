package com.cuso.tailor.view.home.security.api_integration

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.security.CreateApiIntegrationRequest
import com.cuso.tailor.model.security.PermissionScopeItemDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.SecurityViewModel

// Options constants matching React component
private val CATEGORY_OPTIONS = listOf("REST API", "Webhook", "GraphQL", "gRPC")
private val AUTH_TYPE_OPTIONS = listOf("API Key", "Bearer Token", "OAuth 2.0", "Basic Auth")
private val ENVIRONMENT_OPTIONS = listOf("Production", "Sandbox", "Staging", "Development")
private val EXTERNAL_SYSTEM_OPTIONS = listOf("Ecommerce", "CRM", "ERP", "Payment Gateway", "Custom")

private val WEBHOOK_EVENTS = listOf(
    "order.created" to "Orders Created",
    "order.updated" to "Orders Updated",
    "inventory.changed" to "Inventory Changed",
    "payment.received" to "Payment Received",
    "customer.registered" to "Customer Registered"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddApiIntegrationScreen(
    integrationIdToEdit: String? = null,
    viewModel: SecurityViewModel,
    onNavigateBack: () -> Unit,
    onSuccessSaved: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val isEditMode = !integrationIdToEdit.isNullOrBlank()

    val isCreating by viewModel.isCreatingIntegration.collectAsState()
    val isUpdating by viewModel.isUpdatingIntegration.collectAsState()
    val existingDetail by viewModel.selectedIntegrationDetail.collectAsState()
    val isDetailLoading by viewModel.isLoadingIntegrationDetail.collectAsState()

    // Form fields
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("REST API") }
    var categoryExpanded by remember { mutableStateOf(false) }

    var description by remember { mutableStateOf("") }
    var externalSystem by remember { mutableStateOf("Ecommerce") }
    var externalSystemExpanded by remember { mutableStateOf(false) }
    var environment by remember { mutableStateOf("Production") }
    var environmentExpanded by remember { mutableStateOf(false) }

    // API Configuration
    var baseApiUrl by remember { mutableStateOf("") }
    var authType by remember { mutableStateOf("API Key") }
    var authTypeExpanded by remember { mutableStateOf(false) }
    var apiVersion by remember { mutableStateOf("") }

    // Webhook specific fields
    var webhookUrl by remember { mutableStateOf("") }
    var selectedEvents by remember { mutableStateOf<List<String>>(emptyList()) }

    // Resource Scopes matrix
    var ordersScope by remember { mutableStateOf(PermissionScopeItemDto("Orders", read = true, create = true, update = true, delete = false)) }
    var productsScope by remember { mutableStateOf(PermissionScopeItemDto("Products", read = true, create = true, update = true, delete = false)) }
    var inventoryScope by remember { mutableStateOf(PermissionScopeItemDto("Inventory", read = true, create = true, update = true, delete = false)) }
    var customersScope by remember { mutableStateOf(PermissionScopeItemDto("Customers", read = true, create = true, update = true, delete = false)) }

    val isWebhook = category.equals("Webhook", ignoreCase = true)

    // Load integration for Edit Mode
    LaunchedEffect(integrationIdToEdit) {
        if (isEditMode) {
            viewModel.fetchApiIntegrationDetail(integrationIdToEdit)
        } else {
            viewModel.clearSelectedIntegrationDetail()
            // Default values for Create Mode
            name = ""
            category = "REST API"
            description = ""
            externalSystem = "Ecommerce"
            environment = "Production"
            baseApiUrl = ""
            authType = "API Key"
            apiVersion = "v1"
            webhookUrl = ""
            selectedEvents = emptyList()
        }
    }

    // Prefill form values once view-one data arrives
    LaunchedEffect(existingDetail) {
        existingDetail?.let { detail ->
            if (isEditMode) {
                name = detail.name
                category = detail.category.ifBlank { "REST API" }
                description = detail.description
                baseApiUrl = detail.baseUrl
                authType = detail.authType.ifBlank { "API Key" }
                webhookUrl = detail.webhookUrl.orEmpty()
                selectedEvents = detail.events

                detail.permissionsScope.find { it.resource.equals("Orders", true) }?.let { ordersScope = it }
                detail.permissionsScope.find { it.resource.equals("Products", true) }?.let { productsScope = it }
                detail.permissionsScope.find { it.resource.equals("Inventory", true) }?.let { inventoryScope = it }
                detail.permissionsScope.find { it.resource.equals("Customers", true) }?.let { customersScope = it }
            }
        }
    }

    val isBusy = isCreating || isUpdating

    Scaffold(
        containerColor = Primary_background,
        topBar = {
            TitleBar(
                title = if (isEditMode) "Edit API Integration" else "Add API Integration",
                subtitle = if (isEditMode) "ID: $integrationIdToEdit" else "Configure a system-to-system connection.",
                onClose = onNavigateBack
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = whiteBg,
                border = BorderStroke(1.dp, grey_border)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = tokens.screenPadding, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onNavigateBack,
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                        border = BorderStroke(1.dp, sectionBorder),
                        modifier = Modifier
                            .weight(1f)
                            .height(tokens.buttonHeight)
                    ) {
                        Text("Cancel", fontSize = tokens.bodySmall, color = TextSecondary)
                    }

                    Button(
                        onClick = {
                            val trimmedName = name.trim()
                            val trimmedBaseUrl = baseApiUrl.trim()
                            val trimmedWebhookUrl = webhookUrl.trim()

                            // Aligned validations from React code
                            if (trimmedName.length < 2) {
                                DynamicIslandManager.showError("Integration name must be at least 2 characters")
                                return@Button
                            }
                            if (trimmedName.length > 100) {
                                DynamicIslandManager.showError("Integration name can't exceed 100 characters")
                                return@Button
                            }
                            if (trimmedBaseUrl.isNotBlank() && !trimmedBaseUrl.startsWith("http://") && !trimmedBaseUrl.startsWith("https://")) {
                                DynamicIslandManager.showError("Enter a valid http(s) URL for Base API URL")
                                return@Button
                            }
                            if (isWebhook) {
                                if (trimmedWebhookUrl.isBlank()) {
                                    DynamicIslandManager.showError("Webhook URL is required for webhook integrations")
                                    return@Button
                                }
                                if (!trimmedWebhookUrl.startsWith("http://") && !trimmedWebhookUrl.startsWith("https://")) {
                                    DynamicIslandManager.showError("Enter a valid http(s) URL for Webhook URL")
                                    return@Button
                                }
                                if (selectedEvents.isEmpty()) {
                                    DynamicIslandManager.showError("Subscribe to at least one event")
                                    return@Button
                                }
                            }

                            // Request payload matching React submit logic
                            val request = CreateApiIntegrationRequest(
                                name = trimmedName,
                                description = description.trim(),
                                category = category,
                                authType = authType,
                                baseUrl = trimmedBaseUrl,
                                permissionsScope = listOf(
                                    ordersScope.copy(delete = false),
                                    productsScope.copy(delete = false),
                                    inventoryScope.copy(delete = false),
                                    customersScope.copy(delete = false)
                                )
                            )

                            if (isEditMode) {
                                viewModel.updateApiIntegration(
                                    integrationId = integrationIdToEdit,
                                    request = request,
                                    onSuccess = {
                                        DynamicIslandManager.showSuccess("Integration updated successfully")
                                        onSuccessSaved()
                                    },
                                    onError = { errorMsg ->
                                        DynamicIslandManager.showError(errorMsg)
                                    }
                                )
                            } else {
                                viewModel.createApiIntegration(
                                    request = request,
                                    onSuccess = {
                                        DynamicIslandManager.showSuccess("Integration created successfully")
                                        onSuccessSaved()
                                    },
                                    onError = { errorMsg ->
                                        DynamicIslandManager.showError(errorMsg)
                                    }
                                )
                            }
                        },
                        enabled = !isBusy && !isDetailLoading,
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary,
                            disabledContainerColor = disabled
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(tokens.buttonHeight)
                    ) {
                        if (isBusy) {
                            CirculerProgressIndicatorSmall()
                        } else {
                            Icon(Icons.Default.Check, contentDescription = null, tint = whiteBg, modifier = Modifier.size(tokens.iconSize))
                            Spacer(Modifier.width(6.dp))
                            Text(if (isEditMode) "Update " else "Save ", fontSize = tokens.bodySmall, color = whiteBg)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        if (isDetailLoading && isEditMode) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary, strokeWidth = 2.dp)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = tokens.screenPadding, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. BASIC INFORMATION
                IntegrationSectionCard(icon = Icons.Default.Tune, title = "Basic Information") {
                    FormLabel(text = "Integration Name", isRequired = true)
                    FormTextField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = "e.g. Ecommerce Platform"
                    )

                    Spacer(Modifier.height(10.dp))

                    FormDropdown(
                        label = "Type",
                        value = category,
                        expanded = categoryExpanded,
                        onExpandChange = { categoryExpanded = it },
                        options = CATEGORY_OPTIONS,
                        isRequired = true,
                        onOptionSelected = {
                            category = it
                            categoryExpanded = false
                        }
                    )

                    Spacer(Modifier.height(10.dp))

                    FormLabel(text = "Description")
                    FormTextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = "Brief description of this integration..."
                    )

                    Spacer(Modifier.height(10.dp))

                    FormDropdown(
                        label = "External System",
                        value = externalSystem,
                        expanded = externalSystemExpanded,
                        onExpandChange = { externalSystemExpanded = it },
                        options = EXTERNAL_SYSTEM_OPTIONS,
                        onOptionSelected = {
                            externalSystem = it
                            externalSystemExpanded = false
                        }
                    )

                    Spacer(Modifier.height(10.dp))

                    FormDropdown(
                        label = "Environment",
                        value = environment,
                        expanded = environmentExpanded,
                        onExpandChange = { environmentExpanded = it },
                        options = ENVIRONMENT_OPTIONS,
                        onOptionSelected = {
                            environment = it
                            environmentExpanded = false
                        }
                    )
                }

                // 2. API CONFIGURATION
                IntegrationSectionCard(icon = Icons.Default.VpnKey, title = "API Configuration") {
                    FormLabel(text = "Base API URL")
                    FormTextField(
                        value = baseApiUrl,
                        onValueChange = { baseApiUrl = it },
                        placeholder = "https://api.example.com/v1"
                    )

                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            FormDropdown(
                                label = "Authentication Method",
                                value = authType,
                                expanded = authTypeExpanded,
                                onExpandChange = { authTypeExpanded = it },
                                options = AUTH_TYPE_OPTIONS,
                                onOptionSelected = {
                                    authType = it
                                    authTypeExpanded = false
                                }
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel(text = "API Version")
                            FormTextField(
                                value = apiVersion,
                                onValueChange = { apiVersion = it },
                                placeholder = "e.g. v1"
                            )
                        }
                    }

                    // Conditional Webhook Section (Visible only when category == "Webhook")
                    if (isWebhook) {
                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider(color = grey_border)
                        Spacer(Modifier.height(12.dp))

                        FormLabel(text = "Webhook URL", isRequired = true)
                        FormTextField(
                            value = webhookUrl,
                            onValueChange = { webhookUrl = it },
                            placeholder = "https://example.com/webhooks"
                        )

                        Spacer(Modifier.height(12.dp))

                        FormLabel(text = "Subscribed Events", isRequired = true)
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            WEBHOOK_EVENTS.forEach { (eventId, eventLabel) ->
                                val isSelected = selectedEvents.contains(eventId)

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(tokens.cardCornerRadius))
                                        .background(if (isSelected) primary_light else whiteBg)
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) Primary else sectionBorder,
                                            shape = RoundedCornerShape(tokens.cardCornerRadius)
                                        )
                                        .clickable {
                                            selectedEvents = if (isSelected) {
                                                selectedEvents - eventId
                                            } else {
                                                selectedEvents + eventId
                                            }
                                        }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = eventLabel,
                                        fontSize = tokens.caption,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isSelected) Primary else TextSecondary
                                    )
                                }
                            }
                        }
                    }

                    if (!isEditMode) {
                        Spacer(Modifier.height(12.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                            color = primary_light,
                            border = BorderStroke(1.dp, light_blue_border)
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.Top) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "The API key and secret are generated automatically when you save. Use Reveal on the details page to view them.",
                                    fontSize = tokens.caption,
                                    color = Primary
                                )
                            }
                        }
                    }
                }

                // 3. ACCESS & SCOPE
                IntegrationSectionCard(
                    icon = Icons.Default.Security,
                    title = "Access & Scope",
                    subtitle = "Choose what this integration can do on each resource."
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("RESOURCE", fontSize = tokens.label, color = mutedText, modifier = Modifier.weight(1.5f))
                        Text("READ", fontSize = tokens.label, color = mutedText, modifier = Modifier.weight(1f))
                        Text("CREATE", fontSize = tokens.label, color = mutedText, modifier = Modifier.weight(1f))
                        Text("UPDATE", fontSize = tokens.label, color = mutedText, modifier = Modifier.weight(1f))
                    }

                    Spacer(Modifier.height(8.dp))

                    ScopeResourceRow(
                        icon = Icons.Default.ShoppingBag,
                        resourceName = "Orders",
                        scope = ordersScope,
                        onScopeChange = { ordersScope = it }
                    )

                    ScopeResourceRow(
                        icon = Icons.Default.Inventory2,
                        resourceName = "Products",
                        scope = productsScope,
                        onScopeChange = { productsScope = it }
                    )

                    ScopeResourceRow(
                        icon = Icons.Default.Category,
                        resourceName = "Inventory",
                        scope = inventoryScope,
                        onScopeChange = { inventoryScope = it }
                    )

                    ScopeResourceRow(
                        icon = Icons.Default.People,
                        resourceName = "Customers",
                        scope = customersScope,
                        onScopeChange = { customersScope = it }
                    )

                    Spacer(Modifier.height(10.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                        color = light_grey,
                        border = BorderStroke(1.dp, sectionBorder)
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.Top) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = mutedText, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Revoking any default scope may affect automatic background synchronization hooks.",
                                fontSize = tokens.caption,
                                color = headerGrey
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun IntegrationSectionCard(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val tokens = LocalAppTokens.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        color = whiteBg,
        border = BorderStroke(1.dp, sectionBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(primary_light),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(14.dp))
                }
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(title, fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = Primary)
                    if (subtitle != null) {
                        Text(subtitle, fontSize = tokens.caption, color = headerGrey)
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            content()
        }
    }
}

@Composable
private fun ScopeResourceRow(
    icon: ImageVector,
    resourceName: String,
    scope: PermissionScopeItemDto,
    onScopeChange: (PermissionScopeItemDto) -> Unit
) {
    val tokens = LocalAppTokens.current
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
        color = cardBgLight,
        border = BorderStroke(1.dp, grey_border)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(modifier = Modifier.weight(1.5f), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(primary_light),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(14.dp))
                }
                Spacer(Modifier.width(8.dp))
                Text(resourceName, fontSize = tokens.bodySmall, color = TextPrimary)
            }

            Box(modifier = Modifier.weight(1f)) {
                AppCheckbox(
                    checked = scope.read,
                    onCheckedChange = { onScopeChange(scope.copy(read = it)) }
                )
            }

            Box(modifier = Modifier.weight(1f)) {
                AppCheckbox(
                    checked = scope.create,
                    onCheckedChange = { onScopeChange(scope.copy(create = it)) }
                )
            }

            Box(modifier = Modifier.weight(1f)) {
                AppCheckbox(
                    checked = scope.update,
                    onCheckedChange = { onScopeChange(scope.copy(update = it)) }
                )
            }
        }
    }
}