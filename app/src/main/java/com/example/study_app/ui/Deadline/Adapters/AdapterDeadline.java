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
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.study_app.R;
import com.example.study_app.data.DatabaseHelper;
import com.example.study_app.data.DeadlineDao;
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
    private DeadlineDao dbHelper;

    public AdapterDeadline(Context context, ArrayList<Deadline> deadlines) {
        this.context = context;
        this.deadlines = deadlines;
        this.dbHelper = new DeadlineDao(context);
    }

    public void setOnDeadlineInteractionListener(AdapterWeek.OnDeadlineInteractionListener listener) {
        this.deadlineListener = listener;
    }

    public void updateData(List<Deadline> newDeadlines) {
        this.deadlines.clear();
        this.deadlines.addAll(newDeadlines);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return deadlines != null ? deadlines.size() : 0;
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

        holder.tieuDe.setText(deadline.getTieuDe());
        holder.icon.setImageResource(deadline.getIcon());

        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(deadline.isCompleted());


        boolean isCompleted = deadline.isCompleted();
        boolean isExpired = false;

        if (deadline.getNgayKetThuc() != null) {
            long now = System.currentTimeMillis();
            long ddl = deadline.getNgayKetThuc().getTime();
            isExpired = ddl < now;
        }

        holder.checkBox.setEnabled(!(isCompleted || isExpired));


        if (isCompleted) {
            holder.thoiGian.setText("Đã hoàn thành");
        } else if (isExpired) {
            holder.thoiGian.setText("Quá hạn");
        } else {
            holder.thoiGian.setText(deadline.getConLai());
        }


        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            deadline.setCompleted(isChecked);

            if (dbHelper != null) {
                dbHelper.updateDeadline(deadline);
            }

            notifyItemChanged(position);
        });

        // --- Click item ---
        holder.itemView.setOnClickListener(v -> {
            if (deadlineListener != null) {
                deadlineListener.onDeadlineClick(deadline);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (deadlineListener != null) {
                showOptionsDialog(deadline, position);
                return true;
            }
            return false;
        });
    }

    private void showOptionsDialog(final Deadline deadline, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setItems(new CharSequence[]{"Sửa", "Xóa" }, (dialog, which) -> {
            if (deadlineListener == null) return;
            switch (which) {
                case 0:
                    deadlineListener.onEditDeadline(deadline);
                    break;
                case 1:
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
