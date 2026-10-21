@file:Suppress("unused", "UNUSED_PARAMETER","UNUSED_VARIABLE")

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
import androidx.compose.ui.unit.sp
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
    var viewMode by remember { mutableStateOf(StructureViewMode.VERTICAL) }

    // Fetch API on load
    LaunchedEffect(Unit) {
        hrViewModel.fetchOrgStructure()
    }

    // Separate Hierarchy:
    // Level 1: Owner
    val owners = remember(members) {
        members.filter { it.role.equals("owner", ignoreCase = true) }
    }

    // Level 2: Managers / Direct reports under owner
    val managersAndMembers = remember(members) {
        members.filterNot { it.role.equals("owner", ignoreCase = true) }
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0,0,0,0),
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
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header with subtitle description
                item {
                    Column {
                        // Filter Row: Department on the Left, Toggle buttons on the Right
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            // Department dropdown
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

                // Organization hierarchy container card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // 1. Level 1: Owners Card
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

                                    // Dropdown children (Members under owner)
                                    AnimatedVisibility(
                                        visible = isExpanded,
                                        enter = expandVertically() + fadeIn(),
                                        exit = shrinkVertically() + fadeOut()
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(start = 24.dp, top = 12.dp),
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
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

                            // If no owner is assigned, fallback list
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

/**
 * Reusable Member hierarchy card matching the design exactly
 */
@Composable
private fun MemberCardItem(
    member: OrgMemberDto,
    reportCount: Int? = null,
    hasChildren: Boolean = false,
    isExpanded: Boolean = false,
    onToggleExpand: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(enabled = hasChildren) { onToggleExpand() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Dark Squarish Rounded Avatar (Matching screenshot)
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF1E293B)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = member.initials,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(Modifier.width(14.dp))

            // Info Column with tight line heights & exact alignments
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                // Name
                Text(
                    text = member.fullName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    lineHeight = 16.sp
                )

                Spacer(Modifier.height(3.dp))

                // Role / Designation
                Text(
                    text = member.role.ifBlank { "owner" }.lowercase(),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF94A3B8),
                    lineHeight = 14.sp
                )

                Spacer(Modifier.height(6.dp))

                // Active dot & Reports count tag
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (reportCount != null && reportCount > 0) {
                            "ACTIVE · $reportCount REPORTS"
                        } else {
                            "ACTIVE"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF64748B),
                        letterSpacing = 0.6.sp,
                        lineHeight = 10.sp
                    )
                }
            }

            // Dropdown Chevron Indicator (Right aligned)
            if (hasChildren) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = Color(0xFF64748B),
                    modifier = Modifier
                        .size(22.dp)
                        .padding(end = 2.dp)
                )
            }
        }
    }
}