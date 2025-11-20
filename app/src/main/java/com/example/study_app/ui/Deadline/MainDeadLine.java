package com.example.study_app.ui.Deadline;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.study_app.R;
import com.example.study_app.ui.Deadline.Adapters.AdapterWeek;
import com.example.study_app.ui.Deadline.Models.Deadline;
import com.example.study_app.ui.Deadline.Models.Week;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
                                // --- TRƯỜNG HỢP SỬA ---
                                if (weekIndex >= 0 && weekIndex < listWeek.size()) {
                                    Week week = listWeek.get(weekIndex);
                                    if (editIndex >= 0 && editIndex < week.getDeadlines().size()) {
                                        week.getDeadlines().set(editIndex, dlResult);
                                        adapterWeek.updateWeek(weekIndex);
                                    }
                                }
                            } else {
                                // --- TRƯỜNG HỢP THÊM MỚI ---
                                // 1. Thêm vào tuần hiện tại
                                adapterWeek.addDeadlineToWeek(weekIndex, dlResult);

                                // 2. Xử lý lặp lại
                                handleRepeat(dlResult, weekIndex);
                            }
                            // Cập nhật UI tổng thể
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

        // Xử lý sự kiện nhấn giữ deadline
        adapterWeek.setOnDeadlineLongClickListener((weekIndex, deadlineIndex, deadline) -> {
            String pinOption = deadline.isPinned() ? "Bỏ ghim" : "Ghim deadline";
            CharSequence[] options = {pinOption, "Sửa Deadline", "Xóa deadline"};
            
            AlertDialog.Builder builder = new AlertDialog.Builder(MainDeadLine.this);
            builder.setTitle("Tùy chọn cho: " + deadline.getTieuDe());
            builder.setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0: // Ghim / Bỏ ghim
                        if (weekIndex >= 0 && weekIndex < listWeek.size()) {
                            Week week = listWeek.get(weekIndex);
                            ArrayList<Deadline> deadlines = week.getDeadlines();
                            
                            boolean newPinnedState = !deadline.isPinned();
                            deadline.setPinned(newPinnedState);

                            if (newPinnedState) {
                                if (deadlineIndex > 0) {
                                    deadlines.remove(deadlineIndex);
                                    deadlines.add(0, deadline);
                                }
                                Toast.makeText(MainDeadLine.this, "Đã ghim deadline", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MainDeadLine.this, "Đã bỏ ghim", Toast.LENGTH_SHORT).show();
                            }
                            
                            adapterWeek.updateWeek(weekIndex);
                            adapterWeek.notifyDataSetChanged();
                        }
                        break;
                    case 1: // Sửa Deadline
                        Intent intent = new Intent(MainDeadLine.this, InputDeadlineActivity.class);
                        intent.putExtra("weekIndex", weekIndex);
                        intent.putExtra(InputDeadlineActivity.KEY_DEADLINE_EDIT, deadline);
                        intent.putExtra(InputDeadlineActivity.KEY_DEADLINE_INDEX, deadlineIndex);
                        launcher.launch(intent);
                        break;
                    case 2: // Xóa deadline
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

    // --- LOGIC XỬ LÝ LẶP LẠI ---
    private void handleRepeat(Deadline original, int currentWeekIndex) {
        String repeatText = original.getRepeatText();

        if ("Hằng tuần".equals(repeatText)) {
            processWeeklyRepeat(original, currentWeekIndex);
            
        } else if ("Hằng ngày".equals(repeatText)) {
            processDailyRepeat(original, currentWeekIndex);
            
        } else if (repeatText.startsWith("Các tuần:")) {
            processCustomWeeksRepeat(original, repeatText, currentWeekIndex);
            
        } else if (repeatText.startsWith("Các ngày:")) {
            processCustomDaysRepeat(original, currentWeekIndex, repeatText);
        }
    }

    // 1. Hằng tuần: Lặp qua các tuần còn lại
    private void processWeeklyRepeat(Deadline original, int startWeekIndex) {
        for (int i = startWeekIndex + 1; i < listWeek.size(); i++) {
            int weeksDiff = i - startWeekIndex;
            Deadline repeatedDl = createRepeatedDeadline(original, weeksDiff * 7);
            adapterWeek.addDeadlineToWeek(i, repeatedDl);
        }
    }

    // 2. Hằng ngày: Thêm vào các ngày tiếp theo
    private void processDailyRepeat(Deadline original, int startWeekIndex) {
        int totalWeeks = listWeek.size();
        int maxDays = (totalWeeks - startWeekIndex) * 7; 
        
        // Bắt đầu từ ngày mai (+1)
        for (int i = 1; i < maxDays; i++) {
            int targetWeekIndex = startWeekIndex + (i / 7);
            
            if (targetWeekIndex < listWeek.size()) {
                Deadline repeatedDl = createRepeatedDeadline(original, i);
                adapterWeek.addDeadlineToWeek(targetWeekIndex, repeatedDl);
            }
        }
    }

    // 3. Tùy chỉnh theo tuần cụ thể: "Các tuần: 1, 3, 5"
    private void processCustomWeeksRepeat(Deadline original, String repeatText, int currentWeekIndex) {
        try {
            String weeksStr = repeatText.replace("Các tuần: ", "").trim();
            if (weeksStr.isEmpty() || weeksStr.startsWith("Các tuần:")) return; // Xử lý thêm nếu replace chưa sạch
            
            String[] parts = weeksStr.split(", ");
            
            for (String part : parts) {
                try {
                    if (part.trim().isEmpty()) continue;
                    int targetWeekNum = Integer.parseInt(part.trim());
                    int targetWeekIndex = targetWeekNum - 1;
                    
                    // Chỉ thêm nếu không phải là tuần hiện tại (vì tuần hiện tại đã thêm rồi)
                    if (targetWeekIndex != currentWeekIndex && targetWeekIndex >= 0 && targetWeekIndex < listWeek.size()) {
                        int weeksDiff = targetWeekIndex - currentWeekIndex;
                        Deadline repeatedDl = createRepeatedDeadline(original, weeksDiff * 7);
                        adapterWeek.addDeadlineToWeek(targetWeekIndex, repeatedDl);
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 4. Tùy chỉnh theo Thứ: "Các ngày: Thứ 2, Thứ 4"
    private void processCustomDaysRepeat(Deadline original, int startWeekIndex, String repeatText) {
        String daysStr = repeatText.replace("Các ngày: ", "").trim();
        if (daysStr.isEmpty()) return;
        
        // Tách chuỗi và chuẩn hóa
        String[] daysArray = daysStr.split(", ");
        List<String> selectedDays = new ArrayList<>();
        for (String d : daysArray) selectedDays.add(d.trim());
        
        // Danh sách thứ tự chuẩn
        List<String> standardDays = Arrays.asList("Chủ Nhật", "Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7");
        
        // Xác định thứ của deadline gốc
        Calendar cal = Calendar.getInstance();
        cal.setTime(original.getNgayBatDau());
        int originalDayOfWeek = cal.get(Calendar.DAY_OF_WEEK); // 1=CN, 2=T2...
        String originalDayStr = standardDays.get(originalDayOfWeek - 1);

        // Lặp qua các tuần (bắt đầu từ tuần hiện tại)
        for (int i = startWeekIndex; i < listWeek.size(); i++) {
            int weeksDiff = i - startWeekIndex;
            
            // Trong mỗi tuần, kiểm tra từng ngày xem có được chọn không
            for (String day : selectedDays) {
                // Nếu là tuần hiện tại và ngày trùng với ngày gốc -> Bỏ qua (đã thêm)
                if (i == startWeekIndex && day.equals(originalDayStr)) {
                    continue;
                }
                
                int targetDayIndex = standardDays.indexOf(day) + 1; // 1..7
                if (targetDayIndex <= 0) continue; // Không tìm thấy thứ hợp lệ
                
                int dayDiff = targetDayIndex - originalDayOfWeek; // Chênh lệch trong cùng 1 tuần
                int totalDaysDiff = (weeksDiff * 7) + dayDiff;
                
                // Nếu là tuần hiện tại, chỉ thêm các ngày trong tương lai (hoặc giữ nguyên logic thêm cả quá khứ nếu muốn)
                // Ở đây tôi giữ logic: chỉ thêm nếu ngày đó chưa qua (đối với tuần hiện tại) để tránh tạo deadline quá khứ
                if (i == startWeekIndex && totalDaysDiff < 0) continue;

                // Nếu muốn giới hạn chỉ trong 10 tuần, kiểm tra ngày cộng thêm có vượt quá giới hạn không (tùy chọn)
                
                Deadline repeatedDl = createRepeatedDeadline(original, totalDaysDiff);
                
                // Cần xác định deadline này thuộc về tuần nào?
                // Vì logic ở trên đang giả định tuần i chứa deadline. 
                // Nhưng nếu dayDiff làm nó nhảy sang tuần sau hoặc tuần trước thì sao?
                // Ví dụ: Gốc CN (Tuần 1). Chọn Thứ 2.
                // dayDiff = 2 - 1 = +1. -> Vẫn Tuần 1. OK.
                // Ví dụ: Gốc Thứ 7 (Tuần 1). Chọn CN.
                // dayDiff = 1 - 7 = -6. totalDaysDiff = -6.
                // -> Ngày CN này thuộc về tuần trước đó (Tuần 0?). 
                // Nhưng vòng lặp đang xét tuần i.
                // Chính xác hơn: ta đang thêm vào tuần i deadline của "Thứ X của tuần i".
                
                // Vậy nên add vào adapterWeek ở vị trí i là ĐÚNG.
                adapterWeek.addDeadlineToWeek(i, repeatedDl);
            }
        }
    }

    // Hàm hỗ trợ tạo bản sao deadline và cộng ngày
    private Deadline createRepeatedDeadline(Deadline original, int daysOffset) {
        Date newStart = addDays(original.getNgayBatDau(), daysOffset);
        Date newEnd = addDays(original.getNgayKetThuc(), daysOffset);

        Deadline copy = new Deadline(
                original.getTieuDe(),
                original.getNoiDung(),
                newStart,
                newEnd,
                original.getIcon()
        );
        
        // Sao chép các thuộc tính phụ
        copy.setReminder(original.getReminderText());
        copy.setRepeat(original.getRepeatText());
        copy.setPinned(false);
        
        return copy;
    }

    private Date addDays(Date date, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_MONTH, days);
        return cal.getTime();
    }
}
