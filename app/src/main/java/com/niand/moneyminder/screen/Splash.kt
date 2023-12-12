package com.niand.moneyminder.screen

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.niand.moneyminder.R
import com.niand.moneyminder.ui.theme.MoneyMinderTheme


@Composable
fun SplashScreen() {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        Card (
            shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier.size(160.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_moneyminderapp),
                contentDescription = "App logo",
                modifier = Modifier.background(Color.Transparent)
            )
        }

        val customFontFamily = FontFamily(
            Font(R.font.sedgwick_ave_display)
        )

        Text(
            text = stringResource(id = R.string.app_name),
            style = TextStyle(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 38.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = customFontFamily
            )
        )
    }
}

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "DefaultPreviewDark"
)
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    name = "DefaultPreviewLight"
)
@Composable
fun PreviewSplashScreen() {
    MoneyMinderTheme {
        SplashScreen()
    }
}