package com.prism.poc;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;
import com.prism.poc.locationhistory.PostAddress;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import static com.prism.poc.DetachableClickListener.wrap;

/**
 * Created by Krishna Upadhya on 19/03/20.
 */
public class GenericUtil {

    private static long locationUpdatedTimeInMilliSeconds = 0;
    public static boolean mIsActivityInForeGround;

    public static void log(String tag, String message) {
        if (!BuildConfig.BUILD_TYPE.equalsIgnoreCase(AppConstants.RELEASE)) {
            Log.d(tag, message);
        }
    }

    public static void log(String message) {
        if (!BuildConfig.BUILD_TYPE.equalsIgnoreCase(AppConstants.RELEASE)) {
            Log.d(AppConstants.PRISM_APP_TAG, message);
        }
    }

    public static void handleException(Exception e) {
        if (!BuildConfig.BUILD_TYPE.equalsIgnoreCase(AppConstants.RELEASE)) {
            GenericUtil.log(AppConstants.PRISM_APP_TAG, Log.getStackTraceString(e));
        }
    }


    public static boolean isNetworkAvailable() {
        try {
            ConnectivityManager cm = (ConnectivityManager) PrismTrackerApplication.getInstance()
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                GenericUtil.log(AppConstants.PRISM_APP_TAG, "***Available***");
                return true;
            }
            GenericUtil.log(AppConstants.PRISM_APP_TAG, "***Not Available***");
        } catch (Exception ex) {
            GenericUtil.handleException(ex);
        }
        return false;
    }


    public static void putInSharedPreferences(String key, String value) {
        SharedPreferencesUtility.putOnSharedPreference(key, value);
    }

    public static String getFromSharedPreferences(String key) {
        return SharedPreferencesUtility.getStringFromSharedPreferences(key);
    }

    public static boolean isUserLoggedIn() {
        String name=getFromSharedPreferences(AppConstants.USER_NAME);
        return !TextUtils.isEmpty(name);
    }

    public static String getUserName() {
        return getFromSharedPreferences(AppConstants.USER_NAME);
    }

    public static void clearDataOnLogout() {
        putInSharedPreferences(AppConstants.USER_NAME,"");
        putInSharedPreferences(AppConstants.SAVED_LOCATIONS,"");
    }


    public static void putBooleanInSharedPreferences(String key, boolean value) {
        SharedPreferencesUtility.putBooleanOnSharedPreference(key, value);
    }

    public static boolean getBooleanFromSharedPreferences(String key) {
        return SharedPreferencesUtility.getBooleanFromSharedPreferences(key);
    }

    public static double getDistanceFromLatLong(LatLng latLngA, LatLng latLngB) {
        try {
            return SphericalUtil.computeDistanceBetween(latLngA, latLngB);
        } catch (Exception e) {
            GenericUtil.handleException(e);
        }
        return -1;
    }

    public static void updateSentLocationInSharedPref(Location currentLocation) {
        if (currentLocation != null) {
            putInSharedPreferences(AppConstants.LAST_KNOWN_LATITUDE, currentLocation.getLatitude() + "");
            putInSharedPreferences(AppConstants.LAST_KNOWN_LONGITUDE, currentLocation.getLongitude() + "");
        }
    }


    public static LatLng getLocationFromSharedPref() {
        try {
            String lat = getFromSharedPreferences(AppConstants.LAST_KNOWN_LATITUDE);
            String lon = getFromSharedPreferences(AppConstants.LAST_KNOWN_LONGITUDE);
            GenericUtil.log("getLocationFromSharedPref is= " + lat + " " + lon);
            return new LatLng(Double.parseDouble(lat), Double.parseDouble(lon));
        } catch (Exception e) {
            GenericUtil.handleException(e);
        }
        return null;
    }

    public static boolean isDistanceWithinRange(Location location) {

        LatLng latLngA = getLocationFromSharedPref();
        if (location == null || latLngA == null)
        {
            GenericUtil.log("Distance is= locationB or locationA is null");
            return false;
        }
        LatLng latLngB = new LatLng(location.getLatitude(), location.getLongitude());
        double distance = getDistanceFromLatLong(latLngA,latLngB);
        GenericUtil.log("Distance is= " + distance);
        return distance != -1 && (distance > 0 && distance < AppConstants.DEFAULT_LOCATION_RADIUS) ;
    }


    public static long getSavedLocationRadius() {
        int radius = AppConstants.DEFAULT_LOCATION_RADIUS;
        try {
            radius = Integer.parseInt(getFromSharedPreferences(AppConstants.LOCATION_RADIUS));
        } catch (Exception e) {
            GenericUtil.handleException(e);
        }
        GenericUtil.log("LOCATION_RADIUS " + radius);
        return radius;
    }

    public static String getCurrentTimeInUTC() {
        try {
            Date date = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat(AppConstants.DATE_FORMAT_YYYY_MM_DD_T_HH_MM_SS_SSS_Z, Locale.getDefault());
            dateFormat.setTimeZone(TimeZone.getTimeZone(AppConstants.TIME_ZONE_UTC));
            return dateFormat.format(date);
        } catch (Exception e) {
            GenericUtil.handleException(e);
        }
        return "";
    }

    public static String getCurrentTime() {
        try {
            Date date = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat(AppConstants.DATE_FORMATE_DD_MMM_YYYY, Locale.getDefault());
            return dateFormat.format(date);
        } catch (Exception e) {
        }
        return "";
    }

    public static PostAddress getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        PostAddress postAddress = new PostAddress();
        Geocoder geocoder = new Geocoder(PrismTrackerApplication.getInstance(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                postAddress.setAddressLine1(returnedAddress.getAddressLine(0));
                GenericUtil.log(postAddress.getAddressLine1());
                postAddress.setAddressLine2(returnedAddress.getSubLocality());
                postAddress.setCity(returnedAddress.getLocality());
                postAddress.setState(returnedAddress.getAdminArea());
                postAddress.setCountry(returnedAddress.getCountryName());
                postAddress.setZipCode(returnedAddress.getPostalCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return postAddress;
    }

    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        } else {
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }

    public static long getAlarmTime() {
        long timeInMilliSec = 60 * 1000;
        return System.currentTimeMillis() + timeInMilliSec;
    }

    public static void updateLocationBackGroundService(Context context) {
        try {
            if (GenericUtil.isLocationEnabled(context) ) {
                GenericUtil.log("updateLocationBackGroundService");
                Intent startIntent = new Intent(context, UploadLocationService.class);
                startIntent.putExtra(AppConstants.KEY_INTENT_SERVICE_TYPE, AppConstants.START_UPLOADING_LOCATON);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(startIntent);
                } else {
                    context.startService(startIntent);
                }
            }
        } catch (Exception ex) {
            GenericUtil.handleException(ex);
        }

    }

    public static void showAppDetailOnPermissionDenied(Activity activity) {
        Intent intent = new Intent();
        intent.setAction(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package",
                BuildConfig.APPLICATION_ID, null);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);


    }

    public static void initLocationTrackerAlarmManager(Context context) {
        if (context == null) return;
        GenericUtil.log(" initLocationTrackerAlarmManager");
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context, AlarmReceiver.class);
            intent.putExtra(AppConstants.KEY_INTENT_SERVICE_TYPE, AppConstants.START_UPLOADING_LOCATON);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            if (Build.VERSION.SDK_INT >= 23) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, getAlarmTime(), pendingIntent);
            } else if (Build.VERSION.SDK_INT >= 19) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, getAlarmTime(), pendingIntent);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, getAlarmTime(), pendingIntent);
            }
            GenericUtil.log(" initLocationTrackerAlarmManager success");
        } catch (Exception ex) {
            GenericUtil.log(" initLocationTrackerAlarmManager failed " + ex.getLocalizedMessage());
            GenericUtil.handleException(ex);
        }


    }

    public static void dismissKeypad(Activity activity) {
        try {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context
                    .INPUT_METHOD_SERVICE);
            if (null != activity.getCurrentFocus())
                imm.hideSoftInputFromWindow(activity.getCurrentFocus().getApplicationWindowToken
                        (), 0);
        } catch (Exception e) {
            GenericUtil.handleException(e);
        }
    }

    public static void showKeypad(Activity activity, View view) {
        if (activity != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputMethodManager != null) {
                inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            }
        }
    }

    public static void showCustomAlertDialogBtnClick(Context context, String title, String message,
                                                     DialogInterface.OnClickListener onOkClickListener) {

        if (context != null) {
            try {
                AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style
                        .AppCompatAlertDialogStyle);
                onOkClickListener = wrap(onOkClickListener);
                builder.setPositiveButton(android.R.string.ok, onOkClickListener);
                builder.setTitle(title);
                builder.setMessage(message);
                AlertDialog dialog = builder.create();

                dialog.show();
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor
                        (context, R.color.alert_text_color));
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                ((DetachableClickListener) onOkClickListener).clearOnDetach(dialog);
            } catch (Exception e) {
                handleException(e);
            }
        }
    }

    public static void showCustomAlertDialog(Context context, String title, String message) {
        if (context != null) {
            try {
                AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style
                        .AppCompatAlertDialogStyle);
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setTitle(title);
                builder.setMessage(message);
                AlertDialog dialog = builder.create();

                dialog.show();
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor
                        (context, R.color.black));
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            } catch (Exception e) {
                e.getLocalizedMessage();
            }
        }
    }
}
