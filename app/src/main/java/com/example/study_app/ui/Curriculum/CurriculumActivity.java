package com.example.study_app.ui.Curriculum;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.study_app.R;
import com.example.study_app.data.DatabaseHelper;
import com.example.study_app.ui.Curriculum.Adapter.CurriculumAdapter;
import com.example.study_app.ui.Curriculum.Model.Curriculum;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;

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

    // --- Adapter & Data ---
    private CurriculumAdapter adapter;
    private DatabaseHelper dbHelper;
    private Map<String, Integer> facultiesMap;
    private List<String> facultyNames;

    // --- State ---
    private boolean isSortAscending = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.curriculum_activity);

        dbHelper = new DatabaseHelper(this);

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
        // Giả định adapter đã được khởi tạo, nếu chưa, hãy khởi tạo nó
        // adapter = new CurriculumAdapter(new ArrayList<>());
        // recyclerViewCurriculum.setAdapter(adapter);
    }

    private void setupFilterControls() {
        facultiesMap = dbHelper.getFacultiesMap();
        facultyNames = new ArrayList<>(facultiesMap.keySet());
        List<String> groupNames = dbHelper.getAllCourseGroups();

        facultyNames.add(0, getString(R.string.all_faculties));
        groupNames.add(0, getString(R.string.all_groups));

        ArrayAdapter<String> facultyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, facultyNames);
        autoCompleteFaculty.setAdapter(facultyAdapter);

        ArrayAdapter<String> groupAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, groupNames);
        autoCompleteGroup.setAdapter(groupAdapter);

        ArrayAdapter<CharSequence> courseTypeAdapter = ArrayAdapter.createFromResource(this,
                R.array.course_type_array, android.R.layout.simple_dropdown_item_1line);
        autoCompleteCourseType.setAdapter(courseTypeAdapter);

        // --- Cài đặt giá trị mặc định ---
        autoCompleteFaculty.setText(getString(R.string.all_faculties), false);
        autoCompleteGroup.setText(getString(R.string.all_groups), false);
        if (courseTypeAdapter.getCount() > 0) {
            autoCompleteCourseType.setText(courseTypeAdapter.getItem(0), false);
        }

        // Khi người dùng chọn một mục trong bộ lọc, áp dụng lại tất cả bộ lọc
        autoCompleteFaculty.setOnItemClickListener((parent, view, position, id) -> applyAllFilters());
        autoCompleteGroup.setOnItemClickListener((parent, view, position, id) -> applyAllFilters());
        autoCompleteCourseType.setOnItemClickListener((parent, view, position, id) -> applyAllFilters());
    }

    private void setupActionListeners() {
        // 1. Nút Lọc: Hiển thị/ẩn khung bộ lọc
        buttonFilter.setOnClickListener(v -> {
            filtersCard.setVisibility(filtersCard.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        });

        // 2. Nút Sắp xếp: Đảo ngược trạng thái và lọc lại
        buttonSort.setOnClickListener(v -> {
            isSortAscending = !isSortAscending;
            // Gợi ý: bạn có thể thay đổi icon của nút ở đây để thể hiện trạng thái (A-Z / Z-A)
            // buttonSort.setImageResource(isSortAscending ? R.drawable.ic_sort_az : R.drawable.ic_sort_za);
            applyAllFilters();
        });

        // 3. Thanh tìm kiếm: Lọc lại danh sách khi người dùng gõ
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
    }

    private void applyAllFilters() {
        if (adapter == null) return;

        // Lấy tất cả các giá trị từ các điều khiển
        String searchQuery = searchView.getQuery().toString();
        String selectedFacultyName = autoCompleteFaculty.getText().toString();
        String group = autoCompleteGroup.getText().toString();
        String courseType = autoCompleteCourseType.getText().toString();

        int facultyId = -1; // Mặc định là 'Tất cả'
        if (!selectedFacultyName.equals(getString(R.string.all_faculties))) {
            Integer id = facultiesMap.get(selectedFacultyName);
            if (id != null) {
                facultyId = id;
            }
        }

        if (group.equals(getString(R.string.all_groups))) {
            group = "All"; // Sử dụng "All" hoặc một giá trị đặc biệt mà adapter của bạn hiểu
        }
            
        adapter.filterAndSort(searchQuery, facultyId, group, courseType, isSortAscending);
    }

    private void loadCurriculumData() {
        List<Curriculum> allCourses = dbHelper.getAllCoursesForCurriculumWithStatus(1);
        adapter = new CurriculumAdapter(allCourses);
        recyclerViewCurriculum.setAdapter(adapter);
        applyAllFilters();
    }
}