package com.example.study_app.ui.Deadline;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.study_app.ui.Deadline.Models.Deadline;

import java.util.Calendar;

public class DeadlineNotificationManager {

    private Context context;
    private AlarmManager alarmManager;

    public DeadlineNotificationManager(Context context) {
        this.context = context;
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    public void cancelNotification(int deadlineId) {
        Intent intent = new Intent(context, DeadlineNotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                deadlineId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(pendingIntent);
        Log.d("NotificationManager", "Canceled notification for deadline ID: " + deadlineId);
    }

}
