package com.example.study_app.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.study_app.Models.Deadline;
import com.example.study_app.Models.Week;
import com.example.study_app.R;

import java.util.ArrayList;

public class AdapterWeek extends ArrayAdapter<Week> {

    private Context context;
    private int resource;
    private ArrayList<Week> weeks;
    private OnAddDeadlineListener listener;
    private ArrayList<AdapterDeadline> adapters; // AdapterDeadline cho từng tuần

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

        // Tạo adapterDeadline cho từng tuần một lần
        adapters = new ArrayList<>();
        for (Week w : weeks) {
            adapters.add(new AdapterDeadline(context, R.layout.item_deadline, w.getDeadlines()));
        }
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

        // Chỉ set adapter một lần cho ListView con
        if (holder.lvCongViec.getAdapter() == null) {
            holder.lvCongViec.setAdapter(adapters.get(position));
        }

        // Cập nhật chiều cao ListView con
        setListViewHeightBasedOnChildren(holder.lvCongViec);

        holder.btnThem.setOnClickListener(v -> {
            if (listener != null) listener.onAddDeadline(position);
        });

        return convertView;
    }

    // Thêm deadline vào tuần
    public void addDeadlineToWeek(int weekIndex, Deadline dl) {
        // Thêm vào danh sách tuần
        weeks.get(weekIndex).getDeadlines().add(dl);

        // Chỉ thông báo adapterDeadline cập nhật, không cần adapterWeek.notifyDataSetChanged() nữa
        AdapterDeadline adapter = adapters.get(weekIndex);
        adapter.notifyDataSetChanged();  // cập nhật ListView con
    }

    // Tính chiều cao ListView con
    private void setListViewHeightBasedOnChildren(ListView listView) {
        android.widget.ListAdapter adapter = listView.getAdapter();
        if (adapter == null) return;

        int totalHeight = 0;
        for (int i = 0; i < adapter.getCount(); i++) {
            View listItem = adapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        android.view.ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (adapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    // ViewHolder pattern để tránh tạo lại view
    private static class ViewHolder {
        TextView tvTuan;
        ListView lvCongViec;
        Button btnThem;
    }
}
