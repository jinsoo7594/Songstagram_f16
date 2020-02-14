package com.example.songstagram_f16.navigation.model

data class ContentDTO(var explain : String? = null,  // contents의 설명 관리
                      var imageUrl : String? = null, // 이미지 주소 관리
                      var uid : String? = null, // 어느 유저와 어울렸는지 id 관리
                      var userId: String? = null, // 올린 유저의 id 관리
                      var timestamp : Long? = null, // 몇시 몇분에 컨텐츠를 올렸는지 시간 관리
                      var favoriteCount : Int = 0, // 좋아요를 몇개 눌렀는지 관리
                      var favorites : MutableMap<String, Boolean> = HashMap()){ // 중복 좋아요 방지위해 좋아요 누른 유저 관리
    // 덧글관리
    data class Comment(var uid: String? = null,
                       var userId : String? = null,
                       var comment : String? = null,
                       var timestamp : Long? = null)
}