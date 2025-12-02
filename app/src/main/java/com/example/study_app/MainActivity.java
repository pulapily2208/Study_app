package com.example.study_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.study_app.ui.Curriculum.CurriculumActivity;
import com.example.study_app.ui.Deadline.MainDeadLine;
import com.example.study_app.ui.Deadline.MainDeadLineMonHoc;
import com.example.study_app.ui.Notes.NotesActivity;
import com.example.study_app.ui.Subject.SubjectListActivity;
import com.example.study_app.ui.Timetable.TimetableWeek;

public class MainActivity extends AppCompatActivity {

    Button btnNote, btnDeadLine, btnSubject, btnCurriculum, btnTimetable;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnDeadLine=findViewById(R.id.btnDeadLine);
        btnNote = findViewById(R.id.btnNote);
        btnSubject = findViewById(R.id.btnSubject);
        btnCurriculum = findViewById(R.id.btnCurriculum);
        btnTimetable = findViewById(R.id.btnTimetable);

//        DEADLINE
        btnDeadLine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this, MainDeadLine.class);
                startActivity(intent);
            }
        });



//        SUBJECT
        btnSubject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SubjectListActivity.class);
                startActivity(intent);
            }
        });


//      NOTE
        btnNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, NotesActivity.class);
                startActivity(intent);
            }
        });



//        CURRICULUM
        btnCurriculum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CurriculumActivity.class);
                startActivity(intent);
            }
        });



//        TIMETABLE
        btnTimetable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TimetableWeek.class);
                startActivity(intent);
            }
        });

//        KẾT QUẢ HỌC TẬP

    }
}
