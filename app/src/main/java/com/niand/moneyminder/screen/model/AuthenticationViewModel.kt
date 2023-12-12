package com.niand.moneyminder.screen.model

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.niand.moneyminder.data.MongoDB
import com.niand.moneyminder.model.Users
import com.niand.moneyminder.util.Constants.APP_ID
import io.realm.kotlin.mongodb.App
import io.realm.kotlin.mongodb.Credentials
import io.realm.kotlin.mongodb.GoogleAuthType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AuthenticationViewModel : ViewModel() {
    var authenticated = mutableStateOf(false)
        private set

    var showNonDismissableDialog by mutableStateOf(false)
        private set

    fun updateShowNonDismissableDialog(showNonDismissableDialog: Boolean) {
        this.showNonDismissableDialog = showNonDismissableDialog
    }

    fun signUpWithEmailPassword(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val app = App.create(APP_ID)

                withContext(Dispatchers.IO) {
                    app.emailPasswordAuth.registerUser(email, password)
                }
                onSuccess()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    // Hàm này được gọi khi người dùng xác thực email từ deeplink
    fun confirmEmail(token: String, tokenId: String, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        viewModelScope.launch {
            try {
                val app = App.create(APP_ID)

                // Xác thực email
                withContext(Dispatchers.IO) {
                    app.emailPasswordAuth.confirmUser(token, tokenId)
                }
                onSuccess()

            } catch (e: Exception) {
                // Xử lý lỗi khi xác thực email không thành công
                onError(e)
            }
        }
    }

    fun signInWithEmailPassword(
        currentEmail: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    val app = App.create(APP_ID)
                    val user = app.login(Credentials.emailPassword(currentEmail, password))
                    user.loggedIn
                }
                withContext(Dispatchers.Main) {
                    if (result) {
                        onSuccess()
                        authenticated.value = true
                        MongoDB.loginUser()
                        val parts = currentEmail.split("@")
                        val currentUser = MongoDB.getUsersData()
                        if (currentUser == null) {
                            MongoDB.insertData(users = Users().apply {
                                name = parts[0]
                                email = currentEmail
                                balance = 0
                            }, transactions = null, budgets = null)
                        }
                        Log.e("Tag", "Current User: $currentUser")
                    } else {
                        onError(Exception("Đăng nhập không thành công."))
                        Log.e("Error", "$result")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e)
                }
            }
        }
    }

    fun signInWithGoogle(
        tokenId: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    App.create(APP_ID).login(
                        Credentials.google(token = tokenId, type = GoogleAuthType.ID_TOKEN)
                    ).loggedIn
                }
                withContext(Dispatchers.Main) {
                    if (result) {
                        onSuccess()
                        delay(600)
                        authenticated.value = true
                    } else {
                        onError(Exception("Đăng nhập không thành công."))
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e)
                }
            }
        }
    }

    fun signOut(onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        viewModelScope.launch {
            try {
                val app = App.create(APP_ID)

                val user = app.currentUser
                if (user != null) {
                    withContext(Dispatchers.IO) {
                        user.logOut()
                        MongoDB.logoutUser()
                    }
                    onSuccess()
                } else {
                    onError(Exception("Đăng xuất thất bại"))
                }
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    fun changePassword(email: String, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        viewModelScope.launch {
            try {
                val app = App.create(APP_ID)

                withContext(Dispatchers.IO) {
                    app.emailPasswordAuth.sendResetPasswordEmail(email)
                }
                onSuccess()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    fun executeChangePassword(
        token: String,
        tokenId: String,
        newPassword: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val app = App.create(APP_ID)
                withContext(Dispatchers.IO) {
                    app.emailPasswordAuth.resetPassword(token = token, tokenId = tokenId, newPassword = newPassword)
                }

                onSuccess()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }
}