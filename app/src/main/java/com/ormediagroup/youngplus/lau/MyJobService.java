package com.ormediagroup.youngplus.lau;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.ormediagroup.youngplus.MainActivity;
import com.ormediagroup.youngplus.R;

import java.util.Map;

/**
 * Created by Lau on 2019/2/14.
 */

public class MyJobService extends JobService {

    @Override
    public boolean onStartJob(JobParameters job) {
//        sendNotification(getApplicationContext(),"test","test",null);
//        send(getApplicationContext(), "test", "test");
//        createNotification(getApplicationContext(), "Young+", "是否接受到通知？");
        Intent i = new Intent(getApplicationContext(), AlarmService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getApplicationContext().startForegroundService(i);
        } else {
            getApplicationContext().startService(i);
        }
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        return false;
    }

    private RemoteViews getRemoteViews(String title, String content) {
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.notification_test);
        remoteViews.setOnClickPendingIntent(R.id.notification_yes, getPendingIntent(1, 1, 1));
        remoteViews.setOnClickPendingIntent(R.id.notification_no, getPendingIntent(2, 2, 2));
        remoteViews.setTextViewText(R.id.notification_title, title);
        remoteViews.setTextViewText(R.id.notification_content, content);
        return remoteViews;
    }

    private PendingIntent getPendingIntent(int what, int rc, int flags) {
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("what", what);
        return PendingIntent.getBroadcast(this, rc, intent, flags);
    }

    public void createNotification(Context context, String contentTitle, String contentText) {
        final int NOTIFY_ID = 0; // ID of notification
        String id = context.getString(R.string.default_notification_channel_id); // default_channel_id
        String title = "youngplus"; // Default Channel
        NotificationCompat.Builder builder;
        NotificationManager notifManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Action action1 = new NotificationCompat.Action.Builder(0, "Yes", getPendingIntent(1, 1, 1)).build();
        NotificationCompat.Action action2 = new NotificationCompat.Action.Builder(0, "No", getPendingIntent(2, 2, 2)).build();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = notifManager.getNotificationChannel(id);
            if (mChannel == null) {
                mChannel = new NotificationChannel(id, title, importance);
                mChannel.enableVibration(true);
                mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                notifManager.createNotificationChannel(mChannel);
            }
            builder = new NotificationCompat.Builder(context, id);
            builder.setContentTitle(contentTitle)                            // required
                    .setSmallIcon(android.R.drawable.ic_popup_reminder)   // required
                    .setContentText(contentText) // required
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setAutoCancel(false)
                    .setOngoing(true)
                    .setFullScreenIntent(getPendingIntent(3, 3, 3), true)
                    .setTicker(contentTitle)
                    .addAction(action1)
                    .addAction(action2)
                    .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
        } else {
            builder = new NotificationCompat.Builder(context, id);
            builder.setContentTitle(contentTitle)                            // required
                    .setSmallIcon(android.R.drawable.ic_popup_reminder)   // required
                    .setContentText(contentText) // required
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setAutoCancel(false)
                    .setOngoing(true)
                    .setFullScreenIntent(getPendingIntent(3, 3, 3), true)
                    .setTicker(contentTitle)
                    .addAction(action1)
                    .addAction(action2)
                    .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
                    .setPriority(Notification.PRIORITY_HIGH);
        }
        Notification notification = builder.build();
        notifManager.notify("yesorno", NOTIFY_ID, notification);
    }


    private void send(Context context, String title, String content) {

        String channelId = context.getString(R.string.default_notification_channel_id);
        NotificationCompat.Action action1 = new NotificationCompat.Action.Builder(0, "Yes", getPendingIntent(1, 1, 1)).build();
        NotificationCompat.Action action2 = new NotificationCompat.Action.Builder(0, "No", getPendingIntent(2, 2, 2)).build();
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context, channelId)
                        .setSmallIcon(android.R.drawable.ic_popup_reminder)
                        .setContentTitle(title)
                        .setContentText(content)
                        .setAutoCancel(false)
                        .setOngoing(true)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                        .addAction(action1)
                        .addAction(action2)
                        .setDefaults(NotificationCompat.DEFAULT_ALL)
                        .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
                        .setPriority(Notification.PRIORITY_HIGH)
                        .setFullScreenIntent(getPendingIntent(3, 3, 3), true);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            createNotificationChannel(channelId, notificationManager);
            notificationManager.notify("yesorno", 0, notificationBuilder.build());
        }
    }

    private void createNotificationChannel(String channelId, NotificationManager notificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "YoungPlus";
            String description = "YoungPlus";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            channel.setDescription(description);
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setShowBadge(true);
            channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            channel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), null);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }


    private void sendNotification(Context context, String title, String messageBody, Map<String, String> extra) {
        Intent intent = new Intent(context, MainActivity.class);
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

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String channelId = context.getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context, channelId)
                        .setSmallIcon(R.mipmap.ic_youngplus)
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (notificationManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(channelId,
                        "Channel human readable title",
                        NotificationManager.IMPORTANCE_DEFAULT);
                notificationManager.createNotificationChannel(channel);
            }
            notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
        }
    }

}
