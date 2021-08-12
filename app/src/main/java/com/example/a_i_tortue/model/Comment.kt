package com.example.a_i_tortue.model

class Comment {
    var image: String? = null
    var name: String? = null
    var comment: String? = null
    var date: String? = null
    var time: String? = null

    constructor() {}

    constructor(image: String?, name: String?, comment: String?, date: String?, time: String?) {
        this.image = image
        this.name = name
        this.comment = comment
        this.date = date
        this.time = time
    }
}