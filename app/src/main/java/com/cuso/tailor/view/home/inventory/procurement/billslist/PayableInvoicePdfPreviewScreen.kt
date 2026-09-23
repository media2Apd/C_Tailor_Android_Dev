package com.cuso.tailor.view.home.inventory.procurement.billslist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.AppErrorState
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.viewmodel.InventoryViewModel

@Composable
fun PayableInvoicePdfPreviewScreen(
    onClose: () -> Unit,
    onDownloadPdf: () -> Unit = {},
    viewModel: InventoryViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val billDetail by viewModel.selectedBillDetail.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingBillDetail.collectAsStateWithLifecycle()
    val error by viewModel.billDetailError.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = Primary_background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Column(modifier = Modifier.fillMaxWidth().background(whiteBg)) {
                TitleBar("Preview PDF", onClose)
                HorizontalDivider(color = title_border)
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Primary)
                    }
                }

                error != null -> {
                    AppErrorState(
                        title = "Failed to load preview",
                        message = error ?: "Unknown error occurred",
                        onRetry = { }
                    )
                }

                billDetail != null -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(tokens.screenPadding)
                    ) {
                        item {
                            BillPreviewReceiptCard(detail = billDetail!!, tokens = tokens)
                        }
                    }
                }
            }
        }
    }
}