package com.ormediagroup.youngplus.lau;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;

import com.ormediagroup.youngplus.network.JSONResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Lau on 2018/12/18.
 */

public class AlarmService extends Service {
    private String TAG = "ORM";

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

        sendMsgAboutStartServiceRepeat(30*60*1000);
        getSchedule(); // today schedule
//        return super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    // 获取今天的时间表
    private void getSchedule() {
        Log.i(TAG, "getSchedule: ");
        new JSONResponse(getApplicationContext(), API.API_TEST, "action=1", new JSONResponse.onComplete() {
            @Override
            public void onComplete(JSONObject json) {
                Log.i(TAG, "onComplete: schedule json = " + json);
                ScheduleDBOpenHelper helper = new ScheduleDBOpenHelper(getApplicationContext());
                SQLiteDatabase db = helper.getWritableDatabase();
                if (json.has("data")) {
                    try {
                        JSONArray data = json.getJSONArray("data");
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject obj = data.getJSONObject(i);
                            String date = obj.getString("date");
                            JSONArray schedule = obj.getJSONArray("schedule");
                            for (int j = 0; j < schedule.length(); j++) {
                                JSONObject item = schedule.getJSONObject(j);
                                insert(helper, db, item.getString("task"), date + " " + item.getString("time"));
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                getRealFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                Cursor c = db.query(helper.getTable(), null, "time between '" + getTodayStartTime() + " 00:00:00 '" + " and '" + getTodayStartTime() + " 23:59:59'", null, null, null, null);
                if (c.getCount() > 0) {
                    int index = 10;
                    while (c.moveToNext()) {
                        String question = c.getString(c.getColumnIndex("question"));
                        String time = c.getString(c.getColumnIndex("time"));
                        int isUsed = c.getInt(c.getColumnIndex("used"));
                        if (isUsed == 0) {
                            index++;
                            Log.i(TAG, "onComplete: notifyID = " + index);
                            sendTest("Young+", question, time, index);
                            update(helper, db, question, time);
                        }
                    }
                }
                c.close();
                db.close();
                helper.close();
            }
        });
    }

    // 更新已发送的notification状态
    private void update(ScheduleDBOpenHelper helper, SQLiteDatabase db, String question, String time) {
        ContentValues values = new ContentValues();
        values.put("used", 1);
        db.update(helper.getTable(), values, "question='" + question + "' and time='" + time + "'", null);
    }

    // 插入每个时间
    private void insert(ScheduleDBOpenHelper helper, SQLiteDatabase db, String question, String time) {
        Cursor c = db.query(helper.getTable(), null, "question='" + question + "' and time='" + time + "'", null, null, null, null);
        if (c.getCount() == 0) {
            ContentValues value = new ContentValues();
            value.put("question", question);
            value.put("time", time);
            value.put("used", 0);
            db.insert(helper.getTable(), null, value);
        }
        c.close();
    }

    // 获取今天日期
    private String getTodayStartTime() {
        String today = getRealFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String[] nowTime = today.split(" ");
        return nowTime[0];
    }


    // 设置notification AlarmManager
    private void sendMsgTest(String title, String content, long delay, int notifyID) {
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        long triggerAtTime = SystemClock.elapsedRealtime() + delay;
        Intent i = new Intent(this, AlarmReceiver.class);
        i.putExtra("notifyID", notifyID);
        Log.i(TAG, "sendMsgTest: notifyID = " + notifyID);
        if (!title.equals("") && !content.equals("")) {
            i.putExtra("title", title);
            i.putExtra("content", content);
        }
        PendingIntent pi = PendingIntent.getBroadcast(this, notifyID, i, PendingIntent.FLAG_ONE_SHOT);
        if (manager != null) {
            manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        }
    }

    private void sendMsgAboutStartServiceRepeat(long delay) {
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        long triggerAtTime = SystemClock.elapsedRealtime() + delay;
        Intent i = new Intent(this, AlarmReceiver.class);
        i.putExtra("daily", "true");
        PendingIntent pi = PendingIntent.getBroadcast(this, 999, i, PendingIntent.FLAG_ONE_SHOT);
        if (manager != null) {
            manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        }
    }

    // 计算时间偏移
    private void sendTest(String title, String content, String dateTime, int notifyID) {

//        String today = getRealFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
//        String[] nowTimeArr = today.split(" ");
//
        long delta = 0;
//        long goneTime = 0;
//        long currentTime = 0;
//        try {
//            long postTime = getRealFormat("yyyy-MM-dd HH:mm:ss").parse(dateTime).getTime(); // 传入时间
//            long todayStartTime = getRealFormat("yyyy-MM-dd").parse(nowTimeArr[0]).getTime(); // 今天0时0分
//            currentTime = getRealFormat("yyyy-MM-dd HH:mm:ss").parse(today).getTime(); // 当前时间
//            goneTime = currentTime - todayStartTime; // 今天已过去的时间
//            delta = postTime - currentTime; // 传入时间与当前时间差
//            Log.i(TAG, "sendTest: deltaTime = " + delta);
//            Log.i(TAG, "sendTest: notifyID = " + notifyID);
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
        delta = calculateDelay(dateTime);
        if (delta >= 0) {
            sendMsgTest(title, content, delta, notifyID);
        } else if (delta < 0) {
            sendMsgTest(title, content, 0, notifyID);
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
