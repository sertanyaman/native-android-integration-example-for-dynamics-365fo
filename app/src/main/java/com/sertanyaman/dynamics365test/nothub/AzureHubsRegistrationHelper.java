package com.sertanyaman.dynamics365test.nothub;

import android.app.Activity;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

public class AzureHubsRegistrationHelper {
    private static AzureHubsRegistrationHelper helper;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private String fcmToken;
    private static final String TAG = "Azure NHUB";
    public static final String FORCE_REGISTRATION = "force";

    //Singleton
    private AzureHubsRegistrationHelper()
    {
    }

    public static synchronized AzureHubsRegistrationHelper getHelper()
    {
        if(helper==null)
        {
            helper = new AzureHubsRegistrationHelper();
            helper.init();
        }

        return helper;
    }

    private void init()
    {
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                fcmToken = instanceIdResult.getToken();
                Log.d(TAG, "FCM Registration Token received: " + fcmToken);
                if(onNewFirebaseTokenReceived!=null)
                {
                    onNewFirebaseTokenReceived.tokenReceived(fcmToken);
                }
            }
        });
    }

    public String getFcmToken() {
        return fcmToken;
    }

    //Settings change event
    public interface OnNewFirebaseTokenReceived
    {
        void tokenReceived(String token);
    }
    private OnNewFirebaseTokenReceived onNewFirebaseTokenReceived;

    public void setOnNewFirebaseTokenReceived(OnNewFirebaseTokenReceived onNewFirebaseTokenReceived) {
        this.onNewFirebaseTokenReceived = onNewFirebaseTokenReceived;
    }

    private void snackIt(Activity activity, String snack) {
        View view = activity.getCurrentFocus();
        if(view!=null) {
            Snackbar.make(activity.getCurrentFocus(), snack, Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices(Activity activity) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(activity);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(activity, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i("Azure NHubs", "This device is not supported by Google Play Services.");
                snackIt(activity,"This device is not supported by Google Play Services.");
            }
            return false;
        }
        return true;
    }


    private void registerWithNotificationHubs(Activity activity, Intent intent)
    {
        if (checkPlayServices(activity)) {
            activity.startService(intent);
        }
    }

    public void registerWithNotificationHubs(Activity activity, boolean force)
    {
        Intent intent = new Intent(activity, RegistrationIntentService.class);
        intent.putExtra(AzureHubsRegistrationHelper.FORCE_REGISTRATION, force);
        registerWithNotificationHubs(activity, intent);
    }

    public void registerWithNotificationHubs(Activity activity)
    {
        Intent intent = new Intent(activity, RegistrationIntentService.class);
        registerWithNotificationHubs(activity, intent);
    }
}
