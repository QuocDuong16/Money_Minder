package com.niand.moneyminder.data

import android.util.Log
import com.niand.moneyminder.model.Budgets
import com.niand.moneyminder.model.Categories
import com.niand.moneyminder.model.Transactions
import com.niand.moneyminder.model.Users
import com.niand.moneyminder.util.Constants.APP_ID
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.log.LogLevel
import io.realm.kotlin.mongodb.App
import io.realm.kotlin.mongodb.sync.SyncConfiguration
import io.realm.kotlin.query.RealmResults
import io.realm.kotlin.types.RealmInstant
import org.mongodb.kbson.ObjectId

object MongoDB : MongoRepository {
    private val app = App.create(APP_ID)
    private var user = app.currentUser
    private lateinit var realm: Realm

    init {
        configureTheRealm()
    }

    override fun configureTheRealm() {
        if (user != null) {
            val config = SyncConfiguration.Builder(
                user!!,
                setOf(Users::class, Budgets::class, Transactions::class, Categories::class)
            )
                .initialSubscriptions { sub ->
                    add(query = sub.query<Users>(query = "owner_id == $0", user!!.id))
                    add(query = sub.query<Budgets>(query = "owner_id == $0", user!!.id))
                    add(query = sub.query<Transactions>(query = "owner_id == $0", user!!.id))
                    add(query = sub.query<Categories>())
                }
                .log(LogLevel.ALL)
                .build()
            realm = Realm.open(config)
        }
    }

    fun loginUser() {
        configureTheRealm()
    }

    fun logoutUser() {
        realm.close()
    }

    override fun getUsersData(): Users? {
        return realm.query<Users>(query = "owner_id == $0", user!!.id).first().find()
    }

    override fun getCategoriesData() : RealmResults<Categories> {
        return realm.query<Categories>().find()
    }

    override fun getCategoriesByName(name: String) : Categories? {
        return realm.query<Categories>(query = "name == $0", name).first().find()
    }

    override fun getCategoriesById(id: ObjectId) : Categories? {
        return realm.query<Categories>(query = "_id == $0", id).first().find()
    }

    override fun getTransactionData(): RealmResults<Transactions> {
        return realm.query<Transactions>().find()
    }

    override fun getTransactionDataByCategories(categories_id: String): RealmResults<Transactions> {
        return realm.query<Transactions>(query = "categories_id == $0", categories_id).find()
    }

    override fun getTransactionById(id: ObjectId): Transactions? {
        return realm.query<Transactions>(query = "_id == $0", id).first().find()
    }

    override fun getTransactionByTimestamp(timestamp: RealmInstant): RealmResults<Transactions> {
        return realm.query<Transactions>(query = "timestamp >= $0", timestamp).find()
    }

    override fun getDistinctTransactionTimestampBeforeMonth(firstDayInMonth: RealmInstant): RealmResults<Transactions> {
        return realm.query<Transactions>("timestamp < $0", firstDayInMonth).distinct("timestamp").find()
    }

    override fun getDistinctTransactionTimestampByMonth(firstDayInMonth: RealmInstant, lastDayInMonth: RealmInstant): RealmResults<Transactions> {
        return realm.query<Transactions>("timestamp >= $0 AND timestamp <= $1", firstDayInMonth, lastDayInMonth).distinct("timestamp").find()
    }

    override fun getDistinctTransactionTimestampAfterMonth(lastDayInMonth: RealmInstant): RealmResults<Transactions> {
        return realm.query<Transactions>("timestamp > $0", lastDayInMonth).distinct("timestamp").find()
    }

    override fun getBudgetsData(): RealmResults<Budgets> {
        return realm.query<Budgets>().find()
    }

    override fun getBudgetById(id: ObjectId): Budgets? {
        return realm.query<Budgets>(query = "_id == $0", id).first().find()
    }

    override fun filterTransactionsData(name: String): RealmResults<Transactions> {
        return realm.query<Transactions>(query = "name CONTAINS[c] $0", name).find()
    }

    override fun filterBudgetsData(name: String): RealmResults<Budgets> {
        return realm.query<Budgets>(query = "name CONTAINS[c] $0", name).find()
    }

    override suspend fun insertData(users: Users?, transactions: Transactions?, budgets: Budgets?) {
        if (user != null) {
            users?.let {
                realm.write {
                    try {
                        copyToRealm(it.apply { owner_id = user!!.id })
                    } catch (e: Exception) {
                        Log.d("MongoDB", e.message.toString())
                    }
                }
            }
            transactions?.let {
                realm.write {
                    try {
                        copyToRealm(it.apply { owner_id = user!!.id })
                    } catch (e: Exception) {
                        Log.d("MongoDB", e.message.toString())
                    }
                }
            }
            budgets?.let {
                realm.write {
                    try {
                        copyToRealm(it.apply { owner_id = user!!.id })
                    } catch (e: Exception) {
                        Log.d("MongoDB", e.message.toString())
                    }
                }
            }
        }
    }

    override suspend fun updateData(users: Users?, transactions: Transactions?, budgets: Budgets?) {
        users?.let {
            realm.write {
                val queriedUsers =
                    query<Users>(query = "_id == $0", it._id).first().find()
                if (queriedUsers != null) {
                    queriedUsers.name = it.name
                    queriedUsers.balance = it.balance
                } else {
                    Log.d("MongoDB", "Queried Users does not exist.")
                }
            }
        }
        transactions?.let {
            realm.write {
                val queriedTransactions =
                    query<Transactions>(query = "_id == $0", it._id).first().find()
                if (queriedTransactions != null) {
                    queriedTransactions.amount = it.amount
                    queriedTransactions.name = it.name
                    queriedTransactions.type = it.type
                    queriedTransactions.description = it.description
                    queriedTransactions.timestamp = it.timestamp
                    queriedTransactions.categories_id = it.categories_id
                } else {
                    Log.d("MongoDB", "Queried Transactions does not exist.")
                }
            }
        }
        budgets?.let {
            realm.write {
                val queriedBudgets =
                    query<Budgets>(query = "_id == $0", it._id).first().find()
                if (queriedBudgets != null) {
                    queriedBudgets.name = it.name
                    queriedBudgets.amount = it.amount
                    queriedBudgets.start_date = it.start_date
                    queriedBudgets.end_date = it.end_date
                    queriedBudgets.categories_id = it.categories_id
                } else {
                    Log.d("MongoDB", "Queried Budgets does not exist.")
                }
            }
        }
    }

    override suspend fun deleteTransactionData(id: ObjectId) {
        realm.write {
            try {
                val transactions = query<Transactions>(query = "_id == $0", id).first().find()
                transactions?.let { delete(it) }
            } catch (e: Exception) {
                Log.d("MongoRepository", "${e.message}")
            }
        }
    }

    override suspend fun deleteBudgetData(id: ObjectId) {
        realm.write {
            try {
                val budgets = query<Budgets>(query = "_id == $0", id).first().find()
                budgets?.let { delete(it) }
            } catch (e: Exception) {
                Log.d("MongoRepository", "${e.message}")
            }
        }
    }
}