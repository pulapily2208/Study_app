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

    public void scheduleNotification(Deadline deadline) {
        long notificationTime = getNotificationTime(deadline);

        // Do not schedule notifications for past events or if no reminder is set
        if (notificationTime <= System.currentTimeMillis()) {
            Log.w("NotificationManager", "Notification time is in the past. Not scheduling.");
            return;
        }

        Intent intent = new Intent(context, DeadlineNotificationReceiver.class);
        intent.putExtra(DeadlineNotificationReceiver.EXTRA_DEADLINE_ID, deadline.getId());
        intent.putExtra(DeadlineNotificationReceiver.EXTRA_DEADLINE_TITLE, deadline.getTieuDe());
        intent.putExtra(DeadlineNotificationReceiver.EXTRA_DEADLINE_CONTENT, "Deadline của bạn sắp hết hạn.");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                deadline.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Use a standard, less-precise, but more reliable alarm. 
        // This does not require special permissions.
        alarmManager.set(AlarmManager.RTC_WAKEUP, notificationTime, pendingIntent);
        
        Log.d("NotificationManager", "Scheduled notification for deadline ID: " + deadline.getId() + " at " + new java.util.Date(notificationTime));
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

    private long getNotificationTime(Deadline deadline) {
        if (deadline.getNgayKetThuc() == null || deadline.getReminderText() == null) {
            return -1;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(deadline.getNgayKetThuc());

        switch (deadline.getReminderText()) {
            case "Trước sự kiện 5 phút":
                calendar.add(Calendar.MINUTE, -5);
                break;
            case "Trước sự kiện 15 phút":
                calendar.add(Calendar.MINUTE, -15);
                break;
            case "Trước sự kiện 30 phút":
                calendar.add(Calendar.MINUTE, -30);
                break;
            case "Trước sự kiện 1 giờ":
                calendar.add(Calendar.HOUR_OF_DAY, -1);
                break;
            case "Trước sự kiện 1 ngày":
                calendar.add(Calendar.DAY_OF_YEAR, -1);
                break;
            default: // "Không nhắc nhở" or no match
                return -1;
        }

        return calendar.getTimeInMillis();
    }
}
