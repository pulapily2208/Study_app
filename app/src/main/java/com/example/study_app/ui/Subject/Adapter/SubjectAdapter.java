package com.example.study_app.ui.Subject.Adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.study_app.R;
import com.example.study_app.ui.Subject.Model.Subject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.SubjectViewHolder> {

    private List<Subject> subjectList;
    private final OnSubjectActionClickListener actionListener;

    public interface OnSubjectActionClickListener {
        void onEdit(Subject subject);
        void onDelete(Subject subject);
        void onViewDeadlines(Subject subject);
    }

    public SubjectAdapter(List<Subject> subjectList, OnSubjectActionClickListener listener) {
        this.subjectList = subjectList;
        this.actionListener = listener;
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

        // Set subject name and description
        holder.tvSubjectName.setText(subject.tenHp != null ? subject.tenHp : "Chưa có tên môn học");
        holder.tvSubjectDesc.setText(subject.maHp != null ? "Mã HP: " + subject.maHp : "");

        // Handle Time display
        holder.timeLayout.setVisibility(View.VISIBLE); // Always show the time layout
        boolean hasTime = subject.gioBatDau != null && subject.gioKetThuc != null;
        if (hasTime) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            String startTime = sdf.format(subject.gioBatDau);
            String endTime = sdf.format(subject.gioKetThuc);
            holder.tvTime.setText(startTime + " - " + endTime);
        } else {
            holder.tvTime.setText("Chưa có giờ học");
        }

        // Handle Location display
        holder.locationLayout.setVisibility(View.VISIBLE); // Always show the location layout
        boolean hasLocation = subject.phongHoc != null && !subject.phongHoc.isEmpty();
        if (hasLocation) {
            holder.tvLocation.setText("Phòng: " + subject.phongHoc);
        } else {
            holder.tvLocation.setText("Chưa có phòng học");
        }

        // Handle Color Bar
        if (subject.mauSac != null && !subject.mauSac.isEmpty()) {
            try {
                holder.colorBar.setBackgroundColor(Color.parseColor(subject.mauSac));
            } catch (IllegalArgumentException e) {
                holder.colorBar.setBackgroundColor(Color.LTGRAY); // Default color on error
            }
        } else {
            holder.colorBar.setBackgroundColor(Color.LTGRAY); // Default color if not set
        }

        // Set listener for the options menu
        holder.ivOptionsMenu.setOnClickListener(v -> showPopupMenu(v, subject));
    }

    private void showPopupMenu(View view, Subject subject) {
        PopupMenu popup = new PopupMenu(view.getContext(), view);
        popup.getMenuInflater().inflate(R.menu.subject_options_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            if (actionListener == null) return false;

            int itemId = item.getItemId();
            if (itemId == R.id.action_edit_subject) {
                actionListener.onEdit(subject);
                return true;
            } else if (itemId == R.id.action_delete_subject) {
                actionListener.onDelete(subject);
                return true;
            } else if (itemId == R.id.action_view_deadlines) {
                actionListener.onViewDeadlines(subject);
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
        private final View timeLayout;
        private final View locationLayout;

        public SubjectViewHolder(@NonNull View itemView) {
            super(itemView);
            colorBar = itemView.findViewById(R.id.color_bar);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvSubjectName = itemView.findViewById(R.id.tvSubjectName);
            tvSubjectDesc = itemView.findViewById(R.id.tvSubjectDesc);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            ivOptionsMenu = itemView.findViewById(R.id.iv_options_menu);
            timeLayout = itemView.findViewById(R.id.time_layout); // Find the layout
            locationLayout = itemView.findViewById(R.id.location_layout); // Find the layout
        }
    }
}
