package com.example.cusotailor.view.forgot_password

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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import com.example.cusotailor.view.composable.AppLogo
import com.example.cusotailor.view.composable.ForgotPasswordText
import com.example.cusotailor.view.composable.CardContentsForgotPassword



@Composable
fun ForgotPassword(activity: Activity,
                navController: NavController

) {


        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AppLogo()
            ForgotPasswordText()
            Spacer(modifier = Modifier.height(24.dp))
            Card(
                modifier = Modifier
                    .padding(20.dp)
                    .border(
                        width = 2.dp,
                        color = Color.White,
                        shape = RoundedCornerShape(12.dp)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
            ) {
                CardContentsForgotPassword(navController,activity)
            }
        }
    }


