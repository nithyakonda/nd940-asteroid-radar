package com.udacity.asteroidradar.api

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NetworkAsteroid(val id: Long, val codename: String, val closeApproachDate: String,
                    val absoluteMagnitude: Double, val estimatedDiameter: Double,
                    val relativeVelocity: Double, val distanceFromEarth: Double,
                    val isPotentiallyHazardous: Boolean)