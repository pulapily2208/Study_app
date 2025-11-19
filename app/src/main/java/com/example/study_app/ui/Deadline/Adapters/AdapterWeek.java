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

import java.util.ArrayList;

public class AdapterWeek extends ArrayAdapter<Week> {

    private Context context;
    private int resource;
    private ArrayList<Week> weeks;
    private OnAddDeadlineListener listener;
    private OnDeadlineLongClickListener longClickListener; // Listener cho sự kiện nhấn giữ
    private ArrayList<AdapterDeadline> adapters;

    // Interface thêm mới
    public interface OnAddDeadlineListener {
        void onAddDeadline(int weekIndex);
    }

    // Interface nhấn giữ (Mới thêm)
    public interface OnDeadlineLongClickListener {
        void onDeadlineLongClick(int weekIndex, int deadlineIndex, Deadline deadline);
    }

    public void setOnAddDeadlineListener(OnAddDeadlineListener listener) {
        this.listener = listener;
    }

    public void setOnDeadlineLongClickListener(OnDeadlineLongClickListener listener) {
        this.longClickListener = listener;
    }

    public AdapterWeek(Context context, int resource, ArrayList<Week> weeks) {
        super(context, resource, weeks);
        this.context = context;
        this.resource = resource;
        this.weeks = weeks;

        adapters = new ArrayList<>();
        for (Week w : weeks) {
            adapters.add(new AdapterDeadline(context, R.layout.deadline_item, w.getDeadlines()));
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

        if (holder.lvCongViec.getAdapter() == null) {
            holder.lvCongViec.setAdapter(adapters.get(position));
        }

        // Xử lý sự kiện nhấn giữ vào item con (Deadline)
        holder.lvCongViec.setOnItemLongClickListener((parentAv, viewAv, positionAv, idAv) -> {
            if (longClickListener != null) {
                // position: vị trí tuần, positionAv: vị trí deadline trong tuần đó
                longClickListener.onDeadlineLongClick(position, positionAv, week.getDeadlines().get(positionAv));
            }
            return true; // Trả về true để không kích hoạt thêm sự kiện click thường
        });

        setListViewHeightBasedOnChildren(holder.lvCongViec);

        holder.btnThem.setOnClickListener(v -> {
            if (listener != null) listener.onAddDeadline(position);
        });

        return convertView;
    }

    public void addDeadlineToWeek(int weekIndex, Deadline dl) {
        weeks.get(weekIndex).getDeadlines().add(dl);
        AdapterDeadline adapter = adapters.get(weekIndex);
        adapter.notifyDataSetChanged();
    }
    
    // Hàm cập nhật UI sau khi xóa/sửa
    public void updateWeek(int weekIndex) {
        if (weekIndex >= 0 && weekIndex < adapters.size()) {
            adapters.get(weekIndex).notifyDataSetChanged();
        }
    }

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

    private static class ViewHolder {
        TextView tvTuan;
        ListView lvCongViec;
        Button btnThem;
    }
}
