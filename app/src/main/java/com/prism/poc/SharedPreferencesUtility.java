package com.prism.poc;

import com.securepreferences.SecurePreferences;

/**
 * Created by Krishna Upadhya on 19/03/20.
 */
public class SharedPreferencesUtility {

    private static SecurePreferences securePrefs = null;
    private static final String DIVISION_ICONS = "divisionicons";

    private SharedPreferencesUtility() {

    }

    public static void remove(String key) {
        try {
            if (PrismTrackerApplication.getInstance() == null) {
                return;
            }

            if (null == securePrefs) {
                securePrefs = new SecurePreferences(PrismTrackerApplication.getInstance().getApplicationContext(), "", DIVISION_ICONS);
            }
            SecurePreferences.Editor editor = securePrefs.edit();
            editor.remove(key);
            editor.apply();
        } catch (Exception e) {
            GenericUtil.handleException(e);
        }
    }


    static void putOnSharedPreference(String key, String value) {
        try {
            if (PrismTrackerApplication.getInstance() == null) {
                return;
            }
            if (null == securePrefs) {
                securePrefs = new SecurePreferences(PrismTrackerApplication.getInstance().getApplicationContext(), "", DIVISION_ICONS);
            }
            SecurePreferences.Editor editor = securePrefs.edit();
            editor.putString(key, value);
            editor.apply();
        } catch (Exception e) {
            GenericUtil.handleException(e);
        }
    }


    static String getStringFromSharedPreferences(String key) {
        String defaultValue = "";
        try {
            if (PrismTrackerApplication.getInstance() == null) {
                return defaultValue;
            }
            if (null == securePrefs) {
                securePrefs = new SecurePreferences(PrismTrackerApplication.getInstance().getApplicationContext(), "", DIVISION_ICONS);
            }
            return securePrefs.getString(key, defaultValue);
        } catch (Exception e) {
            GenericUtil.handleException(e);
        }
        return defaultValue;
    }

    static void putBooleanOnSharedPreference(String key, boolean value) {
        try {
            if (PrismTrackerApplication.getInstance() == null) {
                return;
            }
            if (null == securePrefs) {
                securePrefs = new SecurePreferences(PrismTrackerApplication.getInstance().getApplicationContext(), "", DIVISION_ICONS);
            }
            SecurePreferences.Editor editor = securePrefs.edit();
            editor.putBoolean(key, value);
            editor.apply();
        } catch (Exception e) {
            GenericUtil.handleException(e);
        }
    }


    static boolean getBooleanFromSharedPreferences(String key) {
        try {
            if (PrismTrackerApplication.getInstance() == null) {
                return false;
            }
            if (null == securePrefs) {
                securePrefs = new SecurePreferences(PrismTrackerApplication.getInstance().getApplicationContext(), "", DIVISION_ICONS);
            }
            return securePrefs.getBoolean(key, false);
        } catch (Exception e) {
            GenericUtil.handleException(e);
        }
        return false;
    }

}
