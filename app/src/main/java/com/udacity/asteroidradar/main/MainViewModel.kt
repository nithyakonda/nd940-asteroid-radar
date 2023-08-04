package com.udacity.asteroidradar.main

import android.app.Application
import androidx.lifecycle.*
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.PictureOfDay
import com.udacity.asteroidradar.api.NasaApi
import com.udacity.asteroidradar.api.parseAsteroidsJsonResult
import com.udacity.asteroidradar.database.getDatabase
import com.udacity.asteroidradar.repository.AsteroidsRepository
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

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val _status = MutableLiveData<NasaApiStatus>()

    val status: LiveData<NasaApiStatus>
        get() = _status

    private val _pictureOfDay = MutableLiveData<PictureOfDay>()

    val pictureOfDay : LiveData<PictureOfDay>
        get() = _pictureOfDay

//    private val _asteroids = MutableLiveData<List<Asteroid>>()
//
//    val asteroids : LiveData<List<Asteroid>>
//        get() = _asteroids

    private val _navigateToSelectedAsteroid = MutableLiveData<Asteroid>()

    val navigateToSelectedAsteroid: LiveData<Asteroid>
        get() = _navigateToSelectedAsteroid

    private val database = getDatabase(application)
    private val repository = AsteroidsRepository(database)

    init {
        getNasaPictureOfTheDay()
    }

    val asteroids = repository.asteroids

    private fun getStartDate(): String {
        val dateFormat = SimpleDateFormat(Constants.API_QUERY_DATE_FORMAT, Locale.getDefault())
        return dateFormat.format(Calendar.getInstance().time)
    }

    private fun getNasaPictureOfTheDay() {
        viewModelScope.launch {
            _status.value = NasaApiStatus.LOADING
            try {
                val result = NasaApi.retrofitService.getImageOfTheDay()
                _status.value = NasaApiStatus.DONE

                if (result.mediaType == "image") {
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