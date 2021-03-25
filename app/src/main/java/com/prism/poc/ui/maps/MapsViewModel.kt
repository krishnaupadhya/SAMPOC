package com.prism.poc.ui.maps

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.prism.poc.GenericUtil

class MapsViewModel : ViewModel() {

    var mCurrentLocation: MutableLiveData<LatLng> = MutableLiveData<LatLng>().apply {
        val latLng = GenericUtil.getLocationFromSharedPref()
        latLng?.let {
            value = latLng
        }
    }

    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }
    val text: LiveData<String> = _text

    fun setLocation(location: LatLng?) {
        mCurrentLocation.value = location
    }

}