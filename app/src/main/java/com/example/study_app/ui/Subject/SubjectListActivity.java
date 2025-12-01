package com.example.study_app.ui.Subject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.study_app.R;
import com.example.study_app.data.DatabaseHelper;
import com.example.study_app.data.SubjectDao;
import com.example.study_app.ui.Deadline.MainDeadLineMonHoc;
import com.example.study_app.ui.Subject.Adapter.SubjectAdapter;
import com.example.study_app.ui.Subject.Model.Subject;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class SubjectListActivity extends AppCompatActivity implements SubjectAdapter.OnSubjectActionClickListener {

    private RecyclerView recyclerViewSubjects;
    private SubjectAdapter subjectAdapter;
    private SubjectDao subjectDao;
    private ArrayList<Subject> subjectList;
    private Spinner spinnerSemesters;
    private TextView tvEmptyList;
    private String selectedSemesterName;
    private FloatingActionButton fabAddSubject;
    private ImageButton btnBulkAddSubject;

    private static final int ADD_SUBJECT_REQUEST = 1;
    private static final int EDIT_SUBJECT_REQUEST = 2;
    private static final int BULK_IMPORT_REQUEST = 3;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subject_list);

        recyclerViewSubjects = findViewById(R.id.recyclerViewSubjects);
        spinnerSemesters = findViewById(R.id.spinnerSemesters);
        tvEmptyList = findViewById(R.id.tvEmptyList);
        fabAddSubject = findViewById(R.id.fab_add_subject);
        btnBulkAddSubject = findViewById(R.id.btn_bulk_add_subject);

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        subjectDao = new SubjectDao(dbHelper);

        subjectList = new ArrayList<>();

        subjectAdapter = new SubjectAdapter(subjectList, this);
        recyclerViewSubjects.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewSubjects.setAdapter(subjectAdapter);

        spinnerSemesters.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedSemesterName = parent.getItemAtPosition(position).toString();
                loadSubjectsForSelectedSemester();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedSemesterName = null;
            }
        });

        fabAddSubject.setOnClickListener(v -> {
            if (selectedSemesterName != null && !selectedSemesterName.isEmpty()) {
                Intent intent = new Intent(this, SubjectAddActivity.class);
                intent.putExtra("SEMESTER_NAME", selectedSemesterName);
                startActivityForResult(intent, ADD_SUBJECT_REQUEST);
            } else {
                Toast.makeText(this, "Vui lòng chọn một học kỳ trước khi thêm.", Toast.LENGTH_SHORT).show();
            }
        });

        btnBulkAddSubject.setOnClickListener(v -> {
            if (selectedSemesterName != null && !selectedSemesterName.isEmpty()) {
                Intent intent = new Intent(this, SubjectBulkImportActivity.class);
                intent.putExtra("SEMESTER_NAME", selectedSemesterName);
                startActivityForResult(intent, BULK_IMPORT_REQUEST);
            } else {
                Toast.makeText(this, "Vui lòng chọn một học kỳ trước.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSemesters();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == ADD_SUBJECT_REQUEST || requestCode == EDIT_SUBJECT_REQUEST
                || requestCode == BULK_IMPORT_REQUEST) && resultCode == RESULT_OK) {
            Toast.makeText(this, "Danh sách môn học đã được cập nhật.", Toast.LENGTH_SHORT).show();
            if (data != null && data.hasExtra("UPDATED_SEMESTER_NAME")) {
                String updatedSemester = data.getStringExtra("UPDATED_SEMESTER_NAME");
                if (updatedSemester != null && !updatedSemester.isEmpty()) {
                    // Set the spinner to the semester where the change happened and reload subjects
                    selectedSemesterName = updatedSemester;
                    loadSemesters();
                    loadSubjectsForSelectedSemester();
                }
            } else {
                // Generic refresh
                loadSemesters();
            }
        }
    }

    private void loadSemesters() {
        String previouslySelected = selectedSemesterName;
        ArrayList<String> semesterNames = subjectDao.getAllSemesterNames();
        ArrayAdapter<String> semesterAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                semesterNames);
        semesterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSemesters.setAdapter(semesterAdapter);

        if (previouslySelected != null && semesterNames.contains(previouslySelected)) {
            int spinnerPosition = semesterAdapter.getPosition(previouslySelected);
            spinnerSemesters.setSelection(spinnerPosition);
        } else if (!semesterNames.isEmpty()) {
            spinnerSemesters.setSelection(0);
        } else {
            subjectList.clear();
            subjectAdapter.notifyDataSetChanged();
            checkEmptyState();
        }
    }

    private void loadSubjectsForSelectedSemester() {
        if (selectedSemesterName != null) {
            ArrayList<Subject> updatedSubjects = subjectDao.getSubjectsBySemester(selectedSemesterName);
            subjectList.clear();
            subjectList.addAll(updatedSubjects);
            subjectAdapter.notifyDataSetChanged();
            checkEmptyState();
        } else {
            subjectList.clear();
            subjectAdapter.notifyDataSetChanged();
            checkEmptyState();
        }
    }

    private void checkEmptyState() {
        if (subjectList.isEmpty()) {
            recyclerViewSubjects.setVisibility(View.GONE);
            tvEmptyList.setVisibility(View.VISIBLE);
        } else {
            recyclerViewSubjects.setVisibility(View.VISIBLE);
            tvEmptyList.setVisibility(View.GONE);
        }
    }

    @Override
    public void onEdit(Subject subject) {
        Intent intent = new Intent(this, SubjectAddActivity.class);
        intent.putExtra("SUBJECT_ID", subject.maHp);
        if (selectedSemesterName != null && !selectedSemesterName.isEmpty()) {
            intent.putExtra("SEMESTER_NAME", selectedSemesterName);
        }
        startActivityForResult(intent, EDIT_SUBJECT_REQUEST);
    }

    @Override
    public void onDelete(Subject subject) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa môn học '" + subject.tenHp + "'?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    subjectDao.deleteSubject(subject.maHp);
                    Toast.makeText(this, "Đã xóa môn học", Toast.LENGTH_SHORT).show();
                    loadSubjectsForSelectedSemester();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onViewDeadlines(Subject subject) {
        Intent intent = new Intent(this, MainDeadLineMonHoc.class);
        intent.putExtra("SUBJECT_MA_HP", subject.maHp);
        intent.putExtra("SUBJECT_TEN_HP", subject.tenHp);
        intent.putExtra("SUBJECT_WEEKS", subject.soTuan);
        startActivity(intent);
    }
}
