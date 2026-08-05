package com.cuso.mobile.view.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.mobile.R
import com.cuso.mobile.ui.theme.blackTitle

@Composable
fun OtpTitle(){
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {

        Text(
            " Verify to ",Modifier,
            style = MaterialTheme.typography.headlineLarge,
            fontSize = 30.sp,
            color = blackTitle,

            )
        Spacer(Modifier.padding(bottom=10.dp))
        Text(
            buildAnnotatedString {
                withStyle(
                    style = SpanStyle(color = colorResource(R.color.logpPrimary))
                ) {
                    append("C")
                }

                withStyle(
                    style = SpanStyle(color = colorResource(R.color.logpHighlight))
                ) {
                    append("U")
                }

                withStyle(
                    style = SpanStyle(color = colorResource(R.color.logpAccent))
                ) {
                    append("S")
                }

                withStyle(
                    style = SpanStyle(color = colorResource(R.color.logpSecondary))
                ) {
                    append("O")
                }
            },
            fontSize = 30.sp,
            letterSpacing = 0.sp,
            style = MaterialTheme.typography.headlineLarge
        )
        Text(
            " Account ",Modifier,
            style = MaterialTheme.typography.headlineLarge,
            fontSize = 30.sp,
            color = blackTitle,

            )
    }
}

