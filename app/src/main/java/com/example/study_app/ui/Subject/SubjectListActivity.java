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
import com.example.study_app.ui.common.NavbarHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.study_app.ui.common.NavbarHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

public class SubjectListActivity extends AppCompatActivity implements SubjectAdapter.OnSubjectActionClickListener {

    private RecyclerView recyclerViewMonHoc;
    private SubjectAdapter adapterMonHoc;
    private SubjectDao subjectDao;
    private ArrayList<Subject> danhSachMonHoc;
    private Spinner spinnerHocKy;
    private TextView tvDanhSachRong;
    private String tenHocKyDuocChon;
    private FloatingActionButton fabThemMonHoc;
    private ImageButton btnNhapHangLoatMonHoc;
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

        recyclerViewMonHoc = findViewById(R.id.recyclerViewSubjects);
        spinnerHocKy = findViewById(R.id.spinnerSemesters);
        tvDanhSachRong = findViewById(R.id.tvEmptyList);
        fabThemMonHoc = findViewById(R.id.fab_add_subject);
        btnNhapHangLoatMonHoc = findViewById(R.id.btn_bulk_add_subject);

        dbHelper = new DatabaseHelper(this);
        subjectDao = new SubjectDao(dbHelper);

        danhSachMonHoc = new ArrayList<>();

        adapterMonHoc = new SubjectAdapter(danhSachMonHoc, this);
        recyclerViewMonHoc.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewMonHoc.setAdapter(adapterMonHoc);

        spinnerHocKy.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                tenHocKyDuocChon = parent.getItemAtPosition(position).toString();
                taiDanhSachMonHocTheoHocKy();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                tenHocKyDuocChon = null;
            }
        });

        fabThemMonHoc.setOnClickListener(v -> {
            if (tenHocKyDuocChon != null && !tenHocKyDuocChon.isEmpty()) {
                Intent intent = new Intent(this, SubjectAddActivity.class);
                intent.putExtra("SEMESTER_NAME", tenHocKyDuocChon);
                startActivityForResult(intent, ADD_SUBJECT_REQUEST);
            } else {
                Toast.makeText(this, "Vui lòng chọn một học kỳ trước khi thêm.", Toast.LENGTH_SHORT).show();
            }
        });

        btnNhapHangLoatMonHoc.setOnClickListener(v -> {
            if (tenHocKyDuocChon != null && !tenHocKyDuocChon.isEmpty()) {
                Intent intent = new Intent(this, SubjectBulkImportActivity.class);
                intent.putExtra("SEMESTER_NAME", tenHocKyDuocChon);
                startActivityForResult(intent, BULK_IMPORT_REQUEST);
            } else {
                Toast.makeText(this, "Vui lòng chọn một học kỳ trước.", Toast.LENGTH_SHORT).show();
            }
        });

        NavbarHelper.setupNavbar(this, R.id.btnSubject);
    }

    @Override
    protected void onResume() {
        super.onResume();
        taiDanhSachHocKy();
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
                    tenHocKyDuocChon = updatedSemester;
                    taiDanhSachHocKy();
                }
            } else {
                taiDanhSachHocKy();
            }
        }
    }

    private int tinhThuTuHocKyHienTai() {
        int startYear = 2023;
        int startMonth = 9;

        Calendar now = Calendar.getInstance();
        int currentYear = now.get(Calendar.YEAR);
        int currentMonth = now.get(Calendar.MONTH) + 1;

        int academicYearOfStart = (startMonth < 8) ? startYear - 1 : startYear;
        int academicYearOfNow = (currentMonth < 8) ? currentYear - 1 : currentYear;

        int academicYearsPassed = academicYearOfNow - academicYearOfStart;
        int semesterOrdinal = academicYearsPassed * 2;

        if (currentMonth >= 8 || currentMonth <= 1) {
            semesterOrdinal += 1;
        } else {
            semesterOrdinal += 2;
        }
        return semesterOrdinal;
    }

    /**
     * Lấy tên học kỳ hiện tại từ CSDL theo ngày hôm nay bằng TimetableDao.
     * Trả về: Tên học kỳ dạng "Học kỳ X" hoặc null nếu không tìm thấy.
     */
    private String layTenHocKyHienTaiTuDb() {
        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        int monthZeroBased = now.get(Calendar.MONTH); // 0-11

        TimetableDao timetableDao = new TimetableDao(dbHelper);
        Integer semesterId = timetableDao.getSemesterIdBySelectedDate(year, monthZeroBased);
        if (semesterId == null)
            return null;
        return timetableDao.getSemesterNameById(semesterId);
    }

    private void taiDanhSachHocKy() {
        String previouslySelected = tenHocKyDuocChon;
        ArrayList<String> semesterNames = subjectDao.getAllSemesterNames();

        if (semesterNames.isEmpty()) {
            danhSachMonHoc.clear();
            adapterMonHoc.notifyDataSetChanged();
            kiemTraTrangThaiRong();
            ArrayAdapter<String> emptyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                    new ArrayList<>());
            spinnerHocKy.setAdapter(emptyAdapter);
            return;
        }

        ArrayAdapter<String> semesterAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                semesterNames);
        semesterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerHocKy.setAdapter(semesterAdapter);

        int selectionIndex = -1;

        if (previouslySelected != null && semesterNames.contains(previouslySelected)) {
            selectionIndex = semesterAdapter.getPosition(previouslySelected);
        } else {
            String currentSemesterName = layTenHocKyHienTaiTuDb();
            if (currentSemesterName != null && semesterNames.contains(currentSemesterName)) {
                selectionIndex = semesterAdapter.getPosition(currentSemesterName);
            } else {
                ArrayList<Integer> semesterNumbers = new ArrayList<>();
                for (String name : semesterNames) {
                    try {
                        int num = Integer.parseInt(name.replaceAll("\\D+", ""));
                        semesterNumbers.add(num);
                    } catch (NumberFormatException e) {
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

        if (selectionIndex == -1 && !semesterNames.isEmpty()) {
            selectionIndex = 0;
        }

        if (selectionIndex != -1) {
            spinnerHocKy.setSelection(selectionIndex);
        }
    }

    private void taiDanhSachMonHocTheoHocKy() {
        if (tenHocKyDuocChon != null) {
            ArrayList<Subject> updatedSubjects = subjectDao.getSubjectsBySemester(tenHocKyDuocChon);
            danhSachMonHoc.clear();
            danhSachMonHoc.addAll(updatedSubjects);
            adapterMonHoc.notifyDataSetChanged();
            kiemTraTrangThaiRong();
        } else {
            danhSachMonHoc.clear();
            adapterMonHoc.notifyDataSetChanged();
            kiemTraTrangThaiRong();
        }
    }

    private void kiemTraTrangThaiRong() {
        if (danhSachMonHoc.isEmpty()) {
            recyclerViewMonHoc.setVisibility(View.GONE);
            tvDanhSachRong.setVisibility(View.VISIBLE);
        } else {
            recyclerViewMonHoc.setVisibility(View.VISIBLE);
            tvDanhSachRong.setVisibility(View.GONE);
        }
    }

    @Override
    public void onEdit(Subject subject) {
        Intent intent = new Intent(this, SubjectAddActivity.class);
        intent.putExtra("SUBJECT_ID", subject.maHp);
        if (tenHocKyDuocChon != null && !tenHocKyDuocChon.isEmpty()) {
            intent.putExtra("SEMESTER_NAME", tenHocKyDuocChon);
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
                    taiDanhSachMonHocTheoHocKy();
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
