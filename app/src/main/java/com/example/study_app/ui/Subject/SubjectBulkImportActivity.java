package com.example.study_app.ui.Subject;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.study_app.R;
import com.example.study_app.data.CurriculumDao;
import com.example.study_app.data.DatabaseHelper;
import com.example.study_app.data.SubjectDao;
import com.example.study_app.ui.Curriculum.Model.Curriculum;
import com.example.study_app.ui.Subject.Adapter.SubjectBulkImportAdapter;
import com.example.study_app.ui.Subject.Model.Subject;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SubjectBulkImportActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SubjectBulkImportAdapter adapter;
    private ArrayList<Subject> subjectList;
    private Button btnAddRow, btnSave;
    private String tenHocKy;
    private SubjectDao subjectDao;
    private CurriculumDao curriculumDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subject_activity_bulk_import);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Nhập môn học hàng loạt");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        tenHocKy = getIntent().getStringExtra("SEMESTER_NAME");
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        subjectDao = new SubjectDao(dbHelper);
        curriculumDao = new CurriculumDao(dbHelper);


        recyclerView = findViewById(R.id.recycler_view_bulk_import);
        btnAddRow = findViewById(R.id.btn_add_row);
        btnSave = findViewById(R.id.btn_save_bulk);

        subjectList = new ArrayList<>();
        adapter = new SubjectBulkImportAdapter(subjectList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Add an initial row
        adapter.addRow();

        btnAddRow.setOnClickListener(v -> {
            adapter.addRow();
        });

        btnSave.setOnClickListener(v -> {
            saveSubjects();
        });
    }

    private void saveSubjects() {
        ArrayList<Subject> subjectsToSave = adapter.getSubjectList();
        if (subjectsToSave.isEmpty()) {
            Toast.makeText(this, "Danh sách trống.", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- Start of new logic ---
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            // Step 1: Validate and fetch subject names
            for (Subject subject : subjectsToSave) {
                if (subject.maHp == null || subject.maHp.trim().isEmpty()) {
                    runOnUiThread(() -> Toast.makeText(this, "Mã học phần không được để trống.", Toast.LENGTH_SHORT).show());
                    return; // Stop the process
                }

                Curriculum curriculumDetails = curriculumDao.getCurriculumDetailsByMaHp(subject.maHp.trim());
                if (curriculumDetails == null) {
                    runOnUiThread(() -> Toast.makeText(this, "Mã học phần không hợp lệ: " + subject.maHp, Toast.LENGTH_SHORT).show());
                    return; // Stop the process
                }

                // Auto-fill subject name and assign semester
                subject.tenHp = curriculumDetails.getTenHp();
                subject.tenHk = tenHocKy;
            }

            // Step 2: Save subjects to the database
            boolean allSuccess = true;
            for (Subject subject : subjectsToSave) {
                long result = subjectDao.addOrEnrollSubject(subject);
                if (result == -1) {
                    allSuccess = false;
                }
            }

            // Step 3: Report result on UI thread
            boolean finalAllSuccess = allSuccess;
            runOnUiThread(() -> {
                if (finalAllSuccess) {
                    Toast.makeText(SubjectBulkImportActivity.this, "Lưu thành công!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(SubjectBulkImportActivity.this, "Đã xảy ra lỗi trong quá trình lưu. Có thể một số môn học đã tồn tại.", Toast.LENGTH_LONG).show();
                }
            });
        });
        // --- End of new logic ---
    }


    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
