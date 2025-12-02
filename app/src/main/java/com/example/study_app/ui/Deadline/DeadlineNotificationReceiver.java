package com.example.study_app.ui.Deadline;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.example.study_app.R;

public class DeadlineNotificationReceiver extends BroadcastReceiver {

    public static final String CHANNEL_ID = "DEADLINE_CHANNEL";
    public static final String EXTRA_DEADLINE_ID = "deadline_id";
    public static final String EXTRA_DEADLINE_TITLE = "deadline_title";
    public static final String EXTRA_DEADLINE_CONTENT = "deadline_content";
    public static final String EXTRA_SUBJECT_NAME = "subject_name";

    @Override
    public void onReceive(Context context, Intent intent) {
        int deadlineId = intent.getIntExtra(EXTRA_DEADLINE_ID, 0);
        String title = intent.getStringExtra(EXTRA_DEADLINE_TITLE);
        String content = intent.getStringExtra(EXTRA_DEADLINE_CONTENT);

        if (title == null) {
            title = "Deadline sắp tới";
        }
        if (content == null) {
            content = "Bạn có một deadline sắp hết hạn.";
        }

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Deadline Notifications",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Notifications for upcoming deadlines");
            notificationManager.createNotificationChannel(channel);
        }


        Intent mainActivityIntent = new Intent(context, MainDeadLine.class);
        mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, deadlineId, mainActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.bell) // Corrected icon name
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        notificationManager.notify(deadlineId, builder.build());
    }

}
