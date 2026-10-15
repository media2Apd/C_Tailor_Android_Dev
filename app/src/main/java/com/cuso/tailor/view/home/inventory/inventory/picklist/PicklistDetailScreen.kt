@file:Suppress("unused","unusedVariable","AssignedValueIsNeverRead")

package com.cuso.tailor.view.home.inventory.inventory.picklist

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.inventory.CreatePackageItemRequest
import com.cuso.tailor.model.inventory.CreatePackageRequest
import com.cuso.tailor.model.inventory.PicklistDetailLineItemDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.ListSkeleton
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.viewmodel.InventoryViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@Composable
fun PicklistDetailScreen(
    picklistId: String,
    viewModel: InventoryViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
    onEditClick: (String) -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current

    val detail by viewModel.picklistDetail.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingPicklistDetail.collectAsStateWithLifecycle()
    val isConverting by viewModel.isCreatingPackage.collectAsStateWithLifecycle()
    val errorMessage by viewModel.picklistDetailError.collectAsStateWithLifecycle()

    LaunchedEffect(picklistId) {
        viewModel.fetchPicklistDetail(picklistId)
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearPicklistDetail()
        }
    }

    Scaffold(
        containerColor = Primary_background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(whiteBg)
            ) {
                TitleBar(
                    title = "Picklist Details",
                    onClose = onClose
                )
                HorizontalDivider(color = title_border)
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                isLoading && detail == null -> {
                    ListSkeleton()
                }

                errorMessage != null && detail == null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = errorMessage ?: "Failed to load picklist details",
                            color = redText,
                            fontSize = tokens.bodyMedium
                        )
                    }
                }

                detail != null -> {
                    val picklist = detail!!
                    val isAlreadyConverted = picklist.status.equals("Converted", ignoreCase = true)

                    val formattedDate = remember(picklist.date) {
                        val rawDate = picklist.date.orEmpty()
                        try {
                            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
                                timeZone = TimeZone.getTimeZone("UTC")
                            }
                            val parsed = parser.parse(rawDate)
                            if (parsed != null) {
                                SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(parsed)
                            } else {
                                if (rawDate.length >= 10) rawDate.substring(0, 10) else "—"
                            }
                        } catch (_: Exception) {
                            if (rawDate.length >= 10) rawDate.substring(0, 10) else "—"
                        }
                    }

                    val packageCode = picklist.convertedPackages.firstOrNull()?.packageNumber ?: "—"

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            horizontal = tokens.screenPadding,
                            vertical = tokens.extraPadding
                        ),
                        verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                    ) {
                        // ── 1. Top Card: Picklist Number ──
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(tokens.cardCornerRadius),
                                colors = CardDefaults.cardColors(containerColor = whiteBg)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(tokens.screenPadding),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = picklist.picklistNumber.orEmpty().ifBlank { "—" },
                                        fontSize = tokens.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }

                        // ── 2. Card: Assignee ──
                        item {
                            AttributeCard(
                                label = "Assignee",
                                value = picklist.assignee?.fullName ?: "Unassigned"
                            )
                        }

                        // ── 3. Card: Date ──
                        item {
                            AttributeCard(
                                label = "Date",
                                value = formattedDate
                            )
                        }

                        // ── 4. Card: Location ──
                        item {
                            AttributeCard(
                                label = "Location",
                                value = picklist.warehouse?.name ?: "—"
                            )
                        }

                        // ── 5. Card: Packages ──
                        item {
                            AttributeCard(
                                label = "Packages",
                                value = packageCode
                            )
                        }

                        // ── 6. Section Header ──
                        item {
                            Text(
                                text = "Items",
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        // ── 7. Line Items ──
                        items(picklist.items) { itemLine: PicklistDetailLineItemDto ->
                            val displayItemName = itemLine.itemName.orEmpty().ifBlank { "Item" }
                            val displayQty = itemLine.qty.toDouble()
                            val displayPicklistCode = picklist.picklistNumber.orEmpty().ifBlank { "—" }
                            val displaySoCode = itemLine.salesOrder?.orderCode.orEmpty().ifBlank { "—" }
                            val displayStatus = itemLine.status.orEmpty().ifBlank {
                                picklist.status.orEmpty().ifBlank { "Pending" }
                            }

                            PicklistItemCard(
                                itemName = displayItemName,
                                qty = displayQty,
                                picklistCode = displayPicklistCode,
                                soCode = displaySoCode,
                                carrier = "FedEx",
                                date = formattedDate,
                                status = displayStatus,
                                onMenuClick = { }
                            )
                        }

                        // ── 8. Bottom Action Buttons ──
                        // ── 8. Bottom Action Buttons ──
                        item {
                            Spacer(modifier = Modifier.height(tokens.extraPadding))

                            // Status condition checks
                            val currentStatus = picklist.status.orEmpty().trim()
                            val isYetToPackage = currentStatus.equals("Yet to Package", ignoreCase = true)
                            val isCompleted = currentStatus.equals("Completed", ignoreCase = true)
                            val isDelivered = currentStatus.equals("Delivered", ignoreCase = true) || currentStatus.equals("Converted", ignoreCase = true)

                            // 1. Edit is ONLY enabled when status is "Yet to Package"
                            val isEditEnabled = isYetToPackage && !isConverting

                            // 2. Convert to Packages is enabled for "Yet to Package" & "Completed", DISABLED for "Delivered/Converted"
                            val isConvertEnabled = (isYetToPackage || isCompleted) && !isDelivered && !isConverting

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Edit Button
                                OutlinedButton(
                                    onClick = {
                                        if (isEditEnabled) onEditClick(picklist.id)
                                    },
                                    enabled = isEditEnabled,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(tokens.buttonHeight),
                                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                                    border = BorderStroke(1.dp, if (isEditEnabled) grey_border else BorderGray),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (isEditEnabled) whiteBg else lightGray,
                                        contentColor = TextPrimary,
                                        disabledContainerColor = lightGray,
                                        disabledContentColor = mutedText
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Edit,
                                        contentDescription = null,
                                        tint = if (isEditEnabled) headerGrey else mutedText,
                                        modifier = Modifier.size(tokens.iconSize * 0.9f)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Edit",
                                        fontSize = tokens.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isEditEnabled) TextPrimary else mutedText
                                    )
                                }

                                // Convert to Packages Button
                                Button(
                                    onClick = {
                                        if (!isConvertEnabled) return@Button

                                        val itemsToPack = picklist.items.mapNotNull { item ->
                                            val itemId = item.id ?: return@mapNotNull null
                                            val qty = if (item.qtyPicked > 0) item.qtyPicked else item.qty
                                            if (qty > 0) CreatePackageItemRequest(picklistItemId = itemId, qty = qty) else null
                                        }

                                        if (itemsToPack.isEmpty()) {
                                            Toast.makeText(context, "No items available to pack", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }

                                        // Direct API execution with correct picklistIds array
                                        val request = CreatePackageRequest(
                                            picklistId = listOf(picklist.id),
                                            estimatedDelivery = null,
                                            items = itemsToPack
                                        )

                                        viewModel.createPackage(
                                            request = request,
                                            onSuccess = {
                                                Toast.makeText(context, "Converted to Package successfully!", Toast.LENGTH_SHORT).show()
                                                viewModel.fetchPicklistDetail(picklistId)
                                            }
                                        )
                                    },
                                    enabled = isConvertEnabled,
                                    modifier = Modifier
                                        .weight(1.3f)
                                        .height(tokens.buttonHeight),
                                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Primary,
                                        disabledContainerColor = disabled,
                                        disabledContentColor = whiteBg.copy(alpha = 0.6f)
                                    )
                                ) {
                                    if (isConverting) {
                                        CircularProgressIndicator(
                                            color = whiteBg,
                                            strokeWidth = 2.dp,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Outlined.Inventory2,
                                            contentDescription = null,
                                            tint = if (isConvertEnabled) whiteBg else whiteBg.copy(alpha = 0.6f),
                                            modifier = Modifier.size(tokens.iconSize * 0.9f)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isDelivered) "Delivered" else "Convert to Packages",
                                            fontSize = tokens.bodySmall,
                                            fontWeight = FontWeight.Medium,
                                            color = if (isConvertEnabled) whiteBg else whiteBg.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(tokens.screenPadding * 2))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Reusable Attribute Card for standard key-value displays.
 */
@Composable
private fun AttributeCard(
    label: String,
    value: String
) {
    val tokens = LocalAppTokens.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.7f),
        colors = CardDefaults.cardColors(containerColor = whiteBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(tokens.screenPadding)
        ) {
            Text(
                text = label,
                fontSize = tokens.caption,
                color = headerGrey,
                fontWeight = FontWeight.Normal
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Card displaying an individual line item inside the picklist.
 */
@Composable
fun PicklistItemCard(
    itemName: String,
    qty: Double,
    picklistCode: String,
    soCode: String,
    carrier: String,
    date: String,
    status: String,
    onMenuClick: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = whiteBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(tokens.screenPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = itemName,
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = title_color,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(tokens.extraPadding))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                        .background(lightGray)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Qty ${qty.toInt()}",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = textSubdued
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = picklistCode,
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = Primary
                )
                Text(
                    text = " · ",
                    fontSize = tokens.bodySmall,
                    color = mutedText,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = soCode,
                    fontSize = tokens.bodySmall,
                    color = headerGrey,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}