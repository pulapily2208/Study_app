package com.example.study_app.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.study_app.Models.Week;
import com.example.study_app.R;

import java.util.ArrayList;

public class AdapterWeek extends ArrayAdapter<Week> {

    private Context context;
    private int resource;
    private ArrayList<Week> weeks;
    private OnAddDeadlineListener listener;

    public interface OnAddDeadlineListener {
        void onAddDeadline(int weekIndex);
    }

    public void setOnAddDeadlineListener(OnAddDeadlineListener listener) {
        this.listener = listener;
    }

    public AdapterWeek(Context context, int resource, ArrayList<Week> weeks) {
        super(context, resource, weeks);
        this.context = context;
        this.resource = resource;
        this.weeks = weeks;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(resource, parent, false);
        }

        TextView tvTuan = convertView.findViewById(R.id.tvTuan);
        ListView lvCongViec = convertView.findViewById(R.id.lvCongViec);
        Button btnThem = convertView.findViewById(R.id.btnThem);

        Week week = weeks.get(position);
        tvTuan.setText(week.getTenTuan());

        AdapterDeadline adapterDeadline = new AdapterDeadline(
                context,
                R.layout.item_deadline,
                week.getDeadlines()
        );
        lvCongViec.setAdapter(adapterDeadline);

        btnThem.setOnClickListener(v -> {
            if (listener != null) listener.onAddDeadline(position);
        });

        return convertView;
    }
}
