package com.cuso.mobile

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import com.cuso.mobile.ui.theme.CusoTailorTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cuso.mobile.model.Organization
import com.cuso.mobile.view.others.privacyPolicy
import com.cuso.mobile.view.others.termsConditions
import com.cuso.mobile.view.signup_screen.signUpScreen
import com.cuso.mobile.view.forgot_password.forgotPassword
import com.cuso.mobile.view.forgot_password.resetPassword
import com.cuso.mobile.view.login.loginOtpScreen
import com.cuso.mobile.view.login.loginScreen
import com.cuso.mobile.view.forgot_password.verifyForgotPassword
import com.cuso.mobile.view.organization.organizationProfile
import com.cuso.mobile.view.others.homeScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            CusoTailorTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNav(activity = this)
//                    OrganizationProfile()
//                    ResetPassword()
                }
            }
        }
    }
}
@Composable
fun AppNav(activity: Activity) {

    val navController = rememberNavController()
    val localActivity = LocalContext.current as Activity

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {

        composable("signup") {
            signUpScreen(
                navController = navController,
                onSignUpSuccess = { navController.navigate("login") },
                onNavigateToLogin = { navController.popBackStack() },
                activity = localActivity
            )
        }
        composable ("home"){
            homeScreen(navController)
        }
        composable(
            route = "login?message={message}",
            arguments = listOf(navArgument("message") {
                type = NavType.StringType
                defaultValue = ""
            })
        ) {backStackEntry->
            val message=backStackEntry.arguments?.getString("message")?:""
            loginScreen(
                activity = localActivity,
                navController = navController,
                onloginSuccess = { navController.navigate("home")},
                resetSuccessMessage = message
            )
        }
        composable(
            route = "login-with-email/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            loginScreen(
                activity = localActivity,
                navController = navController,
                onloginSuccess = { navController.navigate("home") },
                prefilledEmail = email
            )
        }

        composable(
            route = "login-otp/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            loginOtpScreen(
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
            arguments = listOf(navArgument("resetToken") { type = NavType.StringType })

        ) {backStackEntry->
            val resetToken = backStackEntry.arguments?.getString("resetToken") ?: ""
            resetPassword(resetToken=resetToken,navController)
        }
        composable(
            route = "verify-forgot-pass/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType })

        ) {
            backStackEntry->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            verifyForgotPassword(navController,activity, submittedEmail = email)
        }
        composable("new-pass") {
            forgotPassword(activity = localActivity,
                navController = navController)
        }

        composable("privacy") {
            privacyPolicy(navController)
        }

        composable("org"){
            organizationProfile()
        }
    }
}