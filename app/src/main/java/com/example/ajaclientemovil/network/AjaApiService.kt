package com.example.ajaclientemovil.data.network

import com.example.ajaclientemovil.data.LoginDTO
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

/**
 * Interfaz que define los Endpoints del servidor de Alex.
 * Retrofit usará esta interfaz para generar las llamadas a red.
 */
interface AjaApiService {

    @FormUrlEncoded
    @POST("/api/auth/login")
    suspend fun login(
        @Field("username") user: String,
        @Field("password") pass: String
    ): Response<LoginDTO> // Usamos Response para poder leer los Headers (la Cookie)
}