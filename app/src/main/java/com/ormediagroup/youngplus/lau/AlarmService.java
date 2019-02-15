package com.ormediagroup.youngplus.lau;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.Timer;

/**
 * Created by Lau on 2018/12/18.
 */

public class AlarmService extends Service {
    private String TAG = "ORM";
    private int count = 0;
    private Timer timer;

    private String title = "";
    private String content = "";
    private String daily = "";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startForeground((int) (System.currentTimeMillis() % 10000), new Notification.Builder(this).build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        timer = new Timer();
//        TimerTask timerTask = new TimerTask() {
//            @Override
//            public void run() {
//                count++;
//                Log.i(TAG, "run: " + count);
//            }
//        };
//        timer.schedule(timerTask, 0,1000);

        // 有网的时候获取一次，没网的时候读取sharePreference
//        new JSONResponse(this, "http://youngplus.com.hk/lau-test", "", new JSONResponse.onComplete() {
//            @Override
//            public void onComplete(JSONObject json) {
//                if(json!=null){
//                    JSONObject data;
//                    try {
//                        data = json.getJSONObject("data");
//                        title = data.getString("title");
//                        content = data.getString("content");
//                        daily = data.getString("time");
//
//                        String today = getRealFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
//                        String[] nowTime = today.split(" ");
//
//                        long delta = 0;
//                        long goneTime = 0;
//                        try {
//                            long timed = getRealFormat("yyyy-MM-dd HH:mm").parse(nowTime[0] + " " + daily).getTime();
//                            long date = getRealFormat("yyyy-MM-dd").parse(nowTime[0]).getTime();
//                            Log.i(TAG, "onStartCommand: timed=" + timed);
//                            Log.i(TAG, "onStartCommand: date=" + date);
//                            long now = getRealFormat("yyyy-MM-dd HH:mm:ss").parse(today).getTime();
//                            goneTime = now - date;
//                            Log.i(TAG, "onStartCommand: now=" + now);
//                            delta = timed - now;
//                            Log.i(TAG, "onStartCommand: delta=" + delta);
//                        } catch (ParseException e) {
//                            e.printStackTrace();
//                        }
//                        Log.i(TAG, "onStartCommand: next=" + (24 * 60 * 60 * 1000 - goneTime + delta));
//                        long newDelay = delta > 0 ? delta : 24 * 60 * 60 * 1000 - goneTime + delta;
//                        sendMsg(newDelay);
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        });

//        send("young+", "content", "17:34");

//        title = "Young +";
//        content = "Test";
//        daily = "17:42";
//
//        String today = getRealFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
//        String[] nowTime = today.split(" ");
//
//        long delta = 0;
//        long goneTime = 0;
//        try {
//            long timed = getRealFormat("yyyy-MM-dd HH:mm").parse(nowTime[0] + " " + daily).getTime();
//            long date = getRealFormat("yyyy-MM-dd").parse(nowTime[0]).getTime();
//            Log.i(TAG, "onStartCommand: timed=" + timed);
//            Log.i(TAG, "onStartCommand: date=" + date);
//            long now = getRealFormat("yyyy-MM-dd HH:mm:ss").parse(today).getTime();
//            goneTime = now - date;
//            Log.i(TAG, "onStartCommand: now=" + now);
//            delta = timed - now;
//            Log.i(TAG, "onStartCommand: delta=" + delta);
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//        Log.i(TAG, "onStartCommand: next=" + (24 * 60 * 60 * 1000 - goneTime + delta));
//        long newDelay = delta > 0 ? delta : 24 * 60 * 60 * 1000 - goneTime + delta;
//        sendMsg(title, content, newDelay);
        send("Young+","content","17:45");

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void sendMsg(String title, String content, long delay) {
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        long triggerAtTime = SystemClock.elapsedRealtime() + delay;
        Intent i = new Intent(this, AlarmReceiver.class);
        i.putExtra("about", "test");
        if (!title.equals("") && !content.equals("")) {
            i.putExtra("title", title);
            i.putExtra("content", content);
        }
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
        if (manager != null) {
            manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        }
    }

    private void send(String title, String content, String daily) {

        String today = getRealFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String[] nowTime = today.split(" ");

        long delta = 0;
        long goneTime = 0;
        try {
            long timed = getRealFormat("yyyy-MM-dd HH:mm").parse(nowTime[0] + " " + daily).getTime();
            long date = getRealFormat("yyyy-MM-dd").parse(nowTime[0]).getTime();
            Log.i(TAG, "onStartCommand: timed=" + timed);
            Log.i(TAG, "onStartCommand: date=" + date);
            long now = getRealFormat("yyyy-MM-dd HH:mm:ss").parse(today).getTime();
            goneTime = now - date;
            Log.i(TAG, "onStartCommand: now=" + now);
            delta = timed - now;
            Log.i(TAG, "onStartCommand: delta=" + delta);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "onStartCommand: next=" + (24 * 60 * 60 * 1000 - goneTime + delta));
        long newDelay = delta > 0 ? delta : 24 * 60 * 60 * 1000 - goneTime + delta;
        sendMsg(title, content, newDelay);
    }

    private SimpleDateFormat getRealFormat(String pattern) {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        format.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));
        return format;
    }
}
