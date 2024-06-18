package com.example.homestyler.model

import com.google.gson.annotations.SerializedName

data class PredictionRequest(

    @SerializedName("version") val version: String = "854e8727697a057c525cdb45ab037f64ecca770a1769cc52287c2e56472a247b",
    @SerializedName("input") val input: PredictionInputData,
)

