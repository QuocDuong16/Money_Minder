package com.niand.moneyminder.model

import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

open class Budgets() : RealmObject {
    @PrimaryKey
    var _id: ObjectId = ObjectId.invoke()
    var owner_id: String = ""

    var amount: Long = 0L
    var name: String = ""
    var start_date: RealmInstant = RealmInstant.now()
    var end_date: RealmInstant = RealmInstant.now()
    // Các trường dữ liệu khác của ngân sách

    // Mối quan hệ với Category
    var categories_id: String = ""
}

