package com.example.earthsweather
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.earthsweather.adapters.CitiesAdapter
import com.example.earthsweather.okhttp.AuthRequest
import com.example.earthsweather.okhttp.AuthResponse
import com.example.earthsweather.okhttp.ForecaApi
import com.example.earthsweather.okhttp.Location
import com.example.earthsweather.okhttp.LocationResponse
import com.example.earthsweather.okhttp.WeatherResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val BASE_URL = "https://fnw-us.foreca.com"

class MainActivity : AppCompatActivity() {
    private val locations = ArrayList<Location>()
    private val adapter = CitiesAdapter { showWeather(it) }
    private lateinit var searchButton: Button
    private lateinit var queryInput: EditText
    private lateinit var placeholderMessage: TextView
    private lateinit var locationsList: RecyclerView
    private var token: String = ""
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val forecaService = retrofit.create(ForecaApi::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //binds View
        placeholderMessage = findViewById(R.id.placeholderMessage)
        searchButton = findViewById(R.id.searchButton)
        queryInput = findViewById(R.id.queryInput)
        locationsList = findViewById(R.id.locations)

        //fill the recyclerView
        adapter.locations = locations
        locationsList.layoutManager = LinearLayoutManager(this)
        locationsList.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        searchButton.setOnClickListener {
            if (queryInput.text.isNotEmpty()) {
                if (token.isEmpty())  authenticate()
                else  search()
            }
        }
    }

    // authenticate + search------------------------------------------------------------------------
    private fun authenticate() { forecaService
        .authenticate(AuthRequest("goldalbatross", "tOFtdoB8eB3z"))
        .enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                if (response.code() == 200) { token = response.body()?.token.toString(); search() }
                else showMessage(getString(R.string.something_went_wrong), response.code().toString())
            }
            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                showMessage(getString(R.string.something_went_wrong), t.message.toString())
            }

        })
    }

    // method shows the weather by defined location-------------------------------------------------
    private fun showWeather(location: Location) { forecaService
        .getWeather("Bearer $token", location.id)
        .enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                if (response.body()?.weather != null) {
                    val message =
                        "${location.name}\n" +
                        "temperature: ${response.body()?.weather?.temperature}\n" +
                        "feelsLike: ${response.body()?.weather?.feelsLikeTemp}\n"
                    Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
                }
            }
            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
            }
        })
    }

    // no data from the server----------------------------------------------------------------------
    private fun showMessage(text: String, additionalMessage: String) {
        if (text.isNotEmpty()) {
            placeholderMessage.visibility = View.VISIBLE
            locations.clear()
            adapter.notifyDataSetChanged()
            placeholderMessage.text = text
            if (additionalMessage.isNotEmpty()) {
                Toast.makeText(applicationContext, additionalMessage, Toast.LENGTH_LONG)
                    .show()
            }
        } else {
            placeholderMessage.visibility = View.GONE
        }
    }

    //search----------------------------------------------------------------------------------------
    private fun search() { forecaService
        .getLocations("Bearer $token", queryInput.text.toString())
        .enqueue(object : Callback<LocationResponse> {
            override fun onResponse(call: Call<LocationResponse>, response: Response<LocationResponse>) {
                when (response.code()) {
                    200 -> {
                        if (response.body()?.locationList?.isNotEmpty() == true) {
                            locations.clear()
                            locations.addAll(response.body()?.locationList!!)
                            adapter.notifyDataSetChanged()
                            showMessage("", "")
                        } else showMessage(getString(R.string.nothing_found), "") }
                    401 -> authenticate()
                    else -> showMessage(getString(R.string.something_went_wrong), response.code().toString())
                }
            }
            override fun onFailure(call: Call<LocationResponse>, t: Throwable) {
                showMessage(getString(R.string.something_went_wrong), t.message.toString())
            }
        })
    }
}
