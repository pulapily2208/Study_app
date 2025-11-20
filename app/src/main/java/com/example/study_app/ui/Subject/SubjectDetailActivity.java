package com.example.study_app.ui.Subject;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
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

    private TextView subjectNameTextView;
    private LinearLayout headerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subject_detail_activity);

        dbHelper = new DatabaseHelper(this);
        Intent intent = getIntent();
        subjectId = intent.getStringExtra("SUBJECT_ID");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        subjectNameTextView = findViewById(R.id.subject_detail_name);
        headerView = findViewById(R.id.header_view);


        if (subjectId != null) {
            loadSubjectDetails();
        }
    }

    private void loadSubjectDetails() {
        Subject subject = dbHelper.getSubjectByMaHp(subjectId);
        if (subject == null) {
            // Handle case where subject is not found
            finish(); // Close activity if subject is invalid
            return;
        }

        // Set header color and text
        if (subject.mauSac != null && !subject.mauSac.isEmpty()) {
            try {
                int color = Color.parseColor(subject.mauSac);
                // Create a gradient drawable for rounded corners
                GradientDrawable gradientDrawable = new GradientDrawable();
                gradientDrawable.setColor(color);
                float radius = getResources().getDimension(R.dimen.card_corner_radius);
                gradientDrawable.setCornerRadii(new float[]{radius, radius, radius, radius, 0, 0, 0, 0});
                headerView.setBackground(gradientDrawable);

            } catch (IllegalArgumentException e) {
                // Handle invalid color string
                headerView.setBackgroundColor(Color.GRAY); // Default color
            }
        } else {
            headerView.setBackgroundColor(Color.GRAY); // Default color
        }
        subjectNameTextView.setText(subject.tenHp);


        // --- Details Section ---
        String notAvailable = "Chưa có";
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        // Mã môn học
        setupDetailRow(R.id.detail_code, R.drawable.barcode_read, "Mã môn học", subject.maHp);

        // Giảng viên
        setupDetailRow(R.id.detail_lecturer, R.drawable.ic_person, "Giảng viên", (subject.tenGv != null && !subject.tenGv.isEmpty()) ? subject.tenGv : notAvailable);

        // Số tín chỉ
        setupDetailRow(R.id.detail_credits, R.drawable.ic_star, "Số tín chỉ", String.valueOf(subject.soTc));

        // Loại môn học
        setupDetailRow(R.id.detail_type, R.drawable.ic_category, "Loại môn", subject.loaiMon);

        // Học kỳ
        setupDetailRow(R.id.detail_semester, R.drawable.box_open, "Học kỳ", subject.tenHk);


        // Thời gian học
        String timeString = notAvailable;
        if (subject.gioBatDau != null && subject.gioKetThuc != null) {
            timeString = timeFormat.format(subject.gioBatDau) + " - " + timeFormat.format(subject.gioKetThuc);
        }
        setupDetailRow(R.id.detail_time, R.drawable.clock_ten, "Thời gian", timeString);

        // Địa điểm
        setupDetailRow(R.id.detail_location, R.drawable.land_layer_location, "Phòng học", (subject.phongHoc != null && !subject.phongHoc.isEmpty()) ? subject.phongHoc : notAvailable);

        // Ngày học
        String dateString = notAvailable;
        if (subject.ngayBatDau != null && subject.ngayKetThuc != null) {
            dateString = dateFormat.format(subject.ngayBatDau) + " - " + dateFormat.format(subject.ngayKetThuc);
        }
        setupDetailRow(R.id.detail_dates, R.drawable.calendar, "Ngày học", dateString);

        // Số tuần
        setupDetailRow(R.id.detail_weeks, R.drawable.calendar, "Số tuần học", (subject.soTuan > 0) ? String.valueOf(subject.soTuan) : notAvailable);

        // Ghi chú
        setupDetailRow(R.id.detail_notes, R.drawable.note, "Ghi chú", (subject.ghiChu != null && !subject.ghiChu.isEmpty()) ? subject.ghiChu : notAvailable);

    }


    private void setupDetailRow(int viewId, int iconResId, String title, String value) {
        View detailRow = findViewById(viewId);
        ImageView icon = detailRow.findViewById(R.id.item_icon);
        TextView titleView = detailRow.findViewById(R.id.item_title);
        TextView valueView = detailRow.findViewById(R.id.item_content);

        icon.setImageResource(iconResId);
        titleView.setText(title);
        valueView.setText(value);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
