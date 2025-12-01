package com.example.study_app.ui.Deadline;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.study_app.R;
import com.example.study_app.data.DeadlineDao;
import com.example.study_app.ui.Deadline.Adapters.AdapterDeadline;
import com.example.study_app.ui.Deadline.Adapters.AdapterMonHoc;
import com.example.study_app.ui.Deadline.Adapters.AdapterWeek;
import com.example.study_app.ui.Deadline.Models.Deadline;
import com.example.study_app.ui.Subject.Model.Subject;

import java.util.ArrayList;
import java.util.List;

public class MainDeadLine extends AppCompatActivity {

    private RecyclerView rvToday, rvAll;
    private Toolbar toolbar;
    private TextView tvTodayEmpty, tvAllEmpty;
    private DeadlineDao dbHelper;
    private DeadlineNotificationManager notificationManager;

    private AdapterDeadline todayAdapter;
//    private AdapterMonHoc todayAdapter;
    private AdapterMonHoc allAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.deadline_main);

        // --- Init Views ---
        toolbar = findViewById(R.id.toolbar);
        rvToday = findViewById(R.id.rvTodayDeadlines);
        rvAll = findViewById(R.id.rvAllDeadlines);
        tvTodayEmpty = findViewById(R.id.tvTodayEmpty);
        tvAllEmpty = findViewById(R.id.tvAllEmpty);

        dbHelper = new DeadlineDao(this);
        notificationManager = new DeadlineNotificationManager(this);

        // --- Toolbar Setup ---
        toolbar.setTitle("Tổng quan Deadline");
        toolbar.setNavigationOnClickListener(v -> finish());

        // --- RecyclerViews Setup ---
        rvToday.setLayoutManager(new LinearLayoutManager(this));
        rvAll.setLayoutManager(new LinearLayoutManager(this));

        // --- Setup Adapters & Load Data ---
        setupAdapters();
        loadData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void setupAdapters() {
        AdapterWeek.OnDeadlineInteractionListener listener = new AdapterWeek.OnDeadlineInteractionListener() {
            @Override
            public void onDeadlineClick(Deadline deadline) {
//                Toast.makeText(MainDeadLine.this, "Bạn vừa chọn: " + deadline.getTieuDe(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onEditDeadline(Deadline deadline) {
                Toast.makeText(MainDeadLine.this, "Vui lòng vào môn học để chỉnh sửa", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDeleteDeadline(Deadline deadline) {
                dbHelper.deleteDeadline(deadline.getId());
                notificationManager.cancelNotification(deadline.getId());
                loadData();
                Toast.makeText(MainDeadLine.this, "Đã xóa deadline", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStateChanged(Deadline deadline, boolean isCompleted) {
                deadline.setCompleted(isCompleted);
                dbHelper.updateDeadline(deadline);
                if(isCompleted) {
                    notificationManager.cancelNotification(deadline.getId());
                }
                loadData();
            }
        };

        todayAdapter = new AdapterDeadline(this, new ArrayList<>());
        todayAdapter.setOnDeadlineInteractionListener(listener);
        rvToday.setAdapter(todayAdapter);

        allAdapter = new AdapterMonHoc(this, new ArrayList<>());
        allAdapter.setOnDeadlineInteractionListener(listener);
        rvAll.setAdapter(allAdapter);
    }
    private void loadData() {

        // ==== Hôm nay ====
        List<Deadline> todayDeadlines = dbHelper.getTodaysDeadlines();

        if (todayDeadlines.isEmpty()) {
            rvToday.setVisibility(View.GONE);
            tvTodayEmpty.setVisibility(View.VISIBLE);
        } else {
            rvToday.setVisibility(View.VISIBLE);
            tvTodayEmpty.setVisibility(View.GONE);
            for (Deadline d : todayDeadlines) {
                Subject subject = dbHelper.getSubjectByMaHp(d.getMaHp());
                if (subject != null) {
                    d.setTenMon(subject.tenHp);
                }
            }
            todayAdapter.updateData(todayDeadlines);
        }

        // ==== Tất cả deadline ====
        List<Subject> allSubjects = dbHelper.getSubjectsWithDeadlines();

        if (allSubjects.isEmpty()) {
            rvAll.setVisibility(View.GONE);
            tvAllEmpty.setVisibility(View.VISIBLE);
        } else {
            rvAll.setVisibility(View.VISIBLE);
            tvAllEmpty.setVisibility(View.GONE);
            allAdapter.updateData(allSubjects);
        }
    }

}
