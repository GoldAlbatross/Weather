package com.example.earthsweather.okhttp

import com.google.gson.annotations.SerializedName

data class Weather(
    val temperature: String,
    val windSpeed: String,
    val symbolPhrase : String,
)

class WeatherResponse(
    @SerializedName("current")
    val weather: Weather
)