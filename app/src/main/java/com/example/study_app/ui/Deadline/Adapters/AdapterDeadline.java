package com.example.study_app.ui.Deadline.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.study_app.R;
import com.example.study_app.ui.Deadline.Models.Deadline;

import java.util.ArrayList;

public class AdapterDeadline extends ArrayAdapter<Deadline> {

    private Context context;
    private int resource;
    private ArrayList<Deadline> deadlines;
    // Thay đổi ở đây: Thêm listener
    private AdapterWeek.OnDeadlineInteractionListener deadlineListener;

    public AdapterDeadline(Context context, int resource, ArrayList<Deadline> deadlines) {
        super(context, resource, deadlines);
        this.context = context;
        this.resource = resource;
        this.deadlines = deadlines;
    }

    // Thêm phương thức này
    public void setOnDeadlineInteractionListener(AdapterWeek.OnDeadlineInteractionListener listener) {
        this.deadlineListener = listener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(resource, parent, false);
            holder = new ViewHolder();
            // Sửa lại ID cho đúng với deadline_item.xml
            holder.icon = convertView.findViewById(R.id.ivAnh);
            holder.tieuDe = convertView.findViewById(R.id.tvTieuDe);
            holder.thoiGian = convertView.findViewById(R.id.tvKetQua);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Deadline deadline = deadlines.get(position);

        holder.icon.setImageResource(deadline.getIcon());
        holder.tieuDe.setText(deadline.getTieuDe());
        holder.thoiGian.setText(deadline.getConLai());

        // Xử lý sự kiện click và long-click trực tiếp trên item view
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
    }
}
