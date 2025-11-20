package com.example.study_app.ui.Notes;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.study_app.R;
import com.example.study_app.data.DatabaseHelper;
import com.example.study_app.ui.Notes.Model.Note;

public class NotesDetailActivity extends AppCompatActivity {
    private TextView tvTitle, tvDate, tvContent;
    private ImageView imgAnh, btnBack;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notes_detail);

        dbHelper = new DatabaseHelper(this);

        int noteId = getIntent().getIntExtra("noteId", -1);
        initView();

        if(noteId != -1){
            loadNoteData(noteId);
        }

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initView(){
        tvTitle = findViewById(R.id.tvTitle);
        tvDate = findViewById(R.id.tvDate);
        tvContent = findViewById(R.id.tvContent);
        imgAnh = findViewById(R.id.imgAnhHienThi);
        btnBack = findViewById(R.id.btnBack);

    }

    private void loadNoteData(int noteId){
        Note note = dbHelper.getNoteById(noteId);
        if(note != null){
            tvTitle.setText(note.getTitle());
            tvDate.setText(note.getCreated_at());
            tvContent.setText(note.getBody());

            if(note.getImagePath() != null && !note.getImagePath().isEmpty()){
                imgAnh.setVisibility(View.VISIBLE);
                imgAnh.setImageURI(Uri.parse(note.getImagePath()));
            } else {
                imgAnh.setVisibility(View.GONE);
            }
        }
    }
}

