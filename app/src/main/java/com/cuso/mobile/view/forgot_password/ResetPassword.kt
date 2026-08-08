package com.cuso.mobile.view.forgot_password

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.Primary
import com.cuso.mobile.ui.theme.blackTitle
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.AppLogo
import com.cuso.mobile.view.composable.CirculerProgressIndicatorSmall
import com.cuso.mobile.view.composable.CusoTextField
import com.cuso.mobile.viewmodel.Authenticate
import com.cuso.mobile.viewmodel.UiState

@Suppress("UNUSED_PARAMETER")
@Composable
fun ResetPassword(
    resetToken: String,
    navController: NavController,
    onResetClick: (newPassword: String) -> Unit = {},
) {
    val tokens = LocalAppTokens.current

    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val authViewModel: Authenticate = hiltViewModel()

    val resetPasswordState by authViewModel.resetPasswordState.collectAsState()

    val passwordsMatch = newPassword == confirmPassword
    val isFormValid = newPassword.isNotBlank()
            && confirmPassword.isNotBlank()
            && passwordsMatch
            && newPassword.length >= 8

    val isConfirmError = confirmPassword.isNotBlank() && !passwordsMatch

    // Column with verticalScroll + imePadding: centers when content fits,
    // scrolls when it doesn't, and clears the keyboard — same base as
    // ForgotUserPassword / VerifyForgotPassword.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFf5f5f5))
            .verticalScroll(rememberScrollState())
            .imePadding(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AppLogo()
        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Reset Password",
            fontSize = tokens.h1,
            fontWeight = FontWeight.Bold,
            color = blackTitle
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Create a new password for your account",
            fontSize = tokens.bodyMedium,
            color = Color(0xFF6B7280)
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Card wrapped in a Box(padding tokens.screenPadding) with a 2dp white
        // border and adaptive corner radius — same treatment as the other
        // forgot-password screens.
        Box(
            Modifier.padding(tokens.screenPadding)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 2.dp,
                        color = whiteBg,
                        shape = RoundedCornerShape(tokens.cardCornerRadius)
                    ),
                shape = RoundedCornerShape(tokens.cardCornerRadius),
                colors = CardDefaults.cardColors(containerColor = whiteBg)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(tokens.cardPadding)
                ) {

                    // New Password field — now CusoTextField instead of a manual
                    // BasicTextField + DecorationBox.
                    CusoTextField(
                        value = newPassword,
                        onValueChange = {
                            newPassword = it
                            errorMessage = null
                        },
                        label = "New Password",
                        placeholder = "..",
                        isPassword = true,
                        isError = false,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )

                    Spacer(modifier = Modifier.height(15.dp))

                    // Confirm Password field — same conversion, with the
                    // match-error state wired into isError.
                    CusoTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            errorMessage = null
                        },
                        label = "Confirm Password",
                        placeholder = "..",
                        isPassword = true,
                        isError = isConfirmError,
                        errorText = if (isConfirmError) "Passwords don't match" else null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )

                    // Error message (e.g. from API)
                    errorMessage?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = tokens.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // React to state
                    LaunchedEffect(resetPasswordState) {
                        when (resetPasswordState) {
                            is UiState.Success -> {
                                navController.navigate("login?message=Password changed successfully")
                            }
                            is UiState.Error -> {
                                // show error message
                            }
                            else -> {}
                        }
                    }

                    // Show error message
                    if (resetPasswordState is UiState.Error) {
                        Text(
                            text = (resetPasswordState as UiState.Error).message,
                            color = Color.Red,
                            fontSize = tokens.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    // Reset Password button
                    Button(
                        onClick = {
                            authViewModel.resetNewPassword(
                                token = resetToken,
                                newPassword = newPassword,
                                confirmPassword = confirmPassword
                            )
                        },
                        enabled = isFormValid && resetPasswordState !is UiState.Loading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(tokens.buttonHeight),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary,
                            disabledContainerColor = Color(0xFFC7D2FE)
                        )
                    ) {
                        if (resetPasswordState is UiState.Loading) {
                            CirculerProgressIndicatorSmall()

                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Resetting Password",
                                fontSize = tokens.bodyMedium,
                                color = whiteBg
                            )
                        } else {
                            Text(
                                text = "Reset Password",
                                fontSize = tokens.bodyMedium,
                                color = whiteBg
                            )
                        }
                    }
                }
            }
        }
    }
}