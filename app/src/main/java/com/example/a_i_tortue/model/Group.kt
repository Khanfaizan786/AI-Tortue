package com.example.a_i_tortue.model

class Group {
    var image: String? = null
    var name: String? = null
    var timeStamp: String="none"
    var recentMessage:String?=null
    var randomName:String?=null
    var firstName:String?=null

    constructor() {}
    constructor(
        image: String?,
        name: String?,
        timeStamp: String,
        recentMessage: String?,
        randomName:String?,
        firstName:String?
    ) {
        this.image = image
        this.name= name
        this.timeStamp=timeStamp
        this.recentMessage=recentMessage
        this.randomName=randomName
        this.firstName=firstName
    }
}