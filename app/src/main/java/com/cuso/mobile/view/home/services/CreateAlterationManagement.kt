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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
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
import com.cuso.mobile.ui.theme.Primary
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.view.home.reusablecomposables.SmoothBottomSheet
import com.cuso.mobile.view.home.reusablecomposables.SheetValue
import com.cuso.mobile.view.home.reusablecomposables.blurScrim

// ─────────────────────────────────────────────
// Shared colors used across all screens
// ─────────────────────────────────────────────
private val Accent = Color(0xFF3D3DFF)
private val AccentBg = Color(0xFFEDEDFB)
private val TitleColor = Color(0xFF111827)
private val LabelColor = Color(0xFF6B7280)
private val MutedLabel = Color(0xFF9CA3AF)
private val BorderColor = Color(0xFFE5E7EB)
private val StripBg = Color(0xFFF3F4F6)
private val SectionBg = Color(0xFFF7F7FA)
private val SuccessColor = Color(0xFF16A34A)
private val PriorityRed = Color(0xFFE53935)
private val StepGreen = Color(0xFF16A34A)
private val StepGrayBg = Color(0xFFE5E7EB)
private val StepGrayText = Color(0xFF9CA3AF)
private val NavyFabric = Color(0xFF1E293B)
private val NotesBg = Color(0xFFEDEDFB)
private val WipBadgeBg = Color(0xFF3D3DFF)
private val BeforeBadgeBg = Color(0xFF6B7280)

// ─────────────────────────────────────────────
// Step State and Data Classes
// ─────────────────────────────────────────────
private data class Step(val label: String, val state: StepState)
private enum class StepState { DONE, CURRENT, UPCOMING }
private data class Measurement(val point: String, val original: String, val altered: String, val diff: String, val alteredHighlighted: Boolean = false)

// ─────────────────────────────────────────────
// MAIN SCREEN
// ─────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAlterationManagementScreen(
    onClose: () -> Unit
) {
    var updateStatusSheetState by remember { mutableStateOf(SheetValue.Hidden) }
    var assignTailorSheetState by remember { mutableStateOf(SheetValue.Hidden) }

    var updateStatusSheetBlur by remember { mutableStateOf(0.dp) }
    var assignTailorSheetBlur by remember { mutableStateOf(0.dp) }

    var statusNotes by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf("In Progress") }

    var targetDate by remember { mutableStateOf("") }
    var staffInstructions by remember { mutableStateOf("") }

    var comment by remember { mutableStateOf("") }

    val currentBlur = when {
        updateStatusSheetState != SheetValue.Hidden -> updateStatusSheetBlur
        assignTailorSheetState != SheetValue.Hidden -> assignTailorSheetBlur
        else -> 0.dp
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                Surface(modifier = Modifier.fillMaxWidth(), color = whiteBg) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            TitleBar("Alteration Management", onClose = onClose)

                        }


                    }
                }
            },
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            containerColor = Color.Transparent
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .background(Color.Transparent)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .blurScrim(currentBlur)
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Transparent)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row {
                            Text("ALT-882", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TitleColor)
                            Text(" / SO-450", fontSize = 13.sp, color = MutedLabel)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(AccentBg)
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text("In Progress", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Accent)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(40.dp)) {
                        Column {
                            Text("Customer", fontSize = 11.sp, color = MutedLabel)
                            Text("Liam Henderson", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TitleColor)
                        }
                        Column {
                            Text("Priority", fontSize = 11.sp, color = MutedLabel)
                            Text("High Priority", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = PriorityRed)
                        }
                    }
                }

                OutlinedButton(
                    onClick = { assignTailorSheetState = SheetValue.Collapsed },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Accent),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Assign Tailor", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(Modifier.height(20.dp))

                AlterationStepper()

                Spacer(Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Current Status: ", fontSize = 13.sp, color = LabelColor)
                    Text("In Progress", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Accent)
                }
                Spacer(Modifier.height(10.dp))

                Button(
                    onClick = { updateStatusSheetState = SheetValue.Collapsed },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text("Update Status", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = whiteBg)
                }

                Spacer(Modifier.height(24.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Inventory2, contentDescription = null, tint = Accent, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Garment & Alteration Details", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TitleColor)
                }
                Spacer(Modifier.height(14.dp))

                Text("Garment Type", fontSize = 11.sp, color = MutedLabel)
                Spacer(Modifier.height(2.dp))
                Text("Bespoke Blazer", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TitleColor)
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = BorderColor)
                Spacer(Modifier.height(12.dp))

                Text("Fabric", fontSize = 11.sp, color = MutedLabel)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(NavyFabric)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Navy Wool (Super 120s)", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TitleColor)
                }
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = BorderColor)
                Spacer(Modifier.height(12.dp))

                Text("Alteration Notes", fontSize = 11.sp, color = MutedLabel)
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(NotesBg)
                        .padding(12.dp)
                ) {
                    Text(
                        "\"Shorten sleeves by 1.25 inches. Ensure the functional buttonholes are preserved and spaced correctly from the new edge.\"",
                        fontSize = 13.sp,
                        color = TitleColor
                    )
                }

                Spacer(Modifier.height(24.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Straighten, contentDescription = null, tint = Accent, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Measurement Adjustments", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TitleColor)
                }
                Spacer(Modifier.height(14.dp))

                MeasurementTable()

                Spacer(Modifier.height(24.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = Accent, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Trial & Documentation", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TitleColor)
                }
                Spacer(Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MutedLabel, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Column {
                            Text("Trial Date", fontSize = 11.sp, color = MutedLabel)
                            Text("Oct 24, 2023 - 2:00 PM", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TitleColor)
                        }
                    }
                    OutlinedButton(
                        onClick = { /* TODO: change trial date */ },
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Accent),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text("Change", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                Spacer(Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PhotoPlaceholder(label = "BEFORE", badgeColor = BeforeBadgeBg, modifier = Modifier.weight(1f))
                    PhotoPlaceholder(label = "WIP", badgeColor = WipBadgeBg, modifier = Modifier.weight(1f))
                }

                Spacer(Modifier.height(24.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Accent, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Assigned Tailor", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TitleColor)
                }
                Spacer(Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(AccentBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("AR", color = Accent, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Antonio Rossi", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TitleColor)
                        Row {
                            Text("Senior Tailor  •  ", fontSize = 12.sp, color = LabelColor)
                            Text("Suiting Expert", fontSize = 12.sp, color = Accent)
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFefeff8))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Active Jobs", fontSize = 12.sp, color = MutedLabel)
                        Text("4", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TitleColor)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Success Rate", fontSize = 12.sp, color = MutedLabel)
                        Text("98%", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SuccessColor)
                    }
                }
                Spacer(Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { /* TODO: navigate to tailor profile */ },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Accent),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary)
                ) {
                    Text("View Profile & Capacity", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(Modifier.height(24.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.History, contentDescription = null, tint = Accent, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Activity Log & Notes", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TitleColor)
                }
                Spacer(Modifier.height(16.dp))

                val activities = listOf(
                    Triple("Alteration Started", "\"Measurements verified, starting sleeve shortening.\"", "Antonio Rossi • 2h ago"),
                    Triple("Assigned to Antonio Rossi", null, "System • 5h ago"),
                    Triple("Service Request Received", null, "System • Oct 20, 10:45 AM")
                )

                activities.forEachIndexed { index, (title, note, meta) ->
                    Row {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (index == 0) Accent else Color(0xFFD1D5DB))
                            )
                            if (index != activities.lastIndex) {
                                Box(
                                    modifier = Modifier
                                        .width(2.dp)
                                        .height(48.dp)
                                        .background(BorderColor)
                                )
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.padding(bottom = 16.dp)) {
                            Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TitleColor)
                            note?.let {
                                Spacer(Modifier.height(2.dp))
                                Text(it, fontSize = 12.sp, color = LabelColor)
                            }
                            Spacer(Modifier.height(2.dp))
                            Text(meta, fontSize = 11.sp, color = MutedLabel)
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))

                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    placeholder = { Text("Add a comment or update...", fontSize = 13.sp, color = MutedLabel) },
                    shape = RoundedCornerShape(10.dp),
                    trailingIcon = {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = Accent,
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .clickable { comment = "" }
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = BorderColor,
                        focusedBorderColor = Accent,
                        unfocusedContainerColor = whiteBg
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row {
                        Icon(Icons.Default.AttachFile, contentDescription = null, tint = MutedLabel, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.EmojiEmotions, contentDescription = null, tint = MutedLabel, modifier = Modifier.size(16.dp))
                    }
                    Text("Press Enter to send", fontSize = 11.sp, color = MutedLabel)
                }

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Accent)
                ) {
                    Text("Mark as Completed", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = whiteBg)
                }
                Spacer(Modifier.height(20.dp))
            }
        }

        // ─────────────────────────────────────────────
        // UPDATE STATUS bottom sheet
        // ─────────────────────────────────────────────
        SmoothBottomSheet(
            state = updateStatusSheetState,
            onStateChange = { updateStatusSheetState = it },
            peekHeight = 480.dp,
            onDismissRequest = { updateStatusSheetState = SheetValue.Hidden },
            onBlurScrimChange = { blur, _ ->
                updateStatusSheetBlur = blur
            }
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
                Text(
                    "UPDATE STATUS",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TitleColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Text("ALT-882", fontSize = 12.sp, color = MutedLabel)
                    Text(" / SO-450", fontSize = 12.sp, color = MutedLabel)
                }
                Spacer(Modifier.height(20.dp))

                Text("Alteration Status", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TitleColor)
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                        .clickable { /* TODO: open status dropdown */ }
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(selectedStatus, fontSize = 14.sp, color = TitleColor)
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = LabelColor)
                }
                Spacer(Modifier.height(16.dp))

                Text("Notes", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TitleColor)
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = statusNotes,
                    onValueChange = { statusNotes = it },
                    placeholder = { Text("Add an optional note...", fontSize = 13.sp, color = MutedLabel) },
                    minLines = 3,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = BorderColor,
                        focusedBorderColor = Accent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = { updateStatusSheetState = SheetValue.Hidden },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Accent)
                ) {
                    Text("Update Status", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = whiteBg)
                }
                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick = { updateStatusSheetState = SheetValue.Hidden },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, BorderColor),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TitleColor)
                ) {
                    Text("Cancel", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(16.dp))

                Text(
                    "LAST UPDATED 2D AGO BY J. DOE",
                    fontSize = 10.sp,
                    color = MutedLabel,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(20.dp))
            }
        }

        // ─────────────────────────────────────────────
        // ASSIGN TAILOR bottom sheet
        // ─────────────────────────────────────────────
        SmoothBottomSheet(
            state = assignTailorSheetState,
            onStateChange = { assignTailorSheetState = it },
            peekHeight = 600.dp,
            onDismissRequest = { assignTailorSheetState = SheetValue.Hidden },
            onBlurScrimChange = { blur, _ ->
                assignTailorSheetBlur = blur
            }
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
                Text(
                    "ASSIGN TAILOR",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TitleColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Text("ALT-882", fontSize = 12.sp, color = MutedLabel)
                    Text(" / SO-450", fontSize = 12.sp, color = MutedLabel)
                }
                Spacer(Modifier.height(20.dp))

                Text("Select Tailor", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TitleColor)
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                        .clickable { /* TODO: open tailor dropdown */ }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE5E7EB))
                        )
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("Alessandro Ricci", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TitleColor)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Workload: 2 Active Jobs  ", fontSize = 11.sp, color = MutedLabel)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFFE7F8EE))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("Available", fontSize = 10.sp, color = SuccessColor, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = LabelColor)
                }
                Spacer(Modifier.height(16.dp))

                Text("Target Completion Date", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TitleColor)
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = targetDate,
                    onValueChange = { targetDate = it },
                    placeholder = { Text("MM/DD/YYYY", fontSize = 13.sp, color = MutedLabel) },
                    trailingIcon = {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = LabelColor, modifier = Modifier.size(18.dp))
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = BorderColor,
                        focusedBorderColor = Accent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Tailor Weekly Capacity", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Accent)
                    Text("65% Full", fontSize = 12.sp, color = LabelColor)
                }
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFFEDEDED))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.65f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(3.dp))
                            .background(Accent)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "Ricci typically completes 8 alterations per week. Current queue: 5.",
                    fontSize = 11.sp,
                    color = LabelColor
                )
                Spacer(Modifier.height(16.dp))

                Text("Instruction for Staff", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TitleColor)
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = staffInstructions,
                    onValueChange = { staffInstructions = it },
                    placeholder = { Text("Add an optional note...", fontSize = 13.sp, color = MutedLabel) },
                    minLines = 3,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = BorderColor,
                        focusedBorderColor = Accent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { assignTailorSheetState = SheetValue.Hidden },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, BorderColor),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TitleColor)
                    ) {
                        Text("Cancel", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = { assignTailorSheetState = SheetValue.Hidden },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Text("Assign Staff", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = whiteBg)
                    }
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────
// Progress stepper
// ─────────────────────────────────────────────
@Composable
private fun AlterationStepper() {
    val steps = listOf(
        Step("Requested", StepState.DONE),
        Step("Assigned", StepState.DONE),
        Step("In Progress", StepState.CURRENT),
        Step("Trial Ready", StepState.UPCOMING),
        Step("Completed", StepState.UPCOMING)
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, step ->
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    if (index != 0) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(2.dp)
                                .background(if (step.state != StepState.UPCOMING || steps[index - 1].state == StepState.DONE) StepGreen.copy(alpha = if (steps[index - 1].state == StepState.DONE) 1f else 0f) else StepGrayBg)
                        )
                    } else {
                        Spacer(Modifier.weight(1f))
                    }

                    StepCircle(index = index, step = step)

                    if (index != steps.lastIndex) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(2.dp)
                                .background(if (step.state == StepState.DONE) StepGreen else StepGrayBg)
                        )
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    step.label,
                    fontSize = 11.sp,
                    fontWeight = if (step.state == StepState.CURRENT) FontWeight.Bold else FontWeight.Medium,
                    color = when (step.state) {
                        StepState.DONE -> StepGreen
                        StepState.CURRENT -> Accent
                        StepState.UPCOMING -> StepGrayText
                    },
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun StepCircle(index: Int, step: Step) {
    when (step.state) {
        StepState.DONE -> {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(StepGreen),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = whiteBg, modifier = Modifier.size(14.dp))
            }
        }
        StepState.CURRENT -> {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(whiteBg)
                    .border(2.dp, Accent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Accent)
                )
            }
        }
        StepState.UPCOMING -> {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(StepGrayBg),
                contentAlignment = Alignment.Center
            ) {
                Text((index + 1).toString(), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = StepGrayText)
            }
        }
    }
}

// ─────────────────────────────────────────────
// Measurement Adjustments table
// ─────────────────────────────────────────────
@Composable
private fun MeasurementTable() {
    val rows = listOf(
        Measurement("Chest", "42.0\"", "41.5\"", "-0.5\""),
        Measurement("Waist", "36.0\"", "35.2\"", "-0.8\""),
        Measurement("Sleeve Length", "25.5\"", "24.25\"", "-1.25\"", alteredHighlighted = true)
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text("Point", fontSize = 12.sp, color = MutedLabel, modifier = Modifier.weight(1.4f))
            Text("Original", fontSize = 12.sp, color = MutedLabel, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
            Text("Altered", fontSize = 12.sp, color = MutedLabel, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
            Text("Diff", fontSize = 12.sp, color = MutedLabel, modifier = Modifier.weight(0.8f), textAlign = TextAlign.End)
        }
        Spacer(Modifier.height(10.dp))
        HorizontalDivider(color = BorderColor)

        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(row.point, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TitleColor, modifier = Modifier.weight(1.4f))
                Text(row.original, fontSize = 13.sp, color = TitleColor, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                Text(
                    row.altered,
                    fontSize = 13.sp,
                    fontWeight = if (row.alteredHighlighted) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (row.alteredHighlighted) Accent else TitleColor,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End
                )
                Text(row.diff, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = PriorityRed, modifier = Modifier.weight(0.8f), textAlign = TextAlign.End)
            }
            HorizontalDivider(color = BorderColor)
        }
    }
}

// ─────────────────────────────────────────────
// Before/WIP photo placeholder box
// ─────────────────────────────────────────────
@Composable
private fun PhotoPlaceholder(label: String, badgeColor: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(120.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(SectionBg)
            .border(1.dp, BorderColor, RoundedCornerShape(10.dp))
    ) {
        Box(
            modifier = Modifier
                .padding(8.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(badgeColor)
                .padding(horizontal = 10.dp, vertical = 3.dp)
        ) {
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = whiteBg)
        }
    }
}