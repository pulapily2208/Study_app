package com.example.study_app.ui.Deadline.Adapters;

import android.content.Context;
import android.graphics.Color;
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

public class AdapterDeadline extends ArrayAdapter<Deadline> {

    private Context context;
    private int resource;
    private ArrayList<Deadline> deadlines;

    public AdapterDeadline(Context context, int resource, ArrayList<Deadline> deadlines) {
        super(context, resource, deadlines);
        this.context = context;
        this.resource = resource;
        this.deadlines = deadlines;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(resource, parent, false);
        }

        TextView tvTieuDe = convertView.findViewById(R.id.tvTieuDe);
        TextView tvKetQua = convertView.findViewById(R.id.tvKetQua);
        CheckBox cbXacNhan = convertView.findViewById(R.id.cbXacNhan);
        ImageView ivAnh = convertView.findViewById(R.id.ivAnh);
        ImageView ivPin = convertView.findViewById(R.id.ivPin); // Icon ghim

        Deadline d = deadlines.get(position);
        
        // Set dữ liệu cơ bản
        ivAnh.setImageResource(d.getIcon());
        tvTieuDe.setText(d.getTieuDe());
        cbXacNhan.setChecked(d.isCompleted());

        // Xử lý trạng thái hoàn thành
        if (d.isCompleted()) {
            tvKetQua.setText("Đã hoàn thành");
        } else {
            tvKetQua.setText(d.getConLai());
        }
        
        // Xử lý trạng thái Ghim
        if (d.isPinned()) {
            ivPin.setVisibility(View.VISIBLE);
            convertView.setBackgroundColor(Color.parseColor("#FFF8E1")); // Màu vàng nhạt cho item được ghim
        } else {
            ivPin.setVisibility(View.GONE);
            convertView.setBackgroundColor(Color.WHITE); // Màu trắng mặc định
        }

        // Bắt sự kiện checkbox
        cbXacNhan.setOnCheckedChangeListener((buttonView, isChecked) -> {
            d.setCompleted(isChecked);

            if (isChecked) {
                tvKetQua.setText("Đã hoàn thành");
            } else {
                tvKetQua.setText(d.getConLai());
            }
        });

        return convertView;
    }

    public interface OnDeadlineActionListener {
        void onEdit(Deadline d, int position);
        void onDelete(Deadline d, int position);
        void onPin(Deadline d, int position);
    }
}
