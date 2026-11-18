@file:Suppress("unused", "AssignedValueIsNeverRead")

package com.cuso.tailor.view.composable

import android.app.Activity
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.outlined.GppMaybe
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.cuso.tailor.R
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.PrimaryBorder
import com.cuso.tailor.ui.theme.PrimaryTextColor
import com.cuso.tailor.ui.theme.TextPrimary
import com.cuso.tailor.ui.theme.TextSecondary
import com.cuso.tailor.ui.theme.blackTitle
import com.cuso.tailor.ui.theme.greentext
import com.cuso.tailor.ui.theme.light_blue_border
import com.cuso.tailor.ui.theme.light_grey
import com.cuso.tailor.ui.theme.mutedText
import com.cuso.tailor.ui.theme.primary_light
import com.cuso.tailor.ui.theme.redBg
import com.cuso.tailor.ui.theme.redText
import com.cuso.tailor.ui.theme.sectionBorder
import com.cuso.tailor.ui.theme.title_color
import com.cuso.tailor.ui.theme.whiteBg
import com.cuso.tailor.ui.theme.yellowBg
import com.cuso.tailor.ui.theme.yellowText
import com.cuso.tailor.viewmodel.Authenticate
import com.cuso.tailor.viewmodel.PermissionViewModel
import com.cuso.tailor.viewmodel.UiState

@Composable
fun CardContentsLoginScreen(
    navController: NavController,
    activity: Activity,
    authViewModel: Authenticate,
    prefilledEmail: String = "",
    permissionViewModel: PermissionViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val focusManager = LocalFocusManager.current

    val accountState by authViewModel.accountState.collectAsState()
    var email by rememberSaveable { mutableStateOf(prefilledEmail) }
    var password by remember { mutableStateOf("") }
    var submittedEmail by rememberSaveable { mutableStateOf(prefilledEmail) }
    var isSubmitted by remember { mutableStateOf(prefilledEmail.isNotBlank()) }
    var showEmailNotFound by remember { mutableStateOf(false) }

    // Two-factor authentication state
    var isTwoFactorStep by rememberSaveable { mutableStateOf(false) }
    var twoFactorChallengeToken by rememberSaveable { mutableStateOf("") }
    var twoFactorPrompt by rememberSaveable {
        mutableStateOf("Enter the 6-digit code from your authenticator app.")
    }
    var twoFactorCode by rememberSaveable { mutableStateOf("") }

    // Temporary password change state
    var isMustChangePasswordStep by rememberSaveable { mutableStateOf(false) }
    var changePasswordChallengeToken by rememberSaveable { mutableStateOf<String?>(null) }
    var changePasswordSubtitle by rememberSaveable {
        mutableStateOf("Your administrator gave you a temporary password. Choose a new one to continue.")
    }
    var temporaryPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }
    var tempPasswordError by remember { mutableStateOf<String?>(null) }
    var newPasswordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    // Password rule validation flags
    val hasValidLength = newPassword.length in 8..64
    val hasUppercase = newPassword.any { it.isUpperCase() }
    val hasLowercase = newPassword.any { it.isLowerCase() }
    val hasNumber = newPassword.any { it.isDigit() }
    val hasSymbol = newPassword.any { !it.isLetterOrDigit() }
    val allPasswordRulesMet = hasValidLength && hasUppercase && hasLowercase && hasNumber && hasSymbol

    // Local validation and OTP error message state
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val lockedMessage = (accountState as? UiState.WorkspaceLocked)?.message
    val isError = accountState is UiState.Error ||
            accountState is UiState.EmailNotFound ||
            accountState is UiState.WorkspaceLocked ||
            !errorMessage.isNullOrBlank()
    val errorMsg = (accountState as? UiState.Error)?.message?.takeIf { it.isNotBlank() }
        ?: lockedMessage
        ?: errorMessage

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                focusManager.clearFocus()
            }
            .padding(tokens.screenPadding)
    ) {

        if (!lockedMessage.isNullOrBlank()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.6f),
                color = redBg,
                border = BorderStroke(1.dp, redText.copy(alpha = 0.25f))
            ) {
                Text(
                    text = lockedMessage,
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = redText,
                    modifier = Modifier.padding(tokens.extraPadding * 1.2f)
                )
            }
            Spacer(Modifier.height(tokens.screenPadding))
        }

        if (isMustChangePasswordStep) {
            SetNewPasswordSection(
                subtitle = changePasswordSubtitle,
                temporaryPassword = temporaryPassword,
                onTemporaryPasswordChange = {
                    temporaryPassword = it
                    tempPasswordError = null
                    errorMessage = null
                    authViewModel.resetState()
                },
                newPassword = newPassword,
                onNewPasswordChange = {
                    newPassword = it
                    newPasswordError = null
                    confirmPasswordError = null
                    errorMessage = null
                    authViewModel.resetState()
                },
                confirmNewPassword = confirmNewPassword,
                onConfirmNewPasswordChange = {
                    confirmNewPassword = it
                    confirmPasswordError = null
                    errorMessage = null
                    authViewModel.resetState()
                },
                tempPasswordError = tempPasswordError,
                newPasswordError = newPasswordError,
                confirmPasswordError = confirmPasswordError ?: errorMsg,
                hasValidLength = hasValidLength,
                hasUppercase = hasUppercase,
                hasLowercase = hasLowercase,
                hasNumber = hasNumber,
                hasSymbol = hasSymbol,
                enabled = accountState !is UiState.Loading
            )
        } else if (isTwoFactorStep) {
            // Two-Step Verification Info Banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.6f),
                color = primary_light,
                border = BorderStroke(1.dp, light_blue_border)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(tokens.extraPadding * 1.4f),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Shield,
                        contentDescription = "Two-step verification",
                        tint = Primary,
                        modifier = Modifier.size(tokens.iconSize)
                    )
                    Spacer(Modifier.width(tokens.extraPadding))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Two-step verification",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = title_color
                        )
                        Spacer(Modifier.height(tokens.extraPadding * 0.3f))
                        Text(
                            text = twoFactorPrompt,
                            fontSize = tokens.caption,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(Modifier.height(tokens.screenPadding))

            // Verification Code Label & Field using reusable FormComponents
            FormLabel(text = "Verification code")

            FormTextField(
                value = twoFactorCode,
                onValueChange = { input ->
                    twoFactorCode = input
                    authViewModel.resetState()
                    errorMessage = null
                },
                // Supports both 6-digit authenticator codes and alphanumeric backup codes
                keyboardType = KeyboardType.Text,
                placeholder = "123456",
                isError = isError,
                errorMessage = errorMsg,
                enabled = accountState !is UiState.Loading,
                borderColor = if (isError) redText else Primary,
                textColor = TextPrimary,
                placeholderColor = mutedText,
                containerColor = whiteBg
            )

            Spacer(Modifier.height(tokens.extraPadding * 0.6f))

            Text(
                text = "Lost your device? Enter one of your backup codes.",
                fontSize = tokens.caption,
                color = mutedText
            )
        } else if (!isSubmitted) {
            CusoTextField(
                value = email,
                onValueChange = {
                    email = it
                    authViewModel.resetState()
                    showEmailNotFound = false
                    errorMessage = null
                },
                leadingIconPainter = painterResource(R.drawable.ic_mail),
                label = "Email",
                placeholder = "your@email.com",
                isError = isError,
                errorText = errorMsg,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            LaunchedEffect(showEmailNotFound) {
                if (showEmailNotFound) {
                    navController.navigate("org-not-found")
                    authViewModel.resetState()
                }
            }
        } else {
            // User identity box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(tokens.imageDp)
                    .background(whiteBg)
                    .border(
                        1.dp,
                        PrimaryBorder,
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.35f)
                    )
                    .padding(horizontal = tokens.cardPadding * 0.6f)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(tokens.iconSize * 1.8f)
                            .clip(CircleShape)
                            .background(light_grey),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.PersonOutline,
                            contentDescription = null,
                            tint = PrimaryTextColor,
                            modifier = Modifier.size(tokens.iconSize)
                        )
                    }
                    Spacer(modifier = Modifier.width(tokens.extraPadding))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Sign in as",
                            fontSize = tokens.caption,
                            color = PrimaryTextColor
                        )
                        Text(
                            text = submittedEmail,
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = blackTitle,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = "Change",
                        modifier = Modifier
                            .clickable { navController.navigate("login") }
                            .padding(tokens.extraPadding * 0.8f),
                        color = Primary,
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(tokens.screenPadding))

            CusoTextField(
                value = password,
                onValueChange = {
                    password = it
                    authViewModel.resetState()
                    errorMessage = null
                },
                label = "Password",
                placeholder = "Enter password",
                leadingIconPainter = painterResource(R.drawable.ic_lock),
                isPassword = true,
                isError = isError,
                errorText = errorMsg,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            Spacer(Modifier.height(tokens.extraPadding * 0.8f))

            // Row containing Sign In with OTP (Left) and Forgot Password (Right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sign In with OTP",
                    modifier = Modifier
                        .clickable {
                            if (email.isNotBlank()) {
                                val formattedEmail = email.trim()
                                authViewModel.sendOtp(
                                    email = formattedEmail,
                                    onSuccess = {
                                        navController.navigate("login-otp/${Uri.encode(formattedEmail)}")
                                    },
                                    onError = { error ->
                                        errorMessage = error
                                    }
                                )
                            } else {
                                errorMessage = "Please enter your email"
                            }
                        }
                        .padding(vertical = tokens.extraPadding * 0.8f),
                    color = Primary,
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = "Forgot Password?",
                    modifier = Modifier
                        .clickable { navController.navigate("new-pass/${email.trim()}") }
                        .padding(vertical = tokens.extraPadding * 0.8f),
                    color = Primary,
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        LaunchedEffect(accountState) {
            Log.d("LOGIN_STATE", "accountState = $accountState")
            when (val state = accountState) {
                is UiState.EmailVerified -> {
                    Log.d("LOGIN_STATE", "EMAIL VERIFIED -> showing password")
                    submittedEmail = email
                    isSubmitted = true
                }
                is UiState.EmailNotFound -> {
                    Log.d("LOGIN_STATE", "EMAIL NOT FOUND")
                    showEmailNotFound = true
                }
                is UiState.MustChangePassword -> {
                    Log.d("LOGIN_STATE", "MUST CHANGE PASSWORD -> showing set new password form")
                    isTwoFactorStep = false
                    changePasswordChallengeToken = state.challengeToken
                    changePasswordSubtitle = state.message
                    temporaryPassword = ""
                    newPassword = ""
                    confirmNewPassword = ""
                    tempPasswordError = null
                    newPasswordError = null
                    confirmPasswordError = null
                    errorMessage = null
                    isMustChangePasswordStep = true
                }
                is UiState.Success -> {
                    if (isMustChangePasswordStep) {
                        // Return to sign in if backend requires re-authentication after password update
                        isMustChangePasswordStep = false
                        isTwoFactorStep = false
                        temporaryPassword = ""
                        newPassword = ""
                        confirmNewPassword = ""
                        password = ""
                        errorMessage = null
                        authViewModel.resetState()
                    }
                }
                is UiState.TwoFactorRequired -> {
                    Log.d("LOGIN_STATE", "2FA REQUIRED -> showing two-step verification")
                    isMustChangePasswordStep = false
                    twoFactorChallengeToken = state.challengeToken
                    twoFactorPrompt = if (state.method.equals("authenticator", ignoreCase = true)) {
                        "Enter the 6-digit code from your authenticator app."
                    } else {
                        state.message
                    }
                    twoFactorCode = ""
                    errorMessage = null
                    isTwoFactorStep = true
                }
                is UiState.LoginSuccess -> {
                    isMustChangePasswordStep = false
                    isTwoFactorStep = false
                    twoFactorChallengeToken = ""
                    twoFactorCode = ""
                    permissionViewModel.fetchPermissions()
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
                else -> {}
            }
        }

        Spacer(Modifier.height(tokens.screenPadding))

        // Main Action Button
        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
            AppButton(
                text = when {
                    isMustChangePasswordStep -> "Update Password"
                    isTwoFactorStep -> "Verify and Sign In"
                    isSubmitted -> "Continue"
                    else -> "Verify Email"
                },
                onClick = {
                    focusManager.clearFocus()
                    when {
                        isMustChangePasswordStep -> {
                            var hasValidationError = false
                            if (temporaryPassword.isBlank()) {
                                tempPasswordError = "Temporary password is required"
                                hasValidationError = true
                            }
                            if (!allPasswordRulesMet) {
                                newPasswordError = "Password does not meet all requirements"
                                hasValidationError = true
                            }
                            if (confirmNewPassword != newPassword || confirmNewPassword.isBlank()) {
                                confirmPasswordError = "Passwords do not match"
                                hasValidationError = true
                            }
                            if (!hasValidationError) {
                                authViewModel.changeTemporaryPassword(
                                    temporaryPassword = temporaryPassword,
                                    newPassword = newPassword,
                                    confirmPassword = confirmNewPassword,
                                    challengeToken = changePasswordChallengeToken
                                )
                            }
                        }
                        isTwoFactorStep -> {
                            val trimmedCode = twoFactorCode.trim()
                            if (trimmedCode.isBlank()) {
                                errorMessage = "Please enter the verification code"
                            } else {
                                errorMessage = null
                                authViewModel.verifyTwoFa(
                                    challengeToken = twoFactorChallengeToken,
                                    code = trimmedCode
                                )
                            }
                        }
                        !isSubmitted -> authViewModel.verifyEmail(email)
                        else -> authViewModel.login(email, password)
                    }
                },
                isLoading = accountState is UiState.Loading,
                modifier = Modifier.fillMaxWidth()
            )

            if (isTwoFactorStep) {
                Spacer(Modifier.height(tokens.extraPadding))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Back to sign in",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = Primary,
                        modifier = Modifier
                            .clickable {
                                isTwoFactorStep = false
                                twoFactorChallengeToken = ""
                                twoFactorCode = ""
                                password = ""
                                errorMessage = null
                                authViewModel.resetState()
                            }
                            .padding(vertical = tokens.extraPadding * 0.4f)
                    )
                }
            }

            if (!isMustChangePasswordStep) {
                Spacer(Modifier.height(tokens.screenPadding))
                OrText()
                Spacer(Modifier.height(tokens.screenPadding))

                ContinueWithGoogle(activity, navController)

                Spacer(Modifier.height(tokens.screenPadding))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    SignUpText()
                }
            }
        }
    }
}

@Composable
private fun SetNewPasswordSection(
    subtitle: String,
    temporaryPassword: String,
    onTemporaryPasswordChange: (String) -> Unit,
    newPassword: String,
    onNewPasswordChange: (String) -> Unit,
    confirmNewPassword: String,
    onConfirmNewPasswordChange: (String) -> Unit,
    tempPasswordError: String?,
    newPasswordError: String?,
    confirmPasswordError: String?,
    hasValidLength: Boolean,
    hasUppercase: Boolean,
    hasLowercase: Boolean,
    hasNumber: Boolean,
    hasSymbol: Boolean,
    enabled: Boolean
) {
    val tokens = LocalAppTokens.current

    // Header row with amber shield badge, title, and subtitle
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(tokens.fieldHeight)
                .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.6f))
                .background(yellowBg)
                .border(
                    width = 1.dp,
                    color = yellowText.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.6f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.GppMaybe,
                contentDescription = "Set a new password",
                tint = yellowText,
                modifier = Modifier.size(tokens.iconSize)
            )
        }

        Spacer(Modifier.width(tokens.extraPadding * 1.2f))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Set a new password",
                fontSize = tokens.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = title_color
            )
            Spacer(Modifier.height(tokens.extraPadding * 0.4f))
            Text(
                text = subtitle,
                fontSize = tokens.bodySmall,
                color = TextSecondary
            )
        }
    }

    Spacer(Modifier.height(tokens.screenPadding * 1.25f))

    // Temporary password input
    FormLabel(text = "Temporary Password", isRequired = true)
    FormPasswordField(
        value = temporaryPassword,
        onValueChange = onTemporaryPasswordChange,
        placeholder = "",
        isError = !tempPasswordError.isNullOrBlank(),
        errorMessage = tempPasswordError,
        enabled = enabled,
        borderColor = sectionBorder,
        textColor = TextPrimary,
        placeholderColor = mutedText,
        containerColor = whiteBg
    )

    Spacer(Modifier.height(tokens.screenPadding))

    // New password input
    FormLabel(text = "New Password", isRequired = true)
    FormPasswordField(
        value = newPassword,
        onValueChange = onNewPasswordChange,
        placeholder = "",
        isError = !newPasswordError.isNullOrBlank(),
        errorMessage = newPasswordError,
        enabled = enabled,
        borderColor = sectionBorder,
        textColor = TextPrimary,
        placeholderColor = mutedText,
        containerColor = whiteBg
    )

    Spacer(Modifier.height(tokens.extraPadding))

    // Two-column password requirement checklist
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.6f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
        ) {
            PasswordRequirementItem(
                label = "8-64 characters",
                isMet = hasValidLength,
                modifier = Modifier.weight(1f)
            )
            PasswordRequirementItem(
                label = "One uppercase letter",
                isMet = hasUppercase,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
        ) {
            PasswordRequirementItem(
                label = "One lowercase letter",
                isMet = hasLowercase,
                modifier = Modifier.weight(1f)
            )
            PasswordRequirementItem(
                label = "One number",
                isMet = hasNumber,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
        ) {
            PasswordRequirementItem(
                label = "One symbol",
                isMet = hasSymbol,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.weight(1f))
        }
    }

    Spacer(Modifier.height(tokens.screenPadding))

    // Confirm new password input
    FormLabel(text = "Confirm New Password", isRequired = true)
    FormPasswordField(
        value = confirmNewPassword,
        onValueChange = onConfirmNewPasswordChange,
        placeholder = "",
        isError = !confirmPasswordError.isNullOrBlank(),
        errorMessage = confirmPasswordError,
        enabled = enabled,
        borderColor = sectionBorder,
        textColor = TextPrimary,
        placeholderColor = mutedText,
        containerColor = whiteBg
    )
}

@Composable
private fun PasswordRequirementItem(
    label: String,
    isMet: Boolean,
    modifier: Modifier = Modifier
) {
    val tokens = LocalAppTokens.current
    val itemColor = if (isMet) greentext else mutedText

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isMet) Icons.Default.Check else Icons.Default.Close,
            contentDescription = null,
            tint = itemColor,
            modifier = Modifier.size(tokens.iconSize * 0.75f)
        )
        Spacer(Modifier.width(tokens.extraPadding * 0.5f))
        Text(
            text = label,
            fontSize = tokens.caption,
            fontWeight = FontWeight.Normal,
            color = itemColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}