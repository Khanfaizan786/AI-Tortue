package com.example.a_i_tortue.model

class Posts {
    var uid:String?=null
    var date:String?=null
    var time:String?=null
    var description:String?=null
    var title:String?=null
    var postImage:String?=null
    var fullName:String?=null
    var profileImage:String?=null
    var randomName:String?=null

    constructor() {}

    constructor(
        uid:String?,
        date:String?,
        time:String?,
        description:String?,
        title:String?,
        postImage:String?,
        fullName:String?,
        profileImage:String?,
        randomName:String?
    )
    {
        this.date=date
        this.description=description
        this.fullName=fullName
        this.postImage=postImage
        this.profileImage=profileImage
        this.randomName=randomName
        this.title=title
        this.uid=uid
        this.time=time
    }
}