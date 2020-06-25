package com.udacity.project4.locationreminders.savereminder.selectreminderlocation

import android.Manifest
import android.annotation.SuppressLint
import android.content.res.Resources
import android.os.Build
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
        binding.lifecycleOwner = this
        binding.viewModel = _viewModel

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
        if (PermissionUtils.arePermissionsGranted(
                requireContext(),
                LOCATION_PERMISSIONS
            )
        ) {
            enableMyLocation()
        } else {
            PermissionUtils.requestPermissions(
                this,
                LOCATION_PERMISSIONS
            ) { result ->
                if (result.values.all { it }) {
                    enableMyLocation()
                } else {
                    showSnackbar(getString(R.string.permission_denied_explanation)) {
                        startLocationGetterFlow()
                    }
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
    }

    private fun initMapView() {
        val latitude = _viewModel.latitude.value
        val longitude = _viewModel.longitude.value
        val locationSnippet = _viewModel.reminderSelectedLocationStr.value

        if (latitude != null && longitude != null && locationSnippet != null) {
            val latLng = LatLng(latitude, longitude)
            marker = map.addMarker(
                MarkerOptions().position(latLng)
                    .title("Selected Location")
                    .snippet(locationSnippet)
            )
            map.moveCamera(
                CameraUpdateFactory.newLatLngZoom(latLng, 15.0f)
            )
        } else if (PermissionUtils.arePermissionsGranted(
                requireContext(),
                LOCATION_PERMISSIONS
            )
        ) {
            locationGetter.getLastLocation {
                map.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 15.0f)
                )
            }
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
        initMapView()

        map.setOnPoiClickListener {
            marker?.remove()

            val locationSnippet = it.name
            _viewModel.updateSelectedLocation(it.latLng, locationSnippet, it)

            marker = map.addMarker(
                MarkerOptions().position(it.latLng)
                    .title("Selected Location")
                    .snippet(locationSnippet)
            )
        }

        map.setOnMapClickListener {
            marker?.remove()

            val locationSnippet = "${it.latitude.truncate(6)}, ${it.longitude.truncate(6)}"
            _viewModel.updateSelectedLocation(it, locationSnippet)

            marker = map.addMarker(
                MarkerOptions().position(it)
                    .title("Selected Location")
                    .snippet(locationSnippet)
            )
        }
    }

    companion object {
        private val TAG = SelectLocationFragment::class.java.simpleName
        private val LOCATION_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        } else {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }
}
