package com.example.earthsweather.okhttp

data class Location(
    val id: Int,
    val name: String,
    val country: String
)

class LocationResponse(val locationList: ArrayList<Location>)

