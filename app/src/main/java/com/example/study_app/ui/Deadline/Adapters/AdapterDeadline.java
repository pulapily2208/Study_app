package com.example.study_app.ui.Deadline.Adapters;

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

public class AdapterDeadline extends ArrayAdapter<Deadline> {

    private Context context;
    private int resource;
    private ArrayList<Deadline> deadlines;//danh sách các deadline

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
        CheckBox cbXacNhan= convertView.findViewById(R.id.cbXacNhan);
        ImageView ivAnh = convertView.findViewById(R.id.ivAnh);



        ivAnh.setImageResource(deadlines.get(position).getIcon());

        Deadline d = deadlines.get(position);
        tvTieuDe.setText(d.getTieuDe());
        cbXacNhan.setChecked(d.isCompleted());

        if(d.isCompleted()){
            tvKetQua.setText("Đã hoàn thành");
        }else{
            tvKetQua.setText(d.getConLai());
        }

        // Bắt sự kiện checkbox
        cbXacNhan.setOnCheckedChangeListener((buttonView, isChecked) -> {
            d.setCompleted(isChecked); // Lưu trạng thái vào object

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
