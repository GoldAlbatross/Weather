package com.example.earthsweather.okhttp

import android.annotation.SuppressLint
import android.util.Log
import com.example.earthsweather.TAG
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class ForecaRepository {

    private companion object {
        const val BASE_URL = "https://fnw-us.foreca.com"

        const val USER = "snovaodin"
        const val PASSWORD = "j1e5ORYTr2DT"
    }
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
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()

    private val forecaService = retrofit.create(ForecaApi::class.java)

    fun getLocation(location: String): Single<LocationResponse> {
        return forecaService.authenticate(AuthRequest(USER, PASSWORD))
            .flatMap { tokenResponse ->
                token = tokenResponse.token

                val bearerToken = "Bearer ${tokenResponse.token}"
                forecaService.getLocations(bearerToken, location)
            }
            .retry { count, throwable ->
                count < 3 && throwable is HttpException && throwable.code() == 401
            }
            .doOnSuccess { Log.d("qqq", "Got locations") }
            .doOnError { Log.e("qqq", "Got error with auth or locations") }
    }

    fun showWeather(id: Int): Single<WeatherResponse> {
        return forecaService.authenticate(AuthRequest(USER, PASSWORD))
            .flatMap { response ->
                token = response.token

                val bearerToken = "Bearer ${response.token}"
                forecaService.getWeather(bearerToken, id)
            }
            .retry { count, throwable ->
                Log.d(TAG, "retry: $count")
                count < 3 && throwable is HttpException && throwable.code() == 401
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSuccess { Log.d("qqq", "Current forecast") }
            .doOnError { Log.e("qqq", "Got error with auth or locations, or forecast") }
    }
}