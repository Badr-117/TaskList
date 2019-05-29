package com.example.to_reminder.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.to_reminder.database.ReminderContract.ReminderEntry;

public class ReminderProvider extends ContentProvider {

    public static final String LOG_TAG = ReminderProvider.class.getSimpleName();

    private ReminderDbHelper mDbHelper;

    private static final int REMINDERS = 100;

    private static final int REMINDER_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {

        sUriMatcher.addURI(ReminderContract.CONTENT_AUTHORITY, ReminderContract.PATH_REMINDERS, REMINDERS);


        sUriMatcher.addURI(ReminderContract.CONTENT_AUTHORITY, ReminderContract.PATH_REMINDERS + "/#", REMINDER_ID);
    }


    @Override
    public boolean onCreate() {
        mDbHelper = new ReminderDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case REMINDERS:

                cursor = database.query(ReminderEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case REMINDER_ID:

                selection = ReminderEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                cursor = database.query(ReminderEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case REMINDERS:
                return ReminderEntry.CONTENT_LIST_TYPE;
            case REMINDER_ID:
                return ReminderEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case REMINDERS:
                return insertTask(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertTask(Uri uri, ContentValues contentValues){

        String task = contentValues.getAsString(ReminderEntry.COLUMN_TASK_NAME);
        if(task == null){
            throw new IllegalArgumentException("Reminder requires a task");
        }

        String date = contentValues.getAsString(ReminderEntry.COLUMN_TASK_DATE);
        if(task == null){
            throw new IllegalArgumentException("Reminder requires a date");
        }

        String time = contentValues.getAsString(ReminderEntry.COLUMN_TASK_TIME);
        if(task == null){
            throw new IllegalArgumentException("Reminder requires a time");
        }

        Integer priority = contentValues.getAsInteger(ReminderEntry.COLUMN_TASK_PRIORITY);
        if(priority == null || !ReminderEntry.isValidPriority(priority)){
            throw new IllegalArgumentException("Reminder requires a priority");
        }


        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new task with the given values
        long id = database.insert(ReminderEntry.TABLE_NAME, null, contentValues);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the task content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case REMINDERS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(ReminderEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case REMINDER_ID:
                // Delete a single row given by the ID in the URI
                selection = ReminderEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(ReminderEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case REMINDERS:
                return updateTask(uri, contentValues, selection, selectionArgs);

            case REMINDER_ID:
                selection = ReminderEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                return updateTask(uri, contentValues, selection, selectionArgs);

            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateTask(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs){

        String task = contentValues.getAsString(ReminderEntry.COLUMN_TASK_NAME);
        if(task == null){
            throw new IllegalArgumentException("Reminder requires a task");
        }

        String date = contentValues.getAsString(ReminderEntry.COLUMN_TASK_DATE);
        if(task == null){
            throw new IllegalArgumentException("Reminder requires a date");
        }

        String time = contentValues.getAsString(ReminderEntry.COLUMN_TASK_TIME);
        if(task == null){
            throw new IllegalArgumentException("Reminder requires a time");
        }

        Integer priority = contentValues.getAsInteger(ReminderEntry.COLUMN_TASK_PRIORITY);
        if(priority == null || !ReminderEntry.isValidPriority(priority)){
            throw new IllegalArgumentException("Reminder requires a priority");
        }


        // If there are no values to update, then don't try to update the database
        if (contentValues.size() == 0) {
            return 0;
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(ReminderEntry.TABLE_NAME, contentValues, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;
    }
}
