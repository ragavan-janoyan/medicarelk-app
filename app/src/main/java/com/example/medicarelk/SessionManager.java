package com.example.medicarelk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

/** Remembers which user is logged in (only the user id is stored, never the password). */
public class SessionManager {

    private static final String PREFS = "MediCareLKSession";
    private static final String KEY_USER_ID = "userId";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void login(long userId) {
        prefs.edit().putLong(KEY_USER_ID, userId).apply();
    }

    public long getUserId() {
        return prefs.getLong(KEY_USER_ID, -1);
    }

    public boolean isLoggedIn() {
        return getUserId() != -1;
    }

    public void logout() {
        prefs.edit().remove(KEY_USER_ID).apply();
    }

    /**
     * Call at the top of onCreate() of every protected page.
     * If nobody is logged in, sends the user to the login screen and returns false.
     */
    public static boolean requireLogin(Activity activity) {
        if (new SessionManager(activity).isLoggedIn()) return true;

        Intent intent = new Intent(activity, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
        return false;
    }
}
