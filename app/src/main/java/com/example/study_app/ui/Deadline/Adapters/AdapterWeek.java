package com.example.study_app.ui.Deadline.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.study_app.R;
import com.example.study_app.data.DatabaseHelper;
import com.example.study_app.data.DeadlineDao;
import com.example.study_app.ui.Deadline.Models.Deadline;
import com.example.study_app.ui.Deadline.Models.Week;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class AdapterWeek extends RecyclerView.Adapter<AdapterWeek.ViewHolder> {

    private Context context;
    private ArrayList<Week> weeks;
    private String maHp;
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

    public AdapterWeek(Context context, ArrayList<Week> weeks, String maHp, Date subjectStartDate) {
        this.context = context;
        this.weeks = weeks;
        this.maHp = maHp;
        setSubjectStartDate(subjectStartDate);
    }

    public void setWeeks(ArrayList<Week> weeks) {
        this.weeks = weeks;
        notifyDataSetChanged();
    }

    public void setOnAddDeadlineListener(OnAddDeadlineListener listener) {
        this.addListener = listener;
    }

    public void setOnDeadlineInteractionListener(OnDeadlineInteractionListener listener) {
        this.deadlineListener = listener;
    }

    private void setSubjectStartDate(Date subjectStartDate) {
        this.subjectStartDate = subjectStartDate;
        if (this.subjectStartDate != null) {
            long diffMillis = new Date().getTime() - this.subjectStartDate.getTime();
            this.currentWeekIndex = diffMillis >= 0 ? (int) (TimeUnit.MILLISECONDS.toDays(diffMillis) / 7) : -1;
        } else {
            this.currentWeekIndex = -1;
        }
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.deadline_item_tuan, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Week week = weeks.get(position);
        if (week == null) return;

        DeadlineDao db = new DeadlineDao(context);
        ArrayList<Deadline> deadlines = db.getDeadlinesByWeek(this.maHp, this.subjectStartDate, position);

        AdapterDeadline adapterDeadline = new AdapterDeadline(context, deadlines);
        adapterDeadline.setOnDeadlineInteractionListener(new AdapterWeek.OnDeadlineInteractionListener() {
            @Override
            public void onDeadlineClick(Deadline deadline) {
                if (deadlineListener != null) deadlineListener.onDeadlineClick(deadline);
            }

            @Override
            public void onEditDeadline(Deadline deadline) {
                if (deadlineListener != null) deadlineListener.onEditDeadline(deadline);
            }

            @Override
            public void onDeleteDeadline(Deadline deadline) {
                db.deleteDeadline(deadline.getId());
                if (deadlineListener != null) {
                    deadlineListener.onDeleteDeadline(deadline);
                }
            }

            @Override
            public void onStateChanged(Deadline deadline, boolean isCompleted) {
                deadline.setCompleted(isCompleted);
                db.updateDeadline(deadline);
                if (deadlineListener != null) {
                    deadlineListener.onStateChanged(deadline, isCompleted);
                }
            }
        });

        holder.rvCongViec.setAdapter(adapterDeadline);

        holder.tvTuan.setText(week.getTenTuan());

        if (subjectStartDate != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(subjectStartDate);
            calendar.add(Calendar.WEEK_OF_YEAR, position);
            Date weekStart = calendar.getTime();
            calendar.add(Calendar.DAY_OF_YEAR, 6);
            Date weekEnd = calendar.getTime();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
            holder.tvNgayTuan.setText(sdf.format(weekStart) + " - " + sdf.format(weekEnd));
            holder.tvNgayTuan.setVisibility(View.VISIBLE);
        } else {
            holder.tvNgayTuan.setVisibility(View.GONE);
        }

        holder.btnThem.setVisibility((currentWeekIndex != -1 && position < currentWeekIndex) ? View.GONE : View.VISIBLE);
        holder.btnThem.setOnClickListener(v -> {
            if (addListener != null) addListener.onAddDeadline(holder.getAdapterPosition());
        });
    }

    @Override
    public int getItemCount() {
        return weeks != null ? weeks.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTuan;
        TextView tvNgayTuan;
        RecyclerView rvCongViec;
        Button btnThem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTuan = itemView.findViewById(R.id.tvTuan);
            tvNgayTuan = itemView.findViewById(R.id.tvNgayTuan);
            rvCongViec = itemView.findViewById(R.id.lvCongViec);
            rvCongViec.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            btnThem = itemView.findViewById(R.id.btnThem);
        }
    }
}
