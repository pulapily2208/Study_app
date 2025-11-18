package com.example.study_app.ui.Deadline;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.study_app.R;
import com.example.study_app.ui.Deadline.Adapters.AdapterWeek;
import com.example.study_app.ui.Deadline.Models.Deadline;
import com.example.study_app.ui.Deadline.Models.Week;

import java.util.ArrayList;

public class MainDeadLine extends AppCompatActivity {

    ListView lvItemTuan;
    ArrayList<Week> listWeek;
    AdapterWeek adapterWeek;

    ActivityResultLauncher<Intent> launcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.deadline_main);

        lvItemTuan = findViewById(R.id.lvItemTuan);

        // Tạo dữ liệu mẫu: 10 tuần
        listWeek = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            listWeek.add(new Week("Tuần " + (i + 1)));
        }

        adapterWeek = new AdapterWeek(this, R.layout.deadline_item_tuan, listWeek);
        lvItemTuan.setAdapter(adapterWeek);




        launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {

                        Intent data = result.getData();

                        // Lấy tuần Index
                        int weekIndex = data.getIntExtra("weekIndex", 0);

                        // Lấy object Deadline
                        Deadline dlNew = (Deadline) data.getSerializableExtra(InputDeadlineActivity.KEY_TAI_KHOAN);

                        if (dlNew != null) {
                            adapterWeek.addDeadlineToWeek(weekIndex, dlNew);

//                            listWeek.get(weekIndex).getDeadlines().add(dlNew);
                            adapterWeek.notifyDataSetChanged();
                        }
                    }
                }
        );
        // Khi nhấn nút Thêm ở từng tuần
        adapterWeek.setOnAddDeadlineListener(position -> {
            Intent intent = new Intent(MainDeadLine.this, InputDeadlineActivity.class);
            intent.putExtra("weekIndex", position);
            launcher.launch(intent);
        });

    }
}
