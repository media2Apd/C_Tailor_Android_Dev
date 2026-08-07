package com.cuso.mobile.view.composable

import android.app.Activity import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation.NavController
import com.cuso.mobile.viewmodel.Authenticate
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import com.cuso.mobile.ui.theme.blackTitle
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.viewmodel.UiState

@Composable
fun ForgotOtpSelection(
    navController: NavController,
    activity: Activity,
    submittedEmail: String
) {
    BackHandler(enabled = true) {}
    val authViewModel: Authenticate = hiltViewModel()

    var otp by remember { mutableStateOf("") }
    var isOtpComplete by remember { mutableStateOf(false) }
    var savedEmail by rememberSaveable { mutableStateOf(submittedEmail) }

    //  Observe accountState
    val accountState by authViewModel.accountState.collectAsState()
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text("Enter your OTP",color=blackTitle, fontSize = 12.47.sp, fontWeight = FontWeight.Bold)

    }
    Spacer(Modifier.padding(top=10.dp))
    //  React to state changes
    LaunchedEffect(accountState) {
        when (accountState) {
            is UiState.ForgotPasswordVerified -> {
                val resetToken = (accountState as UiState.ForgotPasswordVerified).resetToken
                navController.navigate("reset-pass/$resetToken")
            }
            is UiState.Error -> {
            }
            else -> {}
        }
    }
    ForgotOtpInput(
        activity = activity,
        onOtpComplete = { enteredOtp ->
            otp=enteredOtp
            isOtpComplete=true
        }
    )
    ResendForgotOtpSection(onResendClick = { savedEmail }, email = savedEmail, authViewModel,savedEmail=savedEmail,otp)

    // ... existing UI ...

    Button(
        onClick = {
            if (isOtpComplete) {
                authViewModel.verifyForgotPasswordOtp(savedEmail, otp)
            }
        },
        enabled = isOtpComplete && accountState !is UiState.Loading,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Blue,
            contentColor = whiteBg
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        if (accountState is UiState.Loading) {
            CirculerProgressIndicatorSmall()
        } else {
            Text(
                text = "Verify and continue",
                fontSize = 14.26.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

    }
    Spacer(Modifier.padding(top = 15.dp))
    BackToSignIn(navController)
}