package com.example.study_app.ui.Subject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.study_app.R;
import com.example.study_app.data.DatabaseHelper;
import com.example.study_app.ui.Subject.Adapter.SubjectAdapter;
import com.example.study_app.ui.Subject.Model.Subject;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class SubjectListActivity extends AppCompatActivity {

    private FloatingActionButton fab;
    private RecyclerView recyclerViewSubjects;
    private SubjectAdapter subjectAdapter;
    private DatabaseHelper dbHelper;
    private ArrayList<Subject> subjectList;
    private Spinner spinnerSemesters;
    private TextView tvEmptyList;

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

        // Set up the adapter and RecyclerView
        subjectAdapter = new SubjectAdapter(this, subjectList);
        recyclerViewSubjects.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewSubjects.setAdapter(subjectAdapter);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SubjectListActivity.this, SubjectAddActivity.class);
                startActivity(intent);
            }
        });

        spinnerSemesters.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedSemester = (String) parent.getItemAtPosition(position);
                loadSubjects(selectedSemester);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Load semesters every time the activity is resumed.
        // This will also trigger onItemSelected and load subjects for the first semester.
        loadSemesters();
    }

    private void loadSemesters() {
        ArrayList<String> semesterNames = dbHelper.getAllSemesterNames();
        ArrayAdapter<String> semesterAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, semesterNames);
        semesterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSemesters.setAdapter(semesterAdapter);

        if (semesterNames.isEmpty()) {
            // If there are no semesters, clear the list and show empty state
            subjectList.clear();
            subjectAdapter.notifyDataSetChanged();
            checkEmptyState();
        }
    }

    private void loadSubjects(String semesterName) {
        // 1. Get updated list from the database for the selected semester
        ArrayList<Subject> updatedSubjects = dbHelper.getSubjectsBySemester(semesterName);

        // 2. Clear the old list
        subjectList.clear();

        // 3. Add all new subjects
        subjectList.addAll(updatedSubjects);

        // 4. Notify the adapter to refresh the RecyclerView
        subjectAdapter.notifyDataSetChanged();

        // 5. Check if the list is empty and update UI
        checkEmptyState();
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
}
