package com.example.homestyler.model

data class UserActivityModel(
    val uploaderId: String,
    val beforeImageUrl:String,
    val afterImageUrl: String,
    val timestamp: String,
    val userActivityId:String,
    val deleted: Boolean = false,
)
