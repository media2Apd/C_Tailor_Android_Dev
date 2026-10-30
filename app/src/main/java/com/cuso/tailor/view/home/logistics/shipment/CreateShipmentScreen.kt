@file:Suppress("DEPRECATION", "AssignedValueIsNeverRead", "unused")

package com.cuso.tailor.view.home.logistics.shipment

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.Primary_background
import com.cuso.tailor.ui.theme.disabled
import com.cuso.tailor.ui.theme.headerGrey
import com.cuso.tailor.ui.theme.light_blue
import com.cuso.tailor.ui.theme.light_grey
import com.cuso.tailor.ui.theme.mutedText
import com.cuso.tailor.ui.theme.redText
import com.cuso.tailor.ui.theme.sectionBorder
import com.cuso.tailor.ui.theme.title_color
import com.cuso.tailor.ui.theme.whiteBg
import com.cuso.tailor.view.composable.CirculerProgressIndicatorSmall
import com.cuso.tailor.view.composable.FormDropdown
import com.cuso.tailor.view.composable.FormLabel
import com.cuso.tailor.view.composable.FormTextField
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.viewmodel.InventoryViewModel
import com.cuso.tailor.viewmodel.LogisticsViewModel

@Composable
fun CreateShipmentScreen(
    logisticsViewModel: LogisticsViewModel,
    inventoryViewModel: InventoryViewModel = hiltViewModel(),
    onClose: () -> Unit,
    onSuccessCreated: () -> Unit
) {
    val tokens = LocalAppTokens.current

    // Observe Packages list from InventoryViewModel
    val packagesList by inventoryViewModel.packagesList.collectAsState()

    // Observe Couriers and submission state from LogisticsViewModel
    val couriersList by logisticsViewModel.courierPartnersList.collectAsState()
    val isCreating by logisticsViewModel.isCreatingShipment.collectAsState()
    val createError by logisticsViewModel.createShipmentError.collectAsState()

    // Form states
    var selectedOrderDisplay by remember { mutableStateOf("") }
    var selectedOrderId by remember { mutableStateOf("") }
    var isOrderDropdownExpanded by remember { mutableStateOf(false) }

    var customerName by remember { mutableStateOf("") }
    var deliveryAddress by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var pincode by remember { mutableStateOf("") }

    var selectedCourier by remember { mutableStateOf("") }
    var isCourierDropdownExpanded by remember { mutableStateOf(false) }

    var selectedServiceType by remember { mutableStateOf("") }
    var isServiceDropdownExpanded by remember { mutableStateOf(false) }

    var weightKg by remember { mutableStateOf("1.0") }
    var lengthCm by remember { mutableStateOf("15") }
    var widthCm by remember { mutableStateOf("12") }
    var heightCm by remember { mutableStateOf("10") }

    // Fetch packages and active courier partners on screen launch
    LaunchedEffect(Unit) {
        inventoryViewModel.fetchPackages(page = 1, limit = 50)
        logisticsViewModel.fetchCourierPartners()
    }

    // Build user-friendly dropdown options: "PKG-001 - ORD-001 (Customer Name)"
    val orderOptions = remember(packagesList) {
        packagesList.map { pkg ->
            val pkgCode = pkg.packageNumber.orEmpty().ifBlank { pkg.id }
            val orderCode = pkg.salesOrderId?.orderCode.orEmpty()
            val customer = (pkg.customerId?.fullName ?: pkg.customerId?.name).orEmpty().trim()

            when {
                orderCode.isNotBlank() && customer.isNotBlank() -> "$pkgCode - $orderCode ($customer)"
                orderCode.isNotBlank() -> "$pkgCode - $orderCode"
                customer.isNotBlank() -> "$pkgCode ($customer)"
                else -> pkgCode
            }
        }
    }

    // Courier partner names
    val courierOptions = remember(couriersList) {
        couriersList.map { it.courierName }.distinct()
    }

    // Service types dynamically bound to selected courier
    val serviceOptions = remember(selectedCourier, couriersList) {
        val courier = couriersList.find { it.courierName.equals(selectedCourier, ignoreCase = true) }
        courier?.serviceTypes?.takeIf { it.isNotEmpty() } ?: listOf("Surface", "Standard", "Air")
    }

    Scaffold(
        containerColor = Primary_background,
        topBar = {
            TitleBar(
                title = "Create Shipment",
                onClose = onClose
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = whiteBg,
                shadowElevation = 8.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, sectionBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = tokens.screenPadding, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Cancel Button
                    Button(
                        onClick = onClose,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = light_grey,
                            contentColor = title_color
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(tokens.buttonHeight)
                    ) {
                        Text(
                            text = "Cancel & Discard",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = title_color
                        )
                    }

                    // Validation rule
                    val isFormValid = selectedOrderId.isNotBlank() &&
                            customerName.isNotBlank() &&
                            deliveryAddress.isNotBlank() &&
                            selectedCourier.isNotBlank() &&
                            weightKg.isNotBlank()

                    // Submit Button
                    // Create Shipment Primary Button
                    Button(
                        onClick = {
                            logisticsViewModel.submitCreateShipment(
                                orderId = selectedOrderId,
                                recipientName = customerName,
                                phoneNumber = phoneNumber,
                                fullAddress = deliveryAddress,
                                pincode = pincode,
                                courierPartner = selectedCourier,
                                serviceType = selectedServiceType.ifBlank { "Standard" },
                                weightKg = weightKg.toDoubleOrNull() ?: 1.0,
                                lengthCm = lengthCm.toDoubleOrNull() ?: 15.0,
                                widthCm = widthCm.toDoubleOrNull() ?: 12.0,
                                heightCm = heightCm.toDoubleOrNull() ?: 10.0,
                                onSuccess = {
                                    onSuccessCreated()
                                }
                            )
                        },
                        enabled = isFormValid && !isCreating,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary,
                            disabledContainerColor = disabled
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1.2f)
                            .height(tokens.buttonHeight)
                    ) {
                        if (isCreating) {
                            CirculerProgressIndicatorSmall()
                        } else {
                            Text(
                                text = "Create Shipment",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = whiteBg
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(
                horizontal = tokens.screenPadding,
                vertical = 14.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Server error banner
            if (!createError.isNullOrBlank()) {
                item {
                    Text(
                        text = createError ?: "",
                        color = redText,
                        fontSize = tokens.bodySmall,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }

            // ─────────────────────────────────────────────────────────────────
            // SECTION 1: ORDER INFORMATION
            // ─────────────────────────────────────────────────────────────────
            item {
                FormCardSection(
                    title = "Order Information",
                    subtitle = "Customer and delivery reference",
                    icon = Icons.Default.Description
                ) {
                    // Order ID Dropdown (populated from InventoryViewModel packages)
                    FormLabel(text = "Order ID", isRequired = true)
                    FormDropdown(
                        value = selectedOrderDisplay.ifBlank { "Select order" },
                        expanded = isOrderDropdownExpanded,
                        onExpandChange = { isOrderDropdownExpanded = it },
                        options = orderOptions.ifEmpty { listOf("No packages available") },
                        onOptionSelected = { chosenOption ->
                            selectedOrderDisplay = chosenOption

                            // Match the chosen option back to the PackageItemDto object
                            val chosenPackage = packagesList.find { pkg ->
                                val pkgCode = pkg.packageNumber.orEmpty().ifBlank { pkg.id }
                                chosenOption.startsWith(pkgCode)
                            }

                            if (chosenPackage != null) {
                                // Assign orderId from package reference
                                selectedOrderId = chosenPackage.salesOrderId?.id?.takeIf { it.isNotBlank() }
                                    ?: chosenPackage.salesOrderId?.orderCode?.takeIf { it.isNotBlank() }
                                    ?: chosenPackage.id

                                // Autofill customer information from package
                                customerName = (chosenPackage.customerId?.fullName
                                    ?: chosenPackage.customerId?.name).orEmpty()
                                phoneNumber = chosenPackage.customerId?.mobileNumber.orEmpty()
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Customer Name
                    FormLabel(text = "Customer Name", isRequired = true)
                    FormTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        placeholder = "Enter customer name"
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Delivery Address
                    FormLabel(text = "Delivery Address", isRequired = true)
                    FormTextField(
                        value = deliveryAddress,
                        onValueChange = { deliveryAddress = it },
                        placeholder = "Street, Building, City, State"
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Phone Number & Pincode Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel(text = "Phone Number", isRequired = false)
                            FormTextField(
                                value = phoneNumber,
                                onValueChange = { phoneNumber = it },
                                placeholder = "+91 9876543210",
                                keyboardType = KeyboardType.Phone
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel(text = "Pincode", isRequired = false)
                            FormTextField(
                                value = pincode,
                                onValueChange = { pincode = it },
                                placeholder = "e.g. 600040",
                                keyboardType = KeyboardType.Number
                            )
                        }
                    }
                }
            }

            // ─────────────────────────────────────────────────────────────────
            // SECTION 2: COURIER SELECTION
            // ─────────────────────────────────────────────────────────────────
            item {
                FormCardSection(
                    title = "Courier Selection",
                    subtitle = "Choose delivery service partner",
                    icon = Icons.Default.LocalShipping
                ) {
                    // Courier Partner Dropdown
                    FormLabel(text = "Courier Partner", isRequired = true)
                    FormDropdown(
                        value = selectedCourier.ifBlank { "Select Courier" },
                        expanded = isCourierDropdownExpanded,
                        onExpandChange = { isCourierDropdownExpanded = it },
                        options = courierOptions.ifEmpty { listOf("Loading couriers...") },
                        onOptionSelected = { courier ->
                            selectedCourier = courier
                            // Default to the first available service type of this courier
                            val courierObj = couriersList.find { it.courierName.equals(courier, ignoreCase = true) }
                            selectedServiceType = courierObj?.serviceTypes?.firstOrNull() ?: "Surface"
                        }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Service Type Dropdown
                    FormLabel(text = "Service Type", isRequired = false)
                    FormDropdown(
                        value = selectedServiceType.ifBlank { "Select Service" },
                        expanded = isServiceDropdownExpanded,
                        onExpandChange = { isServiceDropdownExpanded = it },
                        options = serviceOptions,
                        onOptionSelected = { selectedServiceType = it },
                        enabled = selectedCourier.isNotBlank()
                    )
                }
            }

            // ─────────────────────────────────────────────────────────────────
            // SECTION 3: PACKAGE DETAILS
            // ─────────────────────────────────────────────────────────────────
            item {
                FormCardSection(
                    title = "Package Details",
                    subtitle = "Weight and dimension specs",
                    icon = Icons.Default.Inventory2
                ) {
                    // Weight
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Weight (kg)",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = title_color
                        )
                        Text(
                            text = "Required",
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Normal,
                            color = mutedText
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))

                    FormTextField(
                        value = weightKg,
                        onValueChange = { weightKg = it },
                        placeholder = "1.0",
                        trailingText = "kg",
                        keyboardType = KeyboardType.Decimal
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Dimensions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Dimensions (cm)",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = title_color
                        )
                        Text(
                            text = "Optional",
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Normal,
                            color = mutedText
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))

                    // Length, Width, Height side-by-side
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Length
                        Column(modifier = Modifier.weight(1f)) {
                            FormTextField(
                                value = lengthCm,
                                onValueChange = { lengthCm = it },
                                placeholder = "Length",
                                keyboardType = KeyboardType.Decimal
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "L (cm)",
                                fontSize = tokens.label,
                                fontWeight = FontWeight.Normal,
                                color = mutedText,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }

                        // Width
                        Column(modifier = Modifier.weight(1f)) {
                            FormTextField(
                                value = widthCm,
                                onValueChange = { widthCm = it },
                                placeholder = "Width",
                                keyboardType = KeyboardType.Decimal
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "W (cm)",
                                fontSize = tokens.label,
                                fontWeight = FontWeight.Normal,
                                color = mutedText,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }

                        // Height
                        Column(modifier = Modifier.weight(1f)) {
                            FormTextField(
                                value = heightCm,
                                onValueChange = { heightCm = it },
                                placeholder = "Height",
                                keyboardType = KeyboardType.Decimal
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "H (cm)",
                                fontSize = tokens.label,
                                fontWeight = FontWeight.Normal,
                                color = mutedText,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

/**
 * Reusable Card container for sections matching the UI design tokens.
 */
@Composable
private fun FormCardSection(
    title: String,
    subtitle: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    val tokens = LocalAppTokens.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        color = whiteBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, sectionBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(tokens.screenPadding)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(light_blue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(tokens.iconSize)
                    )
                }

                Column {
                    Text(
                        text = title,
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = title_color
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Normal,
                        color = headerGrey
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            content()
        }
    }
}
