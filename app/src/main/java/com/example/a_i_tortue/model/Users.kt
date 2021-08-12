package com.example.a_i_tortue.model

class Users {
    var uid:String?=null
    var name:String?=null
    var profileImage:String?=null

    constructor() {}

    constructor(
        uid:String?,
        profileImage:String?,
        name:String?
    )
    {
        this.profileImage=profileImage
        this.name=name
        this.uid=uid
    }
}