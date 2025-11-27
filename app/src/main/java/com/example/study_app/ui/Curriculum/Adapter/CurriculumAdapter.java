package com.example.study_app.ui.Curriculum.Adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.study_app.R;
import com.example.study_app.ui.Curriculum.Model.Curriculum;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CurriculumAdapter extends RecyclerView.Adapter<CurriculumAdapter.CurriculumViewHolder> {

    // Danh sách đang hiển thị trên màn hình
    private final List<Curriculum> courseListDisplayed;
    // Bản sao của danh sách gốc, không bao giờ thay đổi, chỉ dùng để lọc
    private final List<Curriculum> courseListFull;

    public CurriculumAdapter(List<Curriculum> courseList) {
        this.courseListFull = new ArrayList<>(courseList);
        this.courseListDisplayed = new ArrayList<>(courseList);
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

        // (NEW) Badge trạng thái
        String status = currentCourse.getStatus();
        if (status == null) {
            holder.tvStatusBadge.setVisibility(View.GONE);
        } else {
            holder.tvStatusBadge.setVisibility(View.VISIBLE);
            switch (status) {
                case com.example.study_app.data.DatabaseHelper.STATUS_IN_PROGRESS:
                    holder.tvStatusBadge.setText("Đang học");
                    holder.tvStatusBadge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFFFD54F)); // vàng
                    holder.tvStatusBadge.setTextColor(0xFF000000);
                    break;
                case com.example.study_app.data.DatabaseHelper.STATUS_COMPLETED:
                    holder.tvStatusBadge.setText("Đã học");
                    holder.tvStatusBadge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF4CAF50)); // xanh lá
                    holder.tvStatusBadge.setTextColor(0xFFFFFFFF);
                    break;
                case com.example.study_app.data.DatabaseHelper.STATUS_NOT_ENROLLED:
                default:
                    holder.tvStatusBadge.setText("Chưa học");
                    holder.tvStatusBadge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFE53935)); // đỏ
                    holder.tvStatusBadge.setTextColor(0xFFFFFFFF);
                    break;
            }
        }
    }
    @Override
    public int getItemCount() {
        return courseListDisplayed.size();
    }

    /**
     * Phương thức trung tâm để lọc và sắp xếp danh sách môn học.
     * @param query Từ khóa tìm kiếm (tên hoặc mã HP).
     * @param facultyId ID của khoa (-1 nếu là 'Tất cả').
     * @param group Tên nhóm tự chọn ("All" nếu là 'Tất cả').
     * @param courseType Loại học phần ("Tất cả" nếu là 'Tất cả').
     * @param isAscending True để sắp xếp A-Z, false để sắp xếp Z-A.
     */
    public void filterAndSort(String query, int facultyId, String group, String courseType, boolean isAscending) {
        List<Curriculum> filteredList = new ArrayList<>();
        String allGroups = "All";
        String allTypes = "Tất cả";

        // BƯỚC 1: LỌC
        for (Curriculum item : courseListFull) {
            // Lọc theo Khoa
            final boolean facultyMatch = (facultyId == -1) || (item.getKhoaId() == facultyId);

            // Lọc theo Nhóm
            final boolean groupMatch = allGroups.equals(group) || (item.getNhomTuChon() != null && item.getNhomTuChon().equals(group));

            // Lọc theo Loại môn học
            final boolean courseTypeMatch = allTypes.equals(courseType) || (item.getLoaiHp() != null && item.getLoaiHp().equalsIgnoreCase(courseType));

            // Lọc theo ô tìm kiếm (query) - kiểm tra cả tên và mã học phần
            final boolean searchMatch = query == null || query.isEmpty() ||
                    (item.getTenHp() != null && item.getTenHp().toLowerCase().contains(query.toLowerCase())) ||
                    (item.getMaHp() != null && item.getMaHp().toLowerCase().contains(query.toLowerCase()));

            if (facultyMatch && groupMatch && courseTypeMatch && searchMatch) {
                filteredList.add(item);
            }
        }

        // BƯỚC 2: SẮP XẾP
        if (isAscending) {
            // Sắp xếp A-Z theo Tên học phần
            Collections.sort(filteredList, (o1, o2) -> o1.getTenHp().compareToIgnoreCase(o2.getTenHp()));
        } else {
            // Sắp xếp Z-A theo Tên học phần
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
        // Không cần gọi notifyDataSetChanged() ở đây vì Activity sẽ gọi filterAndSort() ngay sau đó
    }

    static class CurriculumViewHolder extends RecyclerView.ViewHolder {
        final TextView tvMaHp, tvTenHp, tvSoTinChi, tvSoTietLyThuyet, tvSoTietThucHanh, tvHocKy, tvLoaiHp, tvNhomTuChon;
        final TextView tvStatusBadge; // NEW

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
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge); // NEW
        }
    }
}