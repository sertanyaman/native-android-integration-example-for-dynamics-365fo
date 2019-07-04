// Modified and revised Microsoft code example for Notification Hub registration
package com.sertanyaman.dynamics365test.nothub;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import com.microsoft.identity.common.internal.util.StringUtil;
import com.microsoft.windowsazure.messaging.NotificationHub;
import com.sertanyaman.dynamics365test.activities.SettingsHelper;

public class RegistrationIntentService extends IntentService {

    private static final String TAG = "AZHub IntentService";
    private SettingsHelper settingsHelper = SettingsHelper.getHelper();
    private String fcmToken = null;
    private AzureHubsRegistrationHelper hubsHelper = null;

    private NotificationHub hub;


    public RegistrationIntentService() {
        super(TAG);
    }



    @Override
    public void onCreate() {
        super.onCreate();
        hubsHelper = AzureHubsRegistrationHelper.getHelper();
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String resultString = null;
        String regID = null;
        fcmToken = hubsHelper.getFcmToken();

        if(intent.getBooleanExtra(AzureHubsRegistrationHelper.FORCE_REGISTRATION, false))
        {
            sharedPreferences.edit().clear().commit();
        }

        try {
            // Storing the registration ID that indicates whether the generated token has been
            // sent to your server. If it is not stored, send the token to your server,
            // otherwise your server should have already received the token.
            if (((regID=sharedPreferences.getString("registrationID", null)) == null)){
                if(!StringUtil.isEmpty(fcmToken)) {
                    NotificationHub hub = new NotificationHub(settingsHelper.getHubname(),
                            settingsHelper.getHublistenconstring(), this);
                    Log.d(TAG, "Attempting a new registration with NH using FCM token : " + fcmToken);
                    regID = hub.register(fcmToken, settingsHelper.getWorker()).getRegistrationId();

                    // If you want to use tags...
                    // Refer to : https://azure.microsoft.com/documentation/articles/notification-hubs-routing-tag-expressions/
                    // regID = hub.register(token, "tag1,tag2").getRegistrationId();

                    resultString = "New NH Registration Successfully - RegId : " + regID;
                    Log.d(TAG, resultString);

                    SharedPreferences.Editor edit = sharedPreferences.edit();
                    edit.putString("registrationID", regID);
                    edit.putString("FCMtoken", fcmToken);
                    edit.commit();
                }
            }
            // Check if the token may have been compromised and needs refreshing.
            else if ((sharedPreferences.getString("FCMtoken", "")==null) || !(sharedPreferences.getString("FCMtoken", "")).equals(fcmToken)) {
                if(!StringUtil.isEmpty(fcmToken)) {
                    NotificationHub hub = new NotificationHub(settingsHelper.getHubname(),
                            settingsHelper.getHublistenconstring(), this);
                    Log.d(TAG, "NH Registration refreshing with token : " + fcmToken);
                    regID = hub.register(fcmToken, settingsHelper.getWorker()).getRegistrationId();

                    // If you want to use tags...
                    // Refer to : https://azure.microsoft.com/documentation/articles/notification-hubs-routing-tag-expressions/
                    // regID = hub.register(token, "tag1,tag2").getRegistrationId();

                    resultString = "New NH Registration Successfully - RegId : " + regID;
                    Log.d(TAG, resultString);

                    SharedPreferences.Editor edit = sharedPreferences.edit();
                    edit.putString("registrationID", regID);
                    edit.putString("FCMtoken", fcmToken);
                    edit.commit();
                }
            }

            else {
                resultString = "Previously Registered Successfully - RegId : " + regID;
            }
        } catch (Exception e) {
            Log.e(TAG, resultString="Failed to complete registration", e);
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
        }

        Log.d("NotificationHub", "Registration complete: "+ resultString);

    }
}