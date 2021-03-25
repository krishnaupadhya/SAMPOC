package com.prism.poc.ui.history

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.prism.poc.locationhistory.LocationHistory
import com.prism.poc.locationhistory.LocationHistoryUtils

class HistoryViewModel : ViewModel() {

     val locations = MutableLiveData<List<LocationHistory>>()

    fun getHistoryLocationsList() {
        LocationHistoryUtils.getInstance()
            .getAddressFromLocation(LocationHistoryUtils.getInstance().locationHistoryList)
    }

    fun setLocationHistory(locationHistoryList: List<LocationHistory>) {
        locations.value = locationHistoryList
    }


}