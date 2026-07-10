package com.cuso.mobile.view.forgot_password

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.cuso.mobile.view.composable.AppLogo
import com.cuso.mobile.view.composable.CirculerProgressIndicatorReuse
import com.cuso.mobile.view.composable.customFieldColors
import com.cuso.mobile.view.composable.ResetPasswordText
import com.cuso.mobile.viewmodel.Authenticate
import com.cuso.mobile.viewmodel.UiState
@Suppress("UNUSED_PARAMETER")
@Composable
fun ResetPassword(
    resetToken:String,
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

    // ✅ Box(contentAlignment = Center) had no scroll path at all — on a
    // shorter screen, or once the keyboard opened, the card could overflow
    // both the top and bottom of the screen with no way to reach the
    // clipped fields. A Column with verticalScroll + imePadding fixes both:
    // it centers when content fits, and scrolls when it doesn't.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .imePadding(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center

            ) {
                AppLogo()
                Spacer(Modifier.padding(top=10.dp))
                ResetPasswordText()
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp)
            ) {

                // Title & subtitle
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Reset Password",
                        fontSize = 25.sp,
                        color = Color.Black
                    )


                    Spacer(modifier = Modifier.height(15.dp))

                    Text(
                        text = "New Password",
                        fontSize = 16.sp,
                        fontWeight= FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                }


                // New Password field
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = {
                        newPassword = it
                        errorMessage = null
                    },
                    placeholder = { Text("..",color=Color.LightGray) },
                    trailingIcon = {
                        IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                            Icon(
                                imageVector = if (newPasswordVisible)
                                    Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (newPasswordVisible)
                                    "Hide password" else "Show password",
                                tint = Color(0xFF9CA3AF)
                            )
                        }
                    },
                    visualTransformation = if (newPasswordVisible)
                        VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = customFieldColors()
                )
                Column(verticalArrangement = Arrangement.Center)
                {
                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Confirm Password",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                }

                // Confirm Password field
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        errorMessage = null
                    },
                    placeholder = { Text("..",color=Color.LightGray) },
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmPasswordVisible)
                                    Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (confirmPasswordVisible)
                                    "Hide password" else "Show password",
                                tint = Color(0xFF9CA3AF)
                            )
                        }
                    },
                    visualTransformation = if (confirmPasswordVisible)
                        VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    isError = confirmPassword.isNotBlank() && !passwordsMatch,
                    supportingText = {
                        if (confirmPassword.isNotBlank() && !passwordsMatch) {
                            Text(
                                text = "Passwords don't match",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = customFieldColors()
                )

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

                // Reset Password button
                Button(
                    onClick = {
                        authViewModel.resetNewPassword(
                            token = resetToken,
                            newPassword = newPassword,
                            confirmPassword = confirmPassword
                        )
                    },
                    enabled = isFormValid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4F46E5),
                        disabledContainerColor = Color(0xFFC7D2FE)
                    )
                ) {
                    if (resetPasswordState is UiState.Loading) {
                        CirculerProgressIndicatorReuse()

                        Text(
                            text = "Resetting Password",
                            fontSize = 20.sp,
                            color=Color.White
                        )
                    } else {
                        Text(
                            text = "Reset Password",
                            fontSize = 20.sp,
                            color=Color.White
                        )
                    }
                }
            }
        }
    }
}