package com.cuso.mobile.view.organization

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.cuso.mobile.R
import com.cuso.mobile.ui.theme.Primary
import com.cuso.mobile.ui.theme.PrimaryBorder
import androidx.core.net.toUri
import com.cuso.mobile.ui.theme.blackTitle
import com.cuso.mobile.ui.theme.grey_border
import com.cuso.mobile.ui.theme.whiteBg

@Composable
fun OrganizationNotFoundScreen(navController: NavController) {
    val context = LocalContext.current

    var isNavigating by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isNavigating = false
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(whiteBg)
            .padding(top=50.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 24.dp)
        ) {

            // Back + Logo Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.clickable { navController.popBackStack() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF374151),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Back",
                        color = Color(0xFF374151),
                        fontSize = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(60.dp))

            // Centered Logo + Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.cuso_logo), // unga diamond logo drawable
                    contentDescription = "Tailor Logo",
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Tailor",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = blackTitle
                )
            }

            Spacer(modifier = Modifier.height(42.dp))

            // Illustration circle
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.orgtanization_ilustration),
                    contentDescription = "Organization not found illustration",
                    modifier = Modifier.size(180.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Title
            Text(
                text = "Organization not found",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = blackTitle,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Description
            Text(
                text = "It looks like your organization hasn't been set up yet. Please complete your organization setup from the Web Dashboard.",
                fontSize = 14.sp,
                color = Color(0xFF6B7280),
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Open Web Dashboard button

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Primary)
                    .clickable(enabled = !isNavigating) {
                        isNavigating = true
                        try {
                            val intent = Intent(Intent.ACTION_VIEW,
                                "https://tailor.cuso.in/sign-up".toUri())
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            isNavigating = false
                        }
                    }
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Language,
                    contentDescription = null,
                    tint = whiteBg,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Open Web Dashboard",
                    color = whiteBg,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.weight(1f))

                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                    contentDescription = null,
                    tint = whiteBg,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // OR divider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(grey_border)
                )
                Text(
                    text = "OR",
                    color = Color(0xFF9CA3AF),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(grey_border)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Send setup link button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .border(1.dp, Color(0xFFDBEAFE), RoundedCornerShape(5.dp))
                    .clickable { }
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Email,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Send setup link to my email",
                    color = Primary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Bottom help footer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(12.dp),
                        ambientColor = blackTitle.copy(alpha = 0.1f),
                        spotColor = blackTitle.copy(alpha = 0.1f)
                    )
                    .clip(RoundedCornerShape(12.dp))
                    .background(whiteBg)
                    .border(1.dp, PrimaryBorder, RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEFF6FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.footer_person),
                        contentDescription = null,
                        modifier = Modifier.size(17.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Need help?",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = blackTitle
                    )
                    Text(
                        text = "Contact our support team and we'll help you get started.",
                        fontSize = 10.sp,
                        color = Color(0xFF6B7280),
                        lineHeight = 14.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    Modifier
                        .background(Color(0xFFEFF6FF), shape = RoundedCornerShape(5.dp))
                        .padding(horizontal = 10 .dp, vertical = 2.dp )
                ) {
                    Text(
                        text = "Contact Support",
                        color = Primary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable {  }
                    )
                }
            }
        }
    }
}