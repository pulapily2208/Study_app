package com.example.study_app.ui.Deadline;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.study_app.R;
import com.example.study_app.data.DatabaseHelper;
import com.example.study_app.ui.Deadline.Adapters.AdapterWeek;
import com.example.study_app.ui.Deadline.Models.Deadline;
import com.example.study_app.ui.Deadline.Models.Week;
import com.example.study_app.ui.Subject.Model.Subject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MainDeadLine extends AppCompatActivity {

    private ListView lvWeeks;
    private TextView tvSubjectTitle;
    private Button btnBack; // Sửa lỗi: Đổi từ ImageView sang Button

    private DatabaseHelper dbHelper;
    private AdapterWeek adapterWeek;
    private ArrayList<Week> weekList = new ArrayList<>();

    private String subjectMaHp;
    private ActivityResultLauncher<Intent> deadlineLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.deadline_main);

        // --- Khởi tạo Views và Database ---
        lvWeeks = findViewById(R.id.lvItemTuan);
        tvSubjectTitle = findViewById(R.id.tvSubjectTitle);
        btnBack = findViewById(R.id.btnQuaylai);
        dbHelper = new DatabaseHelper(this);

        // --- Lấy dữ liệu từ Intent ---
        Intent intent = getIntent();
        subjectMaHp = intent.getStringExtra("SUBJECT_MA_HP");
        String subjectTenHp = intent.getStringExtra("SUBJECT_TEN_HP");

        if (subjectMaHp == null || subjectMaHp.isEmpty()) {
            // Nếu không có môn học cụ thể, có thể mở màn hình deadline chung (chức năng tương lai)
            // Hiện tại, chỉ đóng lại và thông báo
            Toast.makeText(this, "Lỗi: Không có thông tin môn học.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        tvSubjectTitle.setText(subjectTenHp != null ? subjectTenHp : "Deadlines");

        // --- Khởi tạo ActivityResultLauncher ---
        setupDeadlineLauncher();

        // --- Tải dữ liệu và hiển thị --- 
        loadDataFromDatabase();

        btnBack.setOnClickListener(v -> finish());
    }

    private void loadDataFromDatabase() {
        Subject subject = dbHelper.getSubjectByMaHp(subjectMaHp);
        if (subject == null) {
            Toast.makeText(this, "Lỗi: Không thể tải thông tin môn học.", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayList<Deadline> allDeadlines = dbHelper.getDeadlinesByMaHp(subjectMaHp);

        Map<Integer, List<Deadline>> deadlinesByWeek = groupDeadlinesIntoWeeks(allDeadlines, subject.ngayBatDau);

        weekList.clear();

        int maxWeekFromDeadlines = 0;
        for (Integer weekNum : deadlinesByWeek.keySet()) {
            if (weekNum > maxWeekFromDeadlines) {
                maxWeekFromDeadlines = weekNum;
            }
        }
        int totalWeeks = Math.max(subject.soTuan > 0 ? subject.soTuan : 15, maxWeekFromDeadlines);

        for (int i = 1; i <= totalWeeks; i++) {
            Week week = new Week("Tuần " + i);
            if (deadlinesByWeek.containsKey(i)) {
                week.getDeadlines().addAll(deadlinesByWeek.get(i));
            }
            weekList.add(week);
        }

        adapterWeek = new AdapterWeek(this, R.layout.deadline_item_tuan, weekList);
        lvWeeks.setAdapter(adapterWeek);

        setupAdapterListeners();
    }

    private Map<Integer, List<Deadline>> groupDeadlinesIntoWeeks(List<Deadline> deadlines, Date subjectStartDate) {
        Map<Integer, List<Deadline>> map = new HashMap<>();
        if (deadlines == null || subjectStartDate == null) return map;

        for (Deadline d : deadlines) {
            long diffMillis = d.getNgayKetThuc().getTime() - subjectStartDate.getTime();
            long diffDays = TimeUnit.MILLISECONDS.toDays(diffMillis);
            int weekNumber = (int) (diffDays / 7) + 1;
            if (weekNumber < 1) weekNumber = 1;

            if (!map.containsKey(weekNumber)) {
                map.put(weekNumber, new ArrayList<>());
            }
            map.get(weekNumber).add(d);
        }
        return map;
    }

    private void setupDeadlineLauncher() {
        deadlineLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Toast.makeText(this, "Đã cập nhật danh sách deadline!", Toast.LENGTH_SHORT).show();
                        loadDataFromDatabase();
                    }
                }
        );
    }

    private void setupAdapterListeners() {
        if (adapterWeek == null) return;

        adapterWeek.setOnAddDeadlineListener(weekIndex -> {
            Intent intent = new Intent(MainDeadLine.this, InputDeadlineActivity.class);
            intent.putExtra("weekIndex", weekIndex);
            intent.putExtra(InputDeadlineActivity.SUBJECT_MA_HP, subjectMaHp);
            deadlineLauncher.launch(intent);
        });

        adapterWeek.setOnDeadlineLongClickListener((weekIndex, deadlineIndex, deadline) -> {
            CharSequence[] options = {"Sửa Deadline", "Xóa deadline"};

            new AlertDialog.Builder(MainDeadLine.this)
                    .setTitle("Tùy chọn cho: " + deadline.getTieuDe())
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) {
                            Intent editIntent = new Intent(MainDeadLine.this, InputDeadlineActivity.class);
                            editIntent.putExtra(InputDeadlineActivity.EDIT_DEADLINE, deadline);
                            editIntent.putExtra(InputDeadlineActivity.SUBJECT_MA_HP, subjectMaHp);
                            deadlineLauncher.launch(editIntent);
                        } else if (which == 1) {
                            new AlertDialog.Builder(MainDeadLine.this)
                                    .setTitle("Xác nhận xóa")
                                    .setMessage("Bạn có chắc chắn muốn xóa deadline này không?")
                                    .setPositiveButton("Xóa", (d, w) -> {
                                        dbHelper.deleteDeadline(deadline.getMaDl());
                                        Toast.makeText(MainDeadLine.this, "Đã xóa deadline", Toast.LENGTH_SHORT).show();
                                        loadDataFromDatabase();
                                    })
                                    .setNegativeButton("Hủy", null)
                                    .show();
                        }
                    })
                    .show();
        });

        adapterWeek.setOnDeadlineStateChangeListener((deadline, isCompleted) -> {
            deadline.setCompleted(isCompleted);
            dbHelper.updateDeadline(deadline);
            loadDataFromDatabase(); 
            Toast.makeText(MainDeadLine.this, "Đã cập nhật trạng thái", Toast.LENGTH_SHORT).show();
        });
    }
}
