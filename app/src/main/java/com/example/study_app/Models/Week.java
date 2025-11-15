package com.example.study_app.Models;

import java.util.ArrayList;

public class Week {
    private String tenTuan;
    private ArrayList<Deadline> deadlines;

    public Week(String tenTuan) {
        this.tenTuan = tenTuan;
        this.deadlines = new ArrayList<>();
    }

    public String getTenTuan() { return tenTuan; }
    public ArrayList<Deadline> getDeadlines() { return deadlines; }
}
