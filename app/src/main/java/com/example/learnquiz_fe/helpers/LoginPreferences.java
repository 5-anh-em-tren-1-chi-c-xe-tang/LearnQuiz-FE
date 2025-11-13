package com.example.learnquiz_fe.helpers;

import android.content.Context;
import android.content.SharedPreferences;

public class LoginPreferences {

    private static final String PREFS_NAME = "login_prefs";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_REMEMBER = "remember";

    private final SharedPreferences prefs;

    public LoginPreferences(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // Save login info
    public void saveCredentials(String email, String password) {
        prefs.edit()
                .putString(KEY_EMAIL, email)
                .putString(KEY_PASSWORD, password)
                .putBoolean(KEY_REMEMBER, true)
                .apply();
    }

    // Clear login info
    public void clear() {
        prefs.edit().clear().apply();
    }

    // Should auto-login?
    public boolean isRemembered() {
        return prefs.getBoolean(KEY_REMEMBER, false);
    }

    public String getSavedEmail() {
        return prefs.getString(KEY_EMAIL, "");
    }

    public String getSavedPassword() {
        return prefs.getString(KEY_PASSWORD, "");
    }

    public void disableRemember() {
        prefs.edit()
                .putBoolean(KEY_REMEMBER, false)
                .remove(KEY_PASSWORD)
                .apply();
    }
}

