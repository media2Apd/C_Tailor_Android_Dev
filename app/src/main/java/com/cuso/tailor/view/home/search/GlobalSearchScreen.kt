package com.cuso.tailor.view.home.search

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.database.entities.UserEntity
import com.cuso.tailor.ui.theme.BorderGray
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.Primary_background
import com.cuso.tailor.ui.theme.TextPrimary
import com.cuso.tailor.ui.theme.TextSecondary
import com.cuso.tailor.ui.theme.grey_border
import com.cuso.tailor.ui.theme.light_grey
import com.cuso.tailor.ui.theme.mutedText
import com.cuso.tailor.ui.theme.primary_light
import com.cuso.tailor.ui.theme.redBg
import com.cuso.tailor.ui.theme.redText
import com.cuso.tailor.ui.theme.sectionBorder
import com.cuso.tailor.ui.theme.title_color
import com.cuso.tailor.ui.theme.whiteBg
import com.cuso.tailor.view.home.normalizeRoute
import com.cuso.tailor.view.home.sidebar.SidebarConfig
import com.cuso.tailor.view.home.sidebar.buildNavigationKey
import com.cuso.tailor.viewmodel.PermissionViewModel

/**
 * Represents a flattened searchable destination extracted from the sidebar navigation tree.
 */
data class SearchablePageItem(
    val id: String,
    val title: String,
    val breadcrumb: String,
    val module: String,
    val routeKey: String,
    val searchTerms: String
)

/**
 * Extracts and flattens all allowed navigation pages from SidebarConfig,
 * matching the behavior of extractSearchablePages + filterSidebar in Header.jsx.
 */
fun extractSearchablePages(permissionViewModel: PermissionViewModel): List<SearchablePageItem> {
    val pages = mutableListOf<SearchablePageItem>()

    SidebarConfig.getFullMenuItems().forEach { menuItem ->
        val moduleLabel = menuItem.label

        menuItem.subItems.forEach { (category, subItems) ->
            if (subItems.isEmpty()) {
                val rawRoute = resolveRouteKey(moduleLabel, category, null)
                val normalized = normalizeRoute(rawRoute)
                val breadcrumb = "$moduleLabel > $category"

                if (hasAccessToRoute(permissionViewModel, normalized)) {
                    pages.add(
                        SearchablePageItem(
                            id = "${moduleLabel}_${category}_$normalized",
                            title = category,
                            breadcrumb = breadcrumb,
                            module = moduleLabel,
                            routeKey = normalized,
                            searchTerms = "$category $breadcrumb $moduleLabel $normalized".lowercase()
                        )
                    )
                }
            } else {
                subItems.forEach { subItem ->
                    val rawRoute = resolveRouteKey(moduleLabel, category, subItem)
                    val normalized = normalizeRoute(rawRoute)
                    val breadcrumb = "$moduleLabel > $category > $subItem"

                    if (hasAccessToRoute(permissionViewModel, normalized)) {
                        pages.add(
                            SearchablePageItem(
                                id = "${moduleLabel}_${category}_${subItem}_$normalized",
                                title = subItem,
                                breadcrumb = breadcrumb,
                                module = moduleLabel,
                                routeKey = normalized,
                                searchTerms = "$subItem $category $breadcrumb $moduleLabel $normalized".lowercase()
                            )
                        )
                    }
                }
            }
        }
    }

    // Include core workspace destinations not nested under module sub-items
    val corePages = listOf(
        SearchablePageItem(
            id = "core_home_dashboard",
            title = "Home Dashboard",
            breadcrumb = "Home > Overview",
            module = "Home",
            routeKey = "home",
            searchTerms = "home dashboard overview main"
        ),
        SearchablePageItem(
            id = "core_settings_overview",
            title = "Settings Overview",
            breadcrumb = "Home > Settings Overview",
            module = "Settings",
            routeKey = "settings_overview",
            searchTerms = "settings overview configuration organization module subscription"
        ),
        SearchablePageItem(
            id = "core_profile_settings",
            title = "Profile Settings",
            breadcrumb = "Account > Profile Settings",
            module = "Profile",
            routeKey = "profile-settings",
            searchTerms = "profile account settings logout user"
        ),
        SearchablePageItem(
            id = "core_notifications",
            title = "Notifications",
            breadcrumb = "Home > Notifications",
            module = "Home",
            routeKey = "notifications",
            searchTerms = "notifications alerts inbox updates"
        ),
        SearchablePageItem(
            id = "core_business_agenda",
            title = "Business Agenda",
            breadcrumb = "Home > Calendar & Deadlines",
            module = "Home",
            routeKey = "business_agenda",
            searchTerms = "business agenda calendar deadlines today events"
        )
    )

    corePages.forEach { item ->
        if (hasAccessToRoute(permissionViewModel, item.routeKey)) {
            pages.add(item)
        }
    }

    return pages.distinctBy { "${it.routeKey}_${it.title}" }
}

private fun resolveRouteKey(menu: String, category: String, subItem: String?): String {
    return when {
        menu == "Legal" && category.contains("calendar", ignoreCase = true) -> "legal_calendar"
        menu == "Legal" && (category.contains("vendor", ignoreCase = true) || category.contains("supplier", ignoreCase = true)) -> "legal_vendor"
        menu == "Legal" && (category.contains("employee", ignoreCase = true) || category.contains("labour", ignoreCase = true)) -> "legal_employee"
        menu == "Finance" && category.contains("return", ignoreCase = true) -> "finance_purchase_returns"
        menu == "Legal" && (category.contains("notice", ignoreCase = true) || category.contains("dispute", ignoreCase = true)) -> "legal_notice"
        menu == "Finance" && (category == "Purchase" && subItem?.contains("credit", ignoreCase = true) == true || subItem?.contains("Supplier Credit", ignoreCase = true) == true) -> "finance_vendor_credits"
        menu == "Marketing" && (category.equals("Advertising", ignoreCase = true) || subItem?.equals("Advertising", ignoreCase = true) == true) -> "marketing_advertising_overview"
        menu == "Services" && (category.contains("workflow", ignoreCase = true) || category.contains("stage", ignoreCase = true) || subItem?.contains("stage", ignoreCase = true) == true) -> "services_production_stages"
        menu == "Security" && (category.contains("mobile", ignoreCase = true) || subItem?.contains("mobile", ignoreCase = true) == true) -> "security_mobile_security"
        menu == "Security" && (category.contains("help", ignoreCase = true) || subItem?.contains("help", ignoreCase = true) == true) -> "security_help_center"
        subItem != null -> buildNavigationKey(menu, subItem, category)
        else -> {
            val menuItem = SidebarConfig.getFullMenuItems().find { it.label == menu }
            val firstSubItem = menuItem?.subItems?.get(category)?.firstOrNull()
            buildNavigationKey(menu, firstSubItem ?: category, category)
        }
    }
}

private fun hasAccessToRoute(permissionViewModel: PermissionViewModel, routeKey: String): Boolean {
    val mapping = permissionViewModel.getRouteFeatureMapping(routeKey) ?: return true
    val (module, feature) = mapping
    val requiredAction = permissionViewModel.getRouteAction(routeKey)
    return permissionViewModel.canAction(module, feature, requiredAction)
}

@Composable
fun GlobalSearchScreen(
    userEntity: UserEntity?,
    permissionViewModel: PermissionViewModel,
    onClose: () -> Unit,
    onProfileClick: () -> Unit,
    onPageSelected: (String) -> Unit
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current

    var searchQuery by remember { mutableStateOf("") }
    var isSearchFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    val allPages = remember(permissionViewModel) {
        extractSearchablePages(permissionViewModel)
    }

    val filteredResults = remember(searchQuery, allPages) {
        val trimmed = searchQuery.trim().lowercase()
        if (trimmed.isEmpty()) {
            emptyList()
        } else {
            allPages.filter { it.searchTerms.contains(trimmed) }
        }
    }

    // Request keyboard focus automatically when screen opens
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    BackHandler {
        onClose()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary_background)
            .navigationBarsPadding()
    ) {
        // Top Search Header with Back Arrow, Input Box, and User Profile DP
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = whiteBg,
            shadowElevation = 1.dp,
            tonalElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
            ) {
                // Search Input Container
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(tokens.fieldHeight)
                        .clip(RoundedCornerShape(tokens.cardCornerRadius))
                        .background(light_grey)
                        .border(
                            width = 1.dp,
                            color = if (isSearchFocused) Primary else BorderGray,
                            shape = RoundedCornerShape(tokens.cardCornerRadius)
                        )
                        .padding(horizontal = tokens.extraPadding),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextSecondary,
                        modifier = Modifier
                            .size(tokens.iconSize)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { onClose() }
                    )

                    Spacer(modifier = Modifier.width(tokens.extraPadding * 0.8f))

                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        cursorBrush = SolidColor(Primary),
                        textStyle = TextStyle(
                            fontSize = tokens.bodyMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester)
                            .onFocusChanged { isSearchFocused = it.isFocused },
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Search modules...",
                                        fontSize = tokens.bodySmall,
                                        color = mutedText,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )

                    if (searchQuery.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(tokens.extraPadding * 0.6f))
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear search",
                            tint = mutedText,
                            modifier = Modifier
                                .size(tokens.iconSize)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) { searchQuery = "" }
                        )
                    }
                }

                // Profile DP Avatar next to search bar
                val profilePicture = userEntity?.profilePicture
                val avatarSize = tokens.buttonHeight * 0.95f

                if (!profilePicture.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(profilePicture)
                            .crossfade(true)
                            .memoryCachePolicy(CachePolicy.DISABLED)
                            .diskCachePolicy(CachePolicy.DISABLED)
                            .build(),
                        contentDescription = "Profile",
                        modifier = Modifier
                            .size(avatarSize)
                            .clip(CircleShape)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { onProfileClick() },
                        contentScale = ContentScale.Crop
                    )
                } else {
                    val initials = buildString {
                        userEntity?.firstName?.firstOrNull()?.let { append(it.uppercaseChar()) }
                        userEntity?.lastName?.firstOrNull()?.let { append(it.uppercaseChar()) }
                    }.ifBlank { "U" }

                    Box(
                        modifier = Modifier
                            .size(avatarSize)
                            .clip(CircleShape)
                            .background(Primary)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { onProfileClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            color = whiteBg,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = tokens.bodySmall
                        )
                    }
                }
            }
        }

        HorizontalDivider(color = sectionBorder, thickness = 1.dp)

        // Navigation Results Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "NAVIGATION RESULTS",
                fontSize = tokens.label,
                fontWeight = FontWeight.SemiBold,
                color = mutedText
            )
            Text(
                text = if (searchQuery.trim().isNotEmpty()) {
                    "${filteredResults.size} found"
                } else {
                    "Start typing..."
                },
                fontSize = tokens.label,
                fontWeight = FontWeight.Medium,
                color = mutedText
            )
        }

        HorizontalDivider(color = grey_border, thickness = 1.dp)

        // Results / Empty Prompt / Not Found Body
        when {
            searchQuery.trim().isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = tokens.screenPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(tokens.buttonHeight * 1.25f)
                                .clip(CircleShape)
                                .background(primary_light),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(tokens.iconSize * 1.3f)
                            )
                        }
                        Text(
                            text = "Search any module, settings or pages...",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = mutedText,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            filteredResults.isNotEmpty() -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        horizontal = tokens.screenPadding,
                        vertical = tokens.extraPadding
                    ),
                    verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f)
                ) {
                    items(
                        items = filteredResults,
                        key = { it.id }
                    ) { item ->
                        SearchSuggestionRow(
                            item = item,
                            onClick = { onPageSelected(item.routeKey) }
                        )
                    }
                }
            }

            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = tokens.screenPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(tokens.buttonHeight * 1.35f)
                                .clip(CircleShape)
                                .background(redBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SearchOff,
                                contentDescription = "No results",
                                tint = redText,
                                modifier = Modifier.size(tokens.iconSize * 1.4f)
                            )
                        }
                        Text(
                            text = "Cannot find \"$searchQuery\"",
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = title_color,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "We couldn't find any page matching your search. Please check the spelling or try another keyword.",
                            fontSize = tokens.bodySmall,
                            color = mutedText,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = tokens.screenPadding)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchSuggestionRow(
    item: SearchablePageItem,
    onClick: () -> Unit
) {
    val tokens = LocalAppTokens.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.8f))
            .clickable { onClick() },
        color = whiteBg,
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.8f),
        border = BorderStroke(1.dp, grey_border)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = tokens.extraPadding * 1.1f, vertical = tokens.extraPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
        ) {
            // Left Module Icon Container
            Box(
                modifier = Modifier
                    .size(tokens.buttonHeight * 0.85f)
                    .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                    .background(light_grey),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Layers,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(tokens.iconSize * 0.9f)
                )
            }

            // Title and Breadcrumb
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.2f)
            ) {
                Text(
                    text = item.title,
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = title_color,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.breadcrumb,
                    fontSize = tokens.caption,
                    color = mutedText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Right Module Pill and Navigate Arrow
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.6f)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(tokens.cardCornerRadius))
                        .background(light_grey)
                        .padding(horizontal = tokens.extraPadding * 0.8f, vertical = tokens.extraPadding * 0.3f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.module,
                        fontSize = tokens.label,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary,
                        maxLines = 1
                    )
                }

                Icon(
                    imageVector = Icons.Default.NorthEast,
                    contentDescription = "Open",
                    tint = mutedText,
                    modifier = Modifier.size(tokens.iconSize * 0.85f)
                )
            }
        }
    }
}