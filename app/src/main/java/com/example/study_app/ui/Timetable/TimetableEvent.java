//package com.example.study_app.ui.Timetable;
//
//import com.alamkanak.weekview.WeekViewEntity;
//
//import java.util.Calendar;
//
//public class TimetableEvent {
//
//    public static WeekViewEntity createEvent(
//            long id,
//            String name,
//            int year, int month, int day,
//            int startHour, int startMinute,
//            int endHour, int endMinute,
//            String colorHex
//    ) {
//        Calendar startTime = Calendar.getInstance();
//        startTime.set(year, month - 1, day, startHour, startMinute);
//
//        Calendar endTime = Calendar.getInstance();
//        endTime.set(year, month - 1, day, endHour, endMinute);
//
//        WeekViewEntity event = new WeekViewEntity.Event.Builder(id)
//                .setTitle(name)
//                .setStartTime(startTime)
//                .setEndTime(endTime)
//                .setStyle(new WeekViewEntity.Style.Builder()
//                        .setBackgroundColor(android.graphics.Color.parseColor(colorHex))
//                        .setTextColor(android.graphics.Color.WHITE)
//                        .build())
//                .build();
//        return event;
//    }
//}

//LAY TU DB
//package com.example.study_app.ui.Timetable;
//
//import android.graphics.Color;
//
//import com.alamkanak.weekview.WeekViewEntity;
//import com.example.study_app.ui.Subject.Model.Subject;
//
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.List;
//
//public class TimetableEvent {
//
//    /**
//     * Convert danh sách Subject → danh sách WeekViewEntity (Event instances)
//     * Trả về List<WeekViewEntity> để tránh lỗi kiểu.
//     */
//    public static List<WeekViewEntity> convert(List<Subject> subjects) {
//        List<WeekViewEntity> result = new ArrayList<>();
//
//        if (subjects == null) return result;
//
//        for (Subject s : subjects) {
//
//            if (s.ngayBatDau == null || s.gioBatDau == null || s.gioKetThuc == null)
//                continue;
//
//            // Clone ngày bắt đầu của môn học
//            Calendar startDate = Calendar.getInstance();
//            startDate.setTime(s.ngayBatDau);
//
//            for (int week = 0; week < Math.max(0, s.soTuan); week++) {
//
//                // Ngày học tuần này
//                Calendar lessonDay = (Calendar) startDate.clone();
//                lessonDay.add(Calendar.DAY_OF_MONTH, week * 7);
//
//                // Set giờ bắt đầu
//                Calendar start = (Calendar) lessonDay.clone();
//                Calendar timeStart = Calendar.getInstance();
//                timeStart.setTime(s.gioBatDau);
//                start.set(Calendar.HOUR_OF_DAY, timeStart.get(Calendar.HOUR_OF_DAY));
//                start.set(Calendar.MINUTE, timeStart.get(Calendar.MINUTE));
//                start.set(Calendar.SECOND, 0);
//                start.set(Calendar.MILLISECOND, 0);
//
//                // Set giờ kết thúc
//                Calendar end = (Calendar) lessonDay.clone();
//                Calendar timeEnd = Calendar.getInstance();
//                timeEnd.setTime(s.gioKetThuc);
//                end.set(Calendar.HOUR_OF_DAY, timeEnd.get(Calendar.HOUR_OF_DAY));
//                end.set(Calendar.MINUTE, timeEnd.get(Calendar.MINUTE));
//                end.set(Calendar.SECOND, 0);
//                end.set(Calendar.MILLISECOND, 0);
//
//                // Style cho event
//                WeekViewEntity.Style style = new WeekViewEntity.Style.Builder()
//                        .setBackgroundColor(Color.parseColor(s.mauSac != null ? s.mauSac : "#3F51B5"))
//                        .setTextColor(Color.WHITE)
//                        .build();
//
//                // Tạo event: builder.build() trả về WeekViewEntity (chứ không nhất thiết là Event)
//                WeekViewEntity event = new WeekViewEntity.Event.Builder(s.hashCode() + week)
//                        .setTitle(s.tenHp + (s.phongHoc != null ? " (" + s.phongHoc + ")" : ""))
//                        .setStartTime(start)
//                        .setEndTime(end)
//                        .setStyle(style)
//                        .build();
//
//                result.add(event);
//            }
//        }
//
//        return result;
//    }
//}
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

    public static List<WeekViewEntity> convert(List<Subject> subjects) {
        List<WeekViewEntity> list = new ArrayList<>();

        for (Subject s : subjects) {

            // kiểm tra null – tránh crash
            if (s.ngayBatDau == null || s.ngayKetThuc == null ||
                    s.gioBatDau == null || s.gioKetThuc == null) {

                Log.e("TimetableEvent",
                        "Subject " + s.tenHp + " missing date or time → skipped");
                continue;
            }

            // Ngày bắt đầu - kết thúc của môn
            Calendar startDate = toCalendar(s.ngayBatDau);
            Calendar endDate = toCalendar(s.ngayKetThuc);

            // Giờ bắt đầu - giờ kết thúc
            Calendar timeStart = toCalendar(s.gioBatDau);
            Calendar timeEnd = toCalendar(s.gioKetThuc);

            int weeks = s.soTuan;

            for (int w = 0; w < weeks; w++) {

                Calendar eventStart = (Calendar) startDate.clone();
                eventStart.add(Calendar.WEEK_OF_YEAR, w);

                eventStart.set(Calendar.HOUR_OF_DAY, timeStart.get(Calendar.HOUR_OF_DAY));
                eventStart.set(Calendar.MINUTE, timeStart.get(Calendar.MINUTE));

                Calendar eventEnd = (Calendar) eventStart.clone();
                eventEnd.set(Calendar.HOUR_OF_DAY, timeEnd.get(Calendar.HOUR_OF_DAY));
                eventEnd.set(Calendar.MINUTE, timeEnd.get(Calendar.MINUTE));

                long id = generateId(s, w);

                // màu sk
                WeekViewEntity.Style style = new WeekViewEntity.Style.Builder()
                        .setBackgroundColor(Color.parseColor(s.mauSac))   // mã màu lấy từ db
                        .build();


                WeekViewEntity ev = new WeekViewEntity.Event.Builder(id)
                        .setTitle(s.tenHp)
                        .setStartTime(eventStart)
                        .setEndTime(eventEnd)
                        .setStyle(style)
                        .build();
                list.add(ev);
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

