@file:Suppress("unused", "SpellCheckingInspection", "UNUSED_PARAMETER", "AssignedValueIsNeverRead")

package com.cuso.tailor.view.home.inventory.settings

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.R
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.settings.DocumentTemplateDto
import com.cuso.tailor.model.settings.DocumentType
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.AppUnderlineTabRow
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.view.composable.dashedBorder
import com.cuso.tailor.viewmodel.SettingsViewModel

enum class DocType(val label: String, val apiValue: String) {
    BILL("Bill", DocumentType.BILL),
    PAYMENT_RECEIPT("Payment Receipt", DocumentType.PAYMENT_RECEIPT),
    PURCHASE_RECEIVE("Purchase Receive", DocumentType.PURCHASE_RECEIVE),
    SALES_ORDER("Sales Order", DocumentType.SALES_ORDER),
    PACKING_SLIP("Packing Slip", DocumentType.PACKING_SLIP),
    DELIVERY_SLIP("Delivery Slip", DocumentType.DELIVERY_SLIP),
    CREDIT_NOTE("Credit Note", DocumentType.CREDIT_NOTE),
    RETURN_RECEIPT("Return Receipt", DocumentType.RETURN_RECEIPT)
}

data class PdfTemplate(
    val id: String,
    val type: DocType,
    val name: String,
    val templateTheme: String,
    @DrawableRes val thumbnailRes: Int,
    val updatedAgo: String = "Updated recently",
    val isBuiltInDefault: Boolean = false,
    val rawDto: DocumentTemplateDto? = null
)

fun getThumbnailForTemplate(type: DocType, templateKey: String): Int {
    return when (type) {
        DocType.BILL -> {
            if (templateKey.equals("standard", ignoreCase = true)) R.drawable.ic_bill_standard else R.drawable.ic_bill_modern
        }
        DocType.PAYMENT_RECEIPT -> {
            if (templateKey.equals("standard", ignoreCase = true)) R.drawable.ic_receipt_standard else R.drawable.ic_receipt_modern
        }
        DocType.PURCHASE_RECEIVE -> {
            if (templateKey.equals("standard", ignoreCase = true)) R.drawable.ic_receive_standard else R.drawable.ic_receive_modern
        }
        else -> R.drawable.ic_bill_modern
    }
}

@Composable
fun PdfTemplatesScreen(
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    onClose: () -> Unit,
    onCreateNewTemplate: () -> Unit,
    onPreview: (PdfTemplate) -> Unit = {}
) {
    val tokens = LocalAppTokens.current

    val docTypes = remember { DocType.entries }
    val tabs = remember { docTypes.map { it.label } }
    var selectedTab by remember { mutableIntStateOf(0) }
    val currentType = docTypes[selectedTab]

    val remoteTemplates by settingsViewModel.documentTemplates.collectAsStateWithLifecycle()
    val isLoading by settingsViewModel.isLoadingTemplates.collectAsStateWithLifecycle()
    val isSettingDefault by settingsViewModel.isSettingDefaultTemplate.collectAsStateWithLifecycle()

    LaunchedEffect(currentType) {
        settingsViewModel.fetchDocumentTemplates(docType = currentType.apiValue)
    }

    val templates: List<PdfTemplate> = remember(remoteTemplates, currentType) {
        remoteTemplates.map { dto ->
            val theme = if (dto.templateKey.equals("standard", ignoreCase = true)) "standard" else "modern"
            PdfTemplate(
                id = dto.id,
                type = currentType,
                name = dto.name,
                templateTheme = theme,
                thumbnailRes = getThumbnailForTemplate(currentType, dto.templateKey),
                updatedAgo = "Updated recently",
                isBuiltInDefault = dto.isDefault,
                rawDto = dto
            )
        }
    }

    val defaultTemplate = templates.firstOrNull { it.isBuiltInDefault }

    var previewTemplate by remember { mutableStateOf<PdfTemplate?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        TitleBar(title = "PDF Templates", onClose = onClose)

        Spacer(Modifier.height(8.dp))

        AppUnderlineTabRow(
            tabs = tabs,
            selectedIndex = selectedTab,
            onTabSelected = { selectedTab = it },
            isScrollable = true
        )

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(tokens.screenPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (templates.isNotEmpty()) {
                    item {
                        OutlinedButton(
                            onClick = {
                                defaultTemplate?.let {
                                    previewTemplate = it
                                    onPreview(it)
                                }
                            },
                            enabled = defaultTemplate != null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(tokens.buttonHeight),
                            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.45f),
                            border = BorderStroke(1.dp, Primary)
                        ) {
                            Icon(
                                Icons.Default.RemoveRedEye,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(tokens.iconSize * 0.9f)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Preview Default Layout",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = Primary
                            )
                        }
                    }
                }

                if (templates.isEmpty()) {
                    item {
                        Text(
                            text = "No templates yet for ${currentType.label}.",
                            fontSize = tokens.bodySmall,
                            color = mutedText,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp)
                        )
                    }
                } else {
                    items(templates, key = { it.id }) { template ->
                        TemplateCard(
                            name = template.name,
                            updatedAgo = template.updatedAgo,
                            isDefault = template.isBuiltInDefault,
                            thumbnailRes = template.thumbnailRes,
                            isSettingDefault = isSettingDefault,
                            onSetAsDefault = {
                                settingsViewModel.setDefaultTemplate(
                                    docType = currentType.apiValue,
                                    templateTheme = template.templateTheme
                                )
                            }
                        )
                    }
                }

                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.6f))
                            .dashedBorder(
                                color = Primary,
                                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.6f)
                            )
                            .clickable { onCreateNewTemplate() }
                            .padding(vertical = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(primary_light),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_document),
                                    contentDescription = null,
                                    tint = Primary,
                                    modifier = Modifier.size(tokens.iconSize)
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Create New Template",
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = Primary
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "Start from scratch or a blank slate.",
                                fontSize = tokens.caption,
                                color = close_color
                            )
                        }
                    }
                }
            }
        }
    }

    previewTemplate?.let { template ->
        TemplatePreviewDialog(
            template = template,
            onDismiss = { previewTemplate = null }
        )
    }
}

@Composable
fun TemplatePreviewDialog(
    template: PdfTemplate,
    onDismiss: () -> Unit
) {
    val tokens = LocalAppTokens.current

    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 4f)
        offset = if (scale == 1f) Offset.Zero else offset + panChange
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(title_color.copy(alpha = 0.95f))
        ) {
            Image(
                painter = painterResource(id = template.thumbnailRes),
                contentDescription = "${template.name} preview",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(top = 56.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    )
                    .transformable(state = transformState)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = template.name,
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = whiteBg,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(whiteBg.copy(alpha = 0.15f))
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close preview",
                        tint = whiteBg,
                        modifier = Modifier.size(tokens.iconSize)
                    )
                }
            }
        }
    }
}

@Composable
fun TemplateCard(
    name: String,
    updatedAgo: String,
    thumbnailRes: Int,
    isDefault: Boolean,
    isSettingDefault: Boolean = false,
    onSetAsDefault: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.6f),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        border = BorderStroke(1.dp, sectionBorder)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = thumbnailRes),
                contentDescription = "$name thumbnail",
                modifier = Modifier.size(width = 60.dp, height = 75.dp)
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = name,
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )

                    if (isDefault) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Selected",
                            tint = Primary,
                            modifier = Modifier.size(tokens.iconSize)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = iconMuted,
                            modifier = Modifier.size(tokens.iconSize)
                        )
                    }
                }

                Text(
                    text = updatedAgo,
                    fontSize = tokens.caption,
                    color = mutedText
                )
                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isDefault) greenBg else background_light_purple)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isDefault) "SELECTED" else "CUSTOM",
                            fontSize = tokens.label,
                            fontWeight = FontWeight.Medium,
                            color = if (isDefault) greentext else Primary
                        )
                    }

                    if (isDefault) {
                        Text(
                            text = "Default Template ✓",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = greentext
                        )
                    } else {
                        Text(
                            text = if (isSettingDefault) "Setting..." else "Set as Default",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = Primary,
                            modifier = Modifier.clickable(enabled = !isSettingDefault) {
                                onSetAsDefault()
                            }
                        )
                    }
                }
            }
        }
    }
}