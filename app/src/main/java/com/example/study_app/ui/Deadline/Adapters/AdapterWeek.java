package com.example.study_app.ui.Deadline.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.study_app.R;
import com.example.study_app.ui.Deadline.Models.*;
//import com.example.study_app.ui.Deadline.Models.Week;

import java.util.ArrayList;

public class AdapterWeek extends ArrayAdapter<Week> {

    private final Context context;
    private final int resource;
    private final ArrayList<Week> weeks;

    // --- Listeners for communication with Activity ---
    private OnAddDeadlineListener addListener;
    private OnDeadlineLongClickListener longClickListener;
    private OnDeadlineStateChangeListener stateChangeListener;

    // --- Interfaces for listeners ---
    public interface OnAddDeadlineListener {
        void onAddDeadline(int weekIndex);
    }

    public interface OnDeadlineLongClickListener {
        void onDeadlineLongClick(int weekIndex, int deadlineIndex, Deadline deadline);
    }

    public interface OnDeadlineStateChangeListener {
        void onStateChanged(Deadline deadline, boolean isCompleted);
    }

    // --- Setters for listeners ---
    public void setOnAddDeadlineListener(OnAddDeadlineListener listener) {
        this.addListener = listener;
    }

    public void setOnDeadlineLongClickListener(OnDeadlineLongClickListener listener) {
        this.longClickListener = listener;
    }
    
    public void setOnDeadlineStateChangeListener(OnDeadlineStateChangeListener listener) {
        this.stateChangeListener = listener;
    }


    public AdapterWeek(Context context, int resource, ArrayList<Week> weeks) {
        super(context, resource, weeks);
        this.context = context;
        this.resource = resource;
        this.weeks = weeks;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(resource, parent, false);
            holder = new ViewHolder();
            holder.tvTuan = convertView.findViewById(R.id.tvTuan);
            holder.lvCongViec = convertView.findViewById(R.id.lvCongViec);
            holder.btnThem = convertView.findViewById(R.id.btnThem);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Week week = weeks.get(position);
        holder.tvTuan.setText(week.getTenTuan());

        // Create a new adapter for the deadlines in this specific week
        AdapterDeadline deadlineAdapter = new AdapterDeadline(context, R.layout.deadline_item, week.getDeadlines());
        holder.lvCongViec.setAdapter(deadlineAdapter);

        // --- Pass events from child adapter (Deadline) to this adapter's listeners ---

        // 1. Pass Checkbox change event
        deadlineAdapter.setOnDeadlineStateChangeListener((deadline, isCompleted) -> {
            if (stateChangeListener != null) {
                // Pass it up to the Activity
                stateChangeListener.onStateChanged(deadline, isCompleted);
            }
        });

        // 2. Pass Long Click event
        holder.lvCongViec.setOnItemLongClickListener((parentView, view, deadlinePosition, id) -> {
            if (longClickListener != null) {
                Deadline clickedDeadline = week.getDeadlines().get(deadlinePosition);
                longClickListener.onDeadlineLongClick(position, deadlinePosition, clickedDeadline);
            }
            return true; // Consume the long click event
        });
        
        // 3. Handle "Add" button click
        holder.btnThem.setOnClickListener(v -> {
            if (addListener != null) {
                addListener.onAddDeadline(position);
            }
        });

        // Utility to adjust ListView height
        setListViewHeightBasedOnChildren(holder.lvCongViec);

        return convertView;
    }

    private static void setListViewHeightBasedOnChildren(ListView listView) {
        android.widget.ListAdapter adapter = listView.getAdapter();
        if (adapter == null) return;

        int totalHeight = 0;
        for (int i = 0; i < adapter.getCount(); i++) {
            View listItem = adapter.getView(i, null, listView);
            listItem.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (adapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    private static class ViewHolder {
        TextView tvTuan;
        ListView lvCongViec;
        Button btnThem;
    }
}
