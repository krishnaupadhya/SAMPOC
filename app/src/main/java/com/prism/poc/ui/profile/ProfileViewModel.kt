package com.prism.poc.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prism.poc.GenericUtil
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = ""
    }
    val text: LiveData<String> = _text

    init {
        fetchUserDetails()
    }

    private fun fetchUserDetails() {
        _text.value = GenericUtil.getUserName()
    }
}