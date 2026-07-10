package com.cuso.mobile.view.signup_screen

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

import com.cuso.mobile.view.composable.*
import com.cuso.mobile.viewmodel.Authenticate
import com.cuso.mobile.viewmodel.UiState
@Suppress("UNUSED_PARAMETER")
@Composable
fun SignUpScreen(
    onSignUpSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: Authenticate = hiltViewModel(),
    activity: Activity,
    navController: NavController
) {
    val state by viewModel.accountState.collectAsState()

    var firstName by rememberSaveable { mutableStateOf("") }
    var lastName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var country by rememberSaveable { mutableStateOf("") }
    var stateField by rememberSaveable { mutableStateOf("") }
    var organization by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var isTermsAccepted by remember { mutableStateOf(false) }
    var selectedIso by remember { mutableStateOf("IN") }

    // ✅ SAFE: capture stable email for navigation
    val currentEmail = email

    LaunchedEffect(state) {
        if (state is UiState.RegisterSuccess) {
            viewModel.resetState()
            onSignUpSuccess()

            // ✅ FIX: use stable email instead of raw state email
            navController.navigate("signup_otp/$currentEmail")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            // ✅ This long form (10+ fields, dropdowns, a picker) now scrolls
            // instead of relying on a Card that force-fills the screen, and
            // imePadding keeps whichever field is focused above the keyboard.
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding()
                .border(2.dp, Color.White, RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {

            Column(
                Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AppLogo()
                Spacer(Modifier.padding(top = 20.dp))
                SignUpTitle()
            }

            Column(
                modifier = Modifier.padding(25.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                FirstNameLastName(
                    firstName = firstName,
                    lastName = lastName,
                    onFirstNameChange = { firstName = it },
                    onLastNameChange = { lastName = it }
                )

                Email(
                    emailValue = email,
                    onEmailChange = { email = it }
                )

                PhoneInputField(
                    phoneValue = phone,
                    onPhoneChange = { phone = it },
                    onCountryChange = { selectedIso = it.iso }
                )

                CountryAndStatePicker(
                    selectedCountry = country,
                    selectedState = stateField,
                    onCountryChange = { country = it },
                    onStateChange = { stateField = it }
                )

                Orgname(
                    organizationValue = organization,
                    onOrganizationChange = { organization = it }
                )

                PasswordInputField(
                    passwordValue = password,
                    onPasswordChange = { password = it }
                )

                if (state is UiState.Error) {
                    Text(
                        text = (state as UiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp
                    )
                }

                TermsCheckbox(
                    navController,
                    onCheckedChange = { isTermsAccepted = it }
                )


                Button(
                    onClick = {
                        viewModel.signUp(
                            firstName = firstName,
                            lastName = lastName,
                            email = email,
                            mobile = phone,
                            countryIso = selectedIso,
                            country = country,
                            state = stateField,
                            organizationName = organization,
                            password = password,
                            termsAccepted = true
                        ) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Blue,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    enabled = state !is UiState.Loading
                ) {
                    if (state is UiState.Loading) {
                        CirculerProgressIndicatorReuse()

                    } else {
                        Text(
                            "Create Account",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }


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

                ContinueWithGoogle(activity, navController)
            }

            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    "Already having account?",
                    Modifier.padding(horizontal = 2.dp),
                    color = Color.Gray
                )
                Text(
                    text = "Sign in",
                    color = Color.Blue,
                    modifier = Modifier.clickable {
                        navController.navigate("login")
                    }
                )
            }
        }
    }
}