package com.cuso.mobile.view.home.inventory.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.*
import com.cuso.mobile.view.composable.TitleBar

@Composable
fun PdfTemplatesScreen(
    onClose: () -> Unit,
    onCreateNewTemplate: () -> Unit
) {
    val tokens = LocalAppTokens.current
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Invoice", "Payment Receipt", "Sales Order", "Packing Slip")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        TitleBar(title = "PDF Templates", onClose = onClose)

        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            edgePadding = tokens.screenPadding,
            containerColor = whiteBg,
            contentColor = Primary,
            divider = { HorizontalDivider(color = Color(0xFFE5E7EB)) }
        ) {
            tabs.forEachIndexed { index, tabName ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = tabName,
                            fontSize = tokens.bodySmall,
                            fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (selectedTab == index) Primary else Color(0xFF6B7280)
                        )
                    }
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(tokens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Invoice Templates", fontSize = tokens.bodyLarge, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                    Text("4 templates · 1 default", fontSize = tokens.caption, color = Color(0xFF9CA3AF))
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "Manage ready-to-use invoice template designs and create custom layouts.",
                    fontSize = tokens.bodySmall,
                    color = Color(0xFF6B7280)
                )
            }

            item {
                OutlinedButton(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth().height(tokens.fieldHeight),
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.45f),
                    border = BorderStroke(1.dp, Primary)
                ) {
                    Icon(Icons.Default.RemoveRedEye, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Preview Default Layout", fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = Primary)
                }
            }

            item {
                TemplateCard(name = "Standard Invoice", updatedAgo = "Updated 2 days ago", badge = "DEFAULT", isDefault = true, isSelected = true)
            }

            item {
                TemplateCard(name = "Modern Invoice", updatedAgo = "Updated 1 week ago", badge = "CUSTOM", isDefault = false, accentHeader = true)
            }

            item {
                TemplateCard(name = "Compact Invoice", updatedAgo = "Updated 1 month ago", badge = "CUSTOM", isDefault = false)
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.6f))
                        .border(1.5.dp, Primary.copy(alpha = 0.5f), RoundedCornerShape(tokens.cardCornerRadius * 0.6f))
                        .clickable { onCreateNewTemplate() }
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFEEF2FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.NoteAdd, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.height(10.dp))
                        Text("Create New Template", fontSize = tokens.bodyMedium, fontWeight = FontWeight.SemiBold, color = Primary)
                        Spacer(Modifier.height(2.dp))
                        Text("Start from scratch or a blank slate.", fontSize = tokens.caption, color = Color(0xFF9CA3AF))
                    }
                }
            }
        }
    }
}

@Composable
fun TemplateCard(
    name: String,
    updatedAgo: String,
    badge: String,
    isDefault: Boolean,
    isSelected: Boolean = false,
    accentHeader: Boolean = false
) {
    val tokens = LocalAppTokens.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.6f),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        border = BorderStroke(1.dp, if (isSelected) Primary else Color(0xFFE2E8F0))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(width = 65.dp, height = 80.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(6.dp))
                    .background(Color(0xFFF8FAFC))
                    .padding(6.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (accentHeader) Primary else Color(0xFF94A3B8))
                    )
                    Box(modifier = Modifier.fillMaxWidth(0.7f).height(3.dp).background(Color(0xFFCBD5E1)))
                    Box(modifier = Modifier.fillMaxWidth(0.9f).height(3.dp).background(Color(0xFFE2E8F0)))
                    Box(modifier = Modifier.fillMaxWidth(0.8f).height(3.dp).background(Color(0xFFE2E8F0)))
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(name, fontSize = tokens.bodyMedium, fontWeight = FontWeight.SemiBold, color = Color(0xFF111827))
                    if (isSelected) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
                    } else {
                        Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(18.dp))
                    }
                }

                Text(updatedAgo, fontSize = tokens.caption, color = Color(0xFF9CA3AF))
                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isDefault) Color(0xFFDCFCE7) else Color(0xFFF3E8FF))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badge,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDefault) Color(0xFF16A34A) else Color(0xFF9333EA)
                        )
                    }

                    Text(
                        text = "Edit Layout →",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Primary,
                        modifier = Modifier.clickable { }
                    )
                }
            }
        }
    }
}