package com.example.study_app.ui.Subject;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.study_app.R;
import com.example.study_app.data.CurriculumDao;
import com.example.study_app.data.DatabaseHelper;
import com.example.study_app.data.SubjectDao;
import com.example.study_app.ui.Curriculum.Model.Curriculum;
import com.example.study_app.ui.Subject.Model.Subject;
import com.example.study_app.ui.common.NavbarHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class SubjectAddActivity extends AppCompatActivity {

    private AutoCompleteTextView etMaMon;
    private EditText etTenMon, etTenGv, etSoTc, etNgayBatDau, etNgayKetThuc, etGioBatDau, etGioKetThuc,
            etPhongHoc, etGhiChu;
    private RadioGroup rgLoaiMon;
    private RadioButton rbBatBuoc, rbTuChon;
    private TextView tvSoTuan, tvNhomTuChon;
    private LinearLayout khungChonMau, layoutNgayBatDau, layoutNgayKetThuc;
    private ImageView ivQuayLai, ivLuu;
    private TextView tvTieuDeHoatDong;

    private DatabaseHelper dbHelper;
    private SubjectDao subjectDao;
    private CurriculumDao curriculumDao;
    private boolean cheDoChinhSua = false;
    private String maHpToEdit = null;
    private String tenHocKyHienTai;

    private String mauDuocChon = null;
    private final List<View> cacViewMau = new ArrayList<>();
    private View viewMauDuocChon = null;

    private Calendar lichNgayBatDau = null;
    private Calendar lichNgayKetThuc = null;
    private int soTuanTinhDuoc = 0;
    private ArrayAdapter<String> maMonAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.subject_add);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DatabaseHelper(this);
        subjectDao = new SubjectDao(dbHelper);
        curriculumDao = new CurriculumDao(dbHelper);

        findViews();
        setupAutoComplete();
        setClickListeners();
        caiDatBoChonMau();
        NavbarHelper.setupNavbar(this, R.id.btnSubject);

        tvNhomTuChon.setVisibility(View.GONE);

        if (getIntent().hasExtra("SEMESTER_NAME")) {
            tenHocKyHienTai = getIntent().getStringExtra("SEMESTER_NAME");
        } else {
            Toast.makeText(this, R.string.error_no_semester_info, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        kiemTraCheDoChinhSuaHoacThem();
    }

    private void findViews() {
        etMaMon = findViewById(R.id.etSubjectCode);
        etTenMon = findViewById(R.id.etSubjectName);
        etTenGv = findViewById(R.id.etLecturerName);
        etSoTc = findViewById(R.id.etCredits);
        etNgayBatDau = findViewById(R.id.etStartDate);
        etNgayKetThuc = findViewById(R.id.etEndDate);
        etGioBatDau = findViewById(R.id.etStartTime);
        etGioKetThuc = findViewById(R.id.etEndTime);
        etPhongHoc = findViewById(R.id.etLocation);
        etGhiChu = findViewById(R.id.etNotes);
        rgLoaiMon = findViewById(R.id.rgSubjectType);
        rbBatBuoc = findViewById(R.id.rb_compulsory);
        rbTuChon = findViewById(R.id.rb_elective);
        tvSoTuan = findViewById(R.id.tvCalculatedWeeks);
        tvNhomTuChon = findViewById(R.id.tvChoiceGroup);
        khungChonMau = findViewById(R.id.colorPickerContainer);
        ivQuayLai = findViewById(R.id.ivBack);
        ivLuu = findViewById(R.id.ivSave);
        tvTieuDeHoatDong = findViewById(R.id.tvActivityTitle);
        layoutNgayBatDau = findViewById(R.id.layoutStartDate);
        layoutNgayKetThuc = findViewById(R.id.layoutEndDate);
    }

    private void setupAutoComplete() {
        maMonAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line);
        etMaMon.setAdapter(maMonAdapter);

        etMaMon.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (etMaMon.isPerformingCompletion()) {
                    return;
                }
                List<String> suggestions = curriculumDao.searchSubjectCodes(s.toString());
                maMonAdapter.clear();
                maMonAdapter.addAll(suggestions);
                maMonAdapter.notifyDataSetChanged();
            }
        });

        etMaMon.setOnItemClickListener((parent, view, position, id) -> {
            String selectedMaHp = maMonAdapter.getItem(position);
            if (selectedMaHp != null) {
                Curriculum curriculum = curriculumDao.getCurriculumDetailsByMaHp(selectedMaHp);
                if (curriculum != null) {
                    tuDienThongTinMonHoc(curriculum);
                }
            }
        });

        etMaMon.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String currentCode = etMaMon.getText().toString().trim();
                if (!TextUtils.isEmpty(currentCode)) {
                    Curriculum curriculum = curriculumDao.getCurriculumDetailsByMaHp(currentCode);
                    if (curriculum == null) {
                        Toast.makeText(SubjectAddActivity.this, "Mã môn không tồn tại trong chương trình đào tạo!",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void tuDienThongTinMonHoc(Curriculum curriculum) {
        etTenMon.setText(curriculum.getTenHp());
        etSoTc.setText(String.valueOf(curriculum.getSoTinChi()));

        if (curriculum.getLoaiHp() != null) {
            if ("Tự chọn".equals(curriculum.getLoaiHp())) {
                rbTuChon.setChecked(true);
                tvNhomTuChon.setText("Nhóm: " + curriculum.getNhomTuChon());
                tvNhomTuChon.setVisibility(View.VISIBLE);
            } else {
                rbBatBuoc.setChecked(true);
                tvNhomTuChon.setText("");
                tvNhomTuChon.setVisibility(View.GONE);
            }
        } else {
            rbBatBuoc.setChecked(true);
            tvNhomTuChon.setText("");
            tvNhomTuChon.setVisibility(View.GONE);
        }

        int totalPeriods = curriculum.getSoTietLyThuyet() + curriculum.getSoTietThucHanh();
        int credits = curriculum.getSoTinChi();
        String codeUpper = etMaMon.getText() != null ? etMaMon.getText().toString().trim().toUpperCase()
                : "";
        if (codeUpper.startsWith("DEFE")) {
            soTuanTinhDuoc = 4;
        } else if (codeUpper.startsWith("PHYE")) {
            soTuanTinhDuoc = 7;
        } else if (credits > 0) {
            soTuanTinhDuoc = totalPeriods / credits;
        } else {
            soTuanTinhDuoc = 0;
        }
        tvSoTuan.setText(String.valueOf(soTuanTinhDuoc));

        if (lichNgayBatDau != null) {
            tuTinhNgayKetThuc();
        }
    }

    private void tuTinhNgayKetThuc() {
        if (lichNgayBatDau != null && soTuanTinhDuoc > 0) {
            lichNgayKetThuc = (Calendar) lichNgayBatDau.clone();
            lichNgayKetThuc.add(Calendar.DAY_OF_YEAR, (soTuanTinhDuoc * 7) - 1);

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            etNgayKetThuc.setText(dateFormat.format(lichNgayKetThuc.getTime()));
        } else {
            etNgayKetThuc.setText("");
            lichNgayKetThuc = null;
        }
    }

    private void setClickListeners() {
        ivQuayLai.setOnClickListener(v -> finish());
        ivLuu.setOnClickListener(v -> luuMonHoc());

        layoutNgayBatDau.setOnClickListener(v -> chonNgay(etNgayBatDau));
        etNgayBatDau.setOnClickListener(v -> chonNgay(etNgayBatDau));
        layoutNgayKetThuc.setOnClickListener(v -> chonNgay(etNgayKetThuc));
        etNgayKetThuc.setOnClickListener(v -> chonNgay(etNgayKetThuc));

        etGioBatDau.setOnClickListener(v -> chonGio(etGioBatDau));
        etGioKetThuc.setOnClickListener(v -> chonGio(etGioKetThuc));
    }

    private void kiemTraCheDoChinhSuaHoacThem() {
        if (getIntent().hasExtra("SUBJECT_ID")) {
            cheDoChinhSua = true;
            maHpToEdit = getIntent().getStringExtra("SUBJECT_ID");
            tvTieuDeHoatDong.setText(R.string.activity_title_edit);
            etMaMon.setEnabled(false);

            Subject subject = subjectDao.getSubjectByMaHp(maHpToEdit);
            if (subject != null) {
                napDuLieuLenUI(subject);
            } else {
                Toast.makeText(this, R.string.error_subject_not_found, Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            cheDoChinhSua = false;
            tvTieuDeHoatDong.setText(R.string.activity_title_add);
        }
    }

    private void napDuLieuLenUI(Subject subject) {
        etMaMon.setText(subject.maHp);
        etTenMon.setText(subject.tenHp);
        etTenGv.setText(subject.tenGv);
        etSoTc.setText(String.valueOf(subject.soTc));
        etGhiChu.setText(subject.ghiChu);
        etPhongHoc.setText(subject.phongHoc);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        if (subject.ngayBatDau != null) {
            etNgayBatDau.setText(dateFormat.format(subject.ngayBatDau));
            lichNgayBatDau = Calendar.getInstance();
            lichNgayBatDau.setTime(subject.ngayBatDau);
        }
        if (subject.ngayKetThuc != null) {
            etNgayKetThuc.setText(dateFormat.format(subject.ngayKetThuc));
            lichNgayKetThuc = Calendar.getInstance();
            lichNgayKetThuc.setTime(subject.ngayKetThuc);
        }

        soTuanTinhDuoc = subject.soTuan;
        tvSoTuan.setText(String.valueOf(soTuanTinhDuoc));

        if (subject.gioBatDau != null)
            etGioBatDau.setText(timeFormat.format(subject.gioBatDau));
        if (subject.gioKetThuc != null)
            etGioKetThuc.setText(timeFormat.format(subject.gioKetThuc));

        Curriculum curriculum = curriculumDao.getCurriculumDetailsByMaHp(subject.maHp);
        if ("Tự chọn".equals(subject.loaiMon)) {
            rbTuChon.setChecked(true);
            if (curriculum != null && curriculum.getNhomTuChon() != null && !curriculum.getNhomTuChon().isEmpty()) {
                tvNhomTuChon.setText("Nhóm: " + curriculum.getNhomTuChon());
                tvNhomTuChon.setVisibility(View.VISIBLE);
            } else {
                tvNhomTuChon.setVisibility(View.GONE);
            }
        } else {
            rbBatBuoc.setChecked(true);
            tvNhomTuChon.setText("");
            tvNhomTuChon.setVisibility(View.GONE);
        }

        if (subject.mauSac != null && !subject.mauSac.isEmpty()) {
            for (View colorView : cacViewMau) {
                if (colorView.getTag() != null && colorView.getTag().toString().equalsIgnoreCase(subject.mauSac)) {
                    chonMau(colorView);
                    break;
                }
            }
        }
    }

    private void luuMonHoc() {
        Subject subject = new Subject();
        String error = xacThucVaXayDungMonHoc(subject);
        if (error != null) {
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            return;
        }

        String conflictMaHp = timXungDotThoiGian(subject);
        if (conflictMaHp != null) {
            Toast.makeText(this, "Trùng giờ với môn: " + conflictMaHp, Toast.LENGTH_LONG).show();
            return;
        }

        Intent resultIntent = new Intent();

        if (cheDoChinhSua) {
            int rowsAffected = subjectDao.updateSubject(subject);
            if (rowsAffected > 0) {
                Toast.makeText(this, R.string.update_subject_success, Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                Toast.makeText(this, R.string.update_subject_failed, Toast.LENGTH_SHORT).show();
            }
        } else {
            long newRowId = subjectDao.addOrEnrollSubject(subject);
            if (newRowId != -1) {
                Toast.makeText(this, R.string.add_subject_success, Toast.LENGTH_SHORT).show();

                resultIntent.putExtra("UPDATED_SEMESTER_NAME", subject.tenHk);
                resultIntent.putExtra("UPDATED_MA_HP", subject.maHp);
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                Toast.makeText(this, R.string.add_subject_failed, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String xacThucVaXayDungMonHoc(Subject subject) {
        String maHp = etMaMon.getText().toString().trim();
        String tenHp = etTenMon.getText().toString().trim();
        String soTcStr = etSoTc.getText().toString().trim();
        String gioBatDauStr = etGioBatDau.getText().toString().trim();
        String gioKetThucStr = etGioKetThuc.getText().toString().trim();

        if (TextUtils.isEmpty(maHp))
            return getString(R.string.subject_code_required);
        if (TextUtils.isEmpty(tenHp))
            return getString(R.string.subject_name_required);
        if (TextUtils.isEmpty(soTcStr))
            return getString(R.string.credits_required);
        if (TextUtils.isEmpty(gioBatDauStr))
            return getString(R.string.start_time_required);
        if (TextUtils.isEmpty(gioKetThucStr))
            return getString(R.string.end_time_required);
        if (mauDuocChon == null)
            return getString(R.string.color_required);
        if (rgLoaiMon.getCheckedRadioButtonId() == -1)
            return getString(R.string.subject_type_required);

        if (!cheDoChinhSua) {
            String loiMaMon = kiemTraDieuKienMaMon(maHp);
            if (loiMaMon != null)
                return loiMaMon;
        }

        int soTc;
        try {
            soTc = Integer.parseInt(soTcStr);
        } catch (NumberFormatException e) {
            return getString(R.string.credits_invalid);
        }

        damBaoNgayKetThucTuDong();
        if (lichNgayBatDau == null || lichNgayKetThuc == null)
            return "Vui lòng chọn đầy đủ ngày bắt đầu và kết thúc!";
        if (lichNgayKetThuc.before(lichNgayBatDau))
            return "Ngày kết thúc không thể trước ngày bắt đầu";

        Date gioBatDau, gioKetThuc;
        try {
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            gioBatDau = timeFormat.parse(gioBatDauStr);
            gioKetThuc = timeFormat.parse(gioKetThucStr);
            if (!gioBatDau.before(gioKetThuc))
                return "Giờ bắt đầu phải trước giờ kết thúc";
        } catch (ParseException e) {
            return getString(R.string.date_time_format_invalid);
        }

        subject.maHp = maHp;
        subject.tenHp = tenHp;
        subject.tenGv = etTenGv.getText().toString().trim();
        subject.soTc = soTc;
        subject.ghiChu = etGhiChu.getText().toString().trim();
        subject.phongHoc = etPhongHoc.getText().toString().trim();
        subject.ngayBatDau = lichNgayBatDau.getTime();
        subject.ngayKetThuc = lichNgayKetThuc.getTime();
        subject.gioBatDau = gioBatDau;
        subject.gioKetThuc = gioKetThuc;
        subject.loaiMon = ((RadioButton) findViewById(rgLoaiMon.getCheckedRadioButtonId())).getText().toString();
        subject.mauSac = mauDuocChon;
        subject.soTuan = soTuanTinhDuoc;
        subject.tenHk = tenHocKyHienTai;

        return null;
    }

    private String kiemTraDieuKienMaMon(String maHp) {
        Curriculum curriculum = curriculumDao.getCurriculumDetailsByMaHp(maHp);
        if (curriculum == null)
            return "Mã môn không tồn tại trong chương trình đào tạo!";

        boolean isPrerequisiteMet = curriculumDao.checkPrerequisiteStatus(maHp, 1, subjectDao);
        if (!isPrerequisiteMet)
            return "Chưa đạt điều kiện tiên quyết để thêm môn!";

        return null;
    }

    private void damBaoNgayKetThucTuDong() {
        if ((lichNgayKetThuc == null || TextUtils.isEmpty(etNgayKetThuc.getText().toString()))
                && lichNgayBatDau != null && soTuanTinhDuoc > 0) {
            tuTinhNgayKetThuc();
        }
    }

    private String timXungDotThoiGian(Subject newSubject) {
        List<Subject> existing = subjectDao.getSubjectsBySemester(tenHocKyHienTai);
        if (existing == null || existing.isEmpty())
            return null;

        Date newStartDate = newSubject.ngayBatDau;
        Date newEndDate = newSubject.ngayKetThuc;
        Date newStartTime = newSubject.gioBatDau;
        Date newEndTime = newSubject.gioKetThuc;

        Calendar cal = Calendar.getInstance();
        cal.setTime(newStartDate);
        int newDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);

        for (Subject s : existing) {
            if (s == null || s.maHp == null)
                continue;
            if (s.maHp.equalsIgnoreCase(newSubject.maHp))
                continue;
            if (s.ngayBatDau == null || s.ngayKetThuc == null || s.gioBatDau == null || s.gioKetThuc == null)
                continue;

            if (newEndDate.before(s.ngayBatDau) || newStartDate.after(s.ngayKetThuc))
                continue;

            cal.setTime(s.ngayBatDau);
            if (newDayOfWeek != cal.get(Calendar.DAY_OF_WEEK))
                continue;

            boolean timeOverlap = newStartTime.before(s.gioKetThuc) && s.gioBatDau.before(newEndTime);
            if (timeOverlap)
                return s.maHp;
        }
        return null;
    }

    private void chonNgay(final EditText editText) {
        com.google.android.material.datepicker.MaterialDatePicker.Builder<Long> builder = com.google.android.material.datepicker.MaterialDatePicker.Builder
                .datePicker()
                .setTitleText(getString(R.string.select_date));

        String currentText = editText.getText().toString().trim();
        if (!TextUtils.isEmpty(currentText)) {
            try {
                Date d = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(currentText);
                if (d != null) {
                    builder.setSelection(d.getTime());
                }
            } catch (ParseException ignored) {
            }
        }

        com.google.android.material.datepicker.MaterialDatePicker<Long> picker = builder.build();

        picker.addOnPositiveButtonClickListener(selectionMillis -> {
            Calendar selectedCalendar = Calendar.getInstance();
            selectedCalendar.setTimeInMillis(selectionMillis);

            String selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%d",
                    selectedCalendar.get(Calendar.DAY_OF_MONTH),
                    selectedCalendar.get(Calendar.MONTH) + 1,
                    selectedCalendar.get(Calendar.YEAR));
            editText.setText(selectedDate);

            selectedCalendar.set(Calendar.HOUR_OF_DAY, 0);
            selectedCalendar.set(Calendar.MINUTE, 0);
            selectedCalendar.set(Calendar.SECOND, 0);
            selectedCalendar.set(Calendar.MILLISECOND, 0);

            if (editText.getId() == R.id.etStartDate) {
                lichNgayBatDau = selectedCalendar;
                tuTinhNgayKetThuc();
            } else if (editText.getId() == R.id.etEndDate) {
                lichNgayKetThuc = selectedCalendar;
                tinhVaHienSoTuan();
            }
        });

        picker.show(getSupportFragmentManager(), "date_picker");
    }

    private void tinhVaHienSoTuan() {
        if (lichNgayBatDau != null && lichNgayKetThuc != null) {
            if (lichNgayKetThuc.before(lichNgayBatDau)) {
                tvSoTuan.setText("0");
                soTuanTinhDuoc = 0;
                return;
            }
            long diffMillis = lichNgayKetThuc.getTimeInMillis() - lichNgayBatDau.getTimeInMillis();
            long diffInDays = TimeUnit.MILLISECONDS.toDays(diffMillis);
            soTuanTinhDuoc = (int) (diffInDays / 7) + 1;
            tvSoTuan.setText(String.valueOf(soTuanTinhDuoc));
        } else {
            tvSoTuan.setText("0");
            soTuanTinhDuoc = 0;
        }
    }

    private void chonGio(final EditText editText) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minuteOfHour) -> {
            String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minuteOfHour);
            editText.setText(selectedTime);
        }, hour, minute, true);
        timePickerDialog.show();
    }

    private void caiDatBoChonMau() {
        String[] colors;
        try {
            colors = getResources().getStringArray(R.array.subject_colors);
        } catch (Exception e) {
            Toast.makeText(this, R.string.error_color_picker, Toast.LENGTH_SHORT).show();
            colors = new String[] { "#CCCCCC" };
        }

        khungChonMau.removeAllViews();
        cacViewMau.clear();

        for (String colorHex : colors) {
            ImageView colorSwatch = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(100, 100);
            params.setMargins(8, 8, 8, 8);
            colorSwatch.setLayoutParams(params);

            GradientDrawable background = new GradientDrawable();
            background.setShape(GradientDrawable.OVAL);
            background.setColor(Color.parseColor(colorHex));
            background.setStroke(4, Color.TRANSPARENT);
            colorSwatch.setBackground(background);

            colorSwatch.setTag(colorHex);
            colorSwatch.setOnClickListener(this::chonMau);

            cacViewMau.add(colorSwatch);
            khungChonMau.addView(colorSwatch);
        }

        if (!cheDoChinhSua && viewMauDuocChon == null && !cacViewMau.isEmpty()) {
            chonMau(cacViewMau.get(0));
        }
    }

    private void chonMau(View view) {
        String newColor = (String) view.getTag();

        if (viewMauDuocChon != null) {
            GradientDrawable oldBg = (GradientDrawable) viewMauDuocChon.getBackground();
            oldBg.setStroke(4, Color.TRANSPARENT);
        }

        GradientDrawable newBg = (GradientDrawable) view.getBackground();
        newBg.setStroke(10, Color.BLACK);

        mauDuocChon = newColor;
        viewMauDuocChon = view;
    }
}
