package com.ralhub.model

import java.sql.Timestamp

data class ContentDTO(var explain : String? = null,
var imageUrl : String? = null,
var uid : String? = null,
var userId :String? = null,
var timestamp: Timestamp? =null,
var faviriteCount: Int = 0,
var favorites : Map<String,Boolean> = HashMap()){
    data class Commnet(var uid : String? = null,
                       var userId :String? = null,
                       var commnet : String? =null,
                       var timestamp: Long? = 0)
}