package com.prism.poc;

import org.jetbrains.annotations.Nullable;

/**
 * Created by Krishna Upadhya on 19/03/20.
 */
public class AppConstants {

    public static final String PRISM_APP_TAG = "prism_app_tag";
    public static final String USER_NAME="user_name";

    private AppConstants() {
        //Nothing to do
    }
    public static final String PREF_KEY_EMAIL = "pref_key_email";
    public static final String PREF_KEY_ACCESS_TOKEN = "pref_key_access_token";
    public static final String PREF_KEY_REFRESH_TOKEN = "pref_key_refresh_token";
    public static final String PREF_KEY_JWT_TOKEN = "pref_key_jwt_token";
    public static final String PREF_KEY_EXPIRY_TIME = "pref_key_expiry_time";
    public static final String PREF_KEY_DISCLAIMER_STATUS = "pref_key_disclaimer_status";
    public static final String PREF_KEY_USER_SETTINGS = "pref_key_user_settings";
    public static final String PREF_KEY_FIRST_LAUNCH = "pref_key_first_launch";
    static final String PREF_KEY_BLE_LAST_SYNC_DATE_TIME = "pref_key_ble_last_sync_date_time";
    public static final String PREF_KEY_REPORTED_STATUS = "pref_key_reported_status";
    public static final String PREF_KEY_MATCH_TOKENS = "pref_key_match_tokens";
    public static final String ACCEPTED = "accepted";
    public static final String SHOWN = "shown";

    static final String PREF_KEY_TEMP_BLE_LAST_SYNC_DATE_TIME = "pref_key_temp_ble_last_sync_date_time";

    public static final String TOKEN = "token";
    public static final String CLIENT_ID = "client_id";
    public static final String CLIENT_SECRET = "client_secret";
    public static final String CODE = "code";
    public static final String REFRESH_TOKEN = "refresh_token";
    public static final String GRANT_TYPE = "grant_type";
    public static final String REDIRECT_URI = "redirect_uri";


    public static final String DATE_FORMAT_YYYY_MM_DD_T_HH_MM_SS_SSS_Z = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    public static final String DATE_FORMAT_HH_MM_SS_ZZZZ = "HH:mm:ssZZZZ";
    static final String DATE_FORMAT_HH_MM_A = "hh:mm a";
    static final String DATE_FORMAT_HH_MM = "HH:mm";
    static final String DATE_FORMATE_DD_MMM_YYYY = "MM/dd/yyyy hh:mm a";
    static final String HH_MM = "HH:mm";
    static final String DATE_ONLY_FORMATE_DD_MM_YYYY = "dd-MM-yyyy";
    static final String DATE_ONLY_FORMATE_YYYY_MM_DD_HH_MM_SS_ZZZZ = "yyyy-MM-dd HH:mm:ssZZZZ";
    static final String TIME_ZONE_UTC = "UTC";
    public static final String LOCATION_UPDATED_TIME = "location_updated_time";
    static final String LAST_KNOWN_LATITUDE = "latitude";
    static final String LAST_KNOWN_LONGITUDE = "longitude";
    public static final String LOCATION_WORKER_TAG = "location_worker_tag";
    public static final String REPORT_STATUS_WELL = "Well";
    public static final String REPORT_STATUS_ILL = "ill";
    public static final String REPORT_STATUS_SUSPECTED = "Suspected";
    public static final String REPORT_STATUS_CURED = "Cured";
    public static final String TIME_DURATION_TO_CHECK_LOCATION ="TimeDurationToCheckLocation";
    public static final String LOCATION_RADIUS ="LocationRadius";
    static final long DEFAULT_TIME_TO_CHECK_LOCATION = 15; //in minutes
    static final int DEFAULT_LOCATION_RADIUS = 20; //in meters
    public static final String KEY_REPORT_STATUS = "report_status";
    public static final String FAILED = "failed";
    public static final String PERIODIC_WORK_UNIQUE_NAME = "periodic_work";
    public static final int PERMISSION_REQUEST_CODE = 1001;
    public final static int REQUEST_CHECK_SETTINGS_GPS = 0x1;
    public static final String START_UPLOADING_LOCATON = "start_uploading_locaton";
    public static final String STOP_UPLOADING_LOCATON = "stop_uploading_locaton";

    public static final int NOTIFICATION_ID_FOREGROUND_SERVICE = 8483403;
    public static final String KEY_INTENT_SERVICE_TYPE = "key_intent_service_type";
    static final String RELEASE = "release";

    public static final long ONE_MINUTE_IN_MILLI_SECONDS = 60000;
    public static final String SAVED_LOCATIONS = "saved_locations";
    public static final long TWELVE = 12;
    public static final String CLOSE_NOTIFICATION = "close_notification";
    public static final String COMMENT_ENTRY_TYPE = "comment_entry_point";
    public static final String INFECTED_REPORT_COMMENT = "infected_report_comment";
    public static final String ENTRY_TYPE_REPORT_SCREEN = "entry_type_report_screen";
    public static final String ENTRY_TYPE_STATUS_SCREEN = "entry_type_status_screen";
    public static final int MIN_CHAR_LENGTH_FOR_COMMENT = 10;
    public static final String ANDROID = "ANDROID";

    static final String SUNDAY = "sunday";
    static final String MONDAY = "monday";
    static final String TUESDAY = "tuesday";
    static final String WEDNESDAY = "wednesday";
    static final String THURSDAY = "thursday";
    static final String FRIDAY = "friday";
    static final String SATURDAY = "saturday";
    public static final String NOTIFICATION_UPDATED_TIME = "notification_updated_time";
    public static final long SIX_HOUR_IN_MILLI_SECONDS = 21600000;
    static final long TWELVE_HOURS_IN_MILLI_SECONDS = 10 * 60 * 1000; //(10 * 60 * 1000 = 10 mins) or  (43200000 = 12 hours)
    static final long THIRTY_MINUTES_IN_MILLI_SECONDS = 1800000;

    public static final long ACCESS_BLE_TOKENS_REPEAT_DURATION = 15;
    public static final String PERIODIC_BLE_TOEKN_WORK = "periodic_ble_token_work";
    public static final String START_BLE_TOKENS_MATCHING = "start_ble_tokens_matching";
    public static final int NOTIFICATION_ID_BLE_TOKEN_FOREGROUND_SERVICE = 8488504;


    public static final String REPORT_STATUS_SOURCE_APP = "APP";
    static final String REPORT_STATUS_SOURCE_BLE = "BLE";
    public static final String REPORT_STATUS_CATEGORY_TYPE = "IBMer";

    //Bluetooth
    public static final int REQUEST_ENABLE_BT = 2;

}
