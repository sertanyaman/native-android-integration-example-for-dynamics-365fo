package com.sertanyaman.dynamics365test.comm;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class FirebaseNotificationService extends FirebaseMessagingService {
    public FirebaseNotificationService() {
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        RemoteMessage.Notification not = remoteMessage.getNotification();

        if(not!=null) {
            if(not.getTitle()!=null) {
                Log.d("FCM Message Received", "Title: " + remoteMessage.getNotification().getTitle());
            }
            if(not.getBody()!=null) {
                Log.d("FCM Message Received", "Body: " + remoteMessage.getNotification().getBody());
            }
        }

        Map<String,String> data = remoteMessage.getData();

        if(data!=null)
        {
            if(data.get("message")!=null)
            {
                Log.d("FCM Message Received", "Message: " + data.get("message").toString());

            }
        }

        super.onMessageReceived(remoteMessage);
    }
}
