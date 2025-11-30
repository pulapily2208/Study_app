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
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEntity;
import com.alamkanak.weekview.WeekViewEntity.Event;
import com.alamkanak.weekview.DateTimeInterpreter;
import com.alamkanak.weekview.WeekViewEvent;
import com.example.study_app.R;
import com.example.study_app.data.DatabaseHelper;
import com.example.study_app.data.SubjectDao;
import com.example.study_app.data.TimetableDao;
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

    Button btnAdd;
    private String selectedDate = null; // lưu ngày được chọn (dạng yyyy-MM-dd)

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
        // 🆕 Lưu lại ngày để truyền qua trang Add (format chuẩn DB: yyyy-MM-dd)
        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        selectedDate = dbFormat.format(c.getTime());
    }

    private void goToDate(CalendarDay date) {
        Calendar c = Calendar.getInstance();
        c.set(date.getYear(), date.getMonth(), date.getDay());
        weekView.goToDate(c);  // API mới vẫn có goToDate(Calendar)
    }

    // Tính ngày Monday của tuần
//    private Calendar getStartOfWeek(CalendarDay date) {
//        Calendar cal = Calendar.getInstance();
//        cal.set(date.getYear(), date.getMonth(), date.getDay());
//        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
//        int diff = Calendar.MONDAY - dayOfWeek;
//        cal.add(Calendar.DAY_OF_MONTH, diff);
//        return cal;
//    }
    private Calendar getStartOfWeek(CalendarDay date) {
        Calendar cal = Calendar.getInstance();
        cal.set(date.getYear(), date.getMonth(), date.getDay());

        // Set giờ, phút, giây = 0 để chính xác
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int diff = (dayOfWeek + 5) % 7; // Chủ nhật → 6, Thứ hai → 0
        cal.add(Calendar.DAY_OF_MONTH, -diff);
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
            if (selectedDate == null) {
                Toast.makeText(TimetableWeek.this, "Hãy chọn ngày trước!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Tách year - month - day từ yyyy-MM-dd
            String[] parts = selectedDate.split("-");
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]) - 1; // convert về 0-11 như Calendar

            // lấy học kỳ
            TimetableDao timetableDao = new TimetableDao(new DatabaseHelper(this));
            Integer semesterId = timetableDao.getSemesterIdBySelectedDate(year, month);

            if (semesterId == null) {
                Toast.makeText(this, "Không tìm thấy học kỳ phù hợp!", Toast.LENGTH_SHORT).show();
                return;
            }

            // lấy tên học kỳ từ semesterId
            String semesterName = timetableDao.getSemesterNameById(semesterId);

            if (semesterName == null) {
                Toast.makeText(this, "Không thể lấy tên học kỳ!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Chuyển sang trang thêm môn
            Intent intent = new Intent(TimetableWeek.this, SubjectAddActivity.class);
            intent.putExtra("selectedDate", selectedDate);
            intent.putExtra("semesterId", semesterId);
            intent.putExtra("SEMESTER_NAME", semesterName);
            startActivity(intent);
        });



        DatabaseHelper dbHelper = new DatabaseHelper(this);
        TimetableDao timetableDao = new TimetableDao(dbHelper);
        List<Subject> subjects = timetableDao.getAllSubjects();
        // Convert sang WeekViewEntity
        List<WeekViewEntity> events = TimetableEvent.convertSafe(subjects);

        MyWeekViewAdapter adapter = new MyWeekViewAdapter(events);
        weekView.setAdapter(adapter);

        // Load vào wv
//        weekView.setAdapter((WeekView.Adapter<?>) events);
//        weekView.setEventLoader(period -> events);
//        weekView.setLoadMoreHandler(period -> events);
//        List<WeekViewEntity> events = TimetableEvent.convert(subjects);
        // load vaof wweekview

//        weekView.setOnLoadMoreListener((startDate, endDate) -> {
//            return events; // events là list<WeekViewEntity>
//        });
//        weekView.setWeekViewLoader((start, end) -> events);
//        weekView.setMonthChangeListener((start, end) -> events);

//        weekView.setEventLoader(new EventLoader() {
//            @Override
//            public List<? extends WeekViewEntity> onLoad(DateTimeRange range) {
//                // Trả về danh sách sự kiện nằm trong khoảng range
//                return events;
//            }
//        });







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
//                return weekdayNameFormat.format(date.getTime()).toUpperCase() + " " + dateFormat.format(date.getTime());
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
        // header ngày giờ
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
//            List<WeekViewEntity> weekEvents = new ArrayList<>();
//            weekEvents.addAll(events); // copy toàn bộ events
//            return weekEvents;
//        });
//        weekView.setWeekViewLoader(period -> {
//            return new ArrayList<>(events); // copy tránh lỗi tham chiếu
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
