package com.example.homestyler.model

import com.google.gson.annotations.SerializedName

data class PredictionUrls(
    @SerializedName("cancel") val cancel: String,
    @SerializedName("get") val get: String,
)
