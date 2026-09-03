@file:Suppress("unused", "SpellCheckingInspection", "UNUSED_PARAMETER")

package com.cuso.mobile.view.home.inventory.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.*
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.R
import com.cuso.mobile.view.composable.AppUnderlineTabRow
import com.cuso.mobile.view.composable.dashedBorder

@Composable
fun PdfTemplatesScreen(
    onClose: () -> Unit,
    onCreateNewTemplate: () -> Unit
) {
    val tokens = LocalAppTokens.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedTemplateIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Invoice", "Payment Receipt", "Sales Order", "Packing Slip")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        TitleBar(title = "PDF Templates", onClose = onClose)

        // ── Top Tab Row with Primary Underline Indicator ──
        AppUnderlineTabRow(
            tabs = tabs,
            selectedIndex = selectedTab,
            onTabSelected = { selectedTab = it },
            isScrollable = true
        )

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
                    Text(
                        "${tabs[selectedTab]} Templates",
                        fontSize = tokens.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text("4 templates · 1 default", fontSize = tokens.caption, color = close_color)
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "Manage ready-to-use ${tabs[selectedTab].lowercase()} template designs and create custom layouts.",
                    fontSize = tokens.bodySmall,
                    color = TextSecondary
                )
            }

            item {
                OutlinedButton(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth().height(tokens.fieldHeight),
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.45f),
                    border = BorderStroke(1.dp, Primary)
                ) {
                    Icon(
                        Icons.Default.RemoveRedEye,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Preview Default Layout",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Primary
                    )
                }
            }

            // ── Standard Invoice Card ──
            item {
                TemplateCard(
                    name = "Standard Invoice",
                    updatedAgo = "Updated 2 days ago",
                    badge = "DEFAULT",
                    isDefault = true,
                    isSelected = selectedTemplateIndex == 0,
                    thumbnailRes = R.drawable.ic_standard_invoice,
                    onSelect = { selectedTemplateIndex = 0 },
                    onEditLayout = {}
                )
            }

            // ── Modern Invoice Card ──
            item {
                TemplateCard(
                    name = "Modern Invoice",
                    updatedAgo = "Updated 1 week ago",
                    badge = "CUSTOM",
                    isDefault = false,
                    isSelected = selectedTemplateIndex == 1,
                    thumbnailRes = R.drawable.ic_modern_invoice,
                    onSelect = { selectedTemplateIndex = 1 },
                    onEditLayout = {}
                )
            }

            // ── Compact Invoice Card ──
            item {
                TemplateCard(
                    name = "Compact Invoice",
                    updatedAgo = "Updated 1 month ago",
                    badge = "CUSTOM",
                    isDefault = false,
                    isSelected = selectedTemplateIndex == 2,
                    thumbnailRes = R.drawable.ic_compact_invoice,
                    onSelect = { selectedTemplateIndex = 2 },
                    onEditLayout = {}
                )
            }

            // ── Create New Template Dashed Button ──
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.6f))
                        .dashedBorder(
                            color = Primary,
                            strokeWidth = 1.dp,
                            shape = RoundedCornerShape(tokens.cardCornerRadius)
                        )
                        .clickable { onCreateNewTemplate() }
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(30.dp))
                                .background(primary_light),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_document),
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(
                            "Create New Template",
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Primary
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "Start from scratch or a blank slate.",
                            fontSize = tokens.caption,
                            color = close_color
                        )
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
    thumbnailRes: Int,
    isDefault: Boolean,
    isSelected: Boolean = false,
    onSelect: () -> Unit = {},
    onEditLayout: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.6f),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) Primary else sectionBorder
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = thumbnailRes),
                contentDescription = "$name thumbnail",
                modifier = Modifier.size(width = 65.dp, height = 80.dp)
            )

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = name,
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Selected",
                            tint = Primary,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = mutedText,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Text(updatedAgo, fontSize = tokens.caption, color = mutedText)
                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isDefault) greenBg else background_light_purple)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badge,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDefault) greentext else Primary
                        )
                    }

                    Text(
                        text = "Edit Layout →",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Primary,
                        modifier = Modifier.clickable { onEditLayout() }
                    )
                }
            }
        }
    }
}