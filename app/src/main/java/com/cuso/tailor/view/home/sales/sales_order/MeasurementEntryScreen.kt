@file:Suppress(
    "UNUSED_VALUE",
    "SpellCheckingInspection",
    "GrazieInspection",
    "AssignedValueIsNeverRead",
    "unused_variable",
    "unused_parameter",
    "UnusedMaterial3ScaffoldPaddingParameter",
    "VariableNeverRead"
)

package com.cuso.tailor.view.home.sales.sales_order

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.sales.OrderOverviewItem
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.SettingsViewModel
import com.google.gson.Gson
import com.google.gson.JsonParser

/**
 * Data model representing the measurement fields populated from
 * the Garment Category View-One API response (/api/sales/settings/garment-categories/view-one/{id})
 */
data class CategoryMeasurementFieldData(
    val id: String,
    val name: String,
    val displayName: String,
    val groupName: String = "General",
    val unit: String = "inch",
    val inputType: String = "Number",
    val isRequired: Boolean = false,
    val displayOrder: Int = 0
)
private fun OrderOverviewItem.toInitialMeasurements(): Map<String, Float> {
    return measurements.mapNotNull { measurement ->

        val fieldId = measurement.fieldId?.id
            ?.takeIf { it.isNotBlank() }
            ?: return@mapNotNull null

        val value = measurement.entries
            .firstOrNull()
            ?.value
            ?: return@mapNotNull null

        fieldId to value.toFloat()
    }.toMap()
}
@Composable
fun MeasurementEntryScreen(
    garmentType: String = "Shirt",
    garmentCategory: String = "",
    garmentCategoryId: String = "",
    orderItemId: String = "",
    customerName: String = "",
    customerPhone: String = "",
    initialMeasurements: Map<String, Float> = emptyMap(),
    onClose: () -> Unit = {},
    onDiscard: () -> Unit = {},
    onSaveMeasurement: (Map<String, Float>) -> Unit = {},
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val scrollState = rememberScrollState()

    // Store key: one measurement profile per order item (falls back to category id)
    val storeKey = remember(orderItemId, garmentCategoryId, garmentType) {
        orderItemId.ifBlank { garmentCategoryId.ifBlank { garmentType } }
    }

    // Previously saved state for this item (restored when the user comes back)
    val savedSnapshot = remember(storeKey) { OrderFlowStore.measurements[storeKey] }

    var showDiscardDialog by remember { mutableStateOf(false) }

    // ── API State Collection from SettingsViewModel ──
    val selectedStyleDetail by settingsViewModel.selectedStyleDetail.collectAsStateWithLifecycle()
    val isLoadingStyleDetail by settingsViewModel.isLoadingStyleDetail.collectAsStateWithLifecycle()

    // ── Fetch Garment Category View-One API using the Category ID ──
    LaunchedEffect(garmentCategoryId) {
        if (garmentCategoryId.isNotBlank()) {
            settingsViewModel.fetchGarmentCategoryById(garmentCategoryId)
        }
    }

    // ── Extract measurementFields directly from View-One API response ──
    val activeMeasurementFields: List<CategoryMeasurementFieldData> = remember(selectedStyleDetail) {
        if (selectedStyleDetail == null) return@remember emptyList()
        try {
            val jsonString = Gson().toJson(selectedStyleDetail)
            val jsonObject = JsonParser.parseString(jsonString).asJsonObject

            val array = if (jsonObject.has("data") && jsonObject.get("data").isJsonObject) {
                jsonObject.getAsJsonObject("data").getAsJsonArray("measurementFields")
            } else if (jsonObject.has("measurementFields") && jsonObject.get("measurementFields").isJsonArray) {
                jsonObject.getAsJsonArray("measurementFields")
            } else {
                null
            }

            val list = mutableListOf<CategoryMeasurementFieldData>()
            array?.forEach { element ->
                if (element.isJsonObject) {
                    val itemObj = element.asJsonObject
                    val isRequired = itemObj.get("isRequired")?.asBoolean ?: false
                    val order = itemObj.get("displayOrder")?.asInt ?: 0

                    val fieldObj = if (itemObj.has("fieldId") && itemObj.get("fieldId").isJsonObject) {
                        itemObj.getAsJsonObject("fieldId")
                    } else null

                    if (fieldObj != null) {
                        val id = fieldObj.get("_id")?.asString ?: fieldObj.get("id")?.asString ?: ""
                        val name = fieldObj.get("name")?.asString ?: ""
                        val displayName = fieldObj.get("displayName")?.asString?.takeIf { it.isNotBlank() } ?: name
                        val groupName = fieldObj.get("groupName")?.asString?.trim()?.takeIf { it.isNotBlank() } ?: "General"
                        val unit = fieldObj.get("unit")?.asString?.takeIf { it.isNotBlank() } ?: "inch"
                        val inputType = fieldObj.get("inputType")?.asString ?: "Number"

                        if (id.isNotBlank() && name.isNotBlank()) {
                            list.add(
                                CategoryMeasurementFieldData(
                                    id = id,
                                    name = name,
                                    displayName = displayName,
                                    groupName = groupName,
                                    unit = unit,
                                    inputType = inputType,
                                    isRequired = isRequired,
                                    displayOrder = order
                                )
                            )
                        }
                    }
                }
            }
            list.sortedBy { it.displayOrder }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // ── Category Display Name resolved from API response or passed argument ──
    val displayCategoryName = remember(garmentCategory, selectedStyleDetail) {
        if (garmentCategory.isNotBlank()) {
            garmentCategory
        } else {
            selectedStyleDetail?.displayName ?: selectedStyleDetail?.name ?: ""
        }
    }

    // ── Accordion Expansion States ──
    var detailsByValueExpanded by rememberSaveable { mutableStateOf(true) }
    var specificationsExpanded by rememberSaveable { mutableStateOf(true) }

    // ── Profile and Config States (restored from the saved snapshot when available) ──
    var profileName by rememberSaveable { mutableStateOf(savedSnapshot?.profileName ?: "Standard Profile") }
    var linkedPreviousMeasurement by rememberSaveable {
        mutableStateOf(savedSnapshot?.linkedPreviousMeasurement ?: "Previous Body")
    }
    var linkedPrevExpanded by remember { mutableStateOf(false) }

    var measurementDate by rememberSaveable { mutableStateOf(savedSnapshot?.measurementDate ?: "") }
    var takenByStaff by rememberSaveable { mutableStateOf(savedSnapshot?.takenBy ?: "Master Tailor") }

    var selectedUnit by rememberSaveable { mutableStateOf(savedSnapshot?.unit ?: "IN") } // "CM" or "IN"
    var fitType by rememberSaveable { mutableStateOf(savedSnapshot?.fitType ?: "Regular Fit") }
    var fitTypeExpanded by remember { mutableStateOf(false) }

    var specialInstructions by rememberSaveable {
        mutableStateOf(
            savedSnapshot?.specialInstructions ?: ""
        )
    }

    // ── Dynamic Measurements Map: preserves previously entered values ──
    val measurementValues = remember(storeKey, initialMeasurements) {
        mutableStateMapOf<String, Float>().apply {
            putAll(initialMeasurements)

            // Previously entered values should take priority
            savedSnapshot?.let {
                putAll(it.fieldValues)
            }
        }
    }

    // ── Persist everything entered on this screen into the flow store ──
    fun persistMeasurement() {
        val previousFields = OrderFlowStore.measurements[storeKey]?.fields.orEmpty()

        // If the API fields are not loaded yet, keep the previously saved field list
        val resolvedFields = if (activeMeasurementFields.isNotEmpty()) {
            activeMeasurementFields.map { field ->
                MeasurementFieldValue(
                    fieldId = field.id,
                    name = field.name,
                    label = field.displayName.ifBlank { field.name },
                    value = measurementValues[field.id] ?: 0f,
                    unit = field.unit.ifBlank { selectedUnit },
                    group = field.groupName.ifBlank { "General" }
                )
            }
        } else {
            previousFields.map { old ->
                old.copy(value = measurementValues[old.fieldId] ?: old.value)
            }
        }

        OrderFlowStore.measurements[storeKey] = MeasurementSnapshot(
            key = storeKey,
            garmentType = garmentType,
            garmentCategory = displayCategoryName,
            profileName = profileName,
            linkedPreviousMeasurement = linkedPreviousMeasurement,
            measurementDate = measurementDate,
            takenBy = takenByStaff,
            unit = selectedUnit,
            fitType = fitType,
            specialInstructions = specialInstructions,
            fields = resolvedFields
        )
    }

    // Back press keeps the entered data and navigates back
    BackHandler {
        persistMeasurement()
        onClose()
    }

    if (showDiscardDialog) {
        DiscardOrderConfirmDialog(
            onConfirmDiscard = {
                showDiscardDialog = false
                OrderFlowStore.clear()
                onDiscard()
            },
            onDismiss = { showDiscardDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TitleBar(
                title = "Measurement Entry",
                onClose = {
                    persistMeasurement()
                    onClose()
                }
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
                        .background(primary_light)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    val displayGarmentHeader = if (displayCategoryName.isNotBlank()) {
                        "${garmentType.uppercase()} – ${displayCategoryName.uppercase()}"
                    } else {
                        garmentType.uppercase()
                    }
                    Text(
                        text = "GARMENT: $displayGarmentHeader",
                        fontSize = tokens.label,
                        fontWeight = FontWeight.Medium,
                        color = Primary,
                        letterSpacing = 0.5.sp
                    )
                }

                // ─────────────────────────────────────────────────────────────
                // 1. DETAILS BY VALUE (CUSTOMER AND GARMENT PREFILLED, DISABLED)
                // ─────────────────────────────────────────────────────────────
                AccordionSection(
                    title = "Details by Value",
                    expanded = detailsByValueExpanded,
                    onHeaderClick = { detailsByValueExpanded = !detailsByValueExpanded }
                ) {
                    FormLabel("Customer Name", isRequired = true)
                    DisabledFormTextField(
                        value = customerName.ifBlank { "Customer Name Not Selected" }
                    )

                    Spacer(Modifier.height(14.dp))

                    FormLabel("Phone")
                    DisabledFormTextField(
                        value = customerPhone.ifBlank { "No Phone Number" }
                    )

                    Spacer(Modifier.height(14.dp))

                    FormLabel("Garment Category", isRequired = true)
                    DisabledFormTextField(
                        value = if (displayCategoryName.isNotBlank()) "$garmentType – $displayCategoryName" else garmentType
                    )

                    Spacer(Modifier.height(14.dp))

                    FormLabel("Measurement Profile Name")
                    FormTextField(
                        value = profileName,
                        onValueChange = { profileName = it },
                        placeholder = "e.g. Standard Profile"
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
                        placeholder = "Staff name"
                    )

                    Spacer(Modifier.height(14.dp))

                    FormLabel("Unit Selection")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(grey_border)
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        UnitSegmentButton(
                            label = "CM",
                            isSelected = selectedUnit == "CM",
                            onClick = { selectedUnit = "CM" },
                            modifier = Modifier.weight(1f)
                        )
                        UnitSegmentButton(
                            label = "IN",
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
                        placeholder = "Provide extra ease on armholes, etc."
                    )
                }

                // ─────────────────────────────────────────────────────────────
                // 2. GARMENT SPECIFICATIONS (DRIVEN FROM VIEW-ONE API)
                // ─────────────────────────────────────────────────────────────
                AccordionSection(
                    title = if (displayCategoryName.isNotBlank()) "$displayCategoryName Specifications" else "$garmentType Specifications",
                    expanded = specificationsExpanded,
                    onHeaderClick = { specificationsExpanded = !specificationsExpanded }
                ) {
                    if (isLoadingStyleDetail) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Primary,
                                strokeWidth = 2.dp
                            )
                        }
                    } else if (activeMeasurementFields.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No measurement fields found for this category.",
                                fontSize = tokens.bodySmall,
                                color = TextSecondary
                            )
                        }
                    } else {
                        // Group measurement fields by groupName
                        val groupedFields = remember(activeMeasurementFields) {
                            activeMeasurementFields.groupBy { it.groupName.ifBlank { "General Measurements" } }
                        }

                        for ((groupHeader, fieldsInGroup) in groupedFields) {
                            SectionCategoryHeader(groupHeader.uppercase())

                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                fieldsInGroup.chunked(2).forEach { rowFields ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        rowFields.forEach { field ->
                                            val fieldId = field.id
                                            val initialVal = measurementValues[fieldId] ?: 0f

                                            MeasurementStepperField(
                                                label = field.displayName.ifBlank { field.name },
                                                value = initialVal,
                                                unit = field.unit.ifBlank { selectedUnit },
                                                onValueChange = { newVal ->
                                                    measurementValues[fieldId] = newVal
                                                },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }

                                        if (rowFields.size == 1) {
                                            Spacer(Modifier.weight(1f))
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(14.dp))
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
                        onClick = {
                            persistMeasurement()
                            onSaveMeasurement(measurementValues.toMap())
                        }
                    )
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────
// DISABLED / READ-ONLY FORM TEXT FIELD
// ─────────────────────────────────────────────────────────────────────────
@Composable
fun DisabledFormTextField(
    value: String,
    modifier: Modifier = Modifier
) {
    val tokens = LocalAppTokens.current
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(tokens.fieldHeight),
        shape = RoundedCornerShape(10.dp),
        color = badgeGrey,
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = value,
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.Medium,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────
// STEPPER COMPONENT WITH EDITABLE TEXT FIELD
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
    val tokens = LocalAppTokens.current
    val focusManager = LocalFocusManager.current

    fun formatFloat(num: Float): String {
        return if (num % 1 == 0f) "%.1f".format(num) else "%.2f".format(num).trimEnd('0')
    }

    var textValue by remember { mutableStateOf(formatFloat(value)) }

    LaunchedEffect(value) {
        val parsed = textValue.toFloatOrNull()
        if (parsed != value) {
            textValue = formatFloat(value)
        }
    }

    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = tokens.caption,
            color = headerGrey,
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
                .background(grey_border)
                .border(1.dp, sectionBorder, RoundedCornerShape(8.dp)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Decrement button
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clickable {
                        if (value - step >= min) {
                            val newVal = (value - step).coerceAtLeast(min)
                            onValueChange(newVal)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "–",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = textSubdued
                )
            }

            // Central editable text field and unit label
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(whiteBg),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    BasicTextField(
                        value = textValue,
                        onValueChange = { input ->
                            if (input.isEmpty() || input.matches(Regex("""^\d*\.?\d{0,2}$"""))) {
                                textValue = input
                                val parsed = input.toFloatOrNull()
                                if (parsed != null && parsed >= min) {
                                    onValueChange(parsed)
                                }
                            }
                        },
                        textStyle = TextStyle(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = title_color,
                            textAlign = TextAlign.Center
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .width(IntrinsicSize.Min)
                            .widthIn(min = 28.dp, max = 56.dp)
                    )

                    Spacer(Modifier.width(4.dp))

                    Text(
                        text = unit,
                        fontSize = tokens.label,
                        fontWeight = FontWeight.Medium,
                        color = iconMuted
                    )
                }
            }

            // Increment button
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clickable {
                        val newVal = value + step
                        onValueChange(newVal)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = textSubdued
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────
// SECTION SUB-HEADER
// ─────────────────────────────────────────────────────────────────────────
@Composable
private fun SectionCategoryHeader(text: String) {
    val tokens = LocalAppTokens.current
    Text(
        text = text,
        fontSize = tokens.label,
        fontWeight = FontWeight.Medium,
        color = title_color,
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
    val tokens = LocalAppTokens.current
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) whiteBg else Color.Transparent)
            .then(
                if (isSelected) Modifier.border(0.5.dp, iconMuted, RoundedCornerShape(6.dp))
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
            fontSize = tokens.bodySmall,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) title_color else headerGrey
        )
    }
}