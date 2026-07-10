//package com.cuso.mobile.view.composable
//
//import android.app.Activity
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.text.KeyboardOptions
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Visibility
//import androidx.compose.material.icons.filled.VisibilityOff
//import androidx.compose.material3.Button
//import androidx.compose.material3.ButtonDefaults
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.OutlinedTextField
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.input.KeyboardType
//import androidx.compose.ui.text.input.PasswordVisualTransformation
//import androidx.compose.ui.text.input.VisualTransformation
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.navigation.NavController
//
//@Composable
//fun CardContentsResetPassword(navController: NavController,activity: Activity) {
//    var email by remember { mutableStateOf("") }
//    var password by remember { mutableStateOf("") }
//    var newPassword by remember { mutableStateOf("") }
//    var confirmPassword by remember { mutableStateOf("") }
//    var isNewPasswordVisible by remember { mutableStateOf(false) }
//    var isConfirmPasswordVisible by remember { mutableStateOf(false) }
//
//    Column(
//        Modifier.padding(25.dp)
//    ) {
//        Text(
//            "Create New Password",
//            fontSize = 20.sp,
//            color = Color.Black
//        )
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        OutlinedTextField(
//            value = newPassword,
//            onValueChange = { newPassword = it },
//            placeholder = { Text("..", color = Color.LightGray) },
//            visualTransformation =
//                if (isNewPasswordVisible) VisualTransformation.None
//                else PasswordVisualTransformation(),
//            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
//            trailingIcon = {
//                IconButton(
//                    onClick = { isNewPasswordVisible = !isNewPasswordVisible }
//                ) {
//                    Icon(
//                        imageVector = if (isNewPasswordVisible)
//                            Icons.Default.Visibility
//                        else
//                            Icons.Default.VisibilityOff,
//                        contentDescription = null,
//                        tint = Color.LightGray
//                    )
//                }
//            },
//            modifier = Modifier.fillMaxWidth(),
//            colors = CustomFieldColors()
//        )
//
//        Spacer(modifier = Modifier.height(12.dp))
//
//        OutlinedTextField(
//            value = confirmPassword,
//            onValueChange = { confirmPassword = it },
//            placeholder = { Text("..", color = Color.LightGray) },
//            visualTransformation =
//                if (isConfirmPasswordVisible) VisualTransformation.None
//                else PasswordVisualTransformation(),
//            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
//            trailingIcon = {
//                IconButton(
//                    onClick = {
//                        isConfirmPasswordVisible = !isConfirmPasswordVisible
//                    }
//                ) {
//                    Icon(
//                        imageVector = if (isConfirmPasswordVisible)
//                            Icons.Default.Visibility
//                        else
//                            Icons.Default.VisibilityOff,
//                        contentDescription = null,
//                        tint = Color.LightGray
//                    )
//                }
//            },
//            modifier = Modifier.fillMaxWidth(),
//            colors = CustomFieldColors()
//        )
//
//        Spacer(modifier = Modifier.height(20.dp))
//
//        Button(
//            onClick = {
//                if (newPassword == confirmPassword) {
//                    // Call reset password API
//                }
//            },
//            modifier = Modifier.fillMaxWidth(),
//            shape = RoundedCornerShape(8.dp),
//            colors = ButtonDefaults.buttonColors(
//                containerColor = Color.Blue,
//                contentColor = Color.White
//            )
//        ) {
//            Text(
//                text = "Reset Password",
//                fontSize = 18.sp
//            )
//        }
//
//    }
//}
//
