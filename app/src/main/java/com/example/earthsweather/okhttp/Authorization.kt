package com.example.earthsweather.okhttp

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

class AuthRequest(
    val user: String,
    val password: String
)


class AuthResponse(
    @SerializedName("access_token")
    val token: String
)
