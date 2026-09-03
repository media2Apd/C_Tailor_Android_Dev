@file:Suppress("unused", "SpellCheckingInspection", "UNUSED_PARAMETER")

package com.cuso.mobile.view.home.inventory.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.res.painterResource
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
    icon: Int,
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

    // ── Outer Card matching Image 1 ──
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        color = whiteBg,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // ── Header Row: Icon + Title/Subtitle + Status + Edit/Menu ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // ── Rounded Soft-Lilac Background Box for Hanger Icon ──
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFEFF2FE)), // Exact soft tint from design
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(icon),
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        if (subtitle != null) {
                            Text(
                                text = subtitle,
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                        if (status != null) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFDCFCE7))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = status,
                                    fontSize = 10.sp,
                                    color = Color(0xFF16A34A),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    OutlinedButton(
                        onClick = onEditClick,
                        modifier = Modifier.height(30.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF475569))
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = null,
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "Edit",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF475569)
                        )
                    }
                    IconButton(onClick = onMenuClick, modifier = Modifier.size(28.dp)) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            HorizontalDivider(thickness = 1.dp, color = Color(0xFFF1F5F9))
            Spacer(Modifier.height(12.dp))

            if (showFourGridBoxes) {
                // 4-Box Grid layout (Location Structure Screen)
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        MetricBox(
                            title = "Sequence Order",
                            value = sequenceOrder,
                            modifier = Modifier.weight(1f)
                        )
                        MetricBox(
                            title = "Total Sections",
                            value = totalSections ?: "-",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        MetricBox(
                            title = "Total Racks",
                            value = totalRacks,
                            modifier = Modifier.weight(1f)
                        )
                        MetricBox(
                            title = "Total Bins",
                            value = totalBins,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            } else {
                // Key-Value List layout (Floor, Section, Rack, Bin Screens)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                }

                Spacer(Modifier.height(14.dp))

                // 2 Metric Boxes
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricBox(
                        title = "Total Racks",
                        value = totalRacks,
                        modifier = Modifier.weight(1f)
                    )
                    MetricBox(
                        title = "Total Bins",
                        value = totalBins,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(thickness = 1.dp, color = Color(0xFFF1F5F9))
            Spacer(Modifier.height(12.dp))

            // ── Capacity Summary Section ──
            Text(
                text = "CAPACITY SUMMARY",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF64748B),
                letterSpacing = 0.5.sp
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                capacityMetrics.forEach { (label, value) ->
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Text(
                            text = value,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1E293B)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricBox(title: String, value: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFF8FAFC)) // Soft background for count boxes
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                fontSize = 11.sp,
                color = Color(0xFF64748B),
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )
        }
    }
}

@Composable
private fun MetricRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = Color(0xFF64748B)
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1E293B)
        )
    }
}