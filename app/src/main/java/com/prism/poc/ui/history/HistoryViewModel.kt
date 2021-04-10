package com.prism.poc.ui.history

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prism.poc.db.HistoryLocation
import com.prism.poc.db.LocationRepository
import com.prism.poc.locationhistory.LocationHistoryUtils
import kotlinx.coroutines.launch

class HistoryViewModel(private val repository: LocationRepository) : ViewModel() {

     val locations:MutableLiveData<List<HistoryLocation>>? = MutableLiveData<List<HistoryLocation>>()

    fun getHistoryLocationsList() {
        viewModelScope.launch {
            val history= repository.getAll()
            history.let {
                locations?.value=it
            }
        }
    }



}