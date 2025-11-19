package com.example.study_app.ui.Deadline;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.Toast;

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
                        Deadline dlResult = (Deadline) data.getSerializableExtra(InputDeadlineActivity.KEY_TAI_KHOAN);
                        
                        // Kiểm tra xem có phải là cập nhật (Sửa) hay thêm mới
                        int editIndex = data.getIntExtra(InputDeadlineActivity.KEY_DEADLINE_INDEX, -1);

                        if (dlResult != null) {
                            if (editIndex != -1) {
                                // Cập nhật deadline cũ
                                if (weekIndex >= 0 && weekIndex < listWeek.size()) {
                                    Week week = listWeek.get(weekIndex);
                                    if (editIndex >= 0 && editIndex < week.getDeadlines().size()) {
                                        week.getDeadlines().set(editIndex, dlResult);
                                        adapterWeek.updateWeek(weekIndex);
                                        adapterWeek.notifyDataSetChanged();
                                    }
                                }
                            } else {
                                // Thêm mới
                                adapterWeek.addDeadlineToWeek(weekIndex, dlResult);
                                adapterWeek.notifyDataSetChanged();
                            }
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

        // Xử lý sự kiện nhấn giữ deadline
        adapterWeek.setOnDeadlineLongClickListener((weekIndex, deadlineIndex, deadline) -> {
            // Xác định nội dung menu dựa trên trạng thái ghim
            String pinOption = deadline.isPinned() ? "Bỏ ghim" : "Ghim deadline";
            CharSequence[] options = {pinOption, "Sửa Deadline", "Chuyển tuần", "Xóa deadline"};
            
            AlertDialog.Builder builder = new AlertDialog.Builder(MainDeadLine.this);
            builder.setTitle("Tùy chọn cho: " + deadline.getTieuDe());
            builder.setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0: // Ghim / Bỏ ghim
                        if (weekIndex >= 0 && weekIndex < listWeek.size()) {
                            Week week = listWeek.get(weekIndex);
                            ArrayList<Deadline> deadlines = week.getDeadlines();
                            
                            // Đảo ngược trạng thái
                            boolean newPinnedState = !deadline.isPinned();
                            deadline.setPinned(newPinnedState);

                            if (newPinnedState) {
                                // Nếu chuyển sang ghim -> Đưa lên đầu
                                if (deadlineIndex > 0) {
                                    deadlines.remove(deadlineIndex);
                                    deadlines.add(0, deadline);
                                }
                                Toast.makeText(MainDeadLine.this, "Đã ghim deadline", Toast.LENGTH_SHORT).show();
                            } else {
                                // Nếu bỏ ghim -> giữ nguyên vị trí hiện tại
                                Toast.makeText(MainDeadLine.this, "Đã bỏ ghim", Toast.LENGTH_SHORT).show();
                            }
                            
                            // Cập nhật UI
                            adapterWeek.updateWeek(weekIndex);
                            adapterWeek.notifyDataSetChanged();
                        }
                        break;
                    case 1: // Sửa Deadline
                        Intent intent = new Intent(MainDeadLine.this, InputDeadlineActivity.class);
                        intent.putExtra("weekIndex", weekIndex);
                        intent.putExtra(InputDeadlineActivity.KEY_DEADLINE_EDIT, deadline); // Gửi object deadline
                        intent.putExtra(InputDeadlineActivity.KEY_DEADLINE_INDEX, deadlineIndex); // Gửi vị trí
                        launcher.launch(intent);
                        break;
                    case 2: // Chuyển tuần
                        showMoveWeekDialog(weekIndex, deadlineIndex, deadline);
                        break;
                    case 3: // Xóa deadline
                        if (weekIndex >= 0 && weekIndex < listWeek.size()) {
                            Week week = listWeek.get(weekIndex);
                            if (deadlineIndex >= 0 && deadlineIndex < week.getDeadlines().size()) {
                                week.getDeadlines().remove(deadlineIndex);
                                
                                adapterWeek.updateWeek(weekIndex); 
                                adapterWeek.notifyDataSetChanged(); 
                                
                                Toast.makeText(MainDeadLine.this, "Đã xóa deadline", Toast.LENGTH_SHORT).show();
                            }
                        }
                        break;
                }
            });
            builder.show();
        });
    }

    private void showMoveWeekDialog(int currentWeekIndex, int deadlineIndex, Deadline deadline) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn tuần muốn chuyển đến");

        final NumberPicker numberPicker = new NumberPicker(this);
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(listWeek.size());
        numberPicker.setValue(currentWeekIndex + 1); // Mặc định là tuần hiện tại
        numberPicker.setWrapSelectorWheel(false);

        builder.setView(numberPicker);

        builder.setPositiveButton("Chuyển", (dialog, which) -> {
            int newWeekIndex = numberPicker.getValue() - 1; // Index bắt đầu từ 0

            if (newWeekIndex != currentWeekIndex) {
                // Xóa ở tuần cũ
                if (currentWeekIndex >= 0 && currentWeekIndex < listWeek.size()) {
                    listWeek.get(currentWeekIndex).getDeadlines().remove(deadlineIndex);
                    adapterWeek.updateWeek(currentWeekIndex);
                }

                // Thêm vào tuần mới
                if (newWeekIndex >= 0 && newWeekIndex < listWeek.size()) {
                    listWeek.get(newWeekIndex).getDeadlines().add(deadline);
                    adapterWeek.updateWeek(newWeekIndex);
                }
                
                // Cập nhật toàn bộ để tính lại chiều cao
                adapterWeek.notifyDataSetChanged();
                Toast.makeText(MainDeadLine.this, "Đã chuyển sang Tuần " + (newWeekIndex + 1), Toast.LENGTH_SHORT).show();
            } else {
                 Toast.makeText(MainDeadLine.this, "Bạn đang ở tuần này rồi", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Hủy", null);
        builder.show();
    }
}
