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

    Calendar calendarTu = Calendar.getInstance();
    Calendar calendarDen = Calendar.getInstance();

    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.deadline_input);

        edtTenDeadline = findViewById(R.id.edtTenDeadline);
        edtGhiChu = findViewById(R.id.edtGhiChu);
        txtNgayGioTu = findViewById(R.id.txtNgayGioTu);
        txtNgayGioDen = findViewById(R.id.txtNgayGioDen);
        switchCaNgay = findViewById(R.id.switchCaNgay);
        btnHuy = findViewById(R.id.btnHuy);
        btnThemDeadline = findViewById(R.id.btnThemDeadline);
        imgIcon = findViewById(R.id.imgIcon);
//        LinearLayout layoutMauSac = findViewById(R.id.layoutMauSac);
//        layoutMauSac.setOnClickListener(v -> openIconDialog());
        LinearLayout layoutIcon = findViewById(R.id.layoutIcon);

        layoutIcon.setOnClickListener(v -> openIconDialog(imgIcon));


        weekIndex = getIntent().getIntExtra("weekIndex", 0);

        // Khởi tạo giá trị mặc định cho từ – đến
        txtNgayGioTu.setText(sdf.format(calendarTu.getTime()));
        txtNgayGioDen.setText(sdf.format(calendarDen.getTime()));

        // Chọn ngày giờ từ
        txtNgayGioTu.setOnClickListener(v -> pickDateTime(calendarTu, txtNgayGioTu));

        // Chọn ngày giờ đến
        txtNgayGioDen.setOnClickListener(v -> pickDateTime(calendarDen, txtNgayGioDen));

        // Nút Hủy
        btnHuy.setOnClickListener(v -> finish());
        switchCaNgay.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Lấy ngày từ calendarTu hiện tại
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
                // bật lại chọn giờ thủ công, giữ nguyên thời gian hiện tại
                txtNgayGioTu.setText(sdf.format(calendarTu.getTime()));
                txtNgayGioDen.setText(sdf.format(calendarDen.getTime()));
            }
        });

        // Nút Thêm
        btnThemDeadline.setOnClickListener(v -> {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

            String ten = edtTenDeadline.getText().toString().trim();
            String ghiChu = edtGhiChu.getText().toString().trim();
//            String tu = sdf.format(calendarTu.getTime());
            Date tu = (calendarTu.getTime());
//            String den = sdf.format(calendarDen.getTime());
            Date den =(calendarDen.getTime());
//            String tu = txtNgayGioTu.getText().toString().trim();
//            String den = txtNgayGioDen.getText().toString().trim();

            Deadline dlNew;
            if (!ten.isEmpty()) {

                dlNew = new Deadline(ten, ghiChu, tu, den); // 1. Tạo đối tượng với hàm khởi tạo cũ
                dlNew.setIcon(selectedIcon);                 // 2. Dùng phương thức setIcon() để gán icon

                Intent result = new Intent();
                result.putExtra("weekIndex", weekIndex);
//                result.putExtra("ten", ten);
//                result.putExtra("ghiChu", ghiChu);
//                result.putExtra("tu", tu);
//                result.putExtra("den", den);
                result.putExtra(KEY_TAI_KHOAN, dlNew);
                setResult(RESULT_OK, result);
                Toast.makeText(this, "Đã Them deadline moi", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                edtTenDeadline.setError("Nhập tên deadline");
            }
        });
    }

    private void pickDateTime(Calendar calendar, TextView textView) {
        // Chọn ngày trước
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            // Chọn giờ
            new TimePickerDialog(this, (timePicker, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);

                textView.setText(sdf.format(calendar.getTime()));
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();

        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private int selectedIcon = R.drawable.ic_launcher_foreground; // icon mặc định

    private void openIconDialog(ImageView targetView) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.deadline_dialog_icon);

        GridView gridView = dialog.findViewById(R.id.gridIcons);
        IconAdapter adapter = new IconAdapter(this, ICON_LIST);
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener((parent, view, position, id) -> {
            selectedIcon = ICON_LIST[position];

            // cập nhật icon ngay cho ImageView
            targetView.setImageResource(selectedIcon);

            Toast.makeText(this, "Đã chọn icon!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

}
