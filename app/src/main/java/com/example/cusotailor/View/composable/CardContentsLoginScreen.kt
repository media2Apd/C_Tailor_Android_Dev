package com.example.cusotailor.View.composable

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController


@Composable
fun CardContentsLoginScreen(navController: NavController,activity: Activity) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var submittedEmail by remember { mutableStateOf("") }
    var isSubmitted by remember { mutableStateOf(false) }
    var isPasswordVisible by remember { mutableStateOf(false) }

    Column(
        Modifier.padding(25.dp)
    ) {
        if(!isSubmitted) {
            OutlinedTextField(

                value = email,
                onValueChange = { email = it },
                placeholder = { Text("your@email.com", color = Color.Gray) },
                textStyle = TextStyle(
                    color = Color.Black
                ),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Gray,
                    unfocusedIndicatorColor = Color.Gray,
                    focusedLabelColor = Color.Black,
                    cursorColor = Color.Black,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Email,
                        contentDescription = "Email Icon", tint = Color.Gray
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
        if (isSubmitted) {
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
                            text = "Sign in as",
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
                        Text("Forgot Password?", Modifier
                            .clickable{
                                navController.navigate("forgot")
                            },
                            color = Color.Blue, fontSize = 14.sp)
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
                        navController.navigate("login-otp/${submittedEmail}")

                    },color=Color.Blue, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                if(!isSubmitted) {
                    submittedEmail = email
                    isSubmitted = true
                }
                else {
                    navController.navigate("otp")
                }
            },
            modifier = Modifier

                .fillMaxWidth(),

            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Blue,
                contentColor = Color.White,


                ), shape = RoundedCornerShape(8.dp)

        ) {
            Text(
                "Continue", Modifier
                    .padding(bottom = 0.dp), color = Color.White, fontSize = 20.sp
            )
        }
        Spacer(Modifier.height(20.dp))

        Row(
            Modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            HorizontalDivider(
                Modifier.width(200.dp),
                DividerDefaults.Thickness,
                color = Color.Gray
            )

            Text("Or", Modifier, color = Color.Gray)
            HorizontalDivider(
                Modifier.width(200.dp),
                DividerDefaults.Thickness,
                color = Color.Gray
            )

        }
        Spacer(Modifier.height(20.dp))

        Row() {
            ContinueGoogle(activity)
        }
    }
}
