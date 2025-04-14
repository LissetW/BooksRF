package com.lnd.booksrf.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.lnd.booksrf.R
import com.lnd.booksrf.application.BooksRFApp
import com.lnd.booksrf.data.BookRepository
import com.lnd.booksrf.databinding.FragmentBooksListBinding
import com.lnd.booksrf.ui.NetworkAware
import com.lnd.booksrf.ui.adapters.BooksAdapter
import com.lnd.booksrf.utils.Constants
import kotlinx.coroutines.launch


class BooksListFragment : Fragment(), NetworkAware {

    private var _binding: FragmentBooksListBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: BookRepository
    private var shouldRetry = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentBooksListBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Aquí ya está el fragment en pantalla
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Instanciar el repositorio desde la clase BooksRFApp
        repository = (requireActivity().application as BooksRFApp).repository
        fetchBooks()
    }

    private fun fetchBooks(){
        lifecycleScope.launch {
            try {
                val books = repository.getBooks()

                binding.rvBooks.apply {
                    layoutManager = LinearLayoutManager(requireContext())
                    adapter = BooksAdapter(books){ selectedBook ->
                        // Click de cada libro
                        // Pasar al siguiente fragment con el id del libro seleccionado
                        selectedBook.id?.let{ id ->
                            requireActivity().supportFragmentManager.beginTransaction()
                                .replace(
                                    R.id.fragment_container,
                                    BookDetailFragment.newInstance(id)
                                )
                                .addToBackStack(null)
                                .commit()
                        }
                    }
                }
                shouldRetry = false
            } catch (e: Exception) {
                // Manejar la excepción
                e.printStackTrace()
                shouldRetry = true
                Toast.makeText(
                    requireContext(),
                    context?.getString(R.string.connection_error),
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                binding.pbLoading.visibility = View.GONE
            }
        }
    }
    override fun onNetworkAvailable() {
        if (shouldRetry) {
            fetchBooks()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}