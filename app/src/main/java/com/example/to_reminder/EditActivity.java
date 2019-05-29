package com.example.to_reminder;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.to_reminder.database.ReminderContract.ReminderEntry;
import com.example.to_reminder.database.ReminderProvider;

import java.util.Calendar;
import java.util.Date;

public class EditActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = EditActivity.class.getSimpleName();

    private static final int EXISTING_REMINDER_LOADER = 0;

    private Uri mCurrentTaskUri;

    private EditText mTaskEditText;
    private TextView mDateBtnText, mTimeBtnText, mDateTextView, mTimeTextView;

    private Spinner mPrioritySpinner;

    private int mPriority = ReminderEntry.LOW_PRIORITY;

    private Calendar calendar;

    private boolean mTaskHasChanged = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mTaskHasChanged = true;
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        mCurrentTaskUri = intent.getData();

        if (mCurrentTaskUri == null) {
            setTitle("Add Task");

        } else {
            setTitle("Edit Task");

            // Initialize a loader to read the Task data from the database
            // and display the current values in the editor
            getSupportLoaderManager().initLoader(EXISTING_REMINDER_LOADER, null, this);
        }

        mTaskEditText = (EditText) findViewById(R.id.edit_task_et);
        mDateBtnText = (TextView) findViewById(R.id.date_btn);
        mTimeBtnText = (TextView) findViewById(R.id.time_btn);
        mDateTextView = (TextView) findViewById(R.id.date_tv);
        mTimeTextView = (TextView) findViewById(R.id.time_tv);
        mPrioritySpinner = (Spinner) findViewById(R.id.priority_spinner);

        mTaskEditText.setOnTouchListener(mTouchListener);
        mDateBtnText.setOnTouchListener(mTouchListener);
        mTimeBtnText.setOnTouchListener(mTouchListener);
        mPrioritySpinner.setOnTouchListener(mTouchListener);

        mDateBtnText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCalendar();
            }
        });

        mTimeBtnText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showClock();
            }
        });

        PrioritySetupSpinner();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new Task, hide the "Delete" menu item.
        if (mCurrentTaskUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                if (validInput())
                    finish();
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if (!mTaskHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditActivity.this);
                    return true;
                }

                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(EditActivity.this);
                            }
                        };

                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        if (!mTaskHasChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };

        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void PrioritySetupSpinner() {
        ArrayAdapter prioritySpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.array_priority_options,
                android.R.layout.simple_spinner_item);

        prioritySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        mPrioritySpinner.setAdapter(prioritySpinnerAdapter);

        mPrioritySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.high_priority))) {
                        mPriority = ReminderEntry.HIGH_PRIORITY;
                    } else if (selection.equals(getString(R.string.medium_priority))) {
                        mPriority = ReminderEntry.MEDIUM_PRIORITY;
                    } else {
                        mPriority = ReminderEntry.LOW_PRIORITY;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mPriority = ReminderEntry.LOW_PRIORITY;
            }
        });
    }


    private void saveTask() {

        String taskName = mTaskEditText.getText().toString();
        String dateTask = mDateTextView.getText().toString();
        String timeTask = mTimeTextView.getText().toString();


        if (mCurrentTaskUri == null &&
                TextUtils.isEmpty(taskName) && TextUtils.isEmpty(dateTask) && TextUtils.isEmpty(timeTask) &&
                mPriority == ReminderEntry.LOW_PRIORITY) {
            return;
        }

        ContentValues values = new ContentValues();
        values.put(ReminderEntry.COLUMN_TASK_NAME, taskName);
        values.put(ReminderEntry.COLUMN_TASK_DATE, dateTask);
        values.put(ReminderEntry.COLUMN_TASK_TIME, timeTask);
        values.put(ReminderEntry.COLUMN_TASK_PRIORITY, mPriority);

        // Determine if this is a new or existing Task by checking if mCurrentTaskUri is null or not
        if (mCurrentTaskUri == null) {
            // This is a NEW Task, so insert a new task into the provider,
            // returning the content URI for the new task.
            Uri newUri = getContentResolver().insert(ReminderEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, "Error adding task",
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, "Task added",
                        Toast.LENGTH_SHORT).show();
            }
        } else {

            int rowsAffected = getContentResolver().update(mCurrentTaskUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, "Error updating task",
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, "Task updated",
                        Toast.LENGTH_SHORT).show();
            }
        }


    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int i, @Nullable Bundle bundle) {
        String[] projection = {
                ReminderEntry._ID,
                ReminderEntry.COLUMN_TASK_NAME,
                ReminderEntry.COLUMN_TASK_DATE,
                ReminderEntry.COLUMN_TASK_TIME,
                ReminderEntry.COLUMN_TASK_PRIORITY};

        return new CursorLoader(this,
                mCurrentTaskUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {

        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {

            int taskColumnIndex = cursor.getColumnIndex(ReminderEntry.COLUMN_TASK_NAME);
            int dateColumnIndex = cursor.getColumnIndex(ReminderEntry.COLUMN_TASK_DATE);
            int timeColumnIndex = cursor.getColumnIndex(ReminderEntry.COLUMN_TASK_TIME);
            int priorityColumnIndex = cursor.getColumnIndex(ReminderEntry.COLUMN_TASK_PRIORITY);

            String ReminderTask = cursor.getString(taskColumnIndex);
            String ReminderDate = cursor.getString(dateColumnIndex);
            String ReminderTime = cursor.getString(timeColumnIndex);
            int ReminderPriority = cursor.getInt(priorityColumnIndex);

            mTaskEditText.setText(ReminderTask);
            mDateTextView.setText(ReminderDate);
            mTimeTextView.setText(ReminderTime);

            switch (ReminderPriority) {
                case ReminderEntry.HIGH_PRIORITY:
                    mPrioritySpinner.setSelection(2);
                    break;
                case ReminderEntry.MEDIUM_PRIORITY:
                    mPrioritySpinner.setSelection(1);
                default:
                    mPrioritySpinner.setSelection(0);
            }

        }

    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

        mTaskEditText.setText("");
        mDateTextView.setText("dd/mm/yyyy");
        mTimeTextView.setText("hh:mm");
        mPrioritySpinner.setSelection(0);
    }

    private void showCalendar() {
        calendar = Calendar.getInstance();
        final int date = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);

        DatePickerDialog datePickerDialog = new DatePickerDialog(EditActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month + 1;
                mDateTextView.setText("" + dayOfMonth + "/" + month + "/" + year);
            }
        }, year, month, date);
        datePickerDialog.show();

    }

    private void showClock() {
        calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR);
        int minute = calendar.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog = new TimePickerDialog(EditActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                mTimeTextView.setText("" + convertTime(hourOfDay) + ":" + convertTime(minute));
            }
        }, hour, minute, false);
        timePickerDialog.show();

    }

    public String convertTime(int input) {
        if (input >= 10) {
            return String.valueOf(input);
        } else {
            return "0" + String.valueOf(input);
        }
    }

    private void deleteTask() {
        if (mCurrentTaskUri != null) {
            // Call the ContentResolver to delete the task at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentTaskUri
            // content URI already identifies the task that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentTaskUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, "Error deleting task",
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, "Task deleted",
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you really want to delete the task ?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the task.
                deleteTask();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the task.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Discard your changes and quit editing?");
        builder.setPositiveButton("Discard", discardButtonClickListener);
        builder.setNegativeButton("Keep editing", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the task.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private boolean validInput() {
        String taskName = mTaskEditText.getText().toString();
        String dateTask = mDateTextView.getText().toString();
        String timeTask = mTimeTextView.getText().toString();

        if (taskName.matches("") || dateTask.matches("") || timeTask.matches("")) {
            Toast.makeText(this, "Please enter an input", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            saveTask();
            Toast.makeText(this, "Task saved", Toast.LENGTH_SHORT).show();
            return true;
        }

    }

}
