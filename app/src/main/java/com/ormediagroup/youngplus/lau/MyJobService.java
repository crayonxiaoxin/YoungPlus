package com.ormediagroup.youngplus.lau;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

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
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        return false;
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
