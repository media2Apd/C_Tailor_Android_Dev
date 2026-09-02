@file:Suppress("unused", "SpellCheckingInspection", "UNUSED_PARAMETER")

package com.cuso.mobile.view.home.inventory.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.Primary
import com.cuso.mobile.ui.theme.whiteBg

@Composable
fun SectionHeader(title: String) {
    val tokens = LocalAppTokens.current
    Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
        Text(title, fontSize = tokens.bodyMedium, fontWeight = FontWeight.SemiBold, color = Color(0xFF111827))
        Spacer(Modifier.height(6.dp))
        HorizontalDivider(color = Color(0xFFF1F5F9))
    }
}

@Composable
fun HeaderAddButton(
    label: String,
    onClick: () -> Unit
) {
    val tokens = LocalAppTokens.current
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Primary),
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, fontSize = tokens.label, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun WarehouseDetailCard(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    status: String? = null,
    locationLabel: String = "Floor",
    locationName: String? = null,
    sequenceOrder: String = "1",
    totalSections: String? = null,
    linkedCategory: String? = null,
    rackType: String? = null,
    binType: String? = null,
    totalRacks: String,
    totalBins: String,
    showFourGridBoxes: Boolean = false,
    capacityMetrics: List<Pair<String, String>>,
    onEditClick: () -> Unit = {},
    onMenuClick: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp)
    ) {
        // Header Row: Icon + Title/Subtitle + Status + Edit / Menu
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFEEF2FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                }
                Column {
                    Text(title, fontSize = tokens.bodyMedium, fontWeight = FontWeight.SemiBold, color = Color(0xFF111827))
                    if (subtitle != null) {
                        Text(subtitle, fontSize = tokens.caption, color = Color(0xFF6B7280))
                    }
                    if (status != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFDCFCE7))
                                .padding(horizontal = 6.dp, vertical = 1.dp)
                        ) {
                            Text(status, fontSize = 9.sp, color = Color(0xFF16A34A), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onEditClick,
                    modifier = Modifier.height(28.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFF6B7280), modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Edit", fontSize = tokens.caption, color = Color(0xFF374151))
                }
                IconButton(onClick = onMenuClick, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(18.dp))
                }
            }
        }

        Spacer(Modifier.height(10.dp))
        HorizontalDivider(color = Color(0xFFF1F5F9))
        Spacer(Modifier.height(10.dp))

        if (showFourGridBoxes) {
            // 4-Box Grid layout (Location Structure Screen)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricBox(title = "Sequence Order", value = sequenceOrder, modifier = Modifier.weight(1f))
                    MetricBox(title = "Total Sections", value = totalSections ?: "-", modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricBox(title = "Total Racks", value = totalRacks, modifier = Modifier.weight(1f))
                    MetricBox(title = "Total Bins", value = totalBins, modifier = Modifier.weight(1f))
                }
            }
        } else {
            // Key-Value Stack layout (Floor, Section, Rack, Bin Screens)
            if (locationName != null) {
                MetricRow(locationLabel, locationName)
            }
            MetricRow("Sequence Order", sequenceOrder)
            if (totalSections != null) {
                MetricRow("Total Sections", totalSections)
            }
            if (linkedCategory != null) {
                MetricRow("Linked Category", linkedCategory)
            }
            if (rackType != null) {
                MetricRow("Rack Type", rackType)
            }
            if (binType != null) {
                MetricRow("Bin Type", binType)
            }

            Spacer(Modifier.height(10.dp))

            // 2 Metric Boxes at bottom
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricBox(title = "Total Racks", value = totalRacks, modifier = Modifier.weight(1f))
                MetricBox(title = "Total Bins", value = totalBins, modifier = Modifier.weight(1f))
            }
        }

        Spacer(Modifier.height(12.dp))

        // Capacity Summary
        Text("CAPACITY SUMMARY", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF6B7280))
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            capacityMetrics.forEach { (label, value) ->
                Column {
                    Text(label, fontSize = tokens.caption, color = Color(0xFF9CA3AF))
                    Text(value, fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = Color(0xFF111827))
                }
            }
        }
    }
}

@Composable
private fun MetricBox(title: String, value: String, modifier: Modifier = Modifier) {
    val tokens = LocalAppTokens.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF8FAFC))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Column {
            Text(title, fontSize = tokens.caption, color = Color(0xFF6B7280))
            Spacer(Modifier.height(2.dp))
            Text(value, fontSize = tokens.bodyLarge, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
        }
    }
}

@Composable
private fun MetricRow(label: String, value: String) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = tokens.bodySmall, color = Color(0xFF6B7280))
        Text(value, fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = Color(0xFF111827))
    }
}