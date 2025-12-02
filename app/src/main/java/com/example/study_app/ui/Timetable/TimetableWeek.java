package com.example.study_app.ui.Timetable;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
import com.example.study_app.ui.Curriculum.CurriculumActivity;
import com.example.study_app.ui.Deadline.MainDeadLine;
import com.example.study_app.ui.Notes.NotesActivity;
import com.example.study_app.ui.Subject.Model.Subject;
import com.example.study_app.ui.Subject.SubjectAddActivity;
import com.example.study_app.ui.Subject.SubjectListActivity;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.jakewharton.threetenabp.AndroidThreeTen;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

// Removed adapter-specific imports (library version lacks LoadParams / SimpleAdapter usage)
import java.util.Locale;

public class TimetableWeek extends AppCompatActivity {
    LinearLayout btnNote, btnDeadLine, btnSubject, btnCurriculum, btnTimetable, btnKetQuaHocTap;
    ImageView btnNotifyAll;
    private boolean isNotificationOn = false;


    private WeekView weekView;
    private MaterialCalendarView monthCalendar;
    private TextView tvSelectedDate;
    private LinearLayout llDateContainer;
    private FloatingActionButton btnAdd;

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
        btnNotifyAll = findViewById(R.id.btnNotifyAll);
        // load trang thai tb cu
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        isNotificationOn = prefs.getBoolean("notifOn", false);
        btnNotifyAll.setImageResource(isNotificationOn ? R.drawable.ic_bell_filled : R.drawable.ic_bell_outline);

        btnNotifyAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] options = {"Trước giờ học 5 phút", "Trước giờ học 15 phút", "Trước giờ học 30 phút", "Trước giờ học 1 giờ"};

                if (!isNotificationOn) {
                    new androidx.appcompat.app.AlertDialog.Builder(TimetableWeek.this)
                            .setTitle("Chọn thời gian nhắc")
                            .setItems(options, (dialog, which) -> {
                                String selectedReminder = options[which];

                                TimetableDao dao = new TimetableDao(new DatabaseHelper(TimetableWeek.this));
                                int userId = UserSession.getCurrentUserId(TimetableWeek.this);
                                List<Subject> subjects = dao.getSubjectsForTimetable(userId);
                                SubjectNotificationManager manager = new SubjectNotificationManager(TimetableWeek.this);

                                for (Subject s : subjects) {
                                    manager.scheduleWeeklyNotification(s, selectedReminder);
                                }

                                btnNotifyAll.setImageResource(R.drawable.ic_bell_filled);
                                isNotificationOn = true;
                                Toast.makeText(TimetableWeek.this, "Đã bật thông báo: " + selectedReminder, Toast.LENGTH_SHORT).show();

                                SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
                                prefs.edit().putBoolean("notifOn", true).apply();
                            })
                            .show();
                } else {
                    TimetableDao dao = new TimetableDao(new DatabaseHelper(TimetableWeek.this));
                    int userId = UserSession.getCurrentUserId(TimetableWeek.this);
                    List<Subject> subjects = dao.getSubjectsForTimetable(userId);
                    SubjectNotificationManager manager = new SubjectNotificationManager(TimetableWeek.this);

                    for (Subject s : subjects) {
                        manager.cancelNotification(s); // hủy thông báo
                    }

                    btnNotifyAll.setImageResource(R.drawable.ic_bell_outline);
                    isNotificationOn = false;
                    Toast.makeText(TimetableWeek.this, "Đã tắt tất cả thông báo", Toast.LENGTH_SHORT).show();

                    SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
                    prefs.edit().putBoolean("notifOn", false).apply();
                }
            }
        });

        setupViews();
        setupInteractions();
        intentMenu();

        CalendarDay today = CalendarDay.today();
        monthCalendar.setSelectedDate(today);
        updateSelectedDate(today);
        goToDate(today);
        displayWeekChips(getStartOfWeek(today));

        // Set navbar active state to Timetable
        setActiveNavbarItem(R.id.btnTimetable);
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
        TimetableDao dao = new TimetableDao(new DatabaseHelper(this));
        int userId = UserSession.getCurrentUserId(this);
        List<Subject> subjects = dao.getSubjectsForTimetable(userId);

        Log.d("DEBUG_Timetable", "Subjects returned: " + subjects.size());
        for (Subject s : subjects) {
            Log.d("DEBUG_Timetable", "Subject: " + s.getTenHp()
                    + " | Date=" + s.getNgayBatDau()
                    + " | Start=" + s.getGioBatDau()
                    + " | End=" + s.getGioKetThuc());
        }
        Log.d("DEBUG_Timetable", "userId = " + userId);

        if (subjects == null) {
            Log.e("DEBUG_Timetable", "subjects == NULL !!!");
        } else {
            Log.d("DEBUG_Timetable", "subjects size = " + subjects.size());
        }

        for (int i = 0; i < subjects.size(); i++) {
            Subject s = subjects.get(i);
            Log.d("DEBUG_Timetable", "[" + i + "] tenHp=" + s.getTenHp());
        }

        List<WeekViewEntity.Event<Subject>> events = TimetableEvent.convertSubjectsToEvents(subjects);
//        weekView.setAdapter(new MyWeekViewAdapter(new ArrayList<>(events)));
        // Set adapter inline, không cần tạo class riêng
        weekView.setAdapter(new WeekView.SimpleAdapter<WeekViewEntity.Event<Subject>>() {
            private List<? extends WeekViewEntity> onLoad() {
                return events; // Trả về danh sách sự kiện
            }
        });
        Log.d("TimetableWeek", "Loaded " + events.size() + " events (adapter reset)");
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

        Calendar today = Calendar.getInstance();

        for (int i = 0; i < 7; i++) {
            Calendar day = (Calendar) startOfWeek.clone();
            day.add(Calendar.DAY_OF_MONTH, i);

            Chip chip = new Chip(this);
            chip.setText(sdf.format(day.getTime()));
            chip.setCheckable(true);

            if (selectedDate != null) {
                SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String dayStr = dbFormat.format(day.getTime());
                if (dayStr.equals(selectedDate)) {
                    chip.setChecked(true);
                }
            }

            // bỏ chọn chip cũ
            chip.setOnClickListener(v -> {
                for (int j = 0; j < llDateContainer.getChildCount(); j++) {
                    View child = llDateContainer.getChildAt(j);
                    if (child instanceof Chip) {
                        ((Chip) child).setChecked(false);
                    }
                }
                chip.setChecked(true);

                // Đồng bộ TextView
                SimpleDateFormat sdfFull = new SimpleDateFormat("EEEE, dd MMM, yyyy", Locale.ENGLISH);
                tvSelectedDate.setText(sdfFull.format(day.getTime()));

                // Lưu ngày để truyền Add
                SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                selectedDate = dbFormat.format(day.getTime());

                // Đồng bộ WeekView
                weekView.goToDate(day);
            });

            llDateContainer.addView(chip);
        }
    }
    // Set alaem 1 môn
    // NotiAll
//    private void scheduleAllSubjects() {
//        TimetableDao dao = new TimetableDao(new DatabaseHelper(this));
//        int userId = UserSession.getCurrentUserId(this);
//
//        List<Subject> subjects = dao.getSubjectsForTimetable(userId);
//
//        for (Subject s : subjects) {
//            scheduleSubjectNotification(s);
//        }
//
//        Toast.makeText(this, "Đã bật thông báo cho tất cả môn học!", Toast.LENGTH_SHORT).show();
//    }


    public void intentMenu() {
        btnDeadLine = findViewById(R.id.btnDeadLine);
        btnNote = findViewById(R.id.btnNote);
        btnSubject = findViewById(R.id.btnSubject);
        btnCurriculum = findViewById(R.id.btnCurriculum);
        btnTimetable = findViewById(R.id.btnTimetable);
        btnKetQuaHocTap = findViewById(R.id.btnKetQuaHocTap);

        // Default active state for this screen
        setActiveNavbarItem(R.id.btnTimetable);

        // DEADLINE
        btnDeadLine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TimetableWeek.this, MainDeadLine.class);
                startActivity(intent);
            }
        });

        // SUBJECT
        btnSubject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TimetableWeek.this, SubjectListActivity.class);
                startActivity(intent);
            }
        });

        // NOTE
        btnNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TimetableWeek.this, NotesActivity.class);
                startActivity(intent);
            }
        });

        // CURRICULUM
        btnCurriculum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TimetableWeek.this, CurriculumActivity.class);
                startActivity(intent);
            }
        });

        // TIMETABLE
        btnTimetable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Already on Timetable; keep active highlight
                setActiveNavbarItem(R.id.btnTimetable);
            }
        });

        // KET QUA HOC TAP (Scores)
        btnKetQuaHocTap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TimetableWeek.this, com.example.study_app.ui.Score.InputScoreActivity.class);
                startActivity(intent);
            }
        });

    }

    private void setActiveNavbarItem(int activeId) {
        int[] ids = new int[] { R.id.btnSubject, R.id.btnNote, R.id.btnDeadLine, R.id.btnTimetable,
                R.id.btnKetQuaHocTap, R.id.btnCurriculum };
        for (int id : ids) {
            View v = findViewById(id);
            if (v != null)
                v.setSelected(id == activeId);
        }
    }
}
