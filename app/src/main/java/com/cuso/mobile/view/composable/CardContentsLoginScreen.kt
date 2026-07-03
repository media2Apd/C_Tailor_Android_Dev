    package com.cuso.mobile.view.composable

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
    import androidx.compose.material3.CircularProgressIndicator
    import androidx.compose.material3.DividerDefaults
    import androidx.compose.material3.HorizontalDivider
    import androidx.compose.material3.Icon
    import androidx.compose.material3.IconButton
    import androidx.compose.material3.OutlinedTextField
    import androidx.compose.material3.Text
    import androidx.compose.material3.TextFieldDefaults
    import androidx.compose.runtime.Composable
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
    import androidx.compose.ui.draw.clip
    import androidx.compose.ui.graphics.Color
    import androidx.compose.ui.semantics.contentType
    import androidx.compose.ui.semantics.semantics
    import androidx.compose.ui.text.TextStyle
    import androidx.compose.ui.text.font.FontWeight
    import androidx.compose.ui.text.input.KeyboardType
    import androidx.compose.ui.text.input.PasswordVisualTransformation
    import androidx.compose.ui.text.input.VisualTransformation
    import androidx.compose.ui.text.style.TextDecoration
    import androidx.compose.ui.text.style.TextOverflow
    import androidx.compose.ui.unit.dp
    import androidx.compose.ui.unit.sp
    import androidx.navigation.NavController
    import com.cuso.mobile.viewmodel.Authenticate
    import com.cuso.mobile.viewmodel.UiState


    @Composable
    fun CardContentsLoginScreen(navController: NavController,
                                activity: Activity,
                                authViewModel: Authenticate,
                                prefilledEmail:String="")
    {

        val accountState by authViewModel.accountState.collectAsState()
        var email by rememberSaveable { mutableStateOf(prefilledEmail) }
        var password by remember { mutableStateOf("") }
        var submittedEmail by rememberSaveable { mutableStateOf(prefilledEmail) }
        var isSubmitted by remember { mutableStateOf(prefilledEmail.isNotBlank()) }
        var isPasswordVisible by remember { mutableStateOf(false) }
        var showEmailNotFound by remember { mutableStateOf(false) }

        Column(
            Modifier.padding(25.dp)
        ) {
            Text("Email", fontSize = 16.sp,color=Color.Black)
            if(!isSubmitted) {
                OutlinedTextField(
                    singleLine = true,
                    value = email,
                    onValueChange = { email = it
                                    authViewModel.resetState()
                                    showEmailNotFound=false},
                    placeholder = { Text("..", color = Color.Gray) },
                    textStyle = TextStyle(
                        color = Color.Black
                    ),
                    isError = accountState is UiState.Error||accountState is UiState.EmailNotFound,

                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Blue,
                        unfocusedIndicatorColor = Color.Gray,
                        focusedLabelColor = Color.Black,
                        cursorColor = Color.Black,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        errorContainerColor = Color.White,   // ← add this
                        errorIndicatorColor = Color.Red,     // ← add this
                        errorCursorColor = Color.Black

                    ),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Email,
                            contentDescription = "Email Icon", tint = Color.LightGray
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .semantics { contentType = ContentType.EmailAddress }
                )

                if (accountState is UiState.Error) {
                    Text(
                        text = (accountState as UiState.Error).message,
                        color = Color.Red,
                        fontSize = 14.sp,
                        modifier = Modifier
                    )
                }
                Spacer(Modifier.padding(top=15.dp))
                if (showEmailNotFound) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFFF5F5))
                            .border(1.dp, Color(0xFFFFF5F5), RoundedCornerShape(8.dp))
                            .padding(12.dp)

                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "We couldn't find an account with that email.",
                                color = Color(0xFFCC0000),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {

                                Text(
                                    text = "Sign up here ",
                                    modifier = Modifier.clickable { navController.navigate("signup") },
                                    color = Color(0xFF0047CC),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    textDecoration = TextDecoration.Underline
                                )
                                Text(
                                    text = "if you are new ",
                                    color = Color(0xFFCC0000),
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }

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
                                color = Color.Black,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
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
                                    navController.navigate("new-pass")
                                },
                                color = Color.Blue, fontSize = 14.sp)
                        }


                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it
                                authViewModel.resetState() },
                            textStyle=TextStyle(color=Color.Black),
                            placeholder = { Text("..",color=Color.Black) },
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
                            modifier = Modifier.fillMaxWidth()
                                .semantics { contentType = ContentType.Password },
                            isError = accountState is UiState.Error,
                            colors =CustomFieldColors()
                        )
                        if (accountState is UiState.Error) {
                            Text(
                                text = (accountState as UiState.Error).message,
                                color = Color.Red,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(start = 4.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                }

                Spacer(Modifier.padding(top=10.dp))

                Text("Sign in with OTP",
                    Modifier
                        .clickable {
                            authViewModel.sendOtp(email)
                            navController.navigate("login-otp/${submittedEmail}")

                        },color=Color.Blue, fontSize = 14.sp)
            }

            LaunchedEffect(accountState) {
                when (accountState) {

                    is UiState.EmailVerified -> {
                        submittedEmail = email
                        isSubmitted = true
                    }
                    is UiState.EmailNotFound->{
                        showEmailNotFound=true
                    }


                    else -> {}
                }
            }

            Button(
                onClick = {
                    if(!isSubmitted) {
                        authViewModel.verifyEmail(email)
                    }
                    else{
                        authViewModel.login(
                            email = email,
                            password=password
                        )
                    }
//                    else {
//                        navController.navigate("")
//                    }
                },
                modifier = Modifier

                    .fillMaxWidth(),

                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Blue,
                    contentColor = Color.White,


                    ), shape = RoundedCornerShape(8.dp)

            ) {
                if (accountState is UiState.Loading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        "Continue",
                        Modifier.padding(bottom = 0.dp),
                        color = Color.White,
                        fontSize = 20.sp
                    )
                }
            }
            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),  // takes equal remaining space
                    thickness = DividerDefaults.Thickness,
                    color = Color.Gray
                )

                Text(
                    text = "Or",
                    modifier = Modifier.padding(horizontal = 12.dp),
                    color = Color.Gray
                )

                HorizontalDivider(
                    modifier = Modifier.weight(1f),  // takes equal remaining space
                    thickness = DividerDefaults.Thickness,
                    color = Color.Gray
                )
            }
            Spacer(Modifier.height(20.dp))

            Row {
                ContinueWithGoogle(activity,navController)
            }
        }
    }
