package com.udacity.asteroidradar.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.api.NasaApi
import com.udacity.asteroidradar.api.getDayOffsetBy
import com.udacity.asteroidradar.api.getToday
import com.udacity.asteroidradar.api.parseAsteroidsJsonResult
import com.udacity.asteroidradar.asDatabaseModel
import com.udacity.asteroidradar.database.AsteroidsDatabase
import com.udacity.asteroidradar.database.EntityAsteroid
import com.udacity.asteroidradar.database.asDomainModel
import com.udacity.asteroidradar.main.NasaApiFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber

class AsteroidsRepository (private val database: AsteroidsDatabase) {
    private val _filter = MutableLiveData(NasaApiFilter.SHOW_SAVED)

    val asteroids: LiveData<List<Asteroid>> = Transformations.switchMap(_filter) {
        Transformations.map (getAsteroidsFromDatabase(_filter.value!!)) {
            it.asDomainModel()
        }
    }
    fun updateFilter(filter: NasaApiFilter) {
        _filter.value = filter
    }

    private fun getAsteroidsFromDatabase(filter: NasaApiFilter): LiveData<List<EntityAsteroid>> {
        return database.asteroidDao.getAsteroids(
            startDate = when (filter) {
                NasaApiFilter.SHOW_NEXT_WEEK -> getDayOffsetBy(1)
                else -> getToday()
            },
            endDate = when (filter) {
                NasaApiFilter.SHOW_TODAY ->  getToday()// Same as start date
                else -> getDayOffsetBy(8)
            }
        )
    }

    suspend fun hasAsteroidsForToday(): Boolean {
        var hasAsteroids:Boolean
        withContext(Dispatchers.IO) {
            hasAsteroids = database.asteroidDao.hasAsteroidsWithDate(getToday()) >= 1
        }
        return hasAsteroids
    }

    suspend fun refreshAsteroids(startDate: String) {
        withContext(Dispatchers.IO) {
            val response = NasaApi.retrofitService.getAsteroids(startDate)
            if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody != null) {
                    try {
                        val jsonResult = JSONObject(responseBody)
                        Timber.d("Response JSON \n$jsonResult")
                        jsonResult?.let {
                            database.asteroidDao.insertAll(* parseAsteroidsJsonResult(jsonResult).asDatabaseModel())
                            Timber.i(jsonResult.toString())
                        }
                    } catch (e: JSONException) {
                        // Handle JSON parsing error
                        e.printStackTrace()
                    }
                } else {
                    Timber.e("Null response body")
                }
            } else {
                Timber.i("Network request failed")
            }


        }
    }
}