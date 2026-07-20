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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.cuso.mobile.view.composable.AppLogo
import com.cuso.mobile.view.composable.CardContentsForgotPassword

@Composable
fun ForgotUserPassword(
    activity: Activity,
    navController: NavController,
    prefilledEmail: String = "" // ✅ passed in from the previous screen (e.g. login)
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFf5f5f5)) // ✅ matches LoginScreen's page background
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
        Spacer(modifier = Modifier.height(10.dp))

        // ✅ Title styled like LoginScreenTitle / "Reset Password" heading
        Text(
            text = "Forgot Password",
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Enter your email to receive a reset code",
            fontSize = 14.sp,
            color = Color(0xFF6B7280)
        )
        Spacer(modifier = Modifier.height(10.dp))

        // ✅ Card wrapped in a Box(padding 20dp) with a 2dp white border and
        // 15dp corner radius — same treatment as LoginScreen's card.
        Box(
            Modifier.padding(20.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 2.dp,
                        color = Color.White,
                        shape = RoundedCornerShape(15.dp)
                    ),
                shape = RoundedCornerShape(15.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(28.dp)
                ) {
                    CardContentsForgotPassword(
                        navController = navController,
                        activity = activity,
                        prefilledEmail = prefilledEmail
                    )
                }
            }
        }
    }
}