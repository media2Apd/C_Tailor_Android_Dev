@file:Suppress(
    "UNUSED_VALUE",
    "SpellCheckingInspection",
    "GrazieInspection",
    "AssignedValueIsNeverRead",
    "unused_variable",
    "unused_parameter",
    "UnusedMaterial3ScaffoldPaddingParameter"
)

package com.cuso.mobile

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.adaptive_screen.getAdaptiveTokens
import com.cuso.mobile.repository.SessionManager
import com.cuso.mobile.ui.theme.CusoTailorTheme
import com.cuso.mobile.ui.theme.NoRippleProvider
import com.cuso.mobile.utils.AppLoadingManager
import com.cuso.mobile.utils.LocalIsAppBusy
import com.cuso.mobile.view.forgot_password.ForgotUserPassword
import com.cuso.mobile.view.forgot_password.ResetPassword
import com.cuso.mobile.view.forgot_password.VerifyForgotPassword
import com.cuso.mobile.view.login.LoginOtpScreen
import com.cuso.mobile.view.login.LoginScreen
import com.cuso.mobile.view.organization.OrganizationProfile
import com.cuso.mobile.view.home.HomeScreen
import com.cuso.mobile.view.home.sales.lead.LeadScreenContent
import com.cuso.mobile.view.home.OrderFlowNavigator
import com.cuso.mobile.view.home.profile_settings.SettingsScreen
import com.cuso.mobile.view.home.branch.BranchSettingsScreen
import com.cuso.mobile.view.home.sales.sales_order.SalesOrderScreen
import com.cuso.mobile.view.home.department.DepartmentSettingsScreen
import com.cuso.mobile.view.organization.OrganizationNotFoundScreen
import com.cuso.mobile.view.others.PrivacyPolicy
import com.cuso.mobile.view.others.TermsConditions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var sessionManager: SessionManager

    // Holds login state. Splash screen stays until this is not null.
    private var isLoggedIn: Boolean? = null

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition { isLoggedIn == null }

        lifecycleScope.launch {
            isLoggedIn = sessionManager.isLoggedIn()
            enableEdgeToEdge()

            setContent {
                // 1. Calculate the current WindowSizeClass (Compact, Medium, or Expanded)
                val windowSizeClass = calculateWindowSizeClass(this@MainActivity)

                // 2. Generate the adaptive design tokens based on screen width
                val tokens = getAdaptiveTokens(windowSizeClass.widthSizeClass)

                // Global "is any API call in flight" flag. Read by every
                // reusable input (AppTextField, AppDropdown, AppButton, etc.)
                // via LocalIsAppBusy so fields auto-disable app-wide without
                // each screen having to wire up its own loading state.
                val isAppBusy by AppLoadingManager.busyState.collectAsState()

                // 3. Provide the tokens to the entire UI tree using CompositionLocalProvider
                // This prevents the IllegalStateException in child components like DynamicIsland
                CompositionLocalProvider(
                    LocalAppTokens provides tokens,
                    LocalIsAppBusy provides isAppBusy
                ) {
                    CusoTailorTheme {
                        NoRippleProvider {
                            val focusManager = LocalFocusManager.current
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .pointerInput(Unit) {
                                        detectTapGestures(onTap = {
                                            focusManager.clearFocus()
                                        })
                                    }
                            ) {

                                Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
                                    AppNav(
                                        activity = this@MainActivity,
                                        startLoggedIn = isLoggedIn == true,
                                        widthSizeClass = windowSizeClass.widthSizeClass
                                    )

                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppNav(
    activity: Activity,
    startLoggedIn: Boolean,
    widthSizeClass: WindowWidthSizeClass
) {
    val navController = rememberNavController()

    // Determine entry point based on login session
    val startDestination = if (startLoggedIn) "home" else "login?message={message}"

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.fillMaxSize()
    ) {

        // ── Authentication Flow ──────────────────────────────────

        composable("login?message={message}",
            arguments = listOf(navArgument("message") {
                type = NavType.StringType; defaultValue = ""
            })
        ) { backStackEntry ->
            val message = backStackEntry.arguments?.getString("message") ?: ""
            LoginScreen(
                activity = activity,
                navController = navController,
                onloginSuccess = {
                    navController.navigate("home") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                resetSuccessMessage = message
            )
        }

        composable("login-with-email/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            LoginScreen(
                activity = activity,
                navController = navController,
                onloginSuccess = {
                    navController.navigate("home") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                prefilledEmail = email
            )
        }

        composable("login-otp/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            LoginOtpScreen(
                navController = navController,
                activity = activity,
                submittedEmail = email
            )
        }

        composable(
            "new-pass/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            ForgotUserPassword(
                activity = activity,
                navController = navController,
                prefilledEmail = email
            )
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

        // ── Core Application Screens ───────────────────────────

        composable("home") {
            HomeScreen(navController, widthSizeClass)
        }

        composable("create-order") {
            OrderFlowNavigator(
                onFinish = { savedOrderId ->
                    if (savedOrderId != null) {
                        // Pass the saved order id to the "home" screen's back stack entry
                        // so HomeScreen can pick it up and navigate to Order Overview.
                        navController.getBackStackEntry("home")
                            .savedStateHandle["pendingOrderId"] = savedOrderId
                    }
                    navController.popBackStack()
                }
            )
        }

        composable("sales_sales_orders") {
            SalesOrderScreen(
                navController = navController,
                onMenuClick = { navController.navigate("home") },
                onBack = { navController.popBackStack() }
            )
        }

        composable("sales_lead") {
            // Updated to ensure LeadScreen is called with width class for adaptiveness
            LeadScreenContent()
        }

        // ── Settings & Profile ────────────────────────────────

        composable("home_organization_profile") {
            OrganizationProfile(
                onSetupComplete = { navController.popBackStack() },
            )
        }

        composable("home_branch_management") {
            BranchSettingsScreen(
                navController = navController,
                onBack = {
                    navController.navigate("profile-settings") {
                        popUpTo("profile-settings") { inclusive = true }
                    }
                }
            )
        }

        composable("home_department_teams") {
            DepartmentSettingsScreen(
                navController = navController,
                onBack = {
                    navController.navigate("profile-settings") {
                        popUpTo("profile-settings") { inclusive = true }
                    }
                }
            )
        }

        composable("home_designation") {
            SettingsScreen(navController = navController)
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

        composable("org-not-found") {
            OrganizationNotFoundScreen(navController)
        }

        // ── Legal & Static ───────────────────────────────────

        composable("terms")   { TermsConditions(navController) }
        composable("privacy") { PrivacyPolicy(navController) }
    }
}