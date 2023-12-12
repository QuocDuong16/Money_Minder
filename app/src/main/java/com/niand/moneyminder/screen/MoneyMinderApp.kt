package com.niand.moneyminder.screen

import android.content.Intent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.niand.moneyminder.R
import com.niand.moneyminder.model.Budgets
import com.niand.moneyminder.model.Transactions
import com.niand.moneyminder.screen.dialog.NewPasswordDialog
import com.niand.moneyminder.screen.model.AuthenticationViewModel
import com.niand.moneyminder.screen.model.BudgetsViewModel
import com.niand.moneyminder.screen.model.CategoriesViewModel
import com.niand.moneyminder.screen.model.TransactionsViewModel
import com.niand.moneyminder.screen.model.UsersViewModel
import kotlinx.coroutines.delay
import org.mongodb.kbson.ObjectId
import java.util.Calendar

private const val delayMillis: Long = 1000 // 1 giây

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoneyMinderApp(startDestination: String, navController: NavHostController) {
    var showWelcomeScreen by remember { mutableStateOf(true) }
    Surface {
        if (showWelcomeScreen) {
            SplashScreen()
            LaunchedEffect(Unit) {
                delay(delayMillis)
                showWelcomeScreen = false
            }
        }
        else {
            NavHost(
                navController = navController,
                startDestination = startDestination
            ) {
                welcomeRoute(navController)
                // Chưa đăng nhập
                notLoggedInGraph(navController)

                // Sau khi đăng nhập thành công
                loggedInGraph(navController)

                // Deeplink
                deepLink(navController)
            }
        }
    }
}

fun NavGraphBuilder.welcomeRoute(
    navController: NavHostController
) {
    composable(route = Screen.Welcome.route) {
        WelcomeScreen(navController = navController)
    }
}

fun NavGraphBuilder.notLoggedInGraph(navController: NavHostController) {
    signUpRoute(navController)
    signInRoute(
        navController = navController,
        navigateToHome = {
            navController.popBackStack()
            navController.navigate(Screen.Home.route)
        },
    )
    forgotPasswordRoute(navController)
}

fun NavGraphBuilder.loggedInGraph(navController: NavHostController) {
    homeRoute(navController)
    transactionsRoute(navController)
    budgetsRoute(navController)
    profileRoute(navController)
    settingsRoute(navController)
}

fun NavGraphBuilder.deepLink(navController: NavHostController) {
    confirmEmailSignUpRoute(navController)
    confirmEmailForChangePasswordRoute(navController)
}

@OptIn(ExperimentalMaterial3Api::class)
fun NavGraphBuilder.homeRoute(
    navController: NavHostController
) {
    composable(route = Screen.Home.route) {
        val usersViewModel: UsersViewModel = viewModel()
        val transactionsViewModel: TransactionsViewModel = viewModel()
        val categoriesViewModel: CategoriesViewModel = viewModel()
        val total = getTotalAmount(transactionsViewModel.data.value)
        Scaffold(
            topBar = { TopBarInformation(usersViewModel.balance.longValue) },
            bottomBar = { SootheBottomNavigation(R.string.bottom_navigation_home, navController) },
            floatingActionButton = { AddTransactionOrBudget(navController) }
        ) { padding ->
            HomeScreen(
                Modifier
                    .padding(padding)
                    .fillMaxSize(),
                categoriesData = categoriesViewModel.data.value,
                getCategoriesById = categoriesViewModel::getCategoriesById,
                getTransactionDataByCategories = categoriesViewModel::getTransactionDataByCategories,
                totalSpending = total
            )
        }
    }
}

fun NavGraphBuilder.transactionsRoute(navController: NavHostController) {
    composable(route = Screen.Transactions.route) {
        val usersViewModel: UsersViewModel = viewModel()
        val transactionsViewModel: TransactionsViewModel = viewModel()
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1) // Đặt ngày là 1 để lấy ngày đầu tiên của tháng
        val firstDayOfMonth = calendar.timeInMillis.toRealmInstant()
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH)) // Đặt ngày là ngày cuối cùng của tháng
        val lastDayOfMonth = calendar.timeInMillis.toRealmInstant()
        val beforeThisMonth = transactionsViewModel.getDistinctTransactionTimestampBeforeMonth(firstDayOfMonth)
        val thisMonth = transactionsViewModel.getDistinctTransactionTimestampByMonth(firstDayOfMonth, lastDayOfMonth)
        val afterThisMonth = transactionsViewModel.getDistinctTransactionTimestampAfterMonth(lastDayOfMonth)
        Scaffold(
            topBar = { TopBarInformation(usersViewModel.balance.longValue) },
            bottomBar = { SootheBottomNavigation(R.string.bottom_navigation_transactions, navController) },
            floatingActionButton = { AddTransactionOrBudget(navController) }
        ) { padding ->
            TransactionScreen(
                Modifier
                    .padding(padding)
                    .fillMaxSize(),
                navController = navController,
                transactionsDataBeforeThisMonth = beforeThisMonth,
                transactionsDataThisMonth = thisMonth,
                transactionsDataAfterThisMonth = afterThisMonth,
                getTransactionByTimestamp = transactionsViewModel::getTransactionByTimestamp
            )
        }
    }
    composable(route = Screen.AddTransaction.route) {
        val transactionsViewModel: TransactionsViewModel = viewModel()
        val categoriesViewModel: CategoriesViewModel = viewModel()
        val usersViewModel: UsersViewModel = viewModel()
        TransactionInputScreen(
            navController = navController,
            getType = transactionsViewModel::getType,
            getCategoriesByName = categoriesViewModel::getCategoriesByName,
            onAmountChange = transactionsViewModel::updateAmount,
            onNameChange = transactionsViewModel::updateName,
            onDescriptionChange = transactionsViewModel::updateDescription,
            onTimestampChange = transactionsViewModel::updateTimestamp,
            onCategoriesChange = transactionsViewModel::updateCategories,
            onBalanceChange = transactionsViewModel::updateBalance,
            insertTransactions = transactionsViewModel::insertData,
            updateBalanceUser = transactionsViewModel::updateBalanceUser,
            updateBalance = usersViewModel::updateBalance,
            updateUsers = usersViewModel::updateData
        )
    }
    composable(
        route = Screen.TransactionDetail.route + "/{id}",
        arguments = listOf(
            navArgument("id") {
                type = NavType.StringType
            }
        ),
    ) { entry ->
        val id = entry.arguments?.getString("id") ?: ""
        val transactionsViewModel: TransactionsViewModel = viewModel()
        val categoriesViewModel: CategoriesViewModel = viewModel()
        val usersViewModel: UsersViewModel = viewModel()
        transactionsViewModel.updateObjectId(id)
        val transaction = transactionsViewModel.getTransactionById() ?: Transactions()
        transactionsViewModel.updateName(transaction.name)
        transactionsViewModel.updateAmount(transaction.amount)
        transactionsViewModel.updateDescription(transaction.description)
        transactionsViewModel.updateType(transaction.type)
        transactionsViewModel.updateTimestamp(transaction.timestamp.toLong())
        if (runCatching { ObjectId(hexString = transaction.categories_id) }.isSuccess) {
            transactionsViewModel.updateCategories(transaction.categories_id)
        }

        TransactionDetailScreen(
            navController = navController,
            oldTransactions = transaction,
            getType = transactionsViewModel::getType,
            getCategoriesByName = categoriesViewModel::getCategoriesByName,
            categories = transactionsViewModel.categories.value,
            onAmountChange = transactionsViewModel::updateAmount,
            onNameChange = transactionsViewModel::updateName,
            onDescriptionChange = transactionsViewModel::updateDescription,
            onTimestampChange = transactionsViewModel::updateTimestamp,
            onCategoriesChange = transactionsViewModel::updateCategories,
            refundBalanceOfUser = transactionsViewModel::refundBalanceOfUser,
            onBalanceChange = transactionsViewModel::updateBalance,
            updateTransactions = transactionsViewModel::updateData,
            deleteTransactions = transactionsViewModel::deleteData,
            updateBalanceUser = transactionsViewModel::updateBalanceUser,
            updateBalance = usersViewModel::updateBalance,
            updateUsers = usersViewModel::updateData
        )
    }
}

fun NavGraphBuilder.budgetsRoute(navController: NavHostController) {
    composable(route = Screen.Budgets.route) {
        val usersViewModel: UsersViewModel = viewModel()
        val budgetsViewModel: BudgetsViewModel = viewModel()
        val transactionsViewModel: TransactionsViewModel = viewModel()
        val categoriesViewModel: CategoriesViewModel = viewModel()
        Scaffold(
            topBar = { TopBarInformation(usersViewModel.balance.longValue) },
            bottomBar = { SootheBottomNavigation(R.string.bottom_navigation_budgets, navController) },
            floatingActionButton = { AddTransactionOrBudget(navController) }
        ) { padding ->
            BudgetsScreen(
                Modifier
                    .padding(padding)
                    .fillMaxSize(),
                navController = navController,
                budgetData = budgetsViewModel.data.value,
                filterData = budgetsViewModel::filterData,
                getTransactionDataByCategoriesOfBudgetWithTime = categoriesViewModel::getTransactionDataByCategoriesOfBudgetWithTime
            )
        }
    }
    composable(route = Screen.AddBudget.route) {
        val budgetsViewModel: BudgetsViewModel = viewModel()
        val categoriesViewModel: CategoriesViewModel = viewModel()
        AddBudgetScreen(
            navController = navController,
            categoriesData = categoriesViewModel.data.value.filter { it.name in spendCategories },
            categories = budgetsViewModel.categories.value,
            getCategoriesByName = categoriesViewModel::getCategoriesByName,
            onAmountChange = budgetsViewModel::updateAmount,
            onNameChange = budgetsViewModel::updateName,
            onStartDateChange = budgetsViewModel::updateStartDate,
            onEndDateChange = budgetsViewModel::updateEndDate,
            onCategoriesChange = budgetsViewModel::updateCategories,
            insertBudgets = budgetsViewModel::insertData,
        )
    }
    composable(
        route = Screen.BudgetDetail.route + "/{id}",
        arguments = listOf(
            navArgument("id") {
                type = NavType.StringType
            }
        ),
    ) {entry ->
        val id = entry.arguments?.getString("id") ?: ""
        val budgetsViewModel: BudgetsViewModel = viewModel()
        val categoriesViewModel: CategoriesViewModel = viewModel()
        budgetsViewModel.updateObjectId(id)
        val budget = budgetsViewModel.getBudgetById() ?: Budgets()
        budgetsViewModel.updateName(budget.name)
        budgetsViewModel.updateAmount(budget.amount)
        budgetsViewModel.updateStartDate(budget.start_date.toLong())
        budgetsViewModel.updateEndDate(budget.end_date.toLong())
        if (runCatching { ObjectId(hexString = budget.categories_id) }.isSuccess) {
            budgetsViewModel.updateCategories(budget.categories_id)
        }

        BudgetDetailScreen(
            navController = navController,
            oldBudget = budget,
            categoriesData = categoriesViewModel.data.value.filter { it.name in spendCategories },
            categories = budgetsViewModel.categories.value,
            getCategoriesByName = categoriesViewModel::getCategoriesByName,
            onAmountChange = budgetsViewModel::updateAmount,
            onNameChange = budgetsViewModel::updateName,
            onStartDateChange = budgetsViewModel::updateStartDate,
            onEndDateChange = budgetsViewModel::updateEndDate,
            onCategoriesChange = budgetsViewModel::updateCategories,
            updateBudgets = budgetsViewModel::updateData,
            deleteBudgets = budgetsViewModel::deleteData
        )
    }
}

fun NavGraphBuilder.profileRoute(navController: NavHostController) {
    composable(route = Screen.Profile.route) {
        val usersViewModel: UsersViewModel = viewModel()
        Scaffold(
            topBar = { TopBarInformation(usersViewModel.balance.longValue) },
            bottomBar = { SootheBottomNavigation(R.string.bottom_navigation_profile, navController) }
        ) { padding ->
            UserManagementScreen(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                navController = navController,
                balance = usersViewModel.balance.longValue,
                name = usersViewModel.name.value,
                onUsernameChange = usersViewModel::updateName,
                onBalanceChange = usersViewModel::updateBalance,
                updateUsers = usersViewModel::updateData
            )
        }
    }
}


fun NavGraphBuilder.signUpRoute(navController: NavHostController) {
    composable(route = Screen.SignUp.route) {
        SignUpScreen(navController)
    }
}

fun NavGraphBuilder.signInRoute(
    navController: NavHostController,
    navigateToHome: () -> Unit
) {
    composable(route = Screen.SignIn.route) {
        SignInScreen(navController, navigateToHome)
    }
}

fun NavGraphBuilder.forgotPasswordRoute(navController: NavHostController) {
    composable(route = Screen.ForgotPassword.route) {
        ForgotPasswordScreen(navController)
    }
}

fun NavGraphBuilder.settingsRoute(navController: NavHostController) {
    composable(route = Screen.Settings.route) {
        SettingsScreen(navController)
    }
}

fun NavGraphBuilder.confirmEmailSignUpRoute(navController: NavHostController) {
    composable(
        route = Screen.ConfirmEmailForSignUp.route,
        deepLinks = listOf(
            navDeepLink {
                uriPattern = "https://www.niand.id.vn/confirmEmail?token={token}&tokenId={tokenId}"
                action = Intent.ACTION_VIEW
            }
        ),
        arguments = listOf(
            navArgument("token") {
                type = NavType.StringType
            },
            navArgument("tokenId") {
                type = NavType.StringType
            }
        ),
    ) {entry ->
        // Handle the confirmation logic here
        val authenticationViewModel: AuthenticationViewModel = viewModel()
        authenticationViewModel.updateShowNonDismissableDialog(false)
        val token = entry.arguments?.getString("token") ?: ""
        val tokenId = entry.arguments?.getString("tokenId") ?: ""

        // Your confirmation logic goes here
        authenticationViewModel.confirmEmail(
            token = token,
            tokenId = tokenId,
            onSuccess = {
                authenticationViewModel.updateShowNonDismissableDialog(false)
                navController.navigate(Screen.SignIn.route)
            },
            onError = {

            }
        )
    }
}

fun NavGraphBuilder.confirmEmailForChangePasswordRoute(navController: NavHostController) {
    composable(
        route = Screen.ConfirmEmailForChangePassword.route,
        deepLinks = listOf(
            navDeepLink {
                uriPattern = "https://www.niand.id.vn/resetPassword?token={token}&tokenId={tokenId}"
                action = Intent.ACTION_VIEW
            }
        ),
        arguments = listOf(
            navArgument("token") {
                type = NavType.StringType
            },
            navArgument("tokenId") {
                type = NavType.StringType
            }
        ),
    ) {entry ->
        // Handle the confirmation logic here
        val authenticationViewModel: AuthenticationViewModel = viewModel()
        authenticationViewModel.updateShowNonDismissableDialog(false)
        val token = entry.arguments?.getString("token") ?: ""
        val tokenId = entry.arguments?.getString("tokenId") ?: ""

        var isDialogVisible by remember { mutableStateOf(false) }
        var newPassword by remember { mutableStateOf("") }
        NewPasswordDialog(
            onDismiss = {
                // Xử lý khi hộp thoại đóng
                isDialogVisible = false
            },
            onConfirm = {
                // Xử lý khi người dùng xác nhận mật khẩu mới
                // Cập nhật mật khẩu mới hoặc thực hiện các bước xác nhận khác
                newPassword = it
                isDialogVisible = false
            }
        )
        if (newPassword.isNotEmpty()) {
            authenticationViewModel.executeChangePassword(
                token = token,
                tokenId = tokenId,
                newPassword = newPassword,
                onSuccess = {
                    authenticationViewModel.updateShowNonDismissableDialog(false)
                    navController.navigate(Screen.SignIn.route)
                },
                onError = {

                }
            )
        }
    }
}