package com.niand.moneyminder.screen.model

import android.util.Log
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.niand.moneyminder.data.MongoDB
import com.niand.moneyminder.model.Users
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UsersViewModel : ViewModel() {
    var name = mutableStateOf("")
        private set
    var email = mutableStateOf("")
        private set
    var balance = mutableLongStateOf(0)
        private set
    var currentUser = mutableStateOf<Users?>(Users())
        private set

    init {
        viewModelScope.launch {
            try {
                currentUser.value = MongoDB.getUsersData()
            } catch (e: Exception) {
                Log.e("UsersViewModel", "Error fetching data from MongoDB", e)
            }
        }
        name.value = currentUser.value?.name ?: ""
        balance.longValue = currentUser.value?.balance ?: 0L
        email.value = currentUser.value?.email ?: ""
    }

    fun updateName(name: String) {
        this.name.value = name
    }

    fun updateBalance(balance: Long) {
        this.balance.longValue = balance
    }

    fun updateData() {
        viewModelScope.launch(Dispatchers.IO) {
            MongoDB.updateData(
                users = Users().apply {
                    _id = this@UsersViewModel.currentUser.value!!._id
                    name = this@UsersViewModel.name.value
                    balance = this@UsersViewModel.balance.longValue
                },
                transactions = null,
                budgets = null
            )
        }
    }
}