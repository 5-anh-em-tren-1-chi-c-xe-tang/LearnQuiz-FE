package com.example.learnquiz_fe.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.learnquiz_fe.data.model.auth.AuthResponse;
import com.google.gson.Gson;

public class AuthManager {
    private static final String PREFS_NAME = "user_session";
    private static final String KEY_USER = "auth_user";

    private final SharedPreferences prefs;
    private final Gson gson = new Gson();

    public AuthManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveUser(AuthResponse user) {
        prefs.edit()
                .putString(KEY_USER, gson.toJson(user))
                .apply();
    }

    public AuthResponse getUser() {
        String json = prefs.getString(KEY_USER, null);
        return json != null ? gson.fromJson(json, AuthResponse.class) : null;
    }

    public void clearUser() {
        prefs.edit().remove(KEY_USER).apply();
    }

    public boolean isLoggedIn() {
        return getUser() != null;
    }
}

