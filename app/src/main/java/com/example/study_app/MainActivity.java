package com.example.study_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.study_app.ui.Curriculum.CurriculumActivity;
import com.example.study_app.ui.Deadline.MainDeadLine;
import com.example.study_app.ui.Subject.SubjectListActivity;

public class MainActivity extends AppCompatActivity {

    Button btnDeadLine, btnSubject, btnCurriculum;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnDeadLine=findViewById(R.id.btnDeadLine);
        btnSubject = findViewById(R.id.btnSubject);
        btnCurriculum = findViewById(R.id.btnCurriculum);

        btnDeadLine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this, MainDeadLine.class);
                startActivity(intent);
            }
        });

        btnSubject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SubjectListActivity.class);
                startActivity(intent);
            }
        });

        btnCurriculum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CurriculumActivity.class);
                startActivity(intent);
            }
        });
    }
}
