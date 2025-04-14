package com.lnd.booksrf.data.remote.model

import com.google.gson.annotations.SerializedName
import com.lnd.booksrf.utils.BookJsonKeys

data class BookDto (
    @SerializedName(BookJsonKeys.ID)
    var id: String? = null,
    @SerializedName(BookJsonKeys.THUMBNAIL)
    var thumbnail: String? = null,
    @SerializedName(BookJsonKeys.TITLE)
    var title: String? = null
)
