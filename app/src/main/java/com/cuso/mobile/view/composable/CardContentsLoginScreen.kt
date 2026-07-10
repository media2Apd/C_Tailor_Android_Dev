    package com.cuso.mobile.view.composable

    import android.app.Activity
    import androidx.compose.foundation.background
    import androidx.compose.foundation.border
    import androidx.compose.foundation.clickable
    import androidx.compose.foundation.interaction.MutableInteractionSource
    import androidx.compose.foundation.layout.Box
    import androidx.compose.foundation.layout.Column
    import androidx.compose.foundation.layout.PaddingValues
    import androidx.compose.foundation.layout.Row
    import androidx.compose.foundation.layout.Spacer
    import androidx.compose.foundation.layout.fillMaxSize
    import androidx.compose.foundation.layout.fillMaxWidth
    import androidx.compose.foundation.layout.height
    import androidx.compose.foundation.layout.padding
    import androidx.compose.foundation.layout.size
    import androidx.compose.foundation.layout.width
    import androidx.compose.foundation.shape.CircleShape
    import androidx.compose.foundation.shape.RoundedCornerShape
    import androidx.compose.foundation.text.BasicTextField
    import androidx.compose.foundation.text.KeyboardOptions
    import androidx.compose.material.icons.Icons
    import androidx.compose.material.icons.filled.Email
    import androidx.compose.material.icons.filled.Lock
    import androidx.compose.material.icons.filled.PersonOutline
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
    import androidx.compose.ui.draw.clip
    import androidx.compose.ui.graphics.Color
    import androidx.compose.ui.semantics.contentType
    import androidx.compose.ui.semantics.semantics
    import androidx.compose.ui.text.TextStyle
    import androidx.compose.ui.text.font.FontWeight
    import androidx.compose.ui.text.input.KeyboardType
    import androidx.compose.ui.text.input.PasswordVisualTransformation
    import androidx.compose.ui.text.input.VisualTransformation
    import androidx.compose.ui.text.style.TextOverflow
    import androidx.compose.ui.unit.dp
    import androidx.compose.ui.unit.sp
    import androidx.navigation.NavController
    import com.cuso.mobile.ui.theme.Primary
    import com.cuso.mobile.ui.theme.PrimaryBorder
    import com.cuso.mobile.ui.theme.PrimaryTextColor
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
        val interactionSource = remember { MutableInteractionSource() }
        val isError = accountState is UiState.Error || accountState is UiState.EmailNotFound
        val passwordInteractionSource = remember { MutableInteractionSource() }
        val isPasswordError = accountState is UiState.Error


        Column(
            Modifier.padding(25.dp)
        ) {
            Text("Email", fontSize = 14.sp,color=Color(0xFF374151))
            Spacer(Modifier.padding(top = 5.dp))
            if(!isSubmitted) {
                CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
                    BasicTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            authViewModel.resetState()
                            showEmailNotFound = false
                        },
                        singleLine = true,
                        textStyle = TextStyle(color = Color.Black, fontSize = 13.sp),
                        interactionSource = interactionSource,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .semantics { contentType = ContentType.EmailAddress },
                        decorationBox = { innerTextField ->
                            OutlinedTextFieldDefaults.DecorationBox(
                                value = email,
                                innerTextField = innerTextField,
                                enabled = true,
                                singleLine = true,
                                visualTransformation = VisualTransformation.None,
                                interactionSource = interactionSource,
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
                                        enabled = true,
                                        isError = isError,
                                        interactionSource = interactionSource,
                                        colors = customFieldOutlinedColors(),
                                        shape = OutlinedTextFieldDefaults.shape
                                    )
                                }
                            )
                        }
                    )
                }

                if (accountState is UiState.Error) {
                    Text(
                        text = (accountState as UiState.Error).message,
                        color = Color.Red,
                        fontSize = 14.sp,
                        modifier = Modifier
                    )
                }
                Spacer(Modifier.padding(top=20.dp))
//                if (showEmailNotFound) {
//                    Box(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .clip(RoundedCornerShape(12.dp))
//                            .background(Color(0xFFFFF5F5))
//                            .border(1.dp, Color(0xFFFFF5F5), RoundedCornerShape(8.dp))
//                            .padding(12.dp)
//
//                    ) {
//                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
//                            Text(
//                                text = "We couldn't find an account with that email.",
//                                color = Color(0xFFCC0000),
//                                fontSize = 16.sp,
//                                fontWeight = FontWeight.Medium
//                            )
//                            Row(verticalAlignment = Alignment.CenterVertically) {
//
//                                Text(
//                                    text = "Sign up here ",
//                                    modifier = Modifier.clickable { navController.navigate("signup") },
//                                    color = Color(0xFF0047CC),
//                                    fontSize = 16.sp,
//                                    fontWeight = FontWeight.Bold,
//                                    textDecoration = TextDecoration.Underline
//                                )
//                                Text(
//                                    text = "if you are new ",
//                                    color = Color(0xFFCC0000),
//                                    fontSize = 16.sp
//                                )
//                            }
//                        }
//                    }
//                }
                LaunchedEffect(showEmailNotFound) {
                    if (showEmailNotFound) {
                        navController.navigate("org-not-found")
                        authViewModel.resetState()
                    }
                }

            }

            if (isSubmitted) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .background(Color.White)
                        .border(2.dp, PrimaryBorder, shape = RoundedCornerShape(8.dp))
                        .padding(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Circular icon
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(color = Color(0xFFF5F5F5)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PersonOutline,
                                contentDescription = null,
                                tint = PrimaryTextColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Text block takes remaining space
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Sign in as",
                                fontSize = 11.sp,
                                color = PrimaryTextColor
                            )
                            Text(
                                text = submittedEmail,
                                fontSize = 13.sp,
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
                            color = Primary,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(Modifier.padding(top=20.dp))
            if (isSubmitted){
                Row {
                    Column {
                        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
                            BasicTextField(
                                value = password,
                                onValueChange = {
                                    password = it
                                    authViewModel.resetState()
                                },
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
                                        isError = isPasswordError,
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
                                                isError = isPasswordError,
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
                            Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (accountState is UiState.Error) {
                                Text(
                                    text = (accountState as UiState.Error).message,
                                    color = Color.Red,
                                    fontSize = 8.sp,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Text("Forgot Password?", Modifier
                                .clickable{
                                    navController.navigate("new-pass")
                                }
                                .padding(bottom = 20.dp),
                                color = Color(0xFF0A42BE), fontSize = 11.sp
                            )
                        }

                    }

                }

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

            CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
                Button(
                    onClick = {
                        if (!isSubmitted) {
                            authViewModel.verifyEmail(email)
                        } else {
                            authViewModel.login(
                                email = email,
                                password = password
                            )
                        }
                    },
                    modifier = Modifier
                        .height(40.dp)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 0.dp, horizontal = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2563eb),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(5.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (accountState is UiState.Loading) {
                            CirculerProgressIndicatorReuse()
                        } else {
                            Text(
                                text = if (isSubmitted) "Continue" else "Verify Mail",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(20.dp))

            OrText()
            Spacer(Modifier.height(20.dp))

            Row {
                ContinueWithGoogle(activity,navController)
            }
            Spacer(Modifier.padding(top=20.dp))
            Row {
                ContinueWithApple(activity,navController)
            }
        }
    }
