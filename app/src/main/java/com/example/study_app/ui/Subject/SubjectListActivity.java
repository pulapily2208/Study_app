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

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.study_app.R;
import com.example.study_app.data.DatabaseHelper;
import com.example.study_app.data.SubjectDao;
import com.example.study_app.data.TimetableDao;
import com.example.study_app.ui.Deadline.MainDeadLineMonHoc;
import com.example.study_app.ui.Subject.Adapter.SubjectAdapter;
import com.example.study_app.ui.Subject.Model.Subject;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

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
    private DatabaseHelper dbHelper;

    private static final int ADD_SUBJECT_REQUEST = 1;
    private static final int EDIT_SUBJECT_REQUEST = 2;
    private static final int BULK_IMPORT_REQUEST = 3;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.subject_list);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerViewSubjects = findViewById(R.id.recyclerViewSubjects);
        spinnerSemesters = findViewById(R.id.spinnerSemesters);
        tvEmptyList = findViewById(R.id.tvEmptyList);
        fabAddSubject = findViewById(R.id.fab_add_subject);
        btnBulkAddSubject = findViewById(R.id.btn_bulk_add_subject);

        dbHelper = new DatabaseHelper(this);
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
                    loadSemesters(); // Reload semesters to ensure list is fresh
                    // No need to call loadSubjectsForSelectedSemester here, as setSelection in
                    // loadSemesters will trigger it
                }
            } else {
                // Generic refresh, maintain current selection
                loadSemesters();
            }
        }
    }

    /**
     * Calculates the current semester ordinal based on a presumed start date.
     * NOTE: This makes a strong assumption that the user started their program in
     * Sep 2021.
     * For true accuracy, the user's actual enrollment date should be stored and
     * used.
     * Academic Calendar assumptions:
     * - Fall Semester (Kỳ 1, 3, 5, 7): From September to January.
     * - Spring Semester (Kỳ 2, 4, 6, 8): From February to July.
     * 
     * @return The calculated current semester number (e.g., 6).
     */
    private int calculateCurrentSemesterOrdinal() {
        int startYear = 2021;
        int startMonth = 9; // September

        Calendar now = Calendar.getInstance();
        int currentYear = now.get(Calendar.YEAR);
        int currentMonth = now.get(Calendar.MONTH) + 1; // January is 1

        // Determine the academic year. An academic year runs from August to July.
        int academicYearOfStart = (startMonth < 8) ? startYear - 1 : startYear;
        int academicYearOfNow = (currentMonth < 8) ? currentYear - 1 : currentYear;

        int academicYearsPassed = academicYearOfNow - academicYearOfStart;
        int semesterOrdinal = academicYearsPassed * 2;

        // Check if it's the first or second semester of the academic year.
        if (currentMonth >= 8 || currentMonth <= 1) { // Fall semester (Aug - Jan)
            semesterOrdinal += 1;
        } else { // Spring semester (Feb - Jul)
            semesterOrdinal += 2;
        }
        return semesterOrdinal;
    }

    /**
     * Resolve the current semester name from the database based on today's date.
     * Uses TimetableDao logic to map current year/month to the proper semester
     * record.
     * 
     * @return Semester name like "Học kỳ 6" or null if not found.
     */
    private String resolveCurrentSemesterNameFromDb() {
        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        int monthZeroBased = now.get(Calendar.MONTH); // 0-11

        TimetableDao timetableDao = new TimetableDao(dbHelper);
        Integer semesterId = timetableDao.getSemesterIdBySelectedDate(year, monthZeroBased);
        if (semesterId == null)
            return null;
        return timetableDao.getSemesterNameById(semesterId);
    }

    private void loadSemesters() {
        String previouslySelected = selectedSemesterName;
        ArrayList<String> semesterNames = subjectDao.getAllSemesterNames();

        if (semesterNames.isEmpty()) {
            subjectList.clear();
            subjectAdapter.notifyDataSetChanged();
            checkEmptyState();
            // Also clear the spinner
            ArrayAdapter<String> emptyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                    new ArrayList<>());
            spinnerSemesters.setAdapter(emptyAdapter);
            return;
        }

        ArrayAdapter<String> semesterAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                semesterNames);
        semesterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSemesters.setAdapter(semesterAdapter);

        int selectionIndex = -1;

        // 1. If returning to the screen (e.g., after an edit), re-select the previous
        // semester.
        if (previouslySelected != null && semesterNames.contains(previouslySelected)) {
            selectionIndex = semesterAdapter.getPosition(previouslySelected);
        } else {
            // 2. Prefer to select the current semester resolved from DB/calendar.
            String currentSemesterName = resolveCurrentSemesterNameFromDb();
            if (currentSemesterName != null && semesterNames.contains(currentSemesterName)) {
                selectionIndex = semesterAdapter.getPosition(currentSemesterName);
            } else {
                // 3. FALLBACK: If the resolved semester isn't in the list,
                // select the highest-numbered semester that the user has data for.
                ArrayList<Integer> semesterNumbers = new ArrayList<>();
                for (String name : semesterNames) {
                    try {
                        int num = Integer.parseInt(name.replaceAll("\\D+", ""));
                        semesterNumbers.add(num);
                    } catch (NumberFormatException e) {
                        // Ignore names that don't fit the format "Học kỳ X"
                    }
                }
                if (!semesterNumbers.isEmpty()) {
                    Collections.sort(semesterNumbers, Collections.reverseOrder());
                    String highestSemesterName = "Học kỳ " + semesterNumbers.get(0);
                    if (semesterNames.contains(highestSemesterName)) {
                        selectionIndex = semesterAdapter.getPosition(highestSemesterName);
                    }
                }
            }
        }

        // 4. FINAL FALLBACK: If nothing has been selected, select the first item.
        if (selectionIndex == -1 && !semesterNames.isEmpty()) {
            selectionIndex = 0;
        }

        if (selectionIndex != -1) {
            spinnerSemesters.setSelection(selectionIndex);
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
