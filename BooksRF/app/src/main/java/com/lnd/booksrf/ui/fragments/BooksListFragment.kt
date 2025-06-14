package com.lnd.booksrf.ui.fragments

import android.media.MediaPlayer
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.lnd.booksrf.R
import com.lnd.booksrf.application.BooksRFApp
import com.lnd.booksrf.data.BookRepository
import com.lnd.booksrf.databinding.FragmentBooksListBinding
import com.lnd.booksrf.ui.NetworkAware
import com.lnd.booksrf.ui.adapters.BooksAdapter
import kotlinx.coroutines.launch
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavOptions
import com.lnd.booksrf.utils.NetworkMonitor
import com.lnd.booksrf.utils.message

class BooksListFragment : Fragment(), NetworkAware {

    private var _binding: FragmentBooksListBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: BookRepository
    private var shouldRetry = false

    private var mediaPlayer: MediaPlayer? = null

    private lateinit var networkMonitor: NetworkMonitor

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

        networkMonitor = NetworkMonitor(requireContext()) {
            if (shouldRetry) {
                fetchBooks()
            }
        }

        // Toolbar como ActionBar
        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbar)
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(false)
        setHasOptionsMenu(true)
        // Instanciar el repositorio desde la clase BooksRFApp
        repository = (requireActivity().application as BooksRFApp).repository
        fetchBooks()

        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_logout -> {
                        FirebaseAuth.getInstance().signOut()
                        requireActivity().message(getString(R.string.success_logout))
                        findNavController().navigate(
                            R.id.loginFragment,
                            null,
                            NavOptions.Builder()
                                .setPopUpTo(R.id.booksListFragment, true)
                                .build()
                        )
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

    }

    private fun fetchBooks() {
        lifecycleScope.launch {
            try {
                val books = repository.getBooks()

                binding.rvBooks.apply {
                    layoutManager = LinearLayoutManager(requireContext())
                    adapter = BooksAdapter(books) { selectedBook ->
                        // Reproducir sonido al seleccionar
                        mediaPlayer = MediaPlayer.create(requireContext(), R.raw.page_sound)
                        mediaPlayer?.start()
                        selectedBook.id?.let { id ->
                            val action = BooksListFragmentDirections
                                .actionBooksListFragmentToBookDetailFragment(id)
                            findNavController().navigate(action)
                        }
                    }
                }
                shouldRetry = false
            } catch (e: Exception) {
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

    override fun onStart() {
        super.onStart()
        networkMonitor.register()
    }

    override fun onStop() {
        super.onStop()
        networkMonitor.unregister()
    }

    override fun onNetworkAvailable() {
        if (shouldRetry) {
            fetchBooks()
        }
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer?.pause()
    }

    override fun onResume() {
        super.onResume()
        mediaPlayer?.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        mediaPlayer?.release()
        mediaPlayer = null
    }
}