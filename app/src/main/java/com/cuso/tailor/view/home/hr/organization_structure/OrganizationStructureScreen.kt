@file:Suppress("unused", "UNUSED_PARAMETER", "UNUSED_VARIABLE")

package com.cuso.tailor.view.home.hr.organization_structure

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.hr.OrgMemberDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.FormDropdown
import com.cuso.tailor.view.composable.FormLabel
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.viewmodel.HrViewModel

enum class StructureViewMode {
    TREE,
    VERTICAL
}

@Composable
fun OrganizationStructureScreen(
    hrViewModel: HrViewModel = hiltViewModel(),
    onClose: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current

    val members by hrViewModel.orgMembers.collectAsStateWithLifecycle()
    val isLoading by hrViewModel.isLoadingOrgStructure.collectAsStateWithLifecycle()

    var selectedDepartment by remember { mutableStateOf("All Departments") }
    var isDeptDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        hrViewModel.fetchOrgStructure()
    }

    val owners = remember(members) {
        members.filter { it.role.equals("owner", ignoreCase = true) }
    }

    val managersAndMembers = remember(members) {
        members.filterNot { it.role.equals("owner", ignoreCase = true) }
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TitleBar(
                title = "Organization Structure",
                onClose = onClose
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = tokens.screenPadding),
                contentPadding = PaddingValues(top = tokens.screenPadding, bottom = tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                FormLabel("Department")
                                FormDropdown(
                                    value = selectedDepartment,
                                    expanded = isDeptDropdownExpanded,
                                    onExpandChange = { isDeptDropdownExpanded = it },
                                    options = listOf("All Departments"),
                                    onOptionSelected = {
                                        selectedDepartment = it
                                        hrViewModel.fetchOrgStructure()
                                    }
                                )
                            }
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.7f),
                        colors = CardDefaults.cardColors(containerColor = whiteBg),
                        border = BorderStroke(1.dp, sectionBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(tokens.screenPadding * 0.85f),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            owners.forEach { owner ->
                                var isExpanded by remember { mutableStateOf(true) }

                                Column(modifier = Modifier.fillMaxWidth()) {
                                    MemberCardItem(
                                        member = owner,
                                        reportCount = managersAndMembers.size,
                                        isExpanded = isExpanded,
                                        hasChildren = managersAndMembers.isNotEmpty(),
                                        onToggleExpand = { isExpanded = !isExpanded }
                                    )

                                    AnimatedVisibility(
                                        visible = isExpanded,
                                        enter = expandVertically() + fadeIn(),
                                        exit = shrinkVertically() + fadeOut()
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(start = 20.dp, top = 10.dp),
                                            verticalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            managersAndMembers.forEach { childMember ->
                                                MemberCardItem(
                                                    member = childMember,
                                                    hasChildren = false
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            if (owners.isEmpty()) {
                                managersAndMembers.forEach { member ->
                                    MemberCardItem(member = member)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MemberCardItem(
    member: OrgMemberDto,
    reportCount: Int? = null,
    hasChildren: Boolean = false,
    isExpanded: Boolean = false,
    onToggleExpand: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.6f))
            .clickable(enabled = hasChildren) { onToggleExpand() },
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.6f),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        border = BorderStroke(1.dp, sectionBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(tokens.imageDp * 0.75f)
                    .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
                    .background(title_color),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = member.initials,
                    color = whiteBg,
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = member.fullName,
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )

                Spacer(Modifier.height(2.dp))

                Text(
                    text = member.role.ifBlank { "owner" }.lowercase(),
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Normal,
                    color = iconMuted
                )

                Spacer(Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(complete_button_bg)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (reportCount != null && reportCount > 0) {
                            "ACTIVE · $reportCount REPORTS"
                        } else {
                            "ACTIVE"
                        },
                        fontSize = tokens.label,
                        fontWeight = FontWeight.Medium,
                        color = headerGrey
                    )
                }
            }

            if (hasChildren) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = headerGrey,
                    modifier = Modifier.size(tokens.iconSize)
                )
            }
        }
    }
}