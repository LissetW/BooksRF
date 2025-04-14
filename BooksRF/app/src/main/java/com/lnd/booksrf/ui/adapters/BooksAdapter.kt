package com.lnd.booksrf.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lnd.booksrf.data.remote.model.BookDto
import com.lnd.booksrf.databinding.BookElementBinding

class BooksAdapter (
    private val books: List<BookDto>, // Los libros a desplegar
    private val onBookClick: (BookDto) -> Unit // Para los clicks
): RecyclerView.Adapter<BookViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding = BookElementBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookViewHolder(binding)
    }

    override fun getItemCount(): Int = books.size

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = books[position]

        holder.bind(book)

        //Para el click
        holder.itemView.setOnClickListener {
            onBookClick(book)
        }
    }
}