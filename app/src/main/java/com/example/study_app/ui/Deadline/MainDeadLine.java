package com.example.study_app.ui.Deadline;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.study_app.R;
import com.example.study_app.data.DatabaseHelper;
import com.example.study_app.ui.Deadline.Adapters.AdapterDeadline;
import com.example.study_app.ui.Deadline.Models.Deadline;

import java.util.ArrayList;

public class MainDeadLine extends AppCompatActivity {

    ListView lvDeadlines;
    TextView tvSubjectTitle;
    ArrayList<Deadline> deadlineList;
    AdapterDeadline adapterDeadline;
    String subjectMaHp = null;
    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.deadline_main);

        // Initialize views
        lvDeadlines = findViewById(R.id.lvItemTuan);
        tvSubjectTitle = findViewById(R.id.tvSubjectTitle);
        dbHelper = new DatabaseHelper(this);

        // Get data from Intent
        Intent intent = getIntent();
        subjectMaHp = intent.getStringExtra("SUBJECT_MA_HP");
        String subjectTenHp = intent.getStringExtra("SUBJECT_TEN_HP");

        // Update the title
        if (subjectTenHp != null) {
            tvSubjectTitle.setText(subjectTenHp);
        } else {
            tvSubjectTitle.setText("Deadlines"); // Fallback title
        }

        // --- Load real data ---
        loadDeadlines();

        // Handle the back button
        findViewById(R.id.btnQuaylai).setOnClickListener(v -> finish());

        // TODO: Re-enable Add/Edit functionality later
    }

    private void loadDeadlines() {
        if (subjectMaHp != null) {
            // Get deadlines from the database
            deadlineList = dbHelper.getDeadlinesByMaHp(subjectMaHp);

            // Create and set your old adapter with the correct layout
            adapterDeadline = new AdapterDeadline(this, R.layout.deadline_item, deadlineList);
            lvDeadlines.setAdapter(adapterDeadline);
        }
    }
}
