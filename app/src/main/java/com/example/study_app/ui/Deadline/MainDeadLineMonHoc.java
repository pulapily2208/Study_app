package com.example.study_app.ui.Deadline;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.study_app.R;
import com.example.study_app.data.DeadlineDao;
import com.example.study_app.ui.Deadline.Adapters.AdapterWeek;
import com.example.study_app.ui.Deadline.Models.Deadline;
import com.example.study_app.ui.Deadline.Models.Week;
import com.example.study_app.ui.Subject.Model.Subject;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class MainDeadLineMonHoc extends AppCompatActivity {

    private RecyclerView lvWeeks;
    private Toolbar toolbar;
    private FloatingActionButton fabAddDeadline;

    private DeadlineDao dbHelper;
    private AdapterWeek adapterWeek;

    private String subjectMaHp;
    private Subject currentSubject;

    private ActivityResultLauncher<Intent> deadlineLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.deadline_main_mon_hoc);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        toolbar = findViewById(R.id.toolbar);
        lvWeeks = findViewById(R.id.lvItemTuan);
        fabAddDeadline = findViewById(R.id.fab_add_deadline);
        dbHelper = new DeadlineDao(this);

        Intent intent = getIntent();
        subjectMaHp = intent.getStringExtra("SUBJECT_MA_HP");

        if (subjectMaHp == null || subjectMaHp.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không có thông tin môn học.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        currentSubject = dbHelper.getSubjectByMaHp(subjectMaHp);
        if (currentSubject == null || currentSubject.ngayBatDau == null || currentSubject.ngayKetThuc == null) {
            Toast.makeText(this, "Lỗi: Không thể tải thông tin môn học.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        toolbar.setTitle(currentSubject.tenHp != null ? currentSubject.tenHp : "Deadlines");
        toolbar.setNavigationOnClickListener(v -> finish());

        lvWeeks.setLayoutManager(new LinearLayoutManager(this));

        adapterWeek = new AdapterWeek(this, new ArrayList<>(), subjectMaHp, currentSubject.ngayBatDau);
        lvWeeks.setAdapter(adapterWeek);

        setupDeadlineLauncher();
        loadAndDisplayWeeks();
        setupAdapterListeners();

        fabAddDeadline.setOnClickListener(v -> {
            int currentWeek = getCurrentWeekIndex();
            onAddDeadline(currentWeek);
        });
    }

    private void loadAndDisplayWeeks() {
        if (currentSubject == null) return;

        ArrayList<Week> weekList = new ArrayList<>();
        for (int i = 0; i < currentSubject.soTuan; i++) {
            weekList.add(new Week("Tuần " + (i + 1)));
        }

        adapterWeek.setWeeks(weekList);

        int currentWeek = getCurrentWeekIndex();
        if (currentWeek >= 0 && currentWeek < adapterWeek.getItemCount()) {
            lvWeeks.post(() -> lvWeeks.smoothScrollToPosition(currentWeek));
        }
    }

    private void setupDeadlineLauncher() {
        deadlineLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Serializable extra = result.getData().getSerializableExtra(InputDeadlineActivity.KEY_TAI_KHOAN);
                        if (extra instanceof Deadline) {
                            Deadline returnedDeadline = (Deadline) extra;
                            if (returnedDeadline.getId() > 0) { // Is an update
                                handleUpdateDeadline(returnedDeadline, result.getData());
                            } else { // Is a new deadline
                                handleAddDeadline(returnedDeadline, result.getData());
                            }
                        }
                    }
                }
        );
    }

    private void handleUpdateDeadline(Deadline deadline, Intent data) {
        long dbResult = dbHelper.updateDeadline(deadline);
        if (dbResult != -1) {
            Toast.makeText(this, "Đã cập nhật deadline", Toast.LENGTH_SHORT).show();
            
            int affectedWeek = data.getIntExtra("weekIndex", -1);
            int originalWeek = data.getIntExtra("originalWeekIndex", -1);

            if (affectedWeek != -1) adapterWeek.notifyItemChanged(affectedWeek);
            if (originalWeek != -1 && originalWeek != affectedWeek) adapterWeek.notifyItemChanged(originalWeek);

        } else {
            Toast.makeText(this, "Cập nhật thất bại!", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleAddDeadline(Deadline deadline, Intent data) {
        String repeatType = deadline.getRepeatText();

        if (Deadline.REPEAT_TYPE_WEEKLY.equals(repeatType)) {
            createRepeatingDeadlines(deadline, 7);
        } else if (Deadline.REPEAT_TYPE_DAILY.equals(repeatType)){
            createRepeatingDeadlines(deadline, 1);
        } else if (Deadline.REPEAT_TYPE_WEEKDAYS.equals(repeatType)) {
            createDeadlinesToEndOfWeek(deadline);
        } else {
             if (dbHelper.addDeadline(deadline, subjectMaHp) == -1) {
                 Toast.makeText(this, "Thêm mới thất bại!", Toast.LENGTH_SHORT).show();
                 return;
            }
        }

        Toast.makeText(this, "Đã thêm deadline", Toast.LENGTH_SHORT).show();
        adapterWeek.notifyDataSetChanged();
        int weekToScroll = data.getIntExtra("weekIndex", -1);
        if (weekToScroll != -1) {
            lvWeeks.post(() -> lvWeeks.smoothScrollToPosition(weekToScroll));
        }
    }
    private void createDeadlinesToEndOfWeek(Deadline baseDeadline) {
        long duration = baseDeadline.getNgayKetThuc().getTime() - baseDeadline.getNgayBatDau().getTime();

        Calendar cal = Calendar.getInstance();
        cal.setTime(baseDeadline.getNgayBatDau());

        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);

        // Tính số ngày còn lại đến Chủ Nhật
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK); // 1 = CN, 2 = T2, ..., 7 = T7
        int daysToEndOfWeek = Calendar.SUNDAY - dayOfWeek;
        if (daysToEndOfWeek < 0) daysToEndOfWeek += 7; // nếu ngày gốc là CN, vẫn tính đúng

        for (int i = 0; i <= daysToEndOfWeek; i++) {
            cal.set(Calendar.HOUR_OF_DAY, hour);
            cal.set(Calendar.MINUTE, minute);

            Date startDate = cal.getTime();
            Date endDate = new Date(startDate.getTime() + duration);

            Deadline instance = new Deadline();
            instance.setTieuDe(baseDeadline.getTieuDe());
            instance.setNoiDung(baseDeadline.getNoiDung());
            instance.setMaHp(baseDeadline.getMaHp());
            instance.setIcon(baseDeadline.getIcon());
            instance.setReminder(baseDeadline.getReminderText());
            instance.setRepeat(Deadline.REPEAT_TYPE_NONE);
            instance.setNgayBatDau(startDate);
            instance.setNgayKetThuc(endDate);

            dbHelper.addDeadline(instance, baseDeadline.getMaHp());

            cal.add(Calendar.DATE, 1); // sang ngày tiếp theo
        }
    }


    private void createRepeatingDeadlines(Deadline baseDeadline, int intervalInDays) {
        long duration = baseDeadline.getNgayKetThuc().getTime() - baseDeadline.getNgayBatDau().getTime();

        Calendar startCal = Calendar.getInstance();
        startCal.setTime(baseDeadline.getNgayBatDau());

        while(startCal.getTime().before(currentSubject.ngayKetThuc)){
            Deadline instance = new Deadline();
            instance.setTieuDe(baseDeadline.getTieuDe());
            instance.setNoiDung(baseDeadline.getNoiDung());
            instance.setMaHp(subjectMaHp);
            instance.setIcon(baseDeadline.getIcon());
            instance.setReminder(baseDeadline.getReminderText());
            instance.setRepeat(Deadline.REPEAT_TYPE_NONE);

            Date startDate = startCal.getTime();
            Date endDate = new Date(startDate.getTime() + duration);

            instance.setNgayBatDau(startDate);
            instance.setNgayKetThuc(endDate);
            
            dbHelper.addDeadline(instance, subjectMaHp);

            startCal.add(Calendar.DATE, intervalInDays);
        }
    }


    private void setupAdapterListeners() {
        adapterWeek.setOnAddDeadlineListener(this::onAddDeadline);

        adapterWeek.setOnDeadlineInteractionListener(new AdapterWeek.OnDeadlineInteractionListener() {
            @Override
            public void onDeadlineClick(Deadline deadline) {
                Intent intent = new Intent(MainDeadLineMonHoc.this, InfoDeadlineActivity.class);
                intent.putExtra(InfoDeadlineActivity.EXTRA_DEADLINE, deadline);
                startActivity(intent);
            }

            @Override
            public void onEditDeadline(Deadline deadline) {
                Intent editIntent = new Intent(MainDeadLineMonHoc.this, InputDeadlineActivity.class);
                int weekIndex = -1;
                if(currentSubject.ngayBatDau != null){
                     long diffMillis = deadline.getNgayBatDau().getTime() - currentSubject.ngayBatDau.getTime();
                     long days = TimeUnit.MILLISECONDS.toDays(diffMillis);
                     weekIndex = (int) (days/7);
                }

                editIntent.putExtra("weekIndex", weekIndex);
                editIntent.putExtra(InputDeadlineActivity.EDIT_DEADLINE, deadline);
                editIntent.putExtra(InputDeadlineActivity.SUBJECT_MA_HP, subjectMaHp);
                if (currentSubject != null) {
                    editIntent.putExtra(InputDeadlineActivity.SUBJECT_START_DATE, currentSubject.ngayBatDau.getTime());
                }
                deadlineLauncher.launch(editIntent);
            }

            @Override
            public void onDeleteDeadline(Deadline deadline) {
                Toast.makeText(MainDeadLineMonHoc.this, "Đã xóa deadline", Toast.LENGTH_SHORT).show();
                adapterWeek.notifyDataSetChanged();
            }

            @Override
            public void onStateChanged(Deadline deadline, boolean isCompleted) {
                 adapterWeek.notifyDataSetChanged();
            }
        });
    }

    private void onAddDeadline(int weekIndex) {
        Intent intent = new Intent(MainDeadLineMonHoc.this, InputDeadlineActivity.class);
        intent.putExtra("weekIndex", weekIndex);
        intent.putExtra(InputDeadlineActivity.SUBJECT_MA_HP, subjectMaHp);
        if (currentSubject != null && currentSubject.ngayBatDau != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(currentSubject.ngayBatDau);
            cal.add(Calendar.WEEK_OF_YEAR, weekIndex);
            intent.putExtra(InputDeadlineActivity.WEEK_START_DATE, cal.getTimeInMillis());
            intent.putExtra(InputDeadlineActivity.SUBJECT_START_DATE, currentSubject.ngayBatDau.getTime());
        }
        deadlineLauncher.launch(intent);
    }

    private int getCurrentWeekIndex() {
        if (currentSubject == null || currentSubject.ngayBatDau == null) {
            return 0;
        }
        long diffMillis = new Date().getTime() - currentSubject.ngayBatDau.getTime();
        if (diffMillis < 0) {
            return 0; // Course hasn't started
        }
        long diffDays = TimeUnit.MILLISECONDS.toDays(diffMillis);
        return (int) (diffDays / 7);
    }
}
