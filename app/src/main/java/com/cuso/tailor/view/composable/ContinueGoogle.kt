@file:Suppress(
    "UNUSED_PARAMETER",
    "unused",
    "UNCHECKED_CAST",
    "DEPRECATION",
    "AssignedValueIsNeverRead",
    "GrazieInspection",
    "SpellCheckingInspection",
    "unusedvariable",
    "VariableNeverRead"
)

package com.cuso.tailor.view.composable

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.cuso.tailor.R
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.PrimaryBorder
import com.cuso.tailor.ui.theme.blackTitle
import com.cuso.tailor.ui.theme.whiteBg
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.viewmodel.GoogleLoginUiState
import com.cuso.tailor.viewmodel.LoginViewModel
import com.cuso.tailor.viewmodel.PermissionViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@Suppress("DEPRECATION")
@Composable
fun ContinueWithGoogle(
    activity: Activity,
    navController: NavController,
    loginViewModel: LoginViewModel = hiltViewModel(),
    permissionViewModel: PermissionViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val googleLoginState by loginViewModel.googleLoginState.collectAsState()
    val isLoading by loginViewModel.isLoading.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.data == null) {
            DynamicIslandManager.showError("Google sign in cancelled or returned no data")
            return@rememberLauncherForActivityResult
        }

        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken

            if (!idToken.isNullOrBlank()) {
                loginViewModel.googleLogin(idToken)
            } else {
                DynamicIslandManager.showError("Google idToken is null. Check SHA-1 and Web Client ID.")
            }
        } catch (e: ApiException) {
            DynamicIslandManager.showError("Google sign in failed: code ${e.statusCode}")
        }
    }

    // Replace with your real OAuth 2.0 Web Client ID (type 3) from Google Cloud Console / google-services.json
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("123238978245-kdc6n2qacc51o0nn7cj1vgjn1h9muh11.apps.googleusercontent.coma")
            .requestEmail()
            .build()
    }

    val googleSignInClient = remember(activity, gso) {
        GoogleSignIn.getClient(activity, gso)
    }

    LaunchedEffect(googleLoginState) {
        when (googleLoginState) {
            is GoogleLoginUiState.ExistingOrg -> {
                permissionViewModel.fetchPermissions()
                loginViewModel.resetGoogleLoginState()
                navController.navigate("home") {
                    popUpTo("login") { inclusive = true }
                }
            }
            is GoogleLoginUiState.NewOrg -> {
                loginViewModel.resetGoogleLoginState()
                navController.navigate("org")
            }
            else -> Unit
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
            OutlinedButton(
                onClick = {
                    if (!isLoading) {
                        loginViewModel.resetGoogleLoginState()
                        googleSignInClient.signOut().addOnCompleteListener {
                            launcher.launch(googleSignInClient.signInIntent)
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .height(tokens.buttonHeight)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(
                    vertical = 0.dp,
                    horizontal = tokens.screenPadding
                ),
                border = BorderStroke(1.dp, PrimaryBorder),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = whiteBg,
                    contentColor = blackTitle
                ),
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.35f)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(tokens.iconSize),
                            color = Primary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(R.drawable.google),
                                contentDescription = "Google icon",
                                modifier = Modifier.size(tokens.iconSize)
                            )
                            Spacer(modifier = Modifier.width(tokens.extraPadding * 0.8f))
                            Text(
                                text = "Continue with Google",
                                color = blackTitle,
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}