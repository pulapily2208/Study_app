package com.example.study_app.ui.Timetable;

import androidx.annotation.NonNull;

import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEntity;

import java.util.Calendar;
import java.util.List;

public class MyWeekViewAdapter extends WeekView.SimpleAdapter<WeekViewEntity> {

    private final List<WeekViewEntity> events;

    public MyWeekViewAdapter(List<WeekViewEntity> events) {
        this.events = events;
    }

//    @NonNull

//    @Override
//    public List<WeekViewEntity> onLoad(@NonNull Calendar startDate,
//                                       @NonNull Calendar endDate) {
//        return events; // Trả toàn bộ sự kiện
//    }

}
