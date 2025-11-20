package com.example.study_app.ui.Deadline;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
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
import com.example.study_app.ui.Deadline.Adapters.IconAdapter;
import com.example.study_app.ui.Deadline.Models.Deadline;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class InputDeadlineActivity extends AppCompatActivity {
    public static final String KEY_TAI_KHOAN = "taiKhoanMoi";
    public static final String KEY_DEADLINE_EDIT = "deadline_edit";
    public static final String KEY_DEADLINE_INDEX = "deadline_index";

    EditText edtTenDeadline, edtGhiChu;
    TextView txtNgayGioTu, txtNgayGioDen, txtNhacNho, txtLapLai;
    Button btnHuy, btnThemDeadline;
    Switch switchCaNgay;

    int[] ICON_LIST = {
            R.drawable.brain,
            R.drawable.calendar,
            R.drawable.code,
            R.drawable.exam,
            R.drawable.library,
            R.drawable.note,
            R.drawable.brain,
            R.drawable.tasks,
            R.drawable.teacher
    };

    int weekIndex;
    int editingDeadlineIndex = -1;
    private int selectedIcon = R.drawable.ic_launcher_foreground;
    ImageView imgIcon;
    Deadline deadlineToEdit = null;

    Calendar calendarTu = Calendar.getInstance();
    Calendar calendarDen = Calendar.getInstance();

    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    // --- Cấu hình Nhắc nhở ---
    final String[] reminderOptions = {"Không nhắc", "Đúng giờ", "Trước 5 phút", "Trước 15 phút", "Trước 30 phút", "Trước 1 giờ", "Trước 1 ngày"};
    int selectedReminderIndex = 2; // Mặc định: Trước 5 phút

    // --- Cấu hình Lặp lại ---
    final String[] repeatOptions = {"Không lặp lại", "Hằng ngày", "Hằng tuần", "Tùy chỉnh"};
    final String[] customTypes = {"Theo Thứ (Ngày trong tuần)", "Theo Tuần cụ thể"}; // Menu con
    final String[] daysOfWeek = {"Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ Nhật"};
    
    boolean[] checkedDays = new boolean[7]; 
    
    // Cấu hình chọn tuần (tối đa 20 tuần)
    String[] weekOptions; 
    boolean[] checkedWeeks;

    int selectedRepeatIndex = 0; 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.deadline_input);

        // Khởi tạo dữ liệu tuần (ví dụ 20 tuần)
        weekOptions = new String[20];
        checkedWeeks = new boolean[20];
        for (int i = 0; i < 20; i++) {
            weekOptions[i] = "Tuần " + (i + 1);
        }

        // Ánh xạ view
        edtTenDeadline = findViewById(R.id.edtTenDeadline);
        edtGhiChu = findViewById(R.id.edtGhiChu);
        txtNgayGioTu = findViewById(R.id.txtNgayGioTu);
        txtNgayGioDen = findViewById(R.id.txtNgayGioDen);
        txtNhacNho = findViewById(R.id.txtNhacNho);
        txtLapLai = findViewById(R.id.txtLapLai); 
        switchCaNgay = findViewById(R.id.switchCaNgay);
        btnHuy = findViewById(R.id.btnHuy);
        btnThemDeadline = findViewById(R.id.btnThemDeadline);
        imgIcon = findViewById(R.id.imgIcon);
        
        LinearLayout layoutIcon = findViewById(R.id.layoutIcon);
        LinearLayout layoutNhacNho = findViewById(R.id.layoutNhacNho);
        LinearLayout layoutLapLai = findViewById(R.id.layoutLapLai); 

        // Sự kiện click
        layoutIcon.setOnClickListener(v -> openIconDialog(imgIcon));
        layoutNhacNho.setOnClickListener(v -> showReminderDialog());
        layoutLapLai.setOnClickListener(v -> showRepeatDialog()); 

        // Nhận dữ liệu
        Intent intent = getIntent();
        weekIndex = intent.getIntExtra("weekIndex", 0);
        
        if (intent.hasExtra(KEY_DEADLINE_EDIT)) {
            deadlineToEdit = (Deadline) intent.getSerializableExtra(KEY_DEADLINE_EDIT);
            editingDeadlineIndex = intent.getIntExtra(KEY_DEADLINE_INDEX, -1);
        }

        // Điền dữ liệu nếu là Edit
        if (deadlineToEdit != null) {
            btnThemDeadline.setText("Lưu");
            edtTenDeadline.setText(deadlineToEdit.getTieuDe());
            edtGhiChu.setText(deadlineToEdit.getNoiDung());
            
            calendarTu.setTime(deadlineToEdit.getNgayBatDau());
            calendarDen.setTime(deadlineToEdit.getNgayKetThuc());
            
            selectedIcon = deadlineToEdit.getIcon();
            imgIcon.setImageResource(selectedIcon);

            // Khôi phục Nhắc nhở
            String currentReminder = deadlineToEdit.getReminderText();
            txtNhacNho.setText(currentReminder);
            for (int i = 0; i < reminderOptions.length; i++) {
                if (reminderOptions[i].equals(currentReminder)) {
                    selectedReminderIndex = i;
                    break;
                }
            }

            // Khôi phục Lặp lại
            String currentRepeat = deadlineToEdit.getRepeatText();
            txtLapLai.setText(currentRepeat);
            
            boolean matched = false;
            for (int i = 0; i < repeatOptions.length; i++) {
                if (repeatOptions[i].equals(currentRepeat)) {
                    selectedRepeatIndex = i;
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                selectedRepeatIndex = 3; // Tùy chỉnh
            }

        } else {
            txtNhacNho.setText(reminderOptions[selectedReminderIndex]);
            txtLapLai.setText(repeatOptions[selectedRepeatIndex]);
        }

        txtNgayGioTu.setText(sdf.format(calendarTu.getTime()));
        txtNgayGioDen.setText(sdf.format(calendarDen.getTime()));

        txtNgayGioTu.setOnClickListener(v -> pickDateTime(calendarTu, txtNgayGioTu));
        txtNgayGioDen.setOnClickListener(v -> pickDateTime(calendarDen, txtNgayGioDen));

        btnHuy.setOnClickListener(v -> finish());
        
        switchCaNgay.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Calendar startOfDay = (Calendar) calendarTu.clone();
                startOfDay.set(Calendar.HOUR_OF_DAY, 0);
                startOfDay.set(Calendar.MINUTE, 0);
                startOfDay.set(Calendar.SECOND, 0);
                startOfDay.set(Calendar.MILLISECOND, 0);

                Calendar endOfDay = (Calendar) startOfDay.clone();
                endOfDay.set(Calendar.HOUR_OF_DAY, 23);
                endOfDay.set(Calendar.MINUTE, 59);
                endOfDay.set(Calendar.SECOND, 59);
                endOfDay.set(Calendar.MILLISECOND, 999);

                calendarTu = startOfDay;
                calendarDen = endOfDay;

                txtNgayGioTu.setText(sdf.format(calendarTu.getTime()));
                txtNgayGioDen.setText(sdf.format(calendarDen.getTime()));
            }
        });

        btnThemDeadline.setOnClickListener(v -> {
            String ten = edtTenDeadline.getText().toString().trim();
            String ghiChu = edtGhiChu.getText().toString().trim();
            Date tu = calendarTu.getTime();
            Date den = calendarDen.getTime();
            String reminderText = txtNhacNho.getText().toString();
            String repeatText = txtLapLai.getText().toString();

            if (!ten.isEmpty()) {
                Deadline resultDeadline;
                
                if (deadlineToEdit != null) {
                    deadlineToEdit.setTieuDe(ten);
                    deadlineToEdit.setNoiDung(ghiChu);
                    deadlineToEdit.setNgayBatDau(tu);
                    deadlineToEdit.setNgayKetThuc(den);
                    deadlineToEdit.setIcon(selectedIcon);
                    deadlineToEdit.setReminder(reminderText);
                    deadlineToEdit.setRepeat(repeatText);
                    resultDeadline = deadlineToEdit;
                } else {
                    resultDeadline = new Deadline(ten, ghiChu, tu, den, selectedIcon);
                    resultDeadline.setReminder(reminderText);
                    resultDeadline.setRepeat(repeatText);
                }

                Intent result = new Intent();
                result.putExtra("weekIndex", weekIndex);
                result.putExtra(KEY_TAI_KHOAN, resultDeadline);
                
                if (editingDeadlineIndex != -1) {
                    result.putExtra(KEY_DEADLINE_INDEX, editingDeadlineIndex);
                }
                
                setResult(RESULT_OK, result);
                
                String msg = (editingDeadlineIndex != -1) ? "Đã cập nhật deadline" : "Đã thêm deadline mới";
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                
                finish();
            } else {
                edtTenDeadline.setError("Nhập tên deadline");
            }
        });
    }

    // --- Dialog Nhắc nhở ---
    private void showReminderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn thời gian nhắc nhở");
        builder.setSingleChoiceItems(reminderOptions, selectedReminderIndex, (dialog, which) -> {
            selectedReminderIndex = which;
            txtNhacNho.setText(reminderOptions[which]);
            dialog.dismiss();
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    // --- Dialog Lặp lại ---
    private void showRepeatDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Lặp lại");
        builder.setSingleChoiceItems(repeatOptions, selectedRepeatIndex, (dialog, which) -> {
            selectedRepeatIndex = which;
            if (which == 3) { // Chọn Tùy chỉnh
                dialog.dismiss();
                showCustomTypeDialog(); // Mở dialog chọn loại tùy chỉnh
            } else {
                txtLapLai.setText(repeatOptions[which]);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    // --- Dialog chọn LOẠI Tùy chỉnh ---
    private void showCustomTypeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Loại tùy chỉnh");
        builder.setItems(customTypes, (dialog, which) -> {
            if (which == 0) {
                showCustomDaysDialog(); // Theo Thứ
            } else {
                showCustomWeeksDialog(); // Theo Tuần
            }
        });
        builder.setNegativeButton("Quay lại", (dialog, which) -> showRepeatDialog());
        builder.show();
    }

    // --- Dialog Tùy chỉnh (Chọn nhiều ngày trong tuần) ---
    private void showCustomDaysDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Lặp lại vào các ngày");
        
        builder.setMultiChoiceItems(daysOfWeek, checkedDays, (dialog, which, isChecked) -> {
            checkedDays[which] = isChecked;
        });

        builder.setPositiveButton("OK", (dialog, which) -> {
            StringBuilder result = new StringBuilder();
            boolean first = true;
            for (int i = 0; i < checkedDays.length; i++) {
                if (checkedDays[i]) {
                    if (!first) result.append(", ");
                    result.append(daysOfWeek[i]);
                    first = false;
                }
            }
            if (result.length() == 0) {
                txtLapLai.setText("Tùy chỉnh (Chưa chọn ngày)");
            } else {
                txtLapLai.setText("Các ngày: " + result.toString());
            }
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }
    
    // --- Dialog Tùy chỉnh (Chọn nhiều tuần cụ thể) ---
    private void showCustomWeeksDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn các tuần muốn lặp lại");

        builder.setMultiChoiceItems(weekOptions, checkedWeeks, (dialog, which, isChecked) -> {
            checkedWeeks[which] = isChecked;
        });

        builder.setPositiveButton("OK", (dialog, which) -> {
            StringBuilder result = new StringBuilder();
            boolean first = true;
            for (int i = 0; i < checkedWeeks.length; i++) {
                if (checkedWeeks[i]) {
                    if (!first) result.append(", ");
                    result.append(i + 1); // Lưu số tuần (1, 2, 3...)
                    first = false;
                }
            }
            
            if (result.length() == 0) {
                txtLapLai.setText("Tùy chỉnh (Chưa chọn tuần)");
            } else {
                txtLapLai.setText("Các tuần: " + result.toString());
            }
        });

        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void pickDateTime(Calendar calendar, TextView textView) {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            new TimePickerDialog(this, (timePicker, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);

                textView.setText(sdf.format(calendar.getTime()));
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();

        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void openIconDialog(ImageView targetView) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.deadline_dialog_icon);

        GridView gridView = dialog.findViewById(R.id.gridIcons);
        IconAdapter adapter = new IconAdapter(this, ICON_LIST);
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener((parent, view, position, id) -> {
            selectedIcon = ICON_LIST[position];
            targetView.setImageResource(selectedIcon);
            Toast.makeText(this, "Đã chọn icon!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }
}
