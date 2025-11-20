package com.example.study_app.ui.Deadline;

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

/**
 * Activity để nhập (thêm) một Deadline mới.
 * - Truyền vào: "weekIndex" (int) (tùy optional), "SUBJECT_MA_HP" (String) để gán mã môn cho deadline.
 * - Trả về: Intent chứa Serializable Deadline dưới key InputDeadlineActivity.KEY_TAI_KHOAN
 */
public class InputDeadlineActivity extends AppCompatActivity {
    public static final String KEY_TAI_KHOAN = "taiKhoanMoi";

    EditText edtTenDeadline, edtGhiChu;
    TextView txtNgayGioTu, txtNgayGioDen;
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
    ImageView imgIcon;
    String subjectMaHp; // mã môn (nếu được truyền từ MainDeadLine)

    Calendar calendarTu = Calendar.getInstance();
    Calendar calendarDen = Calendar.getInstance();

    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    private int selectedIcon = R.drawable.ic_launcher_foreground; // icon mặc định

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.deadline_input);

        // Views
        edtTenDeadline = findViewById(R.id.edtTenDeadline);
        edtGhiChu = findViewById(R.id.edtGhiChu);
        txtNgayGioTu = findViewById(R.id.txtNgayGioTu);
        txtNgayGioDen = findViewById(R.id.txtNgayGioDen);
        switchCaNgay = findViewById(R.id.switchCaNgay);
        btnHuy = findViewById(R.id.btnHuy);
        btnThemDeadline = findViewById(R.id.btnThemDeadline);
        imgIcon = findViewById(R.id.imgIcon);
        LinearLayout layoutIcon = findViewById(R.id.layoutIcon);

        // Lấy extras
        weekIndex = getIntent().getIntExtra("weekIndex", 0);
        subjectMaHp = getIntent().getStringExtra("SUBJECT_MA_HP");

        // Thiết lập giá trị mặc định hiển thị ngày giờ
        txtNgayGioTu.setText(sdf.format(calendarTu.getTime()));
        txtNgayGioDen.setText(sdf.format(calendarDen.getTime()));

        // Mở dialog chọn icon khi nhấn
        layoutIcon.setOnClickListener(v -> openIconDialog(imgIcon));

        // Chọn ngày giờ (từ)
        txtNgayGioTu.setOnClickListener(v -> pickDateTime(calendarTu, txtNgayGioTu));

        // Chọn ngày giờ (đến)
        txtNgayGioDen.setOnClickListener(v -> pickDateTime(calendarDen, txtNgayGioDen));

        // Nút Hủy
        btnHuy.setOnClickListener(v -> finish());

        // Switch cả ngày: set start = 00:00, end = 23:59
        switchCaNgay.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Lấy ngày hiện tại của calendarTu, set full-day
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
            } else {
                // giữ nguyên thời gian hiện tại (không thay đổi)
                txtNgayGioTu.setText(sdf.format(calendarTu.getTime()));
                txtNgayGioDen.setText(sdf.format(calendarDen.getTime()));
            }
        });

        // Nút Thêm: tạo Deadline, gán maHp (nếu có), trả về result
        btnThemDeadline.setOnClickListener(v -> {
            String ten = edtTenDeadline.getText().toString().trim();
            String ghiChu = edtGhiChu.getText().toString().trim();
            Date tu = calendarTu.getTime();
            Date den = calendarDen.getTime();

            if (ten.isEmpty()) {
                edtTenDeadline.setError("Nhập tên deadline");
                return;
            }

            // Tạo đối tượng Deadline và gán các thuộc tính
            Deadline dlNew = new Deadline(ten, ghiChu, tu, den);
            dlNew.setIcon(selectedIcon);
            dlNew.setCompleted(false);
            // Gán mã môn nếu có (để MainDeadLine hoặc DatabaseHelper biết deadline thuộc môn nào)
            if (subjectMaHp != null) {
                dlNew.setMaHp(subjectMaHp);
            }

            // Trả về object Deadline qua Intent (Serializable)
            Intent result = new Intent();
            result.putExtra("weekIndex", weekIndex);
            result.putExtra(KEY_TAI_KHOAN, dlNew);
            setResult(RESULT_OK, result);
            Toast.makeText(this, "Đã thêm deadline mới", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    /**
     * Hiển thị DatePickerDialog rồi TimePickerDialog để chọn ngày giờ.
     */
    private void pickDateTime(Calendar calendar, TextView textView) {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            new TimePickerDialog(this, (timePicker, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                textView.setText(sdf.format(calendar.getTime()));
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();

        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    /**
     * Mở dialog chọn icon (GridView). Khi chọn, cập nhật ImageView target và đóng dialog.
     */
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