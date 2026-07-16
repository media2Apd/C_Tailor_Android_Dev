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
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cuso.mobile.repository.SessionManager
import com.cuso.mobile.view.home.finance.ChartOfAccountScreen
import com.cuso.mobile.ui.theme.CusoTailorTheme
import com.cuso.mobile.view.forgot_password.ForgotUserPassword
import com.cuso.mobile.view.forgot_password.ResetPassword
import com.cuso.mobile.view.forgot_password.VerifyForgotPassword
import com.cuso.mobile.view.login.LoginOtpScreen
import com.cuso.mobile.view.login.LoginScreen
import com.cuso.mobile.view.organization.OrganizationProfile
import com.cuso.mobile.view.home.HomeScreen
import com.cuso.mobile.view.home.sales.lead.LeadScreenContent
import com.cuso.mobile.view.home.OrderFlowNavigator
import com.cuso.mobile.view.home.SettingsScreen
import com.cuso.mobile.view.home.branch.BranchSettingsScreen
import com.cuso.mobile.view.home.sales.sales_order.SalesOrderScreen
import com.cuso.mobile.view.home.department.DepartmentSettingsScreen
import com.cuso.mobile.view.home.finance.CustomerDetailScreenStatic
import com.cuso.mobile.view.organization.OrganizationNotFoundScreen
import com.cuso.mobile.view.others.PrivacyPolicy
import com.cuso.mobile.view.others.TermsConditions
import com.cuso.mobile.view.signup_screen.SignUpOtpScreen
import com.cuso.mobile.view.signup_screen.SignUpScreen
import com.cuso.mobile.view.home.finance.FinanceCustomerScreen
import com.cuso.mobile.view.home.finance.FinanceInvoiceScreen
import com.cuso.mobile.view.home.finance.InvoiceDetailScreen
import com.cuso.mobile.view.home.finance.ManualJournalEntryScreen
import com.cuso.mobile.view.home.finance.TrialBalanceScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var sessionManager: SessionManager

    // 👇 null while checking. Splash stays on screen as long as this is null.
    private var isLoggedIn: Boolean? = null

    @Suppress("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        // 👇 MUST be the very first line, before super.onCreate()
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        // 👇 Tell the system splash: "don't dismiss until isLoggedIn is resolved"
        splashScreen.setKeepOnScreenCondition { isLoggedIn == null }

        // 👇 Fast Room query - resolves in a few ms, splash covers this entirely
        runBlocking {
            isLoggedIn = sessionManager.isLoggedIn()
        }

        enableEdgeToEdge()
        setContent {
            CusoTailorTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
                    val navController = rememberNavController()
                    var selectedInvoiceId by remember { mutableStateOf<String?>(null) }

//                    PricingQuotationScreen(onClose = {})
                    // 👇 isLoggedIn is guaranteed non-null here since splash already waited for it
                    AppNav(activity = this, startLoggedIn = isLoggedIn == true)
//                    FinanceCustomerScreen(
//                        onClose = { navController.popBackStack() },
//                        onCustomerClick = { customerId ->
//                            navController.navigate("customer_detail/$customerId")
//                        }
//
//                    )

                //                    OrderManagementScreen(navController = navController)
//
//                    if (selectedCustomerId == null) {
//                        FinanceCustomerScreen(
//                            onClose = { /* navigate back or whatever */ },
//                            onCustomerClick = { customerId ->
//                                selectedCustomerId = customerId   // 👈 real id vandhudum idhu
//                            }
//                        )
//                    } else {
//                        CustomerDetailScreenStatic(
//                            onClose = { selectedCustomerId = null },
//                            customerId = selectedCustomerId!!
//                        )
//                    }
//                    FinanceInvoiceScreen(
//                        onClose = { navController.popBackStack() },
//                        onInvoiceClick = { invoice ->
//                            navController.navigate("invoice_detail/${invoice.id}")
//                        }
//                    )

/*
                    if (selectedInvoiceId == null) {
                        FinanceInvoiceScreen(
                            onClose = { navController.popBackStack() },
                            onInvoiceClick = { invoice ->
                                selectedInvoiceId = invoice.id
                            }
                        )
                    } else {
                        InvoiceDetailScreen(
                            invoiceId = selectedInvoiceId!!,
                            onClose = {
                                selectedInvoiceId = null
                            },
                            onSharePdf = { *//* Handle share PDF *//* },
                            onDownloadInvoice = { *//* Handle download invoice *//* }
                        )
                    }*/
//                    ManualJournalEntryScreen()

                }
            }
        }
    }
}

@Composable
fun AppNav(activity: Activity, startLoggedIn: Boolean) {
    val navController = rememberNavController()

    // 👇 No more null-check, no more spinner box - resolved before first frame
    val startDestination = if (startLoggedIn) "home" else "login?message={message}"

    NavHost(
        navController = navController,
        startDestination = startDestination,
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
        composable("org-not-found"){
            OrganizationNotFoundScreen(navController)
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

        composable("signup") {
            SignUpScreen(
                navController = navController,
                onSignUpSuccess = { navController.navigate("login") },
                onNavigateToLogin = { navController.popBackStack() },
                activity = activity
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
            ForgotUserPassword(activity = activity, navController = navController)
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

        composable("terms")   { TermsConditions(navController) }
        composable("privacy") { PrivacyPolicy(navController) }

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
        composable("lead") {
            LeadScreenContent()
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