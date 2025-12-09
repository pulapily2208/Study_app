package com.example.study_app.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.widget.RemoteViews;

import com.example.study_app.R;
import com.example.study_app.data.DatabaseHelper;
import com.example.study_app.data.TimetableDao;
import com.example.study_app.ui.Subject.Model.Subject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TimetableWidget extends AppWidgetProvider {

    private static final String TAG = "TimetableWidget";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        for (int widgetId : appWidgetIds) {

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.timetable_widget);

            NextLessonResult next = computeNextLesson(context);

            if (next != null && next.subject != null) {

                Subject s = next.subject;
                String weekdayLabel = getWeekdayLabel(s.ngayBatDau);

                views.setTextViewText(R.id.txtSubjectName, s.tenHp);

                views.setTextViewText(R.id.txtWeekday, weekdayLabel);

                views.setTextViewText(
                        R.id.txtTime,
                        formatTime(next.start) + " - " + formatTime(next.end)
                );
                views.setTextViewText(
                        R.id.txtRoom,
                        "Phòng: " + (s.phongHoc == null ? "--" : s.phongHoc)
                );

                // set background color
                try {
                    if (s.mauSac != null && !s.mauSac.isEmpty()) {
                        int color = Color.parseColor(s.mauSac);
                        views.setInt(R.id.widget_root, "setBackgroundColor", color);
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Invalid color: " + s.mauSac);
                }

            } else {
                views.setTextViewText(R.id.txtSubjectName, "Không có buổi học");
                views.setTextViewText(R.id.txtTime, "");
                views.setTextViewText(R.id.txtRoom, "");
            }

            // Widget click => mo detail môn học
//            Intent launch = new Intent(context, com.example.study_app.MainActivity.class);
//            PendingIntent pi = PendingIntent.getActivity(
//                    context,
//                    0,
//                    launch,
//                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
//            );
//            views.setOnClickPendingIntent(R.id.widget_root, pi);

            if (next != null && next.subject != null) {
                Subject s = next.subject;

                // Intent mở SubjectDetailActivity
                Intent intent = new Intent(context, com.example.study_app.ui.Subject.SubjectDetailActivity.class);
                intent.putExtra("SUBJECT_ID", s.maHp);

                PendingIntent pi = PendingIntent.getActivity(
                        context,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

                views.setOnClickPendingIntent(R.id.widget_root, pi);
            }



            appWidgetManager.updateAppWidget(widgetId, views);
        }
    }


    // ========================
    //  Tính buổi học tiếp theo
    // ========================

    private static class NextLessonResult {
        Subject subject;
        Calendar start;
        Calendar end;

        NextLessonResult(Subject s, Calendar st, Calendar en) {
            subject = s;
            start = st;
            end = en;
        }
    }

    private NextLessonResult computeNextLesson(Context context) {

        TimetableDao dao = new TimetableDao(new DatabaseHelper(context));
        List<Subject> subjects = dao.getAllSubjects();

        if (subjects == null || subjects.isEmpty()) return null;

        Calendar now = Calendar.getInstance();
        NextLessonResult best = null;

        for (Subject s : subjects) {

            if (s.ngayBatDau == null || s.gioBatDau == null || s.gioKetThuc == null)
                continue;

            Calendar startDateCal = Calendar.getInstance();
            startDateCal.setTime(s.ngayBatDau);
            int weekday = startDateCal.get(Calendar.DAY_OF_WEEK);

            Calendar candidateDate = nextDateForWeekday(now, weekday);

            Calendar candidateStart = mergeDateTime(candidateDate, s.gioBatDau);
            Calendar candidateEnd = mergeDateTime(candidateDate, s.gioKetThuc);

            if (candidateStart.before(now)) {
                candidateStart.add(Calendar.DAY_OF_YEAR, 7);
                candidateEnd.add(Calendar.DAY_OF_YEAR, 7);
            }

            if (s.ngayKetThuc != null) {
                Calendar endDateOnly = Calendar.getInstance();
                endDateOnly.setTime(s.ngayKetThuc);
                zeroTime(endDateOnly);

                Calendar candidateDateOnly = (Calendar) candidateDate.clone();
                zeroTime(candidateDateOnly);

                if (candidateDateOnly.after(endDateOnly))
                    continue;
            }

            if (best == null ||
                    candidateStart.getTimeInMillis() < best.start.getTimeInMillis()) {

                best = new NextLessonResult(s, candidateStart, candidateEnd);
            }
        }

        return best;
    }


    // ========================
    // Utility
    // ========================

    private Calendar mergeDateTime(Calendar date, Date time) {
        Calendar c = (Calendar) date.clone();
        Calendar t = Calendar.getInstance();
        t.setTime(time);

        c.set(Calendar.HOUR_OF_DAY, t.get(Calendar.HOUR_OF_DAY));
        c.set(Calendar.MINUTE, t.get(Calendar.MINUTE));
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        return c;
    }

    private Calendar nextDateForWeekday(Calendar reference, int targetWeekday) {
        Calendar c = (Calendar) reference.clone();
        zeroTime(c);

        int today = c.get(Calendar.DAY_OF_WEEK);
        int diff = (targetWeekday - today + 7) % 7;

        c.add(Calendar.DAY_OF_YEAR, diff);
        return c;
    }

    private void zeroTime(Calendar c) {
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
    }

    private String formatTime(Calendar cal) {
        SimpleDateFormat f = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return f.format(cal.getTime());
    }

    public static void refreshAll(Context context) {
        AppWidgetManager awm = AppWidgetManager.getInstance(context);
        ComponentName me = new ComponentName(context, TimetableWidget.class);
        int[] ids = awm.getAppWidgetIds(me);

        if (ids != null && ids.length > 0) {
            Intent i = new Intent(context, TimetableWidget.class);
            i.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            i.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
            context.sendBroadcast(i);
        }
    }

    // Lấy thứ
    private String getWeekdayLabel(Date date) {
        if (date == null) return "";

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        int d = cal.get(Calendar.DAY_OF_WEEK); // 1=CN, 2=T2,...

        switch (d) {
            case Calendar.MONDAY: return "Thứ 2";
            case Calendar.TUESDAY: return "Thứ 3";
            case Calendar.WEDNESDAY: return "Thứ 4";
            case Calendar.THURSDAY: return "Thứ 5";
            case Calendar.FRIDAY: return "Thứ 6";
            case Calendar.SATURDAY: return "Thứ 7";
            case Calendar.SUNDAY: return "Chủ nhật";
            default: return "";
        }
    }

}
