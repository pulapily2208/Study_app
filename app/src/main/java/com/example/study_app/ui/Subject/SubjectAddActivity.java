package com.example.study_app.ui.Subject;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.study_app.R;
import com.example.study_app.data.DatabaseHelper;
import com.example.study_app.ui.Subject.Model.Subject;

import java.util.Calendar;
import java.util.Locale;

public class SubjectAddActivity extends AppCompatActivity {

    private EditText etSubjectCode, etSubjectName, etLecturerName, etCredits, etStartDate, etEndDate, etStartTime, etEndTime, etLocation, etNotes;
    private Spinner spinnerNumberOfWeeks;
    private Button btnMajor, btnGeneral;
    private ImageView ivSave, ivBack;

    private String subjectType = "Chuyên ngành"; // Default value

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subject_add);

        dbHelper = new DatabaseHelper(this);

        anhXa();
        setupSpinner();
        setupEventListeners();
        updateButtonSelection();
    }

    private void anhXa() {
        etSubjectCode = findViewById(R.id.etSubjectCode);
        etSubjectName = findViewById(R.id.etSubjectName);
        etLecturerName = findViewById(R.id.etLecturerName);
        etCredits = findViewById(R.id.etCredits);
        etLocation = findViewById(R.id.etLocation);
        etNotes = findViewById(R.id.etNotes);

        etStartDate = findViewById(R.id.etStartDate);
        etEndDate = findViewById(R.id.etEndDate);
        etStartTime = findViewById(R.id.etStartTime);
        etEndTime = findViewById(R.id.etEndTime);

        spinnerNumberOfWeeks = findViewById(R.id.spinnerNumberOfWeeks);

        btnMajor = findViewById(R.id.btnMajor);
        btnGeneral = findViewById(R.id.btnGeneral);

        ivSave = findViewById(R.id.ivSave);
        ivBack = findViewById(R.id.ivBack);
    }

    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.weeks_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerNumberOfWeeks.setAdapter(adapter);
    }

    private void setupEventListeners() {
        ivBack.setOnClickListener(v -> finish());
        ivSave.setOnClickListener(v -> saveSubject());

        etStartDate.setOnClickListener(v -> showDatePickerDialog(etStartDate));
        etEndDate.setOnClickListener(v -> showDatePickerDialog(etEndDate));
        etStartTime.setOnClickListener(v -> showTimePickerDialog(etStartTime));
        etEndTime.setOnClickListener(v -> showTimePickerDialog(etEndTime));

        btnMajor.setOnClickListener(v -> {
            subjectType = "Chuyên ngành";
            updateButtonSelection();
        });

        btnGeneral.setOnClickListener(v -> {
            subjectType = "Môn chung";
            updateButtonSelection();
        });
    }

    private void updateButtonSelection() {
        if ("Chuyên ngành".equals(subjectType)) {
            btnMajor.setBackgroundColor(ContextCompat.getColor(this, R.color.yellow));
            btnGeneral.setBackgroundColor(ContextCompat.getColor(this, android.R.color.darker_gray));
        } else {
            btnMajor.setBackgroundColor(ContextCompat.getColor(this, android.R.color.darker_gray));
            btnGeneral.setBackgroundColor(ContextCompat.getColor(this, R.color.yellow));
        }
    }

    private void showDatePickerDialog(final EditText editText) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, monthOfYear + 1, year1);
                    editText.setText(selectedDate);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void showTimePickerDialog(final EditText editText) {
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minute1) -> {
                    String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute1);
                    editText.setText(selectedTime);
                }, hour, minute, true);
        timePickerDialog.show();
    }

    private void saveSubject() {
        String maMon = etSubjectCode.getText().toString().trim();
        String tenMon = etSubjectName.getText().toString().trim();

        if (TextUtils.isEmpty(maMon)) {
            Toast.makeText(this, "Vui lòng nhập mã môn học", Toast.LENGTH_SHORT).show();
            etSubjectCode.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(tenMon)) {
            Toast.makeText(this, "Vui lòng nhập tên môn học", Toast.LENGTH_SHORT).show();
            etSubjectName.requestFocus();
            return;
        }

        String phongHoc = etLocation.getText().toString().trim();
        String ngayBatDau = etStartDate.getText().toString().trim();
        String ngayKetThuc = etEndDate.getText().toString().trim();
        int soTuanHoc = 0;
        if(spinnerNumberOfWeeks.getSelectedItem() != null) {
            soTuanHoc = Integer.parseInt(spinnerNumberOfWeeks.getSelectedItem().toString());
        }
        String gioBatDau = etStartTime.getText().toString().trim();
        String gioKetThuc = etEndTime.getText().toString().trim();
        String giangVien = etLecturerName.getText().toString().trim();
        int soTinChi = 0;
        try {
            soTinChi = Integer.parseInt(etCredits.getText().toString().trim());
        } catch (NumberFormatException e) {
            // ignore if empty or invalid
        }
        String ghiChu = etNotes.getText().toString().trim();

        // Create new Subject object
        Subject newSubject = new Subject(maMon, tenMon, phongHoc, subjectType, ngayBatDau, ngayKetThuc, soTuanHoc, gioBatDau, gioKetThuc, giangVien, soTinChi, ghiChu);

        // Save to DATABASE using DatabaseHelper
        dbHelper.addSubject(newSubject);

        Toast.makeText(this, "Đã lưu môn học: " + newSubject.tenHp, Toast.LENGTH_LONG).show();
        finish(); // Close activity after saving
    }
}
