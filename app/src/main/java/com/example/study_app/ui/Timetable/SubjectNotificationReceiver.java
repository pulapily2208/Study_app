package com.example.study_app.ui.Timetable;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.study_app.R;
import com.example.study_app.ui.Timetable.TimetableWeek;

public class SubjectNotificationReceiver extends BroadcastReceiver {

    public static final String EXTRA_SUBJECT_ID = "extra_subject_id";
    public static final String EXTRA_SUBJECT_NAME = "extra_subject_name";
    public static final String EXTRA_SUBJECT_CONTENT = "extra_subject_content";

    @Override
    public void onReceive(Context context, Intent intent) {
        int id = intent.getIntExtra(EXTRA_SUBJECT_ID, 0);
        String title = intent.getStringExtra(EXTRA_SUBJECT_NAME);
        String content = intent.getStringExtra(EXTRA_SUBJECT_CONTENT);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "subject_channel";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Subject Notifications", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Thông báo nhắc trước giờ học");
            notificationManager.createNotificationChannel(channel);
        }

        Intent tapIntent = new Intent(context, TimetableWeek.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, id, tapIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.bell)
                .setContentTitle(title)
                .setContentText(content)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        notificationManager.notify(id, builder.build());
    }
}
