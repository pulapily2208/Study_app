package com.example.study_app.ui.Timetable;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEntity;
import com.alamkanak.weekview.WeekViewEntity.Event;
import com.alamkanak.weekview.DateTimeInterpreter;
import com.example.study_app.R;
import com.example.study_app.data.DatabaseHelper;
import com.example.study_app.ui.Subject.Model.Subject;
import com.example.study_app.ui.Subject.SubjectAddActivity;
import com.google.android.material.chip.Chip;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TimetableWeek extends AppCompatActivity {

    // khai báo
    WeekView weekView;

    MaterialCalendarView monthCalendar;
    TextView tvSelectedDate;
    LinearLayout llDateContainer;
//    List<WeekViewEntity> events = new ArrayList<>();
List<WeekViewEntity.Event> events = new ArrayList<>();

    Button btnAdd;

    // ẩn lịch tháng
    private void hideMonthCalendar() {
        if (monthCalendar.getVisibility() == View.VISIBLE) {
            monthCalendar.animate()
                    .translationY(-monthCalendar.getHeight())
                    .alpha(0f)
                    .setDuration(250)
                    .withEndAction(() -> monthCalendar.setVisibility(View.GONE))
                    .start();
        }
    }

    // hiện lịch tháng
    private void showMonthCalendar() {
        if (monthCalendar.getVisibility() != View.VISIBLE) {
            monthCalendar.setVisibility(View.VISIBLE);
            monthCalendar.animate()
                    .translationY(0)
                    .alpha(1f)
                    .setDuration(250)
                    .start();
        }
    }

    private void updateSelectedDate(CalendarDay date) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd MMM, yyyy", Locale.ENGLISH);
        Calendar c = Calendar.getInstance();
        c.set(date.getYear(), date.getMonth(), date.getDay());
        tvSelectedDate.setText(sdf.format(c.getTime()));
    }

    private void goToDate(CalendarDay date) {
        Calendar c = Calendar.getInstance();
        c.set(date.getYear(), date.getMonth(), date.getDay());
        weekView.goToDate(c);  // API mới vẫn có goToDate(Calendar)
    }

    // Tính ngày Monday của tuần
    private Calendar getStartOfWeek(CalendarDay date) {
        Calendar cal = Calendar.getInstance();
        cal.set(date.getYear(), date.getMonth(), date.getDay());
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int diff = Calendar.MONDAY - dayOfWeek;
        cal.add(Calendar.DAY_OF_MONTH, diff);
        return cal;
    }

    // Tạo 7 chip tuần, click scroll WeekView
    private void displayWeekChips(Calendar startOfWeek) {
        llDateContainer.removeAllViews();
        SimpleDateFormat sdf = new SimpleDateFormat("EEE dd", Locale.getDefault());

        for (int i = 0; i < 7; i++) {
            Calendar day = (Calendar) startOfWeek.clone();
            day.add(Calendar.DAY_OF_MONTH, i);

            Chip chip = new Chip(this);
            chip.setText(sdf.format(day.getTime()));
            chip.setCheckable(true);

            chip.setOnClickListener(v -> weekView.goToDate(day));

            llDateContainer.addView(chip);
        }
    }


    @SuppressLint({"MissingInflatedId", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.timetable_week);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_container), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ánh xạ
        weekView = findViewById(R.id.weekView);
        monthCalendar = findViewById(R.id.monthCalendar);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        llDateContainer = findViewById(R.id.llDateContainer);
        btnAdd = findViewById(R.id.btnAdd);

        // nút thêm
        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(TimetableWeek.this, SubjectAddActivity.class);
            startActivity(intent);
        });

        // ds sự kiện mẫu
//        events.add(TimetableEvent.createEvent(1, "Toán", 2025, 11, 20, 8, 0, 9, 30, "#FF5733"));
//        events.add(TimetableEvent.createEvent(2, "Vật lý", 2025, 11, 5, 10, 0, 11, 30, "#33FF57"));
//        events.add(TimetableEvent.createEvent(3, "Hóa học", 2025, 11, 21, 13, 0, 14, 30, "#5733FF"));
//        events.add(TimetableEvent.createEvent(4, "Lịch sử", 2025, 11, 6, 15, 0, 16, 30, "#FF33A1"));

//        Calendar start1 = Calendar.getInstance();
//        start1.set(2025, Calendar.NOVEMBER, 20, 8, 0); // 20/11/2025 08:00
//        Calendar end1 = Calendar.getInstance();
//        end1.set(2025, Calendar.NOVEMBER, 20, 9, 30);  // 20/11/2025 09:30
//
//        WeekViewEntity.Event event1 = new WeekViewEntity.Event.Builder(start1, end1)
//                .setId(1)
//                .setTitle("Toán")
//                .setColor(Color.parseColor("#FF5733"))
//                .build();
//
//        events.add(event1);

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        ArrayList<String> semesters = dbHelper.getAllSemesterNames();
        String selectedSemester = semesters.get(0);
        ArrayList<Subject> subjects = dbHelper.getSubjectsBySemester(selectedSemester);

//        for (Subject s : subjects) {
//            Calendar start = Calendar.getInstance();
//            start.set(s.getYear(), s.getMonth(), s.getDay(), s.getStartHour(), s.getStartMinute());
//
//            Calendar end = Calendar.getInstance();
//            end.set(s.getYear(), s.getMonth(), s.getDay(), s.getEndHour(), s.getEndMinute());
//
//            WeekViewEntity.Event event = new WeekViewEntity.Event.Builder(start, end)
//                    .setId(s.getId())
//                    .setTitle(s.getName())
//                    .setColor(Color.parseColor(s.getColorHex())) // giả sử Subject lưu màu
//                    .build();
//
//            events.add(event);
//        }




        // hien thi weekview
        weekView.setNumberOfVisibleDays(7);
        weekView.setHourHeight(120);
        weekView.setColumnGap(2);
//        weekView.setTextSize(12);
        weekView.setEventTextSize(14);
        weekView.setShowNowLine(true);

        // Header: hiển thị Thứ + ngày
//        weekView.setDateTimeInterpreter(new DateTimeInterpreter() {
//            @Override
//            public String interpretDate(Calendar date) {
//                SimpleDateFormat weekdayNameFormat = new SimpleDateFormat("EEE", Locale.getDefault());
//                SimpleDateFormat dateFormat = new SimpleDateFormat("dd", Locale.getDefault());
//                return weekdayNameFormat.format(date.getTime()).toUpperCase() + "
//" + dateFormat.format(date.getTime());
//            }
//
//            @Override
//            public String interpretTime(int hour) {
//                Calendar calendar = Calendar.getInstance();
//                calendar.set(Calendar.HOUR_OF_DAY, hour);
//                calendar.set(Calendar.MINUTE, 0);
//                SimpleDateFormat timeFormat = new SimpleDateFormat("h a", Locale.getDefault());
//                return timeFormat.format(calendar.getTime());
//            }
//        });
        // hédae ngày giờ
        weekView.setDateFormatter(date -> {
            SimpleDateFormat weekdayNameFormat = new SimpleDateFormat("EEE", Locale.getDefault());
            SimpleDateFormat dayFormat = new SimpleDateFormat("dd", Locale.getDefault());
            return weekdayNameFormat.format(date.getTime()).toUpperCase() + " " + dayFormat.format(date.getTime());
        });

        weekView.setTimeFormatter(hour -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, 0);
            SimpleDateFormat timeFormat = new SimpleDateFormat("h a", Locale.getDefault());
            return timeFormat.format(calendar.getTime());
        });



        // thêm sự kiện vào WeekView bằng MonthChangeListener
//        weekView.setMonthChangeListener((newYear, newMonth) -> {
//            List<WeekViewEntity> monthEvents = new ArrayList<>();
//            for (WeekViewEntity e : events) {
//                Calendar start = e.getStartTime();
//                int eventYear = start.get(Calendar.YEAR);
//                int eventMonth = start.get(Calendar.MONTH); // 0-based
//                if (eventYear == newYear && eventMonth == newMonth - 1) {
//                    monthEvents.add(e);
//                }
//            }
//            return monthEvents;
//        });
        // load sự kiên bản mới
//        weekView.setWeekViewLoader(period -> {
//            // Trả về tất cả event tạm thời (lọc không cần thiết nếu chỉ muốn test)
//            List<WeekViewEntity> weekEvents = new ArrayList<>();
//            for (WeekViewEntity.Event e : events) {
//                weekEvents.add(e); // upcast Event -> WeekViewEntity
//            }
//            return weekEvents;
//        });






        // Đồng bộ lịch tháng, thanh hiển thị ngày, weekview
        monthCalendar.setOnDateChangedListener((widget, date, selected) -> {
            updateSelectedDate(date);
            goToDate(date);
            displayWeekChips(getStartOfWeek(date));
        });

        // Hiển thị ngày hiện tại
        CalendarDay today = CalendarDay.today();
        updateSelectedDate(today);
        goToDate(today);
        displayWeekChips(getStartOfWeek(today));

        // GestureDetector để vuốt lên/ xuống ẩn/hiện lịch tháng
        GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 50;
            private static final int SWIPE_VELOCITY_THRESHOLD = 50;

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                float diffY = e2.getY() - e1.getY();
                if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY < 0) hideMonthCalendar();
                    else showMonthCalendar();
                    return true;
                }
                return false;
            }
        });

        // gắn detector vào textView
        tvSelectedDate.setOnTouchListener((v, event) -> {
            boolean handled = gestureDetector.onTouchEvent(event);
            return handled || weekView.onTouchEvent(event);
        });
    }
}
