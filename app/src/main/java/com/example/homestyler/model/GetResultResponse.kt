package com.example.homestyler.model

import com.google.gson.annotations.SerializedName

data class GetResultResponse(
    @SerializedName("id") val id: String,
    @SerializedName("model") val model: String,
    @SerializedName("version") val version: String,
    @SerializedName("input") val input: PredictionInputData,
    @SerializedName("logs") val logs: String,
    @SerializedName("output") val output: List<String>,
    @SerializedName("error") val error: String?,
    @SerializedName("status") val status: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("started_at") val startedAt: String,
    @SerializedName("completed_at") val completedAt: String,
    @SerializedName("urls") val urls: PredictionUrls,
    @SerializedName("metrics") val metrics: Metrics

)
data class Metrics(
    @SerializedName("predict_time") val predictTime: Double
)
