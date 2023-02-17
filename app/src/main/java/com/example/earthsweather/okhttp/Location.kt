package com.example.earthsweather.okhttp

import com.google.gson.annotations.SerializedName

data class Location(
    val id: Int,
    val name: String,
    val country: String
)

class LocationResponse(
    @SerializedName("locations")
    val locationList: ArrayList<Location>)

