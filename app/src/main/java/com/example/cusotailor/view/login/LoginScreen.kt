package com.example.cusotailor.view.login

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.ui.Alignment
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.cusotailor.view.composable.AppLogo
import com.example.cusotailor.view.composable.CardContentsLoginScreen
import com.example.cusotailor.view.composable.LoginScreenTitle
import com.example.cusotailor.view.composable.SignUpText
import com.example.cusotailor.viewmodel.UiState
import com.example.cusotailor.viewmodel.Authenticate


@Composable
fun LoginScreen(activity: Activity,
                navController: NavController,
                onloginSuccess: (String)-> Unit,
                authViewModel: Authenticate= hiltViewModel()

) {

    val authState by authViewModel.accountState.collectAsState()
    var isLoginMode by remember{ mutableStateOf(true) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val snackbarState = remember { SnackbarHostState() }

    LaunchedEffect(authState) {
        when (val state = authState){
            is UiState.LoginSuccess ->{
                onloginSuccess("${ state.firstName } ${state.lastName}")
                authViewModel.resetState()
            }
            is UiState.Error ->{

            }
            is UiState.RegisterSuccess -> {
                snackbarState.showSnackbar("Account created! Please log in.")
                isLoginMode = true
                username = ""
                password = ""
                authViewModel.resetState()
            }

            else->{}
        }
    }

    var snackbarMessage by remember { mutableStateOf<String?>(null) }
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
            AppLogo()
            LoginScreenTitle()
            Spacer(modifier = Modifier.height(24.dp))
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
                CardContentsLoginScreen(navController,activity,authViewModel)
                Spacer(Modifier.height(10.dp))
                SignUpText(navController)
                Spacer(Modifier.padding(bottom = 30.dp))

            }
        }
    }

}

