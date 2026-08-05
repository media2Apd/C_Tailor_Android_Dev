package com.cuso.mobile.view.forgot_password

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.cuso.mobile.ui.theme.Primary
import com.cuso.mobile.ui.theme.blackTitle
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.AppLogo
import com.cuso.mobile.view.composable.CirculerProgressIndicatorSmall
import com.cuso.mobile.view.composable.customFieldColors
import com.cuso.mobile.viewmodel.Authenticate
import com.cuso.mobile.viewmodel.UiState

@Suppress("UNUSED_PARAMETER")
@Composable
fun ResetPassword(
    resetToken: String,
    navController: NavController,
    onResetClick: (newPassword: String) -> Unit = {},
) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val authViewModel: Authenticate = hiltViewModel()

    val resetPasswordState by authViewModel.resetPasswordState.collectAsState()

    val passwordsMatch = newPassword == confirmPassword
    val isFormValid = newPassword.isNotBlank()
            && confirmPassword.isNotBlank()
            && passwordsMatch
            && newPassword.length >= 8

    val newPasswordInteractionSource = remember { MutableInteractionSource() }
    val confirmPasswordInteractionSource = remember { MutableInteractionSource() }
    val isConfirmError = confirmPassword.isNotBlank() && !passwordsMatch

    // ✅ Column with verticalScroll + imePadding: centers when content fits,
    // scrolls when it doesn't, and clears the keyboard — same base as
    // ForgotUserPassword / VerifyForgotPassword.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFf5f5f5)) // ✅ matches the other forgot-password screens' page background
            .verticalScroll(rememberScrollState())
            .imePadding(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AppLogo()
        Spacer(modifier = Modifier.height(10.dp))

        // ✅ Title styled the same as ForgotUserPassword / VerifyForgotPassword
        Text(
            text = "Reset Password",
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            color = blackTitle
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Create a new password for your account",
            fontSize = 14.sp,
            color = Color(0xFF6B7280)
        )
        Spacer(modifier = Modifier.height(10.dp))

        // ✅ Card wrapped in a Box(padding 20dp) with a 2dp white border and
        // 15dp corner radius — same treatment as the other forgot-password screens.
        Box(
            Modifier.padding(20.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 2.dp,
                        color = whiteBg,
                        shape = RoundedCornerShape(15.dp)
                    ),
                shape = RoundedCornerShape(15.dp),
                colors = CardDefaults.cardColors(containerColor = whiteBg)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp)
                ) {

                    Text(
                        text = "New Password",
                        fontSize = 12.47.sp,
                        fontWeight = FontWeight.Bold,
                        color = blackTitle
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // ✅ New Password field — BasicTextField + DecorationBox instead
                    // of OutlinedTextField, because this Material3 version's
                    // OutlinedTextField(value: String, ...) overload has no
                    // `contentPadding` param, which is required to make text fit
                    // cleanly inside a 40dp-tall field.
                    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
                        BasicTextField(
                            value = newPassword,
                            onValueChange = {
                                newPassword = it
                                errorMessage = null
                            },
                            singleLine = true,
                            textStyle = TextStyle(color = blackTitle, fontSize = 13.sp),
                            visualTransformation = if (newPasswordVisible)
                                VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            interactionSource = newPasswordInteractionSource,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp),
                            decorationBox = { innerTextField ->
                                OutlinedTextFieldDefaults.DecorationBox(
                                    value = newPassword,
                                    innerTextField = innerTextField,
                                    enabled = true,
                                    singleLine = true,
                                    visualTransformation = if (newPasswordVisible)
                                        VisualTransformation.None else PasswordVisualTransformation(),
                                    interactionSource = newPasswordInteractionSource,
                                    isError = false,
                                    placeholder = {
                                        Text("..", color = Color.LightGray, fontSize = 13.sp)
                                    },
                                    trailingIcon = {
                                        IconButton(
                                            onClick = { newPasswordVisible = !newPasswordVisible },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (newPasswordVisible)
                                                    Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                contentDescription = if (newPasswordVisible)
                                                    "Hide password" else "Show password",
                                                tint = Color(0xFF9CA3AF),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    },
                                    colors = customFieldColors(),
                                    contentPadding = PaddingValues(
                                        start = 12.dp,
                                        end = 8.dp,
                                        top = 0.dp,
                                        bottom = 0.dp
                                    ),
                                    container = {
                                        OutlinedTextFieldDefaults.Container(
                                            enabled = true,
                                            isError = false,
                                            interactionSource = newPasswordInteractionSource,
                                            colors = customFieldColors(),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                    }
                                )
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(15.dp))

                    Text(
                        text = "Confirm Password",
                        fontSize = 12.47.sp,
                        fontWeight = FontWeight.Bold,
                        color = blackTitle
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // ✅ Confirm Password field — same BasicTextField + DecorationBox
                    // conversion, with the match-error state wired into isError.
                    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
                        BasicTextField(
                            value = confirmPassword,
                            onValueChange = {
                                confirmPassword = it
                                errorMessage = null
                            },
                            singleLine = true,
                            textStyle = TextStyle(color = blackTitle, fontSize = 13.sp),
                            visualTransformation = if (confirmPasswordVisible)
                                VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            interactionSource = confirmPasswordInteractionSource,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp),
                            decorationBox = { innerTextField ->
                                OutlinedTextFieldDefaults.DecorationBox(
                                    value = confirmPassword,
                                    innerTextField = innerTextField,
                                    enabled = true,
                                    singleLine = true,
                                    visualTransformation = if (confirmPasswordVisible)
                                        VisualTransformation.None else PasswordVisualTransformation(),
                                    interactionSource = confirmPasswordInteractionSource,
                                    isError = isConfirmError,
                                    placeholder = {
                                        Text("..", color = Color.LightGray, fontSize = 13.sp)
                                    },
                                    trailingIcon = {
                                        IconButton(
                                            onClick = { confirmPasswordVisible = !confirmPasswordVisible },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (confirmPasswordVisible)
                                                    Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                contentDescription = if (confirmPasswordVisible)
                                                    "Hide password" else "Show password",
                                                tint = Color(0xFF9CA3AF),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    },
                                    // ✅ 8dp to match the New Password field above and the
                                    // field shape used across the other forgot-password screens
                                    colors = customFieldColors(),
                                    contentPadding = PaddingValues(
                                        start = 12.dp,
                                        end = 8.dp,
                                        top = 0.dp,
                                        bottom = 0.dp
                                    ),
                                    container = {
                                        OutlinedTextFieldDefaults.Container(
                                            enabled = true,
                                            isError = isConfirmError,
                                            interactionSource = confirmPasswordInteractionSource,
                                            colors = customFieldColors(),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                    }
                                )
                            }
                        )
                    }
                    if (isConfirmError) {
                        Text(
                            text = "Passwords don't match",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }

                    // Error message (e.g. from API)
                    errorMessage?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp
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
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Spacer(modifier = Modifier.height(10    .dp))

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
                            .height(50.dp),
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
                                fontSize = 14.sp,
                                color = whiteBg
                            )
                        } else {
                            Text(
                                text = "Reset Password",
                                fontSize = 14.sp,
                                color = whiteBg
                            )
                        }
                    }
                }
            }
        }
    }
}