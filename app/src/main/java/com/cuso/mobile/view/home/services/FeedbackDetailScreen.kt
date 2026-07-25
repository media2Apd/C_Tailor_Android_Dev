@file:Suppress(
    "UNUSED_PARAMETER",
    "unused",
    "UNCHECKED_CAST",
    "DEPRECATION",
    "AssignedValueIsNeverRead",
    "GrazieInspection",
    "SpellCheckingInspection",
    "unusedvariable",
    "SameParameterValue"
)
package com.cuso.mobile.view.home.services


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Design tokens ──
private val AccentColor = Color(0xFF4F39F6)
private val TitleColor = Color(0xFF111827)
private val LabelColor = Color(0xFF6B7280)
private val MutedColor = Color(0xFF9CA3AF)
private val BorderColor = Color(0xFFE3E4E8)
private val SectionBg = Color(0xFFF7F7FA)
private val StarColor = Color(0xFFF59E0B)
private val LinkColor = Color(0xFF4F39F6)
private val SuccessColor = Color(0xFF16A34A)
private val CommentBg = Color(0xFFF7F7FA)

// ── Static data model (matches screenshot fields) ──
private data class FeedbackTimelineEntryStatic(
    val title: String,
    val description: String,
    val timestamp: String,
    val isCompleted: Boolean
)

@Composable
fun FeedbackDetailScreen(
    onDismiss: () -> Unit = {},
    onViewFullHistory: () -> Unit = {}
) {
    // ── Static sample data (matches image 1) ──
    val feedbackId = "FB-1024"
    val orderId = "ORD-1045"
    val customer = "Meena"
    val garment = "Silk Saree Blouse"
    val feedbackDate = "15 March 2026"

    val fullName = "Anitha R"
    val contactNumber = "+91 98765 43210"
    val orderReference = "#ORD-2589"
    val garmentType = "Silk Saree Blouse"
    val actualDeliveryDate = "10 Mar 2026"
    val status = "Delivered"

    val categories = listOf("Product Quality", "Delivery Speed", "Fitting Accuracy", "Staff Behavior", "Fabric Quality")
    val selectedCategory = "Product Quality"

    val overallSatisfaction = 4.0
    val customerComments = "The stitching quality was excellent and the saree blouse fit perfectly. Delivery was also on time. Very satisfied with the service."
    val helpfulCount = 2

    val timeline = listOf(
        FeedbackTimelineEntryStatic(
            title = "Feedback recorded in system",
            description = "Automated processing completed.",
            timestamp = "15 Mar 2026, 02:45 PM",
            isCompleted = true
        ),
        FeedbackTimelineEntryStatic(
            title = "Feedback submitted",
            description = "Submitted via Customer App link.",
            timestamp = "15 Mar 2026, 02:30 PM",
            isCompleted = false
        )
    )

    var categoryExpanded by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // ── Header ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Feedback Details", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TitleColor)
            Icon(
                Icons.Filled.Close,
                contentDescription = "Close",
                tint = LabelColor,
                modifier = Modifier.clickable(onClick = onDismiss)
            )
        }
        HorizontalDivider(color = BorderColor)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // ── Summary strip: feedbackId / orderId, customer/garment/date ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SectionBg, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Text(
                    "$feedbackId / $orderId",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TitleColor
                )
                Spacer(Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    SummaryColumn(label = "Customer", value = customer, modifier = Modifier.weight(1f))
                    SummaryColumn(label = "Garment", value = garment, modifier = Modifier.weight(1f))
                    SummaryColumn(label = "Feedback Date", value = feedbackDate, modifier = Modifier.weight(1f))
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Customer Information ──
            SectionTitle("Customer Information")
            Spacer(Modifier.height(12.dp))
            InfoRow(label = "Full Name", value = fullName)
            InfoRow(label = "Contact Number", value = contactNumber)
            InfoRow(label = "Order Reference", value = orderReference, valueColor = LinkColor)
            InfoRow(label = "Garment Type", value = garmentType)
            InfoRow(label = "Actual Delivery Date", value = actualDeliveryDate)
            InfoRow(label = "Status", value = status, valueColor = SuccessColor, valueWeight = FontWeight.SemiBold)

            Spacer(Modifier.height(20.dp))
            HorizontalDivider(color = BorderColor)
            Spacer(Modifier.height(16.dp))

            // ── Feedback Category (collapsible) ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { categoryExpanded = !categoryExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Feedback Category", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TitleColor)
                Icon(
                    if (categoryExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = LabelColor
                )
            }

            AnimatedVisibility(visible = categoryExpanded) {
                Column {
                    Spacer(Modifier.height(12.dp))
                    CategoryChipGrid(categories = categories, selected = selectedCategory)
                }
            }

            Spacer(Modifier.height(20.dp))
            HorizontalDivider(color = BorderColor)
            Spacer(Modifier.height(16.dp))

            // ── Feedback Details (satisfaction + comments) ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Feedback Details", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TitleColor)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { /* edit note action */ }
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = null, tint = AccentColor, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Internal Note", fontSize = 13.sp, color = AccentColor, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(Modifier.height(14.dp))
            Text("Overall Satisfaction", fontSize = 13.sp, color = MutedColor)
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                StarRating(rating = overallSatisfaction)
                Spacer(Modifier.width(8.dp))
                Text(
                    "${overallSatisfaction} / 5.0",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TitleColor
                )
            }

            Spacer(Modifier.height(14.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CommentBg, RoundedCornerShape(10.dp))
                    .padding(14.dp)
            ) {
                Text("Customer Comments", fontSize = 12.sp, color = MutedColor)
                Spacer(Modifier.height(6.dp))
                Text(
                    "\"$customerComments\"",
                    fontSize = 14.sp,
                    color = TitleColor,
                    lineHeight = 20.sp
                )
            }

            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { /* helpful toggle */ }
                ) {
                    Icon(Icons.Filled.ThumbUp, contentDescription = null, tint = LabelColor, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Helpful ($helpfulCount)", fontSize = 13.sp, color = LabelColor)
                }
                Spacer(Modifier.width(20.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { /* share internal */ }
                ) {
                    Icon(Icons.Filled.Share, contentDescription = null, tint = LabelColor, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Share Internal", fontSize = 13.sp, color = LabelColor)
                }
            }

            Spacer(Modifier.height(20.dp))
            HorizontalDivider(color = BorderColor)
            Spacer(Modifier.height(16.dp))

            // ── Feedback Timeline ──
            Text("Feedback Timeline", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TitleColor)
            Spacer(Modifier.height(16.dp))
            timeline.forEachIndexed { index, entry ->
                TimelineRow(
                    entry = entry,
                    isLast = index == timeline.lastIndex
                )
            }

            Spacer(Modifier.height(20.dp))

            // ── View Full History button ──
            OutlinedButton(
                onClick = onViewFullHistory,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, AccentColor),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentColor),
                modifier = Modifier.fillMaxWidth().height(46.dp)
            ) {
                Text("View Full History", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── Reusable pieces ──

@Composable
private fun SummaryColumn(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label, fontSize = 11.sp, color = MutedColor)
        Spacer(Modifier.height(2.dp))
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TitleColor)
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TitleColor)
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    valueColor: Color = TitleColor,
    valueWeight: FontWeight = FontWeight.Medium
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 13.sp, color = MutedColor)
        Text(value, fontSize = 13.sp, color = valueColor, fontWeight = valueWeight)
    }
}

@Composable
private fun CategoryChipGrid(categories: List<String>, selected: String) {
    // Simple 2-column wrap using rows of 2
    categories.chunked(2).forEach { rowItems ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            rowItems.forEach { category ->
                val isSelected = category == selected
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) AccentColor else BorderColor,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .background(
                            if (isSelected) AccentColor.copy(alpha = 0.06f) else Color.White,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        category,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) AccentColor else LabelColor
                    )
                }
            }
            if (rowItems.size == 1) Spacer(Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))
    }
}

@Composable
private fun StarRating(rating: Double, maxStars: Int = 5) {
    Row {
        repeat(maxStars) { index ->
            val filled = index < rating.toInt()
            Icon(
                imageVector = if (filled) Icons.Filled.Star else Icons.Filled.StarBorder,
                contentDescription = null,
                tint = StarColor,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun TimelineRow(entry: FeedbackTimelineEntryStatic, isLast: Boolean) {
    Row(modifier = Modifier.fillMaxWidth()) {
        // ── Dot + connecting line ──
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(if (entry.isCompleted) AccentColor else Color(0xFFD1D5DB))
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(50.dp)
                        .background(BorderColor)
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.padding(bottom = 20.dp)) {
            Text(entry.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TitleColor)
            Spacer(Modifier.height(2.dp))
            Text(entry.description, fontSize = 12.sp, color = MutedColor)
            Spacer(Modifier.height(2.dp))
            Text(entry.timestamp, fontSize = 11.sp, color = MutedColor)
        }
    }
}