package com.example.earthsweather.okhttp

data class Weather(
    val temperature: Float,
    val feelsLikeTemp: Float
)

class WeatherResponse(val weather: Weather)