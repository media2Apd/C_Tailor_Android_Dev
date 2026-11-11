package com.cuso.tailor.view.home.security.support.help_center

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.security.HelpArticleItem
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.viewmodel.SecurityViewModel

data class HelpCategoryOption(
    val label: String,
    val icon: ImageVector,
    val isDanger: Boolean = false
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HelpCenterScreen(
    viewModel: SecurityViewModel,
    onBack: () -> Unit,
    onArticleClick: (HelpArticleItem) -> Unit,
    onCreateTicketClick: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val articles by viewModel.helpArticles.collectAsState()
    val isLoading by viewModel.isHelpCenterLoading.collectAsState()
    val searchQuery by viewModel.helpSearchQuery.collectAsState()
    val selectedCategory by viewModel.selectedHelpCategory.collectAsState()
    val showAll by viewModel.showAllArticles.collectAsState()

    var searchInput by remember { mutableStateOf(searchQuery) }

    val categories = remember {
        listOf(
            HelpCategoryOption("Login & Authentication", Icons.Default.Lock),
            HelpCategoryOption("Account Security", Icons.Default.Shield),
            HelpCategoryOption("API & Integrations", Icons.Default.Key),
            HelpCategoryOption("Critical Process", Icons.Default.Warning, isDanger = true),
            HelpCategoryOption("Data Protection", Icons.Default.FolderShared)
        )
    }

    LaunchedEffect(Unit) {
        viewModel.fetchHelpArticles()
    }

    Scaffold(
        containerColor = Primary_background,
        topBar = {
            TitleBar(
                title = "Help Center",
                onClose = onBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(tokens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // --- Custom Search Field with Attached Button ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(tokens.cardCornerRadius))
                    .background(whiteBg)
                    .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = mutedText,
                        modifier = Modifier.size(18.dp)
                    )
                    BasicTextField(
                        value = searchInput,
                        onValueChange = { searchInput = it },
                        singleLine = true,
                        cursorBrush = SolidColor(Primary),
                        textStyle = TextStyle(fontSize = tokens.bodySmall, color = TextPrimary),
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { innerTextField ->
                            if (searchInput.isEmpty()) {
                                Text(
                                    text = "What can we help you with?",
                                    fontSize = tokens.bodySmall,
                                    color = mutedText
                                )
                            }
                            innerTextField()
                        }
                    )
                }

                Button(
                    onClick = {
                        viewModel.onHelpSearchChange(searchInput.trim())
                        viewModel.fetchHelpArticles()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(topEnd = tokens.cardCornerRadius, bottomEnd = tokens.cardCornerRadius),
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(vertical = 4.dp, horizontal = 4.dp)
                ) {
                    Text("Search", color = whiteBg, fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold)
                }
            }

            // --- POPULAR CATEGORIES (Wrapping rows instead of scrolling) ---
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "POPULAR CATEGORIES",
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = selectedCategory.equals(cat.label, ignoreCase = true)

                        val (chipBg, borderColor, textColor, iconColor) = when {
                            isSelected -> listOf(Primary, Primary, whiteBg, whiteBg)
                            cat.isDanger -> listOf(redBg, Color(0xFFFCA5A5), redText, redText)
                            else -> listOf(whiteBg, sectionBorder, TextPrimary, Primary)
                        }

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(chipBg)
                                .border(1.dp, borderColor, RoundedCornerShape(20.dp))
                                .clickable { viewModel.onHelpCategorySelect(cat.label) }
                                .padding(horizontal = 14.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(cat.icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(14.dp))
                            Text(
                                text = cat.label,
                                fontSize = tokens.caption,
                                fontWeight = FontWeight.Medium,
                                color = textColor
                            )
                        }
                    }
                }
            }

            // --- Header Row for Articles Section ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val headerTitle = when {
                    searchQuery.isNotBlank() -> "Results for \"$searchQuery\""
                    selectedCategory.isNotBlank() -> selectedCategory
                    else -> "Featured Articles"
                }

                Text(
                    text = headerTitle,
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = title_color
                )

                if (searchQuery.isNotBlank() || selectedCategory.isNotBlank()) {
                    Text(
                        text = "Clear filters",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.SemiBold,
                        color = Primary,
                        modifier = Modifier.clickable {
                            searchInput = ""
                            viewModel.clearHelpFilters()
                        }
                    )
                } else if (!showAll) {
                    Text(
                        text = "View all",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.SemiBold,
                        color = Primary,
                        modifier = Modifier.clickable {
                            viewModel.setShowAllArticles(true)
                        }
                    )
                }
            }

            // --- Articles Grid / List ---
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primary, modifier = Modifier.size(28.dp))
                }
            } else if (articles.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    color = whiteBg,
                    border = BorderStroke(1.dp, grey_border),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (searchQuery.isNotBlank() || selectedCategory.isNotBlank())
                                "No articles match your search."
                            else
                                "No articles published yet.",
                            fontSize = tokens.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    articles.forEach { article ->
                        HelpArticleCard(
                            article = article,
                            onReadClick = { onArticleClick(article) }
                        )
                    }
                }
            }

            // --- Bottom Still Need Help Card ---
            Surface(
                shape = RoundedCornerShape(tokens.cardCornerRadius),
                color = primary_light,
                border = BorderStroke(1.dp, light_blue_border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(tokens.screenPadding),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Still need help?", fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Create a priority ticket with Security Ops.", fontSize = tokens.caption, color = TextSecondary)
                    }
                    Button(
                        onClick = onCreateTicketClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f)
                    ) {
                        Text("Open Ticket", color = whiteBg, fontSize = tokens.caption, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
fun HelpArticleCard(
    article: HelpArticleItem,
    onReadClick: () -> Unit
) {
    val tokens = LocalAppTokens.current

    Surface(
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        color = whiteBg,
        border = BorderStroke(1.dp, grey_border),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(tokens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Category & Helpful Percent
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(primary_light)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = article.category.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                }

                article.helpfulPercent?.let { percent ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.ThumbUp, contentDescription = null, tint = greentext, modifier = Modifier.size(12.dp))
                        Text("$percent% Helpful", fontSize = 11.sp, color = greentext, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Text(
                text = article.title,
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = title_color
            )

            Text(
                text = "Last updated: ${article.updatedAt?.take(10) ?: "Today"}",
                fontSize = tokens.caption,
                color = mutedText
            )

            Text(
                text = article.summary,
                fontSize = tokens.caption,
                color = TextSecondary,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            OutlinedButton(
                onClick = onReadClick,
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                border = BorderStroke(1.dp, Primary),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(tokens.buttonHeight * 0.9f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Read Article", fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold)
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}