package com.example.songstagram_f16.navigation.model

data class AlarmDTO(
    var destinationUid : String? = null,
    var userId : String? = null,
    var uid : String? = null,
    // 0 : like alarm
    // 1 : comment alarm
    // 2 : follow alarm
    var kind : Int? = null, // 어떤 type 인지
    var message : String? = null,
    var timestamp : Long? = null
)