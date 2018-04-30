package com.mathiascraeghs.foodtracker;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v7.preference.PreferenceFragmentCompat;

/**
 * Created by mathi on 28/03/2018.
 */

public class SettingsFragment extends PreferenceFragmentCompat {

    /**
     * Creates the preference fragment.
     * @param savedInstanceState The instance state that is saved.
     * @param rootKey The root key.
     */
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_visualizer);
    }
}

