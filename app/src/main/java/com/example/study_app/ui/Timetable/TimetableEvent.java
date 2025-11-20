package com.example.study_app.ui.Timetable;

import com.alamkanak.weekview.WeekViewEvent;
import java.util.Calendar;

public class TimetableEvent {
    public static WeekViewEvent createEvent(long id, String title, int year, int month, int day,
                                            int startHour, int startMinute, int endHour, int endMinute) {
        Calendar startTime = Calendar.getInstance();
        startTime.set(Calendar.YEAR, year);
        startTime.set(Calendar.MONTH, month - 1); // tháng trong Calendar từ 0
        startTime.set(Calendar.DAY_OF_MONTH, day);
        startTime.set(Calendar.HOUR_OF_DAY, startHour);
        startTime.set(Calendar.MINUTE, startMinute);

        Calendar endTime = Calendar.getInstance();
        endTime.set(Calendar.YEAR, year);
        endTime.set(Calendar.MONTH, month - 1);
        endTime.set(Calendar.DAY_OF_MONTH, day);
        endTime.set(Calendar.HOUR_OF_DAY, endHour);
        endTime.set(Calendar.MINUTE, endMinute);

        WeekViewEvent event = new WeekViewEvent(id, title, startTime, endTime);
        event.setColor(0xFF9F33);
        return event;
    }}
