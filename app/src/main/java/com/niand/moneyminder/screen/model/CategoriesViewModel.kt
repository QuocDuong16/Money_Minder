package com.niand.moneyminder.screen.model

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.niand.moneyminder.data.MongoDB
import com.niand.moneyminder.model.Categories
import com.niand.moneyminder.model.Transactions
import io.realm.kotlin.types.RealmInstant
import kotlinx.coroutines.launch
import org.mongodb.kbson.ObjectId


class CategoriesViewModel : ViewModel() {
    var data = mutableStateOf(emptyList<Categories>())

    init {
        viewModelScope.launch {
            try {
                data.value = MongoDB.getCategoriesData()
                Log.e("Text", MongoDB.getCategoriesData().size.toString())
                Log.e("CategoriesViewModel", "Collected data 2: ${data.value}")
            } catch (e: Exception) {
                Log.e("CategoriesViewModel", "Error fetching data from MongoDB", e)
            }
        }
    }

    fun getTransactionDataByCategories(categories_id: String): List<Transactions> {
        return MongoDB.getTransactionDataByCategories(categories_id)
    }

    fun getTransactionDataByCategoriesOfBudgetWithTime(categories_id: String, start: RealmInstant, end: RealmInstant): List<Transactions> {
        return MongoDB.getTransactionDataByCategories(categories_id).filter { it.timestamp.compareTo(start) >= 0 && it.timestamp.compareTo(end) <= 0 }
    }

    fun getCategoriesByName(name: String): Categories? {
        return MongoDB.getCategoriesByName(name)
    }

    fun getCategoriesById(id: String): Categories? {
        return MongoDB.getCategoriesById(ObjectId(hexString = id))
    }
}