package com.example.to_reminder;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.graphics.drawable.GradientDrawable;

import com.example.to_reminder.database.ReminderContract.ReminderEntry;

public class ReminderCursorAdapter extends CursorAdapter {

    GradientDrawable priorityCircle;


    public ReminderCursorAdapter(Context context, Cursor c){
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.content_list, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView taskTextView = (TextView) view.findViewById(R.id.myTask_tv);
        TextView DateTextView = (TextView) view.findViewById(R.id.myDate_tv);
        TextView TimeTextView = (TextView) view.findViewById(R.id.myTime_tv);
        TextView PriorityTextView = (TextView) view.findViewById(R.id.myPriority_tv);

        int taskColumnIndex = cursor.getColumnIndex(ReminderEntry.COLUMN_TASK_NAME);
        int dateColumnIndex = cursor.getColumnIndex(ReminderEntry.COLUMN_TASK_DATE);
        int timeColumnIndex = cursor.getColumnIndex(ReminderEntry.COLUMN_TASK_TIME);
        int priorityColumnIndex = cursor.getColumnIndex(ReminderEntry.COLUMN_TASK_PRIORITY);

        String ReminderTask = cursor.getString(taskColumnIndex);
        String ReminderDate = cursor.getString(dateColumnIndex);
        String ReminderTime = cursor.getString(timeColumnIndex);
        int ReminderPriority = cursor.getInt(priorityColumnIndex);

        taskTextView.setText(ReminderTask);
        DateTextView.setText(ReminderDate);
        TimeTextView.setText(ReminderTime);

        priorityCircle = (GradientDrawable) PriorityTextView.getBackground();

        if(ReminderPriority == ReminderEntry.LOW_PRIORITY){
            priorityCircle.setColor(ContextCompat.getColor(context, R.color.LowPriorityColor));
        }else if(ReminderPriority == ReminderEntry.MEDIUM_PRIORITY){
            priorityCircle.setColor(ContextCompat.getColor(context, R.color.MediumPriorityColor));
        }else{
            priorityCircle.setColor(ContextCompat.getColor(context, R.color.HighPriorityColor));
        }

    }

}
