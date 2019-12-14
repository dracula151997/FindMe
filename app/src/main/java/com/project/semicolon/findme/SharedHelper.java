package com.project.semicolon.findme;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.lang.ref.WeakReference;

public class SharedHelper {

    public static void saveLocation(Context context, String key, String value) {
        WeakReference<Context> contextWeakReference = new WeakReference<>(context);
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(contextWeakReference.get());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String getLocation(Context context, String key) {
        WeakReference<Context> contextWeakReference = new WeakReference<>(context);
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(contextWeakReference.get());
        return preferences.getString(key, "");

    }
}
