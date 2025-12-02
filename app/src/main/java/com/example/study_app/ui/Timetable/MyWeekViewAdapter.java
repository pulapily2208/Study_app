package com.example.study_app.ui.Timetable;

import android.util.Log;

import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEntity;

import java.util.Calendar;
import java.util.List;

public class MyWeekViewAdapter extends WeekView.SimpleAdapter<WeekViewEntity> {

    private final List<WeekViewEntity> events;

    public MyWeekViewAdapter(List<WeekViewEntity> events) {
        super();
        this.events = events;
        Log.d("DEBUG_MyWeekViewAdapter", "Adapter initialized with " + events.size() + " events");
    }




}
