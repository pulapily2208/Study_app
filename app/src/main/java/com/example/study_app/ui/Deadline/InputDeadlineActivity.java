package com.example.study_app.ui.Deadline;

import android.app.Dialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.study_app.R;
import com.example.study_app.data.DatabaseHelper;
import com.example.study_app.ui.Deadline.Adapters.IconAdapter;
import com.example.study_app.ui.Deadline.Models.Deadline;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class InputDeadlineActivity extends AppCompatActivity {

    // Constants for Intent Extras
    public static final String KEY_TAI_KHOAN = "taiKhoanMoi";
    public static final String EDIT_DEADLINE = "EDIT_DEADLINE";
    public static final String SUBJECT_MA_HP = "SUBJECT_MA_HP";
    public static final String WEEK_START_DATE = "WEEK_START_DATE";

    // Views
    private EditText edtTenDeadline, edtGhiChu;
    private TextView txtNgayGioTu, txtNgayGioDen;
    private Button btnHuy, btnThemDeadline;
    private Switch switchCaNgay;
    private ImageView imgIcon;

    // Data & Helpers
    private DatabaseHelper dbHelper;
    private String subjectMaHp;
    private Deadline deadlineToEdit = null;
    private boolean isEditMode = false;
    private int weekIndex = 0; // Thêm lại weekIndex để trả về

    private final Calendar calendarTu = Calendar.getInstance();
    private final Calendar calendarDen = Calendar.getInstance();
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    private int selectedIcon = R.drawable.tasks; // Default icon
    private final int[] ICON_LIST = {
            R.drawable.brain,
            R.drawable.calendar,
            R.drawable.code,
            R.drawable.exam,
            R.drawable.library,
            R.drawable.note,
            R.drawable.tasks,
            R.drawable.teacher
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.deadline_input);

        dbHelper = new DatabaseHelper(this);
        findViews();
        handleIntent();
        setupListeners();

        updateDateTimeUI();
        imgIcon.setImageResource(selectedIcon);
    }

    private void findViews() {
        edtTenDeadline = findViewById(R.id.edtTenDeadline);
        edtGhiChu = findViewById(R.id.edtGhiChu);
        txtNgayGioTu = findViewById(R.id.txtNgayGioTu);
        txtNgayGioDen = findViewById(R.id.txtNgayGioDen);
        switchCaNgay = findViewById(R.id.switchCaNgay);
        btnHuy = findViewById(R.id.btnHuy);
        btnThemDeadline = findViewById(R.id.btnThemDeadline);
        imgIcon = findViewById(R.id.imgIcon);
    }

    private void handleIntent() {
        Intent intent = getIntent();
        weekIndex = intent.getIntExtra("weekIndex", 0);
        subjectMaHp = intent.getStringExtra(SUBJECT_MA_HP);

        if (intent.hasExtra(EDIT_DEADLINE)) {
            deadlineToEdit = (Deadline) intent.getSerializableExtra(EDIT_DEADLINE);
            isEditMode = (deadlineToEdit != null);
        }

        if (isEditMode) {
            btnThemDeadline.setText("Lưu thay đổi");
            populateDataForEdit();
        } else {
            btnThemDeadline.setText("Thêm Deadline");
            // Chỉ đặt ngày mặc định khi thêm mới
            long weekStartDateMillis = intent.getLongExtra(WEEK_START_DATE, -1);
            if (weekStartDateMillis != -1) {
                calendarTu.setTimeInMillis(weekStartDateMillis);
                calendarDen.setTimeInMillis(weekStartDateMillis);
            }
        }
    }

    private void setupListeners() {
        btnHuy.setOnClickListener(v -> finish());
        btnThemDeadline.setOnClickListener(v -> saveDeadline());

        LinearLayout layoutIcon = findViewById(R.id.layoutIcon);
        layoutIcon.setOnClickListener(v -> openIconDialog());

        txtNgayGioTu.setOnClickListener(v -> pickDateTime(calendarTu));
        txtNgayGioDen.setOnClickListener(v -> pickDateTime(calendarDen));

        switchCaNgay.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                calendarTu.set(Calendar.HOUR_OF_DAY, 0);
                calendarTu.set(Calendar.MINUTE, 0);
                calendarDen.setTime(calendarTu.getTime());
                calendarDen.set(Calendar.HOUR_OF_DAY, 23);
                calendarDen.set(Calendar.MINUTE, 59);
            }
            updateDateTimeUI();
        });
    }

    private void populateDataForEdit() {
        edtTenDeadline.setText(deadlineToEdit.getTieuDe());
        edtGhiChu.setText(deadlineToEdit.getNoiDung());
        calendarTu.setTime(deadlineToEdit.getNgayBatDau());
        calendarDen.setTime(deadlineToEdit.getNgayKetThuc());
        selectedIcon = deadlineToEdit.getIcon();
        subjectMaHp = deadlineToEdit.getMaHp(); // Lấy mã HP từ deadline đang sửa
    }

    private void saveDeadline() {
        String ten = edtTenDeadline.getText().toString().trim();
        if (ten.isEmpty()) {
            edtTenDeadline.setError("Tên deadline không được để trống");
            return;
        }

        String ghiChu = edtGhiChu.getText().toString().trim();
        Date tu = calendarTu.getTime();
        Date den = calendarDen.getTime();

        Deadline deadlineToReturn;
        if (isEditMode) {
            deadlineToReturn = deadlineToEdit;
        } else {
            deadlineToReturn = new Deadline();
        }

        deadlineToReturn.setTieuDe(ten);
        deadlineToReturn.setNoiDung(ghiChu);
        deadlineToReturn.setNgayBatDau(tu);
        deadlineToReturn.setNgayKetThuc(den);
        deadlineToReturn.setIcon(selectedIcon);
        deadlineToReturn.setMaHp(subjectMaHp);

        // Trả về Intent
        Intent resultIntent = new Intent();
        resultIntent.putExtra(KEY_TAI_KHOAN, deadlineToReturn);
        // Trả về weekIndex để MainDeadLine có thể cuộn tới
        if (!isEditMode) {
            resultIntent.putExtra("weekIndex", weekIndex);
        }
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private void updateDateTimeUI() {
        txtNgayGioTu.setText(sdf.format(calendarTu.getTime()));
        txtNgayGioDen.setText(sdf.format(calendarDen.getTime()));
    }

    private void pickDateTime(final Calendar calendar) {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            new TimePickerDialog(this, (timePicker, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                updateDateTimeUI();
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();

        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void openIconDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.deadline_dialog_icon);
        GridView gridView = dialog.findViewById(R.id.gridIcons);
        IconAdapter adapter = new IconAdapter(this, ICON_LIST);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener((parent, view, position, id) -> {
            selectedIcon = ICON_LIST[position];
            imgIcon.setImageResource(selectedIcon);
            dialog.dismiss();
        });
        dialog.show();
    }
}
