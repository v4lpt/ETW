package v4lpt.vpt.f036.esw;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingsManager {
    private static final String PREF_NAME = "AppSettings";
    private static final String KEY_DISPLAY_MODE = "DisplayMode";

    private SharedPreferences sharedPreferences;

    public SettingsManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void setDisplayMode(boolean isModeA) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_DISPLAY_MODE, isModeA);
        editor.apply();
    }

    public boolean isDisplayModeA() {
        return sharedPreferences.getBoolean(KEY_DISPLAY_MODE, true); // Default to Mode A
    }
}