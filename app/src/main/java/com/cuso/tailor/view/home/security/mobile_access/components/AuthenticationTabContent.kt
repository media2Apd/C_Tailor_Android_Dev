package com.cuso.tailor.view.home.security.mobile_access.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.security.*
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.SecurityViewModel

@Composable
fun AuthenticationTabContent(viewModel: SecurityViewModel) {
    val tokens = LocalAppTokens.current
    val authPolicy by viewModel.mobileAuthPolicy.collectAsState()
    var selectedAuthSubTab by remember { mutableIntStateOf(0) }

    val authMethodsTabs = remember {
        listOf(
            TabItem(label = "Password", icon = Icons.Default.Lock),
            TabItem(label = "OTP", icon = Icons.Default.VpnKey),
            TabItem(label = "2FA", icon = Icons.Default.Shield),
            TabItem(label = "Biometrics", icon = Icons.Default.Fingerprint)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(tokens.screenPadding),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("AUTH METHODS", fontSize = tokens.caption, fontWeight = FontWeight.Bold, color = Primary)

        SettingsTabs(
            tabs = authMethodsTabs,
            selectedIndex = selectedAuthSubTab,
            onTabSelected = { selectedAuthSubTab = it },
            height = 36.dp
        )

        when (selectedAuthSubTab) {
            0 -> PasswordPolicyForm(authPolicy.password) { viewModel.savePasswordPolicy(it) }
            1 -> OtpPolicyForm(authPolicy.otp) { viewModel.saveOtpPolicy(it) }
            2 -> TwoFAPolicyForm(authPolicy.twoFA) { viewModel.saveTwoFAPolicy(it) }
            3 -> BiometricPolicyForm(authPolicy.biometric) { viewModel.saveBiometricPolicy(it) }
        }
    }
}

@Composable
private fun PasswordPolicyForm(policy: PasswordPolicy, onSave: (PasswordPolicy) -> Unit) {
    val tokens = LocalAppTokens.current
    var enabled by remember(policy) { mutableStateOf(policy.enabled) }
    var minStrength by remember(policy) { mutableStateOf(policy.minStrength) }
    var minLength by remember(policy) { mutableStateOf(policy.minLength.toString()) }
    var expiryDays by remember(policy) { mutableStateOf(policy.expiryDays.toString()) }
    var failedAttemptLimit by remember(policy) { mutableStateOf(policy.failedAttemptLimit.toString()) }
    var lockDuration by remember(policy) { mutableStateOf(policy.lockDurationMinutes.toString()) }

    var strengthExpanded by remember { mutableStateOf(false) }
    var expiryExpanded by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        color = whiteBg,
        border = BorderStroke(1.dp, grey_border)
    ) {
        Column(modifier = Modifier.padding(tokens.screenPadding), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Password Login", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Allow users to authenticate via username and password on mobile devices.", fontSize = tokens.caption, color = TextSecondary)
                }
                MiniSwitch(checked = enabled, onCheckedChange = { enabled = it })
            }

            HorizontalDivider(color = grey_border)
            Text("REQUIREMENTS", fontSize = tokens.caption, fontWeight = FontWeight.Bold, color = Primary)

            FormDropdown(
                label = "Minimum Password Strength",
                value = when (minStrength) {
                    "weak" -> "Weak (Any characters)"
                    "medium" -> "Medium (Alphanumeric)"
                    else -> "Strong (Alphanumeric + Special)"
                },
                expanded = strengthExpanded,
                onExpandChange = { strengthExpanded = it },
                options = listOf("Weak (Any characters)", "Medium (Alphanumeric)", "Strong (Alphanumeric + Special)"),
                onOptionSelected = {
                    minStrength = when {
                        it.startsWith("Weak") -> "weak"
                        it.startsWith("Medium") -> "medium"
                        else -> "strong"
                    }
                }
            )

            FormLabel("Minimum Length (characters)")
            FormTextField(value = minLength, onValueChange = { minLength = it.filter { char -> char.isDigit() } }, keyboardType = KeyboardType.Number)

            Text("LIFECYCLE & LIMITS", fontSize = tokens.caption, fontWeight = FontWeight.Bold, color = Primary)

            FormDropdown(
                label = "Password Expiry",
                value = "$expiryDays days",
                expanded = expiryExpanded,
                onExpandChange = { expiryExpanded = it },
                options = listOf("30 days", "60 days", "90 days", "180 days", "0 days (Never)"),
                onOptionSelected = { expiryDays = it.split(" ").first() }
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    FormLabel("Failed Attempt Limit")
                    FormTextField(value = failedAttemptLimit, onValueChange = { failedAttemptLimit = it.filter { c -> c.isDigit() } }, keyboardType = KeyboardType.Number)
                }
                Column(modifier = Modifier.weight(1f)) {
                    FormLabel("Account Lock Duration (min)")
                    FormTextField(value = lockDuration, onValueChange = { lockDuration = it.filter { c -> c.isDigit() } }, keyboardType = KeyboardType.Number)
                }
            }

            Button(
                onClick = {
                    onSave(
                        policy.copy(
                            enabled = enabled,
                            minStrength = minStrength,
                            minLength = minLength.toIntOrNull() ?: 8,
                            expiryDays = expiryDays.toIntOrNull() ?: 90,
                            failedAttemptLimit = failedAttemptLimit.toIntOrNull() ?: 5,
                            lockDurationMinutes = lockDuration.toIntOrNull() ?: 15
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().height(tokens.buttonHeight)
            ) {
                Text("Save Changes", color = whiteBg, fontSize = tokens.bodySmall)
            }
        }
    }
}

@Composable
private fun OtpPolicyForm(policy: OtpPolicy, onSave: (OtpPolicy) -> Unit) {
    val tokens = LocalAppTokens.current
    var enabled by remember(policy) { mutableStateOf(policy.enabled) }
    var channel by remember(policy) { mutableStateOf(policy.channel) }
    var expirySeconds by remember(policy) { mutableStateOf(policy.expirySeconds.toString()) }
    var maxAttempts by remember(policy) { mutableStateOf(policy.maxAttempts.toString()) }
    var resendLimit by remember(policy) { mutableStateOf(policy.resendLimit.toString()) }
    var resendCooldown by remember(policy) { mutableStateOf(policy.resendCooldownSeconds.toString()) }

    var channelExpanded by remember { mutableStateOf(false) }
    var expiryExpanded by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        color = whiteBg,
        border = BorderStroke(1.dp, grey_border)
    ) {
        Column(modifier = Modifier.padding(tokens.screenPadding), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Mobile OTP Authentication", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Allow users to authenticate using a one-time password.", fontSize = tokens.caption, color = TextSecondary)
                }
                MiniSwitch(checked = enabled, onCheckedChange = { enabled = it })
            }

            HorizontalDivider(color = grey_border)
            Text("DELIVERY & EXPIRY", fontSize = tokens.caption, fontWeight = FontWeight.Bold, color = Primary)

            FormDropdown(
                label = "OTP Channel",
                value = when (channel) {
                    "sms" -> "SMS"
                    "email" -> "Email"
                    else -> "SMS + Email"
                },
                expanded = channelExpanded,
                onExpandChange = { channelExpanded = it },
                options = listOf("SMS", "Email", "SMS + Email"),
                onOptionSelected = {
                    channel = when (it) {
                        "SMS" -> "sms"
                        "Email" -> "email"
                        else -> "sms_email"
                    }
                }
            )

            FormDropdown(
                label = "OTP Expiry",
                value = "$expirySeconds seconds",
                expanded = expiryExpanded,
                onExpandChange = { expiryExpanded = it },
                options = listOf("30 seconds", "60 seconds", "120 seconds", "300 seconds"),
                onOptionSelected = { expirySeconds = it.split(" ").first() }
            )

            Text("ATTEMPTS & LIMITS", fontSize = tokens.caption, fontWeight = FontWeight.Bold, color = Primary)
            FormLabel("Maximum Attempts")
            FormTextField(value = maxAttempts, onValueChange = { maxAttempts = it.filter { c -> c.isDigit() } }, keyboardType = KeyboardType.Number)

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    FormLabel("Resend Limit")
                    FormTextField(value = resendLimit, onValueChange = { resendLimit = it.filter { c -> c.isDigit() } }, keyboardType = KeyboardType.Number)
                }
                Column(modifier = Modifier.weight(1f)) {
                    FormLabel("Resend Cooldown (sec)")
                    FormTextField(value = resendCooldown, onValueChange = { resendCooldown = it.filter { c -> c.isDigit() } }, keyboardType = KeyboardType.Number)
                }
            }

            Button(
                onClick = {
                    onSave(
                        policy.copy(
                            enabled = enabled,
                            channel = channel,
                            expirySeconds = expirySeconds.toIntOrNull() ?: 60,
                            maxAttempts = maxAttempts.toIntOrNull() ?: 5,
                            resendLimit = resendLimit.toIntOrNull() ?: 3,
                            resendCooldownSeconds = resendCooldown.toIntOrNull() ?: 30
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().height(tokens.buttonHeight)
            ) {
                Text("Save Changes", color = whiteBg, fontSize = tokens.bodySmall)
            }
        }
    }
}

@Composable
private fun TwoFAPolicyForm(policy: TwoFAPolicy, onSave: (TwoFAPolicy) -> Unit) {
    val tokens = LocalAppTokens.current
    var enabled by remember(policy) { mutableStateOf(policy.enabled) }
    var methods by remember(policy) { mutableStateOf(policy.methods) }
    var enforcement by remember(policy) { mutableStateOf(policy.enforcement) }
    var sensitiveOps by remember(policy) { mutableStateOf(policy.requireForSensitiveOps) }
    var enforceExpanded by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        color = whiteBg,
        border = BorderStroke(1.dp, grey_border)
    ) {
        Column(modifier = Modifier.padding(tokens.screenPadding), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Two-Factor Authentication", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Add an additional verification step when users access mobile ERP.", fontSize = tokens.caption, color = TextSecondary)
                }
                MiniSwitch(checked = enabled, onCheckedChange = { enabled = it })
            }

            HorizontalDivider(color = grey_border)
            Text("2FA METHOD", fontSize = tokens.caption, fontWeight = FontWeight.Bold, color = Primary)

            val methodList = listOf(
                Triple("otp", "OTP", "One-time verification code"),
                Triple("authenticator", "Authenticator", "Code generated by an authenticator application"),
                Triple("biometric", "Biometric", "Device-native biometric verification where supported")
            )

            methodList.forEach { (id, label, desc) ->
                val selected = methods.contains(id)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, if (selected) Primary else grey_border, RoundedCornerShape(8.dp))
                        .background(if (selected) primary_light.copy(alpha = 0.5f) else whiteBg)
                        .clickable { methods = if (selected) methods - id else methods + id }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Checkbox(checked = selected, onCheckedChange = { methods = if (selected) methods - id else methods + id })
                    Column {
                        Text(label, fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(desc, fontSize = tokens.caption, color = TextSecondary)
                    }
                }
            }

            Text("ENFORCEMENT", fontSize = tokens.caption, fontWeight = FontWeight.Bold, color = Primary)
            FormDropdown(
                label = "2FA Enforcement",
                value = enforcement.replaceFirstChar { it.uppercase() },
                expanded = enforceExpanded,
                onExpandChange = { enforceExpanded = it },
                options = listOf("Required", "Optional"),
                onOptionSelected = { enforcement = it.lowercase() }
            )

            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).border(1.dp, grey_border, RoundedCornerShape(8.dp)).padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Require 2FA for sensitive operations", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary)
                    Text("Additional verification required before sensitive mobile tasks.", fontSize = tokens.caption, color = TextSecondary)
                }
                MiniSwitch(checked = sensitiveOps, onCheckedChange = { sensitiveOps = it })
            }

            Button(
                onClick = {
                    onSave(policy.copy(enabled = enabled, methods = methods, enforcement = enforcement, requireForSensitiveOps = sensitiveOps))
                },
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().height(tokens.buttonHeight)
            ) {
                Text("Save Changes", color = whiteBg, fontSize = tokens.bodySmall)
            }
        }
    }
}

@Composable
private fun BiometricPolicyForm(policy: BiometricPolicy, onSave: (BiometricPolicy) -> Unit) {
    val tokens = LocalAppTokens.current
    var enabled by remember(policy) { mutableStateOf(policy.enabled) }
    var requirePasscode by remember(policy) { mutableStateOf(policy.requirePasscodeBackup) }

    Surface(
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        color = whiteBg,
        border = BorderStroke(1.dp, grey_border)
    ) {
        Column(modifier = Modifier.padding(tokens.screenPadding), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Biometric Authentication", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Allow users to authenticate using secure biometric capabilities.", fontSize = tokens.caption, color = TextSecondary)
                }
                MiniSwitch(checked = enabled, onCheckedChange = { enabled = it })
            }

            HorizontalDivider(color = grey_border)
            Text("SUPPORTED METHODS", fontSize = tokens.caption, fontWeight = FontWeight.Bold, color = Primary)

            listOf("Fingerprint", "Face Authentication", "Device Biometrics").forEach { method ->
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(light_grey).padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(method, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary)
                    Text("SUPPORTED", fontSize = tokens.label, color = Primary, fontWeight = FontWeight.Bold)
                }
            }

            Text("SECURITY OPTIONS", fontSize = tokens.caption, fontWeight = FontWeight.Bold, color = Primary)
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).border(1.dp, grey_border, RoundedCornerShape(8.dp)).padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Require Device Passcode Backup", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary)
                    Text("Require secure fallback authentication when biometric is unavailable.", fontSize = tokens.caption, color = TextSecondary)
                }
                MiniSwitch(checked = requirePasscode, onCheckedChange = { requirePasscode = it })
            }

            Button(
                onClick = { onSave(policy.copy(enabled = enabled, requirePasscodeBackup = requirePasscode)) },
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().height(tokens.buttonHeight)
            ) {
                Text("Save Changes", color = whiteBg, fontSize = tokens.bodySmall)
            }
        }
    }
}