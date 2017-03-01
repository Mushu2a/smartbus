package com.m1.lesbuteurs.smartbus.helper;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.*;

import com.m1.lesbuteurs.smartbus.LoginActivity;

import java.util.HashMap;

public class SessionManager {

    private SharedPreferences preferences;
    private Editor editor;
    Context _context;

    private static final String PREF_NAME = "AndroidSmartBusLogin";
    private static final String IS_LOGGEDIN = "isLoggedIn";

    public static final String KEY_USERNAME = "username";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PASSWORD = "password";

    public SessionManager(Context context) {
        this._context = context;
        preferences = context.getSharedPreferences(PREF_NAME, 0);
        editor = preferences.edit();
    }

    public void setLogin(boolean isLoggedIn) {
        editor.putBoolean(IS_LOGGEDIN, isLoggedIn);
        editor.commit();
    }

    public boolean isLoggedIn(){
        return preferences.getBoolean(IS_LOGGEDIN, false);
    }

    /**
     * Création session
     * */
    public void createLoginSession(String name, String email, String password) {
        editor.putBoolean(IS_LOGGEDIN, true);

        editor.putString(KEY_USERNAME, name);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_PASSWORD, password);

        editor.commit();
    }

    /**
     * Enregistre les infos en session
     * */
    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<String, String>();

        user.put(KEY_USERNAME, preferences.getString(KEY_USERNAME, null));
        user.put(KEY_EMAIL, preferences.getString(KEY_EMAIL, null));
        user.put(KEY_PASSWORD, preferences.getString(KEY_PASSWORD, null));
        return user;
    }

    /**
     * Efface les sessions
     * */
    public void logoutUser() {
        editor.clear();
        editor.commit();

        Intent i = new Intent(_context, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        _context.startActivity(i);
    }
}
