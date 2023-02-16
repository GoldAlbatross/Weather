package com.example.earthsweather.okhttp

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface ForecaApi {

    @POST("/authorize/token?expire_hours=-1")
    fun authenticate(@Body request: AuthRequest): Call<AuthResponse>

    @GET("/api/v1/location/search/{query}")
    fun getLocations(
        @Header("Authorization") token: String,
        @Path("query") textInput: String
    ): Call<LocationResponse>

    @GET("/api/v1/current/{location}")
    fun getWeather(
        @Header("Authorization") token: String,
        @Path("location") id: Int
    ): Call<WeatherResponse>
}