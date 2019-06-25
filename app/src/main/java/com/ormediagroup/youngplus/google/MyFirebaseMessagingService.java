package com.ormediagroup.youngplus.google;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.print.PrinterId;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.ormediagroup.youngplus.MainActivity;
import com.ormediagroup.youngplus.R;
import com.ormediagroup.youngplus.lau.API;
import com.ormediagroup.youngplus.lau.AlarmReceiver;
import com.ormediagroup.youngplus.lau.LauUtil;
import com.ormediagroup.youngplus.network.JSONResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.i(TAG, "onTaskRemoved: " + rootIntent);
        Toast.makeText(this, "onTaskRemoved", Toast.LENGTH_SHORT).show();
    }

    public void onMessageReceived(RemoteMessage remoteMessage) {

        // TODO(developer): Handle FCM messages here.

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.e("ORM", "Message data payload: " + remoteMessage.getData());
        }
        PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            // for foreground
            Map<String, String> data = null;
            if (remoteMessage.getData().get("type") != null) {
                data = remoteMessage.getData();
                Log.i(TAG, "onMessageReceived: type = " + remoteMessage.getData().get("type"));
                Log.i(TAG, "onMessageReceived: time = " + remoteMessage.getSentTime());
                sendNotification(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody(), data);
            }
        } else {
            // for background
            if (remoteMessage.getData().size() > 0) {
                switch (remoteMessage.getData().get("type")) {
                    case "alarm_alert":
                        String[] request_link = remoteMessage.getData().get("api").split("\\?");
                        new JSONResponse(this, request_link[0], request_link[1], new JSONResponse.onComplete() {
                            @Override
                            public void onComplete(JSONObject json) {
                                Log.i(TAG, "onComplete: alarm_alert data json = " + json);
                                try {
                                    JSONArray jsonArr = json.getJSONArray("data");
                                    for (int i = 0; i < jsonArr.length(); i++) {
                                        JSONObject obj = jsonArr.getJSONObject(i);
                                        String time = obj.getString("day") + " " + obj.getString("time");
                                        String task = "";
                                        for (int j = 0; j < obj.getJSONArray("task").length(); j++) {
                                            if (j != 0) {
                                                task += ",";
                                            }
                                            task += obj.getJSONArray("task").getString(j);
                                        }
                                        String title = obj.getString("stage");
                                        Log.i(TAG, "onComplete: alarm_alert time = " + time);
                                        Log.i(TAG, "onComplete: alarm_alert task = " + task);
                                        String value = obj.getString("value");
                                        String[] a = time.split(" ");
                                        String requestCode = a[0].replace("-", "") + a[1].replace(":", "");
                                        requestCode = requestCode.substring(2, 12);
                                        int rc = Integer.parseInt(requestCode);
                                        Log.i(TAG, "onComplete: requestCode = " + rc);
                                        long delta = calculateDelay(time);
                                        boolean enable = value.equals("on");
                                        if (delta >= 0) {
                                            sendMsgForAlertSystem(title, task, delta, rc, enable);
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        break;
                    case "nutrition":
                        String[] request_link1 = remoteMessage.getData().get("api").split("\\?");
                        Log.i(TAG, "onMessageReceived: " + request_link1);
                        new JSONResponse(this, request_link1[0], request_link1[1], new JSONResponse.onComplete() {
                            @Override
                            public void onComplete(JSONObject json) {
                                Log.i(TAG, "onComplete: json = " + json);
                                try {
                                    int rc = json.getInt("rc");
                                    if (rc == 0) {
                                        JSONArray data = json.getJSONObject("data").getJSONArray("data");
                                        Log.i(TAG, "onComplete: json data = " + data);
                                        for (int i = 0; i < data.length(); i++) {
                                            JSONObject obj = data.getJSONObject(i);
                                            Log.i(TAG, "onComplete: json data item = " + obj);
                                            String title = obj.getString("title");
                                            String time = obj.getString("time");
                                            String content = "";
                                            JSONArray food = obj.getJSONArray("food");
                                            JSONArray quantity = obj.getJSONArray("quantity");
                                            for (int j = 0; j < food.length(); j++) {
                                                content += food.getString(j) + " " + quantity.getString(j);
                                                if (j < food.length() - 1) {
                                                    content += "，";
                                                }
                                            }
                                            Log.i(TAG, "onComplete: json data content = " + content);
                                            String today = getRealFormat("yyyy-MM-dd").format(new Date());
                                            if (time != null && isTime(time) && !title.equals("") && !content.equals(" ")) {
                                                long delta = calculateDelay(today + " " + (time.length() < 6 ? time + ":00" : time));
                                                delta -= delta - 10 * 60 * 1000; // 10min before
                                                String requestCode = today.replace("-", "") + i;
                                                int resC = Integer.parseInt(requestCode.substring(2));
                                                if (delta >= 0) {
//                                                    sendMsgForNutrition(title, content, time, delta, resC, i);
                                                    sendMsgFor(API.ACTION_NUTRITION, title, content, time, delta, resC, i);
                                                } else {
                                                    delta = delta + 24 * 60 * 60 * 1000;
                                                    Date tmp = new Date();
                                                    tmp.setTime(new Date().getTime() + 24 * 60 * 60 * 1000);
                                                    String tomorrow = getRealFormat("yyyy-MM-dd").format(tmp);
                                                    requestCode = tomorrow.replace("-", "") + i;
                                                    resC = Integer.parseInt(requestCode.substring(2));
//                                                    sendMsgForNutrition(title, content, time, delta, resC, i);
                                                    sendMsgFor(API.ACTION_NUTRITION, title, content, time, delta, resC, i);
                                                }
                                            }
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        break;
                    case "client_alert":
                        String[] request_link2 = remoteMessage.getData().get("api").split("\\?");
                        Log.i(TAG, "onMessageReceived: " + request_link2);
                        new JSONResponse(this, request_link2[0], request_link2[1], new JSONResponse.onComplete() {
                            @Override
                            public void onComplete(JSONObject json) {
                                Log.i(TAG, "onComplete: json = " + json);
                                try {
                                    int rc = json.getInt("rc");
                                    if (rc == 0) {
                                        JSONArray data = json.getJSONObject("data").getJSONArray("data");
                                        Log.i(TAG, "onComplete: json data = " + data);
                                        for (int i = 0; i < data.length(); i++) {
                                            JSONObject obj = data.getJSONObject(i);
                                            Log.i(TAG, "onComplete: json data item = " + obj);
                                            String title = obj.getString("title");
                                            String time = obj.getString("time");
                                            String content = "";
                                            JSONArray contentArr = obj.getJSONArray("content");
                                            for (int j = 0; j < contentArr.length(); j++) {
                                                content += contentArr.getString(j);
                                                if (j < contentArr.length() - 1) {
                                                    content += "，";
                                                }
                                            }
                                            Log.i(TAG, "onComplete: json data content = " + content);
                                            String today = getRealFormat("yyyy-MM-dd").format(new Date());
                                            Log.i(TAG, "onComplete: isTime = " + isTime(time));
                                            if (time != null && isTime(time) && !title.equals("") && !content.equals(" ")) {
                                                long delta = calculateDelay(today + " " + (time.length() < 6 ? time + ":00" : time));
                                                delta -= delta - 10 * 60 * 1000; // 10min before
                                                String requestCode = today.replace("-", "") + (i + 10);
                                                int resC = Integer.parseInt(requestCode.substring(2));
                                                if (delta >= 0) {
//                                                    sendMsgForClientAlert(title, content, time, delta, resC, i);
                                                    sendMsgFor(API.ACTION_CLIENT_ALERT, title, content, time, delta, resC, i);
                                                } else {
                                                    delta = delta + 24 * 60 * 60 * 1000;
                                                    Date tmp = new Date();
                                                    tmp.setTime(new Date().getTime() + 24 * 60 * 60 * 1000);
                                                    String tomorrow = getRealFormat("yyyy-MM-dd").format(tmp);
                                                    requestCode = tomorrow.replace("-", "") + (i + 10);
                                                    resC = Integer.parseInt(requestCode.substring(2));
//                                                    sendMsgForClientAlert(title, content, time, delta, resC, i);
                                                    sendMsgFor(API.ACTION_CLIENT_ALERT, title, content, time, delta, resC, i);
                                                }
                                            }
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        break;
                }
            }
        }
    }

    private boolean isTime(String str) {
        Pattern pattern = Pattern.compile("^[0-9]{2}:[0-9]{2}$");
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();
    }


    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
        Log.i(TAG, "onDeletedMessages: payload?");
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
        PendingIntent pendingIntent;
        if (extra != null) {
            String type = extra.get("type") != null ? extra.get("type") : "";
            switch (type) {
                case "page":
                    // for background, foreground can get this message directly without intent.putExtra()
                    intent.putExtra("type", type);
                    if (extra.get("page") != null) {
                        intent.putExtra("page", extra.get("page"));
                    }
                    if (extra.get("id") != null) {
                        Log.i(TAG, "sendNotification: payload page id " + extra.get("id"));
                        intent.putExtra("id", extra.get("id"));
                    }
                    pendingIntent = PendingIntent.getActivity(this, 0, intent,
                            PendingIntent.FLAG_ONE_SHOT);
                    break;
                case "link":
                    // only for foreground, without sending extra
                    if (extra.get("link") != null) {
                        Uri uri = Uri.parse(LauUtil.getLegalURL(extra.get("link")));
                        intent = new Intent(Intent.ACTION_VIEW, uri);
                        intent.putExtra("link", extra.get("link"));
                    }
                    pendingIntent = PendingIntent.getActivity(this, 2, intent,
                            PendingIntent.FLAG_ONE_SHOT);
                    break;
                case "alarm_alert":
                    pendingIntent = getPendingIntentForAlertSystem(this, 0, 0, 0);
                    break;
                default:
                    pendingIntent = PendingIntent.getActivity(this, 0, intent,
                            PendingIntent.FLAG_ONE_SHOT);
                    break;
            }
        } else {
            pendingIntent = PendingIntent.getActivity(this, 0, intent,
                    PendingIntent.FLAG_ONE_SHOT);
        }

        String channelId = getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.setBigContentTitle(title).bigText(messageBody);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.young_icon_192x192)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.young_icon_192x192))
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setStyle(bigTextStyle)
                        .setAutoCancel(true)
                        .setPriority(Notification.PRIORITY_HIGH)
                        .setDefaults(Notification.DEFAULT_ALL)
//                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (notificationManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(channelId,
                        channelId,
                        NotificationManager.IMPORTANCE_DEFAULT);
                channel.setDescription("Young+推送");
                notificationManager.createNotificationChannel(channel);
            }
            notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
        }
    }

    // 获取PendingIntent
    private PendingIntent getPendingIntentForAlertSystem(Context context, int what, int flags, int notifyID) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("notifyID", notifyID);
        intent.putExtra("what", what);
        Log.i(TAG, "getPendingIntent: notifyID = " + notifyID);
        return PendingIntent.getBroadcast(context, notifyID, intent, flags);
    }

    // 设置notification AlarmManager
    private void sendMsgForAlertSystem(String title, String content, long delay, int notifyID, boolean enable) {
        AlarmManager manager = (AlarmManager) getSystemService(Service.ALARM_SERVICE);
        long triggerAtTime = SystemClock.elapsedRealtime() + delay;
        Intent i = new Intent(this, AlarmReceiver.class);
        i.putExtra("notifyID", notifyID);
        i.setAction(API.ACTION_ALARM_ALERT);
        if (!title.equals("") && !content.equals("")) {
            i.putExtra("title", title);
            i.putExtra("content", content);
        }
        PendingIntent pi = PendingIntent.getBroadcast(this, notifyID, i, PendingIntent.FLAG_ONE_SHOT);
        if (manager != null) {
            manager.cancel(pi);
            if (enable) {
                manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
            }
        }
    }

    private void sendMsgFor(String action, String title, String content, String time, long delay, int notifyID, int index) {
        AlarmManager manager = (AlarmManager) getSystemService(Service.ALARM_SERVICE);
        long triggerAtTime = SystemClock.elapsedRealtime() + delay;
        Intent i = new Intent(this, AlarmReceiver.class);
        i.putExtra("notifyID", notifyID);
        i.setAction(action);
        if (!title.equals("") && !content.equals("")) {
            i.putExtra("title", title);
            i.putExtra("content", content);
            i.putExtra("time", time);
            i.putExtra("index", index);
        }
        PendingIntent pi = PendingIntent.getBroadcast(this, notifyID, i, PendingIntent.FLAG_ONE_SHOT);
        if (manager != null) {
            manager.cancel(pi);
            manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        }
    }

    // 计算时间偏移
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
