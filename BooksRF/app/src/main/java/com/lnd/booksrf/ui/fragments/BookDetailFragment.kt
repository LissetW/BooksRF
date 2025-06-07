package com.lnd.booksrf.ui.fragments

import android.graphics.text.LineBreaker
import android.media.MediaPlayer
import android.os.Build.VERSION_CODES.Q
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.lnd.booksrf.R
import com.lnd.booksrf.application.BooksRFApp
import com.lnd.booksrf.data.BookRepository
import com.lnd.booksrf.utils.isAtLeastAndroid
import com.lnd.booksrf.databinding.FragmentBookDetailBinding
import com.lnd.booksrf.ui.NetworkAware
import com.lnd.booksrf.utils.Constants
import kotlinx.coroutines.launch
import androidx.navigation.fragment.navArgs
import com.lnd.booksrf.data.remote.model.BookDetailDto
import com.lnd.booksrf.utils.NetworkMonitor

class BookDetailFragment : Fragment(), NetworkAware {

    private lateinit var networkMonitor: NetworkMonitor
    private var _binding: FragmentBookDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: BookRepository
    private var shouldRetry = false
    private var videoUrl: String? = null

    private val args: BookDetailFragmentArgs by navArgs()

    private var mediaPlayer: MediaPlayer? = null

    private var bookDetail: BookDetailDto? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        networkMonitor = NetworkMonitor(requireContext()) {
            if (shouldRetry) {
                fetchBook()
            }
        }

        repository = (requireActivity().application as BooksRFApp).repository

        fetchBook()

        binding.btnWatchReview.setOnClickListener {
            val url = videoUrl
            if (!url.isNullOrEmpty()) {
                val action = BookDetailFragmentDirections
                    .actionBookDetailFragmentToVideoFragment(url)
                findNavController().navigate(action)
            } else {
                Toast.makeText(requireContext(),
                    getString(R.string.no_video_found), Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnAuthorCity.setOnClickListener {
            val location = bookDetail?.location
            if (location != null) {
                val mapsFragment = MapsFragment.newInstance(
                    location.name ?: requireActivity().getString(R.string.unknown_location),
                    location.latitude,
                    location.longitude
                )
                mapsFragment.show(parentFragmentManager, "MapsDialog")
            } else {
                Toast.makeText(requireContext(), R.string.location_not_available, Toast.LENGTH_SHORT).show()
            }
        }


        mediaPlayer = MediaPlayer.create(requireContext(), R.raw.piano)
        mediaPlayer?.isLooping = true
        mediaPlayer?.start()

    }

    private fun fetchBook() {
        val bookId = args.id

        lifecycleScope.launch {
            try {
                bookDetail = repository.getBooksDetail(bookId)
                videoUrl = bookDetail?.video ?: ""
                binding.apply {
                    tvTitle.text = bookDetail?.title
                    Glide.with(requireActivity()).load(bookDetail?.image).into(ivImage)
                    tvAuthors.text = bookDetail?.authors
                    tvPages.text = context?.getString(R.string.pages, bookDetail?.pages ?: 0)
                    tvPublisher.text = bookDetail?.publisher
                    tvYear.text = bookDetail?.year
                    tvGenre.text = bookDetail?.genre
                    tvLongDesc.text = bookDetail?.summary

                    isAtLeastAndroid(Q) {
                        tvLongDesc.justificationMode = LineBreaker.JUSTIFICATION_MODE_INTER_WORD
                    }
                }
                shouldRetry = false
            } catch (e: Exception) {
                shouldRetry = true
                Toast.makeText(
                    requireContext(),
                    context?.getString(R.string.connection_error),
                    Toast.LENGTH_SHORT
                ).show()
                Log.d(Constants.LOGTAG, getString(R.string.detail_load_error, e))
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
            fetchBook()
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
        mediaPlayer?.release()
        _binding = null
    }
}