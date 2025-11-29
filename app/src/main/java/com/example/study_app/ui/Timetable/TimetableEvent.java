package com.example.study_app.ui.Timetable;

import com.alamkanak.weekview.WeekViewEntity;

import java.util.Calendar;

public class TimetableEvent {

    public static WeekViewEntity createEvent(
            long id,
            String name,
            int year, int month, int day,
            int startHour, int startMinute,
            int endHour, int endMinute,
            String colorHex
    ) {
        Calendar startTime = Calendar.getInstance();
        startTime.set(year, month - 1, day, startHour, startMinute);

        Calendar endTime = Calendar.getInstance();
        endTime.set(year, month - 1, day, endHour, endMinute);

        WeekViewEntity event = new WeekViewEntity.Event.Builder(id)
                .setTitle(name)
                .setStartTime(startTime)
                .setEndTime(endTime)
                .setStyle(new WeekViewEntity.Style.Builder()
                        .setBackgroundColor(android.graphics.Color.parseColor(colorHex))
                        .setTextColor(android.graphics.Color.WHITE)
                        .build())
                .build();
        return event;
    }
}
