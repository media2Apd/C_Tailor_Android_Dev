package com.cuso.mobile.view.composable

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.*
import com.cuso.mobile.viewmodel.Authenticate
import com.cuso.mobile.viewmodel.UiState
import com.cuso.mobile.R

@Composable
fun CardContentsLoginScreen(
    navController: NavController,
    activity: Activity,
    authViewModel: Authenticate,
    prefilledEmail: String = ""
) {
    val tokens = LocalAppTokens.current
    val focusManager = LocalFocusManager.current

    val accountState by authViewModel.accountState.collectAsState()
    var email by rememberSaveable { mutableStateOf(prefilledEmail) }
    var password by remember { mutableStateOf("") }
    var submittedEmail by rememberSaveable { mutableStateOf(prefilledEmail) }
    var isSubmitted by remember { mutableStateOf(prefilledEmail.isNotBlank()) }
    var showEmailNotFound by remember { mutableStateOf(false) }

    val isError = accountState is UiState.Error || accountState is UiState.EmailNotFound
    val errorMsg = (accountState as? UiState.Error)?.message

    Column(
        modifier = Modifier
            .fillMaxWidth()
            // Clear focus and hide keyboard when clicking background
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                focusManager.clearFocus()
            }
            .padding(tokens.screenPadding)
    ) {

        if (!isSubmitted) {
            CusoTextField(
                value = email,
                onValueChange = {
                    email = it
                    authViewModel.resetState()
                    showEmailNotFound = false
                },
                leadingIconPainter = painterResource(R.drawable.ic_mail),
                label = "Email",
                placeholder = "your@email.com",
                isError = isError,
                errorText = errorMsg,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            LaunchedEffect(showEmailNotFound) {
                if (showEmailNotFound) {
                    navController.navigate("org-not-found")
                    authViewModel.resetState()
                }
            }
        }

        if (isSubmitted) {
            // User identity box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(whiteBg)
                    .border(1.dp, PrimaryBorder, shape = RoundedCornerShape(5.dp))
                    .padding(horizontal = 12.dp)
            ) {
                Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(0xFFF5F5F5)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.PersonOutline, null, tint = PrimaryTextColor, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Sign in as", fontSize = tokens.caption, color = PrimaryTextColor)
                        Text(submittedEmail, fontSize = tokens.bodyMedium, fontWeight = FontWeight.Bold, color = blackTitle, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Text(
                        "Change",
                        modifier = Modifier.clickable { navController.navigate("login") }.padding(8.dp),
                        color = Primary, fontSize = tokens.caption, fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(tokens.screenPadding))

            CusoTextField(
                value = password,
                onValueChange = { password = it; authViewModel.resetState() },
                label = "Password",
                placeholder = "Enter password",
                leadingIconPainter = painterResource(R.drawable.ic_lock),
                isPassword = true,
                isError = isError,
                errorText = errorMsg,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Text(
                    "Forgot Password?",
                    modifier = Modifier.clickable { navController.navigate("new-pass/${email}") }.padding(vertical = 8.dp),
                    color = Color(0xFF0A42BE), fontSize = tokens.caption, fontWeight = FontWeight.Medium
                )
            }
        }

        LaunchedEffect(accountState) {
            if (accountState is UiState.EmailVerified) {
                submittedEmail = email
                isSubmitted = true
            } else if (accountState is UiState.EmailNotFound) {
                showEmailNotFound = true
            }
        }

        Spacer(Modifier.height(tokens.screenPadding))

        // Main Login/Verify Button - height 40dp, radius 5dp
        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
            Button(
                onClick = {
                    focusManager.clearFocus()
                    if (!isSubmitted) authViewModel.verifyEmail(email)
                    else authViewModel.login(email, password)
                },
                modifier = Modifier.height(40.dp).fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = whiteBg),
                shape = RoundedCornerShape(5.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                if (accountState is UiState.Loading) {
                    CirculerProgressIndicatorSmall()
                } else {
                    Text(
                        text = if (isSubmitted) "Continue" else "Verify Email",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(Modifier.height(tokens.screenPadding))
        OrText()
        Spacer(Modifier.height(tokens.screenPadding))

        ContinueWithGoogle(activity, navController)
        Spacer(Modifier.height(tokens.screenPadding / 2))
        ContinueWithApple(activity, navController)

        Spacer(Modifier.height(tokens.screenPadding))
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { SignUpText() }
    }
}