package com.example.study_app.ui.Score;

import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.study_app.R;
import com.example.study_app.data.DatabaseHelper;
import com.example.study_app.data.ScoreDao;

public class InputScoreActivity extends AppCompatActivity {
    private TextView tvMaMon, tvTenMonHoc, tvGpa;
    private EditText edtDiemChuyenCan, edtDiemGiuaKi, edtDiemCuoiKi;

    private ImageView ivBack, ivSave;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scrore_input);

        mapViews();
        receiveData();
        setupListeners();
    }

    private void mapViews(){
        tvMaMon = findViewById(R.id.tvMaMon);
        tvTenMonHoc = findViewById(R.id.tvTenMonHoc);
        tvGpa = findViewById(R.id.tvGpa);
        edtDiemChuyenCan = findViewById(R.id.edtDiemChuyenCan);
        edtDiemGiuaKi = findViewById(R.id.edtDiemGiuaKi);
        edtDiemCuoiKi = findViewById(R.id.edtDiemCuoiKi);

        ivBack = findViewById(R.id.ivBack);
        ivSave = findViewById(R.id.ivSave);
    }

    private void receiveData() {
        String maMon = getIntent().getStringExtra("subject_code");
        String tenMonHoc = getIntent().getStringExtra("subject_name");

        tvMaMon.setText(maMon);
        tvTenMonHoc.setText(tenMonHoc);

        // Lấy điểm đã có từ CSDL
        ScoreDao scoreDao = new ScoreDao(this);
        Cursor cursor = scoreDao.getScore(maMon);

        if (cursor != null && cursor.moveToFirst()) {
            // Giả định tên các cột điểm trong CSDL
            int ccIndex = cursor.getColumnIndex("diem_chuyen_can");
            int gkIndex = cursor.getColumnIndex("diem_giua_ki");
            int ckIndex = cursor.getColumnIndex("diem_cuoi_ki");

            if (ccIndex != -1) {
                edtDiemChuyenCan.setText(String.valueOf(cursor.getFloat(ccIndex)));
            }
            if (gkIndex != -1) {
                edtDiemGiuaKi.setText(String.valueOf(cursor.getFloat(gkIndex)));
            }
            if (ckIndex != -1) {
                edtDiemCuoiKi.setText(String.valueOf(cursor.getFloat(ckIndex)));
            }

            cursor.close();
        }
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());
        ivSave.setOnClickListener(v -> saveScore());

        TextWatcher gpaCalculatorWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateGpa();
            }
        };

        edtDiemChuyenCan.addTextChangedListener(gpaCalculatorWatcher);
        edtDiemGiuaKi.addTextChangedListener(gpaCalculatorWatcher);
        edtDiemCuoiKi.addTextChangedListener(gpaCalculatorWatcher);
    }

    private void saveScore() {

        Float cc = getScore(edtDiemChuyenCan);
        Float gk = getScore(edtDiemGiuaKi);
        Float ck = getScore(edtDiemCuoiKi);

        if (cc == null || gk == null || ck == null) {
            Toast.makeText(this, "Vui lòng nhập đủ điểm", Toast.LENGTH_SHORT).show();
            return;
        }

        Float gpa = calculateGpa(cc, gk, ck);

        ScoreDao db = new ScoreDao(this);

        db.saveScore(tvMaMon.getText().toString(), cc, gk, ck, gpa);

        Toast.makeText(this, "Đã lưu điểm", Toast.LENGTH_SHORT).show();
        finish();

    }

    private void updateGpa() {
        Float cc = getScore(edtDiemChuyenCan);
        Float gk = getScore(edtDiemGiuaKi);
        Float ck = getScore(edtDiemCuoiKi);

        Float gpa = calculateGpa(cc, gk, ck);

        if (gpa != null) {
            tvGpa.setText(String.format("%.1f", gpa));
        } else {
            tvGpa.setText("");
        }
    }

    private Float calculateGpa(Float cc, Float gk, Float ck) {
        if(cc == null || gk == null || ck == null) return null;
        return (cc * 0.1f + gk * 0.3f + ck * 0.6f);
    }

    private Float getScore(EditText edt) {
        String scoreStr = edt.getText().toString();
        if (scoreStr.isEmpty()) {
            return null;
        }

        try {
            return Float.parseFloat(scoreStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
