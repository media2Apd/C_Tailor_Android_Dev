@file:Suppress("UNUSED_VALUE", "ASSIGNED_VALUE_IS_NEVER_READ")

package com.cuso.mobile.view.home

import androidx.compose.runtime.*
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.cuso.mobile.view.home.sales.sales_order.CreateOrderScreen
import com.cuso.mobile.viewmodel.BranchViewModel
import com.cuso.mobile.viewmodel.SalesViewModel
import com.cuso.mobile.view.home.sales.sales_order.CreateOrderNextStep
import com.cuso.mobile.view.home.sales.sales_order.OrderReviewData

/**
 * This wraps BOTH screens and holds the data that needs to flow
 * from CreateOrderScreen -> CreateOrderNextStep.
 *
 * Call this ONE composable from wherever you currently call CreateOrderScreen
 * (e.g. your NavHost composable("create_order") { ... } block).
 */
@Composable
fun OrderFlowNavigator(
    onFinish: () -> Unit = {},   // called when whole flow is done (e.g. pop back stack)
    salesViewModel: SalesViewModel = hiltViewModel(),
    branchViewModel: BranchViewModel = hiltViewModel()
) {
    // step = 0 -> show CreateOrderScreen, step = 1 -> show CreateOrderNextStep
    var step by remember { mutableIntStateOf(0) }

    // Holds whatever the user filled in step 1, so step 2 can read it.
    var reviewData by remember { mutableStateOf<OrderReviewData?>(null) }

    when (step) {
        0 -> {
            CreateOrderScreen(
                onBack = { onFinish() },
                onCancel = { onFinish() },
                onNextStep = { data ->
                    // 'data' comes from CreateOrderScreen — see the small change
                    // needed in CreateOrderScreen below.
                    reviewData = data
                    step = 1
                },
                salesViewModel = salesViewModel,
                branchViewModel = branchViewModel
            )
        }

        1 -> {
            // reviewData CANNOT be null here because we only set step=1
            // right after setting it. The '!!' is safe in this flow.
            CreateOrderNextStep(
                orderData = reviewData!!,
                onBack = { step = 0 },              // go back to edit
                onSaveOrder = {
                    onFinish()
                }
            )
        }
    }
}