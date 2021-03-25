package com.prism.poc.locationhistory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Krishna Upadhya on 27/03/20.
 */
public class StoredLocations {

    private List<LocationHistory> locationHistoryList;

    public List<LocationHistory> getLocationHistoryList() {
        if (locationHistoryList == null) {
            locationHistoryList = new ArrayList<>();
        }
        return locationHistoryList;
    }

    public void setLocationHistoryList(List<LocationHistory> locationHistoryList) {
        this.locationHistoryList = locationHistoryList;
    }
}
