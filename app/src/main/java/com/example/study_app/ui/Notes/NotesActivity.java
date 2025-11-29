package com.example.study_app.ui.Notes;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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

        recyclerView = findViewById(R.id.notesRecyclerView);
        emptyImage = findViewById(R.id.emptyImage);
        btnAdd = findViewById(R.id.btnAdd);
        btnBack = findViewById(R.id.btnBack);
        dbHelper = new DatabaseHelper(this);

        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));

        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(NotesActivity.this, InputNoteActivity.class);
            startActivity(intent);
        });

        btnBack.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupNotesList();
    }

    private void setupNotesList() {
        notesList = dbHelper.getAllNotes();

        if (notesList == null || notesList.isEmpty()) {
            emptyImage.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyImage.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            notesAdapter = new NotesAdapter(notesList, this);

            notesAdapter.setOnNoteClickListener(note -> {

                Intent intent = new Intent(NotesActivity.this, InputNoteActivity.class);
                intent.putExtra("note_id", note.getId());
                startActivity(intent);
            });

            notesAdapter.setOnNoteMenuClickListener(new NotesAdapter.OnNoteMenuClickListener() {
                @Override
                public void onEdit(Note note) {
                    Intent intent = new Intent(NotesActivity.this, InputNoteActivity.class);
                    intent.putExtra("note_id", note.getId());
                    startActivity(intent);
                }

                @Override
                public void onDelete(Note note) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(NotesActivity.this);
                    builder.setTitle("Xóa ghi chú");
                    builder.setMessage("Bạn có chắc chắn muốn xóa ghi chú này?");
                    builder.setPositiveButton("XÓA", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dbHelper.deleteNote(note.getId());
                            notesList.clear();
                            notesList.addAll(dbHelper.getAllNotes());
                            notesAdapter.notifyDataSetChanged();
                            setupNotesList();
                        }
                    });
                    builder.setNegativeButton("HỦY", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.create().show();
                }
            });

            recyclerView.setAdapter(notesAdapter);
        }
    }
}
