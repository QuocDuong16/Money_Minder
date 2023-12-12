package com.niand.moneyminder.screen

import android.app.Activity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.niand.moneyminder.screen.dialog.CurrencySelectionDialog
import com.niand.moneyminder.screen.model.AppViewModel
import com.niand.moneyminder.screen.model.AuthenticationViewModel
import com.niand.moneyminder.ui.theme.MoneyMinderTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavHostController) {
    var isDarkMode by remember { mutableStateOf(false) }
    var selectedLanguage by remember { mutableStateOf("Vietnamese") }
    var selectedCurrency by remember { mutableStateOf("VND") }
    val authenticationViewModel: AuthenticationViewModel = viewModel()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Cài Đặt")
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        val appViewModel: AppViewModel = viewModel()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp, 0.dp)
        ) {
            // Tính năng chọn giao diện (Sáng/Tối)
            SwitchSetting(
                label = "Chế Độ Tối",
                isChecked = isDarkMode,
                onCheckedChange = {
                    isDarkMode = it
                    appViewModel.isDarkMode.value = isDarkMode
                }
            )

            Divider(modifier = Modifier.padding(16.dp))

            // Tính năng chọn ngôn ngữ
            LanguageSetting(
                selectedLanguage = selectedLanguage,
                onLanguageSelected = { newLanguage ->
                    selectedLanguage = newLanguage
                    // TODO: Xử lý khi chọn ngôn ngữ
                }
            )

            Divider(modifier = Modifier.padding(16.dp))

            // Tính năng chọn đơn vị tiền tệ
            CurrencySetting(
                selectedCurrency = selectedCurrency,
                onCurrencySelected = { newCurrency ->
                    selectedCurrency = newCurrency
                    appViewModel.currency.value = selectedCurrency
                }
            )

            Divider(modifier = Modifier.padding(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Đăng xuất",
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                )
                val activity = (LocalContext.current as? Activity)
                FilledIconButton(onClick = {
                    authenticationViewModel.signOut(
                        onSuccess = {
                            navController.popBackStack(navController.graph.startDestinationId, inclusive = true)
                            activity?.finish()
                            System.exit(0)
                        },
                        onError = {

                        }
                    )
                }) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Đăng xuất"
                    )
                }
            }

            Divider(modifier = Modifier.padding(16.dp))

            // Thông tin ứng dụng
            AppInfoSetting()
        }
    }
}

@Composable
fun SwitchSetting(
    label: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun LanguageSetting(
    selectedLanguage: String,
    onLanguageSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Ngôn Ngữ",
            fontSize = 16.sp,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = selectedLanguage,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable {

            }
        )
    }
}

@Composable
fun CurrencySetting(
    selectedCurrency: String,
    onCurrencySelected: (String) -> Unit
) {
    var isOpenDialog by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Đơn Vị Tiền Tệ",
            fontSize = 16.sp,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = selectedCurrency,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable {
                isOpenDialog = true
            }
        )
        if (isOpenDialog) {
            CurrencySelectionDialog(
                onCurrencySelected = {
                    onCurrencySelected(it)
                    isOpenDialog = false
                },
                onDismiss = {
                    isOpenDialog = false
                }
            )
        }
    }
}

@Composable
fun AppInfoSetting() {
    Text(
        text = "Thông Tin Ứng Dụng",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(16.dp)
    )
    // Hiển thị thông tin về ứng dụng như phiên bản, người phát triển, v.v.
    // Các thông tin này có thể được đọc từ tài liệu hoặc file tài nguyên
}

@Preview
@Composable
fun PreviewSettingsScreen() {
    MoneyMinderTheme {
        SettingsScreen(rememberNavController())
    }
}