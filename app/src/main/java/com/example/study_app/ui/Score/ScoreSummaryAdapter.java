package com.example.study_app.ui.Score;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.study_app.R;
import com.example.study_app.data.ScoreDao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Adapter hỗ trợ 2 loại item: header học kỳ và dòng môn học */
public class ScoreSummaryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ROW = 1;

    public static class HeaderItem {
        public final String semesterName;
        public final String semesterGpaText; // đã format

        public HeaderItem(String name, String gpaText) {
            this.semesterName = name;
            this.semesterGpaText = gpaText;
        }
    }

    private final List<Object> items = new ArrayList<>();

    public ScoreSummaryAdapter(List<ScoreDao.ScoreWithSemester> rows) {
        // Group theo semesterId
        Map<Integer, List<ScoreDao.ScoreWithSemester>> bySem = new HashMap<>();
        Map<Integer, String> semName = new HashMap<>();
        for (ScoreDao.ScoreWithSemester r : rows) {
            bySem.computeIfAbsent(r.semesterId, k -> new ArrayList<>()).add(r);
            if (r.semesterName != null)
                semName.put(r.semesterId, r.semesterName);
        }

        // Duyệt theo khóa tăng dần để có thứ tự ổn định
        List<Integer> sortedKeys = new ArrayList<>(bySem.keySet());
        java.util.Collections.sort(sortedKeys);
        for (Integer key : sortedKeys) {
            List<ScoreDao.ScoreWithSemester> group = bySem.get(key);
            if (group == null || group.isEmpty())
                continue;

            // Tính GPA học kỳ
            float sumWeighted = 0f;
            int sumCredits = 0;
            for (ScoreDao.ScoreWithSemester r : group) {
                if (r.gpa != null && r.credits > 0) {
                    sumWeighted += r.gpa * r.credits;
                    sumCredits += r.credits;
                }
            }
            String gpaText = sumCredits > 0 ? String.format(Locale.US, "%.1f", (sumWeighted / sumCredits)) : "-";
            String name = semName.getOrDefault(key, "Học kỳ " + key);
            items.add(new HeaderItem(name, gpaText));
            items.addAll(group);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return (items.get(position) instanceof HeaderItem) ? TYPE_HEADER : TYPE_ROW;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            View v = inflater.inflate(R.layout.score_summary_header, parent, false);
            return new HeaderVH(v);
        }
        View v = inflater.inflate(R.layout.score_summary_item, parent, false);
        return new RowVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object obj = items.get(position);
        if (holder instanceof HeaderVH) {
            HeaderItem h = (HeaderItem) obj;
            HeaderVH vh = (HeaderVH) holder;
            vh.tvSemesterName.setText(h.semesterName);
            vh.tvSemesterGpa.setText(h.semesterGpaText);
        } else if (holder instanceof RowVH) {
            ScoreDao.ScoreWithSemester row = (ScoreDao.ScoreWithSemester) obj;
            RowVH vh = (RowVH) holder;
            vh.tvCode.setText(row.maHp);
            vh.tvName.setText(row.tenHp);
            vh.tvCredits.setText(String.valueOf(row.credits));
            vh.tvGpa.setText(row.gpa != null ? String.format(Locale.US, "%.1f", row.gpa) : "-");
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class HeaderVH extends RecyclerView.ViewHolder {
        final TextView tvSemesterName, tvSemesterGpa;

        HeaderVH(@NonNull View itemView) {
            super(itemView);
            tvSemesterName = itemView.findViewById(R.id.tvSemesterName);
            tvSemesterGpa = itemView.findViewById(R.id.tvSemesterGpa);
        }
    }

    static class RowVH extends RecyclerView.ViewHolder {
        final TextView tvCode, tvName, tvCredits, tvGpa;

        RowVH(@NonNull View itemView) {
            super(itemView);
            tvCode = itemView.findViewById(R.id.tvCode);
            tvName = itemView.findViewById(R.id.tvName);
            tvCredits = itemView.findViewById(R.id.tvCredits);
            tvGpa = itemView.findViewById(R.id.tvGpa);
        }
    }
}
