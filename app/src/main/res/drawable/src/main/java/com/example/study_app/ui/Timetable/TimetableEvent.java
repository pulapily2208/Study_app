package com.example.study_app.ui.Timetable;

import com.alamkanak.weekview.WeekViewEvent;
import java.util.Calendar;

public class TimetableEvent {
//    /**
//     * Tạo một sự kiện cho lịch tuần.
//     *
//     * @param id ID của sự kiện
//     * @param title Tiêu đề của sự kiện (Tên môn học)
//     * @param year Năm
//     * @param month Tháng (1-12)
//     * @param day Ngày
//     * @param startHour Giờ bắt đầu
//     * @param startMinute Phút bắt đầu
//     * @param endHour Giờ kết thúc
//     * @param endMinute Phút kết thúc
//     * @param color Màu sắc của sự kiện (Mã màu HEX)
//     * @return WeekViewEvent Đối tượng sự kiện đã được tạo
//     */

//    public static WeekViewEvent createEvent(long id, String title, int year, int month, int day,
//                                            int startHour, int startMinute, int endHour, int endMinute) {
//        Calendar startTime = Calendar.getInstance();
//        startTime.set(Calendar.YEAR, year);
//        startTime.set(Calendar.MONTH, month - 1); // tháng trong Calendar từ 0
//        startTime.set(Calendar.DAY_OF_MONTH, day);
//        startTime.set(Calendar.HOUR_OF_DAY, startHour);
//        startTime.set(Calendar.MINUTE, startMinute);
//        Calendar start = Calendar.getInstance();
////        start.set(year, month - 1, day, startHour, startMinute);
//
//        Calendar endTime = Calendar.getInstance();
//        endTime.set(Calendar.YEAR, year);
//        endTime.set(Calendar.MONTH, month - 1);
//        endTime.set(Calendar.DAY_OF_MONTH, day);
//        endTime.set(Calendar.HOUR_OF_DAY, endHour);
//        endTime.set(Calendar.MINUTE, endMinute);
//
//        WeekViewEvent event = new WeekViewEvent(id, title, startTime, endTime);
//        event.setColor(0xFFFF9F33);
//        return event;
//    }}
    public static WeekViewEvent createEvent(long id, String title, int year, int month, int day,
                                            int startHour, int startMinute, int endHour, int endMinute, String color) {
        // Tạo thời gian bắt đầu
        Calendar startTime = Calendar.getInstance();
        startTime.set(Calendar.YEAR, year);
        startTime.set(Calendar.MONTH, month - 1); // tháng trong Calendar từ 0
        startTime.set(Calendar.DAY_OF_MONTH, day);
        startTime.set(Calendar.HOUR_OF_DAY, startHour);
        startTime.set(Calendar.MINUTE, startMinute);

        // Tạo thời gian kết thúc
        Calendar endTime = Calendar.getInstance();
        endTime.set(Calendar.YEAR, year);
        endTime.set(Calendar.MONTH, month - 1);
        endTime.set(Calendar.DAY_OF_MONTH, day);
        endTime.set(Calendar.HOUR_OF_DAY, endHour);
        endTime.set(Calendar.MINUTE, endMinute);

        // Tạo sự kiện
        WeekViewEvent event = new WeekViewEvent(id, title, startTime, endTime);

        // Đặt màu sắc cho sự kiện từ tham số màu HEX
        event.setColor(android.graphics.Color.parseColor(color));

        return event;
    }
}