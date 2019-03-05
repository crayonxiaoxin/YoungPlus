package com.ormediagroup.youngplus.lau;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.ormediagroup.youngplus.MainActivity;
import com.ormediagroup.youngplus.R;
import com.ormediagroup.youngplus.network.JSONResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by Lau on 2018/12/18.
 */

public class AlarmReceiver extends BroadcastReceiver {

    private String TAG = "ORM";

    @Override
    public void onReceive(final Context context, Intent intent) {
        // 注意requestCode一定要不同，否则获取不到正确的bundle
        Log.i("ORM", "onReceive: " + intent.getStringExtra("about"));
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            if (bundle.getString("title") != null && bundle.getString("content") != null && bundle.getInt("notifyID", 0) > 0 && bundle.getInt("scheduleId", 0) > 0) {
//                sendNotificationForAlertSystem(context, bundle.getString("title"), bundle.getString("content"), bundle.getInt("notifyID"), bundle.getInt("scheduleId"));
                sendNotification(context, bundle.getString("title"), bundle.getString("content"), bundle.getInt("notifyID"), bundle.getInt("scheduleId"));
            } else if (bundle.getInt("what") == 1) {
                NotificationManager notificationManager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                if (notificationManager != null) {
                    notificationManager.cancel("yesorno", bundle.getInt("notifyID"));
                    // submit
                    int sid = bundle.getInt("scheduleId", 0);
//                    Toast.makeText(context, "what=1 notifyID=" + bundle.getInt("notifyID") + " scheduleId = " + sid, Toast.LENGTH_SHORT).show();
                    if (sid > 0) {
                        new JSONResponse(context, API.API_GET_SCHEDULE, "sid=" + sid + "&value=Y&action=save", new JSONResponse.onComplete() {
                            @Override
                            public void onComplete(JSONObject json) {
                                try {
                                    if (json.getInt("rc") == 0) {
                                        Toast.makeText(context, "提交成功", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }

                }
            } else if (bundle.getInt("what") == 2) {
                NotificationManager notificationManager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                if (notificationManager != null) {
                    notificationManager.cancel("yesorno", bundle.getInt("notifyID"));
                    // submit
                    int sid = bundle.getInt("scheduleId", 0);
//                    Toast.makeText(context, "what=1 notifyID=" + bundle.getInt("notifyID") + " scheduleId = " + sid, Toast.LENGTH_SHORT).show();
                    if (sid > 0) {
                        new JSONResponse(context, API.API_GET_SCHEDULE, "sid=" + sid + "&value=N&action=save", new JSONResponse.onComplete() {
                            @Override
                            public void onComplete(JSONObject json) {
                                try {
                                    if (json.getInt("rc") == 0) {
                                        Toast.makeText(context, "提交成功", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
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
    private PendingIntent getPendingIntentForAlertSystem(Context context, int what, int flags, int notifyID, int scheduleId) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("notifyID", notifyID);
        intent.putExtra("what", what);
        intent.putExtra("scheduleId", scheduleId);
        Log.i(TAG, "getPendingIntent: notifyID = " + notifyID);
        return PendingIntent.getBroadcast(context, notifyID, intent, flags);
    }

    // 发送推送（action 是否）
    public void sendNotificationForAlertSystem(Context context, String contentTitle, String contentText, int notifyID, int scheduleId) {
        String channelId = "youngplus_1"; // default_channel_id
        String channelTitle = "youngplus"; // Default Channel
        NotificationCompat.Builder builder;
        NotificationManager notifManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Log.i(TAG, "sendYesOrNoNotification: notifyID = " + notifyID);
        NotificationCompat.Action action1 = new NotificationCompat.Action.Builder(0, "Yes", getPendingIntentForAlertSystem(context, 1, 1, notifyID, scheduleId)).build();
        NotificationCompat.Action action2 = new NotificationCompat.Action.Builder(0, "No", getPendingIntentForAlertSystem(context, 2, 2, notifyID, scheduleId)).build();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = null;
            if (notifManager != null) {
                mChannel = notifManager.getNotificationChannel(channelId);
                if (mChannel == null) {
                    mChannel = new NotificationChannel(channelId, channelTitle, importance);
                    mChannel.enableVibration(true);
                    mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                    notifManager.createNotificationChannel(mChannel);
                }
            }
        }
        builder = new NotificationCompat.Builder(context, channelId);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        builder.setContentTitle(contentTitle)
                .setSmallIcon(R.mipmap.ic_youngplus)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_youngplus))
                .setContentText(contentText)
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(false)
//                    .setOngoing(true)
                .setFullScreenIntent(getPendingIntentForAlertSystem(context, 3, 3, notifyID, scheduleId), true)
                .setTicker(contentTitle)
                .setSound(defaultSoundUri)
                .addAction(action1)
                .addAction(action2)
                .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
                .setPriority(Notification.PRIORITY_HIGH)
        ;
        Notification notification = builder.build();
        if (notifManager != null) {
            notifManager.notify("yesorno", notifyID, notification);
        }
    }

    private void sendNotification(Context context, String contentTitle, String contentText, int notifyID, int scheduleId) {
        String channelId = context.getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Action action1 = new NotificationCompat.Action.Builder(0, "Yes", getPendingIntentForAlertSystem(context, 1, 1, notifyID, scheduleId)).build();
        NotificationCompat.Action action2 = new NotificationCompat.Action.Builder(0, "No", getPendingIntentForAlertSystem(context, 2, 2, notifyID, scheduleId)).build();
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context, channelId)
                        .setSmallIcon(R.mipmap.ic_youngplus)
                        .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_youngplus))
                        .setContentTitle(contentTitle)
                        .setContentText(contentText)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .addAction(action1)
                        .addAction(action2)
                        .setTicker(contentTitle)
                        .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
                        .setPriority(Notification.PRIORITY_HIGH);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (notificationManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                String channelTitle = "youngplus";
                NotificationChannel mChannel = notificationManager.getNotificationChannel(channelId);
                if (mChannel == null) {
                    int importance = NotificationManager.IMPORTANCE_HIGH;
                    mChannel = new NotificationChannel(channelId, channelTitle, importance);
                    mChannel.enableVibration(true);
                    mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                    notificationManager.createNotificationChannel(mChannel);
                }
            }
            notificationManager.notify("yesorno", notifyID, notificationBuilder.build());
        }
    }

}
