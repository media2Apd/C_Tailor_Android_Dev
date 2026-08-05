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
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.CusoAppLogo
import com.cuso.mobile.view.composable.ForgotOtpSelection

@Suppress("UNUSED_PARAMETER")
@Composable
fun VerifyForgotPassword(navController: NavController, activity: Activity, submittedEmail: String) {
//    val authViewModel: Authenticate = hiltViewModel()
//    val accountState by authViewModel.accountState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFf5f5f5)) // ✅ matches ForgotUserPassword's page background
            // ✅ Scrolls instead of clipping the OTP boxes/button on short
            // screens, and clears the keyboard when the OTP field is focused.
            .verticalScroll(rememberScrollState())
            .imePadding(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CusoAppLogo()

        // ✅ Title styled the same as ForgotUserPassword's heading
        Text(
            text = "Verify OTP",
            fontSize = 21.39.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Enter the code sent to $submittedEmail",
            fontSize = 12.47.sp,
            color = Color(0xFF6B7280)
        )
        Spacer(modifier = Modifier.height(10.dp))

        // ✅ Card wrapped in a Box(padding 20dp) with a 2dp white border and
        // 15dp corner radius — same treatment as ForgotUserPassword's card.
        Box(
            Modifier.padding(20.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
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
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(28.dp)
                ) {
                    ForgotOtpSelection(navController, activity = activity, submittedEmail)
                }
            }
        }
    }
}