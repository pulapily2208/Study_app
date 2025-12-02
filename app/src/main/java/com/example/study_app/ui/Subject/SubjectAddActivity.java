package com.example.study_app.ui.Subject;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.study_app.R;
import com.example.study_app.data.CurriculumDao;
import com.example.study_app.data.DatabaseHelper;
import com.example.study_app.data.SubjectDao;
import com.example.study_app.ui.Curriculum.Model.Curriculum;
import com.example.study_app.ui.Subject.Model.Subject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class SubjectAddActivity extends AppCompatActivity {

    private AutoCompleteTextView etSubjectCode;
    private EditText etSubjectName, etLecturerName, etCredits, etStartDate, etEndDate, etStartTime, etEndTime,
            etLocation, etNotes;
    private RadioGroup rgSubjectType;
    private RadioButton rbCompulsory, rbElective;
    private TextView tvCalculatedWeeks, tvChoiceGroup;
    private LinearLayout colorPickerContainer, layoutStartDate, layoutEndDate;
    private ImageView ivBack, ivSave;
    private TextView tvActivityTitle;

    private DatabaseHelper dbHelper;
    private SubjectDao subjectDao;
    private CurriculumDao curriculumDao;
    private boolean isEditMode = false;
    private String maHpToEdit = null;
    private String currentSemesterName;

    private String selectedColor = null;
    private final List<View> colorViews = new ArrayList<>();
    private View selectedColorView = null;

    private Calendar startDateCalendar = null;
    private Calendar endDateCalendar = null;
    private int calculatedWeeks = 0;
    private ArrayAdapter<String> subjectCodeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.subject_add);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DatabaseHelper(this);
        subjectDao = new SubjectDao(dbHelper);
        curriculumDao = new CurriculumDao(dbHelper);
        findViews();
        setupAutoComplete();
        setClickListeners();
        setupColorPicker();

        tvChoiceGroup.setVisibility(View.GONE);

        if (getIntent().hasExtra("SEMESTER_NAME")) {
            currentSemesterName = getIntent().getStringExtra("SEMESTER_NAME");
        } else {
            Toast.makeText(this, R.string.error_no_semester_info, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Nếu được truyền `selectedDate` từ TimetableWeek (format: yyyy-MM-dd),
        // tiền điền `etStartDate` và set `startDateCalendar` để tránh mất ngày khi thêm
        // môn từ timetable.
        if (getIntent().hasExtra("selectedDate")) {
            String selectedDateStr = getIntent().getStringExtra("selectedDate");
            if (selectedDateStr != null && !selectedDateStr.isEmpty()) {
                try {
                    java.text.SimpleDateFormat dbFormat = new java.text.SimpleDateFormat("yyyy-MM-dd",
                            java.util.Locale.getDefault());
                    java.util.Date parsed = dbFormat.parse(selectedDateStr);
                    if (parsed != null) {
                        startDateCalendar = java.util.Calendar.getInstance();
                        startDateCalendar.setTime(parsed);
                        java.text.SimpleDateFormat displayFormat = new java.text.SimpleDateFormat("dd/MM/yyyy",
                                java.util.Locale.getDefault());
                        etStartDate.setText(displayFormat.format(parsed));
                        // Tự động tính ngày kết thúc nếu có thông tin số tuần
                        autoCalculateEndDate();
                    }
                } catch (java.text.ParseException e) {
                    // Ignore invalid format
                }
            }
        }

        checkForEditOrAddMode();

        if (!isEditMode) {
            if (!colorViews.isEmpty()) {
                colorViews.get(0).performClick();
            }
            rbCompulsory.setChecked(true);
        }
    }

    private void findViews() {
        etSubjectCode = findViewById(R.id.etSubjectCode);
        etSubjectName = findViewById(R.id.etSubjectName);
        etLecturerName = findViewById(R.id.etLecturerName);
        etCredits = findViewById(R.id.etCredits);
        etStartDate = findViewById(R.id.etStartDate);
        etEndDate = findViewById(R.id.etEndDate);
        etStartTime = findViewById(R.id.etStartTime);
        etEndTime = findViewById(R.id.etEndTime);
        etLocation = findViewById(R.id.etLocation);
        etNotes = findViewById(R.id.etNotes);
        rgSubjectType = findViewById(R.id.rgSubjectType);
        rbCompulsory = findViewById(R.id.rb_compulsory);
        rbElective = findViewById(R.id.rb_elective);
        tvCalculatedWeeks = findViewById(R.id.tvCalculatedWeeks);
        tvChoiceGroup = findViewById(R.id.tvChoiceGroup);
        colorPickerContainer = findViewById(R.id.colorPickerContainer);
        ivBack = findViewById(R.id.ivBack);
        ivSave = findViewById(R.id.ivSave);
        tvActivityTitle = findViewById(R.id.tvActivityTitle);
        layoutStartDate = findViewById(R.id.layoutStartDate);
        layoutEndDate = findViewById(R.id.layoutEndDate);
    }

    private void setupAutoComplete() {
        subjectCodeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line);
        etSubjectCode.setAdapter(subjectCodeAdapter);

        etSubjectCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (etSubjectCode.isPerformingCompletion()) {
                    return;
                }
                List<String> suggestions = curriculumDao.searchSubjectCodes(s.toString());
                subjectCodeAdapter.clear();
                subjectCodeAdapter.addAll(suggestions);
                subjectCodeAdapter.notifyDataSetChanged();
            }
        });

        etSubjectCode.setOnItemClickListener((parent, view, position, id) -> {
            String selectedMaHp = subjectCodeAdapter.getItem(position);
            if (selectedMaHp != null) {
                Curriculum curriculum = curriculumDao.getCurriculumDetailsByMaHp(selectedMaHp);
                if (curriculum != null) {
                    autoFillSubjectDetails(curriculum);
                }
            }
        });

        etSubjectCode.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String currentCode = etSubjectCode.getText().toString().trim();
                if (!TextUtils.isEmpty(currentCode)) {
                    Curriculum curriculum = curriculumDao.getCurriculumDetailsByMaHp(currentCode);
                    if (curriculum == null) {
                        Toast.makeText(SubjectAddActivity.this, "Mã môn không tồn tại trong chương trình đào tạo!",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void autoFillSubjectDetails(Curriculum curriculum) {
        etSubjectName.setText(curriculum.getTenHp());
        etCredits.setText(String.valueOf(curriculum.getSoTinChi()));

        if (curriculum.getLoaiHp() != null) {
            if ("Tự chọn".equals(curriculum.getLoaiHp())) {
                rbElective.setChecked(true);
                tvChoiceGroup.setText("Nhóm: " + curriculum.getNhomTuChon());
                tvChoiceGroup.setVisibility(View.VISIBLE);
            } else {
                rbCompulsory.setChecked(true);
                tvChoiceGroup.setText("");
                tvChoiceGroup.setVisibility(View.GONE);
            }
        } else {
            rbCompulsory.setChecked(true);
            tvChoiceGroup.setText("");
            tvChoiceGroup.setVisibility(View.GONE);
        }

        int totalPeriods = curriculum.getSoTietLyThuyet() + curriculum.getSoTietThucHanh();
        int credits = curriculum.getSoTinChi();
        String codeUpper = etSubjectCode.getText() != null ? etSubjectCode.getText().toString().trim().toUpperCase()
                : "";
        if (codeUpper.startsWith("DEFE")) {
            calculatedWeeks = 4;
        } else if (codeUpper.startsWith("PHYE")) {
            calculatedWeeks = 7;
        } else if (credits > 0) {
            calculatedWeeks = totalPeriods / credits;
        } else {
            calculatedWeeks = 0;
        }
        tvCalculatedWeeks.setText(String.valueOf(calculatedWeeks));

        if (startDateCalendar != null) {
            autoCalculateEndDate();
        }
    }

    private void autoCalculateEndDate() {
        if (startDateCalendar != null && calculatedWeeks > 0) {
            endDateCalendar = (Calendar) startDateCalendar.clone();
            endDateCalendar.add(Calendar.DAY_OF_YEAR, (calculatedWeeks * 7) - 1);

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            etEndDate.setText(dateFormat.format(endDateCalendar.getTime()));
        } else {
            etEndDate.setText("");
            endDateCalendar = null;
        }
    }

    private void setClickListeners() {
        ivBack.setOnClickListener(v -> finish());
        ivSave.setOnClickListener(v -> saveSubject());

        layoutStartDate.setOnClickListener(v -> showDatePickerDialog(etStartDate));
        etStartDate.setOnClickListener(v -> showDatePickerDialog(etStartDate));
        layoutEndDate.setOnClickListener(v -> showDatePickerDialog(etEndDate));
        etEndDate.setOnClickListener(v -> showDatePickerDialog(etEndDate));

        etStartTime.setOnClickListener(v -> showTimePickerDialog(etStartTime));
        etEndTime.setOnClickListener(v -> showTimePickerDialog(etEndTime));
    }

    private void checkForEditOrAddMode() {
        if (getIntent().hasExtra("SUBJECT_ID")) {
            isEditMode = true;
            maHpToEdit = getIntent().getStringExtra("SUBJECT_ID");
            tvActivityTitle.setText(R.string.activity_title_edit);
            etSubjectCode.setEnabled(false);

            Subject subject = subjectDao.getSubjectByMaHp(maHpToEdit);
            if (subject != null) {
                populateUI(subject);
            } else {
                Toast.makeText(this, R.string.error_subject_not_found, Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            isEditMode = false;
            tvActivityTitle.setText(R.string.activity_title_add);
        }
    }

    private void populateUI(Subject subject) {
        etSubjectCode.setText(subject.maHp);
        etSubjectName.setText(subject.tenHp);
        etLecturerName.setText(subject.tenGv);
        etCredits.setText(String.valueOf(subject.soTc));
        etNotes.setText(subject.ghiChu);
        etLocation.setText(subject.phongHoc);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        if (subject.ngayBatDau != null) {
            etStartDate.setText(dateFormat.format(subject.ngayBatDau));
            startDateCalendar = Calendar.getInstance();
            startDateCalendar.setTime(subject.ngayBatDau);
        }
        if (subject.ngayKetThuc != null) {
            etEndDate.setText(dateFormat.format(subject.ngayKetThuc));
            endDateCalendar = Calendar.getInstance();
            endDateCalendar.setTime(subject.ngayKetThuc);
        }

        calculatedWeeks = subject.soTuan;
        tvCalculatedWeeks.setText(String.valueOf(calculatedWeeks));

        if (subject.gioBatDau != null)
            etStartTime.setText(timeFormat.format(subject.gioBatDau));
        if (subject.gioKetThuc != null)
            etEndTime.setText(timeFormat.format(subject.gioKetThuc));

        Curriculum curriculum = curriculumDao.getCurriculumDetailsByMaHp(subject.maHp);
        if ("Tự chọn".equals(subject.loaiMon)) {
            rbElective.setChecked(true);
            if (curriculum != null && curriculum.getNhomTuChon() != null && !curriculum.getNhomTuChon().isEmpty()) {
                tvChoiceGroup.setText("Nhóm: " + curriculum.getNhomTuChon());
                tvChoiceGroup.setVisibility(View.VISIBLE);
            } else {
                tvChoiceGroup.setVisibility(View.GONE);
            }
        } else {
            rbCompulsory.setChecked(true);
            tvChoiceGroup.setText("");
            tvChoiceGroup.setVisibility(View.GONE);
        }

        if (subject.mauSac != null && !subject.mauSac.isEmpty()) {
            for (View colorView : colorViews) {
                if (colorView.getTag() != null && colorView.getTag().toString().equalsIgnoreCase(subject.mauSac)) {
                    selectColor(colorView);
                    break;
                }
            }
        }
    }

    private void saveSubject() {
        Subject subject = new Subject();
        String error = validateInputsAndBuildSubject(subject);
        if (error != null) {
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            return;
        }

        String conflictMaHp = findTimeConflict(subject);
        if (conflictMaHp != null) {
            Toast.makeText(this, "Trùng giờ với môn: " + conflictMaHp, Toast.LENGTH_LONG).show();
            return;
        }

        Intent resultIntent = new Intent();

        if (isEditMode) {
            int rowsAffected = subjectDao.updateSubject(subject);
            if (rowsAffected > 0) {
                Toast.makeText(this, R.string.update_subject_success, Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                Toast.makeText(this, R.string.update_subject_failed, Toast.LENGTH_SHORT).show();
            }
        } else {
            long newRowId = subjectDao.addOrEnrollSubject(subject);
            if (newRowId != -1) {
                Toast.makeText(this, R.string.add_subject_success, Toast.LENGTH_SHORT).show();
                // Return the semester name and ma_hp so callers can refresh and select
                resultIntent.putExtra("UPDATED_SEMESTER_NAME", subject.tenHk);
                resultIntent.putExtra("UPDATED_MA_HP", subject.maHp);
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                Toast.makeText(this, R.string.add_subject_failed, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Thực hiện toàn bộ kiểm tra dữ liệu đầu vào.
     * Nếu hợp lệ: gán các trường vào đối tượng subject truyền vào và trả về null.
     * Nếu lỗi: trả về thông báo lỗi (String) và KHÔNG chỉnh sửa subject.
     */
    private String validateInputsAndBuildSubject(Subject subject) {
        String maHp = etSubjectCode.getText().toString().trim();
        if (TextUtils.isEmpty(maHp))
            return getString(R.string.subject_code_required);

        Curriculum subjectCurriculumDetails = curriculumDao.getCurriculumDetailsByMaHp(maHp);
        if (!isEditMode && subjectCurriculumDetails == null)
            return "Mã môn không tồn tại trong chương trình đào tạo!";
        if (!isEditMode && subjectCurriculumDetails != null) {
            final int CURRENT_USER_ID = 1;
            boolean isPrerequisiteMet = curriculumDao.checkPrerequisiteStatus(maHp, CURRENT_USER_ID, subjectDao);
            if (!isPrerequisiteMet)
                return "Chưa đạt điều kiện tiên quyết để thêm môn!";
        }

        String tenHp = etSubjectName.getText().toString().trim();
        if (TextUtils.isEmpty(tenHp))
            return getString(R.string.subject_name_required);

        String soTcStr = etCredits.getText().toString().trim();
        if (TextUtils.isEmpty(soTcStr))
            return getString(R.string.credits_required);
        int soTc;
        try {
            soTc = Integer.parseInt(soTcStr);
        } catch (NumberFormatException e) {
            return getString(R.string.credits_invalid);
        }

        ensureEndDateFallback();
        if (startDateCalendar == null || TextUtils.isEmpty(etStartDate.getText().toString()))
            return getString(R.string.start_date_required);
        if (endDateCalendar == null || TextUtils.isEmpty(etEndDate.getText().toString()))
            return getString(R.string.end_date_required);
        if (endDateCalendar.before(startDateCalendar))
            return "Ngày kết thúc không thể trước ngày bắt đầu";

        if (TextUtils.isEmpty(etStartTime.getText().toString()))
            return getString(R.string.start_time_required);
        if (TextUtils.isEmpty(etEndTime.getText().toString()))
            return getString(R.string.end_time_required);

        if (selectedColor == null)
            return getString(R.string.color_required);
        int selectedRadioButtonId = rgSubjectType.getCheckedRadioButtonId();
        if (selectedRadioButtonId == -1)
            return getString(R.string.subject_type_required);
        RadioButton selectedRadioButton = findViewById(selectedRadioButtonId);
        String loaiMon = selectedRadioButton.getText().toString();

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        Date gioBatDau, gioKetThuc;
        try {
            gioBatDau = timeFormat.parse(etStartTime.getText().toString());
            gioKetThuc = timeFormat.parse(etEndTime.getText().toString());
        } catch (ParseException e) {
            return getString(R.string.date_time_format_invalid);
        }
        if (!gioBatDau.before(gioKetThuc))
            return "Giờ bắt đầu phải trước giờ kết thúc";

        subject.maHp = maHp;
        subject.tenHp = tenHp;
        subject.tenGv = etLecturerName.getText().toString().trim();
        subject.soTc = soTc;
        subject.ghiChu = etNotes.getText().toString().trim();
        subject.phongHoc = etLocation.getText().toString().trim();
        subject.ngayBatDau = startDateCalendar.getTime();
        subject.ngayKetThuc = endDateCalendar.getTime();
        subject.gioBatDau = gioBatDau;
        subject.gioKetThuc = gioKetThuc;
        subject.loaiMon = loaiMon;
        subject.mauSac = selectedColor;
        subject.soTuan = calculatedWeeks;
        subject.tenHk = currentSemesterName;
        return null;
    }

    // Tự động tính ngày kết thúc nếu người dùng đã chọn mã môn (có số tuần) và ngày
    // bắt đầu nhưng chưa nhập ngày kết thúc
    private void ensureEndDateFallback() {
        if ((endDateCalendar == null || TextUtils.isEmpty(etEndDate.getText().toString()))
                && startDateCalendar != null && calculatedWeeks > 0) {
            autoCalculateEndDate();
        }
    }

    // Tìm mã môn gây xung đột giờ học; trả về null nếu không có
    private String findTimeConflict(Subject newSubject) {
        List<Subject> existing = subjectDao.getSubjectsBySemester(currentSemesterName);
        if (existing == null || existing.isEmpty())
            return null;

        Calendar newStartDateCal = Calendar.getInstance();
        newStartDateCal.setTime(newSubject.ngayBatDau);
        Calendar newEndDateCal = Calendar.getInstance();
        newEndDateCal.setTime(newSubject.ngayKetThuc);

        for (Subject s : existing) {
            if (s == null || s.maHp == null)
                continue;
            if (s.maHp.equalsIgnoreCase(newSubject.maHp))
                continue; // bỏ qua chính nó khi chỉnh sửa
            if (s.ngayBatDau == null || s.ngayKetThuc == null || s.gioBatDau == null || s.gioKetThuc == null)
                continue;

            Calendar sStartDate = Calendar.getInstance();
            sStartDate.setTime(s.ngayBatDau);
            Calendar sEndDate = Calendar.getInstance();
            sEndDate.setTime(s.ngayKetThuc);

            // Kiểm tra khoảng ngày giao nhau
            boolean dateOverlap = !(newEndDateCal.getTime().before(sStartDate.getTime())
                    || newStartDateCal.getTime().after(sEndDate.getTime()));
            if (!dateOverlap)
                continue;

            // Chỉ xét trùng ngày trong tuần giống nhau (giả định học cố định mỗi tuần)
            if (newStartDateCal.get(Calendar.DAY_OF_WEEK) != sStartDate.get(Calendar.DAY_OF_WEEK))
                continue;

            // Kiểm tra overlap thời gian: (startA < endB) && (startB < endA)
            boolean timeOverlap = newSubject.gioBatDau.before(s.gioKetThuc)
                    && s.gioBatDau.before(newSubject.gioKetThuc);
            if (timeOverlap) {
                return s.maHp;
            }
        }
        return null;
    }

    private void showDatePickerDialog(final EditText editText) {
        Calendar initialCalendar = Calendar.getInstance();
        if (!TextUtils.isEmpty(editText.getText().toString())) {
            try {
                Date d = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(editText.getText().toString());
                if (d != null) {
                    initialCalendar.setTime(d);
                }
            } catch (ParseException e) {
                // Ignore
            }
        }

        int year = initialCalendar.get(Calendar.YEAR);
        int month = initialCalendar.get(Calendar.MONTH);
        int day = initialCalendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, monthOfYear, dayOfMonth) -> {
            String selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, monthOfYear + 1,
                    year1);
            editText.setText(selectedDate);

            Calendar selectedCalendar = Calendar.getInstance();
            selectedCalendar.set(year1, monthOfYear, dayOfMonth, 0, 0, 0);

            if (editText.getId() == R.id.etStartDate) {
                startDateCalendar = selectedCalendar;
                autoCalculateEndDate();
            } else if (editText.getId() == R.id.etEndDate) {
                endDateCalendar = selectedCalendar;
                calculateAndDisplayWeeks();
            }

        }, year, month, day);
        datePickerDialog.show();
    }

    private void calculateAndDisplayWeeks() {
        if (startDateCalendar != null && endDateCalendar != null) {
            if (endDateCalendar.before(startDateCalendar)) {
                tvCalculatedWeeks.setText("0");
                calculatedWeeks = 0;
                return;
            }
            long diffMillis = endDateCalendar.getTimeInMillis() - startDateCalendar.getTimeInMillis();
            long diffInDays = TimeUnit.MILLISECONDS.toDays(diffMillis);
            calculatedWeeks = (int) (diffInDays / 7) + 1;
            tvCalculatedWeeks.setText(String.valueOf(calculatedWeeks));
        } else {
            tvCalculatedWeeks.setText("0");
            calculatedWeeks = 0;
        }
    }

    private void showTimePickerDialog(final EditText editText) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minuteOfHour) -> {
            String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minuteOfHour);
            editText.setText(selectedTime);
        }, hour, minute, true);
        timePickerDialog.show();
    }

    private void setupColorPicker() {
        String[] colors;
        try {
            colors = getResources().getStringArray(R.array.subject_colors);
        } catch (Exception e) {
            Toast.makeText(this, R.string.error_color_picker, Toast.LENGTH_SHORT).show();
            colors = new String[] { "#CCCCCC" };
        }

        colorPickerContainer.removeAllViews();
        colorViews.clear();

        for (String colorHex : colors) {
            ImageView colorSwatch = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(100, 100);
            params.setMargins(8, 8, 8, 8);
            colorSwatch.setLayoutParams(params);

            GradientDrawable background = new GradientDrawable();
            background.setShape(GradientDrawable.OVAL);
            background.setColor(Color.parseColor(colorHex));
            background.setStroke(4, Color.TRANSPARENT);
            colorSwatch.setBackground(background);

            colorSwatch.setTag(colorHex);
            colorSwatch.setOnClickListener(this::selectColor);

            colorViews.add(colorSwatch);
            colorPickerContainer.addView(colorSwatch);
        }
    }

    private void selectColor(View view) {
        String newColor = (String) view.getTag();

        if (selectedColorView != null) {
            GradientDrawable oldBg = (GradientDrawable) selectedColorView.getBackground();
            oldBg.setStroke(4, Color.TRANSPARENT);
        }

        GradientDrawable newBg = (GradientDrawable) view.getBackground();
        newBg.setStroke(10, Color.BLACK);

        selectedColor = newColor;
        selectedColorView = view;
    }
}
