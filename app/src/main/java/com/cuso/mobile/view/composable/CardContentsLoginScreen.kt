package com.cuso.mobile.view.composable

import android.app.Activity
import android.net.Uri
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
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.cuso.mobile.R
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.Primary
import com.cuso.mobile.ui.theme.PrimaryBorder
import com.cuso.mobile.ui.theme.PrimaryTextColor
import com.cuso.mobile.ui.theme.blackTitle
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.viewmodel.Authenticate
import com.cuso.mobile.viewmodel.UiState

@Composable
fun CardContentsLoginScreen(
    navController: NavController,
    activity: Activity,
    authViewModel: Authenticate,
    prefilledEmail: String = ""
) {
    val tokens = LocalAppTokens.current
    val focusManager = LocalFocusManager.current

    val accountState by authViewModel.accountState.collectAsState()
    var email by rememberSaveable { mutableStateOf(prefilledEmail) }
    var password by remember { mutableStateOf("") }
    var submittedEmail by rememberSaveable { mutableStateOf(prefilledEmail) }
    var isSubmitted by remember { mutableStateOf(prefilledEmail.isNotBlank()) }
    var showEmailNotFound by remember { mutableStateOf(false) }

    // Error state declaration for local validation and OTP error messages
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isError = accountState is UiState.Error || accountState is UiState.EmailNotFound
    val errorMsg = (accountState as? UiState.Error)?.message ?: errorMessage

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

        if (!isSubmitted) {
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
        }

        if (isSubmitted) {
            // User identity box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(whiteBg)
                    .border(1.dp, PrimaryBorder, shape = RoundedCornerShape(5.dp))
                    .padding(horizontal = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF5F5F5)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.PersonOutline,
                            contentDescription = null,
                            tint = PrimaryTextColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Sign in as",
                            fontSize = tokens.caption,
                            color = PrimaryTextColor
                        )
                        Text(
                            text = submittedEmail,
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = blackTitle,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = "Change",
                        modifier = Modifier
                            .clickable { navController.navigate("login") }
                            .padding(8.dp),
                        color = Primary,
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Bold
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

            Spacer(Modifier.height(8.dp))

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
                        .padding(vertical = 8.dp),
                    color = Primary,
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = "Forgot Password?",
                    modifier = Modifier
                        .clickable { navController.navigate("new-pass/${email.trim()}") }
                        .padding(vertical = 8.dp),
                    color = Primary,
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        LaunchedEffect(accountState) {
            if (accountState is UiState.EmailVerified) {
                submittedEmail = email
                isSubmitted = true
            } else if (accountState is UiState.EmailNotFound) {
                showEmailNotFound = true
            }
        }

        Spacer(Modifier.height(tokens.screenPadding))

        // Main Login/Verify Button
        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
            AppButton(
                text = if (isSubmitted) "Continue" else "Verify Email",
                onClick = {
                    focusManager.clearFocus()
                    if (!isSubmitted) authViewModel.verifyEmail(email)
                    else authViewModel.login(email, password)
                },
                isLoading = accountState is UiState.Loading,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(tokens.screenPadding))
            OrText()
            Spacer(Modifier.height(tokens.screenPadding))

            ContinueWithGoogle(activity, navController)
            Spacer(Modifier.height(tokens.screenPadding / 2))
            ContinueWithApple(activity, navController)

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