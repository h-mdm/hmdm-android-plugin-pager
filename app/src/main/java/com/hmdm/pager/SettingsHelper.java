package com.hmdm.pager;


import android.content.Context;
import android.content.SharedPreferences;

import com.hmdm.MDMService;

import java.util.Set;

// A simple wrapper of Preferences
public class SettingsHelper {
    private static final String PREFERENCES = "com.hmdm.pager.PREFERENCES";

    // Ключи данных
    public static final String KEY_SERVER_HOST = MDMService.KEY_SERVER_HOST;
    public static final String KEY_SECONDARY_SERVER_HOST = MDMService.KEY_SECONDARY_SERVER_HOST;
    public static final String KEY_SERVER_PATH = MDMService.KEY_SERVER_PATH;
    public static final String KEY_DEVICE_ID = MDMService.KEY_DEVICE_ID;

    private SharedPreferences sharedPreferences;

    private static SettingsHelper instance;

    public static SettingsHelper getInstance(Context context) {
        if (instance == null) {
            instance = new SettingsHelper(context);
        }
        return instance;
    }

    public SettingsHelper(Context context) {
        sharedPreferences = context.getApplicationContext().getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }

    public String getString(String key) {
        return sharedPreferences.getString(key, null);
    }

    public boolean getBoolean(String key) {
        return sharedPreferences.getBoolean(key, false);
    }

    public int getInt(String key) {
        return sharedPreferences.getInt(key, 0);
    }

    public long getLong(String key) {
        return sharedPreferences.getLong(key, 0);
    }

    public Set<String> getStringSet(String key) {
        return sharedPreferences.getStringSet(key, null);
    }

    public void setString(String key, String value) {
        sharedPreferences.edit().putString(key, value).commit();
    }

    public void setBoolean(String key, boolean value) {
        sharedPreferences.edit().putBoolean(key, value).commit();
    }

    public void setInt(String key, int value) {
        sharedPreferences.edit().putInt(key, value).commit();
    }

    public void setLong(String key, long value) {
        sharedPreferences.edit().putLong(key, value).commit();
    }

    public void setStringSet(String key, Set<String> value) {
        // Sometimes StringSet fails to save!
        // https://stackoverflow.com/questions/51001328/shared-preferences-not-saving-stringset-when-application-is-killed-its-a-featu
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(key).commit();
        editor.putStringSet(key, value).commit();

        //sharedPreferences.edit().putStringSet(key, value).commit();
    }
}
