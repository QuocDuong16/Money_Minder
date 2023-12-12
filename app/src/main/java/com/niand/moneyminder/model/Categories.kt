package com.niand.moneyminder.model

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

open class Categories() : RealmObject {
    @PrimaryKey
    var _id: ObjectId = ObjectId.invoke()

    var name:String = ""
    var description: String = ""
}

