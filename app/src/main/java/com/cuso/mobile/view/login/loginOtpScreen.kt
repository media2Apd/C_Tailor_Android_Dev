package com.cuso.mobile.view.login

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.cuso.mobile.view.composable.LoginOtpSelection
import com.cuso.mobile.view.composable.OtpTitle
import com.cuso.mobile.view.composable.AppLogo

@Composable
fun LoginOtpScreen(navController: NavController, activity: Activity, submittedEmail: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            // ✅ Scrolls instead of clipping the OTP boxes/button on short
            // screens, and clears the keyboard when the OTP field is focused.
            .verticalScroll(rememberScrollState())
            .imePadding(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AppLogo()
        OtpTitle()
        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier
                // ❌ was height(500.dp) — a fixed height card has no idea
                // how tall the device screen actually is, so on a shorter
                // screen it just ran off the bottom. fillMaxWidth() lets it
                // size to its actual content instead.
                .fillMaxWidth()
                .padding(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                Modifier.padding(30.dp)
            ) {
                LoginOtpSelection(navController, activity = activity, submittedEmail)
            }


        }
    }
}