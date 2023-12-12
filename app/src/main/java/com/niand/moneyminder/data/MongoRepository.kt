package com.niand.moneyminder.data

import com.niand.moneyminder.model.Budgets
import com.niand.moneyminder.model.Categories
import com.niand.moneyminder.model.Transactions
import com.niand.moneyminder.model.Users
import io.realm.kotlin.query.RealmResults
import io.realm.kotlin.types.RealmInstant
import org.mongodb.kbson.ObjectId

interface MongoRepository {
    fun configureTheRealm()
    fun getUsersData(): Users?

    fun getCategoriesData() : RealmResults<Categories>
    fun getCategoriesByName(name: String) : Categories?
    fun getCategoriesById(id: ObjectId) : Categories?

    fun getTransactionData(): RealmResults<Transactions>
    fun getTransactionById(id: ObjectId): Transactions?
    fun getTransactionByTimestamp(timestamp: RealmInstant): RealmResults<Transactions>
    fun getDistinctTransactionTimestampBeforeMonth(firstDayInMonth: RealmInstant): RealmResults<Transactions>
    fun getDistinctTransactionTimestampByMonth(firstDayInMonth: RealmInstant, lastDayInMonth: RealmInstant): RealmResults<Transactions>
    fun getDistinctTransactionTimestampAfterMonth(lastDayInMonth: RealmInstant): RealmResults<Transactions>

    fun getBudgetsData(): RealmResults<Budgets>
    fun getBudgetById(id: ObjectId): Budgets?

    fun filterTransactionsData(name: String): RealmResults<Transactions>
    fun filterBudgetsData(name: String): RealmResults<Budgets>
    suspend fun insertData(users: Users?, transactions: Transactions?, budgets: Budgets?)
    suspend fun updateData(users: Users?, transactions: Transactions?, budgets: Budgets?)
    suspend fun deleteTransactionData(id: ObjectId)
    suspend fun deleteBudgetData(id: ObjectId)
    fun getTransactionDataByCategories(categories_id: String): RealmResults<Transactions>
}