package com.niand.moneyminder.screen.model

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class AppViewModel : ViewModel() {
    var isDarkMode = mutableStateOf(false)
    var currency = mutableStateOf("đ")
}