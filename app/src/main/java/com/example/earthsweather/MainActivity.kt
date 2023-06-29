package com.example.earthsweather
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.earthsweather.adapters.CitiesAdapter
import com.example.earthsweather.okhttp.ForecaRepository
import com.example.earthsweather.okhttp.Location
import com.google.android.material.snackbar.Snackbar
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

const val TAG = "qqq"
class MainActivity : AppCompatActivity() {
    private val forecaRepository = ForecaRepository()
    private lateinit var recycler: RecyclerView
    private val citiesAdapter = CitiesAdapter {
        forecaRepository.showWeather(it)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { response ->
                    Log.d(TAG, "success: ${response.weather.symbolPhrase}")
                    Log.d(TAG, "success: ${response.weather.temperature}")
                    Log.d(TAG, "success: ${response.weather.windSpeed}")
                    val weatherText = StringBuilder()
                    weatherText.append(response.weather.symbolPhrase)
                        .append("\nТемпература: ${response.weather.temperature} С` ")
                        .append(" Скорость ветра: ${response.weather.windSpeed} м/с")

                    Snackbar.make(searchButton, weatherText.toString(), Snackbar.LENGTH_LONG).show()
                },
                { showMessage("error", "") }
            ).isDisposed
    }
    private var locations = ArrayList<Location>()
    private lateinit var searchButton: Button
    private lateinit var queryInput: EditText
    private lateinit var placeholderMessage: TextView
    //private lateinit var disposable: Disposable


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

        searchButton.setOnClickListener {
            forecaRepository.getLocation("${queryInput.text}")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { locations ->
                    citiesAdapter.locations = locations.locationList
                    citiesAdapter.notifyDataSetChanged()
                },
                { showMessage("error", "") }
            ).isDisposed
        }
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

}

