package com.example.homestyler.model

import com.google.gson.annotations.SerializedName

data class PredictionResponse(
    @SerializedName("id") val id: String,
    @SerializedName("model") val model: String,
    @SerializedName("version") val version: String,
    @SerializedName("input") val input: PredictionInputData,
    @SerializedName("logs") val logs: String,
    @SerializedName("error") val error: String?,
    @SerializedName("status") val status: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("urls") val urls: PredictionUrls

)
