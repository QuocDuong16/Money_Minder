package com.niand.moneyminder.screen

//noinspection UsingMaterialAndMaterial3Libraries
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.niand.moneyminder.R
import com.niand.moneyminder.data.MongoDB
import com.niand.moneyminder.model.Categories
import com.niand.moneyminder.model.Transactions
import com.niand.moneyminder.screen.dialog.ToastSnackbar
import com.niand.moneyminder.screen.dialog.WebViewDialog
import com.niand.moneyminder.screen.model.AppViewModel
import io.realm.kotlin.types.RealmInstant
import org.mongodb.kbson.ObjectId
import java.net.URL
import java.net.URLDecoder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionScreen(
    modifier: Modifier,
    navController: NavHostController,
    transactionsDataBeforeThisMonth: List<RealmInstant>,
    transactionsDataThisMonth: List<RealmInstant>,
    transactionsDataAfterThisMonth: List<RealmInstant>,
    getTransactionByTimestamp: (timestamp: RealmInstant) -> List<Transactions>
) {
    Surface(modifier = modifier.padding(16.dp)) {
        var selectedTabIndex by remember { mutableIntStateOf(1) }

        val tabTitles = listOf("Quá khứ", "Tháng này", "Tương lai")

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    if (selectedTabIndex > 0) {
                                        selectedTabIndex--
                                    }
                                }
                            ) {
                                Icon(Icons.Default.ArrowBack, contentDescription = null)
                            }
                            Text(
                                text = tabTitles[selectedTabIndex],
                                style = MaterialTheme.typography.titleSmall,
                                fontSize = 18.sp
                            )
                            IconButton(
                                onClick = {
                                    if (selectedTabIndex < tabTitles.size - 1) {
                                        selectedTabIndex++
                                    }
                                }
                            ) {
                                Icon(Icons.Default.ArrowForward, contentDescription = null)
                            }
                        }
                    },
                    modifier = Modifier.background(Color.Transparent)
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(0.dp, 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Hiển thị nội dung của các tab
                when (selectedTabIndex) {
                    0 -> ShowTabContent(transactionsDataBeforeThisMonth, navController, getTransactionByTimestamp)
                    1 -> ShowTabContent(transactionsDataThisMonth, navController, getTransactionByTimestamp)
                    2 -> ShowTabContent(transactionsDataAfterThisMonth, navController, getTransactionByTimestamp)
                }
            }
        }
    }
}

@Composable
fun ShowTabContent(transactionsData: List<RealmInstant>, navController: NavHostController, getTransactionByTimestamp: (RealmInstant) -> List<Transactions>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(transactionsData) {
            IntraDayTransactions(getTransactionByTimestamp, false, navController, it)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun TransactionCard(transaction: Transactions, categories: Categories, navController: NavHostController) {
    val appViewModel: AppViewModel = viewModel()
    val color = when (transaction.type) {
        TransactionType.INCOME.value -> {
            MaterialTheme.colorScheme.tertiary
        }
        TransactionType.SPEND.value -> {
            MaterialTheme.colorScheme.error
        }
        else -> {
            MaterialTheme.colorScheme.error
        }
    }
    Row(
        modifier = Modifier
            .padding(all = 12.dp)
            .fillMaxWidth()
            .clickable {
                navController.navigate(Screen.TransactionDetail.route + "/${transaction._id.toHexString()}")
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.google_ic),
                contentDescription = "Icon danh mục chi tiêu",
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "${categories.name} - ${transaction.name}",
                style = MaterialTheme.typography.titleMedium,
                fontSize = 18.sp
            )
        }
        Text(
            text = "${transaction.amount}${appViewModel.currency.value}",
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 18.sp,
            color = color
        )
    }
}

@Composable
fun IntraDayTransactions(
    getTransactionByTimestamp: (timestamp: RealmInstant) -> List<Transactions>,
    expanded: Boolean,
    navController: NavHostController,
    currentDate: RealmInstant
) {
    val appViewModel: AppViewModel = viewModel()
    var isExpanded by remember { mutableStateOf(expanded) }
    val transactions = getTransactionByTimestamp(currentDate)
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = currentDate.toLong()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
            .border(1.dp, Color.Gray)
            .heightIn(max = 400.dp)
    ) {

        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = calendar.get(Calendar.DAY_OF_MONTH).toString(),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Thứ ${calendar.get(Calendar.DAY_OF_WEEK)}",
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tháng ${calendar.get(Calendar.MONTH) + 1}, ${calendar.get(Calendar.YEAR)}",
                        fontWeight = FontWeight.Light
                    )
                }
            }
            val totalAmount = getTotalAmount(transactions)
            var colorTotalAmout = MaterialTheme.colorScheme.error
            if (totalAmount > 0) {
                colorTotalAmout = MaterialTheme.colorScheme.tertiary
            }

            Text(
                text = "${totalAmount}${appViewModel.currency.value}",
                style = MaterialTheme.typography.titleLarge,
                fontSize = 18.sp,
                color = colorTotalAmout
            )
        }
        HorizontalDivider(thickness = 2.dp)
        if (isExpanded) {
            LazyColumn {
                items(transactions) {transaction ->
                    TransactionCard(transaction, MongoDB.getCategoriesById(ObjectId(hexString = transaction.categories_id))!!, navController)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionInputScreen(
    navController: NavHostController,
    getType: () -> String,
    getCategoriesByName: (String) -> Categories?,
    onAmountChange: (Long) -> Unit,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onTimestampChange: (Long) -> Unit,
    onCategoriesChange: (String) -> Unit,
    onBalanceChange: (Long, String) -> Unit,
    insertTransactions: () -> Unit,
    updateBalanceUser: ((Long) -> Unit, () -> Unit) -> Unit,
    updateBalance: (Long) -> Unit,
    updateUsers: () -> Unit
) {
    val calendar = Calendar.getInstance()
    calendar.time = Date()

    // set the initial date
    var isDialogWebVisible by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = calendar.timeInMillis)
    var nameTemp by remember { mutableStateOf("") }
    var amountTemp by remember { mutableStateOf("") }
    var descriptionTemp by remember { mutableStateOf("") }
    var selectedDate by remember { mutableLongStateOf(calendar.timeInMillis) }

    val showDatePicker = remember { mutableStateOf(false) }

    var isError by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Thêm Giao Dịch")
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

            OutlinedTextField(
                value = nameTemp,
                onValueChange = {
                    nameTemp = it
                    onNameChange(nameTemp)
                },
                label = {
                    Text("Tên giao dịch")
                },
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            OutlinedTextField(
                value = amountTemp,
                onValueChange = {
                    amountTemp = it
                    onAmountChange(runCatching { amountTemp.toLong() }.getOrDefault(0L))
                },
                label = {
                    Text("Số tiền")
                },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            OutlinedTextField(
                value = descriptionTemp,
                onValueChange = {
                    descriptionTemp = it
                    onDescriptionChange(descriptionTemp)
                },
                label = {
                    Text("Mô tả (tùy chọn)")
                },
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val formatter = SimpleDateFormat("dd-MM-yyyy", Locale.ROOT)
                Text(
                    text = "Ngày: ${formatter.format(Date(selectedDate))}",
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )
                Button(
                    onClick = {
                        showDatePicker.value = true
                    }
                ) {
                    Text("Chọn ngày")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    when {
                        nameTemp.isBlank() -> { isError = true }
                        amountTemp.isBlank() -> { isError =true }
                        descriptionTemp.isBlank() -> { isError = true}
                        else -> {
                            isError = false
                            onTimestampChange(selectedDate)
                            isDialogWebVisible = true
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

            if (showDatePicker.value) {
                DatePickerDialog(
                    onDismissRequest = {
                        showDatePicker.value = false
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            showDatePicker.value = false
                            selectedDate = datePickerState.selectedDateMillis!!
                        }) {
                            Text(text = "Confirm")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showDatePicker.value = false
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
            if (isError) {
                ToastSnackbar(message = "Lỗi nhập", onDismiss = { isError = false } )
            }
            if (isDialogWebVisible) {
                Log.e("text", "https://${uri}/api?name=${nameTemp.replace(" ", "+")}&description=${descriptionTemp.replace(" ", "+")}")
                WebViewDialog(
                    url = "https://${uri}/api?name=${nameTemp.replace(" ", "+")}&description=${descriptionTemp.replace(" ", "+")}",
                    onDismiss = {
                        Log.e("Test", "URL: $it")
                        val url = URL(it)
                        val queryParams = url.query.split("&")

                        // Lặp qua các tham số để tìm tham số "category"
                        for (param in queryParams) {
                            val pair = param.split("=")
                            val key = URLDecoder.decode(pair[0], "UTF-8")
                            val value = URLDecoder.decode(pair[1], "UTF-8")

                            if (key == "category") {
                                Log.e("Test", "Value of 'category': $value")
                                val temp = getCategoriesByName(value)
                                onCategoriesChange(temp!!._id.toHexString())
                                break
                            }
                        }
                        insertTransactions()
                        onBalanceChange(runCatching { amountTemp.toLong() }.getOrDefault(0L), getType())
                        updateBalanceUser(updateBalance, updateUsers)
                        isDialogWebVisible = false
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(
    navController: NavHostController,
    oldTransactions: Transactions,
    getType: () -> String,
    getCategoriesByName: (String) -> Categories?,
    categories: Categories,
    onAmountChange: (Long) -> Unit,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onTimestampChange: (Long) -> Unit,
    onCategoriesChange: (String) -> Unit,
    refundBalanceOfUser: (Long, String) -> Unit,
    onBalanceChange: (Long, String) -> Unit,
    updateTransactions: () -> Unit,
    deleteTransactions: () -> Unit,
    updateBalanceUser: ((Long) -> Unit, () -> Unit) -> Unit,
    updateBalance: (Long) -> Unit,
    updateUsers: () -> Unit
) {
    val calendar = Calendar.getInstance() // Đặt ngày ban đầu từ dữ liệu giao dịch
    calendar.time = Date()
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = calendar.timeInMillis)
    val oldAmount = oldTransactions.amount
    // Các trạng thái của các trường dữ liệu
    var isDialogWebVisible by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf(oldTransactions.name) }
    var amount by remember { mutableStateOf(oldTransactions.amount.toString()) }
    Log.e("Amount", "Amount: ${oldTransactions.amount}")
    var description by remember { mutableStateOf(oldTransactions.description) }
    var selectedDate by remember { mutableLongStateOf(oldTransactions.timestamp.toLong()) }

    val showDatePicker = remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Chi Tiết Giao Dịch")
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
                    // Nút chỉnh sửa giao dịch
                    IconButton(
                        onClick = {
                            when {
                                name.isBlank() -> { isError = true }
                                amount.isBlank() -> { isError =true }
                                description.isBlank() -> { isError = true}
                                else -> {
                                    isError = false
                                    onTimestampChange(selectedDate)
                                    refundBalanceOfUser(oldAmount, oldTransactions.type)
                                    isDialogWebVisible = true
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Chỉnh Sửa"
                        )
                    }
                    // Nút xóa giao dịch
                    IconButton(
                        onClick = {
                            refundBalanceOfUser(oldAmount, oldTransactions.type)
                            updateBalanceUser(updateBalance, updateUsers)
                            deleteTransactions()
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

            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    onNameChange(name)
                },
                label = {
                    Text("Tên giao dịch")
                },
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            OutlinedTextField(
                value = amount,
                onValueChange = {
                    amount = it
                    onAmountChange(runCatching { amount.toLong() }.getOrDefault(0L))
                },
                label = {
                    Text("Số tiền")
                },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            OutlinedTextField(
                value = description,
                onValueChange = {
                    description = it
                    onDescriptionChange(description)
                },
                label = {
                    Text("Mô tả (tùy chọn)")
                },
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val formatter = SimpleDateFormat("dd MMMM yyyy", Locale.ROOT)
                Text(
                    text = "Ngày: ${formatter.format(Date(selectedDate))}",
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )
                Button(
                    onClick = {
                        showDatePicker.value = true
                    }
                ) {
                    Text("Chọn ngày")
                }
            }

            if (showDatePicker.value) {
                DatePickerDialog(
                    onDismissRequest = {
                        showDatePicker.value = false
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            showDatePicker.value = false
                            selectedDate = datePickerState.selectedDateMillis!!
                        }) {
                            Text(text = "Confirm")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showDatePicker.value = false
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
            if (isError) {
                ToastSnackbar(message = "Lỗi nhập", onDismiss = { isError = false } )
            }
            if (isDialogWebVisible) {
                Log.e("text", "https://${uri}/api?name=${name.replace(" ", "+")}&description=${description.replace(" ", "+")}")
                WebViewDialog(
                    url = "https://${uri}/api?name=${name.replace(" ", "+")}&description=${description.replace(" ", "+")}",
                    onDismiss = {
                        Log.e("Test", "URL: $it")
                        val url = URL(it)
                        val queryParams = url.query.split("&")

                        // Lặp qua các tham số để tìm tham số "category"
                        for (param in queryParams) {
                            val pair = param.split("=")
                            val key = URLDecoder.decode(pair[0], "UTF-8")
                            val value = URLDecoder.decode(pair[1], "UTF-8")

                            if (key == "category") {
                                Log.e("Test", "Value of 'category': $value")
                                val temp = getCategoriesByName(value)
                                onCategoriesChange(temp!!._id.toHexString())
                                break
                            }
                        }
                        updateTransactions()
                        onBalanceChange(runCatching { amount.toLong() }.getOrDefault(0L), getType())
                        updateBalanceUser(updateBalance, updateUsers)
                        isDialogWebVisible = false
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}