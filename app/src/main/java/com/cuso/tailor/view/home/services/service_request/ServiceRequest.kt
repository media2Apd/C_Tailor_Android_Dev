@file:Suppress("unused", "unusedVariable")

package com.cuso.tailor.view.home.services.service_request

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.service.ServiceRequestData
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.utils.currencySymbol
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.ServicesViewModel
import kotlinx.coroutines.delay

@Composable
fun ServiceRequestScreen(
    onClose: () -> Unit,
    onViewClick: (String) -> Unit,
    onCreateNewRequest: () -> Unit,
    viewModel: ServicesViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    var searchQuery by remember { mutableStateOf("") }
    var hasInitializedSearch by remember { mutableStateOf(false) }

    val pagingState by viewModel.serviceRequestsPagingState.collectAsStateWithLifecycle()
    val errorMessage by viewModel.serviceRequestsPagingError.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadServiceRequests()
    }

    LaunchedEffect(searchQuery) {
        if (hasInitializedSearch) {
            delay(400)
            viewModel.searchRequests(searchQuery)
        } else {
            hasInitializedSearch = true
        }
    }

    Scaffold(
        topBar = {
            Surface(modifier = Modifier.fillMaxWidth(), color = whiteBg) {
                TitleBar(title = "All Requests", onClose = onClose)
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = Primary_background
    ) { padding ->
        FabScaffold(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            fab = FabConfig(
                label = "Create Request",
                icon = Icons.Default.Add,
                onClick = onCreateNewRequest,
                alignment = Alignment.BottomEnd,
                endPadding = tokens.screenPadding,
                bottomPadding = tokens.buttonHeight * 1.5f
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Primary_background)
            ) {
                SearchFilterBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search Request Code, Customer...",
                    accentColor = Primary,
                    borderColor = BorderGray,
                    textSecondaryColor = TextSecondary,
                    onFilterClick = {}
                )

                when {
                    errorMessage != null && pagingState.items.isEmpty() && !pagingState.isInitialLoading -> {
                        AppErrorState(
                            title = "Failed to load requests",
                            message = errorMessage ?: "Failed to load requests",
                            onRetry = { viewModel.loadServiceRequests(search = searchQuery.ifBlank { null }) }
                        )
                    }

                    !pagingState.isInitialLoading && pagingState.items.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No Service Requests Found",
                                fontSize = tokens.bodyMedium,
                                color = mutedText
                            )
                        }
                    }

                    else -> {
                        PaginatedDataList(
                            state = pagingState,
                            onLoadMore = { viewModel.loadMoreServiceRequests() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) { item: ServiceRequestData ->
                            ServiceRequestCard(
                                item = item,
                                onViewClick = { onViewClick(item.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ServiceRequestCard(
    item: ServiceRequestData,
    onViewClick: () -> Unit
) {
    val (statusBadgeBg, statusBadgeTextColor) = when (item.status.lowercase()) {
        "completed" -> greenBg to greentext
        "in_progress", "received_in_workshop" -> primary_light to Primary
        "draft" -> yellowBg to yellowText
        else -> redBg to redText
    }

    val (priorityBg, priorityTextColor) = when (item.priority.lowercase()) {
        "high", "urgent" -> redBg to redText
        "medium" -> yellowBg to yellowText
        else -> greenBg to greentext
    }

    val firstItem = item.items.firstOrNull()

    val rawGarment = firstItem?.garmentDescription?.trim()?.takeIf { it.isNotBlank() } ?: "-"
    val truncatedGarment = if (rawGarment.length > 24) {
        rawGarment.take(21).trimEnd() + "..."
    } else {
        rawGarment
    }

    DataCard(
        item = item,
        eyebrowText = "Code: ${item.serviceRequestCode}",
        title = item.customerId?.fullName ?: "Customer",
        subtitle = "Category: ${item.primaryCategory} · ${item.createdAt?.take(10) ?: ""}",
        topBadgeText = item.status.replace("_", " "),
        topBadgeTextColor = statusBadgeTextColor,
        topBadgeBgColor = statusBadgeBg,
        topBadgeInline = true,
        footerAsRows = true,
        footerFields = listOf(
            DataCardField(
                label = "Garment",
                text = truncatedGarment
            ),
            DataCardField(
                label = "Amount",
                text = "${currencySymbol()}${item.grandTotal.toInt()}"
            ),
            DataCardField(
                label = "Priority",
                text = item.priority,
                valueBadge = true,
                valueBadgeBgColor = priorityBg,
                valueBadgeTextColor = priorityTextColor
            )
        ),
        actions = listOf(
            MenuAction(label = "View Details", onClick = onViewClick)
        ),
        content = null
    )
}