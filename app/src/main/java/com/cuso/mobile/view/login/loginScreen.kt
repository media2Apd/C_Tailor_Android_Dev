package com.cuso.mobile.view.login

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cuso.mobile.model.Organization
import com.cuso.mobile.view.composable.cardContentsLoginScreen
import com.cuso.mobile.view.composable.signUpText
import com.cuso.mobile.view.composable.appLogo
import com.cuso.mobile.view.composable.cardContentsLoginScreen
import com.cuso.mobile.view.composable.loginScreenTitle
import com.cuso.mobile.view.composable.signUpText
import com.cuso.mobile.viewmodel.UiState
import com.cuso.mobile.viewmodel.Authenticate
import kotlinx.coroutines.delay


@Composable
fun loginScreen(activity: Activity,
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
    var showBanner by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }



    BackHandler {
        showExitDialog = true
    }
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            containerColor = Color.White,
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
                .background(Color.White),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            appLogo()
            Spacer(modifier = Modifier.height(10.dp))
            loginScreenTitle()
            Spacer(modifier = Modifier.height(10.dp))
            Card(
                modifier = Modifier
                    .padding(20.dp)
                    .border(
                        width = 2.dp,
                        color = Color.White,
                        shape = RoundedCornerShape(12.dp)
                    ),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
            ) {
                cardContentsLoginScreen(
                    navController,
                    activity,
                    authViewModel,
                    prefilledEmail=prefilledEmail
                )
                Spacer(Modifier.height(10.dp))
                signUpText(navController)
                Spacer(Modifier.padding(bottom = 30.dp))

            }
        }
    }

    LaunchedEffect(resetSuccessMessage) {
        if (resetSuccessMessage.isNotBlank()) {
            showBanner = true
            delay(3000) // auto hide after 3 seconds
            showBanner = false
        }
    }

        AnimatedVisibility(
            visible = showBanner,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 40.dp) // 👈 gap from top edge
        ) {
            Box(
                modifier = Modifier
                    .wrapContentWidth()
                    .background(
                        color = Color(0xFF1A1A1A), // 👈 dark like dynamic island
                        shape = RoundedCornerShape(50.dp) // 👈 pill shape
                    )
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF22C55E),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = resetSuccessMessage,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

}

