package com.lnd.booksrf.data.remote.model

import com.google.gson.annotations.SerializedName
import com.lnd.booksrf.utils.BookJsonKeys

data class LocationDto(
    @SerializedName(BookJsonKeys.CITY_NAME)
    val name: String? = null,
    @SerializedName(BookJsonKeys.LATITUDE)
    val latitude: Double = 0.0,
    @SerializedName(BookJsonKeys.LONGITUDE)
    val longitude: Double = 0.0
)
