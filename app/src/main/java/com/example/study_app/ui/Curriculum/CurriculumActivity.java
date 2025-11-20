package com.example.study_app.ui.Curriculum;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.study_app.R;
import com.example.study_app.data.DatabaseHelper;
import com.example.study_app.ui.Curriculum.Adapter.CurriculumAdapter;
import com.example.study_app.ui.Curriculum.Model.Curriculum;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CurriculumActivity extends AppCompatActivity {

    private Spinner spinnerFaculty, spinnerGroup, spinnerCourseType;
    private RecyclerView recyclerViewCurriculum;
    private CurriculumAdapter adapter;
    private DatabaseHelper dbHelper;
    private Map<String, Integer> facultiesMap;
    private List<String> facultyNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.curriculum_activity);

        dbHelper = new DatabaseHelper(this);

        spinnerFaculty = findViewById(R.id.spinnerFaculty);
        spinnerGroup = findViewById(R.id.spinnerGroup);
        spinnerCourseType = findViewById(R.id.spinnerCourseType);
        recyclerViewCurriculum = findViewById(R.id.recyclerViewCurriculum);

        setupRecyclerView();
        setupFilters();
        loadCurriculumData();
    }

    private void setupRecyclerView() {
        recyclerViewCurriculum.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CurriculumAdapter(new ArrayList<>());
        recyclerViewCurriculum.setAdapter(adapter);
    }

    private void setupFilters() {
        // Load dynamic data from DB
//        facultiesMap = dbHelper.getFacultiesMap();
        facultyNames = new ArrayList<>(facultiesMap.keySet());
        List<String> groupNames = dbHelper.getAllCourseGroups();

        // Add "All" option to the top
        facultyNames.add(0, getString(R.string.all_faculties));
        groupNames.add(0, getString(R.string.all_groups));

        // --- Set up Adapters ---
        ArrayAdapter<String> facultyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, facultyNames);
        facultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFaculty.setAdapter(facultyAdapter);

        ArrayAdapter<String> groupAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, groupNames);
        groupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGroup.setAdapter(groupAdapter);

        // Course Type Spinner (still can be static)
        ArrayAdapter<CharSequence> courseTypeAdapter = ArrayAdapter.createFromResource(this,
                R.array.course_type_array, android.R.layout.simple_spinner_item);
        courseTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCourseType.setAdapter(courseTypeAdapter);

        // --- Set up Listener ---
        AdapterView.OnItemSelectedListener filterListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyFilter();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        };

        spinnerFaculty.setOnItemSelectedListener(filterListener);
        spinnerGroup.setOnItemSelectedListener(filterListener);
        spinnerCourseType.setOnItemSelectedListener(filterListener);
    }

    private void applyFilter() {
        if (adapter == null) return;

        // Get selected faculty name and find its ID
        String selectedFacultyName = spinnerFaculty.getSelectedItem().toString();
        int facultyId = -1; // Default to 'All'
        if (!selectedFacultyName.equals(getString(R.string.all_faculties))) {
            Integer id = facultiesMap.get(selectedFacultyName);
            if (id != null) {
                facultyId = id;
            }
        }

        String group = spinnerGroup.getSelectedItem().toString();
        if (group.equals(getString(R.string.all_groups))) {
            group = "All"; // Use a consistent "All" string for the adapter
        }

        String courseType = spinnerCourseType.getSelectedItem().toString();

        adapter.filter(facultyId, group, courseType);
    }

    private void loadCurriculumData() {
        List<Curriculum> allCourses = dbHelper.getAllCoursesForCurriculum();
        adapter.setCourses(allCourses);
        applyFilter(); // Apply initial filter
    }
}
