package com.example.a_i_tortue.model

class GroupChat {
    var image: String? = null
    var name: String? = null
    var from: String? = null
    var type: String? = null
    var message: String? = null
    var date: String? = null
    var time: String? = null
    var timeStamp: String? = null

    constructor() {}
    constructor(
        image: String?,
        name: String?,
        from: String?,
        type: String?,
        message: String?,
        date: String?,
        time: String?,
        timeStamp: String?
    ) {
        this.image = image
        this.name= name
        this.time=time
        this.message=message
        this.date=date
        this.type=type
        this.from=from
        this.timeStamp=timeStamp
    }
}