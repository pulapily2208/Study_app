package com.example.study_app.ui.Deadline.Adapters;

import android.content.Context;
import android.content.res.Resources;
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
import com.example.study_app.ui.Subject.Model.Subject;
import com.google.android.material.divider.MaterialDivider;

import java.util.ArrayList;
import java.util.List;

public class AdapterMonHoc extends RecyclerView.Adapter<AdapterMonHoc.ViewHolder> {

    private final Context context;
    private List<Subject> subjectList;
    private AdapterWeek.OnDeadlineInteractionListener deadlineListener;

    public AdapterMonHoc(Context context, List<Subject> subjectList) {
        this.context = context;
        this.subjectList = subjectList;
    }

    public void setOnDeadlineInteractionListener(AdapterWeek.OnDeadlineInteractionListener listener) {
        this.deadlineListener = listener;
    }

    public void updateData(List<Subject> newSubjects) {
        this.subjectList.clear();
        this.subjectList.addAll(newSubjects);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.deadline_item_mh, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Subject subject = subjectList.get(position);
        holder.bind(subject);
    }

    @Override
    public int getItemCount() {
        return subjectList != null ? subjectList.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvTenMonHoc, tvDeadlineCount;
        Button btnExpandCollapse;
        RecyclerView lvCongViec;
        MaterialDivider divider;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTenMonHoc = itemView.findViewById(R.id.tvTenMonHoc);
            tvDeadlineCount = itemView.findViewById(R.id.tvDeadlineCount);
            btnExpandCollapse = itemView.findViewById(R.id.btnExpandCollapse);
            lvCongViec = itemView.findViewById(R.id.lvCongViec);
            divider = itemView.findViewById(R.id.divider);

            lvCongViec.setLayoutManager(new LinearLayoutManager(context));
        }

        void bind(Subject subject) {
            DeadlineDao db = new DeadlineDao(context);
            ArrayList<Deadline> deadlines = db.getDeadlinesByMaHp(subject.maHp);

            tvTenMonHoc.setText(subject.tenHp);

            Resources res = context.getResources();
            int deadlineSize = deadlines.size();
            tvDeadlineCount.setText(res.getQuantityString(R.plurals.deadline_count, deadlineSize, deadlineSize));

            AdapterDeadline deadlineAdapter = new AdapterDeadline(context, deadlines);
            if (deadlineListener != null) {
                deadlineAdapter.setOnDeadlineInteractionListener(deadlineListener);
            }
            lvCongViec.setAdapter(deadlineAdapter);

            lvCongViec.setVisibility(View.GONE);
            divider.setVisibility(View.GONE);
            btnExpandCollapse.setRotation(180);

            View.OnClickListener expandCollapseListener = v -> {
                boolean isExpanded = lvCongViec.getVisibility() == View.VISIBLE;
                lvCongViec.setVisibility(isExpanded ? View.GONE : View.VISIBLE);
                divider.setVisibility(isExpanded ? View.GONE : View.VISIBLE);
                btnExpandCollapse.animate().rotation(isExpanded ? 180 : 270).start();
            };

            itemView.setOnClickListener(expandCollapseListener);
            btnExpandCollapse.setOnClickListener(expandCollapseListener);
        }
    }
}
