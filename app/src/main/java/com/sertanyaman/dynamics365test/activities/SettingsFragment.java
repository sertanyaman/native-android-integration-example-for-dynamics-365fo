package com.sertanyaman.dynamics365test.activities;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.sertanyaman.dynamics365test.R;


public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.app_settings, rootKey);

        SettingsHelper.getHelper();
    }
}
