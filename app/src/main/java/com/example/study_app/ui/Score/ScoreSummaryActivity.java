package com.example.study_app.ui.Score;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.study_app.R;
import com.example.study_app.data.DatabaseHelper;
import com.example.study_app.data.ScoreDao;
import com.example.study_app.ui.common.NavbarHelper;

import java.util.List;
import java.util.Locale;

public class ScoreSummaryActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private ScoreDao scoreDao;
    private TextView tvCumulative, tvCredits;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.score_summary_activity);

        NavbarHelper.setupNavbar(this, R.id.btnKetQuaHocTap);

        dbHelper = new DatabaseHelper(this);
        scoreDao = new ScoreDao(dbHelper);

        tvCumulative = findViewById(R.id.tvCumulativeGpa);
        tvCredits = findViewById(R.id.tvTotalCredits);
        RecyclerView rv = findViewById(R.id.rvScores);
        rv.setLayoutManager(new LinearLayoutManager(this));

        List<ScoreDao.ScoreWithSemester> rows = scoreDao.getScoresWithSemester();
        rv.setAdapter(new ScoreSummaryAdapter(rows));

        float sumWeighted = 0f;
        int sumCredits = 0;
        for (ScoreDao.ScoreWithSemester r : rows) {
            if (r.gpa != null && r.credits > 0) {
                sumWeighted += r.gpa * r.credits;
                sumCredits += r.credits;
            }
        }
        if (sumCredits > 0) {
            tvCumulative.setText(String.format(Locale.US, "%.2f", sumWeighted / sumCredits));
        } else {
            tvCumulative.setText("-");
        }
        tvCredits.setText(String.valueOf(sumCredits));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            dbHelper.close();
        } catch (Exception ignored) {
        }
    }
}
