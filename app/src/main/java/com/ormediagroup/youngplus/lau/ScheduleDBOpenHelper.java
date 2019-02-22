package com.ormediagroup.youngplus.lau;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Lau on 2019/2/21.
 */

public class ScheduleDBOpenHelper extends SQLiteOpenHelper {

    private String TABLE_NAME;

    public ScheduleDBOpenHelper(Context context) {
        super(context, "alarm_schedule.db", null, 1);
        this.TABLE_NAME = "alarm_schedule";
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table if not exists " + this.TABLE_NAME + "(_id integer primary key autoincrement,question text not null,time text not null,used integer not null)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public String getTable() {
        return this.TABLE_NAME;
    }

}
