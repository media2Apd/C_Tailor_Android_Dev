package com.cuso.mobile.view.composable

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.navigation.NavController
import com.cuso.mobile.viewmodel.Authenticate
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.text.style.TextOverflow


@Composable
fun LoginOtpSelection(
    navController: NavController,
    activity: Activity,
    submittedEmail: String
) {
    //disables back button
    BackHandler(enabled = true) {
    }
    val authViewModel: Authenticate = hiltViewModel()

    var otp by remember { mutableStateOf("") }
    val verifyResult by authViewModel.otpVerifyResult.observeAsState()
    var isOtpComplete by remember { mutableStateOf(false) }
    var savedEmail by rememberSaveable { mutableStateOf(submittedEmail) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .border(2.dp, Color.LightGray, shape = RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(color = Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.PersonOutline,
                    contentDescription = null,
                    tint = Color.DarkGray,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "OTP sent to",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
                Text(
                    text = savedEmail,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = "Change",
                modifier = Modifier.clickable {
                    navController.navigate("login")
                },
                color = Color.Blue,
                fontSize = 16.sp
            )
        }
    }
    Spacer(Modifier.padding(top = 20.dp))
    Column {
        Text("Enter Otp", color = Color.Black)
    }
    Spacer(Modifier.padding(top = 10.dp))
    LoginOtpInput(
        activity = activity,
        onOtpComplete = { enteredOtp ->
            otp = enteredOtp
            isOtpComplete = true
        }
    )
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Sign in using password",
            Modifier
                .clickable {
                    navController.navigate("login-with-email/${savedEmail}") {
                        popUpTo(0) { true }
                    }
                },
            color = Color.Blue, fontSize = 14.sp
        )
        Spacer(Modifier.weight(1f))
        ResendLoginOtpSection(onResendClick = { savedEmail }, email = savedEmail, authViewModel)
    }

    Spacer(Modifier.padding(top = 10.dp))

    // ⬇️ replaced block
    LaunchedEffect(verifyResult) {
        verifyResult?.let { result ->
            result.onSuccess { response ->

                val org = response.data.user.organizationId

                if (org.orgSetupComplete) {
                    navController.navigate("home") {
                        popUpTo(0) { inclusive = true }
                    }
                } else {
                    navController.navigate("org") {
                        popUpTo(0) { inclusive = true }
                    }
                }

            }.onFailure { error ->
                Log.e("OTP_API", "Login OTP verification failed", error)
            }
        }
    }

    Button(
        onClick = {
            if (isOtpComplete) {
                authViewModel.verifyOtp(
                    savedEmail, otp
                )
            }
        },
        enabled = isOtpComplete,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Blue,
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = "Verify and continue",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
    Spacer(Modifier.padding(top = 10.dp))
    BackToSignIn(navController)
}