package com.example.to_reminder.database;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class ReminderContract {

    private ReminderContract(){}

    public static final String CONTENT_AUTHORITY = "com.example.to_reminder";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_REMINDERS = "reminder";

    public static final class ReminderEntry implements BaseColumns{

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_REMINDERS);

        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REMINDERS;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REMINDERS;

        public final static String TABLE_NAME = "reminder3";

        public final static String _ID = BaseColumns._ID;

        public final static String COLUMN_TASK_NAME = "name";

        public final static String COLUMN_TASK_DATE = "date";

        public final static String COLUMN_TASK_TIME = "time";

        public final static String COLUMN_TASK_PRIORITY = "priority";


        public static final int HIGH_PRIORITY = 2;
        public static final int MEDIUM_PRIORITY = 1;
        public static final int LOW_PRIORITY = 0;


        public static boolean isValidPriority(int priority){
            if(priority == LOW_PRIORITY || priority == MEDIUM_PRIORITY || priority == HIGH_PRIORITY){
                return true;
            }
            return false;
        }



    }
}
