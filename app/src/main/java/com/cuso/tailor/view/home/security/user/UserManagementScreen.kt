
@file:Suppress("UNUSED_PARAMETER")

package com.cuso.tailor.view.home.security.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.security.UserManagementItemDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.PermissionViewModel
import com.cuso.tailor.viewmodel.SecurityViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(
    securityViewModel: SecurityViewModel = hiltViewModel(),
    permissionViewModel: PermissionViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onCreateUserClick: () -> Unit = {},
    onEditUserClick: (String) -> Unit = {},
    onViewUserClick: (String) -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val listState = rememberLazyListState()

    val users by securityViewModel.filteredUsers.collectAsStateWithLifecycle()
    val isLoading by securityViewModel.isLoadingUsers.collectAsStateWithLifecycle()
    val errorMessage by securityViewModel.usersError.collectAsStateWithLifecycle()
    val searchQuery by securityViewModel.userSearchQuery.collectAsStateWithLifecycle()

    // Permission checks for Create, Edit, Delete
    val canCreateUser = permissionViewModel.canCreate("security_users")
    val canEditUser = permissionViewModel.canEdit("security_users")
    val canDeleteUser = permissionViewModel.canDelete("security_users")

    // Fetch users on launch
    LaunchedEffect(Unit) {
        securityViewModel.fetchUsers()
    }

    FabScaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary_background),
        fab = FabConfig(
            label = "Create User",
            icon = Icons.Default.Add,
//            enabled = canCreateUser,
            onClick = {
                if (canCreateUser) {
                    onCreateUserClick()
                }
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
            // Header: User Management with Export Action Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(whiteBg)
                    .padding(end = tokens.screenPadding),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    TitleBar(
                        title = "User Management",
                        onClose = onBack
                    )
                }

                // Export Header Button
                OutlinedButton(
                    onClick = { /* Handle Export */ },
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, sectionBorder),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = whiteBg),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = "Export",
                        tint = TextPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Export",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }
            }

            HorizontalDivider(color = title_border)

            // Search Bar & Filter Button
            SearchFilterBar(
                query = searchQuery,
                onQueryChange = { securityViewModel.onUserSearchQueryChange(it) },
                placeholder = "Search SO number, customer...",
                showFilterIcon = true,
                onFilterClick = { /* Handle Filter Dialog */ }
            )

            // Content Switcher
            when {
                isLoading && users.isEmpty() -> {
                    ListSkeleton()
                }

                errorMessage != null && users.isEmpty() -> {
                    AppErrorState(
                        title = "Failed to load users",
                        message = errorMessage?.ifBlank { "Something went wrong loading organization users." }
                            ?: "Something went wrong loading organization users.",
                        onRetry = { securityViewModel.fetchUsers() }
                    )
                }

                users.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(tokens.screenPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isNotBlank()) "No users matching your search" else "No users registered yet",
                            fontSize = tokens.bodySmall,
                            color = mutedText
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(users, key = { it.id }) { user ->
                            UserManagementCard(
                                user = user,
                                canEdit = canEditUser,
                                canDelete = canDeleteUser,
                                onEdit = { onEditUserClick(user.id) },
                                onView = { onViewUserClick(user.id) },
                                onDelete = { /* Trigger delete confirmation */ }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Custom DataCard representation matching the provided screenshot design.
 */
@Composable
private fun UserManagementCard(
    user: UserManagementItemDto,
    canEdit: Boolean = true,
    canDelete: Boolean = true,
    onEdit: () -> Unit,
    onView: () -> Unit,
    onDelete: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val isActive = user.status.equals("active", ignoreCase = true)

    val menuActions = listOf(
        MenuAction(
            label = "View Details",
            icon = Icons.Default.Visibility,
            onClick = onView
        ),
        MenuAction(
            label = "Edit User",
            icon = Icons.Default.Edit,
            enabled = canEdit,
            onClick = onEdit
        ),
        MenuAction(
            label = "Delete User",
            icon = Icons.Default.Delete,
            enabled = canDelete,
            tint = redText,
            textColor = redText,
            onClick = onDelete
        )
    )

    DataCard(
        item = user,
        title = user.fullName,
        titleColor = TitleColor,
        titleFontWeight = FontWeight.Bold,
        topBadgeText = if (isActive) "Active" else "Inactive",
        topBadgeTextColor = if (isActive) greentext else redText,
        topBadgeBgColor = if (isActive) greenBg else redBg,
        topBadgeInline = true,
        showActionsInHeader = false,
        actions = menuActions,
        content = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Role Label & Value Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ROLE",
                        fontSize = tokens.label,
                        fontWeight = FontWeight.Medium,
                        color = mutedText
                    )
                    Text(
                        text = user.roleDisplayName,
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = TitleColor
                    )
                }

                // Inner Stats Card (EMPLOYEE ID, BRANCH, RISK)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Primary_background,
                    border = androidx.compose.foundation.BorderStroke(1.dp, sectionBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. Employee ID
                        Column(
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "EMPLOYEE ID",
                                fontSize = 10.sp,
                                color = mutedText,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = user.memberId ?: "--",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = TitleColor
                            )
                        }

                        // 2. Branch
                        Column(
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "BRANCH",
                                fontSize = 10.sp,
                                color = mutedText,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = user.branchName,
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = TitleColor
                            )
                        }

                        // 3. Risk Level
                        Column(
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "RISK",
                                fontSize = 10.sp,
                                color = mutedText,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = user.riskLevel,
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = when (user.riskLevel.lowercase()) {
                                    "high" -> redText
                                    "medium" -> orangeText
                                    else -> TitleColor
                                }
                            )
                        }
                    }
                }

                // Last Login row
                val lastLoginText = user.userAccount?.lastLoginAt?.take(16)?.replace("T", ", ") ?: "Never"
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "LAST LOGIN : ",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = mutedText
                    )
                    Text(
                        text = "$lastLoginText, Secure Workstation",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        color = close_color
                    )
                }
            }
        }
    )
}