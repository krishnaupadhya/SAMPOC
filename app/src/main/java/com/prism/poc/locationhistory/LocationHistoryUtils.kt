package com.prism.poc.locationhistory

import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.text.TextUtils
import com.google.gson.Gson
import com.prism.poc.AppConstants
import com.prism.poc.GenericUtil
import com.prism.poc.PrismTrackerApplication
import com.prism.poc.db.HistoryLocation
import com.prism.poc.events.LocationHistoryEvent
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import java.util.*

/**
 * Created by Krishna Upadhya on 27/03/20.
 */
class LocationHistoryUtils private constructor() {

    fun saveLocationToLocal(location: Location?) {
        if (location != null) {
            val history = LocationHistory(
                location.latitude,
                location.longitude,
                GenericUtil.getCurrentTimeInUTC()
            )
            saveLocationToDb(history)
            GenericUtil.putInSharedPreferences(
                AppConstants.LOCATION_UPDATED_TIME,
                GenericUtil.getCurrentTimeInUTC()
            )
            GenericUtil.updateSentLocationInSharedPref(location)
        }
    }

    private fun saveLocationInHistoryList(history: LocationHistory) {

//
//        var historyJson =
//            GenericUtil.getFromSharedPreferences(AppConstants.SAVED_LOCATIONS)
//        var historyList: MutableList<LocationHistory?> =
//            ArrayList()
//        try {
//            val gson = Gson()
//            if (!TextUtils.isEmpty(historyJson)) {
//                val storedLocations =
//                    gson.fromJson(historyJson, StoredLocations::class.java)
//                if (storedLocations != null) historyList = storedLocations.locationHistoryList
//            }
//            historyList.add(history)
//            val storedLocations = StoredLocations()
//            storedLocations.locationHistoryList = historyList
//            historyJson = gson.toJson(storedLocations)
//            if (!TextUtils.isEmpty(historyJson)) {
//                GenericUtil.putInSharedPreferences(AppConstants.SAVED_LOCATIONS, historyJson)
//            }
//            EventBus.getDefault().postSticky(LocationHistoryUpdated())
//        } catch (e: Exception) {
//            GenericUtil.handleException(e)
//        }

    }

    private fun saveLocationToDb(history: LocationHistory) {
        val oldLoc=GenericUtil.getLocationFromSharedPref()
        if(oldLoc!=null && oldLoc.latitude==history.latitude && oldLoc.longitude==history.longitude){
            GenericUtil.log("Same as old location old - ${oldLoc} , new ${history.latitude} , ${history.longitude}")
            return
        }
        GlobalScope.launch {
            try {
                val location = HistoryLocation(
                    id = Date().time,
                    latitude = history.latitude,
                    longitude = history.longitude,
                    time = history.time
                )
                location.address = getAddressForLocation(location.latitude, location.longitude)
                PrismTrackerApplication.instance?.repository?.insert(location)
                GenericUtil.log("DB loc inserted - address ${location.address}")
            } catch (e: java.lang.Exception) {
                GenericUtil.log("DB loc insert failed - ${e.message}")
                GenericUtil.handleException(e)
            }
        }
    }

    val locationHistoryList: List<LocationHistory>?
        get() {
            try {
                val historyJson =
                    GenericUtil.getFromSharedPreferences(AppConstants.SAVED_LOCATIONS)
                val gson = Gson()
                if (!TextUtils.isEmpty(historyJson)) {
                    val storedLocations =
                        gson.fromJson(historyJson, StoredLocations::class.java)
                    if (storedLocations != null && storedLocations.locationHistoryList != null && !storedLocations.locationHistoryList.isEmpty()
                    ) {
                        return storedLocations.locationHistoryList
                    }
                }
            } catch (e: Exception) {
                GenericUtil.handleException(e)
            }
            return null
        }

    val addressListFromLocationHistory: List<UserLocationPost>?
        get() {
            try {
                val userLocationList: MutableList<UserLocationPost> =
                    ArrayList()
                val list = locationHistoryList
                if (list != null) {
                    for (locationHistory in list) {
                        userLocationList.add(getUserLocation(locationHistory))
                    }
                }
                return userLocationList
            } catch (e: Exception) {
                GenericUtil.handleException(e)
            }
            return null
        }

    private fun getUserLocation(currentLocation: LocationHistory): UserLocationPost {
        val post = UserLocationPost()
        val address = GenericUtil.getCompleteAddressString(
            currentLocation.latitude,
            currentLocation.longitude
        )
        val geoCode = GeoCode()
        geoCode.latitude = currentLocation.latitude.toString() + ""
        geoCode.longitude = currentLocation.longitude.toString() + ""
        address.geoCode = geoCode
        post.address = address
        post.updatedTime = currentLocation.time
        return post
    }

    private var locationsMap: HashMap<String, String?>? = null
    fun getLocation(key: String?): String? {
        return if (TextUtils.isEmpty(key) || locationsMap == null) null else locationsMap!![key]
    }

    fun setLocationsMap(locationKey: String, locationName: String?) {
        if (locationsMap == null) locationsMap = HashMap()
        locationsMap!![locationKey] = locationName
    }

    fun getAddressFromLocation(locationHistoryList: List<LocationHistory>?) {
        val thread: Thread = object : Thread() {
            override fun run() {
                val geocoder =
                    Geocoder(PrismTrackerApplication.instance, Locale.getDefault())
                try {
                    if (locationHistoryList != null
                        && !locationHistoryList.isEmpty()
                    ) for (history in locationHistoryList) {
                        try {
                            var locationDetails =
                                getLocation("" + history.latitude + history.longitude)
                            if (TextUtils.isEmpty(locationDetails)) {
                                val list =
                                    geocoder.getFromLocation(
                                        history.latitude,
                                        history.longitude,
                                        1
                                    )
                                if (list != null && !list.isEmpty()) {
                                    locationDetails = getLocationName(list)
                                    GenericUtil.log(" sourceName  for " + history.latitude + +history.longitude + " ReverseGeoCode " + locationDetails)
                                    setLocationsMap(
                                        "" + history.latitude + history.longitude,
                                        locationDetails
                                    )
                                }
                            } else {
                                GenericUtil.log(" sourceName from cache $locationDetails")
                            }
                            history.address = locationDetails
                        } catch (ex: Exception) {
                            GenericUtil.handleException(ex)
                        }
                    }
                } catch (e: Exception) {
                    GenericUtil.handleException(e)
                } finally {
                    if (locationHistoryList != null && !locationHistoryList.isEmpty())
                        EventBus.getDefault().postSticky(LocationHistoryEvent(locationHistoryList))
                }
            }
        }
        thread.start()
    }

    private fun getLocationName(list: List<Address>): String {
        val address = list[0]
        // sending back first address line and locality
        val addressLine = address.getAddressLine(0)
        if (!TextUtils.isEmpty(addressLine)) return addressLine
        var sourceName = ""
        if (!TextUtils.isEmpty(address.thoroughfare)) {
            sourceName += address.thoroughfare.trim { it <= ' ' }
        }
        if (!TextUtils.isEmpty(sourceName) && !TextUtils.isEmpty(address.locality)) {
            sourceName += ", " + address.locality.trim { it <= ' ' }
        }
        if (TextUtils.isEmpty(sourceName) && !TextUtils.isEmpty(address.locality)) {
            sourceName += address.locality.trim { it <= ' ' }
        }
        return sourceName
    }

    companion object {
        @JvmStatic
        var instance: LocationHistoryUtils? = null
            get() {
                if (field == null) {
                    field = LocationHistoryUtils()
                }
                return field
            }
            private set
    }

    suspend fun getAddressForLocation(latitude: Double, longitude: Double): String {
        var locationDetails = ""
        val geocoder = Geocoder(PrismTrackerApplication.instance, Locale.getDefault())
        val list = geocoder.getFromLocation(latitude, longitude, 1)
        if (list != null && !list.isEmpty()) {
            locationDetails = getLocationName(list)
        }
        return locationDetails
    }
}