package com.example.earthsweather.okhttp

import com.google.gson.annotations.SerializedName

class AuthRequest(

    val user: String,
    val password: String
)


class AuthResponse(
    @SerializedName("access_token")
    val token: String
)
