package com.example.study_app.ui.Subject;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.study_app.R;
import com.example.study_app.data.DatabaseHelper;
import com.example.study_app.data.SubjectDao;
import com.example.study_app.ui.Subject.Model.Subject;
import android.widget.ImageButton;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class SubjectDetailActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private SubjectDao subjectDao;
    private String subjectId;

    private TextView subjectNameTextView;
    private LinearLayout headerView;
    private Subject currentSubject;
    private ImageButton btnAiAdvice;

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

        ImageView backButton = findViewById(R.id.btn_back);
        backButton.setOnClickListener(v -> finish());

        subjectNameTextView = findViewById(R.id.subject_detail_name);
        headerView = findViewById(R.id.header_view);

        if (subjectId != null) {
            loadSubjectDetails();
        }
        btnAiAdvice = findViewById(R.id.btn_ai_advice);
        btnAiAdvice.setOnClickListener(v -> {
            if (currentSubject != null) {
                // 1. Show Loading Dialog
                AlertDialog loadingDialog = new AlertDialog.Builder(this)
                        .setTitle("Trợ lý AI đang suy nghĩ...")
                        .setMessage("Vui lòng chờ trong giây lát. Quá trình này có thể mất một chút thời gian.")
                        .setCancelable(false)
                        .create();
                loadingDialog.show();

                // 2. Call the new asynchronous Gemini API method
                SubjectAdviceProvider.getAdviceFromGemini(
                        currentSubject.tenHp,
                        new SubjectAdviceProvider.AdviceCallback() {
                            @Override
                            public void onSuccess(String advice) {
                                // Run on the main UI thread to update the UI
                                runOnUiThread(() -> {
                                    loadingDialog.dismiss();
                                    new AlertDialog.Builder(SubjectDetailActivity.this)
                                            .setTitle("💡 Trợ lý AI")
                                            .setMessage(advice)
                                            .setPositiveButton("Đã hiểu", null)
                                            .show();
                                });
                            }

                            @Override
                            public void onError(Exception e) {
                                runOnUiThread(() -> {
                                    loadingDialog.dismiss();
                                    int secs = parseRetrySeconds(e.getMessage());
                                    boolean isRateLimited = isRateLimitError(e.getMessage());

                                    String baseMsg = "Không thể kết nối với AI: " + e.getMessage()
                                            + "\n\nVui lòng kiểm tra lại API Key và kết nối mạng.";
                                    String finalMsg = isRateLimited
                                            ? baseMsg + "\n\nHệ thống sẽ tạm khoá nút AI khoảng " + secs
                                                    + " giây để tránh vượt hạn mức."
                                            : baseMsg;

                                    new AlertDialog.Builder(SubjectDetailActivity.this)
                                            .setTitle("Đã xảy ra lỗi")
                                            .setMessage(finalMsg)
                                            .setPositiveButton("Đóng", null)
                                            .show();

                                    if (isRateLimited) {
                                        applyAiCooldown(secs);
                                    }
                                });
                            }
                        });
            }
        });
    }

    private void loadSubjectDetails() {
        currentSubject = subjectDao.getSubjectByMaHp(subjectId);
        if (currentSubject == null) {
            finish();
            return;
        }

        applyHeaderColor(currentSubject.mauSac);

        subjectNameTextView.setText(currentSubject.tenHp);

        String notAvailable = "Chưa có";
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        setupDetailRow(R.id.detail_code, R.drawable.barcode_read, "Mã môn học", currentSubject.maHp);
        setupDetailRow(R.id.detail_lecturer, R.drawable.ic_person, "Giảng viên",
                safe(currentSubject.tenGv, notAvailable));
        setupDetailRow(R.id.detail_credits, R.drawable.ic_star, "Số tín chỉ",
                String.valueOf(currentSubject.soTc));
        setupDetailRow(R.id.detail_type, R.drawable.ic_category, "Loại môn", currentSubject.loaiMon);
        setupDetailRow(R.id.detail_semester, R.drawable.box_open, "Học kỳ", currentSubject.tenHk);

        String timeString = (currentSubject.gioBatDau != null && currentSubject.gioKetThuc != null)
                ? timeFormat.format(currentSubject.gioBatDau) + " - " + timeFormat.format(currentSubject.gioKetThuc)
                : notAvailable;
        setupDetailRow(R.id.detail_time, R.drawable.clock_ten, "Thời gian", timeString);

        setupDetailRow(R.id.detail_location, R.drawable.land_layer_location, "Phòng học",
                safe(currentSubject.phongHoc, notAvailable));

        String dateString = (currentSubject.ngayBatDau != null && currentSubject.ngayKetThuc != null)
                ? dateFormat.format(currentSubject.ngayBatDau) + " - " + dateFormat.format(currentSubject.ngayKetThuc)
                : notAvailable;
        setupDetailRow(R.id.detail_dates, R.drawable.calendar, "Ngày học", dateString);

        setupDetailRow(R.id.detail_weeks, R.drawable.calendar, "Số tuần học",
                (currentSubject.soTuan > 0) ? String.valueOf(currentSubject.soTuan) : notAvailable);

        setupDetailRow(R.id.detail_notes, R.drawable.note, "Ghi chú",
                safe(currentSubject.ghiChu, notAvailable));
    }

    private void applyHeaderColor(String colorString) {
        if (colorString == null || colorString.isEmpty()) {
            headerView.setBackgroundColor(Color.GRAY);
            return;
        }

        try {
            int color = Color.parseColor(colorString);
            GradientDrawable gradientDrawable = new GradientDrawable();
            gradientDrawable.setColor(color);

            float radius = getResources().getDimension(R.dimen.card_corner_radius);
            gradientDrawable.setCornerRadii(new float[] {
                    radius, radius,
                    radius, radius,
                    0, 0,
                    0, 0
            });

            headerView.setBackground(gradientDrawable);
        } catch (Exception e) {
            headerView.setBackgroundColor(Color.GRAY);
        }
    }

    private String safe(String value, String fallback) {
        return (value != null && !value.isEmpty()) ? value : fallback;
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

    private boolean isRateLimitError(String message) {
        if (message == null)
            return false;
        String m = message.toLowerCase();
        return m.contains("429") || m.contains("resource_exhausted") || m.contains("quota exceeded");
    }

    private int parseRetrySeconds(String message) {
        if (message == null)
            return 30;
        try {
            java.util.regex.Matcher matcher = java.util.regex.Pattern
                    .compile("retry in\\s+([0-9]+(?:\\.[0-9]+)?)", java.util.regex.Pattern.CASE_INSENSITIVE)
                    .matcher(message);
            if (matcher.find()) {
                double secs = Double.parseDouble(matcher.group(1));
                return Math.max(1, (int) Math.ceil(secs));
            }
        } catch (Exception ignore) {
        }
        return 30;
    }

    private void applyAiCooldown(int seconds) {
        if (btnAiAdvice == null)
            return;
        int ms = Math.max(1000, seconds * 1000);
        btnAiAdvice.setEnabled(false);
        btnAiAdvice.setAlpha(0.5f);
        new android.os.Handler(getMainLooper()).postDelayed(() -> {
            btnAiAdvice.setEnabled(true);
            btnAiAdvice.setAlpha(1f);
        }, ms);
    }
}
