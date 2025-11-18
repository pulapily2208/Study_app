package com.example.study_app.ui.Subject.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.study_app.R;
import com.example.study_app.ui.Deadline.MainDeadLine;
import com.example.study_app.ui.Subject.Model.Subject;

import java.util.List;

public class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.SubjectViewHolder> {

    private List<Subject> subjectList;
    private final Context context;

    public SubjectAdapter(Context context, List<Subject> subjectList) {
        this.context = context;
        this.subjectList = subjectList;
    }

    @NonNull
    @Override
    public SubjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.subject_item, parent, false);
        return new SubjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectViewHolder holder, int position) {
        Subject subject = subjectList.get(position);

        // --- Start of Safe Data Binding ---

        // 1. Set Subject Name and Code (already safe)
        holder.tvSubjectName.setText(subject.tenHp != null ? subject.tenHp : "Chưa có tên môn học");
        holder.tvSubjectDesc.setText(subject.maHp != null ? subject.maHp : "Chưa có mã môn học");

        // 2. Set Time (Safely)
        String timeText;
        if (subject.gioBatDau != null && !subject.gioBatDau.isEmpty() && subject.gioKetThuc != null && !subject.gioKetThuc.isEmpty()) {
            timeText = subject.gioBatDau + " - " + subject.gioKetThuc;
        } else {
            timeText = "Chưa có giờ học";
        }
        holder.tvTime.setText(timeText);

        // 3. Set Location (Safely)
        String locationText;
        if (subject.phongHoc != null && !subject.phongHoc.isEmpty()) {
            locationText = "Phòng: " + subject.phongHoc;
        } else {
            locationText = "Chưa có phòng học";
        }
        holder.tvLocation.setText(locationText);

        // --- End of Safe Data Binding ---


        holder.ivOptionsMenu.setOnClickListener(v -> showPopupMenu(v, subject));
    }

    private void showPopupMenu(View view, Subject subject) {
        PopupMenu popup = new PopupMenu(context, view);
        popup.getMenuInflater().inflate(R.menu.subject_options_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_edit_subject) {
                // Use the safe getter for the name
                Toast.makeText(context, "Sửa: " + (subject.tenHp != null ? subject.tenHp : "N/A"), Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.action_delete_subject) {
                // Use the safe getter for the name
                Toast.makeText(context, "Xóa: " + (subject.tenHp != null ? subject.tenHp : "N/A"), Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.action_view_deadlines) {
                Intent intent = new Intent(context, MainDeadLine.class);
                // Pass the String ma_hp instead of the integer id
                intent.putExtra("SUBJECT_MA_HP", subject.maHp);
                intent.putExtra("SUBJECT_TEN_HP", subject.tenHp);
                context.startActivity(intent);
                return true;
            }
            return false;
        });
        popup.show();
    }

    @Override
    public int getItemCount() {
        return subjectList != null ? subjectList.size() : 0;
    }

    public void setSubjects(List<Subject> subjects) {
        this.subjectList = subjects;
        notifyDataSetChanged();
    }

    public static class SubjectViewHolder extends RecyclerView.ViewHolder {
        private final View colorBar;
        private final TextView tvTime;
        private final TextView tvSubjectName;
        private final TextView tvSubjectDesc;
        private final TextView tvLocation;
        private final ImageView ivOptionsMenu;

        public SubjectViewHolder(@NonNull View itemView) {
            super(itemView);
            colorBar = itemView.findViewById(R.id.color_bar);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvSubjectName = itemView.findViewById(R.id.tvSubjectName);
            tvSubjectDesc = itemView.findViewById(R.id.tvSubjectDesc);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            ivOptionsMenu = itemView.findViewById(R.id.iv_options_menu);
        }
    }
}
