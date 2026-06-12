package com.cuso.mobile.view.forgot_password

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.cuso.mobile.view.composable.LoginOtpSelection
import com.cuso.mobile.view.composable.otpTitle
import com.cuso.mobile.view.composable.appLogo
import com.cuso.mobile.view.composable.otpTitle
import com.cuso.mobile.viewmodel.Authenticate
import com.cuso.mobile.view.composable.forgotOtpSelection

@Composable
fun verifyForgotPassword(navController: NavController,activity: Activity,submittedEmail:String){
    var authViewModel: Authenticate= hiltViewModel()
    val accountState by authViewModel.accountState.collectAsState()

    var submittedEmail=
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            appLogo()
            Spacer(Modifier.padding(top = 20.dp))
            otpTitle()
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.height(500.dp)
                    .padding(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    Modifier.padding(30.dp)
                ) {
                    forgotOtpSelection(navController, activity = activity,submittedEmail)
                }


            }
        }
}
