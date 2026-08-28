@file:Suppress(
    "UNUSED_VALUE",
    "SpellCheckingInspection",
    "GrazieInspection",
    "AssignedValueIsNeverRead",
    "unused_variable",
    "unused_parameter",
    "UnusedMaterial3ScaffoldPaddingParameter", "VariableNeverRead"
)

package com.cuso.mobile.view.home.services.service_request

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cuso.mobile.ui.theme.Primary
import com.cuso.mobile.ui.theme.TextSecondary
import com.cuso.mobile.ui.theme.blackTitle
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.AccordionSection
import com.cuso.mobile.view.composable.DashedUploadBox
import com.cuso.mobile.view.composable.DatePickerField
import com.cuso.mobile.view.composable.SelectableChipRow
import com.cuso.mobile.view.composable.rememberFilePickerLauncher
import com.cuso.mobile.view.composable.FormDropdown
import com.cuso.mobile.view.composable.FormLabel
import com.cuso.mobile.view.composable.FormTextField
import com.cuso.mobile.view.composable.StepNavigationFab
import com.cuso.mobile.view.composable.TrailingFabAction
import com.cuso.mobile.view.home.sales.lead.MiniSwitch
// ── NEW: adaptive design tokens ──
import com.cuso.mobile.adaptive_screen.AppDesignTokens
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.R
import com.cuso.mobile.view.composable.FormTextArea

private val BorderColor = Color(0xFFE3E4E8)

@Composable
fun CreateServiceRequest(
    onClose: () -> Unit = {},
    onCancel: () -> Unit = {},
    onCreateServiceRequest: () -> Unit = {},
    isSubmitting: Boolean = false // wire this to your ViewModel's loading state
) {
    // ── Adaptive tokens: pulled from LocalAppTokens (set at app root via
    // CompositionLocalProvider(LocalAppTokens provides getAdaptiveTokens(...))) ──
    val tokens: AppDesignTokens = LocalAppTokens.current
    val sectionGap = tokens.screenPadding                    // ~16 / 24 / 32.dp
    val fieldGap = tokens.screenPadding * 0.75f               // ~12 / 18 / 24.dp
    val smallGap = tokens.screenPadding * 0.5f                 // ~8 / 12 / 16.dp
    val tinyGap = tokens.screenPadding * 0.3f                  // ~5 / 7 / 10.dp
    val adaptiveCorner = RoundedCornerShape(tokens.cardCornerRadius * 0.5f)

    var expandedSection by remember { mutableStateOf("Customer Information") }

    // ── Customer Information state ──
    var customerName by remember { mutableStateOf("") }
    var customerId by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var emailAddress by remember { mutableStateOf("") }

    // ── Sales Order Reference state ──
    var orderId by remember { mutableStateOf("Select Order ID") }
    var orderIdExpanded by remember { mutableStateOf(false) }
    var garmentName by remember { mutableStateOf("") }
    var garmentType by remember { mutableStateOf("") }
    var deliveryDate by remember { mutableStateOf("") }
    var checked by remember { mutableStateOf(true) }

    // ── Service Request state ──
    var serviceType by remember { mutableStateOf("Select Service Type") }
    var serviceTypeExpanded by remember { mutableStateOf(false) }
    var priority by remember { mutableStateOf("Select Service Type") }
    var priorityExpanded by remember { mutableStateOf(false) }
    var issueDescription by remember { mutableStateOf("") }


    // ── Product state ──
    var garmentsForService by remember { mutableStateOf("Select Service Type") }
    var garmentForServiceTypeExpanded by remember { mutableStateOf(false) }
    val garmentOptions = listOf(
        "Sleeve",
        "Neck",
        "Waist",
        "Length",
        "Fit",
        "Damage",
        "Fabric Issue"
    )
    var selectedGarmentCategories by remember { mutableStateOf(listOf<String>()) }

    // ── Upload Evidence state ──
    val launchFilePicker = rememberFilePickerLauncher { uri ->
        // handle selected file uri here
    }
    // ── Preferred Resolution state ──
    var preferredServiceDate by remember { mutableStateOf("") }
    var resolutionNotes by remember { mutableStateOf("") }


    // ── Internal Notes state ──
    var staffOnlyComments by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                TitleBar("Create Service Request", onClose = onClose)
            }
            HorizontalDivider(color = BorderColor)
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = Color.Transparent

    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = tokens.buttonHeight * 1.9f) // clearance so content never sits under the FAB row
            ) {
                // ── Customer Information (expanded by default) ──
                AccordionSection(
                    iconPainter = painterResource(R.drawable.ic_person),
                    title = "Customer Information",
                    expanded = expandedSection == "Customer Information",
                    onHeaderClick = {
                        expandedSection = if (expandedSection == "Customer Information") "" else "Customer Information"
                    }
                ) {
                    FormLabel("Customer Name")
                    FormTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        placeholder = "Enter Customer Name"
                    )

                    Spacer(Modifier.height(fieldGap))
                    FormLabel("Customer ID")
                    FormTextField(
                        value = customerId,
                        onValueChange = { customerId = it },
                        placeholder = "Enter Customer ID"
                    )

                    Spacer(Modifier.height(fieldGap))
                    FormLabel("Phone Number")
                    FormTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        placeholder = "Enter Phone Number",
                        keyboardType = KeyboardType.Phone
                    )

                    Spacer(Modifier.height(fieldGap))
                    FormLabel("Email Address")
                    FormTextField(
                        value = emailAddress,
                        onValueChange = { emailAddress = it },
                        placeholder = "Enter Email Address",
                        keyboardType = KeyboardType.Email
                    )
                }

                // ── Sales Order Reference ──
                AccordionSection(
                    iconPainter = painterResource(R.drawable.ic_transaction_sheet),
                    title = "Sales Order Reference",
                    expanded = expandedSection == "Sales Order Reference",
                    onHeaderClick = {
                        expandedSection = if (expandedSection == "Sales Order Reference") "" else "Sales Order Reference"
                    }
                ) {
                    FormDropdown(
                        label = "Order ID",
                        value = orderId,
                        expanded = orderIdExpanded,
                        onExpandChange = { orderIdExpanded = it },
                        options = listOf("12345","223345","qweweq"),   // list of actual sales order IDs from your ViewModel
                        onOptionSelected = { selected ->
                            orderId = selected
                        }
                    )
                    Spacer(Modifier.height(fieldGap))

                    FormLabel("Garment Name")
                    FormTextField(
                        value = garmentName,
                        onValueChange = { garmentName = it },
                        placeholder = "Enter Garment Name"
                    )
                    Spacer(Modifier.height(fieldGap))

                    FormLabel("Garment Type")
                    FormTextField(
                        value = garmentType,
                        onValueChange = { garmentType = it },
                        placeholder = "Enter Garment Type",
                        keyboardType = KeyboardType.Phone
                    )
                    Spacer(Modifier.height(fieldGap))

                    FormLabel("Delivery Date")
                    DatePickerField(
                        value = deliveryDate,
                        onDateSelected = {deliveryDate = it}
                    )
                    Spacer(Modifier.height(fieldGap))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Text("Trial Completed", color = blackTitle, fontSize = tokens.caption)
                        Spacer(Modifier.width(smallGap))
                        MiniSwitch(
                            checked = false,
                            onCheckedChange = { checked = it}
                        )
                        Spacer(Modifier.weight(1f))
                        Box(
                            Modifier
                                .background(Color(0xFFb2e6c3), RoundedCornerShape(30.dp))
                        ){
                            Text(
                                "Delivered",
                                Modifier.padding(horizontal = smallGap, vertical = tinyGap),
                                color = Color(0xFF0AB83E),
                                fontSize = tokens.caption
                            )
                        }

                    }
                }

                // ── Service Request Details ──
                AccordionSection(
                    iconPainter = painterResource(R.drawable.ic_clippad_lines),
                    title = "Service Request Details",
                    expanded = expandedSection == "Service Request Details",
                    onHeaderClick = {
                        expandedSection = if (expandedSection == "Service Request Details") "" else "Service Request Details"
                    }
                ) {
                    FormDropdown(
                        label = "Service Type",
                        value = serviceType,
                        expanded = serviceTypeExpanded,
                        onExpandChange = { serviceTypeExpanded = it },
                        options = listOf("Replacement","Alteration","Refund"),   // list of actual sales order IDs from your ViewModel
                        onOptionSelected = { selected ->
                            serviceType = selected
                        }
                    )
                    Spacer(Modifier.height(fieldGap))

                    FormDropdown(
                        label = "Priority",
                        value = priority,
                        expanded = priorityExpanded,
                        onExpandChange = { priorityExpanded = it },
                        options = listOf("Low","Medium","High"),   // list of actual sales order IDs from your ViewModel
                        onOptionSelected = { selected ->
                            priority = selected
                        }
                    )
                    Spacer(Modifier.height(fieldGap))

                    FormLabel("Issue Description")
                    FormTextArea(
                        value=issueDescription,
                        onValueChange = { issueDescription = it }
                    )
                }

                // ── Product Details ──
                AccordionSection(
                    iconPainter = painterResource(R.drawable.ic_clippad_tick),
                    title = "Product Details",
                    expanded = expandedSection == "Product Details",
                    onHeaderClick = {
                        expandedSection = if (expandedSection == "Product Details") "" else "Product Details"
                    }
                ) {

                    FormDropdown(
                        label = "Select Garments for Service",
                        value = garmentsForService,
                        expanded = garmentForServiceTypeExpanded,
                        onExpandChange = { garmentForServiceTypeExpanded = it },
                        options = listOf("Replacement","Alteration","Refund"),   // list of actual sales order IDs from your ViewModel
                        onOptionSelected = { selected ->
                            garmentsForService = selected
                        }
                    )
                    Spacer(Modifier.height(fieldGap))
                    FormLabel("Issue Areas")
                    Spacer(Modifier.height(fieldGap))
                    SelectableChipRow(
                        options = garmentOptions,
                        selectedOptions = selectedGarmentCategories,
                        onSelectionChange = { selectedGarmentCategories = it }
                    )
                }

                // ── Upload Evidence ──
                AccordionSection(
                    iconPainter = painterResource(R.drawable.ic_upload_cloud),
                    title = "Upload Evidence",
                    expanded = expandedSection == "Upload Evidence",
                    onHeaderClick = {
                        expandedSection = if (expandedSection == "Upload Evidence") "" else "Upload Evidence"
                    }
                ) {
                    DashedUploadBox(
                        onBrowseClick = { launchFilePicker() }
                    )
                }

                // ── Preferred Resolution ──
                AccordionSection(
                    iconPainter = painterResource(R.drawable.ic_calendar_tick),
                    title = "Preferred Resolution",
                    expanded = expandedSection == "Preferred Resolution",
                    onHeaderClick = {
                        expandedSection = if (expandedSection == "Preferred Resolution") "" else "Preferred Resolution"
                    }
                ) {
                    FormLabel("Preferred Service Date")
                    DatePickerField(
                        value = preferredServiceDate,
                        onDateSelected = {preferredServiceDate = it}
                    )
                    Spacer(Modifier.height(fieldGap))

                    FormLabel("Resolution Notes")
                    FormTextArea(
                        value = resolutionNotes,
                        onValueChange = { resolutionNotes = it }
                    )
                }

                // ── Internal Notes ──
                AccordionSection(
                    iconPainter = painterResource(R.drawable.ic_lock_2),
                    title = "Internal Notes",
                    iconTint = TextSecondary,
                    expanded = expandedSection == "Internal Notes",
                    onHeaderClick = {
                        expandedSection = if (expandedSection == "Internal Notes") "" else "Internal Notes"
                    }
                ) {
                    FormLabel("Staff-only comments")
                    FormTextArea(
                        value = staffOnlyComments,
                        onValueChange = { staffOnlyComments = it }
                    )
                }

                // ── Charges ──
                AccordionSection(
                    title = "Charges",
                    expanded = expandedSection == "Charges",
                    onHeaderClick = {
                        expandedSection = if (expandedSection == "Charges") "" else "Charges"
                    }
                ) {

                    ServicesSection()


                }
            }

            // ── Bottom action row: Cancel (Back) + Create Service Request (Trailing) ──
            StepNavigationFab(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(tokens.buttonHeight * 1.9f)
                    .align(Alignment.BottomCenter),
                showBack = true,
                onBack = onCancel,
                backLabel = "Cancel",
                backWidthFraction = 0.35f,       // keeps "Cancel" from colliding with the long trailing label
                trailingWidthFraction = 0.55f,   // "Create Service Request" needs more room than the default pill
                trailingAction = TrailingFabAction.Update(
                    label = "Create Service Request",
                    isLoading = isSubmitting,
                    onClick = onCreateServiceRequest
                )
            )
        }
    }
}

@Composable
fun AmountInputBox(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "0.00"
) {
    val tokens = LocalAppTokens.current
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = LocalTextStyle.current.copy(
            fontSize = tokens.bodyMedium,
            color = Color(0xFF1A1A1A),
            textAlign = TextAlign.Center
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        cursorBrush = SolidColor(Primary),
        modifier = modifier
            .width(80.dp)
            .height(tokens.fieldHeight * 0.9f)
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
            .background(whiteBg, RoundedCornerShape(tokens.cardCornerRadius * 0.5f)),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = tokens.screenPadding * 0.5f),
                contentAlignment = Alignment.Center
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        fontSize = tokens.bodyMedium,
                        color = Color(0xFFB0B0B0), //   placeholder grey
                        textAlign = TextAlign.Center
                    )
                }
                innerTextField()
            }
        }
    )
}
@Composable
fun ServicesSection() {
    val tokens = LocalAppTokens.current
    var fields by remember { mutableStateOf(listOf("")) }

    Column(Modifier.fillMaxWidth()) {
        fields.forEachIndexed { index, value ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = tokens.screenPadding * 0.25f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Services", fontSize = tokens.bodyMedium, color = Color(0xFF1A1A1A))
                Spacer(Modifier.weight(1f))

                AmountInputBox(
                    value = value,
                    onValueChange = { newValue ->
                        fields = fields.toMutableList().also { it[index] = newValue }
                    }
                )
                Spacer(Modifier.width(tokens.screenPadding * 0.5f))
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove field",
                    tint = Color(0xFF9CA3AF),
                    modifier = Modifier
                        .size(tokens.iconSize)
                        .clickable {
                            fields = fields.toMutableList().also { it.removeAt(index) }
                        }
                )

            }
        }

        Spacer(Modifier.height(tokens.screenPadding * 0.5f))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Add, null, tint = Primary, modifier = Modifier.size(tokens.iconSize))
            Spacer(Modifier.width(tokens.screenPadding * 0.6f))

            Text(
                text = "Add field",
                color = Primary,
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clickable { fields = fields + "" }
                    .padding(vertical = tokens.screenPadding * 0.25f)
            )
        }
    }
}