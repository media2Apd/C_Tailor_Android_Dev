package com.cuso.mobile

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cuso.mobile.ui.theme.CusoTailorTheme
import com.cuso.mobile.view.forgot_password.ForgotPassword
import com.cuso.mobile.view.forgot_password.ResetPassword
import com.cuso.mobile.view.forgot_password.VerifyForgotPassword
import com.cuso.mobile.view.login.LoginOtpScreen
import com.cuso.mobile.view.login.LoginScreen
import com.cuso.mobile.view.organization.OrganizationProfile
import com.cuso.mobile.view.others.privacyPolicy
import com.cuso.mobile.view.others.termsConditions
import com.cuso.mobile.view.home.HomeScreen
import com.cuso.mobile.view.signup_screen.SignUpOtpScreen
import com.cuso.mobile.view.signup_screen.SignUpScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Suppress("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            CusoTailorTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { _ ->
                    AppNav(activity = this)
                    val navController = rememberNavController()

//                    HomeScreen(navController)

                }
            }
        }
    }
}

@Composable
fun AppNav(
    activity: Activity
) {
    val navController = rememberNavController()
    // ✅ Use the activity parameter directly - no LocalContext needed
    val localActivity = activity

    NavHost(
        navController = navController,
        startDestination = "login",
        modifier = Modifier.fillMaxSize()
    ) {

        composable(
            route = "signup_otp/{email}"
        ) { backStackEntry ->

            val email = backStackEntry.arguments?.getString("email") ?: ""

            SignUpOtpScreen(
                navController = navController,
                activity = activity,
                submittedEmail = email
            )
        }

        composable("signup") {
            SignUpScreen(
                navController = navController,
                onSignUpSuccess = {
                    navController.navigate("login")
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                activity = localActivity
            )
        }

        composable("home") {
            HomeScreen(navController)
        }

        composable(
            route = "login?message={message}",
            arguments = listOf(
                navArgument("message") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->

            val message =
                backStackEntry.arguments?.getString("message") ?: ""

            LoginScreen(
                activity = localActivity,
                navController = navController,
                onloginSuccess = {
                    navController.navigate("home")
                },
                resetSuccessMessage = message
            )
        }

        composable(
            route = "login-with-email/{email}",
            arguments = listOf(
                navArgument("email") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->

            val email =
                backStackEntry.arguments?.getString("email") ?: ""

            LoginScreen(
                activity = localActivity,
                navController = navController,
                onloginSuccess = {
                    navController.navigate("home")
                },
                prefilledEmail = email
            )
        }

        composable(
            route = "login-otp/{email}",
            arguments = listOf(
                navArgument("email") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->

            val email =
                backStackEntry.arguments?.getString("email") ?: ""

            LoginOtpScreen(
                navController = navController,
                activity = localActivity,
                submittedEmail = email
            )
        }

        composable("terms") {
            termsConditions(navController)
        }

        composable(
            route = "reset-pass/{resetToken}",
            arguments = listOf(
                navArgument("resetToken") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->

            val resetToken =
                backStackEntry.arguments?.getString("resetToken") ?: ""

            ResetPassword(
                resetToken = resetToken,
                navController = navController
            )
        }

        composable(
            route = "verify-forgot-pass/{email}",
            arguments = listOf(
                navArgument("email") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->

            val email =
                backStackEntry.arguments?.getString("email") ?: ""

            VerifyForgotPassword(
                navController = navController,
                activity = activity,
                submittedEmail = email
            )
        }

        composable("new-pass") {
            ForgotPassword(
                activity = localActivity,
                navController = navController
            )
        }

        composable("privacy") {
            privacyPolicy(navController)
        }

        composable("org") {
            OrganizationProfile(
                onSetupComplete = {
                    navController.navigate("home") {
                        popUpTo("org") { inclusive = true }
                    }
                }
            )
        }
    }
}