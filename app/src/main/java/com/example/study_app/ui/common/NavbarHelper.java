package com.example.study_app.ui.common;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.LinearLayout;

import com.example.study_app.R;
import com.example.study_app.ui.Curriculum.CurriculumActivity;
import com.example.study_app.ui.Notes.NotesActivity;
import com.example.study_app.ui.Score.InputScoreActivity;
import com.example.study_app.ui.Subject.SubjectListActivity;
import com.example.study_app.ui.Timetable.TimetableWeek;
import com.example.study_app.ui.Deadline.MainDeadLine;

public class NavbarHelper {
    public static void setupNavbar(final Activity activity, int activeItemId) {
        int[] ids = new int[] { R.id.btnSubject, R.id.btnNote, R.id.btnDeadLine, R.id.btnTimetable,
                R.id.btnKetQuaHocTap, R.id.btnCurriculum };
        for (int id : ids) {
            View v = activity.findViewById(id);
            if (v == null)
                continue;
            v.setSelected(id == activeItemId);
        }

        // Wire listeners if available
        LinearLayout btnSubject = activity.findViewById(R.id.btnSubject);
        if (btnSubject != null)
            btnSubject.setOnClickListener(v -> {
                if (!(activity instanceof SubjectListActivity)) {
                    Intent i = new Intent(activity, SubjectListActivity.class);
                    activity.startActivity(i);
                } else {
                    // refresh or do nothing
                }
            });

        LinearLayout btnNote = activity.findViewById(R.id.btnNote);
        if (btnNote != null)
            btnNote.setOnClickListener(v -> {
                if (!(activity instanceof NotesActivity)) {
                    Intent i = new Intent(activity, NotesActivity.class);
                    activity.startActivity(i);
                }
            });

        LinearLayout btnDeadLine = activity.findViewById(R.id.btnDeadLine);
        if (btnDeadLine != null)
            btnDeadLine.setOnClickListener(v -> {
                if (!(activity instanceof MainDeadLine)) {
                    Intent i = new Intent(activity, MainDeadLine.class);
                    activity.startActivity(i);
                }
            });

        LinearLayout btnTimetable = activity.findViewById(R.id.btnTimetable);
        if (btnTimetable != null)
            btnTimetable.setOnClickListener(v -> {
                if (!(activity instanceof TimetableWeek)) {
                    Intent i = new Intent(activity, TimetableWeek.class);
                    activity.startActivity(i);
                }
            });

        LinearLayout btnKetQua = activity.findViewById(R.id.btnKetQuaHocTap);
        if (btnKetQua != null)
            btnKetQua.setOnClickListener(v -> {
                if (!(activity instanceof CurriculumActivity)) {
                    Intent i = new Intent(activity, CurriculumActivity.class);
                    activity.startActivity(i);
                }
            });

        LinearLayout btnCurriculum = activity.findViewById(R.id.btnCurriculum);
        if (btnCurriculum != null)
            btnCurriculum.setOnClickListener(v -> {
                if (!(activity instanceof CurriculumActivity)) {
                    Intent i = new Intent(activity, CurriculumActivity.class);
                    activity.startActivity(i);
                }
            });
    }
}
