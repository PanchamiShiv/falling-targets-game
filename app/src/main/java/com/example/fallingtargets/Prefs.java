package com.example.fallingtargets;

import android.content.Context;
import android.content.SharedPreferences;

public class Prefs {
    private static final String FILE = "fallingtargets_prefs";
    private static final String KEY_HIGH = "high_score";
    private static final String KEY_SENS = "tilt_sensitivity";
    private static final String KEY_HAPTIC = "haptics";
    private static final String KEY_MUTE = "mute_all";

    // ---- High score ----
    public static int getHigh(Context c) {
        return c.getSharedPreferences(FILE, Context.MODE_PRIVATE).getInt(KEY_HIGH, 0);
    }

    public static void saveHighScore(Context c, int value) {
        SharedPreferences sp = c.getSharedPreferences(FILE, Context.MODE_PRIVATE);
        sp.edit().putInt(KEY_HIGH, value).apply();
    }

    /** Save only if 'candidate' is greater. Returns the new/current high. */
    public static int updateHighIfGreater(Context c, int candidate) {
        int current = getHigh(c);
        if (candidate > current) {
            saveHighScore(c, candidate);
            return candidate;
        }
        return current;
    }

    // ---- Sensitivity ----
    public static float getSensitivity(Context c) {
        return c.getSharedPreferences(FILE, Context.MODE_PRIVATE).getFloat(KEY_SENS, 18f);
    }
    public static void setSensitivity(Context c, float v) {
        c.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().putFloat(KEY_SENS, v).apply();
    }

    // ---- Haptics ----
    public static boolean getHaptics(Context c) {
        return c.getSharedPreferences(FILE, Context.MODE_PRIVATE).getBoolean(KEY_HAPTIC, true);
    }
    public static void setHaptics(Context c, boolean on) {
        c.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().putBoolean(KEY_HAPTIC, on).apply();
    }

    // ---- Mute ----
    public static boolean isMuted(Context c) {
        return c.getSharedPreferences(FILE, Context.MODE_PRIVATE).getBoolean(KEY_MUTE, false);
    }
    public static void setMuted(Context c, boolean mute) {
        c.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().putBoolean(KEY_MUTE, mute).apply();
    }
}
