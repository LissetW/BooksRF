package com.lnd.booksrf.application

import android.app.Application
import com.lnd.booksrf.data.BookRepository
import com.lnd.booksrf.data.remote.RetrofitHelper

class BooksRFApp: Application() {
    private val retrofit by lazy{
        RetrofitHelper().getRetrofit()
    }

    val repository by lazy {
        BookRepository(retrofit)
    }
}