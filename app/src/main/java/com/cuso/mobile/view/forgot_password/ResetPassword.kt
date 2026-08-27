package com.cuso.mobile.view.forgot_password

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.cuso.mobile.R
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.Primary
import com.cuso.mobile.ui.theme.lightGray
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.AppButton
import com.cuso.mobile.view.composable.AuthScreenScaffold
import com.cuso.mobile.view.composable.CirculerProgressIndicatorSmall
import com.cuso.mobile.view.composable.CusoTextField
import com.cuso.mobile.view.composable.DynamicIslandError
import com.cuso.mobile.viewmodel.Authenticate
import com.cuso.mobile.viewmodel.UiState
import org.json.JSONObject

@Suppress("UNUSED_PARAMETER")
@Composable
fun ResetPassword(
    resetToken: String,
    navController: NavController,
    onResetClick: (newPassword: String) -> Unit = {},
) {
    // Read adaptive design tokens provided at the app root
    val tokens = LocalAppTokens.current

    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Drives the DynamicIslandError pill shown at the top of the screen
    var islandErrorMessage by remember { mutableStateOf<String?>(null) }

    val authViewModel: Authenticate = hiltViewModel()
    val resetPasswordState by authViewModel.resetPasswordState.collectAsState()

    val passwordsMatch = newPassword == confirmPassword
    val isFormValid = newPassword.isNotBlank()
            && confirmPassword.isNotBlank()
            && passwordsMatch
            && newPassword.length >= 8

    val isConfirmError = confirmPassword.isNotBlank() && !passwordsMatch

    // React to state changes coming from the ViewModel
    LaunchedEffect(resetPasswordState) {
        when (val state = resetPasswordState) {
            is UiState.Success -> {
                navController.navigate("login?message=Password changed successfully")
            }
            is UiState.Error -> {
                // Extract only the value of the "message" key from the raw
                // error payload, instead of showing the full raw error string
                islandErrorMessage = extractMessageValue(state.message)
            }
            else -> {}
        }
    }

    // Box wraps the scaffold so the DynamicIslandError pill can float on
    // top of the screen content, anchored to the top center — same
    // approach LoginScreen uses for DynamicIslandSuccess.
    Box(modifier = Modifier.fillMaxSize()) {
        // Reusable structure: same background + scroll + adaptive padding +
        // logo + title + subtitle + width-limited bordered card as
        // LoginScreen / ForgotUserPassword / VerifyForgotPassword.
        AuthScreenScaffold(
            title = "Reset Password",
            subtitle = "Create a new password for your account"
        ) {
            CusoTextField(
                value = newPassword,
                onValueChange = { newPassword = it; authViewModel.resetState() },
                label = "New Password",
                placeholder = "Enter Your Password",
                leadingIconPainter = painterResource(R.drawable.ic_lock),
                isPassword = true,
                isError = false,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            Spacer(modifier = Modifier.height(tokens.screenPadding / 2))

            CusoTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it; authViewModel.resetState() },
                label = "Confirm Password",
                placeholder = "Re-Enter Password",
                leadingIconPainter = painterResource(R.drawable.ic_lock),
                isPassword = true,
                isError = isConfirmError,
                errorText = if (isConfirmError) "Passwords don't match" else null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            // Local validation error message (e.g. from form-level checks)
            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = tokens.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(tokens.screenPadding / 2))

            // Reset Password button
            AppButton(
                text = "Reset Password",
                onClick = {
                    authViewModel.resetNewPassword(
                        token = resetToken,
                        newPassword = newPassword,
                        confirmPassword = confirmPassword
                    )
                },
                enabled = isFormValid && resetPasswordState !is UiState.Loading,
                isLoading = resetPasswordState is UiState.Loading,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Floating error pill, shown at the top of the screen whenever
        // the API returns UiState.Error
        DynamicIslandError(
            modifier = Modifier.align(Alignment.TopCenter),
            message = islandErrorMessage,
            onDismiss = { islandErrorMessage = null }
        )
    }
}

/**
 * The raw error string coming from UiState.Error might be a full JSON
 * payload like {"success": false, "message": "Token expired", "code": 400}
 * or an exception toString() that wraps that JSON.
 *
 * This extracts ONLY the value of the "message" key from that payload,
 * so the pill shows a clean user-facing string instead of the raw
 * JSON/exception dump.
 *
 * Falls back to the original raw string if it isn't valid JSON or doesn't
 * contain a "message" key at all (so nothing silently disappears).
 */
private fun extractMessageValue(raw: String?): String? {
    if (raw.isNullOrBlank()) return raw
    return try {
        JSONObject(raw).optString("message", raw)
    } catch (_: Exception) {
        // Not JSON (or malformed) — fall back to showing the raw string as-is
        raw
    }
}