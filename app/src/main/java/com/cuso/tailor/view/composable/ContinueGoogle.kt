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
import android.util.Log
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
import androidx.compose.ui.res.stringResource
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

private const val TAG = "GoogleSignInDebug"

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

    val webClientId = stringResource(R.string.google_web_client_id).trim()

    LaunchedEffect(webClientId) {
        Log.d(TAG, "Resolved google_web_client_id = $webClientId")
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d(TAG, "ActivityResult received: resultCode=${result.resultCode}, hasData=${result.data != null}")

        if (result.data == null) {
            Log.e(TAG, "Google sign in cancelled or returned null Intent data")
            DynamicIslandManager.showError("Google sign in cancelled or returned no data")
            return@rememberLauncherForActivityResult
        }

        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            Log.d(
                TAG,
                "Google account retrieved: email=${account?.email}, hasIdToken=${!idToken.isNullOrBlank()}, tokenLength=${idToken?.length ?: 0}"
            )

            if (!idToken.isNullOrBlank()) {
                Log.d(TAG, "Calling loginViewModel.googleLogin(idToken)...")
                loginViewModel.googleLogin(idToken)
            } else {
                Log.e(TAG, "idToken is null/blank! Check SHA-1 fingerprint and Web Client ID in Google Cloud Console.")
                DynamicIslandManager.showError("Google idToken is null. Check SHA-1 and Web Client ID.")
            }
        } catch (e: ApiException) {
            Log.e(TAG, "Google sign in ApiException: statusCode=${e.statusCode}, message=${e.message}", e)
            val detailedMsg = when (e.statusCode) {
                10 -> {
                    Log.e(
                        TAG,
                        "DEVELOPER_ERROR (10): Package name 'com.cuso.tailor' and debug/release SHA-1 must be added in Google Cloud Console Android OAuth Client."
                    )
                    "Google sign in failed (Code 10: Developer Error). Check SHA-1 & Android OAuth Client in Google Cloud."
                }
                12500 -> "Google sign in failed (Code 12500). Check OAuth Consent Screen & Play Services."
                12501 -> "Google sign in cancelled by user."
                4 -> "Google sign in required user interaction (Code 4)."
                else -> "Google sign in failed: code ${e.statusCode}"
            }
            DynamicIslandManager.showError(detailedMsg)
        }
    }

    val gso = remember(webClientId) {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
    }

    val googleSignInClient = remember(activity, gso) {
        GoogleSignIn.getClient(activity, gso)
    }

    LaunchedEffect(googleLoginState) {
        Log.d(TAG, "googleLoginState changed -> $googleLoginState")
        when (googleLoginState) {
            is GoogleLoginUiState.ExistingOrg -> {
                Log.d(TAG, "Navigating to home (ExistingOrg)")
                permissionViewModel.fetchPermissions(forceRefresh = true)
                loginViewModel.resetGoogleLoginState()
                navController.navigate("home") {
                    popUpTo("login") { inclusive = true }
                }
            }
            is GoogleLoginUiState.NewOrg -> {
                Log.d(TAG, "Navigating to org (NewOrg)")
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
                        if (webClientId.isBlank()) {
                            DynamicIslandManager.showError("Google Web Client ID is missing")
                            return@OutlinedButton
                        }
                        Log.d(TAG, "Continue with Google clicked -> signing out old session first")
                        loginViewModel.resetGoogleLoginState()
                        googleSignInClient.signOut().addOnCompleteListener { signOutTask ->
                            Log.d(TAG, "signOut complete (success=${signOutTask.isSuccessful}) -> launching Google picker")
                            try {
                                launcher.launch(googleSignInClient.signInIntent)
                            } catch (e: Exception) {
                                Log.e(TAG, "Failed to launch Google Sign In Intent", e)
                                DynamicIslandManager.showError("Unable to open Google Sign In: ${e.localizedMessage}")
                            }
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