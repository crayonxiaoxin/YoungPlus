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
        Bundle bundle = intent.getExtras();
        Log.i(TAG, "onReceive: action = "+intent.getAction());
        if (bundle != null) {
            Log.i(TAG, "onReceive: bundle = " + bundle.toString());
            String action = intent.getAction();
            if (action != null) {
                if (action.equals("com.ormediagroup.youngplus.action.alertsystem")) {
                    sendNotification(context, bundle.getString("title"), bundle.getString("content"), bundle.getInt("notifyID"), null);
                } else if (action.equals("com.ormediagroup.youngplus.action.alerttoast")) {
                    String alert_title = intent.getStringExtra("title");
                    String alert_content = intent.getStringExtra("content");
                    if (alert_title != null && alert_content != null) {
                        Toast.makeText(context, alert_title + "\n" + alert_content, Toast.LENGTH_SHORT).show();
                    }
                }
//                else if (action.equals("com.ormediagroup.youngplus.action.link")) {
//                    String link = bundle.getString("link");
//                    Log.i(TAG, "onReceive: bundle link = "+link);
//                    if (link!=null){
//                        Uri uri = Uri.parse(LauUtil.getLegalURL(link));
//                        context.startActivity(new Intent(Intent.ACTION_VIEW, uri));
//                    }
//                }
            }

//            if (bundle.getString("title") != null && bundle.getString("content") != null && bundle.getInt("notifyID", 0) > 0 && bundle.getInt("scheduleId", 0) > 0) {
//                sendYesOrNoNotification(context, bundle.getString("title"), bundle.getString("content"), bundle.getInt("notifyID"), bundle.getInt("scheduleId"));
//            } else if (bundle.getInt("what") == 1) {
//                NotificationManager notificationManager =
//                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//                if (notificationManager != null) {
//                    notificationManager.cancel("yesorno", bundle.getInt("notifyID"));
//                    // submit
//                    int sid = bundle.getInt("scheduleId", 0);
////                    Toast.makeText(context, "what=1 notifyID=" + bundle.getInt("notifyID") + " scheduleId = " + sid, Toast.LENGTH_SHORT).show();
//                    if (sid > 0) {
//                        new JSONResponse(context, API.API_GET_SCHEDULE, "sid=" + sid + "&value=Y&action=save", new JSONResponse.onComplete() {
//                            @Override
//                            public void onComplete(JSONObject json) {
//                                try {
//                                    if (json.getInt("rc") == 0) {
//                                        Toast.makeText(context, "提交成功", Toast.LENGTH_SHORT).show();
//                                    }
//                                } catch (JSONException e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                        });
//                    }
//
//                }
//            } else if (bundle.getInt("what") == 2) {
//                NotificationManager notificationManager =
//                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//                if (notificationManager != null) {
//                    notificationManager.cancel("yesorno", bundle.getInt("notifyID"));
//                    // submit
//                    int sid = bundle.getInt("scheduleId", 0);
////                    Toast.makeText(context, "what=1 notifyID=" + bundle.getInt("notifyID") + " scheduleId = " + sid, Toast.LENGTH_SHORT).show();
//                    if (sid > 0) {
//                        new JSONResponse(context, API.API_GET_SCHEDULE, "sid=" + sid + "&value=N&action=save", new JSONResponse.onComplete() {
//                            @Override
//                            public void onComplete(JSONObject json) {
//                                try {
//                                    if (json.getInt("rc") == 0) {
//                                        Toast.makeText(context, "提交成功", Toast.LENGTH_SHORT).show();
//                                    }
//                                } catch (JSONException e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                        });
//                    }
//                }
//            }
        }
//        Intent i = new Intent(context, AlarmService.class);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            context.startForegroundService(i);
//        } else {
//            context.startService(i);
//        }
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

    private void sendYesOrNoNotification(Context context, String contentTitle, String contentText, int notifyID, int scheduleId) {
        String channelId = context.getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Action action1 = new NotificationCompat.Action.Builder(0, "Yes", getPendingIntentForAlertSystem(context, 1, 1, notifyID, scheduleId)).build();
        NotificationCompat.Action action2 = new NotificationCompat.Action.Builder(0, "No", getPendingIntentForAlertSystem(context, 2, 2, notifyID, scheduleId)).build();
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context, channelId)
                        .setSmallIcon(R.drawable.young_icon_192x192)
                        .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.young_icon_192x192))
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

    // 获取PendingIntent
    private PendingIntent getPendingIntentForAlertSystem2(Context context, String title, String content, int flags, int notifyID) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        intent.putExtra("type","alert");
        intent.setAction("com.ormediagroup.youngplus.action.alerttoast");
//        intent.putExtra("notifyID", notifyID);
        intent.putExtra("title", title);
        intent.putExtra("content", content);
        Log.i(TAG, "getPendingIntent: notifyID = " + notifyID);
        return PendingIntent.getBroadcast(context, notifyID, intent, flags);
    }

    private void sendNotification(Context context, String title, String messageBody, int notifyId, Map<String, String> extra) {

        PendingIntent pendingIntent = getPendingIntentForAlertSystem2(context, title, messageBody, 0, notifyId);

        String channelId = context.getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.setBigContentTitle(title).bigText(messageBody);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context, channelId)
                        .setSmallIcon(R.drawable.young_icon_192x192)
                        .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.young_icon_192x192))
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setStyle(bigTextStyle)
//                        .setFullScreenIntent(pendingIntent,false)
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
            notificationManager.notify(notifyId /* ID of notification */, notificationBuilder.build());
//            startForeground(1,notificationBuilder.build());
        }
    }

}
