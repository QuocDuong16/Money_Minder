package com.niand.moneyminder.screen

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.niand.moneyminder.R
import com.niand.moneyminder.screen.dialog.WaitingForConfirmEmailDialog
import com.niand.moneyminder.screen.model.AuthenticationViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo_moneyminderapp),
                            contentDescription = "App logo",
                            modifier = Modifier.size(24.dp, 24.dp)
                        )
                        Spacer(modifier = Modifier.size(4.dp, 0.dp))
                        Text(
                            text = "Money Minder",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.background_moneyminderapp),
                contentDescription = "App background",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = { navController.navigate(Screen.SignUp.route) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp, 1.dp)
            ) {
                Text("SIGN UP FOR FREE")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "OR",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 16.sp,
                modifier = Modifier.padding(8.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            FilledTonalButton(
                onClick = { navController.navigate(Screen.SignIn.route) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp, 1.dp)
            ) {
                Text("SIGN IN")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignInScreen(navController: NavHostController, navigateToHome: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val authenticationViewModel: AuthenticationViewModel = viewModel()
    var state by remember { mutableStateOf(false) }
    var isPasswordVisible by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text( "Đăng nhập") },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Welcome Screen"
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
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email đăng nhập") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(40.dp, 8.dp),
                shape = MaterialTheme.shapes.medium
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Mật khẩu") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(40.dp, 8.dp),
                shape = MaterialTheme.shapes.medium,
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    androidx.compose.material.IconButton(
                        onClick = {
                            isPasswordVisible = !isPasswordVisible
                        }
                    ) {
                        androidx.compose.material.Icon(
                            imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle password visibility"
                        )
                    }
                }
            )
            Button(
                onClick = {
                    authenticationViewModel.signInWithEmailPassword(
                        currentEmail = email,
                        password = password,
                        onSuccess = {
                            state = false
                            navigateToHome()
                        },
                        onError = {
                            state = true
                        }
                    )
                    Log.i("SignIn", "Đăng nhập")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(40.dp, 8.dp, 40.dp, 0.dp)
            ) {
                Text(
                    text = "Đăng nhập",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(40.dp, 0.dp, 40.dp, 2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = { navController.navigate(Screen.SignUp.route) }
                    ) {
                        Text(
                            text = "Đăng ký",
                            fontSize = 12.sp,
                            textDecoration = TextDecoration.Underline,
                        )
                    }
                    TextButton(
                        onClick = { navController.navigate(Screen.ForgotPassword.route) }
                    ) {
                        Text(
                            text = "Quên mật khẩu?",
                            fontSize = 12.sp,
                            textDecoration = TextDecoration.Underline
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider(
                modifier = Modifier.padding(8.dp),
                thickness = 2.dp
            )
            FilledTonalButton(
                onClick = { TODO() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.google_ic),
                        contentDescription = "Google",
                        modifier = Modifier.size(16.dp) // Điều chỉnh kích thước của biểu tượng
                    )
                    Text(
                        text = "Kết nối với Google",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp // Điều chỉnh kích thước văn bản
                    )
                }
            }
            if (state) {
                val coroutineScope = rememberCoroutineScope()
                ElevatedCard(
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 6.dp
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                    ),
                    modifier = Modifier
                        .wrapContentHeight()
                        .fillMaxWidth()
                        .padding(horizontal = 40.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Fail to login",
                        modifier = Modifier
                            .padding(16.dp),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                // Tắt state sau 3 giây
                LaunchedEffect(state) {
                    coroutineScope.launch {
                        delay(3000)
                        state = false
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(navController: NavHostController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val authenticationViewModel: AuthenticationViewModel = viewModel()
    var state by remember { mutableStateOf(false) }
    var isPasswordVisible by remember { mutableStateOf(false) }
    Scaffold (
        topBar = {
            TopAppBar(
                title = { Text(text = "Đăng ký") },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Welcome Screen"
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
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(40.dp, 8.dp),
                shape = MaterialTheme.shapes.medium
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Mật khẩu") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(40.dp, 8.dp),
                shape = MaterialTheme.shapes.medium,
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    androidx.compose.material.IconButton(
                        onClick = {
                            isPasswordVisible = !isPasswordVisible
                        }
                    ) {
                        androidx.compose.material.Icon(
                            imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle password visibility"
                        )
                    }
                }
            )
            Button(
                onClick = {
                    authenticationViewModel.signUpWithEmailPassword(
                        email = email,
                        password = password,
                        onSuccess = {
                            authenticationViewModel.updateShowNonDismissableDialog(true)
                            state = false
                        },
                        onError = {
                            state = true
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(40.dp, 8.dp, 40.dp, 0.dp)
            ) {
                Text(
                    text = "Đăng ký",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(40.dp, 0.dp, 40.dp, 2.dp)
            ) {
                TextButton(
                    onClick = { navController.navigate(Screen.SignIn.route) },
                    modifier = Modifier.padding(0.dp)
                ) {
                    Text(
                        text = "Đăng nhập",
                        fontSize = 12.sp,
                        textDecoration = TextDecoration.Underline
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider(
                modifier = Modifier.padding(8.dp),
                thickness = 2.dp
            )
            FilledTonalButton(
                onClick = { /* TODO() */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.google_ic),
                        contentDescription = "Google",
                        modifier = Modifier.size(16.dp) // Điều chỉnh kích thước của biểu tượng
                    )
                    Text(
                        text = "Kết nối với Google",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp // Điều chỉnh kích thước văn bản
                    )
                }
            }
            if (state) {
                val coroutineScope = rememberCoroutineScope()
                ElevatedCard(
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 6.dp
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                    ),
                    modifier = Modifier
                        .wrapContentHeight()
                        .fillMaxWidth()
                        .padding(horizontal = 40.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Fail to register",
                        modifier = Modifier
                            .padding(16.dp),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                // Tắt state sau 3 giây
                LaunchedEffect(state) {
                    coroutineScope.launch {
                        delay(3000)
                        state = false
                    }
                }
            }
        }
        if (authenticationViewModel.showNonDismissableDialog) {
            WaitingForConfirmEmailDialog {
                authenticationViewModel.updateShowNonDismissableDialog(false)
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(navController: NavHostController) {
    var email by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    val authenticationViewModel: AuthenticationViewModel = viewModel()
    val keyboardController = LocalSoftwareKeyboardController.current

    var isError by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quên mật khẩu") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.popBackStack()
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Nhập email của bạn để đặt lại mật khẩu",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        // Xử lý sự kiện khi nhấn Done trên bàn phím
                        keyboardController?.hide()
                    }
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    authenticationViewModel.changePassword(
                        email = email,
                        onSuccess = {
                            authenticationViewModel.updateShowNonDismissableDialog(true)
                        },
                        onError = {
                            isError = true
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                enabled = !isSubmitting
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Color.White)
                } else {
                    Text("Gửi Email Đặt Lại Mật Khẩu")
                }
            }
            if (isError) {
                val coroutineScope = rememberCoroutineScope()
                ElevatedCard(
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 6.dp
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                    ),
                    modifier = Modifier
                        .wrapContentHeight()
                        .fillMaxWidth()
                        .padding(horizontal = 40.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Error email",
                        modifier = Modifier
                            .padding(16.dp),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                // Tắt state sau 3 giây
                LaunchedEffect(isError) {
                    coroutineScope.launch {
                        delay(3000)
                        isError = false
                    }
                }
            }
            if (authenticationViewModel.showNonDismissableDialog) {
                WaitingForConfirmEmailDialog {
                    authenticationViewModel.updateShowNonDismissableDialog(false)
                }
            }
        }
    }
}