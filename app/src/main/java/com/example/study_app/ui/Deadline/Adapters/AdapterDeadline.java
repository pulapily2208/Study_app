package com.example.study_app.ui.Deadline.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.study_app.R;
import com.example.study_app.ui.Deadline.Models.Deadline;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class AdapterDeadline extends ArrayAdapter<Deadline> {

    private Context context;
    private int resource;
    private ArrayList<Deadline> deadlines;
    private AdapterWeek.OnDeadlineInteractionListener deadlineListener;

    public AdapterDeadline(Context context, int resource, ArrayList<Deadline> deadlines) {
        super(context, resource, deadlines);
        this.context = context;
        this.resource = resource;
        this.deadlines = deadlines;
    }

    public void setOnDeadlineInteractionListener(AdapterWeek.OnDeadlineInteractionListener listener) {
        this.deadlineListener = listener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(resource, parent, false);
            holder = new ViewHolder();
            holder.icon = convertView.findViewById(R.id.ivAnh);
            holder.tieuDe = convertView.findViewById(R.id.tvTieuDe);
            holder.thoiGian = convertView.findViewById(R.id.tvKetQua);
            holder.checkBox = convertView.findViewById(R.id.cbXacNhan); // Thêm CheckBox vào ViewHolder
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Deadline deadline = deadlines.get(position);

        holder.icon.setImageResource(deadline.getIcon());
        holder.tieuDe.setText(deadline.getTieuDe());

        // Logic hiển thị trạng thái dựa trên CheckBox và ngày
        if (deadline.isCompleted()) {
            holder.checkBox.setChecked(true);
            Date now = new Date();
            long diff = now.getTime() - deadline.getNgayKetThuc().getTime();

            if (diff > 0) { // Hoàn thành trễ
                long daysLate = TimeUnit.MILLISECONDS.toDays(diff);
                holder.thoiGian.setText("Hoàn thành trễ " + daysLate + " ngày");
            } else { // Hoàn thành đúng hạn
                holder.thoiGian.setText("Đã hoàn thành");
            }
        } else {
            holder.checkBox.setChecked(false);
            holder.thoiGian.setText(deadline.getConLai());
        }

        // Loại bỏ listener cũ để tránh gọi lại nhiều lần khi view được tái sử dụng
        holder.checkBox.setOnCheckedChangeListener(null);

        // Thiết lập listener mới
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (deadlineListener != null) {
                deadlineListener.onStateChanged(deadline, isChecked);
            }
            // Cập nhật lại UI ngay lập tức để người dùng thấy thay đổi
            if (isChecked) {
                Date now = new Date();
                long diff = now.getTime() - deadline.getNgayKetThuc().getTime();
                if (diff > 0) {
                    long daysLate = TimeUnit.MILLISECONDS.toDays(diff);
                    holder.thoiGian.setText("Hoàn thành trễ " + daysLate + " ngày");
                } else {
                    holder.thoiGian.setText("Đã hoàn thành");
                }
            } else {
                holder.thoiGian.setText(deadline.getConLai());
            }
        });

        convertView.setOnClickListener(v -> {
            if (deadlineListener != null) {
                deadlineListener.onDeadlineClick(deadline);
            }
        });

        convertView.setOnLongClickListener(v -> {
            if (deadlineListener != null) {
                showOptionsDialog(deadline);
                return true;
            }
            return false;
        });

        return convertView;
    }

    private void showOptionsDialog(final Deadline deadline) {
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
                            .setPositiveButton("Xóa", (d, w) -> deadlineListener.onDeleteDeadline(deadline))
                            .setNegativeButton("Hủy", null)
                            .show();
                    break;
            }
        });
        builder.create().show();
    }

    private static class ViewHolder {
        ImageView icon;
        TextView tieuDe;
        TextView thoiGian;
        CheckBox checkBox; // Thêm CheckBox
    }
}
