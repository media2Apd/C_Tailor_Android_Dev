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
import androidx.compose.material.icons.filled.PersonOutline
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
import com.cuso.tailor.ui.theme.light_blue_border
import com.cuso.tailor.ui.theme.light_grey
import com.cuso.tailor.ui.theme.mutedText
import com.cuso.tailor.ui.theme.primary_light
import com.cuso.tailor.ui.theme.redText
import com.cuso.tailor.ui.theme.title_color
import com.cuso.tailor.ui.theme.whiteBg
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

    // Local validation and OTP error message state
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isError = accountState is UiState.Error || accountState is UiState.EmailNotFound || !errorMessage.isNullOrBlank()
    val errorMsg = (accountState as? UiState.Error)?.message?.takeIf { it.isNotBlank() } ?: errorMessage

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

        if (isTwoFactorStep) {
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
                is UiState.TwoFactorRequired -> {
                    Log.d("LOGIN_STATE", "2FA REQUIRED -> showing two-step verification")
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
                    isTwoFactorStep -> "Verify and Sign In"
                    isSubmitted -> "Continue"
                    else -> "Verify Email"
                },
                onClick = {
                    focusManager.clearFocus()
                    when {
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