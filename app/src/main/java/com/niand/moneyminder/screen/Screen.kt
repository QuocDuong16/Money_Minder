package com.niand.moneyminder.screen

sealed class Screen(val route: String) {
    object Welcome : Screen(route = "welcome_screen")
    object SignUp: Screen(route = "sign_up_screen")
    object SignIn: Screen(route = "sign_in_screen")
    object ForgotPassword: Screen(route = "forgot_password_screen")
    object Home : Screen(route = "home_screen")
    object Transactions : Screen(route = "transactions_screen")
    object AddTransaction : Screen(route = "add_transaction")
    object TransactionDetail : Screen(route = "transaction_detail")
    object Budgets : Screen(route = "budgets_screen")
    object AddBudget : Screen(route = "add_budget")
    object BudgetDetail : Screen(route = "budget_detail")
    object Profile : Screen(route = "profile_screen")
    object Settings : Screen(route = "settings_screen")
    object ConfirmEmailForSignUp: Screen(route = "confirm_email_for_sign_up")
    object ConfirmEmailForChangePassword: Screen(route = "confirm_email_for_change_password")
}