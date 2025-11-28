package com.example.study_app.ui.Deadline;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.study_app.R;
import com.example.study_app.data.DatabaseHelper;
import com.example.study_app.ui.Deadline.Adapters.AdapterWeek;
import com.example.study_app.ui.Deadline.Models.Deadline;
import com.example.study_app.ui.Deadline.Models.Week;
import com.example.study_app.ui.Subject.Model.Subject;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class MainDeadLine extends AppCompatActivity {

    private RecyclerView lvWeeks;
    private Toolbar toolbar;
    private FloatingActionButton fabAddDeadline;

    private DatabaseHelper dbHelper;
    private AdapterWeek adapterWeek;

    private String subjectMaHp;
    private Subject currentSubject;

    private ActivityResultLauncher<Intent> deadlineLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.deadline_main);

        // --- Init Views ---
        toolbar = findViewById(R.id.toolbar);
        lvWeeks = findViewById(R.id.lvItemTuan);
        fabAddDeadline = findViewById(R.id.fab_add_deadline);

        dbHelper = new DatabaseHelper(this);

        // --- Get Intent Data ---
        Intent intent = getIntent();
        subjectMaHp = intent.getStringExtra("SUBJECT_MA_HP");

        if (subjectMaHp == null || subjectMaHp.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không có thông tin môn học.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // --- Load Subject & Setup UI ---
        currentSubject = dbHelper.getSubjectByMaHp(subjectMaHp);
        if (currentSubject == null) {
            Toast.makeText(this, "Lỗi: Không thể tải thông tin môn học.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // --- Toolbar Setup ---
        toolbar.setTitle(currentSubject.tenHp != null ? currentSubject.tenHp : "Deadlines");
        toolbar.setNavigationOnClickListener(v -> finish());

        // --- RecyclerView Setup ---
        lvWeeks.setLayoutManager(new LinearLayoutManager(this));

        // --- Initialize Adapter ---
        adapterWeek = new AdapterWeek(this, new ArrayList<>(), subjectMaHp, currentSubject.ngayBatDau);
        lvWeeks.setAdapter(adapterWeek);

        // --- Setup Listeners and Load Data ---
        setupDeadlineLauncher();
        loadAndDisplayWeeks();
        setupAdapterListeners();

        fabAddDeadline.setOnClickListener(v -> {
            int currentWeek = getCurrentWeekIndex();
            onAddDeadline(currentWeek);
        });
    }

    private void loadAndDisplayWeeks() {
        if (currentSubject == null) return;

        ArrayList<Week> weekList = new ArrayList<>();
        for (int i = 0; i < currentSubject.soTuan; i++) {
            weekList.add(new Week("Tuần " + (i + 1)));
        }

        adapterWeek.setWeeks(weekList);

        int currentWeek = getCurrentWeekIndex();
        if (currentWeek >= 0 && currentWeek < adapterWeek.getItemCount()) {
            lvWeeks.post(() -> lvWeeks.smoothScrollToPosition(currentWeek));
        }
    }

    private void setupDeadlineLauncher() {
        deadlineLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Serializable extra = result.getData().getSerializableExtra(InputDeadlineActivity.KEY_TAI_KHOAN);
                        if (extra instanceof Deadline) {
                            Deadline returnedDeadline = (Deadline) extra;
                            boolean isUpdate = returnedDeadline.getId() > 0;

                            long dbResult;
                            if (isUpdate) {
                                dbResult = dbHelper.updateDeadline(returnedDeadline);
                            } else {
                                dbResult = dbHelper.addDeadline(returnedDeadline, subjectMaHp);
                            }

                            if (dbResult != -1) {
                                Toast.makeText(this, isUpdate ? "Đã cập nhật deadline" : "Đã thêm deadline", Toast.LENGTH_SHORT).show();
                                // Instead of reloading everything, just notify the specific week's adapter
                                int affectedWeek = result.getData().getIntExtra("weekIndex", -1);
                                int originalWeek = result.getData().getIntExtra("originalWeekIndex", -1);

                                if (affectedWeek != -1) {
                                    adapterWeek.notifyItemChanged(affectedWeek);
                                }
                                if (originalWeek != -1 && originalWeek != affectedWeek) {
                                    adapterWeek.notifyItemChanged(originalWeek);
                                }

                                if (affectedWeek != -1) {
                                   lvWeeks.post(() -> lvWeeks.smoothScrollToPosition(affectedWeek));
                                }

                            } else {
                                Toast.makeText(this, "Thao tác với database thất bại!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
        );
    }

    private void setupAdapterListeners() {
        adapterWeek.setOnAddDeadlineListener(this::onAddDeadline);

        adapterWeek.setOnDeadlineInteractionListener(new AdapterWeek.OnDeadlineInteractionListener() {
            @Override
            public void onDeadlineClick(Deadline deadline) {
                new AlertDialog.Builder(MainDeadLine.this)
                        .setTitle(deadline.getTieuDe())
                        .setMessage("Ghi chú: " + deadline.getNoiDung())
                        .setPositiveButton("Đóng", null)
                        .show();
            }

            @Override
            public void onEditDeadline(Deadline deadline) {
                Intent editIntent = new Intent(MainDeadLine.this, InputDeadlineActivity.class);
                int weekIndex = -1;
                if(currentSubject.ngayBatDau != null){
                     long diffMillis = deadline.getNgayBatDau().getTime() - currentSubject.ngayBatDau.getTime();
                     long days = TimeUnit.MILLISECONDS.toDays(diffMillis);
                     weekIndex = (int) (days/7);
                }

                editIntent.putExtra("weekIndex", weekIndex);
                editIntent.putExtra(InputDeadlineActivity.EDIT_DEADLINE, deadline);
                editIntent.putExtra(InputDeadlineActivity.SUBJECT_MA_HP, subjectMaHp);
                if (currentSubject != null) {
                    editIntent.putExtra(InputDeadlineActivity.SUBJECT_START_DATE, currentSubject.ngayBatDau.getTime());
                }
                deadlineLauncher.launch(editIntent);
            }

            @Override
            public void onDeleteDeadline(Deadline deadline) {
                Toast.makeText(MainDeadLine.this, "Đã xóa deadline", Toast.LENGTH_SHORT).show();
                // The adapter handles the removal, we just need to refresh the view.
                 adapterWeek.notifyDataSetChanged();
            }

            @Override
            public void onStateChanged(Deadline deadline, boolean isCompleted) {
                 adapterWeek.notifyDataSetChanged();
            }
        });
    }

    private void onAddDeadline(int weekIndex) {
        Intent intent = new Intent(MainDeadLine.this, InputDeadlineActivity.class);
        intent.putExtra("weekIndex", weekIndex);
        intent.putExtra(InputDeadlineActivity.SUBJECT_MA_HP, subjectMaHp);
        if (currentSubject != null && currentSubject.ngayBatDau != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(currentSubject.ngayBatDau);
            cal.add(Calendar.WEEK_OF_YEAR, weekIndex);
            intent.putExtra(InputDeadlineActivity.WEEK_START_DATE, cal.getTimeInMillis());
            intent.putExtra(InputDeadlineActivity.SUBJECT_START_DATE, currentSubject.ngayBatDau.getTime());
        }
        deadlineLauncher.launch(intent);
    }

    private int getCurrentWeekIndex() {
        if (currentSubject == null || currentSubject.ngayBatDau == null) {
            return 0;
        }
        long diffMillis = new Date().getTime() - currentSubject.ngayBatDau.getTime();
        if (diffMillis < 0) {
            return 0; // Course hasn't started
        }
        long diffDays = TimeUnit.MILLISECONDS.toDays(diffMillis);
        return (int) (diffDays / 7);
    }
}
