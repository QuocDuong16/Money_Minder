package com.niand.moneyminder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.niand.moneyminder.screen.MoneyMinderApp
import com.niand.moneyminder.screen.Screen
import com.niand.moneyminder.screen.model.AppViewModel
import com.niand.moneyminder.ui.theme.MoneyMinderTheme
import com.niand.moneyminder.util.Constants.APP_ID
import io.realm.kotlin.mongodb.App

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val appViewModel: AppViewModel = viewModel()
            MoneyMinderTheme (
                useDarkTheme = appViewModel.isDarkMode.value
            ) {
                val navController = rememberNavController()
                MoneyMinderApp(
                    startDestination = getStartDestination(),
                    navController = navController
                )
            }
        }
    }
}

private fun getStartDestination(): String {
    val user = App.create(APP_ID).currentUser
    return if (user != null && user.loggedIn) Screen.Home.route
    else Screen.Welcome.route
}