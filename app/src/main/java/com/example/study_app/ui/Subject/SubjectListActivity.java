package com.example.study_app.ui.Subject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.study_app.R;
import com.example.study_app.data.DatabaseHelper;
import com.example.study_app.ui.Subject.Adapter.SubjectAdapter;
import com.example.study_app.ui.Subject.Model.Subject;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class SubjectListActivity extends AppCompatActivity {

    private FloatingActionButton fab;
    private RecyclerView recyclerViewSubjects;
    private SubjectAdapter subjectAdapter;
    private DatabaseHelper dbHelper;
    private ArrayList<Subject> subjectList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subject_list);

        fab = findViewById(R.id.fab);
        recyclerViewSubjects = findViewById(R.id.recyclerViewSubjects);

        dbHelper = new DatabaseHelper(this);
        subjectList = new ArrayList<>();

        // Set up the adapter and RecyclerView
        // Pass 'this' as the context, which is now required by the adapter's constructor
        subjectAdapter = new SubjectAdapter(this, subjectList);
        recyclerViewSubjects.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewSubjects.setAdapter(subjectAdapter);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SubjectListActivity.this, SubjectAddActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Load subjects every time the activity is resumed to reflect changes.
        loadSubjects();
    }

    private void loadSubjects() {
        // 1. Get updated list from the database
        ArrayList<Subject> updatedSubjects = dbHelper.getAllSubjects();

        // 2. Clear the old list in the adapter
        subjectList.clear();

        // 3. Add all new subjects
        subjectList.addAll(updatedSubjects);

        // 4. Notify the adapter to refresh the RecyclerView
        subjectAdapter.notifyDataSetChanged();
    }
}
