package com.example.homestyler


import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance{

    private const val BASE_URL = "https://api.replicate.com/"

    private fun getInstance(): Retrofit{
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val replicateApi:ReplicateApi = getInstance().create(ReplicateApi::class.java)
}