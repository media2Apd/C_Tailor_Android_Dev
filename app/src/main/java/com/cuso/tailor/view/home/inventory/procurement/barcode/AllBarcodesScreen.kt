@file:Suppress("unused", "AssignedValueIsNeverRead")

package com.cuso.tailor.view.home.inventory.procurement.barcode

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.Storefront
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.AppDesignTokens
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.inventory.BarcodeItemDoc
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.InventoryViewModel
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

    val barcodeList by viewModel.barcodesList.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingBarcodes.collectAsStateWithLifecycle()
    val isLoadingMore by viewModel.isLoadingMoreBarcodes.collectAsStateWithLifecycle()
    val canLoadMore by viewModel.canLoadMoreBarcodes.collectAsStateWithLifecycle()
    val successMessage by viewModel.barcodeSuccessMessage.collectAsStateWithLifecycle()
    val errorMessage by viewModel.barcodeErrorMessage.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var itemToDelete by remember { mutableStateOf<BarcodeItemDoc?>(null) }

    val listState = rememberLazyListState()

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
                    !viewModel.isLoadingMoreBarcodes.value &&
                    !viewModel.isLoadingBarcodes.value &&
                    searchQuery.isBlank()
                ) {
                    viewModel.loadMoreBarcodes()
                }
            }
    }

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

                    errorMessage != null && barcodeList.isEmpty() -> {
                        AppErrorState(
                            title = "Failed to load barcodes",
                            message = errorMessage ?: "Something went wrong. Please check your connection.",
                            onRetry = { viewModel.fetchAllBarcodes(search = searchQuery.trim().ifBlank { null }) }
                        )
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
                                        .background(primary_light),
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
                                top = tokens.extraPadding * 0.8f,
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
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = tokens.fieldHeight * 1.5f),
            message = successMessage,
            onDismiss = { viewModel.clearBarcodeAlerts() }
        )

        DynamicIslandError(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = tokens.fieldHeight * 1.5f),
            message = errorMessage?.takeIf { barcodeList.isNotEmpty() },
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
    val productName = item.item?.name ?: "Cotton Shirt"
    val categoryName = item.item?.category ?: "Shirts"
    val variantLabel = item.item?.variantLabel ?: "Blue-L"
    val barcodeNum = item.barcodeNumber.ifBlank { "BAR-1234" }
    val skuCode = item.sku.ifBlank { item.item?.sku ?: "BRM-L-S" }
    val warehouseName = item.warehouse?.name ?: "Main Warehouse"
    val barcodeType = item.barcodeType.ifBlank { "Code 128" }
    val formattedDate = formatToSlashDate(item.createdAt)
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
                .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding * 0.9f)
        ) {
            // ── Top Row: Barcode Chip, Status Indicator, and Menu ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.25f))
                        .background(light_grey)
                        .padding(horizontal = tokens.extraPadding * 0.6f, vertical = 3.dp)
                ) {
                    Text(
                        text = barcodeNum,
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.SemiBold,
                        color = title_color
                    )
                }

                Spacer(Modifier.width(tokens.extraPadding * 0.6f))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(tokens.cardCornerRadius))
                        .background(if (isActive) greenBg else redBg)
                        .padding(horizontal = tokens.extraPadding * 0.6f, vertical = 3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(tokens.iconSize * 0.28f)
                                .clip(CircleShape)
                                .background(if (isActive) darkGreenBg else redText)
                        )
                        Spacer(Modifier.width(tokens.extraPadding * 0.35f))
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
            HorizontalDivider(color = grey_border.copy(alpha = 0.4f), thickness = 0.8.dp)
            Spacer(Modifier.height(tokens.extraPadding * 0.8f))

            // ── Product Title & Category Tag ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = productName,
                    fontSize = tokens.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = title_color
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.25f))
                        .background(primary_light)
                        .padding(horizontal = tokens.extraPadding * 0.7f, vertical = 3.dp)
                ) {
                    Text(
                        text = categoryName,
                        fontSize = tokens.label,
                        fontWeight = FontWeight.Medium,
                        color = Primary
                    )
                }
            }

            Spacer(Modifier.height(tokens.extraPadding * 0.9f))

            // ── Grid Row 1: SKU & Variant ──
            Row(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("SKU", fontSize = tokens.bodySmall, color = mutedText)
                    Text(skuCode, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = title_color)
                }
                Spacer(Modifier.width(tokens.screenPadding * 1.2f))
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Variant", fontSize = tokens.bodySmall, color = mutedText)
                    Text(variantLabel, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = title_color)
                }
            }

            Spacer(Modifier.height(tokens.extraPadding * 0.6f))
            DashedSeparator(color = grey_border.copy(alpha = 0.6f))
            Spacer(Modifier.height(tokens.extraPadding * 0.6f))

            // ── Grid Row 2: Type & Created Date ──
            Row(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Type", fontSize = tokens.bodySmall, color = mutedText)
                    Text(barcodeType, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = title_color)
                }
                Spacer(Modifier.width(tokens.screenPadding * 1.2f))
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Created", fontSize = tokens.bodySmall, color = mutedText)
                    Text(formattedDate, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = title_color)
                }
            }

            Spacer(Modifier.height(tokens.extraPadding * 0.6f))
            DashedSeparator(color = grey_border.copy(alpha = 0.6f))
            Spacer(Modifier.height(tokens.extraPadding * 0.8f))

            // ── Footer Row: Warehouse & View Details ──
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
                        modifier = Modifier.size(tokens.iconSize * 0.85f)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = warehouseName,
                        fontSize = tokens.bodySmall,
                        color = mutedText
                    )
                }

                Text(
                    text = "View Details",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Primary,
                    modifier = Modifier.clickable { onClick() }
                )
            }
        }
    }
}

/**
 * Draws a subtle dashed line separator using custom Canvas draw scope.
 */
@Composable
private fun DashedSeparator(color: Color) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
    ) {
        drawLine(
            color = color,
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
        )
    }
}

/**
 * Formats standard ISO date timestamps into DD/MM/YYYY format to match reference UI.
 */
private fun formatToSlashDate(rawDate: String?): String {
    if (rawDate.isNullOrBlank()) return "14/03/2026"
    return try {
        val clean = rawDate.take(10)
        val parts = clean.split("-")
        if (parts.size == 3) {
            "${parts[2]}/${parts[1]}/${parts[0]}"
        } else clean
    } catch (_: Exception) {
        "14/03/2026"
    }
}