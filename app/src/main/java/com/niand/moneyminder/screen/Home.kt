package com.niand.moneyminder.screen

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.moneyminder.chart.PieChart
import com.niand.moneyminder.R
import com.niand.moneyminder.model.Categories
import com.niand.moneyminder.model.Transactions
import com.niand.moneyminder.screen.dialog.AddSelectionDialog
import com.niand.moneyminder.screen.model.AppViewModel

@Composable
fun HomeScreen(
    modifier: Modifier,
    categoriesData: List<Categories>,
    getCategoriesById: (String) -> Categories?,
    getTransactionDataByCategories: (String) -> List<Transactions>,
    totalSpending: Long
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val appViewModel: AppViewModel = viewModel()
        ElevatedCard(
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            ),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant,)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Totals",
                    fontWeight = FontWeight.Bold,
                    style = TextStyle(fontSize = 20.sp)
                )
                Text(
                    text = "${totalSpending}${appViewModel.currency.value}",
                    color = MaterialTheme.colorScheme.surfaceTint,
                    textAlign = TextAlign.Center,
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        ElevatedCard(
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            ),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Report - Spending",
                    fontWeight = FontWeight.Bold,
                    style = TextStyle(fontSize = 20.sp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                val amountDataSpending = spendCategories.map { cate ->
                    categoriesData.find { it.name == cate }?.let { category ->
                        getTotalAmount(getTransactionDataByCategories(category._id.toHexString()))?.toPositiveFloat()
                    } ?: 0f
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PieChart(
                        values = amountDataSpending,
                        colors = spendColorList,
                        legend = spendCategories
                    )
                }
                HorizontalDivider(thickness = 2.dp, modifier = Modifier.padding(0.dp, 5.dp))
                Text(
                    text = "Report - Incoming",
                    fontWeight = FontWeight.Bold,
                    style = TextStyle(fontSize = 20.sp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                val amountDataIncoming = incomeCategories.map { cate ->
                    categoriesData.find { it.name == cate }?.let { category ->
                        getTotalAmount(getTransactionDataByCategories(category._id.toHexString())).toPositiveFloat()
                    } ?: 0f
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PieChart(
                        values = amountDataIncoming,
                        colors = incomeColorList,
                        legend = incomeCategories
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarInformation(balance: Long) {
    val appViewModel: AppViewModel = viewModel()
    TopAppBar(
        title = {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Wallet,
                    contentDescription = "wallet"
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    "${balance}${appViewModel.currency.value}",
                    maxLines = 1
                )
            }
        },
        actions = {
            IconButton(onClick = {  }) {
                Icon(
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = "Localized description"
                )
            }
        },
    )
}

@Composable
fun SootheBottomNavigation(selectedScreen: Int, navController: NavHostController) {
    BottomAppBar {
        NavigationBar {
            val items = listOf(
                Pair(R.string.bottom_navigation_home, Icons.Default.Home),
                Pair(R.string.bottom_navigation_transactions, Icons.Default.AccountBalanceWallet),
                Pair(R.string.bottom_navigation_budgets, Icons.Default.AccountBalance),
                Pair(R.string.bottom_navigation_profile, Icons.Default.Person)
            )
            items.forEach { (labelResId, icon) ->
                NavigationBarItem(
                    label = {
                        Text(text = stringResource(id = labelResId))
                    },
                    selected = labelResId == selectedScreen, // Thay đổi giá trị "selected" dựa trên màn hình hiện tại
                    onClick = {
                        when (labelResId) {
                            R.string.bottom_navigation_home -> {
                                navController.navigate(Screen.Home.route)
                            }
                            R.string.bottom_navigation_transactions -> {
                                navController.navigate(Screen.Transactions.route)
                            }
                            R.string.bottom_navigation_budgets -> {
                                navController.navigate(Screen.Budgets.route)
                            }
                            R.string.bottom_navigation_profile -> {
                                navController.navigate(Screen.Profile.route)
                            }
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = icon,
                            contentDescription = stringResource(id = labelResId)
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun AddTransactionOrBudget(navController: NavHostController) {
    var showModalBottomSheet by remember { mutableStateOf(false) }
    FloatingActionButton(
        onClick = {
            showModalBottomSheet = true
            Log.e("Tag", "Click!")
        },
    ) {
        Icon(Icons.Filled.Add, "Floating action button.")
    }
    if (showModalBottomSheet) {
        AddSelectionDialog(
            onDismissRequest = {
                showModalBottomSheet = false
            },
            navController = navController
        )
    }
}