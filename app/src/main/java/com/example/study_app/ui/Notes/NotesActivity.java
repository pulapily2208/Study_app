package com.example.study_app.ui.Notes;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.study_app.R;
import com.example.study_app.data.CurriculumDao;
import com.example.study_app.data.DatabaseHelper;
import com.example.study_app.data.NotesDao;
import com.example.study_app.ui.Curriculum.Model.Curriculum;
import com.example.study_app.ui.Notes.Adapters.NotesAdapter;
import com.example.study_app.ui.Notes.Model.Note;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.HashSet;

public class NotesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ImageView emptyImage;
    private Button btnAdd;
    private ImageView btnBack;
    private NotesAdapter notesAdapter;
    private ArrayList<Note> notesList;
    private NotesDao dbHelper;

    private ArrayList<Curriculum> monHocList;

    private ArrayList<String> monDangHoc;
    private ArrayList<String> monDaHoc;

    private ChipGroup chipGroup;

    private CurriculumDao curriculumDao;

    private DatabaseHelper db;

    private String maHpSelected = null;

    SearchView searchViewNote;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.notes_homescreen);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.notesRecyclerView);
        emptyImage = findViewById(R.id.emptyImage);
        btnAdd = findViewById(R.id.btnAdd);
        btnBack = findViewById(R.id.btnBack);
        searchViewNote = findViewById(R.id.searchViewNote);
        db = new DatabaseHelper(this);
        curriculumDao = new CurriculumDao(db);
        dbHelper = new NotesDao(this);

        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));

        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(NotesActivity.this, InputNoteActivity.class);
            intent.putExtra("maHpSelected", maHpSelected);
            startActivity(intent);
        });

        btnBack.setOnClickListener(v -> finish());

        initChips();
        loadMonHoc();


        searchViewNote.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                performSearch(newText);
                return false;
            }
        });
    }

    private void performSearch(String keyword) {
        if (notesAdapter == null) {
            notesAdapter = new NotesAdapter(new ArrayList<>(), this);
            recyclerView.setAdapter(notesAdapter);
        }

        keyword = keyword.trim();

        ArrayList<Note> results;

        if (keyword.isEmpty()) {
            results = (maHpSelected == null)
                    ? dbHelper.getAllNotes()
                    : dbHelper.getNotesBySubjectCode(maHpSelected);
        } else {
            int checkedChipId = chipGroup.getCheckedChipId();
            if (checkedChipId == View.NO_ID) {
                results = dbHelper.searchNotes(keyword);
            } else {
                Chip selectedChip = chipGroup.findViewById(checkedChipId);
                String ma_hp = selectedChip.getTag() != null ? selectedChip.getTag().toString() : null;
                results = dbHelper.searchNotesBySubject(ma_hp, keyword);
            }
        }

        notesAdapter.updateData(results);
    }


    private void loadMonHoc() {
        int userId = 1;
        ArrayList<Curriculum> monHocList = curriculumDao.getSubjectsForNote(userId);
        displayChips(monHocList);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupNotesList();
        if(maHpSelected == null) {
            if (chipGroup.getChildCount() > 0) {
                chipGroup.check(chipGroup.getChildAt(0).getId());
            }
        }
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
                            filterNotesBySubject(maHpSelected);
                        }
                    });
                    builder.setNegativeButton("HỦY", (dialog, which) -> dialog.cancel());
                    builder.create().show();
                }
            });

            recyclerView.setAdapter(notesAdapter);
        }
    }

    private void initChips(){
        chipGroup = findViewById(R.id.chipGroupMonHoc);

    }

    private void displayChips(ArrayList<Curriculum> monHocList) {
        chipGroup.removeAllViews();

        Chip allChip = new Chip(this);
        allChip.setText("Tất cả");
        allChip.setChipBackgroundColorResource(R.color.blue);
        allChip.setTextColor(getResources().getColor(R.color.white));
        allChip.setCheckable(true);
        allChip.setId(View.generateViewId());

        allChip.setOnClickListener(v -> {
            maHpSelected = null;
            filterNotesBySubject(null);
        });
        chipGroup.addView(allChip);


        for (Curriculum monHoc : monHocList) {
            Chip chip = new Chip(this);
            chip.setText(monHoc.getTenHp());
            chip.setChipBackgroundColorResource(R.color.blue);
            chip.setTextColor(getResources().getColor(R.color.white));
            chip.setCheckable(true);
            chip.setId(View.generateViewId());
            chip.setTag(monHoc.getMaHp());

            chip.setOnClickListener(v -> {
                String maHp = (String) v.getTag();
                String tenHp = monHoc.getTenHp();
                maHpSelected = maHp;
                filterNotesBySubject(maHp);
            });

            chip.setOnLongClickListener(v -> {
                String maHp = (String) v.getTag();
                String tenHp = monHoc.getTenHp();

                new AlertDialog.Builder(NotesActivity.this)
                        .setTitle("Xóa ghi chú")
                        .setMessage("Bạn có chắc chắn muốn xóa tất cả ghi chú cho môn học '" + tenHp + "'?")
                        .setPositiveButton("Xóa", (dialog, which) -> {
                            dbHelper.deleteNotesBySubject(maHp);
                            Toast.makeText(NotesActivity.this, "Đã xóa tất cả ghi chú cho " + tenHp, Toast.LENGTH_SHORT).show();
                            loadMonHoc();
                            setupNotesList();
                            maHpSelected = null;
                            if (chipGroup.getChildCount() > 0) {
                                chipGroup.check(chipGroup.getChildAt(0).getId());
                            }
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
                return true;
            });

            chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {

                for (int i = 0; i < group.getChildCount(); i++) {
                    Chip chipAt = (Chip) group.getChildAt(i);

                    if (checkedIds.contains(chipAt.getId())) {
                        chipAt.setChipBackgroundColorResource(R.color.blue);
                        chipAt.setTextColor(getResources().getColor(R.color.white));
                    } else {
                        // Chip chưa chọn
                        chipAt.setChipBackgroundColorResource(R.color.white);
                        chipAt.setTextColor(getResources().getColor(R.color.black));
                    }
                }
            });


            chipGroup.addView(chip);
        }

        chipGroup.setSingleSelection(true);
        chipGroup.setSelectionRequired(true);

        if (maHpSelected == null && chipGroup.getChildCount() > 0) {
            chipGroup.check(chipGroup.getChildAt(0).getId());
        }
    }

    private void filterNotesBySubject(String maHp) {
        maHpSelected = maHp;
        if (maHp == null) {
            notesList = dbHelper.getAllNotes();
        } else {
            notesList = dbHelper.getNotesBySubjectCode(maHp);
        }

        if (notesAdapter == null) {
            setupNotesList();
        } else {
            notesAdapter.updateData(notesList);
        }

        if (notesList == null || notesList.isEmpty()) {
            emptyImage.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyImage.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }


}
