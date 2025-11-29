package com.example.study_app.ui.Deadline.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.study_app.R;
import com.example.study_app.data.DatabaseHelper;
import com.example.study_app.ui.Deadline.Models.Deadline;
import com.example.study_app.ui.Subject.Model.Subject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AdapterDeadline extends RecyclerView.Adapter<AdapterDeadline.ViewHolder> {

    private Context context;
    private List<Deadline> deadlines;
    private AdapterWeek.OnDeadlineInteractionListener deadlineListener;

    public AdapterDeadline(Context context, ArrayList<Deadline> deadlines) {
        this.context = context;
        this.deadlines = deadlines;
    }

    public void setOnDeadlineInteractionListener(AdapterWeek.OnDeadlineInteractionListener listener) {
        this.deadlineListener = listener;
    }

    public void updateData(List<Deadline> newDeadlines) {
        this.deadlines.clear();
        this.deadlines.addAll(newDeadlines);
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.deadline_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Deadline deadline = deadlines.get(position);
        if (deadline == null) return;

//         Lấy tên môn
//        Subject s = new DatabaseHelper(context).getSubjectByMaHp(deadline.getMaHp());
        String displayTitle = deadline.getTieuDe();
//        if (s != null && s.tenHp != null) {
//            displayTitle += " (" + s.tenHp + ")";
//        }

        holder.tieuDe.setText(displayTitle); // ✅ phải set displayTitle, không phải deadline.getTieuDe()

        holder.icon.setImageResource(deadline.getIcon());

        if (!deadline.isCompleted() && deadline.getNgayKetThuc() != null) {
            long diff = deadline.getNgayKetThuc().getTime() - new Date().getTime();
            holder.tieuDe.setTextColor(diff <= 3600_000 ? Color.RED : Color.BLACK);
        } else {
            holder.tieuDe.setTextColor(Color.BLACK);
        }

        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(deadline.isCompleted());

        if (deadline.isCompleted()) {
            holder.thoiGian.setText("Đã hoàn thành");
        } else {
            holder.thoiGian.setText(deadline.getConLai());
        }

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (deadlineListener != null) {
                deadlineListener.onStateChanged(deadline, isChecked);
            }
            notifyItemChanged(holder.getAdapterPosition());
        });

        holder.itemView.setOnClickListener(v -> {
            if (deadlineListener != null) {
                deadlineListener.onDeadlineClick(deadline);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (deadlineListener != null) {
                showOptionsDialog(deadline, holder.getAdapterPosition());
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return deadlines != null ? deadlines.size() : 0;
    }

    private void showOptionsDialog(final Deadline deadline, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setItems(new CharSequence[]{"Sửa", "Xóa"}, (dialog, which) -> {
            if (deadlineListener == null) return;
            switch (which) {
                case 0: // Edit
                    deadlineListener.onEditDeadline(deadline);
                    break;
                case 1: // Delete
                    new AlertDialog.Builder(context)
                            .setTitle("Xác nhận xóa")
                            .setMessage("Bạn có chắc muốn xóa deadline này?")
                            .setPositiveButton("Xóa", (d, w) -> {
                                if (deadlineListener != null) {
                                    deadlineListener.onDeleteDeadline(deadline);
                                    deadlines.remove(position);
                                    notifyItemRemoved(position);
                                    notifyItemRangeChanged(position, deadlines.size());
                                }
                            })
                            .setNegativeButton("Hủy", null)
                            .show();
                    break;
            }
        });
        builder.create().show();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView tieuDe;
        TextView thoiGian;
        CheckBox checkBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.ivAnh);
            tieuDe = itemView.findViewById(R.id.tvTieuDe);
            thoiGian = itemView.findViewById(R.id.tvKetQua);
            checkBox = itemView.findViewById(R.id.cbXacNhan);
        }
    }
}
