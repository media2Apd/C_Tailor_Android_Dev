package com.cuso.mobile.view.composable

import android.app.Activity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.*
import com.cuso.mobile.viewmodel.Authenticate
import com.cuso.mobile.viewmodel.UiState
import com.cuso.mobile.R

@Composable
fun CardContentsForgotPassword(
    navController: NavController,
    activity: Activity,
    prefilledEmail: String = ""
) {
    val tokens = LocalAppTokens.current
    val focusManager = LocalFocusManager.current
    val authViewModel: Authenticate = hiltViewModel()

    var email by rememberSaveable { mutableStateOf(prefilledEmail) }
    val isEmailLocked = prefilledEmail.isNotBlank()
    val forgotPasswordState by authViewModel.forgotPasswordState.collectAsState()

    val isError = forgotPasswordState is UiState.Error
    val errorMsg = (forgotPasswordState as? UiState.Error)?.message

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                focusManager.clearFocus()
            }
            .padding(tokens.screenPadding)
    ) {
        // Adaptive Header
        Text(
            text = "Reset your password",
            fontSize = tokens.h2,
            fontWeight = FontWeight.Bold,
            color = blackTitle
        )

        Spacer(Modifier.height(tokens.screenPadding / 2))

        // Reusable Adaptive TextField
        CusoTextField(
            value = email,
            onValueChange = { if (!isEmailLocked) email = it },
            label = "Email",
            placeholder = "your@email.com",
            leadingIconPainter = painterResource(R.drawable.ic_mail),
            isError = isError,
            errorText = errorMsg,
            // Visual feedback for locked email
            modifier = Modifier.padding(bottom = tokens.screenPadding)
        )

        LaunchedEffect(forgotPasswordState) {
            if (forgotPasswordState is UiState.Success) {
                navController.navigate("verify-forgot-pass/$email")
            }
        }

        // Standardized Primary Button (40dp height, 5dp radius)
        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
            Button(
                onClick = {
                    focusManager.clearFocus()
                    if (email.isNotBlank()) authViewModel.forgotPasswordOtp(email)
                },
                enabled = email.isNotBlank() && forgotPasswordState !is UiState.Loading,
                modifier = Modifier.fillMaxWidth().height(40.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = whiteBg),
                shape = RoundedCornerShape(5.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                if (forgotPasswordState is UiState.Loading) {
                    CirculerProgressIndicatorSmall()
                } else {
                    Text("Send Reset Code", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(Modifier.height(tokens.screenPadding))

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            BackToSignIn(navController)
        }
    }
}