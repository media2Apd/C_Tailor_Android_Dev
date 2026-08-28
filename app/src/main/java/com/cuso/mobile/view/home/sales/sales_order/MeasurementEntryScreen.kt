package com.cuso.mobile.view.home.sales.sales_order

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.*
import com.cuso.mobile.view.composable.*

@Composable
fun MeasurementEntryScreen(
    garmentType: String = "Shirt",
    customerName: String = "",
    customerPhone: String = "",
    onClose: () -> Unit = {},
    onSaveMeasurement: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val scrollState = rememberScrollState()

    // ── Accordion Expansion States ──
    var detailsByValueExpanded by rememberSaveable { mutableStateOf(true) }
    var specificationsExpanded by rememberSaveable { mutableStateOf(true) }

    // ── Details by Value Form States ──
    var selectedCustomer by rememberSaveable { mutableStateOf(customerName.ifBlank { "Select Customer Name..." }) }
    var customerDropdownExpanded by remember { mutableStateOf(false) }

    var phone by rememberSaveable { mutableStateOf(customerPhone.ifBlank { "Default" }) }
    var selectedGarmentType by rememberSaveable { mutableStateOf(garmentType) }
    var garmentTypeExpanded by remember { mutableStateOf(false) }

    var profileName by rememberSaveable { mutableStateOf("Pending Entry") }
    var linkedPreviousMeasurement by rememberSaveable { mutableStateOf("Previous Body") }
    var linkedPrevExpanded by remember { mutableStateOf(false) }

    var measurementDate by rememberSaveable { mutableStateOf("") }
    var takenByStaff by rememberSaveable { mutableStateOf("Pending Entry") }

    var selectedUnit by rememberSaveable { mutableStateOf("IN") } // "CM" or "IN"
    var fitType by rememberSaveable { mutableStateOf("Regular Fit") }
    var fitTypeExpanded by remember { mutableStateOf(false) }

    var specialInstructions by rememberSaveable {
        mutableStateOf("Provide extra ease on standard armholes. Avoid tightening around chest.")
    }

    // ── Primary Body Measurements ──
    var chestRound by rememberSaveable { mutableFloatStateOf(38.5f) }
    var waistRound by rememberSaveable { mutableFloatStateOf(34.0f) }
    var seatHipRound by rememberSaveable { mutableFloatStateOf(40.0f) }
    var shoulderWidth by rememberSaveable { mutableFloatStateOf(18.0f) }
    var bodyWidth by rememberSaveable { mutableFloatStateOf(20.5f) }
    var acrossShoulder by rememberSaveable { mutableFloatStateOf(17.5f) }

    // ── Sleeve Measurements ──
    var sleeveLengthFull by rememberSaveable { mutableFloatStateOf(24.5f) }
    var sleeveLengthHalf by rememberSaveable { mutableFloatStateOf(9.5f) }
    var bicepRound by rememberSaveable { mutableFloatStateOf(14.0f) }
    var elbowRound by rememberSaveable { mutableFloatStateOf(12.5f) }
    var wristRound by rememberSaveable { mutableFloatStateOf(6.5f) }
    var armholeRound by rememberSaveable { mutableFloatStateOf(19.0f) }

    // ── Neck & Collar Measurements ──
    var neckRound by rememberSaveable { mutableFloatStateOf(15.5f) }

    // ── Length & Measurements ──
    var shirtLength by rememberSaveable { mutableFloatStateOf(29.0f) }
    var robeLength by rememberSaveable { mutableFloatStateOf(44.0f) }
    var frontChest by rememberSaveable { mutableFloatStateOf(14.5f) }
    var cuffSize by rememberSaveable { mutableFloatStateOf(9.0f) }
    var placketWidth by rememberSaveable { mutableFloatStateOf(1.25f) }
    var pocketPosition by rememberSaveable { mutableFloatStateOf(8.0f) }

    // ── Auto-Calculated Standard Allowances ──
    val fitAllowanceCm = "3.8 CM"
    val fitAllowanceIn = "1.5 IN"

    Scaffold(
        topBar = {
            TitleBar(
                title = "Measurement Entry",
                onClose = onClose
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(scrollState)
                    .padding(bottom = 90.dp)
            ) {
                // ── Top Garment Type Badge ──
                Box(
                    modifier = Modifier
                        .padding(horizontal = tokens.screenPadding, vertical = 10.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFEEF2FF))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "GARMENT TYPE: ${selectedGarmentType.uppercase()}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Primary,
                        letterSpacing = 0.5.sp
                    )
                }

                // ─────────────────────────────────────────────────────────────
                // 1. DETAILS BY VALUE
                // ─────────────────────────────────────────────────────────────
                AccordionSection(
                    title = "Details by Value",
                    expanded = detailsByValueExpanded,
                    onHeaderClick = { detailsByValueExpanded = !detailsByValueExpanded }
                ) {
                    FormDropdown(
                        label = "Customer Name",
                        value = selectedCustomer,
                        expanded = customerDropdownExpanded,
                        onExpandChange = { customerDropdownExpanded = it },
                        options = listOf("Ravi Kumar", "Anita Sharma", "Rajesh Mehta"),
                        onOptionSelected = { selectedCustomer = it }
                    )

                    Spacer(Modifier.height(14.dp))

                    FormLabel("Phone")
                    FormTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        placeholder = "Default"
                    )

                    Spacer(Modifier.height(14.dp))

                    FormDropdown(
                        label = "Garment Type",
                        value = selectedGarmentType,
                        expanded = garmentTypeExpanded,
                        onExpandChange = { garmentTypeExpanded = it },
                        options = listOf("Shirt", "Pant", "Suit", "Kurta"),
                        onOptionSelected = { selectedGarmentType = it }
                    )

                    Spacer(Modifier.height(14.dp))

                    FormLabel("Measurement Profile Name")
                    FormTextField(
                        value = profileName,
                        onValueChange = { profileName = it },
                        placeholder = "Pending Entry"
                    )

                    Spacer(Modifier.height(14.dp))

                    FormDropdown(
                        label = "Linked Previous Measurement",
                        value = linkedPreviousMeasurement,
                        expanded = linkedPrevExpanded,
                        onExpandChange = { linkedPrevExpanded = it },
                        options = listOf("Previous Body", "Custom Standard", "None"),
                        onOptionSelected = { linkedPreviousMeasurement = it }
                    )

                    Spacer(Modifier.height(14.dp))

                    FormLabel("Date of Measurement")
                    DatePickerField(
                        value = measurementDate,
                        onDateSelected = { measurementDate = it }
                    )

                    Spacer(Modifier.height(14.dp))

                    FormLabel("Taken By (Staff Name)")
                    FormTextField(
                        value = takenByStaff,
                        onValueChange = { takenByStaff = it },
                        placeholder = "Pending Entry"
                    )

                    Spacer(Modifier.height(14.dp))

                    // ── Unit Selection Segmented Button ──
                    FormLabel("Unit Selection")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF1F5F9))
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        UnitSegmentButton(
                            label = "CM (4)",
                            isSelected = selectedUnit == "CM",
                            onClick = { selectedUnit = "CM" },
                            modifier = Modifier.weight(1f)
                        )
                        UnitSegmentButton(
                            label = "IN (1)",
                            isSelected = selectedUnit == "IN",
                            onClick = { selectedUnit = "IN" },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    FormDropdown(
                        label = "Fit Type",
                        value = fitType,
                        expanded = fitTypeExpanded,
                        onExpandChange = { fitTypeExpanded = it },
                        options = listOf("Regular Fit", "Slim Fit", "Comfort Fit", "Tailored Fit"),
                        onOptionSelected = { fitType = it }
                    )

                    Spacer(Modifier.height(14.dp))

                    FormLabel("Special Instructions")
                    FormTextArea(
                        value = specialInstructions,
                        onValueChange = { specialInstructions = it },
                        placeholder = "Provide extra ease on standard armholes. Avoid tightening..."
                    )
                }

                // ─────────────────────────────────────────────────────────────
                // 2. SHIRT SPECIFICATIONS (MEASUREMENTS GRID)
                // ─────────────────────────────────────────────────────────────
                AccordionSection(
                    title = "Shirt Specifications",
                    expanded = specificationsExpanded,
                    onHeaderClick = { specificationsExpanded = !specificationsExpanded }
                ) {
                    // A. PRIMARY BODY
                    SectionCategoryHeader("PRIMARY BODY")
                    MeasurementGrid(
                        items = listOf(
                            Triple("Chest Round", chestRound) { chestRound = it },
                            Triple("Waist Round", waistRound) { waistRound = it },
                            Triple("Seat / Hip Round", seatHipRound) { seatHipRound = it },
                            Triple("Shoulder Width", shoulderWidth) { shoulderWidth = it },
                            Triple("Body Width", bodyWidth) { bodyWidth = it },
                            Triple("Across Shoulder", acrossShoulder) { acrossShoulder = it }
                        ),
                        unit = selectedUnit
                    )

                    Spacer(Modifier.height(16.dp))

                    // B. SLEEVE MEASUREMENTS
                    SectionCategoryHeader("SLEEVE MEASUREMENTS")
                    MeasurementGrid(
                        items = listOf(
                            Triple("Sleeve Length (Full)", sleeveLengthFull) { sleeveLengthFull = it },
                            Triple("Sleeve Length (Half)", sleeveLengthHalf) { sleeveLengthHalf = it },
                            Triple("Bicep Round", bicepRound) { bicepRound = it },
                            Triple("Elbow Round", elbowRound) { elbowRound = it },
                            Triple("Wrist Round", wristRound) { wristRound = it },
                            Triple("Armhole Round", armholeRound) { armholeRound = it }
                        ),
                        unit = selectedUnit
                    )

                    Spacer(Modifier.height(16.dp))

                    // C. NECK & COLLAR MEASUREMENTS
                    SectionCategoryHeader("NECK & COLLAR MEASUREMENTS")
                    Row(modifier = Modifier.fillMaxWidth(0.5f).padding(end = 4.dp)) {
                        MeasurementStepperField(
                            label = "Neck Round",
                            value = neckRound,
                            unit = selectedUnit,
                            onValueChange = { neckRound = it }
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // D. LENGTH & MEASUREMENTS
                    SectionCategoryHeader("LENGTH & MEASUREMENTS")
                    MeasurementGrid(
                        items = listOf(
                            Triple("Shirt Length", shirtLength) { shirtLength = it },
                            Triple("Robe Length", robeLength) { robeLength = it },
                            Triple("Front Chest", frontChest) { frontChest = it },
                            Triple("Cuff Size", cuffSize) { cuffSize = it },
                            Triple("Placket Width", placketWidth) { placketWidth = it },
                            Triple("Pocket Position", pocketPosition) { pocketPosition = it }
                        ),
                        unit = selectedUnit
                    )

                    Spacer(Modifier.height(20.dp))

                    // E. AUTO-CALCULATED VALUES (STANDARDIZE)
                    SectionCategoryHeader("AUTO-CALCULATED VALUES (STANDARDIZE)")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel("Fit Allowance (CM)")
                            StaticDisplayField(value = fitAllowanceCm)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel("Fit Allowance (IN)")
                            StaticDisplayField(value = fitAllowanceIn)
                        }
                    }
                }
            }

            // ─────────────────────────────────────────────────────────────
            // FLOATING ACTION BUTTON (TRAILING FAB)
            // ─────────────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 24.dp)
            ) {
                TrailingFabButton(
                    action = TrailingFabAction.Next(
                        label = "Save Measurement",
                        onClick = onSaveMeasurement
                    )
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────
// PIXEL-PERFECT STEPPER COMPONENT
// ─────────────────────────────────────────────────────────────────────────
@Composable
fun MeasurementStepperField(
    modifier: Modifier = Modifier,
    label: String,
    value: Float,
    unit: String,
    onValueChange: (Float) -> Unit,
    step: Float = 0.5f,
    min: Float = 0f
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF64748B),
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(Modifier.height(6.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF1F5F9))
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Decrement Button [ - ]
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clickable {
                        if (value - step >= min) {
                            onValueChange((value - step).coerceAtLeast(min))
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "–",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF334155)
                )
            }

            // Central Value & Unit Display (Centered)
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(whiteBg),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (value % 1 == 0f) "%.1f".format(value) else "%.2f".format(value).trimEnd('0'),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1E293B)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = unit,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8)
                )
            }

            // Increment Button [ + ]
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clickable {
                        onValueChange(value + step)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF334155)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────
// 2-COLUMN GRID HELPER
// ─────────────────────────────────────────────────────────────────────────
@Composable
private fun MeasurementGrid(
    items: List<Triple<String, Float, (Float) -> Unit>>,
    unit: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items.chunked(2).forEach { rowPair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowPair.forEach { (label, value, onValueChange) ->
                    MeasurementStepperField(
                        label = label,
                        value = value,
                        unit = unit,
                        onValueChange = onValueChange,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowPair.size == 1) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────
// SECTION SUB-HEADER (PRIMARY BODY, SLEEVE, ETC.)
// ─────────────────────────────────────────────────────────────────────────
@Composable
private fun SectionCategoryHeader(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF1E293B),
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(top = 8.dp, bottom = 10.dp)
    )
}

// ─────────────────────────────────────────────────────────────────────────
// UNIT SEGMENTED TOGGLE BUTTON
// ─────────────────────────────────────────────────────────────────────────
@Composable
private fun UnitSegmentButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) whiteBg else Color.Transparent)
            .then(
                if (isSelected) Modifier.border(0.5.dp, Color(0xFFCBD5E1), RoundedCornerShape(6.dp))
                else Modifier
            )
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color(0xFF1E293B) else Color(0xFF64748B)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────
// STATIC DISPLAY FIELD FOR AUTO-CALCULATED VALUES
// ─────────────────────────────────────────────────────────────────────────
@Composable
private fun StaticDisplayField(value: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFFAFAFA))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF334155)
        )
    }
}