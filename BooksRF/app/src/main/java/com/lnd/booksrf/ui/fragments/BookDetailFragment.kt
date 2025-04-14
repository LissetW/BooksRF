package com.lnd.booksrf.ui.fragments

import android.annotation.SuppressLint
import android.graphics.text.LineBreaker
import android.os.Build.VERSION_CODES.Q
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.lnd.booksrf.R
import com.lnd.booksrf.application.BooksRFApp
import com.lnd.booksrf.data.BookRepository
import com.lnd.booksrf.utils.isAtLeastAndroid
import com.lnd.booksrf.databinding.FragmentBookDetailBinding
import com.lnd.booksrf.ui.NetworkAware
import com.lnd.booksrf.utils.Constants
import kotlinx.coroutines.launch

private const val ARG_BOOKID = "id"

class BookDetailFragment : Fragment() , NetworkAware {

    private var _binding: FragmentBookDetailBinding? = null
    private val binding get() = _binding!!

    private var bookId: String? = null

    private lateinit var repository: BookRepository
    private var shouldRetry = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { args ->
            bookId = args.getString(ARG_BOOKID)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflar la vista correspondiente
        _binding = FragmentBookDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Instanciar el repositorio desde la clase BooksRFApp
        repository = (requireActivity().application as BooksRFApp).repository
        fetchBook()
    }

    private fun fetchBook(){
        lifecycleScope.launch {
            try {
                val bookDetail = repository.getBooksDetail(bookId)

                binding.apply {
                    tvTitle.text = bookDetail.title
                    Glide.with(requireActivity()).load(bookDetail.image).into(ivImage)
                    tvAuthors.text = bookDetail.authors
                    tvPages.text = context?.getString(R.string.pages, bookDetail.pages)
                    tvPublisher.text = bookDetail.publisher
                    tvYear.text = bookDetail.year
                    tvGenre.text = bookDetail.genre

                    tvLongDesc.text = bookDetail.summary
                    isAtLeastAndroid(Q){
                        tvLongDesc.justificationMode = LineBreaker.JUSTIFICATION_MODE_INTER_WORD
                    }
                }
                shouldRetry = false

            }catch (e: Exception){
                shouldRetry = true
                Toast.makeText(
                    requireContext(),
                    context?.getString(R.string.connection_error),
                    Toast.LENGTH_SHORT
                ).show()
                Log.d(Constants.LOGTAG, "Error al cargar el detail: $e")
            }finally {
                binding.pbLoading.visibility = View.GONE

            }

        }
    }

    override fun onNetworkAvailable() {
        if (shouldRetry) {
            fetchBook()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        // Instancia al fragment
        @JvmStatic
        fun newInstance(id: String) =
            BookDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_BOOKID, id)
                }
            }
    }
}