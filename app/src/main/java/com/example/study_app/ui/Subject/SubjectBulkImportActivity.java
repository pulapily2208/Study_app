package com.example.study_app.ui.Subject;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.study_app.R;
import com.example.study_app.data.CurriculumDao;
import com.example.study_app.data.DatabaseHelper;
import com.example.study_app.data.SubjectDao;
import com.example.study_app.ui.Curriculum.Model.Curriculum;
import com.example.study_app.ui.Subject.Adapter.SubjectBulkImportAdapter;
import com.example.study_app.ui.Subject.Model.Subject;
import com.example.study_app.ui.common.NavbarHelper;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SubjectBulkImportActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SubjectBulkImportAdapter adapter;
    private ArrayList<Subject> danhSachMonHoc;
    private Button btnThemDong, btnLuu;
    private String tenHocKy;
    private SubjectDao subjectDao;
    private CurriculumDao curriculumDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.subject_activity_bulk_import);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Nhập môn học hàng loạt");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        tenHocKy = getIntent().getStringExtra("SEMESTER_NAME");
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        subjectDao = new SubjectDao(dbHelper);
        curriculumDao = new CurriculumDao(dbHelper);

        recyclerView = findViewById(R.id.recycler_view_bulk_import);
        btnThemDong = findViewById(R.id.btn_add_row);
        btnLuu = findViewById(R.id.btn_save_bulk);

        danhSachMonHoc = new ArrayList<>();
        adapter = new SubjectBulkImportAdapter(danhSachMonHoc);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        adapter.addRow();

        btnThemDong.setOnClickListener(v -> adapter.addRow());

        btnLuu.setOnClickListener(v -> luuMonHocHangLoat());

        NavbarHelper.setupNavbar(this, R.id.btnSubject);
    }

    private void luuMonHocHangLoat() {
        ArrayList<Subject> subjectsToSave = adapter.getSubjectList();
        if (subjectsToSave.isEmpty()) {
            Toast.makeText(this, "Danh sách trống.", Toast.LENGTH_SHORT).show();
            return;
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            // B1: Kiểm tra môn học đã tồn tại chưa
            for (Subject subject : subjectsToSave) {
                if (subject.maHp == null || subject.maHp.trim().isEmpty()) {
                    runOnUiThread(
                            () -> Toast.makeText(this, "Mã học phần không được để trống.", Toast.LENGTH_SHORT).show());
                    return;
                }

                Curriculum curriculumDetails = curriculumDao.getCurriculumDetailsByMaHp(subject.maHp.trim());
                if (curriculumDetails == null) {
                    runOnUiThread(() -> Toast
                            .makeText(this, "Mã học phần không hợp lệ: " + subject.maHp, Toast.LENGTH_SHORT).show());
                    return;
                }

                subject.tenHp = curriculumDetails.getTenHp();
                subject.tenHk = tenHocKy;

                int weeks = 0;
                String code = subject.maHp != null ? subject.maHp.trim().toUpperCase() : "";
                if (code.startsWith("DEFE")) {
                    weeks = 4;
                } else if (code.startsWith("PHYE")) {
                    weeks = 7;
                } else {
                    int credits = curriculumDetails.getSoTinChi();
                    int totalPeriods = curriculumDetails.getSoTietLyThuyet() + curriculumDetails.getSoTietThucHanh();
                    if (credits > 0) {
                        weeks = totalPeriods / credits;
                    }
                }
                subject.soTuan = weeks;

                if (subject.ngayBatDau != null && weeks > 0) {
                    java.util.Calendar cal = java.util.Calendar.getInstance();
                    cal.setTime(subject.ngayBatDau);
                    cal.add(java.util.Calendar.DAY_OF_YEAR, (weeks * 7) - 1);
                    subject.ngayKetThuc = cal.getTime();
                }
            }

            // B2: Lưu môn học
            boolean allSuccess = true;
            for (Subject subject : subjectsToSave) {
                long result = subjectDao.addOrEnrollSubject(subject);
                if (result == -1) {
                    allSuccess = false;
                }
            }

            // B3: Thông báo kết quả
            boolean finalAllSuccess = allSuccess;
            runOnUiThread(() -> {
                if (finalAllSuccess) {
                    Toast.makeText(SubjectBulkImportActivity.this, "Lưu thành công!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(SubjectBulkImportActivity.this,
                            "Đã xảy ra lỗi trong quá trình lưu. Có thể một số môn học đã tồn tại.", Toast.LENGTH_LONG)
                            .show();
                }
            });
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
