package com.example.study_app.ui.Subject.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.study_app.R;
import com.example.study_app.ui.Subject.Model.Subject;

import java.util.List;

public class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.SubjectViewHolder> {

    private List<Subject> subjectList;

    // Constructor to receive the list of subjects
    public SubjectAdapter(List<Subject> subjectList) {
        this.subjectList = subjectList;
    }

    @NonNull
    @Override
    public SubjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.subject_item, parent, false);
        return new SubjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectViewHolder holder, int position) {
        // Get the data model based on position
        Subject subject = subjectList.get(position);

        // Set item views based on your views and data model.
        // We access fields directly as the Subject class does not have getters.
        // We also only display data that we know is being fetched by DatabaseHelper.

        // Display the subject name
        if (subject.tenMon != null) {
            holder.tvSubjectName.setText(subject.tenMon);
        } else {
            holder.tvSubjectName.setText("");
        }

        // Display the subject code in the description field as it's also important
        if (subject.maMon != null) {
            holder.tvSubjectDesc.setText(subject.maMon);
        } else {
            holder.tvSubjectDesc.setText("");
        }

        // The following fields are not currently fetched from the database by getAllSubjects(),
        // so we clear them to avoid showing "null" or old data from recycled views.
        holder.tvLocation.setText("");
        holder.tvTime.setText("");
    }


    @Override
    public int getItemCount() {
        if (subjectList != null) {
            return subjectList.size();
        }
        return 0;
    }

    // A method to update the data in the adapter
    public void setSubjects(List<Subject> subjects) {
        this.subjectList = subjects;
        notifyDataSetChanged(); // Refresh the RecyclerView
    }

    /**
     * ViewHolder class to hold the item view
     */
    public static class SubjectViewHolder extends RecyclerView.ViewHolder {
        // Declare your views here
        private final View colorBar;
        private final ImageView ivTimeIcon;
        private final TextView tvTime;
        private final TextView tvSubjectName;
        private final TextView tvSubjectDesc;
        private final ImageView ivLocationIcon;
        private final TextView tvLocation;

        public SubjectViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize your views here
            colorBar = itemView.findViewById(R.id.color_bar);
            ivTimeIcon = itemView.findViewById(R.id.iv_time_icon);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvSubjectName = itemView.findViewById(R.id.tvSubjectName);
            tvSubjectDesc = itemView.findViewById(R.id.tvSubjectDesc);
            ivLocationIcon = itemView.findViewById(R.id.iv_location_icon);
            tvLocation = itemView.findViewById(R.id.tvLocation);
        }
    }
}
