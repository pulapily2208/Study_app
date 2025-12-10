package com.example.study_app.ui.Timetable;

import com.example.study_app.ui.Subject.Model.Subject;
import com.prolificinteractive.materialcalendarview.CalendarDay;

import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SubjectDateGenerator {

    public static List<CalendarDay> getSubjectDays(List<Subject> subjects) {
        List<CalendarDay> result = new ArrayList<>();

        for (Subject s : subjects) {
            if (s.getNgayBatDau() == null) continue;

            Calendar start = Calendar.getInstance();
            start.setTime(s.getNgayBatDau());
            setZeroTime(start);

            Calendar end = Calendar.getInstance();
            if (s.getNgayKetThuc() != null) {
                end.setTime(s.getNgayKetThuc());
            } else {
                end.setTime(start.getTime());
                end.add(Calendar.MONTH, 4);
            }
            setZeroTime(end);

            Calendar cursor = (Calendar) start.clone();

            while (!cursor.after(end)) {

                long diffMillis = cursor.getTimeInMillis() - start.getTimeInMillis();
                long diffDays = diffMillis / (1000 * 60 * 60 * 24);

                if (diffDays % 7 == 0) {

                    LocalDate date = LocalDate.of(
                            cursor.get(Calendar.YEAR),
                            cursor.get(Calendar.MONTH) + 1,
                            cursor.get(Calendar.DAY_OF_MONTH)
                    );

                    result.add(CalendarDay.from(date));
                }

                cursor.add(Calendar.DATE, 1);
            }
        }

        return result;
    }

    private static void setZeroTime(Calendar c) {
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
    }
}
