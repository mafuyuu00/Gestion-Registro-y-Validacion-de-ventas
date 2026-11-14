package com.example.gestionregistroyvalidaciondeventas.data.remote

import com.example.gestionregistroyvalidaciondeventas.data.local.VentaEntity
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface VentaApiService {
    @GET("ventas")
    suspend fun obtenerVentas(): Response<List<VentaEntity>>

    @POST("ventas")
    suspend fun registrarVenta(@Body venta: VentaEntity): Response<VentaEntity>
}
