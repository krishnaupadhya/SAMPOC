package com.prism.poc.ui.maps

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.prism.poc.GenericUtil
import com.prism.poc.R
import com.prism.poc.events.LocationPostEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class MapsFragment : Fragment(), OnMapReadyCallback {

    private var mapsViewModel: MapsViewModel? = null
    private var mapView: MapView? = null
    private var mGoogleMap: GoogleMap? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_maps, container, false)
        initView(root)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapsViewModel = activity?.let {
            ViewModelProvider(it).get(MapsViewModel::class.java)
        }
        mapsViewModel?.mCurrentLocation?.observe(viewLifecycleOwner, Observer {
            updateLocationIntoMap(it)
        })
    }

    private fun initView(root: View?) {
        root?.let {
            mapView = root.findViewById(R.id.map_view)
            mapView?.let {
                it.onCreate(null)
                it.onResume()
                it.getMapAsync(this)
            }

        }
    }

    override fun onPause() {
        super.onPause()
        if (mapView != null) {
            mapView!!.onPause()
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        if (mapView != null) {
            mapView!!.onLowMemory()
        }
    }


    override fun onMapReady(googleMap: GoogleMap?) {
        if (googleMap == null) return
        mGoogleMap = googleMap
        setDefaultMapSettings(mGoogleMap)
        mapsViewModel?.let { updateLocationIntoMap(it.mCurrentLocation.value) }
    }

    private fun setDefaultMapSettings(googleMap: GoogleMap?) {
        if (googleMap != null) {
            googleMap.uiSettings.isZoomControlsEnabled = true
            googleMap.uiSettings.isMapToolbarEnabled = false
            googleMap.uiSettings.isRotateGesturesEnabled = true
            googleMap.uiSettings.isTiltGesturesEnabled = true
            googleMap.uiSettings.isCompassEnabled = false
            googleMap.isBuildingsEnabled = true
        }
    }

    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    override fun onStop() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
        super.onStop()
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onLocationFetched(locationPostEvent: LocationPostEvent) {
        // Add a marker in Sydney and move the camera
        var currentLocation = LatLng(locationPostEvent.lat, locationPostEvent.long)
        mapsViewModel?.setLocation(currentLocation)
        EventBus.getDefault().removeStickyEvent(locationPostEvent)
    }

    private fun updateLocationIntoMap(currentLocation: LatLng?) {
        currentLocation?.let { location ->
            mGoogleMap?.let {
                GenericUtil.log("updateLocationIntoMap ${location.latitude}, ${location.longitude}")
                it.addMarker(MarkerOptions().position(location).title("My Current Location"))
                it.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(location, 12.0f)
                )
            }
        }
    }
}