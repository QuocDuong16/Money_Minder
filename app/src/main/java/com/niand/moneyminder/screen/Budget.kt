package com.niand.moneyminder.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.niand.moneyminder.model.Budgets
import com.niand.moneyminder.model.Categories
import com.niand.moneyminder.model.Transactions
import com.niand.moneyminder.screen.dialog.ToastSnackbar
import com.niand.moneyminder.screen.model.AppViewModel
import io.realm.kotlin.types.RealmInstant
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetsScreen(
    modifier: Modifier,
    navController: NavHostController,
    budgetData: List<Budgets>,
    filterData: (String) -> List<Budgets>,
    getTransactionDataByCategoriesOfBudgetWithTime: (String, RealmInstant, RealmInstant) -> List<Transactions>
) {
    Surface(modifier = modifier) {
        var searchQuery by remember { mutableStateOf("") }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(text = "Ngân Sách")
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(padding)
            ) {
                Spacer(modifier = Modifier.height(2.dp))

                // Tìm kiếm
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                    },
                    label = {
                        Text("Tìm kiếm ngân sách")
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Danh sách ngân sách
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    when {
                        searchQuery.isBlank() -> {
                            items(budgetData) { budget ->
                                val spentAmount = getTotalAmount(getTransactionDataByCategoriesOfBudgetWithTime(budget.categories_id, budget.start_date, budget.end_date)).toPositiveLong()
                                BudgetItem(budget = budget, spentAmount = spentAmount, navController = navController)
                                Divider(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(Color.Gray)
                                )
                            }
                        }
                        searchQuery.isNotBlank() -> {
                            items(filterData(searchQuery)) { budget ->
                                val spentAmount = getTotalAmount(getTransactionDataByCategoriesOfBudgetWithTime(budget.categories_id, budget.start_date, budget.end_date)).toPositiveLong()
                                BudgetItem(budget = budget, spentAmount = spentAmount, navController = navController)
                                Divider(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(Color.Gray)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BudgetItem(
    budget: Budgets,
    spentAmount: Long,
    navController: NavHostController
) {
    val appViewModel: AppViewModel = viewModel()
    // Kiểm tra cảnh báo tiêu dùng gần vượt quá mức hạn định
    val isNearLimit = spentAmount >= budget.amount * 0.9 && spentAmount <= budget.amount
    val isOverLimit = spentAmount > budget.amount
    val formatter = SimpleDateFormat("dd MMMM yyyy", Locale.ROOT)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable {
                navController.navigate(Screen.BudgetDetail.route + "/${budget._id.toHexString()}")
            },
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = budget.name,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Giới hạn: ${budget.amount}${appViewModel.currency.value}",
            fontSize = 16.sp,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Đã tiêu: ${spentAmount}${appViewModel.currency.value}",
            fontSize = 16.sp,
            color = if (isNearLimit) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary
        )
        Text(
            text = "Khoảng thời gian: ${formatter.format(Date(budget.start_date.toLong()))} " +
                    "- ${formatter.format(Date(budget.end_date.toLong()))}",
            fontSize = 16.sp,
        )
        if (isNearLimit) {
            val randomAdvice = adviceList.random()
            Text(
                text = "Lời khuyên: ${randomAdvice}",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.error
            )
        }
        if (isOverLimit) {
            Text(
                text = "Chi tiêu đã vượt quá ngân sách, cần xem xét lại",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.background(MaterialTheme.colorScheme.errorContainer)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBudgetScreen(
    navController: NavHostController,
    categoriesData: List<Categories>,
    categories: Categories,
    getCategoriesByName: (name: String) -> Categories?,
    onAmountChange: (amount: Long) -> Unit,
    onNameChange: (name: String) -> Unit,
    onStartDateChange: (start_date: Long) -> Unit,
    onEndDateChange: (end_date: Long) -> Unit,
    onCategoriesChange: (id: String) -> Unit,
    insertBudgets: () -> Unit,
) {
    val calendar = Calendar.getInstance()
    calendar.time = Date()
    val formatter = SimpleDateFormat("dd MMMM yyyy", Locale.ROOT)
    var budgetName by remember { mutableStateOf("") }
    var limitAmount by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(false) }
    var tempCategory by remember { mutableStateOf("") }

    val categories = categoriesData.map { it.name }

    val datePickerDialog = remember { mutableStateOf(false) }

    // set the initial date
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = calendar.timeInMillis)

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    var selectedDate by remember { mutableStateOf(calendar.timeInMillis) }
    var startDate by remember { mutableStateOf(calendar.timeInMillis) }
    var endDate by remember { mutableStateOf(calendar.timeInMillis) }

    var isError by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Thêm Ngân Sách Mới")
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.popBackStack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Tên ngân sách
            OutlinedTextField(
                value = budgetName,
                onValueChange = {
                    budgetName = it
                    onNameChange(budgetName)
                },
                label = {
                    Text("Tên Ngân Sách")
                },
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            // Danh mục chi tiêu (ComboBox)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Danh mục chi tiêu",
                    fontSize = 16.sp,
                    modifier = Modifier.padding(16.dp)
                )
                DropdownMenu(
                    expanded = selectedCategory,
                    onDismissRequest = { selectedCategory = false },
                    modifier = Modifier.padding(16.dp)
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            onClick = {
                                tempCategory = category
                            },
                            text = { Text(text = category) }
                        )
                    }
                }
                OutlinedTextField(
                    value = tempCategory,
                    onValueChange = {
                        tempCategory = it
                        selectedCategory = false
                    },
                    readOnly = true,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedCategory = true }
                        .padding(16.dp)
                )
            }

            // Số tiền giới hạn
            OutlinedTextField(
                value = limitAmount,
                onValueChange = {
                    limitAmount = it
                    onAmountChange(runCatching { limitAmount.toLong() }.getOrDefault(0L))
                },
                label = {
                    Text("Số Tiền Giới Hạn")
                },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            // Khoảng thời gian diễn ra ngân sách (DatePicker)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Khoảng thời gian",
                    fontSize = 16.sp,
                    modifier = Modifier.padding(16.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = "Start: ${formatter.format(Date(startDate))}",
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .clickable {
                                datePickerDialog.value = true
                                showStartDatePicker = true
                            }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    OutlinedTextField(
                        value = "End: ${formatter.format(Date(endDate))}",
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth(1f)
                            .clickable {
                                datePickerDialog.value = true
                                showEndDatePicker = true
                            }
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            datePickerDialog.value = true
                            showStartDatePicker = true
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                    ) {
                        Text("Chọn Start")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = {
                            datePickerDialog.value = true
                            showEndDatePicker = true
                        },
                        modifier = Modifier
                            .fillMaxWidth(1f)
                    ) {
                        Text("Chọn End")
                    }
                }
                if (datePickerDialog.value) {
                    DatePickerDialog(
                        onDismissRequest = {
                            datePickerDialog.value = false
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                datePickerDialog.value = false
                                selectedDate = datePickerState.selectedDateMillis!!
                                if (showStartDatePicker) {
                                    startDate = selectedDate
                                    if (startDate > endDate) endDate = startDate
                                    showStartDatePicker = false
                                }
                                if (showEndDatePicker){
                                    endDate = selectedDate
                                    showEndDatePicker = false
                                }
                            }) {
                                Text(text = "Confirm")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                datePickerDialog.value = false
                            }) {
                                Text(text = "Cancel")
                            }
                        }
                    ) {
                        DatePicker(
                            state = datePickerState
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Nút Lưu
            Button(
                onClick = {
                    when {
                        budgetName.isBlank() -> { isError = true }
                        limitAmount.isBlank() -> { isError =true }
                        tempCategory.isBlank() -> { isError = true}
                        else -> {
                            isError = false
                            onStartDateChange(startDate)
                            onEndDateChange(endDate)
                            val temp = getCategoriesByName(tempCategory)
                            onCategoriesChange(temp!!._id.toHexString())
                            insertBudgets()
                            navController.navigate(Screen.Budgets.route)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Lưu",
                    fontSize = 18.sp
                )
            }
            if (isError) {
                ToastSnackbar(message = "Lỗi nhập", onDismiss = { isError = false })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetDetailScreen(
    oldBudget: Budgets,
    navController: NavHostController,
    categoriesData: List<Categories>,
    categories: Categories,
    getCategoriesByName: (name: String) -> Categories?,
    onAmountChange: (amount: Long) -> Unit,
    onNameChange: (name: String) -> Unit,
    onStartDateChange: (start_date: Long) -> Unit,
    onEndDateChange: (end_date: Long) -> Unit,
    onCategoriesChange: (id: String) -> Unit,
    updateBudgets: () -> Unit,
    deleteBudgets: () -> Unit
) {
    val calendar = Calendar.getInstance()
    calendar.time = Date()
    val formatter = SimpleDateFormat("dd MMMM yyyy", Locale.ROOT)
    var selectedCategory by remember { mutableStateOf(false) }
    var tempCategory by remember { mutableStateOf(categories.name) }
    var amount by remember { mutableStateOf(oldBudget.amount.toString()) }
    var name by remember { mutableStateOf(oldBudget.name) }

    val categories = categoriesData.map { it.name }

    val datePickerDialog = remember { mutableStateOf(false) }

    // set the initial date
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = calendar.timeInMillis)

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    var selectedDate by remember { mutableStateOf(calendar.timeInMillis) }
    var startDate by remember { mutableStateOf(oldBudget.start_date.toLong()) }
    var endDate by remember { mutableStateOf(oldBudget.end_date.toLong()) }

    var isError by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Xem Chi Tiết Ngân Sách")
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.popBackStack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                actions = {
                    // Nút chỉnh sửa ngân sách
                    IconButton(
                        onClick = {
                            when {
                                name.isBlank() -> { isError = true }
                                amount.isBlank() -> { isError =true }
                                tempCategory.isBlank() -> { isError = true}
                                else -> {
                                    isError = false
                                    onStartDateChange(startDate)
                                    onEndDateChange(endDate)
                                    val temp = getCategoriesByName(tempCategory)
                                    onCategoriesChange(temp!!._id.toHexString())
                                    updateBudgets()
                                    navController.popBackStack()
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Chỉnh Sửa"
                        )
                    }
                    // Nút xóa ngân sách
                    IconButton(
                        onClick = {
                            deleteBudgets()
                            navController.popBackStack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Xóa"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Tên ngân sách
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    onNameChange(name)
                },
                label = {
                    Text("Tên Ngân Sách")
                },
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            // Danh mục chi tiêu
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Danh mục chi tiêu",
                    fontSize = 16.sp,
                    modifier = Modifier.padding(16.dp)
                )
                DropdownMenu(
                    expanded = selectedCategory,
                    onDismissRequest = { selectedCategory = false },
                    modifier = Modifier.padding(16.dp)
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            onClick = {
                                tempCategory = category
                            },
                            text = { Text(text = category) }
                        )
                    }
                }
                OutlinedTextField(
                    value = tempCategory,
                    onValueChange = {
                        tempCategory = it
                        selectedCategory = false
                    },
                    readOnly = true,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedCategory = true }
                        .padding(16.dp)
                )
            }

            // Số tiền giới hạn
            OutlinedTextField(
                value = amount,
                onValueChange = {
                    amount = it
                    onAmountChange(runCatching { amount.toLong() }.getOrDefault(0L))
                },
                label = {
                    Text("Số Tiền Giới Hạn")
                },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            // Khoảng thời gian diễn ra ngân sách
            // Khoảng thời gian diễn ra ngân sách (DatePicker)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Khoảng thời gian",
                    fontSize = 16.sp,
                    modifier = Modifier.padding(16.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = "Start: ${formatter.format(Date(startDate))}",
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .clickable {
                                datePickerDialog.value = true
                                showStartDatePicker = true
                            }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    OutlinedTextField(
                        value = "End: ${formatter.format(Date(endDate))}",
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth(1f)
                            .clickable {
                                datePickerDialog.value = true
                                showEndDatePicker = true
                            }
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            datePickerDialog.value = true
                            showStartDatePicker = true
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                    ) {
                        Text("Chọn Start")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = {
                            datePickerDialog.value = true
                            showEndDatePicker = true
                        },
                        modifier = Modifier
                            .fillMaxWidth(1f)
                    ) {
                        Text("Chọn End")
                    }
                }
                if (datePickerDialog.value) {
                    DatePickerDialog(
                        onDismissRequest = {
                            datePickerDialog.value = false
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                datePickerDialog.value = false
                                selectedDate = datePickerState.selectedDateMillis!!
                                if (showStartDatePicker) {
                                    startDate = selectedDate
                                    if (startDate > endDate) endDate = startDate
                                    showStartDatePicker = false
                                }
                                if (showEndDatePicker){
                                    endDate = selectedDate
                                    showEndDatePicker = false
                                }
                            }) {
                                Text(text = "Confirm")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                datePickerDialog.value = false
                            }) {
                                Text(text = "Cancel")
                            }
                        }
                    ) {
                        DatePicker(
                            state = datePickerState
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (isError) {
                ToastSnackbar(message = "Lỗi nhập", onDismiss = { isError = false })
            }
        }
    }
}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Preview
//@Composable
//fun PreviewBudgetsScreen() {
//    MoneyMinderTheme {
//        Scaffold(
//            topBar = { TopBarInformation(0) },
//            bottomBar = { SootheBottomNavigation(R.string.bottom_navigation_budgets, rememberNavController()) },
//            floatingActionButton = { AddTransactionOrBudget() }
//        ) { padding ->
//            BudgetsScreen(
//                Modifier
//                    .padding(padding)
//                    .fillMaxSize(),
//                rememberNavController()
//            )
//        }
//    }
//}
//
//@Preview
//@Composable
//fun PreviewAddBudgetScreen() {
//    MoneyMinderTheme {
//        AddBudgetScreen()
//    }
//}
//
//@Preview
//@Composable
//fun PreviewBudgetDetailScreen() {
//    MoneyMinderTheme {
//        BudgetDetailScreen(
//            budget = Budget("Mua sắm", 200000000.0, 30000000.0, "2020"),
//            onEditBudget = { TODO() },
//            onDeleteBudget = { TODO() }
//        )
//    }
//}