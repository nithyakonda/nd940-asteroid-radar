package com.udacity.asteroidradar.main

import android.app.Application
import androidx.lifecycle.*
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.PictureOfDay
import com.udacity.asteroidradar.api.NasaApi
import com.udacity.asteroidradar.api.getToday
import com.udacity.asteroidradar.database.getDatabase
import com.udacity.asteroidradar.repository.AsteroidsRepository
import kotlinx.coroutines.launch
import timber.log.Timber

enum class NasaApiStatus { LOADING, ERROR, DONE }
enum class NasaApiFilter {SHOW_TODAY, SHOW_NEXT_WEEK, SHOW_SAVED}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val _status = MutableLiveData<NasaApiStatus>()

    val status: LiveData<NasaApiStatus>
        get() = _status

    private val _pictureOfDay = MutableLiveData<PictureOfDay>()

    val pictureOfDay : LiveData<PictureOfDay>
        get() = _pictureOfDay

    private val _navigateToSelectedAsteroid = MutableLiveData<Asteroid>()

    val navigateToSelectedAsteroid: LiveData<Asteroid>
        get() = _navigateToSelectedAsteroid

    private val database = getDatabase(application)
    private val repository = AsteroidsRepository(database)

    init {
        getNasaPictureOfTheDay()
        viewModelScope.launch {
            if (!repository.hasAsteroidsForToday()) {
                repository.refreshAsteroids(getToday())
            }
        }
    }

    val asteroids = repository.asteroids

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
    fun updateFilter(filter: NasaApiFilter) {
        repository.updateFilter(filter)
    }

    fun displayAsteroidDetails(asteroid: Asteroid) {
        _navigateToSelectedAsteroid.value = asteroid
    }

    fun displayAsteroidDetailsComplete() { _navigateToSelectedAsteroid.value = null }
}