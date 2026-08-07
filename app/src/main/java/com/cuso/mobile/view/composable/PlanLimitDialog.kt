package com.cuso.mobile.view.composable

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cuso.mobile.ui.theme.blackTitle
import com.cuso.mobile.ui.theme.whiteBg

// ─────────────────────────────────────────────────────────────
// PlanLimits — single shared data class for ALL modules
// (Branch, Department, Employee, Order, Category ...)
// ─────────────────────────────────────────────────────────────
data class PlanLimits(
    val branchLimit: Int,
    val departmentLimit: Int,
    val employeeLimit: Int,
    val orderLimit: Int,
    val categoryLimit: Int
)

// ─────────────────────────────────────────────────────────────
// PlanLimitDialog — single shared dialog for ALL modules.
// Just pass the title/message you want, works for any screen.
// ─────────────────────────────────────────────────────────────
@Composable
fun PlanLimitDialog(
    title: String = "Plan Limit Reached",
    message: String,
    onDismiss: () -> Unit,
    onUpgrade: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(blackTitle.copy(alpha = 0.3f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.TopCenter
        ) {
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(
                    initialOffsetY = { -it },
                    animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(durationMillis = 300)),
                exit = slideOutVertically(
                    targetOffsetY = { -it },
                    animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(durationMillis = 200))
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 40.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { /* prevent click through */ },
                    colors = CardDefaults.cardColors(containerColor = whiteBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF4444),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = message,
                            fontSize = 14.sp,
                            color = Color(0xFF6B7280),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = onDismiss,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFFD1D5DB)),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = whiteBg,
                                    contentColor = Color(0xFF374151)
                                ),
                                modifier = Modifier.weight(0.4f)
                            ) {
                                Text("Close", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF374151))
                            }

                            Button(
                                onClick = onUpgrade,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF3B3BF9),
                                    contentColor = whiteBg
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(0.6f)
                            ) {
                                Text("Upgrade Plan", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = whiteBg)
                            }
                        }
                    }
                }
            }
        }
    }
}