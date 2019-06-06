package com.ormediagroup.youngplus.lau;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.ormediagroup.youngplus.LoginActivity;
import com.ormediagroup.youngplus.MainActivity;
import com.ormediagroup.youngplus.R;
import com.ormediagroup.youngplus.network.JSONResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by Lau on 2018/12/18.
 */

public class AlarmReceiver extends BroadcastReceiver {

    private String TAG = "ORM";

    @Override
    public void onReceive(final Context context, Intent intent) {
        // 注意requestCode一定要不同，否则获取不到正确的bundle
        Bundle bundle = intent.getExtras();
        Log.i(TAG, "onReceive: action = " + intent.getAction());
        if (bundle != null) {
            Log.i(TAG, "onReceive: bundle = " + bundle.toString());
            String action = intent.getAction();
            if (action != null) {
                if (action.equals(API.ACTION_ALARM_ALERT)) {
                    sendNotification(context, bundle.getString("title"), bundle.getString("content"), bundle.getInt("notifyID"), null);
                } else if (action.equals(API.ACTION_NUTRITION)) {
                    sendAndSetNext(context, bundle, API.API_NUTRITION);
                } else if (action.equals(API.ACTION_CLIENT_ALERT)) {
                    sendAndSetNext(context, bundle, API.ACTION_CLIENT_ALERT);
                } else if (action.equals(API.ACTION_ALARM_TOAST)) {
                    String alert_title = intent.getStringExtra("title");
                    String alert_content = intent.getStringExtra("content");
                    if (alert_title != null && alert_content != null) {
                        Toast.makeText(context, alert_title + "\n" + alert_content, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            String intentType = bundle.getString("type", "");
            if (intentType.equals("nutrition")) {
                handleYesOrNoClickAction(context, bundle, intentType);
            } else if (intentType.equals("client_alert")) {
                handleYesOrNoClickAction(context, bundle, intentType);
            }
        }
    }

    private void sendAndSetNext(Context context, Bundle bundle, String action) {
        String title = bundle.getString("title", "");
        String content = bundle.getString("content", "");
        String time = bundle.getString("time", "");
        int index = bundle.getInt("index", -1);
        int notifyID = bundle.getInt("notifyID", -1);
        Log.i(TAG, "onReceive: index = " + index);
        sendYesOrNoNotification(context, title, content, time, "nutrition", notifyID, index);
        if (!time.equals("")) {
            long delta = 24 * 60 * 60 * 1000;
            Log.i(TAG, "onReceive: deltaTime = " + delta);
            Date tomorrow = new Date();
            tomorrow.setTime(new Date().getTime() + delta);
            String today = getRealFormat("yyyy-MM-dd").format(tomorrow);
            String requestCode = today.replace("-", "") + index;
            int resC = Integer.parseInt(requestCode);
            sendMsgFor(context, action, title, content, time, delta, resC, index);
        }
    }

    private void handleYesOrNoClickAction(Context context, Bundle bundle, String intentType) {
        int what = bundle.getInt("what", -1);
        String time = bundle.getString("time", "");
        int index = bundle.getInt("scheduleId");
        Log.i(TAG, "onReceive: type = " + intentType);
        Log.i(TAG, "onReceive: what = " + what);
        Log.i(TAG, "onReceive: time = " + time);
        Log.i(TAG, "onReceive: index = " + index);
        String today = getRealFormat("yyyy-MM-dd").format(new Date());
        String uid = new User(context).getUserId();
        if (what == 1) {
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.cancel("yesorno", bundle.getInt("notifyID"));
//                Toast.makeText(context, "uid=" + uid + " what=1 notifyID=" + bundle.getInt("notifyID") + " date=" + today + " time=" + time, Toast.LENGTH_SHORT).show();
                submitYESorNOFor(context, intentType, uid, today, time, index, what, "", "");
            }
        } else if (what == 2) {
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.cancel("yesorno", bundle.getInt("notifyID"));
//                Toast.makeText(context, "what=2 notifyID=" + bundle.getInt("notifyID") + " date=" + today + " time=" + time, Toast.LENGTH_SHORT).show();
                submitYESorNOFor(context, intentType, uid, today, time, index, what, "", "");
            }
        }
    }

    private void submitYESorNOFor(Context context, String intentType, String uid, String today, String time, int index, int what, String title, String content) {
        if (new User(context).isUserLoggedIn()) {
            Map<String, String> params = new HashMap<>();
            params.put("action", "insert");
            params.put("type", intentType);
            params.put("uid", uid);
            params.put("date", today);
            params.put("time", time);
            params.put("value", what + "");
            params.put("title", title);
            params.put("content", content);
            params.put("index", index + "");
            new JSONResponse(context, API.API_SUBMIT_NOTIFICATION, params, new JSONResponse.JSONResponseComplete() {
                @Override
                public void onComplete(JSONObject json, boolean netError) {
                    Log.i(TAG, "onComplete: alert json = " + json);
                }
            });
        }
    }

    private void sendYesOrNoNotification(Context context, String contentTitle, String contentText, String time, String type, int notifyID, int scheduleId) {
        String channelId = context.getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Action action1 = new NotificationCompat.Action.Builder(0, "Yes", getPendingIntentForAlertSystem(context, 1, 1, time, type, notifyID, scheduleId)).build();
        NotificationCompat.Action action2 = new NotificationCompat.Action.Builder(0, "No", getPendingIntentForAlertSystem(context, 2, 2, time, type, notifyID, scheduleId)).build();
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
                    mChannel.setDescription("Young+推送");
                    notificationManager.createNotificationChannel(mChannel);
                }
            }
            notificationManager.notify("yesorno", notifyID, notificationBuilder.build());
        }
    }

    // 获取PendingIntent
    private PendingIntent getPendingIntentForAlertSystem(Context context, int what, int flags, String time, String type, int notifyID, int scheduleId) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("type", type);
        intent.putExtra("notifyID", notifyID);
        intent.putExtra("what", what);
        intent.putExtra("time", time);
        intent.putExtra("scheduleId", scheduleId);
        Log.i(TAG, "getPendingIntentForAlertSystem: index = " + scheduleId);
        Log.i(TAG, "getPendingIntent: notifyID = " + notifyID);
        return PendingIntent.getBroadcast(context, notifyID, intent, flags);
    }


    // 获取PendingIntent
    private PendingIntent getPendingIntentForAlertSystem2(Context context, String title, String content, int notifyID) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("type", "alert");
        intent.putExtra("title", title);
        intent.putExtra("content", content);
        Log.i(TAG, "getPendingIntent: notifyID = " + notifyID);
        return PendingIntent.getActivity(context, notifyID, intent, PendingIntent.FLAG_ONE_SHOT);
    }


    private void sendNotification(Context context, String title, String messageBody, int notifyId, Map<String, String> extra) {
        PendingIntent pendingIntent = getPendingIntentForAlertSystem2(context, title, messageBody, notifyId);
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
                        channelId,
                        NotificationManager.IMPORTANCE_DEFAULT);
                notificationManager.createNotificationChannel(channel);
            }
            notificationManager.notify(notifyId /* ID of notification */, notificationBuilder.build());
//            startForeground(1,notificationBuilder.build());
        }
    }

    private void sendMsgFor(Context context, String action, String title, String content, String time, long delay, int notifyID, int index) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Service.ALARM_SERVICE);
        long triggerAtTime = SystemClock.elapsedRealtime() + delay;
        Intent i = new Intent(context, AlarmReceiver.class);
        i.putExtra("notifyID", notifyID);
        i.setAction(action);
        if (!title.equals("") && !content.equals("")) {
            i.putExtra("title", title);
            i.putExtra("content", content);
            i.putExtra("time", time);
            i.putExtra("index", index);
        }
        PendingIntent pi = PendingIntent.getBroadcast(context, notifyID, i, PendingIntent.FLAG_ONE_SHOT);
        if (manager != null) {
            manager.cancel(pi);
            manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        }
    }

    private long calculateDelay(String dateTime) {
        String today = getRealFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String[] nowTimeArr = today.split(" ");
        long delta = 0;
        long goneTime = 0;
        long currentTime = 0;
        try {
            long postTime = getRealFormat("yyyy-MM-dd HH:mm:ss").parse(dateTime).getTime(); // 传入时间
            long todayStartTime = getRealFormat("yyyy-MM-dd").parse(nowTimeArr[0]).getTime(); // 今天0时0分
            currentTime = getRealFormat("yyyy-MM-dd HH:mm:ss").parse(today).getTime(); // 当前时间
            goneTime = currentTime - todayStartTime; // 今天已过去的时间
            delta = postTime - currentTime; // 传入时间与当前时间差
            Log.i(TAG, "sendTest: deltaTime = " + delta);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return delta;
    }

    // 格式化时间
    private SimpleDateFormat getRealFormat(String pattern) {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        format.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));
        return format;
    }

}
