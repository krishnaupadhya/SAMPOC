package com.prism.poc.locationhistory;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.prism.poc.AppConstants;
import com.prism.poc.GenericUtil;
import com.prism.poc.PrismTrackerApplication;
import com.prism.poc.events.LocationHistoryEvent;
import com.prism.poc.events.LocationHistoryUpdated;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by Krishna Upadhya on 27/03/20.
 */
public class LocationHistoryUtils {

    private static LocationHistoryUtils instance;

    private LocationHistoryUtils() {

    }

    public static LocationHistoryUtils getInstance() {
        if (instance == null) {
            instance = new LocationHistoryUtils();
        }
        return instance;
    }

    public void saveLocationToLocal(Location location) {
        if (location != null) {
            LocationHistory history = new LocationHistory(location.getLatitude(), location.getLongitude(), GenericUtil.getCurrentTimeInUTC());
            saveLocationInHistoryList(history);
        }
    }

    private void saveLocationInHistoryList(LocationHistory history) {
        String historyJson = GenericUtil.getFromSharedPreferences(AppConstants.SAVED_LOCATIONS);
        List<LocationHistory> historyList = new ArrayList<>();
        try {
            Gson gson = new Gson();
            if (!TextUtils.isEmpty(historyJson)) {
                StoredLocations storedLocations = gson.fromJson(historyJson, StoredLocations.class);
                if (storedLocations != null)
                    historyList = storedLocations.getLocationHistoryList();
            }
            historyList.add(history);
            StoredLocations storedLocations = new StoredLocations();
            storedLocations.setLocationHistoryList(historyList);
            historyJson = gson.toJson(storedLocations);
            if (!TextUtils.isEmpty(historyJson)) {
                GenericUtil.putInSharedPreferences(AppConstants.SAVED_LOCATIONS, historyJson);
            }
            EventBus.getDefault().postSticky(new LocationHistoryUpdated());
        } catch (Exception e) {
            GenericUtil.handleException(e);
        }
    }

    public List<LocationHistory> getLocationHistoryList() {
        try {
            String historyJson = GenericUtil.getFromSharedPreferences(AppConstants.SAVED_LOCATIONS);
            Gson gson = new Gson();
            if (!TextUtils.isEmpty(historyJson)) {
                StoredLocations storedLocations = gson.fromJson(historyJson, StoredLocations.class);
                if (storedLocations != null
                        && storedLocations.getLocationHistoryList() != null
                        && !storedLocations.getLocationHistoryList().isEmpty()) {
                    return storedLocations.getLocationHistoryList();
                }
            }
        } catch (Exception e) {
            GenericUtil.handleException(e);
        }
        return null;
    }

    public List<UserLocationPost> getAddressListFromLocationHistory() {
        try {
            List<UserLocationPost> userLocationList = new ArrayList<>();
            List<LocationHistory> list = getLocationHistoryList();
            if (list != null) {
                for (LocationHistory locationHistory : list) {
                    userLocationList.add(getUserLocation(locationHistory));
                }
            }
            return userLocationList;
        } catch (Exception e) {
            GenericUtil.handleException(e);
        }
        return null;
    }

    private UserLocationPost getUserLocation(LocationHistory currentLocation) {
        UserLocationPost post = new UserLocationPost();
        PostAddress address = GenericUtil.getCompleteAddressString(currentLocation.getLatitude(), currentLocation.getLongitude());
        GeoCode geoCode = new GeoCode();
        geoCode.setLatitude(currentLocation.getLatitude() + "");
        geoCode.setLongitude(currentLocation.getLongitude() + "");
        address.setGeoCode(geoCode);
        post.setAddress(address);
        post.setUpdatedTime(currentLocation.getTime());
        return post;
    }

    private HashMap<String, String> locationsMap;

    public String getLocation(String key) {
        if (TextUtils.isEmpty(key) || locationsMap == null) return null;
        return locationsMap.get(key);
    }

    public void setLocationsMap(String locationKey, String locationName) {
        if (locationsMap == null) locationsMap = new HashMap<>();
        this.locationsMap.put(locationKey, locationName);
    }


    public void getAddressFromLocation(List<LocationHistory> locationHistoryList) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                Geocoder geocoder = new Geocoder(PrismTrackerApplication.getInstance(), Locale.getDefault());
                try {
                    if (locationHistoryList != null
                            && !locationHistoryList.isEmpty())
                        for (LocationHistory history : locationHistoryList) {
                            try {
                                String locationDetails = getLocation("" + history.getLatitude() + history.getLongitude());
                                if (TextUtils.isEmpty(locationDetails)) {
                                    List<Address> list = geocoder.getFromLocation(history.getLatitude(), history.getLongitude(), 1);
                                    if (list != null && !list.isEmpty()) {
                                        locationDetails = getLocationName(list);
                                        GenericUtil.log(" sourceName  for " + history.getLatitude() + +history.getLongitude() + " ReverseGeoCode " + locationDetails);
                                        setLocationsMap("" + history.getLatitude() + history.getLongitude(), locationDetails);
                                    }
                                } else {
                                    GenericUtil.log(" sourceName from cache " + locationDetails);
                                }
                                history.setAddress(locationDetails);
                            } catch (Exception ex) {
                                GenericUtil.handleException(ex);
                            }
                        }
                } catch (Exception e) {
                    GenericUtil.handleException(e);
                } finally {
                    if (locationHistoryList != null && !locationHistoryList.isEmpty())
                        EventBus.getDefault().postSticky(new LocationHistoryEvent(locationHistoryList));
                }
            }
        };
        thread.start();
    }

    private String getLocationName(List<Address> list) {
        Address address = list.get(0);
        // sending back first address line and locality
        String addressLine = address.getAddressLine(0);
        if (!TextUtils.isEmpty(addressLine)) return addressLine;
        String sourceName = "";
        if (!TextUtils.isEmpty(address.getThoroughfare())) {
            sourceName += address.getThoroughfare().trim();
        }
        if (!TextUtils.isEmpty(sourceName) && !TextUtils.isEmpty(address.getLocality())) {
            sourceName += ", " + address.getLocality().trim();
        }
        if (TextUtils.isEmpty(sourceName) && !TextUtils.isEmpty(address.getLocality())) {
            sourceName += address.getLocality().trim();
        }
        return sourceName;
    }

}
