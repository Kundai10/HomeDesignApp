package com.example.homestyler

import com.example.homestyler.model.GetResultResponse
import com.example.homestyler.model.PredictionRequest
import com.example.homestyler.model.PredictionResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Url

interface ReplicateApi {

    @Headers(
        "Authorization:Token r8_R0lX482I8zHhRYzlLWyIg0rKIVf6YoI0JREHo",
        "Content-Type: application/json"
    )
    @POST("v1/predictions")
    suspend fun makePrediction(@Body requestBody: PredictionRequest): Response<PredictionResponse>


    @Headers(
        "Authorization:Token r8_R0lX482I8zHhRYzlLWyIg0rKIVf6YoI0JREHo",
        "Content-Type: application/json"
    )
    @GET
    suspend fun getResult(@Url getUrl: String): Response<GetResultResponse>

    // Might not need to call the cancel request ever
    @Headers(
        "Authorization:Token r8_R0lX482I8zHhRYzlLWyIg0rKIVf6YoI0JREHo",
        "Content-Type: application/json"
    )
    @GET
    suspend fun cancelRequest(@Url cancelUrl: String):Response<GetResultResponse>

}