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
import com.example.study_app.ui.Deadline.Models.Deadline;
import com.example.study_app.ui.Deadline.Models.Week;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class AdapterWeek extends ArrayAdapter<Week> {

    private Context context;
    private int resource;
    private OnAddDeadlineListener addListener;
    private OnDeadlineInteractionListener deadlineListener;
    private Date subjectStartDate;
    private int currentWeekIndex = -1;

    public interface OnAddDeadlineListener {
        void onAddDeadline(int weekIndex);
    }

    public interface OnDeadlineInteractionListener {
        void onDeadlineClick(Deadline deadline);
        void onEditDeadline(Deadline deadline);
        void onDeleteDeadline(Deadline deadline);
        void onStateChanged(Deadline deadline, boolean isCompleted);
    }

    public void setOnAddDeadlineListener(OnAddDeadlineListener listener) {
        this.addListener = listener;
    }

    public void setOnDeadlineInteractionListener(OnDeadlineInteractionListener listener) {
        this.deadlineListener = listener;
    }

    public void setSubjectStartDate(Date subjectStartDate) {
        this.subjectStartDate = subjectStartDate;
        if (this.subjectStartDate != null) {
            long diffMillis = new Date().getTime() - this.subjectStartDate.getTime();
            if (diffMillis >= 0) {
                this.currentWeekIndex = (int) (TimeUnit.MILLISECONDS.toDays(diffMillis) / 7);
            } else {
                this.currentWeekIndex = -1; // Subject starts in the future
            }
        } else {
            this.currentWeekIndex = -1;
        }
    }

    public AdapterWeek(Context context, int resource, ArrayList<Week> weeks, Date subjectStartDate) {
        super(context, resource, weeks);
        this.context = context;
        this.resource = resource;
        setSubjectStartDate(subjectStartDate);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(resource, parent, false);
            holder = new ViewHolder();
            holder.tvTuan = convertView.findViewById(R.id.tvTuan);
            holder.tvNgayTuan = convertView.findViewById(R.id.tvNgayTuan);
            holder.lvCongViec = convertView.findViewById(R.id.lvCongViec);
            holder.btnThem = convertView.findViewById(R.id.btnThem);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Week week = getItem(position);
        if (week == null) return convertView;

        holder.tvTuan.setText(week.getTenTuan());

        if (subjectStartDate != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(subjectStartDate);
            calendar.add(Calendar.DAY_OF_YEAR, position * 7);
            Date weekStart = calendar.getTime();

            calendar.add(Calendar.DAY_OF_YEAR, 6);
            Date weekEnd = calendar.getTime();

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
            String dateRange = sdf.format(weekStart) + " - " + sdf.format(weekEnd);
            holder.tvNgayTuan.setText(dateRange);
            holder.tvNgayTuan.setVisibility(View.VISIBLE);
        } else {
            holder.tvNgayTuan.setText("");
            holder.tvNgayTuan.setVisibility(View.GONE);
        }

        // Sửa lại logic ở đây: Ẩn nút Thêm cho các tuần trong quá khứ
        if (currentWeekIndex != -1 && position < currentWeekIndex) {
            holder.btnThem.setVisibility(View.GONE);
        } else {
            holder.btnThem.setVisibility(View.VISIBLE);
        }

        AdapterDeadline deadlineAdapter = new AdapterDeadline(context, R.layout.deadline_item, week.getDeadlines());
        deadlineAdapter.setOnDeadlineInteractionListener(this.deadlineListener);
        holder.lvCongViec.setAdapter(deadlineAdapter);

        setListViewHeightBasedOnChildren(holder.lvCongViec);

        holder.btnThem.setOnClickListener(v -> {
            if (addListener != null) addListener.onAddDeadline(position);
        });

        holder.lvCongViec.setFocusable(false);
        holder.lvCongViec.setClickable(false);
        holder.lvCongViec.setFocusableInTouchMode(false);

        return convertView;
    }

    private void setListViewHeightBasedOnChildren(ListView listView) {
        android.widget.ListAdapter adapter = listView.getAdapter();
        if (adapter == null || adapter.getCount() == 0) {
            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = 0;
            listView.setLayoutParams(params);
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < adapter.getCount(); i++) {
            View listItem = adapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (adapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    private static class ViewHolder {
        TextView tvTuan;
        TextView tvNgayTuan;
        ListView lvCongViec;
        Button btnThem;
    }
}