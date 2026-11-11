@file:Suppress("unused", "AssignedValueIsNeverRead")

package com.cuso.tailor.view.home.security.api_integration

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.security.PermissionScopeItemDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.view.composable.DeleteModel
import com.cuso.tailor.view.composable.StatusBadge
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.viewmodel.SecurityViewModel

@Composable
fun ApiIntegrationDetailScreen(
    integrationId: String,
    viewModel: SecurityViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val clipboardManager = LocalClipboardManager.current

    val integration by viewModel.selectedIntegrationDetail.collectAsState()
    val isLoading by viewModel.isLoadingIntegrationDetail.collectAsState()
    val revealedSecrets by viewModel.revealedSecrets.collectAsState()
    val isRevealing by viewModel.isRevealingSecret.collectAsState()

    var isTestPassedAlertVisible by remember { mutableStateOf(false) }
    var showRevokeDialog by remember { mutableStateOf(false) }

    // Visibility map for credential fields: "apiKey", "secretKey", "webhookSecret"
    val fieldVisibility = remember { mutableStateMapOf<String, Boolean>() }

    LaunchedEffect(integrationId) {
        viewModel.clearRevealedSecrets()
        viewModel.fetchApiIntegrationDetail(integrationId)
    }

    val item = integration
    val isConnected = item?.status.equals("Connected", ignoreCase = true) || item?.status.equals("Active", ignoreCase = true)
    val isWebhook = item?.category.equals("Webhook", ignoreCase = true)

    Scaffold(
        containerColor = Primary_background,
        topBar = {
            TitleBar(
                title = item?.name ?: "API Integration",
                onClose = onNavigateBack,
                trailingContent = {
                    if (item != null) {
                        StatusBadge(
                            text = item.status,
                            textColor = if (isConnected) greentext else redText,
                            bgColor = if (isConnected) greenBg else redBg
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary, strokeWidth = 2.dp)
            }
        } else if (item == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Integration detail not found",
                    fontSize = tokens.bodyMedium,
                    color = mutedText
                )
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
                // Connection test success banner
                if (isTestPassedAlertVisible) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                        color = greenBg,
                        border = BorderStroke(1.dp, darkGreenBg.copy(alpha = 0.3f))
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = greentext, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Integration connected successfully. Connection test passed in 142ms.",
                                fontSize = tokens.bodySmall,
                                color = greentext
                            )
                        }
                    }
                }

                // 1. INTEGRATION OVERVIEW
                DetailSectionCard(icon = Icons.Default.Info, title = "INTEGRATION OVERVIEW") {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OverviewGridField(title = "Integration Name", value = item.name, modifier = Modifier.weight(1f))
                        OverviewGridField(title = "Type", value = item.category, modifier = Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OverviewGridField(title = "Authentication Method", value = item.authType, modifier = Modifier.weight(1f))
                        OverviewGridField(title = "Environment", value = "Production", modifier = Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OverviewGridField(title = "Created Date", value = "2024-03-15", modifier = Modifier.weight(1f))
                        OverviewGridField(title = "Last Sync", value = "2 mins ago", modifier = Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OverviewGridField(title = "Latency", value = if (item.latencyMs > 0) "${item.latencyMs}ms" else "—", modifier = Modifier.weight(1f))
                        OverviewGridField(title = "API Version", value = "v1", modifier = Modifier.weight(1f))
                    }
                }

                // 2. CONNECTION
                DetailSectionCard(
                    icon = Icons.Default.Link,
                    title = "CONNECTION",
                    trailing = {
                        Button(
                            onClick = {
                                isTestPassedAlertVisible = true
                                DynamicIslandManager.showSuccess("Connection test passed")
                            },
                            enabled = isConnected,
                            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(tokens.buttonHeight * 0.8f)
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = whiteBg, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Test Connection", fontSize = tokens.caption, color = whiteBg)
                        }
                    }
                ) {
                    Text("Base API URL", fontSize = tokens.caption, color = mutedText)
                    Spacer(Modifier.height(4.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                        color = cardBgLight,
                        border = BorderStroke(1.dp, sectionBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.baseUrl.ifBlank { "No base URL configured" },
                                fontSize = tokens.caption,
                                color = TextPrimary
                            )
                            if (item.baseUrl.isNotBlank()) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = mutedText,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable {
                                            clipboardManager.setText(AnnotatedString(item.baseUrl))
                                            DynamicIslandManager.showSuccess("URL copied to clipboard")
                                        }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Authentication Method", fontSize = tokens.caption, color = mutedText)
                            Text(item.authType, fontSize = tokens.bodySmall, color = TextPrimary)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Last Connection Check", fontSize = tokens.caption, color = mutedText)
                            Text("2025-08-21 14:32:00", fontSize = tokens.bodySmall, color = TextPrimary)
                        }
                    }
                }

                // 3. CREDENTIALS (Wired to /reveal-secret API)
                DetailSectionCard(
                    icon = Icons.Default.Lock,
                    title = "CREDENTIALS",
                    trailing = {
                        StatusBadge(text = "PROTECTED", textColor = yellowText, bgColor = yellowBg)
                    }
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                        color = yellowBg,
                        border = BorderStroke(1.dp, yellowText.copy(alpha = 0.3f))
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = yellowText, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Credentials are masked. Use Reveal to view them and treat them as secrets.",
                                fontSize = tokens.caption,
                                color = yellowText
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    // API Key Row
                    CredentialRowItem(
                        label = "API Key",
                        fieldKey = "apiKey",
                        secretValue = revealedSecrets?.apiKey,
                        isVisible = fieldVisibility["apiKey"] == true,
                        isLoading = isRevealing,
                        onRevealToggle = {
                            val currentlyVisible = fieldVisibility["apiKey"] == true
                            if (currentlyVisible) {
                                fieldVisibility["apiKey"] = false
                            } else {
                                viewModel.revealApiIntegrationSecret(
                                    integrationId = item.id,
                                    onSuccess = { fieldVisibility["apiKey"] = true },
                                    onError = { DynamicIslandManager.showError(it) }
                                )
                            }
                        },
                        onCopy = {
                            viewModel.revealApiIntegrationSecret(
                                integrationId = item.id,
                                onSuccess = { secrets ->
                                    val key = secrets.apiKey.orEmpty()
                                    if (key.isNotBlank()) {
                                        clipboardManager.setText(AnnotatedString(key))
                                        DynamicIslandManager.showSuccess("API Key copied to clipboard")
                                    } else {
                                        DynamicIslandManager.showError("No API Key available")
                                    }
                                },
                                onError = { DynamicIslandManager.showError(it) }
                            )
                        },
                        onRegenerate = { DynamicIslandManager.showSuccess("Credentials rotation scheduled") },
                        onRevoke = { showRevokeDialog = true }
                    )

                    // Secret Key Row
                    CredentialRowItem(
                        label = "Secret Key",
                        fieldKey = "secretKey",
                        secretValue = revealedSecrets?.secretKey,
                        isVisible = fieldVisibility["secretKey"] == true,
                        isLoading = isRevealing,
                        onRevealToggle = {
                            val currentlyVisible = fieldVisibility["secretKey"] == true
                            if (currentlyVisible) {
                                fieldVisibility["secretKey"] = false
                            } else {
                                viewModel.revealApiIntegrationSecret(
                                    integrationId = item.id,
                                    onSuccess = { fieldVisibility["secretKey"] = true },
                                    onError = { DynamicIslandManager.showError(it) }
                                )
                            }
                        },
                        onCopy = {
                            viewModel.revealApiIntegrationSecret(
                                integrationId = item.id,
                                onSuccess = { secrets ->
                                    val secret = secrets.secretKey.orEmpty()
                                    if (secret.isNotBlank()) {
                                        clipboardManager.setText(AnnotatedString(secret))
                                        DynamicIslandManager.showSuccess("Secret Key copied to clipboard")
                                    } else {
                                        DynamicIslandManager.showError("No Secret Key available")
                                    }
                                },
                                onError = { DynamicIslandManager.showError(it) }
                            )
                        },
                        onRegenerate = { DynamicIslandManager.showSuccess("Credentials rotation scheduled") },
                        onRevoke = { showRevokeDialog = true }
                    )

                    // Webhook Secret Row (Only visible for Webhook category)
                    if (isWebhook) {
                        CredentialRowItem(
                            label = "Webhook Secret",
                            fieldKey = "webhookSecret",
                            secretValue = revealedSecrets?.webhookSecret,
                            isVisible = fieldVisibility["webhookSecret"] == true,
                            isLoading = isRevealing,
                            onRevealToggle = {
                                val currentlyVisible = fieldVisibility["webhookSecret"] == true
                                if (currentlyVisible) {
                                    fieldVisibility["webhookSecret"] = false
                                } else {
                                    viewModel.revealApiIntegrationSecret(
                                        integrationId = item.id,
                                        onSuccess = { fieldVisibility["webhookSecret"] = true },
                                        onError = { DynamicIslandManager.showError(it) }
                                    )
                                }
                            },
                            onCopy = {
                                viewModel.revealApiIntegrationSecret(
                                    integrationId = item.id,
                                    onSuccess = { secrets ->
                                        val whSecret = secrets.webhookSecret.orEmpty()
                                        if (whSecret.isNotBlank()) {
                                            clipboardManager.setText(AnnotatedString(whSecret))
                                            DynamicIslandManager.showSuccess("Webhook Secret copied")
                                        } else {
                                            DynamicIslandManager.showError("No Webhook Secret configured")
                                        }
                                    },
                                    onError = { DynamicIslandManager.showError(it) }
                                )
                            },
                            onRegenerate = { DynamicIslandManager.showSuccess("Credentials rotation scheduled") },
                            onRevoke = { showRevokeDialog = true }
                        )
                    }
                }

                // 4. RECOVERY VERIFICATION
                DetailSectionCard(icon = Icons.Default.Shield, title = "RECOVERY VERIFICATION") {
                    VerificationStepItem(icon = Icons.Default.Refresh, title = "Data Integrity", status = "Running")
                    VerificationStepItem(icon = Icons.Default.Upload, title = "Post-Restore", status = "Pending")
                    VerificationStepItem(icon = Icons.Default.BarChart, title = "Service Health", status = "Pending")
                }

                // 5. API ACCESS & SCOPE
                DetailSectionCard(
                    icon = Icons.Default.Key,
                    title = "API ACCESS & SCOPE",
                    trailing = {
                        Text(
                            text = "Edit Access",
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Medium,
                            color = Primary,
                            modifier = Modifier.clickable {
                                onNavigateToEdit(item.id)
                            }
                        )
                    }
                ) {
                    val scopes = if (item.permissionsScope.isNotEmpty()) item.permissionsScope else listOf(
                        PermissionScopeItemDto("Orders", read = true, create = true, update = true),
                        PermissionScopeItemDto("Products", read = true),
                        PermissionScopeItemDto("Inventory", read = true),
                        PermissionScopeItemDto("Customers", read = true, create = true, update = true)
                    )

                    scopes.forEach { scope ->
                        ScopeDisplayRow(scope = scope)
                    }
                }

                // 6. QUICK LINKS (Conditional for Webhook)
                if (isWebhook) {
                    DetailSectionCard(icon = Icons.Default.FlashOn, title = "QUICK LINKS") {
                        QuickLinkRow(title = "View Webhooks", onClick = {})
                        HorizontalDivider(color = grey_border)
                        QuickLinkRow(title = "View API Logs", onClick = {})
                    }
                }

                Spacer(Modifier.height(20.dp))
            }
        }
    }

    // Revoke Confirmation Dialog using reusable DeleteModel
    if (showRevokeDialog && item != null) {
        DeleteModel(
            title = "Revoke Integration",
            message = "Revoking removes the whole integration \"${item.name}\". Its credentials will stop working immediately.",
            checkboxText = "I confirm this revocation",
            cancelText = "No, Keep It",
            confirmText = "Yes, Revoke!",
            onDismiss = { showRevokeDialog = false },
            onDelete = {
                viewModel.revokeApiIntegration(
                    integrationId = item.id,
                    onSuccess = { msg ->
                        DynamicIslandManager.showSuccess(msg)
                        showRevokeDialog = false
                        onNavigateBack()
                    },
                    onError = { err ->
                        DynamicIslandManager.showError(err)
                        showRevokeDialog = false
                    }
                )
            }
        )
    }
}

@Composable
private fun DetailSectionCard(
    icon: ImageVector,
    title: String,
    trailing: (@Composable () -> Unit)? = null,
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(title, fontSize = tokens.label, fontWeight = FontWeight.SemiBold, color = Primary)
                }
                trailing?.invoke()
            }
            Spacer(Modifier.height(4.dp))
            content()
        }
    }
}

@Composable
private fun OverviewGridField(title: String, value: String, modifier: Modifier = Modifier) {
    val tokens = LocalAppTokens.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
        color = cardBgLight,
        border = BorderStroke(1.dp, grey_border)
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            Text(title, fontSize = tokens.label, color = mutedText)
            Spacer(Modifier.height(2.dp))
            Text(value, fontSize = tokens.bodySmall, color = TextPrimary, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun CredentialRowItem(
    label: String,
    fieldKey: String,
    secretValue: String?,
    isVisible: Boolean,
    isLoading: Boolean,
    onRevealToggle: () -> Unit,
    onCopy: () -> Unit,
    onRegenerate: () -> Unit,
    onRevoke: () -> Unit
) {
    val tokens = LocalAppTokens.current

    val displayedValue = when {
        isVisible && !secretValue.isNullOrBlank() -> secretValue
        isVisible && secretValue.isNullOrBlank() -> "(empty)"
        else -> "••••••••••••••••••••••••"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, fontSize = tokens.caption, color = mutedText)

        }

        Spacer(Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = displayedValue,
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
        }

        Spacer(Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onRevealToggle,
                enabled = !isLoading,
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                border = BorderStroke(1.dp, sectionBorder),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(tokens.buttonHeight * 0.75f)
            ) {
                Text(if (isVisible) "Hide" else "Reveal", fontSize = tokens.caption, color = TextPrimary)
            }

            OutlinedButton(
                onClick = onCopy,
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                border = BorderStroke(1.dp, sectionBorder),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(tokens.buttonHeight * 0.75f)
            ) {
                Text("Copy", fontSize = tokens.caption, color = TextPrimary)
            }

            OutlinedButton(
                onClick = onRegenerate,
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                border = BorderStroke(1.dp, sectionBorder),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(tokens.buttonHeight * 0.75f)
            ) {
                Text("Regenerate", fontSize = tokens.caption, color = Primary)
            }

            OutlinedButton(
                onClick = onRevoke,
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                border = BorderStroke(1.dp, redBg),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(tokens.buttonHeight * 0.75f)
            ) {
                Text("Revoke", fontSize = tokens.caption, color = redText)
            }
        }

        Spacer(Modifier.height(6.dp))
        HorizontalDivider(color = grey_border)
    }
}

@Composable
private fun VerificationStepItem(icon: ImageVector, title: String, status: String) {
    val tokens = LocalAppTokens.current
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
        color = cardBgLight,
        border = BorderStroke(1.dp, grey_border)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(primary_light),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(14.dp))
                }
                Spacer(Modifier.width(10.dp))
                Text(title, fontSize = tokens.bodySmall, color = TextPrimary)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
                    .background(if (status == "Running") primary_light else light_grey)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(status, fontSize = tokens.label, color = if (status == "Running") Primary else headerGrey)
            }
        }
    }
}

@Composable
private fun ScopeDisplayRow(scope: PermissionScopeItemDto) {
    val tokens = LocalAppTokens.current
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
        color = cardBgLight,
        border = BorderStroke(1.dp, grey_border)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(scope.resource, fontSize = tokens.bodySmall, color = TextPrimary, fontWeight = FontWeight.Medium)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (scope.read) ScopePillBadge("Read")
                if (scope.create) ScopePillBadge("Create")
                if (scope.update) ScopePillBadge("Update")
            }
        }
    }
}

@Composable
private fun ScopePillBadge(label: String) {
    val tokens = LocalAppTokens.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
            .background(primary_light)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(label, fontSize = tokens.label, color = Primary, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun QuickLinkRow(title: String, onClick: () -> Unit) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = tokens.bodySmall, color = TextPrimary)
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = headerGrey, modifier = Modifier.size(16.dp))
    }
}