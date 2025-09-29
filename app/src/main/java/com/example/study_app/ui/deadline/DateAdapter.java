package com.example.study_app.ui.deadline;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.study_app.R;

import java.util.ArrayList;

public class DateAdapter extends RecyclerView.Adapter<DateAdapter.DateViewHolder> {

    private ArrayList<DateModel> dateList;
    private Context context;

    public DateAdapter(ArrayList<DateModel> dateList, Context context) {
        this.dateList = dateList;
        this.context = context;
    }

    @NonNull
    @Override
    public DateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.deadline_item_date, parent, false);
        return new DateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DateViewHolder holder, int position) {
        DateModel model = dateList.get(position);

        holder.tvDayName.setText(model.getDayName());
        holder.tvDayNumber.setText(String.valueOf(model.getDayNumber()));

        if (model.isSelected()) {
            holder.itemView.setBackgroundResource(R.drawable.dealine_chon_ngay);
            holder.tvDayNumber.setTextColor(Color.WHITE);
            holder.tvDayName.setTextColor(Color.WHITE);
        } else {
            holder.itemView.setBackgroundResource(R.drawable.deadline_kchon_ngay);
            holder.tvDayNumber.setTextColor(Color.BLACK);
            holder.tvDayName.setTextColor(Color.BLACK);
        }

        holder.itemView.setOnClickListener(v -> {
            // reset tất cả item
            for (DateModel d : dateList) {
                d.setSelected(false);
            }
            // chọn item hiện tại
            model.setSelected(true);
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return dateList.size();
    }

    public static class DateViewHolder extends RecyclerView.ViewHolder {
        TextView tvDayName, tvDayNumber;

        public DateViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDayName = itemView.findViewById(R.id.tvDayName);
            tvDayNumber = itemView.findViewById(R.id.tvDayNumber);
        }
    }
}
