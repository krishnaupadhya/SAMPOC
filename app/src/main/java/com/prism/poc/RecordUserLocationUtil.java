package com.prism.poc;

import android.location.Location;

/**
 * Created by Krishna Upadhya on 20/03/20.
 */
public class RecordUserLocationUtil {

    private static RecordUserLocationUtil instance;
    private IRecordPostLocationListener iRecordPostLocationLisener;

    private RecordUserLocationUtil() {

    }

    public static RecordUserLocationUtil getInstance() {
        if (instance == null) {
            instance = new RecordUserLocationUtil();
        }
        return instance;
    }

    public void setListener(IRecordPostLocationListener listener) {
        iRecordPostLocationLisener = listener;
    }

    /**
     * @param location              - latest location to be posted to server
     */
    public void sendLocation(Location location) {
        if (location == null &&
                GenericUtil.isDistanceWithinRange(location)) {
            //  GenericUtil.logIntoIntoFirebase("failed to send distance " + GenericUtil.getDistanceFromLatLong(location));
            if (iRecordPostLocationLisener != null) {
                iRecordPostLocationLisener.onPostFailure(null);
            }
            return;
        }
        if (iRecordPostLocationLisener != null) {
            iRecordPostLocationLisener.postLocationStarted();
        }
        GenericUtil.putInSharedPreferences(AppConstants.LOCATION_UPDATED_TIME, GenericUtil.getCurrentTimeInUTC());
        GenericUtil.updateSentLocationInSharedPref(location);
    }

}
