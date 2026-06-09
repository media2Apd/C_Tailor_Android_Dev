package com.example.cusotailor

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
import com.example.cusotailor.ui.theme.CusoTailorTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.cusotailor.View.others.PrivacyPolicy
import com.example.cusotailor.View.others.TermsConditions
import com.example.cusotailor.View.signup_screen.SignUpScreen
import com.example.cusotailor.View.forgot_password.ForgotPassword
import com.example.cusotailor.View.login.LoginOtpScreen
import com.example.cusotailor.View.login.LoginScreen
import com.example.cusotailor.View.others.HomeScreen
import com.example.cusotailor.View.signup_screen.OrganizationProfile
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
            SignUpScreen(
                navController = navController,
                onSignUpSuccess = { navController.navigate("login") },
                onNavigateToLogin = { navController.popBackStack() },
                activity = localActivity
            )
        }
        composable ("home"){
            HomeScreen(navController)
        }

        composable("login") {
            LoginScreen(
                activity = localActivity,
                navController = navController,
                onloginSuccess = { navController.navigate("home") }
            )
        }

        composable(
            route = "login-otp/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            LoginOtpScreen(
                navController = navController,
                activity = localActivity,
                submittedEmail = email
            )
        }

        composable("terms") {
            TermsConditions(navController)
        }
        composable("forgot") {
            ForgotPassword(activity = localActivity,
                navController = navController)
        }

        composable("privacy") {
            PrivacyPolicy(navController)
        }
    }
}