package com.example.homestyler.model

import com.google.firebase.Timestamp

data class ImageModel(
    var url:String="",
    var imageId:String="",
    var uploaderId:String="",
    var createdTime:Timestamp= Timestamp.now()
)
