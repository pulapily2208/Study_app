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
import com.example.study_app.ui.Deadline.Models.*;

import java.util.ArrayList;

public class AdapterDeadline extends ArrayAdapter<Deadline> {

    private final Context context;
    private final int resource;
    private final ArrayList<Deadline> deadlines;

    private OnDeadlineStateChangeListener stateChangeListener;

    public interface OnDeadlineStateChangeListener {
        void onStateChanged(Deadline deadline, boolean isCompleted);
    }

    public void setOnDeadlineStateChangeListener(OnDeadlineStateChangeListener listener) {
        this.stateChangeListener = listener;
    }

    public AdapterDeadline(Context context, int resource, ArrayList<Deadline> deadlines) {
        super(context, resource, deadlines);
        this.context = context;
        this.resource = resource;
        this.deadlines = deadlines != null ? deadlines : new ArrayList<>();
    }

    @Override
    public int getCount() {
        return deadlines.size();
    }

    @Override
    public Deadline getItem(int position) {
        return deadlines.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(resource, parent, false);
            holder = new ViewHolder();
            holder.tvTieuDe = convertView.findViewById(R.id.tvTieuDe);
            holder.tvKetQua = convertView.findViewById(R.id.tvKetQua);
            holder.cbXacNhan = convertView.findViewById(R.id.cbXacNhan);
            holder.ivAnh = convertView.findViewById(R.id.ivAnh);
            holder.ivPin = convertView.findViewById(R.id.ivPin);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Deadline d = getItem(position);

        // Set dữ liệu cơ bản
        holder.ivAnh.setImageResource(d.getIcon());
        holder.tvTieuDe.setText(d.getTieuDe());

        // Reset listener trước khi set trạng thái CheckBox
        holder.cbXacNhan.setOnCheckedChangeListener(null);
        holder.cbXacNhan.setChecked(d.isCompleted());
        holder.cbXacNhan.setOnCheckedChangeListener((buttonView, isChecked) -> {
            d.setCompleted(isChecked);
            holder.tvKetQua.setText(isChecked ? "Đã hoàn thành" : d.getConLai());

            if (stateChangeListener != null) {
                stateChangeListener.onStateChanged(d, isChecked);
            }
        });

        // Set trạng thái kết quả
        holder.tvKetQua.setText(d.isCompleted() ? "Đã hoàn thành" : d.getConLai());

        // Xử lý trạng thái Pinned
        if (d.isPinned()) {
            holder.ivPin.setVisibility(View.VISIBLE);
            convertView.setBackgroundColor(Color.parseColor("#FFF8E1")); // màu vàng nhạt
        } else {
            holder.ivPin.setVisibility(View.GONE);
            convertView.setBackgroundColor(Color.WHITE);
        }

        return convertView;
    }

    private static class ViewHolder {
        TextView tvTieuDe;
        TextView tvKetQua;
        CheckBox cbXacNhan;
        ImageView ivAnh;
        ImageView ivPin;
    }
}
