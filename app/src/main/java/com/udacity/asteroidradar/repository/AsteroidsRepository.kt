package com.udacity.asteroidradar.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.BuildConfig
import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.api.NasaApi
import com.udacity.asteroidradar.api.parseAsteroidsJsonResult
import com.udacity.asteroidradar.asDatabaseModel
import com.udacity.asteroidradar.database.AsteroidsDatabase
import com.udacity.asteroidradar.database.asDomainModel
import com.udacity.asteroidradar.main.NasaApiStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class AsteroidsRepository (private val database: AsteroidsDatabase) {
    val asteroids: LiveData<List<Asteroid>> = Transformations.map (database.asteroidDao.getAsteroids(getStartDate())) {
        it.asDomainModel()
    }

    suspend fun refreshAsteroids() {
        withContext(Dispatchers.IO) {
            val response = NasaApi.retrofitService.getAsteroids(getStartDate())
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

    private fun getStartDate(): String {
        val dateFormat = SimpleDateFormat(Constants.API_QUERY_DATE_FORMAT, Locale.getDefault())
        return dateFormat.format(Calendar.getInstance().time)
    }
}