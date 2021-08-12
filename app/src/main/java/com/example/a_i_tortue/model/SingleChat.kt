package com.example.a_i_tortue.model

class SingleChat {
    var uid: String? = null
    var seen: String? = null
    var timeStamp: String="none"
    var recentMessage:String?=null
    var image:String?=null
    var name:String?=null

    constructor() {}
    constructor(
        uid: String?,
        seen: String?,
        timeStamp: String,
        recentMessage: String?,
        image:String?,
        name:String?
    ) {
        this.uid= uid
        this.seen= seen
        this.timeStamp=timeStamp
        this.recentMessage=recentMessage
        this.image=image
        this.name=name
    }
}