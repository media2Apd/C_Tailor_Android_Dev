package com.cuso.mobile.view.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cuso.mobile.adaptive_screen.LocalAppTokens

/* ---------------------------------------------------------------------- */
/*  Stock Summary  /  Low Stock  — shared "inventory status" list item     */
/* ---------------------------------------------------------------------- */

data class InventoryStatusUiModel(
    val productName: String,
    val sku: String,
    val warehouse: String,
    val badgeText: String,
    val badgeType: ReportBadgeType,
    val availablePcs: Int,
    val reservedPcs: Int,
    val allocatedPcs: Int,
    val totalPcs: Int,
    val updatedAt: String
)

@Composable
fun InventoryStatusListItem(
    item: InventoryStatusUiModel,
    modifier: Modifier = Modifier
) {
    val tokens = LocalAppTokens.current
    val progressFraction = if (item.totalPcs > 0) item.availablePcs / item.totalPcs.toFloat() else 0f
    val progressColor = when (item.badgeType) {
        ReportBadgeType.CRITICAL -> ReportStatusColors.CriticalText
        ReportBadgeType.WARNING -> ReportStatusColors.WarningText
        ReportBadgeType.HEALTHY -> ReportStatusColors.HealthyText
        ReportBadgeType.NEUTRAL -> ReportStatusColors.NeutralText
    }
    val availableColor = progressColor

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = tokens.screenPadding, vertical = tokens.screenPadding / 1.2f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.productName,
                fontSize = tokens.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111111)
            )
            ReportStatusBadge(text = item.badgeText, type = item.badgeType)
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "SKU: ${item.sku} \u00B7 Warehouse: ${item.warehouse}",
            fontSize = tokens.bodySmall,
            color = Color(0xFF9CA3AF)
        )

        Spacer(modifier = Modifier.height(tokens.screenPadding / 1.3f))
        Row(modifier = Modifier.fillMaxWidth()) {
            ReportMiniStatColumn(
                label = "Available",
                value = "${item.availablePcs} pcs",
                valueColor = availableColor,
                modifier = Modifier.weight(1f)
            )
            ReportMiniStatColumn(
                label = "Reserved",
                value = "${item.reservedPcs} pcs",
                modifier = Modifier.weight(1f)
            )
            ReportMiniStatColumn(
                label = "Allocated",
                value = "${item.allocatedPcs} pcs",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(tokens.screenPadding / 1.3f))
        ReportProgressBar(fraction = progressFraction, fillColor = progressColor)

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "${item.availablePcs} Available \u2022 ${item.totalPcs} Total",
            fontSize = tokens.caption,
            color = ReportStatusColors.MutedGray
        )

        Spacer(modifier = Modifier.height(6.dp))
        ReportUpdatedAtRow(text = "Updated ${item.updatedAt}")
    }
    Divider(color = ReportStatusColors.DividerGray, thickness = 1.dp)
}

/* ---------------------------------------------------------------------- */
/*  Warehouse Report — warehouse list item                                 */
/* ---------------------------------------------------------------------- */

data class WarehouseUiModel(
    val name: String,
    val occupancyPercent: Int,
    val badgeText: String,
    val badgeType: ReportBadgeType,
    val manager: String,
    val products: String,
    val availableStock: String,
    val reservedStock: String,
    val updatedAt: String
)

@Composable
fun WarehouseListItem(
    item: WarehouseUiModel,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalAppTokens.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = tokens.screenPadding, vertical = tokens.screenPadding / 1.2f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.name,
                    fontSize = tokens.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111111)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${item.occupancyPercent}%",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.primary
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                ReportStatusBadge(text = item.badgeText, type = item.badgeType)
                ReportKebabMenuIcon(onClick = onMoreClick)
            }
        }

        Text(
            text = "Manager: ${item.manager}",
            fontSize = tokens.bodySmall,
            color = Color(0xFF9CA3AF)
        )

        Spacer(modifier = Modifier.height(tokens.screenPadding / 1.4f))
        ReportLabelValueRow(label = "Products", value = item.products)
        Spacer(modifier = Modifier.height(6.dp))
        ReportLabelValueRow(label = "Available Stock", value = item.availableStock)
        Spacer(modifier = Modifier.height(6.dp))
        ReportLabelValueRow(label = "Reserved Stock", value = item.reservedStock)

        Spacer(modifier = Modifier.height(tokens.screenPadding / 1.4f))
        ReportUpdatedAtRow(text = "Updated ${item.updatedAt}")
    }
    Divider(color = ReportStatusColors.DividerGray, thickness = 1.dp)
}

/* ---------------------------------------------------------------------- */
/*  Purchase Report — purchase order list item                             */
/* ---------------------------------------------------------------------- */

data class PurchaseOrderUiModel(
    val poNumber: String,
    val paymentStatus: String,
    val badgeText: String,
    val badgeType: ReportBadgeType,
    val supplier: String,
    val items: Int,
    val purchaseDate: String,
    val warehouse: String,
    val totalAmount: String,
    val updatedAt: String
)

@Composable
fun PurchaseOrderListItem(
    item: PurchaseOrderUiModel,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalAppTokens.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = tokens.screenPadding, vertical = tokens.screenPadding / 1.2f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.poNumber,
                    fontSize = tokens.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111111)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = item.paymentStatus,
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.primary
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                ReportStatusBadge(text = item.badgeText, type = item.badgeType)
                ReportKebabMenuIcon(onClick = onMoreClick)
            }
        }

        Text(
            text = "Supplier: ${item.supplier} \u00B7 Items: ${item.items}",
            fontSize = tokens.bodySmall,
            color = Color(0xFF9CA3AF)
        )

        Spacer(modifier = Modifier.height(tokens.screenPadding / 1.4f))
        ReportLabelValueRow(label = "Purchase Date", value = item.purchaseDate)
        Spacer(modifier = Modifier.height(6.dp))
        ReportLabelValueRow(label = "Warehouse", value = item.warehouse)
        Spacer(modifier = Modifier.height(6.dp))
        ReportLabelValueRow(label = "Total Amount", value = item.totalAmount)

        Spacer(modifier = Modifier.height(tokens.screenPadding / 1.4f))
        ReportUpdatedAtRow(text = "Updated ${item.updatedAt}")
    }
    Divider(color = ReportStatusColors.DividerGray, thickness = 1.dp)
}

/* ---------------------------------------------------------------------- */
/*  Dead Stock Report — dead stock list item                               */
/* ---------------------------------------------------------------------- */

data class DeadStockUiModel(
    val productName: String,
    val sku: String,
    val noMovementDays: Int,
    val availablePcs: Int,
    val warehouse: String,
    val inventoryValue: String,
    val updatedAt: String
)

@Composable
fun DeadStockListItem(
    item: DeadStockUiModel,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalAppTokens.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = tokens.screenPadding, vertical = tokens.screenPadding / 1.2f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.productName,
                fontSize = tokens.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111111)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                ReportStatusBadge(text = "Dead Stock", type = ReportBadgeType.NEUTRAL)
                ReportKebabMenuIcon(onClick = onMoreClick)
            }
        }

        Row {
            Text(
                text = "SKU: ${item.sku} \u00B7 No Movement: ",
                fontSize = tokens.bodySmall,
                color = Color(0xFF9CA3AF)
            )
            Text(
                text = "${item.noMovementDays} Days",
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = ReportStatusColors.CriticalText
            )
        }

        Spacer(modifier = Modifier.height(tokens.screenPadding / 1.4f))
        ReportLabelValueRow(label = "Available", value = "${item.availablePcs} pcs")
        Spacer(modifier = Modifier.height(6.dp))
        ReportLabelValueRow(label = "Warehouse", value = item.warehouse)
        Spacer(modifier = Modifier.height(6.dp))
        ReportLabelValueRow(label = "Inventory Value", value = item.inventoryValue)

        Spacer(modifier = Modifier.height(tokens.screenPadding / 1.4f))
        ReportUpdatedAtRow(text = "Updated ${item.updatedAt}")
    }
    Divider(color = ReportStatusColors.DividerGray, thickness = 1.dp)
}