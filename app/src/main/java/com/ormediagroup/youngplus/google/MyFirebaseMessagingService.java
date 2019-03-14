package com.ormediagroup.youngplus.google;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.ormediagroup.youngplus.MainActivity;
import com.ormediagroup.youngplus.R;
import com.ormediagroup.youngplus.lau.LauUtil;

import java.util.Map;

/**
 * Created by Lau on 2018/12/5.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.i(TAG, "onNewToken: " + s);
    }

    public void onMessageReceived(RemoteMessage remoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        // Log.d(TAG, "From: " + remoteMessage.getFrom());
        // Log.e("ORM","From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.e("ORM", "Message data payload: " + remoteMessage.getData());
        }
        PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Map<String, String> data = null;
            if (!remoteMessage.getData().get("type").isEmpty()) {
                data = remoteMessage.getData();
            }
            sendNotification(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody(), data);
        }


        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    /**
     * Send notification to foreground & background
     * handle foreground payload here or send extra to MainActivity(handle foreground & background payload together)
     *
     * @param title
     * @param messageBody
     * @param extra
     */
    private void sendNotification(String title, String messageBody, Map<String, String> extra) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (extra != null) {
            String type = extra.get("type") != null ? extra.get("type") : "";
            if (type.equals("about")) {
                // for background, foreground can get this message directly without intent.putExtra()
                intent.putExtra("type", type);
            } else if (type.equals("link")) {
                // only for foreground, without sending extra
                Uri uri = Uri.parse(LauUtil.getLegalURL(extra.get("link")));
                intent = new Intent(Intent.ACTION_VIEW, uri);
            }
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String channelId = getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.mipmap.ic_youngplus)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_youngplus))
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
//                        .setFullScreenIntent(pendingIntent,false)
                        .setContentIntent(pendingIntent)
                ;

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (notificationManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(channelId,
                        "Channel human readable title",
                        NotificationManager.IMPORTANCE_DEFAULT);
                notificationManager.createNotificationChannel(channel);
            }
            notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
//            startForeground(1,notificationBuilder.build());
        }
    }


}
