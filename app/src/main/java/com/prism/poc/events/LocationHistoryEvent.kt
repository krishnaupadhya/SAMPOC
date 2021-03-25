package com.prism.poc.events

import com.prism.poc.locationhistory.LocationHistory

data class LocationHistoryEvent(val locationHistoryList: List<LocationHistory>)