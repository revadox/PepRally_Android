package com.peprally.jeremy.peprally.services;

/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.text.Html;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.activities.LoginActivity;

import java.util.Map;
import java.util.Random;

public class PepRallyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // If the application is in the foreground handle both data and notification messages here.
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        Log.d(TAG, "Notification Message Body: " + remoteMessage.getData());
        sendNotification(remoteMessage.getData());
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param jsonData FCM json data received.
     */
    private void sendNotification(Map<String, String> jsonData) {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        String contentText;
        switch (Integer.parseInt(jsonData.get("notification_type"))) {
            case 0: // direct fistbump
                contentText = getResources().getString(R.string.notification_0_placeholder);
                break;
            case 1: // commented on post
                contentText = getResources().getString(R.string.notification_1_placeholder);
                String comment = jsonData.get("comment_text");
                if (comment.length() > 50)
                    comment = comment.substring(0, 50) + "...\"";
                else
                    comment = comment + "\"";
                contentText = contentText + comment;
                break;
            case 2: // fistbumped post
                contentText = getResources().getString(R.string.notification_2_placeholder);
                break;
            case 3: // fistbumped comment
                contentText = getResources().getString(R.string.notification_3_placeholder);
                break;
            default:
                //TODO: better error handling on invalid notification type
                contentText = "";
                break;
        }

        Drawable notificationDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.logo_push_ut, null);
        if (notificationDrawable != null) {
            Notification.Builder notificationBuilder = new Notification.Builder(this)
                    .setLargeIcon(((BitmapDrawable) notificationDrawable).getBitmap())
                    .setSmallIcon(R.drawable.logo_push)
                    .setContentTitle("PepRally")
                    .setContentText(jsonData.get("sender_nickname") + " " + contentText)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent);

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            Random random = new Random();
            int id = random.nextInt(9999 - 1000) + 1000;
            notificationManager.notify(id, notificationBuilder.build());
            Log.d(TAG, "NOTIFICATION RECEIVED");
        }
    }
}
