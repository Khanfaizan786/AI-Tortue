package com.example.a_i_tortue.model

class Dataset {
    var file: String? = null
    var image: String? = null
    var name: String? = null

    constructor() {}
    constructor(file: String?, image: String?, name: String?) {
        this.file = file
        this.image = image
        this.name= name
    }

}