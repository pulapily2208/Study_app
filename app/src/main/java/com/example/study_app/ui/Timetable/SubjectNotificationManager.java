package com.example.study_app.ui.Timetable;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.study_app.ui.Subject.Model.Subject;

import java.util.Calendar;

public class SubjectNotificationManager {

    private Context context;
    private AlarmManager alarmManager;

    public SubjectNotificationManager(Context context) {
        this.context = context;
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    public void scheduleWeeklyNotification(Subject subject, String reminderText) {
        if (subject.getNgayBatDau() == null || subject.getGioBatDau() == null || subject.getNgayKetThuc() == null) return;

        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(subject.getNgayBatDau());

        Calendar time = Calendar.getInstance();
        time.setTime(subject.getGioBatDau());

        // Ghép giờ vào ngày bắt đầu
        startCalendar.set(Calendar.HOUR_OF_DAY, time.get(Calendar.HOUR_OF_DAY));
        startCalendar.set(Calendar.MINUTE, time.get(Calendar.MINUTE));
        startCalendar.set(Calendar.SECOND, 0);
        startCalendar.set(Calendar.MILLISECOND, 0);

        // Giảm thời gian theo reminder
        switch (reminderText) {
            case "Trước sự kiện 5 phút": startCalendar.add(Calendar.MINUTE, -5); break;
            case "Trước sự kiện 15 phút": startCalendar.add(Calendar.MINUTE, -15); break;
            case "Trước sự kiện 30 phút": startCalendar.add(Calendar.MINUTE, -30); break;
            case "Trước sự kiện 1 giờ": startCalendar.add(Calendar.HOUR_OF_DAY, -1); break;
            default: return;
        }

        if (startCalendar.getTimeInMillis() <= System.currentTimeMillis()) {
            // Nếu ngày bắt đầu đã qua, tìm tuần kế tiếp
            while (startCalendar.getTimeInMillis() <= System.currentTimeMillis()) {
                startCalendar.add(Calendar.WEEK_OF_YEAR, 1);
            }
        }

        Intent intent = new Intent(context, SubjectNotificationReceiver.class);
        intent.putExtra(SubjectNotificationReceiver.EXTRA_SUBJECT_ID, subject.getMaHp());
        intent.putExtra(SubjectNotificationReceiver.EXTRA_SUBJECT_NAME, subject.getTenHp());
        intent.putExtra(SubjectNotificationReceiver.EXTRA_SUBJECT_CONTENT, "Môn học sắp bắt đầu!");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                subject.getMaHp().hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Lặp lại mỗi tuần
        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                startCalendar.getTimeInMillis(),
                7 * 24 * 60 * 60 * 1000, // 1 tuần
                pendingIntent
        );

        Log.d("SubjectNotification", "Scheduled weekly notification for " + subject.getMaHp() +
                " starting at " + startCalendar.getTime() + " until " + subject.getNgayKetThuc());
    }
    public void scheduleNotification(Subject subject, String reminderText) {
        if (subject.getNgayBatDau() == null || subject.getGioBatDau() == null || subject.getNgayKetThuc() == null) return;
        long notificationTime = getNotificationTime(subject, reminderText);

        if (notificationTime <= System.currentTimeMillis()) {
            Log.w("SubjectNotification", "Notification time is in the past. Not scheduling.");
            return;
        }

        Intent intent = new Intent(context, SubjectNotificationReceiver.class);
        intent.putExtra(SubjectNotificationReceiver.EXTRA_SUBJECT_ID, subject.getMaHp());
        intent.putExtra(SubjectNotificationReceiver.EXTRA_SUBJECT_NAME, subject.getTenHp());
        intent.putExtra(SubjectNotificationReceiver.EXTRA_SUBJECT_CONTENT, "Môn học sắp bắt đầu!");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                subject.getMaHp().hashCode(), // chuyenn ve int
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, notificationTime, pendingIntent);

        Log.d("SubjectNotification", "Scheduled notification for subject ID: " + subject.getMaHp() + " at " + new java.util.Date(notificationTime));
    }

    public void cancelNotification(Subject subject) {
        Intent intent = new Intent(context, SubjectNotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                subject.getMaHp().hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(pendingIntent);
        Log.d("SubjectNotification", "Canceled notification for subject ID: " + subject.getMaHp());
    }

    private long getNotificationTime(Subject subject, String reminderText) {
        if (subject.getNgayBatDau() == null || reminderText == null) return -1;

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(subject.getNgayBatDau());

        switch (reminderText) {
            case "Trước giờ học 5 phút":
                calendar.add(Calendar.MINUTE, -5);
                break;
            case "Trước giờ học 15 phút":
                calendar.add(Calendar.MINUTE, -15);
                break;
            case "Trước giờ học 30 phút":
                calendar.add(Calendar.MINUTE, -30);
                break;
            case "Trước giờ học 1 giờ":
                calendar.add(Calendar.HOUR_OF_DAY, -1);
                break;
            default:
                return -1;
        }

        return calendar.getTimeInMillis();
    }
}
