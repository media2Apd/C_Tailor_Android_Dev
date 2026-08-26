@file:Suppress(
    "UNUSED_PARAMETER",
    "unused",
    "UNCHECKED_CAST",
    "DEPRECATION",
    "AssignedValueIsNeverRead",
    "GrazieInspection",
    "SpellCheckingInspection",
    "unusedvariable",
    "UNNECESSARY_SAFE_CALL"
)

package com.cuso.mobile.view.home.services.service_status.service_delivery

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.mobile.adaptive_screen.AppDesignTokens
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.*
import com.cuso.mobile.R

private val ScreenBg = Color(0xFFFBFBFC)
private val UrgentBannerBg = Color(0xFFFDE8E8)
private val UrgentRed = Color(0xFFE53935)
private val DeliveryActiveBlue = Color(0xFF3324D7)
private val DeliveryActiveBg = Color(0xFFF6F5FE)
private val PillBlueBg = Color(0xFFE0E7FF)
private val PillBlueText = Color(0xFF2563EB)
private val PillRedBg = Color(0xFFFFEBEE)
private val PillRedText = Color(0xFFDC2626)
private val MapRoadColor = Color(0xFFE2E8F0)
private val MapLandColor = Color(0xFFF1F5F9)
private val MapBuildingColor = Color(0xFFE2E8F0).copy(alpha = 0.6f)
private val MapPinBlue = Color(0xFF0288D1)

@Composable
fun ServiceDeliveryStatusScreen(
    onClose: () -> Unit = {},
    onContactCustomer: () -> Unit = {},
    onEditDetails: () -> Unit = {},
    onReschedule: () -> Unit = {},
    onMarkDelivered: () -> Unit = {},
    onViewUrgentOrder: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
    ) {
        // Top Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(whiteBg)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Service Status",
                fontSize = tokens.h2,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            IconButton(
                onClick = onClose,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = TextPrimary
                )
            }
        }
        HorizontalDivider(color = BorderGray, thickness = 1.dp)

        // Main Scrollable Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // High Urgency Alert Banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = UrgentBannerBg
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.WarningAmber,
                            contentDescription = null,
                            tint = UrgentRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "HIGH URGENCY",
                                fontSize = tokens.caption,
                                fontWeight = FontWeight.Bold,
                                color = UrgentRed
                            )
                            Text(
                                text = "Order #ORD-1102 is 2 hours overdue.",
                                fontSize = tokens.caption,
                                color = TextPrimary
                            )
                        }
                    }

                    Button(
                        onClick = onViewUrgentOrder,
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = UrgentRed),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text(
                            text = "VIEW",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = whiteBg
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Order Overview Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = whiteBg,
                    border = BorderStroke(1.dp, BorderGray)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Order #ORD-2024-1247",
                                fontSize = tokens.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = PillBlueBg
                            ) {
                                Text(
                                    text = "Home Delivery",
                                    fontSize = tokens.caption,
                                    fontWeight = FontWeight.Medium,
                                    color = PillBlueText,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))

                        HorizontalDivider(color = BorderGray)


                        Spacer(Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Customer",
                                    fontSize = tokens.label,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "Rajesh Kumar",
                                    fontSize = tokens.bodyMedium,
                                    color = TextPrimary
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = PillRedBg
                            ) {
                                Text(
                                    text = "Deadline: Feb 28, 2026",
                                    fontSize = tokens.caption,
                                    fontWeight = FontWeight.Medium,
                                    color = PillRedText,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Action Buttons Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = onContactCustomer,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DeliveryActiveBlue)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = whiteBg
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "Contact",
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = whiteBg,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }

                            OutlinedButton(
                                onClick = onEditDetails,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                border = BorderStroke(1.dp, BorderGray),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = TextPrimary
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "Edit Details",
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }

                            OutlinedButton(
                                onClick = { },
                                modifier = Modifier.size(42.dp),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, BorderGray),
                                contentPadding = PaddingValues(0.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreHoriz,
                                    contentDescription = "More",
                                    modifier = Modifier.size(20.dp),
                                    tint = TextPrimary
                                )
                            }
                        }
                    }
                }

                // Delivery Options Section
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Delivery Options",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    // Option 1: In-store Pickup
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = whiteBg,
                        border = BorderStroke(1.dp, BorderGray)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "In-store Pickup",
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                            Text(
                                text = "Scheduled for Mar 1",
                                fontSize = tokens.caption,
                                color = mutedText
                            )
                        }
                    }

                    // Option 2: Home Delivery (Active)
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = whiteBg,
                        border = BorderStroke(1.5.dp, DeliveryActiveBlue)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.LocalShipping,
                                        contentDescription = null,
                                        tint = DeliveryActiveBlue,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "Home Delivery",
                                        fontSize = tokens.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = DeliveryActiveBlue
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = DeliveryActiveBlue
                                ) {
                                    Text(
                                        text = "ACTIVE",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = whiteBg,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 0.dp)
                                    )
                                }
                            }

                            Spacer(Modifier.height(8.dp))

                            Text(
                                text = "Out for Delivery",
                                fontSize = tokens.bodySmall,
                                color = TextSecondary
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "Assigned: Rohan (Driver)",
                                fontSize = tokens.caption,
                                color = TextSecondary
                            )
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_location),
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "123 Maple St, Apt 4B, New York, NY",
                                    fontSize = tokens.caption,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    // Option 3: Bulk Delivery
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = whiteBg,
                        border = BorderStroke(1.dp, BorderGray)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Bulk Delivery",
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                            Text(
                                text = "Not Applicable",
                                fontSize = tokens.caption,
                                color = mutedText
                            )
                        }
                    }
                }

                // Metric Cards (Today's, Delayed, Completed)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCounterCard(
                        title = "Today's",
                        titleColor = TextSecondary,
                        count = "12",
                        countColor = TextPrimary,
                        modifier = Modifier.weight(1f),
                        tokens = tokens
                    )
                    MetricCounterCard(
                        title = "Delayed",
                        titleColor = redText,
                        count = "2",
                        countColor = redText,
                        modifier = Modifier.weight(1f),
                        tokens = tokens
                    )
                    MetricCounterCard(
                        title = "Completed",
                        titleColor = greentext,
                        count = "45",
                        countColor = greentext,
                        modifier = Modifier.weight(1f),
                        tokens = tokens
                    )
                }

                // Real-time Location Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = whiteBg,
                    border = BorderStroke(1.dp, BorderGray)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Real-time Location",
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Spacer(Modifier.height(12.dp))

                        // Vector Simulated Map View
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MapLandColor)
                        ) {
                            MapSimulationCanvas(modifier = Modifier.fillMaxSize())

                            // Driver status indicator bar
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                shape = RoundedCornerShape(24.dp),
                                color = whiteBg.copy(alpha = 0.95f),
                                shadowElevation = 2.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(PillBlueText)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "Driver Rohan — 1.5 miles away",
                                        fontSize = tokens.caption,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                // Delivery Timeline Section
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Delivery Timeline",
                        fontSize = tokens.bodyMedium,
                        color = TextPrimary
                    )

                    // Timeline Step 1: Scheduled (Completed)
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = whiteBg,
                        border = BorderStroke(1.dp, BorderGray)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.CheckCircle,
                                        contentDescription = null,
                                        tint = greentext,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "Scheduled",
                                        fontSize = tokens.bodyMedium,
                                        color = TextPrimary
                                    )
                                }

                                Text(
                                    text = "Completed",
                                    fontSize = tokens.caption,
                                    fontWeight = FontWeight.Medium,
                                    color = greentext
                                )
                            }

                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Confirmed by Admin Priya on Feb 25, 10:00 AM",
                                fontSize = tokens.caption,
                                color = TextSecondary,
                                modifier = Modifier.padding(start = 28.dp)
                            )
                        }
                    }

                    // Timeline Step 2: Out for Delivery (Active)
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = whiteBg,
                        border = BorderStroke(1.5.dp, DeliveryActiveBlue)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.NearMe,
                                        contentDescription = null,
                                        tint = DeliveryActiveBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "Out for Delivery",
                                        fontSize = tokens.bodyMedium,
                                        color = DeliveryActiveBlue
                                    )
                                }

                                Text(
                                    text = "Active",
                                    fontSize = tokens.caption,
                                    fontWeight = FontWeight.Medium,
                                    color = DeliveryActiveBlue
                                )
                            }

                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Dispatched on Feb 28, 09:15 AM by Rohan",
                                fontSize = tokens.caption,
                                color = TextSecondary,
                                modifier = Modifier.padding(start = 28.dp)
                            )

                            Spacer(Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = onReschedule,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, BorderGray),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                                ) {
                                    Text("Reschedule", fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold)
                                }

                                Button(
                                    onClick = onMarkDelivered,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = DeliveryActiveBlue)
                                ) {
                                    Text("Mark as Delivered", fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = whiteBg)
                                }
                            }
                        }
                    }

                    // Timeline Step 3: Delivered (Awaiting)
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = whiteBg,
                        border = BorderStroke(1.dp, BorderGray)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.AccessTime,
                                        contentDescription = null,
                                        tint = mutedText,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "Delivered",
                                        fontSize = tokens.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = mutedText
                                    )
                                }

                                Text(
                                    text = "Awaiting",
                                    fontSize = tokens.caption,
                                    fontWeight = FontWeight.Medium,
                                    color = mutedText
                                )
                            }

                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Awaiting delivery completion",
                                fontSize = tokens.caption,
                                color = mutedText,
                                modifier = Modifier.padding(start = 28.dp)
                            )

                            Spacer(Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                DashedActionBox(
                                    icon = Icons.Outlined.Draw,
                                    label = "Touch to sign",
                                    modifier = Modifier.weight(1f),
                                    tokens = tokens
                                )
                                DashedActionBox(
                                    icon = Icons.Outlined.PhotoCamera,
                                    label = "Upload Photo",
                                    modifier = Modifier.weight(1f),
                                    tokens = tokens
                                )
                            }
                        }
                    }
                }

                // Activity & Notes Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = whiteBg,
                    border = BorderStroke(1.dp, BorderGray)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Description,
                                contentDescription = null,
                                tint = TextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Activity & Notes",
                                fontSize = tokens.bodyMedium,
                                color = TextPrimary
                            )
                        }

                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider(color = BorderGray)
                        Spacer(Modifier.height(12.dp))

                        ActivityNoteRow(
                            title = "Driver arrived at location",
                            timestamp = "Feb 28, 09:42 AM",
                            note = "\"GPS agent confirmed driver at delivery coordinates\"",
                            tokens = tokens
                        )

                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider(color = BorderGray)
                        Spacer(Modifier.height(12.dp))

                        ActivityNoteRow(
                            title = "Order Dispatched",
                            timestamp = "Feb 28, 09:15 AM",
                            note = "Package processed out of facility",
                            tokens = tokens
                        )

                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider(color = BorderGray)
                        Spacer(Modifier.height(12.dp))

                        ActivityNoteRow(
                            title = "Customer requested contactless",
                            timestamp = "Feb 27, 10:20 PM",
                            note = "Note by Admin Priya: \"leave at front door behind the planter\"",
                            tokens = tokens
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun MetricCounterCard(
    title: String,
    titleColor: Color,
    count: String,
    countColor: Color,
    modifier: Modifier = Modifier,
    tokens: AppDesignTokens
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = whiteBg,
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                fontSize = tokens.caption,
                color = titleColor
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = count,
                fontSize = 24.sp,
                color = countColor
            )
        }
    }
}

@Composable
private fun DashedActionBox(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    tokens: AppDesignTokens
) {
    Box(
        modifier = modifier
            .height(54.dp)
            .border(
                BorderStroke(1.dp, BorderGray),
                RoundedCornerShape(8.dp)
            )
            .clickable { },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = mutedText,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = tokens.caption,
                color = mutedText,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ActivityNoteRow(
    title: String,
    timestamp: String,
    note: String,
    tokens: AppDesignTokens
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = tokens.bodySmall,
                color = title_color
            )
            Text(
                text = timestamp,
                fontSize = tokens.caption,
                color = TextSecondary
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = note,
            fontSize = tokens.caption,
            color = TextSecondary,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun MapSimulationCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // Background grid landscape
        drawRect(color = Color(0xFFEFF6F0))

        // Draw blocks
        drawRoundRect(
            color = Color(0xFFE2E8F0),
            topLeft = Offset(width * 0.08f, height * 0.12f),
            size = Size(width * 0.22f, height * 0.32f),
            cornerRadius = CornerRadius(6.dp.toPx())
        )
        drawRoundRect(
            color = Color(0xFFE2E8F0),
            topLeft = Offset(width * 0.36f, height * 0.12f),
            size = Size(width * 0.26f, height * 0.32f),
            cornerRadius = CornerRadius(6.dp.toPx())
        )
        drawRoundRect(
            color = Color(0xFFE2E8F0),
            topLeft = Offset(width * 0.68f, height * 0.12f),
            size = Size(width * 0.24f, height * 0.32f),
            cornerRadius = CornerRadius(6.dp.toPx())
        )

        // Draw horizontal road
        drawLine(
            color = Color(0xFFCBD5E1),
            start = Offset(0f, height * 0.52f),
            end = Offset(width, height * 0.52f),
            strokeWidth = 24.dp.toPx()
        )
        // Road centerline
        drawLine(
            color = Color.White,
            start = Offset(0f, height * 0.52f),
            end = Offset(width, height * 0.52f),
            strokeWidth = 2.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
        )

        // Draw vertical road
        drawLine(
            color = Color(0xFFCBD5E1),
            start = Offset(width * 0.5f, 0f),
            end = Offset(width * 0.5f, height),
            strokeWidth = 20.dp.toPx()
        )

        // Draw Map Pin
        val pinCenter = Offset(width * 0.5f, height * 0.44f)
        drawCircle(
            color = MapPinBlue,
            radius = 12.dp.toPx(),
            center = pinCenter
        )
        drawCircle(
            color = Color.White,
            radius = 5.dp.toPx(),
            center = pinCenter
        )
    }
}