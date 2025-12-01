package com.example.study_app.ui.Deadline;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.study_app.R;
import com.example.study_app.ui.Deadline.Models.Deadline;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class InfoDeadlineActivity extends AppCompatActivity {

    public static final String EXTRA_DEADLINE = "extra_deadline";

    private ImageView imgDeadlineIcon;
    private TextView tvTitle, tvNote, tvStart, tvEnd, tvRepeat, tvReminder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.deadline_info);

        imgDeadlineIcon = findViewById(R.id.imgDeadlineIcon);
        tvTitle = findViewById(R.id.tvDeadlineTitle);
        tvNote = findViewById(R.id.tvDeadlineNote);
        tvStart = findViewById(R.id.tvDeadlineStart);
        tvEnd = findViewById(R.id.tvDeadlineEnd);
        tvRepeat = findViewById(R.id.tvDeadlineRepeat);
        tvReminder = findViewById(R.id.tvDeadlineReminder);

        Deadline deadline = (Deadline) getIntent().getSerializableExtra(EXTRA_DEADLINE);
        if (deadline != null) {
            // Icon
            imgDeadlineIcon.setImageResource(deadline.getIcon());

            // Tiêu đề & ghi chú
            tvTitle.setText("Tên Deadline: "+deadline.getTieuDe());
            if(!TextUtils.isEmpty(deadline.getNoiDung())){
                tvNote.setText("Ghi chú: "+deadline.getNoiDung());
                tvNote.setVisibility(View.VISIBLE);
            }else{
                tvNote.setVisibility(View.GONE);
            }

            // Định dạng ngày giờ
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm", Locale.getDefault());
            tvStart.setText(sdf.format(deadline.getNgayBatDau()));
            tvEnd.setText(sdf.format(deadline.getNgayKetThuc()));

            // Lặp lại & nhắc nhở
            tvRepeat.setText(deadline.getRepeatText());
            tvReminder.setText(deadline.getReminderText());
        }
    }
}
