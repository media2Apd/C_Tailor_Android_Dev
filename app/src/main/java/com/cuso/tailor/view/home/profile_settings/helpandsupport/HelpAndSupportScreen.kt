package com.cuso.tailor.view.home.profile_settings.helpandsupport

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.BorderGray
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.Primary_background
import com.cuso.tailor.ui.theme.TextPrimary
import com.cuso.tailor.ui.theme.TextSecondary
import com.cuso.tailor.ui.theme.activity_purple_bg
import com.cuso.tailor.ui.theme.light_blue_border
import com.cuso.tailor.ui.theme.mutedText
import com.cuso.tailor.ui.theme.primary_light
import com.cuso.tailor.ui.theme.sectionBorder
import com.cuso.tailor.ui.theme.title_border
import com.cuso.tailor.ui.theme.title_color
import com.cuso.tailor.ui.theme.whiteBg
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.view.composable.DataCard
import com.cuso.tailor.view.composable.DataCardImage
import com.cuso.tailor.view.composable.SearchFilterBar
import com.cuso.tailor.view.composable.TitleBar

private const val SUPPORT_EMAIL = "support@cusotechnologies.in"

data class HelpArticleItem(
    val id: String,
    val title: String,
    val category: String,
    val summary: String,
    val steps: List<String>,
    val icon: ImageVector
)

/**
 * Help Center screen displaying a greeting header, searchable Popular Articles
 * using DataCard, and an Email Support action in the bottom bar.
 */
@Composable
fun HelpCenterScreen(
    onClose: () -> Unit,
    onArticleSelected: (HelpArticleItem) -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current

    var searchQuery by remember { mutableStateOf("") }
    var expandedArticleId by remember { mutableStateOf<String?>(null) }

    val popularArticles = remember {
        listOf(
            HelpArticleItem(
                id = "orders_measurements",
                title = "Creating Orders & Recording Measurements",
                category = "Sales",
                summary = "Step-by-step guide to capturing customer measurements and confirming orders.",
                steps = listOf(
                    "Open Sales and select or create a customer profile.",
                    "Record garment measurements using your configured templates.",
                    "Proceed to Order Review to verify pricing, delivery date, and payment."
                ),
                icon = Icons.AutoMirrored.Outlined.ReceiptLong
            ),
            HelpArticleItem(
                id = "inventory_warehouses",
                title = "Managing Warehouses, Stock & Bins",
                category = "Inventory",
                summary = "Configure warehouse hierarchy and monitor low-stock reorder alerts.",
                steps = listOf(
                    "Go to Module Settings -> Inventory -> Warehouse Structure.",
                    "Set up floors, sections, racks, and bins for organized stock placement.",
                    "Enable Auto Reorder Settings to receive low-stock notifications."
                ),
                icon = Icons.Outlined.Inventory2
            ),
            HelpArticleItem(
                id = "hr_attendance_leave",
                title = "Employee Onboarding, Attendance & Leave",
                category = "HR",
                summary = "Onboard staff members, assign shifts, and approve leave requests.",
                steps = listOf(
                    "Navigate to HR -> All Employees and tap Add Employee.",
                    "Assign a department, shift, and role-based access scope.",
                    "Review daily attendance and pending leave requests from the HR dashboard."
                ),
                icon = Icons.Outlined.People
            ),
            HelpArticleItem(
                id = "finance_gst_invoices",
                title = "GST Configuration, Invoices & Receipts",
                category = "Finance",
                summary = "Set up tax rates, generate sales invoices, and record customer payments.",
                steps = listOf(
                    "Configure GST registration and tax rules under Finance Settings.",
                    "Generate invoices directly from completed sales orders.",
                    "Record incoming payments and reconcile bank statements."
                ),
                icon = Icons.Outlined.AccountBalance
            ),
            HelpArticleItem(
                id = "logistics_couriers",
                title = "Courier Partners & Shipment Tracking",
                category = "Logistics",
                summary = "Add manual or Shiprocket couriers and track live delivery milestones.",
                steps = listOf(
                    "Open Logistics -> Courier Partners to add or sync couriers.",
                    "Configure service types, coverage zones, and primary hubs.",
                    "Track active shipments and update delivery statuses in real time."
                ),
                icon = Icons.Outlined.LocalShipping
            ),
            HelpArticleItem(
                id = "security_roles",
                title = "Roles, Permissions & Account Security",
                category = "Security",
                summary = "Control module visibility and branch-level access for your team.",
                steps = listOf(
                    "Open Security -> Roles to define view, create, edit, and delete permissions.",
                    "Assign organization or regional branch scopes to each role.",
                    "Monitor configuration updates using Audit Logs."
                ),
                icon = Icons.Outlined.VerifiedUser
            )
        )
    }

    val filteredArticles = remember(searchQuery, popularArticles) {
        val query = searchQuery.trim()
        if (query.isEmpty()) {
            popularArticles
        } else {
            popularArticles.filter { article ->
                article.title.contains(query, ignoreCase = true) ||
                        article.category.contains(query, ignoreCase = true) ||
                        article.summary.contains(query, ignoreCase = true)
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Primary_background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TitleBar(
                    title = "Help Center",
                    onClose = onClose
                )
                HorizontalDivider(color = title_border)
            }
        },
        bottomBar = {
            // Fixed bottom bar with Email Support action
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = whiteBg,
                border = BorderStroke(1.dp, sectionBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(
                            horizontal = tokens.screenPadding,
                            vertical = tokens.extraPadding
                        )
                ) {
                    Button(
                        onClick = {
                            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                data = "mailto:$SUPPORT_EMAIL".toUri()
                                putExtra(Intent.EXTRA_EMAIL, arrayOf(SUPPORT_EMAIL))
                                putExtra(Intent.EXTRA_SUBJECT, "CUSO Tailor Support Request")
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            try {
                                context.startActivity(emailIntent)
                            } catch (_: Exception) {
                                DynamicIslandManager.showError("No email app found. Contact $SUPPORT_EMAIL")
                            }
                        },
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.6f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary,
                            contentColor = whiteBg
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(tokens.buttonHeight)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Email Support",
                            tint = whiteBg,
                            modifier = Modifier.size(tokens.iconSize)
                        )
                        Spacer(modifier = Modifier.width(tokens.extraPadding * 0.8f))
                        Text(
                            text = "Email Support",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = whiteBg
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(vertical = tokens.extraPadding),
            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
        ) {
            // Greeting Header Card
            item(key = "greeting_header") {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = tokens.screenPadding),
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    color = activity_purple_bg,
                    border = BorderStroke(1.dp, light_blue_border)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(tokens.screenPadding)
                    ) {
                        Text(
                            text = "Hello there \uD83D\uDC4B",
                            fontSize = tokens.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = title_color
                        )
                        Spacer(modifier = Modifier.height(tokens.extraPadding * 0.3f))
                        Text(
                            text = "How can we help today?",
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Normal,
                            color = TextSecondary
                        )
                    }
                }
            }

            // Search Bar using reusable SearchFilterBar
            item(key = "search_bar") {
                SearchFilterBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search help articles...",
                    isSearchBarAlone = true,
                    accentColor = Primary,
                    borderColor = BorderGray,
                    textSecondaryColor = TextSecondary,
                    height = tokens.fieldHeight
                )
            }

            // Section Title: Popular Articles
            item(key = "popular_articles_header") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = tokens.screenPadding),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Popular Articles",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = title_color
                    )
                    Text(
                        text = "${filteredArticles.size} Articles",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Medium,
                        color = mutedText
                    )
                }
            }

            if (filteredArticles.isEmpty()) {
                item(key = "empty_articles") {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = tokens.screenPadding),
                        shape = RoundedCornerShape(tokens.cardCornerRadius),
                        color = whiteBg,
                        border = BorderStroke(1.dp, sectionBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(tokens.cardPadding),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No matching articles found",
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = title_color
                            )
                            Spacer(modifier = Modifier.height(tokens.extraPadding * 0.4f))
                            Text(
                                text = "Try a different keyword or tap Email Support below to reach our team.",
                                fontSize = tokens.caption,
                                color = mutedText
                            )
                        }
                    }
                }
            } else {
                // Popular Articles rendered with reusable DataCard
                items(filteredArticles, key = { it.id }) { article ->
                    val isExpanded = expandedArticleId == article.id

                    DataCard(
                        item = article,
                        image = DataCardImage(
                            vector = article.icon,
                            size = tokens.fieldHeight,
                            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                            backgroundColor = primary_light,
                            tint = Primary
                        ),
                        title = article.title,
                        subtitle = article.summary,
                        topBadgeText = article.category,
                        topBadgeBgColor = primary_light,
                        topBadgeTextColor = Primary,
                        topBadgeShowDot = false,
                        topBadgeInline = true,
                        showChevron = true,
                        chevronExpanded = isExpanded,
                        onChevronClick = {
                            expandedArticleId = if (isExpanded) null else article.id
                        },
                        onClick = {
                            expandedArticleId = if (isExpanded) null else article.id
                            onArticleSelected(article)
                        },
                        content = if (isExpanded) {
                            {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.6f))
                                        .background(Primary_background)
                                        .padding(tokens.extraPadding),
                                    verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.6f)
                                ) {
                                    article.steps.forEachIndexed { index, step ->
                                        Row(
                                            verticalAlignment = Alignment.Top,
                                            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(tokens.iconSize * 1.1f)
                                                    .clip(CircleShape)
                                                    .background(primary_light),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "${index + 1}",
                                                    fontSize = tokens.label,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Primary
                                                )
                                            }
                                            Text(
                                                text = step,
                                                fontSize = tokens.bodySmall,
                                                color = TextPrimary,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                            }
                        } else null
                    )
                }
            }
        }
    }
}