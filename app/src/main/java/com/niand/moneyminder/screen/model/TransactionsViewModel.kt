package com.niand.moneyminder.screen.model

import android.util.Log
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.niand.moneyminder.data.MongoDB
import com.niand.moneyminder.model.Categories
import com.niand.moneyminder.model.Transactions
import com.niand.moneyminder.model.Users
import com.niand.moneyminder.screen.TransactionType
import com.niand.moneyminder.screen.incomeCategories
import com.niand.moneyminder.screen.spendCategories
import com.niand.moneyminder.screen.toRealmInstant
import io.realm.kotlin.types.RealmInstant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.mongodb.kbson.BsonObjectId

class TransactionsViewModel : ViewModel() {
    var objectId = mutableStateOf("")
        private set
    var name = mutableStateOf("")
        private set
    var amount = mutableLongStateOf(0)
        private set
    var type = mutableStateOf(TransactionType.SPEND.value)
        private set
    var description = mutableStateOf("")
        private set
    var timestamp = mutableLongStateOf(0)
        private set
    var categories = mutableStateOf<Categories>(Categories())
        private set
    var currentUser = mutableStateOf<Users?>(Users())
        private set

    // Filter
    var filtered = mutableStateOf(false)
    var data = mutableStateOf(emptyList<Transactions>())
    var balance = mutableLongStateOf(0)

    init {
        viewModelScope.launch {
            try {
                currentUser.value = MongoDB.getUsersData()
            } catch (e: Exception) {
                Log.e("TransactionsViewModel", "Error fetching data from MongoDB", e)
            }
        }
        viewModelScope.launch {
            try {
                data.value = MongoDB.getTransactionData()
            } catch (e: Exception) {
                Log.e("TransactionsViewModel", "Error fetching data from MongoDB", e)
            }
        }
        balance.longValue = currentUser.value?.balance ?: 0L
    }

    fun getType() : String {
        return this.type.value
    }

    fun refundBalanceOfUser(amount: Long, type: String) {
        when (type) {
            TransactionType.SPEND.value -> this.balance.longValue += amount
            TransactionType.INCOME.value -> this.balance.longValue -= amount
        }
    }

    fun updateBalance(amount: Long, type: String) {
        when (type) {
            TransactionType.SPEND.value -> this.balance.longValue -= amount
            TransactionType.INCOME.value -> this.balance.longValue += amount
        }
    }

    fun updateObjectId(id: String) {
        this.objectId.value = id
    }

    fun updateName(name: String) {
        this.name.value = name
    }

    fun updateAmount(amount: Long) {
        this.amount.longValue = amount
    }

    fun updateType(type: String) {
        this.type.value = type
    }

    fun updateDescription(description: String) {
        this.description.value = description
    }

    fun updateTimestamp(timestamp: Long) {
        this.timestamp.longValue = timestamp
    }

    fun updateCategories(id: String) {
        viewModelScope.launch {
            val temp = MongoDB.getCategoriesById(BsonObjectId(hexString = id))
            if (temp != null) {
                this@TransactionsViewModel.categories.value = temp
                if (spendCategories.contains(temp.name)) {
                    this@TransactionsViewModel.type.value = TransactionType.SPEND.value
                }
                if (incomeCategories.contains(temp.name)) {
                    this@TransactionsViewModel.type.value = TransactionType.INCOME.value
                }
            }
        }
    }

    fun getTransactionById() : Transactions? {
        return MongoDB.getTransactionById(BsonObjectId(hexString = objectId.value))
    }

    fun getTransactionByTimestamp(timestamp: RealmInstant): List<Transactions> {
        return data.value.filter { it.timestamp.compareTo(timestamp) == 0 }
    }

    fun getDistinctTransactionTimestampBeforeMonth(firstDayInMonth: RealmInstant): List<RealmInstant> {
        return data.value.filter { it.timestamp.compareTo(firstDayInMonth) < 0 }.map { it.timestamp }.distinct()
    }

    fun getDistinctTransactionTimestampByMonth(firstDayInMonth: RealmInstant, lastDayInMonth: RealmInstant): List<RealmInstant> {
        return data.value.filter { it.timestamp.compareTo(firstDayInMonth) >= 0 && it.timestamp.compareTo(lastDayInMonth) <= 0 }.map { it.timestamp }.distinct()
    }

    fun getDistinctTransactionTimestampAfterMonth(lastDayInMonth: RealmInstant): List<RealmInstant> {
        return data.value.filter { it.timestamp.compareTo(lastDayInMonth) > 0 }.map { it.timestamp }.distinct()
    }

    fun insertData() {
        viewModelScope.launch(Dispatchers.IO) {
            Log.e("TransactionsViewModel", "Insert into Transaction")
            MongoDB.insertData(
                users = null,
                transactions = Transactions().apply {
                    amount = this@TransactionsViewModel.amount.longValue
                    name = this@TransactionsViewModel.name.value
                    type = this@TransactionsViewModel.type.value
                    description = this@TransactionsViewModel.description.value
                    timestamp = this@TransactionsViewModel.timestamp.longValue.toRealmInstant()
                    categories_id = this@TransactionsViewModel.categories.value._id.toHexString()
                },
                budgets = null
            )
        }
    }

    fun updateBalanceUser(updateBalance: (Long) -> Unit, updateUsers: () -> Unit) {
        updateBalance(balance.longValue)
        updateUsers()
    }

    fun updateData() {
        viewModelScope.launch(Dispatchers.IO) {
            MongoDB.updateData(
                users = null,
                transactions = Transactions().apply {
                    _id = BsonObjectId(hexString = this@TransactionsViewModel.objectId.value)
                    amount = this@TransactionsViewModel.amount.longValue
                    name = this@TransactionsViewModel.name.value
                    type = this@TransactionsViewModel.type.value
                    description = this@TransactionsViewModel.description.value
                    timestamp = this@TransactionsViewModel.timestamp.longValue.toRealmInstant()
                    categories_id = this@TransactionsViewModel.categories.value._id.toHexString()
                },
                budgets = null
            )
        }
    }

    fun deleteData() {
        viewModelScope.launch {
            if (objectId.value.isNotEmpty()) {
                MongoDB.deleteTransactionData(id = BsonObjectId(hexString = objectId.value))
            }
        }
    }

    fun filterData() {
        viewModelScope.launch(Dispatchers.IO) {
            if (filtered.value) {
                filtered.value = false
                data.value = MongoDB.getTransactionData()
            } else {
                filtered.value = true
                data.value = MongoDB.filterTransactionsData(name = name.value)
            }
        }
    }
}