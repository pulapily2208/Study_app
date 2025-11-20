
package com.example.study_app.ui.Subject;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.study_app.R;
import com.example.study_app.data.DatabaseHelper;
import com.example.study_app.ui.Subject.Model.Subject;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class SubjectDetailActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private String subjectId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subject_detail_activity);

        dbHelper = new DatabaseHelper(this);

        // Lấy ID môn học từ Intent
        subjectId = getIntent().getStringExtra("SUBJECT_ID");
        if (subjectId == null) {
            Toast.makeText(this, "Không tìm thấy thông tin môn học.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Thêm Toolbar và nút back
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        loadSubjectDetails();
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Xử lý khi nhấn nút back trên Toolbar
        onBackPressed();
        return true;
    }

    private void loadSubjectDetails() {
        Subject subject = dbHelper.getSubjectByMaHp(subjectId);
        if (subject == null) {
            Toast.makeText(this, "Không thể tải chi tiết môn học.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // --- Header ---
        LinearLayout headerView = findViewById(R.id.header_view);
        TextView tvSubjectName = findViewById(R.id.subject_detail_name);
        tvSubjectName.setText(subject.tenHp != null ? subject.tenHp : "Tên môn học");

        // Đặt màu nền cho header
        if (subject.mauSac != null && !subject.mauSac.isEmpty()) {
            try {
                int color = Color.parseColor(subject.mauSac);
                GradientDrawable gradientDrawable = new GradientDrawable();
                gradientDrawable.setColor(color);
                // Chỉ bo góc trên bên trái và trên bên phải
                gradientDrawable.setCornerRadii(new float[]{
                        getResources().getDimension(R.dimen.card_corner_radius), getResources().getDimension(R.dimen.card_corner_radius),
                        getResources().getDimension(R.dimen.card_corner_radius), getResources().getDimension(R.dimen.card_corner_radius),
                        0, 0, 0, 0 // Các góc dưới không bo
                });
                headerView.setBackground(gradientDrawable);
            } catch (IllegalArgumentException e) {
                // Nếu mã màu sai, dùng màu mặc định
                headerView.setBackgroundColor(Color.GRAY);
            }
        } else {
            // Nếu không có màu, dùng màu mặc định
            headerView.setBackgroundColor(Color.GRAY);
        }

        // --- Details ---
        String notAvailable = "Chưa có";

        setupDetailRow(R.id.detail_code, R.drawable.barcode_read, "Mã môn học", subject.maHp != null ? subject.maHp : notAvailable);
        setupDetailRow(R.id.detail_lecturer, R.drawable.ic_person, "Giảng viên", subject.tenGv != null ? subject.tenGv : notAvailable);
        setupDetailRow(R.id.detail_credits, R.drawable.ic_star, "Số tín chỉ", String.valueOf(subject.soTc)); // int is safe
        setupDetailRow(R.id.detail_type, R.drawable.ic_category, "Loại môn", subject.loaiMon != null ? subject.loaiMon : notAvailable);
        setupDetailRow(R.id.detail_semester, R.drawable.box_open, "Học kỳ", subject.tenHk != null ? subject.tenHk : notAvailable);

        // Thời gian học trong ngày (an toàn với null)
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String timeString;
        if (subject.gioBatDau != null && subject.gioKetThuc != null) {
            try {
                 timeString = timeFormat.format(subject.gioBatDau) + " - " + timeFormat.format(subject.gioKetThuc);
            } catch (Exception e) {
                timeString = notAvailable;
            }
        } else {
            timeString = notAvailable;
        }
        setupDetailRow(R.id.detail_time, R.drawable.clock_ten, "Thời gian", timeString);

        // Phòng học
        setupDetailRow(R.id.detail_location, R.drawable.land_layer_location, "Phòng học", subject.phongHoc != null ? subject.phongHoc : notAvailable);

        // Khoảng ngày học (an toàn với null)
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String dateString;
         if (subject.ngayBatDau != null && subject.ngayKetThuc != null) {
            try {
                dateString = dateFormat.format(subject.ngayBatDau) + " đến " + dateFormat.format(subject.ngayKetThuc);
            } catch (Exception e) {
                dateString = notAvailable;
            }
        } else {
            dateString = notAvailable;
        }
        setupDetailRow(R.id.detail_dates, R.drawable.calendar, "Thời gian học", dateString);

        // Ghi chú (an toàn với null)
        setupDetailRow(R.id.detail_notes, R.drawable.note, "Ghi chú", (subject.ghiChu != null && !subject.ghiChu.isEmpty()) ? subject.ghiChu : "Không có ghi chú");
    }

    private void setupDetailRow(int viewId, int iconResId, String title, String content) {
        View includedLayout = findViewById(viewId);
        if(includedLayout == null) return;

        ImageView icon = includedLayout.findViewById(R.id.item_icon);
        TextView tvTitle = includedLayout.findViewById(R.id.item_title);
        TextView tvContent = includedLayout.findViewById(R.id.item_content);

        if(icon != null) {
            icon.setImageResource(iconResId);
        }
        if(tvTitle != null) {
            tvTitle.setText(title);
        }
        if(tvContent != null) {
            tvContent.setText(content);
        }
    }
}
