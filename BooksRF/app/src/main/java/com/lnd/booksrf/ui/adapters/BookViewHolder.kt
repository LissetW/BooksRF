package com.lnd.booksrf.ui.adapters

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.lnd.booksrf.data.remote.model.BookDto
import com.lnd.booksrf.databinding.BookElementBinding

class BookViewHolder(
    private val binding: BookElementBinding
): RecyclerView.ViewHolder(binding.root) {

    fun bind(book: BookDto){

        //Vincular las vistas con la información correspondiente
        binding.tvTitle.text = book.title

        Glide.with(binding.root.context)
            .load(book.thumbnail)
            .into(binding.ivThumbnail)
    }
}