package com.sertanyaman.dynamics365test.comm;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.microsoft.aad.adal.AuthenticationCallback;
import com.microsoft.aad.adal.AuthenticationContext;
import com.microsoft.aad.adal.AuthenticationParameters;
import com.microsoft.aad.adal.AuthenticationResult;
import com.microsoft.aad.adal.PromptBehavior;
import com.sertanyaman.dynamics365test.activities.SettingsHelper;

import java.net.MalformedURLException;
import java.net.URL;

public class AzureAuthenticationHelper {
    private static AzureAuthenticationHelper helperInstance;
    private SettingsHelper settings;

    public AuthenticationContext getAadContext() {
        return aadContext;
    }

    private AuthenticationContext aadContext;

    private static final String AAD_END_POINT = "https://login.microsoftonline.com/common/oauth2/authorize";
    private static final String AAD_DEF_REDIRURL = "https://login.microsoftonline.com/common/oauth2/nativeclient";
    private String clientId;
    private String axUrl;

    private String curToken, refreshToken;
    private long expiresOn;

    //Events and interfaces
    public interface OnAuthorizationResult
    {
        void onAuthorizationSuccess(String token);
        void onAuthorizationFail();
    }

    private OnAuthorizationResult onAuthorizationResult;

    public void setOnAuthorizationResult(OnAuthorizationResult onAuthorizationResult) {
        this.onAuthorizationResult = onAuthorizationResult;
    }

    private boolean isConnected(Activity activity) {
        boolean retVal = false;

        ConnectivityManager manager = (ConnectivityManager)
                activity.getSystemService(Context.CONNECTIVITY_SERVICE);

        if(manager!=null) {
            NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
            retVal =  activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }

        return retVal;
    }

    private void snackIt(Activity activity, String snack) {
        View view = activity.getCurrentFocus();
        if(view!=null) {
            Snackbar.make(activity.getCurrentFocus(), snack, Snackbar.LENGTH_LONG).show();
        }
    }

    private void getAccessToken(Activity activity) throws NullPointerException, MalformedURLException {
        if(isConnected(activity)) {
            final Context context = activity.getApplicationContext();
            curToken = "";

            //Create context
            if (aadContext == null) {
                aadContext = new AuthenticationContext(context, AAD_END_POINT, false);
            }

            try
            {
                aadContext.acquireToken(activity , axUrl , clientId, AAD_DEF_REDIRURL, PromptBehavior.Auto, authCallback);
            }
            catch (Exception exceptione) {
                Log.e("Azure AAD :", "Authorization failed!");
                exceptione.printStackTrace();
                onAuthorizationResult.onAuthorizationFail();
            }
        }
        else
        {
            snackIt(activity, "Not connected to internet");
            onAuthorizationResult.onAuthorizationFail();
        }
    }

    private AuthenticationCallback<AuthenticationResult> authCallback = new AuthenticationCallback<AuthenticationResult>() {
        @Override
        public void onSuccess(AuthenticationResult result) {
            if(result==null || TextUtils.isEmpty(result.getAccessToken())
                    || result.getStatus()!= AuthenticationResult.AuthenticationStatus.Succeeded){
                Log.e("Azure AAD :", "Authentication Result is invalid");
                onAuthorizationResult.onAuthorizationFail();
            }
            else {
                curToken = result.getAccessToken();
                refreshToken = result.getRefreshToken();
                expiresOn = result.getExpiresOn().getTime();
                Log.d("Azure AAD :", "Authorization success!");
                Log.d("Azure AAD :", "Acquired token : "+curToken);
                onAuthorizationResult.onAuthorizationSuccess(curToken);
            }

        }

        @Override
        public void onError(Exception exc) {
            Log.e("Azure AAD :", "Authorization failed!");
            onAuthorizationResult.onAuthorizationFail();
            exc.printStackTrace();
        }
    };

    public void getTokenFromServer(Activity activity)
    {
        try {
            this.getAccessToken(activity);
        } catch (MalformedURLException e) {
            Log.d("Azure AAD :", "Invalid URL in parameters!");
            e.printStackTrace();
        }
    }

    //Singleton
    public static synchronized AzureAuthenticationHelper getInstance() {
        if(helperInstance==null)
        {
            helperInstance = new AzureAuthenticationHelper();
        }

        return helperInstance;
    }

    private AzureAuthenticationHelper() {
        settings = SettingsHelper.getHelper();
        initializeFromSettings();
    }

    public void initializeFromSettings()
    {
        axUrl = settings.getAxUrl().replaceAll("/$", "");;
        clientId = settings.getClient();
    }

    public String getCurToken() {
        return curToken;
    }

    public void signOut() {
        aadContext.getCache().removeAll();
    }


}
