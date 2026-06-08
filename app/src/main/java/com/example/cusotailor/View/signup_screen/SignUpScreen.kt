package com.example.cusotailor.View.signup_screen

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.navigation.NavController
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.cusotailor.View.composable.AppLogo
import com.example.cusotailor.View.composable.ContinueGoogle
import com.example.cusotailor.View.composable.CountryAndStatePicker
import com.example.cusotailor.View.composable.Email
import com.example.cusotailor.View.composable.Orgname
import com.example.cusotailor.View.composable.PasswordInputField
import com.example.cusotailor.View.composable.PhoneInputField
import com.example.cusotailor.View.composable.SignUpTitle
import com.example.cusotailor.View.composable.TermsCheckbox
import com.example.cusotailor.View.composable.firstNameLastName
import com.example.cusotailor.viewmodel.Authenticate
import com.example.cusotailor.viewmodel.UiState



@Composable
fun SignUpScreen(
    onSignUpSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: Authenticate = viewModel(),
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
                AppLogo()
                SignUpTitle()
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
                    Email(
                        emailValue = email,
                        onEmailChange = { email = it }
                    )

                }
                Row() {

                    PhoneInputField(
                        phoneValue = phone,
                        onPhoneChange = { phone = it },
                        onCountryChange = { selectedIso=it.iso }
                    )
                }
                Row() {
                    CountryAndStatePicker(
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
                    PasswordInputField(
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
                    TermsCheckbox(navController,
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
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
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
                    ContinueGoogle(activity)

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