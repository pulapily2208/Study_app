package com.example.study_app.ui.Notes;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.study_app.R;
import com.example.study_app.data.DatabaseHelper;
import com.example.study_app.ui.Notes.Adapters.NotesAdapter;
import com.example.study_app.ui.Notes.Model.Note;

import java.util.ArrayList;

public class NotesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ImageView emptyImage;
    private Button btnAdd;
    private ImageView btnBack;
    private NotesAdapter notesAdapter;
    private ArrayList<Note> notesList;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notes_homescreen);

        // 1. Tìm và khởi tạo các View và Helper
        recyclerView = findViewById(R.id.notesRecyclerView);
        emptyImage = findViewById(R.id.emptyImage);
        btnAdd = findViewById(R.id.btnAdd);
        btnBack = findViewById(R.id.btnBack);
        dbHelper = new DatabaseHelper(this);

        // 2. Thiết lập LayoutManager và Listener cho các nút
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));

        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(NotesActivity.this, InputNoteActivity.class);
            startActivity(intent);
        });

        btnBack.setOnClickListener(v -> {
            finish(); // Đóng Activity hiện tại và quay về màn hình trước đó
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 3. Tải dữ liệu và thiết lập Adapter mỗi khi Activity được hiển thị
        setupNotesList();
    }

    private void setupNotesList() {
        // 4. Lấy danh sách ghi chú từ cơ sở dữ liệu
        notesList = dbHelper.getAllNotes();

        // 5. Xử lý trường hợp không có ghi chú
        if (notesList == null || notesList.isEmpty()) {
            emptyImage.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            // 6. Xử lý trường hợp có ghi chú
            emptyImage.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            // 7. Khởi tạo Adapter với dữ liệu mới
            notesAdapter = new NotesAdapter(notesList, this);

            // 8. Gán OnClickListener cho Adapter (sau khi đã được khởi tạo)
            notesAdapter.setOnNoteClickListener(note -> {
                Intent intent = new Intent(NotesActivity.this, NotesDetailActivity.class);
                intent.putExtra("noteId", note.getId());
                intent.putExtra("title", note.getTitle());
                intent.putExtra("content", note.getBody());
                intent.putExtra("imagePath", note.getImagePath());
                intent.putExtra("date", note.getCreated_at());
                intent.putExtra("color", note.getColor_tag());
                intent.putExtra("pinned", note.getPinned());
                startActivity(intent);
            });

            // 9. Gán Adapter cho RecyclerView
            recyclerView.setAdapter(notesAdapter);
        }
    }
}
