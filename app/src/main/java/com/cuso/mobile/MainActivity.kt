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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cuso.mobile.ui.theme.CusoTailorTheme
import com.cuso.mobile.view.forgot_password.ForgotUserPassword
import com.cuso.mobile.view.forgot_password.ResetPassword
import com.cuso.mobile.view.forgot_password.VerifyForgotPassword
import com.cuso.mobile.view.home.BranchSettingsScreen
import com.cuso.mobile.view.home.DepartmentSettingsScreen
import com.cuso.mobile.view.login.LoginOtpScreen
import com.cuso.mobile.view.login.LoginScreen
import com.cuso.mobile.view.organization.OrganizationProfile
import com.cuso.mobile.view.others.privacyPolicy
import com.cuso.mobile.view.others.termsConditions
import com.cuso.mobile.view.home.HomeScreen
import com.cuso.mobile.view.home.OrderFlowNavigator
import com.cuso.mobile.view.home.SalesSettingsScreen
import com.cuso.mobile.view.home.SettingsScreen
import com.cuso.mobile.view.sales.CreateOrderScreen
import com.cuso.mobile.view.sales.SalesOrderScreen
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
                Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
                    val navController = rememberNavController()
                    AppNav(activity = this)

                    // OrderFlowNavigator internally handles BOTH screens:
                    // step 0 -> CreateOrderScreen (fill details)
                    // step 1 -> CreateOrderNextStep (review, using real filled data)
//                    OrderFlowNavigator(
//                        onFinish = { finish() }   // closes the activity when flow is done
//                    )
                }
            }
        }
    }
}

@Composable
fun AppNav(activity: Activity) {
    val navController = rememberNavController()
    val localActivity = activity

    NavHost(
        navController = navController,
        startDestination = "login",
        modifier = Modifier.fillMaxSize()
    ) {

        // ── Auth ──────────────────────────────────────────────

        composable("login?message={message}",
            arguments = listOf(navArgument("message") {
                type = NavType.StringType; defaultValue = ""
            })
        ) { backStackEntry ->
            val message = backStackEntry.arguments?.getString("message") ?: ""
            LoginScreen(
                activity = localActivity,
                navController = navController,
                onloginSuccess = { navController.navigate("home") },
                resetSuccessMessage = message
            )
        }

        // "create-order" route now drives the FULL flow (fill -> review),
        // not just the first screen.
        composable("create-order") {
            OrderFlowNavigator(
                onFinish = { navController.popBackStack() }
            )
        }

        composable("login-with-email/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            LoginScreen(
                activity = localActivity,
                navController = navController,
                onloginSuccess = { navController.navigate("home") },
                prefilledEmail = email
            )
        }

        composable("login-otp/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            LoginOtpScreen(
                navController = navController,
                activity = localActivity,
                submittedEmail = email
            )
        }

        composable("signup") {
            SignUpScreen(
                navController = navController,
                onSignUpSuccess = { navController.navigate("login") },
                onNavigateToLogin = { navController.popBackStack() },
                activity = localActivity
            )
        }

        composable("signup_otp/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            SignUpOtpScreen(
                navController = navController,
                activity = activity,
                submittedEmail = email
            )
        }

        composable("new-pass") {
            ForgotUserPassword(activity = localActivity, navController = navController)
        }

        composable("verify-forgot-pass/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            VerifyForgotPassword(
                navController = navController,
                activity = activity,
                submittedEmail = email
            )
        }

        composable("reset-pass/{resetToken}",
            arguments = listOf(navArgument("resetToken") { type = NavType.StringType })
        ) { backStackEntry ->
            val resetToken = backStackEntry.arguments?.getString("resetToken") ?: ""
            ResetPassword(resetToken = resetToken, navController = navController)
        }

        // ── Static pages ──────────────────────────────────────

        composable("terms")   { termsConditions(navController) }
        composable("privacy") { privacyPolicy(navController) }

        composable("org") {
            OrganizationProfile(
                onSetupComplete = {
                    navController.navigate("home") {
                        popUpTo("org") { inclusive = true }
                    }
                }
            )
        }

        // ── Home ──────────────────────────────────────────────

        composable("home") {
            HomeScreen(navController)
        }

        // ── Home sidebar routes ───────────────────────────────

        composable("home_organization_profile") {
            OrganizationProfile(
                onSetupComplete = { navController.popBackStack() }
            )
        }

        composable("home_branch_management") {
            BranchSettingsScreen(
                navController = navController,
                onBack = { navController.popBackStack() }
            )
        }

        composable("home_department_teams") {
            DepartmentSettingsScreen(
                navController = navController,
                onBack = { navController.popBackStack() }
            )
        }

        composable("home_designation") {
            SettingsScreen(
                navController = navController
            )
        }

        // ── Sales sidebar routes ──────────────────────────────

        composable("sales_lead") {
            HomeScreen(navController) // TODO: replace with LeadScreen
        }

        composable("sales_sales_orders") {
            SalesOrderScreen(
                navController = navController,
                onMenuClick = { navController.navigate("home") },
                onBack = { navController.popBackStack() }
            )
        }

        // TODO: add remaining sales routes as screens are built
        // composable("sales_customers")                  { CustomersScreen(...) }
        // composable("sales_measurements")               { MeasurementsScreen(...) }
        // composable("sales_orders")                     { OrdersScreen(...) }
        // composable("sales_overview")                   { OverviewScreen(...) }
        // composable("sales_pricing_and_quotations")     { PricingScreen(...) }
        // composable("sales_targets_vs_achievements")    { TargetsScreen(...) }
        // composable("sales_salesperson_analytics")      { AnalyticsScreen(...) }
    }
}