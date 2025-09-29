package com.example.study_app;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.study_app.ui.deadline.DateAdapter;
import com.example.study_app.ui.deadline.DateModel;
import com.example.study_app.R;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    //        panh

    //        manh

    //        trim

    //        thảo
//    private RecyclerView recyclerView;
//    private DateAdapter adapter;
//    private ArrayList<DateModel> dateList;

//    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
//        panh

//        manh

//        trim

//        thảo


//        recyclerView = findViewById(R.id.recyclerViewDates);
//        recyclerView.setLayoutManager(
//                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
//        );
//
//        // Dữ liệu mẫu
//        dateList = new ArrayList<>();
//        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
//        int[] numbers = {22, 23, 24, 25, 26, 27, 28};
//
//        for (int i = 0; i < days.length; i++) {
//            dateList.add(new DateModel(days[i], numbers[i]));
//        }
//
//        // Gắn adapter
//        adapter = new DateAdapter(dateList, this);
//        recyclerView.setAdapter(adapter);
//
//        // Chọn sẵn ngày 24 (index 2)
//        dateList.get(2).setSelected(true);
//        adapter.notifyItemChanged(2);
    }
}