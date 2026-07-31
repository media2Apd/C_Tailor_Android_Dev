package com.cuso.mobile.view.composable

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.cuso.mobile.ui.theme.Primary
import com.cuso.mobile.ui.theme.PrimaryTextColor
import com.cuso.mobile.viewmodel.Authenticate
import com.cuso.mobile.viewmodel.UiState

@Composable
fun CardContentsForgotPassword(
    navController: NavController,
    activity: Activity,
    prefilledEmail: String = ""
) {
    val authViewModel: Authenticate = hiltViewModel()

    // ✅ Email now comes in prefilled from the previous screen (e.g. login).
    // isEmailLocked tracks whether it should stay read-only.
    var email by rememberSaveable { mutableStateOf(prefilledEmail) }
    val isEmailLocked = prefilledEmail.isNotBlank()
    var password by remember { mutableStateOf("") }
    var submittedEmail by remember { mutableStateOf("") }
    var isSubmitted by remember { mutableStateOf(false) }
    var isPasswordVisible by remember { mutableStateOf(false) }
    val forgotPasswordState by authViewModel.forgotPasswordState.collectAsState()
    val uiState by authViewModel.forgotPasswordState.collectAsState()

    val emailInteractionSource = remember { MutableInteractionSource() }
    val passwordInteractionSource = remember { MutableInteractionSource() }
    val isError = forgotPasswordState is UiState.Error

    Column(
        Modifier
            .fillMaxWidth()
    ) {
        if (!isSubmitted) {
//            Text(
//                text = "Forgot Password",
//                fontSize = 20.sp,
//                color = Color.Black,
//                fontWeight = FontWeight.Bold
//            )
//            Spacer(Modifier.height(6.dp))
//            Text(
//                text = "Enter your email to receive a password code",
//                fontSize = 14.sp,
//                color = Color(0xFF6B7280)
//            )
//            Spacer(Modifier.height(15.dp))

            Text("Email", fontSize = 14.sp, color = Color(0xFF374151), fontWeight = FontWeight.Bold)
            Spacer(Modifier.padding(top = 5.dp))

            // ✅ Same email box as CardContentsLoginScreen, but readOnly + dimmed
            // when it arrived prefilled — user can see it but can't edit it.
            CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
                BasicTextField(
                    value = email,
                    onValueChange = { if (!isEmailLocked) email = it },
                    readOnly = isEmailLocked,
                    singleLine = true,
                    textStyle = TextStyle(
                        color = if (isEmailLocked) Color(0xFF6B7280) else Color.Black,
                        fontSize = 13.sp
                    ),
                    interactionSource = emailInteractionSource,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .semantics { contentType = ContentType.EmailAddress },
                    decorationBox = { innerTextField ->
                        OutlinedTextFieldDefaults.DecorationBox(
                            value = email,
                            innerTextField = innerTextField,
                            enabled = !isEmailLocked,
                            singleLine = true,
                            visualTransformation = VisualTransformation.None,
                            interactionSource = emailInteractionSource,
                            isError = isError,
                            placeholder = {
                                Text(
                                    "your@email.com",
                                    color = PrimaryTextColor,
                                    style = TextStyle(fontSize = 13.sp)
                                )
                            },
                            leadingIcon = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.Filled.Email,
                                        contentDescription = "Email Icon",
                                        tint = PrimaryTextColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            },
                            colors = customFieldOutlinedColors(),
                            contentPadding = PaddingValues(
                                start = 12.dp,
                                end = 12.dp,
                                top = 0.dp,
                                bottom = 0.dp
                            ),
                            container = {
                                OutlinedTextFieldDefaults.Container(
                                    enabled = !isEmailLocked,
                                    isError = isError,
                                    interactionSource = emailInteractionSource,
                                    colors = customFieldOutlinedColors(),
                                    shape = OutlinedTextFieldDefaults.shape
                                )
                            }
                        )
                    }
                )
            }

            if (isError) {
                Text(
                    text = (forgotPasswordState as UiState.Error).message,
                    color = Color.Red,
                    fontSize = 14.sp
                )
            }

            Spacer(Modifier.height(20.dp))

            LaunchedEffect(forgotPasswordState) {
                if (forgotPasswordState is UiState.Success) {
                    navController.navigate("verify-forgot-pass/$submittedEmail")
                }
            }

            // ✅ Same button as login: 40dp height, RoundedCornerShape(5.dp),
            // Color(0xFF2563eb) fill, 16sp SemiBold label.
            CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
                Button(
                    onClick = {
                        if (email.isNotBlank()) {
                            submittedEmail = email
                            authViewModel.forgotPasswordOtp(email)
                        }
                    },
                    enabled = email.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(5.dp),
                    contentPadding = PaddingValues(vertical = 0.dp, horizontal = 16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                ) {
                    if (uiState is UiState.Loading) {
                        CirculerProgressIndicatorSmall()
                    } else {
                        Text(
                            text = "Send Reset Code",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            BackToSignIn(navController)
        }

        if (isSubmitted) {
            Text(
                text = "Enter your email to receive a password code",
                fontSize = 14.sp,
                color = Color(0xFF6B7280)
            )
            Spacer(Modifier.height(15.dp))
            Text("Email", fontSize = 14.sp, color = Color(0xFF374151))
            Spacer(Modifier.padding(top = 5.dp))
            ForgotOtpInput(
                activity = activity,
                onOtpComplete = { otp ->
                    Log.d("OTP", otp)
                }
            )

            Spacer(Modifier.height(20.dp))

            Row {
                Column(Modifier.fillMaxWidth()) {
                    Text("Password", fontSize = 14.sp, color = Color(0xFF374151))
                    Spacer(Modifier.padding(top = 5.dp))

                    // ✅ Same password box as CardContentsLoginScreen.
                    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
                        BasicTextField(
                            value = password,
                            onValueChange = { password = it },
                            singleLine = true,
                            textStyle = TextStyle(color = Color.Black, fontSize = 13.sp),
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            interactionSource = passwordInteractionSource,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .semantics { contentType = ContentType.Password },
                            decorationBox = { innerTextField ->
                                OutlinedTextFieldDefaults.DecorationBox(
                                    value = password,
                                    innerTextField = innerTextField,
                                    enabled = true,
                                    singleLine = true,
                                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    interactionSource = passwordInteractionSource,
                                    isError = isError,
                                    placeholder = {
                                        Text(
                                            "Password",
                                            color = PrimaryTextColor,
                                            style = TextStyle(fontSize = 13.sp)
                                        )
                                    },
                                    leadingIcon = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Icon(
                                                imageVector = Icons.Filled.Lock,
                                                contentDescription = "Password Icon",
                                                tint = PrimaryTextColor,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    },
                                    trailingIcon = {
                                        IconButton(
                                            onClick = { isPasswordVisible = !isPasswordVisible },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                                contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                                                tint = Color.LightGray,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    },
                                    colors = customFieldOutlinedColors(),
                                    contentPadding = PaddingValues(
                                        start = 12.dp,
                                        end = 8.dp,
                                        top = 0.dp,
                                        bottom = 0.dp
                                    ),
                                    container = {
                                        OutlinedTextFieldDefaults.Container(
                                            enabled = true,
                                            isError = isError,
                                            interactionSource = passwordInteractionSource,
                                            colors = customFieldOutlinedColors(),
                                            shape = OutlinedTextFieldDefaults.shape
                                        )
                                    }
                                )
                            }
                        )
                    }

                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            "Forgot Password?",
                            modifier = Modifier
                                .clickable { navController.navigate("new-pass") }
                                .padding(bottom = 20.dp),
                            color = Color(0xFF0A42BE),
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Text(
                text = "Sign in with OTP",
                modifier = Modifier.clickable {
                    navController.navigate("login-otp/${submittedEmail}")
                },
                color = Color(0xFF0A42BE),
                fontSize = 14.sp
            )
            Spacer(Modifier.height(10.dp))
        }

        Spacer(Modifier.height(20.dp))
    }
}