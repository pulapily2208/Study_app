package com.example.study_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.study_app.ui.Deadline.*;
import com.example.study_app.ui.Subject.*;

public class MainActivity extends AppCompatActivity {

    Button btnDeadLine, btnSubject;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        btnDeadLine=findViewById(R.id.btnDeadLine);
        btnSubject = findViewById(R.id.btnSubject);

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
    }
}
