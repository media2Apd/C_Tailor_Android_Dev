package com.cuso.tailor.view.home.security.support.help_center

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.viewmodel.SecurityViewModel

@Composable
fun HelpArticleDetailScreen(
    articleSlug: String,
    viewModel: SecurityViewModel,
    onBack: () -> Unit,
    onCreateTicketClick: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val article by viewModel.selectedHelpArticle.collectAsState()
    val isLoading by viewModel.isHelpCenterLoading.collectAsState()

    var userVote by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(articleSlug) {
        if (articleSlug.isNotBlank()) {
            viewModel.fetchHelpArticleBySlug(articleSlug)
        }
    }

    Scaffold(
        containerColor = Primary_background,
        topBar = {
            TitleBar(
                title = article?.title ?: "Article Details",
                subtitle = "Back To Help Center",
                onClose = onBack
            )
        }
    ) { innerPadding ->
        if (isLoading || article == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
            val a = article!!

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header overview section
                Surface(
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    color = whiteBg,
                    border = BorderStroke(1.dp, grey_border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(tokens.screenPadding),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(primary_light)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(a.category.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Primary)
                        }

                        Text(a.title, fontSize = tokens.bodyLarge, fontWeight = FontWeight.Bold, color = title_color)

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(light_grey),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = mutedText, modifier = Modifier.size(16.dp))
                            }
                            Column {
                                Text("Written by ${a.author ?: "Security Ops Team"}", fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = TextPrimary)
                                Text("Last updated: ${a.updatedAt?.take(16)?.replace('T', ' ') ?: "Today"}", fontSize = 10.sp, color = mutedText)
                            }
                        }

                        if (a.summary.isNotBlank()) {
                            HorizontalDivider(color = grey_border)
                            Text(a.summary, fontSize = tokens.bodySmall, color = TextSecondary, lineHeight = 20.sp)
                        }
                    }
                }

                // Render dynamic sections from API
                a.sections.forEachIndexed { index, section ->
                    Surface(
                        shape = RoundedCornerShape(tokens.cardCornerRadius),
                        color = whiteBg,
                        border = BorderStroke(1.dp, grey_border),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(tokens.screenPadding),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "${index + 1}. ${section.heading}",
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Primary
                            )

                            if (!section.body.isNullOrBlank()) {
                                Text(section.body, fontSize = tokens.bodySmall, color = TextSecondary)
                            }

                            if (section.steps.isNotEmpty()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Primary_background)
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    section.steps.forEachIndexed { i, step ->
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .clip(CircleShape)
                                                    .background(primary_light),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("${i + 1}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Primary)
                                            }
                                            Text(step, fontSize = tokens.caption, color = TextPrimary)
                                        }
                                    }
                                }
                            }

                            if (!section.note.isNullOrBlank()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(primary_light)
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(Icons.Default.Info, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                                    Text(
                                        text = "Note: ${section.note}",
                                        fontSize = tokens.caption,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                // Was this helpful feedback section
                Surface(
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    color = whiteBg,
                    border = BorderStroke(1.dp, grey_border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(tokens.screenPadding),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Was this helpful?", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary)

                        if (userVote == null) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        viewModel.voteHelpArticle(a.id, true) { userVote = true }
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, sectionBorder),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.ThumbUp, contentDescription = null, tint = greentext, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Yes", fontSize = tokens.caption, color = TextPrimary)
                                }

                                OutlinedButton(
                                    onClick = {
                                        viewModel.voteHelpArticle(a.id, false) { userVote = false }
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, sectionBorder),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.ThumbDown, contentDescription = null, tint = redText, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("No", fontSize = tokens.caption, color = TextPrimary)
                                }
                            }
                        } else {
                            Text("Thanks for your feedback.", fontSize = tokens.caption, color = greentext, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                // Create Ticket fallback
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
                            Text("Create a support ticket with Security Ops.", fontSize = tokens.caption, color = TextSecondary)
                        }
                        Button(
                            onClick = onCreateTicketClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f)
                        ) {
                            Text("Create Ticket", color = whiteBg, fontSize = tokens.caption, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}