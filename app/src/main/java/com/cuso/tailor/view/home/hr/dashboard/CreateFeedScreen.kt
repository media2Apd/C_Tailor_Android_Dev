@file:Suppress("unused")

package com.cuso.tailor.view.home.hr.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.view.composable.*

@Composable
fun CreateFeedScreen(
    onClose: () -> Unit,
    onSubmit: (
        postCategory: String,
        title: String,
        description: String,
        status: String,
        branchIds: List<String>,
        departmentIds: List<String>,
        employmentTypes: List<String>,
        pollQuestion: String?,
        pollOptions: List<String>,
        allowMultipleAnswers: Boolean,
        closingDate: String?
    ) -> Unit
) {
    val tokens = LocalAppTokens.current

    // Form states
    var category by remember { mutableStateOf("Company Announcement") }
    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    // Poll states
    var isPollExpanded by remember { mutableStateOf(true) }
    var pollQuestion by remember { mutableStateOf("") }
    val pollOptions = remember { mutableStateListOf("", "") }
    var allowMultipleAnswers by remember { mutableStateOf(true) }
    var closingDate by remember { mutableStateOf("2026-11-12") }

    // Audience & Share With states
    var shareWith by remember { mutableStateOf("Specific Branches & Teams") }
    var isShareWithExpanded by remember { mutableStateOf(false) }

    val selectedBranches = remember { mutableStateListOf("6ae6ec0bdc0babd6b3633da7") }
    val selectedDepartments = remember { mutableStateListOf("6ae6ec0cdc0babd6b3633dae") }
    val selectedEmploymentTypes = remember { mutableStateListOf("full-time") }

    val categoryOptions = listOf(
        "Company Announcement", "General Update", "Event", "Policy Update", "Recognition", "Other"
    )

    fun validateAndSubmit(status: String) {
        if (title.isBlank()) {
            DynamicIslandManager.showError("Post title is required")
            return
        }
        if (description.isBlank()) {
            DynamicIslandManager.showError("Description is required")
            return
        }

        val apiCategory = category.replace(" ", "_")
        val cleanedOptions = pollOptions.filter { it.isNotBlank() }

        onSubmit(
            apiCategory,
            title,
            description,
            status,
            selectedBranches.toList(),
            selectedDepartments.toList(),
            selectedEmploymentTypes.toList(),
            pollQuestion.ifBlank { null },
            cleanedOptions,
            allowMultipleAnswers,
            closingDate.ifBlank { null }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = Primary_background,
        topBar = {
            TitleBar(
                title = "Create Feed",
                subtitle = "Share updates, announcements and important information with your organization.",
                onClose = onClose
            )
        },
        bottomBar = {
            Surface(
                color = whiteBg,
                border = BorderStroke(1.dp, sectionBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = tokens.screenPadding, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onClose,
                        modifier = Modifier.weight(0.7f)
                    ) {
                        Text("Cancel", fontSize = tokens.bodySmall, color = TextSecondary)
                    }

                    // Save as Draft Button
                    OutlinedButton(
                        onClick = { validateAndSubmit("Draft") },
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.45f),
                        border = BorderStroke(1.dp, sectionBorder),
                        modifier = Modifier
                            .weight(1.1f)
                            .height(tokens.buttonHeight)
                    ) {
                        Text("Save as Draft", fontSize = tokens.bodySmall, color = title_color)
                    }

                    // Publish Feed Button
                    Button(
                        onClick = { validateAndSubmit("Published") },
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.45f),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        modifier = Modifier
                            .weight(1.2f)
                            .height(tokens.buttonHeight)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = null,
                            tint = whiteBg,
                            modifier = Modifier.size(tokens.iconSize * 0.9f)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Publish Feed", fontSize = tokens.bodySmall, color = whiteBg)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = tokens.screenPadding, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Post Category Dropdown
            Text(
                text = "Post Category *",
                fontSize = tokens.caption,
                fontWeight = FontWeight.SemiBold,
                color = title_color
            )
            FormDropdown(
                value = category,
                expanded = isCategoryDropdownExpanded,
                onExpandChange = { isCategoryDropdownExpanded = it },
                options = categoryOptions,
                onOptionSelected = {
                    category = it
                    isCategoryDropdownExpanded = false
                }
            )

            // Post Title Input
            Text(
                text = "Post Title *",
                fontSize = tokens.caption,
                fontWeight = FontWeight.SemiBold,
                color = title_color
            )
            FormTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = "e.g. Annual Atelier Craftsmanship Awards 2026: Submissions Open"
            )

            // Description Text Area
            Text(
                text = "Description *",
                fontSize = tokens.caption,
                fontWeight = FontWeight.SemiBold,
                color = title_color
            )
            FormTextArea(
                value = description,
                onValueChange = { description = it },
                placeholder = "We are proud to announce that nominations for the Annual Atelier Craftsmanship Awards 2026 are officially open...",
                minLines = 4,
                maxLines = 6
            )

            // Attachments & Media Action Row
            Text(
                text = "ATTACHMENTS & MEDIA",
                fontSize = tokens.label,
                fontWeight = FontWeight.SemiBold,
                color = headerGrey
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, sectionBorder),
                    color = whiteBg
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                        Text("Documents", fontSize = tokens.label, color = TextPrimary)
                    }
                }
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, sectionBorder),
                    color = whiteBg
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, tint = blueText, modifier = Modifier.size(16.dp))
                        Text("Photos", fontSize = tokens.label, color = TextPrimary)
                    }
                }
            }

            // Create a Poll Card
            Surface(
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.7f),
                color = whiteBg,
                border = BorderStroke(1.dp, sectionBorder)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isPollExpanded = !isPollExpanded },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Poll, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
                            Text(
                                text = "Create a Poll (Optional)",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = title_color
                            )
                        }
                        Icon(
                            imageVector = if (isPollExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = headerGrey
                        )
                    }

                    AnimatedVisibility(visible = isPollExpanded) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Poll Question", fontSize = tokens.label, color = headerGrey)
                            FormTextField(
                                value = pollQuestion,
                                onValueChange = { pollQuestion = it },
                                placeholder = "Preferred townhall celebration session timing?"
                            )

                            Text("Answer Options", fontSize = tokens.label, color = headerGrey)
                            pollOptions.forEachIndexed { index, option ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    FormTextField(
                                        modifier = Modifier.weight(1f),
                                        value = option,
                                        onValueChange = { pollOptions[index] = it },
                                        placeholder = "Option ${index + 1}"
                                    )
                                    if (pollOptions.size > 2) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove option",
                                            tint = redText,
                                            modifier = Modifier
                                                .size(18.dp)
                                                .clickable { pollOptions.removeAt(index) }
                                        )
                                    }
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.clickable { pollOptions.add("") }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Primary, modifier = Modifier.size(14.dp))
                                Text("+ Add Option", fontSize = tokens.caption, fontWeight = FontWeight.SemiBold, color = Primary)
                            }

                            HorizontalDivider(color = grey_border, thickness = 0.8.dp)

                            // Multiple Selections Checkbox
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Checkbox(
                                    checked = allowMultipleAnswers,
                                    onCheckedChange = { allowMultipleAnswers = it },
                                    colors = CheckboxDefaults.colors(checkedColor = Primary)
                                )
                                Text("Allow multiple selections", fontSize = tokens.caption, color = TextPrimary)
                            }

                            // Closing Date Field
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text("Closing date:", fontSize = tokens.caption, color = TextSecondary)
                                DatePickerField(
                                    value = closingDate,
                                    onDateSelected = { closingDate = it }
                                )
                            }
                        }
                    }
                }
            }

            // Share With Section
            Text("Share With *", fontSize = tokens.caption, fontWeight = FontWeight.SemiBold, color = title_color)
            FormDropdown(
                value = shareWith,
                expanded = isShareWithExpanded,
                onExpandChange = { isShareWithExpanded = it },
                options = listOf("Specific Branches & Teams", "All Employees"),
                onOptionSelected = {
                    shareWith = it
                    isShareWithExpanded = false
                }
            )

            // Target Audience Grouping Card
            Surface(
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.7f),
                color = light_grey.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, sectionBorder)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Select Branches:", fontSize = tokens.label, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(primary_light)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("Sample - Main Branch", fontSize = tokens.caption, color = Primary, fontWeight = FontWeight.Medium)
                    }

                    Text("Select Departments & Teams:", fontSize = tokens.label, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(primary_light)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("#Management", fontSize = tokens.caption, color = Primary, fontWeight = FontWeight.Medium)
                    }

                    Text("Select Employment Types:", fontSize = tokens.label, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Full Time", "Part Time", "Contract", "Volunteer").forEach { type ->
                            val key = type.lowercase().replace(" ", "-")
                            val isSelected = key in selectedEmploymentTypes
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) primary_light else whiteBg)
                                    .border(1.dp, if (isSelected) Primary else sectionBorder, RoundedCornerShape(6.dp))
                                    .clickable {
                                        if (isSelected) selectedEmploymentTypes.remove(key)
                                        else selectedEmploymentTypes.add(key)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(type, fontSize = tokens.label, color = if (isSelected) Primary else TextSecondary)
                            }
                        }
                    }
                }
            }

            Text(
                text = "Your post will be visible to the selected audience in their Employee Feed.",
                fontSize = tokens.label,
                color = headerGrey
            )
        }
    }
}