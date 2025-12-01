package com.example.study_app.ui.Timetable;

import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEntity;

import java.util.List;

public class MyWeekViewAdapter extends WeekView.SimpleAdapter<WeekViewEntity> {

    private final List<WeekViewEntity> events;

    public MyWeekViewAdapter(List<WeekViewEntity> events) {
        this.events = events;
    }

}
