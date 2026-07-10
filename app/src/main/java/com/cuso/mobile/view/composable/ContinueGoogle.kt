package com.cuso.mobile.view.composable

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
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.cuso.mobile.R
import com.cuso.mobile.viewmodel.Authenticate
import com.cuso.mobile.viewmodel.UiState
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import com.cuso.mobile.ui.theme.PrimaryBorder
@Suppress("DEPRECATION")
@Composable
fun ContinueWithGoogle(activity: Activity, navController: NavController) {
    val authViewModel: Authenticate = hiltViewModel()
    val accountState by authViewModel.accountState.collectAsState()
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d("GOOGLE", "resultCode = ${result.resultCode}")
        Log.d("GOOGLE", "data = ${result.data}")

        if (result.data == null) {
            errorMessage = "Google sign in cancelled or returned no data"
            Log.e("GOOGLE", "result.data is null")
            return@rememberLauncherForActivityResult
        }

        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            Log.d("GOOGLE", "account = $account")
            Log.d("GOOGLE", "idToken = $idToken")
            Log.d("GOOGLE", "email = ${account?.email}")

            if (idToken != null) {
                Log.d("GOOGLE", "Calling googleLogin API")
                authViewModel.googleLogin(idToken)
            } else {
                errorMessage = "Google idToken is null. Check SHA-1 and Web Client ID."
                Log.e("GOOGLE", "idToken is NULL - check SHA-1 fingerprint and Web Client ID in Firebase console")
            }
        } catch (e: ApiException) {
            errorMessage = "Google sign in failed: code ${e.statusCode}"
            Log.e("GOOGLE", "ApiException code: ${e.statusCode}, message: ${e.message}")
            e.printStackTrace()
        }
    }

    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken("111315665171-i45hggp7t1umbnmh1v0glo35d26qe3e1.apps.googleusercontent.com")
        .requestEmail()
        .build()

    val googleSignInClient = GoogleSignIn.getClient(activity, gso)

    LaunchedEffect(accountState) {
        when (val state = accountState) {
            is UiState.GoogleLoginExisting -> {
                Log.d("GOOGLE", "ExistingUser: ${state.message}")
                navController.navigate("home") {
                    popUpTo("login") { inclusive = true }
                }
            }
            is UiState.GoogleLoginNew -> {
                Log.d("GOOGLE", "NewUser: ${state.message}")
                navController.navigate("org")
            }
            is UiState.Error -> {
                errorMessage = "API Error: ${state.message}"
                Log.e("GOOGLE", "UiState.Error: ${state.message}")
            }
            is UiState.Loading -> {
                Log.d("GOOGLE", "Loading...")
            }
            else -> {}
        }
    }

    Column {
        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
            OutlinedButton(
                onClick = {
                    errorMessage = null
                    Log.d("GOOGLE", "Button clicked, launching sign in")
                    googleSignInClient.signOut().addOnCompleteListener {
                        Log.d("GOOGLE", "signOut complete, launching intent")
                        launcher.launch(googleSignInClient.signInIntent)
                    }
                },
                modifier = Modifier
                    .height(40.dp)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 0.dp, horizontal = 16.dp),
                border = BorderStroke(1.dp, PrimaryBorder),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(5.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(R.drawable.google),
                            contentDescription = "Google icon",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Continue with Google",
                            color = Color.Black,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

//        // 👇 Show error visibly on screen
//        if (errorMessage != null) {
//            Text(
//                text = errorMessage!!,
//                color = Color.Red,
//                fontSize = 13.sp,
//                modifier = Modifier.padding(top = 4.dp)
//            )
//        }
    }
}

