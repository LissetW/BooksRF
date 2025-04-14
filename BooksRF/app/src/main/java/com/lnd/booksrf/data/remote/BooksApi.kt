package com.lnd.booksrf.data.remote

import com.lnd.booksrf.data.remote.model.BookDetailDto
import com.lnd.booksrf.data.remote.model.BookDto
import retrofit2.http.GET
import retrofit2.http.Path

interface BooksApi {
    @GET("books/books_list")
    suspend fun getBooks(): List<BookDto>

    @GET("books/book_detail/{id}")
    suspend fun getBookDetail(
        @Path("id")
        id: String?
    ): BookDetailDto
}