package com.cuso.mobile.view.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cuso.mobile.adaptive_screen.LocalAppTokens

/** Big bold title on the left, "x" close button on the right. */
@Composable
fun ReportHeader(
    title: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = tokens.screenPadding, vertical = tokens.screenPadding / 1.3f),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = tokens.h1,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF111111)
        )
        IconButton(onClick = onClose) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = Color(0xFF6B7280)
            )
        }
    }
}

/**
 * "Reports > Inventory > Stock Summary" on the left, "Export" action on the right.
 * The final breadcrumb segment is rendered in the primary color to indicate the active screen.
 */
@Composable
fun ReportBreadcrumbBar(
    path: List<String>,
    onExportClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = tokens.screenPadding, vertical = tokens.screenPadding / 2.5f),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            path.forEachIndexed { index, segment ->
                val isLast = index == path.lastIndex
                Text(
                    text = segment,
                    fontSize = tokens.bodySmall,
                    fontWeight = if (isLast) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isLast) MaterialTheme.colorScheme.primary else Color(0xFF9CA3AF)
                )
                if (!isLast) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = null,
                        tint = Color(0xFFC7CBD1),
                        modifier = Modifier.size(tokens.iconSize * 0.7f)
                    )
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onExportClick() }
        ) {
            Icon(
                imageVector = Icons.Outlined.Upload,
                contentDescription = "Export",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(tokens.iconSize * 0.8f)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Export",
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/** Rounded search field with a leading search icon, plus a separate filter icon button. */
@Composable
fun ReportSearchFilterBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search Payment..."
) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = tokens.screenPadding, vertical = tokens.screenPadding / 2.5f),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .height(tokens.fieldHeight)
                .background(Color(0xFFF7F7F9), RoundedCornerShape(tokens.cardCornerRadius / 1.5f))
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = Color(0xFF9CA3AF),
                modifier = Modifier.size(tokens.iconSize)
            )
            Spacer(modifier = Modifier.width(8.dp))
            androidx.compose.foundation.text.BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = tokens.bodyMedium,
                    color = Color(0xFF111111)
                ),
                decorationBox = { innerTextField ->
                    if (query.isEmpty()) {
                        Text(
                            text = placeholder,
                            fontSize = tokens.bodyMedium,
                            color = Color(0xFF9CA3AF)
                        )
                    }
                    innerTextField()
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Box(
            modifier = Modifier
                .size(tokens.fieldHeight)
                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(tokens.cardCornerRadius / 1.5f))
                .clickable { onFilterClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.FilterList,
                contentDescription = "Filter",
                tint = Color(0xFF374151),
                modifier = Modifier.size(tokens.iconSize)
            )
        }
    }
}

/** One cell inside a [ReportStatGrid]: a small caption label above a bold value. */
data class ReportStat(
    val label: String,
    val value: String,
    val valueColor: Color = Color(0xFF111111)
)

/**
 * 2x2 (or Nx2) summary grid at the top of every report, e.g.
 * Products / Available, Reserved / Low Stock.
 */
@Composable
fun ReportStatGrid(
    stats: List<ReportStat>,
    modifier: Modifier = Modifier
) {
    val tokens = LocalAppTokens.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = tokens.screenPadding)
    ) {
        stats.chunked(2).forEachIndexed { rowIndex, rowStats ->
            if (rowIndex > 0) {
                Spacer(modifier = Modifier.height(tokens.screenPadding / 1.2f))
                Divider(color = ReportStatusColors.DividerGray, thickness = 1.dp)
                Spacer(modifier = Modifier.height(tokens.screenPadding / 1.2f))
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                rowStats.forEachIndexed { colIndex, stat ->
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stat.label,
                            fontSize = tokens.bodySmall,
                            color = Color(0xFF9CA3AF)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stat.value,
                            fontSize = tokens.h2,
                            fontWeight = FontWeight.Bold,
                            color = stat.valueColor
                        )
                    }
                    if (colIndex == 0 && rowStats.size > 1) {
                        Spacer(modifier = Modifier.width(tokens.screenPadding))
                    }
                }
            }
        }
    }
}

/** Section heading such as "Stock Status" / "Low Stock Items" / "Warehouse List". */
@Composable
fun ReportSectionHeader(text: String, modifier: Modifier = Modifier) {
    val tokens = LocalAppTokens.current
    Text(
        text = text,
        fontSize = tokens.h2,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF111111),
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = tokens.screenPadding,
                vertical = tokens.screenPadding / 1.4f
            )
    )
}

/** "🕐 Updated Today 10:45 AM" footer row used on every list item card. */
@Composable
fun ReportUpdatedAtRow(text: String, modifier: Modifier = Modifier) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.AccessTime,
            contentDescription = null,
            tint = ReportStatusColors.MutedGray,
            modifier = Modifier.size(tokens.iconSize * 0.65f)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            fontSize = tokens.caption,
            color = ReportStatusColors.MutedGray
        )
    }
}

/** Label on the left, value on the right — used by Warehouse / Purchase / Dead Stock rows. */
@Composable
fun ReportLabelValueRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = Color(0xFF111111),
    valueBold: Boolean = true
) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = tokens.bodySmall, color = Color(0xFF9CA3AF))
        Text(
            text = value,
            fontSize = tokens.bodySmall,
            fontWeight = if (valueBold) FontWeight.SemiBold else FontWeight.Normal,
            color = valueColor
        )
    }
}

/** Label above value — used by the Available / Reserved / Allocated mini-columns. */
@Composable
fun ReportMiniStatColumn(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = Color(0xFF111111)
) {
    val tokens = LocalAppTokens.current
    Column(modifier = modifier) {
        Text(text = label, fontSize = tokens.caption, color = Color(0xFF9CA3AF))
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = tokens.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

@Composable
fun ReportKebabMenuIcon(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val tokens = LocalAppTokens.current
    IconButton(onClick = onClick, modifier = modifier.size(tokens.iconSize * 1.4f)) {
        Icon(
            imageVector = Icons.Outlined.MoreVert,
            contentDescription = "More options",
            tint = Color(0xFF9CA3AF)
        )
    }
}