package com.ormediagroup.youngplus.lau;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.ormediagroup.youngplus.MainActivity;
import com.ormediagroup.youngplus.R;

import java.util.Map;

/**
 * Created by Lau on 2018/12/18.
 */

public class AlarmReceiver extends BroadcastReceiver {

    private String TAG = "ORM";

    @Override
    public void onReceive(Context context, Intent intent) {
        // 注意requestCode一定要不同，否则获取不到正确的bundle
        Log.i("ORM", "onReceive: " + intent.getStringExtra("about"));
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            if (bundle.getString("title") != null && bundle.getString("content") != null && bundle.getInt("notifyID", 0) > 0) {
                sendYesOrNoNotification(context, bundle.getString("title"), bundle.getString("content"), bundle.getInt("notifyID"));
            } else if (bundle.getInt("what") == 1) {
                Toast.makeText(context, "what=1 notifyID=" + bundle.getInt("notifyID"), Toast.LENGTH_SHORT).show();
                NotificationManager notificationManager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                if (notificationManager != null) {
                    notificationManager.cancel("yesorno", bundle.getInt("notifyID"));
                }
            } else if (bundle.getInt("what") == 2) {
                Toast.makeText(context, "what=2 notifyID=" + bundle.getInt("notifyID"), Toast.LENGTH_SHORT).show();
                NotificationManager notificationManager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                if (notificationManager != null) {
                    notificationManager.cancel("yesorno", bundle.getInt("notifyID"));
                }
            }
        }
        Intent i = new Intent(context, AlarmService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(i);
        } else {
            context.startService(i);
        }
    }

    // 获取PendingIntent
    private PendingIntent getPendingIntent(Context context, int what, int flags, int notifyID) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("notifyID", notifyID);
        intent.putExtra("what", what);
        Log.i(TAG, "getPendingIntent: notifyID = " + notifyID);
        return PendingIntent.getBroadcast(context, notifyID, intent, flags);
    }

    // 发送推送（action 是否）
    public void sendYesOrNoNotification(Context context, String contentTitle, String contentText, int notifyID) {
        final int NOTIFY_ID = notifyID; // ID of notification
        String id = "youngplus_1"; // default_channel_id
        String title = "youngplus"; // Default Channel
        NotificationCompat.Builder builder;
        NotificationManager notifManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Log.i(TAG, "sendYesOrNoNotification: notifyID = " + notifyID);
        NotificationCompat.Action action1 = new NotificationCompat.Action.Builder(0, "Yes", getPendingIntent(context, 1, 1, notifyID)).build();
        NotificationCompat.Action action2 = new NotificationCompat.Action.Builder(0, "No", getPendingIntent(context, 2, 2, notifyID)).build();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = null;
            if (notifManager != null) {
                mChannel = notifManager.getNotificationChannel(id);
                if (mChannel == null) {
                    mChannel = new NotificationChannel(id, title, importance);
                    mChannel.enableVibration(true);
                    mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                    notifManager.createNotificationChannel(mChannel);
                }
            }
            builder = new NotificationCompat.Builder(context, id);
            builder.setContentTitle(contentTitle)                            // required
                    .setSmallIcon(android.R.drawable.ic_popup_reminder)   // required
                    .setContentText(contentText) // required
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setAutoCancel(false)
                    .setOngoing(true)
//                    .setFullScreenIntent(getPendingIntent(context, 3, 3, notifyID), true)
                    .setTicker(contentTitle)
                    .addAction(action1)
                    .addAction(action2)
                    .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
            ;
        } else {
            builder = new NotificationCompat.Builder(context, id);
            builder.setContentTitle(contentTitle)                            // required
                    .setSmallIcon(android.R.drawable.ic_popup_reminder)   // required
                    .setContentText(contentText) // required
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setAutoCancel(false)
                    .setOngoing(true)
//                    .setFullScreenIntent(getPendingIntent(context, 3, 3, notifyID), true)
                    .setTicker(contentTitle)
                    .addAction(action1)
                    .addAction(action2)
                    .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
                    .setPriority(Notification.PRIORITY_HIGH)
            ;
        }
        Notification notification = builder.build();
        if (notifManager != null) {
            notifManager.notify("yesorno", NOTIFY_ID, notification);
        }
    }

}
