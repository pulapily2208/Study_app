package com.example.study_app.ui.Notes;

import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.study_app.R;
import com.example.study_app.data.DatabaseHelper;
import com.example.study_app.ui.Notes.Model.Note;

import org.json.JSONArray;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NotesDetailActivity extends AppCompatActivity {

    private TextView tvTitle, tvDate, tvContent;
    private LinearLayout imageContainer;
    private DatabaseHelper dbHelper;
    private ImageView btnBack;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notes_detail);

        dbHelper = new DatabaseHelper(this);

        initView();

        int noteId = getIntent().getIntExtra("noteId", -1);

        if (noteId != -1) {
            loadNoteData(noteId);
        } else {
            Toast.makeText(this, "Không tìm thấy ghi chú.", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnBack.setOnClickListener(v -> finish());
    }

    private void initView() {
        tvTitle = findViewById(R.id.tvTitle);
        tvDate = findViewById(R.id.tvDate);
        tvContent = findViewById(R.id.tvContent);
        imageContainer = findViewById(R.id.imageContainer);
        btnBack = findViewById(R.id.btnBack);
    }

    private void loadNoteData(int noteId) {
        Note note = dbHelper.getNoteById(noteId);

        if (note != null) {

            tvTitle.setText(note.getTitle());

            // ✔ HIỂN THỊ HTML ĐÚNG CÁCH
            tvContent.setText(
                    Html.fromHtml(note.getBody(), Html.FROM_HTML_MODE_LEGACY)
            );

            // ✔ Ngày tạo
            try {
                long timestamp = Long.parseLong(note.getCreated_at());
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                tvDate.setText(sdf.format(new Date(timestamp)));
            } catch (Exception e) {
                tvDate.setText(note.getCreated_at());
            }

            // ✔ Hiển thị danh sách ảnh
            imageContainer.removeAllViews();
            String imagePath = note.getImagePath();

            if (imagePath != null && !imagePath.isEmpty()) {
                try {
                    JSONArray array = new JSONArray(imagePath);
                    for (int i = 0; i < array.length(); i++) {
                        addImageToContainer(Uri.parse(array.getString(i)));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } else {
            Toast.makeText(this, "Không thể tải dữ liệu ghi chú.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void addImageToContainer(Uri uri) {
        ImageView imageView = new ImageView(this);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        params.setMargins(0, 12, 0, 12);
        imageView.setLayoutParams(params);
        imageView.setAdjustViewBounds(true);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

        Glide.with(this).load(uri).into(imageView);
        imageContainer.addView(imageView);
    }
}
