package com.example.cusotailor.view.composable

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.navigation.NavController
import com.example.cusotailor.viewmodel.Authenticate

@Composable
fun LoginOtpSelection(navController: NavController,
                      activity: Activity,
                      submittedEmail: String

){
    val authViewModel:Authenticate= hiltViewModel()

    var submittedPassword by remember{mutableStateOf("")}
    var isSubmitted by remember { mutableStateOf(false) }
    var otp by remember { mutableStateOf("") }
    val verifyResult by authViewModel.otpVerifyResult.observeAsState()
    var isOtpComplete by remember { mutableStateOf(false) }

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
                    text = submittedEmail,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
            }

            // Change button pinned to end
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
    Column() {
        Text("Enter Otp", color = Color.Black)
    }
    Spacer(Modifier.padding(top = 10.dp))
    OtpInput(
        activity = activity,
        onOtpComplete = { enteredOtp ->
            otp=enteredOtp
            isOtpComplete=true
        }
    )
    Spacer(Modifier.padding(top=10.dp))

    Text("Sign in using password",
        Modifier
            .clickable{
                navController.navigate("login?isSubmitted=true")
            },
        color=Color.Blue, fontSize = 14.sp)

    Spacer(Modifier.padding(top=10.dp))

    Button(
        onClick = {
            authViewModel.verifyOtp(
                submittedEmail,otp
            )
        },
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
}
