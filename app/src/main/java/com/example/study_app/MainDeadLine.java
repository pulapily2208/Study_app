package com.example.study_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.study_app.Adapters.AdapterWeek;
import com.example.study_app.Models.Deadline;
import com.example.study_app.Models.Week;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainDeadLine extends AppCompatActivity {

    ListView lvItemTuan;
    ArrayList<Week> listWeek;
    AdapterWeek adapterWeek;

    ActivityResultLauncher<Intent> launcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_deadline);

        lvItemTuan = findViewById(R.id.lvItemTuan);

        // Tạo dữ liệu mẫu: 10 tuần
        listWeek = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            listWeek.add(new Week("Tuần " + (i + 1)));
        }

        adapterWeek = new AdapterWeek(this, R.layout.item_tuan, listWeek);
        lvItemTuan.setAdapter(adapterWeek);

        // Khi nhấn nút Thêm ở từng tuần
        adapterWeek.setOnAddDeadlineListener(position -> {
            Intent intent = new Intent(MainDeadLine.this, InputDeadlineActivity.class);
            intent.putExtra("weekIndex", position);
            launcher.launch(intent);
        });

        // ActivityResultLauncher để nhận dữ liệu từ InputDeadlineActivity
        launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();

                        int weekIndex = data.getIntExtra("weekIndex", 0);
                        String ten = data.getStringExtra("ten");
                        String ghiChu = data.getStringExtra("ghiChu");
                        String tu = data.getStringExtra("tu");
                        String den = data.getStringExtra("den");

                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                        try {
                            Date ngayTu = sdf.parse(tu);
                            Date ngayDen = sdf.parse(den);

                            Deadline deadline = new Deadline(ten, ngayTu, ngayDen);
                            listWeek.get(weekIndex).getDeadlines().add(deadline);
                            adapterWeek.notifyDataSetChanged();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }
        );
    }
}
