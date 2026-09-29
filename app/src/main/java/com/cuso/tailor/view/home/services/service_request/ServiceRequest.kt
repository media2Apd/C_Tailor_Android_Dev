@file:Suppress("unused", "unusedVariable")
package com.cuso.tailor.view.home.services.service_request

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.service.ServiceRequestData
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.ServiceRequestUiState
import com.cuso.tailor.viewmodel.ServicesViewModel
import androidx.compose.foundation.lazy.items

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
    val uiState by viewModel.listState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadServiceRequests()
    }

    LaunchedEffect(searchQuery) {
        delay(400)
        viewModel.searchRequests(searchQuery)
    }

    Scaffold(
        topBar = {
            Surface(modifier = Modifier.fillMaxWidth(), color = whiteBg) {
                TitleBar(title ="All Requests", onClose = onClose)
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = Color.Transparent
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
                    .background(Color.Transparent)
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

                when (val state = uiState) {
                    is ServiceRequestUiState.Loading -> {
                        ListSkeleton()
                    }
                    is ServiceRequestUiState.Error -> {
                        AppErrorState(
                            title = "Failed to load requests",
                            message = state.message,
                            onRetry = { viewModel.loadServiceRequests() }
                        )
                    }
                    is ServiceRequestUiState.Success -> {
                        if (state.items.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No Service Requests Found",
                                    fontSize = tokens.bodyMedium,
                                    color = mutedText
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = tokens.buttonHeight * 2)
                            ) {
                                items(
                                    items = state.items,
                                    key = { it.id }
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
    }
}

@Composable
fun ServiceRequestCard(
    item: ServiceRequestData,
    onViewClick: () -> Unit
) {
    val tokens = LocalAppTokens.current

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
                text = "₹${item.grandTotal.toInt()}"
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