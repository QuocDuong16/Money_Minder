package com.niand.moneyminder.screen.model

import android.util.Log
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.niand.moneyminder.data.MongoDB
import com.niand.moneyminder.model.Budgets
import com.niand.moneyminder.model.Categories
import com.niand.moneyminder.model.Users
import com.niand.moneyminder.screen.toRealmInstant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.mongodb.kbson.BsonObjectId

class BudgetsViewModel : ViewModel() {
    private var objectId = mutableStateOf("")
    var amount = mutableLongStateOf(0)
        private set
    var name = mutableStateOf("")
        private set
    private var start_date = mutableLongStateOf(0)
    private var end_date = mutableLongStateOf(0)
    var categories = mutableStateOf<Categories>(Categories())
        private set
    private var currentUser = mutableStateOf<Users?>(Users())

    var data = mutableStateOf(emptyList<Budgets>())

    init {
        viewModelScope.launch {
            try {
                currentUser.value = MongoDB.getUsersData()
                Log.e("TransactionsViewModel", "Collected data 2: ${currentUser.value}")
            } catch (e: Exception) {
                Log.e("TransactionsViewModel", "Error fetching data from MongoDB", e)
            }
        }
        viewModelScope.launch {
            try {
                data.value = MongoDB.getBudgetsData()
                Log.e("TransactionsViewModel", "Collected data 2: ${data.value}")
            } catch (e: Exception) {
                Log.e("TransactionsViewModel", "Error fetching data from MongoDB", e)
            }
        }
    }

    fun updateObjectId(id: String) {
        this.objectId.value = id
    }

    fun updateAmount(amount: Long) {
        this.amount.longValue = amount
    }

    fun updateName(name: String) {
        this.name.value = name
    }

    fun updateStartDate(start_date: Long) {
        this.start_date.longValue = start_date
    }

    fun updateEndDate(end_date: Long) {
        this.end_date.longValue = end_date
    }

    fun updateCategories(id: String) {
        val temp = MongoDB.getCategoriesById(BsonObjectId(hexString = id))
        temp?.let {
            this.categories.value = it
        }
    }

    fun getBudgetById() : Budgets? {
        return MongoDB.getBudgetById(BsonObjectId(hexString = objectId.value))
    }

    fun insertData() {
        Log.e("Category", categories.value._id.toString())
        viewModelScope.launch(Dispatchers.IO) {
            Log.e("Budgets", "Insert into Budgets")
            MongoDB.insertData(
                users = currentUser.value!!,
                transactions = null,
                budgets = Budgets().apply {
                    name = this@BudgetsViewModel.name.value
                    amount = this@BudgetsViewModel.amount.longValue
                    start_date = this@BudgetsViewModel.start_date.longValue.toRealmInstant()
                    end_date = this@BudgetsViewModel.end_date.longValue.toRealmInstant()
                    categories_id = this@BudgetsViewModel.categories.value._id.toHexString()
                }
            )
        }
    }

    fun updateData() {
        Log.e("Category", categories.value._id.toString())
        viewModelScope.launch(Dispatchers.IO) {
            MongoDB.updateData(
                users = null,
                transactions = null,
                budgets = Budgets().apply {
                    _id = BsonObjectId(hexString = this@BudgetsViewModel.objectId.value)
                    name = this@BudgetsViewModel.name.value
                    amount = this@BudgetsViewModel.amount.longValue
                    start_date = this@BudgetsViewModel.start_date.longValue.toRealmInstant()
                    end_date = this@BudgetsViewModel.end_date.longValue.toRealmInstant()
                    categories_id = this@BudgetsViewModel.categories.value._id.toHexString()
                }
            )
        }
    }

    fun deleteData() {
        viewModelScope.launch {
            if (objectId.value.isNotEmpty()) {
                MongoDB.deleteBudgetData(id = BsonObjectId(hexString = objectId.value))
            }
        }
    }

    fun filterData(searchString: String) : List<Budgets> {
        return MongoDB.filterBudgetsData(name = searchString)
    }
}