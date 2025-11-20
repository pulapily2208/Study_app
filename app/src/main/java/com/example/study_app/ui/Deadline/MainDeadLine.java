package com.example.study_app.ui.Deadline;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MainDeadLine extends AppCompatActivity {

    private ListView lvWeeks;
    private TextView tvSubjectTitle;
    private Button btnBack;

    private DatabaseHelper dbHelper;
    private AdapterWeek adapterWeek;

    private String subjectMaHp;
    private Date subjectStartDate; // Lưu ngày bắt đầu để truyền cho adapter
    private ActivityResultLauncher<Intent> deadlineLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.deadline_main);

        lvWeeks = findViewById(R.id.lvItemTuan);
        tvSubjectTitle = findViewById(R.id.tvSubjectTitle);
        btnBack = findViewById(R.id.btnQuaylai);
        dbHelper = new DatabaseHelper(this);

        Intent intent = getIntent();
        subjectMaHp = intent.getStringExtra("SUBJECT_MA_HP");
        String subjectTenHp = intent.getStringExtra("SUBJECT_TEN_HP");

        if (subjectMaHp == null || subjectMaHp.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không có thông tin môn học.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        tvSubjectTitle.setText(subjectTenHp != null ? subjectTenHp : "Deadlines");

        // Khởi tạo adapter một lần duy nhất với danh sách trống
        adapterWeek = new AdapterWeek(this, R.layout.deadline_item_tuan, new ArrayList<>(), null);
        lvWeeks.setAdapter(adapterWeek);

        setupDeadlineLauncher();
        loadDataFromDatabase();
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadDataFromDatabase() {
        Subject subject = dbHelper.getSubjectByMaHp(subjectMaHp);
        if (subject == null) {
            Toast.makeText(this, "Lỗi: Không thể tải thông tin môn học.", Toast.LENGTH_SHORT).show();
            return;
        }
        subjectStartDate = subject.ngayBatDau;

        ArrayList<Deadline> allDeadlines = dbHelper.getDeadlinesByMaHp(subjectMaHp);
        Map<Integer, List<Deadline>> deadlinesByWeek = groupDeadlinesIntoWeeks(allDeadlines, subjectStartDate);

        ArrayList<Week> newWeekList = new ArrayList<>();
        int maxWeekFromDeadlines = 0;
        if (!deadlinesByWeek.isEmpty()) {
            for (Integer weekNum : deadlinesByWeek.keySet()) {
                if (weekNum > maxWeekFromDeadlines) {
                    maxWeekFromDeadlines = weekNum;
                }
            }
        }
        int totalWeeks = Math.max(subject.soTuan > 0 ? subject.soTuan : 15, maxWeekFromDeadlines);

        for (int i = 1; i <= totalWeeks; i++) {
            Week week = new Week("Tuần " + i);
            if (deadlinesByWeek.containsKey(i)) {
                week.getDeadlines().addAll(deadlinesByWeek.get(i));
            }
            newWeekList.add(week);
        }

        // Sửa lỗi ở đây: Cập nhật ngày bắt đầu cho adapter
        adapterWeek.setSubjectStartDate(subjectStartDate);

        // Cập nhật dữ liệu cho adapter
        adapterWeek.clear();
        adapterWeek.addAll(newWeekList);

        setupAdapterListeners();
    }

    private Map<Integer, List<Deadline>> groupDeadlinesIntoWeeks(List<Deadline> deadlines, Date startDate) {
        Map<Integer, List<Deadline>> map = new HashMap<>();
        if (deadlines == null || startDate == null) return map;

        for (Deadline d : deadlines) {
            if (d.getNgayKetThuc() == null) continue;
            long diffMillis = d.getNgayKetThuc().getTime() - startDate.getTime();
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
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Serializable extra = result.getData().getSerializableExtra(InputDeadlineActivity.KEY_TAI_KHOAN);
                        if (extra instanceof Deadline) {
                            Deadline returnedDeadline = (Deadline) extra;
                            boolean isUpdate = returnedDeadline.getMaDl() > 0;

                            long dbResult;
                            if (isUpdate) {
                                dbResult = dbHelper.updateDeadline(returnedDeadline);
                            } else {
                                dbResult = dbHelper.addDeadline(returnedDeadline, subjectMaHp);
                            }

                            if (dbResult != -1) {
                                Toast.makeText(this, isUpdate ? "Đã cập nhật deadline" : "Đã thêm deadline", Toast.LENGTH_SHORT).show();
                                loadDataFromDatabase();
                            } else {
                                Toast.makeText(this, "Thao tác với database thất bại!", Toast.LENGTH_SHORT).show();
                            }
                        }
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
            if (subjectStartDate != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(subjectStartDate);
                cal.add(Calendar.WEEK_OF_YEAR, weekIndex);
                intent.putExtra(InputDeadlineActivity.WEEK_START_DATE, cal.getTimeInMillis());
            }
            deadlineLauncher.launch(intent);
        });

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
                editIntent.putExtra(InputDeadlineActivity.EDIT_DEADLINE, deadline);
                deadlineLauncher.launch(editIntent);
            }

            @Override
            public void onDeleteDeadline(Deadline deadline) {
                new AlertDialog.Builder(MainDeadLine.this)
                        .setTitle("Xác nhận xóa")
                        .setMessage("Bạn có chắc muốn xóa deadline này?")
                        .setPositiveButton("Xóa", (d, w) -> {
                            dbHelper.deleteDeadline(deadline.getMaDl());
                            Toast.makeText(MainDeadLine.this, "Đã xóa deadline", Toast.LENGTH_SHORT).show();
                            loadDataFromDatabase();
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            }

            @Override
            public void onStateChanged(Deadline deadline, boolean isCompleted) {
                deadline.setCompleted(isCompleted);
                dbHelper.updateDeadline(deadline);
                Toast.makeText(MainDeadLine.this, "Đã cập nhật trạng thái", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
