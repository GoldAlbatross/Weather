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
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val BASE_URL = "https://fnw-us.foreca.com"

class MainActivity : AppCompatActivity() {
    private lateinit var recycler: RecyclerView
    private val citiesAdapter = CitiesAdapter { showWeather(it) }
    private val locations = ArrayList<Location>()
    private lateinit var searchButton: Button
    private lateinit var queryInput: EditText
    private lateinit var placeholderMessage: TextView
    private var token: String = ""
    private val logging = HttpLoggingInterceptor().apply {
        setLevel(HttpLoggingInterceptor.Level.BODY)
    }
    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()
    private val forecaService = retrofit.create(ForecaApi::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //binds View
        placeholderMessage = findViewById(R.id.placeholderMessage)
        searchButton = findViewById(R.id.searchButton)
        queryInput = findViewById(R.id.queryInput)
        recycler = findViewById(R.id.locations)

        //fill the recyclerView
        citiesAdapter.locations = locations
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = citiesAdapter
    }

    override fun onResume() {
        super.onResume()
        authenticate()
        searchButton.setOnClickListener { search() }
    }

    // authenticate---------------------------------------------------------------------------------
    private fun authenticate() { forecaService
        .authenticate(AuthRequest("snovaodin", "up716gNyY2Or"))
        .enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                if (response.code() == 200) { token = response.body()?.token.toString()}
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
                        "температура: ${response.body()?.weather?.temperature}\n" +
                        "${response.body()?.weather?.symbolPhrase }\n" +
                        "скорость ветра: ${response.body()?.weather?.windSpeed}"
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
            citiesAdapter.notifyDataSetChanged()
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
                            citiesAdapter.notifyDataSetChanged()
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

