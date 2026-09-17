package com.cuso.mobile.view.home.inventory.procurement.barcode

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.mobile.adaptive_screen.AppDesignTokens
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.model.inventory.BarcodeItemDoc
import com.cuso.mobile.ui.theme.*
import com.cuso.mobile.view.composable.*
import com.cuso.mobile.viewmodel.InventoryViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun AllBarcodesScreen(
    onClose: () -> Unit,
    onCreateBarcode: () -> Unit = {},
    onBarcodeClick: (BarcodeItemDoc) -> Unit = {},
    viewModel: InventoryViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current

    // Observe barcode list and pagination states
    val barcodeList by viewModel.barcodesList.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingBarcodes.collectAsStateWithLifecycle()
    val isLoadingMore by viewModel.isLoadingMoreBarcodes.collectAsStateWithLifecycle()
    val canLoadMore by viewModel.canLoadMoreBarcodes.collectAsStateWithLifecycle()
    val successMessage by viewModel.barcodeSuccessMessage.collectAsStateWithLifecycle()
    val errorMessage by viewModel.barcodeErrorMessage.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var itemToDelete by remember { mutableStateOf<BarcodeItemDoc?>(null) }

    // Scroll state tracker for LazyColumn
    val listState = rememberLazyListState()

    // Unified initial fetch and debounced search (prevents duplicate API call on startup)
    var isInitialized by remember { mutableStateOf(false) }
    LaunchedEffect(searchQuery) {
        if (!isInitialized) {
            isInitialized = true
            viewModel.fetchAllBarcodes()
        } else {
            delay(400)
            viewModel.fetchAllBarcodes(search = searchQuery.trim().ifBlank { null })
        }
    }

    // Scroll listener: triggers next page fetch only when crossing the bottom threshold
    LaunchedEffect(listState, canLoadMore, searchQuery) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = (layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) + 1

            // Trigger when 2 items away from bottom
            totalItems > 0 && lastVisibleItemIndex >= (totalItems - 2)
        }
            .distinctUntilChanged()
            .collect { isNearBottom ->
                if (isNearBottom &&
                    canLoadMore &&
                    !viewModel.isLoadingMoreBarcodes.value &&
                    !viewModel.isLoadingBarcodes.value &&
                    searchQuery.isBlank()
                ) {
                    viewModel.loadMoreBarcodes()
                }
            }
    }

    // Delete Confirmation Dialog
    itemToDelete?.let { barcodeDoc ->
        DeleteModel(
            title = "Delete Barcode",
            message = "Are you sure you want to delete barcode \"${barcodeDoc.barcodeNumber}\"? It can be restored within 7 days, after which it is permanently removed.",
            onDismiss = { itemToDelete = null },
            onDelete = {
                val id = barcodeDoc.id
                if (id.isNotBlank()) {
                    viewModel.deleteBarcode(id) {
                        itemToDelete = null
                    }
                }
                itemToDelete = null
            }
        )
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
                    title = "All Barcodes",
                    onClose = onClose
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = onCreateBarcode,
                    containerColor = Primary,
                    contentColor = whiteBg,
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                    modifier = Modifier.height(tokens.buttonHeight)
                ) {
                    Text(
                        text = "Create Barcode",
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
                    placeholder = "Search Barcodes or SKU...",
                    showFilterIcon = true,
                    onFilterClick = { },
                    height = tokens.fieldHeight * 1.1f
                )

                HorizontalDivider(color = grey_border.copy(alpha = 0.5f), thickness = 1.dp)

                when {
                    isLoading && barcodeList.isEmpty() -> {
                        ListSkeleton()
                    }

                    barcodeList.isEmpty() -> {
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
                                        imageVector = Icons.Default.QrCode2,
                                        contentDescription = null,
                                        tint = Primary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    text = "No Barcodes Found",
                                    fontSize = tokens.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "Generate your first product barcode",
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
                                items = barcodeList,
                                key = { index, item -> item.id.ifBlank { "barcode_$index" } }
                            ) { _, item ->
                                BarcodeCardItem(
                                    item = item,
                                    tokens = tokens,
                                    onClick = { onBarcodeClick(item) },
                                    onToggleStatus = { viewModel.toggleBarcodeStatus(item.id) },
                                    onDeleteClick = { itemToDelete = item }
                                )
                            }

                            // Three-dot loader is displayed only while next page is actively loading
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

        DynamicIslandSuccess(
            message = successMessage,
            onDismiss = { viewModel.clearBarcodeAlerts() }
        )

        DynamicIslandError(
            message = errorMessage,
            onDismiss = { viewModel.clearBarcodeAlerts() }
        )
    }
}

@Composable
private fun BarcodeCardItem(
    item: BarcodeItemDoc,
    tokens: AppDesignTokens,
    onClick: () -> Unit,
    onToggleStatus: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val productName = item.item?.name ?: "Unnamed Product"
    val variantLabel = item.item?.variantLabel ?: "Standard"
    val barcodeNum = item.barcodeNumber.ifBlank { "—" }
    val skuCode = item.sku.ifBlank { item.item?.sku ?: "—" }
    val warehouseName = item.warehouse?.name ?: "Main Warehouse"
    val formattedDate = item.createdAt.take(10)
    val isActive = item.status.equals("Active", ignoreCase = true)

    val menuActions = remember(isActive) {
        listOf(
            MenuAction(
                label = "View Details",
                icon = Icons.Default.Visibility,
                tint = Primary,
                onClick = onClick
            ),
            MenuAction(
                label = if (isActive) "Mark Inactive" else "Mark Active",
                icon = Icons.Default.ToggleOn,
                tint = TextSecondary,
                onClick = onToggleStatus
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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding * 1.2f)
        ) {
            // Row 1: Barcode Number, Status Badge, Action Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.35f))
                        .background(light_grey)
                        .padding(horizontal = tokens.extraPadding * 0.8f, vertical = 3.dp)
                ) {
                    Text(
                        text = barcodeNum,
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Bold,
                        color = title_color
                    )
                }

                Spacer(Modifier.width(tokens.extraPadding * 0.8f))

                // Status Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 2f))
                        .background(if (isActive) greenBg else redBg)
                        .padding(horizontal = tokens.extraPadding * 0.8f, vertical = 3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(tokens.iconSize * 0.3f)
                                .clip(CircleShape)
                                .background(if (isActive) darkGreenBg else redText)
                        )
                        Spacer(Modifier.width(tokens.extraPadding * 0.4f))
                        Text(
                            text = if (isActive) "Active" else "Inactive",
                            fontSize = tokens.label,
                            fontWeight = FontWeight.Medium,
                            color = if (isActive) darkGreenBg else redText
                        )
                    }
                }

                Spacer(Modifier.weight(1f))

                ActionDropdownMenu(
                    actions = menuActions,
                    icon = Icons.Default.MoreVert
                )
            }

            Spacer(Modifier.height(tokens.extraPadding * 0.8f))

            // Row 2: Product Name & Type
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = productName,
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = title_color
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.35f))
                        .background(primary_light)
                        .padding(horizontal = tokens.extraPadding * 0.8f, vertical = 2.dp)
                ) {
                    Text(
                        text = item.barcodeType,
                        fontSize = tokens.label,
                        fontWeight = FontWeight.SemiBold,
                        color = Primary
                    )
                }
            }

            Spacer(Modifier.height(tokens.extraPadding))

            // Row 3: Grid (SKU, Variant, Label Size, Created)
            Row(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("SKU", fontSize = tokens.caption, color = mutedText)
                    Text(skuCode, fontSize = tokens.caption, fontWeight = FontWeight.SemiBold, color = title_color)
                }
                Spacer(Modifier.width(tokens.screenPadding))
                Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Variant", fontSize = tokens.caption, color = mutedText)
                    Text(variantLabel, fontSize = tokens.caption, fontWeight = FontWeight.SemiBold, color = title_color)
                }
            }

            Spacer(Modifier.height(tokens.extraPadding * 0.5f))

            Row(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Label Size", fontSize = tokens.caption, color = mutedText)
                    Text(item.labelSize.ifBlank { "Medium" }, fontSize = tokens.caption, fontWeight = FontWeight.SemiBold, color = title_color)
                }
                Spacer(Modifier.width(tokens.screenPadding))
                Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Created", fontSize = tokens.caption, color = mutedText)
                    Text(formattedDate, fontSize = tokens.caption, fontWeight = FontWeight.SemiBold, color = title_color)
                }
            }

            Spacer(Modifier.height(tokens.extraPadding * 1.2f))

            // Row 4: Warehouse & View Details Link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Storefront,
                        contentDescription = null,
                        tint = mutedText,
                        modifier = Modifier.size(tokens.iconSize * 0.9f)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = warehouseName,
                        fontSize = tokens.caption,
                        color = mutedText
                    )
                }

                Text(
                    text = "View Details",
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.SemiBold,
                    color = Primary,
                    modifier = Modifier.clickable { onClick() }
                )
            }
        }
    }
}