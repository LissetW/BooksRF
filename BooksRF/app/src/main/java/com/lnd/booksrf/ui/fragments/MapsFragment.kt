package com.lnd.booksrf.ui.fragments

import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.lnd.booksrf.R
import com.lnd.booksrf.databinding.FragmentMapsBinding
import androidx.core.graphics.drawable.toDrawable
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import androidx.core.graphics.scale

class MapsFragment : DialogFragment() {

    companion object {
        private const val ARG_NAME = "name"
        private const val ARG_LATITUDE = "latitude"
        private const val ARG_LONGITUDE = "longitude"

        fun newInstance(name: String, latitude: Double, longitude: Double): MapsFragment {
            val fragment = MapsFragment()
            val bundle = Bundle()
            bundle.putString(ARG_NAME, name)
            bundle.putDouble(ARG_LATITUDE, latitude)
            bundle.putDouble(ARG_LONGITUDE, longitude)
            fragment.arguments = bundle
            return fragment
        }
    }

    private var locationName: String? = null
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    private val callback = OnMapReadyCallback { googleMap ->
        val position = LatLng(latitude, longitude)
        val originalBitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_pin)
        val scaledBitmap = originalBitmap.scale(200, 200, false)

        val markerOptions = MarkerOptions()
            .position(position)
            .title(locationName)
            .icon(BitmapDescriptorFactory.fromBitmap(scaledBitmap))

        googleMap.addMarker(markerOptions)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 12f))
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            locationName = it.getString(ARG_NAME)
            latitude = it.getDouble(ARG_LATITUDE)
            longitude = it.getDouble(ARG_LONGITUDE)
        }
    }

    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.apply {
            setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setGravity(Gravity.BOTTOM)
        }
        return dialog
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            (resources.displayMetrics.heightPixels * 0.5).toInt() // 50% de alto
        )
        dialog?.window?.setGravity(Gravity.BOTTOM)
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnClose.setOnClickListener {
            dismiss()
        }

        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}