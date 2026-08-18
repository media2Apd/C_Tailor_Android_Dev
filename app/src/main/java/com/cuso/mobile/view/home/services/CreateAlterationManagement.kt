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
import com.cuso.mobile.ui.theme.Primary
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.view.composable.SmoothBottomSheet
import com.cuso.mobile.view.composable.SheetValue
import com.cuso.mobile.view.composable.blurScrim
// ── NEW: adaptive design tokens ──
import com.cuso.mobile.adaptive_screen.AppDesignTokens
import com.cuso.mobile.adaptive_screen.LocalAppTokens

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
    // ── Adaptive tokens ──
    val tokens: AppDesignTokens = LocalAppTokens.current
    val sectionGap = tokens.screenPadding
    val fieldGap = tokens.screenPadding * 0.75f
    val smallGap = tokens.screenPadding * 0.5f
    val tinyGap = tokens.screenPadding * 0.3f
    val adaptiveCorner = RoundedCornerShape(tokens.cardCornerRadius * 0.5f)

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
                    .padding(horizontal = sectionGap, vertical = fieldGap)
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
                            Text(
                                "ALT-882",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = TitleColor
                            )
                            Text(" / SO-450", fontSize = tokens.bodySmall, color = MutedLabel)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(AccentBg)
                                .padding(horizontal = smallGap, vertical = tinyGap)
                        ) {
                            Text(
                                "In Progress",
                                fontSize = tokens.caption,
                                fontWeight = FontWeight.Medium,
                                color = Accent
                            )
                        }
                    }
                    Spacer(Modifier.height(smallGap))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(sectionGap * 2.5f)
                    ) {
                        Column {
                            Text("Customer", fontSize = tokens.caption, color = MutedLabel)
                            Text(
                                "Liam Henderson",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = TitleColor
                            )
                        }
                        Column {
                            Text("Priority", fontSize = tokens.caption, color = MutedLabel)
                            Text(
                                "High Priority",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = PriorityRed
                            )
                        }
                    }
                }

                OutlinedButton(
                    onClick = { assignTailorSheetState = SheetValue.Collapsed },
                    modifier = Modifier.fillMaxWidth().height(tokens.buttonHeight),
                    shape = adaptiveCorner,
                    border = BorderStroke(1.dp, Accent),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(tokens.iconSize)
                    )
                    Spacer(Modifier.width(smallGap * 0.75f))
                    Text("Assign Tailor", fontSize = tokens.bodyMedium, fontWeight = FontWeight.SemiBold)
                }

                Spacer(Modifier.height(sectionGap))

                AlterationStepper()

                Spacer(Modifier.height(fieldGap))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Current Status: ", fontSize = tokens.bodySmall, color = LabelColor)
                    Text(
                        "In Progress",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Accent
                    )
                }
                Spacer(Modifier.height(smallGap))

                Button(
                    onClick = { updateStatusSheetState = SheetValue.Collapsed },
                    modifier = Modifier.fillMaxWidth().height(tokens.buttonHeight),
                    shape = adaptiveCorner,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text(
                        "Update Status",
                        fontSize = tokens.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = whiteBg
                    )
                }

                Spacer(Modifier.height(sectionGap * 1.5f))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Inventory2,
                        contentDescription = null,
                        tint = Accent,
                        modifier = Modifier.size(tokens.iconSize)
                    )
                    Spacer(Modifier.width(smallGap))
                    Text(
                        "Garment & Alteration Details",
                        fontSize = tokens.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = TitleColor
                    )
                }
                Spacer(Modifier.height(fieldGap))

                Text("Garment Type", fontSize = tokens.caption, color = MutedLabel)
                Spacer(Modifier.height(tinyGap * 0.6f))
                Text(
                    "Bespoke Blazer",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = TitleColor
                )
                Spacer(Modifier.height(smallGap))
                HorizontalDivider(color = BorderColor)
                Spacer(Modifier.height(smallGap))

                Text("Fabric", fontSize = tokens.caption, color = MutedLabel)
                Spacer(Modifier.height(tinyGap))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(NavyFabric)
                    )
                    Spacer(Modifier.width(smallGap))
                    Text(
                        "Navy Wool (Super 120s)",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = TitleColor
                    )
                }
                Spacer(Modifier.height(smallGap))
                HorizontalDivider(color = BorderColor)
                Spacer(Modifier.height(smallGap))

                Text("Alteration Notes", fontSize = tokens.caption, color = MutedLabel)
                Spacer(Modifier.height(tinyGap))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(adaptiveCorner)
                        .background(NotesBg)
                        .padding(smallGap)
                ) {
                    Text(
                        "\"Shorten sleeves by 1.25 inches. Ensure the functional buttonholes are preserved and spaced correctly from the new edge.\"",
                        fontSize = tokens.bodySmall,
                        color = TitleColor
                    )
                }

                Spacer(Modifier.height(sectionGap * 1.5f))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Straighten,
                        contentDescription = null,
                        tint = Accent,
                        modifier = Modifier.size(tokens.iconSize)
                    )
                    Spacer(Modifier.width(smallGap))
                    Text(
                        "Measurement Adjustments",
                        fontSize = tokens.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = TitleColor
                    )
                }
                Spacer(Modifier.height(fieldGap))

                MeasurementTable()

                Spacer(Modifier.height(sectionGap * 1.5f))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.PhotoCamera,
                        contentDescription = null,
                        tint = Accent,
                        modifier = Modifier.size(tokens.iconSize)
                    )
                    Spacer(Modifier.width(smallGap))
                    Text(
                        "Trial & Documentation",
                        fontSize = tokens.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = TitleColor
                    )
                }
                Spacer(Modifier.height(fieldGap))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MutedLabel,
                            modifier = Modifier.size(tokens.iconSize * 0.9f)
                        )
                        Spacer(Modifier.width(smallGap * 0.75f))
                        Column {
                            Text("Trial Date", fontSize = tokens.caption, color = MutedLabel)
                            Text(
                                "Oct 24, 2023 - 2:00 PM",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = TitleColor
                            )
                        }
                    }
                    OutlinedButton(
                        onClick = { },
                        shape = adaptiveCorner,
                        border = BorderStroke(1.dp, Accent),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary),
                        contentPadding = PaddingValues(horizontal = smallGap, vertical = tinyGap)
                    ) {
                        Text("Change", fontSize = tokens.caption, fontWeight = FontWeight.SemiBold)
                    }
                }
                Spacer(Modifier.height(fieldGap))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(smallGap)
                ) {
                    PhotoPlaceholder(
                        label = "BEFORE",
                        badgeColor = BeforeBadgeBg,
                        modifier = Modifier.weight(1f)
                    )
                    PhotoPlaceholder(
                        label = "WIP",
                        badgeColor = WipBadgeBg,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(sectionGap * 1.5f))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = Accent,
                        modifier = Modifier.size(tokens.iconSize)
                    )
                    Spacer(Modifier.width(smallGap))
                    Text(
                        "Assigned Tailor",
                        fontSize = tokens.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = TitleColor
                    )
                }
                Spacer(Modifier.height(smallGap))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(AccentBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("AR", color = Accent, fontWeight = FontWeight.Bold, fontSize = tokens.bodyMedium)
                    }
                    Spacer(Modifier.width(smallGap))
                    Column {
                        Text(
                            "Antonio Rossi",
                            fontSize = tokens.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = TitleColor
                        )
                        Row {
                            Text("Senior Tailor  •  ", fontSize = tokens.caption, color = LabelColor)
                            Text("Suiting Expert", fontSize = tokens.caption, color = Accent)
                        }
                    }
                }
                Spacer(Modifier.height(smallGap))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(adaptiveCorner)
                        .background(Color(0xFFefeff8))
                        .padding(horizontal = sectionGap, vertical = smallGap),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Active Jobs", fontSize = tokens.caption, color = MutedLabel)
                        Text(
                            "4",
                            fontSize = tokens.h2,
                            fontWeight = FontWeight.Bold,
                            color = TitleColor
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Success Rate", fontSize = tokens.caption, color = MutedLabel)
                        Text(
                            "98%",
                            fontSize = tokens.h2,
                            fontWeight = FontWeight.Bold,
                            color = SuccessColor
                        )
                    }
                }
                Spacer(Modifier.height(smallGap))

                OutlinedButton(
                    onClick = {  },
                    modifier = Modifier.fillMaxWidth(),
                    shape = adaptiveCorner,
                    border = BorderStroke(1.dp, Accent),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary)
                ) {
                    Text(
                        "View Profile & Capacity",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(Modifier.height(sectionGap * 1.5f))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.History,
                        contentDescription = null,
                        tint = Accent,
                        modifier = Modifier.size(tokens.iconSize)
                    )
                    Spacer(Modifier.width(smallGap))
                    Text(
                        "Activity Log & Notes",
                        fontSize = tokens.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = TitleColor
                    )
                }
                Spacer(Modifier.height(fieldGap))

                val activities = listOf(
                    Triple(
                        "Alteration Started",
                        "\"Measurements verified, starting sleeve shortening.\"",
                        "Antonio Rossi • 2h ago"
                    ),
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
                        Spacer(Modifier.width(smallGap))
                        Column(modifier = Modifier.padding(bottom = fieldGap)) {
                            Text(
                                title,
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = TitleColor
                            )
                            note?.let {
                                Spacer(Modifier.height(tinyGap))
                                Text(it, fontSize = tokens.caption, color = LabelColor)
                            }
                            Spacer(Modifier.height(tinyGap))
                            Text(meta, fontSize = tokens.caption, color = MutedLabel)
                        }
                    }
                }

                Spacer(Modifier.height(tinyGap))

                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    placeholder = {
                        Text(
                            "Add a comment or update...",
                            fontSize = tokens.bodySmall,
                            color = MutedLabel
                        )
                    },
                    shape = adaptiveCorner,
                    trailingIcon = {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = Accent,
                            modifier = Modifier
                                .size(tokens.iconSize)
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
                    modifier = Modifier.fillMaxWidth().padding(top = tinyGap),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row {
                        Icon(
                            Icons.Default.AttachFile,
                            contentDescription = null,
                            tint = MutedLabel,
                            modifier = Modifier.size(tokens.iconSize * 0.9f)
                        )
                        Spacer(Modifier.width(smallGap))
                        Icon(
                            Icons.Default.EmojiEmotions,
                            contentDescription = null,
                            tint = MutedLabel,
                            modifier = Modifier.size(tokens.iconSize * 0.9f)
                        )
                    }
                    Text("Press Enter to send", fontSize = tokens.caption, color = MutedLabel)
                }

                Spacer(Modifier.height(sectionGap))

                Button(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth().height(tokens.buttonHeight),
                    shape = adaptiveCorner,
                    colors = ButtonDefaults.buttonColors(containerColor = Accent)
                ) {
                    Text(
                        "Mark as Completed",
                        fontSize = tokens.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = whiteBg
                    )
                }
                Spacer(Modifier.height(sectionGap))
            }


            // ─────────────────────────────────────────────
            // UPDATE STATUS bottom sheet
            // ─────────────────────────────────────────────
            SmoothBottomSheet(
                state = updateStatusSheetState,
                onStateChange = { updateStatusSheetState = it },
                peekHeight = 480.dp,
                topInset = 66.dp,
                onDismissRequest = { updateStatusSheetState = SheetValue.Hidden },
                onBlurScrimChange = { blur, _ ->
                    updateStatusSheetBlur = blur
                }
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = sectionGap, vertical = smallGap)
                ) {
                    Text(
                        "UPDATE STATUS",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = TitleColor,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(tinyGap))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("ALT-882", fontSize = tokens.caption, color = MutedLabel)
                        Text(" / SO-450", fontSize = tokens.caption, color = MutedLabel)
                    }
                    Spacer(Modifier.height(sectionGap))

                    Text(
                        "Alteration Status",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = TitleColor
                    )
                    Spacer(Modifier.height(tinyGap))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(adaptiveCorner)
                            .border(1.dp, BorderColor, adaptiveCorner)
                            .clickable {  }
                            .padding(horizontal = fieldGap, vertical = fieldGap),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(selectedStatus, fontSize = tokens.bodyMedium, color = TitleColor)
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = LabelColor
                        )
                    }
                    Spacer(Modifier.height(fieldGap))

                    Text(
                        "Notes",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = TitleColor
                    )
                    Spacer(Modifier.height(tinyGap))
                    OutlinedTextField(
                        value = statusNotes,
                        onValueChange = { statusNotes = it },
                        placeholder = {
                            Text(
                                "Add an optional note...",
                                fontSize = tokens.bodySmall,
                                color = MutedLabel
                            )
                        },
                        minLines = 3,
                        shape = adaptiveCorner,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = BorderColor,
                            focusedBorderColor = Accent
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(sectionGap))

                    Button(
                        onClick = { updateStatusSheetState = SheetValue.Hidden },
                        modifier = Modifier.fillMaxWidth().height(tokens.buttonHeight),
                        shape = adaptiveCorner,
                        colors = ButtonDefaults.buttonColors(containerColor = Accent)
                    ) {
                        Text(
                            "Update Status",
                            fontSize = tokens.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = whiteBg
                        )
                    }
                    Spacer(Modifier.height(smallGap))
                    OutlinedButton(
                        onClick = { updateStatusSheetState = SheetValue.Hidden },
                        modifier = Modifier.fillMaxWidth().height(tokens.buttonHeight),
                        shape = adaptiveCorner,
                        border = BorderStroke(1.dp, BorderColor),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TitleColor)
                    ) {
                        Text("Cancel", fontSize = tokens.bodyLarge, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.height(fieldGap))

                    Text(
                        "LAST UPDATED 2D AGO BY J. DOE",
                        fontSize = tokens.label,
                        color = MutedLabel,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(sectionGap))
                }
            }

            // ─────────────────────────────────────────────
            // ASSIGN TAILOR bottom sheet
            // ─────────────────────────────────────────────
            SmoothBottomSheet(
                state = assignTailorSheetState,
                onStateChange = { assignTailorSheetState = it },
                peekHeight = 600.dp,
                topInset = 66.dp,   //   NEW — same fix
                onDismissRequest = { assignTailorSheetState = SheetValue.Hidden },
                onBlurScrimChange = { blur, _ ->
                    assignTailorSheetBlur = blur
                }
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = sectionGap, vertical = smallGap)
                ) {
                    Text(
                        "ASSIGN TAILOR",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = TitleColor,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(tinyGap))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("ALT-882", fontSize = tokens.caption, color = MutedLabel)
                        Text(" / SO-450", fontSize = tokens.caption, color = MutedLabel)
                    }
                    Spacer(Modifier.height(sectionGap))

                    Text(
                        "Select Tailor",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = TitleColor
                    )
                    Spacer(Modifier.height(tinyGap))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(adaptiveCorner)
                            .border(1.dp, BorderColor, adaptiveCorner)
                            .clickable {  }
                            .padding(horizontal = fieldGap, vertical = smallGap),
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
                            Spacer(Modifier.width(smallGap))
                            Column {
                                Text(
                                    "Alessandro Ricci",
                                    fontSize = tokens.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = TitleColor
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "Workload: 2 Active Jobs  ",
                                        fontSize = tokens.caption,
                                        color = MutedLabel
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFFE7F8EE))
                                            .padding(horizontal = smallGap, vertical = tinyGap * 0.6f)
                                    ) {
                                        Text(
                                            "Available",
                                            fontSize = tokens.label,
                                            color = SuccessColor,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = LabelColor
                        )
                    }
                    Spacer(Modifier.height(fieldGap))

                    Text(
                        "Target Completion Date",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = TitleColor
                    )
                    Spacer(Modifier.height(tinyGap))
                    OutlinedTextField(
                        value = targetDate,
                        onValueChange = { targetDate = it },
                        placeholder = { Text("MM/DD/YYYY", fontSize = tokens.bodySmall, color = MutedLabel) },
                        trailingIcon = {
                            Icon(
                                Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = LabelColor,
                                modifier = Modifier.size(tokens.iconSize)
                            )
                        },
                        singleLine = true,
                        shape = adaptiveCorner,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = BorderColor,
                            focusedBorderColor = Accent
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(fieldGap))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Tailor Weekly Capacity",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = Accent
                        )
                        Text("65% Full", fontSize = tokens.caption, color = LabelColor)
                    }
                    Spacer(Modifier.height(smallGap))
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
                    Spacer(Modifier.height(smallGap))
                    Text(
                        "Ricci typically completes 8 alterations per week. Current queue: 5.",
                        fontSize = tokens.caption,
                        color = LabelColor
                    )
                    Spacer(Modifier.height(fieldGap))

                    Text(
                        "Instruction for Staff",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = TitleColor
                    )
                    Spacer(Modifier.height(tinyGap))
                    OutlinedTextField(
                        value = staffInstructions,
                        onValueChange = { staffInstructions = it },
                        placeholder = {
                            Text(
                                "Add an optional note...",
                                fontSize = tokens.bodySmall,
                                color = MutedLabel
                            )
                        },
                        minLines = 3,
                        shape = adaptiveCorner,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = BorderColor,
                            focusedBorderColor = Accent
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(sectionGap * 1.5f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(smallGap)
                    ) {
                        OutlinedButton(
                            onClick = { assignTailorSheetState = SheetValue.Hidden },
                            modifier = Modifier.weight(1f).height(tokens.buttonHeight),
                            shape = adaptiveCorner,
                            border = BorderStroke(1.dp, BorderColor),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TitleColor)
                        ) {
                            Text("Cancel", fontSize = tokens.bodyLarge, fontWeight = FontWeight.SemiBold)
                        }
                        Button(
                            onClick = { assignTailorSheetState = SheetValue.Hidden },
                            modifier = Modifier.weight(1f).height(tokens.buttonHeight),
                            shape = adaptiveCorner,
                            colors = ButtonDefaults.buttonColors(containerColor = Primary)
                        ) {
                            Text(
                                "Assign Staff",
                                fontSize = tokens.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = whiteBg
                            )
                        }
                    }
                    Spacer(Modifier.height(sectionGap))
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// Progress stepper
// ─────────────────────────────────────────────
@Composable
private fun AlterationStepper() {
    val tokens = LocalAppTokens.current
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
                Spacer(Modifier.height(tokens.screenPadding * 0.3f))
                Text(
                    step.label,
                    fontSize = tokens.label,
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
    val tokens = LocalAppTokens.current
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
                Text((index + 1).toString(), fontSize = tokens.label, fontWeight = FontWeight.SemiBold, color = StepGrayText)
            }
        }
    }
}

// ─────────────────────────────────────────────
// Measurement Adjustments table
// ─────────────────────────────────────────────
@Composable
private fun MeasurementTable() {
    val tokens = LocalAppTokens.current
    val rows = listOf(
        Measurement("Chest", "42.0\"", "41.5\"", "-0.5\""),
        Measurement("Waist", "36.0\"", "35.2\"", "-0.8\""),
        Measurement("Sleeve Length", "25.5\"", "24.25\"", "-1.25\"", alteredHighlighted = true)
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text("Point", fontSize = tokens.caption, color = MutedLabel, modifier = Modifier.weight(1.4f))
            Text("Original", fontSize = tokens.caption, color = MutedLabel, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
            Text("Altered", fontSize = tokens.caption, color = MutedLabel, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
            Text("Diff", fontSize = tokens.caption, color = MutedLabel, modifier = Modifier.weight(0.8f), textAlign = TextAlign.End)
        }
        Spacer(Modifier.height(tokens.screenPadding * 0.6f))
        HorizontalDivider(color = BorderColor)

        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = tokens.screenPadding * 0.75f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(row.point, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TitleColor, modifier = Modifier.weight(1.4f))
                Text(row.original, fontSize = tokens.bodySmall, color = TitleColor, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                Text(
                    row.altered,
                    fontSize = tokens.bodySmall,
                    fontWeight = if (row.alteredHighlighted) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (row.alteredHighlighted) Accent else TitleColor,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End
                )
                Text(row.diff, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = PriorityRed, modifier = Modifier.weight(0.8f), textAlign = TextAlign.End)
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
    val tokens = LocalAppTokens.current
    Box(
        modifier = modifier
            .height(120.dp)
            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.65f))
            .background(SectionBg)
            .border(1.dp, BorderColor, RoundedCornerShape(tokens.cardCornerRadius * 0.65f))
    ) {
        Box(
            modifier = Modifier
                .padding(tokens.screenPadding * 0.5f)
                .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
                .background(badgeColor)
                .padding(horizontal = tokens.screenPadding * 0.6f, vertical = tokens.screenPadding * 0.2f)
        ) {
            Text(label, fontSize = tokens.label, fontWeight = FontWeight.SemiBold, color = whiteBg)
        }
    }
}