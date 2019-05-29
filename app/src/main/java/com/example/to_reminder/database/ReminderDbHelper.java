package com.example.to_reminder.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.to_reminder.database.ReminderContract.ReminderEntry;

public class ReminderDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = ReminderDbHelper.class.getSimpleName();

    private static final String DATABASE_NAME = "task_reminder3.db";

    private static final int DATABASE_VERSION = 1;

    public ReminderDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String SQL_CREATE_REMINDER_TABLE = "CREATE TABLE " + ReminderEntry.TABLE_NAME + " ("
                + ReminderEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ReminderEntry.COLUMN_TASK_NAME + " TEXT NOT NULL, "
                + ReminderEntry.COLUMN_TASK_DATE + " TEXT NOT NULL, "
                + ReminderEntry.COLUMN_TASK_TIME + " TEXT NOT NULL, "
                + ReminderEntry.COLUMN_TASK_PRIORITY + " INTEGER NOT NULL);";

        db.execSQL(SQL_CREATE_REMINDER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

       onCreate(db);
    }
}
