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
    private void sortDeadlines() {
        // Ghim lên đầu
        deadlines.sort((d1, d2) -> {
            if (d1.isPinned() && !d2.isPinned()) return -1;
            if (!d1.isPinned() && d2.isPinned()) return 1;

            // Nếu cùng trạng thái ghim thì sắp xếp theo ngày kết thúc
            if (d1.getNgayKetThuc() != null && d2.getNgayKetThuc() != null) {
                return d1.getNgayKetThuc().compareTo(d2.getNgayKetThuc());
            }
            return 0;
        });
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

        String displayTitle = deadline.getTieuDe();

        holder.tieuDe.setText(displayTitle); // ✅ phải set displayTitle, không phải deadline.getTieuDe()

        holder.icon.setImageResource(deadline.getIcon());


        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(deadline.isCompleted());

        // Disable checkbox nếu đã hết hạn
        boolean isExpired = deadline.getNgayKetThuc() != null &&
                deadline.getNgayKetThuc().before(new Date());
        boolean isCompleted = deadline.isCompleted();

        // ❗ Nếu đã hoàn thành HOẶC đã quá hạn → khóa checkbox
        holder.checkBox.setEnabled(!(isCompleted || isExpired));

        long now = System.currentTimeMillis();
        long deadlineTime = deadline.getNgayKetThuc().getTime();

        int pos = holder.getAdapterPosition();
        if (pos == RecyclerView.NO_POSITION) return;
        Deadline d=deadlines.get(pos);

        if (!deadline.isCompleted()) {

            if (deadlineTime < now) {
                d.setNote("Quá hạn");
                holder.thoiGian.setText(d.getNote());
            } else {
                d.setNote("Chưa hoàn thành");
                holder.thoiGian.setText(deadline.getConLai());
            }
        } else {
            d.setNote("Đã hoàn thành");
            holder.thoiGian.setText(d.getNote());
        }


        if (d.isPinned()) {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.beige));
        } else {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
        }

        // Xử lý khi checkbox thay đổi
        // Nếu chưa hoàn thành → cho phép tick
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {

            d.setCompleted(isChecked);

            // cập nhật DB
            if (dbHelper != null) {
                dbHelper.updateDeadline(d);
            }

            // Sau khi tick → disable checkbox
            notifyItemChanged(pos);
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
        builder.setItems(new CharSequence[]{"Sửa", "Xóa", "Ghim lên đầu"}, (dialog, which) -> {
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
                case 2: // Ghim lên đầu
                    deadlines.remove(position);          // bỏ khỏi vị trí cũ
                    deadline.setPinned(true);
                    deadlines.add(0, deadline);          // thêm vào đầu danh sách
                    notifyItemMoved(position, 0);        // thông báo RecyclerView
                    notifyItemChanged(0);                // refresh item đầu
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
