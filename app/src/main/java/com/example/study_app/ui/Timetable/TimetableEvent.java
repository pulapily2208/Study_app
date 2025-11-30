package com.example.study_app.ui.Timetable;

import static com.alamkanak.weekview.jsr310.WeekViewExtensionsKt.setStartTime;

import android.graphics.Color;
import android.util.Log;

import com.alamkanak.weekview.WeekViewEntity;
import com.example.study_app.ui.Subject.Model.Subject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TimetableEvent {

//    public static List<WeekViewEntity> convert(List<Subject> subjects) {
//        List<WeekViewEntity> list = new ArrayList<>();
//
//        for (Subject s : subjects) {
//
//            // kiểm tra null – tránh crash
//            if (s == null || s.ngayBatDau == null || s.ngayKetThuc == null ||
//                    s.gioBatDau == null || s.gioKetThuc == null) {
//
//                Log.e("TimetableEvent",
//                        "Subject " + s.tenHp + " missing date or time → skipped");
//                continue;
//            }
//
//            // Ngày bắt đầu - kết thúc của môn
//            Calendar startDate = toCalendar(s.ngayBatDau);
//            Calendar endDate = toCalendar(s.ngayKetThuc);
//
//            // Giờ bắt đầu - giờ kết thúc
//            Calendar timeStart = toCalendar(s.gioBatDau);
//            Calendar timeEnd = toCalendar(s.gioKetThuc);
//
//            int weeks = s.soTuan;
//
//            for (int w = 0; w < weeks; w++) {
//
//                Calendar eventStart = (Calendar) startDate.clone();
//                eventStart.add(Calendar.WEEK_OF_YEAR, w);
//
//                eventStart.set(Calendar.HOUR_OF_DAY, timeStart.get(Calendar.HOUR_OF_DAY));
//                eventStart.set(Calendar.MINUTE, timeStart.get(Calendar.MINUTE));
//
//                Calendar eventEnd = (Calendar) eventStart.clone();
//                eventEnd.set(Calendar.HOUR_OF_DAY, timeEnd.get(Calendar.HOUR_OF_DAY));
//                eventEnd.set(Calendar.MINUTE, timeEnd.get(Calendar.MINUTE));
//
//                long id = generateId(s, w);
//
//                // màu sk
//                WeekViewEntity.Style style = new WeekViewEntity.Style.Builder()
//                        .setBackgroundColor(Color.parseColor(s.mauSac))   // mã màu lấy từ db
//                        .build();
//
//
//                WeekViewEntity ev = new WeekViewEntity.Event.Builder(id)
//                        .setTitle(s.tenHp)
//                        .setStartTime(eventStart)
//                        .setEndTime(eventEnd)
//                        .setStyle(style)
//                        .build();
//                list.add(ev);
//            }
//        }
//        return list;
//    }

    public static List<WeekViewEntity> convertSafe(List<Subject> subjects) {
        List<WeekViewEntity> list = new ArrayList<>();
        if (subjects == null) return list;

        for (Subject s : subjects) {
            if (s == null) continue;

            String title = s.tenHp != null ? s.tenHp : "Unknown";
            String color = (s.mauSac != null && !s.mauSac.isEmpty()) ? s.mauSac : "#3F51B5";
            int weeks = Math.max(1, s.soTuan);

            if (s.ngayBatDau == null || s.gioBatDau == null || s.gioKetThuc == null) {
                Log.e("TimetableEvent", "Skipping subject with missing date/time: " + title);
                continue;
            }

            Calendar startDate = toCalendar(s.ngayBatDau);
            Calendar timeStart = toCalendar(s.gioBatDau);
            Calendar timeEnd = toCalendar(s.gioKetThuc);

            for (int w = 0; w < weeks; w++) {
                Calendar eventStart = (Calendar) startDate.clone();
                eventStart.add(Calendar.WEEK_OF_YEAR, w);
                eventStart.set(Calendar.HOUR_OF_DAY, timeStart.get(Calendar.HOUR_OF_DAY));
                eventStart.set(Calendar.MINUTE, timeStart.get(Calendar.MINUTE));

                Calendar eventEnd = (Calendar) eventStart.clone();
                eventEnd.set(Calendar.HOUR_OF_DAY, timeEnd.get(Calendar.HOUR_OF_DAY));
                eventEnd.set(Calendar.MINUTE, timeEnd.get(Calendar.MINUTE));

                if (!eventEnd.after(eventStart)) {
                    Log.e("TimetableEvent", "Skipping event with end <= start: " + title);
                    continue;
                }

                long id = ((s.maHp != null ? s.maHp : title.hashCode()) + "_" + w).hashCode();

                try {
                    WeekViewEntity ev = new WeekViewEntity.Event.Builder(id)
                            .setTitle(title)
                            .setStartTime(eventStart)
                            .setEndTime(eventEnd)
                            .setStyle(new WeekViewEntity.Style.Builder()
                                    .setBackgroundColor(Color.parseColor(color))
                                    .build())
                            .build();
                    list.add(ev);
                } catch (Exception e) {
                    Log.e("TimetableEvent", "Error creating event: " + title, e);
                }
            }
        }
        return list;
    }


    private static long generateId(Subject s, int weekIndex) {
        return (s.maHp + "_" + weekIndex).hashCode();
    }

    private static Calendar toCalendar(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }
}

