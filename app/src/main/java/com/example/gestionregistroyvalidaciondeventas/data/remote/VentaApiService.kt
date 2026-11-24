package com.example.gestionregistroyvalidaciondeventas.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

data class VentaRemota(
    val id: Long? = null,
    val tipoServicio: String,
    val fechaOperacion: String,
    val cliente: String,
    val monto: Double,
    val codigoAutorizacion: String,
    val estado: String,
    val observaciones: String? = ""
)

interface VentaApiService {

    @GET("api/ventas")
    suspend fun listar(): List<VentaRemota>

    @POST("api/ventas")
    suspend fun crear(@Body venta: VentaRemota): VentaRemota

    @PUT("api/ventas/{id}")
    suspend fun actualizar(@Path("id") id: Long, @Body venta: VentaRemota): VentaRemota

    @DELETE("api/ventas/{id}")
    suspend fun eliminar(@Path("id") id: Long): Response<Unit>
}

data class Post(
    val userId: Int,
    val id: Int,
    val title: String,
    val body: String
)

interface PostApiService {

    @GET("posts")
    suspend fun getPosts(): List<Post>
}
