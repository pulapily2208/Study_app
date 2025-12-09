package com.example.study_app.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.widget.RemoteViews;

import com.example.study_app.R;
import com.example.study_app.data.TimetableDao;
import com.example.study_app.ui.Subject.Model.Subject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link CalendarWidgetConfigureActivity CalendarWidgetConfigureActivity}
 */
public class CalendarWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] appWidgetIds) {

        // --- 1. Lấy danh sách môn học đã ghi danh ---
        TimetableDao timetableDao = new TimetableDao(new com.example.study_app.data.DatabaseHelper(context));
        List<Subject> subjects = timetableDao.getAllSubjects(); // lấy tất cả môn
        // Nếu muốn lọc theo user, có thể dùng getSubjectsForTimetable(userId)

        for (int appWidgetId : appWidgetIds) {

            RemoteViews views = new RemoteViews(context.getPackageName(),
                    R.layout.calendar_widget);

            // --- 2. Lấy ngày hiện tại và set tháng/năm ---
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_MONTH, 1); // set về ngày đầu tháng

            String[] monthNames = {"Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6",
                    "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"};
            int monthIndex = cal.get(Calendar.MONTH);
            int year = cal.get(Calendar.YEAR);

            // Set TextView tháng
            views.setTextViewText(R.id.month_text, monthNames[monthIndex] + ", " + year);

            // --- 3. Tính thông tin tháng ---
            int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1; // offset từ CN=0
            int maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

            int today = Calendar.getInstance().get(Calendar.DAY_OF_MONTH); // ngày hôm nay

            // --- 4. Xoá tất cả ô trước khi fill ---
            for (int i = 0; i < 42; i++) {
                int cellId = context.getResources()
                        .getIdentifier("day" + i, "id", context.getPackageName());

                views.setTextViewText(cellId, "");
                views.setInt(cellId, "setBackgroundColor", 0x00000000); // remove background
            }

            // --- 5. Fill ngày và chấm màu môn học ---
            for (int day = 1; day <= maxDay; day++) {
                int cellIndex = firstDayOfWeek + day - 1;
                int cellId = context.getResources()
                        .getIdentifier("day" + cellIndex, "id", context.getPackageName());

                // --- Lấy danh sách môn học trong ngày này ---
                List<Subject> subjectsInDay = new ArrayList<>();
                Calendar dayCal = Calendar.getInstance();
                dayCal.set(Calendar.YEAR, year);
                dayCal.set(Calendar.MONTH, monthIndex);
                dayCal.set(Calendar.DAY_OF_MONTH, day);
                dayCal.set(Calendar.HOUR_OF_DAY, 0);
                dayCal.set(Calendar.MINUTE, 0);
                dayCal.set(Calendar.SECOND, 0);
                dayCal.set(Calendar.MILLISECOND, 0);

                for (Subject s : subjects) {
                    if (s.getNgayBatDau() == null) continue;

                    Calendar start = Calendar.getInstance();
                    start.setTime(s.getNgayBatDau());
                    start.set(Calendar.HOUR_OF_DAY, 0);
                    start.set(Calendar.MINUTE, 0);
                    start.set(Calendar.SECOND, 0);
                    start.set(Calendar.MILLISECOND, 0);

                    Calendar end = Calendar.getInstance();
                    if (s.getNgayKetThuc() != null) {
                        end.setTime(s.getNgayKetThuc());
                        end.set(Calendar.HOUR_OF_DAY, 0);
                        end.set(Calendar.MINUTE, 0);
                        end.set(Calendar.SECOND, 0);
                        end.set(Calendar.MILLISECOND, 0);
                    } else {
                        end = start;
                    }

//                    if (!dayCal.before(start) && !dayCal.after(end)) {
//                        subjectsInDay.add(s);
//                    }

                    // dayCal > start && lặp lại mỗi 7 ngày
                    if (!dayCal.before(start) && !dayCal.after(end)) {

                        long diffMillis = dayCal.getTimeInMillis() - start.getTimeInMillis();
                        long diffDays = diffMillis / (1000 * 60 * 60 * 24);

                        // nếu số ngày cách nhau là bội của 7 → đúng ngày học
                        if (diffDays % 7 == 0) {
                            subjectsInDay.add(s);
                        }
                    }

                }

                // --- Hiển thị ngày và chấm màu ---
                StringBuilder text = new StringBuilder();
                text.append(day); // số ngày

                int maxDisplay = Math.min(2, subjectsInDay.size());

                for (int i = 0; i < maxDisplay; i++) {
                    Subject sub = subjectsInDay.get(i);
                    text.append("\n").append(shortName(sub.getTenHp()));
                }

                if (subjectsInDay.size() > 2) {
                    text.append("\n+").append(subjectsInDay.size() - 2);
                }

//                for (int i = 0; i < Math.min(subjectsInDay.size(), 3); i++) {
//                    // dùng ký tự ● làm chấm nhỏ đại diện môn học
//                    text.append("\n●");
//                }

                views.setTextViewText(cellId, text.toString());
                views.setTextViewTextSize(cellId, TypedValue.COMPLEX_UNIT_SP, 9f);

                // Highlight ngày hôm nay
                if (day == today) {
                    views.setInt(cellId, "setBackgroundColor", 0xFFCCE5FF); // màu xanh nhạt
                }
            }

            // --- 6. Cập nhật widget ---
            manager.updateAppWidget(appWidgetId, views);
        }
    }


    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        for (int appWidgetId : appWidgetIds) {
            CalendarWidgetConfigureActivity.deleteTitlePref(context, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        CharSequence widgetText = CalendarWidgetConfigureActivity.loadTitlePref(context, appWidgetId);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.calendar_widget);
        views.setTextViewText(R.id.appwidget_text, widgetText);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private String shortName(String name) {
        if (name == null || name.isEmpty()) return "";

        // Lấy chữ cái đầu mỗi từ
        String[] parts = name.split(" ");
        StringBuilder abbr = new StringBuilder();

        for (String p : parts) {
            if (!p.isEmpty()) {
                abbr.append(Character.toUpperCase(p.charAt(0)));
            }
        }
        return "\uD83D\uDFE2" + abbr.toString();
    }

}
