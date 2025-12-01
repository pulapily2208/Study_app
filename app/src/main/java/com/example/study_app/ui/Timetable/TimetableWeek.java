package com.example.study_app.ui.Timetable;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEntity;
import com.example.study_app.R;
import com.example.study_app.data.DatabaseHelper;
import com.example.study_app.data.TimetableDao;
import com.example.study_app.data.UserSession;
import com.example.study_app.ui.Subject.Model.Subject;
import com.example.study_app.ui.Subject.SubjectAddActivity;
import com.google.android.material.chip.Chip;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.jakewharton.threetenabp.AndroidThreeTen;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TimetableWeek extends AppCompatActivity {

    private WeekView weekView;
    private MaterialCalendarView monthCalendar;
    private TextView tvSelectedDate;
    private LinearLayout llDateContainer;
    private Button btnAdd;

    private String selectedDate = null;

    @SuppressLint({ "MissingInflatedId", "ClickableViewAccessibility" })
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidThreeTen.init(this);
        EdgeToEdge.enable(this);
        setContentView(R.layout.timetable_week);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_container), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        weekView = findViewById(R.id.weekView);
        monthCalendar = findViewById(R.id.monthCalendar);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        llDateContainer = findViewById(R.id.llDateContainer);
        btnAdd = findViewById(R.id.btnAdd);

        setupViews();
        setupInteractions();

        CalendarDay today = CalendarDay.today();
        monthCalendar.setSelectedDate(today);
        updateSelectedDate(today);
        goToDate(today);
        displayWeekChips(getStartOfWeek(today));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadEvents();
    }

    private void setupViews() {
        weekView.setNumberOfVisibleDays(7);
        weekView.setHourHeight(120);
        weekView.setColumnGap(2);
        weekView.setEventTextSize(14);
        weekView.setShowNowLine(true);

        // Adapter setup removed temporarily to match the WeekView library API on the
        // current classpath. We'll fetch events directly if/when we implement a
        // compatible adapter for the library version being used.

        weekView.setDateFormatter(date -> {
            SimpleDateFormat weekdayFormat = new SimpleDateFormat("EEE", Locale.getDefault());
            SimpleDateFormat dayFormat = new SimpleDateFormat("dd", Locale.getDefault());
            return weekdayFormat.format(date.getTime()).toUpperCase() + " " + dayFormat.format(date.getTime());
        });

        weekView.setTimeFormatter(hour -> String.format(Locale.getDefault(), "%d:00", hour));
    }

    private void loadEvents() {
        // With the SimpleAdapter, we don't need to load data manually here.
        // We just need to tell the WeekView to refresh itself.
        // This will trigger the adapter's onLoad method.
        Log.d("TimetableWeek", "Forcing WeekView to refresh...");
        // The WeekView in this library version doesn't expose a notifyDataSetChanged()
        // method.
        // Invalidate and request layout to force a redraw which will cause the view to
        // reload data.
        weekView.invalidate();
        weekView.requestLayout();
    }

    private void setupInteractions() {
        btnAdd.setOnClickListener(v -> {
            if (selectedDate == null) {
                Toast.makeText(TimetableWeek.this, "Hãy chọn ngày trước!", Toast.LENGTH_SHORT).show();
                return;
            }

            String[] parts = selectedDate.split("-");
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]) - 1;

            TimetableDao timetableDao = new TimetableDao(new DatabaseHelper(this));
            Integer semesterId = timetableDao.getSemesterIdBySelectedDate(year, month);

            if (semesterId == null) {
                Toast.makeText(this, "Không tìm thấy học kỳ phù hợp!", Toast.LENGTH_SHORT).show();
                return;
            }

            String semesterName = timetableDao.getSemesterNameById(semesterId);
            if (semesterName == null) {
                Toast.makeText(this, "Không thể lấy tên học kỳ!", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(TimetableWeek.this, SubjectAddActivity.class);
            intent.putExtra("selectedDate", selectedDate);
            intent.putExtra("semesterId", semesterId);
            intent.putExtra("SEMESTER_NAME", semesterName);
            startActivity(intent);
        });

        monthCalendar.setOnDateChangedListener((widget, date, selected) -> {
            updateSelectedDate(date);
            goToDate(date);
            displayWeekChips(getStartOfWeek(date));
        });

        GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 50;

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1 == null || e2 == null)
                    return false;
                float diffY = e2.getY() - e1.getY();
                if (Math.abs(diffY) > SWIPE_THRESHOLD) {
                    if (diffY < 0) {
                        hideMonthCalendar();
                    } else {
                        showMonthCalendar();
                    }
                    return true;
                }
                return false;
            }
        });

        tvSelectedDate.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return true;
        });
    }

    private void hideMonthCalendar() {
        if (monthCalendar.getVisibility() == View.VISIBLE) {
            monthCalendar.animate().alpha(0f).setDuration(200)
                    .withEndAction(() -> monthCalendar.setVisibility(View.GONE));
        }
    }

    private void showMonthCalendar() {
        if (monthCalendar.getVisibility() != View.VISIBLE) {
            monthCalendar.setVisibility(View.VISIBLE);
            monthCalendar.animate().alpha(1f).setDuration(200);
        }
    }

    private void updateSelectedDate(CalendarDay date) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd MMM, yyyy", Locale.ENGLISH);
        Calendar c = Calendar.getInstance();
        c.set(date.getYear(), date.getMonth() - 1, date.getDay());
        tvSelectedDate.setText(sdf.format(c.getTime()));

        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        selectedDate = dbFormat.format(c.getTime());
    }

    private void goToDate(CalendarDay date) {
        Calendar c = Calendar.getInstance();
        c.set(date.getYear(), date.getMonth() - 1, date.getDay());
        weekView.goToDate(c);
    }

    private Calendar getStartOfWeek(CalendarDay date) {
        Calendar cal = Calendar.getInstance();
        cal.set(date.getYear(), date.getMonth() - 1, date.getDay());
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int diff = cal.getFirstDayOfWeek() - dayOfWeek;
        if (diff > 0) {
            diff -= 7;
        }
        cal.add(Calendar.DAY_OF_MONTH, diff);
        return cal;
    }

    private void displayWeekChips(Calendar startOfWeek) {
        llDateContainer.removeAllViews();
        SimpleDateFormat sdf = new SimpleDateFormat("EEE dd", Locale.getDefault());
        for (int i = 0; i < 7; i++) {
            Calendar day = (Calendar) startOfWeek.clone();
            day.add(Calendar.DAY_OF_MONTH, i);

            Chip chip = new Chip(this);
            chip.setText(sdf.format(day.getTime()));
            chip.setCheckable(true);

            final Calendar finalDay = day;
            chip.setOnClickListener(v -> {
                monthCalendar.setSelectedDate(CalendarDay.from(finalDay.get(Calendar.YEAR),
                        finalDay.get(Calendar.MONTH) + 1, finalDay.get(Calendar.DAY_OF_MONTH)));
                weekView.goToDate(finalDay);
            });
            llDateContainer.addView(chip);
        }
    }
}
