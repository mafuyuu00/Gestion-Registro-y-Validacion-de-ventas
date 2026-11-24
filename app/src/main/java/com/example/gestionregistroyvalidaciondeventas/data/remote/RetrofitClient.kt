package com.example.gestionregistroyvalidaciondeventas.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL_MICROSERVICIO = "http://localhost:8080/"

    private const val BASE_URL_API_EXTERNA = "https://jsonplaceholder.typicode.com/"

    val apiService: VentaApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_MICROSERVICIO)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(VentaApiService::class.java)
    }

    val postApi: PostApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_API_EXTERNA)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PostApiService::class.java)
    }
}
