@file:Suppress(
    "UNUSED_VALUE",
    "ASSIGNED_VALUE_IS_NEVER_READ",
    "unused",
    "SpellCheckingInspection",
    "GrazieInspection",
    "UnusedMaterial3ScaffoldPaddingParameter"
)

package com.cuso.mobile.view.login

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Alignment
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.blackTitle
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.AuthScreenScaffold
import com.cuso.mobile.view.composable.CardContentsLoginScreen
import com.cuso.mobile.view.composable.DynamicIslandSuccess
import com.cuso.mobile.viewmodel.Authenticate
import com.cuso.mobile.viewmodel.UiState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp

@Suppress("UNUSED_PARAMETER", "VariableNeverRead")
@Composable
fun LoginScreen(
    activity: Activity,
    navController: NavController,
    onloginSuccess: (String) -> Unit,
    authViewModel: Authenticate = hiltViewModel(),
    prefilledEmail: String = "",
    resetSuccessMessage: String = ""
) {
    // Access Adaptive Tokens (still needed here for the exit dialog text)
    val tokens = LocalAppTokens.current

    val authState by authViewModel.accountState.collectAsState()
    var isLoginMode by remember { mutableStateOf(true) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val snackbarState = remember { SnackbarHostState() }
    var showExitDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var successMessage by remember { mutableStateOf<String?>(null) }

    BackHandler {
        showExitDialog = true
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            containerColor = whiteBg,
            title = {
                Text(
                    text = "Exit App",
                    color = blackTitle,
                    fontSize = tokens.h2, // Adaptive Font
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to exit?",
                    color = blackTitle,
                    fontSize = tokens.bodyMedium // Adaptive Font
                )
            },
            confirmButton = {
                TextButton(onClick = { (context as? Activity)?.finish() }) {
                    Text("Exit", color = Color.Red, fontSize = tokens.bodyLarge)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Cancel", color = blackTitle, fontSize = tokens.bodyLarge)
                }
            }
        )
    }

    LaunchedEffect(authState) {
        when (val state = authState) {
            is UiState.LoginSuccess -> {
                val orgToken = state.orgToken
                val org = state.organization
                val isOrgRegistered = org != null && org.orgSetupComplete

                if (orgToken.isNullOrEmpty() || !isOrgRegistered) {
                    navController.navigate("org") {
                        popUpTo(0) { inclusive = true }
                    }
                } else {
                    onloginSuccess("${state.firstName} ${state.lastName}")
                }
                authViewModel.resetState()
            }
            is UiState.RegisterSuccess -> {
                snackbarState.showSnackbar("Account created! Please log in.")
                isLoginMode = true
                username = ""
                password = ""
                authViewModel.resetState()
            }
            else -> {}
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarState) },
            containerColor = Color(0xFFf5f5f5)
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize()) {
                // Reusable structure: background + scroll + adaptive padding +
                // logo + title + subtitle + width-limited bordered card
                AuthScreenScaffold(
                    title = "Welcome to CUSO Tailor",
                    subtitle = "Please login using the form below"
                ) {
                    // Only the screen-specific form content goes here
                    CardContentsLoginScreen(
                        navController,
                        activity,
                        authViewModel,
                        prefilledEmail = prefilledEmail
                        // Note: Ensure CardContentsLoginScreen uses tokens for its TextField/Buttons
                    )
                }
            }
            DynamicIslandSuccess(
                modifier = Modifier.align(Alignment.TopCenter)
                    .padding(top = 100.dp),
                message = successMessage,
                onDismiss = { successMessage = null }
            )
        }

        LaunchedEffect(resetSuccessMessage) {
            if (resetSuccessMessage.isNotBlank()) {
                successMessage = resetSuccessMessage
            }
        }
    }
}