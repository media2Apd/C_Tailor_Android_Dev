package com.cuso.tailor.view.home.security.mobile_access.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.FormDropdown
import com.cuso.tailor.view.composable.MiniSwitch
import com.cuso.tailor.view.composable.StatusBadge
import com.cuso.tailor.viewmodel.SecurityViewModel

@Composable
fun DataSecurityTabContent(viewModel: SecurityViewModel) {
    val tokens = LocalAppTokens.current
    val policy by viewModel.mobileDataSecurityPolicy.collectAsState()

    val ss = policy.screenshotRestriction
    val dl = policy.downloadRestriction
    val enc = policy.localEncryption

    var downloadExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(tokens.screenPadding),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Screenshot Restriction
        Surface(
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            border = BorderStroke(1.dp, grey_border)
        ) {
            Column(modifier = Modifier.padding(tokens.screenPadding), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(primary_light), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
                        }
                        Column {
                            Text("Screenshot Restriction", fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("Prevent capturing sensitive screens natively.", fontSize = tokens.caption, color = TextSecondary)
                        }
                    }
                    MiniSwitch(
                        checked = ss.enabled,
                        onCheckedChange = {
                            viewModel.patchDataSecurityPolicy(mapOf("screenshotRestriction" to mapOf("enabled" to it)))
                        }
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Primary_background).padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("PROTECTED CONTEXTS", fontSize = tokens.caption, fontWeight = FontWeight.Bold, color = mutedText)
                    ss.protectedContexts.forEach { context ->
                        Text("• $context", fontSize = tokens.caption, color = TextPrimary)
                    }
                }
            }
        }

        // Download Restriction
        Surface(
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            border = BorderStroke(1.dp, grey_border)
        ) {
            Column(modifier = Modifier.padding(tokens.screenPadding), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(primary_light), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
                    }
                    Column {
                        Text("Download Restriction", fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Control saving files to local device storage.", fontSize = tokens.caption, color = TextSecondary)
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.weight(1f)) {
                        FormDropdown(
                            value = when (dl.level) {
                                "allow_all" -> "Allow All"
                                "allow_some" -> "Allow Some"
                                else -> "Restrict All"
                            },
                            expanded = downloadExpanded,
                            onExpandChange = { downloadExpanded = it },
                            options = listOf("Restrict All", "Allow Some", "Allow All"),
                            onOptionSelected = {
                                val level = when (it) {
                                    "Allow All" -> "allow_all"
                                    "Allow Some" -> "allow_some"
                                    else -> "restrict_all"
                                }
                                viewModel.patchDataSecurityPolicy(mapOf("downloadRestriction" to mapOf("level" to level)))
                            }
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    StatusBadge(
                        text = if (dl.level != "allow_all") "Protected" else "Not protected",
                        bgColor = if (dl.level != "allow_all") greenBg else redBg,
                        textColor = if (dl.level != "allow_all") greentext else redText
                    )
                }
            }
        }

        // Local Data Encryption
        Surface(
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            border = BorderStroke(1.dp, grey_border)
        ) {
            Column(modifier = Modifier.padding(tokens.screenPadding), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(primary_light), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
                        }
                        Column {
                            Text("Local Data Encryption", fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("${enc.algorithm} encryption for cached app data.", fontSize = tokens.caption, color = TextSecondary)
                        }
                    }
                    MiniSwitch(
                        checked = enc.enabled,
                        onCheckedChange = {
                            viewModel.patchDataSecurityPolicy(mapOf("localEncryption" to mapOf("enabled" to it)))
                        }
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    StatusBadge(
                        text = if (enc.enabled) "Protected" else "Not protected",
                        bgColor = if (enc.enabled) greenBg else redBg,
                        textColor = if (enc.enabled) greentext else redText
                    )
                }
            }
        }
    }
}