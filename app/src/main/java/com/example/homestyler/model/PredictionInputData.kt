package com.example.homestyler.model

import com.google.gson.annotations.SerializedName

data class PredictionInputData(
    @SerializedName("image") val image: String = "",
    @SerializedName("prompt") val prompt: String = "",
    @SerializedName("num_samples") val numSamples: String = "1",
)
