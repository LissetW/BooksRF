package com.lnd.booksrf.data

import com.lnd.booksrf.data.remote.BooksApi
import com.lnd.booksrf.data.remote.model.BookDetailDto
import com.lnd.booksrf.data.remote.model.BookDto
import retrofit2.Retrofit

class BookRepository (
    private val retrofit: Retrofit
)
{
    // Crear la instancia al api para acceder a los endpoints
    private val bookApi = retrofit.create(BooksApi::class.java)
    suspend fun getBooks(): List<BookDto> = bookApi.getBooks()
    suspend fun getBooksDetail(id: String?): BookDetailDto = bookApi.getBookDetail(id)
}