package com.example.study_app.ui.Subject;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.study_app.R;
import com.example.study_app.data.DatabaseHelper;
import com.example.study_app.ui.Subject.Model.Subject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SubjectAddActivity extends AppCompatActivity {

    private EditText etSubjectCode, etSubjectName, etLecturerName, etCredits, etStartDate, etEndDate, etStartTime, etEndTime, etLocation, etNotes;
    private RadioGroup rgSubjectType;
    private RadioButton rbMajor, rbGeneral;
    private Spinner spinnerNumberOfWeeks;
    private LinearLayout colorPickerContainer;
    private ImageView ivBack, ivSave;
    private TextView tvActivityTitle;

    private DatabaseHelper dbHelper;
    private boolean isEditMode = false;
    private String maHpToEdit = null;
    private String currentSemesterName;

    private String selectedColor = null;
    private final List<View> colorViews = new ArrayList<>();
    private View selectedColorView = null;
    private ArrayAdapter<String> weeksAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subject_add);

        dbHelper = new DatabaseHelper(this);
        findViews();
        setClickListeners();
        setupWeeksSpinner(); // Thêm hàm này
        setupColorPicker();


        if (getIntent().hasExtra("SEMESTER_NAME")) {
            currentSemesterName = getIntent().getStringExtra("SEMESTER_NAME");
        } else {
            Toast.makeText(this, R.string.error_no_semester_info, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        checkForEditOrAddMode();

        // Mặc định cho mode Thêm mới
        if (!isEditMode) {
            if (!colorViews.isEmpty()) {
                colorViews.get(0).performClick();
            }
            rbGeneral.setChecked(true); // Mặc định là môn đại cương
            spinnerNumberOfWeeks.setSelection(0); // Mặc định là 15 tuần
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
        rbMajor = findViewById(R.id.rbMajor);
        rbGeneral = findViewById(R.id.rbGeneral);
        spinnerNumberOfWeeks = findViewById(R.id.spinnerNumberOfWeeks);
        colorPickerContainer = findViewById(R.id.colorPickerContainer);
        ivBack = findViewById(R.id.ivBack);
        ivSave = findViewById(R.id.ivSave);
        tvActivityTitle = findViewById(R.id.tvActivityTitle);
    }

    private void setClickListeners() {
        ivBack.setOnClickListener(v -> finish());
        ivSave.setOnClickListener(v -> saveSubject());

        etStartDate.setOnClickListener(v -> showDatePickerDialog(etStartDate));
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

            Subject subject = dbHelper.getSubjectByMaHp(maHpToEdit);
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

        if (subject.ngayBatDau != null) etStartDate.setText(dateFormat.format(subject.ngayBatDau));
        if (subject.ngayKetThuc != null) etEndDate.setText(dateFormat.format(subject.ngayKetThuc));
        if (subject.gioBatDau != null) etStartTime.setText(timeFormat.format(subject.gioBatDau));
        if (subject.gioKetThuc != null) etEndTime.setText(timeFormat.format(subject.gioKetThuc));

        // Sửa lỗi: chọn đúng radio button
        if (getString(R.string.subject_type_major).equals(subject.loaiMon)) {
            rbMajor.setChecked(true);
        } else {
            rbGeneral.setChecked(true);
        }

        // Sửa lỗi: Hiển thị đúng số tuần đã lưu
        if (weeksAdapter != null) {
            int weekPosition = weeksAdapter.getPosition(String.valueOf(subject.soTuan));
            if (weekPosition >= 0) {
                spinnerNumberOfWeeks.setSelection(weekPosition);
            }
        }

        // Sửa lỗi CRASH: Thêm kiểm tra null cho màu sắc
        if (subject.mauSac != null && !subject.mauSac.isEmpty()) {
            boolean colorFound = false;
            for (View colorView : colorViews) {
                if (colorView.getTag() != null && colorView.getTag().toString().equalsIgnoreCase(subject.mauSac)) {
                    selectColor(colorView);
                    colorFound = true;
                    break;
                }
            }
            // Nếu không tìm thấy màu đã lưu, chọn màu mặc định
            if (!colorFound && !colorViews.isEmpty()) {
                colorViews.get(0).performClick();
            }
        } else if (!colorViews.isEmpty()) {
            // Nếu môn học cũ không có màu, chọn màu mặc định
            colorViews.get(0).performClick();
        }
    }

    private void saveSubject() {
        // --- Xác thực dữ liệu (giữ nguyên) ---
        String maHp = etSubjectCode.getText().toString().trim();
        if (TextUtils.isEmpty(maHp)) {
            Toast.makeText(this, R.string.subject_code_required, Toast.LENGTH_SHORT).show();
            etSubjectCode.requestFocus();
            return;
        }
        String tenHp = etSubjectName.getText().toString().trim();
        if (TextUtils.isEmpty(tenHp)) {
            Toast.makeText(this, R.string.subject_name_required, Toast.LENGTH_SHORT).show();
            etSubjectName.requestFocus();
            return;
        }
        String soTcStr = etCredits.getText().toString().trim();
        if (TextUtils.isEmpty(soTcStr)) {
            Toast.makeText(this, R.string.credits_required, Toast.LENGTH_SHORT).show();
            etCredits.requestFocus();
            return;
        }
         if (TextUtils.isEmpty(etStartDate.getText().toString())) {
            Toast.makeText(this, R.string.start_date_required, Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(etEndDate.getText().toString())) {
            Toast.makeText(this, R.string.end_date_required, Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(etStartTime.getText().toString())) {
            Toast.makeText(this, R.string.start_time_required, Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(etEndTime.getText().toString())) {
            Toast.makeText(this, R.string.end_time_required, Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedColor == null) {
            Toast.makeText(this, R.string.color_required, Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedRadioButtonId = rgSubjectType.getCheckedRadioButtonId();
        if (selectedRadioButtonId == -1) {
            Toast.makeText(this, R.string.subject_type_required, Toast.LENGTH_SHORT).show();
            return;
        }

        // --- Xử lý dữ liệu ---
        RadioButton selectedRadioButton = findViewById(selectedRadioButtonId);
        String loaiMon = selectedRadioButton.getText().toString();
        String tenGv = etLecturerName.getText().toString().trim();
        String ghiChu = etNotes.getText().toString().trim();
        String phongHoc = etLocation.getText().toString().trim();
        int soTc;
        try {
            soTc = Integer.parseInt(soTcStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, R.string.credits_invalid, Toast.LENGTH_SHORT).show();
            etCredits.requestFocus();
            return;
        }
        
        // Sửa lỗi: Lấy đúng giá trị từ Spinner
        int soTuan = 15; // Giá trị mặc định
        try {
            soTuan = Integer.parseInt(spinnerNumberOfWeeks.getSelectedItem().toString());
        } catch (Exception e) {
            Log.e("SubjectAddActivity", "Không thể lấy số tuần", e);
            Toast.makeText(this, "Lỗi: Không thể lấy số tuần", Toast.LENGTH_SHORT).show();
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        Date ngayBatDau, ngayKetThuc, gioBatDau, gioKetThuc;
        try {
            ngayBatDau = dateFormat.parse(etStartDate.getText().toString());
            ngayKetThuc = dateFormat.parse(etEndDate.getText().toString());
            gioBatDau = timeFormat.parse(etStartTime.getText().toString());
            gioKetThuc = timeFormat.parse(etEndTime.getText().toString());
        } catch (ParseException e) {
            Toast.makeText(this, R.string.date_time_format_invalid, Toast.LENGTH_SHORT).show();
            return;
        }

        // --- Tương tác với cơ sở dữ liệu ---
        Subject subject = new Subject();
        subject.maHp = maHp;
        subject.tenHp = tenHp;
        subject.tenGv = tenGv;
        subject.soTc = soTc;
        subject.ghiChu = ghiChu;
        subject.phongHoc = phongHoc;
        subject.ngayBatDau = ngayBatDau;
        subject.ngayKetThuc = ngayKetThuc;
        subject.gioBatDau = gioBatDau;
        subject.gioKetThuc = gioKetThuc;
        subject.loaiMon = loaiMon;
        subject.mauSac = selectedColor;
        subject.soTuan = soTuan; // Sửa lỗi: lưu số tuần
        subject.tenHk = currentSemesterName;

        if (isEditMode) {
            int rowsAffected = dbHelper.updateSubject(subject);
            if (rowsAffected > 0) {
                Toast.makeText(this, R.string.update_subject_success, Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, R.string.update_subject_failed, Toast.LENGTH_SHORT).show();
            }
        } else {
            if (dbHelper.getSubjectByMaHp(maHp) != null) {
                Toast.makeText(this, R.string.subject_code_exists, Toast.LENGTH_SHORT).show();
                return;
            }
            
            long newRowId = dbHelper.addSubject(subject);
            if (newRowId != -1) {
                Toast.makeText(this, R.string.add_subject_success, Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, R.string.add_subject_failed, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showDatePickerDialog(final EditText editText) {
        //... giữ nguyên
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, monthOfYear, dayOfMonth) -> {
            String selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, monthOfYear + 1, year1);
            editText.setText(selectedDate);
        }, year, month, day);
        datePickerDialog.show();
    }

    private void showTimePickerDialog(final EditText editText) {
        //... giữ nguyên
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minuteOfHour) -> {
            String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minuteOfHour);
            editText.setText(selectedTime);
        }, hour, minute, true);
        timePickerDialog.show();
    }
    
    // Hàm mới để setup Spinner
    private void setupWeeksSpinner() {
        try {
            String[] weeks = getResources().getStringArray(R.array.weeks_array);
            weeksAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, weeks);
            weeksAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerNumberOfWeeks.setAdapter(weeksAdapter);
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi: không thể tải danh sách tuần.", Toast.LENGTH_SHORT).show();
            // Cung cấp một adapter rỗng để tránh crash
             weeksAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"15"});
             spinnerNumberOfWeeks.setAdapter(weeksAdapter);
        }
    }

    private void setupColorPicker() {
        //... giữ nguyên
        String[] colors;
        try {
            colors = getResources().getStringArray(R.array.subject_colors);
        } catch (Exception e) {
            Toast.makeText(this, R.string.error_color_picker, Toast.LENGTH_SHORT).show();
            colors = new String[]{"#CCCCCC"};
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