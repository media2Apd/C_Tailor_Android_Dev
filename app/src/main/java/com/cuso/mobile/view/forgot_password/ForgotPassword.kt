package com.cuso.mobile.view.forgot_password

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import com.cuso.mobile.view.composable.AppLogo
import com.cuso.mobile.view.composable.CardContentsForgotPassword
import com.cuso.mobile.view.composable.ForgotPasswordText


@Composable
fun ForgotUserPassword(activity: Activity,
                   navController: NavController

) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            // ✅ This screen can show an email field, then expand into an
            // OTP input plus a password field once submitted — without
            // scroll + imePadding that extra content pushed past the
            // bottom of shorter screens or got covered by the keyboard.
            .verticalScroll(rememberScrollState())
            .imePadding(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AppLogo()
        Spacer(Modifier.padding(top = 20.dp))
        ForgotPasswordText()
        Spacer(modifier = Modifier.height(24.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
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