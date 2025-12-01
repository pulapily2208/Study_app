package com.example.study_app.ui.Timetable;

import android.graphics.Color;
import android.util.Log;

import com.alamkanak.weekview.WeekViewEntity;
import com.example.study_app.ui.Subject.Model.Subject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TimetableEvent {

    /**
     * Converts a list of Subjects into a list of WeekViewEntity.Event<Subject>.
     * This version is specifically tailored for the WeekView v5.3.2 API.
     */
    public static List<WeekViewEntity.Event<Subject>> convertSubjectsToEvents(List<Subject> subjects) {
        List<WeekViewEntity.Event<Subject>> events = new ArrayList<>();
        if (subjects == null) {
            return events;
        }

        for (Subject subject : subjects) {
            if (subject == null || subject.ngayBatDau == null || subject.gioBatDau == null
                    || subject.gioKetThuc == null) {
                String id = subject != null ? subject.maHp : "null";
                Log.w("TimetableEvent", "Skipping subject (thiếu dữ liệu bắt buộc) id=" + id);
                continue;
            }

            // Nếu không có ngày kết thúc -> dùng luôn ngày bắt đầu để tạo 1 sự kiện đơn lẻ
            if (subject.ngayKetThuc == null) {
                subject.ngayKetThuc = subject.ngayBatDau;
            }

            String title = (subject.tenHp != null ? subject.tenHp : "Event") + "\n"
                    + (subject.phongHoc != null ? subject.phongHoc : "");
            String color = (subject.mauSac != null && !subject.mauSac.isEmpty()) ? subject.mauSac : "#3F51B5";

            Calendar timeStart = toCalendar(subject.gioBatDau);
            Calendar timeEnd = toCalendar(subject.gioKetThuc);

            Calendar dayIterator = toCalendar(subject.ngayBatDau);
            Calendar subjectEndCal = toCalendar(subject.ngayKetThuc);

            while (!dayIterator.after(subjectEndCal)) {
                Calendar eventStart = (Calendar) dayIterator.clone();
                eventStart.set(Calendar.HOUR_OF_DAY, timeStart.get(Calendar.HOUR_OF_DAY));
                eventStart.set(Calendar.MINUTE, timeStart.get(Calendar.MINUTE));
                eventStart.set(Calendar.SECOND, 0);

                Calendar eventEnd = (Calendar) dayIterator.clone();
                eventEnd.set(Calendar.HOUR_OF_DAY, timeEnd.get(Calendar.HOUR_OF_DAY));
                eventEnd.set(Calendar.MINUTE, timeEnd.get(Calendar.MINUTE));
                eventEnd.set(Calendar.SECOND, 0);

                if (!eventEnd.after(eventStart)) {
                    dayIterator.add(Calendar.DAY_OF_YEAR, 7);
                    continue;
                }

                String eventIdString = generateEventId(subject, eventStart);
                long eventIdLong = eventIdString.hashCode();

                try {
                    WeekViewEntity.Event<Subject> event = (WeekViewEntity.Event<Subject>) new WeekViewEntity.Event.Builder<>(
                            subject)
                            .setId(eventIdLong)
                            .setTitle(title)
                            .setStartTime(eventStart)
                            .setEndTime(eventEnd)
                            .setStyle(new WeekViewEntity.Style.Builder()
                                    .setBackgroundColor(Color.parseColor(color))
                                    .build())
                            .build();
                    events.add(event);
                    Log.d("TimetableEvent", "Created event for subject=" + subject.maHp + " eventId=" + eventIdLong);
                } catch (Exception e) {
                    Log.e("TimetableEvent", "Error creating event with id: " + eventIdString, e);
                }

                dayIterator.add(Calendar.DAY_OF_YEAR, 7);
            }
        }
        return events;
    }

    private static String generateEventId(Subject subject, Calendar startTime) {
        String subjectId = subject.maHp != null ? subject.maHp : "unknown";
        return subjectId + "_" + startTime.getTimeInMillis();
    }

    private static Calendar toCalendar(Date date) {
        if (date == null)
            return Calendar.getInstance();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }
}
