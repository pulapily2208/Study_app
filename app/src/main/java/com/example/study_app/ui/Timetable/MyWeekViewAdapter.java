package com.example.study_app.ui.Timetable;

import androidx.annotation.NonNull;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEntity;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public class MyWeekViewAdapter extends WeekView.SimpleAdapter<WeekViewEntity> {

    private final List<WeekViewEntity> events;

    public MyWeekViewAdapter(List<WeekViewEntity> events) {
        this.events = events;
    }

    @NonNull
    public List<WeekViewEntity> onMonthChange(
            @NonNull YearMonth yearMonth,
            @NonNull LocalDate firstVisibleDay,
            @NonNull LocalDate lastVisibleDay
    ) {
        return events;   // load tất cả sự kiện
    }
}
