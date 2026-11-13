package com.cuso.tailor.view.home.topBarEvents

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.topBarModels.AgendaItemDto
import com.cuso.tailor.model.topBarModels.HolidayItemDto
import com.cuso.tailor.model.topBarModels.NotificationItemDto
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.Primary_background
import com.cuso.tailor.ui.theme.TitleColor
import com.cuso.tailor.ui.theme.light_blue_border
import com.cuso.tailor.ui.theme.mutedText
import com.cuso.tailor.ui.theme.redText
import com.cuso.tailor.ui.theme.title_color
import com.cuso.tailor.ui.theme.whiteBg
import com.cuso.tailor.viewmodel.TopbarEventViewModel

// High-contrast text colors matching Profile Settings
private val DarkHeadingColor = TitleColor
private val BodyTextColor = title_color

// =========================================================================
// 1. NOTIFICATIONS SCREEN
// =========================================================================

@Composable
fun NotificationsScreen(
    viewModel: TopbarEventViewModel,
    onNotificationNavigate: (NotificationItemDto) -> Unit = {},
    onBack: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val unreadCount by viewModel.unreadCount.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingNotifications.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = All, 1 = Unread

    LaunchedEffect(Unit) {
        viewModel.fetchNotifications()
    }

    val displayedList = remember(notifications, selectedTab) {
        if (selectedTab == 0) notifications else notifications.filter { !it.isRead }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = whiteBg
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = tokens.screenPadding,
                        vertical = tokens.screenPadding * 0.85f
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(tokens.screenPadding * 0.5f)
                ) {
                    Text(
                        text = "Notifications",
                        color = DarkHeadingColor,
                        fontSize = tokens.bodySmall * 1.35f,
                        fontWeight = FontWeight.Bold
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Primary.copy(alpha = 0.12f))
                            .padding(
                                horizontal = tokens.screenPadding * 0.55f,
                                vertical = tokens.screenPadding * 0.2f
                            )
                    ) {
                        Text(
                            text = "$unreadCount unread",
                            color = Primary,
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(tokens.screenPadding * 0.5f)
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.6f))
                            .clickable(enabled = unreadCount > 0) {
                                viewModel.markAllNotificationsAsRead()
                            }
                            .padding(
                                horizontal = tokens.screenPadding * 0.35f,
                                vertical = tokens.screenPadding * 0.25f
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = "Mark all read",
                            tint = if (unreadCount > 0) Primary else BodyTextColor,
                            modifier = Modifier.size(tokens.iconSize * 0.95f)
                        )
                        Text(
                            text = "Mark all read",
                            color = if (unreadCount > 0) Primary else BodyTextColor,
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(tokens.iconSize * 1.5f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = BodyTextColor,
                            modifier = Modifier.size(tokens.iconSize * 1.15f)
                        )
                    }
                }
            }

            HorizontalDivider(color = light_blue_border)

            // Filter Tabs: All | Unread
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = tokens.screenPadding),
                horizontalArrangement = Arrangement.spacedBy(tokens.screenPadding * 1.4f)
            ) {
                TopbarUnderlineTab(
                    title = "All (${notifications.size})",
                    isSelected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                TopbarUnderlineTab(
                    title = "Unread ($unreadCount)",
                    isSelected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
            }

            HorizontalDivider(color = light_blue_border)

            // Notification List
            when {
                isLoading && notifications.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Primary)
                    }
                }

                displayedList.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(tokens.screenPadding * 2f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (selectedTab == 1) "No unread notifications." else "No notifications yet.",
                            color = BodyTextColor,
                            fontSize = tokens.bodySmall
                        )
                    }
                }

                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(
                            items = displayedList,
                            key = { it._id.ifBlank { it.createdAt + it.title } }
                        ) { item ->
                            NotificationRowItem(
                                item = item,
                                formattedTime = viewModel.formatNotificationDate(item.createdAt),
                                onClick = {
                                    viewModel.markNotificationAsRead(item._id)
                                    onNotificationNavigate(item)
                                }
                            )
                            HorizontalDivider(color = light_blue_border.copy(alpha = 0.7f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationRowItem(
    item: NotificationItemDto,
    formattedTime: String,
    onClick: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val iconBgColor = if (item.severity.equals("error", ignoreCase = true)) {
        redText.copy(alpha = 0.12f)
    } else {
        Primary.copy(alpha = 0.12f)
    }
    val iconTintColor = if (item.severity.equals("error", ignoreCase = true)) redText else Primary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (!item.isRead) Primary_background.copy(alpha = 0.45f) else whiteBg)
            .clickable { onClick() }
            .padding(
                horizontal = tokens.screenPadding,
                vertical = tokens.screenPadding * 0.9f
            ),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(tokens.buttonHeight * 0.95f)
                .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.85f))
                .background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = mapNotificationIcon(item.icon, item.severity),
                contentDescription = item.entityType ?: "Notification",
                tint = iconTintColor,
                modifier = Modifier.size(tokens.iconSize * 1.15f)
            )
        }

        Spacer(modifier = Modifier.width(tokens.screenPadding * 0.75f))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                color = DarkHeadingColor,
                fontSize = tokens.bodySmall * 1.05f,
                fontWeight = if (!item.isRead) FontWeight.Bold else FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = item.message,
                color = BodyTextColor,
                fontSize = tokens.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = mutedText,
                    modifier = Modifier.size(tokens.iconSize * 0.7f)
                )
                Text(
                    text = formattedTime,
                    color = mutedText,
                    fontSize = tokens.caption
                )
            }
        }

        Spacer(modifier = Modifier.width(tokens.screenPadding * 0.5f))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(tokens.screenPadding * 0.45f),
            modifier = Modifier.padding(top = 4.dp)
        ) {
            if (!item.isRead) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Primary)
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Open",
                tint = BodyTextColor,
                modifier = Modifier.size(tokens.iconSize)
            )
        }
    }
}

private fun mapNotificationIcon(iconName: String, severity: String): ImageVector {
    return when (iconName.lowercase()) {
        "layers" -> Icons.Default.Layers
        "warning", "alert" -> Icons.Default.Warning
        "info" -> Icons.Default.Info
        else -> if (severity.equals("warning", ignoreCase = true)) {
            Icons.Default.Warning
        } else {
            Icons.Default.Notifications
        }
    }
}

// =========================================================================
// 2. BUSINESS AGENDA SCREEN
// =========================================================================

@Composable
fun BusinessAgendaScreen(
    viewModel: TopbarEventViewModel,
    onAgendaItemClick: (AgendaItemDto) -> Unit = {},
    onBack: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val agendaData by viewModel.agendaData.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingAgenda.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Today, 1 = Upcoming, 2 = Holidays

    LaunchedEffect(Unit) {
        viewModel.fetchBusinessAgenda()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = whiteBg
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = tokens.screenPadding,
                        vertical = tokens.screenPadding * 0.85f
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(tokens.screenPadding * 0.7f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(tokens.buttonHeight * 0.9f)
                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.8f))
                            .background(Primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Business Agenda",
                            tint = Primary,
                            modifier = Modifier.size(tokens.iconSize * 1.05f)
                        )
                    }

                    Column {
                        Text(
                            text = "Business Agenda",
                            color = DarkHeadingColor,
                            fontSize = tokens.bodySmall * 1.25f,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = viewModel.getFormattedTodayHeader(),
                            color = BodyTextColor,
                            fontSize = tokens.bodySmall
                        )
                    }
                }

                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(tokens.iconSize * 1.5f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = BodyTextColor,
                        modifier = Modifier.size(tokens.iconSize * 1.15f)
                    )
                }
            }

            HorizontalDivider(color = light_blue_border)

            // 3 Tabs: Today [N] | Upcoming (N) | Holidays
            Row(modifier = Modifier.fillMaxWidth()) {
                AgendaTabItem(
                    modifier = Modifier.weight(1f),
                    title = "Today",
                    badgeCount = agendaData.todayAgenda.size,
                    isSelected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                AgendaTabItem(
                    modifier = Modifier.weight(1f),
                    title = "Upcoming (${agendaData.upcomingEvents.size})",
                    badgeCount = null,
                    isSelected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
                AgendaTabItem(
                    modifier = Modifier.weight(1f),
                    title = "Holidays",
                    badgeCount = null,
                    isSelected = selectedTab == 2,
                    onClick = { selectedTab = 2 }
                )
            }

            HorizontalDivider(color = light_blue_border)

            // Tab Body
            val isCurrentTabEmpty = when (selectedTab) {
                0 -> agendaData.todayAgenda.isEmpty()
                1 -> agendaData.upcomingEvents.isEmpty()
                else -> agendaData.holidays.isEmpty()
            }

            val emptyMessage = when (selectedTab) {
                0 -> "No items for today. Great job!"
                1 -> "No upcoming items scheduled."
                else -> "No holidays found."
            }

            when {
                isLoading && isCurrentTabEmpty -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Primary)
                    }
                }

                isCurrentTabEmpty -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = tokens.screenPadding * 4f),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Text(
                            text = emptyMessage,
                            color = BodyTextColor,
                            fontSize = tokens.bodySmall
                        )
                    }
                }

                selectedTab == 2 -> {
                    // Holidays Tab
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(tokens.screenPadding),
                        verticalArrangement = Arrangement.spacedBy(tokens.screenPadding * 0.65f)
                    ) {
                        items(
                            items = agendaData.holidays,
                            key = { it.id.ifBlank { it.name + it.date } }
                        ) { holiday ->
                            HolidayRowCard(holiday = holiday)
                        }
                    }
                }

                else -> {
                    // Today or Upcoming Tab
                    val agendaItems = if (selectedTab == 0) {
                        agendaData.todayAgenda
                    } else {
                        agendaData.upcomingEvents
                    }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(tokens.screenPadding),
                        verticalArrangement = Arrangement.spacedBy(tokens.screenPadding * 0.65f)
                    ) {
                        items(
                            items = agendaItems,
                            key = { it.id.ifBlank { it.title + it.date } }
                        ) { item ->
                            AgendaRowCard(
                                item = item,
                                onClick = { onAgendaItemClick(item) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AgendaTabItem(
    modifier: Modifier = Modifier,
    title: String,
    badgeCount: Int?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val tokens = LocalAppTokens.current
    Column(
        modifier = modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.padding(vertical = tokens.screenPadding * 0.75f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                color = if (isSelected) Primary else BodyTextColor,
                fontSize = tokens.bodySmall,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
            )
            if (badgeCount != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Primary.copy(alpha = 0.12f))
                        .padding(horizontal = 7.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = badgeCount.toString(),
                        color = Primary,
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(if (isSelected) Primary else whiteBg)
        )
    }
}

@Composable
private fun AgendaRowCard(
    item: AgendaItemDto,
    onClick: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val isUrgent = item.type.equals("urgent", ignoreCase = true)
    val accentColor = if (isUrgent) redText else Primary
    val iconVector = when (item.type.lowercase()) {
        "urgent" -> Icons.Default.Warning
        "celebration" -> Icons.Default.Star
        else -> Icons.Default.EventAvailable
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        color = Primary_background.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, light_blue_border)
    ) {
        Row(
            modifier = Modifier.padding(tokens.screenPadding * 0.85f),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(tokens.screenPadding * 0.7f)
        ) {
            Box(
                modifier = Modifier
                    .size(tokens.buttonHeight * 0.85f)
                    .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.75f))
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = item.module,
                    tint = accentColor,
                    modifier = Modifier.size(tokens.iconSize)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (item.module.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(accentColor.copy(alpha = 0.12f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = item.module,
                                color = accentColor,
                                fontSize = tokens.caption,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    val timeOrDate = item.time.ifBlank { item.date }
                    if (timeOrDate.isNotBlank()) {
                        Text(
                            text = timeOrDate,
                            color = if (isUrgent) redText else mutedText,
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = item.title,
                    color = DarkHeadingColor,
                    fontSize = tokens.bodySmall * 1.05f,
                    fontWeight = FontWeight.SemiBold
                )

                if (item.desc.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.desc,
                        color = BodyTextColor,
                        fontSize = tokens.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun HolidayRowCard(
    holiday: HolidayItemDto
) {
    val tokens = LocalAppTokens.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        color = Primary_background.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, light_blue_border)
    ) {
        Row(
            modifier = Modifier.padding(tokens.screenPadding * 0.85f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(tokens.screenPadding * 0.7f)
        ) {
            Box(
                modifier = Modifier
                    .size(tokens.buttonHeight * 0.85f)
                    .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.75f))
                    .background(Primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Holiday",
                    tint = Primary,
                    modifier = Modifier.size(tokens.iconSize)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = holiday.name,
                    color = DarkHeadingColor,
                    fontSize = tokens.bodySmall * 1.05f,
                    fontWeight = FontWeight.SemiBold
                )
                if (holiday.date.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = holiday.date,
                        color = BodyTextColor,
                        fontSize = tokens.caption
                    )
                }
            }

            if (holiday.daysLeft.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Primary.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = holiday.daysLeft,
                        color = Primary,
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun TopbarUnderlineTab(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val tokens = LocalAppTokens.current
    Column(
        modifier = Modifier
            .width(IntrinsicSize.Max)
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            color = if (isSelected) Primary else BodyTextColor,
            fontSize = tokens.bodySmall,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            modifier = Modifier.padding(vertical = tokens.screenPadding * 0.75f)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(if (isSelected) Primary else whiteBg)
        )
    }
}