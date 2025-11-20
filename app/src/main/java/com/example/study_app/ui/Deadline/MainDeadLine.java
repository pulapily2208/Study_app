package com.example.study_app.ui.Deadline;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.study_app.R;
import com.example.study_app.data.DatabaseHelper;
import com.example.study_app.ui.Deadline.Adapters.AdapterWeek;
import com.example.study_app.ui.Deadline.Models.Deadline;
import com.example.study_app.ui.Deadline.Models.Week;
import com.example.study_app.ui.Subject.Model.Subject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MainDeadLine extends AppCompatActivity {

    ListView lvDeadlines;
    TextView tvSubjectTitle;
    ArrayList<Deadline> deadlineList;
    String subjectMaHp = null;
    DatabaseHelper dbHelper;

    private AdapterWeek adapterWeek;
    private ArrayList<Week> weeks; // data for adapter
    private static final int REQ_ADD_DEADLINE = 1001;
    private int lastRequestedWeekIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.deadline_main);

        // Initialize views
        lvDeadlines = findViewById(R.id.lvItemTuan);
        tvSubjectTitle = findViewById(R.id.tvSubjectTitle);
        dbHelper = new DatabaseHelper(this);

        // Get data from Intent
        Intent intent = getIntent();
        subjectMaHp = intent.getStringExtra("SUBJECT_MA_HP");
        String subjectTenHp = intent.getStringExtra("SUBJECT_TEN_HP");
        int subjectWeeksExtra = intent.getIntExtra("SUBJECT_WEEKS", 0);

        // Update the title
        if (subjectTenHp != null) {
            tvSubjectTitle.setText(subjectTenHp);
        } else {
            tvSubjectTitle.setText("Deadlines"); // Fallback title
        }

        // --- Load real data and show with AdapterWeek ---
        loadDeadlines(subjectWeeksExtra);

        // Handle the back button
        findViewById(R.id.btnQuaylai).setOnClickListener(v -> finish());
    }

    private void loadDeadlines(int subjectWeeksExtra) {
        if (subjectMaHp == null) return;

        // Get subject to read ngayBatDau and soTuan fallback
        Subject subject = dbHelper.getSubjectByMaHp(subjectMaHp);
        int subjectWeeks = subjectWeeksExtra;
        Date subjectStart = null;
        if (subject != null) {
            if (subjectWeeks <= 0 && subject.soTuan > 0) {
                subjectWeeks = subject.soTuan;
            }
            subjectStart = subject.ngayBatDau;
        }

        // Get deadlines from DB
        deadlineList = dbHelper.getDeadlinesByMaHp(subjectMaHp);

        // Group deadlines by computed week number
        Map<Integer, List<Deadline>> weekMap = new HashMap<>();
        int maxWeekFound = 0;
        if (deadlineList != null) {
            for (Deadline d : deadlineList) {
                Date due = d.getNgayKetThuc();
                int week = 1; // default to 1 if cannot compute
                if (due != null && subjectStart != null) {
                    long diffMillis = due.getTime() - subjectStart.getTime();
                    long diffDays = TimeUnit.MILLISECONDS.toDays(diffMillis);
                    week = (int) (diffDays / 7) + 1;
                    if (week < 1) week = 1;
                }
                if (subjectWeeks > 0 && week > subjectWeeks) week = subjectWeeks;

                if (!weekMap.containsKey(week)) weekMap.put(week, new ArrayList<>());
                weekMap.get(week).add(d);
                if (week > maxWeekFound) maxWeekFound = week;
            }
        }

        int totalWeeksToShow = subjectWeeks > 0 ? subjectWeeks : Math.max(1, maxWeekFound);

        // Build Weeks list
        weeks = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        for (int w = 1; w <= totalWeeksToShow; w++) {
            Week weekObj = new Week("Tuần " + w);
            List<Deadline> list = weekMap.get(w);
            if (list != null) weekObj.getDeadlines().addAll(list);
            weeks.add(weekObj);
        }

        // Create AdapterWeek (use your layout R.layout.deadline_item_tuan)
        adapterWeek = new AdapterWeek(this, R.layout.deadline_item_tuan, weeks);

        // Khi người click thêm trong 1 tuần, mở InputDeadlineActivity
        adapterWeek.setOnAddDeadlineListener(weekIndex -> {
            lastRequestedWeekIndex = weekIndex;
            Intent i = new Intent(MainDeadLine.this, InputDeadlineActivity.class);
            i.putExtra("weekIndex", weekIndex);
            // truyền mã môn để InputDeadlineActivity gán maHp cho Deadline mới
            i.putExtra("SUBJECT_MA_HP", subjectMaHp);
            startActivityForResult(i, REQ_ADD_DEADLINE);
        });

        lvDeadlines.setAdapter(adapterWeek);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_ADD_DEADLINE && resultCode == RESULT_OK && data != null) {
            Deadline newDl = (Deadline) data.getSerializableExtra(InputDeadlineActivity.KEY_TAI_KHOAN);
            int weekIndex = data.getIntExtra("weekIndex", lastRequestedWeekIndex);
            if (newDl != null) {
                // Lưu vào DB (addDeadline nên trả về id)
                long id = dbHelper.addDeadline(newDl);
                if (id != -1) {
                    newDl.setMaDl((int) id);
                }
                // Thêm vào adapter tuần tương ứng (AdapterWeek sẽ notify con ListView)
                if (weekIndex >= 0 && weekIndex < weeks.size()) {
                    adapterWeek.addDeadlineToWeek(weekIndex, newDl);
                }
            }
        }
    }
}