package com.example.study_app.ui.Curriculum;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.study_app.R;
import com.example.study_app.data.CurriculumDao;
import com.example.study_app.data.DatabaseHelper;
import com.example.study_app.data.UserSession;
import com.example.study_app.ui.Curriculum.Adapter.CurriculumAdapter;
import com.example.study_app.ui.Curriculum.Model.Curriculum;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CurriculumActivity extends AppCompatActivity {

    // --- Views ---
    private AutoCompleteTextView autoCompleteFaculty, autoCompleteGroup, autoCompleteCourseType;
    private RecyclerView recyclerViewCurriculum;
    private SearchView searchView;
    private ImageButton buttonFilter, buttonSort;
    private MaterialCardView filtersCard;
    private ChipGroup chipGroupStatus;

    // --- Adapter & Data ---
    private CurriculumAdapter adapter;
    private DatabaseHelper dbHelper;
    private CurriculumDao curriculumDao;
    private Map<String, Integer> facultiesMap;
    private List<String> facultyNames;

    // --- State ---
    private boolean isSortAscending = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.curriculum_activity);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DatabaseHelper(this);
        curriculumDao = new CurriculumDao(dbHelper);

        // --- Ánh xạ các View ---
        setupViews();

        // --- Cài đặt ---
        setupToolbar();
        setupRecyclerView();
        setupFilterControls();
        setupActionListeners();
        loadCurriculumData();
    }

    private void setupViews() {
        // Dropdowns
        autoCompleteFaculty = findViewById(R.id.autoCompleteFaculty);
        autoCompleteGroup = findViewById(R.id.autoCompleteGroup);
        autoCompleteCourseType = findViewById(R.id.autoCompleteCourseType);
        // RecyclerView
        recyclerViewCurriculum = findViewById(R.id.recyclerViewCurriculum);
        // Action Bar
        searchView = findViewById(R.id.searchView);
        buttonFilter = findViewById(R.id.buttonFilter);
        buttonSort = findViewById(R.id.buttonSort);
        filtersCard = findViewById(R.id.filtersCard);
        chipGroupStatus = findViewById(R.id.chipGroupStatus);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        recyclerViewCurriculum.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupFilterControls() {
        facultiesMap = curriculumDao.getFacultiesMap();
        facultyNames = new ArrayList<>(facultiesMap.keySet());
        List<String> groupNames = curriculumDao.getAllCourseGroups();

        facultyNames.add(0, getString(R.string.all_faculties));
        groupNames.add(0, getString(R.string.all_groups));

        ArrayAdapter<String> facultyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, facultyNames);
        autoCompleteFaculty.setAdapter(facultyAdapter);

        ArrayAdapter<String> groupAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, groupNames);
        autoCompleteGroup.setAdapter(groupAdapter);

        ArrayAdapter<CharSequence> courseTypeAdapter = ArrayAdapter.createFromResource(this,
                R.array.course_type_array, android.R.layout.simple_dropdown_item_1line);
        autoCompleteCourseType.setAdapter(courseTypeAdapter);

        autoCompleteFaculty.setText(getString(R.string.all_faculties), false);
        autoCompleteGroup.setText(getString(R.string.all_groups), false);
        if (courseTypeAdapter.getCount() > 0) {
            autoCompleteCourseType.setText(courseTypeAdapter.getItem(0), false);
        }

        autoCompleteFaculty.setOnItemClickListener((parent, view, position, id) -> applyAllFilters());
        autoCompleteGroup.setOnItemClickListener((parent, view, position, id) -> applyAllFilters());
        autoCompleteCourseType.setOnItemClickListener((parent, view, position, id) -> applyAllFilters());
    }

    private void setupActionListeners() {
        buttonFilter.setOnClickListener(v -> {
            filtersCard.setVisibility(filtersCard.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        });

        buttonSort.setOnClickListener(v -> {
            isSortAscending = !isSortAscending;
            applyAllFilters();
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                applyAllFilters();
                return true;
            }
        });

        chipGroupStatus.setOnCheckedChangeListener((group, checkedId) -> applyAllFilters());
    }

    private void applyAllFilters() {
        if (adapter == null) return;

        String searchQuery = searchView.getQuery().toString();
        String selectedFacultyName = autoCompleteFaculty.getText().toString();
        String group = autoCompleteGroup.getText().toString();
        String courseType = autoCompleteCourseType.getText().toString();
        String status = getSelectedStatus();

        int facultyId = -1;
        if (!selectedFacultyName.equals(getString(R.string.all_faculties))) {
            Integer id = facultiesMap.get(selectedFacultyName);
            if (id != null) {
                facultyId = id;
            }
        }

        if (group.equals(getString(R.string.all_groups))) {
            group = "All";
        }

        adapter.filterAndSort(searchQuery, facultyId, group, courseType, status, isSortAscending);
    }

    private String getSelectedStatus() {
        int checkedChipId = chipGroupStatus.getCheckedChipId();
        if (checkedChipId == R.id.chipNotStudied) {
            return DatabaseHelper.STATUS_NOT_ENROLLED;
        } else if (checkedChipId == R.id.chipStudying) {
            return DatabaseHelper.STATUS_IN_PROGRESS;
        } else if (checkedChipId == R.id.chipStudied) {
            return DatabaseHelper.STATUS_COMPLETED;
        }
        return "All"; // No filter
    }

    private void loadCurriculumData() {
        List<Curriculum> allCourses = curriculumDao.getAllCoursesForCurriculumWithStatus(UserSession.getCurrentUserId(this));
        adapter = new CurriculumAdapter(this, allCourses);
        recyclerViewCurriculum.setAdapter(adapter);
        applyAllFilters();
    }
}
