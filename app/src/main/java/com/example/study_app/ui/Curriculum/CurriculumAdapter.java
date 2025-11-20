package com.example.study_app.ui.Curriculum;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.study_app.R;

import java.util.ArrayList;
import java.util.List;

public class CurriculumAdapter extends RecyclerView.Adapter<CurriculumAdapter.CurriculumViewHolder> {

    private List<Curriculum> courseList;
    private final List<Curriculum> courseListFull; // A copy of the original list to use for filtering

    public CurriculumAdapter(List<Curriculum> courseList) {
        this.courseList = courseList;
        this.courseListFull = new ArrayList<>(courseList);
    }

    @NonNull
    @Override
    public CurriculumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.curriculum_item, parent, false);
        return new CurriculumViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CurriculumViewHolder holder, int position) {
        Curriculum currentCourse = courseList.get(position);
        Context context = holder.itemView.getContext();

        holder.tvMaHp.setText(currentCourse.getMaHp());
        holder.tvTenHp.setText(currentCourse.getTenHp());
        holder.tvSoTinChi.setText(context.getString(R.string.curriculum_credits, currentCourse.getSoTinChi()));
        holder.tvSoTietLyThuyet.setText(context.getString(R.string.curriculum_theory, currentCourse.getSoTietLyThuyet()));
        holder.tvSoTietThucHanh.setText(context.getString(R.string.curriculum_practice, currentCourse.getSoTietThucHanh()));
        holder.tvLoaiHp.setText(currentCourse.getLoaiHp());

        if (currentCourse.getHocKy() > 0) {
            holder.tvHocKy.setText(context.getString(R.string.curriculum_semester, currentCourse.getHocKy()));
            holder.tvHocKy.setVisibility(View.VISIBLE);
        } else {
            holder.tvHocKy.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(currentCourse.getNhomTuChon())) {
            holder.tvNhomTuChon.setText(context.getString(R.string.curriculum_group, currentCourse.getNhomTuChon()));
            holder.tvNhomTuChon.setVisibility(View.VISIBLE);
        } else {
            holder.tvNhomTuChon.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }

    public void filter(int facultyId, String group, String courseType) {
        List<Curriculum> filteredList = new ArrayList<>();
        String allGroups = "All"; // Consider making this a resource string
        String allTypes = "Tất cả"; // Consider making this a resource string

        for (Curriculum item : courseListFull) {
            // Faculty filter (facultyId = -1 means 'All')
            boolean facultyMatch = (facultyId == -1) || (item.getKhoaId() == facultyId);

            // Group filter
            boolean groupMatch = allGroups.equals(group) || (item.getNhomTuChon() != null && item.getNhomTuChon().equals(group));

            // Course Type filter
            boolean courseTypeMatch = allTypes.equals(courseType) || (item.getLoaiHp() != null && item.getLoaiHp().equalsIgnoreCase(courseType));

            if (facultyMatch && groupMatch && courseTypeMatch) {
                filteredList.add(item);
            }
        }
        courseList = filteredList;
        notifyDataSetChanged();
    }

    public void setCourses(List<Curriculum> courses) {
        this.courseList = new ArrayList<>(courses);
        this.courseListFull.clear();
        this.courseListFull.addAll(courses);
        notifyDataSetChanged();
    }

    static class CurriculumViewHolder extends RecyclerView.ViewHolder {
        final TextView tvMaHp;
        final TextView tvTenHp;
        final TextView tvSoTinChi;
        final TextView tvSoTietLyThuyet;
        final TextView tvSoTietThucHanh;
        final TextView tvHocKy;
        final TextView tvLoaiHp;
        final TextView tvNhomTuChon;

        public CurriculumViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMaHp = itemView.findViewById(R.id.tvMaHp);
            tvTenHp = itemView.findViewById(R.id.tvTenHp);
            tvSoTinChi = itemView.findViewById(R.id.tvSoTinChi);
            tvSoTietLyThuyet = itemView.findViewById(R.id.tvSoTietLyThuyet);
            tvSoTietThucHanh = itemView.findViewById(R.id.tvSoTietThucHanh);
            tvHocKy = itemView.findViewById(R.id.tvHocKy);
            tvLoaiHp = itemView.findViewById(R.id.tvLoaiHp);
            tvNhomTuChon = itemView.findViewById(R.id.tvNhomTuChon);
        }
    }
}
