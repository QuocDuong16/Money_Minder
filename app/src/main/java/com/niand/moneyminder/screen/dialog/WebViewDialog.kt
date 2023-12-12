package com.niand.moneyminder.screen.dialog

import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun WebViewDialog(url: String, onDismiss: (currentUrl: String) -> Unit) {
    val context = LocalContext.current
    var webView: WebView? by remember { mutableStateOf(null) }
    var currentUrl by remember { mutableStateOf(url) }

    var isLoading by remember { mutableStateOf(true) }

    DisposableEffect(context) {
        // Initialize WebView
        val newWebView = WebView(context).apply {
            settings.javaScriptEnabled = true
            settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    request?.url?.toString()?.let {
                        currentUrl = it
                    }
                    return super.shouldOverrideUrlLoading(view, request)
                }
            }
            loadUrl(url)
        }

        webView = newWebView

        onDispose {
            // Cleanup WebView when the composable is disposed
            newWebView.destroy()
        }
    }

    if (currentUrl != url) {
        isLoading = false
    }

    AlertDialog(
        onDismissRequest = { },
        title = { Text(text = "WebView Dialog to get Category") },
        text = {
            AndroidView({ webView!! }) { view ->
                // No-op, AndroidView will take care of rendering WebView
            }
        },
        buttons = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                if (!isLoading) {
                    Button(onClick = {
                        onDismiss(currentUrl)
                    }) {
                        Text(text = "Close")
                    }
                }
            }
        },
        modifier = Modifier.width(IntrinsicSize.Max)
    )
}