package com.example.study_app.ui.deadline;

public class DateModel {
    private String dayName;
    private int dayNumber;
    private boolean isSelected;

    public DateModel(String dayName, int dayNumber) {
        this.dayName = dayName;
        this.dayNumber = dayNumber;
        this.isSelected = false;
    }

    public String getDayName() {
        return dayName;
    }

    public int getDayNumber() {
        return dayNumber;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
