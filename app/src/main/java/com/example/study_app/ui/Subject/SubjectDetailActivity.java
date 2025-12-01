package com.example.study_app.ui.Subject;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.study_app.R;
import com.example.study_app.data.DatabaseHelper;
import com.example.study_app.data.SubjectDao;
import com.example.study_app.ui.Subject.Model.Subject;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class SubjectDetailActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private SubjectDao subjectDao;
    private String subjectId;

    private TextView subjectNameTextView;
    private LinearLayout headerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.subject_detail_activity);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DatabaseHelper(this);
        subjectDao = new SubjectDao(dbHelper);
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
        Subject subject = subjectDao.getSubjectByMaHp(subjectId);
        if (subject == null) {
            finish();
            return;
        }

        if (subject.mauSac != null && !subject.mauSac.isEmpty()) {
            try {
                int color = Color.parseColor(subject.mauSac);
                GradientDrawable gradientDrawable = new GradientDrawable();
                gradientDrawable.setColor(color);
                float radius = getResources().getDimension(R.dimen.card_corner_radius);
                gradientDrawable.setCornerRadii(new float[]{radius, radius, radius, radius, 0, 0, 0, 0});
                headerView.setBackground(gradientDrawable);

            } catch (IllegalArgumentException e) {
                headerView.setBackgroundColor(Color.GRAY);
            }
        } else {
            headerView.setBackgroundColor(Color.GRAY);
        }
        subjectNameTextView.setText(subject.tenHp);

        String notAvailable = "Chưa có";
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        setupDetailRow(R.id.detail_code, R.drawable.barcode_read, "Mã môn học", subject.maHp);
        setupDetailRow(R.id.detail_lecturer, R.drawable.ic_person, "Giảng viên", (subject.tenGv != null && !subject.tenGv.isEmpty()) ? subject.tenGv : notAvailable);
        setupDetailRow(R.id.detail_credits, R.drawable.ic_star, "Số tín chỉ", String.valueOf(subject.soTc));
        setupDetailRow(R.id.detail_type, R.drawable.ic_category, "Loại môn", subject.loaiMon);
        setupDetailRow(R.id.detail_semester, R.drawable.box_open, "Học kỳ", subject.tenHk);

        String timeString = notAvailable;
        if (subject.gioBatDau != null && subject.gioKetThuc != null) {
            timeString = timeFormat.format(subject.gioBatDau) + " - " + timeFormat.format(subject.gioKetThuc);
        }
        setupDetailRow(R.id.detail_time, R.drawable.clock_ten, "Thời gian", timeString);

        setupDetailRow(R.id.detail_location, R.drawable.land_layer_location, "Phòng học", (subject.phongHoc != null && !subject.phongHoc.isEmpty()) ? subject.phongHoc : notAvailable);

        String dateString = notAvailable;
        if (subject.ngayBatDau != null && subject.ngayKetThuc != null) {
            dateString = dateFormat.format(subject.ngayBatDau) + " - " + dateFormat.format(subject.ngayKetThuc);
        }
        setupDetailRow(R.id.detail_dates, R.drawable.calendar, "Ngày học", dateString);

        setupDetailRow(R.id.detail_weeks, R.drawable.calendar, "Số tuần học", (subject.soTuan > 0) ? String.valueOf(subject.soTuan) : notAvailable);

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
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
