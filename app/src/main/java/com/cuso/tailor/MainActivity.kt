@file:Suppress(
    "UNUSED_VALUE",
    "SpellCheckingInspection",
    "GrazieInspection",
    "AssignedValueIsNeverRead",
    "unused_variable",
    "unused_parameter",
    "UnusedMaterial3ScaffoldPaddingParameter"
)

package com.cuso.tailor

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.window.DialogProperties
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.adaptive_screen.getAdaptiveTokens
import com.cuso.tailor.repository.SessionManager
import com.cuso.tailor.ui.theme.CusoTailorTheme
import com.cuso.tailor.ui.theme.NoRippleProvider
import com.cuso.tailor.utils.AppLoadingManager
import com.cuso.tailor.utils.AuthEventManager
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.utils.LocalIsAppBusy
import com.cuso.tailor.view.composable.DynamicIslandError
import com.cuso.tailor.view.composable.DynamicIslandSuccess
import com.cuso.tailor.view.forgot_password.ForgotUserPassword
import com.cuso.tailor.view.forgot_password.ResetPassword
import com.cuso.tailor.view.forgot_password.VerifyForgotPassword
import com.cuso.tailor.view.home.HomeScreen
import com.cuso.tailor.view.home.OrderFlowNavigator
import com.cuso.tailor.view.home.branch.BranchSettingsScreen
import com.cuso.tailor.view.home.department.DepartmentSettingsScreen
import com.cuso.tailor.view.home.profile_settings.setup_pages.SettingsScreen
import com.cuso.tailor.view.home.sales.lead.LeadScreenContent
import com.cuso.tailor.view.home.sales.sales_order.SalesOrderScreen
import com.cuso.tailor.view.login.LoginOtpScreen
import com.cuso.tailor.view.login.LoginScreen
import com.cuso.tailor.view.organization.OrganizationNotFoundScreen
import com.cuso.tailor.view.organization.OrganizationProfile
import com.cuso.tailor.view.others.PrivacyPolicy
import com.cuso.tailor.view.others.TermsConditions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var sessionManager: SessionManager

    // Holds the authentication state
    private var isLoggedInState by mutableStateOf<Boolean?>(null)

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Splash screen waits until authentication status is resolved
        splashScreen.setKeepOnScreenCondition { isLoggedInState == null }

        // Immediately invoke setContent to prevent Window Timeout ANR
        setContent {
            // Check login state on background thread without blocking main thread
            LaunchedEffect(Unit) {
                val loggedIn = withContext(Dispatchers.IO) {
                    sessionManager.isLoggedIn()
                }
                isLoggedInState = loggedIn
            }

            // Render UI once authentication check is complete
            isLoggedInState?.let { isLoggedIn ->
                val windowSizeClass = calculateWindowSizeClass(this@MainActivity)
                val tokens = getAdaptiveTokens(windowSizeClass.widthSizeClass)
                val isAppBusy by AppLoadingManager.busyState.collectAsState()

                var globalSuccessMessage by remember { mutableStateOf<String?>(null) }
                var globalErrorMessage by remember { mutableStateOf<String?>(null) }

                LaunchedEffect(Unit) {
                    DynamicIslandManager.successMessage.collect { message ->
                        globalSuccessMessage = message
                    }
                }

                LaunchedEffect(Unit) {
                    DynamicIslandManager.errorMessage.collect { message ->
                        globalErrorMessage = message
                    }
                }

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
                                        startLoggedIn = isLoggedIn,
                                        widthSizeClass = windowSizeClass.widthSizeClass
                                    )
                                }

                                DynamicIslandSuccess(
                                    message = globalSuccessMessage,
                                    onDismiss = { globalSuccessMessage = null }
                                )

                                DynamicIslandError(
                                    message = globalErrorMessage,
                                    onDismiss = { globalErrorMessage = null }
                                )
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
    val startDestination = if (startLoggedIn) "home" else "login?message={message}"
    var sessionExpiredMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        AuthEventManager.sessionExpiredEvent.collect { message ->
            sessionExpiredMessage = message.ifBlank { "Session timeout. Please relog." }
        }
    }

    if (sessionExpiredMessage != null) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text(text = "Session Expired") },
            text = { Text(text = sessionExpiredMessage ?: "Session timeout. Please relog.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        sessionExpiredMessage = null
                        navController.navigate("login?message=") {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                ) {
                    Text("OK")
                }
            },
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        )
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.fillMaxSize()
    ) {
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

        composable("home") {
            HomeScreen(navController, widthSizeClass)
        }

        composable("create-order") {
            OrderFlowNavigator(
                onFinish = { savedOrderId ->
                    if (savedOrderId != null) {
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
            LeadScreenContent()
        }

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

        composable("terms") { TermsConditions(navController) }
        composable("privacy") { PrivacyPolicy(navController) }
    }
}