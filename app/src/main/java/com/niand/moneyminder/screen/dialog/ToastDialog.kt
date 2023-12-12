package com.niand.moneyminder.screen.dialog

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun ToastSnackbar(message: String, durationMillis: Int = 3000, onDismiss: () -> Unit) {
    val snackbarHostState = remember { SnackbarHostState() }

    // Trigger the Snackbar on recomposition
    LaunchedEffect(message) {
        snackbarHostState.showSnackbar(message)

        // Delay for the specified durationMillis and then dismiss the Snackbar
        delay(durationMillis.toLong())
        onDismiss()
        snackbarHostState.currentSnackbarData?.dismiss()
    }

    // Show the Snackbar
    SnackbarHost(
        hostState = snackbarHostState,
        modifier = Modifier.padding(16.dp)
    ) {
        Snackbar(
            content = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = message)
                }
            }
        )
    }
}