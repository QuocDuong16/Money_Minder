package com.niand.moneyminder.model

import com.niand.moneyminder.screen.TransactionType
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

open class Transactions() : RealmObject {
    @PrimaryKey
    var _id: ObjectId = ObjectId.invoke()
    var owner_id: String = ""

    var amount: Long = 0L
    var name: String = ""
    var type: String = TransactionType.SPEND.value
    var description: String = ""
    var timestamp: RealmInstant = RealmInstant.now()

    // Mối quan hệ với Category
    var categories_id: String = ""
}

