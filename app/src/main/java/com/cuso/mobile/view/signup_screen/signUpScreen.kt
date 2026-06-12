package com.cuso.mobile.view.signup_screen

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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

import com.cuso.mobile.view.composable.Orgname

import com.cuso.mobile.view.composable.appLogo
import com.cuso.mobile.view.composable.continueGoogle
import com.cuso.mobile.view.composable.countryAndStatePicker
import com.cuso.mobile.view.composable.email
import com.cuso.mobile.view.composable.firstNameLastName
import com.cuso.mobile.view.composable.passwordInputField
import com.cuso.mobile.view.composable.phoneInputField
import com.cuso.mobile.view.composable.signUpTitle
import com.cuso.mobile.view.composable.termsCheckbox
import com.cuso.mobile.viewmodel.Authenticate
import com.cuso.mobile.viewmodel.UiState


@Composable
fun signUpScreen(
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


    LaunchedEffect(state) {
        if (state is UiState.RegisterSuccess) {
            viewModel.resetState()
            onSignUpSuccess()
            navController.navigate("org")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {



        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .border(2.dp, Color.White, RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
            ) {
                appLogo()
                Spacer(Modifier.padding(top = 20.dp))
                signUpTitle()
            }

            Column(
                modifier = Modifier.padding(25.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row() {
                    firstNameLastName(
                        firstName = firstName,
                        lastName = lastName,
                        onFirstNameChange = { firstName = it },
                        onLastNameChange = { lastName = it }
                    )
                }
                Row() {
                    email(
                        emailValue = email,
                        onEmailChange = { email = it }
                    )

                }
                Row() {

                    phoneInputField(
                        phoneValue = phone,
                        onPhoneChange = { phone = it },
                        onCountryChange = { selectedIso=it.iso }
                    )
                }
                Row() {
                    countryAndStatePicker(
                        selectedCountry = country,
                        selectedState = stateField,
                        onCountryChange = { country = it },
                        onStateChange = { stateField = it }
                    )
                }
                Row() {
                    Orgname(
                        organizationValue = organization,
                        onOrganizationChange = { organization = it }
                    )


                }
                Row() {
                    passwordInputField(
                        passwordValue = password,
                        onPasswordChange = { password = it }
                    )
                }

                    if (state is UiState.Error) {
                        Text(
                            text = (state as UiState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp
                        )
                    }
                    termsCheckbox(navController,
                        onCheckedChange = {isTermsAccepted=it})
                    if(isTermsAccepted)
                    {
                        Button(
                            onClick = {
                                viewModel.signUp(
                                    firstName = firstName,
                                    lastName = lastName,
                                    email = email,
                                    phone = phone,
                                    countryIso = selectedIso,
                                    country = country,
                                    state = stateField,
                                    organization = organization,
                                    password = password
                                )
                            },
                            modifier = Modifier

                                .fillMaxWidth(),

                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Blue,
                                contentColor = Color.White,


                                ), shape = RoundedCornerShape(10.dp),
                            enabled = state !is UiState.Loading
                        ) {
                            if (state is UiState.Loading) {
                                Row(
                                    Modifier
                                        .background(Color.Blue)
                                        .border(1.dp,Color.Blue,RoundedCornerShape(8)),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(22.dp),
                                        strokeWidth = 2.dp,
                                        color = Color.White
                                    )
                                }
                            } else {
                                Text(
                                    "Create Account",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }


                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    HorizontalDivider(
                        Modifier.width(190.dp),
                        DividerDefaults.Thickness,
                        color = Color.Gray
                    )

                    Text("Or", Modifier, color = Color.Gray)
                    HorizontalDivider(
                        Modifier.width(190.dp),
                        DividerDefaults.Thickness,
                        color = Color.Gray
                    )
                }
                Row() {
                    continueGoogle(activity,navController)

                }

            }
            Row(
                Modifier
                    .fillMaxWidth(),
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