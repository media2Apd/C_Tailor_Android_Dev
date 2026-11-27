package com.cuso.tailor.view.composable

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Texture
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.darkGreenBg
import com.cuso.tailor.view.home.sales.quotation.QuotationItemType
import com.cuso.tailor.view.home.sales.quotation.QuotationScopeItem

// ---------------------------------------------------------------------------
// Design constants (swap with your tokens / theme colors if you like)
// ---------------------------------------------------------------------------
private val MenuBorder = Color(0xFFE8EAF2)
private val MenuDivider = Color(0xFFF0F1F6)
private val TitleColor = Color(0xFF1F2437)
private val SubtitleColor = Color(0xFF6B7280)

/** One row inside the Add New Item menu. */
data class AddItemOption(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val tint: Color,
    val onClick: () -> Unit,
)

// ---------------------------------------------------------------------------
// Generic, reusable component: pass any list of options.
// ---------------------------------------------------------------------------
@Composable
fun AddItemMenuButton(
    options: List<AddItemOption>,
    modifier: Modifier = Modifier,
    label: String = "Add New Item",
    containerColor: Color = Primary, // <- your Primary
    menuColor: Color = Color.White,
) {
    var expanded by remember { mutableStateOf(false) }
    var anchorWidthPx by remember { mutableStateOf(0) }
    val density = LocalDensity.current
    val anchorWidth = with(density) { anchorWidthPx.toDp() }
    val chevronRotation by animateFloatAsState(if (expanded) 180f else 0f, label = "chevron")

    Box(
        modifier = modifier
            .fillMaxWidth()
            .onSizeChanged { anchorWidthPx = it.width }
    ) {
        // ---- Button -------------------------------------------------------
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(16.dp),
                    ambientColor = containerColor.copy(alpha = 0.3f),
                    spotColor = containerColor.copy(alpha = 0.3f),
                )
                .clip(RoundedCornerShape(16.dp))
                .clickable { expanded = !expanded },
            color = containerColor,
            shape = RoundedCornerShape(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp),
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    text = label,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                )
                Spacer(Modifier.width(6.dp))
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .size(20.dp)
                        .rotate(chevronRotation),
                )
            }
        }

        // ---- Menu (opens ABOVE the button) --------------------------------
        if (expanded) {
            Popup(
                popupPositionProvider = remember { AboveAnchorPositionProvider() },
                onDismissRequest = { expanded = false },
                properties = PopupProperties(focusable = true),
            ) {
                Column(
                    modifier = Modifier
                        .width(anchorWidth + 24.dp)
                        .padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = menuColor,
                        shadowElevation = 12.dp,
                        border = BorderStroke(1.dp, MenuBorder),
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            options.forEachIndexed { index, option ->
                                if (index > 0) {
                                    // inset divider: aligns with text, not the icon tile
                                    Box(
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(start = 66.dp, end = 12.dp)
                                            .height(1.dp)
                                            .background(MenuDivider)
                                    )
                                }
                                AddItemMenuRow(option) {
                                    expanded = false
                                    option.onClick()
                                }
                            }
                        }
                    }
                    MenuCaret(fill = menuColor)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Row
// ---------------------------------------------------------------------------
@Composable
private fun AddItemMenuRow(option: AddItemOption, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(option.tint.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                option.icon,
                contentDescription = null,
                tint = option.tint,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = option.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TitleColor,
            )
            Text(
                text = option.subtitle,
                fontSize = 12.5.sp,
                color = SubtitleColor,
            )
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = SubtitleColor,
            modifier = Modifier.size(20.dp),
        )
    }
}

// ---------------------------------------------------------------------------
// Little pointer under the menu card
// ---------------------------------------------------------------------------
@Composable
private fun MenuCaret(fill: Color) {
    Canvas(
        modifier = Modifier
            .size(width = 20.dp, height = 9.dp)
            .offset(y = (-1).dp)
    ) {
        val w = size.width
        val h = size.height
        val body = Path().apply {
            moveTo(0f, 0f)
            lineTo(w / 2f, h)
            lineTo(w, 0f)
            close()
        }
        drawPath(body, fill)
        val edge = Path().apply {
            moveTo(0f, 0f)
            lineTo(w / 2f, h)
            lineTo(w, 0f)
        }
        drawPath(
            edge,
            MenuBorder,
            style = Stroke(width = 1.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }
}

// ---------------------------------------------------------------------------
// Popup placement: centered above the button (flips below if no room)
// ---------------------------------------------------------------------------
private class AboveAnchorPositionProvider : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val x = (anchorBounds.left + (anchorBounds.width - popupContentSize.width) / 2)
            .coerceIn(0, (windowSize.width - popupContentSize.width).coerceAtLeast(0))
        val above = anchorBounds.top - popupContentSize.height
        val y = if (above >= 0) above else anchorBounds.bottom
        return IntOffset(x, y)
    }
}

// ---------------------------------------------------------------------------
// Quotation-specific wrapper — THIS is what you call in both places.
// ---------------------------------------------------------------------------
@Composable
fun QuotationAddItemButton(
    items: List<QuotationScopeItem>,
    onItemsChange: (List<QuotationScopeItem>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val options = listOf(
        AddItemOption(
            title = "Add Garment",
            subtitle = "Pick a garment type",
            icon = Icons.Default.Checkroom,
            tint = Primary,
            onClick = {
                onItemsChange(items + QuotationScopeItem(itemType = QuotationItemType.GARMENT))
            },
        ),
        AddItemOption(
            title = "Add Fabric",
            subtitle = "Choose the material",
            icon = Icons.Default.Texture,
            tint = Color(0xFF0D9488),
            onClick = {
                onItemsChange(
                    items + QuotationScopeItem(
                        itemType = QuotationItemType.FABRIC,
                        unit = "",
                        hsnCode = "",
                    )
                )
            },
        ),
        AddItemOption(
            title = "Add Item",
            subtitle = "Add a retail item",
            icon = Icons.Default.ShoppingBag,
            tint = darkGreenBg,
            onClick = {
                onItemsChange(
                    items + QuotationScopeItem(
                        itemType = QuotationItemType.RETAIL_ITEM,
                        unit = "",
                        hsnCode = "",
                    )
                )
            },
        ),
    )
    AddItemMenuButton(options = options, modifier = modifier)
}