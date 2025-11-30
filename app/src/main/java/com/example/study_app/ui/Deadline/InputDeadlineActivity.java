package com.example.study_app.ui.Deadline;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.study_app.data.DeadlineDao;
import com.example.study_app.ui.Subject.Model.Subject;
import com.google.android.material.switchmaterial.SwitchMaterial;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.study_app.R;
import com.example.study_app.data.DatabaseHelper;
import com.example.study_app.ui.Deadline.Adapters.IconAdapter;
import com.example.study_app.ui.Deadline.Models.Deadline;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class InputDeadlineActivity extends AppCompatActivity {

    // Constants for Intent Extras
    public static final String KEY_TAI_KHOAN = "taiKhoanMoi";
    public static final String EDIT_DEADLINE = "EDIT_DEADLINE";
    public static final String SUBJECT_MA_HP = "SUBJECT_MA_HP";
    public static final String WEEK_START_DATE = "WEEK_START_DATE";
    public static final String SUBJECT_START_DATE = "SUBJECT_START_DATE"; // For week calculation

    // Views
    private EditText edtTenDeadline, edtGhiChu;
    private TextView txtNgayGioTu, txtNgayGioDen, txtLapLai, txtNhacNho;
    private Button btnHuy, btnThemDeadline;
    private SwitchMaterial switchCaNgay;
    private ImageView imgIcon;

    // Data & Helpers
    private DeadlineDao dbHelper;
    private String subjectMaHp;
    private Deadline deadlineToEdit = null;
    private boolean isEditMode = false;
    private int weekIndex = 0;
    private long subjectStartDateMillis = -1;

    private final Calendar calendarTu = Calendar.getInstance();
    private final Calendar calendarDen = Calendar.getInstance();
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    private int selectedIcon = R.drawable.tasks;
    private final int[] ICON_LIST = {
            R.drawable.brain, R.drawable.calendar, R.drawable.code, R.drawable.exam,
            R.drawable.library, R.drawable.note, R.drawable.tasks, R.drawable.teacher,
            R.drawable.q, R.drawable.w, R.drawable.e, R.drawable.r,
            R.drawable.t, R.drawable.y, R.drawable.u, R.drawable.i,
            R.drawable.o, R.drawable.p, R.drawable.a, R.drawable.s,
            R.drawable.d, R.drawable.f, R.drawable.g
    };
    private final String[] LAP_LAI_OPTIONS = {
            Deadline.REPEAT_TYPE_NONE,
            Deadline.REPEAT_TYPE_DAILY,
            Deadline.REPEAT_TYPE_WEEKDAYS,
            Deadline.REPEAT_TYPE_WEEKLY,
    };

    private final String[] NHAC_NHO_OPTIONS = {
            "Không nhắc nhở",
            "Trước sự kiện 5 phút",
            "Trước sự kiện 15 phút",
            "Trước sự kiện 30 phút",
            "Trước sự kiện 1 giờ",
            "Trước sự kiện 1 ngày"
    };

    private String selectedLapLai = Deadline.REPEAT_TYPE_NONE;
    private String selectedNhacNho = "Trước sự kiện 5 phút";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.deadline_input);

        dbHelper = new DeadlineDao(this);
        findViews();
        handleIntent();
        setupListeners();

        updateDateTimeUI();
        imgIcon.setImageResource(selectedIcon);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1001);
            }
        }

    }
    private void scheduleDeadlineNotification(Deadline deadline,String tenMonHoc) {
        Calendar notifyTime = Calendar.getInstance();
        notifyTime.setTime(deadline.getNgayBatDau());

        // Trừ thời gian nhắc nhở
        switch (deadline.getReminder()) {
            case "Trước sự kiện 5 phút": notifyTime.add(Calendar.MINUTE, -5); break;
            case "Trước sự kiện 15 phút": notifyTime.add(Calendar.MINUTE, -15); break;
            case "Trước sự kiện 30 phút": notifyTime.add(Calendar.MINUTE, -30); break;
            case "Trước sự kiện 1 giờ": notifyTime.add(Calendar.HOUR_OF_DAY, -1); break;
            case "Trước sự kiện 1 ngày": notifyTime.add(Calendar.DAY_OF_MONTH, -1); break;
        }

        Intent intent = new Intent(this, DeadlineNotificationReceiver.class);
        intent.putExtra(DeadlineNotificationReceiver.EXTRA_DEADLINE_ID, (int) deadline.getNgayBatDau().getTime());
        intent.putExtra(DeadlineNotificationReceiver.EXTRA_DEADLINE_TITLE,"Deadline sắp tới: " +  deadline.getTieuDe());
        intent.putExtra(DeadlineNotificationReceiver.EXTRA_DEADLINE_CONTENT, deadline.getNoiDung());
        intent.putExtra(DeadlineNotificationReceiver.EXTRA_DEADLINE_CONTENT, "Môn học: " + tenMonHoc);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                (int) deadline.getNgayBatDau().getTime(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, notifyTime.getTimeInMillis(), pendingIntent);
        }
    }

    private void findViews() {
        edtTenDeadline = findViewById(R.id.edtTenDeadline);
        edtGhiChu = findViewById(R.id.edtGhiChu);
        txtNgayGioTu = findViewById(R.id.txtNgayGioTu);
        txtNgayGioDen = findViewById(R.id.txtNgayGioDen);
        txtLapLai = findViewById(R.id.txtLapLai);
        txtNhacNho = findViewById(R.id.txtNhacNho);
        switchCaNgay = findViewById(R.id.switchCaNgay);
        btnHuy = findViewById(R.id.btnHuy);
        btnThemDeadline = findViewById(R.id.btnThemDeadline);
        imgIcon = findViewById(R.id.imgIcon);
    }

    private void handleIntent() {
        Intent intent = getIntent();
        weekIndex = intent.getIntExtra("weekIndex", 0);
        subjectMaHp = intent.getStringExtra(SUBJECT_MA_HP);
        subjectStartDateMillis = intent.getLongExtra(SUBJECT_START_DATE, -1);

        if (intent.hasExtra(EDIT_DEADLINE)) {
            deadlineToEdit = (Deadline) intent.getSerializableExtra(EDIT_DEADLINE);
            isEditMode = (deadlineToEdit != null);
        }

        if (isEditMode) {
            btnThemDeadline.setText("Lưu thay đổi");
            populateDataForEdit();
        } else {
            btnThemDeadline.setText("Thêm Deadline");
            txtLapLai.setText(selectedLapLai);
            txtNhacNho.setText(selectedNhacNho);
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

        findViewById(R.id.btnEdit).setOnClickListener(v -> openIconDialog());
        findViewById(R.id.layoutTu).setOnClickListener(v -> pickDateTime(calendarTu));
        findViewById(R.id.layoutDen).setOnClickListener(v -> pickDateTime(calendarDen));

        TextView txtTenDeadlineCount = findViewById(R.id.txtTenDeadlineCount);
        TextView txtGhiChuCount = findViewById(R.id.txtGhiChuCount);

        edtTenDeadline.addTextChangedListener(createCounterWatcher(txtTenDeadlineCount, 30));
        edtGhiChu.addTextChangedListener(createCounterWatcher(txtGhiChuCount, 100));

        findViewById(R.id.layoutLapLai).setOnClickListener(v -> showSingleChoiceDialog("Chọn lặp lại", LAP_LAI_OPTIONS, selectedLapLai, (newValue) -> {
            selectedLapLai = newValue;
            txtLapLai.setText(newValue);
        }));

        findViewById(R.id.layoutNhacNho).setOnClickListener(v -> showSingleChoiceDialog("Chọn nhắc nhở", NHAC_NHO_OPTIONS, selectedNhacNho, (newValue) -> {
            selectedNhacNho = newValue;
            txtNhacNho.setText(newValue);
        }));

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
        subjectMaHp = deadlineToEdit.getMaHp();
        selectedLapLai = deadlineToEdit.getRepeatText();
        selectedNhacNho = deadlineToEdit.getReminderText();
        selectedIcon = deadlineToEdit.getIcon();

        txtLapLai.setText(selectedLapLai);
        txtNhacNho.setText(selectedNhacNho);
        imgIcon.setImageResource(selectedIcon);
    }

    private void saveDeadline() {
        String ten = edtTenDeadline.getText().toString().trim();
        if (ten.isEmpty()) {
            edtTenDeadline.setError("Tên deadline không được để trống");
            return;
        }

        Date tu = calendarTu.getTime();
        Deadline deadlineToReturn = isEditMode ? deadlineToEdit : new Deadline();

        deadlineToReturn.setTieuDe(ten);
        deadlineToReturn.setNoiDung(edtGhiChu.getText().toString().trim());
        deadlineToReturn.setNgayBatDau(tu);
        deadlineToReturn.setNgayKetThuc(calendarDen.getTime());
        deadlineToReturn.setMaHp(subjectMaHp);
        deadlineToReturn.setIcon(selectedIcon);
        deadlineToReturn.setRepeat(selectedLapLai);
        deadlineToReturn.setReminder(selectedNhacNho);

        Subject subject = dbHelper.getSubjectByMaHp(subjectMaHp);
        String tenMonHoc = (subject != null) ? subject.tenHp : "";

        Intent resultIntent = new Intent();
        resultIntent.putExtra(KEY_TAI_KHOAN, deadlineToReturn);

        int finalWeekIndex = calculateWeekIndex(tu);
        resultIntent.putExtra("weekIndex", finalWeekIndex);
        if(isEditMode) {
            resultIntent.putExtra("originalWeekIndex", this.weekIndex);
        }

        setResult(RESULT_OK, resultIntent);
        scheduleDeadlineNotification(deadlineToReturn, tenMonHoc);
        finish();
    }

    private int calculateWeekIndex(Date deadlineDate) {
        if (subjectStartDateMillis != -1) {
            Calendar subjectStartCal = Calendar.getInstance();
            subjectStartCal.setTimeInMillis(subjectStartDateMillis);
            subjectStartCal.set(Calendar.HOUR_OF_DAY, 0);
            subjectStartCal.set(Calendar.MINUTE, 0);
            subjectStartCal.set(Calendar.SECOND, 0);
            subjectStartCal.set(Calendar.MILLISECOND, 0);
            long normalizedSubjectStartMillis = subjectStartCal.getTimeInMillis();

            Calendar deadlineCal = Calendar.getInstance();
            deadlineCal.setTime(deadlineDate);
            deadlineCal.set(Calendar.HOUR_OF_DAY, 0);
            deadlineCal.set(Calendar.MINUTE, 0);
            deadlineCal.set(Calendar.SECOND, 0);
            deadlineCal.set(Calendar.MILLISECOND, 0);
            long normalizedDeadlineMillis = deadlineCal.getTimeInMillis();

            long diffMillis = normalizedDeadlineMillis - normalizedSubjectStartMillis;
            if (diffMillis >= 0) {
                long days = TimeUnit.MILLISECONDS.toDays(diffMillis);
                return (int) (days / 7);
            }
            return 0; // Deadline is before the course starts
        }
        return this.weekIndex; // Fallback to the clicked week index
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

    private TextWatcher createCounterWatcher(TextView counterView, int maxLength) {
        return new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                counterView.setText(String.format(Locale.getDefault(), "%d/%d", s.length(), maxLength));
            }
            @Override public void afterTextChanged(Editable s) { }
        };
    }

    private interface SingleChoiceCallback { void onSelection(String newValue); }
    private void showSingleChoiceDialog(String title, final String[] options, String currentSelection, SingleChoiceCallback callback) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setSingleChoiceItems(options, Arrays.asList(options).indexOf(currentSelection), (dialog, which) -> {
                    callback.onSelection(options[which]);
                    dialog.dismiss();
                })
                .show();
    }
}
