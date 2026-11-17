@file:Suppress("UNUSED_PARAMETER")

package com.cuso.tailor.view.home.security.user

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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

    val pagingState by securityViewModel.filteredUsersPagingState.collectAsStateWithLifecycle()
    val errorMessage by securityViewModel.usersError.collectAsStateWithLifecycle()
    val searchQuery by securityViewModel.userSearchQuery.collectAsStateWithLifecycle()

    // Permission checks for Create, Edit, and Delete
    val canCreateUser = permissionViewModel.canCreate("security_users")
    val canEditUser = permissionViewModel.canEdit("security_users")
    val canDeleteUser = permissionViewModel.canDelete("security_users")

    // Fetch initial users page on launch
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
                .background(Primary_background)
        ) {
            // Page Header with Export trailing action
            TitleBar(
                title = "User Management",
                onClose = onBack,
                trailingContent = {
                    OutlinedButton(
                        onClick = { /* Handle Export */ },
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                        border = BorderStroke(1.dp, sectionBorder),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = whiteBg),
                        contentPadding = PaddingValues(
                            horizontal = tokens.extraPadding * 1.2f,
                            vertical = tokens.extraPadding * 0.6f
                        ),
                        modifier = Modifier.height(tokens.buttonHeight * 0.85f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = "Export",
                            tint = TextPrimary,
                            modifier = Modifier.size(tokens.iconSize)
                        )
                        Spacer(Modifier.width(tokens.extraPadding * 0.6f))
                        Text(
                            text = "Export",
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                }
            )

            HorizontalDivider(color = title_border)

            // Search Bar & Filter Button
            SearchFilterBar(
                query = searchQuery,
                onQueryChange = { securityViewModel.onUserSearchQueryChange(it) },
                placeholder = "Search name, email, employee ID...",
                showFilterIcon = true,
                onFilterClick = { /* Handle Filter Dialog */ }
            )

            // Content Switcher with PaginatedDataList
            when {
                errorMessage != null && pagingState.items.isEmpty() && !pagingState.isInitialLoading -> {
                    AppErrorState(
                        title = "Failed to load users",
                        message = errorMessage?.ifBlank { "Something went wrong loading organization users." }
                            ?: "Something went wrong loading organization users.",
                        onRetry = { securityViewModel.fetchUsers() }
                    )
                }

                !pagingState.isInitialLoading && pagingState.items.isEmpty() -> {
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
                    PaginatedDataList(
                        state = pagingState,
                        onLoadMore = { securityViewModel.loadMoreUsers() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) { user ->
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

/**
 * Custom DataCard representation for User Management items.
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
        titleFontWeight = FontWeight.SemiBold,
        topBadgeText = if (isActive) "Active" else "Inactive",
        topBadgeTextColor = if (isActive) greentext else redText,
        topBadgeBgColor = if (isActive) greenBg else redBg,
        topBadgeInline = true,
        showActionsInHeader = false,
        actions = menuActions,
        onClick = { onView() },
        content = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f)
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

                // Inner Stats Card (Employee ID, Branch, Risk)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                    color = Primary_background,
                    border = BorderStroke(1.dp, sectionBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = tokens.cardPadding * 0.7f,
                                vertical = tokens.extraPadding
                            ),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Employee ID
                        Column(
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.2f)
                        ) {
                            Text(
                                text = "EMPLOYEE ID",
                                fontSize = tokens.label,
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

                        // Branch
                        Column(
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.2f)
                        ) {
                            Text(
                                text = "BRANCH",
                                fontSize = tokens.label,
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

                        // Risk Level
                        Column(
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.2f)
                        ) {
                            Text(
                                text = "RISK",
                                fontSize = tokens.label,
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

                // Last Login Row
                val lastLoginText = user.userAccount?.lastLoginAt?.take(16)?.replace("T", ", ") ?: "Never"
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "LAST LOGIN : ",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Medium,
                        color = mutedText
                    )
                    Text(
                        text = "$lastLoginText, Secure Workstation",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Normal,
                        color = close_color
                    )
                }
            }
        }
    )
}