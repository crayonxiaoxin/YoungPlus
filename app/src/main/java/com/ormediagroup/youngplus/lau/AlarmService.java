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

        sendMsgAboutStartServiceRepeat(30 * 60 * 1000); // 每30分钟获取一次新数据  or 每天一次？
        getSchedule(); // today schedule
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    // 获取今天的时间表
    private void getSchedule() {
        Log.i(TAG, "getSchedule: ");
        final ScheduleDBOpenHelper helper = new ScheduleDBOpenHelper(getApplicationContext());
        final SQLiteDatabase db = helper.getWritableDatabase();

        final User user = new User(getApplicationContext());
        if (user.isUserLoggedIn()) {
            Log.i(TAG, "getSchedule: logged in");
            String selection = "userId=" + user.getUserId() + " and (time between '" + getTodayStartTime() + " 00:00:00'" + " and '" + getTodayStartTime() + " 23:59:59')";
            Log.i(TAG, "getSchedule: selection = " + selection);
            // 如果当天没有，则请求一次
            Cursor c = db.query(helper.getTable(), null, selection, null, null, null, null);
            if (c.getCount() > 0) {
                int notifyID = 10;
                while (c.moveToNext()) {
                    Log.i(TAG, "getSchedule: get");

                    String question = c.getString(c.getColumnIndex("question"));
                    String time = c.getString(c.getColumnIndex("time"));
                    Log.i(TAG, "getSchedule: time = " + time);
                    int scheduleId = c.getInt(c.getColumnIndex("scheduleId"));
                    int isUsed = c.getInt(c.getColumnIndex("used"));
                    if (isUsed == 0) {
                        notifyID++;
                        Log.i(TAG, "onComplete: notifyID = " + notifyID);
                        sendAlertSystemMsgToReceiver(scheduleId, "Young+", question, time, notifyID);
                        update(helper, db, user.getUserId(), scheduleId, question, time);
                    }
                }
            } else {
                new JSONResponse(getApplicationContext(), API.API_GET_SCHEDULE, "uid=" + user.getUserId() + "&action=get", new JSONResponse.onComplete() {
                    @Override
                    public void onComplete(JSONObject json) {
                        Log.i(TAG, "onComplete: schedule json = " + json);
                        if (json.has("rc")) {
                            try {
                                int rc = json.getInt("rc");
                                if (rc == 0) {
                                    JSONArray data = json.getJSONArray("data");
                                    for (int i = 0; i < data.length(); i++) {
                                        String sid = data.getJSONObject(i).getString("id");
                                        String date = data.getJSONObject(i).getString("day");
                                        String question = data.getJSONObject(i).getString("task");
                                        String time = data.getJSONObject(i).getString("time");
                                        insert(helper, db, user.getUserId(), Integer.parseInt(sid), question, date + " " + time);
                                    }
                                    if (data.length() > 0) {
                                        getSchedule();
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
            c.close();
        }
    }

    // 更新已发送的notification状态
    private void update(ScheduleDBOpenHelper helper, SQLiteDatabase db, String userId, int scheduleId, String question, String time) {
        ContentValues values = new ContentValues();
        values.put("used", 1);
        db.update(helper.getTable(), values, "question='" + question + "' and time='" + time + "'" + " and userId=" + userId + " and scheduleId=" + scheduleId, null);
    }

    // 插入每个时间
    private void insert(ScheduleDBOpenHelper helper, SQLiteDatabase db, String userId, int scheduleId, String question, String time) {
        Cursor c = db.query(helper.getTable(), null, "time='" + time + "'" + "and userId=" + userId + " and scheduleId=" + scheduleId, null, null, null, null);
        if (c.getCount() == 0) {
            ContentValues value = new ContentValues();
            value.put("userId", Integer.parseInt(userId));
            value.put("scheduleId", scheduleId);
            value.put("question", question);
            value.put("time", time);
            value.put("used", 0);
            db.insert(helper.getTable(), null, value);
        } else {
            ContentValues values = new ContentValues();
            values.put("question", question);
            values.put("used", 0);
            db.update(helper.getTable(), values, "time='" + time + "'" + "and userId=" + userId + " and scheduleId=" + scheduleId, null);
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
    private void sendMsgForAlertSystem(int scheduleId, String title, String content, long delay, int notifyID) {
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        long triggerAtTime = SystemClock.elapsedRealtime() + delay;
        Intent i = new Intent(this, AlarmReceiver.class);
        i.putExtra("notifyID", notifyID);
        i.putExtra("scheduleId", scheduleId);
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

    // 计划重启时间
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

    // 发送message到receiver
    private void sendAlertSystemMsgToReceiver(int scheduleId, String title, String content, String dateTime, int notifyID) {
        long delta = calculateDelay(dateTime);
        if (delta >= 0) {
            sendMsgForAlertSystem(scheduleId, title, content, delta, notifyID);
        }
        // 处理过期任务
//        else if (delta < 0) {
//            sendMsgTest(title, content, 0, notifyID);
//        }
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
