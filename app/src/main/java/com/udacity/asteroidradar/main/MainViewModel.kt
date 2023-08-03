package com.udacity.asteroidradar.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.PictureOfDay
import com.udacity.asteroidradar.api.NasaApi
import com.udacity.asteroidradar.api.parseAsteroidsJsonResult
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

enum class NasaApiStatus { LOADING, ERROR, DONE }

class MainViewModel : ViewModel() {

    private val API_KEY = ""
    private val _status = MutableLiveData<NasaApiStatus>()

    val status: LiveData<NasaApiStatus>
        get() = _status

    private val _pictureOfDay = MutableLiveData<PictureOfDay>()

    val pictureOfDay : LiveData<PictureOfDay>
        get() = _pictureOfDay

    private val _asteroids = MutableLiveData<List<Asteroid>>()

    val asteroids : LiveData<List<Asteroid>>
        get() = _asteroids

    private val _navigateToSelectedAsteroid = MutableLiveData<Asteroid>()

    val navigateToSelectedAsteroid: LiveData<Asteroid>
        get() = _navigateToSelectedAsteroid

    init {
        getNasaPictureOfTheDay()
        getAsteroidsList()
    }

    private fun getStartDate(): String {
        val dateFormat = SimpleDateFormat(Constants.API_QUERY_DATE_FORMAT, Locale.getDefault())
        return dateFormat.format(Calendar.getInstance().time)
    }

    private fun getAsteroidsList() {
        _status.value = NasaApiStatus.LOADING
        NasaApi.retrofitService.getAsteroids(getStartDate(), API_KEY).enqueue(object :
            Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                val jsonResult = JSONObject(response.body())
                Timber.d("Response JSON \n$jsonResult")
                _status.value = NasaApiStatus.DONE

                jsonResult?.let {
                    _asteroids.value = parseAsteroidsJsonResult(jsonResult)
                    Timber.i(jsonResult.toString())
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Timber.e("Exception::"+ t.stackTraceToString())
                _status.value = NasaApiStatus.ERROR
                _asteroids.value = ArrayList()
            }
        })
    }

    private fun getNasaPictureOfTheDay() {
        viewModelScope.launch {
            _status.value = NasaApiStatus.LOADING
            try {
                val result = NasaApi.retrofitService.getImageOfTheDay(API_KEY) // TODO: find a better way to pass API key
                _status.value = NasaApiStatus.DONE

                if (result.mediaType == "image") { // TODO: remove hardcoded value
                    _pictureOfDay.value = result
                    Timber.i("Picture of the day url::"+result.url)
                } else {
                    Timber.i("Picture of the day is a video")
                }
            } catch (e : java.lang.Exception) {
                _status.value = NasaApiStatus.ERROR
                Timber.e("Error loading image "+e.message)
            }
        }
    }

    // Navigation
    fun displayAsteroidDetails(asteroid: Asteroid) {
        _navigateToSelectedAsteroid.value = asteroid
    }

    fun displayAsteroidDetailsComplete() { _navigateToSelectedAsteroid.value = null }
}