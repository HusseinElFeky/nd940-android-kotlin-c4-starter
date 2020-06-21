package com.udacity.project4.locationreminders.savereminder.selectreminderlocation

import android.Manifest
import android.annotation.SuppressLint
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.databinding.DataBindingUtil
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.LocationGetter
import com.udacity.project4.utils.PermissionUtils
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import com.udacity.project4.utils.truncate
import org.koin.android.ext.android.inject

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    // Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding

    private lateinit var locationGetter: LocationGetter

    private lateinit var map: GoogleMap
    private var marker: Marker? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_select_location,
            container,
            false
        )
        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        locationGetter = LocationGetter(requireContext())

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.btnSave.setOnClickListener {
            if (marker == null) {
                showSnackbar(getString(R.string.select_poi))
            } else {
                _viewModel.navigationCommand.postValue(NavigationCommand.Back)
            }
        }

        return binding.root
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style)
            )

            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }
    }

    private fun startLocationGetterFlow() {
        if (PermissionUtils.isPermissionGranted(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            enableMyLocation()
        } else {
            PermissionUtils.requestPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) { result ->
                if (result) {
                    enableMyLocation()
                } else {
                    showSnackbar(getString(R.string.permission_denied_explanation))
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        map.isMyLocationEnabled = true

        map.setOnMyLocationButtonClickListener {
            locationGetter.checkDeviceLocationSettings(this, true, {
                locationGetter.getLastLocation {
                    map.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 15.0f)
                    )
                }
            }, {
                showSnackbar(getString(R.string.location_required_error))
            })
            true
        }

        locationGetter.getLastLocation {
            map.moveCamera(
                CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 15.0f)
            )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        setMapStyle(map)
        startLocationGetterFlow()

        map.setOnPoiClickListener {
            marker?.remove()

            val locationName = it.name
            _viewModel.updateSelectedLocation(it.latLng, locationName, it)

            marker = map.addMarker(
                MarkerOptions().position(it.latLng)
                    .title("Selected Location")
                    .snippet(locationName)
            )
        }

        map.setOnMapClickListener {
            marker?.remove()

            val locationName = "${it.latitude.truncate(6)}, ${it.longitude.truncate(6)}"
            _viewModel.updateSelectedLocation(it, locationName)

            marker = map.addMarker(
                MarkerOptions().position(it)
                    .title("Selected Location")
                    .snippet(locationName)
            )
        }
    }

    companion object {
        private val TAG = SelectLocationFragment::class.java.simpleName
    }
}
