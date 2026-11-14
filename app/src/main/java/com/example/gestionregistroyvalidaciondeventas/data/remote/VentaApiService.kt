package com.example.gestionregistroyvalidaciondeventas.data.remote

import com.example.gestionregistroyvalidaciondeventas.data.local.VentaEntity
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface VentaApiService {
    @GET("todos")
    suspend fun obtenerVentas(): Response<List<Any>>

    @POST("posts")
    suspend fun registrarVenta(@Body venta: VentaEntity): Response<Any>
}
