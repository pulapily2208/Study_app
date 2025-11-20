package com.example.study_app.ui.Timetable;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;
import com.example.study_app.MainActivity;
import com.example.study_app.R;
import com.example.study_app.ui.Subject.SubjectListActivity;
import com.google.android.material.chip.Chip;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.alamkanak.weekview.DateTimeInterpreter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Calendar;

public class TimetableWeek extends AppCompatActivity {
    // khai báo
    WeekView weekView;
//    FrameLayout weekContainer;

    MaterialCalendarView monthCalendar;
    TextView tvSelectedDate;
    LinearLayout llDateContainer;
    List<WeekViewEvent> events = new ArrayList<>();
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
        weekView.goToDate(c);
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

    // tạo dãy chip ngày 1 đến 31
    private void displayDateChips(Calendar base) {
        llDateContainer.removeAllViews();

        int maxDay = base.getActualMaximum(Calendar.DAY_OF_MONTH);

        for (int d = 1; d <= maxDay; d++) {
            Chip chip = new Chip(this);
            chip.setText(String.valueOf(d));
            chip.setCheckable(true);
            chip.setPadding(16, 8, 16, 8);

            int finalD = d;
            chip.setOnClickListener(v -> {
                CalendarDay day = CalendarDay.from(
                        base.get(Calendar.YEAR),
                        base.get(Calendar.MONTH),
                        finalD
                );
                updateSelectedDate(day);
                goToDate(day);
            });

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

        // anh xa
        weekView = findViewById(R.id.weekView);
//        weekContainer = findViewById(R.id.weekContainer);
        monthCalendar = findViewById(R.id.monthCalendar);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        llDateContainer = findViewById(R.id.llDateContainer);


        // nut them
        btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TimetableWeek.this, SubjectListActivity.class);
                startActivity(intent);
            }
        });



        // ds sk mẫu
        events.add(TimetableEvent.createEvent(1, "Toán", 2025, 11, 20, 8, 0, 9, 30, "#FF5733"));
        events.add(TimetableEvent.createEvent(2, "Vật lý", 2025, 11, 5, 10, 0, 11, 30, "#33FF57"));
        events.add(TimetableEvent.createEvent(3, "Hóa học", 2025, 11, 21, 13, 0, 14, 30, "#5733FF"));
        events.add(TimetableEvent.createEvent(4, "Lịch sử", 2025, 11, 6, 15, 0, 16, 30, "#FF33A1"));
        // Thêm các sự kiện vào WeekView
//        weekView.addEvents(events);
//        weekView.setEventPadding(events);
        // Set sự kiện bằng MonthChangeListener cho weekview .6
//        weekView.setMonthChangeListener((year, month) -> {
//            List<WeekViewEvent> monthEvents = new ArrayList<>();
//            for (WeekViewEvent e : events) {
//                if (e.getStartTime().get(Calendar.YEAR) == year &&
//                        e.getStartTime().get(Calendar.MONTH) == month - 1) {
//                    monthEvents.add(e);
//                }
//            }
//            return monthEvents;
//        });

        weekView.setNumberOfVisibleDays(7); // hiển thị 7 ngày
        weekView.setHourHeight(120);        // chiều cao mỗi giờ
        weekView.setColumnGap(2);
        weekView.setTextSize(12);
        weekView.setEventTextSize(14);
        weekView.setShowNowLine(true);      // hiển thị giờ hiện tại


        // Header: hiển thị Thứ + ngày
        weekView.setDateTimeInterpreter(new DateTimeInterpreter() {
            @Override
            public String interpretDate(Calendar date) {
                SimpleDateFormat weekdayNameFormat = new SimpleDateFormat("EEE", Locale.getDefault());
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd", Locale.getDefault());
                String weekday = weekdayNameFormat.format(date.getTime());
                String day = dateFormat.format(date.getTime());
                return weekday.toUpperCase() + "\n" + day;
            }

            @Override
            public String interpretTime(int hour) {
                // Định dạng thời gian ở cột bên trái (VD: 9 AM, 10 AM)
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, 0);

                SimpleDateFormat timeFormat = new SimpleDateFormat("h a", Locale.getDefault());
                return timeFormat.format(calendar.getTime());
            }
        });



        // --- Thêm sự kiện vào WeekView bằng MonthChangeListener ---
        weekView.setMonthChangeListener((newYear, newMonth) -> {
            List<WeekViewEvent> monthEvents = new ArrayList<>();
            for (WeekViewEvent e : events) {
                Calendar start = e.getStartTime();
                int eventYear = start.get(Calendar.YEAR);
                int eventMonth = start.get(Calendar.MONTH); // 0-based
                if (eventYear == newYear && eventMonth == newMonth - 1) {
                    monthEvents.add(e);
                }
            }
            return monthEvents;
        });



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
                    if (diffY < 0) {
                        hideMonthCalendar(); // vuốt lên → ẩn lịch tháng
                    } else {
                        showMonthCalendar(); // vuốt xuống → hiện lịch tháng
                    }
                    return true;
                }
                return false;
            }
        });

        // Gắn detector vào WeekView
//        weekView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));

//        weekContainer.setOnTouchListener((v, event) -> {
//            // Luôn để GestureDetector thử xử lý sự kiện
//            boolean isFling = gestureDetector.onTouchEvent(event);
//
//            // Nếu không phải là cử chỉ vuốt lên/xuống (onFling)
//            if (!isFling) {
//                // Thì chủ động "ném" sự kiện này cho WeekView xử lý
//                // để nó có thể cuộn ngang.
//                weekView.onTouchEvent(event);
//            }
//
//            // Return true để báo rằng sự kiện đã được xử lý tại đây
//            // và không truyền đi đâu nữa, tránh xung đột.
//            return true;
//        });

        // gan vao main container de vuot tu vi tri bat ky
        tvSelectedDate.setOnTouchListener((v, event) -> {
            // 1. Để GestureDetector phân tích sự kiện
            boolean handled = gestureDetector.onTouchEvent(event);

            // 2. Nếu GestureDetector đã xử lý sự kiện onFling (vuốt) thì return true
            if (handled) {
                return true;
            }

            // 3. Nếu không phải là cử chỉ vuốt, hãy "chuyển tiếp" sự kiện chạm này
            // xuống cho WeekView ở bên dưới để nó có thể cuộn ngang.
            return weekView.onTouchEvent(event);
        });

    }
}
