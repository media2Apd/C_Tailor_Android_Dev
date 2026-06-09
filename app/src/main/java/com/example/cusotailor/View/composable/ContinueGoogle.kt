package com.example.cusotailor.View.composable

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cusotailor.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException


@Composable
fun ContinueGoogle( activity: Activity) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)

            val name = account?.displayName
            val mail = account?.email

        } catch (e: ApiException) {
            e.printStackTrace()
        }
    }

    // Google Sign-In config
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .build()

    val googleSignInClient = GoogleSignIn.getClient(activity, gso)
    OutlinedButton(
        onClick = {
            val signInIntent = googleSignInClient.signInIntent
            launcher.launch(signInIntent)
        },
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Image(
            painter = painterResource(R.drawable.google),
            contentDescription = "Google icon",
            Modifier
                .padding(horizontal = 12.dp)
                .size(30.dp)
        )
        Text("Continue with Google", Modifier, color = Color.Black, fontSize = 18.sp)
    }
}

