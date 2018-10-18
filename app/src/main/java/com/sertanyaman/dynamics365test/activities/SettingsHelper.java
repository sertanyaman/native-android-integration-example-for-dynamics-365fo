package com.sertanyaman.dynamics365test.activities;

import android.content.SharedPreferences;

public class SettingsHelper implements SharedPreferences.OnSharedPreferenceChangeListener{
    private static SettingsHelper helper;
    private static final String KEY_CLIENT= "client";
    private static final String KEY_AXURL = "axurl";
    private static final String KEY_WORKER = "worker";

    private String client, axUrl, worker;

    //Settings change event
    public interface OnSettingChange
    {
        void settingChanged(String s);
    }
    private OnSettingChange onSettingChange;
    public void setOnSettingChange(OnSettingChange onSettingChange) {
        this.onSettingChange = onSettingChange;
    }

    //Singleton
    private SettingsHelper()
    {
    }

    public static synchronized SettingsHelper getHelper()
    {
        if(helper==null)
        {
            helper = new SettingsHelper();
        }

        return helper;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public String getAxUrl() {
        return axUrl;
    }

    public void setAxUrl(String axUrl) {
        this.axUrl = axUrl;
    }

    public String getWorker() {
        return worker;
    }

    public void setWorker(String worker) {
        this.worker = worker;
    }

    public void writeSettings(SharedPreferences  sharedPreferences)
    {
        SharedPreferences.Editor edit = sharedPreferences.edit();

        edit.putString(SettingsHelper.KEY_CLIENT,client);
        edit.putString(SettingsHelper.KEY_AXURL,axUrl);
        edit.putString(SettingsHelper.KEY_WORKER,worker);

        edit.apply();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {

        //Load into settings helper
        switch(s) {
            case SettingsHelper.KEY_CLIENT:
            case SettingsHelper.KEY_AXURL:
            case SettingsHelper.KEY_WORKER:
                this.loadFromSharedPreferences(sharedPreferences);
                break;
        }

        onSettingChange.settingChanged(s);
    }


    public void loadFromSharedPreferences(SharedPreferences sharedPreferences)
    {
        client = sharedPreferences.getString(SettingsHelper.KEY_CLIENT, " ");
        axUrl = sharedPreferences.getString(SettingsHelper.KEY_AXURL, " ");
        worker = sharedPreferences.getString(SettingsHelper.KEY_WORKER, " ");
      }
}
