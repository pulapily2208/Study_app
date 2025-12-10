package com.example.study_app.ui.Curriculum.Adapter;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.study_app.R;
import com.example.study_app.data.DatabaseHelper;
import com.example.study_app.data.ScoreDao;
import com.example.study_app.ui.Curriculum.Model.Curriculum;
import com.example.study_app.ui.Score.InputScoreActivity;
import com.example.study_app.ui.Subject.SubjectAdviceProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CurriculumAdapter extends RecyclerView.Adapter<CurriculumAdapter.CurriculumViewHolder> {

    private final List<Curriculum> courseListDisplayed;
    private final List<Curriculum> courseListFull;
    private final ScoreDao scoreDao;
    private Map<String, Float> gpaMap;

    public CurriculumAdapter(Context context, List<Curriculum> courseList, ScoreDao scoreDao) {
        this.courseListFull = new ArrayList<>(courseList);
        this.courseListDisplayed = new ArrayList<>(courseList);
        this.scoreDao = scoreDao;
        try {
            this.gpaMap = scoreDao.getAllGpaMap();
        } catch (Exception e) {
            this.gpaMap = java.util.Collections.emptyMap();
        }
    }

    @NonNull
    @Override
    public CurriculumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.curriculum_item, parent, false);
        return new CurriculumViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CurriculumViewHolder holder, int position) {
        Curriculum currentCourse = courseListDisplayed.get(position);
        Context context = holder.itemView.getContext();

        holder.tvMaHp.setText(currentCourse.getMaHp());
        holder.tvTenHp.setText(currentCourse.getTenHp());
        holder.tvSoTinChi.setText(context.getString(R.string.curriculum_credits, currentCourse.getSoTinChi()));
        holder.tvSoTietLyThuyet
                .setText(context.getString(R.string.curriculum_theory, currentCourse.getSoTietLyThuyet()));
        holder.tvSoTietThucHanh
                .setText(context.getString(R.string.curriculum_practice, currentCourse.getSoTietThucHanh()));
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

        // Badge trạng thái
        String status = currentCourse.getStatus();
        if (status == null) {
            holder.tvStatusBadge.setVisibility(View.GONE);
        } else {
            holder.tvStatusBadge.setVisibility(View.VISIBLE);
            switch (status) {
                case DatabaseHelper.STATUS_IN_PROGRESS:
                    holder.tvStatusBadge.setText("Đang học");
                    holder.tvStatusBadge.setBackgroundTintList(
                            android.content.res.ColorStateList.valueOf(context.getColor(R.color.yellow)));
                    holder.tvStatusBadge.setTextColor(context.getColor(R.color.white));
                    break;
                case DatabaseHelper.STATUS_COMPLETED:
                    holder.tvStatusBadge.setText("Đã học");
                    holder.tvStatusBadge.setBackgroundTintList(
                            android.content.res.ColorStateList.valueOf(context.getColor(R.color.green)));
                    holder.tvStatusBadge.setTextColor(context.getColor(R.color.white));
                    break;
                case DatabaseHelper.STATUS_NOT_ENROLLED:
                default:
                    holder.tvStatusBadge.setText("Chưa học");
                    holder.tvStatusBadge.setBackgroundTintList(
                            android.content.res.ColorStateList.valueOf(context.getColor(R.color.red)));
                    holder.tvStatusBadge.setTextColor(context.getColor(R.color.white));
                    break;
            }
        }

        // (NEW) Hiển thị điểm GPA nếu có (dùng cache, tránh query DB trên UI thread)
        Float gpa = gpaMap != null ? gpaMap.get(currentCourse.getMaHp()) : null;
        if (gpa != null) {
            holder.tvGPA.setText(String.format(Locale.US, "%.1f", gpa));
            holder.tvGPA.setVisibility(View.VISIBLE);
        } else {
            holder.tvGPA.setVisibility(View.GONE);
        }

        // Khi bấm vào 1 môn → mở InputScoreActivity
        if (DatabaseHelper.STATUS_COMPLETED.equals(status) || DatabaseHelper.STATUS_IN_PROGRESS.equals(status)) {
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, InputScoreActivity.class);
                intent.putExtra("subject_code", currentCourse.getMaHp());
                intent.putExtra("subject_name", currentCourse.getTenHp());
                context.startActivity(intent);
            });
        } else {
            // Môn chưa học: gọi AI tư vấn thay vì nhập điểm
            holder.itemView.setOnClickListener(v -> {
                String subjectName = currentCourse.getTenHp();
                // Hiển thị dialog loading đơn giản
                AlertDialog loading = new AlertDialog.Builder(context)
                        .setTitle("Đang tư vấn AI")
                        .setMessage("Vui lòng đợi trong giây lát...")
                        .setCancelable(false)
                        .create();
                loading.show();

                SubjectAdviceProvider.getAdviceFromGemini(subjectName, new SubjectAdviceProvider.AdviceCallback() {
                    @Override
                    public void onSuccess(String advice) {
                        loading.dismiss();
                        new AlertDialog.Builder(context)
                                .setTitle("Tư vấn cho môn: " + subjectName)
                                .setMessage(advice)
                                .setPositiveButton("Đóng", (d, w) -> d.dismiss())
                                .show();
                    }

                    @Override
                    public void onError(Exception e) {
                        loading.dismiss();
                        new AlertDialog.Builder(context)
                                .setTitle("Không thể kết nối AI")
                                .setMessage(e.getMessage() != null ? e.getMessage() : "Đã xảy ra lỗi không xác định")
                                .setPositiveButton("Đóng", (d, w) -> d.dismiss())
                                .show();
                    }
                });
            });
        }
    }

    @Override
    public int getItemCount() {
        return courseListDisplayed.size();
    }

    /**
     * Phương thức trung tâm để lọc và sắp xếp danh sách môn học.
     * 
     * @param query       Từ khóa tìm kiếm (tên hoặc mã HP).
     * @param facultyId   ID của khoa (-1 nếu là 'Tất cả').
     * @param group       Tên nhóm tự chọn ("All" nếu là 'Tất cả').
     * @param courseType  Loại học phần ("Tất cả" nếu là 'Tất cả').
     * @param status      Trạng thái môn học ("All" nếu không lọc).
     * @param isAscending True để sắp xếp A-Z, false để sắp xếp Z-A.
     */
    public void filterAndSort(String query, int facultyId, String group, String courseType, String status,
            boolean isAscending) {
        List<Curriculum> filteredList = new ArrayList<>();
        String allGroups = "All";
        String allTypes = "Tất cả";
        String allStatus = "All";

        // BƯỚC 1: LỌC
        for (Curriculum item : courseListFull) {
            final boolean facultyMatch = (facultyId == -1) || (item.getKhoaId() == facultyId);

            final boolean groupMatch = allGroups.equals(group)
                    || (item.getNhomTuChon() != null && item.getNhomTuChon().equals(group));

            final boolean courseTypeMatch = allTypes.equals(courseType)
                    || (item.getLoaiHp() != null && item.getLoaiHp().equalsIgnoreCase(courseType));

            String itemStatus = item.getStatus() == null ? DatabaseHelper.STATUS_NOT_ENROLLED : item.getStatus();
            final boolean statusMatch = allStatus.equals(status) || itemStatus.equals(status);

            final boolean searchMatch = query == null || query.isEmpty() ||
                    (item.getTenHp() != null && item.getTenHp().toLowerCase().contains(query.toLowerCase())) ||
                    (item.getMaHp() != null && item.getMaHp().toLowerCase().contains(query.toLowerCase()));

            if (facultyMatch && groupMatch && courseTypeMatch && statusMatch && searchMatch) {
                filteredList.add(item);
            }
        }

        // BƯỚC 2: SẮP XẾP
        if (isAscending) {
            Collections.sort(filteredList, (o1, o2) -> o1.getTenHp().compareToIgnoreCase(o2.getTenHp()));
        } else {
            Collections.sort(filteredList, (o1, o2) -> o2.getTenHp().compareToIgnoreCase(o1.getTenHp()));
        }

        // BƯỚC 3: CẬP NHẬT UI
        courseListDisplayed.clear();
        courseListDisplayed.addAll(filteredList);
        notifyDataSetChanged();
    }

    /**
     * Cập nhật danh sách môn học gốc khi có dữ liệu mới từ database.
     */
    public void setCourses(List<Curriculum> courses) {
        this.courseListFull.clear();
        this.courseListFull.addAll(courses);
        try {
            this.gpaMap = scoreDao.getAllGpaMap();
        } catch (Exception ignored) {
        }
    }

    static class CurriculumViewHolder extends RecyclerView.ViewHolder {
        final TextView tvMaHp, tvTenHp, tvSoTinChi, tvSoTietLyThuyet, tvSoTietThucHanh, tvHocKy, tvLoaiHp, tvNhomTuChon;
        final TextView tvStatusBadge;
        final TextView tvGPA;

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
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
            tvGPA = itemView.findViewById(R.id.tvGPA); // NEW
        }
    }
}
