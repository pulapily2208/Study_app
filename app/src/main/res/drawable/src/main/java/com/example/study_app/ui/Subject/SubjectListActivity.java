package com.example.study_app.ui.Subject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import com.example.study_app.ui.Deadline.MainDeadLineMonHoc;
import com.example.study_app.ui.Subject.Adapter.SubjectAdapter;
import com.example.study_app.ui.Subject.Model.Subject;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class SubjectListActivity extends AppCompatActivity implements SubjectAdapter.OnSubjectActionClickListener {

    private FloatingActionButton fab;
    private RecyclerView recyclerViewSubjects;
    private SubjectAdapter subjectAdapter;
    private DatabaseHelper dbHelper;
    private ArrayList<Subject> subjectList;
    private Spinner spinnerSemesters;
    private TextView tvEmptyList;
    private String selectedSemesterName; // To hold the currently selected semester

    // Request codes for starting activities
    private static final int ADD_SUBJECT_REQUEST = 1;
    private static final int EDIT_SUBJECT_REQUEST = 2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subject_list);

        fab = findViewById(R.id.fab);
        recyclerViewSubjects = findViewById(R.id.recyclerViewSubjects);
        spinnerSemesters = findViewById(R.id.spinnerSemesters);
        tvEmptyList = findViewById(R.id.tvEmptyList);

        dbHelper = new DatabaseHelper(this);
        subjectList = new ArrayList<>();

        subjectAdapter = new SubjectAdapter(subjectList, this);
        recyclerViewSubjects.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewSubjects.setAdapter(subjectAdapter);

        fab.setOnClickListener(v -> {
            if (selectedSemesterName != null && !selectedSemesterName.isEmpty()) {
                Intent intent = new Intent(SubjectListActivity.this, SubjectAddActivity.class);
                intent.putExtra("SEMESTER_NAME", selectedSemesterName); // Pass semester name
                startActivityForResult(intent, ADD_SUBJECT_REQUEST); // Use forResult
            } else {
                Toast.makeText(this, "Vui lòng chọn một học kỳ trước khi thêm.", Toast.LENGTH_SHORT).show();
            }
        });

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
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSemesters();
    }

    // This method is called when an activity started with startActivityForResult completes.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check if the result is from adding or editing a subject and if it was successful.
        if ((requestCode == ADD_SUBJECT_REQUEST || requestCode == EDIT_SUBJECT_REQUEST) && resultCode == RESULT_OK) {
            // The data was changed, so reload the subjects for the currently selected semester.
            Toast.makeText(this, "Danh sách môn học đã được cập nhật.", Toast.LENGTH_SHORT).show();
            loadSubjectsForSelectedSemester();
        }
    }


    private void loadSemesters() {
        // Save current selection
        String previouslySelected = selectedSemesterName;
        ArrayList<String> semesterNames = dbHelper.getAllSemesterNames();
        ArrayAdapter<String> semesterAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, semesterNames);
        semesterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSemesters.setAdapter(semesterAdapter);

        // Restore previous selection if it still exists
        if (previouslySelected != null && semesterNames.contains(previouslySelected)) {
            int spinnerPosition = semesterAdapter.getPosition(previouslySelected);
            spinnerSemesters.setSelection(spinnerPosition);
        } else if (!semesterNames.isEmpty()) {
            spinnerSemesters.setSelection(0);
        }

        if (semesterNames.isEmpty()) {
            subjectList.clear();
            subjectAdapter.notifyDataSetChanged();
            checkEmptyState();
        }
    }

    private void loadSubjectsForSelectedSemester() {
        if (selectedSemesterName != null) {
            ArrayList<Subject> updatedSubjects = dbHelper.getSubjectsBySemester(selectedSemesterName);
            subjectList.clear();
            subjectList.addAll(updatedSubjects);
            subjectAdapter.notifyDataSetChanged();
            checkEmptyState();
        } else {
            // Handle case where no semester is selected (e.g., all are deleted)
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
        // Ensure the correct key "SUBJECT_ID" is used, which SubjectAddActivity expects.
        intent.putExtra("SUBJECT_ID", subject.maHp);
        if (selectedSemesterName != null && !selectedSemesterName.isEmpty()) {
            intent.putExtra("SEMESTER_NAME", selectedSemesterName);
        }
        startActivityForResult(intent, EDIT_SUBJECT_REQUEST); // Use forResult
    }

    @Override
    public void onDelete(Subject subject) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa môn học '" + subject.tenHp + "'?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    dbHelper.deleteSubject(subject.maHp);
                    Toast.makeText(this, "Đã xóa môn học", Toast.LENGTH_SHORT).show();
                    loadSubjectsForSelectedSemester(); // Refresh the list
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onViewDeadlines(Subject subject) {
        Intent intent = new Intent(this, MainDeadLineMonHoc.class);
        intent.putExtra("SUBJECT_MA_HP", subject.maHp);
        intent.putExtra("SUBJECT_TEN_HP", subject.tenHp);
        // Gửi số tuần vào màn hình deadline
        intent.putExtra("SUBJECT_WEEKS", subject.soTuan);
        startActivity(intent);
    }
}