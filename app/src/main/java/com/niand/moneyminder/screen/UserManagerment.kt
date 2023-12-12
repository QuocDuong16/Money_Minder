package com.niand.moneyminder.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
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
import com.niand.moneyminder.screen.model.AppViewModel
import java.text.SimpleDateFormat
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(
    modifier: Modifier,
    navController: NavHostController,
    balance: Long,
    name: String,
    onBalanceChange: (balance: Long) -> Unit,
    onUsernameChange: (name: String) -> Unit,
    updateUsers: () -> Unit
) {
    Surface(
        modifier = modifier
    ) {
        var balance by remember { mutableStateOf(balance) }
        var username by remember { mutableStateOf(name) }
        // Số dư ví và thông tin cá nhân
        val notificationTime = remember { mutableStateOf(Calendar.getInstance()) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(text = "Quản Lý Người Dùng")
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                navController.navigate(Screen.Settings.route)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Cài đặt"
                            )
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Mục Số dư ví
                UserBalanceSection(
                    balance = balance,
                    onBalanceChange = { newBalance ->
                        balance = newBalance
                        onBalanceChange(balance)
                        updateUsers()
                    }
                )

                Divider(modifier = Modifier.padding(16.dp))

                // Mục Thông tin cá nhân
                UserProfileSection(
                    username = username,
                    onUsernameChange = { newUsername ->
                        username = newUsername
                        onUsernameChange(username)
                        updateUsers()
                    }
                )

                Divider(modifier = Modifier.padding(16.dp))

                // Mục Nhắc lịch quản lý chi tiêu hàng ngày
                UserDailyReminderSection(
                    notificationTime = notificationTime,
                    onNotificationTimeChange = { newTime ->
                        // TODO: Xử lý khi thời gian nhắc lịch được thay đổi
                        notificationTime.value = newTime
                    }
                )
            }
        }
    }
}

@Composable
fun UserBalanceSection(
    balance: Long,
    onBalanceChange: (Long) -> Unit
) {
    val appViewModel: AppViewModel = viewModel()
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = "Số Dư Ví",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Số Dư Hiện Tại: $balance${appViewModel.currency.value}",
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = balance.toString(),
            onValueChange = { newBalance ->
                val parsedBalance = newBalance.toLongOrNull() ?: balance
                onBalanceChange(parsedBalance)
            },
            label = {
                Text("Sửa Đổi Số Dư")
            },
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun UserProfileSection(
    username: String,
    onUsernameChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = "Thông Tin Cá Nhân",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tên Người Dùng: $username",
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = username,
            onValueChange = { newUsername ->
                onUsernameChange(newUsername)
            },
            label = {
                Text("Sửa Đổi Tên Người Dùng")
            },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = { TODO() }
        ) {
            Text(text = "Đổi mật khẩu")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDailyReminderSection(
    notificationTime: MutableState<Calendar>,
    onNotificationTimeChange: (Calendar) -> Unit
) {
    var selectedHour by remember {
        mutableStateOf(0) // or use  mutableStateOf(0)
    }

    var selectedMinute by remember {
        mutableStateOf(0) // or use  mutableStateOf(0)
    }

    var showDialog by remember {
        mutableStateOf(false)
    }

    val timePickerState = rememberTimePickerState(
        initialHour = selectedHour,
        initialMinute = selectedMinute
    )
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = "Nhắc Lịch Quản Lý Chi Tiêu Hàng Ngày",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Thời Gian Nhắc: ${SimpleDateFormat("HH:mm").format(notificationTime.value.time)}",
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { showDialog = true }) {
            Text(text = "Đổi thời gian")
        }
        if (showDialog) {
            AlertDialog(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(size = 12.dp)
                    ),
                onDismissRequest = { showDialog = false }
            ) {
                Column(
                    modifier = Modifier
                        .background(
                            color = Color.LightGray.copy(alpha = 0.3f)
                        )
                        .padding(top = 28.dp, start = 20.dp, end = 20.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // time picker
                    TimePicker(state = timePickerState)

                    // buttons
                    Row(
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        // dismiss button
                        TextButton(onClick = { showDialog = false }) {
                            Text(text = "Dismiss")
                        }

                        // confirm button
                        TextButton(
                            onClick = {
                                showDialog = false
                                selectedHour = timePickerState.hour
                                selectedMinute = timePickerState.minute
                            }
                        ) {
                            Text(text = "Confirm")
                        }
                    }
                }
            }
        }
    }
}

//@Preview
//@Composable
//fun PreviewUserManagementScreen() {
//    MoneyMinderTheme {
//        Scaffold(
//            topBar = { TopBarInformation(0) },
//            bottomBar = { SootheBottomNavigation(R.string.bottom_navigation_transactions, rememberNavController()) },
//            floatingActionButton = { AddTransaction() }
//        ) { padding ->
//            UserManagementScreen(
//                Modifier
//                    .padding(padding)
//                    .fillMaxSize(),
//                rememberNavController(),
//                onBalanceChange = {},
//                onUsernameChange = {},
//                updateUsers = {}
//            )
//        }
//    }
//}