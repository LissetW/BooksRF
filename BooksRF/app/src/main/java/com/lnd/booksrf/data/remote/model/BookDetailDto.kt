package com.lnd.booksrf.data.remote.model

import com.google.gson.annotations.SerializedName
import com.lnd.booksrf.utils.BookJsonKeys

data class BookDetailDto (
    @SerializedName(BookJsonKeys.TITLE)
    var title: String? = null,
    @SerializedName(BookJsonKeys.IMAGE)
    var image: String? = null,
    @SerializedName(BookJsonKeys.PAGES)
    var pages: Int = 0,
    @SerializedName(BookJsonKeys.AUTHORS)
    var authors: String? = null,
    @SerializedName(BookJsonKeys.PUBLISHER)
    var publisher: String? = null,
    @SerializedName(BookJsonKeys.YEAR)
    var year: String? = null,
    @SerializedName(BookJsonKeys.GENRE)
    var genre: String? = null,
    @SerializedName(BookJsonKeys.SUMMARY)
    var summary: String? = null,
)
