package com.cuso.tailor.view.home.inventory.procurement.requisitions

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.AppDesignTokens
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.inventory.PurchaseRequisition
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.Primary_background
import com.cuso.tailor.ui.theme.TextPrimary
import com.cuso.tailor.ui.theme.background_light_purple
import com.cuso.tailor.ui.theme.complete_button_bg
import com.cuso.tailor.ui.theme.darkGreenBg
import com.cuso.tailor.ui.theme.greenBg
import com.cuso.tailor.ui.theme.grey_border
import com.cuso.tailor.ui.theme.mutedText
import com.cuso.tailor.ui.theme.redBg
import com.cuso.tailor.ui.theme.redText
import com.cuso.tailor.ui.theme.title_color
import com.cuso.tailor.ui.theme.whiteBg
import com.cuso.tailor.ui.theme.yellowBg
import com.cuso.tailor.ui.theme.yellowText
import com.cuso.tailor.view.composable.ActionDropdownMenu
import com.cuso.tailor.view.composable.AppCheckbox
import com.cuso.tailor.view.composable.AppErrorState
import com.cuso.tailor.view.composable.DeleteModel
import com.cuso.tailor.view.composable.DynamicIslandError
import com.cuso.tailor.view.composable.DynamicIslandSuccess
import com.cuso.tailor.view.composable.ErrorMapper
import com.cuso.tailor.view.composable.ListSkeleton
import com.cuso.tailor.view.composable.MenuAction
import com.cuso.tailor.view.composable.SearchFilterBar
import com.cuso.tailor.view.composable.ThreeDotLoading
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.viewmodel.InventoryViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun AllRequisitionsScreen(
    onClose: () -> Unit,
    onCreateRequisition: () -> Unit = {},
    onEditRequisition: (PurchaseRequisition) -> Unit = {},
    onRequisitionClick: (PurchaseRequisition) -> Unit = {},
    viewModel: InventoryViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current

    val requisitionsList by viewModel.requisitionsList.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingRequisitions.collectAsStateWithLifecycle()
    val isLoadingMore by viewModel.isLoadingMoreRequisitions.collectAsStateWithLifecycle()
    val canLoadMore by viewModel.canLoadMoreRequisitions.collectAsStateWithLifecycle()
    val successMessage by viewModel.requisitionSuccessMessage.collectAsStateWithLifecycle()
    val errorMessage by viewModel.requisitionErrorMessage.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedItemIds by remember { mutableStateOf(setOf<String>()) }
    var itemToDelete by remember { mutableStateOf<PurchaseRequisition?>(null) }
    val listState = rememberLazyListState()

    var isInitialized by remember { mutableStateOf(false) }
    LaunchedEffect(searchQuery) {
        if (!isInitialized) {
            isInitialized = true
            viewModel.fetchAllRequisitions()
        } else {
            delay(400)
            viewModel.fetchAllRequisitions(search = searchQuery.trim().ifBlank { null })
        }
    }

    LaunchedEffect(listState, canLoadMore, searchQuery) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = (layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) + 1
            totalItems > 0 && lastVisibleItemIndex >= (totalItems - 2)
        }
            .distinctUntilChanged()
            .collect { isNearBottom ->
                if (isNearBottom &&
                    canLoadMore &&
                    !viewModel.isLoadingMoreRequisitions.value &&
                    !viewModel.isLoadingRequisitions.value &&
                    searchQuery.isBlank()
                ) {
                    viewModel.loadMoreRequisitions()
                }
            }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary_background)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                TitleBar(
                    title = "All Requisitions",
                    onClose = onClose
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = onCreateRequisition,
                    containerColor = Primary,
                    contentColor = whiteBg,
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                    modifier = Modifier.height(tokens.buttonHeight)
                ) {
                    Text(
                        text = "Purchase Request",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = whiteBg
                    )
                    Spacer(Modifier.width(tokens.extraPadding * 0.4f))
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create",
                        tint = whiteBg,
                        modifier = Modifier.size(tokens.iconSize)
                    )
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                SearchFilterBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search Requisitions (e.g. PR-001)...",
                    showFilterIcon = true,
                    onFilterClick = { },
                    height = tokens.fieldHeight * 1.1f
                )

                HorizontalDivider(color = grey_border.copy(alpha = 0.5f), thickness = 2.dp)

                when {
                    isLoading && requisitionsList.isEmpty() -> {
                        ListSkeleton()
                    }

                    errorMessage != null && requisitionsList.isEmpty() -> {
                        AppErrorState(
                            title = "Failed to load requisitions",
                            message = errorMessage?.let { ErrorMapper.map(it) } ?: "Something went wrong. Please check your connection.",
                            onRetry = { viewModel.fetchAllRequisitions(search = searchQuery.trim().ifBlank { null }) }
                        )
                    }

                    requisitionsList.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(background_light_purple),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = null,
                                        tint = Primary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    text = "No Requisitions Found",
                                    fontSize = tokens.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "Create your first purchase requisition",
                                    fontSize = tokens.caption,
                                    color = mutedText
                                )
                            }
                        }
                    }

                    else -> {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                top = tokens.extraPadding * 0.6f,
                                bottom = tokens.buttonHeight * 2
                            ),
                            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f)
                        ) {
                            itemsIndexed(
                                items = requisitionsList,
                                key = { index, item -> item.id ?: item.prNumber ?: "pr_$index" }
                            ) { index, item ->
                                val itemId = item.id ?: item.prNumber ?: "pr_$index"
                                val isChecked = selectedItemIds.contains(itemId)

                                RequisitionCardItem(
                                    item = item,
                                    isChecked = isChecked,
                                    tokens = tokens,
                                    onCheckedChange = { checked ->
                                        selectedItemIds = if (checked) {
                                            selectedItemIds + itemId
                                        } else {
                                            selectedItemIds - itemId
                                        }
                                    },
                                    onClick = {
                                        Log.d("REQ_LIST", "Card clicked -> ID: ${item.id}, PR: ${item.prNumber}")
                                        onRequisitionClick(item)
                                    },
                                    onEdit = {
                                        onEditRequisition(item)
                                    },
                                    onApprove = {
                                        item.id?.let { id ->
                                            viewModel.actionRequisitionApproval(id, "Approved")
                                        }
                                    },
                                    onReject = {
                                        item.id?.let { id ->
                                            viewModel.actionRequisitionApproval(id, "Rejected")
                                        }
                                    },
                                    onDeleteClick = {
                                        itemToDelete = item
                                    }
                                )
                            }

                            if (isLoadingMore) {
                                item(key = "pagination_threedot_loader") {
                                    ThreeDotLoading(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        itemToDelete?.let { requisition ->
            DeleteModel(
                title = "Delete Requisition",
                message = "Are you sure you want to delete ${requisition.prNumber ?: "this requisition"}?\nIt can be restored within 7 days.",
                onDismiss = { itemToDelete = null },
                onDelete = {
                    val reqId = requisition.id
                    if (!reqId.isNullOrBlank()) {
                        viewModel.deleteRequisition(reqId)
                    }
                    itemToDelete = null
                }
            )
        }

        DynamicIslandSuccess(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = tokens.fieldHeight * 1.5f),
            message = successMessage,
            onDismiss = { viewModel.clearRequisitionAlerts() }
        )

        DynamicIslandError(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = tokens.fieldHeight * 1.5f),
            message = errorMessage?.takeIf { requisitionsList.isNotEmpty() }?.let { ErrorMapper.map(it) },
            onDismiss = { viewModel.clearRequisitionAlerts() }
        )
    }
}

@Composable
private fun RequisitionCardItem(
    item: PurchaseRequisition,
    isChecked: Boolean,
    tokens: AppDesignTokens,
    onCheckedChange: (Boolean) -> Unit,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val priority = item.priority ?: "Normal"
    val status = item.approvalStatus ?: "Draft"
    val isPending = status.equals("Pending", ignoreCase = true) ||
            status.equals("Submitted", ignoreCase = true) ||
            status.equals("Pending Approval", ignoreCase = true)
    val isApproved = status.equals("Approved", ignoreCase = true)

    val menuActions = remember(status) {
        listOf(
            MenuAction(
                label = "View Details",
                icon = Icons.Default.Visibility,
                tint = Primary,
                onClick = onClick
            ),
            MenuAction(
                label = "Edit",
                icon = Icons.Default.Edit,
                tint = Primary,
                onClick = onEdit
            ),
            MenuAction(
                label = "Approve",
                icon = Icons.Default.CheckCircle,
                tint = complete_button_bg,
                enabled = isPending,
                onClick = onApprove
            ),
            MenuAction(
                label = "Reject",
                icon = Icons.Default.Close,
                tint = redText,
                textColor = redText,
                enabled = isPending,
                onClick = onReject
            ),
            MenuAction(
                label = "Delete",
                icon = Icons.Default.Delete,
                tint = redText,
                textColor = redText,
                onClick = onDeleteClick
            )
        )
    }

    val prCode = item.prNumber?.ifBlank { "PR-XXXX" } ?: "PR-XXXX"

    val requestedBy = when (val req = item.requestedBy) {
        is Map<*, *> -> req["name"]?.toString() ?: req["firstName"]?.toString() ?: "Staff"
        is String -> req
        else -> "Staff"
    }

    val department = item.department ?: "Production"
    val itemsCountText = "${item.items.size} Items"
    val requestDate = item.createdAt?.take(10) ?: "—"
    val requiredBy = item.requiredByDate?.take(10) ?: "—"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding * 1.2f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppCheckbox(
                    checked = isChecked,
                    onCheckedChange = onCheckedChange
                )

                Spacer(Modifier.width(tokens.extraPadding))

                Text(
                    text = prCode,
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = title_color
                )

                Spacer(Modifier.width(tokens.extraPadding))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 2f))
                        .background(if (priority.equals("High", ignoreCase = true)) redBg else yellowBg)
                        .padding(horizontal = tokens.extraPadding * 0.8f)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(tokens.iconSize * 0.3f)
                                .clip(CircleShape)
                                .background(if (priority.equals("High", ignoreCase = true)) redText else yellowText)
                        )
                        Spacer(Modifier.width(tokens.extraPadding * 0.4f))
                        Text(
                            text = priority,
                            fontSize = tokens.label,
                            fontWeight = FontWeight.Medium,
                            color = if (priority.equals("High", ignoreCase = true)) redText else yellowText
                        )
                    }
                }

                Spacer(Modifier.weight(1f))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 2f))
                        .background(
                            when {
                                isApproved -> greenBg
                                status.equals("Rejected", ignoreCase = true) -> redBg
                                else -> yellowBg
                            }
                        )
                        .padding(horizontal = tokens.extraPadding * 0.8f)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(tokens.iconSize * 0.3f)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isApproved -> darkGreenBg
                                        status.equals("Rejected", ignoreCase = true) -> redText
                                        else -> yellowText
                                    }
                                )
                        )
                        Spacer(Modifier.width(tokens.extraPadding * 0.4f))
                        Text(
                            text = status,
                            fontSize = tokens.label,
                            fontWeight = FontWeight.Medium,
                            color = when {
                                isApproved -> darkGreenBg
                                status.equals("Rejected", ignoreCase = true) -> redText
                                else -> yellowText
                            }
                        )
                    }
                }

                Spacer(Modifier.width(tokens.extraPadding * 0.4f))

                ActionDropdownMenu(
                    actions = menuActions,
                    icon = Icons.Default.MoreVert
                )
            }

            Spacer(Modifier.height(tokens.extraPadding * 1.2f))

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("REQUESTED BY", fontSize = tokens.label, color = mutedText, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(2.dp))
                    Text(requestedBy, fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = title_color)

                    Spacer(Modifier.height(tokens.extraPadding * 0.8f))

                    Text("ITEMS", fontSize = tokens.label, color = mutedText, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(2.dp))
                    Text(itemsCountText, fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = Primary)
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text("DEPARTMENT", fontSize = tokens.label, color = mutedText, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(2.dp))
                    Text(department, fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = title_color)

                    Spacer(Modifier.height(tokens.extraPadding * 0.8f))

                    Text("REQUEST DATE", fontSize = tokens.label, color = mutedText, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(2.dp))
                    Text(requestDate, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = title_color)
                }
            }

            Spacer(Modifier.height(tokens.extraPadding * 1.2f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Required By:", fontSize = tokens.caption, color = mutedText)
                Text(requiredBy, fontSize = tokens.caption, fontWeight = FontWeight.SemiBold, color = title_color)
            }
        }
    }
}