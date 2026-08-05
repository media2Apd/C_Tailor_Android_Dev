@file:Suppress(
    "UNUSED_VALUE",
    "ASSIGNED_VALUE_IS_NEVER_READ",
    "unused",
    "SpellCheckingInspection",
    "GrazieInspection"
)
package com.cuso.mobile.view.login

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.CardContentsLoginScreen
import com.cuso.mobile.view.composable.AppLogo
import com.cuso.mobile.view.composable.DynamicIslandSuccess
import com.cuso.mobile.view.composable.LoginScreenTitle
import com.cuso.mobile.viewmodel.UiState
import com.cuso.mobile.viewmodel.Authenticate

@Suppress("UNUSED_PARAMETER", "VariableNeverRead")
@Composable
fun LoginScreen(activity: Activity,
                navController: NavController,
                onloginSuccess: (String)-> Unit,
                authViewModel: Authenticate= hiltViewModel(),
                prefilledEmail:String="",
                resetSuccessMessage:String=""

) {


    val authState by authViewModel.accountState.collectAsState()
    var isLoginMode by remember{ mutableStateOf(true) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val snackbarState = remember { SnackbarHostState() }
    var showExitDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var successMessage by remember { mutableStateOf<String?>(null) }
//    var snackbarMessage by remember { mutableStateOf<String?>(null) }



    BackHandler {
        showExitDialog = true
    }
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            containerColor = whiteBg,
            title = { Text("Exit App",Modifier,color=Color.Black, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to exit?",color=Color.Black) },
            confirmButton = {
                TextButton(onClick = {
                    (context as? Activity)?.finish()
                }) {
                    Text("Exit", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Cancel",color=Color.Black)
                }
            }
        )
    }

    LaunchedEffect(authState) {
        when (val state = authState) {
            is UiState.LoginSuccess -> {

                val orgToken = state.orgToken
                val org = state.organization        // ✅ fixed
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
            is UiState.Error -> {

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
            snackbarHost={SnackbarHost(snackbarState)}
        )
        { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFf5f5f5))

                    // ✅ Scrolls instead of clipping fields on short screens, and
                    // pushes content above the keyboard instead of letting it
                    // cover the password field.
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AppLogo()
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LoginScreenTitle()
                }
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    Modifier.padding(20.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .border(
                                width = 2.dp,
                                color = whiteBg,
                                shape = RoundedCornerShape(15.dp)
                            ),
                        shape = RoundedCornerShape(15.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = whiteBg
                        ),
                    ) {
                        CardContentsLoginScreen(
                            navController,
                            activity,
                            authViewModel,
                            prefilledEmail = prefilledEmail
                        )
                        Spacer(Modifier.height(10.dp))
                        Spacer(Modifier.padding(bottom = 30.dp))

                    }
                }
            }
        }

        LaunchedEffect(resetSuccessMessage) {
            if (resetSuccessMessage.isNotBlank()) {
                successMessage = resetSuccessMessage
            }
        }

        DynamicIslandSuccess(
            modifier = Modifier.align(Alignment.TopCenter),
            message = successMessage,
            onDismiss = { successMessage = null }
        )
    }

}