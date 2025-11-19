package com.example.study_app.ui.Timetable;

import android.annotation.SuppressLint;
import android.icu.util.Calendar;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;
import com.example.study_app.R;

import java.util.ArrayList;
import java.util.List;

public class TimetableWeek extends AppCompatActivity {
    // khai báo
    WeekView weekView;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.timetable_week);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // anh xa
        weekView = findViewById(R.id.weekView);
        // ds sk
        List<WeekViewEvent> events = new ArrayList<>();

        // ví dụ thêm các môn học
        events.add(TimetableEvent.createEvent(
                1, "Toán", 2025, 11, 20, 8, 0, 9, 30
        ));
        events.add(TimetableEvent.createEvent(
                2, "Vật lý", 2025, 11, 20, 10, 0, 11, 30
        ));
        events.add(TimetableEvent.createEvent(
                3, "Hóa học", 2025, 11, 21, 13, 0, 14, 30
        ));


        // 3. Set sự kiện vào WeekView .6
//        weekView.setWeekViewLoader(new WeekView.WeekViewLoader() {
//            @Override
//            public List<? extends WeekViewEvent> onLoad(int year, int month) {
//                return events; // trả về sự kiện cho tháng hiện tại
//            }
//        });

        // Tuỳ chỉnh WeekView
        weekView.setNumberOfVisibleDays(7); // hiển thị 7 ngày
        weekView.setHourHeight(100);        // chiều cao mỗi giờ
        weekView.setShowNowLine(true);      // hiển thị giờ hiện tại

        // Set sự kiện bằng MonthChangeListener .7
        weekView.setMonthChangeListener((year, month) -> {
            List<WeekViewEvent> monthEvents = new ArrayList<>();
            for (WeekViewEvent e : events) {
                if (e.getStartTime().get(Calendar.YEAR) == year &&
                        e.getStartTime().get(Calendar.MONTH) == month - 1) {
                    monthEvents.add(e);
                }
            }
            return monthEvents;
        });

    }
}
