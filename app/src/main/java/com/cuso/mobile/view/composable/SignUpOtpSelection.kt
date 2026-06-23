package com.cuso.mobile.view.composable

import android.app.Activity
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
import androidx.compose.material3.CircularProgressIndicator
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
fun SignUpOtpSelection(
    navController: NavController,
    activity: Activity,
    submittedEmail: String
) {
    //disables back button
    BackHandler(enabled = true) {
    }
    val authViewModel: Authenticate = hiltViewModel()

    var otp by remember { mutableStateOf("") }
    val verifyResult by authViewModel.registerOtpVerifyResult.observeAsState()
    var isOtpComplete by remember { mutableStateOf(false) }
    var savedEmail by rememberSaveable { mutableStateOf(submittedEmail) }

    // New state: tracks an in-flight verify call and any error to show the user
    var isVerifying by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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
            // Circular icon
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

            // Text block takes remaining space
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
                    overflow=TextOverflow.Ellipsis
                )
            }

            // Change button pinned to end
            Text(
                text = "Change",
                modifier = Modifier.clickable {
                    navController.navigate("register") // adjust to your signup entry route
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
            // Clear any stale error once the user starts a fresh OTP entry
            errorMessage = null
        }
    )

    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Alignment.End.let { androidx.compose.foundation.layout.Arrangement.End }
    ) {
        ResendLoginOtpSection(onResendClick = { savedEmail }, email = savedEmail, authViewModel)
    }

    Spacer(Modifier.padding(top = 10.dp))

    // Inline error feedback, shown after a failed verify attempt
    errorMessage?.let { message ->
        Text(
            text = message,
            color = Color.Red,
            fontSize = 14.sp
        )
        Spacer(Modifier.padding(top = 8.dp))
    }

    LaunchedEffect(verifyResult) {
        verifyResult?.let { result ->
            isVerifying = false
            if (result.isSuccess) {
                errorMessage = null
                navController.navigate("org") {
                    popUpTo(0) { inclusive = true }
                }
            } else {
                // Surface the failure and force a fresh OTP, since a once-rejected
                // code is typically invalidated server-side and retrying it again
                // will just fail the same way.
                errorMessage = "Invalid or expired OTP. Please request a new one."
                otp = ""
                isOtpComplete = false
            }
        }
    }

    Button(
        onClick = {
            if (isOtpComplete && !isVerifying) {
                isVerifying = true
                errorMessage = null
                authViewModel.RegisterVerifyOtp(
                    savedEmail, otp
                )
            }
        },
        enabled = isOtpComplete && !isVerifying,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Blue,
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        if (isVerifying) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = "Verify and continue",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
    Spacer(Modifier.padding(top = 10.dp))
    backToSignIn(navController)
}