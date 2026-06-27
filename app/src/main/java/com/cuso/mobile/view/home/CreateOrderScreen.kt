package com.cuso.mobile.view.sales

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cuso.mobile.view.composable.PhoneInputField

// ─────────────────────────────────────────────────────────────
// Data Models
// ─────────────────────────────────────────────────────────────

data class SelectedGarment(
    val category: String,
    val categoryName: String,
    val quantity: Int = 1,
    val price: Double = 0.0,
    val priority: String = "Low",
    val trialRequired: Boolean = false,
    val fabricSource: String = "In-House",
    val fabricType: String = "",
    val colorTone: String = "",
    val pattern: String = "Solid",
    val models: List<String> = emptyList()
)

data class GarmentModel(
    val id: String,
    val name: String,
    val isSelected: Boolean = false
)

// ─────────────────────────────────────────────────────────────
// Screen
// ─────────────────────────────────────────────────────────────

@Composable
fun CreateOrderScreen(
    onBack: () -> Unit = {},
    onCancel: () -> Unit = {},
    onNextStep: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    // ── Customer state ──
    var phone by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Male") }
    var dressFor by remember { mutableStateOf("Men") }
    var source by remember { mutableStateOf("Walk-in") }
    var showDressDropdown by remember { mutableStateOf(false) }
    var showSourceDropdown by remember { mutableStateOf(false) }

    // ── Garment state ──
    val quickCategories = listOf(
        "Pant" to "👔",
        "Shirt" to "👕",
        "Jacket" to "🧥",
        "Blazer" to "👔",
        "Kurta" to "👕"
    )
    val selectedGarments = remember { mutableStateListOf<SelectedGarment>() }

    // ── Dialog state ──
    var showGarmentDialog by remember { mutableStateOf(false) }
    var editingGarmentIndex by remember { mutableStateOf<Int?>(null) }

    // ── Temporary garment for dialog ──
    var tempGarment by remember {
        mutableStateOf(
            SelectedGarment(
                category = "",
                categoryName = "",
                quantity = 1,
                price = 0.0,
                priority = "Low",
                trialRequired = false,
                fabricSource = "In-House",
                fabricType = "",
                colorTone = "",
                pattern = "Solid",
                models = emptyList()
            )
        )
    }

    // ── Delivery state ──
    var orderDate by remember { mutableStateOf("27-06-2026") }
    var trialDate by remember { mutableStateOf("") }
    var deliveryDate by remember { mutableStateOf("") }
    var branch by remember { mutableStateOf("") }
    var showBranchDropdown by remember { mutableStateOf(false) }

    // ── Notes ──
    var stylingNotes by remember { mutableStateOf("") }
    var isRecording by remember { mutableStateOf(false) }

    // ── Open dialog for new garment ──
    fun openGarmentDialog(categoryName: String, category: String) {
        tempGarment = SelectedGarment(
            category = category,
            categoryName = categoryName,
            quantity = 1,
            price = 0.0,
            priority = "Low",
            trialRequired = false,
            fabricSource = "In-House",
            fabricType = "",
            colorTone = "",
            pattern = "Solid",
            models = emptyList()
        )
        editingGarmentIndex = null
        showGarmentDialog = true
    }

    // ── Open dialog for editing existing garment ──
    fun editGarmentDialog(index: Int) {
        tempGarment = selectedGarments[index]
        editingGarmentIndex = index
        showGarmentDialog = true
    }

    // ── Save garment from dialog ──
    fun saveGarment() {
        if (editingGarmentIndex != null) {
            selectedGarments[editingGarmentIndex!!] = tempGarment
        } else {
            selectedGarments.add(tempGarment)
        }
        showGarmentDialog = false
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier
                        .size(22.dp)
                        .clickable { onBack() },
                    tint = Color(0xFF111827)
                )
                Text(
                    "Create Order",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
            }
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
                ) {
                    Text("Cancel", color = Color(0xFF374151), fontWeight = FontWeight.Medium)
                }
                Button(
                    onClick = onNextStep,
                    modifier = Modifier.weight(2f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B3BF9)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Next Step", color = Color.White, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.Default.ChevronRight, null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        },
        containerColor = Color(0xFFF3F4F6)
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(scrollState)
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ══════════════════════════════════════════════
                // 1. CUSTOMER DETAILS
                // ══════════════════════════════════════════════
                SectionCard(title = "CUSTOMER DETAILS") {

                    // Phone
                    FormLabel("Phone")
                    PhoneInputField(
                        phoneValue = phone,
                        onPhoneChange = { phone = it },
                        onCountryChange = { /* TODO: handle country change */ }
                    )

                    Spacer(Modifier.height(4.dp))

                    // Full Name
                    FormLabel("Full Name")
                    FormTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        placeholder = "Enter Customer Name"
                    )

                    Spacer(Modifier.height(4.dp))

                    // Address
                    FormLabel("Address")
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        placeholder = {
                            Text("Enter full billing/shipping address...", color = Color(0xFF9CA3AF), fontSize = 14.sp)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFFE5E7EB),
                            focusedBorderColor = Color(0xFF3B3BF9),
                            unfocusedContainerColor = Color(0xFFF9FAFB),
                            focusedContainerColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
                    )

                    Spacer(Modifier.height(4.dp))

                    // Gender
                    FormLabel("Gender")
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        listOf("Male", "Female", "Other").forEach { option ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.clickable { gender = option }
                            ) {
                                RadioButton(
                                    selected = gender == option,
                                    onClick = { gender = option },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = Color(0xFF3B3BF9)
                                    )
                                )
                                Text(option, fontSize = 14.sp, color = Color(0xFF374151))
                            }
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    // Dress For
                    FormLabel("Dress For")
                    Box {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
                                .clickable { showDressDropdown = true }
                                .padding(horizontal = 14.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(dressFor, fontSize = 14.sp, color = Color(0xFF111827))
                            Icon(Icons.Default.KeyboardArrowDown, null, tint = Color(0xFF6B7280))
                        }
                        DropdownMenu(
                            expanded = showDressDropdown,
                            onDismissRequest = { showDressDropdown = false },
                            containerColor = Color.White
                        ) {
                            listOf("Men", "Women", "Kids", "Unisex").forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt) },
                                    onClick = { dressFor = opt; showDressDropdown = false }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    // Source
                    FormLabel("Source")
                    Box {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
                                .clickable { showSourceDropdown = true }
                                .padding(horizontal = 14.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(source, fontSize = 14.sp, color = Color(0xFF111827))
                            Icon(Icons.Default.KeyboardArrowDown, null, tint = Color(0xFF6B7280))
                        }
                        DropdownMenu(
                            expanded = showSourceDropdown,
                            onDismissRequest = { showSourceDropdown = false },
                            containerColor = Color.White
                        ) {
                            listOf("Walk-in", "Phone", "WhatsApp", "Referral", "Online").forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt) },
                                    onClick = { source = opt; showSourceDropdown = false }
                                )
                            }
                        }
                    }
                }

                // ══════════════════════════════════════════════
                // 2. GARMENT DETAILS
                // ══════════════════════════════════════════════
                SectionCard(
                    title = "GARMENT DETAILS",
                    action = {
                        Row(
                            modifier = Modifier.clickable {
                                // Open a category picker or just add a new garment
                                openGarmentDialog("", "")
                            },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(Icons.Default.Add, null, tint = Color(0xFF3B3BF9), modifier = Modifier.size(16.dp))
                            Text("Add Category", fontSize = 13.sp, color = Color(0xFF3B3BF9), fontWeight = FontWeight.SemiBold)
                        }
                    }
                ) {
                    // Quick Add
                    Text(
                        "QUICK ADD CATEGORY",
                        fontSize = 11.sp,
                        color = Color(0xFF9CA3AF),
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(quickCategories) { (name, _) ->
                            Column(
                                modifier = Modifier
                                    .width(90.dp)
                                    .background(Color.White, RoundedCornerShape(10.dp))
                                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(10.dp))
                                    .clickable {
                                        openGarmentDialog(name, name.lowercase())
                                    }
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(Color(0xFFEEF2FF), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Checkroom,
                                        contentDescription = name,
                                        tint = Color(0xFF3B3BF9),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Text(name, fontSize = 12.sp, color = Color(0xFF374151), fontWeight = FontWeight.Medium)
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Selected Garments
                    Text(
                        "SELECTED GARMENTS",
                        fontSize = 11.sp,
                        color = Color(0xFF9CA3AF),
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(Modifier.height(8.dp))

                    if (selectedGarments.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .background(Color(0xFFF9FAFB), RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No garments added yet.", fontSize = 14.sp, color = Color(0xFF9CA3AF))
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            selectedGarments.forEachIndexed { index, garment ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFF9FAFB), RoundedCornerShape(8.dp))
                                        .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
                                        .clickable { editGarmentDialog(index) }
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(Color(0xFFEEF2FF), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Checkroom, null, tint = Color(0xFF3B3BF9), modifier = Modifier.size(18.dp))
                                        }
                                        Column {
                                            Text(
                                                garment.categoryName,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF111827)
                                            )
                                            Text(
                                                "Qty: ${garment.quantity}",
                                                fontSize = 12.sp,
                                                color = Color(0xFF6B7280)
                                            )
                                        }
                                    }
                                    Icon(
                                        Icons.Default.Edit,
                                        null,
                                        tint = Color(0xFF3B3BF9),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // ══════════════════════════════════════════════
                // 3. DELIVERY DETAILS
                // ══════════════════════════════════════════════
                SectionCard(title = "DELIVERY DETAILS") {

                    FormLabel("Order Date")
                    DateField(value = orderDate, placeholder = "dd-mm-yyyy", onClick = { /* date picker */ })

                    Spacer(Modifier.height(4.dp))
                    FormLabel("Trial Date")
                    DateField(value = trialDate, placeholder = "Select Trial Date", onClick = { /* date picker */ })

                    Spacer(Modifier.height(4.dp))
                    FormLabel("Target Delivery Date")
                    DateField(value = deliveryDate, placeholder = "Select Delivery Date", onClick = { /* date picker */ })

                    Spacer(Modifier.height(4.dp))
                    FormLabel("Assigned Branch")
                    Box {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
                                .clickable { showBranchDropdown = true }
                                .padding(horizontal = 14.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                if (branch.isEmpty()) "Select Branch" else branch,
                                fontSize = 14.sp,
                                color = if (branch.isEmpty()) Color(0xFF9CA3AF) else Color(0xFF111827)
                            )
                            Icon(Icons.Default.KeyboardArrowDown, null, tint = Color(0xFF6B7280))
                        }
                        DropdownMenu(
                            expanded = showBranchDropdown,
                            onDismissRequest = { showBranchDropdown = false },
                            containerColor = Color.White
                        ) {
                            listOf("Main Branch", "Branch 1", "Branch 2").forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt) },
                                    onClick = { branch = opt; showBranchDropdown = false }
                                )
                            }
                        }
                    }
                }

                // ══════════════════════════════════════════════
                // 4. DESIGN REFERENCE
                // ══════════════════════════════════════════════
                SectionCard(title = "DESIGN REFERENCE") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .background(Color(0xFFF9FAFB), RoundedCornerShape(8.dp))
                            .border(
                                width = 1.dp,
                                color = Color(0xFFD1D5DB),
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { /* browse files */ },
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD1D5DB)),
                                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
                            ) {
                                Text("Browse Files", fontSize = 13.sp, color = Color(0xFF374151))
                            }
                            OutlinedButton(
                                onClick = { /* camera */ },
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD1D5DB)),
                                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
                            ) {
                                Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(14.dp), tint = Color(0xFF374151))
                                Spacer(Modifier.width(4.dp))
                                Text("Camera", fontSize = 13.sp, color = Color(0xFF374151))
                            }
                        }
                    }
                }

                // ══════════════════════════════════════════════
                // 5. INSTRUCTIONS
                // ══════════════════════════════════════════════
                SectionCard(title = "INSTRUCTIONS") {

                    FormLabel("Styling Notes")
                    OutlinedTextField(
                        value = stylingNotes,
                        onValueChange = { stylingNotes = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        placeholder = {
                            Text("Special requirements, cutting instructions...", color = Color(0xFF9CA3AF), fontSize = 14.sp)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFFE5E7EB),
                            focusedBorderColor = Color(0xFF3B3BF9),
                            unfocusedContainerColor = Color(0xFFF9FAFB),
                            focusedContainerColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
                    )
                }

                // ══════════════════════════════════════════════
                // 6. VOICE INSTRUCTIONS
                // ══════════════════════════════════════════════
                SectionCard(title = "VOICE INSTRUCTIONS") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF9FAFB), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(
                            onClick = { isRecording = !isRecording },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isRecording) Color(0xFFEF4444) else Color(0xFFEEF2FF)
                            ),
                            shape = RoundedCornerShape(20.dp),
                            elevation = ButtonDefaults.buttonElevation(0.dp)
                        ) {
                            Icon(
                                if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                                null,
                                tint = if (isRecording) Color.White else Color(0xFF3B3BF9),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                if (isRecording) "Stop Recording" else "Start Recording",
                                color = if (isRecording) Color.White else Color(0xFF3B3BF9),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // ─────────────────────────────────────────────────────────
            // GARMENT DETAIL DIALOG
            // ─────────────────────────────────────────────────────────
            if (showGarmentDialog) {
                GarmentDetailDialog(
                    garment = tempGarment,
                    onGarmentChange = { tempGarment = it },
                    onSave = { saveGarment() },
                    onDismiss = { showGarmentDialog = false }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// GARMENT DETAIL DIALOG
// ─────────────────────────────────────────────────────────────

// ─────────────────────────────────────────────────────────────
// GARMENT DETAIL DIALOG - FIXED FilterChip usage
// ─────────────────────────────────────────────────────────────

@Composable
fun GarmentDetailDialog(
    garment: SelectedGarment,
    onGarmentChange: (SelectedGarment) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    val priorityOptions = listOf("Low", "Medium", "High", "Urgent")
    val fabricSourceOptions = listOf("In-House", "Client")
    val patternOptions = listOf("Solid", "Striped", "Checked", "Printed", "Plain")

    val availableModels = listOf(
        GarmentModel("1", "Ankle Fit"),
        GarmentModel("2", "Mom Fit"),
        GarmentModel("3", "Slim Fit"),
        GarmentModel("4", "Regular Fit"),
        GarmentModel("5", "Relaxed Fit"),
        GarmentModel("6", "Skinny Fit")
    )

    var selectedModels by remember {
        mutableStateOf(garment.models.toMutableList())
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(horizontal = 12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── Header ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Add New Garment",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827)
                        )
                        Text(
                            "CONFIGURE DETAILS",
                            fontSize = 11.sp,
                            color = Color(0xFF9CA3AF),
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.8.sp
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Close, null, tint = Color(0xFF9CA3AF))
                    }
                }

                HorizontalDivider(color = Color(0xFFE5E7EB))

                // ── BASIC INFORMATION ──
                Text(
                    "BASIC INFORMATION",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827),
                    letterSpacing = 0.5.sp
                )

                // Garment Type
                FormLabel("Garment Type")
                GarmentTypeSelector(
                    garmentName = garment.categoryName,
                    onGarmentTypeChange = { newName ->
                        onGarmentChange(garment.copy(categoryName = newName))
                    }
                )

                // Quantity
                FormLabel("Quantity")
                QuantitySelector(
                    quantity = garment.quantity,
                    onQuantityChange = { newQty ->
                        onGarmentChange(garment.copy(quantity = newQty))
                    }
                )

                // Priority
                FormLabel("Priority")
                PrioritySelector(
                    selectedPriority = garment.priority,
                    options = priorityOptions,
                    onPriorityChange = { newPriority ->
                        onGarmentChange(garment.copy(priority = newPriority))
                    }
                )

                // Trial Required
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Switch(
                        checked = garment.trialRequired,
                        onCheckedChange = { isChecked ->
                            onGarmentChange(garment.copy(trialRequired = isChecked))
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF3B3BF9),
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0xFFD1D5DB)
                        )
                    )
                    Column {
                        Text(
                            "Trial Required",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF374151)
                        )
                        Text(
                            "Schedule fitting?",
                            fontSize = 12.sp,
                            color = Color(0xFF9CA3AF)
                        )
                    }
                }

                HorizontalDivider(color = Color(0xFFE5E7EB))

                // ── FABRIC DETAILS ──
                Text(
                    "FABRIC DETAILS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827),
                    letterSpacing = 0.5.sp
                )

                // Fabric Source
                FormLabel("Fabric Source")
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    fabricSourceOptions.forEach { option ->
                        FilterChip(
                            selected = garment.fabricSource == option,
                            onClick = {
                                onGarmentChange(garment.copy(fabricSource = option))
                            },
                            label = { Text(option, fontSize = 13.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFEEF2FF),
                                selectedLabelColor = Color(0xFF3B3BF9)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = garment.fabricSource == option,
                                borderColor = Color(0xFFE5E7EB),
                                selectedBorderColor = Color(0xFF3B3BF9)
                            )
                        )
                    }
                }

                // Fabric Type
                FormLabel("Fabric Type")
                FormTextField(
                    value = garment.fabricType,
                    onValueChange = { newValue ->
                        onGarmentChange(garment.copy(fabricType = newValue))
                    },
                    placeholder = "e.g. Cotton"
                )

                // Color / Tone
                FormLabel("Color / Tone")
                FormTextField(
                    value = garment.colorTone,
                    onValueChange = { newValue ->
                        onGarmentChange(garment.copy(colorTone = newValue))
                    },
                    placeholder = "Color name"
                )

                // Pattern
                FormLabel("Pattern")
                PatternSelector(
                    selectedPattern = garment.pattern,
                    options = patternOptions,
                    onPatternChange = { newPattern ->
                        onGarmentChange(garment.copy(pattern = newPattern))
                    }
                )

                HorizontalDivider(color = Color(0xFFE5E7EB))

                // ── MODELS ──
                Text(
                    "MODELS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827),
                    letterSpacing = 0.5.sp
                )

                // Model Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(availableModels) { model ->
                        FilterChip(
                            selected = selectedModels.contains(model.name),
                            onClick = {
                                if (selectedModels.contains(model.name)) {
                                    selectedModels.remove(model.name)
                                } else {
                                    selectedModels.add(model.name)
                                }
                                onGarmentChange(garment.copy(models = selectedModels.toList()))
                            },
                            label = { Text(model.name, fontSize = 13.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFEEF2FF),
                                selectedLabelColor = Color(0xFF3B3BF9)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedModels.contains(model.name),
                                borderColor = Color(0xFFE5E7EB),
                                selectedBorderColor = Color(0xFF3B3BF9)
                            )
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // ── Action Buttons ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
                    ) {
                        Text("Cancel", color = Color(0xFF374151), fontWeight = FontWeight.Medium)
                    }
                    Button(
                        onClick = onSave,
                        modifier = Modifier.weight(2f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B3BF9)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Apply", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// FIXED Pattern Selector
// ─────────────────────────────────────────────────────────────

@Composable
private fun PatternSelector(
    selectedPattern: String,
    options: List<String>,
    onPatternChange: (String) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option ->
            val isSelected = selectedPattern == option
            FilterChip(
                selected = isSelected,
                onClick = { onPatternChange(option) },
                label = { Text(option, fontSize = 13.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFEEF2FF),
                    selectedLabelColor = Color(0xFF3B3BF9)
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = if (isSelected) Color(0xFF3B3BF9) else Color(0xFFE5E7EB),
                    selectedBorderColor = Color(0xFF3B3BF9)
                )
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Reusable Components
// ─────────────────────────────────────────────────────────────

@Composable
private fun SectionCard(
    title: String,
    action: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827),
                letterSpacing = 0.8.sp
            )
            action?.invoke()
        }
        Spacer(Modifier.height(16.dp))
        content()
    }
}

@Composable
private fun FormLabel(text: String) {
    Text(
        text,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color(0xFF111827),
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
private fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, color = Color(0xFF9CA3AF), fontSize = 14.sp) },
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = Color(0xFFE5E7EB),
            focusedBorderColor = Color(0xFF3B3BF9),
            unfocusedContainerColor = Color(0xFFF9FAFB),
            focusedContainerColor = Color.White
        ),
        shape = RoundedCornerShape(8.dp),
        singleLine = true,
        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
    )
}

@Composable
private fun DateField(
    value: String,
    placeholder: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            if (value.isEmpty()) placeholder else value,
            fontSize = 14.sp,
            color = if (value.isEmpty()) Color(0xFF9CA3AF) else Color(0xFF111827)
        )
        Icon(Icons.Default.CalendarMonth, null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun GarmentTypeSelector(
    garmentName: String,
    onGarmentTypeChange: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
            .padding(horizontal = 14.dp, vertical = 14.dp)
    ) {
        Text(
            if (garmentName.isNotEmpty()) garmentName else "Select Garment Type",
            fontSize = 14.sp,
            color = if (garmentName.isNotEmpty()) Color(0xFF111827) else Color(0xFF9CA3AF)
        )
    }
}

@Composable
private fun QuantitySelector(
    quantity: Int,
    onQuantityChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { if (quantity > 1) onQuantityChange(quantity - 1) },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(Icons.Default.Remove, null, tint = Color(0xFF6B7280))
        }
        Text(
            quantity.toString(),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF111827),
            modifier = Modifier.width(32.dp),
            textAlign = TextAlign.Center
        )
        IconButton(
            onClick = { onQuantityChange(quantity + 1) },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(Icons.Default.Add, null, tint = Color(0xFF3B3BF9))
        }
    }
}

@Composable
private fun PrioritySelector(
    selectedPriority: String,
    options: List<String>,
    onPriorityChange: (String) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option ->
            val isSelected = selectedPriority == option
            val color = when (option.lowercase()) {
                "urgent" -> Color(0xFFEF4444)
                "high" -> Color(0xFFFF9800)
                "medium" -> Color(0xFFFFC107)
                else -> Color(0xFF9CA3AF)
            }
            FilterChip(
                selected = isSelected,
                onClick = { onPriorityChange(option) },
                label = { Text(option, fontSize = 13.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = color.copy(alpha = 0.15f),
                    selectedLabelColor = color,
                    disabledContainerColor = Color(0xFFF3F4F6),
                    disabledLabelColor = Color(0xFF9CA3AF)
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = if (isSelected) color else Color(0xFFE5E7EB),
                    selectedBorderColor = color
                )
            )
        }
    }
}

