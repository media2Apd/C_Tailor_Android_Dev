package com.example.cusotailor.view.composable

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.cusotailor.viewmodel.UiState

@Composable
fun CardContentsForgotPassword(navController: NavController,activity: Activity) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var submittedEmail by remember { mutableStateOf("") }
    var isSubmitted by remember { mutableStateOf(false) }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var accountState by remember { mutableStateOf("") }

    Column(
        Modifier.padding(25.dp)
    ) {
        AnimatedErrorBanner(
            message = if (accountState is UiState.Error)
                (accountState as UiState.Error).message
            else "",
            visible = accountState is UiState.Error
        )
        if(!isSubmitted) {
            Text("Forgot Password",fontSize=20.sp,color=Color.Black)
            Spacer(Modifier.padding(top = 10.dp))
            Text("Enter your email to receive a password code",color=Color.DarkGray)
            Spacer(Modifier.padding(top = 20.dp))
            Text("Email",fontSize=20.sp,color=Color.Black)
            Spacer(Modifier.padding(top = 10.dp))



            OutlinedTextField(

                value = email,
                onValueChange = { email = it },
                placeholder = { Text("your@email.com", color = Color.Gray) },
                textStyle = TextStyle(
                    color = Color.Black
                ),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.LightGray,
                    unfocusedIndicatorColor = Color.LightGray,
                    focusedLabelColor = Color.Black,
                    cursorColor = Color.Black,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Email,
                        contentDescription = "Email Icon", tint = Color.LightGray
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
            )

            Spacer(Modifier.padding(top = 20.dp))


            Button(
                onClick = {

                },
                modifier = Modifier
                    .fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Blue,
                    contentColor = Color.White,
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    "Send Reset Code", Modifier
                        .padding(bottom = 0.dp), color = Color.White, fontSize = 20.sp
                )
            }
            Spacer(Modifier.padding(top = 20.dp))
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,null,tint=Color.Blue
                )
                Text("Back to Sign In",
                    Modifier
                        .clickable{
                            navController.navigate("login")
                        },
                    fontSize=17.sp,
                    color=Color.Blue)
            }


        }
        if (isSubmitted) {
            Text("",fontSize=20.sp,color=Color.Black)
            Spacer(Modifier.padding(top = 10.dp))
            Text("Enter your email to receive a password code",color=Color.DarkGray)
            Spacer(Modifier.padding(top = 20.dp))
            Text("Email",fontSize=20.sp,color=Color.Black)
            Spacer(Modifier.padding(top = 10.dp))
            OtpInput(
                activity = activity,
                onOtpComplete = { otp ->
                    Log.d("OTP", otp)
                }
            )

        }

        Spacer(Modifier.padding(top=10.dp))
        if (isSubmitted){
            Row() {
                Column() {
                    Row(
                        Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Password",color=Color.Black)
                        Spacer(Modifier.weight(1f))
                        Text("Forgot Password?", Modifier, color = Color.Blue, fontSize = 14.sp)
                    }


                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text("Password",color=Color.LightGray) },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = if (isPasswordVisible) "Hide password" else "Show password", tint = Color.LightGray
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors =customFieldColors()
                    )
                }

            }

            Spacer(Modifier.padding(top=10.dp))

            Text("Sign in with OTP",
                Modifier
                    .clickable {
                        navController.navigate("otp/${submittedEmail}")

                    },color=Color.Blue, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))


    }
}

