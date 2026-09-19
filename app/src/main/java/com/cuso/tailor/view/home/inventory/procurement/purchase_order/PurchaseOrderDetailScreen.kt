@file:Suppress(
    "unused",
    "unusedVariable",
    "AssignedValueIsNeverUsed",
    "AssignedValueIsNeverRead",
    "SpellCheckingInspection",
    "DEPRECATION"
)

package com.cuso.tailor.view.home.inventory.procurement.purchase_order

import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.R
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.inventory.PODetailAddress
import com.cuso.tailor.model.inventory.PODetailItem
import com.cuso.tailor.model.inventory.PODetailSupplier
import com.cuso.tailor.model.inventory.PODetailWarehouse
import com.cuso.tailor.model.inventory.PurchaseOrder
import com.cuso.tailor.model.inventory.ReceivePOItemRequest
import com.cuso.tailor.model.inventory.ReceivePurchaseOrderRequest
import com.cuso.tailor.ui.theme.BluePrimary
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.PrimaryBorder
import com.cuso.tailor.ui.theme.Primary_background
import com.cuso.tailor.ui.theme.TextPrimary
import com.cuso.tailor.ui.theme.TextSecondary
import com.cuso.tailor.ui.theme.badgeGrey
import com.cuso.tailor.ui.theme.close_color
import com.cuso.tailor.ui.theme.complete_button_bg
import com.cuso.tailor.ui.theme.darkGreenBg
import com.cuso.tailor.ui.theme.disabled
import com.cuso.tailor.ui.theme.dividerColor
import com.cuso.tailor.ui.theme.greenBg
import com.cuso.tailor.ui.theme.greentext
import com.cuso.tailor.ui.theme.grey_border
import com.cuso.tailor.ui.theme.iconMuted
import com.cuso.tailor.ui.theme.light_blue
import com.cuso.tailor.ui.theme.light_blue_border
import com.cuso.tailor.ui.theme.mutedText
import com.cuso.tailor.ui.theme.primary_light
import com.cuso.tailor.ui.theme.quickaccessBg
import com.cuso.tailor.ui.theme.redBg
import com.cuso.tailor.ui.theme.redText
import com.cuso.tailor.ui.theme.sectionBorder
import com.cuso.tailor.ui.theme.title_color
import com.cuso.tailor.ui.theme.whiteBg
import com.cuso.tailor.ui.theme.yellowBg
import com.cuso.tailor.ui.theme.yellowText
import com.cuso.tailor.view.composable.ListSkeleton
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.viewmodel.InventoryViewModel
import java.text.NumberFormat
import java.util.Locale

data class PODetailIconConfig(
    @DrawableRes val moreOptionsRes: Int = R.drawable.ic_report,
    @DrawableRes val editRes: Int = R.drawable.ic_pencil,
    @DrawableRes val receiveRes: Int = R.drawable.ic_tick_2,
    @DrawableRes val orderDateRes: Int = R.drawable.ic_calendar,
    @DrawableRes val expDeliveryRes: Int = R.drawable.truck,
    @DrawableRes val advancePaidRes: Int = R.drawable.ic_credit,
    @DrawableRes val availAdvanceRes: Int = R.drawable.pending,
    @DrawableRes val vendorStoreRes: Int = R.drawable.ic_contact,
    @DrawableRes val vendorAvatarRes: Int = R.drawable.person,
    @DrawableRes val emailRes: Int = R.drawable.ic_mail,
    @DrawableRes val phoneRes: Int = R.drawable.ic_contact,
    @DrawableRes val locationRes: Int = R.drawable.ic_location,
    @DrawableRes val copyRes: Int = R.drawable.ic_document,
    @DrawableRes val addressEditRes: Int = R.drawable.ic_pencil,
    @DrawableRes val contactPersonRes: Int = R.drawable.person,
    @DrawableRes val internalNotesRes: Int = R.drawable.ic_message,
    @DrawableRes val itemThumbnailRes: Int = R.drawable.box
)

@Composable
fun PODetailScreen(
    poId: String? = null,
    order: PurchaseOrder? = null,
    viewModel: InventoryViewModel,
    onEdit: () -> Unit,
    onClose: () -> Unit,
    iconConfig: PODetailIconConfig = PODetailIconConfig()
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val effectivePoId = poId?.takeIf { it.isNotBlank() } ?: order?.id.orEmpty()

    val orderDetail by viewModel.purchaseOrderDetail.collectAsStateWithLifecycle()
    val isLoadingDetail by viewModel.isLoadingPODetail.collectAsStateWithLifecycle()
    val poDetailError by viewModel.poDetailError.collectAsStateWithLifecycle()

    val billConvertData by viewModel.billConvertDetail.collectAsStateWithLifecycle()
    val poSuccessMessage by viewModel.poSuccessMessage.collectAsStateWithLifecycle()
    val poError by viewModel.purchaseOrdersError.collectAsStateWithLifecycle()

    var showReceiveDialog by remember { mutableStateOf(false) }
    var showBillConvertDialog by remember { mutableStateOf(false) }

    LaunchedEffect(effectivePoId) {
        if (effectivePoId.isNotBlank()) {
            viewModel.fetchPurchaseOrderDetail(effectivePoId)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearPurchaseOrderDetail()
        }
    }

    LaunchedEffect(poDetailError) {
        poDetailError?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(poSuccessMessage) {
        poSuccessMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearPOAlerts()
        }
    }

    LaunchedEffect(poError) {
        poError?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearPOAlerts()
        }
    }

    val poIdentifier = orderDetail?.poNumber?.takeIf { it.isNotBlank() }
        ?: order?.poNumber?.takeIf { it.isNotBlank() }
        ?: "-"

    val poStatus = orderDetail?.orderStatus?.takeIf { it.isNotBlank() }
        ?: order?.orderStatus?.takeIf { it.isNotBlank() }
        ?: "-"

    val lifecycleStatus = orderDetail?.lifecycleStatus?.takeIf { it.isNotBlank() } ?: "-"
    val poType = orderDetail?.poType?.takeIf { it.isNotBlank() } ?: "-"
    val currencyCode = orderDetail?.currency?.takeIf { it.isNotBlank() }

    val orderDate = orderDetail?.poDate?.substringBefore("T")?.takeIf { it.isNotBlank() } ?: "-"
    val expDeliveryDate = orderDetail?.eta?.substringBefore("T")?.takeIf { it.isNotBlank() } ?: "-"

    val internalNoteText = orderDetail?.internalNotes?.takeIf { it.isNotBlank() }
    val poRemarks = orderDetail?.remarks?.takeIf { it.isNotBlank() }

    val warehouse = orderDetail?.warehouse
    val supplier = orderDetail?.supplier

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary_background)
    ) {
        // Pinned Header Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = whiteBg
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                TitleBar(
                    title = "Purchase Detail",
                    onClose = onClose
                )
                HorizontalDivider(color = grey_border, thickness = 2.dp)
            }
        }

        if (isLoadingDetail && orderDetail == null) {
            ListSkeleton()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(Modifier.height(10.dp))
                // Section 1: Header & Primary Actions
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(whiteBg)
                        .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = poIdentifier,
                                    fontSize = tokens.bodySmall,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.width(tokens.extraPadding))
                                POStatusBadge(status = poStatus)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Purchase Orders / $poIdentifier • Type: $poType",
                                fontSize = tokens.bodySmall,
                                color = close_color
                            )
                        }

                        Icon(
                            painter = painterResource(id = iconConfig.moreOptionsRes),
                            contentDescription = "Options",
                            tint = iconMuted,
                            modifier = Modifier.size(tokens.iconSize)
                        )
                    }

                    Spacer(modifier = Modifier.height(tokens.extraPadding))
                    HorizontalDivider(color = grey_border, thickness = 2.dp)

                    Spacer(modifier = Modifier.height(tokens.extraPadding))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f)
                    ) {
                        OutlinedButton(
                            onClick = onEdit,
                            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                            border = BorderStroke(1.dp, sectionBorder),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(tokens.fieldHeight)
                        ) {
                            Icon(
                                painter = painterResource(id = iconConfig.editRes),
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(tokens.iconSize * 0.85f)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Edit",
                                fontSize = tokens.bodySmall,
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        OutlinedButton(
                            onClick = { showReceiveDialog = true },
                            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                            border = BorderStroke(1.dp, sectionBorder),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            modifier = Modifier
                                .weight(1.15f)
                                .height(tokens.fieldHeight)
                        ) {
                            Icon(
                                painter = painterResource(id = iconConfig.receiveRes),
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(tokens.iconSize * 0.85f)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Receive",
                                fontSize = tokens.bodySmall,
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Button(
                            onClick = {
                                val targetId = orderDetail?.id ?: effectivePoId
                                if (targetId.isNotBlank()) {
                                    viewModel.fetchPOForBillConvert(targetId)
                                    showBillConvertDialog = true
                                }
                            },
                            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            modifier = Modifier
                                .weight(1.4f)
                                .height(tokens.fieldHeight)
                        ) {
                            Text(
                                text = "Convert to Bill",
                                fontSize = tokens.bodySmall,
                                color = whiteBg,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                HorizontalDivider(color = grey_border, thickness = 2.dp)

                // Section 2: Metric Cards Grid
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding),
                    verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                    ) {
                        MetricInfoCard(
                            iconRes = iconConfig.orderDateRes,
                            iconBg = primary_light,
                            iconTint = Primary,
                            label = "ORDER DATE",
                            value = orderDate,
                            modifier = Modifier.weight(1f)
                        )
                        MetricInfoCard(
                            iconRes = iconConfig.expDeliveryRes,
                            iconBg = yellowBg,
                            iconTint = yellowText,
                            label = "EXP. DELIVERY",
                            value = expDeliveryDate,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                    ) {
                        MetricInfoCard(
                            iconRes = iconConfig.advancePaidRes,
                            iconBg = greenBg,
                            iconTint = darkGreenBg,
                            label = "ADVANCE PAID",
                            value = formatCurrency(orderDetail?.advancePaid, currencyCode),
                            modifier = Modifier.weight(1f)
                        )
                        MetricInfoCard(
                            iconRes = iconConfig.availAdvanceRes,
                            iconBg = quickaccessBg,
                            iconTint = BluePrimary,
                            label = "AVAIL. ADVANCE",
                            value = formatCurrency(orderDetail?.availableAdvance, currencyCode),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                HorizontalDivider(color = grey_border, thickness = 2.dp)
                Spacer(modifier = Modifier.height(tokens.extraPadding))

                // Section 4: Vendor Details
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(whiteBg)
                        .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding)
                ) {
                    VendorDetailsSection(
                        supplier = supplier,
                        storeIconRes = iconConfig.vendorStoreRes,
                        avatarIconRes = iconConfig.vendorAvatarRes,
                        emailIconRes = iconConfig.emailRes,
                        phoneIconRes = iconConfig.phoneRes
                    )
                }

                HorizontalDivider(color = grey_border, thickness = 2.dp)
                Spacer(modifier = Modifier.height(tokens.extraPadding))

                // Section 5: Delivery Address
                val formattedWarehouseAddress = buildWarehouseAddress(warehouse)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(whiteBg)
                        .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding)
                ) {
                    DeliveryAddressSection(
                        warehouse = warehouse,
                        warehouseAddress = formattedWarehouseAddress,
                        locationIconRes = iconConfig.locationRes,
                        copyIconRes = iconConfig.copyRes,
                        editIconRes = iconConfig.addressEditRes,
                        contactIconRes = iconConfig.contactPersonRes,
                        onCopyClicked = {
                            clipboardManager.setText(
                                AnnotatedString("${warehouse?.name ?: "-"}\n$formattedWarehouseAddress")
                            )
                            Toast.makeText(context, "Address copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        onEditClicked = onEdit
                    )
                }

                HorizontalDivider(color = grey_border, thickness = 2.dp)
                Spacer(modifier = Modifier.height(tokens.extraPadding))


                // Section 7: Internal Notes & Remarks
                if (!internalNoteText.isNullOrBlank() || !poRemarks.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(tokens.extraPadding))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(light_blue)
                            .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding)
                    ) {
                        InternalNotesSection(
                            note = internalNoteText ?: "-",
                            remarks = poRemarks,
                            notesIconRes = iconConfig.internalNotesRes
                        )
                    }
                }

                Spacer(modifier = Modifier.height(tokens.extraPadding))

                // Section 8: Ordered Items List
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Transparent)
                        .padding( vertical = tokens.extraPadding)
                ) {
                    OrderedItemsSection(
                        items = orderDetail?.items,
                        currency = currencyCode,
                        itemThumbnailRes = iconConfig.itemThumbnailRes
                    )
                }

                HorizontalDivider(color = grey_border, thickness = 2.dp)
                Spacer(modifier = Modifier.height(tokens.extraPadding))

                // Section 9: Cost Breakdown
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(whiteBg)
                        .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding)
                ) {
                    CostBreakdownSection(
                        subtotal = orderDetail?.subtotal,
                        tax = orderDetail?.taxTotal,
                        discount = orderDetail?.discount,
                        shipping = orderDetail?.shippingCost,
                        total = orderDetail?.grandTotal,
                        currency = currencyCode
                    )
                }

                Spacer(modifier = Modifier.height(tokens.buttonHeight + 40.dp))
            }
        }
    }

    // Receive Dialog
    if (showReceiveDialog) {
        AlertDialog(
            onDismissRequest = { showReceiveDialog = false },
            title = {
                Text(
                    text = "Receive Purchase Order",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = "Do you want to confirm reception of items for $poIdentifier?",
                    fontSize = tokens.bodyMedium,
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val req = ReceivePurchaseOrderRequest(
                            poId = orderDetail?.id ?: effectivePoId,
                            warehouseId = warehouse?.id ?: "",
                            subtotal = orderDetail?.subtotal ?: 0.0,
                            taxTotal = orderDetail?.taxTotal ?: 0.0,
                            grandTotal = orderDetail?.grandTotal ?: 0.0,
                            items = orderDetail?.items?.mapNotNull { item ->
                                val itemId = item.itemRef?.id ?: return@mapNotNull null
                                val poItemId = item.id ?: return@mapNotNull null
                                ReceivePOItemRequest(
                                    itemId = itemId,
                                    poItemId = poItemId,
                                    warehouseId = warehouse?.id ?: "",
                                    binId = "",
                                    qtyReceived = item.qty ?: 0.0,
                                    rate = item.rate ?: 0.0,
                                    taxPercent = item.taxPercent ?: 0.0,
                                    subtotal = item.subtotal ?: 0.0,
                                    taxAmount = item.taxAmount ?: 0.0,
                                    total = item.total ?: 0.0
                                )
                            } ?: emptyList()
                        )
                        viewModel.receivePurchaseOrder(req)
                        showReceiveDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text(text = "Confirm Receive", fontSize = tokens.bodySmall, color = whiteBg)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showReceiveDialog = false }) {
                    Text(text = "Cancel", fontSize = tokens.bodySmall, color = TextPrimary)
                }
            }
        )
    }

    // Bill Convert Dialog
    if (showBillConvertDialog && billConvertData != null) {
        AlertDialog(
            onDismissRequest = {
                showBillConvertDialog = false
                viewModel.clearBillConvertDetail()
            },
            title = {
                Text(
                    text = "Convert to Bill Summary",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = "PO: ${billConvertData?.poNumber ?: "-"}",
                        fontWeight = FontWeight.Bold,
                        fontSize = tokens.bodyMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = "Supplier: ${billConvertData?.supplier?.name ?: "-"}",
                        fontSize = tokens.bodySmall,
                        color = TextSecondary
                    )
                    Text(
                        text = "Items Ready for Billing: ${billConvertData?.items?.size ?: 0}",
                        fontSize = tokens.bodySmall,
                        color = TextSecondary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showBillConvertDialog = false
                        viewModel.clearBillConvertDetail()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text(text = "Done", fontSize = tokens.bodySmall, color = whiteBg)
                }
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────
// Design-Specific UI Sub-Components
// ─────────────────────────────────────────────────────────────

@Composable
fun POStatusBadge(status: String) {
    val tokens = LocalAppTokens.current
    val cleanStatus = status.trim().uppercase()
    val (bg, textCol) = when {
        cleanStatus.contains("APPROVED") || cleanStatus.contains("OPEN") || cleanStatus.contains("COMPLETED") ->
            greenBg to greentext
        cleanStatus.contains("PENDING") || cleanStatus.contains("DRAFT") ->
            yellowBg to yellowText
        cleanStatus.contains("CANCELLED") || cleanStatus.contains("REJECTED") ->
            redBg to redText
        else -> badgeGrey to TextSecondary
    }

    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(50.dp))
            .padding(horizontal = 10.dp, vertical = 0.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = cleanStatus,
            color = textCol,
            fontSize = tokens.caption,
        )
    }
}

@Composable
fun MetricInfoCard(
    @DrawableRes iconRes: Int,
    iconBg: Color,
    iconTint: Color,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val tokens = LocalAppTokens.current

    Surface(
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.8f),
        color = whiteBg,
        border = BorderStroke(1.dp, sectionBorder),
        shadowElevation = 0.5.dp,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(tokens.extraPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(tokens.fieldHeight * 0.95f)
                    .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(tokens.iconSize * 1.1f)
                )
            }
            Spacer(modifier = Modifier.width(tokens.extraPadding))
            Column {
                Text(
                    text = label,
                    fontSize = tokens.label,
                    color = close_color
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = value,
                    fontSize = tokens.bodySmall,
                    color = TextPrimary
                )
            }
        }
    }
}

@Composable
fun StatusLabelValueItem(label: String, value: String) {
    val tokens = LocalAppTokens.current

    Column(horizontalAlignment = Alignment.Start) {
        Text(text = label, fontSize = tokens.label, color = close_color, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(3.dp))
        Text(text = value, fontSize = tokens.bodySmall, color = TextPrimary)
    }
}

@Composable
fun VendorDetailsSection(
    supplier: PODetailSupplier?,
    @DrawableRes storeIconRes: Int,
    @DrawableRes avatarIconRes: Int,
    @DrawableRes emailIconRes: Int,
    @DrawableRes phoneIconRes: Int
) {
    val tokens = LocalAppTokens.current

    val vendorName = supplier?.name ?: "-"
    val supplierCode = supplier?.supplierCode ?: "-"
    val complianceStatus = supplier?.complianceStatus ?: "-"

    val contact = supplier?.contact
    val contactName = contact?.contactName ?: "-"
    val email = contact?.email ?: "-"
    val phone = contact?.phone ?: "-"
    val altPhone = contact?.alternatePhone ?: "-"
    val website = contact?.website ?: "-"

    val address = supplier?.address?.billing ?: supplier?.address?.shipping
    val formattedAddress = buildSupplierAddress(address)

    val tax = supplier?.tax
    val gstin = tax?.gstNumber ?: "-"
    val pan = tax?.pan ?: "-"
    val gstType = tax?.gstType ?: "-"
    val tdsApplicable = when (tax?.tdsApplicable) {
        true -> "Yes"
        false -> "No"
        null -> "-"
    }
    val reverseCharge = when (tax?.reverseCharge) {
        true -> "Yes"
        false -> "No"
        null -> "-"
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = storeIconRes),
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(tokens.iconSize)
                )
                Spacer(modifier = Modifier.width(tokens.extraPadding * 0.8f))
                Text(
                    text = "Vendor Details",
                    fontSize = tokens.bodyMedium,
                    color = TextPrimary
                )
            }

            Box(
                modifier = Modifier
                    .background(greenBg, RoundedCornerShape(tokens.cardCornerRadius * 0.3f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = complianceStatus.uppercase(),
                    fontSize = tokens.label,
                    fontWeight = FontWeight.Bold,
                    color = complete_button_bg
                )
            }
        }

        Spacer(modifier = Modifier.height(tokens.extraPadding))
        HorizontalDivider(color = grey_border, thickness = 2.dp)
        Spacer(modifier = Modifier.height(tokens.extraPadding))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(tokens.fieldHeight)
                    .clip(CircleShape)
                    .background(primary_light),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = avatarIconRes),
                    contentDescription = null,
                    tint = iconMuted,
                    modifier = Modifier.size(tokens.iconSize * 1.25f)
                )
            }

            Spacer(modifier = Modifier.width(tokens.extraPadding))

            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = vendorName,
                    fontSize = tokens.bodyMedium,
                    color = TextPrimary
                )

                Text(
                    text = formattedAddress,
                    fontSize = tokens.bodySmall,
                    color = TextSecondary,
                    lineHeight = tokens.bodyMedium
                )

                Spacer(modifier = Modifier.height(2.dp))
                HorizontalDivider(color = grey_border, thickness = 2.dp)
                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = emailIconRes),
                        contentDescription = null,
                        tint = iconMuted,
                        modifier = Modifier.size(tokens.iconSize * 0.8f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = email, fontSize = tokens.bodySmall, color = TextSecondary)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = phoneIconRes),
                        contentDescription = null,
                        tint = iconMuted,
                        modifier = Modifier.size(tokens.iconSize * 0.8f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (altPhone != "-") "$phone / $altPhone" else phone,
                        fontSize = tokens.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(tokens.extraPadding))
    }
}

@Composable
fun DeliveryAddressSection(
    warehouse: PODetailWarehouse?,
    warehouseAddress: String,
    @DrawableRes locationIconRes: Int,
    @DrawableRes copyIconRes: Int,
    @DrawableRes editIconRes: Int,
    @DrawableRes contactIconRes: Int,
    onCopyClicked: () -> Unit,
    onEditClicked: () -> Unit
) {
    val tokens = LocalAppTokens.current

    val warehouseName = warehouse?.name ?: "-"
    val warehouseCode = warehouse?.code ?: "-"
    val contactPerson = warehouse?.contactPerson ?: "-"
    val contactPhone = warehouse?.contactPhone ?: "-"

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = locationIconRes),
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(tokens.iconSize)
                )
                Spacer(modifier = Modifier.width(tokens.extraPadding * 0.8f))
                Text(
                    text = "Delivery Address",
                    fontSize = tokens.bodyMedium,
                    color = TextPrimary
                )
            }


            Row(horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                Icon(
                    painter = painterResource(id = copyIconRes),
                    contentDescription = "Copy",
                    tint = iconMuted,
                    modifier = Modifier
                        .size(tokens.iconSize)
                        .clickable { onCopyClicked() }
                )
                Icon(
                    painter = painterResource(id = editIconRes),
                    contentDescription = "Edit",
                    tint = iconMuted,
                    modifier = Modifier
                        .size(tokens.iconSize)
                        .clickable { onEditClicked() }
                )
            }
        }

        Spacer(modifier = Modifier.height(tokens.extraPadding))
        HorizontalDivider(color = grey_border, thickness = 2.dp)

        Spacer(modifier = Modifier.height(tokens.extraPadding))

        Text(
            text = "$warehouseName (Code: $warehouseCode)",
            fontSize = tokens.bodyMedium,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = warehouseAddress,
            fontSize = tokens.bodySmall,
            color = TextSecondary,
            lineHeight = tokens.bodyMedium
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = contactIconRes),
                contentDescription = null,
                tint = iconMuted,
                modifier = Modifier.size(tokens.iconSize * 0.8f)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Contact: $contactPerson ($contactPhone)",
                fontSize = tokens.bodySmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun LogisticsDetailsSection(
    shippingMethod: String,
    transportName: String,
    vehicleNumber: String,
    trackingNumber: String,
    freightTerms: String
) {
    val tokens = LocalAppTokens.current

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "SHIPPING & LOGISTICS",
            fontSize = tokens.bodySmall,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(tokens.extraPadding * 0.8f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatusLabelValueItem(label = "Shipping Method", value = shippingMethod)
            StatusLabelValueItem(label = "Transport Name", value = transportName)
            StatusLabelValueItem(label = "Vehicle No.", value = vehicleNumber)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatusLabelValueItem(label = "Tracking No.", value = trackingNumber)
            StatusLabelValueItem(label = "Freight Terms", value = freightTerms)
            Spacer(modifier = Modifier.width(tokens.fieldHeight))
        }
    }
}

@Composable
fun InternalNotesSection(
    note: String,
    remarks: String?,
    @DrawableRes notesIconRes: Int
) {
    val tokens = LocalAppTokens.current

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = notesIconRes),
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(tokens.iconSize)
            )
            Spacer(modifier = Modifier.width(tokens.extraPadding * 0.8f))
            Text(
                text = "Internal Notes & Remarks",
                fontSize = tokens.bodyMedium,
                color = TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "“$note”",
            fontSize = tokens.bodySmall,
            fontStyle = FontStyle.Italic,
            color = TextSecondary,
            lineHeight = tokens.bodyMedium
        )

        if (!remarks.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Remarks: $remarks",
                fontSize = tokens.bodySmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun OrderedItemsSection(
    items: List<PODetailItem>?,
    currency: String?,
    @DrawableRes itemThumbnailRes: Int
) {
    val tokens = LocalAppTokens.current
    val itemsList = items.orEmpty()

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = tokens.screenPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Ordered Items",
                fontSize = tokens.bodyMedium,
                color = TextPrimary
            )
            Text(
                text = "Showing ${itemsList.size} Items",
                fontSize = tokens.bodySmall,
                color = mutedText
            )
        }

        Spacer(modifier = Modifier.height(tokens.extraPadding))

        if (itemsList.isEmpty()) {
            Text(text = "No items recorded in this order", fontSize = tokens.bodySmall, color = mutedText)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                itemsList.forEach { item ->
                    val ref = item.itemRef
                    val title = ref?.name ?: "-"
                    val sku = ref?.sku?.takeIf { it.isNotBlank() } ?: "-"
                    val unit = ref?.unit?.takeIf { it.isNotBlank() } ?: "Units"

                    val orderedQty = item.qty?.let { qty ->
                        if (qty % 1.0 == 0.0) "${qty.toLong()} $unit" else "$qty $unit"
                    } ?: "-"

                    OrderItemCard(
                        title = title,
                        sku = sku,
                        status = item.receiveStatus ?: "-",
                        qty = orderedQty,
                        rate = formatCurrency(item.rate, currency),
                        amount = formatCurrency(item.total, currency),
                        itemThumbnailRes = itemThumbnailRes
                    )
                }
            }
        }
    }
}

@Composable
fun OrderItemCard(
    title: String,
    sku: String,
    status: String,
    qty: String,
    rate: String,
    amount: String,
    @DrawableRes itemThumbnailRes: Int? = null,
    modifier: Modifier = Modifier
) {
    val tokens = LocalAppTokens.current

    Surface(
        color = whiteBg,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding)
        ) {
            // Top Row: Thumbnail + Title/SKU + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Soft Rounded Thumbnail Placeholder
                    Box(
                        modifier = Modifier
                            .size(tokens.fieldHeight + 6.dp)
                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.6f))
                            .background(grey_border),
                        contentAlignment = Alignment.Center
                    ) {
                        if (itemThumbnailRes != null) {
                            Icon(
                                painter = painterResource(id = itemThumbnailRes),
                                contentDescription = null,
                                tint = iconMuted,
                                modifier = Modifier.size(tokens.iconSize * 1.15f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(tokens.extraPadding))

                    Column {
                        Text(
                            text = title,
                            fontSize = tokens.bodySmall,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = if (sku.startsWith("SKU:", ignoreCase = true)) sku else "SKU: $sku",
                            fontSize = tokens.bodySmall,
                            color = mutedText
                        )
                    }
                }

                ItemStatusPill(status = status)
            }

            Spacer(modifier = Modifier.height(tokens.extraPadding * 1.5f))

            // Bottom Row: Qty / Rate / Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Qty",
                        fontSize = tokens.bodyMedium,
                        color = close_color
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = qty,
                        fontSize = tokens.bodyMedium,
                        color = TextPrimary
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Rate",
                        fontSize = tokens.bodyMedium,
                        color = close_color
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = rate,
                        fontSize = tokens.bodyMedium,
                        color = TextPrimary
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Amount",
                        fontSize = tokens.bodyMedium,
                        color = close_color
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = amount,
                        fontSize = tokens.bodyMedium,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun ItemStatusPill(status: String) {
    val tokens = LocalAppTokens.current
    val clean = status.trim().uppercase()
    val (bg, border, textCol) = when {
        clean.contains("RECEIVED") -> Triple(greenBg, greenBg, complete_button_bg)
        clean.contains("TRANSIT") -> Triple(primary_light, disabled, Primary)
        clean.contains("CANCEL") || clean.contains("REJECT") -> Triple(redBg, redBg, redText)
        else -> Triple(yellowBg, yellowBg, yellowText)
    }

    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(50.dp))
            .border(BorderStroke(1.dp, border), RoundedCornerShape(50.dp))
            .padding(horizontal = 12.dp, vertical = 0.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = clean,
            color = textCol,
            fontSize = tokens.caption,
            letterSpacing = 0.3.sp
        )
    }
}

@Composable
fun CostBreakdownSection(
    subtotal: Double?,
    tax: Double?,
    discount: Double?,
    shipping: Double?,
    total: Double?,
    currency: String?
) {
    val tokens = LocalAppTokens.current

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "COST BREAKDOWN",
            fontSize = tokens.bodyMedium,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(tokens.extraPadding))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Subtotal", fontSize = tokens.bodySmall, color = TextSecondary)
            Text(
                text = formatCurrency(subtotal, currency),
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        }

        if (discount != null && discount > 0.0) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Discount", fontSize = tokens.bodySmall, color = TextSecondary)
                Text(
                    text = "- ${formatCurrency(discount, currency)}",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = darkGreenBg
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        HorizontalDivider(color = grey_border, thickness = 2.dp)
        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Tax Total", fontSize = tokens.bodySmall, color = TextSecondary)
            Text(
                text = formatCurrency(tax, currency),
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Shipping Charges", fontSize = tokens.bodySmall, color = TextSecondary)
            Text(
                text = formatCurrency(shipping, currency),
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(tokens.extraPadding))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Total", fontSize = tokens.bodyMedium, color = TextPrimary)
            Text(
                text = formatCurrency(total, currency),
                fontSize = tokens.bodyMedium,
                color = Primary
            )
        }
    }
}

private fun buildSupplierAddress(address: PODetailAddress?): String {
    if (address == null) return "-"
    val parts = listOfNotNull(
        address.flatNo?.takeIf { it.isNotBlank() },
        address.street?.takeIf { it.isNotBlank() },
        address.city?.takeIf { it.isNotBlank() },
        address.state?.takeIf { it.isNotBlank() },
        address.country?.takeIf { it.isNotBlank() },
        address.pincode?.takeIf { it.isNotBlank() }
    )
    return if (parts.isNotEmpty()) parts.joinToString(", ") else "-"
}

private fun buildWarehouseAddress(warehouse: PODetailWarehouse?): String {
    val addr = warehouse?.address ?: return "-"
    val parts = listOfNotNull(
        addr.address?.takeIf { it.isNotBlank() },
        addr.city?.takeIf { it.isNotBlank() },
        addr.state?.takeIf { it.isNotBlank() },
        addr.country?.takeIf { it.isNotBlank() },
        addr.pincode?.takeIf { it.isNotBlank() }
    )
    return if (parts.isNotEmpty()) parts.joinToString(", ") else "-"
}

private fun formatCurrency(amount: Double?, currency: String?): String {
    if (amount == null) return "-"
    val formatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN"))
    return try {
        if (!currency.isNullOrBlank()) {
            formatter.currency = java.util.Currency.getInstance(currency)
        }
        formatter.format(amount)
    } catch (_: Exception) {
        formatter.format(amount)
    }
}