@file:Suppress(
    "UNUSED_VALUE",
    "SpellCheckingInspection",
    "GrazieInspection",
    "AssignedValueIsNeverRead",
    "unused_variable",
    "unused_parameter",
    "UnusedMaterial3ScaffoldPaddingParameter"
)
package com.cuso.mobile.view.home.services

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.mobile.ui.theme.modelGray
import com.cuso.mobile.ui.theme.whiteBg

// ---------- Design tokens (derived from the reference screens) ----------
private val AccentIndigo = Color(0xFF6C5CE7)
private val PageBackground = Color(0xFFF6F6F8)
private val CardBackground = Color(0xFFFFFFFF)
private val SubtleBorder = Color(0xFFE7E7EC)
private val LabelGray = Color(0xFF9A9AA2)
private val TitleDark = Color(0xFF1C1C28)
private val StatusGreenBg = Color(0xFFE6F6EC)
private val StatusGreenFg = Color(0xFF1E9E52)
private val StatusOrangeBg = Color(0xFFFFF3E0)
private val StatusOrangeFg = Color(0xFFE08900)
private val PriorityRedFg = Color(0xFFE24C4B)

data class OrderDetails(
    val orderId: String,
    val status: String,
    val garmentItem: String,
    val orderDate: String,
    val deliveryDate: String,
    val issueDescription: String,
    val internalNotes: String,
    val attachmentCount: Int
)

data class ServiceDetails(
    val serviceRef: String,
    val reviewStatus: String,
    val service: String,
    val requestDate: String,
    val priority: String,
    val serviceCategory: String,
    val preferredCompletionDate: String,
    val serviceType: String,
    val customerName: String,
    val phoneNumber: String,
    val emailAddress: String,
    val shippingAddress: String
)

/**
 * Single full-page, scrollable screen that merges the "Service Details" request
 * with the "Original Order Details" it references — one continuous page instead
 * of a separate modal/bottom sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceOrderDetailsScreen(
    service: ServiceDetails,
    order: OrderDetails,
    onBack: () -> Unit = {},
    onViewFullOrderHistory: () -> Unit = {}
) {
    Scaffold(
        containerColor =Color.Transparent ,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {

            TopAppBar(
                title = {
                    Text(
                        "Service Details",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp,
                        color = TitleDark
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TitleDark)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = whiteBg)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {

            Spacer(Modifier.height(4.dp))

            // ---- Service ref + status ----
            SectionCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(service.serviceRef, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = TitleDark)
                    StatusChip(text = service.reviewStatus, bg = StatusOrangeBg, fg = StatusOrangeFg)
                }
                Spacer(Modifier.height(14.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    LabeledValue("Service", service.service, Modifier.weight(1f))
                    LabeledValue("Request Date", service.requestDate, Modifier.weight(1f))
                    LabeledValue(
                        "Priority",
                        service.priority,
                        Modifier.weight(1f),
                        valueColor = PriorityRedFg
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ---- Requested Service ----
            SectionHeader(icon = Icons.Default.Build, title = "Requested Service")
            SectionCard {
                LabeledValue("Service Category", service.serviceCategory)
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    thickness = DividerDefaults.Thickness,
                    color = SubtleBorder
                )
                LabeledValue("Preferred Completion Date", service.preferredCompletionDate)
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    thickness = DividerDefaults.Thickness,
                    color = SubtleBorder
                )
                LabeledValue("Priority Level", service.priority, valueColor = PriorityRedFg)
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    thickness = DividerDefaults.Thickness,
                    color = SubtleBorder
                )
                LabeledValue("Service Type", service.serviceType)
            }

            Spacer(Modifier.height(16.dp))

            // ---- Customer Details ----
            SectionHeader(icon = Icons.Default.Person, title = "Customer Details")
            SectionCard {
                LabeledValue("Customer Name", service.customerName)
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    thickness = DividerDefaults.Thickness,
                    color = SubtleBorder
                )
                LabeledValue("Phone Number", service.phoneNumber)
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    thickness = DividerDefaults.Thickness,
                    color = SubtleBorder
                )
                LabeledValue("Email Address", service.emailAddress)
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    thickness = DividerDefaults.Thickness,
                    color = SubtleBorder
                )
                LabeledValue("Shipping Address", service.shippingAddress)
            }

            Spacer(Modifier.height(16.dp))

            // ---- Original Order Details ----
            SectionHeader(icon = Icons.Default.ShoppingBag, title = "Original Order Details")
            SectionCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Order ID", color = LabelGray, fontSize = 13.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(order.orderId, color = AccentIndigo, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                    }
                    StatusChip(text = order.status, bg = StatusGreenBg, fg = StatusGreenFg)
                }
                Spacer(Modifier.height(14.dp))
                LabeledValue("Garment Item", order.garmentItem)
                Spacer(Modifier.height(14.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    LabeledValue("Order Date", order.orderDate, Modifier.weight(1f))
                    LabeledValue("Delivery Date", order.deliveryDate, Modifier.weight(1f))
                }
                Spacer(Modifier.height(16.dp))
                OutlinedButton(
                    onClick = onViewFullOrderHistory,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, AccentIndigo)
                ) {
                    Text("View Full Order History", color = AccentIndigo, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(Modifier.height(16.dp))

            // ---- Issue Description ----
            SectionHeader(icon = Icons.Default.Description, title = "Issue Description")
            SectionCard {
                Text(
                    order.issueDescription,
                    color = TitleDark,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }

            Spacer(Modifier.height(16.dp))

            // ---- Attachments ----
            SectionHeader(icon = Icons.Default.AttachFile, title = "Attachments")

            Row(horizontalArrangement = Arrangement.Center,modifier=Modifier.background(modelGray).fillMaxWidth().padding(vertical=10.dp)) {
                repeat(order.attachmentCount) {
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CardBackground)
                            .border(1.dp, SubtleBorder, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.Image, contentDescription = null, tint = LabelGray)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ---- Charges ----
            SectionHeader(icon = null, title = "Charges")
            SectionCard {
                var serviceCharge by remember { mutableStateOf("0.00") }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Service", color = LabelGray, fontSize = 14.sp)
                    OutlinedTextField(
                        value = serviceCharge,
                        onValueChange = { serviceCharge = it },
                        modifier = Modifier.width(110.dp),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, textAlign = androidx.compose.ui.text.style.TextAlign.End),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent
                        )
                    )
                }
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = { /* add field */ }, contentPadding = PaddingValues(0.dp)) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = AccentIndigo, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add Field", color = AccentIndigo)
                }
            }

            Spacer(Modifier.height(16.dp))

            // ---- Internal Notes ----
            SectionHeader(icon = Icons.AutoMirrored.Filled.List, title = "Internal Notes")
            SectionCard {
                Text(
                    order.internalNotes,
                    color = TitleDark,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ---------- Reusable pieces ----------

@Composable
private fun SectionHeader(icon: androidx.compose.ui.graphics.vector.ImageVector?, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 10.dp, horizontal = 10.dp).background(whiteBg)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = AccentIndigo, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = TitleDark)
    }
}

@Composable
private fun SectionCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(modelGray)
            .border(1.dp, SubtleBorder)
            .padding(16.dp),
        content = content
    )
}

@Composable
private fun LabeledValue(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = TitleDark
) {
    Column(modifier = modifier) {
        Text(label, color = LabelGray, fontSize = 13.sp)
        Spacer(Modifier.height(4.dp))
        Text(value, color = valueColor, fontSize = 15.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun StatusChip(text: String, bg: Color, fg: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text, color = fg, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

// ---------- Preview with sample data matching the reference screens ----------

@Preview(showBackground = true, widthDp = 360, heightDp = 900)
@Composable
private fun ServiceOrderDetailsScreenPreview() {
    MaterialTheme {
        ServiceOrderDetailsScreen(
            service = ServiceDetails(
                serviceRef = "SR-1045",
                reviewStatus = "Pending Review",
                service = "Bespoke Alteration",
                requestDate = "Oct 24, 2025",
                priority = "High",
                serviceCategory = "Suit Fitting & Adjustments",
                preferredCompletionDate = "Nov 15, 2023",
                serviceType = "Internal Production Refit",
                customerName = "Jonathan Sterling",
                phoneNumber = "+1 (555) 123-4567",
                emailAddress = "j.sterling@executive.com",
                shippingAddress = "452 Premium Way, Floor 12\nManhattan, NY 10001"
            ),
            order = OrderDetails(
                orderId = "#ORD-8829-23",
                status = "Completed",
                garmentItem = "Custom Charcoal 3-Piece Wool Suit",
                orderDate = "Sep 12, 2023",
                deliveryDate = "Oct 15, 2023",
                issueDescription = "The sleeves are approximately 2 inches too long and the waist needs to be taken in by 1 inch for a better fit. The customer also requested to check the shoulder alignment as it feels slightly loose on the left side.",
                internalNotes = "Check fabric elasticity before cutting. This particular silk blend is prone to fraying at the seams. Suggest reinforcing the waist darts.",
                attachmentCount = 3
            )
        )
    }
}